package org.bgee.model.expressiondata.multispecies;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.print.attribute.Size2DSyntax;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.CommonService;
import org.bgee.model.ElementGroupFromListSpliterator;
import org.bgee.model.Entity;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.multispemapping.AnatEntitySimilarity;
import org.bgee.model.anatdev.multispemapping.AnatEntitySimilarityService;
import org.bgee.model.anatdev.multispemapping.DevStageSimilarity;
import org.bgee.model.anatdev.multispemapping.DevStageSimilarityService;
import org.bgee.model.expressiondata.Call;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.ConditionFilter;
import org.bgee.model.expressiondata.MultiGeneExprAnalysis.MultiGeneExprCounts;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.ExpressionLevelInfo;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.gene.GeneService;
import org.bgee.model.gene.OrthologousGeneGroup;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.ontology.OntologyService;
import org.bgee.model.species.Species;
import org.bgee.model.species.SpeciesService;
import org.bgee.model.species.Taxon;
import org.bgee.model.species.TaxonomyFilter;

/**
 * A {@link Service} to obtain {@link MultiSpeciesCall} objects. 
 * Users should use the {@link org.bgee.model.ServiceFactory} to obtain {@code CallService}s.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @author  Julien Wollbrett
 * @version Bgee 15.0, APr. 2021
 * @since   Bgee 13, May 2016
 */
public class MultiSpeciesCallService extends CommonService {
    private static final Logger log = LogManager.getLogger(MultiSpeciesCallService.class.getName());

    public static enum Attribute implements Service.Attribute {
        GENE, ANAT_ENTITY_ID, DEV_STAGE_ID, CELL_TYPE_ID, SEX_ID, STRAIN_ID, 
        CALL_TYPE, DATA_QUALITY, OBSERVED_DATA, MEAN_RANK,
        DATA_TYPE_RANK_INFO, OMA_HOG_ID;
    }

    public static enum OrderingAttribute implements Service.OrderingAttribute {
        GENE_ID, ANAT_ENTITY_ID, DEV_STAGE_ID, CELL_TYPE_ID, SEX_ID, STRAIN_ID, 
        MEAN_RANK, OMA_HOG_ID;
    }
    
    private final CallService callService;
    private final AnatEntitySimilarityService anatEntitySimilarityService;
    private final DevStageSimilarityService devStageSimilarityService;
    private final OntologyService ontologyService;
    private final SpeciesService speciesService;
    private final GeneService geneService;

    /**
     * @param serviceFactory            The {@code ServiceFactory} to be used to obtain
     *                                  {@code Service}s and {@code DAOManager}.
     * @throws IllegalArgumentException If {@code serviceFactory} is {@code null}.
     */
    public MultiSpeciesCallService(ServiceFactory serviceFactory) {
        super(serviceFactory);
        this.callService = this.getServiceFactory().getCallService();
        this.anatEntitySimilarityService = this.getServiceFactory().getAnatEntitySimilarityService();
        this.devStageSimilarityService = this.getServiceFactory().getDevStageSimilarityService();
        this.ontologyService = this.getServiceFactory().getOntologyService();
        this.speciesService = this.getServiceFactory().getSpeciesService();
        this.geneService = this.getServiceFactory().getGeneService();
    }
    
