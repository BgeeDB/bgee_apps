package org.bgee.pipeline.uberon;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import owltools.graph.OWLGraphWrapper;
import owltools.io.ParserWrapper;

/**
 * Test the functionalities of {@link org.bgee.pipeline.uberon.OWLGraphReducer}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13, Feb 2013
 * @since Bgee 13
 *
 */
public class OWLGraphReducerTest 
{
	private final static Logger LOGGER = LogManager.getLogger(OWLGraphReducer.class.getName());
	/**
	 * A <code>OWLGraphWrapper</code> used to store an ontology 
	 * to perform modifications on. 
	 */
	private OWLGraphWrapper graphWrapper;
	
	/**
	 * A <code>TestWatcher</code> to log starting, succeeded and failed tests. 
	 */
	@Rule
	public TestWatcher watchman = new TestWatcher() {
	    @Override
	    protected void starting(Description description) {
	        LOGGER.info("Starting test: {}", description);
	    }
	    @Override
	    protected void failed(Throwable e, Description description) {
	    	if (LOGGER.isErrorEnabled()) {
	            LOGGER.error("Test failed: " + description, e);
	    	}
	    }
	    @Override
	    protected void succeeded(Description description) {
	        LOGGER.info("Test succeeded: {}", description);
	    }
	};
	
	/**
	 * Load the (really basic) ontology <code>/ontologies/OWLGraphReducerTest.obo</code> 
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
		LOGGER.debug("Wrapping test ontology into OWLGraphWrapper...");
		ParserWrapper parserWrapper = new ParserWrapper();
        OWLOntology ont = parserWrapper.parse(
        		this.getClass().getResource("/ontologies/OWLGraphReducerTest.obo").getFile());
    	this.graphWrapper = new OWLGraphWrapper(ont);
		LOGGER.debug("Done.");
	}
	
	/**
	 * Test the functionalities of 
	 * {@link org.bgee.pipeline.uberon.OWLGraphReducer#filterRelations(Collection, boolean)}.
	 */
	@Test
	public void filterRelationsTest()
	{
		
	}
}
