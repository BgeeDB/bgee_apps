package org.bgee.controller.servletutils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.FrontController;
import org.bgee.controller.utils.BgeeCacheService;
import org.bgee.model.BgeeProperties;
import org.bgee.model.dao.api.DAOManager;

/**
 * A {@code ServletContextListener} allowing to properly start/shutdown the Bgee webapp. 
 * It notably removes local Thread to avoid memory leaks, or shutdown the cache, 
 * close connections to the database, etc.
 * <p>
 * To receive notification events, this class must be configured 
 * in the deployment descriptor for the web application. 
 * To do so, add the following to {@code web.xml} in your web application: 
 * <pre>{@code 
 * <listener> 
 *     <listener-class>org.bgee.controller.servletutils.BgeeServletContextListener</listener-class>
 * </listener>
 * }</pre>
 * 
 * @author Frederic Bastian
 * @version Bgee 15.0, Jan. 2023
 * @since Bgee 11
 *
 */
public class BgeeServletContextListener implements ServletContextListener {
    private final static Logger log = LogManager.getLogger(BgeeServletContextListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        log.traceEntry();

        FrontController frontController = new FrontController();

        Runnable commandDataCacheInitializer = () -> {
            try {
                frontController.initializeCaches(1000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
        Thread commandDataInitializerThread = new Thread(commandDataCacheInitializer);

        try {
            commandDataInitializerThread.start();
        } catch (Throwable e) {
            //Nothing much we can do here
            log.error("################ Initialization error ################");
            log.catching(e);
        }

        log.traceExit();
    }

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
	    log.traceEntry();
		DAOManager.closeAll();
		BgeeProperties.releaseAll();
		BgeeCacheService.releaseAll();
		log.traceExit();
	}
}
