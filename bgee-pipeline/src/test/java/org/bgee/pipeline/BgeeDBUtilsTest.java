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
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTO;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTOResultSet;
import org.bgee.model.dao.api.anatdev.StageDAO;
import org.bgee.model.dao.api.anatdev.StageDAO.StageTO;
import org.bgee.model.dao.api.anatdev.StageDAO.StageTOResultSet;
import org.bgee.model.dao.api.expressiondata.ExpressionCallParams;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTOResultSet;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTOResultSet;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallParams;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTOResultSet;
import org.bgee.model.dao.api.ontologycommon.RelationDAO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTOResultSet;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationType;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTOResultSet;
import org.bgee.model.dao.mysql.anatdev.MySQLAnatEntityDAO.MySQLAnatEntityTOResultSet;
import org.bgee.model.dao.mysql.anatdev.MySQLStageDAO.MySQLStageTOResultSet;
import org.bgee.model.dao.mysql.expressiondata.MySQLExpressionCallDAO.MySQLExpressionCallTOResultSet;
import org.bgee.model.dao.mysql.expressiondata.MySQLNoExpressionCallDAO.MySQLNoExpressionCallTOResultSet;
import org.bgee.model.dao.mysql.gene.MySQLGeneDAO.MySQLGeneTOResultSet;
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
        }
        
        try (MockDAOManager mockManager = new MockDAOManager()) {
            RelationTOResultSet mockRelationTOResultSet = this.createMockDAOResultSet(
                    returnedRelTOs, MySQLRelationTOResultSet.class);
            when(mockManager.getRelationDAO().getAnatEntityRelations(
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
            verify(mockManager.getRelationDAO()).setAttributes(RelationDAO.Attribute.SOURCEID, 
                    RelationDAO.Attribute.TARGETID);
            verify(mockRelationTOResultSet).close();
        }
    }
    
    /**
     * Test {@link BgeeDBUtils#getStageChildrenFromParents(Set, RelationDAO)}.
     */
    @Test
    public void shouldGetStageTargetsBySources() {
        try (MockDAOManager mockManager = new MockDAOManager()) {
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

    /**
     * Test {@link BgeeDBUtils#getExpressionCallsByGeneId(Set, ExpressionCallDAO)}.
     */
    @Test
    public void shouldGetExpressionCallsByGeneId() {
        try (MockDAOManager mockManager = new MockDAOManager()) {
        
            Map<String, Set<ExpressionCallTO>> expectedMap = 
                    new HashMap<String, Set<ExpressionCallTO>>();
            Set<ExpressionCallTO> Id1ExprSet = new HashSet<ExpressionCallTO>();
            Id1ExprSet.addAll(Arrays.asList(
                    new ExpressionCallTO("1", "ID1", "Anat_id4", "Stage_id6", 
                            DataState.NODATA, DataState.LOWQUALITY, 
                            DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                            false, false, ExpressionCallTO.OriginOfLine.SELF, 
                            ExpressionCallTO.OriginOfLine.SELF, true),
                    new ExpressionCallTO("2", "ID1", "Anat_id5", "Stage_id6", 
                             DataState.HIGHQUALITY, DataState.NODATA, 
                             DataState.NODATA, DataState.LOWQUALITY, 
                             false, false, ExpressionCallTO.OriginOfLine.SELF, 
                             ExpressionCallTO.OriginOfLine.SELF, true),
                    new ExpressionCallTO("3", "ID1", "Anat_id3", "Stage_id1", 
                             DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                             DataState.NODATA, DataState.LOWQUALITY, 
                             false, false, ExpressionCallTO.OriginOfLine.SELF, 
                             ExpressionCallTO.OriginOfLine.SELF, true)));
            expectedMap.put("ID1", Id1ExprSet);
            
            Set<ExpressionCallTO> Id2ExprSet = new HashSet<ExpressionCallTO>();
            Id2ExprSet.addAll(Arrays.asList(
                    new ExpressionCallTO("4", "ID2", "Anat_id4", "Stage_id7", 
                            DataState.LOWQUALITY, DataState.NODATA, 
                            DataState.HIGHQUALITY, DataState.NODATA, 
                            false, false, ExpressionCallTO.OriginOfLine.SELF, 
                            ExpressionCallTO.OriginOfLine.SELF, true),
                    new ExpressionCallTO("5", "ID2", "Anat_id1", "Stage_id7", 
                            DataState.NODATA, DataState.HIGHQUALITY, 
                            DataState.LOWQUALITY, DataState.LOWQUALITY, 
                            false, false, ExpressionCallTO.OriginOfLine.SELF, 
                            ExpressionCallTO.OriginOfLine.SELF, true)));
            expectedMap.put("ID2", Id2ExprSet);
            
            List<ExpressionCallTO> allTOs = new ArrayList<ExpressionCallTO>(Id1ExprSet);
            allTOs.addAll(Id2ExprSet);
            
            ExpressionCallTOResultSet mockExprResultSet = this.createMockDAOResultSet(
                    allTOs, MySQLExpressionCallTOResultSet.class);
            ExpressionCallParams params = new ExpressionCallParams();
            params.addAllSpeciesIds(Arrays.asList("11", "21"));
            when(mockManager.mockExpressionCallDAO.getExpressionCalls(
                    (ExpressionCallParams) BgeeDBUtilsTest.valueCallParamEq(params))).thenReturn(mockExprResultSet);
            
            Map<String, List<ExpressionCallTO>> returnedMap = BgeeDBUtils.getExpressionCallsByGeneId(
                    new HashSet<String>(Arrays.asList("11", "21")),
                    mockManager.getExpressionCallDAO());
            
            assertEquals("Maps not equal, different sizes", expectedMap.size() , returnedMap.size());
            
            for(String id : expectedMap.keySet()) {
                if(returnedMap.containsKey(id)) {
                    assertTrue("Incorrect map generated: different values for " + id, 
                            TOComparator.areTOCollectionsEqual(expectedMap.get(id), returnedMap.get(id)));
                } else {
                    throw new AssertionError("Incorrect map generated: missing expected id " + id);
                }
            }
        }
    }
    
    /**
     * Test {@link BgeeDBUtils#getNoExpressionCallsByGeneId(Set, NoExpressionCallDAO)}.
     */
    @Test
    public void shouldGetNoExpressionCallsByGeneId() {
        try (MockDAOManager mockManager = new MockDAOManager()) {
        
            Map<String, Set<NoExpressionCallTO>> expectedMap = 
                    new HashMap<String, Set<NoExpressionCallTO>>();
            Set<NoExpressionCallTO> Id1NoExprSet = new HashSet<NoExpressionCallTO>();
            Id1NoExprSet.addAll(Arrays.asList(
                    new NoExpressionCallTO("2", "ID1", "Anat_id3", "Stage_id1", 
                            DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                            DataState.LOWQUALITY, false, NoExpressionCallTO.OriginOfLine.SELF),
                            new NoExpressionCallTO("3", "ID1", "Anat_id4", "Stage_id3", 
                                    DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                                    DataState.LOWQUALITY, false, NoExpressionCallTO.OriginOfLine.SELF),
                                    new NoExpressionCallTO("5", "ID1", "Anat_id5", "Stage_id3", 
                                            DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                                            DataState.LOWQUALITY, false, NoExpressionCallTO.OriginOfLine.SELF)));
            expectedMap.put("ID1", Id1NoExprSet);
            
            Set<NoExpressionCallTO> Id2NoExprSet = new HashSet<NoExpressionCallTO>();
            Id2NoExprSet.add(new NoExpressionCallTO("4", "ID2", "Anat_id4", "Stage_id3", 
                    DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                    DataState.NODATA, false, NoExpressionCallTO.OriginOfLine.SELF));
            expectedMap.put("ID2", Id2NoExprSet);
            
            Set<NoExpressionCallTO> Id3NoExprSet = new HashSet<NoExpressionCallTO>();
            Id3NoExprSet.add(new NoExpressionCallTO("1", "ID3", "Anat_id1", "Stage_id6", 
                    DataState.NODATA, DataState.LOWQUALITY, DataState.HIGHQUALITY, 
                    DataState.LOWQUALITY, false, NoExpressionCallTO.OriginOfLine.SELF));
            expectedMap.put("ID3", Id3NoExprSet);
            
            List<NoExpressionCallTO> allTOs = new ArrayList<NoExpressionCallTO>(Id1NoExprSet);
            allTOs.addAll(Id2NoExprSet);
            allTOs.addAll(Id3NoExprSet);
            
            NoExpressionCallTOResultSet mockNoExprResultSet = this.createMockDAOResultSet(
                    allTOs, MySQLNoExpressionCallTOResultSet.class);
            NoExpressionCallParams params = new NoExpressionCallParams();
            params.addAllSpeciesIds(Arrays.asList("11", "21"));
            when(mockManager.mockNoExpressionCallDAO.getNoExpressionCalls(
                    (NoExpressionCallParams) BgeeDBUtilsTest.valueCallParamEq(params))).
                    thenReturn(mockNoExprResultSet);
            
            Map<String, List<NoExpressionCallTO>> returnedMap = BgeeDBUtils.getNoExpressionCallsByGeneId(
                    new HashSet<String>(Arrays.asList("11", "21")),
                    mockManager.getNoExpressionCallDAO());
            
            assertEquals("Maps not equal, different sizes", expectedMap.size() , returnedMap.size());
            
            for(String id : expectedMap.keySet()) {
                if(returnedMap.containsKey(id)) {
                    assertTrue("Incorrect map generated: different values for " + id, 
                            TOComparator.areTOCollectionsEqual(expectedMap.get(id), returnedMap.get(id)));
                } else {
                    throw new AssertionError("Incorrect map generated: missing expected id " + id);
                }
            }
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
                    new GeneTO("3", "gene C", null), 
                    new GeneTO("3", "gene C", null));
            
            GeneTOResultSet mockRS = this.createMockDAOResultSet(
                    returnedGeneTOs, MySQLGeneTOResultSet.class);
            when(mockManager.getGeneDAO().getGenes(
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
                    new GeneTO("1", "gene B", null));
            
            GeneTOResultSet mockRS = this.createMockDAOResultSet(
                    returnedGeneTOs, MySQLGeneTOResultSet.class);
            when(mockManager.getGeneDAO().getGenes(
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
     * Test {@link BgeeDBUtils#getStageNamesByIds(Set, StageDAO)}.
     */
    @Test
    public void shouldGetStageNamesByIds() {
        try (MockDAOManager mockManager = new MockDAOManager()) {
            List<StageTO> returnedStageTOs = Arrays.asList(
                    new StageTO("1", "stage A", null, null, null, null, null, null), 
                    new StageTO("2", "stage B", null, null, null, null, null, null), 
                    new StageTO("3", "stage C", null, null, null, null, null, null), 
                    new StageTO("3", "stage C", null, null, null, null, null, null));
            
            StageTOResultSet mockRS = this.createMockDAOResultSet(
                    returnedStageTOs, MySQLStageTOResultSet.class);
            when(mockManager.getStageDAO().getStages(
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
                    new StageTO("1", "stage A", null, null, null, null, null, null), 
                    new StageTO("1", "stage B", null, null, null, null, null, null));
            
            StageTOResultSet mockRS = this.createMockDAOResultSet(
                    returnedStageTOs, MySQLStageTOResultSet.class);
            when(mockManager.getStageDAO().getStages(
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
                    new AnatEntityTO("3", "anatEntity C", null, null, null, null), 
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
}
