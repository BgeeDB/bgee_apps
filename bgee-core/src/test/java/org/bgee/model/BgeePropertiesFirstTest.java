package org.bgee.model;

import static org.junit.Assert.*;

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
 * @version Bgee 14, Mar. 2019
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
        prop.put(BgeeProperties.MAJOR_VERSION_KEY, "1");
        prop.put(BgeeProperties.MINOR_VERSION_KEY, "0");
        prop.put(BgeeProperties.BGEE_SEARCH_SERVER_URL_KEY, "/injectedsphinxurl");
        prop.put(BgeeProperties.BGEE_SEARCH_SERVER_PORT_KEY, "/injectedsphinxport");
        prop.put(BgeeProperties.BIOCONDUCTOR_RELEASE_NUMBER_KEY, "injectedbioconductor");
        prop.put(BgeeProperties.TOP_ANAT_R_SCRIPT_EXECUTABLE_KEY, "/injectedrexec");
        prop.put(BgeeProperties.TOP_ANAT_R_WORKING_DIRECTORY_KEY, "/injectedrwd");
        prop.put(BgeeProperties.TOP_ANAT_FUNCTION_FILE_KEY, "/injectedfunctionfile");
        prop.put(BgeeProperties.TOP_ANAT_RESULTS_WRITING_DIRECTORY_KEY, "/injectedwd");
        prop.put(BgeeProperties.MAX_JOB_COUNT_PER_USER_KEY, 5);

        // get the instance of bgeeproperties and check the values
        this.bgeeProp = BgeeProperties.getBgeeProperties(prop);
        assertEquals("Wrong property value retrieved","1",
                bgeeProp.getMajorVersion());
        assertEquals("Wrong property value retrieved","0",
                bgeeProp.getMinorVersion());
        assertEquals("Wrong property value retrieved","/injectedsphinxurl",
                bgeeProp.getSearchServerURL());
        assertEquals("Wrong property value retrieved","/injectedsphinxport",
                bgeeProp.getSearchServerPort());
        assertEquals("Wrong property value retrieved","injectedbioconductor",
                bgeeProp.getBioconductorReleaseNumber());
        assertEquals("Wrong property value retrieved","/injectedrexec",
                bgeeProp.getTopAnatRScriptExecutable());
        assertEquals("Wrong property value retrieved","/injectedrwd",
                bgeeProp.getTopAnatRWorkingDirectory());
        assertEquals("Wrong property value retrieved", 
                "/injectedfunctionfile", bgeeProp.getTopAnatFunctionFile());
        assertEquals("Wrong property value retrieved", 
                "/injectedwd", bgeeProp.getTopAnatResultsWritingDirectory());
        assertEquals("Wrong property value retrieved", 
                5, bgeeProp.getMaxJobCountPerUser());
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
        future.get();
        assertNotSame("The two objects are the same but they should not be",
                bgeeProp1, test.bgeeProp3);
        
        //release the BgeeProperties one by one without calling releaseAll(), 
        //that would make other test to fail
        BgeeProperties.getBgeeProperties().release();
        test.bgeeProp3.release();

    }
    
    /**
    * test the behavior of {@link BgeeProperties.release()} and {@link BgeeProperties.isReleased()}.
    */
   @Test
   public void shouldReleaseBgeeProperties() {
       /**
        * An anonymous class to acquire {@code BgeeProperties}s 
        * from a different thread than this one, 
        * and to be run alternatively to the main thread.
        */
       class ThreadTest extends Thread {
           public volatile BgeeProperties prop1;
           public volatile BgeeProperties prop2;
           public volatile boolean firstReleaseReturn;
           public volatile boolean secondReleaseReturn;
           /**
            * An {@code Exchanger} that will be used to run threads alternatively. 
            */
           public final Exchanger<Integer> exchanger = new Exchanger<Integer>();
           
           @Override
           public void run() {
               try {
                   //acquire a BgeeProperties
                   prop1 = BgeeProperties.getBgeeProperties();
                   
                   //release it
                   firstReleaseReturn = prop1.release();
                   //calling it again should do nothing, store the value to check
                   secondReleaseReturn = prop1.release();
                   
                   //acquire a new BgeeProperties
                   prop2 = BgeeProperties.getBgeeProperties();
                   //main thread's turn
                   this.exchanger.exchange(null);
                   
               } catch (InterruptedException e) {
                   Thread.currentThread().interrupt();
               } 
           }
       }
       
       try {
           //get a BgeeProperties in the main thread
           BgeeProperties prop1 = BgeeProperties.getBgeeProperties();
           
           //release it, should return true
           assertTrue("A BgeeProperties was not correctly released", 
                   prop1.release());
           //calling it again should do nothing
           assertFalse("The returned value of release() is inaccurate", 
                   prop1.release());
           
           //acquire a new BgeeProperties
           BgeeProperties prop2 = BgeeProperties.getBgeeProperties();
           
           //launch a second thread also acquiring and releasing BgeeProperties
           ThreadTest test = new ThreadTest();
           test.start();
           //wait for this thread's turn
           test.exchanger.exchange(null);
           
           //test the returned value of the two calls to release() in the second thread
           assertTrue("A BgeeProperties was not correctly released in second thread", 
                   test.firstReleaseReturn);
           assertFalse("The returned value of release() is inaccurate in second thread", 
                   test.secondReleaseReturn);
  
           //the first BgeeProperties of the main thread and the second thread 
           //should be released
           assertTrue("A BgeeProperties was not correctly released in the main thread", 
                   prop1.isReleased());
           assertTrue("A BgeeProperties was not correctly released in the second thread", 
                   test.prop1.isReleased());
           
           //the second BgeeProperties of the main thread and the second thread 
           //should NOT be released
           assertFalse("A BgeeProperties should not have been released in the main thread", 
                   prop2.isReleased());
           assertFalse("A BgeeProperties should not have been released in the second thread", 
                   test.prop2.isReleased());
           
           //release the BgeeProperties one by one without calling releaseAll(), 
           //that would make other test to fail
           BgeeProperties.getBgeeProperties().release();
           test.prop2.release();
       } catch (InterruptedException e) {
           Thread.currentThread().interrupt();
       } 
   }

}
