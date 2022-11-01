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
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCallSourceDAO.CallSourceDataTO.ExclusionReason;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixProbesetDAO;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.expressiondata.rawdata.MySQLRawDataDAO;
import org.bgee.model.dao.mysql.gene.MySQLGeneDAO;


public class MySQLAffymetrixProbesetDAO extends MySQLRawDataDAO<AffymetrixProbesetDAO.Attribute>
                                        implements AffymetrixProbesetDAO {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(MySQLAffymetrixProbesetDAO.class.getName());
    public final static String TABLE_NAME = "affymetrixProbeset";
    private final static String CHIP_TABLE_NAME = MySQLAffymetrixChipDAO.TABLE_NAME;
    private final static String GENE_TABLE_NAME = MySQLGeneDAO.TABLE_NAME;

    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     * @param manager                   the {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException If {@code manager} is {@code null}.
     */
    public MySQLAffymetrixProbesetDAO(MySQLDAOManager manager) {
        super(manager);
    }

    @Override
    public AffymetrixProbesetTOResultSet getAffymetrixProbesets(Collection<DAORawDataFilter> rawDataFilters,
            Integer limit, Integer offset, Collection<AffymetrixProbesetDAO.Attribute> attrs)
            throws DAOException {
        log.traceEntry("{}, {}, {}, {}", rawDataFilters, limit, offset, attrs);
//        if (rawDataFilter == null) {
//            throw log.throwing(new IllegalArgumentException("rawDataFilter can not be null"));
//        }
//        checkLimitAndOffset(offset, limit);
//        final Integer clonedSpeId = rawDataFilter.getSpeciesId();
//        final Set<Integer> clonedGeneIds = Collections.unmodifiableSet(
//                rawDataFilter.getGeneIds());
//        final Set<Integer> clonedRawDataCondIds = Collections.unmodifiableSet(
//                rawDataFilter.getRawDataCondIds());
//        final Set<String> clonedExpIds = Collections.unmodifiableSet(
//                rawDataFilter.getExperimentIds());
//        final Set<String> clonedChipIds = Collections.unmodifiableSet(
//                rawDataFilter.getAssayIds());
//        final boolean clonedExpChipUnion = rawDataFilter.isExprIdsAssayIdsUnion();
//        final Set<AffymetrixProbesetDAO.Attribute> clonedAttrs = Collections
//                .unmodifiableSet(attrs == null?
//                EnumSet.noneOf(AffymetrixProbesetDAO.Attribute.class): EnumSet.copyOf(attrs));
//        // generate SELECT
        StringBuilder sb = new StringBuilder();
//        sb.append(generateSelectClause(TABLE_NAME, getColToAttributesMap(AffymetrixProbesetDAO
//                .Attribute.class), true, clonedAttrs))
//        // generate FROM
//        .append(generateFromClause(clonedSpeId, clonedExpIds, clonedChipIds, clonedRawDataCondIds));
//        // generate WHERE
//          sb.append(" WHERE ");
//        boolean filterFound = false;
//     // FILTER ON EXPERIMENT IDs
//        if (!clonedExpIds.isEmpty() || clonedExpChipUnion && !clonedChipIds.isEmpty()) {
//            sb.append(CHIP_TABLE_NAME).append(".")
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
//        // FILTER ON CHIP IDs
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
//        // FITER ON SPECIES IDS
//        if (clonedSpeId != null) {
//            if(filterFound) {
//                sb.append(" AND ");
//            }
//            sb.append(CONDITION_TABLE_NAME).append(".")
//            .append(RawDataConditionDAO.Attribute.SPECIES_ID.getTOFieldName()).append(" = ")
//            .append(clonedSpeId);
//            filterFound = true;
//        }
//        // FITER ON GENE IDS
//        if (!clonedGeneIds.isEmpty()) {
//            if(filterFound) {
//                sb.append(" AND ");
//            }
//            sb.append(TABLE_NAME).append(".")
//            .append(AffymetrixProbesetDAO.Attribute.BGEE_GENE_ID.getTOFieldName()).append(" IN (")
//            .append(BgeePreparedStatement.generateParameterizedQueryString(
//                    clonedGeneIds.size()))
//            .append(")");
//            filterFound = true;
//        }
//        // FILTER ON RAW CONDITIONS
//        if (!clonedRawDataCondIds.isEmpty()) {
//            if(filterFound) {
//                sb.append(" AND ");
//            }
//            sb.append(CHIP_TABLE_NAME).append(".")
//            .append(AffymetrixChipDAO.Attribute.AFFYMETRIX_CHIP_ID.getTOFieldName()).append(" IN (")
//            .append(BgeePreparedStatement.generateParameterizedQueryString(
//                    clonedRawDataCondIds.size()))
//            .append(")");
//        }
//
//        //add values to parameterized queries
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
//            if (!clonedGeneIds.isEmpty()) {
//                stmt.setIntegers(paramIndex, clonedGeneIds, true);
//                paramIndex += clonedGeneIds.size();
//            }
//            if (!clonedRawDataCondIds.isEmpty()) {
//                stmt.setIntegers(paramIndex, clonedRawDataCondIds, true);
//                paramIndex += clonedRawDataCondIds.size();
//            }
//            
////            configureRawDataConditionFiltersStmt(stmt, clonedRawDataFilter.getConditionFilters(),
////                    paramIndex);
            return log.traceExit(new MySQLAffymetrixProbesetTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    private String generateFromClause(Integer speciesId, Set<String> expIds, Set<String> chipIds, Set<Integer> condIds) {
        log.traceEntry("{}, {}, {}", speciesId, expIds, condIds);
        StringBuilder sb = new StringBuilder();
        sb.append(" FROM " + TABLE_NAME + " ");
        if (!expIds.isEmpty() || !condIds.isEmpty() || !chipIds.isEmpty()) {            
            sb.append("INNER JOIN " + CHIP_TABLE_NAME + " ON ")
            .append(TABLE_NAME + "." + AffymetrixProbesetDAO.Attribute
                    .BGEE_AFFYMETRIX_CHIP_ID.getTOFieldName())
            .append(" = " + CHIP_TABLE_NAME + "." + AffymetrixChipDAO.Attribute
                    .BGEE_AFFYMETRIX_CHIP_ID.getTOFieldName());
            //if joined already to chip table then join to cond table to retrieve species info
            if (speciesId != null) {
                sb.append(" INNER JOIN " + GENE_TABLE_NAME + " ON ")
                .append(TABLE_NAME + "." + AffymetrixProbesetDAO.Attribute.BGEE_GENE_ID.getTOFieldName())
                .append(" = " + GENE_TABLE_NAME + "." + GeneDAO.Attribute.ID.getTOFieldName());
            }
        //if only species need a jonction then join to gene table to retrieve species info            
        } else if (speciesId != null) {
            sb.append(" INNER JOIN " + CONDITION_TABLE_NAME + " ON ")
            .append(CONDITION_TABLE_NAME + "." + RawDataConditionDAO.Attribute.ID.getTOFieldName())
            .append(" = " + CHIP_TABLE_NAME + "." + AffymetrixChipDAO.Attribute.CONDITION_ID.getTOFieldName());
        }
        return log.traceExit(sb.toString());
    }

    class MySQLAffymetrixProbesetTOResultSet extends MySQLDAOResultSet<AffymetrixProbesetTO>
    implements AffymetrixProbesetTOResultSet{

    /**
     * @param statement The {@code BgeePreparedStatement}
     */
    private MySQLAffymetrixProbesetTOResultSet(BgeePreparedStatement statement) {
        super(statement);
    }

    @Override
    protected AffymetrixProbesetDAO.AffymetrixProbesetTO getNewTO() throws DAOException {
        log.traceEntry();
        try {
            final ResultSet currentResultSet = this.getCurrentResultSet();
            Integer bgeeAffymetrixChipId = null, bgeeGeneId = null;
            String affymetrixProbesetId = null, affymetrixData = null, reasonForExclusion = null;
            Long expressionId = null;
            BigDecimal normalizedSignalIntensity = null, pValue = null, qValue = null, rank = null;

            for (Entry<Integer, String> column : this.getColumnLabels().entrySet()) {
                if (column.getValue().equals(AffymetrixProbesetDAO.Attribute.ID
                        .getTOFieldName())) {
                    affymetrixProbesetId = currentResultSet.getString(column.getKey());
                } else if(column.getValue().equals(AffymetrixProbesetDAO.Attribute
                        .BGEE_AFFYMETRIX_CHIP_ID.getTOFieldName())) {
                    bgeeAffymetrixChipId = currentResultSet.getInt(column.getKey());
                } else if(column.getValue().equals(AffymetrixProbesetDAO.Attribute.BGEE_GENE_ID
                        .getTOFieldName())) {
                    bgeeGeneId = currentResultSet.getInt(column.getKey());
                } else if(column.getValue().equals(AffymetrixProbesetDAO.Attribute
                        .NORMALIZED_SIGNAL_INTENSITY.getTOFieldName())) {
                    normalizedSignalIntensity = currentResultSet.getBigDecimal(column.getKey());
                } else if(column.getValue().equals(AffymetrixProbesetDAO.Attribute.PVALUE
                        .getTOFieldName())) {
                    pValue = currentResultSet.getBigDecimal(column.getKey());
                } else if(column.getValue().equals(AffymetrixProbesetDAO.Attribute.QVALUE
                        .getTOFieldName())) {
                    qValue = currentResultSet.getBigDecimal(column.getKey());
                } else if(column.getValue().equals(AffymetrixProbesetDAO.Attribute.EXPRESSION_ID
                        .getTOFieldName())) {
                    expressionId = currentResultSet.getLong(column.getKey());
                } else if(column.getValue().equals(AffymetrixProbesetDAO.Attribute.RANK
                        .getTOFieldName())) {
                    rank = currentResultSet.getBigDecimal(column.getKey());
                } else if(column.getValue().equals(AffymetrixProbesetDAO.Attribute.AFFYMETRIX_DATA
                        .getTOFieldName())) {
                    affymetrixData = currentResultSet.getString(column.getKey());
                } else if(column.getValue().equals(AffymetrixProbesetDAO.Attribute
                        .REASON_FOR_EXCLUSION.getTOFieldName())) {
                    reasonForExclusion = currentResultSet.getString(column.getKey());
                }  
                // currently disabled this exception as the database schema still contain columns like
                // detectionFlag and rawDetectionFlag that are not used and should be removed
                // from the database
//                else {
//                    log.throwing(new UnrecognizedColumnException(column.getValue()));
//                }
            }
            return log.traceExit(new AffymetrixProbesetTO(affymetrixProbesetId, bgeeAffymetrixChipId,
                    bgeeGeneId, normalizedSignalIntensity, pValue, qValue, expressionId, rank,
                    DataState.convertToDataState(affymetrixData),
                    ExclusionReason.convertToExclusionReason(reasonForExclusion)));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }
}
