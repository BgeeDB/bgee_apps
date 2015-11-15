package org.bgee.model.topanat.exception;

import java.util.Set;

public class InvalidForegroundException extends Exception
{

    /**
     * 
     */
    private static final long serialVersionUID = -7541635356881955063L;
    
    /**
     * 
     */
    private final Set<String> notIncludedIds;

    /**
     * Default constructor
     */
    public InvalidForegroundException(Set <String> notIncludedIds)
    {
        super();
        this.notIncludedIds = notIncludedIds;
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
    public InvalidForegroundException(String message,Set<String> notIncludedIds)
    {        
        super(message);
        this.notIncludedIds = notIncludedIds;
    }

    /**
     * Constructor with an additional {@code cause} argument. 
     * 
     * @param cause     a {@code Throwable} giving the cause of the exception.
     */
    public InvalidForegroundException(Throwable cause,Set <String> notIncludedIds)
    {
        super(cause);
        this.notIncludedIds = notIncludedIds;
    }

    /**
     * Constructor with additional {@code message} and {@code cause} arguments. 
     * 
     * @param message   a {@code String} giving details about the exception.
     * @param cause     a {@code Throwable} giving the cause of the exception.
     */
    public InvalidForegroundException(String message, Throwable cause,Set <String> notIncludedIds)
    {
        super(message, cause);
        this.notIncludedIds = notIncludedIds;
    }

    public Set<String> getNotIncludedIds() {
        return notIncludedIds;
    }
}
