package org.bgee.model.expressiondata;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.TestAncestor;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.DevStageService;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.expressiondata.CallDAOFilter;
import org.bgee.model.dao.api.expressiondata.CallDataDAOFilter;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.ConditionDAO.ConditionTO;
import org.bgee.model.dao.api.expressiondata.ConditionDAO.ConditionTOResultSet;
import org.bgee.model.dao.api.expressiondata.ConditionDAO.GlobalConditionMaxRankTO;
import org.bgee.model.dao.api.expressiondata.DAOConditionFilter;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.DAOExperimentCount;
import org.bgee.model.dao.api.expressiondata.DAOExperimentCountFilter;
import org.bgee.model.dao.api.expressiondata.DAOPropagationState;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO.GlobalExpressionCallDataTO;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO.GlobalExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO.GlobalExpressionCallTOResultSet;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneBioTypeTOResultSet;
import org.bgee.model.dao.api.gene.GeneDAO.GeneBioTypeTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTOResultSet;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.CallService.Attribute;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.CallType.Expression;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.ExperimentExpressionCount;
import org.bgee.model.expressiondata.baseelements.PropagationState;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.ontology.OntologyService;
import org.bgee.model.ontology.RelationType;
import org.bgee.model.species.Species;
import org.bgee.model.species.SpeciesService;
import org.junit.Test;

/**
 * Unit tests for {@link CallService}.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @author  Julien Wollbrett
 * @version Bgee 14, Aug. 2018
 * @since   Bgee 13, Nov. 2015
 */
public class CallServiceTest extends TestAncestor {
    
    private final static Logger log = LogManager.getLogger(CallServiceTest.class.getName());
        
    @Override
    protected Logger getLogger() {
        return log;
    }

    private void configureMockGeneDAOForBioType(GeneDAO geneDAO) {
        getLogger().entry(geneDAO);
        GeneBioTypeTOResultSet geneBioTypeTOResultSet = getMockResultSet(GeneBioTypeTOResultSet.class,
                Arrays.asList(new GeneBioTypeTO(1, "b")));
        when(geneDAO.getGeneBioTypes()).thenReturn(geneBioTypeTOResultSet);
        getLogger().exit();
    }
    
