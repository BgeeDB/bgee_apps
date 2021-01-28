package org.bgee.pipeline.annotations;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.TestAncestor;
import org.bgee.pipeline.Utils;
import org.bgee.pipeline.annotations.SimilarityAnnotation;
import org.bgee.pipeline.annotations.SimilarityAnnotation.CuratorAnnotationBean;
import org.bgee.pipeline.annotations.SimilarityAnnotationUtils.RawAnnotationBean;
import org.bgee.pipeline.annotations.SimilarityAnnotationUtils.SummaryAnnotationBean;
import org.bgee.pipeline.annotations.SimilarityAnnotationUtils.AncestralTaxaAnnotationBean;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;

import owltools.graph.OWLGraphWrapper;
import owltools.io.ParserWrapper;

/**
 * Unit tests for {@link SimilarityAnnotation}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class SimilarityAnnotationTest extends TestAncestor {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(SimilarityAnnotationTest.class.getName());
    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder();
    
    /**
     * Default Constructor. 
     */
    public SimilarityAnnotationTest() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    

    /**
     * Test {@link SimilarityAnnotation#extractCuratorAnnotations(String)}.
     */
    @Test
    public void shouldExtractCuratorAnnotations() throws FileNotFoundException, 
        IllegalArgumentException, IOException, ParseException {
        log.entry();
        
        SimpleDateFormat sdf = new SimpleDateFormat(SimilarityAnnotationUtils.DATE_FORMAT);
        assertEquals("Incorrect CURATOR annotations retrieved", 
                Arrays.asList(
                        //single evidence
                        new CuratorAnnotationBean("HOM:0000007", Arrays.asList("CL:0000000"), 
                                2759, false, "ECO:0000033", "CIO:0000003", 
                                "DOI:10.1073/pnas.032658599", "ref title 1", 
                                "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21")), 
                        //congruent evidence lines
                        new CuratorAnnotationBean("HOM:0000007", Arrays.asList("CL:0000015"), 
                                33208, false, "ECO:0000205", "CIO:0000004", 
                                "DOI:10.1002/bies.950161213", "ref title 2", 
                                "supporting text 2", "bgee", "ANN", sdf.parse("2013-08-29")), 
                        new CuratorAnnotationBean("HOM:0000007", Arrays.asList("CL:0000015"),  
                                33208, false, "ECO:0000205", "CIO:0000005", 
                                "ISBN:978-0198566694", "ref title 3", 
                                "supporting text 3", "bgee", "ANN", sdf.parse("2013-08-29")), 
                        //single evidence
                        new CuratorAnnotationBean("HOM:0000007", Arrays.asList("CL:0000037"), 
                                7742, true, "ECO:0000067", "CIO:0000004", 
                                "DOI:10.1146/annurev.cellbio.22.010605.093317", "ref title 4", 
                                "supporting text 4", "bgee", "ANN", sdf.parse("2013-07-01")), 
                        //multiple Uberon IDs
                        new CuratorAnnotationBean("HOM:0000007", 
                                Arrays.asList("CL:0000037", "UBERON:0000001", "UBERON:0000007"), 
                                7742, true, "ECO:0000067", "CIO:0000004", 
                                "DOI:10.1146/annurev.cellbio.22.010605.093317", "ref title 7", 
                                "supporting text 7", "bgee", "ANN", sdf.parse("2013-07-01")), 
                        //case of independent evolution
                        new CuratorAnnotationBean("HOM:0000007", Arrays.asList("UBERON:0010207"), 
                                7776, true, "ECO:0000034", "CIO:0000003", 
                                "ISBN:978-0030223693", "ref title 8", 
                                "supporting text 8", "bgee", "ANN", sdf.parse("2013-09-05")), 
                        new CuratorAnnotationBean("HOM:0000007", Arrays.asList("UBERON:0010207"), 
                                7778, false, "ECO:0000034", "CIO:0000003", 
                                "ISBN:978-0030223693", "ref title 8", 
                                "supporting text 8", "bgee", "ANN", sdf.parse("2013-09-05")), 
                        new CuratorAnnotationBean("HOM:0000007", Arrays.asList("UBERON:0010207"), 
                                32524, false, "ECO:0000034", "CIO:0000003", 
                                "ISBN:978-0030223693", "ref title 8", 
                                "supporting text 8", "bgee", "ANN", sdf.parse("2013-09-05")),
                        //heritance of positive annotations, conflicting annotation 
                        //with positive parent annotations
                        new CuratorAnnotationBean("HOM:0000007", Arrays.asList("CL:0000216"), 
                                7711, false, "ECO:0000067", "CIO:0000005", 
                                "http://f50006a.eos-intl.net/ELIBSQL12_F50006A_Documents/93grier.pdf", 
                                "ref title 9", 
                                "supporting text 9", "bgee", "ANN", sdf.parse("2015-02-03")), 
                        new CuratorAnnotationBean("HOM:0000007", Arrays.asList("CL:0000216"), 
                                7742, false, "ECO:0000355", "CIO:0000004", 
                                "ISBN:978-0125449045", "ref title 10", 
                                "supporting text 10", "bgee", "ANN", sdf.parse("2015-02-03")), 
                        new CuratorAnnotationBean("HOM:0000007", Arrays.asList("CL:0000216"), 
                                32524, false, "ECO:0000067", "CIO:0000004", 
                                "DOI:10.1002/jemt.1070320602", "ref title 11", 
                                "supporting text 11", "bgee", "ANN", sdf.parse("2015-02-03")), 
                        new CuratorAnnotationBean("HOM:0000007", Arrays.asList("CL:0000216"), 
                                32524, true, "ECO:0000067", "CIO:0000004", 
                                "PMID:17026980", "ref title 12", 
                                "supporting text 12", "bgee", "ANN", sdf.parse("2013-08-30")), 
                        //one low confidence annotation against 2 medium confidence 
                        //annotations => weakly conflicting
                        new CuratorAnnotationBean("HOM:0000007", Arrays.asList("UBERON:0000926"),  
                                33213, false, "ECO:0000067", "CIO:0000004", 
                                "PMID:24281726", "ref title 13", 
                                "supporting text 13", "bgee", "ANN", sdf.parse("2014-01-13")), 
                        new CuratorAnnotationBean("HOM:0000007", Arrays.asList("UBERON:0000926"), 
                                33213, false, "ECO:0000067", "CIO:0000004", 
                                "PMID:22431747", "ref title 14", 
                                "supporting text 14", "bgee", "ANN", sdf.parse("2013-07-10")), 
                        new CuratorAnnotationBean("HOM:0000007", Arrays.asList("UBERON:0000926"), 
                                33213, true, "ECO:0000067", "CIO:0000005", 
                                "PMID:12459924", "ref title 15", 
                                "supporting text 15", "bgee", "ANN", sdf.parse("2013-07-10")), 
                        //simply strongly conflicting
                        new CuratorAnnotationBean("HOM:0000007", Arrays.asList("UBERON:0001245"), 
                                33213, false, "ECO:0000355", "CIO:0000004", 
                                "http://dpc.uba.uva.nl/ctz/vol73/nr01/art01", "ref title 16", 
                                "supporting text 16", "bgee", "ANN", sdf.parse("2013-10-08")), 
                        new CuratorAnnotationBean("HOM:0000007", Arrays.asList("UBERON:0001245"), 
                                33213, true, "ECO:0000033", "CIO:0000004", 
                                "http://dpc.uba.uva.nl/ctz/vol73/nr01/art01", "ref title 16", 
                                "supporting text 16", "bgee", "ANN", sdf.parse("2013-10-08"))), 
                SimilarityAnnotation.extractCuratorAnnotations(
                        SimilarityAnnotationTest.class.
                        getResource("/similarity_annotations/curator_similarity_annotations.tsv").
                        getFile()));
        
        log.exit();
    }
    
    @Ignore
    @Test
    public void test() throws FileNotFoundException, IllegalArgumentException, IOException {
        SimilarityAnnotation.extractCuratorAnnotations(
                "/Users/admin/Desktop/bgee_trans_similarity_annotations_edit2.tsv");
    }
    
    /**
     * Test {@link SimilarityAnnotation#writeAnnotations(List, String, Class)} 
     * for {@code RawAnnotationBean} type.
     */
    @Test
    public void shouldWriteRawAnnotations() throws IOException, IllegalArgumentException, 
        ParseException {
        String tempFile = testFolder.newFile("rawAnnotsTest.tsv").getPath();
        SimpleDateFormat sdf = new SimpleDateFormat(SimilarityAnnotationUtils.DATE_FORMAT);
        SimilarityAnnotation.writeAnnotations(Arrays.asList(
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
                                Arrays.asList("CL:0000037", "UBERON:0000001", "UBERON:0000007"), 
                                Arrays.asList("hematopoietic stem cell", "whatever name1", 
                                        "whatever name2"), 
                                7742, "Vertebrata", true, "ECO:0000067", 
                                "developmental similarity evidence", 
                                "CIO:0000004", "medium confidence from single evidence", 
                                "DOI:10.1146/annurev.cellbio.22.010605.093317", "ref title 7", 
                                "supporting text 7", "bgee", "ANN", sdf.parse("2013-07-01"))), 
                                tempFile, RawAnnotationBean.class);
        
        //we retrieve the annotations without using the extraction methods, to maintain 
        //the unit of the test.
        List<Map<String, String>> expectedAnnots = new ArrayList<Map<String, String>>();
        
        Map<String, String> expectedAnnot1 = new HashMap<String, String>();
        expectedAnnot1.put(SimilarityAnnotationUtils.HOM_COL_NAME, "HOM:0000007");
        expectedAnnot1.put(SimilarityAnnotationUtils.HOM_NAME_COL_NAME, "historical homology");
        expectedAnnot1.put(SimilarityAnnotationUtils.ENTITY_COL_NAME, "CL:0000000");
        expectedAnnot1.put(SimilarityAnnotationUtils.ENTITY_NAME_COL_NAME, "cell");
        expectedAnnot1.put(SimilarityAnnotationUtils.TAXON_COL_NAME, "2759");
        expectedAnnot1.put(SimilarityAnnotationUtils.TAXON_NAME_COL_NAME, "Eukaryota");
        expectedAnnot1.put(SimilarityAnnotationUtils.QUALIFIER_COL_NAME, null);
        expectedAnnot1.put(SimilarityAnnotationUtils.ECO_COL_NAME, "ECO:0000033");
        expectedAnnot1.put(SimilarityAnnotationUtils.ECO_NAME_COL_NAME, "traceable author statement");
        expectedAnnot1.put(SimilarityAnnotationUtils.CONF_COL_NAME, "CIO:0000003");
        expectedAnnot1.put(SimilarityAnnotationUtils.CONF_NAME_COL_NAME, 
                "high confidence from single evidence");
        expectedAnnot1.put(SimilarityAnnotationUtils.REF_COL_NAME, "DOI:10.1073/pnas.032658599");
        expectedAnnot1.put(SimilarityAnnotationUtils.REF_TITLE_COL_NAME, "ref title 1");
        expectedAnnot1.put(SimilarityAnnotationUtils.SUPPORT_TEXT_COL_NAME, "supporting text 1");
        expectedAnnot1.put(SimilarityAnnotationUtils.ASSIGN_COL_NAME, "bgee");
        expectedAnnot1.put(SimilarityAnnotationUtils.CURATOR_COL_NAME, "ANN");
        expectedAnnot1.put(SimilarityAnnotationUtils.DATE_COL_NAME, "2013-06-21");
        
        Map<String, String> expectedAnnot2 = new HashMap<String, String>();
        expectedAnnot2.put(SimilarityAnnotationUtils.HOM_COL_NAME, "HOM:0000007");
        expectedAnnot2.put(SimilarityAnnotationUtils.HOM_NAME_COL_NAME, "historical homology");
        expectedAnnot2.put(SimilarityAnnotationUtils.ENTITY_COL_NAME, "CL:0000015");
        expectedAnnot2.put(SimilarityAnnotationUtils.ENTITY_NAME_COL_NAME, "male germ cell");
        expectedAnnot2.put(SimilarityAnnotationUtils.TAXON_COL_NAME, "33208");
        expectedAnnot2.put(SimilarityAnnotationUtils.TAXON_NAME_COL_NAME, "Metazoa");
        expectedAnnot2.put(SimilarityAnnotationUtils.QUALIFIER_COL_NAME, null);
        expectedAnnot2.put(SimilarityAnnotationUtils.ECO_COL_NAME, "ECO:0000205");
        expectedAnnot2.put(SimilarityAnnotationUtils.ECO_NAME_COL_NAME, "curator inference");
        expectedAnnot2.put(SimilarityAnnotationUtils.CONF_COL_NAME, "CIO:0000004");
        expectedAnnot2.put(SimilarityAnnotationUtils.CONF_NAME_COL_NAME, 
                "medium confidence from single evidence");
        expectedAnnot2.put(SimilarityAnnotationUtils.REF_COL_NAME, "DOI:10.1002/bies.950161213");
        expectedAnnot2.put(SimilarityAnnotationUtils.REF_TITLE_COL_NAME, "ref title 2");
        expectedAnnot2.put(SimilarityAnnotationUtils.SUPPORT_TEXT_COL_NAME, "supporting text 2");
        expectedAnnot2.put(SimilarityAnnotationUtils.ASSIGN_COL_NAME, "bgee");
        expectedAnnot2.put(SimilarityAnnotationUtils.CURATOR_COL_NAME, "ANN");
        expectedAnnot2.put(SimilarityAnnotationUtils.DATE_COL_NAME, "2013-08-29");
        
        Map<String, String> expectedAnnot3 = new HashMap<String, String>();
        expectedAnnot3.put(SimilarityAnnotationUtils.HOM_COL_NAME, "HOM:0000007");
        expectedAnnot3.put(SimilarityAnnotationUtils.HOM_NAME_COL_NAME, "historical homology");
        expectedAnnot3.put(SimilarityAnnotationUtils.ENTITY_COL_NAME, 
                "CL:0000037" + Utils.VALUE_SEPARATORS.get(0) 
                + "UBERON:0000001" + Utils.VALUE_SEPARATORS.get(0) + "UBERON:0000007");
        expectedAnnot3.put(SimilarityAnnotationUtils.ENTITY_NAME_COL_NAME, 
                "hematopoietic stem cell" + Utils.VALUE_SEPARATORS.get(0) 
                + "whatever name1" + Utils.VALUE_SEPARATORS.get(0) + "whatever name2");
        expectedAnnot3.put(SimilarityAnnotationUtils.TAXON_COL_NAME, "7742");
        expectedAnnot3.put(SimilarityAnnotationUtils.TAXON_NAME_COL_NAME, "Vertebrata");
        expectedAnnot3.put(SimilarityAnnotationUtils.QUALIFIER_COL_NAME, 
                SimilarityAnnotationUtils.NEGATE_QUALIFIER);
        expectedAnnot3.put(SimilarityAnnotationUtils.ECO_COL_NAME, "ECO:0000067");
        expectedAnnot3.put(SimilarityAnnotationUtils.ECO_NAME_COL_NAME, 
                "developmental similarity evidence");
        expectedAnnot3.put(SimilarityAnnotationUtils.CONF_COL_NAME, "CIO:0000004");
        expectedAnnot3.put(SimilarityAnnotationUtils.CONF_NAME_COL_NAME, 
                "medium confidence from single evidence");
        expectedAnnot3.put(SimilarityAnnotationUtils.REF_COL_NAME, 
                "DOI:10.1146/annurev.cellbio.22.010605.093317");
        expectedAnnot3.put(SimilarityAnnotationUtils.REF_TITLE_COL_NAME, "ref title 7");
        expectedAnnot3.put(SimilarityAnnotationUtils.SUPPORT_TEXT_COL_NAME, "supporting text 7");
        expectedAnnot3.put(SimilarityAnnotationUtils.ASSIGN_COL_NAME, "bgee");
        expectedAnnot3.put(SimilarityAnnotationUtils.CURATOR_COL_NAME, "ANN");
        expectedAnnot3.put(SimilarityAnnotationUtils.DATE_COL_NAME, "2013-07-01");
        
        expectedAnnots.add(expectedAnnot1);
        expectedAnnots.add(expectedAnnot2);
        expectedAnnots.add(expectedAnnot3);
        
        List<Map<String, String>> actualAnnots = new ArrayList<Map<String, String>>();
        try (ICsvMapReader mapReader = 
                new CsvMapReader(new FileReader(tempFile), Utils.TSVCOMMENTED)) {
            String[] header = mapReader.getHeader(true);
            Map<String, String> row;
            while( (row = mapReader.read(header)) != null ) {
                actualAnnots.add(row);
            }
        }
        
        assertEquals("Incorrect RAW annotations written", expectedAnnots, actualAnnots);
    }
    
    /**
     * Test {@link SimilarityAnnotation#writeAnnotations(List, String, Class)} 
     * for {@code SummaryAnnotationBean} type.
     */
    @Test
    public void shouldWriteSummaryAnnotations() throws IOException, IllegalArgumentException {
        String tempFile = testFolder.newFile("summaryAnnotsTest.tsv").getPath();
        SimilarityAnnotation.writeAnnotations(Arrays.asList(
                        new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                                2759, "Eukaryota", false, 
                                "CIO:0000003", "high confidence from single evidence", true, 
                                null, 1
//                                , 1, 0, 
//                                Arrays.asList("ECO:0000033"), 
//                                Arrays.asList("traceable author statement"), 
//                                null, null, null, null, 
//                                Arrays.asList("bgee")
                                ), 
                        new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("CL:0000015"), Arrays.asList("male germ cell"), 
                                33208, "Metazoa", false, 
                                "CIO:0000019", "confidence statement from congruent evidence "
                                        + "lines of same type, overall confidence medium", 
                                true, 
                                null, 2
//                                , 2, 0, 
//                                Arrays.asList("ECO:0000205"), 
//                                Arrays.asList("curator inference"), 
//                                null, null, null, null, 
//                                Arrays.asList("bgee", "test db")
                                ),
                        new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("CL:0000037", "UBERON:0000001", "UBERON:0000007"), 
                                Arrays.asList("hematopoietic stem cell", "whatever name1", 
                                        "whatever name2"), 
                                7742, "Vertebrata", true, 
                                "CIO:0000004", "medium confidence from single evidence", true, 
                                null, 1
//                                        , 0, 1,  
//                                        null, null, 
//                                        Arrays.asList("ECO:0000067"), 
//                                        Arrays.asList("developmental similarity evidence"), 
//                                        null, null, 
//                                        Arrays.asList("bgee")
                                )), 
                                tempFile, SummaryAnnotationBean.class);
        
        //we retrieve the annotations without using the extraction methods, to maintain 
        //the unit of the test.
        List<Map<String, String>> expectedAnnots = new ArrayList<Map<String, String>>();
        
        Map<String, String> expectedAnnot1 = new HashMap<String, String>();
        expectedAnnot1.put(SimilarityAnnotationUtils.HOM_COL_NAME, "HOM:0000007");
        expectedAnnot1.put(SimilarityAnnotationUtils.HOM_NAME_COL_NAME, "historical homology");
        expectedAnnot1.put(SimilarityAnnotationUtils.ENTITY_COL_NAME, "CL:0000000");
        expectedAnnot1.put(SimilarityAnnotationUtils.ENTITY_NAME_COL_NAME, "cell");
        expectedAnnot1.put(SimilarityAnnotationUtils.TAXON_COL_NAME, "2759");
        expectedAnnot1.put(SimilarityAnnotationUtils.TAXON_NAME_COL_NAME, "Eukaryota");
        expectedAnnot1.put(SimilarityAnnotationUtils.QUALIFIER_COL_NAME, null);
        expectedAnnot1.put(SimilarityAnnotationUtils.CONF_COL_NAME, "CIO:0000003");
        expectedAnnot1.put(SimilarityAnnotationUtils.CONF_NAME_COL_NAME, 
                "high confidence from single evidence");
        expectedAnnot1.put(SimilarityAnnotationUtils.TRUSTED_COL_NAME, "T");
        expectedAnnot1.put(SimilarityAnnotationUtils.ANNOT_COUNT_COL_NAME, "1");
        
        Map<String, String> expectedAnnot2 = new HashMap<String, String>();
        expectedAnnot2.put(SimilarityAnnotationUtils.HOM_COL_NAME, "HOM:0000007");
        expectedAnnot2.put(SimilarityAnnotationUtils.HOM_NAME_COL_NAME, "historical homology");
        expectedAnnot2.put(SimilarityAnnotationUtils.ENTITY_COL_NAME, "CL:0000015");
        expectedAnnot2.put(SimilarityAnnotationUtils.ENTITY_NAME_COL_NAME, "male germ cell");
        expectedAnnot2.put(SimilarityAnnotationUtils.TAXON_COL_NAME, "33208");
        expectedAnnot2.put(SimilarityAnnotationUtils.TAXON_NAME_COL_NAME, "Metazoa");
        expectedAnnot2.put(SimilarityAnnotationUtils.QUALIFIER_COL_NAME, null);
        expectedAnnot2.put(SimilarityAnnotationUtils.CONF_COL_NAME, "CIO:0000019");
        expectedAnnot2.put(SimilarityAnnotationUtils.CONF_NAME_COL_NAME, 
                "confidence statement from congruent evidence "
                + "lines of same type, overall confidence medium");
        expectedAnnot2.put(SimilarityAnnotationUtils.TRUSTED_COL_NAME, "T");
        expectedAnnot2.put(SimilarityAnnotationUtils.ANNOT_COUNT_COL_NAME, "2");
        
        Map<String, String> expectedAnnot3 = new HashMap<String, String>();
        expectedAnnot3.put(SimilarityAnnotationUtils.HOM_COL_NAME, "HOM:0000007");
        expectedAnnot3.put(SimilarityAnnotationUtils.HOM_NAME_COL_NAME, "historical homology");
        expectedAnnot3.put(SimilarityAnnotationUtils.ENTITY_COL_NAME, 
                "CL:0000037" + Utils.VALUE_SEPARATORS.get(0) 
                + "UBERON:0000001" + Utils.VALUE_SEPARATORS.get(0) + "UBERON:0000007");
        expectedAnnot3.put(SimilarityAnnotationUtils.ENTITY_NAME_COL_NAME, 
                "hematopoietic stem cell" + Utils.VALUE_SEPARATORS.get(0) 
                + "whatever name1" + Utils.VALUE_SEPARATORS.get(0) + "whatever name2");
        expectedAnnot3.put(SimilarityAnnotationUtils.TAXON_COL_NAME, "7742");
        expectedAnnot3.put(SimilarityAnnotationUtils.TAXON_NAME_COL_NAME, "Vertebrata");
        expectedAnnot3.put(SimilarityAnnotationUtils.QUALIFIER_COL_NAME, 
                SimilarityAnnotationUtils.NEGATE_QUALIFIER);
        expectedAnnot3.put(SimilarityAnnotationUtils.CONF_COL_NAME, "CIO:0000004");
        expectedAnnot3.put(SimilarityAnnotationUtils.CONF_NAME_COL_NAME, 
                "medium confidence from single evidence");
        expectedAnnot3.put(SimilarityAnnotationUtils.TRUSTED_COL_NAME, "T");
        expectedAnnot3.put(SimilarityAnnotationUtils.ANNOT_COUNT_COL_NAME, "1");
        
        expectedAnnots.add(expectedAnnot1);
        expectedAnnots.add(expectedAnnot2);
        expectedAnnots.add(expectedAnnot3);
        
        List<Map<String, String>> actualAnnots = new ArrayList<Map<String, String>>();
        try (ICsvMapReader mapReader = 
                new CsvMapReader(new FileReader(tempFile), Utils.TSVCOMMENTED)) {
            String[] header = mapReader.getHeader(true);
            Map<String, String> row;
            while( (row = mapReader.read(header)) != null ) {
                actualAnnots.add(row);
            }
        }
        
        assertEquals("Incorrect SUMMARY annotations written", expectedAnnots, actualAnnots);
    }
    
    /**
     * Test {@link SimilarityAnnotation#writeAnnotations(List, String, Class)} 
     * for {@code AncestralTaxaAnnotationBean} type.
     */
    @Test
    public void shouldWriteAncestralTaxaAnnotations() throws IOException, IllegalArgumentException {
        String tempFile = testFolder.newFile("ancestralTaxaAnnotsTest.tsv").getPath();
        SimilarityAnnotation.writeAnnotations(Arrays.asList(
                        new AncestralTaxaAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                                2759, "Eukaryota", 
                                "CIO:0000003", "high confidence from single evidence", 
                                "Summary annotation created from 1 single-evidence annotation"
//                                , 1, 0, 
//                                Arrays.asList("ECO:0000033"), 
//                                Arrays.asList("traceable author statement"), 
//                                null, null, null, null, 
//                                Arrays.asList("bgee")
                                ), 
                        new AncestralTaxaAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("CL:0000015"), Arrays.asList("male germ cell"), 
                                33208, "Metazoa", 
                                "CIO:0000019", "confidence statement from congruent evidence "
                                        + "lines of same type, overall confidence medium", 
                                "Summary annotation created from 2 single-evidence annotations"
//                                , 2, 0, 
//                                Arrays.asList("ECO:0000205"), 
//                                Arrays.asList("curator inference"), 
//                                null, null, null, null, 
//                                Arrays.asList("bgee", "test db")
                                ),
                        new AncestralTaxaAnnotationBean("HOM:0000007", "historical homology", 
                                Arrays.asList("CL:0000037", "UBERON:0000001", "UBERON:0000007"), 
                                Arrays.asList("hematopoietic stem cell", "whatever name1", 
                                        "whatever name2"), 
                                7742, "Vertebrata", 
                                "CIO:0000004", "medium confidence from single evidence", 
                                "Summary annotation created from 1 single-evidence annotation"
//                                        , 0, 1,  
//                                        null, null, 
//                                        Arrays.asList("ECO:0000067"), 
//                                        Arrays.asList("developmental similarity evidence"), 
//                                        null, null, 
//                                        Arrays.asList("bgee")
                                )), 
                                tempFile, AncestralTaxaAnnotationBean.class);
        
        //we retrieve the annotations without using the extraction methods, to maintain 
        //the unit of the test.
        List<Map<String, String>> expectedAnnots = new ArrayList<Map<String, String>>();
        
        Map<String, String> expectedAnnot1 = new HashMap<String, String>();
        expectedAnnot1.put(SimilarityAnnotationUtils.HOM_COL_NAME, "HOM:0000007");
        expectedAnnot1.put(SimilarityAnnotationUtils.HOM_NAME_COL_NAME, "historical homology");
        expectedAnnot1.put(SimilarityAnnotationUtils.ENTITY_COL_NAME, "CL:0000000");
        expectedAnnot1.put(SimilarityAnnotationUtils.ENTITY_NAME_COL_NAME, "cell");
        expectedAnnot1.put(SimilarityAnnotationUtils.TAXON_COL_NAME, "2759");
        expectedAnnot1.put(SimilarityAnnotationUtils.TAXON_NAME_COL_NAME, "Eukaryota");
        expectedAnnot1.put(SimilarityAnnotationUtils.CONF_COL_NAME, "CIO:0000003");
        expectedAnnot1.put(SimilarityAnnotationUtils.CONF_NAME_COL_NAME, 
                "high confidence from single evidence");
        expectedAnnot1.put(SimilarityAnnotationUtils.SUPPORT_TEXT_COL_NAME, 
                "Summary annotation created from 1 single-evidence annotation");
        
        Map<String, String> expectedAnnot2 = new HashMap<String, String>();
        expectedAnnot2.put(SimilarityAnnotationUtils.HOM_COL_NAME, "HOM:0000007");
        expectedAnnot2.put(SimilarityAnnotationUtils.HOM_NAME_COL_NAME, "historical homology");
        expectedAnnot2.put(SimilarityAnnotationUtils.ENTITY_COL_NAME, "CL:0000015");
        expectedAnnot2.put(SimilarityAnnotationUtils.ENTITY_NAME_COL_NAME, "male germ cell");
        expectedAnnot2.put(SimilarityAnnotationUtils.TAXON_COL_NAME, "33208");
        expectedAnnot2.put(SimilarityAnnotationUtils.TAXON_NAME_COL_NAME, "Metazoa");
        expectedAnnot2.put(SimilarityAnnotationUtils.CONF_COL_NAME, "CIO:0000019");
        expectedAnnot2.put(SimilarityAnnotationUtils.CONF_NAME_COL_NAME, 
                "confidence statement from congruent evidence "
                + "lines of same type, overall confidence medium");
        expectedAnnot2.put(SimilarityAnnotationUtils.SUPPORT_TEXT_COL_NAME, 
                "Summary annotation created from 2 single-evidence annotations");
        
        Map<String, String> expectedAnnot3 = new HashMap<String, String>();
        expectedAnnot3.put(SimilarityAnnotationUtils.HOM_COL_NAME, "HOM:0000007");
        expectedAnnot3.put(SimilarityAnnotationUtils.HOM_NAME_COL_NAME, "historical homology");
        expectedAnnot3.put(SimilarityAnnotationUtils.ENTITY_COL_NAME, 
                "CL:0000037" + Utils.VALUE_SEPARATORS.get(0) 
                + "UBERON:0000001" + Utils.VALUE_SEPARATORS.get(0) + "UBERON:0000007");
        expectedAnnot3.put(SimilarityAnnotationUtils.ENTITY_NAME_COL_NAME, 
                "hematopoietic stem cell" + Utils.VALUE_SEPARATORS.get(0) 
                + "whatever name1" + Utils.VALUE_SEPARATORS.get(0) + "whatever name2");
        expectedAnnot3.put(SimilarityAnnotationUtils.TAXON_COL_NAME, "7742");
        expectedAnnot3.put(SimilarityAnnotationUtils.TAXON_NAME_COL_NAME, "Vertebrata");
        expectedAnnot3.put(SimilarityAnnotationUtils.CONF_COL_NAME, "CIO:0000004");
        expectedAnnot3.put(SimilarityAnnotationUtils.CONF_NAME_COL_NAME, 
                "medium confidence from single evidence");
        expectedAnnot3.put(SimilarityAnnotationUtils.SUPPORT_TEXT_COL_NAME, 
                "Summary annotation created from 1 single-evidence annotation");
        
        expectedAnnots.add(expectedAnnot1);
        expectedAnnots.add(expectedAnnot2);
        expectedAnnots.add(expectedAnnot3);
        
        List<Map<String, String>> actualAnnots = new ArrayList<Map<String, String>>();
        try (ICsvMapReader mapReader = 
                new CsvMapReader(new FileReader(tempFile), Utils.TSVCOMMENTED)) {
            String[] header = mapReader.getHeader(true);
            Map<String, String> row;
            while( (row = mapReader.read(header)) != null ) {
                actualAnnots.add(row);
            }
        }
        
        assertEquals("Incorrect SUMMARY annotations written", expectedAnnots, actualAnnots);
    }
    
    /**
     * Test {@link SimilarityAnnotation.checkAnnotations(Collection)} for 
     * {@code RawAnnotationBean}s.
     */
    @Test
    public void shouldCheckRawAnnotations() throws ParseException, OBOFormatParserException, 
    FileNotFoundException, OWLOntologyCreationException, IOException {
        SimpleDateFormat sdf = new SimpleDateFormat(SimilarityAnnotationUtils.DATE_FORMAT);
        
        SimilarityAnnotation simAnnot = new SimilarityAnnotation(
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/taxonConstraints.tsv").getFile(),
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/fake_uberon.obo").getFile(), 
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/fake_taxonomy.obo").getFile(), 
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/homology_ontology.obo").getFile(), 
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/fake_eco.obo").getFile(), 
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/cio-simple.obo").getFile());
        
        List<RawAnnotationBean> annots = new ArrayList<RawAnnotationBean>(Arrays.asList(
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
                        "supporting text 7", "bgee", "ANN", sdf.parse("2013-07-01"))));
        
        //everything should work with these annotations
        simAnnot.checkAnnotations(annots, true);
        
        //incorrect HOM name
        RawAnnotationBean incorrectAnnot = new RawAnnotationBean(
                "HOM:0000007", "fake historical homology", 
                Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                2759, "Eukaryota", false, "ECO:0000033", 
                "traceable author statement", 
                "CIO:0000003", "high confidence from single evidence", 
                "DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1 change to avoid duplicate", "bgee", "ANN", 
                sdf.parse("2013-06-21"));
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for an incorrect HOM name"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);
        //untrimmed HOM name
        incorrectAnnot = new RawAnnotationBean(
                "HOM:0000007", "  historical homology ", 
                Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                2759, "Eukaryota", false, "ECO:0000033", 
                "traceable author statement", 
                "CIO:0000003", "high confidence from single evidence", 
                "DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1 change to avoid duplicate", "bgee", "ANN", 
                sdf.parse("2013-06-21"));
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for an untrimmed HOM name"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);
        
        //incorrect entity name
        incorrectAnnot = new RawAnnotationBean("HOM:0000007", "historical homology", 
                Arrays.asList("CL:0000037", "UBERON:0000001", "UBERON:0000007"), 
                Arrays.asList("hematopoietic stem cell", "WRONG whatever name1", 
                        "whatever name2"), 
                7742, "Vertebrata", true, "ECO:0000067", 
                "developmental similarity evidence", 
                "CIO:0000004", "medium confidence from single evidence", 
                "DOI:10.1146/annurev.cellbio.22.010605.093317", "ref title 7", 
                "supporting text 7 change to avoid duplicate", "bgee", "ANN", 
                sdf.parse("2013-07-01"));
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for an incorrect entity name"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);
        //untrimmed entity name
        incorrectAnnot = new RawAnnotationBean(
                "HOM:0000007", "historical homology", 
                Arrays.asList("CL:0000000"), Arrays.asList(" cell "), 
                2759, "Eukaryota", false, "ECO:0000033", 
                "traceable author statement", 
                "CIO:0000003", "high confidence from single evidence", 
                "DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1 change to avoid duplicate", "bgee", "ANN", 
                sdf.parse("2013-06-21"));
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for an untrimmed entity name"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);
        
        //incorrect taxon name
        incorrectAnnot = new RawAnnotationBean(
                "HOM:0000007", "historical homology", 
                Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                2759, "fake Eukaryota", false, "ECO:0000033", 
                "traceable author statement", 
                "CIO:0000003", "high confidence from single evidence", 
                "DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1 change to avoid duplicate", "bgee", "ANN", 
                sdf.parse("2013-06-21"));
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for an incorrect taxon name"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);
        //untrimmed taxon name
        incorrectAnnot = new RawAnnotationBean(
                "HOM:0000007", "historical homology", 
                Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                2759, "Eukaryota ", false, "ECO:0000033", 
                "traceable author statement", 
                "CIO:0000003", "high confidence from single evidence", 
                "DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1 change to avoid duplicate", "bgee", "ANN", 
                sdf.parse("2013-06-21"));
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for an untrimmed taxon name"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);
        
        //incorrect ECO name
        incorrectAnnot = new RawAnnotationBean(
                "HOM:0000007", "historical homology", 
                Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                2759, "Eukaryota", false, "ECO:0000033", 
                "fake traceable author statement", 
                "CIO:0000003", "high confidence from single evidence", 
                "DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1 change to avoid duplicate", "bgee", "ANN", 
                sdf.parse("2013-06-21"));
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for an incorrect ECO name"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);
        //untrimmed ECO name
        incorrectAnnot = new RawAnnotationBean(
                "HOM:0000007", "historical homology", 
                Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                2759, "Eukaryota", false, "ECO:0000033", 
                "  traceable author statement  ", 
                "CIO:0000003", "high confidence from single evidence", 
                "DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1 change to avoid duplicate", "bgee", "ANN", 
                sdf.parse("2013-06-21"));
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for an untrimmed ECO name"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);
        
        //incorrect CIO name
        incorrectAnnot = new RawAnnotationBean(
                "HOM:0000007", "historical homology", 
                Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                2759, "Eukaryota", false, "ECO:0000033", 
                "traceable author statement", 
                "CIO:0000003", "fake high confidence from single evidence", 
                "DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1 change to avoid duplicate", "bgee", "ANN", 
                sdf.parse("2013-06-21"));
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for an incorrect CIO name"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);
        //untrimmed CIO name
        incorrectAnnot = new RawAnnotationBean(
                "HOM:0000007", "historical homology", 
                Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                2759, "Eukaryota", false, "ECO:0000033", 
                "traceable author statement", 
                "CIO:0000003", "high confidence from single evidence  ", 
                "DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1 change to avoid duplicate", "bgee", "ANN", 
                sdf.parse("2013-06-21"));
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for an untrimmed CIO name"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);
        
        //duplicated annotation
        incorrectAnnot = new RawAnnotationBean("HOM:0000007", "historical homology", 
                Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                2759, "Eukaryota", false, "ECO:0000033", 
                "traceable author statement", 
                "CIO:0000003", "high confidence from single evidence", 
                "DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21"));
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for a duplicated annotation"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);
        
        //incorrect taxon constraints
        incorrectAnnot = new RawAnnotationBean(
                "HOM:0000007", "historical homology", 
                Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                3, "taxon 3", false, "ECO:0000033", 
                "traceable author statement", 
                "CIO:0000003", "high confidence from single evidence", 
                "DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1 change to avoid duplicate", "bgee", "ANN", 
                sdf.parse("2013-06-21"));
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for an incorrect taxon constraints"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);
    
        //missing HOM ID
        incorrectAnnot = new RawAnnotationBean("", "historical homology", 
                Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                2759, "Eukaryota", false, "ECO:0000033", 
                "traceable author statement", 
                "CIO:0000003", "high confidence from single evidence", 
                "DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21"));
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for a missing HOM ID"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);
        //untrimmed HOM ID
        incorrectAnnot = new RawAnnotationBean(
                "HOM:0000007  ", "historical homology", 
                Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                2759, "Eukaryota", false, "ECO:0000033", 
                "traceable author statement", 
                "CIO:0000003", "high confidence from single evidence", 
                "DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1 change to avoid duplicate", "bgee", "ANN", 
                sdf.parse("2013-06-21"));
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for an untrimmed HOM ID"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);
    
        //missing entity ID
        incorrectAnnot = new RawAnnotationBean("HOM:0000007", "historical homology", 
                Arrays.asList(""), Arrays.asList("cell"), 
                2759, "Eukaryota", false, "ECO:0000033", 
                "traceable author statement", 
                "CIO:0000003", "high confidence from single evidence", 
                "DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21"));
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for a missing entity ID"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);
        //untrimmed entity ID
        incorrectAnnot = new RawAnnotationBean(
                "HOM:0000007", "historical homology", 
                Arrays.asList("CL:0000000  "), Arrays.asList("cell"), 
                2759, "Eukaryota", false, "ECO:0000033", 
                "traceable author statement", 
                "CIO:0000003", "high confidence from single evidence", 
                "DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1 change to avoid duplicate", "bgee", "ANN", 
                sdf.parse("2013-06-21"));
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for an untrimmed entity ID"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);
    
        //missing taxon ID
        incorrectAnnot = new RawAnnotationBean("HOM:0000007", "historical homology", 
                Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                0, "Eukaryota", false, "ECO:0000033", 
                "traceable author statement", 
                "CIO:0000003", "high confidence from single evidence", 
                "DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21"));
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for a missing taxon ID"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);
    
        //missing ECO ID
        incorrectAnnot = new RawAnnotationBean("HOM:0000007", "historical homology", 
                Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                2759, "Eukaryota", false, "", 
                "traceable author statement", 
                "CIO:0000003", "high confidence from single evidence", 
                "DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1 change for no duplicate", "bgee", "ANN", sdf.parse("2013-06-21"));
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for a missing ECO ID"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);
        //untrimmed ECO ID
        incorrectAnnot = new RawAnnotationBean(
                "HOM:0000007", "historical homology", 
                Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                2759, "Eukaryota", false, " ECO:0000033", 
                "traceable author statement", 
                "CIO:0000003", "high confidence from single evidence", 
                "DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1 change to avoid duplicate", "bgee", "ANN", 
                sdf.parse("2013-06-21"));
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for an untrimmed ECO ID"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);
    
        //missing CIO ID
        incorrectAnnot = new RawAnnotationBean("HOM:0000007", "historical homology", 
                Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                2759, "Eukaryota", false, "ECO:0000033", 
                "traceable author statement", 
                "", "high confidence from single evidence", 
                "DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1 change for no duplicate", "bgee", "ANN", sdf.parse("2013-06-21"));
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for a missing CIO ID"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);
        //untrimmed CIO ID
        incorrectAnnot = new RawAnnotationBean(
                "HOM:0000007", "historical homology", 
                Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                2759, "Eukaryota", false, "ECO:0000033", 
                "traceable author statement", 
                "CIO:0000003  ", "high confidence from single evidence", 
                "DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1 change to avoid duplicate", "bgee", "ANN", 
                sdf.parse("2013-06-21"));
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for an untrimmed CIO ID"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);

        //untrimmed ref ID
        incorrectAnnot = new RawAnnotationBean(
                "HOM:0000007", "historical homology", 
                Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                2759, "Eukaryota", false, "ECO:0000033", 
                "traceable author statement", 
                "CIO:0000003", "high confidence from single evidence", 
                "  DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1 change to avoid duplicate", "bgee", "ANN", 
                sdf.parse("2013-06-21"));
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for an untrimmed Ref ID"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);
        //untrimmed ref title
        incorrectAnnot = new RawAnnotationBean(
                "HOM:0000007", "historical homology", 
                Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                2759, "Eukaryota", false, "ECO:0000033", 
                "traceable author statement", 
                "CIO:0000003", "high confidence from single evidence", 
                "DOI:10.1073/pnas.032658599", "  ref title 1", 
                "supporting text 1 change to avoid duplicate", "bgee", "ANN", 
                sdf.parse("2013-06-21"));
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for an untrimmed Ref title"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);
    
        //missing date for non-automatic assertion
        incorrectAnnot = new RawAnnotationBean("HOM:0000007", "historical homology", 
                Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                2759, "Eukaryota", false, "ECO:0000033", 
                "traceable author statement", 
                "CIO:0000003", "high confidence from single evidence", 
                "DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1 change for no duplicate", "bgee", "ANN", null);
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for a missing Date for non-automatic assertion"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);
        //but if it was for an automatic annotation, it is fine
        incorrectAnnot = new RawAnnotationBean("HOM:0000007", "historical homology", 
                Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                2759, "Eukaryota", false, "ECO:0000501", 
                "evidence used in automatic assertion", 
                "CIO:0000003", "high confidence from single evidence", 
                "DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1 change for no duplicate", "bgee", "ANN", null);
        annots.add(incorrectAnnot);
        //everything should be fine
        simAnnot.checkAnnotations(annots, true);
        annots.remove(incorrectAnnot);
    
        //entity IDs incorrectly ordered
        incorrectAnnot = new RawAnnotationBean("HOM:0000007", "historical homology", 
                Arrays.asList("UBERON:0000001", "CL:0000037", "UBERON:0000007"), 
                Arrays.asList("whatever name1", "hematopoietic stem cell", 
                        "whatever name2"), 
                7742, "Vertebrata", true, "ECO:0000067", 
                "developmental similarity evidence", 
                "CIO:0000004", "medium confidence from single evidence", 
                "DOI:10.1146/annurev.cellbio.22.010605.093317", "ref title 7", 
                "supporting text 7", "bgee", "ANN", sdf.parse("2013-07-01"));
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for entity IDs incorrectly ordered"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);
    
        //entity IDs and labels not ordered the same
        incorrectAnnot = new RawAnnotationBean("HOM:0000007", "historical homology", 
                Arrays.asList("CL:0000037", "UBERON:0000001", "UBERON:0000007"), 
                Arrays.asList("whatever name1", "hematopoietic stem cell", 
                        "whatever name2"), 
                7742, "Vertebrata", true, "ECO:0000067", 
                "developmental similarity evidence", 
                "CIO:0000004", "medium confidence from single evidence", 
                "DOI:10.1146/annurev.cellbio.22.010605.093317", "ref title 7", 
                "supporting text 7", "bgee", "ANN", sdf.parse("2013-07-01"));
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for inconsistent order of entity IDs and names"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);
    
        //missing HOM label
        incorrectAnnot = new RawAnnotationBean("HOM:0000007", "", 
                Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                2759, "Eukaryota", false, "ECO:0000033", 
                "traceable author statement", 
                "CIO:0000003", "high confidence from single evidence", 
                "DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21"));
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for a missing HOM label"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);
    
        //missing taxon name
        incorrectAnnot = new RawAnnotationBean("HOM:0000007", "historical homology", 
                Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                2759, "", false, "ECO:0000033", 
                "traceable author statement", 
                "CIO:0000003", "high confidence from single evidence", 
                "DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21"));
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for a missig taxon name"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);
    
        //missing ECO label
        incorrectAnnot = new RawAnnotationBean("HOM:0000007", "historical homology", 
                Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                2759, "Eukaryota", false, "ECO:0000033", 
                "", 
                "CIO:0000003", "high confidence from single evidence", 
                "DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21"));
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for a missing ECO label"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);
    
        //missing CIO label
        incorrectAnnot = new RawAnnotationBean("HOM:0000007", "historical homology", 
                Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                2759, "Eukaryota", false, "ECO:0000033", 
                "traceable author statement", 
                "CIO:0000003", "", 
                "DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21"));
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for a missing CIO label"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);
    
        //missing ref ID
        incorrectAnnot = new RawAnnotationBean("HOM:0000007", "historical homology", 
                Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                2759, "Eukaryota", false, "ECO:0000033", 
                "traceable author statement", 
                "CIO:0000003", "high confidence from single evidence", 
                "", "ref title 1", 
                "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21"));
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for a missing ref ID"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);
    
        //missing ref title
        incorrectAnnot = new RawAnnotationBean("HOM:0000007", "historical homology", 
                Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                2759, "Eukaryota", false, "ECO:0000033", 
                "traceable author statement", 
                "CIO:0000003", "high confidence from single evidence", 
                "DOI:10.1073/pnas.032658599", "", 
                "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21"));
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for a missing ref title"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);
    
        //RAW annotation not using a CIO statement from the single-evidence branch
        incorrectAnnot = new RawAnnotationBean("HOM:0000007", "historical homology", 
                Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                2759, "Eukaryota", false, "ECO:0000033", 
                "traceable author statement", 
                "CIO:0000012", "confidence statement from congruent evidence lines of "
                        + "multiple types, overall confidence high", 
                "DOI:10.1073/pnas.032658599", "", 
                "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21"));
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for a missing ref title"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);
        
        //last verification, to check that the SimilarityAnnotation object is still 
        //in a correct state.
        simAnnot.checkAnnotations(annots, true);
    }
    
    /**
     * Test {@link SimilarityAnnotation.checkAnnotations(Collection)} for 
     * {@code SummaryAnnotationBean}s.
     */
    @Test
    public void shouldCheckSummaryAnnotations() throws OBOFormatParserException, 
    FileNotFoundException, OWLOntologyCreationException, IOException {
        
        SimilarityAnnotation simAnnot = new SimilarityAnnotation(
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/taxonConstraints.tsv").getFile(),
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/fake_uberon.obo").getFile(), 
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/fake_taxonomy.obo").getFile(), 
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/homology_ontology.obo").getFile(), 
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/fake_eco.obo").getFile(), 
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/cio-simple.obo").getFile());
        
        List<SummaryAnnotationBean> annots = new ArrayList<SummaryAnnotationBean>(Arrays.asList(
                new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                        2759, "Eukaryota", false, 
                        "CIO:0000003", "high confidence from single evidence", true, 
                        null, 1
//                        , 1, 0, 
//                        Arrays.asList("ECO:0000033"), 
//                        Arrays.asList("traceable author statement"), 
//                        null, null, null, null, 
//                        Arrays.asList("bgee")
                        ), 
                new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("CL:0000037", "UBERON:0000001", "UBERON:0000007"), 
                        Arrays.asList("hematopoietic stem cell", "whatever name1", 
                                "whatever name2"), 
                        7742, "Vertebrata", true, 
                        "CIO:0000004", "medium confidence from single evidence", true, 
                        null, 1
//                                , 0, 1,  
//                                null, null, 
//                                Arrays.asList("ECO:0000067"), 
//                                Arrays.asList("developmental similarity evidence"), 
//                                null, null, 
//                                Arrays.asList("bgee")
                        )));

        //everything should work with these annotations
        simAnnot.checkAnnotations(annots, true);
        
        //duplicated annotation over HOM ID - entity IDs - taxon ID
        SummaryAnnotationBean incorrectAnnot = new SummaryAnnotationBean(
                "HOM:0000007", "historical homology", 
                Arrays.asList("CL:0000037", "UBERON:0000001", "UBERON:0000007"), 
                Arrays.asList("hematopoietic stem cell", "whatever name1", 
                        "whatever name2"), 
                7742, "Vertebrata", false, 
                "CIO:0000003", "high confidence from single evidence", true, 
                "whatever", 4
//                        , 0, 1,  
//                        null, null, 
//                        Arrays.asList("ECO:0000067"), 
//                        Arrays.asList("developmental similarity evidence"), 
//                        null, null, 
//                        Arrays.asList("bgee")
                );
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for a duplicated annotation"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);
        
        //incorrect trust state
        incorrectAnnot = new SummaryAnnotationBean(
                "HOM:0000007", "historical homology", 
                Arrays.asList("CL:0000037", "UBERON:0000001", "UBERON:0000007"), 
                Arrays.asList("hematopoietic stem cell", "whatever name1", 
                        "whatever name2"), 
                7742, "Vertebrata", false, 
                "CIO:0000005", "low confidence from single evidence", true, 
                "whatever", 4
//                        , 0, 1,  
//                        null, null, 
//                        Arrays.asList("ECO:0000067"), 
//                        Arrays.asList("developmental similarity evidence"), 
//                        null, null, 
//                        Arrays.asList("bgee")
                );
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for an incorrect trust state"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);
        
        //missing underling annotation count
        incorrectAnnot = new SummaryAnnotationBean(
                "HOM:0000007", "historical homology", 
                Arrays.asList("CL:0000037", "UBERON:0000001", "UBERON:0000007"), 
                Arrays.asList("hematopoietic stem cell", "whatever name1", 
                        "whatever name2"), 
                7742, "Vertebrata", false, 
                "CIO:0000005", "low confidence from single evidence", true, 
                "whatever", 0
//                        , 0, 1,  
//                        null, null, 
//                        Arrays.asList("ECO:0000067"), 
//                        Arrays.asList("developmental similarity evidence"), 
//                        null, null, 
//                        Arrays.asList("bgee")
                );
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for a missing underlying annotation count"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);

        //last verification, to check that the SimilarityAnnotation object is still 
        //in a correct state.
        simAnnot.checkAnnotations(annots, true);
    }
    
    /**
     * Test {@link SimilarityAnnotation.checkAnnotations(Collection)} for 
     * {@code AncestralTaxaAnnotationBean}s.
     */
    @Test
    public void shouldCheckAncestralTaxaAnnotations() throws OBOFormatParserException, 
    FileNotFoundException, OWLOntologyCreationException, IOException {
        
        SimilarityAnnotation simAnnot = new SimilarityAnnotation(
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/taxonConstraints.tsv").getFile(),
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/fake_uberon.obo").getFile(), 
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/fake_taxonomy.obo").getFile(), 
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/homology_ontology.obo").getFile(), 
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/fake_eco.obo").getFile(), 
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/cio-simple.obo").getFile());
        
        List<AncestralTaxaAnnotationBean> annots = new ArrayList<AncestralTaxaAnnotationBean>(
                Arrays.asList(
                new AncestralTaxaAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("CL:0000037", "UBERON:0000001", "UBERON:0000007"), 
                        Arrays.asList("hematopoietic stem cell", "whatever name1", 
                                "whatever name2"), 
                                7778, "Elasmobranchii", 
                        "CIO:0000004", "medium confidence from single evidence", null)));

        //everything should work with these annotations
        simAnnot.checkAnnotations(annots, true);
        
        //duplicated annotation over HOM ID - entity IDs - taxon ID
        AncestralTaxaAnnotationBean incorrectAnnot = 
                new AncestralTaxaAnnotationBean("HOM:0000007", "historical homology", 
                Arrays.asList("CL:0000037", "UBERON:0000001", "UBERON:0000007"), 
                Arrays.asList("hematopoietic stem cell", "whatever name1", 
                        "whatever name2"), 
                        7778, "Elasmobranchii", 
                "CIO:0000003", "high confidence from single evidence", "fsfsdf");
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for a duplicated annotation"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);
        
        //annotation with a non-trusted CIO statement: this should never happen 
        //for AncestralTaxaAnnotationBeans
        incorrectAnnot = 
                new AncestralTaxaAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                        2759, "Eukaryota", 
                        "CIO:0000005", "low confidence from single evidence", null);
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for a non-trusted CIO statement"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);
        
        //it is possible to have annotations to same HOM ID - entity IDs, but different taxa, 
        //in case of independent evolution
        AncestralTaxaAnnotationBean correctAnnot = 
                new AncestralTaxaAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("CL:0000037", "UBERON:0000001", "UBERON:0000007"), 
                        Arrays.asList("hematopoietic stem cell", "whatever name1", 
                                "whatever name2"), 
                                32524, "Amniota", 
                        "CIO:0000004", "medium confidence from single evidence", null);
        annots.add(correctAnnot);
        //everything should work
        simAnnot.checkAnnotations(annots, true);
        annots.remove(correctAnnot);
        
        //but it is not possible if the taxa are related (parent-child relation)
        incorrectAnnot = 
                new AncestralTaxaAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("CL:0000037", "UBERON:0000001", "UBERON:0000007"), 
                        Arrays.asList("hematopoietic stem cell", "whatever name1", 
                                "whatever name2"), 
                                7711, "Chordata", 
                        "CIO:0000004", "medium confidence from single evidence", null);
        annots.add(incorrectAnnot);
        try {
            simAnnot.checkAnnotations(annots, false);
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError(
                    "No exception was thrown for a parent-child taxon annotation"));
        } catch (Exception e) {
            //test passed
        }
        annots.remove(incorrectAnnot);

        //last verification, to check that the SimilarityAnnotation object is still 
        //in a correct state.
        simAnnot.checkAnnotations(annots, true);
    }
    
    /**
     * Test {@link SimilarityAnnotation#generateInferredAnnotations(Collection)}.
     */
    @Test
    public void shouldGenerateInferredAnnotations() throws ParseException, 
        OBOFormatParserException, FileNotFoundException, OWLOntologyCreationException, 
        IOException {
        SimilarityAnnotation simAnnot = new SimilarityAnnotation(null,
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/fake_uberon.obo").getFile(), 
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/fake_taxonomy.obo").getFile(), 
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/homology_ontology.obo").getFile(), 
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/fake_eco.obo").getFile(), 
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/cio-simple.obo").getFile());
        
        //annotations that will be propagated through transformation_of relations
        SimpleDateFormat sdf = new SimpleDateFormat(SimilarityAnnotationUtils.DATE_FORMAT);
        List<CuratorAnnotationBean> annots = new ArrayList<CuratorAnnotationBean>(Arrays.asList(
                new CuratorAnnotationBean("HOM:0000007", Arrays.asList("    ID:6    "), 
                        2759, false, "ECO:0000033", "CIO:0000003", 
                        "DOI:10.1073/pnas.032658599", "ref title 1", 
                        "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21")), 
                new CuratorAnnotationBean("HOM:0000007", Arrays.asList("    ID:6    "), 
                        7742, false, "ECO:0000205", "CIO:0000004", 
                        "DOI:10.1073/pnas.032658599", "ref title 1", 
                        "supporting text 2", "bgee", "ANN", sdf.parse("2013-06-22")), 
                new CuratorAnnotationBean("HOM:0000007", Arrays.asList("   ID:6"), 
                        7742, true, "ECO:0000067", "CIO:0000005", 
                        "DOI:10.1073/pnas.032658599", "ref title 1", 
                        "supporting text 3", "bgee", "ANN", sdf.parse("2013-06-23")), 
                new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:4   "), 
                        7742, false, "ECO:0000067", "CIO:0000003", 
                        "DOI:10.1073/pnas.032658599", "ref title 1", 
                        "supporting text 3", "bgee", "ANN", sdf.parse("2013-06-23")), 
                //to check that rejected annotations are discarded
                new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:5 "), 
                        7742, false, "ECO:0000067", "CIO:0000039", 
                        "DOI:10.1073/pnas.032658599", "ref title 1", 
                        "supporting text 3", "bgee", "ANN", sdf.parse("2013-06-23"))));
        
        //annotations that will be propagated through logical constraints.
        
        //here, annotations that will be used over parent-child taxa, and same taxon
        //vertebrata
        annots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:9  "), 
                7742, false, "ECO:0000033", "CIO:0000003", 
                        "DOI:10.1073/pnas.032658599", "ref title 1", 
                        "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21")));
        //only the highest level of confidence should be kept
        annots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("  ID:9"), 
                7742, false, "ECO:0000033", "CIO:0000004", 
                        "DOI:10.1073/pnas.0326585990", "ref title 10", 
                        "supporting text 1 whatever", "bgee", "ANN", sdf.parse("2013-06-21")));
        //metazoa
        annots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:9 "), 
                33208, false, "ECO:0000033", "CIO:0000003", 
                        "DOI:10.1073/pnas.032658599", "ref title 1", 
                        "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21")));
        annots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:10"), 
                33208, false, "ECO:0000033", "CIO:0000005", 
                "DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21")));
        
        //here, annotations to unrelated taxa, annotations will not be propagated
        annots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:18  "), 
                7778, false, "ECO:0000033", "CIO:0000003", 
                        "DOI:10.1073/pnas.032658599", "ref title 1", 
                        "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21")));
        annots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:19   "), 
                32524, false, "ECO:0000033", "CIO:0000003", 
                "DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21")));
        
        //here, annotation positive AND negative for a given taxon level (32524, amniota), 
        //negative at higher taxon level (7742, vertebrata).  The NOT annotation to 
        //7776, Gnathostomata should not be generated (regression test): a NOT annotation 
        //should be inferred only if there is a NOT in the taxon considered, not in higher taxa 
        //(we have only positive annotations to Gnathostomata, so there will be no NOT annotations 
        //inferred).
        //For each intersecting class, 
        //the best confidence should be kept; but for positive annotations, the lowest 
        //confidence over the best confidences of intersecting classes will be kept; 
        //for negative annots, it is simply the best over all negative annots.
        
        //amniota
        annots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:21"), 
                32524, false, "ECO:0000033", "CIO:0000003", 
                        "DOI:10.1073/pnas.032658599", "ref title 1", 
                        "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21")));
        //lower confidence, will not be taken into account
        annots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:21"), 
                32524, false, "ECO:0000033", "CIO:0000005", 
                        "DOI:10.1073/pnas.032658599111", "ref title 1111", 
                        "supporting text 1 whatever10", "bgee", "ANN", sdf.parse("2013-06-21")));
        annots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:21"), 
                32524, true, "ECO:0000033", "CIO:0000004", 
                        "DOI:10.1073/pnas.032658599", "ref title 1", 
                        "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21")));
        //lower confidence, will not be taken into account
        annots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:21"), 
                32524, true, "ECO:0000033", "CIO:0000005", 
                        "DOI:10.1073/pnas.03265859942342", "ref title 143432", 
                        "supporting text 1 whatever432432", "bgee", "ANN", sdf.parse("2013-06-21")));
        //chordata
        annots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:21"), 
                7711, true, "ECO:0000033", "CIO:0000004", 
                        "DOI:10.1073/pnas.032658599", "ref title 1", 
                        "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21")));
        //Gnathostomata
        annots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:22"), 
                7776, false, "ECO:0000033", "CIO:0000004", 
                "DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21")));
        //lower confidence, will not be taken into account
        annots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:22"), 
                7776, false, "ECO:0000033", "CIO:0000005", 
                "DOI:10.1073/pnas.032658599654565", "ref title 1656546546", 
                "supporting text 1 whatever9890", "bgee", "ANN", sdf.parse("2013-06-21")));
        //Vertebrata
        annots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:22"), 
                7742, true, "ECO:0000033", "CIO:0000003", 
                "DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21")));
        
        //negative annotations to related taxa
        //Gnathostomata
        annots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:24"), 
                7776, true, "ECO:0000033", "CIO:0000003", 
                        "DOI:10.1073/pnas.032658599", "ref title 1", 
                        "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21")));
        //Vertebrata
        annots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:25"), 
                7742, true, "ECO:0000033", "CIO:0000004", 
                "DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21")));
        
        //annotations that will allow to link more than 1 anatomical entity 
        //(e.g., if skin is homologous, and limb is homologous to fin, 
        //then skin of limb is homologous to skin of fin
        //Vertebrata
        annots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:11", "ID:12"), 
                7742, false, "ECO:0000033", "CIO:0000004", 
                        "DOI:10.1073/pnas.032658599", "ref title 1", 
                        "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21")));
        annots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:13"), 
                7742, false, "ECO:0000033", "CIO:0000003", 
                "DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21")));
        annots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:14", "ID:15"), 
                7742, false, "ECO:0000033", "CIO:0000003", 
                "DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21")));
        //also, this annotation to more recent taxon does not link multiple entities, 
        //it will be valid for only one of them.
        //Gnathostomata
        annots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:11"), 
                7776, false, "ECO:0000033", "CIO:0000003", 
                        "DOI:10.1073/pnas.032658599", "ref title 1", 
                        "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21")));

        //annotations that SHOULD NOT link more than 1 anatomical entity, 
        //this is a regression test, e.g., 
        //left lobe of thyroid gland = (lobe of thyroid gland AND in_left_side_of some thyroid gland) 
        //right lobe of thyroid gland = (lobe of thyroid gland AND in_right_side_of some thyroid gland) 
        //=> we should not generate an annotation 
        //left lobe of thyroid gland|right lobe of thyroid gland
        //Vertebrata
        annots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:27"), 
                7742, false, "ECO:0000033", "CIO:0000004", 
                        "DOI:10.1073/pnas.032658599", "ref title 1", 
                        "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21")));
        annots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:28"), 
                7742, false, "ECO:0000033", "CIO:0000003", 
                "DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21")));
        
        //regression test for following case: 
        //skin of pes = (zone of skin AND part_of pes)
        //there are two annotations related to pes: 
        //1) pes|pelvic fin radial bone in Sarcopterygii, and 
        //2) pes in tetrapoda.
        //This leads to generate two annotations for skin of pes: one in tetrapoda, 
        //and one in Sarcopterygii; it is definitely weird to speak about 
        //a skin of pes on Sarcopterygii. So, if for an intersect class, 
        //there are both annotations with single-entity and multiple-entities, 
        //only the single-entity annotation should be taken into account.
        //Chordata
        annots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:31", "ID:32"), 
                7711, false, "ECO:0000033", "CIO:0000003", 
                        "DOI:10.1073/pnas.032658599", "ref title 1", 
                        "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21")));
        //Gnathostomata
        annots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:31"), 
                7776, false, "ECO:0000033", "CIO:0000004", 
                        "DOI:10.1073/pnas.032658599", "ref title 1", 
                        "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21")));
        //other intersect class, we annotate it to whatever higher taxon, here, metazoa
        annots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:33"), 
                33208, false, "ECO:0000033", "CIO:0000004", 
                "DOI:10.1073/pnas.032658599", "ref title 1", 
                "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21")));
        
        
        Set<CuratorAnnotationBean> expectedAnnots = new HashSet<CuratorAnnotationBean>();
        
        //annots inferred from tranformation_of
        String tranfOfSupportTextStart = "Annotation inferred from transformation_of relations "
                + "using annotations to same HOM ID, same NCBI taxon ID, "
                + "same qualifier, and Entity IDs equal to: ";
        
        expectedAnnots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:5"), 
                2759, false, SimilarityAnnotation.AUTOMATIC_ASSERTION_ECO, "CIO:0000003", 
                null, null, tranfOfSupportTextStart + "ID:6", 
                SimilarityAnnotation.AUTOMATIC_ASSIGNED_BY, null, null));
        expectedAnnots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:7"), 
                2759, false, SimilarityAnnotation.AUTOMATIC_ASSERTION_ECO, "CIO:0000003", 
                null, null, tranfOfSupportTextStart + "ID:6", 
                SimilarityAnnotation.AUTOMATIC_ASSIGNED_BY, null, null));
        expectedAnnots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:8"), 
                2759, false, SimilarityAnnotation.AUTOMATIC_ASSERTION_ECO, "CIO:0000003", 
                null, null, tranfOfSupportTextStart + "ID:6", 
                SimilarityAnnotation.AUTOMATIC_ASSIGNED_BY, null, null));
        
        expectedAnnots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:5"), 
                7742, true, SimilarityAnnotation.AUTOMATIC_ASSERTION_ECO, "CIO:0000005", 
                null, null, tranfOfSupportTextStart + "ID:6", 
                SimilarityAnnotation.AUTOMATIC_ASSIGNED_BY, null, null));
        expectedAnnots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:7"), 
                7742, true, SimilarityAnnotation.AUTOMATIC_ASSERTION_ECO, "CIO:0000005", 
                null, null, tranfOfSupportTextStart + "ID:6", 
                SimilarityAnnotation.AUTOMATIC_ASSIGNED_BY, null, null));
        expectedAnnots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:8"), 
                7742, true, SimilarityAnnotation.AUTOMATIC_ASSERTION_ECO, "CIO:0000005", 
                null, null, tranfOfSupportTextStart + "ID:6", 
                SimilarityAnnotation.AUTOMATIC_ASSIGNED_BY, null, null));
        
        expectedAnnots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:5"), 
                7742, false, SimilarityAnnotation.AUTOMATIC_ASSERTION_ECO, "CIO:0000003", 
                null, null, tranfOfSupportTextStart + "ID:4 - ID:6", 
                SimilarityAnnotation.AUTOMATIC_ASSIGNED_BY, null, null));
        expectedAnnots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:7"), 
                7742, false, SimilarityAnnotation.AUTOMATIC_ASSERTION_ECO, "CIO:0000004", 
                null, null, tranfOfSupportTextStart + "ID:6", 
                SimilarityAnnotation.AUTOMATIC_ASSIGNED_BY, null, null));
        expectedAnnots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:8"), 
                7742, false, SimilarityAnnotation.AUTOMATIC_ASSERTION_ECO, "CIO:0000004", 
                null, null, tranfOfSupportTextStart + "ID:6", 
                SimilarityAnnotation.AUTOMATIC_ASSIGNED_BY, null, null));
        
        //annotations inferred from logical constraints
        String constraintsSupportTextStart = "Annotation inferred from logical constraints "
                + "using annotations to same HOM ID and: ";
        
        expectedAnnots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:8"), 
                7742, false, SimilarityAnnotation.AUTOMATIC_ASSERTION_ECO, "CIO:0000005", 
                null, null, 
                constraintsSupportTextStart 
                + SimilarityAnnotationUtils.ENTITY_COL_NAME 
                + ": ID:10, negated: false, taxon ID: 33208"
                + " - " + SimilarityAnnotationUtils.ENTITY_COL_NAME 
                + ": ID:9, negated: false, taxon ID: 7742", 
                SimilarityAnnotation.AUTOMATIC_ASSIGNED_BY, null, null));
        expectedAnnots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:8"), 
                33208, false, SimilarityAnnotation.AUTOMATIC_ASSERTION_ECO, "CIO:0000005", 
                null, null, 
                constraintsSupportTextStart 
                + SimilarityAnnotationUtils.ENTITY_COL_NAME 
                + ": ID:10, negated: false, taxon ID: 33208"
                + " - " + SimilarityAnnotationUtils.ENTITY_COL_NAME 
                + ": ID:9, negated: false, taxon ID: 33208", 
                SimilarityAnnotation.AUTOMATIC_ASSIGNED_BY, null, null));

        expectedAnnots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:23"), 
                32524, false, SimilarityAnnotation.AUTOMATIC_ASSERTION_ECO, "CIO:0000004", 
                null, null, 
                constraintsSupportTextStart 
                + SimilarityAnnotationUtils.ENTITY_COL_NAME 
                    + ": ID:21, negated: false, taxon ID: 32524"
                + " - " + SimilarityAnnotationUtils.ENTITY_COL_NAME 
                    + ": ID:22, negated: false, taxon ID: 7776", 
                SimilarityAnnotation.AUTOMATIC_ASSIGNED_BY, null, null));
        expectedAnnots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:23"), 
                32524, true, SimilarityAnnotation.AUTOMATIC_ASSERTION_ECO, "CIO:0000004", 
                null, null, 
                constraintsSupportTextStart 
                + SimilarityAnnotationUtils.ENTITY_COL_NAME 
                    + ": ID:21, negated: true, taxon ID: 32524"
                + " - " + SimilarityAnnotationUtils.ENTITY_COL_NAME 
                    + ": ID:22, negated: false, taxon ID: 7776", 
                SimilarityAnnotation.AUTOMATIC_ASSIGNED_BY, null, null));
        expectedAnnots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:23"), 
                7742, true, SimilarityAnnotation.AUTOMATIC_ASSERTION_ECO, "CIO:0000003", 
                null, null, 
                constraintsSupportTextStart 
                + SimilarityAnnotationUtils.ENTITY_COL_NAME 
                    + ": ID:21, negated: true, taxon ID: 7711"
                + " - " + SimilarityAnnotationUtils.ENTITY_COL_NAME 
                    + ": ID:22, negated: true, taxon ID: 7742", 
                SimilarityAnnotation.AUTOMATIC_ASSIGNED_BY, null, null));
        //regression test: actually, the following commented annotations should not be generated, 
        //as there exist only positive annotations to Gnathostomata
