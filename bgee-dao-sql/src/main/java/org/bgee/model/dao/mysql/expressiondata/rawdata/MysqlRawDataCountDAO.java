package org.bgee.model.dao.mysql.expressiondata.rawdata;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCountDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO;
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
                Collections.unmodifiableList(rawDataFilters == null? new ArrayList<>():
                    new ArrayList<>(rawDataFilters));
        StringBuilder sb = new StringBuilder();

        boolean needSpeciesId = orderedRawDataFilters.stream().anyMatch(e -> e.getSpeciesId() != null);
        boolean needGeneId = orderedRawDataFilters.stream().anyMatch(e -> !e.getGeneIds().isEmpty());
        boolean needChipTableInfo = orderedRawDataFilters.stream()
                .anyMatch(e -> !e.getAssayIds().isEmpty() ||
                        !e.getExperimentIds().isEmpty() ||
                        !e.getExprOrAssayIds().isEmpty() ||
                        !e.getRawDataCondIds().isEmpty());
        //We don't need the chip table if call counts are requested but not experiment counts,
        //and we don't need info about exp. or assay public IDs or cond. IDs.
        //In that case the number of assays can be retrieved, if requested,
        //by counting the number of distinct bgeeAffymetrixChipIds in the affymetrixProbeset table.
        //And a filtering on speciesIds can be performed through the gene table directly.
        boolean chipTable = !(callsCount && !experimentCount && !needChipTableInfo);
        boolean probesetTable = needGeneId || callsCount;
        assert chipTable || probesetTable:
            "If chip table not needed then probeset table needed for call counts";
        //We use the condition table only if we need to search for species,
        //and that it was necessary to join to the chip table already
        boolean condTable = needSpeciesId && chipTable;
        //otherwise we use the gene table
        boolean geneTable = needSpeciesId && !chipTable;
        assert !(condTable && geneTable): "If condition table needed, we never use the gene table";
        //In case we use a SELECT STRAIGHT_JOIN, we start from the probeset table
        //if that table is needed and if no filtering is requested on chip table
        String tableName = probesetTable && (!needChipTableInfo && !condTable || needGeneId)?
                MySQLAffymetrixProbesetDAO.TABLE_NAME: MySQLAffymetrixChipDAO.TABLE_NAME;
        // generate SELECT clause
        sb.append("SELECT STRAIGHT_JOIN");
        boolean previousCount = false;
        if (experimentCount) {
            assert chipTable;
            sb.append(" count(distinct ").append(tableName).append(".")
                    .append(AffymetrixChipDAO.Attribute.EXPERIMENT_ID.getTOFieldName()).append(") as ")
                    .append(RawDataCountDAO.Attribute.EXP_COUNT.getTOFieldName());
            previousCount = true;
        }
        if (assayCount) {
            if(previousCount) {
                sb.append(",");
            }
            sb.append(" count(");
            if (!probesetTable) {
                //count(*) is faster than count(columnName)
                //(see https://stackoverflow.com/a/3003482).
                //If the probesets were not required, then
                //number of lines = number of bgeeAffymetrixChipId
                //(primary key of the affymetrixChip table)
                sb.append("*");
            } else {
                //If probesets are needed, we need to add DISTINCT,
                //because relation 1-to-many to table affymetrixProbeset.
                sb.append("distinct ").append(tableName).append(".")
                        .append(AffymetrixChipDAO.Attribute.BGEE_AFFYMETRIX_CHIP_ID.getTOFieldName());
            }
            sb.append(") as ")
              .append(RawDataCountDAO.Attribute.ASSAY_COUNT.getTOFieldName());
            previousCount = true;
        }
        if (callsCount) {
            assert probesetTable;
            if(previousCount) {
                sb.append(",");
            }
            //Here, number of lines always equal to
            //count(distinct affymetrixProbesetId, bgeeAffymetrixChipId)
            //(the primary key of the table that we need to count).
            //We wouldn't need the DISTINCT.
            //And count(*) is faster than count(columnName)
            sb.append(" count(*) as ")
              .append(RawDataCountDAO.Attribute.CALLS_COUNT.getTOFieldName());
        }

        // create a 
        Set<String> necessaryTables = new HashSet<>();
        necessaryTables.add(tableName);
        if (!tableName.equals(MySQLAffymetrixChipDAO.TABLE_NAME) && chipTable) {
            necessaryTables.add(MySQLAffymetrixChipDAO.TABLE_NAME);
        } else if (!tableName.equals(MySQLAffymetrixProbesetDAO.TABLE_NAME) && probesetTable) {
            necessaryTables.add(MySQLAffymetrixProbesetDAO.TABLE_NAME);
        }
//
//        // generate FROM clause
        Map<RawDataColumn, String> colToTable = generateFromClauseRawData(sb, orderedRawDataFilters,
                necessaryTables, DAODataType.AFFYMETRIX);

        // generate WHERE CLAUSE
        if(!orderedRawDataFilters.isEmpty()) {
            sb.append(" WHERE ")
            .append(generateWhereClause(orderedRawDataFilters, colToTable));
        }
        try {
            BgeePreparedStatement stmt = this.parameterizeQuery(sb.toString(), orderedRawDataFilters,
                    null, null);
            MySQLRawDataCountContainerTOResultSet resultSet = new MySQLRawDataCountContainerTOResultSet(stmt);
            resultSet.next();
            RawDataCountContainerTO to = resultSet.getTO();
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
