package org.bgee.model.dao.mysql.expressiondata;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;

/**
 * An {@code ConditionDAO} for MySQL. 
 * 
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 14, Feb. 2017
 * @see org.bgee.model.dao.api.anatdev.ConditionDAO.ConditionTO
 * @since   Bgee 14, Feb. 2017
 */
public class MySQLConditionDAO extends MySQLDAO<ConditionDAO.Attribute> implements ConditionDAO {
    private final static Logger log = LogManager.getLogger(MySQLConditionDAO.class.getName());
    
    /**
     * Get a {@code Map} associating column names to corresponding {@code ConditionDAO.Attribute}.
     * 
     * @param comb  The {@code CondParamCombination} allowing to target the appropriate 
     *              field and table names.
     * @return      A {@code Map} where keys are {@code String}s that are column names, 
     *              the associated value being the corresponding {@code ConditionDAO.Attribute}.
     */
    private static Map<String, ConditionDAO.Attribute> getColToAttributesMap(
            CondParamCombination comb) {
        log.entry(comb);
        Map<String, ConditionDAO.Attribute> colToAttributesMap = new HashMap<>();
        colToAttributesMap.put(comb.getCondIdField(), ConditionDAO.Attribute.ID);
        //only the original condition table containing all parameters has the field "exprMappedConditionId", 
        //allowing to map conditions used in annotations to conditions used in expression tables.
        if (comb.isAllParamCombination()) {
            colToAttributesMap.put("exprMappedConditionId", ConditionDAO.Attribute.EXPR_MAPPED_CONDITION_ID);
        }
        colToAttributesMap.put("anatEntityId", ConditionDAO.Attribute.ANAT_ENTITY_ID);
        colToAttributesMap.put("stageId", ConditionDAO.Attribute.STAGE_ID);
        colToAttributesMap.put("speciesId", ConditionDAO.Attribute.SPECIES_ID);
        colToAttributesMap.put("affymetrixMaxRank", ConditionDAO.Attribute.AFFYMETRIX_MAX_RANK);
        colToAttributesMap.put("rnaSeqMaxRank", ConditionDAO.Attribute.RNA_SEQ_MAX_RANK);
        colToAttributesMap.put("estMaxRank", ConditionDAO.Attribute.EST_MAX_RANK);
        colToAttributesMap.put("inSituMaxRank", ConditionDAO.Attribute.IN_SITU_MAX_RANK);
        
        return log.exit(colToAttributesMap);
    }

