package org.bgee.model.dao.mysql;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.source.SourceDAO;

public class MySQLDAOManager extends DAOManager {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(MySQLDAOManager.class.getName());
    
    /**
     * This {@code ConcurrentMap} is used to store all registered JDBC {@code Driver}s, 
     * to be able to deregister them at application shutdown. Otherwise, it is 
     * a source of memory leak in a servlet container context.
     */
    private final static ConcurrentMap<String, Driver> registeredDrivers = 
            new ConcurrentHashMap<String, Driver>();
    

    /**
     * A {@code Map} storing the opened {@code BgeeConnection}s 
     * provided by this {@code MySQLDAOManager}, associated to their {@code ID} 
     * (see {@link BgeeConnection#getId()}). 
     */
    private final Map<String, BgeeConnection> connections;
    
    /**
     * The {@code DataSource} used to obtain {@code Connection} from. 
     * {@code null} if no {@code DataSource} could be obtained 
     * from {@code InitialContext}. In that case, a JDBC {@code Driver} should 
     * be used.
     */
    private DataSource dataSource;
    
    /**
     * A {@code String} that is the key to retrieve the resource name 
     * of the {@code DataSource} to use from the {@code Properties} provided 
     * to the method {@code setParameters}. See {@link #getDataSourceResourceName()} 
     * for more details.
     * @see #getDataSourceResourceName()
     */
    public final static String RESOURCENAMEKEY = "bgee.dao.datasource.resource.name";
    /**
     * A {@code String} that is the resource name of the {@code DataSource} to use. 
     * This parameter is not mandatory if a JDBC {@code Driver} is used to connect 
     * to the database (see {@link #getJdbcDriverName()}). If the username and 
     * password were not provided to the {@code InitialContext} loading the 
     * {@code DataSource}, it must be provided to this {@code MySQLDAOManager} 
     * (see {@link #getUsername()} and {@link #getPassword()}).
     * 
     * @see #RESOURCENAMEKEY
     * @see #dataSource
     */
    private String dataSourceResourceName;
    
    /**
     * A {@code String} that is the key to retrieve the username to use from 
     * the {@code Properties} provided to the method {@code setParameters}. 
     * See {@link #getUsername()} for more details.
     * @see #getUsername()
     */
    public final static String USERNAMEKEY = "bgee.dao.username";
    /**
     * A {@code String} that is the username to use to connect to the database.
     * It is used either when a {@code DataSource} is used and the username 
     * was not provided in its configuration, or a JDBC {@code Driver} is used 
     * and the username was not provided in the connection URL.
     * 
     * @see #USERNAMEKEY
     */
    private String username;
    
    /**
     * A {@code String} that is the key to retrieve the password to use from 
     * the {@code Properties} provided to the method {@code setParameters}. 
     * See {@link #getPassword()} for more details.
     * @see #getPassword()
     */
    public final static String PASSWORDKEY = "bgee.dao.password";
    /**
     * A {@code String} that is the password to use to connect to the database.
     * It is used either when a {@code DataSource} is used and the password 
     * was not provided in its configuration, or a JDBC {@code Driver} is used 
     * and the password was not provided in the connection URL.
     * 
     * @see #PASSWORDKEY
     */
    private String password;
    
    /**
     * A {@code String} that is the key to retrieve the name of the JDBC {@code Driver} 
     * to use from the {@code Properties} provided to the method {@code setParameters}. 
     * See {@link #getJdbcDriverName()} for more details.
     * @see #getJdbcDriverName()
     */
    public final static String JDBCDRIVERNAMEKEY = "bgee.dao.jdbc.driver.name";
    /**
     * A {@code String} that is the name of the JDBC {@code Driver} to use 
     * to connect to the database. This parameter should not be mandatory, as starting from 
     * JDBC 4, a {@code Driver} is supposed to be auto-loaded and retrieved automatically. 
     * But we make this parameter mandatory when not using a {@code DataSource}, 
     * so that the application can register and deregister the JDBC {@code Driver}: 
     * the auto-loading is basically broken in a servlet container environment. 
     * If a webapp used in a tomcat container has database drivers in its WEB-INF/lib 
     * directory, it cannot rely on the service provider mechanism, and should register 
     * the drivers explicitly, and deregister them to avoid memory leak.
     * <p>
     * When using a {@code Driver} rather than a {@code DataSource}, the connection 
     * URL must be provided (see {@link #getJdbcUrl()}). If this URL does not 
     * contain the username and password to use, they must also be provided 
     * to this {@code DAOManager} (see {@link #getUsername()} and {@link #getPassword()}).
     * 
     * @see #JDBCDRIVERNAMEKEY
     * @see #getJdbcUrl()
     * @see #getUsername()
     * @see #getPassword()
     */
    private String jdbcDriverName;
    
