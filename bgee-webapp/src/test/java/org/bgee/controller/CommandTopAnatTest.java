package org.bgee.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.TestAncestor;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.DevStageService;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneService;
import org.bgee.view.TopAnatDisplay;
import org.bgee.view.ViewFactory;
import org.junit.Test;

/**
 * Unit tests for {@link CommandTopAnat}.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13 Nov. 2015
 * @since   Bgee 13 Nov. 2015
 */
public class CommandTopAnatTest extends TestAncestor {

    private final static Logger log = 
            LogManager.getLogger(CommandTopAnatTest.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Test {@link CommandTopAnat#processRequest()}.
     * @throws Exception 
     */
    @Test
    public void shouldProcessRequest() throws Exception {
        
        //mock Services
        ServiceFactory serviceFac = mock(ServiceFactory.class);
        
        GeneService geneService = mock(GeneService.class);
        when(serviceFac.getGeneService()).thenReturn(geneService);
        
        DevStageService devStageService = mock(DevStageService.class);
        when(serviceFac.getDevStageService()).thenReturn(devStageService);

        //mock data returned by Services
        List<String> submittedGeneIds = Arrays.asList("ID1", "ID2", "ID3", "ID4");
        String selectedSpeciesId = "9606";

        List<Gene> genes = this.getGeneDataTest();
        when(geneService.loadGenesByIdsAndSpeciesIds(new HashSet<>(submittedGeneIds), null))
            .thenReturn(genes);

        List<DevStage> devStages = this.getDevStagesDataTest();
        when(devStageService.loadGroupingDevStages(new HashSet<>(Arrays.asList(selectedSpeciesId))))
            .thenReturn(devStages);

        //mock view
        ViewFactory viewFac = mock(ViewFactory.class);
        TopAnatDisplay display = mock(TopAnatDisplay.class);
        when(viewFac.getTopAnatDisplay()).thenReturn(display);
        
        // Launch tests
        RequestParameters params = new RequestParameters();
        params.setPage(RequestParameters.PAGE_TOP_ANAT);
        params.setAction(RequestParameters.ACTION_TOP_ANAT_GENE_VALIDATION);
        params.addValues(params.getUrlParametersInstance().getParamBackgroundList(), submittedGeneIds);
        log.info("Generated query URL: " + params.getRequestURL());

        CommandTopAnat controller = new CommandTopAnat(mock(HttpServletResponse.class),
                params, mock(BgeeProperties.class), viewFac, serviceFac);
        controller.processRequest();

        Map<String, Long> speciesIdToGeneCount = new HashMap<String, Long>();
        speciesIdToGeneCount.put("9606", 2L);
        speciesIdToGeneCount.put("10090", 1L);
        
        verify(display).sendGeneListReponse(speciesIdToGeneCount, selectedSpeciesId,
                new HashSet<>(Arrays.asList(new DevStage("2443", "embryo", null, 1))), //adult filtered by level
                null, new HashSet<>(Arrays.asList("ID4")), 0, 
                "4 genes entered, 2 from species 9606, 1 from species 10090, "
                + "1 not found in Bgee.");
    }

    private List<DevStage> getDevStagesDataTest() {
        log.entry();
        return log.exit(Arrays.asList(
                new DevStage("2443", "embryo", null, 1),
                new DevStage("8967786", "adult", "adult desc", 2)));
    }

    private List<Gene> getGeneDataTest() {
        log.entry();
        return log.exit(Arrays.asList(
                new Gene("ID1", "9606"),
                new Gene("ID2", "9606"),
                new Gene("ID3", "10090")));
    }
}
