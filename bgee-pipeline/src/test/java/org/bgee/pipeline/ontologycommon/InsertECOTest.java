package org.bgee.pipeline.ontologycommon;

import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.ontologycommon.EvidenceOntologyDAO.ECOTermTO;
import org.bgee.pipeline.TestAncestor;
import org.bgee.pipeline.ontologycommon.InsertECO;
import org.bgee.pipeline.ontologycommon.OntologyUtils;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * Tests for {@link InsertECO}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Apr. 2015
 * @since Bgee 13
 */
public class InsertECOTest extends TestAncestor {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(InsertECOTest.class.getName());
    
    /**
     * Default Constructor. 
     */
    public InsertECOTest() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test {@link InsertECO#insert(OWLOntology)}
     */
    //warnings raised because of the mockito argument captor, discard
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void shouldInsertECO() throws OBOFormatParserException, 
    OWLOntologyCreationException, IOException {
        MockDAOManager mockManager = new MockDAOManager();
        InsertECO insert = new InsertECO(mockManager);
        
        Set<ECOTermTO> expectedTOs = new HashSet<ECOTermTO>();
        expectedTOs.add(new ECOTermTO("ECO:0000091", "isb-sib evidence", null));
        expectedTOs.add(new ECOTermTO("ECO:0000006", "experimental evidence", 
                "An evidence type that is based on the results of a laboratory assay."));
        expectedTOs.add(new ECOTermTO("ECO:0000352", "evidence used in manual assertion", 
                "A type of evidence that is used in an manual assertion."));
        expectedTOs.add(new ECOTermTO("ECO:0000501", "evidence used in automatic assertion", 
                "A type of evidence that is used in an automatic assertion."));
        expectedTOs.add(new ECOTermTO("ECO:0000269", 
                "experimental evidence used in manual assertion", 
                "A type of experimental evidence that is used in a manual assertion."));
        expectedTOs.add(new ECOTermTO("ECO:0000059", 
                "experimental phenotypic evidence", 
                "Experimental evidence that is based on the expression of a genotype "
                + "in an environment."));
        expectedTOs.add(new ECOTermTO("ECO:0000017", 
                "ectopic expression evidence", 
                "Used when an annotation is made based on the analysis of the phenotype "
                + "of a wild-type or mutant transgenic organism that has been engineered "
                + "to overexpress or ectopically express the gene product in question."));
        expectedTOs.add(new ECOTermTO("ECO:0001273", 
                "ectopic expression evidence used in manual assertion", 
                "A type of ectopic expression evidence that is used in a manual assertion."));

        insert.insert(OntologyUtils.loadOntology(
                this.getClass().getResource("/ontologies/test_insert_eco.obo").getFile()));
        
        ArgumentCaptor<Set> ecoTermTOsArg = ArgumentCaptor.forClass(Set.class);
        verify(mockManager.mockEvidenceOntologyDAO).insertECOTerms(ecoTermTOsArg.capture());
        if (!TOComparator.areTOCollectionsEqual(expectedTOs, ecoTermTOsArg.getValue())) {
            throw new AssertionError("Incorrect ECO terms generated, " +
                    "expected " + expectedTOs + ", but was " + ecoTermTOsArg.getValue());
        }
    }
    
}
