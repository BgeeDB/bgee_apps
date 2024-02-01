package org.bgee.controller;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.InvalidRequestException;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.controller.user.User;
import org.bgee.model.BgeeEnum;
import org.bgee.model.BgeeEnum.BgeeEnumField;
import org.bgee.model.ManageReadWriteLocks;
import org.bgee.model.NamedEntity;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.expressiondata.call.Call.ExpressionCall;
import org.bgee.model.expressiondata.call.CallFilter;
import org.bgee.model.expressiondata.call.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.call.CallService;
import org.bgee.model.expressiondata.call.Condition;
import org.bgee.model.expressiondata.call.ConditionFilter;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.job.Job;
import org.bgee.model.job.JobService;
import org.bgee.model.job.exception.ThreadAlreadyWorkingException;
import org.bgee.model.job.exception.TooManyJobsException;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.ontology.OntologyElement;
import org.bgee.model.ontology.OntologyService;
import org.bgee.model.ontology.RelationType;
import org.bgee.model.species.Species;
import org.bgee.model.species.SpeciesService;
import org.bgee.model.topanat.TopAnatUtils;
import org.bgee.view.RPackageDisplay;
import org.bgee.view.ViewFactory;

/**
 * Controller that handles requests necessary for the use of the BgeeDB Bioconductor R package.
 * This controller is needed as, starting from Bgee 14 and the new method for computing call qualities,
 * it is simpler for generating responses to R package queries to use {@code Service} calls rather than
 * direct {@code DAO} calls (handled in {@link CommandDAO}).
 * 
 * @author  Frederic Bastian
 * @version Bgee 15.0, Apr. 2021
 * @see https://www.bioconductor.org/packages/BgeeDB/
 * @since   Bgee 14 Mar. 2017
 */
public class CommandRPackage extends CommandParent {
    private final static Logger log = LogManager.getLogger(CommandRPackage.class.getName());

    //XXX move them to RequestParameters ????
    //XXX: yes, why not? Some already exist I guess

    public final static String AE_ID_PARAM = "ID";
    public final static String AE_NAME_PARAM = "NAME";
    public final static String AE_DESCRIPTION_PARAM = "DESCRIPTION";

    public final static String CALLS_GENE_ID_PARAM = "GENE_ID";
    public final static String CALLS_ANAT_ENTITY_ID_PARAM = "ANAT_ENTITY_ID";
    public final static String CALLS_DEV_STAGE_PARAM = "DEV_STAGE_ID";
    public final static String CALLS_DATA_QUALITY_PARAM = "DATA_QUALITY_ID";

    public final static String RELATIONS_SOURCE_PARAM = "SOURCE_ID";
    public final static String RELATIONS_TARGET_PARAM = "TARGET_ID";
    public final static String RELATION_TYPE_PARAM = "RELATION_TYPE";
    public final static String RELATION_STATUS_PARAM = "RELATION_STATUS";

    public final static String SPECIES_ID_PARAM = "ID";
    public final static String SPECIES_GENUS_PARAM = "GENUS";
    public final static String SPECIES_NAME_PARAM = "SPECIES_NAME";
    public final static String SPECIES_COMMON_NAME_PARAM = "COMMON_NAME";
    public final static String SPECIES_AFFYMETRIX_PARAM = "AFFYMETRIX";
    public final static String SPECIES_EST_PARAM = "EST";
    public final static String SPECIES_IN_SITU_PARAM = "IN_SITU";
    public final static String SPECIES_RNA_SEQ_PARAM = "RNA_SEQ";
    public final static String SPECIES_FULL_LENGTH_PARAM = "SC_RNA_SEQ";

    public final static String PROPAGATION_ID_PARAM = "ID";
    public final static String PROPAGATION_NAME_PARAM = "NAME";
    public final static String PROPAGATION_DESCRIPTION_PARAM = "DESCRIPTION";
    public final static String PROPAGATION_LEVEL_PARAM = "LEVEL";
    public final static String PROPAGATION_LEFTBOUND_PARAM = "LEFT_BOUND";
    public final static String PROPAGATION_RIGHTBOUND_PARAM = "RIGHT_BOUND";

    private final ManageReadWriteLocks manageReadWriteLocks;

    public enum PropagationParam implements BgeeEnumField {
        DESCENDANTS("descendants", true, false), ANCESTORS("ancestors", false, true),
        LEAST_COMMON_ANCESTOR("least_common_ancestor", false, true);

