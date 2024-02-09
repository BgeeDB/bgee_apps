package org.bgee.model.dao.mysql.expressiondata.rawdata.insitu;

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
import org.bgee.model.dao.api.expressiondata.call.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.rawdata.DAOProcessedRawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCallSourceDAO.CallSourceDataTO.ExclusionReason;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituEvidenceDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituSpotDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
import org.bgee.model.dao.mysql.expressiondata.rawdata.MySQLRawDataDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.RawDataFiltersToDatabaseMapping;


public class MySQLInSituSpotDAO extends MySQLRawDataDAO<InSituSpotDAO.Attribute>
        implements InSituSpotDAO {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(MySQLInSituSpotDAO.class.getName());
    public final static String TABLE_NAME = "inSituSpot";

    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     * 
     * @param manager                       The {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException     If {@code manager} is {@code null}.
     */
    public MySQLInSituSpotDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public InSituSpotTOResultSet getInSituSpots(Collection<DAORawDataFilter> rawDataFilters,
            Long offset, Integer limit, Collection<InSituSpotDAO.Attribute> attrs)
            throws DAOException {
        log.traceEntry("{}, {}, {}, {}", rawDataFilters, offset, limit, attrs);
        checkOffsetAndLimit(offset, limit);

        //It is very ugly, but for performance reasons, we use three queries:
        //one for identifying the internal assay IDs, one for the retrieving conditionIds,
        //the third one to retrieve the calls.
        //It is because the optimizer completely fail at generating a correct query plan,
        //we really tried hard to fix this
        //(see https://dba.stackexchange.com/questions/320207/optimization-with-subquery-not-working-as-expected).
        //This logic is managed in the method processFilterForCallTableAssayIds,
        //which returns the appropriate DAOProcessedRawDataFilter to be used in this method.
        final MySQLInSituEvidenceDAO assayDAO = new MySQLInSituEvidenceDAO(this.getManager());
        DAOProcessedRawDataFilter<String> processedFilters = this.processFilterForCallTableAssayIds(
                new DAOProcessedRawDataFilter<String>(rawDataFilters),
                (s) -> assayDAO.getInSituEvidences(s, null, null,
                               Set.of(InSituEvidenceDAO.Attribute.IN_SITU_EVIDENCE_ID))
                       .stream()
                       .map(to -> to.getId())
                    .  collect(Collectors.toSet()),
                String.class, DAODataType.IN_SITU, null, null);
        if (processedFilters == null) {
            try {
                return log.traceExit(new MySQLInSituSpotTOResultSet(
                        this.getManager().getConnection().prepareStatement(
                            "SELECT NULL FROM " + TABLE_NAME + " WHERE FALSE")));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }

        //Finally, we get back to the "regular" code
        final Set<InSituSpotDAO.Attribute> clonedAttrs = Collections
                .unmodifiableSet(attrs == null || attrs.isEmpty()?
                EnumSet.allOf(InSituSpotDAO.Attribute.class): EnumSet.copyOf(attrs));

        StringBuilder sb = new StringBuilder();

        // generate SELECT
        sb.append(generateSelectClauseRawDataFilters(processedFilters, TABLE_NAME,
                getColToAttributesMap(InSituSpotDAO.Attribute.class), true, clonedAttrs));

        // generate FROM
        RawDataFiltersToDatabaseMapping filtersToDatabaseMapping = generateFromClauseRawData(sb,
                processedFilters, null, null, Set.of(TABLE_NAME), DAODataType.IN_SITU);

        // generate WHERE CLAUSE
        if (!processedFilters.getRawDataFilters().isEmpty()) {
            sb.append(" WHERE ").append(generateWhereClauseRawDataFilter(processedFilters,
                    filtersToDatabaseMapping));
        }

        // generate ORDER BY
        sb.append(" ORDER BY")
        .append(" " + TABLE_NAME + "." + InSituSpotDAO.Attribute.IN_SITU_EVIDENCE_ID
                .getTOFieldName())
        .append(", " + TABLE_NAME + "." + InSituSpotDAO.Attribute.CONDITION_ID
                .getTOFieldName());
        if (clonedAttrs.contains(InSituSpotDAO.Attribute.ID)) {
            sb.append(", " + TABLE_NAME + "." + InSituSpotDAO.Attribute.ID
                .getTOFieldName());
        }

        //generate offset and limit
        if (limit != null) {
            sb.append(offset == null ? " LIMIT ?": " LIMIT ?, ?");
        }
        try {
            BgeePreparedStatement stmt = this.parameterizeQuery(sb.toString(), processedFilters,
                    DAODataType.IN_SITU, offset, limit);
            return log.traceExit(new MySQLInSituSpotTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    class MySQLInSituSpotTOResultSet extends MySQLDAOResultSet<InSituSpotTO>
    implements InSituSpotTOResultSet{
        /**
         * @param statement The {@code BgeePreparedStatement}
         */
        private MySQLInSituSpotTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected InSituSpotDAO.InSituSpotTO getNewTO() throws DAOException {
            log.traceEntry();
            try {
                final ResultSet currentResultSet = this.getCurrentResultSet();
                String inSituSpotId = null, inSituEvidenceId = null, inSituExpressionPatternId = null;
                String inSituData = null, reasonForExclusion = null;
                Integer conditionId = null, bgeeGeneId = null;
                Long expressionId = null;
                BigDecimal pValue = null;

                for (Entry<Integer, String> column : this.getColumnLabels().entrySet()) {
                    if (column.getValue().equals(InSituSpotDAO.Attribute.ID.getTOFieldName())) {
                        inSituSpotId = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(InSituSpotDAO.Attribute.IN_SITU_EVIDENCE_ID
                            .getTOFieldName())) {
                        inSituEvidenceId = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(InSituSpotDAO.Attribute
                            .IN_SITU_EXPRESSION_PATTERN_ID.getTOFieldName())) {
                        inSituExpressionPatternId = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(InSituSpotDAO.Attribute.CONDITION_ID
                            .getTOFieldName())) {
                        conditionId = currentResultSet.getInt(column.getKey());
                    } else if(column.getValue().equals(InSituSpotDAO.Attribute.BGEE_GENE_ID
                            .getTOFieldName())) {
                        bgeeGeneId = currentResultSet.getInt(column.getKey());
                    } else if(column.getValue().equals(InSituSpotDAO.Attribute.EXPRESSION_ID
                            .getTOFieldName())) {
                        expressionId = currentResultSet.getLong(column.getKey());
                    } else if(column.getValue().equals(InSituSpotDAO.Attribute.IN_SITU_DATA
                            .getTOFieldName())) {
                        inSituData = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(InSituSpotDAO.Attribute.PVALUE
                            .getTOFieldName())) {
                        pValue = currentResultSet.getBigDecimal(column.getKey());
                    } else if(column.getValue().equals(InSituSpotDAO.Attribute.REASON_FOR_EXCLUSION
                            .getTOFieldName())) {
                        reasonForExclusion = currentResultSet.getString(column.getKey());
                    } else {
                        log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }
                }
                return log.traceExit(new InSituSpotTO(inSituSpotId, inSituEvidenceId,
                        inSituExpressionPatternId, conditionId, bgeeGeneId, expressionId,
                        pValue, DataState.convertToDataState(inSituData),
                        ExclusionReason.convertToExclusionReason(reasonForExclusion)));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }
}
