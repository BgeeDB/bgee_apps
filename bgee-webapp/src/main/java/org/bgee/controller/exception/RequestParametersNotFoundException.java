package org.bgee.controller.exception;

/**
 * An exception thrown when a stored {@code RequestParameters} object is requested based
 * on an indexed key value, 
 * but no {@code RequestParameters} object can be retrieved using that key.
 * 
 * @author 	Frederic Bastian
 * @version Bgee 11, May 2012
 * @see 	org.bgee.controller.RequestParameters
 * @see 	org.bgee.controller.URLParameters#DATA
 * @since 	Bgee 11
 *
 */
public class RequestParametersNotFoundException extends Exception
{

    /**
     * 
     */
    private static final long serialVersionUID = -4828837007674742138L;

    /**
     * Default constructor.
     */
    public RequestParametersNotFoundException()
    {
        super();
    }

    /**
     * Constructor with an additional {@code message} argument. 
     * 
     * @param message 		a {@code String} giving details about the exception.
     */
    public RequestParametersNotFoundException(String message)
    {
        super(message);
    }

    /**
     * Constructor with an additional {@code cause} argument. 
     * 
     * @param cause 		a {@code Throwable} giving the cause of the exception.
     */
    public RequestParametersNotFoundException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Constructor with additional {@code message} and {@code cause} arguments. 
     * 
     * @param message 		a {@code String} giving details about the exception.
     * @param cause 		a {@code Throwable} giving the cause of the exception.
     */
    public RequestParametersNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