        private final String representation;
        private final boolean requireDescendants;
        private final boolean requireAncestors;

        private PropagationParam(String representation,
                boolean requireDescendants, boolean requireAncestors) {
            this.representation = representation;
            this.requireDescendants = requireDescendants;
            this.requireAncestors = requireAncestors;
        }

        public boolean isRequireDescendants() {
            return this.requireDescendants;
        }
        public boolean isRequireAncestors() {
            return this.requireAncestors;
        }

        @Override
        public String getStringRepresentation() {
            return this.representation;
        }
        public static PropagationParam convertToPropagationParam(String representation) {
            log.traceEntry("{}", representation);
            return log.traceExit(BgeeEnum.convert(PropagationParam.class, representation));
        }
    }


          /**
     * Constructor
     * 
     * @param response          A {@code HttpServletResponse} that will be used to display the 
     *                          page to the client
     * @param requestParameters The {@code RequestParameters} that handles the parameters of the 
     *                          current request.
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     * @param viewFactory       A {@code ViewFactory} that provides the display type to be used.
     * @param serviceFactory    A {@code ServiceFactory} that provides bgee services.
     * @param jobService        A {@code JobService} instance allowing to manage jobs between threads 
     *                          across the entire webapp.
     * @param user              The {@code User} who is making the query to the webapp.
     */
    public CommandRPackage(HttpServletResponse response, RequestParameters requestParameters, 
            BgeeProperties prop, ViewFactory viewFactory, ServiceFactory serviceFactory, 
            JobService jobService, User user) {
        super(response, requestParameters, prop, viewFactory, serviceFactory, jobService, user, null, null);
        this.manageReadWriteLocks = new ManageReadWriteLocks();
    }

    @Override
    public void processRequest() throws IllegalStateException, IOException, 
        PageNotFoundException, InvalidRequestException, ThreadAlreadyWorkingException, TooManyJobsException {
        log.traceEntry();
        

        Job job = this.jobService.registerNewJob(this.user.getUUID().toString());
        try {
            if ("get_expression_calls".equals(
                    this.requestParameters.getAction())) {

                this.processGetExpressionCalls();

            } else if ("get_anat_entities".equals(
                    this.requestParameters.getAction())) {

                this.processGetAnatEntities();
            } else if ("get_anat_entity_relations".equals(
                    this.requestParameters.getAction())) {

                this.processGetAnatEntityRelations();
            } else if ("get_all_species".equals(
                    this.requestParameters.getAction())) {

                this.processGetAllSpecies();
            } else if ("get_propagation_anat_entity".equals(
                    this.requestParameters.getAction())) {
                this.processPropagation(CallService.Attribute.ANAT_ENTITY_ID);
            } else if ("get_propagation_dev_stage".equals(
                    this.requestParameters.getAction())) {
                this.processPropagation(CallService.Attribute.DEV_STAGE_ID);
            } else {
                throw log.throwing(new PageNotFoundException("Incorrect " + 
                        this.requestParameters.getUrlParametersInstance().getParamAction() + 
                        " parameter value."));
            }
        } finally {
            //we don't care whether the job is successful or not, we just want 
            //to keep track of number of running jobs per user
            job.release();
        }
        
        log.traceExit();
    }
    
