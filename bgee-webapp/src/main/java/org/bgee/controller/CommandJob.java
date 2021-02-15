package org.bgee.controller;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.InvalidRequestException;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.controller.user.User;
import org.bgee.model.ServiceFactory;
import org.bgee.model.job.Job;
import org.bgee.model.job.JobService;
import org.bgee.view.JobDisplay;
import org.bgee.view.ViewFactory;

/**
 * Controller handling requests related to job management.
 * 
 * @author  Frederic Bastian
 * @version Bgee 13 Oct 2016
 * @since   Bgee 13 Dec 2015
 */
public class CommandJob extends CommandParent {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(CommandJob.class.getName());


    /**
     * Constructor providing necessary dependencies. 
     * 
     * @param response          A {@code HttpServletResponse} that will be used 
     *                          to display the page to the client
     * @param requestParameters The {@code RequestParameters} that handles 
     *                          the parameters of the current request.
     * @param prop              A {@code BgeeProperties} instance that contains 
     *                          the properties to use.
     * @param viewFactory       A {@code ViewFactory} providing the views of the appropriate 
     *                          display type.
     * @param serviceFactory    A {@code ServiceFactory} that provides bgee services.
     * @param jobService        A {@code JobService} instance allowing to manage jobs between threads 
     *                          across the entire webapp.
     * @param user              The {@code User} who is making the query to the webapp. 
     */
    public CommandJob(HttpServletResponse response, RequestParameters requestParameters, 
            BgeeProperties prop, ViewFactory viewFactory, ServiceFactory serviceFactory, 
            JobService jobService, User user) {
        super(response, requestParameters, prop, viewFactory, serviceFactory, jobService, user, null, null);
    }


    @Override
    public void processRequest() throws Exception {
        log.entry();
        
        JobDisplay display = this.viewFactory.getJobDisplay();
        
        if (this.requestParameters.isACancelJob()) {
            Integer jobId = this.requestParameters.getJobId(); 
            
            if (jobId == null || jobId < 1) {
                throw log.throwing(new InvalidRequestException("A job ID must be provided"));
            }
            
            // Retrieve job associated to the provided ID
            Job job = this.jobService.getJob(jobId);
            if (job != null) {
                //Retrieve the underlying Thread running the Task, and gently request interruption
                job.interrupt();
            }
            //If job is null it is OK, maybe the job is simply already gone
            display.cancelJob(job);
        } else {
            throw log.throwing(new PageNotFoundException("Incorrect " 
                    + this.requestParameters.getUrlParametersInstance().getParamAction() 
                    + " parameter value."));
        }
        
        log.traceExit();
    }
}
