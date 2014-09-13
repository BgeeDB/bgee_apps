package org.bgee.controller;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

/**
 * Unit tests for {@link BgeeProperties}.
 * It checks that the properties are loaded from the correct source
 * 
 * @author Mathieu Seppey
 * @version Bgee 13
 * @since Bgee 13
 */
public class BgeePropertiesTest {
 
    /**
     * A {@code Timeout} whose only purpose is to force JUnit to run independent thread
     * for each test, which is important because of the "per-thread singleton" behavior of
     * some important classes such as BgeeProperties
     */
    @Rule
    public Timeout globalTimeout= new Timeout(99999);
    
    /**
     * A {@code BgeeProperties} instance to run the tests on.
     */
    private BgeeProperties bgeeProp;

    /**
     * Set the properties with the default expected values for these tests
     */
    @Before
    public void initTests(){
        System.setProperty(BgeeProperties.PROPERTIES_FILE_NAME_KEY, 
                "/test.properties");
        System.setProperty(BgeeProperties.BGEE_ROOT_DIRECTORY_KEY, 
                "/system");
        System.setProperty(BgeeProperties.URL_MAX_LENGTH_KEY, 
                "30");
    }
    
    /**
     * Reset the properties to avoid to disturb other tests
     */
    @AfterClass
    public static void resetProperties(){
        System.clearProperty(BgeeProperties.PROPERTIES_FILE_NAME_KEY);
        System.clearProperty(BgeeProperties.BGEE_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.URL_MAX_LENGTH_KEY);
    }
    
    /**
     * Test that the injected {@code java.util.Properties} are used
     */
    @Test
    public void testInjectedProperties(){
        // set the properties to inject
        Properties prop = new Properties();
        prop.put(BgeeProperties.BGEE_ROOT_DIRECTORY_KEY, "/injected");
        prop.put(BgeeProperties.URL_MAX_LENGTH_KEY, "10");
        // get the instance of bgeeproperties and check the values
        this.bgeeProp = BgeeProperties.getBgeeProperties(prop);
        assertEquals("Wrong property value retrieved","/injected",bgeeProp.getBgeeRootDirectory());
        assertEquals("Wrong property value retrieved","10",bgeeProp.getUrlMaxLength().toString());
    }

    /**
     * Test that the {@code java.util.Properties} are read from the file
     */
    @Test
    public void testLoadPropertiesFromFile(){
        // First clear the system properties that would be used if present
        System.clearProperty(BgeeProperties.BGEE_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.URL_MAX_LENGTH_KEY);
        // get the instance of bgeeproperties and check the values
        this.bgeeProp = BgeeProperties.getBgeeProperties();
        assertEquals("Wrong property value retrieved","/file",bgeeProp.getBgeeRootDirectory());
        assertEquals("Wrong property value retrieved","20",bgeeProp.getUrlMaxLength().toString());
    }

    /**
     * Test that the {@code java.util.Properties} are read from the system properties
     */
    @Test
    public void testLoadSystemProperties(){
        // get the instance of bgeeproperties and check the values
        this.bgeeProp = BgeeProperties.getBgeeProperties();
        assertEquals("Wrong property value retrieved","/system",bgeeProp.getBgeeRootDirectory());
        assertEquals("Wrong property value retrieved","30",bgeeProp.getUrlMaxLength().toString());
    }
    
    /**
     * Test that the {@code java.util.Properties} are loaded using the default values
     */
    @Test
    public void testLoadDefaultProperties(){
        // First clear the system properties that would be used if present
        // and also the name of the file on the classpath that would be used as well
        System.clearProperty(BgeeProperties.PROPERTIES_FILE_NAME_KEY);
        System.clearProperty(BgeeProperties.BGEE_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.URL_MAX_LENGTH_KEY);
        // get the instance of bgeeproperties and check the values
        this.bgeeProp = BgeeProperties.getBgeeProperties();
        assertEquals("Wrong property value retrieved","/",bgeeProp.getBgeeRootDirectory());
        assertEquals("Wrong property value retrieved","120",bgeeProp.getUrlMaxLength().toString());
    }
    
    /**
     * Test that the returned {@code BgeeProperties} instance is always the same within the
     * same thread
     */
    @Test
    public void testOnePropertiesPerThread(){
        BgeeProperties bgeeProp1 = BgeeProperties.getBgeeProperties();
        BgeeProperties bgeeProp2 = BgeeProperties.getBgeeProperties();
        assertEquals("The two objects are not the same but they should be",
                System.identityHashCode(bgeeProp1),System.identityHashCode((bgeeProp2)));
    }

}
