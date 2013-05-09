package org.bgee.model.data.sql.datasource;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.DataSourceFactory;
import org.bgee.model.BgeeProperties;
import org.bgee.model.data.sql.DriverTestImpl;
import org.bgee.model.data.sql.MockDriverUtils;

import com.sun.jndi.fscontext.RefFSContextFactory;

/**
 * This class provides methods to run for test classes using a <code>BgeeDataSource</code>: 
 * methods to be called before and after running all tests, 
 * before and after each test. They allow to use a mocked <code>Driver</code>, 
 * to obtain mocked <code>Connection</code>.
 * 
 * @author Frederic Bastian
 * @author Mathieu Seppey
 * @version Bgee 13, May 2013
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
        System.setProperty("bgee.jdbc.pool.DataSource.resourceName","testdatasource");
    }
    /**
     * Create a naming service initial context in order to use a JNDI DataSource
     * 
     * @throws NamingException 
     * @throws IllegalStateException 
     */
    public static void initInitialContext() throws IllegalStateException, NamingException{

        // Set RefFSContextFactory as initial context factory.
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                RefFSContextFactory.class.getName());
        
        // Set the java tmp directory as filesystem service provider
        System.setProperty(Context.PROVIDER_URL,"file:"+System.getProperty("java.io.tmpdir"));        

        // Create a reference to a datasource object...
        Reference ref = new Reference(DataSource.class.getName(),
                DataSourceFactory.class.getName(),null);

        ref.add(new StringRefAddr("driverClassName",DriverTestImpl.class.getName()));

        // And bind it to the initial context with the name coming from the properties
        new InitialContext().rebind(
                BgeeProperties.getBgeeProperties().getDataSourceResourceName(), ref);   

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
        System.clearProperty("bgee.jdbc.pool.DataSource.resourceName");
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
