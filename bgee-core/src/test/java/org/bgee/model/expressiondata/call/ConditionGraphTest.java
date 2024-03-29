package org.bgee.model.expressiondata.call;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.ServiceFactory;
import org.bgee.model.TestAncestor;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.DevStageService;
import org.bgee.model.ontology.OntologyService;
import org.bgee.model.ontology.RelationType;
import org.bgee.model.species.Species;
import org.bgee.model.ontology.Ontology;
import org.junit.Test;

/**
 * Unit tests for {@link ConditionGraph}.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, May 2017
 * @since   Bgee 13, Dec. 2015
 */
public class ConditionGraphTest extends TestAncestor {
    private final static Logger log = LogManager.getLogger(ConditionGraphTest.class.getName());
    
    @Override
    protected Logger getLogger() {
        return log;
    } 
    
    private ConditionGraph conditionGraph;
    private List<Condition> conditions;

    /**
     * @param fromService   A {@code boolean} {@code true} if the {@code ConditionGraph} 
     *                      should be loaded from the {@code ServiceFactory}, 
     *                      {@code false} if it should be loaded directly from {@code Ontology}s.
     */
    private void loadConditionGraph(boolean fromService) {
        //            anat1
        //           /  \
        //      anat2  MISSING (test graph reconnection for deleted conditions)
        //                \
        //                anat4
        //
        //            stage1
        //           /  \   \
        //      stage2 stage3\
        //                \   \
        //                stage4
        String anatEntityId1 = "anat1";
        AnatEntity anatEntity1 = new AnatEntity(anatEntityId1);
        String anatEntityId2 = "anat2";
        AnatEntity anatEntity2 = new AnatEntity(anatEntityId2);
        String anatEntityId4 = "anat4";
        AnatEntity anatEntity4 = new AnatEntity(anatEntityId4);
        String devStageId1 = "stage1";
        DevStage devStage1 = new DevStage(devStageId1);
        String devStageId2 = "stage2";
        DevStage devStage2 = new DevStage(devStageId2);
        String devStageId3 = "stage3";
        DevStage devStage3 = new DevStage(devStageId3);
        String devStageId4 = "stage4";
        DevStage devStage4 = new DevStage(devStageId4);

        Species sp = new Species(9606);
        Condition cond1 = new Condition(anatEntity1, devStage1, null, null, null, sp);
        Condition cond2 = new Condition(anatEntity2, devStage2, null, null, null, sp);
        Condition cond4 = new Condition(anatEntity2, devStage1, null, null, null, sp);
        Condition cond5 = new Condition(anatEntity1, devStage3, null, null, null, sp);
        Condition cond6 = new Condition(anatEntity2, devStage3, null, null, null, sp);
        Condition cond8 = new Condition(anatEntity4, devStage4, null, null, null, sp);
        Condition cond1_anatOnly = new Condition(anatEntity1, null, null, null, null, sp);
        Condition cond2_anatOnly = new Condition(anatEntity2, null, null, null, null, sp);
        Condition cond1_stageOnly = new Condition(null, devStage1, null, null, null, sp);
        Condition cond3_stageOnly = new Condition(null, devStage3, null, null, null, sp);
        this.conditions = Arrays.asList(cond1, cond2, cond4, cond5, cond6, cond8,
                cond1_anatOnly, cond2_anatOnly, cond1_stageOnly, cond3_stageOnly);
        
        ServiceFactory mockFact = mock(ServiceFactory.class);
        OntologyService ontService = mock(OntologyService.class);
        AnatEntityService anatEntityService = mock(AnatEntityService.class);
        DevStageService devStageService = mock(DevStageService.class);
        when(mockFact.getOntologyService()).thenReturn(ontService);
        when(mockFact.getAnatEntityService()).thenReturn(anatEntityService);
        when(mockFact.getDevStageService()).thenReturn(devStageService);
        
        //suppress warning as we cannot specify generic type for a mock
        @SuppressWarnings("unchecked")
        Ontology<AnatEntity, String> anatEntityOnt = mock(Ontology.class);
        @SuppressWarnings("unchecked")
        Ontology<DevStage, String> devStageOnt = mock(Ontology.class);
        
        when(ontService.getAnatEntityOntology(9606, new HashSet<>(Arrays.asList(
                anatEntityId1, anatEntityId2, anatEntityId4)),
                EnumSet.of(RelationType.ISA_PARTOF), false, false))
        .thenReturn(anatEntityOnt);
        when(ontService.getDevStageOntology(9606, new HashSet<>(Arrays.asList(
                devStageId1, devStageId2, devStageId3, devStageId4)), false, false))
        .thenReturn(devStageOnt);
        
        when(anatEntityOnt.getElements()).thenReturn(
                new HashSet<>(Arrays.asList(anatEntity1, anatEntity2, anatEntity4)));
        when(anatEntityOnt.getElement(anatEntityId1)).thenReturn(anatEntity1);
        when(anatEntityOnt.getElement(anatEntityId2)).thenReturn(anatEntity2);
        when(anatEntityOnt.getElement(anatEntityId4)).thenReturn(anatEntity4);
        when(devStageOnt.getElements()).thenReturn(
                new HashSet<>(Arrays.asList(devStage1, devStage2, devStage3, devStage4)));
        when(devStageOnt.getElement(devStageId1)).thenReturn(devStage1);
        when(devStageOnt.getElement(devStageId2)).thenReturn(devStage2);
        when(devStageOnt.getElement(devStageId3)).thenReturn(devStage3);
        when(devStageOnt.getElement(devStageId4)).thenReturn(devStage4);
        
        when(anatEntityOnt.getAncestors(anatEntity1)).thenReturn(new HashSet<>());
        when(anatEntityOnt.getAncestors(anatEntity2)).thenReturn(new HashSet<>(Arrays.asList(anatEntity1)));
        when(anatEntityOnt.getAncestors(anatEntity4)).thenReturn(new HashSet<>(Arrays.asList(anatEntity1)));
        when(devStageOnt.getAncestors(devStage1)).thenReturn(new HashSet<>());
        when(devStageOnt.getAncestors(devStage2)).thenReturn(new HashSet<>(Arrays.asList(devStage1)));
        when(devStageOnt.getAncestors(devStage3)).thenReturn(new HashSet<>(Arrays.asList(devStage1)));
        when(devStageOnt.getAncestors(devStage4)).thenReturn(new HashSet<>(Arrays.asList(devStage1, devStage3)));

        when(anatEntityOnt.getAncestors(anatEntity1, false)).thenReturn(new HashSet<>());
        when(anatEntityOnt.getAncestors(anatEntity2, false)).thenReturn(new HashSet<>(Arrays.asList(anatEntity1)));
        when(anatEntityOnt.getAncestors(anatEntity4, false)).thenReturn(new HashSet<>(Arrays.asList(anatEntity1)));
        when(devStageOnt.getAncestors(devStage1, false)).thenReturn(new HashSet<>());
        when(devStageOnt.getAncestors(devStage2, false)).thenReturn(new HashSet<>(Arrays.asList(devStage1)));
        when(devStageOnt.getAncestors(devStage3, false)).thenReturn(new HashSet<>(Arrays.asList(devStage1)));
        when(devStageOnt.getAncestors(devStage4, false)).thenReturn(new HashSet<>(Arrays.asList(devStage1, devStage3)));
        
        when(anatEntityOnt.getDescendants(anatEntity1, false)).thenReturn(
                new HashSet<>(Arrays.asList(anatEntity2, anatEntity4)));
        // We should not propagate to dev. stages child conditions
        when(devStageOnt.getDescendants(devStage1, false)).thenReturn(new HashSet<>());

        ConditionGraphService condGraphService = new ConditionGraphService(mockFact);
        if (fromService) {
            this.conditionGraph = condGraphService.loadConditionGraph(this.conditions);
        } else {
            this.conditionGraph = condGraphService.loadConditionGraph(this.conditions, anatEntityOnt, devStageOnt, null, null, null);
        }
    }

