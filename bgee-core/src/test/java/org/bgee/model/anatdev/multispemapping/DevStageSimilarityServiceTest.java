package org.bgee.model.anatdev.multispemapping;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.ServiceFactory;
import org.bgee.model.TestAncestor;
import org.bgee.model.anatdev.DevStageService;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.anatdev.mapping.StageGroupingDAO;
import org.bgee.model.dao.api.anatdev.mapping.StageGroupingDAO.GroupToStageTO;
import org.bgee.model.dao.api.anatdev.mapping.StageGroupingDAO.GroupToStageTOResultSet;
import org.junit.Test;

/**
 * Unit tests for the {@code DevStageSimilarityService} class.
 * 
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 14 Mar. 2019
 * @since   Bgee 13 Nov. 2015
 */
public class DevStageSimilarityServiceTest extends TestAncestor {
    private final static Logger log = LogManager.getLogger(DevStageSimilarityServiceTest.class.getName());
    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Test the method {@link DevStageService#loadDevStageSimilarities(String, Set)}.
     */
    @Test
    public void shouldLoadDevStageSimilarities() {
        DAOManager managerMock = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
        StageGroupingDAO dao = mock(StageGroupingDAO.class);
        when(managerMock.getStageGroupingDAO()).thenReturn(dao);
        
        Integer taxonId = 1;
        Set<Integer> speciesIds = new HashSet<>(Arrays.asList(1, 2));
        GroupToStageTOResultSet rs = getMockResultSet(GroupToStageTOResultSet.class,
                Arrays.asList(
                        new GroupToStageTO("group1", "stage1"),
                        new GroupToStageTO("group2", "stage2"),
                        new GroupToStageTO("group2", "stage3"),
                        new GroupToStageTO("group1", "stage4"),
                        new GroupToStageTO("group2", "stage5")));
        when(dao.getGroupToStage(taxonId, speciesIds)).thenReturn(rs);
        
        DevStageService service = new DevStageService(serviceFactory);
        Collection<DevStageSimilarity> expected = new HashSet<>(Arrays.asList(
                new DevStageSimilarity("group1", new HashSet<>(Arrays.asList("stage1","stage4"))),
                new DevStageSimilarity("group2", new HashSet<>(Arrays.asList("stage2", "stage3", "stage5")))));

        //FIXME: to reactivate
//        Collection<DevStageSimilarity> actual = service.loadDevStageSimilarities(taxonId, speciesIds);
//        assertEquals("Incorrect dev. stage similarities", expected, actual);
    }
}