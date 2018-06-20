package org.bgee.model.gene;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bgee.model.ServiceFactory;
import org.bgee.model.TestAncestor;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTOResultSet;
import org.bgee.model.dao.api.gene.GeneNameSynonymDAO;
import org.bgee.model.dao.api.gene.GeneNameSynonymDAO.GeneNameSynonymTO;
import org.bgee.model.dao.api.gene.GeneNameSynonymDAO.GeneNameSynonymTOResultSet;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalGroupToGeneTO;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalGroupToGeneTOResultSet;
import org.bgee.model.species.Species;
import org.bgee.model.species.SpeciesService;
import org.junit.Test;

/**
 * This class holds the unit tests for the {@code GeneService} class.
 * 
 * @author  Valentine Rech de Laval
 * @author  Philippe Moret
 * @version Bgee 14, Mar. 2017
 * @since   Bgee 13, Nov. 2015
 */
public class GeneServiceTest extends TestAncestor {

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
        when(speciesService.loadSpeciesByIds(speciesIds, false)).thenReturn(species);
        
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
        expectedGenes.add(new Gene("ID1", "Name1", "Desc1", new Species(11), 1));
        expectedGenes.add(new Gene("ID2", "Name2", "Desc2", new Species(22), 1));
        expectedGenes.add(new Gene("ID4", "Name4", "Desc4", new Species(44), 1));
        
        GeneService service = new GeneService(serviceFactory);
        Set<GeneFilter> geneFilters = new HashSet<>();
        geneFilters.add(new GeneFilter(11, "ID1"));
        geneFilters.add(new GeneFilter(22, "ID2"));
        geneFilters.add(new GeneFilter(44, "ID4"));
        assertEquals("Incorrect gene to keywords mapping",
                expectedGenes, service.loadGenes(geneFilters).collect(Collectors.toSet()));
    }
    
    @Test
    //TODO: finish test
    public void shouldFindByTerm() {
    	DAOManager managerMock = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
        GeneDAO geneDao = mock(GeneDAO.class);
        when(managerMock.getGeneDAO()).thenReturn(geneDao);
        GeneNameSynonymDAO synDao = mock(GeneNameSynonymDAO.class);
        when(managerMock.getGeneNameSynonymDAO()).thenReturn(synDao);
        SpeciesService spService = mock(SpeciesService.class);
        when(serviceFactory.getSpeciesService()).thenReturn(spService);

        String term = "Name1";

        GeneTOResultSet mockGeneRs = getMockResultSet(GeneTOResultSet.class,
                Arrays.asList(new GeneTO(1, "ID1", "Name1", null, 11, null, null, null, 1),
                        new GeneTO(2, "ID2", "Name2", null, 22, null, null, null, 1)));
        when(geneDao.getGeneBySearchTerm(term, null, 1, 100)).thenReturn(mockGeneRs);

        when(spService.loadSpeciesByIds(new HashSet<>(Arrays.asList(11, 22)), false))
            .thenReturn(new HashSet<>(Arrays.asList(new Species(11, null, null),
                new Species(22, null, null))));
        
        GeneNameSynonymTOResultSet mockSynRs = getMockResultSet(GeneNameSynonymTOResultSet.class,
            Arrays.asList(new GeneNameSynonymTO(1, "geneNameSynonym1"),
                new GeneNameSynonymTO(1, "geneNameSynonym1b"),
                new GeneNameSynonymTO(2, "geneName1Synonym2")));
        when(synDao.getGeneNameSynonyms(new HashSet<>(Arrays.asList(1, 2)))).thenReturn(mockSynRs);

        GeneService service = new GeneService(serviceFactory);
        service.searchByTerm(term);
    }
    
    @Test
    public void testGetOrthologies() {
        DAOManager managerMock = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
        
        // Mock HierarchicalGroupDAO
        HierarchicalGroupDAO hgDao = mock(HierarchicalGroupDAO.class);
        when(managerMock.getHierarchicalGroupDAO()).thenReturn(hgDao);
        HierarchicalGroupToGeneTOResultSet resultSet = getMockResultSet(HierarchicalGroupToGeneTOResultSet.class, 
                Arrays.asList(new HierarchicalGroupToGeneTO("TO1", 123, 1),
                        new HierarchicalGroupToGeneTO("TO1", 124, 1),
                        new HierarchicalGroupToGeneTO("TO2", 223, 1)));
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
            new Gene("ID1", "Name1", "Desc1", new Species(11), 1), 
            new Gene("ID2", "Name2", "Desc2", new Species(22), 1))));
        expected.put(2, new HashSet<>(Arrays.asList(
            new Gene("ID4", "Name4", "Desc4", new Species(44), 1))));
        Map<Integer, Set<Gene>> actual = service.getOrthologs(1234, null);
        assertEquals(expected, actual);
    }
}
