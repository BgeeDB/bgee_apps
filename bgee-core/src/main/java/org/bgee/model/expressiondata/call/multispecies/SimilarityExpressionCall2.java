package org.bgee.model.expressiondata.call.multispecies;

import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.call.Call.ExpressionCall2;
import org.bgee.model.gene.Gene;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * This class describes the expression calls for one gene in a multi-species condition,
 * using the new {@code ExpressionCall2} type.
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
     * A {@code Set} of {@code ExpressionCall2}s that are single-species calls
     * used to constitute this multi-species similarity call.
     */
    private final Set<ExpressionCall2> calls;

    /**
     * @param gene                  See {@link #getGene()}.
     * @param multiSpeciesCondition   See {@link #getMultiSpeciesCondition()}.
     * @param calls                 See {@link #getCalls()}. The summary call type is
     *                              {@link ExpressionSummary#EXPRESSED} if any supporting call
     *                              is expressed, otherwise {@link ExpressionSummary#NOT_EXPRESSED}.
     */
    public SimilarityExpressionCall2(Gene gene, MultiSpeciesCondition multiSpeciesCondition,
            Collection<ExpressionCall2> calls) {
        this(gene, multiSpeciesCondition, calls, computeSummaryCallType(calls));
    }

    public SimilarityExpressionCall2(Gene gene, MultiSpeciesCondition multiSpeciesCondition,
            Collection<ExpressionCall2> calls, ExpressionSummary summaryCallType) {
        this.gene = gene;
        this.multiSpeciesCondition = multiSpeciesCondition;
        this.summaryCallType = summaryCallType;
        this.calls = Collections.unmodifiableSet(calls == null ? new HashSet<>() : new HashSet<>(calls));
    }

    private static ExpressionSummary computeSummaryCallType(Collection<ExpressionCall2> calls) {
        if (calls == null || calls.isEmpty()) {
            return ExpressionSummary.NOT_EXPRESSED;
        }
        return calls.stream().anyMatch(c -> ExpressionSummary.EXPRESSED.equals(c.getSummaryCallType()))
                ? ExpressionSummary.EXPRESSED : ExpressionSummary.NOT_EXPRESSED;
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

    public Set<ExpressionCall2> getCalls() {
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
