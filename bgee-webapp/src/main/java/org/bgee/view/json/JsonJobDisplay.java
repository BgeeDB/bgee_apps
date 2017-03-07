package org.bgee.view.json;

import java.io.IOException;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.job.Job;
import org.bgee.view.JobDisplay;
import org.bgee.view.JsonHelper;

/**
 * This class is the JSON implementation of the {@code JobDisplay}.
 * 
 * @author  Frederic Bastian
 * @version Bgee 13, Oct. 2016
 * @since   Bgee 13, Oct. 2016
 */
public class JsonJobDisplay extends JsonParentDisplay implements JobDisplay {
    private final static Logger log = LogManager.getLogger(JsonJobDisplay.class.getName());

    public JsonJobDisplay(HttpServletResponse response, RequestParameters requestParameters, 
            BgeeProperties prop, JsonHelper jsonHelper, JsonFactory factory) 
                    throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, jsonHelper, factory);
    }

    @Override
    public void cancelJob(Job job) {
        log.entry(job);
        
        if (job != null) {
            LinkedHashMap<String, Object> data = new LinkedHashMap<>();
            data.put("job", job);
            
            this.sendResponse("Job cancellation requested with success", data);
        } else {
            this.sendResponse("Job already gone, your job must have been already canceled.", null);
        }
        
        log.exit();
    }

    @Override
    public void sendJobErrorResponse(Job job) {
        // TODO Auto-generated method stub
    }
}
