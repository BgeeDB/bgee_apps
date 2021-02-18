package org.bgee.view.json;

import java.io.IOException;
import java.util.LinkedHashMap;

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
import org.bgee.view.JsonHelper;

public class JsonErrorDisplay extends JsonParentDisplay implements ErrorDisplay {
    
    private final static Logger log = LogManager.getLogger(JsonErrorDisplay.class.getName());
    
    /**
     * A {@code String} that is the key of the parameter for the exception type, 
     * in the JSON responses. 
     */
    private static final String EXCEPTION_TYPE_KEY = "exceptionType";

    /**
     * Constructor providing the necessary dependencies. 
     * 
     * @param response          A {@code HttpServletResponse} that will be used to display the 
     *                          page to the client
     * @param requestParameters The {@code RequestParameters} that handles the parameters of the 
     *                          current request.
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     * @param jsonHelper        A {@code JsonHelper} used to dump variables into Json.
     * @param factory           The {@code HtmlFactory} that instantiated this object.
     * 
     * @throws IllegalArgumentException If {@code factory} or {@code jsonHelper} is {@code null}.
     * @throws IOException      If there is an issue when trying to get or to use the
     *                          {@code PrintWriter} 
     */
    public JsonErrorDisplay(HttpServletResponse response, RequestParameters requestParameters,
            BgeeProperties prop, JsonHelper jsonHelper, JsonFactory factory) 
                    throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, jsonHelper, factory);
    }

    @Override
    public void displayServiceUnavailable() {
        log.traceEntry();

        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put(EXCEPTION_TYPE_KEY, "ServiceUnavailable");
        this.sendResponse(HttpServletResponse.SC_SERVICE_UNAVAILABLE, 
                "Due to technical problems, Bgee is currently unavailable. " 
                 + "Bgee will be restored as soon as possible. "
                 + "We apologize for any inconvenience.", 
                 data);

        log.traceExit();
    }

    @Override
    public void displayUnexpectedError() {
        log.traceEntry();

        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put(EXCEPTION_TYPE_KEY, "UnexpectedError");
        this.sendResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "An error occurred on our side. This error was logged "
                + "and will be investigated. We apologize for any inconvenience.", 
                data);
        
        log.traceExit();
    }

    @Override
    public void displayUnsupportedOperationException() {
        log.traceEntry();

        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put(EXCEPTION_TYPE_KEY, "UnsupportedOperationException");
        this.sendResponse(HttpServletResponse.SC_BAD_REQUEST, "This operation is not supported "
                + "for the requested view or the requested parameters.", data);
        
        log.traceExit();
    }

    @Override
    public void displayControllerException(InvalidFormatException e) {
        log.entry(e);
        
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put(EXCEPTION_TYPE_KEY, e.getClass().getSimpleName());
        data.put("incorrectParameter", e.getURLParameter().getName());
        this.sendResponse(HttpServletResponse.SC_BAD_REQUEST, 
                "One of the request parameters has an incorrect format.", 
                data);
        
        log.traceExit();
    }

    @Override
    public void displayControllerException(InvalidRequestException e) {
        log.entry(e);
        
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put(EXCEPTION_TYPE_KEY, e.getClass().getSimpleName());
        data.putAll(e.getAdditionalData());
        this.sendResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage(), 
                data);
        
        log.traceExit();
    }

    @Override
    public void displayControllerException(MultipleValuesNotAllowedException e) {
        log.entry(e);
        
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put(EXCEPTION_TYPE_KEY, e.getClass().getSimpleName());
        data.put("incorrectParameter", e.getURLParameter().getName());
        this.sendResponse(HttpServletResponse.SC_BAD_REQUEST, 
                "One of the request parameters was incorrectly assigned multiple values.", 
                data);
        
        log.traceExit();
    }

    @Override
    public void displayControllerException(RequestSizeExceededException e) {
        log.entry(e);
        
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put(EXCEPTION_TYPE_KEY, e.getClass().getSimpleName());
        this.sendResponse(HttpServletResponse.SC_BAD_REQUEST, 
                "Request maximum size exceeded.", 
                data);
        
        log.traceExit();
    }

    @Override
    public void displayControllerException(ValueSizeExceededException e) {
        log.entry(e);
        
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put(EXCEPTION_TYPE_KEY, e.getClass().getSimpleName());
        data.put("incorrectParameter", e.getURLParameter().getName());
        this.sendResponse(HttpServletResponse.SC_BAD_REQUEST, 
                "One of the request parameters exceeded its maximum allowed length.", 
                data);
        
        log.traceExit();
    }

    @Override
    public void displayControllerException(PageNotFoundException e) {
        log.entry(e);
        
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put(EXCEPTION_TYPE_KEY, e.getClass().getSimpleName());
        this.sendResponse(HttpServletResponse.SC_NOT_FOUND, "Page not found.", data);
        
        log.traceExit();
    }

    @Override
    public void displayControllerException(RequestParametersNotFoundException e) {
        log.entry(e);
        
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put(EXCEPTION_TYPE_KEY, e.getClass().getSimpleName());
        data.put("invalidKey", e.getKey());
        this.sendResponse(HttpServletResponse.SC_BAD_REQUEST, 
                "You tried to use in your query some parameters supposed to be stored "
                + "on our server, but we could not find them. Either the key you used was wrong, "
                + "or we were not able to save these parameters. Your query should be rebuilt "
                + "by setting all the parameters again. We apologize for any inconvenience.", 
                data);
        
        log.traceExit();
    }

    @Override
    public void displayControllerException(RequestParametersNotStorableException e) {
        log.entry(e);
        
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put(EXCEPTION_TYPE_KEY, e.getClass().getSimpleName());
        this.sendResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "We could not store your parameters, or a key could not be generated to retrieve them. "
                + "We apologize for any inconvenience.", 
                data);
        
        log.traceExit();
    }

    @Override
    public void displayControllerException(TooManyJobsException e) {
        log.entry(e);
        
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put(EXCEPTION_TYPE_KEY, e.getClass().getSimpleName());
        data.put("allowedMaxJobCount", e.getMaxAllowedJobCount());
        //send HTTP status code 429 "Too Many Requests"
        this.sendResponse(429, "Too Many Requests - " + e.getMessage(), data);
        
        log.traceExit();
    }
}
