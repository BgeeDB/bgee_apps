package org.bgee.model.job;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeProperties;
import org.bgee.model.job.exception.JobIdAlreadyRegisteredException;
import org.bgee.model.job.exception.ThreadAlreadyWorkingException;
import org.bgee.model.job.exception.TooManyJobsException;

/**
 * Class allowing to manage and retrieve {@code Job}s. This class is designed to be accessed 
 * concurrently by multiple threads managing/tracking {@code Job}s.
 * <p>
 * A good practice would be to store an instance of this class in the {@code static final} attribute 
 * of a class, to share this same instance across all threads run by the application. 
 * This class is not based on the use of static attributes, so instantiating one instance 
 * of this class per thread would be pointless.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Oct. 2016
 * @see Job
 * @since Bgee 13
 */
public class JobService {
    private final static Logger log = LogManager.getLogger(JobService.class.getName());
   
    /**
     * A {@code ConcurrentMap} storing the running {@code Job}s associated with the value 
     * returned by {@link Job#getId()}. This {@code Map} is used to retrieve a {@code Job} 
     * from different {@code Thread}s (see {@link #getJob(long)}).
     * <p>
     * We can not simply use the ID of the {@code Thread} running the task, 
     * as it is the case in {@link #threadIdsToManagers}, because we need to be able 
     * to assign an ID before actually launching the job, or the {@code Thread} running the job.
     * 
     * @see #getJob(long)
     * @see #threadIdsToJobs
     */
    private final ConcurrentMap<Long, Job> livingJobs;
    /**
     * A {@code Set} of {@code Long}s that are job IDs reserved before the jobs are actually created 
     * and job IDs of currently running jobs. 
     * It is useful when a thread needs to know the ID of a job, to track it, before the job is created 
     * in another thread. This way, the first thread can preempt an ID, and provides it 
     * to the second thread so that it can create a job using it.
     * <p>
     * This {@code Set} contains IDs of both reserved and running jobs, for convenience. 
     * <p>
     * This {@code Set} allows concurrent access, as it is backed by a {@code ConcurrentMap}.
     */
    private final Set<Long> reservedAndLivingJobIds;
    /**
     * A {@code ConcurrentMap} storing the living {@code Job}s associated with the ID 
     * of the {@code Thread} running the task. It is useful when calling {@link #getJob()}, 
     * to retrieve the {@code Job} associated with the current {@code Thread}. 
     * It is also to make sure that a given {@code Thread} is associated with at most one {@code Job}. 
     */
    //Note: don't use a ThreadLocal, because they are a pain...
    private final ConcurrentMap<Long, Job> threadIdsToLivingJobs; 
    /**
     * A {@code ConcurrentMap} storing the number of currently running jobs per user. 
     * Users are identified through an ID used as key in the {@code Map}. 
     * Values do not need to be {@code AtomicInteger} because we will update them atomically.
     * When a job is registered with a {@code null} user ID, then no restriction is applied 
     * on the number of jobs for that "anonymous" user. 
     */
    private final ConcurrentMap<String, Integer> jobCountPerUser;
    /**
     * @see #getProps()
     */
    private final BgeeProperties props;
    
    /**
     * Construct a new {@code JobService} using the provided {@code BgeeProperties} 
     * to retrieve job-related properties. 
     */
    public JobService(BgeeProperties props) {
        this.livingJobs = new ConcurrentHashMap<>();
        this.reservedAndLivingJobIds = ConcurrentHashMap.newKeySet();
        this.threadIdsToLivingJobs = new ConcurrentHashMap<>(); 
        this.jobCountPerUser = new ConcurrentHashMap<>(); 
        
        this.props = props;
    }
    
