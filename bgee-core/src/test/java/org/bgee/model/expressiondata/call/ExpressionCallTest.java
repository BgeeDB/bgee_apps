package org.bgee.model.expressiondata.call;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.TestAncestor;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.expressiondata.baseelements.ExpressionLevelInfo;
import org.bgee.model.expressiondata.call.Condition;
import org.bgee.model.expressiondata.call.ConditionGraph;
import org.bgee.model.expressiondata.call.Call.ExpressionCall;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.species.Species;
import org.junit.Test;

/**
 * Unit tests for {@link ExpressionCall}.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @author  Julien Wollbrett
 * @version Bgee 15, Dec. 2021
 * @since   Bgee 13, June 2016
 */
public class ExpressionCallTest extends TestAncestor {
    private final static Logger log = LogManager.getLogger(ExpressionCallTest.class.getName());
    
    @Override
    protected Logger getLogger() {
        return log;
    }

    @Test
    public void shouldFilterAndOrderCallsByRank() {
        Species species = new Species(1);
        GeneBioType bioType = new GeneBioType("biotype");
        Condition ae1devA = new Condition(new AnatEntity("1"), null, null, null, null, species);
        Condition ae1devB = new Condition(new AnatEntity("2"), null, null, null, null, species);
        Condition ae2devA = new Condition(new AnatEntity("3"), null, null, null, null, species);
        Condition ae2devB = new Condition(new AnatEntity("4"), null, null, null, null, species);
        Gene gene = new Gene("1", species, bioType);
        ExpressionCall call1 = new ExpressionCall(gene, ae1devA, null, null, null, null, null, null,
                new ExpressionLevelInfo(new BigDecimal("2.0")));
        ExpressionCall call2 = new ExpressionCall(gene, ae1devB, null, null, null, null, null, null,
                new ExpressionLevelInfo(new BigDecimal("2.0")));
        ExpressionCall call3 = new ExpressionCall(gene, ae2devA, null, null, null, null, null, null,
                new ExpressionLevelInfo(new BigDecimal("2.0")));
        ExpressionCall call4 = new ExpressionCall(gene, ae2devB, null, null, null, null, null, null,
                new ExpressionLevelInfo(new BigDecimal("2.0")));
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
                ExpressionCall.filterAndOrderCallsByRank(calls, graph, false));
    }
    
    @Test
    public void shouldFilterAndOrderCallsByRankWhenGraphOfCond() {
        Species species = new Species(1);
        GeneBioType bioType = new GeneBioType("biotype");
        Condition ae1devA = new Condition(new AnatEntity("AE1"), new DevStage("DevA"), null, null, null, species);
        Condition ae1devB = new Condition(new AnatEntity("AE1"), new DevStage("DevB"), null, null, null, species);
        Condition ae2devA = new Condition(new AnatEntity("AE2"), new DevStage("DevA"), null, null, null, species);
        Condition ae2devB = new Condition(new AnatEntity("AE2"), new DevStage("DevB"), null, null, null, species);
        Condition ae2devD = new Condition(new AnatEntity("AE2"), new DevStage("DevD"), null, null, null, species);
        Condition ae3devA = new Condition(new AnatEntity("AE3"), new DevStage("DevA"), null, null, null, species);
        Condition ae3devC = new Condition(new AnatEntity("AE3"), new DevStage("DevC"), null, null, null, species);
        Condition ae3devD = new Condition(new AnatEntity("AE3"), new DevStage("DevD"), null, null, null, species);
        Gene gene = new Gene("1", species, bioType);
        ExpressionCall callAe1devA = new ExpressionCall(gene, ae1devA, null, null, null, null, null, null,
                new ExpressionLevelInfo(new BigDecimal("2.0")));
        ExpressionCall callAe1devB = new ExpressionCall(gene, ae1devB, null, null, null, null, null, null,
                new ExpressionLevelInfo(new BigDecimal("2.0")));
        ExpressionCall callAe2devA = new ExpressionCall(gene, ae2devA, null, null, null, null, null, null,
                new ExpressionLevelInfo(new BigDecimal("2.0")));
        ExpressionCall callAe2devB = new ExpressionCall(gene, ae2devB, null, null, null, null, null, null,
                new ExpressionLevelInfo(new BigDecimal("2.0")));
        ExpressionCall callAe2devD = new ExpressionCall(gene, ae2devD, null, null, null, null, null, null,
                new ExpressionLevelInfo(new BigDecimal("2.0")));
        ExpressionCall callAe3devA = new ExpressionCall(gene, ae3devA, null, null, null, null, null, null,
                new ExpressionLevelInfo(new BigDecimal("2.0")));
        ExpressionCall callAe3devC = new ExpressionCall(gene, ae3devC, null, null, null, null, null, null,
                new ExpressionLevelInfo(new BigDecimal("2.0")));
        ExpressionCall callAe3devD = new ExpressionCall(gene, ae3devD, null, null, null, null, null, null,
                new ExpressionLevelInfo(new BigDecimal("2.0")));
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
                ExpressionCall.filterAndOrderCallsByRank(calls, graph, false));

    }
    
    /**
     * Test {@link #ExpressionCall#generateMeanRankScoreClustering(Collection, ClusteringMethod, double)}.
     */
    @Test
    public void shouldGenerateMeanRankScoreClustering() {
        
        Species spe1 = new Species(1);
        Gene g1 = new Gene("gene1", spe1, new GeneBioType("b"));
        AnatEntity anat1 = new AnatEntity("anat1");
        AnatEntity anat2 = new AnatEntity("anat2");
        AnatEntity anat3 = new AnatEntity("anat3");
        DevStage stage1 = new DevStage("stage1");
        DevStage stage2 = new DevStage("stage2");
        DevStage stage3 = new DevStage("stage3");
        Condition cond1 = new Condition(anat1, stage1, null, null, null, spe1);
        Condition cond2 = new Condition(anat1, stage2, null, null, null, spe1);
        Condition cond3 = new Condition(anat1, stage3, null, null, null, spe1);
        Condition cond4 = new Condition(anat2, stage1, null, null, null, spe1);
        Condition cond5 = new Condition(anat2, stage2, null, null, null, spe1);
        Condition cond6 = new Condition(anat2, stage3, null, null, null, spe1);
        Condition cond7 = new Condition(anat3, stage1, null, null, null, spe1);
        Condition cond8 = new Condition(anat3, stage2, null, null, null, spe1);


        //we don't bother to retrieve exact score thresholds etc, we just create calls 
        //very obvious to cluster by Canderra distance
        ExpressionCall c1 = new ExpressionCall(g1, cond1, null, null, null, null, null, null, new ExpressionLevelInfo(new BigDecimal("1.25")));
        ExpressionCall c2 = new ExpressionCall(g1, cond2, null, null, null, null, null, null, new ExpressionLevelInfo(new BigDecimal("1.27")));
        ExpressionCall c3 = new ExpressionCall(g1, cond3, null, null, null, null, null, null, new ExpressionLevelInfo(new BigDecimal("10000")));
        ExpressionCall c4 = new ExpressionCall(g1, cond4, null, null, null, null, null, null, new ExpressionLevelInfo(new BigDecimal("20000")));
        ExpressionCall c5 = new ExpressionCall(g1, cond5, null, null, null, null, null, null, new ExpressionLevelInfo(new BigDecimal("40000")));
        ExpressionCall c6 = new ExpressionCall(g1, cond6, null, null, null, null, null, null, new ExpressionLevelInfo(new BigDecimal("40010")));
        ExpressionCall c7 = new ExpressionCall(g1, cond7, null, null, null, null, null, null, new ExpressionLevelInfo(new BigDecimal("70000")));
        ExpressionCall c8 = new ExpressionCall(g1, cond8, null, null, null, null, null, null, new ExpressionLevelInfo(new BigDecimal("70010")));
        //we'd like to incorrectly order the calls, but there is a method signature accepting a List...
        Set<ExpressionCall> toCluster = new HashSet<>(Arrays.asList(c5, c1, c7, c3, c6, c8, c4, c2));
        Map<ExpressionCall, Integer> expectedClusters = new HashMap<>();
        expectedClusters.put(c1, 0);
        expectedClusters.put(c2, 0);
        expectedClusters.put(c3, 1);
        expectedClusters.put(c4, 2);
        expectedClusters.put(c5, 3);
        expectedClusters.put(c6, 3);
        expectedClusters.put(c7, 4);
        expectedClusters.put(c8, 4);
        
        for (ExpressionCall.ClusteringMethod method: ExpressionCall.ClusteringMethod.values()) {
            log.debug("Testing clustering method: " + method.name());

            assertEquals("Incorrect clustering of expression scores for method: " + method.name(), 
                    expectedClusters, 
                    ExpressionCall.generateMeanRankScoreClustering(toCluster, method, 
                            (method.isDistanceMeasureAboveOne()? 1.5: 0.1)));
        }
    }
    
    @Test
    public void testBgeeRankDistance() {
        ExpressionCall.BgeeRankDistance measure = new ExpressionCall.BgeeRankDistance();
        
        double score1 = 12;
        double score2 = 33;
        double expected = Math.pow(score2, 1.03)/score1;
        assertEquals("Incorrect BgeeRankDistance computed", expected, 
                measure.compute(new double[]{score1}, new double[]{score2}), 0.0000001);
        
    }
    
    /**
     * Test for {@link ExpressionCall#identifyRedundantCalls(Collection, ConditionGraph)}.
     */
    @Test
    public void shouldIdentifyRedundantCalls() {
      //These calls and conditions allow a regression test for management of equal ranks
        //cond2 and cond3 will be considered more precise than cond1, and unrelated to each other
        Species spe1 = new Species(1);
        Condition cond1 = new Condition(new AnatEntity("Anat1"), new DevStage("stage1"), null, null, null, spe1);
        Condition cond2 = new Condition(new AnatEntity("Anat2"), new DevStage("stage1"), null, null, null, spe1);
        Condition cond3 = new Condition(new AnatEntity("Anat3"), new DevStage("stage1"), null, null, null, spe1);
        //we mock the ConditionGraph used to compare Conditions
        ConditionGraph condGraph = mock(ConditionGraph.class);
        Set<Condition> allConds = new HashSet<>(Arrays.asList(cond1, cond2, cond3));
        when(condGraph.getConditions()).thenReturn(allConds);
        when(condGraph.isConditionMorePrecise(cond1, cond1)).thenReturn(false);
        when(condGraph.isConditionMorePrecise(cond2, cond2)).thenReturn(false);
        when(condGraph.isConditionMorePrecise(cond3, cond3)).thenReturn(false);
        when(condGraph.isConditionMorePrecise(cond1, cond2)).thenReturn(true);//1
        when(condGraph.isConditionMorePrecise(cond2, cond1)).thenReturn(false);//-1
        when(condGraph.isConditionMorePrecise(cond1, cond3)).thenReturn(true);//1
        when(condGraph.isConditionMorePrecise(cond3, cond1)).thenReturn(false);//-1
        when(condGraph.isConditionMorePrecise(cond2, cond3)).thenReturn(false);
        when(condGraph.isConditionMorePrecise(cond3, cond2)).thenReturn(false);
        when(condGraph.getDescendantConditions(cond1)).thenReturn(new HashSet<>(Arrays.asList(cond2, cond3)));
        when(condGraph.getDescendantConditions(cond2)).thenReturn(new HashSet<>());
        when(condGraph.getDescendantConditions(cond3)).thenReturn(new HashSet<>());
        
        
        //Nothing too complicated with gene ID1, c3 is redundant
        GeneBioType biotype = new GeneBioType("b");
        ExpressionCall c1 = new ExpressionCall(new Gene("ID1", new Species(1), biotype), cond3, null, null,
                null, null, null, null, new ExpressionLevelInfo(new BigDecimal("1.25000")));
        ExpressionCall c2 = new ExpressionCall(new Gene("ID1", new Species(1), biotype), cond2, null, null,
                null, null, null, null, new ExpressionLevelInfo(new BigDecimal("2.0")));
        ExpressionCall c3 = new ExpressionCall(new Gene("ID1", new Species(1), biotype), cond1, null, null,
                null, null, null, null, new ExpressionLevelInfo(new BigDecimal("3.00")));
        //for gene ID2 we test identification with equal ranks and relations between conditions. 
        //c4 is redundant because less precise condition
        //Note: ranks with different scales are not considered equals
        ExpressionCall c4 = new ExpressionCall(new Gene("ID2", new Species(1), biotype), cond1, null, null,
                null, null, null, null, new ExpressionLevelInfo(new BigDecimal("1.25")));
        ExpressionCall c5 = new ExpressionCall(new Gene("ID2", new Species(1), biotype), cond3, null, null,
                null, null, null, null, new ExpressionLevelInfo(new BigDecimal("1.25")));
        ExpressionCall c6 = new ExpressionCall(new Gene("ID2", new Species(1), biotype), cond2, null, null,
                null, null, null, null, new ExpressionLevelInfo(new BigDecimal("1.25")));
        //for gene ID3 we test identification with equal ranks and relations between conditions. 
        //nothing redundant
        //Note: ranks with different scales are not considered equals
        ExpressionCall c7 = new ExpressionCall(new Gene("ID3", new Species(1), biotype), cond1, null, null,
                null, null, null, null, new ExpressionLevelInfo(new BigDecimal("1")));
        ExpressionCall c8 = new ExpressionCall(new Gene("ID3", new Species(1), biotype), cond3, null, null,
                null, null, null, null, new ExpressionLevelInfo(new BigDecimal("1.25")));
        ExpressionCall c9 = new ExpressionCall(new Gene("ID3", new Species(1), biotype), cond2, null, null,
                null, null, null, null, new ExpressionLevelInfo(new BigDecimal("1.25")));
        
        Set<ExpressionCall> withRedundancy = new HashSet<>(Arrays.asList(c1, c2, c3, c4, c5, c6, c7, c8, c9));
        Set<ExpressionCall> expectedRedundants = new HashSet<>(Arrays.asList(c3, c4));
        assertEquals("Incorrect redundant calls identified", 
                expectedRedundants, ExpressionCall.identifyRedundantCalls(withRedundancy, condGraph));
    }
    
    /**
     * Test {@link ExpressionCall#getFormattedMeanRank()}
     */
    @Test
    public void shouldFormatRankScore() {
        ExpressionCall c = new ExpressionCall(null, null, null,null, null,  null, null, null,
                new ExpressionLevelInfo(new BigDecimal("2")));
        assertEquals("Incorrect score formatting", "2.00", c.getFormattedMeanRank());
        c = new ExpressionCall(null, null, null, null, null, null, null, null,
                new ExpressionLevelInfo(new BigDecimal("2.23")));
        assertEquals("Incorrect score formatting", "2.23", c.getFormattedMeanRank());
        c = new ExpressionCall(null, null, null, null, null, null, null, null,
                new ExpressionLevelInfo(new BigDecimal("20")));
        assertEquals("Incorrect score formatting", "20.0", c.getFormattedMeanRank());
        c = new ExpressionCall(null, null, null, null, null, null, null, null,
                new ExpressionLevelInfo(new BigDecimal("20.23")));
        assertEquals("Incorrect score formatting", "20.2", c.getFormattedMeanRank());
        c = new ExpressionCall(null, null, null, null, null, null, null, null, 
                new ExpressionLevelInfo(new BigDecimal("200")));
        assertEquals("Incorrect score formatting", "200", c.getFormattedMeanRank());
        c = new ExpressionCall(null, null, null, null, null, null, null, null, 
                new ExpressionLevelInfo(new BigDecimal("200.23")));
        assertEquals("Incorrect score formatting", "200", c.getFormattedMeanRank());
        c = new ExpressionCall(null, null, null, null, null, null, null, null, 
                new ExpressionLevelInfo(new BigDecimal("1000")));
        assertEquals("Incorrect score formatting", "1.00e3", c.getFormattedMeanRank());
        c = new ExpressionCall(null, null, null, null, null, null, null, null, 
                new ExpressionLevelInfo(new BigDecimal("2000")));
        assertEquals("Incorrect score formatting", "2.00e3", c.getFormattedMeanRank());
        c = new ExpressionCall(null, null, null, null, null, null, null, null, 
                new ExpressionLevelInfo(new BigDecimal("2000.23")));
        assertEquals("Incorrect score formatting", "2.00e3", c.getFormattedMeanRank());
        c = new ExpressionCall(null, null, null, null, null, null, null, null, 
                new ExpressionLevelInfo(new BigDecimal("20000")));
        assertEquals("Incorrect score formatting", "2.00e4", c.getFormattedMeanRank());
        c = new ExpressionCall(null, null, null, null, null, null, null, null, 
                new ExpressionLevelInfo(new BigDecimal("20000.23")));
        assertEquals("Incorrect score formatting", "2.00e4", c.getFormattedMeanRank());
        c = new ExpressionCall(null, null, null, null, null, null, null, null, 
                new ExpressionLevelInfo(new BigDecimal("20100.23")));
        assertEquals("Incorrect score formatting", "2.01e4", c.getFormattedMeanRank());
    }
}