    /**
     * A {@code String} that is the key to retrieve the JDBC connection URL from 
     * the {@code Properties} provided to the method {@code setParameters}. 
     * See {@link #getJdbcUrl()} for more details.
     * @see #getJdbcUrl()
     */
    public final static String JDBCURLKEY = "bgee.dao.jdbc.url";
    /**
     * A {@code String} that is the JDBC connection URL, used to connect to the database 
     * when a JDBC {@code Driver} is used, rather than a {@code DataSource}. It means  
     * that {@link #dataSourceResourceName} is {@code null}, or does not allow 
     * to obtain a {@code DataSource}).
     * 
     * @see #JDBCURLKEY
     */
    private String jdbcUrl;
    
    /**
     * Default constructor. {@code DAOManager}s must provide a no-arguments public 
     * constructor, to be used as a {@code Service Provider}.
     */
    public MySQLDAOManager() {
        super();
        this.connections = new HashMap<String, BgeeConnection>();
    }
    
    //******************************************
    // METHODS SPECIFIC TO THE MySQLDAOManager
    //******************************************
    /**
     * Attempt to establish a connection, either from a {@code DataSource} 
     * if one was provided using JNDI, or from a JDBC {@code Driver} 
     * if one is defined in the parameters. The JDBC connection {@code URI}, 
     * and username and password if not provided in the {@code URI}, are retrieved 
     * from the attributes of this {@code MySQLDAOManager}, set by the method 
     * {@code setParameters}.
     * <p>
     * If a {@code BgeeConnection} with the same parameters is already hold 
     * by this {@code MySQLDAOManager}, it will be returned, without creating a new one. 
     * This {@code MySQLDAOManager} will hold this {@code BgeeConnection} 
     * as long as it is not closed (by a call to {@link BgeeConnection#close()} 
     * for instance). 
     * <p>
     * If this {@code MySQLDAOManager} was closed ({@link #isClosed()} 
     * returns {@code true}), this method will throw a {@code SQLException}.
     * 
     * @return  An opened {@code BgeeConnection}. 
     * @throws SQLException     If an error occurred while trying to obtain the connection, 
     *                          of if this {@code MySQLDAOManager} is already closed.
     */
    public final BgeeConnection getConnection() throws SQLException
    {
        log.entry();
        log.debug("Trying to obtain a BgeeConnection");

        if (this.isClosed()) {
            throw new SQLException("This DAOManager is already closed.");
        }

        String connectionId = this.generateConnectionId(this.getJdbcUrl(), 
                this.getUsername());
        //we synchronized over this.connections, to establish a happens-before relation 
        //for methods that can be used by other threads (e.g., killDAOManager), 
        //and that will use the same lock, for atomicity. This is not because we expect 
        //this method to be used by different thread, otherwise we could simply 
        //have used a ConcurrentMap.
        synchronized(this.connections) {
            BgeeConnection connection = this.connections.get(connectionId);
            //if the connection already exists, return it
            if (connection != null) {
                log.debug("Return an already opened Connection with ID {}", connection.getId());
                return log.exit(connection);
            }
            //otherwise, create a new connection
            Connection realConnection = null;
            if (this.getDataSource() != null) {
                if (this.getUsername() == null) {
                    log.debug("Trying to obtain a new Connection from the DataSource with default username/password");
                    realConnection = this.getDataSource().getConnection();
                } else {
                    log.debug("Trying to obtain a new Connection from the DataSource using username {}", 
                            this.getUsername());
                    realConnection = this.getDataSource().getConnection(
                            this.getUsername(), this.getPassword());
                }
            } else {
                if (this.getUsername() == null) {
                    log.debug("Trying to obtain a new Connection from the DriverManager using connection URL");
                    DriverManager.getConnection(this.getJdbcUrl());
                } else {
                    log.debug("Trying to obtain a new Connection from the DataSource using username {}", 
                            this.getUsername());
                    realConnection = this.getDataSource().getConnection(
                            this.getUsername(), this.getPassword());
                }
                log.debug("Trying to obtain a new Connection from the DriverManager using connection URL and provided username/password");
                realConnection = DriverManager.getConnection(this.getJdbcUrl(), 
                        this.getUsername(), this.getPassword());
            }
            //just in case we couldn't obtain the connection, without exception
            if (realConnection == null) {
                String msg = "Could not obtain a Connection. ";
                if (this.getDataSource() == null) {
                    msg += "No DataSource was provided. ";
                    if (StringUtils.isNotBlank(this.getJdbcDriverName())) {
                        msg += "The provided JDBC Driver name, " + this.getJdbcDriverName() +
                                ", did not allow to obtain a Connection. ";
                    }
                    if (StringUtils.isNotBlank(this.getJdbcUrl())) {
                        msg += "The provided JDBC URL, " + this.getJdbcUrl() + 
                                ", did not allow to obtain a Connection. ";
                    } else {
                        msg += "No JDBC connection URL was provided. ";
                    }
                } else {
                    msg += "The DataSource did not allow to obtain a Connection. ";
                }
                throw new SQLException(msg);
            }
            //now create the new BgeeConnection
            connection = new BgeeConnection(this, realConnection, connectionId);
            //store and return it
            this.connections.put(connection.getId(), connection);

            log.debug("Return a newly opened Connection with ID {}", connection.getId());
            return log.exit(connection);
        }
    }
    

