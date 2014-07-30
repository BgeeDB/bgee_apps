package org.bgee.model.dao.mysql.connector;

import java.sql.CallableStatement;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.QueryInterruptedException;

/**
 * Abstraction layer to use a {@code java.sql.CallableStatement}. 
 * <p> 
 * It provides only the functionalities of the {@code java.sql.CallableStatement} interface 
 * that are used in the Bgee project. So it does not implement the actual interface.
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @since Bgee 13
 */

public class BgeeCallableStatement extends BgeePreparedStatement {
    
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(BgeeCallableStatement.class.getName());
    
    /**
     * The real {@code java.sql.CallableStatement} that this class wraps.
     */
    private final CallableStatement realCallableStatement;

    /**
     * Default constructor private, should not be used. 
     */
    @SuppressWarnings("unused")
    private BgeeCallableStatement() {
        this(null, null);
    }

    /**
     * Constructor used to provide the real {@code java.sql.CallableStatement} 
     * that this class wraps, and the {@code BgeeConnection} used to obtain 
     * this {@code BgeeCallableStatement}, for notification purpose.
     * <p>
     * Constructor package-private, so that only a {@link BgeeConnection} can provide 
     * a {@code BgeeCallableStatement}.
     * 
     * @param connection                The {@code BgeeConnection} that was used 
     *                                  to obtain this {@code BgeeCallableStatement}.
     * @param realPreparedStatement     The {@code java.sql.PreparedStatement} 
     *                                  that this class wraps
     *                                                         
     */
    BgeeCallableStatement(BgeeConnection connection, 
            CallableStatement realCallableStatement) {
      super(connection, null);
        this.realCallableStatement = realCallableStatement;
    }

    /**
     * Delegated to {@link java.sql.CallableStatement#setString(int, String)}.
     * 
     * @param parameterIndex    {@code int} that is the index of the parameter to set.
     * @param x                 {@code String} that is the value of the parameter 
     *                          to set.
     * @throws SQLException     if parameterIndex does not correspond to a parameter 
     *                          marker in the SQL statement; if a database access error 
     *                          occurs or this method is called on a closed PreparedStatement.
     */
    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        this.getRealCallableStatement().setString(parameterIndex, x);
    }
    
    @Override
    public int executeUpdate() throws SQLException {
        log.entry();
        //TODO: DRY. We just copied pasted here, because this method is used 
        //for insertion methods only, that are only used by the pipeline 
        //(it's not a good reason, I know...)
        
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
            return log.exit(this.getRealCallableStatement().executeUpdate());
        } finally {
            this.setExecuted(true);
            //check that we did not return from the executeQuery method because of 
            //a cancellation
            if (this.isCanceled()) {
                throw log.throwing(new QueryInterruptedException());
            }
        }
    }

    /**
     * Close the real {@code CallableStatement} that this class wraps, 
     * and notify of the closing the {@code BgeeConnection} used to obtain 
     * this {@code CallableStatement}.
     * 
     * @throws SQLException     If the real {@code CallableStatement} that this class 
     *                          wraps throws a {@code SQLException} when closing.  
     */
    @Override
    public void close() throws SQLException {
        log.entry();
        try {
            this.getRealCallableStatement().close();
        } catch (SQLException e) {
            throw log.throwing(e);
        } finally {
            this.getBgeeConnection().statementClosed(this);
        }
        log.exit();
    }

    //***********************************
    // GETTERS
    //***********************************
    /**
     * @return The real {@code java.sql.CallableStatement} that this class wraps.
     */
    public CallableStatement getRealCallableStatement() {
        return this.realCallableStatement;
    }

}
