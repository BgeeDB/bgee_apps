package org.bgee.controller;


import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.model.BgeeUtils;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.Call.ExpressionCall.ClusteringMethod;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.ConditionUtils;
import org.bgee.model.expressiondata.baseelements.CallType.Expression;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.bgee.view.GeneDisplay;
import org.bgee.view.ViewFactory;

/**
 * Controller handling requests related to gene pages. 
 * 
 * @author  Philippe Moret
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 13, May 2016
 * @since   Bgee 13, Nov. 2015
 */
public class CommandGene extends CommandParent {

    private final static Logger log = LogManager.getLogger(CommandGene.class.getName());
    
    /**
     * Contains all information necessary to produce a view related to a {@code Gene}.
     * 
     * @author Frederic Bastian
     * @version Bgee 13, June 2016
     * @since   Bgee 13, Jan. 2016
     */
    public static class GeneResponse {
        private final Gene gene;
        private final List<ExpressionCall> exprCalls;
        private final Set<ExpressionCall> redundantExprCalls;
        private final LinkedHashMap<String, List<ExpressionCall>> callsByAnatEntityId;
        private final boolean includingAllRedundantCalls;
        private final Map<ExpressionCall, Integer> clusteringBestEachAnatEntity;
        private final Map<ExpressionCall, Integer> clusteringWithinAnatEntity;
        private final ConditionUtils conditionUtils;
        
        /**
         * @param gene                          See {@link #getGene()}.
         * @param exprCalls                     See {@link #getExprCalls()}.
         * @param redundantExprCalls            See {@link #getRedundantExprCalls()}.
         * @param includingAllRedundantCalls    See {@link #isIncludingAllRedundantCalls()}.
         * @param callsByAnatEntityId           See {@link #getCallsByAnatEntityId()}.
         * @param clusteringBestEachAnatEntity  See {@link #getClusteringBestEachAnatEntity()}.
         * @param clusteringWithinAnatEntity    See {@link #getClusteringWithinAnatEntity()}.
         * @param conditionUtils                See {@link #getConditionUtils()}.
         */
        public GeneResponse(Gene gene, List<ExpressionCall> exprCalls, 
                Set<ExpressionCall> redundantExprCalls, 
                boolean includingAllRedundantCalls, 
                LinkedHashMap<String, List<ExpressionCall>> callsByAnatEntityId, 
                Map<ExpressionCall, Integer> clusteringBestEachAnatEntity, 
                Map<ExpressionCall, Integer> clusteringWithinAnatEntity, 
                ConditionUtils conditionUtils) {
            this.gene = gene;
            this.exprCalls = BgeeUtils.toList(exprCalls);
            this.redundantExprCalls = BgeeUtils.toSet(redundantExprCalls);
            this.includingAllRedundantCalls = includingAllRedundantCalls;
            //too boring to protect the Maps for this internal class...
            this.callsByAnatEntityId = callsByAnatEntityId;
            this.clusteringBestEachAnatEntity = clusteringBestEachAnatEntity;
            this.clusteringWithinAnatEntity = clusteringWithinAnatEntity;
            this.conditionUtils = conditionUtils;
        }

