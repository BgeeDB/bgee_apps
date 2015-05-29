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
 * @author Mathieu Seppey
 * @author Valentine Rech de Laval
 * @author Frederic Bastian
 * @version Bgee 13 Mar. 2015
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
        System.clearProperty(BgeeProperties.BGEE_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.URL_MAX_LENGTH_KEY);
        System.clearProperty(BgeeProperties.REQUEST_PARAMETERS_STORAGE_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.DOWNLOAD_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.DOWNLOAD_EXPR_FILES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.DOWNLOAD_DIFF_EXPR_FILES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.DOWNLOAD_MULTI_DIFF_EXPR_FILES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.DOWNLOAD_ORTHOLOG_FILES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.DOWNLOAD_REF_EXPR_FILES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.JAVASCRIPT_FILES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.JAVASCRIPT_VERSION_EXTENSION_KEY);
        System.clearProperty(BgeeProperties.CSS_FILES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.CSS_VERSION_EXTENSION_KEY);
        System.clearProperty(BgeeProperties.IMAGES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.TOP_OBO_RESULTS_URL_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.WEBPAGES_CACHE_CONFIG_FILE_NAME_KEY);
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
        System.clearProperty(BgeeProperties.PROPERTIES_FILE_NAME_KEY);
        System.clearProperty(BgeeProperties.BGEE_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.URL_MAX_LENGTH_KEY);
        System.clearProperty(BgeeProperties.REQUEST_PARAMETERS_STORAGE_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.DOWNLOAD_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.DOWNLOAD_EXPR_FILES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.DOWNLOAD_DIFF_EXPR_FILES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.DOWNLOAD_MULTI_DIFF_EXPR_FILES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.DOWNLOAD_ORTHOLOG_FILES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.DOWNLOAD_REF_EXPR_FILES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.JAVASCRIPT_FILES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.JAVASCRIPT_VERSION_EXTENSION_KEY);
        System.clearProperty(BgeeProperties.CSS_FILES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.CSS_VERSION_EXTENSION_KEY);
        System.clearProperty(BgeeProperties.IMAGES_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.TOP_OBO_RESULTS_URL_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.WEBPAGES_CACHE_CONFIG_FILE_NAME_KEY);
    }

}
