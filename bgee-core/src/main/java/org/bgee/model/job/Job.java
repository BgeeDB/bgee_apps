package org.bgee.model.job;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.StartUpShutdown;

/**
 * This class allows to keep track of the advancement of long-running jobs, 
 * and provides means to interrupt this job. Instances of this class notably 
 * hold a reference to the {@code Thread} running the task, to be able 
 * to call {@code interrupt} on it from another {@code Thread} 
 * (see {@link #interrupt()}). 
 * <p>
 * A {@code Job} can be obtained from another {@code Thread} by using 
 * {@link JobService#getJob(long)} with the ID it was associated with. It is very important 
 * to call {@link #release()} when a {@code Job} is completed, otherwise a reference 
 * to the {@code Job} will be kept in the {@code JobService}, and both the {@code JobService} 
 * and the {@code Job} would keep a reference to the executor {@code Thread}).
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Oct. 2016
 * @since Bgee 13
 */
public class Job {
    private final static Logger log = LogManager.getLogger(Job.class.getName());
    
    //**********************************************************************
    // INSTANCE METHODS AND VARIABLES
    // ALL ATTRIBUTES ARE EITHER FINAL OR VOLATILE, BECAUSE THEY ARE MEANT 
    // TO BE READ AND WRITTEN BY DIFFERENT THREADS
    //**********************************************************************
    /**
     * @see #getJobService().
     */
    private final JobService jobService;
    /**
     * @see #getExecutor().
     */
    //XXX: should this field be declared volatile, so that it is set to null when 'release' is called? 
    //Maybe if we don't do that we'll have a circular reference to Thread through the jobService, 
    //preventing garbage collection, thus causing a memory leak.
    //But I think it is not an issue, because I think a terminated Thread is not considered 
    //as a garbage collection root anymore, and thus the subgraph containing this job and the thread 
    //could be garbage collected. Problem might arise in case of a Thread pool? 
    //Well, if we release the Job correctly, it shouldn't be any reference left to it anymore, 
    //and thus, no circular reference at all?
    private final Thread executor;
    /**
     * @see #getId()
     */
    private final long id;
    /**
     * @see #getName() 
     */
    private final String name;
    /**
     * @see #getUserId(). 
     */
    private final String userId;

    /**
     * @see #isStarted()
     */
    private volatile boolean started;
    /**
     * @see #isTerminated()
     */
    private volatile boolean terminated;
    /**
     * @see #isSuccessful()
     */
    private volatile boolean successful;
    /**
     * @see #isInterruptRequested()
     */
    private volatile boolean interruptRequested;
    /**
     * @see #isReleased()
     */
    private volatile boolean released;
    
    /**
     * @see #getTaskCount()
     */
    private final int taskCount;
    /**
     * @see #getCurrentTaskIndex()
     */
    private volatile int currentTaskIndex;
    /**
     * @see #getCurrentTaskName()
     */
    private volatile String currentTaskName;
    
    /**
     * Constructor protected, instances should be obtained using 
     * {@link JobService#registerJob(long)}.
     *
     * @param id            A {@code long} representing the ID this {@code Job} will be associated with.
     * @param userId        A {@code String} that is the ID corresponding to a user. Important to restrict 
     *                      number of concurrent jobs per user.
     * @param name          A {@code String} that is the name of this job, for information purpose.
     * @param taskCount     An {@code int} representing the total number of sub-tasks (meaning, 
     *                      "big steps" of the job) that the job will involve. Can be set to 0 
     *                      for not defining this information.
     * @param executor      The {@code Thread} executing this job.
     * @param jobService    The {@code JobService} that instantiated this {@code Job}.
     */
    protected Job(long id, String userId, String name, int taskCount, Thread executor, JobService jobService) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.executor = executor;
        this.jobService = jobService;
        
        this.terminated         = false;
        this.successful         = false;
        this.released           = false;
        this.interruptRequested = false;
        
