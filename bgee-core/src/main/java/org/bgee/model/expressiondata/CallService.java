package org.bgee.model.expressiondata;

import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;
import org.bgee.model.dao.api.expressiondata.CallDAOFilter;
import org.bgee.model.dao.api.expressiondata.DAOConditionFilter;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallParams;
import org.bgee.model.expressiondata.Call.DiffExpressionCall;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallFilter.DiffExpressionCallFilter;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.baseelements.CallType.DiffExpression;
import org.bgee.model.expressiondata.baseelements.CallType.Expression;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.DataPropagation.PropagationState;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.species.TaxonomyFilter;

/**
 * A {@link Service} to obtain {@link Call} objects. 
 * Users should use the {@link org.bgee.model.ServiceFactory} to obtain {@code CallService}s.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Aug. 2016
 * @since   Bgee 13, Oct. 2015
 */
/// XXX: Check in bgee14 if speciesId is retrieved in CallTO
public class CallService extends Service {
    private final static Logger log = LogManager.getLogger(CallService.class.getName());
    
    //XXX: Enum class for fields of Call to populate? 
    //(e.g., GENE, ANAT_ENTITY, STAGE, DATA). But this means that we once again 
    //"duplicate" the concepts in the Condition class. 
    
//******************
// Notes from previous CallFilter version
//******************
  //XXX: 
 // - if both IDs and multiple species requested AND forceHomology is true 
 //   => find missing orthologous genes/homologous organs/comparable stages
    
    //XXX: impact both the gene filtering and the anat.entity and stage filtering.
    //Species should be always explicitly targeted.
//    private final TaxonomyFilter taxonFilter; 
    //XXX: with this boolean set to true, any multi-species query will search explicitly 
    //for homology/orthology relations, and will complete ID list provided to potentially 
    //add homolog/orthologs (i.e., impacting both ConditionFilters and GeneFilters).
    //if false, then any query is possible, without caring about homology/orthology.
    //XXX: If true, retrieve results only in homologous structure/comparable stages, always.
//    private final boolean forceHomology;
//******************
    
    public static enum Attribute implements Service.Attribute {
        GENE_ID, ANAT_ENTITY_ID, DEV_STAGE_ID, GLOBAL_DATA_QUALITY, GLOBAL_RANK, CALL_DATA, 
        GLOBAL_ANAT_PROPAGATION, GLOBAL_STAGE_PROPAGATION, GLOBAL_OBSERVED_DATA, 
        CALL_DATA_OBSERVED_DATA;
    }
    public static enum OrderingAttribute implements Service.OrderingAttribute {
        GENE_ID, ANAT_ENTITY_ID, DEV_STAGE_ID, GLOBAL_RANK;
    }

    /**
     * @param serviceFactory            The {@code ServiceFactory} to be used to obtain {@code Service}s 
     *                                  and {@code DAOManager}.
     * @throws IllegalArgumentException If {@code serviceFactory} is {@code null}.
     */
    public CallService(ServiceFactory serviceFactory) {
        super(serviceFactory);
    }
    
    //XXX: example multi-species query, signature/returned value to be better defined. 
    //We could then have the calls ordered by OMA HOG IDs (as in the current pipeline), 
    //to be able to group calls of orthologous genes in homologous organs. I believe 
    //this CallService should manage the ordering if requested, but not the grouping. 
    //XXX: or should we have Calls with MultiSpeciesConditions? Then the grouping 
    //of homologous organs could be performed by the CallService, if provided with the mapping 
    //of homologous organs as method argument. 
    public Stream<Call<? extends SummaryCallType, ? extends CallData<?>>> loadCallsInMultiSpecies(
            TaxonomyFilter taxonFilter, Set<CallFilter> callFilters) {
        //TODO
        // For multi-species calls, we need to reconcile calls, gene by gene
//      Map<String, Set<ExpressionCall>> propagatedCallsByGeneId = propagatedCalls.stream()
//          .collect(Collectors.toMap(c -> c.getGeneId(), 
//              c -> new HashSet<ExpressionCall>(Arrays.asList(c)),
//              (v1, v2) -> {
//                  Set<ExpressionCall> newSet = new HashSet<>(v1);
//                  newSet.addAll(v2);
//                  return newSet;
//              }));

        return null;
    }
    
    /** 
     * Load propagated and reconciled expression calls with parameter provided 
     * through an {@code ExpressionCallFilter}.
     * 
     * @param speciesId             A {@code String} that is the ID of the species 
     *                              for which to return the {@code ExpressionCall}s.
     * @param callFilter            An {@code ExpressionCallFilter} allowing 
     *                              to filter retrieving of data.
     * @param attributes            A {@code Collection} of {@code Attribute}s defining the
     *                              attributes to populate in the returned {@code ExpressionCall}s.
     *                              If {@code null} or empty, all attributes are populated. 
     * @param orderingAttributes    A {@code LinkedHashMap} where keys are 
     *                              {@code CallService.OrderingAttribute}s defining the attributes
     *                              used to order the returned {@code ExpressionCall}s, 
     *                              the associated value being a {@code Service.Direction} defining 
     *                              whether the ordering should be ascendant or descendant.
     * @return                      A {@code Stream} of {@code ExpressionCall}s that are propagated
     *                              and reconciled expression calls.
     * @throws IllegalArgumentException If {@code callFilter} or {@code speciesID} are null or empty.
     */
    public Stream<ExpressionCall> loadExpressionCalls(String speciesId, 
            ExpressionCallFilter callFilter, Collection<Attribute> attributes, 
            LinkedHashMap<OrderingAttribute, Service.Direction> orderingAttributes) throws IllegalArgumentException {
        log.entry(speciesId, callFilter, attributes, orderingAttributes);
        
        //sanity checks
        if (callFilter == null) {
            throw log.throwing(new IllegalArgumentException("A CallFilter must be provided."));
        }
        if (StringUtils.isBlank(speciesId)) {
            throw log.throwing(new IllegalArgumentException("A speciesID must be provided"));
        }
        
        final Set<Attribute> clonedAttrs = Collections.unmodifiableSet(
                attributes == null? EnumSet.noneOf(Attribute.class): EnumSet.copyOf(attributes));
        LinkedHashMap<OrderingAttribute, Service.Direction> clonedOrderingAttrs = 
                orderingAttributes == null? new LinkedHashMap<>(): new LinkedHashMap<>(orderingAttributes);

        // We need to retrieved all attributes to be able to build the ConditionUtils and
        // propagate, reconcile and filter calls.
        Set<ExpressionCall> calls = this.performsExpressionQueries(speciesId, callFilter, 
                EnumSet.complementOf(EnumSet.of(Attribute.CALL_DATA_OBSERVED_DATA)), clonedOrderingAttrs)
                .collect(Collectors.toSet());
        
        ConditionUtils conditionUtils = new ConditionUtils(
                calls.stream().map(c -> c.getCondition()).collect(Collectors.toSet()),
                // We should infer ancestral condition to be able to propagate to condition without call.
                true,
                this.getServiceFactory());

        Set<ExpressionCall> propagatedCalls = this.propagateExpressionCalls(calls, 
                callFilter.getConditionFilters(), conditionUtils, speciesId);

        // For single species, we need to reconcile calls with the same gene/organ/stage
        // Note: we need to convert Set into List to have a stable sort
        // (see Javadoc of java.util.stream.Stream.sorted)
        Map<ExpressionCall, List<ExpressionCall>> groupedCalls = propagatedCalls.stream().collect(Collectors.toMap(
                c -> new ExpressionCall(c.getGeneId(), c.getCondition(), null, null, null, null, null), 
                c -> new ArrayList<ExpressionCall>(Arrays.asList(c)), 
                (v1, v2) -> {
                    List<ExpressionCall> newList = new ArrayList<>(v1);
                    newList.addAll(v2);
                    return newList;
                }));

        Stream<ExpressionCall> reconciledCalls = groupedCalls.entrySet().stream()
                // Reconcile calls
                .map(e -> {
                    ExpressionCall reconciledCall = CallService.reconcileSingleGeneCalls(e.getValue());
                    return new ExpressionCall(reconciledCall.getGeneId(), e.getKey().getCondition(),
                            reconciledCall.getDataPropagation(), reconciledCall.getSummaryCallType(),
                            reconciledCall.getSummaryQuality(), reconciledCall.getCallData(),
                            reconciledCall.getGlobalMeanRank());
                })
                // Filter calls according CallFilter
                .filter(c -> CallService.testCallFilter(c, callFilter))
                // Order according to provided orderingAttributes with convertServiceOrdering
                // We order before removing attribute to be able to order on all orderingAttributes
                .sorted(CallService.convertServiceOrdering(clonedOrderingAttrs))
                // Keep provided attributes
                .map(c -> CallService.getClonedExpressionCall(c, clonedAttrs))
                // After removing of some attributes, some calls can be identical
                .distinct();

        return log.exit(reconciledCalls);
    }

