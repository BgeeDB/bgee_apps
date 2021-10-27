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
import org.bgee.controller.exception.InvalidRequestException;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.model.ServiceFactory;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.Call.ExpressionCall.ClusteringMethod;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.gene.GeneHomologs;
import org.bgee.model.gene.GeneHomologsService;
import org.bgee.model.gene.GeneMatchResult;
import org.bgee.model.gene.GeneMatchResultService;
import org.bgee.model.gene.GeneNotFoundException;
import org.bgee.model.gene.GeneService;
import org.bgee.view.GeneDisplay;
import org.bgee.view.ViewFactory;

/**
 * Controller handling requests related to gene pages. 
 * 
 * @author  Philippe Moret
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @author  Julien Wollbrett
 * @version Bgee 15.0, Oct. 2021
 * @since   Bgee 13, Nov. 2015
 */
public class CommandGene extends CommandParent {

    private final static Logger log = LogManager.getLogger(CommandGene.class.getName());

    public static class GeneExpressionResponse {
        //Deactivated as long as we don't retrieve the Gene when there is no expression data
//        private final Gene gene;
        private final List<ExpressionCall> calls;
        private final EnumSet<CallService.Attribute> condParams;
        private final boolean includingAllRedundantCalls;
        private final Map<ExpressionCall, Integer> clustering;
        
