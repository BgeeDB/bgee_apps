package org.bgee.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.model.ServiceFactory;
import org.bgee.view.SparqlDisplay;
import org.bgee.view.ViewFactory;

/**
 * Controller that handles requests having the category "sparql", i.e. with the parameter
 * page=sparql
 * 
 * @author  Julien Wollbrett
 * @version Bgee 14, May 2019
 * @since   Bgee 14, May 2019
 */
public class CommandSparql extends CommandParent {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(CommandSparql.class.getName());

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
    public CommandSparql (HttpServletResponse response, RequestParameters requestParameters, 
            BgeeProperties prop, ViewFactory viewFactory, ServiceFactory serviceFactory) {
        super(response, requestParameters, prop, viewFactory, serviceFactory);
    }

    @Override
    public void processRequest() throws IllegalStateException, IOException, PageNotFoundException {
        log.entry();
        
        SparqlDisplay display = this.viewFactory.getSparqlDisplay();
        if (this.requestParameters.getAction() == null) {

            display.displaySparql();

        } else {
            throw log.throwing(new PageNotFoundException("Incorrect " + 
                    this.requestParameters.getUrlParametersInstance().getParamAction() + 
                    " parameter value."));
        }
        
        log.traceExit();
    }
}
