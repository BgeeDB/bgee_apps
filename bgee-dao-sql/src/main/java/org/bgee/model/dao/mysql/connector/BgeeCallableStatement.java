package org.bgee.model.dao.mysql.connector;

import java.sql.CallableStatement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    BgeeCallableStatement(BgeeConnection connection, CallableStatement realCallableStatement) {
        super(connection, realCallableStatement);
    }


    //***********************************
    // GETTERS
    //***********************************
    /**
     * @return The real {@code java.sql.CallableStatement} that this class wraps.
     */
    public CallableStatement getRealCallableStatement() {
        return (CallableStatement) super.getRealPreparedStatement();
    }

}
