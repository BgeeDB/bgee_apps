package org.bgee.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        SpeciesService speciesService = mock(SpeciesService.class);
        when(serviceFac.getSpeciesService()).thenReturn(speciesService);
        DevStageService devStageService = mock(DevStageService.class);
        when(serviceFac.getDevStageService()).thenReturn(devStageService);

        //mock data returned by Services
        Set<String> speciesIds = new HashSet<String>();
        speciesIds.addAll(Arrays.asList("11"));
        Set<String> geneIds = new HashSet<String>();
        geneIds.addAll(Arrays.asList("ID1", "ID3", "ID4"));

        List<Gene> genes = this.getGeneDataTest();
        when(geneService.loadGenesByIdsAndSpeciesIds(geneIds, speciesIds)).thenReturn(genes);
        List<DevStage> devStages = this.getDevStagesDataTest();
        when(devStageService.loadGroupingDevStages(speciesIds)).thenReturn(devStages);

        //mock view
        ViewFactory viewFac = mock(ViewFactory.class);
        TopAnatDisplay display = mock(TopAnatDisplay.class);
        when(viewFac.getTopAnatDisplay()).thenReturn(display);
        
        // Launch tests
        RequestParameters params = new RequestParameters();
        params.setPage(RequestParameters.PAGE_TOP_ANAT);
        params.setAction(RequestParameters.ACTION_GENE_LIST_UPLOAD);
        params.setGeneIds(geneIds);
        CommandTopAnat controller = new CommandTopAnat(mock(HttpServletResponse.class), params, 
                mock(BgeeProperties.class), viewFac);
        controller.processRequest();

        //TODO finish test
        Map<String, Long> speciesIdToGeneCount= null;
        String selectedSpeciesId = "11";
        Set<DevStage> validStages= null;
        Set<String> underterminedGeneIds = null;
        int statusCode= -1;
        String msg= null;
        
//        verify(display).sendGeneListReponse(speciesIdToGeneCount,
//                selectedSpeciesId, validStages, underterminedGeneIds, statusCode, msg);
    }

    private List<DevStage> getDevStagesDataTest() {
        log.entry();
        
        List<DevStage> validStages = new ArrayList<DevStage>();
        validStages.add(new DevStage("2443", "embryo", null, 1));
        validStages.add(new DevStage("8967786", "adult", "adult desc", 2));
        
        return log.exit(validStages);
    }

    private Set<Species> getSpeciesDataTest() {
        log.entry();

        Set<Species> allDetectedSpecies = new HashSet<Species>();
        allDetectedSpecies.addAll(Arrays.asList(
                new Species("10090", "mouse", null, "Mus", "musculus", "mmus1"),
                new Species("9606", "human", "human desc", "Homo", "sapiens", "hsap1")));
        return log.exit(allDetectedSpecies);
    }

    private List<Gene> getGeneDataTest() {
        log.entry();

//        Map<String, Long> speciesIdToGeneCount = new HashMap<String, Long>();
//        speciesIdToGeneCount.put("9606", 50L);
//        speciesIdToGeneCount.put("10090", 20L);
//        speciesIdToGeneCount.put("UNDETERMINED", 20L);
//        
//        return log.exit(speciesIdToGeneCount);
        return null;
    }
}
