package org.bgee.controller;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.view.TopAnatDisplay;
import org.bgee.view.ViewFactory;

/**
 * Controller handling requests relative to topAnat.
 * 
 * @author  Frederic Bastian
 * @version Bgee 13 Jul 2015
 * @since   Bgee 13
 */
public class CommandTopAnat extends CommandParent {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(CommandTopAnat.class.getName());
    
    /**
     * Constructor providing necessary dependencies. 
     * 
     * @param response          A {@code HttpServletResponse} that will be used 
     *                          to display the page to the client
     * @param requestParameters The {@code RequestParameters} that handles 
     *                          the parameters of the current request.
     * @param prop              A {@code BgeeProperties} instance that contains 
     *                          the properties to use.
     * @param viewFactory       A {@code ViewFactory} providing the views of the appropriate 
     *                          display type.
     */
    public CommandTopAnat(HttpServletResponse response,
            RequestParameters requestParameters, BgeeProperties prop,
            ViewFactory viewFactory) {
        super(response, requestParameters, prop, viewFactory);
    }

    @Override
    public void processRequest() throws Exception {
        log.entry();
        
        TopAnatDisplay display = this.viewFactory.getTopAnatDisplay();
        
        // AJAX gene list upload 
        if (this.requestParameters.isATopAnatGeneListUpload()) {
            // Get params:
            // - submitted gene IDs (either from file upload, or from copy/paste in textarea)

            // Request server and get response:
            // - detected species, along with associated gene count. 
            // - selected species (species with most valid genes).
            // - valid stages for selected species
            // - hash to retrieve gene list needs to be included in the response
            //   (hash to be used when submitting job)

            // Display page (using previous response)
            display.displayTopAnatHomePage();
            
        // Job submission, response 1: job not started
        } else if (this.requestParameters.isATopAnatNewJob()) {
            // Create or get param (auto-generated client-side? Produced server-side?) 
            // - the ID to track job
            
            // Get params
            // - data parameters hash obtained from the gene upload request
            // - all form parameters

            // Request server and get response
            // - "admin" URL, allowing to come back at any moment to track job advancement
            // - advancement status (could be hardcoded, e.g., "starting job")

            // Display page (using previous response)
            display.displayTopAnatWaitingPage();

        // Job submission: job tracking
        } else if (this.requestParameters.isATopAnatTrackingJob()) {
            // Get params
            // - ID to track job
            
            // Request server and get response
            // - advancement status (real one, based on TaskManager)

            // Display page (using previous response)
            display.displayTopAnatWaitingPage();

        // Job submission, response 2: job completed
        } else if (this.requestParameters.isATopAnatCompletedJob()) {
            // Get params
            // - ID to track job

            // Request server and get response
            // - response saying to redirect to a provided URL 
            //   (is it doable or do we get the results through an AJAX query only?)
            
            // Display page (using previous response)
            display.displayTopAnatResultPage();

        // Home page, with data parameters provided in URL
        } else if (this.requestParameters.isATopAnatHomePageWithData()) {
            // Get params:
            // - data parameters
            
            // Request server and get response
            // - information to pre-fill the form
            // - information to display results 
            //   (is it doable or do we get the results through an AJAX query only?)
            boolean hasResults = true;
            
            // Display page (using previous response)
            if (hasResults) {
                display.displayTopAnatResultPage();
            } else {
                display.displayTopAnatHomePage();
            }

        // Home page, empty
        } else if (this.requestParameters.getAction() == null) {
            // Display page
            display.displayTopAnatHomePage();
            
        } else {
            throw log.throwing(new PageNotFoundException("Incorrect " 
                + this.requestParameters.getUrlParametersInstance().getParamAction() 
                + " parameter value."));
        }

        log.exit();
    }
}
