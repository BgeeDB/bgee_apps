package org.bgee.model.dao.mysql;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

/**
 * At instantiation, this class automatically registers a mock {@code Driver} 
 * to the {@code DriverManager}, that accepts the URL {@link #MOCKURL}.
 * When the method {@code connect(String, Properties)} is called 
 * on this mock {@code Driver} , it returns a mock {@code Connection}, 
 * that can be obtained by calling {@link #getMockConnection()} (even before 
 * {@code Driver.connect(String, Properties)} is called). 
 * Any call to {@code Driver.connect(String, Properties)} 
 * (whatever the value of the parameters)
 * will always return the same mock {@code Connection} instance.
 * <p>
 * Any call to the mocked connection prepareStatement method
 * will return a mock {@code PreparedStatement}
 * <p>
 * During unit testing, developers should then simply obtain 
 * this mock {@code Connection}, and define some mock methods. 
 * <p>
 * When done using this {@code MockDriverUtils} object, 
 * a call to {@link #deregister()} must be made.
 * 
 * @author Frederic Bastian
 * @author Mathieu Seppey
 * @version Bgee 13, May 2013
 * @since Bgee 13
 */
public class MockDriverUtils 
{
	/**
	 * A {@code String} representing the URL that the mock {@code Driver} 
	 * will accept.
	 */
	public final static String MOCKURL = "jdbc:mock:test";
	/**
	 * The mock {@code Driver} that this class is responsible to register 
	 * to the {@code DriverManager}.
	 */
	private final Driver mockDriver;
	/**
	 * The mock {@code Connection} returned by the mock Driver registered 
	 * by this class. Any call to {@code Driver.connect(String, Properties)} 
	 * will always return this same mock {@code Connection} instance 
	 * (whatever the value of the parameters).
	 */
	private final Connection mockConnection;
	
	/**
	 * Constructor that create a mock {@code Driver} and register it 
	 * to the {@code DriverManager}. By default, this mock {@code Driver} 
	 * returns a mock {@code Connection} 
	 * when {@code Driver.connect(String, Properties)} is called 
	 * (always the same {@code Connection} instance, whatever the calls 
	 * to {@code connect}).
	 * <p>
	 * When you don't need to use this {@code MockDriverUtils} anymore, 
	 * you must call {@link #deregister()}.
	 * <p>
	 * The mock {@code Driver} can be obtained through the use of 
	 * {@link #getMockDriver()}.
	 * The mock {@code Connection} can be obtained through the use of 
	 * {@link #getMockConnection()} (even before any call to {@code connect} is made).
	 */
	public MockDriverUtils() 
    {
		Connection mockConnectionTemp = null;
		Driver mockDriverTemp         = null;
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

			//register the mock Driver
			DriverManager.registerDriver(mockDriver);
			mockDriverTemp = mockDriver;
		} catch (SQLException e) {
			//do nothing. The only method that could throw an actual exception 
			//is DriverManager.registerDriver, and in that case, the Driver 
			//will not be available for the application anyway
		}
		this.mockDriver     = mockDriverTemp;
		this.mockConnection = mockConnectionTemp;
    }
	
	/**
	 * Deregister this mock {@code Driver} from the {@code DriverManager}.
	 */
	public void deregister() 
	{
    	try {
			DriverManager.deregisterDriver(this.getMockDriver());
		} catch (SQLException e) {
			//I don't think we should care about this during unit testing. 
		}
	}

	/**
	 * Return the mock {@code Connection} provided by the mock {@code Driver}.
	 * Any call to {@code Driver.connect(String, Properties)} 
     * (whatever the value of the parameters)
     * will always return the same mock {@code Connection} instance, 
     * that can be obtain by this getter (even before 
     * {@code Driver.connect(String, Properties)} is called).
     * 
	 * @return the {@link #mockConnection}
	 */
	public Connection getMockConnection() {
		return this.mockConnection;
	}

	/**
	 * @return the {@link #mockDriver}
	 */
	public Driver getMockDriver() {
		return mockDriver;
	}
}
