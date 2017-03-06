package org.bgee.model.dao.mysql.expressiondata;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.ExperimentExpressionDAO;
import org.bgee.model.dao.api.expressiondata.ExperimentExpressionDAO.ExperimentExpressionTO.CallDirection;
import org.bgee.model.dao.api.expressiondata.ExperimentExpressionDAO.ExperimentExpressionTO.CallQuality;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
import org.bgee.model.dao.mysql.gene.MySQLGeneDAO;

/**
 * A {@code ExperimentExpressionDAO} for MySQL. 
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Feb. 2017
 * @see org.bgee.model.dao.api.expressiondata.ExperimentExpressionDAO.ExperimentExpressionTO
 * @since   Bgee 14, Dec. 2016
 */
public class MySQLExperimentExpressionDAO extends MySQLDAO<ExperimentExpressionDAO.Attribute> 
                                            implements ExperimentExpressionDAO  {
    private final static Logger log = LogManager.getLogger(MySQLExperimentExpressionDAO.class.getName());

    private final static Map<String, ExperimentExpressionDAO.Attribute> colToAttrMap;

    static {
        log.entry();
        
        Map<String, ExperimentExpressionDAO.Attribute> colToAttributesMap = new HashMap<>();
        colToAttributesMap.put("exprId", ExperimentExpressionDAO.Attribute.EXPRESSION_ID);
        colToAttributesMap.put("experimentId", ExperimentExpressionDAO.Attribute.EXPERIMENT_ID);
        colToAttributesMap.put("presentHighCount", ExperimentExpressionDAO.Attribute.PRESENT_HIGH_COUNT);
        colToAttributesMap.put("presentLowCount", ExperimentExpressionDAO.Attribute.PRESENT_LOW_COUNT);
        colToAttributesMap.put("absentHighCount", ExperimentExpressionDAO.Attribute.ABSENT_HIGH_COUNT);
        colToAttributesMap.put("absentLowCount", ExperimentExpressionDAO.Attribute.ABSENT_LOW_COUNT);
        colToAttributesMap.put("callDirection", ExperimentExpressionDAO.Attribute.CALL_DIRECTION);
        colToAttributesMap.put("callQuality", ExperimentExpressionDAO.Attribute.CALL_QUALITY);
        
        colToAttrMap = Collections.unmodifiableMap(colToAttributesMap);
    }

    /**
     * Generates the select_expr for retrieving correct expression IDs in queries, 
     * depending on the combination of condition parameters used. This is because 
     * the expression ID in experiment expression table is valid only for original expression calls 
     * using all condition parameters. Otherwise, we will do a join to the appropriate expression table, 
     * and use the corresponding expression ID field name.
     * 
     * @param expExprTableName  A {@code String} that is the name of experiment expression table 
     *                          used for the query.
     * @param comb              The {@code CondParamCombination} allowing to target the appropriate 
     *                          field and table names.
     * @return                  A {@code String} that is the select_expr for retrieving expression IDs.
     */
    private static String getExprIdSelect(String expExprTableName, CondParamCombination comb) {
        log.entry(expExprTableName, comb);

        StringBuilder sb = new StringBuilder();
        if (comb.isAllParamCombination()) {
            //original expression IDs used, so we can simply use those in the experiment expression table
            sb.append(expExprTableName);
        } else {
            sb.append(comb.getRawExprTable());
        }
        //if comb takes into account all condition parameters, then the selected exprIdField
        //should be named as in the table expExprTableName, so we use comb to retrieve it in any case.
        assert !comb.isAllParamCombination() ||
            comb.getRawExprIdField().equals(CondParamCombination.ORIGINAL_RAW_EXPR_ID_FIELD);
        sb.append(".").append(comb.getRawExprIdField());
        
        return log.exit(sb.toString());
    }
    private static String getJoin(String expExprTableName, CondParamCombination comb) {
        log.entry(expExprTableName, comb);
        
        StringBuilder sb = new StringBuilder();
        
        //first, we join to the original raw expression table, we need it in all cases
        //to retrieve the bgeeGeneId for the ordering.
        sb.append(" INNER JOIN ").append(CondParamCombination.ORIGINAL_RAW_EXPR_TABLE)
          .append(" ON ").append(CondParamCombination.ORIGINAL_RAW_EXPR_TABLE).append(".")
          .append(CondParamCombination.ORIGINAL_RAW_EXPR_ID_FIELD)
          .append(" = ").append(expExprTableName).append(".")
          .append(CondParamCombination.ORIGINAL_RAW_EXPR_ID_FIELD);

        //Now we join to the appropriate condition and expression tables for this parameter combination
        //using the appropriate fields for the join, only if this parameter combination 
        //does not take into account all fields. The aim is to retrieve the expression ID
        //from the appropriate table.
        if (!comb.isAllParamCombination()) {
            //we join to the original condition table.
            sb.append(" INNER JOIN ").append(CondParamCombination.ORIGINAL_RAW_COND_TABLE)
            .append(" ON ").append(CondParamCombination.ORIGINAL_RAW_COND_TABLE).append(".")
            .append(CondParamCombination.ORIGINAL_RAW_COND_ID_FIELD)
            .append(" = ").append(CondParamCombination.ORIGINAL_RAW_EXPR_TABLE).append(".")
            .append(CondParamCombination.ORIGINAL_RAW_COND_ID_FIELD);
            
            //then to the appropriate condition table
            sb.append(" INNER JOIN ").append(comb.getCondTable()).append(" ON ")
            .append(MySQLConditionDAO.getJoinOnBetweenCondTables(
                    CondParamCombination.ORIGINAL_RAW_COND_TABLE, comb.getCondTable(), comb));
            
            //finally, we join to the appropriate expression table for this parameter combination, 
            //using the bgeeGeneId in original raw expression table and the mapped conditionId
            //retrieved in specific condition table.
            sb.append(" INNER JOIN ").append(comb.getRawExprTable())
            .append(" ON ").append(comb.getRawExprTable()).append(".").append(MySQLGeneDAO.BGEE_GENE_ID)
            .append(" = ").append(CondParamCombination.ORIGINAL_RAW_EXPR_TABLE)
            .append(".").append(MySQLGeneDAO.BGEE_GENE_ID)
            .append(" AND ").append(comb.getRawExprTable()).append(".").append(comb.getCondIdField())
            .append(" = ").append(comb.getCondTable()).append(".").append(comb.getCondIdField());
        }
        
        return log.exit(sb.toString());
    }
    
    private static String getWhere(Set<Integer> geneIds) {
        log.entry();
        StringBuilder sb = new StringBuilder();
        sb.append(" WHERE ").append(CondParamCombination.ORIGINAL_RAW_EXPR_TABLE)
          .append(".").append(MySQLGeneDAO.BGEE_GENE_ID).append(" IN (")
          .append(BgeePreparedStatement.generateParameterizedQueryString(geneIds.size())).append(") ");
        return log.exit(sb.toString());
    }
    private static String getOrderBy(CondParamCombination comb) {
        log.entry(comb);
        StringBuilder sb = new StringBuilder();
        sb.append(" ORDER BY ").append(comb.getRawExprTable()).append(".").append(MySQLGeneDAO.BGEE_GENE_ID)
          .append(", ").append(comb.getRawExprTable()).append(".").append(comb.getRawExprIdField());
        return log.exit(sb.toString());
    }

    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     * 
     * @param manager                       The {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException     If {@code manager} is {@code null}.
     */
    public MySQLExperimentExpressionDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public ExperimentExpressionTOResultSet getAffymetrixExpExprsOrderedByGeneIdAndExprId(
            Collection<Integer> geneIds,
            Collection<ConditionDAO.Attribute> condParams) throws DAOException, IllegalArgumentException {
        log.entry(geneIds, condParams);
        
        if (geneIds == null || geneIds.isEmpty() || geneIds.stream().anyMatch(id -> id == null)) {
            throw log.throwing(new IllegalArgumentException("No gene IDs or null gene ID provided"));
        }
        Set<Integer> clonedGeneIds = new HashSet<>(geneIds);

        CondParamCombination comb = CondParamCombination.getCombination(condParams);
        String tableName = "microarrayExperimentExpression";
        
        StringBuilder sb = new StringBuilder("SELECT ")
            .append(getExprIdSelect(tableName, comb)).append(" AS ").append(
                    getSelectExprFromAttribute(ExperimentExpressionDAO.Attribute.EXPRESSION_ID, 
                            colToAttrMap))
            .append(", microarrayExperimentId AS ").append(
                    getSelectExprFromAttribute(ExperimentExpressionDAO.Attribute.EXPERIMENT_ID, 
                            colToAttrMap))
            .append(", presentHighMicroarrayChipCount AS ").append(
                    getSelectExprFromAttribute(ExperimentExpressionDAO.Attribute.PRESENT_HIGH_COUNT, 
                            colToAttrMap))
            .append(", presentLowMicroarrayChipCount AS ").append(
                    getSelectExprFromAttribute(ExperimentExpressionDAO.Attribute.PRESENT_LOW_COUNT, 
                            colToAttrMap))
            .append(", absentHighMicroarrayChipCount AS ").append(
                    getSelectExprFromAttribute(ExperimentExpressionDAO.Attribute.ABSENT_HIGH_COUNT, 
                            colToAttrMap))
            .append(", absentLowMicroarrayChipCount AS ").append(
                    getSelectExprFromAttribute(ExperimentExpressionDAO.Attribute.ABSENT_LOW_COUNT, 
                            colToAttrMap))
            .append(", microarrayExperimentCallDirection AS ").append(
                    getSelectExprFromAttribute(ExperimentExpressionDAO.Attribute.CALL_DIRECTION, 
                            colToAttrMap))
            .append(", microarrayExperimentCallQuality AS ").append(
                    getSelectExprFromAttribute(ExperimentExpressionDAO.Attribute.CALL_QUALITY, 
                            colToAttrMap))
            .append(" FROM ").append(tableName)
            .append(getJoin(tableName, comb))
            .append(getWhere(clonedGeneIds))
            .append(getOrderBy(comb));
        
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sb.toString());
            stmt.setIntegers(1, clonedGeneIds, true);
            return log.exit(new MySQLExperimentExpressionTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public ExperimentExpressionTOResultSet getESTExpExprsOrderedByGeneIdAndExprId(
            Collection<Integer> geneIds,
            Collection<ConditionDAO.Attribute> condParams) throws DAOException, IllegalArgumentException {
        log.entry(geneIds, condParams);
        
        if (geneIds == null || geneIds.isEmpty() || geneIds.stream().anyMatch(id -> id == null)) {
            throw log.throwing(new IllegalArgumentException("No gene IDs or null gene ID provided"));
        }
        Set<Integer> clonedGeneIds = new HashSet<>(geneIds);

        CondParamCombination comb = CondParamCombination.getCombination(condParams);
        String tableName = "estLibraryExpression";
        
        //Note: EST libraries are only used to generate present expression calls, 
        //so the table fields are slightly different
        StringBuilder sb = new StringBuilder("SELECT ")
            .append(getExprIdSelect(tableName, comb)).append(" AS ").append(
                    getSelectExprFromAttribute(ExperimentExpressionDAO.Attribute.EXPRESSION_ID, 
                            colToAttrMap))
            .append(", estLibraryId AS ").append(
                    getSelectExprFromAttribute(ExperimentExpressionDAO.Attribute.EXPERIMENT_ID, 
                            colToAttrMap))
            .append(", IF(estLibraryCallQuality = '")
                    .append(CallQuality.HIGH.getStringRepresentation()).append("', 1, 0) AS ").append(
                    getSelectExprFromAttribute(ExperimentExpressionDAO.Attribute.PRESENT_HIGH_COUNT, 
                            colToAttrMap))
            .append(", IF(estLibraryCallQuality = '")
                    .append(CallQuality.LOW.getStringRepresentation()).append("', 1, 0) AS ").append(
                    getSelectExprFromAttribute(ExperimentExpressionDAO.Attribute.PRESENT_LOW_COUNT, 
                            colToAttrMap))
            .append(", 0 AS ").append(
                    getSelectExprFromAttribute(ExperimentExpressionDAO.Attribute.ABSENT_HIGH_COUNT, 
                            colToAttrMap))
            .append(", 0 AS ").append(
                    getSelectExprFromAttribute(ExperimentExpressionDAO.Attribute.ABSENT_LOW_COUNT, 
                            colToAttrMap))
            .append(", '").append(CallDirection.PRESENT.getStringRepresentation())
                    .append("' AS ").append(
                    getSelectExprFromAttribute(ExperimentExpressionDAO.Attribute.CALL_DIRECTION, 
                            colToAttrMap))
            .append(", estLibraryCallQuality AS ").append(
                    getSelectExprFromAttribute(ExperimentExpressionDAO.Attribute.CALL_QUALITY, 
                            colToAttrMap))
            .append(" FROM ").append(tableName)
            .append(getJoin(tableName, comb))
            .append(getWhere(clonedGeneIds))
            .append(getOrderBy(comb));
        
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sb.toString());
            stmt.setIntegers(1, clonedGeneIds, true);
            return log.exit(new MySQLExperimentExpressionTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public ExperimentExpressionTOResultSet getInSituExpExprsOrderedByGeneIdAndExprId(
            Collection<Integer> geneIds,
            Collection<ConditionDAO.Attribute> condParams) throws DAOException, IllegalArgumentException {
        log.entry(geneIds, condParams);
        
        if (geneIds == null || geneIds.isEmpty() || geneIds.stream().anyMatch(id -> id == null)) {
            throw log.throwing(new IllegalArgumentException("No gene IDs or null gene ID provided"));
        }
        Set<Integer> clonedGeneIds = new HashSet<>(geneIds);

        CondParamCombination comb = CondParamCombination.getCombination(condParams);
        String tableName = "inSituExperimentExpression";
        
        StringBuilder sb = new StringBuilder("SELECT ")
            .append(getExprIdSelect(tableName, comb)).append(" AS ").append(
                    getSelectExprFromAttribute(ExperimentExpressionDAO.Attribute.EXPRESSION_ID, 
                            colToAttrMap))
            .append(", inSituExperimentId AS ").append(
                    getSelectExprFromAttribute(ExperimentExpressionDAO.Attribute.EXPERIMENT_ID, 
                            colToAttrMap))
            .append(", presentHighInSituSpotCount AS ").append(
                    getSelectExprFromAttribute(ExperimentExpressionDAO.Attribute.PRESENT_HIGH_COUNT, 
                            colToAttrMap))
            .append(", presentLowInSituSpotCount AS ").append(
                    getSelectExprFromAttribute(ExperimentExpressionDAO.Attribute.PRESENT_LOW_COUNT, 
                            colToAttrMap))
            .append(", absentHighInSituSpotCount AS ").append(
                    getSelectExprFromAttribute(ExperimentExpressionDAO.Attribute.ABSENT_HIGH_COUNT, 
                            colToAttrMap))
            .append(", absentLowInSituSpotCount AS ").append(
                    getSelectExprFromAttribute(ExperimentExpressionDAO.Attribute.ABSENT_LOW_COUNT, 
                            colToAttrMap))
            .append(", inSituExperimentCallDirection AS ").append(
                    getSelectExprFromAttribute(ExperimentExpressionDAO.Attribute.CALL_DIRECTION, 
                            colToAttrMap))
            .append(", inSituExperimentCallQuality AS ").append(
                    getSelectExprFromAttribute(ExperimentExpressionDAO.Attribute.CALL_QUALITY, 
                            colToAttrMap))
            .append(" FROM ").append(tableName)
            .append(getJoin(tableName, comb))
            .append(getWhere(clonedGeneIds))
            .append(getOrderBy(comb));
        
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sb.toString());
            stmt.setIntegers(1, clonedGeneIds, true);
            return log.exit(new MySQLExperimentExpressionTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public ExperimentExpressionTOResultSet getRNASeqExpExprsOrderedByGeneIdAndExprId(
            Collection<Integer> geneIds,
            Collection<ConditionDAO.Attribute> condParams) throws DAOException, IllegalArgumentException {
        log.entry(geneIds, condParams);
        
        if (geneIds == null || geneIds.isEmpty() || geneIds.stream().anyMatch(id -> id == null)) {
            throw log.throwing(new IllegalArgumentException("No gene IDs or null gene ID provided"));
        }
        Set<Integer> clonedGeneIds = new HashSet<>(geneIds);

        CondParamCombination comb = CondParamCombination.getCombination(condParams);
        String tableName = "rnaSeqExperimentExpression";
        
        StringBuilder sb = new StringBuilder("SELECT ")
            .append(getExprIdSelect(tableName, comb)).append(" AS ").append(
                    getSelectExprFromAttribute(ExperimentExpressionDAO.Attribute.EXPRESSION_ID, 
                            colToAttrMap))
            .append(", rnaSeqExperimentId AS ").append(
                    getSelectExprFromAttribute(ExperimentExpressionDAO.Attribute.EXPERIMENT_ID, 
                            colToAttrMap))
            .append(", presentHighRNASeqLibraryCount AS ").append(
                    getSelectExprFromAttribute(ExperimentExpressionDAO.Attribute.PRESENT_HIGH_COUNT, 
                            colToAttrMap))
            .append(", presentLowRNASeqLibraryCount AS ").append(
                    getSelectExprFromAttribute(ExperimentExpressionDAO.Attribute.PRESENT_LOW_COUNT, 
                            colToAttrMap))
            .append(", absentHighRNASeqLibraryCount AS ").append(
                    getSelectExprFromAttribute(ExperimentExpressionDAO.Attribute.ABSENT_HIGH_COUNT, 
                            colToAttrMap))
            .append(", absentLowRNASeqLibraryCount AS ").append(
                    getSelectExprFromAttribute(ExperimentExpressionDAO.Attribute.ABSENT_LOW_COUNT, 
                            colToAttrMap))
            .append(", rnaSeqExperimentCallDirection AS ").append(
                    getSelectExprFromAttribute(ExperimentExpressionDAO.Attribute.CALL_DIRECTION, 
                            colToAttrMap))
            .append(", rnaSeqExperimentCallQuality AS ").append(
                    getSelectExprFromAttribute(ExperimentExpressionDAO.Attribute.CALL_QUALITY, 
                            colToAttrMap))
            .append(" FROM ").append(tableName)
            .append(getJoin(tableName, comb))
            .append(getWhere(clonedGeneIds))
            .append(getOrderBy(comb));
        
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sb.toString());
            stmt.setIntegers(1, clonedGeneIds, true);
            return log.exit(new MySQLExperimentExpressionTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }
    
    /**
     * Implementation of the {@code ExperimentExpressionTOResultSet}. 
     * 
     * @author Frederic Bastian
     * @version Bgee 14 Feb. 2017
     * @since Bgee 14 Feb. 2017
     */
    class MySQLExperimentExpressionTOResultSet 
            extends MySQLDAOResultSet<ExperimentExpressionDAO.ExperimentExpressionTO>
            implements ExperimentExpressionTOResultSet {
        
        /**
         * @param statement The {@code BgeePreparedStatement}
         * @param comb      The {@code CondParamCombination} allowing to target the appropriate 
         *                  field and table names.
         */
        private MySQLExperimentExpressionTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected ExperimentExpressionDAO.ExperimentExpressionTO getNewTO() throws DAOException {
            try {
                log.entry();
                final ResultSet currentResultSet = this.getCurrentResultSet();
                Integer exprId = null, presentHighCount = null, presentLowCount = null, 
                        absentHighCount = null, absentLowCount = null;
                String experimentId = null;
                CallDirection callDirection = null;
                CallQuality callQuality = null;

                for (Map.Entry<Integer, String> col : this.getColumnLabels().entrySet()) {
                    String columnName = col.getValue();
                    ExperimentExpressionDAO.Attribute attr = getAttributeFromColName(columnName, colToAttrMap);
                    switch (attr) {
                        case EXPRESSION_ID:
                            exprId = currentResultSet.getInt(columnName);
                            break;
                        case EXPERIMENT_ID:
                            experimentId = currentResultSet.getString(columnName);
                            break;
                        case PRESENT_HIGH_COUNT:
                            presentHighCount = currentResultSet.getInt(columnName);
                            break;
                        case PRESENT_LOW_COUNT:
                            presentLowCount = currentResultSet.getInt(columnName);
                            break;
                        case ABSENT_HIGH_COUNT:
                            absentHighCount = currentResultSet.getInt(columnName);
                            break;
                        case ABSENT_LOW_COUNT:
                            absentLowCount = currentResultSet.getInt(columnName);
                            break;
                        case CALL_DIRECTION:
                            callDirection = CallDirection.convertToCallDirection(
                                    currentResultSet.getString(columnName));
                            break;
                        case CALL_QUALITY:
                            callQuality = CallQuality.convertToCallQuality(
                                    currentResultSet.getString(columnName));
                            break;
                        default:
                            log.throwing(new UnrecognizedColumnException(columnName));
                    }
                }
                return log.exit(new ExperimentExpressionTO(exprId, experimentId, 
                        presentHighCount, presentLowCount, absentHighCount, absentLowCount,
                        callQuality, callDirection));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }
}
