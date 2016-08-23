package org.bgee.model.analysis;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.bgee.model.ServiceFactory;
import org.bgee.model.TestAncestor;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.anatdev.AnatEntitySimilarity;
import org.bgee.model.anatdev.DevStageService;
import org.bgee.model.anatdev.DevStageSimilarity;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneService;
import org.junit.Test;

/**
 * This class holds the unit tests for the {@code AnalysisService} class.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Aug. 2016
 * @since   Bgee 13, Aug. 2016
 */
public class AnalysisServiceTest extends TestAncestor {

    @Test
    public void shouldLoadMultiSpeciesExpressionCalls() {
        // initialize mocks
        DAOManager managerMock = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
        GeneService geneService = mock(GeneService.class);
        when(serviceFactory.getGeneService()).thenReturn(geneService);
        AnatEntityService anatEntityService = mock(AnatEntityService.class);
        when(serviceFactory.getAnatEntityService()).thenReturn(anatEntityService);
        DevStageService devStageService = mock(DevStageService.class);
        when(serviceFactory.getDevStageService()).thenReturn(devStageService);
        CallService callService = mock(CallService.class);
        when(serviceFactory.getCallService()).thenReturn(callService);
        
        Map<String, Set<String>> omaToGeneIds = new HashMap<>(); // FIXME add data
        when(geneService.getOrthologies(null, null)).thenReturn(omaToGeneIds);

        Set<AnatEntitySimilarity> aeSim = new HashSet<>(); // FIXME add data
        when(anatEntityService.loadAnatEntitySimilarities(null, null, false)).thenReturn(aeSim);

        Set<DevStageSimilarity> dsSim = new HashSet<>(); // FIXME add data
        when(devStageService.loadDevStageSimilarities(null, null)).thenReturn(dsSim);

        Stream<ExpressionCall> exprCallStream = null; // FIXME add data
        when(callService.loadExpressionCalls(null, null, null, null)).thenReturn(exprCallStream);
        
        AnalysisService analysisService = new AnalysisService(serviceFactory);
        Gene gene = null;
        Set<String> speciesIds = null;
//        analysisService.loadMultiSpeciesExpressionCalls(gene, speciesIds);
    }
}