    /**
     * 
     * @param gene          A {@code Gene} allowing to target one specific HOG.
     *                      if {@code null}, all HOGs for the requested {@code taxonId}
     *                      are considered.
     * @return
     */
    //XXX: this method should use loadMultiSpeciesCalls(Collection) at some point
    //
    //XXX: I think we should have a MultiSpeciesExpressionCallFilter, in the same spirit
    //of the ExpressionCallFilter used in the CallService. It could have:
    //  * Collection<MultiSpeciesConditionFilter>, allowing to specify the targeted similarity relations,
    //    the confidence level of the similarity relations, the organs and stages
    //    that should be part of the similarity groups.
    //  * a TaxonomyFilter, allowing to specify the targeted taxon and/or species.
    //    If no taxon is provided but only species, this would mean "target their last common ancestor".
    //  * Collection<GeneFilter> allowing to specify the "starting" genes.So it would be
    //    a slightly different use as compared to the use in the CallFilter.
    //  * the minimum quality level for each CallType through a Map<ExpressionSummary, SummaryQuality>.
    //    Again, this would be a slightly different use as compared to the CallFilter,
    //    as it would not allow to specify the CallTypes to use, since we will always need
    //    to look at both EXPRESSED and NOT_EXPRESSED calls for making the comparisons.
    //  * DataTypeFilters, as in CallFilter
    //  * ObservedData states, as in ExpressionCallFiter
    //  * => Maybe the MultiSpeciesExpressionCallFilter should actually include an ExpressionCallFilter?
    //    but then the conditionFilters attribute would be inappropriate,
    //    cleaner to create a new class IMO.
    //  * => the Gene argument of this method could then be removed if we specify "starting" genes
    //    from the GeneFilters in MultiSpeciesExpressionCallFilter. And several "starting" genes
    //    could be specified at once then?
    //  * => this method's signature would then perfectly mirror the signature of method
    //    CallService.loadExpressionCalls(ExpressionCallFilter, Collection, LinkedHashMap)
    //
    //XXX: note: the CallService can returned ExpressionCalls ordered by OMA HOG ID,
    //this is how several "starting" genes could be managed at once, by loading into memory all genes
    //belonging to a same orthology group before moving to the next group
    //(this should be added to org.bgee.model.expressiondata.CallService.OrderingAttribute then,
    //the Attribute already exists in the DAO).
//    public Stream<Entry<OrthologousGeneGroup, Set<MultiSpeciesCall<ExpressionCall>>>> loadMultiSpeciesCalls(
//    		MultiSpeciesExpressionCallFilter multiSpeciesCallFilter, Set<GeneFilter> startingGeneFilters,
//    		Collection<Attribute> attributes, LinkedHashMap<OrderingAttribute, Service.Direction> orderingAttributes) {
//        log.traceEntry("{}, {}, {}, {}", multiSpeciesCallFilter, startingGeneFilters, attributes, orderingAttributes);
//        if(multiSpeciesCallFilter == null){
//        	throw log.throwing(new IllegalArgumentException("Provided multiSpeciesCallFilter should not be null"));
//        }
//        final Set<Attribute> clonedAttrs = Collections.unmodifiableSet(
//                attributes == null? EnumSet.noneOf(Attribute.class): EnumSet.copyOf(attributes));
//        final LinkedHashMap<OrderingAttribute, Service.Direction> clonedOrderingAttrs = 
//                orderingAttributes == null? new LinkedHashMap<>(): new LinkedHashMap<>(orderingAttributes);
//        final Set<GeneFilter> clonedGeneFilters =  Collections.unmodifiableSet(startingGeneFilters);
//        final MultiSpeciesConditionFilter multiSpeConditionFilter = multiSpeciesCallFilter.getMultiSpeciesCondFilter();
//        //Retrieve Gene
//        if (clonedGeneFilters == null || clonedGeneFilters.isEmpty()) {
//            throw log.throwing(new IllegalArgumentException("At least one starting gene should be provided"));
//        }
//      	//load all selected genes
//      	Set<Gene> genes = this.getServiceFactory().getGeneService().loadGenes(clonedGeneFilters)
//      			.collect(Collectors.toSet());
//      	if (genes == null || genes.isEmpty() || genes.size() > 1) {
//        TaxonomyFilter taxonomyFilter = multiSpeciesCallFilter.getTaxonFilter();
//      //TODO check consistency between taxonId and speciesIds
//        if(!consistencySpeciesAndTaxa(taxonomyFilter)){
//        	throw log.throwing(new IllegalArgumentException("taxon (" + taxonomyFilter.getTaxonIds()) + ") and species ("
//        			+ taxonomyFilter.getSpeciesIds() + ") are not consistent.");
//        }
//        //test that gene from GeneFilter is part of selected species from TaxanomyFilter
//        if(!(taxonomyFilter.getAllSpeciesRequested()||taxonomyFilter.getSpeciesIds().contains(gene.getSpecies()))){
//        	 throw log.throwing(new IllegalArgumentException("Gene species (" + gene.getSpecies().getId() +
//                     ") is not in provided species (" + taxonomyFilter.getSpeciesIds() +")"));
//        }
//        //retrieve Taxon Ontologyy with all descendants of the targeted taxon and/or species
//        //if no taxon is provided but only species, this would mean "target their last common ancestor"
//        MultiSpeciesOntology <Taxon,Integer> taxonOnt = this.getServiceFactory().getOntologyService()
//                        .getTaxonOntology(taxonomyFilter.getSpeciesIds(), Collections.singleton(taxonomyFilter.getTaxonId()),
//                        		true, false);
//        
//        //test that selected gene is part of the taxonomy
//        Integer parentTaxonId = gene.getSpecies().getParentTaxonId();
//        if (taxonOnt.getElement(parentTaxonId) == null) {
//            throw log.throwing(new IllegalStateException("Taxon ID " + gene.getSpecies().getParentTaxonId() +
//                    "not found in retrieved taxonomy"));
//        }
//        List<Integer> orderedTaxonIds = taxonOnt.getOrderedAncestors(
//        		taxonOnt.getElement(parentTaxonId)).stream().map(t -> t.getId())
//        		.collect(Collectors.toList());
//        Integer highestTaxonId = orderedTaxonIds.get(orderedTaxonIds.size() - 1);
//        //create a Map of Map.... taxon Id is a key corresponding to a Map where OMA node Id is the key 
//        //corresponding to a set of genes 
//        Map<Integer, Map<Integer, Set<Gene>>> taxonToGenes;
//        orderedTaxonIds.stream().forEach(t -> {
//        	taxonToGenes.put(t, this.getServiceFactory().getGeneService()
//                .getOrthologs(t, taxonomyFilter.getSpeciesIds(),
//                		Collections.singleton(gene.getGeneId())));
//        });     
//        
//        //from leaf to root
//        TAXON: for (Integer taxId: orderedTaxonIds) {
//        	Map<Integer, Set<Gene>> omaNodeIdToGenes = this.getServiceFactory().getGeneService()
//                    .getOrthologs(taxId, taxonomyFilter.getSpeciesIds(),
//                    		Collections.singleton(gene.getGeneId()));
//        	OMANODE: for (Integer omaNodeId:omaNodeIdToGenes.keySet()){
//        		Set<ExpressionCall> calls = this.callService.loadExpressionCalls(
//                		convertToExprCallFilter(multiSpeciesCallFilter, omaNodeIdToGenes.get(omaNodeId)),
//                		convertMultiSpeciesAttrToSpeciesAttr(attributes), 
//                		convertMultiSpeciesOrderingAttrToSpeciesOrderingAttr(orderingAttributes))
//        				.collect(Collectors.toSet());
//        		
//        		Map<MultiSpeciesCondition, Set<ExpressionCall>> multiSpeCalls = calls.stream()
//            			.map(c -> new AbstractMap.SimpleEntry<>(new MultiSpeciesCondition(), (ExpressionCall)c))
//            			.groupingBy(e -> e.getKey(), Collectors.mapping(e -> e.getValue(), Collectors.toSet()));
//        	}
//        	ExpressionCallFilter ecf;
//        	ecf.get
//        	AnatEntitySimilarity = callService.getServiceFactory().getAnatEntityService()
//        	.loadAnatEntitySimilarities(taxonId, speciesIds, onlyTrusted)
//        			.collect(Collectors.toMap(
//        					a -> a.getAnatEntityIds(),
//        					a -> a));
//  	
//        	
//        	create multiSpeciesCall;
//        	
//        	Map<Integer, Set<String>> speToGeneIds = orthologousGenes.stream()
//        			.collect(Collectors.toMap(
//        					g -> g.getSpecies().getId(),
//        					g -> new HashSet<>(Arrays.asList(g.getGeneId())),
//        					(s1, s2) -> {s1.addAll(s2); return s1;}));
//        	
////        	if conserved, continue to next taxon; if not, stop iteration of taxon.
//        	
//        }
//        
//      	}
//        throw new UnsupportedOperationException("Not implemented");
//    }
      	
//    /**
//     * 
//     * @param multiSpeciesCallFilter	A {@code MultiSpeciesCallFilter} describing filter used to retrieve the
//     * 									multispecies calls: These filters unclude conditions filters (anatomical
//     * 									entities and developmental stage), taxon and species filter, call quality
//     * 									filter (bronze, silver or gold), and data type filter (in situ, RNAseq, etc.)
//     * @param geneFilter				A {@code GeneFilter} corresponding to starting genes for which orthologs
//     * 									should be retrieved. All these genes must correspond to the same species
//     * @return							A {@code Set} of {@code MultiSpeciesCall} for which conservation score is
//     * 									higher than MultiSpeciesCall.CONSERVATION_SCORE_THRESHOLD.
//     */
//    public Set<MultiSpeciesCall<ExpressionCall>> getConservation(
//    		MultiSpeciesExpressionCallFilter multiSpeciesCallFilter, GeneFilter geneFilter){
//    	log.traceEntry("{}, {}", multiSpeciesCallFilter, geneFilter);
//        if(multiSpeciesCallFilter == null){
//        	throw log.throwing(new IllegalArgumentException("Provided multiSpeciesCallFilter should not be null"));
//        }
//        if(geneFilter == null){
//        	throw log.throwing(new IllegalArgumentException("Provided starting genes should not be null"));
//        }
//    	//create taxon ontology containing all wished species (from starting genes and taxon filter)
//        Set<Integer> speciesIds = new HashSet<Integer>(multiSpeciesCallFilter.getTaxonFilter().getSpeciesIds());
//        speciesIds.add(geneFilter.getSpeciesId());
//        Set<Species> species = speciesService.loadSpeciesByIds(speciesIds, false);
//    	Ontology<Taxon, Integer> taxonOntology = ontologyService.getTaxonOntologyLeadingToSpecies(
//    			speciesIds, false, false);
//    	Map<Taxon,Set<Species>> leavesLCA = getLeavesLCA(taxonOntology, species, geneFilter);
//    	Set<AnatEntitySimilarity> anatEntitySims = getAnatSimByTaxonId(leavesLCA);
//    	Set<DevStageSimilarity> devStageSims = getDevStagesSimByTaxonId(leavesLCA);
//    	//TODO need to update to match new GeneHomologs class
//    	Set<OrthologousGeneGroup> orthologousGeneGroups = new HashSet<OrthologousGeneGroup>();
////    	Set<OrthologousGeneGroup> orthologousGeneGroups = geneService.getOrthologs(
////    			leavesLCA.keySet().stream().map(t -> t.getId()).collect(Collectors.toSet()),
////    				speciesIds,geneFilter)
////    			.collect(Collectors.toSet());
//    	ExpressionCallFilter callFilter = convertToExprCallFilter(multiSpeciesCallFilter,
//    			anatEntitySims, devStageSims, 
//    			orthologousGeneGroups);
//    	Stream<ExpressionCall> calls = callService.loadExpressionCalls(callFilter, 
//    			EnumSet.of(CallService.Attribute.GENE, CallService.Attribute.ANAT_ENTITY_ID, 
//                        CallService.Attribute.DEV_STAGE_ID, CallService.Attribute.CELL_TYPE_ID,
//                        CallService.Attribute.SEX_ID, CallService.Attribute.STRAIN_ID, 
//                        CallService.Attribute.DATA_QUALITY),
//    			new LinkedHashMap<>());
//    	Set<MultiSpeciesCall<ExpressionCall>> multiSpeciesCall = groupCalls(
//    			orthologousGeneGroups, anatEntitySims, devStageSims, calls.collect(Collectors.toSet()));
//    	return log.traceExit(computeConservationScore(multiSpeciesCall));
//    	
//    	
//    	
//    }

//    /**
//     * Allows to perform gene expression comparisons for any arbitrary group of genes,
//     * and not only for orthologous genes, as in method {@link #loadMultiSpeciesCalls(TaxonomyFilter, Gene)}.
//     *  
//     * @param genes
//     * @return
//     */
//    public Stream<MultiSpeciesCall<ExpressionCall>> loadMultiSpeciesCalls(TaxonomyFilter taxonomyFilter,
//            Collection<Gene> genes, ExpressionCallFilter callFilter, Collection<Attribute> attributes, 
//            LinkedHashMap<OrderingAttribute, Service.Direction> orderingAttributes) {
//        log.traceEntry("{}, {}, {}, {}, {}",taxonomyFilter, genes, callFilter, attributes, orderingAttributes);
//        throw new UnsupportedOperationException("Not implemented");
//    }

//    /**
//     * Retrieve multi-species expression calls for a specific gene in provided species.
//     * 
//     * @param gene          A {@code Gene} that is the gene for which to return multi-species calls.
//     * @param speciesIds    A {@code Collection} of {@code Integer}s that are the IDs of species
//     *                      in which the gene orthologs should be found.
//     * @return              The {@code LinkedHashMap} where keys are {@code Integer}s corresponding
//     *                      to IDs of taxa ordered from closest to farthest of {@code gene},
//     *                      the associated values being {@code Set} of {@code MultiSpeciesCall}s
//     *                      corresponding to multi-species calls.
//     */
//    public LinkedHashMap<Integer, Set<MultiSpeciesCall<ExpressionCall>>> loadMultiSpeciesExpressionCalls(
//            Gene gene, Collection<Integer> speciesIds) {
//        log.traceEntry("{}, {}", gene, speciesIds);
//        
//        if (gene == null) {
//            throw log.throwing(new IllegalArgumentException("Provided gene should not be null"));
//        }
//        if (gene.getSpecies() == null || gene.getSpecies().getId() == null) {
//            throw log.throwing(new IllegalArgumentException("Expecting species info in provided gene:" + gene));
//        }
//        
//        final Set<Integer> clonedSpeIds = Collections.unmodifiableSet(
//                speciesIds == null? new HashSet<>(): new HashSet<>(speciesIds));
//        if (!clonedSpeIds.isEmpty() && !clonedSpeIds.contains(gene.getSpecies().getId())) {
//            throw log.throwing(new IllegalArgumentException("Gene species (" + gene.getSpecies().getId() +
//                    ") is not in provided species (" + speciesIds +")"));
//        }
//        
//        // Get all relevant taxa from the species
//        // FIXME: load all taxonomy ontology, should we filter it?
//        Ontology<Taxon, Integer> taxonOnt = this.getServiceFactory().getOntologyService()
//                .getTaxonOntology();
//        Integer parentTaxonId = gene.getSpecies().getParentTaxonId();
//        if (taxonOnt.getElement(parentTaxonId) == null) {
//            throw log.throwing(new IllegalStateException("Taxon ID " + gene.getSpecies().getParentTaxonId() +
//                    "not found in retrieved taxonomy"));
//        }
//        
//        LinkedHashMap<Integer, Set<MultiSpeciesCall<ExpressionCall>>> taxaToCalls = new LinkedHashMap<>();
//        
//        LinkedHashMap<CallService.OrderingAttribute, Direction> orderAttrs = new LinkedHashMap<>();
//        orderAttrs.put(CallService.OrderingAttribute.GENE_ID, Direction.ASC);
//
//
//        //XXX: the retrieval of the taxon IDs could be moved at the end of this method,
//        //when you start iterating from the more precise taxon and to the root of the taxon ontology
//        List<Integer> taxonIds = taxonOnt.getOrderedAncestors(
//                taxonOnt.getElement(parentTaxonId)).stream()
//            .map(t -> t.getId())
//            .collect(Collectors.toList());
//
//        //FIXME: getOrthologs should take a Gene, a Taxon and a collection of species,
//        //and return Map<taxonId, Set<Gene>>, using the highest taxon ID
//        int highestTaxonId = 0;// to implement
//        Map<Taxon, Set<Gene>> taxonToGenes = this.getServiceFactory().getGeneHomologsService()
//                .getGeneHomologs(gene.getGeneId(), gene.getSpecies().getId(), 
//                        new HashSet<Integer>(speciesIds),highestTaxonId, true, true, false)
//                .getOrthologsByTaxon();
//        for (Integer taxonId : taxonIds) {
//            log.trace("Starting generation of multi-species calls for taxon ID {}", taxonId);
//            // Retrieve homologous organ groups with gene IDs
//            log.trace("Homologous organ groups with genes: {}", taxonToGenes);
////            Set<String> orthologousGeneIds = omaToGenes.values().stream()
////                    .filter(geneSet -> geneSet.stream()
////                            .anyMatch(g -> gene.getGeneId().equals(g.getGeneId())))
////                    .flatMap(Set::stream)
////                    .map(g -> g.getGeneId())
////                    .collect(Collectors.toSet());
//
//            // Retrieve anat. entity similarities
//            Set<AnatEntitySimilarity> anatEntitySimilarities = this.getServiceFactory()
//                    .getAnatEntitySimilarityService().loadPositiveAnatEntitySimilarities(taxonId, true);
//            log.trace("Anat. entity similarities: {}", anatEntitySimilarities);
//            Set<String> anatEntityIds = anatEntitySimilarities.stream()
//                    .flatMap(s -> s.getSourceAnatEntities().stream().map(a -> a.getId()))
//                    .collect(Collectors.toSet());
//            
//            // Retrieve dev. stage similarities
//            Set<DevStageSimilarity> devStageSimilarities = this.getServiceFactory() 
//                    .getDevStageSimilarityService().loadDevStageSimilarities(taxonId, clonedSpeIds);
//            log.trace("Dev. stage similarities: {}", devStageSimilarities);
//            Set<String> devStageIds = devStageSimilarities.stream()
//                    .map(s -> s.getDevStageIds()).flatMap(Set::stream).collect(Collectors.toSet());
//
//            // Build ExpressionCallFilter
//            Set<ConditionFilter> conditionFilters = new HashSet<>();
//            
//            conditionFilters.add(new ConditionFilter(anatEntityIds, devStageIds, anatEntityIds, null, null, null));
//            log.warn("Only expressed calls are retrieved");
//            
////            // For each species, we load propagated and reconciled calls
////            Set<ExpressionCall> calls = new HashSet<>();
////            for (Integer spId: clonedSpeIds) {
////                //FIXME: adapt to new API
//////                ExpressionCallFilter callFilter =
//////                        new ExpressionCallFilter(
//////                    new GeneFilter(spId, orthologousGeneIds),
//////                    conditionFilters,
//////                    null,   // dataTypeFilter
//////                    ExpressionSummary.EXPRESSED,
//////                    null,   // SummaryQuality
//////                    true);
//////                // FIXME do propagation???
//////                Set<ExpressionCall> currentCalls = this.getServiceFactory().getCallService().
//////                    loadExpressionCalls(callFilter, null, orderAttrs).collect(Collectors.toSet());
//////                calls.addAll(currentCalls);
////            }
////            Set<ExpressionCall> calls = null; //to implement
////            taxaToCalls.put(taxonId, this.groupCalls(taxonId,
////                    omaToGenes, anatEntitySimilarities, devStageSimilarities, calls));
//            log.trace("Done generation of multi-species calls for taxon ID {}", taxonId);
//        }
//        return taxaToCalls;
//    }

//    /**
//     * Group {@code ExpressionCall}s into {@code MultiSpeciesCall}s.
//     * 
//     * @param orthoGeneGroups            A {@code Set} of {@code OrthologousGeneGroup}s containing
//     * 									information about orthologous genes for all OMA nodes of
//     * 									one OMA group
//     * @param anatEntitySimilarities    A {@code Set} of {@code AnatEntitySimilarity}s thats are 
//     *                                  similarity groups of anatomical entities to use to build  
//     *                                  {@code MultiSpeciesCall}s.
//     * @param devStageSimilarities      A {@code Set} of {@code DevStageSimilarity}s thats are 
//     *                                  similarity groups of developmental stages to use to build  
//     *                                  {@code MultiSpeciesCall}s.
//     * @param calls                     A {@code Set} of {@code ExpressionCall}s thats are 
//     *                                  expression calls to use to build {@code MultiSpeciesCall}s.
//     * @return                          The {@code Set} of {@code MultiSpeciesCall}s thats are 
//     *                                  multi-species calls.
//     * @throws IllegalArgumentException If an {@code ExpressionCall} has not all necessary data 
//     *                                  or if similarity groups are incorrect.
//     */
//    // TODO to be added to ExpressionCallUtils see TODOs into ExpressionCall
//    private Set<MultiSpeciesCall<ExpressionCall>> groupCalls(Set<OrthologousGeneGroup> orthoGeneGroups, 
//    		Set<AnatEntitySimilarity> anatEntitySimilarities, 
//    		Set<DevStageSimilarity> devStageSimilarities, Set<ExpressionCall> calls)
//                    throws IllegalArgumentException {
//        log.traceEntry("{}, {}, {}, {}", orthoGeneGroups, anatEntitySimilarities, devStageSimilarities, calls);
//        Set<AnatEntitySimilarity> unmodifiableAESims = Collections.unmodifiableSet(anatEntitySimilarities);
//        Set<DevStageSimilarity> unmodifiableDSSims = Collections.unmodifiableSet(devStageSimilarities);
//        Set<ExpressionCall> unmodifiablecalls = Collections.unmodifiableSet(calls);
//        Set<OrthologousGeneGroup> unmodifiableOGGroups = Collections.unmodifiableSet(orthoGeneGroups);
//        Set<MultiSpeciesCall<ExpressionCall>> multiSpCalls = new HashSet<>();
//        //loop on all calls of one OMA group
//        unmodifiableOGGroups.stream().forEach(ogg -> {
//        	multiSpCalls.add(new MultiSpeciesCall<ExpressionCall>(
//        			new MultiSpeciesCondition(
//        					unmodifiableAESims.stream()
//        						.filter(aes -> ogg.getTaxonId().equals(aes.getRequestedTaxon().getId()))
//        						.findFirst().get(),
//        					unmodifiableDSSims.stream()
//        					.filter(dss -> ogg.getTaxonId().equals(dss.getTaxonId()))
//        					.findFirst().get()
//        					),
//        			ogg.getTaxonId(), ogg.getOmaGroupId(),
//        			ogg.getGenes(), new HashSet<>(), null));
//        });
//        for (ExpressionCall call: unmodifiablecalls) {
//            log.trace("Iteration expr call {}", call);
//            if (call.getCondition() == null) {
//                throw log.throwing(new IllegalArgumentException("No condition for " + call));
//            }
//            if (call.getGene() == null) {
//                throw log.throwing(new IllegalArgumentException("No gene for " + call));
//            }
//            Set<MultiSpeciesCall<ExpressionCall>> currentMultiSpeCalls = multiSpCalls.stream()
//            		.filter(msc -> msc.getMultiSpeciesCondition()
//            		.getAnatSimilarity().getSourceAnatEntities().contains(call.getCondition().getAnatEntity()))
//            		.filter(msc -> msc.getMultiSpeciesCondition()
//            		.getStageSimilarity().getDevStageIds().contains(call.getCondition().getDevStageId()))
//            		.filter(msc -> msc.getOrthologousGenes().contains(call.getGene()))
//            		.collect(Collectors.toSet());
//            currentMultiSpeCalls.stream().forEach(msc -> {
//            	msc.getCalls().add(call);
//            	msc.getSpeciesIds().add(call.getGene().getSpecies().getId());
//            });
//            Set<AnatEntitySimilarity> curAESimilarities = anatEntitySimilarities.stream()
//                    .filter(s -> s.getSourceAnatEntities().contains(call.getCondition().getAnatEntity()))
//                    .collect(Collectors.toSet());
//            if (curAESimilarities.size() == 0) {
//                log.trace(call.getCondition().getAnatEntityId() +
//                        " found in any anat. entity similarity group");
//                continue;
//            }
//        }
//        return log.traceExit(multiSpCalls);
//            
//            
//    }
    
//    /**
//     * Compute conservation score of {@code inputCall}.
//     * The conservation score corresponds to the number of species where genes are expressed, divided by
//     * the total number of species having orthologous genes for one taxon in one OMA group (i.e for one
//     * OMA node)
//     * 
//     * @param inputCall A {@code MultiSpeciesCall} that is the multi-species call expression calls
//     *                  for which to compute conservation score.
//     * @return          The {@code MultiSpeciesCall} that is the multi-species call expression calls
//     *                  with computed conservation score.
//     */
//    public Set<MultiSpeciesCall<ExpressionCall>> computeConservationScore(Set<MultiSpeciesCall<ExpressionCall>> inputCalls) {
//        log.traceEntry("{}", inputCalls);
//        Set<MultiSpeciesCall<ExpressionCall>> outputCalls = new HashSet<>();
//        for(MultiSpeciesCall<ExpressionCall> inputCall:inputCalls){
//	        BigDecimal conservationScore = new BigDecimal(
//	        		inputCall.getCalls().stream().map(a -> a.getGene().getSpecies().getId())
//	        		.collect(Collectors.toSet()).size())
//	        		.divide(
//	        				new BigDecimal(inputCall.getOrthologousGenes().stream().map(g -> g.getSpecies().getId())
//	        						.collect(Collectors.toSet()).size()),
//	        				2, RoundingMode.HALF_UP);
//	        if(conservationScore.compareTo(new BigDecimal(MultiSpeciesCall.CONSERVATION_SCORE_THRESHOLD)) >= 0)
//	        outputCalls.add(new MultiSpeciesCall<>(inputCall.getMultiSpeciesCondition(), inputCall.getTaxonId(),
//	        		inputCall.getOMAGroupId(), inputCall.getOrthologousGenes(), inputCall.getCalls(),
//	        		conservationScore));
//        }
//        return log.traceExit(outputCalls);
//    }
    
    
    
