package org.bgee.controller;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.model.ServiceFactory;
import org.bgee.model.source.Source;
import org.bgee.view.SourceDisplay;
import org.bgee.view.ViewFactory;

/**
 * Controller that handles requests having the category "source",
 * i.e. with the parameter page=source
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 13, July 2016
 * @since   Bgee 13, Mar. 2016
 */
public class CommandSource extends CommandParent {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(CommandSource.class.getName());

    /**
     * Default constructor.
     *
     * @param response          A {@code HttpServletResponse} that will be used to display the 
     *                          page to the client
     * @param requestParameters The {@code RequestParameters} that handles the parameters of the 
     *                          current request.
     * @param prop              A {@code BgeeProperties} instance that contains the properties to use.
     * @param viewFactory       A {@code ViewFactory} that provides the display type to be used.
     */
    public CommandSource(HttpServletResponse response, RequestParameters requestParameters, 
            BgeeProperties prop, ViewFactory viewFactory, ServiceFactory serviceFactory) {
        super(response, requestParameters, prop, viewFactory, serviceFactory);
    }

    @Override
    public void processRequest() throws Exception {
        log.traceEntry();
        
        SourceDisplay display = this.viewFactory.getSourceDisplay();

        if (this.requestParameters.getAction() == null) {
            
            List<Source> sources = serviceFactory.getSourceService().loadDisplayableSources(false);
            
            display.displaySources(sources);
            
        } else {
            throw log.throwing(new PageNotFoundException("Incorrect " + 
                this.requestParameters.getUrlParametersInstance().getParamAction() + 
                " parameter value."));
        }
        log.traceExit();
    }
}
