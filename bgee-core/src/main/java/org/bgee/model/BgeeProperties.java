package org.bgee.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class loads the properties for Bgee. 
 * The configuration is controlled via System properties, 
 * or via a file named <code>bgee.properties</code>, put in the classpath. 
 * When this class is loaded (so only <b>once</b> for a given <code>ClassLoader</code>), 
 * it reads the properties from both the System properties and the property file, 
 * so that, for instance, a property can be provided in the file, 
 * and another property via System properties. 
 * If a property is defined in both locations, then the System property 
 * overrides the property in the file.
 * Of note, an additional property allows to change the name of the property file 
 * to use (corresponds to the property <code>bgee.properties.file</code>).
 * <p>
 * The properties are: 
 * <ul>
 * <li><code>bgee.jdbc.driver</code>: name of the JDBC <code>Driver</code> 
 * used to obtain connections. Optional.
 * <li><code>bgee.jdbc.url</code>: JDBC URL to use to connect to the data source.
 * Mandatory.
 * <li><code>bgee.jdbc.username</code>: default username to connect to the data source.
 * Mandatory.
 * <li><code>bgee.jdbc.password</code>: default password to connect to the data source.
 * Mandatory.
 * <li><code>bgee.properties.file</code>: path to the properties file to use, 
 * from the classpath. Optional.
 * </ul>
 * <p>
 * These properties are read from the properties file 
 * or the System properties only once at class loading. Modifying these properties 
 * after having made any call to this class would have no effect. 
 * To modify a property locally, a <code>Thread</code> must acquire an instance 
 * of this class (by calling the static method {@link #getBgeeProperties()}), 
 * and modify its instance attributes: the instance attributes are initialized 
 * at instantiation using the class attributes obtained from the <code>Properties</code>. 
 * These instance attributes can then be modified, but it will have no effect 
 * on the overall configuration, the modifications will only be seen inside 
 * the related <code>Thread</code>. 
 * <p>
 * Any call, <b>inside a thread</b>, to the method {@link #getBgeeProperties()}, 
 * will always return the same <code>BgeeProperties</code> instance ("per-thread singleton"). 
 * So any modification made to this <code>BgeeProperties</code> instance 
 * will affect the whole thread  (for instance, modifying in a thread the username 
 * to use to connect to a database at one place, will affect all consecutive attempts 
 * to connect to the database in this thread).
 * <p>
 * An exception is if you call this method 
 * after having called {@link #release()} or {@link #releaseAll()}.
 * In that case, <code>getBgeeProperties()</code> 
 * will return a new <code>BgeeProperties</code> instance, with the attributes re-initialized 
 * using the System or file properties.
 * <p>
 * You should always call {@link #release()} at the end of the execution of one thread, 
 * and {@link #releaseAll()} in multi-threads context (for instance, in a webapp context 
 * when the webapp is shutdown). Otherwise, there is small risk to have a collision 
 * of thread IDs, used to store in a <code>Map</code> the <code>BgeeProperties</code>s.
 * <p>
 * This class has been inspired from <code>net.sf.log4jdbc.DriverSpy</code> 
 * developed by Arthur Blake.
 * 
 * @author Frederic Bastian
 * @version Bgee 13, Mar 2013
 * @since Bgee 13
 */
public class BgeeProperties 
{
	//*********************************
	// CLASS ATTRIBUTES
	//*********************************
	/**
	 * <code>Logger</code> of the class. 
	 */
	private final static Logger log = LogManager.getLogger(BgeeProperties.class.getName());
	
	/**
     * A <code>ConcurrentMap</code> used to store all <code>BgeeProperties</code> instances, 
     * associated with the ID of the thread that requested it. 
     * <p>
     * This <code>Map</code> is used to provide a unique and independent 
     * <code>BgeeProperties</code> instance to each thread: 
     * a <code>BgeeProperties</code> is added to this <code>Map</code> 
     * when {@link #getBgeeProperties()} is called, if the thread ID is not already 
     * present in the <code>keySet</code> of the <code>Map</code>. 
     * Otherwise, the already stored <code>BgeeProperties</code> is returned. 
     * <p>
     * If a <code>ThreadLocal</code> was not used, it is because 
     * this <code>Map</code> is used by other treads, 
     * for instance when a <code>ShutdownListener</code> 
     * want to properly remove all <code>BgeeProperties</code>; 
     * or when a thread performing monitoring of another thread want to modify a property.
     * <p>
     * A <code>BgeeProperties</code> is removed from this <code>Map</code> 
     * when the method {@link #release()} is called on it. 
     */
    private static final ConcurrentMap<Long, BgeeProperties> bgeeProperties = 
    		new ConcurrentHashMap<Long, BgeeProperties>();
    /**
     * An <code>AtomicBoolean</code> to define if <code>BgeeProperties</code>s 
     * can still be acquired (using {@link #getBgeeProperties()}), 
     * or if it is not possible anymore (meaning that the method {@link #releaseAll()} 
     * has been called)
     */
    private static final AtomicBoolean bgeePropertiesClosed = new AtomicBoolean();
    
	/**
	 * A <code>String</code> corresponding to the name of the JDBC <code>Driver</code> 
	 * used to obtain connections. 
	 * Examples are <code>com.mysql.jdbc.Driver</code>, 
	 * or <code>org.bgee.easycache4jdbc.sql.jdbcapi.Driver</code>. 
	 * <p>
	 * IMPORTANT: this property does not need to be provided. It is useful only 
	 * for some buggy JDBC <code>Driver</code>s, that fail to register themselves to the 
	 * <code>DriverManager</code>. In that case, it is needed to explicitly load 
	 * the <code>Driver</code>, using this class name.  
	 * <p>
	 * Corresponds to the property <code>bgee.jdbc.driver</code>. 
	 * If a <code>DataSource</code> was set (using JNDI), then this property is not used. 
	 */
	private static final String jdbcDriver;
	/**
	 * A <code>String</code> representing a database url of the form 
	 * <code>jdbc:subprotocol:subname</code>, to connect to the database 
	 * using the <code>DriverManager</code>. 
	 * An example is <code>jdbc:log4jdbc:mysql://127.0.0.1:3306/bgee_v12</code> 
	 * <p>
	 * IMPORTANT: Do NOT provide the username and password you want to use 
	 * by default in the URL. 
	 * You must set the properties {@link #jdbcUsername} and {@link #jdbcPassword} 
	 * to provide the default username and password. You will still be able to call 
	 * a method <code>getConnection(String, String)</code> to provide different 
	 * username and password.
	 * <p>
	 * Corresponds to the property <code>bgee.jdbc.url</code>. 
	 * If a DataSource was set (using JNDI), then this property is not used.
	 */
	private static final String jdbcUrl;
	/**
	 * A <code>String</code> representing the default username to use to connect 
	 * to the database using <code>jdbcUrl</code>. 
	 * <p>
	 * Corresponds to the property <code>bgee.jdbc.username</code>.
	 * If a DataSource was set (using JNDI), then this property is not used.
	 */
	private static final String jdbcUsername;
	/**
	 * A <code>String</code> representing the default password to use to connect 
	 * to the database using <code>jdbcUrl</code>. 
	 * <p>
	 * Corresponds to the property <code>bgee.jdbc.password</code>.
	 * If a DataSource was set (using JNDI), then this property is not used.
	 */
	private static final String jdbcPassword;

	//*********************************
	// INSTANCE ATTRIBUTES
	//*********************************
	/**
	 * A <code>String</code> corresponding to the name of the JDBC <code>Driver</code> 
	 * used to obtain connections. Initialized at instantiation from 
	 * {@link #jdbcDriver}.
	 * @see #jdbcDriver
	 */
	private String localJdbcDriver;
	/**
	 * A <code>String</code> representing a database url of the form 
	 * <code>jdbc:subprotocol:subname</code>, to connect to the database 
	 * using the <code>DriverManager</code>. Initialized at instantiation 
	 * from {@link #jdbcUrl};
	 * @see #jdbcUrl;
	 */
	private String localJdbcUrl;
	/**
	 * A <code>String</code> representing the default username to use to connect 
	 * to the database using the JDBC URL. Initialized at instantiation 
	 * from {@link #jdbcUsername};
	 * @see #jdbcUsername;
	 */
	private String localJdbcUsername;
	/**
	 * A <code>String</code> representing the default password to use to connect 
	 * to the database using the JDBC URL. Initialized at instantiation 
	 * from {@link #jdbcPassword};
	 * @see #jdbcPassword;
	 */
	private String localJdbcPassword;

	//*********************************
	// CLASS METHODS
	//*********************************
	
	/**
	 * Static initializer. 
	 * 
	 * Try to load the properties from a properties file, 
	 * or from the system properties. 
	 * Otherwise, set the default values.
	 */
	static 
	{
		log.entry();
		log.info("Bgee properties initialization...");

		bgeePropertiesClosed.set(false);
		
		java.util.Properties sysProps = new java.util.Properties(System.getProperties());
		//try to get the properties file.
		//default name is bgee.properties
		//check first if an alternative name has been provided in the System properties
		String propertyFile = sysProps.getProperty("bgee.properties.file", 
				"/bgee.properties");
		log.debug("Trying to use properties file {}", propertyFile);

		java.util.Properties fileProps = null;
		InputStream propStream =
				BgeeProperties.class.getResourceAsStream(propertyFile);
		if (propStream != null) {
			try {
				fileProps = new java.util.Properties();
				fileProps.load(propStream);
				log.debug("{} loaded from classpath", propertyFile);
			} catch (IOException e) {
				log.error("Error when loading properties file from classpath", e);
			} finally {
				try {
					propStream.close();
				} catch (IOException e) {
					log.error("Error when closing properties file", e);
				}
			}
		} else {
			log.debug("{} not found in classpath. Using System properties only.", propertyFile);
		}

		jdbcDriver   = getStringOption(sysProps, fileProps, "bgee.jdbc.driver", null);
		jdbcUrl      = getStringOption(sysProps, fileProps, "bgee.jdbc.url", null);
		jdbcUsername = getStringOption(sysProps, fileProps, "bgee.jdbc.username", null);
		jdbcPassword = getStringOption(sysProps, fileProps, "bgee.jdbc.password", null);

		log.info("Initialization done.");
		log.exit();
	}
	
	/**
	 * Try to retrieve the property corresponding to <code>key</code>, 
	 * first from the System properties (<code>sysProps</code>), 
	 * then, if undefined or empty, from properties retrieved from the Bgee property file 
	 * (<code>fileProps</code>). If the property is undefined or empty 
	 * in both <code>fileProps</code> and <code>sysProps</code>, 
	 * return <code>defaultValue</code>.
	 *
	 * @param sysProps 		<code>java.sql.Properties</code> retrieved from System properties, 
	 * 						where <code>key</code> is first searched in.
	 * @param fileProps	 	<code>java.sql.Properties</code> retrieved 
	 * 						from the Bgee properties file, 
	 * 						where <code>key</code> is searched in if the property 
	 * 						was undefined or empty in <code>sysProps</code>. 
	 * 						Can be <code>null</code> if no properties file was found.
	 * @param defaultValue	default value that will be returned if the property 
	 * 						is undefined or empty in both <code>Properties</code>.
	 *
	 * @return 			A <code>String</code> corresponding to the value
	 * 					for that property key. 
	 * 					Or <code>defaultValue</code> if not defined or empty.
	 */
	private static String getStringOption(java.util.Properties sysProps, 
			java.util.Properties fileProps, String key, 
			String defaultValue)
	{
		log.entry(fileProps, sysProps, key, defaultValue);

		String propValue = sysProps.getProperty(key);
		
		if (StringUtils.isNotBlank(propValue)) {
			log.debug("Retrieved from System properties {}={}", key, propValue);
		} else {
			if (fileProps != null) {
			    propValue = fileProps.getProperty(key);
			}
			if (StringUtils.isNotBlank(propValue)) {
				log.debug("Retrieved from properties file {}={}", key, propValue);
			} else {
				log.debug("Property {} not defined neither in properties file nor in System properties, using default value {}", 
						key, defaultValue);
				propValue = defaultValue; 
			}
		}

		return log.exit(propValue);
	}
	/**
	 * Return a <code>BgeeProperties</code> object. At the first call of this method 
	 * inside a given thread, a new <code>BgeeProperties</code> will be instantiated 
	 * and returned. Then all subsequent calls to this method inside the same thread 
	 * will return the same <code>BgeeProperties</code> object. 
	 * <p>
	 * This is to ensure that each thread uses one and only one 
	 * <code>BgeeProperties</code> instance, 
	 * independent from other threads ("per-thread singleton").
	 * <p>
	 * An exception is if you call this method 
     * after having called {@link #release()} on the <code>BgeeProperties</code>.
     * In that case, this method would return a new <code>BgeeProperties</code> instance, 
     * with attributes re-initialized using the <code>Properties</code> from 
     * the properties file or the System properties. 
     * <p>
     * Note that after having called {@link #releaseAll()}, no <code>BgeeProperties</code> 
     * can be obtained anymore. This method will throw an <code>IllegalStateException</code> 
     * if <code>releaseAll()</code> has been previously called.
	 *  
	 * @return	A <code>BgeeProperties</code> object, instantiated at the first call 
	 * 			of this method. Subsequent calls will return the same object. 
	 * @throws IllegalStateException If no <code>BgeeProperties</code> could be obtained anymore. 
	 */
	public static BgeeProperties getBgeeProperties() throws IllegalStateException
	{
		log.entry();
		
	    if (bgeePropertiesClosed.get()) {
	    	throw new IllegalStateException("releaseAll() has been already called, " +
	    			"it is not possible to acquire a BgeeProperties anymore");
	    }
	    
		long threadId = Thread.currentThread().getId();
		log.debug("Trying to obtain a BgeeProperties instance from Thread {}", threadId);
		
		if (!bgeeProperties.containsKey(threadId)) {
			//instantiate the BgeeProperties only if needed
			BgeeProperties prop = new BgeeProperties();
			//we don't use putifAbsent, as the threadId make sure 
			//there won't be any multi-threading key collision
			bgeeProperties.put(threadId, prop);
			log.debug("Return a new BgeeProperties instance");
			
		    return log.exit(prop);
		}
		log.debug("Return an already existing BgeeProperties instance");
		return log.exit(bgeeProperties.get(threadId));
	}

	/**
	 * Release the <code>BgeeProperties</code> associated to the thread 
	 * calling this method. A call to {@link #getBgeeProperties()} from this thread 
	 * will return a new <code>BgeeProperties</code> instance. 
	 * 
	 * @return 	<code>true</code> if there was a <code>BgeeProperties</code> to release 
	 * 			for the caller thread, <code>false</code> otherwise.
	 */
	public static boolean release()
	{
		log.entry();
		if (bgeeProperties.remove(Thread.currentThread().getId()) != null) {
			return log.exit(true);
		}
		return log.exit(false);
	}
	/**
	 * Release all <code>BgeeProperties</code>s currently registered 
	 * and prevent any new 
	 * <code>BgeeProperties</code> to be obtained again (calling {@link #getBgeeProperties()} 
	 * after having called this method will throw a <code>IllegalStateException</code>). 
	 * <p>
	 * This method returns the number of <code>BgeeProperties</code>s that were released. 
	 * <p>
	 * This method is called for instance when a <code>ShutdownListener</code> 
	 * want to release all <code>BgeeProperties</code>s.
	 * 
	 * @return 	An <code>int</code> that is the number of <code>BgeeProperties</code>s 
	 * 			that were released
	 */
	public static int releaseAll()
	{
		log.entry();
		
		//this AtomicBoolean will act more or less like a lock 
		//(no new BgeeProperties can be obtained after this AtomicBoolean is set to true).
		//It's not totally true, but we don't except any major error if it doesn't act like a lock.
		bgeePropertiesClosed.set(true);
		
		int propCount = 0;
		Iterator<BgeeProperties> propIterator = bgeeProperties.values().iterator();
		while (propIterator.hasNext()) {
			propIterator.next();
			propCount++;
			propIterator.remove();
		}
		
		return log.exit(propCount);
	}

	//*********************************
	// INSTANCE METHODS
	//*********************************
	/**
	 * Private constructor, instances can be obtained only through the use 
	 * of the static method {@link getBgeeProperties()}. 
	 * <p>
	 * Instance attributes are initialized using the corresponding 
	 * class attributes. 
	 */
	private BgeeProperties()
	{
		this.setJdbcDriver(jdbcDriver);
		this.setJdbcUrl(jdbcUrl);
		this.setJdbcUsername(jdbcUsername);
		this.setJdbcPassword(jdbcPassword);
	}
	
	/**
	 * Determine whether this <code>BgeeProperties</code> was released 
	 * (following a call to {@link #release()}).
	 * 
	 * @return	<code>true</code> if this <code>BgeeProperties</code> was released, 
	 * 			<code>false</code> otherwise.
	 */
	public boolean isReleased()
	{
		log.entry();
		if (bgeeProperties.containsValue(this)) {
			return log.exit(false);
		}
		return log.exit(true);
	}
	
	/**
	 * Return the name of the JDBC <code>Driver</code> to use 
	 * to connect to the data source.
	 * @return 	a <code>String</code> corresponding to the name 
	 * 			of the JDBC <code>Driver</code>
	 */
	public String getJdbcDriver() {
		return this.localJdbcDriver;
	}
	/**
	 * Set the name of the JDBC <code>Driver</code> to use 
	 * to connect to the data source.
	 * @param jdbcDriver	a <code>String</code> corresponding to the name 
	 * 						of the JDBC <code>Driver</code>
	 */
	public void setJdbcDriver(String jdbcDriver) {
		this.localJdbcDriver = jdbcDriver;
	}
	
	/**
	 * Return the database url of the form <code>jdbc:subprotocol:subname</code>, 
	 * to use to connect to the data source using the <code>DriverManager</code>. 
	 * @return 	a <code>String</code> representing the JDBC URL to use
	 */
	public String getJdbcUrl() {
		return this.localJdbcUrl;
	}
	/**
	 * Set the database url of the form <code>jdbc:subprotocol:subname</code>, 
	 * to use to connect to the data source using the <code>DriverManager</code>. 
	 * @param jdbcUrl 	a <code>String</code> representing the JDBC URL to use
	 */
	public void setJdbcUrl(String jdbcUrl) {
		this.localJdbcUrl = jdbcUrl;
	}
	
	/**
	 * Return the default username to use to connect to the data source 
	 * using the JDBC URL.
	 * @return 	a <code>String</code> representing the default username.
	 */
	public String getJdbcUsername() {
		return this.localJdbcUsername;
	}
	/**
	 * Set the default username to use to connect to the data source 
	 * using the JDBC URL.
	 * @param jdbcUsername	a <code>String</code> representing the default username.
	 */
	public void setJdbcUsername(String jdbcUsername) {
		this.localJdbcUsername = jdbcUsername;
	}
	
	/**
	 * Return the default password to use to connect to the data source 
	 * using the JDBC URL.
	 * @return 	a <code>String</code> representing the default password.
	 */
	public String getJdbcPassword() {
		return this.localJdbcPassword;
	}
	/**
	 * Set the default password to use to connect to the data source 
	 * using the JDBC URL.
	 * @param jdbcPassword	a <code>String</code> representing the default password.
	 */
	public void setJdbcPassword(String jdbcPassword) {
		this.localJdbcPassword = jdbcPassword;
	}
}