//        expectedAnnots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:23"), 
//                7776, true, SimilarityAnnotation.AUTOMATIC_ASSERTION_ECO, "CIO:0000004", 
//                null, null, 
//                constraintsSupportTextStart 
//                + SimilarityAnnotationUtils.ENTITY_COL_NAME 
//                    + ": ID:21, negated: true, taxon ID: 7711"
//                + " - " + SimilarityAnnotationUtils.ENTITY_COL_NAME 
//                    + ": ID:22, negated: false, taxon ID: 7776", 
//                SimilarityAnnotation.AUTOMATIC_ASSIGNED_BY, null, null));

        expectedAnnots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:26"), 
                7776, true, SimilarityAnnotation.AUTOMATIC_ASSERTION_ECO, "CIO:0000003", 
                null, null, 
                constraintsSupportTextStart 
                + SimilarityAnnotationUtils.ENTITY_COL_NAME 
                    + ": ID:24, negated: true, taxon ID: 7776"
                + " - " + SimilarityAnnotationUtils.ENTITY_COL_NAME 
                    + ": ID:25, negated: true, taxon ID: 7742", 
                SimilarityAnnotation.AUTOMATIC_ASSIGNED_BY, null, null));


        expectedAnnots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:16", "ID:17"), 
                7742, false, SimilarityAnnotation.AUTOMATIC_ASSERTION_ECO, "CIO:0000004", 
                null, null, 
                constraintsSupportTextStart 
                + SimilarityAnnotationUtils.ENTITY_COL_NAME 
                    + ": ID:11" + Utils.VALUE_SEPARATORS.get(0) + "ID:12, "
                            + "negated: false, taxon ID: 7742"
                + " - " + SimilarityAnnotationUtils.ENTITY_COL_NAME 
                    + ": ID:13, negated: false, taxon ID: 7742"
                + " - " + SimilarityAnnotationUtils.ENTITY_COL_NAME 
                    + ": ID:14" + Utils.VALUE_SEPARATORS.get(0) + "ID:15, "
                            + "negated: false, taxon ID: 7742", 
                SimilarityAnnotation.AUTOMATIC_ASSIGNED_BY, null, null));
        expectedAnnots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:16"), 
                7776, false, SimilarityAnnotation.AUTOMATIC_ASSERTION_ECO, "CIO:0000003", 
                null, null, 
                constraintsSupportTextStart 
                + SimilarityAnnotationUtils.ENTITY_COL_NAME 
                    + ": ID:11, negated: false, taxon ID: 7776"
                + " - " + SimilarityAnnotationUtils.ENTITY_COL_NAME 
                    + ": ID:13, negated: false, taxon ID: 7742"
                + " - " + SimilarityAnnotationUtils.ENTITY_COL_NAME 
                    + ": ID:14" + Utils.VALUE_SEPARATORS.get(0) + "ID:15, "
                            + "negated: false, taxon ID: 7742", 
                SimilarityAnnotation.AUTOMATIC_ASSIGNED_BY, null, null));
        
        //regression test
        expectedAnnots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:29"), 
                7742, false, SimilarityAnnotation.AUTOMATIC_ASSERTION_ECO, "CIO:0000004", 
                null, null, 
                constraintsSupportTextStart 
                + SimilarityAnnotationUtils.ENTITY_COL_NAME 
                    + ": ID:27, negated: false, taxon ID: 7742"
                + " - " + SimilarityAnnotationUtils.ENTITY_COL_NAME 
                    + ": ID:28, negated: false, taxon ID: 7742", 
                SimilarityAnnotation.AUTOMATIC_ASSIGNED_BY, null, null));
        expectedAnnots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:30"), 
                7742, false, SimilarityAnnotation.AUTOMATIC_ASSERTION_ECO, "CIO:0000004", 
                null, null, 
                constraintsSupportTextStart 
                + SimilarityAnnotationUtils.ENTITY_COL_NAME 
                    + ": ID:27, negated: false, taxon ID: 7742"
                + " - " + SimilarityAnnotationUtils.ENTITY_COL_NAME 
                    + ": ID:28, negated: false, taxon ID: 7742", 
                SimilarityAnnotation.AUTOMATIC_ASSIGNED_BY, null, null));
        
        //another regression test
        expectedAnnots.add(new CuratorAnnotationBean("HOM:0000007", Arrays.asList("ID:34"), 
                7776, false, SimilarityAnnotation.AUTOMATIC_ASSERTION_ECO, "CIO:0000004", 
                null, null, 
                constraintsSupportTextStart 
                + SimilarityAnnotationUtils.ENTITY_COL_NAME 
                    + ": ID:31, negated: false, taxon ID: 7776"
                + " - " + SimilarityAnnotationUtils.ENTITY_COL_NAME 
                    + ": ID:33, negated: false, taxon ID: 33208", 
                SimilarityAnnotation.AUTOMATIC_ASSIGNED_BY, null, null));
        
        assertEquals("Incorrect annotations inferred", expectedAnnots, 
                simAnnot.generateInferredAnnotations(annots));
    }
    
    /**
     * Test {@link SimilarityAnnotation#generateRawAnnotations(Collection)}
     */
    @Test
    public void shouldGenerateRawAnnotations() throws OBOFormatParserException, 
    FileNotFoundException, OWLOntologyCreationException, IOException, ParseException {
        SimilarityAnnotation simAnnot = new SimilarityAnnotation(null,
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/fake_uberon.obo").getFile(), 
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/fake_taxonomy.obo").getFile(), 
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/homology_ontology.obo").getFile(), 
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/fake_eco.obo").getFile(), 
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/cio-simple.obo").getFile());
        

        SimpleDateFormat sdf = new SimpleDateFormat(SimilarityAnnotationUtils.DATE_FORMAT);
        List<RawAnnotationBean> expectedAnnots = Arrays.asList(
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
                        "PMID:22431747", "ref title 14", 
                        "supporting text 14", "bgee", "ANN", sdf.parse("2013-07-10")), 
                new RawAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("UBERON:0000926"), Arrays.asList("mesoderm"), 
                        33213, "Bilateria", false, "ECO:0000067", 
                        "developmental similarity evidence", 
                        "CIO:0000004", "medium confidence from single evidence", 
                        "PMID:24281726", "ref title 13", 
                        "supporting text 13", "bgee", "ANN", sdf.parse("2014-01-13")), 
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
                        "supporting text 16", "bgee", "ANN", sdf.parse("2013-10-08")), 
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
                        "supporting text 8", "bgee", "ANN", sdf.parse("2013-09-05")));
        
        assertEquals("Incorrect RAW annotations generated", expectedAnnots, 
                simAnnot.generateRawAnnotations(Arrays.asList(
                        //single evidence
                        new CuratorAnnotationBean("HOM:0000007", Arrays.asList("CL:0000000"), 
                                2759, false, "ECO:0000033", "CIO:0000003", 
                                "DOI:10.1073/pnas.032658599", "ref title 1", 
                                "supporting text 1", "bgee", "ANN", sdf.parse("2013-06-21")), 
                        //congruent evidence lines
                        new CuratorAnnotationBean("HOM:0000007", Arrays.asList("CL:0000015"), 
                                33208, false, "ECO:0000205", "CIO:0000004", 
                                "DOI:10.1002/bies.950161213", "ref title 2", 
                                "supporting text 2", "bgee", "ANN", sdf.parse("2013-08-29")), 
                        new CuratorAnnotationBean("HOM:0000007", Arrays.asList("CL:0000015"),  
                                33208, false, "ECO:0000205", "CIO:0000005", 
                                "ISBN:978-0198566694", "ref title 3", 
                                "supporting text 3", "bgee", "ANN", sdf.parse("2013-08-29")), 
                        //single evidence
                        new CuratorAnnotationBean("HOM:0000007", Arrays.asList("CL:0000037"), 
                                7742, true, "ECO:0000067", "CIO:0000004", 
                                "DOI:10.1146/annurev.cellbio.22.010605.093317", "ref title 4", 
                                "supporting text 4", "bgee", "ANN", sdf.parse("2013-07-01")), 
                        //multiple Uberon IDs
                        new CuratorAnnotationBean("HOM:0000007", 
                                Arrays.asList("CL:0000037", "UBERON:0000001", "UBERON:0000007"), 
                                7742, true, "ECO:0000067", "CIO:0000004", 
                                "DOI:10.1146/annurev.cellbio.22.010605.093317", "ref title 7", 
                                "supporting text 7", "bgee", "ANN", sdf.parse("2013-07-01")), 
                        //case of independent evolution
                        new CuratorAnnotationBean("HOM:0000007 ", Arrays.asList("  UBERON:0010207"), 
                                7776, true, "ECO:0000034", "CIO:0000003 ", 
                                "ISBN:978-0030223693", "ref title 8", 
                                "supporting text 8", "bgee", "ANN", sdf.parse("2013-09-05")), 
                        new CuratorAnnotationBean("HOM:0000007", Arrays.asList("UBERON:0010207"), 
                                7778, false, "ECO:0000034", "CIO:0000003", 
                                "ISBN:978-0030223693", "ref title 8", 
                                "supporting text 8", "bgee", "ANN", sdf.parse("2013-09-05")), 
                        new CuratorAnnotationBean("HOM:0000007", Arrays.asList("UBERON:0010207"), 
                                32524, false, "ECO:0000034", "CIO:0000003", 
                                "ISBN:978-0030223693", "ref title 8", 
                                "supporting text 8", "bgee", "ANN", sdf.parse("2013-09-05")),
                        //heritance of positive annotations, conflicting annotation 
                        //with positive parent annotations
                        new CuratorAnnotationBean(" HOM:0000007", Arrays.asList("  CL:0000216"), 
                                7711, false, "ECO:0000067", "CIO:0000005", 
                                "http://f50006a.eos-intl.net/ELIBSQL12_F50006A_Documents/93grier.pdf", 
                                "ref title 9", 
                                "supporting text 9", "bgee", "ANN", sdf.parse("2015-02-03")), 
                        new CuratorAnnotationBean("HOM:0000007", Arrays.asList("CL:0000216"), 
                                7742, false, "ECO:0000355", "CIO:0000004", 
                                "ISBN:978-0125449045", "ref title 10", 
                                "supporting text 10", "bgee", "ANN", sdf.parse("2015-02-03")), 
                        new CuratorAnnotationBean("HOM:0000007", Arrays.asList("CL:0000216"), 
                                32524, false, "ECO:0000067", "CIO:0000004", 
                                "DOI:10.1002/jemt.1070320602", "ref title 11", 
                                "supporting text 11", "bgee", "ANN", sdf.parse("2015-02-03")), 
                        new CuratorAnnotationBean("HOM:0000007  ", Arrays.asList("CL:0000216 "), 
                                32524, true, "ECO:0000067", "CIO:0000004", 
                                "PMID:17026980", "ref title 12", 
                                "supporting text 12", "bgee", "ANN", sdf.parse("2013-08-30")), 
                        //one low confidence annotation against 2 medium confidence 
                        //annotations => weakly conflicting
                        new CuratorAnnotationBean("HOM:0000007", Arrays.asList("UBERON:0000926"),  
                                33213, false, "ECO:0000067", "CIO:0000004", 
                                "PMID:24281726", "ref title 13", 
                                "supporting text 13", "bgee", "ANN", sdf.parse("2014-01-13")), 
                        new CuratorAnnotationBean(" HOM:0000007", Arrays.asList(" UBERON:0000926"), 
                                33213, false, "ECO:0000067", "CIO:0000004", 
                                "PMID:22431747", "ref title 14", 
                                "supporting text 14", "bgee", "ANN", sdf.parse("2013-07-10")), 
                        new CuratorAnnotationBean("HOM:0000007  ", Arrays.asList("  UBERON:0000926"), 
                                33213, true, "ECO:0000067", "CIO:0000005", 
                                "PMID:12459924", "ref title 15", 
                                "supporting text 15", "bgee", "ANN", sdf.parse("2013-07-10")), 
                        //simply strongly conflicting
                        new CuratorAnnotationBean("HOM:0000007  ", Arrays.asList("  UBERON:0001245"), 
                                33213, false, "ECO:0000355", "CIO:0000004", 
                                "http://dpc.uba.uva.nl/ctz/vol73/nr01/art01", "ref title 16", 
                                "supporting text 16", "bgee", "ANN", sdf.parse("2013-10-08")), 
                        new CuratorAnnotationBean("HOM:0000007", Arrays.asList("UBERON:0001245"), 
                                33213, true, "ECO:0000033", "CIO:0000004", 
                                "http://dpc.uba.uva.nl/ctz/vol73/nr01/art01", "ref title 16", 
                                "supporting text 16", "bgee", "ANN", sdf.parse("2013-10-08")))));
    }
    
    /**
     * Test {@link SimilarityAnnotation#generateSummaryAnnotations(Collection)}
     */
    @Test
    public void shouldGenerateSummaryAnnotations() throws OBOFormatParserException, 
    FileNotFoundException, OWLOntologyCreationException, IOException, 
    IllegalArgumentException, IllegalStateException, ParseException {
        SimilarityAnnotation simAnnot = new SimilarityAnnotation(null,
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/fake_uberon.obo").getFile(), 
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/fake_taxonomy.obo").getFile(), 
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/homology_ontology.obo").getFile(), 
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/fake_eco.obo").getFile(), 
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/cio-simple.obo").getFile());
        
        List<SummaryAnnotationBean> expectedAnnots = Arrays.asList(
                new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                        2759, "Eukaryota", false, 
                        "CIO:0000003", "high confidence from single evidence", true, 
                        null, 1
//                        , 1, 0, 
//                        Arrays.asList("ECO:0000033"), 
//                        Arrays.asList("traceable author statement"), 
//                        null, null, null, null, 
//                        Arrays.asList("bgee")
                        ), 
                new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("CL:0000015"), Arrays.asList("male germ cell"), 
                        33208, "Metazoa", false, 
                        "CIO:0000019", "confidence statement from congruent evidence "
                                + "lines of same type, overall confidence medium", 
                        true, 
                        null, 2
//                        , 2, 0, 
//                        Arrays.asList("ECO:0000205"), 
//                        Arrays.asList("curator inference"), 
//                        null, null, null, null, 
//                        Arrays.asList("bgee", "test db")
                        ), 
                new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("CL:0000037"), Arrays.asList("hematopoietic stem cell"), 
                        7742, "Vertebrata", true, 
                        "CIO:0000004", "medium confidence from single evidence", true, 
                        null, 1
//                        , 0, 1, 
//                        null, null, 
//                        Arrays.asList("ECO:0000067"), 
//                        Arrays.asList("developmental similarity evidence"), 
//                        null, null, 
//                        Arrays.asList("bgee")
                        ), 
                new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("CL:0000037", "UBERON:0000001", "UBERON:0000007"), 
                        Arrays.asList("hematopoietic stem cell", "whatever name1", 
                                "whatever name2"), 
                        7742, "Vertebrata", true, 
                        "CIO:0000004", "medium confidence from single evidence", true, 
                        null, 1