    /**
     * Test the method {@link ConditionGraph#isConditionMorePrecise(Condition, Condition)}.
     */
    @Test
    public void testIsConditionMorePreciseDifferentLoadings() {
        this.loadConditionGraph(true);
        this.testIsConditionMorePrecise();
        this.loadConditionGraph(false);
        this.testIsConditionMorePrecise();
    }
    /**
     * Test the method {@link ConditionGraph#isConditionMorePrecise(Condition, Condition)}.
     */
    private void testIsConditionMorePrecise() {
        assertTrue("Incorrect determination of precision for more precise condition", 
                this.conditionGraph.isConditionMorePrecise(this.conditions.get(0), this.conditions.get(1)));
        assertTrue("Incorrect determination of precision for more precise condition", 
                this.conditionGraph.isConditionMorePrecise(this.conditions.get(6), this.conditions.get(7)));
        assertFalse("Incorrect determination of precision for less precise condition", 
                this.conditionGraph.isConditionMorePrecise(this.conditions.get(1), this.conditions.get(0)));
        assertFalse("Incorrect determination of precision for less precise condition", 
                this.conditionGraph.isConditionMorePrecise(this.conditions.get(7), this.conditions.get(6)));
        assertTrue("Incorrect determination of precision for more precise condition", 
                this.conditionGraph.isConditionMorePrecise(this.conditions.get(8), this.conditions.get(9)));
        assertFalse("Incorrect determination of precision for less precise condition", 
                this.conditionGraph.isConditionMorePrecise(this.conditions.get(9), this.conditions.get(8)));
        assertFalse("Incorrect determination of precision for less precise condition", 
                this.conditionGraph.isConditionMorePrecise(this.conditions.get(6), this.conditions.get(9)));

        assertFalse("Incorrect determination of precision for condition with anat. entity as precise", 
                this.conditionGraph.isConditionMorePrecise(this.conditions.get(4), this.conditions.get(1)));
        assertTrue("Incorrect determination of precision for condition with anat. entity as precise", 
                this.conditionGraph.isConditionMorePrecise(this.conditions.get(0), this.conditions.get(3)));
        assertFalse("Incorrect determination of precision for condition with dev. stage as precise", 
                this.conditionGraph.isConditionMorePrecise(this.conditions.get(1), this.conditions.get(1)));
        assertTrue("Incorrect determination of precision for condition with dev. stage as precise", 
                this.conditionGraph.isConditionMorePrecise(this.conditions.get(0), this.conditions.get(2)));
        
        AnatEntity anatEntity1 = new AnatEntity(this.conditions.get(0).getAnatEntityId());
        assertEquals("Incorrect AnatEntity retrieved", anatEntity1, this.conditionGraph.getAnatEntityOntology().getElement(
                this.conditions.get(0).getAnatEntityId()));
        DevStage devStage1 = new DevStage(this.conditions.get(0).getDevStageId());
        assertEquals("Incorrect DevStage retrieved", devStage1, this.conditionGraph.getDevStageOntology().getElement(
                this.conditions.get(0).getDevStageId()));
        
        //check that an Exception is correctly thrown if a condition used was not provided at instantiation
        try {
            this.conditionGraph.isConditionMorePrecise(this.conditions.get(0), 
                    new Condition(new AnatEntity("test1"), new DevStage("test2"), null, null, null, new Species(3)));
            //test fail
            fail("An exception should be thrown when a Condition was not provided at instantiation.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        
    }

    /**
     * Test the method {@link ConditionGraph#getDescendantConditions(Condition)}.
     */
    @Test
    public void shouldGetDescendantConditionsDifferentLoadings() {
        this.loadConditionGraph(true);
        this.shouldGetDescendantConditions();
        this.loadConditionGraph(false);
        this.shouldGetDescendantConditions();
    }
    /**
     * Test the method {@link ConditionGraph#getDescendantConditions(Condition)}.
     */
    private void shouldGetDescendantConditions() {
        Set<Condition> expectedDescendants = new HashSet<>(Arrays.asList(this.conditions.get(2)));
        assertEquals("Incorrect descendants retrieved", expectedDescendants, 
                this.conditionGraph.getDescendantConditions(this.conditions.get(0)));
        
        expectedDescendants = new HashSet<>();
        assertEquals("Incorrect descendants retrieved", expectedDescendants, 
                this.conditionGraph.getDescendantConditions(this.conditions.get(2)));
        
        expectedDescendants = new HashSet<>(Arrays.asList(this.conditions.get(4)));
        assertEquals("Incorrect descendants retrieved", expectedDescendants, 
                this.conditionGraph.getDescendantConditions(this.conditions.get(3)));
        
        expectedDescendants = new HashSet<>(Arrays.asList(this.conditions.get(7)));
        assertEquals("Incorrect descendants retrieved", expectedDescendants, 
                this.conditionGraph.getDescendantConditions(this.conditions.get(6)));
        
        expectedDescendants = new HashSet<>();
        assertEquals("Incorrect descendants retrieved", expectedDescendants, 
                this.conditionGraph.getDescendantConditions(this.conditions.get(8)));
    }

    /**
     * Test the method {@link ConditionGraph#getAncestorConditions(Condition, boolean)}.
     */
    @Test
    public void shouldGetAncestorConditionsDifferentLoadings() {
        this.loadConditionGraph(true);
        this.shouldGetAncestorConditions();
        this.loadConditionGraph(false);
        this.shouldGetAncestorConditions();
    }
    /**
     * Test the method {@link ConditionGraph#getAncestorConditions(Condition, boolean)}.
     */
    private void shouldGetAncestorConditions() {
        Set<Condition> expectedAncestors = new HashSet<>(Arrays.asList(this.conditions.get(0)));
        assertEquals("Incorrect ancestors retrieved", expectedAncestors, 
                this.conditionGraph.getAncestorConditions(this.conditions.get(2), false));

        expectedAncestors = new HashSet<>();
        assertEquals("Incorrect ancestors retrieved", expectedAncestors, 
                this.conditionGraph.getAncestorConditions(this.conditions.get(0), false));
        
        expectedAncestors = new HashSet<>(Arrays.asList(
                this.conditions.get(0), this.conditions.get(3)));
        assertEquals("Incorrect ancestors retrieved", expectedAncestors, 
                this.conditionGraph.getAncestorConditions(this.conditions.get(5), false));

        //Test graph reconnection for deleted conditions
        expectedAncestors = new HashSet<>(Arrays.asList(
                this.conditions.get(3)));
        assertEquals("Incorrect ancestors retrieved", expectedAncestors,
                this.conditionGraph.getAncestorConditions(this.conditions.get(5), true));
    }
}
