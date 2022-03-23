package org.bgee.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.management.InvalidAttributeValueException;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.InvalidRequestException;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.controller.user.User;
import org.bgee.model.BgeeEnum.BgeeEnumField;
import org.bgee.model.NamedEntity;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.ConditionFilter;
import org.bgee.model.expressiondata.ConditionService;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.job.Job;
import org.bgee.model.job.JobService;
import org.bgee.model.job.exception.ThreadAlreadyWorkingException;
import org.bgee.model.job.exception.TooManyJobsException;
import org.bgee.model.ontology.MultiSpeciesOntology;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.ontology.OntologyService;
import org.bgee.model.ontology.RelationType;
import org.bgee.model.species.Species;
import org.bgee.model.species.SpeciesService;
import org.bgee.view.ErrorDisplay;
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
    public final static String SPECIES_FULL_LENGTH_PARAM = "FULL_LENGTH";

    public final static String PROPAGATION_ID_PARAM = "ID";
    public final static String PROPAGATION_NAME_PARAM = "NAME";
    public final static String PROPAGATION_DESCRIPTION_PARAM = "DESCRIPTION";
    public final static String PROPAGATION_LEVEL_PARAM = "LEVEL";
    public final static String PROPAGATION_LEFTBOUND_PARAM = "LEFT_BOUND";
    public final static String PROPAGATION_RIGHTBOUND_PARAM = "RIGHT_BOUND";

    public enum PropagationParam {
        DESCENDANTS,ANCESTORS,LEAST_COMMON_ANCESTOR;
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
                this.processPropagation(CALLS_ANAT_ENTITY_ID_PARAM);
            } else if ("get_propagation_dev_stage".equals(
                    this.requestParameters.getAction())) {
                this.processPropagation(CALLS_DEV_STAGE_PARAM);
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


        //****************************************
        // Perform query and display results
        //****************************************
        Stream<ExpressionCall> callStream = this.serviceFactory.getCallService().loadExpressionCalls(
                callFilter,
                //Attributes requested; no ordering requested
                attrs, null);
        display.displayCalls(requestedAttrs, callStream);
        
        log.traceExit();
    }
    
    /**
     * Performs the query and display the results when requesting {@code AnatEntityTO}s.
     * 
     * @throws InvalidRequestException  In case of invalid request parameter.
     * @throws IOException              In case of issue when writing results. 
     */
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
            requestedAttrs.add(SPECIES_AFFYMETRIX_PARAM);
            requestedAttrs.add(SPECIES_EST_PARAM);
            requestedAttrs.add(SPECIES_IN_SITU_PARAM);
            requestedAttrs.add(SPECIES_RNA_SEQ_PARAM);
            requestedAttrs.add(SPECIES_FULL_LENGTH_PARAM);
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
    
    private void processPropagation(String conditionParameter) throws IOException,
    InvalidRequestException {
        log.traceEntry();

        OntologyService ontoService = this.serviceFactory.getOntologyService();
        RPackageDisplay display = this.viewFactory.getRPackageDisplay();

        //****************************************
        // Retrieve and filter request parameters
        //****************************************

        final Integer speciesId = this.requestParameters.getSpeciesId();
        if(speciesId == null) {
            throw log.throwing(new InvalidRequestException("one species ID must be provided"));
        }

        List<String> entityIds = null;
        if(conditionParameter.equals(CALLS_ANAT_ENTITY_ID_PARAM)) {
            entityIds = this.requestParameters.getAnatEntity();
        } else if(conditionParameter.equals(CALLS_DEV_STAGE_PARAM)) {
            entityIds = this.requestParameters.getDevStage();
        } else {
            throw log.throwing(new InvalidRequestException(conditionParameter + "is not a valid "
                    + "condition parameter"));
        }
        String propagation = this.requestParameters.getPropagation();

        Boolean descendant = propagation == null || propagation
                .equals(PropagationParam.DESCENDANTS.toString()) ? true : false;
        //only option not to require ancestors is when descendants are required
        Boolean ancestor = descendant ? false : true;
        if (entityIds == null || entityIds.isEmpty()) {
            throw log.throwing(new InvalidRequestException("At least one ID must be provided"));
        }

        // attributes used to generate the tsv file
        List<String> requestedAttrs = convertPropagatedAttrs(conditionParameter,
                requestParameters.getValues(this.requestParameters
                        .getUrlParametersInstance().getParamAttributeList()));
        
        try {
            if (conditionParameter.equals(CALLS_ANAT_ENTITY_ID_PARAM)) {
                retrievePropagatedAnatEntities(ontoService, display, speciesId,
                        entityIds, propagation, descendant, ancestor, requestedAttrs);
            } else if (conditionParameter.equals(CALLS_DEV_STAGE_PARAM)) {
            retrievePropagatedStages(ontoService, display, speciesId,
                    entityIds, propagation, descendant, ancestor, requestedAttrs);
            }
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        }
    }

    private void retrievePropagatedAnatEntities (OntologyService ontoService,
            RPackageDisplay display, Integer speciesId, Collection<String> entityIds,
            String propagation, boolean descendant, boolean ancestor,
            List<String> requestedAttrs) throws InvalidRequestException {
        
        Ontology<AnatEntity, String> ontology = ontoService
                .getAnatEntityOntology(speciesId, entityIds,
                        Arrays.asList(RelationType.ISA_PARTOF), ancestor, descendant);
        Set<AnatEntity> entities = entityIds.stream().map(s -> ontology.getElement(s))
                .collect(Collectors.toSet());

        if(entities == null || entities.isEmpty()) {
            throw log.throwing(new InvalidRequestException(""));
        }

        Set<AnatEntity> propagatedAnatEntityIds = null;

        //retrieve descendants of provided anatomical entities
        if (propagation == null || propagation.equals(PropagationParam.DESCENDANTS.toString())) {
            propagatedAnatEntityIds = entities.stream()
            .map(s -> ontology.getDescendants(s))
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

        //retrieve ancestors of provided anatomical entities
        } else if (propagation.equals(PropagationParam.ANCESTORS.toString())) {
            propagatedAnatEntityIds = entities.stream()
            .map(s -> ontology.getAncestors(s))
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

        //retrieve least common ancestor of provided anatomical entities
        } else if (propagation.equals(PropagationParam.LEAST_COMMON_ANCESTOR.toString())) {
            propagatedAnatEntityIds = ontology
                    .getLeastCommonAncestors(entities, null).stream()
                    .collect(Collectors.toSet());
        }
        display.displayAnatEntityPropagation(requestedAttrs, propagatedAnatEntityIds);
    }

    private void retrievePropagatedStages (OntologyService ontoService,
            RPackageDisplay display, Integer speciesId, Collection<String> stageIds,
            String propagation, boolean descendant, boolean ancestor,
            List<String> requestedAttrs) throws InvalidRequestException {

        MultiSpeciesOntology<DevStage, String> ontology = ontoService
                .getDevStageOntology(Collections.singleton(speciesId), stageIds, ancestor, descendant);
        Set<DevStage> entities = stageIds.stream().map(s -> ontology.getElement(s))
                .collect(Collectors.toSet());

        if(entities == null || entities.isEmpty()) {
            throw log.throwing(new InvalidRequestException("No queried stages are part "
                    + "of the ontology"));
        }
        //retrieve descendants of provided stages
        Set<DevStage> propagatedStageIds = null;
        if (propagation == null || propagation.equals(
                PropagationParam.DESCENDANTS.toString())) {
            propagatedStageIds = entities.stream()
            .map(s -> ontology.getDescendants(s))
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

        //retrieve ancestors of provided stages
        } else if (propagation.equals(PropagationParam.ANCESTORS.toString())) {
            propagatedStageIds = entities.stream()
            .map(s -> ontology.getAncestors(s))
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

        //retrieve least common ancestor of provided anatomical entities
        } else if (propagation.equals(PropagationParam.LEAST_COMMON_ANCESTOR.toString())) {
            propagatedStageIds = ontology
                    .getLeastCommonAncestors(entities, null).stream()
                    .collect(Collectors.toSet());
        }

        display.displayDevStagePropagation(requestedAttrs, propagatedStageIds);
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

    private static List<String> convertPropagatedAttrs(String conditionParameter,
            List<String> attrs) {
        log.traceEntry("{}", conditionParameter);

        List<String> requestedAttrs = new ArrayList<>();
        if(attrs == null) {
            requestedAttrs.add(PROPAGATION_ID_PARAM);
            requestedAttrs.add(PROPAGATION_NAME_PARAM);
            requestedAttrs.add(PROPAGATION_DESCRIPTION_PARAM);
            if(conditionParameter.equals(CALLS_DEV_STAGE_PARAM)) {
                requestedAttrs.add(PROPAGATION_LEVEL_PARAM);
                requestedAttrs.add(PROPAGATION_LEFTBOUND_PARAM);
                requestedAttrs.add(PROPAGATION_RIGHTBOUND_PARAM);
            }
        } else {
            for(String attr : attrs){
                switch(attr){
                    case PROPAGATION_ID_PARAM :
                        requestedAttrs.add(PROPAGATION_ID_PARAM);
                        break;
                    case PROPAGATION_NAME_PARAM :
                        requestedAttrs.add(PROPAGATION_NAME_PARAM);
                        break;
                    case PROPAGATION_DESCRIPTION_PARAM :
                        requestedAttrs.add(PROPAGATION_DESCRIPTION_PARAM);
                        break;
                    case PROPAGATION_LEVEL_PARAM :
                        requestedAttrs.add(PROPAGATION_LEVEL_PARAM);
                        break;
                    case PROPAGATION_LEFTBOUND_PARAM :
                        requestedAttrs.add(PROPAGATION_LEFTBOUND_PARAM);
                        break;
                    case PROPAGATION_RIGHTBOUND_PARAM :
                        requestedAttrs.add(PROPAGATION_RIGHTBOUND_PARAM);
                        break;
                }
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
                    && !rqAttr.equals(SPECIES_AFFYMETRIX_PARAM)
                    && !rqAttr.equals(SPECIES_EST_PARAM)
                    && !rqAttr.equals(SPECIES_IN_SITU_PARAM)
                    && !rqAttr.equals(SPECIES_RNA_SEQ_PARAM)
                    && !rqAttr.equals(SPECIES_FULL_LENGTH_PARAM)){
                throw log.throwing(new UnsupportedOperationException(
                        "Attribute parameter not supported: " + rqAttr));
            }
        }
    }
}
