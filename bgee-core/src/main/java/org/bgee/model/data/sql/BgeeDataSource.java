package org.bgee.model.data.sql;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeProperties;

/**
 * Abstraction layer to obtain {@link BgeeConnection}s. 
 * <p>
 * This class first tries to load a <code>DataSource</code> 
 * from an <code>InitialContext</code> (most likely provided by Tomcat 
 * when this application is used in a webapp context), to obtain <code>Connection</code>s from. 
 * If the <code>DataSource</code> cannot be obtained, it means that the application 
 * is most likely used in a standalone context, and a classic <code>DriverManager</code> 
 * approach is used to obtain <code>Connection</code>s. 
 * In the standalone context, the parameters to establish a connection are retrieved from 
 * the {@link org.bgee.model.BgeeProperties BgeeProperties}.
 * <p>
 * Any calls, <b>inside a thread</b>, to the method {@link #getBgeeDataSource()}, 
 * will return the same <code>BgeeDataSource</code> instance. 
 * An exception is if you call this method 
 * after having called {@link #close()} or {@link #closeAll()}.
 * In that case, <code>getBgeeDataSource()</code> 
 * would return a new <code>BgeeDataSource</code> instance. It is not a big deal.
 * <p>
 * Following the first call <b>on a given <code>BgeeDataSource</code> instance</b> 
 * to a method to obtain a connection, 
 * with a given <code>username</code> and a given <code>password</code>,
 * (meaning, using {@link #getConnection()} or {@link #getConnection(String, String)}), 
 * this class obtains a <code>Connection</code>, either from a <code>DataSource</code> 
 * or a <code>Driver</code>, depending on the context, and return it. 
 * This <code>BgeeDataSource</code> object stores these <code>BgeeConnection</code>s, 
 * so that any consecutive call to obtain a <code>Connection</code> 
 * with the same <code>username</code> and <code>password</code>, 
 * will return the same <code>BgeeConnection</code> object, 
 * without trying to obtain a new <code>Connection</code> from a <code>DataSource</code> 
 * or a <code>Driver</code>. 
 * <p>
 * An exception is that when a <code>BgeeConnection</code> is closed, 
 * this <code>BgeeDataSource</code> release it, 
 * so that a consecutive call to obtain a <code>Connection</code> 
 * with the same <code>username</code> and <code>password</code>, 
 * will obtain a new <code>Connection</code> from a <code>DataSource</code> 
 * or a <code>Driver</code> again.
 *  
 * @author Frederic Bastian
 * @version Bgee 13, Mar 2013
 * @since Bgee 13
 */
public class BgeeDataSource 
{
	//****************************
	// CLASS ATTRIBUTES
	//****************************
	/**
     * A <code>ConcurrentMap</code> used to store all <code>BgeeDataSource</code>s, 
     * associated with the ID of the thread that requested it. 
     * <p>
     * This <code>Map</code> is used to provide a unique and independent 
     * <code>BgeeDataSource</code> instance to a thread: 
     * a <code>BgeeDataSource</code> is added to this <code>Map</code> 
     * when {@link #getBgeeDataSource()} is called, if the thread ID is not already 
     * present in the <code>keySet</code> of the <code>Map</code>. 
     * Otherwise, the already stored <code>BgeeDataSource</code> is returned. 
     * <p>
     * If a <code>ThreadLocal</code> was not used, it is because 
     * this <code>Map</code> is used by other treads, 
     * for instance when a <code>ShutdownListener</code> 
     * want to properly close all <code>BgeeDataSource</code>s; 
     * or when a thread performing monitoring of another thread want to interrupt it.
     * <p>
     * A <code>BgeeDataSource</code> is removed from this <code>Map</code> 
     * when the method {@link #close()} is called. 
     */
    private static final ConcurrentMap<Long, BgeeDataSource> bgeeDataSources = 
    		new ConcurrentHashMap<Long, BgeeDataSource>();
	/**
	 * The real <code>DataSource</code> that this class wraps. 
	 * <code>null</code> if no <code>DataSource</code> could be obtained 
	 * from <code>InitialContext</code> (see static initializer).
	 */
	private static final DataSource realDataSource;
	/**
	 * A <code>boolean</code> that is <code>true</code> if an error occurred 
	 * while trying to register a <code>Driver</code> during the static initialization. 
	 * It is used because no <code>SQLException</code> can be thrown 
	 * from the static initializer. This <code>boolean</code> will be thus checked 
	 * when trying to obtain a <code>Connection</code> from the <code>DriverManager</code>, 
	 * and a <code>SQLException</code> thrown if this <code>boolean</code> 
	 * is <code>true</code>.
	 */
	private static final boolean driverRegistrationError;
	/**
	 * <code>Logger</code> of the class. 
	 */
	private final static Logger log = LogManager.getLogger(BgeeDataSource.class.getName());
	