    /**
     * Instantiate a new {@code Job} with a generated ID and no user ID defined. 
     * See {@link #registerNewJob(long, String, String, int)} for more details.
     * 
     * @throws ThreadAlreadyWorkingException   If the caller {@code Thread} was already held 
     *                                         when calling this method.
     * @see #registerNewJob(long, String, String, int)
     */
    public Job registerNewJob() throws ThreadAlreadyWorkingException {
        log.traceEntry();
        try {
            return log.traceExit(this.registerNewJob((String) null));
        } catch (TooManyJobsException e) {
            //Should never happen since we defined a null user ID
            log.catching(e);
            throw log.throwing(new IllegalStateException(
                    "No user was defined, so not too many jobs should be seen", e));
        }
    }
    /**
     * Instantiate a new {@code Job} with a generated ID for the user ID provided. 
     * See {@link #registerNewJob(long, String, String, int)} for more details.
     *  
     * @param userId       A {@code String} that is the ID of a user, allowing to track 
     *                     number of jobs per user. If {@code null}, then no restriction 
     *                     on the number of running jobs is applied. 
     * @throws ThreadAlreadyWorkingException   If the caller {@code Thread} was already held 
     *                                         when calling this method.
     * @throws TooManyJobsException            If a user ID is provided and the user already has 
     *                                         too many running jobs
     * @see #registerNewJob(long, String, String, int)
     */
    public Job registerNewJob(String userId) throws ThreadAlreadyWorkingException, TooManyJobsException {
        log.entry(userId);
        try {
            return log.traceExit(this.registerNewJob(this.reserveAndGetJobId(), userId));
        } catch (JobIdAlreadyRegisteredException e) {
            //Should never happen, reserveAndGetJobId guarantees the job ID is not used 
            log.catching(e);
            throw log.throwing(new IllegalStateException(
                    "It should be guaranteed that the job ID was not in use", e));
        }
    }
    /**
     * Instantiate a new {@code Job} with the provided ID and no user ID defined. 
     * See {@link #registerNewJob(long, String, String, int)} for more details.
     *  
     * @param jobId         a {@code long} that will be the ID of the {@code Job}. Note that 
     *                      this ID should always be acquired by calling {@link #reserveAndGetJobId()}, 
     *                      otherwise, different interleaved threads might "steal" a registered ID.
     * @throws JobIdAlreadyRegisteredException If there is already a {@code Job} associated with {@code jobId}.
     * @throws ThreadAlreadyWorkingException   If the caller {@code Thread} was already held 
     *                                         when calling this method.
     * @see #registerNewJob(long, String, String, int)
     * @see #reserveAndGetJobId()
     */
    public Job registerNewJob(long jobId) throws JobIdAlreadyRegisteredException, ThreadAlreadyWorkingException {
        log.entry(jobId);
        try {
            return log.traceExit(this.registerNewJob(jobId, null));
        } catch (TooManyJobsException e) {
            //Should never happen since we defined a null user ID
            log.catching(e);
            throw log.throwing(new IllegalStateException(
                    "No user was defined, so not too many jobs should be seen", e));
        }
    }
    /**
     * Instantiate a new {@code Job} with the provided ID and user ID. 
     * See {@link #registerNewJob(long, String, String, int)} for more details.
     *  
     * @param jobId         a {@code long} that will be the ID of the {@code Job}. Note that 
     *                      this ID should always be acquired by calling {@link #reserveAndGetJobId()}, 
     *                      otherwise, different interleaved threads might "steal" a registered ID.
     * @param userId        A {@code String} that is the ID of a user, allowing to track 
     *                      number of jobs per user. If {@code null}, then no restriction 
     *                      on the number of running jobs is applied. 
     * @throws JobIdAlreadyRegisteredException If there is already a {@code Job} associated with {@code jobId}.
     * @throws ThreadAlreadyWorkingException   If the caller {@code Thread} was already held 
     *                                         when calling this method.
     * @throws TooManyJobsException            If a user ID is provided and the user already has 
     *                                         too many running jobs
     * @see #registerNewJob(long, String, String, int)
     * @see #reserveAndGetJobId()
     */
    public Job registerNewJob(long jobId, String userId) 
            throws JobIdAlreadyRegisteredException, ThreadAlreadyWorkingException, TooManyJobsException {
        log.entry(jobId, userId);
        return log.traceExit(this.registerNewJob(jobId, userId, null, 0));
    }
    /**
     * Instantiate a new {@code Job} with a generated ID and no user ID defined with the provided 
     * job name and number of sub-tasks. See {@link #registerNewJob(long, String, String, int)} 
     * for more details.
     * 
     * @param jobName      A {@code String} that is the name given to the job, for convenience.
     * @param taskCount    An {@code int} that is the number of sub-tasks that the job 
     *                     will required (see {@link Job#getCurrentTaskIndex()} and 
     *                     {@link Job#incrementCurrentTaskIndex()})
     * @throws ThreadAlreadyWorkingException   If the caller {@code Thread} was already held 
     *                                         when calling this method.
     * @see #registerNewJob(long, String, String, int)
     */
    public Job registerNewJob(String jobName, int taskCount) throws ThreadAlreadyWorkingException {
        log.entry(jobName, taskCount);
        try {
            return log.traceExit(this.registerNewJob(null, jobName, taskCount));
        } catch (TooManyJobsException  e) {
            //Should never happen since we defined a null user ID
            log.catching(e);
            throw log.throwing(new IllegalStateException(
                    "No user was defined, so not too many jobs should be seen", e));
        }
    }
    /**
     * Instantiate a new {@code Job} with a generated ID for the user ID provided and job name 
     * and number of sub-tasks. See {@link #registerNewJob(long, String, String, int)} for more details.
     *  
     * @param userId       A {@code String} that is the ID of a user, allowing to track 
     *                     number of jobs per user. If {@code null}, then no restriction 
     *                     on the number of running jobs is applied. 
     * @param jobName      A {@code String} that is the name given to the job, for convenience.
     * @param taskCount    An {@code int} that is the number of sub-tasks that the job 
     *                     will required (see {@link Job#getCurrentTaskIndex()} and 
     *                     {@link Job#incrementCurrentTaskIndex()})
     * @throws ThreadAlreadyWorkingException   If the caller {@code Thread} was already held 
     *                                         when calling this method.
     * @throws TooManyJobsException            If a user ID is provided and the user already has 
     *                                         too many running jobs
     * @see #registerNewJob(long, String, String, int)
     */
    public Job registerNewJob(String userId, String jobName, int taskCount) 
            throws ThreadAlreadyWorkingException, TooManyJobsException {
        log.entry(userId, jobName, taskCount);
        try {
            return log.traceExit(this.registerNewJob(this.reserveAndGetJobId(), userId, jobName, taskCount));
        } catch (JobIdAlreadyRegisteredException e) {
            //Should never happen, reserveAndGetJobId guarantees the job ID is not used 
            log.catching(e);
            throw log.throwing(new IllegalStateException(
                    "It should be guaranteed that the job ID was not in use", e));
        }
    }
    /**
     * Instantiate a new {@code Job} that will hold the caller {@code Thread} 
     * (so that it can be interrupted from another {@code Thread}), and map it to {@code jobId}, 
     * so that the {@code Job} can then be retrieved by a call to {@link #getJob(long)}, 
     * from any {@code Thread}. 
     * <p>
     * Any following call from the caller {@code Thread} to {@link #getJob()} 
     * will return the same {@code Job} instance, unless {@link Job#release()} was called on it.
     * <p>
     * It might be necessary to reserve a job ID before the job is actually launched, 
     * see {@link #reserveAndGetJobId()}.
     * <p>
     * If the caller {@code Thread} was already held when calling this method, 
     * a {@code ThreadAlreadyWorkingException} is thrown. If there is already a {@code Job} 
     * associated with {@code jobId}, a {@code JobIdAlreadyRegisteredException} is thrown. 
     * If a user ID is provided and the user already has too many running jobs (see 
     * {@link BgeeProperties#getMaxJobCountPerUser()}), a {@code TooManyJobsException} is thrown.
     *  
     * @param jobId         a {@code long} that will be the ID of the {@code Job}. Note that 
     *                      this ID should always be acquired by calling {@link #reserveAndGetJobId()}, 
     *                      otherwise, different interleaved threads might "steal" a registered ID.
     * @param userId        A {@code String} that is the ID of a user, allowing to track 
     *                      number of jobs per user. If {@code null}, then no restriction 
     *                      on the number of running jobs is applied. 
     * @param jobName       A {@code String} that is the name given to the job, for convenience.
     * @param taskCount     An {@code int} that is the number of sub-tasks that the job 
     *                      will required (see {@link Job#getCurrentTaskIndex()} and 
     *                      {@link Job#incrementCurrentTaskIndex()})
     * @throws JobIdAlreadyRegisteredException If there is already a {@code Job} associated with {@code jobId}.
     * @throws ThreadAlreadyWorkingException   If the caller {@code Thread} was already held 
     *                                         when calling this method.
     * @throws TooManyJobsException            If a user ID is provided and the user already has 
     *                                         too many running jobs
     * @see #reserveAndGetJobId()
     * @see #getJob()
     * @see #getJob(long)
     */
    public Job registerNewJob(long jobId, String userId, String jobName, int taskCount) 
            throws JobIdAlreadyRegisteredException, ThreadAlreadyWorkingException, TooManyJobsException {
        log.entry(jobId, userId, jobName, taskCount);

        Job job = new Job(jobId, userId, jobName, taskCount, Thread.currentThread(), this);
        
        //First, check that the job ID is not already used by a running job. 
        //It is allowed to have the ID already stored in reservedAndLivingJobIds, 
        //as it could have been reserved before.
        if (this.livingJobs.putIfAbsent(jobId, job) != null) {
            throw log.throwing(new JobIdAlreadyRegisteredException());
        }
        //also reserve the jobId
        boolean reserved = this.reservedAndLivingJobIds.add(jobId);
        
        //then, check that there is no Job already associated with the current Thread
        if (this.threadIdsToLivingJobs.putIfAbsent(Thread.currentThread().getId(), job) != null) {
            //cancel operations already performed
            this.livingJobs.remove(jobId);
            if (reserved) {
                this.reservedAndLivingJobIds.remove(jobId);
            }
            //rethrow exception appropriately
            throw log.throwing(new ThreadAlreadyWorkingException());
        }
        
        //Finally, if a user ID was provided, check that the maximum number of allowed running jobs 
        //is not exceeded. 
        final Integer maxJobCount = this.props == null? 0: this.props.getMaxJobCountPerUser();
        if (userId != null && maxJobCount != null && maxJobCount > 0) {
            final TooManyJobsException realException = new TooManyJobsException(maxJobCount);
            try {
                this.jobCountPerUser.compute(userId, (k, v) -> {
                    if (v == null) {
                        return 1;
                    }
                    if (v + 1 > maxJobCount) {
                        throw new RuntimeException(realException);
                    }
                    return (v + 1);
                });
            } catch (RuntimeException e) {
                //cancel operations already performed
                this.livingJobs.remove(jobId);
                if (reserved) {
                    this.reservedAndLivingJobIds.remove(jobId);
                }
                this.threadIdsToLivingJobs.remove(Thread.currentThread().getId());
                
                //rethrow exception appropriately 
                if (realException.equals(e.getCause())) {
                    throw log.throwing(Level.DEBUG, realException);
                }
                throw log.throwing(e);
            }
        }
        
        if (log.isInfoEnabled()) {
            log.info("New Job with ID {} for Thread {} registered - user {} - "
                    + "number of running jobs for user: {} - max number of jobs allowed: {}", 
                    jobId, Thread.currentThread().getId(), userId, 
                    userId != null? this.jobCountPerUser.get(userId): null, 
                    maxJobCount);
        }
        return log.traceExit(job);
    }
    
