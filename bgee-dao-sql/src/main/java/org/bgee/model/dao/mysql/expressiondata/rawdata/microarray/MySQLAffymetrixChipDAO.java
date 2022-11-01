package org.bgee.model.dao.mysql.expressiondata.rawdata.microarray;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map.Entry;
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
            throw log.throwing(new IllegalArgumentException("rawDataFilter can not be null"));
        }
        checkLimitAndOffset(offset, limit);
        final Set<AffymetrixChipDAO.Attribute> clonedAttrs = Collections
                .unmodifiableSet(attrs == null || attrs.isEmpty()?
                EnumSet.allOf(AffymetrixChipDAO.Attribute.class): EnumSet.copyOf(attrs));
        //detect join to use
        boolean needJoinProbeset = rawDataFilters.stream().anyMatch(e -> !e.getGeneIds().isEmpty());
        boolean needJoinCond = rawDataFilters.stream().anyMatch(e -> e.getSpeciesId() != null);

        // generate SELECT
        StringBuilder sb = new StringBuilder();
//        sb.append(generateSelectClause(TABLE_NAME, getColToAttributesMap(AffymetrixChipDAO
//                .Attribute.class), true, clonedAttrs))
//        // generate FROM
//        .append(generateFromClause(needJoinProbeset, needJoinCond));
//
//        // generate WHERE CLAUSE
//        // there is always a where condition as at least a speciesId, a geneId or a conditionId
//        // has to be provided
//        sb.append(" WHERE ");
//        boolean filterFound = false;
//        // FITLER ON EXPERIMENT IDS
//        if (!clonedExpIds.isEmpty() || clonedExpChipUnion && !clonedChipIds.isEmpty()) {
//            sb.append(TABLE_NAME).append(".")
//            .append(AffymetrixChipDAO.Attribute.EXPERIMENT_ID.getTOFieldName()).append(" IN (")
//            .append(BgeePreparedStatement.generateParameterizedQueryString(clonedExpIds.size()));
//            // if filter on the union of expIds and assayIds
//            if (clonedExpChipUnion && !clonedChipIds.isEmpty()) {
//                sb.append(BgeePreparedStatement.generateParameterizedQueryString(clonedChipIds
//                        .size()));
//            }
//            sb.append(")");
//            filterFound = true;
//        }
//        // FILTER ON Chip IDS
//        if (!clonedChipIds.isEmpty() || clonedExpChipUnion && !clonedExpIds.isEmpty()) {
//            if(filterFound) {
//                if(clonedExpChipUnion) {
//                    sb.append(" OR ");
//                }else {
//                    sb.append(" AND ");
//                }
//            }
//            sb.append(TABLE_NAME).append(".")
//            .append(AffymetrixChipDAO.Attribute.AFFYMETRIX_CHIP_ID.getTOFieldName())
//            .append(" IN (")
//            .append(BgeePreparedStatement.generateParameterizedQueryString(clonedChipIds.size()));
//            // if filter on the union of expIds and assayIds
//            if (clonedExpChipUnion && !clonedExpIds.isEmpty()) {
//                sb.append(BgeePreparedStatement.generateParameterizedQueryString(clonedExpIds
//                        .size()));
//            }
//            sb.append(")");
//            filterFound = true;
//        }
//        // FILTER ON SPECIES ID
//        if (clonedSpeId != null) {
//            if(filterFound) {
//                sb.append(" AND ");
//            }
//            sb.append(CONDITION_TABLE_NAME).append(".")
//            .append(RawDataConditionDAO.Attribute.SPECIES_ID.getTOFieldName()).append(" = ")
//            .append(clonedSpeId);
//            filterFound = true;
//        }
//        // FILTER ON RAW CONDITION IDS
//        if (!clonedrawDataCondIds.isEmpty()) {
//            if(filterFound) {
//                sb.append(" AND ");
//            }
//            sb.append(CONDITION_TABLE_NAME).append(".")
//            .append(RawDataConditionDAO.Attribute.ID.getTOFieldName()).append(" IN (")
//            .append(BgeePreparedStatement.generateParameterizedQueryString(clonedrawDataCondIds
//                    .size()))
//            .append(")");
//            filterFound = true;
//        }
//        // FILTER ON GENE IDS
//        if (!clonedGeneIds.isEmpty()) {
//            if(filterFound) {
//                sb.append(" AND ");
//            }
//            sb.append(PROBESET_TABLE_NAME).append(".")
//            .append(AffymetrixProbesetDAO.Attribute.BGEE_GENE_ID.getTOFieldName()).append(" IN (")
//            .append(BgeePreparedStatement.generateParameterizedQueryString(clonedGeneIds.size()))
//            .append(")");
//        }
//        //generate offset and limit
//        if (limit != null && offset != null) {
//            sb.append(" LIMIT " + offset + ", " + limit);
//        }
        //add values to parameterized queries
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection()
                    .prepareStatement(sb.toString());
