package org.bgee.view.json;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.ErrorDisplay;

public class JsonErrorDisplay extends JsonParentDisplay implements ErrorDisplay {
    
    private final static Logger log = LogManager.getLogger(JsonErrorDisplay.class.getName());

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
    public JsonErrorDisplay(HttpServletResponse response, RequestParameters requestParameters,
            BgeeProperties prop, JsonFactory factory) throws IOException {
        super(response, requestParameters, prop, factory);
    }

    @Override
    public void displayServiceUnavailable() {
        log.entry();
        this.sendServiceUnavailableHeaders();

        this.write(
                "{\"error\": {"
                    + "\"code\": 503, "
                    + "\"message\": \"Due to technical problems, Bgee is currently unavailable. " +
                    "We are working to restore Bgee as soon as possible. " +
                    "We apologize for any inconvenience.\""
                + "}}");

        log.exit();
    }

    @Override
    public void displayRequestParametersNotFound(String key) {
        log.entry(key);
        this.sendBadRequestHeaders();

        this.write(
                "{\"error\": {"
                    + "\"code\": 400, "
                    + "\"message\": \"You tried to use in your query some parameters "
                    + "supposed to be stored on our server, " +
                    "but we could not find them. Either the key you used was wrong, " +
                    "or we were not able to save these parameters. " +
                    "Your query should be rebuilt by setting all the parameters from scratch. " +
                    "We apologize for any inconvenience.\""
                + "}}");
        
        log.exit();
    }

    @Override
    public void displayPageNotFound(String message) {
        log.entry(message);
        this.sendPageNotFoundHeaders();

        this.write(
                "{\"error\": {"
                    + "\"code\": 404, "
                    + "\"message\": \"We could not understand your query, "
                    + "see details : " + message + "\""
                + "}}");
        
        log.exit();
    }

    @Override
    public void displayUnexpectedError() {
        log.entry();
        this.sendInternalErrorHeaders();

        this.write(
                "{\"error\": {"
                    + "\"code\": 500, "
                    + "\"message\": \"An error occurred on our side. This error was logged "
                    + "and will be investigated. We apologize for any inconvenience.\""
                + "}}");
        
        log.exit();
    }

    @Override
    public void displayMultipleParametersNotAllowed(String message) {
        log.entry(message);
        this.sendBadRequestHeaders();
        
        this.write(
                "{\"error\": {"
                    + "\"code\": 400, "
                    + "\"message\": \"" + message + " Please check the URL and retry.\""
                + "}}");
        
        log.exit();
    }

    @Override
    public void displayRequestParametersNotStorable(String message) {
        log.entry(message);
        this.sendBadRequestHeaders();
        
        this.write(
                "{\"error\": {"
                    + "\"code\": 400, "
                    + "\"message\": \"A parameter is not storable or the key is missing: "
                    + message + "\""
                + "}}");
        
        log.exit();
    }

    @Override
    public void displayWrongFormat(String message) {
        log.entry(message);
        this.sendBadRequestHeaders();
        
        this.write(
                "{\"error\": {"
                    + "\"code\": 400, "
                    + "\"message\": \"Wrong format for a parameter: "
                    + message + "\""
                + "}}");
        
        log.exit();
    }

    @Override
    public void displayUnsupportedOperationException(String message) {
        log.entry(message);
        this.sendBadRequestHeaders();
        
        this.write(
                "{\"error\": {"
                    + "\"code\": 400, "
                    + "\"message\": \"The following operation is not supported: "
                    + message + "\""
                + "}}");
        
        log.exit();
    }
    
}
