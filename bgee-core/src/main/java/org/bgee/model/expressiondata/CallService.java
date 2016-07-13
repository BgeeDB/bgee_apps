package org.bgee.model.expressiondata;

import java.util.AbstractMap.SimpleEntry;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;
import org.bgee.model.dao.api.expressiondata.CallDAOFilter;
import org.bgee.model.dao.api.expressiondata.DAOConditionFilter;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTO;
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
 * @version Bgee 13, July 2016
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
     * 0-arg constructor that will cause this {@code CallService} to use 
     * the default {@code DAOManager} returned by {@link DAOManager#getDAOManager()}. 
     * 
     * @see #CallService(DAOManager)
     */
    public CallService() {
        this(DAOManager.getDAOManager());
    }
    /**
     * @param daoManager    The {@code DAOManager} to be used by this {@code CallService} 
     *                      to obtain {@code DAO}s.
     * @throws IllegalArgumentException If {@code daoManager} is {@code null}.
     */
    public CallService(DAOManager daoManager) {
        super(daoManager);
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
        return null;
    }
    
    public Stream<ExpressionCall> loadExpressionCalls(String speciesId, 
            ExpressionCallFilter callFilter, Collection<Attribute> attributes, 
            LinkedHashMap<OrderingAttribute, Service.Direction> orderingAttributes) throws IllegalArgumentException {
        log.entry(speciesId, callFilter, attributes, orderingAttributes);
        return log.exit(this.loadCalls(speciesId, Arrays.asList(callFilter), 
                    attributes, orderingAttributes)
                .map(call -> (ExpressionCall) call));
    }
    
    public Stream<DiffExpressionCall> loadDiffExpressionCalls(String speciesId, 
            DiffExpressionCallFilter callFilter) {
        //TODO
        return null;
    }
    
    //XXX: example single-species query, signature/returned value to be better defined
    //XXX: would several CallFilters represent AND or OR conditions?
    
    //XXX: this method should not return Call objects, it should only take care
    //of the dispatch to different DAOs, and return a Map<CallFilter, Stream<TO>> or similar.
    //no post-processing of the results returned by DAOs.
    public Stream<? extends Call<?, ?>> loadCalls(String speciesId, 
            Collection<CallFilter<?>> callFilters, Collection<Attribute> attributes, 
            LinkedHashMap<OrderingAttribute, Service.Direction> orderingAttributes) 
                    throws IllegalArgumentException {
        log.entry(speciesId, callFilters, attributes, orderingAttributes);
        //sanity checks
        if (StringUtils.isBlank(speciesId)) {
            throw log.throwing(new IllegalArgumentException("A speciesID must be provided"));
        }
        if (callFilters == null || callFilters.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("At least one CallFilter must be provided."));
        }
        if (callFilters.stream().anyMatch(Objects::isNull)) {
            throw log.throwing(new IllegalArgumentException("No CallFilter can be null"));
        }
        final Set<CallFilter<?>> clonedFilters = Collections.unmodifiableSet(new HashSet<>(callFilters));
        final Set<Attribute> clonedAttrs = Collections.unmodifiableSet(
                attributes == null? EnumSet.noneOf(Attribute.class): EnumSet.copyOf(attributes));
        LinkedHashMap<OrderingAttribute, Service.Direction> clonedOrderingAttrs = 
                orderingAttributes == null? new LinkedHashMap<>(): new LinkedHashMap<>(orderingAttributes);
        
        //UnsupportedOperationExceptions (for now)
        if (clonedAttrs.contains(Attribute.CALL_DATA_OBSERVED_DATA)) {
            throw log.throwing(new UnsupportedOperationException(
                    "Retrieval of observed data state per data type not yet implemented."));
        }
        if (clonedFilters.stream().flatMap(filter -> filter.getCallDataFilters().stream())
                .anyMatch(callData -> Expression.NOT_EXPRESSED.equals(callData.getCallType()) || 
                        EnumSet.allOf(DiffExpression.class).contains(callData.getCallType()))) {
            throw log.throwing(new UnsupportedOperationException(
                    "Management of diff. expression and no-expression queries not yet implemented."));
        }

        //OK, real work starts here
        //XXX: NO, should not returned processed Calls, only retrieve streams from DAOs
        Set<Stream<? extends Call<?, ?>>> streamsJoinAnd = new HashSet<>();
        for (CallFilter<?> filter: clonedFilters) { 
            //dispatch the CallData per DAO needed. 
            Set<Stream<? extends Call<?, ?>>> streamsJoinOr = new HashSet<>();
            streamsJoinOr.add(this.performsExpressionQueries(speciesId, filter, clonedAttrs, 
                    clonedOrderingAttrs));
            assert streamsJoinOr.size() > 0;
            
            if (streamsJoinOr.size() > 1) {
                throw log.throwing(new UnsupportedOperationException("Merge of results "
                        + "form several OR queries not yet implemented"));
            }
            
            //TODO: here, we need to implement an InterleaveMergeSpliterator.
            //in the mean time, simply add the current stream
            streamsJoinAnd.addAll(streamsJoinOr);
        }
        if (streamsJoinAnd.size() > 1) {
            throw log.throwing(new UnsupportedOperationException("Merge of results "
                    + "from several AND queries not yet implemented."));
        }
        
        //TODO: here, we need to implement a spliterator avoiding to put all data in memory 
        //to check whether a Call is present in all streams.
        //in the mean time, simply return the stream
        return log.exit(streamsJoinAnd.iterator().next());
    }

    //*************************************************************************
    // METHODS PERFORMING THE QUERIES TO THE DAOs
    //*************************************************************************
    private Stream<ExpressionCall> performsExpressionQueries(String speciesId, 
            CallFilter<?> callFilter, Set<Attribute> attributes, 
            LinkedHashMap<OrderingAttribute, Service.Direction> orderingAttributes) {
        log.entry(speciesId, callFilter, attributes, orderingAttributes);
        
        //Extract only the CallData related to expression queries
        Set<ExpressionCallData> exprCallData = 
                callFilter.getCallDataFilters().stream()
                //consider only callData for the ExpressionCallDAO
                .filter(callData -> Expression.EXPRESSED.equals(callData.getCallType()))
                .map(callData -> (ExpressionCallData) callData)
                .collect(Collectors.toSet());
        
        //now, do one query for each combination of propagation states
        final ExpressionCallDAO dao = this.getDaoManager().getExpressionCallDAO();
        return log.exit(
                dao.getExpressionCalls(Arrays.asList(
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
                                    callFilter.getDataPropagationFilter()).stream())
                            .collect(Collectors.toSet()), 
                        //includeSubstructures
                        !PropagationState.SELF.equals(callFilter.getDataPropagationFilter()
                                .getAnatEntityPropagationState()), 
                        //includeSubStages
                        !PropagationState.SELF.equals(callFilter.getDataPropagationFilter()
                                .getDevStagePropagationState()), 
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
                    .map(callTO -> mapCallTOToExpressionCall(callTO, 
                            callFilter.getDataPropagationFilter(), speciesId)
                ));
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
        //we would need two queries, one including sub-stages/subtructures, 
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
     * Propagate {@code NoExpressionTO}s to descendant conditions from {@code conditionUtils} 
     * and valid for {@code conditionFilter}.
     * <p>
     * Return {@code ExpressionCall}s have {@code DataPropagation}, {@code ExpressionSummary}, 
     * and {@code DataQuality} equal to {@code null}. 
     *  
     * @param noExprTOs         A {@code Collection} of {@code NoExpressionTO} to be propagated.
     * @param conditionFilter   A {@code Collection} of {@code ConditionFilter}s to configure 
     *                          the filtering of conditions in propagated calls. 
     *                          If several {@code ConditionFilter}s are provided, they are seen as
     *                          "OR" conditions. Can be {@code null} or empty. 
     * @param conditionUtils    A {@code ConditionUtils} containing at least anat. entity
     *                          {@code Ontology} to use for the propagation.
     * @param speciesId         A {@code String} that is the ID of the species 
     *                          which to propagate call for.
     * @return                  A {@code Set} of {@code ExpressionCall}s that are propagated calls.
     */
    // TODO: set to private because argument are TOs? 
    // NOTE: No update ExpressionCalls, to provide better unicity of the method, and allow better unit testing
    public Set<ExpressionCall> propagateNoExpressionTOs(Collection<NoExpressionCallTO> noExprTOs,
            Collection<ConditionFilter> conditionFilter, ConditionUtils conditionUtils,
            String speciesId) {
        log.entry(noExprTOs, conditionFilter, conditionUtils, speciesId);
        
        // Check that TOs are not empty and not already propagated
        if (noExprTOs == null || noExprTOs.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No NoExpressionTOs provided"));
        }
        Set<NoExpressionCallTO> alreadyPropagatedTOs = noExprTOs.stream()
            .filter(to -> to.getOriginOfLine() != NoExpressionCallTO.OriginOfLine.SELF 
                            || to.isIncludeParentStructures())
            .collect(Collectors.toSet());
        if (!alreadyPropagatedTOs.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "Some ExpressionTOs has already been propagated: " + alreadyPropagatedTOs));
        }
        
        return log.exit(this.propagateTOs(noExprTOs, conditionFilter, conditionUtils,
                speciesId, NoExpressionCallTO.class));
    }
    
    /**
     * Propagate {@code ExpressionTO}s to ancestor conditions from {@code conditionUtils} 
     * and valid for {@code conditionFilter}.
     * <p>
     * Return {@code ExpressionCall}s have {@code DataPropagation}, {@code ExpressionSummary}, 
     * and {@code DataQuality} equal to {@code null}. 
     *  
     * @param exprTOs           A {@code Collection} of {@code ExpressionCallTO} to be propagated.
     * @param conditionFilter   A {@code Collection} of {@code ConditionFilter}s to configure 
     *                          the filtering of conditions in propagated calls. 
     *                          If several {@code ConditionFilter}s are provided, they are seen as
     *                          "OR" conditions. Can be {@code null} or empty. 
     * @param conditionUtils    A {@code ConditionUtils} containing anat. entity and dev. stage
     *                          {@code Ontology}s to use for the propagation. 
     * @return                  A {@code Set} of {@code ExpressionCall}s that are propagated calls.
     */
    // TODO: set to private because argument are TOs? 
    // NOTE: No update ExpressionCalls, to provide better unicity of the method, and allow better unit testing
    public Set<ExpressionCall> propagateExpressionTOs(Collection<ExpressionCallTO> exprTOs,
            Collection<ConditionFilter> conditionFilter, ConditionUtils conditionUtils,
            String speciesId) {
        log.entry(exprTOs, conditionFilter, conditionUtils, speciesId);
        
        // Check that TOs are not empty and not already propagated
        if (exprTOs == null || exprTOs.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No ExpressionTOs provided"));
        }
        Set<ExpressionCallTO> alreadyPropagatedTOs = exprTOs.stream()
            .filter(to -> to.getAnatOriginOfLine() != ExpressionCallTO.OriginOfLine.SELF 
                            || to.getStageOriginOfLine() != ExpressionCallTO.OriginOfLine.SELF 
                            || !to.isObservedData())
            .collect(Collectors.toSet());
        if (!alreadyPropagatedTOs.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "Some ExpressionTOs has already been propagated: " + alreadyPropagatedTOs ));
        }
        
        return log.exit(
                this.propagateTOs(exprTOs, conditionFilter, conditionUtils, speciesId, ExpressionCallTO.class));
    }
    
    private <T extends CallTO> Set<ExpressionCall> propagateTOs(Collection<T> callTOs,
            Collection<ConditionFilter> conditionFilter, ConditionUtils conditionUtils, 
            String speciesId, Class<T> type) throws IllegalArgumentException {
        log.entry(callTOs, conditionFilter, conditionUtils, speciesId, type);
        
        // Check conditionUtils contains all conditions of callTOs
        Set<Condition> conditions = callTOs.stream()
            .map(to -> new Condition(to.getAnatEntityId(), to.getStageId(), speciesId))
            .collect(Collectors.toSet());
        
        if (!conditionUtils.getConditions().containsAll(conditions)) {
            throw log.throwing(new IllegalArgumentException(
                    "Conditions are not registered to provided ConditionUtils"));
        }
        
        log.trace("Generating propagated expression calls...");
    
        // Convert exprTOs into calls before propagation 
        // to be able to set dataPropagation in ExpressionCallData during propagation 
        Set<ExpressionCall> inputCalls = null;
        if (type.equals(ExpressionCallTO.class)) {
            inputCalls = callTOs.stream()
                    .map(to -> mapCallTOToExpressionCall((ExpressionCallTO) to, 
                            new DataPropagation(), speciesId))
                    .collect(Collectors.toSet());
    
        } else if (type.equals(NoExpressionCallTO.class)) {
            inputCalls = callTOs.stream()
                    .map(to -> mapCallTOToExpressionCall((NoExpressionCallTO) to,
                            new DataPropagation(), speciesId))
                    .collect(Collectors.toSet());
    
        } else {
            throw log.throwing(new IllegalArgumentException("There is no propagation " +
                    "implemented for CallTO " + type.getClass() + "."));
        }
        
        // Here, no calls should have PropagationState which is not SELF
        assert !inputCalls.stream().anyMatch(c -> c.getDataPropagation().getAllPropagationStates()
                .contains(EnumSet.complementOf(EnumSet.of(PropagationState.SELF)))); 
        // Here, no calls should include non-observed data
        assert !inputCalls.stream().anyMatch(c -> !c.getDataPropagation().getIncludingObservedData()); 
        
        // Counts for log tracing 
        int callCount = inputCalls.size();
        int analyzedCallCount = 0;

        // Propagate species by species
        Set<ExpressionCall> allPropagatedCalls = new HashSet<>();
        for (ExpressionCall call: inputCalls) {
            analyzedCallCount++;
            if (log.isDebugEnabled() && analyzedCallCount % 100000 == 0) {
                log.debug("{}/{} expression calls analyzed.", analyzedCallCount, callCount);
            }
    
            log.trace("Propagation for expression call: {}", call);
    
            // Retrieve conditions of the species keeping conditions in allowed organs and stages only
            Set<Condition> propagatedConditions = null;
            if (type.equals(ExpressionCallTO.class)) {
                propagatedConditions = conditionUtils.getAncestorConditions(call.getCondition(), true);
                
            } else if (type.equals(NoExpressionCallTO.class)) {
                propagatedConditions = conditionUtils.getDescendantConditions(call.getCondition(), true);
            }
            
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
     * @param conditions        A {@code Set} of {@code Condition}s that are the conditions 
     *                          in which the propagation have to be done.
     * @param conditionFilters  A {@code Collection} of {@code ConditionFilter}s to configure 
     *                          the filtering of conditions in propagated calls. 
     *                          If several {@code ConditionFilter}s are provided, they are seen as
     *                          "OR" conditions. Can be {@code null} or empty. 
     * @return                  A {@code Set} of {@code ExpressionCall}s that are propagated calls
     *                          from provided {@code childCall}.
     */
    private Set<ExpressionCall> propagateExpressionCall(ExpressionCall call,
            Set<Condition> conditions, Collection<ConditionFilter> conditionFilters) {
        log.entry(call, conditions, conditionFilters);
        
        log.trace("Propagation for call: {}", call);
        
        Set<ExpressionCall> globalCalls = new HashSet<>();
        Condition inputCondition = call.getCondition();

        // We should add input call condition to not loose that call
        Set<Condition> allConditions = new HashSet<>(conditions);
        allConditions.add(inputCondition);
        
        for (Condition condition : allConditions) {
            if (conditionFilters!=null && !conditionFilters.stream().anyMatch(f -> f.test(condition))) {
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
    public ExpressionCall reconcileSingleGeneCalls(Collection<ExpressionCall> calls) {
    // TODO add unit test for management of global mean ranks
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
        PropagationState anatEntityPropagationState = this.summarizePropagationState(
                callData.stream()
                    .map(c -> c.getDataPropagation().getAnatEntityPropagationState())
                    .collect(Collectors.toSet()));
        PropagationState devStagePropagationState = this.summarizePropagationState(
                callData.stream()
                    .map(c -> c.getDataPropagation().getDevStagePropagationState())
                    .collect(Collectors.toSet()));
        
        HashSet<PropagationState> selfStates = new HashSet<>(Arrays.asList(
                PropagationState.SELF, PropagationState.SELF_AND_DESCENDANT,
                PropagationState.SELF_AND_ANCESTOR, PropagationState.ALL));
        Boolean includingObservedData = false;
        if (selfStates.contains(anatEntityPropagationState) && selfStates.contains(devStagePropagationState)) {
            includingObservedData = true;
        }
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
        } else {
            // Ambiguity
            dataQuality = null;
        }

        // Global mean rank
        Optional<BigDecimal> bestGlobalMeanRank = calls.stream()
            .filter(c -> c.getGlobalMeanRank() != null)
            .map(c -> c.getGlobalMeanRank())
            .max((r1, r2) -> r1.compareTo(r2));
        
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
    private PropagationState summarizePropagationState(Set<PropagationState> propStates) 
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
