package org.bgee.model.dao.api;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.concurrent.Callable;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAOManager;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests testing the loading and closing of {@link DAOManager}.

 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class ManagerLoadAndReleaseTest extends TestAncestor {
	/**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
    		LogManager.getLogger(ManagerLoadAndReleaseTest.class.getName());

	@Override
	protected Logger getLogger() {
		return log;
	}
	
	/**
	 * test the behavior of {@link DAOManager#getDAOManager(Map)} and 
	 * {@link DAOManager#getDAOManager()}.
	 */
	@Test
	public void shouldGetDAOManager() throws Exception 
	{
		/**
		 * An anonymous class to acquire {@code DAOManager}s 
		 * from a different thread than this one, 
		 * and to be run alternatively to the main thread.
		 */
        class ThreadTest implements Callable<Boolean> {

			public DAOManager manager1;
			public DAOManager manager2;
			/**
			 * An {@code Exchanger} that will be used to run threads alternatively. 
			 */
			public final Exchanger<Integer> exchanger = new Exchanger<Integer>();
			@Override
			public Boolean call() throws Exception {
				try {
					Map<String, String> parameters = new HashMap<String, String>();
					parameters.put("test.key", "test.value");
					manager1 = DAOManager.getDAOManager();
					manager2 = DAOManager.getDAOManager();
			        
			        //main thread will be wake up by the finally statement
			        
			        return true;
			        
				} finally {
					//whatever happens, make sure to re-launch the main thread, 
					//as we do not use an Executor that might catch the Exception 
					//and interrupt the other Thread. 
					this.exchanger.exchange(null);
				}
			}
		};
		
		//get a DAOManager in the main thread
		DAOManager manager1 = DAOManager.getDAOManager();
		assertNotNull("Could not acquire a DAOManager", manager1);
		//calling getDAOManager() a second time from this thread 
		//should return the same DAOManager instance, even with different parameters
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("test.key", "test.value");
		assertSame("A same thread acquired two instances of DAOManager", 
				manager1, DAOManager.getDAOManager(parameters));

		//launch a second thread also acquiring DAOManager
		ThreadTest test = new ThreadTest();
		ExecutorService executorService = Executors.newFixedThreadPool(1);
		Future<Boolean> future = executorService.submit(test);
		//wait for this thread's turn
		test.exchanger.exchange(null);
		//check that no exception was thrown in the second thread.
		//In that case, it would be completed and calling get would throw 
		//the exception. 
		if (future.isDone()) {
			future.get();
		}

		//the 2 managers in the second thread should be the same
		assertNotNull("Could not acquire a DAOManager", test.manager1);
		assertSame("A same thread acquired two instances of DAOManager", 
				test.manager1, test.manager2);
		//and the managers should be different in the different threads
		assertNotSame("Two threads acquired a same instance of DAOManager", 
				manager1, test.manager1);
		//calling getDAOManager() from the main thread
		//should still return the same DAOManager instance
		assertSame("A same thread acquired two instances of DAOManager", 
				manager1, DAOManager.getDAOManager());

		//release the DAOManager one by one without calling closeAll(), 
		//that would make other test to fail
		DAOManager.getDAOManager().close();
		test.manager1.close();
	}
	
	/**
	 * test the behavior of {@link DAOManager#close()} and 
	 * {@link DAOManager#isClosed()}
	 */
	@Test
	public void shouldCloseDAOManager() throws Exception 
	{
		/**
		 * An anonymous class to acquire {@code DAOManager}s 
		 * from a different thread than this one, 
		 * and to be run alternatively to the main thread.
		 */
		class ThreadTest implements Callable<Boolean> {

			public DAOManager manager1;
			public DAOManager manager2;
			/**
			 * An {@code Exchanger} that will be used to run threads alternatively. 
			 */
			public final Exchanger<Integer> exchanger = new Exchanger<Integer>();
			@Override
			public Boolean call() throws Exception {
				try {
					//acquire a DAOManager
			        manager1 = DAOManager.getDAOManager();
			        
			        //main thread's turn
			        this.exchanger.exchange(null);
			        //wait for this thread's turn
			        this.exchanger.exchange(null);
			        
			        //acquire a new DAOManager after it was closed in the main thread. 
			        //it should not have closed this one, so it should return the same source
			        manager2 = DAOManager.getDAOManager();
			        
			        //main thread will be wake up by the finally statement
			        
			        return true;
			        
				} finally {
					//whatever happens, make sure to re-launch the main thread, 
					//as we do not use an Executor that might catch the Exception 
					//and interrupt the other Thread. 
					this.exchanger.exchange(null);
				}
			}
		};
		
		//get a DAOManager in the main thread
		DAOManager manager1 = DAOManager.getDAOManager();

		//launch a second thread also acquiring DAOManager
		ThreadTest test = new ThreadTest();
		ExecutorService executorService = Executors.newFixedThreadPool(1);
		Future<Boolean> future = executorService.submit(test);
		//wait for this thread's turn
		test.exchanger.exchange(null);
		//check that no exception was thrown in the second thread.
		//In that case, it would be completed and calling get would throw 
		//the exception. 
		if (future.isDone()) {
			future.get();
		}

		//close it
		manager1.close();
		//calling it again should do nothing
		manager1.close();

		//relaunch the other thread so that it can acquire a DAOManager again 
		//(its DAOManager should not have been closed)
		test.exchanger.exchange(null);
		//wait for this thread's turn
		test.exchanger.exchange(null);
		//check that no exception was thrown in the second thread 
		future.get();

		//acquire a new DAOManager
		DAOManager manager2 = DAOManager.getDAOManager();

		//the first DAOManager of the main thread should be closed
		assertTrue("A DAOManager was not correctly closed in the main thread", 
				manager1.isClosed());

		//the first DAOManager of the second thread should NOT be closed
		assertFalse("close() on a DAOManager in one thread affected another thread", 
				test.manager1.isClosed());
		//and the second DAOManager of the second thread should be identical to the first one 
		//(as it was not closed)
		assertSame("close() on a DAOManager in one thread affected another thread", 
				test.manager1, test.manager2);

		//the second DAOManager of the main thread should NOT be closed
		assertFalse("A DAOManager should not have been closed in the main thread", 
				manager2.isClosed());
		//and it should not be equals to the first manager of the main thread
		assertNotSame("A new DAOManager was not acquired after the first one " +
				"was closed", manager1, manager2);

		//close the DAOManager one by one without calling closeAll(), 
		//that would make other test to fail
		DAOManager.getDAOManager().close();
		test.manager2.close();
	}
	
	/**
	 * Test behavior when {@link DAOManager#getDAOManager()} fails.
	 */
	@Test
	public void shouldFailGetDAOManager() throws ClassNotFoundException {
		//call a first time DAOManager to properly load the service providers
		Class.forName("org.bgee.model.dao.api.DAOManager");
		
		//to throw an exception when an instance of the service provider is requested
		MockDAOManager.thrownInstantiationException = true;
		//MockDAOManager is the first provider loaded, so calling getDAOManager 
		//should throw an exception right away
		try {
			DAOManager.getDAOManager();
			//if we reach that point, no ServiceConfigurationError was thrown, 
			//test failed
		    throw new AssertionError("A ServiceConfigurationError should have been thrown");
		} catch (ServiceConfigurationError e) {
			//test passed
		}
		
		//now we configure the second provider to throw an exception, 
		//so the first one should be valid and be returned
		MockDAOManager.thrownInstantiationException = false;
		MockDAOManager2.thrownInstantiationException = true;
		assertNotNull("The DAOManager from the first provider was not returned", 
				DAOManager.getDAOManager());
		
		//reset parameters so that other tests won't fail
		MockDAOManager2.thrownInstantiationException = false;
	}
	
	/**
	 * Test the functionality of {@link DAOManager#setParameters(Map)}, 
	 * in relation with {@link DAOManager#getDAOManager(Map)}.
	 */
	@Test
	public void shouldSetParameters() {
		//make the providers to refuse some parameters
		
		//MockDAOManager
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("test.key", "test.value");
		doThrow(new IllegalArgumentException("Mock exception on purpose")).
		    when(MockDAOManager.mockManager).setParameters(eq(parameters));
		
		//MockDAOManager2
		Map<String, String> parameters2 = new HashMap<String, String>();
		parameters2.put("test.key2", "test.value2");
		doThrow(new IllegalArgumentException("Mock exception on purpose")).
	        when(MockDAOManager2.mockManager).setParameters(eq(parameters2));
		
		//check that we got the second provider when using the parameters 
		//that the first provider rejects
		DAOManager manager = DAOManager.getDAOManager(parameters);
		assertEquals("getDAOManager returned the wrong service provider", 
			"org.bgee.model.dao.api.MockDAOManager2", manager.getClass().getName());
		
		//now, calling getDAOManager again with no parameters should still return 
		//the same instance of the second provider
		assertSame("getDAOManager returned a second instance in a same thread", 
				manager, DAOManager.getDAOManager());
		
		//calling getDAOManager with the parameters the current manager rejects 
		//should throw an IllegalArgumentException
		try {
			DAOManager.getDAOManager(parameters2);
			//if we reach this point, test failed
		    throw new AssertionError("A IllegalArgumentException should have been thrown");
		} catch (IllegalArgumentException e) {
			//test passed
			log.catching(Level.DEBUG, e);
		}
		//but we can still acquire it with no parameters
		assertSame("getDAOManager returned a second instance in a same thread", 
				manager, DAOManager.getDAOManager());
		//or with the proper parameters
		assertSame("getDAOManager returned a second instance in a same thread", 
				manager, DAOManager.getDAOManager(parameters));
		
		//now if we release it, wa can acquire an instance from the first provider
		manager.close();
		manager = DAOManager.getDAOManager(parameters2);
		assertEquals("getDAOManager returned the wrong service provider", 
				"org.bgee.model.dao.api.MockDAOManager", manager.getClass().getName());
		
		//release the DAOManager without calling closeAll(), 
		//that would make other test to fail
		manager.close();
		reset(MockDAOManager.mockManager);
		reset(MockDAOManager2.mockManager);
	}
	
	/**
	 * Test the functionality of {@link DAOManager#kill(long)}.
	 */
	@Test
	public void shouldKillManager() throws Exception {
		class ThreadTest implements Callable<Boolean> {
			
			public DAOManager manager;
			/**
			 * An {@code Exchanger} that will be used to run threads alternatively. 
			 */
			public final Exchanger<Integer> exchanger = new Exchanger<Integer>();
			@Override
			public Boolean call() throws Exception {
				try {
					//acquire a DAOManager
			        manager = DAOManager.getDAOManager();
			        
			        //main thread's turn
			        this.exchanger.exchange(null);
			        //wait for this thread's turn
			        this.exchanger.exchange(null);
			        
			        //the main thread has called kill on this manager, 
			        //let's check that we can have another one
			        manager = DAOManager.getDAOManager();
			        
			        //main thread will be wake up by the finally statement
			        
			        return true;
			        
				} finally {
					//whatever happens, make sure to re-launch the main thread, 
					//as we do not use an Executor that might catch the Exception 
					//and interrupt the other Thread. 
					this.exchanger.exchange(null);
				}
			}
		};
		
		//get a DAOManager in the main thread (just to be sure this one
		//is not killed when we kill the manager in the second thread)
		DAOManager manager = DAOManager.getDAOManager();
		//launch a second thread also acquiring DAOManager
		ThreadTest test = new ThreadTest();
		ExecutorService executorService = Executors.newFixedThreadPool(1);
		Future<Boolean> future = executorService.submit(test);
		//wait for this thread's turn
		test.exchanger.exchange(null);
		//check that no exception was thrown in the second thread.
		//In that case, it would be completed and calling get would throw 
		//the exception. 
		if (future.isDone()) {
			future.get();
		}

		//test kill
		DAOManager.kill(test.manager.getId());
		assertTrue("The manager in the second thread was not closed", 
				test.manager.isClosed());
		assertTrue("The manager in the second thread was not killed", 
				test.manager.isKilled());
		verify(((MockDAOManager) test.manager).instanceMockManager).killDAOManager();
		//check there is no effect on this thread
		assertFalse("The manager in the main thread was closed", 
				manager.isClosed());
		assertFalse("The manager in the main thread was killed", 
				manager.isKilled());
		verify(((MockDAOManager) manager).instanceMockManager, never()).killDAOManager();

		//check that the second thread can still acquire a new manager
		DAOManager storeManager = test.manager;
		test.exchanger.exchange(null);
		//wait for this thread's turn
		test.exchanger.exchange(null);
		//second thread terminated, check that no exception was thrown
		future.get();
		assertNotNull("The second thread did not acquire a new manager", 
				test.manager);
		assertNotSame("The second thread did not acquire a new manager", 
				storeManager, test.manager);

		//release the DAOManager without calling closeAll(), 
		//that would make other test to fail
		test.manager.close();
		manager.close();
	}
	
	/**
	 * Test the functionality of {@link DAOManager#hasDAOManager()}.
	 */
	@Test
	public void shouldHaveDAOManager() throws Exception {

		/**
		 * An anonymous class to acquire {@code DAOManager}s 
		 * from a different thread than this one, 
		 * and to be run alternatively to the main thread.
		 */
		class ThreadTest implements Callable<Boolean> {
			public boolean hasAManager;
			/**
			 * An {@code Exchanger} that will be used to run threads alternatively. 
			 */
			public final Exchanger<Integer> exchanger = new Exchanger<Integer>();
			@Override
			public Boolean call() throws Exception {
				try {
					//test if requesting a DAOManager in the main thread 
					//has affected hasDAOManager in this thread
					hasAManager = DAOManager.hasDAOManager();
			        
			        //main thread will be wake up by the finally statement
			        
			        return true;
			        
				} finally {
					//whatever happens, make sure to re-launch the main thread, 
					//as we do not use an Executor that might catch the Exception 
					//and interrupt the other Thread. 
					this.exchanger.exchange(null);
				}
			}
		};

		assertFalse("hasDAOManager returned a incorrect value in the main thread", 
				DAOManager.hasDAOManager());
		DAOManager manager = DAOManager.getDAOManager();
		assertTrue("hasDAOManager returned an incorrect value in the main thread", 
				DAOManager.hasDAOManager());
		//launch a second thread to see if hasDAOManager is correct in that thread
		ThreadTest test = new ThreadTest();
		ExecutorService executorService = Executors.newFixedThreadPool(1);
		Future<Boolean> future = executorService.submit(test);
		//wait for this thread's turn
		test.exchanger.exchange(null);
		//check that no exception was thrown in the second thread.
		//In that case, it would be completed and calling get would throw 
		//the exception. 
		future.get();

		assertFalse("hasDAOManager returned a incorrect value in the second thread", 
				test.hasAManager);

		//test after closing
		manager.close();	
		assertFalse("hasDAOManager returned a incorrect value in the main thread", 
				DAOManager.hasDAOManager());
	}
	
	/**
	 * Test the getXXXDAO() methods.
	 */
	@Test
	public void shouldGetDAOs() {
		DAOManager manager = DAOManager.getDAOManager();
		DAOManager mockManager = ((MockDAOManager) manager).instanceMockManager;
		
		manager.getSourceDAO();
		//check that the underlying abstract method was called
		verify(mockManager, times(1)).getNewSourceDAO();
		
		//trying to get a DAO on a closed DAOManager should throw an IllegalStateException
		manager.close();
		try {
			manager.getSourceDAO();
			//if we reach this point, test failed
		    throw new AssertionError("An IllegalStateException should have been thrown");
		} catch (IllegalStateException e) {
			//test passed
			log.catching(Level.DEBUG, e);
			//check that the underling method was indeed not called again
			verify(mockManager, times(1)).getNewSourceDAO();
		}
		
		//release the DAOManager without calling closeAll(), 
		//that would make other test to fail
		manager.close();
	}
}