    /**
     * Allows to reserve a new job ID before a {@code Job} is actually created. 
     * It is useful when a thread needs to know the ID of a job, to track it, before the job is created 
     * in another thread. This way, the first thread can preempt an ID, and provides it 
     * to the second thread so that it can create a job using it.
     * 
     * @return A {@code Long} that is an unused job ID, that can be used to create a new {@code Job} 
     *         using a {@code registerNewJob} method.
     * @throws IllegalStateException   If it was not possible to generate a unique job ID 
     *                                 in reasonable amount of retries. 
     */
    public long reserveAndGetJobId() throws IllegalStateException {
        log.traceEntry();
        return log.traceExit(this.reserveAndGetJobId(0));
    }
    /**
     * Allows to reserve a new job ID before a {@code Job} is actually created. 
     * The only difference as compared to {@link #reserveAndGetJobId()} is that this method 
     * accepts as argument the total number of retries already done to acquire an unused ID.
     * 
     * @return A {@code Long} that is an unused job ID, that can be used to create a new {@code Job} 
     *         using a {@code registerNewJob} method.
     * @throws IllegalStateException   If it was not possible to generate a unique job ID 
     *                                 in reasonable amount of retries. 
     * @see #reserveAndGetJobId()
     */
    private long reserveAndGetJobId(int retryCount) throws IllegalStateException {
        log.entry(retryCount);
        
        //To generate the ID, we use the current Thread ID, and we multiply it using current timestamp. 
        //The aim is to get a not too obvious ID, as job can be canceled based on their IDs 
        //by another thread.
        long jobId = Thread.currentThread().getId() * (System.nanoTime() % 10000);
        int i = retryCount;
        int maxRetries = 10000;
        //we also check livingJobs, because the method 'registerNewJob' and this method are not atomic
        while ((this.livingJobs.containsKey(jobId) || !this.reservedAndLivingJobIds.add(jobId)) 
                && i < maxRetries) {
            jobId = jobId * 3;
            i++;
        }
        if (i > maxRetries) {
            throw log.throwing(new IllegalStateException("No unique job ID could be generated."));
        }
        
        //now that we have reserved the ID in reservedAndLivingJobIds, we check that the ID 
        //was not "stolen" in the meantime by calling registerNewJob, as we don't provide atomicity.
        if (this.livingJobs.containsKey(jobId)) {
            //well, let's try again... number of retries is preserved
            return log.traceExit(this.reserveAndGetJobId(i));
        }
        
        return log.traceExit(jobId);
    }
    
