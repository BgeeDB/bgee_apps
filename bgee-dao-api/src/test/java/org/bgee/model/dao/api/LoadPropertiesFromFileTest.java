package org.bgee.model.dao.api;

import static org.mockito.Mockito.*;

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

/**
 * Class testing the loading of the properties of the {@code DAOManager} 
 * from a property file. This has to be done in a separate class, 
 * as the properties are loaded only at class loading, so only once for a given 
 * <code>ClassLoader</code>.
 * <p>
 * See {@link LoadPropertiesFromSysPropsTest} for a class testing the loading of the properties 
 * from the System properties.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @see LoadPropertiesFromSysPropsTest
 * @since Bgee 13
 */
public class LoadPropertiesFromFileTest extends TestAncestor
{
	private final static Logger log = 
	        LogManager.getLogger(LoadPropertiesFromFileTest.class.getName());
	/**
	 * Default constructor.
	 */
	public LoadPropertiesFromFileTest() {
		super();
	}
	@Override
	protected Logger getLogger() {
		return log;
	}

	/**
	 * Try to load the properties from a properties file. 
	 */
	@Test
	public void shouldLoadPropertiesFromFile() {
		//set a system properties to provide the name of the properties file 
		//(default is bgee.dao.properties, but we want to use a test file)
		System.setProperty(DAOManager.CONFIG_FILE_KEY, "/test.properties");
		
		//we use the mock DAOManager to check what properties it received
		Properties parameters = new Properties();
        parameters.put("bgee.dao.fake.parameter", "fake.value");
        DAOManager.getDAOManager();
        verify(MockDAOManager.mockManager).setParameters(eq(parameters));
		
		//clear the System properties
		System.clearProperty(DAOManager.CONFIG_FILE_KEY);
	}
}
