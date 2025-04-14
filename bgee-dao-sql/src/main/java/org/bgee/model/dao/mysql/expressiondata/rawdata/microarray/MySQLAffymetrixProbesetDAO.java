package org.bgee.model.dao.mysql.expressiondata.rawdata.microarray;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.call.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.rawdata.DAOProcessedRawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCallSourceDAO.CallSourceDataTO.ExclusionReason;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixProbesetDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
import org.bgee.model.dao.mysql.expressiondata.rawdata.MySQLRawDataDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.RawDataFiltersToDatabaseMapping;


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
    public AffymetrixProbesetTOResultSet getAffymetrixProbesets(
            Collection<DAORawDataFilter> rawDataFilters, Long offset, Integer limit,
            Collection<AffymetrixProbesetDAO.Attribute> attrs) throws DAOException {
        log.traceEntry("{}, {}, {}, {}", rawDataFilters, offset, limit, attrs);
        checkOffsetAndLimit(offset, limit);

        //It is very ugly, but for performance reasons, we use two queries:
        //one for identifying the internal assay IDs, the second one to retrieve the calls.
        //It is because the optimizer completely fail at generating a correct query plan,
        //we really tried hard to fix this
        //(see https://dba.stackexchange.com/questions/320207/optimization-with-subquery-not-working-as-expected).
        //This logic is managed in the method processFilterForCallTableAssayIds,
        //which returns the appropriate DAOProcessedRawDataFilter to be used in this method.
        final MySQLAffymetrixChipDAO assayDAO = new MySQLAffymetrixChipDAO(this.getManager());
        DAOProcessedRawDataFilter<Integer> processedFilters = this.processFilterForCallTableAssayIds(
                new DAOProcessedRawDataFilter<Integer>(rawDataFilters),
                (s) -> assayDAO.getAffymetrixChips(s, null, null,
                               Set.of(AffymetrixChipDAO.Attribute.BGEE_AFFYMETRIX_CHIP_ID))
                       .stream()
                       .map(to -> to.getId())
                    .  collect(Collectors.toSet()),
                Integer.class, DAODataType.AFFYMETRIX, null);
        if (processedFilters == null) {
            try {
                return log.traceExit(new MySQLAffymetrixProbesetTOResultSet(
                        this.getManager().getConnection().prepareStatement(
                            "SELECT NULL FROM " + TABLE_NAME + " WHERE FALSE")));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }

        final Set<AffymetrixProbesetDAO.Attribute> clonedAttrs = Collections
                .unmodifiableSet(attrs == null || attrs.isEmpty()?
                EnumSet.allOf(AffymetrixProbesetDAO.Attribute.class): EnumSet.copyOf(attrs));

        StringBuilder sb = new StringBuilder();

        // generate SELECT
        sb.append(generateSelectClauseRawDataFilters(processedFilters, TABLE_NAME,
                getColToAttributesMap(AffymetrixProbesetDAO.Attribute.class), false, clonedAttrs));

        // generate FROM
        RawDataFiltersToDatabaseMapping filtersToDatabaseMapping = generateFromClauseRawData(sb,
                processedFilters, null, Set.of(TABLE_NAME), DAODataType.AFFYMETRIX);

        // generate WHERE
        // usedInPropagatedCalls is not used to generate affymetrix queries. If usedInPropagatedCalls is the only
        // rawDataFilter not empty the query will not contain any where clause. That's why we check
        // that any rawDataFilter variable except usedInPropagatedCalls is not empty.
        boolean requireWhereClause = rawDataFilters.stream()
                .allMatch(item -> item.usedInPropagatedCallsIsTheOnlyPotentialNotBlank()) ? false : true;
        if(requireWhereClause) {
            sb.append(" WHERE ")
            .append(generateWhereClauseRawDataFilter(processedFilters, filtersToDatabaseMapping));
        }

        // generate ORDER BY
        sb.append(" ORDER BY")
        .append(" ").append(TABLE_NAME).append(".")
            .append(AffymetrixProbesetDAO.Attribute.BGEE_AFFYMETRIX_CHIP_ID.getTOFieldName())
        .append(", ").append(TABLE_NAME).append(".")
            .append(AffymetrixProbesetDAO.Attribute.ID.getTOFieldName())
        .append(", ").append(TABLE_NAME).append(".")
            .append(AffymetrixProbesetDAO.Attribute.BGEE_GENE_ID.getTOFieldName());

        //generate offset and limit
        if (limit != null) {
            sb.append(offset == null ? " LIMIT ?": " LIMIT ?, ?");
        }
        try {
            BgeePreparedStatement stmt = this.parameterizeQuery(sb.toString(), processedFilters,
                    DAODataType.AFFYMETRIX, offset, limit);
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
                }  else if (column.getValue().equals(AffymetrixProbesetDAO.Attribute
                        .RAW_DETECTION_FLAG.getTOFieldName())) {
                    //TODO the database schema still contain the column rawDetectionFlag that is not
                    // used and should be removed. Remove this condition when the schema is updated
                }
                else {
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
