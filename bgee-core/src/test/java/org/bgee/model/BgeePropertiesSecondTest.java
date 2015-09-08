package org.bgee.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for {@link BgeeProperties}.
 * It checks that the properties are loaded from the correct source
 * These tests are split in several test classes to avoid conflicts between tests due to
 * the per-thread singleton behavior.
 * 
 * @author Mathieu Seppey
 * @author Valentine Rech de Laval
 * @author Frederic Bastian
 * @version Bgee 13, June 2015
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
                
        //set the file to use
        System.setProperty(BgeeProperties.PROPERTIES_FILE_NAME_KEY, "/test.properties");

        // get the instance of bgeeproperties and check the values
        this.bgeeProp = BgeeProperties.getBgeeProperties();
        assertEquals("Wrong property value retrieved","/filerexec",
                bgeeProp.getTopAnatRScriptExecutable());
        assertEquals("Wrong property value retrieved","/filecallerwd",
                bgeeProp.getTopAnatRCallerWorkingDirectory());
        assertEquals("Wrong property value retrieved", 
                "/filefunctionfile", bgeeProp.getTopAnatFunctionFile());
        assertEquals("Wrong property value retrieved", 
                "/filewd", bgeeProp.getTopAnatResultsWritingDirectory());
        assertEquals("Wrong property value retrieved", 
                "/fileurldir", bgeeProp.getTopAnatResultsUrlDirectory());
    }
}
