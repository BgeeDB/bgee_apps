package org.bgee.pipeline.annotations;

import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.anatdev.mapping.RawSimilarityAnnotationDAO.RawSimilarityAnnotationTO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SimAnnotToAnatEntityTO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SummarySimilarityAnnotationTO;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTO;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTO.ConfidenceLevel;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTO.EvidenceConcordance;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTO.EvidenceTypeConcordance;
import org.bgee.pipeline.TestAncestor;
import org.bgee.pipeline.ontologycommon.OntologyUtils;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * Tests for {@link InsertSimilarityAnnotation}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Apr. 2015
 * @since Bgee 13
 */
public class InsertSimilarityAnnotationTest extends TestAncestor {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(InsertSimilarityAnnotationTest.class.getName());
    
    /**
     * Default Constructor. 
     */
    public InsertSimilarityAnnotationTest() {
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
        InsertSimilarityAnnotation insert = new InsertSimilarityAnnotation(mockManager);
        
        Set<SummarySimilarityAnnotationTO> expectedSummaryTOs = 
                new HashSet<SummarySimilarityAnnotationTO>();
        Set<SimAnnotToAnatEntityTO> expectedSimAnnotToAnatEntityTOs = 
                new HashSet<SimAnnotToAnatEntityTO>();
        Set<RawSimilarityAnnotationTO> expectedRawTOs = 
                new HashSet<RawSimilarityAnnotationTO>();
        
    }
    
}
