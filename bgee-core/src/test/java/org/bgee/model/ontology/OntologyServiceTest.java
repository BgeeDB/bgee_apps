package org.bgee.model.ontology;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.bgee.model.ServiceFactory;
import org.bgee.model.TestAncestor;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.DevStageService;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTO;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTOResultSet;
import org.bgee.model.dao.api.anatdev.StageDAO;
import org.bgee.model.dao.api.anatdev.StageDAO.StageTO;
import org.bgee.model.dao.api.anatdev.StageDAO.StageTOResultSet;
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
        AnatEntityDAO anatEntityDao = mock(AnatEntityDAO.class);
        when(managerMock.getAnatEntityDAO()).thenReturn(anatEntityDao);
        
        Set<String> speciesIds = new HashSet<String>();
        speciesIds.addAll(Arrays.asList("11", "22", "44"));
        Set<String> anatEntityIds = new HashSet<String>();
        anatEntityIds.addAll(Arrays.asList("Anat_id3", "Anat_id5"));

        Set<String> sourceAnatEntityIds = new HashSet<String>();
        sourceAnatEntityIds.addAll(anatEntityIds);
        Set<String> targetAnatEntityIds = new HashSet<String>();
        targetAnatEntityIds.addAll(anatEntityIds);
        
        boolean sourceOrTarget = true;
        
        Set<RelationTO.RelationType> daoRelationTypes = new HashSet<>();
        daoRelationTypes.add(RelationTO.RelationType.ISA_PARTOF);
        daoRelationTypes.add(RelationTO.RelationType.DEVELOPSFROM);

        List<RelationTO> relationTOs = Arrays.asList(
        		new RelationTO("12", "Anat_id2", "Anat_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
        		new RelationTO("13", "Anat_id3", "Anat_id2", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
        		new RelationTO("14", "Anat_id4", "Anat_id2", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
        		new RelationTO("15", "Anat_id5", "Anat_id2", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
        		new RelationTO("21", "Anat_id9", "Anat_id5", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
        		new RelationTO("22", "Anat_id10", "Anat_id5", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
        		new RelationTO("23", "Anat_id11", "Anat_id10", RelationType.DEVELOPSFROM, RelationStatus.DIRECT));
        
        RelationTOResultSet mockRelationRs = getMockResultSet(RelationTOResultSet.class, relationTOs);
        when(relationDao.getAnatEntityRelations(
        		speciesIds, true, sourceAnatEntityIds, targetAnatEntityIds, sourceOrTarget, 
        		daoRelationTypes, EnumSet.complementOf(EnumSet.of(RelationStatus.REFLEXIVE)), null))
        	.thenReturn(mockRelationRs);

        AnatEntityTOResultSet mockAnatEntityRs = getMockResultSet(AnatEntityTOResultSet.class, Arrays.asList(
        		new AnatEntityTO("1", "Anat_id1", "desc1", null, null, null),
        		new AnatEntityTO("2", "Anat_id2", "desc2", null, null, null),
        		new AnatEntityTO("3", "Anat_id3", "desc3", null, null, null),
        		new AnatEntityTO("4", "Anat_id4", "desc4", null, null, null),
        		new AnatEntityTO("5", "Anat_id5", "desc5", null, null, null),
        		new AnatEntityTO("9", "Anat_id9", "desc9", null, null, null),
        		new AnatEntityTO("10", "Anat_id10", "desc10", null, null, null),
        		new AnatEntityTO("11", "Anat_id11", "desc11", null, null, null)));
        Set<String> expAnatEntityIds = new HashSet<String>();
        expAnatEntityIds.addAll(Arrays.asList("Anat_id1", "Anat_id2", "Anat_id3",
        		"Anat_id4", "Anat_id5", "Anat_id9", "Anat_id10", "Anat_id11"));
        when(anatEntityDao.getAnatEntities(speciesIds, true, expAnatEntityIds, null))
        	.thenReturn(mockAnatEntityRs);

        Set<AnatEntity> expAnatEntities = new HashSet<>();
        expAnatEntities.addAll(Arrays.asList(
        		new AnatEntity("1", "Anat_id1", "desc1"),
        		new AnatEntity("2", "Anat_id2", "desc2"),
        		new AnatEntity("3", "Anat_id3", "desc3"),
        		new AnatEntity("4", "Anat_id4", "desc4"),
        		new AnatEntity("5", "Anat_id5", "desc5"),
        		new AnatEntity("9", "Anat_id9", "desc9"),
        		new AnatEntity("10", "Anat_id10", "desc10"),
        		new AnatEntity("11", "Anat_id11", "desc11")));
        
        Set<Ontology.RelationType> expRelationTypes = new HashSet<>();
        expRelationTypes.add(Ontology.RelationType.ISA_PARTOF);
        expRelationTypes.add(Ontology.RelationType.DEVELOPSFROM);

        Ontology<AnatEntity> expectedOntology = 
        		new Ontology<>(expAnatEntities, new HashSet<>(relationTOs), expRelationTypes);
        
        OntologyService service = new OntologyService(managerMock);
        AnatEntityService anatEntityService = new AnatEntityService(managerMock);

        assertEquals("Incorrect anatomical entity ontology",
                expectedOntology, service.getAnatEntityOntology(speciesIds, anatEntityIds,
                		expRelationTypes, true, true, anatEntityService));
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
        StageDAO stageDao = mock(StageDAO.class);
        when(managerMock.getStageDAO()).thenReturn(stageDao);
        
        Set<String> speciesIds = new HashSet<String>();
        speciesIds.addAll(Arrays.asList("11", "22", "44"));
        Set<String> stageIds = new HashSet<String>();
        stageIds.addAll(Arrays.asList("Stage_id1", "Stage_id2"));

        Set<String> sourceStageIds = new HashSet<String>();
        sourceStageIds.addAll(stageIds);
        Set<String> targetStageIds = null;
        
        boolean sourceOrTarget = true;
        
        Set<RelationTO.RelationType> daoRelationTypes = new HashSet<>();
        daoRelationTypes.add(RelationTO.RelationType.ISA_PARTOF);
        List<RelationTO> relationTOs = Arrays.asList(
        		new RelationTO("12", "Stage_id1", "Stage_id2", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
        		new RelationTO("13", "Stage_id2", "Stage_id3", RelationType.ISA_PARTOF, RelationStatus.INDIRECT));
        
        RelationTOResultSet mockRelationRs = getMockResultSet(RelationTOResultSet.class, relationTOs);
        when(relationDao.getStageRelations(speciesIds, true, sourceStageIds, targetStageIds,
        		sourceOrTarget, EnumSet.complementOf(EnumSet.of(RelationStatus.REFLEXIVE)), null))
        	.thenReturn(mockRelationRs);

        StageTOResultSet mockStageRs = getMockResultSet(StageTOResultSet.class, Arrays.asList(
        		new StageTO("1", "Stage_id1", "desc1", 0, 0, 0, false, false),
        		new StageTO("2", "Stage_id2", "desc2", 0, 0, 0, false, false),
        		new StageTO("3", "Stage_id3", "desc3", 0, 0, 0, false, false)));
        Set<String> expStageIds = new HashSet<String>();
        expStageIds.addAll(Arrays.asList("Stage_id1", "Stage_id2", "Stage_id3"));
        when(stageDao.getStages(speciesIds, true, expStageIds, null, null, null))
        	.thenReturn(mockStageRs);

        // TODO there is an error when leftBound of TO (Integer) is null
        // because NestedSetModelEntity contains int leftBound
        Set<DevStage> expDevStages= new HashSet<>();
        expDevStages.addAll(Arrays.asList(
        		new DevStage("1", "Stage_id1", "desc1", 0, 0, 0, false, false),
        		new DevStage("2", "Stage_id2", "desc2", 0, 0, 0, false, false),
        		new DevStage("3", "Stage_id3", "desc3", 0, 0, 0, false, false)));
        
        Set<Ontology.RelationType> expRelationTypes = new HashSet<>();
        expRelationTypes.add(Ontology.RelationType.ISA_PARTOF);

        Ontology<DevStage> expectedOntology = 
        		new Ontology<>(expDevStages, new HashSet<>(relationTOs), expRelationTypes);
        
        OntologyService service = new OntologyService(managerMock);
        DevStageService devStageService = new DevStageService(managerMock);

        assertEquals("Incorrect dev. stage ontology",
                expectedOntology, service.getDevStageOntology(speciesIds, stageIds,
                		true, false, devStageService));
    }
    
    //TODO: to remove when proper unit tests are implemented
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
        OntologyService service = fac.getOntologyService();
        
        Ontology<AnatEntity> anatOnt = service.getAnatEntityOntology(null, 
                Arrays.asList("UBERON:0000061", "UBERON:0003037", "FBbt:00017000", "UBERON:0000465"), 
                fac.getAnatEntityService());
        System.out.println(anatOnt.getAncestors(anatOnt.getElement("UBERON:0000061")));
        System.out.println(anatOnt.getDescendants(anatOnt.getElement("UBERON:0000061")));
        System.out.println(anatOnt.getDescendants(anatOnt.getElement("UBERON:0000465")));
        
        Ontology<DevStage> stageOnt = service.getDevStageOntology(null, 
                Arrays.asList("UBERON:0000104", "UBERON:0000068", "UBERON:0000106"), 
                fac.getDevStageService());
        System.out.println(stageOnt.getAncestors(stageOnt.getElement("UBERON:0000106")));
        System.out.println(stageOnt.getDescendants(stageOnt.getElement("UBERON:0000106")));
        System.out.println(stageOnt.getDescendants(stageOnt.getElement("UBERON:0000104")));
        System.out.println(stageOnt.getAncestors(stageOnt.getElement("UBERON:0000104")));
        
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
