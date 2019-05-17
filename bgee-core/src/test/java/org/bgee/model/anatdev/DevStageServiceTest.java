package org.bgee.model.anatdev;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bgee.model.ServiceFactory;
import org.bgee.model.TestAncestor;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.anatdev.StageDAO;
import org.bgee.model.dao.api.anatdev.StageDAO.StageTO;
import org.bgee.model.dao.api.anatdev.StageDAO.StageTOResultSet;
import org.junit.Test;

/**
 * This class holds the unit tests for the {@code DevStageService} class.
 * 
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 13, Aug. 2016
 * @since   Bgee 13, Nov. 2015
 */
public class DevStageServiceTest extends TestAncestor {

    /**
     * Test the method {@link DevStageService#loadGroupingDevStages(Set, Integer)}.
     */
    @Test
    public void shouldLoadGroupingDevStages() {
        // initialize mocks
        DAOManager managerMock = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
        StageDAO dao = mock(StageDAO.class);
        when(managerMock.getStageDAO()).thenReturn(dao);
        
        List<StageTO> stageTOs = Arrays.asList(
                new StageTO("Stage_id11", "stageN11", "stage Desc 11", 19, 20, 3, false, true), 
                new StageTO("Stage_id12", "stageN12", "stage Desc 12", 21, 22, 3, false, true), 
                new StageTO("Stage_id13", "stageN13", "stage Desc 13", 23, 24, 3, false, true), 
                new StageTO("Stage_id15", "stageN15", "stage Desc 15", 27, 32, 3, false, true));

        // Filter on species IDs is not tested here (tested in StageDAO)
        // but we need a variable to mock DAO answer
        Set<Integer> speciesIds1 = new HashSet<>();
        speciesIds1.add(44);

        StageTOResultSet mockStageRs1 = getMockResultSet(StageTOResultSet.class, stageTOs);
        when(dao.getStages(eq(speciesIds1), eq(true), anyObject(), eq(true), eq(3), anyObject()))
            .thenReturn(mockStageRs1);
        
        // Test without defined level
        Set<DevStage> expectedDevStage = new HashSet<>(Arrays.asList(
                new DevStage("Stage_id11", "stageN11", "stage Desc 11", 19, 20, 3, false, true), 
                new DevStage("Stage_id12", "stageN12", "stage Desc 12", 21, 22, 3, false, true), 
                new DevStage("Stage_id13", "stageN13", "stage Desc 13", 23, 24, 3, false, true), 
                new DevStage("Stage_id15", "stageN15", "stage Desc 15", 27, 32, 3, false, true)));
        DevStageService service = new DevStageService(serviceFactory);
        assertEquals("Incorrect dev stages",
                expectedDevStage, service.loadGroupingDevStages(speciesIds1, 3));
    }
    
    /**
     * Test the method {@link DevStageService#loadDevStages(Collection, Boolean, Collection)}.
     */
    @Test
    public void shouldLoadDevStages() {
        // initialize mocks
        DAOManager managerMock = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
        StageDAO dao = mock(StageDAO.class);
        when(managerMock.getStageDAO()).thenReturn(dao);
        
        List<StageTO> stageTOs = Arrays.asList(
                new StageTO("Stage_id3", "stageN3", "stage Desc 3", 3, 4, 3, true, false),
                new StageTO("Stage_id12", "stageN12", "stage Desc 12", 21, 22, 3, false, true));

        Set<String> stageIds = new HashSet<String>();
        stageIds.add("Stage_id12");
        stageIds.add("Stage_id3");
        Set<Integer> speciesIds = new HashSet<>();
        speciesIds.add(44);

        StageTOResultSet mockStageRs = getMockResultSet(StageTOResultSet.class, stageTOs);
        when(dao.getStages(eq(speciesIds), eq(true), eq(stageIds), eq(null), eq(null), anyObject()))
            .thenReturn(mockStageRs);
        
        // Test without defined level
        Set<DevStage> expectedDevStage = new HashSet<>(Arrays.asList(
                new DevStage("Stage_id3", "stageN3", "stage Desc 3", 3, 4, 3, true, false),
                new DevStage("Stage_id12", "stageN12", "stage Desc 12", 21, 22, 3, false, true)));
        DevStageService service = new DevStageService(serviceFactory);
        assertEquals("Incorrect dev stages", expectedDevStage, service.loadDevStages(
                speciesIds, true, stageIds).collect(Collectors.toSet()));
    }
}