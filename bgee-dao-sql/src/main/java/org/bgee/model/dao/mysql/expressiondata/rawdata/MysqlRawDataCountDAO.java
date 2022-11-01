package org.bgee.model.dao.mysql.expressiondata.rawdata;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCountDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixProbesetDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
import org.bgee.model.dao.mysql.expressiondata.rawdata.microarray.MySQLAffymetrixChipDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.microarray.MySQLAffymetrixProbesetDAO;

public class MysqlRawDataCountDAO extends MySQLRawDataDAO<RawDataCountDAO.Attribute>
        implements RawDataCountDAO {

    public MysqlRawDataCountDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    private static final Logger log = LogManager.getLogger(MysqlRawDataCountDAO.class.getName());

    @Override
    public RawDataCountContainerTO getAffymetrixCount(Collection<DAORawDataFilter> rawDataFilters,
            boolean resultCount) {
        if (rawDataFilters == null || rawDataFilters.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("rawDataFilters can not be null or"
                    + " empty"));
        }
        log.traceEntry("{}, {}", rawDataFilters, resultCount);
        // force to have a list in order to keep order of elements. It is mandatory to be able
        // to first generate a parameterised query and then add values.
        final List<DAORawDataFilter> orderedRawDataFilters = 
                Collections.unmodifiableList(new ArrayList<>(rawDataFilters));
        StringBuilder sb = new StringBuilder();
        boolean needJoinProbeset = rawDataFilters.stream()
                .anyMatch(e -> !e.getGeneIds().isEmpty() || resultCount);
        boolean needJoinCond = rawDataFilters.stream().anyMatch(e -> e.getSpeciesId() != null);

        // generate SELECT clause
        sb.append("SELECT")
        .append(" count(distinct " + MySQLAffymetrixChipDAO.TABLE_NAME + "." + 
                AffymetrixChipDAO.Attribute.EXPERIMENT_ID.getTOFieldName() + ") as ")
        .append(RawDataCountDAO.Attribute.AFFY_EXP_COUNT.getTOFieldName())
        .append(", count(distinct " + MySQLAffymetrixChipDAO.TABLE_NAME + "." + 
                AffymetrixChipDAO.Attribute.AFFYMETRIX_CHIP_ID.getTOFieldName() + ") as ")
        .append(RawDataCountDAO.Attribute.AFFY_ASSAY_COUNT.getTOFieldName());
        if (resultCount) {
            sb.append(", count(distinct " + MySQLAffymetrixProbesetDAO.TABLE_NAME + "." + 
                    AffymetrixProbesetDAO.Attribute.ID.getTOFieldName() + ") as ")
            .append(RawDataCountDAO.Attribute.AFFY_RESULT_COUNT.getTOFieldName());
        }

        // generate FROM clause
        this.generateFromClauseAffymetrix(MySQLAffymetrixChipDAO.TABLE_NAME,
                false, false, needJoinProbeset, needJoinCond, false);

        // generate WHERE CLAUSE
        // there is always a where condition as at least a speciesId, a geneId or a conditionId
        // has to be provided in a rawDataFilter.
        sb.append(" WHERE ")
        .append(orderedRawDataFilters.stream()
                .map(e -> this.generateOneFilterWhereClause(e, false))
                .collect(Collectors.joining(") OR (", " (", ")")));
        
        try {
            BgeePreparedStatement stmt = this.parameteriseQuery(sb.toString(), orderedRawDataFilters);
            MySQLRawDataCountContainerTOResultSet resultSet = new MySQLRawDataCountContainerTOResultSet(stmt);
            RawDataCountContainerTO to = resultSet.getAllTOs().iterator().next();
            resultSet.close();
            return log.traceExit(to);
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public RawDataCountContainerTO getEstCount(Collection<DAORawDataFilter> rawDataFilters,
            boolean resultCount) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RawDataCountContainerTO getInSituCount(Collection<DAORawDataFilter> rawDataFilters,
            boolean resultCount) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RawDataCountContainerTO getRnaSeqCount(Collection<DAORawDataFilter> rawDataFilters,
            boolean arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * A {@code MySQLDAOResultSet} specific to {@code RawDataCountContainerTO}.
     *
     * @author Julien Wollbrett
     * @version Bgee 15
     * @since Bgee 15
     */
    public class MySQLRawDataCountContainerTOResultSet extends MySQLDAOResultSet<RawDataCountContainerTO> 
        implements RawDataCountContainerTOResultSet {

        /**
         * Delegates to
         * {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)}
         * super constructor.
         *
         * @param statement
         *            The first {@code BgeePreparedStatement} to execute a query
         *            on.
         */
        private MySQLRawDataCountContainerTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected RawDataCountContainerTO getNewTO() {
            log.traceEntry();
            Integer affyExpCount = null, affyAssayCount= null, affyResultCount = null;
            // Get results
            for (Entry<Integer, String> column : this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals(RawDataCountDAO.Attribute.AFFY_EXP_COUNT
                            .getTOFieldName())) {
                        affyExpCount = this.getCurrentResultSet().getInt(column.getKey());

                    } else if (column.getValue().equals(RawDataCountDAO.Attribute.AFFY_ASSAY_COUNT
                            .getTOFieldName())) {
                        affyAssayCount = this.getCurrentResultSet().getInt(column.getKey());

                    } else if (column.getValue().equals(RawDataCountDAO.Attribute.AFFY_RESULT_COUNT
                            .getTOFieldName())) {
                        affyResultCount = this.getCurrentResultSet().getInt(column.getKey());

                    } else {
                        throw log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }
                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            // Set RawDataCountContainerTO
            return log.traceExit(new RawDataCountContainerTO(affyExpCount, affyAssayCount, affyResultCount));
        }
    }
}
