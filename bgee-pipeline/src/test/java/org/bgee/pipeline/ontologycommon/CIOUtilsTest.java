package org.bgee.pipeline.ontologycommon;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.TestAncestor;
import org.junit.Test;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import owltools.graph.OWLGraphWrapper;

/**
 * Unit tests for {@link CIOUtils}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class CIOUtilsTest extends TestAncestor {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(CIOUtilsTest.class.getName());
    
    /**
     * Default Constructor. 
     */
    public CIOUtilsTest() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test correct loading of the CIO at instantiation of {@code CIOUtils}.
     * @throws OBOFormatParserException
     * @throws OWLOntologyCreationException
     * @throws IOException
     */
    @Test
    public void shouldLoadCIO() throws OBOFormatParserException, OWLOntologyCreationException, 
    IOException {
        OWLOntology ont = OntologyUtils.loadOntology(CIOUtilsTest.class.
                getResource("/ontologies/confidence_information_ontology.owl").getFile());
        OWLGraphWrapper wrapper = new OWLGraphWrapper(ont);
        CIOUtils utils = new CIOUtils(wrapper);
        
        
    }
}