        /**
         * @return  The {@code Gene} which information are requested for. 
         */
        public Gene getGene() {
            return gene;
        }
        /**
         * @return  A {@code List} of {@code ExpressionCall}s, ranked by the normalized global rank 
         *          of the gene in the related {@code Condition}s.
         */
        public List<ExpressionCall> getExprCalls() {
            return exprCalls;
        }
        /**
         * @return  A {@code Set} of {@code ExpressionCall}s for which there exists a more precise call 
         *          (i.e., with a more precise {@code Condition}), at a better rank (i.e., 
         *          with a lower index in the {@code List} returned by {@link #getExprCalls()})
         */
        public Set<ExpressionCall> getRedundantExprCalls() {
            return redundantExprCalls;
        }
        /**
         * @return  A {@code boolean} that is {@code true} if the information returned by 
         *          {@link #getCallsByAnatEntityId}, {@link #getClusteringBestEachAnatEntity()}, 
         *          and {@link #getClusteringWithinAnatEntity()}, were built by including all 
         *          redundant calls (see {@link #getRedundantExprCalls()}), {@code false} otherwise.
         */
        public boolean isIncludingAllRedundantCalls() {
            return includingAllRedundantCalls;
        }
        /**
         * @return  A {@code LinkedHashMap} where keys are {@code String}s corresponding to 
         *          anat. entity IDs, ordered based on the best rank in each anat. entity, 
         *          the associated value being a {@code List} of {@code ExpressionCall}s 
         *          in this anat. entity, ordered by their global mean rank.
         *          Redundant {@code ExpressionCall}s may or may not have been considered, 
         *          depending on {@link #isIncludingRedundantCalls()}.
         * @see #isIncludingRedundantCalls()
         */
        public LinkedHashMap<String, List<ExpressionCall>> getCallsByAnatEntityId() {
            return callsByAnatEntityId;
        }
        /**
         * Returns a clustering of a set of {@code ExpressionCall}s generated by only considering  
         * the best {@code ExpressionCall} from each anatomical entity. 
         * Redundant {@code ExpressionCall}s may or may not have been considered, 
         * depending on {@link #isIncludingRedundantCalls()}.
         * 
         * @return      A {@code Map} where keys are {@code ExpressionCall}s, the associated value 
         *              being the index of the group in which they are clustered, 
         *              based on their expression score. Group indexes are assigned in ascending 
         *              order of expression score, starting from 0. Only one best 
         *              {@code ExpressionCall} per anatomical entity is considered. 
         * @see #isIncludingRedundantCalls()
         */
        public Map<ExpressionCall, Integer> getClusteringBestEachAnatEntity() {
            return clusteringBestEachAnatEntity;
        }
        /**
         * Returns a clustering of {@code ExpressionCall}s clustered independently 
         * for each anatomical entity (so, {@code ExpressionCall}s associated to a same value 
         * in the returned {@code Map} might not be part of a same cluster). 
         * Redundant {@code ExpressionCall}s may or may not have been considered, 
         * depending on {@link #isIncludingRedundantCalls()}. 
         * 
         * @return      A {@code Map} where keys are {@code ExpressionCall}s, the associated value 
         *              being the index of the group in which they are clustered, 
         *              based on their expression score. Group indexes are assigned in ascending 
         *              order of expression score, starting from 0. Clusters are independent 
         *              per anatomical entities. 
         * @see #isIncludingRedundantCalls()
         */
        public Map<ExpressionCall, Integer> getClusteringWithinAnatEntity() {
            return clusteringWithinAnatEntity;
        }
        /**
         * @return  A {@code ConditionUtils} loaded from all {@code Condition}s 
         *          retrieved from the {@code ExpressionCall}s in the {@code List} returned by 
         *          {@link #getExprCalls()}.
         */
        public ConditionUtils getConditionUtils() {
            return conditionUtils;
        }
    }

    /**
     * Constructor
     * 
     * @param response          A {@code HttpServletResponse} that will be used to display the 
     *                          page to the client
     * @param requestParameters The {@code RequestParameters} that handles the parameters of the 
     *                          current request.
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     * @param viewFactory       A {@code ViewFactory} that provides the display type to be used.
     * @param serviceFactory    A {@code ServiceFactory} that provides bgee services.
     */
	public CommandGene(HttpServletResponse response, RequestParameters requestParameters, BgeeProperties prop,
	        ViewFactory viewFactory, ServiceFactory serviceFactory) {
		super(response, requestParameters, prop, viewFactory, serviceFactory);
	}

	@Override
	public void processRequest() throws Exception {
		log.entry();
		GeneDisplay display = viewFactory.getGeneDisplay();
		String geneId = requestParameters.getGeneId();
		if (geneId == null) {
			display.displayGeneHomePage();
		} else {
			display.displayGene(this.buildGeneResponse(geneId));
		}
		log.exit();
	}

