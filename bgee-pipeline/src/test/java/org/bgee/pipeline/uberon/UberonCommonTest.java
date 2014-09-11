package org.bgee.pipeline.uberon;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.TestAncestor;
import org.bgee.pipeline.ontologycommon.OntologyUtils;
import org.bgee.pipeline.ontologycommon.OntologyUtilsTest;
import org.junit.Test;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import owltools.graph.OWLGraphWrapper;

public class UberonCommonTest extends TestAncestor {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(UberonCommonTest.class.getName());
    
    /**
     * Default Constructor. 
     */
    public UberonCommonTest() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test the method {@link UberonCommon#existsInSpecies(OWLClass, int)}, 
     * using the {@code UberonDevStage} class ({@code UberonCommon} is abstract).
     * @throws IOException 
     * @throws OBOFormatParserException 
     * @throws OWLOntologyCreationException 
     */
    @Test
    public void testExistsInSpecies() throws OWLOntologyCreationException, 
    OBOFormatParserException, IOException {
        Map<String, Set<Integer>> taxonConstraints = new HashMap<String, Set<Integer>>();
        taxonConstraints.put("ID:1", new HashSet<Integer>(Arrays.asList(1, 2)));
        taxonConstraints.put("ID:2", new HashSet<Integer>(Arrays.asList(1)));
        taxonConstraints.put("ID:3", new HashSet<Integer>(Arrays.asList(2)));
        taxonConstraints.put("ID:4", new HashSet<Integer>());
        
        OWLOntology ont = OntologyUtils.loadOntology(UberonDevStageTest.class.
                getResource("/ontologies/testExistsInSpecies.obo").getFile());
        OWLGraphWrapper wrapper = new OWLGraphWrapper(ont);
        OntologyUtils utils = new OntologyUtils(wrapper);
        UberonDevStage uberon = new UberonDevStage(utils, taxonConstraints);
        
        OWLClass cls = wrapper.getOWLClassByIdentifier("ID:1");
        assertTrue(uberon.existsInSpecies(cls, 1));
        assertTrue(uberon.existsInSpecies(cls, 2));
        assertFalse(uberon.existsInSpecies(cls, 3));
        
        cls = wrapper.getOWLClassByIdentifier("ID:2");
        assertTrue(uberon.existsInSpecies(cls, 1));
        assertFalse(uberon.existsInSpecies(cls, 2));
        assertFalse(uberon.existsInSpecies(cls, 3));
        
        cls = wrapper.getOWLClassByIdentifier("ID:3");
        assertFalse(uberon.existsInSpecies(cls, 1));
        assertTrue(uberon.existsInSpecies(cls, 2));
        assertFalse(uberon.existsInSpecies(cls, 3));
        
        cls = wrapper.getOWLClassByIdentifier("ID:4");
        assertFalse(uberon.existsInSpecies(cls, 1));
        assertFalse(uberon.existsInSpecies(cls, 2));
        assertFalse(uberon.existsInSpecies(cls, 3));
    }
    
    /**
     * Test the method {@link UberonCommon#existsInAtLeastOneSpecies(OWLClass, Collection)}, 
     * using the {@code UberonDevStage} class ({@code UberonCommon} is abstract).
     * @throws IOException 
     * @throws OBOFormatParserException 
     * @throws OWLOntologyCreationException 
     */
    @Test
    public void testExistsInAtLeastOneSpecies() throws OWLOntologyCreationException, 
    OBOFormatParserException, IOException {
        Map<String, Set<Integer>> taxonConstraints = new HashMap<String, Set<Integer>>();
        taxonConstraints.put("ID:1", new HashSet<Integer>(Arrays.asList(1, 2)));
        taxonConstraints.put("ID:2", new HashSet<Integer>(Arrays.asList(1)));
        taxonConstraints.put("ID:3", new HashSet<Integer>(Arrays.asList(2)));
        taxonConstraints.put("ID:4", new HashSet<Integer>());
        
        OWLOntology ont = OntologyUtils.loadOntology(UberonDevStageTest.class.
                getResource("/ontologies/testExistsInSpecies.obo").getFile());
        OWLGraphWrapper wrapper = new OWLGraphWrapper(ont);
        OntologyUtils utils = new OntologyUtils(wrapper);
        UberonDevStage uberon = new UberonDevStage(utils, taxonConstraints);
        
        OWLClass cls = wrapper.getOWLClassByIdentifier("ID:1");
        assertTrue(uberon.existsInAtLeastOneSpecies(cls, Arrays.asList(2, 3)));
        assertTrue(uberon.existsInAtLeastOneSpecies(cls, Arrays.asList(1, 2, 3)));
        assertTrue(uberon.existsInAtLeastOneSpecies(cls, Arrays.asList(1, 2)));
        assertFalse(uberon.existsInAtLeastOneSpecies(cls, Arrays.asList(3)));
        
        cls = wrapper.getOWLClassByIdentifier("ID:4");
        assertFalse(uberon.existsInAtLeastOneSpecies(cls, Arrays.asList(1, 2, 3)));
    }
    

    
    /**
     * Test the method {@link OntologyUtils#getOWLClass(String)}.
     * @throws IOException 
     * @throws OBOFormatParserException 
     * @throws OWLOntologyCreationException 
     */
    @Test
    public void shouldGetOWLClass() throws OWLOntologyCreationException, 
        OBOFormatParserException, IOException {
        OWLOntology ont = OntologyUtils.loadOntology(OntologyUtilsTest.class.
                getResource("/ontologies/xRefMappings.obo").getFile());
        OWLGraphWrapper wrapper = new OWLGraphWrapper(ont);
        OntologyUtils utils = new OntologyUtils(wrapper);
        Uberon uberon = new Uberon(utils);
        
        OWLClass expectedClass = wrapper.getOWLClassByIdentifier("ID:1");
        
        //Test OBO-like ID
        assertEquals(expectedClass, uberon.getOWLClass("ID:1"));
        //test IRI
        assertEquals(expectedClass, uberon.getOWLClass("http://purl.obolibrary.org/obo/ID_1"));
        //test xrefs
        assertEquals(expectedClass, uberon.getOWLClass("ALT_ID:1"));
        assertEquals(expectedClass, uberon.getOWLClass("ALT_ALT_ID:1"));
        //if mapping is ambiguous, return null
        assertNull(uberon.getOWLClass("ALT_ID:2"));
        //test obsolete class replaced_by another
        assertEquals(expectedClass, uberon.getOWLClass("ID:5"));
        //if mapping ambiguous over replaced_by, return null
        assertNull(uberon.getOWLClass("ID:4"));
    }
    
    /**
     * Test the method {@link OntologyUtils#getOWLClasses(String, boolean)} 
     * with the {@code boolean} argument {@code false}.
     * @throws IOException 
     * @throws OBOFormatParserException 
     * @throws OWLOntologyCreationException 
     */
    @Test
    public void shouldGetOWLClasses() throws OWLOntologyCreationException, 
        OBOFormatParserException, IOException {
        OWLOntology ont = OntologyUtils.loadOntology(OntologyUtilsTest.class.
                getResource("/ontologies/xRefMappings.obo").getFile());
        OWLGraphWrapper wrapper = new OWLGraphWrapper(ont);
        OntologyUtils utils = new OntologyUtils(wrapper);
        Uberon uberon = new Uberon(utils);
        
        Set<OWLClass> expectedClasses = new HashSet<OWLClass>(Arrays.asList(
                wrapper.getOWLClassByIdentifier("ID:1"), 
                wrapper.getOWLClassByIdentifier("ID:2")));
        assertEquals(expectedClasses, uberon.getOWLClasses("ALT_ID:2", false));
        
        expectedClasses = new HashSet<OWLClass>(Arrays.asList(
                wrapper.getOWLClassByIdentifier("ID_REPLACED:4"), 
                wrapper.getOWLClassByIdentifier("ID_REPLACED_BIS_XREF:4")));
        assertEquals(expectedClasses, uberon.getOWLClasses("ID:4", false));
        
        expectedClasses = new HashSet<OWLClass>(Arrays.asList(
                wrapper.getOWLClassByIdentifier("ID:1")));
        assertEquals(expectedClasses, uberon.getOWLClasses("ID_XREF_OBSOLETE:5", false));
        
        expectedClasses = new HashSet<OWLClass>(Arrays.asList(
                wrapper.getOWLClassByIdentifier("ID:1")));
        assertEquals(expectedClasses, uberon.getOWLClasses("ID_XREF_OBSOLETE:1", false));
        
        expectedClasses = new HashSet<OWLClass>(Arrays.asList(
                wrapper.getOWLClassByIdentifier("ID_REPLACED:4"), 
                wrapper.getOWLClassByIdentifier("ID_REPLACED_BIS_XREF:4"),
                wrapper.getOWLClassByIdentifier("ID:1")));
        assertEquals(expectedClasses, uberon.getOWLClasses("ID_XREF_OBSOLETE:6", false));
        
        expectedClasses = new HashSet<OWLClass>();
        assertEquals(expectedClasses, uberon.getOWLClasses("ID:7", false));
        
        expectedClasses = new HashSet<OWLClass>();
        assertEquals(expectedClasses, uberon.getOWLClasses("ID:6", false));
    }
    
    /**
     * Test the method {@link OntologyUtils#getOWLClasses(String, boolean)} 
     * with the {@code boolean} argument {@code false}, to retrieve taxonomic equivalent 
     * classes.
     * @throws IOException 
     * @throws OBOFormatParserException 
     * @throws OWLOntologyCreationException 
     */
    @Test
    public void shouldGetEquivalentOWLClasses() throws OWLOntologyCreationException, 
        OBOFormatParserException, IOException {
        OWLOntology ont = OntologyUtils.loadOntology(OntologyUtilsTest.class.
                getResource("/ontologies/gci_equivalent.owl").getFile());
        OWLGraphWrapper wrapper = new OWLGraphWrapper(ont);
        OntologyUtils utils = new OntologyUtils(wrapper);
        Uberon uberon = new Uberon(utils);
        
        Set<OWLClass> expectedClasses = new HashSet<OWLClass>(Arrays.asList(
                wrapper.getOWLClassByIdentifier("UBERON:0000104"), 
                wrapper.getOWLClassByIdentifier("UBERON:0000105")));
        assertEquals(expectedClasses, uberon.getOWLClasses("HsapDv:0000001", false));
        
        expectedClasses = new HashSet<OWLClass>();
        assertEquals(expectedClasses, uberon.getOWLClasses("HsapDv:0000001", true));
        
        expectedClasses = new HashSet<OWLClass>(Arrays.asList(
                wrapper.getOWLClassByIdentifier("UBERON:0000104")));
        assertEquals(expectedClasses, uberon.getOWLClasses("UBERON:0000104", false));
    }

}
