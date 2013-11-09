package org.bgee.model.dao.mysql;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.mysql.connector.BgeeConnection;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.test.jdbc.JdbcTestUtils;

/**
 * Super class of all classes performing integration tests on a real MySQL database. 
 * The following conditions must be met (the take home message is: if you run 
 * these integration tests through Maven, you have nothing to take care of): 
 * 
 * <h3>System property configuration</h3>
 * The following System properties must be provided in order to perform the tests 
 * (these properties would be usually set by Maven through configuration of 
 * the {@code pom.xml} file, but you can provide them as you wish): 
 * <ul>
 * <li>Property associated to the key {@link #POPULATEDDBKEY} to specify 
 * the name of the test database populated with test data, to run integration tests 
 * of SELECT statements. 
 * <li>Property associated to the key {@link #EMPTYDBKEY} to specify 
 * the name of the empty test database, used to run independent integration tests 
 * of INSERT statements. Tests that might overlap, using the same tables, 
 * should create and drop their own database instance (see 
 * {@link #createAndUseDatabase(String)} and {@link #dropDatabase(String)}).
 * <li>Property associated to the key {@link #SCHEMAFILEKEY} to specify 
 * the path to the SQL file allowing to create the Bgee database. This will be used 
 * to create independent instances of the database to perform insertion tests.
 * <li>Property associated to the key {@link MySQLDAOManager#JDBCDRIVERNAMESKEY} 
 * to specify the class names of the JDBC {@code Driver}s to use.
 * <li>Property associated to the key {@link MySQLDAOManager#JDBCURLKEY} to provide 
 * the JDBC connection URL.
 * <li>Property associated to the key {@link MySQLDAOManager#USERKEY} to specify 
 * the username to connect as root to the database. Or it can be provided in the 
 * connection URL.
 * <li>Property associated to the key {@link MySQLDAOManager#PASSWORDKEY} to specify 
 * the password to connect as root to the database. Or it can be provided in the 
 * connection URL.
 * </ul>
 * 
 * <h3>Test databases requested</h3>
 * The integration tests for SELECT statements will assume that it exists a database 
 * (with the name provided through System property associated to the key 
 * {@link #POPULATEDDBKEY}) that already contains the expected 
 * test data. The dump containing the test data to load is located at 
 * {@code src/test/resources/sql/testDataDump.sql}. 
 * This database will be automatically created and populated before the tests 
 * if you run the tests through Maven, as the {@code maven-failsafe-plugin} 
 * is configured to do it. (and it will be automatically dropped after the tests 
 * as well). This behavior can be modified, see {@code bgee-applicatioons/pom.xml}.
 * <p>
 * The integration tests for INSERT statements will assume that it exists an empty 
 * database, with only tables already created (with the name provided through 
 * System property associated to the key {@link #EMPTYDBKEY}). 
 * This database will be automatically created before the tests and dropped after 
 * the tests if you run the tests through Maven. This behavior can be modified, 
 * see {@code bgee-applicatioons/pom.xml}.
 * 
 * <h3>Cleaning after tests</h3>
 * If some tests failed, the databases created for running the tests might not have 
 * been dropped. You should drop all databases starting with {@link #DBNAMEPREFIX}.
 * This will be done automatically if you run these tests through Maven, as the 
 * {@code maven-failsafe-plugin} is configured to do it.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public abstract class MySQLITAncestor extends TestAncestor{
    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(MySQLITAncestor.class.getName());
    
    /**
     * A {@code String} that is the key to retrieve from the System properties 
     * the name of the test Bgee database populated with test data, in order 
     * to run integration tests of SELECT statements.
     */
    protected static final String POPULATEDDBKEYKEY = "bgee.database.test.select.name";
    /**
     * A {@code String} that is the key to retrieve from the System properties 
     * the name of the empty test Bgee database, used to run integration tests 
     * of independent INSERT statements, using different tables. INSERT integration 
     * tests that might be overlapping should create and drop their own database 
     * instance using the methods {@link #createAndUseDatabase(String)} and 
     * {@link #dropDatabase(String)}.
     */
    protected static final String EMPTYDBKEY = "bgee.database.test.insert.name";
    
    /**
     * A {@code String} that is the key to retrieve from the System properties 
     * the path to the SQL file containing the schema of the Bgee database, 
     * to create it. This is used to create independent databases for insertion tests.
     */
    protected static final String SCHEMAFILEKEY = "bgee.database.file.schema";
    
    /**
     * A {@code String} that the prefix to append to the name of any database 
     * created using {@link #createDatabase(String)}. This is to ensure that all 
     * databases created by these integration tests will be deleted after execution 
     * of the tests, even if an error occurs during the tests. This property is 
     * hardcoded, rather than provided through System properties, to be sure 
     * it cannot be changed, which could result in unexpected database deletions.
     */
    protected static final String DBNAMEPREFIX = "bgeeIntegrationTest_";
    
    /**
     * Default constructor. Checks that mandatory System properties are provided.
     * Does not check properties that should be used by the MySQLDAOManager, 
     * it will do it itself. If a property is missing, an {@code IllegalStateException} 
     * will be thrown.
     * @throws IllegalStateException    If a needed System property is missing.
     */
    public MySQLITAncestor() {
        super();
        //actually there are no more properties to be checked as this point 
        //(it used to be).
        //if some of the existing properties are missing, then the tests will 
        //just fail, so we do not check.
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Create an instance of the Bgee database with the name {@code dbName}, and configure  
     * the {@code DAOManager} to use this database. The path to the file containing 
     * the Bgee schema should be provided in a System property associated to the key 
     * {@link #SCHEMAFILEKEY}.
     * <p>
     * This method is used by tests of insertion operations, to create independent 
     * databases, to avoid collision of the tests. After performing the tests, 
     * the caller should use {@code dropDatabase(String)}.
     * <p> 
     * This method will append to the start of {@code dbName} the prefix 
     * {@link #DBNAMEPREFIX}, to create the database. 
     * This is to ensure that all databases created for test purpose will be deleted 
     * after execution of the tests, even if an error occurs during the tests. 
     * It is not necessary to append the prefix when calling {@code dropDatabase(String)}. 
     * This modification should be invisible to callers.
     * 
     * @param dbName    A {@code String} that is the name of the database to create.
     * @throws SQLException                 If an error occurred while creating 
     *                                      the database.
     */
    protected void createAndUseDatabase(String dbName) throws SQLException  {
        log.entry(dbName);
        
        String testDbName = this.getTestDbName(dbName);
        //it is the responsibility of the client running the code to make sure 
        //that System properties have been configured properly to obtain a MySQLDAOManager, 
        //with no database provided in the JDBC connection URL
        MySQLDAOManager manager = (MySQLDAOManager) DAOManager.getDAOManager();
        BgeeConnection con = manager.getConnection();
        
        //drop, create, and use the database
        //I don-t know why, but I can't use a prepared statement for database commands
        BgeePreparedStatement stmt = con.prepareStatement("drop database if exists " + testDbName);
        stmt.executeUpdate();
        stmt = con.prepareStatement("create database " + testDbName);
        stmt.executeUpdate();
        //we close this connection as we are going to change the database used
        con.close();
        manager.setDatabaseToUse(testDbName);
        
        //use of the Spring framework to execute .sql scripts.
        
        //First, we need a DataSource to provide to the Spring framework
        DataSource dataSource = new SingleConnectionDataSource(
                manager.getConnection().getRealConnection(), false);
        
        //run the scripts
        JdbcTemplate template = new JdbcTemplate(dataSource);
        //create db
        Resource resource = new FileSystemResource(System.getProperty(SCHEMAFILEKEY));
        JdbcTestUtils.executeSqlScript(template, resource, false);
        
        log.exit();
    }
    
    /**
     * Drop the database named {@code dbName} created for integration tests, and 
     * configure the {@code DAOManager} to stop using this database.
     * 
     * @param dbName        A {@code String} that is the name of the database to drop.
     * @throws SQLException If an error occurs while deleting the database.
     */
    protected void dropDatabase(String dbName) throws SQLException {
        log.entry(dbName);
        
        MySQLDAOManager manager = (MySQLDAOManager) DAOManager.getDAOManager();
        BgeeConnection con = manager.getConnection();
        //I don't know why but I can't use prepared statement for database commands.
        BgeePreparedStatement stmt = con.prepareStatement("Drop database " + 
            this.getTestDbName(dbName));
        stmt.executeUpdate();
        //we close this connection as we are going to change the database used
        con.close();
        manager.setDatabaseToUse(null);
        
        log.exit();
    }
    
    /**
     * Get the modified name of {@code dbName}, that allows to retrieve all databases 
     * used for tests, and to ensure that they will be dropped.
     * 
     * @param dbName    A {@code String} that is the original requested name of a database.
     * @return          A {@code String} that is a modified version of {@code dbName}.
     */
    private String getTestDbName(String dbName) {
        return DBNAMEPREFIX + dbName;
    }
}
