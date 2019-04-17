package org.bgee.model.gene;

import org.bgee.model.BgeeProperties;
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
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.sphx.api.SphinxClient;
import org.sphx.api.SphinxException;
import org.sphx.api.SphinxMatch;
import org.sphx.api.SphinxResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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

    private static BgeeProperties props;
    
    /**
     * Set properties.
     */
    @BeforeClass
    public static void initInitialContext() {
        Properties setProps = new Properties();
        setProps.setProperty(BgeeProperties.BGEE_SEARCH_SERVER_URL_KEY,
                "/Users/admin/Desktop/topanat/results/");
        setProps.setProperty(BgeeProperties.BGEE_SEARCH_SERVER_PORT_KEY, "9999");
        props = BgeeProperties.getBgeeProperties(setProps);
    }

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
                new Gene("ID1", "Name1a", null, null, new Species(1), 1),
                new Gene("ID1", "Name1b", null, null, new Species(2), 1))));
        expectedMap.put("OtherID1", new HashSet<>(Arrays.asList(
                new Gene("ID1", "Name1a", null, null, new Species(1), 1))));
        expectedMap.put("OtherID2", new HashSet<>(Arrays.asList(
                new Gene("ID2", "Name2",  null, null, new Species(1), 1),
                new Gene("ID22", "Name22",  null, null, new Species(1), 1))));
        expectedMap.put("OtherID4", new HashSet<>(
                Arrays.asList(new Gene("ID4", "Name4",  null, null, new Species(1), 1))));
        expectedMap.put("UnknownID", new HashSet<>());

        GeneService service = new GeneService(serviceFactory, BgeeProperties.getBgeeProperties());
        assertEquals("Incorrect genes", expectedMap, service.loadGenesByAnyId(inputIds, false)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }
    
    /**
     * Test {@link GeneService#autocomplete(String, int)}.
     */
    @Test
    public void shouldAutocomplete() throws SphinxException {
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        SphinxClient sphinxClient = mock(SphinxClient.class);

        SphinxResult sphinxResult = new SphinxResult();
        sphinxResult.totalFound = 3;
        sphinxResult.attrNames = new String[]{"hit"};
        SphinxMatch sphinxMatch1 = new SphinxMatch(1, 10);
        sphinxMatch1.attrValues = new ArrayList();
        sphinxMatch1.attrValues.add("ENSG01");
        SphinxMatch sphinxMatch2 = new SphinxMatch(2, 5);
        sphinxMatch2.attrValues = new ArrayList();
        sphinxMatch2.attrValues.add("ENSG02");
        SphinxMatch sphinxMatch3 = new SphinxMatch(3, 1);
        sphinxMatch3.attrValues = new ArrayList();
        sphinxMatch3.attrValues.add("ENSG03");
        sphinxResult.matches = new SphinxMatch[] {sphinxMatch1, sphinxMatch2, sphinxMatch3};

        String term = "ENSG";
        when(sphinxClient.Query(term, "bgee_autocomplete")).thenReturn(sphinxResult);

        GeneService service = new GeneService(serviceFactory, sphinxClient, props);
        List<String> autocompleteResult = service.autocomplete(term, 100);

        assertNotNull(autocompleteResult);
        assertEquals(Arrays.asList("ENSG01", "ENSG02", "ENSG03"), autocompleteResult);
    }
    
    @Test
    public void shouldFindByTerm() throws SphinxException {
    	DAOManager managerMock = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
        SpeciesService spService = mock(SpeciesService.class);
        when(serviceFactory.getSpeciesService()).thenReturn(spService);
        SphinxClient sphinxClient = mock(SphinxClient.class);

        Species species = new Species(11, null, null);
        Map<Integer, Species> speciesMap = new HashMap<>();
        speciesMap.put(11, species);
        when(spService.loadSpeciesMap(Collections.singleton(11), false)).thenReturn(speciesMap);

        SphinxResult sphinxResult = new SphinxResult();
        sphinxResult.totalFound = 1;
        sphinxResult.attrNames = new String[]{
                "bgeegeneid", "geneid", "genename", "genedescription",
                "genenamesynonym", "genexref", "speciesid", "genemappedtogeneidcount"};
        SphinxMatch sphinxMatch1 = new SphinxMatch(1, 1);
        sphinxMatch1.attrValues = new ArrayList();
        sphinxMatch1.attrValues.add(86L);
        sphinxMatch1.attrValues.add("ENSG0086");
        sphinxMatch1.attrValues.add("Name1");
        sphinxMatch1.attrValues.add("Desc1");
        sphinxMatch1.attrValues.add("Syn1||Syn2||Syn3");
        sphinxMatch1.attrValues.add("xref_1||xref:2||xref.3");
        sphinxMatch1.attrValues.add(11L);
        sphinxMatch1.attrValues.add(1L);
        sphinxResult.matches = new SphinxMatch[] {sphinxMatch1};

        String term = "Syn2";
        when(sphinxClient.Query(term, "bgee_genes")).thenReturn(sphinxResult);
        
        GeneService service = new GeneService(serviceFactory, sphinxClient, props);
        GeneMatchResult geneMatchResult = service.searchByTerm(term, null, 0, 100);

        assertEquals(1, geneMatchResult.getTotalMatchCount());
        assertNotNull(geneMatchResult.getGeneMatches());
        assertEquals(1, geneMatchResult.getGeneMatches().size());
        Gene g = new Gene("ENSG0086", "Name1", "Desc1", Arrays.asList("Syn1", "Syn2", "Syn3"),
                species, 1);
        assertEquals(new GeneMatch(g, "syn2", GeneMatch.MatchSource.SYNONYM), geneMatchResult.getGeneMatches().get(0));
    }
    
    @Test
    public void shouldFindByTerm_noResult() throws SphinxException {
        DAOManager managerMock = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
        SphinxClient sphinxClient = mock(SphinxClient.class);

        String term = "XXX";

        when(sphinxClient.Query(term, "bgee_genes")).thenReturn(null);

        GeneService service = new GeneService(serviceFactory, sphinxClient, props);
        GeneMatchResult geneMatchResult = service.searchByTerm(term, null, 0, 100);

        assertEquals(0, geneMatchResult.getTotalMatchCount());
        assertNull(geneMatchResult.getGeneMatches());
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
