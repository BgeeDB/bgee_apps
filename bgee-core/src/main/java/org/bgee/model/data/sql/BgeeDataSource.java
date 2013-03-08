package org.bgee.model.data.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.codec.digest.DigestUtils;
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
     * An <code>AtomicBoolean</code> to define if <code>BgeeDataSource</code>s 
     * can still be acquired (using {@link #getBgeeDataSource()}), 
     * or if it is not possible anymore (meaning that the method {@link #closeAll()} 
     * has been called)
     */
    private static final AtomicBoolean dataSourcesClosed = new AtomicBoolean();
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
	 * A <code>String</code> representing the value that must be replaced by 
	 * {@link org.bgee.model.BgeeProperties#getJdbcUsername}, in the JDBC URL 
	 * used to obtain a connection from a <code>DriverManager</code>.
	 */
	private static final String URLUSERNAMETAG = "USERNAME";
	/**
	 * A <code>String</code> representing the value that must be replaced by 
	 * {@link org.bgee.model.BgeeProperties#getJdbcPassword}, in the JDBC URL 
	 * used to obtain a connection from a <code>DriverManager</code>.
	 */
	private static final String URLPASSWORDTAG = "PASSWORD";
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
		
		dataSourcesClosed.set(false);
		
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
     * after having called {@link #close()}.
     * In that case, this method would return a new <code>BgeeDataSource</code> instance. 
     * It is not a big deal.
     * <p>
     * Note that after having called {@link #closeAll()}, no <code>BgeeDataSource</code> 
     * can be obtained anymore. This method will throw a <code>SQLException</code> 
     * if <code>closeAll()</code> has been previously called.
	 *  
	 * @return	A <code>BgeeDataSource</code> object, instantiated at the first call 
	 * 			of this method. Subsequent call will return the same object. 
	 * @throws SQLException 	If no <code>BgeeDataSource</code> can be obtained anymore. 
	 */
	public static BgeeDataSource getBgeeDataSource() throws SQLException
	{
		log.entry();
		
	    if (dataSourcesClosed.get()) {
	    	log.debug("Trying to obtain a BgeeDataSource after they have been closed, returning null");
	    	throw new SQLException("closeAll() has been already called, " +
	    			"it is not possible to acquire a BgeeDataSource anymore");
	    }
	    
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
	 * (call {@link #close()} on each of them), and prevent any 
	 * <code>BgeeDataSource</code> to be obtained again (calling {@link #getBgeeDataSource()} 
	 * after having called this method will throw an Exception). 
	 * <p>
	 * This method returns the number of <code>BgeeDataSource</code>s that were closed. 
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
		log.entry();
		
		//this AtomicBoolean will act more or less like a lock 
		//(no new BgeeDataSource can be obtained after this AtomicBoolean is set true).
		//It's not totally true, but we don't except any major error if it doesn't act like a lock.
		dataSourcesClosed.set(true);
		
		int sourcesCount = bgeeDataSources.size();
		//get a shallow copy of bgeeDataSources, so that the remove operation 
		//performed by the method close() will not interfere with the iteration
		Collection<BgeeDataSource> shallowCopy = new ArrayList<BgeeDataSource>(bgeeDataSources.values());
		for (BgeeDataSource dataSource: shallowCopy) {
			dataSource.close();
		}
		
		return log.exit(sourcesCount);
	}
	/**
	 * Retrieve the <code>BgeeDataSource</code> associated to <code>threadId</code>, 
	 * close all <code>BgeeConnection</code>s that it holds, and release 
	 * the <code>BgeeDataSource</code> (a call to {@link getBgeeDataSource()}, 
	 * from the thread with the ID <code>threadId</code>, would return a new 
	 * <code>BgeeDataSource</code> instance). 
	 * Return the number of <code>BgeeConnection</code>s 
	 * that were closed following a call to this method.
	 * <p>
	 * This method is most likely used by a monitoring thread wanting to abort 
	 * execution of the monitored thread. 
	 * 
	 * @param threadId 	A <code>long</code> that is the ID of the thread for which 
	 * 					we want to abort the <code>BgeeDataSource</code> associated with.
	 * @return 			An <code>int</code> being the number of <code>BgeeConnection</code>s 
	 * 					that were aborted following a call to this method.
	 */
	public static int close(long threadId)
	{
		log.entry();
		
		BgeeDataSource sourceToClose = bgeeDataSources.get(threadId);
		if (sourceToClose != null) {
			return log.exit(sourceToClose.close());
		}
		return log.exit(0);
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
	 * Attempt to establish a connection, either from a <code>DataSource</code> 
	 * if one was provided from a <code>Context</code>, or from a JDBC <code>Driver</code> 
	 * defined in the Bgee properties. Default username and password are used. 
	 * <p>
	 * If a <code>BgeeConnection</code> with the same parameters is already hold 
	 * by this <code>BgeeDataSource</code>, then return it without creating a new one. 
	 * This <code>BgeeDataSource</code> will hold a <code>BgeeConnection</code> 
	 * as long as it is not closed. 
	 * 
	 * @return	A <code>BgeeConnection</code> opened to the data source. 
	 * @throws SQLException 	If an error occurred while trying to obtain the connection, 
	 * 							or if the needed <code>Driver</code> could not be loaded.
	 * @see #getConnection(String, String)
	 */
	public BgeeConnection getConnection() throws SQLException
	{
		log.entry();
		
		String username = null;
		String password = null;
		//if we rely on a DriverManager and not on a DataSource, 
		//we get the username and password from the Bgee properties.
		//if we rely on a DataSource, we do not obtain the default username and password 
		//from the InitialContext.
		
		//TODO: try to obtain the default username and password from the InitialContext.
		//Because, when using a DataSource, if we call getConnection(), 
		//then getConnection(String, String) with the default values, 
		//it would be seen as two different connections, while it should be the same.
		if (realDataSource == null) {
			username = BgeeProperties.getJdbcUsername();
			password = BgeeProperties.getJdbcPassword();
		}
		
		return log.exit(this.getConnection(username, password));
	}
	/**
	 * Attempt to establish a connection, either from a <code>DataSource</code> 
	 * if one was provided from a <code>Context</code>, or from a JDBC <code>Driver</code> 
	 * defined in the Bgee properties, using the provided 
	 * <code>username</code> and <code>password</code>. 
	 * <p>
	 * If a <code>BgeeConnection</code> with the same parameters is already hold 
	 * by this <code>BgeeDataSource</code>, then return it without creating a new one. 
	 * This <code>BgeeDataSource</code> will hold a <code>BgeeConnection</code> 
	 * as long as it is not closed. 
	 * 
	 * @param username 	A <code>String</code> defining the username to use 
	 * 					to open the connection.
	 * @param password 	A <code>String</code> defining the password to use 
	 * 					to open the connection.
	 * @return			A <code>BgeeConnection</code> opened to the data source. 
	 * @throws SQLException 	If an error occurred while trying to obtain the connection, 
	 * 							or if the needed <code>Driver</code> could not be loaded.
	 * @see #getConnection()
	 */
	public BgeeConnection getConnection(String username, String password) throws SQLException
	{
		log.entry(username, password);
		log.debug("Trying to obtain a BgeeConnection with username {} and password {}...", 
				username, password);
		
		//if we couldn't register the driver, don't try to get a connection 
		//from the DriverManager, just throw an Exception
		if (driverRegistrationError) {
			throw new SQLException("The Driver {} could not be properly registered.", 
					BgeeProperties.getJdbcDriver());
		}
		
		String connectionId = this.generateConnectionId(username, password);
		BgeeConnection connection = this.getOpenConnections().get(connectionId);
		//if the connection already exists, return it
		if (connection != null) {
			log.debug("Return an already opened Connection");
			return log.exit(connection);
		}
		//otherwise, create a new connection
		Connection realConnection = null;
		if (realDataSource != null) {
			log.debug("Trying to obtain a new Connection from the DataSource");
			if (username == null && password == null) {
				realConnection = realDataSource.getConnection();
			} else {
				realConnection = realDataSource.getConnection(username, password);
			}
		} else {
			String urlToUse = BgeeProperties.getJdbcUrl()
					.replace(URLUSERNAMETAG, username).replace(URLPASSWORDTAG, password);
			log.debug("Trying to obtain a new Connection from the Driver, using the URL {}", urlToUse);
			realConnection = DriverManager.getConnection(urlToUse);
		}
		//just in case we couldn't obtain the connection, without exception
		if (realConnection == null) {
			throw new SQLException("Could not obtain a Connection");
		}
		//now create the new BgeeConnection
		connection = new BgeeConnection(this, realConnection, connectionId);
		//store and return it
		this.storeOpenConnection(connection);
		
		log.debug("Return a newly opened Connection");
		return log.exit(connection);
	}
	
	/**
	 * Close all <code>BgeeConnection</code>s that this <code>BgeeDataSource</code> holds, 
	 * and release this <code>BgeeDataSource</code> (a call to {@link #getBgeeDataSource()} 
	 * will return a new <code>BgeeDataSource</code> instance). 
	 * Return the number of <code>BgeeConnection</code>s 
	 * that were closed following a call to this method.
	 * 
	 * @return 	An <code>int</code> that is the number of <code>BgeeConnection</code>s 
	 * 			that were closed following a call to this method.
	 */
	public int close()
	{
		log.entry();
		int connectionCount = this.getOpenConnections().size();
		//get a shallow copy of the collection, so that the removal of a connection 
		//will not interfere with the iteration
		Collection<BgeeConnection> shallowCopy = 
				new ArrayList<BgeeConnection>(this.getOpenConnections().values());
		for (BgeeConnection connToClose: shallowCopy) {
			try {
				//calling this method will automatically remove the connection 
				//from openConnections, and will release this BgeeDataSource 
				//if there are no more opened connections. 
				connToClose.close();
			} catch (SQLException e) {
				//do nothing, because the connection will be removed from openConnections
				//even if an Exception occurs
			}
		}
		return log.exit(connectionCount);
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
	/**
	 * Generate an ID to uniquely identify the <code>BgeeConnection</code>s 
	 * holded by this <code>BgeeDataSource</code>. It is based on the 
	 * <code>username</code> and <code>password</code> used to open 
	 * the connection. 
	 * 
	 * @param username 	A <code>String</code> defining the username used to open 
	 * 					the connection. Will be used to generate the ID.
	 * @param password	A <code>String</code> defining the password used to open 
	 * 					the connection. Will be used to generate the ID.
	 * @return 			A <code>String</code> representing an ID generated from 
	 * 					<code>username</code> and <code>password</code>.
	 */
	private String generateConnectionId(String username, String password)
	{
		//I don't like much storing a password in memory, let's hash it
    	return DigestUtils.sha1Hex(username + "[separator]" + password);
	}
}
