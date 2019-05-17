package org.bgee.model.anatdev;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bgee.model.ServiceFactory;
import org.bgee.model.TestAncestor;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTO;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTOResultSet;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SimAnnotToAnatEntityTO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SimAnnotToAnatEntityTOResultSet;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SummarySimilarityAnnotationTO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SummarySimilarityAnnotationTOResultSet;
import org.junit.Test;

/**
 * This class holds the unit tests for the {@code AnatEntityService} class.
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13, Nov. 2015
 * @since   Bgee 13, Nov. 2015
 */
public class AnatEntityServiceTest extends TestAncestor {
    
    @Test
    public void shouldLoadAnatEntitiesBySpeciesIds() {
        // initialize mocks
        DAOManager managerMock = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
        AnatEntityDAO dao = mock(AnatEntityDAO.class);
        when(managerMock.getAnatEntityDAO()).thenReturn(dao);
        List<AnatEntityTO> anatEntityTOs = Arrays.asList(
                new AnatEntityTO("UBERON:0001687", "stapes bone",
                        "stapes bone description", "Stage_id1", "Stage_id2", false),
                new AnatEntityTO("UBERON:0001853", "utricle of membranous labyrinth", 
                        "utricle of membranous labyrinth description", "Stage_id1", "Stage_id2", false),
                new AnatEntityTO("UBERON:0011606", "hyomandibular bone", 
                        "hyomandibular bone description", "Stage_id1", "Stage_id2", false));

        // Filter on species IDs is not tested here (tested in AnatEntityDAO)
        // but we need a variable to mock DAO answer
        Set<Integer> speciesIds = new HashSet<>();
        speciesIds.add(11);

        AnatEntityTOResultSet mockAnatEntRs1 = 
                getMockResultSet(AnatEntityTOResultSet.class, anatEntityTOs);
        when(dao.getAnatEntities(eq(speciesIds), eq(true), anyObject(), anyObject()))
            .thenReturn(mockAnatEntRs1);
        when(dao.getAnatEntitiesBySpeciesIds(speciesIds)).thenReturn(mockAnatEntRs1);
        
        // Test without defined level
        List<AnatEntity> expectedAnatEntity = Arrays.asList(
                new AnatEntity("UBERON:0001687",  "stapes bone",  "stapes bone description"), 
                new AnatEntity("UBERON:0001853", "utricle of membranous labyrinth", "utricle of membranous labyrinth description"), 
                new AnatEntity("UBERON:0011606", "hyomandibular bone", "hyomandibular bone description"));
        AnatEntityService service = new AnatEntityService(serviceFactory);
        assertEquals("Incorrect anat. entities", expectedAnatEntity,
                service.loadAnatEntitiesBySpeciesIds(speciesIds).collect(Collectors.toList()));
    }

    /**
     * Test the method {@link AnatEntityService#loadAnatEntities(Collection, Boolean, Collection)}
     */
    @Test
    public void shouldLoadAnatEntities() {
        // initialize mocks
        DAOManager managerMock = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
        AnatEntityDAO dao = mock(AnatEntityDAO.class);
        when(managerMock.getAnatEntityDAO()).thenReturn(dao);
        List<AnatEntityTO> anatEntityTOs = Arrays.asList(
                new AnatEntityTO("UBERON:0001687", "stapes bone",
                        "stapes bone description", "Stage_id1", "Stage_id2", false),
                new AnatEntityTO("UBERON:0001853", "utricle of membranous labyrinth", 
                        "utricle of membranous labyrinth description", "Stage_id1", "Stage_id2", false),
                new AnatEntityTO("UBERON:0011606", "hyomandibular bone", 
                        "hyomandibular bone description", "Stage_id1", "Stage_id2", false));

        // Filter on species IDs is not tested here (tested in AnatEntityDAO)
        // but we need a variable to mock DAO answer
        Set<Integer> speciesIds = new HashSet<>();
        speciesIds.add(11);
        Set<String> anatEntityIds = new HashSet<>(Arrays.asList(
                "UBERON:0001687", "UBERON:0001853", "UBERON:0011606"));

        AnatEntityTOResultSet mockAnatEntRs1 = 
                getMockResultSet(AnatEntityTOResultSet.class, anatEntityTOs);
        when(dao.getAnatEntities(eq(speciesIds), eq(true), eq(anatEntityIds), anyObject()))
            .thenReturn(mockAnatEntRs1);
        
        // Test without defined level
        List<AnatEntity> expectedAnatEntity = Arrays.asList(
                new AnatEntity("UBERON:0001687",  "stapes bone",  "stapes bone description"), 
                new AnatEntity("UBERON:0001853", "utricle of membranous labyrinth", "utricle of membranous labyrinth description"), 
                new AnatEntity("UBERON:0011606", "hyomandibular bone", "hyomandibular bone description"));
        AnatEntityService service = new AnatEntityService(serviceFactory);
        assertEquals("Incorrect anat. entities", expectedAnatEntity,
                service.loadAnatEntities(speciesIds, true, anatEntityIds, true).collect(Collectors.toList()));
    }
    
