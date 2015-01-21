package org.bgee.model.dao.api.expressiondata;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TestAncestor;
import org.junit.Test;

/**
 * Unit tests for the class {@link NoExpressionCallParams}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class NoExpressionCallParamsTest extends TestAncestor {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(NoExpressionCallParamsTest.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }
    
//    /**
//     * Test the method {@link NoExpressionCallParams#canMerge(CallParams)}, 
//     * only for the parameters specific to {@code NoExpressionCallParams} 
//     * (see {@link CallParamsTest} for other tests).
//     */
//    @Test
//    public void testCanMerge() {
//        NoExpressionCallParams params = new NoExpressionCallParams();
//        NoExpressionCallParams paramsToCompare = new NoExpressionCallParams();
//        
//        //should not be mergeable with a different type of CallParams
//        assertFalse("Incorrect boolean returned by canMerge", 
//                params.canMerge(new ExpressionCallParams()));
//        
//        //it should be mergeable with another NoExpressionCallParams with no params
//        assertTrue("Incorrect boolean returned by canMerge", 
//                params.canMerge(paramsToCompare));
//        
//        //if they have different parameters, merge should fail
//        params.setIncludeParentStructures(true);
//        assertFalse("Incorrect boolean returned by canMerge", 
//                params.canMerge(paramsToCompare));
//        paramsToCompare.setIncludeParentStructures(true);
//        assertTrue("Incorrect boolean returned by canMerge", 
//                params.canMerge(paramsToCompare));
//    }
//    
//    /**
//     * Test the method {@link NoExpressionCallParams#merge(CallParams)}, 
//     * only for the parameters specific to {@code NoExpressionCallParams} 
//     * (see {@link CallParamsTest} for other tests).
//     */
//    @Test
//    public void shouldMerge() {
//        //here we do not test whether the merge is meaningful, only if it is 
//        //correctly done. For this, we bypass the canMerge method with a spy.
//        NoExpressionCallParams params = spy(new NoExpressionCallParams());
//        NoExpressionCallParams paramsToCompare = new NoExpressionCallParams();
//        when(params.canMerge(paramsToCompare)).thenReturn(true);
//        
//        NoExpressionCallParams mergedParams = params.merge(paramsToCompare);
//        assertFalse("Incorrect merged value for includeParentStructures", 
//                mergedParams.isIncludeParentStructures());
//        
//        params.setIncludeParentStructures(true);
//        mergedParams = params.merge(paramsToCompare);
//        assertTrue("Incorrect merged value for includeParentStructures", 
//                mergedParams.isIncludeParentStructures());
//    }
//    
//    /**
//     * Test the method {@link NoExpressionCallParams#getDifferentParametersCount(CallParams)}, 
//     * only for the parameters specific to {@code NoExpressionCallParams} 
//     * (see {@link CallParamsTest} for other tests).
//     */
//    @Test
//    public void shouldGetDifferentParametersCount() {
//        NoExpressionCallParams params = new NoExpressionCallParams();
//        NoExpressionCallParams paramsToCompare = new NoExpressionCallParams();
//        
//        //should return the max number when the argument is not of the proper type
//        assertEquals("Incorrect different parameter count", 1,
//                params.getDifferentParametersCount(new ExpressionCallParams()));
//
//        assertEquals("Incorrect different parameter count", 0,
//                params.getDifferentParametersCount(paramsToCompare));
//        params.setIncludeParentStructures(true);
//        assertEquals("Incorrect different parameter count", 1,
//                params.getDifferentParametersCount(paramsToCompare));
//
//        paramsToCompare.setIncludeParentStructures(true);
//        assertEquals("Incorrect different parameter count", 0,
//                params.getDifferentParametersCount(paramsToCompare));
//    }
//    
//    /**
//     * Test the method {NoExpressionCallParams#hasDataRestrictions()}, 
//     * only for the parameters specific to {@code NoExpressionCallParams} 
//     * (see {@link CallParamsTest} for other tests).
//     */
//    @Test
//    public void testHasDataRestrictions() {
//        //as of Bgee 13, this method in NoExpressionCallParams only relies on 
//        //the super class method, already tested elsewhere.
//        NoExpressionCallParams params = new NoExpressionCallParams();
//        assertFalse("Incorrect boolean returned by hasDataRestrictions", 
//                params.hasDataRestrictions());
//        
//        params.setIncludeParentStructures(true);
//        assertFalse("Incorrect boolean returned by hasDataRestrictions", 
//                params.hasDataRestrictions());
//    }
    
    /**
     * Simply test the getters and setters of {@code NoExpressionCallParams}, 
     * only for the parameters specific to {@code NoExpressionCallParams} 
     * (see {@link CallParamsTest} for other tests).
     */
    @Test
    public void shouldSetGet() {
        NoExpressionCallParams params = new NoExpressionCallParams();
        
        assertFalse("Incorrect set/get for includeParentStructures", 
                params.isIncludeParentStructures());
        params.setIncludeParentStructures(true);
        assertTrue("Incorrect set/get for includeSubstructures", 
                params.isIncludeParentStructures());
    }
}
