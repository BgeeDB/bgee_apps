package org.bgee.model.properties;

import static org.junit.Assert.*;

import java.util.concurrent.Exchanger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeProperties;
import org.bgee.model.TestAncestor;
import org.junit.Test;

/**
 * Class testing the behavior of 
 * {@link org.bgee.model.BgeeProperties#releaseAll() BgeeProperties.releaseAll()}.
 * <p>
 * The reason why this test should go into a dedicated class is that after a call 
 * to {@code releaseAll}, no BgeeProperties could be obtained anymore for other tests.
 * 
 * @author Frederic Bastian
 * @version Bgee 13, Mar 2013
 * @since Bgee 13
 */
//FIXME: fix tests, check other modules such as bgee-pipeline for similar tests
public class ReleaseAllTest extends TestAncestor
{
    private final static Logger log = LogManager.getLogger(ReleaseAllTest.class.getName());
	
	/**
	 * Default Constructor. 
	 */
	public ReleaseAllTest()
	{
		super();
	}
	@Override
	protected Logger getLogger() {
		return log;
	}
	
//	/**
//	 * test the behavior of 
//	 * {@link org.bgee.model.BgeeProperties#releaseAll() BgeeProperties.releaseAll()} and 
//	 * {@link org.bgee.model.BgeeProperties#isReleased() BgeeProperties.isReleased()}
//	 */
//	@Test
//	public void shouldReleaseAllBgeeProperties() 
//	{
//		/**
//		 * An anonymous class to acquire {@code BgeeProperties}s 
//		 * from a different thread than this one, 
//		 * and to be run alternatively to the main thread.
//		 */
//		class ThreadTest extends Thread {
//			public BgeeProperties prop1;
//			public boolean exceptionThrown = false;
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
//			        //main thread's turn
//			        this.exchanger.exchange(null);
//			        //wait for this thread's turn
//			        this.exchanger.exchange(null);
//			        
//			        //should throw an IllegalStateException as the main thread 
//			        //should have called BgeeProperties.releaseAll().
//			        try {
//			        	prop1 = BgeeProperties.getBgeeProperties();
//			        } catch (IllegalStateException e) {
//			        	this.exceptionThrown = true;
//			        }
//			        
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
//			//let a second thread also acquire a BgeeProperties
//	        ThreadTest test = new ThreadTest();
//	        test.start();
//	        //wait for this thread's turn
//	        test.exchanger.exchange(null);
//	        
//	        //none of the properties should be tagged as released
//	        assertFalse("The returned value of isReleased() is inaccurate.", 
//					prop1.isReleased());
//			assertFalse("The returned value of isReleased() is inaccurate in the second thread.", 
//					test.prop1.isReleased());
//	        
//	        //call the tested method releaseAll()
//	        //should have release two BgeeProperties (two threads) 
//	        assertEquals("The value returned by releaseAll() was inaccurate.", 
//					2, BgeeProperties.releaseAll());
//	        //calling it a second time should do nothing
//	        assertEquals("A second call to releaseAll() performed an action.", 
//					0, BgeeProperties.releaseAll());
//	        
//			//the BgeeProperties acquired in both Threads should have been released
//	        assertTrue("A BgeeProperties was not correctly released in the main thread", 
//	        		prop1.isReleased());
//			assertTrue("A BgeeProperties was not correctly released in the second thread", 
//					test.prop1.isReleased());
//			
//	        //trying to acquire a new BgeeProperties should throw an IllegalStateException
//	        try {
//	        	BgeeProperties.getBgeeProperties();
//	        	fail("getBgeeProperties() did not throw an IllegalStateException after releaseAll() was called.");
//	        } catch (IllegalStateException e) {
//	        }
//	        
//	        //then let the second thread also try to get a new BgeeProperties, 
//			//an IllegalStateException should be thrown
//			test.exchanger.exchange(null);
//			//wait for this thread's turn
//			test.exchanger.exchange(null);
//			
//			assertTrue("getBgeeProperties() did not throw an Exception in the second thread " +
//					"after releaseAll() was called.", 
//					test.exceptionThrown);
//			
//		} catch (InterruptedException e) {
//			Thread.currentThread().interrupt();
//		} 
//	}
}
