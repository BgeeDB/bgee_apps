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
     * of {@code BgeeProperties}. The associated value must be provided related to the root 
     * of the classpath (so, must start with {@code /}).
     * 
     * @see #PROPERTIES_FILE_NAME_DEFAULT
     */
    public final static String PROPERTIES_FILE_NAME_KEY = 
            "org.bgee.webapp.properties.file";
    /**
     * A {@code String} that is the default value of the name
     * of the file in the classpath that is read at the initialization 
     * of {@code BgeeProperties}.
     * 
     * @see #PROPERTIES_FILE_NAME_KEY
     */
    public final static String PROPERTIES_FILE_NAME_DEFAULT = "/bgee-webapp.properties";

    /**
     * A {@code String} that is the key to access to the System property that is read at the 
     * initialization of {@code BgeeProperties} to set the request parameters storage directory.
     * 
     * @see #REQUEST_PARAMETERS_STORAGE_DIRECTORY_DEFAULT
     * @see #getRequestParametersStorageDirectory()
     */
    public final static String REQUEST_PARAMETERS_STORAGE_DIRECTORY_KEY = 
            "org.bgee.webapp.requestParametersStorageDirectory";
    /**
     * A {@code String} that is the default value of the request parameters storage directory. 
     * The default is the temp directory, which is not recommended (no long-term 
     * conservation of request parameters).
     * 
     * @see #REQUEST_PARAMETERS_STORAGE_DIRECTORY_KEY
     * @see #getRequestParametersStorageDirectory()
     */
    public final static String REQUEST_PARAMETERS_STORAGE_DIRECTORY_DEFAULT = 
            System.getProperty("java.io.tmpdir");

    /**
     * A {@code String} that is the key to access to the System property that is read at the 
     * initialization of {@code BgeeProperties} to set the bgee root directory.
     * 
     * @see #BGEE_ROOT_DIRECTORY_DEFAULT
     * @see #getBgeeRootDirectory()
     */
    public final static String BGEE_ROOT_DIRECTORY_KEY = 
            "org.bgee.webapp.bgeeRootDirectory";
    /**
     * A {@code String} that is the default value of the bgee root directory.
     * 
     * @see #BGEE_ROOT_DIRECTORY_KEY
     * @see #getBgeeRootDirectory()
     */
    public final static String BGEE_ROOT_DIRECTORY_DEFAULT = "/";

    /**
     * A {@code String} that is the key to access to the System property that is read at the 
     * initialization of {@code BgeeProperties} to set the download root directory. 
     * 
     * @see #DOWNLOAD_ROOT_DIRECTORY_DEFAULT
     * @see #getDownloadRootDirectory()
     */
    public final static String DOWNLOAD_ROOT_DIRECTORY_KEY = 
            "org.bgee.webapp.downloadRootDirectory";
    /**
     * A {@code String} that is the default value of the download root directory. 
     * 
     * @see #DOWNLOAD_ROOT_DIRECTORY_KEY
     * @see #getDownloadRootDirectory()
     */
    public final static String DOWNLOAD_ROOT_DIRECTORY_DEFAULT = "download/";

    /**
     * A {@code String} that is the key to access to the System property that is read at the 
     * initialization of {@code BgeeProperties} to set the expression download files root directory. 
     * 
     * @see #DOWNLOAD_EXPR_FILES_ROOT_DIRECTORY_DEFAULT
     * @see #getDownloadExprFilesRootDirectory()
     */
    public final static String DOWNLOAD_EXPR_FILES_ROOT_DIRECTORY_KEY = 
            "org.bgee.webapp.downloadExprFilesRootDirectory";
    /**
     * A {@code String} that is the default value of the expression download file root directory. 
     * 
     * @see #DOWNLOAD_EXPR_FILES_ROOT_DIRECTORY_KEY
     * @see #getDownloadExprFilesRootDirectory()
     */
    public final static String DOWNLOAD_EXPR_FILES_ROOT_DIRECTORY_DEFAULT = "expressionFiles/";

    /**
     * A {@code String} that is the key to access to the System property that is read at the 
     * initialization of {@code BgeeProperties} to set the differential expression download files
     * root directory. 
     * 
     * @see #DOWNLOAD_DIFF_EXPR_FILES_ROOT_DIRECTORY_DEFAULT
     * @see #getDownloadDiffExprFilesRootDirectory()
     */
    public final static String DOWNLOAD_DIFF_EXPR_FILES_ROOT_DIRECTORY_KEY = 
            "org.bgee.webapp.downloadDiffExprFilesRootDirectory";
    /**
     * A {@code String} that is the default value of the differential expression download files
     * root directory. 
     * 
     * @see #DOWNLOAD_DIFF_EXPR_FILES_ROOT_DIRECTORY_KEY
     * @see #getDownloadDiffExprFilesRootDirectory()
     */
    public final static String DOWNLOAD_DIFF_EXPR_FILES_ROOT_DIRECTORY_DEFAULT = 
            "diffExpressionFiles/";
    
    /**
     * A {@code String} that is the key to access to the System property that is read at the 
     * initialization of {@code BgeeProperties} to set the multi-species differential expression 
     * download files root directory. 
     * 
     * @see #DOWNLOAD_MULTI_DIFF_EXPR_FILES_ROOT_DIRECTORY_DEFAULT
     * @see #getDownloadMultiDiffExprFilesRootDirectory()
     */
    public final static String DOWNLOAD_MULTI_DIFF_EXPR_FILES_ROOT_DIRECTORY_KEY = 
            "org.bgee.webapp.downloadMultiDiffExprFilesRootDirectory";
    /**
     * A {@code String} that is the default value of the multi-species differential expression
     * download file root directory. 
     * 
     * @see #DOWNLOAD_MULTI_DIFF_EXPR_FILES_ROOT_DIRECTORY_KEY
     * @see #getDownloadMultiDiffExprFilesRootDirectory()
     */
    public final static String DOWNLOAD_MULTI_DIFF_EXPR_FILES_ROOT_DIRECTORY_DEFAULT = 
            "multiDiffExpressionFiles/";

    /**
     * A {@code String} that is the key to access to the System property that is read at the 
     * initialization of {@code BgeeProperties} to set the ortholog download files root directory. 
     * 
     * @see #DOWNLOAD_ORTHOLOG_FILES_ROOT_DIRECTORY_DEFAULT
     * @see #getDownloadOrthologFilesRootDirectory()
     */
    public final static String DOWNLOAD_ORTHOLOG_FILES_ROOT_DIRECTORY_KEY = 
            "org.bgee.webapp.downloadOrthologFilesRootDirectory";
    /**
     * A {@code String} that is the default value of the ortholog download files root directory. 
     * 
     * @see #DOWNLOAD_ORTHOLOG_FILES_ROOT_DIRECTORY_KEY
     * @see #getDownloadOrthologFilesRootDirectory()
     */
    public final static String DOWNLOAD_ORTHOLOG_FILES_ROOT_DIRECTORY_DEFAULT = 
            "orthologFiles/";
    
    /**
     * A {@code String} that is the key to access to the System property that is read at the 
     * initialization of {@code BgeeProperties} to set the Affymetrix processed expression value 
     * download files root directory. 
     * 
     * @see #DOWNLOAD_AFFY_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_DEFAULT
     * @see #getDownloadAffyProcExprValueFilesRootDirectory()
     */
    public final static String DOWNLOAD_AFFY_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_KEY = 
            "org.bgee.webapp.downloadAffyProcExprValueFilesRootDirectory";
    /**
     * A {@code String} that is the default value of the Affymetrix processed expression value
     * download files root directory. 
     * 
     * @see #DOWNLOAD_AFFY_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_KEY
     * @see #getDownloadAffyProcExprValueFilesRootDirectory()
     */
    public final static String DOWNLOAD_AFFY_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_DEFAULT = 
            "processed_expr_values/affymetrix/";
    /**
     * A {@code String} that is the key to access to the System property that is read at the 
     * initialization of {@code BgeeProperties} to set the RNA-Seq processed expression value 
     * download files root directory. 
     * 
     * @see #DOWNLOAD_RNA_SEQ_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_DEFAULT
     * @see #getDownloadRNASeqProcExprValueFilesRootDirectory()
     */
    public final static String DOWNLOAD_RNA_SEQ_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_KEY = 
            "org.bgee.webapp.downloadRNASeqProcExprValueFilesRootDirectory";
    /**
     * A {@code String} that is the default value of the RNA-Seq processed expression value
     * download files root directory. 
     * 
     * @see #DOWNLOAD_RNA_SEQ_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_KEY
     * @see #getDownloadRNASeqProcExprValueFilesRootDirectory()
     */
    public final static String DOWNLOAD_RNA_SEQ_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_DEFAULT = 
            "processed_expr_values/rna_seq/";
    
    /**
     * A {@code String} that is the key to access to the System property that is read at the 
     * initialization of {@code BgeeProperties} to set the javascript file root directory. 
     * 
     * @see #JAVASCRIPT_FILES_ROOT_DIRECTORY_DEFAULT
     * @see #getJavascriptFilesRootDirectory()
     */
    public final static String JAVASCRIPT_FILES_ROOT_DIRECTORY_KEY = 
            "org.bgee.webapp.javascript.directory";
    /**
     * A {@code String} that is the default value of the javascript file root directory. 
     * 
     * @see #JAVASCRIPT_FILES_ROOT_DIRECTORY_KEY
     * @see #getJavascriptFilesRootDirectory()
     */
    public final static String JAVASCRIPT_FILES_ROOT_DIRECTORY_DEFAULT = "js/";
    /**
     * A {@code String} that is the key to access to the System property that is read at the 
     * initialization of {@code BgeeProperties} to set the javascript version extension. 
     * 
     * @see #JAVASCRIPT_VERSION_EXTENSION_DEFAULT
     * @see #getJavascriptVersionExtension()
     */
    public final static String JAVASCRIPT_VERSION_EXTENSION_KEY = 
            "org.bgee.webapp.javascript.version.extension";
    /**
     * A {@code String} that is the default value of the javascript version extension. 
     * 
     * @see #JAVASCRIPT_VERSION_EXTENSION_KEY
     * @see #getJavascriptVersionExtension()
     */
    public final static String JAVASCRIPT_VERSION_EXTENSION_DEFAULT = "";

    /**
     * A {@code String} that is the key to access to the System property that is read at the 
     * initialization of {@code BgeeProperties} to set the css file root directory. 
     * 
     * @see #CSS_FILES_ROOT_DIRECTORY_DEFAULT
     * @see #getCssFilesRootDirectory()
     */
    public final static String CSS_FILES_ROOT_DIRECTORY_KEY = 
            "org.bgee.webapp.css.directory";
    /**
     * A {@code String} that is the default value of the css file root directory. 
     * 
     * @see #CSS_FILES_ROOT_DIRECTORY_KEY
     * @see #getCssFilesRootDirectory()
     */
    public final static String CSS_FILES_ROOT_DIRECTORY_DEFAULT = "css/";
    /**
     * A {@code String} that is the key to access to the System property that is read at the 
     * initialization of {@code BgeeProperties} to set the css version extension. 
     * 
     * @see #CSS_VERSION_EXTENSION_DEFAULT
     * @see #getCssVersionExtension()
     */
    public final static String CSS_VERSION_EXTENSION_KEY = 
            "org.bgee.webapp.css.version.extension";
    /**
     * A {@code String} that is the default value of the css version extension. 
     * 
     * @see #CSS_VERSION_EXTENSION_KEY
     * @see #getCssVersionExtension()
     */
    public final static String CSS_VERSION_EXTENSION_DEFAULT = "";

    /**
     * A {@code String} that is the key to access to the System property that is read at the 
     * initialization of {@code BgeeProperties} to set the images root directory. 
     * 
     * @see #IMAGES_ROOT_DIRECTORY_DEFAULT
     * @see #getImagesRootDirectory()
     */
    public final static String IMAGES_ROOT_DIRECTORY_KEY = 
            "org.bgee.webapp.imagesRootDirectory";
    /**
     * A {@code String} that is the default value of the images root directory. 
     * 
     * @see #IMAGES_ROOT_DIRECTORY_KEY
     * @see #getImagesRootDirectory()
     */
    public final static String IMAGES_ROOT_DIRECTORY_DEFAULT = "img/";

    /**
     * A {@code String} that is the key to access to the System property that is read at the 
     * initialization of {@code BgeeProperties} to set the top OBO results url root directory. 
     * 
     * @see #TOP_OBO_RESULTS_URL_ROOT_DIRECTORY_DEFAULT
     * @see #getTopOBOResultsUrlRootDirectory()
     */
    public final static String TOP_OBO_RESULTS_URL_ROOT_DIRECTORY_KEY = 
            "org.bgee.webapp.topOBOResultsUrlRootDirectory";
    /**
     * A {@code String} that is the default value of the top OBO results url root directory. 
     * 
     * @see #TOP_OBO_RESULTS_URL_ROOT_DIRECTORY_KEY
     * @see #getTopOBOResultsUrlRootDirectory()
     */
    public final static String TOP_OBO_RESULTS_URL_ROOT_DIRECTORY_DEFAULT = null;

    /**
     * A {@code String} that is the key to access to the System property that is read at the 
     * initialization of {@code BgeeProperties} to set the url max length.
     * 
     * @see #URL_MAX_LENGTH_DEFAULT
     * @see #getUrlMaxLength()
     */
    public final static String URL_MAX_LENGTH_KEY = 
            "org.bgee.webapp.urlMaxLength";
    /**
     * An {@code int} that is the default value of the url max length.
     * 
     * @see #URL_MAX_LENGTH_KEY
     * @see #getUrlMaxLength()
     */
    public final static int URL_MAX_LENGTH_DEFAULT = 120;

    /**
     * A {@code String} that is the key to access to the System property that contains the name
     * of the file in the classpath that is read to initialize the webapp web pages cache. 
     * The associated value must be provided related to the root of the classpath (so, 
     * must start with {@code /}).
     * 
     * @see #WEBPAGES_CACHE_CONFIG_FILE_NAME_DEFAULT
     */
    public final static String WEBPAGES_CACHE_CONFIG_FILE_NAME_KEY = 
            "org.bgee.webapp.webpages-cache.file";
    /**
     * A {@code String} that is the default value of the name
     * of the file in the classpath that is read to initialize the webapp web pages cache. 
     * Must start with {@code /} (root of the classpath).
     * 
     * @see #WEBPAGES_CACHE_CONFIG_FILE_NAME_KEY
     */
    public final static String WEBPAGES_CACHE_CONFIG_FILE_NAME_DEFAULT = 
            "/ehcache-webpages.xml";

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
     * A {@code String} that defines the expression download file directory where are located 
     * expression files available for download.
     */
    private final String downloadExprFilesRootDirectory;
    
    /**
     * A {@code String} that defines the differential expression download file directory where are 
     * located differential expression files available for download.
     */
    private final String downloadDiffExprFilesRootDirectory;
    
    /**
     * A {@code String} that defines the Affymetrix processed expression value download file
     * directory where  are located processed expression value files available for download.
     */
    private final String downloadAffyProcExprValueFilesRootDirectory;
    /**
     * A {@code String} that defines the RNA-Seq processed expression value download file
     * directory where  are located processed expression value files available for download.
     */
    private final String downloadRNASeqProcExprValueFilesRootDirectory;
    
    /**
     * A {@code String} that defines the multi-species differential expression download file 
     * directory where are located multi-species differential expression files available for 
     * download, to be added to the {@code bgeeRootDirectory} to generate URL to multi-species 
     * differential expression download files.
     */
    private final String downloadMultiDiffExprFilesRootDirectory;
    
    /**
     * A {@code String} that defines the ortholog download file directory where are located 
     * ortholog files available for download, to be added to the {@code bgeeRootDirectory} to 
     * generate URL to ortholog download files.
     */
    private final String downloadOrthologFilesRootDirectory;
    
    /**
     * A {@code String} that defines the root directory where are located javascript files, 
     * to be added to the {@code bgeeRootDirectory} to generate URL to obtain javascript files.
     */
    private final String javascriptFilesRootDirectory;
    /**
     * A {@code String} defining the version extension to generate versioned javascript file names.
     * For instance, if this attribute is equal to "-13", a js file originally named "common.js" 
     * should be used with name "common-13.js".
     * 
     */
    private final String javascriptVersionExtension;

    /**
     * A {@code String} that defines the root directory where are located css files, 
     * to be added to the {@code bgeeRootDirectory} to generate URL to obtain css files.
     */
    private final String cssFilesRootDirectory;
    /**
     * A {@code String} defining the version extension to generate versioned CSS file names.
     * For instance, if this attribute is equal to "-13", a css file originally named "bgee.css" 
     * should be used with name "bgee-13.css".
     * 
     */
    private final String cssVersionExtension;

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
     * An {@code Integer} that defines max length of URLs. Typically, if the URL exceeds the max length, 
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
    private final int urlMaxLength;

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
                PROPERTIES_FILE_NAME_DEFAULT);
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
                REQUEST_PARAMETERS_STORAGE_DIRECTORY_DEFAULT);
        bgeeRootDirectory  = getStringOption(prop, sysProps, fileProps, 
                BGEE_ROOT_DIRECTORY_KEY, BGEE_ROOT_DIRECTORY_DEFAULT);
        downloadRootDirectory  = getStringOption(prop, sysProps, fileProps, 
                DOWNLOAD_ROOT_DIRECTORY_KEY, DOWNLOAD_ROOT_DIRECTORY_DEFAULT);
        downloadExprFilesRootDirectory  = getStringOption(prop, sysProps, fileProps, 
                DOWNLOAD_EXPR_FILES_ROOT_DIRECTORY_KEY, DOWNLOAD_EXPR_FILES_ROOT_DIRECTORY_DEFAULT);
        downloadDiffExprFilesRootDirectory  = getStringOption(prop, sysProps, fileProps, 
                DOWNLOAD_DIFF_EXPR_FILES_ROOT_DIRECTORY_KEY, 
                DOWNLOAD_DIFF_EXPR_FILES_ROOT_DIRECTORY_DEFAULT);
        downloadMultiDiffExprFilesRootDirectory = getStringOption(prop, sysProps, fileProps, 
                DOWNLOAD_MULTI_DIFF_EXPR_FILES_ROOT_DIRECTORY_KEY, 
                DOWNLOAD_MULTI_DIFF_EXPR_FILES_ROOT_DIRECTORY_DEFAULT);
        downloadOrthologFilesRootDirectory = getStringOption(prop, sysProps, fileProps, 
                DOWNLOAD_ORTHOLOG_FILES_ROOT_DIRECTORY_KEY, 
                DOWNLOAD_ORTHOLOG_FILES_ROOT_DIRECTORY_DEFAULT);
        downloadAffyProcExprValueFilesRootDirectory = getStringOption(prop, sysProps, fileProps, 
                DOWNLOAD_AFFY_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_KEY, 
                DOWNLOAD_AFFY_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_DEFAULT);
        downloadRNASeqProcExprValueFilesRootDirectory = getStringOption(prop, sysProps, fileProps, 
                DOWNLOAD_RNA_SEQ_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_KEY, 
                DOWNLOAD_RNA_SEQ_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_DEFAULT);
        javascriptFilesRootDirectory  = getStringOption(prop, sysProps, fileProps, 
                JAVASCRIPT_FILES_ROOT_DIRECTORY_KEY, JAVASCRIPT_FILES_ROOT_DIRECTORY_DEFAULT);
        javascriptVersionExtension  = getStringOption(prop, sysProps, fileProps, 
                JAVASCRIPT_VERSION_EXTENSION_KEY, JAVASCRIPT_VERSION_EXTENSION_DEFAULT);
        cssFilesRootDirectory  = getStringOption(prop, sysProps, fileProps, 
                CSS_FILES_ROOT_DIRECTORY_KEY, CSS_FILES_ROOT_DIRECTORY_DEFAULT);
        cssVersionExtension  = getStringOption(prop, sysProps, fileProps, 
                CSS_VERSION_EXTENSION_KEY, CSS_VERSION_EXTENSION_DEFAULT);
        imagesRootDirectory  = getStringOption(prop, sysProps, fileProps, 
                IMAGES_ROOT_DIRECTORY_KEY, IMAGES_ROOT_DIRECTORY_DEFAULT);
        topOBOResultsUrlRootDirectory  = getStringOption(prop, sysProps, fileProps, 
                TOP_OBO_RESULTS_URL_ROOT_DIRECTORY_KEY, 
                TOP_OBO_RESULTS_URL_ROOT_DIRECTORY_DEFAULT);
        urlMaxLength = getIntegerOption(prop, sysProps, fileProps, 
                URL_MAX_LENGTH_KEY, URL_MAX_LENGTH_DEFAULT);
        webpagesCacheConfigFileName = getStringOption(prop, sysProps, fileProps, 
                WEBPAGES_CACHE_CONFIG_FILE_NAME_KEY, WEBPAGES_CACHE_CONFIG_FILE_NAME_DEFAULT);
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
            int defaultValue) {
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
     * @return  A {@code String} that defines the absolute root directory where are located 
     *          expression files available for download, to generate URL to download files.
     * @see #DOWNLOAD_EXPR_FILES_ROOT_DIRECTORY_KEY
     * @see #DOWNLOAD_EXPR_FILES_ROOT_DIRECTORY_DEFAULT
     */
    public String getDownloadExprFilesRootDirectory() {
        return downloadExprFilesRootDirectory;
    }
    
    /**
     * @return  A {@code String} that defines the absolute root directory where are located 
     *          differential expression files available for download, to generate URL 
     *          to download files.
     * @see #DOWNLOAD_DIFF_EXPR_FILES_ROOT_DIRECTORY_KEY
     * @see #DOWNLOAD_DIFF_EXPR_FILES_ROOT_DIRECTORY_DEFAULT
     */
    public String getDownloadDiffExprFilesRootDirectory() {
        return downloadDiffExprFilesRootDirectory;
    }
    
    /**
     * @return  A {@code String} that defines the absolute root directory where are located 
     *          Affymetrix processed expression value files available for download, to generate URL 
     *          to download files.
     * @see #DOWNLOAD_AFFY_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_KEY
     * @see #DOWNLOAD_AFFY_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_DEFAULT
     */
    public String getDownloadAffyProcExprValueFilesRootDirectory() {
        return downloadAffyProcExprValueFilesRootDirectory;
    }
    /**
     * @return  A {@code String} that defines the absolute root directory where are located 
     *          RNA-Seq processed expression value files available for download, to generate URL 
     *          to download files.
     * @see #DOWNLOAD_RNA_SEQ_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_KEY
     * @see #DOWNLOAD_RNA_SEQ_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_DEFAULT
     */
    public String getDownloadRNASeqProcExprValueFilesRootDirectory() {
        return downloadRNASeqProcExprValueFilesRootDirectory;
    }
    
    /**
     * @return  A {@code String} that defines the absolute root directory where are located 
     *          multi-species differential expression files available for download, to generate URL 
     *          to download files.
     * @see #DOWNLOAD_MULTI_DIFF_EXPR_FILES_ROOT_DIRECTORY_KEY
     * @see #DOWNLOAD_MULTI_DIFF_EXPR_FILES_ROOT_DIRECTORY_DEFAULT
     */
    public String getDownloadMultiDiffExprFilesRootDirectory() {
        return downloadMultiDiffExprFilesRootDirectory;
    }
    
    /**
     * @return  A {@code String} that defines the absolute root directory where are located 
     *          ortholog files available for download, to generate URL to download files.
     * @see #DOWNLOAD_ORTHOLOG_FILES_ROOT_DIRECTORY_KEY
     * @see #DOWNLOAD_ORTHOLOG_FILES_ROOT_DIRECTORY_DEFAULT
     */
    public String getDownloadOrthologFilesRootDirectory() {
        return downloadOrthologFilesRootDirectory;
    }
    /**
     * @return  A {@code String} that defines the root directory where are located javascript 
     *          files, to be added to the {@code bgeeRootDirectory} to generate URL to obtain 
     *          javascript files.
     * @see #JAVASCRIPT_FILES_ROOT_DIRECTORY_KEY
     * @see #JAVASCRIPT_FILES_ROOT_DIRECTORY_DEFAULT
     */
    public String getJavascriptFilesRootDirectory() {
        return javascriptFilesRootDirectory;
    }
    /**
     * @return  A {@code String} defining the version extension to generate versioned javscript 
     *          file names. For instance, if this attribute is equal to "-13", a javscript file 
     *          originally named "common.js" should be used with name "common-13.js".
     * @see #JAVASCRIPT_VERSION_EXTENSION_KEY
     * @see #JAVSCRIPT_VERSION_EXTENSION_DEFAULT
     */
    public String getJavascriptVersionExtension() {
        return javascriptVersionExtension;
    }

    /**
     * @return  A {@code String} that defines the root directory where are located css files, 
     *          to be added to the {@code bgeeRootDirectory} to generate URL to obtain css files.
     * @see #CSS_FILES_ROOT_DIRECTORY_KEY
     * @see #CSS_FILES_ROOT_DIRECTORY_DEFAULT
     */
    public String getCssFilesRootDirectory() {
        return cssFilesRootDirectory;
    }
    /**
     * @return  A {@code String} defining the version extension to generate versioned CSS 
     *          file names. For instance, if this attribute is equal to "-13", a css file 
     *          originally named "bgee.css" should be used with name "bgee-13.css".
     * @see #CSS_VERSION_EXTENSION_KEY
     * @see #CSS_VERSION_EXTENSION_DEFAULT
     */
    public String getCssVersionExtension() {
        return cssVersionExtension;
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
    public int getUrlMaxLength() {
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
