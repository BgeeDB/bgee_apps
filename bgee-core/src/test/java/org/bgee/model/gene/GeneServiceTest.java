package org.bgee.model.gene;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bgee.model.TestAncestor;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTOResultSet;
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
        GeneDAO dao = mock(GeneDAO.class);
        when(managerMock.getGeneDAO()).thenReturn(dao);
        
        GeneTOResultSet mockGeneRs = getMockResultSet(GeneTOResultSet.class,
                Arrays.asList(new GeneTO("ID1", "Name1", 11),
                        new GeneTO("ID2", "Name2", 22),
                        new GeneTO("ID4", "Name4", 44)));
        
        Set<String> speciesIds = new HashSet<String>();
        speciesIds.addAll(Arrays.asList("11", "22", "44"));
        
        Set<String> geneIds = new HashSet<String>();
        geneIds.addAll(Arrays.asList("ID1", "ID3", "ID4"));
        
        when(dao.getGenesBySpeciesIds(speciesIds)).thenReturn(mockGeneRs);

        List<Gene> expectedGenes= new ArrayList<Gene>();
        expectedGenes.add(new Gene("ID1", "11"));
        expectedGenes.add(new Gene("ID4", "44"));
        
        GeneService service = new GeneService(managerMock);
        assertEquals("Incorrect gene to keywords mapping",
                expectedGenes, service.loadGenesByIdsAndSpeciesIds(geneIds, speciesIds));
    }
}
