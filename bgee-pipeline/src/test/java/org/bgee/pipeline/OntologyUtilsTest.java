package org.bgee.pipeline;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;

import owltools.graph.OWLGraphWrapper;

/**
 * Unit tests for {@link OntologyUtils}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class OntologyUtilsTest extends TestAncestor {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(OntologyUtilsTest.class.getName());

    /**
     * An {@code OWLGraphWrapper} wrapping an {@code OWLOntology} used for test purpose.
     */
    private static OWLGraphWrapper wrapper;
    /**
     * An {@code OWLClass} corresponding to the term "root" in the {@code OWLOntology} 
     * wrapped by {@link #wrapper}.
     */
    private static OWLClass classRoot;
    /**
     * An {@code OWLClass} corresponding to the term "A" in the {@code OWLOntology} 
     * wrapped by {@link #wrapper}.
     */
    private static OWLClass classA;
    /**
     * An {@code OWLClass} corresponding to the term "B" in the {@code OWLOntology} 
     * wrapped by {@link #wrapper}.
     */
    private static OWLClass classB;
    /**
     * An {@code OWLClass} corresponding to the term "C" in the {@code OWLOntology} 
     * wrapped by {@link #wrapper}.
     */
    private static OWLClass classC;
    /**
     * An {@code OWLClass} corresponding to the term "D" in the {@code OWLOntology} 
     * wrapped by {@link #wrapper}.
     */
    private static OWLClass classD;
    /**
     * An {@code OWLClass} corresponding to the term "E" in the {@code OWLOntology} 
     * wrapped by {@link #wrapper}.
     */
    private static OWLClass classE;
    /**
     * Default Constructor. 
     */
    public OntologyUtilsTest() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Load the (really basic) ontology {@code /utils/nestedSetModelTest.obo} 
     * into {@link #wrapper}, and the corresponding {@code OWLClass}es into 
     * {@link #classRoot}, {@link #classB}, {@link #classC}, {@link #classD}, 
     * {@link #classE}. The ontology is as followed: 
     * <pre>
     *            FOO:0001 root
     *        /        |         \
     *   FOO:0002 A  FOO:0003 B   FOO:0004 C
     *    /       \
     * FOO:0005 D  FOO:0006 E
     *     
     *     
     * </pre>
     *  
     * @throws OWLOntologyCreationException 
     * @throws OBOFormatParserException
     * @throws IOException
     * 
     * @see #wrapper
     */
    @BeforeClass
    public static void loadTestOntology() 
            throws OWLOntologyCreationException, OBOFormatParserException, IOException
    {
        log.debug("Wrapping test ontology into OWLGraphWrapper...");
        OWLOntology ont = OntologyUtils.loadOntology(OntologyUtilsTest.class.
                getResource("/utils/nestedSetModelTest.obo").getFile());
        wrapper = new OWLGraphWrapper(ont);
        classRoot = wrapper.getOWLClassByIdentifier("FOO:0001");
        classA = wrapper.getOWLClassByIdentifier("FOO:0002");
        classB = wrapper.getOWLClassByIdentifier("FOO:0003");
        classC = wrapper.getOWLClassByIdentifier("FOO:0004");
        classD = wrapper.getOWLClassByIdentifier("FOO:0005");
        classE = wrapper.getOWLClassByIdentifier("FOO:0006");
        log.debug("Done wrapping test ontology into OWLGraphWrapper.");
    }
    
    /**
     * Test the method {@link OntologyUtils#computeNestedSetModelParams(NavigableSet)}.
     */
    @Test
    public void shouldComputeNestedSetModelParams() throws UnknownOWLOntologyException, 
    IllegalStateException, OWLOntologyCreationException {
        OntologyUtils utils = new OntologyUtils(wrapper);
        //get a List to order the ontology terms. We mess it a bit to test properly
        List<OWLClass> classOrder = new ArrayList<OWLClass>();
        //order for first level. Not putting the root should be OK
        classOrder.add(classB);
        //we can interleaves children of different parents, it should still work
        classOrder.add(classE);
        classOrder.add(classD);
        //back to ordering the first level.
        classOrder.add(classA);
        classOrder.add(classC);
        
        Map<OWLClass, Map<String, Integer>> params = 
                utils.computeNestedSetModelParams(classOrder);
        
        assertEquals("Incorrect number of OWLClass with parameters", 6, params.size());
        
        //now, assert the params of each term in the ontology
        this.assertParams(params, classRoot, 1, 12, 1);
        this.assertParams(params, classB, 2, 3, 2);
        this.assertParams(params, classA, 4, 9, 2);
        this.assertParams(params, classE, 5, 6, 3);
        this.assertParams(params, classD, 7, 8, 3);
        this.assertParams(params, classC, 10, 11, 2);
    }
    
    /**
     * Assert whether {@code classExamined} has an entry in {@code nestedSetModelParams}, 
     * allowing to retrieve a {@code Map} storing values corresponding to {@code leftBound}, 
     * {@code rightBound}, and {@code level}, respectively associated to 
     * {@code OntologyUtils.LEFT_BOUND_KEY}, {@code OntologyUtils.RIGHT_BOUND_KEY}, 
     * and {@code OntologyUtils.LEVEL_KEY}.
     * 
     * @param nestedSetModelParams  The {@code Map} containing all parameters 
     *                              for all {@code OWLClass}es.
     * @param classExamined         {@code OWLClass} currently tested.
     * @param leftBound             The expected left bound value of {@code classExamined} 
     *                              in {@code nestedSetModelParams}.
     * @param rightBound            The expected right bound value of {@code classExamined} 
     *                              in {@code nestedSetModelParams}.
     * @param level                 The expected level value of {@code classExamined} 
     *                              in {@code nestedSetModelParams}.
     * @throws AssertionError   if the assertion test fails.
     */
    private void assertParams(Map<OWLClass, Map<String, Integer>> nestedSetModelParams, 
            OWLClass classExamined, int leftBound, int rightBound, 
            int level) throws AssertionError {
        
        String classId = wrapper.getIdentifier(classExamined);
        
        Map<String, Integer> classParams = nestedSetModelParams.get(classExamined);
        assertEquals("Incorrect left bound for " + classId, leftBound, 
                (int) classParams.get(OntologyUtils.LEFT_BOUND_KEY));
        assertEquals("Incorrect left bound for " + classId, rightBound, 
                (int) classParams.get(OntologyUtils.RIGHT_BOUND_KEY));
        assertEquals("Incorrect left bound for " + classId, level, 
                (int) classParams.get(OntologyUtils.LEVEL_KEY));
    }
    
    /**
     * Test that the method {@link OntologyUtils#computeNestedSetModelParams(NavigableSet)} 
     * throws an IllegalStateException if the {@code OWLOntology} used is not 
     * a simple tree.
     */
    @Test
    public void shouldFailComputeNestedSetModel() throws UnknownOWLOntologyException, 
    OWLOntologyCreationException {
        //we add a subClassOf axiom in the ontology, that will make a class 
        //to have several parents, so the ontology would not be a tree anymore
        OWLDataFactory factory = wrapper.getManager().getOWLDataFactory();
        OWLSubClassOfAxiom ax = factory.getOWLSubClassOfAxiom(classD, classB);
        wrapper.getManager().addAxiom(wrapper.getSourceOntology(), ax);
        wrapper.clearCachedEdges();
        try {
            OntologyUtils utils = new OntologyUtils(wrapper);
            //an IllegalStateException should be thrown
            try {
                utils.computeNestedSetModelParams();
                //if we reach this point, test failed
                throw new AssertionError("The OntologyUtils should have thrown " +
                		"an IllegalStateException");
            } catch (IllegalStateException e) {
                //test passed
            }
        } finally {
            //get the ontology identical for following tests
            wrapper.getManager().removeAxiom(wrapper.getSourceOntology(), ax);
            wrapper.clearCachedEdges();
        }
    }
    
    /**
     * Test the method {@link OntologyUtils#convertToTaxOntologyIds(Set)}.
     */
    @Test
    public void shouldConvertToTaxOntologyIds() {
        Set<Integer> ncbiIds = new HashSet<Integer>(Arrays.asList(2, 5, 10, 500));
        Set<String> expectedOntIds = new HashSet<String>(Arrays.asList("NCBITaxon:2", 
                "NCBITaxon:5", "NCBITaxon:10", "NCBITaxon:500"));
        assertEquals("Incorrect conversion", expectedOntIds, 
                OntologyUtils.convertToTaxOntologyIds(ncbiIds));
    }
}
