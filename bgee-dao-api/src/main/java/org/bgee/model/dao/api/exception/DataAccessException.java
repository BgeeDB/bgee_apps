package org.bgee.model.dao.api.exception;

/**
 * <code>Exception</code> thrown when an error occurs while accessing the data source. 
 *  
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class DataAccessException extends Exception {

	/**
	 * Because <code>Exception</code>s are serializable. 
	 */
	private static final long serialVersionUID = 773137615240568541L;
	
	/**
	 * Constructs a new exception with null as its detail message.
	 */
	public DataAccessException() {
		super();
	}

}
