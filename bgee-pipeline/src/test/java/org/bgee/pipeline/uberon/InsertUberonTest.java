package org.bgee.pipeline.uberon;

import static org.junit.Assert.*;
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
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
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

        log.info("direct outgoing edges for ID:17: {}", 
                wrapper.getOutgoingEdges(
                        wrapper.getOWLClassByIdentifier("ID:17")));
        log.info("direct outgoing edges with GCI for ID:17: {}", 
                wrapper.getOutgoingEdgesWithGCI(
                        wrapper.getOWLClassByIdentifier("ID:17")));
        log.info("outgoing edge named closure with GCI for ID:17: {}", 
                wrapper.getOutgoingEdgesNamedClosureOverSupPropsWithGCI(
                        wrapper.getOWLClassByIdentifier("ID:17")));
        log.info("outgoing edge named closure for ID:17: {}", 
                wrapper.getOutgoingEdgesNamedClosureOverSupProps(
                        wrapper.getOWLClassByIdentifier("ID:17")));
        log.info("outgoing edge closure with GCI for ID:17: {}", 
                wrapper.getOutgoingEdgesClosureWithGCI(
                        wrapper.getOWLClassByIdentifier("ID:17")));
        
        //instantiate an Uberon with custom taxon constraints and mock manager
        Map<String, Set<Integer>> taxonConstraints = new HashMap<String, Set<Integer>>();
        taxonConstraints.put("ID:1", new HashSet<Integer>(Arrays.asList(7955, 9606, 10090)));
        taxonConstraints.put("ID:2", new HashSet<Integer>(Arrays.asList(7955)));
        taxonConstraints.put("ID:3", new HashSet<Integer>(Arrays.asList(7955)));
        //obsolete class, should not be considered
        taxonConstraints.put("ID:4", new HashSet<Integer>(Arrays.asList(7955, 9606, 10090)));
        //ID:5 is a taxon equivalent to ID:1, should not be seen
        taxonConstraints.put("ID:5", new HashSet<Integer>(Arrays.asList(7955, 9606, 10090)));
        
        taxonConstraints.put("ID:6", new HashSet<Integer>(Arrays.asList(9606, 10090)));
        taxonConstraints.put("ID:7", new HashSet<Integer>(Arrays.asList(9606, 10090)));

        taxonConstraints.put("ID:8", new HashSet<Integer>(Arrays.asList(7955, 9606)));
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

        Set<RelationTO> expectedRelTOs = new HashSet<RelationTO>();
        //we do not set relation IDs, as the iteration order is not predictable. 
        //comparison to expected relations will be done without taking IDs into account.
        
        //first, all reflexive relations
        expectedRelTOs.add(new RelationTO(null, "ID:1", "ID:1", 
                RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.REFLEXIVE));
        expectedRelTOs.add(new RelationTO(null, "ID:6", "ID:6", 
                RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.REFLEXIVE));
        expectedRelTOs.add(new RelationTO(null, "ID:7", "ID:7", 
                RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.REFLEXIVE));
        expectedRelTOs.add(new RelationTO(null, "ID:8", "ID:8", 
                RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.REFLEXIVE));
        expectedRelTOs.add(new RelationTO(null, "ID:9", "ID:9", 
                RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.REFLEXIVE));
        expectedRelTOs.add(new RelationTO(null, "ID:10", "ID:10", 
                RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.REFLEXIVE));
        expectedRelTOs.add(new RelationTO(null, "ID:11", "ID:11", 
                RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.REFLEXIVE));
        expectedRelTOs.add(new RelationTO(null, "ID:12", "ID:12", 
                RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.REFLEXIVE));
        expectedRelTOs.add(new RelationTO(null, "ID:13", "ID:13", 
                RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.REFLEXIVE));
        expectedRelTOs.add(new RelationTO(null, "ID:14", "ID:14", 
                RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.REFLEXIVE));
        expectedRelTOs.add(new RelationTO(null, "ID:15", "ID:15", 
                RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.REFLEXIVE));
        expectedRelTOs.add(new RelationTO(null, "ID:16", "ID:16", 
                RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.REFLEXIVE));
        expectedRelTOs.add(new RelationTO(null, "ID:17", "ID:17", 
                RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.REFLEXIVE));
        //now, direct relations
        expectedRelTOs.add(new RelationTO(null, "ID:6", "ID:1", 
                RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT));
        expectedRelTOs.add(new RelationTO(null, "ID:7", "ID:1", 
                RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT));
        expectedRelTOs.add(new RelationTO(null, "ID:8", "ID:7", 
                RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT));
        expectedRelTOs.add(new RelationTO(null, "ID:9", "ID:8", 
                RelationTO.RelationType.DEVELOPSFROM, RelationTO.RelationStatus.DIRECT));
        expectedRelTOs.add(new RelationTO(null, "ID:9", "ID:8", 
                RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT));
        expectedRelTOs.add(new RelationTO(null, "ID:10", "ID:1", 
                RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT));
        expectedRelTOs.add(new RelationTO(null, "ID:11", "ID:10", 
                RelationTO.RelationType.DEVELOPSFROM, RelationTO.RelationStatus.DIRECT));
        expectedRelTOs.add(new RelationTO(null, "ID:12", "ID:11", 
                RelationTO.RelationType.TRANSFORMATIONOF, RelationTO.RelationStatus.DIRECT));
        expectedRelTOs.add(new RelationTO(null, "ID:13", "ID:10", 
                RelationTO.RelationType.DEVELOPSFROM, RelationTO.RelationStatus.DIRECT));
        expectedRelTOs.add(new RelationTO(null, "ID:14", "ID:13", 
                RelationTO.RelationType.TRANSFORMATIONOF, RelationTO.RelationStatus.DIRECT));
        expectedRelTOs.add(new RelationTO(null, "ID:15", "ID:10", 
                RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT));
        expectedRelTOs.add(new RelationTO(null, "ID:16", "ID:10", 
                RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT));
        expectedRelTOs.add(new RelationTO(null, "ID:17", "ID:16", 
                RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT));
        //now, indirect relations
        //ID:8 po ID:7 po ID:1
        expectedRelTOs.add(new RelationTO(null, "ID:8", "ID:1", 
                RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.INDIRECT));
        //ID:9 po ID:8 po ID:7 po ID:1
        expectedRelTOs.add(new RelationTO(null, "ID:9", "ID:7", 
                RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.INDIRECT));
        expectedRelTOs.add(new RelationTO(null, "ID:9", "ID:1", 
                RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.INDIRECT));
        //ID:9 dvlt_from ID:8 po ID:7 po ID:1
        expectedRelTOs.add(new RelationTO(null, "ID:9", "ID:7", 
                RelationTO.RelationType.DEVELOPSFROM, RelationTO.RelationStatus.INDIRECT));
        expectedRelTOs.add(new RelationTO(null, "ID:9", "ID:1", 
                RelationTO.RelationType.DEVELOPSFROM, RelationTO.RelationStatus.INDIRECT));
        //ID:11 dvlt_from ID:10 is_a ID:1, should be discarded 
        //(no propagation of develops_from/transformation_of through is_a)
