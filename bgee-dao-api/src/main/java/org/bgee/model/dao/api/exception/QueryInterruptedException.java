package org.bgee.model.dao.api.exception;

/**
 * An {@code Exception} thrown when the query to a data source was interrupted 
 * following a call to {@link org.bgee.model.dao.api.DAOManager#kill()} or 
 * {@link org.bgee.model.dao.api.DAOManager#kill(long)}. This {@code Exception} 
 * should be thrown from the thread that was interrupted, not from the thread 
 * that call the {@code kill} method. It is a {@code RuntimeException}, 
 * as if a query was interrupted, it was on purpose, and the application should not 
 * recovered from it. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class QueryInterruptedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5708317886250562148L;
	
	/**
	 * Constructs a new exception with null as its detail message.
	 */
	public QueryInterruptedException() {
		super();
	}

}