//                        , 0, 1,  
//                        null, null, 
//                        Arrays.asList("ECO:0000067"), 
//                        Arrays.asList("developmental similarity evidence"), 
//                        null, null, 
//                        Arrays.asList("bgee")
                        ), 
                new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("CL:0000216"), Arrays.asList("Sertoli cell"), 
                        7711, "Chordata", false, 
                        "CIO:0000005", "low confidence from single evidence", false, 
                        null, 1
//                                , 1, 0, 
//                                Arrays.asList("ECO:0000067"), 
//                                Arrays.asList("developmental similarity evidence"), 
//                                null, null, null, null, 
//                                Arrays.asList("bgee")
                        ), 
                new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("CL:0000216"), Arrays.asList("Sertoli cell"), 
                        7742, "Vertebrata", false, 
                        "CIO:0000004", "medium confidence from single evidence", 
                        true, 
                        null, 1
//                                , 2, 0, 
//                                Arrays.asList("ECO:0000067", "ECO:0000355"), 
//                                Arrays.asList("developmental similarity evidence", 
//                                        "phylogenetic distribution evidence"), 
//                                null, null, 
//                                Arrays.asList(7711), Arrays.asList("Chordata"), 
//                                Arrays.asList("bgee")
                        ), 
                new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("CL:0000216"), Arrays.asList("Sertoli cell"), 
                        32524, "Amniota", false, 
                        "CIO:0000020", "confidence statement from strongly conflicting "
                                + "evidence lines of same type", 
                        false, 
                        null, 2
