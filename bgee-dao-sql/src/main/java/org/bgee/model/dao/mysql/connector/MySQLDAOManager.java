package org.bgee.model.dao.mysql.connector;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.anatdev.SexDAO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.SamplePValueDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.est.ESTDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.est.ESTLibraryDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituEvidenceDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituExperimentDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipTypeDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.MicroarrayExperimentDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqExperimentDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryAnnotatedSampleDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryDAO;
import org.bgee.model.dao.api.file.DownloadFileDAO;
import org.bgee.model.dao.api.file.SpeciesDataGroupDAO;
import org.bgee.model.dao.api.gene.GeneHomologsDAO;
import org.bgee.model.dao.api.gene.GeneNameSynonymDAO;
import org.bgee.model.dao.api.keyword.KeywordDAO;
import org.bgee.model.dao.api.source.SourceToSpeciesDAO;
import org.bgee.model.dao.mysql.anatdev.MySQLAnatEntityDAO;
import org.bgee.model.dao.mysql.anatdev.MySQLSexDAO;
import org.bgee.model.dao.mysql.anatdev.MySQLStageDAO;
import org.bgee.model.dao.mysql.anatdev.MySQLTaxonConstraintDAO;
import org.bgee.model.dao.mysql.anatdev.mapping.MySQLRawSimilarityAnnotationDAO;
import org.bgee.model.dao.mysql.anatdev.mapping.MySQLStageGroupingDAO;
import org.bgee.model.dao.mysql.anatdev.mapping.MySQLSummarySimilarityAnnotationDAO;
import org.bgee.model.dao.mysql.expressiondata.call.MySQLConditionDAO;
import org.bgee.model.dao.mysql.expressiondata.call.MySQLDiffExpressionCallDAO;
import org.bgee.model.dao.mysql.expressiondata.call.MySQLGlobalExpressionCallDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.MySQLRawDataConditionDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.MySQLRawExpressionCallDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.MySQLSamplePValueDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.MysqlRawDataCountDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.est.MySQLESTDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.est.MySQLESTLibraryDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.insitu.MySQLInSituEvidenceDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.insitu.MySQLInSituExperimentDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.insitu.MySQLInSituSpotDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.microarray.MySQLAffymetrixChipDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.microarray.MySQLAffymetrixChipTypeDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.microarray.MySQLAffymetrixProbesetDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.microarray.MySQLMicroarrayExperimentDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.rnaseq.MySQLRNASeqExperimentDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.rnaseq.MySQLRNASeqLibraryAnnotatedSampleDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.rnaseq.MySQLRNASeqLibraryDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.rnaseq.MySQLRNASeqResultAnnotatedSampleDAO;
import org.bgee.model.dao.mysql.file.MySQLDownloadFileDAO;
import org.bgee.model.dao.mysql.file.MySQLSpeciesDataGroupDAO;
import org.bgee.model.dao.mysql.gene.MySQLGeneDAO;
import org.bgee.model.dao.mysql.gene.MySQLGeneHomologsDAO;
import org.bgee.model.dao.mysql.gene.MySQLGeneNameSynonymDAO;
import org.bgee.model.dao.mysql.gene.MySQLGeneOntologyDAO;
import org.bgee.model.dao.mysql.gene.MySQLGeneXRefDAO;
import org.bgee.model.dao.mysql.gene.MySQLHierarchicalGroupDAO;
import org.bgee.model.dao.mysql.keyword.MySQLKeywordDAO;
import org.bgee.model.dao.mysql.ontologycommon.MySQLCIOStatementDAO;
import org.bgee.model.dao.mysql.ontologycommon.MySQLEvidenceOntologyDAO;
import org.bgee.model.dao.mysql.ontologycommon.MySQLRelationDAO;
import org.bgee.model.dao.mysql.source.MySQLSourceDAO;
import org.bgee.model.dao.mysql.source.MySQLSourceToSpeciesDAO;
import org.bgee.model.dao.mysql.species.MySQLSpeciesDAO;
import org.bgee.model.dao.mysql.species.MySQLTaxonDAO;

public class MySQLDAOManager extends DAOManager {
    private final static Logger log = LogManager.getLogger(MySQLDAOManager.class.getName());
    
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
    public final static String RESOURCE_NAME_KEY = "bgee.dao.datasource.resource.name";
    /**
     * A {@code String} that is the resource name of the {@code DataSource} to use. 
     * This parameter is not mandatory if a JDBC {@code Driver} is used to connect 
     * to the database (see {@link #getJdbcDriverNames()}). If the user and
     * password were not provided to the {@code InitialContext} loading the 
     * {@code DataSource}, it must be provided to this {@code MySQLDAOManager} 
     * (see {@link #getUser()} and {@link #getPassword()}).
     * 
     * @see #RESOURCE_NAME_KEY
     * @see #dataSource
     */
    private String dataSourceResourceName;
    
