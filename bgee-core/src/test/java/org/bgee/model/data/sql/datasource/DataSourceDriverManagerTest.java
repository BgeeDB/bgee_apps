package org.bgee.model.data.sql.datasource;

import static org.junit.Assert.*;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeProperties;
import org.bgee.model.TestAncestor;
import org.bgee.model.data.sql.BgeeConnection;
import org.bgee.model.data.sql.BgeeDataSource;
import org.bgee.model.data.sql.MockDriverUtils;
import org.junit.AfterClass;
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
	 * @see DataSourceTest#init()
	 */
	@BeforeClass
	public static void init()
	{
		DataSourceTest.init();
	}
	/**
	 * @see DataSourceTest#unload()
	 */
	@AfterClass
	public static void unload()
	{
		DataSourceTest.unload();
	}
	
	@Test
	public void loadDataSourceUsingDriverManager()
	{		
		//get a BgeeDataSource
		BgeeDataSource sourceTest = null;
		try {
			sourceTest = BgeeDataSource.getBgeeDataSource();
		} catch (SQLException e) {
			fail("SQLException thrown when trying to acquire a BgeeDataSource");
		}
		
		//get a BgeeConnection
		try {
			BgeeConnection connTest = sourceTest.getConnection();
		} catch (SQLException e) {
			fail("SQLException thrown when trying to acquire a BgeeConnection");
		}
	}
}