        /**
         * @param calls                         See {@link #getCalls()}
         * @param condParams                    See {@link #getCondParams()}
         * @param includingAllRedundantCalls    See {@link #isIncludingAllRedundantCalls()}.
         * @param clustering                    See {@link #getClustering()}.
         */
        public GeneExpressionResponse(List<ExpressionCall> calls, EnumSet<CallService.Attribute> condParams,
                boolean includingAllRedundantCalls, Map<ExpressionCall, Integer> clustering) {
            this.includingAllRedundantCalls = includingAllRedundantCalls;
            this.calls = calls;
            this.condParams = condParams;
            this.clustering = clustering;
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
         * @return  A {@code boolean} that is {@code true} if the information returned by
         *          {@link #getCalls()}, {@link #getClusteringBestEachAnatEntity()},
         *          and {@link #getClusteringWithinAnatEntity()}, were built by including all
         *          redundant calls (see {@link #getRedundantExprCalls()}), {@code false} otherwise.
         */
        public boolean isIncludingAllRedundantCalls() {
            return includingAllRedundantCalls;
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
    }
    /**
     * Contains all information necessary to produce a view related to a {@code Gene}.
     * 
     * @author Frederic Bastian
     * @version Bgee 15, Oct. 2021
     * @since   Bgee 13, Jan. 2016
     */
    //TODO: to delete once the new website will be used
    public static class GeneResponse extends GeneExpressionResponse {
        private final Gene gene;
        private final GeneHomologs geneHomologs;
        
        /**
         * @param gene                          See {@link #getGene()}.
         * @param includingAllRedundantCalls    See {@link #isIncludingAllRedundantCalls()}.
         * @param calls                         See {@link #getCalls()}
         * @param condParams                    See {@link #getCondParams()}
         * @param clustering                    See {@link #getClustering()}.
         * @param geneHomologs                  See {@link #getGeneHomologs()}.
         */
        public GeneResponse(Gene gene, boolean includingAllRedundantCalls, 
                List<ExpressionCall> calls, EnumSet<CallService.Attribute> condParams,
                Map<ExpressionCall, Integer> clustering,
                GeneHomologs geneHomologs) {
            super(calls, condParams, includingAllRedundantCalls, clustering);
            this.gene = gene;
            this.geneHomologs = geneHomologs;
        }

        /**
         * @return  The {@code Gene} which information are requested for. 
         */
        public Gene getGene() {
            return gene;
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
        String action = requestParameters.getAction();
        GeneService geneService = serviceFactory.getGeneService();
        GeneHomologsService geneHomologsService = serviceFactory.getGeneHomologsService();
        CallService callService = serviceFactory.getCallService();

        if (StringUtils.isNotBlank(search)) {
            GeneMatchResult result = serviceFactory.getGeneMatchResultService(this.prop)
                    .searchByTerm(search, null, 0, GeneMatchResultService.SPHINX_MAX_RESULTS);
            display.displayGeneSearchResult(search, result);
            log.traceExit(); return;
        } else if (geneId == null) {
            display.displayGeneHomePage();
            log.traceExit(); return;
        } else if (RequestParameters.ACTION_GENE_GENERAL_INFO.equals(action)) {
            this.processGeneralInfoRequest(geneService, display);
            log.traceExit(); return;
        } else if (RequestParameters.ACTION_GENE_HOMOLOGS.equals(action)) {
            this.processHomologsRequest(geneService, geneHomologsService, display);
            log.traceExit(); return;
        } else if (RequestParameters.ACTION_GENE_XREFS.equals(action)) {
            this.processXRefsRequest(geneService, display);
            log.traceExit(); return;
        } else if (RequestParameters.ACTION_GENE_EXPRESSION.equals(action)) {
            this.processExpressionRequest(callService, display);
            log.traceExit(); return;
        }

        // NOTE: we retrieve genes after the sanity check on geneId to avoid to throw an exception
        Set<Gene> genes = geneService.loadGenesById(geneId, true, true, true);
        if (genes.size() == 0) {
            throw log.throwing(new PageNotFoundException("No gene corresponding to " + geneId));
        }

        if (genes.size() == 1 && speciesId != null) {
            //we want to avoid the use of 'species_id' parameter in URL if not necessary,
            //so if an ID has an unique hit in Bgee, and there is a 'species_id'
            //in the URL, then we redirect to a page without 'species_id' in the URL,
            //for nicer URLs, and to avoid duplicated content.
            // XXX: we do not check that the user gives a bad species?
            RequestParameters url = new RequestParameters(
                    this.requestParameters.getUrlParametersInstance(), this.prop, false, "&");
            url.setPage(RequestParameters.PAGE_GENE);
            url.setGeneId(genes.iterator().next().getGeneId());
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

    /**
     * Process the request for general information about a gene ID. The info for several genes
     * can be displayed if the gene ID exists in several species (since in Bgee we sometimes use
     * the genome of a closely related species for a species with no genome, a gene ID can exist
     * in several species). It is mandatory to provide a gene ID in the request
     * (see {@link #requestParameters}), and a species ID is optional to target a single gene
     * in case of ambiguity.
     *
     * @param geneService
     * @param display
     * @throws InvalidRequestException
     * @throws PageNotFoundException
     */
    private void processGeneralInfoRequest(GeneService geneService, GeneDisplay display)
            throws InvalidRequestException, PageNotFoundException {
        log.traceEntry("{}, {}", geneService, display);
        String geneId = requestParameters.getGeneId();
        Integer speciesId = requestParameters.getSpeciesId();

        Set<Gene> genes = loadGenes(geneService, geneId, speciesId, false, true, false);
        assert genes != null && !genes.isEmpty();
        display.displayGeneGeneralInformation(genes);
        log.traceExit();
    }

    /**
     * Process the request for homology information for a gene. It is mandatory to provide
     * a gene ID and a species ID in the request (see {@link #requestParameters}),
     * since in Bgee we sometimes use the genome of a closely related species
     * for a species with no genome, a gene ID can exist in several species.
     *
     * @param geneService
     * @param homologsService
     * @param display
     * @throws InvalidRequestException
     * @throws PageNotFoundException
     */
    private void processHomologsRequest(GeneService geneService, GeneHomologsService homologsService,
            GeneDisplay display) throws InvalidRequestException, PageNotFoundException {
        log.traceEntry("{}, {}, {}", geneService, homologsService, display);
        String geneId = requestParameters.getGeneId();
        Integer speciesId = requestParameters.getSpeciesId();

        //Other sanity checks will be performed by the method loadHomologs
        if (speciesId == null || speciesId < 1) {
            throw log.throwing(new InvalidRequestException("Invalid species ID argument: " + speciesId));
        }
        GeneHomologs homologs = loadHomologs(geneId, speciesId, homologsService);
        display.displayGeneHomologs(homologs);
        log.traceExit();
    }

    /**
     * Process the request for XRef information for a gene. It is mandatory to provide
     * a gene ID and a species ID in the request (see {@link #requestParameters}),
     * since in Bgee we sometimes use the genome of a closely related species
     * for a species with no genome, a gene ID can exist in several species.
     *
     * @param geneService
     * @param display
     * @throws InvalidRequestException
     * @throws PageNotFoundException
     */
    private void processXRefsRequest(GeneService geneService, GeneDisplay display)
            throws InvalidRequestException, PageNotFoundException {
        log.traceEntry("{}, {}", geneService, display);
        String geneId = requestParameters.getGeneId();
        Integer speciesId = requestParameters.getSpeciesId();

        //Other sanity checks will be performed by the method loadGenes
        if (speciesId == null || speciesId < 1) {
            throw log.throwing(new InvalidRequestException("Invalid species ID argument: " + speciesId));
        }
        Set<Gene> genes = loadGenes(geneService, geneId, speciesId, false, false, true);
        assert genes != null && genes.size() == 1;
        display.displayGeneXRefs(genes.iterator().next());

        log.traceExit();
    }

    /**
     * Process the request for expression information for a gene. It is mandatory to provide
     * a gene ID and a species ID in the request (see {@link #requestParameters}),
     * since in Bgee we sometimes use the genome of a closely related species
     * for a species with no genome, a gene ID can exist in several species.
     *
     * @param callService
     * @param display
     * @throws InvalidRequestException
     * @throws PageNotFoundException
     */
    private void processExpressionRequest(CallService callService, GeneDisplay display)
            throws InvalidRequestException, PageNotFoundException {
        log.traceEntry("{}, {}", callService, display);
        String geneId = requestParameters.getGeneId();
        Integer speciesId = requestParameters.getSpeciesId();
        URLParameters urlParameters = requestParameters.getUrlParametersInstance();
        Set<String> selectedCondParams = new HashSet<>(
                Optional.ofNullable(requestParameters.getValues(urlParameters.getCondParam()))
                .orElseGet(() -> Collections.emptyList()));

        //Other sanity checks will be performed by the method loadGenes
        if (speciesId == null || speciesId < 1) {
            throw log.throwing(new InvalidRequestException("Invalid species ID argument: " + speciesId));
        }
        GeneExpressionResponse exprResponse = loadExpression(geneId, speciesId, selectedCondParams,
                callService, getClusteringFunction());
        display.displayGeneExpression(exprResponse);

        log.traceExit();
    }

    /**
     * Load {@code Gene}s from a gene ID and potentially with species ID information as well.
     * Several {@code Gene}s can be returned when no species ID is provided, since in Bgee
     * we sometimes use the genome of a closely related species from species with no genome,
     * leading to have a same gene ID in different species.
     *
     * @param geneService           The {@code GeneService} to retrieve {@code Gene}s.
     * @param geneId                A {@code String} that is the requested gene ID. Cannot be blank.
     * @param speciesId             A {@code Integer} that is the requested species ID. Can be {@code null}
     *                              but if non-null it must be greater than 0.
     * @param withSpeciesSourceInfo A {@code boolean}s defining whether data sources of the species
     *                              is retrieved or not.
     * @param withSynonymInfo       A {@code boolean} defining whether synonyms of the genes are retrieved.
     * @param withXRefInfo          A {@code boolean} defining whether XRefs of the genes are retrieved.
     * @return                      A {@code Set} containing the matching {@code Gene}s.
     * @throws InvalidRequestException  If {@code geneId} is blank or {@code speciesId} is non-null and
     *                                  not greater than 0.
     * @throws PageNotFoundException    If the {@code geneId} or {@code speciesId} are not found in Bgee.
     */
    private static Set<Gene> loadGenes(GeneService geneService, String geneId, Integer speciesId,
            boolean withSpeciesSourceInfo, boolean withSynonymInfo, boolean withXRefInfo)
                    throws InvalidRequestException, PageNotFoundException {
        log.traceEntry("{}, {}, {}, {}, {}, {}", geneService, geneId, speciesId, withSpeciesSourceInfo,
                withSynonymInfo, withXRefInfo);

        //Sanity checks
        if (StringUtils.isBlank(geneId)) {
            throw log.throwing(new InvalidRequestException("Gene ID cannot be blank"));
        }
        if (speciesId != null && speciesId <= 0) {
            throw log.throwing(new InvalidRequestException("Species ID must be greater than 0"));
        }

        Set<Gene> genes = null;
        try {
            genes = speciesId != null && speciesId > 0?
                geneService.loadGenes(Collections.singleton(new GeneFilter(speciesId, geneId)),
                        withSpeciesSourceInfo, withSynonymInfo, withXRefInfo).collect(Collectors.toSet()):
                geneService.loadGenesById(geneId, withSpeciesSourceInfo, withSynonymInfo, withXRefInfo);
        } catch (IllegalArgumentException | GeneNotFoundException e) {
            //we do nothing here, the speciesId was probably incorrect, this will throw
            //a PageNotFoundException below;
            log.catching(e);
        }
        if (genes == null || genes.size() == 0) {
            throw log.throwing(new PageNotFoundException("No gene corresponding to " + geneId
                    + (speciesId != null && speciesId > 0? " in species " + speciesId: "")));
        }
        return log.traceExit(genes);
    }

    /**
     * Load {@code GeneHomologs} for a {@code Gene} uniquely identified thanks to {@code geneId}
     * and {@code speciesId} (since in Bgee we sometimes use the genome of a closely related species
     * for a species with no genome, a gene ID can exist in several species).
     *
     * @param geneId
     * @param speciesId
     * @param homologsService
     * @return
     * @throws PageNotFoundException
     */
    private static GeneHomologs loadHomologs(String geneId, Integer speciesId, GeneHomologsService homologsService)
            throws PageNotFoundException {
        log.traceEntry("{}, {}, {}", geneId, speciesId, homologsService);

        // Load homology information. As we decided to only show in species paralogs in the gene
        // page, we do not use same filters to retrieve orthologs and paralogs. That is why we
        // first create one GeneHomologs object containing only paralogs and one GeneHomologs
        // object containing only orthologs.
        // TODO: improve that, because as a result, the main gene is requested twice,
        // data sources are requested twice, etc.
        try {
            GeneHomologs geneOrthologs = homologsService.getGeneHomologs(geneId, speciesId, true, false);
            GeneHomologs geneParalogs = homologsService.getGeneHomologs(geneId, speciesId,
                    Collections.singleton(speciesId), null, true, false, true);
            // generate one unique GeneHomologs object containing both paralogs and orthologs
            // retrieved using different filters
            return log.traceExit(GeneHomologs.mergeGeneHomologs(geneOrthologs, geneParalogs));
        } catch (IllegalArgumentException | GeneNotFoundException e) {
            log.catching(e);
            throw log.throwing(new PageNotFoundException("No gene corresponding to " + geneId
                    + (speciesId != null && speciesId > 0? " in species " + speciesId: "")));
        }
    }

    private static GeneExpressionResponse loadExpression(String geneId, Integer speciesId,
            Set<String> condParams, CallService callService,
            Function<List<ExpressionCall>, Map<ExpressionCall, Integer>> clusteringFunction)
                    throws PageNotFoundException {
        log.traceEntry("{}, {}, {}, {}, {}", geneId, speciesId, condParams, callService, clusteringFunction);

        //retrieve calls with silver quality for the requested condition parameters.
        EnumSet<CallService.Attribute> condParamAttrs = condParams == null || condParams.isEmpty()?
                //default value
                EnumSet.of(CallService.Attribute.ANAT_ENTITY_ID, CallService.Attribute.CELL_TYPE_ID):
                //otherwise retrieve condition parameters from request
                CallService.Attribute.getAllConditionParameters()
                    .stream().filter(a -> condParams.contains(a.getCondParamName()))
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(CallService.Attribute.class)));

        try {
            List<ExpressionCall> calls = callService.loadSilverCondObservedCalls(
                    new GeneFilter(speciesId, geneId), condParamAttrs);
            if (calls.isEmpty()) {
                log.debug("No calls for gene {} in species {}", geneId, speciesId);
                //XXX: maybe we should retrieve the gene here with the method loadGenes
                //to provide the gene info in the response even in the absence of expression?
                //But CallService would have already loaded the gene, this duplicates the queries.
                //Or CallService should return a Call container, allowing to retrieve the Gene
                //even in the absence of expression?
                //In the meantime, I removed the gene attribute from GeneExpressionResponse
                return log.traceExit(new GeneExpressionResponse(calls, condParamAttrs, true, new HashMap<>()));
            }

            //Store a clustering of ExpressionCalls
            Map<ExpressionCall, Integer> clustering = clusteringFunction.apply(calls);

            return log.traceExit(new GeneExpressionResponse(calls, condParamAttrs, true, clustering));
            
        } catch (IllegalArgumentException | GeneNotFoundException e) {
            log.catching(e);
            throw log.throwing(new PageNotFoundException("No gene corresponding to " + geneId
                    + (speciesId != null && speciesId > 0? " in species " + speciesId: "")));
        }
    }

    //TODO: to delete once the new website is released.
    private GeneResponse buildGeneResponse(Gene gene, Set<String> condParams) 
            throws IllegalStateException, PageNotFoundException {
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
                new GeneFilter(gene.getSpecies().getId(), gene.getGeneId()), condParamAttrs);
        
        // Load homology information.
        GeneHomologs geneHomologs = loadHomologs(gene.getGeneId(), gene.getSpecies().getId(),
                serviceFactory.getGeneHomologsService());

        if (calls.isEmpty()) {
            log.debug("No calls for gene {}", gene.getGeneId());
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
            log.catching(e);
            throw log.throwing(new IllegalStateException("No clustering method corresponding to "
                    + this.prop.getGeneScoreClusteringMethod().trim()));
        }
    }
}