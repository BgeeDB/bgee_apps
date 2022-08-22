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
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
import org.bgee.model.dao.mysql.expressiondata.rawdata.MySQLRawDataDAO;


public class MySQLAffymetrixProbesetDAO extends MySQLRawDataDAO<AffymetrixProbesetDAO.Attribute>
                                        implements AffymetrixProbesetDAO {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(MySQLAffymetrixProbesetDAO.class.getName());
    private final static String TABLE_NAME = "affymetrixProbeset";
    private final static String CHIP_TABLE_NAME = "affymetrixChip";

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
    public AffymetrixProbesetTOResultSet getAffymetrixProbesetsFromRawDataFilter(
            DAORawDataFilter rawDataFilter,
            Collection<AffymetrixProbesetDAO.Attribute> attrs)
            throws DAOException {
        log.traceEntry("{}, {}", rawDataFilter,attrs);
        return log.traceExit(getAffymetrixProbesets(null, null, rawDataFilter, attrs));
    }

    @Override
    public AffymetrixProbesetTOResultSet getAffymetrixProbesets(Collection<String> probesetIds,
            Collection<String> bgeeChipIds, DAORawDataFilter rawDataFilter,
            Collection<AffymetrixProbesetDAO.Attribute> attrs)
            throws DAOException {
        log.traceEntry("{}, {}, {}, {}", probesetIds, bgeeChipIds, rawDataFilter, attrs);
        final Set<String> clonedProbesetIds = Collections.unmodifiableSet(probesetIds == null?
                new HashSet<>(): new HashSet<>(probesetIds));
        final Set<String> clonedBgeeChipIds = Collections.unmodifiableSet(bgeeChipIds == null?
                new HashSet<>(): new HashSet<>(bgeeChipIds));
        final DAORawDataFilter clonedRawDataFilter = new DAORawDataFilter(rawDataFilter);
        final Set<AffymetrixProbesetDAO.Attribute> clonedAttrs = Collections.unmodifiableSet(attrs == null?
                EnumSet.noneOf(AffymetrixProbesetDAO.Attribute.class): EnumSet.copyOf(attrs));
        // generate SELECT
        StringBuilder sb = new StringBuilder();
        sb.append(generateSelectClause(TABLE_NAME, getColToAttributesMap(AffymetrixProbesetDAO
                .Attribute.class), true, clonedAttrs))
        // generate FROM
        .append(generateFromClause(clonedRawDataFilter));
        // FITER ON LIBRARY IDS
        boolean filterFound = false;
        if (!clonedProbesetIds.isEmpty()) {
            sb.append(TABLE_NAME).append(".")
            .append(AffymetrixProbesetDAO.Attribute.ID.getTOFieldName())
            .append(" IN (")
            .append(BgeePreparedStatement.generateParameterizedQueryString(clonedProbesetIds.size()))
            .append(")");
            filterFound = true;
        }
        // FILTER ON CHIP IDs
        if (!clonedBgeeChipIds.isEmpty()) {
            if(filterFound) {
                sb.append(" AND ");
            }
            sb.append(TABLE_NAME).append(".")
            .append(AffymetrixProbesetDAO.Attribute.BGEE_AFFYMETRIX_CHIP_ID.getTOFieldName())
            .append(" IN (")
            .append(BgeePreparedStatement.generateParameterizedQueryString(clonedBgeeChipIds.size()))
            .append(")");
            filterFound = true;
        }
        // FITER ON SPECIES IDS
        if (!clonedRawDataFilter.getSpeciesIds().isEmpty()) {
            if(filterFound) {
                sb.append(" AND ");
            }
            sb.append(CONDITION_TABLE_NAME).append(".")
            .append(RawDataConditionDAO.Attribute.SPECIES_ID.getTOFieldName()).append(" IN (")
            .append(BgeePreparedStatement.generateParameterizedQueryString(
                    clonedRawDataFilter.getSpeciesIds().size()))
            .append(")");
            filterFound = true;
        }
        // FITER ON GENE IDS
        if (!clonedRawDataFilter.getGeneIds().isEmpty()) {
            if(filterFound) {
                sb.append(" AND ");
            }
            sb.append(TABLE_NAME).append(".")
            .append(AffymetrixProbesetDAO.Attribute.BGEE_GENE_ID.getTOFieldName()).append(" IN (")
            .append(BgeePreparedStatement.generateParameterizedQueryString(
                    clonedRawDataFilter.getGeneIds().size()))
            .append(")");
            filterFound = true;
        }
        // FILTER ON RAW CONDITIONS
        if (!clonedRawDataFilter.getConditionFilters().isEmpty()) {
            if(filterFound) {
                sb.append(" AND ");
            }
            sb.append(clonedRawDataFilter.getConditionFilters().stream()
                    .map(cf -> generateOneConditionFilter(cf))
                    .collect(Collectors.joining(" OR ", "(", ")")));
        }

        //add values to parameterized queries
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection()
                    .prepareStatement(sb.toString());
            int paramIndex = 1;
            if (!clonedProbesetIds.isEmpty()) {
                stmt.setStrings(paramIndex, clonedProbesetIds, true);
                paramIndex += clonedProbesetIds.size();
            }
            if (!clonedBgeeChipIds.isEmpty()) {
                stmt.setStrings(paramIndex, clonedBgeeChipIds, true);
                paramIndex += clonedBgeeChipIds.size();
            }
            if (!clonedRawDataFilter.getSpeciesIds().isEmpty()) {
                stmt.setIntegers(paramIndex, clonedRawDataFilter.getSpeciesIds(), true);
                paramIndex += clonedRawDataFilter.getSpeciesIds().size();
            }
            if (!clonedRawDataFilter.getGeneIds().isEmpty()) {
                stmt.setIntegers(paramIndex, clonedRawDataFilter.getGeneIds(), true);
                paramIndex += clonedRawDataFilter.getSpeciesIds().size();
            }
            configureRawDataConditionFiltersStmt(stmt, clonedRawDataFilter.getConditionFilters(),
                    paramIndex);
            return log.traceExit(new MySQLAffymetrixProbesetTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    private String generateFromClause(DAORawDataFilter rawDataFilter) {
        log.traceEntry("{}", rawDataFilter);
        StringBuilder sb = new StringBuilder();
        sb.append(" FROM " + TABLE_NAME + " ");
        if(!rawDataFilter.getSpeciesIds().isEmpty()
                || !rawDataFilter.getConditionFilters().isEmpty() ) {
            sb.append("INNER JOIN " + CHIP_TABLE_NAME + " ON ")
            .append(TABLE_NAME + "." + AffymetrixProbesetDAO.Attribute.BGEE_AFFYMETRIX_CHIP_ID)
            .append(" = " + CHIP_TABLE_NAME + "." + AffymetrixChipDAO.Attribute.AFFYMETRIX_CHIP_ID)
            .append(" INNER JOIN " + CONDITION_TABLE_NAME + " ON ")
            .append(CONDITION_TABLE_NAME + "." + RawDataConditionDAO.Attribute.ID)
            .append(" = " + CHIP_TABLE_NAME + "." + AffymetrixChipDAO.Attribute.CONDITION_ID);
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
                }  else {
                    log.throwing(new UnrecognizedColumnException(column.getValue()));
                }
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
