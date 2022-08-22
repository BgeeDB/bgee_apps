package org.bgee.model.dao.mysql.expressiondata.rawdata.rnaseq;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataConditionFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqExperimentDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;

public final class MySQLRNASeqExperimentDAO extends MySQLDAO<RNASeqExperimentDAO.Attribute> 
implements RNASeqExperimentDAO{

    private final static Logger log = LogManager.getLogger(MySQLRNASeqExperimentDAO.class.getName());
    private final static String TABLE_NAME = "RNASeqExperiment";
    private final static String CONDITION_TABLE_NAME = "cond";
    private final static String LIB_TABLE_NAME = "RNASeqLibrary";
    private final static String ANNOTATED_LIB_TABLE_NAME = "RNASeqLibraryAnnotatedSample";

    public MySQLRNASeqExperimentDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }
    
    @Override
    public RNASeqExperimentTOResultSet getExperimentsFromIds(Collection<String> experimentIds,
            Collection<RNASeqExperimentDAO.Attribute> attrs) {
        log.traceEntry("{}, {}", experimentIds, attrs);
        return log.traceExit(getExperiments(experimentIds, null, attrs));
    }

    @Override
    public RNASeqExperimentTOResultSet getExperimentsFromRawDataFilter(DAORawDataFilter filter,
            Collection<RNASeqExperimentDAO.Attribute> attrs)
            throws DAOException {
        log.traceEntry("{}, {}", filter, attrs);
        return log.traceExit(getExperiments(null, filter, attrs));
    }

    //XXX For RNASeq experiments we do not filter on gene as all genes could potentially
    // have presence of expression in the experiment. So even if raw data filter contains
    // some genes we do not use them in the query. It avoids a JOIN on table
    // rnaSeqLibraryAnnotatedSampleResult
    @Override
    public RNASeqExperimentTOResultSet getExperiments(Collection<String> experimentIds,
            DAORawDataFilter filter, Collection<RNASeqExperimentDAO.Attribute> attrs) {
        log.traceEntry("{}", experimentIds);
        final Set<String> clonedExpIds = Collections.unmodifiableSet(experimentIds == null?
                new HashSet<String>(): new HashSet<String>(experimentIds));
        final DAORawDataFilter clonedFilter = new DAORawDataFilter(filter);
        final Set<RNASeqExperimentDAO.Attribute> clonedAttrs = Collections.unmodifiableSet(attrs == null?
                new HashSet<RNASeqExperimentDAO.Attribute>():
                    new HashSet<RNASeqExperimentDAO.Attribute>(attrs));

        // generate SELECT
        StringBuilder sb = new StringBuilder();
        sb.append(generateSelectClause(TABLE_NAME, getColToAttributesMap(), true, clonedAttrs))
        // generate FROM
        .append(generateFromClause(clonedFilter));

        boolean filterFound = false;
        // generate WHERE
        // FILTER on RNASeqExperimentId
        if (clonedExpIds.isEmpty()) {
            sb.append(" WHERE " + TABLE_NAME + ".")
            .append(RNASeqExperimentDAO.Attribute.ID.getTOFieldName()).append(" IN (")
            .append(BgeePreparedStatement
                    .generateParameterizedQueryString(clonedExpIds.size()))
            .append(")");
            filterFound = true;
        }
        // FITER ON SPECIES IDS
        if (!clonedFilter.getSpeciesIds().isEmpty()) {
            if(filterFound) {
                sb.append(" AND ");
            }
            sb.append(CONDITION_TABLE_NAME).append(".")
            .append(RawDataConditionDAO.Attribute.SPECIES_ID.getTOFieldName()).append(" IN (")
            .append(BgeePreparedStatement.generateParameterizedQueryString(
                    clonedFilter.getSpeciesIds().size()))
            .append(")");
            filterFound = true;
        }
        // FILTER ON RAW CONDITIONS
        if (!clonedFilter.getConditionFilters().isEmpty()) {
            if(filterFound) {
                sb.append(" AND ");
            }
            sb.append(clonedFilter.getConditionFilters().stream()
                    .map(cf -> generateOneConditionFilter(cf))
                    .collect(Collectors.joining(" OR ", "(", ")")));
        }

      //add values to parameterized queries
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection()
                    .prepareStatement(sb.toString());
            int paramIndex = 1;
            if (!clonedExpIds.isEmpty()) {
                stmt.setStrings(paramIndex, clonedExpIds, true);
                paramIndex += clonedExpIds.size();
            }
            if (!clonedFilter.getSpeciesIds().isEmpty()) {
                stmt.setIntegers(paramIndex, clonedFilter.getSpeciesIds(), true);
                paramIndex += clonedFilter.getSpeciesIds().size();
            }
            configureRawDataConditionFiltersStmt(stmt, clonedFilter.getConditionFilters(),
                    paramIndex);
            return log.traceExit(new MySQLRNASeqExperimentTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }
    
    private static int configureRawDataConditionFiltersStmt(BgeePreparedStatement stmt,
            Collection<DAORawDataConditionFilter> conditionFilters, int paramIndex)
                    throws SQLException {
        log.traceEntry("{}, {}, {}", stmt, conditionFilters, paramIndex);

        int offsetParamIndex = paramIndex;
        for (DAORawDataConditionFilter condFilter: conditionFilters) {

            if (!condFilter.getAnatEntityIds().isEmpty()) {
                stmt.setStrings(offsetParamIndex, condFilter.getAnatEntityIds(), true);
                offsetParamIndex += condFilter.getAnatEntityIds().size();
            }
            if (!condFilter.getDevStageIds().isEmpty()) {
                stmt.setStrings(offsetParamIndex, condFilter.getDevStageIds(), true);
                offsetParamIndex += condFilter.getDevStageIds().size();
            }
            if (!condFilter.getCellTypeIds().isEmpty()) {
                stmt.setStrings(offsetParamIndex, condFilter.getCellTypeIds(), true);
                offsetParamIndex += condFilter.getCellTypeIds().size();
            }
            if (!condFilter.getSexIds().isEmpty()) {
                stmt.setStrings(offsetParamIndex, condFilter.getSexIds(), true);
                offsetParamIndex += condFilter.getSexIds().size();
            }
            if (!condFilter.getStrainIds().isEmpty()) {
                stmt.setStrings(offsetParamIndex, condFilter.getStrainIds(), true);
                offsetParamIndex += condFilter.getStrainIds().size();
            }
        }
        return log.traceExit(offsetParamIndex);
    }

    private String generateOneConditionFilter(DAORawDataConditionFilter condFilter) {
        log.traceEntry("{}", condFilter);
        StringBuilder sb = new StringBuilder();
        boolean previousCond = false;
        if (!condFilter.getAnatEntityIds().isEmpty()) {
            sb.append(generateOneConditionParameterWhereClause(
                    RawDataConditionDAO.Attribute.ANAT_ENTITY_ID,
                    condFilter.getAnatEntityIds(), previousCond));
            previousCond = true;
        }
        if (!condFilter.getDevStageIds().isEmpty()) {
            sb.append(generateOneConditionParameterWhereClause(
                    RawDataConditionDAO.Attribute.STAGE_ID,
                    condFilter.getDevStageIds(), previousCond));
            previousCond = true;
        }
        if (!condFilter.getCellTypeIds().isEmpty()) {
            sb.append(generateOneConditionParameterWhereClause(
                    RawDataConditionDAO.Attribute.CELL_TYPE_ID,
                    condFilter.getCellTypeIds(), previousCond));
            previousCond = true;
        }
        if (!condFilter.getSexIds().isEmpty()) {
            sb.append(generateOneConditionParameterWhereClause(
                    RawDataConditionDAO.Attribute.SEX,
                    condFilter.getSexIds(), previousCond));
            previousCond = true;
        }
        if (!condFilter.getStrainIds().isEmpty()) {
            sb.append(generateOneConditionParameterWhereClause(
                    RawDataConditionDAO.Attribute.STRAIN,
                    condFilter.getStrainIds(), previousCond));
        }
        return log.traceExit(sb.toString());
    }
    
    private String generateOneConditionParameterWhereClause(RawDataConditionDAO.Attribute attr,
            Set<String> condValues, boolean previousFilter) {
        log.traceEntry("{}, {}, {}", attr, condValues, previousFilter);
        StringBuffer sb = new StringBuffer();
        if(previousFilter) {
            sb.append(" AND ");
        }
        sb.append(CONDITION_TABLE_NAME).append(".")
        .append(attr.getTOFieldName()).append(" IN (")
        .append(BgeePreparedStatement.generateParameterizedQueryString(condValues.size()))
        .append(")");
        return log.traceExit(sb.toString());
        
    }

    //TODO update classes of Attributes used for column names. It is not yet possible to use them as
    // we changed the schema of RNASeq tables to fit all RNASeq protocols. The question behind is
    // should we create a class RNASeqLibraryAnnotatedSampleDAO with its own attributes or should we
    // query both rnaSeqLibrary and rnaSeqLibraryAnnotatedSample tables in the same class using the same
    // TO and containing Attribute for all fields of both tables.
    // Attributes 
    private String generateFromClause(DAORawDataFilter rawDataFilter) {
        log.traceEntry("{}", rawDataFilter);
        StringBuilder sb = new StringBuilder();
        sb.append(" FROM " + TABLE_NAME + " ");
        if(!rawDataFilter.getSpeciesIds().isEmpty() 
                || !rawDataFilter.getConditionFilters().isEmpty() ) {
            //join on rnaSeqLibrary table
            sb.append("INNER JOIN " + LIB_TABLE_NAME + " ON ")
            .append(TABLE_NAME + "." + RNASeqExperimentDAO.Attribute.ID.getTOFieldName())
            .append(" = " + LIB_TABLE_NAME + "." + RNASeqLibraryDAO.Attribute.EXPERIMENT_ID
                    .getTOFieldName())
            //join on rnaSeqLibraryAnnotatedSample
            .append(" INNER JOIN " + ANNOTATED_LIB_TABLE_NAME + " ON ")
            .append(LIB_TABLE_NAME + "." + RNASeqLibraryDAO.Attribute.ID.getTOFieldName())
            .append(" = " + ANNOTATED_LIB_TABLE_NAME + "." + RNASeqLibraryDAO.Attribute.ID
                    .getTOFieldName())
            //join on cond table
            .append(" INNER JOIN " + CONDITION_TABLE_NAME + " ON ")
            .append(CONDITION_TABLE_NAME + "." + RawDataConditionDAO.Attribute.ID.getTOFieldName())
            .append(" = " + ANNOTATED_LIB_TABLE_NAME + "." + RawDataConditionDAO.Attribute.ID
                    .getTOFieldName());
        }
        return log.traceExit(sb.toString());
    }

    /**
     * Get a {@code Map} associating column names to corresponding 
     * {@code RNASeqExperimentDAO.Attribute}.
     * 
     * @return          A {@code Map} where keys are {@code String}s that are column names, 
     *                  the associated value being the corresponding 
     *                  {@code RNASeqExperimentDAO.Attribute}.
     */
    private static Map<String, RNASeqExperimentDAO.Attribute> getColToAttributesMap() {
        log.traceEntry();
        return log.traceExit(EnumSet.allOf(RNASeqExperimentDAO.Attribute.class).stream()
                .collect(Collectors.toMap(a -> a.getTOFieldName(), a -> a)));
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
