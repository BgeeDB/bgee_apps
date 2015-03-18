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
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
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
                params.getSpeciesIds(), params.isIncludeSubstructures(), 
                params.isIncludeSubStages(), null)); 
        
//        String sql = "{call getAllExpression(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
//
//        //we don't use a try-with-resource, because we return a pointer to the results, 
//        //not the actual results, so we should not close this BgeeCallableStatement.
//        BgeeCallableStatement callStmt = null;
//        try {
//            callStmt = this.getManager().getConnection().prepareCall(sql);
//            callStmt.setString(1, createStringFromSet(params.getGeneIds(), '|'));
//            callStmt.setString(2, createStringFromSet(params.getAnatEntityIds(), '|'));
//            callStmt.setString(3, createStringFromSet(params.getStageIds(), '|'));
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
    
        try (MySQLExpressionCallTOResultSet resultSet = new MySQLExpressionCallTOResultSet(
                this.getManager().getConnection().prepareStatement(sql))) {
            
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
     * that are the IDs of species allowing to filter the calls to use, a {@code boolean} defining
     * whether this expression call was generated using data from the anatomical entity with  
     * the ID {@link CallTO#getAnatEntityId()} alone, or by also considering all its descendants  
     * by <em>is_a</em> or <em>part_of</em> relations, even indirect, and a {@code boolean}  
     * defining whether this expression call was generated using data from the stage with   
     * the ID {@link CallTO#getStageId()} alone, or by also considering all its descendants.
     * <p>
     * It is possible to obtain the calls ordered based on IDs of homologous gene groups, 
     * which is useful for multi-species analyzes. In that case, it is necessary to provide 
     * the ID of a taxon targeted, in order to retrieve the proper homologous groups.
     * <p>
     * The expression calls are retrieved and returned as a {@code ExpressionCallTOResultSet}. 
     * It is the responsibility of the caller to close this {@code DAOResultSet} once 
     * results are retrieved.
     * 
     * @param speciesIds             A {@code Set} of {@code String}s that are the IDs of species 
     *                               allowing to filter the calls to use
     * @param isIncludeSubstructures A {@code boolean} defining whether descendants 
     *                               of the anatomical entity were considered.
     * @param isIncludeSubStages     A {@code boolean} defining whether descendants 
     *                               of the stage were considered.
     * @param commonAncestralTaxonId A {@code String} that is the ID of the taxon used 
     *                               to retrieve the OMANodeId of the genes, that will be used 
     *                               to order the calls. If {@code null} or empty, calls 
     *                               will not be ordered. 
     * @return                       An {@code ExpressionCallTOResultSet} containing all expression 
     *                               calls from data source.
     * @throws DAOException          If a {@code SQLException} occurred while trying to get 
     *                               expression calls.                      
     */
    //- how will we aggregate with NoExpressionCalls? Just using the ordering by OMAGroupId 
    //  will be complicated :/ Should we use an UNION clause between expression and 
    //  noExpression tables? -> complicated for our DAOs/TOs
    //- Actually, commonAncestralTaxonId will not be derived from the list of species 
    //  provided. This is because, sometimes, we might want to group genes in some species 
    //  based on, e.g., a duplication event preceding the split of their lineages.
    //- Actually, it seems that it is not necessary to put this commonAncestralTaxonId attribute 
    //  in CallParams: as this is ill trigger an ordering of the calls, it needs to be 
    //  a specific method anyway.
    //- Speaking of duplication event... would it be possible to target a specific 
    //  duplication event? They don't have any taxonId...
    private ExpressionCallTOResultSet getExpressionCalls(Set<String> speciesIds, 
            boolean isIncludeSubstructures, boolean isIncludeSubStages, 
            String commonAncestralTaxonId) throws DAOException {
        log.entry(speciesIds, isIncludeSubstructures, isIncludeSubStages, 
                commonAncestralTaxonId);

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
            if (speciesIds != null && !speciesIds.isEmpty()) {
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
            //this method should have an argument 'distinctClause'; it is the responsibility 
            //of the caller, depending on the parameters of the query, to determine 
            //whether the clause is needed.
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
    
    /**
     * Generates the SELECT clause of a MySQL query used to retrieve {@code ExpressionCallTO}s.
     * 
     * @param attributes                A {@code Set} of {@code Attribute}s defining 
     *                                  the columns/information the query should retrieve.
     * @param includeSubstructures      A {@code boolean} defining whether the query will use 
     *                                  expression data propagated from substructures.
     * @param includeSubStages          A {@code boolean} defining whether the query will use 
     *                                  expression data propagated from sub-stages.
     * @param exprTableName             A {@code String} defining the name of the expression 
     *                                  table used.
     * @param propagatedStageTableName  A {@code String} defining the name of the table 
     *                                  allowing to retrieve stage IDs, when 
     *                                  {@code includeSubStages} is {@code true}.
     * @return                          A {@code String} containing the SELECT clause 
     *                                  for the requested query.
     * @throws IllegalArgumentException If one {@code Attribute} of {@code attributes} is unknown.
     */
    private String generateSelectClause(Set<ExpressionCallDAO.Attribute> attributes, 
            boolean includeSubstructures, boolean includeSubStages, 
            String exprTableName, String propagatedStageTableName) throws IllegalArgumentException {
        log.entry(attributes, includeSubstructures, includeSubStages, 
                exprTableName, propagatedStageTableName);
        
        String sql = "";
        //the query construct is so complex that we always iterate each Attribute in any case
        if (attributes == null || attributes.isEmpty()) {
            attributes = EnumSet.allOf(ExpressionCallDAO.Attribute.class);
        }
        
        //String used when includeSubstructures and includeSubStages are true, 
        //so that global expression calls are propagated to parent stages on the fly.
        //For this, we use a GROUP BY clause, and we need to use GROUP_CONCAT to examine 
        //the different origins of a line. We can use IF clauses inside the GROUP_CONCATs, 
        //to make sure they do not exceed the group_concat_max_len. 
        //In the case of the stageOriginOfLine, the only possible values of 
        //the GROUP_CONCAT DISTINCT, when using stageOriginIfClause, will be: '1' (= data come 
        //from the stage only; corresponds to a 'self' origin of line); '0' (= data come 
        //from some sub-stages only; corresponds to a 'descent' origin of line); 
        //'0,1' or '1,0' (= data come from both the stage and some sub-stages; 
        //corresponds to a 'both' origin of line); 
        String stageOriginIfClause = "IF(" + exprTableName + ".stageId =" + 
            propagatedStageTableName + ".stageId" + ", 1, 0)";
        
        for (ExpressionCallDAO.Attribute attribute: attributes) {
            if (sql.isEmpty()) {
                sql += "SELECT ";
                //does the attributes requested ensure that there will be 
                //no duplicated results?
                //FIXME: hmm, what did I write? even if we request the primary keys, 
                //we can still have duplicates, based on the joins made for instance.
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
                    //otherwise, we use the real column in the global expression table, 
                    //unless we are propagating on the fly to include sub-stages
                    if (!includeSubStages) {
                        sql += "originOfLine ";
                    } else {
                        //the anatOriginOfLine has to be recomputed, as it is possible 
                        //for a given anat. entity to have a given origin at a given stage, 
                        //but also a different origin at a sub-stage
                        sql += "IF (GROUP_CONCAT(DISTINCT originOfLine) LIKE " +
                        		"'%" + OriginOfLine.BOTH.getStringRepresentation() + "%', " +
                                   //if concatenation of originOfLine contains 'both', it is easy, 
                                   //the on-the-fly anat. origin is 'both' 
                        	       "'" + OriginOfLine.BOTH.getStringRepresentation() + "', " +
                                   //Otherwise, if it contains 'descent'...
                        		   "IF(GROUP_CONCAT(DISTINCT originOfLine) LIKE " +
                        		   "'%" + OriginOfLine.DESCENT.getStringRepresentation() + "%', " +
                                       //... as well as 'self'...
                        		       "IF(GROUP_CONCAT(DISTINCT originOfLine) LIKE " +
                        		       "'%" + OriginOfLine.SELF.getStringRepresentation() + "%', " +
                                           //... then the anat. origin is also 'both'.
                        		           "'" + OriginOfLine.BOTH.getStringRepresentation() + "', " +
                                           //If it contains only 'descent', then this is it
                        		           "'" + OriginOfLine.DESCENT.getStringRepresentation() + "'), " +
                                       //finally, 'self' is here the only remaining possibility
                        		       "'" + OriginOfLine.SELF.getStringRepresentation() + "')) ";
                    }
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
                    //if there was on propagation from sub-stages, the origin is always 'self'
                    sql += "'" + OriginOfLine.SELF.getStringRepresentation() + "' ";
                } else {
                    // Otherwise, we need to know the stages that allowed to generate a propagated 
                    //expression line. We use group_concat.
                    sql +=  "IF (GROUP_CONCAT(DISTINCT " + stageOriginIfClause + ") = '1', " +
                                //if GROUP_CONCAT = 1, only the stage itself has been seen, 
                                //corresponds to a 'self' origin of line
                            	"'" + OriginOfLine.SELF.getStringRepresentation() + "', " +
                            	"IF (GROUP_CONCAT(DISTINCT " + stageOriginIfClause + ") = '0', " +
                            	    //if GROUP_CONCAT = 0, only some sub-stages have been seen, 
                                    //corresponds to a 'descent' origin of line
                            	    "'" + OriginOfLine.DESCENT.getStringRepresentation() + "', " +
                                    //'both' is the only remaining possibility, corresponds to 
                            	    //GROUP_CONCAT = '0,1' or GROUP_CONCAT = '1,0'
                            	    "'" + OriginOfLine.BOTH.getStringRepresentation() + "')) ";
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
                
            } else if (attribute.equals(ExpressionCallDAO.Attribute.OBSERVED_DATA)) {
                //the Attributes OBSERVED_DATA does not correspond to any columns 
                //in a table. See this attribute's javadoc for an explanation of 
                //why this attribute is needed, and how it is different from 
                //the origins of line.
                
                String anatOriginIfClause = "IF(originOfLine = 'descent', 0, 1) ";
                if (!includeSubStages) {
                    if (!includeSubstructures) {
                        //if no propagation requested, it's easy, all data are observed
                        sql += "1 ";
                    } else {
                        //if we propagate only from substructures, we check the column 
                        //originOfLine in the globalExpression table: if it equals to 'descent', 
                        //then there are no observed data.
                        sql += anatOriginIfClause;
                    }
                } else {
                    //If we propagate from sub-stages, it's more complicated, as data 
                    //are grouped, so we need to use GROUP_CONCAT to examine 
                    //the different origins of a line.
                    //Notably, we need to check at the same time the anat. and the stage 
                    //origins of line, this is why we also use CONCAT. 
                    sql += "IF(GROUP_CONCAT(DISTINCT CONCAT(";
                    //first, retrieved observation status for anat. entity.
                    if (includeSubstructures) {
                        //check the originOfLine column in globalExpression table
                        sql += anatOriginIfClause;
                    } else {
                        //no propagation from substructures, data are always observed in the organ
                        sql += "'1'";
                    }
                    //then, observation status for stage.
                    //we use '.' as a separator between anat. and stage status.
                    sql += ", '.', " + stageOriginIfClause + ")) " +
                           //if we have at the same time observed data in the organ 
                           //and in the stage (corresponds to '1.1'), then this line 
                           //is actually observed.
                    	   "LIKE '%1.1%', 1, 0) ";
                }
                
                sql += "AS observedData ";
                
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

            String id = null, geneId = null, anatEntityId = null, stageId = null;
            DataState affymetrixData = null, estData = null, inSituData = null, rnaSeqData = null;
            Boolean includeSubstructures = null, includeSubStages = null, observedData = null;
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
                        stageId = currentResultSet.getString(column.getKey());

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
                    } else if (column.getValue().equals("observedData")) {
                        observedData = currentResultSet.getBoolean(column.getKey());
                    }

                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            return log.exit(new ExpressionCallTO(id, geneId, anatEntityId, stageId,
                    affymetrixData, estData, inSituData, rnaSeqData,
                    includeSubstructures, includeSubStages, 
                    anatOriginOfLine, stageOriginOfLine, observedData));
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

                    }  else {
                        throw log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }
                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            
            return log.exit(new GlobalExpressionToExpressionTO(globalExpressionId, expressionId));
        }
    }
}
