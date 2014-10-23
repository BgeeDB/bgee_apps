package org.bgee.pipeline;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.mysql.species.MySQLSpeciesDAO.MySQLSpeciesTOResultSet;
import org.junit.Test;

/**
 * Tests for {@link BgeeDBUtils}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class BgeeDBUtilsTest extends TestAncestor {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(BgeeDBUtilsTest.class.getName());

    /**
     * Default Constructor. 
     */
    public BgeeDBUtilsTest() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test {@link BgeeDBUtils#getSpeciesIdsFromDb(SpeciesDAO)}
     */
    @Test
    public void shouldGetSpeciesIdsFromDb() {
        
        MockDAOManager mockManager = new MockDAOManager();
        MySQLSpeciesTOResultSet mockSpeciesResultSet = this.mockGetAllSpecies(mockManager);
        
        assertEquals("Incorrect speciesIDs retrieved", Arrays.asList("21", "11", "30"), 
                BgeeDBUtils.getSpeciesIdsFromDb(mockManager.getSpeciesDAO()));
        verify(mockManager.getSpeciesDAO()).setAttributes(SpeciesDAO.Attribute.ID);
        verify(mockSpeciesResultSet).close();
    }
    
    /**
     * Test {@link BgeeDBUtils#checkAndGetSpeciesIds(List, SpeciesDAO)}
     */
    @Test
    public void shouldCheckAndGetSpeciesIds() {
        
        MockDAOManager mockManager = new MockDAOManager();

        this.mockGetAllSpecies(mockManager);
        assertEquals("Incorrect speciesIDs checked and retrieved", 
                Arrays.asList("21", "11", "30"), BgeeDBUtils.checkAndGetSpeciesIds(
                        null, mockManager.getSpeciesDAO()));
        
        this.mockGetAllSpecies(mockManager);
        assertEquals("Incorrect speciesIDs checked and retrieved", 
                Arrays.asList("21", "11", "30"), BgeeDBUtils.checkAndGetSpeciesIds(
                        new ArrayList<String>(), mockManager.getSpeciesDAO()));

        this.mockGetAllSpecies(mockManager);
        assertEquals("Incorrect speciesIDs checked and retrieved", 
                Arrays.asList("30", "21", "11"), BgeeDBUtils.checkAndGetSpeciesIds(
                        Arrays.asList("30", "21", "11"), mockManager.getSpeciesDAO()));

        this.mockGetAllSpecies(mockManager);
        assertEquals("Incorrect speciesIDs checked and retrieved", 
                Arrays.asList("30", "11"), BgeeDBUtils.checkAndGetSpeciesIds(
                        Arrays.asList("30", "11"), mockManager.getSpeciesDAO()));
        try {
            this.mockGetAllSpecies(mockManager);
            BgeeDBUtils.checkAndGetSpeciesIds(Arrays.asList("11", "30", "100"), 
                    mockManager.getSpeciesDAO());
            //test failed, the method should have thrown an exception
            throw new AssertionError("checkAndGetSpeciesIds did not throw " +
            		"an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            //test passed
        }
    }

    /**
     * Define a mock MySQLSpeciesTOResultSet to mock the return of getAllSpecies.
     * 
     * @param mockManager A {@code MySQLDAOManager} to for the class to acquire mock DAOs.
     */
    private MySQLSpeciesTOResultSet mockGetAllSpecies(MockDAOManager mockManager) {
        
        // We need a mock MySQLSpeciesTOResultSet to mock the return of getAllSpecies().
        MySQLSpeciesTOResultSet mockSpeciesTORs = createMockDAOResultSet(
                Arrays.asList(
                        new SpeciesTO("21", null, null, null, null, null, null, null),
                        new SpeciesTO("11", null, null, null, null, null, null, null),
                        new SpeciesTO("30", null, null, null, null, null, null, null)),
                MySQLSpeciesTOResultSet.class);
        when(mockManager.mockSpeciesDAO.getAllSpecies()).thenReturn(mockSpeciesTORs);
        
        return mockSpeciesTORs;
    }
}
