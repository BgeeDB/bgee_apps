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
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.Attribute;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.junit.Test;

/**
 * Unit tests for {@link ExpressionCallDAO}. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Oct. 2015
 * @since Bgee 13 Oct. 2015
 */
public class ExpressionCallDAOTest extends TestAncestor {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(ExpressionCallDAOTest.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Test {@link ExpressionCallDAO.ExpressionCallTO#extractDataTypesToDataStates()}.
     */
    @Test
    public void shouldExtractDataTypesToDataStates() {
        ExpressionCallTO callTO = new ExpressionCallTO(DataState.HIGHQUALITY, DataState.NODATA, 
                DataState.LOWQUALITY, null);
        Map<ExpressionCallDAO.Attribute, DataState> expectedMap = new HashMap<>();
        expectedMap.put(ExpressionCallDAO.Attribute.AFFYMETRIX_DATA, DataState.HIGHQUALITY);
        expectedMap.put(ExpressionCallDAO.Attribute.EST_DATA, DataState.NODATA);
        expectedMap.put(ExpressionCallDAO.Attribute.IN_SITU_DATA, DataState.LOWQUALITY);
        expectedMap.put(ExpressionCallDAO.Attribute.RNA_SEQ_DATA, null);
        assertEquals("Incorrect data types to data states extracted", expectedMap, 
                callTO.extractDataTypesToDataStates());
        
        callTO = new ExpressionCallTO(DataState.LOWQUALITY, DataState.HIGHQUALITY, 
                DataState.NODATA, DataState.HIGHQUALITY);
        expectedMap = new HashMap<>();
        expectedMap.put(ExpressionCallDAO.Attribute.AFFYMETRIX_DATA, DataState.LOWQUALITY);
        expectedMap.put(ExpressionCallDAO.Attribute.EST_DATA, DataState.HIGHQUALITY);
        expectedMap.put(ExpressionCallDAO.Attribute.IN_SITU_DATA, DataState.NODATA);
        expectedMap.put(ExpressionCallDAO.Attribute.RNA_SEQ_DATA, DataState.HIGHQUALITY);
        assertEquals("Incorrect data types to data states extracted", expectedMap, 
                callTO.extractDataTypesToDataStates());
        
        //verify that all data type attributes are considered
        Set<ExpressionCallDAO.Attribute> dataTypeAttrs = Arrays.stream(Attribute.values())
                .filter(Attribute::isDataTypeAttribute)
                .collect(Collectors.toSet());
        assertEquals("Missing data type attributes, extractDataTypesToDataStates method "
                + "in ExpressionCallTO might need an update", dataTypeAttrs, 
                callTO.extractDataTypesToDataStates().keySet());
    }
    
    /**
     * Test method {@link ExpressionCallTO#extractFilteringDataTypes()}
     */
    @Test
    public void shouldRetrieveExpressionFilteringDataTypes() {
        ExpressionCallTO callTO = new ExpressionCallTO(DataState.HIGHQUALITY, DataState.NODATA, 
                DataState.LOWQUALITY, null);
        Map<ExpressionCallDAO.Attribute, DataState> expectedMap = 
                new EnumMap<>(ExpressionCallDAO.Attribute.class);
        expectedMap.put(ExpressionCallDAO.Attribute.AFFYMETRIX_DATA, DataState.HIGHQUALITY);
        expectedMap.put(ExpressionCallDAO.Attribute.IN_SITU_DATA, DataState.LOWQUALITY);
        
        assertEquals("Incorrect filtering data types retrieved", expectedMap, 
                callTO.extractFilteringDataTypes());
        
        callTO = new ExpressionCallTO(DataState.LOWQUALITY, DataState.LOWQUALITY, 
                DataState.LOWQUALITY, DataState.LOWQUALITY);
        expectedMap = new EnumMap<>(ExpressionCallDAO.Attribute.class);
        expectedMap.put(ExpressionCallDAO.Attribute.AFFYMETRIX_DATA, DataState.LOWQUALITY);
        expectedMap.put(ExpressionCallDAO.Attribute.EST_DATA, DataState.LOWQUALITY);
        expectedMap.put(ExpressionCallDAO.Attribute.IN_SITU_DATA, DataState.LOWQUALITY);
        expectedMap.put(ExpressionCallDAO.Attribute.RNA_SEQ_DATA, DataState.LOWQUALITY);
        
        assertEquals("Incorrect filtering data types retrieved with all LOWQUALITY", expectedMap, 
                callTO.extractFilteringDataTypes());
        
        callTO = new ExpressionCallTO(DataState.NODATA, null, DataState.NODATA, null);
        expectedMap = new EnumMap<>(ExpressionCallDAO.Attribute.class);
        
        assertEquals("Incorrect filtering data types retrieved with mixed null and NODATA", expectedMap, 
                callTO.extractFilteringDataTypes());
        
        callTO = new ExpressionCallTO(DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                DataState.HIGHQUALITY, DataState.HIGHQUALITY);
        expectedMap = new EnumMap<>(ExpressionCallDAO.Attribute.class);
        expectedMap.put(ExpressionCallDAO.Attribute.AFFYMETRIX_DATA, DataState.HIGHQUALITY);
        expectedMap.put(ExpressionCallDAO.Attribute.EST_DATA, DataState.HIGHQUALITY);
        expectedMap.put(ExpressionCallDAO.Attribute.IN_SITU_DATA, DataState.HIGHQUALITY);
        expectedMap.put(ExpressionCallDAO.Attribute.RNA_SEQ_DATA, DataState.HIGHQUALITY);
        
        assertEquals("Incorrect filtering data types retrieved with all HIGHQUALITY", expectedMap, 
                callTO.extractFilteringDataTypes());
    }
}
