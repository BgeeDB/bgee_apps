package org.bgee.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.view.AboutDisplay;
import org.bgee.view.PublicationDisplay;
import org.bgee.view.ViewFactory;

/**
 * Controller that handles requests having the category "publication", i.e. with the parameter
 * page=publication
 *
 * @author  Julien Wollbrett
 * @version Bgee 14, July 2020
 * @since   Bgee 14, July 2020
 */

public class CommandPublication extends CommandParent{

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(CommandPublication.class.getName());

    /**
     * Default constructor.
     *
     * @param response          A {@code HttpServletResponse} that will be used to display the 
     *                          page to the client
     * @param requestParameters The {@code RequestParameters} that handles the parameters of the 
     *                          current request.
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     * @param viewFactory       A {@code ViewFactory} that provides the display type to be used.
     */
    public CommandPublication(HttpServletResponse response, RequestParameters requestParameters, 
            BgeeProperties prop, ViewFactory viewFactory) {
        super(response, requestParameters, prop, viewFactory);
    }

    @Override
    public void processRequest() throws IOException, PageNotFoundException {
        log.entry();
        
        PublicationDisplay display = this.viewFactory.getPublicationDisplay();
        
        if (this.requestParameters.getAction() == null) {
            display.displayPublications();

        } else {
            throw log.throwing(new PageNotFoundException("Incorrect " + 
                this.requestParameters.getUrlParametersInstance().getParamAction() + 
                " parameter value."));
        }
        
        log.traceExit();
    }
}