    /**
     * Performs the query and display the results when requesting {@code ExpressionCall}s.
     *
     * @throws InvalidRequestException  In case of invalid request parameter.
     * @throws IOException              In case of issue when writing results.
     */
    private void processGetExpressionCalls() throws InvalidRequestException, IOException {
        log.traceEntry();

        RPackageDisplay display = this.viewFactory.getRPackageDisplay();

        //****************************************
        // Retrieve and filter request parameters
        //****************************************
        //Data types and quality
        final Set<DataType> dataTypes = this.checkAndGetDataTypes();
        //XXX: why isn't the quality parameter used?
//        final SummaryQuality dataQuality = this.checkAndGetSummaryQuality();

        //parameters not needing processing
        final List<Integer> speciesIds = this.requestParameters.getSpeciesList();
        final List<String> stageIds   = this.requestParameters.getDevStage();
        List<String> requestedAttrs = this.requestParameters.getValues(
                this.requestParameters.getUrlParametersInstance().getParamAttributeList());
        //XXX: why not using 'getAttributes' method?
//        final List<CallService.Attribute> attrs = getAttributes(this.requestParameters, 
//                CallService.Attribute.class);

        //for now, we force to select one and only one species, to not return 
        //the complete expression table at once
        if (speciesIds == null || speciesIds.size() != 1 ||
                speciesIds.stream().anyMatch(id -> id == null || id <= 0)) {
            throw log.throwing(new InvalidRequestException("One and only one species ID must be provided"));
        }
        if (requestedAttrs == null){
            requestedAttrs = new ArrayList<>();
        }
        if(requestedAttrs.isEmpty()) {
            requestedAttrs.add(CALLS_GENE_ID_PARAM);
            requestedAttrs.add(CALLS_ANAT_ENTITY_ID_PARAM);
            requestedAttrs.add(CALLS_DATA_QUALITY_PARAM);
            requestedAttrs.add(CALLS_DEV_STAGE_PARAM);
        }
        final Set<CallService.Attribute> attrs = convertRqAttrsToCallsAttrs(requestedAttrs);
        int speciesId = speciesIds.iterator().next();
        
        //****************************************
        // Create Call filter objects
        //****************************************
        //CallDAOFilter: for now, we only allow to define one CallDAOFilter object.

        //TODO: we need to make sure the logic is identical in TopAnatParams
        Collection<ConditionFilter> conditionFilter = stageIds == null || stageIds.isEmpty()? 
                null: Collections.singleton(new ConditionFilter(null, stageIds, null, null, null));
        GeneFilter geneFilter = new GeneFilter(speciesId, this.requestParameters.getBackgroundList());
        Map<ExpressionSummary, SummaryQuality> summaryCallTypeQualityFilter = new HashMap<>();
        summaryCallTypeQualityFilter.put(ExpressionSummary.EXPRESSED, this.checkAndGetSummaryQuality());
        // retrieve calls for 
        Map<EnumSet<CallService.Attribute>, Boolean> observedDataFilter = new HashMap<>();
        observedDataFilter.put(EnumSet.of(CallService.Attribute.ANAT_ENTITY_ID), true);
        ExpressionCallFilter callFilter = new ExpressionCallFilter(summaryCallTypeQualityFilter,
                Collections.singleton(geneFilter),
                conditionFilter, dataTypes,
                //for now, we always include substages when stages requested, and never include substructures
                observedDataFilter);

        SummaryCallType callType = SummaryCallType.ExpressionSummary.EXPRESSED;

        //****************************************
        // Perform query and display results
        //****************************************
        // For some species topAnat was taking too much time to retrieve calls then resulting in an
        // apache timeout. To avoid that we provide an hardcoded list of species and stages for which
        // we retrieve calls from files on our server and not anymore generate calls files on the fly.
        // TODO: remove all hardcoded parts of that logic once topAnat has been optimized.
        // TODO: refactor topAnat codes from R and web and then implement the logic below for all species/stages
        Set<Integer> speciesRetrievedFromFile = new HashSet<Integer>(Arrays.asList(7227, 9823, 9913, 9606, 10090));
        Set<String> stagesRetrievedFromFile = new HashSet<String>(Arrays.asList(ConditionDAO.DEV_STAGE_ROOT_ID,
                "UBERON:0000066", "UBERON:0000068", "UBERON:0000092"));
        
        Stream<ExpressionCall> callStream;
        
        // for specified species we retrieve gene to anatEntity association from a file.
        // In topAnat R a user can retrieve calls for several stageIDs. Each combination of stageIds will result
        // in a different file. In order not to create too many files for now, we decided to use a generated file
        // only for a subset of stages AND only if one single stage is queried. It should cover a high proportion
        // of analysis on the selected species as stageIds are not often specified per the users.
        
        if (speciesRetrievedFromFile.contains(speciesId) &&
                (stageIds == null || stageIds.size() == 1 && stagesRetrievedFromFile.contains(stageIds.get(0))) &&
                (dataTypes.containsAll(Set.of(DataType.values())) || dataTypes.size() == 1 && 
                    dataTypes.contains(DataType.RNA_SEQ) || dataTypes.size() == 2 &&
                    dataTypes.containsAll(Set.of(DataType.RNA_SEQ, DataType.SC_RNA_SEQ)))
                ) {
            
            Path finalGeneToAnatEntitiesFile = Paths.get(this.prop.getTopAnatResultsWritingDirectory(), 
                    CommandRPackage.getGeneToAnatEntitiesFileName(false, speciesId,
                            callType, stageIds == null || stageIds.isEmpty()? null : stageIds.get(0),
                                    dataTypes, this.checkAndGetSummaryQuality()));
            Path tmpFile = Paths.get(this.prop.getTopAnatResultsWritingDirectory(), 
                    CommandRPackage.getGeneToAnatEntitiesFileName(true, speciesId,
                            callType, stageIds == null || stageIds.isEmpty()? null : stageIds.get(0),
                                    dataTypes, this.checkAndGetSummaryQuality()));//, 
            try {
                this.manageReadWriteLocks.acquireWriteLock(finalGeneToAnatEntitiesFile.toString());
                this.manageReadWriteLocks.acquireWriteLock(tmpFile.toString());

                //check, AFTER having acquired the locks, that the final file does not 
                //already exist (maybe another thread generated the files before this one 
                //acquired the lock)
                if (!Files.exists(finalGeneToAnatEntitiesFile)) {
                    log.info("Gene to AnatEntities association file not already generated.");

                    this.writeToGeneToAnatEntitiesFile(tmpFile.toString(), this.serviceFactory.getCallService(),
                            callFilter, attrs);
                    //move tmp file if successful
                    TopAnatUtils.move(tmpFile, finalGeneToAnatEntitiesFile, false);
                }
            } finally {
                Files.deleteIfExists(tmpFile);
                this.manageReadWriteLocks.releaseWriteLock(finalGeneToAnatEntitiesFile.toString());
                this.manageReadWriteLocks.releaseWriteLock(tmpFile.toString());
            }
            // Now that the file is created we can read it
            try {
                this.manageReadWriteLocks.acquireReadLock(finalGeneToAnatEntitiesFile.toString());
                callStream = Files.lines(finalGeneToAnatEntitiesFile)
                        //do not consider header of files manually generated using SQL queries.
                        //Could have been removed from the files but keep this filter as a sanity check
                        //in case other files have to be generated and we forget to remove the header
                        .filter(line -> ! line.equals("GENE_ID\tANAT_ENTITY_ID"))
                        .map(l -> {
                            String[] lineValues = l.split("\t");
                            // create fake expressionCalls objects to be able to retrieve data from the file
                            // once topAnat has been updated we will directly provide a Stream<String> to the
                            // view
                            return new ExpressionCall(new Gene(lineValues[0], new Species(speciesId),
                                    new GeneBioType("fake")), new Condition(new AnatEntity(lineValues[1]),
                                    null, null, null, null, null),null, null, null, null, null, null, null, null);
                        });

            } finally {
                this.manageReadWriteLocks.releaseReadLock(finalGeneToAnatEntitiesFile.toString());
            }
            
        } else {
             callStream = this.serviceFactory.getCallService().loadExpressionCalls(
                    callFilter,
                    //Attributes requested; no ordering requested
                    attrs, null);
        }
        display.displayCalls(requestedAttrs, callStream);
        
        log.traceExit();
    }

