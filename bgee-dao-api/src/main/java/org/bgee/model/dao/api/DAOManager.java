package org.bgee.model.dao.api;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO;
import org.bgee.model.dao.api.anatdev.SexDAO;
import org.bgee.model.dao.api.anatdev.StageDAO;
import org.bgee.model.dao.api.anatdev.TaxonConstraintDAO;
import org.bgee.model.dao.api.anatdev.mapping.RawSimilarityAnnotationDAO;
import org.bgee.model.dao.api.anatdev.mapping.StageGroupingDAO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.rawdata.RawExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.SamplePValueDAO;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.call.DiffExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.call.GlobalExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCountDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.est.ESTDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.est.ESTLibraryDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituEvidenceDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituExperimentDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituSpotDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipTypeDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixProbesetDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.MicroarrayExperimentDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqExperimentDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryAnnotatedSampleDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqResultAnnotatedSampleDAO;
import org.bgee.model.dao.api.file.DownloadFileDAO;
import org.bgee.model.dao.api.file.SpeciesDataGroupDAO;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneHomologsDAO;
import org.bgee.model.dao.api.gene.GeneNameSynonymDAO;
import org.bgee.model.dao.api.gene.GeneOntologyDAO;
import org.bgee.model.dao.api.gene.GeneXRefDAO;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO;
import org.bgee.model.dao.api.keyword.KeywordDAO;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO;
import org.bgee.model.dao.api.ontologycommon.EvidenceOntologyDAO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO;
import org.bgee.model.dao.api.source.SourceDAO;
import org.bgee.model.dao.api.source.SourceToSpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.TaxonDAO;

/**
 * Manager of DAOs, following the abstract factory pattern, to obtain and manage DAOs. 
 * This abstract class is then implemented by {@code Service Provider}s, 
 * that are automatically discovered and loaded. 
 * <p>
 * To obtain a DAOManager, clients should call one of the {@code getDAOManager} methods.
 * When calling the {@code getDAOManager} methods, the {@code DAOManager} returned 
 * is a "per-thread singleton": a {@code DAOManager} is instantiated 
 * the first time a {@code getDAOManager} method is called inside a given thread, 
 * and the same instance is then always returned when calling 
 * a {@code getDAOManager} method inside the same thread. 
 * An exception is if you call this method after having closed the {@code DAOManager}.
 * In that case, a call to a {@code getDAOManager} method from this thread 
 * would return a new {@code DAOManager} instance. 
 * <p>
 * Parameters can be provided to the DAOManager either via System properties, 
 * or via configuration file, or by using the method {@link #getDAOManager(Properties)}. 
 * Parameters to be provided are specific to the Service Provider used. 
 * For instance, if this {@code DAOManager} was obtained from a Service provider 
 * using the JDBC API to use a SQL database, then the parameters might contain 
 * the URL to connect to the database. It is up to each Service provider to specify 
 * what are the parameters needed. 
 * <p>
 * If a configuration file is used, it must be placed in the classpath. Its default 
 * name is {@link #DEFAULT_CONFIG_FILE}. This can be changed via System properties, 
 * using the key {@link #CONFIG_FILE_KEY}.
 * <p>
 * See {@link #getDAOManager(Properties)} and {@link #getDAOManager()} for more details 
 * about the instantiation process of a {@code DAOManager}.
 * <p>
 * Please note that it is extremely important to close {@code DAOManager}s 
 * when done using them, otherwise a {@code DAOManager} could be improperly 
 * reused if this API is used in an application using thread pooling, or if a thread 
 * re-uses a previously-used ID (which is standard behavior of the JDK). Methods 
 * available to close {@code DAOManager}s are {@link #close()}, {@link #kill()}, 
 * {@link #closeAll()}, {@link #kill(long)}. This class implements the 
 * {@code AutoCloseable} interface, so that it can be used in a 
 * {@code try-with-resources} statement.
 * <p>
 * This class supports the standard <a href=
 * 'http://docs.oracle.com/javase/6/docs/technotes/guides/jar/jar.html#Service%20Provider'> 
 * Service Provider</a> mechanism. Concrete implementations must include the file 
 * {@code META-INF/services/org.bgee.model.dao.api.DAOManager}. The file must contain 
 * the name of the implementation of {@code DAOManager}. For example, 
 * to load the {@code my.sql.Manager} class, 
 * the {@code META-INF/services/org.bgee.model.dao.api.DAOManager} file 
 * would contain the entry:
 * <pre>my.sql.Manager</pre>
 * To conform to the {@code Service Provider} requirements, the class implementing 
 * {@code DAOManager} must provide a default constructor with no arguments. 
 * <p>
 * Important note about {@code ServiceLoader} and shared {@code ClassLoader} 
 * (like in tomcat): http://stackoverflow.com/a/7220918/1768736
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Apr. 2019
 * @since   Bgee 13, July 2013
 */
public abstract class DAOManager implements AutoCloseable
{
    //*****************************************
    //  CLASS ATTRIBUTES AND METHODS
    //*****************************************
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(DAOManager.class.getName());
    
    /**
     * A {@code Collection} of {@code String}s containing the qualified class names 
     * of all service providers of this {@code DAOManager}. This is because 
     * using the ServiceLoader in a servlet container context can be problematic, 
     * and the automatic loading not work. As we know all service providers 
     * (we just don't know which one is used), we will try to load all of them 
     * "manually", so we need their class names.
     * 
     * @see #getServiceProviders
     */
    private static final Collection<String> providerClassNames = 
            Arrays.asList("org.bgee.model.dao.mysql.connector.MySQLDAOManager");
    
    /**
     * A {@code String} representing the default name of the configuration file 
     * to retrieve parameters from. The name of the file to use can be changed 
     * via system properties, using the key {@link #CONFIG_FILE_KEY};
     */
    public static final String DEFAULT_CONFIG_FILE = "/bgee.dao.properties";
    /**
     * A {@code String} representing the key to use to retrieve from system properties 
     * an alternative name for the configuration file.
     */
    public static final String CONFIG_FILE_KEY = "bgee.dao.properties.file";
    
    /**
     * The {@code Properties} obtained at class loading either from system properties, 
     * or from a configuration file (see {@link #DEFAULT_CONFIG_FILE}). They will be used 
     * when {@link #getDAOManager()} is called, as default properties.
     */
    private static final Properties properties = DAOManager.loadProperties();
    
    /**
     * Get the default <code>java.util.Properties</code> either from the System properties, 
     * or from a configuration file. The name of the configuration file can be changed 
     * via System properties (see {@link #CONFIG_FILE_KEY}). This default properties 
     * will be used when {@link #getDAOManager()} is called. 
     * @return      The <code>java.util.Properties</code> to get properties from.
     */
    private final static Properties loadProperties() {
        log.traceEntry();
        
        Properties props = new Properties(System.getProperties());
        //try to get the properties file.
        //default name is bgee.dao.properties
        //check first if an alternative name has been provided in the System properties
        String propertyFile = props.getProperty(CONFIG_FILE_KEY, DEFAULT_CONFIG_FILE);
        log.debug("Trying to use properties file " + propertyFile);
        InputStream propStream = DAOManager.class.getResourceAsStream(propertyFile);
        if (propStream != null) {
            try {
                props.load(propStream);
            } catch (IOException e) {
                //if properties are not correctly set, we let the getDAOManager method 
                //throw an Exception if no DAOManager accepting the parameters is found.
                log.catching(e);
                return log.traceExit((Properties) null);
            } finally {
                try {
                    propStream.close();
                } catch (IOException e) {
                    log.catching(e);
                    return log.traceExit((Properties) null);
                }
            }
            log.debug("{} loaded from classpath", propertyFile);
        } else {
            log.debug("{} not found in classpath. Using System properties.", propertyFile);
        }
        
        return log.traceExit(props);
    }
    
    /**
     * Return the {@code Properties} obtained at class loading either from system properties, 
     * or from a configuration file (see {@link #DEFAULT_CONFIG_FILE}). They will be used 
     * when {@link #getDAOManager()} is called, as default properties.
     * <p>
     * The {@code Properties} returned are a copy with the actual {@code Properties} 
     * used by this object defined as default values.
     * 
     * @return  the {@code Properties} obtained at class loading.
     */
    public static Properties getDefaultProperties() {
        return new Properties(DAOManager.properties);
    }
    