	private GeneResponse buildGeneResponse(String geneId) 
	        throws PageNotFoundException, IllegalStateException {
	    log.entry(geneId);

        //**************************************
        // Retrieve Gene
        //**************************************
	    Gene gene = this.getGene(geneId);

        //**************************************
        // Expression calls, ConditionUtils, 
	    // sorting, and redundant calls
        //**************************************
	    List<ExpressionCall> exprCalls = this.getExpressions(gene);
	    
	    if (exprCalls.isEmpty()) {
	        log.debug("No calls for gene {}", geneId);
	         return log.exit(new GeneResponse(gene, exprCalls, new HashSet<>(), true, 
	                 new LinkedHashMap<>(), new HashMap<>(), new HashMap<>(), null));
	    }
        
        ConditionUtils conditionUtils = new ConditionUtils(
                exprCalls.stream().map(ExpressionCall::getCondition).collect(Collectors.toSet()), 
                serviceFactory);
        
        //we need to make sure that the ExpressionCalls are ordered in exactly the same way 
	    //for the display and for the clustering, otherwise the display will be buggy, 
	    //notably for calls with equal ranks. And we need to take into account 
	    //relations between Conditions for filtering them, which would be difficult to achieve
	    //only by a query to the data source. So, we order them anyway. 
        long startFilteringTimeInMs = System.currentTimeMillis();
        Collections.sort(exprCalls, new ExpressionCall.RankComparator(conditionUtils));
        log.debug("Calls sorted in {} ms", System.currentTimeMillis() - startFilteringTimeInMs);
        
        final Set<ExpressionCall> redundantCalls = ExpressionCall.identifyRedundantCalls(
                exprCalls, conditionUtils);
        
        //**************************************
        // Grouping of Calls per anat. entity, 
        // Clustering, Building GeneResponse
        //**************************************
	    return log.exit(this.buildGeneResponse(gene, exprCalls, redundantCalls, true, conditionUtils));
	}
	
