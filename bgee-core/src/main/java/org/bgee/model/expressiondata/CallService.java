package org.bgee.model.expressiondata;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
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
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;
import org.bgee.model.dao.api.expressiondata.CallDAOFilter.ExpressionCallDAOFilter;
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
            ExpressionCallFilter callFilter) throws IllegalArgumentException {
        log.entry(speciesId, callFilter);
        return log.exit(this.loadCalls(speciesId, Arrays.asList(callFilter))
                .map(call -> (ExpressionCall) call));
    }
    
    public Stream<DiffExpressionCall> loadDiffExpressionCalls(String speciesId, 
            DiffExpressionCallFilter callFilter) {
        //TODO
        return null;
    }
    
    //XXX: example single-species query, signature/returned value to be better defined
    //XXX: would several CallFilters represent AND or OR conditions?
    public Stream<? extends Call<?, ?>> loadCalls(
            String speciesId, Collection<CallFilter<?>> callFilters) throws IllegalArgumentException {
        log.entry(speciesId, callFilters);
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
        
        
        if (clonedFilters.stream().flatMap(filter -> filter.getCallDataFilters().stream())
                .anyMatch(callData -> Expression.NOT_EXPRESSED.equals(callData.getCallType()) || 
                        EnumSet.allOf(DiffExpression.class).contains(callData.getCallType()))) {
            throw log.throwing(new UnsupportedOperationException(
                    "Management of diff. expression and no-expression queries not yet implemented."));
        }

        Set<Stream<? extends Call<?, ?>>> streamsJoinAnd = new HashSet<>();
        for (CallFilter<?> filter: clonedFilters) { 
            //dispatch the CallData per DAO needed. 
            Set<Stream<? extends Call<?, ?>>> streamsJoinOr = new HashSet<>();
            streamsJoinOr.addAll(this.performsExpressionQueries(speciesId, filter));
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
    private Set<Stream<ExpressionCall>> performsExpressionQueries(String speciesId, 
            CallFilter<?> callFilter) {
        log.entry(speciesId, callFilter);
        
        //dispatch ExpressionCallDatas between different propagation states requested, 
        //to determine the ExpressionCallDAO parameters includeSubstructures, and includeSubStages.
        //We use DataPropagation objects to do the dispatching. 
        Map<DataPropagation, Set<ExpressionCallData>> dispatchedCallData = 
                callFilter.getCallDataFilters().stream()
                
                //consider only callData for the ExpressionCallDAO
                .filter(callData -> Expression.EXPRESSED.equals(callData.getCallType()))
                
                .collect(Collectors
                        //I don't know why, but javac doesn't manage to infer the correct types, 
                        //while Eclipse does, so we type explicitly the method toMap
                        .<CallData<?>, DataPropagation, Set<ExpressionCallData>>toMap(callData -> 
                        //create a DataPropagation object as key, using only 
                        //PropagationState.SELF and PropagationState.SELF_OR_DESCENDANT, 
                        //to determine whether to include substructures/sub-stages
                        new DataPropagation(
                                PropagationState.SELF.equals(
                                        callData.getDataPropagation().getAnatEntityPropagationState())?
                                        PropagationState.SELF: PropagationState.SELF_OR_DESCENDANT, 
                                PropagationState.SELF.equals(
                                        callData.getDataPropagation().getDevStagePropagationState())?
                                        PropagationState.SELF: PropagationState.SELF_OR_DESCENDANT), 
                        
                        //store the callData in a Set as value
                        callData -> new HashSet<ExpressionCallData>(Arrays.asList(
                                (ExpressionCallData) callData)),
                        //merge in case of key collision
                        (v1, v2) -> {
                            Set<ExpressionCallData> merge = new HashSet<>(v1);
                            merge.addAll(v2);
                            return merge;
                        })
                );
        
        //now, do one query for each combination of propagation states
        final ExpressionCallDAO dao = this.getDaoManager().getExpressionCallDAO();
        return log.exit(dispatchedCallData.entrySet().stream()
               
                //perform one query to the DAO for each entry. From each of these queries,  
                //we retrieve the Stream<ExpressionCallTO> returned by DAOResultSet.stream(), 
                //and we map it to a Stream<ExpressionCall>. Then we collect the Streams into a Set 
                //(without actually consuming them)
                .map(entry -> 
                    dao.getExpressionCalls(Arrays.asList(
                        //generate an ExpressionCallDAOFilter from callFilter and its ExpressionCallDatas
                        new ExpressionCallDAOFilter(
                            //we will provide the gene IDs to the getExpressionCalls method 
                            //as a global gene filter, not through the ExpressionCallDAOFilter. 
                            null, 
                            //species
                            Arrays.asList(speciesId), 
                            //ConditionFilters
                            callFilter.getConditionFilters().stream()
                                .map(condFilter -> new DAOConditionFilter(
                                        condFilter.getAnatEntitieIds(), 
                                        condFilter.getDevStageIds()))
                                .collect(Collectors.toSet()), 
                            //CallFilter
                            entry.getValue().stream()
                                .flatMap(callData -> mapCallDataToCallTOFilters(callData).stream())
                                .collect(Collectors.toSet())
                        )), 
                        //includeSubstructures
                        !PropagationState.SELF.equals(entry.getKey().getAnatEntityPropagationState()), 
                        //includeSubStages
                        !PropagationState.SELF.equals(entry.getKey().getDevStagePropagationState()), 
                        //global gene filter
                        callFilter.getGeneFilter().getGeneIds(), 
                        //no gene orthology requested
                        null, 
                        //TODO: attributes and ordering attributes
                        null, null
                    )
                    //retrieve the Stream resulting from the query. Note that the query is not executed 
                    //as long as the Stream is not consumed (lazy-loading).
                    .stream()
                    //allow mapping of the ExpressionCallTOs to ExpressionCalls. The Stream is still 
                    //not consumed at this point (map is a lazy operation as well). 
                    .map(callTO -> mapCallTOToExpressionCall(callTO, entry.getKey()))
                )
                //generates a Set<Stream<ExpressionCall>>. Note that while 
                //the Stream<Entry<DataPropagation, Set<ExpressionCallData>>> is consumed at this point, 
                //the Streams stored in the resulting Set are not. 
                .collect(Collectors.toSet()));
    }

    //*************************************************************************
    // METHODS MAPPING CallTOs TO Calls
    //*************************************************************************
    private static ExpressionCall mapCallTOToExpressionCall(ExpressionCallTO callTO, 
            DataPropagation daoDataPropagation) {
        log.entry(callTO, daoDataPropagation);
        
        if (Arrays.stream(new PropagationState[]{
                        daoDataPropagation.getAnatEntityPropagationState(), 
                        daoDataPropagation.getDevStagePropagationState()})
                  .anyMatch(e -> !PropagationState.SELF.equals(e) && 
                                 !PropagationState.SELF_OR_DESCENDANT.equals(e))) {
            throw log.throwing(new IllegalArgumentException("Only the propagation states "
                    + "SELF and SELF_OR_DESCENDANT are permitted for an ExpressionCallDAO query."));
        }
        
        //infer the global Propagation status of the call, either from the CallTO 
        //if it contains this information, or from the PropagationState defined for the DAO.
        DataPropagation globalPropagation = new DataPropagation(
                Optional.ofNullable(convertExprOriginToPropagationState(callTO.getAnatOriginOfLine()))
                .orElse(daoDataPropagation.getAnatEntityPropagationState()), 
                Optional.ofNullable(convertExprOriginToPropagationState(callTO.getStageOriginOfLine()))
                .orElse(daoDataPropagation.getDevStagePropagationState()));
        
        return log.exit(new ExpressionCall(callTO.getGeneId(), 
                callTO.getAnatEntityId() != null || callTO.getStageId() == null? 
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
                            //at this point, we cannot know the propagation status per data type, 
                            //the expression tables only store a global propagation status 
                            //over all data types. To infer the status per data type, 
                            //we would need two queries, one including sub-stages/subtructures, 
                            //and another one not including them. 
                            //so here, we provide the only thing we know: the propagation status 
                            //requested to the DAO (using SELF or SELF_OR_DESCENDANT)
                            daoDataPropagation))
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
    private static DataType convertExprAttributeToDataType(
            ExpressionCallDAO.Attribute attr) throws IllegalStateException {
        log.entry(attr);
        
        switch (attr) {
        case AFFYMETRIX_DATA: 
            return log.exit(DataType.AFFYMETRIX);
        case EST_DATA: 
            return log.exit(DataType.EST);
        case IN_SITU_DATA: 
            return log.exit(DataType.IN_SITU);
        case RNA_SEQ_DATA: 
            return log.exit(DataType.RNA_SEQ);
        default: 
            throw log.throwing(new IllegalStateException("Unsupported ExpressionCallDAO.Attribute: "
                     + attr));
        }
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
    // METHODS MAPPING CallDatas TO CallTOs
    //*************************************************************************
    private static Set<ExpressionCallTO> mapCallDataToCallTOFilters(ExpressionCallData callData) {
        log.entry(callData);
        
        //if the dataType of the callData is null, then it means that it targets all data types. 
        //In order to get OR conditions between data type parameters 
        //(e.g., affymetrixData >= HIGH OR rnaSeqData >= HIGH), we need to create one ExpressionCallTO 
        //per data type (because data type parameters inside a same ExpressionCallTO are considered 
        //as AND conditions)
        Set<DataType> dataTypes = callData.getDataType() != null? 
                EnumSet.of(callData.getDataType()): EnumSet.allOf(DataType.class);
                
        return log.exit(dataTypes.stream().map(dataType -> {
            CallTO.DataState affyState = null;
            CallTO.DataState estState = null;
            CallTO.DataState inSituState = null;
            CallTO.DataState rnaSeqState = null;
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
            return new ExpressionCallTO(affyState, estState, inSituState, rnaSeqState, 
            convertPropagationStateToExprOrigin(callData.getDataPropagation().getAnatEntityPropagationState()), 
            convertPropagationStateToExprOrigin(callData.getDataPropagation().getDevStagePropagationState()), 
            null);
        }).collect(Collectors.toSet()));
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
}
