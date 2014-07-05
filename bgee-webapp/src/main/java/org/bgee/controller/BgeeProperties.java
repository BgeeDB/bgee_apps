package org.bgee.controller;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class loads the properties for Bgee. 
 * The configuration is controlled via System properties, 
 * or via a file named {@code bgee.properties}, put in the classpath. 
 * When this class is loaded (so only <b>once</b> for a given {@code ClassLoader}), 
 * it reads the properties from both the System properties and the property file, 
 * so that, for instance, a property can be provided in the file, 
 * and another property via System properties. 
 * If a property is defined in both locations, then the System property 
 * overrides the property in the file.
 * Of note, an additional property allows to change the name of the property file 
 * to use (corresponds to the property {@code bgee.properties.file}).
 * <p>
 * TODO provide the properties list
 * <p>
 * This class has been inspired from {@code net.sf.log4jdbc.DriverSpy} 
 * developed by Arthur Blake.
 * 
 * @author Frederic Bastian
 * @author Mathieu Seppey
 * @version Bgee 13, May 2013
 * @since Bgee 13
 */
public class BgeeProperties 
{

	/**
	 * {@code Logger} of the class. 
	 */
	private final static Logger log = LogManager.getLogger(BgeeProperties.class.getName());

	//*************************
	//Server parameters
	//*************************
	/**
	 * <code>String</code> defining the directory where query strings holding storable parameters  
	 * from previous large queries are stored. 
	 * Category: server parameters.
	 * @see #loadStorableParametersFromKey()
	 * @see #store()
	 */
	private static String requestParametersStorageDirectory;

	/**
	 * Define the root of URLs to Bgee, 
	 * for instance, "http://bgee.unil.ch/bgee/bgee".
	 * This parameters is loaded by retrieving the value from the server parameters object, 
	 * <code>model.Parameters</code>.
	 * @see model.Parameters
	 */
	private static String bgeeRootDirectory;

	/**
	 * Define the root directory where are located files available for download, 
	 * to be added to the <code>bgeeRootDirectory</code> to generate URL to download files.
	 * This parameters is loaded by retrieving the value from the server parameters object, 
	 * <code>model.Parameters</code>.
	 * @see model.Parameters
	 */
	private static String downloadRootDirectory;

	/**
	 * Define the root directory where are located javascript files, 
	 * to be added to the <code>bgeeRootDirectory</code> to generate URL to obtain javascript files.
	 * This parameters is loaded by retrieving the value from the server parameters object, 
	 * <code>model.Parameters</code>.
	 * @see model.Parameters
	 */
	private static String javascriptFilesRootDirectory;

	/**
	 * Define the root directory where are located css files, 
	 * to be added to the <code>bgeeRootDirectory</code> to generate URL to obtain css files.
	 * This parameters is loaded by retrieving the value from the server parameters object, 
	 * <code>model.Parameters</code>.
	 * @see model.Parameters
	 */
	private static String cssFilesRootDirectory;

	/**
	 * Define the root directory where are located images, 
	 * to be added to the <code>bgeeRootDirectory</code> to generate URL to obtain images.
	 * This parameters is loaded by retrieving the value from the server parameters object, 
	 * <code>model.Parameters</code>.
	 * @see model.Parameters
	 */
	private static String imagesRootDirectory;

	/**
	 * Define the directory where are stored TopOBO result files, 
	 * to be added to the <code>bgeeRootDirectory</code> to generate URL to obtain result files.
	 * This parameters is loaded by retrieving the value from the server parameters object, 
	 * <code>model.Parameters</code>.
	 * @see model.Parameters
	 */
	private static String topOBOResultsUrlRootDirectory;

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
		log.info("Bgee-webapp properties initialization...");

		java.util.Properties sysProps = new java.util.Properties(System.getProperties());
		//try to get the properties file.
		//default name is bgee.properties
		//check first if an alternative name has been provided in the System properties
		String propertyFile = sysProps.getProperty("bgee-webapp.properties.file", 
				"/bgee-webapp.properties");
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

