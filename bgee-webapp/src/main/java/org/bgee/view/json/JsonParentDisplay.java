package org.bgee.view.json;

import java.io.IOException;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.ConcreteDisplayParent;
import org.bgee.view.JsonHelper;

/**
 * Super class of JSON views. Subclasses should solely used the methods 
 * {@link #sendResponse(int, String, LinkedHashMap)} and 
 * {@link #sendResponse(String, LinkedHashMap)} to send responses to client.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Nov. 2015
 * @since Bgee 13
 */
public class JsonParentDisplay extends ConcreteDisplayParent {

    private final static Logger log = LogManager.getLogger(JsonParentDisplay.class.getName());
    public final static String STORABLE_PARAMS_INFO = "storableParams";
    public final static String STORABLE_PARAMS_QUERY_STRING = "queryString";
    public final static String STORABLE_PARAMS_HASH = "hash";
    
    /**
     * An {@code Enum} representing the values that the "status" property can take 
     * in the JSON responses.
     * <ul>
     * <li>{@code FAIL}: corresponds to HTTP status response values from 500-599.
     * <li>{@code ERROR}: corresponds to HTTP status response values from 400-499.
     * <li>{@code SUCCESS}: corresponds to any other HTTP status response (1XX, 2XX and 3XX responses).
     * </ul>
     * 
     * @author Frederic Bastian
     * @version Bgee 13 Nov. 2015
     * @since Bgee 13 Nov. 2015
     * @see <a href="https://www.npmjs.com/package/structured-json-response">https://www.npmjs.com/package/structured-json-response</a>
     */
    public static enum ResponseStatus {
        SUCCESS("success"), FAIL("fail"), ERROR("error");
        
        /**
         * Returns the {@code ResponseStatus} associate to a HTTP status response code. 
         * 
         * @param code  An {@code int} that is the HTTP status response code.
         * @return      A {@code ResponseStatus} corresponding to {@code code}.
         * @throws IllegalArgumentException     If {@code code} is negative or not supported.
         */
        private static ResponseStatus getResponseStatusFromCode(int code) throws IllegalArgumentException {
            log.traceEntry("{}", code);
            if (code < 0) {
                throw log.throwing(new IllegalArgumentException("Accept only positive integer."));
            }
            //get code first digit
            int firstDigit = code;
            while (firstDigit > 9) {
                  firstDigit = firstDigit / 10;
            }
            //determine ReponseStatus from this first digit
            if (firstDigit == 5) {
                return log.traceExit(ResponseStatus.FAIL);
            }
            if (firstDigit == 4) {
                return log.traceExit(ResponseStatus.ERROR);
            }
            if (firstDigit >= 1 && firstDigit <= 3) {
                return log.traceExit(ResponseStatus.SUCCESS);
            }
            throw log.throwing(new IllegalArgumentException("Unsupported HTTP status code: " + code));
        }
        
        private final String stringRepresentation;
        /**
         * @param stringRepresentation  A {@code String} to return when {@code toString} is called.
         */
        private ResponseStatus(String stringRepresentation) {
            this.stringRepresentation = stringRepresentation;
        }
        @Override
        public String toString() {
            return this.stringRepresentation;
        }
    }

    public static LinkedHashMap<String, String> getStorableParamsInfo(RequestParameters rp) {
        log.traceEntry("{}", rp);
        //Clone only with the storable parameters, this is the only thing we want
        RequestParameters clonedRp = rp.cloneWithStorableParameters();
        //Calling getRequestURL will trigger the generation of the query string
        clonedRp.getRequestURL();
        LinkedHashMap<String, String> storableParamsInfo = new LinkedHashMap<>();
        storableParamsInfo.put(STORABLE_PARAMS_QUERY_STRING, clonedRp.getParameterQuery());
        storableParamsInfo.put(STORABLE_PARAMS_HASH, clonedRp.getDataKey());
        return log.traceExit(storableParamsInfo);
    }
    
