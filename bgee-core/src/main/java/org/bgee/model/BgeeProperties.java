package org.bgee.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.StringUtils;
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
 * The properties are: 
 * <ul>
 * <li>{@code bgee.jdbc.driver}: name of the JDBC {@code Driver} 
 * used to obtain connections. Optional.
 * <li>{@code bgee.jdbc.url}: JDBC URL to use to connect to the data source.
 * Mandatory.
 * <li>{@code bgee.jdbc.username}: default username to connect to the data source.
 * Mandatory.
 * <li>{@code bgee.jdbc.password}: default password to connect to the data source.
 * Mandatory.
 * <li>{@code bgee.jdbc.pool.DataSource.resourceName}: A {@code String}
 * containing the name of the resource to look up to get the dataSource. Optional.
 * <li>{@code bgee.jdbc.preparedStatementPoolSize}: An {@code int} containing
 * the maximum size allowed for a {@code PreparedStatement} Pool. Optional.
 * <li>{@code bgee.jdbc.preparedStatementPoolsMaxTotalSize}: An {@code int}
 * containing the maximum cumulated size allowed for all {@code PreparedStatement}
 *  Pools. Optional.
 * <li>{@code bgee.static.factories}: define whether static factories 
 * should be used when available.
 * <li>{@code bgee.properties.file}: path to the properties file to use, 
 * from the classpath. Optional.
 * </ul>
 * <p>
 * These properties are read from the properties file 
 * or the System properties only once at class loading. Modifying these properties 
 * after having made any call to this class would have no effect. 
 * To modify a property locally, a {@code Thread} must acquire an instance 
 * of this class (by calling the static method {@link #getBgeeProperties()}), 
 * and modify its instance attributes: the instance attributes are initialized 
 * at instantiation using the class attributes obtained from the {@code Properties}. 
 * These instance attributes can then be modified, but it will have no effect 
 * on the overall configuration, the modifications will only be seen inside 
 * the related {@code Thread}. 
 * <p>
 * Any call, <b>inside a thread</b>, to the method {@link #getBgeeProperties()}, 
 * will always return the same {@code BgeeProperties} instance ("per-thread singleton"). 
 * So any modification made to this {@code BgeeProperties} instance 
 * will affect the whole thread  (for instance, modifying in a thread the username 
 * to use to connect to a database at one place, will affect all consecutive attempts 
 * to connect to the database in this thread).
 * <p>
 * An exception is if you call this method 
 * after having called {@link #release()} or {@link #releaseAll()}.
 * In that case, {@code getBgeeProperties()} 
 * will return a new {@code BgeeProperties} instance, with the attributes re-initialized 
 * using the System or file properties.
 * <p>
 * You should always call {@code BgeeProperties.getBgeeProperties().release()} 
 * at the end of the execution of a thread, 
 * or {@link #releaseAll()} in multi-threads context (for instance, in a webapp context 
 * when the webapp is shutdown). 
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
    //*********************************
    // CLASS ATTRIBUTES
    //*********************************
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(BgeeProperties.class.getName());

    /**
     * A {@code ConcurrentMap} used to store all {@code BgeeProperties} instances, 
     * associated with the {@code Thread} object that requested it. 
     * <p>
     * This {@code Map} is used to provide a unique and independent 
     * {@code BgeeProperties} instance to each thread: 
     * a {@code BgeeProperties} is added to this {@code Map} 
     * when {@link #getBgeeProperties()} is called, if the thread is not already 
     * present in the {@code keySet} of the {@code Map}. 
     * Otherwise, the already stored {@code BgeeProperties} is returned. 
     * <p>
     * If a {@code ThreadLocal} was not used, it is because 
     * this {@code Map} is used by other treads, 
     * for instance when a {@code ShutdownListener} 
     * want to properly remove all {@code BgeeProperties}; 
     * or when a thread performing monitoring of another thread want to modify a property.
     * <p>
     * A {@code BgeeProperties} is removed from this {@code Map} for a thread
     * when the method {@link #release()} is called from this thread. 
     * All {@code BgeeProperties} are removed when {@link #releaseAll()} is called.
     */
    private static final ConcurrentMap<Thread, BgeeProperties> bgeeProperties = 
            new ConcurrentHashMap<Thread, BgeeProperties>();
    /**
     * An {@code AtomicBoolean} to define if {@code BgeeProperties}s 
     * can still be acquired (using {@link #getBgeeProperties()}), 
     * or if it is not possible anymore (meaning that the method {@link #releaseAll()} 
     * has been called)
     */
    private static final AtomicBoolean bgeePropertiesClosed = new AtomicBoolean();

    /**
     * A {@code String} corresponding to the name of the JDBC {@code Driver} 
     * used to obtain connections. 
     * Examples are {@code com.mysql.jdbc.Driver}, 
     * or {@code org.bgee.easycache4jdbc.sql.jdbcapi.Driver}. 
     * <p>
     * IMPORTANT: this property does not need to be provided. It is useful only 
     * for some buggy JDBC {@code Driver}s, that fail to register themselves to the 
     * {@code DriverManager}. In that case, it is needed to explicitly load 
     * the {@code Driver}, using this class name.  
     * <p>
     * Corresponds to the property {@code bgee.jdbc.driver}. 
     * If a {@code DataSource} was set (using JNDI), then this property is not used. 
     */
    private static final String jdbcDriver;
    /**
     * A {@code String} representing a database url of the form 
     * {@code jdbc:subprotocol:subname}, to connect to the database 
     * using the {@code DriverManager}. 
     * An example is {@code jdbc:log4jdbc:mysql://127.0.0.1:3306/bgee_v12} 
     * <p>
     * IMPORTANT: Do NOT provide the username and password you want to use 
     * by default in the URL. 
     * You must set the properties {@link #jdbcUsername} and {@link #jdbcPassword} 
     * to provide the default username and password. You will still be able to call 
     * a method {@code getConnection(String, String)} to provide different 
     * username and password.
     * <p>
     * Corresponds to the property {@code bgee.jdbc.url}. 
     * If a DataSource was set (using JNDI), then this property is not used.
     */
    private static final String jdbcUrl;
    /**
     * A {@code String} representing the default username to use to connect 
     * to the database using {@code jdbcUrl}. 
     * <p>
     * Corresponds to the property {@code bgee.jdbc.username}.
     * If a DataSource was set (using JNDI), then this property is not used.
     */
    private static final String jdbcUsername;
    /**
     * A {@code String} representing the default password to use to connect 
     * to the database using {@code jdbcUrl}. 
     * <p>
     * Corresponds to the property {@code bgee.jdbc.password}.
     * If a DataSource was set (using JNDI), then this property is not used.
     */
    private static final String jdbcPassword;
    /**
     * A {@code String} containing the name of the resource to look up
     * to get the dataSource
     * <p>
     * Default value is {@code java:comp/env/jdbc/bgeedatasource}
     * 
     * */
    private static final String dataSourceResourceName;
    /**
     * An {@code int} containing the maximum size 
     * allowed for a single {@code PreparedStatement} pool
     * <p>
     * Default value is 1000
     * 
     * @see org.bgee.model.data.sql.BgeeConnection
     * 
     * */
    private static final int prepStatPoolMaxSize;  
    /**
     * An {@code int} containing the maximum cumulated size 
     * allowed for a all {@code PreparedStatement} pools
     * in the application
     * <p>
     * Default value is 10000
     * 
     * @see org.bgee.model.data.sql.BgeeConnection
     * 
     * */
    private static final int prepStatPoolsMaxTotalSize;  
    /**
     * A {@code boolean} defining whether static factories should be used when available.
     * See {@link org.bgee.model.EntityFactoryProvider EntityFactoryProvider} for more details.
     * <p>
     * If {@code true}, static factories will be used when available. 
     * Corresponds to the property {@code bgee.static.factories}. 
     * Default value is {@code false}.
     * <p>
     * Note that this property cannot be modified locally in a given thread 
     * (no corresponding instance variable), 
     * as it increases the memory load when used, thus impacting all threads.
     * 
     * @see org.bgee.model.EntityFactoryProvider
     */
    private static final boolean useStaticFactories;

    //*********************************
    // INSTANCE ATTRIBUTES
    //*********************************
    /**
     * A {@code String} corresponding to the name of the JDBC {@code Driver} 
     * used to obtain connections. Initialized at instantiation from 
     * {@link #jdbcDriver}.
     * @see #jdbcDriver
     */
    private String localJdbcDriver;
    /**
     * A {@code String} representing a database url of the form 
     * {@code jdbc:subprotocol:subname}, to connect to the database 
     * using the {@code DriverManager}. Initialized at instantiation 
     * from {@link #jdbcUrl};
     * @see #jdbcUrl;
     */
    private String localJdbcUrl;
    /**
     * A {@code String} representing the default username to use to connect 
     * to the database using the JDBC URL. Initialized at instantiation 
     * from {@link #jdbcUsername};
     * @see #jdbcUsername;
     */
    private String localJdbcUsername;
    /**
     * A {@code String} representing the default password to use to connect 
     * to the database using the JDBC URL. Initialized at instantiation 
     * from {@link #jdbcPassword};
     * @see #jdbcPassword;
     */
    private String localJdbcPassword;
    /**
     * A {@code String} containing the name of the resource to look up
     * to get the dataSource. Initialized at instantiation 
     * from {@link #dataSourceResourceName}
     * @see #dataSourceResourceName
     */
    private String localDataSourceResourceName;
    /**
     * An {@code int} containing the maximum size 
     * allowed for a {@code PreparedStatement} Pool. Initialized at instantiation 
     * from {@link #prepStatPoolMaxSize}
     * @see #prepStatPoolMaxSize
     */
    private int localPrepStatPoolMaxSize;
    /**
     * An {@code int} containing the maximum cumulated size 
     * allowed for a all {@code PreparedStatement} pools in the application
     * Initialized at instantiation
     * from {@link #prepStatPoolsMaxTotalSize}
     * @see #prepStatPoolsMaxTotalSize
     * 
     * */
    private int localPrepStatPoolsMaxTotalSize;


    //*********************************
    // CLASS METHODS
    //*********************************

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
        log.info("Bgee properties initialization...");

        bgeePropertiesClosed.set(false);

        java.util.Properties sysProps = new java.util.Properties(System.getProperties());
        //try to get the properties file.
        //default name is bgee.properties
        //check first if an alternative name has been provided in the System properties
        String propertyFile = sysProps.getProperty("bgee.properties.file", 
                "/bgee.properties");
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

        jdbcDriver   = getStringOption(sysProps, fileProps, "bgee.jdbc.driver", null);
        jdbcUrl      = getStringOption(sysProps, fileProps, "bgee.jdbc.url", null);
        jdbcUsername = getStringOption(sysProps, fileProps, "bgee.jdbc.username", null);
        jdbcPassword = getStringOption(sysProps, fileProps, "bgee.jdbc.password", null);
        useStaticFactories = getBooleanOption(sysProps, fileProps, "bgee.static.factories", 
                false);
        dataSourceResourceName   = getStringOption(sysProps, fileProps, 
                "bgee.jdbc.pool.DataSource.resourceName","java:comp/env/jdbc/bgeedatasource");
        
        prepStatPoolMaxSize = Integer.valueOf(getStringOption(sysProps,
                fileProps, "bgee.jdbc.preparedStatementPoolMaxSize","1000"));
                
        prepStatPoolsMaxTotalSize = getIntegerOption(sysProps,
                fileProps, "bgee.jdbc.preparedStatementPoolsMaxTotalSize",10000);
        
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
     * Return a {@code BgeeProperties} object. At the first call of this method 
     * inside a given thread, a new {@code BgeeProperties} will be instantiated 
     * and returned. Then all subsequent calls to this method inside the same thread 
     * will return the same {@code BgeeProperties} object. 
     * <p>
     * This is to ensure that each thread uses one and only one 
     * {@code BgeeProperties} instance, 
     * independent from other threads ("per-thread singleton").
     * <p>
     * An exception is if you call this method from a thread
     * after having called {@link #release()} from this thread.
     * In that case, this method would return a new {@code BgeeProperties} instance 
     * when called from this thread, 
     * with attributes re-initialized using the {@code Properties} from 
     * the properties file or the System properties. 
     * <p>
     * Note that after having called {@link #releaseAll()}, no {@code BgeeProperties} 
     * can be obtained anymore. This method will throw an {@code IllegalStateException} 
     * if {@code releaseAll()} has been previously called.
     *  
     * @return	A {@code BgeeProperties} object, instantiated at the first call 
     * 			of this method. Subsequent calls in any given thread will return 
     * 			the same object. 
     * @throws IllegalStateException If no {@code BgeeProperties} could be obtained anymore. 
     */
    public static BgeeProperties getBgeeProperties() throws IllegalStateException
    {
        log.entry();

        Thread currentThread = Thread.currentThread();
        log.debug("Trying to obtain a BgeeProperties instance for Thread {}", 
                currentThread.getId());

        if (bgeePropertiesClosed.get()) {
            throw new IllegalStateException("releaseAll() has been already called, " +
                    "it is not possible to acquire a BgeeProperties anymore");
        }

        BgeeProperties props = bgeeProperties.get(currentThread);
        if (props == null) {
            //instantiate the BgeeProperties only if needed
            props = new BgeeProperties();
            //we don't use putifAbsent, as the thread as key make sure 
            //there won't be any multi-threading key collision
            bgeeProperties.put(currentThread, props);
            log.debug("Return a new BgeeProperties instance");
        } else {
            log.debug("Return an already existing BgeeProperties instance");
        }
        return log.exit(props);
    }

    /**
     * Release all {@code BgeeProperties}s currently registered 
     * and prevent any new 
     * {@code BgeeProperties} to be obtained again (calling {@link #getBgeeProperties()} 
     * after having called this method will throw a {@code IllegalStateException}). 
     * <p>
     * This method returns the number of {@code BgeeProperties}s that were released. 
     * <p>
     * This method is called for instance when a {@code ShutdownListener} 
     * want to release all {@code BgeeProperties}s.
     * 
     * @return 	An {@code int} that is the number of {@code BgeeProperties}s 
     * 			that were released
     */
    public static int releaseAll()
    {
        log.entry();

        //this AtomicBoolean will act more or less like a lock 
        //(no new BgeeProperties can be obtained after this AtomicBoolean is set to true).
        //It's not totally true, but we don't except any major error if it doesn't act like a lock.
        bgeePropertiesClosed.set(true);

        int propCount = bgeeProperties.size();
        bgeeProperties.clear();

        return log.exit(propCount);
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
    private BgeeProperties()
    {
        this.setJdbcDriver(jdbcDriver);
        this.setJdbcUrl(jdbcUrl);
        this.setJdbcUsername(jdbcUsername);
        this.setJdbcPassword(jdbcPassword);
        this.setDataSourceResourceName(dataSourceResourceName);
        this.setPrepStatPoolMaxSize(prepStatPoolMaxSize);
        this.setPrepStatPoolsMaxTotalSize(prepStatPoolsMaxTotalSize);
        
    }

    /**
     * Releases this {@code BgeeProperties}. 
     * A call to {@link #getBgeeProperties()} from the thread that was holding it 
     * will return a new {@code BgeeProperties} instance. 
     * 
     * @return 	{@code true} if this {@code BgeeProperties} was released, 
     * 			{@code false} if it was already released.
     */
    public boolean release()
    {
        log.entry();
        return log.exit(
                bgeeProperties.values().remove(this));
    }

    /**
     * Determines whether this {@code BgeeProperties} was released 
     * (following a call to {@link #release()}).
     * 
     * @return	{@code true} if this {@code BgeeProperties} was released, 
     * 			{@code false} otherwise.
     */
    public boolean isReleased()
    {
        log.entry();
        return log.exit(
                !bgeeProperties.containsValue(this));
    }

    /**
     * Returns the name of the JDBC {@code Driver} to use 
     * to connect to the data source.
     * @return 	a {@code String} corresponding to the name 
     * 			of the JDBC {@code Driver}
     */
    public String getJdbcDriver() {
        return this.localJdbcDriver;
    }
    /**
     * Sets the name of the JDBC {@code Driver} to use 
     * to connect to the data source.
     * @param jdbcDriver	a {@code String} corresponding to the name 
     * 						of the JDBC {@code Driver}
     */
    public void setJdbcDriver(String jdbcDriver) {
        this.localJdbcDriver = jdbcDriver;
    }

    /**
     * Returns the database url of the form {@code jdbc:subprotocol:subname}, 
     * to use to connect to the data source using the {@code DriverManager}. 
     * @return 	a {@code String} representing the JDBC URL to use
     */
    public String getJdbcUrl() {
        return this.localJdbcUrl;
    }
    /**
     * Sets the database url of the form {@code jdbc:subprotocol:subname}, 
     * to use to connect to the data source using the {@code DriverManager}. 
     * @param jdbcUrl 	a {@code String} representing the JDBC URL to use
     */
    public void setJdbcUrl(String jdbcUrl) {
        this.localJdbcUrl = jdbcUrl;
    }

    /**
     * Returns the default username to use to connect to the data source 
     * using the JDBC URL.
     * @return 	a {@code String} representing the default username.
     */
    public String getJdbcUsername() {
        return this.localJdbcUsername;
    }
    /**
     * Sets the default username to use to connect to the data source 
     * using the JDBC URL.
     * @param jdbcUsername	a {@code String} representing the default username.
     */
    public void setJdbcUsername(String jdbcUsername) {
        this.localJdbcUsername = jdbcUsername;
    }

    /**
     * Returns the default password to use to connect to the data source 
     * using the JDBC URL.
     * @return 	a {@code String} representing the default password.
     */
    public String getJdbcPassword() {
        return this.localJdbcPassword;
    }
    /**
     * Sets the default password to use to connect to the data source 
     * using the JDBC URL.
     * @param jdbcPassword	a {@code String} representing the default password.
     */
    public void setJdbcPassword(String jdbcPassword) {
        this.localJdbcPassword = jdbcPassword;
    }
    /**
     * Returns the name of the resource to look up to get the dataSource
     * @return a {@code String} representing the name of the resource
     */
    public String getDataSourceResourceName() {
        return this.localDataSourceResourceName;
    } 
    /**
     * Sets the name of the resource to look up to get the dataSource
     * @param dataSourceResourceName a {@code String} representing
     * the name of the resource
     */
    public void setDataSourceResourceName(String dataSourceResourceName) {
        this.localDataSourceResourceName = dataSourceResourceName;
    }
    /**
     * Returns the maximum allowed size for a single {@code PreparedStatement} pool.
     * @return an {@code int} representing the maximum size of the pool.
     */
    public int getPrepStatPoolMaxSize() {
        return this.localPrepStatPoolMaxSize;
    } 
    /**
     * Returns the maximum allowed cumulated size for all {@code PreparedStatement} pools.
     * @return an {@code int} representing the maximum cumulated size allowed for pools.
     */
    public int getPrepStatPoolsMaxTotalSize() {
        return localPrepStatPoolsMaxTotalSize;
    } 
    /**
     * Sets the maximum allowed size for a single {@code PreparedStatement} pool.
     * @param prepStatPoolMaxSize an {@code int} representing
     * the maximum size of the pool.
     */
    public void setPrepStatPoolMaxSize(int prepStatPoolMaxSize) {
        this.localPrepStatPoolMaxSize = prepStatPoolMaxSize;
    }   
    /**
     * Sets the maximum allowed cumulated size for all {@code PreparedStatement} pools.
     * @param prepStatPoolsMaxTotalSize an {@code int} representing
     * the maximum cumulated size allowed for pools.
     */
    public void setPrepStatPoolsMaxTotalSize(int prepStatPoolsMaxTotalSize) {
        this.localPrepStatPoolsMaxTotalSize = prepStatPoolsMaxTotalSize;
    }    

    /**
     * Returns a {@code boolean} defining whether static factories should be used 
     * when available.
     * See {@link org.bgee.model.EntityFactoryProvider EntityFactoryProvider} for more details.
     * <p>
     * If {@code true}, static factories will be used when available. 
     * Corresponds to the property {@code bgee.static.factories}. 
     * Default value is {@code false}.
     * <p>
     * Note that this property cannot be modified locally in a given thread, 
     * as it increases the memory load when used, thus impacting all threads.
     * 
     * @return 	a {@code boolean} defining whether static factories should be used 
     * 			when available.
     * @see org.bgee.model.EntityFactoryProvider
     * @see #setUseStaticFactory(boolean)
     */
    public boolean useStaticFactories() {
        return useStaticFactories;
    }
}
