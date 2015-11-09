package org.bgee.controller;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.model.TaskManager;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.gene.Gene;
import org.bgee.model.topanat.TopAnatResults;
import org.bgee.view.TopAnatDisplay;
import org.bgee.view.ViewFactory;

/**
 * Controller handling requests relative to topAnat.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 13 Nov 2015
 * @since   Bgee 13
 */
public class CommandTopAnat extends CommandParent {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(CommandTopAnat.class.getName());
    
    /**
     * An {@code Integer} that is the level to be used to filter retrieved dev. stages. 
     */
    private final static Integer DEV_STAGE_LEVEL = 1;
    
    /**
     * A {@code String} that is the label of the count of genes whose the species is undetermined. 
     */
    private final static String UNDETERMINED_SPECIES_LABEL = "UNDETERMINED";
    
    /**
     * Constructor providing necessary dependencies. 
     * 
     * @param response          A {@code HttpServletResponse} that will be used 
     *                          to display the page to the client
     * @param requestParameters The {@code RequestParameters} that handles 
     *                          the parameters of the current request.
     * @param prop              A {@code BgeeProperties} instance that contains 
     *                          the properties to use.
     * @param viewFactory       A {@code ViewFactory} providing the views of the appropriate 
     *                          display type.
     */
    public CommandTopAnat(HttpServletResponse response,
            RequestParameters requestParameters, BgeeProperties prop,
            ViewFactory viewFactory) {
        super(response, requestParameters, prop, viewFactory);
    }

