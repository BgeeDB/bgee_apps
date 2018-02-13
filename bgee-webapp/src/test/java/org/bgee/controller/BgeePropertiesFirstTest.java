package org.bgee.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
public class BgeePropertiesFirstTest extends BgeePropertiesParentTest {

    /**
     * Test that the injected {@code java.util.Properties} are used
     */
    @Test
    public void testInjectedProperties(){
        // set the properties to inject
        Properties prop = new Properties();
        prop.put(BgeeProperties.MINIFY_KEY, "true");
        prop.put(BgeeProperties.WARNING_MESSAGE_KEY, "/injectedwarning");
        prop.put(BgeeProperties.ARCHIVE_KEY, "true");
        prop.put(BgeeProperties.BGEE_CURRENT_URL_KEY, "/injectedcurrenturl");
        prop.put(BgeeProperties.BGEE_ROOT_DIRECTORY_KEY, "/injectedroot");
        prop.put(BgeeProperties.URL_MAX_LENGTH_KEY, "10");
        prop.put(BgeeProperties.CSS_FILES_ROOT_DIRECTORY_KEY, "/injectedcss");
        prop.put(BgeeProperties.CSS_VERSION_EXTENSION_KEY, "injectedCssVersion");
        prop.put(BgeeProperties.FTP_ROOT_DIRECTORY_KEY, "/injectedftp");
        prop.put(BgeeProperties.DOWNLOAD_ROOT_DIRECTORY_KEY, "/injecteddownload");
        prop.put(BgeeProperties.DOWNLOAD_EXPR_FILES_ROOT_DIRECTORY_KEY, "/injectedexprfiles");
        prop.put(BgeeProperties.DOWNLOAD_DIFF_EXPR_FILES_ROOT_DIRECTORY_KEY, "/injecteddiffexprfiles");
        prop.put(BgeeProperties.DOWNLOAD_MULTI_DIFF_EXPR_FILES_ROOT_DIRECTORY_KEY, 
                "/injectedmultidiffexprfiles");
        prop.put(BgeeProperties.DOWNLOAD_ORTHOLOG_FILES_ROOT_DIRECTORY_KEY, "/injectedorthologfiles");
        prop.put(BgeeProperties.DOWNLOAD_AFFY_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_KEY, 
                "/injectedaffyprocvaluefiles");
        prop.put(BgeeProperties.DOWNLOAD_RNA_SEQ_PROC_EXPR_VALUE_FILES_ROOT_DIRECTORY_KEY, 
                "/injectedrnaseqprocvaluefiles");
        prop.put(BgeeProperties.IMAGES_ROOT_DIRECTORY_KEY, "/injectedimg");
        prop.put(BgeeProperties.LOGO_IMAGES_ROOT_DIRECTORY_KEY, "/injectedlogoimg");
        prop.put(BgeeProperties.SPECIES_IMAGES_ROOT_DIRECTORY_KEY, "/injectedspeciesimg");
        prop.put(BgeeProperties.JAVASCRIPT_FILES_ROOT_DIRECTORY_KEY, "/injectedjs");
        prop.put(BgeeProperties.JAVASCRIPT_VERSION_EXTENSION_KEY, "injectedJsVersion");
        prop.put(BgeeProperties.REQUEST_PARAMETERS_STORAGE_DIRECTORY_KEY, "/injectedrequestparam");
        prop.put(BgeeProperties.WEBPAGES_CACHE_CONFIG_FILE_NAME_KEY, "cache");
        prop.put(BgeeProperties.TOP_ANAT_RESULTS_URL_DIRECTORY_KEY, "/injectedurldir");
        // Test a superclass property
        prop.put(BgeeProperties.TOP_ANAT_FUNCTION_FILE_KEY, "/injectedfunctionfile");

        // get the instance of bgeeproperties and check the values
        this.bgeeProp = BgeeProperties.getBgeeProperties(prop);
        assertEquals("Wrong property value retrieved", true, bgeeProp.isMinify());
        assertEquals("Wrong property value retrieved", "/injectedwarning", bgeeProp.getWarningMessage());
        assertEquals("Wrong property value retrieved", true, bgeeProp.isArchive());
        assertEquals("Wrong property value retrieved", "/injectedcurrenturl", bgeeProp.getBgeeCurrentUrl());
        assertEquals("Wrong property value retrieved","/injectedroot",bgeeProp.getBgeeRootDirectory());
        assertEquals("Wrong property value retrieved",10,bgeeProp.getUrlMaxLength());
        assertEquals("Wrong property value retrieved", 
                "/injectedcss", bgeeProp.getCssFilesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                "injectedCssVersion", bgeeProp.getCssVersionExtension());
        assertEquals("Wrong property value retrieved", 
                "/injectedftp", bgeeProp.getFTPRootDirectory());
        assertEquals("Wrong property value retrieved", 
                "/injecteddownload", bgeeProp.getDownloadRootDirectory());
        assertEquals("Wrong property value retrieved", 
                "/injectedexprfiles", bgeeProp.getDownloadExprFilesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                "/injecteddiffexprfiles", bgeeProp.getDownloadDiffExprFilesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                "/injectedmultidiffexprfiles", bgeeProp.getDownloadMultiDiffExprFilesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                "/injectedorthologfiles", bgeeProp.getDownloadOrthologFilesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                "/injectedaffyprocvaluefiles", bgeeProp.getDownloadAffyProcExprValueFilesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                "/injectedrnaseqprocvaluefiles", bgeeProp.getDownloadRNASeqProcExprValueFilesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                "/injectedimg", bgeeProp.getImagesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                "/injectedlogoimg", bgeeProp.getLogoImagesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                "/injectedspeciesimg", bgeeProp.getSpeciesImagesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                "/injectedjs", bgeeProp.getJavascriptFilesRootDirectory());
        assertEquals("Wrong property value retrieved", 
                "injectedJsVersion", bgeeProp.getJavascriptVersionExtension());
        assertEquals("Wrong property value retrieved", 
                "/injectedrequestparam", bgeeProp.getRequestParametersStorageDirectory());
        assertEquals("Wrong property value retrieved", 
                "cache", bgeeProp.getWebpagesCacheConfigFileName());
        assertEquals("Wrong property value retrieved", 
                "/injectedurldir", bgeeProp.getTopAnatResultsUrlDirectory());
        // Test a superclass property
        assertEquals("Wrong property value retrieved", 
                "/injectedfunctionfile", bgeeProp.getTopAnatFunctionFile());
    }

