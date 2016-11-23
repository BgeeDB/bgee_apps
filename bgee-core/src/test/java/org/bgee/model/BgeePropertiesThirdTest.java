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
 * @version Bgee 13, Oct 2016
 * @since Bgee 13
 * @see BgeePropertiesParentTest
 * @see BgeePropertiesFirstTest
 * @see BgeePropertiesSecondTest
 * @see BgeePropertiesThirdTest
 * @see BgeePropertiesFourthTest
 */
public class BgeePropertiesThirdTest extends BgeePropertiesParentTest {

    /**
     * Test that the {@code java.util.Properties} are read from the system properties, 
     * and that they have precedence over properties from a property file.
     */
    @Test
    public void testLoadSystemProperties() {
        System.setProperty(BgeeProperties.PROPERTIES_FILE_NAME_KEY, "/test.properties");
        //BGEE_ROOT_DIRECTORY_KEY is not set in System properties, it should be retrieve 
        //from the file. 
        //System.setProperty(BgeeProperties.BGEE_ROOT_DIRECTORY_KEY, "/system");
        //Other properties are set in System properties, they should override properties 
        //from file
        System.setProperty(BgeeProperties.TOP_ANAT_R_SCRIPT_EXECUTABLE_KEY, "/sysrexec");
        System.setProperty(BgeeProperties.TOP_ANAT_R_WORKING_DIRECTORY_KEY, 
                "/sysrwd");
        System.setProperty(BgeeProperties.TOP_ANAT_FUNCTION_FILE_KEY, "/sysfunction");
        System.setProperty(BgeeProperties.TOP_ANAT_RESULTS_WRITING_DIRECTORY_KEY, "/syswd");
        System.setProperty(BgeeProperties.MAX_JOB_COUNT_PER_USER_KEY, "5");
        
        // get the instance of bgeeproperties and check the values
        this.bgeeProp = BgeeProperties.getBgeeProperties();
        assertEquals("Wrong property value retrieved","/sysrexec",
                bgeeProp.getTopAnatRScriptExecutable());
        assertEquals("Wrong property value retrieved","/sysrwd",
                bgeeProp.getTopAnatRWorkingDirectory());
        assertEquals("Wrong property value retrieved", 
                "/sysfunction", bgeeProp.getTopAnatFunctionFile());
        assertEquals("Wrong property value retrieved", 
                "/syswd", bgeeProp.getTopAnatResultsWritingDirectory());
        assertEquals("Wrong property value retrieved", 
                5, bgeeProp.getMaxJobCountPerUser());
        
    }
}
