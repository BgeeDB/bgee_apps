package org.bgee.model.expressiondata;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bgee.model.TestAncestor;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallFilter.DiffExpressionCallFilter;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.DataPropagation.PropagationState;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.gene.GeneFilter;
import org.junit.Test;

/**
 * Unit tests for {@link CallFilter}.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Feb. 2017
 * @since   Bgee 13, Oct. 2015
 */
public class CallFilterTest extends TestAncestor {

    /**
     * Test the sanity checks performed by {@link CallFilter#CallFilter(org.bgee.model.gene.GeneFilter, Set, Set)}.
     */
    //variables unused because we only test initialization.
    @SuppressWarnings("unused")
    @Test
    public void testSanityChecks() {
        try {
            new ExpressionCallFilter(null, null, null, null, null, null);
            fail("An exception should be thrown when no CallData Set is provided.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        try {
            new ExpressionCallFilter(null, null, new HashSet<>(), null, null, null);
            fail("An exception should be thrown when the CallData Set is empty.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        
        //now, test when everything is fine
        new ExpressionCallFilter(null, null, null, null, null, new DataPropagation());

        //now, test when everything is fine
        new DiffExpressionCallFilter(null, null, null, SummaryQuality.BRONZE, null);
    }
    
    /**
     * Test the method {@link CallFilter#test(Call)}.
     */
    @Test
    public void shouldTest() {
        Collection<ExpressionCallData> callData = new HashSet<>();
        callData.add(new ExpressionCallData(DataType.EST, 2 /*presentHighSelfExpCount*/, 
            0, 0, 0, 2 /*presentHighTotalCount*/, 0, 0, 0));
        callData.add(new ExpressionCallData(DataType.AFFYMETRIX, 0, 
            1 /*presentLowSelfExpCount*/, 0, 0, 0, 1 /*presentLowTotalCount*/, 0, 0));

        ExpressionCall call1 = new ExpressionCall("g1", new Condition("ae1", "ds1", "sp1"), true, 
            ExpressionSummary.EXPRESSED, SummaryQuality.GOLD, callData, new BigDecimal(125.00));
        
        // Test with no GeneFilter and ConditionFilters 
        ExpressionCallFilter callFilter = new ExpressionCallFilter(null, null,
            Arrays.asList(DataType.EST), SummaryQuality.SILVER,
            ExpressionSummary.EXPRESSED, new DataPropagation());
        assertTrue("Call should pass the filter", callFilter.test(call1));

        callFilter = new ExpressionCallFilter(null, null,
            Arrays.asList(DataType.AFFYMETRIX), SummaryQuality.SILVER, ExpressionSummary.EXPRESSED,
            new DataPropagation(PropagationState.DESCENDANT, PropagationState.SELF));
        assertTrue("Call should pass the filter", callFilter.test(call1));

        callFilter = new ExpressionCallFilter(null, null, null, SummaryQuality.GOLD,
            ExpressionSummary.EXPRESSED, new DataPropagation());
        assertTrue("Call should pass the filter", callFilter.test(call1));

        callFilter = new ExpressionCallFilter(null, null,
            Arrays.asList(DataType.AFFYMETRIX, DataType.EST), SummaryQuality.GOLD,
            ExpressionSummary.EXPRESSED, new DataPropagation());
        assertTrue("Call should pass the filter", callFilter.test(call1));

        callFilter = new ExpressionCallFilter(null, null,
            Arrays.asList(DataType.RNA_SEQ), SummaryQuality.GOLD,
            ExpressionSummary.EXPRESSED, new DataPropagation());
        assertFalse("Call should not pass the filter", callFilter.test(call1));

        callFilter = new ExpressionCallFilter(null, null,
            Arrays.asList(DataType.AFFYMETRIX), SummaryQuality.BRONZE,
            ExpressionSummary.NOT_EXPRESSED, new DataPropagation());
        assertFalse("Call should not pass the filter", callFilter.test(call1));

        callData.clear();
        callData.add(new ExpressionCallData(DataType.AFFYMETRIX, 0, 
            1 /*presentLowSelfExpCount*/, 0, 0, 0, 1 /*presentLowTotalCount*/, 0, 0));
        ExpressionCall call2 = new ExpressionCall("g1", new Condition("ae1", "ds1", "sp1"),
                true, ExpressionSummary.EXPRESSED, SummaryQuality.SILVER, callData, new BigDecimal(125.00));
        callFilter = new ExpressionCallFilter(null, null, null, SummaryQuality.GOLD,
            ExpressionSummary.EXPRESSED, new DataPropagation());
        assertFalse("Call should not pass the filter", callFilter.test(call2));

        // Test condition filter
        Set<ConditionFilter> validConditionFilters = new HashSet<>();
        validConditionFilters.add(new ConditionFilter(Arrays.asList("ae1", "ae2"), Arrays.asList("ds1", "ds2")));
        callFilter = new ExpressionCallFilter(new GeneFilter("g1"), validConditionFilters,
            null, null, null, null);
        assertTrue("Call should pass the filter", callFilter.test(call1));

        Set<ConditionFilter> notValidConditionFilters = new HashSet<>();
        notValidConditionFilters.add(new ConditionFilter(Arrays.asList("ae1", "ae2"), Arrays.asList("ds2")));
        callFilter = new ExpressionCallFilter(new GeneFilter("g1"), notValidConditionFilters, 
            null, null, null, null);
        assertFalse("Call should not pass the filter", callFilter.test(call1));

        // Test gene filter
        callFilter = new ExpressionCallFilter(new GeneFilter("g2"), validConditionFilters, 
            null, null, null, null);
        assertFalse("Call should not pass the filter", callFilter.test(call1));
    }
}
