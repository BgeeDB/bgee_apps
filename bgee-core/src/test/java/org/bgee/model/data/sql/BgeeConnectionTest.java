package org.bgee.model.data.sql;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.TestAncestor;
import org.bgee.model.data.sql.datasource.InitDataSourceTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

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
	 * {@link org.bgee.model.data.sql.BgeeConnection#close() BgeeConnection#close()} and 
	 * {@link org.bgee.model.data.sql.BgeeConnection#isClosed() BgeeConnection#isClosed()}
	 */
	@Test
	public void shouldCloseConnection() throws SQLException
	{
		//set the mocked connection
		when(InitDataSourceTest.getMockDriverUtils().getMockConnection()
				.isClosed()).thenAnswer(new Answer<Boolean>() {
					@Override
					public Boolean answer(InvocationOnMock invocation) {
				         //test if close() has been called
						return false;
				     }
				});
		
		//get a connection
		BgeeConnection conn = BgeeDataSource.getBgeeDataSource().getConnection();
		//close it
		conn.close();
		//check that it was correctly closed
	}
}
