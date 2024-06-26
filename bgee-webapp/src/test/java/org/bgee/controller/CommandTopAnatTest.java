package org.bgee.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.TestAncestor;
import org.bgee.controller.CommandTopAnat.GeneListResponse;
import org.bgee.controller.CommandTopAnat.JobResponse;
import org.bgee.controller.user.User;
import org.bgee.controller.utils.MailSender;
import org.bgee.model.ServiceFactory;
import org.bgee.model.job.Job;
import org.bgee.model.job.JobService;
import org.bgee.model.source.Source;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.DevStageService;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
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
 * @version Bgee 14, Mar. 2017
 * @since   Bgee 13, Nov. 2015
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
        JobService jobService = mock(JobService.class);
        User user = mock(User.class);
        
        GeneService geneService = mock(GeneService.class);
        when(serviceFac.getGeneService()).thenReturn(geneService);
        
        DevStageService devStageService = mock(DevStageService.class);
        when(serviceFac.getDevStageService()).thenReturn(devStageService);
        
        SpeciesService speciesService = mock(SpeciesService.class);
        when(serviceFac.getSpeciesService()).thenReturn(speciesService);

        //mock data returned by Services
        List<String> fgSubmittedGeneIds = Arrays.asList("OtherID3");

        TreeSet<String> fgNotSelectedGeneIdSet = new TreeSet<>();
        Integer fgSelectedSpeciesId = 10090;
        Species fgSelectedSpecies = new Species(fgSelectedSpeciesId);

        List<String> bgSubmittedGeneIds = Arrays.asList("ID1", "OtherID2", "ID3", "ID4");
        TreeSet<String> bgNotSelectedGeneIdSet = new TreeSet<>(Arrays.asList("ID3"));
        Integer bgSelectedSpeciesId = 9606;
        Species bgSelectedSpecies = new Species(9606);

        List<Gene> fgGenes = Arrays.asList(new Gene("ID3", fgSelectedSpecies, new GeneBioType("type1")));
        Map<String, Set<Gene>> mapFg = new HashMap<>();
        mapFg.put("OtherID3", new HashSet<>(Arrays.asList(
                new Gene("ID3", fgSelectedSpecies, new GeneBioType("type1")))));
        when(geneService.loadGenesByAnyId(new TreeSet<>(fgSubmittedGeneIds), false)).thenReturn(mapFg.entrySet().stream());

        List<Gene> bgGenes = Arrays.asList(
                new Gene("ID1", bgSelectedSpecies, new GeneBioType("type1")),
                new Gene("ID2", bgSelectedSpecies, new GeneBioType("type1")),
                new Gene("ID3", fgSelectedSpecies, new GeneBioType("type1")));
        Map<String, Set<Gene>> mapBg = new HashMap<>();
        mapBg.put("ID1", new HashSet<>(Arrays.asList(new Gene("ID1", bgSelectedSpecies, new GeneBioType("type1")))));
        mapBg.put("OtherID2", new HashSet<>(Arrays.asList(new Gene("ID2", bgSelectedSpecies, new GeneBioType("type1")))));
        mapBg.put("ID3", new HashSet<>(Arrays.asList(new Gene("ID3", fgSelectedSpecies, new GeneBioType("type1")))));
        mapBg.put("ID4", new HashSet<>());
        when(geneService.loadGenesByAnyId(new TreeSet<>(bgSubmittedGeneIds), false)).thenReturn(mapBg.entrySet().stream());

        when(geneService.loadGenesByIds(new TreeSet<>(fgSubmittedGeneIds))).thenReturn(fgGenes.stream());
        when(geneService.loadGenesByIds(new TreeSet<>(bgSubmittedGeneIds))).thenReturn(bgGenes.stream());
        TreeSet<String> fgUndeterminedGeneIds = new TreeSet<>();
        TreeSet<String> bgUndeterminedGeneIds = new TreeSet<>(Arrays.asList("ID4"));

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
                new Species(10090, "mouse", "", "Mus", "musculus", "genome10090", "assembly10090", new Source(1), null, null, null, null, null));
        when(speciesService.loadSpeciesByIds(new HashSet<>(Arrays.asList(10090)), false))
            .thenReturn(new HashSet<>(fgSpecies));

        List<Species> bgSpecies = Arrays.asList(
                new Species(9606, "human", "", "Homo", "sapiens", "genome9606", "assembly9606", new Source(1), null, null, null, null, null),
                new Species(10090, "mouse", "", "Mus", "musculus", "genome10090", "assembly10090", new Source(1), null, null, null, null, null));
        when(speciesService.loadSpeciesByIds(new HashSet<>(Arrays.asList(9606, 10090)), false))
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
                params, mock(BgeeProperties.class), viewFac, serviceFac, jobService, user, 
                null, mailSender);
        controller.processRequest();

        LinkedHashMap<Integer, Long> fgSpToGeneCount = new LinkedHashMap<>();
        fgSpToGeneCount.put(fgSpecies.get(0).getId(), 1L);

        LinkedHashMap<Integer, Long> bgSpToGeneCount = new LinkedHashMap<>();
        bgSpToGeneCount.put(bgSpecies.get(0).getId(), 2L);
        bgSpToGeneCount.put(bgSpecies.get(1).getId(), 1L);
        bgSpToGeneCount.put(-1, 1L);
        
        TreeMap<Integer, Species> fgDetectedSpecies = new TreeMap<>();
        fgDetectedSpecies.put(fgSpecies.get(0).getId(), fgSpecies.get(0));

        TreeMap<Integer, Species> bgDetectedSpecies = new TreeMap<>();
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
                "1 IDs provided: 1 unique genes found in Bgee, 1 in mouse for " +
                        params.getUrlParametersInstance().getParamForegroundList().getName() + "\n" +
                "4 IDs provided: 2 unique genes found in Bgee, 2 in human, 1 in mouse, " +
                        "and 1 IDs not found for " +
                        params.getUrlParametersInstance().getParamBackgroundList().getName());
    }
    
    /**
     * Test {@link CommandTopAnat#processRequest()} for a job tracking.
     * @throws Exception 
     */
    @Test
    public void shouldProcessRequestJobTracking() throws Exception {

        Integer jobId = 10;
        String keyParam="ab3d";

        //mock Services
        ServiceFactory serviceFac = mock(ServiceFactory.class);
        JobService jobService = mock(JobService.class);
        
        Job job = mock(Job.class);
        when(jobService.getJob(jobId)).thenReturn(job);
        when(job.isTerminated()).thenReturn(true);
        User user = mock(User.class);

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
                params, mock(BgeeProperties.class), viewFac, serviceFac, jobService, user, 
                null, mailSender);
        controller.processRequest();

        JobResponse response = new JobResponse(jobId, "UNDEFINED", keyParam);
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("jobResponse", response);
        
        verify(display).sendTrackingJobResponse(data, "Job is UNDEFINED");

        when(job.isTerminated()).thenReturn(false);
        controller.processRequest();
        response = new JobResponse(jobId, "RUNNING", keyParam);
        data = new LinkedHashMap<>();
        data.put("jobResponse", response);
        
        verify(display).sendTrackingJobResponse(data, "Job is RUNNING");
    }
}
