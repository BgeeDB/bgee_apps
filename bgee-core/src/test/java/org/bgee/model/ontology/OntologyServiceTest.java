package org.bgee.model.ontology;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Properties;

import org.bgee.model.ServiceFactory;
import org.bgee.model.TestAncestor;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.dao.api.DAOManager;
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
        
        Ontology<AnatEntity> anatOnt = service.getAnatEntityOntology(
                Arrays.asList("UBERON:0000061", "UBERON:0003037", "FBbt:00017000", "UBERON:0000465"), 
                fac.getAnatEntityService());
        System.out.println(anatOnt.getAncestors(anatOnt.getElement("UBERON:0000061")));
        System.out.println(anatOnt.getDescendants(anatOnt.getElement("UBERON:0000061")));
        System.out.println(anatOnt.getDescendants(anatOnt.getElement("UBERON:0000465")));
        
        Ontology<DevStage> stageOnt = service.getDevStageOntology(
                Arrays.asList("UBERON:0000104", "UBERON:0000068", "UBERON:0000106"), 
                fac.getDevStageService());
        System.out.println(stageOnt.getAncestors(stageOnt.getElement("UBERON:0000106")));
        System.out.println(stageOnt.getDescendants(stageOnt.getElement("UBERON:0000106")));
        System.out.println(stageOnt.getDescendants(stageOnt.getElement("UBERON:0000104")));
        System.out.println(stageOnt.getAncestors(stageOnt.getElement("UBERON:0000104")));
        
        Condition cond1 = new Condition("UBERON:0000465", "UBERON:0000104");
        Condition cond2 = new Condition("UBERON:0000061", "UBERON:0000106");
        ConditionUtils utils = new ConditionUtils(Arrays.asList(cond1, cond2), fac);
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
