package org.bgee.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.InvalidRequestException;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.expressiondata.CallDAOFilter;
import org.bgee.model.dao.api.expressiondata.DAOConditionFilter;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTOResultSet;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.view.DAODisplay;
import org.bgee.view.ViewFactory;

/**
 * Controller that handles requests allowing to use Bgee DAOs as a webservice. 
 * It is the only controller allowed to directly manipulate DAOs, rather than the bgee-core layer.
 * 
 * @author  Frederic Bastian
 * @version Bgee 13 Mar. 2016
 * @since   Bgee 13
 */
public class CommandDAO extends CommandParent {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(CommandDownload.class.getName());

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
     */
    public CommandDAO (HttpServletResponse response, RequestParameters requestParameters, 
            BgeeProperties prop, ViewFactory viewFactory, ServiceFactory serviceFactory) {
        super(response, requestParameters, prop, viewFactory, serviceFactory);
    }

    @Override
    public void processRequest() throws IllegalStateException, IOException, 
        PageNotFoundException, InvalidRequestException {
        log.entry();
        
        //XXX: we should certainly have something using reflection API, 
        //for now we simply hardcode DAO methods that are supported
        if ("org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.getExpressionCalls".equals(
                this.requestParameters.getAction())) {

            this.processGetExpressionCalls();
            
        } else {
            throw log.throwing(new PageNotFoundException("Incorrect " + 
                    this.requestParameters.getUrlParametersInstance().getParamAction() + 
                    " parameter value."));
        }
        
        log.exit();
    }
    
    /**
     * Performs the query and display the results when requesting {@code ExpressionCallTO}s.
     * 
     * @throws InvalidRequestException  In case of invalid request parameter.
     * @throws IOException              In case of issue when writing results. 
     */
    private void processGetExpressionCalls() throws InvalidRequestException, IOException {
        log.entry();
        
        DAOManager daoManager = this.serviceFactory.getDAOManager();
        DAODisplay display = this.viewFactory.getDAODisplay();
        
        //****************************************
        // Retrieve and filter request parameters
        //****************************************
        //Data types and quality
        final Set<DataType> dataTypes = this.checkAndGetDataTypes();
        final DataQuality dataQuality = this.checkAndGetDataQuality();

        //parameters not needing processing
        final List<String> speciesIds = this.requestParameters.getSpeciesList();
        final List<String> stageIds   = this.requestParameters.getDevStage();
        
        final List<ExpressionCallDAO.Attribute> attrs = getAttributes(this.requestParameters, 
                ExpressionCallDAO.Attribute.class);
        
        //for now, we force to select one and only one species, to not return 
        //the complete expression table at once
        if (speciesIds == null || speciesIds.size() != 1) {
            throw log.throwing(new InvalidRequestException("One and only one species ID must be provided"));
        }
        
        //****************************************
        // Create DAO filter objects
        //****************************************
        //CallDAOFilter: for now, we only allow to define one CallDAOFilter object.
        DAOConditionFilter conditionFilter = new DAOConditionFilter(null, stageIds);
        CallDAOFilter callDAOFilter = new CallDAOFilter(null, speciesIds, Arrays.asList(conditionFilter));
        
        //convert data types and data qualities to ExpressionCallTO filters
        Set<ExpressionCallTO> callTOFilter = null;
        if (!dataQuality.equals(DataQuality.LOW) || 
                (dataTypes != null && !dataTypes.isEmpty() && 
                !dataTypes.containsAll(EnumSet.allOf(DataType.class)))) {
            
            Set<DataType> usedDataTypes = dataTypes == null || dataTypes.isEmpty()? 
                    EnumSet.allOf(DataType.class): dataTypes;
            callTOFilter = usedDataTypes.stream().map(dataType -> {
                
                CallTO.DataState affyState = null;
                CallTO.DataState estState = null;
                CallTO.DataState inSituState = null;
                CallTO.DataState rnaSeqState = null;
                
                //convert DataQuality to DAO Enum
                CallTO.DataState state = null;
                switch(dataQuality) {
                case LOW: 
                    state = CallTO.DataState.LOWQUALITY;
                    break;
                case HIGH:
                    state = CallTO.DataState.HIGHQUALITY;
                    break;
                default: 
                    throw log.throwing(new IllegalStateException("Unsupported DataQuality: " + dataQuality));
                }
                
                switch (dataType) {
                case AFFYMETRIX: 
                    affyState = state;
                    break;
                case EST: 
                    estState = state;
                    break;
                case IN_SITU: 
                    inSituState = state;
                    break;
                case RNA_SEQ: 
                    rnaSeqState = state;
                    break;
                default: 
                    throw log.throwing(new IllegalStateException("Unsupported DataType: " + dataType));
                }
                
                return new ExpressionCallTO(affyState, estState, inSituState, rnaSeqState);
            }).collect(Collectors.toSet());
        }

        //****************************************
        // Perform query and display results
        //****************************************
        ExpressionCallTOResultSet rs = daoManager.getExpressionCallDAO().getExpressionCalls(
                Arrays.asList(callDAOFilter), 
                callTOFilter, 
                //for now, we always include substages and never include substructures
                false, true, 
                //no gene ID filtering
                null, 
                //no gene orthology requested
                null, 
                //Attributes requested; no ordering requested
                attrs, null);
        
        display.displayTOs(attrs, rs);
        
        log.exit();
    }
    
    /**
     * Return the {@code Attribute}s of a DAO corresponding to the attributes requested 
     * in the request parameters of the query. 
     * 
     * @param rqParams  A {@code RequestParameters} holding parameters of a query to the webapp.
     * @param attrType  A {@code Class} defining the type of {@code Attribute}s needed to be retrieved.
     * @return          A {@code List} of {@code Attribute}s of type {@code attrType}.
     */
    private static <T extends Enum<T> & DAO.Attribute> List<T> getAttributes(RequestParameters rqParams, 
            Class<T> attrType) {
        log.entry(rqParams, attrType);
        
        List<String> requestedAttrs = rqParams.getValues(
                rqParams.getUrlParametersInstance().getParamAttributeList());
        if (requestedAttrs == null || requestedAttrs.isEmpty()) {
            return log.exit(Arrays.asList(attrType.getEnumConstants()));
        }
        return log.exit(requestedAttrs.stream().map(rqAttr -> Enum.valueOf(attrType, rqAttr))
                .collect(Collectors.toList()));
    }
}