        this.setCurrentTaskName("");
        this.taskCount   = taskCount;
        //We do not use the setters here, they would throw an IllegalArgumentException
        this.currentTaskIndex = -1;
    }
    
    
    //***********************
    // MANAGEMENT OF JOB TERMINATION
    //***********************
    /**
     * Method called to indicate that the job was completed with success.  
     * Following calls to {@link #isTerminated()} and {@link #isSuccessful()} will return {@code true}. 
     * <p>
     * Any call to a {@code complete} method on a same {@code Job} has effects only on the first call.
     * 
     * @see #release()
     * @see #complete()
     * @see #isTerminated()
     * @see #isSuccessful()
     */
    public void completeWithSuccess() {
        log.traceEntry();
        this.complete(true);
        log.traceExit();
    }
    /**
     * Method called to indicate that the job was terminated, either because of an error, 
     * or because it was interrupted. Following calls to {@link #isTerminated()} will return {@code true}, 
     * and calls to {@link #isSuccessful()} will return {@code false}. 
     * <p>
     * Any call to a {@code complete} method on a same {@code Job} has effects only on the first call.
     * 
     * @see #release()
     * @see #completeWithSuccess()
     */
    public void complete() {
        log.traceEntry();
        this.complete(false);
        log.traceExit();
    }
    /**
     * Method called to indicate that the job was terminated with success or not.
     * <p>
     * Any call to a {@code complete} method on a same {@code Job} has effects only on the first call.
     * 
     * @param success   A {@code boolean} defining whether the job waqs successfully completed.
     * @see #release()
     */
    private void complete(boolean success) {
        log.entry(success);
        //important to modify these values only once per Job
        if (!this.terminated) {
            this.terminated = true;
            this.successful = success;
        }
        log.traceExit();
    }
    
    /**
     * Interrupt the task associated with this {@code Job}. 
     * This method first interrupt any running task by calling {@link 
     * org.bgee.model.StartUpShutdown.interruptThread(Thread)}. This method then calls 
     * {@code interrupt} on the {@code Thread} running the task (see {@link #getExecutor()}). 
     * It is then the responsibility of the applicative code running the task 
     * to deal with the interrupted status (see {@link #checkInterrupted()}).
     */
    public void interrupt() {
        log.traceEntry();
        try {
            StartUpShutdown.interruptThread(this.executor);
        } finally {
            //set this boolean because some libraries reset the Thread interruption status...
            this.interruptRequested = true;
            this.executor.interrupt();
            //Do not release the Job from the JobService even if interrupted, 
            //so that another thread can check the status of this job. 
            //And do not call complete(), because it will be the responsibility 
            //of the executor Thread to do so once the job will be effectively stopped 
            //(see checkInterrupted()). 
        }
        log.traceExit();
    }
    /**
     * Throws an {@code InterruptedException} and interrupts this {@code Job} if the {@code Thread} 
     * running it has been interrupted (see {@link #getExecutor()}). Job implementations should call 
     * this method when they have the opportunity to interrupt the job if requested. 
     * This method is a convenience method only meant to be called from the thread running the job, 
     * otherwise an {@code IllegalStateException} is thrown if it is called from another thread.
     * <p>
     * {@link #complete()} will be called on this {@code Job} prior to launching 
     * the {@code InterruptedException}.
     * 
     * @throws IllegalStateException    If this method is called from a {@code Thread} different from 
     *                                  the executor {@code Thread} (see {@link #getExecutor()}).
     * @throws InterruptedException     If the executor {@code Thread} running this job (therefore 
     *                                  the current {@code Thread}) have been interrupted.
     * @see #isInterruptRequested()
     */
    public void checkInterrupted() throws IllegalStateException, InterruptedException {
        log.traceEntry();
        if (this.executor != Thread.currentThread()) {
            throw log.throwing(new IllegalStateException("This method is a convenience method "
                    + "only meant to be called from the thread running the job."));
        }
        //Also check 'isInterruptRequested' because some libraries improperly reinit 
        //the Thread interruption status
        if (this.executor.isInterrupted() || this.isInterruptRequested()) {
            //Do not release the Job from the JobService even if interrupted, 
            //so that another thread can check the status of this job. 
            //But do notify that the job is finished. 
            this.complete();
            //reset interrupt flag, just in case
            this.executor.interrupt();
            //throw. We don't log it as an error
            throw log.throwing(Level.DEBUG, new InterruptedException());
        }
        log.traceExit();
    }
    /**
     * Release this {@code Job} so that no reference to it or to the {@code Thread} it holds are kept 
     * in the {@code JobService} that instantiated this {@code Job}.
     * Following calls to {@link #isReleased()} and {@link #isTerminated()} will return {@code true}.
     */
    public void release() {
        log.traceEntry();
        //in case the user did not specify that the job was finished. 
        //important to call 'complete' *before* calling 'releaseJob'
        this.complete();
        if (!this.isReleased()) {
            //Actually release the job
            this.jobService.releaseJob(this);
            //important to set 'released' to true *after* calling 'releaseJob'
            this.released = true;
        }
        log.traceExit();
    }
    

    //***********************
    // GETTERS/SETTERS
    //***********************
    /**
     * @return  A {@code long} that is the ID of this {@code Job}. It is different 
     *          from the {@link #executor} {@code Thread} ID, and is used to retrieve this {@code Job} 
     *          from different {@code Thread}s. 
     */
    public long getId() {
        return id;
    }
    /**
     * @return  A {@code String} that is the ID of the user running this {@code Job}.
     *          It is important to restrict the number of concurrent jobs per user.
     */
    public String getUserId() {
        return userId;
    }
    /**
     * @return  The {@code JobService} that instantiated this {@code Job}.
     */
    public JobService getJobService() {
        return jobService;
    }
    /**
     * @return  The {@code Thread} executing this job.
     */
    public Thread getExecutor() {
        return executor;
    }
    
    /**
     * @return  A {@code boolean} indicating whether the job is terminated 
     *          (successfully or not). Whether it was successfully completed is 
     *          returned by {@link #isSuccessful()}.
     * @see #isSuccessful()
     */
    public boolean isTerminated() {
        return this.terminated;
    }
    /**
     * @return  A {@code boolean} indicating whether this job was requested to be interrupted 
     *          (independently from whether the job has already fulfilled this request).
     *          This boolean is needed because some libraries reinit the Thread interruption status...
     * @see #interrupt()
     * @see #checkInterrupted()
     */
    public boolean isInterruptRequested() {
        return this.interruptRequested;
    }
    /**
     * @return  A {@code boolean} to indicate, if the job is completed, 
     *          whether it was successfully completed. As long as the task is not 
     *          completed ({@link #isTerminated()} returns {@code false}), 
     *          this method returns {@code false} in any case.
     * @see #isTerminated()
     */
    public boolean isSuccessful() {
        return this.successful;
    }
    /**
     * @return  A {@code boolean} indicating whether this job has been started.
     */
    public boolean isStarted() {
        return this.started;
    }
    /**
     * Set the {@code Job} state to running. Following calls to {@link #isStarted()} will return {@code true}.
     * {@link #nextTask()} is automatically called to set the task index to 0.
     * @see #startJob(String)
     * @see #isStarted()
     */
    public void startJob() {
        this.nextTask();
        this.started = true;
    }
    /**
     * Set the {@code Job} state to running. Following calls to {@link #isStarted()} will return {@code true}.
     * {@link #nextTask(String)} is automatically called to set the task index to 0.
     * 
     * @param firstTaskName A {@code String} that is the name of the first task run by this {@code Job}.
     * @see #nextTask(String)
     * @see #startJob()
     * @see #isStarted()
     */
    public void startJob(String firstTaskName) {
        this.nextTask(firstTaskName);
        this.started = true;
    }
    
    /**
     * @return  A {@code boolean} to indicate whether this {@code Job} has been released 
     *          from the {@code JobService} maintaining it.
     */
    public boolean isReleased() {
        return released;
    }
    
    /**
     * @return  A {@code String} representing the name of the job.
     */
    public String getName() {
        return name;
    }
    /**
     * @return  An {@code int} representing the total number of sub-tasks (meaning, 
     *          "big steps" of the job) that the job will involve. 
     * @see #getCurrentTaskIndex()
     */
    public int getTaskCount() {
        return taskCount;
    }

    /**
     * @param taskName The {@code String} representing the name of the current sub-task of the job.
     * @see #getCurrentTaskName()
     */
    public void setCurrentTaskName(String taskName) {
        this.currentTaskName = taskName;
    }
    /**
     * @return  A {@code String} representing the name of the current sub-task of the job.
     */
    public String getCurrentTaskName() {
        return currentTaskName;
    }
    /**
     * @param taskIndex     An {@code int} that is the index of the current sub-task 
     *                      (see {@link #getTaskCount()}). First sub-task has an index of 0.
     * @throws IllegalArgumentException If {@code currentTaskIndex} is equal to 
     *                                  or greater than the value returned by 
     *                                  {@link #getTaskCount()}, or less than 0.
     * @see #getCurrentTaskIndex()
     */
    public void setCurrentTaskIndex(int taskIndex) {
        if (taskIndex < 0 || this.getTaskCount() > 0 && taskIndex >= this.getTaskCount()) {
            throw log.throwing(new IllegalArgumentException(
                    "Incorrect value for currentTaskIndex: " + taskIndex + 
                    ". Should not be negative, nor above or equal to totalTaskCount (" + 
                    this.getTaskCount() + ")"));
        }
        this.currentTaskIndex = taskIndex;
    }
    /**
     * @return  An {@code int} that is the index of the current task 
     *          (see {@link #getTaskCount()}). First sub-task has an index of 0.
     *          Equals to -1 if this {@code Job} hasn't started.
     * @see #getTaskCount()
     * @see #setCurrentTaskIndex(int)
     */
    public int getCurrentTaskIndex() {
        return currentTaskIndex;
    }
    
    //***********************
    // HELPER METHODS
    //***********************
    /**
     * Increment by 1 the index of the current task (see {@link #getCurrentTaskIndex()}), 
     * only if it will not be equal to or greater than the total number of sub-tasks 
     * returned by {@link #getTaskCount()}.
     */
    public void incrementCurrentTaskIndex() {
        log.traceEntry();
        try {
            this.setCurrentTaskIndex(this.getCurrentTaskIndex() + 1);
        } catch (IllegalArgumentException e) {
            //nothing here, this method only increments the index if it is in the range 
            //of the total number of sub-tasks, it does not throw any exception
        }
        log.traceExit();
    }
    /**
     * Inform the {@code Job} that a new sub-task has been started.
     */
    public void nextTask() {
        log.traceEntry();
        this.nextTask("");
        log.traceExit();
    }
    /**
     * Inform the {@code Job} that a new sub-task has been started.
     * 
     * @param subTaskName   A {@code String} that is the name of the next sub-task starting.
     */
    public void nextTask(String subTaskName) {
        log.entry(subTaskName);
        log.debug("Starting new sub-task {}", subTaskName);
        this.incrementCurrentTaskIndex();
        this.setCurrentTaskName(subTaskName);
        log.traceExit();
    }


    //***********************
    // EQUALS/HASHCODE/TOSTRING
    //***********************
    //Actually, we don't want to override hashCode/equals for this class
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Job [id=").append(id)
               .append(", name=").append(name)
               .append(", userId=").append(userId)
               .append(", started=").append(started)
               .append(", terminated=").append(terminated)
               .append(", successful=").append(successful)
               .append(", released=").append(released)
               .append(", taskCount=").append(taskCount)
               .append(", currentTaskIndex=").append(currentTaskIndex)
               .append(", currentTaskName=").append(currentTaskName).append("]");
        return builder.toString();
    }
}