    @Override
    public void processRequest() throws Exception {
        log.entry();
        
        TopAnatDisplay display = this.viewFactory.getTopAnatDisplay();
        
        // AJAX gene list upload 
        if (this.requestParameters.isATopAnatGeneListUpload()) {
            // Get submitted gene IDs (either from file upload, or from copy/paste in textarea)
            Set<String> submittedGeneIds = this.getSubmittedGeneIdsList();

            // Load valid submitted gene IDs
            List<Gene> validGenes = this.getGenes(null, submittedGeneIds);

            // Map species ID to valid gene ID count
            Map<String, Long> speciesIdToGeneCount = 
                    validGenes.stream().collect(Collectors.groupingBy(Gene::getSpeciesId, Collectors.counting()));

            // Count number of undetermined genes
            Set<String> validGeneIds = validGenes.stream()
                    .map(Gene::getId)
                    .collect(Collectors.toSet());
            assert validGeneIds.containsAll(submittedGeneIds);
            Long undeterminedGeneCount = submittedGeneIds.stream()
                    .filter(e -> validGeneIds.contains(e))
                    .collect(Collectors.counting());
            speciesIdToGeneCount.put(UNDETERMINED_SPECIES_LABEL, undeterminedGeneCount);
            Set<String> undeterminedGeneIds = submittedGeneIds.stream()
                    .filter(e -> !validGeneIds.contains(e))
                    .collect(Collectors.toSet());

            // Determine selected species ID
            String selectedSpeciesId = this.getSelectedSpecies(speciesIdToGeneCount);
            
            // Load valid stages for selected species
            Set<DevStage> validStages = this.getGroupingDevStages(selectedSpeciesId, DEV_STAGE_LEVEL);

            // Determine status code
            // TODO int? here always 0?
            int statusCode = 0;

            // Determine message
            String msg = getMessage(submittedGeneIds, speciesIdToGeneCount, undeterminedGeneIds);

            // Send response with association species -> gene count, selected species, 
            // and valid stages
            display.sendGeneListReponse(speciesIdToGeneCount,
                    selectedSpeciesId, validStages, undeterminedGeneIds, statusCode, msg);
            
        // Job submission, response 1: job not started
        } else if (this.requestParameters.isATopAnatNewJob()) {
            // Create or get param (auto-generated client-side? Produced server-side?) 
            // - the ID to track job
            int jobTrackingId = 0;
            
            // Get params
            // - data parameters hash obtained from the gene upload request
            // - all form parameters
            
            // Launch the TopAnat analyses / Check that results are cached
            boolean cached = false;
            if (cached) {
                //TODO: update instantiation of TopAnatResults
                TopAnatResults results = new TopAnatResults(null, null, null, null);
                display.sendResultResponse(results);
                // - Or should the client redirects itself to a new page to display results, 
                //   using an URL provided in this response?
                
            } else {
                // Request server and get response
                // - "admin" URL, allowing to come back at any moment to track job advancement
                // - advancement status (could be hardcoded, e.g., "starting job")
                display.sendNewJobResponse(jobTrackingId);
            }

        // Job submission: job tracking
        } else if (this.requestParameters.isATopAnatTrackingJob()) {
            // Get params
            // - ID to track job
            long jobTrackingId = 0L;

            // Request server and get response
            // - advancement status (real one, based on TaskManager)

            // Retrieve task manager associated to the provided ID
            TaskManager taskManager = TaskManager.getTaskManager(jobTrackingId);
            if (taskManager.isSuccessful()) {
                //retrieve results from the task held by the task manager
                //TODO: update instantiation of TopAnatResults
                TopAnatResults results = new TopAnatResults(null, null, null, null);
                display.sendResultResponse(results);
                //Or should the client redirects itself to a new page to display results, 
                //using an URL provided in this response?
                
            } else if (!taskManager.isTerminated()) {
                
                display.sendJobStatusResponse(taskManager);
            } else {
                
                display.sendJobErrorResponse(taskManager);
            }

        // Home page, with data parameters provided in URL
        } else if (this.requestParameters.isATopAnatHomePageWithData()) {
            // Get params:
            // - data parameters
            
            // Request server and get response
            // - information to pre-fill the form
            // - information to display results 
            //   (is it doable or do we get the results through an AJAX query only?)
            boolean hasResults = true;
            
            // Display page (using previous response)
            if (hasResults) {
                //TODO: update instantiation of TopAnatResults
                TopAnatResults results = new TopAnatResults(null, null, null, null);
                display.sendResultResponse(results);
            } else {
                display.displayTopAnatHomePage();
            }

        // Home page, empty
        } else if (this.requestParameters.getAction() == null) {
            // Display page
            display.displayTopAnatHomePage();
            
        } else {
            throw log.throwing(new PageNotFoundException("Incorrect " 
                + this.requestParameters.getUrlParametersInstance().getParamAction() 
                + " parameter value."));
        }

        log.exit();
    }

    /**
     * Determine the species to be used.
     * 
     * @param speciesIdToGeneCount  A {@code Map} where keys are species IDs, the associated values 
     *                              being a {@code Long} that are gene ID count found in the species.
     * @return                      A {@code String} that is the species ID to be used.
     */
    private String getSelectedSpecies(Map<String, Long> speciesIdToGeneCount) {
        log.entry(speciesIdToGeneCount);
        
        // We sort the map by gene count (value) then species ID (key)
        List<String> orderedSpeciesIdsByValue = speciesIdToGeneCount.entrySet().stream()
                .sorted((Entry<String, Long> o1, Entry<String, Long> o2) -> {
                    if (o1.getValue().equals(o2.getValue())) {
                        return o1.getKey().compareTo(o2.getKey()); 
                    } 
                    return o1.getValue().compareTo(o2.getValue());
                })
                .map(e -> e.getKey())
                .collect(Collectors.toList());
        
        return log.exit(orderedSpeciesIdsByValue.get(0));
    }

    /**
     * Get submitted gene IDs.
     * 
     * @return The {@code Set} of {@code String}s that are submitted gene IDs.
     */
    private Set<String> getSubmittedGeneIdsList() {
        // TODO Manage when it's a file
        return this.requestParameters.getGeneIds();
    }

