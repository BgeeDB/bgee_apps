package org.bgee.model.dao.mysql.expressiondata.rawdata;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

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
            boolean experimentCount, boolean assayCount, boolean callsCount) {
        log.traceEntry("{}, {},{}, {}", rawDataFilters, experimentCount, assayCount, callsCount);
        if (!experimentCount && !assayCount && !callsCount) {
            throw log.throwing(new IllegalArgumentException("experimentCount, assayCount and"
                    + " callsCount can not be all false at the same time"));
        }
        // force to have a list in order to keep order of elements. It is mandatory to be able
        // to first generate a parameterised query and then add values.
        final List<DAORawDataFilter> orderedRawDataFilters = 
                Collections.unmodifiableList(new ArrayList<>(rawDataFilters));
        StringBuilder sb = new StringBuilder();

        // if ask only for calls count then start from calls table. Otherwise start from
        // assay table
        String tableName = callsCount && !assayCount && !experimentCount ?
                MySQLAffymetrixProbesetDAO.TABLE_NAME: MySQLAffymetrixChipDAO.TABLE_NAME;
        boolean needJoinProbeset = rawDataFilters.stream()
                .anyMatch(e -> !e.getGeneIds().isEmpty() || callsCount);
        boolean needJoinCond = rawDataFilters.stream().anyMatch(e -> e.getSpeciesId() != null);
        boolean needJoinChip = rawDataFilters.stream().anyMatch(e -> !e.getAssayIds().isEmpty() ||
                !e.getExperimentIds().isEmpty() || !e.getExprOrAssayIds().isEmpty() ||
                !e.getRawDataCondIds().isEmpty() || e.getSpeciesId() != null);
        // generate SELECT clause
        sb.append("SELECT");
        boolean previousCount = false;
        if (experimentCount) {
            sb.append(" count(distinct " + MySQLAffymetrixChipDAO.TABLE_NAME + "." + 
                    AffymetrixChipDAO.Attribute.EXPERIMENT_ID.getTOFieldName() + ") as ")
            .append(RawDataCountDAO.Attribute.EXP_COUNT.getTOFieldName());
            previousCount = true;
        }
        if (assayCount) {
            if(previousCount) {
                sb.append(",");
            }
            sb.append(" count(distinct " + MySQLAffymetrixChipDAO.TABLE_NAME + "." + 
                AffymetrixChipDAO.Attribute.AFFYMETRIX_CHIP_ID.getTOFieldName() + ") as ")
        .append(RawDataCountDAO.Attribute.ASSAY_COUNT.getTOFieldName());
            previousCount = true;
        }
        if (callsCount) {
            if(previousCount) {
                sb.append(",");
            }
            sb.append(" count(distinct " + MySQLAffymetrixProbesetDAO.TABLE_NAME + "." + 
                    AffymetrixProbesetDAO.Attribute.ID.getTOFieldName() + ") as ")
            .append(RawDataCountDAO.Attribute.CALLS_COUNT.getTOFieldName());
        }

        // generate FROM clause
        if(tableName.equals(MySQLAffymetrixProbesetDAO.TABLE_NAME)) {
            sb.append(this.generateFromClauseAffymetrix(MySQLAffymetrixProbesetDAO.TABLE_NAME,
                    false, needJoinChip, false, needJoinCond, false));
        } else if (tableName.equals(MySQLAffymetrixChipDAO.TABLE_NAME)) {
            sb.append(this.generateFromClauseAffymetrix(MySQLAffymetrixChipDAO.TABLE_NAME,
                    false, false, needJoinProbeset, needJoinCond, false));
        } else {
            throw log.throwing(new IllegalArgumentException("unrecognized table " + tableName));
        }

        // generate WHERE CLAUSE
        if(rawDataFilters != null || !rawDataFilters.isEmpty()) {
            sb.append(" WHERE ")
            .append(generateWhereClause(orderedRawDataFilters, MySQLAffymetrixChipDAO.TABLE_NAME,
                    MySQLRawDataConditionDAO.TABLE_NAME));
        }
        try {
            BgeePreparedStatement stmt = this.parameterizeQuery(sb.toString(), orderedRawDataFilters,
                    null, null);
            MySQLRawDataCountContainerTOResultSet resultSet = new MySQLRawDataCountContainerTOResultSet(stmt);
            RawDataCountContainerTO to = resultSet.getAllTOs().iterator().next();
            resultSet.close();
            return log.traceExit(to);
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public RawDataCountContainerTO getEstCount(Collection<DAORawDataFilter> arg0, boolean arg1, boolean arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RawDataCountContainerTO getInSituCount(Collection<DAORawDataFilter> arg0, boolean arg1, boolean arg2,
            boolean arg3) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RawDataCountContainerTO getRnaSeqCount(Collection<DAORawDataFilter> arg0, boolean arg1, boolean arg2,
            boolean arg3, boolean arg4) {
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
            Integer expCount = null, assayCount= null, callsCount = null,
                    rnaSeqLibraryCount = null;
            // Get results
            for (Entry<Integer, String> column : this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals(RawDataCountDAO.Attribute.EXP_COUNT
                            .getTOFieldName())) {
                        expCount = this.getCurrentResultSet().getInt(column.getKey());

                    } else if (column.getValue().equals(RawDataCountDAO.Attribute.ASSAY_COUNT
                            .getTOFieldName())) {
                        assayCount = this.getCurrentResultSet().getInt(column.getKey());

                    } else if (column.getValue().equals(RawDataCountDAO.Attribute.CALLS_COUNT
                            .getTOFieldName())) {
                        callsCount = this.getCurrentResultSet().getInt(column.getKey());

                    } else if (column.getValue().equals(RawDataCountDAO.Attribute.RNA_SEQ_LIBRARY_COUNT
                            .getTOFieldName())) {
                        rnaSeqLibraryCount = this.getCurrentResultSet().getInt(column.getKey());

                    } else {
                        throw log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }
                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            // Set RawDataCountContainerTO
            return log.traceExit(new RawDataCountContainerTO(expCount, assayCount, callsCount,
                    rnaSeqLibraryCount));
        }
    }
}
