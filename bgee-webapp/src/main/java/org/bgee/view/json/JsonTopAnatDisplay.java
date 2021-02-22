package org.bgee.view.json;

import java.io.IOException;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.JsonHelper;
import org.bgee.view.TopAnatDisplay;

/**
 * This class generates the JSON views relative to topAnat.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Oct 2016
 * @since   Bgee 13
 */
public class JsonTopAnatDisplay extends JsonParentDisplay implements TopAnatDisplay {
    
    private final static Logger log = LogManager.getLogger(JsonTopAnatDisplay.class.getName());
    
    /**
     * Constructor providing the necessary dependencies. 
     * 
     * @param response          A {@code HttpServletResponse} that will be used to display the 
     *                          page to the client
     * @param requestParameters The {@code RequestParameters} handling the parameters of the 
     *                          current request.
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     * @param jsonHelper        A {@code JsonHelper} used to dump variables into Json.
     * @param factory           The {@code JsonFactory} that was used to instantiate this object.
     * 
     * @throws IllegalArgumentException If {@code factory} or {@code jsonHelper} is {@code null}.
     * @throws IOException      If there is an issue when trying to get or to use the
     *                          {@code PrintWriter} 
     */
    public JsonTopAnatDisplay(HttpServletResponse response,
            RequestParameters requestParameters, BgeeProperties prop,
            JsonHelper jsonHelper, JsonFactory factory) throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, jsonHelper, factory);
    }

    @Override
    public void displayTopAnatHomePage() {
        throw log.throwing(new UnsupportedOperationException("Not available for JSON display"));
    }

    @Override
    public void sendGeneListReponse(LinkedHashMap<String, Object> data, String msg) {
        log.entry(data, msg);        
        this.sendResponse(msg, data);
        log.traceExit();
    }

    @Override
    public void sendTopAnatParameters(String hash) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void sendTrackingJobResponse(LinkedHashMap<String, Object> data, String msg) {
        log.entry(data, msg);        
        this.sendResponse(msg, data);
        log.traceExit();
    }

    @Override
    public void sendResultResponse(LinkedHashMap<String, Object> data, String msg) {
        log.entry(data, msg);        
        this.sendResponse(msg, data);
        log.traceExit();
    }
}
