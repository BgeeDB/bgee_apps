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
 * @version Bgee 13
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
        System.setProperty(BgeeProperties.PROPERTIES_FILE_NAME_KEY, "/test.properties");
        System.setProperty(BgeeProperties.BGEE_ROOT_DIRECTORY_KEY, "/system");
        System.setProperty(BgeeProperties.URL_MAX_LENGTH_KEY, "30");
        System.setProperty(BgeeProperties.REQUEST_PARAMETERS_STORAGE_DIRECTORY_KEY, "/requestParamStorDir");
        System.setProperty(BgeeProperties.DOWNLOAD_ROOT_DIRECTORY_KEY, "/downRootDir");
        System.setProperty(BgeeProperties.JAVASCRIPT_FILES_ROOT_DIRECTORY_KEY, "/jsFilesRootDir");
        System.setProperty(BgeeProperties.CSS_FILES_ROOT_DIRECTORY_KEY, "/cssFileRootDir");
        System.setProperty(BgeeProperties.IMAGES_ROOT_DIRECTORY_KEY, "/imgRootDir");
        System.setProperty(BgeeProperties.TOP_OBO_RESULTS_URL_ROOT_DIRECTORY_KEY, "/topOboDir");
        System.setProperty(BgeeProperties.WEBPAGES_CACHE_CONFIG_FILE_NAME_KEY, "cacheConfigFileName");

    }
    
    /**
     * Reset the properties to avoid to disturb other tests
     */
    @After
    public void resetProperties(){
        System.clearProperty(BgeeProperties.PROPERTIES_FILE_NAME_KEY);
        System.clearProperty(BgeeProperties.BGEE_ROOT_DIRECTORY_KEY);
        System.clearProperty(BgeeProperties.URL_MAX_LENGTH_KEY);
    }

}
