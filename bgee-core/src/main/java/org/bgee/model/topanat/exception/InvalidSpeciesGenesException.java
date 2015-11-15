package org.bgee.model.topanat.exception;

import java.util.Set;

public class InvalidSpeciesGenesException extends Exception
{

    /**
     * 
     */
    private static final long serialVersionUID = 3416827306247244166L;
    
    private final Set<String> invalidGeneIds;

    /**
     * Default constructor
     */
    public InvalidSpeciesGenesException(Set<String> invalidGeneIds)
    {
        super();
        this.invalidGeneIds = invalidGeneIds;
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
    public InvalidSpeciesGenesException(String message,Set<String> invalidGeneIds)
    {        
        super(message);
        this.invalidGeneIds = invalidGeneIds;
    }

    /**
     * Constructor with an additional {@code cause} argument. 
     * 
     * @param cause     a {@code Throwable} giving the cause of the exception.
     */
    public InvalidSpeciesGenesException(Throwable cause,Set<String> invalidGeneIds)
    {
        super(cause);
        this.invalidGeneIds = invalidGeneIds;
    }

    /**
     * Constructor with additional {@code message} and {@code cause} arguments. 
     * 
     * @param message   a {@code String} giving details about the exception.
     * @param cause     a {@code Throwable} giving the cause of the exception.
     */
    public InvalidSpeciesGenesException(String message, Throwable cause,Set<String> invalidGeneIds)
    {
        super(message, cause);
        this.invalidGeneIds = invalidGeneIds;
    }

    /**
     * 
     * @return
     */
    public Set<String> getInvalidGeneIds() {
        return invalidGeneIds;
        
    }
}
