package org.bgee.controller;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for {@link BgeeWebappProperties}.
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
        // Also, set the properties file to an non-existing file, 
        // so that no property file is used (otherwise, property files in src/test/resources/ 
        // or src/main/resources/ would be used).
        System.setProperty(BgeeWebappProperties.PROPERTIES_FILE_NAME_KEY, "/none");

        // get the instance of bgeeproperties and check the values
        this.bgeeProp = BgeeWebappProperties.getBgeeProperties();
        assertEquals("Wrong property value retrieved", 
                BgeeWebappProperties.BGEE_ROOT_DIRECTORY_DEFAULT, bgeeProp.getBgeeRootDirectory());
        assertEquals("Wrong property value retrieved", BgeeWebappProperties.URL_MAX_LENGTH_DEFAULT, 
                bgeeProp.getUrlMaxLength());
        assertEquals("Wrong property value retrieved", 
                BgeeWebappProperties.REQUEST_PARAMETERS_STORAGE_DIRECTORY_DEFAULT, 
                bgeeProp.getRequestParametersStorageDirectory());
        assertEquals("Wrong property value retrieved", 
                BgeeWebappProperties.FTP_ROOT_DIRECTORY_DEFAULT, 
                bgeeProp.getFTPRootDirectory());
        assertEquals("Wrong property value retrieved", 
                BgeeWebappProperties.DOWNLOAD_ROOT_DIRECTORY_DEFAULT, 
                bgeeProp.getDownloadRootDirectory());
        assertEquals("Wrong property value retrieved", 
                BgeeWebappProperties.DOWNLOAD_EXPR_FILES_ROOT_DIRECTORY_DEFAULT, 
                bgeeProp.getDownloadExprFilesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                BgeeWebappProperties.DOWNLOAD_DIFF_EXPR_FILES_ROOT_DIRECTORY_DEFAULT, 
                bgeeProp.getDownloadDiffExprFilesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                BgeeWebappProperties.DOWNLOAD_MULTI_DIFF_EXPR_FILES_ROOT_DIRECTORY_DEFAULT, 
                bgeeProp.getDownloadMultiDiffExprFilesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                BgeeWebappProperties.DOWNLOAD_ORTHOLOG_FILES_ROOT_DIRECTORY_DEFAULT, 
                bgeeProp.getDownloadOrthologFilesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                BgeeWebappProperties.DOWNLOAD_AFFY_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_DEFAULT, 
                bgeeProp.getDownloadAffyProcExprValueFilesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                BgeeWebappProperties.DOWNLOAD_RNA_SEQ_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_DEFAULT, 
                bgeeProp.getDownloadRNASeqProcExprValueFilesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                BgeeWebappProperties.JAVASCRIPT_FILES_ROOT_DIRECTORY_DEFAULT, 
                bgeeProp.getJavascriptFilesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                BgeeWebappProperties.JAVASCRIPT_VERSION_EXTENSION_DEFAULT, 
                bgeeProp.getJavascriptVersionExtension());
        assertEquals("Wrong property value retrieved", 
                BgeeWebappProperties.CSS_FILES_ROOT_DIRECTORY_DEFAULT, 
                bgeeProp.getCssFilesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                BgeeWebappProperties.CSS_VERSION_EXTENSION_DEFAULT, 
                bgeeProp.getCssVersionExtension());
        assertEquals("Wrong property value retrieved", 
                BgeeWebappProperties.IMAGES_ROOT_DIRECTORY_DEFAULT, 
                bgeeProp.getImagesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                BgeeWebappProperties.LOGO_IMAGES_ROOT_DIRECTORY_DEFAULT, 
                bgeeProp.getLogoImagesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                BgeeWebappProperties.SPECIES_IMAGES_ROOT_DIRECTORY_DEFAULT, 
                bgeeProp.getSpeciesImagesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                BgeeWebappProperties.WEBPAGES_CACHE_CONFIG_FILE_NAME_DEFAULT, 
                bgeeProp.getWebpagesCacheConfigFileName());
    }
}
