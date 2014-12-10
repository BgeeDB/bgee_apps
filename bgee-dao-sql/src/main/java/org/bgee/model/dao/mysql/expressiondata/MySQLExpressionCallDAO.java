package org.bgee.model.dao.mysql.expressiondata;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO.OriginOfLine;
import org.bgee.model.dao.api.expressiondata.ExpressionCallParams;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;

/**
 * A {@code ExpressionCallDAO} for MySQL. 
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO
 * @since Bgee 13
 */
public class MySQLExpressionCallDAO extends MySQLDAO<ExpressionCallDAO.Attribute> 
                                    implements ExpressionCallDAO {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(MySQLExpressionCallDAO.class.getName());

    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     * 
     * @param manager                       The {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException     If {@code manager} is {@code null}.
     */
    public MySQLExpressionCallDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public ExpressionCallTOResultSet getExpressionCalls(ExpressionCallParams params) 
            throws DAOException {
        log.entry(params);
        return log.exit(getExpressionCalls(
                params.getSpeciesIds(), params.isIncludeSubstructures(), params.isIncludeSubStages())); 
        
//        String sql = "{call getAllExpression(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
//
//        //we don't use a try-with-resource, because we return a pointer to the results, 
//        //not the actual results, so we should not close this BgeeCallableStatement.
//        BgeeCallableStatement callStmt = null;
//        try {
//            callStmt = this.getManager().getConnection().prepareCall(sql);
//            callStmt.setString(1, createStringFromSet(params.getGeneIds(), '|'));
//            callStmt.setString(2, createStringFromSet(params.getAnatEntityIds(), '|'));
//            callStmt.setString(3, createStringFromSet(params.getDevStageIds(), '|'));
//            callStmt.setString(4, createStringFromSet(params.getSpeciesIds(), '|'));
//            callStmt.setInt(5, CallTO.getMinLevelData(params.getAffymetrixData(), '|'));
//            callStmt.setInt(6, CallTO.getMinLevelData(params.getESTData()));
//            callStmt.setInt(7, CallTO.getMinLevelData(params.getInSituData()));
//            callStmt.setInt(8, CallTO.getMinLevelData(params.getRNASeqData()));
//            callStmt.setBoolean(9, params.isIncludeSubStages());
//            callStmt.setBoolean(10, params.isIncludeSubstructures());
//            callStmt.setBoolean(11, params.isAllDataTypes());
//            callStmt.setBoolean(12, params.isUseAnatDescendants());
//            callStmt.setBoolean(13, params.isUseDevDescendants());
//            return log.exit(new MySQLExpressionCallTOResultSet(callStmt));
//        } catch (SQLException e) {
//            throw log.throwing(new DAOException(e));
//        }
    }

    @Override
    public int getMaxExpressionCallId(boolean isIncludeSubstructures)
            throws DAOException {
        log.entry(isIncludeSubstructures);
        
        String tableName = "expression";
        if (isIncludeSubstructures) {
            tableName = "globalExpression";
        }        
        
        String id = "expressionId";
        if (isIncludeSubstructures) {
            id = "globalExpressionId";
        } 

        String sql = "SELECT MAX(" + id + ") AS " + id + " FROM " + tableName;
    
        try (BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql)) {
            MySQLExpressionCallTOResultSet resultSet = new MySQLExpressionCallTOResultSet(stmt);
            
            if (resultSet.next() && StringUtils.isNotBlank(resultSet.getTO().getId())) {
                return log.exit(Integer.valueOf(resultSet.getTO().getId()));
            } 
            return log.exit(0);
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    /**
     * Retrieve expression calls from data source according to a {@code Set} of {@code String}s 
     * that are the IDs of species allowing to filter the calls to use, and a {@code boolean} 
     * defining whether this expression call was generated using data from the anatomical entity 
     * with the ID {@link CallTO#getAnatEntityId()} alone, or by also considering all its 
     * descendants by <em>is_a</em> or <em>part_of</em> relations, even indirect. 
     * <p>
     * The expression calls are retrieved and returned as a {@code ExpressionCallTOResultSet}. 
     * It is the responsibility of the caller to close this {@code DAOResultSet} once 
     * results are retrieved.
     * 
     * @param speciesIds             A {@code Set} of {@code String}s that are the IDs of species 
     *                               allowing to filter the calls to use
     * @param isIncludeSubstructures A {@code boolean} defining whether descendants 
     *                               of the anatomical entity were considered.
     * @return                       An {@code ExpressionCallTOResultSet} containing all expression 
     *                               calls from data source.
     * @throws DAOException          If a {@code SQLException} occurred while trying to get 
     *                               expression calls.                      
     */
    private ExpressionCallTOResultSet getExpressionCalls(Set<String> speciesIds, 
            boolean isIncludeSubstructures, boolean isIncludeSubStages) throws DAOException {
        log.entry(speciesIds, isIncludeSubstructures, isIncludeSubStages);

        // Construct sql query
        String exprTableName = "expression";
        if (isIncludeSubstructures) {
            exprTableName = "globalExpression";
        }
        String propagatedStageTableName = "propagatedStage";

        String sql = this.generateSelectClause(this.getAttributes(), 
                isIncludeSubstructures, isIncludeSubStages, 
                exprTableName, propagatedStageTableName);
        
        sql += " FROM " + exprTableName;

        if (isIncludeSubStages) {
            //propagate expression calls to parent stages.
            sql += " INNER JOIN stage ON " + exprTableName + ".stageId = stage.stageId " +
                   " INNER JOIN stage AS " + propagatedStageTableName + " ON " +
                       propagatedStageTableName + ".stageLeftBound <= stage.stageLeftBound AND " +
                       propagatedStageTableName + ".stageRightBound >= stage.stageRightBound ";
            //there are too much data to propagate all expression calls on the fly at once, 
            //so we need to do it using several queries, group of genes by group of genes.
            //So we need to identify genes with expression data, and to retrieve them 
            //with a LIMIT clause. We cannot use a subquery with a LIMIT in a IN clause 
            //(limitation of MySQL as of version 5.5), so we use the subquery to create 
            //a temporary table to join to in the main query. 
            //In the subquery, the EXISTS clause is used to speed-up the main query, 
            //to make sure we will not look up for data not present in the expression table.
            //here, we generate the first part of the subquery, as we may need 
            //to set speciesIds afterwards.
            sql += " INNER JOIN (SELECT geneId from gene where exists " +
            		"(select 1 from expression where expression.geneId = gene.geneId) ";
        }
        if (speciesIds != null && speciesIds.size() > 0) {
            if (isIncludeSubStages) {
                sql += "AND ";
            } else {
                sql += " INNER JOIN gene ON (gene.geneId = " + exprTableName + ".geneId) WHERE ";
            }
            sql += "gene.speciesId IN (" + BgeePreparedStatement.generateParameterizedQueryString(
                            speciesIds.size()) + ") ";
        }
        if (isIncludeSubStages) {
            //finish the subquery and the join to the expression table
            sql += "LIMIT ?, ?) as tempTable on " + exprTableName + ".geneId = tempTable.geneId ";
            //and now, finish the main query
            sql += " GROUP BY " + exprTableName + ".geneId, " + 
                   exprTableName + ".anatEntityId, " + propagatedStageTableName + ".stageId";
        }

        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            if (speciesIds != null && speciesIds.size() > 0) {
                List<Integer> orderedSpeciesIds = MySQLDAO.convertToIntList(speciesIds);
                Collections.sort(orderedSpeciesIds);
                stmt.setIntegers(1, orderedSpeciesIds);
            }

            if (!isIncludeSubStages) {
                return log.exit(new MySQLExpressionCallTOResultSet(stmt));
            } 
            
            int offsetParamIndex = (speciesIds == null ? 1: speciesIds.size() + 1);
            int rowCountParamIndex = offsetParamIndex + 1;
            int rowCount = this.getManager().getExprPropagationGeneCount();
            //if attributes requested are such that there cannot be duplicated results, 
            //we do not need to filter duplicates on the application side (which has 
            //a huge memory cost); also, if geneIds were requested, there cannot be 
            //duplicates between queries (duplicates inside a query will be filtered by 
            //the DISTINCT clause), so, no need to filter on the application side.
            boolean filterDuplicates = 
                    this.getAttributes() != null && !this.getAttributes().isEmpty() && 
                    !this.getAttributes().contains(ExpressionCallDAO.Attribute.ID) && 
                    !this.getAttributes().contains(ExpressionCallDAO.Attribute.GENE_ID);
            
            return log.exit(new MySQLExpressionCallTOResultSet(stmt, offsetParamIndex, 
                    rowCountParamIndex, rowCount, filterDuplicates));
            
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }
    
    private String generateSelectClause(Collection<ExpressionCallDAO.Attribute> attributes, 
            boolean includeSubstructures, boolean includeSubStages, 
            String exprTableName, String propagatedStageTableName) {
        log.entry(attributes, includeSubstructures, includeSubStages, 
                exprTableName, propagatedStageTableName);
        
        String sql = "";
        //the query construct is so complex that we always iterate each Attribute in any case
        if (attributes == null || attributes.isEmpty()) {
            attributes = EnumSet.allOf(ExpressionCallDAO.Attribute.class);
        }
        for (ExpressionCallDAO.Attribute attribute: attributes) {
            if (sql.isEmpty()) {
                sql += "SELECT ";
                //does the attributes requested ensure that there will be 
                //no duplicated results?
                if (!attributes.contains(ExpressionCallDAO.Attribute.ID) &&  
                        (!attributes.contains(ExpressionCallDAO.Attribute.GENE_ID) || 
                            !attributes.contains(ExpressionCallDAO.Attribute.ANAT_ENTITY_ID) || 
                            !attributes.contains(ExpressionCallDAO.Attribute.STAGE_ID))) {
                    sql += "DISTINCT ";
                }
            } else {
                sql += ", ";
            }
            
            if (attribute.equals(ExpressionCallDAO.Attribute.ID)) {
                String colName = "expressionId ";
                if (includeSubstructures) {
                    colName = "globalExpressionId ";
                }
                //in case we include sub-stages, we need to generate fake IDs,  
                //because equality of ExpressionCallTO can be based on ID, and here 
                //a same basic call with a given ID can be associated to different propagated calls.
                if (includeSubStages) {
                    log.warn("Retrieval of expression IDs with on-the-fly propagation " +
                    		"of expression calls, can increase memory usage.");
                    //TODO: transform into a bit value for lower memory consumption?
                    //see convert unsigned: http://dev.mysql.com/doc/refman/5.5/en/cast-functions.html#function_convert
                    sql += "CONCAT(" + exprTableName + ".geneId, '__', " + 
                            exprTableName + ".anatEntityId, '__', " + 
                            propagatedStageTableName + ".stageId) AS " + colName;
                } else {
                    sql += colName;
                }
            } else if (attribute.equals(ExpressionCallDAO.Attribute.GENE_ID)) {
                sql += exprTableName + ".geneId ";
            } else if (attribute.equals(ExpressionCallDAO.Attribute.ANAT_ENTITY_ID)) {
                sql += exprTableName + ".anatEntityId ";
                
            } else if (attribute.equals(ExpressionCallDAO.Attribute.STAGE_ID)) {
                if (!includeSubStages) {
                    sql += exprTableName + ".stageId ";
                } else {
                    sql += propagatedStageTableName + ".stageId ";
                }
            } else if (attribute.equals(ExpressionCallDAO.Attribute.ANAT_ORIGIN_OF_LINE)) {
                //the attribute ANAT_ORIGIN_OF_LINE corresponds to a column only 
                //in the global expression table, not in the basic expression table. 
                //So, if no propagation was requested, we add a fake column to the query 
                //to provide the information to the ResultSet consistently. 
                if (!includeSubstructures) {
                    sql += "'" + OriginOfLine.SELF.getStringRepresentation() + "' ";
                } else {
                    //otherwise, we use the real column in the global expression table
                    sql += "originOfLine ";
                }
                sql += "AS anatOriginOfLine ";
            } else if (attribute.equals(ExpressionCallDAO.Attribute.INCLUDE_SUBSTRUCTURES)) {
                //the Attributes INCLUDE_SUBSTRUCTURES does not correspond to any columns 
                //in a table, but it allow to determine how the TOs returned were generated. 
                //The TOs returned by the ResultSet will have these values set to null 
                //by default. So, we add fake columns to the query to provide 
                //the information to the ResultSet. 
                if (includeSubstructures) {
                    sql += "1 ";
                } else {
                    sql += "0 ";
                }
                sql += "AS includeSubstructures ";
                
            } else if (attribute.equals(ExpressionCallDAO.Attribute.STAGE_ORIGIN_OF_LINE)) {
                //the attribute STAGE_ORIGIN_OF_LINE does not correspond to any column. 
                //We add a fake column to the query to compute this information. 
                if (!includeSubStages) {
                    sql += "'" + OriginOfLine.SELF.getStringRepresentation() + "' ";
                } else {
                    //here, this gets complicated. We need to know the stages that allowed 
                    //to generate a propagated expression line. We use group_concat.
                    sql +=  //if the concatenation of all stage IDs allowing to generate 
                            //the current line contains only the propagated stage, 
                            //origin of line = SELF
                            "IF (GROUP_CONCAT(distinct " + exprTableName + ".stageId) = " +
                            propagatedStageTableName + ".stageId, " +
                                "'" + OriginOfLine.SELF.getStringRepresentation() + "', " + 
                                //otherwise, if the concatenation contains the propagated stage, 
                                //among other stages, origin of line = BOTH. 
                                //in order to not need to exceed the max size 
                                //of group_concat_max_len, we order the stages to get 
                                //the propagated stage first, so we're sure it's not truncated
                                "IF (GROUP_CONCAT(distinct " + exprTableName + ".stageId ORDER BY " +
                                exprTableName + ".stageId = " + propagatedStageTableName + ".stageId DESC) " +
                                "LIKE CONCAT(" + propagatedStageTableName + ".stageId" + ", ',%'), " +
                                    "'" + OriginOfLine.BOTH.getStringRepresentation() + "', " +
                                    //otherwise, the concatenation contains only stages different from 
                                    //the propagated stage, origin of line = DESCENT
                                    "'" + OriginOfLine.DESCENT.getStringRepresentation() + "')) ";
                }
                sql += "AS stageOriginOfLine ";
            } else if (attribute.equals(ExpressionCallDAO.Attribute.INCLUDE_SUBSTAGES)) {
                //the Attributes INCLUDE_SUBSTAGES does not correspond to any columns 
                //in a table, but it allow to determine how the TOs returned were generated. 
                //The TOs returned by the ResultSet will have these values set to null 
                //by default. So, we add fake columns to the query to provide 
                //the information to the ResultSet. 
                if (includeSubStages) {
                    sql += "1 ";
                } else {
                    sql += "0 ";
                }
                sql += "AS includeSubStages ";
                
            } else if (attribute.equals(ExpressionCallDAO.Attribute.AFFYMETRIX_DATA)) {
                if (!includeSubStages) {
                    sql += "(affymetrixData + 0) ";
                } else {
                    //if expression is propagated to parent stages, we get the best value 
                    //from all expression calls group by the propagated stage
                    sql += "MAX(affymetrixData + 0) ";
                }
                sql += "AS affymetrixData ";
            } else if (attribute.equals(ExpressionCallDAO.Attribute.EST_DATA)) {
                if (!includeSubStages) {
                    sql += "(estData + 0) ";
                } else {
                    //if expression is propagated to parent stages, we get the best value 
                    //from all expression calls group by the propagated stage
                    sql += "MAX(estData + 0) ";
                }
                sql += "AS estData ";
            } else if (attribute.equals(ExpressionCallDAO.Attribute.IN_SITU_DATA)) {
                if (!includeSubStages) {
                    sql += "(inSituData + 0) ";
                } else {
                    //if expression is propagated to parent stages, we get the best value 
                    //from all expression calls group by the propagated stage
                    sql += "MAX(inSituData + 0) ";
                }
                sql += "AS inSituData ";
            } else if (attribute.equals(ExpressionCallDAO.Attribute.RNA_SEQ_DATA)) {
                if (!includeSubStages) {
                    sql += "(rnaSeqData + 0) ";
                } else {
                    //if expression is propagated to parent stages, we get the best value 
                    //from all expression calls group by the propagated stage
                    sql += "MAX(rnaSeqData + 0) ";
                }
                sql += "AS rnaSeqData ";
            } else {
                throw log.throwing(new IllegalArgumentException("The attribute provided (" +
                        attribute.toString() + ") is unknown for " + ExpressionCallDAO.class.getName()));
            }
        }
        return log.exit(sql);
    }

    @Override
    public int insertExpressionCalls(Collection<ExpressionCallTO> expressionCalls) 
            throws DAOException, IllegalArgumentException {
        log.entry(expressionCalls);
        
        if (expressionCalls == null || expressionCalls.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "No expression call is given, then no expression call is updated"));
        }

        int callInsertedCount = 0;
        int totalCallNumber = expressionCalls.size();
        
        // According to isIncludeSubstructures(), the ExpressionCallTO is inserted in 
        // expression or globalExpression table. As prepared statement is for the 
        // column values not for table name, we need to separate ExpressionCallTOs into
        // two separated collections. 
        Collection<ExpressionCallTO> toInsertInExpression = new ArrayList<ExpressionCallTO>();
        Collection<ExpressionCallTO> toInsertInGlobalExpression = new ArrayList<ExpressionCallTO>();
        for (ExpressionCallTO call: expressionCalls) {
            if (call.isIncludeSubstructures()) {
                toInsertInGlobalExpression.add(call);
            } else {
                toInsertInExpression.add(call);
            }
        }

        // And we need to build two different queries. 
        String sqlExpression = "INSERT INTO expression " +
                "(expressionId, geneId, anatEntityId, stageId, "+
                "estData, affymetrixData, inSituData, rnaSeqData) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?)";
        
        // To not overload MySQL with an error com.mysql.jdbc.PacketTooBigException, 
        // and because of laziness, we insert expression calls one at a time
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sqlExpression)) {
            for (ExpressionCallTO call: toInsertInExpression) {
                stmt.setInt(1, Integer.parseInt(call.getId()));
                stmt.setString(2, call.getGeneId());
                stmt.setString(3, call.getAnatEntityId());
                stmt.setString(4, call.getStageId());
                stmt.setString(5, call.getESTData().getStringRepresentation());
                stmt.setString(6, call.getAffymetrixData().getStringRepresentation());
                stmt.setString(7, call.getInSituData().getStringRepresentation());
                stmt.setString(8, call.getRNASeqData().getStringRepresentation());
                callInsertedCount += stmt.executeUpdate();
                stmt.clearParameters();
                if (log.isDebugEnabled() && callInsertedCount % 100000 == 0) {
                    log.debug("{}/{} expression calls inserted", callInsertedCount, 
                            totalCallNumber);
                }
            }
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }

        String sqlGlobalExpression = "INSERT INTO globalExpression " +
                "(globalExpressionId, geneId, anatEntityId, stageId, "+
                "estData, affymetrixData, inSituData, rnaSeqData, originOfLine) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sqlGlobalExpression)) {
            for (ExpressionCallTO call: toInsertInGlobalExpression) {
                stmt.setInt(1, Integer.parseInt(call.getId()));
                stmt.setString(2, call.getGeneId());
                stmt.setString(3, call.getAnatEntityId());
                stmt.setString(4, call.getStageId());
                stmt.setString(5, call.getESTData().getStringRepresentation());
                stmt.setString(6, call.getAffymetrixData().getStringRepresentation());
                stmt.setString(7, call.getInSituData().getStringRepresentation());
                stmt.setString(8, call.getRNASeqData().getStringRepresentation());
                stmt.setString(9, call.getAnatOriginOfLine().getStringRepresentation());
                callInsertedCount += stmt.executeUpdate();
                stmt.clearParameters();
                if (log.isDebugEnabled() && callInsertedCount % 100000 == 0) {
                    log.debug("{}/{} global expression calls inserted", callInsertedCount, 
                            totalCallNumber);
                }
            }
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
        return log.exit(callInsertedCount);
    }

    @Override
    public int insertGlobalExpressionToExpression(
            Collection<GlobalExpressionToExpressionTO> globalExpressionToExpression) 
                    throws DAOException, IllegalArgumentException {
        log.entry(globalExpressionToExpression);
        
        if (globalExpressionToExpression == null || globalExpressionToExpression.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "No global expression to expression is given, then nothing is updated"));
        }

        int rowInsertedCount = 0;
        int totalTONumber = globalExpressionToExpression.size();

        // And we need to build two different queries. 
        String sqlExpression = "INSERT INTO globalExpressionToExpression " +
                "(globalExpressionId, expressionId) values (?, ?)";
        
        // To not overload MySQL with an error com.mysql.jdbc.PacketTooBigException, 
        // and because of laziness, we insert rows one at a time
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sqlExpression)) {
            for (GlobalExpressionToExpressionTO call: globalExpressionToExpression) {
                stmt.setString(1, call.getGlobalExpressionId());
                stmt.setString(2, call.getExpressionId());
                rowInsertedCount += stmt.executeUpdate();
                stmt.clearParameters();
                if (log.isDebugEnabled() && rowInsertedCount % 100000 == 0) {
                    log.debug("{}/{} global expression to expression inserted", 
                            rowInsertedCount, totalTONumber);
                }
            }
            return log.exit(rowInsertedCount);
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    /**
     * A {@code MySQLDAOResultSet} specific to {@code ExpressionCallTO}.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public class MySQLExpressionCallTOResultSet extends MySQLDAOResultSet<ExpressionCallTO> 
                                                implements ExpressionCallTOResultSet {

        /**
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)}
         * super constructor.
         * 
         * @param statement The first {@code BgeePreparedStatement} to execute a query on.
         */
        private MySQLExpressionCallTOResultSet(BgeePreparedStatement statement) {
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
        private MySQLExpressionCallTOResultSet(BgeePreparedStatement statement, 
                int offsetParamIndex, int rowCountParamIndex, int rowCount, 
                boolean filterDuplicates) {
            super(statement, offsetParamIndex, rowCountParamIndex, rowCount, filterDuplicates);
        }

        @Override
        protected ExpressionCallTO getNewTO() throws DAOException {
            log.entry();

            String id = null, geneId = null, anatEntityId = null, devStageId = null;
            DataState affymetrixData = null, estData = null, inSituData = null, rnaSeqData = null;
            Boolean includeSubstructures = null, includeSubStages = null;
            OriginOfLine anatOriginOfLine = null, stageOriginOfLine = null;

            ResultSet currentResultSet = this.getCurrentResultSet();
            //every call to values() returns a newly cloned array, so we cache the array
            DataState[] dataStates = DataState.values();
            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("expressionId")) {
                        id = currentResultSet.getString(column.getKey());
                        
                    } else if (column.getValue().equals("globalExpressionId")) {
                            id = currentResultSet.getString(column.getKey());

                    } else if (column.getValue().equals("geneId")) {
                        geneId = currentResultSet.getString(column.getKey());

                    } else if (column.getValue().equals("anatEntityId")) {
                        anatEntityId = currentResultSet.getString(column.getKey());

                    } else if (column.getValue().equals("stageId")) {
                        devStageId = currentResultSet.getString(column.getKey());

                    } else if (column.getValue().equals("affymetrixData")) {
                        //index of the enum in the mysql database corresponds to the ordinal 
                        //of DataState + 1
                        affymetrixData = dataStates[currentResultSet.getInt(column.getKey()) - 1];
                    } else if (column.getValue().equals("estData")) {
                       //index of the enum in the mysql database corresponds to the ordinal 
                        //of DataState + 1
                        estData = dataStates[currentResultSet.getInt(column.getKey()) - 1];
                    } else if (column.getValue().equals("inSituData")) {
                        //index of the enum in the mysql database corresponds to the ordinal 
                        //of DataState + 1
                        inSituData = dataStates[currentResultSet.getInt(column.getKey()) - 1];
                    } else if (column.getValue().equals("rnaSeqData")) {
                        //index of the enum in the mysql database corresponds to the ordinal 
                        //of DataState + 1
                        rnaSeqData = dataStates[currentResultSet.getInt(column.getKey()) - 1];
                        
                    } else if (column.getValue().equals("originOfLine") || 
                            column.getValue().equals("anatOriginOfLine")) {
                        anatOriginOfLine = OriginOfLine.convertToOriginOfLine(
                                currentResultSet.getString(column.getKey()));
                    } else if (column.getValue().equals("stageOriginOfLine")) {
                        stageOriginOfLine = OriginOfLine.convertToOriginOfLine(
                                currentResultSet.getString(column.getKey()));
                    } else if (column.getValue().equals("includeSubstructures")) {
                        includeSubstructures = currentResultSet.getBoolean(column.getKey());
                    } else if (column.getValue().equals("includeSubStages")) {
                        includeSubStages = currentResultSet.getBoolean(column.getKey());
                    } 

                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            
            return log.exit(new ExpressionCallTO(id, geneId, anatEntityId, devStageId,
                    affymetrixData, estData, inSituData, rnaSeqData,
                    includeSubstructures, includeSubStages, 
                    anatOriginOfLine, stageOriginOfLine));
        }
    }
    
    /**
     * A {@code MySQLDAOResultSet} specific to {@code GlobalExpressionToExpressionTO}.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public class MySQLGlobalExpressionToExpressionTOResultSet 
                                         extends MySQLDAOResultSet<GlobalExpressionToExpressionTO> 
                                         implements GlobalExpressionToExpressionTOResultSet {
        /**
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)}
         * super constructor.
         * 
         * @param statement The first {@code BgeePreparedStatement} to execute a query on.
         */
        private MySQLGlobalExpressionToExpressionTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected GlobalExpressionToExpressionTO getNewTO() throws DAOException {
            log.entry();
            String globalExpressionId = null, expressionId = null;

            ResultSet currentResultSet = this.getCurrentResultSet();
            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("globalExpressionId")) {
                        globalExpressionId = currentResultSet.getString(column.getKey());

                    } else if (column.getValue().equals("expressionId")) {
                        expressionId = currentResultSet.getString(column.getKey());

                    } 
                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            
            return log.exit(new GlobalExpressionToExpressionTO(globalExpressionId, expressionId));
        }
    }
}
