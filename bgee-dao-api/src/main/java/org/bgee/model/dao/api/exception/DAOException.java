package org.bgee.model.dao.api.exception;

/**
 * {@code Exception} thrown when an error occurs while accessing the data source. 
 * <p>
 * It is a {@code RuntimeException}, because if a DAO encounters an error 
 * which it can recover from, then it is its responsibility to do it. If the DAO 
 * lets this exception getting out, then the application should not be able 
 * to recover from it. Thus it is an unchecked exception.
 *  
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class DAOException extends RuntimeException {

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
	
	/**
     * Constructs {@code DAOException} with the specified detail message.
     * 
     * @param message   the detail message (which is saved for later retrieval by the 
     *                  {@code Throwable.getMessage()} method).
     */
    public DAOException(String message) {
        super(message);
    }

}
