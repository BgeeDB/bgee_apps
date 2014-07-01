package org.bgee.controller.servletutils;

import javax.servlet.ServletContextEvent;

/**
 * A {@code ServletContextListener} that properly shutdown the Bgee webapp. 
 * It removes local Thread for instance, to avoid memory leacks, or shutdown the cache, etc.
 * <p>
 * To receive notification events, this class must be configured 
 * in the deployment descriptor for the web application. 
 * To do so, add the following to web.xml in your web application: <br />
 * &lt;listener&gt; <br />
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;listener-class&gt;org.bgee.controller.servletutils.ServletContextListener&lt;/listener-class&gt; <br />
 * &lt;/listener&gt;
 * 
 * @author Frederic Bastian
 * @version Bgee 11 July 2012
 * @since Bgee 11
 *
 */
public class ServletContextListener implements javax.servlet.ServletContextListener
{

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		EntityFactoryProvider.loadStaticFactories();
	}

}
