package org.bgee.model.ontology;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.bgee.model.ServiceFactory;
import org.bgee.model.TestAncestor;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
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
 * @version Bgee 13, Dec. 2015
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
        RelationDAO dao = mock(RelationDAO.class);
        when(managerMock.getRelationDAO()).thenReturn(dao);
        
        RelationTOResultSet mockRelationRs = getMockResultSet(RelationTOResultSet.class, Arrays.asList(
        		new RelationTO("22", "Anat_id10", "Anat_id5", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
        		new RelationTO("23", "Anat_id11", "Anat_id10", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
        		new RelationTO("12", "Anat_id2", "Anat_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
        		new RelationTO("13", "Anat_id3", "Anat_id2", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
        		new RelationTO("14", "Anat_id4", "Anat_id2", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
        		new RelationTO("15", "Anat_id5", "Anat_id2", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
        		new RelationTO("16", "Anat_id5", "Anat_id7", RelationType.DEVELOPSFROM, RelationStatus.INDIRECT),
        		new RelationTO("17", "Anat_id5", "Anat_id8", RelationType.DEVELOPSFROM, RelationStatus.DIRECT),
        		new RelationTO("18", "Anat_id6", "Anat_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
        		new RelationTO("19", "Anat_id7", "Anat_id6", RelationType.DEVELOPSFROM, RelationStatus.DIRECT),
        		new RelationTO("20", "Anat_id8", "Anat_id7", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
        		new RelationTO("21", "Anat_id9", "Anat_id5", RelationType.ISA_PARTOF, RelationStatus.DIRECT)));
        
        Set<String> speciesIds = new HashSet<String>();
        speciesIds.addAll(Arrays.asList("11", "22", "44"));
        
//        when(dao.getAnatEntityRelations(speciesIds, true, sourceAnatEntityIds, targetAnatEntityIds,
//                       sourceOrTarget, relationTypes, EnumSet.complementOf(EnumSet.of(RelationStatus.REFLEXIVE)), null)).thenReturn(mockRelationRs);
//      relationTypes.stream()
//      .map(Ontology::convertRelationType)
//      .collect(Collectors.toCollection(() -> 
//          EnumSet.noneOf(RelationTO.RelationType.class))), 

//        Ontology<AnatEntity> expectedOntology = new Ontology<AnatEntity>(anatEntities, relations, relationTypes);
//        
//        OntologyService service = new OntologyService(managerMock);
//        assertEquals("Incorrect anatomical ontology",
//                expectedOntology, service.getAnatEntityOntology(speciesIds, anatEntityIds, 
//                               relationTypes, getAncestors, getDescendants, service));
     
    }

    /**
     * Test the method 
     * {@link OntologyService#getDevStageOntology(Collection, boolean, boolean, DevStageService)}.
     */
    @Test
    public void shouldGetStageOntology() {
        
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
