package org.bgee.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.InvalidRequestException;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.controller.user.User;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTOResultSet;
import org.bgee.model.dao.api.expressiondata.CallDAOFilter;
import org.bgee.model.dao.api.expressiondata.DAOConditionFilter;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO.GlobalExpressionCallTOResultSet;
import org.bgee.model.dao.api.ontologycommon.RelationDAO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTOResultSet;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTOResultSet;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.ConditionFilter;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.job.Job;
import org.bgee.model.job.JobService;
import org.bgee.model.job.exception.ThreadAlreadyWorkingException;
import org.bgee.model.job.exception.TooManyJobsException;
import org.bgee.view.DAODisplay;
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
 * @since   Bgee 14 Mar. 2917
 */
public class CommandRPackage extends CommandParent {
    private final static Logger log = LogManager.getLogger(CommandRPackage.class.getName());

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
//            if ("get_expression_calls".equals(
//                    this.requestParameters.getAction())) {
//                
//                this.processGetExpressionCalls();
//                
//            } else if ("get_anat_entities".equals(
//                    this.requestParameters.getAction())) { 
//                
//                this.processGetAnatEntities();
//            } else if ("get_anat_entity_relations".equals(
//                    this.requestParameters.getAction())) { 
//                
//                this.processGetAnatEntitiyRelations();
//            } else if ("get_all_species".equals(
//                    this.requestParameters.getAction())) { 
//                
//                this.processGetAllSpecies();
//            } else {
//                throw log.throwing(new PageNotFoundException("Incorrect " + 
//                        this.requestParameters.getUrlParametersInstance().getParamAction() + 
//                        " parameter value."));
//            }
        } finally {
            //we don't care whether the job is successful or not, we just want 
            //to keep track of number of running jobs per user
            job.release();
        }
        
        log.exit();
    }
    
