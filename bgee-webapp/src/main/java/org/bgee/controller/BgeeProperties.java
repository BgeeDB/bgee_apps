package org.bgee.controller;

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.call.Call.ExpressionCall;

/**
 * This class loads the properties for Bgee webapp and extends {@link BgeeProperties} from
 * the Bgee core module.
 * The configuration can be a {@code Properties} object injected through 
 * {@link #getBgeeProperties(Properties)} 
 * or loaded from the System properties or via a file named {@code bgee.properties}
 * put in the Bgee webapp classpath, by using {@link #getBgeeProperties()}.
 
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
 * @author  Frederic Bastian
 * @author  Mathieu Seppey
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Feb. 2018
 * @since   Bgee 13
 */
public class BgeeProperties extends org.bgee.model.BgeeProperties
{

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(BgeeProperties.class.getName());
    
    /**
     * A {@code String} that is the key to access to the property that is read at the 
     * initialization of {@code BgeeProperties} to determine whether CSS and JS files were minified.
     * 
     * @see #MINIFY_DEFAULT
     * @see #isMinify()
     */
    public final static String MINIFY_KEY = "org.bgee.webapp.minify";
    /**
     * A {@code boolean} that is the default value of the property to determine whether 
     * CSS and JS files were minified.
     * 
     * @see #MINIFY_KEY
     * @see #isMinify()
     */
    public final static boolean MINIFY_DEFAULT = false;
        
    /**
     * A {@code String} that is a warning message to be displayed on all pages. 
     * Useful, e.g., to warn users about a downtime to come.
     * 
     * @see #WARNING_MESSAGE_DEFAULT
     * @see #getWarningMessage()
     */
    public final static String WARNING_MESSAGE_KEY = "org.bgee.webapp.warning.message";

    /**
     * A {@code String} that is the default value of the warning message to be displayed on all pages. 
     * 
     * @see #WARNING_MESSAGE_KEY
     * @see #getWarningMessage()
     */
    public final static String WARNING_MESSAGE_DEFAULT = "";

    /**
     * A {@code String} that is the key to access to the property that is read at the 
     * initialization of {@code BgeeProperties} to determine whether web app is an archive or not.
     *
     * @see #ARCHIVE_DEFAULT
     * @see #isArchive()
     */
    public final static String ARCHIVE_KEY = "org.bgee.webapp.archive";
    /**
     * A {@code boolean} that is the default value of the property to determine 
     * whether web app is an archive or not.
     *
     * @see #ARCHIVE_KEY
     * @see #isArchive()
     */
    public final static boolean ARCHIVE_DEFAULT = false;

    /**
     * A {@code String} that is the key to access to the System property that is read at the 
     * initialization of {@code BgeeProperties} to set the bgee current url (e.g., 'https://www.bgee.org').
     *
     * @see #BGEE_CURRENT_URL_DEFAULT
     * @see #getBgeeCurrentUrl()
     */
    public final static String BGEE_CURRENT_URL_KEY = "org.bgee.webapp.bgeeCurrentUrl";
    /**
     * A {@code String} that is the default value of the bgee current url.
     *
     * @see #BGEE_CURRENT_URL_KEY
     * @see #getBgeeCurrentUrl()
     */
    public final static String BGEE_CURRENT_URL_DEFAULT = null;

    /**
     * A {@code String} that is the key to access to the System property that is read at the
     * initialization of {@code BgeeProperties} to set the stable frontend url
     * (e.g., 'https://www.bgee.org/bgee14_2/').
     *
     * @see #STABLE_FRONTEND_URL_DEFAULT
     * @see #getStableFrontendUrl()
     */
    public final static String STABLE_FRONTEND_URL_KEY = "org.bgee.webapp.stableFrontendUrl";

    /**
     * A {@code String} that is the default value of the stable frontend url.
     *
     * @see #STABLE_FRONTEND_URL_KEY
     * @see #getStableFrontendUrl()
     */
    public final static String STABLE_FRONTEND_URL_DEFAULT = null;

    /**
     * A {@code String} that is the key to access to the System property that is read at the
     * initialization of {@code BgeeProperties} to set the frontend url (e.g., 'https://www.bgee.org/').
     *
     * @see #FRONTEND_URL_DEFAULT
     * @see #getFrontendUrl()
     */
    public final static String FRONTEND_URL_KEY = "org.bgee.webapp.frontendUrl";

    /**
     * A {@code String} that is the default value of the frontend url.
     *
     * @see #FRONTEND_URL_KEY
     * @see #getFrontendUrl()
     */
    public final static String FRONTEND_URL_DEFAULT = null;

