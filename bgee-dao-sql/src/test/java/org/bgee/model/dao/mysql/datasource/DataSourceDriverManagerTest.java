package org.bgee.model.dao.mysql.datasource;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.TestAncestor;
import org.bgee.model.dao.mysql.BgeeDataSource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the loading of {@link org.bgee.mode.data.BgeeDataSource BgeeDataSource} 
 * when using a {@code DriverManager} to acquire a {@code Connection}. 
 * The loading when using a {@code DataSource} is tested in a separated class: 
 * {@code BgeeDataSource} parameters are set only once at class loading, 
 * so only once for a given {@code ClassLoader}, so it has to be done 
 * in different classes. See {@link JNDIDataSourceIntegrationTest}.
 * 
 * @author Frederic Bastian
 * @author Mathieu Seppey
 * @version Bgee 13, May 2013
 * @see JNDIDataSourceIntegrationTest
 * @since Bgee 13
 */
public class DataSourceDriverManagerTest extends TestAncestor
{
    private final static Logger log = LogManager.getLogger(DataSourceDriverManagerTest.class.getName());
	
	/**
	 * Default Constructor. 
	 */
	public DataSourceDriverManagerTest()
	{
		super();
	}
	@Override
	protected Logger getLogger() {
		return log;
	}
	
	/**
	 * @throws Exception 
	 * @see {@link InitDataSourceTest#initClass()}
	 */
	@BeforeClass
	public static void initClass() throws Exception
	{
		InitDataSourceTest.initClass();
		InitDataSourceTest.initInitialContext();
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
	 * Test the acquisition of a {@code BgeeDataSource} and 
	 * of a {@code BgeeConnection}.
	 * 
	 * @throws SQLException
	 */
	@Test
	public void getDataSourceAndConnection() throws SQLException
	{		
		//get a BgeeDataSource
		BgeeDataSource sourceTest = BgeeDataSource.getBgeeDataSource();
		//get a BgeeConnection
		sourceTest.getConnection();
	}
}