    /**
     * Initialize a {@code DataSource} obtained from a JNDI {@code InitialContext}, 
     * using the resource name provided by {@link #getDataSourceResourceName()}. 
     * Will be stored into {@link #dataSource}.
     * 
     * @throws IllegalStateException    If {@link #getDataSourceResourceName()} is not 
     *                                  {@code null} nor empty, but did not allow to 
     *                                  obtain a {@code DataSource}.
     */
    private void loadDataSource() {
        log.entry();
        if (StringUtils.isBlank(this.getDataSourceResourceName())) {
            log.exit(); return;
        }
        
        try {
            //try to get a DataSource using JNDI
            Context ctx = new InitialContext();
            this.setDataSource((DataSource) ctx.lookup(this.getDataSourceResourceName()));
            log.info("DataSource obtained from InitialContext {} using JNDI", 
                    this.getDataSourceResourceName());
        } catch (NamingException e) {
            log.catching(e);
            throw log.throwing(new IllegalStateException("The DataSource resource name " +
                    "provided (" + this.getDataSourceResourceName() + ") did not allow " +
                    "to obtain a valid DataSource.", e));
        }
        log.exit();
    }

    /**
     * Initialize a JDBC {@code Driver} using the value returned by 
     * {@link #getJdbcDriverName()} as class name, if any was provided. 
     * Providing the class name of the JDBC {@code Driver} to use is not mandatory, 
     * but is strongly recommended, when using {@code Driver}s not auto-loaded, 
     * or when using this application in a servlet container context. See 
     * {@link #getJdbcDriverName()} for more details.
     * 
     * @throws IllegalStateException    If {@link #getJdbcDriverName()} is not {@code null} 
     *                                  nor empty, but did not allow to register a JDBC 
     *                                  {@code Driver}.
     */
    private void loadJdbcDriver() throws IllegalStateException {
        log.entry();
        if (StringUtils.isBlank(this.getJdbcDriverName())) {
            log.exit(); return;
        }
        
        //if the name of a JDBC driver was provided, try to load it.
        //it should not be needed, but some buggy JDBC Drivers need to be 
        //"manually" loaded.
        
        //check that we do not already have this Driver registered
        if (!registeredDrivers.containsKey(this.getJdbcDriverName())) {
            try {
                //also, calling newInstance() should not be needed, 
                //but some buggy JDBC Drivers do not properly initialized 
                //themselves in the static initializer. Plus, we want to keep track 
                //on this instance, to be able to deregister the Driver at application 
                //shutdown.
                log.debug("Trying to register JDBC Driver with class name {}", 
                        this.getJdbcDriverName());
                Driver driver = (Driver) Class.forName(
                        this.getJdbcDriverName()).newInstance();
                
                if (registeredDrivers.putIfAbsent(this.getJdbcDriverName(), driver) != null) {
                    //here it means another Thread interleaved and registered the same Driver.
                    //we deregister the newly instantiated Driver right away
                    log.debug("Equivalent Driver already registered.");
                    try {
                        DriverManager.deregisterDriver(driver);
                    } catch (SQLException e) {
                        //hmm, I don't really know what this error should correspond to.
                        //do nothing, the deregistration is to avoid memory leak 
                        //in a servlet container context.
                        log.catching(e);
                    }
                } else {
                    log.debug("Driver successfully registered.");
                }
                
            } catch (InstantiationException | IllegalAccessException
                    | ClassNotFoundException e) {
                log.catching(e);
                throw log.throwing(new IllegalStateException("The JDBC Driver name " +
                		"provided (" + this.getJdbcDriverName() + ") did not allow " +
                		"to register it.", e));
            }
        } else {
            log.debug("Equivalent Driver already registered.");
        }
        log.exit();
    }
    


