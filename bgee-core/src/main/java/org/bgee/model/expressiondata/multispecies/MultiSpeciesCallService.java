package org.bgee.model.expressiondata.multispecies;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntitySimilarity;
import org.bgee.model.anatdev.DevStageSimilarity;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.ConditionFilter;
import org.bgee.model.gene.Gene;
import org.bgee.model.ontology.MultiSpeciesOntology;
import org.bgee.model.species.Taxon;
import org.bgee.model.species.TaxonomyFilter;

/**
 * A {@link Service} to obtain {@link MultiSpeciesCall} objects. 
 * Users should use the {@link org.bgee.model.ServiceFactory} to obtain {@code CallService}s.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Mar. 2017
 * @since   Bgee 13, May 2016
 */
public class MultiSpeciesCallService extends Service {
    private static final Logger log = LogManager.getLogger(MultiSpeciesCallService.class.getName());

    //XXX: certainly we need different Attributes, just an example
    public static enum Attribute implements Service.Attribute {
        GENE, ANAT_ENTITY_ID, DEV_STAGE_ID, CALL_TYPE,
        DATA_QUALITY, OBSERVED_DATA, GLOBAL_MEAN_RANK,
        EXPERIMENT_COUNTS, DATA_TYPE_RANK_INFO;
    }

    //XXX: certainly we need different OrderingAttributes, just an example
    public static enum OrderingAttribute implements Service.OrderingAttribute {
        GENE_ID, ANAT_ENTITY_ID, DEV_STAGE_ID, GLOBAL_RANK;
    }
    
    private final CallService callService;

