package org.bgee.controller.exception;

/**
 * An {@code InvalidRequestException} thrown when the results of a long-running job could not be retrieved 
 * when requested. This could be simply due to a job being interrupted by the user, and not formally 
 * to an error, this is why we use a different exception. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Oct 2016
 * @since Bgee 13 Oct 2016
 */
public class JobResultNotFoundException extends InvalidRequestException {
    private static final long serialVersionUID = -2528667792878406830L;

    /**
     * Constructor with a {@code message} argument. 
     * This message must be really basic and understandable, 
     * as it will be displayed to the user. 
     * 
     * @param message   a {@code String} giving details about the exception. 
     */
    public JobResultNotFoundException(String message) {        
        super(message);
    }
}
