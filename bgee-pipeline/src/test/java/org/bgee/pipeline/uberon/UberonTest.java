package org.bgee.pipeline.uberon;

import static org.junit.Assert.*;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.OntologyUtils;
import org.bgee.pipeline.OntologyUtilsTest;
import org.bgee.pipeline.TestAncestor;
import org.bgee.pipeline.Utils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;

import owltools.graph.OWLGraphWrapper;

/**
 * Unit tests for {@link Uberon}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class UberonTest extends TestAncestor {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(UberonTest.class.getName());
    
    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder();

    /**
     * Default Constructor. 
     */
    public UberonTest() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test the method {@link Uberon#extractTaxonIds(String)}.
     */
    @Test
    public void shouldExtractTaxonIds() throws OWLOntologyCreationException, 
        OBOFormatParserException, IllegalArgumentException, IOException {
        
        Set<Integer> expectedTaxonIds = new HashSet<Integer>();
        //this one should be obtained from the 
        //oboInOwl:treat-xrefs-as-reverse-genus-differentia ontology annotations
        expectedTaxonIds.add(1); 
        //those should be obtained from the object properties
        expectedTaxonIds.addAll(Arrays.asList(2, 3, 4, 13)); 
        //those should be obtained from annotation properties
        expectedTaxonIds.addAll(Arrays.asList(5, 6, 7, 8, 9, 10, 11, 12));
        
        assertEquals("Incorrect taxon IDs extracted", expectedTaxonIds, 
                new Uberon().extractTaxonIds(
                this.getClass().getResource("/uberon/uberonTaxonTest.owl").getPath()));
    }
    
    /**
     * Test the method {@link Uberon#saveSimplificationInfo(OWLOntology, String, Map)}.
     * @throws IOException 
     * @throws OBOFormatParserException 
     * @throws OWLOntologyCreationException 
     */
    @Test
    public void shouldSaveSimplificationInfo() throws OWLOntologyCreationException, 
        OBOFormatParserException, IOException {
        Uberon uberonTest = new Uberon();
        OWLOntology uberonOnt = OntologyUtils.loadOntology(
                this.getClass().getResource("/uberon/simplifyInfoTest.obo").getPath());
        
        //U:4 is obsolete and should not be displayed in the file
        Map<String, String> classesRemoved = new HashMap<String, String>();
        classesRemoved.put("U:4", "reason 4");
        classesRemoved.put("U:3", "reason 3");
        classesRemoved.put("U:2", "reason 2");
        classesRemoved.put("U:23", "reason 23");
        String tempFile = testFolder.newFile("simplifyInfo.tsv").getPath();
        uberonTest.saveSimplificationInfo(uberonOnt, tempFile, classesRemoved);
        
        //now, read the generated TSV file
        int i = 0;
        try (ICsvMapReader mapReader = new CsvMapReader(
                new FileReader(tempFile), Utils.TSVCOMMENTED)) {
            String[] headers = mapReader.getHeader(true); 
            final CellProcessor[] processors = new CellProcessor[] {
                    new UniqueHashCode(new NotNull()), //Uberon ID
                    new NotNull(), //Uberon name
                    new Optional(), //relations
                    new NotNull()}; //reason for removal

            Map<String, Object> infoMap;
            while( (infoMap = mapReader.read(headers, processors)) != null ) {
                log.trace("Row: {}", infoMap);
                String uberonId = (String) infoMap.get(headers[0]);
                String uberonName = (String) infoMap.get(headers[1]);
                String relations = (String) infoMap.get(headers[2]);
                String reason = (String) infoMap.get(headers[3]);
                log.trace("Retrieved info from line: {} - {}", uberonId, uberonName); 
                if (i == 0) {
                    assertEquals("U:2", uberonId);
                    assertEquals("brain", uberonName);
                    assertEquals("reason 2", reason);
                    String relationTested = "is_a U:1 anatomical structure";
                    assertTrue("Missing relation for U:2: '" + relationTested + "' - " +
                    		"Actual relations were: " + relations, 
                            relations.contains(relationTested));
                } else if (i == 1) {
                    assertEquals("U:3", uberonId);
                    assertEquals("forebrain", uberonName);
                    assertEquals("reason 3", reason);
                    String relationTested = "is_a U:1 anatomical structure";
                    assertTrue("Missing relation for U:3: '" + relationTested + "' - " +
                            "Actual relations were: " + relations, 
                            relations.contains(relationTested));
                    relationTested = "part_of U:2 brain";
                    assertTrue("Missing relation for U:3: '" + relationTested + "' - " +
                            "Actual relations were: " + relations, 
                            relations.contains(relationTested));
                } else if (i == 2) {
                    assertEquals("U:23", uberonId);
                    assertEquals("U_23", uberonName);
                    assertEquals("reason 23", reason);
                    String relationTested = "is_a U:22 antenna";
                    assertTrue("Missing relation for U:23: '" + relationTested + "' - " +
                            "Actual relations were: " + relations, 
                            relations.contains(relationTested));
                } else {
                    throw new AssertionError("Incorrect number of Uberon terms listed, " +
                    		"currently iterated term: " + uberonId + " - " + uberonName);
                }
                i++;
                
            }
        }
        assertEquals("Incorrect number of lines in TSV output", 3, i);
    }
    
    /**
     * Test the method {@link Uberon#simplifyUberon(OWLOntology, Collection, 
     * Collection, Collection, Collection, Collection)}
     */
    @Test
    public void shouldSimplifyUberon() {
        //TODO: this unit test should be done, but as it only uses methods 
        //from OWLGraphManipulator, that are already tested, I am lazy here.
        //but it should be done anyway, because OWLGraphManipulator is not officially 
        //part of Bgee, but of owltools, so we must ensure that no modifications 
        //of owltools mess up our simplification
        //ontology to use for the test: /uberon/simplifyUberonTest.obo
    }
    
    /**
     * Test the method {@link Uberon#saveXRefMappingsToFile(OWLOntology, String)}.
     * @throws IOException 
     * @throws OBOFormatParserException 
     * @throws OWLOntologyCreationException 
     */
    @Test
    public void shouldSaveXRefMappings() throws OWLOntologyCreationException, 
        OBOFormatParserException, IOException {
        
        String tempFile = testFolder.newFile("xRefMappings.tsv").getPath();
        
        new Uberon().saveXRefMappingsToFile(OntologyUtilsTest.class.
                getResource("/ontologies/xRefMappings.obo").getFile(), tempFile);
        
        //now, read the generated TSV file
        int i = 0;
        try (ICsvMapReader mapReader = new CsvMapReader(
                new FileReader(tempFile), Utils.TSVCOMMENTED)) {
            String[] headers = mapReader.getHeader(true); 
            final CellProcessor[] processors = new CellProcessor[] {
                    new NotNull(), //XRef ID
                    new NotNull()}; //Uberon ID

            Map<String, Object> xRefMap;
            while( (xRefMap = mapReader.read(headers, processors)) != null ) {
                log.trace("Row: {}", xRefMap);
                String xRefId = (String) xRefMap.get(headers[0]);
                String uberonId = (String) xRefMap.get(headers[1]);
                log.trace("Retrieved info from line: {} - {}", xRefId, uberonId); 
                
                if (!(xRefId.equals("ALT_ID:1") && uberonId.equals("ID:1") || 
                        xRefId.equals("ALT_ALT_ID:1") && uberonId.equals("ID:1") || 
                        xRefId.equals("ALT_ID:3") && uberonId.equals("ID:3") || 
                        xRefId.equals("ALT_ALT_ID:3") && uberonId.equals("ID:3") || 
                        xRefId.equals("ALT_ID:2") && uberonId.equals("ID:1") || 
                        xRefId.equals("ALT_ID:2") && uberonId.equals("ID:2"))) {
                    throw new AssertionError("Incorrect line: " + mapReader.getUntokenizedRow());
                }
                i++;
                
            }
        }
        assertEquals("Incorrect number of lines in TSV output", 6, i);
    }
    
    /**
     * Test method {@link Uberon#orderByPrecededBy(Set)}
     */
    @Test
    public void shouldOrderByPrecededBy() throws OWLOntologyCreationException, 
        OBOFormatParserException, IOException {
        
        OWLOntology ont = OntologyUtils.loadOntology(OntologyUtilsTest.class.
                getResource("/ontologies/startEndStages.obo").getFile());
        OWLGraphWrapper wrapper = new OWLGraphWrapper(ont);
        OntologyUtils utils = new OntologyUtils(wrapper);
        Uberon uberon = new Uberon(utils);

        OWLClass cls0 = wrapper.getOWLClassByIdentifier("MmulDv:0000000");
        OWLClass cls1 = wrapper.getOWLClassByIdentifier("MmulDv:0000007");
        OWLClass cls2 = wrapper.getOWLClassByIdentifier("MmulDv:0000008");
        OWLClass cls3 = wrapper.getOWLClassByIdentifier("MmulDv:0000009");
        OWLClass cls4 = wrapper.getOWLClassByIdentifier("MmulDv:0000010");
        
        List<OWLClass> expectedOrderedClasses = Arrays.asList(cls1, cls2, cls3, cls4);
        assertEquals("Incorrect ordering of sibling OWLClasses", expectedOrderedClasses, 
                uberon.orderByPrecededBy(
                        new HashSet<OWLClass>(Arrays.asList(cls3, cls2, cls1, cls4))));
        

        //test that we have an error if several classes have no preceded_by between them
        try {
            uberon.orderByPrecededBy(
                    new HashSet<OWLClass>(Arrays.asList(cls3, cls2, cls0)));
            //if we reach this point, test failed
            throw new AssertionError("Several OWLClasses with no precede_by relations " +
            		"among them did not raison an exception");
        } catch (IllegalStateException e) {
            //test passed
        }
    }
    
    /**
     * Test method {@link Uberon#generateStageNestedSetModel(OWLClass)}.
     */
    @Test
    public void shouldGenerateStageNestedSetModel() throws OWLOntologyCreationException, 
    OBOFormatParserException, IOException {
        OWLOntology ont = OntologyUtils.loadOntology(OntologyUtilsTest.class.
                getResource("/ontologies/startEndStages.obo").getFile());
        OWLGraphWrapper wrapper = new OWLGraphWrapper(ont);
        OntologyUtils utils = new OntologyUtils(wrapper);
        Uberon uberon = new Uberon(utils);

        OWLClass lifeCycle = wrapper.getOWLClassByIdentifier("MmulDv:0000001");
        OWLClass prenatal = wrapper.getOWLClassByIdentifier("MmulDv:0000002");
        OWLClass immature = wrapper.getOWLClassByIdentifier("MmulDv:0000003");
        OWLClass prenatal1 = wrapper.getOWLClassByIdentifier("MmulDv:0000004");
        OWLClass prenatal2 = wrapper.getOWLClassByIdentifier("MmulDv:0000005");
        OWLClass prenatal3 = wrapper.getOWLClassByIdentifier("MmulDv:0000006");
        OWLClass immature1 = wrapper.getOWLClassByIdentifier("MmulDv:0000007");
        OWLClass immature2 = wrapper.getOWLClassByIdentifier("MmulDv:0000008");
        OWLClass immature3 = wrapper.getOWLClassByIdentifier("MmulDv:0000009");
        OWLClass immature4 = wrapper.getOWLClassByIdentifier("MmulDv:0000010");
        OWLClass prenatal1_1 = wrapper.getOWLClassByIdentifier("MmulDv:0000011");
        OWLClass prenatal1_2 = wrapper.getOWLClassByIdentifier("MmulDv:0000012");
        OWLClass prenatal2_1 = wrapper.getOWLClassByIdentifier("MmulDv:0000013");
        OWLClass prenatal2_2 = wrapper.getOWLClassByIdentifier("MmulDv:0000014");
        OWLClass immature1_1 = wrapper.getOWLClassByIdentifier("MmulDv:0000015");
        OWLClass immature1_2 = wrapper.getOWLClassByIdentifier("MmulDv:0000016");
        OWLClass immature1_3 = wrapper.getOWLClassByIdentifier("MmulDv:0000017");
        
        Map<OWLClass, Map<String, Integer>> expectedModel = 
                new HashMap<OWLClass, Map<String, Integer>>();
        
        Map<String, Integer> params = new HashMap<String, Integer>();
        params.put(OntologyUtils.LEFT_BOUND_KEY, 1);
        params.put(OntologyUtils.RIGHT_BOUND_KEY, 34);
        params.put(OntologyUtils.LEVEL_KEY, 1);
        expectedModel.put(lifeCycle, params);
        
        params = new HashMap<String, Integer>();
        params.put(OntologyUtils.LEFT_BOUND_KEY, 2);
        params.put(OntologyUtils.RIGHT_BOUND_KEY, 17);
        params.put(OntologyUtils.LEVEL_KEY, 2);
        expectedModel.put(prenatal, params);
        
        params = new HashMap<String, Integer>();
        params.put(OntologyUtils.LEFT_BOUND_KEY, 3);
        params.put(OntologyUtils.RIGHT_BOUND_KEY, 8);
        params.put(OntologyUtils.LEVEL_KEY, 3);
        expectedModel.put(prenatal1, params);
        
        params = new HashMap<String, Integer>();
        params.put(OntologyUtils.LEFT_BOUND_KEY, 4);
        params.put(OntologyUtils.RIGHT_BOUND_KEY, 5);
        params.put(OntologyUtils.LEVEL_KEY, 4);
        expectedModel.put(prenatal1_1, params);
        
        params = new HashMap<String, Integer>();
        params.put(OntologyUtils.LEFT_BOUND_KEY, 6);
        params.put(OntologyUtils.RIGHT_BOUND_KEY, 7);
        params.put(OntologyUtils.LEVEL_KEY, 4);
        expectedModel.put(prenatal1_2, params);
        
        params = new HashMap<String, Integer>();
        params.put(OntologyUtils.LEFT_BOUND_KEY, 9);
        params.put(OntologyUtils.RIGHT_BOUND_KEY, 14);
        params.put(OntologyUtils.LEVEL_KEY, 3);
        expectedModel.put(prenatal2, params);
        
        params = new HashMap<String, Integer>();
        params.put(OntologyUtils.LEFT_BOUND_KEY, 10);
        params.put(OntologyUtils.RIGHT_BOUND_KEY, 11);
        params.put(OntologyUtils.LEVEL_KEY, 4);
        expectedModel.put(prenatal2_1, params);
        
        params = new HashMap<String, Integer>();
        params.put(OntologyUtils.LEFT_BOUND_KEY, 12);
        params.put(OntologyUtils.RIGHT_BOUND_KEY, 13);
        params.put(OntologyUtils.LEVEL_KEY, 4);
        expectedModel.put(prenatal2_2, params);
        
        params = new HashMap<String, Integer>();
        params.put(OntologyUtils.LEFT_BOUND_KEY, 15);
        params.put(OntologyUtils.RIGHT_BOUND_KEY, 16);
        params.put(OntologyUtils.LEVEL_KEY, 3);
        expectedModel.put(prenatal3, params);
        
        params = new HashMap<String, Integer>();
        params.put(OntologyUtils.LEFT_BOUND_KEY, 18);
        params.put(OntologyUtils.RIGHT_BOUND_KEY, 33);
        params.put(OntologyUtils.LEVEL_KEY, 2);
        expectedModel.put(immature, params);
        
        params = new HashMap<String, Integer>();
        params.put(OntologyUtils.LEFT_BOUND_KEY, 19);
        params.put(OntologyUtils.RIGHT_BOUND_KEY, 26);
        params.put(OntologyUtils.LEVEL_KEY, 3);
        expectedModel.put(immature1, params);
        
        params = new HashMap<String, Integer>();
        params.put(OntologyUtils.LEFT_BOUND_KEY, 20);
        params.put(OntologyUtils.RIGHT_BOUND_KEY, 21);
        params.put(OntologyUtils.LEVEL_KEY, 4);
        expectedModel.put(immature1_1, params);
        
        params = new HashMap<String, Integer>();
        params.put(OntologyUtils.LEFT_BOUND_KEY, 22);
        params.put(OntologyUtils.RIGHT_BOUND_KEY, 23);
        params.put(OntologyUtils.LEVEL_KEY, 4);
        expectedModel.put(immature1_2, params);
        
        params = new HashMap<String, Integer>();
        params.put(OntologyUtils.LEFT_BOUND_KEY, 24);
        params.put(OntologyUtils.RIGHT_BOUND_KEY, 25);
        params.put(OntologyUtils.LEVEL_KEY, 4);
        expectedModel.put(immature1_3, params);
        
        params = new HashMap<String, Integer>();
        params.put(OntologyUtils.LEFT_BOUND_KEY, 27);
        params.put(OntologyUtils.RIGHT_BOUND_KEY, 28);
        params.put(OntologyUtils.LEVEL_KEY, 3);
        expectedModel.put(immature2, params);
        
        params = new HashMap<String, Integer>();
        params.put(OntologyUtils.LEFT_BOUND_KEY, 29);
        params.put(OntologyUtils.RIGHT_BOUND_KEY, 30);
        params.put(OntologyUtils.LEVEL_KEY, 3);
        expectedModel.put(immature3, params);
        
        params = new HashMap<String, Integer>();
        params.put(OntologyUtils.LEFT_BOUND_KEY, 31);
        params.put(OntologyUtils.RIGHT_BOUND_KEY, 32);
        params.put(OntologyUtils.LEVEL_KEY, 3);
        expectedModel.put(immature4, params);
        
        assertEquals("Incorrect developmental stage nested set model", expectedModel, 
                uberon.generateStageNestedSetModel(lifeCycle));
        
        
        expectedModel = new HashMap<OWLClass, Map<String, Integer>>();
       
        params = new HashMap<String, Integer>();
        params.put(OntologyUtils.LEFT_BOUND_KEY, 1);
        params.put(OntologyUtils.RIGHT_BOUND_KEY, 16);
        params.put(OntologyUtils.LEVEL_KEY, 1);
        expectedModel.put(immature, params);
        
        params = new HashMap<String, Integer>();
        params.put(OntologyUtils.LEFT_BOUND_KEY, 2);
        params.put(OntologyUtils.RIGHT_BOUND_KEY, 9);
        params.put(OntologyUtils.LEVEL_KEY, 2);
        expectedModel.put(immature1, params);
        
        params = new HashMap<String, Integer>();
        params.put(OntologyUtils.LEFT_BOUND_KEY, 3);
        params.put(OntologyUtils.RIGHT_BOUND_KEY, 4);
        params.put(OntologyUtils.LEVEL_KEY, 3);
        expectedModel.put(immature1_1, params);
        
        params = new HashMap<String, Integer>();
        params.put(OntologyUtils.LEFT_BOUND_KEY, 5);
        params.put(OntologyUtils.RIGHT_BOUND_KEY, 6);
        params.put(OntologyUtils.LEVEL_KEY, 3);
        expectedModel.put(immature1_2, params);
        
        params = new HashMap<String, Integer>();
        params.put(OntologyUtils.LEFT_BOUND_KEY, 7);
        params.put(OntologyUtils.RIGHT_BOUND_KEY, 8);
        params.put(OntologyUtils.LEVEL_KEY, 3);
        expectedModel.put(immature1_3, params);
        
        params = new HashMap<String, Integer>();
        params.put(OntologyUtils.LEFT_BOUND_KEY, 10);
        params.put(OntologyUtils.RIGHT_BOUND_KEY, 11);
        params.put(OntologyUtils.LEVEL_KEY, 2);
        expectedModel.put(immature2, params);
        
        params = new HashMap<String, Integer>();
        params.put(OntologyUtils.LEFT_BOUND_KEY, 12);
        params.put(OntologyUtils.RIGHT_BOUND_KEY, 13);
        params.put(OntologyUtils.LEVEL_KEY, 2);
        expectedModel.put(immature3, params);
        
        params = new HashMap<String, Integer>();
        params.put(OntologyUtils.LEFT_BOUND_KEY, 14);
        params.put(OntologyUtils.RIGHT_BOUND_KEY, 15);
        params.put(OntologyUtils.LEVEL_KEY, 2);
        expectedModel.put(immature4, params);

        assertEquals("Incorrect developmental stage nested set model from root MmulDv:0000003", 
                expectedModel, 
                uberon.generateStageNestedSetModel(immature));
    }
    
    /**
     * Test the method {@link Uberon#getStageIdsBetween(String, String)}
     * @throws IOException 
     * @throws OBOFormatParserException 
     * @throws OWLOntologyCreationException 
     */
    @Test
    public void shouldGetStageIdsBetween() throws OWLOntologyCreationException, 
        OBOFormatParserException, IOException {
        OWLOntology ont = OntologyUtils.loadOntology(OntologyUtilsTest.class.
                getResource("/ontologies/startEndStages.obo").getFile());
        OWLGraphWrapper wrapper = new OWLGraphWrapper(ont);
        OntologyUtils utils = new OntologyUtils(wrapper);
        Uberon uberon = new Uberon(utils);
        
        List<String> expectedList = Arrays.asList("MmulDv:0000004", "MmulDv:0000005", 
                "MmulDv:0000006", "MmulDv:0000007", "MmulDv:0000008", 
                "MmulDv:0000009", "MmulDv:0000010");
        assertEquals("incorrect stages retrieved between start and end", expectedList, 
                uberon.getStageIdsBetween("MmulDv:0000004", "MmulDv:0000010"));
        
        expectedList = Arrays.asList("MmulDv:0000002", "MmulDv:0000003");
        assertEquals("incorrect stages retrieved between start and end", expectedList, 
                uberon.getStageIdsBetween("MmulDv:0000002", "MmulDv:0000003"));
        
        expectedList = Arrays.asList("MmulDv:0000002", 
                "MmulDv:0000015", "MmulDv:0000016", "MmulDv:0000017");
        assertEquals("incorrect stages retrieved between start and end", expectedList, 
                uberon.getStageIdsBetween("MmulDv:0000002", "MmulDv:0000017"));
        
        expectedList = Arrays.asList("MmulDv:0000004", 
                "MmulDv:0000013", "MmulDv:0000014", "MmulDv:0000006", 
                "MmulDv:0000015", "MmulDv:0000016", "MmulDv:0000017");
        assertEquals("incorrect stages retrieved between start and end", expectedList, 
                uberon.getStageIdsBetween("MmulDv:0000004", "MmulDv:0000017"));
        
        expectedList = Arrays.asList("MmulDv:0000004", "MmulDv:0000005", "MmulDv:0000006", 
                "MmulDv:0000007");
        assertEquals("incorrect stages retrieved between start and end", expectedList, 
                uberon.getStageIdsBetween("MmulDv:0000004", "MmulDv:0000007"));
    }
    
    //@Test
    public void test() throws OWLOntologyCreationException, OBOFormatParserException, IOException {
        OWLOntology ont = OntologyUtils.loadOntology("/Users/admin/Desktop/composite-metazoan.obo");
        OWLGraphManipulator manip = new OWLGraphManipulator(ont);
        OWLGraphWrapper wrapper = manip.getOwlGraphWrapper();
        
        OWLClass embryo = wrapper.getOWLClassByIdentifier("UBERON:0000922");
        log.info(wrapper.getNamedAncestors(embryo));
        log.info(wrapper.getEdgesBetween(wrapper.getOWLClassByIdentifier("UBERON:0000922"), 
                wrapper.getOWLClassByIdentifier("NBO:0000313")));
    }
}
