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
public class BgeePropertiesThirdTest extends BgeePropertiesParentTest {

    /**
     * Test that the {@code java.util.Properties} are read from the system properties, 
     * and that they have precedence over properties from a property file.
     */
    @Test
    public void testLoadSystemProperties(){
        System.setProperty(BgeeProperties.PROPERTIES_FILE_NAME_KEY, "/test.properties");
        //BGEE_ROOT_DIRECTORY_KEY is not set in System properties, it should be retrieve 
        //from the file. 
        //System.setProperty(BgeeProperties.BGEE_ROOT_DIRECTORY_KEY, "/system");
        //Other properties are set in System properties, they should override properties 
        //from file
        System.setProperty(BgeeProperties.URL_MAX_LENGTH_KEY, "30");
        System.setProperty(BgeeProperties.REQUEST_PARAMETERS_STORAGE_DIRECTORY_KEY, "/requestParamStorDir");
        System.setProperty(BgeeProperties.DOWNLOAD_ROOT_DIRECTORY_KEY, "/downRootDir");
        System.setProperty(BgeeProperties.DOWNLOAD_EXPR_FILES_ROOT_DIRECTORY_KEY, "/downExprFileDir");
        System.setProperty(BgeeProperties.DOWNLOAD_DIFF_EXPR_FILES_ROOT_DIRECTORY_KEY, "/downDiffExprFileDir");
        System.setProperty(BgeeProperties.JAVASCRIPT_FILES_ROOT_DIRECTORY_KEY, "/jsFilesRootDir");
        System.setProperty(BgeeProperties.JAVASCRIPT_VERSION_EXTENSION_KEY, "-extension-js-1");
        System.setProperty(BgeeProperties.CSS_FILES_ROOT_DIRECTORY_KEY, "/cssFileRootDir");
        System.setProperty(BgeeProperties.CSS_VERSION_EXTENSION_KEY, "-extension-css-1");
        System.setProperty(BgeeProperties.IMAGES_ROOT_DIRECTORY_KEY, "/imgRootDir");
        System.setProperty(BgeeProperties.TOP_OBO_RESULTS_URL_ROOT_DIRECTORY_KEY, "/topOboDir");
        System.setProperty(BgeeProperties.WEBPAGES_CACHE_CONFIG_FILE_NAME_KEY, "cacheConfigFileName");
        
        // get the instance of bgeeproperties and check the values
        this.bgeeProp = BgeeProperties.getBgeeProperties();
        assertEquals("Wrong property value retrieved", "/file", bgeeProp.getBgeeRootDirectory());
        assertEquals("Wrong property value retrieved", 30, bgeeProp.getUrlMaxLength());
        assertEquals("Wrong property value retrieved",
                "/requestParamStorDir", bgeeProp.getRequestParametersStorageDirectory());
        assertEquals("Wrong property value retrieved", 
                "/downRootDir", bgeeProp.getDownloadRootDirectory());
        assertEquals("Wrong property value retrieved", 
                "/downExprFileDir", bgeeProp.getDownloadExprFilesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                "/downDiffExprFileDir", bgeeProp.getDownloadDiffExprFilesRootDirectory());
        assertEquals("Wrong property value retrieved",
                "/jsFilesRootDir", bgeeProp.getJavascriptFilesRootDirectory());
        assertEquals("Wrong property value retrieved",
                "-extension-js-1", bgeeProp.getJavascriptVersionExtension());
        assertEquals("Wrong property value retrieved",
                "/cssFileRootDir", bgeeProp.getCssFilesRootDirectory());
        assertEquals("Wrong property value retrieved",
                "-extension-css-1", bgeeProp.getCssVersionExtension());
        assertEquals("Wrong property value retrieved",
                "/imgRootDir", bgeeProp.getImagesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                "/topOboDir", bgeeProp.getTopOBOResultsUrlRootDirectory());
        assertEquals("Wrong property value retrieved",
                "cacheConfigFileName", bgeeProp.getWebpagesCacheConfigFileName());

    }
}
