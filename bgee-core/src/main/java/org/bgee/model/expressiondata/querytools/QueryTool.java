package org.bgee.model.expressiondata.querytools;

import org.apache.logging.log4j.Logger;
import org.bgee.model.TaskManager;

/**
 * Parent class of all query tools, notably performing tasks relative 
 * to the {@link org.bgee.model.TaskManager TaskManager}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public abstract class QueryTool {
	
	/**
	 * A <code>TaskManager</code> to be notified of the advancement of the query. 
	 * It must have been registered prior to calling {@link #startQuery(String, int)} 
	 * (see {@link org.bgee.model.TaskManager#registerTaskManager(long)}).
	 */
	private TaskManager manager;
	
	/**
	 * A <code>String</code> representing the name of the main task of this query 
	 * (same concept than {@link org.bgee.model.TaskManager#getTaskName()}).
	 */
	private String taskName;
	/**
	 * A <code>String</code> representing the name of the sub-task currently  
	 * being performed (same concept than 
	 * {@link org.bgee.model.TaskManager#getCurrentSubTaskName()}).
	 */
	private String currentSubTaskName;
	
	/**
	 * Default constructor.
	 */
	protected QueryTool() {
		//will be set by the method startQuery
		this.manager = null;
	}
	/**
	 * Return the <code>Logger</code> of the class. This allows parent class 
	 * to perform logging by using the <code>Logger</code> of its sub-class, 
	 * with correct class name. 
	 * 
	 * @return	the <code>Logger</code> of the class
	 */
    protected abstract Logger getLogger();
    
    /**
     * @return	the <code>TaskManager</code> to be notified of the advancement of the query.
     * 			<code>null</code> if no <code>TaskManager</code> was registered before 
     * 			calling {@link startQuery(String, int)} (see {@link 
     * 			org.bgee.model.TaskManager#registerTaskManager(long)}).
     */
    protected TaskManager getTaskManager() {
    	return this.manager;
    }
    
    /**
     * Called at the start of a query, by providing the name of the task, 
     * and the total number of sub-tasks (meaning, "big steps") that it will involve, 
     * as well as the name of the first sub-task (not mandatory). See {@link 
     * org.bgee.model.TaskManager to get a definition of tasks and sub-tasks}.
     * <p>
     * If a <code>TaskManager</code> is used, it needs to be registered prior to calling 
     * this method (see {@link org.bgee.model.TaskManager#registerTaskManager(long)}). 
     * 
     * @param taskName				a <code>String</code> representing the name of the task.
     * @param totalSubTaskCount		an <code>int</code> defining the total number of sub-tasks 
     * 								(meaning, "big steps") that this task will involve.
     * @param firstSubTaskName		a <code>String</code> that is the name of the first 
     * 								sub-task. Can be <code>null</code> or empty.
     */
    protected void startQuery(String taskName, int totalSubTaskCount, 
    		String firstSubTaskName) {
    	//acquire the task manager. If not registered at this point, we will not try 
    	//to acquire it afterwards.
    	this.manager = TaskManager.getTaskManager();
    	this.taskName = taskName;
    	this.currentSubTaskName = firstSubTaskName;
    	
    	if (this.getTaskManager() != null) {
    		this.getTaskManager().setTaskName(taskName);
    		this.getTaskManager().setTotalSubTaskCount(totalSubTaskCount);
    		this.getTaskManager().setCurrentSubTaskIndex(0);
    		this.getTaskManager().setCurrentSubTaskName(firstSubTaskName);
    	}
    	this.getLogger().info("Start of the query: {} - first sub-task: {}", 
    			taskName, firstSubTaskName);
    }
    
    /**
     * Called when a <code>QueryTool</code> ends its current sub-task
     * (same concept than {@link org.bgee.model.TaskManager#getCurrentSubTaskName()}), 
     * to notify a <code>TaskManager</code>, and for logging purpose.
     */
    protected void endSubTask() {
    	//TODO: use a Log4j2 Message. I'm just lazy here
    	if (this.getLogger().isDebugEnabled()) {
    		//the memory usage is useful in unit testing using a single Thread
    		Runtime runTime = Runtime.getRuntime();
    		this.getLogger().debug(
    				"Ending sub-task {} - Total memory: {} kb - Free memory: {} kb", 
    				this.currentSubTaskName, runTime.totalMemory()/1000, 
    				runTime.freeMemory()/1000);
    	}
    }
    /**
     * Called when the <code>QueryTool</code> starts its next sub-task
     * (same concept than {@link org.bgee.model.TaskManager#getCurrentSubTaskName()}), 
     * to notify a <code>TaskManager</code>, and for logging purpose.
     * 
     * @param subTaskName	A <code>String</code> that is the name of the next 
     * 						sub-task starting.
     */
    protected void nextSubTask(String subTaskName) {
    	if (this.getTaskManager() != null) {
    		this.getTaskManager().incrementCurrentSubTaskIndex();
    		this.getTaskManager().setCurrentSubTaskName(subTaskName);
    	}
    	this.currentSubTaskName = subTaskName;
    	
    	this.getLogger().debug("Starting new sub-task {}", subTaskName);
    }
    
    /**
     * Called when a <code>QueryTool</code> ends its overall task, to notify 
     * a <code>TaskManager</code>, and for logging purpose. This method should
     * be called by the <code>QueryTool</code> within a <code>finally</code> block.
     * 
     * @param success	<code>true</code> if the task was completed with success, 
     * 					<code>false</code> if an error occurred or the task was 
     * 					interrupted. 
     */
    protected void endQuery(boolean success) {
    	if (success) {
    		this.getLogger().info("Query {} completed with success", this.taskName);
    		if (this.getTaskManager() != null) {
    			this.getTaskManager().taskCompletedWithSuccess();
    		}
    	} else {
    		this.getLogger().info("Query {} interrupted or termintated with errors", 
    				this.taskName);
    		if (this.getTaskManager() != null) {
    			this.getTaskManager().taskNotCompleted();
    		}
    	}
    }
}
