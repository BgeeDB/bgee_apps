package org.bgee.pipeline.ontologycommon;

import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTO;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTO.ConfidenceLevel;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTO.EvidenceConcordance;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTO.EvidenceTypeConcordance;
import org.bgee.pipeline.TestAncestor;
import org.bgee.pipeline.ontologycommon.InsertCIO;
import org.bgee.pipeline.ontologycommon.OntologyUtils;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * Tests for {@link InsertCIO}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Apr. 2015
 * @since Bgee 13
 */
public class InsertCIOTest extends TestAncestor {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(InsertCIOTest.class.getName());
    
    /**
     * Default Constructor. 
     */
    public InsertCIOTest() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test {@link InsertCIO#insert(OWLOntology)}
     */
    //warnings raised because of the mockito argument captor, discard
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void shouldInsertCIO() throws OBOFormatParserException, 
    OWLOntologyCreationException, IOException {
        MockDAOManager mockManager = new MockDAOManager();
        InsertCIO insert = new InsertCIO(mockManager);
        
        Set<CIOStatementTO> expectedTOs = new HashSet<CIOStatementTO>();
        expectedTOs.add(new CIOStatementTO("CIO:0000003", 
                "high confidence from single evidence", 
                "A confidence statement from single evidence related to assertions "
                + "supported by a highly reliable evidence.", 
                true, ConfidenceLevel.HIGH_CONFIDENCE, EvidenceConcordance.SINGLE_EVIDENCE, 
                null));
        expectedTOs.add(new CIOStatementTO("CIO:0000010", 
                "confidence statement from strongly conflicting evidence lines of multiple types", 
                "A confidence statement from conflicting evidence lines of multiple types, "
                + "where evidence lines are in strong contradiction, meaning that "
                + "the supporting evidence lines yield different conclusion, several of "
                + "them being equally likely to be true.", 
                false, null, EvidenceConcordance.STRONGLY_CONFLICTING, 
                EvidenceTypeConcordance.DIFFERENT_TYPE));
        expectedTOs.add(new CIOStatementTO("CIO:0000013", 
                "confidence statement from congruent evidence lines of multiple types, "
                + "overall confidence medium", 
                "A confidence statement from congruent evidence lines of multiple types, "
                + "of an overall moderately trusted confidence level.", 
                true, ConfidenceLevel.MEDIUM_CONFIDENCE, EvidenceConcordance.CONGRUENT, 
                EvidenceTypeConcordance.DIFFERENT_TYPE));
        expectedTOs.add(new CIOStatementTO("CIO:0000018", 
                "confidence statement from congruent evidence lines of same type, "
                + "overall confidence low", 
                "A confidence statement from congruent evidence lines of same type, "
                + "of an overall not-trusted confidence level.", 
                true, ConfidenceLevel.LOW_CONFIDENCE, EvidenceConcordance.CONGRUENT, 
                EvidenceTypeConcordance.SAME_TYPE));
        expectedTOs.add(new CIOStatementTO("CIO:0000020", 
                "confidence statement from strongly conflicting evidence lines of same type", 
                "Confidence statement from conflicting evidence lines of same type, "
                + "where evidence lines are in strong contradiction, meaning that "
                + "the supporting evidence lines yield different conclusion, several "
                + "of them being equally likely to be true.", 
                false, null, EvidenceConcordance.STRONGLY_CONFLICTING, 
                EvidenceTypeConcordance.SAME_TYPE));
        expectedTOs.add(new CIOStatementTO("CIO:0000023", 
                "confidence statement from weakly conflicting evidence lines of same type, "
                + "overall confidence low", 
                "Confidence statement from weakly conflicting evidence lines of same type, "
                + "of an overall not-trusted confidence level.", 
                false, ConfidenceLevel.LOW_CONFIDENCE, EvidenceConcordance.WEAKLY_CONFLICTING, 
                EvidenceTypeConcordance.SAME_TYPE));
        expectedTOs.add(new CIOStatementTO("CIO:0000025", 
                "confidence statement from weakly conflicting evidence lines of multiple types, "
                + "overall confidence high", 
                "Confidence statement from weakly conflicting evidence lines of multiple types, "
                + "of an overall high confidence level.", 
                true, ConfidenceLevel.HIGH_CONFIDENCE, EvidenceConcordance.WEAKLY_CONFLICTING, 
                EvidenceTypeConcordance.DIFFERENT_TYPE));

        insert.insert(OntologyUtils.loadOntology(
                this.getClass().getResource("/ontologies/test_insert_cio.obo").getFile()));
        
        ArgumentCaptor<Set> cioTermTOsArg = ArgumentCaptor.forClass(Set.class);
        verify(mockManager.mockCIOStatementDAO).insertCIOStatements(cioTermTOsArg.capture());
        if (!TOComparator.areTOCollectionsEqual(expectedTOs, cioTermTOsArg.getValue())) {
            throw new AssertionError("Incorrect CIOStatementTOs generated, " +
                    "expected " + expectedTOs + ", but was " + cioTermTOsArg.getValue());
        }
    }
}
