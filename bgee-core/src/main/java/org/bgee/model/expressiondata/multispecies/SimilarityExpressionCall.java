package org.bgee.model.expressiondata.multispecies;

import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.gene.Gene;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * This class describes the expression calls for one gene in a multi-species condition. 
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Mar. 2019
 * @since   Bgee 14, Mar. 2019
 */
public class SimilarityExpressionCall {

    /**
     * A {@code Gene} representing the gene associated to
     * this similarity expression call.
     */
    private final Gene gene;

    /**
     * A {@code SummaryCallType} representing the type of expression call
     * in this similarity expression call.
     */
    private final SummaryCallType summaryCallType;

    /**
     * A {@code MultiSpeciesCondition} representing the condition associated to
     * this similarity expression call.
     */
    private final MultiSpeciesCondition multiSpeciesCondition;
    
    /**
     * A {@code Set} of {@code Call}s that are single-species calls 
     * used to constitute this {@code MultiSpeciesCall}.
     */
    private final Set<ExpressionCall> calls;

    public SimilarityExpressionCall(Gene gene, MultiSpeciesCondition multiSpeciesCondition,
                                    Collection<ExpressionCall> calls, SummaryCallType summaryCallType) {
        this.gene = gene;
        this.multiSpeciesCondition = multiSpeciesCondition;
        this.summaryCallType = summaryCallType;
        this.calls = Collections.unmodifiableSet(calls == null? new HashSet<>(): new HashSet<>(calls));
    }

    public Gene getGene() {
        return gene;
    }

    public MultiSpeciesCondition getMultiSpeciesCondition() {
        return multiSpeciesCondition;
    }

    public SummaryCallType getSummaryCallType() {
        return summaryCallType;
    }

    public Set<ExpressionCall> getCalls() {
        return calls;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimilarityExpressionCall that = (SimilarityExpressionCall) o;
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
        final StringBuilder sb = new StringBuilder("SimilarityExpressionCall{");
        sb.append("gene=").append(gene);
        sb.append(", summaryCallType=").append(summaryCallType);
        sb.append(", multiSpeciesCondition=").append(multiSpeciesCondition);
        sb.append(", calls=").append(calls);
        sb.append('}');
        return sb.toString();
    }
}
