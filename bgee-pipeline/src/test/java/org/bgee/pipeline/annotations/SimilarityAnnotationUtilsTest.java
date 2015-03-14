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
import org.bgee.pipeline.annotations.SimilarityAnnotationUtils.SummaryAnnotationBean;
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
                        //single evidence
                        new RawAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                                2759, "Eukaryota", false, "ECO:0000033", 
                                "traceable author statement", 
                                "CIO:0000003", "high confidence from single evidence", 
                                "DOI:10.1073/pnas.032658599", "ref title 1", 
                                "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21")), 
                        //congruent evidence lines
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
                        //single evidence
                        new RawAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("CL:0000037"), Arrays.asList("hematopoietic stem cell"), 
                                7742, "Vertebrata", true, "ECO:0000067", 
                                "developmental similarity evidence", 
                                "CIO:0000004", "medium confidence from single evidence", 
                                "DOI:10.1146/annurev.cellbio.22.010605.093317", "ref title 4", 
                                "supporting text 4", "bgee", "ANN", sdf.parse("2013-07-01")), 
                        //multiple Uberon IDs
                        new RawAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("CL:0000037", "UBERON:0000001", "UBERON:0000007"), 
                                Arrays.asList("hematopoietic stem cell", "whatever name1", 
                                        "whatever name2"), 
                                7742, "Vertebrata", true, "ECO:0000067", 
                                "developmental similarity evidence", 
                                "CIO:0000004", "medium confidence from single evidence", 
                                "DOI:10.1146/annurev.cellbio.22.010605.093317", "ref title 7", 
                                "supporting text 7", "bgee", "ANN", sdf.parse("2013-07-01")), 
                        //case of independent evolution
                        new RawAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("UBERON:0010207"), 
                                Arrays.asList("nictitating membrane"), 
                                7776, "Gnathostomata", true, "ECO:0000034", 
                                "non-traceable author statement", 
                                "CIO:0000003", "high confidence from single evidence", 
                                "ISBN:978-0030223693", "ref title 8", 
                                "supporting text 8", "bgee", "ANN", sdf.parse("2013-09-05")), 
                        new RawAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("UBERON:0010207"), 
                                Arrays.asList("nictitating membrane"), 
                                7778, "Elasmobranchii", false, "ECO:0000034", 
                                "non-traceable author statement", 
                                "CIO:0000003", "high confidence from single evidence", 
                                "ISBN:978-0030223693", "ref title 8", 
                                "supporting text 8", "bgee", "ANN", sdf.parse("2013-09-05")), 
                        new RawAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("UBERON:0010207"), 
                                Arrays.asList("nictitating membrane"), 
                                32524, "Amniota", false, "ECO:0000034", 
                                "non-traceable author statement", 
                                "CIO:0000003", "high confidence from single evidence", 
                                "ISBN:978-0030223693", "ref title 8", 
                                "supporting text 8", "bgee", "ANN", sdf.parse("2013-09-05")),
                        //heritance of positive annotations, conflicting annotation 
                        //with positive parent annotations
                        new RawAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("CL:0000216"), Arrays.asList("Sertoli cell"), 
                                7711, "Chordata", false, "ECO:0000067", 
                                "developmental similarity evidence", 
                                "CIO:0000005", "low confidence from single evidence", 
                                "http://f50006a.eos-intl.net/ELIBSQL12_F50006A_Documents/93grier.pdf", 
                                "ref title 9", 
                                "supporting text 9", "bgee", "ANN", sdf.parse("2015-02-03")), 
                        new RawAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("CL:0000216"), Arrays.asList("Sertoli cell"), 
                                7742, "Vertebrata", false, "ECO:0000355", 
                                "phylogenetic distribution evidence", 
                                "CIO:0000004", "medium confidence from single evidence", 
                                "ISBN:978-0125449045", "ref title 10", 
                                "supporting text 10", "bgee", "ANN", sdf.parse("2015-02-03")), 
                        new RawAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("CL:0000216"), Arrays.asList("Sertoli cell"), 
                                32524, "Amniota", false, "ECO:0000067", 
                                "developmental similarity evidence", 
                                "CIO:0000004", "medium confidence from single evidence", 
                                "DOI:10.1002/jemt.1070320602", "ref title 11", 
                                "supporting text 11", "bgee", "ANN", sdf.parse("2015-02-03")), 
                        new RawAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("CL:0000216"), Arrays.asList("Sertoli cell"), 
                                32524, "Amniota", true, "ECO:0000067", 
                                "developmental similarity evidence", 
                                "CIO:0000004", "medium confidence from single evidence", 
                                "PMID:17026980", "ref title 12", 
                                "supporting text 12", "bgee", "ANN", sdf.parse("2013-08-30")), 
                        //one low confidence annotation against 2 medium confidence 
                        //annotations => weakly conflicting
                        new RawAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("UBERON:0000926"), Arrays.asList("mesoderm"), 
                                33213, "Bilateria", false, "ECO:0000067", 
                                "developmental similarity evidence", 
                                "CIO:0000004", "medium confidence from single evidence", 
                                "PMID:24281726", "ref title 13", 
                                "supporting text 13", "bgee", "ANN", sdf.parse("2014-01-13")), 
                        new RawAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("UBERON:0000926"), Arrays.asList("mesoderm"), 
                                33213, "Bilateria", false, "ECO:0000067", 
                                "developmental similarity evidence", 
                                "CIO:0000004", "medium confidence from single evidence", 
                                "PMID:22431747", "ref title 14", 
                                "supporting text 14", "bgee", "ANN", sdf.parse("2013-07-10")), 
                        new RawAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("UBERON:0000926"), Arrays.asList("mesoderm"), 
                                33213, "Bilateria", true, "ECO:0000067", 
                                "developmental similarity evidence", 
                                "CIO:0000005", "low confidence from single evidence", 
                                "PMID:12459924", "ref title 15", 
                                "supporting text 15", "bgee", "ANN", sdf.parse("2013-07-10")), 
                        //simply strongly conflicting
                        new RawAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("UBERON:0001245"), Arrays.asList("anus"), 
                                33213, "Bilateria", false, "ECO:0000355", 
                                "phylogenetic distribution evidence", 
                                "CIO:0000004", "medium confidence from single evidence", 
                                "http://dpc.uba.uva.nl/ctz/vol73/nr01/art01", "ref title 16", 
                                "supporting text 16", "bgee", "ANN", sdf.parse("2013-10-08")), 
                        new RawAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("UBERON:0001245"), Arrays.asList("anus"), 
                                33213, "Bilateria", true, "ECO:0000033", 
                                "traceable author statement", 
                                "CIO:0000004", "medium confidence from single evidence", 
                                "http://dpc.uba.uva.nl/ctz/vol73/nr01/art01", "ref title 16", 
                                "supporting text 16", "bgee", "ANN", sdf.parse("2013-10-08"))), 
                SimilarityAnnotationUtils.extractRawAnnotations(
                        SimilarityAnnotationUtilsTest.class.
                        getResource("/annotations/raw_similarity_annotations.tsv").getFile()));
        
        log.exit();
    }
    

    /**
     * Test {@link SimilarityAnnotationUtils#extractSummaryAnnotations(String)}.
     */
    @Test
    public void shouldExtractSummaryAnnotations() throws FileNotFoundException, 
        IllegalArgumentException, IOException {
        log.entry();
        
        assertEquals("Incorrect AGGREGATED EVIDENCE annotations retrieved", 
                Arrays.asList(
                        new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                                2759, "Eukaryota", false, 
                                "CIO:0000003", "high confidence from single evidence", 
                                1, 0, true, 
                                Arrays.asList("ECO:0000033"), 
                                Arrays.asList("traceable author statement"), 
                                null, null, null, null, 
                                Arrays.asList("bgee")), 
                        new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("CL:0000015"), Arrays.asList("male germ cell"), 
                                33208, "Metazoa", false, 
                                "CIO:0000019", "confidence statement from congruent evidence "
                                        + "lines of same type, overall confidence medium", 
                                2, 0, true, 
                                Arrays.asList("ECO:0000205"), 
                                Arrays.asList("curator inference"), 
                                null, null, null, null, 
                                Arrays.asList("bgee", "test db")), 
                        new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("CL:0000037"), Arrays.asList("hematopoietic stem cell"), 
                                7742, "Vertebrata", true, 
                                "CIO:0000004", "medium confidence from single evidence", 
                                0, 1, true, 
                                null, null, 
                                Arrays.asList("ECO:0000067"), 
                                Arrays.asList("developmental similarity evidence"), 
                                null, null, 
                                Arrays.asList("bgee")), 
                        new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("CL:0000037", "UBERON:0000001", "UBERON:0000007"), 
                                Arrays.asList("hematopoietic stem cell", "whatever name1", 
                                        "whatever name2"), 
                                7742, "Vertebrata", true, 
                                "CIO:0000004", "medium confidence from single evidence", 
                                0, 1, true, 
                                null, null, 
                                Arrays.asList("ECO:0000067"), 
                                Arrays.asList("developmental similarity evidence"), 
                                null, null, 
                                Arrays.asList("bgee")), 
                        new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("UBERON:0010207"), Arrays.asList("nictitating membrane"), 
                                7776, "Gnathostomata", true, 
                                "CIO:0000003", "high confidence from single evidence", 
                                0, 1, true, 
                                null, null, 
                                Arrays.asList("ECO:0000034"), 
                                Arrays.asList("non-traceable author statement"), 
                                null, null, 
                                Arrays.asList("bgee")), 
                        new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("UBERON:0010207"), Arrays.asList("nictitating membrane"), 
                                7778, "Elasmobranchii", false, 
                                "CIO:0000003", "high confidence from single evidence", 
                                1, 0, true, 
                                Arrays.asList("ECO:0000034"), 
                                Arrays.asList("non-traceable author statement"), 
                                null, null, null, null, 
                                Arrays.asList("bgee")), 
                        new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("UBERON:0010207"), Arrays.asList("nictitating membrane"), 
                                32524, "Amniota", false, 
                                "CIO:0000003", "high confidence from single evidence", 
                                1, 0, true, 
                                Arrays.asList("ECO:0000034"), 
                                Arrays.asList("non-traceable author statement"), 
                                null, null, null, null, 
                                Arrays.asList("bgee")), 
                        new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("CL:0000216"), Arrays.asList("Sertoli cell"), 
                                7711, "Chordata", false, 
                                "CIO:0000005", "low confidence from single evidence", 
                                1, 0, false, 
                                Arrays.asList("ECO:0000067"), 
                                Arrays.asList("developmental similarity evidence"), 
                                null, null, null, null, 
                                Arrays.asList("bgee")), 
                        new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("CL:0000216"), Arrays.asList("Sertoli cell"), 
                                7742, "Vertebrata", false, 
                                "CIO:0000013", "confidence statement from congruent evidence "
                                        + "lines of multiple types, overall confidence medium", 
                                2, 0, true, 
                                Arrays.asList("ECO:0000067", "ECO:0000355"), 
                                Arrays.asList("developmental similarity evidence", 
                                        "phylogenetic distribution evidence"), 
                                null, null, 
                                Arrays.asList(7711), Arrays.asList("Chordata"), 
                                Arrays.asList("bgee")), 
                        new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("CL:0000216"), Arrays.asList("Sertoli cell"), 
                                32524, "Amniota", false, 
                                "CIO:0000027", "confidence statement from weakly conflicting "
                                + "evidence lines of multiple types, overall confidence medium", 
                                3, 1, true, 
                                Arrays.asList("ECO:0000067", "ECO:0000355"), 
                                Arrays.asList("developmental similarity evidence", 
                                        "phylogenetic distribution evidence"), 
                                Arrays.asList("ECO:0000067"), 
                                Arrays.asList("developmental similarity evidence"), 
                                Arrays.asList(7711, 7742), Arrays.asList("Chordata", "Vertebrata"), 
                                Arrays.asList("bgee")), 
                        new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("UBERON:0000926"), Arrays.asList("mesoderm"), 
                                33213, "Bilateria", false, 
                                "CIO:0000024", "confidence statement from weakly conflicting "
                                    + "evidence lines of same type, overall confidence medium", 
                                2, 1, true, 
                                Arrays.asList("ECO:0000067"), 
                                Arrays.asList("developmental similarity evidence"), 
                                Arrays.asList("ECO:0000067"), 
                                Arrays.asList("developmental similarity evidence"), 
                                null, null, 
                                Arrays.asList("bgee")), 
                        new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("UBERON:0001245"), Arrays.asList("anus"), 
                                33213, "Bilateria", false, 
                                "CIO:0000010", "confidence statement from strongly conflicting "
                                        + "evidence lines of multiple types", 
                                1, 1, false, 
                                Arrays.asList("ECO:0000355"), 
                                Arrays.asList("phylogenetic distribution evidence"), 
                                Arrays.asList("ECO:0000033"), 
                                Arrays.asList("traceable author statement"), 
                                null, null, 
                                Arrays.asList("bgee"))), 
                SimilarityAnnotationUtils.extractSummaryAnnotations(
                        SimilarityAnnotationUtilsTest.class.
                        getResource("/annotations/summary_similarity_annotations.tsv").getFile()));

        log.exit();
    }
}