    /**
     * A {@code ConcurrentMap} used to store {@code DAOManager}s, 
     * associated to their ID as key (corresponding to the ID of the thread 
     * who requested the {@code DAOManager}). 
     * <p>
     * This {@code Map} is used to provide a unique and independent 
     * {@code DAOManager} instance to each thread: a {@code DAOManager} is added 
     * to this {@code Map} when a {@code getDAOManager} method is called, 
     * if the thread ID is not already present in the {@code keySet} 
     * of the {@code Map}. Otherwise, the already stored {@code DAOManager} 
     * is returned. 
     * <p>
     * If a {@code ThreadLocal} was not used, it is because 
     * this {@code Map} is used by other treads, 
     * for instance when a {@code ShutdownListener} 
     * want to properly release all {@code DAOManager}s; 
     * or when a thread performing monitoring of another thread want to kill it.
     * <p>
     * A {@code DAOManager} is removed from this {@code Map} when 
     * {@link #close()} is called on it, 
     * or when {@link #kill(long)} is called using the ID assigned to this thread, 
     * or when the method {@link #closeAll()} is called. 
     * All {@code DAOManager}s are removed when {@link #closeAll()} is called.
     */
    private static final ConcurrentMap<Long, DAOManager> managers = 
            new ConcurrentHashMap<Long, DAOManager>(); 
    /**
     * This {@code ConcurrentMap} stores one instance of {@code DAOManager} for each 
     * service provider used (so for each class name among the {@code DAOManager}s 
     * used). They are still stored even after having called {@code close} on 
     * a {@code DAOManager} that is a representative. This is to be able  
     * to call {@link #shutdown()} on them, when {@link #closeAll()} is called, 
     * to bypass the limitation of Java of not allowing abstract static methods.
     */
    private static final ConcurrentMap<String, DAOManager> representativeManagers = 
            new ConcurrentHashMap<String, DAOManager>(); 
    
    /**
     * A unmodifiable {@code List} containing all available providers of 
     * the {@code DAOManager} service, in the order they were obtained 
     * from the {@code ServiceLoader}. This is needed because we want 
     * to load services from the {@code ServiceLoader} only once 
     * by {@code ClassLoader}. So we could have used as attribute  
     * a {@code static final ServiceLoader}, but {@code ServiceLoader} 
     * lazyly instantiate service providers and is not thread-safe, so we would 
     * have troubles in a multi-threading context. So we load all providers 
     * at once, we don't except this pre-loading to require too much memory 
     * (very few service providers available, used in very few libraries). 
     * <p>
     * As this {@code List} is unmodifiable and declared {@code final}, 
     * and the stored {@code DAOManager}s will always be copied before use, 
     * this attribute can safely be accessed in a multi-threading context. 
     */
    private static final List<DAOManager> serviceProviders = 
            DAOManager.getServiceProviders();
    /**
     * Get all available providers of the {@code DAOManager} service 
     * from the {@code ServiceLoader}, as an unmodifiable {@code List}. 
     * Empty {@code List} if no service providers could be found. 
     * 
     * @return     An unmodifiable {@code List} of {@code DAOManager}s, 
     *             in the same order they were obtained from the {@code ServiceLoader}. 
     *             Empty {@code List} if no service providers could be found.
     */
    private final static List<DAOManager> getServiceProviders() {
        log.traceEntry();
        log.debug("Loading DAOManager service providers");
        List<DAOManager> providers = new ArrayList<DAOManager>();
        //first, we try to load the classes that are the service providers: 
        //Using the ServiceLoader in a servlet container context can be problematic, 
        //and the automatic loading not work. As we know all service providers 
        //(we just don't know which one is used), we try to load all of then 
        //"manually"
        for (String className: DAOManager.providerClassNames) {
            try {
                providers.add((DAOManager) Class.forName(className).getDeclaredConstructor().newInstance());
                log.debug("A DAOManager service provider was loaded by class name");
            } catch (ClassNotFoundException | InstantiationException | 
                    IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                //we do nothing, maybe the jar for this service provider is not present
                log.catching(Level.TRACE, e);
            }
        }
        //now, we also check the "classical" service loader mechanism, maybe 
        //there is a sevice provider we don't know about
        ServiceLoader<DAOManager> loader = 
                ServiceLoader.load(DAOManager.class);
        for (DAOManager provider: loader) {
            if (!DAOManager.providerClassNames.contains(provider.getClass().getName())) {
                providers.add(provider);
                log.debug("A DAOManager service provider was loaded by the ServiceLoader");
            }
        }
        log.debug("Providers found: {}", providers);
        return log.traceExit(Collections.unmodifiableList(providers));
    }
    
    /**
     * A volatile {@code boolean} to define if {@code DAOManager}s 
     * can still be acquired (using the {@code getDAOManager} methods), 
     * or if it is not possible anymore (meaning that the method {@link #closeAll()} 
     * has been called).
     */
    private static final AtomicBoolean allClosed = new AtomicBoolean(false);
    
    /**
     * Return a {@code DAOManager} instance with its parameters set using 
     * the provided {@code Properties}. If it is the first call to one 
     * of the {@code getDAOManager} methods within a given thread, 
     * the {@code getDAOManager} methods return a newly instantiated 
     * {@code DAOManager}. Following calls from the same thread will always 
     * return the same instance ("per-thread singleton"), unless the {@code close} 
     * or {@code kill} method was called. Please note that it is extremely 
     * important to close or kill {@code DAOManager} when done using it. 
     * <p>
     * If no {@code DAOManager} is available for this thread yet, this method 
     * will return the first available {@code Service Provider} that accepts 
     * {@code props}, meaning that it finds all its required parameters in it. 
     * This method will return {@code null} if none could be found, or if no service 
     * providers were available at all. {@code props} can be {@code null}, 
     * but that would mean that the {@code Service Provider} obtained had  
     * absolutely no mandatory parameters.
     * <p>
     * If a {@code DAOManager} is already available for this thread, 
     * then this method will simply return it, after having provided {@code props} 
     * to it, so that its parameters can be changed after obtaining it. 
     * If {@code props} is {@code null}, then the previously provided 
     * parameters will still be used. If {@code props} is not null, 
     * but the already instantiated {@code DAOManager} does not find its required 
     * parameters in it, an {@code IllegalStateException} will be thrown.
     * <p>
     * If caller wants to use a different {@code Service Provider}, accepting 
     * different parameters, then {@code close} or {@code kill} 
     * should first be called. 
     * <p>
     * This method will throw an {@code IllegalStateException} if {@link #closeAll()} 
     * was called prior to calling this method, and a {@code ServiceConfigurationError} 
     * if an error occurred while trying to find a service provider from the 
     * {@code ServiceLoader}. 
     * 
     * @param props    A {@code java.util.Properties} object, 
     *                 to be passed to the {@code DAOManager} instance. 
     * @return            A {@code DAOManager} accepting the parameters provided 
     *                 in {@code props}. {@code null} if none could be found 
     *                 accepting the parameters, or if no service providers 
     *                 were available at all.
     * @throws IllegalStateException   if {@code closeAll} was already called, 
     *                                 so that no {@code DAOManager}s can be 
     *                                 acquired anymore. Or if the already instantiated 
     *                                 {@code DAOManager} does not accept the provided 
     *                                 parameters.
     * @throws ServiceConfigurationError   If an error occurred while trying to find 
     *                                        a {@code DAOManager} service provider 
     *                                        from the {@code ServiceLoader}. 
     * @see #getDAOManager()
     */
    public static DAOManager getDAOManager(Properties props) 
        throws IllegalStateException, ServiceConfigurationError {
        log.traceEntry("{}", props);

        if (DAOManager.allClosed.get()) {
            throw log.throwing(
                    new IllegalStateException("closeAll() has been already called, " +
                    "it is not possible to acquire a DAOManager anymore"));
        }

        //get Thread ID as key. As Thread IDs can be reused, it is extremely important 
        //to call close() on a DAOManager after use. We use Thread ID because 
        //ThreadLocal are source of troubles. 
        long threadId = Thread.currentThread().getId();
        log.debug("Trying to obtain a DAOManager with ID {}", threadId);

        DAOManager manager = managers.get(threadId);
        Throwable toThrow = null;
        if (manager == null) {
            //obtain a DAOManager from a Service Provider accepting the parameters
            log.debug("No DAOManager available for this thread, trying to obtain a DAOManager from a Service provider");
            
            Iterator<DAOManager> managerIterator = DAOManager.serviceProviders.iterator();
            providers: while (managerIterator.hasNext()) {
                try {
                    //need to get a new instance, because as we store 
                    //in a static attribute the providers, it always returns 
                    //a same instance, while we want one instance per thread. 
                    DAOManager testManager =
                            managerIterator.next().getClass().getDeclaredConstructor().newInstance();

                    log.trace("Testing: {}", testManager);
                    if (props != null) {
                        testManager.setParameters(props);
                    }
                    //parameters accepted, we will use this manager
                    manager = testManager;
                    manager.setId(threadId);
                    //as soon as a DAOManager is loaded, we need to store a representative 
                    //of its class to properly unload them at application shutdown
                    representativeManagers.putIfAbsent(
                            testManager.getClass().getName(), testManager);
                    
                    log.debug("Valid DAOManager: {}", manager);
                    break providers;

                } catch (IllegalArgumentException e) {
                    //do nothing, this exception is thrown when calling 
                    //setParameters to try to find the appropriate service provider. 
                    log.catching(Level.TRACE, e);
                } catch (Exception e) {
                    //this catch block is needed only because of the line 
                    //managerIterator.next().getClass().newInstance();
                    //These exceptions should never happen, as service providers 
                    //must implement a default public constructor with no arguments. 
                    //If such an exception occurred, it could be seen as 
                    //a ServiceConfigurationError
                    toThrow = new ServiceConfigurationError(
                            "DAOManager service provider instantiation error: " +
                            "service provider did not provide a valid constructor", e);
                    break providers;
                }
            }    
            if (manager == null) {
                log.debug("No DAOManager could be found");
            } else {
                //we don't use putifAbsent, as idAssigned make sure 
                //there won't be any multi-threading key collision
                managers.put(threadId, manager);
            }
        } else {
            log.debug("Get an already existing DAOManager instance");
            if (props != null) {
                try {
                    manager.setParameters(props);
                } catch (IllegalArgumentException e) {
                    toThrow = e;
                }
            }
        }
        if (manager != null && toThrow == null) {
            //check that the manager was not closed by another thread while we were 
            //acquiring it
            synchronized (manager.closed) {
                if (manager.isClosed()) {
                    //if the manager was closed following a call to closeAll
                    if (DAOManager.allClosed.get()) {
                        toThrow = new IllegalStateException(
                                "closeAll() has been already called, " +
                                "it is not possible to acquire a DAOManager anymore");
                    }
                    //otherwise, it means it was killed following a call to kill(long).
                    //we just return the closed DAOManager, this will throw 
                    //an IllegalStateException when trying to acquire a DAO from it
                }
                if (toThrow == null) {
                    return log.traceExit(manager);
                }
            }
        }
        
        if (toThrow != null) {
            if (toThrow instanceof IllegalStateException) {
                throw log.throwing((IllegalStateException) toThrow);
            } else if (toThrow instanceof ServiceConfigurationError) {
                throw log.throwing((ServiceConfigurationError) toThrow);
            } else if (toThrow instanceof IllegalArgumentException) {
                //this exception means that an already instantiated DAOManager
                //refused the parameters. This is then an illegal state.
                throw log.throwing(new IllegalStateException(toThrow));
            } else {
                throw log.throwing(
                        new ServiceConfigurationError("Unexpected error", toThrow));
            }
        }
        
        return log.traceExit((DAOManager) null);
    }
    
