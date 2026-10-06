package org.bgee.model.expressiondata.call.multispecies;

import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.call.Call.ExpressionCall2;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.species.Species;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SimilarityExpressionCall2}.
 *
 * @author  Harald Detering
 * @version Bgee 16, Mar. 2026
 * @since   Bgee 16, Mar. 2026
 */
public class SimilarityExpressionCall2Test {

    @Test
    public void shouldCreateSimilarityExpressionCall2WithEmptyCalls() {
        Species species = new Species(9606);
        Gene gene = new Gene("ENSG00000130208", species, new GeneBioType("protein_coding"));
        MultiSpeciesCondition msc = new MultiSpeciesCondition(null, null, null, null);

        SimilarityExpressionCall2 sec = new SimilarityExpressionCall2(
                gene, msc, Collections.emptyList());

        assertNotNull(sec);
        assertEquals(gene, sec.getGene());
        assertEquals(msc, sec.getMultiSpeciesCondition());
        assertEquals(ExpressionSummary.NOT_EXPRESSED, sec.getSummaryCallType());
        assertNotNull(sec.getCalls());
        assertEquals(0, sec.getCalls().size());
    }

    @Test
    public void shouldDeriveExpressedSummaryWhenAnySupportingCallIsExpressed() {
        Species species = new Species(9606);
        Gene gene = new Gene("ENSG00000130208", species, new GeneBioType("protein_coding"));
        MultiSpeciesCondition msc = new MultiSpeciesCondition(null, null, null, null);
        ExpressionCall2 expressedCall = mock(ExpressionCall2.class);
        when(expressedCall.getSummaryCallType()).thenReturn(ExpressionSummary.EXPRESSED);
        ExpressionCall2 notExpressedCall = mock(ExpressionCall2.class);
        when(notExpressedCall.getSummaryCallType()).thenReturn(ExpressionSummary.NOT_EXPRESSED);

        SimilarityExpressionCall2 sec = new SimilarityExpressionCall2(
                gene, msc, Arrays.asList(expressedCall, notExpressedCall));

        assertEquals(ExpressionSummary.EXPRESSED, sec.getSummaryCallType());
    }

    @Test
    public void shouldDeriveNotExpressedSummaryWhenNoSupportingCallIsExpressed() {
        Species species = new Species(9606);
        Gene gene = new Gene("ENSG00000130208", species, new GeneBioType("protein_coding"));
        MultiSpeciesCondition msc = new MultiSpeciesCondition(null, null, null, null);
        ExpressionCall2 notExpressedCall = mock(ExpressionCall2.class);
        when(notExpressedCall.getSummaryCallType()).thenReturn(ExpressionSummary.NOT_EXPRESSED);

        SimilarityExpressionCall2 sec = new SimilarityExpressionCall2(
                gene, msc, Collections.singletonList(notExpressedCall));

        assertEquals(ExpressionSummary.NOT_EXPRESSED, sec.getSummaryCallType());
    }

    @Test
    public void shouldCreateSimilarityExpressionCall2WithCalls() {
        Species species = new Species(9606);
        Gene gene = new Gene("ENSG00000130208", species, new GeneBioType("protein_coding"));
        MultiSpeciesCondition msc = new MultiSpeciesCondition(null, null, null, null);
        ExpressionCall2 call1 = mock(ExpressionCall2.class);
        ExpressionCall2 call2 = mock(ExpressionCall2.class);
        Set<ExpressionCall2> calls = new HashSet<>(Arrays.asList(call1, call2));

        SimilarityExpressionCall2 sec = new SimilarityExpressionCall2(
                gene, msc, calls, ExpressionSummary.NOT_EXPRESSED);

        assertNotNull(sec);
        assertEquals(gene, sec.getGene());
        assertEquals(msc, sec.getMultiSpeciesCondition());
        assertEquals(ExpressionSummary.NOT_EXPRESSED, sec.getSummaryCallType());
        assertNotNull(sec.getCalls());
        assertEquals(2, sec.getCalls().size());
        assertEquals(calls, sec.getCalls());
    }

    @Test
    public void shouldHandleNullCallsAsEmptySet() {
        Species species = new Species(9606);
        Gene gene = new Gene("ENSG00000130208", species, new GeneBioType("protein_coding"));
        MultiSpeciesCondition msc = new MultiSpeciesCondition(null, null, null, null);

        SimilarityExpressionCall2 sec = new SimilarityExpressionCall2(
                gene, msc, null, ExpressionSummary.EXPRESSED);

        assertNotNull(sec.getCalls());
        assertEquals(0, sec.getCalls().size());
    }

    @Test
    public void shouldSupportEqualsAndHashCode() {
        Species species = new Species(9606);
        Gene gene = new Gene("ENSG00000130208", species, new GeneBioType("protein_coding"));
        MultiSpeciesCondition msc = new MultiSpeciesCondition(null, null, null, null);

        SimilarityExpressionCall2 sec1 = new SimilarityExpressionCall2(
                gene, msc, Collections.emptyList(), ExpressionSummary.EXPRESSED);
        SimilarityExpressionCall2 sec2 = new SimilarityExpressionCall2(
                gene, msc, Collections.emptyList(), ExpressionSummary.EXPRESSED);

        assertEquals(sec1, sec2);
        assertEquals(sec1.hashCode(), sec2.hashCode());
    }

    @Test
    public void shouldReturnUnmodifiableCalls() {
        Species species = new Species(9606);
        Gene gene = new Gene("ENSG00000130208", species, new GeneBioType("protein_coding"));
        MultiSpeciesCondition msc = new MultiSpeciesCondition(null, null, null, null);
        ExpressionCall2 call = mock(ExpressionCall2.class);

        SimilarityExpressionCall2 sec = new SimilarityExpressionCall2(
                gene, msc, Collections.singletonList(call), ExpressionSummary.EXPRESSED);

        try {
            sec.getCalls().add(mock(ExpressionCall2.class));
            throw new AssertionError("Expected UnsupportedOperationException when modifying calls");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }
}
