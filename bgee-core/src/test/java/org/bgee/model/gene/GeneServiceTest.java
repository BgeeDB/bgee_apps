package org.bgee.model.gene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bgee.model.TestAncestor;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTOResultSet;
import org.bgee.model.dao.api.gene.GeneXRefDAO;
import org.bgee.model.dao.api.gene.GeneXRefDAO.GeneXRefTO;
import org.bgee.model.dao.api.gene.GeneXRefDAO.GeneXRefTOResultSet;
import org.bgee.model.species.Species;
import org.bgee.model.species.SpeciesService;
import org.junit.Test;

/**
 * This class holds the unit tests for the {@code GeneService} class.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Apr. 2016
 * @since   Bgee 13, Nov. 2015
 */
public class GeneServiceTest extends TestAncestor {

    /**
     * Test {@link GeneService#loadGenesByIdsAndSpeciesIds(java.util.Collection, java.util.Collection)}.
     */
    @Test
    public void shouldLoadGenesByIdsAndSpeciesIds() {
        // initialize mocks
        DAOManager managerMock = mock(DAOManager.class);
        GeneDAO dao = mock(GeneDAO.class);
        when(managerMock.getGeneDAO()).thenReturn(dao);
        
        GeneTOResultSet mockGeneRs = getMockResultSet(GeneTOResultSet.class,
                Arrays.asList(new GeneTO("ID1", "Name1", 11),
                        new GeneTO("ID2", "Name2", 22),
                        new GeneTO("ID4", "Name4", 44)));
        
        Set<String> speciesIds = new HashSet<String>();
        speciesIds.addAll(Arrays.asList("11", "22", "44"));
        
        Set<String> geneIds = new HashSet<String>();
        geneIds.addAll(Arrays.asList("ID1", "ID2", "ID4"));
        
        when(dao.getGenesBySpeciesIds(speciesIds, geneIds)).thenReturn(mockGeneRs);

        Set<Gene> expectedGenes= new HashSet<Gene>();
        expectedGenes.add(new Gene("ID1", "11", "Name1"));
        expectedGenes.add(new Gene("ID2", "22", "Name2"));
        expectedGenes.add(new Gene("ID4", "44", "Name4"));
        
        GeneService service = new GeneService(managerMock);
        assertEquals("Incorrect gene", expectedGenes,
                service.loadGenesByIdsAndSpeciesIds(geneIds, speciesIds).collect(Collectors.toSet()));
    }
    
    /**
     * Test {@link GeneService#loadGeneIdsByAnyId(java.util.Collection, java.util.Collection)}.
     */
    @Test
    public void shouldLoadGeneIdsByAnyId() {
        // Initialize mocks
        DAOManager managerMock = mock(DAOManager.class);
        GeneDAO geneDao = mock(GeneDAO.class);
        when(managerMock.getGeneDAO()).thenReturn(geneDao);
        GeneXRefDAO geneXrefDao = mock(GeneXRefDAO.class);
        when(managerMock.getGeneXRefDAO()).thenReturn(geneXrefDao);
        
        // Initialize params
        Set<String> inputIds = new HashSet<String>(Arrays.asList("ID1", "OtherID2", "OtherID4"));
        Set<String> filteredIds = new HashSet<String>(Arrays.asList("ID1", "ID2", "ID4"));

        // Mock the GeneXrefDAO response
        GeneXRefTOResultSet mockGeneXRefRs = getMockResultSet(GeneXRefTOResultSet.class,
                Arrays.asList(new GeneXRefTO("ID2", "OtherID2", null, null),
                        new GeneXRefTO("ID4", "OtherID4", null, null)));
        when(geneXrefDao.getGeneXRefsByXRefIds(inputIds, Arrays.asList(GeneXRefDAO.Attribute.GENE_ID, 
                GeneXRefDAO.Attribute.XREF_ID))).thenReturn(mockGeneXRefRs);
        
        // Mock the GeneXrefDAO response
        GeneTOResultSet mockGeneRs = getMockResultSet(GeneTOResultSet.class,
                Arrays.asList(new GeneTO("ID1", "Name1", 11),
                        new GeneTO("ID2", "Name2", 22),
                        new GeneTO("ID4", "Name4", 44)));
        when(geneDao.getGenesBySpeciesIds(new HashSet<>(), filteredIds)).thenReturn(mockGeneRs);
        
        Set<String> expectedGenes = new HashSet<>(Arrays.asList("ID1", "ID2", "ID4"));

        GeneService service = new GeneService(managerMock);
        assertEquals("Incorrect genes", expectedGenes,
                service.loadGeneIdsByAnyId(inputIds).collect(Collectors.toSet()));
        
        mockGeneXRefRs = getMockResultSet(GeneXRefTOResultSet.class,
                Arrays.asList(new GeneXRefTO("ID2", "OtherID2", null, null),
                        new GeneXRefTO("ID4", "OtherID2", null, null),
                        new GeneXRefTO("ID4", "OtherID4", null, null)));
        when(geneXrefDao.getGeneXRefsByXRefIds(inputIds, Arrays.asList(GeneXRefDAO.Attribute.GENE_ID, 
                GeneXRefDAO.Attribute.XREF_ID))).thenReturn(mockGeneXRefRs);
        try {
            service.loadGeneIdsByAnyId(inputIds).collect(Collectors.toSet());
            fail("An exception should be thrown when provided ID map to severals gene IDs");
        } catch (IllegalArgumentException e) {
            //test passed
        }
    }
    
