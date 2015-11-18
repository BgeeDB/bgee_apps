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
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO;
import org.junit.Test;

/**
 * Unit tests for {@link DiffExpressionCallDAO}. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Oct. 2015
 * @since Bgee 13 Oct. 2015
 */
public class DiffExpressionCallDAOTest extends TestAncestor {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(DiffExpressionCallDAOTest.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Test {@link DiffExpressionCallDAOTest.DiffExpressionCallTO#extractDataTypesToDataStates()}.
     */
    @Test
    public void shouldExtractDataTypesToDataStates() {
        DiffExpressionCallTO callTO = new DiffExpressionCallTO(null, null, null, null, null, 
                null, DataState.HIGHQUALITY, null, null, null, 
                null, null, null, null, null);
        Map<DiffExpressionCallDAO.Attribute, DataState> expectedMap = new HashMap<>();
        expectedMap.put(DiffExpressionCallDAO.Attribute.DIFF_EXPR_AFFYMETRIX_DATA, DataState.HIGHQUALITY);
        expectedMap.put(DiffExpressionCallDAO.Attribute.DIFF_EXPR_RNA_SEQ_DATA, null);
        assertEquals("Incorrect data types to data states extracted", expectedMap, 
                callTO.extractDataTypesToDataStates());
        
        callTO = new DiffExpressionCallTO(null, null, null, null, null, 
                null, DataState.LOWQUALITY, null, null, null, 
                null, DataState.NODATA, null, null, null);
        expectedMap = new HashMap<>();
        expectedMap.put(DiffExpressionCallDAO.Attribute.DIFF_EXPR_AFFYMETRIX_DATA, DataState.LOWQUALITY);
        expectedMap.put(DiffExpressionCallDAO.Attribute.DIFF_EXPR_RNA_SEQ_DATA, DataState.NODATA);
        assertEquals("Incorrect data types to data states extracted", expectedMap, 
                callTO.extractDataTypesToDataStates());
        
        //verify that all data type attributes are considered
        Set<DiffExpressionCallDAO.Attribute> dataTypeAttrs = 
                Arrays.stream(DiffExpressionCallDAO.Attribute.values())
                .filter(DiffExpressionCallDAO.Attribute::isDataTypeAttribute)
                .collect(Collectors.toSet());
        assertEquals("Missing data type attributes, extractDataTypesToDataStates method "
                + "in DiffExpressionCallTO might need an update", dataTypeAttrs, 
                callTO.extractDataTypesToDataStates().keySet());
    }

    /**
     * Test method {@link DiffExpressionCallTO#extractFilteringDataTypes()}
     */
    @Test
    public void shouldRetrieveDiffExpressionFilteringDataTypes() {
        DiffExpressionCallTO callTO = new DiffExpressionCallTO(null, null, null, null, null, 
                null, DataState.LOWQUALITY, null, null, null, 
                null, null, null, null, null);
        Map<DiffExpressionCallDAO.Attribute, DataState> expectedMap = 
                new EnumMap<>(DiffExpressionCallDAO.Attribute.class);
        expectedMap.put(DiffExpressionCallDAO.Attribute.DIFF_EXPR_AFFYMETRIX_DATA, DataState.LOWQUALITY);
        
        assertEquals("Incorrect filtering data types retrieved", expectedMap, 
                callTO.extractFilteringDataTypes());
        
        callTO = new DiffExpressionCallTO(null, null, null, null, null, 
                null, DataState.LOWQUALITY, null, null, null, 
                null, DataState.LOWQUALITY, null, null, null);
        expectedMap = new EnumMap<>(DiffExpressionCallDAO.Attribute.class);
        expectedMap.put(DiffExpressionCallDAO.Attribute.DIFF_EXPR_AFFYMETRIX_DATA, DataState.LOWQUALITY);
        expectedMap.put(DiffExpressionCallDAO.Attribute.DIFF_EXPR_RNA_SEQ_DATA, DataState.LOWQUALITY);
        
        assertEquals("Incorrect filtering data types retrieved with all LOWQUALITY", expectedMap, 
                callTO.extractFilteringDataTypes());
        
        callTO = new DiffExpressionCallTO(null, null, null, null, null, 
                null, null, null, null, null, 
                null, DataState.NODATA, null, null, null);
        expectedMap = new EnumMap<>(DiffExpressionCallDAO.Attribute.class);
        
        assertEquals("Incorrect filtering data types retrieved with mixed null and NODATA", expectedMap, 
                callTO.extractFilteringDataTypes());
        
        callTO = new DiffExpressionCallTO(null, null, null, null, null, 
                null, DataState.HIGHQUALITY, null, null, null, 
                null, DataState.HIGHQUALITY, null, null, null);
        expectedMap = new EnumMap<>(DiffExpressionCallDAO.Attribute.class);
        expectedMap.put(DiffExpressionCallDAO.Attribute.DIFF_EXPR_AFFYMETRIX_DATA, DataState.HIGHQUALITY);
        expectedMap.put(DiffExpressionCallDAO.Attribute.DIFF_EXPR_RNA_SEQ_DATA, DataState.HIGHQUALITY);
        
        assertEquals("Incorrect filtering data types retrieved with all HIGHQUALITY", expectedMap, 
                callTO.extractFilteringDataTypes());
    }
}
