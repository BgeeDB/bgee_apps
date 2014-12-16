package org.bgee.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class loads the properties for Bgee webapp.
 * The configuration can be a {@code Properties} object injected through 
 * {@link #getBgeeProperties(Properties)} 
 * or loaded from the System properties or via a file named {@code bgee-webapp.properties}
 * put in the classpath, by using {@link #getBgeeProperties()}.
 * 
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
 * @version Bgee 14, August 2014
 * @since Bgee 13
 */
public class BgeeProperties 
{

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(BgeeProperties.class.getName());

    /**
     * A {@code String} that is the key to access to the System property that contains the name
     * of the file in the classpath that is read at the initialization 
     * of {@code BgeeProperties}
     */
    public final static String PROPERTIES_FILE_NAME_KEY = 
            "org.bgee.webapp.properties.file";

    /**
     * A {@code String} that is the key to access to the System property that is read at the 
     * initialization of {@code BgeeProperties} to set the request parameters storage directory
     * @see #getRequestParametersStorageDirectory()
     */
    public final static String REQUEST_PARAMETERS_STORAGE_DIRECTORY_KEY = 
            "org.bgee.webapp.requestParametersStorageDirectory";

    /**
     * A {@code String} that is the key to access to the System property that is read at the 
     * initialization of {@code BgeeProperties} to set the bgee root directory
     * @see #getBgeeRootDirectory()
     */
    public final static String BGEE_ROOT_DIRECTORY_KEY = 
            "org.bgee.webapp.bgeeRootDirectory";

    /**
     * A {@code String} that is the key to access to the System property that is read at the 
     * initialization of {@code BgeeProperties} to set the download root directory
     * @see #getDownloadRootDirectory()
     */
    public final static String DOWNLOAD_ROOT_DIRECTORY_KEY = 
            "org.bgee.webapp.downloadRootDirectory";

    /**
     * A {@code String} that is the key to access to the System property that is read at the 
     * initialization of {@code BgeeProperties} to set the javascript file root directory
     * @see #getJavascriptFilesRootDirectory()
     */
    public final static String JAVASCRIPT_FILES_ROOT_DIRECTORY_KEY = 
            "org.bgee.webapp.javascriptFilesRootDirectory";

    /**
     * A {@code String} that is the key to access to the System property that is read at the 
     * initialization of {@code BgeeProperties} to set the css file root directory
     * @see #getCssFilesRootDirectory()
     */
    public final static String CSS_FILES_ROOT_DIRECTORY_KEY = 
            "org.bgee.webapp.cssFilesRootDirectory";

    /**
     * A {@code String} that is the key to access to the System property that is read at the 
     * initialization of {@code BgeeProperties} to set the images root directory
     * @see #getImagesRootDirectory()
     */
    public final static String IMAGES_ROOT_DIRECTORY_KEY = 
            "org.bgee.webapp.imagesRootDirectory";

    /**
     * A {@code String} that is the key to access to the System property that is read at the 
     * initialization of {@code BgeeProperties} to set the top OBO results url root directory
     * @see #getTopOBOResultsUrlRootDirectory()
     */
    public final static String TOP_OBO_RESULTS_URL_ROOT_DIRECTORY_KEY = 
            "org.bgee.webapp.topOBOResultsUrlRootDirectory";

    /**
     * A {@code String} that is the key to access to the System property that is read at the 
     * initialization of {@code BgeeProperties} to set the url max length.
     * @see #getUrlMaxLength()
     */
    public final static String URL_MAX_LENGTH_KEY = 
            "org.bgee.webapp.urlMaxLength";

    /**
     * A {@code String} that is the key to access to the System property that contains the name
     * of the file in the classpath that is read to initialize the webapp web pages cache
     */
    public final static String WEBPAGES_CACHE_CONFIG_FILE_NAME_KEY = 
            "org.bgee.webapp.webpages-cache.file";

    /**
     * A {@code ConcurrentMap} used to store {@code BgeeProperties}, 
     * associated to their ID as key (corresponding to the ID of the thread 
     * who requested the {@code BgeeProperties}). 
     * <p>
     * This {@code Map} is used to provide a unique and independent 
     * {@code BgeeProperties} instance to each thread: a {@code BgeeProperties} is added 
     * to this {@code Map} when a {@code getBgeeProperties} method is called, 
     * if the thread ID is not already present in the {@code keySet} 
     * of the {@code Map}. Otherwise, the already stored {@code BgeeProperties} 
     * is returned.
     */
    private static final ConcurrentMap<Long, BgeeProperties> bgeeProperties = 
            new ConcurrentHashMap<Long, BgeeProperties>(); 

    /**
     * {@code String} that defines the directory where query strings holding storable parameters  
     * from previous large queries are stored. 
     */
    private final String requestParametersStorageDirectory;

    /**
     * A {@code String} that defines the root of URLs to Bgee, 
     * for instance, "http://bgee.unil.ch/bgee/bgee".
     */
    private final String bgeeRootDirectory;

    /**
     * A {@code String} that defines the root directory where are located files available for download, 
     * to be added to the {@code bgeeRootDirectory} to generate URL to download files.
     */
    private final String downloadRootDirectory;

    /**
     * A {@code String} that defines the root directory where are located javascript files, 
     * to be added to the {@code bgeeRootDirectory} to generate URL to obtain javascript files.
     */
    private final String javascriptFilesRootDirectory;

    /**
     * A {@code String} that defines the root directory where are located css files, 
     * to be added to the {@code bgeeRootDirectory} to generate URL to obtain css files.
     */
    private final String cssFilesRootDirectory;

    /**
     * A {@code String} that defines the root directory where are located images, 
     * to be added to the {@code bgeeRootDirectory} to generate URL to obtain images.
     */
    private final String imagesRootDirectory;

    /**
     * A {@code String} that defines the directory where are stored TopOBO result files, 
     * to be added to the {@code bgeeRootDirectory} to generate URL to obtain result files.
     */
    private final String topOBOResultsUrlRootDirectory;

    /**
     * An {@code Integer} that definesmax length of URLs. Typically, if the URL exceeds the max length, 
     * a key is generated to store and retrieve a query string, 
     * holding the "storable" parameters. The "storable" parameters are removed from the URL, 
     * and replaced by the generated key.
     * <p>
     * The max length of URL is currently 2,083 characters, 
     * because of limitations of IE9. Just to be sure, 
     * because of potential server limitations, the limit should be 1,500 characters.
     * <p>
     * Anyway, we use a much lower limitation, as we do not want too long URL.
     */
    private final Integer urlMaxLength;

    /**
     * {@code String} that contains the name of the web pages cache config file in the
     * resources folder. To reach the file, the resources folder has to be added in front of
     * this properties. The default value is ehcache-webpages.xml
     */
    private final String webpagesCacheConfigFileName;

    /**
     * Private constructor, can be only called through the use of one of the
     * {@code getBgeeProperties} method, the only way for the user to obtain an instance of this
     * class.
     * Try to load the properties from the injected {@code Properties}, or a properties file, 
     * or from the system properties. 
     * Otherwise, set the default values.
     * 
     * @param prop  A {@code java.util.Properties} instance that contains the system properties
     *              to use.
     */
    private BgeeProperties(Properties prop) 
    {
        log.entry(prop);
        log.info("Bgee-webapp properties initialization...");
        // Fetch the existing system properties
        Properties sysProps = new Properties(System.getProperties());
        //try to get the properties file.
        //default name is bgee.properties
        //check first if an alternative name has been provided in the System properties
        String propertyFile = sysProps.getProperty(PROPERTIES_FILE_NAME_KEY, 
                "/bgee-webapp.properties");
        log.debug("Trying to use properties file {}", propertyFile);
        Properties fileProps = null;
        InputStream propStream =
                BgeeProperties.class.getResourceAsStream(propertyFile);
        if (propStream != null) {
            try {
                fileProps = new Properties();
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
            log.debug("{} not found in classpath.", propertyFile);
        }
        // Initialize all properties using the injected prop first, alternatively the System
        // properties and then the file. The default value provided will be use if none of the
        // previous solutions contain the property
        requestParametersStorageDirectory  = getStringOption(prop, sysProps, fileProps, 
                REQUEST_PARAMETERS_STORAGE_DIRECTORY_KEY,  
                System.getProperty("java.io.tmpdir"));
        bgeeRootDirectory  = getStringOption(prop, sysProps, fileProps, 
                BGEE_ROOT_DIRECTORY_KEY, "/");
        downloadRootDirectory  = getStringOption(prop, sysProps, fileProps, 
                DOWNLOAD_ROOT_DIRECTORY_KEY, "download/");
        javascriptFilesRootDirectory  = getStringOption(prop, sysProps, fileProps, 
                JAVASCRIPT_FILES_ROOT_DIRECTORY_KEY, "js/");
        cssFilesRootDirectory  = getStringOption(prop, sysProps, fileProps, 
                CSS_FILES_ROOT_DIRECTORY_KEY, "css/");
        imagesRootDirectory  = getStringOption(prop, sysProps, fileProps, 
                IMAGES_ROOT_DIRECTORY_KEY, "img/");
        topOBOResultsUrlRootDirectory  = getStringOption(prop, sysProps, fileProps, 
                TOP_OBO_RESULTS_URL_ROOT_DIRECTORY_KEY, null);
        urlMaxLength = getIntegerOption(prop, sysProps, fileProps, 
                URL_MAX_LENGTH_KEY, 120);
        webpagesCacheConfigFileName = getStringOption(prop, sysProps, fileProps, 
                WEBPAGES_CACHE_CONFIG_FILE_NAME_KEY, "/ehcache-webpages.xml");
        log.info("Initialization done.");
        log.exit();
    }

    /**
     * @return  An instance of {@code BgeeProperties} with values based on the System properties
     *          or the properties file present in the classpath or the default properties if 
     *          nothing else is available. The method will create an instance only once for 
     *          each thread and always return this instance when called. 
     *          ("per-thread singleton")
     */
    public static BgeeProperties getBgeeProperties(){
        return getBgeeProperties(null);
    }

    /**
     * @param prop  A {@code java.util.Properties} instance that contains the system properties
     *              to use.
     * @return  An instance of {@code BgeeProperties} with values based on the provided
     *          {@code Properties}. The method will create an instance only once for each
     *          thread and always return this instance when called. ("per-thread singleton")
     */
    public static BgeeProperties getBgeeProperties(Properties prop){

        log.entry(prop);
        BgeeProperties bgeeProp;
        long threadId = Thread.currentThread().getId();
        if (! hasBgeeProperties()) {
            // Create an instance
            bgeeProp = new BgeeProperties(prop);
            // Add it to the map
            bgeeProperties.put(threadId, bgeeProp);
        }
        else {
            bgeeProp = bgeeProperties.get(threadId);
        }
        return log.exit(bgeeProp);

    }

    /**
     * Determine whether the {@code Thread} calling this method already 
     * holds a {@code BgeeProperties}. 
     * 
     * @return  A {@code boolean} {@code true} if the {@code Thread} 
     *          calling this method currently holds a {@code BgeeProperties}, 
     *          {@code false} otherwise. 
     */
    public static boolean hasBgeeProperties() {
        log.entry();
        return log.exit(bgeeProperties.containsKey(Thread.currentThread().getId()));
    }

    /**
     * Remove the current instance of {@code BgeeProperties} from the {@code ConcurrentMap} 
     * used to store {@code BgeeProperties}
     */
    public void removeFromBgeePropertiesPool(){
        bgeeProperties.remove(Thread.currentThread().getId());
    }

    /**
     * Try to retrieve the property corresponding to {@code key}, 
     * first from the injected {@code Properties} ({@code prop}), then from the System properties 
     * ({@code sysProps}), then, if undefined or empty, from properties retrieved from the 
     * Bgee property file ({@code fileProps}). If the property is still undefined or empty 
     * return {@code defaultValue}.
     *
     * @param prop          A {@code java.util.Properties} instance that contains the system 
     *                      properties to look for {@code key} first
     * @param sysProps      {@code java.util.Properties} retrieved from System properties, 
     *                      where {@code key} is searched in second
     * @param fileProps     {@code java.util.Properties} retrieved 
     *                      from the Bgee properties file, 
     *                      where {@code key} is searched in if {@code prop} and {@code sysProps}
     *                      were undefined or empty for {@code key}. 
     *                      Can be {@code null} if no properties file was found.
     * @param defaultValue  default value that will be returned if the property 
     *                      is undefined or empty in all {@code Properties}.
     *
     * @return              A {@code String} corresponding to the value
     *                      for that property key. 
     *                      Or {@code defaultValue} if not defined or empty.
     */
    private String getStringOption(Properties prop, Properties sysProps, 
            Properties fileProps, String key, 
            String defaultValue)
    {
        log.entry(prop, sysProps, fileProps,  key, defaultValue);

        String propValue = null;

        if (prop != null) {
            propValue = prop.getProperty(key);
        }

        if (StringUtils.isNotBlank(propValue)) {
            log.debug("Retrieved from injected properties {}={}", key, propValue);
        } else {
            propValue = sysProps.getProperty(key);
            if(StringUtils.isNotBlank(propValue)){
                log.debug("Retrieved from System properties {}={}", key, propValue);
            }
            else{
                if (fileProps != null) {
                    propValue = fileProps.getProperty(key);
                }
                if (StringUtils.isNotBlank(propValue)) {
                    log.debug("Retrieved from properties file {}={}", key, propValue);
                } else {
                    log.debug("Property {} not defined neither in injected properties nor in properties file nor in System properties, using default value {}", 
                            key, defaultValue);
                    propValue = defaultValue; 
                }
            }
        }

        return log.exit(propValue);
    }

    /**
     * Try to retrieve the property corresponding to {@code key}, 
     * first from the injected {@code Properties} ({@code prop}), then from the System properties 
     * ({@code sysProps}), then, if undefined or empty, from properties retrieved from the 
     * Bgee property file ({@code fileProps}). If the property is still undefined or empty 
     * return {@code defaultValue}.
     *
     * @param prop          A {@code java.util.Properties} instance that contains the system 
     *                      properties to look for {@code key} first
     * @param sysProps      {@code java.util.Properties} retrieved from System properties, 
     *                      where {@code key} is searched in second
     * @param fileProps     {@code java.util.Properties} retrieved 
     *                      from the Bgee properties file, 
     *                      where {@code key} is searched in if {@code prop} and {@code sysProps}
     *                      were undefined or empty for {@code key}. 
     *                      Can be {@code null} if no properties file was found.
     * @param defaultValue  default value that will be returned if the property 
     *                      is undefined or empty in all {@code Properties}.
     *
     * @return             An {@code int} corresponding to the value
     *                     for that property key.
     *                     Or {@code defaultValue} if not defined or empty.
     */
    private int getIntegerOption(Properties prop, Properties sysProps, 
            Properties fileProps, String key, 
            int defaultValue)
    {
        log.entry(prop, fileProps, sysProps, key, defaultValue);

        String propValue = this.getStringOption(prop,sysProps, fileProps, key, null);
        int val = defaultValue;
        if (propValue != null) {
            val= Integer.valueOf(propValue);
        }

        return log.exit(val);
    }

    /**
     * @return  A {@code String} that defines the directory where query strings holding storable 
     *          parameters from previous large queries are stored. 
     */
    public String getRequestParametersStorageDirectory() {
        return requestParametersStorageDirectory;
    }

    /**
     * @return  A {@code String} that defines the root of URLs to Bgee, for instance, 
     *          "http://bgee.unil.ch/bgee/bgee".
     */
    public String getBgeeRootDirectory() {
        return bgeeRootDirectory;
    }

    /**
     * @return  A {@code String} that defines the root directory where are located files 
     *          available for download, to be added to the {@code bgeeRootDirectory} to 
     *          generate URL to download files
     */
    public String getDownloadRootDirectory() {
        return downloadRootDirectory;
    }

    /**
     * @return  A {@code String} that defines the root directory where are located javascript 
     *          files, to be added to the {@code bgeeRootDirectory} to generate URL to obtain 
     *          javascript files.
     */
    public String getJavascriptFilesRootDirectory() {
        return javascriptFilesRootDirectory;
    }

    /**
     * @return  A {@code String} that defines the root directory where are located css files, 
     *          to be added to the {@code bgeeRootDirectory} to generate URL to obtain css files.
     */
    public String getCssFilesRootDirectory() {
        return cssFilesRootDirectory;
    }

    /**
     * @return  A {@code String} that defines the root directory where are located images, 
     *          to be added to the {@code bgeeRootDirectory} to generate URL to obtain images.
     */
    public String getImagesRootDirectory() {
        return imagesRootDirectory;
    }

    /**
     * @return  A {@code String} that defines the directory where are stored TopOBO result files, 
     *          to be added to the {@code bgeeRootDirectory} to generate URL to obtain result files.
     */
    public String getTopOBOResultsUrlRootDirectory() {
        return topOBOResultsUrlRootDirectory;
    }

    /**
     * @return  An {@code Integer} that defines max length of URLs. Typically, if the URL 
     *          exceeds the max length, a key is generated to store and retrieve a query 
     *          string, holding the "storable" parameters. The "storable" parameters are
     *          removed from the URL, and replaced by the generated key.
     */
    public Integer getUrlMaxLength() {
        return urlMaxLength;
    }

    /**
     * @return  A {@code String} that contains the name of the web pages cache config file in the
     *          resources folder. To reach the file, the resources folder has to be added in front of
     *          this properties. The default value is ehcache-webpages.xml
     */
    public String getWebpagesCacheConfigFileName() {
        return webpagesCacheConfigFileName;
    }

}
