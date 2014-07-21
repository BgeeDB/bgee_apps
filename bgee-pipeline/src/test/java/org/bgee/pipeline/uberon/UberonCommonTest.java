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

}