//    /**
//     * Performs the query and display the results when requesting {@code ExpressionCall}s.
//     * 
//     * @throws InvalidRequestException  In case of invalid request parameter.
//     * @throws IOException              In case of issue when writing results. 
//     */
//    private void processGetExpressionCalls() throws InvalidRequestException, IOException {
//        log.entry();
//        
//        DAODisplay display = this.viewFactory.getRPackageDisplay();
//        
//        //****************************************
//        // Retrieve and filter request parameters
//        //****************************************
//        //Data types and quality
//        final Set<DataType> dataTypes = this.checkAndGetDataTypes();
//        final SummaryQuality dataQuality = this.checkAndGetSummaryQuality();
//
//        //parameters not needing processing
//        final List<Integer> speciesIds = this.requestParameters.getSpeciesList();
//        final List<String> stageIds   = this.requestParameters.getDevStage();
//
//        final List<CallService.Attribute> attrs = getAttributes(this.requestParameters, 
//                CallService.Attribute.class);
//
//        //for now, we force to select one and only one species, to not return 
//        //the complete expression table at once
//        if (speciesIds == null || speciesIds.size() != 1 ||
//                speciesIds.stream().anyMatch(id -> id == null || id <= 0)) {
//            throw log.throwing(new InvalidRequestException("One and only one species ID must be provided"));
//        }
//        int speciesId = speciesIds.iterator().next();
//        
//        //****************************************
//        // Create Call filter objects
//        //****************************************
//        //CallDAOFilter: for now, we only allow to define one CallDAOFilter object.
//        ConditionFilter conditionFilter = stageIds == null || stageIds.isEmpty()? 
//                null: new ConditionFilter(null, stageIds);
//        GeneFilter geneFilter = new GeneFilter(speciesId, this.requestParameters.getBackgroundList());
//        ExpressionCallFilter callFilter = new ExpressionCallFilter(geneFilter,
//                Collections.singleton(conditionFilter), this.checkAndGetDataTypes(),
//                ExpressionSummary.EXPRESSED, this.checkAndGetSummaryQuality(), true);
//
//        //****************************************
//        // Perform query and display results
//        //****************************************
//        Stream<ExpressionCall> callStream = this.serviceFactory.getCallService().loadExpressionCalls(
//                callFilter, 
//                //for now, we always include substages and never include substructures
////                false, true,
//                //Attributes requested; no ordering requested
//                attrs, null);
//        
//        display.displayCalls(attrs, callStream);
//        
//        log.exit();
//    }
//    
//    /**
//     * Performs the query and display the results when requesting {@code AnatEntityTO}s.
//     * 
//     * @throws InvalidRequestException  In case of invalid request parameter.
//     * @throws IOException              In case of issue when writing results. 
//     */
//    private void processGetAnatEntities() throws InvalidRequestException, IOException {
//        log.entry();
//        
//        DAOManager daoManager = this.serviceFactory.getDAOManager();
//        DAODisplay display = this.viewFactory.getDAODisplay();
//
//        //****************************************
//        // Retrieve and filter request parameters
//        //****************************************
//        final List<String> speciesIds = this.requestParameters.getSpeciesList();
//        
//        final List<AnatEntityDAO.Attribute> attrs = getAttributes(this.requestParameters, 
//                AnatEntityDAO.Attribute.class);
//        
//        //for now, we force to select one and only one species, to not return 
//        //the complete expression table at once
//        if (speciesIds == null || speciesIds.size() != 1) {
//            throw log.throwing(new InvalidRequestException("One and only one species ID must be provided"));
//        }
//        
//        //****************************************
//        // Perform query and display results
//        //****************************************
//        AnatEntityTOResultSet rs = daoManager.getAnatEntityDAO().getAnatEntities(
//                speciesIds, true, null, attrs);
//        
//        display.displayTOs(attrs, rs);
//        
//        log.exit();
//    }
//    
//    /**
//     * Performs the query and display the results when requesting {@code RelationTO}s 
//     * for anatomical entities.
//     * 
//     * @throws InvalidRequestException  In case of invalid request parameter.
//     * @throws IOException              In case of issue when writing results. 
//     */
//    private void processGetAnatEntitiyRelations() throws InvalidRequestException, IOException {
//        log.entry();
//        
//        DAOManager daoManager = this.serviceFactory.getDAOManager();
//        DAODisplay display = this.viewFactory.getDAODisplay();
//
//        //****************************************
//        // Retrieve and filter request parameters
//        //****************************************
//        final List<String> speciesIds = this.requestParameters.getSpeciesList();
//        
//        final List<RelationDAO.Attribute> attrs = getAttributes(this.requestParameters, 
//                RelationDAO.Attribute.class);
//        
//        //for now, we force to select one and only one species, to not return 
//        //the complete expression table at once
//        if (speciesIds == null || speciesIds.size() != 1) {
//            throw log.throwing(new InvalidRequestException("One and only one species ID must be provided"));
//        }
//        
//        //****************************************
//        // Perform query and display results
//        //****************************************
//        RelationTOResultSet rs = daoManager.getRelationDAO().getAnatEntityRelations(
//                speciesIds, true, null, null, true, 
//                EnumSet.of(RelationTO.RelationType.ISA_PARTOF), 
//                EnumSet.of(RelationTO.RelationStatus.DIRECT), 
//                attrs);
//        
//        display.displayTOs(attrs, rs);
//        
//        log.exit();
//    }
//    
//    /**
//     * Performs the query and display the results when requesting {@code SpeciesTO}s.
//     * 
//     * @throws InvalidRequestException  In case of invalid request parameter.
//     * @throws IOException              In case of issue when writing results. 
//     */
//    private void processGetAllSpecies() throws IOException {
//        log.entry();
//        
//        DAOManager daoManager = this.serviceFactory.getDAOManager();
//        DAODisplay display = this.viewFactory.getDAODisplay();
//
//        //****************************************
//        // Retrieve and filter request parameters
//        //****************************************
//        final List<SpeciesDAO.Attribute> attrs = getAttributes(this.requestParameters, 
//                SpeciesDAO.Attribute.class);
//        
//        //****************************************
//        // Perform query and display results
//        //****************************************
//        SpeciesDAO dao = daoManager.getSpeciesDAO();
//        dao.setAttributes(attrs);
//        SpeciesTOResultSet rs = dao.getAllSpecies();
//        
//        display.displayTOs(attrs, rs);
//        
//        log.exit();
//    }
}
