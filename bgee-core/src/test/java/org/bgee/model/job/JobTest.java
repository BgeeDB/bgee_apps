package org.bgee.model.job;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.TestAncestor;
import org.bgee.model.job.JobServiceTest.ThreadTest;
import org.bgee.model.job.exception.ThreadAlreadyWorkingException;
import org.bgee.model.job.exception.TooManyJobsException;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Unit tests for {@link Job}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Oct. 2016
 * @since Bgee 13
 */
public class JobTest extends TestAncestor {
    private final static Logger log = LogManager.getLogger(JobTest.class.getName());
    @Override
    protected Logger getLogger() {
        return log;
    } 
    
    /**
     * Test the method {@link Job#release()}.
     */
    @Test
    public void shouldRelease() {
        Thread t = mock(Thread.class);
        JobService service = mock(JobService.class);
        
        Job job = new Job(1L, null, null, 0, t, service);
        assertFalse("Incorrect terminated status", job.isTerminated());
        job.release();
        assertTrue("Incorrect released status", job.isReleased());
        assertTrue("Incorrect terminated status", job.isTerminated());
        assertFalse("Incorrect successful status", job.isSuccessful());
        verify(service, times(1)).releaseJob(job);
        
        //Chek that releasing again does not cause to call the service again
        job.release();
        verify(service, times(1)).releaseJob(job);
        
        //Check that it does not impact the success status
        job = new Job(1L, null, null, 0, t, service);
        job.completeWithSuccess();
        assertTrue("Incorrect terminated status", job.isTerminated());
        assertTrue("Incorrect successful status", job.isSuccessful());
        job.release();
        assertTrue("Incorrect released status", job.isReleased());
        assertTrue("Incorrect terminated status", job.isTerminated());
        assertTrue("Incorrect successful status", job.isSuccessful());
        verify(service, times(1)).releaseJob(job);
    }
    
    /**
     * Test methods {@link Job#interrupt()} and {@link Job#checkInterrupted()}.
     */
    @Test
    public void shouldInterrupt() throws InterruptedException, ExecutionException {
        Thread t = Thread.currentThread();
        JobService service = mock(JobService.class);
        
        //Test to interrupt job in another thread 
        /**
         * An anonymous class to acquire {@code Job}s from a different thread than this one, 
         * and to be run alternatively to the main thread.
         */
        class ThreadTest implements Callable<Job> {
            @Override
            public Job call() throws InterruptedException, ThreadAlreadyWorkingException, TooManyJobsException {
                Thread t = Thread.currentThread();
                JobService service = mock(JobService.class);
                
                Job job = new Job(1L, null, null, 0, t, service);
                
                //calling checkInterrupted should not throw an exception at this point
                job.checkInterrupted();
                
                return job;
            }
        }

        ThreadTest test = new ThreadTest();
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Future<Job> future = executorService.submit(test);
        //check that no exception was thrown in the second thread.
        //In that case, it would be completed and calling get would throw 
        //the exception. 
        Job job = future.get();
        job.interrupt();
        assertFalse("The main thread should not have an interrupted status", Thread.interrupted());
        
        //test that it not possible to call checkInterrupted from different threads than the executor
        try {
            job.checkInterrupted();
            //test failed
            throw log.throwing(new AssertionError("An IllegalStateException should have been thrown"));
        } catch (IllegalStateException e) {
            //test passed
        }
        
        //check that calling checkInterrupted in the same thread throws an exception
        job = new Job(1L, null, null, 0, t, service);
        job.interrupt();
        assertTrue("The main thread should have an interrupted status", 
                Thread.currentThread().isInterrupted());
        try {
            job.checkInterrupted();
            //test failed
            throw log.throwing(new AssertionError("An IllegalStateException should have been thrown"));
        } catch (InterruptedException e) {
            //test passed
        }
        
        //we reset the Thread interruption status to check that checkInterrupted still works 
        //(regression test, because of some libraries that improperly reinit the Thread interruption state)
        Thread.interrupted();
        try {
            job.checkInterrupted();
            //test failed
            throw log.throwing(new AssertionError("An IllegalStateException should have been thrown"));
        } catch (InterruptedException e) {
            //test passed
        }
    }
}
