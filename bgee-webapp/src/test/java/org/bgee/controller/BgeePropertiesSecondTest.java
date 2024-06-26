package org.bgee.controller;

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
 * @version Bgee 14, Feb. 2018
 * @since   Bgee 13
 * @see BgeePropertiesParentTest
 * @see BgeePropertiesFirstTest
 * @see BgeePropertiesSecondTest
 * @see BgeePropertiesThirdTest
 * @see BgeePropertiesFourthTest
 */
public class BgeePropertiesSecondTest extends BgeePropertiesParentTest {
    
    /**
     * Test that the {@code java.util.Properties} are read from the file
     */
    @Test
    public void testLoadPropertiesFromFile(){
        //set the file to use
        System.setProperty(BgeeProperties.PROPERTIES_FILE_NAME_KEY, "/test.properties");

        // get the instance of bgeeproperties and check the values
        this.bgeeProp = BgeeProperties.getBgeeProperties();
        assertEquals("Wrong property value retrieved", true, bgeeProp.isMinify());
        assertEquals("Wrong property value retrieved", "testwarning", bgeeProp.getWarningMessage());
        assertEquals("Wrong property value retrieved", true, bgeeProp.isArchive());
        assertEquals("Wrong property value retrieved", "testcurrenturl", bgeeProp.getBgeeCurrentUrl());
        assertEquals("Wrong property value retrieved", "frontendUrl", bgeeProp.getFrontendUrl());
        assertEquals("Wrong property value retrieved", "stableFrontendUrl", bgeeProp.getStableFrontendUrl());
        assertEquals("Wrong property value retrieved", "testcurrenturl", bgeeProp.getBgeeCurrentUrl());
        assertEquals("Wrong property value retrieved", "/file", bgeeProp.getBgeeRootDirectory());
        assertEquals("Wrong property value retrieved", 20, bgeeProp.getUrlMaxLength());
        assertEquals("Wrong property value retrieved",
                "/requestParametersStorageDirectory", bgeeProp.getRequestParametersStorageDirectory());
        assertEquals("Wrong property value retrieved", 
                "/ftpRootDirectory", bgeeProp.getFTPRootDirectory());
        assertEquals("Wrong property value retrieved", 
                "/downloadRootDirectory", bgeeProp.getDownloadRootDirectory());
        assertEquals("Wrong property value retrieved", 
                "/downloadExprFileDirectory", bgeeProp.getDownloadExprFilesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                "/downloadDiffExprFileDirectory", bgeeProp.getDownloadDiffExprFilesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                "/downloadMultiDiffExprFileDirectory", bgeeProp.getDownloadMultiDiffExprFilesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                "/downloadOrthologFileDirectory", bgeeProp.getDownloadOrthologFilesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                "/downloadAffyProcExprValueFileDirectory", bgeeProp.getDownloadAffyProcExprValueFilesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                "/downloadRNASeqProcExprValueFileDirectory", bgeeProp.getDownloadRNASeqProcExprValueFilesRootDirectory());
        assertEquals("Wrong property value retrieved",
                "/javascriptFilesRootDirectory", bgeeProp.getJavascriptFilesRootDirectory());
        assertEquals("Wrong property value retrieved",
                "-test-extension-1", bgeeProp.getJavascriptVersionExtension());
        assertEquals("Wrong property value retrieved",
                "/cssFilesRootDirectory", bgeeProp.getCssFilesRootDirectory());
        assertEquals("Wrong property value retrieved",
                "", bgeeProp.getCssVersionExtension());
        assertEquals("Wrong property value retrieved",
                "/imagesRootDirectory", bgeeProp.getImagesRootDirectory());
        assertEquals("Wrong property value retrieved",
                "/logoImagesRootDirectory", bgeeProp.getLogoImagesRootDirectory());
        assertEquals("Wrong property value retrieved",
                "/speciesImagesRootDirectory", bgeeProp.getSpeciesImagesRootDirectory());
        assertEquals("Wrong property value retrieved",
                "webpagescachefile", bgeeProp.getWebpagesCacheConfigFileName());
        assertEquals("Wrong property value retrieved", 
                "/fileurldir", bgeeProp.getTopAnatResultsUrlDirectory());
        // Test a superclass property
        assertEquals("Wrong property value retrieved", 
                "/filetopanatfunction", bgeeProp.getTopAnatFunctionFile());
    }
}
