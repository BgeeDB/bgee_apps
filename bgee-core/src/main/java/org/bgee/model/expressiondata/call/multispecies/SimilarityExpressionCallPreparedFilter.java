package org.bgee.model.expressiondata.call.multispecies;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.multispemapping.AnatEntitySimilarity;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.species.Taxon;

/**
 * Immutable context prepared from a {@link SimilarityExpressionCallFilter}, used internally
 * by {@link SimilarityExpressionCallLoader}.
 * 
 * @author  Harald Detering
 * @version Bgee 16, Jul. 2026
 * @since   Bgee 16, Jul. 2026
 */
final class SimilarityExpressionCallPreparedFilter {

    private final SimilarityExpressionCallFilter sourceFilter;
    private final List<AnatEntitySimilarity> orderedSimilarities;
    private final Set<AnatEntitySimilarity> positiveSimilarities;
    private final Map<AnatEntity, Set<AnatEntitySimilarity>> similaritiesByAnatEntity;
    private final Set<String> userAnatEntityIds;
    private final Set<String> userCellTypeIds;
    private final List<GeneFilter> orderedGeneFilters;
    private final Map<ExpressionSummary, SummaryQuality> summaryCallTypeQualityFilter;
    private final Taxon requestedTaxon;
    private final Ontology<Taxon, Integer> taxonOntology;
    private final Set<String> globalAnatEntityIds;
    private final Set<String> globalCellTypeIds;

    SimilarityExpressionCallPreparedFilter(SimilarityExpressionCallFilter sourceFilter,
            List<AnatEntitySimilarity> orderedSimilarities, Set<AnatEntitySimilarity> positiveSimilarities,
            Map<AnatEntity, Set<AnatEntitySimilarity>> similaritiesByAnatEntity,
            Set<String> userAnatEntityIds, Set<String> userCellTypeIds,
            List<GeneFilter> orderedGeneFilters,
            Map<ExpressionSummary, SummaryQuality> summaryCallTypeQualityFilter,
            Taxon requestedTaxon, Ontology<Taxon, Integer> taxonOntology,
            Set<String> globalAnatEntityIds, Set<String> globalCellTypeIds) {
        this.sourceFilter = sourceFilter;
        this.orderedSimilarities = Collections.unmodifiableList(orderedSimilarities);
        this.positiveSimilarities = Collections.unmodifiableSet(positiveSimilarities);
        this.similaritiesByAnatEntity = Collections.unmodifiableMap(similaritiesByAnatEntity);
        this.userAnatEntityIds = Collections.unmodifiableSet(userAnatEntityIds);
        this.userCellTypeIds = Collections.unmodifiableSet(userCellTypeIds);
        this.orderedGeneFilters = Collections.unmodifiableList(orderedGeneFilters);
        this.summaryCallTypeQualityFilter = Collections.unmodifiableMap(summaryCallTypeQualityFilter);
        this.requestedTaxon = requestedTaxon;
        this.taxonOntology = taxonOntology;
        this.globalAnatEntityIds = Collections.unmodifiableSet(globalAnatEntityIds);
        this.globalCellTypeIds = Collections.unmodifiableSet(globalCellTypeIds);
    }

    SimilarityExpressionCallFilter getSourceFilter() {
        return sourceFilter;
    }

    List<AnatEntitySimilarity> getOrderedSimilarities() {
        return orderedSimilarities;
    }

    Set<AnatEntitySimilarity> getPositiveSimilarities() {
        return positiveSimilarities;
    }

    Map<AnatEntity, Set<AnatEntitySimilarity>> getSimilaritiesByAnatEntity() {
        return similaritiesByAnatEntity;
    }

    Set<String> getUserAnatEntityIds() {
        return userAnatEntityIds;
    }

    Set<String> getUserCellTypeIds() {
        return userCellTypeIds;
    }

    List<GeneFilter> getOrderedGeneFilters() {
        return orderedGeneFilters;
    }

    Map<ExpressionSummary, SummaryQuality> getSummaryCallTypeQualityFilter() {
        return summaryCallTypeQualityFilter;
    }

    Taxon getRequestedTaxon() {
        return requestedTaxon;
    }

    Ontology<Taxon, Integer> getTaxonOntology() {
        return taxonOntology;
    }

    Set<String> getGlobalAnatEntityIds() {
        return globalAnatEntityIds;
    }

    Set<String> getGlobalCellTypeIds() {
        return globalCellTypeIds;
    }
}