	//****************************
	// INSTANCE ATTRIBUTES
	//****************************
	/**
	 * A <code>Map</code> to store the opened <code>BgeeConnection</code>s 
	 * provided by this <code>BgeeDataSource</code>, associated to their <code>ID</code> 
	 * (see {@link BgeeConnection#getId()}). 
	 */
	private Map<String, BgeeConnection> openConnections;
	

	//****************************
	// CLASS METHODS
	//****************************
	/**
	 * Static initializer, initialize {@link realDataSource}, 
	 * or try to register a <code>Driver</code>.
	 */
	static {
		log.entry();
		log.info("Initializing BgeeDataSource...");
		
		DataSource dataSourceTemp = null;
		boolean driverRegErrTemp = false; 
		try {
			Context ctx = new InitialContext();
			dataSourceTemp = (DataSource) ctx.lookup("java:comp/env/jdbc/bgeedatasource");
			//here it is likely that we are in a webapp context
			log.info("DataSource obtained from InitialContext using JNDI");
		} catch (NamingException e) {
			//here it is likely that we are in a standalone context, 
			//then we don't need a DataSource and will rely on a classic DriverManager
			//if the name of a driver was provide, try to load it, 
			//otherwise, only the URL to connect will be used
			if (BgeeProperties.getJdbcDriver() != null) {
				log.info("No DataSource obtained from InitialContext using JNDI, register Driver {}", 
						BgeeProperties.getJdbcDriver());
				//we register the Driver once 
				try {
					Class.forName(BgeeProperties.getJdbcDriver());
				} catch (ClassNotFoundException e1) {
					log.error("Could not load Driver", e1);
					driverRegErrTemp = true;
				}
			} else {
				log.info("Will rely on URL to identify proper Driver: {}", 
						BgeeProperties.getJdbcUrl());
			}
		}
		realDataSource = dataSourceTemp;
		driverRegistrationError = driverRegErrTemp;
		
		log.info("BgeeDataSource initialization done.");
		log.exit();
	}
	
	/**
	 * Return a <code>BgeeDataSource</code> object. At the first call of this method 
	 * inside a given thread, a new <code>BgeeDataSource</code> will be instantiated 
	 * and returned. Then all subsequent calls to this method inside the same thread 
	 * will return the same <code>BgeeDataSource</code> object. 
	 * <p>
	 * This is to ensure that each thread uses one and only one 
	 * <code>BgeeDataSource</code> instance, 
	 * independent from other threads ("per-thread singleton").
	 * <p>
	 * An exception is if you call this method 
     * after having called {@link #close()} or {@link #closeAll()}.
     * In that case, this method would return a new <code>BgeeDataSource</code> instance. 
     * It is not a big deal.
	 *  
	 * @return	A <code>BgeeDataSource</code> object, instantiated at the first call 
	 * 			of this method. Subsequent call will return the same object.
	 */
	public static BgeeDataSource getBgeeDataSource()
	{
		log.entry();
		long threadId = Thread.currentThread().getId();
		
		if (!bgeeDataSources.containsKey(threadId)) {
			//we take care of instantiating the BgeeDataSource only if needed
			BgeeDataSource source = new BgeeDataSource();
			//we don't use putifAbsent as the threadId make sure 
			//there won't be any key collision
		    bgeeDataSources.put(threadId, source);
		    
		    return log.exit(source);
		}
		return log.exit(bgeeDataSources.get(threadId));
	}
	
	/**
	 * Close all <code>BgeeDataSource</code>s currently registered 
	 * (call {@link #close()} on each of them). 
	 * It returns the number of <code>BgeeDataSource</code>s that were closed. 
	 * <p>
	 * This method is called for instance when a <code>ShutdownListener</code> 
	 * want to close all <code>Connection</code>s and release all 
	 * <code>BgeeDataSource</code>s.
	 * 
	 * @return 	An <code>int</code> that is the number of <code>BgeeDataSource</code>s 
	 * 			that were closed
	 */
	public static int closeAll()
	{
		
	}
	/**
	 * Retrieve the <code>BgeeDataSource</code> associated to <code>threadId</code>, 
	 * abort all <code>BgeeConnection</code>s that it holds, and release 
	 * the <code>BgeeDataSource</code> (a call to {@link getBgeeDataSource()}, 
	 * from the thread with the ID <code>threadId</code>, would return a new 
	 * <code>BgeeDataSource</code> instance). 
	 * <p>
	 * This method is most likely used by a monitoring thread wanting to abort 
	 * execution of the monitored thread. 
	 * 
	 * @param threadId 	A <code>long</code> that is the ID of the thread for which 
	 * 					we want to abort the <code>BgeeDataSource</code> associated with.
	 */
	public static void abort(long threadId)
	{
		
	}
	
