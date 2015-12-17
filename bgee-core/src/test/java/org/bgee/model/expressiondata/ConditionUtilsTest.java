package org.bgee.model.expressiondata;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.ServiceFactory;
import org.bgee.model.TestAncestor;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.DevStageService;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.ontology.OntologyService;
import org.junit.Test;

/**
 * Unit tests for {@link ConditionUtils}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Dec. 2015
 * @since Bgee 13 Dec. 2015
 */
public class ConditionUtilsTest extends TestAncestor {
    private final static Logger log = LogManager.getLogger(ConditionUtilsTest.class.getName());
    
    @Override
    protected Logger getLogger() {
        return log;
    } 

    /**
     * Test the method {@link ConditionUtils#isConditionMorePrecise(Condition, Condition)}.
     */
    @Test
    public void testIsConditionMorePrecise() {
        String anatEntityId1 = "anat1";
        AnatEntity anatEntity1 = new AnatEntity(anatEntityId1);
        String anatEntityId2 = "anat2";
        AnatEntity anatEntity2 = new AnatEntity(anatEntityId2);
        String anatEntityId3 = "anat3";
        AnatEntity anatEntity3 = new AnatEntity(anatEntityId3);
        String devStageId1 = "stage1";
        DevStage devStage1 = new DevStage(devStageId1);
        String devStageId2 = "stage2";
        DevStage devStage2 = new DevStage(devStageId2);
        String devStageId3 = "stage3";
        DevStage devStage3 = new DevStage(devStageId3);
        
        Condition cond1 = new Condition(anatEntityId1, devStageId1);
        Condition cond2 = new Condition(anatEntityId2, devStageId2);
        Condition cond3 = new Condition(anatEntityId3, devStageId3);
        Condition cond4 = new Condition(anatEntityId2, devStageId1);
        Condition cond5 = new Condition(anatEntityId1, devStageId3);
        
        ServiceFactory mockFact = mock(ServiceFactory.class);
        OntologyService ontService = mock(OntologyService.class);
        AnatEntityService anatEntityService = mock(AnatEntityService.class);
        DevStageService devStageService = mock(DevStageService.class);
        when(mockFact.getOntologyService()).thenReturn(ontService);
        when(mockFact.getAnatEntityService()).thenReturn(anatEntityService);
        when(mockFact.getDevStageService()).thenReturn(devStageService);
        
        //suppress warning as we cannot specify generic type for a mock
        @SuppressWarnings("unchecked")
        Ontology<AnatEntity> anatEntityOnt = mock(Ontology.class);
        @SuppressWarnings("unchecked")
        Ontology<DevStage> devStageOnt = mock(Ontology.class);
        
        when(ontService.getAnatEntityOntology(
                new HashSet<>(Arrays.asList(anatEntityId1, anatEntityId2, anatEntityId3)), 
                anatEntityService))
        .thenReturn(anatEntityOnt);
        when(ontService.getDevStageOntology(
                new HashSet<>(Arrays.asList(devStageId1, devStageId2, devStageId3)), 
                devStageService))
        .thenReturn(devStageOnt);
        
        when(anatEntityOnt.getElement(anatEntityId1)).thenReturn(anatEntity1);
        when(anatEntityOnt.getElement(anatEntityId2)).thenReturn(anatEntity2);
        when(anatEntityOnt.getElement(anatEntityId3)).thenReturn(anatEntity3);
        when(devStageOnt.getElement(devStageId1)).thenReturn(devStage1);
        when(devStageOnt.getElement(devStageId2)).thenReturn(devStage2);
        when(devStageOnt.getElement(devStageId3)).thenReturn(devStage3);
        
        when(anatEntityOnt.getAncestors(anatEntity1)).thenReturn(new HashSet<>());
        when(anatEntityOnt.getAncestors(anatEntity2)).thenReturn(new HashSet<>(Arrays.asList(anatEntity1)));
        when(anatEntityOnt.getAncestors(anatEntity3)).thenReturn(new HashSet<>(Arrays.asList(anatEntity1)));
        when(devStageOnt.getAncestors(devStage1)).thenReturn(new HashSet<>());
        when(devStageOnt.getAncestors(devStage2)).thenReturn(new HashSet<>(Arrays.asList(devStage1)));
        when(devStageOnt.getAncestors(devStage3)).thenReturn(new HashSet<>(Arrays.asList(devStage1)));
        
        ConditionUtils utils = new ConditionUtils(Arrays.asList(cond1, cond2, cond3, cond4, cond5), 
                mockFact);
        assertTrue("Incorrect determination of precision for more precise condition", 
                utils.isConditionMorePrecise(cond1, cond2));
        assertTrue("Incorrect determination of precision for more precise condition", 
                utils.isConditionMorePrecise(cond1, cond3));
        assertFalse("Incorrect determination of precision for less precise condition", 
                utils.isConditionMorePrecise(cond2, cond1));
        assertFalse("Incorrect determination of precision for less precise condition", 
                utils.isConditionMorePrecise(cond3, cond1));
        assertFalse("Incorrect determination of precision for as precise conditions", 
                utils.isConditionMorePrecise(cond2, cond3));

        assertFalse("Incorrect determination of precision for condition with anat. entity as precise", 
                utils.isConditionMorePrecise(cond1, cond5));
        assertFalse("Incorrect determination of precision for condition with dev. stage as precise", 
                utils.isConditionMorePrecise(cond1, cond4));
        
        assertEquals("Incorrect AnatEntity retrieved", anatEntity1, utils.getAnatEntity(anatEntityId1));
        assertEquals("Incorrect DevStage retrieved", devStage1, utils.getDevStage(devStageId1));
        
        //check that an Exception is correctly thrown if a condition used was not provided at instantiation
        try {
            utils.isConditionMorePrecise(cond1, new Condition(anatEntityId1, devStageId2));
            //test fail
            fail("An exception should be thrown when a Condition was not provided at instantiation.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        
    }
}
