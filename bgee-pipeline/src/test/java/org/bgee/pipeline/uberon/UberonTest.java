package org.bgee.pipeline.uberon;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.TestAncestor;
import org.junit.Test;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * Unit tests for {@link Uberon}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class UberonTest extends TestAncestor {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(UberonTest.class.getName());

    /**
     * Default Constructor. 
     */
    public UberonTest() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test the method {@link Uberon#extractTaxonIds(String)}.
     */
    @Test
    public void shouldExtractTaxonIds() throws OWLOntologyCreationException, 
        OBOFormatParserException, IllegalArgumentException, IOException {
        
        Set<String> expectedTaxonIds = new HashSet<String>();
        //this one should be obtained from the 
        //oboInOwl:treat-xrefs-as-reverse-genus-differentia ontology annotations
        expectedTaxonIds.add("NCBITaxon:8292"); 
        //this one should be obtained from an "only_in_taxon" property
        expectedTaxonIds.add("NCBITaxon:6656"); 
        //this one should be obtained from an "in_taxon" property generating 
        //a class expression equivalent to owl:nothing
        expectedTaxonIds.add("NCBITaxon:110815");
        
        assertEquals("Incorrect taxon IDs extracted", expectedTaxonIds, 
                new Uberon().extractTaxonIds(
                this.getClass().getResource("/uberon/uberonTaxonTest.owl").getPath()));
    }
    
}
