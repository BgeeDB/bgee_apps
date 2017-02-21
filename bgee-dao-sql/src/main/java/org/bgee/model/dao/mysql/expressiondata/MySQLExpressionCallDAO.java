package org.bgee.model.dao.mysql.expressiondata;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.CallDAOFilter;
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
//TODO: FINAL SOLUTION!! the DAO should never "merge" calls when include substructures 
//or substages is true, it should return ALL calls. Meaning that it should not be possible to query, 
//e.g. includeSubstructures=true AND EST=HIGH AND Affy=HIGH, because this information needs call "merging". 
//Rather, all calls, in a structure should be retrieved, including calls in substructures, 
//that satisfy EST=HIGH OR Affy=HIGH. Call merging should be performed solely in bgee-core.
//=> NO MORE GROUP BY NOR HAVING CLAUSES NEEDED!!
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
     * {@code getAffymetrixMeanRank}, etc); or any rank parameter;
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
            Collection<CallDAOFilter> callFilters, Collection<ExpressionCallTO> callTOFilters, 
            boolean includeSubstructures, boolean includeSubStages, 
            Collection<String> globalGeneIds, String taxonId, 
            Collection<ExpressionCallDAO.Attribute> attributes, 
            LinkedHashMap<ExpressionCallDAO.OrderingAttribute, DAO.Direction> orderingAttributes) 
                    throws DAOException, IllegalArgumentException {
        log.entry(callFilters, includeSubstructures, includeSubStages, globalGeneIds, taxonId, 
                attributes, orderingAttributes);

        //needs a LinkedHashSet for consistent settings of the parameters. 
        LinkedHashSet<CallDAOFilter> clonedCallFilters = Optional.ofNullable(callFilters)
                .map(e -> new LinkedHashSet<>(e)).orElse(new LinkedHashSet<>());
        //same for CallTOs
        LinkedHashSet<ExpressionCallTO> clonedCallTOFilters = Optional.ofNullable(callTOFilters)
                .map(e -> new LinkedHashSet<>(e)).orElse(new LinkedHashSet<>());
        //attributes
        Set<ExpressionCallDAO.Attribute> originalAttrs = Optional.ofNullable(attributes)
                .map(e -> EnumSet.copyOf(e)).orElse(EnumSet.allOf(ExpressionCallDAO.Attribute.class));

        if (attributes == null && includeSubstructures) {
        //if attributes is null we filter out the rankAttributes, since they are not available
            originalAttrs = originalAttrs.stream()
                    .filter(a -> !a.isRankAttribute())
                    .collect(Collectors.toSet());
        }
        //ordering attributes
        LinkedHashMap<ExpressionCallDAO.OrderingAttribute, DAO.Direction> clonedOrderingAttrs = 
                Optional.ofNullable(orderingAttributes)
                .map(e -> new LinkedHashMap<>(orderingAttributes)).orElse(new LinkedHashMap<>());
        
        //ranks are not accessible from the globalExpression table
        if (includeSubstructures && 
                (originalAttrs.stream().anyMatch(e -> e.isRankAttribute()) || 
                 clonedOrderingAttrs.keySet().contains(ExpressionCallDAO.OrderingAttribute.MEAN_RANK))) {
            throw log.throwing(new IllegalArgumentException(
                    "Rank information is not accessible when includeSubstructures is true."));
        }
        
        if (taxonId != null && !taxonId.isEmpty()) {
            throw log.throwing(new UnsupportedOperationException(
                    "Query using gene orthology not yet implemented"));
        }
        
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
        //or some specific attributes are requested, the filtering of some parameters will need to be done 
        //in a HAVING clause. In that case, we add the columns we will need in the HAVING clause, 
        //based on the parameters provided, to avoid over-verbose HAVING clause. 
        //We also need these columns if an ordering is requested on them. 
        Set<ExpressionCallDAO.Attribute> updatedAttrs = EnumSet.copyOf(originalAttrs);
        boolean havingClauseNeeded = false;
        boolean groupingByNeeded = false;
        
        //if we want to order based on global rank, as it is not a real column we need 
        //to add it to the SELECT clause for simplicity. 
        if (clonedOrderingAttrs.keySet().contains(ExpressionCallDAO.OrderingAttribute.MEAN_RANK)) {
            updatedAttrs.add(ExpressionCallDAO.Attribute.GLOBAL_MEAN_RANK);
        }
        
        //boolean to know whether attributes based on primary key or full unique index are requested, 
        //this is important to know if a GROUP BY is needed in some situations.
        boolean primaryKey = (originalAttrs.contains(ExpressionCallDAO.Attribute.ID) || 
                (originalAttrs.contains(ExpressionCallDAO.Attribute.GENE_ID) && 
                 originalAttrs.contains(ExpressionCallDAO.Attribute.CONDITION_ID)));
        
        if (includeSubStages || !primaryKey) {
            if (originalAttrs.stream().anyMatch(e -> e.isDataTypeAttribute()) || 
                    originalAttrs.stream().anyMatch(e -> e.isRankAttribute()) || 
                    ((includeSubStages || includeSubstructures) && 
                            originalAttrs.contains(ExpressionCallDAO.Attribute.OBSERVED_DATA)) || 
                    (includeSubstructures && 
                            originalAttrs.contains(ExpressionCallDAO.Attribute.ANAT_ORIGIN_OF_LINE)) || 
                    (includeSubStages && 
                            originalAttrs.contains(ExpressionCallDAO.Attribute.STAGE_ORIGIN_OF_LINE)) || 
                    clonedOrderingAttrs.keySet().contains(ExpressionCallDAO.OrderingAttribute.MEAN_RANK)) {
                
                groupingByNeeded = true;
                log.trace("GROUP BY needed because of requested Attributes or ordering.");
            }
            
            //for filtering based on data types, we can still avoid to filter in the HAVING clause 
            //or to use a GROUP BY, if there is no GROUP BY needed because of requested attributes 
            //(test above), and if there is no AND condition between data types.
            //First, retrieve all data types with a specified filtering, per CallTO.
            Set<Set<ExpressionCallDAO.Attribute>> filteringDataTypesPerCallTO = clonedCallTOFilters
                    .stream()
                    .map(callTO -> callTO.extractFilteringDataTypes().keySet())
                    .collect(Collectors.toSet());
            if (groupingByNeeded || 
                    //check whether there is any AND condition between data types
                    filteringDataTypesPerCallTO.stream().anyMatch(set -> set.size() > 1)) {
                if (log.isTraceEnabled() && !groupingByNeeded) {
                    log.trace("GROUP BY needed because of AND conditions between data types. {}", 
                            filteringDataTypesPerCallTO);
                }

                //FIXME: Actually, if it is needed to compute some rank scores, the filtering must be done 
                //in the WHERE clause. Otherwise, if we want to compute ranks only based on, e.g., Affymetrix, 
                //filtering in the HAVING clause would lead to consider all calls for, e.g., a gene-anat, 
                //as long as at least one is supported by Affymetrix data.
                if (updatedAttrs.stream()
                        .anyMatch(attr -> attr.isDataTypeAttribute() || attr.isPropagationAttribute()) || 
                        filteringDataTypesPerCallTO.stream().flatMap(attrs -> attrs.stream())
                        .collect(Collectors.toSet()).size() > 1) {
                    havingClauseNeeded = true;
                    //add in the SELECT clause the columns we will need in the HAVING clause, 
                    //to avoid over-verbose HAVING clause.
                    updatedAttrs.addAll(filteringDataTypesPerCallTO.stream()
                            .flatMap(Set::stream)
                            .collect(Collectors.toSet()));
                }
                groupingByNeeded = true;
            }
            
            if ((includeSubStages || includeSubstructures) && 
                    clonedCallTOFilters.stream().anyMatch(e -> e.isObservedData() != null)) {
                //FIXME: opposite problem: if we request Affymetrix data only, with the HAVING clause 
                //we will determine whether a call having *some* affymetrix data has been observed 
                //*somewhere* (maybe somewhere else). Is it really what we want?
                log.warn("having clause needed");
                havingClauseNeeded = true;
                groupingByNeeded = true;
                //add in the SELECT clause the columns we will need in the HAVING clause, 
                //to avoid over-verbose HAVING clause.
                updatedAttrs.add(ExpressionCallDAO.Attribute.OBSERVED_DATA);
                log.trace("GROUP BY needed because of filtering based on OBSERVED_DATA.");
            }
            if (includeSubstructures && 
                    clonedCallTOFilters.stream().anyMatch(e -> e.getAnatOriginOfLine() != null)) {
                //FIXME: same here
                log.warn("having clause needed");
                havingClauseNeeded = true;
                groupingByNeeded = true;
                //add in the SELECT clause the columns we will need in the HAVING clause, 
                //to avoid over-verbose HAVING clause.
                updatedAttrs.add(ExpressionCallDAO.Attribute.ANAT_ORIGIN_OF_LINE);
                log.trace("GROUP BY needed because of filtering based on ANAT_ORIGIN_OF_LINE.");
            }
            if (includeSubStages && 
                    clonedCallTOFilters.stream().anyMatch(e -> e.getStageOriginOfLine() != null)) {
                //FIXME: same here
                log.warn("having clause needed");
                havingClauseNeeded = true;
                groupingByNeeded = true;
                //add in the SELECT clause the columns we will need in the HAVING clause, 
                //to avoid over-verbose HAVING clause.
                updatedAttrs.add(ExpressionCallDAO.Attribute.STAGE_ORIGIN_OF_LINE);
                log.trace("GROUP BY needed because of filtering based on STAGE_ORIGIN_OF_LINE.");
            }

            //FIXME: THIS ALL GROUP BY / INCLUDE SUBSTAGES STUFF IS BROKEN. Let's think 
            //if it is needed if we manage propagation in bgee-core.
            //Poor fix meanwhile.
            if (havingClauseNeeded && updatedAttrs.stream().anyMatch(attr -> attr.isRankAttribute())) {
                throw log.throwing(new IllegalArgumentException(
                        "Retrieval of ranks when a HAVING clause is needed is not supported."));
            }
            
            if (log.isWarnEnabled() && includeSubStages && groupingByNeeded && 
                    (geneIds.isEmpty() || geneIds.size() > this.getManager().getExprPropagationGeneCount())) {
                log.warn("IncludeSubStages is true and some parameters highly costly to compute "
                        + "are needed, this will take lots of time... "
                        + "You should rather compute these results in several queries.");
            }
        }
        
        //determine the attributes on which to perform the GROUP BY
        Set<ExpressionCallDAO.Attribute> groupByAttrs = null;
        if (groupingByNeeded) {
            groupByAttrs = originalAttrs.stream()
                .filter(e -> e.equals(ExpressionCallDAO.Attribute.ID) || 
                        e.equals(ExpressionCallDAO.Attribute.GENE_ID) || 
                        e.equals(ExpressionCallDAO.Attribute.CONDITION_ID))
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(ExpressionCallDAO.Attribute.class)));
        }

        //**********************************************
        
        //execute query
        return log.exit(this.getExpressionCalls(clonedCallFilters, clonedCallTOFilters, 
                geneIds, globalGeneFilter, speciesIds, globalSpeciesFilter, 
                includeSubstructures, includeSubStages, 
                taxonId, originalAttrs, updatedAttrs, clonedOrderingAttrs, groupByAttrs, 
                havingClauseNeeded));
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

        String sql = "SELECT MAX(" + id + ") AS exprId FROM " + tableName;
    
        try (MySQLExpressionCallTOResultSet resultSet = new MySQLExpressionCallTOResultSet(
                this.getManager().getConnection().prepareStatement(sql), null)) {
            
            if (resultSet.next() && resultSet.getTO().getId() != null) {
                return log.exit(resultSet.getTO().getId());
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
    //TODO: update javadoc
    private ExpressionCallTOResultSet getExpressionCalls(
            LinkedHashSet<CallDAOFilter> callFilters, LinkedHashSet<ExpressionCallTO> callTOFilters, 
            Set<String> allGeneIds, boolean globalGeneFilter, 
            Set<String> allSpeciesIds, boolean globalSpeciesFilter, 
            boolean includeSubstructures, final boolean includeSubStages, 
            String commonAncestralTaxonId, 
            Set<ExpressionCallDAO.Attribute> originalAttrs, Set<ExpressionCallDAO.Attribute> updatedAttrs, 
            LinkedHashMap<ExpressionCallDAO.OrderingAttribute, DAO.Direction> orderingAttrs, 
            Set<ExpressionCallDAO.Attribute> groupByAttrs, boolean havingClauseNeeded) throws DAOException {
        
        log.entry(callFilters, callTOFilters, allGeneIds, globalGeneFilter, 
                allSpeciesIds, globalSpeciesFilter, 
                includeSubstructures, includeSubStages, 
                commonAncestralTaxonId, originalAttrs, updatedAttrs, orderingAttrs, 
                groupByAttrs, havingClauseNeeded);

        // Construct sql query
        final String exprTableName = includeSubstructures? "globalExpression": "expression";
        final String propagatedStageTableName = "propagatedStage";
        //if no filtering based on stage ID is requested, and no group by on stage is needed, 
        //then we don't need the join to stage table
        final boolean realIncludeSubStages = includeSubStages && 
                (updatedAttrs.contains(ExpressionCallDAO.Attribute.ID)
                ||
                callFilters.stream().anyMatch(
                        callFilter -> callFilter.getConditionFilters().stream().anyMatch(
                                condFilter -> !condFilter.getDevStageIds().isEmpty())) 
                || 
                groupByAttrs != null);

        //determine whether to use DISTINCT keyword.
        boolean distinct = !(groupByAttrs != null || (!realIncludeSubStages && 
                (updatedAttrs.contains(ExpressionCallDAO.Attribute.ID) || 
                (updatedAttrs.contains(ExpressionCallDAO.Attribute.GENE_ID) && 
                 updatedAttrs.contains(ExpressionCallDAO.Attribute.CONDITION_ID)))));
        String sql = this.generateSelectClause(
                updatedAttrs, 
                //Attributes corresponding to data types used for filtering the results
                callTOFilters.stream()
                    .flatMap(callTO -> callTO.extractFilteringDataTypes().keySet().stream())
                    .collect(Collectors.toCollection(() -> 
                             EnumSet.noneOf(ExpressionCallDAO.Attribute.class))), 
                distinct, groupByAttrs != null, 
                includeSubstructures, realIncludeSubStages, exprTableName, propagatedStageTableName);
        
        sql += " FROM " + exprTableName;

        //a boolean to determine whether genes and species were already filtered because it is needed 
        //to perform the query in several iterations, because includeSubStages is true 
        //and a high number of genes has to be considered. 
        boolean genesAndSpeciesFilteredForPropagation = false;
        if (realIncludeSubStages) {
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
            
            //XXX: maybe this condition should not be under the 'includeSubStages' condition, 
            //to be used if necessary for all GROUP BY cases. But we hope it is not needed 
            //when there is no propagation to parent stages, we'll see. 
            if (groupByAttrs != null && (allGeneIds.isEmpty() || 
                    allGeneIds.size() > this.getManager().getExprPropagationGeneCount())) {
                
                genesAndSpeciesFilteredForPropagation = true;
                
                sql += " INNER JOIN (SELECT bgeeGeneId from gene AS tempGene where exists " 
                    + "(select 1 from expression where expression.bgeeGeneId = tempGene.bgeeGeneId) ";
                if (!allSpeciesIds.isEmpty()) {
                    sql += "AND tempGene.speciesId IN (" + BgeePreparedStatement.generateParameterizedQueryString(
                            allSpeciesIds.size()) + ") ";
                }
                if (!allGeneIds.isEmpty()) {
                    sql += "AND tempGene.bgeeGeneId IN (" + BgeePreparedStatement.generateParameterizedQueryString(
                            allGeneIds.size()) + ") ";
                }
                sql += "ORDER BY tempGene.bgeeGeneId LIMIT ?, ?) as tempTable on " 
                    + exprTableName + ".bgeeGeneId = tempTable.bgeeGeneId ";
            }
        }
        //even if we already joined the gene table because includeSubStages is true, 
        //we need to access to this table in the main query for the species filtering 
        //defined in the CallFilters. 
        if (!allSpeciesIds.isEmpty() || 
                callFilters.stream().anyMatch(filter -> !filter.getSpeciesIds().isEmpty())) {
            sql += " INNER JOIN gene ON (gene.bgeeGeneId = " + exprTableName + ".bgeeGeneId) ";
        }
        //If a global gene filter is defined, we can apply it to all conditions in all cases, 
        //only if not already apply to the sub-query. 
        boolean whereClauseStarted = false;
        if (!allGeneIds.isEmpty() && !genesAndSpeciesFilteredForPropagation && globalGeneFilter) {
            sql += " WHERE " + exprTableName + ".bgeeGeneId IN (" 
                + BgeePreparedStatement.generateParameterizedQueryString(allGeneIds.size()) + ") ";
            whereClauseStarted = true;
        }
        //If a global species filter is defined, we can apply it to all conditions in all cases, 
        //only if not already apply to the sub-query. 
        if (!allSpeciesIds.isEmpty() && !genesAndSpeciesFilteredForPropagation && globalSpeciesFilter) {
            if (!whereClauseStarted) {
                sql += "WHERE ";
            } else {
                sql += "AND ";
            }
            sql += "gene.speciesId IN (" + BgeePreparedStatement.generateParameterizedQueryString(
                            allSpeciesIds.size()) + ") ";
            whereClauseStarted = true;
        }
        
        String callDAOFilterClause = this.generateCallDAOFilterClause(callFilters, 
                !globalGeneFilter, !globalSpeciesFilter, 
                exprTableName, 
                realIncludeSubStages? propagatedStageTableName: exprTableName,
                "gene");
        if (!callDAOFilterClause.isEmpty()) {
            if (!whereClauseStarted) {
                sql += "WHERE ";
            } else {
                sql += "AND ";
            }
            sql += callDAOFilterClause;
            whereClauseStarted = true;
        }

        //The filtering based on CallTOs must be done in a HAVING clause in case of GROUP BY, 
        //otherwise, in the WHERE clause. 
        String callTOFilterClause = this.generateCallTOFilterClause(callTOFilters, 
                havingClauseNeeded? null: exprTableName, 
                includeSubstructures, includeSubStages, groupByAttrs != null, havingClauseNeeded);
        if (!havingClauseNeeded && !callTOFilterClause.isEmpty()) {
            if (!whereClauseStarted) {
                sql += "WHERE ";
            } else {
                sql += "AND ";
            }
            sql += callTOFilterClause;
            whereClauseStarted = true;
        }
        
        if (groupByAttrs != null) {
            assert !groupByAttrs.isEmpty(); 
            
            sql += groupByAttrs.stream().map(attr -> {
                    switch (attr) {
                    case ID: 
                        return "exprId";
                    case GENE_ID: 
                        return exprTableName + ".bgeeGeneId";
                    case CONDITION_ID: 
                        return exprTableName + ".conditionId";
                    default: 
                        throw log.throwing(new IllegalStateException("GROUP BY Attribute not supported: "
                                + attr));
                    }
                }).collect(Collectors.joining(", ", " GROUP BY ", ""));
            
            if (havingClauseNeeded && !callTOFilterClause.isEmpty()) {
                sql += " HAVING " + callTOFilterClause;
            }
        }
        
        //ORDER BY
        if (!orderingAttrs.isEmpty()) {
            sql += orderingAttrs.entrySet().stream()
                .map(entry -> {
                    String orderBy = "";
                    switch(entry.getKey()) {
                    case MEAN_RANK: 
                        orderBy = "globalMeanRank";
                        break;
                    case GENE_ID: 
                        orderBy = exprTableName + ".bgeeGeneId";
                        break;
                    case CONDITION_ID: 
                        orderBy = exprTableName + ".conditionId";
                        break;
                    default: 
                        throw log.throwing(new IllegalStateException("Unsupported OrderingAttribute: " 
                                + entry.getKey()));
                    }
                    switch(entry.getValue()) {
                    case DESC: 
                        orderBy += " desc";
                        break;
                    case ASC: 
                        orderBy += " asc";
                        break;
                    default: 
                        throw log.throwing(new IllegalStateException("Unsupported Direction: " 
                                + entry.getValue()));
                    }
                    return orderBy;
                })
                .collect(Collectors.joining(", ", " ORDER BY ", ""));
        }
        

        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            
            int index = 1;
            int offsetParamIndex = 0;
            if (realIncludeSubStages && groupByAttrs != null && (allGeneIds.isEmpty() || 
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
            
            if (!allGeneIds.isEmpty() && !genesAndSpeciesFilteredForPropagation && globalGeneFilter) {
                stmt.setStrings(index, allGeneIds, true);
                index += allGeneIds.size();
            }
            if (!allSpeciesIds.isEmpty() && !genesAndSpeciesFilteredForPropagation && globalSpeciesFilter) {
                stmt.setStringsToIntegers(index, allSpeciesIds, true);
                index += allSpeciesIds.size();
            }
            
            index = this.parameterizeCallDAOFilterClause(stmt, index, callFilters, !globalGeneFilter, 
                    !globalSpeciesFilter);
            index = this.parameterizeCallTOFilterClause(stmt, index, callTOFilters, 
                    includeSubstructures, includeSubStages);
            
            //If we don't need to perform several queries with a LIMIT clause, return
            if (offsetParamIndex == 0) {
                return log.exit(new MySQLExpressionCallTOResultSet(stmt, originalAttrs));
            } 
            //In case we need to perform several queries with a LIMIT clause, 
            //determine whether we need to filter duplicated results over several queries.
            boolean filterDistinct = !originalAttrs.contains(ExpressionCallDAO.Attribute.ID) && 
                    !originalAttrs.contains(ExpressionCallDAO.Attribute.GENE_ID);
            
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

        String sql = this.generateSelectClause(
                //make sure this method keeps working while we implement new features 
                //in the non-deprecated methods. 
                Optional.ofNullable(this.getAttributes())
                    .map(e -> e.isEmpty()? EnumSet.allOf(ExpressionCallDAO.Attribute.class): e)
                    .orElse(EnumSet.allOf(ExpressionCallDAO.Attribute.class))
                    .stream().filter(e -> !e.isRankAttribute())
                    .collect(Collectors.toSet()), 
                null, 
                true, isIncludeSubStages, 
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
            sql += " INNER JOIN (SELECT bgeeGeneId from gene where exists " +
            		"(select 1 from expression where expression.bgeeGeneId = gene.bgeeGeneId) ";
        }
        if (speciesIds != null && speciesIds.size() > 0) {
            if (isIncludeSubStages) {
                sql += "AND ";
            } else {
                sql += " INNER JOIN gene ON (gene.bgeeGeneId = " + exprTableName + ".bgeeGeneId) WHERE ";
            }
            sql += "gene.speciesId IN (" + BgeePreparedStatement.generateParameterizedQueryString(
                            speciesIds.size()) + ") ";
        }
        if (isIncludeSubStages) {
            //finish the subquery and the join to the expression table
            sql += "LIMIT ?, ?) as tempTable on " + exprTableName + ".bgeeGeneId = tempTable.bgeeGeneId ";
            //and now, finish the main query
            sql += " GROUP BY " + exprTableName + ".bgeeGeneId, " + 
                   exprTableName + ".conditionId";
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
    //TODO: update javadoc
    private String generateSelectClause(Set<ExpressionCallDAO.Attribute> attributes, 
            Set<ExpressionCallDAO.Attribute> filteringDataTypes, 
            boolean distinctClause, boolean groupByClause, 
            boolean includeSubstructures, boolean includeSubStages, 
            String exprTableName, String propagatedStageTableName) throws IllegalArgumentException {
        log.entry(attributes, filteringDataTypes, distinctClause, groupByClause, 
                includeSubstructures, includeSubStages, 
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
        
        //Ranks: 
        Map<ExpressionCallDAO.Attribute, String> dataTypeToNormRankSql = new HashMap<>();
        dataTypeToNormRankSql.put(ExpressionCallDAO.Attribute.AFFYMETRIX_DATA, 
                exprTableName + ".affymetrixMeanRankNorm ");
        dataTypeToNormRankSql.put(ExpressionCallDAO.Attribute.EST_DATA, 
                exprTableName + ".estRankNorm ");
        dataTypeToNormRankSql.put(ExpressionCallDAO.Attribute.IN_SITU_DATA, 
                exprTableName + ".inSituRankNorm ");
        dataTypeToNormRankSql.put(ExpressionCallDAO.Attribute.RNA_SEQ_DATA, 
                exprTableName + ".rnaSeqMeanRankNorm ");
        
        //for weighted mean computation: sum of numbers of distinct ranks for data using 
        //fractional ranking (Affy and RNA-Seq), max ranks for data using dense ranking 
        //and pooling of all samples in a condition (EST and in situ)
        Map<ExpressionCallDAO.Attribute, String> dataTypeToWeightSql = new HashMap<>();
        dataTypeToWeightSql.put(ExpressionCallDAO.Attribute.AFFYMETRIX_DATA, 
                exprTableName + ".affymetrixDistinctRankSum ");
        dataTypeToWeightSql.put(ExpressionCallDAO.Attribute.RNA_SEQ_DATA, 
                exprTableName + ".rnaSeqDistinctRankSum ");
        dataTypeToWeightSql.put(ExpressionCallDAO.Attribute.EST_DATA, 
                exprTableName + ".estMaxRank ");
        dataTypeToWeightSql.put(ExpressionCallDAO.Attribute.IN_SITU_DATA, 
                exprTableName + ".inSituMaxRank ");

        Set<ExpressionCallDAO.Attribute> attributesForRank = 
                (filteringDataTypes == null || filteringDataTypes.isEmpty()? 
                        EnumSet.allOf(ExpressionCallDAO.Attribute.class): filteringDataTypes)
                .stream()
                //in case we retrieved all Attributes because no filtering on data types
                .filter(dataType -> dataType.isDataTypeAttribute())
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(ExpressionCallDAO.Attribute.class)));
        //use for dividing afterwards, don't want a division by 0 :p
        assert attributesForRank.size() > 0;
        
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
                //in case we include sub-stages, we need to generate fake IDs,  
                //because equality of ExpressionCallTO can be based on ID, and here 
                //a same basic call with a given ID can be associated to different propagated calls.
                if (includeSubStages || groupByClause) {
                    log.warn("Retrieval of expression IDs with on-the-fly propagation " +
                    		"of expression calls, can increase memory usage.");
                    //XXX: transform into a bit value for lower memory consumption?
                    //see convert unsigned: http://dev.mysql.com/doc/refman/5.5/en/cast-functions.html#function_convert
                    //We always use the three attributes, otherwise it wouldn't be an ID...
                    sql += "CONCAT(" + exprTableName + ".bgeeGeneId, '__', " + 
                            exprTableName + ".conditionId) ";
                } else {
                    sql += includeSubstructures? "globalExpressionId ": "expressionId ";
                }
                sql += "AS exprId ";
            } else if (attribute.equals(ExpressionCallDAO.Attribute.GENE_ID)) {
                sql += exprTableName + ".bgeeGeneId ";
            } else if (attribute.equals(ExpressionCallDAO.Attribute.CONDITION_ID)) {
                sql += exprTableName + ".conditionId ";
                
            } else if (attribute.equals(ExpressionCallDAO.Attribute.ANAT_ORIGIN_OF_LINE)) {
                //the attribute ANAT_ORIGIN_OF_LINE corresponds to a column only 
                //in the global expression table, not in the basic expression table. 
                //So, if no propagation was requested, we add a fake column to the query 
                //to provide the information to the ResultSet consistently. 
                if (!includeSubstructures) {
                    sql += "'" + OriginOfLine.SELF.getStringRepresentation() + "' ";
                } else {
                    //otherwise, we use the real column in the global expression table, 
                    //unless we are doing a grouping by
                    if (!groupByClause) {
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
                    assert groupByClause;
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
                if (!includeSubStages && !includeSubstructures) {
                    //if no propagation requested, it's easy, all data are observed
                    sql += "1 ";
                } else if (!includeSubStages && !groupByClause) {
                    //if we propagate only from substructures, we check the column 
                    //originOfLine in the globalExpression table: if it equals to 'descent', 
                    //then there are no observed data.
                    assert includeSubstructures == true;
                    sql += anatOriginIfClause;
                } else {
                    //It's more complicated when data are grouped, we need 
                    //to use GROUP_CONCAT to examine the different origins of a line.
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
                    sql += ", '.', ";
                    if (includeSubStages) {
                        sql += stageOriginIfClause;
                    } else {
                        sql += "'1' ";
                    }
                    sql +=  ")) ";
                    //if we have at the same time observed data in the organ 
                    //and in the stage (corresponds to '1.1'), then this line 
                    //is actually observed.
                    sql += "LIKE '%1.1%', 1, 0) ";
                }
                
                sql += "AS observedData ";
                
            } else if (attribute.equals(ExpressionCallDAO.Attribute.GLOBAL_MEAN_RANK)) {
                
                //in case several raws are grouped, we retrieve the min value of the ranking score
                sql +=  (groupByClause? "MIN(": "") + attributesForRank.stream()
                            .map(attr -> {
                                String rankSql = dataTypeToNormRankSql.get(attr);
                                String weightSql = dataTypeToWeightSql.get(attr);
                                if (rankSql == null || weightSql == null) {
                                    throw log.throwing(new IllegalStateException(
                                        "No rank clause associated to data type: " + attr));
                                }
                                return "if (" + convertDataTypeAttrToColName(attr) + " + 0 = " 
                                           + convertDataStateToInt(DataState.NODATA) + ", 0, "
                                           + rankSql + " * " + weightSql + ")";
                            })
                            .collect(Collectors.joining(" + ", "((", ")")) 
                            
                      + attributesForRank.stream()
                            .map(attr -> "if (" + convertDataTypeAttrToColName(attr) + " + 0 = " 
                                                + convertDataStateToInt(DataState.NODATA) + ", 0, "
                                         + dataTypeToWeightSql.get(attr) + ")")
                            .collect(Collectors.joining(" + ", "/ (", 
                                     (groupByClause? ")": "") + ")) AS globalMeanRank "));

            } else if (attribute.equals(ExpressionCallDAO.Attribute.AFFYMETRIX_DATA)) {
                if (!groupByClause) {
                    sql += "(affymetrixData + 0) ";
                } else {
                    //if expression is propagated to parent stages, we get the best value 
                    //from all expression calls group by the propagated stage
                    sql += "MAX(affymetrixData + 0) ";
                }
                sql += "AS affymetrixData ";
            } else if (attribute.equals(ExpressionCallDAO.Attribute.AFFYMETRIX_MEAN_RANK)) {
                
                if (attributesForRank.contains(ExpressionCallDAO.Attribute.AFFYMETRIX_DATA)) {
                    sql += (groupByClause? "MIN(": "") 
                            + dataTypeToNormRankSql.get(ExpressionCallDAO.Attribute.AFFYMETRIX_DATA) 
                            + (groupByClause? ")": "");
                } else {
                    sql += "NULL";
                }
                sql += " AS affymetrixRank ";
                
            } else if (attribute.equals(ExpressionCallDAO.Attribute.EST_DATA)) {
                if (!groupByClause) {
                    sql += "(estData + 0) ";
                } else {
                    //if expression is propagated to parent stages, we get the best value 
                    //from all expression calls group by the propagated stage
                    sql += "MAX(estData + 0) ";
                }
                sql += "AS estData ";
            } else if (attribute.equals(ExpressionCallDAO.Attribute.EST_MEAN_RANK)) {
                
                if (attributesForRank.contains(ExpressionCallDAO.Attribute.EST_DATA)) {
                    sql += (groupByClause? "MIN(": "") 
                            + dataTypeToNormRankSql.get(ExpressionCallDAO.Attribute.EST_DATA) 
                            + (groupByClause? ")": "");
                } else {
                    sql += "NULL";
                }
                sql += " AS estRank ";
                
            } else if (attribute.equals(ExpressionCallDAO.Attribute.IN_SITU_DATA)) {
                if (!groupByClause) {
                    sql += "(inSituData + 0) ";
                } else {
                    //if expression is propagated to parent stages, we get the best value 
                    //from all expression calls group by the propagated stage
                    sql += "MAX(inSituData + 0) ";
                }
                sql += "AS inSituData ";
            } else if (attribute.equals(ExpressionCallDAO.Attribute.IN_SITU_MEAN_RANK)) {
                
                if (attributesForRank.contains(ExpressionCallDAO.Attribute.IN_SITU_DATA)) {
                    sql += (groupByClause? "MIN(": "") 
                            + dataTypeToNormRankSql.get(ExpressionCallDAO.Attribute.IN_SITU_DATA) 
                            + (groupByClause? ")": "");
                } else {
                    sql += "NULL";
                }
                sql += " AS inSituRank ";
                
            } else if (attribute.equals(ExpressionCallDAO.Attribute.RNA_SEQ_DATA)) {
                if (!groupByClause) {
                    sql += "(rnaSeqData + 0) ";
                } else {
                    //if expression is propagated to parent stages, we get the best value 
                    //from all expression calls group by the propagated stage
                    sql += "MAX(rnaSeqData + 0) ";
                }
                sql += "AS rnaSeqData ";
            } else if (attribute.equals(ExpressionCallDAO.Attribute.RNA_SEQ_MEAN_RANK)) {
                
                if (attributesForRank.contains(ExpressionCallDAO.Attribute.RNA_SEQ_DATA)) {
                    sql += (groupByClause? "MIN(": "") 
                            + dataTypeToNormRankSql.get(ExpressionCallDAO.Attribute.RNA_SEQ_DATA) 
                            + (groupByClause? ")": "");
                } else {
                    sql += "NULL";
                }
                sql += " AS rnaSeqRank ";
                
            } else {
                throw log.throwing(new IllegalArgumentException("The attribute provided (" +
                        attribute.toString() + ") is unknown for " + ExpressionCallDAO.class.getName()));
            }
        }
        return log.exit(sql);
    }
    
    /**
     * Generates the WHERE clause of an expression query relative to {@code CallDAOFiler}s. 
     * 
     * @param callFilters           A {@code LinkedHashSet} of {@code CallDAOFilter}s, 
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
     * @return                      A {@code String} that is the filtering clause of the query.
     */
    private String generateCallDAOFilterClause(LinkedHashSet<CallDAOFilter> callFilters, 
            boolean useGeneIds, boolean useSpeciesIds, String exprTableName, String stageTableName, 
            String geneTableName) {
        log.entry(callFilters, useGeneIds, useSpeciesIds, exprTableName, stageTableName, geneTableName);
        
        StringBuilder sb = new StringBuilder();
        for (CallDAOFilter callFilter: callFilters) {
            if (sb.length() != 0) {
                sb.append("OR ");
            }
            StringBuilder sb2 = new StringBuilder();
            boolean hasPreviousClause = false;
            
            //genes
            if (useGeneIds && !callFilter.getGeneIds().isEmpty()) {
                if (hasPreviousClause) {
                    sb2.append("AND ");
                }
                if (exprTableName != null) {
                    sb2.append(exprTableName).append(".");
                }
                sb2.append("bgeeGeneId IN (")
                .append(BgeePreparedStatement.generateParameterizedQueryString(callFilter.getGeneIds().size()))
                .append(") ");
                hasPreviousClause = true;
            }
            
            //species
            if (useSpeciesIds && !callFilter.getSpeciesIds().isEmpty()) {
                if (hasPreviousClause) {
                    sb2.append("AND ");
                }
                if (geneTableName != null) {
                    sb2.append(geneTableName).append(".");
                }
                sb2.append("speciesId IN (")
                .append(BgeePreparedStatement.generateParameterizedQueryString(callFilter.getSpeciesIds().size()))
                .append(") ");
                hasPreviousClause = true;
            }
            
            //conditions
            if (!callFilter.getConditionFilters().isEmpty()) {
                if (hasPreviousClause) {
                    sb2.append("AND ");
                }
                sb2.append("(");
                sb2.append(callFilter.getConditionFilters().stream()
                    .map(cond -> {
                        StringBuilder sb3 = new StringBuilder();
                        if (!cond.getAnatEntitieIds().isEmpty()) {
                            if (exprTableName != null) {
                                sb3.append(exprTableName).append(".");
                            }
                            sb3.append("anatEntityId IN (")
                            .append(BgeePreparedStatement.generateParameterizedQueryString(
                                    cond.getAnatEntitieIds().size())).append(") ");
                        }
                        if (!cond.getDevStageIds().isEmpty()) {
                            if (sb3.length() != 0) {
                                sb3.append("AND ");
                            }
                            if (stageTableName != null) {
                                sb3.append(stageTableName).append(".");
                            }
                            sb3.append("stageId IN (")
                            .append(BgeePreparedStatement.generateParameterizedQueryString(
                                    cond.getDevStageIds().size())).append(") ");
                        }
                        return sb3.toString();
                    }).collect(Collectors.joining("OR "))
                );
                sb2.append(") ");
                hasPreviousClause = true;
            }
            if (sb2.length() != 0) {
                sb.append("(").append(sb2.toString()).append(") ");
            }
        }
        if (sb.length() != 0) {
            sb.insert(0, "(");
            sb.append(") ");
        }
        
        return log.exit(sb.toString());
    }
    
    /**
     * Generates the WHERE or HAVING clause of an expression query relative to 
     * {@code ExpressionCallTO}s. If it is meant to be used on the HAVING clause, 
     * then no table name should be provided, calling a table name from a HAVING clause is invalid.
     * 
     * @param callTOFilters         A {@code LinkedHashSet} of {@code ExpressionCallTO}s, 
     *                              providing the parameters of the query.
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
    private String generateCallTOFilterClause(LinkedHashSet<ExpressionCallTO> callTOFilters, 
            String exprTableName, 
            boolean includeSubstructures, boolean includeSubStages, boolean groupByClause, 
            boolean havingClauseNeeded) {
        log.entry(callTOFilters, exprTableName, 
                includeSubstructures, includeSubStages, groupByClause, havingClauseNeeded);
        
        if (havingClauseNeeded && exprTableName!= null && !exprTableName.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("A table name should not be used "
                    + "in a HAVING clause."));
        }
        
        StringBuilder sb = new StringBuilder();
        for (ExpressionCallTO callTO: callTOFilters) {
            if (sb.length() != 0) {
                sb.append("OR ");
            }
            boolean hasPreviousClause = false;
            StringBuilder sb2 = new StringBuilder();
                
            //data filtering (affymetrixData, rnaSeqData, ...)
            String dataFilter = callTO.extractFilteringDataTypes().keySet().stream()
                    //we don't use the expression table name here, maybe these parameters 
                    //were computed using a MAX and a GROUP BY, and renamed using a AS statement.
                    .map(attr -> convertDataTypeAttrToColName(attr) + " >= ? ")
                    .collect(Collectors.joining("AND "));
            if (!dataFilter.isEmpty()) {
                sb2.append(dataFilter);
                hasPreviousClause = true;
            }
            
            //origin of call from propagation
            if (callTO.getAnatOriginOfLine() != null && includeSubstructures) {
                if (hasPreviousClause) {
                    sb2.append("AND ");
                } 
                if (!groupByClause) {
                    if (exprTableName != null) {
                        sb2.append(exprTableName).append(".");
                    }
                    sb2.append("originOfLine = ? ");
                } else {
                    sb2.append("anatOriginOfLine = ? ");
                }
                hasPreviousClause = true;
            }
            if (callTO.getStageOriginOfLine() != null && includeSubStages) {
                if (hasPreviousClause) {
                    sb2.append("AND ");
                } 
                sb2.append("stageOriginOfLine = ? ");
                hasPreviousClause = true;
            }
            if (callTO.isObservedData() != null && (includeSubStages || includeSubstructures)) {
                if (hasPreviousClause) {
                    sb2.append("AND ");
                } 
                if (!includeSubStages && includeSubstructures && !groupByClause) {
                    //if we propagate only from substructures, we check the column 
                    //originOfLine in the globalExpression table: if it equals to 'descent', 
                    //then there are no observed data.
                    //But if we made a GROUP BY, then we have already generated the obervedData 
                    //field in the SELECT clause. 
                    sb2.append("IF(originOfLine = 'descent', 0, 1) ");
                } else if (includeSubStages || groupByClause) {
                    //if includeSubStages is true, then it means that this parameter 
                    //can only be filtered from the HAVING clause, so we directly have access 
                    //to the parameter name
                    sb2.append("observedData ");
                }
                sb2.append("= ? ");
                hasPreviousClause = true;
            }
                
            if (sb2.length() != 0) {
                sb.append("(").append(sb2.toString()).append(") ");
            }
        }
        if (sb.length() != 0) {
            sb.insert(0, "(");
            sb.append(") ");
        }
        
        return log.exit(sb.toString());
    }
    
    private int parameterizeCallDAOFilterClause(BgeePreparedStatement stmt, int startIndex, 
            LinkedHashSet<CallDAOFilter> callFilters, boolean useGeneIds, boolean useSpeciesIds) 
                    throws SQLException {
        log.entry(callFilters, useGeneIds, useSpeciesIds);
        
        int index = startIndex;
        for (CallDAOFilter callFilter: callFilters) {
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
        }
        
        return log.exit(index);
    }
    
    private int parameterizeCallTOFilterClause(BgeePreparedStatement stmt, int startIndex, 
            LinkedHashSet<ExpressionCallTO> callTOFilters, 
            boolean includeSubstructures, boolean includeSubStages) throws SQLException {
        log.entry(callTOFilters, includeSubstructures, includeSubStages);
        
        int index = startIndex;
        for (ExpressionCallTO callTO: callTOFilters) {
            
            //data filtering (affymetrixData, rnaSeqData, ...)
            for (DataState state: callTO.extractFilteringDataTypes().values()) {
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
                "(expressionId, bgeeGeneId, conditionId, "+
                "estData, affymetrixData, inSituData, rnaSeqData) " +
                "values (?, ?, ?, ?, ?, ?, ?)";
        
        // To not overload MySQL with an error com.mysql.jdbc.PacketTooBigException, 
        // and because of laziness, we insert expression calls one at a time
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sqlExpression)) {
            for (ExpressionCallTO call: toInsertInExpression) {
                stmt.setInt(1, call.getId());
                stmt.setInt(2, call.getBgeeGeneId());
                stmt.setInt(3, call.getConditionId());
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
                "(globalExpressionId, bgeeGeneId, conditionId, "+
                "estData, affymetrixData, inSituData, rnaSeqData, originOfLine) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?)";
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sqlGlobalExpression)) {
            for (ExpressionCallTO call: toInsertInGlobalExpression) {
                stmt.setInt(1, call.getId());
                stmt.setInt(2, call.getBgeeGeneId());
                stmt.setInt(3, call.getConditionId());
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
                stmt.setInt(1, call.getGlobalExpressionId());
                stmt.setInt(2, call.getExpressionId());
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

            Integer id = null, geneId = null, conditionId = null;
            DataState affymetrixData = null, estData = null, inSituData = null, rnaSeqData = null;
            BigDecimal globalMeanRank = null, affymetrixMeanRank = null, estMeanRank = null, 
                    inSituMeanRank = null, rnaSeqMeanRank = null;
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
                        id = this.getCurrentResultSet().getInt(column.getKey());
                        break;
                    case GENE_ID:
                        geneId = this.getCurrentResultSet().getInt(column.getKey());
                        break;
                    case CONDITION_ID:
                        conditionId = this.getCurrentResultSet().getInt(column.getKey());
                        break;
                    case GLOBAL_MEAN_RANK:
                        globalMeanRank = this.getCurrentResultSet().getBigDecimal(column.getKey());
                        break;
                    case AFFYMETRIX_DATA: 
                        //index of the enum in the mysql database corresponds to the ordinal 
                        //of DataState + 1
                        affymetrixData = 
                            dataStates[this.getCurrentResultSet().getInt(column.getKey()) - 1];
                        break;
                    case AFFYMETRIX_MEAN_RANK:
                        affymetrixMeanRank = this.getCurrentResultSet().getBigDecimal(column.getKey());
                        break;
                    case EST_DATA:
                       //index of the enum in the mysql database corresponds to the ordinal 
                        //of DataState + 1
                        estData = 
                            dataStates[this.getCurrentResultSet().getInt(column.getKey()) - 1];
                        break;
                    case EST_MEAN_RANK:
                        estMeanRank = this.getCurrentResultSet().getBigDecimal(column.getKey());
                        break;
                    case IN_SITU_DATA: 
                        //index of the enum in the mysql database corresponds to the ordinal 
                        //of DataState + 1
                        inSituData = 
                            dataStates[this.getCurrentResultSet().getInt(column.getKey()) - 1];
                        break;
                    case IN_SITU_MEAN_RANK:
                        inSituMeanRank = this.getCurrentResultSet().getBigDecimal(column.getKey());
                        break;
                    case RNA_SEQ_DATA:
                        //index of the enum in the mysql database corresponds to the ordinal 
                        //of DataState + 1
                        rnaSeqData = 
                            dataStates[this.getCurrentResultSet().getInt(column.getKey()) - 1];
                        break;
                    case RNA_SEQ_MEAN_RANK:
                        rnaSeqMeanRank = this.getCurrentResultSet().getBigDecimal(column.getKey());
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
            return log.exit(new ExpressionCallTO(id, geneId, conditionId, globalMeanRank, 
                    affymetrixData, affymetrixMeanRank, estData, estMeanRank, 
                    inSituData, inSituMeanRank, rnaSeqData, rnaSeqMeanRank, 
                    includeSubstructures, includeSubStages, 
                    anatOriginOfLine, stageOriginOfLine, observedData));
        }
        
        private ExpressionCallDAO.Attribute getAttributeFromColName(String colName) 
                throws UnrecognizedColumnException{
            log.entry(colName);
            
            if (colName.equals("exprId")) {
                return log.exit(ExpressionCallDAO.Attribute.ID);
            } 
            if (colName.equals("bgeeGeneId")) {
                return log.exit(ExpressionCallDAO.Attribute.GENE_ID);
            } 
            if (colName.equals("conditionId")) {
                return log.exit(ExpressionCallDAO.Attribute.CONDITION_ID);
            }
            if (colName.equals("globalMeanRank")) {
                return log.exit(ExpressionCallDAO.Attribute.GLOBAL_MEAN_RANK);
            } 
            if (colName.equals("affymetrixData")) {
                return log.exit(ExpressionCallDAO.Attribute.AFFYMETRIX_DATA);
            } 
            if (colName.equals("affymetrixRank")) {
                return log.exit(ExpressionCallDAO.Attribute.AFFYMETRIX_MEAN_RANK);
            } 
            if (colName.equals("estData")) {
                return log.exit(ExpressionCallDAO.Attribute.EST_DATA);
            } 
            if (colName.equals("estRank")) {
                return log.exit(ExpressionCallDAO.Attribute.EST_MEAN_RANK);
            } 
            if (colName.equals("inSituData")) {
                return log.exit(ExpressionCallDAO.Attribute.IN_SITU_DATA);
            } 
            if (colName.equals("inSituRank")) {
                return log.exit(ExpressionCallDAO.Attribute.IN_SITU_MEAN_RANK);
            } 
            if (colName.equals("rnaSeqData")) {
                return log.exit(ExpressionCallDAO.Attribute.RNA_SEQ_DATA);
            } 
            if (colName.equals("rnaSeqRank")) {
                return log.exit(ExpressionCallDAO.Attribute.RNA_SEQ_MEAN_RANK);
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
            Integer globalExpressionId = null, expressionId = null;

            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("globalExpressionId")) {
                        globalExpressionId = this.getCurrentResultSet().getInt(column.getKey());

                    } else if (column.getValue().equals("expressionId")) {
                        expressionId = this.getCurrentResultSet().getInt(column.getKey());

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
