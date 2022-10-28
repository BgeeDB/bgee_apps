package org.bgee.model.dao.mysql.expressiondata.rawdata.insitu;

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
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituEvidenceDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituSpotDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
import org.bgee.model.dao.mysql.expressiondata.rawdata.MySQLRawDataDAO;

public class MySQLInSituEvidenceDAO extends MySQLRawDataDAO<InSituEvidenceDAO.Attribute> 
implements InSituEvidenceDAO{

    private final static Logger log = LogManager.getLogger(MySQLInSituEvidenceDAO.class.getName());
    public final static String TABLE_NAME = "inSituEvidence";
    private final static String INSITU_SPOT_TABLE_NAME = MySQLInSituSpotDAO.TABLE_NAME;

    public MySQLInSituEvidenceDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public InSituEvidenceTOResultSet getInSituEvidenceFromExperimentIds(Collection<String> experimentIds,
            Collection<InSituEvidenceDAO.Attribute> attrs) {
        log.traceEntry("{}, {}", experimentIds, attrs);
        return log.traceExit(getInSituEvidences(null, experimentIds, null, attrs));
    }

    @Override
    public InSituEvidenceTOResultSet getInSituEvidenceFromIds(Collection<String> evidenceIds,
            Collection<InSituEvidenceDAO.Attribute> attrs) {
        log.traceEntry("{}, {}", evidenceIds, attrs);
        return log.traceExit(getInSituEvidences(evidenceIds, null, null, attrs));
    }

    @Override
    public InSituEvidenceTOResultSet getInSituEvidenceFromRawDataFilter(DAORawDataFilter filter,
            Collection<InSituEvidenceDAO.Attribute> attrs) {
        log.traceEntry("{}, {}", filter, attrs);
        return log.traceExit(getInSituEvidences(null, null, filter, attrs));
    }

    @Override
    public InSituEvidenceTOResultSet getInSituEvidences(Collection<String> evidenceIds,
            Collection<String> experimentIds, DAORawDataFilter filter,
            Collection<InSituEvidenceDAO.Attribute> attrs) {
        log.traceEntry("{}, {}, {}, {}", evidenceIds, experimentIds, filter, attrs);
//        final Set<String> clonedEvidenceIds = Collections.unmodifiableSet(evidenceIds == null?
//                new HashSet<String>(): new HashSet<String>(evidenceIds));
//        final Set<String> clonedExpIds = Collections.unmodifiableSet(experimentIds == null?
//                new HashSet<String>(): new HashSet<String>(experimentIds));
//        final DAORawDataFilter clonedFilter = new DAORawDataFilter(filter);
//        final Set<InSituEvidenceDAO.Attribute> clonedAttrs = Collections.unmodifiableSet(attrs == null?
//                new HashSet<InSituEvidenceDAO.Attribute>():
//                    new HashSet<InSituEvidenceDAO.Attribute>(attrs));
     // generate SELECT
        StringBuilder sb = new StringBuilder();
//        sb.append(generateSelectClause(TABLE_NAME, getColToAttributesMap(InSituEvidenceDAO
//                .Attribute.class), true, clonedAttrs))
//        // generate FROM
//        .append(generateFromClause(clonedFilter));
//
//        // generate WHERE
//        if (!clonedEvidenceIds.isEmpty() || !clonedExpIds.isEmpty() || !clonedFilter.getGeneIds().isEmpty()
//                || clonedFilter.getGeneIds().isEmpty() || clonedFilter.getConditionFilters().isEmpty()) {
//            sb.append(" WHERE ");
//        }
//        boolean filteredBefore = false;
//        // FILTER on evidence Id
//        if (!clonedEvidenceIds.isEmpty()) {
//            sb.append(TABLE_NAME + ".")
//            .append(InSituEvidenceDAO.Attribute.IN_SITU_EVIDENCE_ID.getTOFieldName()).append(" IN (")
//            .append(BgeePreparedStatement
//                    .generateParameterizedQueryString(clonedEvidenceIds.size()))
//            .append(")");
//            filteredBefore = true;
//        }
//        // FILTER on experiment Id
//        if (!clonedExpIds.isEmpty()) {
//            if(filteredBefore) {
//                sb.append(" AND ");
//            }
//            sb.append(TABLE_NAME + ".")
//            .append(InSituEvidenceDAO.Attribute.EXPERIMENT_ID.getTOFieldName()).append(" IN (")
//            .append(BgeePreparedStatement
//                    .generateParameterizedQueryString(clonedExpIds.size()))
//            .append(")");
//            filteredBefore = true;
//        }
//        // FILTER on species Ids
//        if (!clonedFilter.getSpeciesIds().isEmpty()) {
//            if(filteredBefore) {
//                sb.append(" AND ");
//            }
//            sb.append(CONDITION_TABLE_NAME + ".")
//            .append(RawDataConditionDAO.Attribute.SPECIES_ID.getTOFieldName()).append(" IN (")
//            .append(BgeePreparedStatement
//                    .generateParameterizedQueryString(clonedFilter.getSpeciesIds().size()))
//            .append(")");
//            filteredBefore = true;
//        }
//        // FILTER on gene Ids
//        if (!clonedFilter.getGeneIds().isEmpty()) {
//            if(filteredBefore) {
//                sb.append(" AND ");
//            }
//            sb.append(INSITU_SPOT_TABLE_NAME + ".")
//            .append(InSituSpotDAO.Attribute.BGEE_GENE_ID.getTOFieldName()).append(" IN (")
//            .append(BgeePreparedStatement
//                    .generateParameterizedQueryString(clonedFilter.getGeneIds().size()))
//            .append(")");
//            filteredBefore = true;
//        }
//        // FILTER on raw conditions
//        if (!clonedFilter.getConditionFilters().isEmpty()) {
//            if(filteredBefore) {
//                sb.append(" AND ");
//            }
//            sb.append(clonedFilter.getConditionFilters().stream()
//                    .map(cf -> generateOneConditionFilter(cf))
//                    .collect(Collectors.joining(" OR ", "(", ")")));
//        }
        //add values to parameterized queries
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection()
                    .prepareStatement(sb.toString());
//            int paramIndex = 1;
//            if (!clonedEvidenceIds.isEmpty()) {
//                stmt.setStrings(paramIndex, clonedEvidenceIds, true);
//                paramIndex += clonedEvidenceIds.size();
//            }
//            if (!clonedExpIds.isEmpty()) {
//                stmt.setStrings(paramIndex, clonedExpIds, true);
//                paramIndex += clonedEvidenceIds.size();
//            }
//            if (!clonedFilter.getSpeciesIds().isEmpty()) {
//                stmt.setIntegers(paramIndex, clonedFilter.getSpeciesIds(), true);
//                paramIndex += clonedFilter.getSpeciesIds().size();
//            }
//            if (!clonedFilter.getGeneIds().isEmpty()) {
//                stmt.setIntegers(paramIndex, clonedFilter.getGeneIds(), true);
//                paramIndex += clonedFilter.getGeneIds().size();
//            }
//            configureRawDataConditionFiltersStmt(stmt, clonedFilter.getConditionFilters(),
//                    paramIndex);
            return log.traceExit(new MySQLInSituEvidenceTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

//    private String generateFromClause(DAORawDataFilter filter) {
//        log.traceEntry("{}", filter);
//        StringBuilder sb = new StringBuilder();
//        sb.append(" FROM " + TABLE_NAME);
//        // join inSituSpot table
//        if(!filter.getGeneIds() .isEmpty() || !filter.getSpeciesIds() .isEmpty()
//                || !filter.getConditionFilters().isEmpty()) {
//            sb.append(" INNER JOIN " + INSITU_SPOT_TABLE_NAME + " ON ")
//            .append(TABLE_NAME + "." + InSituEvidenceDAO.Attribute.IN_SITU_EVIDENCE_ID
//                    .getTOFieldName())
//            .append(" = " + INSITU_SPOT_TABLE_NAME + "." 
//                    + InSituSpotDAO.Attribute.IN_SITU_EVIDENCE_ID.getTOFieldName());
//        }
//        // join cond table
//        if(!filter.getSpeciesIds() .isEmpty() || !filter.getConditionFilters().isEmpty()) {
//            sb.append(" INNER JOIN " + CONDITION_TABLE_NAME + " ON ")
//            .append(INSITU_SPOT_TABLE_NAME + "." + InSituSpotDAO.Attribute.CONDITION_ID
//                    .getTOFieldName())
//            .append(" = " + CONDITION_TABLE_NAME + "." 
//                    + RawDataConditionDAO.Attribute.ID.getTOFieldName());
//        }
//        return log.traceExit(sb.toString());
//    }

    class MySQLInSituEvidenceTOResultSet extends MySQLDAOResultSet<InSituEvidenceTO> 
    implements InSituEvidenceTOResultSet{

        /**
         * @param statement The {@code BgeePreparedStatement}
         */
        private MySQLInSituEvidenceTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected InSituEvidenceDAO.InSituEvidenceTO getNewTO() throws DAOException {
            log.traceEntry();
            try {
                final ResultSet currentResultSet = this.getCurrentResultSet();
                Boolean distinguishable = null;
                String id = null, experimentId = null, evidenceUrlPath= null;

                for (Entry<Integer, String> column : this.getColumnLabels().entrySet()) {
                    if (column.getValue().equals(InSituEvidenceDAO.Attribute.IN_SITU_EVIDENCE_ID.getTOFieldName())) {
                        id = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(InSituEvidenceDAO.Attribute.EXPERIMENT_ID
                            .getTOFieldName())) {
                        experimentId = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(InSituEvidenceDAO.Attribute.EVIDENCE_DISTINGUISHABLE
                            .getTOFieldName())) {
                        distinguishable = currentResultSet.getBoolean(column.getKey());
                    } else if(column.getValue().equals(InSituEvidenceDAO.Attribute.EVIDENCE_URL_PART
                            .getTOFieldName())) {
                        evidenceUrlPath = currentResultSet.getString(column.getKey());
                    } else {
                        log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }
                }
                return log.traceExit(new InSituEvidenceTO(id, experimentId, distinguishable,
                        evidenceUrlPath));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }

}
