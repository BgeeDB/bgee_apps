package org.bgee.pipeline.uberon;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    /**
     * A {@code Set} of {@code String}s that are the OBO-like IDs of the Uberon terms 
     * for which we want to generate taxon constraints. "U:6" is present in the ontology 
     * but not in this {@code Set}, so it will be discarded. "FAKE:1" and "FAKE:2" are not present 
     * in the ontology but present in this {@code Set}, they should be considered 
     * as existing in all taxa. "FAKE:100" is present in an imported ontology, 
     * this is a regression test to check that the imported ontologies are correctly merged 
     * for the reasoner to "see" them; it exists in all taxa.
     */
    private final static Set<String> UBERON_IDS = new HashSet<>(Arrays.asList(
            "UBERON:0001062", "U:1", "U:2", "U:3", "U:4", "U:5", "U:100", 
            "U:22", "U:23", "U:24", "U:25", "U:26", 
            "S:1", "S:2", "S:3", "S:4", "S:5", "S:6", "S:9", "S:12", "S:998", "S:999", "S:1000", 
            "FAKE:1", "FAKE:2", "FAKE:100"));
    /**
     * A {@code Map} where keys are {@code String}s representing prefixes of uberon terms 
     * to match, the associated value being a {@code Set} of {@code Integer}s 
     * to replace taxon constraints of matching terms.
     */
    private final static Map<String, Set<Integer>> ID_STARTS_TO_OVERRIDING_TAX_IDS = Stream.of(
            //should match S:9 only, not S:998 nor S:999 because there is a longest match
            new SimpleEntry<String, Set<Integer>>("S:9", new HashSet<>(Arrays.asList(8))), 
            //should match both S:998 and S:999. Also, test with a taxon not existing 
            //in taxon ontology (should be discarded)
            new SimpleEntry<String, Set<Integer>>("S:99", new HashSet<>(Arrays.asList(8, 13, 100))), 
            //taxon should be expanded to also include NCBITaxon:13, parent of NCBITaxon:14
            new SimpleEntry<String, Set<Integer>>("S:1000", new HashSet<>(Arrays.asList(8, 14))), 
            //class not present in the ontology but requested anyway
            new SimpleEntry<String, Set<Integer>>("FAKE:2", new HashSet<>(Arrays.asList(8))), 
            //class not present in the ontology nor requested, should not appear in taxon constraints
            new SimpleEntry<String, Set<Integer>>("FAKE:3", new HashSet<>(Arrays.asList(8))))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    
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
     * {@link TaxonConstraints#generateTaxonConstraints(Map, Set, Map, String)} with intermediate 
     * simplifications for some taxa.
     */
    @Test
    public void shouldGenerateTaxonConstraintsWithIntermediateSteps() throws IOException, 
        UnknownOWLOntologyException, OWLOntologyCreationException, 
        OBOFormatParserException, IllegalArgumentException, OWLOntologyStorageException {
        
        //first, we test that if we request a pre-filtering using a taxon related 
        //to the main taxon for which constraints should be generated, an exception is thrown
        try {
            Map<Integer, List<Integer>> taxIdsWithPrefiltering = new HashMap<Integer, List<Integer>>();
            taxIdsWithPrefiltering.put(14, Arrays.asList(13));
            TaxonConstraints generate = new TaxonConstraints(
                    new OWLGraphWrapper(OntologyUtils.loadOntology(UBERONFILE)), 
                    new OWLGraphWrapper(OntologyUtils.loadOntology(TAXONTFILE)));
            generate.generateTaxonConstraints(taxIdsWithPrefiltering, UBERON_IDS, 
                    ID_STARTS_TO_OVERRIDING_TAX_IDS, testFolder.getRoot().getPath());
            
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
                taxIdsWithPrefiltering, UBERON_IDS, 
                ID_STARTS_TO_OVERRIDING_TAX_IDS, testFolder.getRoot().getPath());
        
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
        this.checkConstraints(constraints);
    }
    
    /**
     * Test the method 
     * {@link TaxonConstraints#generateTaxonConstraints(Map, Set, Map, String)} 
     * with no intermediate simplifications.
     */
    @Test
    public void shouldGenerateTaxonConstraints() throws IOException, 
        UnknownOWLOntologyException, OWLOntologyCreationException, 
        OBOFormatParserException, IllegalArgumentException, OWLOntologyStorageException {
        
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
                taxIdsWithPrefiltering, UBERON_IDS, 
                ID_STARTS_TO_OVERRIDING_TAX_IDS, testFolder.getRoot().getPath());
        
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
        this.checkConstraints(constraints);
    }
    
    /**
     * Test the method {@link TaxonConstraints#explainTaxonExistence(Collection, Collection)}.
     * @throws IOException 
     * @throws OWLOntologyCreationException 
     * @throws UnknownOWLOntologyException 
     * @throws OBOFormatParserException 
     * @throws OWLOntologyStorageException 
     * @throws IllegalArgumentException 
     */
    @Test
    //TODO: continue test, does not work with "never_in_taxon" from OBO, doesn't seem to be expanded to equivalent to OWL:Nothing
    public void shouldExplainTaxonExistence() throws OBOFormatParserException, UnknownOWLOntologyException, 
        OWLOntologyCreationException, IOException, IllegalArgumentException, OWLOntologyStorageException {
        TaxonConstraints explain = new TaxonConstraints(
                new OWLGraphWrapper(OntologyUtils.loadOntology(UBERONFILE)), 
                new OWLGraphWrapper(OntologyUtils.loadOntology(TAXONTFILE)));
        explain.explainAndPrintTaxonExistence(Arrays.asList("U:23"), Arrays.asList(8), log::info);
        
        Map<Integer, List<Integer>> taxIds = new HashMap<Integer, List<Integer>>();
        taxIds.put(8, null);
        
        explain.generateTaxonConstraints(taxIds, UBERON_IDS, null, testFolder.getRoot().getPath());
        
        explain = new TaxonConstraints(
                new OWLGraphWrapper(OntologyUtils.loadOntology(testFolder.getRoot().getPath() + "/uberon_reasoning_source.owl")), 
                new OWLGraphWrapper(OntologyUtils.loadOntology(TAXONTFILE)));
        explain.explainAndPrintTaxonExistence(Arrays.asList("U:23"), Arrays.asList(8), log::info);
    }
    
    /**
     * Check the expected constraints generated by {@link #shouldGenerateTaxonConstraints()} and 
     * {@link #shouldGenerateTaxonConstraintsWithIntermediateSteps}, and check the intermediate 
     * ontology produced by using {@link #checkIntermediateOntology(int, Set, File)}. 
     * 
     * @param constraints   A {@cod Map} representing the taxon constraints generated 
     *                      by the caller method.
     */
    private void checkConstraints(Map<String, Set<Integer>> constraints) 
            throws OBOFormatParserException, OWLOntologyCreationException, AssertionError, 
            IOException {

        assertEquals("Incorrect number of OWLClasses in taxon constraints", 26, 
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
                
            } else if (classId.equals("S:12")) {
                //S:998 never_in_taxon NCBITaxon:13 - NCBITaxon:14 subClassOf NCBITaxon:13 
                // => exists in taxa 8 and 15
                //note that this will also test that disjoint classes axioms are removed 
                //from Uberon (in Uberon, NCBITaxon:13 disjoint_from NCBITaxon:14).
                //S:9 subclass of S:998 - exists in taxa 8 and 15
                //S:12 intersection_of: S:6 and of part_of S:998 - exists in taxa 8 and 15. 
                //But taxon constraints of S:9 and S:998 are overridden, so they are not present 
                //in this condition
                toCompare = new HashSet<Integer>(Arrays.asList(8, 15));
                
            } else if (classId.equals("U:25") || classId.equals("U:26")) {
                //U:25  - only in taxon 14 - exists in taxon 13 and 14
                //U:26 subclass of U:25 - exists in taxon 13 and 14
                toCompare = new HashSet<Integer>(Arrays.asList(13, 14));
                
            } else if (classId.equals("S:9") || classId.equals("FAKE:2")) {
                //S:9 supposed to exist in taxa 8 and 15, but constraints overridden.
                //FAKE:2 not present in Uberon ontology but requested anyway, 
                //supposed to exist in all taxa, but constraints overridden. 
                toCompare = new HashSet<Integer>(Arrays.asList(8));
                
            } else if (classId.equals("S:998") || classId.equals("S:999")) {
                //S:998 supposed to exist in taxa 8 and 15, but constraints overridden. 
                //S:999 supposed to exist in all taxa, but constraints overridden.
                //This also tests that the overriding taxon 100, not part of the taxonomy ontology, 
                //is correctly discarded.
                toCompare = new HashSet<Integer>(Arrays.asList(8, 13));
                
            } else if (classId.equals("S:1000")) {
                //S:1000 supposed to exist in all taxa, but constraints overridden. 
                //This also tests that the overriding taxon 14 is correctly expanded 
                //to also include parent taxon 13.
                toCompare = new HashSet<Integer>(Arrays.asList(8, 13, 14));
                
            } else if (classId.equals("U:100")) {
                //U:100 exists in none of the requested taxa
                toCompare = new HashSet<Integer>();
                
            } else if (classId.equals("UBERON:0001062") || classId.equals("U:1") || 
                    classId.equals("U:2") || classId.equals("U:3") || classId.equals("U:4") || 
                    classId.equals("U:5") || classId.equals("S:1") || classId.equals("S:2") || 
                    classId.equals("S:3") || classId.equals("S:4") || classId.equals("S:5") || 
                    classId.equals("S:6") || classId.equals("FAKE:1") || classId.equals("FAKE:100")) {
                //FAKE:1 not present in Uberon ontology but requested anyway, constraints 
                //not overridden => will be defined as existing in all taxa, with a warning log.
                //FAKE:100 is present in an imported ontology, it is a regression test to check 
                //that imported ontologies are correctly merged for the reasoner; it exists 
                //in all taxa.
                toCompare = TAXONIDS;
                
            } else {
                //Notably, U:6 is present in the Uberon ontology, but is not requested, 
                //should be discarded. Taxa present in the taxonomy ontology should also be discarded. 
                throw log.throwing(new AssertionError("Class not supposed to be present "
                        + "in taxon constraints: " + classId));
            }
             
            assertEquals("Incorrect value in taxon constraints for class " + 
                    classId, toCompare, constraints.get(classId));
        }

        File tempDir = testFolder.getRoot().getAbsoluteFile();
        
        Set<String> expectedIdsInOntology = new HashSet<String>(UBERON_IDS);
        //U:6 was discarded from the requested classes, but is present in the ontology
        expectedIdsInOntology.add("U:6");
        //FAKE:1 and FAKE:2 were requested, but are not present in the ontology
        expectedIdsInOntology.remove("FAKE:1");
        expectedIdsInOntology.remove("FAKE:2");
        //Now we chek the intermediate ontology. Of course, this does not take into account 
        //overriding taxon constraints, as the override is performed after producing the ontologies. 
        this.checkIntermediateOntology(8, expectedIdsInOntology.stream()
                .filter(e -> !e.equals("U:22") && !e.equals("U:23") && !e.equals("U:24") && 
                             !e.equals("U:25") && !e.equals("U:26") && 
                             !e.equals("U:100"))
                .collect(Collectors.toSet()), 
                tempDir);
        this.checkIntermediateOntology(13, expectedIdsInOntology.stream()
                .filter(e -> !e.equals("S:998") && !e.equals("S:9") && !e.equals("S:12") && 
                        !e.equals("U:100"))
                .collect(Collectors.toSet()), 
                tempDir);
        this.checkIntermediateOntology(14, expectedIdsInOntology.stream()
                .filter(e -> !e.equals("S:998") && !e.equals("S:9") && !e.equals("S:12") && 
                        !e.equals("U:100"))
                .collect(Collectors.toSet()), 
                tempDir);
        this.checkIntermediateOntology(15, expectedIdsInOntology.stream()
                .filter(e -> !e.equals("U:25") && !e.equals("U:26") && 
                        !e.equals("U:100"))
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
                    expectedClassIds.size() + TAXONIDS.size() 
                    //+1 because there is an additional taxon in the taxonomy ontology 
                    //for which we don't want to generate constraints (NCBITaxon:100)
                    + 1, 
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
     * {@link TaxonConstraints#generateTaxonConstraints(String, Map, String, Map, String, String)}
     */
    @Test
    public void shouldGenerateTaxonConstraintsTSV() throws IOException, 
        UnknownOWLOntologyException, IllegalArgumentException, 
        OWLOntologyCreationException, OBOFormatParserException, 
        OWLOntologyStorageException {
        
        String outputTSV = testFolder.newFile("table.tsv").getPath();

        TaxonConstraints generate = new TaxonConstraints(UBERONFILE, TAXONTFILE);
        generate.generateTaxonConstraints(TAXONFILE, null, TaxonConstraintsTest.class.
                getResource("/uberon/taxonConstraintsTest_allClasses.obo").getPath(), 
                ID_STARTS_TO_OVERRIDING_TAX_IDS, outputTSV, null);

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
                if (uberonId.equals("UBERON:0001062")) {
                    assertEquals("Incorrect name for UBERON:0001062", "anatomical entity", 
                            uberonName);
                } else if (uberonId.equals("U:1")) {
                    assertEquals("Incorrect name for U:1", "anatomical structure", 
                            uberonName);
                } else if (uberonId.equals("U:2")) {
                    assertEquals("Incorrect name for U:2", "brain", 
                            uberonName);
                } else if (uberonId.equals("U:3")) {
                    assertEquals("Incorrect name for U:3", "forebrain", 
                            uberonName);
                } else if (uberonId.equals("U:4")) {
                    assertEquals("Incorrect name for U:4", "forebrain astrocyte", 
                            uberonName);
                } else if (uberonId.equals("U:22")) {
                    assertEquals("Incorrect name for U:22", "antenna", uberonName);
                } else if (uberonId.equals("U:23")) {
                    assertEquals("Incorrect name for U:23", "left antenna", uberonName);
                } else if (uberonId.equals("U:24")) {
                    assertEquals("Incorrect name for U:24", "left antenna segment", 
                            uberonName);
                } else if (uberonId.equals("U:25")) {
                    assertEquals("Incorrect name for U:25", "left antenna2", 
                            uberonName);
                } else if (uberonId.equals("U:26")) {
                    assertEquals("Incorrect name for U:26", "left antenna segment2", 
                            uberonName);
                } else if (uberonId.equals("U:5")) {
                    assertEquals("Incorrect name for U:5", "cell", 
                            uberonName);
                } else if (uberonId.equals("S:1")) {
                    assertEquals("Incorrect name for S:1", "anatomical_entity", 
                            uberonName);
                } else if (uberonId.equals("S:2")) {
                    assertEquals("Incorrect name for S:2", "Brain", 
                            uberonName);
                } else if (uberonId.equals("S:3")) {
                    assertEquals("Incorrect name for S:3", "ForeBrain", 
                            uberonName);
                } else if (uberonId.equals("S:4")) {
                    assertEquals("Incorrect name for S:4", "Forebrain Astrocyte", 
                            uberonName);
                } else if (uberonId.equals("S:12")) {
                    assertEquals("Incorrect name for S:12", "Cephalus Obscurus Astrocyte", 
                            uberonName);
                } else if (uberonId.equals("S:5")) {
                    assertEquals("Incorrect name for S:5", "Cell", 
                            uberonName);
                } else if (uberonId.equals("S:6")) {
                    assertEquals("Incorrect name for S:6", "Astrocyte", 
                            uberonName);
                } else if (uberonId.equals("S:999")) {
                    assertEquals("Incorrect name for S:999", "Musculus Obscurus", 
                            uberonName);
                } else if (uberonId.equals("S:1000")) {
                    assertEquals("Incorrect name for S:1000", "Left Musculus Obscurus", 
                            uberonName);
                } else if (uberonId.equals("S:998")) {
                    assertEquals("Incorrect name for S:998", "Cephalus Obscurus", 
                            uberonName);
                } else if (uberonId.equals("S:9")) {
                    assertEquals("Incorrect name for S:9", "Cell of Cephalus Obscurus", 
                            uberonName);
                } else if (uberonId.equals("U:100")) {
                    assertEquals("Incorrect name for U:100", "structure x", 
                            uberonName);
                } else if (uberonId.equals("FAKE:1")) {
                    assertEquals("Incorrect name for FAKE:1", "Fake 1", 
                            uberonName);
                } else if (uberonId.equals("FAKE:2")) {
                    assertEquals("Incorrect name for FAKE:2", "Fake 2", 
                            uberonName);
                } else if (uberonId.equals("FAKE:100")) {
                    assertEquals("Incorrect name for FAKE:100", "Fake 100", 
                            uberonName);
                } else {
                    throw log.throwing(new AssertionError("Unrecognized class ID: " + uberonId));
                }
                
                boolean taxon8ExpectedValue = true;
                boolean taxon13ExpectedValue = true;
                boolean taxon14ExpectedValue = true;
                boolean taxon15ExpectedValue = true;
                if (uberonId.equals("U:22") || uberonId.equals("U:23") || uberonId.equals("U:24")) {
                    taxon8ExpectedValue = false;
                    
                } else if (uberonId.equals("S:12")) {
                    taxon13ExpectedValue = false;
                    taxon14ExpectedValue = false;
                    
                } else if (uberonId.equals("U:25") || uberonId.equals("U:26")) {
                    taxon8ExpectedValue = false;
                    taxon15ExpectedValue = false;
                    
                } else if (uberonId.equals("S:9") || uberonId.equals("FAKE:2")) {
                    taxon13ExpectedValue = false;
                    taxon14ExpectedValue = false;
                    taxon15ExpectedValue = false;
                    
                } else if (uberonId.equals("S:998") || uberonId.equals("S:999")) {
                    taxon14ExpectedValue = false;
                    taxon15ExpectedValue = false;
                    
                } else if (uberonId.equals("S:1000")) {
                    taxon15ExpectedValue = false;
                    
                } else if (uberonId.equals("U:100")) {
                    taxon8ExpectedValue = false;
                    taxon13ExpectedValue = false;
                    taxon14ExpectedValue = false;
                    taxon15ExpectedValue = false;
                    
                } 
                assertEquals("Incorrect value for Uberon ID " + uberonId + 
                        " and taxon 8", taxon8ExpectedValue, 
                        taxonConstraintMap.get(headers[2]));
                assertEquals("Incorrect value for Uberon ID " + uberonId + 
                        " and taxon 13", taxon13ExpectedValue, 
                        taxonConstraintMap.get(headers[3]));
                assertEquals("Incorrect value for Uberon ID " + uberonId + 
                        " and taxon 14", taxon14ExpectedValue, 
                        taxonConstraintMap.get(headers[4]));
                assertEquals("Incorrect value for Uberon ID " + uberonId + 
                        " and taxon 15", taxon15ExpectedValue, 
                        taxonConstraintMap.get(headers[5]));
            }
            assertEquals("Incorrect number of lines in TSV output", 26, i);
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
}
