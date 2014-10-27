package org.bgee.pipeline;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.ontologycommon.RelationDAO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTOResultSet;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationType;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTOResultSet;
import org.bgee.model.dao.mysql.ontologycommon.MySQLRelationDAO.MySQLRelationTOResultSet;
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
        SpeciesTOResultSet mockSpeciesResultSet = this.mockGetAllSpecies(mockManager);
        
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
    private SpeciesTOResultSet mockGetAllSpecies(MockDAOManager mockManager) {
        
        // We need a mock MySQLSpeciesTOResultSet to mock the return of getAllSpecies().
        SpeciesTOResultSet mockSpeciesTORs = this.createMockDAOResultSet(
                Arrays.asList(
                        new SpeciesTO("21", null, null, null, null, null, null, null),
                        new SpeciesTO("11", null, null, null, null, null, null, null),
                        new SpeciesTO("30", null, null, null, null, null, null, null)),
                MySQLSpeciesTOResultSet.class);
        when(mockManager.mockSpeciesDAO.getAllSpecies()).thenReturn(mockSpeciesTORs);
        
        return mockSpeciesTORs;
    }
    
    /**
     * Test {@link BgeeDBUtils#getAnatEntityChildrenFromParents(Set, RelationDAO)} and 
     * {@link BgeeDBUtils#getAnatEntityParentsFromChildren(Set, RelationDAO)}
     */
    @Test
    public void shouldGetAnatEntityTargetsOrSources() {
        MockDAOManager mockManager = new MockDAOManager();
        List<RelationTO> returnedRelTOs = Arrays.asList(
                new RelationTO("1", "1"), 
                new RelationTO("2", "2"), 
                new RelationTO("3", "3"), 
                new RelationTO("4", "4"), 
                new RelationTO("5", "5"), 
                new RelationTO("2", "1"), 
                new RelationTO("3", "1"), 
                new RelationTO("4", "3"), 
                new RelationTO("4", "1"), 
                new RelationTO("3", "5"));
        
        RelationTOResultSet mockRelationTOResultSet = this.createMockDAOResultSet(
                returnedRelTOs, MySQLRelationTOResultSet.class);
        when(mockManager.getRelationDAO().getAnatEntityRelations(
                new HashSet<String>(Arrays.asList("1", "2")), 
                EnumSet.of(RelationType.ISA_PARTOF), null)).thenReturn(mockRelationTOResultSet);
        
        Map<String, Set<String>> expectedReturnedVal = new HashMap<String, Set<String>>();
        expectedReturnedVal.put("1", new HashSet<String>(Arrays.asList("1", "2", "3", "4")));
        expectedReturnedVal.put("2", new HashSet<String>(Arrays.asList("2")));
        expectedReturnedVal.put("3", new HashSet<String>(Arrays.asList("3", "4")));
        expectedReturnedVal.put("4", new HashSet<String>(Arrays.asList("4")));
        expectedReturnedVal.put("5", new HashSet<String>(Arrays.asList("5", "3")));
        
        assertEquals("Incorrect anat entity relatives by source", expectedReturnedVal, 
                BgeeDBUtils.getAnatEntityChildrenFromParents(
                        new HashSet<String>(Arrays.asList("1", "2")), 
                        mockManager.getRelationDAO()));
        verify(mockManager.getRelationDAO()).setAttributes(RelationDAO.Attribute.SOURCEID, 
                RelationDAO.Attribute.TARGETID);
        verify(mockRelationTOResultSet).close();
        
        
        mockManager = new MockDAOManager();
        mockRelationTOResultSet = this.createMockDAOResultSet(
                returnedRelTOs, MySQLRelationTOResultSet.class);
        when(mockManager.getRelationDAO().getAnatEntityRelations(
                new HashSet<String>(Arrays.asList("1", "2")), 
                EnumSet.of(RelationType.ISA_PARTOF), null)).thenReturn(mockRelationTOResultSet);
        
        expectedReturnedVal = new HashMap<String, Set<String>>();
        expectedReturnedVal.put("1", new HashSet<String>(Arrays.asList("1")));
        expectedReturnedVal.put("2", new HashSet<String>(Arrays.asList("2", "1")));
        expectedReturnedVal.put("3", new HashSet<String>(Arrays.asList("3", "1", "5")));
        expectedReturnedVal.put("4", new HashSet<String>(Arrays.asList("4", "3", "1")));
        expectedReturnedVal.put("5", new HashSet<String>(Arrays.asList("5")));
        
        assertEquals("Incorrect anat entity relatives by target", expectedReturnedVal, 
                BgeeDBUtils.getAnatEntityParentsFromChildren(
                        new HashSet<String>(Arrays.asList("1", "2")), 
                        mockManager.getRelationDAO()));
        verify(mockManager.getRelationDAO()).setAttributes(RelationDAO.Attribute.SOURCEID, 
                RelationDAO.Attribute.TARGETID);
        verify(mockRelationTOResultSet).close();
    }
    
    /**
     * Test {@link BgeeDBUtils#getStageChildrenFromParents(Set, RelationDAO)}.
     */
    @Test
    public void shouldGetStageTargetsBySources() {
        MockDAOManager mockManager = new MockDAOManager();
        //stages can have only one direct parent
        List<RelationTO> returnedRelTOs = Arrays.asList(
                new RelationTO("1", "1"), 
                new RelationTO("2", "2"), 
                new RelationTO("3", "3"), 
                new RelationTO("4", "4"), 
                new RelationTO("2", "1"), 
                new RelationTO("3", "1"), 
                new RelationTO("4", "3"), 
                new RelationTO("4", "1"));
        
        RelationTOResultSet mockRelationTOResultSet = this.createMockDAOResultSet(
                returnedRelTOs, MySQLRelationTOResultSet.class);
        when(mockManager.getRelationDAO().getStageRelations(
                new HashSet<String>(Arrays.asList("1", "2")), null)).thenReturn(
                        mockRelationTOResultSet);
        
        Map<String, Set<String>> expectedReturnedVal = new HashMap<String, Set<String>>();
        expectedReturnedVal.put("1", new HashSet<String>(Arrays.asList("1", "2", "3", "4")));
        expectedReturnedVal.put("2", new HashSet<String>(Arrays.asList("2")));
        expectedReturnedVal.put("3", new HashSet<String>(Arrays.asList("3", "4")));
        expectedReturnedVal.put("4", new HashSet<String>(Arrays.asList("4")));
        
        assertEquals("Incorrect stage relatives by source", expectedReturnedVal, 
                BgeeDBUtils.getStageChildrenFromParents(
                        new HashSet<String>(Arrays.asList("1", "2")), 
                        mockManager.getRelationDAO()));
        verify(mockManager.getRelationDAO()).setAttributes(RelationDAO.Attribute.SOURCEID, 
                RelationDAO.Attribute.TARGETID);
        verify(mockRelationTOResultSet).close();
    }
}
