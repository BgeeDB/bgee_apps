package org.bgee.model;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.keyword.KeywordDAO.KeywordTO;
import org.bgee.model.dao.api.keyword.KeywordDAO.KeywordTOResultSet;
import org.junit.Test;

/**
 * Class to test correct behavior of the mock objects returned by static methods 
 * of {@link TestAncestor}. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Oct. 2015
 * @since Bgee 13 Oct. 2015
 */
public class MockTest extends TestAncestor {
    private final static Logger log = LogManager.getLogger(MockTest.class.getName());
    
    @Override
    protected Logger getLogger() {
        return log;
    } 

    /**
     * Test the mock returned by {@link TestAncestor#getMockResultSet(Class, List)};
     */
    @Test
    public void shouldGetMockResultSet() {
        List<KeywordTO> expectedTOs = Arrays.asList(
                new KeywordTO("1", "k1"), new KeywordTO("2", "k2"), new KeywordTO("1", "k1"));
        KeywordTOResultSet mockRs = getMockResultSet(KeywordTOResultSet.class, expectedTOs);

        assertNull("Incorrect value returned before first next()", mockRs.getTO());
        assertTrue("Incorrect 1st value returned by next", mockRs.next());
        assertEquals("Incorrect 1st value returned", expectedTOs.get(0), mockRs.getTO());
        assertTrue("Incorrect 2nd value returned by next", mockRs.next());
        assertEquals("Incorrect 2nd value returned", expectedTOs.get(1), mockRs.getTO());
        assertTrue("Incorrect 3rd value returned by next", mockRs.next());
        assertEquals("Incorrect 3rd value returned", expectedTOs.get(2), mockRs.getTO());
        assertEquals("Incorrect 3rd value returned for second time", expectedTOs.get(2), mockRs.getTO());
        assertFalse("Incorrect end of resultset", mockRs.next());
        assertNull("Incorrect end of resultset", mockRs.getTO());
        
        mockRs = getMockResultSet(KeywordTOResultSet.class, expectedTOs);
        assertEquals("Incorrect TOs obtained through stream", expectedTOs, mockRs.stream()
                .collect(Collectors.toList()));
        
        mockRs = getMockResultSet(KeywordTOResultSet.class, expectedTOs);
        assertEquals("Incorrect TOs obtained through getAllTOs", expectedTOs, mockRs.getAllTOs());
    }
}
