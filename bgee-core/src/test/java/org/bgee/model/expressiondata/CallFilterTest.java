package org.bgee.model.expressiondata;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bgee.model.TestAncestor;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallFilter.DiffExpressionCallFilter;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.DataPropagation.PropagationState;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.ExperimentExpressionCount;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.species.Species;
import org.junit.Test;

/**
 * Unit tests for {@link CallFilter}.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Mar. 2017
 * @since   Bgee 13, Oct. 2015
 */
public class CallFilterTest extends TestAncestor {

    private static final Map<DataType, Set<ExperimentExpressionCount>> VALID_EXP_COUNTS = 
        //we go through all combinations of DataType, CallType.Expression,
        //PropagationState, and DataQuality, to identify and store the valid ones.
        Collections.unmodifiableMap(EnumSet.allOf(DataType.class).stream()
        .flatMap(dataType -> EnumSet.allOf(CallType.Expression.class).stream()
            .filter(callType -> callType.isValidDataType(dataType))
            .flatMap(callType -> ExperimentExpressionCount.ALLOWED_PROP_STATES.stream()
                .filter(propState -> callType.isValidPropagationState(propState))
                .flatMap(propState -> EnumSet.allOf(DataQuality.class).stream()
                    .map(
                        dataQuality -> new AbstractMap.SimpleEntry<>(dataType, 
                            new ExperimentExpressionCount(callType, dataQuality, propState, 0))
                    )
                )
            )
        ).collect(Collectors.groupingBy(e -> e.getKey(), 
                Collectors.mapping(e -> e.getValue(), Collectors.toSet()))));

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
        try {
            new DiffExpressionCallFilter(null, null, new HashSet<>(), null, null);
            fail("An exception should be thrown when the CallData Set is empty.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        
        //now, test when everything is fine
        new ExpressionCallFilter(null, null, null, null, null, true);

        //now, test when everything is fine
        new DiffExpressionCallFilter(null, null, null, SummaryQuality.BRONZE, null);
    }
    
    /**
     * Test the method {@link CallFilter#test(Call)}.
     */
    @Test
    public void shouldTest() {
        
        Collection<ExpressionCallData> callData = new HashSet<>();
        Set<ExperimentExpressionCount> counts = new HashSet<>();
        counts.addAll(VALID_EXP_COUNTS.get(DataType.EST).stream()
                .filter(c -> !(c.getCallType().equals(CallType.Expression.EXPRESSED)
                    && DataQuality.HIGH.equals(c.getDataQuality()) 
                    && PropagationState.SELF.equals(c.getPropagationState())))
                .filter(c -> !(c.getCallType().equals(CallType.Expression.EXPRESSED)
                    && DataQuality.HIGH.equals(c.getDataQuality()) 
                    && PropagationState.ALL.equals(c.getPropagationState())))
                .collect(Collectors.toSet()));
        counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.HIGH,
                PropagationState.SELF, 2));
        counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.HIGH,
                PropagationState.ALL, 2));
        callData.add(new ExpressionCallData(DataType.EST, counts, 0, null, null, null));
        
        counts = new HashSet<>();
        counts.addAll(VALID_EXP_COUNTS.get(DataType.AFFYMETRIX).stream()
            .filter(c -> !(c.getCallType().equals(CallType.Expression.EXPRESSED)
                && DataQuality.HIGH.equals(c.getDataQuality()) 
                && PropagationState.SELF.equals(c.getPropagationState())))
            .filter(c -> !(c.getCallType().equals(CallType.Expression.EXPRESSED)
                && DataQuality.HIGH.equals(c.getDataQuality()) 
                && PropagationState.ALL.equals(c.getPropagationState())))
            .filter(c -> !(c.getCallType().equals(CallType.Expression.EXPRESSED)
                && DataQuality.LOW.equals(c.getDataQuality())
                && PropagationState.SELF.equals(c.getPropagationState())))
            .filter(c -> !(c.getCallType().equals(CallType.Expression.EXPRESSED)
                && DataQuality.LOW.equals(c.getDataQuality()) 
                && PropagationState.ALL.equals(c.getPropagationState())))
            .collect(Collectors.toSet()));
        counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.LOW,
                PropagationState.SELF, 1));
        counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.LOW,
                PropagationState.ALL, 1));
        counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.HIGH,
                PropagationState.SELF, 0));
        counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.HIGH,
                PropagationState.ALL, 0));
        callData.add(new ExpressionCallData(DataType.AFFYMETRIX, counts, 0, null, null, null));

        ExpressionCall call1 = new ExpressionCall(new Gene("g1", new Species(1)),
            new Condition("ae1", "ds1", 1), true, ExpressionSummary.EXPRESSED, SummaryQuality.GOLD,
            callData, new BigDecimal(125.00));
        
        // Test with no GeneFilter and ConditionFilters 
        ExpressionCallFilter callFilter = new ExpressionCallFilter(null, null,
            Arrays.asList(DataType.EST), ExpressionSummary.EXPRESSED, SummaryQuality.SILVER,
            true);
        assertTrue("Call should pass the filter", callFilter.test(call1));

        callFilter = new ExpressionCallFilter(null, null, Arrays.asList(DataType.AFFYMETRIX),
            ExpressionSummary.EXPRESSED, SummaryQuality.SILVER, null);
        assertTrue("Call should pass the filter", callFilter.test(call1));

        callFilter = new ExpressionCallFilter(null, null, null, ExpressionSummary.EXPRESSED,
            SummaryQuality.GOLD, true);
        assertTrue("Call should pass the filter", callFilter.test(call1));

        callFilter = new ExpressionCallFilter(null, null,
            Arrays.asList(DataType.AFFYMETRIX, DataType.EST), ExpressionSummary.EXPRESSED,
            SummaryQuality.GOLD, true);
        assertTrue("Call should pass the filter", callFilter.test(call1));

        callFilter = new ExpressionCallFilter(null, null, Arrays.asList(DataType.RNA_SEQ),
            ExpressionSummary.EXPRESSED, SummaryQuality.GOLD, true);
        assertFalse("Call should not pass the filter", callFilter.test(call1));

        callFilter = new ExpressionCallFilter(null, null, Arrays.asList(DataType.AFFYMETRIX),
            ExpressionSummary.NOT_EXPRESSED, SummaryQuality.BRONZE, true);
        assertFalse("Call should not pass the filter", callFilter.test(call1));

        callFilter = new ExpressionCallFilter(null, null, null, ExpressionSummary.EXPRESSED, 
            SummaryQuality.GOLD, false);
        assertFalse("Call should not pass the filter", callFilter.test(call1));

        callData.clear();
        counts = new HashSet<>();
        counts.addAll(VALID_EXP_COUNTS.get(DataType.AFFYMETRIX).stream()
            .filter(c -> !(c.getCallType().equals(CallType.Expression.EXPRESSED)
                && DataQuality.LOW.equals(c.getDataQuality())
                && PropagationState.SELF.equals(c.getPropagationState())))
            .filter(c -> !(c.getCallType().equals(CallType.Expression.EXPRESSED)
                && DataQuality.LOW.equals(c.getDataQuality()) 
                && PropagationState.ALL.equals(c.getPropagationState())))
            .collect(Collectors.toSet()));
        counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.LOW,
                PropagationState.SELF, 1));
        counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.LOW,
                PropagationState.ALL, 1));
        callData.add(new ExpressionCallData(DataType.AFFYMETRIX, counts, 0, null, null, null));
        
        ExpressionCall call2 = new ExpressionCall(new Gene("g1", new Species(1)), new Condition("ae1", "ds1", 1),
                true, ExpressionSummary.EXPRESSED, SummaryQuality.SILVER, callData, new BigDecimal(125.00));
        callFilter = new ExpressionCallFilter(null, null, null, ExpressionSummary.EXPRESSED,
            SummaryQuality.GOLD, true);
        assertFalse("Call should not pass the filter", callFilter.test(call2));

        // Test condition filter
        Set<ConditionFilter> validConditionFilters = new HashSet<>();
        validConditionFilters.add(new ConditionFilter(Arrays.asList("ae1", "ae2"), Arrays.asList("ds1", "ds2")));
        callFilter = new ExpressionCallFilter(new GeneFilter("g1", 1), validConditionFilters,
            null, null, null, null);
        assertTrue("Call should pass the filter", callFilter.test(call1));

        Set<ConditionFilter> notValidConditionFilters = new HashSet<>();
        notValidConditionFilters.add(new ConditionFilter(Arrays.asList("ae1", "ae2"), Arrays.asList("ds2")));
        callFilter = new ExpressionCallFilter(new GeneFilter("g1", 1), notValidConditionFilters, 
            null, null, null, null);
        assertFalse("Call should not pass the filter", callFilter.test(call1));

        // Test gene filter
        callFilter = new ExpressionCallFilter(new GeneFilter("g2", 1), validConditionFilters, 
            null, null, null, null);
        assertFalse("Call should not pass the filter", callFilter.test(call1));
    }
}
