package org.bgee.model.dao.mysql.connector;

import static org.junit.Assert.*;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.concurrent.Exchanger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.mysql.TestAncestor;
import org.bgee.model.dao.mysql.connector.BgeeConnection;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.mock.MockDataSource;
import org.bgee.model.dao.mysql.connector.mock.MockDriver;
import org.bgee.model.dao.mysql.connector.mock.MockInitialContextFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


import static org.mockito.Mockito.*;

/**
 * Test the behavior of {@link MySQLDAOManager} when acquiring {@code BgeeConnection}s, 
 * either from a JDBC {@code Driver}, or a {@code DataSource}.
 * The getters and setters are also tested in this class, as the {@code Driver} or 
 * the {@code DataSource} must be available when setting parameters.
 * 
 * @author Frederic Bastian
 * @author Mathieu Seppey
 * @version Bgee 13
 * @since Bgee 13
 */
public class MySQLDAOManagerTest extends TestAncestor
{
    private final static Logger log = 
            LogManager.getLogger(MySQLDAOManagerTest.class.getName());
    
	
	/**
	 * Default Constructor. 
	 */
	public MySQLDAOManagerTest()
	{
		super();
	}
	@Override
	protected Logger getLogger() {
		return log;
	}
	
	/**
     * Create a naming service initial context in order to use a JNDI DataSource
     * 
     * @throws NamingException 
     * @throws IllegalStateException 
     */
	@BeforeClass
    public static void initInitialContext() throws IllegalStateException, NamingException{

        // Set our mock factory as initial context factory.
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                MockInitialContextFactory.class.getName());     

        // Create a reference to a datasource object...
        Reference ref = new Reference(MockDataSource.class.getName());

