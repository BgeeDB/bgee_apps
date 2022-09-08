package org.bgee.model.dao.mysql.expressiondata.rawdata.microarray;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

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
    public MicroarrayExperimentTOResultSet getExperimentFromIds(Collection<String> experimentIds,
            Collection<MicroarrayExperimentDAO.Attribute> attrs)
            throws DAOException {
        log.traceEntry("{}, {}", experimentIds, attrs);
        return log.traceExit(getExperiments(experimentIds, null, null, attrs));
    }

    @Override
    public MicroarrayExperimentTOResultSet getExperiments(Collection<String> experimentIds,
            Collection<String> chipIds, DAORawDataFilter filter,
            Collection<MicroarrayExperimentDAO.Attribute> attrs)
            throws DAOException {
        log.traceEntry("{}, {}, {}, {}", experimentIds, chipIds, filter, attrs);
        final Set<String> clonedExpIds = Collections.unmodifiableSet(experimentIds == null?
                new HashSet<String>(): new HashSet<String>(experimentIds));
        final Set<String> clonedChipIds = Collections.unmodifiableSet(chipIds == null?
                new HashSet<String>(): new HashSet<String>(chipIds));
        final DAORawDataFilter clonedFilter = new DAORawDataFilter(filter);
        final Set<MicroarrayExperimentDAO.Attribute> clonedAttrs = 
                Collections.unmodifiableSet(attrs == null?
                new HashSet<MicroarrayExperimentDAO.Attribute>():
                    new HashSet<MicroarrayExperimentDAO.Attribute>(attrs));

        // generate SELECT
        StringBuilder sb = new StringBuilder();
        sb.append(generateSelectClause(TABLE_NAME, getColToAttributesMap(MicroarrayExperimentDAO
                .Attribute.class), true, clonedAttrs))
        // generate FROM
        .append(generateFromClause(clonedFilter, clonedChipIds));

        // generate WHERE
        if(!clonedExpIds.isEmpty() || !clonedFilter.getSpeciesIds().isEmpty()
                || !clonedFilter.getGeneIds().isEmpty()
                || !clonedFilter.getConditionFilters().isEmpty()) {
            sb.append(" WHERE ");
        }
        boolean alreadyFiltered = false;
        // FILTER on microarray experiment Ids
        if (!clonedExpIds.isEmpty()) {
            sb.append(TABLE_NAME + ".")
            .append(MicroarrayExperimentDAO.Attribute.ID.getTOFieldName()).append(" IN (")
            .append(BgeePreparedStatement
                    .generateParameterizedQueryString(clonedExpIds.size()))
            .append(")");
            alreadyFiltered = true;
        }
        // FILTER on microarray Chip Ids
        if (!clonedChipIds.isEmpty()) {
            if (alreadyFiltered) {
                sb.append(" AND ");
            }
            sb.append(CHIP_TABLE_NAME + ".")
            .append(AffymetrixChipDAO.Attribute.AFFYMETRIX_CHIP_ID.getTOFieldName()).append(" IN (")
            .append(BgeePreparedStatement
                    .generateParameterizedQueryString(clonedChipIds.size()))
            .append(")");
            alreadyFiltered = true;
        }
        // FILTER on species Ids
        if (!clonedFilter.getSpeciesIds().isEmpty()) {
            if (alreadyFiltered) {
                sb.append(" AND ");
            }
            sb.append(CONDITION_TABLE_NAME + ".")
            .append(RawDataConditionDAO.Attribute.SPECIES_ID.getTOFieldName()).append(" IN (")
            .append(BgeePreparedStatement
                    .generateParameterizedQueryString(clonedFilter.getSpeciesIds().size()))
            .append(")");
            alreadyFiltered = true;
        }
        // FILTER on species Ids
        if (!clonedFilter.getGeneIds().isEmpty()) {
            if (alreadyFiltered) {
                sb.append(" AND ");
            }
            sb.append(PROBESET_TABLE_NAME + ".")
            .append(AffymetrixProbesetDAO.Attribute.BGEE_GENE_ID.getTOFieldName()).append(" IN (")
            .append(BgeePreparedStatement
                    .generateParameterizedQueryString(clonedFilter.getGeneIds().size()))
            .append(")");
            alreadyFiltered = true;
        }
     // FILTER on raw conditions
        if (!clonedFilter.getConditionFilters().isEmpty()) {
            if(alreadyFiltered) {
                sb.append(" AND ");
            }
            sb.append(clonedFilter.getConditionFilters().stream()
                    .map(cf -> generateOneConditionFilter(cf))
                    .collect(Collectors.joining(" OR ", "(", ")")));
        }
        

        //add values to parameterized queries
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection()
                    .prepareStatement(sb.toString());
            int paramIndex = 1;
            if (!clonedExpIds.isEmpty()) {
                stmt.setStrings(paramIndex, clonedExpIds, true);
                paramIndex += clonedExpIds.size();
            }
            if (!clonedChipIds.isEmpty()) {
                stmt.setStrings(paramIndex, clonedChipIds, true);
                paramIndex += clonedChipIds.size();
            }
            if (!clonedFilter.getSpeciesIds().isEmpty()) {
                stmt.setIntegers(paramIndex, clonedFilter.getSpeciesIds(), true);
                paramIndex += clonedFilter.getSpeciesIds().size();
            }
            if (!clonedFilter.getGeneIds().isEmpty()) {
                stmt.setIntegers(paramIndex, clonedFilter.getGeneIds(), true);
                paramIndex += clonedFilter.getGeneIds().size();
            }
            configureRawDataConditionFiltersStmt(stmt, clonedFilter.getConditionFilters(),
                    paramIndex);
            return log.traceExit(new MySQLMicroarrayExperimentTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    private String generateFromClause(DAORawDataFilter filter, Collection<String> chipIds) {
        log.traceEntry("{}, {}", filter, chipIds);
        StringBuilder sb = new StringBuilder();
        sb.append(" FROM " + TABLE_NAME);
        // join affymetrixChip table
        if(!filter.getGeneIds() .isEmpty() || !filter.getSpeciesIds().isEmpty()
                || !filter.getConditionFilters().isEmpty() || !chipIds.isEmpty()) {
            sb.append(" INNER JOIN " + CHIP_TABLE_NAME + " ON ")
            .append(TABLE_NAME + "." + MicroarrayExperimentDAO.Attribute.ID
                    .getTOFieldName())
            .append(" = " + CHIP_TABLE_NAME + "." 
                    + AffymetrixChipDAO.Attribute.EXPERIMENT_ID.getTOFieldName());
        }
        // join affymetrixProbeset table
        if(!filter.getGeneIds() .isEmpty()) {
            sb.append(" INNER JOIN " + PROBESET_TABLE_NAME + " ON ")
            .append(CHIP_TABLE_NAME + "." + AffymetrixChipDAO.Attribute.BGEE_AFFYMETRIX_CHIP_ID
                    .getTOFieldName())
            .append(" = " + PROBESET_TABLE_NAME + "." 
                    + AffymetrixProbesetDAO.Attribute.BGEE_AFFYMETRIX_CHIP_ID.getTOFieldName());
        }
        // join cond table
        if(!filter.getSpeciesIds() .isEmpty() || !filter.getConditionFilters().isEmpty()) {
            sb.append(" INNER JOIN " + CONDITION_TABLE_NAME + " ON ")
            .append(CHIP_TABLE_NAME + "." + AffymetrixChipDAO.Attribute.CONDITION_ID
                    .getTOFieldName())
            .append(" = " + CONDITION_TABLE_NAME + "." 
                    + RawDataConditionDAO.Attribute.ID.getTOFieldName());
        }
        return log.traceExit(sb.toString());
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
