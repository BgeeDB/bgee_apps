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
import org.bgee.model.species.Species;
import org.bgee.model.species.SpeciesService;
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
        
        SpeciesService speciesService = mock(SpeciesService.class);
        when(serviceFac.getSpeciesService()).thenReturn(speciesService);

        //mock data returned by Services
        List<String> submittedGeneIds = Arrays.asList("ID1", "ID2", "ID3", "ID4");
        String selectedSpeciesId = "9606";

        List<Gene> genes = this.getGeneDataTest();
        when(geneService.loadGenesByIdsAndSpeciesIds(new HashSet<>(submittedGeneIds), null))
            .thenReturn(genes);

        List<DevStage> devStages = this.getDevStagesDataTest();
        when(devStageService.loadGroupingDevStages(new HashSet<>(Arrays.asList(selectedSpeciesId))))
            .thenReturn(devStages);

        List<Species> species = this.getSpeciesDataTest();
        when(speciesService.loadSpeciesByIds(new HashSet<>(Arrays.asList("9606", "10090"))))
            .thenReturn(new HashSet<>(species));

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

        Map<Species, Long> speciesToGeneCount = new HashMap<>();
        speciesToGeneCount.put(species.get(0), 2L);
        speciesToGeneCount.put(species.get(1), 1L);
        
        verify(display).sendGeneListReponse(speciesToGeneCount, selectedSpeciesId,
                new HashSet<>(Arrays.asList(devStages.get(1), devStages.get(2))), 
                null, new HashSet<>(Arrays.asList("ID4")), 0, 
                "4 genes entered, 2 in human, 1 in mouse, "
                + "1 not found in Bgee.");
    }

    private List<DevStage> getDevStagesDataTest() {
        log.entry();
        return log.exit(Arrays.asList(
                new DevStage("2443", "Life stage", null, 1, 6, 1, false, true),
                new DevStage("25", "embryo", "embryo desc", 2, 3, 2, false, true),
                new DevStage("26", "adult", "adult desc", 4, 5, 2, false, true)));
    }

    private List<Species> getSpeciesDataTest() {
        log.entry();
        return log.exit(Arrays.asList(
                new Species("9606", "human", "", "Homo", "sapiens", "genome9606"), 
                new Species("10090", "mouse", "", "Mus", "musculus", "genome10090")));
    }

    private List<Gene> getGeneDataTest() {
        log.entry();
        return log.exit(Arrays.asList(
                new Gene("ID1", "9606"),
                new Gene("ID2", "9606"),
                new Gene("ID3", "10090")));
    }
}
