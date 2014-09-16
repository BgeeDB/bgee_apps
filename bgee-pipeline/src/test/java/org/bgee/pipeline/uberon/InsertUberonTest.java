package org.bgee.pipeline.uberon;

import static org.mockito.Mockito.verify;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.anatdev.StageDAO.StageTO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.pipeline.TestAncestor;
import org.bgee.pipeline.ontologycommon.OntologyUtils;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import owltools.graph.OWLGraphWrapper;

/**
 * Unit tests for {@link InsertUberon}
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class InsertUberonTest extends TestAncestor {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(InsertUberonTest.class.getName());
    
    /**
     * Default Constructor. 
     */
    public InsertUberonTest() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    

    
    /**
     * Test {@link UberonDevStage#insertStageOntologyIntoDataSource(Collection)}.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void shouldInsertStages() throws FileNotFoundException, 
        OWLOntologyCreationException, OBOFormatParserException, IllegalArgumentException, 
        DAOException, IOException {
        //first, we need a mock MySQLDAOManager, for the class to acquire mock 
        //MySQLStageDAO. This will allow to verify 
        //that the correct values were tried to be inserted into the database.
        MockDAOManager mockManager = new MockDAOManager();

        OWLOntology ont = OntologyUtils.loadOntology(UberonDevStageTest.class.
                getResource("/ontologies/test_dev_stage_ont.obo").getFile());
        OWLGraphWrapper wrapper = new OWLGraphWrapper(ont);
        OntologyUtils utils = new OntologyUtils(wrapper);
        
        //instantiate an UberonDevStage with custom taxon constraints and mock manager
        Map<String, Set<Integer>> taxonConstraints = new HashMap<String, Set<Integer>>();
        taxonConstraints.put("UBERON:1", new HashSet<Integer>(Arrays.asList(1, 2, 3)));
        taxonConstraints.put("UBERON:2", new HashSet<Integer>(Arrays.asList(1, 2, 3)));
        taxonConstraints.put("UBERON:3", new HashSet<Integer>(Arrays.asList(1, 2, 3)));
        taxonConstraints.put("ID:4", new HashSet<Integer>(Arrays.asList(1, 3)));
        taxonConstraints.put("ID:5", new HashSet<Integer>(Arrays.asList(2, 3)));
        taxonConstraints.put("ID:6", new HashSet<Integer>(4, 5));
        UberonDevStage uberon = new UberonDevStage(utils, taxonConstraints);
        
        InsertUberon insert = new InsertUberon(mockManager);
        insert.insertStageOntologyIntoDataSource(uberon, Arrays.asList(1, 2));
        
        //generate the expected Sets of SpeciesTOs and taxonTOs to verify the calls 
        //made to the DAOs
        Set<StageTO> expectedStageTOs = new HashSet<StageTO>();
        expectedStageTOs.add(new StageTO("UBERON:1", "stage 1", "def stage 1", 
                        1, 10, 1, false, true));
        expectedStageTOs.add(new StageTO("UBERON:2", "stage 2", "def stage 2", 
                2, 7, 2, false, true));
        expectedStageTOs.add(new StageTO("UBERON:3", "stage 3", null, 
                8, 9, 2, false, true));
        expectedStageTOs.add(new StageTO("ID:4", "stage 4", "def stage 4", 
                3, 4, 3, false, false));
        expectedStageTOs.add(new StageTO("ID:5", "stage 5", "def stage 5", 
                5, 6, 3, true, false));
        ArgumentCaptor<Set> stageTOsArg = ArgumentCaptor.forClass(Set.class);
        verify(mockManager.mockStageDAO).insertStages(stageTOsArg.capture());
        if (!TOComparator.areTOCollectionsEqual(
                expectedStageTOs, stageTOsArg.getValue())) {
            throw new AssertionError("Incorrect StageTOs generated to insert stages, " +
                    "expected " + expectedStageTOs.toString() + ", but was " + 
                    stageTOsArg.getValue());
        }
    }
}
