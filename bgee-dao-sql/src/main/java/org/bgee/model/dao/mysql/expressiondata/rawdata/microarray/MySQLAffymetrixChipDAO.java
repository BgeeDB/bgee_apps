package org.bgee.model.dao.mysql.expressiondata.rawdata.microarray;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO.AffymetrixChipTO.DetectionType;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO.AffymetrixChipTO.NormalizationType;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
import org.bgee.model.dao.mysql.expressiondata.rawdata.MySQLRawDataConditionDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.MySQLRawDataDAO;

public class MySQLAffymetrixChipDAO extends MySQLRawDataDAO<AffymetrixChipDAO.Attribute>
        implements AffymetrixChipDAO{

    private static final Logger log = LogManager.getLogger(MySQLAffymetrixChipDAO.class.getName());
    public static final String TABLE_NAME = "affymetrixChip";

    public MySQLAffymetrixChipDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    // The query was taking too much time when querying genes. In order to solve this issue
    // the order of the table can be manually decided rather than using the MySQL query planner.
    // If all DAORawDataFilter contain geneIds, then affymetrixProbeset will always be the first
    // table followed by affymetrixChip and at the end cond.
    // In case of several DAORawDataFilter and if not all of them contain geneIds it is safer to
    // let MySQL otpimize the query plan. Indeed gene IDs could be used in one
    // DAORawDataFilter but not in the other ones. Forcing MySQL to first use the affymetrixProbeset
    // could then result in a loss of performance.
    @Override
    public AffymetrixChipTOResultSet getAffymetrixChips(Collection<DAORawDataFilter> rawDataFilters,
            Integer offset, Integer limit, Collection<AffymetrixChipDAO.Attribute> attrs)
            throws DAOException {
        log.traceEntry("{}, {}, {}, {}", rawDataFilters, offset, limit, attrs);
        checkOffsetAndLimit(offset, limit);
        // force to have a list in order to keep order of elements. It is mandatory to be able
        // to first generate a parameterised query and then add values.
        final List<DAORawDataFilter> orderedRawDataFilters = 
                Collections.unmodifiableList(rawDataFilters == null? new ArrayList<>():
                    new ArrayList<>(rawDataFilters));
        final Set<AffymetrixChipDAO.Attribute> clonedAttrs = Collections
                .unmodifiableSet(attrs == null || attrs.isEmpty()?
                EnumSet.allOf(AffymetrixChipDAO.Attribute.class): EnumSet.copyOf(attrs));
        //detect join to use
        boolean needJoinProbeset = orderedRawDataFilters.stream().anyMatch(e -> !e.getGeneIds().isEmpty());
        boolean needJoinCond = orderedRawDataFilters.stream().anyMatch(e -> e.getSpeciesId() != null);

        StringBuilder sb = new StringBuilder();

        // generate SELECT
        // do not let MySQL decide the execution plan if all DAORawDataFilter contain geneIDs.
        // This is done by adding STRAIGHT_JOIN in the select clause
        boolean allFiltersContainGeneIds = orderedRawDataFilters.stream()
                .allMatch(c -> !c.getGeneIds().isEmpty());
        sb.append(generateSelectClause(TABLE_NAME, getColToAttributesMap(AffymetrixChipDAO
                .Attribute.class), true, allFiltersContainGeneIds, clonedAttrs));

        // generate FROM
        // if require to join to probeset table, then start the FROM clause with this table. Has a
        // huge impact on time to run the query if the STRAIGHT_JOIN clause is used.
        if (needJoinProbeset) {
            sb.append(generateFromClauseAffymetrix(MySQLAffymetrixProbesetDAO.TABLE_NAME, false, true,
                    false, needJoinCond, false));
        } else {
            sb.append(generateFromClauseAffymetrix(TABLE_NAME, false, false, needJoinProbeset,
                    needJoinCond, false));
        }

        // generate WHERE CLAUSE
        if (!orderedRawDataFilters.isEmpty()) {
            sb.append(" WHERE ").append(generateWhereClause(orderedRawDataFilters,
                    MySQLAffymetrixChipDAO.TABLE_NAME, MySQLRawDataConditionDAO.TABLE_NAME));
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
            BgeePreparedStatement stmt = this.parameterizeQuery(sb.toString(), orderedRawDataFilters,
                    offset, limit);
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
