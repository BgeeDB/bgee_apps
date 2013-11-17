package org.bgee.pipeline.species;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.OntologyUtils;
import org.bgee.pipeline.TestAncestor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import owltools.graph.OWLGraphEdge;
import owltools.graph.OWLGraphWrapper;

/**
 * Unit tests for {@link GenerateTaxonOntology}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class GenerateTaxonOntologyTest extends TestAncestor {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(GenerateTaxonOntologyTest.class.getName());
    
    /**
     * JUnit temp folder to store the generated test ontology. 
     */
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    /**
     * Default Constructor. 
     */
    public GenerateTaxonOntologyTest() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test {@link GenerateTaxonOntology#generateOntology()}. Generate 
     * an OBO ontology from the data file {@code 
     * src/test/resources/species/sample.dat} and save it as an OBO, then open it 
     * to perform the assertion tests. 
     */
    @Test
    public void shouldGenerateOntology() throws IllegalArgumentException, 
        OWLOntologyCreationException, OWLOntologyStorageException, IOException, 
        OBOFormatParserException  {
        
        String outputFile = this.tempFolder.newFile("test.obo").getAbsolutePath();
        String dataFile = this.getClass().getResource("/species/sample.dat").getFile();
        String taxonSubgraph = "NCBITaxon:84992";
        
        GenerateTaxonOntology generate = new GenerateTaxonOntology();
        generate.generateOntology(dataFile,  taxonSubgraph, outputFile);
        
        //now we open the saved ontology and perform the assertion tests
        OWLOntology ont = OntologyUtils.loadOntology(outputFile);
        OWLGraphWrapper wrapper = new OWLGraphWrapper(ont);
        
        assertEquals("Incorrect number of classes in geenrated ontology", 11, 
                wrapper.getAllOWLClasses().size());
        OWLClass cls1 = wrapper.getOWLClassByIdentifier("NCBITaxon:1");
        OWLClass cls2 = wrapper.getOWLClassByIdentifier("NCBITaxon:131567");
        OWLClass cls3 = wrapper.getOWLClassByIdentifier("NCBITaxon:2");
        OWLClass cls4 = wrapper.getOWLClassByIdentifier("NCBITaxon:201174");
        OWLClass cls5 = wrapper.getOWLClassByIdentifier("NCBITaxon:1760");
        OWLClass cls6 = wrapper.getOWLClassByIdentifier("NCBITaxon:84992");
        OWLClass cls7 = wrapper.getOWLClassByIdentifier("NCBITaxon:84993");
        OWLClass cls8 = wrapper.getOWLClassByIdentifier("NCBITaxon:85003");
        OWLClass cls11 = wrapper.getOWLClassByIdentifier("NCBITaxon:65645");
        OWLClass cls9 = wrapper.getOWLClassByIdentifier("NCBITaxon:1223512");
        OWLClass cls10 = wrapper.getOWLClassByIdentifier("NCBITaxon:12235122");
        assertNotNull("Incorrect classes in generated ontology", cls1);
        assertNotNull("Incorrect classes in generated ontology", cls2);
        assertNotNull("Incorrect classes in generated ontology", cls3);
        assertNotNull("Incorrect classes in generated ontology", cls4);
        assertNotNull("Incorrect classes in generated ontology", cls5);
        assertNotNull("Incorrect classes in generated ontology", cls6);
        assertNotNull("Incorrect classes in generated ontology", cls7);
        assertNotNull("Incorrect classes in generated ontology", cls8);
        assertNotNull("Incorrect classes in generated ontology", cls9);
        assertNotNull("Incorrect classes in generated ontology", cls10);
        assertNotNull("Incorrect classes in generated ontology", cls11);
        
        Set<OWLClass> roots = wrapper.getOntologyRoots();
        assertEquals("Incorrect root in geenrated ontology", 1, 
                roots.size());
        assertEquals("Incorrect root in geenrated ontology", roots.iterator().next(), 
                cls1);
        
        Set<OWLGraphEdge> expectedEdges = new HashSet<OWLGraphEdge>();
        expectedEdges.add(new OWLGraphEdge(cls2, cls1, ont));
        assertEquals("Incorrect incoming edges to " + cls1, expectedEdges, 
                wrapper.getIncomingEdges(cls1));
        
        expectedEdges = new HashSet<OWLGraphEdge>();
        expectedEdges.add(new OWLGraphEdge(cls3, cls2, ont));
        assertEquals("Incorrect incoming edges to " + cls2, expectedEdges, 
                wrapper.getIncomingEdges(cls2));
        
        expectedEdges = new HashSet<OWLGraphEdge>();
        expectedEdges.add(new OWLGraphEdge(cls4, cls3, ont));
        assertEquals("Incorrect incoming edges to " + cls3, expectedEdges, 
                wrapper.getIncomingEdges(cls3));
        
        expectedEdges = new HashSet<OWLGraphEdge>();
        expectedEdges.add(new OWLGraphEdge(cls5, cls4, ont));
        assertEquals("Incorrect incoming edges to " + cls4, expectedEdges, 
                wrapper.getIncomingEdges(cls4));
        
        expectedEdges = new HashSet<OWLGraphEdge>();
        expectedEdges.add(new OWLGraphEdge(cls6, cls5, ont));
        assertEquals("Incorrect incoming edges to " + cls5, expectedEdges, 
                wrapper.getIncomingEdges(cls5));
        
        expectedEdges = new HashSet<OWLGraphEdge>();
        expectedEdges.add(new OWLGraphEdge(cls7, cls6, ont));
        expectedEdges.add(new OWLGraphEdge(cls8, cls6, ont));
        expectedEdges.add(new OWLGraphEdge(cls11, cls6, ont));
        assertEquals("Incorrect incoming edges to " + cls6, expectedEdges, 
                wrapper.getIncomingEdges(cls6));
        
        
        expectedEdges = new HashSet<OWLGraphEdge>();
        assertEquals("Incorrect incoming edges to " + cls7, expectedEdges, 
                wrapper.getIncomingEdges(cls7));
        assertEquals("Incorrect incoming edges to " + cls11, expectedEdges, 
                wrapper.getIncomingEdges(cls11));
        assertEquals("Incorrect incoming edges to " + cls9, expectedEdges, 
                wrapper.getIncomingEdges(cls9));
        assertEquals("Incorrect incoming edges to " + cls10, expectedEdges, 
                wrapper.getIncomingEdges(cls10));
        
        expectedEdges = new HashSet<OWLGraphEdge>();
        expectedEdges.add(new OWLGraphEdge(cls9, cls8, ont));
        expectedEdges.add(new OWLGraphEdge(cls10, cls8, ont));
        assertEquals("Incorrect incoming edges to " + cls8, expectedEdges, 
                wrapper.getIncomingEdges(cls8));
        
        
        //check the disjoint axioms. Generate a Collection of siblings.
        Collection<Collection<OWLClass>> siblingSet = new HashSet<Collection<OWLClass>>();
        Collection<OWLClass> siblings1 = new HashSet<OWLClass>();
        siblings1.add(cls7);
        siblings1.add(cls8);
        siblings1.add(cls11);
        siblingSet.add(siblings1);
        Collection<OWLClass> siblings2 = new HashSet<OWLClass>();
        siblings2.add(cls9);
        siblings2.add(cls10);
        siblingSet.add(siblings2);
        
        OWLDataFactory f = wrapper.getManager().getOWLDataFactory();
        OWLObjectProperty inTaxon = f.getOWLObjectProperty(
                IRI.create(GenerateTaxonOntology.INTAXONRELID));
        
        for (Collection<OWLClass> siblings: siblingSet) {
            for (OWLClass sibling1 : siblings) {
                for (OWLClass sibling2 : siblings) {
                    if (sibling1 != sibling2) {
                        OWLClassExpression ce1 = 
                                f.getOWLObjectSomeValuesFrom(inTaxon, sibling1);
                        OWLClassExpression ce2 = 
                                f.getOWLObjectSomeValuesFrom(inTaxon, sibling2);
                        assertTrue("Missing disjoint axiom", 
                                wrapper.getSourceOntology().containsAxiom(
                                        f.getOWLDisjointClassesAxiom(ce1, ce2)));
                        
                        assertTrue("Missing disjoint axiom", 
                                wrapper.getSourceOntology().containsAxiom(
                                f.getOWLDisjointClassesAxiom(sibling1, sibling2)));
                    }
                }
            }
        }
    }
}
