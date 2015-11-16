package org.bgee.controller.exception;

/**
 * Exception corresponding to a 404 "not found". 
 * 
 * @author 	Frederic Bastian
 * @version Bgee 13, Nov. 2015
 * @see 	org.bgee.controller.CommandParent#processRequest()
 * @since 	Bgee 12
 */
public class PageNotFoundException extends Exception implements ControllerException {
    private static final long serialVersionUID = 1687241145584707290L;

    /**
     * Default constructor
     */
    public PageNotFoundException() {
        super();
    }

    /**
     * Constructor with an additional {@code message} argument. 
     * This message must be really basic and understandable, 
     * as it will be displayed to the user. 
     * 
     * @param message 		a {@code String} giving details about the exception. 
     */
    public PageNotFoundException(String message) {
        super(message);
    }
}