//                                , 3, 1, 
//                                Arrays.asList("ECO:0000067", "ECO:0000355"), 
//                                Arrays.asList("developmental similarity evidence", 
//                                        "phylogenetic distribution evidence"), 
//                                Arrays.asList("ECO:0000067"), 
//                                Arrays.asList("developmental similarity evidence"), 
//                                Arrays.asList(7711, 7742), Arrays.asList("Chordata", "Vertebrata"), 
//                                Arrays.asList("bgee")
                        ), 
                new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("UBERON:0000926"), Arrays.asList("mesoderm"), 
                        33213, "Bilateria", false, 
                        "CIO:0000024", "confidence statement from weakly conflicting "
                            + "evidence lines of same type, overall confidence medium", 
                        true, 
                        null, 3
//                                , 2, 1, 
//                                Arrays.asList("ECO:0000067"), 
//                                Arrays.asList("developmental similarity evidence"), 
//                                Arrays.asList("ECO:0000067"), 
//                                Arrays.asList("developmental similarity evidence"), 
//                                null, null, 
//                                Arrays.asList("bgee")
                        ), 
                new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("UBERON:0001245"), Arrays.asList("anus"), 
                        33213, "Bilateria", false, 
                        "CIO:0000010", "confidence statement from strongly conflicting "
                                + "evidence lines of multiple types", 
                        false, 
                        null, 2
