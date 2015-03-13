package org.bgee.pipeline.annotations;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.annotations.SimilarityAnnotationUtils.RawAnnotationBean;
import org.junit.Test;

/**
 * Unit tests for {@link SimilarityAnnotationUtils}. This class does not use the usual 
 * Bgee utils, to be provided as code example. The dependencies for this class are: 
 * Super CSV, OWLTools, OWLAPI, Log4j2, Junit.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Mar. 2015
 * @since Bgee 13
 */
public class SimilarityAnnotationUtilsTest {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(SimilarityAnnotationUtilsTest.class.getName());
    
    /**
     * Default Constructor. 
     */
    public SimilarityAnnotationUtilsTest() {
    }
    
    /**
     * Test {@link SimilarityAnnotationUtils#extractRawAnnotations(String)}.
     */
    @Test
    public void shouldExtractRawAnnotations() throws FileNotFoundException, 
        IllegalArgumentException, IOException {
        log.entry();
        
        assertEquals("Incorrect RAW annotations retrieved", 
                Arrays.asList(
                        new RawAnnotationBean()), 
                SimilarityAnnotationUtils.extractRawAnnotations(
                        SimilarityAnnotationUtilsTest.class.
                        getResource("/annotations/raw_similarity_annotations.tsv").getFile()));
        
        log.exit();
    }
}
