package org.bgee.view.html;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.ErrorDisplay;

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
        log.entry();
        this.sendServiceUnavailableHeaders();

        this.startDisplay("Service unavailable for maintenance");

        this.writeln("<p class='alert'>Due to technical problems, Bgee is currently unavailable. " +
                "We are working to restore Bgee as soon as possible. " +
                "We apologize for any inconvenience.</p>");

        this.endDisplay();
        log.exit();
    }

    @Override
    public void displayRequestParametersNotFound(String key) {
        log.entry(key);
        this.sendBadRequestHeaders();
        this.startDisplay("Request parameters not found");
        this.writeln("<p class='alert'>Woops, something wrong happened</p>");
        this.writeln("<p>You tried to use in your query some parameters supposed to be stored on our server, " +
                "but we could not find them. Either the key you used was wrong, " +
                "or we were not able to save these parameters. " +
                "Your query should be rebuilt by setting all the parameters from scratch. " +
                "We apologize for any inconvenience.</p>");
        this.endDisplay();
        log.exit();
    }

    @Override
    public void displayPageNotFound(String message) {
        log.entry(message);
        this.sendPageNotFoundHeaders();
        this.startDisplay("404 not found");
        this.writeln("<p class='alert'>Woops, something wrong happened</p>");
        this.writeln("<p>404 not found. We could not understand your query, see details below:</p> " +
                "<p>" + htmlEntities(message) + "</p>");
        this.endDisplay();
        log.exit();
    }

    @Override
    public void displayUnexpectedError() {
        log.entry();
        this.sendInternalErrorHeaders();
        this.startDisplay("500 internal server error");
        this.writeln("<p class='alert'>Woops, something wrong happened</p>");
        this.writeln("<p>500 internal server error. " +
                "An error occurred on our side. This error was logged and will be investigated. " +
                "We apologize for any inconvenience.</p>");
        this.endDisplay();
        log.exit();
    }

    @Override
    public void displayMultipleParametersNotAllowed(String message) {
        log.entry(message);
        this.sendBadRequestHeaders();
        this.startDisplay("Multiple values not allowed");
        this.writeln("<p class='alert'>Woops, something wrong happened</p>");
        this.writeln("<p>"+ message
                + "</p>"
                + "Please check the URL and retry.</p>");
        this.endDisplay();  
        log.exit();
    }

    @Override
    public void displayRequestParametersNotStorable(String message) {
        log.entry(message);
        this.sendBadRequestHeaders();
        this.startDisplay("A parameter is not storable or the key is missing");
        this.writeln("<p class='alert'>Woops, something wrong happened</p>");
        this.writeln("<p>"+ message
                + "</p>");
        this.endDisplay();
        log.exit();
    }

    @Override
    public void displayWrongFormat(String message) {
        log.entry(message);
        this.sendBadRequestHeaders();
        this.startDisplay("Wrong format for a parameter");
        this.writeln("<p class='alert'>Woops, something wrong happened</p>");
        this.writeln("<p>"+ message
                + "</p>"
                + "Please check the URL and retry.</p>");
        this.endDisplay();  
        log.exit();
    }

    @Override
    public void displayUnsupportedOperationException(String message) {
        log.entry(message);
        this.sendBadRequestHeaders();
        this.startDisplay("Wrong format for a parameter");
        this.writeln("<p class='alert'>Woops, something wrong happened</p>");
        this.writeln("<p>The following operation is not supported: "+ message
                + "</p>");
        this.endDisplay();  
        log.exit();
    }

    @Override
    protected void includeCss() {
        log.entry();
        super.includeCss();
        this.includeCss("general.css");
        log.exit();
    }
}
