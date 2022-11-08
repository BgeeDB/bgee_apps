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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCallSourceDAO.CallSourceDataTO.ExclusionReason;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixProbesetDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.expressiondata.rawdata.MySQLRawDataConditionDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.MySQLRawDataDAO;
import org.bgee.model.dao.mysql.gene.MySQLGeneDAO;


public class MySQLAffymetrixProbesetDAO extends MySQLRawDataDAO<AffymetrixProbesetDAO.Attribute>
                                        implements AffymetrixProbesetDAO {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(MySQLAffymetrixProbesetDAO.class.getName());
    public final static String TABLE_NAME = "affymetrixProbeset";

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
            Integer offset, Integer limit, Collection<AffymetrixProbesetDAO.Attribute> attrs)
            throws DAOException {
        log.traceEntry("{}, {}, {}, {}", rawDataFilters, offset, limit, attrs);
        checkOffsetAndLimit(offset, limit);
        // force to have a list in order to keep order of elements. It is mandatory to be able
        // to first generate a parameterised query and then add values.
        final List<DAORawDataFilter> orderedRawDataFilter = 
                Collections.unmodifiableList(rawDataFilters == null? new ArrayList<>():
                    new ArrayList<>(rawDataFilters));
        final Set<AffymetrixProbesetDAO.Attribute> clonedAttrs = Collections
                .unmodifiableSet(attrs == null || attrs.isEmpty()?
                EnumSet.allOf(AffymetrixProbesetDAO.Attribute.class): EnumSet.copyOf(attrs));
        //detect join to use
        boolean needJoinChip = orderedRawDataFilter.stream().anyMatch(e -> !e.getExperimentIds().isEmpty() 
                ||  !e.getRawDataCondIds().isEmpty() || !e.getAssayIds().isEmpty());            
        boolean needJoinCond = orderedRawDataFilter.stream().anyMatch(e -> e.getSpeciesId() != null 
                && needJoinChip) ? true : false;
        final boolean needJoinGene = orderedRawDataFilter.stream().anyMatch(e -> e.getSpeciesId() != null
                && !needJoinChip) ? true : false;
        // generate SELECT
        StringBuilder sb = new StringBuilder();
        sb.append(generateSelectClause(TABLE_NAME, getColToAttributesMap(AffymetrixProbesetDAO
                .Attribute.class), true, clonedAttrs))
//        // generate FROM
        .append(generateFromClauseAffymetrix(TABLE_NAME, false, needJoinChip, false, needJoinCond,
                needJoinGene));
        // generate WHERE
        if (!orderedRawDataFilter.isEmpty()) {
            sb.append(" WHERE ")
            // if needJoinGene is true it means the join for speciesId is done to the gene table.
            // Otherwise it is done on the condition table
            .append(generateWhereClause(orderedRawDataFilter, MySQLAffymetrixChipDAO.TABLE_NAME,
                    needJoinGene ? MySQLGeneDAO.TABLE_NAME: MySQLRawDataConditionDAO.TABLE_NAME));
        }

        // generate ORDER BY
        sb.append(" ORDER BY")
        .append(" " + TABLE_NAME + "." + AffymetrixProbesetDAO.Attribute.BGEE_AFFYMETRIX_CHIP_ID
                .getTOFieldName())
        .append(", " + TABLE_NAME + "." + AffymetrixProbesetDAO.Attribute.ID.getTOFieldName());

        //generate offset and limit
        if (limit != null) {
            sb.append(offset == null ? " LIMIT ?": " LIMIT ?, ?");
        }
        try {
            BgeePreparedStatement stmt = this.parameterizeQuery(sb.toString(), orderedRawDataFilter,
                    offset, limit);
            return log.traceExit(new MySQLAffymetrixProbesetTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
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
