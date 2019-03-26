package org.bgee.model.gene;

import org.bgee.model.BgeeProperties;
import org.bgee.model.Entity;
import org.bgee.model.ServiceFactory;
import org.bgee.model.TestAncestor;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTOResultSet;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalGroupToGeneTO;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalGroupToGeneTOResultSet;
import org.bgee.model.species.Species;
import org.bgee.model.species.SpeciesService;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
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
 * @version Bgee 14, Mar. 2019
 * @since   Bgee 13, Nov. 2015
 */
public class GeneServiceTest extends TestAncestor {

    private static BgeeProperties props;
    
    /**
     * Set properties.
     */
    @BeforeClass
    public static void initInitialContext() {
        Properties setProps = new Properties();
        setProps.setProperty(BgeeProperties.BGEE_SEARCH_SERVER_URL_KEY,
                "/Users/admin/Desktop/topanat/results/");
        setProps.setProperty(BgeeProperties.BGEE_SEARCH_SERVER_PORT_KEY,
                "/Users/admin/Desktop/topanat/results/");
        props = BgeeProperties.getBgeeProperties(setProps);
    }

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
        expectedGenes.add(new Gene("ID1", "Name1", "Desc1", null, new Species(11), 1));
        expectedGenes.add(new Gene("ID2", "Name2", "Desc2", null, new Species(22), 1));
        expectedGenes.add(new Gene("ID4", "Name4", "Desc4", null, new Species(44), 1));
        
        GeneService service = new GeneService(serviceFactory, props);
        Set<GeneFilter> geneFilters = new HashSet<>();
        geneFilters.add(new GeneFilter(11, "ID1"));
        geneFilters.add(new GeneFilter(22, "ID2"));
        geneFilters.add(new GeneFilter(44, "ID4"));
        assertEquals("Incorrect gene to keywords mapping",
                expectedGenes, service.loadGenes(geneFilters).collect(Collectors.toSet()));
    }
    
    @Test
    //FIXME: implement test
    public void shouldFindByTerm() {
    	DAOManager managerMock = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
        SpeciesService spService = mock(SpeciesService.class);
        when(serviceFactory.getSpeciesService()).thenReturn(spService);

        String term = "Name1";

        when(spService.loadSpeciesByIds(new HashSet<>(Arrays.asList(11, 22)), false))
            .thenReturn(new HashSet<>(Arrays.asList(new Species(11, null, null),
                new Species(22, null, null))));
        
        GeneService service = new GeneService(serviceFactory, props);
//        service.searchByTerm(term, null, 0, 100);
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
        GeneService service = new GeneService(serviceFactory, props);
        Map<Integer, Set<Gene>> expected = new HashMap<>();
        expected.put(1, new HashSet<>(Arrays.asList(
            new Gene("ID1", "Name1", "Desc1", null, new Species(11), 1), 
            new Gene("ID2", "Name2", "Desc2", null, new Species(22), 1))));
        expected.put(2, new HashSet<>(Arrays.asList(
            new Gene("ID4", "Name4", "Desc4", null, new Species(44), 1))));
        Map<Integer, Set<Gene>> actual = service.getOrthologs(1234, null);
        assertEquals(expected, actual);
    }
}
