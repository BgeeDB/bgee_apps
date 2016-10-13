package org.bgee.model.job.exception;

/**
 * A {@code JobException} thrown when a user tries to launch a new {@code Job} in a {@code Thread} 
 * that is currently already holding another {@code Job}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Oct. 2016
 * @since Bgee 13 Oct. 2016
 */
public class ThreadAlreadyWorkingException extends JobException {
    private static final long serialVersionUID = 7597527408835894159L;

    /**
     * Construct a new {@code ThreadAlreadyWorkingException}. 
     */
    public ThreadAlreadyWorkingException() {
        super("You tried to launch a new job from a thread that is already holding another job. "
              + "Did you forget to call 'release' on the job after result retrieval? "
              + "Otherwise, wait for the first job to finish before registering a new one "
              + "from this thread.");
    }
}
