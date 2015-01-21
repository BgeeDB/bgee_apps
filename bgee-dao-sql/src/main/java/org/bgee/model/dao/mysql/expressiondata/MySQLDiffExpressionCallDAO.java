package org.bgee.model.dao.mysql.expressiondata;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.EnumSet;
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
    public MySQLDiffExpressionCallDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public DiffExpressionCallTOResultSet getDiffExpressionCalls(DiffExpressionCallParams params) 
            throws DAOException {
        log.entry(params);
        return log.exit(getDiffExpressionCalls(params.getSpeciesIds())); 
    }

    /**
     * Retrieve differential expression calls from data source according to a {@code Set} of 
     * {@code String}s that are the IDs of species allowing to filter the calls to use. 
     * <p>
     * The differential expression calls are retrieved and returned as a 
     * {@code DiffExpressionCallTOResultSet}. It is the responsibility of the caller to close this 
     * {@code DAOResultSet} once results are retrieved.
     * 
     * @param speciesIds             A {@code Set} of {@code String}s that are the IDs of species 
     *                               allowing to filter the calls to use
     * @return                       An {@code ExpressionCallTOResultSet} containing all expression 
     *                               calls from data source.
     * @throws DAOException          If a {@code SQLException} occurred while trying to get 
     *                               expression calls.                      
     */
    private DiffExpressionCallTOResultSet getDiffExpressionCalls(Set<String> speciesIds) {
        log.entry(speciesIds);

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

        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        BgeePreparedStatement stmt = null;
        try {
            stmt = this.getManager().getConnection().prepareStatement(sql.toString());
            if (speciesIds != null && speciesIds.size() > 0) {
                List<Integer> orderedSpeciesIds = MySQLDAO.convertToIntList(speciesIds);
                Collections.sort(orderedSpeciesIds);
                stmt.setIntegers(1, orderedSpeciesIds);
            }             
            return log.exit(new MySQLDiffExpressionCallTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
        
    }

    private String generateSelectClause(
                    Set<DiffExpressionCallDAO.Attribute> attributes, String diffExprTableName) {
        log.entry(attributes, diffExprTableName);
        
        //the query construct is so complex that we always iterate each Attribute in any case
        if (attributes == null || attributes.isEmpty()) {
            attributes = EnumSet.allOf(DiffExpressionCallDAO.Attribute.class);
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
            } else if (attribute.equals(DiffExpressionCallDAO.Attribute.CONSISTENT_DEA_COUNT_AFFYMETRIX)) {
                sql += "consistentDEACountAffymetrix";
            } else if (attribute.equals(DiffExpressionCallDAO.Attribute.INCONSISTENT_DEA_COUNT_AFFYMETRIX)) {
                sql += "inconsistentDEACountAffymetrix";
            } else if (attribute.equals(DiffExpressionCallDAO.Attribute.DIFF_EXPR_CALL_RNA_SEQ)) {
                sql += "diffExprCallRNASeq";
            } else if (attribute.equals(DiffExpressionCallDAO.Attribute.DIFF_EXPR_RNA_SEQ_DATA)) {
                sql += "diffExprRNASeqData";
            } else if (attribute.equals(DiffExpressionCallDAO.Attribute.BEST_P_VALUE_RNA_SEQ)) {
                sql += "bestPValueRNASeq";
            } else if (attribute.equals(DiffExpressionCallDAO.Attribute.CONSISTENT_DEA_COUNT_RNA_SEQ)) {
                sql += "consistentDEACountRNASeq";
            } else if (attribute.equals(DiffExpressionCallDAO.Attribute.INCONSISTENT_DEA_COUNT_RNA_SEQ)) {
                sql += "inconsistentDEACountRNASeq";
            } else {
                throw log.throwing(new IllegalStateException("The attribute provided (" +
                        attribute.toString() + ") is unknown for " + DiffExpressionCallDAO.class.getName()));
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
