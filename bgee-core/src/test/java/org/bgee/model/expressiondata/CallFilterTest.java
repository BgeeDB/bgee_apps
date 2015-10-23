package org.bgee.model.expressiondata;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;

import org.bgee.model.TestAncestor;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.CallData.DiffExpressionCallData;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.baseelements.CallType.DiffExpression;
import org.bgee.model.expressiondata.baseelements.CallType.Expression;
import org.junit.Test;

/**
 * Unit tests for {@link CallFilter}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Oct. 2015
 * @since Bgee 13 Oct. 2015
 */
public class CallFilterTest extends TestAncestor {

    /**
     * Test the sanity checks performed by {@link CallFilter#CallFilter(org.bgee.model.gene.GeneFilter, Set, Set)}.
     */
    //variables unused because we only test initialization.
    @SuppressWarnings("unused")
    @Test
    public void testSanityChecks() {
        try {
            new CallFilter<>(null, null, null);
            fail("An exception should be thrown when no CallData Set is provided.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        try {
            new CallFilter<>(null, null, new HashSet<>());
            fail("An exception should be thrown when the CallData Set is empty.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        try {
            new CallFilter<>(null, null, new HashSet<>(Arrays.asList((CallData<?>) null)));
            fail("An exception should be thrown when the CallData Set contains null value.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        try {
            new CallFilter<ExpressionCallData>(null, null, new HashSet<>(Arrays.asList(
                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX), 
                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX))));
            fail("An exception should be thrown when some CallData target redundant combinations "
                    + "of CallType/DataType.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        try {
            new CallFilter<ExpressionCallData>(null, null, new HashSet<>(Arrays.asList(
                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX),
                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, null))));
            fail("An exception should be thrown when some CallData target redundant combinations "
                    + "of CallType/DataType.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        
        //now, test when everything is fine
        new CallFilter<>(null, null, new HashSet<>(Arrays.asList(
                new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX), 
                new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ), 
                new DiffExpressionCallData(DiffExpression.OVER_EXPRESSED, DataQuality.LOW, null), 
                new DiffExpressionCallData(DiffExpression.UNDER_EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ))));
    }
}
