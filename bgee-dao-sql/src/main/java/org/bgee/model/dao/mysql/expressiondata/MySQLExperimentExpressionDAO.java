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
import org.bgee.model.dao.api.expressiondata.rawdata.ExperimentExpressionDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.ExperimentExpressionDAO.ExperimentExpressionTO.CallDirection;
import org.bgee.model.dao.api.expressiondata.rawdata.ExperimentExpressionDAO.ExperimentExpressionTO.CallQuality;
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
 * @see org.bgee.model.dao.api.expressiondata.rawdata.ExperimentExpressionDAO.ExperimentExpressionTO
 * @since   Bgee 14, Dec. 2016
 */
public class MySQLExperimentExpressionDAO extends MySQLDAO<ExperimentExpressionDAO.Attribute> 
                                            implements ExperimentExpressionDAO  {
    private final static Logger log = LogManager.getLogger(MySQLExperimentExpressionDAO.class.getName());

    private final static Map<String, ExperimentExpressionDAO.Attribute> colToAttrMap;

    static {
        log.traceEntry();
        
        Map<String, ExperimentExpressionDAO.Attribute> colToAttributesMap = new HashMap<>();
        colToAttributesMap.put(MySQLRawExpressionCallDAO.EXPR_ID_FIELD,
                ExperimentExpressionDAO.Attribute.EXPRESSION_ID);
        colToAttributesMap.put("experimentId", ExperimentExpressionDAO.Attribute.EXPERIMENT_ID);
        colToAttributesMap.put("presentHighCount", ExperimentExpressionDAO.Attribute.PRESENT_HIGH_COUNT);
        colToAttributesMap.put("presentLowCount", ExperimentExpressionDAO.Attribute.PRESENT_LOW_COUNT);
        colToAttributesMap.put("absentHighCount", ExperimentExpressionDAO.Attribute.ABSENT_HIGH_COUNT);
        colToAttributesMap.put("absentLowCount", ExperimentExpressionDAO.Attribute.ABSENT_LOW_COUNT);
        colToAttributesMap.put("callDirection", ExperimentExpressionDAO.Attribute.CALL_DIRECTION);
        colToAttributesMap.put("callQuality", ExperimentExpressionDAO.Attribute.CALL_QUALITY);
        
        colToAttrMap = Collections.unmodifiableMap(colToAttributesMap);
    }

    private static String getJoin(String expExprTableName) {
        log.entry(expExprTableName);
        
        StringBuilder sb = new StringBuilder();
        String expressionIdField = getSelectExprFromAttribute(
                ExperimentExpressionDAO.Attribute.EXPRESSION_ID, colToAttrMap);
        sb.append(" INNER JOIN ").append(MySQLRawExpressionCallDAO.EXPR_TABLE_NAME).append(" ON ")
          .append(MySQLRawExpressionCallDAO.EXPR_TABLE_NAME).append(".").append(expressionIdField)
          .append(" = ").append(expExprTableName).append(".").append(expressionIdField);
        
        return log.traceExit(sb.toString());
    }
    private static String getWhere(Set<Integer> geneIds) {
        log.traceEntry();
        StringBuilder sb = new StringBuilder();
        sb.append(" WHERE ").append(MySQLRawExpressionCallDAO.EXPR_TABLE_NAME)
          .append(".").append(MySQLGeneDAO.BGEE_GENE_ID).append(" IN (")
          .append(BgeePreparedStatement.generateParameterizedQueryString(geneIds.size())).append(") ");
        return log.traceExit(sb.toString());
    }
    private static String getOrderBy() {
        log.traceEntry();
        String expressionIdField = getSelectExprFromAttribute(
                ExperimentExpressionDAO.Attribute.EXPRESSION_ID, colToAttrMap);
        StringBuilder sb = new StringBuilder();
        sb.append(" ORDER BY ")
          .append(MySQLRawExpressionCallDAO.EXPR_TABLE_NAME).append(".").append(MySQLGeneDAO.BGEE_GENE_ID)
          .append(", ").append(MySQLRawExpressionCallDAO.EXPR_TABLE_NAME).append(".").append(expressionIdField);
        return log.traceExit(sb.toString());
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
            Collection<Integer> geneIds) throws DAOException, IllegalArgumentException {
        log.entry(geneIds);
        
        if (geneIds == null || geneIds.isEmpty() || geneIds.stream().anyMatch(id -> id == null)) {
            throw log.throwing(new IllegalArgumentException("No gene IDs or null gene ID provided"));
        }
        Set<Integer> clonedGeneIds = new HashSet<>(geneIds);

        String tableName = "microarrayExperimentExpression";
        
        StringBuilder sb = new StringBuilder("SELECT ")
            //This field is retrieved solely to fix #173, we do not need it.
            .append(MySQLRawExpressionCallDAO.EXPR_TABLE_NAME).append(".").append(MySQLGeneDAO.BGEE_GENE_ID)

            .append(", ").append(tableName).append(".").append(
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
            .append(getJoin(tableName))
            .append(getWhere(clonedGeneIds))
            .append(getOrderBy());
        
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sb.toString());
            stmt.setIntegers(1, clonedGeneIds, true);
            return log.traceExit(new MySQLExperimentExpressionTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public ExperimentExpressionTOResultSet getESTExpExprsOrderedByGeneIdAndExprId(
            Collection<Integer> geneIds) throws DAOException, IllegalArgumentException {
        log.entry(geneIds);
        
        if (geneIds == null || geneIds.isEmpty() || geneIds.stream().anyMatch(id -> id == null)) {
            throw log.throwing(new IllegalArgumentException("No gene IDs or null gene ID provided"));
        }
        Set<Integer> clonedGeneIds = new HashSet<>(geneIds);

        String tableName = "estLibraryExpression";
        
        //Note: EST libraries are only used to generate present expression calls, 
        //so the table fields are slightly different
        StringBuilder sb = new StringBuilder("SELECT ")
            //This field is retrieved solely to fix #173, we do not need it.
            .append(MySQLRawExpressionCallDAO.EXPR_TABLE_NAME).append(".").append(MySQLGeneDAO.BGEE_GENE_ID)

            .append(", ").append(tableName).append(".").append(
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
            .append(getJoin(tableName))
            .append(getWhere(clonedGeneIds))
            .append(getOrderBy());
        
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sb.toString());
            stmt.setIntegers(1, clonedGeneIds, true);
            return log.traceExit(new MySQLExperimentExpressionTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public ExperimentExpressionTOResultSet getInSituExpExprsOrderedByGeneIdAndExprId(
            Collection<Integer> geneIds) throws DAOException, IllegalArgumentException {
        log.entry(geneIds);
        
        if (geneIds == null || geneIds.isEmpty() || geneIds.stream().anyMatch(id -> id == null)) {
            throw log.throwing(new IllegalArgumentException("No gene IDs or null gene ID provided"));
        }
        Set<Integer> clonedGeneIds = new HashSet<>(geneIds);

        String tableName = "inSituExperimentExpression";
        
        StringBuilder sb = new StringBuilder("SELECT ")
            //This field is retrieved solely to fix #173, we do not need it.
            .append(MySQLRawExpressionCallDAO.EXPR_TABLE_NAME).append(".").append(MySQLGeneDAO.BGEE_GENE_ID)

            .append(", ").append(tableName).append(".").append(
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
            .append(getJoin(tableName))
            .append(getWhere(clonedGeneIds))
            .append(getOrderBy());
        
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sb.toString());
            stmt.setIntegers(1, clonedGeneIds, true);
            return log.traceExit(new MySQLExperimentExpressionTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public ExperimentExpressionTOResultSet getRNASeqExpExprsOrderedByGeneIdAndExprId(
            Collection<Integer> geneIds) throws DAOException, IllegalArgumentException {
        log.entry(geneIds);
        
        if (geneIds == null || geneIds.isEmpty() || geneIds.stream().anyMatch(id -> id == null)) {
            throw log.throwing(new IllegalArgumentException("No gene IDs or null gene ID provided"));
        }
        Set<Integer> clonedGeneIds = new HashSet<>(geneIds);

        String tableName = "rnaSeqExperimentExpression";
        
        StringBuilder sb = new StringBuilder("SELECT ")
            //This field is retrieved solely to fix #173, we do not need it.
            .append(MySQLRawExpressionCallDAO.EXPR_TABLE_NAME).append(".").append(MySQLGeneDAO.BGEE_GENE_ID)

            .append(", ").append(tableName).append(".").append(
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
            .append(getJoin(tableName))
            .append(getWhere(clonedGeneIds))
            .append(getOrderBy());
        
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sb.toString());
            stmt.setIntegers(1, clonedGeneIds, true);
            return log.traceExit(new MySQLExperimentExpressionTOResultSet(stmt));
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
                log.traceEntry();
                final ResultSet currentResultSet = this.getCurrentResultSet();
                Integer exprId = null, presentHighCount = null, presentLowCount = null, 
                        absentHighCount = null, absentLowCount = null;
                String experimentId = null;
                CallDirection callDirection = null;
                CallQuality callQuality = null;

                for (Map.Entry<Integer, String> col : this.getColumnLabels().entrySet()) {
                    String columnName = col.getValue();
                    if (MySQLGeneDAO.BGEE_GENE_ID.equals(columnName)) {
                        //this column is retrieved solely to fix #173, we do not need it
                        continue;
                    }
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
                return log.traceExit(new ExperimentExpressionTO(exprId, experimentId, 
                        presentHighCount, presentLowCount, absentHighCount, absentLowCount,
                        callQuality, callDirection));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }
}
