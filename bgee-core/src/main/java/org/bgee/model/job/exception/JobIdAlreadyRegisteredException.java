package org.bgee.model.job.exception;

/**
 * A {@code JobException} thrown when a user tries to register a new {@code Job} 
 * with a job ID that is already used by another {@code Job}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Oct. 2016
 * @since Bgee 13 Oct. 2016
 */
public class JobIdAlreadyRegisteredException extends JobException {
    private static final long serialVersionUID = 8173909890376124554L;

    /**
     * Construct a new {@code JobIdAlreadyRegisteredException}. 
     */
    public JobIdAlreadyRegisteredException() {
        super("You tried to register a new job with a job ID already used by another job. "
              + "Did you forget to call 'release' on the first job after result retrieval? "
              + "Otherwise, either use the job registration methods auto-generating a job ID, "
              + "or use the method 'reserveAndGetJobId' to reserve a valid job ID before launching a job.");
    }
}
