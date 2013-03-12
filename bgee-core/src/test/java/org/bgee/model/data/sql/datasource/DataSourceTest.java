package org.bgee.model.data.sql.datasource;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.concurrent.Exchanger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.TestAncestor;
import org.bgee.model.data.sql.BgeeConnection;
import org.bgee.model.data.sql.BgeeDataSource;
import org.bgee.model.data.sql.MockDriverUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.*;

/**
 * This class allows to test the functionalities of 
 * {@link org.bgee.mode.data.BgeeDataSource BgeeDataSource}. 
 * It also provides some test on its own.
 * <p>
 * It is used by {@link DataSourceDriverManagerTest} and {@link DataSourceJNDITest} 
 * to run common methods ({@link #init()}, {@link #unload()}, 
 * and {@link #getMockDriverUtils()}).
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
	private MockDriverUtils mockDriverUtils;
	
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
	 * Change the System properties 
	 * in order to automatically acquire mocked <code>Driver</code>.
	 */
	@BeforeClass
	public static void initClass()
	{
		System.setProperty("bgee.jdbc.url", MockDriverUtils.MOCKURL);
		System.clearProperty("bgee.jdbc.driver");
	}
	/**
	 * Reset the System properties that were changed 
	 * in order to automatically acquire mocked <code>Driver</code>.
	 */
	@AfterClass
	public static void unloadClass()
	{
		System.clearProperty("bgee.jdbc.url");
	}
	
	/**
	 * Obtain a mocked <code>Driver</code> 
	 * that will registered itself to the <code>DriverManager</code>, allowing to provide 
	 * mocked <code>Connection</code>s. Also change
	 * the <code>BgeeProperties</code> <code>jdbcUrl</code> and <code>jdbcDriver</code> 
	 * in order to automatically use this mocked <code>Driver</code>.
	 * @see #unload()
	 */
	@Before
	public void init()
	{
		this.mockDriverUtils = new MockDriverUtils();
	}
	/**
	 * Deregister the mocked <code>Driver</code> and reset the <code>BgeeProperties</code>.
	 * @see #init()
	 */
	@After
	public void unload()
	{
		this.mockDriverUtils.deregister();
	}
	
	public MockDriverUtils getMockDriverUtils()
	{
		return this.mockDriverUtils;
	}
	
	
	/**
	 * test the behavior of 
	 * {@link org.bgee.model.BgeeDataSource#getBgeeDataSource() 
	 * BgeeDataSource.getBgeeDataSource()}
	 */
	@Test
	public void shouldGetBgeeDataSource() throws SQLException 
	{
		/**
		 * An anonymous class to acquire <code>BgeeDataSource</code>s 
		 * from a different thread than this one, 
		 * and to be run alternatively to the main thread.
		 */
		class ThreadTest extends Thread {
			public volatile BgeeDataSource source1;
			public volatile BgeeDataSource source2;
			public volatile boolean exceptionThrown = false;
			/**
			 * An <code>Exchanger</code> that will be used to run threads alternatively. 
			 */
			public final Exchanger<Integer> exchanger = new Exchanger<Integer>();
			
			@Override
			public void run() {
				try {
					source1 = BgeeDataSource.getBgeeDataSource();
					source2 = BgeeDataSource.getBgeeDataSource();
			        //main thread's turn
			        this.exchanger.exchange(null);
			        
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch (SQLException e) {
					exceptionThrown = true;
				} 
			}
		}
		
		try {
			//get a BgeeDataSource in the main thread
			BgeeDataSource source1 = BgeeDataSource.getBgeeDataSource();
			assertNotNull("Could not acquire a BgeeDataSource", source1);
			//calling getBgeeDataSource() a second time from this thread 
			//should return the same BgeeDataSource instance
			assertEquals("A same thread acquired two instances of BgeeDataSource", 
					source1, BgeeDataSource.getBgeeDataSource());
			
			//launch a second thread also acquiring BgeeDataSource
	        ThreadTest test = new ThreadTest();
	        test.start();
	        //wait for this thread's turn
	        test.exchanger.exchange(null);
	        //check that no exception was thrown in the second thread 
	        if (test.exceptionThrown) {
	        	throw new SQLException("A SQLException occurred in the second thread.");
	        }

			//the 2 sources in the second thread should be the same
	        assertNotNull("Could not acquire a BgeeDataSource", test.source1);
			assertEquals("A same thread acquired two instances of BgeeDataSource", 
					test.source1, test.source2);
			//and the sources should be different in the different threads
			assertNotEquals("Two threads acquired a same instance of BgeeDataSource", 
					source1, test.source1);
			//calling getBgeeDataSource() from the main thread
			//should still return the same BgeeDataSource instance
			assertEquals("A same thread acquired two instances of BgeeDataSource", 
					source1, BgeeDataSource.getBgeeDataSource());
			
			//release the BgeeDataSource one by one without calling closeAll(), 
			//that would make other test to fail
			BgeeDataSource.getBgeeDataSource().close();
			test.source1.close();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} 
	}
	
	/**
	 * test the behavior of 
	 * {@link org.bgee.model.BgeeDataSource#close() BgeeDataSource.close()} and 
	 * {@link org.bgee.model.BgeeDataSource#isClosed() BgeeDataSource.isClosed()}
	 */
	@Test
	public void shouldCloseBgeeDataSource() throws SQLException 
	{
		/**
		 * An anonymous class to acquire <code>BgeeDataSource</code>s 
		 * from a different thread than this one, 
		 * and to be run alternatively to the main thread.
		 */
		class ThreadTest extends Thread {
			public volatile BgeeDataSource source1;
			public volatile BgeeDataSource source2;
			public volatile boolean exceptionThrown = false;
			/**
			 * An <code>Exchanger</code> that will be used to run threads alternatively. 
			 */
			public final Exchanger<Integer> exchanger = new Exchanger<Integer>();
			
			@Override
			public void run() {
				try {
					//acquire a BgeeDataSource
			        source1 = BgeeDataSource.getBgeeDataSource();
			        
			        //main thread's turn
			        this.exchanger.exchange(null);
			        //wait for this thread's turn
			        this.exchanger.exchange(null);
			        
			        //acquire a new BgeeDataSource after it was closed in the main thread. 
			        //it should not have closed this one, so it should return the same source
			        source2 = BgeeDataSource.getBgeeDataSource();
			        
			        //main thread's turn
			        this.exchanger.exchange(null);
			        
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch (SQLException e) {
					exceptionThrown = true;
				}
			}
		}
		
		try {
			//get a BgeeDataSource in the main thread
			BgeeDataSource source1 = BgeeDataSource.getBgeeDataSource();

			//launch a second thread also acquiring BgeeDataSource
	        ThreadTest test = new ThreadTest();
	        test.start();
	        //wait for this thread's turn
	        test.exchanger.exchange(null);
	        //check that no exception was thrown in the second thread 
	        if (test.exceptionThrown) {
	        	throw new SQLException("A SQLException occurred in the second thread.");
	        }
			
			//close it
			source1.close();
			//calling it again should do nothing
			source1.close();
			
			//relaunch the other thread so that it can acquire a BgeeDataSoure again 
			//(its BgeeDataSoure should not have been closed)
	        test.exchanger.exchange(null);
	        //wait for this thread's turn
	        test.exchanger.exchange(null);
	        //check that no exception was thrown in the second thread 
	        if (test.exceptionThrown) {
	        	throw new SQLException("A SQLException occurred in the second thread.");
	        }
			
			//acquire a new BgeeDataSource
			BgeeDataSource source2 = BgeeDataSource.getBgeeDataSource();

			//the first BgeeDataSource of the main thread should be closed
	        assertTrue("A BgeeDataSource was not correctly closed in the main thread", 
					source1.isClosed());
	        
	        //the first BgeeDataSource of the second thread should NOT be closed
	        assertFalse("close() on a BgeeDataSource in one thread affected another thread", 
					test.source1.isClosed());
	        //and the second BgeeDataSource of the second thread should be identical to the first one 
	        //(as it was not closed)
	        assertEquals("close() on a BgeeDataSource in one thread affected another thread", 
	        		test.source1, test.source2);
			
			//the second BgeeDataSource of the main thread should NOT be closed
	        assertFalse("A BgeeDataSource should not have been closed in the main thread", 
					source2.isClosed());
			
			//close the BgeeDataSource one by one without calling closeAll(), 
			//that would make other test to fail
			BgeeDataSource.getBgeeDataSource().close();
			test.source2.close();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} 
	}
	
	/**
	 * test the behavior of 
	 * {@link org.bgee.model.BgeeDataSource#getConnection() BgeeDataSource.getConnection()} 
	 * and {@link org.bgee.model.BgeeDataSource#getConnection(String, String) 
	 * BgeeDataSource.getConnection(String, String)}.
	 */
	@Test
	public void shouldGetAndReleaseConnections() throws SQLException 
	{
		/**
		 * An anonymous class to acquire <code>BgeeDataSource</code>s 
		 * from a different thread than this one, 
		 * and to be run alternatively to the main thread.
		 */
		class ThreadTest extends Thread {
			public volatile BgeeDataSource source1;
			public volatile BgeeConnection conn1;
			public volatile BgeeConnection conn2;
			public volatile boolean exceptionThrown = false;
			/**
			 * An <code>Exchanger</code> that will be used to run threads alternatively. 
			 */
			public final Exchanger<Integer> exchanger = new Exchanger<Integer>();
			
			@Override
			public void run() {
				try {
					//acquire a BgeeDataSource
					source1 = BgeeDataSource.getBgeeDataSource();
			        //acquire 2 different connection
			        conn1 = source1.getConnection();
			        conn2 = source1.getConnection("", "");
			        
			        //main thread's turn
			        this.exchanger.exchange(null);
			        //wait for this thred's turn
			        this.exchanger.exchange(null);
			        
			        //try to get a connection again. It should not fail, 
			        //as the BgeeDataSource of this thread was not closed
			        source1.getConnection();
			        source1.getConnection("", "");
			        
			        //main thread's turn
			        this.exchanger.exchange(null);
			        
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch (SQLException e) {
					exceptionThrown = true;
				}
			}
		}
		
		try {
			//get a BgeeDataSource in the main thread
			BgeeDataSource source1 = BgeeDataSource.getBgeeDataSource();

	        //acquire 2 different connections
	        BgeeConnection conn1 = source1.getConnection();
	        BgeeConnection conn2 = source1.getConnection("", "");
			
			//launch a second thread also acquiring BgeeConnections
	        ThreadTest test = new ThreadTest();
	        test.start();
	        //wait for this thread's turn
	        test.exchanger.exchange(null);
	        //check that no exception was thrown in the second thread 
	        if (test.exceptionThrown) {
	        	throw new SQLException("A SQLException occurred in the second thread.");
	        }
	        
	        //the two connection in the main thread should be different
	        assertNotNull("Failed to acquire a BgeeConnection", conn1);
	        assertNotNull("Failed to acquire a BgeeConnection", conn2);
	        assertNotEquals("A same thread acquire a same BgeeConnection instance " + 
	        		"for different parameters", conn1, conn2);
	        //even if different parameters when using getConnection(String, String)
	        assertNotEquals("A same thread acquire a same BgeeConnection instance " + 
	        		"for different parameters", 
	        		conn2, source1.getConnection("gfdsgd", "dfdsfsd"));
	        
	        //as well as in the second thread
	        assertNotNull("Failed to acquire a BgeeConnection", test.conn1);
	        assertNotNull("Failed to acquire a BgeeConnection", test.conn2);
	        assertNotEquals("A same thread acquire a same BgeeConnection instance " + 
	        		"for different parameters", test.conn1, test.conn2);
	        
	        //the connections with the same parameters should be different in different threads
	        assertNotEquals("Two threads acquire a same BgeeConnection instance ", 
	        		conn1, test.conn1);
	        assertNotEquals("Two threads acquire a same BgeeConnection instance ", 
	        		conn2, test.conn2);
	        
	        //trying to acquire connections with the same parameters should return the same connection
	        assertEquals("Get two BgeeConnection instances for the smae parameters", 
	        		conn1, source1.getConnection());
	        assertEquals("Get two BgeeConnection instances for the smae parameters", 
	        		conn2, source1.getConnection("", ""));
			
			//close the source in the main thread
			source1.close();

			//close() should have been called on the mocked connection exactly three times 
			//(because we opened three connections in this thread)
			verify(this.getMockDriverUtils().getMockConnection(), times(3)).close();
			//trying to acquire a Connection again should throw a SQLException
			try {
				source1.getConnection();
				fail("Could acquire a Connection after the BgeeDataSource was closed");
			} catch (SQLException e) {
			}
			
			//relaunch the other thread to check if it can still acquire connections, 
			//as its BgeeDataSource was not closed
			test.exchanger.exchange(null);
	        //wait for this thread's turn
	        test.exchanger.exchange(null);
	        //check that no exception was thrown in the second thread 
	        if (test.exceptionThrown) {
	        	throw new SQLException("A SQLException occurred in the second thread.");
	        }
			
			//close the BgeeDataSource one by one without calling closeAll(), 
			//that would make other test to fail
			BgeeDataSource.getBgeeDataSource().close();
			test.source1.close();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} 
	}
}
