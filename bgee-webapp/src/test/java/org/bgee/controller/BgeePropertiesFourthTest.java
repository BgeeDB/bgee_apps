package org.bgee.controller;

import static org.junit.Assert.assertEquals;

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
public class BgeePropertiesFourthTest extends BgeePropertiesParentTest {
    
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

}