	//****************************
	// INSTANCE METHODS
	//****************************
	/**
	 * Private constructor. Instances of this class can only be obtained 
	 * through the <code>static</code> method {@link getBgeeDataSource()}. 
	 * This is to ensure that a thread will use its own and only one instance of this class.
	 */
	private BgeeDataSource()
	{
		log.entry();
		this.setOpenConnections(new HashMap<String, BgeeConnection>());
		log.exit();
	}
	
	/**
	 * Close all <code>BgeeConnection</code>s that this <code>BgeeDataSource</code> holds, 
	 * and release this <code>BgeeDataSource</code> (a call to {@link #getBgeeDataSource()} 
	 * will return a new <code>BgeeDataSource</code> instance). 
	 * 
	 * @return 	An <code>int</code> that is the number of <code>BgeeConnection</code>s 
	 * 			that were closed following a call to this method.
	 */
	public int close()
	{
		log.entry();
		
		log.exit();
	}
	
	/**
	 * Notification that a <code>BgeeConnection</code>, with an <code>ID</code> 
	 * equals to <code>connectionId</code>, holds by this <code>BgeeDataSource</code>, 
	 * has been closed. 
	 * <p>
	 * This method will thus removed from {@link #openConnections} 
	 * the <code>BgeeConnection</code> with the corresponding key. 
	 * <p>
	 * If no more <code>BgeeConnection</code>s are stored, release this 
	 * <code>BgeeDataSource</code> (a call to {@link #getBgeeDataSource()} 
	 * will return a new <code>BgeeDataSource</code> instance).
	 * 
	 * @param connectionId 	A <code>String</code> representing the <code>ID</code> 
	 * 						of the <code>BgeeConnection</code> that was closed. 
	 */
	protected void connectionClosed(String connectionId)
	{
		log.entry(connectionId);
		log.debug("Releasing BgeeConnection with ID {}", connectionId);
		
		this.removeFromOpenConnection(connectionId);
		if (this.getOpenConnections().size() == 0) {
			log.debug("No more opened BgeeConnection, releasing this BgeeDataSource.");
			this.release();
		}
		
		log.exit();
	}
	
	/**
	 * Release this <code>BgeeDataSource</code> instance, so that 
	 * a consecutive call to {@link #getBgeeDataSource()} will return a new 
	 * <code>BgeeDataSource</code> instance. 
	 * <p>
	 * It removed this <code>BgeeDataSource</code> instance from {@link #bgeeDataSources}.
	 */
	private void release()
	{
		bgeeDataSources.remove(Thread.currentThread().getId());
	}
	
	/**
	 * @param openConnections A <code>Map<String,BgeeConnection></code> to set {@link #openConnections} 
	 */
	private void setOpenConnections(Map<String, BgeeConnection> openConnections) {
		this.openConnections = openConnections;
	}
	/**
	 * Store <code>connection</code> in the <code>Map</code> {@link #openConnections}, 
	 * associated to the key provided by a call on <code>connection</code>  
	 * to the method <code>getId()</code>.
	 * 
	 * @param connection 	A <code>BgeeConnection</code> that is opened, to be stored. 
	 */
	private void storeOpenConnection(BgeeConnection connection)
	{
		this.openConnections.put(connection.getId(), connection);
	}
	/**
	 * Remove from {@link #openConnections} the <code>BgeeConnection</code> 
	 * mapped to the key <code>key</code>. 
	 * 
	 * @param key 	A <code>String</code> representing the key of the 
	 * 				<code>BgeeConnection</code> to be removed from <code>openConnections</code>.
	 */
	private void removeFromOpenConnection(String key)
	{
		this.openConnections.remove(key);
	}
	/**
	 * @return the {@link #openConnections}
	 */
	private Map<String, BgeeConnection> getOpenConnections() {
		return this.openConnections;
	}
}
