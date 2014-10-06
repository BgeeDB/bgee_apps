package org.bgee.pipeline.uberon;

import static org.mockito.Mockito.verify;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTO;
import org.bgee.model.dao.api.anatdev.StageDAO.StageTO;
import org.bgee.model.dao.api.anatdev.TaxonConstraintDAO.TaxonConstraintTO;
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
     * Test {@link InsertUberon#insertStageOntologyIntoDataSource(UberonDevStage, Collection)}.
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

        OWLOntology ont = OntologyUtils.loadOntology(InsertUberonTest.class.
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
        //Taxonomy is part of a subgraph to ignore, should not be considered
        taxonConstraints.put("NCBITaxon:1", new HashSet<Integer>(Arrays.asList(1, 2, 3)));
        taxonConstraints.put("NCBITaxon:9606", new HashSet<Integer>(Arrays.asList(1, 2, 3)));
        //obsolete class, should not be considered
        taxonConstraints.put("ID:7", new HashSet<Integer>(Arrays.asList(1, 2, 3)));
        //ID:8 is a taxon equivalent to ID:4, should not be seen
        taxonConstraints.put("ID:8", new HashSet<Integer>(Arrays.asList(1, 2, 3)));
        
        UberonDevStage uberon = new UberonDevStage(utils, taxonConstraints);
        uberon.setToIgnoreSubgraphRootIds(Arrays.asList("NCBITaxon:1"));
        
        InsertUberon insert = new InsertUberon(mockManager);
        insert.insertStageOntologyIntoDataSource(uberon, Arrays.asList(1, 2, 3));
        
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

        Set<TaxonConstraintTO> expectedTaxonConstraintTOs = new HashSet<TaxonConstraintTO>();
        expectedTaxonConstraintTOs.add(new TaxonConstraintTO("UBERON:1", null));
        expectedTaxonConstraintTOs.add(new TaxonConstraintTO("UBERON:2", null));
        expectedTaxonConstraintTOs.add(new TaxonConstraintTO("UBERON:3", null));
        expectedTaxonConstraintTOs.add(new TaxonConstraintTO("ID:4", "1"));
        expectedTaxonConstraintTOs.add(new TaxonConstraintTO("ID:4", "3"));
        expectedTaxonConstraintTOs.add(new TaxonConstraintTO("ID:5", "2"));
        expectedTaxonConstraintTOs.add(new TaxonConstraintTO("ID:5", "3"));
        ArgumentCaptor<Set> taxonConstraintTOsArg = ArgumentCaptor.forClass(Set.class);
        verify(mockManager.mockTaxonConstraintDAO).insertStageTaxonConstraints(
                taxonConstraintTOsArg.capture());
        if (!TOComparator.areTOCollectionsEqual(
                expectedTaxonConstraintTOs, taxonConstraintTOsArg.getValue())) {
            throw new AssertionError("Incorrect TaxonConstraintTOs generated to insert stages, " +
                    "expected " + expectedTaxonConstraintTOs.toString() + ", but was " + 
                    taxonConstraintTOsArg.getValue());
        }
    }
    
    /**
     * Test {@link InsertUberon#insertStageOntologyIntoDataSource(Uberon, Collection)}.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void insertAnatOntologyIntoDataSource() throws OBOFormatParserException, 
    OWLOntologyCreationException, IOException {
      //first, we need a mock MySQLDAOManager, for the class to acquire mock 
        //DAOs. This will allow to verify 
        //that the correct values were tried to be inserted into the database.
        MockDAOManager mockManager = new MockDAOManager();

        OWLOntology ont = OntologyUtils.loadOntology(InsertUberonTest.class.
                getResource("/ontologies/insertAnatOntTest.obo").getFile());
        OWLGraphWrapper wrapper = new OWLGraphWrapper(ont);
        OntologyUtils utils = new OntologyUtils(wrapper);
        
        //instantiate an Uberon with custom taxon constraints and mock manager
        Map<String, Set<Integer>> taxonConstraints = new HashMap<String, Set<Integer>>();
        taxonConstraints.put("ID:1", new HashSet<Integer>(Arrays.asList(7955, 9606, 10090)));
        taxonConstraints.put("ID:2", new HashSet<Integer>(Arrays.asList(7955)));
        taxonConstraints.put("ID:3", new HashSet<Integer>(Arrays.asList(7955)));
        //obsolete class, should not be considered
        taxonConstraints.put("ID:4", new HashSet<Integer>(Arrays.asList(7955, 9606, 10090)));
        //ID:5 is a taxon equivalent to ID:1, should not be seen
        taxonConstraints.put("ID:5", new HashSet<Integer>(Arrays.asList(7955, 9606, 10090)));
        
        taxonConstraints.put("ID:6", new HashSet<Integer>(Arrays.asList(7955, 9606, 10090)));
        taxonConstraints.put("ID:7", new HashSet<Integer>(Arrays.asList(7955, 9606, 10090)));

        taxonConstraints.put("ID:8", new HashSet<Integer>(Arrays.asList(9606)));
        taxonConstraints.put("ID:9", new HashSet<Integer>(Arrays.asList(9606, 10090)));

        taxonConstraints.put("ID:10", new HashSet<Integer>(Arrays.asList(7955, 9606, 10090)));
        taxonConstraints.put("ID:11", new HashSet<Integer>(Arrays.asList(7955, 9606, 10090)));
        taxonConstraints.put("ID:12", new HashSet<Integer>(Arrays.asList(7955, 9606, 10090)));
        taxonConstraints.put("ID:13", new HashSet<Integer>(Arrays.asList(7955, 9606, 10090)));
        taxonConstraints.put("ID:14", new HashSet<Integer>(Arrays.asList(7955, 9606, 10090)));
        taxonConstraints.put("ID:15", new HashSet<Integer>(Arrays.asList(7955, 9606, 10090)));
        taxonConstraints.put("ID:16", new HashSet<Integer>(Arrays.asList(7955, 9606, 10090)));
        taxonConstraints.put("ID:17", new HashSet<Integer>(Arrays.asList(7955, 9606, 10090)));
        
        Uberon uberon = new Uberon(utils, taxonConstraints);
        uberon.setToIgnoreSubgraphRootIds(Arrays.asList("NCBITaxon:1"));
        
        InsertUberon insert = new InsertUberon(mockManager);
        insert.insertAnatOntologyIntoDataSource(uberon, Arrays.asList(9606, 10090));
        
        //generate the expected Sets of AnatEntityTO, RelationTO, TaxonConstraintTO 
        //to verify the calls made to the DAOs
        Set<AnatEntityTO> expectedAnatEntityTOs = new HashSet<AnatEntityTO>();
        expectedAnatEntityTOs.add(new AnatEntityTO("ID:1", "name cls 1", "Def. cls 1", 
                "UBERON:0000104", "UBERON:0000104", true));
        expectedAnatEntityTOs.add(new AnatEntityTO("ID:6", "name cls 6", "Def. cls 6", 
                "UBERON:0000104", "UBERON:0000104", false));
        expectedAnatEntityTOs.add(new AnatEntityTO("ID:7", "name cls 7", "Def. cls 7", 
                "UBERON:0000104", "UBERON:0000104", false));
        expectedAnatEntityTOs.add(new AnatEntityTO("ID:8", "name cls 8", "Def. cls 8", 
                "UBERON:0000104", "UBERON:0000104", true));
        expectedAnatEntityTOs.add(new AnatEntityTO("ID:9", "name cls 9", "Def. cls 9", 
                "UBERON:0000104", "UBERON:0000104", false));
        expectedAnatEntityTOs.add(new AnatEntityTO("ID:10", "name cls 10", null, 
                "UBERON:0000104", "UBERON:0000104", true));
        expectedAnatEntityTOs.add(new AnatEntityTO("ID:11", "name cls 11", "Def. cls 11", 
                "UBERON:0000104", "UBERON:0000104", false));
        expectedAnatEntityTOs.add(new AnatEntityTO("ID:12", "name cls 12", "Def. cls 12", 
                "UBERON:0000104", "UBERON:0000104", false));
        expectedAnatEntityTOs.add(new AnatEntityTO("ID:13", "name cls 13", "Def. cls 13", 
                "UBERON:0000104", "UBERON:0000104", false));
        expectedAnatEntityTOs.add(new AnatEntityTO("ID:14", "name cls 14", "Def. cls 14", 
                "UBERON:0000104", "UBERON:0000104", false));
        expectedAnatEntityTOs.add(new AnatEntityTO("ID:15", "name cls 15", "Def. cls 15", 
                "UBERON:0000104", "UBERON:0000104", false));
        expectedAnatEntityTOs.add(new AnatEntityTO("ID:16", "name cls 16", "Def. cls 16", 
                "UBERON:0000104", "UBERON:0000104", false));
        expectedAnatEntityTOs.add(new AnatEntityTO("ID:17", "name cls 17", "Def. cls 17", 
                "UBERON:0000104", "UBERON:0000104", false));
        ArgumentCaptor<Set> anatEntityTOsArg = ArgumentCaptor.forClass(Set.class);
        verify(mockManager.mockAnatEntityDAO).insertAnatEntities(anatEntityTOsArg.capture());
        if (!TOComparator.areTOCollectionsEqual(
                expectedAnatEntityTOs, anatEntityTOsArg.getValue())) {
            throw new AssertionError("Incorrect anatEntityTOs generated to insert anatomy, " +
                    "expected " + expectedAnatEntityTOs.toString() + ", but was " + 
                    anatEntityTOsArg.getValue());
        }

        Set<TaxonConstraintTO> expectedTaxonConstraintTOs = new HashSet<TaxonConstraintTO>();
        expectedTaxonConstraintTOs.add(new TaxonConstraintTO("ID:1", null));
        expectedTaxonConstraintTOs.add(new TaxonConstraintTO("ID:6", null));
        expectedTaxonConstraintTOs.add(new TaxonConstraintTO("ID:7", null));
        expectedTaxonConstraintTOs.add(new TaxonConstraintTO("ID:9", null));
        expectedTaxonConstraintTOs.add(new TaxonConstraintTO("ID:10", null));
        expectedTaxonConstraintTOs.add(new TaxonConstraintTO("ID:11", null));
        expectedTaxonConstraintTOs.add(new TaxonConstraintTO("ID:12", null));
        expectedTaxonConstraintTOs.add(new TaxonConstraintTO("ID:13", null));
        expectedTaxonConstraintTOs.add(new TaxonConstraintTO("ID:14", null));
        expectedTaxonConstraintTOs.add(new TaxonConstraintTO("ID:15", null));
        expectedTaxonConstraintTOs.add(new TaxonConstraintTO("ID:16", null));
        expectedTaxonConstraintTOs.add(new TaxonConstraintTO("ID:17", null));

        expectedTaxonConstraintTOs.add(new TaxonConstraintTO("ID:8", "9606"));
        
        ArgumentCaptor<Set> taxonConstraintTOsArg = ArgumentCaptor.forClass(Set.class);
        verify(mockManager.mockTaxonConstraintDAO).insertAnatEntityTaxonConstraints(
                taxonConstraintTOsArg.capture());
        if (!TOComparator.areTOCollectionsEqual(
                expectedTaxonConstraintTOs, taxonConstraintTOsArg.getValue())) {
            throw new AssertionError("Incorrect TaxonConstraintTOs generated for anatomical entities, " +
                    "expected " + expectedTaxonConstraintTOs.toString() + ", but was " + 
                    taxonConstraintTOsArg.getValue());
        }
    }
}
