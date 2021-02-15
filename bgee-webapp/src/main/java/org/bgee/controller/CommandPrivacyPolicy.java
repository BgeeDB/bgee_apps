package org.bgee.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.view.PrivacyPolicyDisplay;
import org.bgee.view.ViewFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Controller that handles requests having the category "privacy_policy",
 * i.e. with the parameter page=privacy_policy.
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Aug. 2018
 * @since   Bgee 14, Aug. 2018
 */
public class CommandPrivacyPolicy extends CommandParent {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(CommandPrivacyPolicy.class.getName());

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
    public CommandPrivacyPolicy(HttpServletResponse response, RequestParameters requestParameters,
                                BgeeProperties prop, ViewFactory viewFactory) {
        super(response, requestParameters, prop, viewFactory);
    }

    @Override
    public void processRequest() throws IOException, PageNotFoundException {
        log.entry();

        PrivacyPolicyDisplay display = this.viewFactory.getPrivacyPolicyDisplay();
        
        if (this.requestParameters.getAction() == null) {
            display.displayPrivacyPolicyPage();
            
        } else {
            throw log.throwing(new PageNotFoundException("Incorrect " + 
                this.requestParameters.getUrlParametersInstance().getParamAction() + 
                " parameter value."));
        }
        
        log.traceExit();
    }
}
