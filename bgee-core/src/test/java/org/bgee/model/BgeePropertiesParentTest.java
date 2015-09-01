package org.bgee.model;

import org.junit.After;
import org.junit.Before;

/**
 * Unit tests for {@link BgeeProperties}.
 * It checks that the properties are loaded from the correct source
 * These tests are split in several test classes to avoid conflicts between tests due to
 * the per-thread singleton behavior.
 * This abstract class is extended by all other test classes and define the common part of the
 * {@link BgeeProperties} tests.
 * 
 * @author Mathieu Seppey
 * @author Valentine Rech de Laval
 * @author Frederic Bastian
 * @version Bgee 13, August 2015
 * @since Bgee 13
 * @see BgeePropertiesParentTest
 * @see BgeePropertiesFirstTest
 * @see BgeePropertiesSecondTest
 * @see BgeePropertiesThirdTest
 * @see BgeePropertiesFourthTest
 */
public abstract class BgeePropertiesParentTest {
     
    /**
     * A {@code BgeeProperties} instance to run the tests on.
     */
    protected BgeeProperties bgeeProp;

    /**
     * Set the properties with the default expected values for these tests
     */
    @Before
    public void initTests(){
        this.clearCommonProperties();
        // set the properties file to an non-existing file, 
        // so that no property file is used (otherwise, property files in src/test/resources/ 
        // or src/main/resources/ would be used).
        System.setProperty(BgeeProperties.PROPERTIES_FILE_NAME_KEY, "/none");
    }
    
    /**
     * Reset the properties to avoid to disturb other tests
     */
    @After
    public void resetProperties(){
        this.clearCommonProperties();
        System.clearProperty(BgeeProperties.PROPERTIES_FILE_NAME_KEY);
    }

    /**
     * Clear the common properties to be cleared before and after tests.
     */
    private void clearCommonProperties() {
        System.clearProperty(BgeeProperties.TOP_ANAT_R_SCRIPT_EXECUTABLE_DEFAULT);
        System.clearProperty(BgeeProperties.TOP_ANAT_RCALLER_WORKING_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.TOP_ANAT_RCALLER_WORKING_DIRECTORY_DEFAULT);
        System.clearProperty(BgeeProperties.TOP_ANAT_FUNCTIONS_FILE_KEY);
        System.clearProperty(BgeeProperties.TOP_ANAT_FUNCTIONS_FILE_DEFAULT);
        System.clearProperty(BgeeProperties.TOP_ANAT_RESULTS_WRITING_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.TOP_ANAT_RESULTS_WRITING_DIRECTORY_DEFAULT);
        System.clearProperty(BgeeProperties.TOP_ANAT_RESULTS_URL_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.TOP_ANAT_RESULTS_URL_DIRECTORY_DEFAULT);
    }
}
