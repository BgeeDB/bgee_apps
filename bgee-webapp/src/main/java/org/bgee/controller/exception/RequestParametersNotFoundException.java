package org.bgee.controller.exception;

/**
 * An exception thrown when a stored <code>RequestParameters</code> object is requested based on an indexed key value, 
 * but no <code>RequestParameters</code> object can be retrieved using that key.
 * 
 * @author 	Frederic Bastian
 * @version Bgee 11, May 2012
 * @see 	controller.RequestParameters
 * @see 	controller.RequestParameters#generatedKey
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
	 * Constructor with an additional <code>message</code> argument. 
	 * 
	 * @param message 		a <code>String</code> giving details about the exception.
	 */
	public RequestParametersNotFoundException(String message)
	{
		super(message);
	}
	
	/**
	 * Constructor with an additional <code>cause</code> argument. 
	 * 
	 * @param cause 		a <code>Throwable</code> giving the cause of the exception.
	 */
	public RequestParametersNotFoundException(Throwable cause)
	{
		super(cause);
	}
	
	/**
	 * Constructor with additional <code>message</code> and <code>cause</code> arguments. 
	 * 
	 * @param message 		a <code>String</code> giving details about the exception.
	 * @param cause 		a <code>Throwable</code> giving the cause of the exception.
	 */
	public RequestParametersNotFoundException(String message, Throwable cause)
	{
		super(message, cause);
	}

}
