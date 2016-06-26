package org.bgee.model.expressiondata;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Arrays;
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
    private final static Logger log = LogManager.getLogger(ConditionUtilsTest.class.getName());
    
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
        ExpressionCall c4 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("40000"));
        ExpressionCall c5 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("40010"));
        //on purpose we incorrectly order the calls (regression test)
        List<ExpressionCall> toCluster = Arrays.asList(c5, c1, c3, c4, c2);
        Map<ExpressionCall, Integer> expectedClusters = new HashMap<>();
        expectedClusters.put(c1, 0);
        expectedClusters.put(c2, 0);
        expectedClusters.put(c3, 1);
        expectedClusters.put(c4, 2);
        expectedClusters.put(c5, 2);
        
        for (ExpressionCall.ClusteringMethod method: ExpressionCall.ClusteringMethod.values()) {
            log.debug("Testing clustering method: " + method.name());
            assertEquals("Incorrect clustering of expression scores for method: " + method.name(), 
                    expectedClusters, 
                    ExpressionCall.generateMeanRankScoreClustering(toCluster, method, 0.2));
        }
    }
    
    /**
     * Test {@link #ExpressionCall#filterAndOrderByGlobalMeanRank(Collection)}.
     */
    @Test
    public void shouldFilterAndOrderByGlobalMeanRank() {
        ExpressionCall c1 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("1.25"));
        ExpressionCall c2 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("1.27"));
        ExpressionCall c3 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("1.27"));
        ExpressionCall c4 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("10000"));
        ExpressionCall c5 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("40000"));
        ExpressionCall c6 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("40010"));
        List<ExpressionCall> toFilterSort = Arrays.asList(c5, c1, c6, c3, c4, c2);
        List<ExpressionCall> expectedResult = Arrays.asList(c1, c2, c4, c5, c6);
        
        assertEquals("Incorrect sort and filtering of ExpressionCalls based on their rank", 
                expectedResult, ExpressionCall.filterAndOrderByGlobalMeanRank(toFilterSort));
    }
}
