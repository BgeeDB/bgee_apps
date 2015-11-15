package org.bgee.view.json;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.ConcreteDisplayParent;
import org.bgee.view.JsonHelper;

public class JsonParentDisplay extends ConcreteDisplayParent {

    private final static Logger log = LogManager.getLogger(JsonParentDisplay.class.getName());
    
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

    @Override
    protected String getContentType() {
        log.entry();
        return log.exit("application/json");
    }
    
    /**
     * @return  A {@code JsonHelper} used to dump variables into Json. It is immutable 
     *          and can be safely reused for different calls. 
     */
    protected JsonHelper getJsonHelper() {
        return jsonHelper;
    }
}
