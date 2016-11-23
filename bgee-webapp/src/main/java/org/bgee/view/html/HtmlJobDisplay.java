package org.bgee.view.html;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.job.Job;
import org.bgee.view.JobDisplay;

/**
 * This class is the HTML implementation of the {@code JobDisplay}.
 * 
 * @author  Frederic Bastian
 * @version Bgee 13, Oct. 2016
 * @since   Bgee 13, Oct. 2016
 */
public class HtmlJobDisplay extends HtmlParentDisplay implements JobDisplay {
    private final static Logger log = LogManager.getLogger(HtmlGeneDisplay.class.getName());

    public HtmlJobDisplay(HttpServletResponse response, RequestParameters requestParameters, 
            BgeeProperties prop, HtmlFactory factory) throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, factory);
    }

    @Override
    public void cancelJob(Job job) {
        log.entry(job);
        this.startDisplay("Job cancellation");
        
        if (job != null) {
            this.writeln("<h1>Job cancellation requested with success</h1>");
            this.write("<p>Cancellation of your job ");
            if (StringUtils.isNotBlank(job.getName())) {
                this.write("\"" + htmlEntities(job.getName()) + "\"");
            } else {
                this.write("with ID " + htmlEntities(String.valueOf(job.getId())));
            }
            this.writeln(" has been successfully requested.</p>");
        } else {
            this.writeln("<h1>Job already gone</h1>");
            this.writeln("<p>Your job must have been already canceled.</p>");
        }
        
        this.endDisplay();
        log.exit();
    }

    @Override
    public void sendJobErrorResponse(Job job) {
        //TODO
    }
}
