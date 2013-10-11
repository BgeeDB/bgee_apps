package org.bgee.model.dao.mysql.datasource;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.DataSourceFactory;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.mysql.MockDriverUtils;
import org.bgee.model.dao.mysql.MySQLDAOManager;

import com.sun.jndi.fscontext.RefFSContextFactory;

/**
 * This class provides methods to run for test classes using a {@code MySQLDAOManager}: 
 * methods to be called before and after running all tests, 
 * before and after each test. They allow to use a mocked {@code Driver}, 
 * to obtain mocked {@code Connection}.
 * 
 * @author Frederic Bastian
 * @author Mathieu Seppey
 * @version Bgee 13, May 2013
 * @since Bgee 13
 */
public class InitDataSourceTest {
    private static final String DATASOURCENAME = "testdatasource";

    /**
     * Change the System properties 
     * in order to automatically acquire mocked {@code Driver}.
     */
    public static void initClass() { 
        System.setProperty(MySQLDAOManager.CONFIGFILEKEY, "/none");
        System.setProperty(MySQLDAOManager.JDBCURLKEY, MockDriverUtils.MOCKURL);
        System.setProperty(MySQLDAOManager.JDBCDRIVERNAMEKEY, DriverTestImpl.class.getName());
        System.setProperty(MySQLDAOManager.USERNAMEKEY, "bgee.jdbc.username.test");
        System.setProperty(MySQLDAOManager.PASSWORDKEY, "bgee.jdbc.password.test");
        System.setProperty(MySQLDAOManager.RESOURCENAMEKEY, DATASOURCENAME);
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

        // And bind it to the initial context 
        new InitialContext().rebind(DATASOURCENAME, ref);   

    } 
    /**
     * Reset the System properties that were changed 
     * in order to automatically acquire mocked {@code Driver}.
     */
    public static void unloadClass()
    {
        System.clearProperty(MySQLDAOManager.CONFIGFILEKEY);
        System.clearProperty(MySQLDAOManager.JDBCURLKEY);
        System.clearProperty(MySQLDAOManager.JDBCDRIVERNAMEKEY);
        System.clearProperty(MySQLDAOManager.USERNAMEKEY);
        System.clearProperty(MySQLDAOManager.PASSWORDKEY);
        System.clearProperty(MySQLDAOManager.RESOURCENAMEKEY);
    }

    /**
     * Deregister the mocked {@code Driver}.
     */
    public static void unload() 
         {
        if (DAOManager.hasDAOManager()) {
            DAOManager.getDAOManager().close();
        }
    }
}
