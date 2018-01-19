package org.bgee.model.dao.api.expressiondata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TestAncestor;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.junit.Test;

/**
 * Unit tests for the class {@link CallParams}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class CallParamsTest extends TestAncestor {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(CallParamsTest.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Anonymous class extending {@code CallParams}, which is abstract, 
     * to test its methods with no overloading from real subclasses. 
     */
    class FakeCallParams extends CallParams {
        protected FakeCallParams() {
            //this could be any CallTO
            super(new ExpressionCallTO());
        }
//        @Override
//        protected CallParams merge(CallParams paramsToMerge) {
//            CallParams newParams = new FakeCallParams();
//            super.merge(paramsToMerge, newParams);
//            return newParams;
//        }
    }
    
//    /**
//     * Unit test for {@link CallTO#merge(Collection)}.
//     */
//    @Test
//    public void shouldMergeAll() {
//        //first, we mock CallParams that are not mergeable
//        CallParams params1 = mock(CallParams.class);
//        CallParams params2 = mock(CallParams.class);
//        CallParams params3 = mock(CallParams.class);
//        when(params1.merge(params2)).thenReturn(null);
//        when(params1.merge(params3)).thenReturn(null);
//        when(params2.merge(params1)).thenReturn(null);
//        when(params2.merge(params3)).thenReturn(null);
//        when(params3.merge(params1)).thenReturn(null);
//        when(params3.merge(params2)).thenReturn(null);
//        
//        //we provide the Collection of CallParams as a List, so that we control 
//        //the order in which the CallParams will be merged
//        List<CallParams> allParams = new ArrayList<CallParams>();
//        allParams.add(params1);
//        allParams.add(params2);
//        allParams.add(params3);
//        
//        Set<CallParams> mergedParams = CallParams.merge(allParams);
//        //nothing should have been merged
//        assertTrue("Incorrect merged Set returned", mergedParams.size() == 3 && 
//                mergedParams.contains(params1) && mergedParams.contains(params2) && 
//                mergedParams.contains(params3));
//        
//        //now we make params1 and params3 mergeable
//        CallParams merged = mock(CallParams.class);
//        when(params1.merge(params3)).thenReturn(merged);
//        mergedParams = CallParams.merge(allParams);
//        //check that the merged occurred 
//        assertTrue("Incorrect merged Set returned", mergedParams.size() == 2 && 
//                mergedParams.contains(merged) && mergedParams.contains(params2));
//        
//        //now we make the resulting merged from params1 and params3 to be mergeable 
//        //with params2. As we provide a List, we now that params1 and params2 will be 
//        //first examined, then params1 and params3. It means that the resulting merged 
//        //will be merged with params2 in a second pass, so we check this behavior
//        CallParams merged2 = mock(CallParams.class);
//        when(merged.merge(params2)).thenReturn(merged2);
//        //just for the fun, we add two other CallParams that are mergeable
//        CallParams params4 = mock(CallParams.class);
//        CallParams params5 = mock(CallParams.class);
//        CallParams merged3 = mock(CallParams.class);
//        //we do not make them reciprocally mergeable, as we now thanks to the List order 
//        //that params4 will first be examined.
//        when(params4.merge(params5)).thenReturn(merged3);
//        allParams.add(params4);
//        allParams.add(params5);
//        mergedParams = CallParams.merge(allParams);
//        //check that the merged occurred 
//        assertTrue("Incorrect merged Set returned", mergedParams.size() == 2 && 
//                mergedParams.contains(merged2) && mergedParams.contains(merged3));
//    }
//    
//    /**
//     * Test the method {@link CallParams#canMerge(CallParams)}
//     */
//    @Test
//    public void testCanMerge() {
//        CallParams params = new FakeCallParams();
//        CallParams paramsToCompare = new FakeCallParams();
//        String id = "testId";
//        
//        //as long as one of the CallParams has no parameters, whatever the parameters 
//        //of the other CallParams, the merge should be accepted (one of the CallParams 
//        //requests any data, so the data queried by the other CallParams will always be 
//        //a subset)
//        params.setAllDataTypes(true);
//        params.setAffymetrixData(DataState.LOWQUALITY);
//        params.setESTData(DataState.LOWQUALITY);
//        assertTrue("Incorrect boolean returned by canMerge", 
//                params.canMerge(paramsToCompare));
//        
//        //add the same DataStates to the other CallParams
//        paramsToCompare.setAffymetrixData(DataState.LOWQUALITY);
//        paramsToCompare.setESTData(DataState.LOWQUALITY);
//        //the merge should be refused, allDataTypes is different
//        assertFalse("Incorrect boolean returned by canMerge", 
//                params.canMerge(paramsToCompare));
//        //setting allDataTypes should make it work
//        paramsToCompare.setAllDataTypes(true);
//        assertTrue("Incorrect boolean returned by canMerge", 
//                params.canMerge(paramsToCompare));
//        //making their DataStates different should make it fail again
//        paramsToCompare.setInSituData(DataState.LOWQUALITY);
//        assertFalse("Incorrect boolean returned by canMerge", 
//                params.canMerge(paramsToCompare));
//        paramsToCompare.setInSituData(DataState.NODATA);
//        
//        //A CallParams with allDataTypes to true with less than 2 data types set 
//        //(so, equivalent to false) should be mergeable with another CallParams 
//        //with allDataTypes false
//        paramsToCompare.setAllDataTypes(false);
//        params.setESTData(DataState.NODATA);
//        assertTrue("Incorrect boolean returned by canMerge", 
//                params.canMerge(paramsToCompare));
//        
//        //if we add a filter on some entity IDs, they should not be mergeble 
//        //as their DataStates differ
//        params.addAnatEntityId(id);
//        assertFalse("Incorrect boolean returned by canMerge", 
//                params.canMerge(paramsToCompare));
//        //if we make their DataStates equal, it should be mergeable
//        params.setESTData(DataState.LOWQUALITY);
//        params.setAllDataTypes(false);
//        assertTrue("Incorrect boolean returned by canMerge", 
//                params.canMerge(paramsToCompare));
//        //but if we had yet another different filter, it is not mergeable anymore
//        paramsToCompare.addStageId(id);
//        assertFalse("Incorrect boolean returned by canMerge", 
//                params.canMerge(paramsToCompare));
//        
//        //now, let's make their filter identical
//        params.addStageId(id);
//        paramsToCompare.addAnatEntityId(id);
//        //it should be possible to merge them even if their DataStates are slightly 
//        //different (as long as they are consecutive)
//        params.setESTData(DataState.NODATA);
//        paramsToCompare.setESTData(DataState.LOWQUALITY);
//        assertTrue("Incorrect boolean returned by canMerge", 
//                params.canMerge(paramsToCompare));
//        //if not consecutive, it should not work
//        paramsToCompare.setESTData(DataState.HIGHQUALITY);
//        assertFalse("Incorrect boolean returned by canMerge", 
//                params.canMerge(paramsToCompare));
//        
//        //now, we make their DataStates identical, but change the way the filters 
//        //are used, they should not be mergeable
//        paramsToCompare.setESTData(DataState.NODATA);
//        //just to be sure we are in the proper state
//        assertTrue("Incorrect boolean returned by canMerge", 
//                params.canMerge(paramsToCompare));
//        //actual test
//        params.setUseAnatDescendants(true);
//        assertFalse("Incorrect boolean returned by canMerge", 
//                params.canMerge(paramsToCompare));
//        //if we remove the anatEntity filter, useAnatDescendants will not be used, 
//        //there will be only one difference between the CallParams, they should be 
//        //mergeable again
//        params.clearAnatEntityIds();
//        assertTrue("Incorrect boolean returned by canMerge", 
//                params.canMerge(paramsToCompare));
//    }
//    
//    /**
//     * Test the functionality of {@link CallParams#merge(CallParams, CallParams)}.
//     */
//    @Test
//    public void shouldMerge() {
//        CallParams params = new FakeCallParams();
//        CallParams paramsToCompare = new FakeCallParams();
//        CallParams mergedParams = new FakeCallParams();
//        String id1 = "testId";
//        String id2 = "testId2";
//        
//        //as long as one of the CallParams has no parameters, whatever the parameters 
//        //of the other CallParams, the merged CallParams should have no parameters as well 
//        //(one of the CallParams requests any data, so the data queried by the other 
//        //CallParams will always be a subset)
//        params.setAllDataTypes(true);
//        params.setAffymetrixData(DataState.LOWQUALITY);
//        params.setESTData(DataState.LOWQUALITY);
//        params.merge(paramsToCompare, mergedParams);
//        assertFalse("Incorrect resulting merged CallParameters", 
//                mergedParams.isAllDataTypes());
//        assertEquals("Incorrect resulting merged CallParameters", DataState.NODATA, 
//                mergedParams.getAffymetrixData());
//        assertEquals("Incorrect resulting merged CallParameters", DataState.NODATA, 
//                mergedParams.getESTData());
//        
//        
//        //we add a parameter to paramsToCompare, so that a merge actually occurs
//        //here we do not test whether the merge is meaningful, only if it is 
//        //correctly done.
//        params.setAllDataTypes(true);
//        paramsToCompare.setAffymetrixData(DataState.LOWQUALITY);
//        params.setAffymetrixData(DataState.HIGHQUALITY);
//        paramsToCompare.setESTData(DataState.LOWQUALITY);
//        params.setESTData(DataState.NODATA);
//        paramsToCompare.setInSituData(DataState.HIGHQUALITY);
//        params.setInSituData(DataState.HIGHQUALITY);
//        paramsToCompare.setRelaxedInSituData(DataState.LOWQUALITY);
//        params.setRelaxedInSituData(DataState.LOWQUALITY);
//        paramsToCompare.setRNASeqData(DataState.HIGHQUALITY);
//        params.setRNASeqData(DataState.NODATA);
//        //the merge between the ID filters only occurs if they are both non-empty.
//        //same for their associated boolean
//        params.addAnatEntityId(id1);
//        params.setUseAnatDescendants(true);
//        params.addStageId(id1);
//        paramsToCompare.addStageId(id2);
//        params.setUseDevDescendants(true);
//        params.addGeneId(id1);
//        paramsToCompare.addGeneId(id1);
//        params.addSpeciesId(id1);
//        paramsToCompare.addSpeciesId(id1);
//        
//        mergedParams = new FakeCallParams();
//        params.merge(paramsToCompare, mergedParams);
//        assertTrue("Incorrect resulting merged CallParameters", 
//                mergedParams.isAllDataTypes());
//        assertEquals("Incorrect resulting merged CallParameters", DataState.LOWQUALITY, 
//                mergedParams.getAffymetrixData());
//        assertEquals("Incorrect resulting merged CallParameters", DataState.NODATA, 
//                mergedParams.getESTData());
//        assertEquals("Incorrect resulting merged CallParameters", DataState.HIGHQUALITY, 
//                mergedParams.getInSituData());
//        assertEquals("Incorrect resulting merged CallParameters", DataState.LOWQUALITY, 
//                mergedParams.getRelaxedInSituData());
//        assertEquals("Incorrect resulting merged CallParameters", DataState.NODATA, 
//                mergedParams.getRNASeqData());
//        Set<String> oneElement = new HashSet<String>();
//        oneElement.add(id1);
//        Set<String> twoElements = new HashSet<String>(oneElement);
//        twoElements.add(id2);
//        assertEquals("Incorrect resulting merged CallParameters", new HashSet<String>(), 
//                mergedParams.getAnatEntityIds());
//        assertFalse("Incorrect resulting merged CallParameters", 
//                mergedParams.isUseAnatDescendants());
//        assertEquals("Incorrect resulting merged CallParameters", twoElements, 
//                mergedParams.getStageIds());
//        assertTrue("Incorrect resulting merged CallParameters", 
//                mergedParams.isUseDevDescendants());
//        assertEquals("Incorrect resulting merged CallParameters", oneElement, 
//                mergedParams.getGeneIds());
//    }
//    
//    /**
//     * Test the method {@link CallParams#getDifferentParametersCount(CallParams)}
//     */
//    @Test
//    public void shouldGetDifferentParametersCount() {
//        
//        CallParams params = new FakeCallParams();
//        CallParams paramsToCompare = new FakeCallParams();
//        String id = "testId";
//        assertEquals("incorrect number of parameters that are different", 0, 
//                params.getDifferentParametersCount(paramsToCompare));
//        
//        params.addAnatEntityId(id);
//        assertEquals("incorrect number of parameters that are different", 1, 
//                params.getDifferentParametersCount(paramsToCompare));
//        
//        params.addStageId(id);
//        assertEquals("incorrect number of parameters that are different", 2, 
//                params.getDifferentParametersCount(paramsToCompare));
//        
//        params.addGeneId(id);
//        assertEquals("incorrect number of parameters that are different", 3, 
//                params.getDifferentParametersCount(paramsToCompare));
//
//        params.addSpeciesId(id);
//        assertEquals("incorrect number of parameters that are different", 4, 
//                params.getDifferentParametersCount(paramsToCompare));
//
//        params.setUseAnatDescendants(true);
//        assertEquals("incorrect number of parameters that are different", 5, 
//                params.getDifferentParametersCount(paramsToCompare));
//        
//        params.setUseDevDescendants(true);
//        assertEquals("incorrect number of parameters that are different", 6, 
//                params.getDifferentParametersCount(paramsToCompare));
//        
//        params.setAllDataTypes(true);
//        assertEquals("incorrect number of parameters that are different", 6, 
//                params.getDifferentParametersCount(paramsToCompare));
//        //allDataTypes is considered only if at least 2 data types are set
//        params.setESTData(DataState.LOWQUALITY);
//        params.setAffymetrixData(DataState.LOWQUALITY);
//        assertEquals("incorrect number of parameters that are different", 7, 
//                params.getDifferentParametersCount(paramsToCompare));
//        
//        
//        
//        
//        paramsToCompare.addAnatEntityId(id);
//        assertEquals("incorrect number of parameters that are different", 6, 
//                params.getDifferentParametersCount(paramsToCompare));
//        
//        paramsToCompare.addStageId(id);
//        assertEquals("incorrect number of parameters that are different", 5, 
//                params.getDifferentParametersCount(paramsToCompare));
//        
//        paramsToCompare.addGeneId(id);
//        assertEquals("incorrect number of parameters that are different", 4, 
//                params.getDifferentParametersCount(paramsToCompare));
//
//        paramsToCompare.addSpeciesId(id);
//        assertEquals("incorrect number of parameters that are different", 3, 
//                params.getDifferentParametersCount(paramsToCompare));
//
//        paramsToCompare.setUseAnatDescendants(true);
//        assertEquals("incorrect number of parameters that are different", 2, 
//                params.getDifferentParametersCount(paramsToCompare));
//        
//        paramsToCompare.setUseDevDescendants(true);
//        assertEquals("incorrect number of parameters that are different", 1, 
//                params.getDifferentParametersCount(paramsToCompare));
//        
//        paramsToCompare.setAllDataTypes(true);
//        assertEquals("incorrect number of parameters that are different", 1, 
//                params.getDifferentParametersCount(paramsToCompare));
//        //allDataTypes is considered only if at least 2 data types are set
//        paramsToCompare.setESTData(DataState.LOWQUALITY);
//        paramsToCompare.setAffymetrixData(DataState.LOWQUALITY);
//        assertEquals("incorrect number of parameters that are different", 0, 
//                params.getDifferentParametersCount(paramsToCompare));
//    }
//    
//    /**
//     * Test the private method {@link CallParams#getDataTypesSetCount()}.
//     */
//    @Test
//    public void shouldGetDataTypesSetCount() throws NoSuchMethodException, 
//        SecurityException, IllegalAccessException, IllegalArgumentException, 
//        InvocationTargetException {
//        //in order to test private method
//        Method method = CallParams.class.getDeclaredMethod("getDataTypesSetCount", 
//                (Class<?>[]) null);
//        method.setAccessible(true);
//
//        CallParams params = new FakeCallParams();
//
//        assertEquals("Incorrect number of data types set.", 0, 
//                method.invoke(params, (Object[]) null));
//        
//        params.setAffymetrixData(DataState.LOWQUALITY);
//        assertEquals("Incorrect number of data types set.", 1, 
//                method.invoke(params, (Object[]) null));
//
//        params.setESTData(DataState.LOWQUALITY);
//        assertEquals("Incorrect number of data types set.", 2, 
//                method.invoke(params, (Object[]) null));
//
//        params.setInSituData(DataState.HIGHQUALITY);
//        assertEquals("Incorrect number of data types set.", 3, 
//                method.invoke(params, (Object[]) null));
//
//        params.setRelaxedInSituData(DataState.HIGHQUALITY);
//        assertEquals("Incorrect number of data types set.", 4, 
//                method.invoke(params, (Object[]) null));
//
//        params.setRNASeqData(DataState.LOWQUALITY);
//        assertEquals("Incorrect number of data types set.", 5, 
//                method.invoke(params, (Object[]) null));
//
//        params.setAffymetrixData(null);
//        assertEquals("Incorrect number of data types set.", 4, 
//                method.invoke(params, (Object[]) null));
//
//        params.setESTData(DataState.NODATA);
//        assertEquals("Incorrect number of data types set.", 3, 
//                method.invoke(params, (Object[]) null));
//        
//    }
//    
//    /**
//     * Test the method {CallParams#hasDataRestrictions()}.
//     */
//    @Test
//    public void testHasDataRestrictions() {
//        
//        CallParams params = new FakeCallParams();
//        assertFalse("hasDataRestriction returned true, while no parameter was set", 
//                params.hasDataRestrictions());
//        
//        
//        params.setAllDataTypes(true);
//        //as long as there are no DataStates provided for a data type, 
//        //the parameter allDataTypes do nothing.
//        assertFalse("hasDataRestriction returned true, while no parameter was set", 
//                params.hasDataRestrictions());
//    
//        //---------- plays with the ID filters individually ---------------
//        params.addAnatEntityId("test");
//        assertTrue("hasDataRestriction returned false, while a parameter was set", 
//                params.hasDataRestrictions());
//        params.clearAnatEntityIds();
//        assertFalse("hasDataRestriction returned true, while no parameter was set", 
//                params.hasDataRestrictions());
//    
//        params.addStageId("test");
//        assertTrue("hasDataRestriction returned false, while a parameter was set", 
//                params.hasDataRestrictions());
//        params.clearStageIds();
//        assertFalse("hasDataRestriction returned true, while no parameter was set", 
//                params.hasDataRestrictions());
//    
//        params.addGeneId("test");
//        assertTrue("hasDataRestriction returned false, while a parameter was set", 
//                params.hasDataRestrictions());
//        params.clearGeneIds();
//        assertFalse("hasDataRestriction returned true, while no parameter was set", 
//                params.hasDataRestrictions());
//
//        params.addSpeciesId("test");
//        assertTrue("hasDataRestriction returned false, while a parameter was set", 
//                params.hasDataRestrictions());
//        params.clearSpeciesIds();
//        assertFalse("hasDataRestriction returned true, while no parameter was set", 
//                params.hasDataRestrictions());
//    
//        //---------- plays with each data type individually ---------------
//        
//        DataState dataState = DataState.HIGHQUALITY;
//        DataState noDataState = DataState.NODATA;
//        
//        params.setAffymetrixData(dataState);
//        assertTrue("hasDataRestriction returned false, while a parameter was set", 
//                params.hasDataRestrictions());
//        params.setAffymetrixData(null);
//        assertFalse("hasDataRestriction returned true, while no parameter was set", 
//                params.hasDataRestrictions());
//        params.setAffymetrixData(noDataState);
//        assertFalse("hasDataRestriction returned true, while no parameter was set", 
//                params.hasDataRestrictions());
//        
//        params.setESTData(dataState);
//        assertTrue("hasDataRestriction returned false, while a parameter was set", 
//                params.hasDataRestrictions());
//        params.setESTData(null);
//        assertFalse("hasDataRestriction returned true, while no parameter was set", 
//                params.hasDataRestrictions());
//        params.setESTData(noDataState);
//        assertFalse("hasDataRestriction returned true, while no parameter was set", 
//                params.hasDataRestrictions());
//        
//        params.setInSituData(dataState);
//        assertTrue("hasDataRestriction returned false, while a parameter was set", 
//                params.hasDataRestrictions());
//        params.setInSituData(null);
//        assertFalse("hasDataRestriction returned true, while no parameter was set", 
//                params.hasDataRestrictions());
//        params.setInSituData(noDataState);
//        assertFalse("hasDataRestriction returned true, while no parameter was set", 
//                params.hasDataRestrictions());
//        
//        params.setRelaxedInSituData(dataState);
//        assertTrue("hasDataRestriction returned false, while a parameter was set", 
//                params.hasDataRestrictions());
//        params.setRelaxedInSituData(null);
//        assertFalse("hasDataRestriction returned true, while no parameter was set", 
//                params.hasDataRestrictions());
//        params.setRelaxedInSituData(noDataState);
//        assertFalse("hasDataRestriction returned true, while no parameter was set", 
//                params.hasDataRestrictions());
//        
//        params.setRNASeqData(dataState);
//        assertTrue("hasDataRestriction returned false, while a parameter was set", 
//                params.hasDataRestrictions());
//        params.setRNASeqData(null);
//        assertFalse("hasDataRestriction returned true, while no parameter was set", 
//                params.hasDataRestrictions());
//        params.setRNASeqData(noDataState);
//        assertFalse("hasDataRestriction returned true, while no parameter was set", 
//                params.hasDataRestrictions());
//    
//        
//        //---------- plays with several parameters at the same time ---------------
//        params.addAnatEntityId("test");
//        params.addStageId("test2");
//        params.addGeneId("test3");
//        params.addSpeciesId("test4");
//        params.setRNASeqData(dataState);
//        params.setRelaxedInSituData(dataState);
//        assertTrue("hasDataRestriction returned false, while a parameter was set", 
//                params.hasDataRestrictions());
//    }
//
//    /**
//     * Test the private method {@link CallParams#hasSameDataStates(CallParams)}.
//     */
//    @Test
//    public void testHasSameDataStates() throws NoSuchMethodException, 
//        SecurityException, IllegalAccessException, IllegalArgumentException, 
//        InvocationTargetException {
//        //in order to test private method
//        Method method = CallParams.class.getDeclaredMethod("hasSameDataStates", 
//                CallParams.class);
//        method.setAccessible(true);
//
//        CallParams params = new FakeCallParams();
//        CallParams paramsToCompare = new FakeCallParams();
//        
//        assertTrue("Incorrect boolean returned by hasSameDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
// 
//        params.setAffymetrixData(DataState.LOWQUALITY);
//        assertFalse("Incorrect boolean returned by hasSameDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//        paramsToCompare.setAffymetrixData(DataState.HIGHQUALITY);
//        assertFalse("Incorrect boolean returned by hasSameDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//        params.setAffymetrixData(DataState.HIGHQUALITY);
//        assertTrue("Incorrect boolean returned by hasSameDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
// 
//        params.setESTData(DataState.LOWQUALITY);
//        assertFalse("Incorrect boolean returned by hasSameDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//        paramsToCompare.setESTData(DataState.HIGHQUALITY);
//        assertFalse("Incorrect boolean returned by hasSameDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//        params.setESTData(DataState.HIGHQUALITY);
//        assertTrue("Incorrect boolean returned by hasSameDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
// 
//        params.setInSituData(DataState.LOWQUALITY);
//        assertFalse("Incorrect boolean returned by hasSameDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//        paramsToCompare.setInSituData(DataState.HIGHQUALITY);
//        assertFalse("Incorrect boolean returned by hasSameDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//        params.setInSituData(DataState.HIGHQUALITY);
//        assertTrue("Incorrect boolean returned by hasSameDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
// 
//        params.setRelaxedInSituData(DataState.LOWQUALITY);
//        assertFalse("Incorrect boolean returned by hasSameDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//        paramsToCompare.setRelaxedInSituData(DataState.HIGHQUALITY);
//        assertFalse("Incorrect boolean returned by hasSameDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//        params.setRelaxedInSituData(DataState.HIGHQUALITY);
//        assertTrue("Incorrect boolean returned by hasSameDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
// 
//        params.setRNASeqData(DataState.LOWQUALITY);
//        assertFalse("Incorrect boolean returned by hasSameDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//        paramsToCompare.setRNASeqData(DataState.HIGHQUALITY);
//        assertFalse("Incorrect boolean returned by hasSameDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//        params.setRNASeqData(DataState.HIGHQUALITY);
//        assertTrue("Incorrect boolean returned by hasSameDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//        
//
//
//        params.setAffymetrixData(DataState.NODATA);
//        paramsToCompare.setAffymetrixData(null);
//        assertTrue("Incorrect boolean returned by hasSameDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//        
//        params.setESTData(DataState.NODATA);
//        paramsToCompare.setESTData(null);
//        assertTrue("Incorrect boolean returned by hasSameDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//        
//        params.setInSituData(DataState.NODATA);
//        paramsToCompare.setInSituData(null);
//        assertTrue("Incorrect boolean returned by hasSameDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//        
//        params.setRelaxedInSituData(DataState.NODATA);
//        paramsToCompare.setRelaxedInSituData(null);
//        assertTrue("Incorrect boolean returned by hasSameDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//
//        params.setRNASeqData(DataState.NODATA);
//        paramsToCompare.setRNASeqData(null);
//        assertTrue("Incorrect boolean returned by hasSameDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//    }
//    
//    /**
//     * Test the private method {@link CallParams#hasCoherentDataStates(CallParams)}.
//     */
//    @Test
//    public void testHasCoherentDataStates() throws NoSuchMethodException, 
//        SecurityException, IllegalAccessException, IllegalArgumentException, 
//        InvocationTargetException {
//        //in order to test private method
//        Method method = CallParams.class.getDeclaredMethod("hasCoherentDataStates", 
//                CallParams.class);
//        method.setAccessible(true);
//
//        CallParams params = new FakeCallParams();
//        CallParams paramsToCompare = new FakeCallParams();
//        
//        assertTrue("Incorrect boolean returned by hasCoherentDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
// 
//        params.setAffymetrixData(DataState.LOWQUALITY);
//        assertTrue("Incorrect boolean returned by hasCoherentDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//        paramsToCompare.setAffymetrixData(DataState.HIGHQUALITY);
//        assertTrue("Incorrect boolean returned by hasCoherentDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//        params.setAffymetrixData(DataState.NODATA);
//        assertFalse("Incorrect boolean returned by hasCoherentDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//        params.setAffymetrixData(null);
//        assertFalse("Incorrect boolean returned by hasCoherentDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//        //to continue the test 
//        params.setAffymetrixData(DataState.HIGHQUALITY);
// 
//        params.setESTData(DataState.LOWQUALITY);
//        assertTrue("Incorrect boolean returned by hasCoherentDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//        paramsToCompare.setESTData(DataState.HIGHQUALITY);
//        assertTrue("Incorrect boolean returned by hasCoherentDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//        params.setESTData(DataState.NODATA);
//        assertFalse("Incorrect boolean returned by hasCoherentDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//        params.setESTData(null);
//        assertFalse("Incorrect boolean returned by hasCoherentDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//        //to continue the test 
//        params.setESTData(DataState.HIGHQUALITY);
// 
//        params.setInSituData(DataState.LOWQUALITY);
//        assertTrue("Incorrect boolean returned by hasCoherentDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//        paramsToCompare.setInSituData(DataState.HIGHQUALITY);
//        assertTrue("Incorrect boolean returned by hasCoherentDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//        params.setInSituData(DataState.NODATA);
//        assertFalse("Incorrect boolean returned by hasCoherentDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//        params.setInSituData(null);
//        assertFalse("Incorrect boolean returned by hasCoherentDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//        //to continue the test 
//        params.setInSituData(DataState.HIGHQUALITY);
// 
//        params.setRelaxedInSituData(DataState.LOWQUALITY);
//        assertTrue("Incorrect boolean returned by hasCoherentDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//        paramsToCompare.setRelaxedInSituData(DataState.HIGHQUALITY);
//        assertTrue("Incorrect boolean returned by hasCoherentDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//        params.setRelaxedInSituData(DataState.NODATA);
//        assertFalse("Incorrect boolean returned by hasCoherentDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//        params.setRelaxedInSituData(null);
//        assertFalse("Incorrect boolean returned by hasCoherentDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//        //to continue the test 
//        params.setRelaxedInSituData(DataState.HIGHQUALITY);
// 
//        params.setRNASeqData(DataState.LOWQUALITY);
//        assertTrue("Incorrect boolean returned by hasCoherentDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//        paramsToCompare.setRNASeqData(DataState.HIGHQUALITY);
//        assertTrue("Incorrect boolean returned by hasCoherentDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//        params.setRNASeqData(DataState.NODATA);
//        assertFalse("Incorrect boolean returned by hasCoherentDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//        params.setRNASeqData(null);
//        assertFalse("Incorrect boolean returned by hasCoherentDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//        //to continue the test 
//        params.setRNASeqData(DataState.HIGHQUALITY);
//        
//        //test several data types and situations at the same time
//        params = new FakeCallParams();
//        paramsToCompare = new FakeCallParams();
//        params.setAffymetrixData(DataState.HIGHQUALITY);
//        paramsToCompare.setAffymetrixData(DataState.HIGHQUALITY);
//        params.setESTData(DataState.HIGHQUALITY);
//        paramsToCompare.setESTData(DataState.LOWQUALITY);
//        params.setInSituData(DataState.LOWQUALITY);
//        paramsToCompare.setInSituData(DataState.NODATA);
//        params.setRelaxedInSituData(DataState.LOWQUALITY);
//        paramsToCompare.setRelaxedInSituData(null);
//        params.setRNASeqData(null);
//        paramsToCompare.setRNASeqData(null);
//        assertTrue("Incorrect boolean returned by hasCoherentDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//        
//        paramsToCompare.setRNASeqData(DataState.HIGHQUALITY);
//        assertFalse("Incorrect boolean returned by hasCoherentDataStates", 
//                (boolean) method.invoke(params, paramsToCompare));
//        
//    }
//    
//    /**
//     * Test the private method {@link CallParams#mergeDataStates(DataState, DataState)}.
//     */
//    @Test
//    public void shouldMergeDataStates() throws NoSuchMethodException, 
//        SecurityException, IllegalAccessException, IllegalArgumentException, 
//        InvocationTargetException {
//        //in order to test private method
//        Method method = CallParams.class.getDeclaredMethod("mergeDataStates", 
//                DataState.class, DataState.class);
//        method.setAccessible(true);
//        
//
//        assertEquals("Incorrect merged DataState", DataState.NODATA, 
//                method.invoke(null, null, null));
//        
//        assertEquals("Incorrect merged DataState", DataState.NODATA, 
//                method.invoke(null, DataState.NODATA, null));
//        
//        assertEquals("Incorrect merged DataState", DataState.NODATA, 
//                method.invoke(null, DataState.NODATA, DataState.LOWQUALITY));
//        
//        assertEquals("Incorrect merged DataState", DataState.NODATA, 
//                method.invoke(null, DataState.NODATA, DataState.HIGHQUALITY));
//        assertEquals("Incorrect merged DataState", DataState.NODATA, 
//                method.invoke(null, DataState.HIGHQUALITY, DataState.NODATA));
//        
//        assertEquals("Incorrect merged DataState", DataState.LOWQUALITY, 
//                method.invoke(null, DataState.LOWQUALITY, DataState.LOWQUALITY));
//        
//        assertEquals("Incorrect merged DataState", DataState.LOWQUALITY, 
//                method.invoke(null, DataState.LOWQUALITY, DataState.HIGHQUALITY));
//        
//        assertEquals("Incorrect merged DataState", DataState.HIGHQUALITY, 
//                method.invoke(null, DataState.HIGHQUALITY, DataState.HIGHQUALITY));
//    }
//    
//    /**
//     * Test the private method {@link CallParams#equivalent(DataState, DataState)}.
//     */
//    @Test
//    public void testEquivalent() throws NoSuchMethodException, 
//        SecurityException, IllegalAccessException, IllegalArgumentException, 
//        InvocationTargetException {
//        //in order to test private method
//        Method method = CallParams.class.getDeclaredMethod("equivalent", 
//                DataState.class, DataState.class);
//        method.setAccessible(true);
//        
//
//        assertTrue("Incorrect equivalence between DataStates", 
//                (boolean) method.invoke(null, DataState.NODATA, DataState.NODATA));
//        assertTrue("Incorrect equivalence between DataStates", 
//                (boolean) method.invoke(null, null, null));
//
//        assertTrue("Incorrect equivalence between DataStates", 
//                (boolean) method.invoke(null, DataState.NODATA, null));
//        assertTrue("Incorrect equivalence between DataStates", 
//                (boolean) method.invoke(null, null, DataState.NODATA));
//
//        assertTrue("Incorrect equivalence between DataStates", 
//                (boolean) method.invoke(null, DataState.LOWQUALITY, DataState.LOWQUALITY));
//        assertTrue("Incorrect equivalence between DataStates", 
//                (boolean) method.invoke(null, DataState.HIGHQUALITY, DataState.HIGHQUALITY));
//
//        assertFalse("Incorrect equivalence between DataStates", 
//                (boolean) method.invoke(null, DataState.LOWQUALITY, DataState.NODATA));
//        assertFalse("Incorrect equivalence between DataStates", 
//                (boolean) method.invoke(null, DataState.LOWQUALITY, DataState.HIGHQUALITY));
//        assertFalse("Incorrect equivalence between DataStates", 
//                (boolean) method.invoke(null, DataState.LOWQUALITY, null));
//        assertFalse("Incorrect equivalence between DataStates", 
//                (boolean) method.invoke(null, null, DataState.LOWQUALITY));
//
//        assertFalse("Incorrect equivalence between DataStates", 
//                (boolean) method.invoke(null, DataState.HIGHQUALITY, DataState.NODATA));
//        assertFalse("Incorrect equivalence between DataStates", 
//                (boolean) method.invoke(null, DataState.HIGHQUALITY, DataState.LOWQUALITY));
//        assertFalse("Incorrect equivalence between DataStates", 
//                (boolean) method.invoke(null, DataState.HIGHQUALITY, null));
//        assertFalse("Incorrect equivalence between DataStates", 
//                (boolean) method.invoke(null, null, DataState.HIGHQUALITY));
//    }
//    
//    /**
//     * Test the private method {@link CallParams#coherent(DataState, DataState)}.
//     */
//    @Test
//    public void testCoherent() throws NoSuchMethodException, 
//        SecurityException, IllegalAccessException, IllegalArgumentException, 
//        InvocationTargetException {
//        //in order to test private method
//        Method method = CallParams.class.getDeclaredMethod("coherent", 
//                DataState.class, DataState.class);
//        method.setAccessible(true);
//        
//
//        assertTrue("Incorrect coherence between DataStates", 
//                (boolean) method.invoke(null, DataState.NODATA, DataState.NODATA));
//        assertTrue("Incorrect coherence between DataStates", 
//                (boolean) method.invoke(null, null, null));
//
//        assertTrue("Incorrect coherence between DataStates", 
//                (boolean) method.invoke(null, DataState.NODATA, null));
//        assertTrue("Incorrect coherence between DataStates", 
//                (boolean) method.invoke(null, null, DataState.NODATA));
//
//        assertTrue("Incorrect coherence between DataStates", 
//                (boolean) method.invoke(null, DataState.NODATA, DataState.LOWQUALITY));
//        assertTrue("Incorrect coherence between DataStates", 
//                (boolean) method.invoke(null, null, DataState.LOWQUALITY));
//        assertTrue("Incorrect coherence between DataStates", 
//                (boolean) method.invoke(null, DataState.LOWQUALITY, DataState.NODATA));
//        assertTrue("Incorrect coherence between DataStates", 
//                (boolean) method.invoke(null, DataState.LOWQUALITY, null));
//
//        assertTrue("Incorrect coherence between DataStates", 
//                (boolean) method.invoke(null, DataState.LOWQUALITY, DataState.HIGHQUALITY));
//        assertTrue("Incorrect coherence between DataStates", 
//                (boolean) method.invoke(null, DataState.HIGHQUALITY, DataState.LOWQUALITY));
//
//        assertFalse("Incorrect coherence between DataStates", 
//                (boolean) method.invoke(null, DataState.NODATA, DataState.HIGHQUALITY));
//        assertFalse("Incorrect coherence between DataStates", 
//                (boolean) method.invoke(null, DataState.HIGHQUALITY, DataState.NODATA));
//        assertFalse("Incorrect coherence between DataStates", 
//                (boolean) method.invoke(null, null, DataState.HIGHQUALITY));
//        assertFalse("Incorrect coherence between DataStates", 
//                (boolean) method.invoke(null, DataState.HIGHQUALITY, null));
//    }
//    
    /**
     * Simply test the getters and setters of {@code CallParams}.
     */
    @Test
    public void shouldSetGet() {
        CallParams params = new FakeCallParams();
        
        
        params.setAllDataTypes(true);
        assertTrue("Incorrect allDataTypes set/get", params.isAllDataTypes());
        params.setAllDataTypes(false);

        params.setUseAnatDescendants(true);
        assertTrue("Incorrect useAnatDescendants set/get", params.isUseAnatDescendants());
        params.setUseAnatDescendants(false);

        params.setUseDevDescendants(true);
        assertTrue("Incorrect useDevDescendants set/get", params.isUseDevDescendants());
        params.setUseDevDescendants(false);
        
        
        
        Set<String> twoElementSet = new HashSet<>();
        twoElementSet.add("ID1");
        twoElementSet.add("ID2");
        Set<Integer> twoIntElementSet = new HashSet<>();
        twoIntElementSet.add(1);
        twoIntElementSet.add(2);

        String thirdElement = "ID3";
        Set<String> threeElementSet = new HashSet<>(twoElementSet);
        threeElementSet.add(thirdElement);
        
        Integer thirdIntElement = 3;
        Set<Integer> threeIntElementSet = new HashSet<>(twoIntElementSet);
        threeIntElementSet.add(thirdIntElement);

        params.addAllAnatEntityIds(twoElementSet);
        assertEquals("Incorrect anatEntityIds set/get", twoElementSet, 
                params.getAnatEntityIds());
        params.addAnatEntityId(thirdElement);
        assertEquals("Incorrect anatEntityIds set/get", threeElementSet, 
                params.getAnatEntityIds());
        params.clearAnatEntityIds();
        assertEquals("Incorrect anatEntityIds set/get", new HashSet<String>(), 
                params.getAnatEntityIds());
        
        params.addAllStageIds(twoElementSet);
        assertEquals("Incorrect stageIds set/get", twoElementSet, 
                params.getStageIds());
        params.addStageId(thirdElement);
        assertEquals("Incorrect stageIds set/get", threeElementSet, 
                params.getStageIds());
        params.clearStageIds();
        assertEquals("Incorrect stageIds set/get", new HashSet<String>(), 
                params.getStageIds());
        
        params.addAllGeneIds(twoElementSet);
        assertEquals("Incorrect geneIds set/get", twoElementSet, 
                params.getGeneIds());
        params.addGeneId(thirdElement);
        assertEquals("Incorrect geneIds set/get", threeElementSet, 
                params.getGeneIds());
        params.clearGeneIds();
        assertEquals("Incorrect geneId set/get", new HashSet<String>(), 
                params.getGeneIds());
        
        params.addAllSpeciesIds(twoIntElementSet);
        assertEquals("Incorrect species set/get", twoIntElementSet, 
                params.getSpeciesIds());
        params.addSpeciesId(thirdIntElement);
        assertEquals("Incorrect species set/get", threeIntElementSet, 
                params.getSpeciesIds());
        params.clearSpeciesIds();
        assertEquals("Incorrect geneId set/get", new HashSet<Integer>(), 
                params.getSpeciesIds());

        
        DataState state = DataState.HIGHQUALITY;
        
        params.setAffymetrixData(state);
        assertEquals("Incorrect Affymetrix DataState set/get", state, params.getAffymetrixData());
        //to be sure to not interfere with other setters in case of bug
        params.setAffymetrixData(null);

        params.setESTData(state);
        assertEquals("Incorrect EST DataState set/get", state, params.getESTData());
        //to be sure to not interfere with other setters in case of bug
        params.setESTData(null);

        params.setInSituData(state);
        assertEquals("Incorrect in situ DataState set/get", state, params.getInSituData());
        //to be sure to not interfere with other setters in case of bug
        params.setInSituData(null);

        params.setRelaxedInSituData(state);
        assertEquals("Incorrect relaxed in situ DataState set/get", state, 
                params.getRelaxedInSituData());
        //to be sure to not interfere with other setters in case of bug
        params.setRelaxedInSituData(null);

        params.setRNASeqData(state);
        assertEquals("Incorrect RNA-Seq DataState set/get", state, params.getRNASeqData());
        //to be sure to not interfere with other setters in case of bug
        params.setRNASeqData(null);
    }
 }