    /**
     * Return a {@code DAOManager} instance. If it is the first call to one 
     * of the {@code getDAOManager} methods within a given thread, 
     * the {@code getDAOManager} methods return a newly instantiated 
     * {@code DAOManager}. Following calls from the same thread will always 
     * return the same instance ("per-thread singleton"), unless the {@code close} 
     * or {@code kill} method was called. 
     * <p>
     * If no {@code DAOManager} is available for this thread yet, this method 
     * will try to obtain a {@code DAOManager} from the first Service provider 
     * accepting the default parameters, or will return {@code null} if none 
     * could be found. The default parameters are retrieved either 
     * from system properties, or from a configuration file. This method then calls 
     * {@link #getDAOManager(Properties)} and provide to it these default 
     * properties. If these properties were not {@code null}, the 
     * {@code DAOManager} obtained has to accept them, meaning that it found 
     * all its mandatory parameters in it. Note that the properties obtained from 
     * system properties or configuration file are obtained only once at class loading.
     * <p>
     * If a {@code DAOManager} is already available for this thread, 
     * then this method will simply return it, with the previously provided properties 
     * still in use. If you want to change these properties, you need to directly 
     * call {@link #getDAOManager(Properties)}.
     * <p>
     * This method will throw an {@code IllegalStateException} if {@link #closeAll()} 
     * was called prior to calling this method, and a {@code ServiceConfigurationError} 
     * if an error occurred while trying to find a service provider from the 
     * {@code ServiceLoader}. 
     * 
     * @return                 The first {@code DAOManager} available,  
     *                         {@code null} if no service providers were available at all.
     * @throws IllegalStateException   if {@code closeAll} was already called, 
     *                                    so that no {@code DAOManager}s can be 
     *                                    acquired anymore. 
     * @throws ServiceConfigurationError    If an error occurred while trying to find 
     *                                         a {@code DAOManager} service provider 
     *                                         from the {@code ServiceLoader}. 
     * @see #getDAOManager(Properties)
     */
    //TODO: we must completely get rid of these "per-thread singletons" and static methods...
    public static DAOManager getDAOManager() throws IllegalStateException, ServiceConfigurationError {
        log.traceEntry();
        
        if (hasDAOManager()) {
            //this will avoid useless parsing of the properties.
            return log.traceExit(getDAOManager(null));
        }
        
        //otherwise, we use the properties obtained at class loading.
        return log.traceExit(DAOManager.getDAOManager(DAOManager.properties));
    }
    
    /**
     * Determine whether the {@code Thread} calling this method already 
     * holds a {@code DAOManager}. It is useful for instance when willing 
     * to close all resources at the end of an applicative code. For instance, 
     * if the thread was not holding a {@code DAOManager}, calling 
     * {@code DAOManager.getDAOManager().close()} would instantiate 
     * a {@code DAOManager} just for closing it... Applicative code should rather do: 
     * <pre>if (DAOManager.hasDAOManager()) {
     *     DAOManager.getDAOManager().close();
     * }</pre>
     * There is a risk that another thread could interleave to close 
     * the {@code DAOManager} between the test and the call to {@code close}, 
     * but it would not have any harmful effect. 
     * 
     * @return    A {@code boolean} {@code true} if the {@code Thread} 
     *             calling this method currently holds a {@code DAOManager}, 
     *             {@code false} otherwise. 
     */
    public static boolean hasDAOManager() {
        log.traceEntry();
        return log.traceExit(managers.containsKey(Thread.currentThread().getId()));
    }
    
    /**
     * Call {@link #close()} on all {@code DAOManager} instances currently registered,
     * and prevent any new {@code DAOManager} instance to be obtained again 
     * (calling a {@code getDAOManager} method from any thread 
     * after having called this method will throw an {@code IllegalStateException}). 
     * <p>
     * This method returns the number of {@code DAOManager}s that were closed. 
     * <p>
     * This method is called for instance when a {@code ShutdownListener} 
     * want to release all resources using a data source.
     * 
     * @return     An {@code int} that is the number of {@code DAOManager} instances  
     *             that were closed.
     * 
     * @throws DAOException If an error occurred while closing the managers.
     */
    public static int closeAll() throws DAOException {
        log.traceEntry();
        
        //this volatile boolean will act more or less like a lock 
        //(no new DAOManager can be obtained after this boolean is set to true).
        //It's not totally true, but we don't except any major error 
        //if it doesn't act like a lock.
        if (DAOManager.allClosed.getAndSet(true)) {
            //already closed, or closing.
            log.traceExit(); return 0;
        }

        int managerCount = 0;
        for (DAOManager manager: managers.values()) {
            managerCount++;
            manager.close();
        }
        //call shutdown on representative DAOManagers (see representativeManagers attribute, 
        //and shutdown method documentation)
        for (DAOManager manager: representativeManagers.values()) {
            manager.shutdown();
        }

        return log.traceExit(managerCount);
    }
    
    /**
     * Call {@link #kill()} on the {@code DAOManager} currently registered 
     * with an ID (returned by {@link #getId()}) equals to {@code managerId}.
     * 
     * @param managerId     A {@code long} corresponding to the ID of 
     *                         the {@code DAOManager} to kill.
     * @throws DAOException If an error occurred while killing the manager.
     * @see #kill()
     */
    public static void kill(long managerId) throws DAOException {
        log.traceEntry("{}", managerId);
        DAOManager manager = managers.get(managerId);
        if (manager != null) {
            manager.kill();
        }
        log.traceExit();
    }
    /**
     * Call {@link #kill()} on the {@code DAOManager} currently associated 
     * with {@code thread}.
     * 
     * @param thread     A {@code Thread} associated with a {@code DAOManager}. 
     * @throws DAOException If an error occurred while killing the manager.
     * @see #kill()
     */
    /*
     * This method is used to hide the implementation detail that the ID associated 
     * to a DAOManager is its holder Thread ID.
     */
    //TODO: actually, rather than defining static methods (which we try to avoid), 
    //we could have each DAOManager instance to store its Thread used to run it, 
    //like in org.bgee.model.job.Job. This would allow to check the interruption status 
    //of the running Thread, from another Thread. And we could have a "DAOManagerService" or "pool" 
    //allowing to retrieve the DAOManager of a Thread from another Thread (as in org.bgee.model.job.JobService)
    public static void kill(Thread thread) throws DAOException {
        log.traceEntry("{}", thread);
        DAOManager.kill(thread.getId());
        log.traceExit();
    }
    
