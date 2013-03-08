package org.bgee.model.data.sql;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Abstraction layer to use a <code>java.sql.Connection</code>. 
 * This class is used to connect to the Bgee database. 
 * It provides only the functionalities of the <code>java.sql.Connection</code> interface 
 * that are used in the Bgee project. So it does not implement the actual interface.
 * 
 * @author Frederic Bastian
 * @version Bgee 13, Mar 2013
 * @since Bgee 13
 */
public class BgeeConnection implements AutoCloseable
{
	/**
	 * <code>Logger</code> of the class. 
	 */
	private final static Logger log = LogManager.getLogger(BgeeConnection.class.getName());
	/**
	 * The <code>BgeeDataSource</code> used to obtain this <code>BgeeConnection</code>. 
	 * Used for notifications purpose. 
	 */
	private final BgeeDataSource bgeeDataSource;
	/**
	 * The real <code>java.sql.Connection</code> that this class wraps.
	 */
    private final Connection realConnection;
    /**
     * A <code>String</code> representing an identifier 
     * for this <code>BgeeConnection</code> object. It is used for the connection 
     * to be tracked by the container <code>BgeeDataSource</code> object 
     * (the <code>BgeeDataSource</code> that instantiated this <code>BgeeConnection</code>).
     */
    private final String id;
    
    /**
     * Default constructor, should not be used. 
     * Constructor protected, so that only a {@link BgeeDataSource} can provide 
     * a <code>BgeeConnection</code>.
     */
    protected BgeeConnection() 
    {
    	this(null, null, null);
    }
    /**
     * Constructor providing the <code>BgeeDataSource</code> object used to instantiate 
     * this connection (for notifications purpose), the real <code>java.sql.Connection</code> 
     * that this class wraps, and the <code>id</code> to use to identify and track this connection.
     * 
     * Constructor protected, so that only a {@link BgeeDataSource} can instantiate 
     * a <code>BgeeConnection</code>.
     * @param dataSource 		The <code>BgeeDataSource</code> used to obtain 
     * 							this connection. Is used for notifications purpose 
     * 							(notably when <code>close()</code> is called 
     * 							on this connection <code>BgeeConnection</code>).
     * @param realConnection 	The <code>java.sql.Connection</code> that this class wraps
     * @param id 				A <code>String</code> representing the ID of this 
     * 							<code>BgeeConnection</code>, used by <code>dataSource</code> 
     * 							to track the connection.
     */
    protected BgeeConnection(BgeeDataSource dataSource, Connection realConnection, 
    		String id)
    {
    	log.entry(dataSource, realConnection, id);
    	
    	this.bgeeDataSource = dataSource;
    	this.realConnection = realConnection;
    	this.id = id;
    	
    	log.exit();
    }
    
    
    public BgeePreparedStatement prepareStatement(String sql) throws SQLException 
    {
    	log.entry(sql);
    	return log.exit(
    		new BgeePreparedStatement(this, this.getRealConnection().prepareStatement(sql)));
    }
    

	/**
	 * @return the {@link #realConnection}
	 */
	private Connection getRealConnection() {
		return this.realConnection;
	}
	/**
	 * @return the {@link #id}
	 */
	public String getId() {
		return this.id;
	}
	/**
	 * @return the {@link #bgeeDataSource}
	 */
	private BgeeDataSource getBgeeDataSource() {
		return this.bgeeDataSource;
	}
	
	/**
	 * Close the real <code>Connection</code> that this class wraps, 
	 * and notify of the closing the <code>BgeeDataSource</code> used to obtain 
	 * this <code>BgeeConnection</code>.
	 * 
	 * @throws SQLException 	If the real <code>Connection</code> that this class wraps
	 * 							throws a <code>SQLException</code> when closing.  
	 */
	@Override
	public void close() throws SQLException {
		try {
		    this.getRealConnection().close();
		} catch (SQLException e) {
			throw e;
		} finally {
			this.getBgeeDataSource().connectionClosed(this.getId());
		}
		
	}
}
