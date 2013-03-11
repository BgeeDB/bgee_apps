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
 * The test about <b>the loading</b> of <code>BgeeProperties</code> 
 * are performed in {@link PropertiesFromFileTest}, {@link PropertiesFromSystemTest}, 
 * and {@link PropertiesMixLoadingTest}.
 * It has to be done in different classes, 
 * as the properties are read only once at class loading, so only once 
 * for a given <code>ClassLoader</code>.
 * <p>
 * This class does not test the loading behavior, it test the actual functionalities 
 * of <code>BgeeProperties</code>. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13, Mar 2013
 * @since Bgee 13
 */
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
	
	/**
	 * Class testing how to obtain a same instance, and different instances 
	 * of <code>BgeeProperties</code> from one thread.
	 */
	@Test
	public void shouldAcquirePropsInOneThread()
	{
		//acquire a first instance of BgeeProperties
		BgeeProperties prop1  = BgeeProperties.getBgeeProperties();
		//a consecutive call should return the same instance
		BgeeProperties prop2 = BgeeProperties.getBgeeProperties();
		assertEquals("A same thread acquired two instances of BgeeProperties", 
				prop1, prop2);
		//so of course any change to one will be reflected on the other
		prop1.setJdbcDriver("mytest");
		assertEquals("A same thread acquired two instances of BgeeProperties", 
				prop1.getJdbcDriver(), prop2.getJdbcDriver());
		
		//we release the BgeeProperties, 
		assertEquals("The value returned by release() was inaccurate.", 
				true, BgeeProperties.release());
		//calling it a second time should do nothing
		assertEquals("Two release() in a row returned true", 
				false, BgeeProperties.release());
		
		//we should acquire a new instance
		BgeeProperties newProp1 = BgeeProperties.getBgeeProperties();
		assertNotEquals("The BgeeProperties was not correctly released", 
				prop1, newProp1);
		//so of course any change to one will not be seen by the other
		prop1.setJdbcDriver("mytest");
		assertNotEquals("The BgeeProperties was not correctly released", 
				prop1.getJdbcDriver(), newProp1.getJdbcDriver());
		//the original BgeeProperties should be tagged as released
		assertEquals("A released BgeeProperties was not correctly tagged as released", 
				true, prop1.isReleased());
		//and not the newly acquired one
		assertEquals("A not-released BgeeProperties was incorrectly tagged as released", 
				false, newProp1.isReleased());
		
		//then, we keep getting the same instance if we call getBgeeProperties() again
		BgeeProperties newProp2 = BgeeProperties.getBgeeProperties();
		assertEquals("A same thread acquired two instances of BgeeProperties", 
				newProp1, newProp2);
		
		//reset the BgeeProperties for other test
		BgeeProperties.release();
	}
	
	/**
	 * Class testing the behavior when acquiring <code>BgeeProperties</code> 
	 * from different threads. 
	 */
	@Test
	public void shouldAcquirePropsInTwoThreads() 
	{
		/**
		 * An anonymous class to acquire <code>BgeeProperties</code>s 
		 * from a different thread than this one, 
		 * and to be run alternatively to the main thread.
		 */
		class ThreadTest extends Thread {
			public BgeeProperties prop1;
			public BgeeProperties prop2;
			public boolean firstReleaseReturn;
			public boolean secondReleaseReturn;
			/**
			 * An <code>Exchanger</code> that will be used to run threads alternatively. 
			 */
			public final Exchanger<Integer> exchanger = new Exchanger<Integer>();
			
			@Override
			public void run() {
				try {
					//start of the synchronization 
	                int turn = 1;
	                while (turn != 2) {
	                    turn = this.exchanger.exchange(turn);
	                }
	                
			        prop1 = BgeeProperties.getBgeeProperties();
			        prop2 = BgeeProperties.getBgeeProperties();
			        
			        //main thread's turn
			        turn = 1;
			        this.exchanger.exchange(turn);
			        //wait for this thread's turn
			        while (turn != 2) {
				        turn = this.exchanger.exchange(turn);
			        }
			        
			        //test the release() method from this thread
			        this.firstReleaseReturn = BgeeProperties.release();
			        //calling it twice in a row should do nothing the second time, 
			        //we'll check in the main thread
			        this.secondReleaseReturn = BgeeProperties.release();
			        
			        prop1 = BgeeProperties.getBgeeProperties();
			        prop2 = BgeeProperties.getBgeeProperties();
			        
			        //main thread's turn
			        turn = 1;
			        this.exchanger.exchange(turn);
			        //wait for this thread's turn
			        while (turn != 2) {
				        turn = this.exchanger.exchange(turn);
			        }
			        
			        //reset the BgeeProperties for other test 
			        //without using releaseAll() that would make other tests to fail
			        BgeeProperties.release();
			        
			        //main thread's turn
			        turn = 1;
			        this.exchanger.exchange(turn);
			        
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} 
			}
		}
		
		try {
			//get a BgeeProperties in the main thread
			BgeeProperties mainProp1 = BgeeProperties.getBgeeProperties();
			
			//launch the job in the second thread
	        ThreadTest test = new ThreadTest();
	        test.start();
	        //start of the synchronization
	        int turn = 2;
	        test.exchanger.exchange(turn);
	        //wait for this thread's turn
	        while (turn != 1) {
	            turn = test.exchanger.exchange(turn);
	        }

			//the 2 props in the second thread should be the same
			assertEquals("A same thread acquired two instances of BgeeProperties", 
					test.prop1, test.prop2);
			//and the props should be different in the different threads
			assertNotEquals("Two threads acquired a same instance of BgeeProperties", 
					mainProp1, test.prop1);
			
			//store a prop of the second thread for next test
			BgeeProperties secondProp1 = test.prop1;
			//then let the second thread release its BgeeProperties 
			//and acquire new BgeeProperties again
			turn = 2;
			test.exchanger.exchange(turn);
			//wait for this thread's turn
			while (turn != 1) {
				turn = test.exchanger.exchange(turn);
			}
			
			//the first call to release() in the second thread should have return true, 
			//the second one false
			assertEquals("The value returned by release() was inaccurate.", 
					true, test.firstReleaseReturn);
			assertEquals("Two release() in a row returned true", 
					false, test.secondReleaseReturn);
			
			//it should have acquired two new and identical BgeeProperties
			assertNotEquals("The BgeeProperties were not correctly released", 
					secondProp1, test.prop1);
			assertEquals("A same thread acquired two instances of BgeeProperties", 
					test.prop1, test.prop2);
			//and still different from the BgeeProperties in this thread
			assertNotEquals("Two threads acquired a same instance of BgeeProperties", 
					mainProp1, test.prop1);
			//the original BgeeProperties of the second thread should be tagged as released
			assertEquals("A released BgeeProperties was not correctly tagged as released", 
					true, secondProp1.isReleased());
			//and not the newly acquired one
			assertEquals("A not-released BgeeProperties was incorrectly tagged as released", 
					false, test.prop1.isReleased());
			
			//check that the release in the second thread did not affect the main thread
			assertEquals("A release in a Thread affected another Thread, " +
					"a not-released BgeeProperties was incorrectly tagged as released", 
					false, mainProp1.isReleased());
			BgeeProperties mainProp2 = BgeeProperties.getBgeeProperties();
			assertEquals("A release in a Thread affected another Thread", 
					mainProp1, mainProp2);
			
			
			//to finish, test the release in the main thread
			assertEquals("The value returned by release() was inaccurate.", 
					true, BgeeProperties.release());
			//calling it a second time should do nothing
			assertEquals("Two release() in a row returned true", 
					false, BgeeProperties.release());
			
			//the original BgeeProperties should be tagged as released
			assertEquals("A released BgeeProperties was not correctly tagged as released", 
					true, mainProp1.isReleased());
			//and we should acquire a new BgeeProperties instance
			mainProp2 = BgeeProperties.getBgeeProperties();
			assertNotEquals("The BgeeProperties were not correctly released", 
					mainProp1, mainProp2);
			//this new instance should not be tagged as released
			assertEquals("A not-released BgeeProperties was incorrectly tagged as released", 
					false, mainProp2.isReleased());
			//neither the BgeeProperties from the second thread
			assertEquals("A release in a Thread affected another Thread, " +
					"a not-released BgeeProperties was incorrectly tagged as released", 
					false, test.prop1.isReleased());
			
			//let the second thread releasing its BgeeProperties for other test
			//without using releaseAll() that would cause other threads to fail
			turn = 2;
			test.exchanger.exchange(turn);
			//wait for this thread's turn
			while (turn != 1) {
				turn = test.exchanger.exchange(turn);
			}
			//finally, reset the BgeeProperties of this thread
			BgeeProperties.release();
			
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} 
	}
	
	/**
	 * Test the {@link org.bgee.model.BgeeProperties#releaseAll() releaseAll()} 
	 * method when two threads are in play.
	 */
	@Test
	public void shouldReleaseAllProperties()
	{
		/**
		 * An anonymous class to acquire <code>BgeeProperties</code>s 
		 * from a different thread than this one, 
		 * and to be run alternatively to the main thread.
		 */
		class ThreadTest extends Thread {
			public BgeeProperties props;
			public boolean exceptionThrown = false;
			/**
			 * An <code>Exchanger</code> that will be used to run threads alternatively. 
			 */
			public final Exchanger<Integer> exchanger = new Exchanger<Integer>();
			
			@Override
			public void run() {
				try {
					//start of the synchronization 
	                int turn = 1;
	                while (turn != 2) {
	                    turn = this.exchanger.exchange(turn);
	                }
	                
			        props = BgeeProperties.getBgeeProperties();
			        
			        //main thread's turn
			        turn = 1;
			        this.exchanger.exchange(turn);
			        //wait for this thread's turn
			        while (turn != 2) {
				        turn = this.exchanger.exchange(turn);
			        }
			        
			        //should throw an IllegalStateException as the main thread 
			        //should have called BgeeProperties.releaseAll().
			        try {
			        	props = BgeeProperties.getBgeeProperties();
			        } catch (IllegalStateException e) {
			        	this.exceptionThrown = true;
			        }
			        
			        //main thread's turn
			        turn = 1;
			        this.exchanger.exchange(turn);
			        //wait for this thread's turn
			        while (turn != 2) {
				        turn = this.exchanger.exchange(turn);
			        }
			        
			        //reset the BgeeProperties for other test 
			        //without using releaseAll() that would make other tests to fail
			        BgeeProperties.release();
			        
			        //main thread's turn
			        turn = 1;
			        this.exchanger.exchange(turn);
			        
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} 
			}
		}
		
		try {
			//get a BgeeProperties in the main thread
			BgeeProperties mainProps = BgeeProperties.getBgeeProperties();
			
			//launch the job in the second thread
	        ThreadTest test = new ThreadTest();
	        test.start();
	        //start of the synchronization
	        int turn = 2;
	        test.exchanger.exchange(turn);
	        //wait for this thread's turn
	        while (turn != 1) {
	            turn = test.exchanger.exchange(turn);
	        }
	        
	        //call the tested method releaseAll()
	        //should have release two BgeeProperties (two threads) 
	        assertEquals("The value returned by releaseAll() was inaccurate.", 
					2, BgeeProperties.releaseAll());
	        //calling it a second time should do nothing
	        assertEquals("A second call to releaseAll() performed an action.", 
					0, BgeeProperties.releaseAll());

	       //the BgeeProperties from the main thread should have been released
		   assertEquals("A BgeeProperties was not released following a call to releaseAll()", 
					true, mainProps.isReleased());
		   //as well as the property from the second thread
		   assertEquals("A BgeeProperties was not released following a call to releaseAll()", 
					true, test.props.isReleased());
		   
		   //trying to acquire a new BgeeProperties should throw an IllegalStateException
		   try {
			   BgeeProperties.getBgeeProperties();
			   fail("getBgeeProperties() did not throw an IllegalStateException after releaseAll() was called.");
		   } catch (IllegalStateException e) {
			   //do nothing, the test passed
	        }
	        
			//then let the second thread try to get a BgeeProperties again, 
		   //this should also throw an exception
			turn = 2;
			test.exchanger.exchange(turn);
			//wait for this thread's turn
			while (turn != 1) {
				turn = test.exchanger.exchange(turn);
			}
			assertEquals("getBgeeProperties() did not throw an Exception in the second thread " +
					"after releaseAll() was called.", 
					true, test.exceptionThrown);
			
			//let the second thread releasing its BgeeProperties for other test
			//without using releaseAll() that would cause other threads to fail
			turn = 2;
			test.exchanger.exchange(turn);
			//wait for this thread's turn
			while (turn != 1) {
				turn = test.exchanger.exchange(turn);
			}
			//finally, reset the BgeeProperties of this thread
			BgeeProperties.release();
			
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} 
	}
	
	/**
	 * A really simple test of the setters of <code>BgeeProperties</code>.
	 */
	@Test
	public void shouldUseSetters()
	{
		BgeeProperties props = BgeeProperties.getBgeeProperties();
		props.setJdbcDriver("test1");
		props.setJdbcUrl("test2");
		props.setJdbcUsername("test3");
		props.setJdbcPassword("test4");
		
		assertEquals("setJdbcDriver does not work",   "test1", props.getJdbcDriver());
		assertEquals("setJdbcUrl does not work",      "test2", props.getJdbcUrl());
		assertEquals("setJdbcUsername does not work", "test3", props.getJdbcUsername());
		assertEquals("setJdbcPassword does not work", "test4", props.getJdbcPassword());
		
		//release for other test
		BgeeProperties.release();
	}
}
