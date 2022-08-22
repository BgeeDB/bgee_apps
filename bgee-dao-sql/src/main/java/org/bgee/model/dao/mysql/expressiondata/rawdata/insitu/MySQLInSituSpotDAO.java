package org.bgee.model.dao.mysql.expressiondata.rawdata.insitu;

import java.math.BigDecimal;
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
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCallSourceDAO.CallSourceDataTO.ExclusionReason;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituEvidenceDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituSpotDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
import org.bgee.model.dao.mysql.expressiondata.rawdata.MySQLRawDataDAO;


public class MySQLInSituSpotDAO extends MySQLRawDataDAO<InSituSpotDAO.Attribute>
        implements InSituSpotDAO {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(MySQLInSituSpotDAO.class.getName());
    private final static String TABLE_NAME = "inSituSpot";
    private final static String EVIDENCE_TABLE_NAME = "inSituEvidence";

    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     * 
     * @param manager                       The {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException     If {@code manager} is {@code null}.
     */
    public MySQLInSituSpotDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public InSituSpotTOResultSet getInSituSpots(Collection<String> spotIds,
            Collection<String> evidenceIds, Collection<String> experimentIds,
            DAORawDataFilter rawDataFilter, Collection<InSituSpotDAO.Attribute> attrs) {
        log.traceEntry("{}, {}, {}, {}, {}", spotIds, evidenceIds, experimentIds,
                rawDataFilter, attrs);
        final Set<String> clonedSpotIds = Collections.unmodifiableSet(spotIds == null?
                new HashSet<String>(): new HashSet<String>(spotIds));
        final Set<String> clonedEvidenceIds = Collections.unmodifiableSet(evidenceIds == null?
                new HashSet<String>(): new HashSet<String>(evidenceIds));
        final Set<String> clonedExperimentIds = Collections.unmodifiableSet(experimentIds == null?
                new HashSet<String>(): new HashSet<String>(experimentIds));
        DAORawDataFilter clonedFilter = new DAORawDataFilter(rawDataFilter);
        final Set<InSituSpotDAO.Attribute> clonedAttrs = Collections.unmodifiableSet(attrs == null?
                new HashSet<InSituSpotDAO.Attribute>():
                    new HashSet<InSituSpotDAO.Attribute>(attrs));

        if (clonedSpotIds.isEmpty() && clonedEvidenceIds.isEmpty() && clonedExperimentIds.isEmpty()
                && clonedFilter.getConditionFilters().isEmpty() && clonedFilter.getGeneIds().isEmpty()
                && clonedFilter.getSpeciesIds().isEmpty()) {
            throw log.throwing(new IllegalArgumentException("At least a species ID, "
                    + "a bgee gene ID, an Insitu Spot ID, an InSitu evidence ID, an InSitu experiment"
                    + " ID or raw condition filter should be provided"));
        }
        // generate SELECT
        StringBuilder sb = new StringBuilder();
        sb.append(generateSelectClause(TABLE_NAME, getColToAttributesMap(InSituSpotDAO
                .Attribute.class), true, clonedAttrs))
        // generate FROM
        .append(generateFromClause(clonedExperimentIds, clonedFilter));

     // generate WHERE CLAUSE
        if (!clonedSpotIds.isEmpty() || !clonedEvidenceIds.isEmpty() ||
                !clonedFilter.getConditionFilters().isEmpty() || !clonedFilter.getGeneIds().isEmpty()
                || !clonedFilter.getSpeciesIds().isEmpty()) {
          sb.append(" WHERE ");
        }
        // FITER ON SPOT IDS
        boolean filteredAlready = false;
        if (!clonedSpotIds.isEmpty()) {
            sb.append(TABLE_NAME).append(".")
            .append(InSituSpotDAO.Attribute.ID.getTOFieldName()).append(" IN (")
            .append(BgeePreparedStatement.generateParameterizedQueryString(clonedSpotIds.size()))
            .append(")");
            filteredAlready = true;
        }
        // FITER ON EVIDENCE IDS
        if (!clonedEvidenceIds.isEmpty()) {
            if (filteredAlready) {
                sb.append(" AND ");
            }
            sb.append(TABLE_NAME).append(".")
            .append(InSituSpotDAO.Attribute.IN_SITU_EVIDENCE_ID.getTOFieldName()).append(" IN (")
            .append(BgeePreparedStatement.generateParameterizedQueryString(clonedEvidenceIds.size()))
            .append(")");
            filteredAlready = true;
        }
        // FITER ON EXPERIMENT IDS
        if (!clonedExperimentIds.isEmpty()) {
            if (filteredAlready) {
                sb.append(" AND ");
            }
            sb.append(EVIDENCE_TABLE_NAME).append(".")
            .append(InSituEvidenceDAO.Attribute.EXPERIMENT_ID.getTOFieldName()).append(" IN (")
            .append(BgeePreparedStatement.generateParameterizedQueryString(clonedExperimentIds
                    .size()))
            .append(")");
            filteredAlready = true;
        }
        // FITER ON SPECIES IDS
        if (!clonedFilter.getSpeciesIds().isEmpty()) {
            if (filteredAlready) {
                sb.append(" AND ");
            }
            sb.append(CONDITION_TABLE_NAME).append(".")
            .append(RawDataConditionDAO.Attribute.SPECIES_ID.getTOFieldName()).append(" IN (")
            .append(BgeePreparedStatement.generateParameterizedQueryString(clonedFilter
                    .getSpeciesIds().size()))
            .append(")");
            filteredAlready = true;
        }
        // FITER ON GENE IDS
        if (!clonedFilter.getGeneIds().isEmpty()) {
            if (filteredAlready) {
                sb.append(" AND ");
            }
            sb.append(TABLE_NAME).append(".")
            .append(InSituSpotDAO.Attribute.BGEE_GENE_ID.getTOFieldName()).append(" IN (")
            .append(BgeePreparedStatement.generateParameterizedQueryString(clonedFilter
                    .getSpeciesIds().size()))
            .append(")");
            filteredAlready = true;
        }
        // FILTER ON RAW DATA (genes/species/conditions)
        if (!clonedFilter.getConditionFilters().isEmpty()) {
            if (!filteredAlready) {
                sb.append(" AND ");
            }
            sb.append(clonedFilter.getConditionFilters()
                    .stream().map(cf -> generateOneConditionFilter(cf))
                    .collect(Collectors.joining(" OR ", "(", ")")));
        }
      //add values to parameterized queries
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sb.toString());
            int paramIndex = 1;
            if (!clonedSpotIds.isEmpty()) {
                stmt.setStrings(paramIndex, clonedSpotIds, true);
                paramIndex += clonedSpotIds.size();
            }
            if (!clonedEvidenceIds.isEmpty()) {
                stmt.setStrings(paramIndex, clonedEvidenceIds, true);
                paramIndex += clonedEvidenceIds.size();
            }
            if (!clonedExperimentIds.isEmpty()) {
                stmt.setStrings(paramIndex, clonedExperimentIds, true);
                paramIndex += clonedExperimentIds.size();
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
            return log.traceExit(new MySQLInSituSpotTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    private String generateFromClause(Collection<String> experimentIds,
            DAORawDataFilter rawDataFilter) {
        log.traceEntry("{}, {}", experimentIds, rawDataFilter);
        StringBuilder sb = new StringBuilder();
        sb.append(" FROM " + TABLE_NAME);
        // if at least one speciesId or one filter on condition
        if(!rawDataFilter.getSpeciesIds().isEmpty() ||
                !rawDataFilter.getConditionFilters().isEmpty()) {
            sb.append(" INNER JOIN " + CONDITION_TABLE_NAME + " ON ")
            .append(TABLE_NAME + "." + InSituSpotDAO.Attribute.CONDITION_ID.getTOFieldName())
            .append(" = " + CONDITION_TABLE_NAME + "."
                    + RawDataConditionDAO.Attribute.ID.getTOFieldName());
        }
        if(experimentIds.isEmpty()) {
            sb.append(" INNER JOIN " + EVIDENCE_TABLE_NAME + " ON ")
            .append(TABLE_NAME + "." + InSituEvidenceDAO.Attribute.EXPERIMENT_ID
                    .getTOFieldName())
            .append(" = " + CONDITION_TABLE_NAME + "."
                    + RawDataConditionDAO.Attribute.ID.getTOFieldName());
        }
        return log.traceExit(sb.toString());
    }

    class MySQLInSituSpotTOResultSet extends MySQLDAOResultSet<InSituSpotTO>
    implements InSituSpotTOResultSet{
        /**
         * @param statement The {@code BgeePreparedStatement}
         */
        private MySQLInSituSpotTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected InSituSpotDAO.InSituSpotTO getNewTO() throws DAOException {
            log.traceEntry();
            try {
                final ResultSet currentResultSet = this.getCurrentResultSet();
                String inSituSpotId = null, inSituEvidenceId = null, inSituExpressionPatternId = null;
                String inSituData = null, reasonForExclusion = null;
                Integer conditionId = null, bgeeGeneId = null;
                Long expressionId = null;
                BigDecimal pValue = null;

                for (Entry<Integer, String> column : this.getColumnLabels().entrySet()) {
                    if (column.getValue().equals(InSituSpotDAO.Attribute.ID.getTOFieldName())) {
                        inSituSpotId = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(InSituSpotDAO.Attribute.IN_SITU_EVIDENCE_ID
                            .getTOFieldName())) {
                        inSituEvidenceId = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(InSituSpotDAO.Attribute
                            .IN_SITU_EXPRESSION_PATTERN_ID.getTOFieldName())) {
                        inSituExpressionPatternId = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(InSituSpotDAO.Attribute.CONDITION_ID
                            .getTOFieldName())) {
                        conditionId = currentResultSet.getInt(column.getKey());
                    } else if(column.getValue().equals(InSituSpotDAO.Attribute.BGEE_GENE_ID
                            .getTOFieldName())) {
                        bgeeGeneId = currentResultSet.getInt(column.getKey());
                    } else if(column.getValue().equals(InSituSpotDAO.Attribute.EXPRESSION_ID
                            .getTOFieldName())) {
                        expressionId = currentResultSet.getLong(column.getKey());
                    } else if(column.getValue().equals(InSituSpotDAO.Attribute.IN_SITU_DATA
                            .getTOFieldName())) {
                        inSituData = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(InSituSpotDAO.Attribute.PVALUE
                            .getTOFieldName())) {
                        pValue = currentResultSet.getBigDecimal(column.getKey());
                    } else if(column.getValue().equals(InSituSpotDAO.Attribute.REASON_FOR_EXCLUSION
                            .getTOFieldName())) {
                        reasonForExclusion = currentResultSet.getString(column.getKey());
                    } else {
                        log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }
                }
                return log.traceExit(new InSituSpotTO(inSituSpotId, inSituEvidenceId,
                        inSituExpressionPatternId, conditionId, bgeeGeneId, expressionId,
                        pValue, DataState.convertToDataState(inSituData),
                        ExclusionReason.convertToExclusionReason(reasonForExclusion)));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }
}
