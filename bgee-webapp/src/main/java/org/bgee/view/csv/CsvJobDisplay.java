package org.bgee.view.csv;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.job.Job;
import org.bgee.view.JobDisplay;
import org.bgee.view.ViewFactory;

/**
 * This class is the HTML implementation of the {@code JobDisplay}.
 * 
 * @author  Frederic Bastian
 * @version Bgee 13, Oct. 2016
 * @since   Bgee 13, Oct. 2016
 */
public class CsvJobDisplay extends CsvParentDisplay implements JobDisplay {
    private final static Logger log = LogManager.getLogger(CsvJobDisplay.class.getName());

    protected CsvJobDisplay(HttpServletResponse response, RequestParameters requestParameters, 
            BgeeProperties prop, ViewFactory factory, Delimiter delimiter) 
                    throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, factory, delimiter);
    }

    @Override
    public void cancelJob(Job job) {
        log.entry(job);
        this.startDisplay();

        if (job != null) {
            this.write("Cancellation of your job ");
            if (StringUtils.isNotBlank(job.getName())) {
                this.write("\"" + job.getName() + "\"");
            } else {
                this.write("with ID " + job.getId());
            }
            this.writeln(" has been successfully requested.");
        } else {
            this.writeln("Job already gone, your job must have been already canceled.");
        }
        
        this.endDisplay();
        log.traceExit();
    }

    @Override
    public void sendJobErrorResponse(Job job) {
        //TODO
    }
}
