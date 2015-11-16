package org.bgee.controller.exception;

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
     * Constructor with a {@code message} argument. 
     * This message must be really basic and understandable, 
     * as it will be displayed to the user. 
     * 
     * @param message   a {@code String} giving details about the exception. 
     */
    public InvalidRequestException(String message) {        
        super(message);
    }
}