    /** 
     * Evaluates this {@code ExpressionCallFilter} on the given {@code ExpressionCall}.
     * 
     * @param call          An {@code ExpressionCall} that is the expression call to be evaluated.
     * @param callFilter    An {@code ExpressionCallFilter} that is the filter 
     *                      to be used to evaluate {@code call}.
     * @return              {@code true} if the {@code call} matches {@code callFilter}.
     * @throws IllegalArgumentException If {@code call}, call data of {@code call}
     *                                  or {@code callFilter} are null.
     */
    // TODO to be added to ExpressionCallUtils see TODOs into ExpressionCall
    public static boolean testCallFilter(ExpressionCall call, ExpressionCallFilter callFilter) 
            throws IllegalArgumentException {
        log.entry(call, callFilter);

        if (call == null) {
            throw log.throwing(new IllegalArgumentException("ExpressionCall could not be null"));
        }
        if (call.getCallData() == null || call.getCallData().isEmpty()) {
            throw log.throwing(new IllegalArgumentException("ExpressionCallData could not be null or empty"));
        }
        if (callFilter == null) {
            throw log.throwing(new IllegalArgumentException("ExpressionCallFilter could not be null"));
        }

        // Filter according GeneFilter
        if (callFilter.getGeneFilter() != null
                && !callFilter.getGeneFilter().getGeneIds().contains(call.getGeneId())) {
            return log.exit(false);
        }

        // Filter according ConditionFilters
        if (callFilter.getConditionFilters() != null
                && !callFilter.getConditionFilters().isEmpty()
                && !callFilter.getConditionFilters().stream().anyMatch(f -> f.test(call.getCondition()))) {
            return log.exit(false);
        }

        // Filter according CallDataFilters, if several filters are provided, they are seen as "OR" conditions        
        for (ExpressionCallData callDataFilter: callFilter.getCallDataFilters()) {
            for (ExpressionCallData callData: call.getCallData()) {
                boolean isDataTypeValid = false;
                boolean isCallTypeValid = false;
                boolean isDataQualityValid = false;

                // Filter on DataType (AFFYMETRIX, EST, IN_SITU, RNA_SEQ)
                if (callDataFilter.getDataType() == null 
                        || !Collections.disjoint(
                                callDataFilter.getDataType() == null?
                                        EnumSet.allOf(DataType.class): EnumSet.of(callDataFilter.getDataType()),
                                callData.getDataType() == null?
                                        EnumSet.allOf(DataType.class): EnumSet.of(callData.getDataType()))) {
                    isDataTypeValid = true;
                } else {
                    continue;
                }
                
                // Filter on CallType (EXPRESSED, NOT_EXPRESSED)
                if (callDataFilter.getCallType() == null
                        || callData.getCallType().equals(callDataFilter.getCallType())) {
                    isCallTypeValid = true;
                } else {
                    continue;
                }
                
                // Filter on DataQuality (NODATA, LOW, HIGH)
                if (callDataFilter.getDataQuality() == null
                        || callDataFilter.getDataQuality().equals(DataQuality.LOW) 
                        || !Collections.disjoint(
                                callDataFilter.getDataQuality() == null?
                                        EnumSet.allOf(DataQuality.class): EnumSet.of(callDataFilter.getDataQuality()),
                                callData.getDataQuality() == null?
                                        EnumSet.allOf(DataQuality.class): EnumSet.of(callData.getDataQuality()))) {
                    isDataQualityValid = true;
                } else {
                    continue;
                }
                if (isDataTypeValid && isCallTypeValid && isDataQualityValid) {
                    return log.exit(true);
                }
            }
        }
        return log.exit(false);
    }

    /**
     * Return an {@code ExpressionCall} populated according to {@code attributes}.
     * 
     * @param call          An {@code ExpressionCall} to be cloned only, with provided {@code attributes}.
     * @param attributes    A {@code Set} of {@code Attribute}s defining the attributes
     *                      to populate in the returned {@code ExpressionCall}s.
     *                      If {@code null} or empty, all attributes are populated. 
     * @return              The clones {@code ExpressionCall} populated according to {@code attributes}.
     */
    private static ExpressionCall getClonedExpressionCall(ExpressionCall call, Set<Attribute> attributes) {
        log.entry(call, attributes);
        
        assert attributes != null;
        
        Set<Attribute> clonedAttrs = attributes.isEmpty()? EnumSet.allOf(Attribute.class): attributes;

        String geneId = null;
        if (clonedAttrs.contains(Attribute.GENE_ID)) {
            geneId = call.getGeneId();
        }
        Condition condition = null;
        if (call.getCondition() != null && (clonedAttrs.contains(Attribute.ANAT_ENTITY_ID) || 
                clonedAttrs.contains(Attribute.DEV_STAGE_ID))) {
            String anatEntityId = null;
            if (clonedAttrs.contains(Attribute.ANAT_ENTITY_ID) ) {
                anatEntityId = call.getCondition().getAnatEntityId();
            }
            String devStageId = null;
            if (clonedAttrs.contains(Attribute.DEV_STAGE_ID) ) {
                devStageId = call.getCondition().getDevStageId();
            }
            condition = new Condition(anatEntityId, devStageId, call.getCondition().getSpeciesId());
        }
        DataQuality summaryQual = null; 
        if (clonedAttrs.contains(Attribute.GLOBAL_DATA_QUALITY)) {
            summaryQual = call.getSummaryQuality();
        }
        BigDecimal globalMeanRank = null;
        if (clonedAttrs.contains(Attribute.GLOBAL_RANK)) {
            globalMeanRank = call.getGlobalMeanRank();
        }
        DataPropagation dataPropagation = null; 
        if (call.getDataPropagation() != null && (
                clonedAttrs.contains(Attribute.GLOBAL_ANAT_PROPAGATION) || 
                clonedAttrs.contains(Attribute.GLOBAL_STAGE_PROPAGATION) || 
                clonedAttrs.contains(Attribute.GLOBAL_OBSERVED_DATA))) {
            PropagationState anatPropa = null;
            if (clonedAttrs.contains(Attribute.GLOBAL_ANAT_PROPAGATION) ) {
                anatPropa = call.getDataPropagation().getAnatEntityPropagationState();
            }
            PropagationState stagePropa = null;
            if (clonedAttrs.contains(Attribute.GLOBAL_STAGE_PROPAGATION) ) {
                stagePropa = call.getDataPropagation().getDevStagePropagationState();
            }
            Boolean includingObservedData = null;
            if (clonedAttrs.contains(Attribute.GLOBAL_OBSERVED_DATA) ) {
                includingObservedData = call.getDataPropagation().getIncludingObservedData();
            }
            dataPropagation = new DataPropagation(anatPropa, stagePropa, includingObservedData);
        }
        Collection<ExpressionCallData> callData = null;
        if (clonedAttrs.contains(Attribute.CALL_DATA)) {
            callData = call.getCallData();
        }
        // FIXME: Take into account Attribute.CALL_DATA_OBSERVED_DATA ??

        // FIXME: Create Attribute.GLOBAL_SUMMARY or always fill the summary ??
        ExpressionSummary summaryCallType = call.getSummaryCallType();
        
        return log.exit(new ExpressionCall(geneId, condition, dataPropagation, summaryCallType,
                summaryQual, callData, globalMeanRank));
    }
    
