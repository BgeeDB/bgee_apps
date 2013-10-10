package org.bgee.model.dao.api;

import static org.junit.Assert.*;

import java.util.concurrent.Callable;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * A class to test the functionality of {@link DAOManager#closeAll()}.
 * Following a call to this method, it is not possible to acquire 
 * new {@code DAOManager}s from the same {@code ClassLoader}. 
 * So we configured the project to use a different {@code ClassLoader} 
 * for each test class, and we put the test of {@code closeAll} 
 * in a separate class.
 *  
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class CloseAllTest extends TestAncestor {
	/**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
    		LogManager.getLogger(CloseAllTest.class.getName());

	@Override
	protected Logger getLogger() {
		return log;
	}
	
	/**
	 * Test {@link DAOManager#closeAll()}.
	 */
	@Test
	public void shouldCloseAll() throws Exception {

		/**
		 * An anonymous class to acquire {@code DAOManager}s 
		 * from a different thread than this one, 
		 * and to be run alternatively to the main thread.
		 */
		class ThreadTest implements Callable<Boolean> {
			public volatile DAOManager manager;
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
			        
			        //the main thread has called closeAll, trying to acquire 
			        //a DAOManager should throw an IllegalStateException
			        DAOManager.getDAOManager();
			        
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

		//test closeAll
        assertEquals("closeAll did not return the expected number of managers closed", 
        		2, DAOManager.closeAll());
        //The managers in both threads should have been closed
        assertTrue("DAOManager in main thread was not closed", manager.isClosed());
        assertTrue("DAOManager in second thread was not closed", 
        		test.manager.isClosed());
        
        //check that an IllegalStateException is thrown if we try 
        //to acquire a new DAOManager.
        try {
        	DAOManager.getDAOManager();
        	//if we reach this point, test failed
        	throw new AssertionError("IllegalStateException not thrown in main thread");
        } catch (IllegalStateException e) {
        	log.catching(Level.DEBUG, e);
        }
        
        //relaunch the other thread so that it can try a DAOManager again 
		//(it should throw an IllegalStateException)
        test.exchanger.exchange(null);
        //wait for this thread's turn
        test.exchanger.exchange(null);
        //check that an IllegalStateException was thrown 
        try {
        	future.get();
        	//if we reach this point, test failed
        	throw new AssertionError(
        			"IllegalStateException not thrown in the second thread");
        } catch (ExecutionException e) {
        	if (e.getCause() instanceof IllegalStateException) {
        	    log.catching(Level.DEBUG, e);
        	} else {
	        	throw new AssertionError(
	        		"IllegalStateException not thrown in the second thread");
        	}
        }
        
        //now, check that shutdown was called on only one representative of mock DAOManager
        verify(MockDAOManager.mockManager, times(1)).shutdown();
        verify(MockDAOManager2.mockManager, times(0)).shutdown();
	}
}
