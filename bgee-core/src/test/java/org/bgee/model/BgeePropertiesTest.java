package org.bgee.model;

import static org.junit.Assert.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeProperties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Class testing the functionalities of 
 * {@link org.bgee.model.BgeeProperties BgeeProperties}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13, Mar 2013
 * @since Bgee 13
 */
public class BgeePropertiesTest extends TestAncestor
{
	private final static Logger log = LogManager.getLogger(BgeePropertiesTest.class.getName());
	
	/**
	 * Default Constructor. 
	 */
	public BgeePropertiesTest()
	{
		super();
	}
	
	@Override
	protected Logger getLogger() {
		return log;
	}
	
	/**
	 * Init the properties before the tests, 
	 * so that the logs for initialization do not pollute the logs of the test.
	 */
	@BeforeClass
	public static void initProperties()
	{
		//this will trigger the static initializer
		BgeeProperties.getJdbcDriver();
		log.info("========Start testing=========");
	}
	/**
	 * Reinit the properties after the tests. Not really useful 
	 * as another test class would use another class loader.
	 */
	@AfterClass
	public static void reinitProperties()
	{
		log.info("========End testing=========");
		BgeeProperties.init();
	}

	/**
	 * Try to load the properties from a properties file. 
	 */
	@Test
	public void shouldLoadPropertiesFromFile()
	{
		//set a system properties to provide the name of the properties file 
		//(default is bgee.properties, but we want to use a test file)
		System.setProperty("bgee.properties.file", "/test.properties");
		//Properties are set in a static initializer, only called once by a same ClassLoader.
		//Need to reinit the properties for the test, as we don't know which test is run first. 
		BgeeProperties.init();
		
		//check if the properties correspond to values in the test file
		assertEquals("Incorrect property jdbcDriver", 
				"bgee.jdbc.driver.test", BgeeProperties.getJdbcDriver());
		assertEquals("Incorrect property jdbcUrl", 
				"bgee.jdbc.url.test", BgeeProperties.getJdbcUrl());
		assertEquals("Incorrect property jdbcUsername", 
				"bgee.jdbc.username.test", BgeeProperties.getJdbcUsername());
		assertEquals("Incorrect property jdbcPassword", 
				"bgee.jdbc.password.test", BgeeProperties.getJdbcPassword());
		
		//clear the System properties
		System.clearProperty("bgee.properties.file");
	}

	/**
	 * Try to load the properties from the System properties. 
	 */
	@Test
	public void shouldLoadPropertiesFromSysProps()
	{
		//set the properties to test
		System.setProperty("bgee.jdbc.driver", "bgee.jdbc.driver.test");
		System.setProperty("bgee.jdbc.url", "bgee.jdbc.url.test");
		System.setProperty("bgee.jdbc.username", "bgee.jdbc.username.test");
		System.setProperty("bgee.jdbc.password", "bgee.jdbc.password.test");

		//set the properties file to an non-existing file, 
		//so that System properties are used 
		System.setProperty("bgee.properties.file", "/none");
		
		//Properties are set in a static initializer, only called once by a same ClassLoader.
		//Need to reinit the properties for the test, as we don't know which test is run first. 
		BgeeProperties.init();
		
		//check if the properties correspond to values in the system properties
		assertEquals("Incorrect property jdbcDriver", 
				"bgee.jdbc.driver.test", BgeeProperties.getJdbcDriver());
		assertEquals("Incorrect property jdbcUrl", 
				"bgee.jdbc.url.test", BgeeProperties.getJdbcUrl());
		assertEquals("Incorrect property jdbcUsername", 
				"bgee.jdbc.username.test", BgeeProperties.getJdbcUsername());
		assertEquals("Incorrect property jdbcPassword", 
				"bgee.jdbc.password.test", BgeeProperties.getJdbcPassword());
		
		//clear the System properties
		System.clearProperty("bgee.jdbc.driver");
		System.clearProperty("bgee.jdbc.url");
		System.clearProperty("bgee.jdbc.username");
		System.clearProperty("bgee.jdbc.password");
	}
	
	/**
	 * Bgee reads the properties from both the System properties and the property file, 
	 * so that for instance a property can be provided in the file, 
	 * and another property via System properties. 
	 * If a property is defined in both locations, 
	 * then the System properties override the property file. 
	 * <p>
	 * Test this behavior.
	 */
	@Test
	public void shouldMixPropertiesFromFileAndSystem()
	{
		//set a system properties to provide the name of the properties file 
		//(default is bgee.properties, but we want to use a test file)
		System.setProperty("bgee.properties.file", "/test.properties");
		//set only some properties via System properties
		System.setProperty("bgee.jdbc.username", "bgee.jdbc.username.test.sys");
		System.setProperty("bgee.jdbc.password", "bgee.jdbc.password.test.sys");
		
		//Properties are set in a static initializer, only called once by a same ClassLoader.
		//Need to reinit the properties for the test, as we don't know which test is run first. 
		BgeeProperties.init();
		
		//check if the properties correspond to values provided both 
		//in properties file and in system properties
		assertEquals("Incorrect property jdbcDriver", 
				"bgee.jdbc.driver.test", BgeeProperties.getJdbcDriver());
		assertEquals("Incorrect property jdbcUrl", 
				"bgee.jdbc.url.test", BgeeProperties.getJdbcUrl());
		assertEquals("Incorrect property jdbcUsername", 
				"bgee.jdbc.username.test.sys", BgeeProperties.getJdbcUsername());
		assertEquals("Incorrect property jdbcPassword", 
				"bgee.jdbc.password.test.sys", BgeeProperties.getJdbcPassword());

		//clear the System properties
		System.clearProperty("bgee.properties.file");
		System.clearProperty("bgee.jdbc.username");
		System.clearProperty("bgee.jdbc.password");
	}
}
