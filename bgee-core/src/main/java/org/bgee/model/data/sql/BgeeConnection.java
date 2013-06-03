package org.bgee.model.data.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeProperties;

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
 * @version Bgee 13, May 2013
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
     * A <code>Map</code> which contains every already existing and available
     * <code>BgeePreparedStatement</code>
     * related to this <code>BgeeConnection</code>.
     * Its size is maintained to a maximum defined in the properties
     * <code>preparedStatmentPoolSize</code> in 
     * <code>BgeeProperties</code>, using
     * a LRU exclusion algorithm.
     * @see BgeeProperties
     */
    private final Map<String,BgeePreparedStatement> preparedStatementPool 
    = new LinkedHashMap<String,BgeePreparedStatement>(BgeeProperties
            .getBgeeProperties().getPrepStatPoolMaxSize(),1F) {

        private static final long serialVersionUID = 6122694567906790867L;

        /**
         * Return <code>true</code> if the maximum capacity of the <code>LinkedHashMap</code>
         * is reached. It is meant to be only automatically called by the
         * <code>PreparedStatementPool put()</code> method to define if an item has 
         * to be dropped to keep the list at the limit size.
         */
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, BgeePreparedStatement> eldest)
        {

            log.entry(eldest);

            if(size() >= BgeeProperties.getBgeeProperties().getPrepStatPoolMaxSize()){
                log.info("Too many prepared statement pooled for connection {}. Drop the LRU PreparedStatement."
                        ,BgeeConnection.this); 
                BgeeConnection.this.reportPoolState(-1);
                
                return log.exit(true);
            }

            return  log.exit(false);
        }

    };
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



        log.exit();
    }

    /**
     * Provide a <code>BgeePreparedStatement</code> for the requested sql.
     * <p>
     * It is fetched in the <code>BgeePreparedStatement</code> pool if 
     * any is available for the requested sql. Else create and return a new one.
     * <p>
     * When an existing <code>BgeePreparedStatement</code> is returned, it is
     * removed from the pool making it unavailable.
     * <p>
     * It hashes the sql <code>String</code> to use it as pool key
     * 
     * @param   sql a <code>String</code> with contains the PreparedStatement sql
     * @return  a <code>BgeePreparedStatement</code> which already existed or newly created
     *  
     * @throws SQLException if a database error occurred or if the method is called on
     * a closed connection
     */
    public BgeePreparedStatement prepareStatement(String sql) throws SQLException 
    {
        log.entry(sql);

        String digestedSql = DigestUtils.sha256Hex(sql) ;

        if(this.preparedStatementPool.containsKey(digestedSql)){
            BgeePreparedStatement ps =  this.preparedStatementPool.remove(digestedSql);
            log.debug("Return a already existing BgeePreparedStatement : {}", ps);  
            log.debug("Remove from the pool the prepared statement {} from connection {} and datasource {}",
                    ps,this,this.getBgeeDataSource());  
            this.reportPoolState(-1);

            return log.exit(ps);
        }
        else{
            BgeePreparedStatement ps = new BgeePreparedStatement(digestedSql,this,
                    this.getRealConnection().prepareStatement(sql));
            log.debug("Return a newly created BgeePreparedStatement : {}", ps);         
            return log.exit(ps);
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
    protected BgeeDataSource getBgeeDataSource() {
        return this.bgeeDataSource;
    }
    /**
     * @return the {@link #preparedStatementPool}
     */
    protected Map<String, BgeePreparedStatement> getPreparedStatementPool() {
        return this.preparedStatementPool;
    }

    /**
     * Add a <code>BgeePreparedStatement</code> to the <code>preparedStatementPool</code>
     * 
     * @param ps   a BgeePreparedStatement to be added to the pool
     * @throws SQLException     If an error occurred while trying to obtain the connection, 
     *                          of if this <code>BgeeDataSource</code> was closed.
     * 
     */
    protected void addToPrepStatPool(BgeePreparedStatement ps) throws SQLException{
        log.entry(ps);

        this.getBgeeDataSource().checkPrepStatPools();

        log.debug("Put in the pool the prepared statement {} from connection {} and datasource {}",
                ps,this,this.getBgeeDataSource());  

        this.preparedStatementPool.put(ps.getId(),ps);

        this.reportPoolState(+1);
        log.exit();
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
     * Remove the last recent used item in the <code>PreparedStatementPool</code>
     */
    protected void cleanPrepStatPools(){

        log.entry();

        log.info("Connection {} has the most pooled prepared statement and has to be cleaned",
                this); 

        String key = this.preparedStatementPool.keySet().iterator().next();

        log.debug("Remove from the pool the prepared statement {} from connection {} and datasource {}",
                this.preparedStatementPool.remove(key),this,this.getBgeeDataSource());

        this.reportPoolState(-1);

        log.exit();

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
    /**
     * Reports the updated number of pooled <code>BgeePreparedStatement</code>
     * to the related <code>BgeeDataSource</code>
     * 
     * @param deltaPrepStatNumber  An <code>int</code> which represents the change
     *                             in the <code>BgeePreparedStatement</code> number
     *                                           
     */    
    private void reportPoolState(int deltaPrepStatNumber) {

        log.entry(deltaPrepStatNumber);

        this.getBgeeDataSource().reportPoolState(deltaPrepStatNumber,this);

        log.exit();

    }
}
