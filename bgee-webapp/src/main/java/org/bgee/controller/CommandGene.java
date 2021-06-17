package org.bgee.controller;


import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.model.ServiceFactory;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.Call.ExpressionCall.ClusteringMethod;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.gene.GeneHomologs;
import org.bgee.model.gene.GeneMatchResult;
import org.bgee.view.GeneDisplay;
import org.bgee.view.ViewFactory;

/**
 * Controller handling requests related to gene pages. 
 * 
 * @author  Philippe Moret
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @author  Julien Wollbrett
 * @version Bgee 15.0, Jun. 2021
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
        private final List<ExpressionCall> calls;
        private final EnumSet<CallService.Attribute> condParams;
        private final GeneHomologs geneHomologs;
        private final boolean includingAllRedundantCalls;
        private final Map<ExpressionCall, Integer> clustering;
        
        /**
         * @param gene                          See {@link #getGene()}.
         * @param includingAllRedundantCalls    See {@link #isIncludingAllRedundantCalls()}.
         * @param calls                         See {@link #getCalls()}
         * @param condParams                    See {@link #getCondParams()}
         * @param clustering                    See {@link #getClustering()}.
         */
        public GeneResponse(Gene gene, boolean includingAllRedundantCalls, 
                List<ExpressionCall> calls, EnumSet<CallService.Attribute> condParams,
                Map<ExpressionCall, Integer> clustering,
                GeneHomologs geneHomologs) {
            this.gene = gene;
            this.includingAllRedundantCalls = includingAllRedundantCalls;
            this.calls = calls;
            this.condParams = condParams;
            this.clustering = clustering;
            this.geneHomologs = geneHomologs;
        }

        /**
         * @return  The {@code Gene} which information are requested for. 
         */
        public Gene getGene() {
            return gene;
        }
        /**
         * @return  A {@code boolean} that is {@code true} if the information returned by 
         *          {@link #getCalls()}, {@link #getClusteringBestEachAnatEntity()}, 
         *          and {@link #getClusteringWithinAnatEntity()}, were built by including all 
         *          redundant calls (see {@link #getRedundantExprCalls()}), {@code false} otherwise.
         */
        public boolean isIncludingAllRedundantCalls() {
            return includingAllRedundantCalls;
        }
        /**
         * @return  A {@code List} of {@code ExpressionCall}s for the requested condition parameters
         *          (see #getRequestedCondParams()), ordered by their global mean rank and
         *          most precise condition for equal ranks.
         *          Redundant {@code ExpressionCall}s may or may not have been considered, 
         *          depending on {@link #isIncludingAllRedundantCalls()}.
         * @see #isIncludingAllRedundantCalls()
         */
        public List<ExpressionCall> getCalls() {
            return calls;
        }
        /**
         * @return  An {@code EnumSet} containing the condition parameters (see
         *          {@link CallService.Attribute#isConditionParameter()}) requested to retrieve
         *          the calls returned by {@link #getCalls()}.
         */
        public EnumSet<CallService.Attribute> getCondParams() {
            return this.condParams;
        }
        /**
         * Returns a clustering of the {@code ExpressionCall}s returned by {@link #getCalls()}.
         * 
         * @return      A {@code Map} where keys are {@code ExpressionCall}s, the associated value 
         *              being the index of the group in which they are clustered, 
         *              based on their expression score. Group indexes are assigned in ascending 
         *              order of expression score, starting from 0.
         * @see #getCalls()
         */
        public Map<ExpressionCall, Integer> getClustering() {
            return clustering;
        }

        /**
         * @return  The {@code GeneHomologs} of the gene which information are requested for. 
         */
        public GeneHomologs getGeneHomologs() {
            return geneHomologs;
        }
    }

    /**
     * Constructor
     * 
     * @param response                  A {@code HttpServletResponse} that will be used to display the 
     *                                  page to the client
     * @param requestParameters         The {@code RequestParameters} that handles the parameters of the 
     *                                  current request.
     * @param prop                      A {@code BgeeProperties} instance that contains the properties
     *                                  to use.
     * @param viewFactory               A {@code ViewFactory} that provides the display type to be used.
     * @param serviceFactory            A {@code ServiceFactory} that provides bgee services.
     */
    public CommandGene(HttpServletResponse response, RequestParameters requestParameters,
                       BgeeProperties prop, ViewFactory viewFactory, ServiceFactory serviceFactory) {
        super(response, requestParameters, prop, viewFactory, serviceFactory);
    }

    @Override
    public void processRequest() throws Exception {
        log.traceEntry();
        GeneDisplay display = viewFactory.getGeneDisplay();
        String geneId = requestParameters.getGeneId();
        Integer speciesId = requestParameters.getSpeciesId();
        String search = requestParameters.getQuery();

        if (StringUtils.isNotBlank(search)) {
            GeneMatchResult result = serviceFactory.getGeneMatchResultService(this.prop)
                    .searchByTerm(search, null, 0, 1000);
            display.displayGeneSearchResult(search, result);
            log.traceExit(); return;
        }

        if (geneId == null) {
            display.displayGeneHomePage();
            log.traceExit(); return;
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
            log.traceExit(); return;
        }

        Gene selectedGene = null;
        if (genes.size() == 1) {
            selectedGene = genes.iterator().next();
        } else if (genes.size() > 1)  {
            //if several gene IDs match, then we need to get the speciesId information,
            //otherwise we need to let the user choose the species he/she wants
            if (speciesId == null || speciesId <= 0) {
                display.displayGeneChoice(genes);
                log.traceExit(); return;
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

        URLParameters urlParameters = requestParameters.getUrlParametersInstance();
        Set<String> selectedCondParams = new HashSet<>(
                Optional.ofNullable(requestParameters.getValues(urlParameters.getCondParam()))
                .orElseGet(() -> Collections.emptyList()));

        display.displayGene(this.buildGeneResponse(selectedGene, selectedCondParams));
        log.traceExit();
    }

    private GeneResponse buildGeneResponse(Gene gene, Set<String> condParams) 
            throws IllegalStateException {
        log.traceEntry("{}, {}", gene, condParams);
        //retrieve calls with silver quality for the requested condition parameters.
        EnumSet<CallService.Attribute> condParamAttrs = condParams == null || condParams.isEmpty()?
                //default value
                EnumSet.of(CallService.Attribute.ANAT_ENTITY_ID, CallService.Attribute.CELL_TYPE_ID):
                //otherwise retrieve condition parameters from request
                CallService.Attribute.getAllConditionParameters()
                    .stream().filter(a -> condParams.contains(a.getCondParamName()))
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(CallService.Attribute.class)));

        List<ExpressionCall> calls = serviceFactory.getCallService().loadSilverCondObservedCalls(
                new GeneFilter(gene.getSpecies().getId(), gene.getEnsemblGeneId()), condParamAttrs);
        
        // Load homology information. As we decided to only show in species paralogs in the gene
        // page, we do not use same filters to retrieve orthologs and paralogs. That is why we 
        // first create one GeneHomologs object containing only paralogs and one GeneHomologs 
        // object containing only orthologs.
        GeneHomologs geneOrthologs = serviceFactory.getGeneHomologsService()
                .getGeneHomologs(gene.getEnsemblGeneId(), gene.getSpecies().getId(), 
                        true, false);
        GeneHomologs geneParalogs = serviceFactory.getGeneHomologsService()
                .getGeneHomologs(gene.getEnsemblGeneId(), gene.getSpecies().getId(), 
                        Collections.singleton(gene.getSpecies().getId()), null, true, 
                        false, true);
        // generate one unique GeneHomologs object containing both paralogs and orthologs 
        // retrieved using different filters
        GeneHomologs geneHomologs = GeneHomologs.mergeGeneHomologs(geneOrthologs, geneParalogs);
        
        if (calls.isEmpty()) {
            log.debug("No calls for gene {}", gene.getEnsemblGeneId());
            return log.traceExit(new GeneResponse(gene, true, calls, condParamAttrs,
                    new HashMap<>(), geneHomologs));
        }
        
        //**************************************
        // Clustering, Building GeneResponse
        //**************************************
        long startFilteringTimeInMs = System.currentTimeMillis();
        //define clustering method
        Function<List<ExpressionCall>, Map<ExpressionCall, Integer>> clusteringFunction = 
                getClusteringFunction();
        //Store a clustering of ExpressionCalls
        Map<ExpressionCall, Integer> clustering = clusteringFunction.apply(calls);
        log.debug("Total clustering of calls performed in {} ms", 
                System.currentTimeMillis() - startFilteringTimeInMs);

        return log.traceExit(new GeneResponse(gene, true, calls, condParamAttrs,
                clustering, geneHomologs));
    }
    
    /**
     * Return the {@code Function} corresponding to the clustering method to used, 
     * based on the properties {@link BgeeProperties#getGeneScoreClusteringMethod()} 
     * and {@link BgeeProperties#getGeneScoreClusteringThreshold()}. The {@code Function}
     * will trigger a call to {@link ExpressionCall#generateMeanRankScoreClustering(
     * List, ClusteringMethod, double)}.
     * 
     * @return     A {@code Function} accepting a {@code List} of {@code ExpressionCall}s 
     *             as input, and returns a {@code Map} corresponding to the clustering as output.
     * @throws IllegalStateException   If {@link #prop} does not provide properties 
     *                                 allowing to parameterize the clustering function.
     * @see ExpressionCall#generateMeanRankScoreClustering(List, ClusteringMethod, double)
     */
    private Function<List<ExpressionCall>, Map<ExpressionCall, Integer>> getClusteringFunction() 
            throws IllegalStateException {
        log.traceEntry();
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
            return log.traceExit(
                    callList -> ExpressionCall.generateMeanRankScoreClustering(callList, method, 
                            this.prop.getGeneScoreClusteringThreshold()));
        } catch (IllegalArgumentException e) {
            throw log.throwing(new IllegalStateException("No clustering method corresponding to "
                    + this.prop.getGeneScoreClusteringMethod().trim()));
        }
    }
}
