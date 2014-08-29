package org.bgee.model.dao.api.expressiondata;

import static org.junit.Assert.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TestAncestor;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.DiffCallType;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.Factor;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTO;
import org.junit.Test;

/**
 * Unit tests for the class {@link CallTO}, {@link ExpressionCallTO}, 
 * {@link NoExpressionCallTO}, and {@link DiffExpressionCallTO}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class CallTOTest extends TestAncestor {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(CallTOTest.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Simply test the getters and setters of {@code CallTO}.
     */
    @Test
    public void shouldSetGetCallTO() {
        //get an ExpressionCallTO, CallTO is abstract, we can still use 
        //the protected method anyway
        CallTO callTO = new ExpressionCallTO();
        
        String geneId = "geneId1";
        callTO.setGeneId(geneId);
        assertEquals("Incorrect geneId set/get", geneId, callTO.getGeneId());
        
        String devStageId = "stageId1";
        callTO.setDevStageId(devStageId);
        assertEquals("Incorrect devStageId set/get", devStageId, callTO.getDevStageId());
        
        String anatEntityId = "anatEntityId1";
        callTO.setAnatEntityId(anatEntityId);
        assertEquals("Incorrect anatEntityId set/get", anatEntityId, callTO.getAnatEntityId());
        
        DataState state = DataState.HIGHQUALITY;
        
        callTO.setAffymetrixData(state);
        assertEquals("Incorrect Affymetrix DataState set/get", state, callTO.getAffymetrixData());
        //to be sure to not interfere with other setters in case of bug
        callTO.setAffymetrixData(null);

        callTO.setESTData(state);
        assertEquals("Incorrect EST DataState set/get", state, callTO.getESTData());
        //to be sure to not interfere with other setters in case of bug
        callTO.setESTData(null);

        callTO.setInSituData(state);
        assertEquals("Incorrect in situ DataState set/get", state, callTO.getInSituData());
        //to be sure to not interfere with other setters in case of bug
        callTO.setInSituData(null);

        callTO.setRelaxedInSituData(state);
        assertEquals("Incorrect relaxed in situ DataState set/get", state, 
                callTO.getRelaxedInSituData());
        //to be sure to not interfere with other setters in case of bug
        callTO.setRelaxedInSituData(null);

        callTO.setRNASeqData(state);
        assertEquals("Incorrect RNA-Seq DataState set/get", state, callTO.getRNASeqData());
        //to be sure to not interfere with other setters in case of bug
        callTO.setRNASeqData(null);
    }
    
    /**
     * Simply test the getters and setters of {@code ExpressionCallTO}.
     */
    @Test
    public void shouldSetGetExpressionCallTO() {
        ExpressionCallTO callTO = new ExpressionCallTO();
        
        callTO.setIncludeSubstructures(true);
        assertTrue("Incorrect includeSubstructures set/get", callTO.isIncludeSubstructures());
        callTO.setIncludeSubstructures(false);

        callTO.setIncludeSubStages(true);
        assertTrue("Incorrect includeSubStages set/get", callTO.isIncludeSubStages());
        
    }
    
    /**
     * Simply test the getters and setters of {@code NoExpressionCallTO}.
     */
    @Test
    public void shouldSetGetNoExpressionCallTO() {
        NoExpressionCallTO callTO = new NoExpressionCallTO();
        
        callTO.setIncludeParentStructures(true);
        assertTrue("Incorrect includeParentStructures set/get", callTO.isIncludeParentStructures());
        
    }
    
    /**
     * Simply test the getters and setters of {@code DiffExpressionCallTO}.
     */
    @Test
    public void shouldSetGetDiffExpressionCallTO() {
        DiffExpressionCallTO callTO = new DiffExpressionCallTO();
        
        DiffCallType callType = DiffCallType.UNDEREXPRESSED;
        callTO.setDiffCallType(callType);
        assertEquals("Incorrect DiffCallType set/get", callType, callTO.getDiffCallType());
        
        Factor factor = Factor.DEVELOPMENT;
        callTO.setFactor(factor);
        assertEquals("Incorrect Factor set/get", factor, callTO.getFactor());
        
        int count = 10;
        callTO.setMinConditionCount(count);
        assertEquals("Incorrect minConditionCount set/get", count, callTO.getMinConditionCount());
    }
    
}
