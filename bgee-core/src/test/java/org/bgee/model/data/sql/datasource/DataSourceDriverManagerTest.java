package org.bgee.model.data.sql.datasource;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.TestAncestor;
import org.bgee.model.data.sql.BgeeDataSource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the loading of {@link org.bgee.mode.data.BgeeDataSource BgeeDataSource} 
 * when using a <code>DriverManager</code> to acquire a <code>Connection</code>. 
 * The loading when using a <code>DataSource</code> is tested in a separated class: 
 * <code>BgeeDataSource</code> parameters are set only once at class loading, 
 * so only once for a given <code>ClassLoader</code>, so it has to be done 
 * in different classes. See {@link DataSourceJNDITest}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13, Mar 2013
 * @see DataSourceJNDITest
 * @since Bgee 13
 */
public class DataSourceDriverManagerTest extends TestAncestor
{
    private final static Logger log = LogManager.getLogger(DataSourceDriverManagerTest.class.getName());
    private final static DataSourceTest dataSourceTest = new DataSourceTest();
	
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
	 * @see {@link DataSourceTest#initClass()}
	 */
	@BeforeClass
	public static void initClass()
	{
		DataSourceTest.initClass();
	}
	/**
	 * @see {@link DataSourceTest#unloadClass()}
	 */
	@AfterClass
	public static void unloadClass()
	{
		DataSourceTest.unloadClass();
	}
	
	/**
	 * @see DataSourceTest#init()
	 */
	@Before
	public void init()
	{
	    dataSourceTest.init();
	}
	/**
	 * @see DataSourceTest#unload()
	 */
	@After
	public void unload()
	{
		dataSourceTest.unload();
	}
	
	/**
	 * Test the acquisition of a <code>BgeeDataSource</code> and 
	 * of a <code>BgeeConnection</code>.
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
