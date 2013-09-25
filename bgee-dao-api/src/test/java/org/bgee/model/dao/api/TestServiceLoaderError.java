package org.bgee.model.dao.api;

import java.util.ServiceConfigurationError;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

/**
 * Test the behavior when a service provider throws an exception 
 * when the ServiceLoader tries to instantiate it. The {@code ServiceLoader} 
 * is used during static initialization of the class {@code DAOManager}.
 * <p>
 * This test will be valid only if a new {@code ClassLoader} is used for each 
 * unit test class. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class TestServiceLoaderError extends TestAncestor {
	/**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
    		LogManager.getLogger(TestServiceLoaderError.class.getName());

	@Override
	protected Logger getLogger() {
		return log;
	}
	
	/**
	 * Test behavior when an error occurs while using the {@code ServiceLoader}.
	 */
	@Test
	public void shouldFailInitialization() throws ClassNotFoundException {
		//to throw an exception when a instance of the service provider is requested
		TestAncestor.thrownInstantiationException = true;
		
		//The ServiceLoader is used during static initialization of the class DAOManager
		try {
		    Class.forName("org.bgee.model.dao.api.DAOManager");
			//if we reach that point, no ServiceConfigurationError was thrown, 
			//test failed
		    throw new AssertionError("A ServiceConfigurationError should have been thrown");
		} catch (ServiceConfigurationError e) {
			//test passed
			log.catching(Level.DEBUG, e);
		}		
		
		//restore setting for other tests
		TestAncestor.thrownInstantiationException = false;
	}
}