    /**
     * A {@code String} that is the key to retrieve the user to use from 
     * the {@code Properties} provided to the method {@code setParameters}. 
     * See {@link #getUser()} for more details.
     * @see #getUser()
     */
    public final static String USER_KEY = "bgee.dao.jdbc.username";
    /**
     * A {@code String} that is the user to use to connect to the database.
     * It is used either when a {@code DataSource} is used and the user 
     * was not provided in its configuration, or a JDBC {@code Driver} is used 
     * and the user was not provided in the connection URL.
     * 
     * @see #USER_KEY
     */
    private String user;
    
    /**
     * A {@code String} that is the key to retrieve the password to use from 
     * the {@code Properties} provided to the method {@code setParameters}. 
     * See {@link #getPassword()} for more details.
     * @see #getPassword()
     */
    public final static String PASSWORD_KEY = "bgee.dao.jdbc.password";
    /**
     * A {@code String} that is the password to use to connect to the database.
     * It is used either when a {@code DataSource} is used and the password 
     * was not provided in its configuration, or a JDBC {@code Driver} is used 
     * and the password was not provided in the connection URL.
     * 
     * @see #PASSWORD_KEY
     */
    private String password;
    
    /**
     * A {@code String} that is the key to retrieve the names of the JDBC {@code Driver}s 
     * to use from the {@code Properties} provided to the method {@code setParameters}. 
     * See {@link #getJdbcDriverNames()} for more details.
     * <p>
     * Several driver names can be provided separated by a comma. This is useful, 
     * for instance to load both a MySQL Driver and a log4jdbc-log4j2 Driver.
     * @see #getJdbcDriverNames()
     */
    public final static String JDBC_DRIVER_NAMES_KEY = "bgee.dao.jdbc.driver.names";
    /**
     * A {@code Set} of {@code String}s containing the names of the JDBC {@code Driver} 
     * to load. This parameter should not be mandatory, as starting from 
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
     * contain the user and password to use, they must also be provided 
     * to this {@code DAOManager} (see {@link #getUser()} and {@link #getPassword()}).
     * 
     * @see #JDBC_DRIVER_NAMES_KEY
     * @see #getJdbcUrl()
     * @see #getUser()
     * @see #getPassword()
     */
    private final Set<String> jdbcDriverNames;
    
    /**
     * A {@code String} that is the key to retrieve the JDBC connection URL from 
     * the {@code Properties} provided to the method {@code setParameters}. 
     * See {@link #getJdbcUrl()} for more details.
     * @see #getJdbcUrl()
     */
    public final static String JDBC_URL_KEY = "bgee.dao.jdbc.url";
    /**
     * A {@code String} that is the JDBC connection URL, used to connect to the database 
     * when a JDBC {@code Driver} is used, rather than a {@code DataSource}. It means  
     * that {@link #dataSourceResourceName} is {@code null}, or does not allow 
     * to obtain a {@code DataSource}).
     * 
     * @see #JDBC_URL_KEY
     */
    private String jdbcUrl;
    
    /**
     * A {@code String} that is the key to retrieve the gene count limit for expression propagation 
     * from the {@code Properties} provided to the method {@code setParameters}. 
     * See {@link #getExprPropagationGeneCount()} for more details.
     * 
     * @see #getExprPropagationGeneCount()
     * @see #DEFAULT_EXPR_PROPAGATION_GENE_COUNT
     */
    public final static String EXPR_PROPAGATION_GENE_COUNT_KEY = 
            "bgee.dao.expr.propagation.gene.count";
    /**
     * An {@code int} that is the gene count limit for expression propagation. 
     * See {@link #getExprPropagationGeneCount()} for more details.
     * 
     * @see #getExprPropagationGeneCount()
     * @see #DEFAULT_EXPR_PROPAGATION_GENE_COUNT
     * @see #EXPR_PROPAGATION_GENE_COUNT_KEY
     */
    private int exprPropagationGeneCount;
    /**
     * An {@code int} that is the defaut value for gene count limit for expression propagation. 
     * See {@link #getExprPropagationGeneCount()} for more details.
     * 
     * @see #getExprPropagationGeneCount()
     * @see #EXPR_PROPAGATION_GENE_COUNT_KEY
     */
    public final static int DEFAULT_EXPR_PROPAGATION_GENE_COUNT = 1000;
    
    
    /**
     * A {@code String} representing the name of the database to use. All following 
     * calls to {@link #getConnection()} will return {@code BgeeConnection}s with  
     * their underlying real JDBC {@code Connection} set to use this database 
     * (by using the method {@code Connection#setCatalog(String)}).
     * <p>
     * This is useful when a client needs to use a different database than the one 
     * specified in the {@link #jdbcUrl}, or specified to the {@link #dataSource} used.
     * <p>
     * If {@code null}, then default parameters specified will be used again.
     */
    private String databaseToUse;
    
