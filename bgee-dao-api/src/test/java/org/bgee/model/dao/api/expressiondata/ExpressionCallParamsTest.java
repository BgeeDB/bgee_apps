package org.bgee.model.dao.api.expressiondata;

import static org.junit.Assert.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TestAncestor;
import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * Unit tests for the class {@link ExpressionCallParams}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class ExpressionCallParamsTest extends TestAncestor {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(ExpressionCallParamsTest.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test the method {@link ExpressionCallParams#canMerge(CallParams)}, 
     * only for the parameters specific to {@code ExpressionCallParams} 
     * (see {@link CallParamsTest} for other tests).
     */
    @Test
    public void testCanMerge() {
        ExpressionCallParams params = new ExpressionCallParams();
        ExpressionCallParams paramsToCompare = new ExpressionCallParams();
        
        //should not be mergeable with a different type of CallParams
        assertFalse("Incorrect boolean returned by canMerge", 
                params.canMerge(new NoExpressionCallParams()));
        
        //it should be mergeable with another ExpressionCallParams with no params
        assertTrue("Incorrect boolean returned by canMerge", 
                params.canMerge(paramsToCompare));
        
        //if they have different parameters, merge should fail
        params.setIncludeSubstructures(true);
        assertFalse("Incorrect boolean returned by canMerge", 
                params.canMerge(paramsToCompare));
        paramsToCompare.setIncludeSubstructures(true);
        assertTrue("Incorrect boolean returned by canMerge", 
                params.canMerge(paramsToCompare));
        
        params.setIncludeSubStages(true);
        assertFalse("Incorrect boolean returned by canMerge", 
                params.canMerge(paramsToCompare));
        paramsToCompare.setIncludeSubStages(true);
        assertTrue("Incorrect boolean returned by canMerge", 
                params.canMerge(paramsToCompare));
    }
    
    /**
     * Test the method {@link ExpressionCallParams#merge(CallParams)}, 
     * only for the parameters specific to {@code ExpressionCallParams} 
     * (see {@link CallParamsTest} for other tests).
     */
    @Test
    public void shouldMerge() {
        //here we do not test whether the merge is meaningful, only if it is 
        //correctly done. For this, we bypass the canMerge method with a spy.
        ExpressionCallParams params = spy(new ExpressionCallParams());
        ExpressionCallParams paramsToCompare = new ExpressionCallParams();
        when(params.canMerge(paramsToCompare)).thenReturn(true);
        
        ExpressionCallParams mergedParams = params.merge(paramsToCompare);
        assertFalse("Incorrect merged value for includeSubstructures", 
                mergedParams.isIncludeSubstructures());
        assertFalse("Incorrect merged value for includeSubStages", 
                mergedParams.isIncludeSubStages());
        
        params.setIncludeSubstructures(true);
        mergedParams = params.merge(paramsToCompare);
        assertTrue("Incorrect merged value for includeSubstructures", 
                mergedParams.isIncludeSubstructures());
        assertFalse("Incorrect merged value for includeSubStages", 
                mergedParams.isIncludeSubStages());
        
        paramsToCompare.setIncludeSubStages(true);
        mergedParams = params.merge(paramsToCompare);
        assertTrue("Incorrect merged value for includeSubstructures", 
                mergedParams.isIncludeSubstructures());
        assertTrue("Incorrect merged value for includeSubStages", 
                mergedParams.isIncludeSubStages());
    }
    
    /**
     * Test the method {@link ExpressionCallParams#getDifferentParametersCount(CallParams)}, 
     * only for the parameters specific to {@code ExpressionCallParams} 
     * (see {@link CallParamsTest} for other tests).
     */
    @Test
    public void shouldGetDifferentParametersCount() {
        ExpressionCallParams params = new ExpressionCallParams();
        ExpressionCallParams paramsToCompare = new ExpressionCallParams();
        
        //should return the max number when the argument is not of the proper type
        assertEquals("Incorrect different parameter count", 2,
                params.getDifferentParametersCount(new NoExpressionCallParams()));

        assertEquals("Incorrect different parameter count", 0,
                params.getDifferentParametersCount(paramsToCompare));
        params.setIncludeSubstructures(true);
        assertEquals("Incorrect different parameter count", 1,
                params.getDifferentParametersCount(paramsToCompare));
        paramsToCompare.setIncludeSubStages(true);
        assertEquals("Incorrect different parameter count", 2,
                params.getDifferentParametersCount(paramsToCompare));
        
        paramsToCompare.setIncludeSubstructures(true);
        assertEquals("Incorrect different parameter count", 1,
                params.getDifferentParametersCount(paramsToCompare));
        params.setIncludeSubStages(true);
        assertEquals("Incorrect different parameter count", 0,
                params.getDifferentParametersCount(paramsToCompare));
    }
    
    /**
     * Test the method {ExpressionCallParams#hasDataRestrictions()}, 
     * only for the parameters specific to {@code ExpressionCallParams} 
     * (see {@link CallParamsTest} for other tests).
     */
    @Test
    public void testHasDataRestrictions() {
        //as of Bgee 13, this method in ExpressionCallParams only relies on 
        //the super class method, already tested elsewhere.
        ExpressionCallParams params = new ExpressionCallParams();
        assertFalse("Incorrect boolean returned by hasDataRestrictions", 
                params.hasDataRestrictions());
        
        params.setIncludeSubstructures(true);
        assertFalse("Incorrect boolean returned by hasDataRestrictions", 
                params.hasDataRestrictions());
    }
    
    /**
     * Simply test the getters and setters of {@code ExpressionCallParams}, 
     * only for the parameters specific to {@code ExpressionCallParams} 
     * (see {@link CallParamsTest} for other tests).
     */
    @Test
    public void shouldSetGet() {
        ExpressionCallParams params = new ExpressionCallParams();
        
        assertFalse("Incorrect set/get for includeSubstructures", 
                params.isIncludeSubstructures());
        params.setIncludeSubstructures(true);
        assertTrue("Incorrect set/get for includeSubstructures", 
                params.isIncludeSubstructures());

        assertFalse("Incorrect set/get for includeSubStages", 
                params.isIncludeSubStages());
        params.setIncludeSubStages(true);
        assertTrue("Incorrect set/get for includeSubStages", 
                params.isIncludeSubStages());
    }
}
