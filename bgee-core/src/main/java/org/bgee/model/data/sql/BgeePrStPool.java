package org.bgee.model.data.sql;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeProperties;

/**
 * Extention of <code>LinkedHashMap</code> to contains <code>BgeePreparedStatement</code>
 * and acts as a LRU (Last Recently Used) pool with a defined capacity
 * <p>
 * Its capacity is automatically set from properties
 * <code>bgee.jdbc.preparedStatementPoolSize</code>
 * @see BgeeProperties
 * 
 * @author Mathieu Seppey
 * @version Bgee 13, May 2013
 * @since Bgee 13
 */
class BgeePrStPool<K, V> extends LinkedHashMap<String, BgeePreparedStatement>
{
    private static final long serialVersionUID = -5534277204853073778L;

    /**
     * <code>Logger</code> of the class. 
     */
    private final static Logger log = LogManager.getLogger(BgeePrStPool.class.getName());

    /**
     * The maximum number of <code>BgeePreparedStatement</code>
     * contained by an instance of <code>BgeePrStPool</code>
     */
    private int maxCapacity;

    /**
     * The <code>BgeeConnection</code> that created the current <code>BgeePrStPool</code>
     */
    private BgeeConnection relatedConnection ;

    /**
     * Constructor used to create a new instance of <code>BgeePrStPool</code>
     * Constructor protected, so that only a {@link BgeeConnection} can create one
     * 
     * @param connection                The <code>BgeeConnection</code> that created
     *                                  the <code>BgeePrStPool</code>.
     *                                                         
     */
    protected BgeePrStPool(BgeeConnection connection)
    {
        super(0, 0.75F, true);
        // placed the log entry after super because it has to be a the first place 
        log.entry(connection);
        this.maxCapacity = BgeeProperties.getBgeeProperties().getPrStPoolMaxSize();
        this.relatedConnection = connection;
        log.exit();
    }

    /**
     * Reports the updated number of <code>BgeePreparedStatement</code> where is it is
     * expected, i.e. at the DataSource static and instanced level and also register the
     * related <code>BgeeConnection</code> and <code>BgeeDataSource</code> as the one with
     * the most pooled <code>BgeePreparedStatement</code> if appropriate.
     * 
     *  @param deltaPrStNumber      An <code>int</code> which represents the change
     *                              in the <code>BgeePreparedStatement</code> number
     *                                           
     */    
    protected void reportPrStPoolState(int deltaPrStNumber){

        log.entry(deltaPrStNumber);
        
        // Update the number of PreparedStatment pooled, 
        // at the global and the DataSource level
        // Note : no need to count pooled prep statement at the connection level
        // as it is simpley represented by the pool object length
        BgeeDataSource.getBgeePrStPoolsTotalSize().addAndGet(deltaPrStNumber);

        this.relatedConnection.getBgeeDataSource().getPrStPoolsDataSourceSize()
        .addAndGet(deltaPrStNumber);

        // Update the connection and the datasource which have the most prep st pooled
        
        // First, fetch all informations previously in these variables
        Enumeration<Integer> size;
        Enumeration<BgeeConnection> bgConWithMaxPrSt;
        Enumeration<BgeeDataSource> bgDSWithMaxPrSt;

        size = this.relatedConnection.getBgeeDataSource().getBgeeConnWithMaxPrStPooled()
                .elements();
        bgConWithMaxPrSt = this.relatedConnection.getBgeeDataSource()
                .getBgeeConnWithMaxPrStPooled().keys();

        // Replace it with the related connection if
        // - no previous entry, or
        // - has a bigger number of prep st pooled than the current in the variable
        // - is already the current in the variable, to update its size 
        if(! bgConWithMaxPrSt.hasMoreElements() || 
                (this.size() > size.nextElement() || bgConWithMaxPrSt.nextElement() 
                        == this.relatedConnection)){
            this.relatedConnection.getBgeeDataSource()
            .setBgeeConnWithMaxPrStPooled(this.relatedConnection,new Integer(this.size())); 
            log.debug("Register the current connection as the one with the " +
                    "most prepared statements pooled : {} with {} items",
                    this.relatedConnection.toString(),this.size());    
        }

        size = BgeeDataSource.getBgeeDataSourceWithMaxPrStPooled().elements();
        bgDSWithMaxPrSt = BgeeDataSource.getBgeeDataSourceWithMaxPrStPooled().keys();

        // Replace it with the related datasource if
        // - no previous entry, or
        // - has a bigger number of prep st pooled than the current in the variable
        // - is already the current in the variable, to update its size 
        if(! bgDSWithMaxPrSt.hasMoreElements() || 
                (this.relatedConnection.getBgeeDataSource().getPrStPoolsDataSourceSize()
                        .intValue() > size.nextElement() || bgDSWithMaxPrSt.nextElement() 
                        == this.relatedConnection.getBgeeDataSource())){
            BgeeDataSource.setBgeeDataSourceWithMaxPrStPooled(this.relatedConnection
                    .getBgeeDataSource(),this.relatedConnection.getBgeeDataSource()
                    .getPrStPoolsDataSourceSize().intValue());
            log.debug("Register the current datasource as the one with the " +
                    "most prepared statements pooled : {} with {} items",
                    this.relatedConnection.getBgeeDataSource().toString(),
                    this.relatedConnection.getBgeeDataSource().getPrStPoolsDataSourceSize()); 

        }
        
        log.exit();

    }

    /**
     * Remove the first entry of the <code>BgeePrStPool</code> if its maximum capacity 
     * is reached and always return <code>false</code>. It is meant to be automatically
     * called by the <code>BgeePrStPool put()</code> method.
     * <p>
     * The standard behavior of this function would be not to modify the map by itself but 
     * to return <code>true</code> if it has to be and let the native implementation handle it.
     * However, it behaves this way to keep control on the <code>remove()</code> function
     * and its impact on the Pooled <code>PreparedStatment</code> count.
     */
    @Override
    protected boolean removeEldestEntry(Map.Entry<String, BgeePreparedStatement> eldest)
    {
        
        log.entry(eldest);
        
        if(size() >= this.maxCapacity){
            log.info("Too many prepared statement pool for connection {}",
                    this.relatedConnection.toString()); 
            this.remove(this.keySet().iterator().next());
        }
        log.exit();
        return false;
    }
    @Override
    public BgeePreparedStatement remove(Object key){

        log.entry(key);
        
        log.debug("Remove from pool the prepared statement {} " +
                "from connection {} and datasource {}",
                this.get(key),this.relatedConnection.toString(),
                this.relatedConnection.getBgeeDataSource().toString()); 

        BgeePreparedStatement ret = super.remove(key);

        this.reportPrStPoolState(-1);

        log.exit();
        
        return ret ;
    }
    @Override
    public BgeePreparedStatement put(String key, BgeePreparedStatement value){

        log.entry(key,value);
        
        BgeeDataSource.checkAndCleanBgeePsStPools();

        BgeePreparedStatement ret = super.put(key, value);


        log.debug("Put in the pool the prepared statement {} " +
                "from connection {} and datasource {}",
                value,this.relatedConnection.toString(),
                this.relatedConnection.getBgeeDataSource().toString());  

        this.reportPrStPoolState(+1);

        log.exit();
        
        return ret ;

    }
}    