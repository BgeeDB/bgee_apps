package org.bgee.model.expressiondata.call.multispecies;

import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.PropagationState;
import org.bgee.model.expressiondata.call.OTFExpressionCall;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.species.Species;
import org.junit.Test;

import java.math.BigDecimal;
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
        OTFExpressionCall expressedCall = mock(OTFExpressionCall.class);
        //EXPRESSED GOLD: p-value below the PRESENT HIGH threshold (0.01)
        when(expressedCall.getAllDataTypePValue()).thenReturn(new BigDecimal("0.001"));
        OTFExpressionCall notExpressedCall = mock(OTFExpressionCall.class);
        //Not expressed: p-value above the ABSENT LOW threshold (0.05), with observed data
        when(notExpressedCall.getAllDataTypePValue()).thenReturn(new BigDecimal("0.9"));
        when(notExpressedCall.getDataPropagation()).thenReturn(PropagationState.SELF);

        SimilarityExpressionCall2 sec = new SimilarityExpressionCall2(
                gene, msc, Arrays.asList(expressedCall, notExpressedCall));

        assertEquals(ExpressionSummary.EXPRESSED, sec.getSummaryCallType());
    }

    @Test
    public void shouldDeriveNotExpressedSummaryWhenNoSupportingCallIsExpressed() {
        Species species = new Species(9606);
        Gene gene = new Gene("ENSG00000130208", species, new GeneBioType("protein_coding"));
        MultiSpeciesCondition msc = new MultiSpeciesCondition(null, null, null, null);
        OTFExpressionCall notExpressedCall = mock(OTFExpressionCall.class);
        //Not expressed: p-value above the ABSENT LOW threshold (0.05), with observed data
        when(notExpressedCall.getAllDataTypePValue()).thenReturn(new BigDecimal("0.9"));
        when(notExpressedCall.getDataPropagation()).thenReturn(PropagationState.SELF);

        SimilarityExpressionCall2 sec = new SimilarityExpressionCall2(
                gene, msc, Collections.singletonList(notExpressedCall));

        assertEquals(ExpressionSummary.NOT_EXPRESSED, sec.getSummaryCallType());
    }

    @Test
    public void shouldCreateSimilarityExpressionCall2WithCalls() {
        Species species = new Species(9606);
        Gene gene = new Gene("ENSG00000130208", species, new GeneBioType("protein_coding"));
        MultiSpeciesCondition msc = new MultiSpeciesCondition(null, null, null, null);
        OTFExpressionCall call1 = mock(OTFExpressionCall.class);
        OTFExpressionCall call2 = mock(OTFExpressionCall.class);
        Set<OTFExpressionCall> calls = new HashSet<>(Arrays.asList(call1, call2));

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
        OTFExpressionCall call = mock(OTFExpressionCall.class);

        SimilarityExpressionCall2 sec = new SimilarityExpressionCall2(
                gene, msc, Collections.singletonList(call), ExpressionSummary.EXPRESSED);

        try {
            sec.getCalls().add(mock(OTFExpressionCall.class));
            throw new AssertionError("Expected UnsupportedOperationException when modifying calls");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }
}
