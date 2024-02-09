package org.bgee.model.dao.mysql.expressiondata.rawdata.insitu;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.rawdata.DAOProcessedRawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituExperimentDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
import org.bgee.model.dao.mysql.expressiondata.rawdata.MySQLRawDataDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.RawDataFiltersToDatabaseMapping;

public class MySQLInSituExperimentDAO extends MySQLRawDataDAO<InSituExperimentDAO.Attribute> 
implements InSituExperimentDAO{

    private final static Logger log = LogManager.getLogger(MySQLInSituExperimentDAO.class.getName());
    public final static String TABLE_NAME = "inSituExperiment";

    public MySQLInSituExperimentDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public InSituExperimentTOResultSet getInSituExperiments(Collection<DAORawDataFilter> rawDataFilters,
            Long offset, Integer limit, Collection<InSituExperimentDAO.Attribute> attrs)
            throws DAOException {
        log.traceEntry("{}, {}, {}, {}", rawDataFilters, offset, limit, attrs);
        checkOffsetAndLimit(offset, limit);

        final DAOProcessedRawDataFilter<String> processedFilters =
                new DAOProcessedRawDataFilter<>(rawDataFilters);
        final Set<InSituExperimentDAO.Attribute> clonedAttrs = Collections
                .unmodifiableSet(attrs == null || attrs.isEmpty()?
                EnumSet.allOf(InSituExperimentDAO.Attribute.class): EnumSet.copyOf(attrs));

        StringBuilder sb = new StringBuilder();

        // generate SELECT
        sb.append(generateSelectClauseRawDataFilters(processedFilters, TABLE_NAME,
                getColToAttributesMap(InSituExperimentDAO.Attribute.class), true, clonedAttrs));

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
        .append(" " + TABLE_NAME + "." + InSituExperimentDAO.Attribute.ID
                .getTOFieldName());

        //generate offset and limit
        if (limit != null) {
            sb.append(offset == null ? " LIMIT ?": " LIMIT ?, ?");
        }
        try {
            BgeePreparedStatement stmt = this.parameterizeQuery(sb.toString(), processedFilters,
                    DAODataType.IN_SITU, offset, limit);
            return log.traceExit(new MySQLInSituExperimentTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    class MySQLInSituExperimentTOResultSet extends MySQLDAOResultSet<InSituExperimentTO> 
    implements InSituExperimentTOResultSet{

        /**
         * @param statement The {@code BgeePreparedStatement}
         */
        private MySQLInSituExperimentTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected InSituExperimentDAO.InSituExperimentTO getNewTO() throws DAOException {
            log.traceEntry();
            try {
                final ResultSet currentResultSet = this.getCurrentResultSet();
                Integer dataSourceId = null;
                String id = null, name = null, description = null;

                for (Entry<Integer, String> column : this.getColumnLabels().entrySet()) {
                    if (column.getValue().equals(InSituExperimentDAO.Attribute.ID.getTOFieldName())) {
                        id = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(InSituExperimentDAO.Attribute.NAME
                            .getTOFieldName())) {
                        name = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(InSituExperimentDAO.Attribute.DESCRIPTION
                            .getTOFieldName())) {
                        description = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(InSituExperimentDAO.Attribute.DATA_SOURCE_ID
                            .getTOFieldName())) {
                        dataSourceId = currentResultSet.getInt(column.getKey());
                    } else {
                        log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }
                }
                return log.traceExit(new InSituExperimentTO(id, name, description, dataSourceId));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }
}
