package org.bgee.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class loads the properties for Bgee core.
 * The configuration can be a {@code Properties} object injected through 
 * {@link #getBgeeProperties(Properties)} 
 * or loaded from the System properties or via a file named {@code bgee.properties}
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
 * @author Valentine Rech de Laval
 * @version Bgee 13, August 2015
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
            "org.bgee.core.properties.file";
    /**
     * A {@code String} that is the default value of the name
     * of the file in the classpath that is read at the initialization 
     * of {@code BgeeProperties}.
     * 
     * @see #PROPERTIES_FILE_NAME_KEY
     */
    public final static String PROPERTIES_FILE_NAME_DEFAULT = "/bgee.properties";
    
    /**
     * A {@code String} that is the key to access to the System property that contains the value
     * of the path of RScript Executable file which is used by {@code RCaller} to execute the R code.
     * 
     * @see #TOP_ANAT_R_SCRIPT_EXECUTABLE_DEFAULT
     */
    public final static String TOP_ANAT_R_SCRIPT_EXECUTABLE_KEY = 
            "org.bgee.core.topAnatRScriptExecutable";
    
    /**
     * A {@code String} that is the default value of the path of RScript Executable file 
     * which is used by {@code RCaller} to execute the R code.
     * 
     * @see #TOP_ANAT_R_SCRIPT_EXECUTABLE_KEY
     */
    public final static String TOP_ANAT_R_SCRIPT_EXECUTABLE_DEFAULT = "/usr/bin/Rscript";

    /**
     * A {@code String} that is the key to access to the System property that contains 
     * the current working directory of {@code R}, where all the other files required 
     * for the processing of the topAnat analysis are kept.
     * 
     * @see #TOP_ANAT_RCALLER_WORKING_DIRECTORY_DEFAULT
     */
    public final static String TOP_ANAT_RCALLER_WORKING_DIRECTORY_KEY = 
            "org.bgee.core.topAnatRCallerWorkingDirectory";

    /**
     * A {@code String} that is the default value of the current working directory of {@code R}, 
     * where all the other files required for the processing of the topAnat analysis are kept.
     * 
     * @see #TOP_ANAT_RCALLER_WORKING_DIRECTORY_KEY
     */
    public final static String TOP_ANAT_RCALLER_WORKING_DIRECTORY_DEFAULT = 
            "/home/bgee/webapps/TopAnatFiles/results";
    
    /**
     * A {@code String} that is the key to access to the System property that contains the name of 
     * the file which contains the additional modified topGO R functions used by topAnat 
     * to perform the analyses.
     * 
     * @see #TOP_ANAT_FUNCTIONS_FILE_DEFAULT
     */
    public final static String TOP_ANAT_FUNCTIONS_FILE_KEY = 
            "org.bgee.core.topAnatFunctionsFile";

    /**
     * A {@code String} that is the default value of the name of the file which contains the 
     * additional modified topGO R functions used by topAnat to perform the analyses.
     * 
     * @see #TOP_ANAT_FUNCTIONS_FILE_KEY
     */
    public final static String TOP_ANAT_FUNCTIONS_FILE_DEFAULT = 
            "/home/bgee/webapps/TopAnatFiles/R_scripts/topAnat_functions.R";   

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
    public final static String TOP_ANAT_RESULTS_WRITING_DIRECTORY_DEFAULT = 
            "webapps/bgee/TopAnatFiles/results/";   

    /**
     * A {@code String} that is the key to access to the System property that contains the name 
     * of the path to be used in URL to link to a file stored in the TopAnat result directory 
     * (see {@code #topAnatResultsWritingDirectory}).
     * 
     * @see #TOP_ANAT_RESULTS_URL_DIRECTORY_DEFAULT
     */
    public final static String TOP_ANAT_RESULTS_URL_DIRECTORY_KEY = 
            "org.bgee.core.topAnatResultsUrlDirectory";

    /**
     * A {@code String} that is the default value of the name of the path to be used in URL 
     * to link to a file stored in the TopAnat result directory
     * (see {@code #topAnatResultsWritingDirectory}).
     * 
     * @see #TOP_ANAT_RESULTS_URL_DIRECTORY_KEY
     */
    public final static String TOP_ANAT_RESULTS_URL_DIRECTORY_DEFAULT = 
            "bgee/TopAnatFiles/results/"; 
    
    /**
     * A {@code String} that is the path of RScript Executable file 
     * which is used by {@code RCaller} to execute the R code.
     */
    private final String topAnatRScriptExecutable;

    /**
     * A {@code String} that is the current working directory of {@code R}, 
     * where all the other files required for the processing of the topAnat analysis
     * are kept.
     * <p>
     * This directory should only be used with {@code RCaller}, to set the
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
    private final String topAnatRCallerWorkingDirectory;
    
    /**
     * A {@code String} that is the name of the file which contains the additional modified
     * topGO R functions used by topAnat to perform the analyses.
     */
    private final String topAnatFunctionsFile;  
    
    /**
     * A {@code String} that is the name of the directory to store outputs of the TopAnat analyses
     * that should be kept to be retrieved in case the same TopAnat query is performed again.
     * <p>
     * This directory has to be used when writing files. If you want to link to
     * such a file using a URL, you must use
     * {@code #topAnatResultsUrlDirectory}.
     * <p>
     * If you want to set the working directory for {@code R}, use
     * {@code #topAnatRCallerWorkingDirectory}
     * 
     * @see #topAnatResultsUrlDirectory
     * @see #topAnatCallerWorkingDirectory
     */ 
    private final String topAnatResultsWritingDirectory;    
    
    /**
     * A {@code String} that is the name of the path to be used in URL to link to a file stored
     * in the TopAnat result directory (see {@code #topAnatResultsWritingDirectory}).
     * <p>
     * This path has to be used to link to a TopAnat result file. If you want to
     * get the directory to write TopAnat result files, use
     * {@code #topAnatResultsWritingDirectory}
     * <p>
     * If you want to set the working directory for {@code R}, use
     * {@code #topOBORCallerWorkingDirectory}
     * 
     * @see #topOBOResultsWritingDirectory
     * @see #topOBORCallerWorkingDirectory
     */
    private final String topAnatResultsUrlDirectory;  
    
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
     * A {@code java.util.Properties} set used to load the values present in the Bgee property file
     * and where a {@code key} is searched
     */
    protected static Properties fileProps = null;

    /**
     * A {@code java.util.Properties} set used to load the values set in System properties
     * and where a {@code key} is searched
     */
    protected static Properties sysProps = null;

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
    protected BgeeProperties(Properties prop) 
    {
        log.entry(prop);
        loadProps();
        // Initialize all properties using the injected prop first, alternatively the System
        // properties and then the file. The default value provided will be use if none of the
        // previous solutions contain the property
        topAnatRScriptExecutable = getStringOption(prop, sysProps, fileProps, 
                TOP_ANAT_R_SCRIPT_EXECUTABLE_KEY,  
                TOP_ANAT_R_SCRIPT_EXECUTABLE_DEFAULT);
        topAnatRCallerWorkingDirectory = getStringOption(prop, sysProps, fileProps, 
                TOP_ANAT_RCALLER_WORKING_DIRECTORY_KEY,
                TOP_ANAT_RCALLER_WORKING_DIRECTORY_DEFAULT);
        topAnatFunctionsFile = getStringOption(prop, sysProps, fileProps, 
                TOP_ANAT_FUNCTIONS_FILE_KEY,
                TOP_ANAT_FUNCTIONS_FILE_DEFAULT);
        topAnatResultsWritingDirectory = getStringOption(prop, sysProps, fileProps, 
                TOP_ANAT_RESULTS_WRITING_DIRECTORY_KEY,
                TOP_ANAT_RESULTS_WRITING_DIRECTORY_DEFAULT);
        topAnatResultsUrlDirectory = getStringOption(prop, sysProps, fileProps, 
                TOP_ANAT_RESULTS_URL_DIRECTORY_KEY,
                TOP_ANAT_RESULTS_URL_DIRECTORY_DEFAULT);
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
     * This method loads the {@code java.util.Properties} from the System properties into 
     * {@code sysProps} and the {@code java.util.Properties} present in the properties file
     * in the classpath into {@code fileProps}
     */
    protected void loadProps(){
        log.info("Bgee-core properties initialization...");
        // Fetch the existing system properties
        sysProps = new Properties(System.getProperties());
        //try to get the properties file.
        //default name is bgee.properties
        //check first if an alternative name has been provided in the System properties
        String propertyFile = sysProps.getProperty(PROPERTIES_FILE_NAME_KEY, 
                PROPERTIES_FILE_NAME_DEFAULT);
        log.debug("Trying to use properties file {}", propertyFile);
        fileProps = null;
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
     * @param fileProps     {@code java.util.Properties} retrieved from the Bgee properties file, 
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
    protected String getStringOption(Properties prop, Properties sysProps, 
            Properties fileProps, String key, String defaultValue) {
        log.entry(prop, sysProps, fileProps, key, defaultValue);

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
    protected int getIntegerOption(Properties prop, Properties sysProps, 
            Properties fileProps, String key, int defaultValue) {
        log.entry(prop, fileProps, sysProps, key, defaultValue);

        String propValue = this.getStringOption(prop,sysProps, fileProps, key, null);
        int val = defaultValue;
        if (propValue != null) {
            val= Integer.valueOf(propValue);
        }

        return log.exit(val);
    }

    /**
     * @return A {@code String} that is the path of RScript Executable file which is used by 
     * {@code RCaller} to execute the {@code R} code.
     */
    public String getTopAnatRScriptExecutable() {
        return topAnatRScriptExecutable;
    }

    /**
     * @return A {@code String} that is the current working directory of {@code R}, where all
     * the other files required for the processing of the topAnat analysis are kept.
     */
    public String getTopAnatRCallerWorkingDirectory() {
        return topAnatRCallerWorkingDirectory;
    }

    /**
     * @return A {@code String} that is the name of the file which contains the additional modified 
     * topGO {@code R} functions used by topAnat to perform the analyses.
     */
    public String getTopAnatFunctionsFile() {
        return topAnatFunctionsFile;
    }

    /**
     * @return A {@code String} that is the name of the directory to store outputs of the TopAnat 
     * analyses that should be kept to be retrieved in case the same TopAnat query is performed again.
     */
    public String getTopAnatResultsWritingDirectory() {
        return topAnatResultsWritingDirectory;
    }

    /**
     * @return A {@code String} that is the name of the path to be used in URL to link to a file stored 
     * in the TopAnat result directory
     */
    public String getTopAnatResultsUrlDirectory() {
        return topAnatResultsUrlDirectory;
    }

}
