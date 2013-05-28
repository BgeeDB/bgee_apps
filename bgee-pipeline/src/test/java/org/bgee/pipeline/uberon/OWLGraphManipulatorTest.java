package org.bgee.pipeline.uberon;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.TestAncestor;
import org.junit.Before;
import org.junit.Test;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import owltools.graph.OWLGraphEdge;
import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapperEdges;
import owltools.io.ParserWrapper;

/**
 * Test the functionalities of {@link org.bgee.pipeline.uberon.OWLGraphManipulator}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13, May 2013
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
	 * {@link org.bgee.pipeline.uberon.OWLGraphManipulator#filterRelations(Collection, boolean)} 
	 * with the <code>boolean</code> parameters set to <code>false</code>.
	 */
	@Test
	public void shouldFilterRelations()
	{
		//filter relations to keep only is_a, part_of and develops_from
		//4 relations should be removed
		this.shouldFilterOrRemoveRelations(Arrays.asList("BFO:0000050", "RO:0002202"), 
				false, 4, true);
	}
	/**
	 * Test the functionalities of 
	 * {@link org.bgee.pipeline.uberon.OWLGraphManipulator#filterRelations(Collection, boolean)} 
	 * with the <code>boolean</code> parameters set to <code>true</code>.
	 */
	@Test
	public void shouldFilterRelationsWithSubRel()
	{
		//filter relations to keep is_a, part_of, develops_from, 
		//and their sub-relations.
		//2 relations should be removed
		this.shouldFilterOrRemoveRelations(Arrays.asList("BFO:0000050", "RO:0002202"), 
				true, 2, true);
	}
	/**
	 * Test the functionalities of 
	 * {@link org.bgee.pipeline.uberon.OWLGraphManipulator#filterRelations(Collection, boolean)} 
	 * when filtering a relation with a non-OBO-style ID (in this method, 
	 * <code>http://semanticscience.org/resource/SIO_000657</code>).
	 */
	@Test
	public void shouldFilterRelationsWithNonOboId()
	{
		//filter relations to keep only is_a and transformation_of relations
		//10 relations should be removed
		this.shouldFilterOrRemoveRelations(Arrays.asList("http://semanticscience.org/resource/SIO_000657"), 
				true, 10, true);
	}	
	/**
	 * Test the functionalities of 
	 * {@link org.bgee.pipeline.uberon.OWLGraphManipulator#filterRelations(Collection, boolean)} 
	 * when filtering all relations but is_a.
	 */
	@Test
	public void shouldFilterAllRelations()
	{
		//filter relations to keep only is_a relations
		//11 relations should be removed
		this.shouldFilterOrRemoveRelations(Arrays.asList(""), 
				true, 11, true);
	}
	/**
	 * Method to test the functionalities of 
	 * {@link OWLGraphManipulator#filterRelations(Collection, boolean)} and 
	 * {@link OWLGraphManipulator#removeRelations(Collection, boolean)}
	 * with various configurations, called by the methods performing the actual unit test. 
	 * 
	 * @param rels 				corresponds to the first parameter of 
	 * 							the <code>filterRelations</code> or 
	 * 							<code>removeRelations</code> method.
	 * @param subRels			corresponds to the second parameter of 
	 * 							the <code>filterRelations</code> or 
	 * 							<code>removeRelations</code> method.
	 * @param expRelsRemoved 	An <code>int</code> representing the expected number 
	 * 							of relations removed
	 * @param filter 			A <code>boolean</code> defining whether the method tested is 
	 * 							<code>filterRelations</code>, or <code>removeRelations</code>. 
	 * 							If <code>true</code>, the method tested is 
	 * 							<code>filterRelations</code>.
	 */
	private void shouldFilterOrRemoveRelations(Collection<String> rels, 
			boolean subRels, int expRelsRemoved, boolean filter)
	{
		//get the original number of axioms
		int axiomCountBefore = this.graphManipulator.getOwlGraphWrapper()
			    .getSourceOntology().getAxiomCount();
		
		//filter relations to keep 
		int relRemovedCount = 0;
		if (filter) {
			relRemovedCount = this.graphManipulator.filterRelations(rels, subRels);
		} else {
			relRemovedCount = this.graphManipulator.removeRelations(rels, subRels);
		}
		//expRelsRemoved relations should have been removed
		assertEquals("Incorrect number of relations removed", expRelsRemoved, relRemovedCount);
		
		//get the number of axioms after removal
		int axiomCountAfter = this.graphManipulator.getOwlGraphWrapper()
			    .getSourceOntology().getAxiomCount();
		//check that it corresponds to the returned value
		assertEquals("The number of relations removed does not correspond to " +
				"the number of axioms removed", 
				axiomCountBefore - axiomCountAfter, relRemovedCount);
	}
	
	/**
	 * Test the functionalities of 
	 * {@link org.bgee.pipeline.uberon.OWLGraphManipulator#removeRelations(Collection, boolean)} 
	 * with the <code>boolean</code> parameters set to <code>false</code>.
	 */
	@Test
	public void shouldRemoveRelations()
	{
		//remove part_of and develops_from relations
		//7 relations should be removed
		this.shouldFilterOrRemoveRelations(Arrays.asList("BFO:0000050", "RO:0002202"), 
			false, 7, false);
	}
	/**
	 * Test the functionalities of 
	 * {@link org.bgee.pipeline.uberon.OWLGraphManipulator#removeRelations(Collection, boolean)} 
	 * with the <code>boolean</code> parameters set to <code>true</code>.
	 */
	@Test
	public void shouldRemoveRelationsWithSubRel()
	{
		//remove develops_from relations and sub-relations
		//2 relations should be removed
		this.shouldFilterOrRemoveRelations(Arrays.asList("RO:0002202"), 
			true, 2, false);
	}
	/**
	 * Test the functionalities of 
	 * {@link org.bgee.pipeline.uberon.OWLGraphManipulator#removeRelations(Collection, boolean)} 
	 * with an empty list of relations to remove, to check that it actually removed nothing.
	 */
	@Test
	public void shouldRemoveNoRelation()
	{
		//remove nothing
		//0 relations should be removed
		this.shouldFilterOrRemoveRelations(Arrays.asList(""), 
			true, 0, false);
	}
	
	@Test
	public void test() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		OWLGraphWrapper wrapper = this.graphManipulator.getOwlGraphWrapper();
    	
		Collection<OWLGraphEdge> edges = wrapper.getEdgesBetween(
				wrapper.getOWLClassByIdentifier("FOO:0004"),
				wrapper.getOWLClassByIdentifier("FOO:0001"));
		for (OWLGraphEdge edge: edges) {
			log.info("YE {}", edge);
			log.info("YA {}", edge.getQuantifiedPropertyList());
			for (OWLGraphEdge edge2: wrapper.getOWLGraphEdgeSubsumers(edge)) {
			    log.info("YO {}", edge2);
			}
		}
		
		Collection<OWLGraphEdge> edges2 = wrapper.getOutgoingEdges(wrapper.getOWLClassByIdentifier("FOO:0004"));
		for (OWLGraphEdge edge: edges2) {
			if (wrapper.getIdentifier(edge.getTarget()).equals("FOO:0002")) {
				for (OWLGraphEdge edge3: wrapper.getOWLGraphEdgeSubsumers(edge)) {
				    log.info("YOOO1 {}", wrapper.edgeToTargetExpression(edge3));
				}
				Collection<OWLGraphEdge> edges3 = wrapper.getOutgoingEdges(edge.getTarget());
				for (OWLGraphEdge edge2: edges3) {
					if (wrapper.getIdentifier(edge2.getTarget()).equals("FOO:0001")) {
						OWLGraphEdge combine = wrapper.combineEdgePair(edge.getSource(), 
								edge, edge2, 0);
						log.info("YII {}", combine.getQuantifiedPropertyList().size());
						for (OWLGraphEdge edge3: wrapper.getOWLGraphEdgeSubsumers(combine)) {
						    log.info("YOOO {}", wrapper.edgeToTargetExpression(edge3));
						}
					}
				}
			}
		}
	}
	
	
	
	/**
	 * Test the functionalities of 
	 * {@link OWLGraphManipulator#filterSubgraphs(Collection)}.
	 */
	@Test
	public void shouldFilterSubgraphs()
	{
		//The test ontology includes several subgraphs, with 1 to be removed, 
		//and with two terms part of both a subgraph to remove and a subgraph to keep 
		//(FOO:0011, FOO:0014).
		//All terms belonging to the subgraph to remove, except these common terms, 
		//should be removed.
		
		//first, let's get the number of classes in the ontology
		int classCount = this.graphManipulator.getOwlGraphWrapper()
				    .getSourceOntology().getClassesInSignature().size();
		
		//filter the subgraphs, we want to keep: 
		//FOO:0002 corresponds to term "A", root of the first subgraph to keep. 
		//FOO:0013 to "subgraph3_root".
		//FOO:0014 to "subgraph4_root_subgraph2" 
		//(both root of a subgraph to keep, and part of a subgraph to remove).
		//subgraph starting from FOO:0006 "subgraph2_root" will be removed, 
		//(but not FOO:0006 itself, because it is an ancestor of FOO:0014; 
		//if FOO:0014 was not an allowed root, then FOO:0006 would be removed)
		Collection<String> toKeep = new ArrayList<String>();
		toKeep.add("FOO:0002");
		toKeep.add("FOO:0013");
		toKeep.add("FOO:0014");
		int countRemoved = this.graphManipulator.filterSubgraphs(toKeep);
		
		//The test ontology is designed so that 5 classes should have been removed
		assertEquals("Incorrect number of classes removed", 5, countRemoved);
		
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
		
		//now, we need to check that the relations FOO:0003 B is_a FOO:0001 root, 
		//FOO:0004 C part_of FOO:0001 root, FOO:0005 D is_a FOO:0001 root
		//have been removed (terms should be kept as it is part of a subgraph to keep, 
		//but the relations to the root are still undesired subgraphs, 
		//that should be removed)
		OWLClass root = 
				this.graphManipulator.getOwlGraphWrapper().getOWLClassByIdentifier("FOO:0001");
		for (OWLGraphEdge incomingEdge: 
		    this.graphManipulator.getOwlGraphWrapper().getIncomingEdges(root)) {
			assertNotEquals("The relation FOO:0003 B is_a FOO:0001 root, " +
					"causing an undesired subgraph, was not correctly removed", 
					"FOO:0003", 
					this.graphManipulator.getOwlGraphWrapper().getIdentifier(
							incomingEdge.getSource()));
			assertNotEquals("The relation FOO:0004 C is_a FOO:0001 root, " +
					"causing an undesired subgraph, was not correctly removed", 
					"FOO:0004", 
					this.graphManipulator.getOwlGraphWrapper().getIdentifier(
							incomingEdge.getSource()));
			assertNotEquals("The relation FOO:0005 D is_a FOO:0001 root, " +
					"causing an undesired subgraph, was not correctly removed", 
					"FOO:0005", 
					this.graphManipulator.getOwlGraphWrapper().getIdentifier(
							incomingEdge.getSource()));
		}
	}
	
	/**
	 * Test the functionalities of 
	 * {@link OWLGraphManipulator#removeSubgraphs(Collection, boolean)}, 
	 * with the <code>boolean</code> parameter set to <code>true</code>.
	 */
	@Test
	public void shouldRemoveSubgraphs()
	{
		//The test ontology includes several subgraphs, with 1 to be removed, 
		//and with two terms part of both a subgraph to remove and a subgraph to keep.
		//All terms belonging to the subgraph to remove, except these common terms, 
		//should be removed.

		//first, let's get the number of classes in the ontology
		int classCount = this.graphManipulator.getOwlGraphWrapper()
				.getSourceOntology().getClassesInSignature().size();

		//remove the subgraph
		Collection<String> toRemove = new ArrayList<String>();
		toRemove.add("FOO:0006");
		//add as a root to remove a term that is in the FOO:0006 subgraph, 
		//to check if the ancestors check will not lead to keep erroneously FOO:0007
		toRemove.add("FOO:0008");
		int countRemoved = this.graphManipulator.removeSubgraphs(toRemove, true);

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
	
	/**
	 * Test the functionalities of 
	 * {@link OWLGraphManipulator#removeSubgraphs(Collection, boolean)}, 
	 * with the <code>boolean</code> parameter set to <code>false</code>.
	 */
	@Test
	public void shouldRemoveSubgraphsAndSharedClasses()
	{
		//The test ontology includes several subgraphs, with 1 to be removed, 
		//and with two terms part of both a subgraph to remove and a subgraph to keep.
		//All terms belonging to the subgraph to remove, EVEN these common terms, 
		//should be removed.

		//first, let's get the number of classes in the ontology
		int classCount = this.graphManipulator.getOwlGraphWrapper()
				.getSourceOntology().getClassesInSignature().size();

		//remove the subgraph
		Collection<String> toRemove = new ArrayList<String>();
		toRemove.add("FOO:0006");
		int countRemoved = this.graphManipulator.removeSubgraphs(toRemove, false);

		//The test ontology is designed so that 8 classes should have been removed
		assertEquals("Incorrect number of classes removed", 8, countRemoved);
		//test that these classes were actually removed from the ontology
		int newClassCount = this.graphManipulator.getOwlGraphWrapper()
				.getSourceOntology().getClassesInSignature().size();
		assertEquals("removeSubgraph did not return the correct number of classes removed", 
				classCount - newClassCount, countRemoved);
	}
}
