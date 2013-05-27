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
		this.getLogger().info("Ye: {}", this.graphManipulator.getOwlGraphWrapper().getOWLClassByIdentifier("FOO:0005").getSuperClasses(this.graphManipulator.getOwlGraphWrapper().getSourceOntology()));
	}
	
	/**
	 * Test the functionalities of 
	 * {@link OWLGraphManipulator#filterSubgraphs(Collection)}.
	 */
	@Test
	public void shouldFilterSubgraphs()
	{
		//The test ontology includes 3 subgraphs (2 to be kept, 1 to be removed), 
		//with two terms part of both a subgraph to remove and a subgraph to keep.
		//All terms belonging to the subgraph to remove, except these common terms, 
		//should be removed.
		
		//first, let's get the number of classes in the ontology
		int classCount = this.graphManipulator.getOwlGraphWrapper()
				    .getSourceOntology().getClassesInSignature().size();
		
		//filter the subgraphs, we want to keep: 
		//FOO:0001 corresponds to term "A", root of the first subgraph to keep. 
		//FOO:0013 to "subgraph3_root".
		//FOO:0014 to "subgraph4_root_subgraph2" 
		//(both root of a subgraph to keep, and part of a subgraph to remove).
		//subgraph starting from FOO:0006 "subgraph2_root" will be removed
		Collection<String> toKeep = new ArrayList<String>();
		toKeep.add("FOO:0002");
		toKeep.add("FOO:0013");
		toKeep.add("FOO:0014");
		int countRemoved = this.graphManipulator.filterSubgraphs(toKeep);
		
		//The test ontology is designed so that 6 classes should have been removed
		assertEquals("Incorrect number of classes removed", 6, countRemoved);
		//test that these classes were actually removed from the ontology
		int newClassCount = this.graphManipulator.getOwlGraphWrapper()
			    .getSourceOntology().getClassesInSignature().size();
		assertEquals("filterSubgraph did not return the correct number of classes removed", 
				classCount - newClassCount, countRemoved);
		
		//Test that the terms part of both subgraphs were not incorrectly removed.
		//Their IDs are FOO:0011 and FOO:0014, they have slighty different relations to the root
		assertNotNull("A term part of both subgraphs was incorrectly removed", 
				this.graphManipulator.getOwlGraphWrapper().getOWLClassByIdentifier("FOO:0011"));
		assertNotNull("A term part of both subgraphs was incorrectly removed", 
				this.graphManipulator.getOwlGraphWrapper().getOWLClassByIdentifier("FOO:0014"));
	}
	
	/**
	 * Test the functionalities of 
	 * {@link OWLGraphManipulator#removeSubgraphs(Collection)}.
	 */
	@Test
	public void shouldRemoveSubgraphs()
	{
		//The test ontology includes 3 subgraphs (2 to be kept, 1 to be removed), 
		//with two terms part of both a subgraph to remove and a subgraph to keep.
		//All terms belonging to the subgraph to remove, except these common terms, 
		//should be removed.

		//first, let's get the number of classes in the ontology
		int classCount = this.graphManipulator.getOwlGraphWrapper()
				.getSourceOntology().getClassesInSignature().size();

		//remove the subgraph
		Collection<String> toRemove = new ArrayList<String>();
		toRemove.add("FOO:0006");
		int countRemoved = this.graphManipulator.removeSubgraphs(toRemove);

		//The test ontology is designed so that 6 classes should have been removed
		assertEquals("Incorrect number of classes removed", 6, countRemoved);
		//test that these classes were actually removed from the ontology
		int newClassCount = this.graphManipulator.getOwlGraphWrapper()
				.getSourceOntology().getClassesInSignature().size();
		assertEquals("removeSubgraph did not return the correct number of classes removed", 
				classCount - newClassCount, countRemoved);

		//Test that the terms part of both subgraphs, or part of independent subgraphs, 
		//were not incorrectly removed.
		//Their IDs are FOO:0011 and FOO:0014, they have slighty different relations to the root
		assertNotNull("A term part of both subgraphs was incorrectly removed", 
				this.graphManipulator.getOwlGraphWrapper().getOWLClassByIdentifier("FOO:0011"));
		assertNotNull("A term part of both subgraphs was incorrectly removed", 
				this.graphManipulator.getOwlGraphWrapper().getOWLClassByIdentifier("FOO:0014"));
	}
}