    public MySQLConditionDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public ConditionTOResultSet getConditionsBySpeciesIds(Collection<Integer> speciesIds,
            Collection<ConditionDAO.Attribute> conditionParameters, 
            Collection<ConditionDAO.Attribute> attributes) throws DAOException, IllegalArgumentException {
        log.entry(speciesIds, conditionParameters, attributes);

        CondParamCombination comb = CondParamCombination.getCombination(conditionParameters);
        Set<Integer> speIds = speciesIds == null? new HashSet<>(): new HashSet<>(speciesIds);
        Set<ConditionDAO.Attribute> attrs = attributes == null? 
                EnumSet.noneOf(ConditionDAO.Attribute.class): EnumSet.copyOf(attributes);
        
        String sql = generateSelectClause(comb.getCondTable(), getColToAttributesMap(comb), 
                !attrs.isEmpty() && !attrs.containsAll(EnumSet.allOf(ConditionDAO.Attribute.class)) &&
                    !attrs.contains(ConditionDAO.Attribute.ID), 
                attrs) 
                + " FROM " + comb.getCondTable();
        if (!speIds.isEmpty()) {
            sql += " WHERE speciesId IN (" 
                + BgeePreparedStatement.generateParameterizedQueryString(speIds.size()) + ")";
        }
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            if (!speIds.isEmpty()) {
                stmt.setIntegers(1, speIds, true);
            }
            return log.exit(new MySQLConditionTOResultSet(stmt, comb));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public int getMaxConditionId(Collection<ConditionDAO.Attribute> conditionParameters)
            throws DAOException, IllegalArgumentException {
        log.entry(conditionParameters);
        
        CondParamCombination comb = CondParamCombination.getCombination(conditionParameters);

        String sql = "SELECT MAX(" + comb.getCondIdField() + ") AS " + comb.getCondIdField() 
            + " FROM " + comb.getCondTable();
    
        try (MySQLConditionTOResultSet resultSet = new MySQLConditionTOResultSet(
                this.getManager().getConnection().prepareStatement(sql), comb)) {
            
            if (resultSet.next() && resultSet.getTO().getId() != null) {
                return log.exit(resultSet.getTO().getId());
            } 
            return log.exit(0);
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public int insertConditions(Collection<ConditionTO> conditionTOs, 
            Collection<ConditionDAO.Attribute> conditionParameters)
            throws DAOException, IllegalArgumentException {
        log.entry(conditionTOs, conditionParameters);

        CondParamCombination comb = CondParamCombination.getCombination(conditionParameters);
        
        if (conditionTOs == null || conditionTOs.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No condition provided"));
        }
        
        //The order of the parameters is important for generating the query and then setting the parameters.
        List<ConditionDAO.Attribute> toPopulate = new ArrayList<>();
        if (conditionTOs.stream().anyMatch(c -> c.getId() != null)) {
            toPopulate.add(ConditionDAO.Attribute.ID);
        }
        //only the original condition table holding all condition parameters has an exprMappedCondId field
        if (comb.isAllParamCombination() && 
                conditionTOs.stream().anyMatch(c -> c.getExprMappedConditionId() != null)) {
            toPopulate.add(ConditionDAO.Attribute.EXPR_MAPPED_CONDITION_ID);
        }
        if (conditionTOs.stream().anyMatch(c -> c.getSpeciesId() != null)) {
            toPopulate.add(ConditionDAO.Attribute.SPECIES_ID);
        }
        if (conditionTOs.stream().anyMatch(c -> c.getAnatEntityId() != null)) {
            toPopulate.add(ConditionDAO.Attribute.ANAT_ENTITY_ID);
        }
        if (conditionTOs.stream().anyMatch(c -> c.getStageId() != null)) {
            toPopulate.add(ConditionDAO.Attribute.STAGE_ID);
        }
        if (conditionTOs.stream().anyMatch(c -> c.getAffymetrixMaxRank() != null)) {
            toPopulate.add(ConditionDAO.Attribute.AFFYMETRIX_MAX_RANK);
        }
        if (conditionTOs.stream().anyMatch(c -> c.getRNASeqMaxRank() != null)) {
            toPopulate.add(ConditionDAO.Attribute.RNA_SEQ_MAX_RANK);
        }
        if (conditionTOs.stream().anyMatch(c -> c.getESTMaxRank() != null)) {
            toPopulate.add(ConditionDAO.Attribute.EST_MAX_RANK);
        }
        if (conditionTOs.stream().anyMatch(c -> c.getInSituMaxRank() != null)) {
            toPopulate.add(ConditionDAO.Attribute.IN_SITU_MAX_RANK);
        }
        
        final Map<String, ConditionDAO.Attribute> colToAttrMap = getColToAttributesMap(comb);
        StringBuilder sql = new StringBuilder(); 
        sql.append("INSERT INTO ").append(comb.getCondTable()).append(" (")
        .append(toPopulate.stream().map(a -> getSelectExprFromAttribute(a, colToAttrMap))
                          .collect(Collectors.joining(", ")))
        .append(") VALUES ");
        for (int i = 0; i < conditionTOs.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("(").append(BgeePreparedStatement.generateParameterizedQueryString(toPopulate.size()))
               .append(") ");
        }
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sql.toString())) {
            int paramIndex = 1;
            for (ConditionTO conditionTO: conditionTOs) {
                for (ConditionDAO.Attribute attr: toPopulate) {
                    switch (attr) {
                    case ID:
                        stmt.setInt(paramIndex, conditionTO.getId());
                        paramIndex++;
                        break;
                    case EXPR_MAPPED_CONDITION_ID:
                        stmt.setInt(paramIndex, conditionTO.getExprMappedConditionId());
                        paramIndex++;
                        break;
                    case SPECIES_ID:
                        stmt.setInt(paramIndex, conditionTO.getSpeciesId());
                        paramIndex++;
                        break;
                    case ANAT_ENTITY_ID:
                        stmt.setString(paramIndex, conditionTO.getAnatEntityId());
                        paramIndex++;
                        break;
                    case STAGE_ID:
                        stmt.setString(paramIndex, conditionTO.getStageId());
                        paramIndex++;
                        break;
                    case AFFYMETRIX_MAX_RANK:
                        stmt.setBigDecimal(paramIndex, conditionTO.getAffymetrixMaxRank());
                        paramIndex++;
                        break;
                    case RNA_SEQ_MAX_RANK:
                        stmt.setBigDecimal(paramIndex, conditionTO.getRNASeqMaxRank());
                        paramIndex++;
                        break;
                    case EST_MAX_RANK:
                        stmt.setBigDecimal(paramIndex, conditionTO.getESTMaxRank());
                        paramIndex++;
                        break;
                    case IN_SITU_MAX_RANK:
                        stmt.setBigDecimal(paramIndex, conditionTO.getInSituMaxRank());
                        paramIndex++;
                        break;
                    default:
                        log.throwing(new IllegalStateException("Unsupported attribute: " + attr));
                    }
                }
            }
            
            return log.exit(stmt.executeUpdate());
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }
    
    /**
     * Implementation of the {@code ConditionTOResultSet}. 
     * 
     * @author Frederic Bastian
     * @version Bgee 14 Feb. 2017
     * @since Bgee 14 Feb. 2017
     */
    class MySQLConditionTOResultSet extends MySQLDAOResultSet<ConditionDAO.ConditionTO>
            implements ConditionTOResultSet {

        /**
         * The {@code CondParamCombination} allowing to target the appropriate field and table names.
         */
        private final CondParamCombination comb;
        
        /**
         * @param statement The {@code BgeePreparedStatement}
         * @param comb      The {@code CondParamCombination} allowing to target the appropriate 
         *                  field and table names.
         */
        private MySQLConditionTOResultSet(BgeePreparedStatement statement, CondParamCombination comb) {
            super(statement);
            this.comb = comb;
        }

        @Override
        protected ConditionDAO.ConditionTO getNewTO() throws DAOException {
            try {
                log.entry();
                final ResultSet currentResultSet = this.getCurrentResultSet();
                Integer id = null, exprMappedCondId = null, speciesId = null;
                String anatEntityId = null, stageId = null;
                BigDecimal affyMaxRank = null, rnaSeqMaxRank = null, estMaxRank = null, inSituMaxRank = null;
                Map<String, ConditionDAO.Attribute> colToAttrMap = getColToAttributesMap(comb);

                for (Map.Entry<Integer, String> col : this.getColumnLabels().entrySet()) {
                    String columnName = col.getValue();
                    ConditionDAO.Attribute attr = getAttributeFromColName(columnName, colToAttrMap);
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
                        case AFFYMETRIX_MAX_RANK:
                            affyMaxRank = currentResultSet.getBigDecimal(columnName);
                            break;
                        case RNA_SEQ_MAX_RANK:
                            rnaSeqMaxRank = currentResultSet.getBigDecimal(columnName);
                            break;
                        case EST_MAX_RANK:
                            estMaxRank = currentResultSet.getBigDecimal(columnName);
                            break;
                        case IN_SITU_MAX_RANK:
                            inSituMaxRank = currentResultSet.getBigDecimal(columnName);
                            break;
                        default:
                            log.throwing(new UnrecognizedColumnException(columnName));
                    }
                }
                return log.exit(new ConditionTO(id, exprMappedCondId, anatEntityId, stageId, speciesId, 
                        affyMaxRank, rnaSeqMaxRank, estMaxRank, inSituMaxRank));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }
}