    /**
     * Get the {@code List} of {@code Gene}s containing valid genes from a given list.
     * 
     * @param speciesIds    A {@code Set} of {@code String}s that are IDs of species 
     *                      for which to return the {@code Gene}s.
     * @param geneIds       A {@code Set} of {@code String}s that are IDs of genes 
     *                      for which to return the {@code Gene}s.
     * @return              A {@List} of {@code Gene}s that are valid genes.
     * @throws IllegalStateException    If the {@code GeneService} obtained from the 
     *                                  {@code ServiceFactory} did not allow
     *                                  to obtain any {@code Gene}.
     */
    private List<Gene> getGenes(Set<String> speciesIds, Set<String> geneIds) 
            throws IllegalStateException {
        log.entry(speciesIds, geneIds);
        List<Gene> genes = serviceFactory.getGeneService().
                loadGenesByIdsAndSpeciesIds(geneIds, speciesIds);
        if (genes.isEmpty()) {
            throw log.throwing(new IllegalStateException("A GeneService did not allow "
                    + "to obtain any Gene."));
        }
        return log.exit(genes);
    }
    
    /**
     * Get the {@code Set} of {@code DevStage}s containing valid genes from a given list.
     * 
     * @param speciesId     A {@code String}s that are ID of species 
     *                      for which to return the {@code DevStage}s.
     * @param level         As {@code Integer} that is the level of dev. stages 
     *                      for which to return the {@code DevStage}s.
     * @return              A {@List} of {@code DevStage}s that are dev. stages in the 
     *                      provided species at the provided level.
     * @throws IllegalStateException    If the {@code DevStageService} obtained from the 
     *                                  {@code ServiceFactory} did not allow
     *                                  to obtain any {@code DevStage}.
     */
    private Set<DevStage> getGroupingDevStages(String speciesId, Integer level) 
            throws IllegalStateException {
        log.entry(speciesId, level);
        List<DevStage> devStages = serviceFactory.getDevStageService().
                loadGroupingDevStages(new HashSet<String>(Arrays.asList(speciesId)));
        if (devStages.isEmpty()) {
            throw log.throwing(new IllegalStateException("A DevStageService did not allow "
                    + "to obtain any DevStage."));
        }
        return log.exit(devStages.stream()
                .filter(e -> e.getLevel() == level)
                .collect(Collectors.toSet()));
    }

    /**
     * Build message according to submitted gene IDs, the gene count by species,
     * and the undetermined gene IDs.
     * 
     * @param submittedGeneIds      A {@code Set} of {@code String}s that are submitted gene IDs.
     * @param speciesIdToGeneCount  A {@code Map} where keys are species IDs, the associated values 
     *                              being a {@code Long} that are gene ID count found in the species.
     * @param undeterminedGeneIds   A {@code Set} of {@code String}s that are submitted gene IDs
     *                              from undetermined species.
     * @return                      A {@code String} that is the message to display.
     */
    private String getMessage(Set<String> submittedGeneIds, Map<String, Long> speciesIdToGeneCount,
            Set<String> undeterminedGeneIds) {
        log.entry(submittedGeneIds, speciesIdToGeneCount, undeterminedGeneIds);
        
        StringBuilder msg = new StringBuilder();
        msg.append(submittedGeneIds.size());
        msg.append(" genes entered");
        if (!speciesIdToGeneCount.isEmpty()) {
            boolean isFirst = true;
            for (Entry<String, Long> entry : speciesIdToGeneCount.entrySet()) {
                if (isFirst) {
                    msg.append(", ");
                    isFirst = false;
                }
                msg.append(entry.getValue());
                msg.append(" from species ");
                msg.append(entry.getKey());
            }
        }
        if (!undeterminedGeneIds.isEmpty()) {
            msg.append(undeterminedGeneIds.size());
            msg.append(" from undetermined species");
        }
        msg.append(".");
        
        return log.exit(msg.toString());
    }
}
