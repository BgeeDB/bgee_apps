package org.bgee.model.gene;

import org.bgee.model.Entity;
import org.bgee.model.ServiceFactory;
import org.bgee.model.TestAncestor;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTOResultSet;
import org.bgee.model.dao.api.gene.GeneXRefDAO;
import org.bgee.model.dao.api.gene.GeneXRefDAO.GeneXRefTO;
import org.bgee.model.dao.api.gene.GeneXRefDAO.GeneXRefTOResultSet;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalGroupToGeneTO;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalGroupToGeneTOResultSet;
import org.bgee.model.species.Species;
import org.bgee.model.species.SpeciesService;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This class holds the unit tests for the {@code GeneService} class.
 * 
 * @author  Valentine Rech de Laval
 * @author  Philippe Moret
 * @version Bgee 14, Apr. 2019
 * @since   Bgee 13, Nov. 2015
 */
public class GeneServiceTest extends TestAncestor {


    /**
     * Test {@link GeneService#loadGenes(Collection)}.
     */
    @Test
    public void shouldLoadGenes() {
        // initialize mocks
        DAOManager managerMock = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);

        // Mock SpeciesService
        Set<Integer> speciesIds = new HashSet<>(Arrays.asList(11, 22, 44));
        SpeciesService speciesService = mock(SpeciesService.class);
        when(serviceFactory.getSpeciesService()).thenReturn(speciesService);
        Set<Species> species = new HashSet<>(Arrays.asList(
            new Species(11), new Species(22), new Species(44)));
        Map<Integer, Species> speciesMap = species.stream()
                .collect(Collectors.toMap(Entity::getId, s -> s));
        when(speciesService.loadSpeciesMap(speciesIds, false)).thenReturn(speciesMap);
        
        // Mock GeneDAO
        Map<Integer, Set<String>> filtersToMap = new HashMap<>();
        filtersToMap.put(11, new HashSet<>(Arrays.asList("ID1")));
        filtersToMap.put(22, new HashSet<>(Arrays.asList("ID2")));
        filtersToMap.put(44, new HashSet<>(Arrays.asList("ID4")));
        GeneDAO dao = mock(GeneDAO.class);
        when(managerMock.getGeneDAO()).thenReturn(dao);
        GeneTOResultSet mockGeneRs = getMockResultSet(GeneTOResultSet.class,
                Arrays.asList(new GeneTO(1, "ID1", "Name1", "Desc1", 11, 1, 1, true, 1),
                        new GeneTO(2, "ID2", "Name2", "Desc2", 22, 1, 1, true, 1),
                        new GeneTO(4, "ID4", "Name4", "Desc4", 44, 1, 1, true, 1)));
        when(dao.getGenesBySpeciesAndGeneIds(filtersToMap)).thenReturn(mockGeneRs);

        // Test
        Set<Gene> expectedGenes = new HashSet<>();
        expectedGenes.add(new Gene("ID1", "Name1", "Desc1", null, null, new Species(11), 1));
        expectedGenes.add(new Gene("ID2", "Name2", "Desc2", null, null, new Species(22), 1));
        expectedGenes.add(new Gene("ID4", "Name4", "Desc4", null, null, new Species(44), 1));
        
