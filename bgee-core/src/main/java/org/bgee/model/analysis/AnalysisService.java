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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntitySimilarity;
import org.bgee.model.anatdev.DevStageSimilarity;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.ConditionFilter;
import org.bgee.model.expressiondata.MultiSpeciesCall;
import org.bgee.model.expressiondata.baseelements.CallType.Expression;
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
 * @version Bgee 13, Nov. 2016
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
     * @param speciesIds    A {@code Collection} of {@code String}s that are the IDs of species
     *                      in which the gene orthologs should be found.
     * @return              The {@code LinkedHashMap} where keys are {@code String}s corresponding
     *                      to IDs of taxa ordered from closest to farthest of {@code gene},
     *                      the associated values being {@code Set} of {@code MultiSpeciesCall}s
     *                      corresponding to multi-species calls.
     */
    public LinkedHashMap<String, Set<MultiSpeciesCall<ExpressionCall>>> loadMultiSpeciesExpressionCalls(
            Gene gene, Collection<String> speciesIds) {
        log.entry(gene, speciesIds);
        
        if (gene == null) {
            throw new IllegalArgumentException("Provided gene should not be null");
        }
        if (gene.getSpeciesId() == null || gene.getSpecies() == null) {
            throw new IllegalArgumentException("Expecting species info in provided gene:" + gene);
        }
        
        final Set<String> clonedSpeIds = Collections.unmodifiableSet(
                speciesIds == null? new HashSet<>(): new HashSet<>(speciesIds));
        if (!clonedSpeIds.isEmpty() && !clonedSpeIds.contains(gene.getSpeciesId())) {
            throw new IllegalArgumentException("Gene species (" + gene.getSpeciesId() +
                    ") is not in provided species (" + speciesIds +")");
        }
        
        // Get all relevant taxa from the species
        MultiSpeciesOntology<Taxon> taxonOnt = this.getServiceFactory().getOntologyService()
                .getTaxonOntology(clonedSpeIds, null, true, false);
        if (taxonOnt.getElement(gene.getSpecies().getParentTaxonId()) == null) {
            throw new IllegalStateException("Taxon ID " + gene.getSpecies().getParentTaxonId() +
                    "not found in retrieved taxonomy");
        }
        List<String> taxonIds = taxonOnt.getOrderedAncestors(
                    taxonOnt.getElement(gene.getSpecies().getParentTaxonId())).stream()
                .map(t -> t.getId())
                .collect(Collectors.toList());
        
        LinkedHashMap<String, Set<MultiSpeciesCall<ExpressionCall>>> taxaToCalls = new LinkedHashMap<>();
        
        LinkedHashMap<CallService.OrderingAttribute, Direction> orderAttrs = new LinkedHashMap<>();
        orderAttrs.put(CallService.OrderingAttribute.GENE_ID, Direction.ASC);

        for (String taxonId : taxonIds) {
            log.trace("Starting generation of multi-species calls for taxon ID {}", taxonId);
            // Retrieve homologous organ groups with gene IDs
            Map<String, Set<String>> omaToGeneIds = this.getServiceFactory().getGeneService()
                    .getOrthologs(taxonId, clonedSpeIds);
            log.trace("Homologous organ groups with gene IDs: {}", omaToGeneIds);
            // XXX filter by Gene should be done in GeneService or DAO?
            Set<String> orthologousGeneIds = omaToGeneIds.entrySet().stream()
                    .filter(e -> e.getValue().contains(gene.getId()))
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
            GeneFilter geneFilter = new GeneFilter(orthologousGeneIds);
            Set<ConditionFilter> conditionFilters = new HashSet<>();
            conditionFilters.add(new ConditionFilter(anatEntityIds, devStageIds));
            Set<ExpressionCallData> callDataFilters = new HashSet<>(); // cannot be null in ExpressionCallFilter
            log.warn("Only expressed calls are retrieved");
            callDataFilters.add(new ExpressionCallData(Expression.EXPRESSED));
            ExpressionCallFilter callFilter = new ExpressionCallFilter(
                    geneFilter, conditionFilters, callDataFilters);
            
            // For each species, we load propagated and reconciled calls
            // (filtered by previous filters)
            Set<ExpressionCall> calls = new HashSet<>();
            for (String spId: clonedSpeIds) {
                // FIXME do propagation???
                Set<ExpressionCall> currentCalls = this.getServiceFactory().getCallService().
                    loadExpressionCalls(spId, callFilter, null, orderAttrs, false).collect(Collectors.toSet());
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
     * @param taxonId                   A {@code String} that is the taxon ID to use to build  
     *                                  {@code MultiSpeciesCall}s.
     * @param omaToGeneIds              A {@code Map} where keys are {@code String}s corresponding
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
    private Set<MultiSpeciesCall<ExpressionCall>> groupCalls(String taxonId,
            Map<String, Set<String>> omaToGeneIds, Set<AnatEntitySimilarity> anatEntitySimilarities,
            Set<DevStageSimilarity> devStageSimilarities, Set<ExpressionCall> calls)
                    throws IllegalArgumentException {
        log.entry(taxonId, omaToGeneIds, anatEntitySimilarities, devStageSimilarities, calls);
        
        Map<MultiSpeciesCall<ExpressionCall>, Set<ExpressionCall>> multiSpCallToCalls = new HashMap<>();
        
        for (ExpressionCall call: calls) {
            log.trace("Iteration expr call {}", call);
            
            if (call.getCondition() == null) {
                throw new IllegalArgumentException("No condition for " + call);
            }
            if (call.getGeneId() == null) {
                throw new IllegalArgumentException("No gene ID for " + call);
            }
            Set<AnatEntitySimilarity> curAESimilarities = anatEntitySimilarities.stream()
                    .filter(s -> s.getAnatEntityIds().contains(call.getCondition().getAnatEntityId()))
                    .collect(Collectors.toSet());
            if (curAESimilarities.size() > 1) {
                throw new IllegalArgumentException(
                        "An anat. entity is contained in more than anat. entity similarity groups: " +
                        call.getCondition().getAnatEntityId() + " found in " +
                                curAESimilarities.stream()
                                    .map(s -> s.getId())
                                    .collect(Collectors.toSet()));
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
                throw new IllegalArgumentException(
                        "A dev. stage is contained in more than one dev. stage similarity groups: " +
                        call.getCondition().getDevStageId() + " found in " +
                                curDSSimilarities.stream()
                                    .map(s -> s.getId())
                                    .collect(Collectors.toSet()));
            } else if (curDSSimilarities.size() == 0) {
                log.trace(call.getCondition().getDevStageId() + 
                        " found in any dev. entity similarity group");
                continue;
            }
            DevStageSimilarity dsSimilarity = curDSSimilarities.iterator().next();
            Set<String> omaNodeIds = omaToGeneIds.entrySet().stream()
                    .filter(e -> e.getValue().contains(call.getGeneId()))
                    .map(e -> e.getKey())
                    .collect(Collectors.toSet());
            if (omaNodeIds.size() > 1) {
                throw new IllegalArgumentException(
                        "A gene is contained in more than one OMA node ID: " +
                                call.getGeneId() + " found in " + omaNodeIds);
            } else if (omaNodeIds.size() == 0) {
                log.trace(call.getCondition().getDevStageId() + 
                        " found in any dev. entity similarity group");
                continue;
            }
            String omaNodeId = omaNodeIds.iterator().next();

            MultiSpeciesCall<ExpressionCall> multiSpeciesCall = new MultiSpeciesCall<ExpressionCall>(
                    aeSimilarity, dsSimilarity, taxonId, omaNodeId, omaToGeneIds.get(omaNodeId), null,
                    null, this.getServiceFactory());
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
