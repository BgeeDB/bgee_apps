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
public class BgeePropertiesSecondTest extends BgeePropertiesParentTest {
    
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
}
