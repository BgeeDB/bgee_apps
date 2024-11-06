package org.bgee.model.expressiondata.call;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class OTFExpressionCallLoaderTest {
    
    private static final Logger log = LogManager.getLogger(OTFExpressionCallLoaderTest.class.getName());

    @Test
    public void testComputeExpressionScore() {
        // Define max rank
        BigDecimal maxRank = new BigDecimal(50);

        // Test cases for edge ranks and mid-ranks
        // Rank = 1 (should return 100)
        BigDecimal result = OTFExpressionCallLoader.computeExpressionScore(BigDecimal.ONE, maxRank);
        assertEquals("Rank 1 should return a score of 100", new BigDecimal("100.00000"), result);

        // Rank = maxRank (should return 1)
        result = OTFExpressionCallLoader.computeExpressionScore(maxRank, maxRank);
        assertEquals("Max rank should return a score of 1", new BigDecimal("1.00000"), result);

        // Rank = 25 (middle rank in this case, should return around 50)
        result = OTFExpressionCallLoader.computeExpressionScore(new BigDecimal(25), maxRank);
        assertEquals("Middle rank should return a score of around 50", new BigDecimal("51.51020"), result);
        
        // Rank = 10 (lower part of the range)
        result = OTFExpressionCallLoader.computeExpressionScore(new BigDecimal(10), maxRank);
        assertEquals("Rank 10 should return a score of around 82", new BigDecimal("81.81633"), result);

        // Rank = 40 (upper part of the range)
        result = OTFExpressionCallLoader.computeExpressionScore(new BigDecimal(40), maxRank);
        assertEquals("Rank 40 should return a score of around 20", new BigDecimal("21.20408"), result);
    }
}
