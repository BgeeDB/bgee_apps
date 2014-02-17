package org.bgee.pipeline.uberon;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.OntologyUtils;
import org.bgee.pipeline.TestAncestor;
import org.junit.Test;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;
import org.supercsv.cellprocessor.ParseBool;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import owltools.graph.OWLGraphWrapper;

/**
 * Unit tests for {@link TaxonConstraints}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class TaxonConstraintsTest extends TestAncestor {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(TaxonConstraintsTest.class.getName());
    
    /**
     * A {@code String} that is the path from the classpath to the fake taxon  
     * file, containing the NCBI IDs of the fake taxa to use for the tests.
     */
    private final static String TAXONFILE = 
            TaxonConstraintsTest.class.getResource("/uberon/taxa.tsv").getPath();
    /**
     * A {@code String} that is the path from the classpath to the fake Uberon   
     * ontology used for the tests.
     */
    private final static String UBERONFILE = TaxonConstraintsTest.class.
            getResource("/uberon/taxonConstraintsTest.obo").getPath();
    /**
     * A {@code String} that is the path from the classpath to the fake NCBI    
     * taxonomy ontology used for the tests.
     */
    private final static String TAXONTFILE = TaxonConstraintsTest.class.
            getResource("/uberon/fakeTaxonomy.obo").getPath();
    
    /**
     * A {@code Set} of {@code Integer}s that are the NCBI IDs contained in the file 
     * {@link #TAXONFILE}.
     */
    private final static Set<Integer> TAXONIDS = 
            new HashSet<Integer>(Arrays.asList(8, 13, 14, 15));

    /**
     * Default Constructor. 
     */
    public TaxonConstraintsTest() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test the method 
     * {@link TaxonConstraints#generateTaxonConstraints(String, Set, String)}.
     */
    @Test
    public void shouldGenerateTaxonConstraints() throws IOException, 
        UnknownOWLOntologyException, OWLOntologyCreationException, 
        OBOFormatParserException, IllegalArgumentException, OWLOntologyStorageException {
        TaxonConstraints generate = new TaxonConstraints(
                OntologyUtils.loadOntology(UBERONFILE), 
                OntologyUtils.loadOntology(TAXONTFILE));
        File tempDir = null;
        try {
            tempDir = Files.createTempDirectory(null).toFile();
            Map<String, Set<Integer>> constraints = generate.generateTaxonConstraints(
                    TAXONIDS, tempDir.getPath());
            
            assertEquals("Incorrect number of OWLClasses in taxon constraints", 21, 
                    constraints.keySet().size());

            //U:22 antenna never_in_taxon NCBITaxon:8 - exists in taxa 13, 14, and 15.
            //note that this is also test that incorrect relations between taxa 
            //are removed from Uberon (in Uberon, NCBITaxon:13 is NCBITaxon:8, 
            //not in the provided taxonomy ontology).
            //U:23 subclass of U:22 - exists in taxa 13, 14, and 15
            //U:24 subclass of U:23 - exists in taxa 13, 14, and 15
            Set<Integer> expectedTaxa1 = new HashSet<Integer>(Arrays.asList(13, 14, 15));
            Set<String> expectedClassIds1 = new HashSet<String>(constraints.keySet());
            expectedClassIds1.removeAll(Arrays.asList("U:22", "U:23", "U:24"));

            //S:998 never_in_taxon NCBITaxon:13 - NCBITaxon:14 subClassOf NCBITaxon:13 
            // => exists in taxa 8 and 15
            //note that this will also test that disjoint classes axioms are removed 
            //from Uberon (in Uberon, NCBITaxon:13 disjoint_from NCBITaxon:14).
            //S:9 subclass of S:998 - exists in taxa 8 and 15
            //S:12 intersection_of: S:6 and of part_of S:998 - exists in taxa 8 and 15
            Set<Integer> expectedTaxa2 = new HashSet<Integer>(Arrays.asList(8, 15));
            Set<String> expectedClassIds2 = new HashSet<String>(constraints.keySet());
            expectedClassIds2.removeAll(Arrays.asList("S:998", "S:9", "S:12"));
            
            //now iterate the Uberon OWLClass IDs
            for (String classId: constraints.keySet()) {
                Set<Integer> toCompare = TAXONIDS;
                if (!expectedClassIds1.contains(classId)) {
                    toCompare = expectedTaxa1;
                } else if (!expectedClassIds2.contains(classId)) {
                    toCompare = expectedTaxa2;
                } 
                assertEquals("Incorrect value in taxon constraints for class " + 
                    classId, toCompare, constraints.get(classId));
            }
            
            this.checkIntermediateOntology(8, expectedClassIds1, tempDir);
            this.checkIntermediateOntology(13, expectedClassIds2, tempDir);
            //taxon 14 is subclass of taxon 13, so we expect the exact same results
            this.checkIntermediateOntology(14, expectedClassIds2, tempDir);
            this.checkIntermediateOntology(15, constraints.keySet(), tempDir);
        } finally {
            if (tempDir != null) {
                for (File child: tempDir.listFiles()) {
                    if (!child.delete()) {
                        log.warn("File could not be deleted: {}", child);
                    }
                }
                if (!tempDir.delete()) {
                    log.warn("Temp directory not deleted: {}", tempDir);
                }
            }
        }
    }
    
    /**
     * Checks that the intermediate ontology file generated for the taxon with ID 
     * {@code taxonId} contains  all the {@code OWLClass}es with their OBO-like 
     * ID in {@code expectedClassIds}, and only those {@code OWLClass}es. 
     * Otherwise, an {@code AssertionError} is thrown. 
     * 
     * @param taxonId           A {@code int} that is the ID of the taxon for which 
     *                          we want to check the intermediate ontology file generated.
     * @param expectedClassIds  A {@code Set} of {@code String}s that are the OBO-like 
     *                          IDs of the allowed {@code OWLClass}es.
     * @param directory         A {@code File} that is the directory where generated 
     *                          ontologies are stored.
     * @throws AssertionError               If the ontology is not as expected.
     * @throws OWLOntologyCreationException If an error occurred while loading 
     *                                      the ontology.
     * @throws OBOFormatParserException     If an error occurred while loading 
     *                                      the ontology.
     * @throws IOException                  If an error occurred while loading 
     *                                      the ontology.
     */
    private void checkIntermediateOntology(int taxonId, Set<String> expectedClassIds, 
            File directory) 
        throws AssertionError, OWLOntologyCreationException, OBOFormatParserException, 
        IOException {
        
        File ontFile = null;
        ontFile = new File(directory, "uberon_subset" + taxonId + ".owl");
        OWLGraphWrapper wrapper = 
                new OWLGraphWrapper(OntologyUtils.loadOntology(ontFile.getPath()));
        //the taxonomy ontology will have been added in this intermediate ontology as well
        assertEquals("Incorrect OWLCLasses contained in intermediate ontology", 
                wrapper.getAllOWLClasses().size(), 
                expectedClassIds.size() + TAXONIDS.size());
        for (String classId: expectedClassIds) {
            assertNotNull("Missing class " + classId, 
                    wrapper.getOWLClassByIdentifier(classId));
        }
    }
    
    /**
     * Test the method 
     * {@link TaxonConstraints#generateTaxonConstraints(String, String, String, String)}
     */
    @Test
    public void shouldGenerateTaxonConstraintsTSV() throws IOException, 
        UnknownOWLOntologyException, IllegalArgumentException, 
        OWLOntologyCreationException, OBOFormatParserException, 
        OWLOntologyStorageException {
        
        File tempDir = null;
        try {
            tempDir = Files.createTempDirectory(null).toFile();
            String outputTSV = new File(tempDir, "table.tsv").getPath();

            TaxonConstraints generate = new TaxonConstraints(UBERONFILE, TAXONTFILE);
            generate.generateTaxonConstraints(TAXONFILE, outputTSV, null);
            
            //now read the TSV file
            try (ICsvMapReader mapReader = new CsvMapReader(
                    new FileReader(outputTSV), CsvPreference.TAB_PREFERENCE)) {
                String[] headers = mapReader.getHeader(true); 
                final CellProcessor[] processors = new CellProcessor[] {
                        new NotNull(new UniqueHashCode()), 
                        new NotNull(), 
                        new NotNull(new ParseBool("t", "f")), 
                        new NotNull(new ParseBool("t", "f")), 
                        new NotNull(new ParseBool("t", "f")), 
                        new NotNull(new ParseBool("t", "f"))};
                
                Map<String, Object> taxonConstraintMap;
                int i = 0;
                while( (taxonConstraintMap = mapReader.read(headers, processors)) != null ) {
                    log.trace("Row: {}", taxonConstraintMap);
                    i++;
                    String uberonId = (String) taxonConstraintMap.get(headers[0]);
                    String uberonName = (String) taxonConstraintMap.get(headers[1]);
                    if (uberonId.equals("U:22")) {
                        assertEquals("Incorrect name for U:22", "antenna", uberonName);
                    }
                    if (uberonId.equals("U:23")) {
                        assertEquals("Incorrect name for U:23", "left antenna", uberonName);
                    }
                    if (uberonId.equals("U:24")) {
                        assertEquals("Incorrect name for U:24", "left antenna segment", 
                                uberonName);
                    }
                    if (uberonId.equals("S:998")) {
                        assertEquals("Incorrect name for S:998", "Cephalus Obscurus", 
                                uberonName);
                    }
                    if (uberonId.equals("S:9")) {
                        assertEquals("Incorrect name for S:9", "Cell of Cephalus Obscurus", 
                                uberonName);
                    }
                    if (uberonId.equals("S:12")) {
                        assertEquals("Incorrect name for S:12", "Cephalus Obscurus Astrocyte", 
                                uberonName);
                    }
                    boolean taxon1ExpectedValue = true;
                    boolean taxon2ExpectedValue = true;
                    boolean taxon3ExpectedValue = true;
                    boolean taxon4ExpectedValue = true;
                    if (uberonId.equals("U:22") || uberonId.equals("U:23") || 
                            uberonId.equals("U:24")) {
                        taxon1ExpectedValue = false;
                    } else if (uberonId.equals("S:998") || uberonId.equals("S:9") || 
                            uberonId.equals("S:12")) {
                        taxon2ExpectedValue = false;
                        taxon3ExpectedValue = false;
                    }
                    assertEquals("Incorrect value for Uberon ID " + uberonId + 
                            " and taxon 8", taxon1ExpectedValue, 
                            taxonConstraintMap.get(headers[2]));
                    assertEquals("Incorrect value for Uberon ID " + uberonId + 
                            " and taxon 13", taxon2ExpectedValue, 
                            taxonConstraintMap.get(headers[3]));
                    assertEquals("Incorrect value for Uberon ID " + uberonId + 
                            " and taxon 14", taxon3ExpectedValue, 
                            taxonConstraintMap.get(headers[4]));
                    assertEquals("Incorrect value for Uberon ID " + uberonId + 
                            " and taxon 15", taxon4ExpectedValue, 
                            taxonConstraintMap.get(headers[5]));
                }
                assertEquals("Incorrect number of lines in TSV output", 21, i);
            }
        } finally {
            if (tempDir != null) {
                for (File child: tempDir.listFiles()) {
                    if (!child.delete()) {
                        log.warn("File could not be deleted: {}", child);
                    }
                }
                if (!tempDir.delete()) {
                    log.warn("Temp directory not deleted: {}", tempDir);
                }
            }
        }
    }
    
    /**
     * Test the method {@link TaxonConstraints#extractTaxonIds(String)}
     */
    @Test
    public void shouldExtractTaxonIds() throws FileNotFoundException, IOException {
        Set<Integer> expectedTaxonIds = new HashSet<Integer>(Arrays.asList(10, 15, 16, 19));
        assertEquals(expectedTaxonIds, new TaxonConstraints().extractTaxonIds(
                this.getClass().getResource("/uberon/taxonConstraints.tsv").getPath()));
    }
    
    /**
     * Test the method {@link TaxonConstraints#extractTaxonConstraints(String)}
     */
    @Test
    public void shouldExtractTaxonConstraints() throws FileNotFoundException, IOException {
        Map<String, Set<Integer>> expectedConstraints = new HashMap<String, Set<Integer>>();
        String clsId1 = "id1";
        String clsId2 = "id2";
        String clsId3 = "id3";
        String clsId4 = "id4";
        String clsId5 = "id5";
        expectedConstraints.put(clsId1, new HashSet<Integer>(Arrays.asList(10, 15, 16, 19)));
        expectedConstraints.put(clsId2, new HashSet<Integer>(Arrays.asList(10, 15, 16)));
        expectedConstraints.put(clsId3, new HashSet<Integer>(Arrays.asList(10, 15)));
        expectedConstraints.put(clsId4, new HashSet<Integer>(Arrays.asList(10)));
        expectedConstraints.put(clsId5, new HashSet<Integer>());
        
        assertEquals(expectedConstraints, new TaxonConstraints().extractTaxonConstraints(
                this.getClass().getResource("/uberon/taxonConstraints.tsv").getPath()));
    }
    
}
