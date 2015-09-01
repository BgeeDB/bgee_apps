package org.bgee.controller;

import org.junit.After;
import org.junit.Before;

/**
 * Unit tests for {@link BgeeWebappProperties}.
 * It checks that the properties are loaded from the correct source
 * These tests are split in several test classes to avoid conflicts between tests due to
 * the per-thread singleton behavior.
 * This abstract class is extended by all other test classes and define the common part of the
 * {@link BgeeWebappProperties} tests.
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
public abstract class BgeePropertiesParentTest {
     
    /**
     * A {@code BgeeProperties} instance to run the tests on.
     */
    protected BgeeWebappProperties bgeeProp;

    /**
     * Set the properties with the default expected values for these tests
     */
    @Before
    public void initTests(){
        this.clearCommonProperties();
        // set the properties file to an non-existing file, 
        // so that no property file is used (otherwise, property files in src/test/resources/ 
        // or src/main/resources/ would be used).
        System.setProperty(BgeeWebappProperties.PROPERTIES_FILE_NAME_KEY, "/none");
    }
    
    /**
     * Reset the properties to avoid to disturb other tests
     */
    @After
    public void resetProperties(){
        this.clearCommonProperties();
        System.clearProperty(BgeeWebappProperties.PROPERTIES_FILE_NAME_KEY);
    }

    /**
     * Clear the common properties to be cleared before and after tests.
     */
    private void clearCommonProperties() {
        System.clearProperty(BgeeWebappProperties.BGEE_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeWebappProperties.URL_MAX_LENGTH_KEY);
        System.clearProperty(BgeeWebappProperties.REQUEST_PARAMETERS_STORAGE_DIRECTORY_KEY);
        System.clearProperty(BgeeWebappProperties.FTP_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeWebappProperties.DOWNLOAD_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeWebappProperties.DOWNLOAD_EXPR_FILES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeWebappProperties.DOWNLOAD_DIFF_EXPR_FILES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeWebappProperties.DOWNLOAD_MULTI_DIFF_EXPR_FILES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeWebappProperties.DOWNLOAD_ORTHOLOG_FILES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeWebappProperties.DOWNLOAD_AFFY_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeWebappProperties.DOWNLOAD_RNA_SEQ_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeWebappProperties.JAVASCRIPT_FILES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeWebappProperties.JAVASCRIPT_VERSION_EXTENSION_KEY);
        System.clearProperty(BgeeWebappProperties.CSS_FILES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeWebappProperties.CSS_VERSION_EXTENSION_KEY);
        System.clearProperty(BgeeWebappProperties.IMAGES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeWebappProperties.LOGO_IMAGES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeWebappProperties.SPECIES_IMAGES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeWebappProperties.WEBPAGES_CACHE_CONFIG_FILE_NAME_KEY);
    }
}
