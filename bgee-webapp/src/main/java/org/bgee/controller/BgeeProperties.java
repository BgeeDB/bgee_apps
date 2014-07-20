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
	 * <code>String</code> that defines the directory where query strings holding storable parameters  
	 * from previous large queries are stored. 
	 */
	private static String requestParametersStorageDirectory;

	/**
	 * A <code>String</code> that defines the root of URLs to Bgee, 
	 * for instance, "http://bgee.unil.ch/bgee/bgee".
	 */
	private static String bgeeRootDirectory;

	/**
	 * A <code>String</code> that defines the root directory where are located files available for download, 
	 * to be added to the <code>bgeeRootDirectory</code> to generate URL to download files.
	 */
	private static String downloadRootDirectory;

	/**
	 * A <code>String</code> that defines the root directory where are located javascript files, 
	 * to be added to the <code>bgeeRootDirectory</code> to generate URL to obtain javascript files.
	 */
	private static String javascriptFilesRootDirectory;

	/**
	 * A <code>String</code> that defines the root directory where are located css files, 
	 * to be added to the <code>bgeeRootDirectory</code> to generate URL to obtain css files.
	 */
	private static String cssFilesRootDirectory;

	/**
	 * A <code>String</code> that defines the root directory where are located images, 
	 * to be added to the <code>bgeeRootDirectory</code> to generate URL to obtain images.
	 */
	private static String imagesRootDirectory;

	/**
	 * A <code>String</code> that defines the directory where are stored TopOBO result files, 
	 * to be added to the <code>bgeeRootDirectory</code> to generate URL to obtain result files.
	 */
	private static String topOBOResultsUrlRootDirectory;

	/**
	 * An <code>Integer</code> that definesmax length of URLs. Typically, if the URL exceeds the max length, 
	 * a key is generated to store and retrieve a query string, 
	 * holding the "storable" parameters. The "storable" parameters are removed from the URL, 
	 * and replaced by the generated key.
	 * <p>
	 * The max length of URL is currently 2,083 characters, 
	 * because of limitations of IE9. Just to be sure, 
	 * because of potential server limitations, the limit should be 1,500 characters.
	 * <p>
	 * Anyway, we use a much lower limitation, as we do not want too long URL.
	 * @see org.bgee.RequestParameters.java
	 */
	private static Integer urlMaxLength;

	/**
	 * A <code>boolean</code> that defines whether parameters should be url encoded 
	 * by the <code>encodeUrl</code> method.
	 * If <code>false</code>, then the <code>encodeUrl</code> method returns 
	 * Strings with no modifications, otherwise, they are url encoded if needed 
	 * (it does not necessarily mean they will. For index, if there are no 
	 * special chars to encode in the submitted String).
	 * <parameter>
	 * Default value is <code>true</code>.
	 */
	private static boolean encodeUrl;

	/**
	 * A {@code String} that defines the character used to separate parameters 
	 * in the URL
	 */
	private static String parametersSeparator;

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
				"org.bgee.webapp.bgeeRootDirectory", "/");

		downloadRootDirectory  = getStringOption(sysProps, fileProps, 
				"org.bgee.webapp.requestParametersStorageDirectory", "download/");

		javascriptFilesRootDirectory  = getStringOption(sysProps, fileProps, 
				"org.bgee.webapp.downloadRootDirectory", "js/");

		cssFilesRootDirectory  = getStringOption(sysProps, fileProps, 
				"org.bgee.webapp.cssFilesRootDirectory", "css/");

		imagesRootDirectory  = getStringOption(sysProps, fileProps, 
				"org.bgee.webapp.imagesRootDirectory", "img/");

		topOBOResultsUrlRootDirectory  = getStringOption(sysProps, fileProps, 
				"org.bgee.webapp.topOBOResultsUrlRootDirectory", null);

		urlMaxLength  = getIntegerOption(sysProps, fileProps, 
				"org.bgee.webapp.urlMaxLength", 120);

		encodeUrl  = getBooleanOption(sysProps, fileProps, 
				"org.bgee.webapp.encodeUrl", true);

		parametersSeparator  = getStringOption(sysProps, fileProps, 
				"org.bgee.webapp.parametersSeparator", "&");

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

	/**
	 * Try to retrieve the property corresponding to {@code key}, 
	 * first from the System properties ({@code sysProps}), 
	 * then, if undefined or empty, from properties retrieved from the Bgee property file 
	 * ({@code fileProps}), and cast it into a {@code int} value.
	 * If the property is undefined or empty in both {@code fileProps} 
	 * and {@code sysProps}, return {@code defaultValue}.
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
	 * @return 			An {@code int} corresponding to the value
	 * 					for that property key.
	 * 					Or {@code defaultValue} if not defined or empty.
	 */
	private static int getIntegerOption(java.util.Properties sysProps, 
			java.util.Properties fileProps, String key, 
			int defaultValue)
	{
		log.entry(fileProps, sysProps, key, defaultValue);

		String propValue = getStringOption(sysProps, fileProps, key, null);
		int val = defaultValue;
		if (propValue != null) {
			val= Integer.valueOf(propValue);
		}

		return log.exit(val);
	}

	/**
	 * Try to retrieve the property corresponding to {@code key}, 
	 * first from the System properties ({@code sysProps}), 
	 * then, if undefined or empty, from properties retrieved from the Bgee property file 
	 * ({@code fileProps}), and cast it into a {@code boolean} 
	 * (if the value of the property is set, and equal to "true", "yes", or "on", 
	 * the returned boolean will be {@code true}, {@code false} otherwise). 
	 * If the property is undefined or empty in both {@code fileProps} 
	 * and {@code sysProps}, return {@code defaultValue}.
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
	 * @return 			A {@code boolean} corresponding to the value
	 * 					for that property key (if the value of the property is set and equal 
	 * 					to "true", "yes", or "on", the returned boolean 
	 * 					will be {@code true}, {@code false} otherwise). 
	 * 					Or {@code defaultValue} if not defined or empty.
	 */
	private static boolean getBooleanOption(java.util.Properties sysProps, 
			java.util.Properties fileProps, String key, 
			boolean defaultValue)
	{
		log.entry(fileProps, sysProps, key, defaultValue);

		String propValue = getStringOption(sysProps, fileProps, key, null);
		boolean val = defaultValue;
		if (propValue != null) {
			val= "true".equals(propValue) ||
					"yes".equals(propValue) || 
					"on".equals(propValue);
		}

		return log.exit(val);
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
	 * @return 	A <code>String</code> that defines the directory where query strings holding storable parameters  
	 * from previous large queries are stored. 
	 */
	public static String getRequestParametersStorageDirectory() {
		return requestParametersStorageDirectory;
	}

	/**
	 * @return A <code>String</code> that defines the root of URLs to Bgee, 
	 * for instance, "http://bgee.unil.ch/bgee/bgee".
	 */
	public static String getBgeeRootDirectory() {
		return bgeeRootDirectory;
	}

	/**
	 * @return A <code>String</code> that defines the root directory where are located files available for download, 
	 * to be added to the <code>bgeeRootDirectory</code> to generate URL to download files
	 */
	public static String getDownloadRootDirectory() {
		return downloadRootDirectory;
	}

	/**
	 * @return A <code>String</code> that defines the root directory where are located javascript files, 
	 * to be added to the <code>bgeeRootDirectory</code> to generate URL to obtain javascript files.
	 */
	public static String getJavascriptFilesRootDirectory() {
		return javascriptFilesRootDirectory;
	}

	/**
	 * @return A <code>String</code> that defines the root directory where are located css files, 
	 * to be added to the <code>bgeeRootDirectory</code> to generate URL to obtain css files.
	 */
	public static String getCssFilesRootDirectory() {
		return cssFilesRootDirectory;
	}

	/**
	 * @return A <code>String</code> that defines the root directory where are located images, 
	 * to be added to the <code>bgeeRootDirectory</code> to generate URL to obtain images.
	 */
	public static String getImagesRootDirectory() {
		return imagesRootDirectory;
	}

	/**
	 * @return A <code>String</code> that defines the directory where are stored TopOBO result files, 
	 * to be added to the <code>bgeeRootDirectory</code> to generate URL to obtain result files.
	 */
	public static String getTopOBOResultsUrlRootDirectory() {
		return topOBOResultsUrlRootDirectory;
	}

	/**
	 * @return	An <code>Integer</code> that definesmax length of URLs. Typically, if the URL exceeds the max length, 
	 * 			a key is generated to store and retrieve a query string, 
	 * 			holding the "storable" parameters. The "storable" parameters are removed from the URL, 
	 * 			and replaced by the generated key.
	 */
	public static Integer getUrlMaxLength() {
		return urlMaxLength;
	}

	/**
	 * @return	A <code>boolean</code> that defines whether parameters should be url encoded 
	 * 			by the <code>encodeUrl</code> method.
	 */
	public static boolean isEncodeUrl() {
		return encodeUrl;
	}

	/**
	 * @return	A {@code String} that defines the character used to separate parameters 
	 * 			in the URL
	 */
	public static String getParametersSeparator() {
		return parametersSeparator;
	}

}
