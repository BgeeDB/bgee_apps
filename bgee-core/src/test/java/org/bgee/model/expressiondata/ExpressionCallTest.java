package org.bgee.model.expressiondata;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.TestAncestor;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.gene.Gene;
import org.bgee.model.species.Species;
import org.junit.Test;

/**
 * Unit tests for {@link ExpressionCall}.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 13, July 2016
 * @since   Bgee 13, June 2016
 */
//FIXME: to reactivate
public class ExpressionCallTest extends TestAncestor {
    private final static Logger log = LogManager.getLogger(ExpressionCallTest.class.getName());
    
    @Override
    protected Logger getLogger() {
        return log;
    }

    @Test
    public void shouldFilterAndOrderCallsByRank() {
        Species species = new Species(1);
        Condition ae1devA = new Condition(new AnatEntity("1"), null, species);
        Condition ae1devB = new Condition(new AnatEntity("2"), null, species);
        Condition ae2devA = new Condition(new AnatEntity("3"), null, species);
        Condition ae2devB = new Condition(new AnatEntity("4"), null, species);
        Gene gene = new Gene("1", species);
        ExpressionCall call1 = new ExpressionCall(gene, ae1devA, null, null, null, null,
                new BigDecimal("2.0"), new BigDecimal("100"));
        ExpressionCall call2 = new ExpressionCall(gene, ae1devB, null, null, null, null,
                new BigDecimal("2.0"), new BigDecimal("100"));
        ExpressionCall call3 = new ExpressionCall(gene, ae2devA, null, null, null, null,
                new BigDecimal("2.0"), new BigDecimal("100"));
        ExpressionCall call4 = new ExpressionCall(gene, ae2devB, null, null, null, null,
                new BigDecimal("2.0"), new BigDecimal("100"));
        ConditionGraph graph = mock(ConditionGraph.class);
        //           ae1devA
        //          /   \
        //     ae1devB   ae2devA
        //              |
        //             ae2devB
        when(graph.isConditionMorePrecise(ae1devA, ae1devA)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae1devA, ae1devB)).thenReturn(true);
        when(graph.isConditionMorePrecise(ae1devA, ae2devA)).thenReturn(true);
        when(graph.isConditionMorePrecise(ae1devA, ae2devB)).thenReturn(true);
        when(graph.isConditionMorePrecise(ae1devB, ae1devA)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae1devB, ae1devB)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae1devB, ae2devA)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae1devB, ae2devB)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae2devA, ae1devA)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae2devA, ae1devB)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae2devA, ae2devA)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae2devA, ae2devB)).thenReturn(true);
        when(graph.isConditionMorePrecise(ae2devB, ae1devA)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae2devB, ae1devB)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae2devB, ae2devA)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae2devB, ae2devB)).thenReturn(false);

        List<ExpressionCall> calls = Arrays.asList(call1, call2, call3, call4);
        List<ExpressionCall> expectedOrder = Arrays.asList(call2, call4, call3, call1);
        assertEquals("Incorrect ordering of calls", expectedOrder,
                ExpressionCall.filterAndOrderCallsByRank(calls, graph));
    }
    
    @Test
    public void shouldFilterAndOrderCallsByRankWhenGraphOfCond() {
        Species species = new Species(1);
        Condition ae1devA = new Condition(new AnatEntity("AE1"), new DevStage("DevA"), species);
        Condition ae1devB = new Condition(new AnatEntity("AE1"), new DevStage("DevB"), species);
        Condition ae2devA = new Condition(new AnatEntity("AE2"), new DevStage("DevA"), species);
        Condition ae2devB = new Condition(new AnatEntity("AE2"), new DevStage("DevB"), species);
        Condition ae2devD = new Condition(new AnatEntity("AE2"), new DevStage("DevD"), species);
        Condition ae3devA = new Condition(new AnatEntity("AE3"), new DevStage("DevA"), species);
        Condition ae3devC = new Condition(new AnatEntity("AE3"), new DevStage("DevC"), species);
        Condition ae3devD = new Condition(new AnatEntity("AE3"), new DevStage("DevD"), species);
        Gene gene = new Gene("1", species);
        ExpressionCall callAe1devA = new ExpressionCall(gene, ae1devA, null, null, null, null,
                new BigDecimal("2.0"), new BigDecimal("100"));
        ExpressionCall callAe1devB = new ExpressionCall(gene, ae1devB, null, null, null, null,
                new BigDecimal("2.0"), new BigDecimal("100"));
        ExpressionCall callAe2devA = new ExpressionCall(gene, ae2devA, null, null, null, null,
                new BigDecimal("2.0"), new BigDecimal("100"));
        ExpressionCall callAe2devB = new ExpressionCall(gene, ae2devB, null, null, null, null,
                new BigDecimal("2.0"), new BigDecimal("100"));
        ExpressionCall callAe2devD = new ExpressionCall(gene, ae2devD, null, null, null, null,
                new BigDecimal("2.0"), new BigDecimal("100"));
        ExpressionCall callAe3devA = new ExpressionCall(gene, ae3devA, null, null, null, null,
                new BigDecimal("2.0"), new BigDecimal("100"));
        ExpressionCall callAe3devC = new ExpressionCall(gene, ae3devC, null, null, null, null,
                new BigDecimal("2.0"), new BigDecimal("100"));
        ExpressionCall callAe3devD = new ExpressionCall(gene, ae3devD, null, null, null, null,
                new BigDecimal("2.0"), new BigDecimal("100"));
        ConditionGraph graph = mock(ConditionGraph.class);
        when(graph.isConditionMorePrecise(ae1devA, ae1devA)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae1devA, ae1devB)).thenReturn(true);
        when(graph.isConditionMorePrecise(ae1devA, ae2devA)).thenReturn(true);
        when(graph.isConditionMorePrecise(ae1devA, ae2devB)).thenReturn(true);
        when(graph.isConditionMorePrecise(ae1devA, ae2devD)).thenReturn(true);
        when(graph.isConditionMorePrecise(ae1devA, ae3devA)).thenReturn(true);
        when(graph.isConditionMorePrecise(ae1devA, ae3devC)).thenReturn(true);
        when(graph.isConditionMorePrecise(ae1devA, ae3devD)).thenReturn(true);
        //ae1devB
        when(graph.isConditionMorePrecise(ae1devB, ae1devA)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae1devB, ae1devB)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae1devB, ae2devA)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae1devB, ae2devB)).thenReturn(true);
        when(graph.isConditionMorePrecise(ae1devB, ae2devD)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae1devB, ae3devA)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae1devB, ae3devC)).thenReturn(true);
        when(graph.isConditionMorePrecise(ae1devB, ae3devD)).thenReturn(false);
        //ae2devA
        when(graph.isConditionMorePrecise(ae2devA, ae1devA)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae2devA, ae1devB)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae2devA, ae2devA)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae2devA, ae2devB)).thenReturn(true);
        when(graph.isConditionMorePrecise(ae2devA, ae2devD)).thenReturn(true);
        when(graph.isConditionMorePrecise(ae2devA, ae3devA)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae2devA, ae3devC)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae2devA, ae3devD)).thenReturn(false);
        //ae2devB
        when(graph.isConditionMorePrecise(ae2devB, ae1devA)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae2devB, ae1devB)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae2devB, ae2devA)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae2devB, ae2devB)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae2devB, ae2devD)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae2devB, ae3devA)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae2devB, ae3devC)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae2devB, ae3devD)).thenReturn(false);
        //ae2devD
        when(graph.isConditionMorePrecise(ae2devD, ae1devA)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae2devD, ae1devB)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae2devD, ae2devA)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae2devD, ae2devB)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae2devD, ae2devD)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae2devD, ae3devA)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae2devD, ae3devC)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae2devD, ae3devD)).thenReturn(false);
        //ae3devA
        when(graph.isConditionMorePrecise(ae3devA, ae1devA)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae3devA, ae1devB)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae3devA, ae2devA)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae3devA, ae2devB)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae3devA, ae2devD)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae3devA, ae3devA)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae3devA, ae3devC)).thenReturn(true);
        when(graph.isConditionMorePrecise(ae3devA, ae3devD)).thenReturn(true);
        //ae3devC
        when(graph.isConditionMorePrecise(ae3devC, ae1devA)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae3devC, ae1devB)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae3devC, ae2devA)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae3devC, ae2devB)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae3devC, ae2devD)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae3devC, ae3devA)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae3devC, ae3devC)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae3devC, ae3devD)).thenReturn(false);
        //ae3devD
        when(graph.isConditionMorePrecise(ae3devD, ae1devA)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae3devD, ae1devB)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae3devD, ae2devA)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae3devD, ae2devB)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae3devD, ae2devD)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae3devD, ae3devA)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae3devD, ae3devC)).thenReturn(false);
        when(graph.isConditionMorePrecise(ae3devD, ae3devD)).thenReturn(false);
       
        
        List<ExpressionCall> calls = Arrays.asList(callAe1devA, callAe1devB, callAe2devA, 
                callAe2devB, callAe2devD, callAe3devA, callAe3devC, callAe3devD);
        //In this example some condition (ae2devB and ae3devC) have two direct parent conditions.
        //
        //Maybe the algorithm should prioritize parents having same Anat Entity (like in the web page)???
        //Then, the correct order could be callAe1devB, callAe2devD, callAe2devB, callAe2devA, callAe3devD, callAe3devC, 
        //callAe3devA, callAe1devA
        //rather than callAe2devB, callAe3devC, callAe1devB, callAe2devD, callAe2devA, callAe3devD, callAe3devA, callAe1devA
        //However, the algorithm works as expected (no error, bug or non-reproducible bug). Even if the the conditions 
        //are not filters by anat entity, the first appearance of each Anat Entity is in the good order (AE2, AE3 and then AE1)
        
        //                 Conditions :
        //
        //           AE1              DevA                                        ae1devA
        //          /   \            /    \                 =>            /          |          \
        //        AE2   AE3        DevB  DevD                         ae2devA     ae1devB     ae3devA
        //                          |                                 /     \     /     \     /     \
        //                         DevC                          ae2devD    ae2devB     ae3devC   ae3devD

        //order that should be computed by current version of the algorithm
        List<ExpressionCall> expectedOrder = Arrays.asList(callAe2devB, callAe3devC, callAe1devB, callAe2devD, callAe2devA, 
                callAe3devD, callAe3devA, callAe1devA);
        assertEquals("Incorrect ordering of calls", expectedOrder,
                ExpressionCall.filterAndOrderCallsByRank(calls, graph));

    }

