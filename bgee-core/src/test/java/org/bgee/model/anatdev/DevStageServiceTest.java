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
                new StageTO("Stage_id1", "stageN1", "stage Desc 1", 1, 36, 1, false, true), 
                new StageTO("Stage_id2", "stageN2", "stage Desc 2", 2, 7, 2, false, false), 
                new StageTO("Stage_id3", "stageN3", "stage Desc 3", 3, 4, 3, true, false), 
                new StageTO("Stage_id4", "stageN4", "stage Desc 4", 5, 6, 3, false, false), 
                new StageTO("Stage_id5", "stageN5", "stage Desc 5", 8, 17, 2, false, false), 
                new StageTO("Stage_id6", "stageN6", "stage Desc 6", 9, 10, 3, false, false), 
                new StageTO("Stage_id7", "stageN7", "stage Desc 7", 11, 16, 3, false, false), 
                new StageTO("Stage_id8", "stageN8", "stage Desc 8", 12, 13, 4, false, true), 
                new StageTO("Stage_id9", "stageN9", "stage Desc 9", 14, 15, 4, false, false), 
                new StageTO("Stage_id10", "stageN10", "stage Desc 10", 18, 25, 2, false, true), 
                new StageTO("Stage_id11", "stageN11", "stage Desc 11", 19, 20, 3, false, true), 
                new StageTO("Stage_id12", "stageN12", "stage Desc 12", 21, 22, 3, false, true), 
                new StageTO("Stage_id13", "stageN13", "stage Desc 13", 23, 24, 3, false, true), 
                new StageTO("Stage_id14", "stageN14", "stage Desc 14", 26, 35, 2, false, false), 
                new StageTO("Stage_id15", "stageN15", "stage Desc 15", 27, 32, 3, false, true), 
                new StageTO("Stage_id16", "stageN16", "stage Desc 16", 28, 29, 4, false, false), 
                new StageTO("Stage_id17", "stageN17", "stage Desc 17", 30, 31, 4, false, false), 
                new StageTO("Stage_id18", "stageN18", "stage Desc 18", 33, 34, 3, false, false));

        // Filter on species IDs is not tested here (tested in StageDAO)
        // but we need a variable to mock DAO answer
        Set<String> speciesIds1 = new HashSet<String>();
        speciesIds1.add("44");
        Set<String> speciesIds2 = new HashSet<String>();
        speciesIds2.add("11");

        StageTOResultSet mockStageRs1 = getMockResultSet(StageTOResultSet.class, stageTOs);
        when(dao.getStagesBySpeciesIds(speciesIds1)).thenReturn(mockStageRs1);
        StageTOResultSet mockStageRs2 = getMockResultSet(StageTOResultSet.class, stageTOs);
        when(dao.getStagesBySpeciesIds(speciesIds2)).thenReturn(mockStageRs2);
        
        // Test without defined level
        List<DevStage> expectedDevStage = Arrays.asList(
                new DevStage("Stage_id1", "stageN1", "stage Desc 1", 1), 
                new DevStage("Stage_id8", "stageN8", "stage Desc 8", 4), 
                new DevStage("Stage_id10", "stageN10", "stage Desc 10", 2), 
                new DevStage("Stage_id11", "stageN11", "stage Desc 11", 3), 
                new DevStage("Stage_id12", "stageN12", "stage Desc 12", 3), 
                new DevStage("Stage_id13", "stageN13", "stage Desc 13", 3), 
                new DevStage("Stage_id15", "stageN15", "stage Desc 15", 3));
        DevStageService service = new DevStageService(managerMock);
        assertEquals("Incorrect dev stages",
                expectedDevStage, service.loadGroupingDevStages(speciesIds1));

    }
}