//            int paramIndex = 1;
//            if (!clonedExpIds.isEmpty() || clonedExpChipUnion && !clonedChipIds.isEmpty()) {
//                stmt.setStrings(paramIndex, clonedExpIds, true);
//                paramIndex += clonedExpIds.size();
//                if (clonedExpChipUnion && !clonedChipIds.isEmpty()) {
//                    stmt.setStrings(paramIndex, clonedChipIds, true);
//                    paramIndex += clonedChipIds.size();
//                }
//            }
//            if (!clonedChipIds.isEmpty() || clonedExpChipUnion && !clonedExpIds.isEmpty()) {
//                stmt.setStrings(paramIndex, clonedChipIds, true);
//                paramIndex += clonedChipIds.size();
//                if (clonedExpChipUnion && !clonedExpIds.isEmpty()) {
//                    stmt.setStrings(paramIndex, clonedExpIds, true);
//                    paramIndex += clonedExpIds.size();
//                }
//            }
//            if (!clonedrawDataCondIds.isEmpty()) {
//                stmt.setIntegers(paramIndex, clonedrawDataCondIds, true);
//                paramIndex += clonedrawDataCondIds.size();
//            }
//            if (!clonedGeneIds.isEmpty()) {
//                stmt.setIntegers(paramIndex, clonedGeneIds, true);
//                paramIndex += clonedGeneIds.size();
//            }
            return log.traceExit(new MySQLAffymetrixChipTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    private String generateFromClause(Integer speciesId, Set<Integer> geneIds,
            Set<Integer> rawDataCondIds) {
        log.traceEntry("{}, {}, {}", speciesId, geneIds, rawDataCondIds);
        StringBuilder sb = new StringBuilder();
        sb.append(" FROM " + TABLE_NAME);
        // join on affymetrixProbeset table if geneIds provided.
        if (!geneIds.isEmpty()) {
            sb.append(" INNER JOIN " + PROBESET_TABLE_NAME + " ON ")
            .append(TABLE_NAME + "." + AffymetrixChipDAO.Attribute
                    .BGEE_AFFYMETRIX_CHIP_ID.getTOFieldName())
            .append(" = " + PROBESET_TABLE_NAME + "." 
                    + AffymetrixProbesetDAO.Attribute.BGEE_AFFYMETRIX_CHIP_ID.getTOFieldName());
        }
        // join on cond table
        if (speciesId != null) {
            sb.append(" INNER JOIN " + CONDITION_TABLE_NAME + " ON ")
            .append(TABLE_NAME + "." + AffymetrixChipDAO.Attribute.CONDITION_ID.getTOFieldName())
            .append(" = " + CONDITION_TABLE_NAME + "." 
                    + RawDataConditionDAO.Attribute.ID.getTOFieldName());
        }
        return log.traceExit(sb.toString());
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
