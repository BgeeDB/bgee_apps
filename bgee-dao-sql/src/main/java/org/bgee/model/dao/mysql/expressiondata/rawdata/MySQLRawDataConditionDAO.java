package org.bgee.model.dao.mysql.expressiondata.rawdata;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.BaseConditionTO.Sex;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataConditionFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;

public class MySQLRawDataConditionDAO extends MySQLDAO<RawDataConditionDAO.Attribute> implements RawDataConditionDAO {
    private final static Logger log = LogManager.getLogger(MySQLRawDataConditionDAO.class.getName());

    private static final Map<String, RawDataConditionDAO.Attribute> columnToAttributesMap;

    static {
        columnToAttributesMap = new HashMap<>();
        columnToAttributesMap.put("conditionId", RawDataConditionDAO.Attribute.ID);
        columnToAttributesMap.put("exprMappedConditionId", RawDataConditionDAO.Attribute.EXPR_MAPPED_CONDITION_ID);
        columnToAttributesMap.put("anatEntityId", RawDataConditionDAO.Attribute.ANAT_ENTITY_ID);
        columnToAttributesMap.put("stageId", RawDataConditionDAO.Attribute.STAGE_ID);
        columnToAttributesMap.put("sex", RawDataConditionDAO.Attribute.SEX);
        columnToAttributesMap.put("sexInferred", RawDataConditionDAO.Attribute.SEX_INFERRED);
        columnToAttributesMap.put("strain", RawDataConditionDAO.Attribute.STRAIN);
        columnToAttributesMap.put("speciesId", RawDataConditionDAO.Attribute.SPECIES_ID);
    }

    public MySQLRawDataConditionDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public RawDataConditionTOResultSet getRawDataConditionsBySpeciesIds(Collection<Integer> speciesIds,
            Collection<RawDataConditionDAO.Attribute> attributes) throws DAOException {
        log.entry(speciesIds, attributes);

        final Set<Integer> speIds = Collections.unmodifiableSet(speciesIds == null? new HashSet<>(): new HashSet<>(speciesIds));
        final Set<RawDataConditionDAO.Attribute> attrs = Collections.unmodifiableSet(attributes == null? 
                EnumSet.noneOf(RawDataConditionDAO.Attribute.class): EnumSet.copyOf(attributes));

        StringBuilder sb = new StringBuilder();
        String tableName = "cond";
        sb.append(generateSelectClause(tableName, columnToAttributesMap, true, attrs))
          .append(" FROM ").append(tableName);
        if (!speIds.isEmpty()) {
            sb.append(" WHERE ")
              .append(tableName).append(".").append("speciesId").append(" IN (")
              .append(BgeePreparedStatement.generateParameterizedQueryString(speIds.size()))
              .append(")");
        }
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sb.toString());
            if (!speIds.isEmpty()) {
                stmt.setIntegers(1, speIds, true);
            }
            return log.exit(new MySQLRawDataConditionTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public RawDataConditionTOResultSet getRawDataConditionsBySpeciesIdsAndConditionFilters(Collection<Integer> arg0,
            Collection<DAORawDataConditionFilter> arg1,
            Collection<org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO.Attribute> arg2)
            throws DAOException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Implementation of the {@code ConditionTOResultSet}. 
     * 
     * @author Frederic Bastian
     * @version Bgee 14 Feb. 2017
     * @since Bgee 14 Feb. 2017
     */
    class MySQLRawDataConditionTOResultSet extends MySQLDAOResultSet<RawDataConditionDAO.RawDataConditionTO>
            implements RawDataConditionTOResultSet {
        
        /**
         * @param statement The {@code BgeePreparedStatement}
         */
        private MySQLRawDataConditionTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected RawDataConditionDAO.RawDataConditionTO getNewTO() throws DAOException {
            log.entry();
            try {
                final ResultSet currentResultSet = this.getCurrentResultSet();
                Integer id = null, exprMappedCondId = null, speciesId = null;
                String anatEntityId = null, stageId = null, strain = null;
                Sex sex = null;
                Boolean sexInferred = null;

                //don't use MySQLDAO.getAttributeFromColName because we don't cover all columns
                //with ConditionDAO.Attributes (max rank columns)
                COL: for (String columnName : this.getColumnLabels().values()) {
                    RawDataConditionDAO.Attribute attr = columnToAttributesMap.get(columnName);
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
                        case SEX:
                            sex = Sex.convertToSex(currentResultSet.getString(columnName));
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
                return log.exit(new RawDataConditionTO(id, exprMappedCondId, anatEntityId, stageId,
                        sex, sexInferred, strain, speciesId));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }
}