    /**
     * Check whether the user would have too many running jobs if he/she was launching another one. 
     * This method checks whether the current number of running jobs is equal to or greater than 
     * the max allowed number of running jobs per user, and throws a {@code TooManyJobsException} 
     * if it is the case.
     * 
     * @param userId                        A {@code String} that is the ID of the user for which 
     *                                      to check the number of running jobs. 
     * @throws IllegalArgumentException     If {@code userId} is {@code null}.
     * @throws TooManyJobsException         If the number of running jobs for user with ID {@code userId}
     *                                      is equal to or greater than the max allowed number of 
     *                                      running jobs. 
     */
    public void checkTooManyJobs(String userId) throws IllegalArgumentException, TooManyJobsException {
        log.entry(userId);
        if (userId == null) {
            throw log.throwing(new IllegalArgumentException("A user ID must be provided"));
        }
        final Integer maxJobCount = this.props == null? null: this.props.getMaxJobCountPerUser();
        final Integer jobCount = this.jobCountPerUser.get(userId);
        if (maxJobCount != null && maxJobCount > 0 && jobCount != null && jobCount >= maxJobCount) {
            throw log.throwing(Level.DEBUG, new TooManyJobsException(maxJobCount));
        }
        log.debug("User {} currently has {} running jobs, max number of running jobs {}, it's OK", 
                userId, jobCount, maxJobCount);
        log.traceExit();
    }
    
