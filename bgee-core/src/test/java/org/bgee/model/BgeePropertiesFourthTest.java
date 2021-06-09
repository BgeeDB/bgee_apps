package org.bgee.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for {@link BgeeProperties}.
 * It checks that the properties are loaded from the correct source
 * These tests are split in several test classes to avoid conflicts between tests due to
 * the per-thread singleton behavior.
 * 
 * @author  Mathieu Seppey
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 14, Mar. 2019
 * @since   Bgee 13
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
        System.clearProperty(BgeeProperties.MAJOR_VERSION_KEY);
        System.clearProperty(BgeeProperties.MINOR_VERSION_KEY);
        System.clearProperty(BgeeProperties.BGEE_SEARCH_SERVER_URL_KEY);
        System.clearProperty(BgeeProperties.BGEE_SEARCH_SERVER_PORT_KEY);
        System.clearProperty(BgeeProperties.BIOCONDUCTOR_RELEASE_NUMBER_KEY);
        System.clearProperty(BgeeProperties.BGEE_SEARCH_INDEX_GENES_KEY);
        System.clearProperty(BgeeProperties.BGEE_SEARCH_INDEX_AUTOCOMPLETE_KEY);
        System.clearProperty(BgeeProperties.TOP_ANAT_R_SCRIPT_EXECUTABLE_KEY);
        System.clearProperty(BgeeProperties.TOP_ANAT_R_WORKING_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.TOP_ANAT_FUNCTION_FILE_KEY);
        System.clearProperty(BgeeProperties.TOP_ANAT_RESULTS_WRITING_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.MAX_JOB_COUNT_PER_USER_KEY);
        
        // Also, set the properties file to an non-existing file, 
        // so that no property file is used (otherwise, property files in src/test/resources/ 
        // or src/main/resources/ would be used).
        System.setProperty(BgeeProperties.PROPERTIES_FILE_NAME_KEY, "/none");

        // get the instance of bgeeproperties and check the values
        this.bgeeProp = BgeeProperties.getBgeeProperties();
        assertEquals("Wrong property value retrieved",
                BgeeProperties.MAJOR_VERSION_DEFAULT,
                bgeeProp.getMajorVersion());
        assertEquals("Wrong property value retrieved",
                BgeeProperties.MINOR_VERSION_DEFAULT,
                bgeeProp.getMinorVersion());
        assertEquals("Wrong property value retrieved",
                BgeeProperties.BGEE_SEARCH_SERVER_URL_DEFAULT,
                bgeeProp.getSearchServerURL());
        assertEquals("Wrong property value retrieved",
                BgeeProperties.BGEE_SEARCH_SERVER_PORT_DEFAULT,
                bgeeProp.getSearchServerPort());
        assertEquals("Wrong property value retrieved",
                BgeeProperties.BIOCONDUCTOR_RELEASE_NUMBER_DEFAULT,
                bgeeProp.getBioconductorReleaseNumber());
        assertEquals("Wrong property value retrieved",
                BgeeProperties.BGEE_SEARCH_INDEX_GENES_DEFAULT,
                bgeeProp.getSearchGenesIndex());
        assertEquals("Wrong property value retrieved",
                BgeeProperties.BGEE_SEARCH_INDEX_AUTOCOMPLETE_DEFAULT,
                bgeeProp.getSearchAutocompleteIndex());
        assertEquals("Wrong property value retrieved",
                BgeeProperties.TOP_ANAT_R_SCRIPT_EXECUTABLE_DEFAULT,
                bgeeProp.getTopAnatRScriptExecutable());
        assertEquals("Wrong property value retrieved",
                BgeeProperties.TOP_ANAT_R_WORKING_DIRECTORY_DEFAULT,
                bgeeProp.getTopAnatRWorkingDirectory());
        assertEquals("Wrong property value retrieved",
                BgeeProperties.TOP_ANAT_FUNCTION_FILE_DEFAULT,
                bgeeProp.getTopAnatFunctionFile());
        assertEquals("Wrong property value retrieved",
                BgeeProperties.TOP_ANAT_RESULTS_WRITING_DIRECTORY_DEFAULT, 
                bgeeProp.getTopAnatResultsWritingDirectory());
        assertEquals("Wrong property value retrieved",
                BgeeProperties.MAX_JOB_COUNT_PER_USER_DEFAULT, 
                bgeeProp.getMaxJobCountPerUser());
    }
}
