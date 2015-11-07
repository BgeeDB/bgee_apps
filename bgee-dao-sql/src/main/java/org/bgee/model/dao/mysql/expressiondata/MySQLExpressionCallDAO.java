package org.bgee.model.dao.mysql.expressiondata;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.CallDAOFilter.ExpressionCallDAOFilter;
import org.bgee.model.dao.api.expressiondata.DAOConditionFilter;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO.OriginOfLine;
import org.bgee.model.dao.api.expressiondata.ExpressionCallParams;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;

/**
 * A {@code ExpressionCallDAO} for MySQL. 
 * 
 * @author Valentine Rech de Laval
 * @author Frederic Bastian
 * @version Bgee 13 Oct. 2015
 * @see org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO
 * @since Bgee 13
 */
//TODO: manage OrderingAttributes in new method getExpressionCalls.
//TODO: add ranks to method generateSelectClause. If including substages, 
//      the mean rank (over the GROUP BY) of each data type rank should be used.
//TODO: add ranks to ExpressionCallTO and MySQLExpressionCallTOResultSet
//TODO: allow ordering by the mean rank of the ranks of each data type (OrderingAttribute.MEAN_RANK). 
//      The data types used should be all data types considered from the CallDAOFilters.
//      If including substages, the mean rank (over the GROUP BY) of the mean rank 
//      (over the requested data types) should be used.
//TODO: manage ancestralTaxonId in new method getExpressionCalls
public class MySQLExpressionCallDAO extends MySQLDAO<ExpressionCallDAO.Attribute> 
implements ExpressionCallDAO {
    
    private final static Logger log = LogManager.getLogger(MySQLExpressionCallDAO.class.getName());
    
    /**
     * Convert a data type attribute into a {@code String} that is the corresponding column name.
     * If {@code attribute} is not a data type attribute (see {@link CallDAO.Attribute#isDataTypeAttribute()}), 
     * an {@code IllegalArgumentException} is thrown.
     * 
     * @param attribute An {@code Attribute} that is a data type attribute.
     * @return          A {@code String} that is the column name corresponding to {@code attribute}.
     * @throws IllegalArgumentException If {@code attribute} is not a data type attribute.
     */
    private static String convertDataTypeAttrToColName(ExpressionCallDAO.Attribute attribute) 
            throws IllegalArgumentException {
        log.entry(attribute);
        if (!attribute.isDataTypeAttribute()) {
            throw log.throwing(new IllegalArgumentException("Only a data type attribute can be provided."));
        }
        switch(attribute) {
        case AFFYMETRIX_DATA: 
            return log.exit("affymetrixData");
        case EST_DATA: 
            return log.exit("estData");
        case IN_SITU_DATA: 
            return log.exit("inSituData");
        case RNA_SEQ_DATA: 
            return log.exit("rnaSeqData");
        default: 
            throw log.throwing(new IllegalStateException("Attribute " + attribute + " not supported")); 
        }
    }

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

    /** 
     * {@inheritDoc}
     * <p>
     * <strong>Implementation notes</strong>: 
     * If {@code isIncludeSubStages} is {@code true}, and if it is expected 
     * to have a large amount of data to consider, it is highly recommended: 
     * <ul>
     * <li>to only request basic attributes related to {@code getGeneId}, 
     * {@code getAnatEntityId}, and {@code getStageId}. So, to avoid requesting attributes 
     * related to: {@code getAnatOriginOfLine}, {@code getStageOriginOfLine}, 
     * or {@code isObservedData}; or any data types parameter ({@code getAffymetrixData}, 
     * {@code getAffymetrixMeanRank}, etc); 
     * <li>to not request ordering based on {@code CallDAO.OrderingAttribute.MEAN_RANK}. 
     * <li>to not perform a filtering with AND conditions between data types (for instance, 
     * "affymetrixData >= LOW_QUALITY AND rnaSeqData >= LOW_QUALITY"). It means that it is recommended 
     * to not set parameters for different data types in a same {@code ExpressionCallTO}. 
     * It is OK to set parameters for different data types in different {@code ExpressionCallTO}s 
     * to perform an OR filtering (for instance, "affymetrixData >= LOW_QUALITY OR 
     * rnaSeqData >= LOW_QUALITY").
     * </ul>
     * This is because when {@code isIncludeSubStages} is requested, these parameters need 
     * to be computed on-the-fly, necessitating an expensive "GROUP BY" of the data. 
     * So this should be used only when requesting data for a very low number of specific genes.
     * <p>
     * Another reason is due to the propagation itself. Consider the following case, 
     * where stageId2 is a sub-stage of stageId1, and where the filtering requested is 
     * "affymetrixData >= LOW_QUALITY AND rnaSeqData >= LOW_QUALITY": 
     * <ul>
     * <li>geneId1, anatEntityId1, stageId1, affymetrixData = HIGH_QUALITY, rnaSeqData = NO_DATA
     * <li>geneId1, anatEntityId1, stageId2, affymetrixData = NO_DATA, rnaSeqData = HIGH_QUALITY
     * </ul>
     * The infer call is: 
     * <ul>
     * <li>geneId1, anatEntityId1, stageId1, affymetrixData = HIGH_QUALITY, rnaSeqData = HIGH_QUALITY
     * </ul>
     * If the filtering was performed in a WHERE clause with no GROUP BY, no results would be returned, 
     * because of the AND conditions between data types. If the conditions between data types 
     * were OR conditions, we could filter in a WHERE clause with no GROUP BY. But only 
     * if no attributes necessitating a GROUP BY were requested, otherwise the values produced  
     * would be inconsistent, depending on the calls filtered in the WHERE clause. 
     * So, even if there are only OR conditions between data types, we might need a GROUP BY 
     * and a filtering in a HAVING clause, depending on the attributes requested.
     * <p>
     * All of this is OK if you query only a few genes, as it wouldn't be too computer-intensive.
     */
    @Override
    public ExpressionCallTOResultSet getExpressionCalls(
            Collection<ExpressionCallDAOFilter> callFilters, 
            boolean includeSubstructures, boolean includeSubStages, 
            Collection<String> globalGeneIds, String taxonId, 
            Collection<ExpressionCallDAO.Attribute> attributes, 
            LinkedHashMap<ExpressionCallDAO.OrderingAttribute, DAO.Direction> orderingAttributes) 
                    throws DAOException, IllegalArgumentException {
        log.entry(callFilters, includeSubstructures, includeSubStages, globalGeneIds, taxonId, 
                attributes, orderingAttributes);
        
        if (taxonId != null && !taxonId.isEmpty()) {
            throw log.throwing(new UnsupportedOperationException(
                    "Query using gene orthology not yet implemented"));
        }
        
        //needs a LinkedHashSet for consistent settings of the parameters. 
        LinkedHashSet<ExpressionCallDAOFilter> clonedCallFilters = Optional.ofNullable(callFilters)
                .map(e -> new LinkedHashSet<>(e)).orElse(new LinkedHashSet<>());
        //attributes
        Set<ExpressionCallDAO.Attribute> originalAttrs = Optional.ofNullable(attributes)
                .map(e -> EnumSet.copyOf(e)).orElse(EnumSet.allOf(ExpressionCallDAO.Attribute.class));
        //ordering attributes
        LinkedHashMap<ExpressionCallDAO.OrderingAttribute, DAO.Direction> clonedOrderingAttrs = 
                Optional.ofNullable(orderingAttributes)
                .map(e -> new LinkedHashMap<>(orderingAttributes)).orElse(new LinkedHashMap<>());
        //**********************************************
        // Extract relevant information from arguments
        //**********************************************
        
        //store the global gene ID filtering, or try to create one based on the CallDAOFilters
        Set<String> geneIds = Optional.ofNullable(globalGeneIds)
                .map(e -> new HashSet<>(e)).orElse(new HashSet<>());
        boolean globalGeneFilter = false;
        if (geneIds.isEmpty()) {
            //if all callFilters have the same gene ID filter
            Set<Set<String>> allGeneIdFilters = clonedCallFilters.stream().map(e -> e.getGeneIds())
                    .collect(Collectors.toSet());
            if (allGeneIdFilters.size() == 1 && !allGeneIdFilters.iterator().next().isEmpty()) {
                geneIds = new HashSet<>(allGeneIdFilters.iterator().next());
                globalGeneFilter = true;
            }
            //otherwise, check whether all callFilters specify a gene ID filtering, 
            //we will collect them all. We will use these gene IDs in case we need to perform 
            //a GROUP BY with a LIMIT clause.
            if (geneIds.isEmpty() && clonedCallFilters.stream()
                    .noneMatch(filter -> filter.getGeneIds() == null || filter.getGeneIds().isEmpty())) {
                //we collect the gene IDs, but it is not the same as a global gene ID filter, 
                //so globalGeneFilter remains false
                geneIds = clonedCallFilters.stream().flatMap(filter -> filter.getGeneIds().stream())
                        .collect(Collectors.toSet());
            }
        } else {
            //global gene ID filter provided as argument
            globalGeneFilter = true;
        }
        
        //same principle for species IDs: if all callFilters specify some species ID filtering, 
        //then we can apply a "global" species ID filtering to the query.
        boolean globalSpeciesFilter = false;
        Set<String> speciesIds = new HashSet<>();
        Set<Set<String>> allSpeciesIdFilters = clonedCallFilters.stream().map(e -> e.getSpeciesIds())
                .collect(Collectors.toSet());
        if (allSpeciesIdFilters.size() == 1 && !allSpeciesIdFilters.iterator().next().isEmpty()) {
            speciesIds = new HashSet<>(allSpeciesIdFilters.iterator().next());
            globalSpeciesFilter = true;
            assert !speciesIds.isEmpty();
        }
        //otherwise, check whether all callFilters specify a species ID filtering, 
        //we will collect them all. We will use these species IDs in case we need to perform 
        //a GROUP BY with a LIMIT clause.
        if (speciesIds.isEmpty() && clonedCallFilters.stream()
                .noneMatch(filter -> filter.getSpeciesIds() == null || filter.getSpeciesIds().isEmpty())) {
            //we collect the species IDs, but it is not the same as a global species ID filter, 
            //so globalSpeciesFilter remains false
            speciesIds = clonedCallFilters.stream().flatMap(filter -> filter.getSpeciesIds().stream())
                    .collect(Collectors.toSet());
        }
        
        //Get the requested attributes, and update them if needed. Notably, if includeSubStages is true, 
        //and some specific parameters are requested, the filtering will need to be done 
        //in a HAVING clause. In that case, we add the columns we will need in the HAVING clause, 
        //based on the parameters provided, to avoid over-verbose HAVING clause. 
        //We also need these columns if an ordering is requested on them.
        Set<ExpressionCallDAO.Attribute> updatedAttrs = EnumSet.copyOf(originalAttrs);
        boolean havingClauseNeeded = false;
        boolean groupingByNeeded = false;
        
        if (includeSubStages) {
            if (originalAttrs.contains(ExpressionCallDAO.Attribute.AFFYMETRIX_DATA) || 
                    originalAttrs.contains(ExpressionCallDAO.Attribute.EST_DATA) || 
                    originalAttrs.contains(ExpressionCallDAO.Attribute.IN_SITU_DATA) || 
                    originalAttrs.contains(ExpressionCallDAO.Attribute.RNA_SEQ_DATA) || 
                    originalAttrs.contains(ExpressionCallDAO.Attribute.OBSERVED_DATA) || 
                    (includeSubstructures && 
                            originalAttrs.contains(ExpressionCallDAO.Attribute.ANAT_ORIGIN_OF_LINE)) || 
                    originalAttrs.contains(ExpressionCallDAO.Attribute.STAGE_ORIGIN_OF_LINE) || 
                    clonedOrderingAttrs.keySet().contains(ExpressionCallDAO.OrderingAttribute.MEAN_RANK)) {
                
                groupingByNeeded = true;
            }
            
            //for filtering based on data types, we can still avoid to filter in the HAVING clause 
            //or to use a GROUP BY, if there is no GROUP BY needed because of requested attributes 
            //(test above), and if there is no AND condition between data types.
            //First, retrieve all data types with a specified filtering, per CallTO.
            Set<Set<ExpressionCallDAO.Attribute>> filteringDataTypesPerCallTO = clonedCallFilters.stream()
                    .flatMap(filter -> filter.getCallTOFilters().stream()
                            .map(callTO -> filter.extractFilteringDataTypes(callTO).keySet()))
                    .collect(Collectors.toSet());
            if (groupingByNeeded || 
                    //check whether there is any AND condition between data types
                    filteringDataTypesPerCallTO.stream().anyMatch(set -> set.size() > 1)) {
                havingClauseNeeded = true;
                groupingByNeeded = true;
                //add in the SELECT clause the columns we will need in the HAVING clause, 
                //to avoid over-verbose HAVING clause.
                updatedAttrs.addAll(filteringDataTypesPerCallTO.stream()
                        .flatMap(Set::stream)
                        .collect(Collectors.toCollection(() -> 
                            EnumSet.noneOf(ExpressionCallDAO.Attribute.class))));
            }
            
            Set<ExpressionCallTO> allCallTOs = clonedCallFilters.stream()
                    .flatMap(e -> e.getCallTOFilters().stream())
                    .collect(Collectors.toSet());
            
            if (allCallTOs.stream().anyMatch(e -> e.isObservedData() != null)) {
                havingClauseNeeded = true;
                groupingByNeeded = true;
                //add in the SELECT clause the columns we will need in the HAVING clause, 
                //to avoid over-verbose HAVING clause.
                updatedAttrs.add(ExpressionCallDAO.Attribute.OBSERVED_DATA);
            }
            if (includeSubstructures && 
                    allCallTOs.stream().anyMatch(e -> e.getAnatOriginOfLine() != null)) {
                havingClauseNeeded = true;
                groupingByNeeded = true;
                //add in the SELECT clause the columns we will need in the HAVING clause, 
                //to avoid over-verbose HAVING clause.
                updatedAttrs.add(ExpressionCallDAO.Attribute.ANAT_ORIGIN_OF_LINE);
            }
            if (allCallTOs.stream().anyMatch(e -> e.getStageOriginOfLine() != null)) {
                havingClauseNeeded = true;
                groupingByNeeded = true;
                //add in the SELECT clause the columns we will need in the HAVING clause, 
                //to avoid over-verbose HAVING clause.
                updatedAttrs.add(ExpressionCallDAO.Attribute.STAGE_ORIGIN_OF_LINE);
            }
            
            if (log.isWarnEnabled() && groupingByNeeded && 
                    (geneIds.isEmpty() || geneIds.size() > this.getManager().getExprPropagationGeneCount())) {
                log.warn("IncludeSubStages is true and some parameters highly costly to compute "
                        + "are needed, this will take lots of time... "
                        + "You should rather compute these results in several queries.");
            }
        }

        //**********************************************
        
        //execute query
        return log.exit(this.getExpressionCalls(clonedCallFilters, geneIds, globalGeneFilter, 
                speciesIds, globalSpeciesFilter, includeSubstructures, includeSubStages, 
                taxonId, originalAttrs, updatedAttrs, clonedOrderingAttrs, 
                groupingByNeeded, havingClauseNeeded));
    }

    @Override
    //deprecated, see other getExpressionCalls method
    @Deprecated
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
                this.getManager().getConnection().prepareStatement(sql), null)) {
            
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
    //
    private ExpressionCallTOResultSet getExpressionCalls(
            LinkedHashSet<ExpressionCallDAOFilter> callFilters, 
            Set<String> allGeneIds, boolean globalGeneFilter, 
            Set<String> allSpeciesIds, boolean globalSpeciesFilter, 
            boolean includeSubstructures, boolean includeSubStages, 
            String commonAncestralTaxonId, 
            Set<ExpressionCallDAO.Attribute> originalAttrs, Set<ExpressionCallDAO.Attribute> updatedAttrs, 
            LinkedHashMap<ExpressionCallDAO.OrderingAttribute, DAO.Direction> orderingAttrs, 
            boolean stageGroupingByNeeded, boolean havingClauseNeeded) throws DAOException {
        log.entry(callFilters, allGeneIds, globalGeneFilter, allSpeciesIds, globalSpeciesFilter, 
                includeSubstructures, includeSubStages, 
                commonAncestralTaxonId, originalAttrs, updatedAttrs, orderingAttrs, 
                stageGroupingByNeeded, havingClauseNeeded);

        // Construct sql query
        String exprTableName = "expression";
        if (includeSubstructures) {
            exprTableName = "globalExpression";
        }
        String propagatedStageTableName = "propagatedStage";

        //determine whether to use DISTINCT keyword.
        boolean distinct = !(stageGroupingByNeeded || (!includeSubStages && 
                (updatedAttrs.contains(ExpressionCallDAO.Attribute.ID) || 
                (updatedAttrs.contains(ExpressionCallDAO.Attribute.GENE_ID) && 
                 updatedAttrs.contains(ExpressionCallDAO.Attribute.ANAT_ENTITY_ID) && 
                 updatedAttrs.contains(ExpressionCallDAO.Attribute.STAGE_ID)))));
        String sql = this.generateSelectClause(updatedAttrs, distinct, 
                includeSubstructures, includeSubStages, 
                exprTableName, propagatedStageTableName);
        
        sql += " FROM " + exprTableName;

        //a boolean to determine whether genes and species were already filtered because it is needed 
        //to perform the query in several iterations, because includeSubStages is true 
        //and a high number of genes has to be considered. 
        boolean genesAndSpeciesFilteredForPropagation = false;
        if (includeSubStages) {
            //propagate expression calls to parent stages.
            sql += " INNER JOIN stage ON " + exprTableName + ".stageId = stage.stageId " +
                   " INNER JOIN stage AS " + propagatedStageTableName + " ON " +
                       propagatedStageTableName + ".stageLeftBound <= stage.stageLeftBound AND " +
                       propagatedStageTableName + ".stageRightBound >= stage.stageRightBound ";
            //when some parameters are requested, we need to perform a GROUP BY to compute them 
            //on the fly. But there is too much data to do it at once, except when restricted to few genes, 
            //so we need to do it using several queries, group of genes by group of genes.
            //So we need to identify genes with expression data, and to retrieve them 
            //with a LIMIT clause. We cannot use a subquery with a LIMIT in a IN clause 
            //(limitation of MySQL as of version 5.5), so we use the subquery to create 
            //a temporary table to join to in the main query. 
            //In the subquery, the EXISTS clause is used to speed-up the main query, 
            //to make sure we will not look up for data not present in the expression table.
            if (stageGroupingByNeeded && (allGeneIds.isEmpty() || 
                    allGeneIds.size() > this.getManager().getExprPropagationGeneCount())) {
                
                genesAndSpeciesFilteredForPropagation = true;
                
                sql += " INNER JOIN (SELECT geneId from gene AS tempGene where exists " 
                    + "(select 1 from expression where expression.geneId = tempGene.geneId) ";
                if (!allSpeciesIds.isEmpty()) {
                    sql += "AND tempGene.speciesId IN (" + BgeePreparedStatement.generateParameterizedQueryString(
                            allSpeciesIds.size()) + ") ";
                }
                if (!allGeneIds.isEmpty()) {
                    sql += "AND tempGene.geneId IN (" + BgeePreparedStatement.generateParameterizedQueryString(
                            allGeneIds.size()) + ") ";
                }
                sql += "ORDER BY tempGene.geneId LIMIT ?, ?) as tempTable on " 
                    + exprTableName + ".geneId = tempTable.geneId ";
            }
        }
        //even if we already joined the gene table because includeSubStages is true, 
        //we need to access to this table in the main query for the species filtering 
        //defined in the CallFilters. 
        if (!allSpeciesIds.isEmpty() || 
                callFilters.stream().anyMatch(filter -> !filter.getSpeciesIds().isEmpty())) {
            sql += " INNER JOIN gene ON (gene.geneId = " + exprTableName + ".geneId) ";
        }
        //If a global gene filter is defined, we can apply it to all conditions in all cases.
        //Also, if all CallFilters provide a non-empty gene ID list, and if we need 
        //to filter the query in the HAVING clause rather than in the WHERE clause, 
        //then it's worth filtering the genes in the WHERE clause immediately; 
        //otherwise, if the query will be filtered in the WHERE clause, then these gene IDs 
        //will be specified anyway, so we don't need to set them here.
        boolean whereClauseStarted = false;
        if (!allGeneIds.isEmpty() && !genesAndSpeciesFilteredForPropagation && 
                (globalGeneFilter || havingClauseNeeded)) {
            sql += " WHERE " + exprTableName + ".geneId IN (" 
                + BgeePreparedStatement.generateParameterizedQueryString(allGeneIds.size()) + ") ";
            whereClauseStarted = true;
        }
        //same for species IDs: if a HAVING clause is needed, and allSpeciesIds is not empty, 
        //then it's worth filtering immediately based on species IDs, to simplify 
        //the HAVING filtering; otherwise, it's useless, the species IDs are going to be specified 
        //in the WHERE clause by the CallFilters anyway, unless it is a global filter to be applied 
        //on top of all CallFilter filtering.
        if (!allSpeciesIds.isEmpty() && !genesAndSpeciesFilteredForPropagation && 
                (globalSpeciesFilter || havingClauseNeeded)) {
            if (!whereClauseStarted) {
                sql += "WHERE ";
            } else {
                sql += "AND ";
            }
            sql += "gene.speciesId IN (" + BgeePreparedStatement.generateParameterizedQueryString(
                            allSpeciesIds.size()) + ") ";
            whereClauseStarted = true;
        }
        
        String filtering = this.generateFilteringClause(callFilters, !globalGeneFilter, 
                !globalSpeciesFilter, exprTableName, 
                includeSubStages? propagatedStageTableName: exprTableName,
                "gene", includeSubstructures, includeSubStages);
        if (!havingClauseNeeded && !filtering.isEmpty()) {
            if (!whereClauseStarted) {
                sql += "WHERE ";
            } else {
                sql += "AND ";
            }
            sql += filtering;
        }
        
        if (stageGroupingByNeeded) {
            sql += " GROUP BY " + exprTableName + ".geneId, " + 
                   exprTableName + ".anatEntityId, " + propagatedStageTableName + ".stageId ";
            
            if (havingClauseNeeded && !filtering.isEmpty()) {
                sql += "HAVING " + filtering;
            }
        }
        

        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            
            int index = 1;
            int offsetParamIndex = 0;
            if (includeSubStages && stageGroupingByNeeded && (allGeneIds.isEmpty() || 
                    allGeneIds.size() > this.getManager().getExprPropagationGeneCount())) {
                    
                if (!allSpeciesIds.isEmpty()) {
                    stmt.setStringsToIntegers(index, allSpeciesIds, true);
                    index += allSpeciesIds.size();
                }
                if (!allGeneIds.isEmpty()) {
                    stmt.setStrings(index, allGeneIds, true);
                    index += allGeneIds.size();
                }
                //keep two parameters for the offset and count LIMIT arguments
                offsetParamIndex = index;
                index += 2;
            }
            
            if (!allGeneIds.isEmpty() && !genesAndSpeciesFilteredForPropagation && 
                    (globalGeneFilter || havingClauseNeeded)) {
                stmt.setStrings(index, allGeneIds, true);
                index += allGeneIds.size();
            }
            if (!allSpeciesIds.isEmpty() && !genesAndSpeciesFilteredForPropagation && 
                    (globalSpeciesFilter || havingClauseNeeded)) {
                stmt.setStringsToIntegers(index, allSpeciesIds, true);
                index += allSpeciesIds.size();
            }
            
            this.parameterizeFilteringClause(stmt, index, callFilters, !globalGeneFilter, 
                    !globalSpeciesFilter, includeSubstructures, includeSubStages);
            
            //If we don't need to perform several queries with a LIMIT clause, return
            if (offsetParamIndex == 0) {
                return log.exit(new MySQLExpressionCallTOResultSet(stmt, originalAttrs));
            } 
            //In case we need to perform several queries with a LIMIT clause, 
            //determine whether we need to filter duplicated results over several queries.
            boolean filterDistinct = !(originalAttrs.contains(ExpressionCallDAO.Attribute.ID) || 
                    (originalAttrs.contains(ExpressionCallDAO.Attribute.GENE_ID) && 
                            originalAttrs.contains(ExpressionCallDAO.Attribute.ANAT_ENTITY_ID) && 
                            originalAttrs.contains(ExpressionCallDAO.Attribute.STAGE_ID)));
            
            return log.exit(new MySQLExpressionCallTOResultSet(stmt, originalAttrs, offsetParamIndex, 
                    offsetParamIndex + 1, this.getManager().getExprPropagationGeneCount(), filterDistinct));
            
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
    //
    @Deprecated
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

        String sql = this.generateSelectClause(this.getAttributes(), true, 
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
                stmt.setStringsToIntegers(1, speciesIds, true);
            }

            if (!isIncludeSubStages) {
                return log.exit(new MySQLExpressionCallTOResultSet(stmt, this.getAttributes()));
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
            
            return log.exit(new MySQLExpressionCallTOResultSet(stmt, this.getAttributes(), 
                    offsetParamIndex, rowCountParamIndex, rowCount, filterDuplicates));
            
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }
    
    /**
     * Generates the SELECT clause of a MySQL query used to retrieve {@code ExpressionCallTO}s.
     * 
     * @param attributes                A {@code Set} of {@code Attribute}s defining 
     *                                  the columns/information the query should retrieve.
     * @param distinctClause            A {@code boolean} defining the DISTINCT keyword should be used.
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
            boolean distinctClause, boolean includeSubstructures, boolean includeSubStages, 
            String exprTableName, String propagatedStageTableName) throws IllegalArgumentException {
        log.entry(attributes, distinctClause, includeSubstructures, includeSubStages, 
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
                if (distinctClause) {
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
                    //XXX: transform into a bit value for lower memory consumption?
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
    
    /**
     * Generates the WHERE or HAVING clause of an expression query. 
     * 
     * @param callFilters           A {@code LinkedHashSet} of {@code ExpressionCallDAOFilter}s, 
     *                              providing the parameters of the query.
     * @param useGeneIds            A {@code boolean} defining whether gene ID filters 
     *                              of {@code callFilters} should be used. Useful if a global 
     *                              gene filtering has already been applied. 
     * @param useSpeciesIds         A {@code boolean} defining whether species ID filters 
     *                              of {@code callFilters} should be used. Useful if a global 
     *                              species filtering has already been applied. 
     * @param exprTableName         A {@code String} that is the name of the expression table used.
     * @param stageTableName        A {@code String} that is the name of the table containing 
     *                              stages to filter.
     * @param geneTableName         A {@code String} that is the name of the gene table used.
     * @param includeSubstructures  A {@code boolean} defining whether the query requests 
     *                              to propagate calls from anatomical substructures.
     * @param includeSubStages      A {@code boolean} defining whether the query requests 
     *                              to propagate calls from child developmental stages.
     * @return                      A {@code String} that is the filtering clause of the query.
     */
    private String generateFilteringClause(LinkedHashSet<ExpressionCallDAOFilter> callFilters, 
            boolean useGeneIds, boolean useSpeciesIds, String exprTableName, String stageTableName, 
            String geneTableName, boolean includeSubstructures, boolean includeSubStages) {
        log.entry(callFilters, useGeneIds, useSpeciesIds, exprTableName, stageTableName, geneTableName, 
                includeSubstructures, includeSubStages);
        
        StringBuilder sb = new StringBuilder();
        for (ExpressionCallDAOFilter callFilter: callFilters) {
            if (sb.length() != 0) {
                sb.append("OR ");
            }
            boolean hasPreviousClause = false;
            
            //genes
            if (useGeneIds && !callFilter.getGeneIds().isEmpty()) {
                if (hasPreviousClause) {
                    sb.append("AND ");
                }
                sb.append(exprTableName).append(".geneId IN (")
                .append(BgeePreparedStatement.generateParameterizedQueryString(callFilter.getGeneIds().size()))
                .append(") ");
                hasPreviousClause = true;
            }
            
            //species
            if (useSpeciesIds && !callFilter.getSpeciesIds().isEmpty()) {
                if (hasPreviousClause) {
                    sb.append("AND ");
                }
                sb.append(geneTableName).append(".speciesId IN (")
                .append(BgeePreparedStatement.generateParameterizedQueryString(callFilter.getSpeciesIds().size()))
                .append(") ");
                hasPreviousClause = true;
            }
            
            //conditions
            if (!callFilter.getConditionFilters().isEmpty()) {
                if (hasPreviousClause) {
                    sb.append("AND ");
                }
                sb.append("(");
                sb.append(callFilter.getConditionFilters().stream()
                    .map(cond -> {
                        StringBuilder sb2 = new StringBuilder();
                        if (!cond.getAnatEntitieIds().isEmpty()) {
                            sb2.append(exprTableName).append(".anatEntityId IN (")
                            .append(BgeePreparedStatement.generateParameterizedQueryString(
                                    cond.getAnatEntitieIds().size())).append(") ");
                        }
                        if (!cond.getDevStageIds().isEmpty()) {
                            if (sb2.length() != 0) {
                                sb2.append("AND ");
                            }
                            sb2.append(stageTableName).append(".stageId IN (")
                            .append(BgeePreparedStatement.generateParameterizedQueryString(
                                    cond.getDevStageIds().size())).append(") ");
                        }
                        return sb2.toString();
                    }).collect(Collectors.joining("OR "))
                );
                sb.append(") ");
                hasPreviousClause = true;
            }
            
            //CallTOs
            StringBuilder sb2 = new StringBuilder();
            for (ExpressionCallTO callTO: callFilter.getCallTOFilters()) {
                if (sb2.length() != 0) {
                    sb2.append("OR ");
                }
                boolean callTOClauseStarted = false;
                
                //data filtering (affymetrixData, rnaSeqData, ...)
                String dataFilter = callFilter.extractFilteringDataTypes(callTO).keySet().stream()
                    //we don't use the expression table name here, maybe these parameters 
                    //were computed using a MAX and a GROUP BY, and renamed using a AS statement.
                    .map(attr -> convertDataTypeAttrToColName(attr) + " >= ? ")
                    .collect(Collectors.joining("AND "));
                if (!dataFilter.isEmpty()) {
                    sb2.append(dataFilter);
                    callTOClauseStarted = true;
                }
                
                //origin of call from propagation
                if (callTO.getAnatOriginOfLine() != null && includeSubstructures) {
                    if (callTOClauseStarted) {
                        sb2.append("AND ");
                    } 
                    if (!includeSubStages) {
                        sb2.append(exprTableName).append(".originOfLine = ? ");
                    } else {
                        sb2.append("anatOriginOfLine = ? ");
                    }
                    callTOClauseStarted = true;
                }
                if (callTO.getStageOriginOfLine() != null && includeSubStages) {
                    if (callTOClauseStarted) {
                        sb2.append("AND ");
                    } 
                    sb2.append("stageOriginOfLine = ? ");
                    callTOClauseStarted = true;
                }
                if (callTO.isObservedData() != null && (includeSubStages || includeSubstructures)) {
                    if (callTOClauseStarted) {
                        sb2.append("AND ");
                    } 
                    if (!includeSubStages && includeSubstructures) {
                        //if we propagate only from substructures, we check the column 
                        //originOfLine in the globalExpression table: if it equals to 'descent', 
                        //then there are no observed data.
                        sb2.append("IF(originOfLine = 'descent', 0, 1) ");
                    } else if (includeSubStages) {
                        //if includeSubStages is true, then it means that this parameter 
                        //can only be filtered from the HAVING clause, so we directly have access 
                        //to the parameter name
                        sb2.append("observedData ");
                    }
                    sb2.append("= ? ");
                    callTOClauseStarted = true;
                }
            }
            if (sb2.length() != 0) {
                if (hasPreviousClause) {
                    sb.append("AND ");
                }
                sb.append("(").append(sb2.toString()).append(") ");
                hasPreviousClause = true;
            }
            
            if (sb.length() != 0) {
                sb.insert(0, "(");
                sb.append(") ");
            }
        }
        
        return log.exit(sb.toString());
    }
    
    private int parameterizeFilteringClause(BgeePreparedStatement stmt, int startIndex, 
            LinkedHashSet<ExpressionCallDAOFilter> callFilters, 
            boolean useGeneIds, boolean useSpeciesIds, 
            boolean includeSubstructures, boolean includeSubStages) throws SQLException {
        log.entry(callFilters, useGeneIds, useSpeciesIds, includeSubstructures, includeSubStages);
        
        int index = startIndex;
        for (ExpressionCallDAOFilter callFilter: callFilters) {
            //genes
            if (useGeneIds && !callFilter.getGeneIds().isEmpty()) {
                stmt.setStrings(index, callFilter.getGeneIds(), true);
                index += callFilter.getGeneIds().size();
            }
            //species
            if (useSpeciesIds && !callFilter.getSpeciesIds().isEmpty()) {
                stmt.setStringsToIntegers(index, callFilter.getSpeciesIds(), true);
                index += callFilter.getSpeciesIds().size();
            }
            
            //conditions
            for (DAOConditionFilter cond: callFilter.getConditionFilters()) {
                if (!cond.getAnatEntitieIds().isEmpty()) {
                    stmt.setStrings(index, cond.getAnatEntitieIds(), true);
                    index += cond.getAnatEntitieIds().size();
                }
                if (!cond.getDevStageIds().isEmpty()) {
                    stmt.setStrings(index, cond.getDevStageIds(), true);
                    index += cond.getDevStageIds().size();
                }
            }
            
            //CallTOs
            for (ExpressionCallTO callTO: callFilter.getCallTOFilters()) {
                //data filtering (affymetrixData, rnaSeqData, ...)
                for (DataState state: callFilter.extractFilteringDataTypes(callTO).values()) {
                    stmt.setInt(index, convertDataStateToInt(state)); 
                    index++;
                }
                //origin of call from propagation
                if (callTO.getAnatOriginOfLine() != null && includeSubstructures) {
                    stmt.setEnumDAOField(index, callTO.getAnatOriginOfLine()); 
                    index++;
                }
                if (callTO.getStageOriginOfLine() != null && includeSubStages) {
                    stmt.setEnumDAOField(index, callTO.getStageOriginOfLine()); 
                    index++;
                }
                if (callTO.isObservedData() != null && (includeSubStages || includeSubstructures)) {
                    if (callTO.isObservedData()) {
                        stmt.setInt(index, 1);
                    } else {
                        stmt.setInt(index, 0);
                    }
                    index++;
                }
            }
        }
        
        return log.exit(index);
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
         * A {@code Set} of {@code ExpressionCallDAO.Attribute} defining which columns 
         * were originally requested. This is needed because, in {@code MySQLExpressionDAO}, 
         * some SELECT clauses can be added to the query to be accessible from a HAVING clause, 
         * while it is not needed to populate the retrieved {@code ExpressionCallTO}s 
         * with these data.
         */
        private final Set<ExpressionCallDAO.Attribute> attributes;

        /**
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)}
         * super constructor.
         * 
         * @param statement             The first {@code BgeePreparedStatement} to execute a query on.
         * @param attributes            A {@code Set} of {@code ExpressionCallDAO.Attribute} 
         *                              defining which columns were originally requested. 
         *                              This is needed because, in {@code MySQLExpressionDAO}, 
         *                              some SELECT clauses can be added to the query to be accessible 
         *                              from a HAVING clause, while it is not needed to populate 
         *                              the retrieved {@code ExpressionCallTO}s with these data.
         */
        private MySQLExpressionCallTOResultSet(BgeePreparedStatement statement, 
                Set<ExpressionCallDAO.Attribute> attributes) {
            super(statement);
            this.attributes = attributes;
        }
        /**
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement, 
         * int, int, int)} super constructor.
         * 
         * @param statement             The first {@code BgeePreparedStatement} to execute 
         *                              a query on.
         * @param attributes            A {@code Set} of {@code ExpressionCallDAO.Attribute} 
         *                              defining which columns were originally requested. 
         *                              This is needed because, in {@code MySQLExpressionDAO}, 
         *                              some SELECT clauses can be added to the query to be accessible 
         *                              from a HAVING clause, while it is not needed to populate 
         *                              the retrieved {@code ExpressionCallTO}s with these data.
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
                Set<ExpressionCallDAO.Attribute> attributes, 
                int offsetParamIndex, int rowCountParamIndex, int rowCount, 
                boolean filterDuplicates) {
            super(statement, offsetParamIndex, rowCountParamIndex, rowCount, 0, filterDuplicates);
            this.attributes = attributes;
        }

        @Override
        protected ExpressionCallTO getNewTO() throws DAOException {
            log.entry();

            String id = null, geneId = null, anatEntityId = null, stageId = null;
            DataState affymetrixData = null, estData = null, inSituData = null, rnaSeqData = null;
            Boolean includeSubstructures = null, includeSubStages = null, observedData = null;
            OriginOfLine anatOriginOfLine = null, stageOriginOfLine = null;

            //every call to values() returns a newly cloned array, so we cache the array
            DataState[] dataStates = DataState.values();
            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                ExpressionCallDAO.Attribute attr = this.getAttributeFromColName(column.getValue());
                if (this.attributes != null && !this.attributes.isEmpty() && !this.attributes.contains(attr)) {
                    continue;
                }
                try {
                    switch(attr) {
                    case ID:
                        id = this.getCurrentResultSet().getString(column.getKey());
                        break;
                    case GENE_ID:
                        geneId = this.getCurrentResultSet().getString(column.getKey());
                        break;
                    case ANAT_ENTITY_ID:
                        anatEntityId = this.getCurrentResultSet().getString(column.getKey());
                        break;
                    case STAGE_ID:
                        stageId = this.getCurrentResultSet().getString(column.getKey());
                        break;
                    case AFFYMETRIX_DATA: 
                        //index of the enum in the mysql database corresponds to the ordinal 
                        //of DataState + 1
                        affymetrixData = 
                            dataStates[this.getCurrentResultSet().getInt(column.getKey()) - 1];
                        break;
                    case EST_DATA:
                       //index of the enum in the mysql database corresponds to the ordinal 
                        //of DataState + 1
                        estData = 
                            dataStates[this.getCurrentResultSet().getInt(column.getKey()) - 1];
                        break;
                    case IN_SITU_DATA: 
                        //index of the enum in the mysql database corresponds to the ordinal 
                        //of DataState + 1
                        inSituData = 
                            dataStates[this.getCurrentResultSet().getInt(column.getKey()) - 1];
                        break;
                    case RNA_SEQ_DATA:
                        //index of the enum in the mysql database corresponds to the ordinal 
                        //of DataState + 1
                        rnaSeqData = 
                            dataStates[this.getCurrentResultSet().getInt(column.getKey()) - 1];
                        break;
                    case ANAT_ORIGIN_OF_LINE: 
                        anatOriginOfLine = OriginOfLine.convertToOriginOfLine(
                                this.getCurrentResultSet().getString(column.getKey()));
                        break;
                    case STAGE_ORIGIN_OF_LINE: 
                        stageOriginOfLine = OriginOfLine.convertToOriginOfLine(
                                this.getCurrentResultSet().getString(column.getKey()));
                        break;
                    case INCLUDE_SUBSTRUCTURES: 
                        includeSubstructures = 
                                this.getCurrentResultSet().getBoolean(column.getKey());
                        break;
                    case INCLUDE_SUBSTAGES: 
                        includeSubStages = 
                                this.getCurrentResultSet().getBoolean(column.getKey());
                        break;
                    case OBSERVED_DATA:
                        observedData = this.getCurrentResultSet().getBoolean(column.getKey());
                        break;
                    default: 
                        throw log.throwing(new IllegalStateException("Unsupported Attribute: " + attr));
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
        
        private ExpressionCallDAO.Attribute getAttributeFromColName(String colName) 
                throws UnrecognizedColumnException{
            log.entry(colName);
            
            if (colName.equals("expressionId") || colName.equals("globalExpressionId")) {
                return log.exit(ExpressionCallDAO.Attribute.ID);
            } 
            if (colName.equals("geneId")) {
                return log.exit(ExpressionCallDAO.Attribute.GENE_ID);
            } 
            if (colName.equals("anatEntityId")) {
                return log.exit(ExpressionCallDAO.Attribute.ANAT_ENTITY_ID);
            } 
            if (colName.equals("stageId")) {
                return log.exit(ExpressionCallDAO.Attribute.STAGE_ID);
            } 
            if (colName.equals("affymetrixData")) {
                return log.exit(ExpressionCallDAO.Attribute.AFFYMETRIX_DATA);
            } 
            if (colName.equals("estData")) {
                return log.exit(ExpressionCallDAO.Attribute.EST_DATA);
            } 
            if (colName.equals("inSituData")) {
                return log.exit(ExpressionCallDAO.Attribute.IN_SITU_DATA);
            } 
            if (colName.equals("rnaSeqData")) {
                return log.exit(ExpressionCallDAO.Attribute.RNA_SEQ_DATA);
            } 
            if (colName.equals("originOfLine") || colName.equals("anatOriginOfLine")) {
                return log.exit(ExpressionCallDAO.Attribute.ANAT_ORIGIN_OF_LINE);
            } 
            if (colName.equals("stageOriginOfLine")) {
                return log.exit(ExpressionCallDAO.Attribute.STAGE_ORIGIN_OF_LINE);
            } 
            if (colName.equals("includeSubstructures")) {
                return log.exit(ExpressionCallDAO.Attribute.INCLUDE_SUBSTRUCTURES);
            } 
            if (colName.equals("includeSubStages")) {
                return log.exit(ExpressionCallDAO.Attribute.INCLUDE_SUBSTAGES);
            } 
            if (colName.equals("observedData")) {
                return log.exit(ExpressionCallDAO.Attribute.OBSERVED_DATA);
            }
            
            throw log.throwing(new UnrecognizedColumnException(colName));
        }
    }
    
    /**
     * A {@code MySQLDAOResultSet} specific to {@code GlobalExpressionToExpressionTO}.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public class MySQLGlobalExpressionToExpressionTOResultSet extends MySQLDAOResultSet<GlobalExpressionToExpressionTO> 
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

            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("globalExpressionId")) {
                        globalExpressionId = this.getCurrentResultSet().getString(column.getKey());

                    } else if (column.getValue().equals("expressionId")) {
                        expressionId = this.getCurrentResultSet().getString(column.getKey());

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
