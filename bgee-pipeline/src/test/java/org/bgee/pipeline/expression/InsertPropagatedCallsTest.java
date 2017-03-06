package org.bgee.pipeline.expression;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.TestAncestor;

/**
 * Unit tests for {@link InsertPropagatedCalls}.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Feb. 2017
 * @since   Bgee 14, Feb. 2017
 */
public class InsertPropagatedCallsTest extends TestAncestor {

//    private final static DataPropagation dpSelfAndSelf = 
//        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true);
//    private final static DataPropagation dpSelfAndDesc = 
//        new DataPropagation(PropagationState.SELF, PropagationState.DESCENDANT, false);
//    private final static DataPropagation dpSelfAndSelfDesc = 
//        new DataPropagation(PropagationState.SELF, PropagationState.SELF_AND_DESCENDANT, true);
//    private final static DataPropagation dpSelfDescAndAll= 
//        new DataPropagation(PropagationState.SELF_AND_DESCENDANT, PropagationState.ALL, true);
//    private final static DataPropagation dpDescAndSelf = 
//        new DataPropagation(PropagationState.DESCENDANT, PropagationState.SELF, false);
//    private final static DataPropagation dpDescAndDesc = 
//        new DataPropagation(PropagationState.DESCENDANT, PropagationState.DESCENDANT, false);
//    private final static DataPropagation dpAncAndSelf = 
//        new DataPropagation(PropagationState.ANCESTOR, PropagationState.SELF, false);
//    private final static DataPropagation dpSelfAncAndSelf = 
//        new DataPropagation(PropagationState.SELF_AND_ANCESTOR, PropagationState.SELF, true);
//    private final static DataPropagation dpSelfDescAndDesc = 
//        new DataPropagation(PropagationState.SELF_AND_DESCENDANT, PropagationState.DESCENDANT, false);
//    private final static DataPropagation dpSelfDescAndSelf = 
//        new DataPropagation(PropagationState.SELF_AND_DESCENDANT, PropagationState.SELF, true);

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(InsertPropagatedCallsTest.class.getName());
    
    /**
     * Default Constructor. 
     */
    public InsertPropagatedCallsTest() {
        super();
    }
    
