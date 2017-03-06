package org.bgee.model.dao.api.expressiondata;

import static org.junit.Assert.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.TestAncestor;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.DiffExprCallType;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.ComparisonFactor;
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
     * Test {@link GlobalExpressionToExpressionTO#hashCode()} and 
     * {@link GlobalExpressionToExpressionTO#equals(Object)}.
     */
    @Test
    public void testGlobalExpressionToExpressionTOHashCodeEquals() {
        GlobalExpressionToExpressionTO to1 = 
                new GlobalExpressionToExpressionTO(1, 2);
        GlobalExpressionToExpressionTO to2 = 
                new GlobalExpressionToExpressionTO(1, 2);
        
        assertTrue(TOComparator.areTOsEqual(to1, to2));
    }
    
    /**
     * Test {@link GlobalNoExpressionToNoExpressionTO#hashCode()} and 
     * {@link GlobalNoExpressionToNoExpressionTO#equals(Object)}.
     */
    @Test
    public void testGlobalNoExpressionToNoExpressionTOHashCodeEquals() {
        GlobalNoExpressionToNoExpressionTO to1 = 
                new GlobalNoExpressionToNoExpressionTO(1, 2);
        GlobalNoExpressionToNoExpressionTO to2 = 
                new GlobalNoExpressionToNoExpressionTO(1, 2);

        assertTrue(TOComparator.areTOsEqual(to1, to2));
        
    }
}