    //*****************************************
    //  INSTANCE ATTRIBUTES AND METHODS
    //*****************************************    
    //FIXME: needs to make all these variables final
    /**
     * An {@code AtomicBoolean} to indicate whether 
     * this {@code DAOManager} was closed (following a call to {@link #close()}, 
     * {@link #closeAll()}, {@link #kill()}, or {@link #kill(long)}).
     * <p>
     * This attribute is {@code final} because it is used as a lock to perform 
     * some atomic operations. It is an {@code AtomicBoolean} as it can be read 
     * by method not acquiring a lock on it.
     */
    private final AtomicBoolean closed;
    /**
     * A {@code volatile} {@code boolean} to indicate whether 
     * this {@code DAOManager} was requested to be killed ({@link #kill()} 
     * or {@link #kill(long)}). This does not necessarily mean that a query 
     * was interrupted, only that the {@code DAOManager} received 
     * a {@code kill} command (if no query was running when receiving the command, 
     * none were killed).
     * <p>
     * This attribute is {@code volatile} as it can be read and written 
     * from different threads.
     * 
     */
    private volatile boolean killed;
    /**
     * The ID of this {@code DAOManager}, corresponding to the Thread ID 
     * who requested it. This is for the sake of avoiding using a {@code ThreadLocal} 
     * to associate a {@code DAOManager} to a thread (generates issues 
     * in a thread pooling context). As Thread IDs can be reused, it is extremely important 
     * to call close() on a DAOManager after use.
     */
    private volatile long id;
    
    /**
     * The {@code Properties} that were used to obtain this {@code DAOManager}.
     */
    private volatile Properties parameters;
    /**
     * Every concrete implementation must provide a default constructor 
     * with no parameters. 
     */
    public DAOManager() {
        log.traceEntry();
        this.closed = new AtomicBoolean(false);
        this.setKilled(false);
        this.parameters = null;
        log.traceExit();
    }
    
    /**
     * Return the ID associated to this {@code DAOManager}. 
     * This ID can be used to call {@link #kill(long)}.
     * 
     * @return     A {@code long} that is the ID of this {@code DAOManager}.
     */
    public long getId() {
        log.traceEntry();
        return log.traceExit(this.id);
    }
    /**
     * Set the ID of this {@code DAOManager}. 
     * 
     * @param id     the ID of this {@code DAOManager}
     */
    private void setId(long id) {
        this.id = id;
    }
    
    /**
     * Close all resources managed by this {@code DAOManager} instance, 
     * and release it (a call to a {@code getDAOManager} method from the thread 
     * that was holding it will return a new {@code DAOManager} instance).
     * <p>
     * Following a call to this method, it is not possible to acquire DAOs 
     * from this {@code DAOManager} instance anymore.
     * <p>
     * Specified by {@link java.lang.AutoCloseable#close()}.
     * 
     * @throws DAOException If an error occurred while closing the manager.
     * @see #closeAll()
     * @see #kill()
     * @see #kill(long)
     */
    @Override
    public void close() throws DAOException {
        log.traceEntry();
        if (this.atomicCloseAndRemoveFromPool(false)) {
            //implementation-specific code here
            this.closeDAOManager();
        }
        
        log.traceExit();
    }
    /**
     * Determine whether this {@code DAOManager} was closed 
     * (following a call to {@link #close()}, {@link #closeAll()}, 
     * {@link #kill()}, or {@link #kill(long)}).
     * 
     * @return    {@code true} if this {@code DAOManager} was closed, 
     *             {@code false} otherwise.
     */
    public boolean isClosed() {
        log.traceEntry();
        return log.traceExit(closed.get());
    }
    /**
     * Set {@link #closed}. The only method that should call this one besides constructors 
     * is {@link #atomicCloseAndRemoveFromPool(boolean)}. 
     * 
     * @param closed     a {@code boolean} to set {@link #closed}
     */
    private void setClosed(boolean closed) {
        this.closed.set(closed);
    }
    
    /**
     * Try to kill immediately all ongoing processes performed by DAOs of this 
     * {@code DAOManager}, and close and release all its resources. 
     * Closing the resources will have the same effects then calling {@link #close()}.
     * If a query was interrupted following a call to this method, the service provider 
     * should throw a {@code QueryInterruptedException} from the thread 
     * that was interrupted. 
     * 
     * @throws DAOException If an error occurred while killing the manager.
     * @see #kill(long)
     */
    public void kill() throws DAOException {
        log.traceEntry();
        if (this.atomicCloseAndRemoveFromPool(true)) {
            //implementation-specific code here
            this.killDAOManager();
        }
        
        log.traceExit();
    }
    
    /**
     * Determine whether this {@code DAOManager} was killed 
     * (following a call to {@link #kill()} or {@link #kill(long)}).
     * This does not necessarily mean that a query was interrupted, only that 
     * the {@code DAOManager} received a {@code kill} command 
     * (if no query was running when receiving the command, none were killed).
     * <p>
     * If a query was actually interrupted, then the service provider 
     * should have thrown a {@code QueryInterruptedException} from the thread 
     * that was interrupted. 
     * <p>
     * If this method returns {@code true}, then a call to {@link #isClosed()} 
     * will also return {@code true}.
     * 
     * @return    {@code true} if this {@code DAOManager} was killed, 
     *             {@code false} otherwise.
     * @see #kill()
     * @see #kill(long)
     */
    public boolean isKilled() {
        log.traceEntry();
        return log.traceExit(this.killed);
    }
    /**
     * Set {@link #killed}. The only method that should call this one besides constructors 
     * is {@link #atomicCloseAndRemoveFromPool(boolean)}. 
     * 
     * @param killed     a {@code boolean} to set {@link #killed}
     */
    private final void setKilled(boolean killed) {
        this.killed = killed;
    }
    
    /**
     * Atomic operation to set {@link #closed} to {@code true}, 
     * {@link #killed} to {@code true} if the parameter is {@code true},
     * and to remove from the managers pool.
     * This method returns {@code true} if the operations were actually performed, 
     * and {@code false} if this {@code DAOManager} was actually 
     * already closed. 
     * 
     * @param killed     To indicate whether this {@code DAOManager} 
     *                     is being closed following a {@link #kill()} command.
     * @return             A {@code boolean} {@code true} if the operations 
     *                     were actually performed, {@code false} if this 
     *                     {@code DAOManager} was already closed. 
     */
    private final boolean atomicCloseAndRemoveFromPool(boolean killed) {
        log.traceEntry("{}", killed);
        synchronized(this.closed) {
            if (!this.isClosed()) {
                this.setClosed(true);
                if (killed) {
                    this.setKilled(true);
                }
                managers.remove(this.getId());
                return log.traceExit(true);
            }
            return log.traceExit(false);
        }
    }
    
