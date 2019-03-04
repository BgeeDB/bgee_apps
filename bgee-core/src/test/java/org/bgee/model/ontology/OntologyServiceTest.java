package org.bgee.model.ontology;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.ServiceFactory;
import org.bgee.model.TestAncestor;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.DevStageService;
import org.bgee.model.anatdev.TaxonConstraint;
import org.bgee.model.anatdev.TaxonConstraintService;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.anatdev.TaxonConstraintDAO;
import org.bgee.model.dao.api.anatdev.TaxonConstraintDAO.TaxonConstraintTO;
import org.bgee.model.dao.api.anatdev.TaxonConstraintDAO.TaxonConstraintTOResultSet;
import org.bgee.model.dao.api.ontologycommon.RelationDAO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationStatus;
import org.bgee.model.expressiondata.CallServiceTest;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTOResultSet;
import org.bgee.model.species.Taxon;
import org.bgee.model.species.TaxonService;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * This class holds the unit tests for the {@code OntologyService} class.
 * 
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 14 Mar. 2019
 * @since   Bgee 13, Dec. 2015
 */
public class OntologyServiceTest extends TestAncestor {
    private final static Logger log = LogManager.getLogger(CallServiceTest.class.getName());
    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Test the method{@link OntologyService#getAnatEntityOntology(
     * Collection, Collection, boolean, boolean, AnatEntityService)}.
     */
    @Test
    public void shouldGetAnatEntityOntology() {
        DAOManager managerMock = mock(DAOManager.class);
        RelationDAO relationDao = mock(RelationDAO.class);
        when(managerMock.getRelationDAO()).thenReturn(relationDao);
        
        Set<Integer> speciesIds = new HashSet<>(Arrays.asList(11, 22, 44));
        Set<String> anatEntityIds = new HashSet<String>(Arrays.asList("UBERON:0002", "UBERON:0002p"));

        Set<String> sourceAnatEntityIds = new HashSet<String>(anatEntityIds);
        Set<String> targetAnatEntityIds = new HashSet<String>(anatEntityIds);
                
        Set<RelationTO.RelationType> daoRelationTypes1 = new HashSet<>(
                Arrays.asList(RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationType.DEVELOPSFROM));

        Set<RelationTO.RelationType> daoRelationTypes23 = new HashSet<>(
                Arrays.asList(RelationTO.RelationType.ISA_PARTOF));

        Set<RelationStatus> relationStatus = EnumSet.complementOf(EnumSet.of(RelationStatus.REFLEXIVE));
        // UBERON:0001 ------------------
        // | is_a       \ dev_from      |
        // UBERON:0002   UBERON:0002p   | is_a (indirect)
        // | is_a       / is_a          |
        // UBERON:0003 ------------------
        
        List<RelationTO<String>> allRelations = Arrays.asList(
                new OntologyService.WrapperRelationTO<>(new RelationTO<>(1, "UBERON:0002", "UBERON:0001", RelationTO.RelationType.ISA_PARTOF, RelationStatus.DIRECT)),
                new OntologyService.WrapperRelationTO<>(new RelationTO<>(2, "UBERON:0002p", "UBERON:0001", RelationTO.RelationType.DEVELOPSFROM, RelationStatus.DIRECT)),
                new OntologyService.WrapperRelationTO<>(new RelationTO<>(3, "UBERON:0003", "UBERON:0002", RelationTO.RelationType.ISA_PARTOF, RelationStatus.DIRECT)),
                new OntologyService.WrapperRelationTO<>(new RelationTO<>(4, "UBERON:0003", "UBERON:0002p", RelationTO.RelationType.ISA_PARTOF, RelationStatus.DIRECT)),
                new OntologyService.WrapperRelationTO<>(new RelationTO<>(5, "UBERON:0003", "UBERON:0001", RelationTO.RelationType.ISA_PARTOF, RelationStatus.INDIRECT)));

        List<RelationTO<String>> relationTOs1 = Arrays.asList(allRelations.get(0), allRelations.get(1),
                allRelations.get(2), allRelations.get(3));
        RelationTOResultSet<String> mockRelationRs1 = getMockResultSet(RelationTOResultSet.class, relationTOs1);
        when(relationDao.getAnatEntityRelations(
        		speciesIds, true, sourceAnatEntityIds, targetAnatEntityIds, true, 
        		daoRelationTypes1, relationStatus, null))
        	.thenReturn(mockRelationRs1);

        Set<String> newSourceAnatEntityIds = new HashSet<String>(Arrays.asList("UBERON:0001", "UBERON:0003"));
        Set<String> newTargetAnatEntityIds = new HashSet<String>(Arrays.asList("UBERON:0001", "UBERON:0003"));
        RelationTOResultSet<String> mockRelationRs1b = getMockResultSet(RelationTOResultSet.class, allRelations);
        when(relationDao.getAnatEntityRelations(speciesIds, true, newSourceAnatEntityIds,
                newTargetAnatEntityIds, true, daoRelationTypes1, relationStatus, null))
            .thenReturn(mockRelationRs1b);

        List<RelationTO<String>> relationTOs2 = Arrays.asList(allRelations.get(0), allRelations.get(1));
        RelationTOResultSet<String> mockRelationRs2 = getMockResultSet(RelationTOResultSet.class, relationTOs2);
        when(relationDao.getAnatEntityRelations(speciesIds, true, sourceAnatEntityIds, null, true, 
        		daoRelationTypes23, relationStatus, null)).thenReturn(mockRelationRs2);
        
        RelationTOResultSet<String> mockRelationRs2b = getMockResultSet(RelationTOResultSet.class, new ArrayList<>());
        when(relationDao.getAnatEntityRelations(speciesIds, true, new HashSet<String>(Arrays.asList("UBERON:0001")), 
                new HashSet<>(), true, daoRelationTypes23, relationStatus, null))
            .thenReturn(mockRelationRs2b);

        List<RelationTO<String>> relationTOs3 = new ArrayList<>();
        RelationTOResultSet<String> mockRelationRs3 = getMockResultSet(RelationTOResultSet.class, relationTOs3);
        when(relationDao.getAnatEntityRelations(
        		speciesIds, true, sourceAnatEntityIds, targetAnatEntityIds, false, 
        		daoRelationTypes23, relationStatus, null))
        	.thenReturn(mockRelationRs3);

        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
        AnatEntityService anatEntityService = mock(AnatEntityService.class);
        when(serviceFactory.getAnatEntityService()).thenReturn(anatEntityService);
        
        Set<AnatEntity> anatEntities1 = new HashSet<>(Arrays.asList(
        		new AnatEntity("1", "UBERON:0001", "desc1"),
        		new AnatEntity("2", "UBERON:0002", "desc2"),
        		new AnatEntity("3", "UBERON:0002p", "desc2p"),
        		new AnatEntity("4", "UBERON:0003", "desc3")));
        Stream<AnatEntity> anatEntityStream1 = anatEntities1.stream();
        Set<String> expAnatEntityIds1 = new HashSet<String>(
        		Arrays.asList("UBERON:0001", "UBERON:0002", "UBERON:0002p", "UBERON:0003"));
        when(anatEntityService.loadAnatEntities(speciesIds, true, expAnatEntityIds1, true))
        	.thenReturn(anatEntityStream1);

        Set<AnatEntity> anatEntities2 = new HashSet<>(Arrays.asList(
        		new AnatEntity("1", "UBERON:0001", "desc1"),
        		new AnatEntity("2", "UBERON:0002", "desc2"),
        		new AnatEntity("3", "UBERON:0002p", "desc2p")));
        Stream<AnatEntity> anatEntityStream2 = anatEntities2.stream();
        Set<String> expAnatEntityIds2 = new HashSet<String>(
        		Arrays.asList("UBERON:0001", "UBERON:0002", "UBERON:0002p"));
        when(anatEntityService.loadAnatEntities(speciesIds, true, expAnatEntityIds2, true))
        	.thenReturn(anatEntityStream2);

        Set<AnatEntity> anatEntities3 = new HashSet<>(Arrays.asList(
        		new AnatEntity("2", "UBERON:0002", "desc2"),
        		new AnatEntity("3", "UBERON:0002p", "desc2p")));
        Stream<AnatEntity> anatEntityStream3 = anatEntities3.stream();
        Set<String> expAnatEntityIds3 = new HashSet<String>(
        		Arrays.asList("UBERON:0002", "UBERON:0002p"));
        when(anatEntityService.loadAnatEntities(speciesIds, true, expAnatEntityIds3, true))
        	.thenReturn(anatEntityStream3);

        
        TaxonConstraintService tcService = mock(TaxonConstraintService.class);
        when(serviceFactory.getTaxonConstraintService()).thenReturn(tcService);

        Set<TaxonConstraint<String>> taxonConstraints = new HashSet<>(Arrays.asList(
                // UBERON:0001 sp1/sp2/sp3 --------------------
                // |                    \                      |
                // UBERON:0002 sp1/sp2   UBERON:0002p sp2/sp3  | 
                // |                    /                      |
                // UBERON:0003 sp1/sp2 ------------------------
                new TaxonConstraint<>("UBERON:0001", null),
                new TaxonConstraint<>("UBERON:0002", 11),
                new TaxonConstraint<>("UBERON:0002", 22),
                new TaxonConstraint<>("UBERON:0002p", 33),
                new TaxonConstraint<>("UBERON:0002p", 33),
                new TaxonConstraint<>("UBERON:0003", 11),
                new TaxonConstraint<>("UBERON:0003", 22)));
        // Note: we need to use thenReturn() twice because a stream can be use only once 
        when(tcService.loadAnatEntityTaxonConstraintBySpeciesIds(speciesIds))
            .thenReturn(taxonConstraints.stream()).thenReturn(taxonConstraints.stream())
            .thenReturn(taxonConstraints.stream()).thenReturn(taxonConstraints.stream())
            .thenReturn(taxonConstraints.stream()).thenReturn(taxonConstraints.stream());
        
        Set<TaxonConstraint<Integer>> relationTaxonConstraints = new HashSet<>(Arrays.asList(
                // UBERON:0001 ------------------
                // | sp1/sp2   \ sp2            |
                // UBERON:0002   UBERON:0002p   | sp2/sp1 (indirect)
                // | sp1       / sp2            |
                // UBERON:0003 ------------------
                new TaxonConstraint<>(1, 11),
                new TaxonConstraint<>(1, 22),
                new TaxonConstraint<>(2, 22),
                new TaxonConstraint<>(3, 11),
                new TaxonConstraint<>(4, 22),
                new TaxonConstraint<>(5, 11),
                new TaxonConstraint<>(5, 22)));
        // Note: we need to use thenReturn() twice because a stream can be use only once 
        when(tcService.loadAnatEntityRelationTaxonConstraintBySpeciesIds(speciesIds))
            .thenReturn(relationTaxonConstraints.stream()).thenReturn(relationTaxonConstraints.stream())
            .thenReturn(relationTaxonConstraints.stream()).thenReturn(relationTaxonConstraints.stream())
            .thenReturn(relationTaxonConstraints.stream()).thenReturn(relationTaxonConstraints.stream());
        
        TaxonConstraintDAO tcDAO = mock(TaxonConstraintDAO.class);
        when(managerMock.getTaxonConstraintDAO()).thenReturn(tcDAO);
        
        List<TaxonConstraintTO<Integer>> txTOs1 = Arrays.asList(
                new TaxonConstraintTO<Integer>(1, 11),
                new TaxonConstraintTO<Integer>(1, 22),
                new TaxonConstraintTO<Integer>(2, 22),
                new TaxonConstraintTO<Integer>(3, 11),
                new TaxonConstraintTO<Integer>(4, 22),
                new TaxonConstraintTO<Integer>(5, 11),
                new TaxonConstraintTO<Integer>(5, 22));
        List<TaxonConstraintTO<Integer>> txTOs2 = Arrays.asList(
                new TaxonConstraintTO<Integer>(1, 11),
                new TaxonConstraintTO<Integer>(1, 22),
                new TaxonConstraintTO<Integer>(2, 22),
                new TaxonConstraintTO<Integer>(3, 11));
        List<TaxonConstraintTO<Integer>> txTOs3 = new ArrayList<>();
        
        TaxonConstraintTOResultSet<Integer> txTOrs1 = getMockResultSet(TaxonConstraintTOResultSet.class, txTOs1);
        TaxonConstraintTOResultSet<Integer> txTOrs2 = getMockResultSet(TaxonConstraintTOResultSet.class, txTOs2);
        TaxonConstraintTOResultSet<Integer> txTOrs3 = getMockResultSet(TaxonConstraintTOResultSet.class, txTOs3);
        
        when(tcDAO.getAnatEntityRelationTaxonConstraints(speciesIds, 
                allRelations.stream().map(rel -> rel.getId()).collect(Collectors.toSet()), null)).thenReturn(txTOrs1);
        when(tcDAO.getAnatEntityRelationTaxonConstraints(speciesIds, 
                relationTOs2.stream().map(rel -> rel.getId()).collect(Collectors.toSet()), null)).thenReturn(txTOrs2);
        when(tcDAO.getAnatEntityRelationTaxonConstraints(speciesIds, new HashSet<>(),  null)).thenReturn(txTOrs3);
        
//        
        Set<RelationType> expRelationTypes1 = new HashSet<>(Arrays.asList(
                RelationType.ISA_PARTOF, RelationType.DEVELOPSFROM));
        
        Set<RelationType> expRelationTypes23 = new HashSet<>(Arrays.asList(RelationType.ISA_PARTOF));

        OntologyService service = new OntologyService(serviceFactory);

        OntologyBase<AnatEntity, String> expectedOntology1 = 
        		new MultiSpeciesOntology<>(speciesIds, anatEntities1, new HashSet<>(allRelations),
        		        taxonConstraints, relationTaxonConstraints,
        		        expRelationTypes1, serviceFactory, AnatEntity.class);
        assertEquals("Incorrect anatomical entity ontology",
                expectedOntology1, service.getAnatEntityOntology(speciesIds, anatEntityIds,
                		expRelationTypes1, true, true));
        
        OntologyBase<AnatEntity, String> expectedOntology2 = 
        		new MultiSpeciesOntology<>(speciesIds, anatEntities2, new HashSet<>(relationTOs2),
                        taxonConstraints, relationTaxonConstraints,
        		        expRelationTypes23, serviceFactory, AnatEntity.class);
        assertEquals("Incorrect anatomical entity ontology",
                expectedOntology2, service.getAnatEntityOntology(speciesIds, anatEntityIds,
                		expRelationTypes23, true, false));
        
        OntologyBase<AnatEntity, String> expectedOntology3 = 
        		new MultiSpeciesOntology<>(speciesIds, anatEntities3, new HashSet<>(relationTOs3),
                        taxonConstraints, relationTaxonConstraints,
        		        expRelationTypes23, serviceFactory, AnatEntity.class);
        assertEquals("Incorrect anatomical entity ontology",
                expectedOntology3, service.getAnatEntityOntology(speciesIds, anatEntityIds,
                		expRelationTypes23, false, false));
    }
    
    /**
     * Test the method{@link OntologyService#getAnatEntityOntology(
     * Collection, Collection, boolean, boolean, AnatEntityService)}.
     * <p>
     * Regression test: missed relations of inferred entities.
     */
    @Test
    public void shouldGetAnatEntityOntology_gettingAllRelations() {
        DAOManager managerMock = mock(DAOManager.class);
        RelationDAO relationDao = mock(RelationDAO.class);
        when(managerMock.getRelationDAO()).thenReturn(relationDao);
        
        Set<Integer> speciesIds = new HashSet<>(Arrays.asList(11, 22, 44));
        Set<String> anatEntityIds = new HashSet<String>(Arrays.asList("UBERON:0004"));

        Set<String> sourceAnatEntityIds = new HashSet<String>(anatEntityIds);
        Set<String> targetAnatEntityIds = new HashSet<String>(anatEntityIds);

        Set<RelationTO.RelationType> daoRelationTypes = new HashSet<>(
                Arrays.asList(RelationTO.RelationType.ISA_PARTOF));

        Set<RelationStatus> relationStatus = EnumSet.complementOf(EnumSet.of(RelationStatus.REFLEXIVE));

        // UBERON:0001 is_a UBERON:0002 is_a UBERON:0003 is_a UBERON:0004
        List<RelationTO<String>> allRelations = Arrays.asList(
                new OntologyService.WrapperRelationTO<>(new RelationTO<>(1, "UBERON:0002", "UBERON:0001", RelationTO.RelationType.ISA_PARTOF, RelationStatus.DIRECT)),
                new OntologyService.WrapperRelationTO<>(new RelationTO<>(2, "UBERON:0003", "UBERON:0002", RelationTO.RelationType.ISA_PARTOF, RelationStatus.DIRECT)),
                new OntologyService.WrapperRelationTO<>(new RelationTO<>(3, "UBERON:0003", "UBERON:0001", RelationTO.RelationType.ISA_PARTOF, RelationStatus.INDIRECT)),     
                new OntologyService.WrapperRelationTO<>(new RelationTO<>(4, "UBERON:0004", "UBERON:0003", RelationTO.RelationType.ISA_PARTOF, RelationStatus.DIRECT)),
                new OntologyService.WrapperRelationTO<>(new RelationTO<>(5, "UBERON:0004", "UBERON:0002", RelationTO.RelationType.ISA_PARTOF, RelationStatus.INDIRECT)),
                new OntologyService.WrapperRelationTO<>(new RelationTO<>(6, "UBERON:0004", "UBERON:0001", RelationTO.RelationType.ISA_PARTOF, RelationStatus.INDIRECT)));
        List<RelationTO<String>> relationTOs1 = Arrays.asList(allRelations.get(3), allRelations.get(4), allRelations.get(5));

        RelationTOResultSet<String> mockRelationRs1 = getMockResultSet(RelationTOResultSet.class, relationTOs1);
        when(relationDao.getAnatEntityRelations(
                speciesIds, true, sourceAnatEntityIds, targetAnatEntityIds, true, 
                daoRelationTypes, relationStatus, null))
            .thenReturn(mockRelationRs1);

        Set<String> newSourceAnatEntityIds = new HashSet<String>(Arrays.asList("UBERON:0001", "UBERON:0002", "UBERON:0003"));
        Set<String> newTargetAnatEntityIds = new HashSet<String>(Arrays.asList("UBERON:0001", "UBERON:0002", "UBERON:0003"));
        List<RelationTO<String>> relationTOs1b = Arrays.asList(allRelations.get(0), allRelations.get(1), allRelations.get(2));
        RelationTOResultSet<String> mockRelationRs1b = getMockResultSet(RelationTOResultSet.class, relationTOs1b);
        when(relationDao.getAnatEntityRelations(
                speciesIds, true, newSourceAnatEntityIds, newTargetAnatEntityIds, true, 
                daoRelationTypes, relationStatus, null))
            .thenReturn(mockRelationRs1b);

        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
        AnatEntityService anatEntityService = mock(AnatEntityService.class);
        when(serviceFactory.getAnatEntityService()).thenReturn(anatEntityService);
        
        Set<AnatEntity> anatEntities = new HashSet<>(Arrays.asList(
                new AnatEntity("1", "UBERON:0001", "desc1"),
                new AnatEntity("2", "UBERON:0002", "desc2"),
                new AnatEntity("3", "UBERON:0003", "desc3"),
                new AnatEntity("4", "UBERON:0004", "desc4")));
        Stream<AnatEntity> anatEntityStream1 = anatEntities.stream();
        Set<String> expAnatEntityIds1 = new HashSet<String>(
                Arrays.asList("UBERON:0001", "UBERON:0002", "UBERON:0003", "UBERON:0004"));
        when(anatEntityService.loadAnatEntities(speciesIds, true, expAnatEntityIds1, true))
            .thenReturn(anatEntityStream1);

        TaxonConstraintService tcService = mock(TaxonConstraintService.class);
        when(serviceFactory.getTaxonConstraintService()).thenReturn(tcService);

        Set<TaxonConstraint<String>> taxonConstraints = new HashSet<>(Arrays.asList(
                new TaxonConstraint<>("UBERON:0001", null),
                new TaxonConstraint<>("UBERON:0002", null),
                new TaxonConstraint<>("UBERON:0003", null),
                new TaxonConstraint<>("UBERON:0004", null)));
        // Note: we need to use thenReturn() twice because a stream can be use only once 
        when(tcService.loadAnatEntityTaxonConstraintBySpeciesIds(speciesIds))
            .thenReturn(taxonConstraints.stream()).thenReturn(taxonConstraints.stream());
        
        Set<TaxonConstraint<Integer>> relationTaxonConstraints = new HashSet<>(Arrays.asList(
                new TaxonConstraint<>(1, null),
                new TaxonConstraint<>(2, null),
                new TaxonConstraint<>(3, null),
                new TaxonConstraint<>(4, null),
                new TaxonConstraint<>(5, null),
                new TaxonConstraint<>(6, null)));
        // Note: we need to use thenReturn() twice because a stream can be use only once 
        when(tcService.loadAnatEntityRelationTaxonConstraintBySpeciesIds(speciesIds))
            .thenReturn(relationTaxonConstraints.stream())
            .thenReturn(relationTaxonConstraints.stream());
        
        TaxonConstraintDAO tcDAO = mock(TaxonConstraintDAO.class);
        when(managerMock.getTaxonConstraintDAO()).thenReturn(tcDAO);
        
        List<TaxonConstraintTO<Integer>> tcTOs = Arrays.asList(
                new TaxonConstraintTO<Integer>(1, null),
                new TaxonConstraintTO<Integer>(2, null),
                new TaxonConstraintTO<Integer>(3, null),
                new TaxonConstraintTO<Integer>(4, null),
                new TaxonConstraintTO<Integer>(5, null),
                new TaxonConstraintTO<Integer>(6, null));
        
        TaxonConstraintTOResultSet<Integer> tcTOResSet = getMockResultSet(TaxonConstraintTOResultSet.class, tcTOs);
        
        when(tcDAO.getAnatEntityRelationTaxonConstraints(speciesIds, 
                allRelations.stream().map(rel -> rel.getId()).collect(Collectors.toSet()), null)).thenReturn(tcTOResSet);
        
        Set<RelationType> expRelationTypes = new HashSet<>(Arrays.asList(RelationType.ISA_PARTOF));

        OntologyService service = new OntologyService(serviceFactory);

        OntologyBase<AnatEntity, String> expectedOntology1 = 
                new MultiSpeciesOntology<>(speciesIds, anatEntities, new HashSet<>(allRelations),
                        taxonConstraints, relationTaxonConstraints,
                        expRelationTypes, serviceFactory, AnatEntity.class);
        assertEquals("Incorrect anatomical entity ontology",
                expectedOntology1, service.getAnatEntityOntology(speciesIds, anatEntityIds,
                        expRelationTypes, true, true));
    }

    /**
     * Test the method 
     * {@link OntologyService#getDevStageOntology(Collection, boolean, boolean, DevStageService)}.
     */
    @Test
    public void shouldGetStageOntology() {
        DAOManager managerMock = mock(DAOManager.class);
        RelationDAO relationDao = mock(RelationDAO.class);
        when(managerMock.getRelationDAO()).thenReturn(relationDao);
        
        Set<Integer> speciesIds = new HashSet<>(Arrays.asList(11, 22, 44));
        Set<String> stageIds = new HashSet<String>(Arrays.asList("Stage_id1", "Stage_id2"));

        Set<String> sourceStageIds = new HashSet<String>(stageIds);
        Set<String> targetStageIds = new HashSet<String>(stageIds);
                
        // Stage_id1 -----------------
        // | is_a       \ is_a        |
        // Stage_id2     Stage_id2p   | is_a (indirect)
        // | is_a       / is_a        |
        // Stage_id3 -----------------

        Set<RelationStatus> relationStatus = EnumSet.complementOf(EnumSet.of(RelationStatus.REFLEXIVE));

        List<RelationTO<String>> allRelationTOs = Arrays.asList(
        		new OntologyService.WrapperRelationTO<>(new RelationTO<>(null, "Stage_id2", "Stage_id1", RelationTO.RelationType.ISA_PARTOF, RelationStatus.DIRECT)),
        		new OntologyService.WrapperRelationTO<>(new RelationTO<>(null, "Stage_id2p", "Stage_id1", RelationTO.RelationType.ISA_PARTOF, RelationStatus.DIRECT)),
        		new OntologyService.WrapperRelationTO<>(new RelationTO<>(null, "Stage_id3", "Stage_id2", RelationTO.RelationType.ISA_PARTOF, RelationStatus.DIRECT)),
        		new OntologyService.WrapperRelationTO<>(new RelationTO<>(null, "Stage_id3", "Stage_id2p", RelationTO.RelationType.ISA_PARTOF, RelationStatus.DIRECT)),
        		new OntologyService.WrapperRelationTO<>(new RelationTO<>(null, "Stage_id3", "Stage_id1", RelationTO.RelationType.ISA_PARTOF, RelationStatus.INDIRECT)));
        List<RelationTO<String>> relationTOs1 = Arrays.asList(allRelationTOs.get(0),
                allRelationTOs.get(1), allRelationTOs.get(2), allRelationTOs.get(4));
        RelationTOResultSet<String> mockRelationRs1 = getMockResultSet(RelationTOResultSet.class, relationTOs1);
        when(relationDao.getStageRelations(speciesIds, true, sourceStageIds, targetStageIds,
        		true, relationStatus, null))
        	.thenReturn(mockRelationRs1);
        
        Set<String> newSourceStageIds = new HashSet<String>(Arrays.asList("Stage_id2p", "Stage_id3"));
        Set<String> newTargetStageIds = new HashSet<String>(Arrays.asList("Stage_id2p", "Stage_id3"));
        List<RelationTO<String>> relationTOs1b = Arrays.asList(allRelationTOs.get(1),
                allRelationTOs.get(2), allRelationTOs.get(3), allRelationTOs.get(4));
        RelationTOResultSet<String> mockRelationRs1b = getMockResultSet(RelationTOResultSet.class, relationTOs1b);
        when(relationDao.getStageRelations(speciesIds, true, newSourceStageIds, newTargetStageIds,
                true, relationStatus, null))
            .thenReturn(mockRelationRs1b);

        List<RelationTO<String>> relationTOs2 = Arrays.asList(allRelationTOs.get(0),
                allRelationTOs.get(1), allRelationTOs.get(2), allRelationTOs.get(4));
        RelationTOResultSet<String> mockRelationRs2 = getMockResultSet(RelationTOResultSet.class, relationTOs2);
        when(relationDao.getStageRelations(speciesIds, true, null, targetStageIds,
        		true, relationStatus, null))
        	.thenReturn(mockRelationRs2);
        List<RelationTO<String>> relationTOs2b = Arrays.asList(allRelationTOs.get(3));
        RelationTOResultSet<String> mockRelationRs2b = getMockResultSet(RelationTOResultSet.class, relationTOs2b);
        when(relationDao.getStageRelations(speciesIds, true, new HashSet<>(), 
                new HashSet<String>(Arrays.asList("Stage_id2p", "Stage_id3")), true, relationStatus, null))
            .thenReturn(mockRelationRs2b);
        
        List<RelationTO<String>> relationTOs3 = Arrays.asList(
                new OntologyService.WrapperRelationTO<>(new RelationTO<>(null, "Stage_id2", "Stage_id1", RelationTO.RelationType.ISA_PARTOF, RelationStatus.DIRECT)));
        RelationTOResultSet<String> mockRelationRs3 = getMockResultSet(RelationTOResultSet.class, relationTOs3);
        when(relationDao.getStageRelations(speciesIds, true, sourceStageIds, targetStageIds,
        		false, relationStatus, null))
        	.thenReturn(mockRelationRs3);

        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
        DevStageService devStageService = mock(DevStageService.class);
        when(serviceFactory.getDevStageService()).thenReturn(devStageService);

        Set<DevStage> devStages1 = new HashSet<>(Arrays.asList(
        		new DevStage("Stage_id1", "Stage_id1", "desc1", 0, 0, 0, false, false),
        		new DevStage("Stage_id2", "Stage_id2", "desc2", 0, 0, 0, false, false),
        		new DevStage("Stage_id2p", "Stage_id2p", "desc2p", 0, 0, 0, false, false),
        		new DevStage("Stage_id3", "Stage_id3", "desc3", 0, 0, 0, false, false)));
        Set<String> expStageIds1 = new HashSet<String>(
        		Arrays.asList("Stage_id1", "Stage_id2", "Stage_id2p", "Stage_id3"));
        when(devStageService.loadDevStages(speciesIds, true, expStageIds1))
            .thenReturn(devStages1.stream()).thenReturn(devStages1.stream());

        Set<DevStage> devStages2 = new HashSet<>(Arrays.asList(
        		new DevStage("Stage_id1", "Stage_id1", "desc1", 0, 0, 0, false, false),
        		new DevStage("Stage_id2", "Stage_id2", "desc2", 0, 0, 0, false, false),
                new DevStage("Stage_id2p", "Stage_id2p", "desc2p", 0, 0, 0, false, false),
        		new DevStage("Stage_id3", "Stage_id3", "desc3", 0, 0, 0, false, false)));
        Set<String> expStageIds2 = new HashSet<String>(
        		Arrays.asList("Stage_id1", "Stage_id2", "Stage_id3"));
        when(devStageService.loadDevStages(speciesIds, true, expStageIds2))
            .thenReturn(devStages2.stream()).thenReturn(devStages2.stream());

        Set<DevStage> devStages3 = new HashSet<>(Arrays.asList(
        		new DevStage("Stage_id1", "Stage_id1", "desc1", 0, 0, 0, false, false),
        		new DevStage("Stage_id2", "Stage_id2", "desc2", 0, 0, 0, false, false)));
        Set<String> expStageIds3 = new HashSet<String>(Arrays.asList("Stage_id1", "Stage_id2"));
        when(devStageService.loadDevStages(speciesIds, true, expStageIds3)).thenReturn(devStages3.stream());

        TaxonConstraintService tcService = mock(TaxonConstraintService.class);
        when(serviceFactory.getTaxonConstraintService()).thenReturn(tcService);
        Set<TaxonConstraint<String>> stageTCs = 
                // stage1 sp1/sp2 -------
                // |               \     \    
                // stage2 sp1/sp2   |     stage2p sp2
                // |               /      | 
                // stage3 sp1             stage3p sp2
                new HashSet<>(Arrays.asList(
                        new TaxonConstraint<>("Stage_id1", null),
                        new TaxonConstraint<>("Stage_id2", null),
                        new TaxonConstraint<>("Stage_id2p", 2),
                        new TaxonConstraint<>("Stage_id3", 1),
                        new TaxonConstraint<>("Stage_id3p", 2)));

        // Note: we need to use thenReturn() twice because a stream can be use only once
        when(tcService.loadDevStageTaxonConstraintBySpeciesIds(speciesIds))
            .thenReturn(stageTCs.stream()).thenReturn(stageTCs.stream())
            .thenReturn(stageTCs.stream()).thenReturn(stageTCs.stream())
            .thenReturn(stageTCs.stream()).thenReturn(stageTCs.stream());

        Set<RelationType> expRelationTypes = new HashSet<>();
        expRelationTypes.add(RelationType.ISA_PARTOF);

        OntologyService service = new OntologyService(serviceFactory);

        MultiSpeciesOntology<DevStage, String> expectedOntology1 = 
        		new MultiSpeciesOntology<DevStage, String>(speciesIds, devStages1,
        		        new HashSet<>(allRelationTOs), stageTCs, null,
        		        expRelationTypes, serviceFactory, DevStage.class);
        assertEquals("Incorrect dev. stage ontology", expectedOntology1,
        		service.getDevStageOntology(speciesIds, stageIds, true, true));

        MultiSpeciesOntology<DevStage, String> expectedOntology2 = 
        		new MultiSpeciesOntology<DevStage, String>(speciesIds, devStages2,
        		        new HashSet<>(allRelationTOs), stageTCs, null,
        		        expRelationTypes, serviceFactory, DevStage.class);
        assertEquals("Incorrect dev. stage ontology", expectedOntology2,
        		service.getDevStageOntology(speciesIds, stageIds, false, true));

        MultiSpeciesOntology<DevStage, String> expectedOntology3 = 
        		new MultiSpeciesOntology<DevStage, String>(speciesIds, devStages3,
        		        new HashSet<>(relationTOs3), stageTCs, null,
        		        expRelationTypes, serviceFactory, DevStage.class);
        assertEquals("Incorrect dev. stage ontology", expectedOntology3, 
        		service.getDevStageOntology(speciesIds, stageIds, false, false));
    }
    
    /**
     * Test the method 
     * {@link OntologyService#getTaxonOntology(java.util.Collection, java.util.Collection, boolean, boolean)}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void shouldGetTaxonOntology() {
        
        DAOManager managerMock = mock(DAOManager.class);
        RelationDAO relationDao = mock(RelationDAO.class);
        when(managerMock.getRelationDAO()).thenReturn(relationDao);

        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
        TaxonService taxonService = mock(TaxonService.class);
        when(serviceFactory.getTaxonService()).thenReturn(taxonService);
        
        Set<Integer> speciesIds = new HashSet<>();
        speciesIds.addAll(Arrays.asList(11, 22, 44));
        Set<Integer> taxonIds = new HashSet<>();
        taxonIds.addAll(Arrays.asList(1, 2));

        Set<Integer> sourceTaxonIds = new HashSet<>();
        sourceTaxonIds.addAll(taxonIds);
        Set<Integer> targetTaxonIds = new HashSet<>();
        targetTaxonIds.addAll(taxonIds);

        Set<RelationTO.RelationType> daoRelationTypes = new HashSet<>();
        daoRelationTypes.add(RelationTO.RelationType.ISA_PARTOF);

        Taxon taxon1 = new Taxon(1, "1", "desc1", "sc1", 1, true);
        Taxon taxon2 = new Taxon(2, "2", "desc2", "sc2", 2, true);
        Taxon taxon3 = new Taxon(3, "3", "desc3", "sc3", 3, false);
        Taxon taxon4 = new Taxon(4, "4", "desc4", "sc4", 3, true);
        RelationTO<Integer> rel1 = new OntologyService.WrapperRelationTO<>(new RelationTO<>(
                null, 2, 1, RelationTO.RelationType.ISA_PARTOF, RelationStatus.DIRECT));
        RelationTO<Integer> rel2 = new OntologyService.WrapperRelationTO<>(new RelationTO<>(
                null, 3, 2, RelationTO.RelationType.ISA_PARTOF, RelationStatus.DIRECT));
        RelationTO<Integer> rel3 = new OntologyService.WrapperRelationTO<>(new RelationTO<>(
                null, 4, 2, RelationTO.RelationType.ISA_PARTOF, RelationStatus.DIRECT));
        RelationTO<Integer> rel4 = new OntologyService.WrapperRelationTO<>(new RelationTO<>(
                null, 3, 1, RelationTO.RelationType.ISA_PARTOF, RelationStatus.INDIRECT));
        RelationTO<Integer> rel5 = new OntologyService.WrapperRelationTO<>(new RelationTO<>(
                null, 4, 1, RelationTO.RelationType.ISA_PARTOF, RelationStatus.INDIRECT));
        Set<RelationType> relTypes = EnumSet.of(RelationType.ISA_PARTOF);
        Set<RelationStatus> relationStatus = EnumSet.complementOf(EnumSet.of(RelationStatus.REFLEXIVE));

        OntologyService service = new OntologyService(serviceFactory);

        Set<Taxon> taxa = new HashSet<>(Arrays.asList(taxon1, taxon2, taxon3, taxon4));
        when(taxonService.loadTaxa(new HashSet<>(), false))
        .thenReturn(taxa.stream());
        RelationTOResultSet<Integer> mockRelationRs = getMockResultSet(RelationTOResultSet.class,
                Arrays.asList(rel1, rel2, rel3, rel4, rel5));
        when(relationDao.getTaxonRelations(new HashSet<>(), new HashSet<>(),
                false, relationStatus, false, null))
            .thenReturn(mockRelationRs);
        Ontology<Taxon,Integer> expectedOntology = new Ontology<>(null, taxa,
                new HashSet<>(Arrays.asList(rel1, rel2, rel3, rel4, rel5)),
                relTypes, serviceFactory, Taxon.class);
        assertEquals("Incorrect taxon ontology", expectedOntology, service.getTaxonOntology());

        taxa = new HashSet<>(Arrays.asList(taxon1, taxon2, taxon3, taxon4));
        when(taxonService.loadTaxa(taxa.stream().map(t -> t.getId()).collect(Collectors.toSet()), false))
        .thenReturn(taxa.stream());
        mockRelationRs = getMockResultSet(RelationTOResultSet.class,
                Arrays.asList(rel2, rel3, rel4, rel5));
        when(relationDao.getTaxonRelations(new HashSet<>(Arrays.asList(taxon3.getId(), taxon4.getId())),
                null, true, relationStatus, false, null))
            .thenReturn(mockRelationRs);
        mockRelationRs = getMockResultSet(RelationTOResultSet.class,
                Arrays.asList(rel1));
        when(relationDao.getTaxonRelations(new HashSet<>(Arrays.asList(taxon1.getId(), taxon2.getId())),
                new HashSet<>(), true, relationStatus, false, null))
            .thenReturn(mockRelationRs);
        assertEquals("Incorrect taxon ontology", expectedOntology,
                service.getTaxonOntologyFromTaxonIds(Arrays.asList(taxon3.getId(), taxon4.getId()),
                        false, true, false));

        taxa = new HashSet<>(Arrays.asList(taxon1, taxon2, taxon3, taxon4));
        when(taxonService.loadTaxa(taxa.stream().map(t -> t.getId()).collect(Collectors.toSet()), false))
        .thenReturn(taxa.stream());
        mockRelationRs = getMockResultSet(RelationTOResultSet.class,
                Arrays.asList(rel1, rel4, rel5));
        when(relationDao.getTaxonRelations(null, new HashSet<>(Arrays.asList(taxon1.getId())),
                true, relationStatus, false, null))
            .thenReturn(mockRelationRs);
        mockRelationRs = getMockResultSet(RelationTOResultSet.class,
                Arrays.asList(rel2, rel3));
        when(relationDao.getTaxonRelations(new HashSet<>(),
                new HashSet<>(Arrays.asList(taxon2.getId(), taxon3.getId(), taxon4.getId())), true,
                relationStatus, false, null))
            .thenReturn(mockRelationRs);
        assertEquals("Incorrect taxon ontology", expectedOntology,
                service.getTaxonOntologyFromTaxonIds(Arrays.asList(taxon1.getId()),
                        false, false, true));

        taxa = new HashSet<>(Arrays.asList(taxon1, taxon2, taxon3, taxon4));
        when(taxonService.loadTaxa(taxa.stream().map(t -> t.getId()).collect(Collectors.toSet()), false))
        .thenReturn(taxa.stream());
        mockRelationRs = getMockResultSet(RelationTOResultSet.class,
                Arrays.asList(rel1, rel2, rel3));
        when(relationDao.getTaxonRelations(null, new HashSet<>(Arrays.asList(taxon2.getId())), true,
                relationStatus, false, null))
            .thenReturn(mockRelationRs);
        mockRelationRs = getMockResultSet(RelationTOResultSet.class,
                Arrays.asList(rel4, rel5));
        when(relationDao.getTaxonRelations(new HashSet<>(),
                new HashSet<>(Arrays.asList(taxon3.getId(), taxon4.getId())), true,
                relationStatus, false, null))
            .thenReturn(mockRelationRs);
        assertEquals("Incorrect taxon ontology", expectedOntology,
                service.getTaxonOntologyFromTaxonIds(Arrays.asList(taxon2.getId()),
                        false, false, true));

        taxa = new HashSet<>(Arrays.asList(taxon1, taxon2));
        when(taxonService.loadTaxa(taxa.stream().map(t -> t.getId()).collect(Collectors.toSet()), false))
        .thenReturn(taxa.stream());
        mockRelationRs = getMockResultSet(RelationTOResultSet.class,
                Arrays.asList(rel1));
        when(relationDao.getTaxonRelations(new HashSet<>(Arrays.asList(taxon2.getId())),
                new HashSet<>(Arrays.asList(taxon2.getId())), true,
                relationStatus, true, null))
            .thenReturn(mockRelationRs);
        mockRelationRs = getMockResultSet(RelationTOResultSet.class,
                Arrays.asList(rel1));
        when(relationDao.getTaxonRelations(new HashSet<>(taxon1.getId()),
                new HashSet<>(taxon1.getId()), true, relationStatus, true, null))
        .thenReturn(mockRelationRs);
        expectedOntology = new Ontology<>(null, taxa,
                new HashSet<>(Arrays.asList(rel1)),
                relTypes, serviceFactory, Taxon.class);
        assertEquals("Incorrect taxon ontology", expectedOntology,
                service.getTaxonOntologyFromTaxonIds(Arrays.asList(taxon2.getId()),
                        true, true, true));
    }
}
