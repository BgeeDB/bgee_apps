package org.bgee.pipeline.uberon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.TestAncestor;
import org.bgee.pipeline.ontologycommon.OntologyUtils;
import org.junit.Test;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import owltools.graph.OWLGraphWrapper;

/**
 * Unit tests for {@link UberonDevStage}
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class UberonDevStageTest extends TestAncestor {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(UberonDevStageTest.class.getName());
    
    /**
     * Default Constructor. 
     */
    public UberonDevStageTest() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test method {@link Uberon#orderByPrecededBy(Set)}
     */
    @Test
    public void shouldOrderByPrecededBy() throws OWLOntologyCreationException, 
        OBOFormatParserException, IOException {
        
        OWLOntology ont = OntologyUtils.loadOntology(UberonDevStageTest.class.
                getResource("/ontologies/startEndStages.obo").getFile());
        OWLGraphWrapper wrapper = new OWLGraphWrapper(ont);
        OntologyUtils utils = new OntologyUtils(wrapper);
        UberonDevStage uberon = new UberonDevStage(utils);

        OWLClass cls0 = wrapper.getOWLClassByIdentifier("MmulDv:0000002");
        OWLClass cls1 = wrapper.getOWLClassByIdentifier("MmulDv:0000007");
        OWLClass cls2 = wrapper.getOWLClassByIdentifier("MmulDv:0000008");
        OWLClass cls3 = wrapper.getOWLClassByIdentifier("MmulDv:0000009");
        OWLClass cls4 = wrapper.getOWLClassByIdentifier("MmulDv:0000010");
        
        List<OWLClass> expectedOrderedClasses = Arrays.asList(cls1, cls2, cls3, cls4);
        assertEquals("Incorrect ordering of sibling OWLClasses", expectedOrderedClasses, 
                uberon.orderByPrecededBy(
                        new HashSet<OWLClass>(Arrays.asList(cls3, cls2, cls1, cls4))));
        
        expectedOrderedClasses = Arrays.asList(cls0, cls2, cls3, cls4);
        assertEquals("Incorrect ordering of sibling OWLClasses", expectedOrderedClasses, 
                uberon.orderByPrecededBy(
                        new HashSet<OWLClass>(Arrays.asList(cls3, cls2, cls0, cls4))));
    }
    
    /**
     * Test the method {@link Uberon#getLastClassByPrecededBy(Set)}.
     */
    @Test
    public void shouldGetLastClassByPrecededBy() throws OWLOntologyCreationException, 
    OBOFormatParserException, IOException {
        OWLOntology ont = OntologyUtils.loadOntology(UberonDevStageTest.class.
                getResource("/ontologies/startEndStages.obo").getFile());
        OWLGraphWrapper wrapper = new OWLGraphWrapper(ont);
        OntologyUtils utils = new OntologyUtils(wrapper);
        UberonDevStage uberon = new UberonDevStage(utils);

        OWLClass cls0 = wrapper.getOWLClassByIdentifier("MmulDv:0000000");
        OWLClass cls1 = wrapper.getOWLClassByIdentifier("MmulDv:0000007");
        OWLClass cls2 = wrapper.getOWLClassByIdentifier("MmulDv:0000008");
        OWLClass cls3 = wrapper.getOWLClassByIdentifier("MmulDv:0000009");
        OWLClass cls4 = wrapper.getOWLClassByIdentifier("MmulDv:0000010");
        
        assertEquals("Incorrect last class returned", cls4, 
                uberon.getLastClassByPrecededBy(
                        new HashSet<OWLClass>(Arrays.asList(cls3, cls2, cls1, cls4))));
        assertEquals("Incorrect last class returned", cls4, 
                uberon.getLastClassByPrecededBy(
                        new HashSet<OWLClass>(Arrays.asList(cls3, cls2, cls0, cls4))));
        assertEquals("Incorrect last class returned", cls3, 
                uberon.getLastClassByPrecededBy(
                        new HashSet<OWLClass>(Arrays.asList(cls3, cls2, cls1))));
        assertEquals("Incorrect last class returned", cls2, 
                uberon.getLastClassByPrecededBy(
                        new HashSet<OWLClass>(Arrays.asList(cls2, cls1))));
        
        //test via indirect edges
        assertEquals("Incorrect last class returned", cls4, 
                uberon.getLastClassByPrecededBy(
                        new HashSet<OWLClass>(Arrays.asList(cls2, cls1, cls4))));
        assertEquals("Incorrect last class returned", cls4, 
                uberon.getLastClassByPrecededBy(
                        new HashSet<OWLClass>(Arrays.asList(cls2, cls0, cls4))));
    }
    
    /**
     * Test the method {@link Uberon#generatePrecededByFromComments()}.
     */
    @Test
    public void shouldGeneratePrecededByFromComments() throws OWLOntologyCreationException, 
    OBOFormatParserException, IOException {
        OWLOntology ont = OntologyUtils.loadOntology(UberonDevStageTest.class.
                getResource("/ontologies/fbdv_test.obo").getFile());
        OWLGraphWrapper wrapper = new OWLGraphWrapper(ont);
        OntologyUtils utils = new OntologyUtils(wrapper);
        UberonDevStage uberon = new UberonDevStage(utils);
        
        uberon.generatePrecededByFromComments(new HashSet<OWLClass>(Arrays.asList(
                wrapper.getOWLClassByIdentifier("FBdv:00000000"), 
                wrapper.getOWLClassByIdentifier("FBdv:00000001"), 
                wrapper.getOWLClassByIdentifier("FBdv:00000002"), 
                wrapper.getOWLClassByIdentifier("FBdv:00000003"))));
        
        //check that the desired edges were created. 
        OWLDataFactory factory = ont.getOWLOntologyManager().getOWLDataFactory();
        OWLObjectProperty precededBy = 
            wrapper.getOWLObjectPropertyByIdentifier(OntologyUtils.PRECEDED_BY_ID);
        OWLObjectProperty immPrecededBy = 
            wrapper.getOWLObjectPropertyByIdentifier(OntologyUtils.IMMEDIATELY_PRECEDED_BY_ID);
        
        assertTrue("missing preceded_by relations generated from comments", 
                ont.containsAxiom(factory.getOWLSubClassOfAxiom(
                    wrapper.getOWLClassByIdentifier("FBdv:00000003"), 
                    factory.getOWLObjectSomeValuesFrom(precededBy, 
                        wrapper.getOWLClassByIdentifier("FBdv:00000002")))));
        assertTrue("missing preceded_by relations generated from comments", 
                ont.containsAxiom(factory.getOWLSubClassOfAxiom(
                    wrapper.getOWLClassByIdentifier("FBdv:00000002"), 
                    factory.getOWLObjectSomeValuesFrom(precededBy, 
                        wrapper.getOWLClassByIdentifier("FBdv:00000001")))));
        assertTrue("missing preceded_by relations generated from comments", 
                ont.containsAxiom(factory.getOWLSubClassOfAxiom(
                    wrapper.getOWLClassByIdentifier("FBdv:00000001"), 
                    factory.getOWLObjectSomeValuesFrom(precededBy, 
                        wrapper.getOWLClassByIdentifier("FBdv:00000000")))));
        
        //check that the already existing immediately_preceded_by relation was not removed
        assertTrue("An existing relation was incorrectly removed", 
                ont.containsAxiom(factory.getOWLSubClassOfAxiom(
                    wrapper.getOWLClassByIdentifier("FBdv:00000002"), 
                    factory.getOWLObjectSomeValuesFrom(immPrecededBy, 
                        wrapper.getOWLClassByIdentifier("FBdv:00000001")))));
    }
    
    /**
     * Test method {@link Uberon#generateStageNestedSetModel(OWLClass)}.
     */
    @Test
    public void shouldGenerateStageNestedSetModel() throws OWLOntologyCreationException, 
    OBOFormatParserException, IOException {
        OWLOntology ont = OntologyUtils.loadOntology(UberonDevStageTest.class.
                getResource("/ontologies/startEndStages.obo").getFile());
        OWLGraphWrapper wrapper = new OWLGraphWrapper(ont);
        OntologyUtils utils = new OntologyUtils(wrapper);
        UberonDevStage uberon = new UberonDevStage(utils);

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
        
        //calling again the method on a child should return the same nested set model 
        //in cache
        assertEquals("Incorrect developmental stage nested set model from cache", 
                expectedModel, 
                uberon.generateStageNestedSetModel(immature));
        
        
        //now we reinit Uberon to get the actual nested set model 
        uberon = new UberonDevStage(utils);
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
     * Test the method {@link Uberon#getStageIdsBetween(String, String)} on an actual 
     * tricky messy stage ontology. 
     * 
     * @throws OWLOntologyCreationException
     * @throws OBOFormatParserException
     * @throws IOException
     */
    @Test
    public void shouldGetComplexStageIdsBetween() throws OWLOntologyCreationException, 
    OBOFormatParserException, IOException {
        OWLOntology ont = OntologyUtils.loadOntology(UberonDevStageTest.class.
                getResource("/ontologies/dev_stage_ontology.obo").getFile());
        OWLGraphWrapper wrapper = new OWLGraphWrapper(ont);
        OntologyUtils utils = new OntologyUtils(wrapper);
        UberonDevStage uberon = new UberonDevStage(utils);
        
        //FBdv:00005289: embryonic stage
        //FBdv:00007026: mature adult stage
        List<String> expectedStageIds = Arrays.asList("FBdv:00005289", "FBdv:00005336", 
                "FBdv:00007001", "FBdv:00005369", "FBdv:00007026");
        assertEquals("Incorrect stage range returned", expectedStageIds, 
                uberon.getStageIdsBetween("FBdv:00005289", "FBdv:00007026"));
        
        //reinit uberon to recompute the nested set model
        uberon = new UberonDevStage(utils);
        //FBdv:00005304: blastoderm stage
        //FBdv:00005333: late embryonic stage
        expectedStageIds = Arrays.asList("FBdv:00005304", "FBdv:00005317", 
                "FBdv:00005321", "FBdv:00005331", "FBdv:00005333");
        assertEquals("Incorrect stage range returned", expectedStageIds, 
                uberon.getStageIdsBetween("FBdv:00005304", "FBdv:00005333"));
        
        //reinit uberon to recompute the nested set model
        uberon = new UberonDevStage(utils);
        //FBdv:00005294: embryonic cycle 2
        //FBdv:00005333: late embryonic stage
        expectedStageIds = Arrays.asList("FBdv:00005294", "FBdv:00005295", "FBdv:00005296", 
                "FBdv:00005297", "FBdv:00005298", "FBdv:00005299", "FBdv:00005300", 
                "FBdv:00005302", "FBdv:00005303", "FBdv:00005307", "FBdv:00005308", 
                "FBdv:00005309", "FBdv:00005311", "FBdv:00005318", "FBdv:00005319", 
                "FBdv:00005322", "FBdv:00005323", "FBdv:00005324", "FBdv:00005325", 
                "FBdv:00005327", "FBdv:00005328", "FBdv:00005330", "FBdv:00005332", 
                "FBdv:00005333");
        assertEquals("Incorrect stage range returned", expectedStageIds, 
                uberon.getStageIdsBetween("FBdv:00005294", "FBdv:00005333"));
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
        OWLOntology ont = OntologyUtils.loadOntology(UberonDevStageTest.class.
                getResource("/ontologies/startEndStages.obo").getFile());
        OWLGraphWrapper wrapper = new OWLGraphWrapper(ont);
        OntologyUtils utils = new OntologyUtils(wrapper);
        UberonDevStage uberon = new UberonDevStage(utils);
        
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
}
