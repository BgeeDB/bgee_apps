package org.bgee.model.dao.mysql.expressiondata.rawdata.microarray;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO.AffymetrixChipTO.DetectionType;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO.AffymetrixChipTO.NormalizationType;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixProbesetDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
import org.bgee.model.dao.mysql.expressiondata.rawdata.MySQLRawDataDAO;

public class MySQLAffymetrixChipDAO extends MySQLRawDataDAO<AffymetrixChipDAO.Attribute>
        implements AffymetrixChipDAO{

    private static final Logger log = LogManager.getLogger(MySQLAffymetrixChipDAO.class.getName());
    public static final String TABLE_NAME = "affymetrixChip";
    private static final String PROBESET_TABLE_NAME = MySQLAffymetrixProbesetDAO.TABLE_NAME;

    public MySQLAffymetrixChipDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public AffymetrixChipTOResultSet getAffymetrixChips(Collection<DAORawDataFilter> rawDataFilters,
            Integer offset, Integer limit, Collection<AffymetrixChipDAO.Attribute> attrs)
            throws DAOException {
        log.traceEntry("{}, {}, {}, {}", rawDataFilters, attrs);
        if (rawDataFilters == null) {
            throw log.throwing(new IllegalArgumentException("rawDataFilters can not be null"));
        }
        checkLimitAndOffset(offset, limit);
        // force to have a list in order to keep order of elements. It is mandatory to be able
        // to first generate a parameterised query and then add values.
        final List<DAORawDataFilter> orderedRawDataFilter = 
                Collections.unmodifiableList(new ArrayList<>(rawDataFilters));
        final Set<AffymetrixChipDAO.Attribute> clonedAttrs = Collections
                .unmodifiableSet(attrs == null || attrs.isEmpty()?
                EnumSet.allOf(AffymetrixChipDAO.Attribute.class): EnumSet.copyOf(attrs));
        //detect join to use
        boolean needJoinProbeset = rawDataFilters.stream().anyMatch(e -> !e.getGeneIds().isEmpty());
        boolean needJoinCond = rawDataFilters.stream().anyMatch(e -> e.getSpeciesId() != null);

        // generate SELECT
        StringBuilder sb = new StringBuilder();
        sb.append(generateSelectClause(TABLE_NAME, getColToAttributesMap(AffymetrixChipDAO
                .Attribute.class), true, clonedAttrs))
        // generate FROM
        .append(generateFromClauseAffymetrix(TABLE_NAME, false, false, needJoinProbeset,
                needJoinCond, false));

        // generate WHERE CLAUSE
        // there is always a where condition as at least a speciesId, a geneId or a conditionId
        // has to be provided in a rawDataFilter.
        sb.append(" WHERE ").append(generateWhereClause(orderedRawDataFilter));
        //generate offset and limit
        if (limit != null && offset != null) {
            sb.append(" LIMIT " + offset + ", " + limit);
        }
        return log.traceExit(parameteriseAndRunQuery(orderedRawDataFilter, sb.toString()));
    }

    private MySQLAffymetrixChipTOResultSet parameteriseAndRunQuery(
            List<DAORawDataFilter> rawDataFilters, String query) {
        //add values to parameterised queries
        try {
            BgeePreparedStatement stmt = this.parameteriseQuery(query, rawDataFilters);
            return log.traceExit(new MySQLAffymetrixChipTOResultSet(stmt));
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
            .append(AffymetrixChipDAO.Attribute.EXPERIMENT_ID.getTOFieldName()).append(" IN (")
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
            sb.append(TABLE_NAME).append(".")
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
            sb.append(TABLE_NAME).append(".")
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

    class MySQLAffymetrixChipTOResultSet extends MySQLDAOResultSet<AffymetrixChipTO> 
    implements AffymetrixChipTOResultSet{

    /**
     * @param statement The {@code BgeePreparedStatement}
     */
    private MySQLAffymetrixChipTOResultSet(BgeePreparedStatement statement) {
        super(statement);
    }
    
    @Override
    protected AffymetrixChipDAO.AffymetrixChipTO getNewTO() throws DAOException {
        log.traceEntry();
        try {
            final ResultSet currentResultSet = this.getCurrentResultSet();
            Integer bgeeAffymetrixChipId = null, conditionId = null, chipDistinctRankCount = null;
            String affymetrixChipId = null, microarrayExperimentId = null, chipTypeId = null,
                    scanDate = null, normalizationType = null, detectionType = null;
            BigDecimal qualityScore = null, percentPresent = null, chipMaxRank = null;

            for (Entry<Integer, String> column : this.getColumnLabels().entrySet()) {
                if (column.getValue().equals(AffymetrixChipDAO.Attribute.BGEE_AFFYMETRIX_CHIP_ID
                        .getTOFieldName())) {
                    bgeeAffymetrixChipId = currentResultSet.getInt(column.getKey());
                } else if(column.getValue().equals(AffymetrixChipDAO.Attribute.AFFYMETRIX_CHIP_ID
                        .getTOFieldName())) {
                    affymetrixChipId = currentResultSet.getString(column.getKey());
                } else if(column.getValue().equals(AffymetrixChipDAO.Attribute.EXPERIMENT_ID
                        .getTOFieldName())) {
                    microarrayExperimentId = currentResultSet.getString(column.getKey());
                } else if(column.getValue().equals(AffymetrixChipDAO.Attribute.CHIP_TYPE_ID
                        .getTOFieldName())) {
                    chipTypeId = currentResultSet.getString(column.getKey());
                } else if(column.getValue().equals(AffymetrixChipDAO.Attribute.SCAN_DATE
                        .getTOFieldName())) {
                    scanDate = currentResultSet.getString(column.getKey());
                } else if(column.getValue().equals(AffymetrixChipDAO.Attribute.NORMALIZATION_TYPE
                        .getTOFieldName())) {
                    normalizationType = currentResultSet.getString(column.getKey());
                } else if(column.getValue().equals(AffymetrixChipDAO.Attribute.DETECTION_TYPE
                        .getTOFieldName())) {
                    detectionType = currentResultSet.getString(column.getKey());
                } else if(column.getValue().equals(AffymetrixChipDAO.Attribute.CONDITION_ID
                        .getTOFieldName())) {
                    conditionId = currentResultSet.getInt(column.getKey());
                } else if(column.getValue().equals(AffymetrixChipDAO.Attribute.QUALITY_SCORE
                        .getTOFieldName())) {
                    qualityScore = currentResultSet.getBigDecimal(column.getKey());
                } else if(column.getValue().equals(AffymetrixChipDAO.Attribute.PERCENT_PRESENT
                        .getTOFieldName())) {
                    percentPresent = currentResultSet.getBigDecimal(column.getKey());
                } else if(column.getValue().equals(AffymetrixChipDAO.Attribute.MAX_RANK
                        .getTOFieldName())) {
                    chipMaxRank = currentResultSet.getBigDecimal(column.getKey());
                } else if(column.getValue().equals(AffymetrixChipDAO.Attribute.DISTINCT_RANK_COUNT
                        .getTOFieldName())) {
                    chipDistinctRankCount = currentResultSet.getInt(column.getKey());
                } else {
                    log.throwing(new UnrecognizedColumnException(column.getValue()));
                }
            }
            return log.traceExit(new AffymetrixChipTO(bgeeAffymetrixChipId, affymetrixChipId,
                    microarrayExperimentId, chipTypeId, scanDate,
                    NormalizationType.convertToNormalizationType(normalizationType),
                    DetectionType.convertToDetectionType(detectionType), conditionId, qualityScore,
                    percentPresent, chipMaxRank, chipDistinctRankCount));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }
}
