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
 * Test the behavior when loading the {@code Properties} from both the System properties 
 * and a property file.
 * It has to be done in a different class than when testing loading from properties file only, 
 * and System properties only,  
 * as the properties are read only once at class loading, so only once 
 * for a given {@code ClassLoader}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13, Mar 2013
 * @since Bgee 13
 */
//FIXME: fix tests, check other modules such as bgee-pipeline for similar tests
public class PropertiesMixLoadingTest extends TestAncestor
{
	private final static Logger log = LogManager.getLogger(PropertiesMixLoadingTest.class.getName());
	
	/**
	 * Default Constructor. 
	 */
	public PropertiesMixLoadingTest()
	{
		super();
	}
	@Override
	protected Logger getLogger() {
		return log;
	}
	
//	/**
//	 * Bgee reads the properties from both the System properties and the property file, 
//	 * so that for instance a property can be provided in the file, 
//	 * and another property via System properties. 
//	 * If a property is defined in both locations, 
//	 * then the System properties override the property file. 
//	 * <p>
//	 * Test this behavior.
//	 */
//	@Test
//	public void shouldMixPropertiesFromFileAndSystem()
//	{
//		//set a system properties to provide the name of the properties file 
//		//(default is bgee.properties, but we want to use a test file)
//		System.setProperty("bgee.properties.file", "/test.properties");
//		//set only some properties via System properties
//		System.setProperty("bgee.jdbc.username", "bgee.jdbc.username.test.sys");
//		System.setProperty("bgee.jdbc.password", "bgee.jdbc.password.test.sys");
//		System.setProperty("bgee.static.factories", "on");
//		
//		//check if the properties correspond to values in the test file
//		assertEquals("Incorrect property jdbcDriver", "bgee.jdbc.driver.test", 
//				BgeeProperties.getBgeeProperties().getJdbcDriver());
//		assertEquals("Incorrect property jdbcUrl", "bgee.jdbc.url.test", 
//				BgeeProperties.getBgeeProperties().getJdbcUrl());
//		assertEquals("Incorrect property jdbcUsername", "bgee.jdbc.username.test.sys", 
//				BgeeProperties.getBgeeProperties().getJdbcUsername());
//		assertEquals("Incorrect property jdbcPassword", "bgee.jdbc.password.test.sys", 
//				BgeeProperties.getBgeeProperties().getJdbcPassword());
//		assertTrue("Incorrect property useStaticFactories",  
//				BgeeProperties.getBgeeProperties().useStaticFactories());
//
//		//clear the System properties
//		System.clearProperty("bgee.properties.file");
//		System.clearProperty("bgee.jdbc.username");
//		System.clearProperty("bgee.jdbc.password");
//		System.clearProperty("bgee.static.factories");
//	}
}
