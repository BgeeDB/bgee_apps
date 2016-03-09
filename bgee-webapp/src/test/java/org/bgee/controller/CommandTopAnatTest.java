package org.bgee.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.TestAncestor;
import org.bgee.controller.CommandTopAnat.GeneListResponse;
import org.bgee.controller.CommandTopAnat.JobResponse;
import org.bgee.controller.utils.MailSender;
import org.bgee.model.ServiceFactory;
import org.bgee.model.TaskManager;
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
     * Test {@link CommandTopAnat#processRequest()} for a gene list validation
     * @throws Exception 
     */
    @Test
    public void shouldProcessRequestGeneValidation() throws Exception {
        
        //mock Services
        ServiceFactory serviceFac = mock(ServiceFactory.class);
        
        GeneService geneService = mock(GeneService.class);
        when(serviceFac.getGeneService()).thenReturn(geneService);
        
        DevStageService devStageService = mock(DevStageService.class);
        when(serviceFac.getDevStageService()).thenReturn(devStageService);
        
        SpeciesService speciesService = mock(SpeciesService.class);
        when(serviceFac.getSpeciesService()).thenReturn(speciesService);

        //mock data returned by Services
        List<String> fgSubmittedGeneIds = Arrays.asList("ID3");
        TreeSet<String> fgNotSelectedGeneIdSet = new TreeSet<>();
        String fgSelectedSpeciesId = "10090";

        List<String> bgSubmittedGeneIds = Arrays.asList("ID1", "ID2", "ID3", "ID4");
        TreeSet<String> bgNotSelectedGeneIdSet = new TreeSet<>(Arrays.asList("ID3"));
        String bgSelectedSpeciesId = "9606";

        List<Gene> fgGenes = Arrays.asList(new Gene("ID3", "10090"));
        when(geneService.loadGenesByIdsAndSpeciesIds(new HashSet<>(fgSubmittedGeneIds), null))
            .thenReturn(fgGenes);

        List<Gene> bgGenes = Arrays.asList(
                new Gene("ID1", "9606"), new Gene("ID2", "9606"), new Gene("ID3", "10090"));
        when(geneService.loadGenesByIdsAndSpeciesIds(new HashSet<>(bgSubmittedGeneIds), null))
            .thenReturn(bgGenes);
        
        TreeSet<String> fgUndeterminedGeneIds = new TreeSet<>();

        TreeSet<String> bgUndeterminedGeneIds = new TreeSet<>(bgSubmittedGeneIds);
        bgUndeterminedGeneIds.removeAll(bgGenes.stream()
                .map(Gene::getId)
                .collect(Collectors.toSet()));

        Set<DevStage> devStages = new HashSet<>(Arrays.asList(
                new DevStage("25", "embryo", "embryo desc", 2, 3, 2, false, true),
                new DevStage("26", "adult", "adult desc", 4, 5, 2, false, true)));
        when(devStageService.loadGroupingDevStages(
                Arrays.asList(fgSelectedSpeciesId), 2))
            .thenReturn(devStages);
        when(devStageService.loadGroupingDevStages(
                Arrays.asList(bgSelectedSpeciesId), 2))
            .thenReturn(devStages);

        List<Species> fgSpecies = Arrays.asList(
                new Species("10090", "mouse", "", "Mus", "musculus", "genome10090"));
        when(speciesService.loadSpeciesByIds(new HashSet<>(Arrays.asList("10090"))))
            .thenReturn(new HashSet<>(fgSpecies));

        List<Species> bgSpecies = Arrays.asList(
                new Species("9606", "human", "", "Homo", "sapiens", "genome9606"), 
                new Species("10090", "mouse", "", "Mus", "musculus", "genome10090"));
        when(speciesService.loadSpeciesByIds(new HashSet<>(Arrays.asList("9606", "10090"))))
            .thenReturn(new HashSet<>(bgSpecies)).thenReturn(new HashSet<>(bgSpecies));

        //mock view
        ViewFactory viewFac = mock(ViewFactory.class);
        TopAnatDisplay display = mock(TopAnatDisplay.class);
        when(viewFac.getTopAnatDisplay()).thenReturn(display);
        //mock mail
        MailSender mailSender = mock(MailSender.class);
        
        // Launch tests
        RequestParameters params = new RequestParameters();
        params.setPage(RequestParameters.PAGE_TOP_ANAT);
        params.setAction(RequestParameters.ACTION_TOP_ANAT_GENE_VALIDATION);
        params.addValues(params.getUrlParametersInstance().getParamBackgroundList(), bgSubmittedGeneIds);
        params.addValues(params.getUrlParametersInstance().getParamForegroundList(), fgSubmittedGeneIds);
        log.info("Generated query URL: " + params.getRequestURL());

        CommandTopAnat controller = new CommandTopAnat(mock(HttpServletResponse.class),
                params, mock(BgeeProperties.class), viewFac, serviceFac, null, mailSender);
        controller.processRequest();

        LinkedHashMap<String, Long> fgSpToGeneCount = new LinkedHashMap<>();
        fgSpToGeneCount.put(fgSpecies.get(0).getId(), 1L);

        LinkedHashMap<String, Long> bgSpToGeneCount = new LinkedHashMap<>();
        bgSpToGeneCount.put(bgSpecies.get(0).getId(), 2L);
        bgSpToGeneCount.put(bgSpecies.get(1).getId(), 1L);
        bgSpToGeneCount.put("UNDETERMINED", 1L);
        
        TreeMap<String, Species> fgDetectedSpecies = new TreeMap<>();
        fgDetectedSpecies.put(fgSpecies.get(0).getId(), fgSpecies.get(0));

        TreeMap<String, Species> bgDetectedSpecies = new TreeMap<>();
        bgDetectedSpecies.put(bgSpecies.get(0).getId(), bgSpecies.get(0));
        bgDetectedSpecies.put(bgSpecies.get(1).getId(), bgSpecies.get(1));
        
        GeneListResponse response1 = new GeneListResponse(
                fgSpToGeneCount, fgDetectedSpecies, fgSelectedSpeciesId, 
                devStages, fgNotSelectedGeneIdSet, fgUndeterminedGeneIds);
        GeneListResponse response2 = new GeneListResponse(
                bgSpToGeneCount, bgDetectedSpecies, bgSelectedSpeciesId, 
                devStages, bgNotSelectedGeneIdSet, bgUndeterminedGeneIds);
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put(params.getUrlParametersInstance().getParamForegroundList().getName(),
                response1);
        data.put(params.getUrlParametersInstance().getParamBackgroundList().getName(),
                response2);
        
        verify(display).sendGeneListReponse(data, 
                "1 genes entered, 1 in mouse in Bgee for " + 
                        params.getUrlParametersInstance().getParamForegroundList().getName() + "\n" +
                "4 genes entered, 2 in human, 1 in mouse, 1 not found in Bgee for " + 
                        params.getUrlParametersInstance().getParamBackgroundList().getName());
    }
    
    /**
     * Test {@link CommandTopAnat#processRequest()} for a job tracking.
     * @throws Exception 
     */
//    @Test
    //TODO activate this test. It's not activate because I need to continue
    public void shouldProcessRequestJobTracking() throws Exception {

        Integer jobId = 10;
        String keyParam="ab3d";

        //mock Services
        ServiceFactory serviceFac = mock(ServiceFactory.class);
        
        TaskManager taskManager = mock(TaskManager.class);
        when(TaskManager.getTaskManager(jobId)).thenReturn(taskManager);
        when(taskManager.isTerminated()).thenReturn(true);

        GeneService geneService = mock(GeneService.class);
        when(serviceFac.getGeneService()).thenReturn(geneService);
        
        DevStageService devStageService = mock(DevStageService.class);
        when(serviceFac.getDevStageService()).thenReturn(devStageService);
        
        SpeciesService speciesService = mock(SpeciesService.class);
        when(serviceFac.getSpeciesService()).thenReturn(speciesService);

        //mock data returned by Services
        
        //mock view
        ViewFactory viewFac = mock(ViewFactory.class);
        TopAnatDisplay display = mock(TopAnatDisplay.class);
        when(viewFac.getTopAnatDisplay()).thenReturn(display);
        //mock mail
        MailSender mailSender = mock(MailSender.class);
        
        // Launch tests
        RequestParameters params = new RequestParameters();
        params.setPage(RequestParameters.PAGE_TOP_ANAT);
        params.setAction(RequestParameters.ACTION_TOP_ANAT_TRACKING_JOB);
        params.addValue(params.getUrlParametersInstance().getParamJobId(), jobId);
        params.addValue(params.getUrlParametersInstance().getParamData(), keyParam);
        log.info("Generated query URL: " + params.getRequestURL());

        CommandTopAnat controller = new CommandTopAnat(mock(HttpServletResponse.class),
                params, mock(BgeeProperties.class), viewFac, serviceFac, null, mailSender);
        controller.processRequest();

        JobResponse response1 = new JobResponse(jobId, "DONE", keyParam);
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("job_response", response1);
        
        verify(display).sendTrackingJobResponse(data, "message");
    }
}
