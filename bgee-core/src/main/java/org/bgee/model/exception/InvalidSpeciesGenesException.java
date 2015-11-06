package org.bgee.model.exception;

public class InvalidSpeciesGenesException extends Exception
{

    /**
     * 
     */
    private static final long serialVersionUID = 3416827306247244166L;

    /**
     * Default constructor
     */
    public InvalidSpeciesGenesException()
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
    public InvalidSpeciesGenesException(String message)
    {        
        super(message);
    }

    /**
     * Constructor with an additional {@code cause} argument. 
     * 
     * @param cause     a {@code Throwable} giving the cause of the exception.
     */
    public InvalidSpeciesGenesException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Constructor with additional {@code message} and {@code cause} arguments. 
     * 
     * @param message   a {@code String} giving details about the exception.
     * @param cause     a {@code Throwable} giving the cause of the exception.
     */
    public InvalidSpeciesGenesException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