	/**
	 * Continue the building of a {@code GeneResponse}, by taking care of the steps 
	 * of grouping of {@code ExpressionCall}s per anatomical entity, and of clustering. 
	 * 
	 * @param gene                     The requested {@code Gene}.
	 * @param exprCalls                A {@code List} of {@code ExpressionCall}s sorted using 
	 *                                 the {@link ExpressionCall.RankComparator}.
	 * @param redundantCalls           A {@code Set} of {@code ExpressionCall}s that are redundant.
	 * @param filterRedundantCalls     A {@code boolean} defining whether redundant calls 
	 *                                 should be filtered for the grouping and clustering steps.
	 * @param conditionUtils           A {@code ConditionUtils} built from {@code exprCalls}.
	 * @return                         A built {@code GeneResponse}.
	 */
	private GeneResponse buildGeneResponse(Gene gene, List<ExpressionCall> exprCalls, 
	        Set<ExpressionCall> redundantCalls, boolean filterRedundantCalls, 
	        ConditionUtils conditionUtils) {
	    log.entry(exprCalls, redundantCalls, filterRedundantCalls, conditionUtils);
	    
	    //*********************
        // Grouping
        //*********************
        long startFilteringTimeInMs = System.currentTimeMillis();
        //first, filter calls and group calls by anat. entity. We need to preserve the order 
        //of the keys, as we have already sorted the calls by their rank. 
        //If filterRedundantCalls is true, we completely discard anat. entities 
        //that have only redundant calls, but if an anat. entity has some non-redundant calls 
        //and is not discarded, we preserve all its calls, even the redundant ones. 
        LinkedHashMap<String, List<ExpressionCall>> callsByAnatEntityId = exprCalls.stream()
                //group by anat. entity
                .collect(Collectors.groupingBy(
                        c -> c.getCondition().getAnatEntityId(), 
                        LinkedHashMap::new, 
                        Collectors.toList()))
                .entrySet().stream()
                //discard if all calls of an anat. entity are redundant
                .filter(entry -> !filterRedundantCalls || !redundantCalls.containsAll(entry.getValue()))
                //reconstruct the LinkedHashMap
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), 
                        (l1, l2) -> {
                            throw log.throwing(new AssertionError("Not possible to have key collision"));
                        }, 
                        LinkedHashMap::new));

        //*********************
        // Clustering
        //*********************
        //define clustering method
        Function<List<ExpressionCall>, Map<ExpressionCall, Integer>> clusteringFunction = 
                getClusteringFunction();
        
        //Store a clustering of ExpressionCalls, by considering only one best ExpressionCall 
        //from each anatomical entity.
        Map<ExpressionCall, Integer> clusteringBestEachAnatEntity = clusteringFunction.apply(
                callsByAnatEntityId.values().stream()
                         //store the best call from each anat. entity 
                        .map(callList -> callList.get(0))
                        //in the order of the sorted List of ExpressionCalls
                        .collect(Collectors.toList()));
        
        //store a clustering, independent for each anatomical entity, of the ExpressionCalls 
        //of an anatomical entity
        Map<ExpressionCall, Integer> clusteringWithinAnatEntity = callsByAnatEntityId.values().stream()
                .flatMap(callList -> clusteringFunction.apply(callList).entrySet().stream())
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
        
        log.debug("Total clustering of calls performed in {} ms", 
                System.currentTimeMillis() - startFilteringTimeInMs);
        
        //*********************
        // Build GeneResponse
        //*********************
        return log.exit(new GeneResponse(gene, exprCalls, redundantCalls, !filterRedundantCalls, 
                callsByAnatEntityId, clusteringBestEachAnatEntity, clusteringWithinAnatEntity, 
                conditionUtils));
	}
	
	/**
	 * Gets the {@code Gene} instance from its id. 
	 * 
	 * @param geneId A {@code String} containing the gene id.
	 * @return       The {@code Gene} instance.
	 * @throws PageNotFoundException   If no {@code Gene} could be found corresponding to {@code geneId}.
	 */
	private Gene getGene(String geneId) throws PageNotFoundException {
	    log.entry(geneId);
		Gene gene = serviceFactory.getGeneService().loadGeneById(geneId);
		if (gene == null) {
		    throw log.throwing(new PageNotFoundException("No gene corresponding to " + geneId));
		}
		return log.exit(gene);
	}
	
	/**
	 * Retrieves the sorted list of {@code ExpressionCall} associated to this gene, 
	 * ordered by global mean rank.
	 * 
	 * @param gene The {@code Gene}
	 * @return     The {@code List} of {@code ExpressionCall} associated to this gene, 
	 *             ordered by global mean rank.
	 */
	private List<ExpressionCall> getExpressions(Gene gene) {
	    log.entry(gene);
	    
		LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = 
	                new LinkedHashMap<>();
		//The ordering is not essential here, because anyway we will need to order calls 
		//with an equal rank, based on the relations between their conditions, which is difficult 
		//to make in a query to the data source. 
	    serviceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.ASC);
	        
		CallService service = serviceFactory.getCallService();
		return log.exit(service.loadExpressionCalls(
		        gene.getSpeciesId(), 
                new ExpressionCallFilter(new GeneFilter(gene.getId()), null, new DataPropagation(), 
                        Arrays.asList(new ExpressionCallData(Expression.EXPRESSED))), 
                EnumSet.of(CallService.Attribute.GENE_ID, CallService.Attribute.ANAT_ENTITY_ID, 
                        CallService.Attribute.DEV_STAGE_ID, CallService.Attribute.CALL_DATA, 
                        CallService.Attribute.GLOBAL_DATA_QUALITY, CallService.Attribute.GLOBAL_RANK), 
                serviceOrdering)
            .collect(Collectors.toList()));
	}
	
	/**
	 * Return the {@code Function} corresponding to the clustering method to used, 
	 * based on the properties {@link BgeeProperties#getGeneScoreClusteringMethod()} 
	 * and {@link BgeeProperties#getGeneScoreClusteringThreshold()}. The {@code Function} 
	 * will trigger a call to {@link ExpressionCall#generateMeanRankScoreClustering(
	 * List, ClusteringMethod, Double)}.
	 * 
	 * @return     A {@code Function} accepting a {@code List} of {@code ExpressionCall}s 
	 *             as input, and returns a {@code Map} corresponding to the clustering as output.
	 * @throws IllegalStateException   If {@link #props} does not provide properties 
	 *                                 allowing to parameterize the clustering function.
	 * @see ExpressionCall#generateMeanRankScoreClustering(List, ClusteringMethod, Double)
	 */
	private Function<List<ExpressionCall>, Map<ExpressionCall, Integer>> getClusteringFunction() 
	        throws IllegalStateException {
	    log.entry();
	    if (this.prop.getGeneScoreClusteringMethod() == null) {
	        throw log.throwing(new IllegalStateException("No clustering method specified."));
	    }
	    //Distance threshold
        if (this.prop.getGeneScoreClusteringThreshold() == null || 
                //we don't want negative nor near-zero values
                this.prop.getGeneScoreClusteringThreshold() < 0.000001) {
            throw log.throwing(new IllegalStateException("A clustering method was specified, "
                    + "but no distance threshold or incorrect threshold value assigned."));
        }
        try {
            //find clustering method
            final ClusteringMethod method = ClusteringMethod.valueOf(
                    this.prop.getGeneScoreClusteringMethod().trim());
            
            //define clustering function
            log.debug("Using clustering method {} with distance threshold {}", method, 
                    this.prop.getGeneScoreClusteringThreshold());
            return log.exit(
                    callList -> ExpressionCall.generateMeanRankScoreClustering(callList, method, 
                            this.prop.getGeneScoreClusteringThreshold()));
        } catch (IllegalArgumentException e) {
            throw log.throwing(new IllegalStateException("No custering method corresponding to "
                    + this.prop.getGeneScoreClusteringMethod().trim()));
        }
	}
}
