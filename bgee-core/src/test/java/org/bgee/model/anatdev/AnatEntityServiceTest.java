package org.bgee.model.anatdev;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bgee.model.TestAncestor;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTO;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTOResultSet;
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
        Set<String> speciesIds = new HashSet<String>();
        speciesIds.add("11");

        AnatEntityTOResultSet mockAnatEntRs1 = 
                getMockResultSet(AnatEntityTOResultSet.class, anatEntityTOs);
        when(dao.getAnatEntitiesBySpeciesIds(speciesIds)).thenReturn(mockAnatEntRs1);
        
        // Test without defined level
        List<AnatEntity> expectedAnatEntity = Arrays.asList(
                new AnatEntity("UBERON:0001687",  "stapes bone",  "stapes bone description"), 
                new AnatEntity("UBERON:0001853", "utricle of membranous labyrinth", "utricle of membranous labyrinth description"), 
                new AnatEntity("UBERON:0011606", "hyomandibular bone", "hyomandibular bone description"));
        AnatEntityService service = new AnatEntityService(managerMock);
        assertEquals("Incorrect anat. entities", expectedAnatEntity,
                service.loadAnatEntitiesBySpeciesIds(speciesIds).collect(Collectors.toList()));
    }


}
