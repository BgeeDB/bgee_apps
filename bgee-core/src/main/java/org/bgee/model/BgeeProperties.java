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
	 * If a <code>DataSource</code> was set (using JNDI), then this property is not used. 
	 */
	private static String jdbcDriverName;
	
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
	 * Initialize all Bgee properties.
	 */
	public static void init()
	{
		log.entry();
		log.info("Bgee properties initialization...");

		java.util.Properties props = new java.util.Properties(System.getProperties());
		//try to get the properties file.
		//default name is bgee.properties
		//check first if an alternative name has been provided in the System properties
		String propertyFile = getStringOption(props, "bgee.properties.file", 
				"/bgee.properties");

		InputStream propStream =
				BgeeProperties.class.getResourceAsStream(propertyFile);
		if (propStream != null) {
			try {
				props.load(propStream);
			} catch (IOException e) {
				log.error("Error when loading bgee.properties from classpath", e);
			} finally {
				try {
					propStream.close();
				} catch (IOException e) {
					log.error("Error when closing bgee.properties file", e);
				}
			}
			log.debug("bgee.properties loaded from classpath");
		} else {
			log.debug("bgee.properties not found in classpath. Using System properties.");
		}

		jdbcDriverName = getStringOption(props, "bgee.jdbc.driver", null);

		log.info("Initialization done.");
		log.exit();
	}
	
	/**
	 * Retrieve a property from <code>props</code> corresponding to <code>key</code>. 
	 * Return <code>defaultValue</code> if the property is not defined or empty.
	 *
	 * @param props 		<code>java.sql.Properties</code> to get the property from.
	 * @param key		 	property key.
	 * @param defaultValue	default value that will be returned if the property 
	 * 						is not defined or empty.
	 *
	 * @return 			A <code>String</code> corresponding to the value
	 * 					for that property key. 
	 * 					Or <code>defaultValue</code> if not defined or empty.
	 */
	private static String getStringOption(java.util.Properties props, String key, 
			String defaultValue)
	{
		log.entry(props, key);

		String propValue = props.getProperty(key);
		if (propValue == null || propValue.length()==0) {
			log.debug("Property {} not defined, using default value {}", key, defaultValue);
			propValue = defaultValue; 
		} else {
			log.debug("Retrieved {}={}", key, propValue);
		}

		return log.exit(propValue);
	}
	
	/**
	 * @return the jdbcDriverName
	 * @see #jdbcDriverName
	 */
	public static String getJdbcDriverName() {
		return jdbcDriverName;
	}
}