//                                , 1, 1,
//                                Arrays.asList("ECO:0000355"), 
//                                Arrays.asList("phylogenetic distribution evidence"), 
//                                Arrays.asList("ECO:0000033"), 
//                                Arrays.asList("traceable author statement"), 
//                                null, null, 
//                                Arrays.asList("bgee")
                        ), 
                new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("UBERON:0010207"), Arrays.asList("nictitating membrane"), 
                        7776, "Gnathostomata", true, 
                        "CIO:0000003", "high confidence from single evidence", true, 
                        null, 1
//                        , 0, 1, 
//                        null, null, 
//                        Arrays.asList("ECO:0000034"), 
//                        Arrays.asList("non-traceable author statement"), 
//                        null, null, 
//                        Arrays.asList("bgee")
                        ), 
                new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("UBERON:0010207"), Arrays.asList("nictitating membrane"), 
                        7778, "Elasmobranchii", false, 
                        "CIO:0000003", "high confidence from single evidence", true, 
                        null, 1
//                        , 1, 0,  
//                        Arrays.asList("ECO:0000034"), 
//                        Arrays.asList("non-traceable author statement"), 
//                        null, null, null, null, 
//                        Arrays.asList("bgee")
                        ), 
                new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("UBERON:0010207"), Arrays.asList("nictitating membrane"), 
                        32524, "Amniota", false, 
                        "CIO:0000003", "high confidence from single evidence", true, 
                        null, 1
