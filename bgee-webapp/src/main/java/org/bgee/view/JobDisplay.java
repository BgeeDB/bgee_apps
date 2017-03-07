package org.bgee.view;

import org.bgee.model.job.Job;

/**
 * Interface defining methods to be implemented by views related to {@code Job}s.
 * 
 * @author  Frederic Bastian
 * @version Bgee 13, Oct. 2016
 * @since   Bgee 13, Oct. 2016
 */
//TODO: add a tracking job method
public interface JobDisplay {
    /**
     * Send response following the cancellation of a {@code Job} requested by the user.
     * 
     * @param job   The {@code Job} that was cancelled by the user.
     */
    public void cancelJob(Job job);
    
    /**
     * Send the response following an error in a job.
     * 
     * @param job   A {@code Job} tracking an analysis that failed. Can be {@code null} 
     *              if the job is already gone.
     */
    public void sendJobErrorResponse(Job job);
}