    /**
     * A {@code String} that is the key to access to the System property that is read at the
     * initialization of {@code BgeeProperties} to set the SPARQL current url (e.g., 'https://www.bgee.org/sparql').
     *
     * @see #SPARQL_CURRENT_URL_KEY
     * @see #getSparqlCurrentUrl()
     */
    public final static String SPARQL_CURRENT_URL_KEY = "org.bgee.webapp.sparqlCurrentUrl";
    /**
     * A {@code String} that is the default value of the SPARQL current url.
     *
     * @see #SPARQL_CURRENT_URL_DEFAULT
     * @see #getSparqlCurrentUrl()
     */
    public final static String SPARQL_CURRENT_URL_DEFAULT = null;

    /**
     * A {@code String} that is the key to access to the System property that is read at the
     * initialization of {@code BgeeProperties} to set the SPARQL stable url (e.g., 'https://www.bgee.org/sparql_14').
     *
     * @see #SPARQL_STABLE_URL_KEY
     * @see #getSparqlStableUrl()
     */
    public final static String SPARQL_STABLE_URL_KEY = "org.bgee.webapp.sparqlStableUrl";
    /**
     * A {@code String} that is the default value of the SPARQL stable url.
     *
     * @see #SPARQL_CURRENT_URL_DEFAULT
     * @see #getSparqlStableUrl()
     */
    public final static String SPARQL_STABLE_URL_DEFAULT = null;

    /**
     * A {@code String} that is the key to access to the System property that is read at the
     * initialization of {@code BgeeProperties} to set the SPARQL stable graph (e.g., 'https://www.bgee.org/rdf_v14').
     *
     * @see #SPARQL_STABLE_GRAPH
     * @see #getSparqlStableGraph()
     */
    public final static String SPARQL_STABLE_GRAPH_KEY = "org.bgee.webapp.sparqlStableGraph";
    /**
     * A {@code String} that is the default value of the SPARQL stable graph.
     *
     * @see #SPARQL_STABLE_GRAPH
     * @see #getSparqlStableGraph()
     */
    public final static String SPARQL_STABLE_GRAPH_DEFAULT = null;

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
    public final static String BGEE_ROOT_DIRECTORY_KEY = "org.bgee.webapp.bgeeRootDirectory";
    /**
     * A {@code String} that is the default value of the bgee root directory.
     * 
     * @see #BGEE_ROOT_DIRECTORY_KEY
     * @see #getBgeeRootDirectory()
     */
    public final static String BGEE_ROOT_DIRECTORY_DEFAULT = "/";
    
    /**
     * A {@code String} that is the key to access to the System property that is read at the 
     * initialization of {@code BgeeProperties} to set the stable bgee root directory.
     *
     * @see #BGEE_STABLE_ROOT_DIRECTORY_DEFAULT
     * @see #getBgeeStableRootDirectory()
     */
    public final static String BGEE_STABLE_ROOT_DIRECTORY_KEY = "org.bgee.webapp.bgeeStableRootDirectory";
    /**
     * A {@code String} that is the default value of the stable bgee root directory.
     *
     * @see #BGEE_STABLE_ROOT_DIRECTORY_KEY
     * @see #getBgeeStableRootDirectory()
     */
    public final static String BGEE_STABLE_ROOT_DIRECTORY_DEFAULT = "/";

    /**
     * A {@code String} that is the key to access to the System property that is read at the 
     * initialization of {@code BgeeProperties} to set the bgee root domain (e.g., '.bgee.org').
     * 
     * @see #BGEE_ROOT_DOMAIN_DEFAULT
     * @see #getBgeeRootDomain()
     */
    public final static String BGEE_ROOT_DOMAIN_KEY = "org.bgee.webapp.bgeeRootDomain";
    /**
     * A {@code String} that is the default value of the bgee root directory.
     * 
     * @see #BGEE_ROOT_DOMAIN_KEY
     * @see #getBgeeRootDomain()
     */
    public final static String BGEE_ROOT_DOMAIN_DEFAULT = null;
    
    
    
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
     * initialization of {@code BgeeProperties} to set the logo images root directory. 
     * 
     * @see #LOGO_IMAGES_ROOT_DIRECTORY_DEFAULT
     * @see #getLogoImagesRootDirectory()
     */
    public final static String LOGO_IMAGES_ROOT_DIRECTORY_KEY = 
            "org.bgee.webapp.logoImagesRootDirectory";
    /**
     * A {@code String} that is the default value of the logo images root directory. 
     * 
     * @see #LOGO_IMAGES_ROOT_DIRECTORY_KEY
     * @see #getLogoImagesRootDirectory()
     */
    public final static String LOGO_IMAGES_ROOT_DIRECTORY_DEFAULT = "img/logo/";