    /**
     * 
     * @param attributes	A {@code Collection} of {@code MultiSpeciesCallService.Attribute} having to
     * 						be converted in a {@code Set} of {@code CallService.Attribute}
     * @return				a {@code Set} of {@code CallService.Attribute} that can be used to load
     * 						expression calls.
     */
    private static Set<CallService.Attribute> convertMultiSpeciesAttrToSpeciesAttr(
        Collection<Attribute> attributes) {
        log.traceEntry("{}", attributes);
        
        return log.traceExit(attributes.stream().flatMap(attr -> {
            switch (attr) {
                case GENE: 
                    return Stream.of(CallService.Attribute.GENE);
                case ANAT_ENTITY_ID:
                	return Stream.of(CallService.Attribute.ANAT_ENTITY_ID);
                case DEV_STAGE_ID: 
                    return Stream.of(CallService.Attribute.DEV_STAGE_ID);
                case CELL_TYPE_ID: 
                    return Stream.of(CallService.Attribute.CELL_TYPE_ID);
                case SEX_ID: 
                    return Stream.of(CallService.Attribute.SEX_ID);
                case STRAIN_ID: 
                    return Stream.of(CallService.Attribute.STRAIN_ID);
                case CALL_TYPE: 
                	return Stream.of(CallService.Attribute.CALL_TYPE);
                case DATA_QUALITY:
                    return Stream.of(CallService.Attribute.DATA_QUALITY);
                case OBSERVED_DATA:
                    return Stream.of(CallService.Attribute.OBSERVED_DATA);
                case MEAN_RANK:
                    return Stream.of(CallService.Attribute.MEAN_RANK);
                case DATA_TYPE_RANK_INFO:
                    return Stream.of(CallService.Attribute.DATA_TYPE_RANK_INFO);
                default: 
                    throw log.throwing(new IllegalStateException(
                            "Unsupported Attributes from CallService: " + attr));
            }
        }).collect(Collectors.toCollection(() -> EnumSet.noneOf(CallService.Attribute.class))));
    }
    
