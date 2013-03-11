package org.bgee.model.data.sql.datasource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeProperties;
import org.bgee.model.TestAncestor;
import org.bgee.model.data.sql.BgeeDataSource;
import org.bgee.model.data.sql.MockDriverUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class allows to test the functionalities of 
 * {@link org.bgee.mode.data.BgeeDataSource BgeeDataSource}. 
 * It provides some test on its own.
 * It is also used by {@link DataSourceDriverManagerTest} and {@link DataSourceJNDITest} 
 * to run common tests: once when using a <code>DriverManager</code> to acquire 
 * <code>Connection</code>s, once when using a <code>DataSource</code> 
 * obtained from JNDI. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13, Mar 2013
 * @see DataSourceDriverManagerTest
 * @see DataSourceJNDITest
 * @since Bgee 13
 */
public class DataSourceTest extends TestAncestor
{
    private final static Logger log = LogManager.getLogger(DataSourceTest.class.getName());
	/**
	 * The <code>MockDriverUtils</code> providing mocked <code>Driver</code> 
	 * and mocked <code>Connection</code>s.
	 */
	protected static final MockDriverUtils mockDriverUtils = new MockDriverUtils();
	
	/**
	 * Default constructor, protected so that only classes from this package 
	 * can use it.
	 */
	public DataSourceTest()
	{
		super();
	}
	@Override
	protected Logger getLogger() {
		return log;
	}
	
	/**
	 * Initialize a <code>DataSourceTest</code> to obtain a mocked <code>Driver</code> 
	 * that will registered itself to the <code>DriverManager</code>, allowing to provide 
	 * mocked <code>Connection</code>s. Also change
	 * the <code>BgeeProperties</code> <code>jdbcUrl</code> and <code>jdbcDriver</code> 
	 * in order to automatically use this mocked <code>Driver</code>.
	 * @see #unload()
	 */
	@BeforeClass
	public static void init()
	{
		BgeeProperties props = BgeeProperties.getBgeeProperties();
		props.setJdbcUrl(MockDriverUtils.MOCKURL);
		props.setJdbcDriver(null);
	}
	/**
	 * Deregister the mocked <code>Driver</code> and reset the <code>BgeeProperties</code>.
	 * @see #init()
	 */
	@AfterClass
	public static void unload()
	{
		mockDriverUtils.deregister();
		BgeeProperties.release();
	}
	
	
	/**
	 * Class testing how to obtain a same instance, and different instances 
	 * of <code>BgeeDataSource</code> from one thread.
	 * @throws SQLException 	If an error occurs while acquiring the <code>BgeeDataSource</code>
	 */
	@Test
	public void shouldAcquireBgeeDataSourceInOneThread() throws SQLException
	{
		//acquire a first instance of BgeeDataSource
		BgeeDataSource source1  = BgeeDataSource.getBgeeDataSource();
		//a consecutive call should return the same instance
		BgeeDataSource source2  = BgeeDataSource.getBgeeDataSource();
		assertEquals("A same thread acquired two instances of BgeeDataSource", 
				source1, source2);
		
		//we release the BgeeDataSource, 
		assertEquals("The value returned by release() was inaccurate.", 
				true, BgeeDataSource.release());
		//calling it a second time should do nothing
		assertEquals("Two release() in a row returned true", 
				false, BgeeDataSource.release());
		
		//we should acquire a new instance
		BgeeDataSource newSource1 = BgeeDataSource.getBgeeDataSource();
		assertNotEquals("The BgeeProperties was not correctly released", 
				source1, newSource1);
		//the original BgeeDataSource should be tagged as released
		assertEquals("A released BgeeDataSource was not correctly tagged as released", 
				true, source1.isReleased());
		//and not the newly acquired one
		assertEquals("A not-released BgeeDataSource was incorrectly tagged as released", 
				false, newSource1.isReleased());
		
		//then, we keep getting the same instance if we call getBgeeDataSource() again
		BgeeDataSource newSource2 = BgeeDataSource.getBgeeDataSource();
		assertEquals("A same thread acquired two instances of BgeeProperties", 
				newSource1, newSource2);
		
		
		//now let's try to release by using release(long). 
		assertEquals("The value returned by release() was inaccurate.", 
				true, BgeeDataSource.release(Thread.currentThread().getId()));
		//calling it a second time should do nothing
		assertEquals("Two release() in a row returned true", 
				false, BgeeDataSource.release(Thread.currentThread().getId()));
		
		//we should acquire a new instance
		BgeeDataSource newSource3 = BgeeDataSource.getBgeeDataSource();
		assertNotEquals("The BgeeProperties was not correctly released", 
				newSource2, newSource3);
		//the original BgeeDataSource should be tagged as released
		assertEquals("A released BgeeDataSource was not correctly tagged as released", 
				true, newSource2.isReleased());
		//and not the newly acquired one
		assertEquals("A not-released BgeeDataSource was incorrectly tagged as released", 
				false, newSource3.isReleased());
		
		//reset the BgeeDataSource for other test
		BgeeDataSource.release();
	}
}
