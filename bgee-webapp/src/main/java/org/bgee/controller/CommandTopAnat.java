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
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.InvalidRequestException;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.model.BgeeEnum;
import org.bgee.model.ServiceFactory;
import org.bgee.model.TaskManager;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.DecorelationType;
import org.bgee.model.gene.Gene;
import org.bgee.model.species.Species;
import org.bgee.model.topanat.TopAnatController;
import org.bgee.model.topanat.TopAnatParams;
import org.bgee.model.topanat.TopAnatResults;
import org.bgee.model.topanat.exception.MissingParameterException;
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
    
    //TODO Remove, it's for live tests
    private volatile static int nbJobTrackingTries = 0;
    
    /**
     * An {@code int} that is the level to be used to filter retrieved dev. stages. 
     */
    private final static int DEV_STAGE_LEVEL = 2;
    
    /**
     * A {@code String} that is the label of the count of genes whose the species is undetermined. 
     */
    private final static String UNDETERMINED_SPECIES_LABEL = "UNDETERMINED";

    /**
     * A {@code String} that is the label of the job response. 
     */
    private final static String JOB_RESPONSE_LABEL = "job_response";
    
    /**
     * An {@code int} that is the number of try to create a task manager with a job ID. 
     */
    private final static int MAX_TASK_MANAGER_RETRY = 10000;
    
    /**
     * An {@code enum} defining the job status. 
     * 
     * @author  Valentine Rech de Laval
     * @version Bgee 13 Nov. 2015
     * @since   Bgee 13 Nov. 2015
     */
    private static enum JobStatus {
        UNDEFINED, RUNNING;
    }
    
    
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
    public void processRequest() throws IOException, PageNotFoundException, InvalidRequestException,
            MissingParameterException {
        log.entry();
        
        TopAnatDisplay display = this.viewFactory.getTopAnatDisplay();
        
        // Gene list validation 
        if (this.requestParameters.isATopAnatGeneUpload()) {
            
            // Get gene responses
            LinkedHashMap<String, Object> data = this.getGeneResponses();

            // Send response
            display.sendGeneListReponse(data, messages.stream().collect(Collectors.joining("\n")));

        // New job submission
        } else if (this.requestParameters.isATopAnatSubmitJob()) {
            
            this.submitNewJob(display);

        // Job tracking
        } else if (this.requestParameters.isATopAnatTrackingJob()) {
            // Get params
            Integer jobID = this.requestParameters.getJobId(); 
            String keyParam = this.requestParameters.getDataKey(); 
            
            if (jobID == null || jobID < 1) {
                throw log.throwing(new InvalidRequestException("A job ID must be provided"));
            }
            
            // Retrieve task manager associated to the provided ID
//            TaskManager taskManager = TaskManager.getTaskManager(jobID);
            JobStatus jobStatus;
            //TODO Remove, it's for live tests
            if (nbJobTrackingTries % 3 == 0) {
                jobStatus = JobStatus.UNDEFINED;
            } else {
                jobStatus = JobStatus.RUNNING;                
            }
//            if (taskManager == null || taskManager.isTerminated()) {
//                jobStatus = JobStatus.UNDEFINED;
//            } else {
//                jobStatus = JobStatus.RUNNING;
//                // TODO nice complete message
//            }
            nbJobTrackingTries++;
            LinkedHashMap<String, Object> data = new LinkedHashMap<>();

            data.put(JOB_RESPONSE_LABEL, new JobResponse(jobID, jobStatus.name(), keyParam));

            if (this.requestParameters.getGeneInfo() != null &&
                    this.requestParameters.getGeneInfo()) {
                data.putAll(this.getGeneResponses());
            }
            display.sendTrackingJobResponse(data, "Job is " + jobStatus.name());

        // Get results
        } else if (this.requestParameters.isATopAnatGetResult()) {
            
            // TODO get allTopAnatParams
            TopAnatController controller = new TopAnatController(
                    null, prop, serviceFactory);
            Stream<TopAnatResults> topAnatResults = controller.proceedToTopAnatAnalyses();

//            topAnatResults
//                .map(TopAnatResults::getRows)
//                .collect(Collectors.toSet());

            LinkedHashMap<String, Object> data = new LinkedHashMap<>();
            if (this.requestParameters.getGeneInfo() != null &&
                    this.requestParameters.getGeneInfo()) {
                data.putAll(this.getGeneResponses());
            }
            
            
            
//            data.put("results", results);
            display.sendResultResponse(data, "");

        // Home page, empty
        } else if (this.requestParameters.getAction() == null) {
            display.displayTopAnatHomePage();
            
        } else {
            throw log.throwing(new PageNotFoundException("Incorrect " 
                + this.requestParameters.getUrlParametersInstance().getParamAction() 
                + " parameter value."));
        }

        log.exit();
    }
    
    /**
     * @return
     * @throws InvalidRequestException
     */
    private LinkedHashMap<String, Object> getGeneResponses() throws InvalidRequestException {
        log.entry();

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
        return log.exit(data);
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

        // Identify gene IDs not in the selected species
        TreeSet<String> notSelectedSpeciesGenes = new TreeSet<>(
                validGenes.stream()
                    .filter(g -> !g.getSpeciesId().equals(selectedSpeciesId))
                    .map(Gene::getId)
                    .collect(Collectors.toSet()));
        
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
                //SortedSet of gene IDs with known species but not the selected species
                notSelectedSpeciesGenes,
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
     * @param display
     * @throws InvalidRequestException
     * @throws MissingParameterException
     */
    private void submitNewJob(TopAnatDisplay display) throws InvalidRequestException, MissingParameterException {
        log.entry(display);

        // Get submitted params
        // Fg gene list cannot be null
        final List<String> subFgIds = Collections.unmodifiableList(Optional.ofNullable(
                this.requestParameters.getForegroundList()).orElse(new ArrayList<>()));
        if (subFgIds.isEmpty()) {
            throw log.throwing(new InvalidRequestException(
                    "A foreground gene ID list must be provided"));
        }
    
        // Bg gene list can be null if the default species background should be used
        final List<String> subBgIds = Collections.unmodifiableList(Optional.ofNullable(
                this.requestParameters.getBackgroundList()).orElse(new ArrayList<>())); 
        boolean hasBgList = !subBgIds.isEmpty();
    
        // Data quality cannot be null
        final List<String> subCallTypes = Collections.unmodifiableList(Optional.ofNullable(
                this.requestParameters.getExprType()).orElse(new ArrayList<>()));
        if (subCallTypes.isEmpty()) {
            throw log.throwing(new InvalidRequestException("A expression type must be provided"));
        }
        Set<String> callTypes = new HashSet<>(subCallTypes);
        // TODO remove when "ALL" call type is removed from web app
        if (callTypes.contains("ALL")) {
            callTypes.remove("ALL");
            callTypes.add(CallType.Expression.EXPRESSED.name());
            callTypes.add(CallType.DiffExpression.DIFF_EXPRESSED.name());
        }
        
        // Data quality can be null if there is no filter to be applied
        final String subDataQuality = this.requestParameters.getDataQuality();
        DataQuality dataQuality = null; 
        if (subDataQuality != null) {
            if (subDataQuality.equalsIgnoreCase(DataQuality.HIGH.name())) {
                dataQuality = DataQuality.HIGH;
            } else {
                dataQuality = DataQuality.LOW;
            }
        }

        // Data types can be null if there is no filter to be applied
        final List<String> subDataTypes = Collections.unmodifiableList(Optional.ofNullable(
                this.requestParameters.getDataType()).orElse(new ArrayList<>()));
        Set<String> dataTypes = null; 
        if (!subDataTypes.isEmpty()) {
            dataTypes = new HashSet<>(subDataTypes);
        }
    
        // Dev. stages can be null if all selected species stages should be used
        final List<String> subDevStages = Collections.unmodifiableList(Optional.ofNullable(
                this.requestParameters.getDevStage()).orElse(new ArrayList<>()));
        Set<String> devStageIds = null; 
        if (!subDevStages.isEmpty()) {
            devStageIds = new HashSet<>(subDevStages);
        }

        // Decorrelation type can be null if all selected species stages should be used
        final String subDecorrType = this.requestParameters.getDecorrelationType();
        if (StringUtils.isBlank(subDecorrType)) {
            throw log.throwing(new InvalidRequestException("A decorrelation type must be provided"));
        }
        
        final Integer subNodeSize = this.requestParameters.getNodeSize(); 
        if (subNodeSize != null && subNodeSize <= 0) {
            throw log.throwing(new InvalidRequestException("A node size must be positive"));
        }
        
        final Integer subNbNodes = this.requestParameters.getNbNode(); 
        if (subNbNodes != null && subNbNodes <= 0) {
            throw log.throwing(new InvalidRequestException("A number of nodes must be positive"));
        }
        
        final Double subFdrThr = this.requestParameters.getFdrThreshold(); 
        if (subFdrThr != null && subFdrThr <= 0) {
            throw log.throwing(new InvalidRequestException("A FDR threshold must be positive"));
        }
        
        final Double subPValueThr = this.requestParameters.getPValueThreshold();
        if (subPValueThr != null && subPValueThr <= 0) {
            throw log.throwing(new InvalidRequestException("A p-value threshold must be positive"));
        }
        
        Set<String> cleanFgIds = new HashSet<>(subFgIds);
        Set<String> cleanBgIds = null;
        String speciesId = null;
        // If a bg list is provided, we do a gene validation on it and clean both lists
        if (hasBgList) {
            GeneListResponse bgGeneResponse = this.getGeneResponse(subBgIds, null);
            speciesId = bgGeneResponse.getSelectedSpecies();
            devStageIds = this.cleanDevStages(bgGeneResponse, devStageIds);
            cleanBgIds = new HashSet<>(subBgIds);
            // Remove in fg gene IDs that are not in bg list
            cleanFgIds.retainAll(subBgIds);
            if (cleanFgIds.isEmpty()) {
                throw log.throwing(new InvalidRequestException("No gene IDs of foreground "
                        + "are in background gene ID list"));
            }
            cleanFgIds = this.cleanGeneIds(bgGeneResponse, cleanFgIds);
            cleanBgIds = this.cleanGeneIds(bgGeneResponse, cleanBgIds);
        }
        
        // Get gene response for clean fg gene IDs
        GeneListResponse fgGeneResponse = this.getGeneResponse(new ArrayList<>(cleanFgIds), null);
        
        // If a bg list is NOT provided, we clean fg list and get data according to fgGeneResponse
        if (!hasBgList) {
            cleanFgIds = this.cleanGeneIds(fgGeneResponse, cleanFgIds);
            devStageIds = this.cleanDevStages(fgGeneResponse, devStageIds);
            speciesId = fgGeneResponse.getSelectedSpecies();
        }

        assert cleanFgIds != null && !cleanFgIds.isEmpty();
        assert devStageIds != null && !devStageIds.isEmpty();
        assert StringUtils.isNotBlank(speciesId);
        assert callTypes == null || callTypes.isEmpty();

        // One TopAnat analyze has one call type and one dev. stage
        List<TopAnatParams> allTopAnatParams = new ArrayList<TopAnatParams>();
        for (String callType: callTypes) {
            if (callType.isEmpty()) {
                continue;
            }
            for (String devStageId: devStageIds) {
                log.debug("Iteration: callType={} - devStageId={}", callType, devStageId);
                if (StringUtils.isBlank(devStageId)) {
                    continue;
                }
                CallType callTypeEnum = null;
                
                if (BgeeEnum.isInEnum(CallType.Expression.class, callType)) {
                    callTypeEnum = CallType.Expression.convertToExpression(callType);
                } else if (BgeeEnum.isInEnum(CallType.DiffExpression.class, callType)) {
                    callTypeEnum = CallType.DiffExpression.convertToDiffExpression(callType);
                } else {
                    throw log.throwing(new InvalidRequestException("Unkown call type: " + callType));
                }

                TopAnatParams.Builder builder = new TopAnatParams.Builder(
                        cleanFgIds, cleanBgIds, speciesId, callTypeEnum);
                builder.dataQuality(dataQuality);
                if (dataTypes == null || BgeeEnum.areAllInEnum(DataType.class, dataTypes)) {
                    builder.dataTypes(DataType.convertToDataTypeSet(dataTypes));
                } else {
                    throw log.throwing(new InvalidRequestException("Error in data types: " + 
                            subDecorrType));
                }
                builder.devStageId(devStageId);
                if (BgeeEnum.isInEnum(DecorelationType.class, subDecorrType)) {
                    builder.decorelationType(DecorelationType.convertToDecorelationType(subDecorrType));
                } else {
                    throw log.throwing(new InvalidRequestException("Unkown decorrelation type: " + 
                            subDecorrType));
                }
                builder.nodeSize(subNodeSize);
                builder.numberOfSignificantNode(subNbNodes);
                builder.fdrThreshold(subFdrThr);
                builder.pvalueThreshold(subPValueThr);
                allTopAnatParams.add(builder.build());
            }
        }

        // Create the ID to track job creating a random int
        Integer jobTrackingId = null;
        boolean hasCreateTaskManager = false;
        for (int i = 0; i < MAX_TASK_MANAGER_RETRY ; i++) {
            try {
                // We want only positive job ID
                jobTrackingId = Math.abs(ThreadLocalRandom.current().nextInt());
                TaskManager.registerTaskManager(jobTrackingId);
            } catch (IllegalArgumentException | IllegalStateException e) {
                continue;
            }
            hasCreateTaskManager = true;
            break;
        }
        
        if (!hasCreateTaskManager) {
            throw log.throwing(new RuntimeException("Failed to get task manager after " +
                    MAX_TASK_MANAGER_RETRY + " tries"));
        }
        log.trace("Job ID defined: {}", jobTrackingId);
        
        TopAnatController controller = new TopAnatController(allTopAnatParams, prop, serviceFactory);
        
        // Job ID if available, add hash
    
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put(JOB_RESPONSE_LABEL, new JobResponse(
                jobTrackingId, JobStatus.RUNNING.name(), this.requestParameters.getDataKey()));

        display.sendTrackingJobResponse(data, "Job is " + JobStatus.RUNNING.name());

        // Launch the TopAnat analysis
        controller.proceedToTopAnatAnalyses();
    }

    /**
     * @param geneResponse
     * @param geneIds
     * @return
     */
    private Set<String> cleanGeneIds(GeneListResponse geneResponse, Set<String> geneIds) {
        log.entry(geneResponse, geneIds);
        
        Set<String> cleanGeneIds = new HashSet<>(geneIds);
        
        // Remove gene IDs that are not in bg selected species.
        cleanGeneIds.removeAll(geneResponse.getNotInSelectedSpeciesGeneIds());
        // Remove gene IDs that are in bg undetermined gene IDs
        cleanGeneIds.removeAll(geneResponse.getUndeterminedGeneIds());
        
        if (cleanGeneIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No gene IDs of foreground "
                    + "are in selected species from background gene ID list"));
        }
        
        return log.exit(cleanGeneIds);
    }

    /**
     * @param geneResponse
     * @param devStageIds
     * @return
     */
    private Set<String> cleanDevStages(GeneListResponse geneResponse, Set<String> devStageIds) {
        log.entry(geneResponse, devStageIds);

        Set<String> allDevStageIds = geneResponse.getStages().stream()
                .map(DevStage::getId)
                .collect(Collectors.toSet()); 
        if (devStageIds == null) {
            // We need stages to be able to build all TopAnatParams
            return log.exit(allDevStageIds);
        }
        Set<String> cleanDevStageIds = new HashSet<>(devStageIds);
        if (!allDevStageIds.containsAll(cleanDevStageIds)) {
            throw log.throwing(new IllegalArgumentException("Provided developmental stages " +
                    "are not from selected species"));
        }
        
        return log.exit(cleanDevStageIds);
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
         * See {@link #getNotInSelectedSpeciesGeneIds()}.
         */
        private final TreeSet<String> notInSelectedSpeciesGeneIds;
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
         * @param notInSelectedSpeciesGeneIds      A {@code TreeSet} of {@code String}s that are 
         *                              submitted gene IDs that are not in the selected species.
         * @param undeterminedGeneIds   A {@code TreeSet} of {@code String}s that are gene IDs
         *                              with undetermined species.
         */
        public GeneListResponse(LinkedHashMap<String, Long> geneCount,
                TreeMap<String, Species> detectedSpecies,
                String selectedSpecies, List<DevStage> stages, TreeSet<String> notInSelectedSpeciesGeneIds,
                TreeSet<String> undeterminedGeneIds) {
            log.entry(geneCount, detectedSpecies, selectedSpecies, stages,
                    notInSelectedSpeciesGeneIds, undeterminedGeneIds);
            this.geneCount= geneCount;
            this.detectedSpecies = detectedSpecies;
            this.selectedSpecies = selectedSpecies;
            this.stages = stages;
            this.notInSelectedSpeciesGeneIds = notInSelectedSpeciesGeneIds;
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
         * @return  The {@code TreeSet} of {@code String}s that are 
         *          submitted gene IDs that are not in the selected species.
         */
        public TreeSet<String> getNotInSelectedSpeciesGeneIds() {
            return this.notInSelectedSpeciesGeneIds;
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
            result = prime * result + ((notInSelectedSpeciesGeneIds == null) ? 0 : 
                notInSelectedSpeciesGeneIds.hashCode());
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
            if (notInSelectedSpeciesGeneIds == null) {
                if (other.notInSelectedSpeciesGeneIds != null)
                    return false;
            } else if (!notInSelectedSpeciesGeneIds.equals(other.notInSelectedSpeciesGeneIds))
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
                    + " - Gene IDs not in selected: " + getNotInSelectedSpeciesGeneIds()
                    + " - Undetermined gene IDs: " + getUndeterminedGeneIds();
        }
    }

    /**
     * A convenient class to be passed to {@link JsonHelper} for job responses. 
     */
    public static class JobResponse {
        /**
         * See {@link #getJobId()}.
         */
        private final Integer jobId;
        /**
         * See {@link #getJobStatus()}.
         */
        private final String jobStatus;
        /**
         * See {@link #getData()}.
         */
        private final String data;
        
        /**
         * Constructor of {@code JobResponse}.
         * 
         * @param jobId     An {@code Integer} representing the ID of the job (task).
         * @param jobStatus A {@code String} representing the status of the job.
         * @param data      A {@code String} representing the key of the parameters.
         */
        public JobResponse(Integer jobId, String jobStatus, String data) {
            log.entry(jobId, jobStatus, data);
            this.jobId = jobId;
            this.jobStatus = jobStatus;
            this.data = data;
            log.exit();
        }
        
        /**
         * @return  The {@code Integer} representing the ID of the job (task).
         */
        public Integer getJobId() {
            return this.jobId;
        }
        /**
         * @return  The {@code String} representing the status of the job.
         */
        public String getJobStatus() {
            return this.jobStatus;
        }
        /**
         * @return  The {@code String} representing the key of the parameters.
         */
        public String getData() {
            return this.data;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((jobId == null) ? 0 : jobId.hashCode());
            result = prime * result + ((jobStatus == null) ? 0 : jobStatus.hashCode());
            result = prime * result + ((data == null) ? 0 : data.hashCode());
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
            JobResponse other = (JobResponse) obj;
            if (jobId == null) {
                if (other.jobId != null)
                    return false;
            } else if (!jobId.equals(other.jobId))
                return false;
            if (jobStatus == null) {
                if (other.jobStatus != null)
                    return false;
            } else if (!jobStatus.equals(other.jobStatus))
                return false;
            if (data == null) {
                if (other.data != null)
                    return false;
            } else if (!data.equals(other.data))
                return false;
            return true;
        }
        @Override
        public String toString() {
            return "Job ID: " + getJobId() + " - Job status: "
                    + getJobStatus() + " - Data: " + getData();
        }
    }
}