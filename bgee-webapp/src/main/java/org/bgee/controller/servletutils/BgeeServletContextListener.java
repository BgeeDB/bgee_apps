package org.bgee.controller.servletutils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
 * @version Bgee 13 Oct. 2015
 * @since Bgee 11
 *
 */
public class BgeeServletContextListener implements ServletContextListener {
    
    private final static Logger log = LogManager.getLogger(BgeeServletContextListener.class.getName());

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
	    log.entry();
		DAOManager.closeAll();
		BgeeProperties.releaseAll();
		log.exit();
	}

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		log.entry();
		//nothing for now. Should launch the cache and the data source in the future. 
		log.exit();
	}

}
