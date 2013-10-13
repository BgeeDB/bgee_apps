package org.bgee.model.dao.mysql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Abstraction layer to use a {@code java.sql.Connection}. 
 * This class is used to connect to the Bgee database. 
 * It provides only the functionalities of the {@code java.sql.Connection} interface 
 * that are used in the Bgee project. So it does not implement the actual interface.
 * <p>
 * It implements the {@code AutoCloseable} interface so that it can be used in a 
 * {@code try-with-resources} statement.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public final class BgeeConnection implements AutoCloseable {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(BgeeConnection.class.getName());
    /**
     * The {@code MySQLDAOManager} used to obtain this {@code BgeeConnection}. 
     * Used for notifications purpose. 
     */
    private final MySQLDAOManager manager;
    /**
     * The real {@code java.sql.Connection} that this class wraps.
     */
    private final Connection realConnection;
    /**
     * A {@code String} representing an identifier 
     * for this {@code BgeeConnection} object. It is used for the connection 
     * to be tracked by the container {@code MySQLDAOManager} object 
     * (see {@link #manager}).
     */
    private final String id;
    
    /**
     * A <code>Set</code> to store all {@code BgeePreparedStatement}s provided by this 
     * {@code BgeeConnection}, and not yet closed. This will allow to close them if 
     * this {@code BgeeConnection} is closed, or to cancel a running statement 
     * when {@link #kill()} is called.
     */
    private final Set<BgeePreparedStatement> preparedStatements;
    
    /**
     * Default constructor private, should not be used. 
     */
    @SuppressWarnings("unused")
    private BgeeConnection()
    {
        this(null, null, null);
    }
    /**
     * Constructor providing the {@code MySQLDAOManager} object used to instantiate 
     * this {@code BgeeConnection} (for notifications purpose), the real 
     * {@code java.sql.Connection} that this class wraps, and the {@code id} 
     * to use to identify and track this {@code BgeeConnection}.
     * 
     * Constructor package-private, so that only a {@link MySQLDAOManager} can instantiate 
     * a {@code BgeeConnection}.
     * @param manager    		The {@code MySQLDAOManager} used to obtain 
     * 							this connection. Is used for notifications purpose 
     * 							(notably when {@code close()} is called 
     * 							on this {@code BgeeConnection}).
     * @param realConnection 	The {@code java.sql.Connection} that this class wraps
     * @param id 				A {@code String} representing the ID of this 
     * 							{@code BgeeConnection}, used by {@code manager} 
     * 							to track the connection.
     */
    BgeeConnection(MySQLDAOManager manager, Connection realConnection, 
            String id)
    {
        log.entry(manager, realConnection, id);

        this.manager        = manager;
        this.realConnection = realConnection;
        this.id             = id;
        this.preparedStatements = new HashSet<BgeePreparedStatement>();

        log.exit();
    }

    /**
     * @return The real {@code java.sql.Connection} that this class wraps.
     */
    public Connection getRealConnection() {
        return this.realConnection;
    }
    /**
     * Returns the ID of this {@code BgeeConnection} object. It is used for the 
     * connection to be tracked by the container {@code MySQLDAOManager}. 
     * @return  the {@code String} representing this {@code BgeeConnection} ID. 
     */
    String getId() {
        return this.id;
    }
    
    /**
     * Creates a {@code BgeePreparedStatement} object for sending parameterized SQL 
     * statements to the database. 
     * 
     * @param   sql an SQL statement that may contain one or more '?' IN 
     *          parameter placeholders
     * @return  a new {@code BgeePreparedStatement} object containing the pre-compiled 
     *          SQL statement.
     * @throws  SQLException if a database access error occurs or this method is called 
     *          on a closed connection.
     */
    public BgeePreparedStatement prepareStatement(String sql) throws SQLException {
        log.entry(sql);
        BgeePreparedStatement bgeeStmt = new BgeePreparedStatement(this, 
                this.getRealConnection().prepareStatement(sql));
        //we synchronize over this.preparedStatements, to establish a happens-before 
        //relation for methods that can be used by other threads (e.g., kill), 
        //and that will use the same lock, for atomicity. This is not because we expect 
        //this method to be used by different thread, otherwise we could simply 
        //have used a Set backed by a ConcurrentMap.
        synchronized(this.preparedStatements) {
            this.preparedStatements.add(bgeeStmt);
        }
        return log.exit(bgeeStmt);
    }
    
    /**
     * Notification that a {@code BgeePreparedStatement}, held by this {@code BgeeConnection}, 
     * has been closed. 
     * 
     * @param stmt  The {@code BgeePreparedStatement} that was closed. 
     */
    void statementClosed(BgeePreparedStatement stmt)
    {
        log.entry(stmt);
        log.debug("Releasing BgeePreparedStatement {}", stmt);
        //we synchronize over this.preparedStatements, to establish a happens-before 
        //relation for methods that can be used by other threads (e.g., kill), 
        //and that will use the same lock, for atomicity. This is not because we expect 
        //this method to be used by different thread, otherwise we could simply 
        //have used a Set backed by a ConcurrentMap.
        synchronized(this.preparedStatements) {
            this.preparedStatements.remove(stmt);
        }
        log.exit();
    }

    /**
     * Close the real {@code Connection} that this class wraps, 
     * and notify of the closing the {@code MySQLDAOManager} used to obtain 
     * this {@code BgeeConnection}.
     * 
     * @throws SQLException 	If the real {@code Connection} that this class wraps
     * 							throws a {@code SQLException} when closing.  
     */
    @Override
    public void close() throws SQLException {
        log.entry();
        try {
            synchronized(this.preparedStatements) {
                //get a shallow copy of preparedStatements, because closing the statement 
                //will modify the collection
                Set<BgeePreparedStatement> shallowCopy = 
                        new HashSet<BgeePreparedStatement>(this.preparedStatements);
                for (BgeePreparedStatement stmt: shallowCopy) {
                    //this method will also remove the statement from preparedStatements
                    stmt.close();
                }
                this.getRealConnection().close();
            }
        } catch (SQLException e) {
            throw log.throwing(e);
        } finally {
            this.manager.connectionClosed(this.getId());
        }
        log.exit();
    }    
    /**
     * Retrieves whether this {@code BgeeConnection} object has been closed. 
     * A {@code BgeeConnection} is closed if the method {@link #close()} 
     * has been called on it, or on its container {@code MySQLDAOManager}.
     *  
     * @return 	{@code true} if this {@code BgeeConnection} object is closed; 
     * 			{@code false} if it is still open.
     * @throws SQLException		if a database access error occurs
     */
    public boolean isClosed() throws SQLException {
        return this.getRealConnection().isClosed();
    }
    
    /**
     * Call the method {@code cancel} on any {@code BgeePreparedStatement} provided 
     * by this {@code BgeeConnection}, currently running a query, and then call 
     * {@link #close()}. This method is used to implement 
     * {@link MySQLDAOManager#killDAOManager()}. It has to be called by a different thread 
     * than the one running the queries.
     * 
     * @throws SQLException If an error occurred while canceling a {@code PreparedStatement}.
     */
    void kill() throws SQLException {
        synchronized(this.preparedStatements) {
            for (BgeePreparedStatement stmt: this.preparedStatements) {
                try {
                    stmt.cancel();
                } finally {
                    //we remove this statement "manually", because calling its close 
                    //method to remove it will also call close on the real underlying 
                    //statement, which is useless (cancel will already close it)
                    this.statementClosed(stmt);
                }
            }
            this.close();
        }
    }
}
