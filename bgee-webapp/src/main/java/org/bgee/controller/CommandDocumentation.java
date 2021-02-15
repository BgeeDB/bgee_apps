package org.bgee.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.view.DocumentationDisplay;
import org.bgee.view.ViewFactory;


/**
 * Controller that handles requests having the category "documentation",
 * i.e. with the parameter page=doc
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Aug. 2018
 * @since   Bgee 13, Mar. 2015
 */
public class CommandDocumentation extends CommandParent {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(CommandDocumentation.class.getName());

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
    public CommandDocumentation(HttpServletResponse response, RequestParameters requestParameters, 
            BgeeProperties prop, ViewFactory viewFactory) {
        super(response, requestParameters, prop, viewFactory);
    }

    @Override
    public void processRequest() throws IOException, PageNotFoundException {
        log.entry();
        
        DocumentationDisplay display = this.viewFactory.getDocumentationDisplay();
        
        if (this.requestParameters.getAction() == null) {
            display.displayDocumentationHomePage();
        } else if (this.requestParameters.getAction().equals(
                RequestParameters.ACTION_DOC_CALL_DOWLOAD_FILES)) {
            display.displayCallDownloadFileDocumentation();
        } else if (this.requestParameters.getAction().equals(
                RequestParameters.ACTION_DOC_PROC_EXPR_VALUE_DOWLOAD_FILES)) {
            display.displayRefExprDownloadFileDocumentation();
        } else if (this.requestParameters.getAction().equals(RequestParameters.ACTION_DOC_TOP_ANAT)) {
            display.displayTopAnatDocumentation();
        } else if (this.requestParameters.getAction().equals(RequestParameters.ACTION_DOC_DATA_SETS)) {
            display.displayDataSets();
        } else if (this.requestParameters.getAction().equals(RequestParameters.ACTION_DOC_FAQ)) {
            display.displayFaq();
        } else {
            throw log.throwing(new PageNotFoundException("Incorrect " + 
                this.requestParameters.getUrlParametersInstance().getParamAction() + 
                " parameter value."));
        }
        
        log.traceExit();
    }
}