    /** 
     * Return the {@code Comparator} of {@code ExpressionCall}s, performing the comparisons
     * in order provided by {@code orderingAttributes}.
     * 
     * @param orderingAttributes    A {@code LinkedHashMap} where keys are 
     *                              {@code CallService.OrderingAttribute}s defining the attributes
     *                              used to order the returned {@code ExpressionCall}s, 
     *                              the associated value being a {@code Service.Direction} defining 
     *                              whether the ordering should be ascendant or descendant.
     * @return                      The {@code Comparator} of {@code ExpressionCall}s.
     */
    // TODO to be added to ExpressionCallUtils see TODOs into ExpressionCall
    public static Comparator<ExpressionCall> convertServiceOrdering(
            LinkedHashMap<CallService.OrderingAttribute, Service.Direction> orderingAttributes) {
        log.entry(orderingAttributes);
        
        LinkedHashMap<OrderingAttribute, Service.Direction> clonedOrderingAttrs = 
                orderingAttributes == null || orderingAttributes.isEmpty()?
                        getDefaultOrdering() : new LinkedHashMap<>(orderingAttributes);
        
        Comparator<ExpressionCall> comparator = null;
        for (Entry<CallService.OrderingAttribute, Service.Direction> entry: clonedOrderingAttrs.entrySet()) {
            Comparator<String> compStr = null;
            Comparator<BigDecimal> compBigD = null;
            switch (entry.getValue()) {
            case ASC:
                compStr = Comparator.nullsLast(Comparator.naturalOrder());
                compBigD = Comparator.nullsLast(Comparator.naturalOrder());
                break;
            case DESC:
                compStr = Comparator.nullsLast(Comparator.reverseOrder());
                compBigD = Comparator.nullsLast(Comparator.reverseOrder());
                break;
            default: 
                throw log.throwing(new IllegalStateException("Unsupported Service.Direction: " +
                        entry.getValue()));
            }

            Comparator<ExpressionCall> tmpComp = null;
            switch (entry.getKey()) {
            case GENE_ID:
                tmpComp = Comparator.comparing(ExpressionCall::getGeneId, compStr);
                break;
            case ANAT_ENTITY_ID:
                tmpComp = Comparator.comparing(c -> c.getCondition().getAnatEntityId(), compStr);
                break;
            case DEV_STAGE_ID:
                tmpComp = Comparator.comparing(c -> c.getCondition().getDevStageId(), compStr);
                break;
            case GLOBAL_RANK:
                tmpComp = Comparator.comparing(c -> c.getGlobalMeanRank(), compBigD);
                break;
            default: 
                throw log.throwing(new IllegalStateException("Unsupported OrderingAttribute: " + 
                        entry.getKey()));
            }
            
            if (comparator == null) {
                comparator = tmpComp;
            } else {
                comparator = comparator.thenComparing(tmpComp);
            }
        }
        return log.exit(comparator);
    }

    private static LinkedHashMap<OrderingAttribute, Direction> getDefaultOrdering() {
        log.entry();
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = 
                new LinkedHashMap<>();
        for (CallService.OrderingAttribute attr: EnumSet.allOf(CallService.OrderingAttribute.class)) {
            serviceOrdering.put(attr, Service.Direction.ASC);
        }
        return log.exit(serviceOrdering);
    }

    public Stream<DiffExpressionCall> loadDiffExpressionCalls(String speciesId, 
            DiffExpressionCallFilter callFilter) {
        //TODO
        return null;
    }
    
    //*************************************************************************
    // METHODS PERFORMING THE QUERIES TO THE DAOs
    //*************************************************************************
    /**
     * Perform expression queries without the post-processing of the results returned by {@code DAO}s.
     * 
     * @param speciesId             A {@code String} that is the ID of the species 
     *                              for which to return the {@code ExpressionCall}s.
     * @param callFilter            An {@code ExpressionCallFilter} allowing 
     *                              to configure retrieving of data throw {@code DAO}s.
     *                              Cannot be {@code null} or empty (check by calling methods).
     * @param attributes            A {@code Set} of {@code Attribute}s defining the attributes
     *                              to populate in the returned {@code ExpressionCall}s.
     *                              If {@code null} or empty, all attributes are populated. 
     * @param orderingAttributes    A {@code LinkedHashMap} where keys are 
     *                              {@code CallService.OrderingAttribute}s defining the attributes
     *                              used to order the returned {@code ExpressionCall}s, 
     *                              the associated value being a {@code Service.Direction} defining 
     *                              whether the ordering should be ascendant or descendant.
     * @return                      A {@code Stream} of {@code ExpressionCall}s.
     * @throws IllegalArgumentException If the {@code callFilter} provided define multiple 
     *                                  expression propagation states requested.
     */
    private Stream<ExpressionCall> performsExpressionQueries(String speciesId, 
            CallFilter<?> callFilter, Set<Attribute> attributes, 
            LinkedHashMap<OrderingAttribute, Service.Direction> orderingAttributes)
                    throws IllegalArgumentException {
        log.entry(speciesId, callFilter, attributes, orderingAttributes);
        
        //UnsupportedOperationExceptions (for now)
        if (attributes.contains(Attribute.CALL_DATA_OBSERVED_DATA)) {
            throw log.throwing(new UnsupportedOperationException(
                    "Retrieval of observed data state per data type not yet implemented."));
        }
        if (callFilter.getCallDataFilters().stream()
                .anyMatch(callData -> EnumSet.allOf(DiffExpression.class).contains(callData.getCallType()))) {
            throw log.throwing(new UnsupportedOperationException(
                    "Management of diff. expression queries not yet implemented."));
        }
                
        if (callFilter.getConditionFilters() != null || !callFilter.getConditionFilters().isEmpty()) {
            log.warn("ExpressionDAO may not return what you expect");
        }

        //Extract only the CallData related to expression queries
        Set<ExpressionCallData> exprCallData = 
                callFilter.getCallDataFilters().stream()
                //consider only callData for the ExpressionCallDAO
                .filter(callData -> Expression.EXPRESSED.equals(callData.getCallType()))
                .map(callData -> (ExpressionCallData) callData)
                .collect(Collectors.toSet());
        
        //now, do one query for each combination of propagation states
        final ExpressionCallDAO exprDao = this.getDaoManager().getExpressionCallDAO();
        Stream<ExpressionCall> expr = exprDao.getExpressionCalls(Arrays.asList(
                //generate an ExpressionCallDAOFilter from callFilter 
                new CallDAOFilter(
                    //we will provide the gene IDs to the getExpressionCalls method 
                    //as a global gene filter, not through the CallDAOFilter. 
                    null, 
                    //species
                    Arrays.asList(speciesId), 
                    //ConditionFilters
                    callFilter.getConditionFilters().stream()
                        .map(condFilter -> new DAOConditionFilter(
                                condFilter.getAnatEntitieIds(), 
                                condFilter.getDevStageIds()))
                        .collect(Collectors.toSet())
                )),  
                //CallTOFilters
                exprCallData.stream()
                    .flatMap(callData -> mapCallDataToExprCallTOFilters(callData, 
                            new DataPropagation(PropagationState.SELF, 
                                    PropagationState.SELF, true)).stream())
                    .collect(Collectors.toSet()), 
                //includeSubstructures
                //FIXME for the moment, only not propagated calls
                false,
                //includeSubStages
                //FIXME for the moment, only not propagated calls
                false,
                //global gene filter
                Optional.ofNullable(callFilter.getGeneFilter())
                    .map(geneFilter -> geneFilter.getGeneIds()).orElse(new HashSet<>()), 
                //no gene orthology requested
                null, 
                //Attributes
                convertServiceAttrsToExprDAOAttrs(attributes, exprCallData.stream()
                            .flatMap(callData -> callData.getDataType() != null? 
                                EnumSet.of(callData.getDataType()).stream(): 
                                EnumSet.allOf(DataType.class).stream())
                            .collect(Collectors.toCollection(() -> EnumSet.noneOf(DataType.class)))), 
                //OrderingAttributes
                convertServiceOrderingAttrsToExprDAOOrderingAttrs(orderingAttributes)
            )
                //retrieve the Stream resulting from the query. Note that the query is not executed 
                //as long as the Stream is not consumed (lazy-loading).
                .stream()
                //allow mapping of the ExpressionCallTOs to ExpressionCalls. The Stream is still 
                //not consumed at this point (map is a lazy operation as well). 
                .map(callTO -> mapCallTOToExpressionCall(callTO, new DataPropagation(), speciesId));

        final NoExpressionCallDAO noExprDao = this.getDaoManager().getNoExpressionCallDAO();
        NoExpressionCallParams params = new NoExpressionCallParams();
        params.addSpeciesId(speciesId);
        Stream<ExpressionCall> noExpr = noExprDao.getNoExpressionCalls(params)
                //retrieve the Stream resulting from the query. Note that the query is not executed 
                //as long as the Stream is not consumed (lazy-loading).
                .stream()
                //allow mapping of the ExpressionCallTOs to ExpressionCalls. The Stream is still 
                //not consumed at this point (map is a lazy operation as well). 
                .map(callTO -> mapCallTOToExpressionCall(callTO, new DataPropagation(), speciesId));

        return log.exit(Stream.concat(expr, noExpr));
    }

