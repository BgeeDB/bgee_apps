package org.bgee.model.dao.mysql.expressiondata.rawdata.est;

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
import org.bgee.model.dao.api.expressiondata.rawdata.est.ESTDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.est.ESTLibraryDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
import org.bgee.model.dao.mysql.expressiondata.rawdata.MySQLRawDataDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.RawDataFiltersToDatabaseMapping;

public class MySQLESTDAO extends MySQLRawDataDAO<ESTDAO.Attribute> implements ESTDAO {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(MySQLESTDAO.class.getName());
    public final static String TABLE_NAME = "expressedSequenceTag";

    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     * @param manager                   the {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException If {@code manager} is {@code null}.
     */
    public MySQLESTDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public ESTTOResultSet getESTs(Collection<DAORawDataFilter> rawDataFilters, Long offset, Integer limit,
            Collection<ESTDAO.Attribute> attributes) throws DAOException {
        log.traceEntry("{}, {}, {}, {}", rawDataFilters, offset, limit, attributes);
        checkOffsetAndLimit(offset, limit);

        //It is very ugly, but for performance reasons, we use two queries:
        //one for identifying the internal assay IDs, the second one to retrieve the calls.
        //It is because the optimizer completely fail at generating a correct query plan,
        //we really tried hard to fix this
        //(see https://dba.stackexchange.com/questions/320207/optimization-with-subquery-not-working-as-expected).
        //This logic is managed in the method processFilterForCallTableAssayIds,
        //which returns the appropriate DAOProcessedRawDataFilter to be used in this method.
        final MySQLESTLibraryDAO assayDAO = new MySQLESTLibraryDAO(this.getManager());
        DAOProcessedRawDataFilter<String> processedFilters = this.processFilterForCallTableAssayIds(
                new DAOProcessedRawDataFilter<String>(rawDataFilters),
                (s) -> assayDAO.getESTLibraries(s, null, null,
                               Set.of(ESTLibraryDAO.Attribute.ID))
                       .stream()
                       .map(to -> to.getId())
                       .collect(Collectors.toSet()),
                String.class, DAODataType.EST, null);
        if (processedFilters == null) {
            try {
                return log.traceExit(new MySQLESTTOResultSet(
                        this.getManager().getConnection().prepareStatement(
                            "SELECT NULL FROM " + TABLE_NAME + " WHERE FALSE")));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }

        final Set<ESTDAO.Attribute> clonedAttrs = Collections
                .unmodifiableSet(attributes == null || attributes.isEmpty()?
                EnumSet.allOf(ESTDAO.Attribute.class): EnumSet.copyOf(attributes));

        StringBuilder sb = new StringBuilder();

        // generate SELECT
        sb.append(generateSelectClauseRawDataFilters(processedFilters, TABLE_NAME,
                getColToAttributesMap(ESTDAO.Attribute.class), true, clonedAttrs));

        // generate FROM
        RawDataFiltersToDatabaseMapping filtersToDatabaseMapping = generateFromClauseRawData(sb,
                processedFilters, null, Set.of(TABLE_NAME), DAODataType.EST);

        // generate WHERE CLAUSE
        // usedInPropagatedCalls is not used to generate EST queries. If usedInPropagatedCalls is the only
        // rawDataFilter not empty the query will not contain any where clause. That's why we check
        // that any rawDataFilter variable except usedInPropagatedCalls is not empty.
        boolean requireWhereClause = rawDataFilters.stream()
                .allMatch(item -> item.usedInPropagatedCallsIsTheOnlyPotentialNotBlank()) ? false : true;
        if(requireWhereClause) {
            sb.append(" WHERE ").append(generateWhereClauseRawDataFilter(processedFilters,
                    filtersToDatabaseMapping));
        }
        // generate ORDER BY
        sb.append(" ORDER BY")
        .append(" " + TABLE_NAME + "." + ESTDAO.Attribute.EST_LIBRARY_ID
                .getTOFieldName())
        .append(", " + TABLE_NAME + "." + ESTDAO.Attribute.EST_ID.getTOFieldName());

        //generate offset and limit
        if (limit != null) {
            sb.append(offset == null ? " LIMIT ?": " LIMIT ?, ?");
        }
        try {
            BgeePreparedStatement stmt = this.parameterizeQuery(sb.toString(), processedFilters,
                    DAODataType.EST, offset, limit);
            return log.traceExit(new MySQLESTTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    class MySQLESTTOResultSet extends MySQLDAOResultSet<ESTTO> 
    implements ESTTOResultSet{

        /**
         * @param statement The {@code BgeePreparedStatement}
         */
        private MySQLESTTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected ESTDAO.ESTTO getNewTO() throws DAOException {
            log.traceEntry();
            try {
                final ResultSet currentResultSet = this.getCurrentResultSet();
                BigDecimal pValue = null;
                Integer bgeeGeneId = null;
                Long expressionid = null;
                String estId1 = null, estId2 = null, estLibraryId = null, uniGeneClusterId = null,
                        estData = null;

                for (Entry<Integer, String> column : this.getColumnLabels().entrySet()) {
                    if (column.getValue().equals(ESTDAO.Attribute.EST_ID.getTOFieldName())) {
                        estId1 = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(ESTDAO.Attribute.EST_ID2
                            .getTOFieldName())) {
                        estId2 = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(ESTDAO.Attribute.EST_LIBRARY_ID
                            .getTOFieldName())) {
                        estLibraryId = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(ESTDAO.Attribute.BGEE_GENE_ID
                            .getTOFieldName())) {
                        bgeeGeneId = currentResultSet.getInt(column.getKey());
                    } else if(column.getValue().equals(ESTDAO.Attribute.UNIGENE_CLUSTER_ID
                            .getTOFieldName())) {
                        uniGeneClusterId = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(ESTDAO.Attribute.EXPRESSION_ID
                            .getTOFieldName())) {
                        expressionid = currentResultSet.getLong(column.getKey());
                    } else if(column.getValue().equals(ESTDAO.Attribute.PVALUE
                            .getTOFieldName())) {
                        pValue = currentResultSet.getBigDecimal(column.getKey());
                    } else if(column.getValue().equals(ESTDAO.Attribute.EST_DATA
                            .getTOFieldName())) {
                        estData = currentResultSet.getString(column.getKey());
                    } else {
                        log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }
                }
                return log.traceExit(new ESTTO(estId1, estId2, estLibraryId, uniGeneClusterId,
                        bgeeGeneId, DataState.convertToDataState(estData), pValue, expressionid));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }
}
