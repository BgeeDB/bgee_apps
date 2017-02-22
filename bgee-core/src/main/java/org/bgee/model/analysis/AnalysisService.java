package org.bgee.model.analysis;

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
import org.bgee.model.expressiondata.MultiSpeciesCall;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.ontology.MultiSpeciesOntology;
import org.bgee.model.species.Taxon;

/**
 * A {@link Service} to obtain {@link MultiSpeciesCall} objects. 
 * Users should use the {@link org.bgee.model.ServiceFactory} to obtain {@code CallService}s.
 * 
 * @author  Philippe Moret
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Feb. 2017
 * @since   Bgee 13, May 2016
 */
// XXX: why not call it MultiSpeciesCallService?
public class AnalysisService extends Service {

    private static final Logger log = LogManager.getLogger(AnalysisService.class.getName());

    /**
     * @param serviceFactory            The {@code ServiceFactory} to be used to obtain
     *                                  {@code Service}s and {@code DAOManager}.
     * @throws IllegalArgumentException If {@code serviceFactory} is {@code null}.
     */
    public AnalysisService(ServiceFactory serviceFactory) {
        super(serviceFactory);
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
        if (gene.getSpeciesId() == null || gene.getSpecies() == null) {
            throw log.throwing(new IllegalArgumentException("Expecting species info in provided gene:" + gene));
        }
        
        final Set<Integer> clonedSpeIds = Collections.unmodifiableSet(
                speciesIds == null? new HashSet<>(): new HashSet<>(speciesIds));
        if (!clonedSpeIds.isEmpty() && !clonedSpeIds.contains(gene.getSpeciesId())) {
            throw log.throwing(new IllegalArgumentException("Gene species (" + gene.getSpeciesId() +
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
        List<Integer> taxonIds = taxonOnt.getOrderedAncestors(
                    taxonOnt.getElement(parentTaxonId)).stream()
                .map(t -> t.getId())
                .collect(Collectors.toList());
        
        LinkedHashMap<Integer, Set<MultiSpeciesCall<ExpressionCall>>> taxaToCalls = new LinkedHashMap<>();
        
        LinkedHashMap<CallService.OrderingAttribute, Direction> orderAttrs = new LinkedHashMap<>();
        orderAttrs.put(CallService.OrderingAttribute.GENE_ID, Direction.ASC);

        for (Integer taxonId : taxonIds) {
            log.trace("Starting generation of multi-species calls for taxon ID {}", taxonId);
            // Retrieve homologous organ groups with gene IDs
            Map<Integer, Set<Integer>> omaToGeneIds = this.getServiceFactory().getGeneService()
                    .getOrthologs(taxonId, clonedSpeIds);
            log.trace("Homologous organ groups with gene IDs: {}", omaToGeneIds);
            // XXX filter by Gene should be done in GeneService or DAO?
            Set<Integer> orthologousGeneIds = omaToGeneIds.entrySet().stream()
                    .filter(e -> e.getValue().contains(gene.getEnsemblGeneId()))
                    .map(e -> e.getValue())
                    .flatMap(Set::stream)
                    .collect(Collectors.toSet());

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
            conditionFilters.add(new ConditionFilter(anatEntityIds, devStageIds));
            log.warn("Only expressed calls are retrieved");
            ExpressionCallFilter callFilter = new ExpressionCallFilter(
                // FIXME use GeneFilter. Be careful between bgee and Ensembl gene IDs, 
                // Ensembl gene IDs are not unique.
                null,
//                new GeneFilter(orthologousGeneIds),
                conditionFilters,
                null, null,
                ExpressionSummary.EXPRESSED,
                new DataPropagation());
            
            // For each species, we load propagated and reconciled calls
            // (filtered by previous filters)
            Set<ExpressionCall> calls = new HashSet<>();
            for (Integer spId: clonedSpeIds) {
                // FIXME do propagation???
                Set<ExpressionCall> currentCalls = this.getServiceFactory().getCallService().
                    loadExpressionCalls(spId, callFilter, null, orderAttrs).collect(Collectors.toSet());
                calls.addAll(currentCalls);
            }

            taxaToCalls.put(taxonId, this.groupCalls(taxonId,
                    omaToGeneIds, anatEntitySimilarities, devStageSimilarities, calls));
            log.trace("Done generation of multi-species calls for taxon ID {}", taxonId);
        }
        return taxaToCalls;
    }

    /**
     * Group {@code ExpressionCall}s into {@code MultiSpeciesCall}s.
     * 
     * @param taxonId                   An {@code Integer} that is the taxon ID to use to build  
     *                                  {@code MultiSpeciesCall}s.
     * @param omaToGeneIds              A {@code Map} where keys are {@code Integer}s corresponding
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
            Map<Integer, Set<Integer>> omaToGeneIds, Set<AnatEntitySimilarity> anatEntitySimilarities,
            Set<DevStageSimilarity> devStageSimilarities, Set<ExpressionCall> calls)
                    throws IllegalArgumentException {
        log.entry(taxonId, omaToGeneIds, anatEntitySimilarities, devStageSimilarities, calls);
        
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
            Set<Integer> omaNodeIds = omaToGeneIds.entrySet().stream()
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
        
        return log.exit(multiSpCallToCalls.entrySet().stream()
                .map(e -> {
                    MultiSpeciesCall<ExpressionCall> call = e.getKey();
                    return this.computeConservationScore(new MultiSpeciesCall<ExpressionCall>(
                            call.getAnatEntitySimilarity(), call.getDevStageSimilarity(),
                            call.getTaxonId(), call.getOMANodeId(), call.getOrthologousGeneIds(),
                            new HashSet<>(e.getValue()), null, this.getServiceFactory()));
                })
                .collect(Collectors.toSet()));
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
        log.warn("Conservation score is not calculated: implementation of computation must be done");
        return log.exit(new MultiSpeciesCall<ExpressionCall>(
                inputCall.getAnatEntitySimilarity(), inputCall.getDevStageSimilarity(),
                inputCall.getTaxonId(), inputCall.getOMANodeId(), inputCall.getOrthologousGeneIds(),
                inputCall.getCalls(), conservationScore, this.getServiceFactory()));
    }
}