//    
//    /**
//     * Test {@link #ExpressionCall#generateMeanRankScoreClustering(Collection, ClusteringMethod, double)}.
//     */
//    @Test
//    public void shouldGenerateMeanRankScoreClustering() {
//        //we don't bother to retrieve exact score thresholds etc, we just create calls 
//        //very obvious to cluster by Canderra distance
//        ExpressionCall c1 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("1.25"));
//        ExpressionCall c2 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("1.27"));
//        ExpressionCall c3 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("10000"));
//        ExpressionCall c4 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("20000"));
//        ExpressionCall c5 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("40000"));
//        ExpressionCall c6 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("40010"));
//        ExpressionCall c7 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("70000"));
//        ExpressionCall c8 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("70010"));
//        //we'd like to incorrectly order the calls, but there is a method signature accepting a List...
//        Set<ExpressionCall> toCluster = new HashSet<>(Arrays.asList(c5, c1, c7, c3, c6, c8, c4, c2));
//        Map<ExpressionCall, Integer> expectedClusters = new HashMap<>();
//        expectedClusters.put(c1, 0);
//        expectedClusters.put(c2, 0);
//        expectedClusters.put(c3, 1);
//        expectedClusters.put(c4, 2);
//        expectedClusters.put(c5, 3);
//        expectedClusters.put(c6, 3);
//        expectedClusters.put(c7, 4);
//        expectedClusters.put(c8, 4);
//        
//        for (ExpressionCall.ClusteringMethod method: ExpressionCall.ClusteringMethod.values()) {
//            log.debug("Testing clustering method: " + method.name());
//            assertEquals("Incorrect clustering of expression scores for method: " + method.name(), 
//                    expectedClusters, 
//                    ExpressionCall.generateMeanRankScoreClustering(toCluster, method, 
//                            (method.isDistanceMeasureAboveOne()? 1.5: 0.1)));
//        }
//    }
//    
//    /**
//     * Test {@link ExpressionCall.RankComparator}.
//     */
//    @Test
//    public void testRankComparator() {
//        
//        //These calls and conditions allow a regression test for management of equal ranks
//        //ae1devB and ae2devA will be considered more precise than ae1devA, and unrelated to each other. 
//        //It is important for the test that ae1devA would be sorted first alphabetically. 
//        Condition ae1devA = new Condition(new AnatEntity("Anat1"), new DevStage("stage1"), 1);
//        Condition ae1devB = new Condition(new AnatEntity("Anat2"), new DevStage("stage1"), 1);
//        Condition ae2devA = new Condition(new AnatEntity("Anat3"), new DevStage("stage1"), 1);
//        //we mock the ConditionGraph used to compare Conditions
//        ConditionGraph condGraph = mock(ConditionGraph.class);
//        Set<Condition> allConds = new HashSet<>(Arrays.asList(ae1devA, ae1devB, ae2devA));
//        when(condGraph.getConditions()).thenReturn(allConds);
//        when(condGraph.compare(ae1devA, ae1devA)).thenReturn(0);
//        when(condGraph.compare(ae1devB, ae1devB)).thenReturn(0);
//        when(condGraph.compare(ae2devA, ae2devA)).thenReturn(0);
//        when(condGraph.compare(ae1devA, ae1devB)).thenReturn(1);
//        when(condGraph.compare(ae1devB, ae1devA)).thenReturn(-1);
//        when(condGraph.compare(ae1devA, ae2devA)).thenReturn(1);
//        when(condGraph.compare(ae2devA, ae1devA)).thenReturn(-1);
//        when(condGraph.compare(ae1devB, ae2devA)).thenReturn(0);
//        when(condGraph.compare(ae2devA, ae1devB)).thenReturn(0);
//
//        ExpressionCall c1 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("1.25"));
//
//        //test ordering by geneId
//        ExpressionCall c2 = new ExpressionCall(new Gene("ID0", new Species(1)), null, null, null,
//            null, null, new BigDecimal("1.27"));
//        //Test ordering based on Conditions
//        //anat2 will be considered more precise than anat1, and as precise as anat3, 
//        //the order between anat2 and anat3 should be based on their attributes
//        ExpressionCall c3 = new ExpressionCall(new Gene("ID1", new Species(1)), ae1devB, null, null, 
//                null, null, new BigDecimal("1.27"));
//        //anat3 will be considered more precise than anat1, so we will have different ordering 
//        //whether we consider the relations between Conditions, or only attributes of Conditions
//        ExpressionCall c4 = new ExpressionCall(new Gene("ID1", new Species(1)), ae2devA, null, null, 
//                null, null, new BigDecimal("1.27"));
//        ExpressionCall c5 = new ExpressionCall(new Gene("ID1", new Species(1)), ae1devA, null, null, 
//                null, null, new BigDecimal("1.27"));
//        //test null Conditions last
//        ExpressionCall c6 = new ExpressionCall(new Gene("ID1", new Species(1)), null, null, null,
//            null, null, new BigDecimal("1.27"));
//        //test null geneID last
//        ExpressionCall c7 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("1.27"));
//        //test equal ExpressionCalls
//        ExpressionCall c8 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("1.27"));
//        
//        ExpressionCall c9 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("10000"));
//        ExpressionCall c10 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("40000"));
//        
//        List<ExpressionCall> toSort = Arrays.asList(c10, c2, c1, c6, c8, c4, c9, c7, c3, c5);
//        List<ExpressionCall> expectedResult = Arrays.asList(c1, c2, c3, c4, c5, c6, c7, c7, c9, c10);
//        Collections.sort(toSort, new ExpressionCall.RankComparator(condGraph));
//        
//        assertEquals("Incorrect sorting of ExpressionCalls based on their rank and relations", 
//                expectedResult, toSort);
//        
//        //ordering without taking the relations between Conditions into account
//        expectedResult = Arrays.asList(c1, c2, c5, c3, c4, c6, c7, c7, c9, c10);
//        Collections.sort(toSort, new ExpressionCall.RankComparator());
//        assertEquals("Incorrect sorting of ExpressionCalls based on their rank without relations", 
//                expectedResult, toSort);
//    }
//    
//    @Test
//    public void testBgeeRankDistance() {
//        ExpressionCall.BgeeRankDistance measure = new ExpressionCall.BgeeRankDistance();
//        
//        double score1 = 12;
//        double score2 = 33;
//        double expected = Math.pow(score2, 1.03)/score1;
//        assertEquals("Incorrect BgeeRankDistance computed", expected, 
//                measure.compute(new double[]{score1}, new double[]{score2}), 0.0000001);
//        
//    }
//    
//    /**
//     * Test for {@link ExpressionCall#identifyRedundantCalls(Collection, ConditionGraph)}.
//     */
//    @Test
//    public void shouldIdentifyRedundantCalls() {
//      //These calls and conditions allow a regression test for management of equal ranks
//        //ae1devB and ae2devA will be considered more precise than ae1devA, and unrelated to each other
//        Condition ae1devA = new Condition(new AnatEntity("Anat1"), new DevStage("stage1"), 1);
//        Condition ae1devB = new Condition(new AnatEntity("Anat2"), new DevStage("stage1"), 1);
//        Condition ae2devA = new Condition(new AnatEntity("Anat3"), new DevStage("stage1"), 1);
//        //we mock the ConditionGraph used to compare Conditions
//        ConditionGraph condGraph = mock(ConditionGraph.class);
//        Set<Condition> allConds = new HashSet<>(Arrays.asList(ae1devA, ae1devB, ae2devA));
//        when(condGraph.getConditions()).thenReturn(allConds);
//        when(condGraph.compare(ae1devA, ae1devA)).thenReturn(0);
//        when(condGraph.compare(ae1devB, ae1devB)).thenReturn(0);
//        when(condGraph.compare(ae2devA, ae2devA)).thenReturn(0);
//        when(condGraph.compare(ae1devA, ae1devB)).thenReturn(1);
//        when(condGraph.compare(ae1devB, ae1devA)).thenReturn(-1);
//        when(condGraph.compare(ae1devA, ae2devA)).thenReturn(1);
//        when(condGraph.compare(ae2devA, ae1devA)).thenReturn(-1);
//        when(condGraph.compare(ae1devB, ae2devA)).thenReturn(0);
//        when(condGraph.compare(ae2devA, ae1devB)).thenReturn(0);
//        when(condGraph.getDescendantConditions(ae1devA)).thenReturn(new HashSet<>(Arrays.asList(ae1devB, ae2devA)));
//        when(condGraph.getDescendantConditions(ae1devB)).thenReturn(new HashSet<>());
//        when(condGraph.getDescendantConditions(ae2devA)).thenReturn(new HashSet<>());
//        
//        
//        //Nothing too complicated with gene ID1, c3 is redundant
//        ExpressionCall c1 = new ExpressionCall(new Gene("ID1", new Species(1)), ae2devA, null, null,
//            null, null, new BigDecimal("1.25000"));
//        ExpressionCall c2 = new ExpressionCall(new Gene("ID1", new Species(1)), ae1devB, null, null,
//            null, null, new BigDecimal("2.0"));
//        ExpressionCall c3 = new ExpressionCall(new Gene("ID1", new Species(1)), ae1devA, null, null,
//            null, null, new BigDecimal("3.00"));
//        //for gene ID2 we test identification with equal ranks and relations between conditions. 
//        //c4 is redundant because less precise condition
//        ExpressionCall c4 = new ExpressionCall(new Gene("ID2", new Species(1)), ae1devA, null, null,
//            null, null, new BigDecimal("1.250"));
//        ExpressionCall c5 = new ExpressionCall(new Gene("ID2", new Species(1)), ae2devA, null, null,
//            null, null, new BigDecimal("1.25000"));
//        ExpressionCall c6 = new ExpressionCall(new Gene("ID2", new Species(1)), ae1devB, null, null,
//            null, null, new BigDecimal("1.25"));
//        //for gene ID3 we test identification with equal ranks and relations between conditions. 
//        //nothing redundant
//        ExpressionCall c7 = new ExpressionCall(new Gene("ID3", new Species(1)), ae1devA, null, null,
//            null, null, new BigDecimal("1"));
//        ExpressionCall c8 = new ExpressionCall(new Gene("ID3", new Species(1)), ae2devA, null, null,
//            null, null, new BigDecimal("1.25000"));
//        ExpressionCall c9 = new ExpressionCall(new Gene("ID3", new Species(1)), ae1devB, null, null,
//            null, null, new BigDecimal("1.25"));
//        
//        Set<ExpressionCall> withRedundancy = new HashSet<>(Arrays.asList(c1, c2, c3, c4, c5, c6, c7, c8, c9));
//        Set<ExpressionCall> expectedRedundants = new HashSet<>(Arrays.asList(c3, c4));
//        assertEquals("Incorrect redundant calls identified", 
//                expectedRedundants, ExpressionCall.identifyRedundantCalls(withRedundancy, condGraph));
//    }
//    
//    /**
//     * Test {@link ExpressionCall#getFormattedGlobalMeanRank()}
//     */
//    @Test
//    public void shouldFormatRankScore() {
//        ExpressionCall c = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("2"));
//        assertEquals("Incorrect score formatting", "2.00", c.getFormattedGlobalMeanRank());
//        c = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("2.23"));
//        assertEquals("Incorrect score formatting", "2.23", c.getFormattedGlobalMeanRank());
//        c = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("20"));
//        assertEquals("Incorrect score formatting", "20.0", c.getFormattedGlobalMeanRank());
//        c = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("20.23"));
//        assertEquals("Incorrect score formatting", "20.2", c.getFormattedGlobalMeanRank());
//        c = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("200"));
//        assertEquals("Incorrect score formatting", "200", c.getFormattedGlobalMeanRank());
//        c = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("200.23"));
//        assertEquals("Incorrect score formatting", "200", c.getFormattedGlobalMeanRank());
//        c = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("2000"));
//        assertEquals("Incorrect score formatting", "2.00e3", c.getFormattedGlobalMeanRank());
//        c = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("2000.23"));
//        assertEquals("Incorrect score formatting", "2.00e3", c.getFormattedGlobalMeanRank());
//        c = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("20000"));
//        assertEquals("Incorrect score formatting", "2.00e4", c.getFormattedGlobalMeanRank());
//        c = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("20000.23"));
//        assertEquals("Incorrect score formatting", "2.00e4", c.getFormattedGlobalMeanRank());
//        c = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("20100.23"));
//        assertEquals("Incorrect score formatting", "2.01e4", c.getFormattedGlobalMeanRank());
//    }
}
