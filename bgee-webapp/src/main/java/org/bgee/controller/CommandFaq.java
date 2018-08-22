package org.bgee.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.view.FaqDisplay;
import org.bgee.view.ViewFactory;

import javax.servlet.http.HttpServletResponse;

/**
 * Controller that handles requests having the category "faq", i.e. with the parameter
 * page=faq
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Aug. 2018
 * @since   Bgee 14, June 2018
 */
public class CommandFaq extends CommandParent {

    private final static Logger log = LogManager.getLogger(CommandFaq.class.getName());

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
    public CommandFaq(HttpServletResponse response, RequestParameters requestParameters,
                      BgeeProperties prop, ViewFactory viewFactory) {
        super(response, requestParameters, prop, viewFactory);
    }

    @Override
    public void processRequest() throws Exception {
        log.entry();

        FaqDisplay display = this.viewFactory.getFaqDisplay();

        if (requestParameters.isAFaqPageCategory()) {
            display.displayFaqPage();
        } else {
            throw new PageNotFoundException();
        }

        log.exit();
    }
}
