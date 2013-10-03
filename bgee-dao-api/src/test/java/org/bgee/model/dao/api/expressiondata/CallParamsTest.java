package org.bgee.model.dao.api.expressiondata;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TestAncestor;
import org.bgee.model.dao.api.expressiondata.CallTO.DataState;
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
        @Override
        protected CallParams merge(CallParams paramsToMerge) {
            CallParams newParams = new FakeCallParams();
            super.merge(paramsToMerge, newParams);
            return newParams;
        }
    }
    
    /**
     * Test the method {@link CallParams#getDifferentParametersCount(CallParams)}
     */
    @Test
    public void shouldGetDifferentParametersCount() {
        
        CallParams params = new FakeCallParams();
        CallParams paramsToCompare = new FakeCallParams();
        String id = "testId";
        assertEquals("incorrect number of parameters that are different", 0, 
                params.getDifferentParametersCount(paramsToCompare));
        
        params.addAnatEntityId(id);
        assertEquals("incorrect number of parameters that are different", 1, 
                params.getDifferentParametersCount(paramsToCompare));
        
        params.addDevStageId(id);
        assertEquals("incorrect number of parameters that are different", 2, 
                params.getDifferentParametersCount(paramsToCompare));
        
        params.addGeneId(id);
        assertEquals("incorrect number of parameters that are different", 3, 
                params.getDifferentParametersCount(paramsToCompare));
        
        params.setUseAnatDescendants(true);
        assertEquals("incorrect number of parameters that are different", 4, 
                params.getDifferentParametersCount(paramsToCompare));
        
        params.setUseDevDescendants(true);
        assertEquals("incorrect number of parameters that are different", 5, 
                params.getDifferentParametersCount(paramsToCompare));
        
        params.setAllDataTypes(true);
        assertEquals("incorrect number of parameters that are different", 5, 
                params.getDifferentParametersCount(paramsToCompare));
        //allDataTypes is considered only if at least 2 data types are set
        params.setESTData(DataState.LOWQUALITY);
        params.setAffymetrixData(DataState.LOWQUALITY);
        assertEquals("incorrect number of parameters that are different", 6, 
                params.getDifferentParametersCount(paramsToCompare));
        
        
        
        
        paramsToCompare.addAnatEntityId(id);
        assertEquals("incorrect number of parameters that are different", 5, 
                params.getDifferentParametersCount(paramsToCompare));
        
        paramsToCompare.addDevStageId(id);
        assertEquals("incorrect number of parameters that are different", 4, 
                params.getDifferentParametersCount(paramsToCompare));
        
        paramsToCompare.addGeneId(id);
        assertEquals("incorrect number of parameters that are different", 3, 
                params.getDifferentParametersCount(paramsToCompare));
        
        paramsToCompare.setUseAnatDescendants(true);
        assertEquals("incorrect number of parameters that are different", 2, 
                params.getDifferentParametersCount(paramsToCompare));
        
        paramsToCompare.setUseDevDescendants(true);
        assertEquals("incorrect number of parameters that are different", 1, 
                params.getDifferentParametersCount(paramsToCompare));
        
        paramsToCompare.setAllDataTypes(true);
        assertEquals("incorrect number of parameters that are different", 1, 
                params.getDifferentParametersCount(paramsToCompare));
        //allDataTypes is considered only if at least 2 data types are set
        paramsToCompare.setESTData(DataState.LOWQUALITY);
        paramsToCompare.setAffymetrixData(DataState.LOWQUALITY);
        assertEquals("incorrect number of parameters that are different", 0, 
                params.getDifferentParametersCount(paramsToCompare));
    }
    
    /**
     * Test the private method {@link CallParams#getDataTypesSetCount()}.
     */
    @Test
    public void shouldGetDataTypesSetCount() throws NoSuchMethodException, 
        SecurityException, IllegalAccessException, IllegalArgumentException, 
        InvocationTargetException {
        //in order to test private method
        Method method = CallParams.class.getDeclaredMethod("getDataTypesSetCount", 
                (Class<?>[]) null);
        method.setAccessible(true);

        CallParams params = new FakeCallParams();

        assertEquals("Incorrect number of data types set.", 0, 
                method.invoke(params, (Object[]) null));
        
        params.setAffymetrixData(DataState.LOWQUALITY);
        assertEquals("Incorrect number of data types set.", 1, 
                method.invoke(params, (Object[]) null));

        params.setESTData(DataState.LOWQUALITY);
        assertEquals("Incorrect number of data types set.", 2, 
                method.invoke(params, (Object[]) null));

        params.setInSituData(DataState.HIGHQUALITY);
        assertEquals("Incorrect number of data types set.", 3, 
                method.invoke(params, (Object[]) null));

        params.setRelaxedInSituData(DataState.HIGHQUALITY);
        assertEquals("Incorrect number of data types set.", 4, 
                method.invoke(params, (Object[]) null));

        params.setRNASeqData(DataState.LOWQUALITY);
        assertEquals("Incorrect number of data types set.", 5, 
                method.invoke(params, (Object[]) null));

        params.setAffymetrixData(null);
        assertEquals("Incorrect number of data types set.", 4, 
                method.invoke(params, (Object[]) null));

        params.setESTData(DataState.NODATA);
        assertEquals("Incorrect number of data types set.", 3, 
                method.invoke(params, (Object[]) null));
        
    }
    
    /**
     * Test the private method {@link CallParams#hasSameDataStates(CallParams)}.
     */
    @Test
    public void testHasSameDataStates() throws NoSuchMethodException, 
        SecurityException, IllegalAccessException, IllegalArgumentException, 
        InvocationTargetException {
        //in order to test private method
        Method method = CallParams.class.getDeclaredMethod("hasSameDataStates", 
                CallParams.class);
        method.setAccessible(true);

        CallParams params = new FakeCallParams();
        CallParams paramsToCompare = new FakeCallParams();
        
        assertTrue("Incorrect boolean returned by hasSameDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
 
        params.setAffymetrixData(DataState.LOWQUALITY);
        assertFalse("Incorrect boolean returned by hasSameDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
        paramsToCompare.setAffymetrixData(DataState.HIGHQUALITY);
        assertFalse("Incorrect boolean returned by hasSameDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
        params.setAffymetrixData(DataState.HIGHQUALITY);
        assertTrue("Incorrect boolean returned by hasSameDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
 
        params.setESTData(DataState.LOWQUALITY);
        assertFalse("Incorrect boolean returned by hasSameDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
        paramsToCompare.setESTData(DataState.HIGHQUALITY);
        assertFalse("Incorrect boolean returned by hasSameDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
        params.setESTData(DataState.HIGHQUALITY);
        assertTrue("Incorrect boolean returned by hasSameDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
 
        params.setInSituData(DataState.LOWQUALITY);
        assertFalse("Incorrect boolean returned by hasSameDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
        paramsToCompare.setInSituData(DataState.HIGHQUALITY);
        assertFalse("Incorrect boolean returned by hasSameDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
        params.setInSituData(DataState.HIGHQUALITY);
        assertTrue("Incorrect boolean returned by hasSameDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
 
        params.setRelaxedInSituData(DataState.LOWQUALITY);
        assertFalse("Incorrect boolean returned by hasSameDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
        paramsToCompare.setRelaxedInSituData(DataState.HIGHQUALITY);
        assertFalse("Incorrect boolean returned by hasSameDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
        params.setRelaxedInSituData(DataState.HIGHQUALITY);
        assertTrue("Incorrect boolean returned by hasSameDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
 
        params.setRNASeqData(DataState.LOWQUALITY);
        assertFalse("Incorrect boolean returned by hasSameDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
        paramsToCompare.setRNASeqData(DataState.HIGHQUALITY);
        assertFalse("Incorrect boolean returned by hasSameDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
        params.setRNASeqData(DataState.HIGHQUALITY);
        assertTrue("Incorrect boolean returned by hasSameDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
        


        params.setAffymetrixData(DataState.NODATA);
        paramsToCompare.setAffymetrixData(null);
        assertTrue("Incorrect boolean returned by hasSameDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
        
        params.setESTData(DataState.NODATA);
        paramsToCompare.setESTData(null);
        assertTrue("Incorrect boolean returned by hasSameDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
        
        params.setInSituData(DataState.NODATA);
        paramsToCompare.setInSituData(null);
        assertTrue("Incorrect boolean returned by hasSameDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
        
        params.setRelaxedInSituData(DataState.NODATA);
        paramsToCompare.setRelaxedInSituData(null);
        assertTrue("Incorrect boolean returned by hasSameDataStates", 
                (boolean) method.invoke(params, paramsToCompare));

        params.setRNASeqData(DataState.NODATA);
        paramsToCompare.setRNASeqData(null);
        assertTrue("Incorrect boolean returned by hasSameDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
    }
    
    /**
     * Test the private method {@link CallParams#hasCoherentDataStates(CallParams)}.
     */
    @Test
    public void testHasCoherentDataStates() throws NoSuchMethodException, 
        SecurityException, IllegalAccessException, IllegalArgumentException, 
        InvocationTargetException {
        //in order to test private method
        Method method = CallParams.class.getDeclaredMethod("hasCoherentDataStates", 
                CallParams.class);
        method.setAccessible(true);

        CallParams params = new FakeCallParams();
        CallParams paramsToCompare = new FakeCallParams();
        
        assertTrue("Incorrect boolean returned by hasCoherentDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
 
        params.setAffymetrixData(DataState.LOWQUALITY);
        assertTrue("Incorrect boolean returned by hasCoherentDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
        paramsToCompare.setAffymetrixData(DataState.HIGHQUALITY);
        assertTrue("Incorrect boolean returned by hasCoherentDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
        params.setAffymetrixData(DataState.NODATA);
        assertFalse("Incorrect boolean returned by hasCoherentDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
        params.setAffymetrixData(null);
        assertFalse("Incorrect boolean returned by hasCoherentDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
        //to continue the test 
        params.setAffymetrixData(DataState.HIGHQUALITY);
 
        params.setESTData(DataState.LOWQUALITY);
        assertTrue("Incorrect boolean returned by hasCoherentDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
        paramsToCompare.setESTData(DataState.HIGHQUALITY);
        assertTrue("Incorrect boolean returned by hasCoherentDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
        params.setESTData(DataState.NODATA);
        assertFalse("Incorrect boolean returned by hasCoherentDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
        params.setESTData(null);
        assertFalse("Incorrect boolean returned by hasCoherentDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
        //to continue the test 
        params.setESTData(DataState.HIGHQUALITY);
 
        params.setInSituData(DataState.LOWQUALITY);
        assertTrue("Incorrect boolean returned by hasCoherentDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
        paramsToCompare.setInSituData(DataState.HIGHQUALITY);
        assertTrue("Incorrect boolean returned by hasCoherentDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
        params.setInSituData(DataState.NODATA);
        assertFalse("Incorrect boolean returned by hasCoherentDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
        params.setInSituData(null);
        assertFalse("Incorrect boolean returned by hasCoherentDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
        //to continue the test 
        params.setInSituData(DataState.HIGHQUALITY);
 
        params.setRelaxedInSituData(DataState.LOWQUALITY);
        assertTrue("Incorrect boolean returned by hasCoherentDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
        paramsToCompare.setRelaxedInSituData(DataState.HIGHQUALITY);
        assertTrue("Incorrect boolean returned by hasCoherentDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
        params.setRelaxedInSituData(DataState.NODATA);
        assertFalse("Incorrect boolean returned by hasCoherentDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
        params.setRelaxedInSituData(null);
        assertFalse("Incorrect boolean returned by hasCoherentDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
        //to continue the test 
        params.setRelaxedInSituData(DataState.HIGHQUALITY);
 
        params.setRNASeqData(DataState.LOWQUALITY);
        assertTrue("Incorrect boolean returned by hasCoherentDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
        paramsToCompare.setRNASeqData(DataState.HIGHQUALITY);
        assertTrue("Incorrect boolean returned by hasCoherentDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
        params.setRNASeqData(DataState.NODATA);
        assertFalse("Incorrect boolean returned by hasCoherentDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
        params.setRNASeqData(null);
        assertFalse("Incorrect boolean returned by hasCoherentDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
        //to continue the test 
        params.setRNASeqData(DataState.HIGHQUALITY);
        
        //test several data types and situations at the same time
        params = new FakeCallParams();
        paramsToCompare = new FakeCallParams();
        params.setAffymetrixData(DataState.HIGHQUALITY);
        paramsToCompare.setAffymetrixData(DataState.HIGHQUALITY);
        params.setESTData(DataState.HIGHQUALITY);
        paramsToCompare.setESTData(DataState.LOWQUALITY);
        params.setInSituData(DataState.LOWQUALITY);
        paramsToCompare.setInSituData(DataState.NODATA);
        params.setRelaxedInSituData(DataState.LOWQUALITY);
        paramsToCompare.setRelaxedInSituData(null);
        params.setRNASeqData(null);
        paramsToCompare.setRNASeqData(null);
        assertTrue("Incorrect boolean returned by hasCoherentDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
        
        paramsToCompare.setRNASeqData(DataState.HIGHQUALITY);
        assertFalse("Incorrect boolean returned by hasCoherentDataStates", 
                (boolean) method.invoke(params, paramsToCompare));
        
    }
    
    /**
     * Test the private method {@link CallParams#mergeDataStates(DataState, DataState)}.
     */
    @Test
    public void shouldMergeDataStates() throws NoSuchMethodException, 
        SecurityException, IllegalAccessException, IllegalArgumentException, 
        InvocationTargetException {
        //in order to test private method
        Method method = CallParams.class.getDeclaredMethod("mergeDataStates", 
                DataState.class, DataState.class);
        method.setAccessible(true);
        

        assertEquals("Incorrect merged DataState", DataState.NODATA, 
                method.invoke(null, null, null));
        
        assertEquals("Incorrect merged DataState", DataState.NODATA, 
                method.invoke(null, DataState.NODATA, null));
        
        assertEquals("Incorrect merged DataState", DataState.NODATA, 
                method.invoke(null, DataState.NODATA, DataState.LOWQUALITY));
        
        assertEquals("Incorrect merged DataState", DataState.NODATA, 
                method.invoke(null, DataState.NODATA, DataState.HIGHQUALITY));
        assertEquals("Incorrect merged DataState", DataState.NODATA, 
                method.invoke(null, DataState.HIGHQUALITY, DataState.NODATA));
        
        assertEquals("Incorrect merged DataState", DataState.LOWQUALITY, 
                method.invoke(null, DataState.LOWQUALITY, DataState.LOWQUALITY));
        
        assertEquals("Incorrect merged DataState", DataState.LOWQUALITY, 
                method.invoke(null, DataState.LOWQUALITY, DataState.HIGHQUALITY));
        
        assertEquals("Incorrect merged DataState", DataState.HIGHQUALITY, 
                method.invoke(null, DataState.HIGHQUALITY, DataState.HIGHQUALITY));
    }
    
    /**
     * Test the private method {@link CallParams#equivalent(DataState, DataState)}.
     */
    @Test
    public void testEquivalent() throws NoSuchMethodException, 
        SecurityException, IllegalAccessException, IllegalArgumentException, 
        InvocationTargetException {
        //in order to test private method
        Method method = CallParams.class.getDeclaredMethod("equivalent", 
                DataState.class, DataState.class);
        method.setAccessible(true);
        

        assertTrue("Incorrect equivalence between DataStates", 
                (boolean) method.invoke(null, DataState.NODATA, DataState.NODATA));
        assertTrue("Incorrect equivalence between DataStates", 
                (boolean) method.invoke(null, null, null));

        assertTrue("Incorrect equivalence between DataStates", 
                (boolean) method.invoke(null, DataState.NODATA, null));
        assertTrue("Incorrect equivalence between DataStates", 
                (boolean) method.invoke(null, null, DataState.NODATA));

        assertTrue("Incorrect equivalence between DataStates", 
                (boolean) method.invoke(null, DataState.LOWQUALITY, DataState.LOWQUALITY));
        assertTrue("Incorrect equivalence between DataStates", 
                (boolean) method.invoke(null, DataState.HIGHQUALITY, DataState.HIGHQUALITY));

        assertFalse("Incorrect equivalence between DataStates", 
                (boolean) method.invoke(null, DataState.LOWQUALITY, DataState.NODATA));
        assertFalse("Incorrect equivalence between DataStates", 
                (boolean) method.invoke(null, DataState.LOWQUALITY, DataState.HIGHQUALITY));
        assertFalse("Incorrect equivalence between DataStates", 
                (boolean) method.invoke(null, DataState.LOWQUALITY, null));
        assertFalse("Incorrect equivalence between DataStates", 
                (boolean) method.invoke(null, null, DataState.LOWQUALITY));

        assertFalse("Incorrect equivalence between DataStates", 
                (boolean) method.invoke(null, DataState.HIGHQUALITY, DataState.NODATA));
        assertFalse("Incorrect equivalence between DataStates", 
                (boolean) method.invoke(null, DataState.HIGHQUALITY, DataState.LOWQUALITY));
        assertFalse("Incorrect equivalence between DataStates", 
                (boolean) method.invoke(null, DataState.HIGHQUALITY, null));
        assertFalse("Incorrect equivalence between DataStates", 
                (boolean) method.invoke(null, null, DataState.HIGHQUALITY));
    }
    
    /**
     * Test the private method {@link CallParams#coherent(DataState, DataState)}.
     */
    @Test
    public void testCoherent() throws NoSuchMethodException, 
        SecurityException, IllegalAccessException, IllegalArgumentException, 
        InvocationTargetException {
        //in order to test private method
        Method method = CallParams.class.getDeclaredMethod("coherent", 
                DataState.class, DataState.class);
        method.setAccessible(true);
        

        assertTrue("Incorrect coherence between DataStates", 
                (boolean) method.invoke(null, DataState.NODATA, DataState.NODATA));
        assertTrue("Incorrect coherence between DataStates", 
                (boolean) method.invoke(null, null, null));

        assertTrue("Incorrect coherence between DataStates", 
                (boolean) method.invoke(null, DataState.NODATA, null));
        assertTrue("Incorrect coherence between DataStates", 
                (boolean) method.invoke(null, null, DataState.NODATA));

        assertTrue("Incorrect coherence between DataStates", 
                (boolean) method.invoke(null, DataState.NODATA, DataState.LOWQUALITY));
        assertTrue("Incorrect coherence between DataStates", 
                (boolean) method.invoke(null, null, DataState.LOWQUALITY));
        assertTrue("Incorrect coherence between DataStates", 
                (boolean) method.invoke(null, DataState.LOWQUALITY, DataState.NODATA));
        assertTrue("Incorrect coherence between DataStates", 
                (boolean) method.invoke(null, DataState.LOWQUALITY, null));

        assertTrue("Incorrect coherence between DataStates", 
                (boolean) method.invoke(null, DataState.LOWQUALITY, DataState.HIGHQUALITY));
        assertTrue("Incorrect coherence between DataStates", 
                (boolean) method.invoke(null, DataState.HIGHQUALITY, DataState.LOWQUALITY));

        assertFalse("Incorrect coherence between DataStates", 
                (boolean) method.invoke(null, DataState.NODATA, DataState.HIGHQUALITY));
        assertFalse("Incorrect coherence between DataStates", 
                (boolean) method.invoke(null, DataState.HIGHQUALITY, DataState.NODATA));
        assertFalse("Incorrect coherence between DataStates", 
                (boolean) method.invoke(null, null, DataState.HIGHQUALITY));
        assertFalse("Incorrect coherence between DataStates", 
                (boolean) method.invoke(null, DataState.HIGHQUALITY, null));
    }
    
    /**
     * Test the method {CallParams#hasDataRestrictions()}.
     */
    @Test
    public void testHasDataRestrictions() {
        
        CallParams params = new FakeCallParams();
        assertFalse("hasDataRestriction returned true, while no parameter was set", 
                params.hasDataRestrictions());
        
        
        params.setAllDataTypes(true);
        //as long as there are no DataStates provided for a data type, 
        //the parameter allDataTypes do nothing.
        assertFalse("hasDataRestriction returned true, while no parameter was set", 
                params.hasDataRestrictions());

        //---------- plays with the ID filters individually ---------------
        params.addAnatEntityId("test");
        assertTrue("hasDataRestriction returned false, while a parameter was set", 
                params.hasDataRestrictions());
        params.clearAnatEntityIds();
        assertFalse("hasDataRestriction returned true, while no parameter was set", 
                params.hasDataRestrictions());

        params.addDevStageId("test");
        assertTrue("hasDataRestriction returned false, while a parameter was set", 
                params.hasDataRestrictions());
        params.clearDevStageIds();
        assertFalse("hasDataRestriction returned true, while no parameter was set", 
                params.hasDataRestrictions());

        params.addGeneId("test");
        assertTrue("hasDataRestriction returned false, while a parameter was set", 
                params.hasDataRestrictions());
        params.clearGeneIds();
        assertFalse("hasDataRestriction returned true, while no parameter was set", 
                params.hasDataRestrictions());
        
        //---------- plays with each data type individually ---------------
        
        DataState dataState = DataState.HIGHQUALITY;
        DataState noDataState = DataState.NODATA;
        
        params.setAffymetrixData(dataState);
        assertTrue("hasDataRestriction returned false, while a parameter was set", 
                params.hasDataRestrictions());
        params.setAffymetrixData(null);
        assertFalse("hasDataRestriction returned true, while no parameter was set", 
                params.hasDataRestrictions());
        params.setAffymetrixData(noDataState);
        assertFalse("hasDataRestriction returned true, while no parameter was set", 
                params.hasDataRestrictions());
        
        params.setESTData(dataState);
        assertTrue("hasDataRestriction returned false, while a parameter was set", 
                params.hasDataRestrictions());
        params.setESTData(null);
        assertFalse("hasDataRestriction returned true, while no parameter was set", 
                params.hasDataRestrictions());
        params.setESTData(noDataState);
        assertFalse("hasDataRestriction returned true, while no parameter was set", 
                params.hasDataRestrictions());
        
        params.setInSituData(dataState);
        assertTrue("hasDataRestriction returned false, while a parameter was set", 
                params.hasDataRestrictions());
        params.setInSituData(null);
        assertFalse("hasDataRestriction returned true, while no parameter was set", 
                params.hasDataRestrictions());
        params.setInSituData(noDataState);
        assertFalse("hasDataRestriction returned true, while no parameter was set", 
                params.hasDataRestrictions());
        
        params.setRelaxedInSituData(dataState);
        assertTrue("hasDataRestriction returned false, while a parameter was set", 
                params.hasDataRestrictions());
        params.setRelaxedInSituData(null);
        assertFalse("hasDataRestriction returned true, while no parameter was set", 
                params.hasDataRestrictions());
        params.setRelaxedInSituData(noDataState);
        assertFalse("hasDataRestriction returned true, while no parameter was set", 
                params.hasDataRestrictions());
        
        params.setRNASeqData(dataState);
        assertTrue("hasDataRestriction returned false, while a parameter was set", 
                params.hasDataRestrictions());
        params.setRNASeqData(null);
        assertFalse("hasDataRestriction returned true, while no parameter was set", 
                params.hasDataRestrictions());
        params.setRNASeqData(noDataState);
        assertFalse("hasDataRestriction returned true, while no parameter was set", 
                params.hasDataRestrictions());

        
        //---------- plays with several parameters at the same time ---------------
        params.addAnatEntityId("test");
        params.addDevStageId("test2");
        params.addGeneId("test3");
        params.setRNASeqData(dataState);
        params.setRelaxedInSituData(dataState);
        assertTrue("hasDataRestriction returned false, while a parameter was set", 
                params.hasDataRestrictions());
    }

    
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
        
        
        
        Set<String> twoElementSet = new HashSet<String>();
        twoElementSet.add("ID1");
        twoElementSet.add("ID2");
        String thirdElement = "ID3";
        Set<String> threeElementSet = new HashSet<String>(twoElementSet);
        threeElementSet.add(thirdElement);
        
        params.addAllAnatEntityIds(twoElementSet);
        assertEquals("Incorrect anatEntityIds set/get", twoElementSet, 
                params.getAnatEntityIds());
        params.addAnatEntityId(thirdElement);
        assertEquals("Incorrect anatEntityIds set/get", threeElementSet, 
                params.getAnatEntityIds());
        params.clearAnatEntityIds();
        assertEquals("Incorrect anatEntityIds set/get", new HashSet(), 
                params.getAnatEntityIds());
        
        params.addAllDevStageIds(twoElementSet);
        assertEquals("Incorrect devStageIds set/get", twoElementSet, 
                params.getDevStageIds());
        params.addDevStageId(thirdElement);
        assertEquals("Incorrect devStageIds set/get", threeElementSet, 
                params.getDevStageIds());
        params.clearDevStageIds();
        assertEquals("Incorrect devStageIds set/get", new HashSet(), 
                params.getDevStageIds());
        
        params.addAllGeneIds(twoElementSet);
        assertEquals("Incorrect geneIds set/get", twoElementSet, 
                params.getGeneIds());
        params.addGeneId(thirdElement);
        assertEquals("Incorrect geneIds set/get", threeElementSet, 
                params.getGeneIds());
        params.clearGeneIds();
        assertEquals("Incorrect geneId set/get", new HashSet(), 
                params.getGeneIds());
        
        
        
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
