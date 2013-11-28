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
import org.bgee.pipeline.annotations.SimilarityAnnotation;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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
     * Test {@link SimilarityAnnotation#extractAnnotations()}.
     */
    @Test
    public void shouldExtractAnnotations() throws ParseException, 
        FileNotFoundException, IOException {
        
        List<Map<String, Object>> expectedAnnots = new ArrayList<Map<String, Object>>();
        
        Map<String, Object> row1 = new HashMap<String, Object>();
        row1.put(SimilarityAnnotation.ENTITY_COL_NAME, "entity1");
        row1.put(SimilarityAnnotation.ENTITY_NAME_COL_NAME, "entityName1");
        row1.put(SimilarityAnnotation.QUALIFIER_COL_NAME, null);
        row1.put(SimilarityAnnotation.HOM_COL_NAME, "HOM:1");
        row1.put(SimilarityAnnotation.HOM_NAME_COL_NAME, "HOMName1");
        row1.put(SimilarityAnnotation.REF_COL_NAME, "myRef:1");
        row1.put(SimilarityAnnotation.REF_TITLE_COL_NAME, "myRefTitle1");
        row1.put(SimilarityAnnotation.ECO_COL_NAME, "ECO:1");
        row1.put(SimilarityAnnotation.ECO_NAME_COL_NAME, "ECOName1");
        row1.put(SimilarityAnnotation.CONF_COL_NAME, "CONF:1");
        row1.put(SimilarityAnnotation.CONF_NAME_COL_NAME, "CONFName1");
        row1.put(SimilarityAnnotation.TAXON_COL_NAME, 1);
        row1.put(SimilarityAnnotation.TAXON_NAME_COL_NAME, "taxon:1");
        row1.put(SimilarityAnnotation.SUPPORT_TEXT_COL_NAME, "blabla1");
        row1.put(SimilarityAnnotation.ASSIGN_COL_NAME, "bgee1");
        row1.put(SimilarityAnnotation.CURATOR_COL_NAME, "me1");
        row1.put(SimilarityAnnotation.DATE_COL_NAME, 
                new SimpleDateFormat("yyyy-MM-dd").parse("1984-01-01"));
        expectedAnnots.add(row1);
        
        Map<String, Object> row2 = new HashMap<String, Object>();
        row2.put(SimilarityAnnotation.ENTITY_COL_NAME, "entity2");
        row2.put(SimilarityAnnotation.ENTITY_NAME_COL_NAME, null);
        row2.put(SimilarityAnnotation.QUALIFIER_COL_NAME, "NOT");
        row2.put(SimilarityAnnotation.HOM_COL_NAME, "HOM:2");
        row2.put(SimilarityAnnotation.HOM_NAME_COL_NAME, null);
        row2.put(SimilarityAnnotation.REF_COL_NAME, "myRef:2");
        row2.put(SimilarityAnnotation.REF_TITLE_COL_NAME, null);
        row2.put(SimilarityAnnotation.ECO_COL_NAME, null);
        row2.put(SimilarityAnnotation.ECO_NAME_COL_NAME, null);
        row2.put(SimilarityAnnotation.CONF_COL_NAME, "CONF:2");
        row2.put(SimilarityAnnotation.CONF_NAME_COL_NAME, null);
        row2.put(SimilarityAnnotation.TAXON_COL_NAME, 2);
        row2.put(SimilarityAnnotation.TAXON_NAME_COL_NAME, null);
        row2.put(SimilarityAnnotation.SUPPORT_TEXT_COL_NAME, null);
        row2.put(SimilarityAnnotation.ASSIGN_COL_NAME, "bgee2");
        row2.put(SimilarityAnnotation.CURATOR_COL_NAME, null);
        row2.put(SimilarityAnnotation.DATE_COL_NAME, null);
        expectedAnnots.add(row2);
        
        assertEquals(expectedAnnots, new SimilarityAnnotation().extractAnnotations(
                this.getClass().getResource("/annotations/similarity2.tsv").getFile()));
    }
    
    /**
     * Test the method {@link SimilarityAnnotation#extractTaxonIds(String)}
     */
    @Test
    public void shouldExtractTaxonIds() throws FileNotFoundException, IOException {
        Set<Integer> expectedIds = new HashSet<Integer>(Arrays.asList(7742, 40674, 1294634));
        assertEquals("Incorrect taxon IDs extract from similarity annotation file", 
                expectedIds, new SimilarityAnnotation().extractTaxonIds(
                        this.getClass().getResource("/annotations/similarity.tsv").getFile()));
    }
    
    /**
     * Test the method {@link SimilarityAnnotation#extractTaxonIdsToFile(String, String)}
     */
    @Test
    public void shouldExtractTaxonIdsToFile() throws FileNotFoundException, IOException {
        String tempFile = testFolder.newFile("taxonIdsOutput.txt").getPath();
        new SimilarityAnnotation().extractTaxonIdsToFile(
                this.getClass().getResource("/annotations/similarity.tsv").getFile(), 
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
    
}
