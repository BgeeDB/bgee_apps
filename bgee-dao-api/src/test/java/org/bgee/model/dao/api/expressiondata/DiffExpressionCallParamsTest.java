package org.bgee.model.dao.api.expressiondata;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TestAncestor;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallTO.DiffCallType;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallTO.Factor;
import org.junit.Test;

/**
 * Unit tests for the class {@link DiffExpressionCallParams}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class DiffExpressionCallParamsTest extends TestAncestor {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(DiffExpressionCallParamsTest.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test the method {@link DiffExpressionCallParams#canMerge(CallParams)}, 
     * only for the parameters specific to {@code DiffExpressionCallParams} 
     * (see {@link CallParamsTest} for other tests).
     */
    @Test
    public void testCanMerge() {
        DiffExpressionCallParams params = new DiffExpressionCallParams();
        DiffExpressionCallParams paramsToCompare = new DiffExpressionCallParams();
        
        //should not be mergeable with a different type of CallParams
        assertFalse("Incorrect boolean returned by canMerge", 
                params.canMerge(new ExpressionCallParams()));
        
        //it should be mergeable with another DiffExpressionCallParams with no params
        assertTrue("Incorrect boolean returned by canMerge", 
                params.canMerge(paramsToCompare));
        
        //if they have different min conditions, merge should fail
        params.setMinConditionCount(6);
        paramsToCompare.setMinConditionCount(5);
        assertFalse("Incorrect boolean returned by canMerge", 
                params.canMerge(paramsToCompare));
        paramsToCompare.setMinConditionCount(6);
        assertTrue("Incorrect boolean returned by canMerge", 
                params.canMerge(paramsToCompare));
        
        //otherwise, if they have only one parameter different, they can be merged, 
        //as long as the parameter is set in only one of them
        params.setFactor(Factor.DEVELOPMENT);
        assertTrue("Incorrect boolean returned by canMerge", 
                params.canMerge(paramsToCompare));
        paramsToCompare.setFactor(Factor.ANATOMY);
        assertFalse("Incorrect boolean returned by canMerge", 
                params.canMerge(paramsToCompare));
        paramsToCompare.setFactor(Factor.DEVELOPMENT);
        assertTrue("Incorrect boolean returned by canMerge", 
                params.canMerge(paramsToCompare));
        
        //reset to continue
        params.setFactor(null);
        paramsToCompare.setFactor(null);
        
        params.setDiffCallType(DiffCallType.UNDEREXPRESSED);
        assertTrue("Incorrect boolean returned by canMerge", 
                params.canMerge(paramsToCompare));
        paramsToCompare.setDiffCallType(DiffCallType.NOTDIFFEXPRESSED);
        assertFalse("Incorrect boolean returned by canMerge", 
                params.canMerge(paramsToCompare));
        paramsToCompare.setDiffCallType(DiffCallType.UNDEREXPRESSED);
        assertTrue("Incorrect boolean returned by canMerge", 
                params.canMerge(paramsToCompare));
        //reset to continue
        paramsToCompare.setDiffCallType(null);
        
        //if they have more than 1 parameter different, they cannot be merged, 
        //even if parameters are set in only one of them
        //first test just to be sure of the state
        assertTrue("Incorrect boolean returned by canMerge", 
                params.canMerge(paramsToCompare));
        //actual test
        params.setFactor(Factor.DEVELOPMENT);
        assertFalse("Incorrect boolean returned by canMerge", 
                params.canMerge(paramsToCompare));
    }
    
    /**
     * Test the method {@link DiffExpressionCallParams#merge(CallParams)}, 
     * only for the parameters specific to {@code DiffExpressionCallParams} 
     * (see {@link CallParamsTest} for other tests).
     */
    @Test
    public void shouldMerge() {
        //here we do not test whether the merge is meaningful, only if it is 
        //correctly done. For this, we bypass the canMerge method with a spy.
        DiffExpressionCallParams params = spy(new DiffExpressionCallParams());
        DiffExpressionCallParams paramsToCompare = new DiffExpressionCallParams();
        when(params.canMerge(paramsToCompare)).thenReturn(true);
        
        //if one of the factor is null, it "wins" (more general)
        params.setFactor(Factor.DEVELOPMENT);
        DiffExpressionCallParams mergedParams = params.merge(paramsToCompare);
        assertNull("Incorrect merged value for factor", 
                mergedParams.getFactor());
        //if both are set, those set for params is used (completely meaningless, 
        //no use in real code)
        paramsToCompare.setFactor(Factor.ANATOMY);
        mergedParams = params.merge(paramsToCompare);
        assertEquals("Incorrect merged value for factor", Factor.DEVELOPMENT, 
                mergedParams.getFactor());
        
        //if one of the DiffCallType is null, it "wins" (more general)
        params.setDiffCallType(DiffCallType.UNDEREXPRESSED);
        mergedParams = params.merge(paramsToCompare);
        assertNull("Incorrect merged value for diffCallType", 
                mergedParams.getDiffCallType());
        //if both are set, those set for params is used (completely meaningless, 
        //no use in real code)
        paramsToCompare.setDiffCallType(DiffCallType.NOTDIFFEXPRESSED);
        mergedParams = params.merge(paramsToCompare);
        assertEquals("Incorrect merged value for diffCallType", DiffCallType.UNDEREXPRESSED, 
                mergedParams.getDiffCallType());
        
        //the max value between the two minConditionCount is taken (completely 
        //meaningless as well)
        paramsToCompare.setMinConditionCount(6);
        mergedParams = params.merge(paramsToCompare);
        assertEquals("Incorrect merged value for minConditionCount", 6, 
                mergedParams.getMinConditionCount());
    }
    
    /**
     * Test the method {@link DiffExpressionCallParams#getDifferentParametersCount(CallParams)}, 
     * only for the parameters specific to {@code DiffExpressionCallParams} 
     * (see {@link CallParamsTest} for other tests).
     */
    @Test
    public void shouldGetDifferentParametersCount() {
        DiffExpressionCallParams params = new DiffExpressionCallParams();
        DiffExpressionCallParams paramsToCompare = new DiffExpressionCallParams();
        
        //should return the max number when the argument is not of the proper type
        assertEquals("Incorrect different parameter count", 3,
                params.getDifferentParametersCount(new ExpressionCallParams()));

        assertEquals("Incorrect different parameter count", 0,
                params.getDifferentParametersCount(paramsToCompare));
        params.setFactor(Factor.ANATOMY);
        assertEquals("Incorrect different parameter count", 1,
                params.getDifferentParametersCount(paramsToCompare));
        paramsToCompare.setDiffCallType(DiffCallType.UNDEREXPRESSED);
        assertEquals("Incorrect different parameter count", 2,
                params.getDifferentParametersCount(paramsToCompare));
        paramsToCompare.setMinConditionCount(6);
        assertEquals("Incorrect different parameter count", 3,
                params.getDifferentParametersCount(paramsToCompare));
        

        paramsToCompare.setFactor(Factor.ANATOMY);
        assertEquals("Incorrect different parameter count", 2,
                params.getDifferentParametersCount(paramsToCompare));
        params.setDiffCallType(DiffCallType.UNDEREXPRESSED);
        assertEquals("Incorrect different parameter count", 1,
                params.getDifferentParametersCount(paramsToCompare));
        params.setMinConditionCount(6);
        assertEquals("Incorrect different parameter count", 0,
                params.getDifferentParametersCount(paramsToCompare));
    }
    
    /**
     * Test the method {DiffExpressionCallParams#hasDataRestrictions()}, 
     * only for the parameters specific to {@code DiffExpressionCallParams} 
     * (see {@link CallParamsTest} for other tests).
     */
    @Test
    public void testHasDataRestrictions() {
        DiffExpressionCallParams params = new DiffExpressionCallParams();
        assertFalse("Incorrect boolean returned by hasDataRestrictions", 
                params.hasDataRestrictions());
        
        params.setFactor(Factor.ANATOMY);
        assertTrue("Incorrect boolean returned by hasDataRestrictions", 
                params.hasDataRestrictions());
        params.setFactor(null);
        assertFalse("Incorrect boolean returned by hasDataRestrictions", 
                params.hasDataRestrictions());

        params.setDiffCallType(DiffCallType.NOTDIFFEXPRESSED);
        assertTrue("Incorrect boolean returned by hasDataRestrictions", 
                params.hasDataRestrictions());
        params.setDiffCallType(null);
        assertFalse("Incorrect boolean returned by hasDataRestrictions", 
                params.hasDataRestrictions());

        params.setMinConditionCount(10);
        assertTrue("Incorrect boolean returned by hasDataRestrictions", 
                params.hasDataRestrictions());
    }
    
    /**
     * Simply test the getters and setters of {@code DiffExpressionCallParams}, 
     * only for the parameters specific to {@code DiffExpressionCallParams} 
     * (see {@link CallParamsTest} for other tests).
     */
    @Test
    public void shouldSetGet() {
        DiffExpressionCallParams params = new DiffExpressionCallParams();
        
        params.setFactor(Factor.DEVELOPMENT);
        assertEquals("Incorrect set/get for factor", Factor.DEVELOPMENT, 
                params.getFactor());
        
        params.setDiffCallType(DiffCallType.NOTDIFFEXPRESSED);
        assertEquals("Incorrect set/get for diffCallType", DiffCallType.NOTDIFFEXPRESSED, 
                params.getDiffCallType());

        assertEquals("Incorrect set/get for minConditionCount", 
                DiffExpressionCallParams.MINCONDITIONCOUNT, params.getMinConditionCount());
        params.setMinConditionCount(10);
        assertEquals("Incorrect set/get for minConditionCount", 10, 
                params.getMinConditionCount());
    }
}
