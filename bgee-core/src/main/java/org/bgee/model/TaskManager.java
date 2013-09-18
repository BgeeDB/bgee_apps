package org.bgee.model;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class allows to keep track of the advancement of log-running tasks, 
 * and provides means to interrupt this task. Instances of this class notably 
 * hold a reference to the <code>Thread</code> running the task, to be able 
 * to call <code>Interrupt</code> on it from another <code>Thread</code> 
 * (see {@link #interrupt()}). 
 * <p>
 * A <code>TaskManager</code> can be obtained from another <code>Thread</code> by using 
 * {@link #getTaskManager(long)} with the ID it was associated to. It is very important 
 * to call {@link #release()} when a <code>Task</code> is completed, otherwise a reference 
 * to the <code>TaskManager</code> will be statically kept (and the <code>TaskManager</code> 
 * would keep a reference to the executor <code>Thread</code>).
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
	 * A <code>ConcurrentMap</code> storing the living <code>TaskManager</code>s 
	 * associated with the value returned {@link #getId()}. This <code>Map</code> 
	 * is used to retrieve a <code>TaskManager</code> from different <code>Thread</code>s 
	 * (see {@link #getTaskManager(long)}).
	 * <p>
	 * We can not simply use the ID of the <code>Thread</code> running the task, 
	 * as it is the case in {@link #threadIdsToManagers}, because we need to be able 
	 * to assign an ID before actually launching the task, or the <code>Thread</code> 
	 * running the task.
	 * 
	 * @see #getTaskManager(long)
	 * @see #threadIdsToManagers
	 */
	private static final ConcurrentMap<Long, TaskManager> managers = 
			new ConcurrentHashMap<Long, TaskManager>(); 
	/**
	 * A <code>ConcurrentMap</code> storing the living <code>TaskManager</code>s 
	 * associated with the ID of the <code>Thread</code> running the task. 
	 * This is to make sure that a given <code>Thread</code> will be associated 
	 * with only one <code>TaskManager</code> at most. I am reluctant in using a
	 * <code>TreadLocal</code>, because... because.
	 */
	private static final ConcurrentMap<Long, TaskManager> threadIdsToManagers = 
			new ConcurrentHashMap<Long, TaskManager>(); 
	
	/**
	 * Instantiate a new <code>TaskManager</code> that will hold the caller <code>Thread</code> 
	 * (so that it can be interrupted from another <code>Thread</code>), 
	 * and map it to <code>id</code>, so that the <code>Task</code> can then 
	 * be retrieved by a call to {@link #getTaskManager(long)}, from any <code>Thread</code>. 
	 * <p>
	 * Any following call from the caller <code>Thread</code> to {@link #getTaskManager()} 
	 * will return the same <code>TaskManager</code> instance, unless {@link #release()} 
	 * was called on it. 
	 * <p>
	 * If the caller <code>Thread</code> was already holding when calling this method, 
	 * an <code>IllegalStateException</code> is thrown.If there is already a 
	 * <code>TaskManager</code> associated with <code>id</code>, 
	 * an <code>IllegalArgumentException</code> is thrown. 
	 *  
	 * @param id	an <code>long</code> that will be the ID of the 
	 * 				<code>TaskManager</code> associated with the caller <code>Thread</code>.
	 * @throws IllegalStateException	If the caller <code>Thread</code> is already 
	 * 									holding a <code>TaskManager</code>.
	 * @throws IllegalArgumentException	If there is already a <code>TaskManager</code> 
	 * 									registered with <code>id</code>.
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
		
		log.info("New TaskManager with ID {} registered", id);
		log.exit();
	}
	
	/**
	 * @return	the <code>TaskManager</code> currently associated with the caller 
	 * 			<code>Thread</code>. <code>null</code> if no <code>TaskManager</code> 
	 * 			was associated to the caller <code>Thread</code> (by previously calling 
	 * 			{@link #registerTaskManager(long)}).
	 */
	public static TaskManager getTaskManager() {
		return threadIdsToManagers.get(Thread.currentThread().getId());
	}
	/**
	 * Get the <code>TaskManager</code> associated to <code>id</code>. 
	 * 
	 * @param id	a <code>long</code> that is the ID of the desired 
	 * 				<code>TaskManager</code>. 
	 * @return		The <code>TaskManager</code> mapped to <code>id</code>, 
	 * 				<code>null</code> if no <code>TaskManager</code> was associated
	 * 				to <code>id</code> (by previously calling {@link 
	 * 				#registerTaskManager(long)}).
	 */
	public static TaskManager getTaskManager(long id) {
		return managers.get(id);
	}
	
	//************************************
	// INSTANCE METHODS AND VARIABLES
	//************************************
	/**
	 * A <code>long</code> that is the ID of this <code>TaskManager</code>. 
	 * It is different from the {@link #executor} <code>Thread</code> ID, 
	 * and is used to retrieve this <code>TaskManager</code> from different 
	 * <code>Thread</code>s. 
	 */
	private final long id;
	/**
	 * The <code>Thread</code> that is performing the task. It will be used 
	 * to call {@link org.bgee.model.dao.api.DAOManager#kill(Thread)}, and to call
	 * <code>interrupt</code> on it, so that execution of the long-running task 
	 * is killed. 
	 */
	private final Thread executor;
	
	/**
	 * Constructor private, instances should be obtained using 
	 * {@link #getTaskManager()} or {@link #getTaskManager(long)}, after having called 
	 * {@link #registerTaskManager(long)}.
	 *
	 * @param id				A <code>long</code> representing the ID this 
	 * 							<code>TaskManager</code> will be associated with.
	 */
	private TaskManager(long id) {
		this.id = id;
		this.executor = Thread.currentThread();
	}
	
	/**
	 * Interrupt the current task. This method calls <code>interrupt</code> 
	 * on the <code>Thread</code> running the task. It is then the responsibility 
	 * of the applicative code running the task to deal with the 
	 * <code>InterruptedException</code>.
	 */
	public void interrupt() {
		this.executor.interrupt();
	}
	/**
	 * Release this <code>TaskManager</code> so that no reference to it 
	 * or to the <code>Thread</code> it holds are kept.
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
