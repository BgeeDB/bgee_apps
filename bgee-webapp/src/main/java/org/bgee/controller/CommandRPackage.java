package org.bgee.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.InvalidRequestException;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.controller.user.User;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.ConditionFilter;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.job.Job;
import org.bgee.model.job.JobService;
import org.bgee.model.job.exception.ThreadAlreadyWorkingException;
import org.bgee.model.job.exception.TooManyJobsException;
import org.bgee.model.ontology.OntologyRelation;
import org.bgee.model.ontology.OntologyService;
import org.bgee.model.ontology.RelationType;
import org.bgee.model.species.Species;
import org.bgee.model.species.SpeciesService;
import org.bgee.view.RPackageDisplay;
import org.bgee.view.ViewFactory;

/**
 * Controller that handles requests necessary for the use of the BgeeDB Bioconductor R package.
 * This controller is needed as, starting from Bgee 14 and the new method for computing call qualities,
 * it is simpler for generating responses to R package queries to use {@code Service} calls rather than
 * direct {@code DAO} calls (handled in {@link CommandDAO}).
 * 
 * @author  Frederic Bastian
 * @version Bgee 14 Mar. 2017
 * @see https://www.bioconductor.org/packages/BgeeDB/
 * @since   Bgee 14 Mar. 2017
 */
public class CommandRPackage extends CommandParent {
    private final static Logger log = LogManager.getLogger(CommandRPackage.class.getName());
    
    //XXX move them to RequestParameters ????
    
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
        log.entry();
        

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
        
