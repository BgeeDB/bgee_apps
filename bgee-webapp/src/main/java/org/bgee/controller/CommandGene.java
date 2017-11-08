package org.bgee.controller;


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
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.Call.ExpressionCall.ClusteringMethod;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.ConditionGraph;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
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
 * @version Bgee 14, May 2017
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
        private final LinkedHashMap<AnatEntity, List<ExpressionCall>> callsByAnatEntity;
        private final boolean includingAllRedundantCalls;
        private final Map<ExpressionCall, Integer> clusteringBestEachAnatEntity;
        private final Map<ExpressionCall, Integer> clusteringWithinAnatEntity;
        private final ConditionGraph conditionGraph;
        
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
                LinkedHashMap<AnatEntity, List<ExpressionCall>> callsByAnatEntity, 
                Map<ExpressionCall, Integer> clusteringBestEachAnatEntity, 
                Map<ExpressionCall, Integer> clusteringWithinAnatEntity, 
                ConditionGraph conditionGraph) {
            this.gene = gene;
            this.exprCalls = BgeeUtils.toList(exprCalls);
            this.redundantExprCalls = BgeeUtils.toSet(redundantExprCalls);
            this.includingAllRedundantCalls = includingAllRedundantCalls;
            //too boring to protect the Maps for this internal class...
            this.callsByAnatEntity = callsByAnatEntity;
            this.clusteringBestEachAnatEntity = clusteringBestEachAnatEntity;
            this.clusteringWithinAnatEntity = clusteringWithinAnatEntity;
            this.conditionGraph = conditionGraph;
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
         * @return  A {@code LinkedHashMap} where keys are {@code AnatEntity}s corresponding to 
         *          anat. entity, ordered based on the best rank in each anat. entity, 
         *          the associated value being a {@code List} of {@code ExpressionCall}s 
         *          in this anat. entity, ordered by their global mean rank.
         *          Redundant {@code ExpressionCall}s may or may not have been considered, 
         *          depending on {@link #isIncludingAllRedundantCalls()}.
         * @see #isIncludingAllRedundantCalls()
         */
        public LinkedHashMap<AnatEntity, List<ExpressionCall>> getCallsByAnatEntity() {
            return callsByAnatEntity;
        }
        /**
         * Returns a clustering of a set of {@code ExpressionCall}s generated by only considering  
         * the best {@code ExpressionCall} from each anatomical entity. 
         * Redundant {@code ExpressionCall}s may or may not have been considered, 
         * depending on {@link #isIncludingAllRedundantCalls()}.
         * 
         * @return      A {@code Map} where keys are {@code ExpressionCall}s, the associated value 
         *              being the index of the group in which they are clustered, 
         *              based on their expression score. Group indexes are assigned in ascending 
         *              order of expression score, starting from 0. Only one best 
         *              {@code ExpressionCall} per anatomical entity is considered. 
         * @see #isIncludingAllRedundantCalls()
         */
        public Map<ExpressionCall, Integer> getClusteringBestEachAnatEntity() {
            return clusteringBestEachAnatEntity;
        }
        /**
         * Returns a clustering of {@code ExpressionCall}s clustered independently 
         * for each anatomical entity (so, {@code ExpressionCall}s associated to a same value 
         * in the returned {@code Map} might not be part of a same cluster). 
         * Redundant {@code ExpressionCall}s may or may not have been considered, 
         * depending on {@link #isIncludingAllRedundantCalls()}. 
         * 
         * @return      A {@code Map} where keys are {@code ExpressionCall}s, the associated value 
         *              being the index of the group in which they are clustered, 
         *              based on their expression score. Group indexes are assigned in ascending 
         *              order of expression score, starting from 0. Clusters are independent 
         *              per anatomical entities. 
         * @see #isIncludingAllRedundantCalls()
         */
        public Map<ExpressionCall, Integer> getClusteringWithinAnatEntity() {
            return clusteringWithinAnatEntity;
        }
        /**
         * @return  A {@code ConditionUtils} loaded from all {@code Condition}s 
         *          retrieved from the {@code ExpressionCall}s in the {@code List} returned by 
         *          {@link #getExprCalls()}.
         */
        public ConditionGraph getConditionGraph() {
            return conditionGraph;
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
        Integer speciesId = requestParameters.getSpeciesId();

        if (geneId == null) {
            display.displayGeneHomePage();
            log.exit(); return;
        }

        // NOTE: we retrieve genes after the sanity check on geneId to avoid to throw an exception
        Set<Gene> genes = serviceFactory.getGeneService().loadGenesByEnsemblId(geneId, true);
        if (genes.size() == 0) {
            throw log.throwing(new PageNotFoundException("No gene corresponding to " + geneId));
        }

        if (genes.size() == 1 && speciesId != null) {
            //we want to avoid the use of 'species_id' parameter in URL if not necessary,
            //so if an Ensembl ID has an unique hit in Bgee, and there is a 'species_id'
            //in the URL, then we redirect to a page without 'species_id' in the URL,
            //for nicer URLs, and to avoid duplicated content.
            // XXX: we do not check that the user gives a bad species?
            RequestParameters url = new RequestParameters(
                    this.requestParameters.getUrlParametersInstance(), this.prop, false, "&");
            url.setPage(RequestParameters.PAGE_GENE);
            url.setGeneId(genes.iterator().next().getEnsemblGeneId());
            this.response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            this.response.addHeader("Location", url.getRequestURL());
            log.exit(); return;
        }

        Gene selectedGene = null;
        if (genes.size() == 1) {
            selectedGene = genes.iterator().next();
        } else if (genes.size() > 1)  {
            //if several gene IDs match, then we need to get the speciesId information,
            //otherwise we need to let the user choose the species he/she wants
            if (speciesId == null || speciesId <= 0) {
                display.displayGeneChoice(genes);
                log.exit(); return;
            }
            Set<Gene> speciesGenes = genes.stream()
                    .filter(g -> g.getSpecies().getId().equals(speciesId))
                    .collect(Collectors.toSet());
            if (speciesGenes.size() != 1) {
                throw log.throwing(new PageNotFoundException("No gene corresponding to "
                        + geneId + " for species " + speciesId));
            }
            selectedGene = speciesGenes.iterator().next();
        } else {
            throw log.throwing(new AssertionError("Impossible case"));
        }

        display.displayGene(this.buildGeneResponse(selectedGene));
        log.exit();
    }

    private GeneResponse buildGeneResponse(Gene gene) throws IllegalStateException {
        log.entry(gene);

        //**************************************
        // Expression calls, ConditionUtils, 
        // sorting, and redundant calls
        //**************************************
        List<ExpressionCall> organCalls = this.getAnatEntitySilverExpressionCalls(gene);
        if (organCalls.isEmpty()) {
            log.debug("No calls for gene {}", gene.getEnsemblGeneId());
             return log.exit(new GeneResponse(gene, organCalls, new HashSet<>(), true, 
                     new LinkedHashMap<>(), new HashMap<>(), new HashMap<>(), null));
        }
        final Set<String> organIds = organCalls.stream()
                .map(c ->c .getCondition().getAnatEntityId())
                .collect(Collectors.toSet());
        
        final List<ExpressionCall> organStageCalls = this.getAnatEntityDevStageBronzeExpressionCalls(gene);
        //XXX: why don't we provided the organIds to perform the SQL query, instead of filtering afterwards?
        List<ExpressionCall> orderedCalls = organStageCalls.stream()
                .filter(c -> organIds.contains(c.getCondition().getAnatEntityId()))
                .collect(Collectors.toList());
        if (orderedCalls.isEmpty()) {
            log.debug("No calls for gene {}", gene.getEnsemblGeneId());
            //XXX: So, organCalls is needed only to determine the organ calls with a at least silver quality?
             return log.exit(new GeneResponse(gene, orderedCalls, new HashSet<>(), true,
                     new LinkedHashMap<>(), new HashMap<>(), new HashMap<>(), null));
        }

        //we need to make sure that the ExpressionCalls are ordered in exactly the same way
        //for the display and for the clustering, otherwise the display will be buggy,
        //notably for calls with equal ranks. And we need to take into account
        //relations between Conditions for filtering them, which would be difficult to achieve
        //only by a query to the data source. So, we order them anyway.
        //ORGAN-STAGE
        ConditionGraph organStageGraph = new ConditionGraph(
                orderedCalls.stream().map(ExpressionCall::getCondition).collect(Collectors.toSet()), 
                serviceFactory);
        Collections.sort(orderedCalls, new ExpressionCall.RankComparator(organStageGraph));
        //ORGAN
        //We need the ConditionGraph for sorting the calls. Creating the AnatEntityOntology for this graph is costly,
        //so we re-use the AnatEntityOntology already produced for the organStageGraph
        ConditionGraph organGraph = new ConditionGraph(
                organCalls.stream().map(ExpressionCall::getCondition).collect(Collectors.toSet()),
                organStageGraph.getAnatEntityOntology(), null);
        Collections.sort(organCalls, new ExpressionCall.RankComparator(organGraph));
        //REDUNDANT ORGAN-STAGE CALLS
        final Set<ExpressionCall> redundantCalls = ExpressionCall.identifyRedundantCalls(
                orderedCalls, organStageGraph);

        //**************************************
        // Grouping of Calls per anat. entity, 
        // Clustering, Building GeneResponse
        //**************************************
        return log.exit(this.buildGeneResponse(gene, orderedCalls, redundantCalls, true, organStageGraph));
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
            ConditionGraph conditionGraph) {
        log.entry(exprCalls, redundantCalls, filterRedundantCalls, conditionGraph);
        
        //*********************
        // Grouping
        //*********************
        long startFilteringTimeInMs = System.currentTimeMillis();
        //first, filter calls and group calls by anat. entity. We need to preserve the order 
        //of the keys, as we have already sorted the calls by their rank. 
        //If filterRedundantCalls is true, we completely discard anat. entities 
        //that have only redundant calls, but if an anat. entity has some non-redundant calls 
        //and is not discarded, we preserve all its calls, even the redundant ones. 
        LinkedHashMap<AnatEntity, List<ExpressionCall>> callsByAnatEntity = exprCalls.stream()
                //group by anat. entity
                .collect(Collectors.groupingBy(
                        c -> c.getCondition().getAnatEntity(), 
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
                callsByAnatEntity.values().stream()
                         //store the best call from each anat. entity 
                        .map(callList -> callList.get(0))
                        //in the order of the sorted List of ExpressionCalls
                        .collect(Collectors.toList()));
        
        //store a clustering, independent for each anatomical entity, of the ExpressionCalls 
        //of an anatomical entity
        Map<ExpressionCall, Integer> clusteringWithinAnatEntity = callsByAnatEntity.values().stream()
                .flatMap(callList -> clusteringFunction.apply(callList).entrySet().stream())
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
        
        log.debug("Total clustering of calls performed in {} ms", 
                System.currentTimeMillis() - startFilteringTimeInMs);
        
        //*********************
        // Build GeneResponse
        //*********************
        return log.exit(new GeneResponse(gene, exprCalls, redundantCalls, !filterRedundantCalls, 
                callsByAnatEntity, clusteringBestEachAnatEntity, clusteringWithinAnatEntity, 
                conditionGraph));
    }
    
    /**
     * Retrieves the sorted list of {@code ExpressionCall} associated to this gene, 
     * ordered by global mean rank.
     * 
     * @param gene The {@code Gene}
     * @return     The {@code List} of {@code ExpressionCall} associated to this gene, 
     *             ordered by global mean rank.
     */
    private List<ExpressionCall> getAnatEntitySilverExpressionCalls(Gene gene) {
        log.entry(gene);

        CallService service = serviceFactory.getCallService();

        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = 
                new LinkedHashMap<>();
        //The ordering is not essential here, because anyway we will need to order calls 
        //with an equal rank, based on the relations between their conditions, which is difficult 
        //to make in a query to the data source.
        //XXX: test if there is a performance difference if we don't use the order by
        serviceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.ASC);

        Map<SummaryCallType.ExpressionSummary, SummaryQuality> silverExpressedCallFilter = new HashMap<>();
        silverExpressedCallFilter.put(ExpressionSummary.EXPRESSED, SummaryQuality.SILVER);
        final List<ExpressionCall> calls = service.loadExpressionCalls(
                new ExpressionCallFilter(silverExpressedCallFilter,
                        Collections.singleton(new GeneFilter(gene.getSpecies().getId(), gene.getEnsemblGeneId())),
                        null, null, true, true, null),
                EnumSet.of(CallService.Attribute.GENE, CallService.Attribute.ANAT_ENTITY_ID,
                           CallService.Attribute.GLOBAL_MEAN_RANK),
                serviceOrdering)
            .collect(Collectors.toList());

        return log.exit(calls);
    }
    
    /**
     * Retrieves the sorted list of {@code ExpressionCall} associated to this gene, 
     * ordered by global mean rank.
     * 
     * @param gene The {@code Gene}
     * @return     The {@code List} of {@code ExpressionCall} associated to this gene, 
     *             ordered by global mean rank.
     */
    private List<ExpressionCall> getAnatEntityDevStageBronzeExpressionCalls(Gene gene) {
        log.entry(gene);
        
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = 
                    new LinkedHashMap<>();
        //The ordering is not essential here, because anyway we will need to order calls 
        //with an equal rank, based on the relations between their conditions, which is difficult 
        //to make in a query to the data source.
        //XXX: test if there is a performance difference if we don't use the order by
        serviceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.ASC);
            
        CallService service = serviceFactory.getCallService();
        
        Map<SummaryCallType.ExpressionSummary, SummaryQuality> summaryCallTypeQualityFilter = new HashMap<>();
        summaryCallTypeQualityFilter.put(ExpressionSummary.EXPRESSED, SummaryQuality.BRONZE);
        return log.exit(service.loadExpressionCalls(
                new ExpressionCallFilter(summaryCallTypeQualityFilter,
                        Collections.singleton(new GeneFilter(gene.getSpecies().getId(), gene.getEnsemblGeneId())),
                        null, null, true, true, true),
                EnumSet.of(CallService.Attribute.GENE, CallService.Attribute.ANAT_ENTITY_ID,
                        //XXX: why do we need the CALL_TYPE here, since we requested only EXPRESSED calls?
                        CallService.Attribute.DEV_STAGE_ID, CallService.Attribute.CALL_TYPE,
                        //XXX: do we need DATA_QUALITY?
                        CallService.Attribute.DATA_QUALITY, CallService.Attribute.GLOBAL_MEAN_RANK,
                        //XXX: experiment counts to display them on the page? No cost in performances?
                        CallService.Attribute.EXPERIMENT_COUNTS),
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
