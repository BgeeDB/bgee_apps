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
 * You should always call {@link BgeeProperties().release()} at the end of the execution of a thread, 
 * or {@link #releaseAll()} in multi-threads context (for instance, in a webapp context 
 * when the webapp is shutdown). 
 * <p>
 * This class has been inspired from {@code net.sf.log4jdbc.DriverSpy}
 * developed by Arthur Blake.
 * 
 * @author Frederic Bastian
 * @author Mathieu Seppey
 * @author Valentine Rech de Laval
 * @version Bgee 13, Oct. 2015
 * @since Bgee 13
 */
public class BgeeProperties {

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
     * of the path of RScript Executable file which is used to execute the R code.
     * 
     * @see #TOP_ANAT_R_SCRIPT_EXECUTABLE_DEFAULT
     */
    public final static String TOP_ANAT_R_SCRIPT_EXECUTABLE_KEY = 
            "org.bgee.core.topAnatRScriptExecutable";
    
    /**
     * A {@code String} that is the default value of the path of RScript Executable file 
     * which is used to execute the R code.
     * 
     * @see #TOP_ANAT_R_SCRIPT_EXECUTABLE_KEY
     */
    public final static String TOP_ANAT_R_SCRIPT_EXECUTABLE_DEFAULT = "/usr/bin/Rscript";

    /**
     * A {@code String} that is the key to access to the System property that contains 
     * the current working directory of {@code R}, where all the other files required 
     * for the processing of the topAnat analysis are kept.
     * 
     * @see #TOP_ANAT_R_WORKING_DIRECTORY_DEFAULT
     */
    public final static String TOP_ANAT_R_WORKING_DIRECTORY_KEY = 
            "org.bgee.core.topAnatRWorkingDirectory";

    /**
     * A {@code String} that is the default value of the current working directory of {@code R}, 
     * where all the other files required for the processing of the topAnat analysis are kept.
     * 
     * @see #TOP_ANAT_R_WORKING_DIRECTORY_KEY
     */
    public final static String TOP_ANAT_R_WORKING_DIRECTORY_DEFAULT = 
            "TopAnatFiles/results/";
    
    /**
     * A {@code String} that is the key to access to the System property that contains the name of 
     * the file containing R functions used by topAnat.
     * 
     * @see #TOP_ANAT_FUNCTION_FILE_DEFAULT
     */
    public final static String TOP_ANAT_FUNCTION_FILE_KEY = 
            "org.bgee.core.topAnatFunctionFile";

    /**
     * A {@code String} that is the default value of the name of the file which contains the 
     * additional modified topGO R functions used by topAnat to perform the analyses.
     * 
     * @see #TOP_ANAT_FUNCTION_FILE_KEY
     */
    public final static String TOP_ANAT_FUNCTION_FILE_DEFAULT = 
            "/R_scripts/topAnat_functions.R";   

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
            "TopAnatFiles/results/";   
   
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
        log.entry();
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
        return log.exit(filePropsToReturn);
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
     * @param SYS_PROPS      {@code java.util.Properties} retrieved from System properties, 
     *                      where {@code key} is searched in second
     * @param FILE_PROPS     {@code java.util.Properties} retrieved from the Bgee properties file, 
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
     * ({@code SYS_PROPS}), then, if undefined or empty, from properties retrieved from the 
     * Bgee property file ({@code FILE_PROPS}). If the property is still undefined or empty 
     * return {@code defaultValue}.
     *
     * @param prop          A {@code java.util.Properties} instance that contains the system 
     *                      properties to look for {@code key} first
     * @param SYS_PROPS      {@code java.util.Properties} retrieved from System properties, 
     *                      where {@code key} is searched in second
     * @param FILE_PROPS     {@code java.util.Properties} retrieved 
     *                      from the Bgee properties file, 
     *                      where {@code key} is searched in if {@code prop} and {@code SYS_PROPS}
     *                      were undefined or empty for {@code key}. 
     *                      Can be {@code null} if no properties file was found.
     * @param defaultValue  default value that will be returned if the property 
     *                      is undefined or empty in all {@code Properties}.
     *
     * @return             An {@code int} corresponding to the value
     *                     for that property key.
     *                     Or {@code defaultValue} if not defined or empty.
     */
    protected static int getIntegerOption(Properties prop, Properties sysProps, 
            Properties fileProps, String key, int defaultValue) {
        log.entry(prop, fileProps, sysProps, key, defaultValue);
    
        String propValue = getStringOption(prop,sysProps, fileProps, key, null);
        int val = defaultValue;
        if (propValue != null) {
            val= Integer.valueOf(propValue);
        }
    
        return log.exit(val);
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
        log.entry(prop);
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
        log.entry();

        //this AtomicBoolean will act more or less like a lock 
        //(no new BgeeProperties can be obtained after this AtomicBoolean is set to true).
        //It's not totally true, but we don't expect any major error if it doesn't act like a lock.
        bgeePropertiesClosed.set(true);

        int propCount = bgeeProperties.size();
        bgeeProperties.clear();

        return log.exit(propCount);
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
        log.entry(prop);
        log.debug("Bgee-core properties initialization...");
        // Initialize all properties using the injected prop first, alternatively the System
        // properties and then the file. The default value provided will be use if none of the
        // previous solutions contain the property
        topAnatRScriptExecutable = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                TOP_ANAT_R_SCRIPT_EXECUTABLE_KEY,  
                TOP_ANAT_R_SCRIPT_EXECUTABLE_DEFAULT);
        topAnatRWorkingDirectory = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                TOP_ANAT_R_WORKING_DIRECTORY_KEY,
                TOP_ANAT_R_WORKING_DIRECTORY_DEFAULT);
        topAnatFunctionFile = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                TOP_ANAT_FUNCTION_FILE_KEY,
                TOP_ANAT_FUNCTION_FILE_DEFAULT);
        topAnatResultsWritingDirectory = getStringOption(prop, SYS_PROPS, FILE_PROPS, 
                TOP_ANAT_RESULTS_WRITING_DIRECTORY_KEY,
                TOP_ANAT_RESULTS_WRITING_DIRECTORY_DEFAULT);
        log.debug("Initialization done.");
        log.exit();
    }

    /**
     * Releases this {@code BgeeProperties}. 
     * A call to {@link #getBgeeProperties()} from the thread that was holding it 
     * will return a new {@code BgeeProperties} instance. 
     * 
     * @return  {@code true} if this {@code BgeeProperties} was released, 
     *          {@code false} if it was already released.
     */
    public boolean release() {
        log.entry();
        return log.exit(bgeeProperties.values().remove(this));
    }
    /**
     * Determines whether this {@code BgeeProperties} was released 
     * (following a call to {@link #release()}).
     * 
     * @return  {@code true} if this {@code BgeeProperties} was released, 
     *          {@code false} otherwise.
     */
    public boolean isReleased() {
        log.entry();
        return log.exit(!bgeeProperties.containsValue(this));
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
}
