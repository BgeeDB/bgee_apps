package org.bgee.model.data.sql.datasource;

import org.bgee.model.data.sql.MockDriverUtils;

/**
 * This class provides methods to run for test classes using a <code>BgeeDataSource</code>: 
 * methods to be called before and after running all tests, 
 * before and after each test. They allow to use a mocked <code>Driver</code>, 
 * to obtain mocked <code>Connection</code>.
 * 
 * @author Frederic Bastian
 * @version Bgee 13, Mar 2013
 * @since Bgee 13
 */
public class InitDataSourceTest 
{
	/**
	 * The <code>MockDriverUtils</code> providing mocked <code>Driver</code> 
	 * and mocked <code>Connection</code>s.
	 */
	private static volatile MockDriverUtils mockDriverUtils;
	
	/**
	 * Change the System properties 
	 * in order to automatically acquire mocked <code>Driver</code>.
	 */
	public static void initClass()
	{
		System.setProperty("bgee.properties.file", "/none");
		System.setProperty("bgee.jdbc.url", MockDriverUtils.MOCKURL);
		System.clearProperty("bgee.jdbc.driver");
		System.setProperty("bgee.jdbc.username", "bgee.jdbc.username.test");
		System.setProperty("bgee.jdbc.password", "bgee.jdbc.password.test");
	}
	/**
	 * Reset the System properties that were changed 
	 * in order to automatically acquire mocked <code>Driver</code>.
	 */
	public static void unloadClass()
	{
		System.clearProperty("bgee.jdbc.url");
		System.clearProperty("bgee.jdbc.username");
		System.clearProperty("bgee.jdbc.password");
	}
	
	/**
	 * Obtain a <code>MockDriverUtils</code>, loading a mocked <code>Driver</code> 
	 * that will registered itself to the <code>DriverManager</code>, allowing to provide 
	 * mocked <code>Connection</code>s. 
	 * @see #unload()
	 */
	public static void init()
	{
		mockDriverUtils = new MockDriverUtils();
	}
	/**
	 * Deregister the mocked <code>Driver</code>.
	 * @see #init()
	 */
	public static void unload()
	{
		mockDriverUtils.deregister();
	}
	
	public static MockDriverUtils getMockDriverUtils()
	{
		return mockDriverUtils;
	}
}
