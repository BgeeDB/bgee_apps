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
    public void displayRequestParametersNotFound(String key) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void displayPageNotFound(String message) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void displayUnexpectedError() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void displayMultipleParametersNotAllowed(String message) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void displayRequestParametersNotStorable(String message) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void displayWrongFormat(String message) {
        // TODO Auto-generated method stub
        
    }
    
}
