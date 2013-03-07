package org.bgee.model.data.sql;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import static org.mockito.Mockito.*;

/**
 * At instantiation, this class automatically registers a mock <code>Driver</code> 
 * to the <code>DriverManager</code>, that accepts the URL {@link #MOCKURL}.
 * When the method <code>connect(String, Properties)</code> is called 
 * on this mock <code>Driver</code> , it returns a mock <code>Connection</code>, 
 * that can be obtained by calling {@link #getMockConnection()}.
 * <p>
 * During unit testing, developers should then simply obtain 
 * this mock <code>Connection</code>, and define some mock methods. 
 * <p>
 * A convenient mock <code>PreparedStatement</code> can be obtained by calling 
 * {@link #getMockPreparedStatement()}. This <code>PreparedStatement</code> is the one 
 * returned when calling <code>preparedStatement(String)</code> 
 * on the mock <code>Connection</code>.
 * <p>
 * When done using this <code>MockDriver</code> object, call {@link #deregister()}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13, Mar 2013
 * @since Bgee 13
 */
public class MockDriver 
{
	/**
	 * A <code>String</code> representing the URL that the mock <code>Driver</code> 
	 * will accept.
	 */
	public final String MOCKURL = "jdbc:mock:test";
	/**
	 * The mock <code>Driver</code> that this class is responsible to register 
	 * to the <code>DriverManager</code>.
	 */
	private Driver mockDriver;
	/**
	 * The mock <code>Connection</code> returned by the mock Driver registered 
	 * by this class. 
	 */
	private Connection mockConnection;
	/**
	 * Convenient mock <code>PreparedStatement</code>. 
	 * When <code>preparedStatement(String)</code> is called on {@link mockConnection}, 
	 * return this <code>mockPrepStmt</code>
	 */
	private PreparedStatement mockPrepStmt;
	
	public MockDriver() throws SQLException
    {
		//create the mock Driver
    	Driver mockDriver = mock(Driver.class);
    	when(mockDriver.acceptsURL(MOCKURL)).thenReturn(true);
    	//will return a mock Connection, that unit tests will use
    	this.setMockConnection(mock(Connection.class));
    	when(mockDriver.connect(MOCKURL, any(Properties.class)))
                .thenReturn(this.getMockConnection());
    	//this mock connection will returned a mock preparedStatement 
    	//when preparedStatement(String) is called
    	this.setMockPrepStmt(mock(PreparedStatement.class));
    	when(this.getMockConnection().prepareStatement(anyString()))
    	        .thenReturn(this.getMockPrepStmt());
    	
    	//register the mock Driver
    	DriverManager.registerDriver(mockDriver);
    	this.setMockDriver(mockDriver);
    }
	
	/**
	 * Deregister this mock <code>Driver</code> from the <code>DriverManager</code>
	 */
	public void deregister() throws SQLException
	{
    	DriverManager.deregisterDriver(this.getMockDriver());
	}

	/**
	 * @return the {@link #mockConnection}
	 */
	public Connection getMockConnection() {
		return this.mockConnection;
	}
	/**
	 * @param mockConnection A <code>Connection</code> to set {@link #mockConnection} 
	 */
	private void setMockConnection(Connection mockConnection) {
		this.mockConnection = mockConnection;
	}

	/**
	 * @return the {@link #mockPrepStmt}
	 */
	public PreparedStatement getMockPrepStmt() {
		return this.mockPrepStmt;
	}
	/**
	 * @param mockPrepStmt A <code>PreparedStatement</code> to set {@link #mockPrepStmt} 
	 */
	private void setMockPrepStmt(PreparedStatement mockPrepStmt) {
		this.mockPrepStmt = mockPrepStmt;
	}

	/**
	 * @return the {@link #mockDriver}
	 */
	private Driver getMockDriver() {
		return mockDriver;
	}
	/**
	 * @param mockDriver A <code>Driver</code> to set {@link #mockDriver} 
	 */
	private void setMockDriver(Driver mockDriver) {
		this.mockDriver = mockDriver;
	}
}