    /**
     * @return     the {@code Job} currently associated with the caller {@code Thread}. 
     *             {@code null} if no {@code Job} was associated to the caller {@code Thread} 
     *             (by previously calling a {@code #registerNewJob} method).
     * @see #getJob(long)
     */
    public Job getJob() {
        log.traceEntry();
        return log.traceExit(this.threadIdsToLivingJobs.get(Thread.currentThread().getId()));
    }
    /**
     * Get the {@code Job} associated with {@code id}. 
     * 
     * @param id   a {@code long} that is the ID of the desired {@code Job}. 
     * @return     The {@code Job} mapped to {@code id}, {@code null} if no {@code Job} 
     *             was associated with {@code id} (by previously calling a {@code #registerNewJob} method).
     */
    public Job getJob(long id) {
        log.entry(id);
        return log.traceExit(this.livingJobs.get(id));
    }
    
    /**
     * Release {@code job} so that no reference to it or to the {@code Thread} it holds are kept.
     * Number of jobs for the corresponding user, if any, is updated accordingly.
     * 
     * @param job                       The {@code Job} to be released.
     * @throws IllegalArgumentException If {@code job} was not properly terminated 
     *                                  before calling this method, or was already released.
     * @throws IllegalStateException    If {@code job} was not yet released (see {@link Job#isReleased()}), 
     *                                  but could not be found in this {@code JobService}.
     */
    protected void releaseJob(Job job) throws IllegalArgumentException, IllegalStateException {
        log.entry(job);
        
        if (job.isReleased()) {
            throw log.throwing(new IllegalArgumentException("The job has already been released."));
        }
        if (!job.isTerminated()) {
            throw log.throwing(new IllegalArgumentException(
                    "The job was not properly terminated before releasing it."));
        }
        log.debug("Releasing Job: {}", job);
        
        //here we do not provide atomicity, because in the worst case scenario, 
        //exception will be thrown by registerNewJob method because of an illegal state.
        if (this.threadIdsToLivingJobs.remove(job.getExecutor().getId()) == null || 
                !this.reservedAndLivingJobIds.remove(job.getId()) ||
                //remove from livingJobs at last since this is what is used to block concurrent registration 
                //in registerNewJob
                this.livingJobs.remove(job.getId()) == null || 
                //number of jobs per user is not as essential
                (job.getUserId() != null && !this.jobCountPerUser.containsKey(job.getUserId()))) {
            
            throw log.throwing(new IllegalStateException(
                    "A job that was not yet released could not be found in the JobService"));
        }
        if (job.getUserId() != null) {
            this.jobCountPerUser.compute(job.getUserId(), (k, v) -> v - 1 == 0? null: v - 1);
        }
        
        log.traceExit();
    }
    
    /**
     * @return  A {@code BgeeProperties} object defining properties used by this {@code JobService}.
     *          See {@link BgeeProperties#getMaxJobCountPerUser()}.
     */
    public BgeeProperties getProps() {
        return props;
    }
}
