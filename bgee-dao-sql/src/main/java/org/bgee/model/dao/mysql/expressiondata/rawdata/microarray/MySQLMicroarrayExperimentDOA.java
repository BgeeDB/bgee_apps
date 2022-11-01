package org.bgee.model.dao.mysql.expressiondata.rawdata.microarray;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixProbesetDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.MicroarrayExperimentDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
import org.bgee.model.dao.mysql.expressiondata.rawdata.MySQLRawDataDAO;

public class MySQLMicroarrayExperimentDOA extends MySQLRawDataDAO<MicroarrayExperimentDAO.Attribute> 
        implements MicroarrayExperimentDAO {

    private static final Logger log = LogManager.getLogger(MySQLMicroarrayExperimentDOA.class.getName());
    public static final String TABLE_NAME = "microarrayExperiment";
    private static final String CHIP_TABLE_NAME = MySQLAffymetrixChipDAO.TABLE_NAME;
    private static final String PROBESET_TABLE_NAME = MySQLAffymetrixProbesetDAO.TABLE_NAME;

    public MySQLMicroarrayExperimentDOA(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public MicroarrayExperimentTOResultSet getExperiments(Collection<DAORawDataFilter> rawDataFilters, Integer limit,
            Integer offset, Collection<MicroarrayExperimentDAO.Attribute> attrs)
            throws DAOException {
        log.traceEntry("{}, {}, {}, {}", rawDataFilters, limit, offset, attrs);
        if (rawDataFilters == null || rawDataFilters.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("rawDataFilter can not be null or empty"));
        }
        // force to have a list in order to keep order of elements. It is mandatory to be able
        // to first generate a parameterised query and then add values.
        final List<DAORawDataFilter> orderedRawDataFilter = 
                Collections.unmodifiableList(new ArrayList<>(rawDataFilters));
        final Set<MicroarrayExperimentDAO.Attribute> clonedAttrs = Collections
                .unmodifiableSet(attrs == null || attrs.isEmpty()?
                EnumSet.allOf(MicroarrayExperimentDAO.Attribute.class): EnumSet.copyOf(attrs));
        // generate SELECT
        StringBuilder sb = new StringBuilder();
        sb.append(generateSelectClause(TABLE_NAME, getColToAttributesMap(MicroarrayExperimentDAO
                .Attribute.class), true, clonedAttrs));
        // generate FROM
        boolean needJoinChip = false;
        boolean needJoinCond = false;
        boolean needJoinProbeset = false;
        if(rawDataFilters.stream().anyMatch(e -> !e.getAssayIds().isEmpty() ||
                e.getSpeciesId() != null || !e.getGeneIds().isEmpty() ||
                !e.getRawDataCondIds().isEmpty())) {
            needJoinChip = true;
        }
        if (rawDataFilters.stream().anyMatch(e -> e.getSpeciesId() != null)) {
            needJoinCond = true;
        }
        if (rawDataFilters.stream().anyMatch(e -> !e.getGeneIds().isEmpty())) {
            needJoinProbeset = true;
        }
        //generate FROM clause
        sb.append(generateFromClauseAffymetrix(TABLE_NAME, false, needJoinChip, needJoinProbeset,
                needJoinCond, false));

        // generate WHERE
        // there is always a where condition as at least a speciesId, a geneId or a conditionId
        // has to be provided in a rawDataFilter.
        sb.append(" WHERE ").append(generateWhereClause(orderedRawDataFilter));
        //generate offset and limit
        if (limit != null && offset != null) {
            sb.append(" LIMIT " + offset + ", " + limit);
        }
        //add values to parameterized queries
        try {
            BgeePreparedStatement stmt = this.parameteriseQuery(sb.toString(),
                    orderedRawDataFilter);
            return log.traceExit(new MySQLMicroarrayExperimentTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    private String generateWhereClause(List<DAORawDataFilter> rawDataFilters) {
        String whereClause = rawDataFilters.stream().map(e -> {
            return this.generateOneFilterWhereClause(e);
        }).collect(Collectors.joining(") OR (", " (", ")"));
        return whereClause;
    }

    private String generateOneFilterWhereClause(DAORawDataFilter rawDataFilter) {
        log.traceEntry("{}", rawDataFilter);
      Integer speId = rawDataFilter.getSpeciesId();
      Set<Integer> geneIds = rawDataFilter.getGeneIds();
      Set<Integer> rawDataCondIds = rawDataFilter.getRawDataCondIds();
      Set<String> expIds = rawDataFilter.getExperimentIds();
      Set<String> assayIds = rawDataFilter.getAssayIds();
      boolean isExpAssayUnion = rawDataFilter.isExprIdsAssayIdsUnion();
      Set<String> expAssayMerged = isExpAssayUnion ? new HashSet<>(): 
          Stream.concat(expIds.stream(), assayIds.stream()).collect(Collectors.toSet());
      boolean filterFound = false;
      StringBuilder sb = new StringBuilder();
        // FITLER ON EXPERIMENT IDS
        if (!expIds.isEmpty() || !expAssayMerged.isEmpty()) {
            sb.append(TABLE_NAME).append(".")
            .append(MicroarrayExperimentDAO.Attribute.ID.getTOFieldName()).append(" IN (")
            .append(BgeePreparedStatement.generateParameterizedQueryString(
                    isExpAssayUnion ? expAssayMerged.size() : expIds.size()));
            sb.append(")");
            filterFound = true;
        }
        // FILTER ON Chip IDS
        if (!assayIds.isEmpty() || !expAssayMerged.isEmpty()) {
            if(filterFound) {
                if(isExpAssayUnion) {
                    sb.append(" OR ");
                }else {
                    sb.append(" AND ");
                }
            }
            //has to filter on chip table as probeset table only contains bgee chip ID
            sb.append(CHIP_TABLE_NAME).append(".")
            .append(AffymetrixChipDAO.Attribute.AFFYMETRIX_CHIP_ID.getTOFieldName()).append(" IN (")
            .append(BgeePreparedStatement.generateParameterizedQueryString(
                    isExpAssayUnion ? expAssayMerged.size() : assayIds.size()));
            sb.append(")");
            filterFound = true;
        }
        // FILTER ON SPECIES ID
        if (speId != null) {
            if(filterFound) {
                sb.append(" AND ");
            }
            sb.append(CONDITION_TABLE_NAME).append(".")
            .append(RawDataConditionDAO.Attribute.SPECIES_ID.getTOFieldName()).append(" = ")
            .append(speId);
            filterFound = true;
        }
        // FILTER ON RAW CONDITION IDS
        if (!rawDataCondIds.isEmpty()) {
            if(filterFound) {
                sb.append(" AND ");
            }
            sb.append(CHIP_TABLE_NAME).append(".")
            .append(AffymetrixChipDAO.Attribute.CONDITION_ID.getTOFieldName()).append(" IN (")
            .append(BgeePreparedStatement.generateParameterizedQueryString(rawDataCondIds
                    .size()))
            .append(")");
            filterFound = true;
        }
        // FILTER ON GENE IDS
        if (!geneIds.isEmpty()) {
            if(filterFound) {
                sb.append(" AND ");
            }
            sb.append(PROBESET_TABLE_NAME).append(".")
            .append(AffymetrixProbesetDAO.Attribute.BGEE_GENE_ID.getTOFieldName()).append(" IN (")
            .append(BgeePreparedStatement.generateParameterizedQueryString(geneIds.size()))
            .append(")");
        }
        return sb.toString();
    }
    
    class MySQLMicroarrayExperimentTOResultSet extends MySQLDAOResultSet<MicroarrayExperimentTO> 
    implements MicroarrayExperimentTOResultSet{

        /**
         * @param statement The {@code BgeePreparedStatement}
         */
        private MySQLMicroarrayExperimentTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected MicroarrayExperimentDAO.MicroarrayExperimentTO getNewTO() throws DAOException {
            log.traceEntry();
            try {
                final ResultSet currentResultSet = this.getCurrentResultSet();
                Integer dataSourceId = null;
                String id = null, name = null, description = null;

                for (Entry<Integer, String> column : this.getColumnLabels().entrySet()) {
                    if (column.getValue().equals(MicroarrayExperimentDAO.Attribute.ID.getTOFieldName())) {
                        id = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(MicroarrayExperimentDAO.Attribute.NAME
                            .getTOFieldName())) {
                        name = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(MicroarrayExperimentDAO.Attribute.DESCRIPTION
                            .getTOFieldName())) {
                        description = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(MicroarrayExperimentDAO.Attribute.DATA_SOURCE_ID
                            .getTOFieldName())) {
                        dataSourceId = currentResultSet.getInt(column.getKey());
                    } else {
                        log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }
                }
                return log.traceExit(new MicroarrayExperimentTO(id, name, description, dataSourceId));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }

}
