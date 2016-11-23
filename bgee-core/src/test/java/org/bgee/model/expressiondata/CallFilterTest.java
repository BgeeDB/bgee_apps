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
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.DiffExpressionFactor;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.DiffExpressionCallData;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallFilter.DiffExpressionCallFilter;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.baseelements.CallType.DiffExpression;
import org.bgee.model.expressiondata.baseelements.CallType.Expression;
import org.bgee.model.expressiondata.baseelements.DataPropagation.PropagationState;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.gene.GeneFilter;
import org.junit.Test;

/**
 * Unit tests for {@link CallFilter}.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Nov. 2016
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
            new ExpressionCallFilter(null, null, null);
            fail("An exception should be thrown when no CallData Set is provided.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        try {
            new ExpressionCallFilter(null, null, new HashSet<>());
            fail("An exception should be thrown when the CallData Set is empty.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        try {
            new ExpressionCallFilter(null, null, 
                    new HashSet<>(Arrays.asList((ExpressionCallData) null)));
            fail("An exception should be thrown when the CallData Set contains null value.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        try {
            new ExpressionCallFilter(null, null, new HashSet<>(Arrays.asList(
                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX), 
                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX))));
            fail("An exception should be thrown when some CallData target redundant combinations "
                    + "of CallType/DataType.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        try {
            new ExpressionCallFilter(null, null, new HashSet<>(Arrays.asList(
                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX),
                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, null))));
            fail("An exception should be thrown when some CallData target redundant combinations "
                    + "of CallType/DataType.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        try {
            new DiffExpressionCallFilter(null, null, new HashSet<>(Arrays.asList(
                    new DiffExpressionCallData(DiffExpressionFactor.ANATOMY, DiffExpression.UNDER_EXPRESSED, 
                            DataQuality.LOW, DataType.RNA_SEQ), 
                    new DiffExpressionCallData(DiffExpressionFactor.ANATOMY, DiffExpression.UNDER_EXPRESSED, 
                            DataQuality.HIGH, DataType.RNA_SEQ))));
            fail("An exception should be thrown when some CallData target redundant combinations "
                    + "of CallType/DataType/DiffExpressionFactor.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        
        //now, test when everything is fine
        new CallFilter<>(null, null, new HashSet<>(Arrays.asList(
                new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX), 
                new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ), 
                new DiffExpressionCallData(DiffExpressionFactor.ANATOMY, DiffExpression.OVER_EXPRESSED, 
                        DataQuality.LOW, null), 
                new DiffExpressionCallData(DiffExpressionFactor.ANATOMY, DiffExpression.UNDER_EXPRESSED, 
                        DataQuality.LOW, DataType.RNA_SEQ), 
                new DiffExpressionCallData(DiffExpressionFactor.DEVELOPMENT, DiffExpression.UNDER_EXPRESSED, 
                        DataQuality.LOW, DataType.RNA_SEQ))));
    }
    
    /**
     * Test the method {@link CallFilter#test(Call)}.
     */
    @Test
    public void shouldTest() {
        Collection<ExpressionCallData> callData = new HashSet<>();
        callData.add(new ExpressionCallData(
                Expression.EXPRESSED, DataQuality.HIGH, DataType.EST, new DataPropagation()));
        callData.add(new ExpressionCallData(
                Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, new DataPropagation()));

        ExpressionCall call1 = new ExpressionCall("g1", new Condition("ae1", "ds1", "sp1"),
                new DataPropagation(PropagationState.SELF, PropagationState.SELF, true),
                ExpressionSummary.EXPRESSED, DataQuality.HIGH, callData, new BigDecimal(125.00));
        
        ExpressionCallData validCallDataFilter1 = new ExpressionCallData(
                Expression.EXPRESSED, DataQuality.LOW, DataType.EST, new DataPropagation());
        ExpressionCallData validCallDataFilter2 = new ExpressionCallData(
                Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, 
                new DataPropagation(PropagationState.DESCENDANT, PropagationState.SELF));
        ExpressionCallData validCallDataFilter3 = new ExpressionCallData(
                Expression.EXPRESSED, DataQuality.HIGH, null, new DataPropagation());

        ExpressionCallData notValidCallDataFilter1 = new ExpressionCallData(
                Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, new DataPropagation());
        ExpressionCallData notValidCallDataFilter2 = new ExpressionCallData(
                Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, new DataPropagation());

        // Test with no GeneFilter and ConditionFilters 
        Set<ExpressionCallData> callDataFilters = new HashSet<>(Arrays.asList(validCallDataFilter1));
        ExpressionCallFilter callFilter = new ExpressionCallFilter(null, null, callDataFilters);
        assertTrue("Call should pass the filter", callFilter.test(call1));

        callDataFilters = new HashSet<>(Arrays.asList(validCallDataFilter2));
        callFilter = new ExpressionCallFilter(null, null, callDataFilters);
        assertTrue("Call should pass the filter", callFilter.test(call1));

        callDataFilters = new HashSet<>(Arrays.asList(validCallDataFilter3));
        callFilter = new ExpressionCallFilter(null, null, callDataFilters);
        assertTrue("Call should pass the filter", callFilter.test(call1));

        callDataFilters = new HashSet<>(Arrays.asList(validCallDataFilter1, notValidCallDataFilter1));
        callFilter = new ExpressionCallFilter(null, null, callDataFilters);
        assertTrue("Call should pass the filter", callFilter.test(call1));

        callDataFilters = new HashSet<>(Arrays.asList(notValidCallDataFilter1));
        callFilter = new ExpressionCallFilter(null, null, callDataFilters);
        assertFalse("Call should not pass the filter", callFilter.test(call1));

        callDataFilters = new HashSet<>(Arrays.asList(notValidCallDataFilter2));
        callFilter = new ExpressionCallFilter(null, null, callDataFilters);
        assertFalse("Call should not pass the filter", callFilter.test(call1));

        callData.clear();
        callData.add(new ExpressionCallData(
                Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, new DataPropagation()));
        ExpressionCall call2 = new ExpressionCall("g1", new Condition("ae1", "ds1", "sp1"),
                new DataPropagation(PropagationState.SELF, PropagationState.SELF, true),
                ExpressionSummary.EXPRESSED, DataQuality.HIGH, callData, new BigDecimal(125.00));
        ExpressionCallData notValidCallDataFilter3 = new ExpressionCallData(
                Expression.EXPRESSED, DataQuality.HIGH, null, new DataPropagation());
        callDataFilters = new HashSet<>(Arrays.asList(notValidCallDataFilter3));
        callFilter = new ExpressionCallFilter(null, null, callDataFilters);
        assertFalse("Call should not pass the filter", callFilter.test(call2));

        // Test condition filter
        callFilter = new ExpressionCallFilter(null, null, callDataFilters);
        Set<ConditionFilter> validConditionFilters = new HashSet<>();
        validConditionFilters.add(new ConditionFilter(Arrays.asList("ae1", "ae2"), Arrays.asList("ds1", "ds2")));
        callFilter = new ExpressionCallFilter(new GeneFilter("g1"), validConditionFilters, 
                new HashSet<>(Arrays.asList(validCallDataFilter1)));
        assertTrue("Call should pass the filter", callFilter.test(call1));

        Set<ConditionFilter> notValidConditionFilters = new HashSet<>();
        notValidConditionFilters.add(new ConditionFilter(Arrays.asList("ae1", "ae2"), Arrays.asList("ds2")));
        callFilter = new ExpressionCallFilter(new GeneFilter("g1"), notValidConditionFilters, 
                new HashSet<>(Arrays.asList(validCallDataFilter1)));
        assertFalse("Call should not pass the filter", callFilter.test(call1));

        // Test gene filter
        callFilter = new ExpressionCallFilter(new GeneFilter("g2"), validConditionFilters, 
                new HashSet<>(Arrays.asList(validCallDataFilter1)));
        assertFalse("Call should not pass the filter", callFilter.test(call1));
    }
}