    /**
     * 
     * @param orderingAttributes	A {@code LinkedHashMap} with {@code MultiSpeciesCallService.OrderingAttribute}
     * 								as key and {@code Service.Direction} as value. It will be converted into a 
     * 								{@code LinkedHashMap} with {@code CallService.OrderingAttribute} as key and 
     * 								{@code Service.Direction} as value
     * @return						Q {@code LinkedHashMap} with {@code CallService.OrderingAttribute} as key and 
     * 								{@code Service.Direction} as value, that can be used to order retrieved
     * 								expression calls
     */
    private static LinkedHashMap<CallService.OrderingAttribute, Service.Direction>
    convertMultiSpeciesOrderingAttrToSpeciesOrderingAttr(
            LinkedHashMap<MultiSpeciesCallService.OrderingAttribute, Service.Direction> orderingAttributes) {
        log.traceEntry("{}", orderingAttributes);
        
        return log.traceExit(orderingAttributes.entrySet().stream().collect(Collectors.toMap(
            e -> {
                switch (e.getKey()) {
                    case GENE_ID: 
                        return CallService.OrderingAttribute.GENE_ID;
                    case ANAT_ENTITY_ID:
                        return CallService.OrderingAttribute.ANAT_ENTITY_ID;
                    case DEV_STAGE_ID: 
                        return CallService.OrderingAttribute.DEV_STAGE_ID;
                    case CELL_TYPE_ID: 
                        return CallService.OrderingAttribute.CELL_TYPE_ID;
                    case SEX_ID: 
                        return CallService.OrderingAttribute.SEX_ID;
                    case STRAIN_ID: 
                        return CallService.OrderingAttribute.STRAIN_ID;
                    case MEAN_RANK:
                        return CallService.OrderingAttribute.MEAN_RANK;
                    default: 
                        throw log.throwing(new IllegalStateException(
                                "Unsupported OrderingAttributes from CallService: " + e.getKey()));
                }
            },
            e -> e.getValue(), 
            (v1, v2) -> {throw log.throwing(new IllegalStateException("No key collision possible"));}, 
            () -> new LinkedHashMap<CallService.OrderingAttribute, Service.Direction>())));
    }
    /**
     * 
     * @param taxOnt	An {@code Ontology} containing all taxa and relation between them 
     * @param species	A {@code Set} of {@code Species} for which LCA taxon will be retrieved
     * @param gene		A {code GeneFilter} corresponding to a Set of starting genes IDs from the same
     * 					species.
     * @return			A {@code Map} of {@code Taxon} associated to a {@code Set} of {@code Species}
     * 					corresponding to the Least Common Ancestor taxa for subset of input species
     */
    private static Map <Taxon,Set<Species>> getLeavesLCA(Ontology<Taxon, Integer> taxOnt, 
    		Set<Species> species, GeneFilter gene){
    	log.traceEntry("{}, {}, {}", taxOnt,species,gene);
    	Map<Taxon, Set<Species>> taxonLCAToSpecies = new HashMap<>();
    	//get the parent taxon ID of the starting genes
    	Integer leafStartingGeneParentTaxonId = species.stream()
    			.filter(s -> s.getId().equals(gene.getSpeciesId()))
    			.map(s -> s.getParentTaxonId())
    			.findFirst().get();
    	//get all ancestor taxa of the starting gene parent Taxon in the ontology
    	List<Taxon> orderedStartingGeneTaxonAncestors = taxOnt.getOrderedAncestors(
    			taxOnt.getElement(leafStartingGeneParentTaxonId));
     	//remove the starting gene from the list of not found species and add it to already found species
    	Set<Species> alreadyFoundSpecies = species.stream()
    			.filter(s -> s.getId().equals(gene.getSpeciesId()))
    			.collect(Collectors.toSet());
    	Set<Species> notFoundSpecies = species.stream()
    			.filter(s -> !alreadyFoundSpecies.contains(s))
    			.collect(Collectors.toSet());
    	orderedStartingGeneTaxonAncestors.stream().forEach(t -> {
    		Set<Taxon> descendants = taxOnt.getDescendants(t);
    		for(Taxon currentTaxon :descendants){
    			Set<Species> newFoundSpecies = notFoundSpecies.stream()
    					.filter(s -> s.getParentTaxonId().equals(currentTaxon.getId()))
    					.collect(Collectors.toSet());
    			//if taxon was already found for a starting gene from a different species (
    			if(newFoundSpecies != null){
    				//create a new Entry in the Map
    				taxonLCAToSpecies.put(t, newFoundSpecies);
    				//add species already associated to a deeper taxon
    				taxonLCAToSpecies.get(t).addAll(alreadyFoundSpecies);
    			}
    			notFoundSpecies.removeAll(newFoundSpecies);
    		}
    		
    	});
    	return taxonLCAToSpecies;
    }
    
