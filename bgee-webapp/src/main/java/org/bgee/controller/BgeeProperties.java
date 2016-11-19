package org.bgee.controller;

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.Call.ExpressionCall;

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
 * @author Frederic Bastian
 * @author Mathieu Seppey
 * @author Valentine Rech de Laval
 * @version Bgee 13, August 2015
 * @since Bgee 13
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
     * initialization of {@code BgeeProperties} to set the FTP server root directory. 
     * 
     * @see #FTP_ROOT_DIRECTORY_DEFAULT
     * @see #getFTPRootDirectory()
     */
    public final static String FTP_ROOT_DIRECTORY_KEY = "org.bgee.webapp.ftpRootDirectory";
    /**
     * A {@code String} that is the default value of the FTP server root directory. 
     * 
     * @see #FTP_ROOT_DIRECTORY_KEY
     * @see #getFTPRootDirectory()
     */
    public final static String FTP_ROOT_DIRECTORY_DEFAULT = "ftp/";

    /**
     * A {@code String} that is the key to access to the System property that is read at the 
     * initialization of {@code BgeeProperties} to set the download root directory. 
     * 
     * @see #DOWNLOAD_ROOT_DIRECTORY_DEFAULT
     * @see #getDownloadRootDirectory()
     */
    public final static String DOWNLOAD_ROOT_DIRECTORY_KEY = "org.bgee.webapp.downloadRootDirectory";
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
            bgeeProp = (BgeeProperties) bgeeProperties.get(threadId);
        }
        return log.exit(bgeeProp);
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
     * {@code String} that defines the directory where query strings holding storable parameters  
     * from previous large queries are stored. 
     */
    private final String requestParametersStorageDirectory;

    /**
     * A {@code String} that defines the root of URLs to Bgee, 
     * for instance, "http://bgee.org/".
     */
    private final String bgeeRootDirectory;
    /**
     * A {@code String} that defines the root domain of Bgee servers, 
     * for instance, ".bgee.org".
     */
    private final String bgeeRootDomain;

    /**
     * A {@code String} that defines the root directory where is the FTP server, 
     * to be added to the {@code bgeeRootDirectory} to generate URL of the FTP server.
     */
    private final String ftpRootDirectory;

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
     * @see #getGeneScoreClusteringMethod()
     */
    private final String geneScoreClusteringMethod;
    /**
     * @see #getGeneScoreClusteringThreshold()
     */
    private final Double geneScoreClusteringThreshold;

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
        log.entry(prop);
        log.debug("Bgee-webapp properties initialization...");
        // load the properties from properties file, System and default values
        // Initialize all properties using the injected prop first, alternatively the System
        // properties and then the file. The default value provided will be use if none of the
        // previous solutions contain the property
        minify = getBooleanOption(prop, SYS_PROPS, FILE_PROPS, MINIFY_KEY, MINIFY_DEFAULT);
        warningMessage = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                WARNING_MESSAGE_KEY, WARNING_MESSAGE_DEFAULT);
        requestParametersStorageDirectory = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                REQUEST_PARAMETERS_STORAGE_DIRECTORY_KEY,  
                REQUEST_PARAMETERS_STORAGE_DIRECTORY_DEFAULT);
        bgeeRootDirectory = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                BGEE_ROOT_DIRECTORY_KEY, BGEE_ROOT_DIRECTORY_DEFAULT);
        bgeeRootDomain = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                BGEE_ROOT_DOMAIN_KEY, BGEE_ROOT_DOMAIN_DEFAULT);
        ftpRootDirectory = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                FTP_ROOT_DIRECTORY_KEY, FTP_ROOT_DIRECTORY_DEFAULT);
        downloadRootDirectory = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                DOWNLOAD_ROOT_DIRECTORY_KEY, DOWNLOAD_ROOT_DIRECTORY_DEFAULT);
        downloadExprFilesRootDirectory = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                DOWNLOAD_EXPR_FILES_ROOT_DIRECTORY_KEY, DOWNLOAD_EXPR_FILES_ROOT_DIRECTORY_DEFAULT);
        downloadDiffExprFilesRootDirectory = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                DOWNLOAD_DIFF_EXPR_FILES_ROOT_DIRECTORY_KEY, 
                DOWNLOAD_DIFF_EXPR_FILES_ROOT_DIRECTORY_DEFAULT);
        downloadMultiDiffExprFilesRootDirectory = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                DOWNLOAD_MULTI_DIFF_EXPR_FILES_ROOT_DIRECTORY_KEY, 
                DOWNLOAD_MULTI_DIFF_EXPR_FILES_ROOT_DIRECTORY_DEFAULT);
        downloadOrthologFilesRootDirectory = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                DOWNLOAD_ORTHOLOG_FILES_ROOT_DIRECTORY_KEY, 
                DOWNLOAD_ORTHOLOG_FILES_ROOT_DIRECTORY_DEFAULT);
        downloadAffyProcExprValueFilesRootDirectory = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                DOWNLOAD_AFFY_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_KEY, 
                DOWNLOAD_AFFY_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_DEFAULT);
        downloadRNASeqProcExprValueFilesRootDirectory = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                DOWNLOAD_RNA_SEQ_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_KEY, 
                DOWNLOAD_RNA_SEQ_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_DEFAULT);
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
        geneScoreClusteringMethod = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                GENE_SCORE_CLUSTERING_METHOD_KEY, GENE_SCORE_CLUSTERING_METHOD_DEFAULT);
        geneScoreClusteringThreshold = getDoubleOption(prop, SYS_PROPS, FILE_PROPS, 
                GENE_SCORE_CLUSTERING_THRESHOLD_KEY, GENE_SCORE_CLUSTERING_THRESHOLD_DEFAULT);
        log.debug("Initialization done.");
        log.exit();
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
     * @return  A {@code String} that defines the directory where query strings holding storable 
     *          parameters from previous large queries are stored. 
     */
    public String getRequestParametersStorageDirectory() {
        return requestParametersStorageDirectory;
    }

    /**
     * @return  A {@code String} that defines the root of URLs to Bgee, for instance, 
     *          "http://bgee.org/".
     */
    public String getBgeeRootDirectory() {
        return bgeeRootDirectory;
    }
    /**
     * @return  A {@code String} that defines the root domain of Bgee servers, 
     *          for instance, ".bgee.org".
     */
    public String getBgeeRootDomain() {
        return bgeeRootDomain;
    }

    /**
     * @return  A {@code String} that defines the FTP root directory, to be added to the 
     *          {@code bgeeRootDirectory} to generate URL of FTP server.
     */
    public String getFTPRootDirectory() {
        return ftpRootDirectory;
    }

    /**
     * @return  A {@code String} that defines the download files root directory where are located 
     *          data files available for download, to be added to the {@code bgeeRootDirectory} to 
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
     * @return  A {@code String} corresponding to the name of the clustering method to use 
     *          to cluster {@code ExpressionCall}s based on their mean rank score. 
     *          See {@code org.bgee.model.expressiondata.Call.ExpressionCall.ClusteringMethod} 
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
     *          See {@code org.bgee.model.expressiondata.Call.ExpressionCall
     *          .generateMeanRankScoreClustering(Collection, ClusteringMethod, double)}. 
     * @see #GENE_SCORE_CLUSTERING_THRESHOLD_KEY
     * @see #GENE_SCORE_CLUSTERING_THRESHOLD_DEFAULT
     * @see #getGeneScoreClusteringMethod()
     */
    public Double getGeneScoreClusteringThreshold() {
        return geneScoreClusteringThreshold;
    }
}
