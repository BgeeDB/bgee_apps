package org.bgee.model.expressiondata.baseelements;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.TestAncestor;
import org.junit.Test;

/**
 * @author Frederic Bastian
 * @since Bgee 14 Feb. 2019
 * @see ExpressionLevelCategory
 * @version Bgee 14 Feb. 2019
 */
public class ExpressionLevelCategoryTest  extends TestAncestor {
    private final static Logger log = LogManager.getLogger(ExpressionLevelCategoryTest.class.getName());
    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Test for {@link ExpressionLevelCategory#
     * getExpressionLevelCategory(EntityMinMaxRanks, BigDecimal)}.
     */
    @Test
    public void shouldGetExpressionLevelCategory() {
        BigDecimal minRank = new BigDecimal("2499.98");
        BigDecimal maxRank = new BigDecimal("22500.01");
        EntityMinMaxRanks<?> minMaxRanks = new EntityMinMaxRanks<>(minRank, maxRank);

        assertEquals("Incorrect inferred ExpressionLevelCategory", ExpressionLevelCategory.HIGH,
                ExpressionLevelCategory.getExpressionLevelCategory(minMaxRanks, new BigDecimal("2500.1")));

        assertEquals("Incorrect inferred ExpressionLevelCategory", ExpressionLevelCategory.MEDIUM,
                ExpressionLevelCategory.getExpressionLevelCategory(minMaxRanks, new BigDecimal("10000")));

        assertEquals("Incorrect inferred ExpressionLevelCategory", ExpressionLevelCategory.LOW,
                ExpressionLevelCategory.getExpressionLevelCategory(minMaxRanks, new BigDecimal("18000")));

        try {
            ExpressionLevelCategory.getExpressionLevelCategory(minMaxRanks, new BigDecimal("1"));
            //test failed
            fail("Rank smaller than min rank should throw exception");
        } catch (IllegalArgumentException e) {
            //test passed, do nothing
        }
        try {
            ExpressionLevelCategory.getExpressionLevelCategory(minMaxRanks, new BigDecimal("100000"));
            //test failed
            fail("Rank greater than max rank should throw exception");
        } catch (IllegalArgumentException e) {
            //test passed, do nothing
        }
    }
}