        // And bind it to the initial context 
        InitialContext ic = new InitialContext();
        ic.rebind(MockDataSource.DATASOURCENAME, ref);   
    } 
	
	/**
	 * Destroy the {@code InitialContextFactory} registered by {@link #initInitialContext()}.
	 */
	@AfterClass
	public static void destroyInitialContext() {
	    System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
	}
	
	/**
     * Test {@link MySQLDAOManager#connectionClosed(String)}.
     */
    @Test
    public void testConnectionClosed() throws SQLException {
        MockDriver.initialize();
        //set the properties to use it
        Properties props = new Properties();
        props.setProperty(MySQLDAOManager.JDBC_URL_KEY, MockDriver.MOCKURL);
        //test to provide several JDBC driver names
        props.setProperty(MySQLDAOManager.JDBC_DRIVER_NAMES_KEY, MockDriver.class.getName());
        //we "manually" obtain a MySQLDAOManager and set parameters, rather than 
        //going through the DAOManager#getDAOManager() method
        MySQLDAOManager manager = new MySQLDAOManager();
        manager.setParameters(props);
        
        BgeeConnection conn = manager.getConnection();
        manager.connectionClosed(conn.getId());
        //the manager should not hold this connection anymore, yet it should still 
        //be opened, as we did not call the close method of the connection
        verify(MockDriver.getMockConnection(), never()).close();
        assertNotEquals("Should get a new BgeeConnection after calling connectionClosed.", 
                conn, manager.getConnection());
        
        manager.shutdown();
        MockDriver.initialize();
    }
    /**
     * Try to set parameters and to read them.
     * @throws SQLException 
     */
    @Test
    public void shouldGetSet() throws SQLException {
        //we need to initialize the mock DataSource, because some attributes can only 
        //be set if they are valid, corresponding to real java.sql or javax.sql objects.
        MockDataSource.initialize();
        //we will try to load the mock Driver and the MySQL Driver
        Properties props = new Properties();
        props.setProperty(MySQLDAOManager.JDBC_URL_KEY, MockDriver.MOCKURL);
        props.setProperty(MySQLDAOManager.RESOURCE_NAME_KEY, MockDataSource.DATASOURCENAME);
        props.setProperty(MySQLDAOManager.JDBC_DRIVER_NAMES_KEY, 
                MockDriver.class.getName() + "," + com.mysql.jdbc.Driver.class.getName());
        props.setProperty(MySQLDAOManager.USER_KEY, "bgee.jdbc.username.test");
        props.setProperty(MySQLDAOManager.PASSWORD_KEY, "bgee.jdbc.password.test");
        props.setProperty(MySQLDAOManager.EXPR_PROPAGATION_GENE_COUNT_KEY, "20");
        
        MySQLDAOManager manager = new MySQLDAOManager();
        manager.setParameters(props);
        
        assertEquals("Incorrect JDBC URL read", MockDriver.MOCKURL, manager.getJdbcUrl());
        assertEquals("Incorrect DataSource name read", MockDataSource.DATASOURCENAME, 
                manager.getDataSourceResourceName());
        assertEquals("Incorrect JDBC Driver names read", new HashSet<String>(
                Arrays.asList(MockDriver.class.getName(), com.mysql.jdbc.Driver.class.getName())), 
                manager.getJdbcDriverNames());
        assertEquals("Incorrect username name read", "bgee.jdbc.username.test", 
                manager.getUser());
        assertEquals("Incorrect password name read", "bgee.jdbc.password.test", 
                manager.getPassword());
        assertEquals("Incorrect gene count limit", 20, manager.getExprPropagationGeneCount());
    
        manager.shutdown();
        MockDataSource.initialize();
        
    }
    
    /**
     * Test the behavior of {@link MySQLDAOManager#setParameters(Properties)} 
     * when provided with incorrect properties. 
     */
    @Test
    public void shouldSetWrongParameters() throws SQLException {
      //we need to initialize the mock DataSource, because some attributes can only 
        //be set if they are valid, corresponding to real java.sql or javax.sql objects.
        MockDataSource.initialize();
        MockDriver.initialize();
        MySQLDAOManager manager = new MySQLDAOManager();
        
        //it is mandatory to provided at least a DataSource JNDI resource name, 
        //or a JDBC Driver name with JDBC connection URL
        Properties props = new Properties();
        try {
            manager.setParameters(props);
            //if we reach this point, test failed
            throw new AssertionError("The manager should refuse properties " +
            		"with no DataSource name provided, nor any JDBC Driver name " +
            		"with JDBC Connection URL.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        
        //DataSource provided? then it should work
        props.setProperty(MySQLDAOManager.RESOURCE_NAME_KEY, MockDataSource.DATASOURCENAME);
        manager.setParameters(props);
        props.remove(MySQLDAOManager.RESOURCE_NAME_KEY);
        
        //only the JDBC Driver? The JDBC Connection URL is missing, it should fail.
        props.setProperty(MySQLDAOManager.JDBC_DRIVER_NAMES_KEY, MockDriver.class.getName());
        try {
            manager.setParameters(props);
            //if we reach this point, test failed
            throw new AssertionError("The manager should refuse properties " +
                    "with a JDBC Driver provided without a JDBC Connection URL.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        props.remove(MySQLDAOManager.JDBC_DRIVER_NAMES_KEY);
        
        //only the JDBC URL? Should fail also
        props.setProperty(MySQLDAOManager.JDBC_URL_KEY, MockDriver.MOCKURL);
        try {
            manager.setParameters(props);
            //if we reach this point, test failed
            throw new AssertionError("The manager should refuse properties " +
                    "with a JDBC URL provided without a JDBC Driver name.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        
        //let's add the Driver name, it should work
        props.setProperty(MySQLDAOManager.JDBC_DRIVER_NAMES_KEY, MockDriver.class.getName());
        manager.setParameters(props);
        //let's add a DataSource, it should still work
        props.setProperty(MySQLDAOManager.RESOURCE_NAME_KEY, MockDataSource.DATASOURCENAME);
        manager.setParameters(props);
        
        //let's set an incorrect Driver, it should fail
        props.setProperty(MySQLDAOManager.JDBC_DRIVER_NAMES_KEY, "whatever");
        try {
            manager.setParameters(props);
            //if we reach this point, test failed
            throw new AssertionError("The manager should refuse properties " +
                    "with an incorrect JDBC Driver name.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        
        //now an incorrect URL
        props.setProperty(MySQLDAOManager.JDBC_DRIVER_NAMES_KEY, MockDriver.class.getName());
        props.setProperty(MySQLDAOManager.JDBC_URL_KEY, "whatever");
        try {
            manager.setParameters(props);
            //if we reach this point, test failed
            throw new AssertionError("The manager should refuse properties " +
                    "with an incorrect JDBC connection URL.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        
        //now an incorrect DataSource resource name
        props.remove(MySQLDAOManager.JDBC_DRIVER_NAMES_KEY);
        props.remove(MySQLDAOManager.JDBC_URL_KEY);
        props.setProperty(MySQLDAOManager.RESOURCE_NAME_KEY, "whatever");
        try {
            manager.setParameters(props);
            //if we reach this point, test failed
            throw new AssertionError("The manager should refuse properties " +
                    "with an incorrect DataSource resource name.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        
        //now, incorrect gene count limit for expression propagation.
        //first, check that we correctly use the default value for now
        assertEquals("Incorrect default value of gene count limit", 
                MySQLDAOManager.DEFAULT_EXPR_PROPAGATION_GENE_COUNT, 
                manager.getExprPropagationGeneCount());
        //restore correct parameters
        props.setProperty(MySQLDAOManager.RESOURCE_NAME_KEY, MockDataSource.DATASOURCENAME);
        props.setProperty(MySQLDAOManager.EXPR_PROPAGATION_GENE_COUNT_KEY, "30");
        manager.setParameters(props);
        assertEquals("Incorrect gene count limit", 30, manager.getExprPropagationGeneCount());
        //now, set incorrect value
        props.setProperty(MySQLDAOManager.EXPR_PROPAGATION_GENE_COUNT_KEY, "20.1");
        try {
            manager.setParameters(props);
            //if we reach this point, test failed
            throw new AssertionError("The manager should refuse properties " +
                    "with an incorrect gene count limit.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        //value should have been unchanged
        assertEquals("Incorrect gene count limit", 30, manager.getExprPropagationGeneCount());
        
        //final check, let's provide valid parameters to check we didn't "block" 
        //the manager
        props.remove(MySQLDAOManager.EXPR_PROPAGATION_GENE_COUNT_KEY);
        manager.setParameters(props);
        //value should have been set to default
        assertEquals("Incorrect default value of gene count limit", 
                MySQLDAOManager.DEFAULT_EXPR_PROPAGATION_GENE_COUNT, 
                manager.getExprPropagationGeneCount());

        MockDataSource.initialize();
        MockDriver.initialize();
        manager.shutdown();
    }
    
    /**
     * Test {@link MySQLDAOManager#killDAOManager()}.
     */
    @Test
    public void shouldKillDAOManager() throws SQLException {
        MockDriver.initialize();
        //set the properties to use it
        Properties props = new Properties();
        props.setProperty(MySQLDAOManager.JDBC_URL_KEY, MockDriver.MOCKURL);
        //test to provide several JDBC driver names
        props.setProperty(MySQLDAOManager.JDBC_DRIVER_NAMES_KEY, MockDriver.class.getName());
        //we "manually" obtain a MySQLDAOManager and set parameters, rather than 
        //going through the DAOManager#getDAOManager() method
        MySQLDAOManager manager = new MySQLDAOManager();
        manager.setParameters(props);
        
        BgeeConnection conn1 = manager.getConnection();
        //get mock PreparedStatement
        conn1.prepareStatement("test");
        //get another connection
        props.setProperty(MySQLDAOManager.USER_KEY, "test");
        manager.setParameters(props);
        BgeeConnection conn2 = manager.getConnection();
        //get mock PreparedStatement
        conn2.prepareStatement("test");
        //just to be sure we get the connections we expected
        assertNotSame("Invalid BgeeConnections returned", conn1, conn2);
        
        //actual test
        manager.killDAOManager();
        //statements held by the connections should have been canceled
        verify(MockDriver.getMockStatement(), times(2)).cancel();
        //connections should have been closed
        verify(MockDriver.getMockConnection(), times(2)).close();
        
        manager.shutdown();
        MockDriver.initialize();
    }
    
    /**
     * Test {@link MySQLDAOManager#shutdown()}
     */
    @Test
    public void shouldShutdown() throws SQLException {
        MockDriver.initialize();
        
        //first we deregister any Driver already loaded
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            DriverManager.deregisterDriver(drivers.nextElement());
        }
        
        Properties props = new Properties();
        props.setProperty(MySQLDAOManager.JDBC_URL_KEY, MockDriver.MOCKURL);
        props.setProperty(MySQLDAOManager.JDBC_DRIVER_NAMES_KEY, 
                MockDriver.class.getName() + "," + com.mysql.jdbc.Driver.class.getName());
        MySQLDAOManager manager = new MySQLDAOManager();
        manager.setParameters(props);
        
        //The DriverManager should have our two Drivers loaded
        drivers = DriverManager.getDrivers();
        int myDriversFound = 0;
        while (drivers.hasMoreElements()) {
            Driver nextDriver = drivers.nextElement();
            log.trace("Driver in DriverManager: {}", nextDriver);
            if (nextDriver.getClass().getName().equals(MockDriver.class.getName()) || 
               nextDriver.getClass().getName().equals(com.mysql.jdbc.Driver.class.getName())) {
                myDriversFound++;
            }
        }
        assertEquals("The DriverManager did not registered our Drivers", 2, myDriversFound);
        
        manager.shutdown();
        
        drivers = DriverManager.getDrivers();
        myDriversFound = 0;
        while (drivers.hasMoreElements()) {
            Driver nextDriver = drivers.nextElement();
            log.trace("Driver in DriverManager: {}", nextDriver);
            if (nextDriver.getClass().getName().equals(MockDriver.class.getName()) || 
               nextDriver.getClass().getName().equals(com.mysql.jdbc.Driver.class.getName())) {
                myDriversFound++;
            }
        }
        assertEquals("The DriverManager did not deregistered our Drivers", 0, myDriversFound);
        
        MockDriver.initialize();
    }
    
    
    /**
	 * Test the acquisition of a {@code BgeeConnection} when using a JDBC {@code Driver}, 
	 * and only a connection URL (meaning username and password should be provided in the URL).
	 * 
	 * @throws SQLException
	 */
	@Test
	public void getDriverBgeeConnectionUrlOnly() throws SQLException {
        MockDriver.initialize();
        //set the properties to use it
	    Properties props = new Properties();
	    props.setProperty(MySQLDAOManager.JDBC_URL_KEY, MockDriver.MOCKURL);
	    //test to provide several JDBC driver names
	    props.setProperty(MySQLDAOManager.JDBC_DRIVER_NAMES_KEY, MockDriver.class.getName());
	    //we "manually" obtain a MySQLDAOManager and set parameters, rather than 
	    //going through the DAOManager#getDAOManager() method
		MySQLDAOManager manager = new MySQLDAOManager();
		manager.setParameters(props);
		
		BgeeConnection conn = manager.getConnection();
		//verify that there is an actual underlying real connection
		assertNotNull("Real underlying connection missing.", conn.getRealConnection());
		//verify that the correct Driver was used
		verify(MockDriver.getMockDriver()).connect(eq(MockDriver.MOCKURL), (Properties) anyObject());
		
		manager.shutdown();
        MockDriver.initialize();
	}
    
    /**
     * Test the acquisition of a {@code BgeeConnection} when using a {@code DataSource}, 
     * with default parameters (no username/password provided to the {@code getConnection} 
     * method).
     * 
     * @throws SQLException
     */
    @Test
    public void getDataSourceBgeeConnection() throws SQLException {
        MockDataSource.initialize();
        //set the properties to use it
        Properties props = new Properties();
        props.setProperty(MySQLDAOManager.RESOURCE_NAME_KEY, MockDataSource.DATASOURCENAME);
        //we "manually" obtain a MySQLDAOManager and set parameters, rather than 
        //going through the DAOManager#getDAOManager() method
        MySQLDAOManager manager = new MySQLDAOManager();
        manager.setParameters(props);
        
        BgeeConnection conn = manager.getConnection();
        //verify that there is an actual underlying real connection
        assertNotNull("Real underlying connection missing.", conn.getRealConnection());
        //verify that the DataSource was correctly used
        verify(MockDataSource.getMockDataSource()).getConnection();
        
        manager.shutdown();
        MockDataSource.initialize();
    }
    
    /**
     * Test the acquisition of a {@code BgeeConnection} when using a JDBC {@code Driver}, 
     * a connection URL, a username and a passord.
     * 
     * @throws SQLException
     */
    @Test
    public void getDriverBgeeConnectionUsernamePassword() throws SQLException {
        MockDriver.initialize();
        //set the properties to use it
        Properties props = new Properties();
        props.setProperty(MySQLDAOManager.JDBC_URL_KEY, MockDriver.MOCKURL);
        props.setProperty(MySQLDAOManager.JDBC_DRIVER_NAMES_KEY, MockDriver.class.getName());
        props.setProperty(MySQLDAOManager.USER_KEY, "bgee.jdbc.username.test");
        props.setProperty(MySQLDAOManager.PASSWORD_KEY, "bgee.jdbc.password.test");
        //we "manually" obtain a MySQLDAOManager and set parameters, rather than 
        //going through the DAOManager#getDAOManager() method
        MySQLDAOManager manager = new MySQLDAOManager();
        manager.setParameters(props);
        
        BgeeConnection conn = manager.getConnection();
        //verify that there is an actual underlying real connection
        assertNotNull("Real underlying connection missing.", conn.getRealConnection());
        //verify that the correct Driver properly used
        Properties driverProps = new Properties();
        driverProps.put("user", "bgee.jdbc.username.test");
        driverProps.put("password", "bgee.jdbc.password.test");
        //this test is based on the fact that the JDBC DriverManager add two properties 
        //when a user and a password are provided. If the DriverManager implementation 
        //changed, this test would fail.
        verify(MockDriver.getMockDriver()).connect(eq(MockDriver.MOCKURL), eq(driverProps));
        
        manager.shutdown();
        MockDriver.initialize();
    }
    
    /**
     * Unit test for {@link MySQLDAOManager#setDatabasToUse(String)}.
     */
    @Test
    public void shouldSetAlternativeDatabase() throws SQLException {
        MockDriver.initialize();
        //set the properties to use it
        Properties props = new Properties();
        props.setProperty(MySQLDAOManager.JDBC_URL_KEY, MockDriver.MOCKURL);
        //test to provide several JDBC driver names
        props.setProperty(MySQLDAOManager.JDBC_DRIVER_NAMES_KEY, MockDriver.class.getName());
        //we "manually" obtain a MySQLDAOManager and set parameters, rather than 
        //going through the DAOManager#getDAOManager() method
        MySQLDAOManager manager = new MySQLDAOManager();
        manager.setParameters(props);
        
        BgeeConnection conn = manager.getConnection();
        
        //now, set an alternative database to use
        String dbName = "alternative";
        manager.setDatabaseToUse(dbName);
        BgeeConnection conn2 = manager.getConnection();
        assertNotNull("Real underlying connection missing.", conn.getRealConnection());
        //check that the proper database name was set
        verify(conn.getRealConnection()).setCatalog(dbName);
        //it should be a different connection
        assertNotSame("A same connection was returned for different database name", 
                conn, conn2);
        
        //reset the database to use, we should get the first connection again
        manager.setDatabaseToUse(null);
        BgeeConnection conn3 = manager.getConnection();
        assertSame("Different connections were returned for same parameters", 
                conn, conn3);
        //check that the database was not incorrectly changed
        verify(conn.getRealConnection()).setCatalog(anyString());
        
        //reset the database again, we will have the second one again
        manager.setDatabaseToUse(dbName);
        BgeeConnection conn4 = manager.getConnection();
        assertSame("Different connections were returned for same parameters", 
                conn2, conn4);
        
        manager.shutdown();
        MockDriver.initialize();
    }
    
    /**
     * Test the acquisition of a {@code BgeeConnection} when using a {@code DataSource}, 
     * a username and a passord.
     * 
     * @throws SQLException
     */
    @Test
    public void getDataSourceBgeeConnectionUsernamePassword() throws SQLException {
        MockDataSource.initialize();
        //set the properties to use it
        Properties props = new Properties();
        props.setProperty(MySQLDAOManager.RESOURCE_NAME_KEY, MockDataSource.DATASOURCENAME);
        props.setProperty(MySQLDAOManager.USER_KEY, "bgee.jdbc.username.test");
        props.setProperty(MySQLDAOManager.PASSWORD_KEY, "bgee.jdbc.password.test");
        //we "manually" obtain a MySQLDAOManager and set parameters, rather than 
        //going through the DAOManager#getDAOManager() method
        MySQLDAOManager manager = new MySQLDAOManager();
        manager.setParameters(props);
        
        BgeeConnection conn = manager.getConnection();
        //verify that there is an actual underlying real connection
        assertNotNull("Real underlying connection missing.", conn.getRealConnection());
        //verify that the DataSource was correctly used
        verify(MockDataSource.getMockDataSource()).getConnection(
                "bgee.jdbc.username.test", "bgee.jdbc.password.test");
        
        manager.shutdown();
        MockDataSource.initialize();
    }
    

    /**
     * Test the behavior of {@link MySQLDAOManager#getConnection()}.
     */
    @Test
    public void shouldGetAndReleaseConnections() throws SQLException {

        MockDriver.initialize();

        //set the properties to use it
        final Properties props = new Properties();
        props.setProperty(MySQLDAOManager.JDBC_URL_KEY, MockDriver.MOCKURL);
        //test to provide several JDBC driver names
        props.setProperty(MySQLDAOManager.JDBC_DRIVER_NAMES_KEY, MockDriver.class.getName());
        
        /**
         * An anonymous class to acquire {@code MySQLDAOManager}s 
         * from a different thread than this one, 
         * and to be run alternatively to the main thread.
         */
        class ThreadTest extends Thread {
            public volatile MySQLDAOManager manager1;
            public volatile BgeeConnection conn1;
            public volatile BgeeConnection conn2;
            public volatile boolean exceptionThrown = false;
            /**
             * An {@code Exchanger} that will be used to run threads alternatively. 
             */
            public final Exchanger<Integer> exchanger = new Exchanger<Integer>();
            
            @Override
            public void run() {
                try {
                    //acquire a BgeeDataSource
                    manager1 = new MySQLDAOManager();
                    manager1.setParameters(props);
                    //acquire 2 different connection
                    conn1 = manager1.getConnection();
                    props.setProperty(MySQLDAOManager.USER_KEY, "test");
                    manager1.setParameters(props);
                    conn2 = manager1.getConnection();
                    props.remove(MySQLDAOManager.USER_KEY);
                    manager1.setParameters(props);
                    
                    //main thread's turn
                    this.exchanger.exchange(null);
                    //wait for this thred's turn
                    this.exchanger.exchange(null);
                    
                    //try to get a connection again. It should not fail, 
                    //as the BgeeDataSource of this thread was not closed
                    manager1.getConnection();
                    
                    //main thread's turn
                    this.exchanger.exchange(null);
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (SQLException e) {
                    exceptionThrown = true;
                }
            }
        }
        
        try {
            //get a BgeeDataSource in the main thread
            MySQLDAOManager manager1 = new MySQLDAOManager();

            //acquire 2 different connections
            manager1.setParameters(props);
            BgeeConnection conn1 = manager1.getConnection();
            
            props.setProperty(MySQLDAOManager.USER_KEY, "test");
            manager1.setParameters(props);
            BgeeConnection conn2 = manager1.getConnection();
            props.remove(MySQLDAOManager.USER_KEY);
            manager1.setParameters(props);
            
            //launch a second thread also acquiring BgeeConnections
            ThreadTest test = new ThreadTest();
            test.start();
            //wait for this thread's turn
            test.exchanger.exchange(null);
            //check that no exception was thrown in the second thread 
            if (test.exceptionThrown) {
                throw new SQLException("A SQLException occurred in the second thread.");
            }
            
            //the two connection in the main thread should be different
            assertNotNull("Failed to acquire a BgeeConnection", conn1);
            assertNotNull("Failed to acquire a BgeeConnection", conn2);
            assertNotEquals("A same thread acquire a same BgeeConnection instance " + 
                    "for different parameters", conn1, conn2);
            
            //as well as in the second thread
            assertNotNull("Failed to acquire a BgeeConnection", test.conn1);
            assertNotNull("Failed to acquire a BgeeConnection", test.conn2);
            assertNotEquals("A same thread acquire a same BgeeConnection instance " + 
                    "for different parameters", test.conn1, test.conn2);
            
            //the connections with the same parameters should be different in different threads
            assertNotEquals("Two threads acquire a same BgeeConnection instance ", 
                    conn1, test.conn1);
            assertNotEquals("Two threads acquire a same BgeeConnection instance ", 
                    conn2, test.conn2);
            
            //trying to acquire connections with the same parameters should return the same connection
            assertEquals("Get two BgeeConnection instances for the same parameters", 
                    conn1, manager1.getConnection());
            
            props.setProperty(MySQLDAOManager.USER_KEY, "test");
            manager1.setParameters(props);
            assertEquals("Get two BgeeConnection instances for the same parameters", 
                    conn2, manager1.getConnection());
            
            props.remove(MySQLDAOManager.USER_KEY);
            manager1.setParameters(props);
            assertEquals("Get two BgeeConnection instances for the same parameters", 
                    conn1, manager1.getConnection());
            
            //close the source in the main thread
            manager1.closeDAOManager();

            //close() should have been called on the mocked connection exactly two times 
            //(because we opened two connections in this thread)
            verify(MockDriver.getMockConnection(), times(2)).close();
            
            //relaunch the other thread to check if it can still acquire connections, 
            //as its BgeeDataSource was not closed
            test.exchanger.exchange(null);
            //wait for this thread's turn
            test.exchanger.exchange(null);
            //check that no exception was thrown in the second thread 
            if (test.exceptionThrown) {
                throw new SQLException("A SQLException occurred in the second thread.");
            }
            
            //try to get a Connection with same parameters again, 
            //it should be a new one now the MySQLDAOManager was closed
            assertNotNull("Failed to acquire a BgeeConnection", manager1.getConnection());
            assertNotEquals("Should get a new BgeeConnection after closing.", 
                    conn1, manager1.getConnection());
            
            //close the BgeeDataSource one by one without calling closeAll(), 
            //that would make other test to fail
            manager1.closeDAOManager();
            test.manager1.closeDAOManager();
            
            manager1.shutdown();
            MockDriver.initialize();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } 

    }
    
    @Test
    public void shouldReleaseDAOManager() throws SQLException {
        MockDriver.initialize();

        //set the properties to use it
        final Properties props = new Properties();
        props.setProperty(MySQLDAOManager.JDBC_URL_KEY, MockDriver.MOCKURL);
        //test to provide several JDBC driver names
        props.setProperty(MySQLDAOManager.JDBC_DRIVER_NAMES_KEY, MockDriver.class.getName());
        
        //get a BgeeDataSource in the main thread
        MySQLDAOManager manager1 = new MySQLDAOManager();

        //acquire 2 different connections
        manager1.setParameters(props);
        BgeeConnection conn1 = manager1.getConnection();
        manager1.releaseResources();
        BgeeConnection conn2 = manager1.getConnection();
        assertNotSame("connection was not closed", conn1, conn2);
        
        assertFalse("manager incorrectly closed", manager1.isClosed());

        MockDriver.initialize();
    }
}
