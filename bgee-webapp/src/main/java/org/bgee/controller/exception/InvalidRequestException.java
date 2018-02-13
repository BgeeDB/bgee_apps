package org.bgee.controller.exception;

import java.util.LinkedHashMap;

/**
 * An {@code Exception} thrown when a request is malformed and could not be processed. 
 * This exception can for instance be thrown when a required parameter for a query 
 * is missing, or incompatible parameters are provided. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Nov. 2015
 * @since Bgee 13 Nov. 2015
 */
public class InvalidRequestException extends Exception implements ControllerException {

    private static final long serialVersionUID = 92035576451396047L;

    /**
     * @see #getAdditionalData()
     */
    private final LinkedHashMap<String, Object> additionalData;

    /**
     * Constructor with a {@code message} argument.
     * This message must be really basic and understandable,
     * as it will be displayed to the user.
     *
     * @param message           A {@code String} giving details about the exception.
     */
    public InvalidRequestException(String message) {
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
    public InvalidRequestException(String message, LinkedHashMap<String, Object> additionalData) {
        super(message);
        this.additionalData = additionalData == null ? new LinkedHashMap<>(): new LinkedHashMap<>(additionalData);
    }

    /**
     * @return  A {@code LinkedHashMap} where keys are {@code String}s that are parameter names,
     *          the associated value being the value to be dumped. Provided as {@code LinkedHashMap}
     *          to obtain predictable responses. Allows to store additional information
     *          related to the invalid request.
     */
    public LinkedHashMap<String, Object> getAdditionalData() {
        //Defensive copying, there is no unmodifiable LinkedHashMap (should use an ImmutableMap?)
        return new LinkedHashMap<>(this.additionalData);
    }
}
