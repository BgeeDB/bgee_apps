package org.bgee.model.dao.api.exception;

/**
 * {@code Exception} thrown when an error occurs while accessing the data source. 
 *  
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class DAOException extends Exception {

	/**
	 * Because {@code Exception}s are serializable. 
	 */
	private static final long serialVersionUID = 773137615240568541L;
	
	/**
	 * Constructor providing the cause for this {@code Exception} to occur.
	 * 
	 * @param cause    A {@code Throwable} that is the cause of this {@code DAOException}.
	 */
	public DAOException(Throwable cause) {
		super(cause);
	}

}
