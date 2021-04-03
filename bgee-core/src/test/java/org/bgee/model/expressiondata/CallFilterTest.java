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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bgee.model.TestAncestor;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallFilter.DiffExpressionCallFilter;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.CallType.Expression;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.PropagationState;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.ExperimentExpressionCount;
import org.bgee.model.expressiondata.baseelements.ExpressionLevelInfo;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.species.Species;
import org.junit.Test;

/**
 * Unit tests for {@link CallFilter}.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @author  Julien Wollbrett
 * @version Bgee 14, Aug. 2018
 * @since   Bgee 13, Oct. 2015
 */
//FIXME: to reactivate
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
            new ExpressionCallFilter(null, null, null, null, null, null, null, null, null, null);
            fail("An exception should be thrown when no CallData Set is provided.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        try {
            new ExpressionCallFilter(null, null, new HashSet<>(), null, null, null, null, null, null, null);
            fail("An exception should be thrown when the CallData Set is empty.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        try {
            new DiffExpressionCallFilter(null, null, new HashSet<>(), null);
            fail("An exception should be thrown when the CallData Set is empty.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        
        //now, test when everything is fine (GeneFilter or ConditionFilter provided)
        Set<GeneFilter> geneFilter = Collections.singleton(new GeneFilter(9606));
        Set<ConditionFilter> conditionFilter = 
                Collections.singleton(new ConditionFilter(Arrays.asList("ae1", "ae2"), 
                        Arrays.asList("devStage1"), null, null, null));
        

        //now, test when everything is fine
        //test when GeneFilter and/or ConditionFilter are provided
        new ExpressionCallFilter(null, geneFilter, null, null, null, null, null, null, null, null);
        new ExpressionCallFilter(null, null, conditionFilter, null, null, null, null, null, null, null);
        new ExpressionCallFilter(null, geneFilter, conditionFilter, null, null, null, null, null, null, null);
        Map<SummaryCallType.DiffExpressionSummary, SummaryQuality> callTypeMap = new HashMap<>();
        callTypeMap.put(SummaryCallType.DiffExpressionSummary.OVER_EXPRESSED, SummaryQuality.BRONZE);
        new DiffExpressionCallFilter(null, geneFilter, null, null);
        new DiffExpressionCallFilter(null, null, conditionFilter, null);
        new DiffExpressionCallFilter(null, geneFilter, conditionFilter, null);
    }

//    /**
//     * Test the method {@link CallFilter#test(Call)}.
//     */
//    @Test
//    public void shouldTest() {
//        
//        Collection<ExpressionCallData> callData = new HashSet<>();
//        Set<ExperimentExpressionCount> counts = new HashSet<>();
//        counts.addAll(VALID_EXP_COUNTS.get(DataType.EST).stream()
//                .filter(c -> !(c.getCallType().equals(CallType.Expression.EXPRESSED)
//                    && DataQuality.HIGH.equals(c.getDataQuality()) 
//                    && PropagationState.SELF.equals(c.getPropagationState())))
//                .filter(c -> !(c.getCallType().equals(CallType.Expression.EXPRESSED)
//                    && DataQuality.HIGH.equals(c.getDataQuality()) 
//                    && PropagationState.ALL.equals(c.getPropagationState())))
//                .collect(Collectors.toSet()));
//        counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.HIGH,
//                PropagationState.SELF, 2));
//        counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.HIGH,
//                PropagationState.ALL, 2));
//        callData.add(new ExpressionCallData(DataType.EST, counts, 0, null, null, null, null));
//        
//        counts = new HashSet<>();
//        counts.addAll(VALID_EXP_COUNTS.get(DataType.AFFYMETRIX).stream()
//            .filter(c -> !(c.getCallType().equals(CallType.Expression.EXPRESSED)
//                && DataQuality.HIGH.equals(c.getDataQuality()) 
//                && PropagationState.SELF.equals(c.getPropagationState())))
//            .filter(c -> !(c.getCallType().equals(CallType.Expression.EXPRESSED)
//                && DataQuality.HIGH.equals(c.getDataQuality()) 
//                && PropagationState.ALL.equals(c.getPropagationState())))
//            .filter(c -> !(c.getCallType().equals(CallType.Expression.EXPRESSED)
//                && DataQuality.LOW.equals(c.getDataQuality())
//                && PropagationState.SELF.equals(c.getPropagationState())))
//            .filter(c -> !(c.getCallType().equals(CallType.Expression.EXPRESSED)
//                && DataQuality.LOW.equals(c.getDataQuality()) 
//                && PropagationState.ALL.equals(c.getPropagationState())))
//            .collect(Collectors.toSet()));
//        counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.LOW,
//                PropagationState.SELF, 1));
//        counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.LOW,
//                PropagationState.ALL, 1));
//        counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.HIGH,
//                PropagationState.SELF, 0));
//        counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.HIGH,
//                PropagationState.ALL, 0));
//        callData.add(new ExpressionCallData(DataType.AFFYMETRIX, counts, 0, null, null, null, null));
//        
//        AnatEntity ae1 = new AnatEntity("ae1");
//        DevStage ds1 = new DevStage("ds1");
//        Species sp1 = new Species(1);
//        
//        DataPropagation selfSelfPropagation = new DataPropagation(PropagationState.SELF, PropagationState.SELF);
//        
//        ExpressionCall call1 = new ExpressionCall(new Gene("g1", new Species(1), new GeneBioType("b")),
//            new Condition(ae1, ds1, sp1), selfSelfPropagation, ExpressionSummary.EXPRESSED, 
//            SummaryQuality.GOLD, callData, new ExpressionLevelInfo(new BigDecimal(125.00)));
//        
//        // Test ConditionFilter
//        ConditionFilter cond1 = new ConditionFilter(Collections.singleton("ae1"), Collections.singleton("ds1"));
//        Map<SummaryCallType.ExpressionSummary, SummaryQuality> callTypeMap = new HashMap<>();
//        Map<Expression, Boolean> expressedData = new HashMap<>();
//        expressedData.put(Expression.EXPRESSED, true);
//        callTypeMap.put(ExpressionSummary.EXPRESSED, SummaryQuality.SILVER);
//        ExpressionCallFilter callFilter = new ExpressionCallFilter(callTypeMap, null, Collections.singleton(cond1),
//            Arrays.asList(DataType.EST), expressedData, null, null);
//        assertTrue("Call should pass the filter", callFilter.test(call1));
//
//        callTypeMap = new HashMap<>();
//        callTypeMap.put(ExpressionSummary.EXPRESSED, SummaryQuality.SILVER);
//        callFilter = new ExpressionCallFilter(callTypeMap, null, Collections.singleton(cond1),
//                Arrays.asList(DataType.AFFYMETRIX), null, null, null);
//        assertTrue("Call should pass the filter", callFilter.test(call1));
//
//        callTypeMap = new HashMap<>();
//        
//        callTypeMap.put(ExpressionSummary.EXPRESSED, SummaryQuality.GOLD);
//        callFilter = new ExpressionCallFilter(callTypeMap, null, Collections.singleton(cond1),
//                null, expressedData, null, null);
//        assertTrue("Call should pass the filter", callFilter.test(call1));
//
//        callTypeMap = new HashMap<>();
//        callTypeMap.put(ExpressionSummary.EXPRESSED, SummaryQuality.GOLD);
//        callFilter = new ExpressionCallFilter(callTypeMap, null, Collections.singleton(cond1),
//            Arrays.asList(DataType.AFFYMETRIX, DataType.EST), expressedData, null, null);
//        assertTrue("Call should pass the filter", callFilter.test(call1));
//
//        callTypeMap = new HashMap<>();
//        callTypeMap.put(ExpressionSummary.EXPRESSED, SummaryQuality.GOLD);
//        callFilter = new ExpressionCallFilter(callTypeMap, null, Collections.singleton(cond1),
//                Arrays.asList(DataType.RNA_SEQ), expressedData, null, null);
//        assertFalse("Call should not pass the filter", callFilter.test(call1));
//
//        callTypeMap = new HashMap<>();
//        callTypeMap.put(ExpressionSummary.NOT_EXPRESSED, SummaryQuality.BRONZE);
//        callFilter = new ExpressionCallFilter(callTypeMap, null, Collections.singleton(cond1),
//                Arrays.asList(DataType.AFFYMETRIX), expressedData, null, null);
//        assertFalse("Call should not pass the filter", callFilter.test(call1));
//
//        callTypeMap = new HashMap<>();
//        callTypeMap.put(ExpressionSummary.EXPRESSED, SummaryQuality.GOLD);
//        Map<Expression, Boolean> notExpressedData = new HashMap<>();
//        notExpressedData.put(Expression.NOT_EXPRESSED, true);
//        notExpressedData.put(Expression.EXPRESSED, false);
//        callFilter = new ExpressionCallFilter(callTypeMap, null, Collections.singleton(cond1),
//                Arrays.asList(DataType.IN_SITU), notExpressedData, null, null);
//        assertFalse("Call should not pass the filter", callFilter.test(call1));
//
//        callData.clear();
//        counts = new HashSet<>();
//        counts.addAll(VALID_EXP_COUNTS.get(DataType.AFFYMETRIX).stream()
//            .filter(c -> !(c.getCallType().equals(CallType.Expression.EXPRESSED)
//                && DataQuality.LOW.equals(c.getDataQuality())
//                && PropagationState.SELF.equals(c.getPropagationState())))
//            .filter(c -> !(c.getCallType().equals(CallType.Expression.EXPRESSED)
//                && DataQuality.LOW.equals(c.getDataQuality()) 
//                && PropagationState.ALL.equals(c.getPropagationState())))
//            .collect(Collectors.toSet()));
//        counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.LOW,
//                PropagationState.SELF, 1));
//        counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.LOW,
//                PropagationState.ALL, 1));
//        callData.add(new ExpressionCallData(DataType.AFFYMETRIX, counts, 0, null, null, null, null));
//        
//        ExpressionCall call2 = new ExpressionCall(new Gene("g1", new Species(1), new GeneBioType("b")), 
//                new Condition(ae1, ds1, sp1), selfSelfPropagation, ExpressionSummary.EXPRESSED, 
//                SummaryQuality.SILVER, callData, new ExpressionLevelInfo(new BigDecimal(125.00)));
//        callTypeMap = new HashMap<>();
//        callTypeMap.put(ExpressionSummary.EXPRESSED, SummaryQuality.GOLD);
//        callFilter = new ExpressionCallFilter(callTypeMap, null, Collections.singleton(cond1), 
//                null, expressedData, null, null);
//        assertFalse("Call should not pass the filter", callFilter.test(call2));
//
//        // Test gene filter
//        Set<ConditionFilter> validConditionFilters = new HashSet<>();
//        validConditionFilters.add(new ConditionFilter(Arrays.asList("ae1", "ae2"), Arrays.asList("ds1", "ds2")));
//        callFilter = new ExpressionCallFilter(null, Collections.singleton(new GeneFilter(1, "g1")),
//                validConditionFilters, null, null, null, null);
//        assertTrue("Call should pass the filter", callFilter.test(call1));
//
//        Set<ConditionFilter> notValidConditionFilters = new HashSet<>();
//        notValidConditionFilters.add(new ConditionFilter(Arrays.asList("ae1", "ae2"), Arrays.asList("ds2")));
//        callFilter = new ExpressionCallFilter(null, Collections.singleton(new GeneFilter(1, "g1")),
//                notValidConditionFilters, null, null, null, null);
//        assertFalse("Call should not pass the filter", callFilter.test(call1));
//
//        // Test gene filter
//        callFilter = new ExpressionCallFilter(null, Collections.singleton(new GeneFilter(1, "g2")),
//                validConditionFilters, null, null, null, null);
//        assertFalse("Call should not pass the filter", callFilter.test(call1));
//    }
}
