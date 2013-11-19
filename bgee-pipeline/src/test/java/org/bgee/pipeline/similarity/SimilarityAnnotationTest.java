package org.bgee.pipeline.similarity;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.TestAncestor;
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
     * Test the method {@link SimilarityAnnotation#extractTaxonIds(String)}
     */
    @Test
    public void shouldExtractTaxonIds() throws FileNotFoundException, IOException {
        Set<Integer> expectedIds = new HashSet<Integer>(Arrays.asList(7742, 40674, 1294634));
        assertEquals("Incorrect taxon IDs extract from similarity annotation file", 
                expectedIds, new SimilarityAnnotation().extractTaxonIds(
                        this.getClass().getResource("/similarity/homology.tsv").getFile()));
    }
    
    /**
     * Test the method {@link SimilarityAnnotation#extractTaxonIdsTiFile(String, String)}
     */
    @Test
    public void shouldExtractTaxonIdsToFile() throws FileNotFoundException, IOException {
        String tempFile = testFolder.newFile("taxonIdsOutput.txt").getPath();
        new SimilarityAnnotation().extractTaxonIdsToFile(
                this.getClass().getResource("/similarity/homology.tsv").getFile(), 
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
