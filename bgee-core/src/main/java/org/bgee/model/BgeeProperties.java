package org.bgee.model;

import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class loads the properties for Bgee. 
 * They are tried to be read first from a property file in the classpath 
 * (called "bgee.properties"), then from the <code>System</code> properties.
 * <p>
 * This class has been copied from <code>net.sf.log4jdbc.DriverSpy</code> 
 * developed by Arthur Blake.
 * 
 * @author Frederic Bastian
 * @author Arthur Blake
 * @version Bgee 13, Mar 2013
 * @since Bgee 13
 */
public class BgeeProperties 
{
	/**
	 * <code>Logger</code> of the class. 
	 */
	private final static Logger log = LogManager.getLogger(BgeeProperties.class.getName());
	/**
	 * A <code>String</code> corresponding to the name of the JDBC driver to be registered, 
	 * to then obtain <code>Connection</code>s from a <code>DriverManager</code>. 
	 * Examples are <code>com.mysql.jdbc.Driver</code>, 
	 * or <code>org.bgee.easycache4jdbc.sql.jdbcapi.Driver</code>. 
	 * <p>
	 * This property is used by {@link org.bgee.model.data.sql.BgeeDataSource BgeeDataSource} 
	 * to determine how <code>Connection</code>s should be obtained 
	 * when a <code>DataSource</code> is not available (standalone context). 
	 * <p>
	 * Corresponds to the property <code>bgee.jdbc.driver</code>. 
	 * If a <code>DataSource</code> was set (using JNDI), then this property is not used. 
	 */
	private static String jdbcDriver;
	/**
	 * A <code>String</code> representing a database url of the form 
	 * <code>jdbc:subprotocol:subname</code>, to connect to the database 
	 * using the driver specified by <code>jdbcDriver</code>. 
	 * An example is <code>jdbc:log4jdbc:mysql://127.0.0.1:3306/bgee_v12</code> 
	 * <p>
	 * IMPORTANT: Do NOT put in this URL the username and password you want to use. 
	 * You need to set <code>jdbcUsername</code> and <code>jdbcPassword</code> instead. 
	 * Otherwise you won't be able to use the method <code>getConnection(username, password)</code>, 
	 * but only the method <code>getConnection()</code>. 
	 * <p>
	 * Corresponds to the property <code>bgee.jdbc.url</code>. 
	 * If a DataSource was set (using JNDI), then this property is not used.
	 */
	private static String jdbcUrl;
	/**
	 * A <code>String</code> representing the default username to use to connect 
	 * to the database specified in <code>jdbcUrl</code>, using the Driver specified in 
	 * <code>jdbcDriver</code>. 
	 * <p>
	 * Corresponds to the property <code>bgee.jdbc.username</code>.
	 * If a DataSource was set (using JNDI), then this property is not used.
	 */
	private static String jdbcUsername;
	/**
	 * A <code>String</code> representing the default password to use to connect 
	 * to the database specified in <code>jdbcUrl</code>, using the Driver specified in 
	 * <code>jdbcDriver</code>. 
	 * <p>
	 * Corresponds to the property <code>bgee.jdbc.password</code>.
	 * If a DataSource was set (using JNDI), then this property is not used.
	 */
	private static String jdbcPassword;
	
	/**
	 * Static initializer. 
	 * 
	 * Try first to load the properties from a properties file, 
	 * then try to load them from the system properties. 
	 * Otherwise, set the default values.
	 */
	static 
	{
		log.entry();
		init();
		log.exit();
	}
	
	/**
	 * Initialize (or re-initialize) all Bgee properties.
	 */
	public static void init()
	{
		log.entry();
		log.info("Bgee properties initialization...");

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
		
		if (propValue != null && propValue.length() != 0) {
			log.debug("Retrieved from System properties {}={}", key, propValue);
		} else {
			if (fileProps != null) {
			    propValue = fileProps.getProperty(key);
			}
			if (propValue != null && propValue.length() != 0) {
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
	 * @return the jdbcDriver
	 * @see #jdbcDriver
	 */
	public static String getJdbcDriver() {
		return jdbcDriver;
	}
	/**
	 * @return the {@link #jdbcUrl}
	 */
	public static String getJdbcUrl() {
		return jdbcUrl;
	}
	/**
	 * @return the {@link #jdbcUsername}
	 */
	public static String getJdbcUsername() {
		return jdbcUsername;
	}
	/**
	 * @return the {@link #jdbcPassword}
	 */
	public static String getJdbcPassword() {
		return jdbcPassword;
	}
}
