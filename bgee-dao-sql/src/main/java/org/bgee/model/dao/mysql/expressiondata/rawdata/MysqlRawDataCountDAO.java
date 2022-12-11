package org.bgee.model.dao.mysql.expressiondata.rawdata;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.rawdata.DAOProcessedRawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCountDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.est.ESTDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituEvidenceDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituSpotDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryAnnotatedSampleDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqResultAnnotatedSampleDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
import org.bgee.model.dao.mysql.expressiondata.rawdata.est.MySQLESTDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.est.MySQLESTLibraryDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.insitu.MySQLInSituEvidenceDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.insitu.MySQLInSituSpotDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.microarray.MySQLAffymetrixChipDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.microarray.MySQLAffymetrixProbesetDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.rnaseq.MySQLRNASeqLibraryAnnotatedSampleDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.rnaseq.MySQLRNASeqLibraryDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.rnaseq.MySQLRNASeqResultAnnotatedSampleDAO;

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
        final DAOProcessedRawDataFilter processedRawDataFilters = 
                new DAOProcessedRawDataFilter(rawDataFilters);
        StringBuilder sb = new StringBuilder();

        boolean probesetTable = processedRawDataFilters.isNeedGeneId() || callsCount;

        // generate SELECT clause
        sb.append("SELECT STRAIGHT_JOIN");
        boolean previousCount = false;
        if (experimentCount) {
            sb.append(" count(distinct ").append(MySQLAffymetrixChipDAO.TABLE_NAME).append(".")
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
                sb.append("distinct ").append(MySQLAffymetrixProbesetDAO.TABLE_NAME).append(".")
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

        // create the set of tables it is necessary to use in FROM clause even if no filter
        // on those columns
        Set<String> necessaryTables = new HashSet<>();
        if (callsCount) {
            necessaryTables.add(MySQLAffymetrixProbesetDAO.TABLE_NAME);
        }
        if (experimentCount || assayCount && !callsCount) {
            necessaryTables.add(MySQLAffymetrixChipDAO.TABLE_NAME);
        }

        // generate FROM clause
        RawDataFiltersToDatabaseMapping filtersToDatabaseMapping = generateFromClauseRawData(sb,
                processedRawDataFilters, null, necessaryTables, DAODataType.AFFYMETRIX);

        // generate WHERE CLAUSE
        if(!processedRawDataFilters.getRawDataFilters().isEmpty()) {
            sb.append(" WHERE ")
            .append(generateWhereClauseRawDataFilter(processedRawDataFilters,
                    filtersToDatabaseMapping));
        }
        try {
            BgeePreparedStatement stmt = this.parameterizeQuery(sb.toString(), 
                    processedRawDataFilters, DAODataType.AFFYMETRIX, null, null);
            MySQLRawDataCountContainerTOResultSet resultSet = new MySQLRawDataCountContainerTOResultSet(stmt);
            resultSet.next();
            RawDataCountContainerTO to = resultSet.getTO();
            resultSet.close();
            return log.traceExit(to);
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    public RawDataCountContainerTO getESTCount(Collection<DAORawDataFilter> rawDataFilters,
            boolean assayCount, boolean callCount) {
        log.traceEntry("{}, {},{}, {}", rawDataFilters, assayCount, callCount);
        if (!assayCount && !callCount) {
            throw log.throwing(new IllegalArgumentException("experimentCount, assayCount and"
                    + " callsCount can not be all false at the same time"));
        }
        final DAOProcessedRawDataFilter processedRawDataFilters = 
                new DAOProcessedRawDataFilter(rawDataFilters);
        StringBuilder sb = new StringBuilder();

        boolean callTable = processedRawDataFilters.isNeedGeneId() || callCount;

        // generate SELECT clause
        sb.append("SELECT STRAIGHT_JOIN");
        boolean previousCount = false;
        if (assayCount) {
            sb.append(" count(");
            if (!callTable) {
                //count(*) is faster than count(columnName)
                //(see https://stackoverflow.com/a/3003482).
                //If the calls were not required, then
                //number of lines = number of estLibraryId
                //(primary key of the estLibrary table)
                sb.append("*");
            } else {
                //If calls are needed, we need to add DISTINCT,
                //because relation 1-to-many to table call.
                sb.append("distinct ").append(MySQLESTDAO.TABLE_NAME).append(".")
                        .append(ESTDAO.Attribute.EST_LIBRARY_ID.getTOFieldName());
            }
            sb.append(") as ")
              .append(RawDataCountDAO.Attribute.ASSAY_COUNT.getTOFieldName());
            previousCount = true;
        }
        if (callCount) {
            assert callTable;
            if(previousCount) {
                sb.append(",");
            }
            //Here, number of lines always equal to
            //count(distinct estId)
            //(the primary key of the table that we need to count).
            //We wouldn't need the DISTINCT.
            //And count(*) is faster than count(columnName)
            sb.append(" count(*) as ")
              .append(RawDataCountDAO.Attribute.CALLS_COUNT.getTOFieldName());
        }

        // create the set of tables it is necessary to use in FROM clause even if no filter
        // on those columns.
        // If callCount then all count can be retrieved from the call table
        Set<String> necessaryTables = new HashSet<>();
        if (callCount) {
            necessaryTables.add(MySQLESTDAO.TABLE_NAME);
        } else {
            assert assayCount;
            necessaryTables.add(MySQLESTLibraryDAO.TABLE_NAME);
        }

        // generate FROM clause
        RawDataFiltersToDatabaseMapping filtersToDatabaseMapping = generateFromClauseRawData(sb,
                processedRawDataFilters, null, necessaryTables, DAODataType.EST);

        // generate WHERE CLAUSE
        if(!processedRawDataFilters.getRawDataFilters().isEmpty()) {
            sb.append(" WHERE ")
            .append(generateWhereClauseRawDataFilter(processedRawDataFilters,
                    filtersToDatabaseMapping));
        }
        try {
            BgeePreparedStatement stmt = this.parameterizeQuery(sb.toString(), 
                    processedRawDataFilters, DAODataType.EST, null, null);
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
    public RawDataCountContainerTO getInSituCount(Collection<DAORawDataFilter> rawDataFilters,
            boolean experimentCount, boolean assayCount, boolean assayConditionCount,
            boolean callCount) {
        log.traceEntry("{}, {},{}, {}", rawDataFilters, experimentCount, assayCount, callCount);
        if (!experimentCount && !assayCount && !callCount) {
            throw log.throwing(new IllegalArgumentException("experimentCount, assayCount and"
                    + " callsCount can not be all false at the same time"));
        }
        final DAOProcessedRawDataFilter processedRawDataFilters = 
                new DAOProcessedRawDataFilter(rawDataFilters);
        StringBuilder sb = new StringBuilder();

        // for insitu the condition is linked to a call
        boolean callTable = processedRawDataFilters.isNeedGeneId() || callCount ||
                processedRawDataFilters.isNeedConditionId() || assayConditionCount;

        // generate SELECT clause
        sb.append("SELECT STRAIGHT_JOIN");
        boolean previousCount = false;
        if (experimentCount) {
            sb.append(" count(distinct ").append(MySQLInSituEvidenceDAO.TABLE_NAME).append(".")
                    .append(InSituEvidenceDAO.Attribute.EXPERIMENT_ID.getTOFieldName()).append(") as ")
                    .append(RawDataCountDAO.Attribute.EXP_COUNT.getTOFieldName());
            previousCount = true;
        }
        if (assayCount) {
            if(previousCount) {
                sb.append(",");
            }
            sb.append(" count(");
            if (!callTable) {
                //count(*) is faster than count(columnName)
                //(see https://stackoverflow.com/a/3003482).
                //If the spots were not required, then
                //number of lines = number of inSituEvidenceId
                //(primary key of the inSituEvidence table)
                sb.append("*");
            } else {
                //If spots are needed, we need to add DISTINCT,
                //because relation 1-to-many to table inSituSpot.
                sb.append("distinct ").append(MySQLInSituSpotDAO.TABLE_NAME).append(".")
                        .append(InSituSpotDAO.Attribute.IN_SITU_EVIDENCE_ID.getTOFieldName());

            }
            sb.append(") as ")
              .append(RawDataCountDAO.Attribute.ASSAY_COUNT.getTOFieldName());
            previousCount = true;
        }
        if (assayConditionCount) {
            if(previousCount) {
                sb.append(",");
            }
            sb.append(" count(distinct ")
            .append(MySQLInSituSpotDAO.TABLE_NAME).append(".")
            .append(InSituSpotDAO.Attribute.IN_SITU_EVIDENCE_ID.getTOFieldName())
            .append(", ").append(MySQLInSituSpotDAO.TABLE_NAME).append(".")
            .append(InSituSpotDAO.Attribute.CONDITION_ID.getTOFieldName());
            sb.append(") as ")
              .append(RawDataCountDAO.Attribute.INSITU_ASSAY_COND_COUNT.getTOFieldName());
            previousCount = true;
        }
        if (callCount) {
            assert callTable;
            if(previousCount) {
                sb.append(",");
            }
            //Here, number of lines always equal to
            //count(distinct inSituSpotId)
            //(the primary key of the table that we need to count).
            //We wouldn't need the DISTINCT.
            //And count(*) is faster than count(columnName)
            sb.append(" count(*) as ")
              .append(RawDataCountDAO.Attribute.CALLS_COUNT.getTOFieldName());
        }

        // create the set of tables it is necessary to use in FROM clause even if no filter
        // on those columns
        Set<String> necessaryTables = new HashSet<>();
        if (callCount || assayConditionCount) {
            necessaryTables.add(MySQLInSituSpotDAO.TABLE_NAME);
        }
        if (experimentCount || assayCount && !callCount && !assayConditionCount) {
            necessaryTables.add(MySQLInSituEvidenceDAO.TABLE_NAME);
        }

        // generate FROM clause
        RawDataFiltersToDatabaseMapping filtersToDatabaseMapping = generateFromClauseRawData(sb,
                processedRawDataFilters, null, necessaryTables, DAODataType.IN_SITU);

        // generate WHERE CLAUSE
        if(!processedRawDataFilters.getRawDataFilters().isEmpty()) {
            sb.append(" WHERE ")
            .append(generateWhereClauseRawDataFilter(processedRawDataFilters,
                    filtersToDatabaseMapping));
        }
        try {
            BgeePreparedStatement stmt = this.parameterizeQuery(sb.toString(), 
                    processedRawDataFilters, DAODataType.IN_SITU, null, null);
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
    public RawDataCountContainerTO getRnaSeqCount(Collection<DAORawDataFilter> rawDataFilters,
            Boolean isSingleCell, boolean experimentCount, boolean libraryCount,
            boolean assayCount, boolean callCount) {
        log.traceEntry("{}, {},{}, {}", rawDataFilters, isSingleCell, experimentCount, libraryCount,
                assayCount, callCount);
        if (!experimentCount && !libraryCount && !assayCount && !callCount) {
            throw log.throwing(new IllegalArgumentException(
                    "experimentCount, libraryCount, assayCount and"
                    + " callsCount can not be all false at the same time"));
        }
        // force to have a list in order to keep order of elements. It is mandatory to be able
        // to first generate a parameterised query and then add values.
        final DAOProcessedRawDataFilter processedFilters = new DAOProcessedRawDataFilter(rawDataFilters);
        StringBuilder sb = new StringBuilder();

        boolean callTable = processedFilters.isNeedGeneId() || callCount;
        // used to know if it is possible to do a count(*) for rnaSeqLibrary count
        boolean assayOrCallTable = callTable || assayCount || processedFilters.isNeedConditionId();

        // generate SELECT clause
        sb.append("SELECT STRAIGHT_JOIN");
        boolean previousCount = false;
        if (experimentCount) {
            sb.append(" count(distinct ").append(MySQLRNASeqLibraryDAO.TABLE_NAME).append(".")
                    .append(RNASeqLibraryDAO.Attribute.EXPERIMENT_ID.getTOFieldName()).append(") as ")
                    .append(RawDataCountDAO.Attribute.EXP_COUNT.getTOFieldName());
            previousCount = true;
        }
        if (libraryCount) {
            if(previousCount) {
                sb.append(",");
            }
            sb.append(" count(");
            if (!assayOrCallTable) {
                //count(*) is faster than count(columnName)
                //(see https://stackoverflow.com/a/3003482).
                //If the assays were not required, then
                //number of lines = number of rnaSeqLibraryId
                sb.append("*");
            } else {
                //If assay are needed, we need to add DISTINCT,
                //because relation 1-to-many to table rnaSeqLibraryAnnotatedSample.
                sb.append("distinct ").append(MySQLRNASeqLibraryAnnotatedSampleDAO.TABLE_NAME)
                .append(".").append(RNASeqLibraryAnnotatedSampleDAO.Attribute.RNASEQ_LIBRARY_ID
                        .getTOFieldName());
            }
            sb.append(") as ")
              .append(RawDataCountDAO.Attribute.RNA_SEQ_LIBRARY_COUNT.getTOFieldName());
            previousCount = true;
        }
        if (assayCount) {
            assert assayOrCallTable;
            if(previousCount) {
                sb.append(",");
            }
            sb.append(" count(");
            if (!callTable) {
                //If the calls were not required, then
                //number of lines = number of rnaSeqLibraryAnnotatedSampleId
                //(primary key of the rnaSeqLibraryAnnotatedSample table)
                sb.append("*");
            } else {
                //If calls are needed, we need to add DISTINCT,
                //because relation 1-to-many to table rnaSeqLibraryAnnotatedSampleGeneResult.
                sb.append("distinct ").append(MySQLRNASeqResultAnnotatedSampleDAO.TABLE_NAME)
                .append(".").append(RNASeqResultAnnotatedSampleDAO.Attribute
                        .LIBRARY_ANNOTATED_SAMPLE_ID.getTOFieldName());
            }
            sb.append(") as ")
              .append(RawDataCountDAO.Attribute.ASSAY_COUNT.getTOFieldName());
            previousCount = true;
        }
        if (callCount) {
            assert callTable;
            if(previousCount) {
                sb.append(",");
            }
            //Here, number of lines always equal to
            //count(distinct rnaSeqLibraryAnnotatedSampleGeneResultId)
            //(the primary key of the table that we need to count).
            //We wouldn't need the DISTINCT.
            //And count(*) is faster than count(columnName)
            sb.append(" count(*) as ")
              .append(RawDataCountDAO.Attribute.CALLS_COUNT.getTOFieldName());
        }

        // create the set of tables it is necessary to use in FROM clause even if no filter
        // on those columns
        Set<String> necessaryTables = new HashSet<>();
        if (callCount) {
            necessaryTables.add(MySQLRNASeqResultAnnotatedSampleDAO.TABLE_NAME);
        }
        if (libraryCount || assayCount) {
            necessaryTables.add(MySQLRNASeqLibraryAnnotatedSampleDAO.TABLE_NAME);
        }
        if (experimentCount) {
            necessaryTables.add(MySQLRNASeqLibraryDAO.TABLE_NAME);
        }
        // generate FROM clause
        RawDataFiltersToDatabaseMapping filtersToDatabaseMapping = generateFromClauseRawData(sb,
                processedFilters, isSingleCell, necessaryTables, DAODataType.RNA_SEQ);

        // generate WHERE CLAUSE
        if (!processedFilters.getRawDataFilters().isEmpty() || isSingleCell != null) {
            sb.append(" WHERE ");
        }
        boolean foundPrevious = false;
        if(!processedFilters.getRawDataFilters().isEmpty()) {
            sb.append(generateWhereClauseRawDataFilter(processedFilters, filtersToDatabaseMapping));
            foundPrevious = true;
        }
        foundPrevious = generateWhereClauseTechnologyRnaSeq(sb, isSingleCell, foundPrevious);
        try {
            BgeePreparedStatement stmt = this.parameterizeQuery(sb.toString(), processedFilters,
                    isSingleCell, DAODataType.RNA_SEQ, null, null);
            MySQLRawDataCountContainerTOResultSet resultSet = new MySQLRawDataCountContainerTOResultSet(stmt);
            resultSet.next();
            RawDataCountContainerTO to = resultSet.getTO();
            resultSet.close();
            return log.traceExit(to);
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
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
                    rnaSeqLibraryCount = null, insituAssayConditionCount = null;
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

                    } else if (column.getValue().equals(RawDataCountDAO.Attribute.INSITU_ASSAY_COND_COUNT
                            .getTOFieldName())) {
                        insituAssayConditionCount = this.getCurrentResultSet().getInt(column.getKey());

                    } else {
                        throw log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }
                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            // Set RawDataCountContainerTO
            return log.traceExit(new RawDataCountContainerTO(expCount, assayCount, callsCount,
                    rnaSeqLibraryCount, insituAssayConditionCount));
        }
    }
}
