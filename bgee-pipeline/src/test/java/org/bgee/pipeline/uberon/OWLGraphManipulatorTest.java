package org.bgee.pipeline.uberon;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.TestAncestor;
import org.junit.Before;
import org.junit.Test;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import owltools.graph.OWLGraphWrapper;
import owltools.io.ParserWrapper;

/**
 * Test the functionalities of {@link org.bgee.pipeline.uberon.OWLGraphManipulator}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13, Feb 2013
 * @since Bgee 13
 *
 */
public class OWLGraphManipulatorTest extends TestAncestor
{
    private final static Logger log = 
    		LogManager.getLogger(OWLGraphManipulatorTest.class.getName());
    /**
     * The <code>OWLGraphWrapper</code> used to perform the test. 
     */
    private OWLGraphManipulator graphManipulator;
	
	/**
	 * Default Constructor. 
	 */
	public OWLGraphManipulatorTest()
	{
		super();
	}
	@Override
	protected Logger getLogger() {
		return log;
	}
	
	/**
	 * Load the (really basic) ontology <code>/ontologies/OWLGraphManipulatorTest.obo</code> 
	 * into {@link #graphWrapper}.
	 * It is loaded before the execution of each test, so that a test can modify it 
	 * without impacting another test.
	 *  
	 * @throws OWLOntologyCreationException 
	 * @throws OBOFormatParserException
	 * @throws IOException
	 * 
	 * @see #graphWrapper
	 */
	@Before
	public void loadTestOntology() 
			throws OWLOntologyCreationException, OBOFormatParserException, IOException
	{
		log.debug("Wrapping test ontology into OWLGraphManipulator...");
		ParserWrapper parserWrapper = new ParserWrapper();
        OWLOntology ont = parserWrapper.parse(
        		this.getClass().getResource("/ontologies/OWLGraphManipulatorTest.obo").getFile());
    	this.graphManipulator = new OWLGraphManipulator(new OWLGraphWrapper(ont));
		log.debug("Done wrapping test ontology into OWLGraphManipulator.");
	}
	
	/**
	 * Test the functionalities of 
	 * {@link org.bgee.pipeline.uberon.OWLGraphManipulator#filterRelations(Collection, boolean)}.
	 */
	@Test
	public void shouldFilterRelationsTest()
	{
		
	}
	
	/**
	 * Test the functionalities of 
	 * {@link OWLGraphManipulator#filterSubgraphs(Collection)}.
	 */
	@Test
	public void shouldFilterSubgraphs()
	{
		//The test ontology includes 3 subgraphs (2 to be kept, 1 to be removed), 
		//with one term part of both a subgraph to remove and a subgraph to keep.
		//All terms belonging to the subgraph to remove, except this common term, 
		//should be removed.
		
		//first, let's get the number of classes in the ontology
		int classCount = this.graphManipulator.getOwlGraphWrapper()
				    .getSourceOntology().getClassesInSignature().size();
		
		//filter the subgraphs, we want to keep: 
		//FOO:0001 corresponds to term "A", root of the first subgraph to keep. 
		//FOO:0013 to "subgraph3_root".
		Collection<String> toRemove = new ArrayList<String>();
		toRemove.add("FOO:0006");
		toRemove.add("FOO:0013");
		int countRemoved = this.graphManipulator.filterSubgraphs(toRemove);
		
		//The test ontology is designed so that 7 classes should have been removed
		assertEquals("Incorrect number of classes removed", 7, countRemoved);
		//test that these classes were actually removed from the ontology
		int newClassCount = this.graphManipulator.getOwlGraphWrapper()
			    .getSourceOntology().getClassesInSignature().size();
		assertEquals("filterSubgraph did not return the correct number of classes removed", 
				classCount - newClassCount, countRemoved);
		
		//Test that the term part of both subgraphs was not incorrectly removed.
		//Its ID is FOO:0011
		assertNotNull("The term part of both subgraphs was incorrectly removed", 
				this.graphManipulator.getOwlGraphWrapper().getOWLClass("FOO:0011"));
	}
}
