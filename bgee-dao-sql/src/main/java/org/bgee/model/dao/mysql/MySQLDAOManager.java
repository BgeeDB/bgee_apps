package org.bgee.model.dao.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.source.SourceDAO;

public class MySQLDAOManager extends DAOManager {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(MySQLDAOManager.class.getName());
    

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
     * to connect to the database. This parameter is not mandatory, as starting from 
     * JDBC 4, a {@code Driver} is supposed to be auto-loaded and retrieved automatically. 
     * Unfortunately, some JDBC {@code Driver}s need to be "manually" loaded. 
     * In that case, the class name of the {@code Driver} is needed.
     * <p>
     * Also, the auto-loading is basically broken in a servlet container environment. 
     * If a webapp used in a tomcat container has database drivers in its WEB-INF/lib 
     * directory, it cannot rely on the service provider mechanism, and should register 
     * the drivers explicitly.
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
                this.getUsername(), this.getPassword());
        BgeeConnection connection = this.getConnections().get(connectionId);
        //if the connection already exists, return it
        if (connection != null) {
            log.debug("Return an already opened Connection with ID {}", connection.getId());
            return log.exit(connection);
        }
        //otherwise, create a new connection
        Connection realConnection = null;
        if (MySQLDAOManager.dataSource != null) {
            if (this.getUsername() == null) {
                log.debug("Trying to obtain a new Connection from the DataSource with default username/password");
                realConnection = MySQLDAOManager.dataSource.getConnection();
            } else {
                log.debug("Trying to obtain a new Connection from the DataSource using username {}", 
                        this.getUsername());
                realConnection = MySQLDAOManager.dataSource.getConnection(
                        this.getUsername(), this.getPassword());
            }
        } else {
            if (this.getUsername() == null) {
                log.debug("Trying to obtain a new Connection from the DriverManager using connection URL");
                DriverManager.getConnection(this.getJdbcUrl());
            } else {
                log.debug("Trying to obtain a new Connection from the DataSource using username {}", 
                        this.getUsername());
                realConnection = MySQLDAOManager.dataSource.getConnection(
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
        this.storeConnection(connection);

        log.debug("Return a newly opened Connection with ID {}", connection.getId());
        return log.exit(connection);
    }
    

    /**
     * Initialize a {@code DataSource} obtained from a JNDI {@code InitialContext}, 
     * using the resource name provided by {@link #getDataSourceResourceName()}. 
     * Will be stored into {@link #dataSource}.
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
            log.info("No DataSource obtained from InitialContext using JNDI, should rely on a JDBC Driver");
            //here, we do nothing: the DataSource could not be loaded, 
            //SQLExceptions will be thrown when getConnection will be called anyway 
            //(if no JDBC Driver is available).
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
     */
    private void loadJdbcDriver() {
        log.entry();
        if (StringUtils.isBlank(this.getJdbcDriverName())) {
            log.exit(); return;
        }
        
        //if the name of a JDBC driver was provided, try to load it.
        //it should not be needed, but some buggy JDBC Drivers need to be 
        //"manually" loaded.
        try {
            //also, calling newInstance() should not be needed, 
            //but some buggy JDBC Drivers do not properly initialized 
            //themselves in the static initializer.
            Class.forName(this.getJdbcDriverName()).newInstance();
        } catch (InstantiationException | IllegalAccessException
                | ClassNotFoundException e) {
            //here, we do nothing: the JDBC Driver could not be loaded, 
            //SQLExceptions will be thrown when getConnection will be called anyway.
            log.catching(e);
        }
        log.exit();
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
    public DataSource getDataSource() {
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
    public String getUsername() {
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
    public String getPassword() {
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
     * to connect to the database. This parameter is not mandatory, as starting from 
     * JDBC 4, a {@code Driver} is supposed to be auto-loaded and retrieved automatically. 
     * Unfortunately, some JDBC {@code Driver}s need to be "manually" loaded. 
     * In that case, the class name of the {@code Driver} is needed.
     * <p>
     * Also, the auto-loading is basically broken in a servlet container environment. 
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
    public String getJdbcDriverName() {
        return jdbcDriverName;
    }
    /**
     * Sets the name of the JDBC {@code Driver} to use 
     * to connect to the database. This parameter is not mandatory, as starting from 
     * JDBC 4, a {@code Driver} is supposed to be auto-loaded and retrieved automatically. 
     * Unfortunately, some JDBC {@code Driver}s need to be "manually" loaded. 
     * In that case, the class name of the {@code Driver} is needed.
     * <p>
     * Also, the auto-loading is basically broken in a servlet container environment. 
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
    public String getJdbcUrl() {
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
        
        //check whether the required parameters are provided: this MySQLDAOManager 
        //either needs a DataSource, or a JDBC connection URL to use a JDBC Driver 
        //(the JDBC Driver name is not mandatory, it is possible to obtain it 
        //from the DriverManager only thanks to the URL).
        //username and password are not mandatory, they can either be provided 
        //in the configuration of the DataSource, or in the JDBC URL.
        if (StringUtils.isBlank(resourceName) && StringUtils.isBlank(jdbcUrl)) {
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
        
        String driverName   = props.getProperty(JDBCDRIVERNAMEKEY);
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
        //and driverName
        if (dataSourceChange) {
            this.loadDataSource();
        }
        if (driverChange) {
            this.loadJdbcDriver();
        }
        
        log.exit();
    }
    
    @Override
    protected void closeDAOManager() {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void killDAOManager() {
        // TODO Auto-generated method stub
        
    }
    
    protected void closeAllImpl() {
//        The list of drivers in java.sql.DriverManager is also a known source 
//        of memory leaks. Any Drivers registered by a web application must be 
//        deregistered when the web application stops. Tomcat will attempt to 
//        automatically discover and deregister any JDBC drivers loaded by the 
//        web application class loader when the web application stops. However, 
//        it is expected that applications do this for themselves via a ServletContextListener. 
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
