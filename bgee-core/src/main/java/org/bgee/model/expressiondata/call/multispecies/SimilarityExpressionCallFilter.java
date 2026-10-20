package org.bgee.model.expressiondata.call.multispecies;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.call.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.call.CallFilter;
import org.bgee.model.expressiondata.call.ConditionFilter2;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.gene.GeneFilter;

/**
 * A {@link CallFilter} to parameterize loading of {@link SimilarityExpressionCall2}s
 * through a {@link SimilarityExpressionCallLoader}.
 * <p>
 * Unlike {@link CallFilter.ExpressionCallFilter2}, this filter supports multiple
 * {@code GeneFilter}s (one per species).
 *
 * @author  Harald Detering
 * @version Bgee 16, Jul. 2026
 * @since   Bgee 16, Jul. 2026
 */
public class SimilarityExpressionCallFilter
extends CallFilter<ExpressionCallData, ExpressionSummary, ConditionFilter2> {

    private static final Logger log = LogManager.getLogger(SimilarityExpressionCallFilter.class.getName());

    private final int taxonId;
    private final boolean onlyTrusted;
    /**
     * {@code true} to keep only calls observed in the anatomical entity and cell type,
     * {@code false} to keep only calls that are not observed there, {@code null} for no
     * observation filter. Corresponds to the {@code observed_data} request parameter.
     */
    private final Boolean callObservedData;

    /**
     * @param taxonId           An {@code int} that is the NCBI ID of the taxon for which
     *                          calls should be retrieved. Must be strictly positive.
     * @param geneFilters       A non-empty {@code Collection} of {@code GeneFilter}s.
     *                          When empty gene filters are desired, they must be normalized
     *                          before constructing this filter (see
     *                          {@link MultiSpeciesCallService#loadSimilarityCallLoader(SimilarityExpressionCallFilter)}).
     * @param conditionFilters  A {@code Collection} of {@code ConditionFilter2}s, or
     *                          {@code null}/empty for no condition filtering.
     * @param onlyTrusted       A {@code boolean} defining whether results should be restricted
     *                          to trusted anatomical entity similarities.
     * @param summaryQuality    A {@code SummaryQuality} defining the minimum quality level
     *                          for expression calls to be included. If {@code null},
     *                          {@link SummaryQuality#BRONZE} is used.
     */
    public SimilarityExpressionCallFilter(int taxonId, Collection<GeneFilter> geneFilters,
            Collection<ConditionFilter2> conditionFilters, boolean onlyTrusted,
            SummaryQuality summaryQuality) throws IllegalArgumentException {
        this(taxonId, geneFilters, conditionFilters, onlyTrusted, summaryQuality, null);
    }

    /**
     * @param taxonId           An {@code int} that is the NCBI ID of the taxon for which
     *                          calls should be retrieved. Must be strictly positive.
     * @param geneFilters       A non-empty {@code Collection} of {@code GeneFilter}s.
     * @param conditionFilters  A {@code Collection} of {@code ConditionFilter2}s, or
     *                          {@code null}/empty for no condition filtering.
     * @param onlyTrusted       A {@code boolean} defining whether results should be restricted
     *                          to trusted anatomical entity similarities.
     * @param summaryQuality    A {@code SummaryQuality} defining the minimum quality level
     *                          for expression calls to be included. If {@code null},
     *                          {@link SummaryQuality#BRONZE} is used.
     * @param callObservedData  {@code true} to retrieve only calls observed in the anatomical
     *                          entity and cell type, {@code false} to retrieve only calls that
     *                          are not observed there (propagation only), or {@code null} to
     *                          apply no observation filter. Corresponds to the
     *                          {@code observed_data} request parameter.
     */
    public SimilarityExpressionCallFilter(int taxonId, Collection<GeneFilter> geneFilters,
            Collection<ConditionFilter2> conditionFilters, boolean onlyTrusted,
            SummaryQuality summaryQuality, Boolean callObservedData) throws IllegalArgumentException {
        this(taxonId, geneFilters, conditionFilters, null, onlyTrusted, summaryQuality,
                callObservedData);
    }

    /**
     * @param taxonId           An {@code int} that is the NCBI ID of the taxon for which
     *                          calls should be retrieved. Must be strictly positive.
     * @param geneFilters       A non-empty {@code Collection} of {@code GeneFilter}s.
     * @param conditionFilters  A {@code Collection} of {@code ConditionFilter2}s, or
     *                          {@code null}/empty for no condition filtering.
     * @param dataTypeFilter    A {@code Collection} of {@code DataType}s to consider
     *                          to retrieve expression calls. If {@code null} or empty,
     *                          all data types are considered. Corresponds to the
     *                          {@code data_type} request parameter.
     * @param onlyTrusted       A {@code boolean} defining whether results should be restricted
     *                          to trusted anatomical entity similarities.
     * @param summaryQuality    A {@code SummaryQuality} defining the minimum quality level
     *                          for expression calls to be included. If {@code null},
     *                          {@link SummaryQuality#BRONZE} is used.
     * @param callObservedData  {@code true} to retrieve only calls observed in the anatomical
     *                          entity and cell type, {@code false} to retrieve only calls that
     *                          are not observed there (propagation only), or {@code null} to
     *                          apply no observation filter. Corresponds to the
     *                          {@code observed_data} request parameter.
     */
    public SimilarityExpressionCallFilter(int taxonId, Collection<GeneFilter> geneFilters,
            Collection<ConditionFilter2> conditionFilters, Collection<DataType> dataTypeFilter,
            boolean onlyTrusted, SummaryQuality summaryQuality, Boolean callObservedData)
                    throws IllegalArgumentException {
        super(buildSummaryCallTypeQualityFilter(summaryQuality),
                geneFilters == null ? Set.of() : Set.copyOf(geneFilters),
                conditionFilters == null ? Set.of() : Set.copyOf(conditionFilters),
                dataTypeFilter, ExpressionSummary.class, extractSpeciesIds(geneFilters));
        if (taxonId <= 0) {
            throw log.throwing(new IllegalArgumentException("taxonId must be strictly positive"));
        }
        if (geneFilters == null || geneFilters.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("At least one GeneFilter must be provided"));
        }
        if (geneFilters.stream().anyMatch(Objects::isNull)) {
            throw log.throwing(new IllegalArgumentException("No gene filter should be null"));
        }
        this.taxonId = taxonId;
        this.onlyTrusted = onlyTrusted;
        this.callObservedData = callObservedData;
    }

    public int getTaxonId() {
        return taxonId;
    }

    public boolean isOnlyTrusted() {
        return onlyTrusted;
    }

    /**
     * @return  {@code true} if at least one of the condition filters requests to exclude
     *          non-informative anatomical entities. Corresponds to the
     *          {@code exclude_non_informative} request parameter.
     */
    public boolean isExcludeNonInformative() {
        return getConditionFilters().stream().anyMatch(ConditionFilter2::isExcludeNonInformative);
    }

    /**
     * @return  {@code true} when only calls observed in the anatomical entity and cell type
     *          are requested, {@code false} when only non-observed calls are requested,
     *          or {@code null} when no observation filter is applied.
     */
    public Boolean getCallObservedData() {
        return callObservedData;
    }

    @Override
    public Set<Integer> getSpeciesIdsConsidered() {
        return getGeneFilters().stream()
                .map(GeneFilter::getSpeciesId)
                .collect(Collectors.toUnmodifiableSet());
    }

    private static Map<ExpressionSummary, SummaryQuality> buildSummaryCallTypeQualityFilter(
            SummaryQuality summaryQuality) {
        SummaryQuality qualToUse = summaryQuality != null ? summaryQuality : SummaryQuality.BRONZE;
        Map<ExpressionSummary, SummaryQuality> summaryCallTypeQualityFilter = new HashMap<>();
        summaryCallTypeQualityFilter.put(ExpressionSummary.EXPRESSED, qualToUse);
        summaryCallTypeQualityFilter.put(ExpressionSummary.NOT_EXPRESSED, qualToUse);
        return summaryCallTypeQualityFilter;
    }

    private static Collection<Integer> extractSpeciesIds(Collection<GeneFilter> geneFilters) {
        if (geneFilters == null || geneFilters.isEmpty()) {
            return Collections.emptySet();
        }
        return geneFilters.stream().map(GeneFilter::getSpeciesId).collect(Collectors.toSet());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(callObservedData, onlyTrusted, taxonId);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SimilarityExpressionCallFilter other = (SimilarityExpressionCallFilter) obj;
        return Objects.equals(callObservedData, other.callObservedData)
                && onlyTrusted == other.onlyTrusted && taxonId == other.taxonId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SimilarityExpressionCallFilter [taxonId=").append(taxonId)
               .append(", onlyTrusted=").append(onlyTrusted)
               .append(", callObservedData=").append(callObservedData)
               .append(", summaryCallTypeQualityFilter=").append(getSummaryCallTypeQualityFilter())
               .append(", dataTypeFilters=").append(getDataTypeFilters())
               .append(", geneFilters=").append(getGeneFilters())
               .append(", conditionFilters=").append(getConditionFilters())
               .append(", speciesIdsConsidered=").append(getSpeciesIdsConsidered())
               .append("]");
        return builder.toString();
    }
}
