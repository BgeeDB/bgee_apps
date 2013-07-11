package org.bgee.model.dao.api.exception;

/**
 * An <code>Exception</code> thrown when the query to a data source was interrupted 
 * following a call to {@link org.bgee.model.dao.api.DAOManager#kill()}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class QueryInterruptedException extends Exception {

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
