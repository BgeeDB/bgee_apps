package org.bgee.model.dao.api.expressiondata;

import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TestAncestor;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.DiffExprCallType;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.ComparisonFactor;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO.OriginOfLine;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.GlobalExpressionToExpressionTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.GlobalNoExpressionToNoExpressionTO;
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
     * Test {@link CallTO.DataState#convertToDataState(String)}.
     */
    @Test
    public void shouldConvertToDataState() {
        boolean hasElement = false;
        for (DataState element: DataState.values()) {
            hasElement = true;
            log.trace("Testing: {}", element);
            assertEquals("Incorrect DataState returned", element, 
                    DataState.convertToDataState(element.getStringRepresentation()));
            assertEquals("Incorrect DataState returned", element, 
                    DataState.convertToDataState(element.name()));
        }
        assertTrue("No element for DataState", hasElement);
        
        //should throw an IllegalArgumentException when not matching any DataState
        try {
            DataState.convertToDataState("whatever");
            //test failed
            throw new AssertionError("convertToDataState did not throw " +
            		"an IllegalArgumentException as expected");
        } catch (IllegalArgumentException e) {
            //test passed, do nothing
            log.catching(e);
        }
    }
    
    /**
     * Test {@link DiffExpressionCallTO.DiffCallType#convertToDiffCallType(String)}.
     */
    @Test
    public void shouldConvertToDiffCallType() {
        boolean hasElement = false;
        for (DiffExprCallType element: DiffExprCallType.values()) {
            hasElement = true;
            log.trace("Testing: {}", element);
            assertEquals("Incorrect DiffCallType returned", element, 
                    DiffExprCallType.convertToDiffExprCallType(element.getStringRepresentation()));
            assertEquals("Incorrect DiffCallType returned", element, 
                    DiffExprCallType.convertToDiffExprCallType(element.name()));
        }
        assertTrue("No element for DiffCallType", hasElement);
        
        //should throw an IllegalArgumentException when not matching any DiffCallType
        try {
            DiffExprCallType.convertToDiffExprCallType("whatever");
            //test failed
            throw new AssertionError("convertToDiffCallType did not throw " +
                    "an IllegalArgumentException as expected");
        } catch (IllegalArgumentException e) {
            //test passed, do nothing
            log.catching(e);
        }
    }
    
    /**
     * Test {@link DiffExpressionCallTO.ComparisonFactor#convertToComparisonFactor(String)}.
     */
    @Test
    public void shouldConvertToFactor() {
        boolean hasElement = false;
        for (ComparisonFactor element: ComparisonFactor.values()) {
            hasElement = true;
            log.trace("Testing: {}", element);
            assertEquals("Incorrect Factor returned", element, 
                    ComparisonFactor.convertToComparisonFactor(element.getStringRepresentation()));
            assertEquals("Incorrect Factor returned", element, 
                    ComparisonFactor.convertToComparisonFactor(element.name()));
        }
        assertTrue("No element for Factor", hasElement);
        
        //should throw an IllegalArgumentException when not matching any Factor
        try {
            ComparisonFactor.convertToComparisonFactor("whatever");
            //test failed
            throw new AssertionError("convertToFactor did not throw " +
                    "an IllegalArgumentException as expected");
        } catch (IllegalArgumentException e) {
            //test passed, do nothing
            log.catching(e);
        }
    }
    
    /**
     * Test {@link ExpressionCallTO.OriginOfLine#convertToOriginOfLine(String)}.
     */
    @Test
    public void shouldConvertToExpressionOriginOfLine() {
        boolean hasElement = false;
        for (OriginOfLine element: OriginOfLine.values()) {
            hasElement = true;
            log.trace("Testing: {}", element);
            assertEquals("Incorrect OriginOfLine returned", element, 
                    OriginOfLine.convertToOriginOfLine(element.getStringRepresentation()));
            assertEquals("Incorrect OriginOfLine returned", element, 
                    OriginOfLine.convertToOriginOfLine(element.name()));
        }
        assertTrue("No element for OriginOfLine", hasElement);
        
        //should throw an IllegalArgumentException when not matching any OriginOfLine
        try {
            OriginOfLine.convertToOriginOfLine("whatever");
            //test failed
            throw new AssertionError("convertToOriginOfLine did not throw " +
                    "an IllegalArgumentException as expected");
        } catch (IllegalArgumentException e) {
            //test passed, do nothing
            log.catching(e);
        }
    }
    
    /**
     * Test {@link NoExpressionCallTO.OriginOfLine#convertToOriginOfLine(String)}.
     */
    @Test
    public void shouldConvertToNoExpressionOriginOfLine() {
        boolean hasElement = false;
        for (NoExpressionCallTO.OriginOfLine element: NoExpressionCallTO.OriginOfLine.values()) {
            hasElement = true;
            log.trace("Testing: {}", element);
            assertEquals("Incorrect OriginOfLine returned", element, 
                    NoExpressionCallTO.OriginOfLine.convertToOriginOfLine(
                            element.getStringRepresentation()));
            assertEquals("Incorrect OriginOfLine returned", element, 
                    NoExpressionCallTO.OriginOfLine.convertToOriginOfLine(element.name()));
        }
        assertTrue("No element for OriginOfLine", hasElement);
        
        //should throw an IllegalArgumentException when not matching any OriginOfLine
        try {
            NoExpressionCallTO.OriginOfLine.convertToOriginOfLine("whatever");
            //test failed
            throw new AssertionError("convertToOriginOfLine did not throw " +
                    "an IllegalArgumentException as expected");
        } catch (IllegalArgumentException e) {
            //test passed, do nothing
            log.catching(e);
        }
    }
    
    /**
     * Test {@link DiffExpressionCallTO#hashCode()} and 
     * {@link DiffExpressionCallTO#equals(Object)}
     */
    @Test
    public void testDiffExpressionCallTOHashCodeEquals() {
        DiffExpressionCallTO callTO1 = 
                new DiffExpressionCallTO("1", "1", null, null, null, null, null, null, null, 
                        null, null, null, null, null, null);
        DiffExpressionCallTO callTO2 = 
                new DiffExpressionCallTO("1", "3", null, null, null, null, null, null, null, 
                        null, null, null, null, null, null);
        assertEquals("CallTOs with same IDs should be equal whatever their other attributes", 
                callTO1, callTO2);
        assertEquals("CallTOs with same IDs should have equal hashCode whatever " +
        		"their other attributes", callTO1.hashCode(), callTO2.hashCode());
        
        callTO1 = new DiffExpressionCallTO(null, "1", "2", "3", null, null, null, null, null, 
                null, null, null, null, null, 0);
        callTO2 = new DiffExpressionCallTO(null, "1", "2", "3", null, null, null, null, null, 
                null, null, null, null, null, 2);
        assertEquals("CallTOs with a null ID but same geneId-stageId-anatEntityId " +
                "should be equal whatever their other attributes", 
                callTO1, callTO2);
        assertEquals("CallTOs with a null ID but same geneId-stageId-anatEntityId " +
                "should be have equal hashCode whatever their other attributes", 
                callTO1.hashCode(), callTO2.hashCode());
        
        callTO1 = new DiffExpressionCallTO(null, "1", "2", null, null, null, null, null, null, 
                null, null, null, null, null, 2);
        callTO2 = new DiffExpressionCallTO(null, "1", "2", null, null, null, null, null, null, 
                null, null, null, null, null, 0);
        assertNotEquals("CallTOs with a null ID, and at least one of " +
                "geneId-stageId-anatEntityId also null, " +
                "should be compared over all attributes", callTO1, callTO2);
        //we do not test hashCode, as it is not mandatory to have different hashCode 
        //for non-equal objects
        
    }
    
    /**
     * Test {@link ExpressionCallTO#hashCode()} and {@link ExpressionCallTO#equals(Object)}
     */
    @Test
    public void testExpressionCallTOHashCodeEquals() {
        ExpressionCallTO callTO1 = 
                new ExpressionCallTO("1", "1", null, null, new BigDecimal("1.2"), 
                        DataState.NODATA, null, DataState.NODATA, null, 
                        DataState.NODATA, null, DataState.NODATA, null, false, false, 
                        OriginOfLine.SELF, OriginOfLine.SELF, true);
        ExpressionCallTO callTO2 = 
                new ExpressionCallTO("1", "3", null, null, null, DataState.NODATA, null, 
                        DataState.NODATA, null, DataState.NODATA, null, DataState.NODATA, null, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true);
        assertEquals("CallTOs with same IDs should be equal whatever their other attributes", 
                callTO1, callTO2);
        assertEquals("CallTOs with same IDs should have equal hashCode whatever " +
                "their other attributes", callTO1.hashCode(), callTO2.hashCode());
        
        callTO1 = new ExpressionCallTO(null, "1", "2", "3", new BigDecimal("2.566"), 
                DataState.NODATA, new BigDecimal("1.566"), DataState.NODATA, null, 
                DataState.NODATA, null, DataState.NODATA, null, false, true, 
                OriginOfLine.SELF, OriginOfLine.DESCENT, false);
        callTO2 = new ExpressionCallTO(null, "1", "2", "3", null, 
                DataState.NODATA, null, DataState.NODATA, new BigDecimal("1.566"), 
                DataState.NODATA, null, DataState.NODATA, null, false, false, 
                OriginOfLine.SELF, OriginOfLine.SELF, true);
        assertEquals("CallTOs with a null ID but same geneId-stageId-anatEntityId " +
                "should be equal whatever their other attributes", 
                callTO1, callTO2);
        assertEquals("CallTOs with a null ID but same geneId-stageId-anatEntityId " +
                "should be have equal hashCode whatever their other attributes", 
                callTO1.hashCode(), callTO2.hashCode());

        callTO1 = new ExpressionCallTO(null, "1", "2", null, new BigDecimal("1.5"), 
                DataState.NODATA, new BigDecimal("2.5"), DataState.NODATA, null, 
                DataState.NODATA, new BigDecimal("3.5"), DataState.NODATA, new BigDecimal("4.5"), 
                false, true, OriginOfLine.SELF, OriginOfLine.BOTH, true);
        callTO2 = new ExpressionCallTO(null, "1", "2", null, new BigDecimal("1.5"), 
                DataState.NODATA, new BigDecimal("2.5"), DataState.NODATA, null, 
                DataState.NODATA, new BigDecimal("3.5"), DataState.NODATA, new BigDecimal("4.5"), 
                false, true, OriginOfLine.SELF, OriginOfLine.BOTH, true);
        assertEquals("CallTOs with a null ID, and at least one of " +
                "geneId-stageId-anatEntityId also null, " +
                "should be compared over all attributes", callTO1, callTO2);
        assertEquals("CallTOs with a null ID, and at least one of " +
                "geneId-stageId-anatEntityId also null, " +
                "should be compared over all attributes", 
                callTO1.hashCode(), callTO2.hashCode());
        
        callTO1 = new ExpressionCallTO(null, "1", "2", null, new BigDecimal("1.5"), 
                DataState.NODATA, new BigDecimal("2.5"), DataState.NODATA, null, 
                DataState.NODATA, new BigDecimal("3.5"), DataState.NODATA, new BigDecimal("4.5"), 
                false, true, OriginOfLine.SELF, OriginOfLine.BOTH, true);
        callTO2 = new ExpressionCallTO(null, "1", "2", null, new BigDecimal("1.5"), 
                DataState.NODATA, new BigDecimal("2.5"), DataState.NODATA, null, 
                DataState.NODATA, new BigDecimal("0.5"), DataState.NODATA, new BigDecimal("4.5"), 
                false, true, OriginOfLine.SELF, OriginOfLine.SELF, true);
        assertNotEquals("CallTOs with a null ID, and at least one of " +
                "geneId-stageId-anatEntityId also null, " +
                "should be compared over all attributes", callTO1, callTO2);
        //we do not test hashCode, as it is not mandatory to have different hashCode 
        //for non-equal objects

        callTO1 = new ExpressionCallTO(null, "1", "2", null, new BigDecimal("1.5"), 
                DataState.NODATA, new BigDecimal("2.5"), DataState.NODATA, null, 
                DataState.NODATA, new BigDecimal("3.5"), DataState.NODATA, new BigDecimal("4.5"), 
                false, true, OriginOfLine.SELF, OriginOfLine.BOTH, true);
        callTO2 = new ExpressionCallTO(null, "1", "2", null, new BigDecimal("1.5"), 
                DataState.NODATA, new BigDecimal("2.5"), DataState.NODATA, null, 
                DataState.NODATA, new BigDecimal("3.5"), DataState.NODATA, new BigDecimal("4.5"), 
                false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, true);
        assertNotEquals("CallTOs with a null ID, and at least one of " +
                "geneId-stageId-anatEntityId also null, " +
                "should be compared over all attributes", callTO1, callTO2);
        //we do not test hashCode, as it is not mandatory to have different hashCode 
        //for non-equal objects
        
    }
    
    /**
     * Test {@link GlobalExpressionToExpressionTO#hashCode()} and 
     * {@link GlobalExpressionToExpressionTO#equals(Object)}.
     */
    @Test
    public void testGlobalExpressionToExpressionTOHashCodeEquals() {
        GlobalExpressionToExpressionTO to1 = 
                new GlobalExpressionToExpressionTO("1", "2");
        GlobalExpressionToExpressionTO to2 = 
                new GlobalExpressionToExpressionTO("1", "2");
        assertEquals("GlobalExpressionToExpressionTO incorrectly seen as non equal", 
                to1, to2);
        assertEquals("GlobalExpressionToExpressionTO incorrectly seen as non equal", 
                to1.hashCode(), to2.hashCode());
        
    }
    
    /**
     * Test {@link NoExpressionCallTO#hashCode()} and {@link NoExpressionCallTO#equals(Object)}
     */
    @Test
    public void testNoExpressionCallTOHashCodeEquals() {
        NoExpressionCallTO callTO1 = 
                new NoExpressionCallTO("1", "1", null, null, DataState.NODATA, DataState.NODATA, 
                        DataState.NODATA, DataState.NODATA, false, 
                        NoExpressionCallTO.OriginOfLine.SELF);
        NoExpressionCallTO callTO2 = 
                new NoExpressionCallTO("1", "3", null, null, DataState.NODATA, DataState.NODATA, 
                        DataState.NODATA, DataState.NODATA, false, 
                        NoExpressionCallTO.OriginOfLine.SELF);
        assertEquals("CallTOs with same IDs should be equal whatever their other attributes", 
                callTO1, callTO2);
        assertEquals("CallTOs with same IDs should have equal hashCode whatever " +
                "their other attributes", callTO1.hashCode(), callTO2.hashCode());
        
        callTO1 = new NoExpressionCallTO(null, "1", "2", "3", 
                DataState.NODATA, DataState.NODATA, 
                DataState.NODATA, DataState.NODATA, false, 
                NoExpressionCallTO.OriginOfLine.SELF);
        callTO2 = new NoExpressionCallTO(null, "1", "2", "3", 
                DataState.NODATA, DataState.NODATA, 
                DataState.NODATA, DataState.NODATA, true, 
                NoExpressionCallTO.OriginOfLine.SELF);
        assertEquals("CallTOs with a null ID but same geneId-stageId-anatEntityId " +
                "should be equal whatever their other attributes", 
                callTO1, callTO2);
        assertEquals("CallTOs with a null ID but same geneId-stageId-anatEntityId " +
                "should be have equal hashCode whatever their other attributes", 
                callTO1.hashCode(), callTO2.hashCode());
        
        callTO1 = new NoExpressionCallTO(null, "1", "2", null, 
                DataState.NODATA, DataState.NODATA, 
                DataState.NODATA, DataState.NODATA, false, 
                NoExpressionCallTO.OriginOfLine.SELF);
        callTO2 = new NoExpressionCallTO(null, "1", "2", null, 
                DataState.NODATA, DataState.NODATA, 
                DataState.NODATA, DataState.NODATA, true, 
                NoExpressionCallTO.OriginOfLine.SELF);
        assertNotEquals("CallTOs with a null ID, and at least one of " +
                "geneId-stageId-anatEntityId also null, " +
                "should be compared over all attributes", callTO1, callTO2);
        //we do not test hashCode, as it is not mandatory to have different hashCode 
        //for non-equal objects
    }
    
    /**
     * Test {@link GlobalNoExpressionToNoExpressionTO#hashCode()} and 
     * {@link GlobalNoExpressionToNoExpressionTO#equals(Object)}.
     */
    @Test
    public void testGlobalNoExpressionToNoExpressionTOHashCodeEquals() {
        GlobalNoExpressionToNoExpressionTO to1 = 
                new GlobalNoExpressionToNoExpressionTO("1", "2");
        GlobalNoExpressionToNoExpressionTO to2 = 
                new GlobalNoExpressionToNoExpressionTO("1", "2");
        assertEquals("GlobalNoExpressionToNoExpressionTO incorrectly seen as non equal", 
                to1, to2);
        assertEquals("GlobalNoExpressionToNoExpressionTO incorrectly seen as non equal", 
                to1.hashCode(), to2.hashCode());
        
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
        
        String stageId = "stageId1";
        callTO.setStageId(stageId);
        assertEquals("Incorrect stageId set/get", stageId, callTO.getStageId());
        
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
        
        DiffExprCallType callType = DiffExprCallType.UNDER_EXPRESSED;
        callTO.setDiffExprCallTypeAffymetrix(callType);
        assertEquals("Incorrect DiffCallType set/get for Affymetrix", callType, 
                callTO.getDiffExprCallTypeAffymetrix());
        
        callType = DiffExprCallType.OVER_EXPRESSED;
        callTO.setDiffExprCallTypeRNASeq(callType);
        assertEquals("Incorrect DiffCallType set/get for RNA-Seq", callType, 
                callTO.getDiffExprCallTypeRNASeq());

        ComparisonFactor factor = ComparisonFactor.DEVELOPMENT;
        callTO.setComparisonFactor(factor);
        assertEquals("Incorrect Factor set/get", factor, callTO.getComparisonFactor());
        
//        int count = 10;
//        callTO.setMinConditionCount(count);
//        assertEquals("Incorrect minConditionCount set/get", 
//                count, (int) callTO.getMinConditionCount());
    }
}
