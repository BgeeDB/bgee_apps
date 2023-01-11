package org.bgee.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class loads the properties for Bgee core.
 * The configuration can be a {@code Properties} object injected through 
 * {@link #getBgeeProperties(Properties)}, or loaded from the System properties 
 * or via a file named "bgee.properties" put in the classpath, by using {@link #getBgeeProperties()}.
 * <p>
 * When this class is loaded (so only <b>once</b> for a given {@code ClassLoader}), 
 * it reads the properties from both the System properties and the property file, 
 * so that, for instance, a property can be provided in the file, 
 * and another property via System properties. If a property is defined in both locations, 
 * then the System property overrides the property in the file.
 * Of note, an additional property allows to change the name of the property file 
 * to use (corresponds to the property {@code bgee.properties.file}).
 * <p>
 * Any call, <b>inside a thread</b>, to the method {@link #getBgeeProperties()}, 
 * will always return the same {@code BgeeProperties} instance ("per-thread singleton"). 
 * After calling {@link #releaseAll()}, it is not possible to acquire a {@code BgeeProperties} object 
 * by calling {@link #getBgeeProperties()} anymore (throws an exception).
 * <p>
 * You should always call {@link BgeeProperties#release()} at the end of the execution of a thread, 
 * or {@link #releaseAll()} in multi-threads context (for instance, in a webapp context 
 * when the webapp is shutdown). 
 * <p>
 * This class has been inspired from {@code net.sf.log4jdbc.DriverSpy}
 * developed by Arthur Blake.
 * 
 * @author  Frederic Bastian
 * @author  Mathieu Seppey
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Mar. 2019
 * @since   Bgee 13
 */
public class BgeeProperties {
    private final static Logger log = LogManager.getLogger(BgeeProperties.class.getName());


    /**
     * A {@code String} that is the key to access to the System property that contains
     * the major version number of Bgee (if the release is Bgee {@code v14.2},
     * the major version number is {@code 14}).
     *
     * @see #MAJOR_VERSION_DEFAULT
     */
    public final static String MAJOR_VERSION_KEY = "org.bgee.core.version.major";
    /**
     * A {@code String} that is the default value of the major version number of Bgee.
     *
     * @see #MAJOR_VERSION_KEY
     */
    public final static String MAJOR_VERSION_DEFAULT = null;
    /**
     * A {@code String} that is the key to access to the System property that contains
     * the minor version number of Bgee (if the release is Bgee {@code v14.2},
     * the minor version number is {@code 2}).
     *
     * @see #MINOR_VERSION_DEFAULT
     */
    public final static String MINOR_VERSION_KEY = "org.bgee.core.version.minor";
    /**
     * A {@code String} that is the default value of the minor version number of Bgee.
     *
     * @see #MINOR_VERSION_KEY
     */
    public final static String MINOR_VERSION_DEFAULT = null;

    /**
     * A {@code String} that is the key to access to the System property that contains the name
     * of the file in the classpath that is read at the initialization 
     * of {@code BgeeProperties}. The associated value must be provided related to the root 
     * of the classpath (so, must start with {@code /}).
     * 
     * @see #PROPERTIES_FILE_NAME_DEFAULT
     */
    public final static String PROPERTIES_FILE_NAME_KEY = "org.bgee.core.properties.file";
    /**
     * A {@code String} that is the default value of the name
     * of the file in the classpath that is read at the initialization 
     * of {@code BgeeProperties}.
     * 
     * @see #PROPERTIES_FILE_NAME_KEY
     */
    public final static String PROPERTIES_FILE_NAME_DEFAULT = "/bgee.properties";
    
    // Sphinx search engine
    /**
     * A {@code String} that is the key to access to the System property that contains the value
     * of the server URL which is used to query a search.
     * 
     * @see #BGEE_SEARCH_SERVER_URL_DEFAULT
     */
    public final static String BGEE_SEARCH_SERVER_URL_KEY = "org.bgee.search.url";

    /**
     * A {@code String} that is the default value of the server URL which is used to query a search.
     *
     * @see #BGEE_SEARCH_SERVER_URL_KEY
     */
    public final static String BGEE_SEARCH_SERVER_URL_DEFAULT = null;
    /**
     * A {@code String} that is the key to access to the System property that contains the value
     * of the server port which is used to query a search.
     * 
     * @see #BGEE_SEARCH_SERVER_PORT_DEFAULT
     */
    public final static String BGEE_SEARCH_SERVER_PORT_KEY = "org.bgee.search.port";

    /**
     * A {@code String} that is the default value of the server port which is used to query a search.
     *
     * @see #BGEE_SEARCH_SERVER_PORT_KEY
     */
    public final static String BGEE_SEARCH_SERVER_PORT_DEFAULT = null;
    /**
     * A {@code String} that is the key to access to the System property that contains the value
     * of the sphinx genes index used to query a search.
     * 
     * @see #BGEE_SEARCH_INDEX_GENES_DEFAULT
     */
    public final static String BGEE_SEARCH_INDEX_GENES_KEY = "org.bgee.search.genes";
    /**
     * A {@code String} that is the default value of the genes index used to query a search.
     *
     * @see #BGEE_SEARCH_INDEX_GENES_KEY
     */
    public final static String BGEE_SEARCH_INDEX_GENES_DEFAULT = null;
    /**
     * A {@code String} that is the key to access to the System property that contains the value
     * of the sphinx anat. entities index used to query a search.
     *
     * @see #BGEE_SEARCH_INDEX_ANAT_ENTITIES_DEFAULT
     */
    public final static String BGEE_SEARCH_INDEX_ANAT_ENTITIES_KEY = "org.bgee.search.anat.entities";
    /**
     * A {@code String} that is the default value of the anat. entities index used to query a search.
     *
     * @see #BGEE_SEARCH_INDEX_ANAT_ENTITIES_KEY
     */
    public final static String BGEE_SEARCH_INDEX_ANAT_ENTITIES_DEFAULT = null;
    /**
     * A {@code String} that is the key to access to the System property that contains the value
     * of the sphinx strain index used to query a search.
     *
     * @see #BGEE_SEARCH_INDEX_STRAINS_DEFAULT
     */
    public final static String BGEE_SEARCH_INDEX_STRAINS_KEY = "org.bgee.search.strains";
    /**
     * A {@code String} that is the default value of the strain index used to query a search.
     *
     * @see #BGEE_SEARCH_INDEX_STRAINS_KEY
     */
    public final static String BGEE_SEARCH_INDEX_STRAINS_DEFAULT = null;
    /**
     * A {@code String} that is the key to access to the System property that contains the value
     * of the sphinx autocomplete index used to query a search.
     * 
     * @see #BGEE_SEARCH_INDEX_AUTOCOMPLETE_DEFAULT
     */
    public final static String BGEE_SEARCH_INDEX_AUTOCOMPLETE_KEY = "org.bgee.search.autocomplete";

    /**
     * A {@code String} that is the default value of the genes index used to query a search.
     *
     * @see #BGEE_SEARCH_INDEX_AUTOCOMPLETE_KEY
     */
    public final static String BGEE_SEARCH_INDEX_AUTOCOMPLETE_DEFAULT = null;
    /**
     * A {@code String} that is the key to access to the System property that contains the value
     * of the sphinx experiment index used for searches..
     *
     * @see #BGEE_SEARCH_INDEX_EXPERIMENTS_DEFAULT
     */
    public final static String BGEE_SEARCH_INDEX_EXPERIMENTS_KEY = "org.bgee.search.experiments";
    /**
     * A {@code String} that is the default value of the experiment index used for searches.
     *
     * @see #BGEE_SEARCH_INDEX_EXPERIMENTS_KEY
     */
    public final static String BGEE_SEARCH_INDEX_EXPERIMENTS_DEFAULT = null;
    /**
     * A {@code String} that is the key to access to the System property that contains the value
     * of the sphinx assay index used for searches.
     *
     * @see #BGEE_SEARCH_INDEX_ASSAYS_DEFAULT
     */
    public final static String BGEE_SEARCH_INDEX_ASSAYS_KEY = "org.bgee.search.assays";
    /**
     * A {@code String} that is the default value of the assay index used for searches.
     *
     * @see #BGEE_SEARCH_INDEX_ASSAYS_KEY
     */
    public final static String BGEE_SEARCH_INDEX_ASSAYS_DEFAULT = null;

    //TopAnat
    /**
     * A {@code String} that is the key to access to the System property that contains the value
     * of the path of RScript Executable file which is used to execute the R code.
     * 
     * @see #TOP_ANAT_R_SCRIPT_EXECUTABLE_DEFAULT
     */
    public final static String TOP_ANAT_R_SCRIPT_EXECUTABLE_KEY = "org.bgee.core.topAnatRScriptExecutable";
    /**
     * A {@code String} that is the default value of the path of RScript Executable file 
     * which is used to execute the R code.
     * 
     * @see #TOP_ANAT_R_SCRIPT_EXECUTABLE_KEY
     */
    public final static String TOP_ANAT_R_SCRIPT_EXECUTABLE_DEFAULT = "/usr/bin/Rscript";

    /**
     * A {@code String} that is the key to access to the System property that contains 
     * the Bioconductor release number to use to install {@code R} packages for topAnat.
     *
     * @see #BIOCONDUCTOR_RELEASE_NUMBER_DEFAULT
     */
    public final static String BIOCONDUCTOR_RELEASE_NUMBER_KEY = "org.bgee.core.bioconductorReleaseNumber";
    /**
     * A {@code String} that is the default value of the Bioconductor release number to use to load
     * {@code R} packages for the topAnat analysis.
     *
     * @see #BIOCONDUCTOR_RELEASE_NUMBER_KEY
     */
    public final static String BIOCONDUCTOR_RELEASE_NUMBER_DEFAULT = "3.11";

    /**
     * A {@code String} that is the key to access to the System property that contains 
     * the current working directory of {@code R}, where all the other files required 
     * for the processing of the topAnat analysis are kept.
     * 
     * @see #TOP_ANAT_R_WORKING_DIRECTORY_DEFAULT
     */
    public final static String TOP_ANAT_R_WORKING_DIRECTORY_KEY = "org.bgee.core.topAnatRWorkingDirectory";
    /**
     * A {@code String} that is the default value of the current working directory of {@code R}, 
     * where all the other files required for the processing of the topAnat analysis are kept.
     * 
     * @see #TOP_ANAT_R_WORKING_DIRECTORY_KEY
     */
    public final static String TOP_ANAT_R_WORKING_DIRECTORY_DEFAULT = "topanat/results/";
    
    /**
     * A {@code String} that is the key to access to the System property that contains the name of 
     * the file containing R functions used by topAnat.
     * 
     * @see #TOP_ANAT_FUNCTION_FILE_DEFAULT
     */
    public final static String TOP_ANAT_FUNCTION_FILE_KEY = "org.bgee.core.topAnatFunctionFile";
    /**
     * A {@code String} that is the default value of the name of the file which contains the 
     * additional modified topGO R functions used by topAnat to perform the analyses.
     * 
     * @see #TOP_ANAT_FUNCTION_FILE_KEY
     */
    public final static String TOP_ANAT_FUNCTION_FILE_DEFAULT = "/R_scripts/topAnat_functions.R";   

    /**
     * A {@code String} that is the key to access to the System property that contains the name of 
     * the directory to store outputs of the TopAnat analyses that should be kept to be retrieved 
     * in case the same TopAnat query is performed again.
     * 
     * @see #TOP_ANAT_RESULTS_WRITING_DIRECTORY_DEFAULT
     */
    public final static String TOP_ANAT_RESULTS_WRITING_DIRECTORY_KEY = 
            "org.bgee.core.topAnatResultsWritingDirectory";
    /**
     * A {@code String} that is the default value of the name of the directory to store outputs of
     * the TopAnat analyses that should be kept to be retrieved in case the same TopAnat query
     * is performed again.
     * 
     * @see #TOP_ANAT_RESULTS_WRITING_DIRECTORY_KEY
     */
    public final static String TOP_ANAT_RESULTS_WRITING_DIRECTORY_DEFAULT = "topanat/results/";
   
    //Jobs
    /**
     * A {@code String} that is the key to access to the System property that contains 
     * the maximum number of allowed running jobs per user. If equals to 0, no limit 
     * on the number of simultaneously running jobs is set. 
     * 
     * @see #MAX_JOB_COUNT_PER_USER_DEFAULT
     */
    public final static String MAX_JOB_COUNT_PER_USER_KEY = "org.bgee.core.maxJobCountPerUser";
    /**
     * An {@code int} that is the default value of the maximum number of allowed running jobs per user.
     * 
     * @see #MAX_JOB_COUNT_PER_USER_KEY
     */
    public final static int MAX_JOB_COUNT_PER_USER_DEFAULT = 0;

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
     * initialization of {@code BgeeProperties} to set the single cell RNA-Seq full length 
     * processed expression value download files root directory. 
     * 
     * @see #DOWNLOAD_SC_RNA_SEQ_FL_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_DEFAULT
     * @see #getDownloadRNASeqProcExprValueFilesRootDirectory()
     */
    public final static String DOWNLOAD_SC_RNA_SEQ_FL_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_KEY = 
            "org.bgee.webapp.downloadSingleCellRNASeqFullLengthProcExprValueFilesRootDirectory";
    /**
     * A {@code String} that is the default value of the single cell RNA-Seq full length processed 
     * expression value download files root directory. 
     * 
     * @see #DOWNLOAD_SC_RNA_SEQ_FL_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_KEY
     * @see #getDownloadRNASeqProcExprValueFilesRootDirectory()
     */
    public final static String DOWNLOAD_SC_RNA_SEQ_FL_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_DEFAULT = 
            "processed_expr_values/single_cell_rna_seq_full_length/";
    
    /**
     * A {@code String} that is the key to access to the System property that is read at the 
     * initialization of {@code BgeeProperties} to set the single cell RNA-Seq target based 
     * processed expression value download files root directory. 
     * 
     * @see #DOWNLOAD_SC_RNA_SEQ_TB_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_DEFAULT
     * @see #getDownloadRNASeqProcExprValueFilesRootDirectory()
     */
    public final static String DOWNLOAD_SC_RNA_SEQ_TB_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_KEY = 
            "org.bgee.webapp.downloadSingleCellRNASeqTargetBasedProcExprValueFilesRootDirectory";
    /**
     * A {@code String} that is the default value of the single cell RNA-Seq target based processed 
     * expression value download files root directory. 
     * 
     * @see #DOWNLOAD_SC_RNA_SEQ_TB_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_KEY
     * @see #getDownloadRNASeqProcExprValueFilesRootDirectory()
     */
    public final static String DOWNLOAD_SC_RNA_SEQ_TB_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_DEFAULT = 
            "processed_expr_values/single_cell_rna_seq_target_based/";
    
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
    protected static final ConcurrentMap<Long, BgeeProperties> bgeeProperties = 
            new ConcurrentHashMap<Long, BgeeProperties>(); 
    /**
     * An {@code AtomicBoolean} to define if {@code BgeeProperties}s 
     * can still be acquired (using {@link #getBgeeProperties()}), 
     * or if it is not possible anymore (meaning that the method {@link #releaseAll()} 
     * has been called)
     */
    private static final AtomicBoolean bgeePropertiesClosed = new AtomicBoolean(false);

    /**
     * A {@code java.util.Properties} used to load the values present in the Bgee property file. 
     * Either loaded from the default property file (see {@link #PROPERTIES_FILE_NAME_DEFAULT}), 
     * or from a provided file (see {@link #PROPERTIES_FILE_NAME_KEY})
     */
    protected static final Properties FILE_PROPS = loadFileProps();

    /**
     * A {@code java.util.Properties} set used to load the values set in System properties
     * and where a {@code key} is searched
     */
    protected static final Properties SYS_PROPS = new Properties(System.getProperties());

    /**
     * This method loads and returns the {@code java.util.Properties} present in the property file
     * in the classpath. Either use the default property file name 
     * (see {@link #PROPERTIES_FILE_NAME_DEFAULT}), or a provided file name (see 
     * {@link #PROPERTIES_FILE_NAME_KEY}).
     */
    private static Properties loadFileProps() {
        log.traceEntry();
        Properties filePropsToReturn = null;
        //try to get the properties file.
        //default name is bgee.properties
        //check first if an alternative name has been provided in the System properties
        String propertyFile = (new Properties(System.getProperties()))
                .getProperty(PROPERTIES_FILE_NAME_KEY, PROPERTIES_FILE_NAME_DEFAULT);
        
        log.debug("Trying to use properties file {}", propertyFile);
        InputStream propStream =
                BgeeProperties.class.getResourceAsStream(propertyFile);
        if (propStream != null) {
            try {
                filePropsToReturn = new Properties();
                filePropsToReturn.load(propStream);
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
        return log.traceExit(filePropsToReturn);
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
     * @param sysProps      A {@code java.util.Properties} retrieved from System properties, 
     *                      where {@code key} is searched in second
     * @param fileProps     A {@code java.util.Properties} retrieved from the Bgee properties file, 
     *                      where {@code key} is searched in if {@code prop} and {@code SYS_PROPS}
     *                      were undefined or empty for {@code key}. 
     *                      Can be {@code null} if no properties file was found.
     * @param key           A {@code String} that is the key for which to return the property.
     * @param defaultValue  default value that will be returned if the property 
     *                      is undefined or empty in all {@code Properties}.
     *
     * @return              An {@code Object} corresponding to the value
     *                      for that property key. 
     *                      Or {@code defaultValue} if not defined or empty.
     */
    protected static Object getObjectOption(Properties prop, Properties sysProps, 
            Properties fileProps, String key, String defaultValue) {
        log.traceEntry("{}, {}, {}, {}", prop, sysProps, fileProps, key, defaultValue);
    
        Object propValue = null;
    
        if (prop != null) {
            propValue = prop.get(key);
            if (!isValidValue(propValue)) {
                propValue = prop.getProperty(key);
            }
        }
    
        if (isValidValue(propValue)) {
            log.debug("Retrieved from injected properties {}={}", key, propValue);
        } else {
            propValue = sysProps.get(key);
            if (!isValidValue(propValue)) {
                propValue = sysProps.getProperty(key);
            }
            if(isValidValue(propValue)){
                log.debug("Retrieved from System properties {}={}", key, propValue);
            }
            else {
                if (fileProps != null) {
                    propValue = fileProps.get(key);
                    if (!isValidValue(propValue)) {
                        propValue = fileProps.getProperty(key);
                    }
                }
                if (isValidValue(propValue)) {
                    log.debug("Retrieved from properties file {}={}", key, propValue);
                } else {
                    log.debug("Property {} not defined neither in injected properties nor in properties file nor in System properties, using default value {}", 
                            key, defaultValue);
                    propValue = defaultValue; 
                }
            }
        }
    
        return log.traceExit(propValue);
    }
    /**
     * Determines the value of a property was correctly set.
     * @param propValue An {@code Object} that is a property value to evaluate.
     * @return          {@code true} if {@code propValue} was correctly set, {@code false} otherwise.
     */
    private static boolean isValidValue(Object propValue) {
        log.traceEntry("{}", propValue);
        return log.traceExit(propValue != null && 
                (!(propValue instanceof String) || StringUtils.isNotBlank((String) propValue)));
    }
    
    /**
     * Try to retrieve the property corresponding to {@code key}, 
     * first from the injected {@code Properties} ({@code prop}), then from the System properties 
     * ({@code SYS_PROPS}), then, if undefined or empty, from properties retrieved from the 
     * Bgee property file ({@code FILE_PROPS}). If the property is still undefined or empty 
     * return {@code defaultValue}.
     *
     * @param prop          A {@code java.util.Properties} instance that contains the system 
     *                      properties to look for {@code key} first
     * @param sysProps      A {@code java.util.Properties} retrieved from System properties, 
     *                      where {@code key} is searched in second
     * @param fileProps     A {@code java.util.Properties} retrieved from the Bgee properties file, 
     *                      where {@code key} is searched in if {@code prop} and {@code SYS_PROPS}
     *                      were undefined or empty for {@code key}. 
     *                      Can be {@code null} if no properties file was found.
     * @param defaultValue  default value that will be returned if the property 
     *                      is undefined or empty in all {@code Properties}.
     *
     * @return              A {@code String} corresponding to the value
     *                      for that property key. 
     *                      Or {@code defaultValue} if not defined or empty.
     */
    protected static String getStringOption(Properties prop, Properties sysProps, 
            Properties fileProps, String key, String defaultValue) {
        log.traceEntry("{}, {}, {}, {}, {}", prop, fileProps, sysProps, key, defaultValue);
    
        Object propValue = getObjectOption(prop, sysProps, fileProps, key, null);
        String val = defaultValue;
        if (propValue != null && propValue instanceof String) {
            val= (String) propValue;
        }
    
        return log.traceExit(val);
    }

    /**
     * Try to retrieve the property corresponding to {@code key}, 
     * first from the injected {@code Properties} ({@code prop}), then from the System properties 
     * ({@code SYS_PROPS}), then, if undefined or empty, from properties retrieved from the 
     * Bgee property file ({@code FILE_PROPS}). If the property is still undefined or empty 
     * return {@code defaultValue}.
     *
     * @param prop          A {@code java.util.Properties} instance that contains the system 
     *                      properties to look for {@code key} first
     * @param sysProps      A {@code java.util.Properties} retrieved from System properties, 
     *                      where {@code key} is searched in second
     * @param fileProps     A {@code java.util.Properties} retrieved 
     *                      from the Bgee properties file, 
     *                      where {@code key} is searched in if {@code prop} and {@code SYS_PROPS}
     *                      were undefined or empty for {@code key}. 
     *                      Can be {@code null} if no properties file was found.
     * @param key           A {@code String} that is the key for which to return the property.
     * @param defaultValue  default value that will be returned if the property 
     *                      is undefined or empty in all {@code Properties}.
     *
     * @return             An {@code int} corresponding to the value
     *                     for that property key.
     *                     Or {@code defaultValue} if not defined or empty.
     */
    protected static Integer getIntegerOption(Properties prop, Properties sysProps, 
            Properties fileProps, String key, Integer defaultValue) {
        log.traceEntry("{}, {}, {}, {}, {}", prop, fileProps, sysProps, key, defaultValue);
    
        Object propValue = getObjectOption(prop, sysProps, fileProps, key, null);
        Integer val = defaultValue;
        if (propValue != null) {
            if (propValue instanceof String) {
                val= Integer.valueOf((String) propValue);
            } else if (propValue instanceof Integer) {
                val = (Integer) propValue;
            }
        }
    
        return log.traceExit(val);
    }
    /**
     * Try to retrieve the property corresponding to {@code key}, 
     * first from the injected {@code Properties} ({@code prop}), then from the System properties 
     * ({@code SYS_PROPS}), then, if undefined or empty, from properties retrieved from the 
     * Bgee property file ({@code FILE_PROPS}). If the property is still undefined or empty 
     * return {@code defaultValue}.
     *
     * @param prop          A {@code java.util.Properties} instance that contains the system 
     *                      properties to look for {@code key} first
     * @param sysProps      A {@code java.util.Properties} retrieved from System properties, 
     *                      where {@code key} is searched in second
     * @param fileProps     A {@code java.util.Properties} retrieved 
     *                      from the Bgee properties file, 
     *                      where {@code key} is searched in if {@code prop} and {@code SYS_PROPS}
     *                      were undefined or empty for {@code key}. 
     *                      Can be {@code null} if no properties file was found.
     * @param key           A {@code String} that is the key for which to return the property.
     * @param defaultValue  default value that will be returned if the property 
     *                      is undefined or empty in all {@code Properties}.
     *
     * @return             A {@code double} corresponding to the value
     *                     for that property key.
     *                     Or {@code defaultValue} if not defined or empty.
     */
    protected static Double getDoubleOption(Properties prop, Properties sysProps, 
            Properties fileProps, String key, Double defaultValue) {
        log.traceEntry("{}, {}, {}, {}, {}", prop, fileProps, sysProps, key, defaultValue);
    
        Object propValue = getObjectOption(prop, sysProps, fileProps, key, null);
        Double val = defaultValue;
        if (propValue != null) {
            if (propValue instanceof String) {
                val= Double.parseDouble((String) propValue);
            } else if (propValue instanceof Double) {
                val = (Double) propValue;
            }
        }
    
        return log.traceExit(val);
    }
    /**
     * Try to retrieve the property corresponding to {@code key}, 
     * first from the injected {@code Properties} ({@code prop}), then from the System properties 
     * ({@code SYS_PROPS}), then, if undefined or empty, from properties retrieved from the 
     * Bgee property file ({@code FILE_PROPS}). If the property is still undefined or empty 
     * return {@code defaultValue}.
     *
     * @param prop          A {@code java.util.Properties} instance that contains the system 
     *                      properties to look for {@code key} first
     * @param sysProps      A {@code java.util.Properties} retrieved from System properties, 
     *                      where {@code key} is searched in second
     * @param fileProps     A {@code java.util.Properties} retrieved 
     *                      from the Bgee properties file, 
     *                      where {@code key} is searched in if {@code prop} and {@code SYS_PROPS}
     *                      were undefined or empty for {@code key}. 
     *                      Can be {@code null} if no properties file was found.
     * @param key           A {@code String} that is the key for which to return the property.
     * @param defaultValue  default value that will be returned if the property 
     *                      is undefined or empty in all {@code Properties}.
     *
     * @return             A {@code boolean} corresponding to the value
     *                     for that property key.
     *                     Or {@code defaultValue} if not defined or empty.
     */
    protected static Boolean getBooleanOption(Properties prop, Properties sysProps, 
            Properties fileProps, String key, Boolean defaultValue) {
        log.traceEntry("{}, {}, {}, {}, {}", prop, sysProps, fileProps, key, defaultValue);
        
        Object propValue = getObjectOption(prop, sysProps, fileProps, key, null);
        Boolean val = defaultValue;
        if (propValue != null) {
            if (propValue instanceof String) {
                String trimLowCase = ((String) propValue).trim().toLowerCase();
                val= "true".equals(trimLowCase) ||
                     "yes".equals(trimLowCase) || 
                     "on".equals(trimLowCase) || 
                     "1".equals(trimLowCase);
            } else if (propValue instanceof Boolean) {
                val = (Boolean) propValue;
            }
        }
        
        return log.traceExit(val);
    }

    /**
     * Gets the {@code BgeeProperties} object associated to the current thread. This method 
     * creates a new instance only once for each thread, and always returns this instance 
     * when called ("per-thread singleton").
     * <p>
     * To set the returned {@code BgeeProperties}, properties are read from, in order of preeminence:
     * <ul>
     * <li>The System properties, read only once at loading of this class. Modifying them afterwards 
     * has no effect. 
     * <li>The property file defined in System properties (see {@link #PROPERTIES_FILE_NAME_KEY}), 
     * or from the default property file (see {@link #PROPERTIES_FILE_NAME_DEFAULT}).
     * It is read only once at loading of this class.
     * <li>The default values defined in this class. 
     * </ul> 
     * <p>
     * These properties are read only once at class loading. Modifying the system properties 
     * after class loading will have no effect on the {@code BgeeProperties} objects 
     * returned by this method. If the method {@link #getBgeeProperties(Properties)} 
     * was first call in a given thread, then the provided properties will be used 
     * for all following calls, including calls to this method (it means that this method 
     * might thus not use the System properties or the file properties).
     * <p>
     * Note that after having called {@link #releaseAll()}, no {@code BgeeProperties} 
     * can be obtained anymore. This method will throw an {@code IllegalStateException} 
     * if {@code releaseAll()} has been previously called.
     * 
     * @return  A {@code BgeeProperties} object with values already set. 
     *          The method will create an instance only once for each thread 
     *          and always return this instance when called ("per-thread singleton").
     * @see #getBgeeProperties(Properties)
     * @throws IllegalStateException If no {@code BgeeProperties} could be obtained anymore. 
     */
    public static BgeeProperties getBgeeProperties() throws IllegalStateException {
        return getBgeeProperties(null);
    }

    /**
     * Gets a {@code BgeeProperties} object with properties also read from {@code prop}. 
     * To set the returned {@code BgeeProperties}, properties are read from, 
     * in order of preeminence:
     * <ul>
     * <li>The provided properties, {@code prop}.
     * <li>The System properties, read only once at loading of this class. Modifying them afterwards 
     * has no effect. 
     * <li>The property file defined in {@code prop} or in System properties 
     * (see {@link #PROPERTIES_FILE_NAME_KEY}), or from the default property file 
     * (see {@link #PROPERTIES_FILE_NAME_DEFAULT}). It is read only when instantiating 
     * a new {@code BgeeProperties} object, so, only at first call 
     * to a {@code getBgeeProperties} method in a given thread.
     * <li>The default values defined in this class. 
     * </ul> 
     * <p>
     * Note that this method creates a new instance only once for each thread, 
     * and always returns this instance when called ("per-thread singleton").  
     * If this method or the method {@link #getBgeeProperties()} were already called 
     * from this thread, calling this method again with different properties will have no effect. 
     * The provided properties will be read only at first instantiation of 
     * a {@code BgeeProperties} object in a given thread. 
     * <p>
     * Note that after having called {@link #releaseAll()}, no {@code BgeeProperties} 
     * can be obtained anymore. This method will throw an {@code IllegalStateException} 
     * if {@code releaseAll()} has been previously called.
     * 
     * @param prop  A {@code java.util.Properties} instance that contains the system properties
     *              to use.
     * @return  An instance of {@code BgeeProperties} with values based on the provided
     *          {@code Properties}. The method will create an instance only once for each thread 
     *          and always return this instance when called ("per-thread singleton").
     * @see #getBgeeProperties()
     * @throws IllegalStateException If no {@code BgeeProperties} could be obtained anymore. 
     */
    public static BgeeProperties getBgeeProperties(Properties prop) throws IllegalStateException {
        log.traceEntry("{}", prop);
        BgeeProperties bgeeProp;
        long threadId = Thread.currentThread().getId();
        log.trace("Trying to obtain a BgeeProperties instance for Thread {}", threadId);
        
        if (bgeePropertiesClosed.get()) {
            throw new IllegalStateException("releaseAll() has been already called, " +
                    "it is not possible to acquire a BgeeProperties anymore");
        }
        
        if (! hasBgeeProperties()) {
            // Create an instance
            bgeeProp = new BgeeProperties(prop);
            // Add it to the map
            bgeeProperties.put(threadId, bgeeProp);
        }
        else {
            bgeeProp = bgeeProperties.get(threadId);
        }
        return log.traceExit(bgeeProp);
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
        log.traceEntry();
        return log.traceExit(bgeeProperties.containsKey(Thread.currentThread().getId()));
    }

    /**
     * Remove the instance of {@code BgeeProperties} associated with the current thread
     * from the {@code ConcurrentMap} used to store {@code BgeeProperties}
     */
    public static void removeFromBgeePropertiesPool(){
        bgeeProperties.remove(Thread.currentThread().getId());
    }

    /**
     * Release all {@code BgeeProperties}s currently registered, and prevent any new 
     * {@code BgeeProperties} to be obtained again by calling a {@code getBgeeProperties} method  
     * (would throw a {@code IllegalStateException}). 
     * <p>
     * This method returns the number of {@code BgeeProperties}s that were released. 
     * <p>
     * This method is called for instance when a {@code ShutdownListener} 
     * want to release all {@code BgeeProperties}s.
     * 
     * @return  An {@code int} that is the number of {@code BgeeProperties}s that were released
     */
    public static int releaseAll() {
        log.traceEntry();

        //this AtomicBoolean will act more or less like a lock 
        //(no new BgeeProperties can be obtained after this AtomicBoolean is set to true).
        //It's not totally true, but we don't expect any major error if it doesn't act like a lock.
        bgeePropertiesClosed.set(true);

        int propCount = bgeeProperties.size();
        bgeeProperties.clear();

        return log.traceExit(propCount);
    }
    
    //******************************
    // INSTANCE METHODS
    //******************************

    /**
     * Protected constructor, can be only called through the use of one of the
     * {@code getBgeeProperties} method, the only way for the user to obtain an instance of this
     * class, unless it is called within a subclass constructor.
     * Try to load the properties from the injected {@code Properties}, or a properties file, 
     * or from the system properties. 
     * Otherwise, set the default values.
     * 
     * @param prop  A {@code java.util.Properties} instance that contains the system properties
     *              to use.
     */
    protected BgeeProperties(Properties prop) {
        log.traceEntry("{}", prop);
        log.debug("Bgee-core properties initialization...");
        // Initialize all properties using the injected prop first, alternatively the System
        // properties and then the file. The default value provided will be use if none of the
        // previous solutions contain the property
        majorVersion = getStringOption(prop, SYS_PROPS, FILE_PROPS,
                MAJOR_VERSION_KEY,
                MAJOR_VERSION_DEFAULT);
        minorVersion = getStringOption(prop, SYS_PROPS, FILE_PROPS,
                MINOR_VERSION_KEY,
                MINOR_VERSION_DEFAULT);
        searchServerUrl = getStringOption(prop, SYS_PROPS, FILE_PROPS,
                BGEE_SEARCH_SERVER_URL_KEY,
                BGEE_SEARCH_SERVER_URL_DEFAULT);
        searchUrlPort = getStringOption(prop, SYS_PROPS, FILE_PROPS,
                BGEE_SEARCH_SERVER_PORT_KEY,
                BGEE_SEARCH_SERVER_PORT_DEFAULT);
        searchGenesIndex = getStringOption(prop, SYS_PROPS, FILE_PROPS,
                BGEE_SEARCH_INDEX_GENES_KEY,
                BGEE_SEARCH_INDEX_GENES_DEFAULT);
        searchAnatEntitiesIndex = getStringOption(prop, SYS_PROPS, FILE_PROPS,
                BGEE_SEARCH_INDEX_ANAT_ENTITIES_KEY,
                BGEE_SEARCH_INDEX_ANAT_ENTITIES_DEFAULT);
        searchStrainsIndex = getStringOption(prop, SYS_PROPS, FILE_PROPS,
                BGEE_SEARCH_INDEX_STRAINS_KEY,
                BGEE_SEARCH_INDEX_STRAINS_DEFAULT);
        searchAutocompleteIndex = getStringOption(prop, SYS_PROPS, FILE_PROPS,
                BGEE_SEARCH_INDEX_AUTOCOMPLETE_KEY,
                BGEE_SEARCH_INDEX_AUTOCOMPLETE_DEFAULT);
        searchExperimentsIndex = getStringOption(prop, SYS_PROPS, FILE_PROPS,
                BGEE_SEARCH_INDEX_EXPERIMENTS_KEY,
                BGEE_SEARCH_INDEX_EXPERIMENTS_DEFAULT);
        searchAssaysIndex = getStringOption(prop, SYS_PROPS, FILE_PROPS,
                BGEE_SEARCH_INDEX_ASSAYS_KEY,
                BGEE_SEARCH_INDEX_ASSAYS_DEFAULT);
        topAnatRScriptExecutable = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                TOP_ANAT_R_SCRIPT_EXECUTABLE_KEY,  
                TOP_ANAT_R_SCRIPT_EXECUTABLE_DEFAULT);
        topAnatRWorkingDirectory = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                TOP_ANAT_R_WORKING_DIRECTORY_KEY,
                TOP_ANAT_R_WORKING_DIRECTORY_DEFAULT);
        bioconductorReleaseNumber = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                BIOCONDUCTOR_RELEASE_NUMBER_KEY,
                BIOCONDUCTOR_RELEASE_NUMBER_DEFAULT);
        topAnatFunctionFile = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                TOP_ANAT_FUNCTION_FILE_KEY,
                TOP_ANAT_FUNCTION_FILE_DEFAULT);
        topAnatResultsWritingDirectory = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                TOP_ANAT_RESULTS_WRITING_DIRECTORY_KEY,
                TOP_ANAT_RESULTS_WRITING_DIRECTORY_DEFAULT);
        maxJobCountPerUser = getIntegerOption(prop, SYS_PROPS, FILE_PROPS, 
                MAX_JOB_COUNT_PER_USER_KEY,
                MAX_JOB_COUNT_PER_USER_DEFAULT);
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
        downloadSingleCellRNASeqFullLengthProcExprValueFilesRootDirectory = getStringOption(prop, 
                SYS_PROPS, FILE_PROPS, DOWNLOAD_SC_RNA_SEQ_FL_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_KEY, 
                DOWNLOAD_SC_RNA_SEQ_FL_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_DEFAULT);
        downloadSingleCellRNASeqTargetBasedProcExprValueFilesRootDirectory = getStringOption(prop, 
                SYS_PROPS, FILE_PROPS, DOWNLOAD_SC_RNA_SEQ_TB_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_KEY, 
                DOWNLOAD_SC_RNA_SEQ_TB_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_DEFAULT);
        log.debug("Initialization done.");
        log.traceExit();
    }


    /**
     * A {@code String} that is the major version number of Bgee
     * (if the release is Bgee {@code v14.2}, the major version number is {@code 14}).
     */
    private final String majorVersion;
    /**
     * A {@code String} that is the minor version number of Bgee
     * (if the release is Bgee {@code v14.2}, the minor version number is {@code 2}).
     */
    private final String minorVersion;

    /**
     * A {@code String} that is the server URL which is used to query a search.
     */
    private final String searchServerUrl;

    /**
     * A {@code String} that is the server port which is used to query a search.
     */
    private final String searchUrlPort;

    /**
     * A {@code String} that is the name of the genes index which is used to query a search.
     */
    private final String searchGenesIndex;
    
    /**
     * A {@code String} that is the name of the anat. entities index which is used to query a search.
     */
    private final String searchAnatEntitiesIndex;

    /**
     * A {@code String} that is the name of the strain index which is used to query a search.
     */
    private final String searchStrainsIndex;

    /**
     * A {@code String} that is the name of the autocomplete index which is used to query a search.
     */
    private final String searchAutocompleteIndex;

    /**
     * A {@code String} that is the name of the experiment index which is used for searches.
     */
    private final String searchExperimentsIndex;
    /**
     * A {@code String} that is the name of the assay index which is used for searches.
     */
    private final String searchAssaysIndex;

    /**
     * A {@code String} that is the Bioconductor Release number used to download
     * {@code R} packages.
     */
    private final String bioconductorReleaseNumber;

    /**
     * A {@code String} that is the path of RScript Executable file 
     * which is used to execute the R code.
     */
    private final String topAnatRScriptExecutable;

    /**
     * A {@code String} that is the current working directory of {@code R}, 
     * where all the other files required for the processing of the topAnat analysis
     * are kept.
     * <p>
     * This directory should only be used with the library for calling R, to set the
     * working directory of the {@code R}. If you need to use the directory
     * to access a result file or get the directory to write TopAnat result
     * files, use {@code #topAnatResultsWritingDirectory}
     * <p>
     * If you want to link to such a file using a URL, you must use
     * {@code #topAnatResultsUrlDirectory}.
     * 
     * @see #topAnatResultsWritingDirectory
     * @see #topAnatResultsUrlDirectory
     */
    private final String topAnatRWorkingDirectory;

    /**
     * A {@code String} that is the name of the file which contains the additional modified
     * topGO R functions used by topAnat to perform the analyses.
     */
    private final String topAnatFunctionFile;

    /**
     * A {@code String} that is the name of the directory to store outputs of the TopAnat analyses
     * that should be kept to be retrieved in case the same TopAnat query is performed again.
     * <p>
     * This directory has to be used when writing files. If you want to link to
     * such a file using a URL, you must use
     * {@code #topAnatResultsUrlDirectory}.
     * <p>
     * If you want to set the working directory for {@code R}, use
     * {@code #topAnatRWorkingDirectory}
     * 
     * @see #topAnatResultsUrlDirectory
     * @see #topAnatCallerWorkingDirectory
     */ 
    private final String topAnatResultsWritingDirectory;
    
    /**
     * @see #getMaxJobCountPerUser()
     */
    private final int maxJobCountPerUser; 
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
     * A {@code String} that defines the RNA-Seq processed expression value download file
     * directory where  are located processed expression value files available for download.
     */
    private final String downloadSingleCellRNASeqFullLengthProcExprValueFilesRootDirectory;
    /**
     * A {@code String} that defines the RNA-Seq processed expression value download file
     * directory where  are located processed expression value files available for download.
     */
    private final String downloadSingleCellRNASeqTargetBasedProcExprValueFilesRootDirectory;
    
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

    //******************
    // RELEASE METHODS
    //******************
    /**
     * Releases this {@code BgeeProperties}. 
     * A call to {@link #getBgeeProperties()} from the thread that was holding it 
     * will return a new {@code BgeeProperties} instance. 
     * 
     * @return  {@code true} if this {@code BgeeProperties} was released, 
     *          {@code false} if it was already released.
     */
    public boolean release() {
        log.traceEntry();
        return log.traceExit(bgeeProperties.values().remove(this));
    }
    /**
     * Determines whether this {@code BgeeProperties} was released 
     * (following a call to {@link #release()}).
     * 
     * @return  {@code true} if this {@code BgeeProperties} was released, 
     *          {@code false} otherwise.
     */
    public boolean isReleased() {
        log.traceEntry();
        return log.traceExit(!bgeeProperties.containsValue(this));
    }

    //**************************
    // PROPERTY GETTERS
    //**************************
    /**
     * @return  A {@code String} that is the major version number of Bgee
     *          (if the release is Bgee {@code v14.2}, the major version number is {@code 14}).
     */
    public String getMajorVersion() {
        return majorVersion;
    }
    /**
     * @return  A {@code String} that is the minor version number of Bgee
     *          (if the release is Bgee {@code v14.2}, the minor version number is {@code 2}).
     */
    public String getMinorVersion() {
        return minorVersion;
    }

    // Sphinx search engine
    /**
     * @return A {@code String} that is the server URL which is used to query a search.
     */
    public String getSearchServerURL() {
        return searchServerUrl;
    }
    /**
     * @return A {@code String} that is the server port which is used to query a search.
     */
    public String getSearchServerPort() {
        return searchUrlPort;
    }
    /**
     * @return A {@code String} that is the name of the genes index which is used to
     * query a search.
     */
    public String getSearchGenesIndex() {
        return searchGenesIndex;
    }
    /**
     * @return A {@code String} that is the name of the anat. entities index which is used to 
     * query a search.
     */
    public String getSearchAnatEntitiesIndex() {
        return searchAnatEntitiesIndex;
    }
    /**
     * @return A {@code String} that is the name of the strain index which is used to 
     * query a search.
     */
    public String getSearchStrainsIndex() {
        return searchStrainsIndex;
    }
    /**
     * @return A {@code String} that is the name of the autocomplete index which is used 
     * to query a search.
     */
    public String getSearchAutocompleteIndex() {
        return searchAutocompleteIndex;
    }
    /**
     * @return A {@code String} that is the name of the experiment index which is used for searches.
     */
    public String getSearchExperimentsIndex() {
        return searchExperimentsIndex;
    }
    /**
     * @return A {@code String} that is the name of the assay index which is used for searches.
     */
    public String getSearchAssaysIndex() {
        return searchAssaysIndex;
    }

    //TopAnat
    /**
     * @return A {@code String} that is the Boconductor release number from which {@code R} 
     * packages will be downloaded
     */
    public String getBioconductorReleaseNumber() {
        return bioconductorReleaseNumber;
    }
    /**
     * @return A {@code String} that is the path of RScript Executable file which is used 
     * to execute the {@code R} code.
     */
    public String getTopAnatRScriptExecutable() {
        return topAnatRScriptExecutable;
    }
    /**
     * @return A {@code String} that is the current working directory of {@code R}, where all
     * the other files required for the processing of the topAnat analysis are kept.
     */
    public String getTopAnatRWorkingDirectory() {
        return topAnatRWorkingDirectory;
    }
    /**
     * @return A {@code String} that is the name of the file which contains the additional modified 
     * topGO {@code R} functions used by topAnat to perform the analyses.
     */
    public String getTopAnatFunctionFile() {
        return topAnatFunctionFile;
    }
    /**
     * @return A {@code String} that is the name of the directory to store outputs of the TopAnat 
     * analyses that should be kept to be retrieved in case the same TopAnat query is performed again.
     */
    public String getTopAnatResultsWritingDirectory() {
        return topAnatResultsWritingDirectory;
    }

    //Jobs
    /**
     * @return  An {@code int} that is the maximum number of allowed running jobs per user.
     *          If equals to 0, no limit on the number of simultaneously running jobs is set. 
     */
    public int getMaxJobCountPerUser() {
        return maxJobCountPerUser;
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
     *          single cell RNA-Seq full length processed expression value files available 
     *          for download, to generate URL to download files.
     * @see #DOWNLOAD_SC_RNA_SEQ_FL_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_KEY
     * @see #DOWNLOAD_SC_RNA_SEQ_FL_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_DEFAULT
     */
    public String getDownloadSingleCellRNASeqFullLengthProcExprValueFilesRootDirectory() {
        return downloadSingleCellRNASeqFullLengthProcExprValueFilesRootDirectory;
    }
    /**
     * @return  A {@code String} that defines the absolute root directory where are located 
     *          single cell RNA-Seq target based processed expression value files available 
     *          for download, to generate URL to download files.
     * @see #DOWNLOAD_SC_RNA_SEQ_TB_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_KEY
     * @see #DOWNLOAD_SC_RNA_SEQ_TB_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_DEFAULT
     */
    public String getDownloadSingleCellRNASeqTargetBasedProcExprValueFilesRootDirectory() {
        return downloadSingleCellRNASeqTargetBasedProcExprValueFilesRootDirectory;
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BgeeProperties [topAnatRScriptExecutable=").append(topAnatRScriptExecutable)
                .append(", bioconductorReleaseNumber=").append(bioconductorReleaseNumber)
                .append(", topAnatRWorkingDirectory=").append(topAnatRWorkingDirectory)
                .append(", topAnatFunctionFile=").append(topAnatFunctionFile)
                .append(", topAnatResultsWritingDirectory=").append(topAnatResultsWritingDirectory)
                .append("]");
        return builder.toString();
    }
}
