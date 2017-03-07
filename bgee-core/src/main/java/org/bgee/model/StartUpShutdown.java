package org.bgee.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private final static Logger log = LogManager.getLogger(StartUpShutdown.class.getName());
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
        log.entry();
    	DAOManager.closeAll();
    	//Should we have something like 'JobService.releaseAll()'?
    	//Would mean to store all JobService instances, not sure how we can make sure they would be deallocated.
    	log.exit();
    }
    
    /**
     * Kill and release all resources hold by the current {@code Thread}. For instance, 
     * if the current {@code Thread} was running a query to a database, 
     * this query is killed and the connection to the database is closed.
     * <p>
     * This method will usually be called from a different {@code Thread} than {@code t}.
     * 
     * @param t     The {@code Thread} that was interrupted.
     */
    //XXX:Maybe this method should accept the DAOManager as argument, when we'll remove 
    //these DAOManager static methods?
    public static void interruptThread(Thread t) {
        log.entry(t);
        //Kill any running DAO queries
        DAOManager.kill(t);
        log.exit();
    }
}