    /**
     * Test the method {@link CallService#loadExpressionCalls(String, ExpressionCallFilter, 
     * Collection, LinkedHashMap, boolean)}.
     */
    // Keep in this test only one gene: it allows to detect if only one iteration is possible
    @Test
    public void shouldLoadExpressionCallsForBasicGene() {
        //First test for one gene, with sub-stages but without substructures. 
        //Retrieving all attributes, ordered by mean rank. 
        DAOManager manager = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        
        when(serviceFactory.getDAOManager()).thenReturn(manager);
        GlobalExpressionCallDAO dao = mock(GlobalExpressionCallDAO.class);
        when(manager.getGlobalExpressionCallDAO()).thenReturn(dao);
        ConditionDAO condDAO = mock(ConditionDAO.class);
        when(manager.getConditionDAO()).thenReturn(condDAO);
        GeneDAO geneDAO = mock(GeneDAO.class);
        when(manager.getGeneDAO()).thenReturn(geneDAO);
        SpeciesService speciesService = mock(SpeciesService.class);
        when(serviceFactory.getSpeciesService()).thenReturn(speciesService);
        
        Species spe1 = new Species(1);
        Integer bgeeGeneId1 = 1;
        Integer cond1Id = 1;
        Integer cond2Id = 2;
        Integer cond3Id = 3;
        Map<ConditionDAO.Attribute, DAOPropagationState> dataPropagation = new HashMap<>();
        dataPropagation.put(ConditionDAO.Attribute.ANAT_ENTITY_ID, DAOPropagationState.SELF);
        dataPropagation.put(ConditionDAO.Attribute.STAGE_ID, DAOPropagationState.SELF);
        
        
        GlobalExpressionCallTOResultSet resultSetMock = getMockResultSet(GlobalExpressionCallTOResultSet.class, 
                Arrays.asList(
                        // To not overload tests, we put null for not used attributes 
                        // but, real query return all attributes
                    new GlobalExpressionCallTO(1, bgeeGeneId1, cond1Id, 
                        new BigDecimal(1257.34), new HashSet<>(Arrays.asList(
                                new GlobalExpressionCallDataTO(DAODataType.AFFYMETRIX, true, 
                                        dataPropagation , new HashSet<>(Arrays.asList(
                                               new DAOExperimentCount(
                                                       DAOExperimentCount.CallType.PRESENT, 
                                                       DAOExperimentCount.DataQuality.LOW, 
                                                       DAOPropagationState.SELF, 1),
                                               new DAOExperimentCount(
                                                       DAOExperimentCount.CallType.PRESENT, 
                                                       DAOExperimentCount.DataQuality.LOW, 
                                                       DAOPropagationState.ALL, 1))), 
                                        0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77)),
                                new GlobalExpressionCallDataTO(DAODataType.EST, true, 
                                        dataPropagation , new HashSet<>(Arrays.asList(
                                                new DAOExperimentCount(
                                                        DAOExperimentCount.CallType.PRESENT, 
                                                        DAOExperimentCount.DataQuality.HIGH, 
                                                        DAOPropagationState.SELF, 2),
                                                new DAOExperimentCount(
                                                        DAOExperimentCount.CallType.PRESENT, 
                                                        DAOExperimentCount.DataQuality.LOW, 
                                                        DAOPropagationState.SELF, 1),
                                                new DAOExperimentCount(
                                                        DAOExperimentCount.CallType.PRESENT, 
                                                        DAOExperimentCount.DataQuality.HIGH, 
                                                        DAOPropagationState.ALL, 2),
                                                new DAOExperimentCount(
                                                        DAOExperimentCount.CallType.PRESENT, 
                                                        DAOExperimentCount.DataQuality.LOW, 
                                                        DAOPropagationState.ALL, 1))), 
                                        0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77))))),
                   
                    new GlobalExpressionCallTO(2, bgeeGeneId1, cond2Id, 
                        new BigDecimal(125.00), new HashSet<>(Arrays.asList(
                                new GlobalExpressionCallDataTO(DAODataType.AFFYMETRIX, true, 
                                        dataPropagation, new HashSet<>(Arrays.asList(
                                                new DAOExperimentCount(
                                                        DAOExperimentCount.CallType.PRESENT, 
                                                        DAOExperimentCount.DataQuality.HIGH, 
                                                        DAOPropagationState.ALL, 3),
                                                new DAOExperimentCount(
                                                        DAOExperimentCount.CallType.PRESENT, 
                                                        DAOExperimentCount.DataQuality.LOW, 
                                                        DAOPropagationState.ALL, 1))), 
                                        0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77)),
                                new GlobalExpressionCallDataTO(DAODataType.EST, true, 
                                        dataPropagation, new HashSet<>(Arrays.asList(
                                                new DAOExperimentCount(
                                                        DAOExperimentCount.CallType.PRESENT, 
                                                        DAOExperimentCount.DataQuality.LOW,
                                                        DAOPropagationState.SELF, 1),
                                                new DAOExperimentCount(
                                                        DAOExperimentCount.CallType.PRESENT, 
                                                        DAOExperimentCount.DataQuality.LOW, 
                                                        DAOPropagationState.ALL, 1))), 
                                        0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77)),
                                new GlobalExpressionCallDataTO(DAODataType.IN_SITU, true, 
                                        dataPropagation, new HashSet<>(Arrays.asList(
                                                new DAOExperimentCount(
                                                        DAOExperimentCount.CallType.PRESENT, 
                                                        DAOExperimentCount.DataQuality.LOW, 
                                                        DAOPropagationState.SELF, 1),
                                                new DAOExperimentCount(
                                                        DAOExperimentCount.CallType.PRESENT, 
                                                        DAOExperimentCount.DataQuality.LOW, 
                                                        DAOPropagationState.ALL, 1))), 
                                        0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77))))),
                    new GlobalExpressionCallTO(3, bgeeGeneId1, cond3Id, 
                            new BigDecimal(125.00), new HashSet<>(Arrays.asList(
                                    new GlobalExpressionCallDataTO(DAODataType.AFFYMETRIX, true, 
                                            dataPropagation, new HashSet<>(Arrays.asList(
                                                    new DAOExperimentCount(
                                                            DAOExperimentCount.CallType.PRESENT, 
                                                            DAOExperimentCount.DataQuality.LOW, 
                                                            DAOPropagationState.ALL, 1))), 
                                            0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77)),
                                    new GlobalExpressionCallDataTO(DAODataType.EST, true,
                                            dataPropagation, new HashSet<>(Arrays.asList(
                                                    new DAOExperimentCount(
                                                            DAOExperimentCount.CallType.PRESENT, 
                                                            DAOExperimentCount.DataQuality.LOW, 
                                                            DAOPropagationState.ALL, 1))), 
                                            0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77)),
                                    new GlobalExpressionCallDataTO(DAODataType.IN_SITU, true, 
                                            dataPropagation, new HashSet<>(Arrays.asList(
                                                    new DAOExperimentCount(
                                                            DAOExperimentCount.CallType.PRESENT, 
                                                            DAOExperimentCount.DataQuality.LOW, 
                                                            DAOPropagationState.ALL, 1))), 
                                            0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77)))))
                    
                    ));

        
        //we'll do the verify afterwards, it's easier to catch a problem in the parameters
        when(dao.getGlobalExpressionCalls(
                // CallDAOFilters
                anyCollectionOf(CallDAOFilter.class), 
                // condition parameters
                anyCollectionOf(ConditionDAO.Attribute.class),
                // global expression attributes
                anyCollectionOf(GlobalExpressionCallDAO.Attribute.class),
                // ordering attributes
                anyObject()))
        .thenReturn(resultSetMock);
        
        OntologyService ontService = mock(OntologyService.class);
        AnatEntityService anatEntityService = mock(AnatEntityService.class);
        DevStageService devStageService = mock(DevStageService.class);
        when(serviceFactory.getOntologyService()).thenReturn(ontService);
        when(serviceFactory.getAnatEntityService()).thenReturn(anatEntityService);
        when(serviceFactory.getDevStageService()).thenReturn(devStageService);
        //suppress warning as we cannot specify generic type for a mock
        @SuppressWarnings("unchecked")
        Ontology<AnatEntity, String> anatEntityOnt = mock(Ontology.class);
        //suppress warning as we cannot specify generic type for a mock
        @SuppressWarnings("unchecked")
        Ontology<DevStage, String> devStageOnt = mock(Ontology.class);

        when(ontService.getAnatEntityOntology(spe1.getId(), new HashSet<>(Arrays.asList(
                "anatEntityId1")), EnumSet.of(RelationType.ISA_PARTOF), true, false))
        .thenReturn(anatEntityOnt);
        when(ontService.getDevStageOntology(spe1.getId(), new HashSet<>(Arrays.asList(
                "stageId1", "stageId2")), true, false)).thenReturn(devStageOnt);
        String anatEntityId1 = "anatEntityId1";
        AnatEntity anatEntity1 = new AnatEntity(anatEntityId1);
        String stageId1 = "stageId1";
        DevStage stage1 = new DevStage(stageId1);
        String stageId2 = "stageId2";
        DevStage stage2 = new DevStage(stageId2);
        String stageId3 = "stageId3";
        DevStage stage3 = new DevStage(stageId3);
        Gene g1 = new Gene("geneId1", spe1, new GeneBioType("b"));
        
        when(anatEntityService.loadAnatEntities(Collections.singleton(spe1.getId()), 
                true, new HashSet<String>(Arrays.asList(anatEntity1.getId())),false))
        .thenReturn(Stream.of(anatEntity1));
        when(devStageService.loadDevStages(Collections.singleton(spe1.getId()), 
                true, new HashSet<String>(Arrays.asList(
                        stage1.getId(), stage2.getId(), stage3.getId())),false))
        .thenReturn(Stream.of(stage1, stage2, stage3));

        when(anatEntityOnt.getElements()).thenReturn(new HashSet<>(Arrays.asList(anatEntity1)));
        when(devStageOnt.getElements()).thenReturn(new HashSet<>(Arrays.asList(stage1, stage2, stage3)));
        when(anatEntityOnt.getElement(anatEntityId1)).thenReturn(anatEntity1);
        when(devStageOnt.getElement(stageId1)).thenReturn(stage1);
        when(devStageOnt.getElement(stageId2)).thenReturn(stage2);
        when(devStageOnt.getElement(stageId3)).thenReturn(stage3);
        when(anatEntityOnt.getAncestors(anatEntity1)).thenReturn(new HashSet<>());
        when(devStageOnt.getAncestors(stage1)).thenReturn(new HashSet<>(Arrays.asList(stage2, stage3)));
        when(devStageOnt.getAncestors(stage2)).thenReturn(new HashSet<>(Arrays.asList(stage3)));
        when(devStageOnt.getAncestors(stage3)).thenReturn(new HashSet<>());
        when(anatEntityOnt.getAncestors(anatEntity1, false)).thenReturn(new HashSet<>());
        when(devStageOnt.getAncestors(stage1, false)).thenReturn(new HashSet<>(Arrays.asList(stage2, stage3)));
        when(devStageOnt.getAncestors(stage2, false)).thenReturn(new HashSet<>(Arrays.asList(stage3)));
        when(devStageOnt.getAncestors(stage3, false)).thenReturn(new HashSet<>());
        
        Map<Integer, Species> speciesById = new HashMap<>();
        speciesById.put(spe1.getId(), spe1);
        when(speciesService.loadSpeciesMap(new HashSet<>(Arrays.asList(1)), false)).thenReturn(speciesById);
        
        GlobalConditionMaxRankTO maxRankTO = new GlobalConditionMaxRankTO(new BigDecimal(100),new BigDecimal(100));
        when(condDAO.getMaxRank()).thenReturn(maxRankTO);
        ConditionTOResultSet condTOResultSet = getMockResultSet(ConditionTOResultSet.class, Arrays.asList(
                new ConditionTO(1, anatEntity1.getId(), stage1.getId(), spe1.getId()),
                new ConditionTO(2, anatEntity1.getId(), stage2.getId(), spe1.getId()),
                new ConditionTO(3, anatEntity1.getId(), stage3.getId(), spe1.getId())));
        when(condDAO.getGlobalConditionsBySpeciesIds(eq(Collections.singleton(spe1.getId())), 
                eq(new HashSet<>(Arrays.asList(ConditionDAO.Attribute.ANAT_ENTITY_ID, 
                        ConditionDAO.Attribute.STAGE_ID))),
                anyObject())).thenReturn(condTOResultSet);
        
        GeneTOResultSet geneTOResultSet = getMockResultSet(GeneTOResultSet.class, Arrays.asList(
                new GeneTO(1, g1.getEnsemblGeneId(), g1.getName(), g1.getDescription(), g1.getSpecies().getId(), 
                        1, 1, true, 1)));
        Map<Integer, Set<String>> speciesIdToGeneIds = new HashMap<>();
        speciesIdToGeneIds.put(spe1.getId(), Collections.singleton(g1.getEnsemblGeneId()));
        when(geneDAO.getGenesBySpeciesAndGeneIds(speciesIdToGeneIds)).thenReturn(geneTOResultSet);
        configureMockGeneDAOForBioType(geneDAO);


        List<ExpressionCall> expectedResults = Arrays.asList(
                new ExpressionCall(g1 , new Condition(anatEntity1, stage1, spe1), 
                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true),
                        ExpressionSummary.EXPRESSED, SummaryQuality.GOLD, 
                        new HashSet<ExpressionCallData>(Arrays.asList(
                            new ExpressionCallData(DataType.AFFYMETRIX, new HashSet<>(Arrays.asList(
                                    new ExperimentExpressionCount(
                                            CallType.Expression.EXPRESSED, 
                                            DataQuality.LOW, 
                                            PropagationState.SELF, 1),
                                    new ExperimentExpressionCount(
                                            CallType.Expression.EXPRESSED, 
                                            DataQuality.LOW, 
                                            PropagationState.ALL, 1)
                                    )),
                                    0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77),
                                  new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)), 
                            new ExpressionCallData(DataType.EST, new HashSet<>(Arrays.asList(
                                    new ExperimentExpressionCount(
                                            CallType.Expression.EXPRESSED, 
                                            DataQuality.LOW, 
                                            PropagationState.SELF, 1),
                                    new ExperimentExpressionCount(
                                            CallType.Expression.EXPRESSED, 
                                            DataQuality.HIGH, 
                                            PropagationState.SELF, 2),
                                    new ExperimentExpressionCount(
                                            CallType.Expression.EXPRESSED, 
                                            DataQuality.HIGH, 
                                            PropagationState.ALL, 2),
                                    new ExperimentExpressionCount(
                                            CallType.Expression.EXPRESSED, 
                                            DataQuality.LOW, 
                                            PropagationState.ALL, 1)
                                    )),
                                    0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77),
                                    new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)))), 
                        new BigDecimal(1257.34), new BigDecimal(100.00)),
                
                new ExpressionCall(g1, new Condition(anatEntity1, stage2, spe1), 
                    new DataPropagation(PropagationState.SELF, PropagationState.SELF, true),
                    ExpressionSummary.EXPRESSED, SummaryQuality.GOLD, 
                    Arrays.asList(
                        new ExpressionCallData(DataType.AFFYMETRIX, new HashSet<>(Arrays.asList(
                                new ExperimentExpressionCount(
                                        CallType.Expression.EXPRESSED, 
                                        DataQuality.LOW, 
                                        PropagationState.ALL, 1),
                                new ExperimentExpressionCount(
                                        CallType.Expression.EXPRESSED, 
                                        DataQuality.HIGH, 
                                        PropagationState.ALL, 3))),
                                0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77),
                                new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)),
                        new ExpressionCallData(DataType.EST, new HashSet<>(Arrays.asList(
                                new ExperimentExpressionCount(
                                        CallType.Expression.EXPRESSED, 
                                        DataQuality.LOW, 
                                        PropagationState.SELF, 1),
                                new ExperimentExpressionCount(
                                        CallType.Expression.EXPRESSED, 
                                        DataQuality.LOW, 
                                        PropagationState.ALL, 1))),
                                0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77),
                                new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)),
                        new ExpressionCallData(DataType.IN_SITU, new HashSet<>(Arrays.asList(
                                new ExperimentExpressionCount(
                                        CallType.Expression.EXPRESSED, 
                                        DataQuality.LOW, 
                                        PropagationState.SELF, 1),
                                new ExperimentExpressionCount(
                                        CallType.Expression.EXPRESSED, 
                                        DataQuality.LOW, 
                                        PropagationState.ALL, 1))),
                                0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77),
                                new DataPropagation(PropagationState.SELF, PropagationState.SELF, true))),
                    new BigDecimal(125.00), new BigDecimal(100.00)),
                
                new ExpressionCall(g1, new Condition(anatEntity1, stage3, spe1), 
                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true),
                        ExpressionSummary.EXPRESSED, SummaryQuality.SILVER, 
                        Arrays.asList(
                                new ExpressionCallData(DataType.AFFYMETRIX, new HashSet<>(Arrays.asList(
                                        new ExperimentExpressionCount(
                                                CallType.Expression.EXPRESSED, 
                                                DataQuality.LOW, 
                                                PropagationState.ALL, 1))),
                                        0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77),
                                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)),
                                new ExpressionCallData(DataType.EST, new HashSet<>(Arrays.asList(
                                        new ExperimentExpressionCount(
                                                CallType.Expression.EXPRESSED, 
                                                DataQuality.LOW, 
                                                PropagationState.ALL, 1))),
                                        0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77),
                                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)),
                                new ExpressionCallData(DataType.IN_SITU, new HashSet<>(Arrays.asList(
                                        new ExperimentExpressionCount(
                                                CallType.Expression.EXPRESSED, 
                                                DataQuality.LOW, 
                                                PropagationState.ALL, 1))),
                                        0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77),
                                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true))), 
                        new BigDecimal(125.00), new BigDecimal(100.00)));
        
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = 
                new LinkedHashMap<>();
        serviceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.ASC);
        
        List<CallService.Attribute> attrs = Arrays.asList(Attribute.values());
        
        Map<ExpressionSummary, SummaryQuality> summaryCallTypeQualityFilter = new HashMap<>();
        summaryCallTypeQualityFilter.put(ExpressionSummary.EXPRESSED, SummaryQuality.SILVER);
        Map<Expression, Boolean> callObservedData = new HashMap<>();
        callObservedData.put(Expression.EXPRESSED, true);
        callObservedData.put(Expression.NOT_EXPRESSED, false);
        CallService service = new CallService(serviceFactory);
        List<ExpressionCall> actualResults = service.loadExpressionCalls(
                new ExpressionCallFilter(summaryCallTypeQualityFilter, 
                        Collections.singleton(
                                new GeneFilter(g1.getSpecies().getId(), g1.getEnsemblGeneId())),
                        null, null, callObservedData,
                    null, null), 
                attrs, // all attributes 
                serviceOrdering)
                .collect(Collectors.toList());

        assertEquals("Incorrect ExpressionCalls generated", expectedResults, actualResults);
        
        LinkedHashMap<GlobalExpressionCallDAO.OrderingAttribute, DAO.Direction> orderingAttrs = 
                new LinkedHashMap<>();
        orderingAttrs.put(GlobalExpressionCallDAO.OrderingAttribute.MEAN_RANK, DAO.Direction.ASC);
        
        verify(dao).getGlobalExpressionCalls(
                //CallDAOFilters
                anyObject(), 
                // condition parameters
                eq(new HashSet<>(Arrays.asList(
                        ConditionDAO.Attribute.ANAT_ENTITY_ID, 
                        ConditionDAO.Attribute.STAGE_ID))),
                // global expression attributes
                eq(getAllGlobalExpressionCallDAOAttributes()),
                // ordering attributes
                eq(orderingAttrs));

    }
    