    /**
     * Notification that a {@code BgeeConnection}, with an {@code ID} 
     * equals to {@code connectionId}, holds by this {@code MySQLDAOManager}, 
     * has been closed. 
     * <p>
     * This method will thus removed from {@link #connections} 
     * the {@code BgeeConnection} with the corresponding key. 
     * 
     * @param connectionId  A {@code String} representing the {@code ID} 
     *                      of the {@code BgeeConnection} that was closed. 
     */
    protected void connectionClosed(String connectionId)
    {
        log.entry(connectionId);
        log.debug("Releasing BgeeConnection with ID {}", connectionId);
        this.removeFromConnections(connectionId);
        log.exit();
    }
    
    /**
     * Remove from {@link #connections} the {@code BgeeConnection} 
     * mapped to the key {@code key}. 
     * 
     * @param key   A {@code String} representing the key of the 
     *              {@code BgeeConnection} to be removed from {@code connections}.
     */
    private void removeFromConnections(String key) {
        //we synchronize over this.connections, to establish a happens-before relation 
        //for methods that can be used by other threads (e.g., killDAOManager), 
        //and that will use the same lock, for atomicity. This is not because we expect 
        //this method to be used by different thread, otherwise we could simply 
        //have used a ConcurrentMap.
        synchronized(this.connections) {
            this.connections.remove(key);
        }
    }
    
    /**
     * Generate an ID to uniquely identify the {@code BgeeConnection}s 
     * holded by this {@code MySQLDAOManager}. It is based on  
     * {@code jdbcUrl} and {@code username}}. 
     * 
     * @param jdbcUrl   A {@code String} defining the JDBC URL used to open 
     *                  the connection. Will be used to generate the ID.
     * @param username  A {@code String} defining the username used to open 
     *                  the connection. Will be used to generate the ID.
     * @return          A {@code String} representing an ID generated from 
     *                  the JDBC URL, {@code username}.
     */
    private String generateConnectionId(String jdbcUrl, String username) {
        //I don't like much storing a password in memory, as it could be in the url, 
        //let's hash it.
        return DigestUtils.sha1Hex(jdbcUrl + "[sep]" + username + "[sep]");
    }

