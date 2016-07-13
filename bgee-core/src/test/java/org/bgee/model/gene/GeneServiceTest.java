package org.bgee.model.gene;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bgee.model.ServiceFactory;
import org.bgee.model.TestAncestor;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTOResultSet;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalGroupToGeneTOResultSet;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalGroupToGeneTO;

import org.junit.Test;

/**
 * This class holds the unit tests for the {@code GeneService} class.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Nov. 2015
 * @since   Bgee 13, Nov. 2015
 */
public class GeneServiceTest extends TestAncestor {

    @Test
    public void shouldLoadGenes() {
        // initialize mocks
        DAOManager managerMock = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
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

        List<Gene> expectedGenes= new ArrayList<Gene>();
        expectedGenes.add(new Gene("ID1", "11", "Name1"));
        expectedGenes.add(new Gene("ID2", "22", "Name2"));
        expectedGenes.add(new Gene("ID4", "44", "Name4"));
        
        GeneService service = new GeneService(serviceFactory);
        assertEquals("Incorrect gene to keywords mapping",
                expectedGenes, service.loadGenesByIdsAndSpeciesIds(geneIds, speciesIds));
    }
    
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
    

    
    @Test
    public void testGetOrthologies() {
        DAOManager managerMock = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
        HierarchicalGroupDAO dao = mock(HierarchicalGroupDAO.class);
        when(managerMock.getHierarchicalGroupDAO()).thenReturn(dao);
        HierarchicalGroupToGeneTOResultSet resultSet = getMockResultSet(HierarchicalGroupToGeneTOResultSet.class, 
                Arrays.asList(new HierarchicalGroupToGeneTO("1", "123"),
                        new HierarchicalGroupToGeneTO("1", "124"),
                        new HierarchicalGroupToGeneTO("2", "223")

                        ));
        when(dao.getGroupToGene("1234", null)).thenReturn(resultSet);
        
        GeneService service = new GeneService(serviceFactory);
        Map<String, Set<String>> expected = new HashMap<>();
        expected.put("1", new HashSet<>(Arrays.asList("123","124")));
        expected.put("2", new HashSet<>(Arrays.asList("223")));
        Map<String, Set<String>> actual = service.getOrthologies("1234", null);
        assertEquals(expected, actual);
    }
}
