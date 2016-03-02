package org.bgee.pipeline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
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
import org.bgee.model.dao.api.anatdev.AnatEntityDAO;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTO;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTOResultSet;
import org.bgee.model.dao.api.anatdev.StageDAO;
import org.bgee.model.dao.api.anatdev.StageDAO.StageTO;
import org.bgee.model.dao.api.anatdev.StageDAO.StageTOResultSet;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTOResultSet;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTOResultSet;
import org.bgee.model.dao.api.ontologycommon.RelationDAO;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTO;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTO.ConfidenceLevel;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTO.EvidenceConcordance;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTO.EvidenceTypeConcordance;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationType;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTOResultSet;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTOResultSet;
import org.bgee.model.dao.mysql.anatdev.MySQLAnatEntityDAO.MySQLAnatEntityTOResultSet;
import org.bgee.model.dao.mysql.anatdev.MySQLStageDAO.MySQLStageTOResultSet;
import org.bgee.model.dao.mysql.gene.MySQLGeneDAO.MySQLGeneTOResultSet;
import org.bgee.model.dao.mysql.ontologycommon.MySQLCIOStatementDAO.MySQLCIOStatementTOResultSet;
import org.bgee.model.dao.mysql.ontologycommon.MySQLRelationDAO.MySQLRelationTOResultSet;
import org.bgee.model.dao.mysql.species.MySQLSpeciesDAO.MySQLSpeciesTOResultSet;
import org.junit.Test;

