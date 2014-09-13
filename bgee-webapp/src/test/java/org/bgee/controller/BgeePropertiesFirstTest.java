package org.bgee.controller;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.Test;

/**
 * Unit tests for {@link BgeeProperties}.
 * It checks that the properties are loaded from the correct source
 * These tests are split in several test classes to avoid conflicts between tests due to
 * the per-thread singleton behavior.
 * 
 * @author Mathieu Seppey
 * @version Bgee 13
 * @since Bgee 13
 * @see BgeePropertiesParentTest
 * @see BgeePropertiesFirstTest
 * @see BgeePropertiesSecondTest
 * @see BgeePropertiesThirdTest
 * @see BgeePropertiesFourthTest
 */
public class BgeePropertiesFirstTest extends BgeePropertiesParentTest {
       
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