    //*************************************************************************
    // METHODS MAPPING CallTOs TO Calls
    //*************************************************************************
    private static ExpressionCall mapCallTOToExpressionCall(ExpressionCallTO callTO, 
            DataPropagation callFilterPropag, String speciesId) {
        log.entry(callTO, callFilterPropag, speciesId);

        //at this point, we cannot know the propagation status per data type, 
        //the expression tables only store a global propagation status 
        //over all data types. To infer the status per data type, 
        //we would need two queries, one including sub-stages/substructures, 
        //and another one not including them. 
        //so here, we provide the only thing we know: the propagation status 
        //requested to the DAO.
        //Infer observation state first. No way to get any information about "observed data" 
        //per data type at this point, unless the results have some specific propagation states.
        Set<PropagationState> allPropagStates = callFilterPropag.getAllPropagationStates();
        Boolean observedData = null;
        if (allPropagStates.size() == 1 && allPropagStates.contains(PropagationState.SELF)) {
            observedData = true;
        } else if (allPropagStates.contains(PropagationState.DESCENDANT)) {
            observedData = false;
        }
        DataPropagation callDataPropagation = new DataPropagation(
                !PropagationState.SELF.equals(callFilterPropag.getAnatEntityPropagationState())? 
                        PropagationState.SELF_OR_DESCENDANT: PropagationState.SELF, 
                !PropagationState.SELF.equals(callFilterPropag.getDevStagePropagationState())? 
                        PropagationState.SELF_OR_DESCENDANT: PropagationState.SELF, 
                observedData);
        
        //infer the global Propagation status of the call, either from the CallTO 
        //if it contains this information, or from the PropagationState defined from the CallFilter.
        DataPropagation globalPropagation = new DataPropagation(
                Optional.ofNullable(convertExprOriginToPropagationState(callTO.getAnatOriginOfLine()))
                .orElse(callDataPropagation.getAnatEntityPropagationState()), 
                Optional.ofNullable(convertExprOriginToPropagationState(callTO.getStageOriginOfLine()))
                .orElse(callDataPropagation.getDevStagePropagationState()), 
                callTO.isObservedData() == null? observedData: callTO.isObservedData());
        
        return log.exit(new ExpressionCall(callTO.getGeneId(), 
                callTO.getAnatEntityId() != null || callTO.getStageId() != null? 
                        new Condition(callTO.getAnatEntityId(), callTO.getStageId(), speciesId): null, 
                globalPropagation, 
                //At this point, there can't be any ambiguity state, as we haven't compare 
                //the expression calls to no-expression calls yet.
                ExpressionSummary.EXPRESSED, 
                //get the best quality among all data types
                extractBestQual(callTO),
                //map to CallDatas
                callTO.extractDataTypesToDataStates().entrySet().stream()
                    .filter(entry -> entry.getValue() != null && 
                                     !entry.getValue().equals(CallTO.DataState.NODATA))
                    .map(entry -> new ExpressionCallData(Expression.EXPRESSED, 
                            convertDataStateToDataQuality(entry.getValue()), 
                            convertExprAttributeToDataType(entry.getKey()), 
                            callDataPropagation))
                    
                    .collect(Collectors.toSet()), 
                callTO.getGlobalMeanRank()
                ));
    }
    
    // TODO refactoring with previous method?
    private static ExpressionCall mapCallTOToExpressionCall(NoExpressionCallTO callTO, 
            DataPropagation callFilterPropag, String speciesId) {
        log.entry(callTO, callFilterPropag, speciesId);
        //at this point, we cannot know the propagation status per data type, 
        //the expression tables only store a global propagation status 
        //over all data types. To infer the status per data type, 
        //we would need two queries, one including parent structures, 
        //and another one not including them. 
        //so here, we provide the only thing we know: the propagation status 
        //requested to the DAO.
        //Infer observation state first. No way to get any information about "observed data" 
        //per data type at this point, unless the results have some specific propagation states.
        Set<PropagationState> allPropagStates = callFilterPropag.getAllPropagationStates();
        Boolean observedData = null;
        if (allPropagStates.size() == 1 && allPropagStates.contains(PropagationState.SELF)) {
            observedData = true;
        } else if (allPropagStates.contains(PropagationState.ANCESTOR)) {
            observedData = false;
        }
        DataPropagation callDataPropagation = new DataPropagation(
                !PropagationState.SELF.equals(callFilterPropag.getAnatEntityPropagationState())? 
                        PropagationState.SELF_OR_ANCESTOR: PropagationState.SELF, 
                PropagationState.SELF, 
                observedData);
        
        //infer the global Propagation status of the call, either from the CallTO 
        //if it contains this information, or from the PropagationState defined from the CallFilter.
        DataPropagation globalPropagation = new DataPropagation(
                Optional.ofNullable(convertNoExprOriginToPropagationState(callTO.getOriginOfLine()))
                .orElse(callDataPropagation.getAnatEntityPropagationState()), 
                PropagationState.SELF, 
                observedData);
        
        return log.exit(new ExpressionCall(callTO.getGeneId(), 
                callTO.getAnatEntityId() != null || callTO.getStageId() != null? 
                        new Condition(callTO.getAnatEntityId(), callTO.getStageId(), speciesId): null, 
                globalPropagation, 
                //At this point, there can't be any ambiguity state, as we haven't compare 
                //the expression calls to no-expression calls yet.
                ExpressionSummary.NOT_EXPRESSED, 
                //get the best quality among all data types
                extractBestQual(callTO),
                //map to CallDatas
                callTO.extractDataTypesToDataStates().entrySet().stream()
                    .filter(entry -> entry.getValue() != null && 
                                     !entry.getValue().equals(CallTO.DataState.NODATA))
                    .map(entry -> new ExpressionCallData(Expression.NOT_EXPRESSED, 
                            convertDataStateToDataQuality(entry.getValue()), 
                            convertNoExprAttributeToDataType(entry.getKey()), 
                            callDataPropagation))
                    .collect(Collectors.toSet()), 
                null
                ));
    }

    //*************************************************************************
    // HELPER METHODS CONVERTING INFORMATION FROM CallDAO LAYER TO Call LAYER
    //*************************************************************************
    private static DataQuality extractBestQual(CallTO<?> callTO) {
        log.entry(callTO);
        
        return log.exit(callTO.extractDataTypesToDataStates().values().stream()
            .filter(e -> e != null && !e.equals(CallTO.DataState.NODATA))
            .max(Comparator.naturalOrder())
            .map(CallService::convertDataStateToDataQuality).orElse(null));
    }
    
    private static DataQuality convertDataStateToDataQuality(CallTO.DataState state) 
            throws IllegalStateException{
        log.entry(state);
        switch(state) {
        case LOWQUALITY: 
            return log.exit(DataQuality.LOW);
        case HIGHQUALITY:
            return log.exit(DataQuality.HIGH);
        case NODATA: 
            return log.exit(DataQuality.NODATA);
        default: 
            throw log.throwing(new IllegalStateException("Unsupported CallTO.DataState: " + state));
        }
    }
    
