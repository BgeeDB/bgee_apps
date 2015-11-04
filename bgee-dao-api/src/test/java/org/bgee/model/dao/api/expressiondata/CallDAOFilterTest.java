package org.bgee.model.dao.api.expressiondata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TestAncestor;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.CallDAOFilter.DiffExpressionCallDAOFilter;
import org.bgee.model.dao.api.expressiondata.CallDAOFilter.ExpressionCallDAOFilter;
import org.bgee.model.dao.api.expressiondata.CallDAOFilter.NoExpressionCallDAOFilter;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTO;
import org.junit.Test;

/**
 * Unit tests for {@link CallDAOFilter}. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Oct. 2015
 * @since Bgee 13 Oct. 2015
 */
public class CallDAOFilterTest extends TestAncestor {
    private final static Logger log = LogManager.getLogger(CallDAOFilterTest.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test the sanity checks performed by 
     * {@link ExpressionCallDAOFilter#ExpressionCallDAOFilter(Collection, Collection, Collection, Collection)}.
     */
    //variables unused because we only test initialization.
    @SuppressWarnings("unused")
    @Test
    public void testExpressionCallDAOFilterSanityChecks() {
        ExpressionCallTO callTO1 = new ExpressionCallTO(null, null, null, null,
                null, null, null, null,
                true, null, null, null, null); 
        ExpressionCallTO callTO2 = new ExpressionCallTO(null, null, null, null,
                null, null, null, null,
                false, null, null, null, null); 
        try {
            new ExpressionCallDAOFilter(null, null, null, Arrays.asList(callTO1, callTO2));
            fail("An exception should be thrown when multiple propagation states are mixed.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        
        callTO1 = new ExpressionCallTO(null, null, null, null, null, null, null, null,
                true, null, null, null, null); 
        callTO2 = new ExpressionCallTO(null, null, null, null, null, null, null, null,
                null, null, null, null, null); 
        try {
            new ExpressionCallDAOFilter(null, null, null, Arrays.asList(callTO1, callTO2));
            fail("An exception should be thrown when multiple propagation states are mixed.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        
        callTO1 = new ExpressionCallTO(null, null, null, null, null, null, null, null,
                true, null, null, null, null); 
        callTO2 = new ExpressionCallTO(null, null, null, null, null, null, null, null,
                true, true, null, null, null); 
        try {
            new ExpressionCallDAOFilter(null, null, null, Arrays.asList(callTO1, callTO2));
            fail("An exception should be thrown when multiple propagation states are mixed.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        
        callTO1 = new ExpressionCallTO(null, null, null, null, null, null, null, null,
                true, false, null, null, null); 
        callTO2 = new ExpressionCallTO(null, null, null, null, null, null, null, null,
                true, true, null, null, null); 
        try {
            new ExpressionCallDAOFilter(null, null, null, Arrays.asList(callTO1, callTO2));
            fail("An exception should be thrown when multiple propagation states are mixed.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        
        callTO1 = new ExpressionCallTO(null, null, null, null, null, null, null, null,
                false, true, ExpressionCallTO.OriginOfLine.BOTH, null, null); 
        try {
            new ExpressionCallDAOFilter(null, null, null, Arrays.asList(callTO1));
            fail("An exception should be thrown when an origin of line/observed data stage "
                    + "is incompatible with the propagation state.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        
        callTO1 = new ExpressionCallTO(null, null, null, null, null, null, null, null,
                false, true, ExpressionCallTO.OriginOfLine.DESCENT, null, null); 
        try {
            new ExpressionCallDAOFilter(null, null, null, Arrays.asList(callTO1));
            fail("An exception should be thrown when an origin of line/observed data stage "
                    + "is incompatible with the propagation state.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        
        callTO1 = new ExpressionCallTO(null, null, null, null, null, null, null, null,
                true, false, null, ExpressionCallTO.OriginOfLine.DESCENT, null); 
        try {
            new ExpressionCallDAOFilter(null, null, null, Arrays.asList(callTO1));
            fail("An exception should be thrown when an origin of line/observed data stage "
                    + "is incompatible with the propagation state.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        
        callTO1 = new ExpressionCallTO(null, null, null, null, null, null, null, null,
                true, false, null, ExpressionCallTO.OriginOfLine.BOTH, null); 
        try {
            new ExpressionCallDAOFilter(null, null, null, Arrays.asList(callTO1));
            fail("An exception should be thrown when an origin of line/observed data stage "
                    + "is incompatible with the propagation state.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        
        callTO1 = new ExpressionCallTO(null, null, null, null, null, null, null, null,
                false, false, null, null, false); 
        try {
            new ExpressionCallDAOFilter(null, null, null, Arrays.asList(callTO1));
            fail("An exception should be thrown when an origin of line/observed data stage "
                    + "is incompatible with the propagation state.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        
        //test when everything is fine
        callTO1 = new ExpressionCallTO(null, null, null, null, null, null, null, null,
                true, false, null, null, null); 
        callTO2 = new ExpressionCallTO(null, null, null, null, null, null, null, null,
                true, null, null, null, null); 
        new ExpressionCallDAOFilter(null, null, null, Arrays.asList(callTO1, callTO2));
    }

    /**
     * Test the sanity checks performed by 
     * {@link NoExpressionCallDAOFilter#NoExpressionCallDAOFilter(Collection, Collection, Collection, Collection)}.
     */
    //variables unused because we only test initialization.
    @SuppressWarnings("unused")
    @Test
    public void testNoExpressionCallDAOFilterSanityChecks() {
        NoExpressionCallTO callTO1 = new NoExpressionCallTO(null, null, null, null,
                null, null, null, null, true, null);
        NoExpressionCallTO callTO2 = new NoExpressionCallTO(null, null, null, null,
                null, null, null, null, false, null);
        try {
            new NoExpressionCallDAOFilter(null, null, null, Arrays.asList(callTO1, callTO2));
            fail("An exception should be thrown when multiple propagation states are mixed.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        
        callTO1 = new NoExpressionCallTO(null, null, null, null, null, null, null, null, true, null);
        callTO2 = new NoExpressionCallTO(null, null, null, null, null, null, null, null, null, null);
        try {
            new NoExpressionCallDAOFilter(null, null, null, Arrays.asList(callTO1, callTO2));
            fail("An exception should be thrown when multiple propagation states are mixed.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        
        callTO1 = new NoExpressionCallTO(null, null, null, null, null, null, null, null, false, 
                NoExpressionCallTO.OriginOfLine.BOTH);
        try {
            new NoExpressionCallDAOFilter(null, null, null, Arrays.asList(callTO1));
            fail("An exception should be thrown when an origin of line/observed data stage "
                    + "is incompatible with the propagation state.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        
        callTO1 = new NoExpressionCallTO(null, null, null, null, null, null, null, null, false, 
                NoExpressionCallTO.OriginOfLine.PARENT);
        try {
            new NoExpressionCallDAOFilter(null, null, null, Arrays.asList(callTO1));
            fail("An exception should be thrown when an origin of line/observed data stage "
                    + "is incompatible with the propagation state.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        
        //test when everything is fine
        callTO1 = new NoExpressionCallTO(null, null, null, null, null, null, null, null, false, null);
        callTO2 = new NoExpressionCallTO(null, null, null, null, null, null, null, null, null, null);
        new NoExpressionCallDAOFilter(null, null, null, Arrays.asList(callTO1, callTO2));
    }
    
    /**
     * Test method {@link ExpressionCallDAOFilter#retrieveFilteringDataTypes(Object)}
     */
    @Test
    public void shouldRetrieveExpressionFilteringDataTypes() {
        ExpressionCallDAOFilter exprFilter = new ExpressionCallDAOFilter(null, null, null, null);
        ExpressionCallTO callTO = new ExpressionCallTO(null, null, null, null,
                DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, null,
                null, null, null, null, null);
        Map<ExpressionCallDAO.Attribute, DataState> expectedMap = 
                new EnumMap<>(ExpressionCallDAO.Attribute.class);
        expectedMap.put(ExpressionCallDAO.Attribute.AFFYMETRIX_DATA, DataState.HIGHQUALITY);
        expectedMap.put(ExpressionCallDAO.Attribute.IN_SITU_DATA, DataState.LOWQUALITY);
        
        assertEquals("Incorrect filtering data types retrieved", expectedMap, 
                exprFilter.extractFilteringDataTypes(callTO));
        
        callTO = new ExpressionCallTO(null, null, null, null,
                DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY,
                null, null, null, null, null);
        expectedMap = new EnumMap<>(ExpressionCallDAO.Attribute.class);
        
        assertEquals("Incorrect filtering data types retrieved with all LOWQUALITY", expectedMap, 
                exprFilter.extractFilteringDataTypes(callTO));
        
        callTO = new ExpressionCallTO(null, null, null, null,
                DataState.NODATA, null, DataState.NODATA, null,
                null, null, null, null, null);
        expectedMap = new EnumMap<>(ExpressionCallDAO.Attribute.class);
        
        assertEquals("Incorrect filtering data types retrieved with mixed null and NODATA", expectedMap, 
                exprFilter.extractFilteringDataTypes(callTO));
        
        callTO = new ExpressionCallTO(null, null, null, null,
                DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                null, null, null, null, null);
        expectedMap = new EnumMap<>(ExpressionCallDAO.Attribute.class);
        expectedMap.put(ExpressionCallDAO.Attribute.AFFYMETRIX_DATA, DataState.HIGHQUALITY);
        expectedMap.put(ExpressionCallDAO.Attribute.EST_DATA, DataState.HIGHQUALITY);
        expectedMap.put(ExpressionCallDAO.Attribute.IN_SITU_DATA, DataState.HIGHQUALITY);
        expectedMap.put(ExpressionCallDAO.Attribute.RNA_SEQ_DATA, DataState.HIGHQUALITY);
        
        assertEquals("Incorrect filtering data types retrieved with all HIGHQUALITY", expectedMap, 
                exprFilter.extractFilteringDataTypes(callTO));
    }

    /**
     * Test method {@link NoExpressionCallDAOFilter#retrieveFilteringDataTypes(Object)}
     */
    @Test
    public void shouldRetrieveNoExpressionFilteringDataTypes() {
        NoExpressionCallDAOFilter exprFilter = new NoExpressionCallDAOFilter(null, null, null, null);
        NoExpressionCallTO callTO = new NoExpressionCallTO(null, null, null, null,
                DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, DataState.LOWQUALITY,
                null, null);
        Map<NoExpressionCallDAO.Attribute, DataState> expectedMap = 
                new EnumMap<>(NoExpressionCallDAO.Attribute.class);
        expectedMap.put(NoExpressionCallDAO.Attribute.AFFYMETRIX_DATA, DataState.HIGHQUALITY);
        expectedMap.put(NoExpressionCallDAO.Attribute.RNA_SEQ_DATA, DataState.LOWQUALITY);
        
        assertEquals("Incorrect filtering data types retrieved", expectedMap, 
                exprFilter.extractFilteringDataTypes(callTO));
        
        callTO = new NoExpressionCallTO(null, null, null, null,
                DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY,
                null, null);
        expectedMap = new EnumMap<>(NoExpressionCallDAO.Attribute.class);
        
        assertEquals("Incorrect filtering data types retrieved with all LOWQUALITY", expectedMap, 
                exprFilter.extractFilteringDataTypes(callTO));
        
        callTO = new NoExpressionCallTO(null, null, null, null,
                DataState.NODATA, null, DataState.LOWQUALITY, DataState.NODATA,
                null, null);
        expectedMap = new EnumMap<>(NoExpressionCallDAO.Attribute.class);
        
        assertEquals("Incorrect filtering data types retrieved with mixed null and NODATA", expectedMap, 
                exprFilter.extractFilteringDataTypes(callTO));
        
        callTO = new NoExpressionCallTO(null, null, null, null,
                DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.HIGHQUALITY,
                null, null);
        expectedMap = new EnumMap<>(NoExpressionCallDAO.Attribute.class);
        expectedMap.put(NoExpressionCallDAO.Attribute.AFFYMETRIX_DATA, DataState.HIGHQUALITY);
        expectedMap.put(NoExpressionCallDAO.Attribute.IN_SITU_DATA, DataState.HIGHQUALITY);
        expectedMap.put(NoExpressionCallDAO.Attribute.RNA_SEQ_DATA, DataState.HIGHQUALITY);
        
        assertEquals("Incorrect filtering data types retrieved with all HIGHQUALITY", expectedMap, 
                exprFilter.extractFilteringDataTypes(callTO));
    }

    /**
     * Test method {@link DiffExpressionCallDAOFilter#retrieveFilteringDataTypes(Object)}
     */
    @Test
    public void shouldRetrieveDiffExpressionFilteringDataTypes() {
        DiffExpressionCallDAOFilter exprFilter = new DiffExpressionCallDAOFilter(null, null, null, null);
        DiffExpressionCallTO callTO = new DiffExpressionCallTO(null, null, null, null, null, 
                null, DataState.LOWQUALITY, null, null, null, 
                null, null, null, null, null);
        Map<DiffExpressionCallDAO.Attribute, DataState> expectedMap = 
                new EnumMap<>(DiffExpressionCallDAO.Attribute.class);
        expectedMap.put(DiffExpressionCallDAO.Attribute.DIFF_EXPR_AFFYMETRIX_DATA, DataState.LOWQUALITY);
        
        assertEquals("Incorrect filtering data types retrieved", expectedMap, 
                exprFilter.extractFilteringDataTypes(callTO));
        
        callTO = new DiffExpressionCallTO(null, null, null, null, null, 
                null, DataState.LOWQUALITY, null, null, null, 
                null, DataState.LOWQUALITY, null, null, null);
        expectedMap = new EnumMap<>(DiffExpressionCallDAO.Attribute.class);
        
        assertEquals("Incorrect filtering data types retrieved with all LOWQUALITY", expectedMap, 
                exprFilter.extractFilteringDataTypes(callTO));
        
        callTO = new DiffExpressionCallTO(null, null, null, null, null, 
                null, null, null, null, null, 
                null, DataState.NODATA, null, null, null);
        expectedMap = new EnumMap<>(DiffExpressionCallDAO.Attribute.class);
        
        assertEquals("Incorrect filtering data types retrieved with mixed null and NODATA", expectedMap, 
                exprFilter.extractFilteringDataTypes(callTO));
        
        callTO = new DiffExpressionCallTO(null, null, null, null, null, 
                null, DataState.HIGHQUALITY, null, null, null, 
                null, DataState.HIGHQUALITY, null, null, null);
        expectedMap = new EnumMap<>(DiffExpressionCallDAO.Attribute.class);
        expectedMap.put(DiffExpressionCallDAO.Attribute.DIFF_EXPR_AFFYMETRIX_DATA, DataState.HIGHQUALITY);
        expectedMap.put(DiffExpressionCallDAO.Attribute.DIFF_EXPR_RNA_SEQ_DATA, DataState.HIGHQUALITY);
        
        assertEquals("Incorrect filtering data types retrieved with all HIGHQUALITY", expectedMap, 
                exprFilter.extractFilteringDataTypes(callTO));
    }
}
