package org.bgee.model.dao.mysql.expressiondata.rawdata.est;

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
import org.bgee.model.dao.api.expressiondata.rawdata.est.ESTLibraryDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
import org.bgee.model.dao.mysql.expressiondata.rawdata.MySQLRawDataDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.RawDataFiltersToDatabaseMapping;

public class MySQLESTLibraryDAO extends MySQLRawDataDAO<ESTLibraryDAO.Attribute>
        implements ESTLibraryDAO{

    private final static Logger log = LogManager.getLogger(MySQLESTLibraryDAO.class.getName());
    public final static String TABLE_NAME = "estLibrary";

    public MySQLESTLibraryDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public ESTLibraryTOResultSet getESTLibraries(Collection<DAORawDataFilter> rawDataFilters,
            Integer offset, Integer limit, Collection<ESTLibraryDAO.Attribute> attrs)
            throws DAOException {
        log.traceEntry("{}, {}, {}, {}", rawDataFilters, offset, limit, attrs);
        checkOffsetAndLimit(offset, limit);

        final DAOProcessedRawDataFilter<String> processedFilters =
                new DAOProcessedRawDataFilter<>(rawDataFilters);
        final Set<ESTLibraryDAO.Attribute> clonedAttrs = Collections
                .unmodifiableSet(attrs == null || attrs.isEmpty()?
                EnumSet.allOf(ESTLibraryDAO.Attribute.class): EnumSet.copyOf(attrs));

        StringBuilder sb = new StringBuilder();

        // generate SELECT
        sb.append(generateSelectClauseRawDataFilters(processedFilters, TABLE_NAME,
                getColToAttributesMap(ESTLibraryDAO.Attribute.class), true, clonedAttrs));

        // generate FROM
        RawDataFiltersToDatabaseMapping filtersToDatabaseMapping = generateFromClauseRawData(sb,
                processedFilters, null, Set.of(TABLE_NAME), DAODataType.EST);

        // generate WHERE CLAUSE
        if (!processedFilters.getRawDataFilters().isEmpty() &&
                !processedFilters.getRawDataFilters().stream().allMatch(f ->
                f.getAssayIds().isEmpty() && f.getExprOrAssayIds().isEmpty() &&
                f.getGeneIds().isEmpty() && f.getRawDataCondIds().isEmpty() &&
                f.getSpeciesId() == null)) {
            sb.append(" WHERE ").append(generateWhereClauseRawDataFilter(processedFilters,
                    filtersToDatabaseMapping, DAODataType.EST));
        }

        // generate ORDER BY
        sb.append(" ORDER BY")
        .append(" " + TABLE_NAME + "." + ESTLibraryDAO.Attribute.ID
                .getTOFieldName());

        //generate offset and limit
        if (limit != null) {
            sb.append(offset == null ? " LIMIT ?": " LIMIT ?, ?");
        }
        try {
            BgeePreparedStatement stmt = this.parameterizeQuery(sb.toString(), processedFilters,
                    DAODataType.EST, offset, limit);
            return log.traceExit(new MySQLESTLibraryTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    class MySQLESTLibraryTOResultSet extends MySQLDAOResultSet<ESTLibraryTO> 
        implements ESTLibraryTOResultSet{

        /**
         * @param statement The {@code BgeePreparedStatement}
         */
        private MySQLESTLibraryTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }
        
        @Override
        protected ESTLibraryDAO.ESTLibraryTO getNewTO() throws DAOException {
            log.traceEntry();
            try {
                final ResultSet currentResultSet = this.getCurrentResultSet();
                Integer dataSourceid = null, conditionId = null;
                String estLibraryId = null, estLibraryName = null, estLibraryDescription = null;

                for (Entry<Integer, String> column : this.getColumnLabels().entrySet()) {
                    if (column.getValue().equals(ESTLibraryDAO.Attribute.ID.getTOFieldName())) {
                        estLibraryId = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(ESTLibraryDAO.Attribute.NAME
                            .getTOFieldName())) {
                        estLibraryName = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(ESTLibraryDAO.Attribute.DESCRIPTION
                            .getTOFieldName())) {
                        estLibraryDescription = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(ESTLibraryDAO.Attribute.DATA_SOURCE_ID
                            .getTOFieldName())) {
                        dataSourceid = currentResultSet.getInt(column.getKey());
                    }  else if(column.getValue().equals(ESTLibraryDAO.Attribute.CONDITION_ID
                            .getTOFieldName())) {
                        conditionId = currentResultSet.getInt(column.getKey());
                    } else {
                        log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }
                }
                return log.traceExit(new ESTLibraryTO(estLibraryId, estLibraryName, 
                        estLibraryDescription, dataSourceid, conditionId));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }
}
