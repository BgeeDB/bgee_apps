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
    private final static String TABLE_NAME = "cond";

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
}