//                        , 1, 0, 
//                        Arrays.asList("ECO:0000034"), 
//                        Arrays.asList("non-traceable author statement"), 
//                        null, null, null, null, 
//                        Arrays.asList("bgee")
                        ));

        SimpleDateFormat sdf = new SimpleDateFormat(SimilarityAnnotationUtils.DATE_FORMAT);
        assertEquals("Incorrect SUMMARY annotations generated", expectedAnnots, 
                simAnnot.generateSummaryAnnotations(Arrays.asList(
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
                        "supporting text 16", "bgee", "ANN", sdf.parse("2013-10-08")))));
    }
    
    /**
     * Test {@link SimilarityAnnotation#generateAncestralTaxaAnnotations(Collection)}
     */
    @Test
    public void shouldGenerateAncestralTaxaAnnotations() throws OBOFormatParserException, 
    FileNotFoundException, OWLOntologyCreationException, IOException, 
    IllegalArgumentException, IllegalStateException {
        SimilarityAnnotation simAnnot = new SimilarityAnnotation(null,
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/fake_uberon.obo").getFile(), 
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/fake_taxonomy.obo").getFile(), 
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/homology_ontology.obo").getFile(), 
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/fake_eco.obo").getFile(), 
                SimilarityAnnotationTest.class.getResource(
                        "/similarity_annotations/cio-simple.obo").getFile());
        
        List<AncestralTaxaAnnotationBean> expectedAnnots = Arrays.asList(
                new AncestralTaxaAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                        2759, "Eukaryota", 
                        "CIO:0000003", "high confidence from single evidence", null), 
                new AncestralTaxaAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("CL:0000015"), Arrays.asList("male germ cell"), 
                        33208, "Metazoa", 
                        "CIO:0000019", "confidence statement from congruent evidence "
                                + "lines of same type, overall confidence medium", null), 
                new AncestralTaxaAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("CL:0000037"), 
                        Arrays.asList("hematopoietic stem cell"), 
                        7742, "Vertebrata", 
                        "CIO:0000004", "medium confidence from single evidence", null), 
                new AncestralTaxaAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("CL:0000037", "UBERON:0000001", "UBERON:0000007"), 
                        Arrays.asList("hematopoietic stem cell", "whatever name1", 
                                "whatever name2"), 
                        7742, "Vertebrata", 
                        "CIO:0000004", "medium confidence from single evidence", null), 
                new AncestralTaxaAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("CL:0000216"), Arrays.asList("Sertoli cell"), 
                        7742, "Vertebrata", 
                        "CIO:0000004", "medium confidence from single evidence", 
                        "Alternative homology hypotheses of low confidence exist "
                        + "for taxa: Bilateria, Chordata"), 
                new AncestralTaxaAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("UBERON:0000926"), Arrays.asList("mesoderm"), 
                        33213, "Bilateria", 
                        "CIO:0000024", "confidence statement from weakly conflicting "
                            + "evidence lines of same type, overall confidence medium", null), 
                new AncestralTaxaAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("UBERON:0010207"), Arrays.asList("nictitating membrane"), 
                        7778, "Elasmobranchii", 
                        "CIO:0000003", "high confidence from single evidence", null), 
                new AncestralTaxaAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("UBERON:0010207"), Arrays.asList("nictitating membrane"), 
                        32524, "Amniota", 
                        "CIO:0000003", "high confidence from single evidence", null));
        
        assertEquals("Incorrect ANCESTRAL TAXA annotations generated", expectedAnnots, 
                simAnnot.generateAncestralTaxaAnnotations(Arrays.asList(
                new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("CL:0000000"), Arrays.asList("cell"), 
                        2759, "Eukaryota", false, 
                        "CIO:0000003", "high confidence from single evidence", true, 
                        null, 1
//                                , 1, 0, 
//                                Arrays.asList("ECO:0000033"), 
//                                Arrays.asList("traceable author statement"), 
//                                null, null, null, null, 
//                                Arrays.asList("bgee")
                        ), 
                new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("CL:0000015"), Arrays.asList("male germ cell"), 
                        33208, "Metazoa", false, 
                        "CIO:0000019", "confidence statement from congruent evidence "
                                + "lines of same type, overall confidence medium", 
                        true, 
                        null, 2
//                                , 2, 0, 
//                                Arrays.asList("ECO:0000205"), 
//                                Arrays.asList("curator inference"), 
//                                null, null, null, null, 
//                                Arrays.asList("bgee", "test db")
                        ), 
                new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("CL:0000037"), Arrays.asList("hematopoietic stem cell"), 
                        7742, "Vertebrata", false, 
                        "CIO:0000004", "medium confidence from single evidence", true, 
                        null, 1
//                                , 0, 1, 
//                                null, null, 
//                                Arrays.asList("ECO:0000067"), 
//                                Arrays.asList("developmental similarity evidence"), 
//                                null, null, 
//                                Arrays.asList("bgee")
                        ), 
                new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("CL:0000037", "UBERON:0000001", "UBERON:0000007"), 
                        Arrays.asList("hematopoietic stem cell", "whatever name1", 
                                "whatever name2"), 
                        7742, "Vertebrata", false, 
                        "CIO:0000004", "medium confidence from single evidence", true, 
                        null, 1