    //TODO: this function reproduce the logic of TopAnatAnalysis.getGeneToAnatEntitiesFileName(boolean, TopAnatParameter)
    // Code refactoring has to be done while optimazing topAnat
    // As topAnat R does not yet use celltypes we can not reuse files created for topAnat web. That is why we add a
    // "_R" suffix at the end of the file name. It is ugly but it will be fixed for Bgee 15.2. All modifications to
    // implement for Bgee 15.2 are listed in the Jira issue BA-744
    private static String getGeneToAnatEntitiesFileName(boolean tmpFile, int speciesId, SummaryCallType callType,
            String devStageId, Set<DataType> dataTypes, SummaryQuality summaryQuality){
        log.traceEntry("{}, {}, {}, {}, {}, {}", tmpFile, speciesId, callType, devStageId, dataTypes, summaryQuality);
        
        String paramsEncoded = "";
        //TODO: use some kind of encoding of the Strings for file name (see replacement for stage ID)
        final StringBuilder sb = new StringBuilder();
        sb.append(speciesId);
        sb.append("_").append(callType.toString());
        Optional.ofNullable(devStageId)
            //replace column in IDs
            .ifPresent(e -> sb.append("_").append(e.replace(":", "_")));
        //use EnumSet for consistent ordering
        Optional.ofNullable(dataTypes).map(e -> EnumSet.copyOf(e))
        .orElse(EnumSet.allOf(DataType.class))
        .stream()
        .forEach(e -> sb.append("_").append(e.toString()));
        sb.append("_").append(Optional.ofNullable(summaryQuality).orElse(SummaryQuality.SILVER)
                .toString());
        //on topAnat web a last parameter corresponding to the decorrelation type is used to generate
        //the name of the file. As topAnat R does not allow to choose the decorrelation type we always
        //add _NONE add the end of the filename
        sb.append("_NONE");
        paramsEncoded = sb.toString();
        String fileName = TopAnatUtils.FILE_PREFIX + "GeneToAnatEntities_" 
        //TODO: Do not forget to remove the "_R" suffix once updates for Bgee 15.2 are implemented
            + paramsEncoded + "_R" + ".tsv";
        if (tmpFile) {
            fileName += TopAnatUtils.TMP_FILE_SUFFIX;
        }
        return log.traceExit(fileName);
    }
    //TODO: this function partially reproduce the logic of TopAnatAnalysis.writeToGeneToAnatEntitiesFile(String)
    // Code refactoring has to be done for Bgee 15.2
    private void writeToGeneToAnatEntitiesFile(String geneToAnatEntitiesFile, CallService callService,
            CallFilter<?, ?, ConditionFilter> callFilter, Set<CallService.Attribute> callServiceAttributes)
            throws IOException {
        log.traceEntry("{}", geneToAnatEntitiesFile);

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
                geneToAnatEntitiesFile)))) {
            callService.loadExpressionCalls(
                    (ExpressionCallFilter) callFilter,
                    callServiceAttributes,
                    null
                ).forEach(
                    call -> out.println(
                        call.getGene().getGeneId() + '\t' + call.getCondition().getAnatEntityId())
                );
        }
        log.traceExit();
    }
    
    /**
     * Performs the query and display the results when requesting {@code AnatEntityTO}s.
     * 
     * @throws InvalidRequestException  In case of invalid request parameter.
     * @throws IOException              In case of issue when writing results. 
     */
    //TODO: once topAnat has been refactored then generate/read those files from our server
    private void processGetAnatEntities() throws InvalidRequestException, IOException {
        log.traceEntry();
        
        AnatEntityService aeService = this.serviceFactory.getAnatEntityService();
        RPackageDisplay display = this.viewFactory.getRPackageDisplay();

        //****************************************
        // Retrieve and filter request parameters
        //****************************************
        final List<Integer> speciesIds = this.requestParameters.getSpeciesList();
        //XXX: why not using 'getAttributes' method?
        List<String> requestedAttrs = this.requestParameters.getValues(
                this.requestParameters.getUrlParametersInstance().getParamAttributeList());
        
        //for now, we force to select one and only one species, to not return 
        //the complete expression table at once
        if (speciesIds == null || speciesIds.size() != 1) {
            throw log.throwing(new InvalidRequestException("One and only one species ID must be provided"));
        }
        //if null or empty add all parameter values
        if (requestedAttrs == null){
            requestedAttrs = new ArrayList<>();
        }
        if(requestedAttrs.isEmpty()) {
            requestedAttrs.add(AE_ID_PARAM);
            requestedAttrs.add(AE_NAME_PARAM);
            requestedAttrs.add(AE_DESCRIPTION_PARAM);
        }
        final Set<AnatEntityService.Attribute> attrs = convertRqAttrsToAEAttrs(requestedAttrs);
        //****************************************
        // Perform query and display results
        //****************************************
        Stream<AnatEntity> ae = aeService.loadAnatEntities(speciesIds, true, null, attrs);
//        AnatEntityTOResultSet rs = daoManager.getAnatEntityDAO().getAnatEntities(
//                speciesIds, true, null, attrs);
        
        display.displayAnatEntities(requestedAttrs, ae);
        
        log.traceExit();
    }
    
    /**
     * Performs the query and display the results when requesting {@code RelationTO}s 
     * for anatomical entities.
     * 
     * @throws InvalidRequestException  In case of invalid request parameter.
     * @throws IOException              In case of issue when writing results. 
     */
    //TODO: test
    //TODO: once topAnat has been refactored then generate/read those files from our server
    private void processGetAnatEntityRelations() throws InvalidRequestException, IOException {
        log.traceEntry();
        RPackageDisplay display = this.viewFactory.getRPackageDisplay();
        OntologyService ontologyService = this.serviceFactory.getOntologyService();

        //****************************************
        // Retrieve and filter request parameters
        //****************************************
        final List<Integer> speciesIds = this.requestParameters.getSpeciesList();

        //XXX: why not using 'getAttributes' method?
        List<String> requestedAttrs = this.requestParameters.getValues(
                this.requestParameters.getUrlParametersInstance().getParamAttributeList());
        if (requestedAttrs == null){
            requestedAttrs = new ArrayList<>();
        }
        if(requestedAttrs.isEmpty()){
            requestedAttrs.add(RELATIONS_SOURCE_PARAM);
            requestedAttrs.add(RELATIONS_TARGET_PARAM);
            requestedAttrs.add(RELATION_TYPE_PARAM);
            requestedAttrs.add(RELATION_STATUS_PARAM);
        }
        
        //check value of parameters
        checkRelationAttrs(requestedAttrs);
        
        //for now, we force to select one and only one species, to not return 
        //the complete expression table at once
        if (speciesIds == null || speciesIds.size() != 1) {
            throw log.throwing(new InvalidRequestException("One and only one species ID must be provided"));
        }
        Integer speciesId = speciesIds.iterator().next();
        //****************************************
        // Perform query and display results
        //****************************************
        Ontology<AnatEntity, String> anatEntityOnt = ontologyService.getAnatEntityOntology(speciesIds, null)
                .getAsSingleSpeciesOntology(speciesId);
        display.displayAERelations(requestedAttrs, anatEntityOnt);
        
        log.traceExit();
    }
    
    /**
     * Performs the query and display the results when requesting {@code SpeciesTO}s.
     * 
     * @throws InvalidRequestException  In case of invalid request parameter.
     * @throws IOException              In case of issue when writing results. 
     */
    private void processGetAllSpecies() throws IOException {
        log.traceEntry();
        
        SpeciesService spService = this.serviceFactory.getSpeciesService();
        RPackageDisplay display = this.viewFactory.getRPackageDisplay();

        //****************************************
        // Retrieve and filter request parameters
        //****************************************
        //XXX: why not using 'getAttributes' method?
//        final List<SpeciesService.Attribute> attrs = getAttributes(this.requestParameters, 
//                SpeciesService.Attribute.class);
        List<String> requestedAttrs = this.requestParameters.getValues(
                this.requestParameters.getUrlParametersInstance().getParamAttributeList());
        if(requestedAttrs == null){
            requestedAttrs = new ArrayList<>();
        }
        if(requestedAttrs.isEmpty()){
            requestedAttrs.add(SPECIES_ID_PARAM);
            requestedAttrs.add(SPECIES_GENUS_PARAM);
            requestedAttrs.add(SPECIES_NAME_PARAM);
            requestedAttrs.add(SPECIES_COMMON_NAME_PARAM);
            requestedAttrs.add(DataType.AFFYMETRIX.toString());
            requestedAttrs.add(DataType.EST.toString());
            requestedAttrs.add(DataType.IN_SITU.toString());
            requestedAttrs.add(DataType.RNA_SEQ.toString());
            requestedAttrs.add(DataType.SC_RNA_SEQ.toString());
        }
        checkSpeciesAttrs(requestedAttrs);
        //****************************************
        // Perform query and display results
        //****************************************
        //we don't need datasource information
        List <Species> species = spService.loadSpeciesByIds(null, true)
                .stream().sorted((s1, s2) -> Integer.compare(
                        s1.getId(), s2.getId()))
                .collect(Collectors.toList());
        
        display.displaySpecies(requestedAttrs, species);
        
        log.traceExit();
    }
    
    private void processPropagation(CallService.Attribute condParam) throws IOException,
    InvalidRequestException {
        log.traceEntry("{}", condParam);

        //****************************************
        // Retrieve and filter request parameters
        //****************************************

        final Integer speciesId = this.requestParameters.getSpeciesId();
        if(speciesId == null) {
            throw log.throwing(new InvalidRequestException("one species ID must be provided"));
        }

        //Retrieve requested entities
        List<String> entityIds = null;
        if (CallService.Attribute.ANAT_ENTITY_ID.equals(condParam)) {
            entityIds = this.requestParameters.getAnatEntity();
        } else if (CallService.Attribute.DEV_STAGE_ID.equals(condParam)) {
            entityIds = this.requestParameters.getDevStage();
        } else {
            throw log.throwing(new InvalidRequestException(condParam + "is not a valid "
                    + "condition parameter"));
        }
        if (entityIds == null || entityIds.isEmpty()) {
            throw log.throwing(new InvalidRequestException("At least one ID must be provided"));
        }

        //Retrieve requested propagation
        PropagationParam propagation = PropagationParam.convertToPropagationParam(
                this.requestParameters.getPropagation());
        if (propagation == null) {
            propagation = PropagationParam.DESCENDANTS;
        }

        //Create ontologies
        OntologyService ontoService = this.serviceFactory.getOntologyService();
        Ontology<?, String> ontology = null;
        if (CallService.Attribute.ANAT_ENTITY_ID.equals(condParam)) {
            ontology = ontoService.getAnatEntityOntology(speciesId, entityIds,
                    Arrays.asList(RelationType.ISA_PARTOF),
                    propagation.isRequireAncestors(), propagation.isRequireDescendants());
        } else if (CallService.Attribute.DEV_STAGE_ID.equals(condParam)) {
            ontology = ontoService.getDevStageOntology(speciesId, entityIds,
                    propagation.isRequireAncestors(), propagation.isRequireDescendants());
        }

        // attributes used to generate the tsv file
        List<String> requestedAttrs = convertPropagatedAttrs(condParam,
                requestParameters.getValues(this.requestParameters
                        .getUrlParametersInstance().getParamAttributeList()));
        retrievePropagatedEntities(ontology, entityIds, propagation, requestedAttrs);
    }

    private <T extends NamedEntity<U> & OntologyElement<T, U>,U extends Comparable<U>> void
    retrievePropagatedEntities (Ontology<T,U> ontology, Collection<U> entityIds,
            PropagationParam propagation, List<String> requestedAttrs)
                    throws InvalidRequestException, IOException {
        log.traceEntry("{}, {}, {}, {}", ontology, entityIds, propagation, requestedAttrs);

        Set<U> entityIdSet = new HashSet<>(entityIds);
        Set<T> entities = entityIdSet.stream().map(s -> ontology.getElement(s))
                .collect(Collectors.toSet());
        if(entityIdSet.size() != entities.size()) {
            throw log.throwing(new InvalidRequestException(
                    "Some queried entities are not part of the ontology"));
        }

        Set<T> propagatedEntityIds = null;
        try {
            //retrieve descendants of provided entities
            if (propagation.equals(PropagationParam.DESCENDANTS)) {
                propagatedEntityIds = entities.stream()
                        .map(s -> ontology.getDescendants(s))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toSet());

            //retrieve ancestors of provided entities
            } else if (propagation.equals(PropagationParam.ANCESTORS)) {
                propagatedEntityIds = entities.stream()
                        .map(s -> ontology.getAncestors(s))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toSet());

            //retrieve least common ancestor of provided entities
            } else if (propagation.equals(PropagationParam.LEAST_COMMON_ANCESTOR)) {
                propagatedEntityIds = ontology
                        .getLeastCommonAncestors(entities, null).stream()
                        .collect(Collectors.toSet());
            }
        } catch (IllegalArgumentException e) {
            throw log.throwing(new InvalidRequestException(e.getMessage()));
        }

        RPackageDisplay display = this.viewFactory.getRPackageDisplay();
        display.displayPropagation(requestedAttrs, propagatedEntityIds);
    }

    private static Set<AnatEntityService.Attribute> convertRqAttrsToAEAttrs(List<String> rqAttrs){
        log.traceEntry("{}", rqAttrs);
        Set<AnatEntityService.Attribute> attrs = new HashSet<>();
        for(String rqAttr : rqAttrs){
            switch(rqAttr){
                case AE_ID_PARAM :
                    attrs.add(AnatEntityService.Attribute.ID);
                    break;
                case AE_NAME_PARAM :
                    attrs.add(AnatEntityService.Attribute.NAME);
                    break;
                case AE_DESCRIPTION_PARAM :
                    attrs.add(AnatEntityService.Attribute.DESCRIPTION);
                    break;
                default :
                    throw log.throwing(new UnsupportedOperationException(
                            "Attribute parameter not supported: " + rqAttr));
            } 
        }
        return log.traceExit(attrs);
    }

    private static Set<CallService.Attribute> convertRqAttrsToCallsAttrs(List<String> rqAttrs){
        log.traceEntry("{}", rqAttrs);
        Set<CallService.Attribute> attrs = new HashSet<>();
        for(String rqAttr : rqAttrs){
            switch(rqAttr){
                case CALLS_GENE_ID_PARAM :
                    attrs.add(CallService.Attribute.GENE);
                    break;
                case CALLS_ANAT_ENTITY_ID_PARAM :
                    attrs.add(CallService.Attribute.ANAT_ENTITY_ID);
                    break;
                case CALLS_DATA_QUALITY_PARAM :
                    attrs.add(CallService.Attribute.DATA_QUALITY);
                    break;
                case CALLS_DEV_STAGE_PARAM :
                    attrs.add(CallService.Attribute.DEV_STAGE_ID);
                    break;
                default :
                    throw log.throwing(new UnsupportedOperationException(
                            "Attribute parameter not supported: " + rqAttr));
            }
        }
        return log.traceExit(attrs);
    }

    private static List<String> convertPropagatedAttrs(CallService.Attribute condParam,
            List<String> attrs) {
        log.traceEntry("{}, {}", condParam, attrs);

        List<String> requestedAttrs = new ArrayList<>();
        Set<String> validAttrs = new HashSet<>(Arrays.asList(PROPAGATION_ID_PARAM,
                PROPAGATION_NAME_PARAM, PROPAGATION_DESCRIPTION_PARAM,
                PROPAGATION_LEVEL_PARAM, PROPAGATION_LEFTBOUND_PARAM,
                PROPAGATION_RIGHTBOUND_PARAM));
        if (attrs != null) {
            requestedAttrs = attrs.stream().filter(a -> validAttrs.contains(a))
                    .collect(Collectors.toList());
        }
        if(requestedAttrs.isEmpty()) {
            requestedAttrs.add(PROPAGATION_ID_PARAM);
            requestedAttrs.add(PROPAGATION_NAME_PARAM);
            requestedAttrs.add(PROPAGATION_DESCRIPTION_PARAM);
            if (CallService.Attribute.DEV_STAGE_ID.equals(condParam)) {
                requestedAttrs.add(PROPAGATION_LEVEL_PARAM);
                requestedAttrs.add(PROPAGATION_LEFTBOUND_PARAM);
                requestedAttrs.add(PROPAGATION_RIGHTBOUND_PARAM);
            }
        }
        return log.traceExit(requestedAttrs);
    }

    private static void checkRelationAttrs(List<String> rqAttrs){
        log.traceEntry("{}", rqAttrs);
        for(String rqAttr : rqAttrs){
            if (!rqAttr.equals(CommandRPackage.RELATIONS_SOURCE_PARAM)
                    && !rqAttr.equals(CommandRPackage.RELATIONS_TARGET_PARAM)
                    && !rqAttr.equals(CommandRPackage.RELATION_TYPE_PARAM)
                    && !rqAttr.equals(CommandRPackage.RELATION_STATUS_PARAM)){
                throw log.throwing(new UnsupportedOperationException(
                        "Attribute parameter not supported: " + rqAttr));
            }
        }
    }

    private static void checkSpeciesAttrs(List<String> rqAttrs){
        log.traceEntry("{}", rqAttrs);
        for(String rqAttr : rqAttrs){
            if (!rqAttr.equals(SPECIES_ID_PARAM)
                    && !rqAttr.equals(SPECIES_GENUS_PARAM)
                    && !rqAttr.equals(SPECIES_NAME_PARAM)
                    && !rqAttr.equals(SPECIES_COMMON_NAME_PARAM)
                    && !rqAttr.equals(DataType.AFFYMETRIX.toString())
                    && !rqAttr.equals(DataType.EST.toString())
                    && !rqAttr.equals(DataType.IN_SITU.toString())
                    && !rqAttr.equals(DataType.RNA_SEQ.toString())
                    && !rqAttr.equals(DataType.SC_RNA_SEQ.toString())){
                throw log.throwing(new UnsupportedOperationException(
                        "Attribute parameter not supported: " + rqAttr));
            }
        }
    }
}
