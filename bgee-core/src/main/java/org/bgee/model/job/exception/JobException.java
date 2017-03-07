package org.bgee.model.job.exception;

/**
 * Parent class of all job-related exceptions.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Oct. 2016
 * @since Bgee 13 Oct. 2016
 */
public abstract class JobException extends Exception {
    private static final long serialVersionUID = -3660591356782860933L;

    /**
     * Constructs a new job-related exception with the specified detail message.
     * 
     * @param message   A {@code String} that is the detail message, saved for later retrieval 
     *                  by the {@code Throwable.getMessage()} method.
     */
    protected JobException(String message) {
        super(message);
    }
}
