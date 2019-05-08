package org.bgee.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.model.ServiceFactory;
import org.bgee.view.ResourceDisplay;
import org.bgee.view.ViewFactory;

/**
 * Controller that handles requests having the category "resources", i.e. with the parameter
 * page=resources
 * 
 * @author  Julien Wollbrett
 * @version Bgee 14, May 2019
 * @since   Bgee 14, May 2019
 **/

public class CommandResource extends CommandParent {
    
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(CommandResource.class.getName());

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
    public CommandResource (HttpServletResponse response, RequestParameters requestParameters, 
            BgeeProperties prop, ViewFactory viewFactory, ServiceFactory serviceFactory) {
        super(response, requestParameters, prop, viewFactory, serviceFactory);
    }
    
    @Override
    public void processRequest() throws IllegalStateException, IOException, PageNotFoundException {
        log.entry();

        ResourceDisplay display = this.viewFactory.getResourceDisplay();
        if (this.requestParameters.getAction() != null &&
                this.requestParameters.getAction().equals(RequestParameters.ACTION_R_PACKAGES)) {

            display.displayRPackages();

        } if (this.requestParameters.getAction() != null && 
                this.requestParameters.getAction().equals(RequestParameters.ACTION_ANNOTATIONS)) {

            display.displayAnnotations();

        } if (this.requestParameters.getAction() != null &&
                this.requestParameters.getAction().equals(RequestParameters.ACTION_ONTOLOGIES)) {

            display.displayOntologies();

        } if (this.requestParameters.getAction() != null &&
                this.requestParameters.getAction().equals(RequestParameters.ACTION_SOURCE_CODE)) {

            display.displaySourceCode();

        } else {
            throw log.throwing(new PageNotFoundException("Incorrect " + 
                this.requestParameters.getUrlParametersInstance().getParamAction() + 
                " parameter value."));
        }
        
        log.exit();
    }

}
