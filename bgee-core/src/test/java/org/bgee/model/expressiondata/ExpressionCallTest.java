package org.bgee.model.expressiondata;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.TestAncestor;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.junit.Test;

public class ExpressionCallTest extends TestAncestor {
    private final static Logger log = LogManager.getLogger(ConditionUtilsTest.class.getName());
    
    @Override
    protected Logger getLogger() {
        return log;
    } 
    
    @Test
    public void shouldGenerateCallsToScoreGroupIndex() {
        //we don't bother to retrieve exact score thresholds, we just create calls 
        //with very obvious to cluster expression calls
        ExpressionCall c1 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("1.25"));
        ExpressionCall c2 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("1.3"));
        ExpressionCall c3 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("10000"));
        ExpressionCall c4 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("40000"));
        ExpressionCall c5 = new ExpressionCall(null, null, null, null, null, null, new BigDecimal("40001"));
        Set<ExpressionCall> toCluster = new HashSet<>(Arrays.asList(c1, c2, c3, c4, c5));
        Map<ExpressionCall, Integer> expectedClusters = new HashMap<>();
        expectedClusters.put(c1, 0);
        expectedClusters.put(c2, 0);
        expectedClusters.put(c3, 1);
        expectedClusters.put(c4, 2);
        expectedClusters.put(c5, 2);
        
        assertEquals("Incorrect clustering of expression scores", expectedClusters, 
                ExpressionCall.generateCallsToScoreGroupIndex(toCluster));
    }
}
