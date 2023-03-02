package org.bgee.view.html;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.controller.exception.InvalidFormatException;
import org.bgee.controller.exception.InvalidRequestException;
import org.bgee.controller.exception.MultipleValuesNotAllowedException;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.controller.exception.RequestParametersNotFoundException;
import org.bgee.controller.exception.RequestParametersNotStorableException;
import org.bgee.controller.exception.RequestSizeExceededException;
import org.bgee.controller.exception.ValueSizeExceededException;
import org.bgee.model.job.exception.TooManyJobsException;
import org.bgee.view.ErrorDisplay;

/**
 * This class is the HTML implementation of the {@code ErrorDisplay}.
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Mar. 2019
 * @since   Bgee 14, July 2015
 */
public class HtmlErrorDisplay extends HtmlParentDisplay implements ErrorDisplay {
    
    private final static Logger log = LogManager.getLogger(HtmlErrorDisplay.class.getName());

    /**
     * Constructor providing the necessary dependencies. 
     * 
     * @param response          A {@code HttpServletResponse} that will be used to display the 
     *                          page to the client
     * @param requestParameters The {@code RequestParameters} that handles the parameters of the 
     *                          current request.
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     * @param factory           The {@code HtmlFactory} that instantiated this object.
     * @throws IOException      If there is an issue when trying to get or to use the
     *                          {@code PrintWriter} 
     */
    public HtmlErrorDisplay(HttpServletResponse response, RequestParameters requestParameters,
            BgeeProperties prop, HtmlFactory factory) throws IOException {
        super(response, requestParameters, prop, factory);
    }

    @Override
    public void displayServiceUnavailable() {
        log.traceEntry();
        this.sendServiceUnavailableHeaders();

        this.startDisplay("Service unavailable for maintenance");

        this.writeln("<p class='alert'>Due to technical problems, Bgee is currently unavailable.</p> " +
                "<p>We are working to restore Bgee as soon as possible. " +
                "We apologize for any inconvenience.</p>");

        this.endDisplay();
        log.traceExit();
    }

    @Override
    public void displayUnexpectedError() {
        log.traceEntry();
        this.sendInternalErrorHeaders();
        this.startDisplay("500 internal server error");
        this.writeln("<p class='alert'>Woops, something wrong happened.</p>");
        this.writeln("<p>500 internal server error. " +
                "An error occurred on our side. This error was logged and will be investigated. " +
                "We apologize for any inconvenience.</p>");
        this.endDisplay();
        log.traceExit();
    }

    @Override
    public void displayControllerException(InvalidFormatException e) {
        log.traceEntry("{}",e);

        this.sendBadRequestHeaders();

        this.startDisplay("Invalid request!");

        this.writeln("<p class='alert'>One of the request parameters has an incorrect format.</p>");
        this.writeln("<p>Incorrect parameter: " + htmlEntities(e.getURLParameter().getName()) + "</p>");

        this.endDisplay();
        log.traceExit();
    }

    @Override
    public void displayControllerException(InvalidRequestException e) {
        log.traceEntry("{}",e);

        this.sendBadRequestHeaders();

        this.startDisplay("Invalid request!");

        this.writeln("<p class='alert'>" + htmlEntities(e.getMessage()) + "</p>");

        this.endDisplay();
        log.traceExit();
    }

    @Override
    public void displayControllerException(MultipleValuesNotAllowedException e) {
        log.traceEntry("{}",e);

        this.sendBadRequestHeaders();

        this.startDisplay("Invalid request!");

        this.writeln("<p class='alert'>One of the request parameters was incorrectly assigned "
                + "multiple values.</p>");
        this.writeln("<p>Incorrect parameter: " + htmlEntities(e.getURLParameter().getName()) + "</p>");

        this.endDisplay();
        log.traceExit();
    }

    @Override
    public void displayControllerException(RequestSizeExceededException e) {
        log.traceEntry("{}",e);

        this.sendBadRequestHeaders();

        this.startDisplay("Invalid request!");

        this.writeln("<p class='alert'>Request maximum size exceeded.</p>");

        this.endDisplay();
        log.traceExit();
    }

    @Override
    public void displayControllerException(ValueSizeExceededException e) {
        log.traceEntry("{}",e);

        this.sendBadRequestHeaders();

        this.startDisplay("Invalid request!");

        this.writeln("<p class='alert'>One of the request parameters exceeded "
                + "its maximum allowed length.</p>");
        this.writeln("<p>Incorrect parameter: " + htmlEntities(e.getURLParameter().getName()) + "</p>");

        this.endDisplay();
        log.traceExit();
    }

    @Override
    public void displayControllerException(PageNotFoundException e) {
        log.traceEntry("{}",e);
        
        this.sendPageNotFoundHeaders();
        this.startDisplay("404 not found");
        this.writeln("<p class='alert'>Something wrong happened!</p>");
        this.writeln("<p>404 not found. We could not understand your query.</p> ");

        this.endDisplay();
        log.traceExit();
    }

    @Override
    public void displayControllerException(RequestParametersNotFoundException e) {
        log.traceEntry("{}",e);
        
        this.sendBadRequestHeaders();
        this.startDisplay("Invalid request!");
        this.writeln("<p class='alert'>Something wrong happened!</p>");
        this.writeln("<p>You tried to use in your query some parameters supposed to be stored "
                + "on our server, but we could not find them. Either the key you used was wrong, "
                + "or we were not able to save these parameters. Your query should be rebuilt "
                + "by setting all the parameters again. We apologize for any inconvenience.</p>");
        this.writeln("<p>Invalid key: " + htmlEntities(e.getKey()) + "</p>");

        this.endDisplay();
        log.traceExit();
    }

    @Override
    public void displayControllerException(RequestParametersNotStorableException e) {
        log.traceEntry("{}",e);
        
        this.sendInternalErrorHeaders();
        this.startDisplay("500 internal server error");
        this.writeln("<p class='alert'>Something wrong happened!</p>");
        this.writeln("<p>We could not store your parameters, or a key could not be generated to retrieve them. "
                + "We apologize for any inconvenience.</p>");

        this.endDisplay();
        log.traceExit();
    }

    @Override
    public void displayUnsupportedOperationException() {
        log.traceEntry();
        
        this.sendBadRequestHeaders();
        this.startDisplay("Invalid request!");
        this.writeln("<p class='alert'>Something wrong happened!</p>");
        this.writeln("<p>This operation is not supported "
                + "for the requested view or the requested parameters.</p>");

        this.endDisplay();
        log.traceExit();
    }

    @Override
    public void displayControllerException(TooManyJobsException e) {
        log.traceEntry("{}",e);

        this.sendTooManyRequeststHeaders();

        this.startDisplay("Too many jobs!");
        this.writeln("<p class='alert'>Too Many Requests - " + htmlEntities(e.getMessage()) + "</p>");
        this.endDisplay();
        
        log.traceExit();
    }

    @Override
    protected void includeCss() {
        log.traceEntry();
        super.includeCss();
        //If you ever add new files, you need to edit bgee-webapp/pom.xml 
        //to correctly merge/minify them.
        this.includeCss("general.css");
        log.traceExit();
    }
}
