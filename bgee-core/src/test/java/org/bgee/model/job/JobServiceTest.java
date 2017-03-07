package org.bgee.model.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeProperties;
import org.bgee.model.TestAncestor;
import org.bgee.model.job.JobService;
import org.bgee.model.job.exception.JobIdAlreadyRegisteredException;
import org.bgee.model.job.exception.ThreadAlreadyWorkingException;
import org.bgee.model.job.exception.TooManyJobsException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link JobService}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Oct. 2016
 * @since Bgee 13
 */
public class JobServiceTest extends TestAncestor {
    private final static Logger log = LogManager.getLogger(JobServiceTest.class.getName());
    @Override
    protected Logger getLogger() {
        return log;
    } 

    /**
     * An anonymous class to acquire {@code Job}s from a different thread than this one, 
     * and to be run alternatively to the main thread.
     */
    class ThreadTest implements Callable<Job> {

        public final JobService service;
        public final String userId;
        
        public ThreadTest(JobService service, String userId) {
            this.service = service;
            this.userId = userId;
        }
        @Override
        public Job call() throws InterruptedException, ThreadAlreadyWorkingException, TooManyJobsException {
            return this.service.registerNewJob(this.userId);
        }
    }
    
    private JobService service;
    
    @Before
    public void init() {
        // set the properties to inject
        Properties prop = new Properties();
        prop.put(BgeeProperties.MAX_JOB_COUNT_PER_USER_KEY, 5);

        this.service = new JobService(BgeeProperties.getBgeeProperties(prop));
    }
    
    @After
    public void destroy() {
        this.service.getProps().release();
        this.service = null;
    }

    /**
     * Test the process of registering and releasing {@code Job}s from different threads, 
     * see {@link JobService#registerNewJob()} and {@link Job#release()}. 
     * 
     * @throws ThreadAlreadyWorkingException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Test
    public void shouldRegisterAndReleaseNewJob() throws ThreadAlreadyWorkingException, InterruptedException, 
    ExecutionException, JobIdAlreadyRegisteredException {
        final JobService service = this.service;
        
        //FIRST TEST: try (and fail) to obtain two jobs in a same Thread
        Job job1 = service.registerNewJob();
        assertNotNull("Could not acquire a job", job1);
        try {
            service.registerNewJob();
            //test failed
            throw log.throwing(new AssertionError("A ThreadAlreadyWorkingException should have been thrown"));
        } catch (ThreadAlreadyWorkingException e) {
            //test passed
        } finally {
            if (job1 != null) {
                job1.release();
            }
        }
        
        //SECOND TEST: launch a second thread also acquiring a Job
        job1 = service.registerNewJob(); //this also test that we can acquire a new job after release
        assertNotNull("Could not acquire a job", job1);
        Job job2 = null;
        ThreadTest test = new ThreadTest(service, null);
        try {
            ExecutorService executorService = Executors.newFixedThreadPool(1);
            Future<Job> future = executorService.submit(test);
            //check that no exception was thrown in the second thread.
            //In that case, it would be completed and calling get would throw 
            //the exception. 
            job2 = future.get();
            assertNotNull("Could not acquire a second job", job2);
            assertNotSame("Same job registered twice", job1, job2);
            assertNotEquals("Two jobs with same ID", job1.getId(), job2.getId());
        } finally {
            if (job1 != null) {
                job1.release();
            }
            if (job2 != null) {
                job2.release();
            }
        }
        
        //THIRD TEST: try (and fail) to acquire a second job with a same ID
        job1 = null;
        job2 = null;
        test = new ThreadTest(service, null);
        try {
            ExecutorService executorService = Executors.newFixedThreadPool(1);
            Future<Job> future = executorService.submit(test);
            //check that no exception was thrown in the second thread.
            //In that case, it would be completed and calling get would throw 
            //the exception. 
            job2 = future.get();
            
            //now try (and fail) to obtain in the main Thread a job with same ID
            try {
                job1 = service.registerNewJob(job2.getId());
                //test failed
                throw log.throwing(new AssertionError("A JobIdAlreadyRegisteredException should have been thrown"));
            } catch (JobIdAlreadyRegisteredException e) {
                //test passed
            }
            
            //now try to see if we can still get a job from the main thread anyway
            job1 = service.registerNewJob();
            assertNotNull(job1);
            
        } finally {
            if (job1 != null) {
                job1.release();
            }
            if (job2 != null) {
                job2.release();
            }
        }
        
        //FOURTH test: check that we can obtain jobs with same ID after release
        job1 = null;
        try {
            job1 = service.registerNewJob(1L);
            assertNotNull(job1);
            job1.release();
            job1 = service.registerNewJob(1L);
            assertNotNull(job1);
        } finally {
            if (job1 != null) {
                job1.release();
            }
        }
    }
    
    /**
     * Test {@code registerNewJob} methods when exceeding max allowed number of jobs for a user.
     */
    @Test
    public void shouldRegisterTooManyJobs() throws InterruptedException, ExecutionException, 
    ThreadAlreadyWorkingException, TooManyJobsException {
        final JobService service = this.service;
        
        final int maxJobCount = service.getProps().getMaxJobCountPerUser();
        //make sure we have defined a max allowed number of threads
        assertTrue("The JobService initialized does not allow to test the method", 
                maxJobCount > 0 && maxJobCount <= 10);

        Set<Job> concurrentJobs = ConcurrentHashMap.newKeySet();
        try {
            String userId = "MyID1";
            int i = 0;
            while (i < maxJobCount) {
                ThreadTest test = new ThreadTest(service, userId);
                ExecutorService executorService = Executors.newFixedThreadPool(1);
                Future<Job> future = executorService.submit(test);
                //check that no exception was thrown in the second thread.
                //In that case, it would be completed and calling get would throw 
                //the exception. 
                concurrentJobs.add(future.get());
                i++;
            }
            assertEquals("Incorrect number of jobs created", maxJobCount, concurrentJobs.size());
            
            //Check that we cannot run another job for the same user
            try {
                concurrentJobs.add(service.registerNewJob(userId));
                //test failed
                throw log.throwing(new AssertionError("A TooManyJobsException should have been thrown"));
            } catch (TooManyJobsException e) {
                //test passed
            }
            //But that we can still get a job for another user, or for no user
            concurrentJobs.add(service.registerNewJob("User2"));
            service.getJob().release();
            concurrentJobs.add(service.registerNewJob());
            service.getJob().release();
            
            //Check that removing a job associated with the user will allow to create a new job
            //First, check again that we can still not run another job for the same user
            try {
                concurrentJobs.add(service.registerNewJob(userId));
                //test failed
                throw log.throwing(new AssertionError("A TooManyJobsException should have been thrown"));
            } catch (TooManyJobsException e) {
                //test passed
            }
            concurrentJobs.stream()
            .filter(j -> userId.equals(j.getUserId()) && !j.isReleased())
            .findFirst().ifPresent(j -> j.release());
            concurrentJobs.add(service.registerNewJob(userId));
            
            //And check again that now we cannot acquire a new one from another thread
            ThreadTest test = new ThreadTest(service, userId);
            ExecutorService executorService = Executors.newFixedThreadPool(1);
            Future<Job> future = executorService.submit(test);
            //check that no exception was thrown in the second thread.
            //In that case, it would be completed and calling get would throw 
            //the exception. 
            try {
                concurrentJobs.add(future.get());
                //test failed
                throw log.throwing(new AssertionError("A TooManyJobsException should have been thrown"));
            } catch (ExecutionException e) {
                //test passed
                assertTrue("Incorrect exception thrown", e.getCause() instanceof TooManyJobsException);
            }
        } finally {
            concurrentJobs.stream().forEach(j -> j.release());
        }
    }
    
