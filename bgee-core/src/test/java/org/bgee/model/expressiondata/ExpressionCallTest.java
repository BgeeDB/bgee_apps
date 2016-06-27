package org.bgee.model.expressiondata;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.TestAncestor;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.junit.Test;

/**
 * Unit tests for {@link ExpressionCall}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 June 2016
 * @since Bgee 13 June 2016
 */
public class ExpressionCallTest extends TestAncestor {
    private final static Logger log = LogManager.getLogger(ExpressionCallTest.class.getName());
    
    @Override
    protected Logger getLogger() {
        return log;
    } 
    
    /**
     * Test {@link #ExpressionCall#generateMeanRankScoreClustering(Collection, ClusteringMethod, double)}.
     */
    @Test
    public void shouldGenerateMeanRankScoreClustering() {
        //we don't bother to retrieve exact score thresholds etc, we just create calls 
        //very obvious to cluster by Canderra distance
        ExpressionCall c1 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("1.25"));
        ExpressionCall c2 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("1.27"));
        ExpressionCall c3 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("10000"));
        ExpressionCall c4 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("20000"));
        ExpressionCall c5 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("40000"));
        ExpressionCall c6 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("40010"));
        ExpressionCall c7 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("70000"));
        ExpressionCall c8 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("70010"));
        //on purpose we incorrectly order the calls (regression test)
        List<ExpressionCall> toCluster = Arrays.asList(c5, c1, c7, c3, c6, c8, c4, c2);
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
                    ExpressionCall.generateMeanRankScoreClustering(toCluster, method, 0.1));
        }
    }
    
    /**
     * Test {@link ExpressionCall#RANK_COMPARATOR}.
     */
    @Test
    public void testRankComparator() {
        ExpressionCall c1 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("1.25"));
        
        //These calls allow a regression test for management of equal ranks
        ExpressionCall c2 = new ExpressionCall("ID1", new Condition("Anat1", "stage1"), null, null, 
                null, null, new BigDecimal("1.27"));
        ExpressionCall c3 = new ExpressionCall("ID1", new Condition("Anat1", null), null, null, 
                null, null, new BigDecimal("1.27"));
        ExpressionCall c4 = new ExpressionCall("ID1", null, null, null, null, null, new BigDecimal("1.27"));
        ExpressionCall c5 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("1.27"));
        ExpressionCall c6 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("1.27"));
        
        ExpressionCall c7 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("10000"));
        ExpressionCall c8 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("40000"));
        ExpressionCall c9 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("40010"));
        
        List<ExpressionCall> toSort = Arrays.asList(c2, c1, c6, c8, c4, c9, c7, c3, c5);
        List<ExpressionCall> expectedResult = Arrays.asList(c1, c2, c3, c4, c5, c5, c7, c8, c9);
        Collections.sort(toSort, ExpressionCall.RANK_COMPARATOR);
        
        assertEquals("Incorrect sorting of ExpressionCalls based on their rank", 
                expectedResult, toSort);
    }
}
