package org.bgee.model.expressiondata.call.multispecies;

import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.expressiondata.call.CallServiceParent;
import org.bgee.model.expressiondata.call.OTFExpressionCall;
import org.bgee.model.expressiondata.call.OTFExpressionCallFilterEngine;
import org.bgee.model.gene.Gene;

import java.util.Collection;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

/**
 * This class describes the expression calls for one gene in a multi-species condition,
 * using the {@code OTFExpressionCall}s produced by the on-the-fly propagation.
 *
 * @author  Harald Detering
 * @version Bgee 16, Mar. 2026
 * @since   Bgee 16, Mar. 2026
 * @see     SimilarityExpressionCall
 */
public class SimilarityExpressionCall2 {

    /**
     * A {@code Gene} representing the gene associated to
     * this similarity expression call.
     */
    private final Gene gene;

    /**
     * An {@code ExpressionSummary} representing the type of expression call
     * in this similarity expression call.
     */
    private final ExpressionSummary summaryCallType;

    /**
     * A {@code MultiSpeciesCondition} representing the condition associated to
     * this similarity expression call.
     */
    private final MultiSpeciesCondition multiSpeciesCondition;

    /**
     * A {@code Set} of {@code OTFExpressionCall}s that are single-species calls
     * used to constitute this multi-species similarity call.
     */
    private final Set<OTFExpressionCall> calls;

    /**
     * @param gene                  See {@link #getGene()}.
     * @param multiSpeciesCondition   See {@link #getMultiSpeciesCondition()}.
     * @param calls                 See {@link #getCalls()}. The summary call type is
     *                              {@link ExpressionSummary#EXPRESSED} if any supporting call
     *                              is expressed, otherwise {@link ExpressionSummary#NOT_EXPRESSED}.
     */
    public SimilarityExpressionCall2(Gene gene, MultiSpeciesCondition multiSpeciesCondition,
            Collection<OTFExpressionCall> calls) {
        this(gene, multiSpeciesCondition, calls, computeSummaryCallType(calls));
    }

    public SimilarityExpressionCall2(Gene gene, MultiSpeciesCondition multiSpeciesCondition,
            Collection<OTFExpressionCall> calls, ExpressionSummary summaryCallType) {
        this.gene = gene;
        this.multiSpeciesCondition = multiSpeciesCondition;
        this.summaryCallType = summaryCallType;
        this.calls = Collections.unmodifiableSet(calls == null ? new HashSet<>() : new HashSet<>(calls));
    }

    /**
     * An {@code OTFExpressionCall} carries p-values and scores, not a summary call type: it is
     * inferred from the p-values here, with the same thresholds as everywhere else
     * (see {@code ExpressionCallService}, which builds its processed filters from these very
     * constants). As before, the group is EXPRESSED as soon as one of its calls is.
     */
    private static ExpressionSummary computeSummaryCallType(Collection<OTFExpressionCall> calls) {
        if (calls == null || calls.isEmpty()) {
            return ExpressionSummary.NOT_EXPRESSED;
        }
        return calls.stream()
                .map(SimilarityExpressionCall2::inferCallTypeAndQuality)
                .filter(Objects::nonNull)
                .anyMatch(e -> ExpressionSummary.EXPRESSED.equals(e.getKey()))
                ? ExpressionSummary.EXPRESSED : ExpressionSummary.NOT_EXPRESSED;
    }

    /**
     * @param call  An {@code OTFExpressionCall} to infer the summary call type and quality of.
     * @return      An {@code Entry} of the {@code ExpressionSummary} and the {@code SummaryQuality}
     *              of {@code call}, {@code null} if no summary call type applies to it.
     */
    private static Entry<ExpressionSummary, SummaryQuality> inferCallTypeAndQuality(
            OTFExpressionCall call) {
        return OTFExpressionCallFilterEngine.inferSummaryCallTypeAndQuality(call,
                CallServiceParent.PRESENT_HIGH_LESS_THAN_OR_EQUALS_TO,
                CallServiceParent.PRESENT_LOW_LESS_THAN_OR_EQUALS_TO,
                CallServiceParent.ABSENT_LOW_GREATER_THAN,
                CallServiceParent.ABSENT_HIGH_GREATER_THAN);
    }

    /**
     * @return  The best {@code SummaryQuality} among the calls of this group that are of
     *          its {@link #getSummaryCallType()}, {@code SummaryQuality.BRONZE} when none
     *          can be inferred. Replaces the per-call quality that the former
     *          {@code ExpressionCall2} carried.
     */
    public SummaryQuality getSummaryQuality() {
        return calls.stream()
                .map(SimilarityExpressionCall2::inferCallTypeAndQuality)
                .filter(Objects::nonNull)
                .filter(e -> e.getKey().equals(summaryCallType))
                .map(Entry::getValue)
                .max(Comparator.naturalOrder())
                .orElse(SummaryQuality.BRONZE);
    }

    public Gene getGene() {
        return gene;
    }

    public MultiSpeciesCondition getMultiSpeciesCondition() {
        return multiSpeciesCondition;
    }

    public ExpressionSummary getSummaryCallType() {
        return summaryCallType;
    }

    public Set<OTFExpressionCall> getCalls() {
        return calls;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimilarityExpressionCall2 that = (SimilarityExpressionCall2) o;
        return Objects.equals(gene, that.gene) &&
                Objects.equals(multiSpeciesCondition, that.multiSpeciesCondition) &&
                Objects.equals(summaryCallType, that.summaryCallType) &&
                Objects.equals(calls, that.calls);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gene, multiSpeciesCondition, summaryCallType, calls);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SimilarityExpressionCall2{");
        sb.append("gene=").append(gene);
        sb.append(", summaryCallType=").append(summaryCallType);
        sb.append(", multiSpeciesCondition=").append(multiSpeciesCondition);
        sb.append(", calls=").append(calls);
        sb.append('}');
        return sb.toString();
    }
}
