package org.bgee.model.dao.mysql.expressiondata.rawdata.microarray;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
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
    private static final String TABLE_NAME = "affymetrixChip";
    private static final String PROBESET_TABLE_NAME = "affymetrixProbeset";

    public MySQLAffymetrixChipDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public AffymetrixChipTOResultSet getAffymetrixChips(Collection<String> experimentIds, 
            Collection<String> bgeeChipIds, DAORawDataFilter filter, 
            Collection<AffymetrixChipDAO.Attribute> attrs)
            throws DAOException {
        log.traceEntry("{}, {}, {}, {}", experimentIds, bgeeChipIds, filter, attrs);
        final Set<String> clonedExpIds = Collections.unmodifiableSet(experimentIds == null?
                new HashSet<>(): new HashSet<>(experimentIds));
        final Set<String> clonedBgeeChipIds = Collections.unmodifiableSet(bgeeChipIds == null?
                new HashSet<>(): new HashSet<>(bgeeChipIds));
        final DAORawDataFilter clonedFilter = new DAORawDataFilter(filter);
        final Set<AffymetrixChipDAO.Attribute> clonedAttrs = Collections.unmodifiableSet(attrs == null? 
                EnumSet.noneOf(AffymetrixChipDAO.Attribute.class): EnumSet.copyOf(attrs));

        // generate SELECT
        StringBuilder sb = new StringBuilder();
        sb.append(generateSelectClause(TABLE_NAME, getColToAttributesMap(AffymetrixChipDAO
                .Attribute.class), true, clonedAttrs))
        // generate FROM
        .append(generateFromClause(clonedFilter));

        // generate WHERE CLAUSE
        if (!clonedExpIds.isEmpty() || !clonedBgeeChipIds.isEmpty() 
                || !clonedFilter.getSpeciesIds().isEmpty() || !clonedFilter.getGeneIds().isEmpty()
                || !clonedFilter.getConditionFilters().isEmpty()) {
          sb.append(" WHERE ");
        }
        boolean filterFound = false;
        // FITER ON EXPERIMENT IDS
        if (!clonedExpIds.isEmpty()) {
            sb.append(TABLE_NAME).append(".")
            .append(AffymetrixChipDAO.Attribute.EXPERIMENT_ID.getTOFieldName()).append(" IN (")
            .append(BgeePreparedStatement.generateParameterizedQueryString(clonedExpIds.size()))
            .append(")");
            filterFound = true;
        }
        // FITER ON LIBRARY IDS
        if (!clonedBgeeChipIds.isEmpty()) {
            if(filterFound) {
                sb.append(" AND ");
            }
            sb.append(TABLE_NAME).append(".")
            .append(AffymetrixChipDAO.Attribute.BGEE_AFFYMETRIX_CHIP_ID.getTOFieldName())
            .append(" IN (")
            .append(BgeePreparedStatement.generateParameterizedQueryString(clonedBgeeChipIds.size()))
            .append(")");
            filterFound = true;
        }
        // FITER ON SPECIES IDS
        if (!clonedFilter.getSpeciesIds().isEmpty()) {
            if(filterFound) {
                sb.append(" AND ");
            }
            sb.append(CONDITION_TABLE_NAME).append(".")
            .append(RawDataConditionDAO.Attribute.SPECIES_ID.getTOFieldName()).append(" IN (")
            .append(BgeePreparedStatement.generateParameterizedQueryString(clonedFilter
                    .getSpeciesIds().size()))
            .append(")");
            filterFound = true;
        }
        // FITER ON GENE IDS
        if (!clonedFilter.getGeneIds().isEmpty()) {
            if(filterFound) {
                sb.append(" AND ");
            }
            sb.append(PROBESET_TABLE_NAME).append(".")
            .append(AffymetrixProbesetDAO.Attribute.BGEE_GENE_ID.getTOFieldName()).append(" IN (")
            .append(BgeePreparedStatement.generateParameterizedQueryString(clonedFilter
                    .getGeneIds().size()))
            .append(")");
            filterFound = true;
        }
        // FILTER ON RAW CONDITIONS
        if (!clonedFilter.getConditionFilters().isEmpty()) {
            if(filterFound) {
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
            if (!clonedBgeeChipIds.isEmpty()) {
                stmt.setStrings(paramIndex, clonedBgeeChipIds, true);
                paramIndex += clonedBgeeChipIds.size();
            }
            if (!clonedFilter.getSpeciesIds().isEmpty()) {
                stmt.setIntegers(paramIndex, clonedFilter.getSpeciesIds(), true);
                paramIndex += clonedFilter.getSpeciesIds().size();
            }
            if (!clonedFilter.getGeneIds().isEmpty()) {
                stmt.setIntegers(paramIndex, clonedFilter.getGeneIds(), true);
                paramIndex += clonedFilter.getGeneIds().size();
            }
            configureRawDataConditionFiltersStmt(stmt,
                    clonedFilter.getConditionFilters(), paramIndex);
            return log.traceExit(new MySQLAffymetrixChipTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    
    @Override
    public AffymetrixChipTOResultSet getAffymetrixChipsFromRawDataFilter(
            DAORawDataFilter filter, Collection<AffymetrixChipDAO.Attribute> attrs)
            throws DAOException {
        log.traceEntry("{}, {}", filter, attrs);
        return log.traceExit(getAffymetrixChips(null, null, filter, attrs));
    }
    
    @Override
    public AffymetrixChipTOResultSet getAffymetrixChipFromIds(Collection<String> bgeeChipIds,
            Collection<AffymetrixChipDAO.Attribute> attrs)
            throws DAOException {
        log.traceEntry("{}, {}", bgeeChipIds, attrs);
        return log.traceExit(getAffymetrixChips(null, bgeeChipIds, null, attrs));
    }

    private String generateFromClause(DAORawDataFilter filter) {
        log.traceEntry("{}", filter);
        StringBuilder sb = new StringBuilder();
        sb.append(" FROM " + TABLE_NAME);
        // join on affymetrixProbeset table
        if(!filter.getGeneIds().isEmpty()) {
            sb.append(" INNER JOIN " + PROBESET_TABLE_NAME + " ON ")
            .append(TABLE_NAME + "." + AffymetrixChipDAO.Attribute
                    .BGEE_AFFYMETRIX_CHIP_ID.getTOFieldName())
            .append(" = " + PROBESET_TABLE_NAME + "." 
                    + AffymetrixProbesetDAO.Attribute.BGEE_AFFYMETRIX_CHIP_ID.getTOFieldName());
        }
        // join on cond table
        if(!filter.getSpeciesIds().isEmpty() || !filter.getConditionFilters().isEmpty()) {
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
