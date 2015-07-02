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
 * Test the behavior when loading the {@code Properties} from the System properties.
 * It has to be done in a different class than when testing loading from properties file,  
 * as the properties are read only once at class loading, so only once 
 * for a given {@code ClassLoader}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13, Mar 2013
 * @since Bgee 13
 */
//FIXME: fix tests, check other modules such as bgee-pipeline for similar tests
public class PropertiesFromSystemTest extends TestAncestor
{
	private final static Logger log = LogManager.getLogger(PropertiesFromSystemTest.class.getName());
	
	/**
	 * Default Constructor. 
	 */
	public PropertiesFromSystemTest()
	{
		super();
	}
	@Override
	protected Logger getLogger() {
		return log;
	}

//	/**
//	 * Try to load the properties from the System properties. 
//	 */
//	@Test
//	public void shouldLoadPropertiesFromSysProps()
//	{
//		//set the properties to test
//		System.setProperty("bgee.jdbc.driver", "bgee.jdbc.driver.test");
//		System.setProperty("bgee.jdbc.url", "bgee.jdbc.url.test");
//		System.setProperty("bgee.jdbc.username", "bgee.jdbc.username.test");
//		System.setProperty("bgee.jdbc.password", "bgee.jdbc.password.test");
//		System.setProperty("bgee.static.factories", "yes");
//
//		//set the properties file to an non-existing file, 
//		//so that System properties are used 
//		System.setProperty("bgee.properties.file", "/none");
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
//		System.clearProperty("bgee.jdbc.driver");
//		System.clearProperty("bgee.jdbc.url");
//		System.clearProperty("bgee.jdbc.username");
//		System.clearProperty("bgee.jdbc.password");
//		System.clearProperty("bgee.static.factories");
//	}
}
