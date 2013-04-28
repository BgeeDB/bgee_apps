package org.bgee.model.data.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Abstraction layer to use a <code>java.sql.Connection</code>. 
 * This class is used to connect to the Bgee database. 
 * It provides only the functionalities of the <code>java.sql.Connection</code> interface 
 * that are used in the Bgee project. So it does not implement the actual interface.
 * <p>
 * It implements the <code>AutoCloseable</code> interface so that it can be used in a 
 * <code>try-with-resources</code> statement.
 * 
 * @author Frederic Bastian
 * @author Mathieu Seppey
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
     * A <code>HashMap</code> which contains every available <code>BgeePreparedStatement</code>
     * related to this <code>BgeeConnection</code>
     */
    private final HashMap<String,BgeePreparedStatement> preparedStatementPool;
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
     * 							on this <code>BgeeConnection</code>).
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
        this.preparedStatementPool = new HashMap<String,BgeePreparedStatement>();

        log.exit();
    }

    /**
     * Provide a <code>BgeePreparedStatement</code> fetched in the pool if 
     * any available for the requested sql. Else create and return a new one.
     * 
     * When an existing <code>BgeePreparedStatement</code> is returned, it is
     * removed from the pool making it unavailable.
     * 
     * @param sql a <code>String</code> with contains the PreparedStatement sql
     * @return a <code>BgeePreparedStatement</code> which already existed or newly created
     *  
     * @throws SQLException
     */
    public BgeePreparedStatement prepareStatement(String sql) throws SQLException 
    {
        log.entry(sql);

        if(this.preparedStatementPool.containsKey(sql)){
            return log.exit(
                    this.preparedStatementPool.remove(sql));
        }
        else{
            return log.exit(new BgeePreparedStatement(this, sql));
        }
    }


    /**
     * @return the {@link #realConnection}
     */
    protected Connection getRealConnection() {
        return this.realConnection;
    }
    /**
     * @return the {@link #id}
     */
    protected String getId() {
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
    /**
     * Retrieves whether this <code>BgeeConnection</code> object has been closed. 
     * A <code>BgeeConnection</code> is closed if the method {@link #close()} 
     * has been called on it, or on its container <code>BgeeDataSource</code>.
     *  
     * @return 	<code>true</code> if this <code>BgeeConnection</code> object is closed; 
     * 			<code>false</code> if it is still open.
     * @throws SQLException		if a database access error occurs
     */
    public boolean isClosed() throws SQLException
    {
        return this.getRealConnection().isClosed();
    }

    protected HashMap<String,BgeePreparedStatement> getPreparedStatementPool()
    {
        return this.preparedStatementPool;
    }
    
}