//    @Test
//    @Ignore("Test ignored until it is re-implemented following many modifications.")
//    public void shouldLoadExpressionCallsForTwoExpressionSummary() {
//        DAOManager manager = mock(DAOManager.class);
//        ServiceFactory serviceFactory = mock(ServiceFactory.class);
//        when(serviceFactory.getDAOManager()).thenReturn(manager);
//        ExpressionCallDAO exprDao = mock(ExpressionCallDAO.class);
//        when(manager.getExpressionCallDAO()).thenReturn(exprDao);
//        NoExpressionCallDAO noExprDao = mock(NoExpressionCallDAO.class);
//        when(manager.getNoExpressionCallDAO()).thenReturn(noExprDao);
//        
//        LinkedHashMap<ExpressionCallDAO.OrderingAttribute, DAO.Direction> orderingAttrs = 
//                new LinkedHashMap<>();
//        orderingAttrs.put(ExpressionCallDAO.OrderingAttribute.GENE_ID, DAO.Direction.ASC);
//        orderingAttrs.put(ExpressionCallDAO.OrderingAttribute.CONDITION_ID, DAO.Direction.ASC);
//        
//        ExpressionCallTOResultSet resultSetMock = getMockResultSet(ExpressionCallTOResultSet.class, 
//                Arrays.asList(
//                        // To not overload tests, we put null for not used attributes 
//                        // but, real query return all attributes
//                    new ExpressionCallTO(null, "geneId1", "anatEntityId1", "stageId1", 
//                        new BigDecimal(1257.34), CallTO.DataState.LOWQUALITY, null,
//                        CallTO.DataState.HIGHQUALITY, null, CallTO.DataState.NODATA, null,
//                        CallTO.DataState.NODATA, null, false, false, ExpressionCallTO.OriginOfLine.SELF, 
//                        ExpressionCallTO.OriginOfLine.SELF, true), 
//                    new ExpressionCallTO(null, "geneId1", "anatEntityId1", "stageId2", 
//                        new BigDecimal(125.00), CallTO.DataState.NODATA, null,
//                        CallTO.DataState.LOWQUALITY, null, CallTO.DataState.LOWQUALITY, null,
//                        CallTO.DataState.NODATA, null, false, false, ExpressionCallTO.OriginOfLine.SELF, 
//                        ExpressionCallTO.OriginOfLine.SELF, true), 
//                    new ExpressionCallTO(null, "geneId2", "anatEntityId2", "stageId2", 
//                        new BigDecimal(125.00), CallTO.DataState.NODATA, null,
//                        CallTO.DataState.NODATA, null, CallTO.DataState.NODATA, null,
//                        CallTO.DataState.HIGHQUALITY, null, false, false, ExpressionCallTO.OriginOfLine.SELF, 
//                        ExpressionCallTO.OriginOfLine.SELF, true)));
//        
//        //we'll do the verify afterwards, it's easier to catch a problem in the parameters
//        when(exprDao.getExpressionCalls(
//                //CallDAOFilters
//                anyCollectionOf(CallDAOFilter.class), 
//                //CallTOs
//                anyCollectionOf(ExpressionCallTO.class),
//                //propagation
//                anyBoolean(), anyBoolean(), 
//                //genes
//                anyCollectionOf(String.class), 
//                //orthology
//                anyObject(), 
//                //attributes
//                anyCollectionOf(ExpressionCallDAO.Attribute.class), 
//                anyObject()))
//        .thenReturn(resultSetMock);
//        
//        NoExpressionCallTOResultSet resultSetNoExprMock = getMockResultSet(NoExpressionCallTOResultSet.class, 
//                Arrays.asList(
//                        // To not overload tests, we put null for not used attributes 
//                        // but, real query return all attributes
//                    new NoExpressionCallTO(null, "geneId1", "anatEntityId1", "stageId1",
//                            CallTO.DataState.NODATA, CallTO.DataState.NODATA,  
//                            CallTO.DataState.NODATA, CallTO.DataState.HIGHQUALITY, false,
//                            NoExpressionCallTO.OriginOfLine.SELF),
//                    new NoExpressionCallTO(null, "geneId2", "anatEntityId1", "stageId2", 
//                        CallTO.DataState.NODATA, CallTO.DataState.LOWQUALITY,
//                        CallTO.DataState.NODATA, CallTO.DataState.NODATA, false,
//                        NoExpressionCallTO.OriginOfLine.SELF)));
//        //we'll do the verify afterwards, it's easier to catch a problem in the parameters
//        when(noExprDao.getNoExpressionCalls(anyObject())) //NoExpressionCallParams
//        .thenReturn(resultSetNoExprMock);
//
//        OntologyService ontService = mock(OntologyService.class);
//        AnatEntityService anatEntityService = mock(AnatEntityService.class);
//        DevStageService devStageService = mock(DevStageService.class);
//        when(serviceFactory.getOntologyService()).thenReturn(ontService);
//        when(serviceFactory.getAnatEntityService()).thenReturn(anatEntityService);
//        when(serviceFactory.getDevStageService()).thenReturn(devStageService);
//        //suppress warning as we cannot specify generic type for a mock
//        @SuppressWarnings("unchecked")
//        MultiSpeciesOntology<AnatEntity, String> anatEntityOnt = mock(MultiSpeciesOntology.class);
//        //suppress warning as we cannot specify generic type for a mock
//        @SuppressWarnings("unchecked")
//        MultiSpeciesOntology<DevStage, String> devStageOnt = mock(MultiSpeciesOntology.class);
//
//        when(ontService.getAnatEntityOntology(Arrays.asList(1), new HashSet<>(Arrays.asList(
//                "anatEntityId1", "anatEntityId2")), EnumSet.of(RelationType.ISA_PARTOF), true, false))
//        .thenReturn(anatEntityOnt);
//        when(ontService.getDevStageOntology(Arrays.asList(1), new HashSet<>(Arrays.asList(
//                "stageId1", "stageId2")), true, false)).thenReturn(devStageOnt);
//        String anatEntityId1 = "anatEntityId1";
//        AnatEntity anatEntity1 = new AnatEntity(anatEntityId1);
//        String anatEntityId2 = "anatEntityId2";
//        AnatEntity anatEntity2 = new AnatEntity(anatEntityId2);
//        String stageId1 = "stageId1";
//        DevStage stage1 = new DevStage(stageId1);
//        String stageId2 = "stageId2";
//        DevStage stage2 = new DevStage(stageId2);
//        String stageId3 = "stageId3";
//        DevStage stage3 = new DevStage(stageId3);
//
//        when(anatEntityOnt.getElements()).thenReturn(new HashSet<>(Arrays.asList(anatEntity1, anatEntity2)));
//        when(anatEntityOnt.getElement(anatEntityId1)).thenReturn(anatEntity1);
//        when(anatEntityOnt.getElement(anatEntityId2)).thenReturn(anatEntity2);
//        when(anatEntityOnt.getAncestors(anatEntity1)).thenReturn(new HashSet<>());
//        when(anatEntityOnt.getAncestors(anatEntity1, false)).thenReturn(new HashSet<>());
//        when(anatEntityOnt.getAncestors(anatEntity2)).thenReturn(new HashSet<>(Arrays.asList(anatEntity1)));
//        when(anatEntityOnt.getAncestors(anatEntity2, false)).thenReturn(new HashSet<>(Arrays.asList(anatEntity1)));
//        when(anatEntityOnt.getDescendants(anatEntity1)).thenReturn(new HashSet<>(Arrays.asList(anatEntity2)));
//        when(anatEntityOnt.getDescendants(anatEntity1, false)).thenReturn(new HashSet<>(Arrays.asList(anatEntity2)));
//
//        when(devStageOnt.getElements()).thenReturn(new HashSet<>(Arrays.asList(stage1, stage2, stage3)));
//        when(devStageOnt.getElement(stageId1)).thenReturn(stage1);
//        when(devStageOnt.getElement(stageId2)).thenReturn(stage2);
//        when(devStageOnt.getElement(stageId3)).thenReturn(stage3);
//        when(devStageOnt.getAncestors(stage1)).thenReturn(new HashSet<>(Arrays.asList(stage2, stage3)));
//        when(devStageOnt.getAncestors(stage2)).thenReturn(new HashSet<>(Arrays.asList(stage3)));
//        when(devStageOnt.getAncestors(stage3)).thenReturn(new HashSet<>());
//        when(devStageOnt.getAncestors(stage1, false)).thenReturn(new HashSet<>(Arrays.asList(stage2, stage3)));
//        when(devStageOnt.getAncestors(stage2, false)).thenReturn(new HashSet<>(Arrays.asList(stage3)));
//        when(devStageOnt.getAncestors(stage3, false)).thenReturn(new HashSet<>());
//
//        List<ExpressionCall> expectedResults = Arrays.asList(
//            new ExpressionCall("geneId1", new Condition("anatEntityId1", "stageId1", "speciesId1"), 
//                true, ExpressionSummary.STRONG_AMBIGUITY, null, Arrays.asList(
//                    new ExpressionCallData(DataType.AFFYMETRIX,
//                        0 /*presentHighSelfExpCount*/, 1 /*presentLowSelfExpCount*/, 0, 0,
//                        0 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0), 
//                    new ExpressionCallData(DataType.EST,
//                        1 /*presentHighSelfExpCount*/, 0 /*presentLowSelfExpCount*/, 0, 0,
//                        1 /*presentHighTotalCount*/, 0 /*presentLowTotalCount*/, 0, 0), 
//                    new ExpressionCallData(DataType.RNA_SEQ,
//                        0 , 0 , 2 /*absentHighSelfExpCount*/, 0,
//                        0 , 0 , 2 /*absentHighTotalCount*/, 0)), 
////                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, 
////                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)), 
////                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.EST, 
////                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)), 
////                    new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, 
////                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true))), 
//                new BigDecimal(1257.34)),
//            new ExpressionCall("geneId1", new Condition("anatEntityId1", "stageId2", "speciesId1"), 
//                true, ExpressionSummary.EXPRESSED, SummaryQuality.GOLD, Arrays.asList(
//                    new ExpressionCallData(DataType.AFFYMETRIX,
//                        0 /*presentHighSelfExpCount*/, 0 /*presentLowSelfExpCount*/, 0, 0,
//                        0 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0), 
//                    new ExpressionCallData(DataType.EST,
//                        0 /*presentHighSelfExpCount*/, 0 /*presentLowSelfExpCount*/, 0, 0,
//                        2 /*presentHighTotalCount*/, 0 /*presentLowTotalCount*/, 0, 0), 
//                    new ExpressionCallData(DataType.EST,
//                        0 /*presentHighSelfExpCount*/, 1 /*presentLowSelfExpCount*/, 0, 0,
//                        0 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0), 
//                    new ExpressionCallData(DataType.IN_SITU,
//                        0 /*presentHighSelfExpCount*/, 1 /*presentLowSelfExpCount*/, 0, 0,
//                        0 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0)), 
////                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, 
////                        new DataPropagation(PropagationState.SELF, PropagationState.DESCENDANT, false)), 
////                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.EST, 
////                        new DataPropagation(PropagationState.SELF, PropagationState.DESCENDANT, false)),
////                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST,
////                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)), 
////                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU,
////                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true))), 
//                new BigDecimal(125.00))
//            ,
//            new ExpressionCall("geneId1", new Condition("anatEntityId1", "stageId3", "speciesId1"), 
//                false, ExpressionSummary.EXPRESSED, SummaryQuality.GOLD, Arrays.asList(
//                    new ExpressionCallData(DataType.AFFYMETRIX,
//                        0 /*presentHighSelfExpCount*/, 0 /*presentLowSelfExpCount*/, 0, 0,
//                        0 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0), 
//                    new ExpressionCallData(DataType.EST,
//                        0 /*presentHighSelfExpCount*/, 0 /*presentLowSelfExpCount*/, 0, 0,
//                        1 /*presentHighTotalCount*/, 0 /*presentLowTotalCount*/, 0, 0), 
//                    new ExpressionCallData(DataType.EST,
//                        0 /*presentHighSelfExpCount*/, 0 /*presentLowSelfExpCount*/, 0, 0,
//                        0 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0), 
//                    new ExpressionCallData(DataType.IN_SITU,
//                        0 /*presentHighSelfExpCount*/, 0 /*presentLowSelfExpCount*/, 0, 0,
//                        0 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0)), 
////                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, 
////                        new DataPropagation(PropagationState.SELF, PropagationState.DESCENDANT, false)), 
////                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.EST, 
////                        new DataPropagation(PropagationState.SELF, PropagationState.DESCENDANT, false)),
////                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST,
////                        new DataPropagation(PropagationState.SELF, PropagationState.DESCENDANT, false)), 
////                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU,
////                        new DataPropagation(PropagationState.SELF, PropagationState.DESCENDANT, false))), 
//                null),
//            // We propagate no-expression only in where at least one expression call
//            // (propagated or not) is found. So no ExpressionCall geneId1 / anatEntityId2 / stageId1 
//            new ExpressionCall("geneId2", new Condition("anatEntityId1", "stageId2", "speciesId1"), 
//                true, ExpressionSummary.STRONG_AMBIGUITY, null, Arrays.asList(
//                    new ExpressionCallData(DataType.RNA_SEQ,
//                        0 /*presentHighSelfExpCount*/, 0 /*presentLowSelfExpCount*/, 0, 0,
//                        3 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0), 
//                    new ExpressionCallData(DataType.IN_SITU,
//                        0 , 0 , 0 /*absentHighSelfExpCount*/, 1 /*absentLowSelfExpCount*/,
//                        0 , 0 , 0 /*absentHighTotalCount*/, 1 /*absentLowTotalCount*/)), 
////                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.High, DataType.RNA_SEQ,
////                        new DataPropagation(PropagationState.DESCENDANT, PropagationState.SELF, false)),
////                    new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.IN_SITU,
////                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true))), 
//                null),
//            new ExpressionCall("geneId2", new Condition("anatEntityId1", "stageId3", "speciesId1"), 
//                false, ExpressionSummary.EXPRESSED, SummaryQuality.GOLD, Arrays.asList(
//                    new ExpressionCallData(DataType.RNA_SEQ,
//                        0 /*presentHighSelfExpCount*/, 0 /*presentLowSelfExpCount*/, 0, 0,
//                        3 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0)), 
////                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ,
////                        new DataPropagation(PropagationState.DESCENDANT, PropagationState.DESCENDANT, false))), 
//                null),
//            new ExpressionCall("geneId2", new Condition("anatEntityId2", "stageId2", "speciesId1"), 
//                true, ExpressionSummary.WEAK_AMBIGUITY, null, Arrays.asList(
//                    new ExpressionCallData(DataType.RNA_SEQ,
//                        2 /*presentHighSelfExpCount*/, 0 /*presentLowSelfExpCount*/, 0, 0,
//                        3 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0), 
//                    new ExpressionCallData(DataType.IN_SITU,
//                        0 , 0 , 0 /*absentHighSelfExpCount*/, 0 /*absentLowSelfExpCount*/,
//                        0 , 0 , 0 /*absentHighTotalCount*/, 1 /*absentLowTotalCount*/)), 
////                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ,
////                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)),
////                    new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.IN_SITU,
////                        new DataPropagation(PropagationState.ANCESTOR, PropagationState.SELF, false))), 
//                new BigDecimal(125.00)),
//            new ExpressionCall("geneId2", new Condition("anatEntityId2", "stageId3", "speciesId1"), 
//                false, ExpressionSummary.EXPRESSED, SummaryQuality.GOLD, Arrays.asList(
//                    new ExpressionCallData(DataType.RNA_SEQ,
//                        0 /*presentHighSelfExpCount*/, 0 /*presentLowSelfExpCount*/, 0, 0,
//                        1 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0)), 
////                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ,
////                        new DataPropagation(PropagationState.SELF, PropagationState.DESCENDANT, false))), 
//                null));
//
//        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = 
//                new LinkedHashMap<>();
//        serviceOrdering.put(CallService.OrderingAttribute.GENE_ID, Service.Direction.ASC);
//        serviceOrdering.put(CallService.OrderingAttribute.ANAT_ENTITY_ID, Service.Direction.ASC);
//        serviceOrdering.put(CallService.OrderingAttribute.DEV_STAGE_ID, Service.Direction.ASC);
//        
//
//        CallService service = new CallService(serviceFactory);
//        List<ExpressionCall> actualResults = service.loadExpressionCalls("speciesId1", 
//                new ExpressionCallFilter(null, null, null, null, null, new DataPropagation()),
//                null, // all attributes 
//                serviceOrdering)
//                .collect(Collectors.toList());
//        assertEquals("Incorrect ExpressionCalls generated", expectedResults, actualResults);
//        
//        verify(exprDao).getExpressionCalls(
//                //CallDAOFilters
//                collectionEq(Arrays.asList(
//                    new CallDAOFilter(null, Arrays.asList("speciesId1"), null))
//                ), 
//                //CallTOs
//                collectionEq(Arrays.asList(new ExpressionCallTO(
//                        null, null, null, null, null, null, true))),
//                //propagation
//                eq(false), eq(false), 
//                //genes
//                collectionEq(Arrays.asList()), 
//                //orthology
//                eq(null), 
//                //attributes
//                collectionEq(this.getAllExpressionCallDAOAttributes()), 
//                eq(orderingAttrs));
//        // TODO NoExpressionCallParams has no equals method so we cannot set something like eq(params)
//        verify(noExprDao).getNoExpressionCalls(anyObject());
//    }

    /**
     * Test the method {@link CallService#loadExpressionCalls(String, ExpressionCallFilter, 
     * Collection, LinkedHashMap, boolean)}.
     */    
    // Keep in this test empty stream for no-expression calls:
    // it allows to detect if all calls are read when only one query has calls
    @Test
    public void shouldLoadExpressionCallsForSeveralGenes() {
        //Retrieving geneId, anatEntityId, unordered, with substructures but without sub-stages. 
        DAOManager manager = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(manager);
        SpeciesService speciesService = mock(SpeciesService.class);
        when(serviceFactory.getSpeciesService()).thenReturn(speciesService);
        GlobalExpressionCallDAO dao = mock(GlobalExpressionCallDAO.class);
        when(manager.getGlobalExpressionCallDAO()).thenReturn(dao);
        GeneDAO geneDAO = mock(GeneDAO.class);
        when(manager.getGeneDAO()).thenReturn(geneDAO);
        ConditionDAO condDAO = mock(ConditionDAO.class);
        when(manager.getConditionDAO()).thenReturn(condDAO);
        
        Species spe1 = new Species(1);
        GeneTO gTO1 = new GeneTO(1, "gene1", "geneName1", spe1.getId());
        GeneTO gTO2 = new GeneTO(2, "gene2", "geneName2", spe1.getId());
        Gene g1 = new Gene(gTO1.getGeneId(), gTO1.getName(), null, spe1, new GeneBioType("b"), 1);
        Gene g2 = new Gene(gTO2.getGeneId(), gTO2.getName(), null, spe1, new GeneBioType("b"), 1);
        
        AnatEntity anatEntity1 = new AnatEntity("anatEntity1");
        AnatEntity anatEntity2 = new AnatEntity("anatEntity2");
        DevStage devStage1 = new DevStage("devStage1");
        DevStage devStage2 = new DevStage("devStage2");
        ConditionTO condTO1 = new ConditionTO(1, anatEntity1.getId(), devStage1.getId(), spe1.getId());
        ConditionTO condTO2 = new ConditionTO(2, anatEntity1.getId(), devStage2.getId(), spe1.getId());
        ConditionTO condTO3 = new ConditionTO(3, anatEntity2.getId(), devStage1.getId(), spe1.getId());
        Condition cond1 = new Condition(anatEntity1, devStage1, spe1);
        Condition cond2 = new Condition(anatEntity1, devStage2, spe1);
        Condition cond3 = new Condition(anatEntity2, devStage1, spe1);
        
        GeneTOResultSet geneTOResultSet = getMockResultSet(GeneTOResultSet.class, Arrays.asList(
                new GeneTO(gTO1.getId(), gTO1.getGeneId(), gTO1.getName(), gTO1.getDescription(), gTO1.getSpeciesId(), 
                        1, 1, true, 1),
                new GeneTO(gTO2.getId(), gTO2.getGeneId(), gTO2.getName(), gTO2.getDescription(), gTO2.getSpeciesId(), 
                        1, 1, true, 1)));
        Map<Integer, Set<String>> speciesIdToGeneIds = new HashMap<>();
        speciesIdToGeneIds.put(spe1.getId(), 
                new HashSet<>(Arrays.asList(g1.getEnsemblGeneId(), g2.getEnsemblGeneId())));
        when(geneDAO.getGenesBySpeciesAndGeneIds(speciesIdToGeneIds)).thenReturn(geneTOResultSet);
        configureMockGeneDAOForBioType(geneDAO);
        
        Map<Integer, Species> speciesById = new HashMap<>();
        speciesById.put(spe1.getId(), spe1);
        when(speciesService.loadSpeciesMap(new HashSet<>(Arrays.asList(1)), false)).thenReturn(speciesById);
                
        Map<ConditionDAO.Attribute, DAOPropagationState> dataPropagation = new HashMap<>();
        dataPropagation.put(ConditionDAO.Attribute.ANAT_ENTITY_ID, DAOPropagationState.SELF);
        dataPropagation.put(ConditionDAO.Attribute.STAGE_ID, DAOPropagationState.SELF);
        ConditionTOResultSet condTOResultSet = getMockResultSet(ConditionTOResultSet.class, Arrays.asList(
                new ConditionTO(1, anatEntity1.getId(), devStage1.getId(), spe1.getId()),
                new ConditionTO(2, anatEntity1.getId(), devStage2.getId(), spe1.getId()),
                new ConditionTO(3, anatEntity2.getId(), devStage1.getId(), spe1.getId())));
        when(condDAO.getGlobalConditionsBySpeciesIds(eq(Collections.singleton(spe1.getId())), 
                eq(new HashSet<>(Arrays.asList(ConditionDAO.Attribute.ANAT_ENTITY_ID, 
                        ConditionDAO.Attribute.STAGE_ID))),
                anyObject())).thenReturn(condTOResultSet);
        
        GlobalExpressionCallTOResultSet resultSetMock = getMockResultSet(GlobalExpressionCallTOResultSet.class, 
                Arrays.asList(
                        new GlobalExpressionCallTO(1, gTO1.getId(), condTO1.getId(), 
                                new BigDecimal(1257.34), new HashSet<>(Arrays.asList(
                                        new GlobalExpressionCallDataTO(DAODataType.AFFYMETRIX, true, 
                                                dataPropagation , new HashSet<>(Arrays.asList(
                                                       new DAOExperimentCount(
                                                               DAOExperimentCount.CallType.PRESENT, 
                                                               DAOExperimentCount.DataQuality.LOW, 
                                                               DAOPropagationState.ALL, 1))), 
                                                0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77))))),
                        new GlobalExpressionCallTO(2, gTO1.getId(), condTO2.getId(), 
                                new BigDecimal(1257.34), new HashSet<>(Arrays.asList(
                                        new GlobalExpressionCallDataTO(DAODataType.EST, true, 
                                                dataPropagation , new HashSet<>(Arrays.asList(
                                                        new DAOExperimentCount(
                                                                DAOExperimentCount.CallType.PRESENT, 
                                                                DAOExperimentCount.DataQuality.LOW, 
                                                                DAOPropagationState.ALL, 2))), 
                                                0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77))))),
                        new GlobalExpressionCallTO(3, gTO2.getId(), condTO3.getId(), 
                                new BigDecimal(1257.34), new HashSet<>(Arrays.asList(
                                        new GlobalExpressionCallDataTO(DAODataType.EST, true, 
                                                dataPropagation , new HashSet<>(Arrays.asList(
                                                        new DAOExperimentCount(
                                                                DAOExperimentCount.CallType.PRESENT, 
                                                                DAOExperimentCount.DataQuality.LOW, 
                                                                DAOPropagationState.ALL, 1))), 
                                                0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77)))))));

        //we'll do the verify afterwards, it's easier to catch a problem in the parameters
        when(dao.getGlobalExpressionCalls(
                // CallDAOFilters
                anyCollectionOf(CallDAOFilter.class), 
                // condition parameters
                anyCollectionOf(ConditionDAO.Attribute.class),
                // global expression attributes
                anyCollectionOf(GlobalExpressionCallDAO.Attribute.class),
                // ordering attributes
                anyObject()))
        .thenReturn(resultSetMock);
        
        OntologyService ontService = mock(OntologyService.class);
        AnatEntityService anatEntityService = mock(AnatEntityService.class);
        DevStageService devStageService = mock(DevStageService.class);
        when(serviceFactory.getOntologyService()).thenReturn(ontService);
        when(serviceFactory.getAnatEntityService()).thenReturn(anatEntityService);
        when(serviceFactory.getDevStageService()).thenReturn(devStageService);
        //suppress warning as we cannot specify generic type for a mock
        @SuppressWarnings("unchecked")
        Ontology<AnatEntity, String> anatEntityOnt = mock(Ontology.class);
        //suppress warning as we cannot specify generic type for a mock
        @SuppressWarnings("unchecked")
        Ontology<DevStage, String> devStageOnt = mock(Ontology.class);

        when(ontService.getAnatEntityOntology(spe1.getId(), Arrays.asList(
                anatEntity1.getId(), anatEntity2.getId()), 
                EnumSet.of(RelationType.ISA_PARTOF), true, false))
            .thenReturn(anatEntityOnt);
        when(ontService.getDevStageOntology(spe1.getId(), Arrays.asList(
                devStage1.getId(), devStage2.getId()), true, false)).thenReturn(devStageOnt);
        
        when(anatEntityService.loadAnatEntities(Collections.singleton(spe1.getId()), 
                true, new HashSet<String>(Arrays.asList(
                        anatEntity1.getId(), anatEntity2.getId())),false))
        .thenReturn(Stream.of(anatEntity1, anatEntity2));
        when(devStageService.loadDevStages(Collections.singleton(spe1.getId()), 
                true, new HashSet<String>(Arrays.asList(
                        devStage1.getId(), devStage2.getId())),false))
        .thenReturn(Stream.of(devStage1, devStage2));

        when(anatEntityOnt.getElements()).thenReturn(new HashSet<>(Arrays.asList(anatEntity1, anatEntity2)));
        when(devStageOnt.getElements()).thenReturn(new HashSet<>(Arrays.asList(devStage1, devStage2)));
        when(anatEntityOnt.getElement(anatEntity1.getId())).thenReturn(anatEntity1);
        when(anatEntityOnt.getElement(anatEntity2.getId())).thenReturn(anatEntity2);
        when(devStageOnt.getElement(devStage1.getId())).thenReturn(devStage1);
        when(devStageOnt.getElement(devStage2.getId())).thenReturn(devStage2);
        when(anatEntityOnt.getAncestors(anatEntity1)).thenReturn(new HashSet<>(Arrays.asList(anatEntity2)));
        when(anatEntityOnt.getAncestors(anatEntity2)).thenReturn(new HashSet<>());
        when(devStageOnt.getAncestors(devStage1)).thenReturn(new HashSet<>(Arrays.asList(devStage2)));
        when(devStageOnt.getAncestors(devStage2)).thenReturn(new HashSet<>());
        when(anatEntityOnt.getAncestors(anatEntity1, false)).thenReturn(new HashSet<>(Arrays.asList(anatEntity2)));
        when(anatEntityOnt.getAncestors(anatEntity2, false)).thenReturn(new HashSet<>());
        when(devStageOnt.getAncestors(devStage1, false)).thenReturn(new HashSet<>(Arrays.asList(devStage2)));
        when(devStageOnt.getAncestors(devStage2, false)).thenReturn(new HashSet<>());
        
        GlobalConditionMaxRankTO maxRankTO = new GlobalConditionMaxRankTO(new BigDecimal(100),new BigDecimal(100));
        when(condDAO.getMaxRank()).thenReturn(maxRankTO);

        List<ExpressionCall> expectedResults = Arrays.asList(
                new ExpressionCall(g1, cond1, 
                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true),
                        ExpressionSummary.EXPRESSED, SummaryQuality.BRONZE, 
                        new HashSet<ExpressionCallData>(Arrays.asList(
                            new ExpressionCallData(DataType.AFFYMETRIX, new HashSet<>(Arrays.asList(
                                    new ExperimentExpressionCount(
                                            CallType.Expression.EXPRESSED, DataQuality.LOW, PropagationState.ALL, 1)
                                    )),
                                    0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77),
                                  new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)))), 
                        new BigDecimal(1257.34), new BigDecimal(100.00)), 
                new ExpressionCall(g1, cond2, 
                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true),
                        ExpressionSummary.EXPRESSED, SummaryQuality.SILVER, 
                        new HashSet<ExpressionCallData>(Arrays.asList(
                            new ExpressionCallData(DataType.EST, new HashSet<>(Arrays.asList(
                                    new ExperimentExpressionCount(
                                            CallType.Expression.EXPRESSED, DataQuality.LOW, PropagationState.ALL, 2)
                                    )),
                                    0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77),
                                  new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)))), 
                        new BigDecimal(1257.34), new BigDecimal(100.00)),
                new ExpressionCall(g2, cond3, 
                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true),
                        ExpressionSummary.EXPRESSED, SummaryQuality.BRONZE, 
                        new HashSet<ExpressionCallData>(Arrays.asList(
                            new ExpressionCallData(DataType.EST, new HashSet<>(Arrays.asList(
                                    new ExperimentExpressionCount(
                                            CallType.Expression.EXPRESSED, DataQuality.LOW, PropagationState.ALL, 1)
                                    )),
                                    0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77),
                                  new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)))), 
                        new BigDecimal(1257.34), new BigDecimal(100.00)) 
            );
        
        Map<ExpressionSummary, SummaryQuality> summaryCallTypeQualityFilter = new HashMap<>();
        summaryCallTypeQualityFilter.put(ExpressionSummary.EXPRESSED, SummaryQuality.BRONZE);
        
        Map<Expression, Boolean> callObservedData = new HashMap<>();
        callObservedData.put(Expression.EXPRESSED, true);
        callObservedData.put(Expression.NOT_EXPRESSED, false);
        
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = 
                new LinkedHashMap<>();
        serviceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.ASC);
        
        List<CallService.Attribute> attrs = Arrays.asList(Attribute.values());
        
        CallService service = new CallService(serviceFactory);
        
        List<ExpressionCall> actualResults = service.loadExpressionCalls(
                new ExpressionCallFilter(summaryCallTypeQualityFilter, 
                        Collections.singleton(
                                new GeneFilter(g1.getSpecies().getId(), 
                                        Arrays.asList(g1.getEnsemblGeneId(), g2.getEnsemblGeneId()))),
                        null, null, callObservedData,
                    null, null), 
                attrs, // all attributes 
                serviceOrdering)
                .collect(Collectors.toList());
        
        
        assertEquals("Incorrect ExpressionCalls generated", expectedResults, actualResults);
        
        LinkedHashMap<GlobalExpressionCallDAO.OrderingAttribute, DAO.Direction> orderingAttrs = 
                new LinkedHashMap<>();
        orderingAttrs.put(GlobalExpressionCallDAO.OrderingAttribute.MEAN_RANK, DAO.Direction.ASC);
        
        verify(dao).getGlobalExpressionCalls(
                //CallDAOFilters
        eq(Arrays.asList(
                new CallDAOFilter(new HashSet<>(Arrays.asList(1, 2)), new HashSet<>(), null, 
                        new HashSet<>(Arrays.asList(
                                new CallDataDAOFilter(
                                       new HashSet<>(Arrays.asList(
                                               new HashSet<>(Arrays.asList(
                                                       new DAOExperimentCountFilter(
                                                               DAOExperimentCount.CallType.PRESENT, 
                                                               DAOExperimentCount.DataQuality.HIGH, 
                                                               DAOPropagationState.ALL, 
                                                               DAOExperimentCountFilter.Qualifier.GREATER_THAN,
                                                               0),
                                                       new DAOExperimentCountFilter(
                                                               DAOExperimentCount.CallType.PRESENT, 
                                                               DAOExperimentCount.DataQuality.LOW, 
                                                               DAOPropagationState.ALL, 
                                                               DAOExperimentCountFilter.Qualifier.GREATER_THAN, 
                                                               0))),
                                               new HashSet<>(Arrays.asList(
                                                       new DAOExperimentCountFilter(
                                                               DAOExperimentCount.CallType.ABSENT, 
                                                               DAOExperimentCount.DataQuality.HIGH, 
                                                               DAOPropagationState.SELF, 
                                                               DAOExperimentCountFilter.Qualifier.EQUALS_TO, 
                                                               0))),
                                               new HashSet<>(Arrays.asList(
                                                       new DAOExperimentCountFilter(
                                                               DAOExperimentCount.CallType.ABSENT, 
                                                               DAOExperimentCount.DataQuality.LOW, 
                                                               DAOPropagationState.SELF, 
                                                               DAOExperimentCountFilter.Qualifier.EQUALS_TO, 
                                                               0))),
                                               new HashSet<>(Arrays.asList(
                                                       new DAOExperimentCountFilter(
                                                               DAOExperimentCount.CallType.PRESENT, 
                                                               DAOExperimentCount.DataQuality.LOW, 
                                                               DAOPropagationState.SELF, 
                                                               DAOExperimentCountFilter.Qualifier.GREATER_THAN, 
                                                               0),
                                                       new DAOExperimentCountFilter(
                                                               DAOExperimentCount.CallType.PRESENT, 
                                                               DAOExperimentCount.DataQuality.HIGH, 
                                                               DAOPropagationState.SELF, 
                                                               DAOExperimentCountFilter.Qualifier.GREATER_THAN, 
                                                               0)))
                                                   )),
                                        Arrays.asList(DAODataType.values()),
                                        null,
                                        new HashMap<>())))
                        ))),
                // condition parameters
                eq(new HashSet<>(Arrays.asList(
                        ConditionDAO.Attribute.ANAT_ENTITY_ID, ConditionDAO.Attribute.STAGE_ID))),
                // global expression attributes
                eq(getAllGlobalExpressionCallDAOAttributes()),
                // ordering attributes
                eq(orderingAttrs));

    }

    private Set<GlobalExpressionCallDAO.Attribute> getAllGlobalExpressionCallDAOAttributes() {
        return EnumSet.of(GlobalExpressionCallDAO.Attribute.DATA_TYPE_RANK_INFO, 
                GlobalExpressionCallDAO.Attribute.GLOBAL_CONDITION_ID, GlobalExpressionCallDAO.Attribute.BGEE_GENE_ID, 
                GlobalExpressionCallDAO.Attribute.DATA_TYPE_EXPERIMENT_TOTAL_COUNTS, 
                GlobalExpressionCallDAO.Attribute.DATA_TYPE_OBSERVED_DATA, 
                GlobalExpressionCallDAO.Attribute.MEAN_RANK, 
                GlobalExpressionCallDAO.Attribute.DATA_TYPE_EXPERIMENT_SELF_COUNTS, 
                GlobalExpressionCallDAO.Attribute.DATA_TYPE_EXPERIMENT_PROPAGATED_COUNTS);
    }

    /**
     * Test the method {@link CallService#loadExpressionCalls(String, ExpressionCallFilter, 
     * Collection, LinkedHashMap, boolean)}.
     */
    @Test
    public void shouldLoadExpressionCallsWithFiltering() {
        //More complex query
        DAOManager manager = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(manager);
        GlobalExpressionCallDAO dao = mock(GlobalExpressionCallDAO.class);
        when(manager.getGlobalExpressionCallDAO()).thenReturn(dao);
        
        Species spe1 = new Species(1);
        GeneTO gTO1 = new GeneTO(1, "gene1", "geneName1", spe1.getId());
        Gene g1 = new Gene(gTO1.getGeneId(), gTO1.getName(), null, spe1, new GeneBioType("b"), 1);
        
        AnatEntity anatEntity1 = new AnatEntity("anatEntity1");
        DevStage devStage1 = new DevStage("devStage1");
        ConditionTO condTO1 = new ConditionTO(1, anatEntity1.getId(), devStage1.getId(), spe1.getId());
        Condition cond1 = new Condition(anatEntity1, devStage1, spe1);
        
        Map<ConditionDAO.Attribute, DAOPropagationState> dataPropagation = new HashMap<>();
        dataPropagation.put(ConditionDAO.Attribute.ANAT_ENTITY_ID, DAOPropagationState.SELF);
        dataPropagation.put(ConditionDAO.Attribute.STAGE_ID, DAOPropagationState.SELF);
        
        GlobalExpressionCallTOResultSet resultSetMock = getMockResultSet(GlobalExpressionCallTOResultSet.class, 
                Arrays.asList(
                        new GlobalExpressionCallTO(3, gTO1.getId(), condTO1.getId(), 
                                new BigDecimal(125.42), new HashSet<>(Arrays.asList(
                                        new GlobalExpressionCallDataTO(DAODataType.EST, true, 
                                                dataPropagation , new HashSet<>(Arrays.asList(
                                                        new DAOExperimentCount(
                                                                DAOExperimentCount.CallType.PRESENT, 
                                                                DAOExperimentCount.DataQuality.HIGH, 
                                                                DAOPropagationState.ALL, 3))), 
                                                0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77))
                                        )))
                        ));
        
        //we'll do the verify afterwards, it's easier to catch a problem in the parameters
        when(dao.getGlobalExpressionCalls(
                // CallDAOFilters
                anyCollectionOf(CallDAOFilter.class), 
                // condition parameters
                anyCollectionOf(ConditionDAO.Attribute.class),
                // global expression attributes
                anyCollectionOf(GlobalExpressionCallDAO.Attribute.class),
                // ordering attributes
                anyObject()))
        .thenReturn(resultSetMock);
        
        OntologyService ontService = mock(OntologyService.class);
        AnatEntityService anatEntityService = mock(AnatEntityService.class);
        DevStageService devStageService = mock(DevStageService.class);
        when(serviceFactory.getOntologyService()).thenReturn(ontService);
        when(serviceFactory.getAnatEntityService()).thenReturn(anatEntityService);
        when(serviceFactory.getDevStageService()).thenReturn(devStageService);
        //suppress warning as we cannot specify generic type for a mock
        @SuppressWarnings("unchecked")
        Ontology<AnatEntity, String> anatEntityOnt = mock(Ontology.class);
        //suppress warning as we cannot specify generic type for a mock
        @SuppressWarnings("unchecked")
        Ontology<DevStage, String> devStageOnt = mock(Ontology.class);

        when(ontService.getAnatEntityOntology(spe1.getId(), new HashSet<>(Arrays.asList(
                "anatEntityId1")), EnumSet.of(RelationType.ISA_PARTOF), false, false))
        .thenReturn(anatEntityOnt);
        when(ontService.getDevStageOntology(spe1.getId(), new HashSet<>(Arrays.asList(
                "stageId1", "stageId2")), false, false)).thenReturn(devStageOnt);
        
        when(anatEntityService.loadAnatEntities(Collections.singleton(spe1.getId()), 
                true, new HashSet<String>(Arrays.asList(
                        anatEntity1.getId())),false))
        .thenReturn(Stream.of(anatEntity1));
        when(devStageService.loadDevStages(Collections.singleton(spe1.getId()), 
                true, new HashSet<String>(Arrays.asList(
                        devStage1.getId())),false))
        .thenReturn(Stream.of(devStage1));

        when(anatEntityOnt.getElements()).thenReturn(new HashSet<>(Arrays.asList(anatEntity1)));
        when(devStageOnt.getElements()).thenReturn(new HashSet<>(Arrays.asList(devStage1)));
        when(anatEntityOnt.getElement(anatEntity1.getId())).thenReturn(anatEntity1);
        when(devStageOnt.getElement(devStage1.getId())).thenReturn(devStage1);
        // No relation to not propagate calls
        when(anatEntityOnt.getAncestors(anatEntity1, true)).thenReturn(new HashSet<>());
        when(devStageOnt.getAncestors(devStage1, true)).thenReturn(new HashSet<>());
        
        SpeciesService speciesService = mock(SpeciesService.class);
        when(serviceFactory.getSpeciesService()).thenReturn(speciesService);
        ConditionDAO condDAO = mock(ConditionDAO.class);
        when(manager.getConditionDAO()).thenReturn(condDAO);
        
        GeneDAO geneDAO = mock(GeneDAO.class);
        when(manager.getGeneDAO()).thenReturn(geneDAO);
        GeneTOResultSet geneTOResultSet = getMockResultSet(GeneTOResultSet.class, Arrays.asList(
                new GeneTO(1, g1.getEnsemblGeneId(), g1.getName(), g1.getDescription(), g1.getSpecies().getId(), 
                        1, 1, true, 1)));
        Map<Integer, Set<String>> speciesIdToGeneIds = new HashMap<>();
        speciesIdToGeneIds.put(spe1.getId(), Collections.singleton(g1.getEnsemblGeneId()));
        when(geneDAO.getGenesBySpeciesAndGeneIds(speciesIdToGeneIds)).thenReturn(geneTOResultSet);
        configureMockGeneDAOForBioType(geneDAO);
        
        Map<Integer, Species> speciesById = new HashMap<>();
        speciesById.put(spe1.getId(), spe1);
        when(speciesService.loadSpeciesMap(new HashSet<>(Arrays.asList(1)), false)).thenReturn(speciesById);
        
        GlobalConditionMaxRankTO maxRankTO = new GlobalConditionMaxRankTO(new BigDecimal(100),new BigDecimal(100));
        when(condDAO.getMaxRank()).thenReturn(maxRankTO);
        ConditionTOResultSet condTOResultSet = 
                getMockResultSet(ConditionTOResultSet.class, Arrays.asList(condTO1));
        when(condDAO.getGlobalConditionsBySpeciesIds(eq(Collections.singleton(spe1.getId())), 
                eq(new HashSet<>(Arrays.asList(ConditionDAO.Attribute.ANAT_ENTITY_ID, 
                        ConditionDAO.Attribute.STAGE_ID))),
                anyObject())).thenReturn(condTOResultSet);
        
        List<ExpressionCall> expectedResults = Arrays.asList(
                new ExpressionCall(g1, cond1, 
                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true),
                        ExpressionSummary.EXPRESSED, SummaryQuality.GOLD, 
                        new HashSet<ExpressionCallData>(Arrays.asList(
                            new ExpressionCallData(DataType.EST, new HashSet<>(Arrays.asList(
                                    new ExperimentExpressionCount(
                                            CallType.Expression.EXPRESSED, 
                                            DataQuality.HIGH, 
                                            PropagationState.ALL, 3))),
                                    0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77),
                                  new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)))), 
                        new BigDecimal(125.42), new BigDecimal(100.00)));


        
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = 
                new LinkedHashMap<>();
        serviceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.ASC);

        Map<ExpressionSummary, SummaryQuality> summaryCallTypeQualityFilter = new HashMap<>();
        summaryCallTypeQualityFilter.put(ExpressionSummary.EXPRESSED, SummaryQuality.GOLD);
        
        Map<Expression, Boolean> callObservedData = new HashMap<>();
        callObservedData.put(Expression.EXPRESSED, true);
        callObservedData.put(Expression.NOT_EXPRESSED, false);
        
        List<CallService.Attribute> attrs = Arrays.asList(Attribute.values());
        
        CallService service = new CallService(serviceFactory);
        
        List<ExpressionCall> actualResults = service.loadExpressionCalls(
                new ExpressionCallFilter(summaryCallTypeQualityFilter, 
                        Collections.singleton(
                                new GeneFilter(spe1.getId(), g1.getEnsemblGeneId())),
                        Collections.singleton(
                                new ConditionFilter(Collections.singleton(anatEntity1.getId()), 
                                        Collections.singleton(devStage1.getId()), null)), 
                        Collections.singleton(
                                DataType.EST), 
                        callObservedData,
                    null, null), 
                attrs, // all attributes 
                serviceOrdering)
                .collect(Collectors.toList());
        
        assertEquals("Incorrect ExpressionCalls generated", expectedResults, actualResults);
        
        
        LinkedHashMap<GlobalExpressionCallDAO.OrderingAttribute, DAO.Direction> orderingAttrs = 
                new LinkedHashMap<>();
        orderingAttrs.put(GlobalExpressionCallDAO.OrderingAttribute.MEAN_RANK, DAO.Direction.ASC);
        
        verify(dao).getGlobalExpressionCalls(
        //CallDAOFilters
        eq(Arrays.asList(
              new CallDAOFilter(new HashSet<>(Arrays.asList(1)), new HashSet<>(), 
                      new HashSet<>(Arrays.asList(
                              new DAOConditionFilter(Collections.singleton(anatEntity1.getId()), 
                                      Collections.singleton(devStage1.getId()), null))), 
                      new HashSet<>(Arrays.asList(
                              new CallDataDAOFilter(
                                     new HashSet<>(Arrays.asList(
                                             new HashSet<>(Arrays.asList(
                                                     new DAOExperimentCountFilter(
                                                             DAOExperimentCount.CallType.ABSENT, 
                                                             DAOExperimentCount.DataQuality.HIGH, 
                                                             DAOPropagationState.SELF, 
                                                             DAOExperimentCountFilter.Qualifier.EQUALS_TO, 
                                                             0))),
                                             new HashSet<>(Arrays.asList(
                                                     new DAOExperimentCountFilter(
                                                             DAOExperimentCount.CallType.PRESENT, 
                                                             DAOExperimentCount.DataQuality.HIGH, 
                                                             DAOPropagationState.ALL, 
                                                             DAOExperimentCountFilter.Qualifier.GREATER_THAN, 
                                                             1))),
                                             new HashSet<>(Arrays.asList(
                                                     new DAOExperimentCountFilter(
                                                             DAOExperimentCount.CallType.ABSENT, 
                                                             DAOExperimentCount.DataQuality.LOW, 
                                                             DAOPropagationState.SELF, 
                                                             DAOExperimentCountFilter.Qualifier.EQUALS_TO, 
                                                             0))),
                                             new HashSet<>(Arrays.asList(
                                                     new DAOExperimentCountFilter(
                                                             DAOExperimentCount.CallType.PRESENT,
                                                             DAOExperimentCount.DataQuality.LOW, 
                                                             DAOPropagationState.SELF, 
                                                             DAOExperimentCountFilter.Qualifier.GREATER_THAN, 
                                                             0),
                                                     new DAOExperimentCountFilter(
                                                             DAOExperimentCount.CallType.PRESENT, 
                                                             DAOExperimentCount.DataQuality.HIGH, 
                                                             DAOPropagationState.SELF, 
                                                             DAOExperimentCountFilter.Qualifier.GREATER_THAN, 
                                                             0)))
                                                 )),
                                      Arrays.asList(DAODataType.EST),
                                      null, new HashMap<>())))
              ))), 
      // condition parameters
      eq(new HashSet<>(Arrays.asList(ConditionDAO.Attribute.ANAT_ENTITY_ID, ConditionDAO.Attribute.STAGE_ID))),
      // global expression attributes
      eq(getAllGlobalExpressionCallDAOAttributes()),
      // ordering attributes
      eq(orderingAttrs));
    }
}