    /**
     * XXX Do we need to use only trusted values to load AnatEntitySimilarities?
     * @param lcaTaxonToSpecies		A {@code Map} of {@code Taxon} associated to a {@code Set} of {@code Species}
     * 								corresponding to the Least Common Ancestor taxa for a set of species
     * @return						A {@code Map} of {@code Integer} associated to a {@code Set} of 
     * 								{@code AnatEntitSimilarity}. It corresponds to anatomical entity similarities
     * 								ordered by taxa IDs  
     */
    private Set<AnatEntitySimilarity> getAnatSimByTaxonId(Map<Taxon,Set<Species>> lcaTaxonToSpecies){
    	log.traceEntry("{}", lcaTaxonToSpecies);
    	Set<AnatEntitySimilarity> anatEntitySimilarities = new HashSet<>();
    	lcaTaxonToSpecies.entrySet().forEach(t -> {
    		anatEntitySimilarities.addAll(
    				anatEntitySimilarityService.loadPositiveAnatEntitySimilarities(
    						t.getKey().getId(), true));
    	});
    	return log.traceExit(anatEntitySimilarities);
    }
    
    /**
     * 
     * @param taxonToSpecies	A {@code Map} of {@code Taxon} associated to a {@code Set} of {@code Species} 
     * 							corresponding to the Least Common Ancestor taxa for a set of species.
     * @return					A {@code Map} of {@code Integer} associated to a {@code Set} of 
     * 							{@code DevStageSimilarity}. It corresponds to developmental stage similarities
     * 							ordered by taxa IDs.
     */
    private Set<DevStageSimilarity> getDevStagesSimByTaxonId(Map<Taxon, Set<Species>> taxonToSpecies){
    	log.traceEntry("{}", taxonToSpecies);
    	Set<DevStageSimilarity> devStageSimilarities = new HashSet<>();
    	//Retrieve dev stage sim for each taxon and a set of species
    	taxonToSpecies.entrySet().stream().forEach(t -> {
    		Set<DevStageSimilarity> groupingStages = devStageSimilarityService.loadDevStageSimilarities(
    				t.getKey().getId(), 
    				t.getValue().stream().map(
    						s -> s.getId()).collect(Collectors.toSet())
    				);
    		devStageSimilarities.addAll(groupingStages);
    	});
    	return log.traceExit(devStageSimilarities);
    }

    
    /**
     * XXX Do we need to use true or false value for booleans conditionObservedData, anatEntityObservedData,
     * and devStageObservedData. Does it mean that we want to return condition, anatEntity and devStage??? then
     * should be TRUE
     * @param filter			A {@code MultiSpeciesExpressionCallFilter} that is the multi-species
     * 							expression call filter that will be converted into an ExpressionCallFilter
     * @param orthologousGenes	A {@code Set} of {@code Gene} corresponding to orthologous genes having to
     * 							be used as gene filter in the returned {@code ExpressionCallFilter}
     * @return					The {@code ExpressionCallFilter} that is the expression call filter used to
     *                  		retrieve expression calls.
     */
    private static ExpressionCallFilter convertToExprCallFilter(MultiSpeciesExpressionCallFilter filter,
    		Set<AnatEntitySimilarity> anatEntSims, Set<DevStageSimilarity> devStageSims,
    		Set<OrthologousGeneGroup> orthologousGenes) {
    	log.traceEntry("{}, {}, {}, {}", filter, anatEntSims, devStageSims, orthologousGenes);

    	//creates filter on anatEntities by taking into account anat. entity similarities of anat. entities
    	//from the MultiSpeciesConditionFilter
    	Set<String> expressionCallAEntityIds = anatEntSims.stream()
                .filter(aes -> {
                    if (filter.getMultiSpeciesCondFilter().getAnatEntityIds().isEmpty()) {
                        return true;
                    }
                    Set<String> anatEntityIds = aes.getSourceAnatEntities()
                            .stream().map(ae -> ae.getId())
                            .collect(Collectors.toSet());
                    return !Collections.disjoint(anatEntityIds,
                            filter.getMultiSpeciesCondFilter().getAnatEntityIds());
                })
                .flatMap(aes -> aes.getSourceAnatEntities().stream().map(a -> a.getId()))
                .collect(Collectors.toSet());
    	//creates filter on dev. stages by taking into account dev. stage similarities of dev. stages
    	//from the MultiSpeciesConditionFilter
    	Set<String> expressionCallDevStageIds = devStageSims.stream()
                .filter(dss -> filter.getMultiSpeciesCondFilter().getDevStageIds().isEmpty() ||
                        !Collections.disjoint(dss.getDevStageIds(),
                            filter.getMultiSpeciesCondFilter().getDevStageIds()))
                .flatMap(dss -> dss.getDevStageIds().stream())
                .collect(Collectors.toSet());
    	//Same for cell type IDs
        Set<String> expressionCallCellTypeIds = anatEntSims.stream()
                .filter(aes -> {
                    if (filter.getMultiSpeciesCondFilter().getCellTypeIds().isEmpty()) {
                        return true;
                    }
                    Set<String> anatEntityIds = aes.getSourceAnatEntities()
                            .stream().map(ae -> ae.getId())
                            .collect(Collectors.toSet());
                    return !Collections.disjoint(anatEntityIds,
                            filter.getMultiSpeciesCondFilter().getCellTypeIds());
                })
                .flatMap(aes -> aes.getSourceAnatEntities().stream().map(a -> a.getId()))
                .collect(Collectors.toSet());

    	//creates geneFilter
    	Map<Integer, Set<String>> speToGeneIds = orthologousGenes.stream()
    			.flatMap(ogg -> ogg.getGenes().stream())
    			.collect(Collectors.toMap(
    					g -> g.getSpecies().getId(),
    					g -> new HashSet<>(Arrays.asList(g.getGeneId())),
    					(s1, s2) -> {s1.addAll(s2); return s1;}));
    	Set<GeneFilter> geneFilters = new HashSet<>();
    	speToGeneIds.keySet().stream().forEach( s -> {
    		geneFilters.add(new GeneFilter(s, speToGeneIds.get(s)));
    	});

    	return new ExpressionCallFilter(filter.getSummaryCallTypeQualityFilter(), geneFilters, 
    			Collections.singleton(new ConditionFilter(expressionCallAEntityIds,
    			        expressionCallDevStageIds, expressionCallCellTypeIds,
    			        filter.getMultiSpeciesCondFilter().getSexIds(), null)), 
    			filter.getDataTypeFilters(),
    			//no filtering on observed data
    			null);
    }

