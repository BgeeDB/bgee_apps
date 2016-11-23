package org.bgee.model.job.exception;

/**
 * A {@code JobException} thrown when a user tries to launch too many simultaneous running jobs.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Oct. 2016
 * @since Bgee 13 Oct. 2016
 */
public class TooManyJobsException extends JobException {
    private static final long serialVersionUID = 1536732665789836656L;
    
    /**
     * @see {@link #getMaxAllowedJobCount()}
     */
    private final int maxAllowedJobCount;
    
    /**
     * Construct a new {@code TooManyJobsException} with the provided max allowed number of running jobs. 
     *  
     * @param maxAllowedJobCount    An {@code int} that is the maximum allowed number  
     *                              of running jobs for the user. 
     */
    public TooManyJobsException(int maxAllowedJobCount) {
        super("You already have " + maxAllowedJobCount + " jobs running, which is the maximum allowed "
                + "number of running jobs. Wait for some of your jobs to finish before retrying.");
        this.maxAllowedJobCount = maxAllowedJobCount;
    }
    
    /**
     * @return  An {@code int} that is the maximum number of running jobs allowed for this user.
     */
    public int getMaxAllowedJobCount() {
        return maxAllowedJobCount;
    }
}
