package org.bgee.model.dao.mysql.datasource;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.mysql.BgeeConnection;
import org.bgee.model.dao.mysql.BgeeDataSource;
import org.bgee.model.dao.mysql.MockDriverUtils;
import org.bgee.model.dao.mysql.MySQLDAOManager;
import org.bgee.model.dao.mysql.TestAncestor;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the behavior of {@link MySQLDAOManager} when using a {@code DriverManager} 
 * to acquire a {@code Connection}. The loading when using a {@code DataSource} 
 * is tested in a separated class: parameters are set only once at class loading, 
 * so only once for a given {@code ClassLoader}, so it has to be done 
 * in different classes. See {@link JNDIDataSourceIntegrationTest}.
 * 
 * @author Frederic Bastian
 * @author Mathieu Seppey
 * @version Bgee 13
 * @see JNDIDataSourceIntegrationTest
 * @since Bgee 13
 */
public class DataSourceDriverManagerTest extends TestAncestor
{
    private final static Logger log = 
            LogManager.getLogger(DataSourceDriverManagerTest.class.getName());
	
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
	public static void initClass() throws Exception {
	    System.setProperty(MySQLDAOManager.CONFIGFILEKEY, "/none");
        System.setProperty(MySQLDAOManager.JDBCURLKEY, MockDriverUtils.MOCKURL);
        System.setProperty(MySQLDAOManager.JDBCDRIVERNAMEKEY, DriverTestImpl.class.getName());
        System.setProperty(MySQLDAOManager.USERNAMEKEY, "bgee.jdbc.username.test");
        System.setProperty(MySQLDAOManager.PASSWORDKEY, "bgee.jdbc.password.test");
	}
	/**
	 * @see {@link InitDataSourceTest#unloadClass()}
	 */
	@AfterClass
	public static void unloadClass() {
	    System.clearProperty(MySQLDAOManager.CONFIGFILEKEY);
        System.clearProperty(MySQLDAOManager.JDBCURLKEY);
        System.clearProperty(MySQLDAOManager.JDBCDRIVERNAMEKEY);
        System.clearProperty(MySQLDAOManager.USERNAMEKEY);
        System.clearProperty(MySQLDAOManager.PASSWORDKEY);
	}
    /**
     * @see InitDataSourceTest#unload()
     */
    @Before
    public void init() {
        InitDataSourceTest.init();
    }
	/**
	 * @see InitDataSourceTest#unload()
	 */
	@After
	public void unload() {
		InitDataSourceTest.unload();
	}
	
	/**
	 * Test the acquisition of a {@code BgeeConnection}.
	 * 
	 * @throws SQLException
	 */
	@Test
	public void getBgeeConnection() throws SQLException {		
		MySQLDAOManager manager = (MySQLDAOManager) DAOManager.getDAOManager();
		//BgeeConnection conn = manager.getConnection();
	}
}