    /**
     * @see #getJsonHelper()
     */
    private final JsonHelper jsonHelper;
    
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
     * @param factory           The {@code JsonFactory} that was used to instantiate this object.
     * 
     * @throws IllegalArgumentException If {@code factory} or {@code jsonHelper} is {@code null}.
     * @throws IOException              If there is an issue when trying to get or to use the
     *                                  {@code PrintWriter} 
     */
    public JsonParentDisplay(HttpServletResponse response, RequestParameters requestParameters, 
            BgeeProperties prop, JsonHelper jsonHelper, JsonFactory factory) 
                    throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, factory);
        if (jsonHelper == null) {
            throw log.throwing(new IllegalArgumentException("The JsonHelper cannot be null."));
        }
        this.jsonHelper = jsonHelper;
    }

    /**
     * Delegate to {@link #sendResponse(int, String, LinkedHashMap)} 
     * with a "200" HTTP response status.
     * 
     * @param msg
     * @param data                      An {@code Object} to be dumped. Usually, if several objects
     *                                  must be dumped, {@code data} is a {@code LinkedHashMap}
     *                                  where keys are {@code String}s that are parameter names,
     *                                  the associated value being the value to be dumped.
     *                                  Provided as {@code LinkedHashMap} to obtain predictable responses. 
     */
    protected void sendResponse(String msg, Object data) {
        log.traceEntry("{}, {}", msg, data);
        this.sendResponse(HttpServletResponse.SC_OK, msg, data);
        log.traceExit();   
    }
    /**
     * Send a standard JSON response to client. The response include the HTTP response status code, 
     * ("code" parameter), and status message ("status" parameter, see {@link ResponseStatus}), 
     * a custom message provided ("message" parameter), and some data ("data" parameter).
     * If the value associated to the URL parameter {@code URLParameters.getParamDisplayRequestParams()} 
     * is {@code true} in the current request, then the {@code RequestParameters} object corresponding to
     * the current request will be sent with the response.
     * 
     * @param code                      An {@code int} that is the HTTP response status code.
     * @param msg                       A {@code String} that is a message describing the response.
     * @param data                      An {@code Object} to be dumped. Usually, if several objects
     *                                  must be dumped, {@code data} is a {@code LinkedHashMap}
     *                                  where keys are {@code String}s that are parameter names,
     *                                  the associated value being the value to be dumped.
     *                                  Provided as {@code LinkedHashMap} to obtain predictable responses. 
     */
    protected void sendResponse(int code, String msg, Object data) {
        log.traceEntry("{}, {}, {}", code, msg, data);
        this.sendResponse(code, msg, data, false);
        log.traceExit();
    }
    protected void sendResponse(int code, String msg, Object data, boolean sendStorableParamsQueryString) {
        log.traceEntry("{}, {}, {}, {}", code, msg, data, sendStorableParamsQueryString);
        
        //The code will be validated by the calls to the methods sendAppropriateHeaders and 
        //getResponseStatusFromCode. 
        //Use a LinkedHashMap for predictable responses.
        LinkedHashMap<String, Object> jsonResponse = new LinkedHashMap<>();
        jsonResponse.put("code", code);
        jsonResponse.put("status", ResponseStatus.getResponseStatusFromCode(code));
        if (StringUtils.isNotBlank(msg)) {
            jsonResponse.put("message", msg);
        }
        if (Boolean.TRUE.equals(this.getRequestParameters().getFirstValue(
                this.getRequestParameters().getUrlParametersInstance().getParamDisplayRequestParams()))) {
            //We call getRequestURL() method to make sure the data hash is generated
            //before dumping the object
            this.getRequestParameters().getRequestURL();
            jsonResponse.put("requestParameters", this.getRequestParameters());
        }
        if (sendStorableParamsQueryString) {
            jsonResponse.put(STORABLE_PARAMS_INFO, getStorableParamsInfo(this.getRequestParameters()));
        }
        if (data != null) {
            jsonResponse.put("data", data);
        }

        this.sendAppropriateHeaders(code);
        this.jsonHelper.toJson(jsonResponse, this.getOut());
        
        log.traceExit();
    }
    
    /**
     * Method determining the correct headers to send client, depending on 
     * the HTTP response status code.
     * 
     * @param code  An {@code int} that is the HTTP response status code.
     */
    private void sendAppropriateHeaders(int code) {
        log.traceEntry("{}", code);
        if (ResponseStatus.getResponseStatusFromCode(code).equals(ResponseStatus.SUCCESS)) {
            super.sendHeaders();
            log.traceExit(); return;
        }
        if (code == HttpServletResponse.SC_SERVICE_UNAVAILABLE) {
            super.sendServiceUnavailableHeaders();
            log.traceExit(); return;
        }
        if (code == HttpServletResponse.SC_BAD_REQUEST) {
            super.sendBadRequestHeaders();
            log.traceExit(); return;
        }
        if (code == HttpServletResponse.SC_NOT_FOUND) {
            super.sendPageNotFoundHeaders();
            log.traceExit(); return;
        }
        if (code == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
            super.sendInternalErrorHeaders();
            log.traceExit(); return;
        }
        if (code == 429) {
            super.sendTooManyRequeststHeaders();
            log.traceExit(); return;
        }
        throw log.throwing(new IllegalArgumentException("Unsupported HTTP status code: " + code));
    }

    @Override
    protected String getContentType() {
        log.traceEntry();
        return log.traceExit("application/json");
    }
    
    //*****************************************************************
    // DISABLE SOME ConcreteDisplayParent METHODS FOR SUBCLASSES
    // (everything should go through the sendResponse methods)
    //*****************************************************************
    protected void sendHeaders() {
        throw log.throwing(new UnsupportedOperationException(
            "Subclasses are only allowed to call one of the JsonParentDisplay sendResponse methods."));
    }
    public void sendServiceUnavailableHeaders() {
        throw log.throwing(new UnsupportedOperationException(
            "Subclasses are only allowed to call one of the JsonParentDisplay sendResponse methods."));
    }
    protected void sendBadRequestHeaders() {
        throw log.throwing(new UnsupportedOperationException(
            "Subclasses are only allowed to call one of the JsonParentDisplay sendResponse methods."));
    }
    protected void sendPageNotFoundHeaders() {
        throw log.throwing(new UnsupportedOperationException(
            "Subclasses are only allowed to call one of the JsonParentDisplay sendResponse methods."));
    }
    protected void sendInternalErrorHeaders() {
        throw log.throwing(new UnsupportedOperationException(
            "Subclasses are only allowed to call one of the JsonParentDisplay sendResponse methods."));
    }
    
    protected void writeln(String stringToWrite) {
        throw log.throwing(new UnsupportedOperationException(
            "Subclasses are only allowed to call one of the JsonParentDisplay sendResponse methods."));
    }
    protected void write(String stringToWrite) {
        throw log.throwing(new UnsupportedOperationException(
            "Subclasses are only allowed to call one of the JsonParentDisplay sendResponse methods."));
    }
}