    /**
     * Test {@link GeneService#loadMappingAnyIdToGeneIds(java.util.Collection)}.
     */
    @Test
    public void shouldLoadMappingAnyIdToGeneIds() {
        // Initialize mocks
        DAOManager managerMock = mock(DAOManager.class);
        GeneXRefDAO geneXrefDao = mock(GeneXRefDAO.class);
        when(managerMock.getGeneXRefDAO()).thenReturn(geneXrefDao);
        
        // Initialize params
        Set<String> inputIds = new HashSet<String>(Arrays.asList("ID1", "OtherID2", "OtherID4"));

        // Mock the GeneXrefDAO response
        GeneXRefTOResultSet mockGeneXRefRs = getMockResultSet(GeneXRefTOResultSet.class,
                Arrays.asList(new GeneXRefTO("ID2", "OtherID2", null, null),
                        new GeneXRefTO("ID4", "OtherID2", null, null),
                        new GeneXRefTO("ID4", "OtherID4", null, null)));
        when(geneXrefDao.getGeneXRefsByXRefIds(inputIds, Arrays.asList(GeneXRefDAO.Attribute.GENE_ID, 
                GeneXRefDAO.Attribute.XREF_ID))).thenReturn(mockGeneXRefRs);
                
        Map<String, Set<String>> expectedMap = new HashMap<>();        
        expectedMap.put("ID1", new HashSet<String>(Arrays.asList("ID1")));
        expectedMap.put("OtherID2", new HashSet<String>(Arrays.asList("ID2", "ID4")));
        expectedMap.put("OtherID4", new HashSet<String>(Arrays.asList("ID4")));

        GeneService service = new GeneService(managerMock);
        assertEquals("Incorrect mapping", expectedMap, service.loadMappingAnyIdToGeneIds(inputIds));
    }
    
    /**
     * Test {@link GeneService#loadGeneById(String)}.
     */
    @Test
    public void shouldLoadGeneById() {
        // Initialize mocks
        DAOManager managerMock = mock(DAOManager.class);
        GeneDAO geneDao = mock(GeneDAO.class);
        when(managerMock.getGeneDAO()).thenReturn(geneDao);
        SpeciesService speciesService = mock(SpeciesService.class);

        // Initialize params
        String geneId = "ID1";
        
        // Mock gene DAO
        GeneTOResultSet mockGeneRs = getMockResultSet(GeneTOResultSet.class,
                Arrays.asList(new GeneTO(geneId, "Name1", 10090)));        
        when(geneDao.getGenesByIds(new HashSet<String>(Arrays.asList(geneId)))).thenReturn(mockGeneRs);

        // Mock species service
        Set<String> speciesId = new HashSet<>(Arrays.asList("10090"));
        Species species = new Species("10090", "mouse", "", "Mus", "musculus", "genome10090");
        Set<Species> speciesSet = new HashSet<>(Arrays.asList(species));
        when(speciesService.loadSpeciesByIds(speciesId)).thenReturn(speciesSet);

        // Test
        Gene expectedGene = new Gene(geneId, "10090", "Name1");
        expectedGene.setSpecies(species);
        
        GeneService service = new GeneService(managerMock, speciesService);

        assertEquals("Incorrect gene", expectedGene, service.loadGeneById(geneId));
    }

    /**
     * Test {@link GeneService#searchByTerm(String)}.
     */
    @Test
    //TODO: finish test
    public void shouldFindByTerm() {
    	DAOManager managerMock = mock(DAOManager.class);
        GeneDAO dao = mock(GeneDAO.class);
        when(managerMock.getGeneDAO()).thenReturn(dao);
        
        GeneTOResultSet mockGeneRs = getMockResultSet(GeneTOResultSet.class,
                Arrays.asList(new GeneTO("ID1", "Name1", 11),
                        new GeneTO("ID2", "Name2", 22),
                        new GeneTO("ID4", "Name4", 44)));
        
        when(dao.getGeneBySearchTerm("Name", null, 1, 25)).thenReturn(mockGeneRs);
        
    }
}