//                                , 0, 1,  
//                                null, null, 
//                                Arrays.asList("ECO:0000067"), 
//                                Arrays.asList("developmental similarity evidence"), 
//                                null, null, 
//                                Arrays.asList("bgee")
                        ), 
                new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("CL:0000216"), Arrays.asList("Sertoli cell"), 
                        7711, "Chordata", false, 
                        "CIO:0000005", "low confidence from single evidence", false, 
                        null, 1
//                                        , 1, 0, 
//                                        Arrays.asList("ECO:0000067"), 
//                                        Arrays.asList("developmental similarity evidence"), 
//                                        null, null, null, null, 
//                                        Arrays.asList("bgee")
                        ), 
                new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("CL:0000216"), Arrays.asList("Sertoli cell"), 
                        7742, "Vertebrata", false, 
                        "CIO:0000004", "medium confidence from single evidence", 
                        true, 
                        null, 1
//                                        , 2, 0, 
//                                        Arrays.asList("ECO:0000067", "ECO:0000355"), 
//                                        Arrays.asList("developmental similarity evidence", 
//                                                "phylogenetic distribution evidence"), 
//                                        null, null, 
//                                        Arrays.asList(7711), Arrays.asList("Chordata"), 
//                                        Arrays.asList("bgee")
                        ), 
                new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("CL:0000216"), Arrays.asList("Sertoli cell"), 
                        33213, "Bilateria", false, 
                        "CIO:0000020", "confidence statement from strongly conflicting "
                                + "evidence lines of same type", 
                        false, 
                        null, 2