        log.exit();
    }
    
    /**
     * Performs the query and display the results when requesting {@code ExpressionCall}s.
     * 
     * @throws InvalidRequestException  In case of invalid request parameter.
     * @throws IOException              In case of issue when writing results. 
     */
    private void processGetExpressionCalls() throws InvalidRequestException, IOException {
        log.entry();
        
        RPackageDisplay display = this.viewFactory.getRPackageDisplay();
        
        //****************************************
        // Retrieve and filter request parameters
        //****************************************
        //Data types and quality
        final Set<DataType> dataTypes = this.checkAndGetDataTypes();
//        final SummaryQuality dataQuality = this.checkAndGetSummaryQuality();

        //parameters not needing processing
        final List<Integer> speciesIds = this.requestParameters.getSpeciesList();
        final List<String> stageIds   = this.requestParameters.getDevStage();
        List<String> requestedAttrs = this.requestParameters.getValues(
        		this.requestParameters.getUrlParametersInstance().getParamAttributeList());
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
        Boolean onlyObservedCalls = true;
        Boolean onlyObservedStages = true;
        if(stageIds != null){
        	onlyObservedCalls = false;
        	onlyObservedStages = false;
        }
        Collection<ConditionFilter> conditionFilter = stageIds == null || stageIds.isEmpty()? 
                null: Collections.singleton(new ConditionFilter(null, stageIds));
        GeneFilter geneFilter = new GeneFilter(speciesId, this.requestParameters.getBackgroundList());
        Map<ExpressionSummary, SummaryQuality> summaryCallTypeQualityFilter = new HashMap<>();
        summaryCallTypeQualityFilter.put(ExpressionSummary.EXPRESSED, this.checkAndGetSummaryQuality());
        ExpressionCallFilter callFilter = new ExpressionCallFilter(summaryCallTypeQualityFilter,
        		Collections.singleton(geneFilter),
                conditionFilter, dataTypes,
                onlyObservedCalls, true, onlyObservedStages);


        //****************************************
        // Perform query and display results
        //****************************************
        Stream<ExpressionCall> callStream = this.serviceFactory.getCallService().loadExpressionCalls(
                callFilter,
                //for now, we always include substages and never include substructures
//                false, true,
                //Attributes requested; no ordering requested
                attrs, null);
//        Set<ExpressionCall> calls = callStream.collect(Collectors.toSet());
        display.displayCalls(requestedAttrs, callStream);
        
        log.exit();
    }
    
    /**
     * Performs the query and display the results when requesting {@code AnatEntityTO}s.
     * 
     * @throws InvalidRequestException  In case of invalid request parameter.
     * @throws IOException              In case of issue when writing results. 
     */
    private void processGetAnatEntities() throws InvalidRequestException, IOException {
        log.entry();
        
        AnatEntityService aeService = this.serviceFactory.getAnatEntityService();
        RPackageDisplay display = this.viewFactory.getRPackageDisplay();

        //****************************************
        // Retrieve and filter request parameters
        //****************************************
        final List<Integer> speciesIds = this.requestParameters.getSpeciesList();
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
        
        log.exit();
    }
    
    /**
     * Performs the query and display the results when requesting {@code RelationTO}s 
     * for anatomical entities.
     * 
     * @throws InvalidRequestException  In case of invalid request parameter.
     * @throws IOException              In case of issue when writing results. 
     */
    private void processGetAnatEntityRelations() throws InvalidRequestException, IOException {
        log.entry();
        RPackageDisplay display = this.viewFactory.getRPackageDisplay();
        OntologyService ontologyService = this.serviceFactory.getOntologyService();

        //****************************************
        // Retrieve and filter request parameters
        //****************************************
        final List<Integer> speciesIds = this.requestParameters.getSpeciesList();
        
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
//        Integer speciesId = speciesIds.iterator().next();
        //****************************************
        // Perform query and display results
        //****************************************
//        Ontology<AnatEntity, String> anatOntology = ontologyService.getAnatEntityOntology(
//        		speciesId, null, Collections.singleton(RelationType.ISA_PARTOF), false, false);
//        Set<AnatEntity> anatEntities = anatOntology.getElements();
//        anatEntities.stream().forEach(ae -> {
//        		anatEntityRelations.put(ae.getId(), 
//        				anatOntology.getAncestors(ae, true).stream().map(target -> target.getId())
//        				.collect(Collectors.toSet()));
//        });
        Set<OntologyRelation<String>> elementRelations = ontologyService.getAnatEntityRelations(speciesIds, null,
        		Collections.singleton(RelationType.ISA_PARTOF), 
        		Collections.singleton(OntologyRelation.RelationStatus.DIRECT), false, false);
        
        display.displayAERelations(requestedAttrs, elementRelations);
        
        log.exit();
    }
    
    /**
     * Performs the query and display the results when requesting {@code SpeciesTO}s.
     * 
     * @throws InvalidRequestException  In case of invalid request parameter.
     * @throws IOException              In case of issue when writing results. 
     */
    private void processGetAllSpecies() throws IOException {
        log.entry();
        
        SpeciesService spService = this.serviceFactory.getSpeciesService();
        RPackageDisplay display = this.viewFactory.getRPackageDisplay();

        //****************************************
        // Retrieve and filter request parameters
        //****************************************
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
        
        log.exit();
    }
    
    private static Set<AnatEntityService.Attribute> convertRqAttrsToAEAttrs(List<String> rqAttrs){
    	log.entry(rqAttrs);
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
        return log.exit(attrs);
    }
    
    private static Set<CallService.Attribute> convertRqAttrsToCallsAttrs(List<String> rqAttrs){
    	log.entry(rqAttrs);
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
        return log.exit(attrs);
    }
    
    private static void checkRelationAttrs(List<String> rqAttrs){
    	log.entry(rqAttrs);
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
    	log.entry(rqAttrs);
        for(String rqAttr : rqAttrs){
        	if (!rqAttr.equals(SPECIES_ID_PARAM)
        			&& !rqAttr.equals(SPECIES_GENUS_PARAM)
        			&& !rqAttr.equals(SPECIES_NAME_PARAM)
        			&& !rqAttr.equals(SPECIES_COMMON_NAME_PARAM)
        			&& !rqAttr.equals(SPECIES_AFFYMETRIX_PARAM)
        			&& !rqAttr.equals(SPECIES_EST_PARAM)
        			&& !rqAttr.equals(SPECIES_IN_SITU_PARAM)
        			&& !rqAttr.equals(SPECIES_RNA_SEQ_PARAM)){
        		throw log.throwing(new UnsupportedOperationException(
                        "Attribute parameter not supported: " + rqAttr));
        	}
        }
    }
    
}
