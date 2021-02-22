package org.bgee.view.xml;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.controller.exception.*;
import org.bgee.model.job.exception.TooManyJobsException;
import org.bgee.view.ErrorDisplay;
import org.bgee.view.ViewFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * {@code ErrorDisplay} returning objects generating XML views.
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Mar. 2019
 * @since   Bgee 14, Jan. 2019
 */
public class XmlErrorDisplay extends XmlParentDisplay implements ErrorDisplay {

    private final static Logger log = LogManager.getLogger(XmlErrorDisplay.class.getName());

    /**
     * Constructor providing the necessary dependencies. 
     *
     * @param response          A {@code HttpServletResponse} that will be used to display the 
     *                          page to the client
     * @param requestParameters The {@code RequestParameters} that handles the parameters of the 
     *                          current request.
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     * @param factory           The {@code XmlFactory} that instantiated this object.
     * @throws IOException      If there is an issue when trying to get or to use the
     *                          {@code PrintWriter} 
     */
    protected XmlErrorDisplay(HttpServletResponse response, RequestParameters requestParameters,
                              BgeeProperties prop, ViewFactory factory) throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, factory);
    }

    @Override
    public void displayServiceUnavailable() {
        log.traceEntry();

        this.sendServiceUnavailableHeaders();

        displayError("Service unavailable for maintenance</title>", 
                "Due to technical problems, Bgee is currently unavailable. " +
                "We are working to restore Bgee as soon as possible. " +
                "We apologize for any inconvenience.");

        log.traceExit();
    }
    
    @Override
    public void displayControllerException(InvalidFormatException e) {
        log.entry(e);

        this.sendBadRequestHeaders();

        this.displayError("Invalid request!", 
                "One of the request parameters has an incorrect format. " +
                "Incorrect parameter: " + e.getURLParameter().getName());

        log.traceExit();
    }

    @Override
    public void displayControllerException(InvalidRequestException e) {
        log.entry(e);

        this.sendBadRequestHeaders();

        this.displayError("Invalid request!", e.getMessage());

        log.traceExit();
    }

    @Override
    public void displayControllerException(MultipleValuesNotAllowedException e) {
        log.entry(e);

        this.sendBadRequestHeaders();

        this.displayError("Invalid request!", 
                "One of the request parameters was incorrectly assigned multiple values. " +
                "Incorrect parameter: " + e.getURLParameter().getName());

        log.traceExit();
    }

    @Override
    public void displayControllerException(RequestSizeExceededException e) {
        log.entry(e);

        this.sendBadRequestHeaders();

        this.displayError("Invalid request!", "Request maximum size exceeded.");

        log.traceExit();
    }

    @Override
    public void displayControllerException(ValueSizeExceededException e) {
        log.entry(e);

        this.sendBadRequestHeaders();

        this.displayError("Invalid request!", 
                "One of the request parameters exceeded its maximum allowed length. " +
                "Incorrect parameter: " + e.getURLParameter().getName());

        log.traceExit();
    }

    @Override
    public void displayControllerException(PageNotFoundException e) {
        log.entry(e);

        this.sendPageNotFoundHeaders();

        this.displayError("404 not found", "Something wrong happened! We could not understand your query.");

        log.traceExit();
    }

    @Override
    public void displayControllerException(RequestParametersNotFoundException e) {
        log.entry(e);

        this.sendBadRequestHeaders();

        this.displayError("Invalid request!",
                "Something wrong happened! " +
                "You tried to use in your query some parameters supposed to be stored " +
                "on our server, but we could not find them. Either the key you used was wrong, " +
                "or we were not able to save these parameters. Your query should be rebuilt " +
                "by setting all the parameters again. We apologize for any inconvenience. " +
                "Invalid key: " + e.getKey());

        log.traceExit();
    }

    @Override
    public void displayControllerException(RequestParametersNotStorableException e) {
        log.entry(e);

        this.sendInternalErrorHeaders();

        this.displayError("500 internal server error", 
                "Something wrong happened! " +
                "We could not store your parameters, or a key could not be generated to retrieve them. " +
                "We apologize for any inconvenience.");

        log.traceExit();
    }

    @Override
    public void displayControllerException(TooManyJobsException e) {
        log.entry(e);

        this.sendTooManyRequeststHeaders();

        this.displayError("Too many jobs!", "Too Many Requests - " + e.getMessage());

        log.traceExit();
    }

    @Override
    public void displayUnsupportedOperationException() {
        log.traceEntry();

        this.sendBadRequestHeaders();

        this.displayError("Invalid request!",
                "Something wrong happened! This operation is not supported " +
                "for the requested view or the requested parameters.");

        log.traceExit();
    }

    @Override
    public void displayUnexpectedError() {
        log.traceEntry();
        
        this.sendInternalErrorHeaders();
        
        this.displayError("500 internal server error", 
                "Woops, something wrong happened. " +
                "An error occurred on our side. This error was logged and will be investigated. " +
                "We apologize for any inconvenience.");
        
        log.traceExit();
    }

    private void displayError(String title, String message) {
        log.entry(title, message);

        this.startDisplay();

        this.writeln("<error>");
        this.writeln("<title>" + title + "</title>");
        this.writeln("<message>" + message + "</message>");
        this.writeln("</error>");

        this.endDisplay();

        log.traceExit();
    }
}
