package org.bgee.model.dao.mysql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

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
public final class BgeePreparedStatement implements AutoCloseable
{
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
     * A {@code String} that represents the unique 
     * {@code BgeePreparedStatement} identification
     */
    private final String id;
    /**
     * Default constructor private, should not be used. 
     */
    @SuppressWarnings("unused")
    private BgeePreparedStatement() {
        this(null, null,null);
    }
    /**
     * Constructor used to provide the real {@code java.sql.PreparedStatement} 
     * that this class wraps, and the {@code BgeeConnection} used to obtain 
     * this {@code BgeePreparedStatement}, for notification purpose.
     * <p>
     * Constructor package-private, so that only a {@link BgeeConnection} can provide 
     * a {@code BgeePreparedStatement}.
     *  
     * @param id                        A {@code String} that represent the unique id
     *                                  of the {@code BgeePreparedStatement}. It has
     *                                  to be the hashed sql passed to the real
     *                                  {@code PreparedStatement}
     * @param connection				The {@code BgeeConnection} that was used 
     * 									to obtain this {@code BgeePreparedStatement}.
     * @param realPreparedStatement     The {@code java.sql.PreparedStatement} 
     *                                  that this class wraps
     *                                                         
     */
    BgeePreparedStatement(String id,BgeeConnection connection,
            PreparedStatement realPreparedStatement)
    {
        this.bgeeConnection = connection;
        this.realPreparedStatement = realPreparedStatement;
        this.id = id;
    }    

    /**
     * @return The real {@code java.sql.PreparedStatement} that this class wraps.
     */
    public PreparedStatement getRealPreparedStatement() {
        return realPreparedStatement;
    }
    /**
     * @return A {@code String} that represents the unique 
     * {@code BgeePreparedStatement} identification
     */
    protected String getId(){
        return this.id;
    }
    /**
     * This method put back the {@code BgeePreparedStatement}
     * in the PreparedStatement Pool
     * instead of actually closing it and the underlying real {@code PreparedStatement}.
     * 
     * It clears the parameters of the statement before, 
     * 
     * @throws SQLException 
     */
    @Override
    public void close() throws SQLException {
        //TODO
    }	

}
