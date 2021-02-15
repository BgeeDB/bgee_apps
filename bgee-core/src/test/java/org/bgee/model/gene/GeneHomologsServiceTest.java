package org.bgee.model.gene;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bgee.model.ServiceFactory;
import org.bgee.model.TestAncestor;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneBioTypeTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneBioTypeTOResultSet;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTOResultSet;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalNodeToGeneTO;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalNodeToGeneTOResultSet;
import org.bgee.model.species.Species;
import org.bgee.model.species.SpeciesService;
import org.junit.Ignore;
import org.junit.Test;

public class GeneHomologsServiceTest extends TestAncestor {
    
    /**
     * Test {@link GeneService#searchByTerm(String)}.
     */
    @Test
    @Ignore("Test ignored until the method getOrthologs() is re-implemented.")
    public void testGetOrthologies() {
        DAOManager managerMock = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
        
        // Mock HierarchicalGroupDAO
        HierarchicalGroupDAO hgDao = mock(HierarchicalGroupDAO.class);
        when(managerMock.getHierarchicalGroupDAO()).thenReturn(hgDao);
        HierarchicalNodeToGeneTOResultSet resultSet = getMockResultSet(HierarchicalNodeToGeneTOResultSet.class, 
                Arrays.asList(new HierarchicalNodeToGeneTO(1, 123, 3),
                        new HierarchicalNodeToGeneTO(1, 124, 3),
                        new HierarchicalNodeToGeneTO(2, 223, 4)));
        when(hgDao.getOMANodeToGene(1234, null)).thenReturn(resultSet);
        
        // Mock SpeciesService
        SpeciesService speciesService = mock(SpeciesService.class);
        when(serviceFactory.getSpeciesService()).thenReturn(speciesService);
        Map<Integer, Species> speciesMap = new HashMap<>();
        speciesMap.put(11, new Species(11));
        speciesMap.put(22, new Species(22));
        speciesMap.put(44, new Species(44));
        when(speciesService.loadSpeciesMap(new HashSet<>(), false)).thenReturn(speciesMap);

        
        // Mock GeneService
        GeneDAO geneDao = mock(GeneDAO.class);
        when(managerMock.getGeneDAO()).thenReturn(geneDao);
        GeneTOResultSet mockGeneRs = getMockResultSet(GeneTOResultSet.class,
            Arrays.asList(new GeneTO(123, "ID1", "Name1", "Desc1", 11, 1, 1, true, 1),
                    new GeneTO(124, "ID2", "Name2", "Desc2", 22, 1, 1, true, 1),
                    new GeneTO(223, "ID4", "Name4", "Desc4", 44, 2, 1, true, 1)));
        when(geneDao.getGenesBySpeciesIds(null)).thenReturn(mockGeneRs);
        GeneBioTypeTOResultSet mockBioTypeRs = getMockResultSet(GeneBioTypeTOResultSet.class,
                Arrays.asList(new GeneBioTypeTO(1, "type1"), new GeneBioTypeTO(2, "type2")));
        when(geneDao.getGeneBioTypes()).thenReturn(mockBioTypeRs);

        // Test
        GeneService service = new GeneService(serviceFactory);
        Map<Integer, Set<Gene>> expected = new HashMap<>();
        expected.put(1, new HashSet<>(Arrays.asList(
            new Gene("ID1", "Name1", "Desc1", null, null, new Species(11), new GeneBioType("type1"), 1), 
            new Gene("ID2", "Name2", "Desc2", null, null, new Species(22), new GeneBioType("type1"), 1))));
        expected.put(2, new HashSet<>(Arrays.asList(
            new Gene("ID4", "Name4", "Desc4", null, null, new Species(44), new GeneBioType("type2"), 1))));
//        Map<Integer, Set<Gene>> actual = service.getOrthologs(1234, null);
//        assertEquals(expected, actual);
    }

}
