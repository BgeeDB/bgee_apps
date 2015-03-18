package org.bgee.model.dao.mysql.connector;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
public class BgeeConnection implements AutoCloseable {
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
     * <p>
     * This {@code Set} is a concurrent {@code Set} backed by a {@code ConcurrentHashMap}. 
     * This is because a {@code BgeeConnection} can be killed by another thread, 
     * so we need to make sure it would see the statements to cancel. 
     */
    private final Set<BgeePreparedStatement> preparedStatements;
    
    /**
     * A {@code boolean} that is {@code true} if a transaction has been started, 
     * and has not yet been commit or rollback.
     */
    private boolean ongoingTransaction;
    
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
        this.preparedStatements = Collections.newSetFromMap(
                new ConcurrentHashMap<BgeePreparedStatement, Boolean>());
        this.setOngoingTransaction(false);

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
    //we return an open PreparedStatement on purpose, this is not a resource leak
    @SuppressWarnings("resource")
    public BgeePreparedStatement prepareStatement(String sql) throws SQLException {
        log.entry(sql);
        BgeePreparedStatement bgeeStmt = new BgeePreparedStatement(this, 
                this.getRealConnection().prepareStatement(sql));
        this.preparedStatements.add(bgeeStmt);
        return log.exit(bgeeStmt);
    }
    
    /**
     * Creates a {@code BgeeCallableStatement} object for for calling database stored 
     * procedures. 
     * 
     * @param sql   an SQL statement that may contain one or more '?' parameter 
     *              placeholders. Typically this statement is specified using JDBC call 
     *              escape syntax.
     * @return      a new {@code BgeeCallableStatement} object containing the pre-compiled 
     *              SQL statement.
     * @throws      SQLException if a database access error occurs or this method is 
     *              called on a closed connection.
     */
    //we return an open CallableStatement on purpose, this is not a resource leak
    @SuppressWarnings("resource")
    public BgeeCallableStatement prepareCall(String sql) throws SQLException {
        log.entry(sql);
        BgeeCallableStatement bgeeCallStmt = new BgeeCallableStatement(this, 
                this.getRealConnection().prepareCall(sql));
        this.preparedStatements.add(bgeeCallStmt);
        return log.exit(bgeeCallStmt);
    }
    
    /**
     * Starts a transaction with default isolation level. If {@code close} is called 
     * before this transaction was commit, it will be rollback. If a transaction 
     * is already ongoing, an {@code IllegalStateException} is thrown. It will 
     * then be the responsibility of the caller to decide whether the ongoing 
     * transaction should be rollback.
     * 
     * @throws SQLException If an error occurred while setting {@code autoCommit} 
     *                      to {@code false}.
     * @throws IllegalStateException    If a transaction is already ongoing.
     */
    public void startTransaction() throws SQLException, IllegalStateException {
        log.entry();
        if (this.isOngoingTransaction()) {
            throw log.throwing(new IllegalStateException("A transaction is already ongoing, " +
            		"cannot start a new one"));
        }
        this.getRealConnection().setAutoCommit(false);
        this.setOngoingTransaction(true);
        log.exit();
    }
    
    /**
     * Commit the current transaction, and set {@code autoCommit} back to {@code true}.
     * If no transaction was ongoing, an {@code IllegalStateException} is thrown.
     * 
     * @throws SQLException             If an error occurred while the transaction 
     *                                  was commit, or {@code autoCommit} set to 
     *                                  {@code true}.
     * @throws IllegalStateException    If no transaction was ongoing.
     */
    public void commit() throws SQLException, IllegalStateException {
        log.entry();
        if (!this.isOngoingTransaction()) {
            throw log.throwing(new IllegalStateException("Try to commit a transaction, " +
                    "but there was no ongoing transactions"));
        }
        this.getRealConnection().commit();
        this.getRealConnection().setAutoCommit(true);
        this.setOngoingTransaction(false);
        log.exit();
    }

    /**
     * Rollback the current transaction, and set {@code autoCommit} back to {@code true}.
     * If no transaction was ongoing, an {@code IllegalStateException} is thrown.
     * 
     * @throws SQLException             If an error occurred while the transaction 
     *                                  was rollback, or {@code autoCommit} set to 
     *                                  {@code true}.
     * @throws IllegalStateException    If no transaction was ongoing.
     */
    public void rollback() throws SQLException, IllegalStateException {
        log.entry();
        if (!this.isOngoingTransaction()) {
            throw log.throwing(new IllegalStateException("Try to rollback a transaction, " +
                    "but there was no ongoing transactions"));
        }
        this.getRealConnection().rollback();
        this.getRealConnection().setAutoCommit(true);
        this.setOngoingTransaction(false);
        log.exit();
    }
    
    /**
     * @return  A {@code boolean} that is {@code true} if a transaction has been 
     *          started, and has not yet been commit or rollback.
     */
    public boolean isOngoingTransaction() {
        return ongoingTransaction;
    }
    /**
     * @param ongoingTransaction    {@code boolean} to set {@link #ongoingTransaction}.
     */
    private void setOngoingTransaction(boolean ongoingTransaction) {
        this.ongoingTransaction = ongoingTransaction;
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
        this.preparedStatements.remove(stmt);
        log.exit();
    }
    
    /**
     * @return  an {@code int} representing the current number of 
     * {@code BgeePreparedStatement}s held by this {@code BgeeConnection}.
     */
    int getStatementCount() {
        return this.preparedStatements.size();
    }

    /**
     * Close the real {@code Connection} that this class wraps, rollback 
     * any ongoing transaction by calling {@link #rollback()}, 
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
            //rollback any ongoing transaction
            if (this.isOngoingTransaction()) {
                this.rollback();
            }
            //get a shallow copy of preparedStatements, because closing the statement 
            //will modify the collection
            Set<BgeePreparedStatement> shallowCopy = 
                    new HashSet<BgeePreparedStatement>(this.preparedStatements);
            for (BgeePreparedStatement stmt: shallowCopy) {
                //this method will also remove the statement from preparedStatements
                stmt.close();
            }
            this.getRealConnection().close();
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
      //get a shallow copy of preparedStatements, because closing the statement 
        //will modify the collection
        Set<BgeePreparedStatement> shallowCopy = 
                new HashSet<BgeePreparedStatement>(this.preparedStatements);
        for (BgeePreparedStatement stmt: shallowCopy) {
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