		requestParametersStorageDirectory  = getStringOption(sysProps, fileProps, 
				"org.bgee.webapp.requestParametersStorageDirectory", "/tmp/");

		bgeeRootDirectory  = getStringOption(sysProps, fileProps, 
				"org.bgee.webapp.bgeeRootDirectory", null);

		downloadRootDirectory  = getStringOption(sysProps, fileProps, 
				"org.bgee.webapp.requestParametersStorageDirectory", null);

		javascriptFilesRootDirectory  = getStringOption(sysProps, fileProps, 
				"org.bgee.webapp.downloadRootDirectory", null);
		
		cssFilesRootDirectory  = getStringOption(sysProps, fileProps, 
				"org.bgee.webapp.cssFilesRootDirectory", null);
		
		imagesRootDirectory  = getStringOption(sysProps, fileProps, 
				"org.bgee.webapp.imagesRootDirectory", null);
		
		topOBOResultsUrlRootDirectory  = getStringOption(sysProps, fileProps, 
				"org.bgee.webapp.topOBOResultsUrlRootDirectory", null);

		log.info("Initialization done.");
		
		log.exit();

	}

	/**
	 * Try to retrieve the property corresponding to {@code key}, 
	 * first from the System properties ({@code sysProps}), 
	 * then, if undefined or empty, from properties retrieved from the Bgee property file 
	 * ({@code fileProps}). If the property is undefined or empty 
	 * in both {@code fileProps} and {@code sysProps}, 
	 * return {@code defaultValue}.
	 *
	 * @param sysProps 		{@code java.sql.Properties} retrieved from System properties, 
	 * 						where {@code key} is first searched in.
	 * @param fileProps	 	{@code java.sql.Properties} retrieved 
	 * 						from the Bgee properties file, 
	 * 						where {@code key} is searched in if the property 
	 * 						was undefined or empty in {@code sysProps}. 
	 * 						Can be {@code null} if no properties file was found.
	 * @param defaultValue	default value that will be returned if the property 
	 * 						is undefined or empty in both {@code Properties}.
	 *
	 * @return 			A {@code String} corresponding to the value
	 * 					for that property key. 
	 * 					Or {@code defaultValue} if not defined or empty.
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
	private BgeeProperties(){}

	/**
	 * @return 	A <code>String</code> defining the directory where query strings holding storable parameters  
	 * from previous large queries are stored. 
	 */
	public static String getRequestParametersStorageDirectory() {
		return requestParametersStorageDirectory;
	}

	/**
	 * @return A <code>String</code> defining the root of URLs to Bgee, 
	 * for instance, "http://bgee.unil.ch/bgee/bgee".
	 */
	public static String getBgeeRootDirectory() {
		return bgeeRootDirectory;
	}

	/**
	 * @return A <code>String</code> defining the root directory where are located files available for download, 
	 * to be added to the <code>bgeeRootDirectory</code> to generate URL to download files
	 */
	public static String getDownloadRootDirectory() {
		return downloadRootDirectory;
	}

	/**
	 * @return A <code>String</code> defining the root directory where are located javascript files, 
	 * to be added to the <code>bgeeRootDirectory</code> to generate URL to obtain javascript files.
	 */
	public static String getJavascriptFilesRootDirectory() {
		return javascriptFilesRootDirectory;
	}

	/**
	 * @return A <code>String</code> defining the root directory where are located css files, 
	 * to be added to the <code>bgeeRootDirectory</code> to generate URL to obtain css files.
	 */
	public static String getCssFilesRootDirectory() {
		return cssFilesRootDirectory;
	}

	/**
	 * @return A <code>String</code> defining the root directory where are located images, 
	 * to be added to the <code>bgeeRootDirectory</code> to generate URL to obtain images.
	 */
	public static String getImagesRootDirectory() {
		return imagesRootDirectory;
	}

	/**
	 * @return A <code>String</code> defining the directory where are stored TopOBO result files, 
	 * to be added to the <code>bgeeRootDirectory</code> to generate URL to obtain result files.
	 */
	public static String getTopOBOResultsUrlRootDirectory() {
		return topOBOResultsUrlRootDirectory;
	}

}