//        expectedRelTOs.add(new RelationTO(null, "ID:11", "ID:1", 
//                RelationTO.RelationType.DEVELOPSFROM, RelationTO.RelationStatus.INDIRECT));
        //ID:12 transf_of ID:11 dvlt_from ID:10 is_a ID:1
        expectedRelTOs.add(new RelationTO(null, "ID:12", "ID:10", 
                RelationTO.RelationType.DEVELOPSFROM, RelationTO.RelationStatus.INDIRECT));
        //should be discarded 
        //(no propagation of develops_from/transformation_of through is_a)
//        expectedRelTOs.add(new RelationTO(null, "ID:12", "ID:1", 
//                RelationTO.RelationType.DEVELOPSFROM, RelationTO.RelationStatus.INDIRECT));
        //ID:13 dvlt_from ID:10 is_a ID:1, should be discarded 
        //(no propagation of develops_from/transformation_of through is_a)
//        expectedRelTOs.add(new RelationTO(null, "ID:13", "ID:1", 
//                RelationTO.RelationType.DEVELOPSFROM, RelationTO.RelationStatus.INDIRECT));
        //ID:14 transf_of ID:13 dvlt_from ID:10 is_a ID:1
        expectedRelTOs.add(new RelationTO(null, "ID:14", "ID:10", 
                RelationTO.RelationType.DEVELOPSFROM, RelationTO.RelationStatus.INDIRECT));
        //should be discarded 
        //(no propagation of develops_from/transformation_of through is_a)