    //*************************************************************************
    // HELPER METHODS CONVERTING INFORMATION FROM ExpressionCallDAO LAYER TO Call LAYER
    //*************************************************************************
    private static final Map<ExpressionCallDAO.Attribute, DataType> EXPR_ATTR_TO_DATA_TYPE = Stream.of(
            new SimpleEntry<>(ExpressionCallDAO.Attribute.AFFYMETRIX_DATA, DataType.AFFYMETRIX), 
            new SimpleEntry<>(ExpressionCallDAO.Attribute.EST_DATA, DataType.EST), 
            new SimpleEntry<>(ExpressionCallDAO.Attribute.IN_SITU_DATA, DataType.IN_SITU), 
            new SimpleEntry<>(ExpressionCallDAO.Attribute.RNA_SEQ_DATA, DataType.RNA_SEQ))
            .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));
            
    private static DataType convertExprAttributeToDataType(
            ExpressionCallDAO.Attribute attr) throws IllegalStateException {
        log.entry(attr);
        
        return log.exit(Optional.ofNullable(EXPR_ATTR_TO_DATA_TYPE.get(attr))
                //bug of javac for type inference, we need to type the exception explicitly to RuntimeException,
                //see http://stackoverflow.com/questions/25523375/java8-lambdas-and-exceptions
                .<RuntimeException>orElseThrow(
                () -> log.throwing(new IllegalStateException(
                        "Unsupported ExpressionCallDAO.Attribute: " + attr))));
    }
    
    private static PropagationState convertExprOriginToPropagationState(
            ExpressionCallTO.OriginOfLine origin) throws IllegalStateException {
        log.entry(origin);
        
        if (origin == null) {
            return log.exit(null);
        }
        switch (origin) {
        case SELF: 
            return log.exit(PropagationState.SELF);
        case DESCENT: 
            return log.exit(PropagationState.DESCENDANT);
        case BOTH: 
            return log.exit(PropagationState.SELF_AND_DESCENDANT);
        default: 
            throw log.throwing(new IllegalStateException("Unsupported ExpressionCallTO.OriginOfLine: "
                     + origin));
        }
    }
    
    //*************************************************************************
    // HELPER METHODS CONVERTING INFORMATION FROM ExpressionCallDAO LAYER TO Call LAYER
    //*************************************************************************
    private static final Map<NoExpressionCallDAO.Attribute, DataType> NO_EXPR_ATTR_TO_DATA_TYPE = Stream.of(
            new SimpleEntry<>(NoExpressionCallDAO.Attribute.AFFYMETRIX_DATA, DataType.AFFYMETRIX), 
            new SimpleEntry<>(NoExpressionCallDAO.Attribute.IN_SITU_DATA, DataType.IN_SITU), 
            new SimpleEntry<>(NoExpressionCallDAO.Attribute.RNA_SEQ_DATA, DataType.RNA_SEQ))
            .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));
    
    private static DataType convertNoExprAttributeToDataType(
            NoExpressionCallDAO.Attribute attr) throws IllegalStateException {
        log.entry(attr);
        
        return log.exit(Optional.ofNullable(NO_EXPR_ATTR_TO_DATA_TYPE.get(attr))
                //bug of javac for type inference, we need to type the exception explicitly to RuntimeException,
                //see http://stackoverflow.com/questions/25523375/java8-lambdas-and-exceptions
                .<RuntimeException>orElseThrow(
                        () -> log.throwing(new IllegalStateException(
                                "Unsupported NoExpressionCallDAO.Attribute: " + attr))));
    }
    
    private static PropagationState convertNoExprOriginToPropagationState(
            NoExpressionCallTO.OriginOfLine origin) throws IllegalStateException {
        log.entry(origin);
        
        if (origin == null) {
            return log.exit(null);
        }
        switch (origin) {
        case SELF: 
            return log.exit(PropagationState.SELF);
        case PARENT: 
            return log.exit(PropagationState.ANCESTOR);
        case BOTH: 
            return log.exit(PropagationState.SELF_AND_ANCESTOR);
        default: 
            throw log.throwing(new IllegalStateException("Unsupported NoExpressionCallTO.OriginOfLine: "
                    + origin));
        }
    }

    //*************************************************************************
    // METHODS MAPPING CallDatas TO ExpressionCallTOs
    //*************************************************************************
    private static Set<ExpressionCallTO> mapCallDataToExprCallTOFilters(ExpressionCallData callData, 
            DataPropagation callFilterPropag) {
        log.entry(callData, callFilterPropag);
        
        //if the dataType of the callData is null, then it means that it targets all data types. 
        //In order to get OR conditions between data type parameters 
        //(e.g., affymetrixData >= HIGH OR rnaSeqData >= HIGH), we need to create one ExpressionCallTO 
        //per data type (because data type parameters inside a same ExpressionCallTO are considered 
        //as AND conditions). But this is needed only if there is a filtering requested 
        //on a minimum quality level, of course.
        //Here, don't use an EnumSet to be able to put 'null' in it (see below).
        Set<DataType> dataTypes = new HashSet<>();
        if (callData.getDataType() == null && callData.getDataQuality().equals(DataQuality.LOW)) {
            //no filtering on data quality for any data type
            dataTypes.add(null);
        } else {
            //filtering requested on data quality for any data type, 
            //or filtering requested on one specific data type for any quality
            dataTypes = callData.getDataType() != null? 
                EnumSet.of(callData.getDataType()): EnumSet.allOf(DataType.class);
        }
                
        return log.exit(dataTypes.stream().map(dataType -> {
            CallTO.DataState affyState = null;
            CallTO.DataState estState = null;
            CallTO.DataState inSituState = null;
            CallTO.DataState rnaSeqState = null;
            assert dataType != null || 
                    (dataType == null && callData.getDataQuality().equals(DataQuality.LOW));
            
            if (dataType != null) {
                CallTO.DataState state = convertDataQualityToDataState(callData.getDataQuality());
                switch (dataType) {
                case AFFYMETRIX: 
                    affyState = state;
                    break;
                case EST: 
                    estState = state;
                    break;
                case IN_SITU: 
                    inSituState = state;
                    break;
                case RNA_SEQ: 
                    rnaSeqState = state;
                    break;
                default: 
                    throw log.throwing(new IllegalStateException("Unsupported DataType: " + dataType));
                }
            }
            
            return new ExpressionCallTO(affyState, estState, inSituState, rnaSeqState, 
                convertPropagationStateToExprOrigin(callFilterPropag.getAnatEntityPropagationState()), 
                convertPropagationStateToExprOrigin(callFilterPropag.getDevStagePropagationState()), 
                callFilterPropag.getIncludingObservedData());
        })
        //filter CallTOs that provide no filtering at all
        .filter(callTO -> !callTO.equals(new ExpressionCallTO(null, null, null, null)))
        .collect(Collectors.toSet()));
    }

    //*************************************************************************
    // HELPER METHODS CONVERTING INFORMATION FROM Call LAYER TO CallDAO LAYER
    //*************************************************************************
    private static CallTO.DataState convertDataQualityToDataState(DataQuality qual) 
            throws IllegalStateException{
        log.entry(qual);
        switch(qual) {
        case LOW: 
            return log.exit(CallTO.DataState.LOWQUALITY);
        case HIGH:
            return log.exit(CallTO.DataState.HIGHQUALITY);
        default: 
            throw log.throwing(new IllegalStateException("Unsupported DataQuality: " + qual));
        }
    }
    
    private static LinkedHashMap<ExpressionCallDAO.OrderingAttribute, DAO.Direction> 
        convertServiceOrderingAttrsToExprDAOOrderingAttrs(
            LinkedHashMap<OrderingAttribute, Service.Direction> orderingAttrs) {
        log.entry(orderingAttrs);
        
        return log.exit(orderingAttrs.entrySet().stream().collect(Collectors.toMap(
            entry -> {
                switch (entry.getKey()) {
                case GENE_ID: 
                    return ExpressionCallDAO.OrderingAttribute.GENE_ID;
                case ANAT_ENTITY_ID: 
                    return ExpressionCallDAO.OrderingAttribute.ANAT_ENTITY_ID;
                case DEV_STAGE_ID: 
                    return ExpressionCallDAO.OrderingAttribute.STAGE_ID;
                case GLOBAL_RANK: 
                    return ExpressionCallDAO.OrderingAttribute.MEAN_RANK;
                default: 
                    throw log.throwing(new IllegalStateException("Unsupported OrderingAttributes from CallService: "
                            + entry.getKey()));
                }
            }, 
            entry -> {
                switch (entry.getValue()) {
                case ASC: 
                    return DAO.Direction.ASC;
                case DESC: 
                    return DAO.Direction.DESC;
                default: 
                    throw log.throwing(new IllegalStateException("Unsupported ordering Direction from CallService: "
                            + entry.getValue()));
                }
            }, 
            (v1, v2) -> {throw log.throwing(new IllegalStateException("No key collision possible"));}, 
            () -> new LinkedHashMap<ExpressionCallDAO.OrderingAttribute, DAO.Direction>())));
    }

    //*************************************************************************
    // HELPER METHODS CONVERTING INFORMATION FROM Call LAYER TO ExpressionCallDAO LAYER
    //*************************************************************************
    private static ExpressionCallTO.OriginOfLine convertPropagationStateToExprOrigin(
            PropagationState state) throws IllegalStateException {
        log.entry(state);
        switch (state) {
        case DESCENDANT: 
            return log.exit(ExpressionCallTO.OriginOfLine.DESCENT);
        case SELF_AND_DESCENDANT: 
            return log.exit(ExpressionCallTO.OriginOfLine.BOTH);
        case SELF: 
        case SELF_OR_DESCENDANT: 
            //SELF or SELF_OR_DESCENDANT simply means "include substructures/substages or not", 
            //so this is managed when calling the method of the DAO, there is no further 
            //filtering necessary here.
            return log.exit(null);
        default: 
            throw log.throwing(new IllegalStateException("Unsupported PropagationState "
                    + "for ExpressionCallTOs: " + state));
        }
    }
    
    private static Set<ExpressionCallDAO.Attribute> convertServiceAttrsToExprDAOAttrs(
            Set<Attribute> attributes, Set<DataType> dataTypesRequested) {
        log.entry(attributes, dataTypesRequested);
        
        //revert the existing map ExpressionCallDAO.Attribute -> DataType
        Map<DataType, ExpressionCallDAO.Attribute> typeToDAOAttr = EXPR_ATTR_TO_DATA_TYPE.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        
        return log.exit(attributes.stream().flatMap(attr -> {
            switch (attr) {
            case GENE_ID: 
                return Stream.of(ExpressionCallDAO.Attribute.GENE_ID);
            case ANAT_ENTITY_ID: 
                return Stream.of(ExpressionCallDAO.Attribute.ANAT_ENTITY_ID);
            case DEV_STAGE_ID: 
                return Stream.of(ExpressionCallDAO.Attribute.STAGE_ID);
            //Whether we need to get a global quality level over all requested data types, 
            //or the detailed quality level per data type, it's the same DAO attributes that we need. 
            case GLOBAL_DATA_QUALITY:
            case CALL_DATA: 
                return dataTypesRequested.stream().map(type -> Optional.ofNullable(typeToDAOAttr.get(type))
                        //bug of javac for type inference, we need to type the exception 
                        //explicitly to RuntimeException,
                        //see http://stackoverflow.com/questions/25523375/java8-lambdas-and-exceptions
                        .<RuntimeException>orElseThrow(() -> log.throwing(new IllegalStateException(
                                "Unsupported DataType: " + type))));
            case GLOBAL_ANAT_PROPAGATION: 
                return Stream.of(ExpressionCallDAO.Attribute.ANAT_ORIGIN_OF_LINE);
            case GLOBAL_STAGE_PROPAGATION: 
                return Stream.of(ExpressionCallDAO.Attribute.STAGE_ORIGIN_OF_LINE);
            case GLOBAL_OBSERVED_DATA: 
                return Stream.of(ExpressionCallDAO.Attribute.OBSERVED_DATA);
            case CALL_DATA_OBSERVED_DATA: 
                //nothing here, the only way to get this information is by performing 2 queries, 
                //one including substructures/sub-stages, another one without substructures/sub-stages.
                return Stream.empty();
            case GLOBAL_RANK: 
                return Stream.of(ExpressionCallDAO.Attribute.GLOBAL_MEAN_RANK);
            default: 
                throw log.throwing(new IllegalStateException("Unsupported Attributes from CallService: "
                        + attr));
            }
        }).collect(Collectors.toCollection(() -> EnumSet.noneOf(ExpressionCallDAO.Attribute.class))));
    }
    
    //*************************************************************************
    // METHODS PROPAGATION: from CallTOs to propagated Calls
    //*************************************************************************

    /**
     * Propagate {@code ExpressionCall}s to descendant or ancestor conditions 
     * from {@code conditionUtils} and valid for {@code conditionFilter}.
     * <p>
     * Return {@code ExpressionCall}s have {@code DataPropagation}, {@code ExpressionSummary}, 
     * and {@code DataQuality} equal to {@code null}. 
     *  
     * @param calls             A {@code Collection} of {@code ExpressionCall}s to be propagated.
     * @param conditionFilter   A {@code Collection} of {@code ConditionFilter}s to configure 
     *                          the filtering of conditions in propagated calls. 
     *                          If several {@code ConditionFilter}s are provided, they are seen as
     *                          "OR" conditions. Can be {@code null} or empty. 
     * @param conditionUtils    A {@code ConditionUtils} containing at least anat. entity
     *                          {@code Ontology} to use for the propagation.
     * @param speciesId         A {@code String} that is the ID of the species 
     *                          which to propagate call for.
     * @return                  A {@code Set} of {@code ExpressionCall}s that are propagated calls.
     * @throws IllegalArgumentException If {@code calls} is null, empty, or 
     *                                  contains already propagated calls.
     */
    // NOTE: No update ExpressionCalls, to provide better unicity of the method, and allow better unit testing
    public Set<ExpressionCall> propagateExpressionCalls(Collection<ExpressionCall> calls,
            Collection<ConditionFilter> conditionFilter, ConditionUtils conditionUtils, 
            String speciesId) throws IllegalArgumentException {
        log.entry(calls, conditionFilter, conditionUtils, speciesId);
        
        if (calls == null || calls.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No ExpressionCalls provided"));
        }

        Set<ConditionFilter> clonedConditionFilters = Collections.unmodifiableSet(
                conditionFilter == null? new HashSet<>() : new HashSet<>(conditionFilter));

        // Check that ExpressionCalls are expressed or not expressed calls
        if (calls.stream()
                .anyMatch(c -> c.getSummaryCallType() == null 
                    && !c.getSummaryCallType().equals(ExpressionSummary.EXPRESSED) 
                    && !c.getSummaryCallType().equals(ExpressionSummary.NOT_EXPRESSED))) {
                throw log.throwing(new IllegalArgumentException(
                        "All provided ExpressionCalls should be expressed or not expressed calls"));
        }
        
        // Check that ExpressionCalls are not propagated
        if (calls.stream()
                .anyMatch(c -> c.getDataPropagation() == null 
                || c.getDataPropagation().equals(DataPropagation.PropagationState.SELF))) {
                throw log.throwing(new IllegalArgumentException(
                        "All provided ExpressionCalls should be not propagated"));
        }

        // Propagate ExpressionCalls according their ExpressionSummary.
        Set<ExpressionCall> propagatedCalls = new HashSet<>();
        Set<ExpressionCall> expressedCalls = calls.stream()
                .filter(c -> c.getSummaryCallType().equals(ExpressionSummary.EXPRESSED))
                .collect(Collectors.toSet());
        if (!expressedCalls.isEmpty()) {
            Set<ExpressionCall> propagatedExpressedCalls = this.propagateExpressionCalls(
                    expressedCalls, clonedConditionFilters, conditionUtils, speciesId, true);
            if (propagatedExpressedCalls != null) {
                propagatedCalls.addAll(propagatedExpressedCalls);
            }
        }        
        Set<ExpressionCall> notExpressedCalls = calls.stream()
                .filter(c -> c.getSummaryCallType().equals(ExpressionSummary.NOT_EXPRESSED))
                .collect(Collectors.toSet());
        if (!notExpressedCalls.isEmpty()) {
            Set<ExpressionCall> propagatedNotExpressedCalls = this.propagateExpressionCalls(
                    notExpressedCalls, clonedConditionFilters, conditionUtils, speciesId, false);
            if (propagatedNotExpressedCalls != null) {
                propagatedCalls.addAll(propagatedNotExpressedCalls);
            }
        }
        return log.exit(propagatedCalls);
    }
    
    /**
     * Propagate {@code ExpressionCall}s to descendant or ancestor conditions, according to 
     * {@code areExpressedCalls}, from {@code conditionUtils} and valid for {@code conditionFilter}.
     * <p>
     * Return {@code ExpressionCall}s having {@code DataPropagation}, {@code ExpressionSummary}, 
     * and {@code DataQuality} equal to {@code null}.
     * 
     * @param calls             A {@code Set} of {@code ExpressionCall}s to be propagated.
     * @param conditionFilter   A {@code Set} of {@code ConditionFilter}s to configure 
     *                          the filtering of conditions in propagated calls. 
     *                          If several {@code ConditionFilter}s are provided, they are seen as
     *                          "OR" conditions. Can be {@code null} or empty. 
     * @param conditionUtils    A {@code ConditionUtils} containing at least anat. entity
     *                          {@code Ontology} to use for the propagation.
     * @param speciesId         A {@code String} that is the ID of the species 
     *                          which to propagate call for.
     * @param areExpressedCalls A {@code boolean} defining whether propagation should be done to
     *                          to descendant or ancestor conditions. If {@code true}, the propagation
     *                          is done to descendants. 
     * @return                  A {@code Set} of {@code ExpressionCall}s that are propagated calls.
     * @throws IllegalArgumentException
     */
    private Set<ExpressionCall> propagateExpressionCalls(Set<ExpressionCall> calls,
            Set<ConditionFilter> conditionFilter, ConditionUtils conditionUtils, 
            String speciesId, boolean areExpressedCalls) throws IllegalArgumentException {
        log.entry(calls, conditionFilter, conditionUtils, speciesId, areExpressedCalls);
        
        // As it is a private method, we can assume that provided parameters
        // have already been checked but we can add asserts
        assert calls != null && !calls.isEmpty();
        assert conditionFilter != null;
        assert conditionUtils != null;
        assert speciesId != null;
        
        // Check that TOs are not empty and not already propagated
        if (calls == null || calls.isEmpty()) {
            log.trace("No calls to propagate");
            return log.exit(null);
        }

        // Check conditionUtils contains all conditions of callTOs
        Set<Condition> conditions = calls.stream().map(c -> c.getCondition()).collect(Collectors.toSet());
        
        if (!conditionUtils.getConditions().containsAll(conditions)) {
            throw log.throwing(new IllegalArgumentException(
                    "Conditions are not registered to provided ConditionUtils"));
        }
        
        log.trace("Generating propagated expression calls...");
        
        // Here, no calls should have PropagationState which is not SELF
        assert !calls.stream().anyMatch(c -> c.getDataPropagation().getAllPropagationStates()
                .contains(EnumSet.complementOf(EnumSet.of(PropagationState.SELF)))); 
        // Here, no calls should include non-observed data
        assert !calls.stream().anyMatch(c -> !c.getDataPropagation().getIncludingObservedData()); 
        
        // Counts for log tracing 
        int callCount = calls.size();
        int analyzedCallCount = 0;

        // Propagate species by species
        Set<ExpressionCall> allPropagatedCalls = new HashSet<>();
        for (ExpressionCall call: calls) {
            analyzedCallCount++;
            if (log.isDebugEnabled() && analyzedCallCount % 100000 == 0) {
                log.debug("{}/{} expression calls analyzed.", analyzedCallCount, callCount);
            }
    
            log.trace("Propagation for expression call: {}", call);
    
            // Retrieve conditions of the species keeping conditions in allowed organs and stages only
            Set<Condition> propagatedConditions = null;
            if (areExpressedCalls) {
                propagatedConditions = conditionUtils.getAncestorConditions(call.getCondition(), false);
                log.debug("Ancestor conditions for {}: {}", call.getCondition(), propagatedConditions);
            } else {
                propagatedConditions = conditionUtils.getDescendantConditions(call.getCondition(), false);
                log.debug("Descendant conditions for {}: {}",  call.getCondition(), propagatedConditions);
            }
            propagatedConditions.add(call.getCondition());
            
            assert propagatedConditions != null;
    
            // Propagation to propagated conditions
            Set<ExpressionCall> propagatedCalls = this.propagateExpressionCall(
                    call, propagatedConditions, conditionFilter);
            allPropagatedCalls.addAll(propagatedCalls);
            
            log.trace("Add the propagated calls: {}", propagatedCalls);
        }

        log.trace("Done generating propagated calls.");

        return log.exit(allPropagatedCalls);
    }
    
    /**
     * Propagate {@code ExpressionCall} to provided {@code parentConditions}.
     * 
     * @param call              An {@code ExpressionCall} that is the call to be propagated.
     * @param conditions        A {@code Collection} of {@code Condition}s that are the conditions 
     *                          in which the propagation have to be done.
     * @param conditionFilters  A {@code Collection} of {@code ConditionFilter}s to configure 
     *                          the filtering of conditions in propagated calls. 
     *                          If several {@code ConditionFilter}s are provided, they are seen as
     *                          "OR" conditions. Can be {@code null} or empty. 
     * @return                  A {@code Set} of {@code ExpressionCall}s that are propagated calls
     *                          from provided {@code childCall}.
     */
    private Set<ExpressionCall> propagateExpressionCall(ExpressionCall call,
            Collection<Condition> conditions, Collection<ConditionFilter> conditionFilters) {
        log.entry(call, conditions, conditionFilters);
        
        log.trace("Propagation for call: {}", call);
        
        Set<ExpressionCall> globalCalls = new HashSet<>();
        Condition inputCondition = call.getCondition();

        // We should add input call condition to not loose that call
        Set<Condition> allConditions = new HashSet<>(conditions);
        allConditions.add(inputCondition);
        
        for (Condition condition : allConditions) {
            if (conditionFilters != null && !conditionFilters.isEmpty() 
                    && !conditionFilters.stream().anyMatch(f -> f.test(condition))) {
                continue;
            }
            
            log.trace("Propagation of the current call to condition: {}", condition);

            Set<ExpressionCallData> selfCallData = new HashSet<>();
            Set<ExpressionCallData> relativeCallData = new HashSet<>();
            
            for (ExpressionCallData callData: call.getCallData()) {
                
                if (!callData.getDataPropagation().equals(
                           new DataPropagation(PropagationState.SELF, PropagationState.SELF, true))
                   && !callData.getDataPropagation().equals(
                           new DataPropagation(PropagationState.SELF, PropagationState.SELF, null))) {
                    throw log.throwing(new IllegalArgumentException(
                            "ExpressionCallData already propagated: " + callData));
                }
                
                selfCallData.add(new ExpressionCallData(callData.getCallType(),
                        callData.getDataQuality(), callData.getDataType(), 
                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)));

                PropagationState anatEntityPropagationState = null;
                PropagationState devStagePropagationState = null;
                if (callData.getCallType().equals(Expression.EXPRESSED)) {
                    anatEntityPropagationState = PropagationState.DESCENDANT;
                    devStagePropagationState = PropagationState.DESCENDANT;
                } else if (callData.getCallType().equals(Expression.NOT_EXPRESSED)) {
                    anatEntityPropagationState = PropagationState.ANCESTOR;
                } else {
                    throw log.throwing(new IllegalArgumentException("Unsupported Expression"));
                }
                
                if (inputCondition.getAnatEntityId().equals(condition.getAnatEntityId())) {
                    anatEntityPropagationState = PropagationState.SELF;
                }
                if (inputCondition.getDevStageId().equals(condition.getDevStageId())) {
                    devStagePropagationState = PropagationState.SELF;
                }
                
                boolean includingObservedData = false;
                if (anatEntityPropagationState == PropagationState.SELF 
                        && devStagePropagationState == PropagationState.SELF) {
                    includingObservedData = true;
                }
                assert anatEntityPropagationState != null && devStagePropagationState != null;

                // NOTE: we do not manage includingObservedData here, 
                // it's should be done during the grouping of ExpressionCalls
                relativeCallData.add(new ExpressionCallData(callData.getCallType(),
                        callData.getDataQuality(), callData.getDataType(), 
                        new DataPropagation(anatEntityPropagationState, devStagePropagationState,
                                includingObservedData)));
            }
            
            // Add propagated expression call.
            Set<ExpressionCallData> currentCallData = null;
            BigDecimal currentGlobalMeanRank = null;
            if (inputCondition.equals(condition)) {
                currentCallData = selfCallData;
                // The global mean rank is kept only when it is not a propagated call
                currentGlobalMeanRank = call.getGlobalMeanRank();
            } else {
                currentCallData = relativeCallData;
            }

            ExpressionCall propagatedCall = new ExpressionCall(
                    call.getGeneId(),
                    condition,
                    null, // DataPropagation (update after the propagation of all TOs)
                    null, // ExpressionSummary (update after the propagation of all TOs)
                    null, // DataQuality (update after the propagation of all TOs)
                    currentCallData, 
                    currentGlobalMeanRank);

            log.debug("Add the propagated call: {}", propagatedCall);
            globalCalls.add(propagatedCall);
        }
    
        return log.exit(globalCalls);        
    }
    
    /** 
     * Reconcile calls for a single-gene: either for a single organ, or for a group of 
     * homologous organs (e.g., expr affy vs. no-expr RNA-Seq). 
     * <p>
     * Return the representative {@code ExpressionCall} (with reconciled quality per data types,
     * observed data state, conflict status etc., but not with organId-stageId.
     * The condition is set to {@code null}.
     * 
     * @param calls A {@code Collection} of {@code ExpressionCall}s that are the calls to be reconciled.
     * @return      The representative {@code ExpressionCall} (with reconciled quality per data types,
     *              observed data state, conflict status etc. But not with organId-stageId)
     */
    // TODO to be added to ExpressionCallUtils see TODOs into ExpressionCall
    public static ExpressionCall reconcileSingleGeneCalls(Collection<ExpressionCall> calls) {
        log.entry(calls);
        
        // Check calls have same gene ID
        Set<String> geneIds = calls.stream().map(c -> c.getGeneId()).collect(Collectors.toSet());
        if (geneIds.size() != 1) {
            throw log.throwing(new IllegalArgumentException(
                    "Provided no gene ID or several gene IDs: " + geneIds));
        }
        String geneId = geneIds.iterator().next();
        
        Set<ExpressionCallData> callData = calls.stream()
                .map(c-> c.getCallData())
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        // DataPropagation
        PropagationState anatEntityPropagationState = CallService.summarizePropagationState(
                callData.stream()
                    .map(c -> c.getDataPropagation().getAnatEntityPropagationState())
                    .collect(Collectors.toSet()));
        PropagationState devStagePropagationState = CallService.summarizePropagationState(
                callData.stream()
                    .map(c -> c.getDataPropagation().getDevStagePropagationState())
                    .collect(Collectors.toSet()));
        boolean includingObservedData = callData.stream()
                .anyMatch(c -> c.getDataPropagation().getIncludingObservedData() == true);
        DataPropagation callDataProp = new DataPropagation(
                anatEntityPropagationState, devStagePropagationState, includingObservedData);

        // ExpressionSummary
        ExpressionSummary expressionSummary;
        Set<Expression> expression = callData.stream()
                .map(c -> c.getCallType())
                .collect(Collectors.toSet());
        if (expression.size() == 1) {
            Expression expr = expression.iterator().next();
            switch (expr) {
                case EXPRESSED:
                    expressionSummary = ExpressionSummary.EXPRESSED;
                    break;
                case NOT_EXPRESSED:
                    expressionSummary = ExpressionSummary.NOT_EXPRESSED;
                    break;
                default:
                    throw log.throwing(new IllegalArgumentException("Unsupported Expression"));
            }
        } else {
            long notPropagatedNoExprCount = callData.stream()
                .filter(c -> Boolean.TRUE.equals(c.getDataPropagation().getIncludingObservedData()) &&
                        c.getCallType().equals(Expression.NOT_EXPRESSED))
                .count();
            
            if (notPropagatedNoExprCount == 0) {
                expressionSummary = ExpressionSummary.WEAK_AMBIGUITY;
            } else {
                expressionSummary = ExpressionSummary.STRONG_AMBIGUITY;
            }
        }
        
        //DataQuality
        DataQuality dataQuality = null;
        if (expressionSummary == ExpressionSummary.EXPRESSED 
                || expressionSummary == ExpressionSummary.NOT_EXPRESSED) {
            Set<DataQuality> qualities = callData.stream()
                    .map(c -> c.getDataQuality())
                    .collect(Collectors.toSet());
            if (qualities.contains(DataQuality.HIGH)) {
                dataQuality = DataQuality.HIGH;
            } else {
                dataQuality = DataQuality.LOW;
            }
        }

        // Global mean rank
        Optional<BigDecimal> bestGlobalMeanRank = calls.stream()
                .map(c -> c.getGlobalMeanRank())
                .filter(r -> r != null)
                .min((r1, r2) -> r1.compareTo(r2));

        return log.exit(new ExpressionCall(geneId, null, callDataProp, expressionSummary, 
                dataQuality, callData, bestGlobalMeanRank.orElse(null)));
    }
    
    /**
     * Summarize {@code PropagationState}s from {@code ExpressionCall}s.
     * 
     * @param propStates    A {@code Set} of {@code PropagationState}s that are propagation states
     *                      to summarize in one {@code PropagationState}.
     * @return              The {@code PropagationState} that is the summary of provided {@code propStates}.
     * @throws IllegalArgumentException If it is impossible to summarize provided {@code PropagationState}s.
     *                                  For instance, {@code PropagationState.DESCENDANT} and 
     *                                  {@code PropagationState.SELF_OR_ANCESTOR} combination.
     */
    private static PropagationState summarizePropagationState(Set<PropagationState> propStates) 
            throws IllegalArgumentException {
        log.entry(propStates);
        
        if (propStates.contains(PropagationState.ALL)) {
            return log.exit(PropagationState.ALL);
        }

        if (propStates.size() == 1) {
            return log.exit(propStates.iterator().next());
        }

        HashSet<PropagationState> desc = new HashSet<>(Arrays.asList(
                PropagationState.DESCENDANT, PropagationState.SELF_AND_DESCENDANT, PropagationState.ALL));
        HashSet<PropagationState> asc = new HashSet<>(Arrays.asList(
                PropagationState.ANCESTOR, PropagationState.SELF_AND_ANCESTOR, PropagationState.ALL));
        HashSet<PropagationState> self = new HashSet<>(Arrays.asList(
                PropagationState.SELF, PropagationState.SELF_AND_DESCENDANT,
                PropagationState.SELF_AND_ANCESTOR, PropagationState.ALL));

        boolean fromDesc = !Collections.disjoint(propStates, desc);
        boolean fromAsc = !Collections.disjoint(propStates, asc);
        boolean fromSelf = !Collections.disjoint(propStates, self);
        
        if (fromDesc && fromAsc && fromSelf) {
            return log.exit(PropagationState.ALL);
        }

        if (fromDesc && fromSelf) {
            return log.exit(PropagationState.SELF_AND_DESCENDANT);
        }

        if (fromAsc && fromSelf) {
            return log.exit(PropagationState.SELF_AND_ANCESTOR);
        }
        
        if (fromAsc && fromDesc && !propStates.contains(PropagationState.SELF_OR_ANCESTOR)
                && !propStates.contains(PropagationState.SELF_OR_DESCENDANT)) {
            return log.exit(PropagationState.ANCESTOR_AND_DESCENDANT);
        }

        if (propStates.containsAll(
                Arrays.asList(PropagationState.SELF_OR_ANCESTOR, PropagationState.SELF))
            || propStates.containsAll(
                    Arrays.asList(PropagationState.SELF_OR_ANCESTOR, PropagationState.ANCESTOR))) {
            return log.exit(PropagationState.SELF_OR_ANCESTOR);
        }
        if (propStates.containsAll(
                Arrays.asList(PropagationState.SELF_OR_DESCENDANT, PropagationState.SELF))
            || propStates.containsAll(
                    Arrays.asList(PropagationState.SELF_OR_DESCENDANT, PropagationState.DESCENDANT))) {
            return log.exit(PropagationState.SELF_OR_DESCENDANT);
        }

        // XXX: Not resolved combinations:
        // - ANCESTOR && DESCENDANT &  & SELF_OR_ANCESTOR
        // - ANCESTOR && DESCENDANT && SELF_OR_ANCESTOR && ANCESTOR_AND_DESCENDANT
        // - ANCESTOR && DESCENDANT && SELF_OR_ANCESTOR && SELF_OR_DESCENDANT
        // - ANCESTOR && DESCENDANT && SELF_OR_ANCESTOR && SELF_OR_DESCENDANT && ANCESTOR_AND_DESCENDANT
        // - ANCESTOR && DESCENDANT && SELF_OR_DESCENDANT
        // - ANCESTOR && DESCENDANT && SELF_OR_DESCENDANT && ANCESTOR_AND_DESCENDANT
        // - ANCESTOR && SELF_OR_ANCESTOR && ANCESTOR_AND_DESCENDANT
        // - ANCESTOR && SELF_OR_ANCESTOR && SELF_OR_DESCENDANT
        // - ANCESTOR && SELF_OR_ANCESTOR && SELF_OR_DESCENDANT && ANCESTOR_AND_DESCENDANT
        // - ANCESTOR && SELF_OR_DESCENDANT
        // - ANCESTOR && SELF_OR_DESCENDANT && ANCESTOR_AND_DESCENDANT
        // - DESCENDANT && SELF_OR_ANCESTOR
        // - DESCENDANT && SELF_OR_ANCESTOR && ANCESTOR_AND_DESCENDANT
        // - DESCENDANT && SELF_OR_ANCESTOR && SELF_OR_DESCENDANT
        // - DESCENDANT && SELF_OR_ANCESTOR && SELF_OR_DESCENDANT && ANCESTOR_AND_DESCENDANT
        // - DESCENDANT && SELF_OR_DESCENDANT && ANCESTOR_AND_DESCENDANT
        // - SELF_OR_ANCESTOR && ANCESTOR_AND_DESCENDANT
        // - SELF_OR_ANCESTOR && SELF_OR_DESCENDANT
        // - SELF_OR_ANCESTOR && SELF_OR_DESCENDANT && ANCESTOR_AND_DESCENDANT
        // - SELF_OR_DESCENDANT && ANCESTOR_AND_DESCENDANT
        // - SELF && SELF_OR_ANCESTOR && SELF_OR_DESCENDANT
        throw log.throwing(new IllegalArgumentException(
                "Impossible to summarize provided propagation states: " + propStates));
    }

}
