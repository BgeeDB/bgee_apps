package org.bgee.model.properties;

import static org.junit.Assert.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeProperties;
import org.bgee.model.TestAncestor;
import org.junit.Test;

/**
 * Class testing the functionalities of 
 * {@link org.bgee.model.BgeeProperties BgeeProperties}. 
 * <p>
 * Test the behavior when loading the {@code Properties} from a properties file.
 * It has to be done in a different class than when testing loading from System 
 * properties, as the properties are read only once at class loading, so only once 
 * for a given {@code ClassLoader}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13, Mar 2013
 * @since Bgee 13
 */
//FIXME: fix tests, check other modules such as bgee-pipeline for similar tests
public class PropertiesFromFileTest extends TestAncestor
{
	private final static Logger log = LogManager.getLogger(PropertiesFromFileTest.class.getName());
	
	/**
	 * Default Constructor. 
	 */
	public PropertiesFromFileTest()
	{
		super();
	}
	@Override
	protected Logger getLogger() {
		return log;
	}

//	/**
//	 * Try to load the properties from a properties file. 
//	 */
//	@Test
//	public void shouldLoadPropertiesFromFile()
//	{
//		//set a system properties to provide the name of the properties file 
//		//(default is bgee.properties, but we want to use a test file)
//		System.setProperty("bgee.properties.file", "/test.properties");
//		
//		//check if the properties correspond to values in the test file
//		assertEquals("Incorrect property jdbcDriver", "bgee.jdbc.driver.test", 
//				BgeeProperties.getBgeeProperties().getJdbcDriver());
//		assertEquals("Incorrect property jdbcUrl", "bgee.jdbc.url.test", 
//				BgeeProperties.getBgeeProperties().getJdbcUrl());
//		assertEquals("Incorrect property jdbcUsername", "bgee.jdbc.username.test", 
//				BgeeProperties.getBgeeProperties().getJdbcUsername());
//		assertEquals("Incorrect property jdbcPassword", "bgee.jdbc.password.test", 
//				BgeeProperties.getBgeeProperties().getJdbcPassword());
//		assertTrue("Incorrect property useStaticFactories",  
//				BgeeProperties.getBgeeProperties().useStaticFactories());
//		
//		//clear the System properties
//		System.clearProperty("bgee.properties.file");
//	}
}
