package org.bgee.model.dao.mysql.expressiondata.rawdata.rnaseq;

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
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqExperimentDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
import org.bgee.model.dao.mysql.expressiondata.rawdata.MySQLRawDataDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.RawDataFiltersToDatabaseMapping;

public final class MySQLRNASeqExperimentDAO extends MySQLRawDataDAO<RNASeqExperimentDAO.Attribute> 
implements RNASeqExperimentDAO{

    private final static Logger log = LogManager.getLogger(MySQLRNASeqExperimentDAO.class.getName());
    public final static String TABLE_NAME = "rnaSeqExperimentDev";

    public MySQLRNASeqExperimentDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public RNASeqExperimentTOResultSet getExperiments(Collection<DAORawDataFilter> rawDataFilters,
            Boolean isSingleCell, Long offset, Integer limit,
            Collection<RNASeqExperimentDAO.Attribute> attrs) throws DAOException {
        log.traceEntry("{}, {}, {}, {}, {}", rawDataFilters, isSingleCell, offset, limit, attrs);
        checkOffsetAndLimit(offset, limit);

        final DAOProcessedRawDataFilter<Integer> processedFilters =
                new DAOProcessedRawDataFilter<>(rawDataFilters);
        final Set<RNASeqExperimentDAO.Attribute> clonedAttrs = Collections
                .unmodifiableSet(attrs == null || attrs.isEmpty()?
                EnumSet.allOf(RNASeqExperimentDAO.Attribute.class): EnumSet.copyOf(attrs));

        StringBuilder sb = new StringBuilder();

        // generate SELECT
        sb.append(generateSelectClauseRawDataFilters(processedFilters, TABLE_NAME,
                getColToAttributesMap(RNASeqExperimentDAO.Attribute.class), true, clonedAttrs));

        // generate FROM
        RawDataFiltersToDatabaseMapping filtersToDatabaseMapping = generateFromClauseRawData(sb,
                processedFilters, isSingleCell, Set.of(TABLE_NAME),
                DAODataType.RNA_SEQ);

        // generate WHERE CLAUSE
        if (!processedFilters.getRawDataFilters().isEmpty() || isSingleCell != null) {
            sb.append(" WHERE ")
              .append(generateWhereClauseRawDataFilter(processedFilters,
                    filtersToDatabaseMapping, isSingleCell));
        }

        // generate ORDER BY
        sb.append(" ORDER BY")
        .append(" " + TABLE_NAME + "." + RNASeqExperimentDAO.Attribute.ID
                .getTOFieldName());

        //generate offset and limit
        if (limit != null) {
            sb.append(offset == null ? " LIMIT ?": " LIMIT ?, ?");
        }

        try {
            BgeePreparedStatement stmt = this.parameterizeQuery(sb.toString(), processedFilters,
                    isSingleCell, DAODataType.RNA_SEQ, offset, limit);
            return log.traceExit(new MySQLRNASeqExperimentTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    class MySQLRNASeqExperimentTOResultSet extends MySQLDAOResultSet<RNASeqExperimentTO> 
    implements RNASeqExperimentTOResultSet{

        /**
         * @param statement The {@code BgeePreparedStatement}
         */
        private MySQLRNASeqExperimentTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected RNASeqExperimentDAO.RNASeqExperimentTO getNewTO() throws DAOException {
            log.traceEntry();
            try {
                final ResultSet currentResultSet = this.getCurrentResultSet();
                Integer dataSourceId = null;
                String id = null, name = null, description = null;

                for (Entry<Integer, String> column : this.getColumnLabels().entrySet()) {
                    if (column.getValue().equals(RNASeqExperimentDAO.Attribute.ID.getTOFieldName())) {
                        id = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(RNASeqExperimentDAO.Attribute.NAME
                            .getTOFieldName())) {
                        name = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(RNASeqExperimentDAO.Attribute.DESCRIPTION
                            .getTOFieldName())) {
                        description = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(RNASeqExperimentDAO.Attribute.DATA_SOURCE_ID
                            .getTOFieldName())) {
                        dataSourceId = currentResultSet.getInt(column.getKey());
                    } else {
                        log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }
                }
                return log.traceExit(new RNASeqExperimentTO(id, name, description, dataSourceId));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }

}
