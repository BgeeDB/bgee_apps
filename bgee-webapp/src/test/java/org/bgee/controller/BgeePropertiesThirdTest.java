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
 * @version Bgee 13
 * @since Bgee 13
 * @see BgeePropertiesParentTest
 * @see BgeePropertiesFirstTest
 * @see BgeePropertiesSecondTest
 * @see BgeePropertiesThirdTest
 * @see BgeePropertiesFourthTest
 */
public class BgeePropertiesThirdTest extends BgeePropertiesParentTest {

    /**
     * Test that the {@code java.util.Properties} are read from the system properties
     */
    @Test
    public void testLoadSystemProperties(){
        // get the instance of bgeeproperties and check the values
        this.bgeeProp = BgeeProperties.getBgeeProperties();
        assertEquals("Wrong property value retrieved", "/system", bgeeProp.getBgeeRootDirectory());
        assertEquals("Wrong property value retrieved", "30", bgeeProp.getUrlMaxLength().toString());
        assertEquals("Wrong property value retrieved",
                "/requestParamStorDir", bgeeProp.getRequestParametersStorageDirectory());
        assertEquals("Wrong property value retrieved", 
                "/downRootDir", bgeeProp.getDownloadRootDirectory());
        assertEquals("Wrong property value retrieved",
                "/jsFilesRootDir", bgeeProp.getJavascriptFilesRootDirectory());
        assertEquals("Wrong property value retrieved",
                "/cssFileRootDir", bgeeProp.getCssFilesRootDirectory());
        assertEquals("Wrong property value retrieved",
                "/imgRootDir", bgeeProp.getImagesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                "/topOboDir", bgeeProp.getTopOBOResultsUrlRootDirectory());
        assertEquals("Wrong property value retrieved",
                "cacheConfigFileName", bgeeProp.getWebpagesCacheConfigFileName());

    }
}
