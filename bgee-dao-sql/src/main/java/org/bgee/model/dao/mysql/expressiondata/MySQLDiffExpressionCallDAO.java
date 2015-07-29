package org.bgee.model.dao.mysql.expressiondata;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.ComparisonFactor;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.DiffExprCallType;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallParams;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.MySQLOrderingDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;


/**
 * A {@code DiffExpressionCallDAO} for MySQL. 
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO
 * @since Bgee 13
 */
public class MySQLDiffExpressionCallDAO extends MySQLOrderingDAO<DiffExpressionCallDAO.Attribute, 
                                                            DiffExpressionCallDAO.OrderingAttribute>
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
        return log.exit(this.getDiffExpressionCalls(null, params.getSpeciesIds(), 
                params.getComparisonFactor(), 
                params.getAffymetrixDiffExprCallTypes(), params.isIncludeAffymetrixTypes(),
                params.getRNASeqDiffExprCallTypes(), params.isIncludeRNASeqTypes(), 
                params.isSatisfyAllCallTypeConditions()));
    }

    @Override
    //TODO: integration test
    public DiffExpressionCallTOResultSet getHomologousGenesDiffExpressionCalls(
            String taxonId, DiffExpressionCallParams params) throws DAOException {
        log.entry(taxonId, params);
        return log.exit(this.getDiffExpressionCalls(taxonId, params.getSpeciesIds(), 
                params.getComparisonFactor(), 
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
     * @param omaTaxonId                    A {@code String} that is the taxon id to be used to 
     *                                      retrieve calls for homologous genes. 
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
    private DiffExpressionCallTOResultSet getDiffExpressionCalls(
            String omaTaxonId, Set<String> speciesIds,
            ComparisonFactor factor, Set<DiffExprCallType> diffExprCallTypeAffymetrix,
            boolean includeAffymetrixTypes, Set<DiffExprCallType> diffExprCallTypeRNASeq, 
            boolean includeRnaSeqTypes, boolean isSatisfyAllCallTypeCondition) {
        log.entry(omaTaxonId, speciesIds, factor, diffExprCallTypeAffymetrix, includeAffymetrixTypes, 
                diffExprCallTypeRNASeq, includeRnaSeqTypes, isSatisfyAllCallTypeCondition);

        // Construct sql query
        String diffExprTableName = "differentialExpression";
        boolean distinct = false;
        if (!this.getAttributes().contains(DiffExpressionCallDAO.Attribute.ID) &&  
                (!this.getAttributes().contains(DiffExpressionCallDAO.Attribute.GENE_ID) || 
                 !this.getAttributes().contains(DiffExpressionCallDAO.Attribute.ANAT_ENTITY_ID) || 
                 !this.getAttributes().contains(DiffExpressionCallDAO.Attribute.STAGE_ID))) {
            distinct = true;
        }
        String sql = this.generateSelectClause(this.getAttributes(), diffExprTableName, distinct);

        boolean hasSpecies  = speciesIds != null && !speciesIds.isEmpty();
        boolean hasOMATaxon = StringUtils.isNotBlank(omaTaxonId);
        boolean orderTOsByOmaGroup = this.getOrderingAttributes().containsKey(
                DiffExpressionCallDAO.OrderingAttribute.OMA_GROUP);
        
        // For the moment, it's not possible to order by OrderingAttributes other than OMA_GROUP
        // because there is no other OrderingAttributes. But if we add an OrderingAttributes and 
        // forget to implement the feature, an exception will be thrown.
        if ((this.getOrderingAttributes().keySet().size() == 1 && !orderTOsByOmaGroup) ||
                this.getOrderingAttributes().keySet().size() > 2) {
            throw log.throwing(new UnsupportedOperationException("Operation not yet implemented, " +
                    "ordering is possible only by " + DiffExpressionCallDAO.OrderingAttribute.OMA_GROUP +
                    ". Provided set contains: " + this.getOrderingAttributes().keySet()));
        }
        
        String geneInfoTable = null;
        
        //either because we want to limit the results retrieved to some species, 
        //or because we want to order results by groups of homologous genes, 
        //we need to join additional tables. 
        sql += "FROM ";
        
        if (hasSpecies || hasOMATaxon || orderTOsByOmaGroup) {
            log.debug("hasOMATaxon = "+hasOMATaxon+" //// omaTaxonId="+omaTaxonId);
            if (hasOMATaxon || orderTOsByOmaGroup) {
                //we want to order results by groups of homologous genes, so we recover 
                //the correct OMA node IDs for the requested taxon
                geneInfoTable = "tempGene";
                // If taxon ID is not provided, we retrieve the OMAParentNodeId of the gene,
                // else we retrieve all OMA node IDs above each gene.
                String leftBoundComparison = "=";
                String rightBoundComparison = "=";
                if (hasOMATaxon) {
                    leftBoundComparison = ">=";
                    rightBoundComparison = "<=";
                }
                sql += "(SELECT DISTINCT t10.OMANodeId, t30.geneId "
                    + "FROM OMAHierarchicalGroup AS t10 "
                    + "INNER JOIN OMAHierarchicalGroup AS t20 ON "
                    + "t20.OMANodeLeftBound " + leftBoundComparison + " t10.OMANodeLeftBound AND " 
                    + "t20.OMANodeRightBound " + rightBoundComparison + " t10.OMANodeRightBound "
                    + "INNER JOIN gene AS t30 ON t20.OMANodeId = t30.OMAParentNodeId ";
                if (hasOMATaxon || hasSpecies) {
                    sql += "WHERE ";
                }
                if (hasOMATaxon) {
                    sql += "t10.taxonId = ? ";
                }
                if (hasOMATaxon && hasSpecies) {
                    sql += "AND ";
                }
                if (hasSpecies) {
                    sql += "t30.speciesId IN (" +
                            BgeePreparedStatement.generateParameterizedQueryString(
                                    speciesIds.size()) + ")";
                }
                sql += ") AS " + geneInfoTable;
            } else {
                //filter species considered by using info in the gene table
                geneInfoTable = "gene";
                sql += geneInfoTable;
            }
            
            //the MySQL optimizer sucks and do the join in the wrong order, 
            //when species are requested. So we use the STRAIGHT_JOIN clause, and order 
            //the tables appropriately (gene table first).
            //XXX: this order might not be optimal if other filtering options are added 
            //in the future (not based only on speciesIds)
            sql += " STRAIGHT_JOIN " + diffExprTableName + 
                    " ON " + geneInfoTable + ".geneId = " + diffExprTableName + ".geneId ";
            
            //if we want to retrieve or order results by groups of homologous genes, 
            //we have already filtered the species considered in the sub-query.
            if (!hasOMATaxon && !orderTOsByOmaGroup) {
                sql += " WHERE " + geneInfoTable + ".speciesId IN (" +
                        BgeePreparedStatement.generateParameterizedQueryString(
                                speciesIds.size()) + ")";
            }
        } else {
            //if no conditions on the species considered, no conditions on the taxon and  
            //no ordering by groups of homologous genes, we only use the diff expression table.
            sql += diffExprTableName;
        }
        
        boolean filterAffymetrixTypes = 
                (diffExprCallTypeAffymetrix != null && diffExprCallTypeAffymetrix.size() != 0 );
        boolean filterRNASeqTypes = 
                (diffExprCallTypeRNASeq != null && diffExprCallTypeRNASeq.size() != 0 );
        if (factor != null || filterAffymetrixTypes || filterRNASeqTypes) {
            if (hasSpecies && !hasOMATaxon && !orderTOsByOmaGroup) {
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
        
        if (orderTOsByOmaGroup) {
            sql += " ORDER BY " + geneInfoTable + ".OMANodeId ";
            // The default sort order is ascending, so if Direction of OMA_GROUP is DESC, 
            // we need to specified it.
            if (this.getOrderingAttributes().get(DiffExpressionCallDAO.OrderingAttribute.OMA_GROUP).
                    equals(Direction.DESC)) {
                sql += " DESC";                
            }
        }
        
        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        BgeePreparedStatement stmt = null;
        try {
            stmt = this.getManager().getConnection().prepareStatement(sql);
            int stmtIndex = 1;
            if (hasOMATaxon) {
                stmt.setString(1, omaTaxonId);
                stmtIndex = 2;
            }
            if (hasSpecies) {
                List<Integer> orderedSpeciesIds = MySQLDAO.convertToOrderedIntList(speciesIds);
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
     * @param distinct              A {@code boolean} defining whether the 'DISTINCT' option 
     *                              should be used in the 'SELECT' clause.
     * @return                      A {@code String} containing the SELECT clause 
     *                              for the requested query.
     * @throws IllegalArgumentException If one {@code Attribute} of {@code attributes} is unknown.
     */
    private String generateSelectClause(
                    Set<DiffExpressionCallDAO.Attribute> attributes, String diffExprTableName, 
                    boolean distinct) throws IllegalArgumentException {
        log.entry(attributes, diffExprTableName, distinct);
        
        if (attributes == null || attributes.isEmpty()) {
            return log.exit("SELECT " + diffExprTableName + ".* ");
        }
        
        String sql = "";
        for (DiffExpressionCallDAO.Attribute attribute: attributes) {
            if (sql.isEmpty()) {
                sql += "SELECT ";
                if (distinct) {
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
                throw log.throwing(new IllegalArgumentException("The attribute provided (" +
                        attribute.toString() + ") is unknown for " + 
                        DiffExpressionCallDAO.class.getName()));
            }
        }
        sql += " ";
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

            //every call to values() returns a newly cloned array, so we cache the array
            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("differentialExpressionId")) {
                        id = this.getCurrentResultSet().getString(column.getKey());

                    } else if (column.getValue().equals("geneId")) {
                        geneId = this.getCurrentResultSet().getString(column.getKey());

                    } else if (column.getValue().equals("anatEntityId")) {
                        anatEntityId = this.getCurrentResultSet().getString(column.getKey());

                    } else if (column.getValue().equals("stageId")) {
                        stageId = this.getCurrentResultSet().getString(column.getKey());

                    } else if (column.getValue().equals("comparisonFactor")) {
                        comparisonFactor = ComparisonFactor.convertToComparisonFactor(
                                this.getCurrentResultSet().getString(column.getKey()));
                        
                    } else if (column.getValue().equals("diffExprCallAffymetrix")) {
                        diffExprCallTypeAffymetrix = DiffExprCallType.convertToDiffExprCallType(
                                this.getCurrentResultSet().getString(column.getKey()));

                    } else if (column.getValue().equals("diffExprAffymetrixData")) {
                        diffExprAffymetrixData = DataState.convertToDataState(
                                this.getCurrentResultSet().getString(column.getKey()));

                    } else if (column.getValue().equals("bestPValueAffymetrix")) {
                        bestPValueAffymetrix = this.getCurrentResultSet().getFloat(
                                column.getKey());

                    } else if (column.getValue().equals("consistentDEACountAffymetrix")) {
                        consistentDEACountAffymetrix = this.getCurrentResultSet().getInt(
                                column.getKey());
                        
                    } else if (column.getValue().equals("inconsistentDEACountAffymetrix")) {
                        inconsistentDEACountAffymetrix = this.getCurrentResultSet().getInt(
                                column.getKey());
                        
                    } else if (column.getValue().equals("diffExprCallRNASeq")) {
                        diffExprCallTypeRNASeq = DiffExprCallType.convertToDiffExprCallType(
                                this.getCurrentResultSet().getString(column.getKey()));
                        
                    } else if (column.getValue().equals("diffExprRNASeqData")) {
                        //index of the enum in the mysql database corresponds to the ordinal 
                        //of DataState + 1
                        diffExprRNASeqData = DataState.convertToDataState(
                                this.getCurrentResultSet().getString(column.getKey()));
                        
                    } else if (column.getValue().equals("bestPValueRNASeq")) {
                        bestPValueRNASeq = this.getCurrentResultSet().getFloat(column.getKey());
                        
                    } else if (column.getValue().equals("consistentDEACountRNASeq")) {
                        consistentDEACountRNASeq = this.getCurrentResultSet().getInt(
                                column.getKey());
                        
                    } else if (column.getValue().equals("inconsistentDEACountRNASeq")) {
                        inconsistentDEACountRNASeq = this.getCurrentResultSet().getInt(
                                column.getKey());
                    } else {
                        throw log.throwing(new UnrecognizedColumnException(column.getValue()));
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
