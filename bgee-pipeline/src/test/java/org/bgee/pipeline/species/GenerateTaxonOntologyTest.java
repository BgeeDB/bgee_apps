package org.bgee.pipeline.species;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.TestAncestor;
import org.junit.Test;
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
     * Test {@link GenerateTaxonOntology#generateOntology(String, Set)}. Generates 
     * the ontology  from the data file {@code src/test/resources/species/sample.dat}.
     */
    @Test
    public void shouldGenerateOntology() throws IllegalArgumentException, 
        OWLOntologyCreationException, OWLOntologyStorageException, IOException {
        
        String dataFile = this.getClass().getResource("/species/sample.dat").getFile();
        String taxId1 = "NCBITaxon:1";
        String taxId2 = "NCBITaxon:131567";
        String taxId3 = "NCBITaxon:2";
        String taxId4 = "NCBITaxon:201174";
        String taxId5 = "NCBITaxon:1760";
        String taxId6 = "NCBITaxon:84992";
        String taxId7 = "NCBITaxon:84993";
        String taxId8 = "NCBITaxon:85003";
        String taxId9 = "NCBITaxon:1223512";
        String taxId10 = "NCBITaxon:12235122";
        String taxId11 = "NCBITaxon:65645";
        Set<String> taxonIds = new HashSet<String>(Arrays.asList(taxId3, taxId4, 
                taxId5, taxId6, taxId7, taxId8, taxId9, taxId10, taxId11));
        
        GenerateTaxonOntology generate = new GenerateTaxonOntology();
        OWLOntology ont = generate.generateOntology(dataFile,  taxonIds);
        OWLGraphWrapper wrapper = new OWLGraphWrapper(ont);
        
        assertEquals("Incorrect number of classes in generated ontology", 11, 
                wrapper.getAllOWLClasses().size());
        
        OWLClass cls1 = wrapper.getOWLClassByIdentifier(taxId1);
        OWLClass cls2 = wrapper.getOWLClassByIdentifier(taxId2);
        OWLClass cls3 = wrapper.getOWLClassByIdentifier(taxId3);
        OWLClass cls4 = wrapper.getOWLClassByIdentifier(taxId4);
        OWLClass cls5 = wrapper.getOWLClassByIdentifier(taxId5);
        OWLClass cls6 = wrapper.getOWLClassByIdentifier(taxId6);
        OWLClass cls7 = wrapper.getOWLClassByIdentifier(taxId7);
        OWLClass cls8 = wrapper.getOWLClassByIdentifier(taxId8);
        OWLClass cls11 = wrapper.getOWLClassByIdentifier(taxId11);
        OWLClass cls9 = wrapper.getOWLClassByIdentifier(taxId9);
        OWLClass cls10 = wrapper.getOWLClassByIdentifier(taxId10);
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
        assertEquals("Incorrect root in generated ontology", 1, 
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
        Collection<Set<OWLClass>> siblingSet = new HashSet<Set<OWLClass>>();
        Set<OWLClass> siblings1 = new HashSet<OWLClass>();
        siblings1.add(cls7);
        siblings1.add(cls8);
        siblings1.add(cls11);
        siblingSet.add(siblings1);
        Set<OWLClass> siblings2 = new HashSet<OWLClass>();
        siblings2.add(cls9);
        siblings2.add(cls10);
        siblingSet.add(siblings2);
        
        OWLDataFactory f = wrapper.getManager().getOWLDataFactory();
        OWLObjectProperty inTaxon = f.getOWLObjectProperty(
                IRI.create(GenerateTaxonOntology.INTAXONRELID));
        
        for (Set<OWLClass> siblings: siblingSet) {
            assertTrue("Missing disjoint axiom", 
                    wrapper.getSourceOntology().containsAxiom(
                    f.getOWLDisjointClassesAxiom(siblings)));
            
            Set<OWLClassExpression> expressions = new HashSet<OWLClassExpression>();
            for (OWLClass sibling : siblings) {
                expressions.add(f.getOWLObjectSomeValuesFrom(inTaxon, sibling));
            }
            assertTrue("Missing disjoint axiom", 
                    wrapper.getSourceOntology().containsAxiom(
                    f.getOWLDisjointClassesAxiom(expressions)));
        }
    }
}
