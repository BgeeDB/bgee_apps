package org.bgee.model.ontology;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import org.bgee.model.ServiceFactory;
import org.bgee.model.TestAncestor;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.DevStageService;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.ontologycommon.RelationDAO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationStatus;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationType;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTOResultSet;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.expressiondata.Condition;
import org.bgee.model.expressiondata.ConditionUtils;
import org.junit.Test;

/**
 * This class holds the unit tests for the {@code OntologyService} class.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Jan. 2016
 * @since   Bgee 13, Dec. 2015
 */
public class OntologyServiceTest extends TestAncestor {

    /**
     * Test the method{@link OntologyService#getAnatEntityOntology(
     * Collection, Collection, boolean, boolean, AnatEntityService)}.
     */
    @Test
    public void shouldGetAnatEntityOntology() {
        DAOManager managerMock = mock(DAOManager.class);
        RelationDAO relationDao = mock(RelationDAO.class);
        when(managerMock.getRelationDAO()).thenReturn(relationDao);
        
        Set<String> speciesIds = new HashSet<String>();
        speciesIds.addAll(Arrays.asList("11", "22", "44"));
        Set<String> anatEntityIds = new HashSet<String>();
        anatEntityIds.addAll(Arrays.asList("UBERON:0002", "UBERON:0002p"));

        Set<String> sourceAnatEntityIds = new HashSet<String>();
        sourceAnatEntityIds.addAll(anatEntityIds);
        Set<String> targetAnatEntityIds = new HashSet<String>();
        targetAnatEntityIds.addAll(anatEntityIds);
                
        Set<RelationTO.RelationType> daoRelationTypes1 = new HashSet<>();
        daoRelationTypes1.add(RelationTO.RelationType.ISA_PARTOF);
        daoRelationTypes1.add(RelationTO.RelationType.DEVELOPSFROM);

        Set<RelationTO.RelationType> daoRelationTypes23 = new HashSet<>();
        daoRelationTypes23.add(RelationTO.RelationType.ISA_PARTOF);

        // UBERON:0001 ------------------
        // | is_a       \ dev_from      |
        // UBERON:0002   UBERON:0002p   | is_a (indirect)
        // | is_a       / is_a          |
        // UBERON:0003 ------------------
        List<RelationTO> relationTOs1 = Arrays.asList(
        		new RelationTO("1", "UBERON:0002", "UBERON:0001", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
        		new RelationTO("2", "UBERON:0002p", "UBERON:0001", RelationType.DEVELOPSFROM, RelationStatus.DIRECT),
        		new RelationTO("3", "UBERON:0003", "UBERON:0002", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
        		new RelationTO("4", "UBERON:0003", "UBERON:0002p", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
        		new RelationTO("5", "UBERON:0003", "UBERON:0001", RelationType.ISA_PARTOF, RelationStatus.INDIRECT));
        RelationTOResultSet mockRelationRs1 = getMockResultSet(RelationTOResultSet.class, relationTOs1);
        when(relationDao.getAnatEntityRelations(
        		speciesIds, true, sourceAnatEntityIds, targetAnatEntityIds, true, 
        		daoRelationTypes1, EnumSet.complementOf(EnumSet.of(RelationStatus.REFLEXIVE)), null))
        	.thenReturn(mockRelationRs1);
        
        List<RelationTO> relationTOs2 = Arrays.asList(
        		new RelationTO("1", "UBERON:0002", "UBERON:0001", RelationType.ISA_PARTOF, RelationStatus.DIRECT));
        RelationTOResultSet mockRelationRs2 = getMockResultSet(RelationTOResultSet.class, relationTOs2);
        when(relationDao.getAnatEntityRelations(
        		speciesIds, true, sourceAnatEntityIds, null, true, 
        		daoRelationTypes23, EnumSet.complementOf(EnumSet.of(RelationStatus.REFLEXIVE)), null))
        	.thenReturn(mockRelationRs2);

        List<RelationTO> relationTOs3 = new ArrayList<>();
        RelationTOResultSet mockRelationRs3 = getMockResultSet(RelationTOResultSet.class, relationTOs3);
        when(relationDao.getAnatEntityRelations(
        		speciesIds, true, sourceAnatEntityIds, targetAnatEntityIds, false, 
        		daoRelationTypes23, EnumSet.complementOf(EnumSet.of(RelationStatus.REFLEXIVE)), null))
        	.thenReturn(mockRelationRs3);

        AnatEntityService anatEntityService = mock(AnatEntityService.class);
        
        Set<AnatEntity> anatEntities1 = new HashSet<>(Arrays.asList(
        		new AnatEntity("1", "UBERON:0001", "desc1"),
        		new AnatEntity("2", "UBERON:0002", "desc2"),
        		new AnatEntity("3", "UBERON:0002p", "desc2p"),
        		new AnatEntity("4", "UBERON:0003", "desc3")));
        Stream<AnatEntity> anatEntityStream1 = anatEntities1.stream();
        Set<String> expAnatEntityIds1 = new HashSet<String>(
        		Arrays.asList("UBERON:0001", "UBERON:0002", "UBERON:0002p", "UBERON:0003"));
        when(anatEntityService.loadAnatEntities(speciesIds, true, expAnatEntityIds1))
        	.thenReturn(anatEntityStream1);

        Set<AnatEntity> anatEntities2 = new HashSet<>(Arrays.asList(
        		new AnatEntity("1", "UBERON:0001", "desc1"),
        		new AnatEntity("2", "UBERON:0002", "desc2"),
        		new AnatEntity("3", "UBERON:0002p", "desc2p")));
        Stream<AnatEntity> anatEntityStream2 = anatEntities2.stream();
        Set<String> expAnatEntityIds2 = new HashSet<String>(
        		Arrays.asList("UBERON:0001", "UBERON:0002", "UBERON:0002p"));
        when(anatEntityService.loadAnatEntities(speciesIds, true, expAnatEntityIds2))
        	.thenReturn(anatEntityStream2);

        Set<AnatEntity> anatEntities3 = new HashSet<>(Arrays.asList(
        		new AnatEntity("2", "UBERON:0002", "desc2"),
        		new AnatEntity("3", "UBERON:0002p", "desc2p")));
        Stream<AnatEntity> anatEntityStream3 = anatEntities3.stream();
        Set<String> expAnatEntityIds3 = new HashSet<String>(
        		Arrays.asList("UBERON:0002", "UBERON:0002p"));
        when(anatEntityService.loadAnatEntities(speciesIds, true, expAnatEntityIds3))
        	.thenReturn(anatEntityStream3);

        Set<Ontology.RelationType> expRelationTypes1 = new HashSet<>();
        expRelationTypes1.add(Ontology.RelationType.ISA_PARTOF);
        expRelationTypes1.add(Ontology.RelationType.DEVELOPSFROM);
        
        Set<Ontology.RelationType> expRelationTypes23 = new HashSet<>();
        expRelationTypes23.add(Ontology.RelationType.ISA_PARTOF);

        OntologyService service = new OntologyService(managerMock);

        Ontology<AnatEntity> expectedOntology1 = 
        		new Ontology<>(anatEntities1, new HashSet<>(relationTOs1), expRelationTypes1);
        assertEquals("Incorrect anatomical entity ontology",
                expectedOntology1, service.getAnatEntityOntology(speciesIds, anatEntityIds,
                		expRelationTypes1, true, true, anatEntityService));
        
        Ontology<AnatEntity> expectedOntology2 = 
        		new Ontology<>(anatEntities2, new HashSet<>(relationTOs2), expRelationTypes23);
        assertEquals("Incorrect anatomical entity ontology",
                expectedOntology2, service.getAnatEntityOntology(speciesIds, anatEntityIds,
                		expRelationTypes23, true, false, anatEntityService));
        
        Ontology<AnatEntity> expectedOntology3 = 
        		new Ontology<>(anatEntities3, new HashSet<>(relationTOs3), expRelationTypes23);
        assertEquals("Incorrect anatomical entity ontology",
                expectedOntology3, service.getAnatEntityOntology(speciesIds, anatEntityIds,
                		expRelationTypes23, false, false, anatEntityService));
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
        
        Set<String> speciesIds = new HashSet<String>();
        speciesIds.addAll(Arrays.asList("11", "22", "44"));
        Set<String> stageIds = new HashSet<String>();
        stageIds.addAll(Arrays.asList("Stage_id1", "Stage_id2"));

        Set<String> sourceStageIds = new HashSet<String>();
        sourceStageIds.addAll(stageIds);
        Set<String> targetStageIds = new HashSet<String>();
        targetStageIds.addAll(stageIds);
                
        // Stage_id1 -----------------
        // | is_a       \ is_a        |
        // Stage_id2     Stage_id2p   | is_a (indirect)
        // | is_a       / is_a        |
        // Stage_id3 -----------------

        Set<RelationTO.RelationType> daoRelationTypes = new HashSet<>();
        daoRelationTypes.add(RelationTO.RelationType.ISA_PARTOF);
        
        List<RelationTO> relationTOs1 = Arrays.asList(
        		new RelationTO("12", "Stage_id2", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
        		new RelationTO("12", "Stage_id2p", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
        		new RelationTO("12", "Stage_id3", "Stage_id2", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
        		new RelationTO("12", "Stage_id3", "Stage_id2p", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
        		new RelationTO("13", "Stage_id3", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT));
        RelationTOResultSet mockRelationRs1 = getMockResultSet(RelationTOResultSet.class, relationTOs1);
        when(relationDao.getStageRelations(speciesIds, true, sourceStageIds, targetStageIds,
        		true, EnumSet.complementOf(EnumSet.of(RelationStatus.REFLEXIVE)), null))
        	.thenReturn(mockRelationRs1);

        List<RelationTO> relationTOs2 = Arrays.asList(
        		new RelationTO("12", "Stage_id2", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
        		new RelationTO("12", "Stage_id3", "Stage_id2", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
        		new RelationTO("13", "Stage_id3", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT));
        RelationTOResultSet mockRelationRs2 = getMockResultSet(RelationTOResultSet.class, relationTOs2);
        when(relationDao.getStageRelations(speciesIds, true, null, targetStageIds,
        		true, EnumSet.complementOf(EnumSet.of(RelationStatus.REFLEXIVE)), null))
        	.thenReturn(mockRelationRs2);

        List<RelationTO> relationTOs3 = Arrays.asList(
        		new RelationTO("12", "Stage_id2", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT));
        RelationTOResultSet mockRelationRs3 = getMockResultSet(RelationTOResultSet.class, relationTOs3);
        when(relationDao.getStageRelations(speciesIds, true, sourceStageIds, targetStageIds,
        		false, EnumSet.complementOf(EnumSet.of(RelationStatus.REFLEXIVE)), null))
        	.thenReturn(mockRelationRs3);

        DevStageService devStageService = mock(DevStageService.class);
        Set<DevStage> devStages1 = new HashSet<>(Arrays.asList(
        		new DevStage("1", "Stage_id1", "desc1", 0, 0, 0, false, false),
        		new DevStage("2", "Stage_id2", "desc2", 0, 0, 0, false, false),
        		new DevStage("3", "Stage_id2p", "desc2p", 0, 0, 0, false, false),
        		new DevStage("4", "Stage_id3", "desc3", 0, 0, 0, false, false)));
        Stream<DevStage> devStageStream1 = devStages1.stream();
        Set<String> expStageIds1 = new HashSet<String>(
        		Arrays.asList("Stage_id1", "Stage_id2", "Stage_id2p", "Stage_id3"));
        when(devStageService.loadDevStages(speciesIds, true, expStageIds1)).thenReturn(devStageStream1);

        Set<DevStage> devStages2 = new HashSet<>(Arrays.asList(
        		new DevStage("1", "Stage_id1", "desc1", 0, 0, 0, false, false),
        		new DevStage("2", "Stage_id2", "desc2", 0, 0, 0, false, false),
        		new DevStage("4", "Stage_id3", "desc3", 0, 0, 0, false, false)));
        Stream<DevStage> devStageStream2 = devStages2.stream();
        Set<String> expStageIds2 = new HashSet<String>(
        		Arrays.asList("Stage_id1", "Stage_id2", "Stage_id3"));
        when(devStageService.loadDevStages(speciesIds, true, expStageIds2)).thenReturn(devStageStream2);

        Set<DevStage> devStages3 = new HashSet<>(Arrays.asList(
        		new DevStage("1", "Stage_id1", "desc1", 0, 0, 0, false, false),
        		new DevStage("2", "Stage_id2", "desc2", 0, 0, 0, false, false)));
        Stream<DevStage> devStageStream3 = devStages3.stream();
        Set<String> expStageIds3 = new HashSet<String>(Arrays.asList("Stage_id1", "Stage_id2"));
        when(devStageService.loadDevStages(speciesIds, true, expStageIds3)).thenReturn(devStageStream3);

        // TODO there is an error when leftBound of TO (Integer) is null
        // because NestedSetModelEntity contains int leftBound
        
        Set<Ontology.RelationType> expRelationTypes = new HashSet<>();
        expRelationTypes.add(Ontology.RelationType.ISA_PARTOF);

        ServiceFactory fac = new ServiceFactory(managerMock);
        OntologyService service = fac.getOntologyService();

        Ontology<DevStage> expectedOntology1 = 
        		new Ontology<>(devStages1, new HashSet<>(relationTOs1), expRelationTypes);
        assertEquals("Incorrect dev. stage ontology", expectedOntology1,
        		service.getDevStageOntology(speciesIds, stageIds, true, true, devStageService));

        Ontology<DevStage> expectedOntology2 = 
        		new Ontology<>(devStages2, new HashSet<>(relationTOs2), expRelationTypes);
        assertEquals("Incorrect dev. stage ontology", expectedOntology2,
        		service.getDevStageOntology(speciesIds, stageIds, false, true, devStageService));

        Ontology<DevStage> expectedOntology3 = 
        		new Ontology<>(devStages3, new HashSet<>(relationTOs3), expRelationTypes);
        assertEquals("Incorrect dev. stage ontology", expectedOntology3, 
        		service.getDevStageOntology(speciesIds, stageIds, false, false, devStageService));
    }
    
    //TODO: remove when proper unit tests are implemented in ConditionUtilsTest
    //@Test
    public void it() {
        Properties setProps = new Properties();
        setProps.setProperty(MySQLDAOManager.USER_KEY, "bgee");
        setProps.setProperty(MySQLDAOManager.PASSWORD_KEY, "bgee");
        setProps.setProperty(MySQLDAOManager.JDBC_DRIVER_NAMES_KEY, 
                "com.mysql.jdbc.Driver,net.sf.log4jdbc.sql.jdbcapi.DriverSpy");
        setProps.setProperty(MySQLDAOManager.JDBC_URL_KEY, 
                "jdbc:log4jdbc:mysql://altbioinfo.unil.ch:3306/bgee_v13");
        
        DAOManager manager = DAOManager.getDAOManager(setProps);
        
        ServiceFactory fac = new ServiceFactory(manager);
                
        Condition cond1 = new Condition("UBERON:0000465", "UBERON:0000104");
        Condition cond2 = new Condition("UBERON:0000061", "UBERON:0000106");
        ConditionUtils utils = new ConditionUtils("9606", Arrays.asList(cond1, cond2), fac);
        assertTrue(utils.isConditionMorePrecise(cond1, cond2));
        assertFalse(utils.isConditionMorePrecise(cond2, cond1));
        System.out.println(utils.getAnatEntityOntology().getElement("UBERON:0000465"));
        System.out.println(utils.getDevStageOntology().getElement("UBERON:0000104"));
        
        System.out.println(utils.getAnatEntityOntology().getElement("UBERON:0000061")
                .getAncestors(utils.getAnatEntityOntology(), null));
        System.out.println(utils.getDevStageOntology().getElement("UBERON:0000106")
                .getAncestors(utils.getDevStageOntology(), null));
    }
}
