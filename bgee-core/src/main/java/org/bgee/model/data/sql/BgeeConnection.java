package org.bgee.model.data.sql;

import java.sql.Connection;
import java.sql.SQLException;

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
public class BgeeConnection 
{
	/**
	 * The real <code>java.sql.Connection</code> that this class wraps.
	 */
    private Connection realConnection;
    
    /**
     * Default constructor, should not be used. 
     * Constructor protected, so that only a {@link BgeeDataSource} can provide 
     * a <code>BgeeConnection</code>.
     */
    protected BgeeConnection() 
    {
    	this(null);
    }
    /**
     * Constructor used to provide the real <code>java.sql.Connection</code> 
     * that this class wraps.
     * Constructor protected, so that only a {@link BgeeDataSource} can provide 
     * a <code>BgeeConnection</code>.
     * @param realConnection 	The <code>java.sql.Connection</code> that this class wraps
     */
    protected BgeeConnection(Connection realConnection)
    {
    	this.setRealConnection(realConnection);
    }
    
    
    public BgeePreparedStatement prepareStatement(String sql) throws SQLException 
    {
    	return new BgeePreparedStatement(getRealConnection().prepareStatement(sql));
    }
    

	/**
	 * @return the {@link #realConnection}
	 */
	private Connection getRealConnection() {
		return realConnection;
	}
	/**
	 * @param realConnection A <code>Connection</code> to set {@link #realConnection} 
	 */
	private void setRealConnection(Connection realConnection) {
		this.realConnection = realConnection;
	}
}
