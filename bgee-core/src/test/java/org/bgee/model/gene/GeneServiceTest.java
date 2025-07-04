package org.bgee.model.gene;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneBioTypeTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneBioTypeTOResultSet;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTOResultSet;
import org.bgee.model.dao.api.gene.GeneNameSynonymDAO;
import org.bgee.model.dao.api.gene.GeneNameSynonymDAO.GeneNameSynonymTO;
import org.bgee.model.dao.api.gene.GeneNameSynonymDAO.GeneNameSynonymTOResultSet;
import org.bgee.model.dao.api.gene.GeneXRefDAO;
import org.bgee.model.dao.api.gene.GeneXRefDAO.GeneXRefTO;
import org.bgee.model.dao.api.gene.GeneXRefDAO.GeneXRefTOResultSet;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTOResultSet;
import org.bgee.model.source.Source;
import org.bgee.model.source.SourceService;
import org.bgee.model.ServiceFactory;
import org.bgee.model.species.Species;
import org.bgee.model.species.SpeciesService;
import org.bgee.model.TestAncestor;

import static org.junit.Assert.assertEquals;
import org.junit.Test;


import org.mockito.invocation.InvocationOnMock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.stubbing.Answer;

/**
 * This class holds the unit tests for the {@code GeneService} class.
 * 
 * @author  Valentine Rech de Laval
 * @author  Philippe Moret
 * @author  Julien Wollbrett
 * @version Bgee 15, Oct. 2021
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

        Map<Integer, Set<String>> filtersToMap = new HashMap<>();
        filtersToMap.put(11, new HashSet<>(Arrays.asList("ID1")));
        filtersToMap.put(22, new HashSet<>(Arrays.asList("ID2")));
        filtersToMap.put(44, new HashSet<>(Arrays.asList("ID4")));

        // Mock SpeciesDAO
        SpeciesDAO speciesDAO = mock(SpeciesDAO.class);
        when(managerMock.getSpeciesDAO()).thenReturn(speciesDAO);
        SpeciesTOResultSet mockSpeciesRs = getMockResultSet(SpeciesTOResultSet.class,
                Arrays.asList(
                        new SpeciesTO(11, null, null, null, null, null, null, null, null, 1, null),
                        new SpeciesTO(22, null, null, null, null, null, null, null, null, 1, null),
                        new SpeciesTO(44, null, null, null, null, null, null, null, null, 1, null)));
        when(speciesDAO.getSpeciesByIds(filtersToMap.keySet(), null)).thenReturn(mockSpeciesRs);

        // Mock GeneDAO
        GeneDAO dao = mock(GeneDAO.class);
        when(managerMock.getGeneDAO()).thenReturn(dao);
        GeneTOResultSet mockGeneRs = getMockResultSet(GeneTOResultSet.class,
                Arrays.asList(new GeneTO(1, "ID1", "Name1", "Desc1", 11, 1, 1, true, 1, null),
                        new GeneTO(2, "ID2", "Name2", "Desc2", 22, 1, 1, true, 1, null),
                        new GeneTO(4, "ID4", "Name4", "Desc4", 44, 2, 1, true, 1, null)));
        when(dao.getGenesBySpeciesAndGeneIds(filtersToMap, false)).thenReturn(mockGeneRs);
        GeneBioTypeTOResultSet mockBioTypeRs = getMockResultSet(GeneBioTypeTOResultSet.class,
                Arrays.asList(new GeneBioTypeTO(1, "type1"), new GeneBioTypeTO(2, "type2")));
        when(dao.getGeneBioTypes()).thenReturn(mockBioTypeRs);

        // Mock SourceService
        SourceService sourceService = mock(SourceService.class);
        when(serviceFactory.getSourceService()).thenReturn(sourceService);
        Map<Integer, Source> sourceMap = new HashMap<>();
        sourceMap.put(1, new Source(1));
        when(sourceService.loadSourcesByIds(new HashSet<Integer>(Arrays.asList(1)))).thenReturn(sourceMap);

        // Test
        Set<Gene> expectedGenes = new HashSet<>();
        expectedGenes.add(new Gene("ID1", "Name1", "Desc1", null, null, new Species(11), new GeneBioType("type1"), 1, null));
        expectedGenes.add(new Gene("ID2", "Name2", "Desc2", null, null, new Species(22), new GeneBioType("type1"), 1, null));
        expectedGenes.add(new Gene("ID4", "Name4", "Desc4", null, null, new Species(44), new GeneBioType("type2"), 1, null));
        
        GeneService service = new GeneService(serviceFactory);
        Set<GeneFilter> geneFilters = new HashSet<>();
        geneFilters.add(new GeneFilter(11, "ID1"));
        geneFilters.add(new GeneFilter(22, "ID2"));
        geneFilters.add(new GeneFilter(44, "ID4"));
        assertEquals("Incorrect gene to keywords mapping",
                expectedGenes, service.loadGenes(geneFilters, false, false, false, false).collect(Collectors.toSet()));
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
        //XXX: was null attribute before merge, let's see if it works like that
        when(speciesService.loadSpeciesMap(null, false)).thenReturn(speciesMap).thenReturn(speciesMap2);

        // Mock the GeneDAO response from cross-ref ids
        Set<Integer> bgeeGeneIds = new HashSet<>(Arrays.asList(1, 2, 22, 4));
        GeneTOResultSet mockGeneRs1 = getMockResultSet(GeneTOResultSet.class,
                Arrays.asList(new GeneTO(1, "ID1", "Name1a", null, 1, 1, null, null, 1, null),
                        new GeneTO(2, "ID2", "Name2", null, 1, 1, null, null, 1, null),
                        new GeneTO(22, "ID22", "Name22", null, 1, 1, null, null, 1, null),
                        new GeneTO(4, "ID4", "Name4", null, 1, 1, null, null, 1, null)));
        when(geneDao.getGenesByBgeeIds(bgeeGeneIds)).thenReturn(mockGeneRs1);

        // Mock the GeneDAO response from IDs
        GeneTOResultSet mockGeneRs2 = getMockResultSet(GeneTOResultSet.class,
                Arrays.asList(new GeneTO(1, "ID1", "Name1a", null, 1, 1, null, null, 1, null),
                        new GeneTO(11, "ID1", "Name1b", null, 2, 1, null, null, 1, null)));
        when(geneDao.getGenesByGeneIds(new HashSet<>(Arrays.asList("ID1", "UnknownID"))))
                .thenReturn(mockGeneRs2);

        when(geneDao.getGeneBioTypes()).thenAnswer(
                new Answer<GeneBioTypeTOResultSet>() {
                    public GeneBioTypeTOResultSet answer(InvocationOnMock invocation) {
                        return getMockResultSet(GeneBioTypeTOResultSet.class,
                                Arrays.asList(new GeneBioTypeTO(1, "type1"), new GeneBioTypeTO(2, "type2")));
                    }
                });
        
        Map<String,Set<Gene>> expectedMap = new HashMap<>();
        expectedMap.put("ID1", new HashSet<>(Arrays.asList(
                new Gene("ID1", "Name1a", null, null, null, new Species(1), new GeneBioType("type1"), 1, null),
                new Gene("ID1", "Name1b", null, null, null, new Species(2), new GeneBioType("type1"), 1, null))));
        expectedMap.put("OtherID1", new HashSet<>(Arrays.asList(
                new Gene("ID1", "Name1a", null, null, null, new Species(1), new GeneBioType("type1"), 1, null))));
        expectedMap.put("OtherID2", new HashSet<>(Arrays.asList(
                new Gene("ID2", "Name2",  null, null, null, new Species(1), new GeneBioType("type1"), 1, null),
                new Gene("ID22", "Name22",  null, null, null, new Species(1), new GeneBioType("type1"), 1, null))));
        expectedMap.put("OtherID4", new HashSet<>(
                Arrays.asList(new Gene("ID4", "Name4", null, null, null, new Species(1), new GeneBioType("type1"), 1, null))));
        expectedMap.put("UnknownID", new HashSet<>());

        GeneService service = new GeneService(serviceFactory);
        assertEquals("Incorrect genes", expectedMap, service.loadGenesByAnyId(inputIds, false)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }
    
    /**
     * Test {@link GeneService#loadGeneById(String)}.
     */
    @Test
    public void shouldLoadGeneById() {
        // Initialize mock
        int bgeeGeneId = 1;
        String geneId = "ID1";
        int sourceId = 1;

        DAOManager managerMock = mock(DAOManager.class);
        GeneDAO geneDao = mock(GeneDAO.class);
        when(managerMock.getGeneDAO()).thenReturn(geneDao);
        GeneBioTypeTOResultSet mockBioTypeRs = getMockResultSet(GeneBioTypeTOResultSet.class,
                Arrays.asList(new GeneBioTypeTO(1, "type1")));
        when(geneDao.getGeneBioTypes()).thenReturn(mockBioTypeRs);
        GeneNameSynonymDAO synDao = mock(GeneNameSynonymDAO.class);
        when(managerMock.getGeneNameSynonymDAO()).thenReturn(synDao);
        GeneNameSynonymTOResultSet mockSynRs = getMockResultSet(GeneNameSynonymTOResultSet.class,
                Arrays.asList(new GeneNameSynonymTO(bgeeGeneId, "syn")));
        when(synDao.getGeneNameSynonyms(new HashSet<>(Arrays.asList(bgeeGeneId)))).thenReturn(mockSynRs);
        GeneXRefDAO xrefDao = mock(GeneXRefDAO.class);
        when(managerMock.getGeneXRefDAO()).thenReturn(xrefDao);
        GeneXRefTOResultSet mockXRefRs = getMockResultSet(GeneXRefTOResultSet.class,
                Arrays.asList(new GeneXRefTO(bgeeGeneId, "1", "1", sourceId)));
        when(xrefDao.getGeneXRefsByBgeeGeneIds(new HashSet<>(Arrays.asList(bgeeGeneId)), null))
                .thenReturn(mockXRefRs);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
        SpeciesService speciesService = mock(SpeciesService.class);
        when(serviceFactory.getSpeciesService()).thenReturn(speciesService);
        SourceService sourceService = mock(SourceService.class);
        when(serviceFactory.getSourceService()).thenReturn(sourceService);
        Map<Integer, Source> sources = new HashMap<>();
        sources.put(sourceId, new Source(sourceId));
        when(sourceService.loadSourcesByIds(null)).thenReturn(sources);

        // Mock gene DAO
        GeneTOResultSet mockGeneRs = getMockResultSet(GeneTOResultSet.class,
                Arrays.asList(new GeneTO(bgeeGeneId, geneId, "Name1", "", 10090, 1, 1, true, 1, null)));
        when(geneDao.getGenesByGeneIds(new HashSet<String>(Arrays.asList(geneId)))).thenReturn(mockGeneRs);

        // Mock species service
        Set<Integer> speciesId = new HashSet<>(Arrays.asList(10090));
        Species species = new Species(10090);
        Map<Integer, Species> speciesMap = new HashMap<>();
        speciesMap.put(10090, species);
        when(speciesService.loadSpeciesMap(speciesId, false)).thenReturn(speciesMap);

        // Test
        Gene expectedGene = new Gene(geneId, species, new GeneBioType("type1"));

        GeneService service = new GeneService(serviceFactory);

        assertEquals("Incorrect gene", new HashSet<>(Arrays.asList(expectedGene)),
                service.loadGenesById(geneId));
    }

}