    //TODO: once the method accepting ExpressionCallFilter will be ready, change this method
    //to accept Genes rather than GeneFilters, to parallel the methods loadMultiSpeciesExprAnalysis
    //and the equivalent methods in CallService
    /**
     * Retrieves similar expression calls from the provided {@code geneFilter},
     * {@code conditionFilter}, and {@code onlyTrusted}.
     * <strong>Warning:</strong> only the anatEntityIds and the cellTypeIds from the provided
     * {@code conditionFilter} are handled in this method for now.
     *
     * @param taxonId           An {@code int} that is the NCBI ID of the taxon for which 
     * 	                        calls should be retrieved.
     * @param geneFilters       A {@code Collection} of {@code GeneFilter}s allowing to filter
     *                          the {@code SimilarityExpressionCall}s to retrieve.
     * @param conditionFilter   A {@code ConditionFilter} containing the conditions for which 
     *                          similar expression calls should be retrieved.
     * @param onlyTrusted       A {@code boolean} defining whether results should be restricted 
     *                          to "trusted" annotations.
     *                          If {@code true}, only trusted annotations are returned.
     * @return                  The {@code Stream} of {@code SimilarityExpressionCall}s that are 
     *                          the similar expression calls for the requested parameters.
     */
    public Stream<SimilarityExpressionCall> loadSimilarityExpressionCalls(int taxonId,
            Collection<GeneFilter> geneFilters, ConditionFilter conditionFilter, boolean onlyTrusted) {
        log.traceEntry("{}, {}, {}, {}", taxonId, geneFilters, conditionFilter, onlyTrusted);
        if (taxonId <= 0) {
            throw log.throwing(new IllegalArgumentException("taxonId must be stricly positive"));
        }
        if (geneFilters != null && geneFilters.stream().anyMatch(Objects::isNull)) {
            throw log.throwing(new IllegalArgumentException("No gene filter should be null"));
        }
        //If GeneFilters are not provided, we retrieve data for all species of the requested taxon
        Set<GeneFilter> clnGeneFilters = Collections.unmodifiableSet(
                geneFilters == null || geneFilters.isEmpty()?
                        this.getServiceFactory().getSpeciesService()
                        .loadSpeciesByTaxonIds(Collections.singleton(taxonId), false)
                        .stream().map(s -> new GeneFilter(s.getId()))
                        .collect(Collectors.toSet()):
                            new HashSet<>(geneFilters));

        // Retrieve AnatEntitySimilarity from the provided taxon.
        // Of note, the root of cell types MUST have a similarity annotation, otherwise,
        // calls in conditions not using cell types would not be retrieved
        Set<AnatEntitySimilarity> anatEntitySimilaritiesFromAnatFilter = anatEntitySimilarityService
                .loadPositiveAnatEntitySimilarities(taxonId, onlyTrusted);
        Set<AnatEntitySimilarity> anatEntitySimilaritiesFromCellTypeFilter =
                new HashSet<>(anatEntitySimilaritiesFromAnatFilter);
        if (conditionFilter != null) {
            if (!conditionFilter.getAnatEntityIds().isEmpty()) {
                // we keep anat. entity similarities with at least one anat. entity from condition filters
                anatEntitySimilaritiesFromAnatFilter = anatEntitySimilaritiesFromAnatFilter.stream()
                    .filter(s -> s.getSourceAnatEntities().stream()
                            .anyMatch(ae -> conditionFilter.getAnatEntityIds().contains(ae.getId())))
                    .collect(Collectors.toSet());
            }
            if (!conditionFilter.getCellTypeIds().isEmpty()) {
                anatEntitySimilaritiesFromCellTypeFilter = anatEntitySimilaritiesFromCellTypeFilter.stream()
                    .filter(s -> s.getSourceAnatEntities().stream()
                            .anyMatch(ae -> conditionFilter.getCellTypeIds().contains(ae.getId())))
                    .collect(Collectors.toSet());
            }
        }

        // Build a new condition filter based on retrieved anat. entity similarities
        // For non-transitive similarity relations, an AnatEntity
        // could be part of several AnatEntitySimilaritys.
        // It should not be the case in most cases for transitive similarity relations
        // (see method AnatEntitySimilarityService.getValidMultipleEntityAnnotations)
        Map<AnatEntity, Set<AnatEntitySimilarity>> similaritiesByAnatEntityFromAnatFilter =
                anatEntitySimilaritiesFromAnatFilter.stream()
                        .flatMap(sim -> sim.getSourceAnatEntities().stream()
                                .map(ae -> new SimpleEntry<>(ae, sim)))
                        .collect(Collectors.groupingBy(SimpleEntry::getKey,
                                Collectors.mapping(SimpleEntry::getValue, Collectors.toSet())));
        Set<String> allAnatEntityIds = similaritiesByAnatEntityFromAnatFilter.keySet().stream()
                .map(Entity::getId)
                .collect(Collectors.toSet());
        Map<AnatEntity, Set<AnatEntitySimilarity>> similaritiesByAnatEntityFromCellTypeFilter =
                anatEntitySimilaritiesFromCellTypeFilter.stream()
                        .flatMap(sim -> sim.getSourceAnatEntities().stream()
                                .map(ae -> new SimpleEntry<>(ae, sim)))
                        .collect(Collectors.groupingBy(SimpleEntry::getKey,
                                Collectors.mapping(SimpleEntry::getValue, Collectors.toSet())));
        Set<String> allCellTypeIds = similaritiesByAnatEntityFromCellTypeFilter.keySet().stream()
                .map(Entity::getId)
                .collect(Collectors.toSet());
        ConditionFilter newConditionFilter = new ConditionFilter(allAnatEntityIds, null,
                allCellTypeIds, null, null,
                //filtering on observed conditions
                EnumSet.of(CallService.Attribute.ANAT_ENTITY_ID, CallService.Attribute.CELL_TYPE_ID));

        Map<ExpressionSummary, SummaryQuality> summaryCallTypeQualityFilter = new HashMap<>();
        summaryCallTypeQualityFilter.put(ExpressionSummary.EXPRESSED, SummaryQuality.BRONZE);
        summaryCallTypeQualityFilter.put(ExpressionSummary.NOT_EXPRESSED, SummaryQuality.BRONZE);

        // Build a new ExpressionCallFilter to use the ConditionFilter with similar anat. entities
        ExpressionCallFilter expressionCallFilter = new ExpressionCallFilter(
                summaryCallTypeQualityFilter, clnGeneFilters,
                Collections.singleton(newConditionFilter), null, null);

        // Define an order to be able to use an ElementGroupFromListSpliterator
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering =
                new LinkedHashMap<>();
        serviceOrdering.put(CallService.OrderingAttribute.GENE_ID, Service.Direction.ASC);
        
        // Retrieve ExpressionCalls
        Stream<ExpressionCall> callStream = callService.loadExpressionCalls(
                expressionCallFilter,
                EnumSet.of(CallService.Attribute.GENE, CallService.Attribute.ANAT_ENTITY_ID,
                        CallService.Attribute.CELL_TYPE_ID, CallService.Attribute.CALL_TYPE, 
                        CallService.Attribute.OBSERVED_DATA, CallService.Attribute.EXPRESSION_SCORE),
                serviceOrdering);

        Stream<List<ExpressionCall>> callsByGene = StreamSupport.stream(
                new ElementGroupFromListSpliterator<>(callStream, Call::getGene, Gene.COMPARATOR), false);

        // Build SimilarityExpressionCalls for each Gene/AnatEntitySimilarity
        Stream<SimilarityExpressionCall> similarityExpressionCallStream =
                callsByGene.flatMap(callList -> {
                    //As of Bgee 15.0, since we can have post-composition of anat. entity and cell type,
                    //the key of the Map changes from AnatEntitySimilarity to MultiSpeciesCondition
                    LinkedHashMap<MultiSpeciesCondition, List<ExpressionCall>> callsPerSimilarity =
                            callList.stream()
                            .flatMap(c -> similaritiesByAnatEntityFromAnatFilter
                                    .get(c.getCondition().getAnatEntity()).stream()
                                    .flatMap(anatSim -> similaritiesByAnatEntityFromCellTypeFilter
                                        .get(c.getCondition().getCellType()).stream()
                                        .map(cellTypeSim -> new AbstractMap.SimpleEntry<>(
                                            new MultiSpeciesCondition(anatSim, null, cellTypeSim, null),
                                            new ArrayList<>(Arrays.asList(c))))))
                            .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(),
                                    (v1, v2) -> {v1.addAll(v2); return v1;},
                                    //Need compiler hint on my Eclipse version
                                    () -> new LinkedHashMap<MultiSpeciesCondition, List<ExpressionCall>>()));

                    Gene gene = callList.get(0).getGene();
                    return callsPerSimilarity.entrySet().stream().map(e -> {
                        MultiSpeciesCondition cond = e.getKey();
                        boolean hasExpression = e.getValue().stream()
                                .anyMatch(c -> ExpressionSummary.EXPRESSED.equals(c.getSummaryCallType()));
                        return new SimilarityExpressionCall(gene, cond, e.getValue(),
                                hasExpression? ExpressionSummary.EXPRESSED: ExpressionSummary.NOT_EXPRESSED);
                    });
                });
        return log.traceExit(similarityExpressionCallStream);

    }