    /**
     * Default constructor. {@code DAOManager}s must provide a no-arguments public 
     * constructor, to be used as a {@code Service Provider}.
     */
    public MySQLDAOManager() {
        super();
        this.connections = new HashMap<String, BgeeConnection>();
        this.jdbcDriverNames = new HashSet<String>();
        this.setExprPropagationGeneCount(DEFAULT_EXPR_PROPAGATION_GENE_COUNT);
    }
    
    //******************************************
    // METHODS SPECIFIC TO THE MySQLDAOManager
    //******************************************
    /**
     * Attempt to establish a connection, either from a {@code DataSource} 
     * if one was provided using JNDI, or from a JDBC {@code Driver} 
     * if one is defined in the parameters. The JDBC connection {@code URI}, 
     * and user and password if not provided in the {@code URI}, are retrieved 
     * from the attributes of this {@code MySQLDAOManager}, set by the method 
     * {@code setParameters}.
     * <p>
     * If a {@code BgeeConnection} with the same parameters is already held 
     * by this {@code MySQLDAOManager}, it will be returned, without creating a new one. 
     * This {@code MySQLDAOManager} will hold this {@code BgeeConnection} 
     * as long as it is not closed (by a call to {@link BgeeConnection#close()} 
     * for instance). 
     * <p>
     * If this {@code MySQLDAOManager} was closed ({@link #isClosed()} 
     * returns {@code true}), this method will throw a {@code SQLException}.
     * <p>
     * If you want to use a different database than the one defined by the connection 
     * URL, you can use the method {@link #setDatabaseToUse(String)}.
     * <p>
     * <strong>Warning:</strong> you should never call {@code setCatalog} on the 
     * real underlying JDBC {@code Connection} yourself. You must use 
     * {@link #setDatabaseToUse(String)} instead.
     * 
     * @return  An opened {@code BgeeConnection}. 
     * @throws SQLException     If an error occurred while trying to obtain the connection, 
     *                          of if this {@code MySQLDAOManager} is already closed.
     */
    public BgeeConnection getConnection() throws SQLException {
        log.traceEntry();

        if (this.isClosed()) {
            throw new SQLException("This DAOManager is already closed.");
        }

        String connectionId = this.generateConnectionId(this.getJdbcUrl(), 
                this.getUser(), this.getDatabaseToUse());
        //we synchronized over this.connections, to establish a happens-before relation 
        //for methods that can be used by other threads (e.g., killDAOManager), 
        //and that will use the same lock, for atomicity. This is not because we expect 
        //this method to be used by different thread, otherwise we could simply 
        //have used a ConcurrentMap.
        synchronized(this.connections) {
            BgeeConnection connection = this.connections.get(connectionId);
            //if the connection already exists, return it
            if (connection != null) {
                log.trace("Return an already opened Connection with ID {}", connection.getId());
                return log.traceExit(connection);
            }
            //otherwise, create a new connection
            Connection realConnection = null;
            if (this.getDataSource() != null) {
                if (this.getUser() == null) {
                    log.debug("Trying to obtain a new Connection from the DataSource with default user/password");
                    realConnection = this.getDataSource().getConnection();
                } else {
                    log.debug("Trying to obtain a new Connection from the DataSource using user {}", 
                            this.getUser());
                    realConnection = this.getDataSource().getConnection(
                            this.getUser(), this.getPassword());
                }
            } else {
                if (this.getUser() == null) {
                    log.debug("Trying to obtain a new Connection from the DriverManager using connection URL");
                    realConnection = DriverManager.getConnection(this.getJdbcUrl());
                } else {
                    log.debug("Trying to obtain a new Connection from the DriverManager using connection URL and user {}", 
                            this.getUser());
                    realConnection = DriverManager.getConnection(this.getJdbcUrl(), 
                            this.getUser(), this.getPassword());
                }
            }
            //just in case we couldn't obtain the connection, without exception
            if (realConnection == null) {
                String msg = "Could not obtain a Connection. ";
                if (this.getDataSource() == null) {
                    msg += "No DataSource was provided. ";
                    if (!this.getJdbcDriverNames().isEmpty()) {
                        msg += "The provided JDBC Drivers , " + this.getJdbcDriverNames() +
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
            //if an alternative database to use has been specified, set it
            if (this.getDatabaseToUse() != null) {
                connection.getRealConnection().setCatalog(this.getDatabaseToUse());
            }
            //store and return it
            this.connections.put(connection.getId(), connection);
            

            log.debug("Return a newly opened Connection with ID {}", connection.getId());
            return log.traceExit(connection);
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
        log.traceEntry();
        if (StringUtils.isBlank(this.getDataSourceResourceName())) {
            log.traceExit(); return;
        }
        
        DataSource dataSource = null;
        Exception exception = null;
        try {
            //try to get a DataSource using JNDI
            Context ctx = new InitialContext();
            dataSource = (DataSource) ctx.lookup(this.getDataSourceResourceName());
        } catch (NamingException e) {
            log.catching(e);
            //simply catch, an exception will be thrown if dataSource is null
            exception = e;
        }
        if (dataSource == null) {
            throw log.throwing(new IllegalStateException("The DataSource resource name " +
                    "provided (" + this.getDataSourceResourceName() + ") did not allow " +
                    "to obtain a valid DataSource.", exception));
        }
        this.setDataSource(dataSource);
        log.info("DataSource obtained from InitialContext {} using JNDI", 
                this.getDataSourceResourceName());
        log.traceExit();
    }

    /**
     * Initialize JDBC {@code Driver}s using the values returned by 
     * {@link #getJdbcDriverNames()} as class names, if any was provided. 
     * Providing the class name of the JDBC {@code Driver} to use is not mandatory, 
     * but is strongly recommended, when using {@code Driver}s not auto-loaded, 
     * or when using this application in a servlet container context. See 
     * {@link #getJdbcDriverNames()} for more details.
     * 
     * @throws IllegalStateException    If {@link #getJdbcDriverNames()} is not {@code null} 
     *                                  nor empty, but did not allow to register any JDBC 
     *                                  {@code Driver}.
     */
    private void loadJdbcDrivers() throws IllegalStateException {
        log.traceEntry();
        if (this.getJdbcDriverNames().isEmpty()) {
            log.traceExit(); return;
        }
        
        //if the name of a JDBC driver was provided, try to load it.
        //it should not be needed, but some buggy JDBC Drivers need to be 
        //"manually" loaded.
        
        //check that we do not already have this Drivers registered
        boolean jdbcUrlAccepted = false;
        for (String driverName: this.getJdbcDriverNames()) {
            if (!registeredDrivers.containsKey(driverName)) {
                try {
                    //also, calling newInstance() should not be needed, 
                    //but some buggy JDBC Drivers do not properly initialized 
                    //themselves in the static initializer. Plus, we want to keep track 
                    //on this instance, to be able to deregister the Driver at application 
                    //shutdown.
                    log.debug("Trying to register JDBC Driver with class name {}", 
                            driverName);
                    Driver driver = (Driver) Class.forName(driverName).getDeclaredConstructor().newInstance();
                    DriverManager.registerDriver(driver);

                    if (registeredDrivers.putIfAbsent(driverName, driver) != null) {
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
                        log.debug("Driver {} successfully registered.", driver);
                    }

                } catch (InstantiationException | IllegalAccessException
                        | ClassNotFoundException | SQLException
                        | InvocationTargetException | NoSuchMethodException e) {
                    log.catching(e);
                    throw log.throwing(new IllegalStateException("The JDBC Driver name " +
                            "provided (" + driverName + ") did not allow " +
                            "to register it.", e));
                }
            } else {
                log.debug("Equivalent Driver already registered.");
            }
            try {
                if (registeredDrivers.get(driverName).acceptsURL(this.getJdbcUrl())) {
                    jdbcUrlAccepted = true;
                }
            } catch (SQLException e) {
                throw log.throwing(new IllegalStateException("The Driver " + 
                    driverName + " refuses to answer nicely.", e));
            }
        }
        if (!jdbcUrlAccepted) {
            throw log.throwing(new IllegalStateException("No Drivers accepting the JDBC URL " +
                this.getJdbcUrl()));
        }
        log.traceExit();
    }
    


    /**
     * Notification that a {@code BgeeConnection}, with an {@code ID} 
     * equals to {@code connectionId}, held by this {@code MySQLDAOManager}, 
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
        log.traceEntry("{}", connectionId);
        log.debug("Releasing BgeeConnection with ID {}", connectionId);
        this.removeFromConnections(connectionId);
        log.traceExit();
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
     * {@code jdbcUrl}, {@code user}, and {@code databaseToUse}. 
     * 
     * @param jdbcUrl       A {@code String} defining the JDBC URL used to open 
     *                      the connection. Will be used to generate the ID.
     * @param username          A {@code String} defining the user used to open
     *                      the connection. Will be used to generate the ID.
     * @param databaseToUse A {@code String} representing an alternative database 
     *                      to use than the one specified in {@code jdbcUrl}. 
     *                      See {@link #databaseToUse} for more details
     * @return          A {@code String} representing an ID generated from 
     *                  the JDBC URL, {@code user}.
     */
    private String generateConnectionId(String jdbcUrl, String username, 
            String databaseToUse) {
        //I don't like much storing a password in memory, as it could be in the url, 
        //let's hash it.
        return DigestUtils.sha1Hex(
                (jdbcUrl       != null ? jdbcUrl:"") + "[sep]" + 
                (username      != null ? username:"") + "[sep]" + 
                (databaseToUse != null ? databaseToUse:""));
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
     * to connect to the database (see {@link #getJdbcDriverNames()}). 
     * If the user and password were not provided to the {@code InitialContext} 
     * loading the {@code DataSource}, it must be provided to this {@code MySQLDAOManager} 
     * (see {@link #getUser()} and {@link #getPassword()}).
     * 
     * @return  A {@code String} representing the resource name of a {@code DataSource} 
     *          to use.
     * @see #RESOURCE_NAME_KEY
     */
    public String getDataSourceResourceName() {
        return dataSourceResourceName;
    }
    /**
     * Sets the {@code String} that is the resource name of the {@code DataSource} 
     * to use. This parameter is not mandatory if a JDBC {@code Driver} is used 
     * to connect to the database (see {@link #getJdbcDriverNames()}).
     * If the user and password were not provided to the {@code InitialContext} 
     * loading the {@code DataSource}, it must be provided to this {@code MySQLDAOManager} 
     * (see {@link #getUser()} and {@link #getPassword()}).
     * 
     * @param name  The {@code String} that is the resource name 
     *              of the {@code DataSource} to use.
     * @see #RESOURCE_NAME_KEY
     */
    private void setDataSourceResourceName(String name) {
        this.dataSourceResourceName = name;
    }

    /**
     * Returns the user to use to connect to the database. It is used either 
     * when a {@code DataSource} is used and the user was not provided in 
     * its configuration, or a JDBC {@code Driver} is used and the user 
     * was not provided in the connection URL.
     * 
     * @return  A {@code String} that is the user to use to connect to the database.
     * @see #USER_KEY
     */
    public String getUser() {
        return this.user;
    }
    /**
     * Sets the user to use to connect to the database. It is used either 
     * when a {@code DataSource} is used and the user was not provided in 
     * its configuration, or a JDBC {@code Driver} is used and the user 
     * was not provided in the connection URL.
     * 
     * @param user  A {@code String} that is the user to use 
     *                  to connect to the database.
     * @see #USER_KEY
     */
    private void setUser(String user) {
        this.user = user;
    }

    /**
     * Returns the password to use to connect to the database. It is used either 
     * when a {@code DataSource} is used and the password was not provided in 
     * its configuration, or a JDBC {@code Driver} is used and the password 
     * was not provided in the connection URL.
     * 
     * @return  A {@code String} that is the password to use to connect to the database.
     * @see #PASSWORD_KEY
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
     * @see #PASSWORD_KEY
     */
    private void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the names of the JDBC {@code Driver}s to use 
     * to connect to the database. This parameter should not be mandatory, as starting from 
     * JDBC 4, a {@code Driver} is supposed to be auto-loaded and retrieved automatically. 
     * But we make this parameter mandatory when not using a {@code DataSource}, 
     * so that the application can register and deregister the JDBC {@code Driver}s: 
     * the auto-loading is basically broken in a servlet container environment. 
     * If a webapp used in a tomcat container has database drivers in its WEB-INF/lib 
     * directory, it cannot rely on the service provider mechanism, and should register 
     * the drivers explicitly.
     * <p>
     * Several driver names can be provided separated by a comma. This is useful, 
     * for instance to load both a MySQL Driver and a log4jdbc-log4j2 Driver.
     * <p>
     * When using a {@code Driver} rather than a {@code DataSource}, the connection 
     * URL must be provided (see {@link #getJdbcUrl()}). If this URL does not 
     * contain the user and password to use, they must also be provided 
     * to this {@code DAOManager} (see {@link #getUser()} and {@link #getPassword()}).
     * 
     * @return  A {@code Set} of {@code String}s representing the names of all 
     *          the JDBC {@code Driver} to load.
     * @see #JDBC_DRIVER_NAMES_KEY
     * @see #getJdbcUrl()
     * @see #getUser()
     * @see #getPassword()
     */
    public Set<String> getJdbcDriverNames() {
        return this.jdbcDriverNames;
    }
    /**
     * Sets the names of the JDBC {@code Driver}s to load. Several driver names 
     * can be provided into the {@code driverNames} argument, separated by commas. 
     * This parameter should not be mandatory, as starting from JDBC 4, a {@code Driver} 
     * is supposed to be auto-loaded and retrieved automatically. 
     * But we make this parameter mandatory when not using a {@code DataSource}, 
     * so that the application can register and deregister the JDBC {@code Driver}: 
     * the auto-loading is basically broken in a servlet container environment. 
     * If a webapp used in a tomcat container has database drivers in its WEB-INF/lib 
     * directory, it cannot rely on the service provider mechanism, and should register 
     * the drivers explicitly.
     * <p>
     * When using a {@code Driver} rather than a {@code DataSource}, the connection 
     * URL must be provided (see {@link #getJdbcUrl()}). If this URL does not 
     * contain the user and password to use, they must also be provided 
     * to this {@code DAOManager} (see {@link #getUser()} and {@link #getPassword()}).
     * 
     * @param driverNames   a {@code String} containing the names of JDBC drivers 
     *                      to load, separated by commas.
     * @see #JDBC_DRIVER_NAMES_KEY
     * @see #getJdbcUrl()
     * @see #getUser()
     * @see #getPassword()
     */
    private void parseAndSetJdbcDriverNames(String driverNames) {
        this.jdbcDriverNames.clear();
        if (StringUtils.isNotBlank(driverNames)) {
            this.jdbcDriverNames.addAll(Arrays.asList(driverNames.split(",")));
        }
    }

    /**
     * Returns the JDBC connection URL, used to connect to the database 
     * when a JDBC {@code Driver} is used, rather than a {@code DataSource}. It means  
     * that {@link #getDataSourceResourceName()} returns {@code null}, or does not allow 
     * to obtain a {@code DataSource}).
     * 
     * @return  A {@code String} that is the JDBC connection URL, used to connect 
     *          to the database.
     * @see #JDBC_URL_KEY
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
     * @see #JDBC_URL_KEY
     */
    private void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    /**
     * Returns the gene count limit for expression propagation: when expression data 
     * are propagated on-the-fly, it is not possible to propagate all data at once 
     * (too much disk space used for temporary tables); data are thus propagated 
     * group of genes by group of genes; this parameter specifies how many genes 
     * should be considered at a same time when propagating data; the higher this value is, 
     * the faster the computations should be, but more disk space will then be used. 
     * <p>
     * See {@link #DEFAULT_EXPR_PROPAGATION_GENE_COUNT} for default value.
     * 
     * @return  An {@code int} that is the gene count limit for expression propagation.
     * @see #DEFAULT_EXPR_PROPAGATION_GENE_COUNT
     * @see #EXPR_PROPAGATION_GENE_COUNT_KEY
     */
    public int getExprPropagationGeneCount() {
        return this.exprPropagationGeneCount;
    }
    /**
     * Sets the gene count limit for expression propagation. 
     * See {@link #getExprPropagationGeneCount()} for more details.
     * 
     * @param exprPropagationGeneCount  An {@code int} that is the gene count limit 
     *                                  for expression propagation.
     * @see #getExprPropagationGeneCount()
     * @see #DEFAULT_EXPR_PROPAGATION_GENE_COUNT
     * @see #EXPR_PROPAGATION_GENE_COUNT_KEY
     */
    private void setExprPropagationGeneCount(int exprPropagationGeneCount) {
        this.exprPropagationGeneCount = exprPropagationGeneCount;
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
        log.traceEntry("{}", props);

        if (props == null) {
            log.traceExit(); return;
        }
        
        //Store Properties used to create this DAOManager
        super.setParameters(props);
        
//      log.trace("Current parameters: DataSource name: {} - JDBC URL: {} - Driver names: {} - User: {} - Password: {}", 
//              this.getDataSourceResourceName(), this.getJdbcUrl(), 
//              this.getJdbcDriverNames(), this.getUser(), this.getPassword());
        
        
        String resourceName = props.getProperty(RESOURCE_NAME_KEY);
        String jdbcUrl      = props.getProperty(JDBC_URL_KEY);
        String driverNames   = props.getProperty(JDBC_DRIVER_NAMES_KEY);
        
        //check whether the required parameters are provided: this MySQLDAOManager 
        //either needs a DataSource, or a JDBC connection URL and the JDBC Driver name 
        //(the JDBC Driver name should not be mandatory, but it is as the ServiceLoader 
        //mechanism is broken in a servlet container environment).
        //user and password are not mandatory, they can either be provided 
        //in the configuration of the DataSource, or in the JDBC URL.
        if (StringUtils.isBlank(resourceName) && 
                (StringUtils.isBlank(jdbcUrl) || StringUtils.isBlank(driverNames))) {
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
        this.parseAndSetJdbcDriverNames(driverNames);
        Set<String> names = new HashSet<String>(this.getJdbcDriverNames());
        names.removeAll(registeredDrivers.keySet());
        if (!names.isEmpty()) {
            driverChange = true;
        }
        if ((this.getJdbcUrl() == null && jdbcUrl != null) || 
                (this.getJdbcUrl() != null && 
                !this.getJdbcUrl().equals(jdbcUrl))) {
            driverChange = true;
        }
        
        this.setDataSourceResourceName(resourceName);
        this.setJdbcUrl(props.getProperty(JDBC_URL_KEY));
        this.setUser(props.getProperty(USER_KEY));
        this.setPassword(props.getProperty(PASSWORD_KEY));
        
        //these methods are responsible to check for the validity of resourceName 
        //and driverName, they throw an IllegalStateException if they are invalid
        try {
            if (dataSourceChange) {
                this.loadDataSource();
            }
            if (driverChange) {
                this.loadJdbcDrivers();
            }
        } catch (IllegalStateException e) {
            log.catching(e);
            throw log.throwing(new IllegalArgumentException("The parameters provided " +
            		"did not allow to load a valid DataSource or JDBC Driver", e));
        }
        
        String exprPropagGeneCountStr = props.getProperty(EXPR_PROPAGATION_GENE_COUNT_KEY);
        if (exprPropagGeneCountStr == null) {
            this.setExprPropagationGeneCount(DEFAULT_EXPR_PROPAGATION_GENE_COUNT);
        } else {
            try {
                this.setExprPropagationGeneCount(Integer.parseInt(exprPropagGeneCountStr));
            } catch (NumberFormatException e) {
                throw log.throwing(new IllegalArgumentException("Incorrect format " +
                		"when specifying gene count limit for expression propagation, " +
                		"parameter: " + EXPR_PROPAGATION_GENE_COUNT_KEY, e));
            }
        }

//      log.trace("New parameters set: DataSource name: {} - JDBC URL: {} - Driver names: {} - User: {} - Password: {}", 
//              this.getDataSourceResourceName(), this.getJdbcUrl(), 
//              this.getJdbcDriverNames(), this.getUser(), this.getPassword());
        log.traceExit();
    }
    
    /**
     * Sets the name of an alternative database to use. All following 
     * calls to {@link #getConnection()} will return {@code BgeeConnection}s with  
     * their underlying real JDBC {@code Connection} set to use this database 
     * (by using the method {@code Connection#setCatalog(String)}).
     * <p>
     * This is useful when a client needs to use a different database than the one 
     * specified in the {@link #jdbcUrl}, or specified to the {@link #dataSource} used. 
     * Note that it will not modified any {@code BgeeConnection}s already acquired, 
     * but will allow to acquire new ones if not already existing.
     * <p>
     * If {@code null}, then default parameters specified will be used again.
     * 
     * @param dbName    A {@code String} representing the name of an alternative 
     *                  database to use.
     */
    public void setDatabaseToUse(String dbName) {
        this.databaseToUse = dbName;
    }
    /**
     * @return the {@link #databaseToUse}.
     */
    private String getDatabaseToUse() {
        return this.databaseToUse;
    }

    @Override
    public void releaseResources() throws DAOException {
        log.traceEntry();
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
        log.traceExit();
    }
    
    @Override
    protected void closeDAOManager() throws DAOException {
        log.traceEntry();
        //note that for now, the methods closeDAOManager and releaseResources do 
        //the same thing, but this might change in the future, according to their javadoc.
        this.releaseResources();
        log.traceExit();
    }

    @Override
    protected void killDAOManager() throws DAOException {
        log.traceEntry();
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
            this.close();
        }
        log.traceExit();
    }
    
    @Override
    protected void shutdown() throws DAOException {
        log.traceEntry();
        //reminder: this method should actually be a static method, 
        //but Java does not permit abstract static methods...
        synchronized(registeredDrivers) {
            for (Driver driver: registeredDrivers.values()) {
                try {
                    DriverManager.deregisterDriver(driver);
                    log.debug("Driver {} deregistered", driver);
                } catch (SQLException e) {
                    log.catching(e);
                    throw log.throwing(new DAOException(e));
                }
            }
            registeredDrivers.clear();
        }
        log.traceExit();
    }

    //******************************************
    // IMPLEMENT DAOManager ABSTRACT METHODS TO OBTAIN DAOs
    //******************************************
    @Override
    protected MySQLSourceDAO getNewSourceDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLSourceDAO(this));
    }
    @Override
    protected MySQLSpeciesDAO getNewSpeciesDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLSpeciesDAO(this));
    }
    @Override
    protected MySQLTaxonDAO getNewTaxonDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLTaxonDAO(this));
    }
    @Override
    protected MySQLTaxonConstraintDAO getNewTaxonConstraintDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLTaxonConstraintDAO(this));
    }
    @Override
    protected MySQLGeneOntologyDAO getNewGeneOntologyDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLGeneOntologyDAO(this));
    }
    @Override
    protected MySQLGeneDAO getNewGeneDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLGeneDAO(this));
    }
    @Override
    protected MySQLGeneXRefDAO getNewGeneXRefDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLGeneXRefDAO(this));
    }
    @Override
    protected MySQLHierarchicalGroupDAO getNewHierarchicalGroupDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLHierarchicalGroupDAO(this));
    }
    @Override
    protected MySQLStageDAO getNewStageDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLStageDAO(this));
    }
    @Override
    protected MySQLRelationDAO getNewRelationDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLRelationDAO(this));
    }
    @Override
    protected MySQLRawExpressionCallDAO getNewRawExpressionCallDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLRawExpressionCallDAO(this));
    }
    @Override
    protected MySQLGlobalExpressionCallDAO getNewGlobalExpressionCallDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLGlobalExpressionCallDAO(this));
    }
    @Override
    protected MySQLDiffExpressionCallDAO getNewDiffExpressionCallDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLDiffExpressionCallDAO(this));
    }
    @Override
    protected MySQLAnatEntityDAO getNewAnatEntityDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLAnatEntityDAO(this));
    }
    @Override
    protected ConditionDAO getNewConditionDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLConditionDAO(this));
    }
    @Override
    protected RawDataConditionDAO getNewRawDataConditionDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLRawDataConditionDAO(this));
    }
    @Override
    protected MySQLAffymetrixProbesetDAO getNewAffymetrixProbesetDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLAffymetrixProbesetDAO(this));
    }
    @Override
    protected InSituEvidenceDAO getNewInSituEvidenceDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLInSituEvidenceDAO(this));
    }
    @Override
    protected InSituExperimentDAO getNewInSituExperimentDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLInSituExperimentDAO(this));
    }
    @Override
    protected MySQLInSituSpotDAO getNewInSituSpotDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLInSituSpotDAO(this));
    }
    @Override
    protected MySQLRNASeqResultAnnotatedSampleDAO getNewRNASeqResultAnnotatedSampleDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLRNASeqResultAnnotatedSampleDAO(this));
    }
    @Override
    protected MySQLCIOStatementDAO getNewCIOStatementDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLCIOStatementDAO(this));
    }
    @Override
    protected MySQLEvidenceOntologyDAO getNewEvidenceOntologyDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLEvidenceOntologyDAO(this));
    }
    @Override
    protected MySQLSummarySimilarityAnnotationDAO getNewSummarySimilarityAnnotationDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLSummarySimilarityAnnotationDAO(this));
    }
    @Override
    protected MySQLRawSimilarityAnnotationDAO getNewRawSimilarityAnnotationDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLRawSimilarityAnnotationDAO(this));
    }
    @Override
    protected MySQLStageGroupingDAO getNewStageGroupingDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLStageGroupingDAO(this));
    }

    @Override
    protected DownloadFileDAO getNewDownloadFileDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLDownloadFileDAO(this));
    }

    @Override
    protected SpeciesDataGroupDAO getNewSpeciesDataGroupDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLSpeciesDataGroupDAO(this));
    }

	@Override
	protected KeywordDAO getNewKeywordDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLKeywordDAO(this));
	}

	@Override
	protected GeneNameSynonymDAO getNewGeneNameSynonymDAO() {
		log.traceEntry();
        return log.traceExit(new MySQLGeneNameSynonymDAO(this));
	}

    @Override
    protected SourceToSpeciesDAO getNewSourceToSpeciesDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLSourceToSpeciesDAO(this));
    }
    
    @Override
    protected GeneHomologsDAO getNewGeneHomologsDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLGeneHomologsDAO(this));
    }

    @Override
    protected SamplePValueDAO getNewSamplePValueDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLSamplePValueDAO(this));
    }

    @Override
    protected AffymetrixChipDAO getNewAffymetrixChipDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLAffymetrixChipDAO(this));
    }

    @Override
    protected MicroarrayExperimentDAO getNewMicroarrayExperimentDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLMicroarrayExperimentDAO(this));
    }

    @Override
    protected RNASeqExperimentDAO getNewRnaSeqExperimentDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLRNASeqExperimentDAO(this));
    }

    @Override
    protected RNASeqLibraryAnnotatedSampleDAO getNewRnaSeqLibraryAnnotatedSampleDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLRNASeqLibraryAnnotatedSampleDAO(this));
    }

    @Override
    protected RNASeqLibraryDAO getNewRnaSeqLibraryDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLRNASeqLibraryDAO(this));
    }
    @Override
    protected ESTLibraryDAO getNewESTLibraryDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLESTLibraryDAO(this));
    }
    @Override
    protected ESTDAO getNewESTDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLESTDAO(this));
    }
    @Override
    protected SexDAO getNewSexDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLSexDAO(this));
    }

    @Override
    protected MysqlRawDataCountDAO getNewRawDataCountDAO() {
        log.traceEntry();
        return log.traceExit(new MysqlRawDataCountDAO(this));
    }

    @Override
    protected AffymetrixChipTypeDAO getNewAffymetrixChipTypeDAO() {
        log.traceEntry();
        return log.traceExit(new MySQLAffymetrixChipTypeDAO(this));
    }
}