//                                        , 3, 1, 
//                                        Arrays.asList("ECO:0000067", "ECO:0000355"), 
//                                        Arrays.asList("developmental similarity evidence", 
//                                                "phylogenetic distribution evidence"), 
//                                        Arrays.asList("ECO:0000067"), 
//                                        Arrays.asList("developmental similarity evidence"), 
//                                        Arrays.asList(7711, 7742), Arrays.asList("Chordata", "Vertebrata"), 
//                                        Arrays.asList("bgee")
                        ), 
                new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("UBERON:0000926"), Arrays.asList("mesoderm"), 
                        33213, "Bilateria", false, 
                        "CIO:0000024", "confidence statement from weakly conflicting "
                            + "evidence lines of same type, overall confidence medium", 
                        true, 
                        null, 3
//                                        , 2, 1, 
//                                        Arrays.asList("ECO:0000067"), 
//                                        Arrays.asList("developmental similarity evidence"), 
//                                        Arrays.asList("ECO:0000067"), 
//                                        Arrays.asList("developmental similarity evidence"), 
//                                        null, null, 
//                                        Arrays.asList("bgee")
                        ), 
                new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("UBERON:0001245"), Arrays.asList("anus"), 
                        33213, "Bilateria", false, 
                        "CIO:0000010", "confidence statement from strongly conflicting "
                                + "evidence lines of multiple types", 
                        false, 
                        null, 2
//                                        , 1, 1,
//                                        Arrays.asList("ECO:0000355"), 
//                                        Arrays.asList("phylogenetic distribution evidence"), 
//                                        Arrays.asList("ECO:0000033"), 
//                                        Arrays.asList("traceable author statement"), 
//                                        null, null, 
//                                        Arrays.asList("bgee")
                        ), 
                new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("UBERON:0010207"), Arrays.asList("nictitating membrane"), 
                        7776, "Gnathostomata", true, 
                        "CIO:0000003", "high confidence from single evidence", true, 
                        null, 1
//                                , 0, 1, 
//                                null, null, 
//                                Arrays.asList("ECO:0000034"), 
//                                Arrays.asList("non-traceable author statement"), 
//                                null, null, 
//                                Arrays.asList("bgee")
                        ), 
                new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("UBERON:0010207"), Arrays.asList("nictitating membrane"), 
                        7778, "Elasmobranchii", false, 
                        "CIO:0000003", "high confidence from single evidence", true, 
                        null, 1
//                                , 1, 0,  
//                                Arrays.asList("ECO:0000034"), 
//                                Arrays.asList("non-traceable author statement"), 
//                                null, null, null, null, 
//                                Arrays.asList("bgee")
                        ), 
                new SummaryAnnotationBean("HOM:0000007", "historical homology", 
                        Arrays.asList("UBERON:0010207"), Arrays.asList("nictitating membrane"), 
                        32524, "Amniota", false, 
                        "CIO:0000003", "high confidence from single evidence", true, 
                        null, 1
//                                , 1, 0, 
//                                Arrays.asList("ECO:0000034"), 
//                                Arrays.asList("non-traceable author statement"), 
//                                null, null, null, null, 
//                                Arrays.asList("bgee")
                        ))));
    }
    
    /**
     * Test {@link SimilarityAnnotation.CuratorAnnotationBean#setRefId(String)}, 
     * which extract ref IDs and titles from Strings mixing both.
     */
    @Test
    public void shouldExtractRefIdAndTitle() {
        CuratorAnnotationBean bean = new CuratorAnnotationBean();
        
        String expectedId = "ID:1";
        String expectedTitle = "my great title";
        bean.setRefId("ID:1 my great title");
        assertEquals(expectedId, bean.getRefId());
        assertEquals(expectedTitle, bean.getRefTitle());

        bean.setRefId("ID:1 \"my great title\"");
        assertEquals(expectedId, bean.getRefId());
        assertEquals(expectedTitle, bean.getRefTitle());

        bean.setRefId(" ID:1 ");
        assertEquals(expectedId, bean.getRefId());
        assertNull(bean.getRefTitle());

        bean.setRefId(" ID:1 regression\"test\"");
        assertEquals(expectedId, bean.getRefId());
        assertEquals("regression\"test", bean.getRefTitle());
        
        bean.setRefId("");
        assertNull(bean.getRefId());
        assertNull(bean.getRefTitle());
        
        bean.setRefId(null);
        assertNull(bean.getRefId());
        assertNull(bean.getRefTitle());
    }
    
    /**
     * Test the method {@link SimilarityAnnotation#extractTaxonIdsToFile(String, String)}
     */
    @Test
    public void shouldExtractTaxonIdsToFile() throws FileNotFoundException, IOException {
        String tempFile = testFolder.newFile("taxonIdsOutput.txt").getPath();
        SimilarityAnnotation.extractTaxonIdsToFile(
                this.getClass().getResource("/similarity_annotations/similarity.tsv").getFile(), 
                tempFile);
        Set<Integer> retrievedIds = new HashSet<Integer>();
        int lineCount = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(tempFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                lineCount++;
                retrievedIds.add(Integer.parseInt(line));
            }
        }
        assertEquals("Incorrect number of lines in file", 3, lineCount);
        Set<Integer> expectedIds = new HashSet<Integer>(Arrays.asList(7742, 40674, 1294634));
        assertEquals("Incorrect taxon IDs retrieved from generated file", 
                expectedIds, retrievedIds);
    }
    
    /**
     * Test the method {@link SimilarityAnnotation#getAnatEntitiesWithNoTransformationOf(String, String)}
     */
    @Test
    public void shouldGetAnatEntitiesWithNoTransformationOf() 
            throws UnknownOWLOntologyException, IllegalArgumentException, 
            FileNotFoundException, OWLOntologyCreationException, 
            OBOFormatParserException, IOException {
        
        ParserWrapper parserWrapper = new ParserWrapper();
        parserWrapper.setCheckOboDoc(false);
        try (OWLGraphWrapper fakeOntology = new OWLGraphWrapper(parserWrapper.parse(
            this.getClass().getResource("/similarity_annotations/fake_uberon.obo").getFile()))) {
        
            Set<OWLClass> expectedClasses = new HashSet<OWLClass>(
                    Arrays.asList(fakeOntology.getOWLClassByIdentifierNoAltIds("ID:1"), 
                            fakeOntology.getOWLClassByIdentifierNoAltIds("ID:4")));
            
            assertEquals(
                "Incorrect anatomical entities with no transformation_of relations identified", 
                expectedClasses, SimilarityAnnotation.getAnatEntitiesWithNoTransformationOf(
                this.getClass().getResource("/similarity_annotations/similarity.tsv").getFile(), 
                this.getClass().getResource("/similarity_annotations/fake_uberon.obo").getFile()));
        }
    }
    
    /**
     * Test the method {@link 
     * SimilarityAnnotation#writeAnatEntitiesWithNoTransformationOfToFile(String, String, String)}
     * @throws OBOFormatParserException 
     * @throws OWLOntologyCreationException 
     * @throws IllegalArgumentException 
     * @throws UnknownOWLOntologyException 
     */
    @Test
    public void shouldExtractAnatEntitiesWithNoTransformationOfToFile() 
            throws FileNotFoundException, IOException, UnknownOWLOntologyException, 
            IllegalArgumentException, OWLOntologyCreationException, OBOFormatParserException {
        String tempFile = testFolder.newFile("anatEntitiesNoTransfOfOutput.txt").getPath();
        SimilarityAnnotation.writeAnatEntitiesWithNoTransformationOfToFile(
                this.getClass().getResource("/similarity_annotations/similarity.tsv").getFile(), 
                this.getClass().getResource("/similarity_annotations/fake_uberon.obo").getFile(), 
                tempFile);
        Set<String> retrievedEntities = new HashSet<String>();
        int lineCount = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(tempFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                //skip the first line that is supposed to be a header line
                lineCount++;
                if (lineCount == 1) {
                    continue;
                }
                retrievedEntities.add(line);
            }
        }
        //we should have 3 lines: one header line, and 2 lines with data
        assertEquals("Incorrect number of lines in file", 3, lineCount);
        Set<String> expectedEntities = new HashSet<String>(Arrays.asList("ID:1\tuberon 1\t" +
        		"develops from: ID:3 uberon 3", 
        		"ID:4\tuberon 4\t"));
        assertEquals("Incorrect anatomical entities IDs retrieved from generated file", 
                expectedEntities, retrievedEntities);
    }
    
}
