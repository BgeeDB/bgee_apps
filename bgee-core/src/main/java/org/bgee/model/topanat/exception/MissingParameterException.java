package org.bgee.model.topanat.exception;

public class MissingParameterException extends Exception
{

    /**
     * 
     */
    private static final long serialVersionUID = 7643673641476450801L;
    
    /**
     * 
     */
    private final String paramName;

    /**
     * Constructor with an additional {@code message} argument. 
     * This message MUST be really basic and understandable, 
     * as it will be displayed to the user. 
     * 
     * @param message   a {@code String} giving details about the exception, 
     *                  BUT despite usually, it MUST be really basic and 
     *                  understandable, as it will be displayed to the user. 
     */
    public MissingParameterException(String paramName)
    {        
        super("The mandatory parameter for "+paramName+" was not provided.");
        this.paramName = paramName;
    }

    /**
     * Constructor with additional {@code message} and {@code cause} arguments. 
     * 
     * @param message   a {@code String} giving details about the exception.
     * @param cause     a {@code Throwable} giving the cause of the exception.
     */
    public MissingParameterException(String paramName, Throwable cause)
    {
        super(paramName, cause);
        this.paramName = paramName;

    }

    /**
     * 
     * @return
     */
    public String getParamName() {
        return paramName;
    }
}
