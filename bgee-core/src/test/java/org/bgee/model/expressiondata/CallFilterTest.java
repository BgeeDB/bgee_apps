package org.bgee.model.expressiondata;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;

import org.bgee.model.TestAncestor;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.DiffExpressionFactor;
import org.bgee.model.expressiondata.CallData.DiffExpressionCallData;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallFilter.DiffExpressionCallFilter;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.baseelements.CallType.DiffExpression;
import org.bgee.model.expressiondata.baseelements.CallType.Expression;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.DataPropagation.PropagationState;
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
            new ExpressionCallFilter(null, null, null, null);
            fail("An exception should be thrown when no CallData Set is provided.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        try {
            new ExpressionCallFilter(null, null, null, new HashSet<>());
            fail("An exception should be thrown when the CallData Set is empty.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        try {
            new ExpressionCallFilter(null, null, null, 
                    new HashSet<>(Arrays.asList((ExpressionCallData) null)));
            fail("An exception should be thrown when the CallData Set contains null value.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        try {
            new ExpressionCallFilter(null, null, null, new HashSet<>(Arrays.asList(
                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX), 
                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX))));
            fail("An exception should be thrown when some CallData target redundant combinations "
                    + "of CallType/DataType.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        try {
            new ExpressionCallFilter(null, null, null, new HashSet<>(Arrays.asList(
                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX),
                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, null))));
            fail("An exception should be thrown when some CallData target redundant combinations "
                    + "of CallType/DataType.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        try {
            new DiffExpressionCallFilter(null, null, new HashSet<>(Arrays.asList(
                    new DiffExpressionCallData(DiffExpressionFactor.ANATOMY, DiffExpression.UNDER_EXPRESSED, 
                            DataQuality.LOW, DataType.RNA_SEQ), 
                    new DiffExpressionCallData(DiffExpressionFactor.ANATOMY, DiffExpression.UNDER_EXPRESSED, 
                            DataQuality.HIGH, DataType.RNA_SEQ))));
            fail("An exception should be thrown when some CallData target redundant combinations "
                    + "of CallType/DataType/DiffExpressionFactor.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        
        //test when DataPropagation is incompatible with the provided CallData
        try {
            new ExpressionCallFilter(null, null, 
                    new DataPropagation(PropagationState.ANCESTOR, PropagationState.SELF), 
                    new HashSet<>(Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, null), 
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, null))));
            fail("An exception should be thrown when the DataPropagation is incompatible "
                    + "with the requested CallType.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        try {
            new ExpressionCallFilter(null, null, 
                    new DataPropagation(PropagationState.SELF, PropagationState.SELF_OR_DESCENDANT), 
                    new HashSet<>(Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, null), 
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, null))));
            fail("An exception should be thrown when the DataPropagation is incompatible "
                    + "with the requested CallType.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        
        //now, test when everything is fine
        new CallFilter<>(null, null, null, new HashSet<>(Arrays.asList(
                new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX), 
                new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ), 
                new DiffExpressionCallData(DiffExpressionFactor.ANATOMY, DiffExpression.OVER_EXPRESSED, 
                        DataQuality.LOW, null), 
                new DiffExpressionCallData(DiffExpressionFactor.ANATOMY, DiffExpression.UNDER_EXPRESSED, 
                        DataQuality.LOW, DataType.RNA_SEQ), 
                new DiffExpressionCallData(DiffExpressionFactor.DEVELOPMENT, DiffExpression.UNDER_EXPRESSED, 
                        DataQuality.LOW, DataType.RNA_SEQ))));
    }
}
