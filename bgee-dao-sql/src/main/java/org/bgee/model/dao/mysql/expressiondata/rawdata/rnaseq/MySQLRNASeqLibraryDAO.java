package org.bgee.model.dao.mysql.expressiondata.rawdata.rnaseq;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryAnnotatedSampleDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
import org.bgee.model.dao.mysql.expressiondata.rawdata.MySQLRawDataDAO;

// For RNASeq Library queries we not filter on genes as all genes should have a processed
// expression value for 
public class MySQLRNASeqLibraryDAO extends MySQLRawDataDAO<RNASeqLibraryDAO.Attribute> 
        implements RNASeqLibraryDAO{

    private final static Logger log = LogManager.getLogger(MySQLRNASeqLibraryDAO.class.getName());
    private final static String TABLE_NAME = "rnaSeqLibrary";
    private final static String LIBRARY_ANNOTATED_TABLE_NAME = "rnaSeqLibraryAnnotatedSample";

    public MySQLRNASeqLibraryDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public RNASeqLibraryTOResultSet getRnaSeqLibraryFromExperimentIds(
            Collection<String> experimentIds, Collection<RNASeqLibraryDAO.Attribute> attrs)
            throws DAOException {
        log.traceEntry("{}, {}", experimentIds, attrs);
        return log.traceExit(getRnaSeqLibraries(null, experimentIds, null, attrs));
    }

    @Override
    public RNASeqLibraryTOResultSet getRnaSeqLibraryFromIds(Collection<String> libraryIds,
            Collection<RNASeqLibraryDAO.Attribute> attrs)
            throws DAOException {
        log.traceEntry("{}, {}", libraryIds, attrs);
        return log.traceExit(getRnaSeqLibraries(libraryIds, null, null, attrs));
    }

    @Override
    public RNASeqLibraryTOResultSet getRnaSeqLibraryFromRawDataFilter(DAORawDataFilter filter,
            Collection<RNASeqLibraryDAO.Attribute> attrs)
            throws DAOException {
        log.traceEntry("{}, {}", filter, attrs);
        return log.traceExit(getRnaSeqLibraries(null, null, filter, attrs));
    }

    @Override
    public RNASeqLibraryTOResultSet getRnaSeqLibraries(Collection<String> libraryIds,
            Collection<String> experimentIds, DAORawDataFilter filter,
            Collection<RNASeqLibraryDAO.Attribute> attrs)
            throws DAOException {
        log.traceEntry("{}, {}, {}", libraryIds, experimentIds, filter, attrs);
        final Set<String> clonedLibraryIds = Collections.unmodifiableSet(libraryIds == null?
                new HashSet<String>(): new HashSet<String>(libraryIds));
        final Set<String> clonedExperimentIds = Collections.unmodifiableSet(experimentIds == null?
                new HashSet<String>(): new HashSet<String>(experimentIds));
        final DAORawDataFilter clonedFilter = new DAORawDataFilter(filter);
        final Set<RNASeqLibraryDAO.Attribute> clonedAttrs = Collections.unmodifiableSet(attrs == null?
                new HashSet<>(): new HashSet<>(attrs));
        // generate SELECT
        StringBuilder sb = new StringBuilder();
        sb.append(generateSelectClause(TABLE_NAME, getColToAttributesMap(RNASeqLibraryDAO
                .Attribute.class), true, clonedAttrs))
        // generate FROM
        .append(generateFromClause(clonedFilter));
        //generate WHERE clause
        if(!clonedFilter.getSpeciesIds().isEmpty() || !clonedFilter.getConditionFilters().isEmpty()
                || !clonedLibraryIds.isEmpty() || !clonedExperimentIds.isEmpty()) {
            sb.append(" WHERE ");
        }
        boolean filteredBefore = false;
        //filter on libraryIds
        if (!clonedLibraryIds.isEmpty()) {
            sb.append(TABLE_NAME + ".")
            .append(RNASeqLibraryDAO.Attribute.ID.getTOFieldName()).append(" IN (")
            .append(BgeePreparedStatement
                    .generateParameterizedQueryString(clonedLibraryIds.size()))
            .append(")");
            filteredBefore = true;
        }
        // FILTER on experiment Id
        if (!clonedExperimentIds.isEmpty()) {
            if(filteredBefore) {
                sb.append(" AND ");
            }
            sb.append(TABLE_NAME + ".")
            .append(RNASeqLibraryDAO.Attribute.EXPERIMENT_ID.getTOFieldName()).append(" IN (")
            .append(BgeePreparedStatement
                    .generateParameterizedQueryString(clonedExperimentIds.size()))
            .append(")");
            filteredBefore = true;
        }
        // FILTER on speciesIds
        if (!clonedFilter.getSpeciesIds().isEmpty()) {
            if(filteredBefore) {
                sb.append(" AND ");
            }
            sb.append(CONDITION_TABLE_NAME + ".")
            .append(RawDataConditionDAO.Attribute.SPECIES_ID.getTOFieldName() + " IN (")
            .append(BgeePreparedStatement
                    .generateParameterizedQueryString(clonedFilter.getSpeciesIds().size()))
            .append(")");
            filteredBefore = true;
        }
     // FILTER on raw conditions
        if (!clonedFilter.getConditionFilters().isEmpty()) {
            if(filteredBefore) {
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
            if (!clonedLibraryIds.isEmpty()) {
                stmt.setStrings(paramIndex, clonedLibraryIds, true);
                paramIndex += clonedLibraryIds.size();
            }
            if (!clonedExperimentIds.isEmpty()) {
                stmt.setStrings(paramIndex, clonedExperimentIds, true);
                paramIndex += clonedExperimentIds.size();
            }
            if (!clonedFilter.getSpeciesIds().isEmpty()) {
                stmt.setIntegers(paramIndex, clonedFilter.getSpeciesIds(), true);
                paramIndex += clonedFilter.getSpeciesIds().size();
            }
            configureRawDataConditionFiltersStmt(stmt, clonedFilter.getConditionFilters(),
                    paramIndex);
            return log.traceExit(new MySQLRNASeqLibraryTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    private String generateFromClause(DAORawDataFilter filter) {
        log.traceEntry("{}", filter);
        StringBuilder sb = new StringBuilder();
        sb.append(" FROM " + TABLE_NAME);
        // join inSituSpot table
        if(!filter.getSpeciesIds() .isEmpty() || !filter.getConditionFilters().isEmpty()) {
            // join on rnaSeqLibraryAnnotatedSample table
            sb.append(" INNER JOIN " + LIBRARY_ANNOTATED_TABLE_NAME + " ON ")
            .append(TABLE_NAME + "." + RNASeqLibraryDAO.Attribute.ID.getTOFieldName())
            .append(" = " + LIBRARY_ANNOTATED_TABLE_NAME + "." 
                    + RNASeqLibraryAnnotatedSampleDAO.Attribute.RNASEQ_LIBRARY_ID
                    .getTOFieldName());
            //join on cond table
            sb.append(" INNER JOIN " + CONDITION_TABLE_NAME + " ON ")
            .append(LIBRARY_ANNOTATED_TABLE_NAME + "." + RNASeqLibraryAnnotatedSampleDAO
                    .Attribute.CONDITION_ID.getTOFieldName())
            .append(" = " + CONDITION_TABLE_NAME + "." + RawDataConditionDAO.Attribute.ID
                    .getTOFieldName());
        }
        return log.traceExit(sb.toString());
    }

    class MySQLRNASeqLibraryTOResultSet extends MySQLDAOResultSet<RNASeqLibraryTO> 
    implements RNASeqLibraryTOResultSet{

        /**
         * @param statement The {@code BgeePreparedStatement}
         */
        private MySQLRNASeqLibraryTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected RNASeqLibraryDAO.RNASeqLibraryTO getNewTO() throws DAOException {
            log.traceEntry();
            try {
                final ResultSet currentResultSet = this.getCurrentResultSet();
                Boolean sampleMultiplexing = null, libraryMultiplexing = null;
                String id = null, experimentId = null, platformId= null;

                for (Entry<Integer, String> column : this.getColumnLabels().entrySet()) {
                    if (column.getValue().equals(RNASeqLibraryDAO.Attribute.ID.getTOFieldName())) {
                        id = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryDAO.Attribute.EXPERIMENT_ID
                            .getTOFieldName())) {
                        experimentId = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryDAO.Attribute.PLATFORM_ID
                            .getTOFieldName())) {
                        platformId = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryDAO.Attribute.SAMPLE_MULTIPLEXING
                            .getTOFieldName())) {
                        sampleMultiplexing = currentResultSet.getBoolean(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryDAO.Attribute.LIBRARY_MULTIPLEXING
                            .getTOFieldName())) {
                        libraryMultiplexing = currentResultSet.getBoolean(column.getKey());
                    } else {
                        log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }
                }
                return log.traceExit(new RNASeqLibraryTO(id, experimentId, platformId, 
                        sampleMultiplexing, libraryMultiplexing));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }
}