/**
 * Tests for {@link BgeeDBUtils}.
 * 
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 13 july 
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
        
        try (MockDAOManager mockManager = new MockDAOManager()) {
            SpeciesTOResultSet mockSpeciesResultSet = this.mockGetAllSpecies(mockManager);
            
            assertEquals("Incorrect speciesIDs retrieved", Arrays.asList("21", "11", "30"), 
                    BgeeDBUtils.getSpeciesIdsFromDb(mockManager.getSpeciesDAO()));
            verify(mockManager.getSpeciesDAO()).setAttributes(SpeciesDAO.Attribute.ID);
            verify(mockSpeciesResultSet).close();
        }
    }
    
    /**
     * Test {@link BgeeDBUtils#checkAndGetSpeciesIds(List, SpeciesDAO)}
     */
    @Test
    public void shouldCheckAndGetSpeciesIds() {
        
        try (MockDAOManager mockManager = new MockDAOManager()) {

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
                        new SpeciesTO("21", null, null, null, null, null, null, null, null, null, null),
                        new SpeciesTO("11", null, null, null, null, null, null, null, null, null, null),
                        new SpeciesTO("30", null, null, null, null, null, null, null, null, null, null)),
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
        
        try (MockDAOManager mockManager = new MockDAOManager()) {
            
            RelationTOResultSet mockRelationTOResultSet = this.createMockDAOResultSet(
                    returnedRelTOs, MySQLRelationTOResultSet.class);
            when(mockManager.getRelationDAO().getAnatEntityRelationsBySpeciesIds(
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
            verify(mockManager.getRelationDAO()).setAttributes(RelationDAO.Attribute.SOURCE_ID, 
                    RelationDAO.Attribute.TARGET_ID);
            verify(mockRelationTOResultSet).close();
        }
        
        try (MockDAOManager mockManager = new MockDAOManager()) {
            RelationTOResultSet mockRelationTOResultSet = this.createMockDAOResultSet(
                    returnedRelTOs, MySQLRelationTOResultSet.class);
            when(mockManager.getRelationDAO().getAnatEntityRelationsBySpeciesIds(
                    new HashSet<String>(Arrays.asList("1", "2")), 
                    EnumSet.of(RelationType.ISA_PARTOF), null)).thenReturn(mockRelationTOResultSet);
            
            Map<String, Set<String>> expectedReturnedVal = new HashMap<String, Set<String>>();
            expectedReturnedVal.put("1", new HashSet<String>(Arrays.asList("1")));
            expectedReturnedVal.put("2", new HashSet<String>(Arrays.asList("2", "1")));
            expectedReturnedVal.put("3", new HashSet<String>(Arrays.asList("3", "1", "5")));
            expectedReturnedVal.put("4", new HashSet<String>(Arrays.asList("4", "3", "1")));
            expectedReturnedVal.put("5", new HashSet<String>(Arrays.asList("5")));
            
            assertEquals("Incorrect anat entity relatives by target", expectedReturnedVal, 
                    BgeeDBUtils.getAnatEntityParentsFromChildren(
                            new HashSet<String>(Arrays.asList("1", "2")), 
                            mockManager.getRelationDAO()));
            verify(mockManager.getRelationDAO()).setAttributes(RelationDAO.Attribute.SOURCE_ID, 
                    RelationDAO.Attribute.TARGET_ID);
            verify(mockRelationTOResultSet).close();
        }
    }
    
    /**
     * Test {@link BgeeDBUtils#getStageChildrenFromParents(Set, RelationDAO)} and 
     * {@link BgeeDBUtils#getStageParentsFromChildren(Set, RelationDAO)}
     */
    @Test
    public void shouldGetStageTargetsOrSources() {
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
        
        try (MockDAOManager mockManager = new MockDAOManager()) {
            
            RelationTOResultSet mockRelationTOResultSet = this.createMockDAOResultSet(
                    returnedRelTOs, MySQLRelationTOResultSet.class);
            when(mockManager.getRelationDAO().getStageRelationsBySpeciesIds(
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
            verify(mockManager.getRelationDAO()).setAttributes(RelationDAO.Attribute.SOURCE_ID, 
                    RelationDAO.Attribute.TARGET_ID);
            verify(mockRelationTOResultSet).close();
        }
        
        try (MockDAOManager mockManager = new MockDAOManager()) {
            
            RelationTOResultSet mockRelationTOResultSet = this.createMockDAOResultSet(
                    returnedRelTOs, MySQLRelationTOResultSet.class);
            when(mockManager.getRelationDAO().getStageRelationsBySpeciesIds(
                    new HashSet<String>(Arrays.asList("1", "2")), null)).thenReturn(
                            mockRelationTOResultSet);
            
            Map<String, Set<String>> expectedReturnedVal = new HashMap<String, Set<String>>();
            expectedReturnedVal.put("1", new HashSet<String>(Arrays.asList("1")));
            expectedReturnedVal.put("2", new HashSet<String>(Arrays.asList("1", "2")));
            expectedReturnedVal.put("3", new HashSet<String>(Arrays.asList("1", "3")));
            expectedReturnedVal.put("4", new HashSet<String>(Arrays.asList("1", "3", "4")));
            
            assertEquals("Incorrect stage relatives by source", expectedReturnedVal, 
                    BgeeDBUtils.getStageParentsFromChildren(
                            new HashSet<String>(Arrays.asList("1", "2")), 
                            mockManager.getRelationDAO()));
            verify(mockManager.getRelationDAO()).setAttributes(RelationDAO.Attribute.SOURCE_ID, 
                    RelationDAO.Attribute.TARGET_ID);
            verify(mockRelationTOResultSet).close();
        }
    }
    
    /**
     * Test {@link BgeeDBUtils#getGeneNamesByIds(Set, GeneDAO)}.
     */
    @Test
    public void shouldGetGeneNamesByIds() {
        try (MockDAOManager mockManager = new MockDAOManager()) {
            List<GeneTO> returnedGeneTOs = Arrays.asList(
                    new GeneTO("1", "gene A", null), 
                    new GeneTO("2", "gene B", null), 
                    new GeneTO("3", "gene C", null));
            
            GeneTOResultSet mockRS = this.createMockDAOResultSet(
                    returnedGeneTOs, MySQLGeneTOResultSet.class);
            when(mockManager.getGeneDAO().getGenesBySpeciesIds(
                    new HashSet<String>(Arrays.asList("1", "2")))).thenReturn(
                            mockRS);
            
            Map<String, String> expectedReturnedVal = new HashMap<String, String>();
            expectedReturnedVal.put("1", "gene A");
            expectedReturnedVal.put("2", "gene B");
            expectedReturnedVal.put("3", "gene C");
            
            assertEquals("Incorrect ID-name mapping", expectedReturnedVal, 
                    BgeeDBUtils.getGeneNamesByIds(
                            new HashSet<String>(Arrays.asList("1", "2")), 
                            mockManager.getGeneDAO()));
            verify(mockManager.getGeneDAO()).setAttributes(GeneDAO.Attribute.ID, 
                    GeneDAO.Attribute.NAME);
            verify(mockRS).close();
        }
        
        try (MockDAOManager mockManager = new MockDAOManager()) {
            List<GeneTO> returnedGeneTOs = Arrays.asList(
                    new GeneTO("1", "gene A", null), 
                    new GeneTO(null, "gene B", null));
            
            GeneTOResultSet mockRS = this.createMockDAOResultSet(
                    returnedGeneTOs, MySQLGeneTOResultSet.class);
            when(mockManager.getGeneDAO().getGenesBySpeciesIds(
                    new HashSet<String>(Arrays.asList("1", "2")))).thenReturn(
                            mockRS);
            
            try {
                //an IllegalArgumentException should be thrown, because no ID for a TO
                BgeeDBUtils.getGeneNamesByIds(
                        new HashSet<String>(Arrays.asList("1", "2")), 
                        mockManager.getGeneDAO());
                //test failed
                throw log.throwing(new AssertionError("No IllegalArgumentException was thrown " + 
                		"with no ID for a TO"));
            } catch (IllegalArgumentException e) {
                //test passed
            }
            verify(mockRS).close();
        }

        try (MockDAOManager mockManager = new MockDAOManager()) {
            List<GeneTO> returnedGeneTOs = Arrays.asList(
                    new GeneTO("1", "gene A", null), 
                    new GeneTO("1", "gene B", null));
            
            GeneTOResultSet mockRS = this.createMockDAOResultSet(
                    returnedGeneTOs, MySQLGeneTOResultSet.class);
            when(mockManager.getGeneDAO().getGenesBySpeciesIds(
                    new HashSet<String>(Arrays.asList("1", "2")))).thenReturn(
                            mockRS);
            
            try {
                //an IllegalStateException should be thrown, because several names are mapped 
                //to a same ID
                BgeeDBUtils.getGeneNamesByIds(
                        new HashSet<String>(Arrays.asList("1", "2")), 
                        mockManager.getGeneDAO());
                //test failed
                throw log.throwing(new AssertionError("No IllegalStateException was thrown " +
                        "with several names mapped to a same ID"));
            } catch (IllegalStateException e) {
                //test passed
            }
            verify(mockRS).close();
        }
    }
    
    /**
     * Test {@link BgeeDBUtils#getGeneTOsByIds(Set, GeneDAO)}.
     */
    @Test
    public void shouldGetGeneTOsByIds() {
        try (MockDAOManager mockManager = new MockDAOManager()) {
            
            GeneTO gene1 = new GeneTO("1", "gene A", "desc A", 1, 1, 1, true); 
            GeneTO gene2 = new GeneTO("2", "gene B", "desc B", 2, 1, 1, true); 
            GeneTO gene3 = new GeneTO("3", "gene C", "desc C", 1, 2, 2, true); 

            List<GeneTO> returnedGeneTOs = Arrays.asList(gene1, gene2, gene3);
            
            GeneTOResultSet mockRS = this.createMockDAOResultSet(
                    returnedGeneTOs, MySQLGeneTOResultSet.class);
            when(mockManager.getGeneDAO().getGenesBySpeciesIds(
                    new HashSet<String>(Arrays.asList("1", "2")))).thenReturn(
                            mockRS);
            
            Map<String, GeneTO> expectedReturnedVal = new HashMap<String, GeneTO>();
            expectedReturnedVal.put("1", gene1);
            expectedReturnedVal.put("2", gene2);
            expectedReturnedVal.put("3", gene3);
            
            assertEquals("Incorrect ID-TO mapping", expectedReturnedVal, 
                    BgeeDBUtils.getGeneTOsByIds(
                            new HashSet<String>(Arrays.asList("1", "2")), 
                            mockManager.getGeneDAO()));
            verify(mockRS).close();
        }
        
        try (MockDAOManager mockManager = new MockDAOManager()) {
            List<GeneTO> returnedGeneTOs = Arrays.asList(
                    new GeneTO("1", "gene A", null), 
                    new GeneTO(null, "gene B", null));
            
            GeneTOResultSet mockRS = this.createMockDAOResultSet(
                    returnedGeneTOs, MySQLGeneTOResultSet.class);
            when(mockManager.getGeneDAO().getGenesBySpeciesIds(
                    new HashSet<String>(Arrays.asList("1", "2")))).thenReturn(
                            mockRS);
            
            try {
                //an IllegalStateException should be thrown, because several names are mapped 
                //to a same ID
                BgeeDBUtils.getGeneNamesByIds(
                        new HashSet<String>(Arrays.asList("1", "2")), 
                        mockManager.getGeneDAO());
                //test failed
                fail("No IllegalArgumentException was thrown, TO doesn't allow to retrieve ID");
            } catch (IllegalArgumentException e) {
                //test passed
            }
            verify(mockRS).close();
        }

        try (MockDAOManager mockManager = new MockDAOManager()) {
            List<GeneTO> returnedGeneTOs = Arrays.asList(
                    new GeneTO("1", "gene A", "desc A", 1, 1, 1, true), 
                    new GeneTO("1", "gene B", "desc B", 1, 1, 1, true));
            
            GeneTOResultSet mockRS = this.createMockDAOResultSet(
                    returnedGeneTOs, MySQLGeneTOResultSet.class);
            when(mockManager.getGeneDAO().getGenesBySpeciesIds(
                    new HashSet<String>(Arrays.asList("1", "2")))).thenReturn(
                            mockRS);
            
            try {
                //an IllegalStateException should be thrown, because several TOs are mapped 
                //to a same ID
                BgeeDBUtils.getGeneTOsByIds(
                        new HashSet<String>(Arrays.asList("1", "2")), 
                        mockManager.getGeneDAO());
                //test failed
                fail("No IllegalStateException was thrown with several TOs mapped to a same ID");
            } catch (IllegalStateException e) {
                //test passed
            }
            verify(mockRS).close();
        }
    }
    
    /**
     * Test {@link BgeeDBUtils#getStageNamesByIds(Set, StageDAO)}.
     */
    @Test
    public void shouldGetStageNamesByIds() {
        try (MockDAOManager mockManager = new MockDAOManager()) {
            List<StageTO> returnedStageTOs = Arrays.asList(
                    new StageTO("1", "stage A", null, null, null, null, null, null), 
                    new StageTO("2", "stage B", null, null, null, null, null, null), 
                    new StageTO("3", "stage C", null, null, null, null, null, null));
            
            StageTOResultSet mockRS = this.createMockDAOResultSet(
                    returnedStageTOs, MySQLStageTOResultSet.class);
            when(mockManager.getStageDAO().getStagesBySpeciesIds(
                    new HashSet<String>(Arrays.asList("1", "2")))).thenReturn(
                            mockRS);
            
            Map<String, String> expectedReturnedVal = new HashMap<String, String>();
            expectedReturnedVal.put("1", "stage A");
            expectedReturnedVal.put("2", "stage B");
            expectedReturnedVal.put("3", "stage C");
            
            assertEquals("Incorrect ID-name mapping", expectedReturnedVal, 
                    BgeeDBUtils.getStageNamesByIds(
                            new HashSet<String>(Arrays.asList("1", "2")), 
                            mockManager.getStageDAO()));
            verify(mockManager.getStageDAO()).setAttributes(StageDAO.Attribute.ID, 
                    StageDAO.Attribute.NAME);
            verify(mockRS).close();
        }
        
        try (MockDAOManager mockManager = new MockDAOManager()) {
            List<StageTO> returnedStageTOs = Arrays.asList(
                    new StageTO("1", null, null, null, null, null, null, null), 
                    new StageTO("2", "stage B", null, null, null, null, null, null));
            
            StageTOResultSet mockRS = this.createMockDAOResultSet(
                    returnedStageTOs, MySQLStageTOResultSet.class);
            when(mockManager.getStageDAO().getStagesBySpeciesIds(
                    new HashSet<String>(Arrays.asList("1", "2")))).thenReturn(
                            mockRS);
            
            try {
                //an IllegalArgumentException should be thrown, because no name for the first TO
                BgeeDBUtils.getStageNamesByIds(
                        new HashSet<String>(Arrays.asList("1", "2")), 
                        mockManager.getStageDAO());
                //test failed
                throw log.throwing(new AssertionError("No IllegalArgumentException was thrown " +
                        "with no name for the TO"));
            } catch (IllegalArgumentException e) {
                //test passed
            }
            verify(mockRS).close();
        }
        
        try (MockDAOManager mockManager = new MockDAOManager()) {
            List<StageTO> returnedStageTOs = Arrays.asList(
                    new StageTO("1", "stage A", null, null, null, null, null, null), 
                    new StageTO("1", "stage B", null, null, null, null, null, null));
            
            StageTOResultSet mockRS = this.createMockDAOResultSet(
                    returnedStageTOs, MySQLStageTOResultSet.class);
            when(mockManager.getStageDAO().getStagesBySpeciesIds(
                    new HashSet<String>(Arrays.asList("1", "2")))).thenReturn(
                            mockRS);
            
            try {
                //an IllegalStateException should be thrown, because several names are mapped 
                //to a same ID
                BgeeDBUtils.getStageNamesByIds(
                        new HashSet<String>(Arrays.asList("1", "2")), 
                        mockManager.getStageDAO());
                //test failed
                throw log.throwing(new AssertionError("No IllegalStateException was thrown " +
                        "with several names mapped to a same ID"));
            } catch (IllegalStateException e) {
                //test passed
            }
            verify(mockRS).close();
        }
    }
    
    /**
     * Test {@link BgeeDBUtils#getAnatEntityNamesByIds(Set, AnatEntityDAO)}.
     */
    @Test
    public void shouldGetAnatEntityNamesByIds() {
        try (MockDAOManager mockManager = new MockDAOManager()) {
            List<AnatEntityTO> returnedAnatEntityTOs = Arrays.asList(
                    new AnatEntityTO("1", "anatEntity A", null, null, null, null), 
                    new AnatEntityTO("2", "anatEntity B", null, null, null, null), 
                    new AnatEntityTO("3", "anatEntity C", null, null, null, null));
            
            AnatEntityTOResultSet mockRS = this.createMockDAOResultSet(
                    returnedAnatEntityTOs, MySQLAnatEntityTOResultSet.class);
            when(mockManager.getAnatEntityDAO().getAnatEntitiesBySpeciesIds(
                    new HashSet<String>(Arrays.asList("1", "2")))).thenReturn(
                            mockRS);
            
            Map<String, String> expectedReturnedVal = new HashMap<String, String>();
            expectedReturnedVal.put("1", "anatEntity A");
            expectedReturnedVal.put("2", "anatEntity B");
            expectedReturnedVal.put("3", "anatEntity C");
            
            assertEquals("Incorrect ID-name mapping", expectedReturnedVal, 
                    BgeeDBUtils.getAnatEntityNamesByIds(
                            new HashSet<String>(Arrays.asList("1", "2")), 
                            mockManager.getAnatEntityDAO()));
            verify(mockManager.getAnatEntityDAO()).setAttributes(AnatEntityDAO.Attribute.ID, 
                    AnatEntityDAO.Attribute.NAME);
            verify(mockRS).close();
        }
        
        try (MockDAOManager mockManager = new MockDAOManager()) {
            List<AnatEntityTO> returnedAnatEntityTOs = Arrays.asList(
                    new AnatEntityTO(null, null, null, null, null, null), 
                    new AnatEntityTO("1", "anatEntity B", null, null, null, null));
            
            AnatEntityTOResultSet mockRS = this.createMockDAOResultSet(
                    returnedAnatEntityTOs, MySQLAnatEntityTOResultSet.class);
            when(mockManager.getAnatEntityDAO().getAnatEntitiesBySpeciesIds(
                    new HashSet<String>(Arrays.asList("1", "2")))).thenReturn(
                            mockRS);
            
            try {
                //an IllegalArgumentException should be thrown, because no name and ID for a TO
                BgeeDBUtils.getAnatEntityNamesByIds(
                        new HashSet<String>(Arrays.asList("1", "2")), 
                        mockManager.getAnatEntityDAO());
                //test failed
                throw log.throwing(new AssertionError("No IllegalArgumentException was thrown " +
                        "with no name for the TO"));
            } catch (IllegalArgumentException e) {
                //test passed
            }
            verify(mockRS).close();
        }
        
        try (MockDAOManager mockManager = new MockDAOManager()) {
            List<AnatEntityTO> returnedAnatEntityTOs = Arrays.asList(
                    new AnatEntityTO("1", "anatEntity A", null, null, null, null), 
                    new AnatEntityTO("1", "anatEntity B", null, null, null, null));
            
            AnatEntityTOResultSet mockRS = this.createMockDAOResultSet(
                    returnedAnatEntityTOs, MySQLAnatEntityTOResultSet.class);
            when(mockManager.getAnatEntityDAO().getAnatEntitiesBySpeciesIds(
                    new HashSet<String>(Arrays.asList("1", "2")))).thenReturn(
                            mockRS);
            
            try {
                //an IllegalStateException should be thrown, because several names are mapped 
                //to a same ID
                BgeeDBUtils.getAnatEntityNamesByIds(
                        new HashSet<String>(Arrays.asList("1", "2")), 
                        mockManager.getAnatEntityDAO());
                //test failed
                throw log.throwing(new AssertionError("No IllegalStateException was thrown " +
                        "with several names mapped to a same ID"));
            } catch (IllegalStateException e) {
                //test passed
            }
            verify(mockRS).close();
        }
    }
    
    /**
     * Test {@link BgeeDBUtils#getCIOStatementNamesByIds(CIOStatementDAO)}.
     */
    @Test
    public void shouldGetCIOStatementNamesByIds() {
        try (MockDAOManager mockManager = new MockDAOManager()) {
            CIOStatementTO cio1 = new CIOStatementTO("CIO:1", "name1", "desc1", true, 
                    ConfidenceLevel.HIGH_CONFIDENCE, EvidenceConcordance.CONGRUENT, 
                    EvidenceTypeConcordance.SAME_TYPE); 
            CIOStatementTO cio2 = new CIOStatementTO("CIO:2", "name2", "desc2", false, 
                    ConfidenceLevel.LOW_CONFIDENCE, EvidenceConcordance.SINGLE_EVIDENCE, null); 
            CIOStatementTO cio3 = new CIOStatementTO("CIO:3", "name3", null, true, 
                    ConfidenceLevel.MEDIUM_CONFIDENCE, EvidenceConcordance.STRONGLY_CONFLICTING, 
                    EvidenceTypeConcordance.DIFFERENT_TYPE); 

            List<CIOStatementTO> returnedCIOTOs = Arrays.asList(cio1, cio2, cio3);
            
            CIOStatementTOResultSet mockRS = this.createMockDAOResultSet(
                    returnedCIOTOs, MySQLCIOStatementTOResultSet.class);
            when(mockManager.getCIOStatementDAO().getAllCIOStatements()).thenReturn(mockRS);
            
            Map<String, String> expectedReturnedVal = new HashMap<String, String>();
            expectedReturnedVal.put("CIO:1", "name1");
            expectedReturnedVal.put("CIO:2", "name2");
            expectedReturnedVal.put("CIO:3", "name3");
            
            assertEquals("Incorrect ID-name mapping", expectedReturnedVal, 
                    BgeeDBUtils.getCIOStatementNamesByIds(mockManager.getCIOStatementDAO()));
            verify(mockRS).close();
        }
        
        try (MockDAOManager mockManager = new MockDAOManager()) {
            List<CIOStatementTO> returnedCIOTOs = Arrays.asList(
                    new CIOStatementTO("CIO:1", "name1", "desc1", true, 
                            ConfidenceLevel.HIGH_CONFIDENCE, EvidenceConcordance.CONGRUENT, 
                            EvidenceTypeConcordance.SAME_TYPE), 
                    new CIOStatementTO(null, "name2", "desc1", true, 
                            ConfidenceLevel.HIGH_CONFIDENCE, EvidenceConcordance.SINGLE_EVIDENCE, 
                            EvidenceTypeConcordance.SAME_TYPE));
            
            CIOStatementTOResultSet mockRS = this.createMockDAOResultSet(
                    returnedCIOTOs, MySQLCIOStatementTOResultSet.class);
            when(mockManager.getCIOStatementDAO().getAllCIOStatements()).thenReturn(mockRS);
                        
            try {
                // an IllegalArgumentException should be thrown, because a TO 
            	// does not allow to retrieve EntityTO IDs
                BgeeDBUtils.getCIOStatementNamesByIds(mockManager.getCIOStatementDAO());
                // test failed
                fail("No IllegalArgumentException was thrown, TO doesn't allow to retrieve ID or name");
            } catch (IllegalArgumentException e) {
                // test passed
            }
            verify(mockRS).close();
        }
        
        try (MockDAOManager mockManager = new MockDAOManager()) {
            List<CIOStatementTO> returnedCIOTOs = Arrays.asList(
                    new CIOStatementTO("CIO:1", "name1", "desc1", true, 
                            ConfidenceLevel.HIGH_CONFIDENCE, EvidenceConcordance.CONGRUENT, 
                            EvidenceTypeConcordance.SAME_TYPE), 
                    new CIOStatementTO("CIO:1", "name2", "desc1", true, 
                            ConfidenceLevel.HIGH_CONFIDENCE, EvidenceConcordance.SINGLE_EVIDENCE, 
                            EvidenceTypeConcordance.SAME_TYPE));
            
            CIOStatementTOResultSet mockRS = this.createMockDAOResultSet(
                    returnedCIOTOs, MySQLCIOStatementTOResultSet.class);
            when(mockManager.getCIOStatementDAO().getAllCIOStatements()).thenReturn(mockRS);
            
            try {
                //an IllegalStateException should be thrown, because several TOs are mapped 
                //to a same ID
                BgeeDBUtils.getCIOStatementNamesByIds(mockManager.getCIOStatementDAO());
                //test failed
                fail("No IllegalStateException was thrown with several names mapped to a same ID");
            } catch (IllegalStateException e) {
                //test passed
            }
            verify(mockRS).close();
        }

    }
    /**
     * Test {@link BgeeDBUtils#getCIOStatementTOsByIds(Set, CIOStatementDAO)}.
     */
    @Test
    public void shouldGetCIOStatementTOsByIds() {
        try (MockDAOManager mockManager = new MockDAOManager()) {
            CIOStatementTO cio1 = new CIOStatementTO("CIO:1", "name1", "desc1", true, 
                    ConfidenceLevel.HIGH_CONFIDENCE, EvidenceConcordance.CONGRUENT, 
                    EvidenceTypeConcordance.SAME_TYPE); 
            CIOStatementTO cio2 = new CIOStatementTO("CIO:2", "name2", "desc2", false, 
                    ConfidenceLevel.LOW_CONFIDENCE, EvidenceConcordance.SINGLE_EVIDENCE, null); 
            CIOStatementTO cio3 = new CIOStatementTO("CIO:3", "name3", null, true, 
                    ConfidenceLevel.MEDIUM_CONFIDENCE, EvidenceConcordance.STRONGLY_CONFLICTING, 
                    EvidenceTypeConcordance.DIFFERENT_TYPE); 

            List<CIOStatementTO> returnedCIOTOs = Arrays.asList(cio1, cio2, cio3);
            
            CIOStatementTOResultSet mockRS = this.createMockDAOResultSet(
                    returnedCIOTOs, MySQLCIOStatementTOResultSet.class);
            when(mockManager.getCIOStatementDAO().getAllCIOStatements()).thenReturn(mockRS);
            
            Map<String, CIOStatementTO> expectedReturnedVal = new HashMap<String, CIOStatementTO>();
            expectedReturnedVal.put("CIO:1", cio1);
            expectedReturnedVal.put("CIO:2", cio2);
            expectedReturnedVal.put("CIO:3", cio3);
            
            assertEquals("Incorrect ID-TO mapping", expectedReturnedVal, 
                    BgeeDBUtils.getCIOStatementTOsByIds(mockManager.getCIOStatementDAO()));
            verify(mockRS).close();
        }
        
        try (MockDAOManager mockManager = new MockDAOManager()) {
            List<CIOStatementTO> returnedCIOTOs = Arrays.asList(
                    new CIOStatementTO("CIO:1", "name1", "desc1", true, 
                            ConfidenceLevel.HIGH_CONFIDENCE, EvidenceConcordance.CONGRUENT, 
                            EvidenceTypeConcordance.SAME_TYPE), 
                    new CIOStatementTO(null, "name2", "desc1", true, 
                            ConfidenceLevel.HIGH_CONFIDENCE, EvidenceConcordance.SINGLE_EVIDENCE, 
                            EvidenceTypeConcordance.SAME_TYPE));
            
            CIOStatementTOResultSet mockRS = this.createMockDAOResultSet(
                    returnedCIOTOs, MySQLCIOStatementTOResultSet.class);
            when(mockManager.getCIOStatementDAO().getAllCIOStatements()).thenReturn(mockRS);
            
            try {
                // an IllegalArgumentException should be thrown, because a TO 
            	// does not allow to retrieve EntityTO IDs
                BgeeDBUtils.getCIOStatementTOsByIds(mockManager.getCIOStatementDAO());
                // test failed
                fail("No IllegalArgumentException was thrown, TO doesn't allow to retrieve ID");
            } catch (IllegalArgumentException e) {
                // test passed
            }
            verify(mockRS).close();
        }
        
        try (MockDAOManager mockManager = new MockDAOManager()) {
            List<CIOStatementTO> returnedCIOTOs = Arrays.asList(
                    new CIOStatementTO("CIO:1", "name1", "desc1", true, 
                            ConfidenceLevel.HIGH_CONFIDENCE, EvidenceConcordance.CONGRUENT, 
                            EvidenceTypeConcordance.SAME_TYPE), 
                    new CIOStatementTO("CIO:1", "name2", "desc1", true, 
                            ConfidenceLevel.HIGH_CONFIDENCE, EvidenceConcordance.SINGLE_EVIDENCE, 
                            EvidenceTypeConcordance.SAME_TYPE));
            
            CIOStatementTOResultSet mockRS = this.createMockDAOResultSet(
                    returnedCIOTOs, MySQLCIOStatementTOResultSet.class);
            when(mockManager.getCIOStatementDAO().getAllCIOStatements()).thenReturn(mockRS);
            
            try {
                //an IllegalStateException should be thrown, because several TOs are mapped 
                //to a same ID
                BgeeDBUtils.getCIOStatementTOsByIds(mockManager.getCIOStatementDAO());
                //test failed
                fail("No IllegalStateException was thrown, several TOs mapped to a same ID");
            } catch (IllegalStateException e) {
                //test passed
            }
            verify(mockRS).close();
        }

    }
}
