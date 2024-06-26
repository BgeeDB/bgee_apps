package org.bgee.controller;

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
 * @author  Mathieu Seppey
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 14, Feb. 2018
 * @since   Bgee 13
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
        System.clearProperty(BgeeProperties.MINIFY_KEY);
        System.clearProperty(BgeeProperties.WARNING_MESSAGE_KEY);
        System.clearProperty(BgeeProperties.ARCHIVE_KEY);
        System.clearProperty(BgeeProperties.BGEE_CURRENT_URL_KEY);
        System.clearProperty(BgeeProperties.BGEE_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.URL_MAX_LENGTH_KEY);
        System.clearProperty(BgeeProperties.REQUEST_PARAMETERS_STORAGE_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.FTP_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.DOWNLOAD_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.DOWNLOAD_EXPR_FILES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.DOWNLOAD_DIFF_EXPR_FILES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.DOWNLOAD_MULTI_DIFF_EXPR_FILES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.DOWNLOAD_ORTHOLOG_FILES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.DOWNLOAD_AFFY_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.DOWNLOAD_RNA_SEQ_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.JAVASCRIPT_FILES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.JAVASCRIPT_VERSION_EXTENSION_KEY);
        System.clearProperty(BgeeProperties.CSS_FILES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.CSS_VERSION_EXTENSION_KEY);
        System.clearProperty(BgeeProperties.IMAGES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.LOGO_IMAGES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.SPECIES_IMAGES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.WEBPAGES_CACHE_CONFIG_FILE_NAME_KEY);
        System.clearProperty(BgeeProperties.TOP_ANAT_RESULTS_URL_DIRECTORY_KEY);
        // Superclass property
        System.clearProperty(BgeeProperties.TOP_ANAT_FUNCTION_FILE_KEY);
    }
}
