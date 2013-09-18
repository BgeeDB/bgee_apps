package org.bgee.model.expressiondata.querytools;

import org.apache.logging.log4j.Logger;
import org.bgee.model.TaskManager;

/**
 * Parent class of all query tools, notably performing task relative 
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
     * Call at the start of a query, by providing the name of the task, 
     * and the total number of sub-tasks (meaning, "big steps") that it will involve. 
     * If a <code>TaskManager</code> is used, it needs to be registered prior to calling 
     * this method (see {@link org.bgee.model.TaskManager#registerTaskManager(long)}). 
     * 
     * @param taskName				a <code>String</code> representing the name of the task.
     * @param totalSubTaskCount		an <code>int</code> defining the total number of sub-tasks 
     * 								(meaning, "big steps") that this task will involve.
     */
    protected void startQuery(String taskName, int totalSubTaskCount) {
    	//acquire the task manager. It will be tried only once.
    	this.manager = TaskManager.getTaskManager();
    	if (this.getTaskManager() != null) {
    		this.getTaskManager().setTaskName(taskName);
    		this.getTaskManager().setTotalSubTaskCount(totalSubTaskCount);
    	}
    	this.getLogger().info("Start of the query: {}", taskName);
    }
    
    
}
