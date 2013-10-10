package org.bgee.model.dao.mysql;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.mysql.datasource.InitDataSourceTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BgeeConnectionTest extends TestAncestor
{
	private final static Logger log = LogManager.getLogger(BgeeConnectionTest.class.getName());
	
	/**
	 * Default constructor.
	 */
	public BgeeConnectionTest()
	{
		super();
	}
	@Override
	protected Logger getLogger() {
		return log;
	}
	
	/**
	 * @see {@link InitDataSourceTest#initClass()}
	 */
	@BeforeClass
	public static void initClass()
	{
		InitDataSourceTest.initClass();
	}
	/**
	 * @see {@link InitDataSourceTest#unloadClass()}
	 */
	@AfterClass
	public static void unloadClass()
	{
		InitDataSourceTest.unloadClass();
	}
	
	/**
	 * @see InitDataSourceTest#init()
	 */
	@Before
	public void init()
	{
		InitDataSourceTest.init();
	}
	/**
	 * @see InitDataSourceTest#unload()
	 */
	@After
	public void unload()
	{
		InitDataSourceTest.unload();
	}
	
	/**
	 * Test the methods 
	 * {@link org.bgee.model.dao.mysql.BgeeConnection#close() BgeeConnection#close()} and 
	 * {@link org.bgee.model.dao.mysql.BgeeConnection#isClosed() BgeeConnection#isClosed()}
	 */
	@Test
	public void shouldCloseConnection() throws SQLException
	{
		//set the mocked connection
		when(InitDataSourceTest.getMockDriverUtils().getMockConnection().isClosed())
		    .thenAnswer(new Answer<Boolean>() {
				@Override
				public Boolean answer(InvocationOnMock invocation) {
			        try {
						verify((Connection) invocation.getMock()).close();
						return true;
					} catch (Throwable e) {
						return false;
					}
			    }
			});
		
		//get two connections
		BgeeConnection conn1 = BgeeDataSource.getBgeeDataSource().getConnection();
		BgeeConnection conn2 = BgeeDataSource.getBgeeDataSource().getConnection("", "");
		//close the first connection
		conn1.close();
		//check that it was correctly closed
		assertTrue("The connection was not properly closed", conn1.isClosed());
		//and that trying to get this connection again will return a new one
		assertNotEquals("A BgeeConnection was acquired after it has been closed", 
				conn1, BgeeDataSource.getBgeeDataSource().getConnection());
		//but we can still obtain the second connection
		assertEquals("Closing a BgeeConnection interfered with another one", 
				conn2, BgeeDataSource.getBgeeDataSource().getConnection("", ""));
	}
	
	/**
	 * Test the method {@link org.bgee.model.dao.mysql.BgeeConnection#prepareStatement(String) 
	 * BgeeConnection#prepareStatement(String)}
	 * @throws SQLException 
	 */
	@Test
	public void shouldPrepareStatement() throws SQLException
	{
		//get a PreparedStatement
		BgeePreparedStatement statement = 
				BgeeDataSource.getBgeeDataSource().getConnection().prepareStatement("test");
		assertNotNull("Could not acquire a BgeePreparedStatement", statement);
		
	}
}
