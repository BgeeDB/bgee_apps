package org.bgee.model.dao.mysql.expressiondata.rawdata.microarray;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.rawdata.DAOProcessedRawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO.AffymetrixChipTO.DetectionType;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO.AffymetrixChipTO.NormalizationType;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
import org.bgee.model.dao.mysql.expressiondata.rawdata.MySQLRawDataDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.RawDataFiltersToDatabaseMapping;

public class MySQLAffymetrixChipDAO extends MySQLRawDataDAO<AffymetrixChipDAO.Attribute>
        implements AffymetrixChipDAO{

    private static final Logger log = LogManager.getLogger(MySQLAffymetrixChipDAO.class.getName());
    public static final String TABLE_NAME = "affymetrixChip";

    public MySQLAffymetrixChipDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public AffymetrixChipTOResultSet getAffymetrixChips(Collection<DAORawDataFilter> rawDataFilters,
            Integer offset, Integer limit, Collection<AffymetrixChipDAO.Attribute> attrs)
            throws DAOException {
        log.traceEntry("{}, {}, {}, {}", rawDataFilters, offset, limit, attrs);
        checkOffsetAndLimit(offset, limit);

        final DAOProcessedRawDataFilter processedFilters =
                new DAOProcessedRawDataFilter(rawDataFilters);
        final Set<AffymetrixChipDAO.Attribute> clonedAttrs = Collections
                .unmodifiableSet(attrs == null || attrs.isEmpty()?
                EnumSet.allOf(AffymetrixChipDAO.Attribute.class): EnumSet.copyOf(attrs));

        StringBuilder sb = new StringBuilder();

        // generate SELECT
        sb.append(generateSelectClauseRawDataFilters(processedFilters, TABLE_NAME,
                getColToAttributesMap(AffymetrixChipDAO.Attribute.class), true, clonedAttrs));

        // generate FROM
        RawDataFiltersToDatabaseMapping filtersToDatabaseMapping = generateFromClauseRawData(sb,
                processedFilters, null, Set.of(TABLE_NAME), DAODataType.AFFYMETRIX);

        // generate WHERE CLAUSE
        if (!processedFilters.getRawDataFilters().isEmpty()) {
            sb.append(" WHERE ").append(generateWhereClauseRawDataFilter(processedFilters,
                    filtersToDatabaseMapping));
        }

        // generate ORDER BY
        sb.append(" ORDER BY")
        .append(" " + TABLE_NAME + "." + AffymetrixChipDAO.Attribute.EXPERIMENT_ID
                .getTOFieldName())
        .append(", " + TABLE_NAME + "." + AffymetrixChipDAO.Attribute.BGEE_AFFYMETRIX_CHIP_ID
                .getTOFieldName());

        //generate offset and limit
        if (limit != null) {
            sb.append(offset == null ? " LIMIT ?": " LIMIT ?, ?");
        }
        try {
            BgeePreparedStatement stmt = this.parameterizeQuery(sb.toString(), processedFilters,
                    DAODataType.AFFYMETRIX, offset, limit);
            return log.traceExit(new MySQLAffymetrixChipTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public AffymetrixChipTOResultSet getAffymetrixChipsFromBgeeChipIds(
            Collection<Integer> bgeeChipIds, Collection<AffymetrixChipDAO.Attribute> attrs)
            throws DAOException {
        log.traceEntry("{}, {}", bgeeChipIds, attrs);
        if (bgeeChipIds == null || bgeeChipIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("need to provide at least one"
                    + "bgeeChipId"));
        }
        final Set<AffymetrixChipDAO.Attribute> clonedAttrs = Collections
                .unmodifiableSet(attrs == null || attrs.isEmpty()?
                EnumSet.allOf(AffymetrixChipDAO.Attribute.class): EnumSet.copyOf(attrs));
        final Set<Integer> clonedBgeeChipIds = Collections.unmodifiableSet(bgeeChipIds.stream()
                .filter(id -> id != null).collect(Collectors.toSet()));
     // generate SELECT
        StringBuilder sb = new StringBuilder();
        sb.append(generateSelectClause(TABLE_NAME, getColToAttributesMap(AffymetrixChipDAO
                .Attribute.class), true, clonedAttrs))
        .append(" FROM ").append(TABLE_NAME).append(" WHERE ")
        .append(AffymetrixChipDAO.Attribute.BGEE_AFFYMETRIX_CHIP_ID.getTOFieldName())
        .append(" IN (")
        .append(BgeePreparedStatement.generateParameterizedQueryString(clonedBgeeChipIds.size()))
        .append(")");
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection()
                    .prepareStatement(sb.toString());
            stmt.setIntegers(1, clonedBgeeChipIds, true);
            return log.traceExit(new MySQLAffymetrixChipTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
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
