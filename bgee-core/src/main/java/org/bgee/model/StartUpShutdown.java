package org.bgee.model;

import org.bgee.model.dao.api.DAOManager;

/**
 * This class provides methods to load the resources needed at application start-up, 
 * or to release resources hold by a {@code Thread} when it is terminated, 
 * or to release all resources at application shutdown.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class StartUpShutdown {
	/**
	 * Start up the resources used by the Application. For instance, start up a cache 
	 * if one is configured. Note that this method will not start up resources 
	 * living outside of the JVM, such as for instance, a MySQL database. 
	 */
    public static void startUpApplication() {
    	
    }
    /**
     * Shutdown all resources used by the application. For instance, if a cache was used, 
     * this cache is shutdown. Note that this method will not shutdown resources 
	 * living outside of the JVM, such as for instance, a MySQL database. 
     */
    public static void shutdownApplication() {
    	
    }
    
    /**
     * Release all resources hold by the current {@code Thread}. For instance, 
     * if the current {@code Thread} was holding a connection to a database, 
     * this connection is closed. 
     * <p>
     * This method should always be called just before the end of the execution 
     * of a {@code Thread}, for instance by calling it in a {@code finally} 
     * block. 
     */
    public static void threadTerminated() {
    	try {
    		//release DAO
    		if (DAOManager.hasDAOManager()) {
    			DAOManager.getDAOManager().close();
    		}
    	} finally {
    		//release TaskManager
    		TaskManager manager = TaskManager.getTaskManager();
    		if (manager != null) {
    			manager.release();
    		}
    	}
    }
}
