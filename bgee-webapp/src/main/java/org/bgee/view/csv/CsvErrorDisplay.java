package org.bgee.view.csv;

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
import org.bgee.view.ViewFactory;

public class CsvErrorDisplay extends CsvParentDisplay implements ErrorDisplay {
    private final static Logger log = LogManager.getLogger(CsvErrorDisplay.class.getName());

    protected CsvErrorDisplay(HttpServletResponse response, RequestParameters requestParameters, 
            BgeeProperties prop, ViewFactory factory, Delimiter delimiter) 
                    throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, factory, delimiter);
    }

    @Override
    public void displayServiceUnavailable() {
        log.entry();
        
        this.sendServiceUnavailableHeaders();

        this.startDisplay();
        this.displayErrorMessage("Due to technical problems, Bgee is currently unavailable. " +
                "We are working to restore Bgee as soon as possible. " +
                "We apologize for any inconvenience.");
        this.endDisplay();
        
        log.exit();
    }

    @Override
    public void displayUnexpectedError() {
        log.entry();
        
        this.sendInternalErrorHeaders();
        
        this.startDisplay();
        this.displayErrorMessage("500 internal server error. " +
                "An error occurred on our side. This error was logged and will be investigated. " +
                "We apologize for any inconvenience.");
        this.endDisplay();
        
        log.exit();
    }

    @Override
    public void displayControllerException(InvalidFormatException e) {
        log.entry(e);

        this.sendBadRequestHeaders();

        this.startDisplay();
        this.displayErrorMessage("One of the request parameters has an incorrect format. "
                + "Incorrect parameter: " + e.getURLParameter().getName());
        this.endDisplay();
        
        log.exit();
    }

    @Override
    public void displayControllerException(InvalidRequestException e) {
        log.entry(e);

        this.sendBadRequestHeaders();

        this.startDisplay();
        this.displayErrorMessage(e.getMessage());
        this.endDisplay();
        
        log.exit();
    }

    @Override
    public void displayControllerException(MultipleValuesNotAllowedException e) {
        log.entry(e);

        this.sendBadRequestHeaders();

        this.startDisplay();
        this.displayErrorMessage("One of the request parameters was incorrectly assigned "
                + "multiple values. Incorrect parameter: " + e.getURLParameter().getName());
        this.endDisplay();
        
        log.exit();
    }

    @Override
    public void displayControllerException(RequestSizeExceededException e) {
        log.entry(e);

        this.sendBadRequestHeaders();

        this.startDisplay();
        this.displayErrorMessage("Request maximum size exceeded.");
        this.endDisplay();
        
        log.exit();
    }

    @Override
    public void displayControllerException(ValueSizeExceededException e) {
        log.entry(e);

        this.sendBadRequestHeaders();

        this.startDisplay();
        this.displayErrorMessage("One of the request parameters exceeded "
                + "its maximum allowed length. Incorrect parameter: " + e.getURLParameter().getName());
        this.endDisplay();
        
        log.exit();
    }

    @Override
    public void displayControllerException(PageNotFoundException e) {
        log.entry(e);
        
        this.sendPageNotFoundHeaders();
        
        this.startDisplay();
        this.displayErrorMessage("404 not found. We could not understand your query.");
        this.endDisplay();
        
        log.exit();
    }

    @Override
    public void displayControllerException(RequestParametersNotFoundException e) {
        log.entry(e);
        
        this.sendBadRequestHeaders();
        
        this.startDisplay();
        this.displayErrorMessage("You tried to use in your query some parameters supposed to be stored "
                + "on our server, but we could not find them. Either the key you used was wrong, "
                + "or we were not able to save these parameters. Your query should be rebuilt "
                + "by setting all the parameters again. We apologize for any inconvenience. "
                + "Invalid key: " + e.getKey());
        this.endDisplay();
        
        log.exit();
    }

    @Override
    public void displayControllerException(RequestParametersNotStorableException e) {
        log.entry(e);
        
        this.sendInternalErrorHeaders();
        
        this.startDisplay();
        this.displayErrorMessage("We could not store your parameters, "
                + "or a key could not be generated to retrieve them. "
                + "We apologize for any inconvenience.</p>");
        this.endDisplay();
        
        log.exit();
    }

    @Override
    public void displayUnsupportedOperationException() {
        log.entry();
        
        this.sendBadRequestHeaders();
        
        this.startDisplay();
        this.displayErrorMessage("This operation is not supported "
                + "for the requesed view or the requested parameters.");
        this.endDisplay();
        
        log.exit();
    }

    @Override
    public void displayControllerException(TooManyJobsException e) {
        log.entry();
        
        this.sendTooManyRequeststHeaders();
        
        this.startDisplay();
        this.displayErrorMessage("Too Many Requests - " + e.getMessage());
        this.endDisplay();
        
        log.exit();
    }
    
    /**
     * Format an error message before displaying it. Notably, all CSV error messages 
     * have to start with "Query error: ".
     * 
     * @param msg   A {@code String} that is the error message to display.
     */
    private void displayErrorMessage(String msg) {
        log.entry(msg);
        this.writeln("Query error: " + msg);
        log.exit();
    }
}
