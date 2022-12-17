//package org.bgee.controller;
//
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.EnumSet;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//import javax.servlet.http.HttpServletResponse;
//
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.bgee.controller.exception.InvalidRequestException;
//import org.bgee.controller.exception.PageNotFoundException;
//import org.bgee.controller.user.User;
//import org.bgee.model.ServiceFactory;
//import org.bgee.model.dao.api.DAO;
//import org.bgee.model.dao.api.DAOManager;
//import org.bgee.model.dao.api.anatdev.AnatEntityDAO;
//import org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTOResultSet;
//import org.bgee.model.dao.api.expressiondata.call.CallDAOFilter;
//import org.bgee.model.dao.api.expressiondata.call.DAOConditionFilter;
//import org.bgee.model.dao.api.expressiondata.call.ExpressionCallDAO;
//import org.bgee.model.dao.api.expressiondata.call.CallDAO.CallTO;
//import org.bgee.model.dao.api.expressiondata.call.ExpressionCallDAO.ExpressionCallTO;
//import org.bgee.model.dao.api.expressiondata.call.ExpressionCallDAO.ExpressionCallTOResultSet;
//import org.bgee.model.dao.api.expressiondata.call.GlobalExpressionCallDAO.GlobalExpressionCallTOResultSet;
//import org.bgee.model.dao.api.ontologycommon.RelationDAO;
//import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
//import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTOResultSet;
//import org.bgee.model.dao.api.species.SpeciesDAO;
//import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTOResultSet;
//import org.bgee.model.expressiondata.baseelements.DataQuality;
//import org.bgee.model.expressiondata.baseelements.DataType;
//import org.bgee.model.job.Job;
//import org.bgee.model.job.JobService;
//import org.bgee.model.job.exception.ThreadAlreadyWorkingException;
//import org.bgee.model.job.exception.TooManyJobsException;
//import org.bgee.view.DAODisplay;
//import org.bgee.view.ViewFactory;
//
///**
// * Controller that handles requests allowing to use Bgee DAOs as a webservice. 
// * It is the only controller allowed to directly manipulate DAOs, rather than the bgee-core layer.
// * 
// * @author  Frederic Bastian
// * @version Bgee 13 Mar. 2016
// * @since   Bgee 13
// */
//public class CommandDAO extends CommandParent {
//
//    /**
//     * {@code Logger} of the class. 
//     */
//    private final static Logger log = LogManager.getLogger(CommandDownload.class.getName());
//
//    /**
//     * Constructor
//     * 
//     * @param response          A {@code HttpServletResponse} that will be used to display the 
//     *                          page to the client
//     * @param requestParameters The {@code RequestParameters} that handles the parameters of the 
//     *                          current request.
//     * @param prop              A {@code BgeeProperties} instance that contains the properties
//     *                          to use.
//     * @param viewFactory       A {@code ViewFactory} that provides the display type to be used.
//     * @param serviceFactory    A {@code ServiceFactory} that provides bgee services.
//     * @param jobService        A {@code JobService} instance allowing to manage jobs between threads 
//     *                          across the entire webapp.
//     * @param user              The {@code User} who is making the query to the webapp.
//     */
//    public CommandDAO (HttpServletResponse response, RequestParameters requestParameters, 
//            BgeeProperties prop, ViewFactory viewFactory, ServiceFactory serviceFactory, 
//            JobService jobService, User user) {
//        super(response, requestParameters, prop, viewFactory, serviceFactory, jobService, user, null, null);
//    }
//
//    @Override
//    public void processRequest() throws IllegalStateException, IOException, 
//        PageNotFoundException, InvalidRequestException, ThreadAlreadyWorkingException, TooManyJobsException {
//        log.traceEntry();
//        throw log.throwing(new UnsupportedOperationException(
//                "To be implemented, not used anymore for R package"));
//
//        Job job = this.jobService.registerNewJob(this.user.getUUID().toString());
//        try {
//            //XXX: we should certainly have something using reflection API, 
//            //for now we simply hardcode DAO methods that are supported
//            if ("org.bgee.model.dao.api.expressiondata.call.ExpressionCallDAO.getExpressionCalls".equals(
//                    this.requestParameters.getAction())) {
//                
//                this.processGetExpressionCalls();
//                
//            } else if ("org.bgee.model.dao.api.anatdev.AnatEntityDAO.getAnatEntities".equals(
//                    this.requestParameters.getAction())) { 
//                
//                this.processGetAnatEntities();
//            } else if ("org.bgee.model.dao.api.ontologycommon.RelationDAO.getAnatEntityRelations".equals(
//                    this.requestParameters.getAction())) { 
//                
//                this.processGetAnatEntitiyRelations();
//            } else if ("org.bgee.model.dao.api.species.SpeciesDAO.getAllSpecies".equals(
//                    this.requestParameters.getAction())) { 
//                
//                this.processGetAllSpecies();
//            } else {
//                throw log.throwing(new PageNotFoundException("Incorrect " + 
//                        this.requestParameters.getUrlParametersInstance().getParamAction() + 
//                        " parameter value."));
//            }
//        } finally {
//            //we don't care whether the job is successful or not, we just want 
//            //to keep track of number of running jobs per user
//            job.release();
//        }
//        
//        log.traceExit();
//    }
//    
//    /**
//     * Performs the query and display the results when requesting {@code ExpressionCallTO}s.
//     * 
//     * @throws InvalidRequestException  In case of invalid request parameter.
//     * @throws IOException              In case of issue when writing results. 
//     */
//    private void processGetExpressionCalls() throws InvalidRequestException, IOException {
//        log.traceEntry();
//        
//        DAOManager daoManager = this.serviceFactory.getDAOManager();
//        DAODisplay display = this.viewFactory.getDAODisplay();
//        
//        //****************************************
//        // Retrieve and filter request parameters
//        //****************************************
//        //Data types and quality
//        final Set<DataType> dataTypes = this.checkAndGetDataTypes();
//        final DataQuality dataQuality = this.checkAndGetDataQuality();
//
//        //parameters not needing processing
//        final List<String> speciesIds = this.requestParameters.getSpeciesList();
//        final List<String> stageIds   = this.requestParameters.getDevStage();
//
//        //For now, we exclude any param that will trigger a GROUP BY in the expression query 
//        //(very slow when requesting data for a whole species)
//        final List<ExpressionCallDAO.Attribute> attrs = getAttributes(this.requestParameters, 
//                ExpressionCallDAO.Attribute.class).stream()
//                .filter(attr -> attr.equals(ExpressionCallDAO.Attribute.GENE_ID) || 
//                                attr.equals(ExpressionCallDAO.Attribute.ANAT_ENTITY_ID) || 
//                                attr.equals(ExpressionCallDAO.Attribute.STAGE_ID))
//                .collect(Collectors.toList());
//        
//        
//        //for now, we force to select one and only one species, to not return 
//        //the complete expression table at once
//        if (speciesIds == null || speciesIds.size() != 1) {
//            throw log.throwing(new InvalidRequestException("One and only one species ID must be provided"));
//        }
//        
//        //****************************************
//        // Create DAO filter objects
//        //****************************************
//        //CallDAOFilter: for now, we only allow to define one CallDAOFilter object.
//        DAOConditionFilter conditionFilter = stageIds == null || stageIds.isEmpty()? 
//                null: new DAOConditionFilter(null, stageIds);
//        Set<String> geneIds = this.requestParameters.getBackgroundList() == null? 
//                null: new HashSet<>(this.requestParameters.getBackgroundList());
//        CallDAOFilter callDAOFilter = new CallDAOFilter(geneIds, speciesIds, Arrays.asList(conditionFilter));
//        
//        //convert data types and data qualities to ExpressionCallTO filters
//        Set<ExpressionCallTO> callTOFilter = null;
//        if (DataQuality.HIGH.equals(dataQuality) || 
//                (dataTypes != null && !dataTypes.isEmpty() && 
//                !dataTypes.containsAll(EnumSet.allOf(DataType.class)))) {
//            
//            Set<DataType> usedDataTypes = dataTypes == null || dataTypes.isEmpty()? 
//                    EnumSet.allOf(DataType.class): dataTypes;
//            callTOFilter = usedDataTypes.stream().map(dataType -> {
//                
//                CallTO.DataState affyState = null;
//                CallTO.DataState estState = null;
//                CallTO.DataState inSituState = null;
//                CallTO.DataState rnaSeqState = null;
//                
//                //convert DataQuality to DAO Enum
//                CallTO.DataState state = null;
//                DataQuality usedDataQual = dataQuality == null? DataQuality.LOW: dataQuality;
//                switch(usedDataQual) {
//                case LOW: 
//                    state = CallTO.DataState.LOWQUALITY;
//                    break;
//                case HIGH:
//                    state = CallTO.DataState.HIGHQUALITY;
//                    break;
//                default: 
//                    throw log.throwing(new IllegalStateException("Unsupported DataQuality: " + dataQuality));
//                }
//                
//                switch (dataType) {
//                case AFFYMETRIX: 
//                    affyState = state;
//                    break;
//                case EST: 
//                    estState = state;
//                    break;
//                case IN_SITU: 
//                    inSituState = state;
//                    break;
//                case RNA_SEQ: 
//                    rnaSeqState = state;
//                    break;
//                default: 
//                    throw log.throwing(new IllegalStateException("Unsupported DataType: " + dataType));
//                }
//                
//                return new ExpressionCallTO(affyState, estState, inSituState, rnaSeqState);
//            }).collect(Collectors.toSet());
//        }
//
//        //****************************************
//        // Perform query and display results
//        //****************************************
//        GlobalExpressionCallTOResultSet rs = daoManager.getGlobalExpressionCallDAO().getGlobalExpressionCalls(
//                Arrays.asList(callDAOFilter), 
//                callTOFilter, 
//                //for now, we always include substages and never include substructures
//                false, true, 
//                //no gene ID filtering
//                null, 
//                //no gene orthology requested
//                null, 
//                //Attributes requested; no ordering requested
//                attrs, null);
//        
//        display.displayTOs(attrs, rs);
//        
//        log.traceExit();
//    }
//    
//    /**
//     * Performs the query and display the results when requesting {@code AnatEntityTO}s.
//     * 
//     * @throws InvalidRequestException  In case of invalid request parameter.
//     * @throws IOException              In case of issue when writing results. 
//     */
//    private void processGetAnatEntities() throws InvalidRequestException, IOException {
//        log.traceEntry();
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
//        log.traceExit();
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
//        log.traceEntry();
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
//        log.traceExit();
//    }
//    
//    /**
//     * Performs the query and display the results when requesting {@code SpeciesTO}s.
//     * 
//     * @throws InvalidRequestException  In case of invalid request parameter.
//     * @throws IOException              In case of issue when writing results. 
//     */
//    private void processGetAllSpecies() throws IOException {
//        log.traceEntry();
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
//        log.traceExit();
//    }
//
//    /**
//     * Return the {@code Attribute}s of a DAO corresponding to the attributes requested 
//     * in the request parameters of the query. 
//     * 
//     * @param rqParams  A {@code RequestParameters} holding parameters of a query to the webapp.
//     * @param attrType  A {@code Class} defining the type of {@code Attribute}s needed to be retrieved.
//     * @return          A {@code List} of {@code Attribute}s of type {@code attrType}.
//     */
//    private static <T extends Enum<T> & DAO.Attribute> List<T> getAttributes(RequestParameters rqParams, 
//            Class<T> attrType) {
//        log.entry(rqParams, attrType);
//        
//        List<String> requestedAttrs = rqParams.getValues(
//                rqParams.getUrlParametersInstance().getParamAttributeList());
//        if (requestedAttrs == null || requestedAttrs.isEmpty()) {
//            return log.traceExit(Arrays.asList(attrType.getEnumConstants()));
//        }
//        //we don't use Enum.valueOf to be able to get parameters in lower case. 
//        final Map<String, T> nameToAttr = Arrays.stream(attrType.getEnumConstants())
//                .collect(Collectors.toMap(attr -> attr.name().toLowerCase(), attr -> attr));
//        return log.traceExit(requestedAttrs.stream().map(rqAttr -> nameToAttr.get(rqAttr.toLowerCase()))
//                .collect(Collectors.toList()));
//    }
//}