    /**
     * Test that the returned {@code BgeeProperties} instance is always the same within the
     * same thread but different between two threads
     * @throws InterruptedException 
     * @throws ExecutionException 
     */
    @Test
    public void testOnePropertiesPerThread() throws InterruptedException, ExecutionException{

        /**
         * An anonymous class to acquire {@code BgeeProperties}s 
         * from a different thread than this one, 
         * and to be run alternatively to the main thread.
         */
        class ThreadTest implements Callable<Boolean> {

            public BgeeProperties bgeeProp3;
            /**
             * An {@code Exchanger} that will be used to run threads alternatively. 
             */
            public final Exchanger<Integer> exchanger = new Exchanger<Integer>();
            @Override
            public Boolean call() throws InterruptedException{
                try{
                    bgeeProp3 = BgeeProperties.getBgeeProperties();
                    return true;
                } finally {
                    //whatever happens, make sure to re-launch the main thread, 
                    //as we do not use an Executor that might catch the Exception 
                    //and interrupt the other Thread. 
                    this.exchanger.exchange(null);
                }
            }
        }

        // Get two BgeeProperties in the main thread and check that it is the same instance
        BgeeProperties bgeeProp1 = BgeeProperties.getBgeeProperties();
        BgeeProperties bgeeProp2 = BgeeProperties.getBgeeProperties();
        assertSame("The two objects are not the same but they should be",
                bgeeProp1, bgeeProp2);

        //launch a second thread also acquiring BgeeProperties
        ThreadTest test = new ThreadTest();
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Future<Boolean> future = executorService.submit(test);
        //wait for this thread's turn
        test.exchanger.exchange(null);
        //check that no exception was thrown in the second thread.
        //In that case, it would be completed and calling get would throw 
        //the exception. 
        if (future.isDone()) {
            future.get();
        }
        assertNotSame("The two objects are the same but they should not be",
                bgeeProp1, test.bgeeProp3);

    }

}
