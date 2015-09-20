package org.bgee.pipeline.uberon;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.TestAncestor;
import org.bgee.pipeline.Utils;
import org.bgee.pipeline.ontologycommon.OntologyUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
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

import owltools.graph.OWLGraphWrapper;
import owltools.mooncat.SpeciesSubsetterUtil;

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
            getResource("/uberon/fakeTaxonomy.owl").getPath();
    
    /**
     * A {@code Set} of {@code Integer}s that are the NCBI IDs contained in the file 
     * {@link #TAXONFILE}.
     */
    private final static Set<Integer> TAXONIDS = new HashSet<>(Arrays.asList(8, 13, 14, 15));
    
    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder();

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
     * {@link TaxonConstraints#generateTaxonConstraints(Map, String)} with intermediate 
     * simplifications for some taxa.
     */
    @Test
    public void shouldGenerateTaxonConstraintsWithIntermediateSteps() throws IOException, 
        UnknownOWLOntologyException, OWLOntologyCreationException, 
        OBOFormatParserException, IllegalArgumentException, OWLOntologyStorageException {

        File tempDir = testFolder.getRoot().getAbsoluteFile();
        
        //first, we test that if we request a pre-filtering using a taxon related 
        //to the main taxon for which constraints should be generated, an exception is thrown
        try {
            Map<Integer, List<Integer>> taxIdsWithPrefiltering = new HashMap<Integer, List<Integer>>();
            taxIdsWithPrefiltering.put(14, Arrays.asList(13));
            TaxonConstraints generate = new TaxonConstraints(
                    new OWLGraphWrapper(OntologyUtils.loadOntology(UBERONFILE)), 
                    new OWLGraphWrapper(OntologyUtils.loadOntology(TAXONTFILE)));
            generate.generateTaxonConstraints(
                            taxIdsWithPrefiltering, testFolder.getRoot().getPath());
            
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError("No exception was thrown when using related taxa."));
        } catch (IllegalArgumentException e) {
            //test passed
        }
        
        //now, test the correct behavior. 
        
        //use a LinkedHashMap to have predictable generation order for the tests
        Map<Integer, List<Integer>> taxIdsWithPrefiltering = 
                TAXONIDS.stream().collect(Collectors.toMap(Function.identity(), 
                //if the taxon is NCBITaxon:8 or NCBITaxon:15, 
                //we request to first pre-filter the ontology for structures 
                //existing in unrelated taxa, by steps (first pre-filtering 
                //for taxon NCBITaxon:14, then for taxon NCBITaxon:13)
                e -> {
                    if (e == 8 || e == 15) {
                        return Arrays.asList(14, 13);
                    }
                    return new ArrayList<Integer>();
                }, 
                (u, v) -> {throw new IllegalStateException("Duplicate key: " + u);}, 
                LinkedHashMap::new));

        //spy on the SpeciesSubsetterUtils used, to check that intermediate simplifications 
        //are correctly performed. We inject a custom supplier returning spied objects. 
        List<SpeciesSubsetterUtil> spiedSubsetters = new ArrayList<SpeciesSubsetterUtil>();
        TaxonConstraints generate = new TaxonConstraints(
                new OWLGraphWrapper(OntologyUtils.loadOntology(UBERONFILE)), 
                new OWLGraphWrapper(OntologyUtils.loadOntology(TAXONTFILE)), 
                e -> {
                    SpeciesSubsetterUtil spiedSubsetter = spy(new SpeciesSubsetterUtil(e));
                    spiedSubsetters.add(spiedSubsetter);
                    return spiedSubsetter;
                });
        
        Map<String, Set<Integer>> constraints = generate.generateTaxonConstraints(
                taxIdsWithPrefiltering, testFolder.getRoot().getPath());
        
        //check use of SpeciesSubsetterUtils
        assertEquals("Incorrect number of taxon constraint analyses performed", 8, 
                spiedSubsetters.size());
        //it would be nice to check the actual taxon studied for each SpeciesSubsetterUtil, 
        //but there is no method to obtain it, so we'll do without this check. 
        //first subsetter, simplification step for taxon 8 using taxon 14
        verify(spiedSubsetters.get(0), never()).removeOtherSpecies();
        verify(spiedSubsetters.get(0)).removeSpecies();
        //second subsetter, simplification step for taxon 8 using taxon 13
        verify(spiedSubsetters.get(1), never()).removeOtherSpecies();
        verify(spiedSubsetters.get(1)).removeSpecies();
        //third subsetter, generate taxon constraints for taxon 8
        verify(spiedSubsetters.get(2)).removeOtherSpecies();
        verify(spiedSubsetters.get(2), never()).removeSpecies();
        //fourth subsetter, generate taxon constraints for taxon 13, no simplification steps
        verify(spiedSubsetters.get(3)).removeOtherSpecies();
        verify(spiedSubsetters.get(3), never()).removeSpecies();
        //fifth subsetter, generate taxon constraints for taxon 14, no simplification steps
        verify(spiedSubsetters.get(4)).removeOtherSpecies();
        verify(spiedSubsetters.get(4), never()).removeSpecies(); 
        //sixth subsetter, simplification step for taxon 15 using taxon 14
        verify(spiedSubsetters.get(5), never()).removeOtherSpecies();
        verify(spiedSubsetters.get(5)).removeSpecies();
        //seventh subsetter, simplification step for taxon 15 using taxon 13
        verify(spiedSubsetters.get(6), never()).removeOtherSpecies();
        verify(spiedSubsetters.get(6)).removeSpecies();
        //eighth subsetter, generate taxon constraints for taxon 15
        verify(spiedSubsetters.get(7)).removeOtherSpecies();
        verify(spiedSubsetters.get(7), never()).removeSpecies();

        //now, check resulting taxon constraints and intermediate ontologies
        
        assertEquals("Incorrect number of OWLClasses in taxon constraints", 23, 
                constraints.keySet().size());

        //now iterate the Uberon OWLClass IDs
        for (String classId: constraints.keySet()) {
            Set<Integer> toCompare;
            if (classId.equals("U:22") || classId.equals("U:23") || classId.equals("U:24")) {
                //U:22 antenna never_in_taxon NCBITaxon:8 - exists in taxa 13, 14, and 15.
                //note that this is also test that incorrect relations between taxa 
                //are removed from Uberon (in Uberon, NCBITaxon:13 is NCBITaxon:8, 
                //not in the provided taxonomy ontology).
                //U:23 subclass of U:22 - exists in taxa 13, 14, and 15
                //U:24 subclass of U:23 - exists in taxa 13, 14, and 15
                toCompare = new HashSet<Integer>(Arrays.asList(13, 14, 15));
            } else if (classId.equals("S:998") || classId.equals("S:9") || classId.equals("S:12")) {
                //S:998 never_in_taxon NCBITaxon:13 - NCBITaxon:14 subClassOf NCBITaxon:13 
                // => exists in taxa 8 and 15
                //note that this will also test that disjoint classes axioms are removed 
                //from Uberon (in Uberon, NCBITaxon:13 disjoint_from NCBITaxon:14).
                //S:9 subclass of S:998 - exists in taxa 8 and 15
                //S:12 intersection_of: S:6 and of part_of S:998 - exists in taxa 8 and 15
                toCompare = new HashSet<Integer>(Arrays.asList(8, 15));
            } else if (classId.equals("U:25") || classId.equals("U:26")) {
                //U:25  - only in taxon 14 - exists in taxon 13 and 14
                //U:26 subclass of U:25 - exists in taxon 13 and 14
                toCompare = new HashSet<Integer>(Arrays.asList(13, 14));
            } else {
                toCompare = TAXONIDS;
            }
             
            assertEquals("Incorrect value in taxon constraints for class " + 
                    classId, toCompare, constraints.get(classId));
        }

        this.checkIntermediateOntology(8, 
                constraints.keySet().stream()
                .filter(e -> !e.equals("U:22") && !e.equals("U:23") && !e.equals("U:24") && 
                             !e.equals("U:25") && !e.equals("U:26"))
                .collect(Collectors.toSet()), 
                tempDir);
        this.checkIntermediateOntology(13, 
                constraints.keySet().stream()
                .filter(e -> !e.equals("S:998") && !e.equals("S:9") && !e.equals("S:12"))
                .collect(Collectors.toSet()), 
                tempDir);
        this.checkIntermediateOntology(14, 
                constraints.keySet().stream()
                .filter(e -> !e.equals("S:998") && !e.equals("S:9") && !e.equals("S:12"))
                .collect(Collectors.toSet()), 
                tempDir);
        this.checkIntermediateOntology(15, 
                constraints.keySet().stream()
                .filter(e -> !e.equals("U:25") && !e.equals("U:26"))
                .collect(Collectors.toSet()), 
                tempDir);
    }
    
    /**
     * Test the method 
     * {@link TaxonConstraints#generateTaxonConstraints(Map, String)} with no intermediate 
     * simplifications.
     */
    @Test
    public void shouldGenerateTaxonConstraints() throws IOException, 
        UnknownOWLOntologyException, OWLOntologyCreationException, 
        OBOFormatParserException, IllegalArgumentException, OWLOntologyStorageException {
        
        File tempDir = testFolder.getRoot().getAbsoluteFile();
        //use a LinkedHashMap to have predictable generation order for the tests. 
        //value associated to each key is an empty list (no simplification steps requested)
        Map<Integer, List<Integer>> taxIdsWithPrefiltering = 
                TAXONIDS.stream().collect(Collectors.toMap(Function.identity(), ArrayList::new, 
                (u, v) -> {throw new IllegalStateException("Duplicate key: " + u);}, 
                LinkedHashMap::new));

        //spy on the SpeciesSubsetterUtils used, to check no intermediate simplifications 
        //are performed. We inject a custom supplier returning spied objects. 
        List<SpeciesSubsetterUtil> spiedSubsetters = new ArrayList<SpeciesSubsetterUtil>();
        TaxonConstraints generate = new TaxonConstraints(
                new OWLGraphWrapper(OntologyUtils.loadOntology(UBERONFILE)), 
                new OWLGraphWrapper(OntologyUtils.loadOntology(TAXONTFILE)), 
                e -> {
                    SpeciesSubsetterUtil spiedSubsetter = spy(new SpeciesSubsetterUtil(e));
                    spiedSubsetters.add(spiedSubsetter);
                    return spiedSubsetter;
                });
        
        Map<String, Set<Integer>> constraints = generate.generateTaxonConstraints(
                taxIdsWithPrefiltering, testFolder.getRoot().getPath());
        
        //check use of SpeciesSubsetterUtils
        assertEquals("Incorrect number of taxon constraint analyses performed", 4, 
                spiedSubsetters.size());
        //it would be nice to check the actual taxon studied for each SpeciesSubsetterUtil, 
        //but there is no method to obtain it, so we'll do without this check. 
        //first subsetter, generate taxon constraints for taxon 8
        verify(spiedSubsetters.get(0)).removeOtherSpecies();
        verify(spiedSubsetters.get(0), never()).removeSpecies();
        //second subsetter, generate taxon constraints for taxon 13, no simplification steps
        verify(spiedSubsetters.get(1)).removeOtherSpecies();
        verify(spiedSubsetters.get(1), never()).removeSpecies();
        //third subsetter, generate taxon constraints for taxon 14, no simplification steps
        verify(spiedSubsetters.get(2)).removeOtherSpecies();
        verify(spiedSubsetters.get(2), never()).removeSpecies(); 
        //fourth subsetter, generate taxon constraints for taxon 15
        verify(spiedSubsetters.get(3)).removeOtherSpecies();
        verify(spiedSubsetters.get(3), never()).removeSpecies();

        //now, check resulting taxon constraints and intermediate ontologies

        assertEquals("Incorrect number of OWLClasses in taxon constraints", 23, 
                constraints.keySet().size());

        //now iterate the Uberon OWLClass IDs
        for (String classId: constraints.keySet()) {
            Set<Integer> toCompare;
            if (classId.equals("U:22") || classId.equals("U:23") || classId.equals("U:24")) {
                //U:22 antenna never_in_taxon NCBITaxon:8 - exists in taxa 13, 14, and 15.
                //note that this is also test that incorrect relations between taxa 
                //are removed from Uberon (in Uberon, NCBITaxon:13 is NCBITaxon:8, 
                //not in the provided taxonomy ontology).
                //U:23 subclass of U:22 - exists in taxa 13, 14, and 15
                //U:24 subclass of U:23 - exists in taxa 13, 14, and 15
                toCompare = new HashSet<Integer>(Arrays.asList(13, 14, 15));
            } else if (classId.equals("S:998") || classId.equals("S:9") || classId.equals("S:12")) {
                //S:998 never_in_taxon NCBITaxon:13 - NCBITaxon:14 subClassOf NCBITaxon:13 
                // => exists in taxa 8 and 15
                //note that this will also test that disjoint classes axioms are removed 
                //from Uberon (in Uberon, NCBITaxon:13 disjoint_from NCBITaxon:14).
                //S:9 subclass of S:998 - exists in taxa 8 and 15
                //S:12 intersection_of: S:6 and of part_of S:998 - exists in taxa 8 and 15
                toCompare = new HashSet<Integer>(Arrays.asList(8, 15));
            } else if (classId.equals("U:25") || classId.equals("U:26")) {
                //U:25  - only in taxon 14 - exists in taxon 13 and 14
                //U:26 subclass of U:25 - exists in taxon 13 and 14
                toCompare = new HashSet<Integer>(Arrays.asList(13, 14));
            } else {
                toCompare = TAXONIDS;
            }
             
            assertEquals("Incorrect value in taxon constraints for class " + 
                    classId, toCompare, constraints.get(classId));
        }

        this.checkIntermediateOntology(8, 
                constraints.keySet().stream()
                .filter(e -> !e.equals("U:22") && !e.equals("U:23") && !e.equals("U:24") && 
                             !e.equals("U:25") && !e.equals("U:26"))
                .collect(Collectors.toSet()), 
                tempDir);
        this.checkIntermediateOntology(13, 
                constraints.keySet().stream()
                .filter(e -> !e.equals("S:998") && !e.equals("S:9") && !e.equals("S:12"))
                .collect(Collectors.toSet()), 
                tempDir);
        this.checkIntermediateOntology(14, 
                constraints.keySet().stream()
                .filter(e -> !e.equals("S:998") && !e.equals("S:9") && !e.equals("S:12"))
                .collect(Collectors.toSet()), 
                tempDir);
        this.checkIntermediateOntology(15, 
                constraints.keySet().stream()
                .filter(e -> !e.equals("U:25") && !e.equals("U:26"))
                .collect(Collectors.toSet()), 
                tempDir);
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
        log.entry(taxonId, expectedClassIds, directory);
        
        File ontFile = null;
        ontFile = new File(directory, "uberon_subset" + taxonId + ".owl");
        try (OWLGraphWrapper wrapper = 
                new OWLGraphWrapper(OntologyUtils.loadOntology(ontFile.getPath()))) {
            //the taxonomy ontology will have been added in this intermediate ontology as well
            assertEquals("Incorrect OWLCLasses contained in intermediate ontology, "
                    + "contained classes: " + wrapper.getAllOWLClasses(), 
                    expectedClassIds.size() + TAXONIDS.size(), 
                    wrapper.getAllOWLClasses().size());
            for (String classId: expectedClassIds) {
                assertNotNull("Missing class " + classId, 
                        wrapper.getOWLClassByIdentifier(classId));
            }
        }
        log.exit();
    }
    
    /**
     * Test the method 
     * {@link TaxonConstraints#generateTaxonConstraints(String, Map, String, String)}
     */
    @Test
    public void shouldGenerateTaxonConstraintsTSV() throws IOException, 
        UnknownOWLOntologyException, IllegalArgumentException, 
        OWLOntologyCreationException, OBOFormatParserException, 
        OWLOntologyStorageException {
        
        String outputTSV = testFolder.newFile("table.tsv").getPath();

        TaxonConstraints generate = new TaxonConstraints(UBERONFILE, TAXONTFILE);
        generate.generateTaxonConstraints(TAXONFILE, null, outputTSV, null);

        //now read the TSV file
        try (ICsvMapReader mapReader = new CsvMapReader(
                new FileReader(outputTSV), Utils.TSVCOMMENTED)) {
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
                if (uberonId.equals("U:25")) {
                    assertEquals("Incorrect name for U:25", "left antenna2", 
                            uberonName);
                }
                if (uberonId.equals("U:26")) {
                    assertEquals("Incorrect name for U:26", "left antenna segment2", 
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
                } else if (uberonId.equals("U:25") || uberonId.equals("U:26")) {
                    taxon1ExpectedValue = false;
                    taxon4ExpectedValue = false;
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
            assertEquals("Incorrect number of lines in TSV output", 23, i);
        }
      
    }
    
    /**
     * Test the method {@link TaxonConstraints#extractTaxonIds(String)}
     */
    @Test
    public void shouldExtractTaxonIdsFromFile() throws FileNotFoundException, IOException {
        Set<Integer> expectedTaxonIds = new HashSet<Integer>(Arrays.asList(10, 15, 16, 19));
        assertEquals(expectedTaxonIds, TaxonConstraints.extractTaxonIds(
                this.getClass().getResource("/uberon/taxonConstraints.tsv").getPath()));
    }

    /**
     * Test the method {@link TaxonConstraints#extractTaxonIds(Map)}
     */
    @Test
    public void shouldExtractTaxonIdsFromConstraints() {
        Map<String, Set<Integer>> constraints = new HashMap<String, Set<Integer>>();
        String clsId1 = "id1";
        String clsId2 = "id2";
        String clsId3 = "id3";
        String clsId4 = "id4";
        String clsId5 = "id5_1_5";
        constraints.put(clsId1, new HashSet<Integer>(Arrays.asList(10, 15, 16, 19)));
        constraints.put(clsId2, new HashSet<Integer>(Arrays.asList(10, 15, 16)));
        constraints.put(clsId3, new HashSet<Integer>(Arrays.asList(10, 15)));
        constraints.put(clsId4, new HashSet<Integer>(Arrays.asList(10)));
        constraints.put(clsId5, new HashSet<Integer>());
        
        assertEquals(new HashSet<Integer>(Arrays.asList(10, 15, 16, 19)), 
                TaxonConstraints.extractTaxonIds(constraints));
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
        String clsId5 = "id5_1_5";
        expectedConstraints.put(clsId1, new HashSet<Integer>(Arrays.asList(10, 15, 16, 19)));
        expectedConstraints.put(clsId2, new HashSet<Integer>(Arrays.asList(10, 15, 16)));
        expectedConstraints.put(clsId3, new HashSet<Integer>(Arrays.asList(10, 15)));
        expectedConstraints.put(clsId4, new HashSet<Integer>(Arrays.asList(10)));
        expectedConstraints.put(clsId5, new HashSet<Integer>());
        
        assertEquals(expectedConstraints, TaxonConstraints.extractTaxonConstraints(
                this.getClass().getResource("/uberon/taxonConstraints.tsv").getPath()));
    }
    
    /**
     * Test the method {@link TaxonConstraints#extractTaxonConstraints(String, Map)}
     */
    @Test
    public void shouldExtractTaxonConstraintsWithReplacement() 
            throws FileNotFoundException, IOException {
        Map<String, Set<Integer>> expectedConstraints = new HashMap<String, Set<Integer>>();
        String clsId1 = "id1";
        String clsId2 = "id2";
        String clsId3 = "id3";
        String clsId4 = "id4";
        String clsId5 = "id5_1_5";
        expectedConstraints.put(clsId1, new HashSet<Integer>(Arrays.asList(10, 15, 16, 19)));
        expectedConstraints.put(clsId2, new HashSet<Integer>(Arrays.asList(10, 15, 16)));
        expectedConstraints.put(clsId3, new HashSet<Integer>(Arrays.asList(10, 15)));
        expectedConstraints.put(clsId4, new HashSet<Integer>(Arrays.asList(10)));
        expectedConstraints.put(clsId5, new HashSet<Integer>(Arrays.asList(10)));
        
        Map<String, Set<Integer>> replacementConstrains = new HashMap<String, Set<Integer>>();
        replacementConstrains.put("id5", new HashSet<Integer>(Arrays.asList(10, 15)));
        replacementConstrains.put("id5_1", new HashSet<Integer>(Arrays.asList(10)));
        
        assertEquals(expectedConstraints, TaxonConstraints.extractTaxonConstraints(
                this.getClass().getResource("/uberon/taxonConstraints.tsv").getPath(), 
                replacementConstrains));
    }
    
    //@Test
    public void test() throws UnknownOWLOntologyException, OWLOntologyCreationException, OBOFormatParserException, IOException {
        TaxonConstraints tc = new TaxonConstraints(
                "/Users/admin/Desktop/composite-metazoan.owl", 
                "/Users/admin/Desktop/bgee_ncbitaxon.owl");
//        log.info(tc.explainTaxonExistence(Arrays.asList("FBdv:00005342", "FBdv:00005343"), 
//                Arrays.asList(7227)));
//        log.info(tc.explainTaxonExistence(Arrays.asList("HsapDv:0000009"), 
//                Arrays.asList(9606)));
        log.info(tc.explainTaxonExistence(Arrays.asList("HsapDv:0000011", "HsapDv:0000013"), 
                Arrays.asList(9606)));
    }
}
