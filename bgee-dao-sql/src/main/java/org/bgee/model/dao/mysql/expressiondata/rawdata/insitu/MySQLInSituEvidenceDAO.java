package org.bgee.model.dao.mysql.expressiondata.rawdata.insitu;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.rawdata.DAOProcessedRawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituEvidenceDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
import org.bgee.model.dao.mysql.expressiondata.rawdata.MySQLRawDataDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.RawDataFiltersToDatabaseMapping;

public class MySQLInSituEvidenceDAO extends MySQLRawDataDAO<InSituEvidenceDAO.Attribute> 
implements InSituEvidenceDAO{

    private final static Logger log = LogManager.getLogger(MySQLInSituEvidenceDAO.class.getName());
    public final static String TABLE_NAME = "inSituEvidence";

    public MySQLInSituEvidenceDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public InSituEvidenceTOResultSet getInSituEvidenceFromIds(Collection<String> evidenceIds,
            Collection<InSituEvidenceDAO.Attribute> attrs) {
        log.traceEntry("{}, {}", evidenceIds, attrs);
        if (evidenceIds == null || evidenceIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("need to provide at least one"
                    + "evidenceId"));
        }
        final Set<InSituEvidenceDAO.Attribute> clonedAttrs = Collections
                .unmodifiableSet(attrs == null || attrs.isEmpty()?
                EnumSet.allOf(InSituEvidenceDAO.Attribute.class): EnumSet.copyOf(attrs));
        // sort and use a linked hash set to be able to use cache
        final Set<String> clonedEvidenceIds = Collections.unmodifiableSet(evidenceIds.stream()
                .filter(id -> id != null).sorted()
                .collect(Collectors.<String,LinkedHashSet<String>>toCollection(LinkedHashSet::new)));
     // generate SELECT
        StringBuilder sb = new StringBuilder();
        sb.append(generateSelectClause(TABLE_NAME, getColToAttributesMap(InSituEvidenceDAO
                .Attribute.class), true, clonedAttrs))
        .append(" FROM ").append(TABLE_NAME).append(" WHERE ")
        .append(InSituEvidenceDAO.Attribute.IN_SITU_EVIDENCE_ID.getTOFieldName())
        .append(" IN (")
        .append(BgeePreparedStatement.generateParameterizedQueryString(clonedEvidenceIds.size()))
        .append(")");
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection()
                    .prepareStatement(sb.toString());
            stmt.setStrings(1, clonedEvidenceIds, true);
            return log.traceExit(new MySQLInSituEvidenceTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public InSituEvidenceTOResultSet getInSituEvidences(Collection<DAORawDataFilter> rawDataFilters,
            Long offset, Integer limit, Collection<InSituEvidenceDAO.Attribute> attrs)
            throws DAOException {
        log.traceEntry("{}, {}, {}, {}", rawDataFilters, offset, limit, attrs);
        checkOffsetAndLimit(offset, limit);

        final DAOProcessedRawDataFilter<String> processedFilters =
                new DAOProcessedRawDataFilter<>(rawDataFilters);
        final Set<InSituEvidenceDAO.Attribute> clonedAttrs = Collections
                .unmodifiableSet(attrs == null || attrs.isEmpty()?
                EnumSet.allOf(InSituEvidenceDAO.Attribute.class): EnumSet.copyOf(attrs));

        StringBuilder sb = new StringBuilder();

        // generate SELECT
        sb.append(generateSelectClauseRawDataFilters(processedFilters, TABLE_NAME,
                getColToAttributesMap(InSituEvidenceDAO.Attribute.class), true, clonedAttrs));

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
        .append(" " + TABLE_NAME + "." + InSituEvidenceDAO.Attribute.EXPERIMENT_ID
                .getTOFieldName())
        .append(", " + TABLE_NAME + "." + InSituEvidenceDAO.Attribute.IN_SITU_EVIDENCE_ID
                .getTOFieldName());

        //generate offset and limit
        if (limit != null) {
            sb.append(offset == null ? " LIMIT ?": " LIMIT ?, ?");
        }
        try {
            BgeePreparedStatement stmt = this.parameterizeQuery(sb.toString(), processedFilters,
                    DAODataType.IN_SITU, offset, limit);
            return log.traceExit(new MySQLInSituEvidenceTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    class MySQLInSituEvidenceTOResultSet extends MySQLDAOResultSet<InSituEvidenceTO> 
    implements InSituEvidenceTOResultSet{

        /**
         * @param statement The {@code BgeePreparedStatement}
         */
        private MySQLInSituEvidenceTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected InSituEvidenceDAO.InSituEvidenceTO getNewTO() throws DAOException {
            log.traceEntry();
            try {
                final ResultSet currentResultSet = this.getCurrentResultSet();
                Boolean distinguishable = null;
                String id = null, experimentId = null, evidenceUrlPath= null;

                for (Entry<Integer, String> column : this.getColumnLabels().entrySet()) {
                    if (column.getValue().equals(InSituEvidenceDAO.Attribute.IN_SITU_EVIDENCE_ID.getTOFieldName())) {
                        id = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(InSituEvidenceDAO.Attribute.EXPERIMENT_ID
                            .getTOFieldName())) {
                        experimentId = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(InSituEvidenceDAO.Attribute.EVIDENCE_DISTINGUISHABLE
                            .getTOFieldName())) {
                        distinguishable = currentResultSet.getBoolean(column.getKey());
                    } else if(column.getValue().equals(InSituEvidenceDAO.Attribute.EVIDENCE_URL_PART
                            .getTOFieldName())) {
                        evidenceUrlPath = currentResultSet.getString(column.getKey());
                    } else {
                        log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }
                }
                return log.traceExit(new InSituEvidenceTO(id, experimentId, distinguishable,
                        evidenceUrlPath));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }
}
