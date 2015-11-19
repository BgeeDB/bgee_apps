package org.bgee.model.expressiondata;

import java.util.AbstractMap.SimpleEntry;
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
 * @author Frederic Bastian
 * @version Bgee 13 Oct. 2015
 * @since Bgee 13 Oct. 2015
 */
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
        GENE_ID, ANAT_ENTITY_ID, DEV_STAGE_ID, GLOBAL_DATA_QUALITY, CALL_DATA, 
        GLOBAL_ANAT_PROPAGATION, GLOBAL_STAGE_PROPAGATION, GLOBAL_OBSERVED_DATA, 
        CALL_DATA_OBSERVED_DATA;
    }
    public static enum OrderingAttribute implements Service.OrderingAttribute {
        GENE_ID, ANAT_ENTITY_ID, DEV_STAGE_ID, RANK;
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
                        callFilter.getGeneFilter().getGeneIds(), 
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
                    .map(callTO -> mapCallTOToExpressionCall(callTO, callFilter.getDataPropagationFilter())
                ));
    }

    //*************************************************************************
    // METHODS MAPPING CallTOs TO Calls
    //*************************************************************************
    private static ExpressionCall mapCallTOToExpressionCall(ExpressionCallTO callTO, 
            DataPropagation callFilterPropag) {
        log.entry(callTO, callFilterPropag);

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
                .orElse(callDataPropagation.getDevStagePropagationState()));
        
        return log.exit(new ExpressionCall(callTO.getGeneId(), 
                callTO.getAnatEntityId() != null || callTO.getStageId() != null? 
                        new Condition(callTO.getAnatEntityId(), callTO.getStageId()): null, 
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
                    
                    .collect(Collectors.toSet())
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
            return log.exit(null);
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
                case RANK: 
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
                return Stream.of(ExpressionCallDAO.Attribute.GENE_ID);
            case DEV_STAGE_ID: 
                return Stream.of(ExpressionCallDAO.Attribute.GENE_ID);
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
            default: 
                throw log.throwing(new IllegalStateException("Unsupported Attributes from CallService: "
                        + attr));
            }
        }).collect(Collectors.toCollection(() -> EnumSet.noneOf(ExpressionCallDAO.Attribute.class))));
    }
}
