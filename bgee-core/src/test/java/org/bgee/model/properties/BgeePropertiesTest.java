package org.bgee.model.properties;

import static org.junit.Assert.*;

import java.util.concurrent.Exchanger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeProperties;
import org.bgee.model.TestAncestor;
import org.junit.Test;

/**
 * Class testing the functionalities of 
 * {@link org.bgee.model.BgeeProperties BgeeProperties}. 
 * <p>
 * The test about <b>the loading</b> of {@code BgeeProperties} 
 * are performed in {@link PropertiesFromFileTest}, {@link PropertiesFromSystemTest}, 
 * and {@link PropertiesMixLoadingTest}.
 * It has to be done in different classes, 
 * as the properties are read only once at class loading, so only once 
 * for a given {@code ClassLoader}.
 * <p>
 * This class does not test the loading behavior, it test the actual functionalities 
 * of {@code BgeeProperties}. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13, Mar 2013
 * @since Bgee 13
 */
//FIXME: fix tests, check other modules such as bgee-pipeline for similar tests
public class BgeePropertiesTest extends TestAncestor
{
    private final static Logger log = LogManager.getLogger(BgeePropertiesTest.class.getName());
	
	/**
	 * Default Constructor. 
	 */
	public BgeePropertiesTest()
	{
		super();
	}
	@Override
	protected Logger getLogger() {
		return log;
	}
	
//	/**
//	 * test the behavior of 
//	 * {@link org.bgee.model.BgeeProperties#getBgeeProperties() 
//	 * BgeeProperties.getBgeeProperties()}
//	 */
//	@Test
//	public void shouldGetBgeeProperties() 
//	{
//		/**
//		 * An anonymous class to acquire {@code BgeeProperties}s 
//		 * from a different thread than this one, 
//		 * and to be run alternatively to the main thread.
//		 */
//		class ThreadTest extends Thread {
//			public volatile BgeeProperties prop1;
//			public volatile BgeeProperties prop2;
//			/**
//			 * An {@code Exchanger} that will be used to run threads alternatively. 
//			 */
//			public final Exchanger<Integer> exchanger = new Exchanger<Integer>();
//			
//			@Override
//			public void run() {
//				try {
//			        prop1 = BgeeProperties.getBgeeProperties();
//			        prop2 = BgeeProperties.getBgeeProperties();
//			        //main thread's turn
//			        this.exchanger.exchange(null);
//			        
//				} catch (InterruptedException e) {
//					Thread.currentThread().interrupt();
//				} 
//			}
//		}
//		
//		try {
//			//get a BgeeProperties in the main thread
//			BgeeProperties prop1 = BgeeProperties.getBgeeProperties();
//			assertNotNull("Could not acquire a BgeeProperties", prop1);
//			//calling getBgeeProperties() a second time from this thread 
//			//should return the same BgeeProperties instance
//			assertEquals("A same thread acquired two instances of BgeeProperties", 
//					prop1, BgeeProperties.getBgeeProperties());
//			
//			//launch a second thread also acquiring BgeeProperties
//	        ThreadTest test = new ThreadTest();
//	        test.start();
//	        //wait for this thread's turn
//	        test.exchanger.exchange(null);
//
//			//the 2 props in the second thread should be the same
//	        assertNotNull("Could not acquire a BgeeProperties", test.prop1);
//			assertEquals("A same thread acquired two instances of BgeeProperties", 
//					test.prop1, test.prop2);
//			//and the props should be different in the different threads
//			assertNotEquals("Two threads acquired a same instance of BgeeProperties", 
//					prop1, test.prop1);
//			//calling getBgeeProperties() from the main thread
//			//should still return the same BgeeProperties instance
//			assertEquals("A same thread acquired two instances of BgeeProperties", 
//					prop1, BgeeProperties.getBgeeProperties());
//			
//			//release the BgeeProperties one by one without calling releaseAll(), 
//			//that would make other test to fail
//			BgeeProperties.getBgeeProperties().release();
//			test.prop1.release();
//		} catch (InterruptedException e) {
//			Thread.currentThread().interrupt();
//		} 
//	}
//	
//	/**
//	 * test the behavior of 
//	 * {@link org.bgee.model.BgeeProperties#release() BgeeProperties.release()} and 
//	 * {@link org.bgee.model.BgeeProperties#isReleased() BgeeProperties.isReleased()}
//	 */
//	@Test
//	public void shouldReleaseBgeeProperties() 
//	{
//		/**
//		 * An anonymous class to acquire {@code BgeeProperties}s 
//		 * from a different thread than this one, 
//		 * and to be run alternatively to the main thread.
//		 */
//		class ThreadTest extends Thread {
//			public volatile BgeeProperties prop1;
//			public volatile BgeeProperties prop2;
//			public volatile boolean firstReleaseReturn;
//			public volatile boolean secondReleaseReturn;
//			/**
//			 * An {@code Exchanger} that will be used to run threads alternatively. 
//			 */
//			public final Exchanger<Integer> exchanger = new Exchanger<Integer>();
//			
//			@Override
//			public void run() {
//				try {
//					//acquire a BgeeProperties
//			        prop1 = BgeeProperties.getBgeeProperties();
//			        
//			        //release it
//			        firstReleaseReturn = prop1.release();
//			        //calling it again should do nothing, store the value to check
//			        secondReleaseReturn = prop1.release();
//			        
//			        //acquire a new BgeeProperties
//			        prop2 = BgeeProperties.getBgeeProperties();
//			        //main thread's turn
//			        this.exchanger.exchange(null);
//			        
//				} catch (InterruptedException e) {
//					Thread.currentThread().interrupt();
//				} 
//			}
//		}
//		
//		try {
//			//get a BgeeProperties in the main thread
//			BgeeProperties prop1 = BgeeProperties.getBgeeProperties();
//			
//			//release it, should return true
//			assertTrue("A BgeeProperties was not correctly released", 
//					prop1.release());
//			//calling it again should do nothing
//			assertFalse("The returned value of release() is inaccurate", 
//					prop1.release());
//			
//			//acquire a new BgeeProperties
//			BgeeProperties prop2 = BgeeProperties.getBgeeProperties();
//			
//			//launch a second thread also acquiring and releasing BgeeProperties
//	        ThreadTest test = new ThreadTest();
//	        test.start();
//	        //wait for this thread's turn
//	        test.exchanger.exchange(null);
//	        
//	        //test the returned value of the two calls to release() in the second thread
//	        assertTrue("A BgeeProperties was not correctly released in second thread", 
//					test.firstReleaseReturn);
//	        assertFalse("The returned value of release() is inaccurate in second thread", 
//					test.secondReleaseReturn);
//
//			//the first BgeeProperties of the main thread and the second thread 
//	        //should be released
//	        assertTrue("A BgeeProperties was not correctly released in the main thread", 
//					prop1.isReleased());
//	        assertTrue("A BgeeProperties was not correctly released in the second thread", 
//					test.prop1.isReleased());
//			
//			//the second BgeeProperties of the main thread and the second thread 
//	        //should NOT be released
//	        assertFalse("A BgeeProperties should not have been released in the main thread", 
//					prop2.isReleased());
//	        assertFalse("A BgeeProperties should not have been released in the second thread", 
//					test.prop2.isReleased());
//			
//			//release the BgeeProperties one by one without calling releaseAll(), 
//			//that would make other test to fail
//			BgeeProperties.getBgeeProperties().release();
//			test.prop2.release();
//		} catch (InterruptedException e) {
//			Thread.currentThread().interrupt();
//		} 
//	}
//	
//	/**
//	 * A really simple test of the setters of {@code BgeeProperties}.
//	 */
//	@Test
//	public void shouldUseSetters()
//	{
//		BgeeProperties props = BgeeProperties.getBgeeProperties();
//		props.setJdbcDriver("test1");
//		props.setJdbcUrl("test2");
//		props.setJdbcUsername("test3");
//		props.setJdbcPassword("test4");
//		
//		assertEquals("setJdbcDriver does not work",   "test1", props.getJdbcDriver());
//		assertEquals("setJdbcUrl does not work",      "test2", props.getJdbcUrl());
//		assertEquals("setJdbcUsername does not work", "test3", props.getJdbcUsername());
//		assertEquals("setJdbcPassword does not work", "test4", props.getJdbcPassword());
//		
//		//release for other test
//		props.release();
//	}
}