    /**
     * A {@code String} that is the key to access to the System property that is read at the 
     * initialization of {@code BgeeProperties} to set the species images root directory. 
     * 
     * @see #SPECIES_IMAGES_ROOT_DIRECTORY_DEFAULT
     * @see #getSpeciesImagesRootDirectory()
     */
    public final static String SPECIES_IMAGES_ROOT_DIRECTORY_KEY = 
            "org.bgee.webapp.speciesImagesRootDirectory";
    /**
     * A {@code String} that is the default value of the species images root directory. 
     * 
     * @see #SPECIES_IMAGES_ROOT_DIRECTORY_KEY
     * @see #getSpeciesImagesRootDirectory()
     */
    public final static String SPECIES_IMAGES_ROOT_DIRECTORY_DEFAULT = "img/species/";

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
     * A {@code String} that is the key to access to the System property that contains the name 
     * of the path to be used in URL to link to a file stored in the TopAnat result directory 
     * (see {@code org.bgee.model.BgeeProperties#topAnatResultsWritingDirectory}).
     * 
     * @see #TOP_ANAT_RESULTS_URL_DIRECTORY_DEFAULT
     */
    public final static String TOP_ANAT_RESULTS_URL_DIRECTORY_KEY = 
            "org.bgee.webapp.topAnatResultsUrlDirectory";

    /**
     * A {@code String} that is the default value of the name of the path to be used in URL 
     * to link to a file stored in the TopAnat result directory
     * (see {@code org.bgee.model.BgeeProperties#topAnatResultsWritingDirectory}).
     * 
     * @see #TOP_ANAT_RESULTS_URL_DIRECTORY_KEY
     */
    public final static String TOP_ANAT_RESULTS_URL_DIRECTORY_DEFAULT = 
            "bgee/TopAnatFiles/results/";   

    /**
     * A {@code String} that is the key to access to the property containing 
     * the URI providing the parameters to send emails.
     * 
     * @see #MAIL_URI_DEFAULT
     * @see #getMailUri()
     */
    public final static String MAIL_URI_KEY = "org.bgee.webapp.mailUri";
    /**
     * A {@code String} that is the default value of the property containing 
     * the URI providing the parameters to send emails.
     * 
     * @see #MAIL_URI_KEY
     * @see #getMailUri()
     */
    public final static String MAIL_URI_DEFAULT = null;  
    /**
     * A {@code String} that is the key to access to the property containing 
     * the minimum waiting time between sending two mails.
     * 
     * @see #MAIL_WAIT_TIME_DEFAULT
     * @see #getMailWaitTime()
     */
    public final static String MAIL_WAIT_TIME_KEY = "org.bgee.webapp.mailWaitTime";
    /**
     * An {@code int} that is the default value of the property containing 
     * the minimum waiting time between sending two mails.
     * 
     * @see #MAIL_WAIT_TIME_KEY
     * @see #getMailWaitTime()
     */
    public final static int MAIL_WAIT_TIME_DEFAULT = 10000;  

    /**
     * A {@code String} that is the key to access to the property containing 
     * the mail address which to send mails related to TopAnat from.
     * 
     * @see #TOPANAT_FROM_ADDRESS_DEFAULT
     * @see #getTopAnatFromAddress()
     */
    public final static String TOPANAT_FROM_ADDRESS_KEY = "org.bgee.webapp.topAnatFromAddress";
    /**
     * A {@code String} that is the default value of the property containing 
     * the mail address which to send mails related to TopAnat from.
     * 
     * @see #TOPANAT_FROM_ADDRESS_KEY
     * @see #getTopAnatFromAddress()
     */
    public final static String TOPANAT_FROM_ADDRESS_DEFAULT = null;  

    /**
     * A {@code String} that is the key to access to the property containing 
     * the mail personal which to send mails related to TopAnat from.
     * 
     * @see #TOPANAT_FROM_PERSONAL_DEFAULT
     * @see #getTopAnatFromPersonal()
     */
    public final static String TOPANAT_FROM_PERSONAL_KEY = "org.bgee.webapp.topAnatFromPersonal";
    /**
     * A {@code String} that is the default value of the property containing 
     * the mail personal which to send mails related to TopAnat from.
     * 
     * @see #TOPANAT_FROM_PERSONAL_KEY
     * @see #getTopAnatFromPersonal()
     */
    public final static String TOPANAT_FROM_PERSONAL_DEFAULT = null;  

    public final static String FEEDBACK_FROM_ADDRESS_KEY = "org.bgee.webapp.feedbackFromAddress";
    public final static String FEEDBACK_FROM_ADDRESS_DEFAULT = null;
    public final static String FEEDBACK_FROM_PERSONAL_KEY = "org.bgee.webapp.feedbackFromPersonal";
    public final static String FEEDBACK_FROM_PERSONAL_DEFAULT = null;
    public final static String FEEDBACK_SEND_TO_KEY = "org.bgee.webapp.feedbackSendTo";
    public final static String FEEDBACK_SEND_TO_DEFAULT = null;
    

    /**
     * A {@code String} that is the key to access to the property containing 
     * the clustering method to use to cluster {@code ExpressionCall}s based on 
     * their global mean rank.
     * 
     * @see #GENE_SCORE_CLUSTERING_METHOD_DEFAULT
     * @see #getGeneScoreClusteringMethod()
     */
    public final static String GENE_SCORE_CLUSTERING_METHOD_KEY = "org.bgee.webapp.geneScoreClusteringMethod";
    /**
     * A {@code String} that is the default value of the property containing 
     * the clustering method to use to cluster {@code ExpressionCall}s based on 
     * their global mean rank.
     * 
     * @see #GENE_SCORE_CLUSTERING_METHOD_KEY
     * @see #getGeneScoreClusteringMethod()
     */
    public final static String GENE_SCORE_CLUSTERING_METHOD_DEFAULT = 
            ExpressionCall.DEFAULT_CLUSTERING_METHOD.name();  
    /**
     * A {@code String} that is the key to access to the property containing 
     * the distance threshold used when clustering {@code ExpressionCall}s based on 
     * their global mean rank.
     * 
     * @see #GENE_SCORE_CLUSTERING_THRESHOLD_DEFAULT
     * @see #getGeneScoreClusteringThreshold()
     */
    public final static String GENE_SCORE_CLUSTERING_THRESHOLD_KEY = "org.bgee.webapp.geneScoreClusteringThreshold";
    /**
     * A {@code String} that is the default value of the property containing 
     * the distance threshold used when clustering {@code ExpressionCall}s based on 
     * their global mean rank.
     * 
     * @see #GENE_SCORE_CLUSTERING_THRESHOLD_KEY
     * @see #getGeneScoreClusteringThreshold()
     */
    public final static Double GENE_SCORE_CLUSTERING_THRESHOLD_DEFAULT = 
            ExpressionCall.DEFAULT_DISTANCE_THRESHOLD; 

    /**
     * A {@code String} that is the key to access to the property containing 
     * the {@code boolean} defining whether caches stored in {@code CommandData}
     * should be initialized on webapp startup.
     * 
     * @see #INITIALIZE_COMMANDDATA_CACHES_ON_STARTUP_DEFAULT
     * @see #isInitializeCommandDataCachesOnStartup()
     */
    public final static String INITIALIZE_COMMANDDATA_CACHES_ON_STARTUP_KEY =
            "org.bgee.webapp.initializeCommandDataCaches";
    /**
     * The default {@code boolean} value for the property {@link #INITIALIZE_COMMANDDATA_CACHES_ON_STARTUP_KEY}.
     */
    public final static boolean INITIALIZE_COMMANDDATA_CACHES_ON_STARTUP_DEFAULT = false;

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
     * @param prop  A {@code java.util.BgeeProperties} instance that contains the system properties
     *              to use.
     * @return  An instance of {@code BgeeProperties} with values based on the provided
     *          {@code Properties}. The method will create an instance only once for each
     *          thread and always return this instance when called. ("per-thread singleton")
     */
    public static BgeeProperties getBgeeProperties(Properties prop) {
        log.traceEntry("{}", prop);
        BgeeProperties bgeeProp;
        long threadId = Thread.currentThread().getId();
        if (! hasBgeeProperties()) {
            // Create an instance
            bgeeProp = new BgeeProperties(prop);
            // Add it to the map
            bgeeProperties.put(threadId, bgeeProp);
        }
        else {
            bgeeProp = (BgeeProperties) bgeeProperties.get(threadId);
        }
        return log.traceExit(bgeeProp);
    }

    /**
     * {@code boolean} defining whether CSS and JS files were minified. 
     */
    private final boolean minify;
    
    /**
     * @see #getWarningMessage()
     */
    private final String warningMessage;

    /**
     * {@code boolean} defining whether web app is an archive. 
     */
    private final boolean archive;

    /**
     * @see #getBgeeCurrentUrl()
     */
    private final String bgeeCurrentUrl;

    /**
     * {@code String} that defines the directory where query strings holding storable parameters  
     * from previous large queries are stored. 
     */
    private final String requestParametersStorageDirectory;

    /**
     * A {@code String} that defines the root of URLs to Bgee, 
     * for instance, "https://www.bgee.org/".
     */
    private final String bgeeRootDirectory;

    /**
     * A {@code String} that defines the stable root of URLs to Bgee, 
     * for instance, "https://www.bgee.org/bgee14/".
     */
    private final String bgeeStableRootDirectory;

    /**
     * A {@code String} that defines the stable URL of bgee frontend,
     * for instance, "https://www.bgee.org/bgee14_2/".
     */
    private final String stableFrontendUrl;

    /**
     * A {@code String} that defines the URL of bgee frontend,
     * for instance, "https://www.bgee.org/".
     */
    private final String frontendUrl;

    /**
     * A {@code String} that defines the current SPARQL endpoint URL,
     * for instance, "https://www.bgee.org/sparql".
     */
    private final String sparqlCurrentUrl;

    /**
     * A {@code String} that defines the stable SPARQL endpoint URL,
     * for instance, "https://www.bgee.org/sparql_14".
     */
    private final String sparqlStableUrl;

    /**
     * A {@code String} that defines the stable SPARQL endpoint graph,
     * for instance, "https://www.bgee.org/rdf_V14".
     */
    private final String sparqlStableGraph;

    /**
     * A {@code String} that defines the root domain of Bgee servers, 
     * for instance, ".bgee.org".
     */
    private final String bgeeRootDomain;
    
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
     * A {@code String} that defines the root directory where are located logo images, 
     * to be added to the {@code bgeeRootDirectory} to generate URL to obtain images.
     */
    private final String logoImagesRootDirectory;

    /**
     * A {@code String} that defines the root directory where are located species images, 
     * to be added to the {@code bgeeRootDirectory} to generate URL to obtain images.
     */
    private final String speciesImagesRootDirectory;

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
     * A {@code String} that is the name of the path to be used in URL to link to a file stored
     * in the TopAnat result directory
     * (see {@code org.bgee.model.BgeeProperties#topAnatResultsWritingDirectory}).
     * <p>
     * This path has to be used to link to a TopAnat result file. If you want to
     * get the directory to write TopAnat result files, use
     * {@code org.bgee.model.BgeeProperties#topAnatResultsWritingDirectory}
     * <p>
     * If you want to set the working directory for {@code R}, use
     * {@code org.bgee.model.BgeeProperties#topAnatRWorkingDirectory}
     * 
     * @see org.bgee.model.BgeeProperties#topAnatResultsWritingDirectory
     * @see org.bgee.model.BgeeProperties#topAnatRWorkingDirectory
     */
    private final String topAnatResultsUrlDirectory;
    
    /**
     * @see #getMailUri()
     */
    private final String mailUri;
    /**
     * @see #getMailWaitTime()
     */
    private final int mailWaitTime;
    
    /**
     * @see #getTopAnatFromAddress()
     */
    private final String topAnatFromAddress;
    /**
     * @see #getTopAnatFromPersonal()
     */
    private final String topAnatFromPersonal;


    /**
     * @see #getFeedbackFromAddress()
     */
    private final String feedbackFromAddress;
    /**
     * @see #getFeedbackFromPersonal()
     */
    private final String feedbackFromPersonal;
    /**
     * @see #getFeedbackSendTo()
     */
    private final String feedbackSendTo;

    /**
     * @see #getGeneScoreClusteringMethod()
     */
    private final String geneScoreClusteringMethod;
    /**
     * @see #getGeneScoreClusteringThreshold()
     */
    private final Double geneScoreClusteringThreshold;

    /**
     * @see isInitializeCommandDataCachesOnStartup()
     */
    private final boolean initializeCommandDataCachesOnStartup;

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
    public BgeeProperties(Properties prop) {
        // First called the parent constructor, which loads the properties defined in bgee-core
        super(prop);
        log.traceEntry("{}", prop);
        log.debug("Bgee-webapp properties initialization...");
        // load the properties from properties file, System and default values
        // Initialize all properties using the injected prop first, alternatively the System
        // properties and then the file. The default value provided will be use if none of the
        // previous solutions contain the property
        minify = getBooleanOption(prop, SYS_PROPS, FILE_PROPS, MINIFY_KEY, MINIFY_DEFAULT);
        warningMessage = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                WARNING_MESSAGE_KEY, WARNING_MESSAGE_DEFAULT);
        archive = getBooleanOption(prop, SYS_PROPS, FILE_PROPS, ARCHIVE_KEY, ARCHIVE_DEFAULT);
        bgeeCurrentUrl = getStringOption(prop, SYS_PROPS, FILE_PROPS,
                BGEE_CURRENT_URL_KEY, BGEE_CURRENT_URL_DEFAULT);
        requestParametersStorageDirectory = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                REQUEST_PARAMETERS_STORAGE_DIRECTORY_KEY,  
                REQUEST_PARAMETERS_STORAGE_DIRECTORY_DEFAULT);
        bgeeRootDirectory = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                BGEE_ROOT_DIRECTORY_KEY, BGEE_ROOT_DIRECTORY_DEFAULT);
        bgeeStableRootDirectory = getStringOption(prop, SYS_PROPS, FILE_PROPS,
                BGEE_STABLE_ROOT_DIRECTORY_KEY, BGEE_STABLE_ROOT_DIRECTORY_DEFAULT);
        stableFrontendUrl = getStringOption(prop, SYS_PROPS, FILE_PROPS,
                STABLE_FRONTEND_URL_KEY, STABLE_FRONTEND_URL_DEFAULT);
        frontendUrl = getStringOption(prop, SYS_PROPS, FILE_PROPS,
                FRONTEND_URL_KEY, FRONTEND_URL_DEFAULT);
        sparqlCurrentUrl = getStringOption(prop, SYS_PROPS, FILE_PROPS,
                SPARQL_CURRENT_URL_KEY, SPARQL_CURRENT_URL_DEFAULT);
        sparqlStableUrl = getStringOption(prop, SYS_PROPS, FILE_PROPS,
                SPARQL_STABLE_URL_KEY, SPARQL_STABLE_URL_DEFAULT);
        sparqlStableGraph = getStringOption(prop, SYS_PROPS, FILE_PROPS,
                SPARQL_STABLE_GRAPH_KEY, SPARQL_STABLE_GRAPH_DEFAULT);
        bgeeRootDomain = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                BGEE_ROOT_DOMAIN_KEY, BGEE_ROOT_DOMAIN_DEFAULT);
        javascriptFilesRootDirectory = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                JAVASCRIPT_FILES_ROOT_DIRECTORY_KEY, JAVASCRIPT_FILES_ROOT_DIRECTORY_DEFAULT);
        javascriptVersionExtension = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                JAVASCRIPT_VERSION_EXTENSION_KEY, JAVASCRIPT_VERSION_EXTENSION_DEFAULT);
        cssFilesRootDirectory = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                CSS_FILES_ROOT_DIRECTORY_KEY, CSS_FILES_ROOT_DIRECTORY_DEFAULT);
        cssVersionExtension = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                CSS_VERSION_EXTENSION_KEY, CSS_VERSION_EXTENSION_DEFAULT);
        imagesRootDirectory = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                IMAGES_ROOT_DIRECTORY_KEY, IMAGES_ROOT_DIRECTORY_DEFAULT);
        logoImagesRootDirectory = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                LOGO_IMAGES_ROOT_DIRECTORY_KEY, LOGO_IMAGES_ROOT_DIRECTORY_DEFAULT);
        speciesImagesRootDirectory = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                SPECIES_IMAGES_ROOT_DIRECTORY_KEY, SPECIES_IMAGES_ROOT_DIRECTORY_DEFAULT);
        urlMaxLength = getIntegerOption(prop, SYS_PROPS, FILE_PROPS, 
                URL_MAX_LENGTH_KEY, URL_MAX_LENGTH_DEFAULT);
        webpagesCacheConfigFileName = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                WEBPAGES_CACHE_CONFIG_FILE_NAME_KEY, WEBPAGES_CACHE_CONFIG_FILE_NAME_DEFAULT);
        topAnatResultsUrlDirectory = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                TOP_ANAT_RESULTS_URL_DIRECTORY_KEY,
                TOP_ANAT_RESULTS_URL_DIRECTORY_DEFAULT);
        mailUri = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                MAIL_URI_KEY, MAIL_URI_DEFAULT);
        mailWaitTime = getIntegerOption(prop, SYS_PROPS, FILE_PROPS, 
                MAIL_WAIT_TIME_KEY, MAIL_WAIT_TIME_DEFAULT);
        topAnatFromAddress = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                TOPANAT_FROM_ADDRESS_KEY, TOPANAT_FROM_ADDRESS_DEFAULT);
        topAnatFromPersonal = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                TOPANAT_FROM_PERSONAL_KEY, TOPANAT_FROM_PERSONAL_DEFAULT);
        feedbackFromAddress = getStringOption(prop, SYS_PROPS, FILE_PROPS,
                FEEDBACK_FROM_ADDRESS_KEY, FEEDBACK_FROM_ADDRESS_DEFAULT);
        feedbackFromPersonal = getStringOption(prop, SYS_PROPS, FILE_PROPS,
                FEEDBACK_FROM_PERSONAL_KEY, FEEDBACK_FROM_PERSONAL_DEFAULT);
        feedbackSendTo = getStringOption(prop, SYS_PROPS, FILE_PROPS,
                FEEDBACK_SEND_TO_KEY, FEEDBACK_SEND_TO_DEFAULT);
        geneScoreClusteringMethod = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                GENE_SCORE_CLUSTERING_METHOD_KEY, GENE_SCORE_CLUSTERING_METHOD_DEFAULT);
        geneScoreClusteringThreshold = getDoubleOption(prop, SYS_PROPS, FILE_PROPS, 
                GENE_SCORE_CLUSTERING_THRESHOLD_KEY, GENE_SCORE_CLUSTERING_THRESHOLD_DEFAULT);
        initializeCommandDataCachesOnStartup = getBooleanOption(prop, SYS_PROPS, FILE_PROPS,
                INITIALIZE_COMMANDDATA_CACHES_ON_STARTUP_KEY, INITIALIZE_COMMANDDATA_CACHES_ON_STARTUP_DEFAULT);
        log.debug("Initialization done.");
        log.traceExit();
    }

    /**
     * @return  A {@code boolean} defining whether CSS and JS files were minified.
     */
    public boolean isMinify() {
        return minify;
    }
    
    /**
     * @return  A {@code String} that is a warning message to be displayed on all pages.
     *          Useful, e.g., to warn users about a downtime to come.
     * 
     * @see #WARNING_MESSAGE_KEY
     * @see #WARNING_MESSAGE_DEFAULT
     */
    public String getWarningMessage() {
        return warningMessage;
    }

    /**
     * @return  A {@code boolean} defining whether web app is an archive.
     * @see #ARCHIVE_KEY
     * @see #ARCHIVE_DEFAULT
     */
    public boolean isArchive() {
        return archive;
    }

    /**
     * @return  A {@code String} that is the default value of the bgee current url.
     *
     * @see #BGEE_CURRENT_URL_KEY
     * @see #BGEE_CURRENT_URL_DEFAULT
     */
    public String getBgeeCurrentUrl() {
        return bgeeCurrentUrl;
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
     *          "https://www.bgee.org/".
     */
    public String getBgeeRootDirectory() {
        return bgeeRootDirectory;
    }

    /**
     * @return  A {@code String} that defines the stable root of URLs to Bgee, for instance, 
     *          "https://www.bgee.org/bgee14/".
     */
    public String getBgeeStableRootDirectory() {
        return bgeeStableRootDirectory;
    }

    /**
     * @return  A {@code String} that is the default value of stable frontend url.
     *
     * @see #STABLE_FRONTEND_URL_KEY
     * @see #STABLE_FRONTEND_URL_DEFAULT
     */
    public String getStableFrontendUrl() {
        return stableFrontendUrl;
    }

    /**
     * @return  A {@code String} that is the default value of frontend url.
     *
     * @see #FRONTEND_URL_KEY
     * @see #FRONTEND_URL_DEFAULT
     */
    public String getFrontendUrl() {
        return frontendUrl;
    }

    /**
     * @return  A {@code String} that is the default value of the SPARQL current url.
     *
     * @see #SPARQL_CURRENT_URL_KEY
     * @see #SPARQL_CURRENT_URL_DEFAULT
     */
    public String getSparqlCurrentUrl() {
        return sparqlCurrentUrl;
    }

    /**
     * @return  A {@code String} that is the default value of the SPARQL stable url.
     *
     * @see #SPARQL_STABLE_URL_KEY
     * @see #SPARQL_STABLE_URL_DEFAULT
     */
    public String getSparqlStableUrl() {
        return sparqlStableUrl;
    }

    /**
     * @return  A {@code String} that is the default value of the SPARQL stable graph.
     *
     * @see #SPARQL_STABLE_GRAPH_KEY
     * @see #SPARQL_STABLE_GRAPH_DEFAULT
     */
    public String getSparqlStableGraph() {
        return sparqlStableGraph;
    }

    /**
     * @return  A {@code String} that defines the root domain of Bgee servers, 
     *          for instance, ".bgee.org".
     */
    public String getBgeeRootDomain() {
        return bgeeRootDomain;
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
     * @see #JAVASCRIPT_VERSION_EXTENSION_DEFAULT
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
     * @return  A {@code String} that defines the root directory where are located logo images, 
     *          to be added to the {@code bgeeRootDirectory} to generate URL to obtain images.
     */
    public String getLogoImagesRootDirectory() {
        return logoImagesRootDirectory;
    }

    /**
     * @return  A {@code String} that defines the root directory where are located species images, 
     *          to be added to the {@code bgeeRootDirectory} to generate URL to obtain images.
     */
    public String getSpeciesImagesRootDirectory() {
        return speciesImagesRootDirectory;
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
  
    /**
     * @return A {@code String} that is the name of the path to be used in URL to link to a file stored 
     * in the TopAnat result directory
     */
    public String getTopAnatResultsUrlDirectory() {
        return topAnatResultsUrlDirectory;
    }    
    
    /**
     * Gets the URI providing the parameters for sending emails. This URI is used for convenience, 
     * and is not an URI used in a protocol. The URI must be of the form: 
     * {@code <protocol>://<server>:<port>/?<queryString>}. The query string provides 
     * additional parameters besides protocol, server and port, that must be properties 
     * defined by the {@code javax.mail} API, for instance, {@code mail.smtp.auth}, 
     * or {@code mail.smtp.starttls.enable}. If the default port of the protocol is used, 
     * it is not needed to provide it.
     * <p>
     * Additional parameters not part of the {@code javax.mail} API can be provided: 
     * {@code username}, and {@code password}, used to create a {@code PasswordAuthentication} object).
     * <p>
     * Example URI: {@code smtp://smtp.unil.ch:465/?username=user&password=pass
     * &mail.smtp.auth=true&mail.smtp.ssl.enable=true&mail.smtp.starttls.enable=true}.
     * 
     * @return  A {@code String} that is the URI providing the parameters for sending emails.
     * @see #MAIL_URI_KEY
     * @see #MAIL_URI_DEFAULT
     */
    public String getMailUri() {
        return mailUri;
    }
    /**
     * @return  An {@code int} that is the minimum waiting time between sending two mails.
     * @see #MAIL_WAIT_TIME_KEY
     * @see #MAIL_WAIT_TIME_DEFAULT
     */
    public int getMailWaitTime() {
        return mailWaitTime;
    }

    /**
     * @return  A {@code String} that is the mail address which to send mails related to TopAnat from.
     * @see #TOPANAT_FROM_ADDRESS_KEY
     * @see #TOPANAT_FROM_ADDRESS_DEFAULT
     */
    public String getTopAnatFromAddress() {
        return topAnatFromAddress;
    }
    /**
     * @return  A {@code String} that is the mail personal which to send mails related to TopAnat from.
     * @see #TOPANAT_FROM_PERSONAL_KEY
     * @see #TOPANAT_FROM_PERSONAL_DEFAULT
     */
    public String getTopAnatFromPersonal() {
        return topAnatFromPersonal;
    }

    /**
     * @return  A {@code String} that is the mail address which to send mails related to feedback from.
     * @see #FEEDBACK_FROM_ADDRESS_KEY
     * @see #FEEDBACK_FROM_ADDRESS_DEFAULT
     */
    public String getFeedbackFromAddress() {
        return feedbackFromAddress;
    }
    /**
     * @return  A {@code String} that is the mail personal which to send mails related to feedback from.
     * @see #FEEDBACK_FROM_PERSONAL_KEY
     * @see #FEEDBACK_FROM_PERSONAL_DEFAULT
     */
    public String getFeedbackFromPersonal() {
        return feedbackFromPersonal;
    }
    /**
     * @return  A {@code String} that is the mail address to send feedback to.
     * @see #FEEDBACK_SEND_TO_KEY
     * @see #FEEDBACK_SEND_TO_DEFAULT
     */
    public String getFeedbackSendTo() {
        return feedbackSendTo;
    }
    
    /**
     * @return  A {@code String} corresponding to the name of the clustering method to use 
     *          to cluster {@code ExpressionCall}s based on their mean rank score. 
     *          See {@code org.bgee.model.expressiondata.call.Call.ExpressionCall.ClusteringMethod} 
     *          for list of valid method names. 
     * @see #GENE_SCORE_CLUSTERING_METHOD_KEY
     * @see #GENE_SCORE_CLUSTERING_METHOD_DEFAULT
     */
    public String getGeneScoreClusteringMethod() {
        return geneScoreClusteringMethod;
    }
    /**
     * @return  A {@code Double} corresponding to the distance threshold used by methods 
     *          for clustering {@code ExpressionCall}s, based on their mean rank score. 
     *          See {@code org.bgee.model.expressiondata.call.Call.ExpressionCall
     *          .generateMeanRankScoreClustering(Collection, ClusteringMethod, double)}. 
     * @see #GENE_SCORE_CLUSTERING_THRESHOLD_KEY
     * @see #GENE_SCORE_CLUSTERING_THRESHOLD_DEFAULT
     * @see #getGeneScoreClusteringMethod()
     */
    public Double getGeneScoreClusteringThreshold() {
        return geneScoreClusteringThreshold;
    }

    /**
     * @return  A {@code boolean} defining, when {@code true}, that internal caches
     *          used by the controller {@code CommandData} should be initialized
     *          on webapp startup.
     */
    public boolean isInitializeCommandDataCachesOnStartup() {
        return initializeCommandDataCachesOnStartup;
    }
}