    /**
     * Test {@link JobService#getJob()} and {@link JobService#getJob(long)}.
     */
    @Test
    public void shouldGetJob() throws ThreadAlreadyWorkingException, InterruptedException, 
    ExecutionException {
        final JobService service = this.service;
        Job job1 = service.registerNewJob();
        Job job2 = null;
        
        try {
            assertSame("Incorrect job retrieved", job1, service.getJob());
            assertSame("Incorrect job retrieved", job1, service.getJob(job1.getId()));
            
            //try from a different thread
            ThreadTest test = new ThreadTest(service, null);
            ExecutorService executorService = Executors.newFixedThreadPool(1);
            Future<Job> future = executorService.submit(test);
            //check that no exception was thrown in the second thread.
            //In that case, it would be completed and calling get would throw 
            //the exception. 
            job2 = future.get();
            assertSame("Incorrect job retrieved", job2, service.getJob(job2.getId()));
            assertNotSame("Incorrect job retrieved", job2, service.getJob());
            
            job1.release();
            assertNull("No Job should be retrieved", service.getJob(job1.getId()));
            assertNull("No Job should be retrieved", service.getJob());
        } finally {
            if (job1 != null) {
                job1.release();
            }
            if (job2 != null) {
                job2.release();
            }
        }
    }
    
    /**
     * Test {@link JobService#reserveAndGetJobId()}.
     */
    @Test
    public void shouldReserveAndGetJobId() throws JobIdAlreadyRegisteredException, ThreadAlreadyWorkingException {
        final JobService service = this.service;
        long jobId1 = service.reserveAndGetJobId();
        long jobId2 = service.reserveAndGetJobId();
        assertNotNull("Incorrect job ID generated", jobId1);
        assertNotNull("Incorrect job ID generated", jobId2);
        assertNotEquals("Incorrect job IDs generated", jobId1, jobId2);
        
        //Check that we can acquire a job using this reserved IDs
        Job job = service.registerNewJob(jobId1);
        job.release();
        job = service.registerNewJob(jobId2);
        job.release();
    }
    
    /**
     * Test {@link JobService#checkTooManyJobs(String)}.
     */
    @Test
    public void shouldCheckTooManyJobs() throws IllegalArgumentException, TooManyJobsException, 
    ThreadAlreadyWorkingException {

        BgeeProperties props = mock(BgeeProperties.class);
        when(props.getMaxJobCountPerUser()).thenReturn(1);

        String userId = "1";
        JobService service = new JobService(props);
        //no exception should be thrown here
        service.checkTooManyJobs(userId);
        
        Job job = service.registerNewJob(userId);
        //should throw an exception now
        try {
            service.checkTooManyJobs(userId);
            //test failed
            throw new AssertionError("A TooManyJobsException should have been thrown");
        } catch (TooManyJobsException e) {
            //test passed
        }
        
        //another user should have no problem checking number of jobs
        service.checkTooManyJobs("2");
        
        //after finishing first job, first user should have no problem checking number of jobs
        job.release();
        service.checkTooManyJobs(userId);
    }
}