    //*****************************************
    //  PUBLIC GET DAO METHODS
    //*****************************************
    /**
     * Method used before acquiring a DAO to check if this {@code DAOManager} 
     * is closed. It throws an {@code IllegalStateException} with proper message 
     * if it is closed. 
     * 
     * @throws IllegalStateException     If this {@code DAOManager} is closed. 
     */
    private void checkClosed() {
        if (this.isClosed()) {
            throw log.throwing(new IllegalStateException(
                "It is not possible to acquire a DAO after the DAOManager has been closed."));
        }
    }
    //XXX: change the names of the DAO getters, to make clear a new one is provided 
    //at each call?
    /**
     * Get a new {@link org.bgee.model.dao.api.source.SourceDAO SourceDAO}, 
     * unless this {@code DAOManager} is already closed. 
     * 
     * @return     a new {@code SourceDAO}.
     * @throws IllegalStateException     If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.source.SourceDAO SourceDAO
     */
    public SourceDAO getSourceDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewSourceDAO());
    }
    /**
     * Get a new {@link org.bgee.model.dao.api.source.SourceToSpeciesDAO SourceToSpeciesDAO}, 
     * unless this {@code DAOManager} is already closed. 
     * 
     * @return  a new {@code SourceToSpeciesDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.source.SourceToSpeciesDAO SourceToSpeciesDAO
     */
    public SourceToSpeciesDAO getSourceToSpeciesDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewSourceToSpeciesDAO());
    }

    /**
     * Get a new {@link org.bgee.model.dao.api.species.SpeciesDAO SpeciesDAO}, 
     * unless this {@code DAOManager} is already closed. 
     * 
     * @return  a new {@code SpeciesDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.species.SpeciesDAO SpeciesDAO
     */
    public SpeciesDAO getSpeciesDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewSpeciesDAO());
    }
    
    /**
     * Get a new {@link org.bgee.model.dao.api.gene.GeneHomologsDOA GeneHomologsDOA}, 
     * unless this {@code DAOManager} is already closed. 
     * 
     * @return  a new {@code GeneHomologsDOA}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.gene.GeneHomologsDOA GeneHomologsDOA
     */
    public GeneHomologsDAO getGeneHomologsDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewGeneHomologsDAO());
    }
    /**
     * Get a new {@link org.bgee.model.dao.api.species.TaxonDAO TaxonDAO}, 
     * unless this {@code DAOManager} is already closed. 
     * 
     * @return  a new {@code TaxonDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.species.TaxonDAO TaxonDAO
     */
    public TaxonDAO getTaxonDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewTaxonDAO());
    }
    /**
     * Get a new {@link org.bgee.model.dao.api.anatdev.TaxonConstraintDAO TaxonConstraintDAO}, 
     * unless this {@code DAOManager} is already closed. 
     * 
     * @return  a new {@code TaxonConstraintDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.anatdev.TaxonConstraintDAO TaxonConstraintDAO
     */
    public TaxonConstraintDAO getTaxonConstraintDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewTaxonConstraintDAO());
    }
    /**
     * Get a new {@link org.bgee.model.dao.api.gene.GeneOntologyDAO GeneOntologyDAO}, 
     * unless this {@code DAOManager} is already closed. 
     * 
     * @return  a new {@code GeneOntologyDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.gene.GeneOntologyDAO GeneOntologyDAO
     */
    public GeneOntologyDAO getGeneOntologyDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewGeneOntologyDAO());
    }
    /**
     * Get a new {@link org.bgee.model.dao.api.gene.GeneDAO GeneDAO}, 
     * unless this {@code DAOManager} is already closed. 
     * 
     * @return  a new {@code GeneDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.gene.GeneDAO GeneDAO
     */
    public GeneDAO getGeneDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewGeneDAO());
    }
    /**
     * Get a new {@link org.bgee.model.dao.api.gene.GeneXRefDAO GeneXRefDAO}, 
     * unless this {@code DAOManager} is already closed. 
     * 
     * @return  a new {@code GeneXRefDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.gene.GeneXRefDAO GeneXRefDAO
     */
    public GeneXRefDAO getGeneXRefDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewGeneXRefDAO());
    }
    /**
     * Get a new {@link org.bgee.model.dao.api.gene.HierarchicalGroupDAO 
     * HierarchicalGroupDAO}, unless this {@code DAOManager} is already closed. 
     * 
     * @return  a new {@code HierarchicalGroupDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.gene.HierarchicalGroupDAO HierarchicalGroupDAO
     */
    public HierarchicalGroupDAO getHierarchicalGroupDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewHierarchicalGroupDAO());
    }
    /**
     * Get a new {@link org.bgee.model.dao.api.anatdev.StageDAO 
     * StageDAO}, unless this {@code DAOManager} is already closed. 
     * 
     * @return  a new {@code StageDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.anatdev.StageDAO StageDAO
     */
    public StageDAO getStageDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewStageDAO());
    }
    /**
     * Get a new {@link org.bgee.model.dao.api.ontologycommon.RelationDAO RelationDAO}, 
     * unless this {@code DAOManager} is already closed. 
     * 
     * @return  a new {@code RelationDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.ontologycommon.RelationDAO RelationDAO
     */
    public RelationDAO getRelationDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewRelationDAO());
    }
    /**
     * Get a new {@link org.bgee.model.dao.api.expressiondata.call.ConditionDAO ConditionDAO}, 
     * unless this {@code DAOManager} is already closed. 
     * 
     * @return  a new {@code ConditionDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.expressiondata.call.ConditionDAO ConditionDAO
     */
    public ConditionDAO getConditionDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewConditionDAO());
    }
    /**
     * Get a new {@link org.bgee.model.dao.api.expressiondata.call.ConditionDAO ConditionDAO}, 
     * unless this {@code DAOManager} is already closed. 
     * 
     * @return  a new {@code RawDataConditionDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO RawDataConditionDAO
     */
    public RawDataConditionDAO getRawDataConditionDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewRawDataConditionDAO());
    }
    /**
     * Get a new {@link org.bgee.model.dao.api.anatdev.SexDAO SexDAO},
     * unless this {@code DAOManager} is already closed.
     *
     * @return  a new {@code SexDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.anatdev.SexDAO SexDAO
     */
    public SexDAO getSexDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewSexDAO());
    }
    /**
     * Get a new {@link org.org.bgee.model.dao.api.expressiondata.rawdata.RawExpressionCallDAO RawExpressionCallDAO}, 
     * unless this {@code DAOManager} is already closed. 
     * 
     * @return  a new {@code RawExpressionCallDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.org.bgee.model.dao.api.expressiondata.rawdata.RawExpressionCallDAO RawExpressionCallDAO
     */
    public RawExpressionCallDAO getRawExpressionCallDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewRawExpressionCallDAO());
    }
    /**
     * Get a new {@link org.bgee.model.dao.api.expressiondata.call.GlobalExpressionCallDAO GlobalExpressionCallDAO}, 
     * unless this {@code DAOManager} is already closed. 
     * 
     * @return  a new {@code GlobalExpressionCallDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.expressiondata.call.GlobalExpressionCallDAO GlobalExpressionCallDAO
     */
    public GlobalExpressionCallDAO getGlobalExpressionCallDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewGlobalExpressionCallDAO());
    }
    
    /**
     * Get a new {@link org.org.bgee.model.dao.api.expressiondata.rawdata.SamplePValueDAO SamplePValueDAO}, 
     * unless this {@code DAOManager} is already closed. 
     * 
     * @return  a new {@code SamplePValueDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.org.bgee.model.dao.api.expressiondata.rawdata.SamplePValueDAO SamplePValueDAO
     */
    public SamplePValueDAO getSamplePValueDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewSamplePValueDAO());
    }

    /**
     * Get a new {@link org.bgee.model.dao.api.expressiondata.call.DiffExpressionCallDAO 
     * DiffExpressionCallDAO}, unless this {@code DAOManager} is already closed. 
     * 
     * @return  a new {@code DiffExpressionCallDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.expressiondata.call.DiffExpressionCallDAO DiffExpressionCallDAO
     */
    public DiffExpressionCallDAO getDiffExpressionCallDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewDiffExpressionCallDAO());
    }
    /**
     * Get a new {@link org.bgee.model.dao.api.anatdev.AnatEntityDAO AnatEntityDAO}, 
     * unless this {@code DAOManager} is already closed. 
     * 
     * @return  a new {@code AnatEntityDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.anatdev.AnatEntityDAO AnatEntityDAO
     */
    public AnatEntityDAO getAnatEntityDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewAnatEntityDAO());
    }
    
    /**
     * Get a new {@link org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixProbesetDAO
     * AffymetrixProbesetDAO}, unless this {@code DAOManager} is already closed. 
     * 
     * @return  a new {@code AffymetrixProbesetDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixProbesetDAO 
     * AffymetrixProbesetDAO
     */
    public AffymetrixProbesetDAO getAffymetrixProbesetDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewAffymetrixProbesetDAO());
    }

    /**
     * Get a new {@link org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO}, 
     * unless this {@code DAOManager} is already closed. 
     * 
     * @return  a new {@code AffymetrixChipDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO 
     */
    public AffymetrixChipDAO getAffymetrixChipDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewAffymetrixChipDAO());
    }
    /**
     * Get a new {@link org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipTypeDAO}, 
     * unless this {@code DAOManager} is already closed. 
     * 
     * @return  a new {@code AffymetrixChipTypeDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipTypeDAO 
     */
    public AffymetrixChipTypeDAO getAffymetrixChipTypeDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewAffymetrixChipTypeDAO());
    }
    /**
     * Get a new {@link org.bgee.model.dao.api.expressiondata.rawdata.microarray.MicroarrayExperimentDAO}, 
     * unless this {@code DAOManager} is already closed. 
     * 
     * @return  a new {@code MicroarrayExperimentDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.expressiondata.rawdata.microarray.MicroarrayExperimentDAO
     */
    public MicroarrayExperimentDAO getMicroarrayExperimentDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewMicroarrayExperimentDAO());
    }

    /**
     * Get a new {@link org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqExperimentDAO}, 
     * unless this {@code DAOManager} is already closed. 
     * 
     * @return  a new {@code RnaSeqExperimentDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RnaSeqExperimentDAO
     */
    public RNASeqExperimentDAO getRnaSeqExperimentDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewRnaSeqExperimentDAO());
    }

    /**
     * Get a new {@link org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqResultAnnotatedSampleDAO}, 
     * unless this {@code DAOManager} is already closed. 
     * 
     * @return  a new {@code RnaSeqResultAnnotatedSampleDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RnaSeqResultAnnotatedSampleDAO
     */
    public RNASeqResultAnnotatedSampleDAO getRnaSeqResultAnnotatedSampleDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewRNASeqResultAnnotatedSampleDAO());
    }

    /**
     * Get a new {@link org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryAnnotatedSampleDAO}, 
     * unless this {@code DAOManager} is already closed. 
     * 
     * @return  a new {@code RnaSeqLibraryAnnotatedSampleDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RnaSeqLibraryAnnotatedSampleDAO
     */
    public RNASeqLibraryAnnotatedSampleDAO getRnaSeqLibraryAnnotatedSampleDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewRnaSeqLibraryAnnotatedSampleDAO());
    }
    /**
     * Get a new {@link org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryDAO}, 
     * unless this {@code DAOManager} is already closed. 
     * 
     * @return  a new {@code RnaSeqLibraryDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RnaSeqLibraryDAO
     */
    public RNASeqLibraryDAO getRnaSeqLibraryDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewRnaSeqLibraryDAO());
    }

    /**
     * Get a new {@link org.bgee.model.dao.api.expressiondata.rawdata.est.ESTLibraryDAO}, 
     * unless this {@code DAOManager} is already closed. 
     * 
     * @return  a new {@code ESTLibraryDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.expressiondata.rawdata.est.ESTLibraryDAO
     */
    public ESTLibraryDAO getESTLibraryDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewESTLibraryDAO());
    }
    /**
     * Get a new {@link org.bgee.model.dao.api.expressiondata.rawdata.est.ESTDAO}, 
     * unless this {@code DAOManager} is already closed. 
     * 
     * @return  a new {@code ESTDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.expressiondata.rawdata.est.ESTDAO
     */
    public ESTDAO getESTDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewESTDAO());
    }

    /**
     * Get a new {@link org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituExperimentDAO 
     * InSituExperimentDAO}, unless this {@code DAOManager} is already closed. 
     * 
     * @return  a new {@code InSituExperimentDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituExperimentDAO InSituExperimentDAO
     */
    public InSituExperimentDAO getInSituExperimentDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewInSituExperimentDAO());
    }

    /**
     * Get a new {@link org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituEvidenceDAO 
     * InSituEvidenceDAO}, unless this {@code DAOManager} is already closed. 
     * 
     * @return  a new {@code InSituEvidenceDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituEvidenceDAO InSituEvidenceDAO
     */
    public InSituEvidenceDAO getInSituEvidenceDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewInSituEvidenceDAO());
    }

    /**
     * Get a new {@link org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituSpotDAO 
     * InSituSpotDAO}, unless this {@code DAOManager} is already closed. 
     * 
     * @return  a new {@code InSituSpotDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituSpotDAO InSituSpotDAO
     */
    public InSituSpotDAO getInSituSpotDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewInSituSpotDAO());
    }

    /**
     * Get a new {@link org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituSpotDAO 
     * InSituSpotDAO}, unless this {@code DAOManager} is already closed. 
     * 
     * @return  a new {@code InSituSpotDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituSpotDAO InSituSpotDAO
     */
    public RawDataCountDAO getRawDataCountDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewRawDataCountDAO());
    }

    /**
     * Get a new {@link org.bgee.model.dao.api.ontologycommon.CIOStatementDAO CIOStatementDAO}, 
     * unless this {@code DAOManager} is already closed. 
     * 
     * @return  a new {@code CIOStatementDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.ontologycommon.CIOStatementDAO CIOStatementDAO 
     */
    public CIOStatementDAO getCIOStatementDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewCIOStatementDAO());
    }

    /**
     * Get a new {@link org.bgee.model.dao.api.ontologycommon.EvidenceOntologyDAO EvidenceOntologyDAO}, 
     * unless this {@code DAOManager} is already closed. 
     * 
     * @return  a new {@code EvidenceOntologyDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.ontologycommon.EvidenceOntologyDAO EvidenceOntologyDAO 
     */
    public EvidenceOntologyDAO getEvidenceOntologyDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewEvidenceOntologyDAO());
    }

    /**
     * Get a new {@link org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO 
     * SummarySimilarityAnnotationDAO}, unless this {@code DAOManager} is already closed. 
     * 
     * @return  A new {@code SummarySimilarityAnnotationDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO SummarySimilarityAnnotationDAO 
     */
    public SummarySimilarityAnnotationDAO getSummarySimilarityAnnotationDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewSummarySimilarityAnnotationDAO());
    }

    /**
     * Get a new {@link org.bgee.model.dao.api.anatdev.mapping.RawSimilarityAnnotationDAO 
     * RawSimilarityAnnotationDAO}, unless this {@code DAOManager} is already closed. 
     * 
     * @return  A new {@code RawSimilarityAnnotationDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.anatdev.mapping.RawSimilarityAnnotationDAO RawSimilarityAnnotationDAO 
     */
    public RawSimilarityAnnotationDAO getRawSimilarityAnnotationDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewRawSimilarityAnnotationDAO());
    }

    /**
     * Get a new {@link org.bgee.model.dao.api.anatdev.mapping.StageGroupingDAO StageGroupingDAO}, 
     * unless this {@code DAOManager} is already closed. 
     * 
     * @return  A new {@code StageGroupingDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see org.bgee.model.dao.api.anatdev.mapping.StageGroupingDAO StageGroupingDAO 
     */
    public StageGroupingDAO getStageGroupingDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewStageGroupingDAO());
    }

    /**
     * Get a new {@link DownloadFileDAO},
     * unless this {@code DAOManager} is already closed.
     *
     * @return  A new {@code DownloadFileDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see DownloadFileDAO
     */
    public DownloadFileDAO getDownloadFileDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewDownloadFileDAO());
    }

    /**
     * Get a new {@link SpeciesDataGroupDAO},
     * unless this {@code DAOManager} is already closed.
     *
     * @return  A new {@code SpeciesDataGroupDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see SpeciesDataGroupDAO
     */
    public SpeciesDataGroupDAO getSpeciesDataGroupDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewSpeciesDataGroupDAO());
    }
    
    /**
     * Get a new {@link KeywordDAO},
     * unless this {@code DAOManager} is already closed.
     *
     * @return  A new {@code KeywordDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see KeywordDAO
     */
    public KeywordDAO getKeywordDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewKeywordDAO());
    }
    
    /**
     * Get a new {@link GeneNameSynonymDAO}, unless this {@code DAOManager} is already closed.
     *
     * @return  A new {@code GeneNameSynonymDAO}.
     * @throws IllegalStateException    If this {@code DAOManager} is already closed.
     * @see GeneNameSynonymDAO
     */
    public GeneNameSynonymDAO getGeneNameSynonymDAO() {
        log.traceEntry();
        this.checkClosed();
        return log.traceExit(this.getNewGeneNameSynonymDAO());
    }
    
    //*****************************************
    //  CORE ABSTRACT METHODS TO IMPLEMENT
    //*****************************************    
    
    /**
     * Release all underlying resources used by this {@code DAOManager}. 
     * This {@code DAOManager} will not be closed and can still be used. An example 
     * of such a release process could be the closing of a JDBC connection,  
     * hold by a {@code DAOManager} making use of a SQL database. To close 
     * this {@code DAOManager} instead, see {@link #close()}.
     * <p>
     * The difference between {@code close} and {@code releaseResources} is useful 
     * when it is impractical to acquire a new {@code DAOManager} after closing it.
     * 
     * @throws DAOException If an error occurred while releasing resources.
     */
    public abstract void releaseResources() throws DAOException;
    
    /**
     * Release resources and close this {@code DAOManager}, so that no {@code DAO}s 
     * could be obtained from it anymore. Resources are released 
     * by calling {@link #releaseResources()}.
     * <p>
     * This method is called by {@link #close()} after having remove this 
     * {@code DAOManager} from the pool. 
     * 
     * @throws DAOException If an error occurred while closing the manager.
     */
    protected abstract void closeDAOManager() throws DAOException;
    
    /**
     * Service providers must implement in this method the operations necessary 
     * to immediately kill all ongoing processes handled by this {@code DAOManager}, 
     * and release and close all resources it hold. 
     * It should not affect other {@code DAOManager}s.
     * <p>
     * If a query was interrupted following a call to this method, the service provider 
     * should make sure that a {@code QueryInterruptedException} will be thrown 
     * from the thread that was interrupted. To help determining 
     * if the method {@code kill} was called, service provider can use 
     * {@link #isKilled()}. If no query was running, then nothing should happen.
     * <p>
     * For instance, if a service provider uses the JDBC API to use a SQL database,
     * the {@code DAOManager} should keep track of which {@code Statement} 
     * is currently running, in order to be able to call {@code cancel} on it.
     * The thread using the {@code Statement} should have checked {@code isKilled} 
     * before calling {@code execute</cod>, and after returning from <code>execute}, 
     * should immediately check {@link #isKilled()} to determine if it was interrupted, 
     * and throw a {@code QueryInterruptedException} if it was the case. 
     * <p>
     * This method is called by {@link #kill()} after having remove this 
     * {@code DAOManager} from the pool. Note that {@link #closeDAOManager()} 
     * is not called, it is up to the implementation to do it when needed, 
     * because it might require some atomic operations, but this {@code DAOManager} 
     * <strong>must</strong> be closed following a call to this method, 
     * as if {@link #closeDAOManager()} had been called. 
     * 
     * @throws DAOException If an error occurred while killing the manager.
     */
    protected abstract void killDAOManager() throws DAOException;
    
    /**
     * Service providers must implement in this method all the operations necessary 
     * to close the application. For instance, if the service provider use the JDBC API, 
     * then it should deregister all {@code Driver}s used, when this method is called.
     * Note that the method {@code close} would have already been called on each individual 
     * {@code DAOManager}s, before this method is called (so it is not necessary to 
     * close {@code Connection}s, for instance). 
     * <p>
     * This method should definitely be static, but it is not possible to declare 
     * an abstract method static in Java, damn it. This method will then be called 
     * <strong>only on one</strong> {@code DAOManager} instance, for each 
     * service provider.
     * 
     * @throws DAOException If an error occurred while closing the service.
     */
    protected abstract void shutdown() throws DAOException;
    
    /**
     * Set the parameters of this {@code DAOManager}. For instance, 
     * if this {@code DAOManager} was obtained from a Service provider using 
     * the JDBC API to use a SQL database, then the provided properties might 
     * contain a key/value pair defining the {@code URI} to connect to the database. 
     * It is up to each Service provider to specify what are the parameters needed. 
     * <p>
     * This method throws an {@code IllegalArgumentException} if 
     * the {@code DAOManager} does not accept these parameters, meaning that it could 
     * not find its mandatory parameters in it. This is the method used to find 
     * an appropriate Service provider when calling {@link #getDAOManager(Properties)}.
     * 
     * @param props     A {@code Properties} object containing parameter names 
     *                  and values to be used by this {@code DAOManager}. 
     * @throws IllegalArgumentException     If this {@code DAOManager} does not accept 
     *                                         {@code props}. 
     */
    public void setParameters(Properties props) throws IllegalArgumentException {
        log.traceEntry("{}", props);
        //enforce immutable properties
        this.parameters = props;
        log.traceExit();
    }
    
    /**
     * @return  The {@code Properties} that were used to obtain this {@code DAOManager}.
     */
    public Properties getParameters() {
        return parameters;
    }
    

    //*****************************************
    //  METHODS TO OBTAIN DAOs TO IMPLEMENT
    //***************************************** 
    /**
     * Service provider must return a new 
     * {@link org.bgee.model.dao.api.source.SourceDAO SourceDAO} instance 
     * when this method is called. 
     * 
     * @return  A new {@code SourceDAO}
     */
    protected abstract SourceDAO getNewSourceDAO();
    /**
     * Service provider must return a new 
     * {@link org.bgee.model.dao.api.source.SourceToSpeciesDAO SourceToSpeciesDAO} instance 
     * when this method is called. 
     * 
     * @return  A new {@code SourceToSpeciesDAO}
     */
    protected abstract SourceToSpeciesDAO getNewSourceToSpeciesDAO();
    /**
     * Service provider must return a new 
     * {@link org.bgee.model.dao.api.species.SpeciesDAO SpeciesDAO} instance 
     * when this method is called. 
     * 
     * @return  A new {@code SpeciesDAO}
     */
    protected abstract SpeciesDAO getNewSpeciesDAO();
    /**
     * Service provider must return a new 
     * {@link org.bgee.model.dao.api.gene.GeneHomologsDAO GeneHomologsDAO} instance 
     * when this method is called. 
     * 
     * @return  A new {@code GeneHomologsDAO}
     */
    protected abstract GeneHomologsDAO getNewGeneHomologsDAO();
    /**
     * Service provider must return a new 
     * {@link org.bgee.model.dao.api.species.TaxonDAO TaxonDAO} instance 
     * when this method is called. 
     * 
     * @return  A new {@code TaxonDAO}
     */
    protected abstract TaxonDAO getNewTaxonDAO();
    /**
     * Service provider must return a new 
     * {@link org.bgee.model.dao.api.anatdev.TaxonConstraintDAO TaxonConstraintDAO} instance 
     * when this method is called. 
     * 
     * @return  A new {@code TaxonConstraintDAO}
     */
    protected abstract TaxonConstraintDAO getNewTaxonConstraintDAO();
    /**
     * Service provider must return a new 
     * {@link org.bgee.model.dao.api.gene.GeneOntologyDAO GeneOntologyDAO} instance 
     * when this method is called. 
     * 
     * @return  A new {@code GeneOntologyDAO}
     */
    protected abstract GeneOntologyDAO getNewGeneOntologyDAO();
    /**
     * Service provider must return a new 
     * {@link org.bgee.model.dao.api.gene.GeneDAO GeneDAO} instance 
     * when this method is called. 
     * 
     * @return  A new {@code GeneDAO}
     */
    protected abstract GeneDAO getNewGeneDAO();
    /**
     * Service provider must return a new 
     * {@link org.bgee.model.dao.api.gene.GeneXRefDAO GeneXRefDAO} instance 
     * when this method is called. 
     * 
     * @return  A new {@code GeneXRefDAO}
     */
    protected abstract GeneXRefDAO getNewGeneXRefDAO();
    /**
     * Service provider must return a new 
     * {@link org.bgee.model.dao.api.gene.HierarchicalGroupDAO 
     * HierarchicalGroupDAO} instance when this method is called. 
     * 
     * @return  A new {@code HierarchicalGroupDAO}
     */
    protected abstract HierarchicalGroupDAO getNewHierarchicalGroupDAO();
    /**
     * Service provider must return a new 
     * {@link org.bgee.model.dao.api.anatdev.StageDAO 
     * StageDAO} instance when this method is called. 
     * 
     * @return  A new {@code StageDAO}
     */
    protected abstract StageDAO getNewStageDAO();
    /**
     * Service provider must return a new {@link org.bgee.model.dao.api.ontologycommon.RelationDAO 
     * RelationDAO} instance when this method is called. 
     * 
     * @return  A new {@code RelationDAO}
     */
    protected abstract RelationDAO getNewRelationDAO();
    /**
     * Service provider must return a new {@link org.bgee.model.dao.api.expressiondata.call.ConditionDAO 
     * ConditionDAO} instance when this method is called. 
     * 
     * @return  A new {@code ConditionDAO}
     */
    protected abstract ConditionDAO getNewConditionDAO();
    /**
     * Service provider must return a new {@link org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO 
     * RawDataConditionDAO} instance when this method is called. 
     * 
     * @return  A new {@code RawDataConditionDAO}
     */
    protected abstract RawDataConditionDAO getNewRawDataConditionDAO();
    /**
     * Service provider must return a new {@link org.bgee.model.dao.api.anatdev.SexDAO
     * SexDAO} instance when this method is called.
     *
     * @return  A new {@code SexDAO}
     */
    protected abstract SexDAO getNewSexDAO();
    /**
     * Service provider must return a new 
     * {@link org.org.bgee.model.dao.api.expressiondata.rawdata.RawExpressionCallDAO RawExpressionCallDAO} 
     * instance when this method is called. 
     * 
     * @return  A new {@code RawExpressionCallDAO}
     */
    protected abstract RawExpressionCallDAO getNewRawExpressionCallDAO();
    /**
     * Service provider must return a new 
     * {@link org.bgee.model.dao.api.expressiondata.call.GlobalExpressionCallDAO GlobalExpressionCallDAO} 
     * instance when this method is called. 
     * 
     * @return  A new {@code GlobalExpressionCallDAO}
     */
    protected abstract GlobalExpressionCallDAO getNewGlobalExpressionCallDAO();
    /**
     * Service provider must return a new 
     * {@link org.org.bgee.model.dao.api.expressiondata.rawdata.SamplePValueDAO SamplePValueDAO} 
     * instance when this method is called. 
     * 
     * @return  A new {@code SamplePValueDAO}
     */
    protected abstract SamplePValueDAO getNewSamplePValueDAO();
    /**
     * Service provider must return a new 
     * {@link org.bgee.model.dao.api.expressiondata.call.DiffExpressionCallDAO DiffExpressionCallDAO} 
     * instance when this method is called. 
     * 
     * @return  A new {@code DiffExpressionCallDAO}
     */
    protected abstract DiffExpressionCallDAO getNewDiffExpressionCallDAO();
    /**
     * Service provider must return a new 
     * {@link org.bgee.model.dao.api.anatdev.AnatEntityDAO AnatEntityDAO} instance 
     * when this method is called. 
     * 
     * @return  A new {@code AnatEntityDAO}
     */
    protected abstract AnatEntityDAO getNewAnatEntityDAO();
    /**
     * Service provider must return a new 
     * {@link org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixProbesetDAO 
     * AffymetrixProbesetDAO} instance when this method is called. 
     * 
     * @return  A new {@code AffymetrixProbesetDAO}
     */
    protected abstract AffymetrixProbesetDAO getNewAffymetrixProbesetDAO();
    /**
     * Service provider must return a new 
     * {@link org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO 
     * AffymetrixChipDAO} instance when this method is called. 
     * 
     * @return  A new {@code AffymetrixChipDAO}
     */
    protected abstract AffymetrixChipDAO getNewAffymetrixChipDAO();
    /**
     * Service provider must return a new 
     * {@link org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipTypeDAO 
     * AffymetrixChipTypeDAO} instance when this method is called. 
     * 
     * @return  A new {@code AffymetrixChipTypeDAO}
     */
    protected abstract AffymetrixChipTypeDAO getNewAffymetrixChipTypeDAO();
    /**
     * Service provider must return a new 
     * {@link org.bgee.model.dao.api.expressiondata.rawdata.microarray.MicroarrayExperimentDAO 
     * MicroarrayExperimentDAO} instance when this method is called. 
     * 
     * @return  A new {@code MicroarrayExperimentDAO}
     */
    protected abstract MicroarrayExperimentDAO getNewMicroarrayExperimentDAO();
    /**
     * Service provider must return a new 
     * {@link org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RnaSeqExperimentDAO 
     * RnaSeqExperimentDAO} instance when this method is called. 
     * 
     * @return  A new {@code RnaSeqExperimentDAO}
     */
    protected abstract RNASeqExperimentDAO getNewRnaSeqExperimentDAO();
    /**
     * Service provider must return a new 
     * {@link org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RnaSeqLibraryAnnotatedSampleDAO 
     * RnaSeqLibraryAnnotatedSampleDAO} instance when this method is called. 
     * 
     * @return  A new {@code RnaSeqLibraryAnnotatedSampleDAO}
     */
    protected abstract RNASeqLibraryAnnotatedSampleDAO getNewRnaSeqLibraryAnnotatedSampleDAO();
    /**
     * Service provider must return a new 
     * {@link org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RnaSeqLibraryDAO 
     * RnaSeqLibraryDAO} instance when this method is called. 
     * 
     * @return  A new {@code RnaSeqLibraryDAO}
     */
    protected abstract RNASeqLibraryDAO getNewRnaSeqLibraryDAO();
    /**
     * Service provider must return a new 
     * {@link org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqResultAnnotatedSampleDAO RNASeqResultDAO}
     * instance when this method is called. 
     * 
     * @return  A new {@code RNASeqResultDAO}
     */
    protected abstract RNASeqResultAnnotatedSampleDAO getNewRNASeqResultAnnotatedSampleDAO();
    /**
     * Service provider must return a new 
     * {@link org.bgee.model.dao.api.expressiondata.rawdata.est.ESTLibraryDAO 
     * ESTLibraryDAO} instance when this method is called. 
     * 
     * @return  A new {@code ESTLibraryDAO}
     */
    protected abstract ESTLibraryDAO getNewESTLibraryDAO();
    /**
     * Service provider must return a new 
     * {@link org.bgee.model.dao.api.expressiondata.rawdata.est.ESTDAO 
     * ESTDAO} instance when this method is called. 
     * 
     * @return  A new {@code ESTDAO}
     */
    protected abstract ESTDAO getNewESTDAO();

    /**
     * Service provider must return a new 
     * {@link org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituExperimentDAO InSituExperimentDAO} 
     * instance when this method is called. 
     * 
     * @return  A new {@code InSituExperimentDAO}
     */
    protected abstract InSituExperimentDAO getNewInSituExperimentDAO();

    /**
     * Service provider must return a new 
     * {@link org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituEvidenceDAO InSituEvidenceDAO} 
     * instance when this method is called. 
     * 
     * @return  A new {@code InSituEvidenceDAO}
     */
    protected abstract InSituEvidenceDAO getNewInSituEvidenceDAO();

    /**
     * Service provider must return a new 
     * {@link org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituSpotDAO InSituSpotDAO} 
     * instance when this method is called. 
     * 
     * @return  A new {@code InSituSpotDAO}
     */
    protected abstract InSituSpotDAO getNewInSituSpotDAO();
    /**
     * Service provider must return a new 
     * {@link org.bgee.model.dao.api.expressiondata.rawdata.RawDataCountDAO RawDataCountDAO} 
     * instance when this method is called. 
     * 
     * @return  A new {@code RawDataCountDAO}
     */
    protected abstract RawDataCountDAO getNewRawDataCountDAO();
    /**
     * Service provider must return a new 
     * {@link org.bgee.model.dao.api.ontologycommon.CIOStatementDAO CIOStatementDAO}
     * instance when this method is called. 
     * 
     * @return  A new {@code CIOStatementDAO}
     */
    protected abstract CIOStatementDAO getNewCIOStatementDAO();
    /**
     * Service provider must return a new 
     * {@link org.bgee.model.dao.api.ontologycommon.EvidenceOntologyDAO EvidenceOntologyDAO}
     * instance when this method is called. 
     * 
     * @return  A new {@code EvidenceOntologyDAO}
     */
    protected abstract EvidenceOntologyDAO getNewEvidenceOntologyDAO();
    /**
     * Service provider must return a new 
     * {@link org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO 
     * SummarySimilarityAnnotationDAO} instance when this method is called. 
     * 
     * @return  A new {@code SummarySimilarityAnnotationDAO}
     */
    protected abstract SummarySimilarityAnnotationDAO getNewSummarySimilarityAnnotationDAO();
    /**
     * Service provider must return a new 
     * {@link org.bgee.model.dao.api.anatdev.mapping.RawSimilarityAnnotationDAO 
     * RawSimilarityAnnotationDAO} instance when this method is called. 
     * 
     * @return  A new {@code RawSimilarityAnnotationDAO}
     */
    protected abstract RawSimilarityAnnotationDAO getNewRawSimilarityAnnotationDAO();
    /**
     * Service provider must return a new 
     * {@link org.bgee.model.dao.api.anatdev.mapping.StageGroupingDAO StageGroupingDAO} instance 
     * when this method is called. 
     * 
     * @return  A new {@code StageGroupingDAO}
     */
    protected abstract StageGroupingDAO getNewStageGroupingDAO();

    /**
     * Service provider must return a new {@link DownloadFileDAO} instance when this method is called
     * @return A new {@link DownloadFileDAO}
     */
    protected abstract DownloadFileDAO getNewDownloadFileDAO();

    /**
     * Service provider must return a new {@link SpeciesDataGroupDAO} instance when this method is called
     * @return A new {@link SpeciesDataGroupDAO}
     */
    protected abstract SpeciesDataGroupDAO getNewSpeciesDataGroupDAO();

    /**
     * Service provider must return a new {@link KeywordDAO} instance when this method is called
     * @return A new {@link KeywordDAO}
     */
    protected abstract KeywordDAO getNewKeywordDAO();
    
    /**
     * Service provider must return a new {@link GeneNameSynonymDAO} instance when this method is called
     * @return A new {@link GeneNameSynonymDAO}
     */
    protected abstract GeneNameSynonymDAO getNewGeneNameSynonymDAO();
}
