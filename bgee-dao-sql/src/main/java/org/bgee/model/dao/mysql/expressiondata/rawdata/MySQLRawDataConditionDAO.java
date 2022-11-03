package org.bgee.model.dao.mysql.expressiondata.rawdata;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataConditionFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;

public class MySQLRawDataConditionDAO extends MySQLRawDataDAO<RawDataConditionDAO.Attribute>
implements RawDataConditionDAO {
    private final static Logger log = LogManager.getLogger(MySQLRawDataConditionDAO.class.getName());
    public final static String TABLE_NAME = "cond";

    public MySQLRawDataConditionDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public RawDataConditionTOResultSet getRawDataConditionsFromSpeciesIds(Collection<Integer> speciesIds,
            Collection<RawDataConditionDAO.Attribute> attributes) throws DAOException {
        log.traceEntry("{}, {}", speciesIds, attributes);
        return log.traceExit(getRawDataConditions(speciesIds, null, attributes));
    }

    @Override
    public RawDataConditionTOResultSet getRawDataConditionsFromRawConditionFilters(
            Collection<DAORawDataConditionFilter> rawCondFilters,
            Collection<RawDataConditionDAO.Attribute> attributes) throws DAOException {
        log.traceEntry("{}, {}", rawCondFilters, attributes);
        return log.traceExit(getRawDataConditions(null, rawCondFilters, attributes));
    }

    @Override
    public RawDataConditionTOResultSet getRawDataConditions(Collection<Integer> speciesIds,
            Collection<DAORawDataConditionFilter> conditionFilters,
            Collection<RawDataConditionDAO.Attribute> attributes)
            throws DAOException {
        final Set<Integer> speIds = Collections.unmodifiableSet(speciesIds == null? new HashSet<>():
            new HashSet<>(speciesIds));
        final Set<DAORawDataConditionFilter> condFilters = Collections.unmodifiableSet(
                conditionFilters == null?
                new HashSet<>(): new HashSet<>(conditionFilters));
        final Set<RawDataConditionDAO.Attribute> attrs = Collections.unmodifiableSet(attributes == null? 
                EnumSet.noneOf(RawDataConditionDAO.Attribute.class): EnumSet.copyOf(attributes));

        // generate SELECT
        StringBuilder sb = new StringBuilder();
        sb.append(generateSelectClause(TABLE_NAME, getColToAttributesMap(RawDataConditionDAO
                .Attribute.class), true, attrs))
        .append(" FROM ").append(TABLE_NAME);

        // generate WHERE CLAUSE
        if (!speIds.isEmpty() || !condFilters.isEmpty()) {
          sb.append(" WHERE ");
        }
        // FITER ON SPECIES IDS
        if (!speIds.isEmpty()) {
            sb.append(TABLE_NAME).append(".")
            .append(RawDataConditionDAO.Attribute.SPECIES_ID.getTOFieldName()).append(" IN (")
            .append(BgeePreparedStatement.generateParameterizedQueryString(speIds.size()))
            .append(")");
            if (!condFilters.isEmpty()) {
                sb.append(" AND ");
            }
        }
        // FILTER ON CONDITION PARAMETERS
        if (!condFilters.isEmpty()) {
            sb.append(condFilters.stream().map(cf -> {
                return generateOneConditionFilter(cf);
            }).collect(Collectors.joining(" OR ", "(", ")")));
        }
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection()
                    .prepareStatement(sb.toString());
            int paramIndex = 1;
            if (!speIds.isEmpty()) {
                stmt.setIntegers(paramIndex, speIds, true);
                paramIndex += speIds.size();
            }
            configureRawDataConditionFiltersStmt(stmt, condFilters, paramIndex);
            return log.traceExit(new MySQLRawDataConditionTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public RawDataConditionTOResultSet getRawDataConditionsFromIds(Collection<Integer> conditionIds,
            Collection<RawDataConditionDAO.Attribute> attrs)
            throws DAOException {
        log.traceEntry("{}, {}", conditionIds, attrs);
        if (conditionIds == null) {
            throw log.throwing(new IllegalArgumentException("conditionIds can not be null"));
        }
        final Set<RawDataConditionDAO.Attribute> clonedAttrs = Collections
                .unmodifiableSet(attrs == null || attrs.isEmpty()?
                EnumSet.allOf(RawDataConditionDAO.Attribute.class): EnumSet.copyOf(attrs));
        final Set<Integer> clonedCondIds = Collections.unmodifiableSet(conditionIds.stream()
                .filter(c -> c != null).collect(Collectors.toSet()));
        if (clonedCondIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("at least one conditionId has to be"
                    + "provided"));
        }
        // generate SELECT
        StringBuilder sb = new StringBuilder();
        sb.append(generateSelectClause(TABLE_NAME, getColToAttributesMap(RawDataConditionDAO
                .Attribute.class), true, clonedAttrs))
        .append(" FROM ").append(TABLE_NAME).append(" WHERE ")
        .append(RawDataConditionDAO.Attribute.ID.getTOFieldName())
        .append(" IN (")
        .append(BgeePreparedStatement.generateParameterizedQueryString(clonedCondIds.size()))
        .append(")");        
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection()
                    .prepareStatement(sb.toString());
            stmt.setIntegers(1, clonedCondIds, true);
            return log.traceExit(new MySQLRawDataConditionTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }    }
    /**
     * Implementation of the {@code ConditionTOResultSet}. 
     * 
     * @author Frederic Bastian
     * @version Bgee 15, Mar. 2021
     * @since Bgee 14, Feb. 2017
     */
    class MySQLRawDataConditionTOResultSet
    extends MySQLDAOResultSet<RawDataConditionDAO.RawDataConditionTO>
            implements RawDataConditionTOResultSet {

        /**
         * @param statement The {@code BgeePreparedStatement}
         */
        private MySQLRawDataConditionTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected RawDataConditionDAO.RawDataConditionTO getNewTO() throws DAOException {
            log.traceEntry();
            try {
                final ResultSet currentResultSet = this.getCurrentResultSet();
                Integer id = null, exprMappedCondId = null, speciesId = null;
                String anatEntityId = null, stageId = null, cellTypeId = null, strain = null;
                RawDataConditionDAO.RawDataConditionTO.DAORawDataSex sex = null;
                Boolean sexInferred = null;

                COL: for (String columnName : this.getColumnLabels().values()) {
                    RawDataConditionDAO.Attribute attr = getColToAttributesMap(RawDataConditionDAO
                            .Attribute.class).get(columnName);
                    if (attr == null) {
                        continue COL;
                    }
                    switch (attr) {
                        case ID:
                            id = currentResultSet.getInt(columnName);
                            break;
                        case EXPR_MAPPED_CONDITION_ID:
                            exprMappedCondId = currentResultSet.getInt(columnName);
                            break;
                        case SPECIES_ID:
                            speciesId = currentResultSet.getInt(columnName);
                            break;
                        case ANAT_ENTITY_ID:
                            anatEntityId = currentResultSet.getString(columnName);
                            break;
                        case STAGE_ID:
                            stageId = currentResultSet.getString(columnName);
                            break;
                        case CELL_TYPE_ID:
                            cellTypeId = currentResultSet.getString(columnName);
                            break;
                        case SEX:
                            sex = RawDataConditionDAO.RawDataConditionTO.DAORawDataSex
                            .convertToDAORawDataSex(currentResultSet.getString(columnName));
                            break;
                        case STRAIN:
                            strain = currentResultSet.getString(columnName);
                            break;
                        case SEX_INFERRED:
                            sexInferred = currentResultSet.getBoolean(columnName);
                            break;
                        default:
                            log.throwing(new UnrecognizedColumnException(columnName));
                    }
                }
                return log.traceExit(new RawDataConditionTO(id, exprMappedCondId, anatEntityId, stageId,
                        cellTypeId, sex, sexInferred, strain, speciesId));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }
    
    protected static int configureRawDataConditionFiltersStmt(BgeePreparedStatement stmt,
            Collection<DAORawDataConditionFilter> conditionFilters, int paramIndex)
                    throws SQLException {
        log.traceEntry("{}, {}, {}", stmt, conditionFilters, paramIndex);

        if (conditionFilters == null) {
            throw log.throwing(new IllegalArgumentException("conditionFilters can not be null"));
        }
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
        if(condFilter == null) {
            throw log.throwing(new IllegalArgumentException("condFilter can not be null"));
        }
        if(!condFilter.getAnatEntityIds().isEmpty() || !condFilter.getDevStageIds().isEmpty()
                || !condFilter.getCellTypeIds().isEmpty() || !condFilter.getSexIds().isEmpty()
                || !condFilter.getStrainIds().isEmpty()) {
            sb.append("(");
        }
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
            previousCond = true;
        }
        if (previousCond) {
            sb.append(")");
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
        sb.append(MySQLRawDataConditionDAO.TABLE_NAME).append(".")
        .append(attr.getTOFieldName()).append(" IN (")
        .append(BgeePreparedStatement.generateParameterizedQueryString(condValues.size()))
        .append(")");
        return log.traceExit(sb.toString());
        
    }
}