package org.bgee.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.view.CollaborationDisplay;
import org.bgee.view.ViewFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Controller that handles requests having the category "collaborations",
 * i.e. with the parameter page=collaborations.
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 14, May 2019
 * @since   Bgee 14, Apr. 2019
 */
public class CommandCollaborations extends CommandParent {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(CommandCollaborations.class.getName());

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
    public CommandCollaborations(HttpServletResponse response, RequestParameters requestParameters,
                                 BgeeProperties prop, ViewFactory viewFactory) {
        super(response, requestParameters, prop, viewFactory);
    }

    @Override
    public void processRequest() throws IOException, PageNotFoundException {
        log.entry();

        CollaborationDisplay display = this.viewFactory.getCollaborationDisplay();
        
        if (this.requestParameters.getAction() == null) {
            display.displayCollaborationPage();
            
        } else {
            throw log.throwing(new PageNotFoundException("Incorrect " + 
                this.requestParameters.getUrlParametersInstance().getParamAction() + 
                " parameter value."));
        }
        
        log.traceExit();
    }
}
