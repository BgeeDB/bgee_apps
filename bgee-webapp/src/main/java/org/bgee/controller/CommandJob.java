package org.bgee.controller;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.InvalidRequestException;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.model.ServiceFactory;
import org.bgee.model.TaskManager;
import org.bgee.view.ViewFactory;

/**
 * Controller handling requests related to job management.
 * 
 * @author  Frederic Bastian
 * @version Bgee 13 Dec 2015
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
     */
    public CommandJob(HttpServletResponse response, RequestParameters requestParameters, 
            BgeeProperties prop, ViewFactory viewFactory, ServiceFactory serviceFactory) {
        super(response, requestParameters, prop, viewFactory, serviceFactory);
    }


    @Override
    public void processRequest() throws Exception {
        log.entry();
        
        if (this.requestParameters.isACancelJob()) {
            Integer jobID = this.requestParameters.getJobId(); 
            
            if (jobID == null || jobID < 1) {
                throw log.throwing(new InvalidRequestException("A job ID must be provided"));
            }
            
            // Retrieve task manager associated to the provided ID
            TaskManager taskManager = TaskManager.getTaskManager(jobID);
            if (taskManager != null) {
                //Retrieve the underlying Thread running the Task, and gently request interruption
                taskManager.interrupt();
            }
            //TODO: we should have some kind of response sent.
        } else {
            throw log.throwing(new PageNotFoundException("Incorrect " 
                    + this.requestParameters.getUrlParametersInstance().getParamAction() 
                    + " parameter value."));
        }
        
        log.exit();
    }
}