    /**
     * @param serviceFactory            The {@code ServiceFactory} to be used to obtain
     *                                  {@code Service}s and {@code DAOManager}.
     * @throws IllegalArgumentException If {@code serviceFactory} is {@code null}.
     */
    public MultiSpeciesCallService(ServiceFactory serviceFactory) {
        super(serviceFactory);
        this.callService = this.getServiceFactory().getCallService();
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
    public Stream<MultiSpeciesCall<ExpressionCall>> loadMultiSpeciesCalls(TaxonomyFilter taxonomyFilter,
            Gene gene, ExpressionCallFilter callFilter, Collection<Attribute> attributes, 
            LinkedHashMap<OrderingAttribute, Service.Direction> orderingAttributes) {
        log.entry(taxonomyFilter, gene, callFilter, attributes, orderingAttributes);
        throw new UnsupportedOperationException("Not implemented");
    }
    /**
     * Allows to perform gene expression comparisons for any arbitrary group of genes,
     * and not only for orthologous genes, as in method {@link #loadMultiSpeciesCalls(TaxonomyFilter, Gene)}.
     *  
     * @param genes
     * @return
     */
    public Stream<MultiSpeciesCall<ExpressionCall>> loadMultiSpeciesCalls(TaxonomyFilter taxonomyFilter,
            Collection<Gene> genes, ExpressionCallFilter callFilter, Collection<Attribute> attributes, 
            LinkedHashMap<OrderingAttribute, Service.Direction> orderingAttributes) {
        log.entry(taxonomyFilter, genes, callFilter, attributes, orderingAttributes);
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Retrieve multi-species expression calls for a specific gene in provided species.
     * 
     * @param gene          A {@code Gene} that is the gene for which to return multi-species calls.
     * @param speciesIds    A {@code Collection} of {@code Integer}s that are the IDs of species
     *                      in which the gene orthologs should be found.
     * @return              The {@code LinkedHashMap} where keys are {@code Integer}s corresponding
     *                      to IDs of taxa ordered from closest to farthest of {@code gene},
     *                      the associated values being {@code Set} of {@code MultiSpeciesCall}s
     *                      corresponding to multi-species calls.
     */
    public LinkedHashMap<Integer, Set<MultiSpeciesCall<ExpressionCall>>> loadMultiSpeciesExpressionCalls(
            Gene gene, Collection<Integer> speciesIds) {
        log.entry(gene, speciesIds);
        
        if (gene == null) {
            throw log.throwing(new IllegalArgumentException("Provided gene should not be null"));
        }
        if (gene.getSpecies() == null || gene.getSpecies().getId() == null) {
            throw log.throwing(new IllegalArgumentException("Expecting species info in provided gene:" + gene));
        }
        
        final Set<Integer> clonedSpeIds = Collections.unmodifiableSet(
                speciesIds == null? new HashSet<>(): new HashSet<>(speciesIds));
        if (!clonedSpeIds.isEmpty() && !clonedSpeIds.contains(gene.getSpecies().getId())) {
            throw log.throwing(new IllegalArgumentException("Gene species (" + gene.getSpecies().getId() +
                    ") is not in provided species (" + speciesIds +")"));
        }
        
        // Get all relevant taxa from the species
        // FIXME: load all taxonomy ontology, should we filter it?
        MultiSpeciesOntology<Taxon, Integer> taxonOnt = this.getServiceFactory().getOntologyService()
                .getTaxonOntology();
        Integer parentTaxonId = gene.getSpecies().getParentTaxonId();
        if (taxonOnt.getElement(parentTaxonId) == null) {
            throw log.throwing(new IllegalStateException("Taxon ID " + gene.getSpecies().getParentTaxonId() +
                    "not found in retrieved taxonomy"));
        }
        
        LinkedHashMap<Integer, Set<MultiSpeciesCall<ExpressionCall>>> taxaToCalls = new LinkedHashMap<>();
        
        LinkedHashMap<CallService.OrderingAttribute, Direction> orderAttrs = new LinkedHashMap<>();
        orderAttrs.put(CallService.OrderingAttribute.GENE_ID, Direction.ASC);


        //XXX: the retrieval of the taxon IDs could be moved at the end of this method,
        //when you start iterating from the more precise taxon and to the root of the taxon ontology
        List<Integer> taxonIds = taxonOnt.getOrderedAncestors(
                taxonOnt.getElement(parentTaxonId)).stream()
            .map(t -> t.getId())
            .collect(Collectors.toList());

        //FIXME: getOrthologs should take a Gene, a Taxon and a collection of species,
        //and return Map<taxonId, Set<Gene>>, using the highest taxon ID
        int highestTaxonId = 0;// to implement
        Map<Integer, Set<Gene>> omaToGenes = this.getServiceFactory().getGeneService()
                .getOrthologs(highestTaxonId, clonedSpeIds);
        for (Integer taxonId : taxonIds) {
            log.trace("Starting generation of multi-species calls for taxon ID {}", taxonId);
            // Retrieve homologous organ groups with gene IDs
            log.trace("Homologous organ groups with genes: {}", omaToGenes);
//            Set<String> orthologousEnsemblGeneIds = omaToGenes.values().stream()
//                    .filter(geneSet -> geneSet.stream()
//                            .anyMatch(g -> gene.getEnsemblGeneId().equals(g.getEnsemblGeneId())))
//                    .flatMap(Set::stream)
//                    .map(g -> g.getEnsemblGeneId())
//                    .collect(Collectors.toSet());

            // Retrieve anat. entity similarities
            Set<AnatEntitySimilarity> anatEntitySimilarities = this.getServiceFactory()
                    .getAnatEntityService().loadAnatEntitySimilarities(taxonId, clonedSpeIds, true);
            log.trace("Anat. entity similarities: {}", anatEntitySimilarities);
            Set<String> anatEntityIds = anatEntitySimilarities.stream()
                    .map(s -> s.getAnatEntityIds()).flatMap(Set::stream).collect(Collectors.toSet());
            
            // Retrieve dev. stage similarities
            Set<DevStageSimilarity> devStageSimilarities = this.getServiceFactory() 
                    .getDevStageService().loadDevStageSimilarities(taxonId, clonedSpeIds);
            log.trace("Dev. stage similarities: {}", devStageSimilarities);
            Set<String> devStageIds = devStageSimilarities.stream()
                    .map(s -> s.getDevStageIds()).flatMap(Set::stream).collect(Collectors.toSet());

            // Build ExpressionCallFilter
            Set<ConditionFilter> conditionFilters = new HashSet<>();
            conditionFilters.add(new ConditionFilter(anatEntityIds, devStageIds, null));
            log.warn("Only expressed calls are retrieved");
            
//            // For each species, we load propagated and reconciled calls
//            Set<ExpressionCall> calls = new HashSet<>();
//            for (Integer spId: clonedSpeIds) {
//                //FIXME: adapt to new API
////                ExpressionCallFilter callFilter =
////                        new ExpressionCallFilter(
////                    new GeneFilter(spId, orthologousEnsemblGeneIds),
////                    conditionFilters,
////                    null,   // dataTypeFilter
////                    ExpressionSummary.EXPRESSED,
////                    null,   // SummaryQuality
////                    true);
////                // FIXME do propagation???
////                Set<ExpressionCall> currentCalls = this.getServiceFactory().getCallService().
////                    loadExpressionCalls(callFilter, null, orderAttrs).collect(Collectors.toSet());
////                calls.addAll(currentCalls);
//            }
            Set<ExpressionCall> calls = null; //to implement
            taxaToCalls.put(taxonId, this.groupCalls(taxonId,
                    omaToGenes, anatEntitySimilarities, devStageSimilarities, calls));
            log.trace("Done generation of multi-species calls for taxon ID {}", taxonId);
        }
        return taxaToCalls;
    }

    /**
     * Group {@code ExpressionCall}s into {@code MultiSpeciesCall}s.
     * 
     * @param taxonId                   An {@code Integer} that is the taxon ID to use to build  
     *                                  {@code MultiSpeciesCall}s.
     * @param omaToGenes                A {@code Map} where keys are {@code Integer}s corresponding
     *                                  to IDs of OMA nodes, the associated values being {@code Set}
     *                                  of {@code String}s corresponding to gene IDs.
     * @param anatEntitySimilarities    A {@code Set} of {@code AnatEntitySimilarity}s thats are 
     *                                  similarity groups of anatomical entities to use to build  
     *                                  {@code MultiSpeciesCall}s.
     * @param devStageSimilarities      A {@code Set} of {@code DevStageSimilarity}s thats are 
     *                                  similarity groups of developmental stages to use to build  
     *                                  {@code MultiSpeciesCall}s.
     * @param calls                     A {@code Set} of {@code ExpressionCall}s thats are 
     *                                  expression calls to use to build {@code MultiSpeciesCall}s.
     * @return                          The {@code Set} of {@code MultiSpeciesCall}s thats are 
     *                                  multi-species calls.
     * @throws IllegalArgumentException If an {@code ExpressionCall} has not all necessary data 
     *                                  or if similarity groups are incorrect.
     */
    // TODO to be added to ExpressionCallUtils see TODOs into ExpressionCall
    private Set<MultiSpeciesCall<ExpressionCall>> groupCalls(Integer taxonId,
            Map<Integer, Set<Gene>> omaToGenes, Set<AnatEntitySimilarity> anatEntitySimilarities,
            Set<DevStageSimilarity> devStageSimilarities, Set<ExpressionCall> calls)
                    throws IllegalArgumentException {
        log.entry(taxonId, omaToGenes, anatEntitySimilarities, devStageSimilarities, calls);
        
        Map<MultiSpeciesCall<ExpressionCall>, Set<ExpressionCall>> multiSpCallToCalls = new HashMap<>();
        
        for (ExpressionCall call: calls) {
            log.trace("Iteration expr call {}", call);
            
            if (call.getCondition() == null) {
                throw log.throwing(new IllegalArgumentException("No condition for " + call));
            }
            if (call.getGene() == null || StringUtils.isBlank(call.getGene().getEnsemblGeneId())) {
                throw log.throwing(new IllegalArgumentException("No gene ID for " + call));
            }
            Set<AnatEntitySimilarity> curAESimilarities = anatEntitySimilarities.stream()
                    .filter(s -> s.getAnatEntityIds().contains(call.getCondition().getAnatEntityId()))
                    .collect(Collectors.toSet());
            if (curAESimilarities.size() > 1) {
                throw log.throwing(new IllegalArgumentException(
                        "An anat. entity is contained in more than anat. entity similarity groups: " +
                        call.getCondition().getAnatEntityId() + " found in " +
                                curAESimilarities.stream()
                                    .map(s -> s.getId())
                                    .collect(Collectors.toSet())));
            } else if (curAESimilarities.size() == 0) {
                log.trace(call.getCondition().getAnatEntityId() +
                        " found in any anat. entity similarity group");
                continue;
            }
            AnatEntitySimilarity aeSimilarity = curAESimilarities.iterator().next();
            
            Set<DevStageSimilarity> curDSSimilarities = devStageSimilarities.stream()
                    .filter(s -> s.getDevStageIds().contains(call.getCondition().getDevStageId()))
                    .collect(Collectors.toSet());
            if (curDSSimilarities.size() > 1) {
                throw log.throwing(new IllegalArgumentException(
                        "A dev. stage is contained in more than one dev. stage similarity groups: " +
                        call.getCondition().getDevStageId() + " found in " +
                                curDSSimilarities.stream()
                                    .map(s -> s.getId())
                                    .collect(Collectors.toSet())));
            } else if (curDSSimilarities.size() == 0) {
                log.trace(call.getCondition().getDevStageId() + 
                        " found in any dev. entity similarity group");
                continue;
            }
            DevStageSimilarity dsSimilarity = curDSSimilarities.iterator().next();
            Set<Integer> omaNodeIds = omaToGenes.entrySet().stream()
                    .filter(e -> e.getValue().contains(call.getGene().getEnsemblGeneId()))
                    .map(e -> e.getKey())
                    .collect(Collectors.toSet());
            if (omaNodeIds.size() > 1) {
                throw log.throwing(new IllegalArgumentException(
                        "A gene is contained in more than one OMA node ID: " +
                                call.getGene().getEnsemblGeneId() + " found in " + omaNodeIds));
            } else if (omaNodeIds.size() == 0) {
                log.trace(call.getCondition().getDevStageId() + 
                        " found in any dev. entity similarity group");
                continue;
            }
            Integer omaNodeId = omaNodeIds.iterator().next();

            MultiSpeciesCall<ExpressionCall> multiSpeciesCall = null; 
//                new MultiSpeciesCall<ExpressionCall>(
//                    aeSimilarity, dsSimilarity, taxonId, omaNodeId, omaToGeneIds.get(omaNodeId), null,
//                    null, this.getServiceFactory());
            Set<ExpressionCall> associatedCalls = multiSpCallToCalls.get(multiSpeciesCall);
            if (associatedCalls == null) {
                log.trace("Create new map key: {}", multiSpeciesCall);
                associatedCalls = new HashSet<ExpressionCall>();
                multiSpCallToCalls.put(multiSpeciesCall, associatedCalls);
            }
            associatedCalls.add(call);
        }
        throw new UnsupportedOperationException("To continue");
//        return log.exit(multiSpCallToCalls.entrySet().stream()
//                .map(e -> {
//                    MultiSpeciesCall<ExpressionCall> call = e.getKey();
//                    return this.computeConservationScore(new MultiSpeciesCall<ExpressionCall>(
//                            call.getAnatEntitySimilarity(), call.getDevStageSimilarity(),
//                            call.getTaxonId(), call.getOMANodeId(), call.getOrthologousGeneIds(),
//                            new HashSet<>(e.getValue()), null, this.getServiceFactory()));
//                })
//                .collect(Collectors.toSet()));
    }
    
    /**
     * Compute conservation score of {@code inputCall}.
     * 
     * @param inputCall A {@code MultiSpeciesCall} that is the multi-species call expression calls
     *                  for which to compute conservation score.
     * @return          The {@code MultiSpeciesCall} that is the multi-species call expression calls
     *                  with computed conservation score.
     */
    public MultiSpeciesCall<ExpressionCall> computeConservationScore(MultiSpeciesCall<ExpressionCall> inputCall) {
        log.entry(inputCall);
        BigDecimal conservationScore = null;
        // FIXME computation of conservation score must be implemented.        
        throw new UnsupportedOperationException("To continue");
//        return log.exit(new MultiSpeciesCall<ExpressionCall>(
//                inputCall.getAnatEntitySimilarity(), inputCall.getDevStageSimilarity(),
//                inputCall.getTaxonId(), inputCall.getOMANodeId(), inputCall.getOrthologousGeneIds(),
//                inputCall.getCalls(), conservationScore, this.getServiceFactory()));
    }
}
