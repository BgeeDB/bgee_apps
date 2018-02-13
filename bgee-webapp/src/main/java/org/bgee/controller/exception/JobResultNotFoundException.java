package org.bgee.controller.exception;

import java.util.LinkedHashMap;

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
        this(message, null);
    }
    /**
     * Constructor with a {@code message} argument.
     * This message must be really basic and understandable,
     * as it will be displayed to the user.
     *
     * @param message           A {@code String} giving details about the exception.
     * @param additionalData    A {@code LinkedHashMap} where keys are {@code String}s
     *                          that are parameter names, the associated value being
     *                          the value to be dumped. Provided as {@code LinkedHashMap}
     *                          to obtain predictable responses. Allows to store additional information
     *                          related to the invalid request.
     */
    public JobResultNotFoundException(String message, LinkedHashMap<String, Object> additionalData) {
        super(message, additionalData);
    }
}
