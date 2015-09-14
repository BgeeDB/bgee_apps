package org.bgee.controller;

import static org.junit.Assert.assertEquals;

import org.bgee.controller.BgeeProperties;
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
        // Test a superclass property
        System.clearProperty(BgeeProperties.TOP_ANAT_FUNCTION_FILE_KEY);
        // Also, set the properties file to an non-existing file, 
        // so that no property file is used (otherwise, property files in src/test/resources/ 
        // or src/main/resources/ would be used).
        System.setProperty(BgeeProperties.PROPERTIES_FILE_NAME_KEY, "/none");

        // get the instance of bgeeproperties and check the values
        this.bgeeProp = BgeeProperties.getBgeeProperties();
        assertEquals("Wrong property value retrieved", 
                BgeeProperties.BGEE_ROOT_DIRECTORY_DEFAULT, bgeeProp.getBgeeRootDirectory());
        assertEquals("Wrong property value retrieved", BgeeProperties.URL_MAX_LENGTH_DEFAULT, 
                bgeeProp.getUrlMaxLength());
        assertEquals("Wrong property value retrieved", 
                BgeeProperties.REQUEST_PARAMETERS_STORAGE_DIRECTORY_DEFAULT, 
                bgeeProp.getRequestParametersStorageDirectory());
        assertEquals("Wrong property value retrieved", 
                BgeeProperties.FTP_ROOT_DIRECTORY_DEFAULT, 
                bgeeProp.getFTPRootDirectory());
        assertEquals("Wrong property value retrieved", 
                BgeeProperties.DOWNLOAD_ROOT_DIRECTORY_DEFAULT, 
                bgeeProp.getDownloadRootDirectory());
        assertEquals("Wrong property value retrieved", 
                BgeeProperties.DOWNLOAD_EXPR_FILES_ROOT_DIRECTORY_DEFAULT, 
                bgeeProp.getDownloadExprFilesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                BgeeProperties.DOWNLOAD_DIFF_EXPR_FILES_ROOT_DIRECTORY_DEFAULT, 
                bgeeProp.getDownloadDiffExprFilesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                BgeeProperties.DOWNLOAD_MULTI_DIFF_EXPR_FILES_ROOT_DIRECTORY_DEFAULT, 
                bgeeProp.getDownloadMultiDiffExprFilesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                BgeeProperties.DOWNLOAD_ORTHOLOG_FILES_ROOT_DIRECTORY_DEFAULT, 
                bgeeProp.getDownloadOrthologFilesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                BgeeProperties.DOWNLOAD_AFFY_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_DEFAULT, 
                bgeeProp.getDownloadAffyProcExprValueFilesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                BgeeProperties.DOWNLOAD_RNA_SEQ_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_DEFAULT, 
                bgeeProp.getDownloadRNASeqProcExprValueFilesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                BgeeProperties.JAVASCRIPT_FILES_ROOT_DIRECTORY_DEFAULT, 
                bgeeProp.getJavascriptFilesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                BgeeProperties.JAVASCRIPT_VERSION_EXTENSION_DEFAULT, 
                bgeeProp.getJavascriptVersionExtension());
        assertEquals("Wrong property value retrieved", 
                BgeeProperties.CSS_FILES_ROOT_DIRECTORY_DEFAULT, 
                bgeeProp.getCssFilesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                BgeeProperties.CSS_VERSION_EXTENSION_DEFAULT, 
                bgeeProp.getCssVersionExtension());
        assertEquals("Wrong property value retrieved", 
                BgeeProperties.IMAGES_ROOT_DIRECTORY_DEFAULT, 
                bgeeProp.getImagesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                BgeeProperties.LOGO_IMAGES_ROOT_DIRECTORY_DEFAULT, 
                bgeeProp.getLogoImagesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                BgeeProperties.SPECIES_IMAGES_ROOT_DIRECTORY_DEFAULT, 
                bgeeProp.getSpeciesImagesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                BgeeProperties.WEBPAGES_CACHE_CONFIG_FILE_NAME_DEFAULT, 
                bgeeProp.getWebpagesCacheConfigFileName());
        assertEquals("Wrong property value retrieved",
                BgeeProperties.TOP_ANAT_RESULTS_URL_DIRECTORY_DEFAULT,
                bgeeProp.getTopAnatResultsUrlDirectory());
        // Test a superclass property
        assertEquals("Wrong property value retrieved", 
                BgeeProperties.TOP_ANAT_FUNCTION_FILE_DEFAULT, bgeeProp.getTopAnatFunctionFile());
        
    }
}