//    /**
//     * Retrieves gene expression calls for any arbitrary group of genes,
//     * and not only for orthologous genes.
//     *
//     * @param taxonId       An {@code int} that is the NCBI ID of the taxon for which 
//     * 	                    calls should be retrieved.
//     * @param callFilter    An {@code ExpressionCallFilter} allowing to filter
//     *                      retrieving of expression calls.
//     * @param onlyTrusted   A {@code boolean} defining whether results should be restricted 
//     *                      to "trusted" annotations.
//     *                      If {@code true}, only trusted annotations are returned.
//     * @return              The {@code Stream} of {@code MultiSpeciesCall}s that are 
//     *                          the multi-species expression calls for the requested parameters.
//     */
//    public Stream<SimilarityExpressionCall> loadSimilarityExpressionCalls(int taxonId,
//            ExpressionCallFilter callFilter, boolean onlyTrusted) {
//        log.traceEntry("{}, {}, {}", taxonId, callFilter, onlyTrusted);
//
//        if (callFilter.getConditionFilters() == null) {
//            throw log.throwing(new IllegalArgumentException("Provided conditionFilters should not be null"));
//        }
//        if (callFilter.getConditionFilters().stream()
//                .anyMatch(f -> f.getDevStageIds() != null && !f.getDevStageIds().isEmpty())) {
//            throw log.throwing(new UnsupportedOperationException(
//                    "Dev. stages are not managed to retrieve SimilarityExpressionCalls"));
//        }
//        
//        // Retrieve AnatEntitySimilarity from the provided taxon
//        final Set<String> providedAnatEntityIds = callFilter.getConditionFilters().stream()
//                .flatMap(f -> f.getAnatEntityIds().stream())
//                .collect(Collectors.toSet());
//        Set<AnatEntitySimilarity> anatEntitySimilarities = anatEntitySimilarityService
//                .loadPositiveAnatEntitySimilarities(taxonId, onlyTrusted).stream()
//                // we keep anat. entity similarities with at least one anat. entity from condition filters
//                .filter(s -> s.getSourceAnatEntities().stream()
//                        .anyMatch(ae -> providedAnatEntityIds.contains(ae.getId())))
//                .collect(Collectors.toSet());
//
//        // Build the condition filter based on the AnatEntitySimilarity
//        Set<String> retrievedAnatEntityIds = anatEntitySimilarities.stream()
//                .map(AnatEntitySimilarity::getSourceAnatEntities)
//                .flatMap(Set::stream)
//                .map(Entity::getId)
//                .collect(Collectors.toSet());
//        // FIXME not sure we can have only one condition filter if there are several ones in provided callFilter
//        // FIXME: We can simply remove from each ConditionFilter any anat. entity ID not present
//        // in the AnatEntitySimilarities.
//        ConditionFilter newConditionFilter = new ConditionFilter(retrievedAnatEntityIds, null, retrievedAnatEntityIds, null, null);
//
//        // Build a new ExpressionCallFilter to use the ConditionFilter with similar anat. entities
//        // XXX: I don't see why we pass to the method an ExpressionCallFilter instead of GeneFilters and ConditionFilter
//        ExpressionCallFilter updatedCallFilter = new ExpressionCallFilter(
//                callFilter.getSummaryCallTypeQualityFilter(),
//                callFilter.getGeneFilters(),
//                Collections.singletonList(newConditionFilter),
//                callFilter.getDataTypeFilters(), callFilter.getCallObservedData(),
//                callFilter.getAnatEntityObservedData(), callFilter.getDevStageObservedData(),
//                callFilter.getCellTypeObservedData(), callFilter.getSexObservedData(),
//                callFilter.getStrainObservedData());
//
//        // XXX from here it's a duplicate of the previous method.
//        // Define an order to be able to use an ElementGroupFromListSpliterator
//        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering =
//                new LinkedHashMap<>();
//        serviceOrdering.put(CallService.OrderingAttribute.GENE_ID, Service.Direction.ASC);
//
//        // Retrieve ExpressionCalls
//        Stream<ExpressionCall> callStream = callService.loadExpressionCalls(
//                updatedCallFilter,
//                EnumSet.of(CallService.Attribute.GENE, CallService.Attribute.ANAT_ENTITY_ID,
//                        CallService.Attribute.CALL_TYPE),
//                serviceOrdering);
//
//        Stream<List<ExpressionCall>> callsByGene = StreamSupport.stream(
//                new ElementGroupFromListSpliterator<>(callStream, Call::getGene, Gene.COMPARATOR), false);
//
//        // Build SimilarityExpressionCalls for each Gene/AnatEntitySimilarity
//        Stream<SimilarityExpressionCall> similarityExpressionCallStream =
//                callsByGene.flatMap(callList -> {
//                    Gene gene = callList.get(0).getGene();
//                    return anatEntitySimilarities.stream()
//                            .map(anatEntitySimilarity -> {
//                                MultiSpeciesCondition cond = new MultiSpeciesCondition(anatEntitySimilarity, null);
//                                List<ExpressionCall> filteredCalls = callList.stream()
//                                        .filter(c -> anatEntitySimilarity.getSourceAnatEntities().contains(c.getCondition().getAnatEntity()))
//                                        .collect(Collectors.toList());
//                                return new SimilarityExpressionCall(gene, cond, filteredCalls, null);
//                            }).filter(s -> !s.getCalls().isEmpty());
//                });
//        return log.traceExit(similarityExpressionCallStream);
//    }

    //TODO: equivalent method accepting ExpressionCallFilter
    //XXX: Maybe we need a DataPropagation attribute in SimilarityExpressionCall,
    //otherwise here we can retrieve calls in conditions with no observed data for any of the genes
    //(as opposed to the single species analysis, where only conditions with observed data
    //for at least one gene are retrieved)
    public MultiSpeciesExprAnalysis loadMultiSpeciesExprAnalysis(Collection<Gene> requestedGenes) {
        log.traceEntry("{}", requestedGenes);
        if (requestedGenes == null || requestedGenes.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Some genes must be provided"));
        }
        Set<Gene> clonedGenes = new HashSet<>(requestedGenes);

        Set<Species> species = clonedGenes.stream().map(g -> g.getSpecies()).collect(Collectors.toSet());
        if (species.size() <= 1) {
            throw log.throwing(new IllegalArgumentException(
                    "This method is for comparing the expression of genes between several species"));
        }
        Set<GeneFilter> geneFilters = convertGenesToGeneFilters(clonedGenes);
        int lcaId = this.getServiceFactory().getTaxonService().loadLeastCommonAncestor(
                species.stream().map(s -> s.getId()).collect(Collectors.toSet())
                ).getId();


        Stream<SimilarityExpressionCall> callStream = this.loadSimilarityExpressionCalls(
                lcaId, geneFilters, null, false);
        Map<MultiSpeciesCondition, MultiGeneExprCounts> condToCounts = callStream
                //produces a Map<MultiSpeciesCondition, List<SimilarityExpressionCall>>
                .collect(Collectors.groupingBy(c -> c.getMultiSpeciesCondition()))
                //streaming Entry<MultiSpeciesCondition, List<SimilarityExpressionCall>>
                .entrySet().stream()
                //Keep only conditions where at least one gene has observed data in it
                //As of Bgee 15.0, because of the way we manage call propagation along
                //condition parameters now, we disable this filter
//                .filter(e -> e.getValue().stream().anyMatch(sc -> sc.getCalls().stream()
//                        .anyMatch(c -> Boolean.TRUE.equals(c.getDataPropagation().isIncludingObservedData()))))
                //mapping Entry<MultiSpeciesCondition, List<SimilarityExpressionCall>>
                //to Entry<MultiSpeciesCondition, MultiGeneExprCounts>
                .map(e -> {
                    List<SimilarityExpressionCall> list = e.getValue();
                    Map<ExpressionSummary, Collection<Gene>> callTypeToGenes = list.stream()
                            .collect(Collectors.toMap(
                                    c -> c.getSummaryCallType(),
                                    c -> new HashSet<>(Arrays.asList(c.getGene())),
                                    (v1, v2) -> {v1.addAll(v2); return v1;}));
                    Set<Gene> genesWithData = list.stream().map(c -> c.getGene()).collect(Collectors.toSet());
                    Set<Gene> genesWithNoData = new HashSet<>(clonedGenes);
                    genesWithNoData.removeAll(genesWithData);

                    //For each gene with an expression score, we find the max expression score
                    Map<Gene, Optional<ExpressionLevelInfo>> geneToMaxExprScoreOptional = list.stream()
                    .flatMap(sc -> sc.getCalls().stream())
                    .filter(c -> c.getExpressionScore() != null)
                    .collect(Collectors.groupingBy(c -> c.getGene(),
                            Collectors.mapping(c -> c.getExpressionLevelInfo(),
                                    //Need a compiler hint with my local version of Java
                                    Collectors.maxBy(Comparator.comparing(eli -> eli.getExpressionScore())))));
                    //We insert in the Map all genes with data, with a null expr score if they don't have one
                    Map<Gene, ExpressionLevelInfo> geneToMaxExprScore = genesWithData.stream()
                    .map(g -> new AbstractMap.SimpleEntry<>(g, geneToMaxExprScoreOptional.containsKey(g)? geneToMaxExprScoreOptional.get(g).get(): null))
                    //Collectors.toMap does not accept null values,
                    //see https://stackoverflow.com/a/24634007/1768736
                    .collect(HashMap::new, (m, v) -> m.put(v.getKey(), v.getValue()), Map::putAll);
 
                    return new AbstractMap.SimpleEntry<>(e.getKey(),
                            new MultiGeneExprCounts(callTypeToGenes, genesWithNoData, geneToMaxExprScore));
                })
                //And we create the final Map condToCounts
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

        return log.traceExit(new MultiSpeciesExprAnalysis(clonedGenes, condToCounts));
    }
    //TODO: Once the method loadSimilarityExpressionCalls accepting an ExpressionCallFiter
    //will be ready, add a method to accept an ExpressionCallFilter rather than geneFilters,
    //as in CallService
}