        GeneService service = new GeneService(serviceFactory);
        Set<GeneFilter> geneFilters = new HashSet<>();
        geneFilters.add(new GeneFilter(11, "ID1"));
        geneFilters.add(new GeneFilter(22, "ID2"));
        geneFilters.add(new GeneFilter(44, "ID4"));
        assertEquals("Incorrect gene to keywords mapping",
                expectedGenes, service.loadGenes(geneFilters).collect(Collectors.toSet()));
    }
    
    /**
     * Test {@link GeneService#loadGenesByAnyId(Collection, boolean)}.
     */
    @Test
    public void shouldLoadGenesByAnyId() {
        // Initialize mocks
        DAOManager managerMock = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
        SpeciesService speciesService = mock(SpeciesService .class);
        when(serviceFactory.getSpeciesService()).thenReturn(speciesService);
        GeneXRefDAO geneXrefDao = mock(GeneXRefDAO.class);
        when(managerMock.getGeneXRefDAO()).thenReturn(geneXrefDao);
        GeneDAO geneDao = mock(GeneDAO.class);
        when(managerMock.getGeneDAO()).thenReturn(geneDao);
        
        // Initialize params
        Set<String> inputIds = new HashSet<>(Arrays.asList("ID1", "OtherID1",
                "OtherID2", "OtherID4", "UnknownID"));

        // Mock the GeneXrefDAO response
        GeneXRefTOResultSet mockGeneXRefRs = getMockResultSet(GeneXRefTOResultSet.class,
                Arrays.asList(new GeneXRefTO(1, "OtherID1", null, null),
                        new GeneXRefTO(2, "OtherID2", null, null),
                        new GeneXRefTO(22, "OtherID2", null, null),
                        new GeneXRefTO(4, "OtherID4", null, null)));
        when(geneXrefDao.getGeneXRefsByXRefIds(inputIds, 
                Arrays.asList(GeneXRefDAO.Attribute.BGEE_GENE_ID, GeneXRefDAO.Attribute.XREF_ID)))
                .thenReturn(mockGeneXRefRs);

        // Mock the SpeciesService response
        Map<Integer, Species> speciesMap = new HashMap<>();
        speciesMap.put(1, new Species(1));
        Map<Integer, Species> speciesMap2 = new HashMap<>();
        speciesMap2.put(1, new Species(1));
        speciesMap2.put(2, new Species(2));
        when(speciesService.loadSpeciesMap(null, false)).thenReturn(speciesMap).thenReturn(speciesMap2);

        // Mock the GeneDAO response from cross-ref ids
        Set<Integer> bgeeGeneIds = new HashSet<>(Arrays.asList(1, 2, 22, 4));
        GeneTOResultSet mockGeneRs1 = getMockResultSet(GeneTOResultSet.class,
                Arrays.asList(new GeneTO(1, "ID1", "Name1a", null, 1, null, null, null, 1),
                        new GeneTO(2, "ID2", "Name2", null, 1, null, null, null, 1),
                        new GeneTO(22, "ID22", "Name22", null, 1, null, null, null, 1),
                        new GeneTO(4, "ID4", "Name4", null, 1, null, null, null, 1)));
        when(geneDao.getGenesByBgeeIds(bgeeGeneIds)).thenReturn(mockGeneRs1);

        // Mock the GeneDAO response from ensembl ids
        GeneTOResultSet mockGeneRs2 = getMockResultSet(GeneTOResultSet.class,
                Arrays.asList(new GeneTO(1, "ID1", "Name1a", null, 1, null, null, null, 1),
                        new GeneTO(11, "ID1", "Name1b", null, 2, null, null, null, 1)));
        when(geneDao.getGenesByIds(new HashSet<>(Arrays.asList("ID1", "UnknownID"))))
                .thenReturn(mockGeneRs2);
        
        Map<String,Set<Gene>> expectedMap = new HashMap<>();
        expectedMap.put("ID1", new HashSet<>(Arrays.asList(
                new Gene("ID1", "Name1a", null, null, null, new Species(1), 1),
                new Gene("ID1", "Name1b", null, null, null, new Species(2), 1))));
        expectedMap.put("OtherID1", new HashSet<>(Arrays.asList(
                new Gene("ID1", "Name1a", null, null, null, new Species(1), 1))));
        expectedMap.put("OtherID2", new HashSet<>(Arrays.asList(
                new Gene("ID2", "Name2",  null, null, null, new Species(1), 1),
                new Gene("ID22", "Name22",  null, null, null, new Species(1), 1))));
        expectedMap.put("OtherID4", new HashSet<>(
                Arrays.asList(new Gene("ID4", "Name4", null, null, null, new Species(1), 1))));
        expectedMap.put("UnknownID", new HashSet<>());

        GeneService service = new GeneService(serviceFactory);
        assertEquals("Incorrect genes", expectedMap, service.loadGenesByAnyId(inputIds, false)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }
    
    @Test
    @Ignore("Test ignored until the method getOrthologs() is re-implemented.")
    public void testGetOrthologies() {
        DAOManager managerMock = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
        
        // Mock HierarchicalGroupDAO
        HierarchicalGroupDAO hgDao = mock(HierarchicalGroupDAO.class);
        when(managerMock.getHierarchicalGroupDAO()).thenReturn(hgDao);
        HierarchicalGroupToGeneTOResultSet resultSet = getMockResultSet(HierarchicalGroupToGeneTOResultSet.class, 
                Arrays.asList(new HierarchicalGroupToGeneTO(1, 123, 1),
                        new HierarchicalGroupToGeneTO(1, 124, 1),
                        new HierarchicalGroupToGeneTO(2, 223, 1)));
        when(hgDao.getGroupToGene(1234, null)).thenReturn(resultSet);
        
        // Mock SpeciesService
        SpeciesService speciesService = mock(SpeciesService.class);
        when(serviceFactory.getSpeciesService()).thenReturn(speciesService);
        Set<Species> species = new HashSet<>(Arrays.asList(
            new Species(11), new Species(22), new Species(44)));
        when(speciesService.loadSpeciesByIds(new HashSet<>(), false)).thenReturn(species);

        
        // Mock GeneService
        GeneDAO geneDao = mock(GeneDAO.class);
        when(managerMock.getGeneDAO()).thenReturn(geneDao);
        GeneTOResultSet mockGeneRs = getMockResultSet(GeneTOResultSet.class,
            Arrays.asList(new GeneTO(123, "ID1", "Name1", "Desc1", 11, 1, 1, true, 1),
                    new GeneTO(124, "ID2", "Name2", "Desc2", 22, 1, 1, true, 1),
                    new GeneTO(223, "ID4", "Name4", "Desc4", 44, 1, 1, true, 1)));
        when(geneDao.getGenesBySpeciesIds(null)).thenReturn(mockGeneRs);

        // Test
        GeneService service = new GeneService(serviceFactory);
        Map<Integer, Set<Gene>> expected = new HashMap<>();
        expected.put(1, new HashSet<>(Arrays.asList(
            new Gene("ID1", "Name1", "Desc1", null, null, new Species(11), 1), 
            new Gene("ID2", "Name2", "Desc2", null, null, new Species(22), 1))));
        expected.put(2, new HashSet<>(Arrays.asList(
            new Gene("ID4", "Name4", "Desc4", null, null, new Species(44), 1))));
        Map<Integer, Set<Gene>> actual = service.getOrthologs(1234, null);
        assertEquals(expected, actual);
    }
}
