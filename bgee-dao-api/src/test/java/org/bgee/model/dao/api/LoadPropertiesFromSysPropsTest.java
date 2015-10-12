package org.bgee.model.dao.api;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

/**
 * Class testing the loading of the properties of the {@code DAOManager}  
 * from the System Properties. This has to be done in a separate class, 
 * as the properties are loaded only at class loading, so only once for a given 
 * <code>ClassLoader</code>.
 * <p>
 * See {@link LoadPropertiesFromFileTest} for a class testing the loading of the properties 
 * from a property file.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @see LoadPropertiesFromFileTest
 * @since Bgee 13
 */
public class LoadPropertiesFromSysPropsTest extends TestAncestor
{
	private final static Logger log = 
	        LogManager.getLogger(LoadPropertiesFromSysPropsTest.class.getName());
	/**
	 * Default constructor.
	 */
	public LoadPropertiesFromSysPropsTest() {
		super();
	}
	@Override
	protected Logger getLogger() {
		return log;
	}

	/**
	 * Try to load the properties from the System properties. 
	 */
	@Test
	public void shouldLoadPropertiesFromSysProps() {
		//set the property to test
		//(this is not the default value)
		System.setProperty("key.from.sys.props", "value.from.sys.props");
		//set the properties file to an non-existing file, 
		//so that System properties are used 
		System.setProperty(DAOManager.CONFIG_FILE_KEY, "/none");
		
		//we use the mock DAOManager to check what properties it received
        Properties props = new Properties(System.getProperties());
        DAOManager.getDAOManager();
        verify(MockDAOManager.mockManager).setParameters(eq(props));
		
		//clear the System properties
		System.clearProperty(DAOManager.CONFIG_FILE_KEY);
		System.clearProperty("key.from.sys.props");
	}
}
