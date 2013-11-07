package org.bgee.model.dao.mysql;

import java.sql.SQLException;
import java.util.Properties;
import java.util.ServiceConfigurationError;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.mysql.connector.BgeeConnection;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.jdbc.JdbcTestUtils;

/**
 * Super class of all classes performing integration tests on a real MySQL database.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public abstract class MySQLITAncestor extends TestAncestor{
    private final static Logger log = LogManager.getLogger(MySQLITAncestor.class.getName());
    
    /**
     * A {@code String} defining the key to use to retrieve from System properties 
     * the class name of the real JDBC Driver used for connecting to the database 
     * (becaise several Driver names can be provided to the Bgee application, we need 
     * to know the real one). Note that the property key used by the real Bgee application 
     * is different ("bgee.dao.jdbc.driver.name<strong>s</strong>")
     */
    private final static String REALJDBCDRIVERNAMEPROP = "bgee.dao.jdbc.driver.name";
    
    /**
     * Default constructor.
     */
    public MySQLITAncestor() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    protected void createDatabase(String dbName, boolean constraints, boolean indexes, 
            boolean foreignKeys) throws IllegalStateException, SQLException, 
            ServiceConfigurationError {
        log.entry(dbName, constraints, indexes, foreignKeys);
        
        //it is the responsibility of the client running the code to make sure 
        //that System properties have been configured properly to obtain a MySQLDAOManager.
        MySQLDAOManager manager = (MySQLDAOManager) DAOManager.getDAOManager();
        BgeeConnection con = manager.getConnection();
        
        //create and use the database
        BgeePreparedStatement stmt = con.prepareStatement("Create database ?");
        stmt.setString(1, dbName);
        con.getRealConnection().setCatalog(dbName);
        //we modify the properties used by the manager to use the database created.
        //we assume that the Connection URL is of the form 
        //jdbc:mysql://[host][,failoverhost...][:port]/
        //with no parameters after, so that we can simply append the database name.
        Properties props = new Properties();
        props.setProperty(MySQLDAOManager., value)
        System.setProperty(MySQLDAOManager.JDBCURLKEY, manager.getJdbcUrl() + dbName);
        manager.setParameters(System.getProperties());
        
        //use of the Spring framework to execute .sql scripts.
        
        //First, we need a DataSource to provide to Spring framework
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        //the real JDBC Driver class name should have been provided as a System property 
        //for integration tests (because we can provide several Drivers to the MySQLDAOManager)
        dataSource.setDriverClassName(System.getProperty(REALJDBCDRIVERNAMEPROP));
        //other parameters should have been captured by the MySQLDAOManager
        dataSource.setUrl(manager.getJdbcUrl());
        dataSource.setUsername(manager.getUser());
        dataSource.setPassword(manager.getPassword());
        dataSource.setConnectionProperties(connectionProperties)
        
        //run the scripts
        JdbcTemplate template = new JdbcTemplate(dataSource);
        Resource resource = new FileSystemResource("");
        JdbcTestUtils.executeSqlScript(template, resource, false);
        
        log.exit();
    }
}
