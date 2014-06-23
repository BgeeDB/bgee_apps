package org.bgee.controller.exception;

/**
 * This <code>Exception</code> is thrown when a <code>RequestParameters</code> object 
 * could not be serialized and stored in a file, to be later retrieved. 
 * <p>
 * Usually, when parameters of a request are too long to be passed through URL, 
 * a key is generated, and is used i) to name the file where the <code>RequestParameters</code> object 
 * holding the parameters is serialized, 
 * and ii) to replace some parameters in the URL by this key (to shorten it). 
 * If something goes wrong when trying to generate the key, or to serialize 
 * and store the <code>RequestParameters</code> object, this <code>RequestParametersNotStorableException</code> 
 * is thrown.
 * This <code>Exception</code> can then be thrown when trying to get obtain an URL 
 * from a <code>RequestParameters</code> object.
 * 
 * @author Frederic Bastian
 * @version Bgee 11, May 2012
 * @see controller.RequestParameters
 * @since Bgee 11
 *
 */
public class RequestParametersNotStorableException extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4587049017135736170L;
	
	/**
	 * Default constructor.
	 */
	public RequestParametersNotStorableException()
	{
		super();
	}
	
	/**
	 * Constructor with an additional <code>message</code> argument. 
	 * 
	 * @param message 		a <code>String</code> giving details about the exception.
	 */
	public RequestParametersNotStorableException(String message)
	{
		super(message);
	}
	
	/**
	 * Constructor with an additional <code>cause</code> argument. 
	 * 
	 * @param cause 		a <code>Throwable</code> giving the cause of the exception.
	 */
	public RequestParametersNotStorableException(Throwable cause)
	{
		super(cause);
	}
	
	/**
	 * Constructor with additional <code>message</code> and <code>cause</code> arguments. 
	 * 
	 * @param message 		a <code>String</code> giving details about the exception.
	 * @param cause 		a <code>Throwable</code> giving the cause of the exception.
	 */
	public RequestParametersNotStorableException(String message, Throwable cause)
	{
		super(message, cause);
	}

}