    @Override
    protected Logger getLogger() {
        return log;
    }

//  /**
//  * Test the method 
//  * {@link CallService#propagateExpressionCalls(Collection, Collection, ConditionUtils, String)}.
//  */
// @Test
// public void shouldPropagateExpressedCalls() {
//     DAOManager manager = mock(DAOManager.class);
//     ServiceFactory serviceFactory = mock(ServiceFactory.class);
//     when(serviceFactory.getDAOManager()).thenReturn(manager);
//
//     CallService service = new CallService(serviceFactory);
//     try {
//         service.propagateExpressionCalls(null, null, null, null);
//         fail("Should throw an IllegalArgumentException");
//     } catch (IllegalArgumentException e) {
//         // test passed
//     }
//
//     ConditionUtils mockConditionUtils = mock(ConditionUtils.class);
//     String speciesId = "speciesId1";
//     
//     Set<Condition> conditions = new HashSet<>(Arrays.asList(
//             new Condition("organA", "stageA", speciesId),
//             new Condition("organA", "parentStageA1", speciesId),
//             new Condition("parentOrganA1", "stageA", speciesId),
//             new Condition("parentOrganA1", "parentStageA1", speciesId),
//             new Condition("organB", "stageB", speciesId)));
//     when(mockConditionUtils.getConditions()).thenReturn(conditions);
//     when(mockConditionUtils.isInferredAncestralConditions()).thenReturn(true);
//     
//     Condition childCond = new Condition("organA", "stageA", speciesId);
//     Set<Condition> ancestorConds = new HashSet<>(Arrays.asList(
//             new Condition("organA", "parentStageA1", speciesId),
//             new Condition("parentOrganA1", "stageA", speciesId),
//             new Condition("parentOrganA1", "parentStageA1", speciesId)));
//     when(mockConditionUtils.getAncestorConditions(childCond, false)).thenReturn(ancestorConds);
//     
//     childCond = new Condition("organA", "parentStageA1", speciesId);
//     ancestorConds = new HashSet<>(Arrays.asList(
//             new Condition("parentOrganA1", "parentStageA1", speciesId)));
//     when(mockConditionUtils.getAncestorConditions(childCond, false)).thenReturn(ancestorConds);
//
//     childCond = new Condition("parentOrganA1", "parentStageA1", speciesId);
//     ancestorConds = new HashSet<>();
//     when(mockConditionUtils.getAncestorConditions(childCond, false)).thenReturn(ancestorConds);
//
//     childCond = new Condition("organB", "stageB", speciesId);
//     ancestorConds = new HashSet<>(Arrays.asList(new Condition("organB", "parentStageB1", speciesId)));
//     when(mockConditionUtils.getAncestorConditions(childCond, false)).thenReturn(ancestorConds);
//
//     ExpressionCall call1 = new ExpressionCall("geneA", new Condition("organA", "stageA", speciesId),
//         dpSelfAndSelf, ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
//             new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndSelf),
//             new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)),
//         new BigDecimal("1.25"));
//     ExpressionCall call2 = new ExpressionCall("geneA", new Condition("organA", "parentStageA1", speciesId),
//         dpSelfAndSelf, ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
//             new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.EST, dpSelfAndSelf),
//             new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpSelfAndSelf)),
//         new BigDecimal("1250"));
//     ExpressionCall call3 = new ExpressionCall("geneB", new Condition("parentOrganA1", "parentStageA1", speciesId),
//         dpSelfAndSelf, ExpressionSummary.EXPRESSED, DataQuality.LOW, Arrays.asList(
//             new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, dpSelfAndSelf)),
//         new BigDecimal("10"));
//     ExpressionCall call4 = new ExpressionCall("geneB", new Condition("organB", "stageB", speciesId),
//         dpSelfAndSelf, ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
//             new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelf)),
//         new BigDecimal("111"));
//     
//     Collection<ExpressionCall> exprCalls = Arrays.asList(call1, call2, call3, call4);
//     
//     Set<ExpressionCall> allResults = new HashSet<>(Arrays.asList(
//             // From ExpressionCall 1
//             new ExpressionCall("geneA", new Condition("organA", "stageA", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)), 
//                     new BigDecimal("1.25"), Arrays.asList(call1)), 
//             
//             new ExpressionCall("geneA", new Condition("organA", "parentStageA1", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndDesc),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndDesc)), 
//                     null, Arrays.asList(call1)),
//
//             new ExpressionCall("geneA", new Condition("parentOrganA1", "stageA", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpDescAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpDescAndSelf)), 
//                     null, Arrays.asList(call1)),
//
//             new ExpressionCall("geneA", new Condition("parentOrganA1", "parentStageA1", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpDescAndDesc),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpDescAndDesc)), 
//                     null, Arrays.asList(call1)),
//
//             // From ExpressionCall 2
//             new ExpressionCall("geneA", new Condition("organA", "parentStageA1", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.EST, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpSelfAndSelf)), 
//                     new BigDecimal("1250"), Arrays.asList(call2)),
//             
//             new ExpressionCall("geneA", new Condition("parentOrganA1", "parentStageA1", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.EST, dpDescAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpDescAndSelf)), 
//                     null, Arrays.asList(call2)),
//
//             // From ExpressionCall 3
//             new ExpressionCall("geneB", new Condition("parentOrganA1", "parentStageA1", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, dpSelfAndSelf)), 
//                     new BigDecimal("10"), Arrays.asList(call3)),
//
//             // From ExpressionCall 4
//             new ExpressionCall("geneB", new Condition("organB", "stageB", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelf)), 
//                     new BigDecimal("111"), Arrays.asList(call4)),
//             new ExpressionCall("geneB", new Condition("organB", "parentStageB1", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndDesc)), 
//                     null, Arrays.asList(call4))));
//
//     Set<ExpressionCall> actualResults = service.propagateExpressionCalls(
//             exprCalls, new HashSet<>(), mockConditionUtils, speciesId);
//     assertEquals("Incorrect ExpressionCalls generated", allResults, actualResults);
//     
//     final Set<String> allOrgans = allResults.stream().map(c -> c.getCondition().getAnatEntityId()).collect(Collectors.toSet());
//     final Set<String> allStages = allResults.stream().map(c -> c.getCondition().getDevStageId()).collect(Collectors.toSet());
//     
//     Set<String> allowedOrganIds = new HashSet<>(Arrays.asList("organA"));
//     Set<ExpressionCall> expectedResults = allResults.stream()
//             .filter(c -> allowedOrganIds.contains(c.getCondition().getAnatEntityId()))
//             .collect(Collectors.toSet());
//     actualResults = service.propagateExpressionCalls(exprCalls,
//         allowedOrganIds.stream()
//         .map(o -> allStages.stream()
//                 .map(s -> new Condition(o, s, speciesId)).collect(Collectors.toSet()))
//         .flatMap(Set::stream)
//         .collect(Collectors.toSet()),
//         mockConditionUtils, speciesId);
//     assertEquals("Incorrect ExpressionCalls generated", expectedResults, actualResults);
//
//     Set<String> allowedStageIds = new HashSet<>(Arrays.asList("parentStageA1"));
//     expectedResults = allResults.stream()
//             .filter(c -> allowedStageIds.contains(c.getCondition().getDevStageId()))
//             .collect(Collectors.toSet());
//     actualResults = service.propagateExpressionCalls(exprCalls,
//         allOrgans.stream()
//         .map(o -> allowedStageIds.stream()
//                 .map(s -> new Condition(o, s, speciesId)).collect(Collectors.toSet()))
//         .flatMap(Set::stream)
//         .collect(Collectors.toSet()),
//         mockConditionUtils, speciesId);
//     assertEquals("Incorrect ExpressionCalls generated", expectedResults, actualResults);
//
//     expectedResults = allResults.stream()
//             .filter(c -> allowedOrganIds.contains(c.getCondition().getAnatEntityId()))
//             .filter(c -> allowedStageIds.contains(c.getCondition().getDevStageId()))
//             .collect(Collectors.toSet());
//             
//     actualResults = service.propagateExpressionCalls(exprCalls,
//         allowedOrganIds.stream()
//         .map(o -> allowedStageIds.stream()
//                 .map(s -> new Condition(o, s, speciesId)).collect(Collectors.toSet()))
//         .flatMap(Set::stream)
//         .collect(Collectors.toSet()),
//         mockConditionUtils, speciesId);
//     assertEquals("Incorrect ExpressionCalls generated", expectedResults, actualResults);
// }
// 
// /**
//  * Test the method {@link CallService#propagateExpressionCalls(Collection, Collection, ConditionUtils, String)}.
//  */
// @Test
// public void shouldPropagateNotExpressedCalls() {
//     DAOManager manager = mock(DAOManager.class);
//     ServiceFactory serviceFactory = mock(ServiceFactory.class);
//     when(serviceFactory.getDAOManager()).thenReturn(manager);
//
//     CallService service = new CallService(serviceFactory);
//     try {
//         service.propagateExpressionCalls(null, null, null, null);
//         fail("Should throw an IllegalArgumentException");
//     } catch (IllegalArgumentException e) {
//         // test passed
//     }
//
//     ConditionUtils mockConditionUtils = mock(ConditionUtils.class);
//     String speciesId = "speciesId1";
//
//     Set<Condition> conditions = new HashSet<>(Arrays.asList(
//             new Condition("organA", "stageA", speciesId),
//             new Condition("organA", "parentStageA1", speciesId),
//             new Condition("parentOrganA1", "parentStageA1", speciesId),
//             new Condition("parentOrganA2", "parentStageA1", speciesId),
//             new Condition("organB", "stageB", speciesId),
//             new Condition("parentOrganB1", "stageB", speciesId),
//             new Condition("parentOrganB2", "stageB", speciesId)));
//     when(mockConditionUtils.getConditions()).thenReturn(conditions);
//     when(mockConditionUtils.isInferredAncestralConditions()).thenReturn(true);
//     
//     Condition parentCond = new Condition("organA", "stageA", speciesId);
//     Set<Condition> descendantConds = new HashSet<>();
//     when(mockConditionUtils.getDescendantConditions(parentCond, false, false)).thenReturn(descendantConds);
//     
//     parentCond = new Condition("organA", "parentStageA1", speciesId);
//     descendantConds = new HashSet<>();
//     when(mockConditionUtils.getDescendantConditions(parentCond, false, false)).thenReturn(descendantConds);
//
//     parentCond = new Condition("parentOrganA2", "parentStageA1", speciesId);
//     descendantConds = new HashSet<>(Arrays.asList(
//             new Condition("parentOrganA1", "parentStageA1", speciesId),
//             new Condition("organA", "parentStageA1", speciesId)));
//     when(mockConditionUtils.getDescendantConditions(parentCond, false, false)).thenReturn(descendantConds);
//
//     parentCond = new Condition("parentOrganB2", "stageB", speciesId);
//     descendantConds = new HashSet<>(Arrays.asList(
//             new Condition("parentOrganB1", "stageB", speciesId),
//             new Condition("organB", "stageB", speciesId)));
//     when(mockConditionUtils.getDescendantConditions(parentCond, false, false)).thenReturn(descendantConds);
//     
//     ExpressionCall call1 = new ExpressionCall("geneA", new Condition("organA", "stageA", speciesId),
//             dpSelfAndSelf, ExpressionSummary.NOT_EXPRESSED, DataQuality.LOW, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW,
//                             DataType.AFFYMETRIX, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW,
//                             DataType.IN_SITU, dpSelfAndSelf)),
//             new BigDecimal("2.25"));
//     
//     ExpressionCall call2 = new ExpressionCall("geneA", new Condition("organA", "parentStageA1", speciesId),
//         dpSelfAndSelf, ExpressionSummary.NOT_EXPRESSED, DataQuality.HIGH, Arrays.asList(
//             new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW,
//                     DataType.AFFYMETRIX, dpSelfAndSelf),
//             new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH,
//                     DataType.RNA_SEQ, dpSelfAndSelf)),
//     new BigDecimal("2250"));
//
//     ExpressionCall call3 = new ExpressionCall("geneA", new Condition("parentOrganA2", "parentStageA1", speciesId),
//         dpSelfAndSelf, ExpressionSummary.NOT_EXPRESSED, DataQuality.HIGH, Arrays.asList(
//             new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH,
//                 DataType.AFFYMETRIX, dpSelfAndSelf),
//             new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW,
//                 DataType.IN_SITU, dpSelfAndSelf),
//             new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW,
//                 DataType.RNA_SEQ, dpSelfAndSelf)),
//         new BigDecimal("20"));
//     
//     ExpressionCall call4 =new ExpressionCall("geneB", new Condition("parentOrganB2", "stageB", speciesId),
//         dpSelfAndSelf, ExpressionSummary.NOT_EXPRESSED, DataQuality.HIGH, Arrays.asList(
//             new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH,
//                 DataType.AFFYMETRIX, dpSelfAndSelf)),
//         new BigDecimal("211"));
//     
//     Collection<ExpressionCall> noExprCalls = Arrays.asList(call1, call2, call3, call4);
//
//     Set<ExpressionCall> allResults = new HashSet<>(Arrays.asList(
//             // From ExpressionCall 1
//             new ExpressionCall("geneA", new Condition("organA", "stageA", speciesId), 
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpSelfAndSelf)), 
//                     new BigDecimal("2.25"), Arrays.asList(call1)),
//             
//             // From ExpressionCall 2
//             new ExpressionCall("geneA", new Condition("organA", "parentStageA1", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)), 
//                     new BigDecimal("2250"), Arrays.asList(call2)), 
//
//             // From ExpressionCall 3
//             new ExpressionCall("geneA", new Condition("parentOrganA2", "parentStageA1", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpSelfAndSelf), 
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, dpSelfAndSelf)), 
//                     new BigDecimal("20"), Arrays.asList(call3)), 
//
//             new ExpressionCall("geneA", new Condition("parentOrganA1", "parentStageA1", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpAncAndSelf),
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpAncAndSelf), 
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, dpAncAndSelf)), 
//                     null, Arrays.asList(call3)), 
//
//             new ExpressionCall("geneA", new Condition("organA", "parentStageA1", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpAncAndSelf),
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpAncAndSelf), 
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, dpAncAndSelf)), 
//                     null, Arrays.asList(call3)),
//
//             // From ExpressionCall 4
//             new ExpressionCall("geneB", new Condition("parentOrganB2", "stageB", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelf)), 
//                     new BigDecimal("211"), Arrays.asList(call4)),
//
//             new ExpressionCall("geneB", new Condition("parentOrganB1", "stageB", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpAncAndSelf)), 
//                     null, Arrays.asList(call4)),
//
//             new ExpressionCall("geneB", new Condition("organB", "stageB", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpAncAndSelf)), 
//                     null, Arrays.asList(call4))));
//
//     final Set<String> allOrgans = allResults.stream().map(c -> c.getCondition().getAnatEntityId()).collect(Collectors.toSet());
//     final Set<String> allStages = allResults.stream().map(c -> c.getCondition().getDevStageId()).collect(Collectors.toSet());
//
//     Set<ExpressionCall> actualResults = service.propagateExpressionCalls(
//             noExprCalls, null, mockConditionUtils, speciesId);
//     assertEquals("Incorrect ExpressionCalls generated", allResults, actualResults);
//     
//     Set<String> allowedOrganIds = new HashSet<>(Arrays.asList("organA"));
//     Set<ExpressionCall> expectedResults = allResults.stream()
//             .filter(c -> allowedOrganIds.contains(c.getCondition().getAnatEntityId()))
//             .collect(Collectors.toSet());
//     actualResults = service.propagateExpressionCalls(noExprCalls,
//         allowedOrganIds.stream()
//             .map(o -> allStages.stream()
//                 .map(s -> new Condition(o, s, speciesId)).collect(Collectors.toSet()))
//             .flatMap(Set::stream)
//             .collect(Collectors.toSet()),
//         mockConditionUtils, speciesId);
//     assertEquals("Incorrect ExpressionCalls generated", expectedResults, actualResults);
//
//     Set<String> allowedStageIds = new HashSet<>(Arrays.asList("parentStageA1"));
//     expectedResults = allResults.stream()
//             .filter(c -> allowedStageIds.contains(c.getCondition().getDevStageId()))
//             .collect(Collectors.toSet());
//     actualResults = service.propagateExpressionCalls(noExprCalls,
//         allOrgans.stream()
//         .map(o -> allowedStageIds.stream()
//             .map(s -> new Condition(o, s, speciesId)).collect(Collectors.toSet()))
//         .flatMap(Set::stream)
//         .collect(Collectors.toSet()),
//             mockConditionUtils, speciesId);
//     assertEquals("Incorrect ExpressionCalls generated", expectedResults, actualResults);
//
//     expectedResults = allResults.stream()
//             .filter(c -> allowedOrganIds.contains(c.getCondition().getAnatEntityId()))
//             .filter(c -> allowedStageIds.contains(c.getCondition().getDevStageId()))
//             .collect(Collectors.toSet());
//     actualResults = service.propagateExpressionCalls(noExprCalls, 
//         allowedOrganIds.stream()
//             .map(o -> allowedStageIds.stream()
//                 .map(s -> new Condition(o, s, speciesId)).collect(Collectors.toSet()))
//             .flatMap(Set::stream)
//             .collect(Collectors.toSet()),
//         mockConditionUtils, speciesId);
//     assertEquals("Incorrect ExpressionCalls generated", expectedResults, actualResults);
// }
// 
// /**
//  * @return  the {@code Set} of {@code ExpressionCall}s from 
//  * not expressed propagated {@code ExpressionCall}s,
//  */
// private Set<ExpressionCall> getPropagationFromNotExpressedCalls(String speciesId) {
//     return new HashSet<>(Arrays.asList(
//             new ExpressionCall("ID1", new Condition("Anat_id1", "Stage_id1", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelf)), 
//                     null), 
//             new ExpressionCall("ID1", new Condition("Anat_id2", "Stage_id1", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpAncAndSelf)), 
//                     null), 
//
//             new ExpressionCall("ID2", new Condition("Anat_id1", "Stage_id2", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)), 
//                     null), 
//             new ExpressionCall("ID2", new Condition("Anat_id2", "Stage_id2", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpAncAndSelf)), 
//                     null), 
//             new ExpressionCall("ID2", new Condition("Anat_id3", "ParentStage_id2", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)), 
//                     null), 
//             new ExpressionCall("ID2", new Condition("Anat_id3", "Stage_id2", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpAncAndSelf)), 
//                     null), 
//
//             new ExpressionCall("ID4", new Condition("Anat_id1", "Stage_id5", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)), 
//                     null), 
//             new ExpressionCall("ID4", new Condition("Anat_id4", "Stage_id5", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAncAndSelf), 
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpSelfAncAndSelf), 
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAncAndSelf)), 
//                     null), 
//             new ExpressionCall("ID4", new Condition("Anat_id5", "Stage_id5", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpAncAndSelf), 
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpAncAndSelf), 
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpAncAndSelf)), 
//                     null), 
//
//             new ExpressionCall("ID5", new Condition("Anat_id1", "Stage_id5", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)), 
//                     null), 
//             new ExpressionCall("ID5", new Condition("Anat_id5", "Stage_id5", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpAncAndSelf)), 
//                     null),
//             
//             new ExpressionCall("ID6", new Condition("Anat_id9", "Stage_id7", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)), 
//                     null),
//             new ExpressionCall("ID6", new Condition("Anat_id8", "Stage_id6", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpAncAndSelf)), 
//                     null)));
// }
// 
// /**
//  * @return  the {@code Set} of not expressed {@code ExpressionCall}s 
//  * from not expressed propagated {@code ExpressionCall}s,
//  */
// private Set<ExpressionCall> getPropagationFromExpressedCalls(String speciesId) {
//     
//     return new HashSet<>(Arrays.asList(
//             new ExpressionCall("ID1", new Condition("Anat_id1", "Stage_id1", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, dpSelfAndSelf)), 
//                     null), 
//             new ExpressionCall("ID1", new Condition("Anat_id1", "ParentStage_id1", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndDesc),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpSelfAndDesc),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, dpSelfAndDesc)), 
//                     null), 
//             new ExpressionCall("ID1", new Condition("Anat_id1", "ParentStage_id2", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.EST, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpSelfAndSelf)), 
//                     null), 
//             new ExpressionCall("ID1", new Condition("Anat_id1", "Stage_id2", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpSelfAndSelf)), 
//                     null), 
//             
//             new ExpressionCall("ID2", new Condition("Anat_id1", "Stage_id2", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndSelf)), 
//                     null), 
//             new ExpressionCall("ID2", new Condition("Anat_id1", "ParentStage_id2", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfDescAndDesc),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfDescAndDesc),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpSelfDescAndDesc)), 
//                     null), 
//             new ExpressionCall("ID2", new Condition("Anat_id2", "ParentStage_id2", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpDescAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpDescAndSelf)), 
//                     null), 
//             new ExpressionCall("ID2", new Condition("Anat_id3", "ParentStage_id2", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpSelfAndSelf)), 
//                     null), 
//             new ExpressionCall("ID2", new Condition("NonInfoAnatEnt1", "ParentStage_id2", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpSelfAndSelf)), 
//                     null), 
//
//             new ExpressionCall("ID3", new Condition("Anat_id1", "Stage_id2", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfDescAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfDescAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpSelfDescAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfDescAndSelf)), 
//                     null), 
//             new ExpressionCall("ID3", new Condition("Anat_id4", "Stage_id2", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpDescAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpDescAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpDescAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, dpDescAndSelf)), 
//                     null), 
//             new ExpressionCall("ID3", new Condition("Anat_id5", "Stage_id2", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, dpSelfAndSelf)), 
//                     null), 
//
//             new ExpressionCall("ID5", new Condition("Anat_id1", "Stage_id5", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpDescAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpDescAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpDescAndSelf)), 
//                     null), 
//             new ExpressionCall("ID5", new Condition("Anat_id1", "ParentStage_id5", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpDescAndDesc),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpDescAndDesc),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpDescAndDesc)), 
//                     null), 
//             new ExpressionCall("ID5", new Condition("Anat_id4", "Stage_id5", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpSelfAndSelf)), 
//                     null), 
//             new ExpressionCall("ID5", new Condition("Anat_id4", "ParentStage_id5", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndDesc),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndDesc),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpSelfAndDesc)), 
//                     null)));
// }
// 
// /**
//  * Test the method {@link CallService#reconcileSingleGeneCalls(Set)}.
//  */
// @Test
// public void shouldReconcileSingleGeneCalls() {
//     String speciesId = "speciesId1";
//
//     ExpressionCall descendantCall =  new ExpressionCall("geneA", new Condition("organB", "stageB1", speciesId),
//         null, null, null, Arrays.asList(
//             new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelf)), 
//         null);
//     ExpressionCall parentCall =  new ExpressionCall("geneA", new Condition("parentOrganA1", "stageA", speciesId),
//         null, null, null, Arrays.asList(
//             new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndSelf)), 
//         null);
//     
//     // EXPRESSED - HIGH quality - observed
//     Set<ExpressionCall> inputCalls = new HashSet<>(Arrays.asList(
//             new ExpressionCall("geneA", new Condition("organA", "stageA", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)), 
//                     new BigDecimal("1.25")), 
//             new ExpressionCall("geneA", new Condition("organA", "stageA", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpDescAndSelf)), 
//                     new BigDecimal("12.5"), Arrays.asList(parentCall)),
//             new ExpressionCall("geneA", new Condition("organB", "parentStageB1", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndDesc)), 
//                     new BigDecimal("125"), Arrays.asList(descendantCall))));
//     
//     ExpressionCall expectedResult = new ExpressionCall("geneA", null, 
//             new DataPropagation(PropagationState.SELF_AND_DESCENDANT, PropagationState.SELF_AND_DESCENDANT, true), 
//             ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
//                 new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndSelf),
//                 new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf), 
//                 new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpDescAndSelf), 
//                 new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndDesc)), 
//             new BigDecimal("1.25"), Arrays.asList(descendantCall, parentCall));
//     ExpressionCall actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
//     assertEquals("Incorrect ExpressionCall generated", expectedResult, actualResult);
//     
//     inputCalls = new HashSet<>(Arrays.asList(
//             new ExpressionCall("geneA", new Condition("organA", "stageA", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpDescAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)), 
//                     new BigDecimal("1.25")), 
//             new ExpressionCall("geneA", new Condition("organB", "parentStageB1", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelfDesc)), 
//                     null)));
//     expectedResult = new ExpressionCall("geneA", null, 
//             new DataPropagation(PropagationState.SELF_AND_DESCENDANT, PropagationState.SELF_AND_DESCENDANT, true), 
//             ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpDescAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelfDesc)), 
//             new BigDecimal("1.25"));
//     actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
//     assertEquals("Incorrect ExpressionCall generated", expectedResult, actualResult);
//
//     // EXPRESSED - LOW quality - not observed
//     inputCalls = new HashSet<>(Arrays.asList(
//             new ExpressionCall("geneA", new Condition("organA", "stageA", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpDescAndSelf)), 
//                     null), 
//             new ExpressionCall("geneA", new Condition("organB", "parentStageB1", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndDesc)), 
//                     null)));
//     expectedResult = new ExpressionCall("geneA", null, 
//             new DataPropagation(PropagationState.SELF_AND_DESCENDANT, PropagationState.SELF_AND_DESCENDANT, false), 
//             ExpressionSummary.EXPRESSED, DataQuality.LOW, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpDescAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndDesc)), 
//             null);
//     actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
//     assertEquals("Incorrect ExpressionCall generated", expectedResult, actualResult);
//
//     // NOT_EXPRESSED - HIGH quality - not observed
//     inputCalls = new HashSet<>(Arrays.asList(
//             new ExpressionCall("geneA", new Condition("organA", "stageA", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpAncAndSelf)), 
//                     new BigDecimal("1.25")), 
//             new ExpressionCall("geneA", new Condition("organB", "parentStageB1", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpAncAndSelf)), 
//                     new BigDecimal("1.25"))));
//     expectedResult = new ExpressionCall("geneA", null, 
//             new DataPropagation(PropagationState.ANCESTOR, PropagationState.SELF, false), 
//             ExpressionSummary.NOT_EXPRESSED, DataQuality.HIGH, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpAncAndSelf),
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpAncAndSelf)), 
//             new BigDecimal("1.25"));
//     actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
//     assertEquals("Incorrect ExpressionCall generated", expectedResult, actualResult);
//
//     // WEAK_AMBIGUITY - null - not observed
//     inputCalls = new HashSet<>(Arrays.asList(
//             new ExpressionCall("geneA", new Condition("organA", "stageA", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpAncAndSelf)), 
//                     null), 
//             new ExpressionCall("geneA", new Condition("organB", "parentStageB1", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndDesc)), 
//                     null)));
//     expectedResult = new ExpressionCall("geneA", null, 
//             new DataPropagation(PropagationState.SELF_AND_ANCESTOR, PropagationState.SELF_AND_DESCENDANT, false), 
//             ExpressionSummary.WEAK_AMBIGUITY, null, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpAncAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndDesc)), 
//             null);
//     actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
//     assertEquals("Incorrect ExpressionCall generated", expectedResult, actualResult);
//
//     // WEAK_AMBIGUITY - null - observed
//     inputCalls = new HashSet<>(Arrays.asList(
//             new ExpressionCall("geneA", new Condition("organA", "stageA", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpAncAndSelf)), 
//                     null), 
//             new ExpressionCall("geneA", new Condition("organA", "parentStageB1", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpAncAndSelf)), 
//                     null), 
//             new ExpressionCall("geneA", new Condition("organB", "parentStageB1", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfDescAndAll)), 
//                     null)));
//     expectedResult = new ExpressionCall("geneA", null, 
//             new DataPropagation(PropagationState.ALL, PropagationState.ALL, true), 
//             ExpressionSummary.WEAK_AMBIGUITY, null, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpAncAndSelf),
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpAncAndSelf), 
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfDescAndAll)), 
//             null);
//     actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
//     assertEquals("Incorrect ExpressionCall generated", expectedResult, actualResult);
//
//     // STRONG_AMBIGUITY - null - observed
//     inputCalls = new HashSet<>(Arrays.asList(
//             new ExpressionCall("geneA", new Condition("organA", "stageA", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndSelf)), 
//                     null), 
//             new ExpressionCall("geneA", new Condition("organB", "parentStageB1", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndDesc)), 
//                     null)));
//     expectedResult = new ExpressionCall("geneA", null, 
//             new DataPropagation(PropagationState.SELF, PropagationState.SELF_AND_DESCENDANT, true), 
//             ExpressionSummary.STRONG_AMBIGUITY, null, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndDesc)), 
//             null);
//     actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
//     assertEquals("Incorrect ExpressionCall generated", expectedResult, actualResult);
//
//     // WEAK_AMBIGUITY - null - not observed
//     inputCalls = new HashSet<>(Arrays.asList(
//             new ExpressionCall("geneA", new Condition("organA", "stageA", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpAncAndSelf)), 
//                     null), 
//             new ExpressionCall("geneA", new Condition("organB", "parentStageB1", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpDescAndDesc)), 
//                     null)));
//     expectedResult = new ExpressionCall("geneA", null, 
//             new DataPropagation(PropagationState.ANCESTOR_AND_DESCENDANT, PropagationState.SELF_AND_DESCENDANT, false), 
//             ExpressionSummary.WEAK_AMBIGUITY, null, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpAncAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpDescAndDesc)), 
//             null);
//     actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
//     assertEquals("Incorrect ExpressionCall generated", expectedResult, actualResult);
//
//     // Two different gene IDs
//     inputCalls = new HashSet<>(Arrays.asList(
//             new ExpressionCall("geneA", new Condition("organA", "stageA", speciesId),
//                     null, null, null, null, null), 
//             new ExpressionCall("geneB", new Condition("organA", "stageA", speciesId),
//                     null, null, null, null, null)));
//     try {
//         CallService.reconcileSingleGeneCalls(inputCalls);
//         fail("Should throw an IllegalArgumentException");
//     } catch (IllegalArgumentException e) {
//         // Test passed
//     }
//
//     // Reconciliation of DataPropagation.ANCESTOR with DataPropagation.DESCENDANT
//     inputCalls = new HashSet<>(Arrays.asList(
//             new ExpressionCall("geneA", new Condition("organA", "stageA", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, 
//                             new DataPropagation(PropagationState.SELF_OR_ANCESTOR, PropagationState.SELF, false))), 
//                     null), 
//             new ExpressionCall("geneA", new Condition("organB", "parentStageB1", speciesId),
//                     null, null, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpDescAndDesc)), 
//                     null)));
//     try {
//         CallService.reconcileSingleGeneCalls(inputCalls);
//         fail("Should throw an IllegalArgumentException");
//     } catch (IllegalArgumentException e) {
//         // Test passed
//     }
// }
// 
// /**
//  * Test the method {@link CallService#reconcileSingleGeneCalls(Set)}.
//  */
// @Test
// public void shouldReconcileSingleGeneCalls_pipelineTest() {
//     String geneId = "ID1";
//     String speciesId = "speciesId1";
//     Condition cond = new Condition("Anat_id1", "Stage_id1", speciesId);
//     Set<ExpressionCall> inputCalls = filterExprCalls(geneId, cond, speciesId);
//     inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
//     ExpressionCall expectedResult = new ExpressionCall(geneId, null, 
//             dpSelfAndSelf, ExpressionSummary.STRONG_AMBIGUITY, null, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, dpSelfAndSelf)), 
//             null);
//     ExpressionCall actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
//     assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);
//     
//     cond = new Condition("Anat_id1", "ParentStage_id1", speciesId);
//     inputCalls = filterExprCalls(geneId, cond, speciesId);
//     inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
//     expectedResult = new ExpressionCall(geneId, null, 
//             dpSelfAndDesc, ExpressionSummary.EXPRESSED, DataQuality.LOW, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndDesc),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpSelfAndDesc),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, dpSelfAndDesc)), 
//             null);
//     actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
//     assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);
//
//     cond = new Condition("Anat_id1", "ParentStage_id2", speciesId);
//     inputCalls = filterExprCalls(geneId, cond, speciesId);
//     inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
//     expectedResult = new ExpressionCall(geneId, null, 
//             dpSelfAndSelf, ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.EST, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpSelfAndSelf)), 
//             null);
//     actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
//     assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);
//
//     cond = new Condition("Anat_id1", "Stage_id2", speciesId);
//     inputCalls = filterExprCalls(geneId, cond, speciesId);
//     inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
//     expectedResult = new ExpressionCall(geneId, null, 
//             dpSelfAndSelf, ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpSelfAndSelf)), 
//             null);
//     actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
//     assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);
//     
//     geneId = "ID2";
//     cond = new Condition("Anat_id1", "Stage_id2", speciesId);
//     inputCalls = filterExprCalls(geneId, cond, speciesId);
//     inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
//     expectedResult = new ExpressionCall(geneId, null, 
//             dpSelfAndSelf, ExpressionSummary.STRONG_AMBIGUITY, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndSelf), 
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)), 
//             null);
//     actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
//     assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);
//
//     cond = new Condition("Anat_id1", "ParentStage_id2", speciesId);
//     inputCalls = filterExprCalls(geneId, cond, speciesId);
//     inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
//     expectedResult = new ExpressionCall(geneId, null, 
//             dpSelfDescAndDesc, ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfDescAndDesc),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfDescAndDesc),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpSelfDescAndDesc)), 
//             null);
//     actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
//     assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);
//
//     cond = new Condition("Anat_id2", "Stage_id2", speciesId);
//     inputCalls = filterExprCalls(geneId, cond, speciesId);
//     inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
//     expectedResult = new ExpressionCall(geneId, null, 
//             dpAncAndSelf, ExpressionSummary.NOT_EXPRESSED, DataQuality.HIGH, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpAncAndSelf)), 
//             null);
//     actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
//     assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);
//
//     cond = new Condition("Anat_id2", "ParentStage_id2", speciesId);
//     inputCalls = filterExprCalls(geneId, cond, speciesId);
//     inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
//     expectedResult = new ExpressionCall(geneId, null, 
//             dpDescAndSelf, ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpDescAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpDescAndSelf)), 
//             null);
//     actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
//     assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);
//
//     cond = new Condition("Anat_id3", "ParentStage_id2", speciesId);
//     inputCalls = filterExprCalls(geneId, cond, speciesId);
//     inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
//     expectedResult = new ExpressionCall(geneId, null, 
//             dpSelfAndSelf, ExpressionSummary.STRONG_AMBIGUITY, null, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf), 
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpSelfAndSelf)), 
//             null);
//     actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
//     assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);
//
//     geneId = "ID3";
//     cond = new Condition("Anat_id1", "Stage_id2", speciesId);
//     inputCalls = filterExprCalls(geneId, cond, speciesId);
//     inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
//     expectedResult = new ExpressionCall(geneId, null, 
//             dpSelfDescAndSelf, ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfDescAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfDescAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpSelfDescAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfDescAndSelf)), 
//             null);
//     actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
//     assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);
//
//     cond = new Condition("Anat_id4", "Stage_id2", speciesId);
//     inputCalls = filterExprCalls(geneId, cond, speciesId);
//     inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
//     expectedResult = new ExpressionCall(geneId, null, 
//             dpDescAndSelf, ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpDescAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpDescAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpDescAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, dpDescAndSelf)), 
//             null);
//     actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
//     assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);
//     
//     cond = new Condition("Anat_id5", "Stage_id2", speciesId);
//     inputCalls = filterExprCalls(geneId, cond, speciesId);
//     inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
//     expectedResult = new ExpressionCall(geneId, null, 
//             dpSelfAndSelf, ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, dpSelfAndSelf)), 
//             null);
//     actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
//     assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);
//
//     geneId = "ID4";
//     cond = new Condition("Anat_id1", "Stage_id5", speciesId);
//     inputCalls = filterExprCalls(geneId, cond, speciesId);
//     inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
//     expectedResult = new ExpressionCall(geneId, null, 
//             dpSelfAndSelf, ExpressionSummary.NOT_EXPRESSED, DataQuality.HIGH, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)), 
//             null);
//     actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
//     assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);
//
//     cond = new Condition("Anat_id4", "Stage_id5", speciesId);
//     inputCalls = filterExprCalls(geneId, cond, speciesId);
//     inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
//     expectedResult = new ExpressionCall(geneId, null, 
//             dpSelfAncAndSelf, ExpressionSummary.NOT_EXPRESSED, DataQuality.HIGH, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAncAndSelf), 
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpSelfAncAndSelf), 
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAncAndSelf)), 
//             null);
//     actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
//     assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);
//     
//     cond = new Condition("Anat_id5", "Stage_id5", speciesId);
//     inputCalls = filterExprCalls(geneId, cond, speciesId);
//     inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
//     expectedResult = new ExpressionCall(geneId, null, 
//             dpAncAndSelf, ExpressionSummary.NOT_EXPRESSED, DataQuality.HIGH, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpAncAndSelf), 
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpAncAndSelf), 
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpAncAndSelf)), 
//             null);
//     actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
//     assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);
//
//     geneId = "ID5";
//     cond = new Condition("Anat_id1", "Stage_id5", speciesId);
//     inputCalls = filterExprCalls(geneId, cond, speciesId);
//     inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
//     expectedResult = new ExpressionCall(geneId, null, 
//             dpSelfDescAndSelf, ExpressionSummary.STRONG_AMBIGUITY, null, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpDescAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpDescAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpDescAndSelf), 
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)), 
//             null);
//     actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
//     assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);
//
//     cond = new Condition("Anat_id1", "ParentStage_id5", speciesId);
//     inputCalls = filterExprCalls(geneId, cond, speciesId);
//     inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
//     expectedResult = new ExpressionCall(geneId, null, 
//             dpDescAndDesc, ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpDescAndDesc),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpDescAndDesc),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpDescAndDesc)), 
//             null);
//     actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
//     assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);
//     
//     cond = new Condition("Anat_id4", "Stage_id5", speciesId);
//     inputCalls = filterExprCalls(geneId, cond, speciesId);
//     inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
//     expectedResult = new ExpressionCall(geneId, null, 
//             dpSelfAndSelf, ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndSelf),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpSelfAndSelf)), 
//             null);
//     actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
//     assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);
//
//     cond = new Condition("Anat_id4", "ParentStage_id5", speciesId);
//     inputCalls = filterExprCalls(geneId, cond, speciesId);
//     inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
//     expectedResult = new ExpressionCall(geneId, null, 
//             dpSelfAndDesc, ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndDesc),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndDesc),
//                     new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpSelfAndDesc)), 
//             null);
//     actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
//     assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);
//
//     geneId = "ID6";
//     cond = new Condition("Anat_id9", "Stage_id7", speciesId);
//     inputCalls = filterExprCalls(geneId, cond, speciesId);
//     inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
//     expectedResult = new ExpressionCall(geneId, null, 
//             dpSelfAndSelf, ExpressionSummary.NOT_EXPRESSED, DataQuality.HIGH, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)), 
//             null);
//     actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
//     assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);
//
//     cond = new Condition("Anat_id8", "Stage_id6", speciesId);
//     inputCalls = filterExprCalls(geneId, cond, speciesId);
//     inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
//     expectedResult = new ExpressionCall(geneId, null, 
//             dpAncAndSelf, ExpressionSummary.NOT_EXPRESSED, DataQuality.HIGH, Arrays.asList(
//                     new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpAncAndSelf)), 
//             null);
//     actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
//     assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);
// }
//
// private Set<ExpressionCall> filterExprCalls(String geneId, Condition cond, String speciesId) {
//     Set<ExpressionCall> inputCalls = getPropagationFromExpressedCalls(speciesId).stream()
//             .filter(c -> c.getGeneId().equals(geneId))
//             .filter(c -> c.getCondition().equals(cond))
//             .collect(Collectors.toSet());
//     return inputCalls;
// }
// 
// private Set<ExpressionCall> filterNotExprCalls(String geneId, Condition cond, String speciesId) {
//     Set<ExpressionCall> inputCalls = getPropagationFromNotExpressedCalls(speciesId).stream()
//             .filter(c -> c.getGeneId().equals(geneId))
//             .filter(c -> c.getCondition().equals(cond))
//             .collect(Collectors.toSet());
//     return inputCalls;
// }
// 
// @Test
// public void shouldTryAdvance() {
//     
//     List<ExpressionCallTO> callTOs = new ArrayList<ExpressionCallTO>();
//     callTOs.add(new ExpressionCallTO("expId1", "geneId1", null, null, null, null, null, null, null, null, null, null, null));
//     callTOs.add(new ExpressionCallTO("expId2", "geneId1", null, null, null, null, null, null, null, null, null, null, null));
//     callTOs.add(new ExpressionCallTO("expId3", "geneId1", null, null, null, null, null, null, null, null, null, null, null));
//     callTOs.add(new ExpressionCallTO("expId4", "geneId2", null, null, null, null, null, null, null, null, null, null, null));
//     callTOs.add(new ExpressionCallTO("expId5", "geneId2", null, null, null, null, null, null, null, null, null, null, null));
//     callTOs.add(new ExpressionCallTO("expId6", "geneId2", null, null, null, null, null, null, null, null, null, null, null));
//     
//     // Two streams well defined
//     List<ExperimentExpressionTO> expExprTOs1 = new ArrayList<ExperimentExpressionTO>();
//     expExprTOs1.add(new ExperimentExpressionTO("expId1", 1, null, null, null, null));
//     expExprTOs1.add(new ExperimentExpressionTO("expId1", 2, null, null, null, null));
//     expExprTOs1.add(new ExperimentExpressionTO("expId2", 3, null, null, null, null));
//     expExprTOs1.add(new ExperimentExpressionTO("expId5", 4, null, null, null, null));
//     Stream<ExperimentExpressionTO> stream1 = expExprTOs1.stream();
//
//     List<ExperimentExpressionTO> expExprTOs2 = new ArrayList<ExperimentExpressionTO>();
//     expExprTOs2.add(new ExperimentExpressionTO("expId1", 5, null, null, null, null));
//     expExprTOs2.add(new ExperimentExpressionTO("expId2", 6, null, null, null, null));
//     expExprTOs2.add(new ExperimentExpressionTO("expId4", 7, null, null, null, null));
//     Stream<ExperimentExpressionTO> stream2 = expExprTOs2.stream();
//
//     Set<Stream<ExperimentExpressionTO>> set = new HashSet<>();
//     set.add(stream1);
//     set.add(stream2);
//     
//     DAOManager manager = mock(DAOManager.class);
//     ServiceFactory serviceFactory = mock(ServiceFactory.class);
//     when(serviceFactory.getDAOManager()).thenReturn(manager);
//     CallService service = new CallService(serviceFactory);
//     final CallSpliterator<ExpressionCallTO, Map<ExpressionCallTO, Set<ExperimentExpressionTO>>> 
//         spliterator1 = service.new CallSpliterator<>(callTOs.stream(), set, 
//             Comparator.comparing(ExpressionCallTO::getGeneId, Comparator.nullsLast(Comparator.naturalOrder())));
//     List<Map<ExpressionCallTO, Set<ExperimentExpressionTO>>> callsByGene = StreamSupport.stream(spliterator1, false)
//             .onClose(() -> spliterator1.close()).collect(Collectors.toList());
//     
//     assertEquals(2, callsByGene.size());
//     Map<ExpressionCallTO, Set<ExperimentExpressionTO>> mapGene1 = new HashMap<>();
//     mapGene1.put(new ExpressionCallTO("expId1", "geneId1", null, null, null, null, null, null, null, null, null, null, null),
//         new HashSet<>(Arrays.asList(
//             new ExperimentExpressionTO("expId1", 1, null, null, null, null),
//             new ExperimentExpressionTO("expId1", 2, null, null, null, null),
//             new ExperimentExpressionTO("expId1", 5, null, null, null, null))));
//     mapGene1.put(new ExpressionCallTO("expId2", "geneId1", null, null, null, null, null, null, null, null, null, null, null),
//         new HashSet<>(Arrays.asList(
//             new ExperimentExpressionTO("expId2", 3, null, null, null, null),
//             new ExperimentExpressionTO("expId2", 6, null, null, null, null))));
//     assertEquals(mapGene1, callsByGene.get(0));
//
//     Map<ExpressionCallTO, Set<ExperimentExpressionTO>> mapGene2 = new HashMap<>();
//     mapGene2.put(new ExpressionCallTO("expId4", "geneId2", null, null, null, null, null, null, null, null, null, null, null),
//         new HashSet<>(Arrays.asList(
//             new ExperimentExpressionTO("expId4", 7, null, null, null, null))));
//     mapGene2.put(new ExpressionCallTO("expId5", "geneId2", null, null, null, null, null, null, null, null, null, null, null),
//         new HashSet<>(Arrays.asList(
//             new ExperimentExpressionTO("expId5", 4, null, null, null, null))));
//     assertEquals(mapGene2, callsByGene.get(1));
//
//     // Different comparator
//     callTOs = new ArrayList<ExpressionCallTO>();
//     callTOs.add(new ExpressionCallTO("expId1", "geneId1", "anatEntityId1", "stageId1", null, null, null, null, null, null, null, null, null));
//     callTOs.add(new ExpressionCallTO("expId2", "geneId1", "anatEntityId2", "stageId1", null, null, null, null, null, null, null, null, null));
//     callTOs.add(new ExpressionCallTO("expId3", "geneId2", "anatEntityId2", "stageId1", null, null, null, null, null, null, null, null, null));
//     
//     expExprTOs1 = new ArrayList<ExperimentExpressionTO>();
//     expExprTOs1.add(new ExperimentExpressionTO("expId1", 1, null, null, null, null));
//     expExprTOs1.add(new ExperimentExpressionTO("expId2", 2, null, null, null, null));
//     expExprTOs1.add(new ExperimentExpressionTO("expId3", 3, null, null, null, null));
//     stream1 = expExprTOs1.stream();
//
//     set = new HashSet<>();
//     set.add(stream1);
//
//     final CallSpliterator<ExpressionCallTO, Map<ExpressionCallTO, Set<ExperimentExpressionTO>>> 
//         spliterator2 = service.new CallSpliterator<>(callTOs.stream(), set, 
//             Comparator.comparing(ExpressionCallTO::getAnatEntityId, Comparator.nullsLast(Comparator.naturalOrder())));
//     callsByGene = StreamSupport.stream(spliterator2, false).onClose(() -> spliterator2.close())
//             .collect(Collectors.toList());
//     
//     assertEquals(2, callsByGene.size());
//     mapGene1 = new HashMap<>();
//     mapGene1.put(new ExpressionCallTO("expId1", "geneId1", "anatEntityId1", "stageId1", null, null, null, null, null, null, null, null, null),
//         new HashSet<>(Arrays.asList(
//             new ExperimentExpressionTO("expId1", 1, null, null, null, null))));
//     assertEquals(mapGene1, callsByGene.get(0));
//
//     mapGene2 = new HashMap<>();
//     mapGene2.put(new ExpressionCallTO("expId2", "geneId1", "anatEntityId2", "stageId1", null, null, null, null, null, null, null, null, null),
//         new HashSet<>(Arrays.asList(
//             new ExperimentExpressionTO("expId2", 2, null, null, null, null))));
//     mapGene2.put(new ExpressionCallTO("expId3", "geneId2", "anatEntityId2", "stageId1", null, null, null, null, null, null, null, null, null),
//         new HashSet<>(Arrays.asList(
//             new ExperimentExpressionTO("expId3", 3, null, null, null, null))));
//     assertEquals(mapGene2, callsByGene.get(1));
//
//
//     // Bad order
//     final CallSpliterator<ExpressionCallTO, Map<ExpressionCallTO, Set<ExperimentExpressionTO>>> 
//         spliterator3 = service.new CallSpliterator<>(callTOs.stream(), set, 
//         Comparator.comparing(ExpressionCallTO::getAnatEntityId, Comparator.nullsLast(Comparator.naturalOrder())));
//     try {
//         callsByGene = StreamSupport.stream(spliterator3, false)
//                 .onClose(() -> spliterator3.close())
//                 .collect(Collectors.toList());
//         fail("Should throw an exception due to bad order of call TOs");
//     } catch (IllegalStateException e) {
//         // Test passed
//     }
// }
// 
// @Test
// public void shouldGetComparator() {
//     ServiceFactory serviceFactory = mock(ServiceFactory.class);
//     CallService service = new CallService(serviceFactory);
//
//     final CallSpliterator<ExpressionCallTO, Map<ExpressionCallTO, Set<ExperimentExpressionTO>>> 
//     spliterator1 = service.new CallSpliterator<>(Stream.empty(), new HashSet<>(), 
//     Comparator.comparing(ExpressionCallTO::getGeneId, Comparator.nullsLast(Comparator.naturalOrder())));
//     Comparator<? super Map<ExpressionCallTO, Set<ExperimentExpressionTO>>> comparator = spliterator1.getComparator();
//     
//     Map<ExpressionCallTO, Set<ExperimentExpressionTO>> mapGene1 = new HashMap<>();
//     mapGene1.put(new ExpressionCallTO("expId1", "geneId1", "ae2", null, null, null, null, null, null, null, null, null, null),
//         null);
//     mapGene1.put(new ExpressionCallTO("expId2", "geneId1", "ae2", null, null, null, null, null, null, null, null, null, null),
//         null);
//
//     Map<ExpressionCallTO, Set<ExperimentExpressionTO>> mapGene2 = new HashMap<>();
//     mapGene2.put(new ExpressionCallTO("expId4", "geneId2", "ae1", null, null, null, null, null, null, null, null, null, null),
//         null);
//     mapGene2.put(new ExpressionCallTO("expId5", "geneId2", "ae1", null, null, null, null, null, null, null, null, null, null),
//         null);
//
//     Set<ExpressionCallTO> gp2 = new HashSet<>();
//     gp2.add(new ExpressionCallTO("expId6", "geneId2", null, null, null, null, null, null, null, null, null, null, null));
//
//     assertTrue("Incorect compararison", comparator.compare(mapGene1, mapGene2) < 0);
//     assertTrue("Incorect compararison", comparator.compare(mapGene2, mapGene1) > 0);
//     assertTrue("Incorect compararison", comparator.compare(mapGene2, mapGene2) == 0);
//
//     final CallSpliterator<ExpressionCallTO, Map<ExpressionCallTO, Set<ExperimentExpressionTO>>> 
//     spliterator2 = service.new CallSpliterator<>(Stream.empty(), new HashSet<>(), 
//     Comparator.comparing(ExpressionCallTO::getAnatEntityId, Comparator.nullsLast(Comparator.naturalOrder())));
//     comparator = spliterator2.getComparator();
//     assertTrue("Incorect compararison", comparator.compare(mapGene1, mapGene2) > 0);
//     assertTrue("Incorect compararison", comparator.compare(mapGene2, mapGene1) < 0);
//     assertTrue("Incorect compararison", comparator.compare(mapGene1, mapGene1) == 0);
// }

}
