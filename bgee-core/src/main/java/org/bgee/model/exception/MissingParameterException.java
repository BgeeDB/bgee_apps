package org.bgee.model.exception;

public class MissingParameterException extends Exception
{

    /**
     * 
     */
    private static final long serialVersionUID = 7643673641476450801L;

    /**
     * Default constructor
     */
    public MissingParameterException()
    {
        super();
    }

    /**
     * Constructor with an additional {@code message} argument. 
     * This message MUST be really basic and understandable, 
     * as it will be displayed to the user. 
     * 
     * @param message   a {@code String} giving details about the exception, 
     *                  BUT despite usually, it MUST be really basic and 
     *                  understandable, as it will be displayed to the user. 
     */
    public MissingParameterException(String message)
    {        
        super("The mandatory parameter for "+message+" was not provided.");
    }

    /**
     * Constructor with an additional {@code cause} argument. 
     * 
     * @param cause     a {@code Throwable} giving the cause of the exception.
     */
    public MissingParameterException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Constructor with additional {@code message} and {@code cause} arguments. 
     * 
     * @param message   a {@code String} giving details about the exception.
     * @param cause     a {@code Throwable} giving the cause of the exception.
     */
    public MissingParameterException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
