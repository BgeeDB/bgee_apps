package org.bgee.model.dao.mysql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Abstraction layer to use a {@code java.sql.PreparedStatement}. 
 * <p> 
 * It provides only the functionalities of the {@code java.sql.PreparedStatement} interface 
 * that are used in the Bgee project. So it does not implement the actual interface.
 * <p>
 * It implements the {@code AutoCloseable} interface so that it can be used in a 
 * {@code try-with-resources} statement.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public final class BgeePreparedStatement implements AutoCloseable {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(BgeePreparedStatement.class.getName());
    
    /**
     * The {@code BgeeConnection} that was used 
     * to obtain this {@code BgeePreparedStatement}.
     * Used for notification purpose. 
     */
    private final BgeeConnection bgeeConnection;
    /**
     * The real {@code java.sql.PreparedStatement} that this class wraps.
     */
    private final PreparedStatement realPreparedStatement;
    /**
     * A {@code boolean} set to {@code true} when a query is currently run  
     * by this {@code BgeePreparedStatement}. It is set to {@code true} at the 
     * beginning of the method {@code executeQuery}, and to {@code false} 
     * at the end. This is used to determine whether the method {@code cancel}
     * might be called on this {@code BgeePreparedStatement}. This is why it is 
     * an {@code volatile}, its only purpose is to be accessed by different threads.
     */
    private volatile boolean runningQuery;
    /**
     * A {@code boolean} set to {@code true} if the method {@code cancel} was called. 
     * A {@code BgeePreparedStatement} should then launch a {@code QueryInterruptedException} 
     * when it realizes that it was canceled. The exception should not be thrown 
     * by the thread asking the cancellation, but by the thread that was running 
     * the query killed. This {@code boolean} is volatile as it will be accessed 
     * by different threads.
     */
    private volatile boolean canceled;
    /**
     * Default constructor private, should not be used. 
     */
    @SuppressWarnings("unused")
    private BgeePreparedStatement() {
        this(null, null);
    }
    /**
     * Constructor used to provide the real {@code java.sql.PreparedStatement} 
     * that this class wraps, and the {@code BgeeConnection} used to obtain 
     * this {@code BgeePreparedStatement}, for notification purpose.
     * <p>
     * Constructor package-private, so that only a {@link BgeeConnection} can provide 
     * a {@code BgeePreparedStatement}.
     * 
     * @param connection				The {@code BgeeConnection} that was used 
     * 									to obtain this {@code BgeePreparedStatement}.
     * @param realPreparedStatement     The {@code java.sql.PreparedStatement} 
     *                                  that this class wraps
     *                                                         
     */
    BgeePreparedStatement(BgeeConnection connection, 
            PreparedStatement realPreparedStatement) {
        this.bgeeConnection = connection;
        this.realPreparedStatement = realPreparedStatement;
        this.setRunningQuery(false);
        this.setCanceled(false);
    }    

    /**
     * Close the real {@code PreparedStatement} that this class wraps, 
     * and notify of the closing the {@code BgeeConnection} used to obtain 
     * this {@code BgeePreparedStatement}.
     * 
     * @throws SQLException     If the real {@code PreparedStatement} that this class 
     *                          wraps throws a {@code SQLException} when closing.  
     */
    @Override
    public void close() throws SQLException {
        log.entry();
        try {
            this.getRealPreparedStatement().close();
        } catch (SQLException e) {
            throw log.throwing(e);
        } finally {
            this.bgeeConnection.statementClosed(this);
        }
        log.exit();
    }
    
    /**
     * Cancels this Statement object. This method can be used by one thread 
     * to cancel a statement that is being executed by another thread. 
     * A {@code BgeePreparedStatement} should then launch a {@code QueryInterruptedException} 
     * when it realizes that it was canceled. The exception should not be thrown 
     * by the thread asking the cancellation, but by the thread that was running 
     * the query killed.
     * 
     * @throws SQLException if a database access error occurs or this method 
     *                      is called on a closed statement.
     */
    void cancel() throws SQLException {
        if (this.isRunningQuery()) {
            //we do not use any lock for atomicity. It means that this running 
            //query could actually be completed when we will be calling cancel. 
            //As a result, no QueryInterruptedException would be thrown.
            //But the BgeeConnection and MySQLDAOManager will then be closed, 
            //forbidding to the thread doing the queries to pursue (this method 
            //is called when kill is called on the BgeeConnection, because 
            //killDAOManager is called on the MySQLDAOManager)
            to continue
        }
    }
    
    //***********************************
    // GETTERS/SETTERS
    //***********************************
    /**
     * @return The real {@code java.sql.PreparedStatement} that this class wraps.
     */
    public PreparedStatement getRealPreparedStatement() {
        return realPreparedStatement;
    }
    
    /**
     * Returns the {@code boolean} determining if a query is currently run  
     * by this {@code BgeePreparedStatement}. It is set to {@code true} at the 
     * beginning of the method {@code executeQuery}, and to {@code false} 
     * at the end. This is used to determine whether the method {@code cancel}
     * might be called on this {@code BgeePreparedStatement}. It is declared 
     * {@code volatile}, its only purpose is to be accessed by different threads.
     * 
     * @return  a {@code boolean} determining if a query is currently run.
     */
    public boolean isRunningQuery() {
        return runningQuery;
    }
    /**
     * @param runningQuery the {@link #runningQuery} to set.
     */
    private void setRunningQuery(boolean runningQuery) {
        this.runningQuery = runningQuery;
    }
    
    /**
     * Returns the {@code boolean} determining if the method {@code cancel} was called. 
     * A {@code BgeePreparedStatement} should then launch a {@code QueryInterruptedException} 
     * when it realizes that it was canceled. The exception should not be thrown 
     * by the thread asking the cancellation, but by the thread that was running 
     * the query killed. This {@code boolean} is volatile as it will be accessed 
     * by different threads.
     * 
     * @return  A {@code boolean} determining if the method {@code cancel} was called.
     */
    public boolean isCanceled() {
        return canceled;
    }
    /**
     * @param canceled the {@link #canceled} to set.
     */
    private void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }	

}