//        expectedRelTOs.add(new RelationTO(null, "ID:14", "ID:1", 
//                RelationTO.RelationType.DEVELOPSFROM, RelationTO.RelationStatus.INDIRECT));
        //ID:15 po ID:10 is_a ID:1
        expectedRelTOs.add(new RelationTO(null, "ID:15", "ID:1", 
                RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.INDIRECT));
        //ID:16 po ID:10 is_a ID:1
        expectedRelTOs.add(new RelationTO(null, "ID:16", "ID:1", 
                RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.INDIRECT));
        //ID:17 po ID:16 po ID:10 is_a ID:1
        expectedRelTOs.add(new RelationTO(null, "ID:17", "ID:10", 
                RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.INDIRECT));
        expectedRelTOs.add(new RelationTO(null, "ID:17", "ID:1", 
                RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.INDIRECT));
        
        ArgumentCaptor<Set> relTOsArg = ArgumentCaptor.forClass(Set.class);
        verify(mockManager.mockRelationDAO).insertAnatEntityRelations(relTOsArg.capture());
        
        if (!TOComparator.areTOCollectionsEqual(
                expectedRelTOs, relTOsArg.getValue(), false)) {
            Set<RelationTO> diffRelTos = new HashSet<RelationTO>();
            for (RelationTO relTO: (Set<RelationTO>) relTOsArg.getValue()) {
                diffRelTos.add(new RelationTO(null, relTO.getSourceId(), relTO.getTargetId(), 
                        relTO.getRelationType(), relTO.getRelationStatus()));
            }
            Set<RelationTO> unexpectedRelTOs = new HashSet<RelationTO>(diffRelTos);
            unexpectedRelTOs.removeAll(expectedRelTOs);
            expectedRelTOs.removeAll(diffRelTos);
            throw new AssertionError("Incorrect RelationTOs generated for relations " +
            		"between anatomical entities, unexpected RelationTOs: " + unexpectedRelTOs + 
            		" - missing RelationTOs: " + expectedRelTOs + " - All generated " +
            		"RelationTOs: " + relTOsArg.getValue());
        }

        ArgumentCaptor<Set> relTaxonConstraintTOsArg = ArgumentCaptor.forClass(Set.class);
        verify(mockManager.mockTaxonConstraintDAO).insertAnatEntityRelationTaxonConstraints(
                relTaxonConstraintTOsArg.capture());
        int allSpeciesReflexiveTaxonConstraints = 0;
        int restrainedReflexiveTaxonConstraints = 0;
        int allSpeciesOtherTaxonConstraints = 0;
        int restrainedOtherTaxonConstraints = 0;
        for (RelationTO insertedRelTO: (Set<RelationTO>) relTOsArg.getValue()) {
            //several taxon constraints can be generated for a same relation, 
            //this is why we use a Set here
            Set<TaxonConstraintTO> expectedRelTaxonConstraintTOs = new HashSet<TaxonConstraintTO>();
            
            if (insertedRelTO.getRelationStatus() == RelationTO.RelationStatus.REFLEXIVE) {
                if (insertedRelTO.getSourceId().equals("ID:8")) {
                    expectedRelTaxonConstraintTOs.add(
                            new TaxonConstraintTO(insertedRelTO.getId(), "9606"));
                    restrainedReflexiveTaxonConstraints++;
                } else {
                    allSpeciesReflexiveTaxonConstraints++;
                    expectedRelTaxonConstraintTOs.add(
                            new TaxonConstraintTO(insertedRelTO.getId(), null));
                }
            } else if (TOComparator.areTOsEqual(insertedRelTO, new RelationTO(null, "ID:8", "ID:7", 
                        RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT), false) || 
                    TOComparator.areTOsEqual(insertedRelTO, 
                        new RelationTO(null, "ID:9", "ID:8", 
                        RelationTO.RelationType.DEVELOPSFROM, RelationTO.RelationStatus.DIRECT), 
                        false) || 
                    TOComparator.areTOsEqual(insertedRelTO, 
                        new RelationTO(null, "ID:9", "ID:8", 
                        RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT), 
                        false) || 
                    TOComparator.areTOsEqual(insertedRelTO, 
                        new RelationTO(null, "ID:8", "ID:1", 
                        RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.INDIRECT), 
                        false) || 
                    TOComparator.areTOsEqual(insertedRelTO, 
                        new RelationTO(null, "ID:9", "ID:7", 
                        RelationTO.RelationType.DEVELOPSFROM, RelationTO.RelationStatus.INDIRECT), 
                        false) || 
                    TOComparator.areTOsEqual(insertedRelTO, 
                        new RelationTO(null, "ID:9", "ID:1", 
                        RelationTO.RelationType.DEVELOPSFROM, RelationTO.RelationStatus.INDIRECT), 
                        false) || 
                    TOComparator.areTOsEqual(insertedRelTO, 
                        new RelationTO(null, "ID:9", "ID:7", 
                        RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.INDIRECT), 
                        false) || 
                    TOComparator.areTOsEqual(insertedRelTO, 
                        new RelationTO(null, "ID:9", "ID:1", 
                        RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.INDIRECT), 
                        false) || 
                    TOComparator.areTOsEqual(insertedRelTO, 
                        new RelationTO(null, "ID:14", "ID:13", 
                        RelationTO.RelationType.TRANSFORMATIONOF, RelationTO.RelationStatus.DIRECT), 
                        false) || 
                    TOComparator.areTOsEqual(insertedRelTO, 
                        new RelationTO(null, "ID:14", "ID:10", 
                        RelationTO.RelationType.DEVELOPSFROM, RelationTO.RelationStatus.INDIRECT), 
                        false)) {

                restrainedOtherTaxonConstraints++;
                expectedRelTaxonConstraintTOs.add(
                        new TaxonConstraintTO(insertedRelTO.getId(), "9606"));
            } else {
                allSpeciesOtherTaxonConstraints++;
                expectedRelTaxonConstraintTOs.add(
                        new TaxonConstraintTO(insertedRelTO.getId(), null));
            }
            
            assertTrue("Missing relation taxon constraints: " + expectedRelTaxonConstraintTOs + 
                    " for RelationTO: " + insertedRelTO + " - all taxon constraints: " + 
                    relTaxonConstraintTOsArg.getValue(), 
                    relTaxonConstraintTOsArg.getValue().containsAll(
                            expectedRelTaxonConstraintTOs));
            
        }
        assertEquals("Incorrect relation taxon constraints generated: " + 
                relTaxonConstraintTOsArg, 37, relTaxonConstraintTOsArg.getValue().size());
        assertEquals("Incorrect relation taxon constraints generated: " + 
                relTaxonConstraintTOsArg, 12, allSpeciesReflexiveTaxonConstraints);
        assertEquals("Incorrect relation taxon constraints generated: " + 
                relTaxonConstraintTOsArg, 1, restrainedReflexiveTaxonConstraints);
        assertEquals("Incorrect relation taxon constraints generated: " + 
                relTaxonConstraintTOsArg, 14, allSpeciesOtherTaxonConstraints);
        assertEquals("Incorrect relation taxon constraints generated: " + 
                relTaxonConstraintTOsArg, 10, restrainedOtherTaxonConstraints);
    }
}
