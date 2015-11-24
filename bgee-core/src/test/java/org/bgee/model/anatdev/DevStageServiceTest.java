package org.bgee.model.anatdev;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bgee.model.TestAncestor;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.anatdev.StageDAO;
import org.bgee.model.dao.api.anatdev.StageDAO.StageTO;
import org.bgee.model.dao.api.anatdev.StageDAO.StageTOResultSet;
import org.junit.Test;

/**
 * This class holds the unit tests for the {@code DevStageService} class.
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13, Nov. 2015
 * @since   Bgee 13, Nov. 2015
 */
public class DevStageServiceTest extends TestAncestor {

    @Test
    public void shouldLoadGroupingDevStages() {
        // initialize mocks
        DAOManager managerMock = mock(DAOManager.class);
        StageDAO dao = mock(StageDAO.class);
        when(managerMock.getStageDAO()).thenReturn(dao);
        
        List<StageTO> stageTOs = Arrays.asList(
                new StageTO("Stage_id11", "stageN11", "stage Desc 11", 19, 20, 3, false, true), 
                new StageTO("Stage_id12", "stageN12", "stage Desc 12", 21, 22, 3, false, true), 
                new StageTO("Stage_id13", "stageN13", "stage Desc 13", 23, 24, 3, false, true), 
                new StageTO("Stage_id15", "stageN15", "stage Desc 15", 27, 32, 3, false, true));

        // Filter on species IDs is not tested here (tested in StageDAO)
        // but we need a variable to mock DAO answer
        Set<String> speciesIds1 = new HashSet<String>();
        speciesIds1.add("44");

        StageTOResultSet mockStageRs1 = getMockResultSet(StageTOResultSet.class, stageTOs);
        when(dao.getStagesBySpeciesIds(speciesIds1, true, 3)).thenReturn(mockStageRs1);
        
        // Test without defined level
        List<DevStage> expectedDevStage = Arrays.asList(
                new DevStage("Stage_id11", "stageN11", "stage Desc 11", 19, 20, 3, false, true), 
                new DevStage("Stage_id12", "stageN12", "stage Desc 12", 21, 22, 3, false, true), 
                new DevStage("Stage_id13", "stageN13", "stage Desc 13", 23, 24, 3, false, true), 
                new DevStage("Stage_id15", "stageN15", "stage Desc 15", 27, 32, 3, false, true));
        DevStageService service = new DevStageService(managerMock);
        assertEquals("Incorrect dev stages",
                expectedDevStage, service.loadGroupingDevStages(speciesIds1, 3));
    }
}