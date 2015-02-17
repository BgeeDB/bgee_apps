package org.bgee.model.dao.api.expressiondata;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TestAncestor;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.DiffExprCallType;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.ComparisonFactor;
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
    
//    /**
//     * Test the method {@link DiffExpressionCallParams#canMerge(CallParams)}, 
//     * only for the parameters specific to {@code DiffExpressionCallParams} 
//     * (see {@link CallParamsTest} for other tests).
//     */
//    @Test
//    public void testCanMerge() {
//        DiffExpressionCallParams params = new DiffExpressionCallParams();
//        DiffExpressionCallParams paramsToCompare = new DiffExpressionCallParams();
//        
//        //should not be mergeable with a different type of CallParams
//        assertFalse("Incorrect boolean returned by canMerge", 
//                params.canMerge(new ExpressionCallParams()));
//        
//        //it should be mergeable with another DiffExpressionCallParams with no params
//        assertTrue("Incorrect boolean returned by canMerge", 
//                params.canMerge(paramsToCompare));
//        
////        //if they have different min conditions, merge should fail
////        params.setMinConditionCount(6);
////        assertFalse("Incorrect boolean returned by canMerge", 
////                params.canMerge(paramsToCompare));
////        paramsToCompare.setMinConditionCount(6);
////        assertTrue("Incorrect boolean returned by canMerge", 
////                params.canMerge(paramsToCompare));
//        
//        //otherwise, if they have only one parameter different, they can be merged, 
//        //as long as the parameter is set in only one of them
//        params.setComparisonFactor(ComparisonFactor.DEVELOPMENT);
//        assertTrue("Incorrect boolean returned by canMerge", 
//                params.canMerge(paramsToCompare));
//        paramsToCompare.setComparisonFactor(ComparisonFactor.ANATOMY);
//        assertFalse("Incorrect boolean returned by canMerge", 
//                params.canMerge(paramsToCompare));
//        paramsToCompare.setComparisonFactor(ComparisonFactor.DEVELOPMENT);
//        assertTrue("Incorrect boolean returned by canMerge", 
//                params.canMerge(paramsToCompare));
//        
//        //reset to continue
//        params.setComparisonFactor(null);
//        paramsToCompare.setComparisonFactor(null);
//        
//        params.setDiffExprCallTypeAffymetrix(DiffExprCallType.UNDER_EXPRESSED);
//        assertTrue("Incorrect boolean returned by canMerge", 
//                params.canMerge(paramsToCompare));
//        paramsToCompare.setDiffExprCallTypeAffymetrix(DiffExprCallType.NOT_EXPRESSED);
//        assertFalse("Incorrect boolean returned by canMerge", 
//                params.canMerge(paramsToCompare));
//        paramsToCompare.setDiffExprCallTypeAffymetrix(DiffExprCallType.UNDER_EXPRESSED);
//        assertTrue("Incorrect boolean returned by canMerge", 
//                params.canMerge(paramsToCompare));
//        //reset to continue
//        paramsToCompare.setDiffExprCallTypeAffymetrix(null);
//                
//        params.setDiffExprCallTypeRNASeq(DiffExprCallType.UNDER_EXPRESSED);
//        assertTrue("Incorrect boolean returned by canMerge", 
//                params.canMerge(paramsToCompare));
//        paramsToCompare.setDiffExprCallTypeRNASeq(DiffExprCallType.NOT_EXPRESSED);
//        assertFalse("Incorrect boolean returned by canMerge", 
//                params.canMerge(paramsToCompare));
//        paramsToCompare.setDiffExprCallTypeRNASeq(DiffExprCallType.UNDER_EXPRESSED);
//        assertTrue("Incorrect boolean returned by canMerge", 
//                params.canMerge(paramsToCompare));
//        //reset to continue
//        paramsToCompare.setDiffExprCallTypeAffymetrix(null);
//
//        //if they have more than 1 parameter different, they cannot be merged, 
//        //even if parameters are set in only one of them
//        //first test just to be sure of the state
//        assertTrue("Incorrect boolean returned by canMerge", 
//                params.canMerge(paramsToCompare));
//        //actual test
//        params.setComparisonFactor(ComparisonFactor.DEVELOPMENT);
//        assertFalse("Incorrect boolean returned by canMerge", 
//                params.canMerge(paramsToCompare));
//    }
//    
//    /**
//     * Test the method {@link DiffExpressionCallParams#merge(CallParams)}, 
//     * only for the parameters specific to {@code DiffExpressionCallParams} 
//     * (see {@link CallParamsTest} for other tests).
//     */
//    @Test
//    public void shouldMerge() {
//        //here we do not test whether the merge is meaningful, only if it is 
//        //correctly done. For this, we bypass the canMerge method with a spy.
//        DiffExpressionCallParams params = spy(new DiffExpressionCallParams());
//        DiffExpressionCallParams paramsToCompare = new DiffExpressionCallParams();
//        when(params.canMerge(paramsToCompare)).thenReturn(true);
//        
//        //if one of the factor is null, it "wins" (more general)
//        params.setComparisonFactor(ComparisonFactor.DEVELOPMENT);
//        DiffExpressionCallParams mergedParams = params.merge(paramsToCompare);
//        assertNull("Incorrect merged value for factor", 
//                mergedParams.getComparisonFactor());
//        //if both are set, those set for params is used (completely meaningless, 
//        //no use in real code)
//        paramsToCompare.setComparisonFactor(ComparisonFactor.ANATOMY);
//        mergedParams = params.merge(paramsToCompare);
//        assertEquals("Incorrect merged value for factor", ComparisonFactor.DEVELOPMENT, 
//                mergedParams.getComparisonFactor());
//        
//        //if one of the DiffCallType is null, it "wins" (more general)
//        params.setDiffExprCallTypeAffymetrix(DiffExprCallType.UNDER_EXPRESSED);
//        mergedParams = params.merge(paramsToCompare);
//        assertNull("Incorrect merged value for diffExprCallType", 
//                mergedParams.getDiffExprCallTypeAffymetrix());
//        //if both are set, those set for params is used (completely meaningless, 
//        //no use in real code)
//        paramsToCompare.setDiffExprCallTypeAffymetrix(DiffExprCallType.NOT_DIFF_EXPRESSED);
//        mergedParams = params.merge(paramsToCompare);
//        assertEquals("Incorrect merged value for diffExprCallType", DiffExprCallType.UNDER_EXPRESSED, 
//                mergedParams.getDiffExprCallTypeAffymetrix());
//        
//        //if one of the DiffExprCallType is null, it "wins" (more general)
//        params.setDiffExprCallTypeRNASeq(DiffExprCallType.UNDER_EXPRESSED);
//        mergedParams = params.merge(paramsToCompare);
//        assertNull("Incorrect merged value for diffExprCallType", 
//                mergedParams.getDiffExprCallTypeRNASeq());
//        //if both are set, those set for params is used (completely meaningless, 
//        //no use in real code)
//        paramsToCompare.setDiffExprCallTypeRNASeq(DiffExprCallType.NOT_DIFF_EXPRESSED);
//        mergedParams = params.merge(paramsToCompare);
//        assertEquals("Incorrect merged value for diffExprCallType", DiffExprCallType.UNDER_EXPRESSED, 
//                mergedParams.getDiffExprCallTypeRNASeq());
//
////        //the max value between the two minConditionCount is taken (completely 
////        //meaningless as well)
////        paramsToCompare.setMinConditionCount(6);
////        mergedParams = params.merge(paramsToCompare);
////        assertEquals("Incorrect merged value for minConditionCount", 6, 
////                mergedParams.getMinConditionCount());
//    }
//    
//    /**
//     * Test the method {@link DiffExpressionCallParams#getDifferentParametersCount(CallParams)}, 
//     * only for the parameters specific to {@code DiffExpressionCallParams} 
//     * (see {@link CallParamsTest} for other tests).
//     */
//    @Test
//    public void shouldGetDifferentParametersCount() {
//        DiffExpressionCallParams params = new DiffExpressionCallParams();
//        DiffExpressionCallParams paramsToCompare = new DiffExpressionCallParams();
//        
//        //should return the max number when the argument is not of the proper type
//        assertEquals("Incorrect different parameter count", 3,
//                params.getDifferentParametersCount(new ExpressionCallParams()));
//
//        assertEquals("Incorrect different parameter count", 0,
//                params.getDifferentParametersCount(paramsToCompare));
//        params.setComparisonFactor(ComparisonFactor.ANATOMY);
//        assertEquals("Incorrect different parameter count", 1,
//                params.getDifferentParametersCount(paramsToCompare));
//        paramsToCompare.setDiffExprCallTypeAffymetrix(DiffExprCallType.UNDER_EXPRESSED);
//        assertEquals("Incorrect different parameter count", 2,
//                params.getDifferentParametersCount(paramsToCompare));
//        paramsToCompare.setDiffExprCallTypeRNASeq(DiffExprCallType.OVER_EXPRESSED);
//        assertEquals("Incorrect different parameter count", 3,
//                params.getDifferentParametersCount(paramsToCompare));
////        paramsToCompare.setMinConditionCount(6);
////        assertEquals("Incorrect different parameter count", 3,
////                params.getDifferentParametersCount(paramsToCompare));
////        
//
//        paramsToCompare.setComparisonFactor(ComparisonFactor.ANATOMY);
//        assertEquals("Incorrect different parameter count", 2,
//                params.getDifferentParametersCount(paramsToCompare));
//        params.setDiffExprCallTypeAffymetrix(DiffExprCallType.UNDER_EXPRESSED);
//        assertEquals("Incorrect different parameter count", 1,
//                params.getDifferentParametersCount(paramsToCompare));
//        params.setDiffExprCallTypeRNASeq(DiffExprCallType.OVER_EXPRESSED);
//        assertEquals("Incorrect different parameter count", 0,
//                params.getDifferentParametersCount(paramsToCompare));
////        params.setMinConditionCount(6);
////        assertEquals("Incorrect different parameter count", 0,
////                params.getDifferentParametersCount(paramsToCompare));
//    }
//    
//    /**
//     * Test the method {DiffExpressionCallParams#hasDataRestrictions()}, 
//     * only for the parameters specific to {@code DiffExpressionCallParams} 
//     * (see {@link CallParamsTest} for other tests).
//     */
//    @Test
//    public void testHasDataRestrictions() {
//        DiffExpressionCallParams params = new DiffExpressionCallParams();
//        assertFalse("Incorrect boolean returned by hasDataRestrictions", 
//                params.hasDataRestrictions());
//        
//        params.setComparisonFactor(ComparisonFactor.ANATOMY);
//        assertTrue("Incorrect boolean returned by hasDataRestrictions", 
//                params.hasDataRestrictions());
//        params.setComparisonFactor(null);
//        assertFalse("Incorrect boolean returned by hasDataRestrictions", 
//                params.hasDataRestrictions());
//
//        params.setDiffExprCallTypeAffymetrix(DiffExprCallType.NOT_DIFF_EXPRESSED);
//        assertTrue("Incorrect boolean returned by hasDataRestrictions", 
//                params.hasDataRestrictions());
//        params.setDiffExprCallTypeAffymetrix(null);
//        assertFalse("Incorrect boolean returned by hasDataRestrictions", 
//                params.hasDataRestrictions());
//
//        params.setDiffExprCallTypeRNASeq(DiffExprCallType.NOT_EXPRESSED);
//        assertTrue("Incorrect boolean returned by hasDataRestrictions", 
//                params.hasDataRestrictions());
//        params.setDiffExprCallTypeRNASeq(null);
//        assertFalse("Incorrect boolean returned by hasDataRestrictions", 
//                params.hasDataRestrictions());
//
////        params.setMinConditionCount(10);
////        assertTrue("Incorrect boolean returned by hasDataRestrictions", 
////                params.hasDataRestrictions());
//    }
    
    /**
     * Simply test the getters and setters of {@code DiffExpressionCallParams}, 
     * only for the parameters specific to {@code DiffExpressionCallParams} 
     * (see {@link CallParamsTest} for other tests).
     */
    @Test
    public void shouldSetGet() {
        DiffExpressionCallParams params = new DiffExpressionCallParams();
        
        params.setComparisonFactor(ComparisonFactor.DEVELOPMENT);
        assertEquals("Incorrect set/get for factor", ComparisonFactor.DEVELOPMENT, 
                params.getComparisonFactor());
        
        params.setIncludeAffymetrixTypes(true);
        assertTrue("Incorrect includeAffymetrixTypes set/get", params.isIncludeAffymetrixTypes());
        params.setIncludeAffymetrixTypes(false);

        params.setIncludeRNASeqTypes(true);
        assertTrue("Incorrect includeRnaSeqTypes set/get", params.isIncludeRNASeqTypes());
        params.setIncludeRNASeqTypes(false);

        params.setSatisfyAllCallTypeConditions(true);
        assertTrue("Incorrect satisfyAllCallTypeCondition set/get", 
                params.isSatisfyAllCallTypeConditions());
        params.setSatisfyAllCallTypeConditions(false);

        Set<DiffExprCallType> twoElementSet = new HashSet<DiffExprCallType>();
        twoElementSet.add(DiffExprCallType.NO_DATA);
        twoElementSet.add(DiffExprCallType.NOT_DIFF_EXPRESSED);
        DiffExprCallType thirdElement = DiffExprCallType.UNDER_EXPRESSED;
        Set<DiffExprCallType> threeElementSet = new HashSet<DiffExprCallType>(twoElementSet);
        threeElementSet.add(thirdElement);
        
        params.addAllAffymetrixDiffExprCallTypes(twoElementSet);
        assertEquals("Incorrect affymetrixTypes set/get", twoElementSet, 
                params.getAffymetrixDiffExprCallTypes());
        params.addAffymetrixDiffExprCallType(thirdElement);
        assertEquals("Incorrect affymetrixTypes set/get", threeElementSet, 
                params.getAffymetrixDiffExprCallTypes());
        params.clearAffymetrixDiffExprCallTypes();
        assertEquals("Incorrect affymetrixTypes set/get", new HashSet<String>(), 
                params.getAffymetrixDiffExprCallTypes());

        params.addAllRNASeqDiffExprCallTypes(twoElementSet);
        assertEquals("Incorrect rnaSeqTypes set/get", twoElementSet, 
                params.getRNASeqDiffExprCallTypes());
        params.addRNASeqDiffExprCallType(thirdElement);
        assertEquals("Incorrect rnaSeqTypes set/get", threeElementSet, 
                params.getRNASeqDiffExprCallTypes());
        params.clearRNASeqDiffExprCallTypes();
        assertEquals("Incorrect rnaSeqTypes set/get", new HashSet<String>(), 
                params.getRNASeqDiffExprCallTypes());

//        assertEquals("Incorrect set/get for minConditionCount", 
//                DiffExpressionCallParams.MINCONDITIONCOUNT, params.getMinConditionCount());
//        params.setMinConditionCount(10);
//        assertEquals("Incorrect set/get for minConditionCount", 10, 
//                params.getMinConditionCount());
    }
}
