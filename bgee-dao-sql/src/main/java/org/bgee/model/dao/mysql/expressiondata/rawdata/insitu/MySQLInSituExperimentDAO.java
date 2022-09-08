package org.bgee.model.dao.mysql.expressiondata.rawdata.insitu;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituEvidenceDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituExperimentDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituSpotDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
import org.bgee.model.dao.mysql.expressiondata.rawdata.MySQLRawDataDAO;

public class MySQLInSituExperimentDAO extends MySQLRawDataDAO<InSituExperimentDAO.Attribute> 
implements InSituExperimentDAO{

    private final static Logger log = LogManager.getLogger(MySQLInSituExperimentDAO.class.getName());
    public final static String TABLE_NAME = "inSituExperiment";
    private final static String EVIDENCE_TABLE_NAME = MySQLInSituEvidenceDAO.TABLE_NAME;
    private final static String SPOT_TABLE_NAME = MySQLInSituSpotDAO.TABLE_NAME;

    public MySQLInSituExperimentDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public InSituExperimentTOResultSet getExperimentsFromIds(Collection<String> experimentIds,
            Collection<InSituExperimentDAO.Attribute> attrs) {
        log.traceEntry("{}", experimentIds);
        return log.traceExit(getExperiments(experimentIds, null, attrs));
    }
        
    @Override
    public InSituExperimentTOResultSet getExperiments(Collection<String> experimentIds,
            DAORawDataFilter filter, Collection<InSituExperimentDAO.Attribute> attrs) {
            log.traceEntry("{}", experimentIds);
        final Set<String> clonedExpIds = Collections.unmodifiableSet(experimentIds == null?
                new HashSet<String>(): new HashSet<String>(experimentIds));
        final DAORawDataFilter clonedFilter = new DAORawDataFilter(filter);
        final Set<InSituExperimentDAO.Attribute> clonedAttrs = 
                Collections.unmodifiableSet(attrs == null? new HashSet<>(): new HashSet<>(attrs));

        // generate SELECT
        StringBuilder sb = new StringBuilder();
        sb.append(generateSelectClause(TABLE_NAME, getColToAttributesMap(InSituExperimentDAO
                .Attribute.class), true, clonedAttrs))
        // generate FROM
        .append(generateFromClause(clonedFilter));

        // generate WHERE
        if(!clonedExpIds.isEmpty() || !clonedFilter.getSpeciesIds().isEmpty()
                || !clonedFilter.getGeneIds().isEmpty()
                || !clonedFilter.getConditionFilters().isEmpty()) {
            sb.append(" WHERE ");
        }
        boolean foundFilter = false;
        // FILTER on inSituExperimentId
        if (!clonedExpIds.isEmpty()) {
            sb.append(TABLE_NAME + ".")
            .append(InSituExperimentDAO.Attribute.ID.getTOFieldName()).append(" IN (")
            .append(BgeePreparedStatement
                    .generateParameterizedQueryString(clonedExpIds.size()))
            .append(")");
            foundFilter = true;
        }
        // FILTER on species IDs
        if (!clonedFilter.getSpeciesIds().isEmpty()) {
            if (foundFilter) {
                sb.append(" AND ");
            }
            sb.append(CONDITION_TABLE_NAME + ".")
            .append(RawDataConditionDAO.Attribute.SPECIES_ID.getTOFieldName()).append(" IN (")
            .append(BgeePreparedStatement
                    .generateParameterizedQueryString(clonedFilter.getSpeciesIds().size()))
            .append(")");
            foundFilter = true;
        }
        // FILTER on gene IDs
        if (!clonedFilter.getGeneIds().isEmpty()) {
            if (foundFilter) {
                sb.append(" AND ");
            }
            sb.append(SPOT_TABLE_NAME + ".")
            .append(InSituSpotDAO.Attribute.BGEE_GENE_ID.getTOFieldName()).append(" IN (")
            .append(BgeePreparedStatement
                    .generateParameterizedQueryString(clonedFilter.getGeneIds().size()))
            .append(")");
            foundFilter = true;
        }
        // FILTER on raw conditions
        if (!clonedFilter.getConditionFilters().isEmpty()) {
            if(foundFilter) {
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
            if (!clonedFilter.getGeneIds().isEmpty()) {
                stmt.setIntegers(paramIndex, clonedFilter.getGeneIds(), true);
                paramIndex += clonedFilter.getGeneIds().size();
            }
            configureRawDataConditionFiltersStmt(stmt, clonedFilter.getConditionFilters(),
                    paramIndex);
            return log.traceExit(new MySQLInSituExperimentTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    private String generateFromClause(DAORawDataFilter filter) {
        log.traceEntry("{}", filter);
        StringBuilder sb = new StringBuilder();
        sb.append(" FROM " + TABLE_NAME);
        // join inSituEvidence table
        if(!filter.getGeneIds() .isEmpty() || !filter.getSpeciesIds() .isEmpty()
                || !filter.getConditionFilters().isEmpty()) {
            sb.append(" INNER JOIN " + EVIDENCE_TABLE_NAME + " ON ")
            .append(TABLE_NAME + "." + InSituExperimentDAO.Attribute.ID
                    .getTOFieldName())
            .append(" = " + EVIDENCE_TABLE_NAME + "." 
                    + InSituEvidenceDAO.Attribute.EXPERIMENT_ID.getTOFieldName());
         // join inSituSpot table
            sb.append(" INNER JOIN " + SPOT_TABLE_NAME + " ON ")
            .append(EVIDENCE_TABLE_NAME + "." + InSituEvidenceDAO.Attribute.IN_SITU_EVIDENCE_ID
                    .getTOFieldName())
            .append(" = " + SPOT_TABLE_NAME + "." 
                   + InSituSpotDAO.Attribute.IN_SITU_EVIDENCE_ID.getTOFieldName());
        }
            
        if(!filter.getSpeciesIds() .isEmpty() || !filter.getConditionFilters().isEmpty()) {
            sb.append(" INNER JOIN " + CONDITION_TABLE_NAME + " ON ")
            .append(SPOT_TABLE_NAME + "." + InSituSpotDAO.Attribute.CONDITION_ID
                    .getTOFieldName())
            .append(" = " + CONDITION_TABLE_NAME + "." 
                    + RawDataConditionDAO.Attribute.ID.getTOFieldName());
        }
        return log.traceExit(sb.toString());
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
