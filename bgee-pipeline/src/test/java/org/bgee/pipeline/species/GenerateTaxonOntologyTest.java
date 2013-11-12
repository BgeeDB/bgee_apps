package org.bgee.pipeline.species;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.OntologyUtils;
import org.bgee.pipeline.TestAncestor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

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
        
        System.out.println("YO " + wrapper.getOBOSynonyms(wrapper.getOWLObjectByIdentifier("NCBITaxon:2")));
        
        assertEquals("Incorrect number of classes in geenrated ontology", 7, 
                wrapper.getAllOWLClasses().size());
        assertNotNull("Incorrect classes in generated ontology", 
                wrapper.getOWLClassByIdentifier("NCBITaxon:1"));
        assertNotNull("Incorrect classes in generated ontology", 
                wrapper.getOWLClassByIdentifier("NCBITaxon:131567"));
        assertNotNull("Incorrect classes in generated ontology", 
                wrapper.getOWLClassByIdentifier("NCBITaxon:2"));
        assertNotNull("Incorrect classes in generated ontology", 
                wrapper.getOWLClassByIdentifier("NCBITaxon:201174"));
        assertNotNull("Incorrect classes in generated ontology", 
                wrapper.getOWLClassByIdentifier("NCBITaxon:1760"));
        assertNotNull("Incorrect classes in generated ontology", 
                wrapper.getOWLClassByIdentifier("NCBITaxon:84992"));
        assertNotNull("Incorrect classes in generated ontology", 
                wrapper.getOWLClassByIdentifier("NCBITaxon:84993"));
        
        Set<OWLClass> roots = wrapper.getOntologyRoots();
        assertEquals("Incorrect root in geenrated ontology", 1, 
                roots.size());
        assertEquals("Incorrect root in geenrated ontology", roots.iterator().next(), 
                wrapper.getOWLClassByIdentifier("NCBITaxon:1"));
        
        assertEquals("Incorrect leaf in generated ontology", 0, 
                wrapper.getDescendants(
                        wrapper.getOWLClassByIdentifier("NCBITaxon:84993")).size());
        
        System.out.println(wrapper.getOBOSynonyms( wrapper.getOWLClassByIdentifier("NCBITaxon:2")));
    }
}
