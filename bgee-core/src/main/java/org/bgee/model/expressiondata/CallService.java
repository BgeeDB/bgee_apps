package org.bgee.model.expressiondata;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.dao.api.DAOManager;
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
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
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
        Set<CallFilter<?>> clonedFilters = Collections.unmodifiableSet(new HashSet<>(callFilters));
        
        
        if (clonedFilters.stream().flatMap(filter -> filter.getCallDataFilters().stream())
                .anyMatch(callData -> Expression.NOT_EXPRESSED.equals(callData.getCallType()) || 
                        EnumSet.allOf(DiffExpression.class).contains(callData.getCallType()))) {
            throw log.throwing(new UnsupportedOperationException(
                    "Management of diff. expression and no-expression queries not yet implemented."));
        }

        Set<Stream<? extends Call<?, ?>>> streamsJoinAnd = new HashSet<>();
        for (CallFilter<?> filter: clonedFilters) { 
            //dispatch CallData per DAO needed. 
            //first, manage queries to ExpressionCallDAO. We extract from filter 
            //the CallDatas with an Expression.EXPRESSED call type, and create a new 
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
                    + "form several AND queries not yet implemented"));
        }
        
        //TODO: here, we need to implement a spliterator avoiding to put all data in memory 
        //to check whether a Call is present in all streams.
        //in the mean time, simply return the stream
        return log.exit(streamsJoinAnd.iterator().next());
    }
    
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
                
                .collect(Collectors.toMap(callData -> 
                        //create a DataPropagation object as key, using only 
                        //PropagationState.SELF and PropagationState.DESCENDANT, 
                        //to determine whether to include substructures/sub-stages
                        new DataPropagation(
                                PropagationState.SELF.equals(
                                        callData.getDataPropagation().getAnatEntityPropagationState())?
                                        PropagationState.SELF: PropagationState.DESCENDANT, 
                                PropagationState.SELF.equals(
                                        callData.getDataPropagation().getDevStagePropagationState())?
                                        PropagationState.SELF: PropagationState.DESCENDANT), 
                        
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
        
        final ExpressionCallDAO dao = this.getDaoManager().getExpressionCallDAO();
        return log.exit(dispatchedCallData.entrySet().stream()
               
                //perform one query to the DAO for each entry. From each of these queries. 
                //we retrieve the Stream returned by DAOResultSet.stream().
                .map(entry -> 
                    dao.getExpressionCalls(
                        //generate a ExpressionCallDAOFilter from callFilter and its ExpressionCallDatas
                        Arrays.asList(new ExpressionCallDAOFilter(null, Arrays.asList(speciesId), 
                            callFilter.getConditionFilters().stream()
                                .map(condFilter -> new DAOConditionFilter(condFilter.getAnatEntitieIds(), 
                                    condFilter.getDevStageIds()))
                                .collect(Collectors.toSet()), 
                            entry.getValue().stream()
                                .flatMap(callData -> this.mapCallDataToCallTOFilters(callData).stream())
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
                        //attributes and ordering attributes
                        null, null
                    )
                    //ultimately, retrieve the Stream<ExpressionCallTO>
                    .stream()
                )
                
                //transform each Stream<ExpressionCallTO> into a Stream<ExpressionCall>
                .map(callTOStream -> callTOStream.map(callTO -> this.mapCallFromTO(callTO)))
                //generates a Set<Stream<ExpressionCall>>.
                .collect(Collectors.toSet()));
    }
    
    private ExpressionCall mapCallFromTO(ExpressionCallTO callTO) {
        log.entry(callTO);
        
        return null;
    }
    
    private Set<ExpressionCallTO> mapCallDataToCallTOFilters(ExpressionCallData callData) {
        log.entry(callData);
        
        return null;
    }
}
