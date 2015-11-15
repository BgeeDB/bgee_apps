package org.bgee.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.IncorrectRequest;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.model.ServiceFactory;
import org.bgee.model.TaskManager;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.gene.Gene;
import org.bgee.model.species.Species;
import org.bgee.model.topanat.TopAnatController;
import org.bgee.model.topanat.TopAnatParams;
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
    private final static Integer DEV_STAGE_LEVEL = 2;
    
    /**
     * Comparator sorting gene count by species map by gene count then species ID
     */
    private final static Comparator<Entry<String, Long>> SPECIES_COUNT_COMPARATOR =
            (Entry<String, Long> o1, Entry<String, Long> o2) -> {
                if (o2.getValue().equals(o1.getValue())) {
                    return o2.getKey().compareTo(o1.getKey()); 
                } 
                return o1.getValue().compareTo(o2.getValue());
            };

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
     * @param serviceFactory    A {@code ServiceFactory} that provides bgee services.
     */
    public CommandTopAnat(HttpServletResponse response, RequestParameters requestParameters, 
            BgeeProperties prop, ViewFactory viewFactory, ServiceFactory serviceFactory) {
        super(response, requestParameters, prop, viewFactory, serviceFactory);
    }

    @Override
    public void processRequest() throws Exception {
        log.entry();
        
        TopAnatDisplay display = this.viewFactory.getTopAnatDisplay();
        
        // AJAX gene list upload 
        if (this.requestParameters.isATopAnatGeneUpload()) {
            
            this.processGeneUpload(display);
            
        // Job submission, response 1: job not started
        } else if (this.requestParameters.isATopAnatNewJob()) {
            
            // TODO Create the ID to track job
            int jobTrackingId = 0;
            
            // Get params
            List<String> foregroundIds = this.requestParameters.getForegroundList();
            List<String> backgroundIds = this.requestParameters.getBackgroundList(); 
            List<String> callTypes = this.requestParameters.getExprType(); 
            String dataQuality = this.requestParameters.getDataQuality(); 
            List<String> dataTypes = this.requestParameters.getDataType(); 
            List<String> devStages = this.requestParameters.getDevStage(); 
            String decorrelationType = this.requestParameters.getDecorrelationType(); 
            Integer nodeSize = this.requestParameters.getNodeSize(); 
            Integer nbNodes = this.requestParameters.getNbNode(); 
            Float fdrThr = this.requestParameters.getFdrThreshold(); 
            Float pValueThr = this.requestParameters.getPValueThreshold();

            // un topanat par analyse et une analyse = 1call type & un stade de dev
            List<TopAnatParams> allTopAnatParams = new ArrayList<TopAnatParams>();
            for (String callType: callTypes) {
                for (String devStage: devStages) {
//                    TopAnatParams.Builder builder = new TopAnatParams.Builder(
//                            foregroundIds, backgroundIds, "", CallType.Expression);
//                    //TODO where convert data quality from String to DataQuality
//                    if (dataQuality.equals("all")) {
//                        //TODO do we need to set data quality if all qualities are requested
//                        builder.dataQuality(DataQuality.LOW);
//                    } else {
//                        builder.dataQuality(DataQuality.HIGH);
//                    }
//                    //TODO where convert data types from String to DataType
//                    builder.dataTypes(dataTypes);
//                    builder.devStageId(devStage);
//                    //TODO where convert decorelation type from String to DecorelationType
//                    builder.decorelationType(decorelationType);
//                    builder.nodeSize(nodeSize);
//                    builder.numberOfSignificantNode(nbNodes);
//                    builder.fdrThreshold(fdrThr);
//                    builder.pvalueThreshold(pValueThr);
//                    allTopAnatParams.add(builder.build());
                }
            }
            TopAnatController controller =
                    new TopAnatController(allTopAnatParams, prop, serviceFactory);

            // Check if results are already cached
            boolean cached = false;
            if (cached) {
                //TODO: update instantiation of TopAnatResults
                TopAnatResults results = new TopAnatResults(null, null, null, null);
                display.sendResultResponse(results);
                // - Or should the client redirects itself to a new page to display results, 
                //   using an URL provided in this response?
                
            } else {
                // Launch the TopAnat analysis
                
                // Request server and get response
                // - "admin" URL, allowing to come back at any moment to track job advancement
                // - advancement status (could be hardcoded, e.g., "starting job")
                display.sendNewJobResponse(jobTrackingId);
            }

        // Job submission: job tracking
        } else if (this.requestParameters.isATopAnatTrackingJob()) {
            // Get params
            Integer jobID = this.requestParameters.getJobId(); 
            String formData = this.requestParameters.getFormData();
            
            if (jobID == null || formData == null) {
                display.sendJobErrorResponse(null);
            }
            
            // Retrieve task manager associated to the provided ID
            TaskManager taskManager = TaskManager.getTaskManager(jobID);
            if (taskManager == null) {
                // We did not know that job
                display.sendJobErrorResponse(taskManager);
            } else if (taskManager.isSuccessful()) {
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
        } else if (this.requestParameters.isATopAnatFormDataUpload()) {
            // Get params:
            String formData = this.requestParameters.getFormData(); 

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
    
    private void processGeneUpload(TopAnatDisplay display) {
        log.entry(display);

        //retrieve possible parameters for this query
        final String fgFile = this.requestParameters.getForegroundFile();
        final String bgFile = this.requestParameters.getBackgroundFile();
        final List<String> fgList = Collections.unmodifiableList(Optional.ofNullable(
                this.requestParameters.getForegroundList()).orElse(new ArrayList<>()));
        final List<String> bgList = Collections.unmodifiableList(Optional.ofNullable(
                this.requestParameters.getBackgroundList()).orElse(new ArrayList<>()));
        
        //sanity checks
        if (StringUtils.isBlank(fgFile) && StringUtils.isBlank(bgFile) && 
            fgList.isEmpty() && bgList.isEmpty()) {
            throw log.throwing(new IllegalStateException("A gene ID list must be provided "
                    + "through either file upload or request parameter."));
        }
        if (StringUtils.isNotBlank(fgFile) && StringUtils.isNotBlank(bgFile) || 
            !fgList.isEmpty() && !bgList.isEmpty()) {
            throw log.throwing(new IllegalStateException("It is not possible to submit both "
                    + "a foreground and a background gene ID list at the same time"));
        }
        if ((StringUtils.isNotBlank(fgFile) || StringUtils.isNotBlank(bgFile)) && 
            (!fgList.isEmpty() || !bgList.isEmpty())) {
            throw log.throwing(new IllegalStateException("It is not possible to submit a gene ID list "
                    + "through both file upload and request parameter at the same time"));
        }
        
        //OK, start processing the query. First, retrieve the gene list.
        String fileToUse = null;
        if (StringUtils.isNotBlank(fgFile)) {
            fileToUse = fgFile;
        } else if (StringUtils.isNotBlank(bgFile)) {
            fileToUse = bgFile;
        }

        Set<String> submittedGeneIds = null;
        boolean isFileUpdoad = false;
        if (fileToUse != null) {
            isFileUpdoad = true;
            submittedGeneIds = this.getGeneIdsFromFile(fileToUse);
            if (submittedGeneIds.isEmpty()) {
                throw log.throwing(new IncorrectRequest("A file supposed to contain a gene ID list "
                        + "was provided, but it is empty or incorrectly formatted."));
            }
        } else if (!fgList.isEmpty()) {
            submittedGeneIds = new HashSet<>(fgList);
        } else if (!bgList.isEmpty()) {
            submittedGeneIds = new HashSet<>(bgList);
        } else {
            throw log.throwing(new AssertionError("Code supposed to be unreachable."));
        }
        

        // Load valid submitted gene IDs
        final Set<Gene> validGenes = new HashSet<>(this.getGenes(null, submittedGeneIds));
        // Identify undetermined gene IDs
        final Set<String> undeterminedGeneIds = new HashSet<>(submittedGeneIds);
        undeterminedGeneIds.removeAll(validGenes.stream()
                .map(Gene::getId)
                .collect(Collectors.toSet()));
        
        // Map species ID to valid gene ID count
        final Map<String, Long> speciesIdToGeneCount = validGenes.stream()
                    .collect(Collectors.groupingBy(Gene::getSpeciesId, Collectors.counting()));
        // Retrieve detected species, and create a new Map Species -> Long
        final Map<Species, Long> speciesToGeneCount = speciesIdToGeneCount.isEmpty()? new HashMap<>(): 
                this.serviceFactory.getSpeciesService()
                .loadSpeciesByIds(speciesIdToGeneCount.keySet())
                .stream()
                .collect(Collectors.toMap(spe -> spe, spe -> speciesIdToGeneCount.get(spe.getId())));
        // Determine selected species ID. 
        String selectedSpeciesId = speciesIdToGeneCount.entrySet().stream()
                //sort based on gene count (and in case of equality, based on species ID)
                .max((e1, e2) -> {
                    if (e1.getValue().equals(e2.getValue())) {
                        return e1.getKey().compareTo(e2.getKey()); 
                    } 
                    return e1.getValue().compareTo(e2.getValue());
                })
                .map(e -> e.getKey())
                .orElse(null);
        // Load valid stages for selected species
        Set<DevStage> validStages = null;
        if (selectedSpeciesId != null) {
            validStages = this.getGroupingDevStages(selectedSpeciesId, DEV_STAGE_LEVEL);
        }

        // Determine message
        String msg = this.getGeneUploadResponseMessage(submittedGeneIds, speciesToGeneCount, 
                undeterminedGeneIds);

        // We do not return submitted gene IDs if they were not extracted from a file.
        if (!isFileUpdoad) {
            submittedGeneIds = null;
        }
        
        // Send response
        display.sendGeneListReponse(speciesToGeneCount, selectedSpeciesId,
                validStages, submittedGeneIds, undeterminedGeneIds, msg);
        log.exit();
    }

    /**
     * Get submitted gene IDs from a file.
     * 
     * @param fileName  A {@code String} that is the path to the file to retrieve gene IDs from.
     * @return          The {@code Set} of {@code String}s that are submitted gene IDs.
     */
    private Set<String> getGeneIdsFromFile(String fileName) {
        log.entry(fileName);
        //TODO: sanity check on the size of the file.
        //TODO: how to access to uploaded file on Tomcat?
        return null;
    }

    /**
     * Build message according to submitted gene IDs, the gene count by species,
     * and the undetermined gene IDs.
     * 
     * @param submittedGeneIds      A {@code Set} of {@code String}s that are submitted gene IDs.
     * @param speciesToGeneCount    A {@code Map} where keys are {@code Species} objects, 
     *                              the associated values being a {@code Long} that are gene ID 
     *                              count found in the species.
     * @param undeterminedGeneIds   A {@code Set} of {@code String}s that are submitted gene IDs
     *                              from undetermined species.
     * @return                      A {@code String} that is the message to display.
     */
    private String getGeneUploadResponseMessage(Set<String> submittedGeneIds, 
            Map<Species, Long> speciesToGeneCount, Set<String> undeterminedGeneIds) {
        log.entry(submittedGeneIds, speciesToGeneCount, undeterminedGeneIds);
        
        StringBuilder msg = new StringBuilder();
        msg.append(submittedGeneIds.size());
        msg.append(" genes entered");
        if (!speciesToGeneCount.isEmpty()) {
            msg.append(speciesToGeneCount.entrySet().stream()
                //sort in descending order of gene count (and in case of equality, 
                //by ascending order of key, for predictable message generation)
                .sorted((e1, e2) -> {
                    if (e1.getValue().equals(e2.getValue())) {
                        return e1.getKey().getId().compareTo(e2.getKey().getId()); 
                    } 
                    return e2.getValue().compareTo(e1.getValue());
                })
                .map(e -> ", " + e.getValue() + " in " + e.getKey().getName())
                .collect(Collectors.joining()));
        }
        if (!undeterminedGeneIds.isEmpty()) {
            msg.append(", ");
            msg.append(undeterminedGeneIds.size());
            msg.append(" not found in Bgee");
        }
        msg.append(".");
        
        return log.exit(msg.toString());
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
        return log.exit(genes);
    }
    
    /**
     * Get the {@code Set} of {@code DevStage}s for the given {@code speciesId}.
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
        // TODO filter level in DAO
        return log.exit(devStages.stream()
                .filter(e -> e.getLevel() == level)
                .collect(Collectors.toSet()));
    }

}
