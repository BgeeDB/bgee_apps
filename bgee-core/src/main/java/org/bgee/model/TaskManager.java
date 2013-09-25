package org.bgee.model;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAOManager;

/**
 * This class allows to keep track of the advancement of log-running tasks, 
 * and provides means to interrupt this task. Instances of this class notably 
 * hold a reference to the {@code Thread} running the task, to be able 
 * to call {@code Interrupt} on it from another {@code Thread} 
 * (see {@link #interrupt()}). 
 * <p>
 * A {@code TaskManager} can be obtained from another {@code Thread} by using 
 * {@link #getTaskManager(long)} with the ID it was associated to. It is very important 
 * to call {@link #release()} when a {@code Task} is completed, otherwise a reference 
 * to the {@code TaskManager} will be statically kept (and the {@code TaskManager} 
 * would keep a reference to the executor {@code Thread}).
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class TaskManager {
	//************************************
	// STATIC METHODS AND VARIABLES
	//************************************
	/**
    * <code>Logger/code> of this class.
    */
   private final static Logger log = LogManager.getLogger(TaskManager.class.getName());
	/**
	 * A {@code ConcurrentMap} storing the living {@code TaskManager}s 
	 * associated with the value returned by {@link #getId()}. This {@code Map} 
	 * is used to retrieve a {@code TaskManager} from different {@code Thread}s 
	 * (see {@link #getTaskManager(long)}).
	 * <p>
	 * We can not simply use the ID of the {@code Thread} running the task, 
	 * as it is the case in {@link #threadIdsToManagers}, because we need to be able 
	 * to assign an ID before actually launching the task, or the {@code Thread} 
	 * running the task.
	 * 
	 * @see #getTaskManager(long)
	 * @see #threadIdsToManagers
	 */
	private static final ConcurrentMap<Long, TaskManager> managers = 
			new ConcurrentHashMap<Long, TaskManager>(); 
	/**
	 * A {@code ConcurrentMap} storing the living {@code TaskManager}s 
	 * associated with the ID of the {@code Thread} running the task. 
	 * This is to make sure that a given {@code Thread} will be associated 
	 * with only one {@code TaskManager} at most. I am reluctant in using a
	 * {@code TreadLocal}, because... because.
	 */
	private static final ConcurrentMap<Long, TaskManager> threadIdsToManagers = 
			new ConcurrentHashMap<Long, TaskManager>(); 
	
	/**
	 * Instantiate a new {@code TaskManager} that will hold the caller {@code Thread} 
	 * (so that it can be interrupted from another {@code Thread}), 
	 * and map it to {@code id}, so that the {@code TaskManager} can then 
	 * be retrieved by a call to {@link #getTaskManager(long)}, from any {@code Thread}. 
	 * <p>
	 * Any following call from the caller {@code Thread} to {@link #getTaskManager()} 
	 * will return the same {@code TaskManager} instance, unless {@link #release()} 
	 * was called on it. 
	 * <p>
	 * If the caller {@code Thread} was already held when calling this method, 
	 * an {@code IllegalStateException} is thrown. If there is already a 
	 * {@code TaskManager} associated with {@code id}, 
	 * an {@code IllegalArgumentException} is thrown. 
	 *  
	 * @param id	an {@code long} that will be the ID of the 
	 * 				{@code TaskManager} associated with the caller {@code Thread}.
	 * @throws IllegalStateException	If the caller {@code Thread} is already 
	 * 									held by a {@code TaskManager}.
	 * @throws IllegalArgumentException	If there is already a {@code TaskManager} 
	 * 									registered with {@code id}.
	 */
	public static void registerTaskManager(long id) throws IllegalArgumentException, 
	    IllegalStateException {
		log.entry(id);

		TaskManager manager = new TaskManager(id);
		//First, check that the ID is not already used
		if (managers.putIfAbsent(id, manager) != null) {
			throw log.throwing(new IllegalArgumentException("Trying to register " +
					"a new TaskManager with an already used ID"));
		}
		//then, check that there is no TaskManager already associated to the current Thread
		if (threadIdsToManagers.putIfAbsent(Thread.currentThread().getId(), manager) != null) {
			managers.remove(id);
			throw log.throwing(new IllegalStateException("Trying to register " +
					"a new TaskManager with a Thread already holding a TaskManager"));
		}
		
		log.info("New TaskManager with ID {} for Thread {} registered", id, 
				Thread.currentThread().getId());
		log.exit();
	}
	
	/**
	 * @return	the {@code TaskManager} currently associated with the caller 
	 * 			{@code Thread}. {@code null} if no {@code TaskManager} 
	 * 			was associated to the caller {@code Thread} (by previously calling 
	 * 			{@link #registerTaskManager(long)}).
	 */
	public static TaskManager getTaskManager() {
		return threadIdsToManagers.get(Thread.currentThread().getId());
	}
	/**
	 * Get the {@code TaskManager} associated to {@code id}. 
	 * 
	 * @param id	a {@code long} that is the ID of the desired 
	 * 				{@code TaskManager}. 
	 * @return		The {@code TaskManager} mapped to {@code id}, 
	 * 				{@code null} if no {@code TaskManager} was associated
	 * 				to {@code id} (by previously calling {@link 
	 * 				#registerTaskManager(long)}).
	 */
	public static TaskManager getTaskManager(long id) {
		return managers.get(id);
	}
	
	//**********************************************************************
	// INSTANCE METHODS AND VARIABLES
	// ALL ATTRIBUTES ARE EITHER FINAL OR VOLATILE, BECAUSE THEY ARE MEANT 
	// TO BE READ AND WRITTEN BY DIFFERENT THREADS
	//**********************************************************************
	/**
	 * A {@code long} that is the ID of this {@code TaskManager}. 
	 * It is different from the {@link #executor} {@code Thread} ID, 
	 * and is used to retrieve this {@code TaskManager} from different 
	 * {@code Thread}s. 
	 */
	private final long id;
	/**
	 * The {@code Thread} that is performing the task. It will be used 
	 * to call {@link org.bgee.model.dao.api.DAOManager#kill(Thread)}, and to call
	 * {@code interrupt} on it, so that execution of the long-running task 
	 * is killed. 
	 */
	private final Thread executor;
	
	/**
	 * A {@code boolean} indicating whether the task is terminated (successfully 
	 * or not). Whether it was successfully completed is stored in the attribute 
	 * {@link #successful}.
	 * @see #successful
	 */
	private volatile boolean terminated;

	/**
	 * A {@code boolean} to indicate, if the task is completed, whether it was 
	 * successfully completed. As long as the task is not completed ({@link 
	 * #terminated} is {@code false}), this attribute is {@code false} 
	 * in any case. 
	 * @see #taskTerminated
	 */
	private volatile boolean successful;
	
	/**
	 * A {@code String} representing the name of the task.
	 */
	private volatile String taskName;
	/**
	 * An {@code int} representing the total number of sub-tasks (meaning, 
	 * "big steps" of the task) that the managed task will involve. 
	 * @see #currentSubTaskIndex
	 */
	private volatile int totalSubTaskCount;
	/**
	 * An {@code int} that is the index of the current sub-task 
	 * (see {@link #totalSubTaskCount}). First sub-task has an index of 0.
	 * @see #totalSubTaskCount
	 */
	private volatile int currentSubTaskIndex;
	/**
	 * A {@code String} representing the title of the current sub-task 
	 * (meaning, of the current "big step" in the task process)
	 */
	private volatile String currentSubTaskName;
	
	/**
	 * Constructor private, instances should be obtained using 
	 * {@link #getTaskManager()} or {@link #getTaskManager(long)}, after having called 
	 * {@link #registerTaskManager(long)}.
	 *
	 * @param id				A {@code long} representing the ID this 
	 * 							{@code TaskManager} will be associated with.
	 */
	private TaskManager(long id) {
		this.id       = id;
		this.executor = Thread.currentThread();
		
		this.terminated = false;
		this.successful = false;
		
		this.setTaskName("");
		this.setCurrentSubTaskName("");
		//We do not use the setters here, they would throw an IllegalArgumentException
		this.totalSubTaskCount   = 0;
		this.currentSubTaskIndex = 0;
	}
	

	/**
	 * Method called to indicate that the task was completed with success. 
	 * Following calls to {@link #isTerminated()} and {@link #isSuccessful()} 
	 * will return {@code true}. 
	 * @see #isTerminated()
	 * @see #isSuccessful()
	 */
	public void taskCompletedWithSuccess() {
		this.terminated = true;
		this.successful = true;
	}
	/**
	 * Method called to indicate that the task was terminated, either because of 
	 * an error, or because it was interrupted. Following calls to 
	 * {@link #isTerminated()} will return {@code true}, and calls to 
	 * {@link #isSuccessful()} will return {@code false}. 
	 * @see #isTerminated()
	 * @see #isSuccessful()
	 */
	public void taskNotCompleted() {
		this.terminated = true;
		this.successful = false;
	}
	/**
	 * @return 	A {@code boolean} indicating whether the task is terminated 
	 * 			(successfully or not). Whether it was successfully completed is 
	 * 			returned by {@link #isSuccessful()}.
	 * @see #isSuccessful()
	 */
	public boolean isTerminated() {
		return this.terminated;
	}
	/**
	 * @return 	A {@code boolean} to indicate, if the task is completed, 
	 * 			whether it was successfully completed. As long as the task is not 
	 * 			completed ({@link #isTerminated()} returns {@code false}), 
	 * 			this method returns {@code false} in any case.
	 * @see #isTerminated()
	 */
	public boolean isSuccessful() {
		return this.successful;
	}
	
	/**
	 * @return A {@code String} representing the name of the task.
	 * @see #setTaskName(String)
	 */
	public String getTaskName() {
		return taskName;
	}
	/**
	 * @param taskName The {@code String} representing the name of the task.
	 * @see #getTaskName()
	 */
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	/**
	 * @return 	An {@code int} representing the total number of sub-tasks (meaning, 
	 * 			"big steps" of the task) that the managed task will involve. 
	 * @see #getCurrentSubTaskIndex()
	 * @see #setTotalSubTaskCount(int)
	 */
	public int getTotalSubTaskCount() {
		return totalSubTaskCount;
	}
	/**
	 * @param totalSubTaskCount An {@code int} representing the total number 
	 * 							of sub-tasks (meaning, "big steps" of the task) 
	 * 							that the managed task will involve. 
	 * @throws IllegalArgumentException	If {@code totalSubTaskCount} is not greater 
	 * 									than 0.
	 * @see #setCurrentSubTaskIndex(int)
	 * @see #getTotalSubTaskCount()
	 */
	public void setTotalSubTaskCount(int totalSubTaskCount) {
		if (totalSubTaskCount <= 0) {
			throw log.throwing(new IllegalArgumentException(
					"Incorrect value for totalSubTaskCount: " + totalSubTaskCount + 
					". Should be greater than 0."));
		}
		this.totalSubTaskCount = totalSubTaskCount;
	}

	/**
	 * @return 	An {@code int} that is the index of the current sub-task 
	 * 			(see {@link #getTotalSubTaskCount()}). First sub-task has an index of 0.
	 * @see #getTotalSubTaskCount()
	 * @see #setCurrentSubTaskIndex(int)
	 */
	public int getCurrentSubTaskIndex() {
		return currentSubTaskIndex;
	}
	/**
	 * @param currentSubTaskIndex 	An {@code int} that is the index 
	 * 								of the current sub-task (see {@link 
	 * 								#getTotalSubTaskCount()}). First sub-task 
	 * 								has an index of 0.
	 * @throws IllegalArgumentException	If {@code currentSubTaskIndex} is equal to 
	 * 									or greater than the value returned by 
	 * 									{@link #getTotalSubTaskCount()}, or less than 0.
	 * @see #setTotalSubTaskCount(int)
	 * @see #getCurrentSubTaskIndex()
	 */
	public void setCurrentSubTaskIndex(int currentSubTaskIndex) {
		if (currentSubTaskIndex < 0 || currentSubTaskIndex >= this.getTotalSubTaskCount()) {
			throw log.throwing(new IllegalArgumentException(
					"Incorrect value for currentSubTaskIndex: " + currentSubTaskIndex + 
					". Should not be negative, nor above or equal to totalSubTaskCount (" + 
					this.getTotalSubTaskCount() + ")"));
		}
		this.currentSubTaskIndex = currentSubTaskIndex;
	}
	/**
	 * Increment by 1 the index of the current sub-task (see {@link 
	 * #getCurrentSubTaskIndex()}), only if it will not be equal to or greater than 
	 * the total number of sub-tasks returned by {@link #getTotalSubTaskCount()}.
	 */
	public void incrementCurrentSubTaskIndex() {
		try {
			this.setCurrentSubTaskIndex(this.getCurrentSubTaskIndex() + 1);
		} catch (IllegalArgumentException e) {
			//nothing here, this method only increments the index if it is in the range 
			//of the total number of sub-tasks, it does not throw any exception
		}
	}

	/**
	 * @return 	A {@code String} representing the title of the current sub-task 
	 * 			(meaning, of the current "big step" in the task process)
	 * @see #setCurrentSubTaskName(String)
	 */
	public String getCurrentSubTaskName() {
		return currentSubTaskName;
	}

	/**
	 * @param currentSubTaskName 	A {@code String} representing the title 
	 * 								of the current sub-task (meaning, of the current 
	 * 								"big step" in the task process)
	 * @see #getCurrentSubTaskName()
	 */
	public void setCurrentSubTaskName(String currentSubTaskName) {
		this.currentSubTaskName = currentSubTaskName;
	}

	/**
	 * Interrupt the task associated with this {@code TaskManager}. 
	 * This method first interrupt any running DAO calls requested by 
	 * the {@code Thread} running the task, by calling {@link 
	 * org.bgee.model.dao.api.DAOManager.kill(Thread)}. This method then calls 
	 * {@code interrupt} on the {@code Thread} running the task. 
	 * It is then the responsibility of the applicative code running the task 
	 * to deal with the {@code InterruptedException}.
	 */
	public void interrupt() {
		try {
		    DAOManager.kill(this.executor);
		} finally {
		    this.executor.interrupt();
		}
	}
	/**
	 * Release this {@code TaskManager} so that no reference to it 
	 * or to the {@code Thread} it holds are kept.
	 */
    public void release() {
    	//here we do not provide atomicity, because in the worst case scenario, 
    	//exceptions will be thrown by registerTaskManager(long) because of 
    	//an illegal state.
    	managers.remove(this.id);
    	threadIdsToManagers.remove(this.executor.getId());
    	//this.executor = null;
    }
}