    /**
     * Test the method {@link AnatEntityService#loadNonInformativeAnatEntitiesBySpeciesIds(Collection)}
     */
    @Test
    public void shouldLoadNonInformativeAnatEntitiesBySpeciesIds() {
        // initialize mocks
        DAOManager managerMock = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
        AnatEntityDAO dao = mock(AnatEntityDAO.class);
        when(managerMock.getAnatEntityDAO()).thenReturn(dao);
        List<AnatEntityTO> anatEntityTOs = Arrays.asList(
                new AnatEntityTO("UBERON:0001687", "stapes bone",
                        "stapes bone description", "Stage_id1", "Stage_id2", false),
                new AnatEntityTO("UBERON:0011606", "hyomandibular bone", 
                        "hyomandibular bone description", "Stage_id1", "Stage_id2", false));

        // Filter on species IDs is not tested here (tested in AnatEntityDAO)
        // but we need a variable to mock DAO answer
        Set<Integer> speciesIds = new HashSet<>();
        speciesIds.add(11);

        AnatEntityTOResultSet mockAnatEntRs1 = 
                getMockResultSet(AnatEntityTOResultSet.class, anatEntityTOs);
        when(dao.getNonInformativeAnatEntitiesBySpeciesIds(speciesIds)).thenReturn(mockAnatEntRs1);
        
        // Test without defined level
        List<AnatEntity> expectedAnatEntity = Arrays.asList(
                new AnatEntity("UBERON:0001687",  "stapes bone",  "stapes bone description"), 
                new AnatEntity("UBERON:0011606", "hyomandibular bone", "hyomandibular bone description"));
        AnatEntityService service = new AnatEntityService(serviceFactory);
        assertEquals("Incorrect anat. entities", expectedAnatEntity,
                service.loadNonInformativeAnatEntitiesBySpeciesIds(speciesIds).collect(Collectors.toList()));
    }
    
//    /**
//     * Test the method {@link AnatEntityService#loadAnatEntitySimilarities(String, Set, boolean)}.
//     */
//    @Test
//    public void shouldLoadAnatEntitySimilarities() {
//        DAOManager managerMock = mock(DAOManager.class);
//        ServiceFactory serviceFactory = mock(ServiceFactory.class);
//        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
//        SummarySimilarityAnnotationDAO dao = mock(SummarySimilarityAnnotationDAO.class);
//        when(managerMock.getSummarySimilarityAnnotationDAO()).thenReturn(dao);
//        
//        SummarySimilarityAnnotationTOResultSet resultSetSim = getMockResultSet(
//                SummarySimilarityAnnotationTOResultSet.class, 
//                Arrays.asList(new SummarySimilarityAnnotationTO("sim1", 1, false, "cio01"),
//                  new SummarySimilarityAnnotationTO("sim2", 1, false, "cio01")));
//
//        SimAnnotToAnatEntityTOResultSet resultSetSimToAnat = getMockResultSet(
//                SimAnnotToAnatEntityTOResultSet.class, 
//                Arrays.asList(new SimAnnotToAnatEntityTO("sim1", "anat1"),
//               new SimAnnotToAnatEntityTO("sim1", "anat2"),
//                new SimAnnotToAnatEntityTO("sim2", "anat3")
//        ));
//       
//        when(dao.getSummarySimilarityAnnotations(1,true)).thenReturn(resultSetSim);
//        when(dao.getSimAnnotToAnatEntity(1, null)).thenReturn(resultSetSimToAnat);
//        
//        AnatEntityService service = new AnatEntityService(serviceFactory);
//        Collection<AnatEntitySimilarity> expected = new HashSet<>(Arrays.asList(
//                new AnatEntitySimilarity("sim1", new HashSet<>(Arrays.asList("anat1","anat2"))),
//                new AnatEntitySimilarity("sim2", new HashSet<>(Arrays.asList("anat3")))
//                ));
//        
//        Collection<AnatEntitySimilarity> actual = service.loadAnatEntitySimilarities(1, null, true);
//        assertEquals(expected, actual);
//        
//    }


}
