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
public class BgeePropertiesFourthTest extends BgeePropertiesParentTest {
    
    /**
     * Test that the {@code java.util.Properties} are loaded using the default values
     */
    @Test
    public void testLoadDefaultProperties(){
        // First clear the system properties that would be used if present.
        System.clearProperty(BgeeProperties.TOP_ANAT_R_SCRIPT_EXECUTABLE_KEY);
        System.clearProperty(BgeeProperties.TOP_ANAT_RCALLER_WORKING_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.TOP_ANAT_FUNCTION_FILE_KEY);
        System.clearProperty(BgeeProperties.TOP_ANAT_RESULTS_WRITING_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.TOP_ANAT_RESULTS_URL_DIRECTORY_KEY);
        
        // Also, set the properties file to an non-existing file, 
        // so that no property file is used (otherwise, property files in src/test/resources/ 
        // or src/main/resources/ would be used).
        System.setProperty(BgeeProperties.PROPERTIES_FILE_NAME_KEY, "/none");

        // get the instance of bgeeproperties and check the values
        this.bgeeProp = BgeeProperties.getBgeeProperties();
        assertEquals("Wrong property value retrieved",
                BgeeProperties.TOP_ANAT_R_SCRIPT_EXECUTABLE_DEFAULT,
                bgeeProp.getTopAnatRScriptExecutable());
        assertEquals("Wrong property value retrieved",
                BgeeProperties.TOP_ANAT_RCALLER_WORKING_DIRECTORY_DEFAULT,
                bgeeProp.getTopAnatRCallerWorkingDirectory());
        assertEquals("Wrong property value retrieved",
                BgeeProperties.TOP_ANAT_FUNCTION_FILE_DEFAULT,
                bgeeProp.getTopAnatFunctionFile());
        assertEquals("Wrong property value retrieved",
                BgeeProperties.TOP_ANAT_RESULTS_WRITING_DIRECTORY_DEFAULT, 
                bgeeProp.getTopAnatResultsWritingDirectory());
        assertEquals("Wrong property value retrieved",
                BgeeProperties.TOP_ANAT_RESULTS_URL_DIRECTORY_DEFAULT,
                bgeeProp.getTopAnatResultsUrlDirectory());
    }
}
