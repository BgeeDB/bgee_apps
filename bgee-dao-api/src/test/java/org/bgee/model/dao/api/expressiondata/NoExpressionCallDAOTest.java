package org.bgee.model.dao.api.expressiondata;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TestAncestor;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTO;
import org.junit.Test;

/**
 * Unit tests for {@link NoExpressionCallDAO}. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Oct. 2015
 * @since Bgee 13 Oct. 2015
 */
public class NoExpressionCallDAOTest extends TestAncestor {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(NoExpressionCallDAOTest.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Test {@link NoExpressionCallDAOTest.NoExpressionCallTO#extractDataTypesToDataStates()}.
     */
    @Test
    public void shouldExtractDataTypesToDataStates() {
        NoExpressionCallTO callTO = new NoExpressionCallTO(null, null, null,
                DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, null,
                null, null);
        Map<NoExpressionCallDAO.Attribute, DataState> expectedMap = new HashMap<>();
        expectedMap.put(NoExpressionCallDAO.Attribute.AFFYMETRIX_DATA, DataState.HIGHQUALITY);
        expectedMap.put(NoExpressionCallDAO.Attribute.IN_SITU_DATA, DataState.NODATA);
        expectedMap.put(NoExpressionCallDAO.Attribute.RNA_SEQ_DATA, null);
        assertEquals("Incorrect data types to data states extracted", expectedMap, 
                callTO.extractDataTypesToDataStates());
        
        callTO = new NoExpressionCallTO(null, null, null,
                DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.HIGHQUALITY,
                null, null);
        expectedMap = new HashMap<>();
        expectedMap.put(NoExpressionCallDAO.Attribute.AFFYMETRIX_DATA, DataState.LOWQUALITY);
        expectedMap.put(NoExpressionCallDAO.Attribute.IN_SITU_DATA, DataState.HIGHQUALITY);
        expectedMap.put(NoExpressionCallDAO.Attribute.RNA_SEQ_DATA, DataState.HIGHQUALITY);
        assertEquals("Incorrect data types to data states extracted", expectedMap, 
                callTO.extractDataTypesToDataStates());
        
        //verify that all data type attributes are considered
        Set<NoExpressionCallDAO.Attribute> dataTypeAttrs = 
                Arrays.stream(NoExpressionCallDAO.Attribute.values())
                .filter(NoExpressionCallDAO.Attribute::isDataTypeAttribute)
                .collect(Collectors.toSet());
        assertEquals("Missing data type attributes, extractDataTypesToDataStates method "
                + "in NoExpressionCallTO might need an update", dataTypeAttrs, 
                callTO.extractDataTypesToDataStates().keySet());
    }

    /**
     * Test method {@link NoExpressionCallTO#extractFilteringDataTypes()}
     */
    @Test
    public void shouldRetrieveNoExpressionFilteringDataTypes() {
        NoExpressionCallTO callTO = new NoExpressionCallTO(null, null, null,
                DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, DataState.LOWQUALITY,
                null, null);
        Map<NoExpressionCallDAO.Attribute, DataState> expectedMap = 
                new EnumMap<>(NoExpressionCallDAO.Attribute.class);
        expectedMap.put(NoExpressionCallDAO.Attribute.AFFYMETRIX_DATA, DataState.HIGHQUALITY);
        expectedMap.put(NoExpressionCallDAO.Attribute.RNA_SEQ_DATA, DataState.LOWQUALITY);
        
        assertEquals("Incorrect filtering data types retrieved", expectedMap, 
                callTO.extractFilteringDataTypes());
        
        callTO = new NoExpressionCallTO(null, null, null,
                DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY,
                null, null);
        expectedMap = new EnumMap<>(NoExpressionCallDAO.Attribute.class);
        expectedMap.put(NoExpressionCallDAO.Attribute.AFFYMETRIX_DATA, DataState.LOWQUALITY);
        expectedMap.put(NoExpressionCallDAO.Attribute.IN_SITU_DATA, DataState.LOWQUALITY);
        expectedMap.put(NoExpressionCallDAO.Attribute.RNA_SEQ_DATA, DataState.LOWQUALITY);
        
        assertEquals("Incorrect filtering data types retrieved with all LOWQUALITY", expectedMap, 
                callTO.extractFilteringDataTypes());
        
        callTO = new NoExpressionCallTO(null, null, null,
                DataState.NODATA, null, DataState.LOWQUALITY, DataState.NODATA,
                null, null);
        expectedMap = new EnumMap<>(NoExpressionCallDAO.Attribute.class);
        
        assertEquals("Incorrect filtering data types retrieved with mixed null and NODATA", expectedMap, 
                callTO.extractFilteringDataTypes());
        
        callTO = new NoExpressionCallTO(null, null, null,
                DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.HIGHQUALITY,
                null, null);
        expectedMap = new EnumMap<>(NoExpressionCallDAO.Attribute.class);
        expectedMap.put(NoExpressionCallDAO.Attribute.AFFYMETRIX_DATA, DataState.HIGHQUALITY);
        expectedMap.put(NoExpressionCallDAO.Attribute.IN_SITU_DATA, DataState.HIGHQUALITY);
        expectedMap.put(NoExpressionCallDAO.Attribute.RNA_SEQ_DATA, DataState.HIGHQUALITY);
        
        assertEquals("Incorrect filtering data types retrieved with all HIGHQUALITY", expectedMap, 
                callTO.extractFilteringDataTypes());
    }
}
