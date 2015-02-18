package org.bgee.model.dao.mysql.expressiondata;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.ComparisonFactor;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.DiffExprCallType;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallParams;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;


/**
 * A {@code DiffExpressionCallDAO} for MySQL. 
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO
 * @since Bgee 13
 */
public class MySQLDiffExpressionCallDAO extends MySQLDAO<DiffExpressionCallDAO.Attribute>
                                        implements DiffExpressionCallDAO {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(MySQLDiffExpressionCallDAO.class.getName());

    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     * 
     * @param manager                       The {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException     If {@code manager} is {@code null}.
     */
    //XXX shouldn't it be a NullPointerException then? (also in other DAOs)
    //but IllegalArgumentException is thrown to indicate that a method has been passed 
    //an illegal or inappropriate argument.
    public MySQLDiffExpressionCallDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public DiffExpressionCallTOResultSet getDiffExpressionCalls(DiffExpressionCallParams params) 
            throws DAOException {
        log.entry(params);
        return log.exit(getDiffExpressionCalls(params.getSpeciesIds(), params.getComparisonFactor(), 
                params.getAffymetrixDiffExprCallTypes(), params.isIncludeAffymetrixTypes(),
                params.getRNASeqDiffExprCallTypes(), params.isIncludeRNASeqTypes(), 
                params.isSatisfyAllCallTypeConditions()));
    }

    /**
     * Retrieve differential expression calls from data source according to a {@code Set} of 
     * {@code String}s that are the IDs of species allowing to filter the calls to use. 
     * <p>
     * The differential expression calls are retrieved and returned as a 
     * {@code DiffExpressionCallTOResultSet}. It is the responsibility of the caller to close this 
     * {@code DAOResultSet} once results are retrieved.
     * 
     * @param speciesIds                    A {@code Set} of {@code String}s that are the IDs of 
     *                                      species allowing to filter the calls to use.
     * @param factor                        A {@code ComparisonFactor} that is the comparison factor
     *                                      allowing to filter the calls to use.
     * @param diffExprCallTypeAffymetrix    A {@code DiffExprCallType} that is the type of the 
     *                                      differential expression calls to be used for Affymetrix 
     *                                      data.
     * @param includeAffymetrixTypes        A {@code boolean} defining whether differential 
     *                                      expression call types from Affymetrix data should be 
     *                                      include or exclude.
     * @param diffExprCallTypeRNASeq        A {@code DiffExprCallType} that is the type of the 
     *                                      differential expression calls to be used for RNA-seq 
     *                                      data.
     * @param includeRNASeqTypes            A {@code boolean} defining whether differential 
     *                                      expression call types from RNA-seq data should be 
     *                                      include or exclude.
     * @param isSatisfyAllCallTypeCondition A {@code boolean} defining whether both requested
     *                                      minimum contributions have to be satisfied or at 
     *                                      least one of the two.
     * @return                              An {@code ExpressionCallTOResultSet} containing all 
     *                                      expression calls from data source.
     * @throws DAOException                 If a {@code SQLException} occurred while trying to get 
     *                                      expression calls.                      
     */
    private DiffExpressionCallTOResultSet getDiffExpressionCalls(Set<String> speciesIds,
            ComparisonFactor factor, Set<DiffExprCallType> diffExprCallTypeAffymetrix,
            boolean includeAffymetrixTypes, Set<DiffExprCallType> diffExprCallTypeRNASeq, 
            boolean includeRnaSeqTypes, boolean isSatisfyAllCallTypeCondition) {
        log.entry(speciesIds, factor, diffExprCallTypeAffymetrix, includeAffymetrixTypes, 
                diffExprCallTypeRNASeq, includeRnaSeqTypes, isSatisfyAllCallTypeCondition);

        // Construct sql query
        String diffExprTableName = "differentialExpression";

        String sql = this.generateSelectClause(this.getAttributes(), diffExprTableName);

        if (speciesIds != null && speciesIds.size() > 0) {
            //the MySQL optimizer sucks and do the join in the wrong order, 
            //when species are requested. So we use the STRAIGHT_JOIN clause, and order 
            //the tables appropriately (gene table first).
            //TODO: this order might not be optimal if other filtering options are added 
            //in the future (not based only on speciesIds)
            sql += " FROM gene STRAIGHT_JOIN " + diffExprTableName + 
                    " ON (gene.geneId = " + diffExprTableName + ".geneId) " +
                    
                    " WHERE gene.speciesId IN (" +
                    BgeePreparedStatement.generateParameterizedQueryString(
                            speciesIds.size()) + ")";
        } else {
            sql += " FROM " + diffExprTableName;
        }
        
        boolean filterAffymetrixTypes = 
                (diffExprCallTypeAffymetrix != null && diffExprCallTypeAffymetrix.size() != 0 );
        boolean filterRNASeqTypes = 
                (diffExprCallTypeRNASeq != null && diffExprCallTypeRNASeq.size() != 0 );
        if (factor != null || filterAffymetrixTypes || filterRNASeqTypes) {
            if (speciesIds != null && speciesIds.size() > 0) {
                sql += " AND ";                
            } else {
                sql += " WHERE ";
            }
        }
        if (factor != null) {
            sql += diffExprTableName + ".comparisonFactor = ?";
        }
        int nbFilterCallType = 0;
        if (filterAffymetrixTypes) {
            nbFilterCallType++;
        }
        if (filterRNASeqTypes) {
            nbFilterCallType++;
        }
        assert nbFilterCallType < 3;
        if (factor != null && nbFilterCallType > 0) {
            sql += " AND ";
        }
        if (nbFilterCallType > 1) {
            sql += " (";
        }
        if (filterAffymetrixTypes) {
            sql += diffExprTableName + ".diffExprCallAffymetrix ";
            if (!includeAffymetrixTypes) {
                sql += "NOT";
            }
            sql += " IN (" + BgeePreparedStatement.generateParameterizedQueryString(
                            diffExprCallTypeAffymetrix.size()) + ")";
        }
        if (nbFilterCallType > 1) {
            if (isSatisfyAllCallTypeCondition) {
                sql += " AND ";
            } else {
                sql += " OR ";
            }
        } 
        if (filterRNASeqTypes) {
            sql += diffExprTableName + ".diffExprCallRNASeq ";
            if (!includeRnaSeqTypes) {
                sql += "NOT";
            }
            sql += " IN (" + BgeePreparedStatement.generateParameterizedQueryString(
                            diffExprCallTypeRNASeq.size()) + ")";
        }
        if (nbFilterCallType > 1) {
            sql += ")";
        }
        
        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        BgeePreparedStatement stmt = null;
        try {
            stmt = this.getManager().getConnection().prepareStatement(sql);
            int stmtIndex = 1;
            if (speciesIds != null && speciesIds.size() > 0) {
                List<Integer> orderedSpeciesIds = MySQLDAO.convertToIntList(speciesIds);
                Collections.sort(orderedSpeciesIds);
                stmt.setIntegers(stmtIndex, orderedSpeciesIds);
                stmtIndex += speciesIds.size();
            }             
            
            if (factor != null) {
                stmt.setString(stmtIndex, factor.getStringRepresentation());
                stmtIndex++;
            }
            
            if (filterAffymetrixTypes) {
                List<DiffExprCallType> orderedAffymetrixTypes = 
                        new ArrayList<DiffExprCallType>(diffExprCallTypeAffymetrix);
                Collections.sort(orderedAffymetrixTypes);
                stmt.setEnumDAOFields(stmtIndex, orderedAffymetrixTypes);
                stmtIndex += orderedAffymetrixTypes.size();
            }             
            
            if (filterRNASeqTypes) {
                List<DiffExprCallType> orderedRNASeqTypes = 
                        new ArrayList<DiffExprCallType>(diffExprCallTypeRNASeq);
                Collections.sort(orderedRNASeqTypes);
                stmt.setEnumDAOFields(stmtIndex, orderedRNASeqTypes);
                stmtIndex += orderedRNASeqTypes.size();
            }             
            
            return log.exit(new MySQLDiffExpressionCallTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    /**
     * Generates the SELECT clause of a MySQL query used to retrieve {@code DiffExpressionCallTO}s.
     * 
     * @param attributes            A {@code Set} of {@code Attribute}s defining 
     *                              the columns/information the query should retrieve.
     * @param diffExprTableName     A {@code String} defining the name of the differential
     *                              expression table used.
     * @return                      A {@code String} containing the SELECT clause 
     *                              for the requested query.
     */
    private String generateSelectClause(
                    Set<DiffExpressionCallDAO.Attribute> attributes, String diffExprTableName) {
        log.entry(attributes, diffExprTableName);
        
        if (attributes == null || attributes.isEmpty()) {
            return log.exit("SELECT * ");
        }
        
        String sql = "";
        for (DiffExpressionCallDAO.Attribute attribute: attributes) {
            if (sql.isEmpty()) {
                sql += "SELECT ";
                //does the attributes requested ensure that there will be 
                //no duplicated results?
                if (!attributes.contains(DiffExpressionCallDAO.Attribute.ID) &&  
                        (!attributes.contains(DiffExpressionCallDAO.Attribute.GENE_ID) || 
                            !attributes.contains(DiffExpressionCallDAO.Attribute.ANAT_ENTITY_ID) || 
                            !attributes.contains(DiffExpressionCallDAO.Attribute.STAGE_ID))) {
                    sql += "DISTINCT ";
                }
            } else {
                sql += ", ";
            }
            sql += diffExprTableName + ".";
            if (attribute.equals(DiffExpressionCallDAO.Attribute.ID)) {
                sql += "differentialExpressionId";
            } else if (attribute.equals(DiffExpressionCallDAO.Attribute.GENE_ID)) {
                sql += "geneId";
            } else if (attribute.equals(DiffExpressionCallDAO.Attribute.ANAT_ENTITY_ID)) {
                sql += "anatEntityId";
            } else if (attribute.equals(DiffExpressionCallDAO.Attribute.STAGE_ID)) {
                sql += "stageId";
            } else if (attribute.equals(DiffExpressionCallDAO.Attribute.COMPARISON_FACTOR)) {
                sql += "comparisonFactor";
            } else if (attribute.equals(DiffExpressionCallDAO.Attribute.DIFF_EXPR_CALL_AFFYMETRIX)) {
                sql += "diffExprCallAffymetrix";
            } else if (attribute.equals(DiffExpressionCallDAO.Attribute.DIFF_EXPR_AFFYMETRIX_DATA)) {
                sql += "diffExprAffymetrixData";
            } else if (attribute.equals(DiffExpressionCallDAO.Attribute.BEST_P_VALUE_AFFYMETRIX)) {
                sql += "bestPValueAffymetrix";
            } else if (attribute.equals(
                    DiffExpressionCallDAO.Attribute.CONSISTENT_DEA_COUNT_AFFYMETRIX)) {
                sql += "consistentDEACountAffymetrix";
            } else if (attribute.equals(
                    DiffExpressionCallDAO.Attribute.INCONSISTENT_DEA_COUNT_AFFYMETRIX)) {
                sql += "inconsistentDEACountAffymetrix";
            } else if (attribute.equals(DiffExpressionCallDAO.Attribute.DIFF_EXPR_CALL_RNA_SEQ)) {
                sql += "diffExprCallRNASeq";
            } else if (attribute.equals(DiffExpressionCallDAO.Attribute.DIFF_EXPR_RNA_SEQ_DATA)) {
                sql += "diffExprRNASeqData";
            } else if (attribute.equals(DiffExpressionCallDAO.Attribute.BEST_P_VALUE_RNA_SEQ)) {
                sql += "bestPValueRNASeq";
            } else if (attribute.equals(
                    DiffExpressionCallDAO.Attribute.CONSISTENT_DEA_COUNT_RNA_SEQ)) {
                sql += "consistentDEACountRNASeq";
            } else if (attribute.equals(
                    DiffExpressionCallDAO.Attribute.INCONSISTENT_DEA_COUNT_RNA_SEQ)) {
                sql += "inconsistentDEACountRNASeq";
            } else {
                throw log.throwing(new IllegalStateException("The attribute provided (" +
                        attribute.toString() + ") is unknown for " + 
                        DiffExpressionCallDAO.class.getName()));
            }
        }
        return log.exit(sql);
    }
    
    /**
     * A {@code MySQLDAOResultSet} specific to {@code DiffExpressionCallTO}.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public class MySQLDiffExpressionCallTOResultSet extends MySQLDAOResultSet<DiffExpressionCallTO> 
                                                    implements DiffExpressionCallTOResultSet {

        /**
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)}
         * super constructor.
         * 
         * @param statement The first {@code BgeePreparedStatement} to execute a query on.
         */
        private MySQLDiffExpressionCallTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }
        /**
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement, 
         * int, int, int)} super constructor.
         * 
         * @param statement             The first {@code BgeePreparedStatement} to execute 
         *                              a query on.
         * @param offsetParamIndex      An {@code int} that is the index of the parameter 
         *                              defining the offset argument of a LIMIT clause, 
         *                              in the SQL query hold by {@code statement}.
         * @param rowCountParamIndex    An {@code int} that is the index of the parameter 
         *                              specifying the maximum number of rows to return 
         *                              in a LIMIT clause, in the SQL query 
         *                              hold by {@code statement}.
         * @param rowCount              An {@code int} that is the maximum number of rows to use 
         *                              in a LIMIT clause, in the SQL query 
         *                              hold by {@code statement}.
         * @param filterDuplicates      A {@code boolean} defining whether equal {@code TransferObject}s 
         *                              returned by different queries should be filtered: 
         *                              when {@code true}, only 
         *                              one of them will be returned. This implies that all 
         *                              {@code TransferObject}s returned will be stored, implying 
         *                              potentially great memory usage.
         */
        private MySQLDiffExpressionCallTOResultSet(BgeePreparedStatement statement, 
                int offsetParamIndex, int rowCountParamIndex, int rowCount, 
                boolean filterDuplicates) {
            super(statement, offsetParamIndex, rowCountParamIndex, rowCount, filterDuplicates);
        }

        @Override
        protected DiffExpressionCallTO getNewTO() throws DAOException {
            log.entry();

            String id = null, geneId = null, anatEntityId = null, stageId = null;
            ComparisonFactor comparisonFactor = null;
            DataState diffExprAffymetrixData = null, diffExprRNASeqData = null;
            DiffExprCallType diffExprCallTypeAffymetrix = null, diffExprCallTypeRNASeq = null;
            Float bestPValueAffymetrix = null, bestPValueRNASeq = null;
            Integer consistentDEACountAffymetrix = null, inconsistentDEACountAffymetrix = null,
                    consistentDEACountRNASeq = null, inconsistentDEACountRNASeq = null;

            ResultSet currentResultSet = this.getCurrentResultSet();
            //every call to values() returns a newly cloned array, so we cache the array
            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("differentialExpressionId")) {
                        id = currentResultSet.getString(column.getKey());

                    } else if (column.getValue().equals("geneId")) {
                        geneId = currentResultSet.getString(column.getKey());

                    } else if (column.getValue().equals("anatEntityId")) {
                        anatEntityId = currentResultSet.getString(column.getKey());

                    } else if (column.getValue().equals("stageId")) {
                        stageId = currentResultSet.getString(column.getKey());

                    } else if (column.getValue().equals("comparisonFactor")) {
                        comparisonFactor = ComparisonFactor.convertToComparisonFactor(
                                currentResultSet.getString(column.getKey()));
                        
                    } else if (column.getValue().equals("diffExprCallAffymetrix")) {
                        diffExprCallTypeAffymetrix = DiffExprCallType.convertToDiffExprCallType(
                                currentResultSet.getString(column.getKey()));

                    } else if (column.getValue().equals("diffExprAffymetrixData")) {
                        diffExprAffymetrixData = DataState.convertToDataState(
                                currentResultSet.getString(column.getKey()));

                    } else if (column.getValue().equals("bestPValueAffymetrix")) {
                        bestPValueAffymetrix = currentResultSet.getFloat(column.getKey());

                    } else if (column.getValue().equals("consistentDEACountAffymetrix")) {
                        consistentDEACountAffymetrix = currentResultSet.getInt(column.getKey());
                        
                    } else if (column.getValue().equals("inconsistentDEACountAffymetrix")) {
                        inconsistentDEACountAffymetrix = currentResultSet.getInt(column.getKey());
                        
                    } else if (column.getValue().equals("diffExprCallRNASeq")) {
                        diffExprCallTypeRNASeq = DiffExprCallType.convertToDiffExprCallType(
                                currentResultSet.getString(column.getKey()));
                        
                    } else if (column.getValue().equals("diffExprRNASeqData")) {
                        //index of the enum in the mysql database corresponds to the ordinal 
                        //of DataState + 1
                        diffExprRNASeqData = DataState.convertToDataState(
                                currentResultSet.getString(column.getKey()));
                        
                    } else if (column.getValue().equals("bestPValueRNASeq")) {
                        bestPValueRNASeq = currentResultSet.getFloat(column.getKey());
                        
                    } else if (column.getValue().equals("consistentDEACountRNASeq")) {
                        consistentDEACountRNASeq = currentResultSet.getInt(column.getKey());
                        
                    } else if (column.getValue().equals("inconsistentDEACountRNASeq")) {
                        inconsistentDEACountRNASeq = currentResultSet.getInt(column.getKey());
                    }

                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            return log.exit(new DiffExpressionCallTO(id, geneId, anatEntityId, stageId, 
                    comparisonFactor, diffExprCallTypeAffymetrix, diffExprAffymetrixData, 
                    bestPValueAffymetrix, consistentDEACountAffymetrix, inconsistentDEACountAffymetrix, 
                    diffExprCallTypeRNASeq, diffExprRNASeqData, bestPValueRNASeq, 
                    consistentDEACountRNASeq, inconsistentDEACountRNASeq));
        }
    }
}
