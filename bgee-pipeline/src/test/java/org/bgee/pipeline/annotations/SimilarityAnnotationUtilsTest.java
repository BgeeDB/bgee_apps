package org.bgee.pipeline.annotations;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
        IllegalArgumentException, IOException, ParseException {
        log.entry();
        
        SimpleDateFormat sdf = new SimpleDateFormat(SimilarityAnnotationUtils.DATE_FORMAT);
        assertEquals("Incorrect RAW annotations retrieved", 
                Arrays.asList(
                        new RawAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                                2759, "Eukaryota", false, "ECO:0000033", 
                                "traceable author statement", 
                                "CIO:0000003", "high confidence from single evidence", 
                                "DOI:10.1073/pnas.032658599", "ref title 1", 
                                "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21")), 
                        new RawAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("CL:0000015"), Arrays.asList("male germ cell"), 
                                33208, "Metazoa", false, "ECO:0000205", 
                                "curator inference", 
                                "CIO:0000004", "medium confidence from single evidence", 
                                "DOI:10.1002/bies.950161213", "ref title 2", 
                                "supporting text 2", "bgee", "ANN", sdf.parse("2013-08-29")), 
                        new RawAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("CL:0000015"), Arrays.asList("male germ cell"), 
                                33208, "Metazoa", false, "ECO:0000205", 
                                "curator inference", 
                                "CIO:0000005", "low confidence from single evidence", 
                                "ISBN:978-0198566694", "ref title 3", 
                                "supporting text 3", "bgee", "ANN", sdf.parse("2013-08-29")), 
                        new RawAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("CL:0000037"), Arrays.asList("hematopoietic stem cell"), 
                                7742, "Vertebrata", true, "ECO:0000067", 
                                "developmental similarity evidence", 
                                "CIO:0000004", "medium confidence from single evidence", 
                                "DOI:10.1146/annurev.cellbio.22.010605.093317", "ref title 4", 
                                "supporting text 4", "bgee", "ANN", sdf.parse("2013-07-01")), 
                        new RawAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("CL:0000216"), Arrays.asList("Sertoli cell"), 
                                32524, "Amniota", false, "ECO:0000067", 
                                "developmental similarity evidence", 
                                "CIO:0000004", "medium confidence from single evidence", 
                                "DOI:10.1002/jemt.1070320602", "ref title 5", 
                                "supporting text 5", "bgee", "ANN", sdf.parse("2015-02-03")), 
                        new RawAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("CL:0000216"), Arrays.asList("Sertoli cell"), 
                                32524, "Amniota", true, "ECO:0000067", 
                                "developmental similarity evidence", 
                                "CIO:0000004", "medium confidence from single evidence", 
                                "PMID:17026980", "ref title 6", 
                                "supporting text 6", "bgee", "ANN", sdf.parse("2013-08-30")), 
                        new RawAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("CL:0000037", "UBERON:0000001", "UBERON:0000007"), 
                                Arrays.asList("hematopoietic stem cell", "whatever name1", 
                                        "whatever name2"), 
                                7742, "Vertebrata", true, "ECO:0000067", 
                                "developmental similarity evidence", 
                                "CIO:0000004", "medium confidence from single evidence", 
                                "DOI:10.1146/annurev.cellbio.22.010605.093317", "ref title 7", 
                                "supporting text 7", "bgee", "ANN", sdf.parse("2013-07-01"))), 
                SimilarityAnnotationUtils.extractRawAnnotations(
                        SimilarityAnnotationUtilsTest.class.
                        getResource("/annotations/raw_similarity_annotations.tsv").getFile()));
        
        log.exit();
    }
}