    //******************************************
    // GETTERS/SETTERS
    //******************************************
    /**
     * Returns the {@code DataSource} used to obtain {@code Connection}s from. 
     * {@code null} if no {@code DataSource} could be obtained 
     * from {@code InitialContext}. In that case, a JDBC {@code Driver} should 
     * be used.
     * 
     * @return the {@code DataSource} used to obtain {@code Connection}s from.
     */
    private DataSource getDataSource() {
        return dataSource;
    }
    /**
     * Sets the {@code DataSource} used to obtain {@code Connection}s from. 
     * {@code null} if no {@code DataSource} could be obtained 
     * from {@code InitialContext}. In that case, a JDBC {@code Driver} should 
     * be used.
     * 
     * @param dataSource    the {@code DataSource} that should be used to obtain 
     *                      {@code Connection}s from.
     */
    private void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    /**
     * Returns the {@code String} that is the resource name of the {@code DataSource} 
     * to use. This parameter is not mandatory if a JDBC {@code Driver} is used 
     * to connect to the database (see {@link #getJdbcDriverName()}). 
     * If the username and password were not provided to the {@code InitialContext} 
     * loading the {@code DataSource}, it must be provided to this {@code MySQLDAOManager} 
     * (see {@link #getUsername()} and {@link #getPassword()}).
     * 
     * @return  A {@code String} representing the resource name of a {@code DataSource} 
     *          to use.
     * @see #RESOURCENAMEKEY
     */
    private String getDataSourceResourceName() {
        return dataSourceResourceName;
    }
    /**
     * Sets the {@code String} that is the resource name of the {@code DataSource} 
     * to use. This parameter is not mandatory if a JDBC {@code Driver} is used 
     * to connect to the database (see {@link #getJdbcDriverName()}). 
     * If the username and password were not provided to the {@code InitialContext} 
     * loading the {@code DataSource}, it must be provided to this {@code MySQLDAOManager} 
     * (see {@link #getUsername()} and {@link #getPassword()}).
     * 
     * @param name  The {@code String} that is the resource name 
     *              of the {@code DataSource} to use.
     * @see #RESOURCENAMEKEY
     */
    private void setDataSourceResourceName(String name) {
        this.dataSourceResourceName = name;
    }

