package org.bgee.model.exception;

public class InvalidForegroundException extends Exception
{

    /**
     * 
     */
    private static final long serialVersionUID = -7541635356881955063L;

    /**
     * Default constructor
     */
    public InvalidForegroundException()
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
    public InvalidForegroundException(String message)
    {        
        super(message);
    }

    /**
     * Constructor with an additional {@code cause} argument. 
     * 
     * @param cause     a {@code Throwable} giving the cause of the exception.
     */
    public InvalidForegroundException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Constructor with additional {@code message} and {@code cause} arguments. 
     * 
     * @param message   a {@code String} giving details about the exception.
     * @param cause     a {@code Throwable} giving the cause of the exception.
     */
    public InvalidForegroundException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
