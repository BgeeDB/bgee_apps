package org.bgee.model.dao.mysql.connector.mock;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * A mock {@code DataSource} used for unit testing. Method calls are delegated to 
 * a mock {@code DataSource}, returns by {@link #getMockDataSource()}, which allows 
 * to verify method calls during tests. We use this class, rather than simply 
 * a mock {@code DataSource}, because we need a proper class name to test 
 * the JNDI behavior.
 * Only one {@code MockDataSource} instance should be registered at any time, because 
 * it uses static attributes that can be easily accessed by unit test methods, so 
 * no unit tests should be run in parallel. This static attributes are reinitialized 
 * each time {@link #initialize()} is called. Each unit test method or class should 
 * call {@link #initialize()} at the beginning and at the end of the test/the class, 
 * and should provide this class name as DataSource name property to {@code MySQLDAOManager}.
 * <p>
 * When the methods {@code getConnection} are called, it returns 
 * a mock {@code Connection}, that can be obtained by calling {@link #getMockConnection()} 
 * (even before {@code getConnection} is called). The same mock Connection instance 
 * is always returned, as long as {@code initialize} is not called.
 * 
 * @author Frederic Bastian
 * @author Mathieu Seppey
 * @version Bgee 13 
 * @since Bgee 13
 */
public class MockDataSource implements DataSource {
    /**
     * The name to use for {@code Context} lookup.
     */
    public static final String DATASOURCENAME = "testdatasource";
    
    /**
     * A mock {@code DataSource} which methods will be delegated to, in order to verify 
     * method calls..
     */
    public static DataSource mockDataSource;
    /**
     * The mock {@code Connection} returned by this class. Any call to  
     * {@code getConnection} methods will always return this same mock 
     * {@code Connection} instance (whatever the value of the parameters).
     */
    private static Connection mockConnection;
    
    public MockDataSource() throws SQLException {
        initialize();
    }
    
    public static void initialize() throws SQLException {
        //create the mock DataSource
        mockDataSource = mock(DataSource.class);
        mockConnection = mock(Connection.class);
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockDataSource.getConnection(any(String.class), any(String.class))).
            thenReturn(mockConnection);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return mockDataSource.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password)
            throws SQLException {
        return mockDataSource.getConnection(username, password);
    }
    
    /**
     * Return the mock {@code Connection} provided by this {@code MockDataSource}.
     * Any call to a {@code getConnection} method 
     * (whatever the value of the parameters)
     * will always return the same mock {@code Connection} instance, 
     * that can be obtain by this getter (even before 
     * {@code getConnection} is called).
     * 
     * @return the {@link #mockConnection}
     */
    public static Connection getMockConnection() {
        return mockConnection;
    }

    /**
     * @return the {@link #mockDataSource}
     */
    public static DataSource getMockDataSource() {
        return mockDataSource;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
}
