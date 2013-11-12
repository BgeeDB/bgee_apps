package org.bgee.pipeline.uberon;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.TestAncestor;
import org.junit.Test;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;

/**
 * Unit tests for {@link GenerateTaxonConstraints}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class GenerateTaxonConstraintsTest extends TestAncestor {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(GenerateTaxonConstraintsTest.class.getName());
    
    /**
     * A {@code String} that is the path from the classpath to the fake taxon  
     * file, containing the NCBI IDs of the fake taxa to use for the tests.
     */
    private final static String TAXONFILE = 
            GenerateTaxonConstraintsTest.class.getResource("/uberon/taxa.tsv").getPath();
    /**
     * A {@code String} that is the path from the classpath to the fake Uberon   
     * ontology used for the tests.
     */
    private final static String ONTOLOGYFILE = GenerateTaxonConstraintsTest.class.
            getResource("/uberon/taxonConstraintsTest.obo").getPath();
    
    /**
     * A {@code Set} of {@code Integer}s that are the NCBI IDs contained in the file 
     * {@link #TAXONFILE}.
     */
    private final static Set<Integer> TAXONIDS = 
            new HashSet<Integer>(Arrays.asList(8, 13, 15));

    /**
     * Default Constructor. 
     */
    public GenerateTaxonConstraintsTest() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test the method 
     * {@link GenerateTaxonConstraints.generateTaxonConstraints(String, Set, String)}.
     */
    @Test
    public void generateTaxonConstraints() throws IOException, 
        UnknownOWLOntologyException, OWLOntologyCreationException, 
        OBOFormatParserException {
        GenerateTaxonConstraints generate = new GenerateTaxonConstraints();
        File tempDir = null;
        try {
            tempDir = Files.createTempDirectory(null).toFile();
            Map<String, Set<Integer>> constraints = generate.generateTaxonConstraints(
                    ONTOLOGYFILE, TAXONIDS, tempDir.getPath());
            
        } finally {
            if (tempDir != null) {
                tempDir.delete();
            }
        }
    }
    
}
