package org.bgee.model.dao.mysql.mock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * A mock {@code Driver} used for unit testing. Method calls are delegated to 
 * a mock {@code Driver}, returns by {@link #getMockDriver()}, which allows 
 * to verify method calls during tests. We use this class, rather than simply 
 * a mock {@code Driver}, because we need a proper class name to provide 
 * to {@code MySQLDAOManager}, as it always tries to register the {@code Driver}.
 * Only one {@code MockDriver} instance should be registered at any time, because 
 * it uses static attributes that can be easily accessed by unit test methods, so 
 * no unit tests should be run in parallel. This static attributes are reinitialized 
 * each time {@link #initialize()} is called. Each unit test method or class should 
 * call {@link #initialize()} at the beginning and at the end of the test/the class, 
 * and should provide this class name as driver name property to {@code MySQLDAOManager}.
 * <p>
 * This {@code MockDriver} accepts the URL {@link #MOCKURL}.
 * When the method {@code connect(String, Properties)} is called, it returns 
 * a mock {@code Connection}, that can be obtained by calling {@link #getMockConnection()} 
 * (even before {@code Driver.connect(String, Properties)} is called). 
 * Any call to {@code Driver.connect(String, Properties)}  (whatever the value 
 * of the parameters) will always return the same mock {@code Connection} instance.
 * <p>
 * Any call to the mocked connection prepareStatement method
 * will return a mock {@code PreparedStatement}.
 * <p>
 * During unit testing, developers should then simply obtain 
 * this mock {@code Connection}, and define some mock methods. 
 * 
 * @author Frederic Bastian
 * @author Mathieu Seppey
 * @version Bgee 13 
 * @since Bgee 13
 */
public class MockDriver implements Driver {
    /**
     * A {@code String} representing the URL that the mock {@code Driver} 
     * will accept.
     */
    public final static String MOCKURL = "jdbc:mock:test";
    /**
     * A mock {@code Driver} which methods will be delegated to, in order to verify 
     * method calls..
     */
    private static Driver mockDriver;
    /**
     * The mock {@code Connection} returned by this class. Any call to  
     * {@code Driver.connect(String, Properties)} will always return this same mock 
     * {@code Connection} instance (whatever the value of the parameters).
     */
    private static Connection mockConnection;
    /**
     * The mock {@code PreparedStatement} returned by the {@link #mockConnection}
     */
    private static PreparedStatement mockStatement;
    
    /**
     * Default constructor.
     * @throws SQLException 
     */
    public MockDriver() throws SQLException {
        DriverManager.registerDriver(this);
    }
    
    public static void initialize() {
        Connection mockConnectionTemp  = null;
        Driver mockDriverTemp          = null;
        PreparedStatement mockStmtTemp = null;
        try {
            //create the mock Driver
            Driver mockDriver = mock(Driver.class);
            when(mockDriver.acceptsURL(eq(MOCKURL))).thenReturn(true);
            
            //will return a mock Connection, that unit tests will use.
            //all calls to the connect method will return the same mock Connection instance.
            mockConnectionTemp = mock(Connection.class);
            when(mockDriver.connect(eq(MOCKURL), any(Properties.class)))
                .thenReturn(mockConnectionTemp);

            // Mock a preparedStatement
            PreparedStatement mockPrep = mock(PreparedStatement.class);
            when(mockConnectionTemp.prepareStatement(any(String.class)))
            .thenReturn(mockPrep);

            mockDriverTemp = mockDriver;
            mockStmtTemp = mockPrep;
        } catch (SQLException e) {
            //do nothing. The only method that could throw an actual exception 
            //is DriverManager.registerDriver, and in that case, the Driver 
            //will not be available for the application anyway
        }
        mockDriver     = mockDriverTemp;
        mockConnection = mockConnectionTemp;
        mockStatement  = mockStmtTemp;
    }

    /**
     * Return the mock {@code Connection} provided by this {@code MockDriver}.
     * Any call to {@code Driver.connect(String, Properties)} 
     * (whatever the value of the parameters)
     * will always return the same mock {@code Connection} instance, 
     * that can be obtain by this getter (even before 
     * {@code Driver.connect(String, Properties)} is called).
     * 
     * @return the {@link #mockConnection}
     */
    public static Connection getMockConnection() {
        return mockConnection;
    }

    /**
     * Gets the mock {@code PreparedStatement} returned by the mock 
     * {@code Connection}.
     */
    public static PreparedStatement getMockStatement() {
        return mockStatement;
    }

    /**
     * @return the {@link #mockDriver}
     */
    public static Driver getMockDriver() {
        return mockDriver;
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException{
        return mockDriver.acceptsURL(url);
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        return mockDriver.connect(url, info);
    }

    @Override
    public int getMajorVersion()
    {
        return 1;
    }

    @Override
    public int getMinorVersion()
    {
        return 0;
    }

    @Override
    public boolean jdbcCompliant()
    {
        return true;
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException
    {
        return new DriverPropertyInfo[0];
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException
    {
        return null;
    }
}