    /**
     * Returns the username to use to connect to the database. It is used either 
     * when a {@code DataSource} is used and the username was not provided in 
     * its configuration, or a JDBC {@code Driver} is used and the username 
     * was not provided in the connection URL.
     * 
     * @return  A {@code String} that is the username to use to connect to the database.
     * @see #USERNAMEKEY
     */
    private String getUsername() {
        return username;
    }
    /**
     * Sets the username to use to connect to the database. It is used either 
     * when a {@code DataSource} is used and the username was not provided in 
     * its configuration, or a JDBC {@code Driver} is used and the username 
     * was not provided in the connection URL.
     * 
     * @param username  A {@code String} that is the username to use 
     *                  to connect to the database.
     * @see #USERNAMEKEY
     */
    private void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the password to use to connect to the database. It is used either 
     * when a {@code DataSource} is used and the password was not provided in 
     * its configuration, or a JDBC {@code Driver} is used and the password 
     * was not provided in the connection URL.
     * 
     * @return  A {@code String} that is the password to use to connect to the database.
     * @see #PASSWORDKEY
     */
    private String getPassword() {
        return password;
    }
    /**
     * Sets the password to use to connect to the database. It is used either 
     * when a {@code DataSource} is used and the password was not provided in 
     * its configuration, or a JDBC {@code Driver} is used and the password 
     * was not provided in the connection URL.
     * 
     * @param password  A {@code String} that is the password to use 
     *                  to connect to the database.
     * @see #PASSWORDKEY
     */
    private void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the name of the JDBC {@code Driver} to use 
     * to connect to the database. This parameter should not be mandatory, as starting from 
     * JDBC 4, a {@code Driver} is supposed to be auto-loaded and retrieved automatically. 
     * But we make this parameter mandatory when not using a {@code DataSource}, 
     * so that the application can register and deregister the JDBC {@code Driver}: 
     * the auto-loading is basically broken in a servlet container environment. 
     * If a webapp used in a tomcat container has database drivers in its WEB-INF/lib 
     * directory, it cannot rely on the service provider mechanism, and should register 
     * the drivers explicitly.
     * <p>
     * When using a {@code Driver} rather than a {@code DataSource}, the connection 
     * URL must be provided (see {@link #getJdbcUrl()}). If this URL does not 
     * contain the username and password to use, they must also be provided 
     * to this {@code DAOManager} (see {@link #getUsername()} and {@link #getPassword()}).
     * 
     * @return  the {@code String} that is the name of the JDBC {@code Driver} to use.
     * @see #JDBCDRIVERNAMEKEY
     * @see #getJdbcUrl()
     * @see #getUsername()
     * @see #getPassword()
     */
    private String getJdbcDriverName() {
        return jdbcDriverName;
    }
    /**
     * Sets the name of the JDBC {@code Driver} to use 
     * to connect to the database. This parameter should not be mandatory, as starting from 
     * JDBC 4, a {@code Driver} is supposed to be auto-loaded and retrieved automatically. 
     * But we make this parameter mandatory when not using a {@code DataSource}, 
     * so that the application can register and deregister the JDBC {@code Driver}: 
     * the auto-loading is basically broken in a servlet container environment. 
     * If a webapp used in a tomcat container has database drivers in its WEB-INF/lib 
     * directory, it cannot rely on the service provider mechanism, and should register 
     * the drivers explicitly.
     * <p>
     * When using a {@code Driver} rather than a {@code DataSource}, the connection 
     * URL must be provided (see {@link #getJdbcUrl()}). If this URL does not 
     * contain the username and password to use, they must also be provided 
     * to this {@code DAOManager} (see {@link #getUsername()} and {@link #getPassword()}).
     * 
     * @param jdbcDriverName    the {@code String} that is the name of the JDBC 
     *                          {@code Driver} to use.
     * @see #JDBCDRIVERNAMEKEY
     * @see #getJdbcUrl()
     * @see #getUsername()
     * @see #getPassword()
     */
    private void setJdbcDriverName(String jdbcDriverName) {
        this.jdbcDriverName = jdbcDriverName;
    }

    /**
     * Returns the JDBC connection URL, used to connect to the database 
     * when a JDBC {@code Driver} is used, rather than a {@code DataSource}. It means  
     * that {@link #getDataSourceResourceName()} returns {@code null}, or does not allow 
     * to obtain a {@code DataSource}).
     * 
     * @return  A {@code String} that is the JDBC connection URL, used to connect 
     *          to the database.
     * @see #JDBCURLKEY
     */
    private String getJdbcUrl() {
        return jdbcUrl;
    }
    /**
     * Sets the JDBC connection URL, used to connect to the database 
     * when a JDBC {@code Driver} is used, rather than a {@code DataSource}. It means  
     * that {@link #getDataSourceResourceName()} returns {@code null}, or does not allow 
     * to obtain a {@code DataSource}).
     * 
     * @param jdbcUrl   A {@code String} that is the JDBC connection URL, used to connect 
     *                  to the database.
     * @see #JDBCURLKEY
     */
    private void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }
    
    //******************************************
    // IMPLEMENT DAOManager ABSTRACT METHODS
    //******************************************

    /*
     * (non-Javadoc)
     * Of note, this method is responsible for triggering the loading of 
     * the DataSource or the JDBC Driver when the appropriate parameters are provided.
     * 
     * @see org.bgee.model.dao.api.DAOManager#setParameters(java.util.Properties)
     */
    @Override
    public void setParameters(Properties props) throws IllegalArgumentException {
        log.entry(props);
        
        String resourceName = props.getProperty(RESOURCENAMEKEY);
        String jdbcUrl      = props.getProperty(JDBCURLKEY);
        String driverName   = props.getProperty(JDBCDRIVERNAMEKEY);
        
        //check whether the required parameters are provided: this MySQLDAOManager 
        //either needs a DataSource, or a JDBC connection URL and the JDBC Driver name 
        //(the JDBC Driver name should not be mandatory, but it is as the ServiceLoader 
        //mechanism is broken in a servlet container environment).
        //username and password are not mandatory, they can either be provided 
        //in the configuration of the DataSource, or in the JDBC URL.
        if (StringUtils.isBlank(resourceName) && 
                (StringUtils.isBlank(jdbcUrl) || StringUtils.isBlank(driverName))) {
            throw log.throwing(new IllegalArgumentException("The parameters provided " +
            		"do not allow to use a MySQLDAOManager: it must be provided " +
            		"either the name of the resource to retrieve a DataSource " +
            		"from an InitialContext, or the JDBC connection URL to use " +
            		"with the DriverManager."));
        }
        
        //check whether the DataSource or the Driver should be reloaded, 
        //to avoid useless loadings
        boolean dataSourceChange = false;
        if ((this.getDataSourceResourceName() == null && resourceName != null) || 
                (this.getDataSourceResourceName() != null && 
                !this.getDataSourceResourceName().equals(resourceName))) {
            dataSourceChange = true;
        }
        
        boolean driverChange = false;
        if ((this.getJdbcDriverName() == null && driverName != null) || 
                (this.getJdbcDriverName() != null && 
                !this.getJdbcDriverName().equals(driverName))) {
            driverChange = true;
        }
        
        this.setDataSourceResourceName(resourceName);
        this.setJdbcDriverName(driverName);
        this.setJdbcUrl(props.getProperty(JDBCURLKEY));
        this.setUsername(props.getProperty(USERNAMEKEY));
        this.setPassword(props.getProperty(PASSWORDKEY));
        
        //these methods are responsible to check for the validity of resourceName 
        //and driverName, they throw an IllegalStateException if they are invalid
        try {
            if (dataSourceChange) {
                this.loadDataSource();
            }
            if (driverChange) {
                this.loadJdbcDriver();
            }
        } catch (IllegalStateException e) {
            log.catching(e);
            throw log.throwing(new IllegalArgumentException("The parameters provided " +
            		"did not allow to load a valid DataSource or JDBC Driver", e));
        }
        
        log.exit();
    }
    
    @Override
    protected void closeDAOManager() throws DAOException {
        log.entry();
        synchronized(this.connections) {
            //get a shallow copy of the collection, so that the removal of 
            //a connection will not interfere with the iteration
            Collection<BgeeConnection> shallowCopy = 
                    new ArrayList<BgeeConnection>(this.connections.values());
            for (BgeeConnection connToClose: shallowCopy) {
                try {
                    //calling this method will automatically remove the connection 
                    //from connections
                    connToClose.close();
                } catch (SQLException e) {
                    log.catching(e);
                    throw log.throwing(new DAOException(e));
                }
            }
        }
        log.exit();
    }

    @Override
    protected void killDAOManager() throws DAOException {
        synchronized(this.connections) {
            //get a shallow copy of the collection, so that the removal of 
            //a connection will not interfere with the iteration
            Collection<BgeeConnection> shallowCopy = 
                    new ArrayList<BgeeConnection>(this.connections.values());
            for (BgeeConnection conn: shallowCopy) {
                try {
                    //calling this method will automatically call close 
                    //on the connection, removing it from the attribute connections.
                    conn.kill();
                } catch (SQLException e) {
                    log.catching(e);
                    throw log.throwing(new DAOException(e));
                }
            }
        }
    }
    
    @Override
    protected void shutdown() throws DAOException {
        //reminder: this method should actually be a static method, 
        //but Java does not permit abstract static methods...
        synchronized(registeredDrivers) {
            for (Driver driver: registeredDrivers.values()) {
                try {
                    DriverManager.deregisterDriver(driver);
                } catch (SQLException e) {
                    log.catching(e);
                    throw log.throwing(new DAOException(e));
                }
            }
        }
    }

    //******************************************
    // IMPLEMENT DAOManager ABSTRACT METHODS TO OBTAIN DAOs
    //******************************************
    @Override
    protected SourceDAO getNewSourceDAO() {
        // TODO Auto-generated method stub
        return null;
    }
    
}
