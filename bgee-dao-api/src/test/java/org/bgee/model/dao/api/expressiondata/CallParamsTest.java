package org.bgee.model.dao.api.expressiondata;

import static org.junit.Assert.*;

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
