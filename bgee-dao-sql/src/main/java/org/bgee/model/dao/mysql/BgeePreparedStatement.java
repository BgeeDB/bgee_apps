package org.bgee.model.dao.mysql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.QueryInterruptedException;

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
     * An {@code boolean} set to {@code true} if the method {@code cancel} 
     * was called. A {@code BgeePreparedStatement} should then launch a 
     * {@code QueryInterruptedException} when it realizes that it was canceled. 
     * The exception should not be thrown by the thread asking the cancellation, 
     * but by the thread that was running the query killed. It is a {@code volatile}, 
     * as its only purpose is to be accessed by different threads.
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
        this.setCanceled(false);
    }  
    
    public ResultSet executeQuery() throws SQLException {
        log.entry();
        
        //before launching the query, we check the interruption flag, 
        //maybe another thread used the method cancel and does not want 
        //the query to be executed
        if (this.isCanceled()) {
            //we throw an exception even if the query was not "interrupted" as such, 
            //to let know our thread that it should not pursue its execution.
            throw log.throwing(new QueryInterruptedException());
        }
        
        //now we launch the query. Maybe another thread is requesting cancellation 
        //at this point, just before the query is launched, so that it will not 
        //be actually cancelled. But we cannot do anything to ensure atomicity, 
        //except having this thread to put a lock while launching the query in 
        //another thread, to release the lock while the query is running. Without 
        //such a mechanism, the cancel method would not be able to acquire the lock 
        //before the end of the query...
        try {
            return log.exit(this.getRealPreparedStatement().executeQuery());
        } finally {
            //check that we did not return from the executeQuery method because of 
            //a cancellation
            if (this.isCanceled()) {
                throw log.throwing(new QueryInterruptedException());
            }
        }
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
     * by the thread requesting the cancellation, but by the thread that was running 
     * the query killed.
     * 
     * @throws SQLException if a database access error occurs or this method 
     *                      is called on a closed statement.
     */
    void cancel() throws SQLException {
        //set the interrupted flag before canceling, so that the thread running 
        //the query can see the flag when returning from the execution of the query.
        this.setCanceled(true);
        //here we cannot ensure any atomicity, maybe the other thread will launch 
        //its query just after the cancel method is called, but before it is completed, 
        //so that the query will not be prevented to be run. But there is nothing 
        //we can do, we can not simply use a lock in the other thread, otherwise 
        //we would need to wait for the query to end before entering this block...
        this.getRealPreparedStatement().cancel();
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
     * Returns the {@code boolean} determining if the method {@code cancel} was called. 
     * A {@code BgeePreparedStatement} should then launch a {@code QueryInterruptedException} 
     * when it realizes that it was canceled. The exception should not be thrown 
     * by the thread asking the cancellation, but by the thread that was running 
     * the query killed. It is a {@code volatile}, as its only purpose is to be 
     * accessed by different threads.
     * 
     * @return  A {@code boolean} determining if the method {@code cancel} was called.
     */
    public boolean isCanceled() {
        return this.canceled;
    }
    /**
     * @param canceled the {@link #canceled} to set.
     */
    private void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }	

}
