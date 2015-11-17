package org.bgee.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.InvalidRequestException;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.model.ServiceFactory;
import org.bgee.model.TaskManager;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.gene.Gene;
import org.bgee.model.species.Species;
import org.bgee.model.topanat.TopAnatController;
import org.bgee.model.topanat.TopAnatParams;
import org.bgee.model.topanat.TopAnatResults;
import org.bgee.view.JsonHelper;
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
     * A {@code String} that is the label of the count of genes whose the species is undetermined. 
     */
    private final static String UNDETERMINED_SPECIES_LABEL = "UNDETERMINED";

    /**
     * A {@code List} of {@code String}s that are messages to be displayed.
     */
    private static List<String> messages;

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
        messages = new ArrayList<>();
    }

    @Override
    public void processRequest() throws IOException, PageNotFoundException, InvalidRequestException {
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
                TopAnatResults results = new TopAnatResults(null, null);
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
                TopAnatResults results = new TopAnatResults(null, null);
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
                TopAnatResults results = new TopAnatResults(null, null);
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
    
    private void processGeneUpload(TopAnatDisplay display) throws InvalidRequestException {
        log.entry(display);

        //retrieve possible parameters for this query
        final List<String> fgList = Collections.unmodifiableList(Optional.ofNullable(
                this.requestParameters.getForegroundList()).orElse(new ArrayList<>()));
        final List<String> bgList = Collections.unmodifiableList(Optional.ofNullable(
                this.requestParameters.getBackgroundList()).orElse(new ArrayList<>()));
        
        //sanity checks
        if (fgList.isEmpty() && bgList.isEmpty()) {
            throw log.throwing(new InvalidRequestException("A gene ID list must be provided"));
        }
        
        //OK, start processing the query. First, retrieve the gene list.
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        if (!fgList.isEmpty()) {
            data.put(
                    this.requestParameters.getUrlParametersInstance().getParamForegroundList().getName(),
                    this.getGeneResponse(fgList, 
                            this.requestParameters.getUrlParametersInstance().getParamForegroundList().getName()));
        } 
        if (!bgList.isEmpty()) {
            data.put(
                    this.requestParameters.getUrlParametersInstance().getParamBackgroundList().getName(),
                    this.getGeneResponse(bgList,
                            this.requestParameters.getUrlParametersInstance().getParamBackgroundList().getName()));
        }

        // Send response
        display.sendGeneListReponse(data, messages.stream().collect(Collectors.joining("\n")));
        log.exit();
    }

    /**
     * Build a {@code GeneListResponse}.
     * 
     * @param geneList  A {@code List} of {@code String}s that are IDs of genes.
     * @param paramName A {@code String} that is the name of the parameter.
     * @return          The {@code GeneListResponse} built from 
     *                  {@code geneList} and {@code paramName}.
     */
    private GeneListResponse getGeneResponse(List<String> geneList, String paramName) {
        log.entry(geneList, paramName);
        
        if (geneList.isEmpty()) {
            throw log.throwing(new AssertionError("Code supposed to be unreachable."));
        }

        TreeSet<String> geneSet = new TreeSet<>(geneList);
        // Load valid submitted gene IDs
        final Set<Gene> validGenes = new HashSet<>(this.getGenes(null, geneSet));
        // Identify undetermined gene IDs
        final Set<String> undeterminedGeneIds = new HashSet<>(geneSet);
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
        messages.add(this.getGeneUploadResponseMessage(geneSet, speciesToGeneCount, 
                undeterminedGeneIds, paramName));
        
        //sanity checks
        if (speciesToGeneCount.isEmpty() && undeterminedGeneIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "Some gene information to display must be provided."));
        }

        //Transform speciesToGeneCount into a Map species ID -> gene count, and add
        //the invalid gene count, associated to a specific key, and make it a LinkedHashMap,
        //for sorted and predictable responses
        LinkedHashMap<String, Long> responseSpeciesIdToGeneCount = Optional.of(speciesToGeneCount)
                .map(map -> {
                    //create a map species ID -> gene count
                    Map<String, Long> newMap = map.entrySet().stream()
                            .collect(Collectors.toMap(e -> e.getKey().getId(), e -> e.getValue()));
                    //add an entry for undetermined genes
                    if (!undeterminedGeneIds.isEmpty()) {
                        newMap.put(UNDETERMINED_SPECIES_LABEL, Long.valueOf(undeterminedGeneIds.size()));
                    }
                    return newMap;
                })
                .get().entrySet().stream()
                //sort in descending order of gene count (and in case of equality,
                //by ascending order of key, for predictable message generation)
                .sorted((e1, e2) -> {
                    if (e1.getValue().equals(e2.getValue())) {
                        return e1.getKey().compareTo(e2.getKey());
                    }
                    return e2.getValue().compareTo(e1.getValue());
                }).collect(Collectors.toMap(Entry::getKey, Entry::getValue,
                    (v1, v2) -> {throw log.throwing(new IllegalStateException("no key collision possible"));},
                    LinkedHashMap::new));

        return log.exit(new GeneListResponse(
                responseSpeciesIdToGeneCount,
                //provide a TreeMap species ID -> species
                speciesToGeneCount.keySet().stream().collect(Collectors.toMap(
                        spe -> spe.getId(), spe -> spe,
                        (v1, v2) -> {throw log.throwing(new IllegalStateException("No key collision possible"));},
                        TreeMap::new)),
                selectedSpeciesId,
                //provide a List of DevStages sorted by their natural ordering
                //(= by left bound = by temporal ordering)
                Optional.ofNullable(validStages)
                    .map(stages -> stages.stream()
                    .sorted(Comparator.naturalOrder())
                    .collect(Collectors.toList()))
                    .orElse(new ArrayList<>()),
                //SortedSet of submitted gene IDs
                geneSet,
                //SortedSet of undetermined gene IDs
                Optional.ofNullable(undeterminedGeneIds)
                    .map(TreeSet<String>::new)
                    .orElse(new TreeSet<>())));
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
            Map<Species, Long> speciesToGeneCount, Set<String> undeterminedGeneIds,
            String paramName) {
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
            msg.append(" not found");
        }
        msg.append(" in Bgee for ");
        msg.append(paramName);
        
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

    /**
     * A convenient class to be passed to {@link JsonHelper} for gene list upload responses. 
     */
    public static class GeneListResponse {
        /**
         * See {@link #getGeneCount()}.
         */
        private final LinkedHashMap<String, Long> geneCount;
        /**
         * See {@link #getDetectedSpecies()}.
         */
        private final TreeMap<String, Species> detectedSpecies;
        /**
         * See {@link #getSelectedSpecies()}.
         */
        private final String selectedSpecies;
        /**
         * See {@link #getStages()}.
         */
        private final List<DevStage> stages;
        /**
         * See {@link #getSubmittedGeneIds()}.
         */
        private final TreeSet<String> submittedGeneIds;
        /**
         * See {@link #getUndeterminedGeneIds()}.
         */
        private final TreeSet<String> undeterminedGeneIds;
        
        /**
         * Constructor of {@code GeneListResponse}. All {@code Collection}s or {@code Map}s
         * have a predictable iteration order, for predictable and consistent responses.
         * 
         * @param geneCount             A {@code LinkedHashMap} where keys are {@code String}s
         *                              corresponding to species IDs, the associated value being
         *                              a {@code Long} that is the gene count on the species.
         * @param detectedSpecies       A {@code List} of {@code Species} detected in the gene list uploaded.
         * @param selectedSpecies       A {@code String} representing the ID of the selected species.
         * @param stages                A {@code List} of {@code DevStage}s that are
         *                              valid dev. stages for {@code selectedSpecies}.
         * @param submittedGeneIds      A {@code TreeSet} of {@code String}s that are submitted gene IDs
         *                              by the user.
         * @param undeterminedGeneIds   A {@code TreeSet} of {@code String}s that are gene IDs
         *                              with undetermined species.
         */
        public GeneListResponse(LinkedHashMap<String, Long> geneCount,
                TreeMap<String, Species> detectedSpecies,
                String selectedSpecies, List<DevStage> stages, TreeSet<String> submittedGeneIds,
                TreeSet<String> undeterminedGeneIds) {
            log.entry(geneCount, detectedSpecies, selectedSpecies, stages,
                    submittedGeneIds, undeterminedGeneIds);
            this.geneCount= geneCount;
            this.detectedSpecies = detectedSpecies;
            this.selectedSpecies = selectedSpecies;
            this.stages = stages;
            this.submittedGeneIds = submittedGeneIds;
            this.undeterminedGeneIds = undeterminedGeneIds;
            log.exit();
        }
        
        /**
         * @return  The {@code Map} where keys are {@code String}s corresponding species IDs,
         *          the associated value being a {@code Long} that is the gene count on the species.
         */
        public LinkedHashMap<String, Long> getGeneCount() {
            return this.geneCount;
        }
        /**
         * @return  The {@code TreeMap} where keys are {@code String}s corresponding 
         *          to IDs of detected species, the associated value being the corresponding 
         *          {@code Species} object.
         */
        public TreeMap<String, Species> getDetectedSpecies() {
            return this.detectedSpecies;
        }
        /**
         * @return  The {@code String} representing the ID of the selected species.
         */
        public String getSelectedSpecies() {
            return this.selectedSpecies;
        }
        /**
         * @return The {@code Set} of {@code DevStage}s that are 
         *          valid dev. stages for {@code selectedSpecies}.
         */
        public List<DevStage> getStages() {
            return this.stages;
        }
        /**
         * @return  The {@code Set} of {@code String}s that are submitted gene IDs by the user.
         */
        public TreeSet<String> getSubmittedGeneIds() {
            return this.submittedGeneIds;
        }
        /**
         * @return  The {@code Set} of {@code String}s that are gene IDs with undetermined species.
         */
        public TreeSet<String> getUndeterminedGeneIds() {
            return this.undeterminedGeneIds;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((detectedSpecies == null) ? 0 : detectedSpecies.hashCode());
            result = prime * result + ((geneCount == null) ? 0 : geneCount.hashCode());
            result = prime * result + ((selectedSpecies == null) ? 0 : selectedSpecies.hashCode());
            result = prime * result + ((stages == null) ? 0 : stages.hashCode());
            result = prime * result + ((submittedGeneIds == null) ? 0 : submittedGeneIds.hashCode());
            result = prime * result + ((undeterminedGeneIds == null) ? 0 : undeterminedGeneIds.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            GeneListResponse other = (GeneListResponse) obj;
            if (detectedSpecies == null) {
                if (other.detectedSpecies != null)
                    return false;
            } else if (!detectedSpecies.equals(other.detectedSpecies))
                return false;
            if (geneCount == null) {
                if (other.geneCount != null)
                    return false;
            } else if (!geneCount.equals(other.geneCount))
                return false;
            if (selectedSpecies == null) {
                if (other.selectedSpecies != null)
                    return false;
            } else if (!selectedSpecies.equals(other.selectedSpecies))
                return false;
            if (stages == null) {
                if (other.stages != null)
                    return false;
            } else if (!stages.equals(other.stages))
                return false;
            if (submittedGeneIds == null) {
                if (other.submittedGeneIds != null)
                    return false;
            } else if (!submittedGeneIds.equals(other.submittedGeneIds))
                return false;
            if (undeterminedGeneIds == null) {
                if (other.undeterminedGeneIds != null)
                    return false;
            } else if (!undeterminedGeneIds.equals(other.undeterminedGeneIds))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "Gene count: " + getGeneCount() + " - Detected species: " + getDetectedSpecies()
                    + " - Selected species: " + getSelectedSpecies() + " - Stages: " + getStages()
                    + " - Submitted gene IDs: " + getSubmittedGeneIds()
                    + " - Undetermined gene IDs: " + getUndeterminedGeneIds();
        }
    }
}