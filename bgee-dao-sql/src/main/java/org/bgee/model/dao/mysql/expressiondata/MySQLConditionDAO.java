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
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.ConditionDAO.GlobalConditionToRawConditionTO.ConditionRelationOrigin;
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
     * A {@code String} that is the field name for species IDs in condition tables.
     */
    public final static String SPECIES_ID = "speciesId";
    public final static String RAW_COND_ID_FIELD = "conditionId";
    public final static String GLOBAL_COND_ID_FIELD = "globalConditionId";
    private final static String COND_REL_ORIGIN_FIELD = "conditionRelationOrigin";
    
//    /**
//     * Create the "ON" part of a JOIN clause to link the original condition table containing 
//     * raw conditions using all parameters to a specific condition table, 
//     * for the provided parameter combination, using the appropriate fields for the join.
//     * 
//     * @param originalCondTable A {@code String} that is the name used in the query 
//     *                          of the original raw condition table.
//     * @param specificCondTable A {@code String} that is the name used in the query 
//     *                          of the targeted specific condition table.
//     * @param comb              The {@code CondParamCombination} allowing to target the appropriate fields.
//     * @return                  A {@code String} that is the "ON" part of the JOIN clause.
//     */
//    public static String getJoinOnBetweenCondTables(final String originalCondTable, 
//            final String specificCondTable, final CondParamCombination comb) {
//        log.entry(originalCondTable, specificCondTable, comb);
//        
//        if (comb.isAllParamCombination()) {
//            throw log.throwing(new IllegalArgumentException(
//                    "No join needed for condition parameter combination using all parameters."));
//        }
//
//        //retrieve the column names of condition parameters and species ID. 
//        //we use the method getColToAttributesMap(CondParamCombination) for convenience,
//        //these column names do not vary depending on the condition parameters used.
//        final Map<ConditionDAO.Attribute, String> condParamToColName = getColToAttributesMap(comb)
//                .entrySet().stream()
//                .collect(Collectors.toMap(e -> e.getValue(), e -> e.getKey()));
//
//        StringBuilder sb = new StringBuilder();
//        //always use the speciesId field for the join
//        String speIdField = condParamToColName.get(ConditionDAO.Attribute.SPECIES_ID);
//        if (speIdField == null) {
//            throw log.throwing(new IllegalStateException("No column name corresponding to "
//                    + ConditionDAO.Attribute.SPECIES_ID));
//        }
//        sb.append(originalCondTable).append(".").append(speIdField)
//          .append(" = ").append(specificCondTable).append(".").append(speIdField);
//        //now, join using the condition parameters used in this combination
//        sb.append(comb.getParameters().stream().map(p -> {
//            StringBuilder sb2 = new StringBuilder();
//            String colName = condParamToColName.get(p);
//            if (colName == null) {
//                throw log.throwing(new IllegalStateException("No column name corresponding to "
//                        + p));
//            }
//            sb2.append(" AND ").append(originalCondTable).append(".").append(colName)
//              .append(" = ").append(specificCondTable).append(".").append(colName);
//            return sb2.toString();
//        }).collect(Collectors.joining()));
//        
//        return log.exit(sb.toString());
//    }

    /**
     * @param tableName             A {@code String} that is the name of the global condition table
     *                              in the SQL query.
     * @param condParamCombination  A {@code Collection} of {@code ConditionDAO.Attribute}s defining the
     *                              condition parameters considered for aggregating the expression data
     *                              (see {@link Attribute#isConditionParameter()}).
     * @return                      A {@code String} that is the part of a WHERE clause
     *                              allowing to select the conditions for the requested
     *                              condition parameter combination.
     * @throws IllegalArgumentException If {@code conditionParameters} is {@code null}, empty,
     *                                  or one of the {@code Attribute}s in {@code conditionParameters}
     *                                  is not a condition parameter attributes (see 
     *                                  {@link ConditionDAO.Attribute#isConditionParameter()}). 
     */
    public static String getCondParamCombinationWhereClause(final String tableName,
            Collection<ConditionDAO.Attribute> condParamCombination) throws IllegalArgumentException {
        log.entry(tableName, condParamCombination);
        if (condParamCombination == null || condParamCombination.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "A condition parameter combination must be provided."));
        }
        if (condParamCombination.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "A combination of condition parameters must be provided"));
        }
        final Set<ConditionDAO.Attribute> condParams = EnumSet.copyOf(condParamCombination);
        if (condParams.stream().anyMatch(a -> !a.isConditionParameter())) {
            throw log.throwing(new IllegalArgumentException("The condition parameter combination "
                    + "contains some Attributes that are not condition parameters: " + condParams));
        }
    
        final Map<String, ConditionDAO.Attribute> colToAttr = getColToAttributesMap(true);
        return log.exit(EnumSet.allOf(ConditionDAO.Attribute.class).stream()
                .filter(a -> a.isConditionParameter())
                .map(a -> tableName + "." + getSelectExprFromAttribute(a, colToAttr) + " IS "
                        + (condParams.contains(a)? "NOT NULL": "NULL"))
                .collect(Collectors.joining(" AND ", "(", ")")));
    }

    /**
     * Get a {@code Map} associating column names to corresponding {@code ConditionDAO.Attribute}.
     * 
     * @param global    A {@code boolean} defining whether the global conditions (if {@code true})
     *                  were targeted, or the raw conditions (if {@code false}).
     * @return          A {@code Map} where keys are {@code String}s that are column names, 
     *                  the associated value being the corresponding {@code ConditionDAO.Attribute}.
     */
    private static Map<String, ConditionDAO.Attribute> getColToAttributesMap(boolean global) {
        log.entry(global);
        Map<String, ConditionDAO.Attribute> colToAttributesMap = new HashMap<>();
        colToAttributesMap.put(global? GLOBAL_COND_ID_FIELD: RAW_COND_ID_FIELD, ConditionDAO.Attribute.ID);
        //only the original condition table containing all parameters has the field "exprMappedConditionId", 
        //allowing to map conditions used in annotations to conditions used in expression tables.
        if (!global) {
            colToAttributesMap.put("exprMappedConditionId", ConditionDAO.Attribute.EXPR_MAPPED_CONDITION_ID);
        }
        colToAttributesMap.put("anatEntityId", ConditionDAO.Attribute.ANAT_ENTITY_ID);
        colToAttributesMap.put("stageId", ConditionDAO.Attribute.STAGE_ID);
        colToAttributesMap.put(SPECIES_ID, ConditionDAO.Attribute.SPECIES_ID);
//        colToAttributesMap.put("sex", ConditionDAO.Attribute.SEX);
//        colToAttributesMap.put("sexInferred", ConditionDAO.Attribute.SEX_INFERRED);
//        colToAttributesMap.put("strain", ConditionDAO.Attribute.STRAIN);
        
        return log.exit(colToAttributesMap);
    }

    public MySQLConditionDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public ConditionTOResultSet getRawConditionsBySpeciesIds(Collection<Integer> speciesIds,
            Collection<ConditionDAO.Attribute> attributes) throws DAOException {
        log.entry(speciesIds, attributes);
        return log.exit(this.getConditionsBySpeciesIds(false, speciesIds, null, attributes));
    }

    @Override
    public ConditionTOResultSet getGlobalConditionsBySpeciesIds(Collection<Integer> speciesIds,
            Collection<ConditionDAO.Attribute> conditionParameters, 
            Collection<ConditionDAO.Attribute> attributes) throws DAOException, IllegalArgumentException {
        log.entry(speciesIds, conditionParameters, attributes);
        return log.exit(this.getConditionsBySpeciesIds(true, speciesIds, conditionParameters, attributes));
    }


    private ConditionTOResultSet getConditionsBySpeciesIds(boolean global, Collection<Integer> speciesIds,
            Collection<ConditionDAO.Attribute> conditionParameters, 
            Collection<ConditionDAO.Attribute> attributes) throws DAOException, IllegalArgumentException {
        log.entry(global, speciesIds, conditionParameters, attributes);

        final Set<Integer> speIds = speciesIds == null? new HashSet<>(): new HashSet<>(speciesIds);
        final Set<ConditionDAO.Attribute> attrs = attributes == null? 
                EnumSet.noneOf(ConditionDAO.Attribute.class): EnumSet.copyOf(attributes);
        final String tableName = global? "globalCond": "cond";

        StringBuilder sb = new StringBuilder();
        sb.append(generateSelectClause(tableName, getColToAttributesMap(global),
                //for global conditions, we are never going to need the DISTINCT clause,
                //since we are always going to define the NULL/NOT NULL status for
                //all condition parameters. For raw conditions the table is small,
                //so we don't bother and always add the DISTINCT clause.
                !global, 
                attrs)).append(" FROM ").append(tableName);
        if (global) {
            sb.append(" WHERE ")
              .append(getCondParamCombinationWhereClause(tableName, conditionParameters));
        }
        if (!speIds.isEmpty()) {
            sb.append(global? " AND ": " WHERE ")
              .append(tableName).append(".").append(SPECIES_ID).append(" IN (")
              .append(BgeePreparedStatement.generateParameterizedQueryString(speIds.size()))
              .append(")");
        }
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sb.toString());
            if (!speIds.isEmpty()) {
                stmt.setIntegers(1, speIds, true);
            }
            return log.exit(new MySQLConditionTOResultSet(stmt, global));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public int getMaxGlobalConditionId() throws DAOException {
        log.entry();

        String condIdField = getSelectExprFromAttribute(ConditionDAO.Attribute.ID,
                getColToAttributesMap(true));
        String sql = "SELECT MAX(" + condIdField + ") AS " + condIdField + " FROM globalCond";
    
        try (ConditionTOResultSet resultSet = new MySQLConditionTOResultSet(
                this.getManager().getConnection().prepareStatement(sql), true)) {
            
            if (resultSet.next() && resultSet.getTO().getId() != null) {
                return log.exit(resultSet.getTO().getId());
            } 
            return log.exit(0);
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }
    
    @Override
    public GlobalConditionMaxRankTO getMaxRank() throws DAOException {
        log.entry();
        StringBuilder sb = new StringBuilder();
        //XXX: either we should have a ConditionMaxRankResultSet at some point,
        //or it will be managed directly by the ConditionTOResultSet if we add a 'Set' attribute
        //to ConditionTO to store one ConditionMaxRankTO per data type.
        //Anyway, these maxRanks attributes are not described in ConditionDAO.Attributes
        //because we abstracted away this database design with enumerated columns
        //(for a discussion about this design, see http://stackoverflow.com/q/42781299/1768736)
        sb.append("SELECT MAX(GREATEST(affymetrixMaxRank, rnaSeqMaxRank, ")
          .append("estMaxRank, inSituMaxRank)) AS maxRank, ")
          // FIXME: Enable usage of global max ranks. Global max ranks were not generated for the bgee v14.0
//          .append("MAX(GREATEST(affymetrixGlobalMaxRank, rnaSeqGlobalMaxRank, ")
//          .append("estGlobalMaxRank, inSituGlobalMaxRank)) AS globalMaxRank ")
          .append("MAX(GREATEST(affymetrixMaxRank, rnaSeqMaxRank, ")
          .append("estMaxRank, inSituMaxRank)) AS globalMaxRank ")
          .append("FROM globalCond");

        try (BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sb.toString())) {
            BigDecimal maxRank = null, globalMaxRank = null;
            ResultSet rs = stmt.getRealPreparedStatement().executeQuery();
            if (rs.next()) {
                maxRank = rs.getBigDecimal("maxRank");
                globalMaxRank = rs.getBigDecimal("globalMaxRank");
            }
            GlobalConditionMaxRankTO rankTO = new GlobalConditionMaxRankTO(maxRank, globalMaxRank);
            rs.close();
            return log.exit(rankTO);
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public int insertGlobalConditions(Collection<ConditionTO> conditionTOs) throws DAOException {
        log.entry(conditionTOs);
        
        if (conditionTOs == null || conditionTOs.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No condition provided"));
        }

        Set<ConditionDAO.Attribute> attrs = EnumSet.allOf(ConditionDAO.Attribute.class);
        //this field is not present in the global condition table
        attrs.remove(ConditionDAO.Attribute.EXPR_MAPPED_CONDITION_ID);
        //The order of the parameters is important for generating the query and then setting the parameters.
        final List<ConditionDAO.Attribute> toPopulate = new ArrayList<>(attrs);

        final Map<String, ConditionDAO.Attribute> colToAttrMap = getColToAttributesMap(true);
        StringBuilder sql = new StringBuilder(); 
        sql.append("INSERT INTO globalCond (")
           .append(toPopulate.stream()
                   .map(a -> getSelectExprFromAttribute(a, colToAttrMap))
                   .collect(Collectors.joining(", ")))
           .append(") VALUES ")
           .append(conditionTOs.stream()
                   .map(c -> "(" + BgeePreparedStatement.generateParameterizedQueryString(
                           toPopulate.size()) + ")")
                   .collect(Collectors.joining(", ")));

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

    @Override
    public int insertGlobalConditionToRawCondition(
            Collection<GlobalConditionToRawConditionTO> globalCondToRawCondTOs)
                    throws DAOException, IllegalArgumentException {
        log.entry(globalCondToRawCondTOs);

        if (globalCondToRawCondTOs == null || globalCondToRawCondTOs.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No condition relation provided"));
        }

        StringBuilder sql = new StringBuilder(); 
        sql.append("INSERT INTO globalCondToCond (")
           .append(RAW_COND_ID_FIELD).append(", ")
           .append(GLOBAL_COND_ID_FIELD).append(", ")
           .append(COND_REL_ORIGIN_FIELD)
           .append(") VALUES ");
        for (int i = 0; i < globalCondToRawCondTOs.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("(").append(BgeePreparedStatement.generateParameterizedQueryString(3))
               .append(") ");
        }
        try (BgeePreparedStatement stmt =
                this.getManager().getConnection().prepareStatement(sql.toString())) {
            int paramIndex = 1;
            for (GlobalConditionToRawConditionTO to: globalCondToRawCondTOs) {
                stmt.setInt(paramIndex, to.getRawConditionId());
                paramIndex++;
                stmt.setInt(paramIndex, to.getGlobalConditionId());
                paramIndex++;
                stmt.setString(paramIndex, to.getConditionRelationOrigin().getStringRepresentation());
                paramIndex++;
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
         * A {@code boolean} defining whether the global conditions (if {@code true}) were targeted,
         * or the raw conditions (if {@code false}).
         */
        private final boolean global;
        
        /**
         * @param statement The {@code BgeePreparedStatement}
         * @param global    A {@code boolean} defining whether the global conditions (if {@code true})
         *                  were targeted, or the raw conditions (if {@code false}).
         */
        private MySQLConditionTOResultSet(BgeePreparedStatement statement, boolean global) {
            super(statement);
            this.global = global;
        }

        @Override
        protected ConditionDAO.ConditionTO getNewTO() throws DAOException {
            log.entry();
            try {
                final ResultSet currentResultSet = this.getCurrentResultSet();
                Integer id = null, exprMappedCondId = null, speciesId = null;
                String anatEntityId = null, stageId = null;
                Map<String, ConditionDAO.Attribute> colToAttrMap = getColToAttributesMap(this.global);

                COL: for (Map.Entry<Integer, String> col : this.getColumnLabels().entrySet()) {
                    String columnName = col.getValue();
                    //don't use MySQLDAO.getAttributeFromColName because we don't cover all columns
                    //with ConditionDAO.Attributes (max rank columns)
                    ConditionDAO.Attribute attr = colToAttrMap.get(columnName);
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
                        default:
                            log.throwing(new UnrecognizedColumnException(columnName));
                    }
                }
                return log.exit(new ConditionTO(id, exprMappedCondId, anatEntityId, stageId, speciesId));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }

    /**
     * MySQL implementation of {@code GlobalConditionToRawConditionTOResultSet}.
     *
     * @author Frederic Bastian
     * @version Bgee 14 Mar. 2017
     * @since Bgee 14 Mar. 2017
     */
    public class MySQLGlobalConditionToRawConditionTOResultSet
    extends MySQLDAOResultSet<GlobalConditionToRawConditionTO>
    implements GlobalConditionToRawConditionTOResultSet {

        private MySQLGlobalConditionToRawConditionTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected GlobalConditionToRawConditionTO getNewTO() throws DAOException {
            log.entry();
            try {
                final ResultSet currentResultSet = this.getCurrentResultSet();
                Integer rawConditionId = null, globalConditionId = null;
                ConditionRelationOrigin relOrigin = null;

                for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                    String columnName = column.getValue();

                    if (columnName.equals(RAW_COND_ID_FIELD)) {
                        rawConditionId = currentResultSet.getInt(columnName);
                    } else if (columnName.equals(GLOBAL_COND_ID_FIELD)) {
                        globalConditionId = currentResultSet.getInt(columnName);
                    } else if (columnName.equals(COND_REL_ORIGIN_FIELD)) {
                        relOrigin = ConditionRelationOrigin.convertToCondRelOrigin(
                                currentResultSet.getString(columnName));
                    }  else {
                        throw log.throwing(new UnrecognizedColumnException(columnName));
                    }
                }

                return log.exit(new GlobalConditionToRawConditionTO(
                        rawConditionId, globalConditionId, relOrigin));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }
}
