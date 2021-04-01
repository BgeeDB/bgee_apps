package org.bgee.model.expressiondata;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.TestAncestor;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.expressiondata.CallDAOFilter;
import org.bgee.model.dao.api.expressiondata.CallDataDAOFilter;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.ConditionDAO.ConditionTO;
import org.bgee.model.dao.api.expressiondata.ConditionDAO.ConditionTOResultSet;
import org.bgee.model.dao.api.expressiondata.ConditionDAO.ConditionRankInfoTO;
import org.bgee.model.dao.api.expressiondata.DAOConditionFilter;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.DAOExperimentCount;
import org.bgee.model.dao.api.expressiondata.DAOExperimentCountFilter;
import org.bgee.model.dao.api.expressiondata.DAOPropagationState;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO.EntityMinMaxRanksTO;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO.EntityMinMaxRanksTOResultSet;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO.GlobalExpressionCallDataTO;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO.GlobalExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO.GlobalExpressionCallTOResultSet;
import org.bgee.model.dao.api.gene.GeneDAO.GeneBioTypeTOResultSet;
import org.bgee.model.dao.api.gene.GeneDAO.GeneBioTypeTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTOResultSet;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.CallService.Attribute;
import org.bgee.model.expressiondata.CallService.OrderingAttribute;
import org.bgee.model.expressiondata.MultiGeneExprAnalysis.MultiGeneExprCounts;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.CallType.Expression;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.EntityMinMaxRanks;
import org.bgee.model.expressiondata.baseelements.ExperimentExpressionCount;
import org.bgee.model.expressiondata.baseelements.ExpressionLevelCategory;
import org.bgee.model.expressiondata.baseelements.ExpressionLevelInfo;
import org.bgee.model.expressiondata.baseelements.PropagationState;
import org.bgee.model.expressiondata.baseelements.QualitativeExpressionLevel;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.ontology.RelationType;
import org.bgee.model.species.Species;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Unit tests for {@link CallService}.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @author  Julien Wollbrett
 * @version Bgee 14, Feb. 2019
 * @since   Bgee 13, Nov. 2015
 */
public class CallServiceTest extends TestAncestor {

    private final static Logger log = LogManager.getLogger(CallServiceTest.class.getName());
        
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    private final Species spe1 = new Species(1);

    @Before
    public void configureMocks() {
        getLogger().entry();
        GeneBioTypeTOResultSet geneBioTypeTOResultSet = getMockResultSet(GeneBioTypeTOResultSet.class,
                Arrays.asList(new GeneBioTypeTO(1, "b")));
        when(this.geneDAO.getGeneBioTypes()).thenReturn(geneBioTypeTOResultSet);

        Map<Integer, Species> speciesById = new HashMap<>();
        speciesById.put(spe1.getId(), spe1);
        when(this.speciesService.loadSpeciesMap(new HashSet<>(Arrays.asList(spe1.getId())), false))
        .thenReturn(speciesById);

        ConditionRankInfoTO maxRankTO = new ConditionRankInfoTO(new BigDecimal("41025.00"), null);
        Map<Integer, ConditionRankInfoTO> maxRankBySpeciesId = new HashMap<>();
        maxRankBySpeciesId.put(spe1.getId(), maxRankTO);
        when(this.condDAO.getMaxRanks(anyCollectionOf(Integer.class), anyCollectionOf(DAODataType.class),
                anyCollectionOf(ConditionDAO.Attribute.class))).thenReturn(maxRankBySpeciesId);

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
                        new BigDecimal("1257.34"), new HashSet<>(Arrays.asList(
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
                                        0, new BigDecimal("99"), new BigDecimal("88"), new BigDecimal("77")),
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
                                        0, new BigDecimal("99"), new BigDecimal("88"), new BigDecimal("77"))))),
                   
                    new GlobalExpressionCallTO(2, bgeeGeneId1, cond2Id, 
                        new BigDecimal("125.00"), new HashSet<>(Arrays.asList(
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
                                        0, new BigDecimal("99"), new BigDecimal("88"), new BigDecimal("77")),
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
                                        0, new BigDecimal("99"), new BigDecimal("88"), new BigDecimal("77")),
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
                                        0, new BigDecimal("99"), new BigDecimal("88"), new BigDecimal("77"))))),
                    new GlobalExpressionCallTO(3, bgeeGeneId1, cond3Id, 
                            new BigDecimal("125.00"), new HashSet<>(Arrays.asList(
                                    new GlobalExpressionCallDataTO(DAODataType.AFFYMETRIX, true, 
                                            dataPropagation, new HashSet<>(Arrays.asList(
                                                    new DAOExperimentCount(
                                                            DAOExperimentCount.CallType.PRESENT, 
                                                            DAOExperimentCount.DataQuality.LOW, 
                                                            DAOPropagationState.ALL, 1))), 
                                            0, new BigDecimal("99"), new BigDecimal("88"), new BigDecimal("77")),
                                    new GlobalExpressionCallDataTO(DAODataType.EST, true,
                                            dataPropagation, new HashSet<>(Arrays.asList(
                                                    new DAOExperimentCount(
                                                            DAOExperimentCount.CallType.PRESENT, 
                                                            DAOExperimentCount.DataQuality.LOW, 
                                                            DAOPropagationState.ALL, 1))), 
                                            0, new BigDecimal("99"), new BigDecimal("88"), new BigDecimal("77")),
                                    new GlobalExpressionCallDataTO(DAODataType.IN_SITU, true, 
                                            dataPropagation, new HashSet<>(Arrays.asList(
                                                    new DAOExperimentCount(
                                                            DAOExperimentCount.CallType.PRESENT, 
                                                            DAOExperimentCount.DataQuality.LOW, 
                                                            DAOPropagationState.ALL, 1))), 
                                            0, new BigDecimal("99"), new BigDecimal("88"), new BigDecimal("77")))))
                    
                    ));

        
        //we'll do the verify afterwards, it's easier to catch a problem in the parameters
        when(this.globalExprCallDAO.getGlobalExpressionCalls(
                // CallDAOFilters
                anyCollectionOf(CallDAOFilter.class), 
                // condition parameters
                anyCollectionOf(ConditionDAO.Attribute.class),
                // global expression attributes
                anyCollectionOf(GlobalExpressionCallDAO.Attribute.class),
                // ordering attributes
                anyObject()))
        .thenReturn(resultSetMock);


        when(this.ontService.getAnatEntityOntology(spe1.getId(), new HashSet<>(Arrays.asList(
                "anatEntityId1")), EnumSet.of(RelationType.ISA_PARTOF), true, false))
        .thenReturn(this.anatEntityOnt);
        when(this.ontService.getDevStageOntology(spe1.getId(), new HashSet<>(Arrays.asList(
                "stageId1", "stageId2")), true, false)).thenReturn(this.devStageOnt);
        String anatEntityId1 = "anatEntityId1";
        AnatEntity anatEntity1 = new AnatEntity(anatEntityId1);
        String stageId1 = "stageId1";
        DevStage stage1 = new DevStage(stageId1);
        String stageId2 = "stageId2";
        DevStage stage2 = new DevStage(stageId2);
        String stageId3 = "stageId3";
        DevStage stage3 = new DevStage(stageId3);
        Gene g1 = new Gene("geneId1", spe1, new GeneBioType("b"));
        
        when(this.anatEntityService.loadAnatEntities(Collections.singleton(spe1.getId()), 
                true, new HashSet<String>(Arrays.asList(anatEntity1.getId())),false))
        .thenReturn(Stream.of(anatEntity1));
        when(this.devStageService.loadDevStages(Collections.singleton(spe1.getId()), 
                true, new HashSet<String>(Arrays.asList(
                        stage1.getId(), stage2.getId(), stage3.getId())),false))
        .thenReturn(Stream.of(stage1, stage2, stage3));

        when(this.anatEntityOnt.getElements()).thenReturn(new HashSet<>(Arrays.asList(anatEntity1)));
        when(this.devStageOnt.getElements()).thenReturn(new HashSet<>(Arrays.asList(stage1, stage2, stage3)));
        when(this.anatEntityOnt.getElement(anatEntityId1)).thenReturn(anatEntity1);
        when(this.devStageOnt.getElement(stageId1)).thenReturn(stage1);
        when(this.devStageOnt.getElement(stageId2)).thenReturn(stage2);
        when(this.devStageOnt.getElement(stageId3)).thenReturn(stage3);
        when(this.anatEntityOnt.getAncestors(anatEntity1)).thenReturn(new HashSet<>());
        when(this.devStageOnt.getAncestors(stage1)).thenReturn(new HashSet<>(Arrays.asList(stage2, stage3)));
        when(this.devStageOnt.getAncestors(stage2)).thenReturn(new HashSet<>(Arrays.asList(stage3)));
        when(this.devStageOnt.getAncestors(stage3)).thenReturn(new HashSet<>());
        when(this.anatEntityOnt.getAncestors(anatEntity1, false)).thenReturn(new HashSet<>());
        when(this.devStageOnt.getAncestors(stage1, false)).thenReturn(new HashSet<>(Arrays.asList(stage2, stage3)));
        when(this.devStageOnt.getAncestors(stage2, false)).thenReturn(new HashSet<>(Arrays.asList(stage3)));
        when(this.devStageOnt.getAncestors(stage3, false)).thenReturn(new HashSet<>());

        ConditionTOResultSet condTOResultSet = getMockResultSet(ConditionTOResultSet.class, Arrays.asList(
                new ConditionTO(1, anatEntity1.getId(), stage1.getId(),  null, null, null, spe1.getId(), null),
                new ConditionTO(2, anatEntity1.getId(), stage2.getId(),  null, null, null, spe1.getId(), null),
                new ConditionTO(3, anatEntity1.getId(), stage3.getId(),  null, null, null, spe1.getId(), null)));
        when(this.condDAO.getGlobalConditionsBySpeciesIds(eq(Collections.singleton(spe1.getId())), 
                eq(new HashSet<>(Arrays.asList(ConditionDAO.Attribute.ANAT_ENTITY_ID, 
                        ConditionDAO.Attribute.STAGE_ID))),
                anyObject())).thenReturn(condTOResultSet);
        
        GeneTOResultSet geneTOResultSet = getMockResultSet(GeneTOResultSet.class, Arrays.asList(
                new GeneTO(1, g1.getEnsemblGeneId(), g1.getName(), g1.getDescription(), g1.getSpecies().getId(), 
                        1, 1, true, 1)));
        Map<Integer, Set<String>> speciesIdToGeneIds = new HashMap<>();
        speciesIdToGeneIds.put(spe1.getId(), Collections.singleton(g1.getEnsemblGeneId()));
        when(this.geneDAO.getGenesBySpeciesAndGeneIds(speciesIdToGeneIds)).thenReturn(geneTOResultSet);


        List<ExpressionCall> expectedResults = Arrays.asList(
                new ExpressionCall(g1 , new Condition(anatEntity1, stage1, null, null, null, spe1), 
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
                                    0, new BigDecimal("99"), new BigDecimal("88"), new BigDecimal("77"),
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
                                    0, new BigDecimal("99"), new BigDecimal("88"), new BigDecimal("77"),
                                    new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)))), 
                        new ExpressionLevelInfo(new BigDecimal("1257.34"))),
                
                new ExpressionCall(g1, new Condition(anatEntity1, stage2, null, null, null, spe1), 
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
                                0, new BigDecimal("99"), new BigDecimal("88"), new BigDecimal("77"),
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
                                0, new BigDecimal("99"), new BigDecimal("88"), new BigDecimal("77"),
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
                                0, new BigDecimal("99"), new BigDecimal("88"), new BigDecimal("77"),
                                new DataPropagation(PropagationState.SELF, PropagationState.SELF, true))),
                    new ExpressionLevelInfo(new BigDecimal("125.00"))),
                
                new ExpressionCall(g1, new Condition(anatEntity1, stage3, null, null, null, spe1), 
                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true),
                        ExpressionSummary.EXPRESSED, SummaryQuality.SILVER, 
                        Arrays.asList(
                                new ExpressionCallData(DataType.AFFYMETRIX, new HashSet<>(Arrays.asList(
                                        new ExperimentExpressionCount(
                                                CallType.Expression.EXPRESSED, 
                                                DataQuality.LOW, 
                                                PropagationState.ALL, 1))),
                                        0, new BigDecimal("99"), new BigDecimal("88"), new BigDecimal("77"),
                                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)),
                                new ExpressionCallData(DataType.EST, new HashSet<>(Arrays.asList(
                                        new ExperimentExpressionCount(
                                                CallType.Expression.EXPRESSED, 
                                                DataQuality.LOW, 
                                                PropagationState.ALL, 1))),
                                        0, new BigDecimal("99"), new BigDecimal("88"), new BigDecimal("77"),
                                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)),
                                new ExpressionCallData(DataType.IN_SITU, new HashSet<>(Arrays.asList(
                                        new ExperimentExpressionCount(
                                                CallType.Expression.EXPRESSED, 
                                                DataQuality.LOW, 
                                                PropagationState.ALL, 1))),
                                        0, new BigDecimal("99"), new BigDecimal("88"), new BigDecimal("77"),
                                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true))), 
                        new ExpressionLevelInfo(new BigDecimal("125.00"))));
        
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = 
                new LinkedHashMap<>();
        serviceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.ASC);
        
        Set<CallService.Attribute> attrs = EnumSet.complementOf(
                EnumSet.of(Attribute.ANAT_ENTITY_QUAL_EXPR_LEVEL, Attribute.GENE_QUAL_EXPR_LEVEL, Attribute.EXPRESSION_SCORE));
        
        Map<ExpressionSummary, SummaryQuality> summaryCallTypeQualityFilter = new HashMap<>();
        summaryCallTypeQualityFilter.put(ExpressionSummary.EXPRESSED, SummaryQuality.SILVER);
        Map<Expression, Boolean> callObservedData = new HashMap<>();
        callObservedData.put(Expression.EXPRESSED, true);
        callObservedData.put(Expression.NOT_EXPRESSED, false);
        CallService service = new CallService(this.serviceFactory);
        List<ExpressionCall> actualResults = service.loadExpressionCalls(
                new ExpressionCallFilter(summaryCallTypeQualityFilter, 
                        Collections.singleton(
                                new GeneFilter(g1.getSpecies().getId(), g1.getEnsemblGeneId())),
                        null, null, callObservedData,
                    null, null, null, null, null), 
                attrs,
                serviceOrdering)
                .collect(Collectors.toList());

        assertCallsEquals(expectedResults, actualResults);
        
        LinkedHashMap<GlobalExpressionCallDAO.OrderingAttribute, DAO.Direction> orderingAttrs = 
                new LinkedHashMap<>();
        orderingAttrs.put(GlobalExpressionCallDAO.OrderingAttribute.MEAN_RANK, DAO.Direction.ASC);
        
        verify(this.globalExprCallDAO).getGlobalExpressionCalls(
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
//        ExpressionCallDAO exprDao = mock(ExpressionCallDAO.class);
//        when(this.manager.getExpressionCallDAO()).thenReturn(exprDao);
//        NoExpressionCallDAO noExprDao = mock(NoExpressionCallDAO.class);
//        when(this.manager.getNoExpressionCallDAO()).thenReturn(noExprDao);
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
//                        new BigDecimal("1257.34"), CallTO.DataState.LOWQUALITY, null,
//                        CallTO.DataState.HIGHQUALITY, null, CallTO.DataState.NODATA, null,
//                        CallTO.DataState.NODATA, null, false, false, ExpressionCallTO.OriginOfLine.SELF, 
//                        ExpressionCallTO.OriginOfLine.SELF, true), 
//                    new ExpressionCallTO(null, "geneId1", "anatEntityId1", "stageId2", 
//                        new BigDecimal("125.00"), CallTO.DataState.NODATA, null,
//                        CallTO.DataState.LOWQUALITY, null, CallTO.DataState.LOWQUALITY, null,
//                        CallTO.DataState.NODATA, null, false, false, ExpressionCallTO.OriginOfLine.SELF, 
//                        ExpressionCallTO.OriginOfLine.SELF, true), 
//                    new ExpressionCallTO(null, "geneId2", "anatEntityId2", "stageId2", 
//                        new BigDecimal("125.00"), CallTO.DataState.NODATA, null,
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
//        when(this.ontService.getAnatEntityOntology(Arrays.asList(1), new HashSet<>(Arrays.asList(
//                "anatEntityId1", "anatEntityId2")), EnumSet.of(RelationType.ISA_PARTOF), true, false))
//        .thenReturn(this.multiSpeAnatEntityOnt);
//        when(this.ontService.getDevStageOntology(Arrays.asList(1), new HashSet<>(Arrays.asList(
//                "stageId1", "stageId2")), true, false)).thenReturn(this.multiSpeDevStageOnt);
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
//        when(this.multiSpeAnatEntityOnt.getElements()).thenReturn(new HashSet<>(Arrays.asList(anatEntity1, anatEntity2)));
//        when(this.multiSpeAnatEntityOnt.getElement(anatEntityId1)).thenReturn(anatEntity1);
//        when(this.multiSpeAnatEntityOnt.getElement(anatEntityId2)).thenReturn(anatEntity2);
//        when(this.multiSpeAnatEntityOnt.getAncestors(anatEntity1)).thenReturn(new HashSet<>());
//        when(this.multiSpeAnatEntityOnt.getAncestors(anatEntity1, false)).thenReturn(new HashSet<>());
//        when(this.multiSpeAnatEntityOnt.getAncestors(anatEntity2)).thenReturn(new HashSet<>(Arrays.asList(anatEntity1)));
//        when(this.multiSpeAnatEntityOnt.getAncestors(anatEntity2, false)).thenReturn(new HashSet<>(Arrays.asList(anatEntity1)));
//        when(this.multiSpeAnatEntityOnt.getDescendants(anatEntity1)).thenReturn(new HashSet<>(Arrays.asList(anatEntity2)));
//        when(this.multiSpeAnatEntityOnt.getDescendants(anatEntity1, false)).thenReturn(new HashSet<>(Arrays.asList(anatEntity2)));
//
//        when(this.multiSpeDevStageOnt.getElements()).thenReturn(new HashSet<>(Arrays.asList(stage1, stage2, stage3)));
//        when(this.multiSpeDevStageOnt.getElement(stageId1)).thenReturn(stage1);
//        when(this.multiSpeDevStageOnt.getElement(stageId2)).thenReturn(stage2);
//        when(this.multiSpeDevStageOnt.getElement(stageId3)).thenReturn(stage3);
//        when(this.multiSpeDevStageOnt.getAncestors(stage1)).thenReturn(new HashSet<>(Arrays.asList(stage2, stage3)));
//        when(this.multiSpeDevStageOnt.getAncestors(stage2)).thenReturn(new HashSet<>(Arrays.asList(stage3)));
//        when(this.multiSpeDevStageOnt.getAncestors(stage3)).thenReturn(new HashSet<>());
//        when(this.multiSpeDevStageOnt.getAncestors(stage1, false)).thenReturn(new HashSet<>(Arrays.asList(stage2, stage3)));
//        when(this.multiSpeDevStageOnt.getAncestors(stage2, false)).thenReturn(new HashSet<>(Arrays.asList(stage3)));
//        when(this.multiSpeDevStageOnt.getAncestors(stage3, false)).thenReturn(new HashSet<>());
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
//                new BigDecimal("1257.34")),
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
//                new BigDecimal("125.00"))
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
//                new BigDecimal("125.00")),
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
//        CallService service = new CallService(this.serviceFactory);
//        List<ExpressionCall> actualResults = service.loadExpressionCalls("speciesId1", 
//                new ExpressionCallFilter(null, null, null, null, null, new DataPropagation()),
//                null, // all attributes 
//                serviceOrdering)
//                .collect(Collectors.toList());
//        assertCallsEqual(expectedResults, actualResults);
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
     * Collection, LinkedHashMap, boolean)}. Test notably with computation of expression scores.
     */    
    // Keep in this test empty stream for no-expression calls:
    // it allows to detect if all calls are read when only one query has calls
    @Test
    public void shouldLoadExpressionCallsForSeveralGenes() {
        //Retrieving geneId, anatEntityId, unordered, with substructures but without sub-stages.
        
        GeneTO gTO1 = new GeneTO(1, "gene1", "geneName1", spe1.getId());
        GeneTO gTO2 = new GeneTO(2, "gene2", "geneName2", spe1.getId());
        Gene g1 = new Gene(gTO1.getGeneId(), gTO1.getName(), null, null, null, spe1, new GeneBioType("b"), 1);
        Gene g2 = new Gene(gTO2.getGeneId(), gTO2.getName(), null, null, null, spe1, new GeneBioType("b"), 1);
        
        AnatEntity anatEntity1 = new AnatEntity("anatEntity1");
        AnatEntity anatEntity2 = new AnatEntity("anatEntity2");
        DevStage devStage1 = new DevStage("devStage1");
        DevStage devStage2 = new DevStage("devStage2");
        ConditionTO condTO1 = new ConditionTO(1, anatEntity1.getId(), devStage1.getId(), null, null, 
                null, spe1.getId(), null);
        ConditionTO condTO2 = new ConditionTO(2, anatEntity1.getId(), devStage2.getId(), null, 
                null, null, spe1.getId(), null);
        ConditionTO condTO3 = new ConditionTO(3, anatEntity2.getId(), devStage1.getId(), null, 
                null, null, spe1.getId(), null);
        Condition cond1 = new Condition(anatEntity1, devStage1, null, null, null, spe1);
        Condition cond2 = new Condition(anatEntity1, devStage2, null, null, null, spe1);
        Condition cond3 = new Condition(anatEntity2, devStage1, null, null, null, spe1);
        
        GeneTOResultSet geneTOResultSet = getMockResultSet(GeneTOResultSet.class, Arrays.asList(
                new GeneTO(gTO1.getId(), gTO1.getGeneId(), gTO1.getName(), gTO1.getDescription(), gTO1.getSpeciesId(), 
                        1, 1, true, 1),
                new GeneTO(gTO2.getId(), gTO2.getGeneId(), gTO2.getName(), gTO2.getDescription(), gTO2.getSpeciesId(), 
                        1, 1, true, 1)));
        Map<Integer, Set<String>> speciesIdToGeneIds = new HashMap<>();
        speciesIdToGeneIds.put(spe1.getId(), 
                new HashSet<>(Arrays.asList(g1.getEnsemblGeneId(), g2.getEnsemblGeneId())));
        when(this.geneDAO.getGenesBySpeciesAndGeneIds(speciesIdToGeneIds)).thenReturn(geneTOResultSet);
                
        Map<ConditionDAO.Attribute, DAOPropagationState> dataPropagation = new HashMap<>();
        dataPropagation.put(ConditionDAO.Attribute.ANAT_ENTITY_ID, DAOPropagationState.SELF);
        dataPropagation.put(ConditionDAO.Attribute.STAGE_ID, DAOPropagationState.SELF);
        ConditionTOResultSet condTOResultSet = getMockResultSet(ConditionTOResultSet.class, Arrays.asList(
                new ConditionTO(1, anatEntity1.getId(), devStage1.getId(), null, null, null, spe1.getId(), null),
                new ConditionTO(2, anatEntity1.getId(), devStage2.getId(), null, null, null, spe1.getId(), null),
                new ConditionTO(3, anatEntity2.getId(), devStage1.getId(), null, null, null, spe1.getId(), null)));
        when(this.condDAO.getGlobalConditionsBySpeciesIds(eq(Collections.singleton(spe1.getId())), 
                eq(new HashSet<>(Arrays.asList(ConditionDAO.Attribute.ANAT_ENTITY_ID, 
                        ConditionDAO.Attribute.STAGE_ID))),
                anyObject())).thenReturn(condTOResultSet);
        
        GlobalExpressionCallTOResultSet resultSetMock = getMockResultSet(GlobalExpressionCallTOResultSet.class, 
                Arrays.asList(
                        new GlobalExpressionCallTO(1, gTO1.getId(), condTO1.getId(), 
                                new BigDecimal("1257.34"), new HashSet<>(Arrays.asList(
                                        new GlobalExpressionCallDataTO(DAODataType.AFFYMETRIX, true, 
                                                dataPropagation , new HashSet<>(Arrays.asList(
                                                       new DAOExperimentCount(
                                                               DAOExperimentCount.CallType.PRESENT, 
                                                               DAOExperimentCount.DataQuality.LOW, 
                                                               DAOPropagationState.ALL, 1))), 
                                                0, new BigDecimal("99"), new BigDecimal("88"), new BigDecimal("77"))))),
                        new GlobalExpressionCallTO(2, gTO1.getId(), condTO2.getId(), 
                                new BigDecimal("41025"), new HashSet<>(Arrays.asList(
                                        new GlobalExpressionCallDataTO(DAODataType.EST, true, 
                                                dataPropagation , new HashSet<>(Arrays.asList(
                                                        new DAOExperimentCount(
                                                                DAOExperimentCount.CallType.PRESENT, 
                                                                DAOExperimentCount.DataQuality.LOW, 
                                                                DAOPropagationState.ALL, 2))), 
                                                0, new BigDecimal("99"), new BigDecimal("88"), new BigDecimal("77"))))),
                        new GlobalExpressionCallTO(3, gTO2.getId(), condTO3.getId(), 
                                new BigDecimal("20000.52"), new HashSet<>(Arrays.asList(
                                        new GlobalExpressionCallDataTO(DAODataType.EST, true, 
                                                dataPropagation , new HashSet<>(Arrays.asList(
                                                        new DAOExperimentCount(
                                                                DAOExperimentCount.CallType.PRESENT, 
                                                                DAOExperimentCount.DataQuality.LOW, 
                                                                DAOPropagationState.ALL, 1))), 
                                                0, new BigDecimal("99"), new BigDecimal("88"), new BigDecimal("77")))))));

        //we'll do the verify afterwards, it's easier to catch a problem in the parameters
        when(this.globalExprCallDAO.getGlobalExpressionCalls(
                // CallDAOFilters
                anyCollectionOf(CallDAOFilter.class), 
                // condition parameters
                anyCollectionOf(ConditionDAO.Attribute.class),
                // global expression attributes
                anyCollectionOf(GlobalExpressionCallDAO.Attribute.class),
                // ordering attributes
                anyObject()))
        .thenReturn(resultSetMock);

        when(this.ontService.getAnatEntityOntology(spe1.getId(), Arrays.asList(
                anatEntity1.getId(), anatEntity2.getId()), 
                EnumSet.of(RelationType.ISA_PARTOF), true, false))
            .thenReturn(this.anatEntityOnt);
        when(this.ontService.getDevStageOntology(spe1.getId(), Arrays.asList(
                devStage1.getId(), devStage2.getId()), true, false)).thenReturn(this.devStageOnt);
        
        when(this.anatEntityService.loadAnatEntities(Collections.singleton(spe1.getId()), 
                true, new HashSet<String>(Arrays.asList(
                        anatEntity1.getId(), anatEntity2.getId())),false))
        .thenReturn(Stream.of(anatEntity1, anatEntity2));
        when(this.devStageService.loadDevStages(Collections.singleton(spe1.getId()), 
                true, new HashSet<String>(Arrays.asList(
                        devStage1.getId(), devStage2.getId())),false))
        .thenReturn(Stream.of(devStage1, devStage2));

        when(this.anatEntityOnt.getElements()).thenReturn(new HashSet<>(Arrays.asList(anatEntity1, anatEntity2)));
        when(this.devStageOnt.getElements()).thenReturn(new HashSet<>(Arrays.asList(devStage1, devStage2)));
        when(this.anatEntityOnt.getElement(anatEntity1.getId())).thenReturn(anatEntity1);
        when(this.anatEntityOnt.getElement(anatEntity2.getId())).thenReturn(anatEntity2);
        when(this.devStageOnt.getElement(devStage1.getId())).thenReturn(devStage1);
        when(this.devStageOnt.getElement(devStage2.getId())).thenReturn(devStage2);
        when(this.anatEntityOnt.getAncestors(anatEntity1)).thenReturn(new HashSet<>(Arrays.asList(anatEntity2)));
        when(this.anatEntityOnt.getAncestors(anatEntity2)).thenReturn(new HashSet<>());
        when(this.devStageOnt.getAncestors(devStage1)).thenReturn(new HashSet<>(Arrays.asList(devStage2)));
        when(this.devStageOnt.getAncestors(devStage2)).thenReturn(new HashSet<>());
        when(this.anatEntityOnt.getAncestors(anatEntity1, false)).thenReturn(new HashSet<>(Arrays.asList(anatEntity2)));
        when(this.anatEntityOnt.getAncestors(anatEntity2, false)).thenReturn(new HashSet<>());
        when(this.devStageOnt.getAncestors(devStage1, false)).thenReturn(new HashSet<>(Arrays.asList(devStage2)));
        when(this.devStageOnt.getAncestors(devStage2, false)).thenReturn(new HashSet<>());

        List<ExpressionCall> expectedResults = Arrays.asList(
                new ExpressionCall(g1, cond1, 
                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true),
                        ExpressionSummary.EXPRESSED, SummaryQuality.BRONZE, 
                        new HashSet<ExpressionCallData>(Arrays.asList(
                            new ExpressionCallData(DataType.AFFYMETRIX, new HashSet<>(Arrays.asList(
                                    new ExperimentExpressionCount(
                                            CallType.Expression.EXPRESSED, DataQuality.LOW, PropagationState.ALL, 1)
                                    )),
                                    0, new BigDecimal("99"), new BigDecimal("88"), new BigDecimal("77"),
                                  new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)))), 
                        new ExpressionLevelInfo(new BigDecimal("1257.34"), new BigDecimal("96.93762"),
                                new BigDecimal("41025"), null, null)),
                new ExpressionCall(g1, cond2, 
                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true),
                        ExpressionSummary.EXPRESSED, SummaryQuality.SILVER, 
                        new HashSet<ExpressionCallData>(Arrays.asList(
                            new ExpressionCallData(DataType.EST, new HashSet<>(Arrays.asList(
                                    new ExperimentExpressionCount(
                                            CallType.Expression.EXPRESSED, DataQuality.LOW, PropagationState.ALL, 2)
                                    )),
                                    0, new BigDecimal("99"), new BigDecimal("88"), new BigDecimal("77"),
                                  new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)))), 
                        new ExpressionLevelInfo(new BigDecimal("41025"), CallService.EXPRESSION_SCORE_MIN_VALUE,
                                new BigDecimal("41025"), null, null)),
                new ExpressionCall(g2, cond3, 
                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true),
                        ExpressionSummary.EXPRESSED, SummaryQuality.BRONZE, 
                        new HashSet<ExpressionCallData>(Arrays.asList(
                            new ExpressionCallData(DataType.EST, new HashSet<>(Arrays.asList(
                                    new ExperimentExpressionCount(
                                            CallType.Expression.EXPRESSED, DataQuality.LOW, PropagationState.ALL, 1)
                                    )),
                                    0, new BigDecimal("99"), new BigDecimal("88"), new BigDecimal("77"),
                                  new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)))), 
                        new ExpressionLevelInfo(new BigDecimal("20000.52"), new BigDecimal("51.25041"),
                                new BigDecimal("41025"), null, null))
            );
        
        Map<ExpressionSummary, SummaryQuality> summaryCallTypeQualityFilter = new HashMap<>();
        summaryCallTypeQualityFilter.put(ExpressionSummary.EXPRESSED, SummaryQuality.BRONZE);
        
        Map<Expression, Boolean> callObservedData = new HashMap<>();
        callObservedData.put(Expression.EXPRESSED, true);
        callObservedData.put(Expression.NOT_EXPRESSED, false);
        
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = 
                new LinkedHashMap<>();
        serviceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.ASC);
        
        Set<CallService.Attribute> attrs = EnumSet.complementOf(
                EnumSet.of(Attribute.ANAT_ENTITY_QUAL_EXPR_LEVEL, Attribute.GENE_QUAL_EXPR_LEVEL));
        
        CallService service = new CallService(this.serviceFactory);
        
        List<ExpressionCall> actualResults = service.loadExpressionCalls(
                new ExpressionCallFilter(summaryCallTypeQualityFilter, 
                        Collections.singleton(
                                new GeneFilter(g1.getSpecies().getId(), 
                                        Arrays.asList(g1.getEnsemblGeneId(), g2.getEnsemblGeneId()))),
                        null, null, callObservedData,
                    null, null, null, null, null), 
                attrs,
                serviceOrdering)
                .collect(Collectors.toList());
        
        
        assertCallsEquals(expectedResults, actualResults);
        
        LinkedHashMap<GlobalExpressionCallDAO.OrderingAttribute, DAO.Direction> orderingAttrs = 
                new LinkedHashMap<>();
        orderingAttrs.put(GlobalExpressionCallDAO.OrderingAttribute.MEAN_RANK, DAO.Direction.ASC);
        
        verify(this.globalExprCallDAO).getGlobalExpressionCalls(
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
        
        GeneTO gTO1 = new GeneTO(1, "gene1", "geneName1", spe1.getId());
        Gene g1 = new Gene(gTO1.getGeneId(), gTO1.getName(), null, null, null, spe1, new GeneBioType("b"), 1);
        
        AnatEntity anatEntity1 = new AnatEntity("anatEntity1");
        DevStage devStage1 = new DevStage("devStage1");
        ConditionTO condTO1 = new ConditionTO(1, anatEntity1.getId(), devStage1.getId(), null, null, null, spe1.getId(), null);
        Condition cond1 = new Condition(anatEntity1, devStage1, null, null, null, spe1);
        
        Map<ConditionDAO.Attribute, DAOPropagationState> dataPropagation = new HashMap<>();
        dataPropagation.put(ConditionDAO.Attribute.ANAT_ENTITY_ID, DAOPropagationState.SELF);
        dataPropagation.put(ConditionDAO.Attribute.STAGE_ID, DAOPropagationState.SELF);
        
        GlobalExpressionCallTOResultSet resultSetMock = getMockResultSet(GlobalExpressionCallTOResultSet.class, 
                Arrays.asList(
                        new GlobalExpressionCallTO(3, gTO1.getId(), condTO1.getId(), 
                                new BigDecimal("125.42"), new HashSet<>(Arrays.asList(
                                        new GlobalExpressionCallDataTO(DAODataType.EST, true, 
                                                dataPropagation , new HashSet<>(Arrays.asList(
                                                        new DAOExperimentCount(
                                                                DAOExperimentCount.CallType.PRESENT, 
                                                                DAOExperimentCount.DataQuality.HIGH, 
                                                                DAOPropagationState.ALL, 3))), 
                                                0, new BigDecimal("99"), new BigDecimal("88"), new BigDecimal("77"))
                                        )))
                        ));
        
        //we'll do the verify afterwards, it's easier to catch a problem in the parameters
        when(this.globalExprCallDAO.getGlobalExpressionCalls(
                // CallDAOFilters
                anyCollectionOf(CallDAOFilter.class), 
                // condition parameters
                anyCollectionOf(ConditionDAO.Attribute.class),
                // global expression attributes
                anyCollectionOf(GlobalExpressionCallDAO.Attribute.class),
                // ordering attributes
                anyObject()))
        .thenReturn(resultSetMock);

        when(this.ontService.getAnatEntityOntology(spe1.getId(), new HashSet<>(Arrays.asList(
                "anatEntityId1")), EnumSet.of(RelationType.ISA_PARTOF), false, false))
        .thenReturn(this.anatEntityOnt);
        when(this.ontService.getDevStageOntology(spe1.getId(), new HashSet<>(Arrays.asList(
                "stageId1", "stageId2")), false, false)).thenReturn(this.devStageOnt);
        
        when(this.anatEntityService.loadAnatEntities(Collections.singleton(spe1.getId()), 
                true, new HashSet<String>(Arrays.asList(
                        anatEntity1.getId())),false))
        .thenReturn(Stream.of(anatEntity1));
        when(this.devStageService.loadDevStages(Collections.singleton(spe1.getId()), 
                true, new HashSet<String>(Arrays.asList(
                        devStage1.getId())),false))
        .thenReturn(Stream.of(devStage1));

        when(this.anatEntityOnt.getElements()).thenReturn(new HashSet<>(Arrays.asList(anatEntity1)));
        when(this.devStageOnt.getElements()).thenReturn(new HashSet<>(Arrays.asList(devStage1)));
        when(this.anatEntityOnt.getElement(anatEntity1.getId())).thenReturn(anatEntity1);
        when(this.devStageOnt.getElement(devStage1.getId())).thenReturn(devStage1);
        // No relation to not propagate calls
        when(this.anatEntityOnt.getAncestors(anatEntity1, true)).thenReturn(new HashSet<>());
        when(this.devStageOnt.getAncestors(devStage1, true)).thenReturn(new HashSet<>());

        GeneTOResultSet geneTOResultSet = getMockResultSet(GeneTOResultSet.class, Arrays.asList(
                new GeneTO(1, g1.getEnsemblGeneId(), g1.getName(), g1.getDescription(), g1.getSpecies().getId(), 
                        1, 1, true, 1)));
        Map<Integer, Set<String>> speciesIdToGeneIds = new HashMap<>();
        speciesIdToGeneIds.put(spe1.getId(), Collections.singleton(g1.getEnsemblGeneId()));
        when(this.geneDAO.getGenesBySpeciesAndGeneIds(speciesIdToGeneIds)).thenReturn(geneTOResultSet);

        ConditionTOResultSet condTOResultSet = 
                getMockResultSet(ConditionTOResultSet.class, Arrays.asList(condTO1));
        when(this.condDAO.getGlobalConditionsBySpeciesIds(eq(Collections.singleton(spe1.getId())), 
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
                                    0, new BigDecimal("99"), new BigDecimal("88"), new BigDecimal("77"),
                                  new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)))), 
                        new ExpressionLevelInfo(new BigDecimal("125.42"))));


        
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = 
                new LinkedHashMap<>();
        serviceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.ASC);

        Map<ExpressionSummary, SummaryQuality> summaryCallTypeQualityFilter = new HashMap<>();
        summaryCallTypeQualityFilter.put(ExpressionSummary.EXPRESSED, SummaryQuality.GOLD);
        
        Map<Expression, Boolean> callObservedData = new HashMap<>();
        callObservedData.put(Expression.EXPRESSED, true);
        callObservedData.put(Expression.NOT_EXPRESSED, false);
        
        Set<CallService.Attribute> attrs = EnumSet.complementOf(
                EnumSet.of(Attribute.ANAT_ENTITY_QUAL_EXPR_LEVEL, Attribute.GENE_QUAL_EXPR_LEVEL, Attribute.EXPRESSION_SCORE));
        
        CallService service = new CallService(this.serviceFactory);
        
        List<ExpressionCall> actualResults = service.loadExpressionCalls(
                new ExpressionCallFilter(summaryCallTypeQualityFilter, 
                        Collections.singleton(
                                new GeneFilter(spe1.getId(), g1.getEnsemblGeneId())),
                        Collections.singleton(
                                new ConditionFilter(Collections.singleton(anatEntity1.getId()), 
                                        Collections.singleton(devStage1.getId()), null, null, null, null)), 
                        Collections.singleton(
                                DataType.EST), 
                        callObservedData,
                    null, null, null, null, null), 
                attrs,
                serviceOrdering)
                .collect(Collectors.toList());
        
        assertCallsEquals(expectedResults, actualResults);
        
        
        LinkedHashMap<GlobalExpressionCallDAO.OrderingAttribute, DAO.Direction> orderingAttrs = 
                new LinkedHashMap<>();
        orderingAttrs.put(GlobalExpressionCallDAO.OrderingAttribute.MEAN_RANK, DAO.Direction.ASC);
        
        verify(this.globalExprCallDAO).getGlobalExpressionCalls(
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

    /**
     * Test the method {@link CallService#loadExpressionCalls(String, ExpressionCallFilter, 
     * Collection, LinkedHashMap, boolean)} when quantitative expression levels are requested.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void shouldLoadCallsWithQuantExprLevels() {
        //2 genes, 2 organs, 1 stage, 2 conditions
        GeneTO gTO1 = new GeneTO(1, "gene1", "geneName1", spe1.getId());
        Gene g1 = new Gene(gTO1.getGeneId(), gTO1.getName(), null, null, null, spe1, new GeneBioType("b"), 1);
        GeneTO gTO2 = new GeneTO(2, "gene2", "geneName2", spe1.getId());
        Gene g2 = new Gene(gTO2.getGeneId(), gTO2.getName(), null, null, null, spe1, new GeneBioType("b"), 1);
        AnatEntity anatEntity1 = new AnatEntity("anatEntity1");
        AnatEntity anatEntity2 = new AnatEntity("anatEntity2");
        DevStage devStage1 = new DevStage("devStage1");
        ConditionTO condTO1 = new ConditionTO(1, anatEntity1.getId(), devStage1.getId(), null, null, null, spe1.getId(), null);
        ConditionTO condTO2 = new ConditionTO(2, anatEntity2.getId(), devStage1.getId(), null, null, null, spe1.getId(), null);
        Condition cond1 = new Condition(anatEntity1, devStage1, null, null, null, spe1);
        Condition cond2 = new Condition(anatEntity2, devStage1, null, null, null, spe1);

        //Mock services and objects
        when(this.ontService.getAnatEntityOntology(eq(spe1.getId()), anyCollectionOf(String.class),
                eq(EnumSet.of(RelationType.ISA_PARTOF)), eq(false), eq(false)))
        .thenReturn(this.anatEntityOnt);
        when(this.ontService.getDevStageOntology(spe1.getId(), new HashSet<>(Arrays.asList(
                "stageId1")), false, false)).thenReturn(this.devStageOnt);
        when(this.anatEntityService.loadAnatEntities(Collections.singleton(spe1.getId()), 
                true, new HashSet<String>(Arrays.asList(
                        anatEntity1.getId())),false))
        .thenAnswer(
                new Answer<Stream<AnatEntity>>() {
                    public Stream<AnatEntity> answer(InvocationOnMock invocation) {
                        return Stream.of(anatEntity1);
                    }
                });
        when(this.anatEntityService.loadAnatEntities(Collections.singleton(spe1.getId()), 
                true, new HashSet<String>(Arrays.asList(
                        anatEntity1.getId(), anatEntity2.getId())),false))
        .thenAnswer(
                new Answer<Stream<AnatEntity>>() {
                    public Stream<AnatEntity> answer(InvocationOnMock invocation) {
                        return Stream.of(anatEntity1, anatEntity2);
                    }
                });
        when(this.devStageService.loadDevStages(Collections.singleton(spe1.getId()), 
                true, new HashSet<String>(Arrays.asList(
                        devStage1.getId())),false))
        .thenAnswer(
                new Answer<Stream<DevStage>>() {
                    public Stream<DevStage> answer(InvocationOnMock invocation) {
                        return Stream.of(devStage1);
                    }
                });
        when(this.anatEntityOnt.getElements()).thenReturn(new HashSet<>(Arrays.asList(anatEntity1, anatEntity2)));
        when(this.devStageOnt.getElements()).thenReturn(new HashSet<>(Arrays.asList(devStage1)));
        when(this.anatEntityOnt.getElement(anatEntity1.getId())).thenReturn(anatEntity1);
        when(this.anatEntityOnt.getElement(anatEntity2.getId())).thenReturn(anatEntity2);
        when(this.devStageOnt.getElement(devStage1.getId())).thenReturn(devStage1);
        // No relation to not propagate calls
        when(this.anatEntityOnt.getAncestors(anatEntity1, true)).thenReturn(new HashSet<>());
        when(this.anatEntityOnt.getAncestors(anatEntity2, true)).thenReturn(new HashSet<>());
        when(this.devStageOnt.getAncestors(devStage1, true)).thenReturn(new HashSet<>());

        Map<Integer, Set<String>> speciesIdToGeneIds = new HashMap<>();
        speciesIdToGeneIds.put(spe1.getId(), Collections.singleton(g1.getEnsemblGeneId()));
        when(this.geneDAO.getGenesBySpeciesAndGeneIds(speciesIdToGeneIds)).thenAnswer(
                new Answer<GeneTOResultSet>() {
                    public GeneTOResultSet answer(InvocationOnMock invocation) {
                        return getMockResultSet(GeneTOResultSet.class, Arrays.asList(
                                new GeneTO(gTO1.getId(), gTO1.getGeneId(), gTO1.getName(), gTO1.getDescription(), gTO1.getSpeciesId(), 
                                        1, 1, true, 1)));
                    }
                });
        speciesIdToGeneIds = new HashMap<>();
        speciesIdToGeneIds.put(spe1.getId(), new HashSet<>());
        when(this.geneDAO.getGenesBySpeciesAndGeneIds(speciesIdToGeneIds)).thenAnswer(
                new Answer<GeneTOResultSet>() {
                    public GeneTOResultSet answer(InvocationOnMock invocation) {
                        return getMockResultSet(GeneTOResultSet.class, Arrays.asList(
                                new GeneTO(gTO1.getId(), gTO1.getGeneId(), gTO1.getName(), gTO1.getDescription(), gTO1.getSpeciesId(), 
                                        1, 1, true, 1),
                                new GeneTO(gTO2.getId(), gTO2.getGeneId(), gTO2.getName(), gTO2.getDescription(), gTO2.getSpeciesId(), 
                                        1, 1, true, 1)));
                    }
                });

        when(this.condDAO.getGlobalConditionsBySpeciesIds(Collections.singleton(spe1.getId()), 
                EnumSet.of(ConditionDAO.Attribute.ANAT_ENTITY_ID, 
                        ConditionDAO.Attribute.STAGE_ID),
                EnumSet.of(ConditionDAO.Attribute.ANAT_ENTITY_ID, 
                        ConditionDAO.Attribute.STAGE_ID, ConditionDAO.Attribute.ID,
                        ConditionDAO.Attribute.SPECIES_ID))).thenAnswer(
                                new Answer<ConditionTOResultSet>() {
                                    public ConditionTOResultSet answer(InvocationOnMock invocation) {
                                        return getMockResultSet(ConditionTOResultSet.class,
                                                Arrays.asList(condTO1, condTO2));
                                    }
                                });

        GlobalExpressionCallDataTO presentCallDataTO = new GlobalExpressionCallDataTO(DAODataType.RNA_SEQ,
                true, null,
                Collections.singleton(new DAOExperimentCount(DAOExperimentCount.CallType.PRESENT,
                        DAOExperimentCount.DataQuality.HIGH, DAOPropagationState.ALL, 1)),
                0, null, null, null);
        GlobalExpressionCallDataTO absentCallDataTO = new GlobalExpressionCallDataTO(DAODataType.RNA_SEQ,
                true, null,
                Collections.singleton(new DAOExperimentCount(DAOExperimentCount.CallType.ABSENT,
                        DAOExperimentCount.DataQuality.HIGH, DAOPropagationState.ALL, 1)),
                0, null, null, null);
        //To mock the returned GlobalExpressionCallTOResultSets
        //4 calls: 2 genes in 2 organs
        List<GlobalExpressionCallTO> callTOs = Arrays.asList(
                //cond1: ranks from 10 to 10000
                //cond2: ranks from 20000 to 30000
                //gene1: ranks from 10 to 30000
                //gene2: ranks from 10000 to 20000
                //gene1 - cond1: gene HIGH, anat HIGH
                //gene1 - cond2: gene LOW, anat LOW
                //gene2 - cond1: gene HIGH, anat LOW
                //gene2 - cond2: gene LOW, anat HIGH
                new GlobalExpressionCallTO(1, gTO1.getId(), condTO1.getId(), 
                        new BigDecimal("10"), Collections.singleton(presentCallDataTO)),
                new GlobalExpressionCallTO(2, gTO1.getId(), condTO2.getId(), 
                        new BigDecimal("30000"), Collections.singleton(presentCallDataTO)),
                new GlobalExpressionCallTO(3, gTO2.getId(), condTO1.getId(), 
                        new BigDecimal("10000"), Collections.singleton(presentCallDataTO)),
                new GlobalExpressionCallTO(4, gTO2.getId(), condTO2.getId(), 
                        new BigDecimal("20000"), Collections.singleton(presentCallDataTO))
                );
        //2 calls, one with absent call
        List<GlobalExpressionCallTO> callTOsWithAbsentCall = Arrays.asList(
                new GlobalExpressionCallTO(1, gTO1.getId(), condTO1.getId(),
                        new BigDecimal("10"), Collections.singleton(presentCallDataTO)),
                new GlobalExpressionCallTO(2, gTO1.getId(), condTO2.getId(),
                        new BigDecimal("500000"), Collections.singleton(absentCallDataTO)));

        //To mock the returned min./max ranks
        EntityMinMaxRanksTO<Integer> g1MinMax = new EntityMinMaxRanksTO<>(gTO1.getId(),
                new BigDecimal("10"), new BigDecimal("30000"), null);
        EntityMinMaxRanksTO<Integer> g2MinMax = new EntityMinMaxRanksTO<>(gTO2.getId(),
                new BigDecimal("10000"), new BigDecimal("20000"), null);
        EntityMinMaxRanksTO<String> ae1MinMax = new EntityMinMaxRanksTO<>(anatEntity1.getId(),
                new BigDecimal("10"), new BigDecimal("10000"), null);
        EntityMinMaxRanksTO<String> ae2MinMax = new EntityMinMaxRanksTO<>(anatEntity2.getId(),
                new BigDecimal("20000"), new BigDecimal("30000"), null);

        //Create the objects used for calling the GlobalExpressionCallDAO
        DAOExperimentCountFilter presentLowAllCountFilter = new DAOExperimentCountFilter(
                DAOExperimentCount.CallType.PRESENT, DAOExperimentCount.DataQuality.LOW,
                DAOPropagationState.ALL, DAOExperimentCountFilter.Qualifier.GREATER_THAN, 0);
        DAOExperimentCountFilter presentHighAllCountFilter = new DAOExperimentCountFilter(
                DAOExperimentCount.CallType.PRESENT, DAOExperimentCount.DataQuality.HIGH,
                DAOPropagationState.ALL, DAOExperimentCountFilter.Qualifier.GREATER_THAN, 0);
        DAOExperimentCountFilter presentForGoldAllCountFilter = new DAOExperimentCountFilter(
                DAOExperimentCount.CallType.PRESENT, DAOExperimentCount.DataQuality.HIGH,
                DAOPropagationState.ALL, DAOExperimentCountFilter.Qualifier.GREATER_THAN, 1);
        DAOExperimentCountFilter absentForGoldAllCountFilter = new DAOExperimentCountFilter(
                DAOExperimentCount.CallType.ABSENT, DAOExperimentCount.DataQuality.HIGH,
                DAOPropagationState.ALL, DAOExperimentCountFilter.Qualifier.GREATER_THAN, 1);
        DAOExperimentCountFilter presentLowSelfCountFilter = new DAOExperimentCountFilter(
                DAOExperimentCount.CallType.PRESENT, DAOExperimentCount.DataQuality.LOW,
                DAOPropagationState.SELF, DAOExperimentCountFilter.Qualifier.GREATER_THAN, 0);
        DAOExperimentCountFilter presentHighSelfCountFilter = new DAOExperimentCountFilter(
                DAOExperimentCount.CallType.PRESENT, DAOExperimentCount.DataQuality.HIGH,
                DAOPropagationState.SELF, DAOExperimentCountFilter.Qualifier.GREATER_THAN, 0);
        DAOExperimentCountFilter noPresentHighAllCountFilter = new DAOExperimentCountFilter(
                DAOExperimentCount.CallType.PRESENT, DAOExperimentCount.DataQuality.HIGH,
                DAOPropagationState.ALL, DAOExperimentCountFilter.Qualifier.EQUALS_TO, 0);
        DAOExperimentCountFilter noPresentLowAllCountFilter = new DAOExperimentCountFilter(
                DAOExperimentCount.CallType.PRESENT, DAOExperimentCount.DataQuality.LOW,
                DAOPropagationState.ALL, DAOExperimentCountFilter.Qualifier.EQUALS_TO, 0);
        CallDataDAOFilter presentObservedDataDAOFilter = new CallDataDAOFilter(
                new HashSet<>(Arrays.asList(
                        new HashSet<>(Arrays.asList(presentLowAllCountFilter, presentHighAllCountFilter))
                            )),
                 null, true, new HashMap<>());
        CallDataDAOFilter highPresentObservedDataDAOFilter = new CallDataDAOFilter(
                new HashSet<>(Arrays.asList(
                        new HashSet<>(Arrays.asList(presentForGoldAllCountFilter))
                            )),
                 null, true, new HashMap<>());
        CallDataDAOFilter highAbsentObservedDataDAOFilter = new CallDataDAOFilter(
                new HashSet<>(Arrays.asList(
                        new HashSet<>(Arrays.asList(absentForGoldAllCountFilter)),
                        new HashSet<>(Arrays.asList(noPresentHighAllCountFilter)),
                        new HashSet<>(Arrays.asList(noPresentLowAllCountFilter))
                            )),
                 null, true, new HashMap<>());
        CallDataDAOFilter observedPresentDataDAOFilter = new CallDataDAOFilter(
                new HashSet<>(Arrays.asList(
                        new HashSet<>(Arrays.asList(presentLowAllCountFilter, presentHighAllCountFilter)),
                        new HashSet<>(Arrays.asList(presentLowSelfCountFilter, presentHighSelfCountFilter))
                            )),
                 null, null, new HashMap<>());

        CallDAOFilter allObservedPresentCallDAOFilter = new CallDAOFilter(
                null, Arrays.asList(spe1.getId()),
                null, Arrays.asList(presentObservedDataDAOFilter));
        CallDAOFilter onlyObservedPresentCallDAOFilter = new CallDAOFilter(
                null, Arrays.asList(spe1.getId()),
                null, Arrays.asList(observedPresentDataDAOFilter));
        CallDAOFilter oneGeneOneOrganObservedPresentCallDAOFilter = new CallDAOFilter(
                Arrays.asList(gTO1.getId()), Arrays.asList(),
                Arrays.asList(new DAOConditionFilter(Arrays.asList(anatEntity1.getId()), null, null)),
                Arrays.asList(presentObservedDataDAOFilter));
        CallDAOFilter oneGeneAllOrganObservedPresentCallDAOFilter = new CallDAOFilter(
                Arrays.asList(gTO1.getId()), Arrays.asList(),
                null,
                Arrays.asList(presentObservedDataDAOFilter));
        CallDAOFilter oneGeneAllOrganObservedHighCallDAOFilter = new CallDAOFilter(
                Arrays.asList(gTO1.getId()), Arrays.asList(),
                null,
                Arrays.asList(highPresentObservedDataDAOFilter, highAbsentObservedDataDAOFilter));
        CallDAOFilter allGeneOneOrganObservedPresentCallDAOFilter = new CallDAOFilter(
                null, Arrays.asList(spe1.getId()),
                Arrays.asList(new DAOConditionFilter(Arrays.asList(anatEntity1.getId()), null, null)),
                Arrays.asList(presentObservedDataDAOFilter));

        Set<ConditionDAO.Attribute> allCondParams = EnumSet.of(
                ConditionDAO.Attribute.ANAT_ENTITY_ID, ConditionDAO.Attribute.STAGE_ID);
        Set<GlobalExpressionCallDAO.Attribute> attributes = EnumSet.of(
                GlobalExpressionCallDAO.Attribute.BGEE_GENE_ID,
                GlobalExpressionCallDAO.Attribute.GLOBAL_CONDITION_ID,
                GlobalExpressionCallDAO.Attribute.DATA_TYPE_EXPERIMENT_TOTAL_COUNTS,
                GlobalExpressionCallDAO.Attribute.MEAN_RANK);
        LinkedHashMap<GlobalExpressionCallDAO.OrderingAttribute, DAO.Direction> orderByGeneAttributes =
                new LinkedHashMap<>();
        orderByGeneAttributes.put(GlobalExpressionCallDAO.OrderingAttribute.PUBLIC_GENE_ID, DAO.Direction.ASC);
        orderByGeneAttributes.put(GlobalExpressionCallDAO.OrderingAttribute.MEAN_RANK, DAO.Direction.ASC);
        LinkedHashMap<GlobalExpressionCallDAO.OrderingAttribute, DAO.Direction> orderByAnatAttributes =
                new LinkedHashMap<>();
        orderByAnatAttributes.put(GlobalExpressionCallDAO.OrderingAttribute.ANAT_ENTITY_ID, DAO.Direction.ASC);
        orderByAnatAttributes.put(GlobalExpressionCallDAO.OrderingAttribute.MEAN_RANK, DAO.Direction.ASC);

        //Mock calls to GlobalExpressionCallDAO
        //All calls with no ordering
        GlobalExpressionCallTOResultSet mockCallTOResultSet = getMockResultSet(
                GlobalExpressionCallTOResultSet.class, callTOs);
        when(this.globalExprCallDAO.getGlobalExpressionCalls(Arrays.asList(allObservedPresentCallDAOFilter),
                allCondParams, attributes, new LinkedHashMap<>()))
        .thenReturn(mockCallTOResultSet);
        //All present bronze calls order by gene and rank
        mockCallTOResultSet = getMockResultSet(
                GlobalExpressionCallTOResultSet.class, callTOs);
        when(this.globalExprCallDAO.getGlobalExpressionCalls(Arrays.asList(allObservedPresentCallDAOFilter),
                allCondParams, attributes, orderByGeneAttributes))
        .thenReturn(mockCallTOResultSet);
        //Only calls with presence of expression in the condition itself order by gene and rank
        mockCallTOResultSet = getMockResultSet(
                GlobalExpressionCallTOResultSet.class, callTOs);
        when(this.globalExprCallDAO.getGlobalExpressionCalls(Arrays.asList(onlyObservedPresentCallDAOFilter),
                allCondParams, attributes, orderByGeneAttributes))
        .thenReturn(mockCallTOResultSet);
        //All present bronze calls order by anat. entity and rank
        mockCallTOResultSet = getMockResultSet(
                GlobalExpressionCallTOResultSet.class,
                Arrays.asList(callTOs.get(0), callTOs.get(2), callTOs.get(1), callTOs.get(3)));
        when(this.globalExprCallDAO.getGlobalExpressionCalls(Arrays.asList(allObservedPresentCallDAOFilter),
                allCondParams, attributes, orderByAnatAttributes))
        .thenReturn(mockCallTOResultSet);
        //1 present bronze call for 1 gene in 1 organ
        mockCallTOResultSet = getMockResultSet(
                GlobalExpressionCallTOResultSet.class,
                Arrays.asList(callTOs.get(0)));
        when(this.globalExprCallDAO.getGlobalExpressionCalls(
                Arrays.asList(oneGeneOneOrganObservedPresentCallDAOFilter),
                allCondParams, attributes, orderByGeneAttributes))
        .thenReturn(mockCallTOResultSet);
        //Calls for one gene all organs, unordered. Should still be usable
        //to compute min./max ranks for the gene
        mockCallTOResultSet = getMockResultSet(
                GlobalExpressionCallTOResultSet.class,
                Arrays.asList(callTOs.get(0), callTOs.get(2)));
        when(this.globalExprCallDAO.getGlobalExpressionCalls(
                Arrays.asList(allGeneOneOrganObservedPresentCallDAOFilter),
                allCondParams, attributes, new LinkedHashMap<>()))
        .thenReturn(mockCallTOResultSet);
        //Calls for one gene all organs, unordered, HIGH quality, including one ABSENT call.
        //Not usable to compute min./max ranks for the gene nor for the anat. entity
        mockCallTOResultSet = getMockResultSet(
                GlobalExpressionCallTOResultSet.class,
                Arrays.asList(callTOsWithAbsentCall.get(0), callTOsWithAbsentCall.get(1)));
        when(this.globalExprCallDAO.getGlobalExpressionCalls(
                Arrays.asList(oneGeneAllOrganObservedHighCallDAOFilter),
                allCondParams, attributes, new LinkedHashMap<>()))
        .thenReturn(mockCallTOResultSet);
        //Calls for one organ all genes, unordered. Should still be usable
        //to compute min./max ranks for the organ
        mockCallTOResultSet = getMockResultSet(
                GlobalExpressionCallTOResultSet.class,
                Arrays.asList(callTOs.get(0), callTOs.get(1)));
        when(this.globalExprCallDAO.getGlobalExpressionCalls(
                Arrays.asList(oneGeneAllOrganObservedPresentCallDAOFilter),
                allCondParams, attributes, new LinkedHashMap<>()))
        .thenReturn(mockCallTOResultSet);
        //min./max rank for one gene
        when(this.globalExprCallDAO.getMinMaxRanksPerGene(
                Arrays.asList(oneGeneAllOrganObservedPresentCallDAOFilter),
                allCondParams)).thenAnswer(
                        new Answer<EntityMinMaxRanksTOResultSet<Integer>>() {
                            public EntityMinMaxRanksTOResultSet<Integer> answer(InvocationOnMock invocation) {
                                return getMockResultSet(
                                        EntityMinMaxRanksTOResultSet.class,
                                        Arrays.asList(g1MinMax));
                            }
                        });
        //min./max rank for one organ
        EntityMinMaxRanksTOResultSet<String> mockAnatMinMaxResultSet = getMockResultSet(
                EntityMinMaxRanksTOResultSet.class, 
                Arrays.asList(ae1MinMax));
        when(this.globalExprCallDAO.getMinMaxRanksPerAnatEntity(
                Arrays.asList(allGeneOneOrganObservedPresentCallDAOFilter),
                allCondParams))
        .thenReturn(mockAnatMinMaxResultSet);
        //min./max rank for all genes
        when(this.globalExprCallDAO.getMinMaxRanksPerGene(
                Arrays.asList(allObservedPresentCallDAOFilter),
                allCondParams)).thenAnswer(
                        new Answer<EntityMinMaxRanksTOResultSet<Integer>>() {
                            public EntityMinMaxRanksTOResultSet<Integer> answer(InvocationOnMock invocation) {
                                return getMockResultSet(
                                        EntityMinMaxRanksTOResultSet.class, 
                                        Arrays.asList(g1MinMax, g2MinMax));
                            }
                        });
        //min./max rank for all organs
        when(this.globalExprCallDAO.getMinMaxRanksPerAnatEntity(
                Arrays.asList(allObservedPresentCallDAOFilter),
                allCondParams)).thenAnswer(
                        new Answer<EntityMinMaxRanksTOResultSet<String>>() {
                            public EntityMinMaxRanksTOResultSet<String> answer(InvocationOnMock invocation) {
                                return getMockResultSet(
                                        EntityMinMaxRanksTOResultSet.class, 
                                        Arrays.asList(ae1MinMax, ae2MinMax));
                            }
                        });

        //expected results
        ExpressionCall callg1ae1 = new ExpressionCall(g1, cond1, 
                null, ExpressionSummary.EXPRESSED, null, null,
                new ExpressionLevelInfo(new BigDecimal("10"), null, null,
                        new QualitativeExpressionLevel<Gene>(ExpressionLevelCategory.HIGH,
                                new EntityMinMaxRanks<Gene>(
                                        new BigDecimal("10"), new BigDecimal("30000"),
                                        g1)),
                        new QualitativeExpressionLevel<AnatEntity>(ExpressionLevelCategory.HIGH,
                                new EntityMinMaxRanks<AnatEntity>(
                                        new BigDecimal("10"), new BigDecimal("10000"),
                                        anatEntity1))));
        ExpressionCall callg1ae2 = new ExpressionCall(g1, cond2, 
                null, ExpressionSummary.EXPRESSED, null, null,
                new ExpressionLevelInfo(new BigDecimal("30000"), null, null,
                        new QualitativeExpressionLevel<Gene>(ExpressionLevelCategory.LOW,
                                new EntityMinMaxRanks<Gene>(
                                        new BigDecimal("10"), new BigDecimal("30000"),
                                        g1)),
                        new QualitativeExpressionLevel<AnatEntity>(ExpressionLevelCategory.LOW,
                                new EntityMinMaxRanks<AnatEntity>(
                                        new BigDecimal("20000"), new BigDecimal("30000"),
                                        anatEntity2))));
        ExpressionCall callg2ae1 = new ExpressionCall(g2, cond1, 
                null, ExpressionSummary.EXPRESSED, null, null,
                new ExpressionLevelInfo(new BigDecimal("10000"), null, null,
                        new QualitativeExpressionLevel<Gene>(ExpressionLevelCategory.HIGH,
                                new EntityMinMaxRanks<Gene>(
                                        new BigDecimal("10000"), new BigDecimal("20000"),
                                        g2)),
                        new QualitativeExpressionLevel<AnatEntity>(ExpressionLevelCategory.LOW,
                                new EntityMinMaxRanks<AnatEntity>(
                                        new BigDecimal("10"), new BigDecimal("10000"),
                                        anatEntity1))));
        ExpressionCall callg2ae2 = new ExpressionCall(g2, cond2, 
                null, ExpressionSummary.EXPRESSED, null, null,
                new ExpressionLevelInfo(new BigDecimal("20000"), null, null,
                        new QualitativeExpressionLevel<Gene>(ExpressionLevelCategory.LOW,
                                new EntityMinMaxRanks<Gene>(
                                        new BigDecimal("10000"), new BigDecimal("20000"),
                                        g2)),
                        new QualitativeExpressionLevel<AnatEntity>(ExpressionLevelCategory.HIGH,
                                new EntityMinMaxRanks<AnatEntity>(
                                        new BigDecimal("20000"), new BigDecimal("30000"),
                                        anatEntity2))));
        //Calls with ABSENT
        ExpressionCall callg1ae1WithAbsent = new ExpressionCall(g1, cond1,
                null, ExpressionSummary.EXPRESSED, null, null,
                new ExpressionLevelInfo(new BigDecimal("10"), null, null,
                        new QualitativeExpressionLevel<Gene>(ExpressionLevelCategory.HIGH,
                                new EntityMinMaxRanks<Gene>(
                                        new BigDecimal("10"), new BigDecimal("30000"),
                                        g1)),
                        new QualitativeExpressionLevel<AnatEntity>(ExpressionLevelCategory.HIGH,
                                new EntityMinMaxRanks<AnatEntity>(
                                        new BigDecimal("10"), new BigDecimal("10000"),
                                        anatEntity1))));
        ExpressionCall callg1ae2WithAbsent = new ExpressionCall(g1, cond2,
                null, ExpressionSummary.NOT_EXPRESSED, null, null,
                new ExpressionLevelInfo(new BigDecimal("500000"), null, null,
                        new QualitativeExpressionLevel<Gene>(ExpressionLevelCategory.ABSENT,
                                new EntityMinMaxRanks<Gene>(
                                        new BigDecimal("10"), new BigDecimal("30000"),
                                        g1)),
                        new QualitativeExpressionLevel<AnatEntity>(ExpressionLevelCategory.ABSENT,
                                new EntityMinMaxRanks<AnatEntity>(
                                        new BigDecimal("20000"), new BigDecimal("30000"),
                                        anatEntity2))));


        CallService service = new CallService(this.serviceFactory);
        Map<ExpressionSummary, SummaryQuality> summaryCallTypeQualityFilter = new HashMap<>();
        summaryCallTypeQualityFilter.put(ExpressionSummary.EXPRESSED, SummaryQuality.BRONZE);
        Map<Expression, Boolean> callObservedData = new HashMap<>();
        callObservedData.put(null, true);
        Set<CallService.Attribute> attrs = EnumSet.of(Attribute.GENE, Attribute.ANAT_ENTITY_ID,
                Attribute.DEV_STAGE_ID, Attribute.CALL_TYPE, Attribute.MEAN_RANK,
                Attribute.GENE_QUAL_EXPR_LEVEL, Attribute.ANAT_ENTITY_QUAL_EXPR_LEVEL);

        //First, test by requesting all calls without any ordering.
        //The service should retrieve the calls, but also the min./max ranks per anat. entity
        //and per gene. That will make 3 calls to the DAO
        Set<ExpressionCall> actualResults = service.loadExpressionCalls(
                new ExpressionCallFilter(summaryCallTypeQualityFilter, 
                        Collections.singleton(new GeneFilter(spe1.getId())),
                        null, 
                        null, 
                        callObservedData,
                        null, null, null, null, null), 
                attrs,
                null)
                .collect(Collectors.toSet());
        assertCallsEquals(new HashSet<>(Arrays.asList(callg1ae1, callg1ae2, callg2ae1, callg2ae2)),
                actualResults);
        //Verify that the DAO for min./max ranks was correctly called
        verify(this.globalExprCallDAO, times(1)).getMinMaxRanksPerGene(
                Arrays.asList(allObservedPresentCallDAOFilter),
                allCondParams);
        verify(this.globalExprCallDAO, times(1)).getMinMaxRanksPerAnatEntity(
                Arrays.asList(allObservedPresentCallDAOFilter),
                allCondParams);

        //Now, test by requesting all calls, but ordered by genes.
        //The service should use the calls retrieved for computing min./max ranks per gene,
        //rather than querying the database, and querying the database for min./max ranks per anat. entity
        this.configureMocks();
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = 
                new LinkedHashMap<>();
        serviceOrdering.put(CallService.OrderingAttribute.GENE_ID, Service.Direction.ASC);
        serviceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.ASC);
        List<ExpressionCall> actualResultsOrdered = service.loadExpressionCalls(
                new ExpressionCallFilter(summaryCallTypeQualityFilter, 
                        Collections.singleton(new GeneFilter(spe1.getId())),
                        null, 
                        null, 
                        callObservedData,
                        null, null, null, null, null), 
                attrs,
                serviceOrdering)
                .collect(Collectors.toList());
        assertCallsEquals(Arrays.asList(callg1ae1, callg1ae2, callg2ae1, callg2ae2),
                actualResultsOrdered);
        //Verify that the DAO for min./max ranks was correctly called
        verify(this.globalExprCallDAO, times(1)).getMinMaxRanksPerGene(
                Arrays.asList(allObservedPresentCallDAOFilter),
                allCondParams);
        verify(this.globalExprCallDAO, times(2)).getMinMaxRanksPerAnatEntity(
                Arrays.asList(allObservedPresentCallDAOFilter),
                allCondParams);

        //Now, test by requesting all calls, but ordered by anat. entities.
        //The service should use the calls retrieved for computing min./max ranks per anat. entity,
        //rather than querying the database, and querying the database for min./max ranks per gene
        this.configureMocks();
        serviceOrdering = new LinkedHashMap<>();
        serviceOrdering.put(CallService.OrderingAttribute.ANAT_ENTITY_ID, Service.Direction.ASC);
        serviceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.ASC);
        actualResultsOrdered = service.loadExpressionCalls(
                new ExpressionCallFilter(summaryCallTypeQualityFilter, 
                        Collections.singleton(new GeneFilter(spe1.getId())),
                        null, 
                        null, 
                        callObservedData,
                        null, null, null, null, null), 
                attrs,
                serviceOrdering)
                .collect(Collectors.toList());
        assertCallsEquals(Arrays.asList(callg1ae1, callg2ae1, callg1ae2, callg2ae2),
                actualResultsOrdered);
        //Verify that the DAO for min./max ranks was correctly called
        verify(this.globalExprCallDAO, times(2)).getMinMaxRanksPerGene(
                Arrays.asList(allObservedPresentCallDAOFilter),
                allCondParams);
        verify(this.globalExprCallDAO, times(2)).getMinMaxRanksPerAnatEntity(
                Arrays.asList(allObservedPresentCallDAOFilter),
                allCondParams);

        //Now, query call for one gene in one anat. entity. The service should query the database
        //for both min./max ranks per anat. entity and per gene
        this.configureMocks();
        serviceOrdering = new LinkedHashMap<>();
        serviceOrdering.put(CallService.OrderingAttribute.GENE_ID, Service.Direction.ASC);
        serviceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.ASC);
        actualResultsOrdered = service.loadExpressionCalls(
                new ExpressionCallFilter(summaryCallTypeQualityFilter, 
                        Collections.singleton(new GeneFilter(spe1.getId(), g1.getEnsemblGeneId())),
                        Collections.singleton(new ConditionFilter(Collections.singleton(anatEntity1.getId()), 
                                null, null, null, null)), 
                        null, 
                        callObservedData,
                        null, null, null, null, null), 
                attrs,
                serviceOrdering)
                .collect(Collectors.toList());
        assertCallsEquals(Arrays.asList(callg1ae1),
                actualResultsOrdered);
        //Verify that the DAO for min./max ranks was correctly called.
        //The should have been queried only for one specific gene, and one specific anat. entity
        verify(this.globalExprCallDAO, times(1)).getMinMaxRanksPerGene(
                Arrays.asList(oneGeneAllOrganObservedPresentCallDAOFilter),
                allCondParams);
        verify(this.globalExprCallDAO, times(1)).getMinMaxRanksPerAnatEntity(
                Arrays.asList(allGeneOneOrganObservedPresentCallDAOFilter),
                allCondParams);

        //Now, test by requesting calls for one gene in all organs, unordered.
        //The service should use the calls retrieved for computing min./max ranks per gene,
        //rather than querying the database, and querying the database for min./max ranks per anat. entity
        this.configureMocks();
        actualResults = service.loadExpressionCalls(
                new ExpressionCallFilter(summaryCallTypeQualityFilter,
                        Collections.singleton(new GeneFilter(spe1.getId(), g1.getEnsemblGeneId())),
                        null,
                        null,
                        callObservedData,
                        null, null, null, null, null), 
                attrs,
                null)
                .collect(Collectors.toSet());
        assertCallsEquals(new HashSet<>(Arrays.asList(callg1ae1, callg1ae2)),
                actualResults);
        //Verify that the DAO for min./max ranks was correctly called
        verify(this.globalExprCallDAO, times(1)).getMinMaxRanksPerGene(
                Arrays.asList(oneGeneAllOrganObservedPresentCallDAOFilter),
                allCondParams);
        verify(this.globalExprCallDAO, times(2)).getMinMaxRanksPerGene(
                Arrays.asList(allObservedPresentCallDAOFilter),
                allCondParams);
        verify(this.globalExprCallDAO, times(3)).getMinMaxRanksPerAnatEntity(
                Arrays.asList(allObservedPresentCallDAOFilter),
                allCondParams);

        //Now, test by requesting calls for all genes in one organ, unordered.
        //The service should use the calls retrieved for computing min./max ranks per anat. entity,
        //rather than querying the database, and querying the database for min./max ranks per gene
        this.configureMocks();
        actualResults = service.loadExpressionCalls(
                new ExpressionCallFilter(summaryCallTypeQualityFilter,
                        Collections.singleton(new GeneFilter(spe1.getId())),
                        Collections.singleton(new ConditionFilter(Collections.singleton(anatEntity1.getId()), null, 
                                null, null, null)),
                        null,
                        callObservedData,
                        null, null, null, null, null),
                attrs,
                null)
                .collect(Collectors.toSet());
        assertCallsEquals(new HashSet<>(Arrays.asList(callg1ae1, callg2ae1)),
                actualResults);
        //Verify that the DAO for min./max ranks was correctly called
        verify(this.globalExprCallDAO, times(3)).getMinMaxRanksPerGene(
                Arrays.asList(allObservedPresentCallDAOFilter),
                allCondParams);
        verify(this.globalExprCallDAO, times(1)).getMinMaxRanksPerAnatEntity(
                Arrays.asList(allGeneOneOrganObservedPresentCallDAOFilter),
                allCondParams);
        verify(this.globalExprCallDAO, times(3)).getMinMaxRanksPerAnatEntity(
                Arrays.asList(allObservedPresentCallDAOFilter),
                allCondParams);

        //Now, we check when presence of expression is requested in the conditions themselves.
        //In that case, the min./max ranks cannot be retrieved from the main query
        //(because we can have an observed call with rank computed, but with absence of expression
        //in the condition itself, but expression in a sub-condition)
        this.configureMocks();
        serviceOrdering = new LinkedHashMap<>();
        serviceOrdering.put(CallService.OrderingAttribute.GENE_ID, Service.Direction.ASC);
        serviceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.ASC);
        callObservedData = new HashMap<>();
        callObservedData.put(Expression.EXPRESSED, true);
        actualResults = service.loadExpressionCalls(
                new ExpressionCallFilter(summaryCallTypeQualityFilter,
                        Collections.singleton(new GeneFilter(spe1.getId())),
                        null,
                        null,
                        callObservedData,
                        null, null, null, null, null),
                attrs,
                serviceOrdering)
                .collect(Collectors.toSet());
        assertCallsEquals(new HashSet<>(Arrays.asList(callg1ae1, callg1ae2, callg2ae1, callg2ae2)),
                actualResults);
        //Verify that the DAO for min./max ranks was correctly called
        verify(this.globalExprCallDAO, times(4)).getMinMaxRanksPerGene(
                Arrays.asList(allObservedPresentCallDAOFilter),
                allCondParams);
        verify(this.globalExprCallDAO, times(4)).getMinMaxRanksPerAnatEntity(
                Arrays.asList(allObservedPresentCallDAOFilter),
                allCondParams);

        //Now, test by requesting calls for one gene in all organs, HIGH quality, unordered,
        //with ABSENT calls
        //The service should use the DAO for getting both min./max ranks per gene
        //and min./max ranks per anat. entity.
        //Also, it's a test to see how ABSENT calls are managed
        this.configureMocks();
        summaryCallTypeQualityFilter = new HashMap<>();
        summaryCallTypeQualityFilter.put(ExpressionSummary.EXPRESSED, SummaryQuality.GOLD);
        summaryCallTypeQualityFilter.put(ExpressionSummary.NOT_EXPRESSED, SummaryQuality.GOLD);
        callObservedData = new HashMap<>();
        callObservedData.put(null, true);
        actualResults = service.loadExpressionCalls(
                new ExpressionCallFilter(summaryCallTypeQualityFilter,
                        Collections.singleton(new GeneFilter(spe1.getId(), g1.getEnsemblGeneId())),
                        null,
                        null,
                        callObservedData,
                        null, null, null, null, null), 
                attrs,
                null)
                .collect(Collectors.toSet());
        assertCallsEquals(new HashSet<>(Arrays.asList(callg1ae1WithAbsent, callg1ae2WithAbsent)),
                actualResults);
        //Verify that the DAO for min./max ranks was correctly called
        verify(this.globalExprCallDAO, times(2)).getMinMaxRanksPerGene(
                Arrays.asList(oneGeneAllOrganObservedPresentCallDAOFilter),
                allCondParams);
        verify(this.globalExprCallDAO, times(5)).getMinMaxRanksPerAnatEntity(
                Arrays.asList(allObservedPresentCallDAOFilter),
                allCondParams);
    }

    /**
     * Test the method {@link CallService#loadSingleSpeciesExprAnalysis(Collection)}
     */
    @Test
    public void shouldLoadSingleSpeciesExprAnalysis() {
        Species spe1 = new Species(1);
        GeneBioType biotype = new GeneBioType("type1");
        Gene g1 = new Gene("1", spe1, biotype);
        Gene g2 = new Gene("2", spe1, biotype);

        Set<Attribute> attributes = EnumSet.of(Attribute.GENE, Attribute.ANAT_ENTITY_ID,
                Attribute.CALL_TYPE, Attribute.DATA_QUALITY, Attribute.OBSERVED_DATA, Attribute.EXPRESSION_SCORE);
        LinkedHashMap<OrderingAttribute, Service.Direction> orderingAttributes = new LinkedHashMap<>();
        //IMPORTANT: results must be ordered by anat. entity so that we can compare expression
        //in each anat. entity without overloading the memory.
        orderingAttributes.put(OrderingAttribute.ANAT_ENTITY_ID, Service.Direction.ASC);
        Collection<GeneFilter> geneFilters = Arrays.asList(new GeneFilter(spe1.getId(),
                Arrays.asList(g1.getEnsemblGeneId(), g2.getEnsemblGeneId())));
        ExpressionCallFilter callFilter = new ExpressionCallFilter(
                null,                              //we want both present and absent calls, of any quality
                geneFilters,                       //requested genes
                null,                              //any condition
                null,                              //any data type
                null, null, null, null, null, null //both observed and propagated calls
                );

        Condition cond1 = new Condition(new AnatEntity("1"), null, null, null, null, spe1);
        Condition cond2 = new Condition(new AnatEntity("2"), null, null, null, null, spe1);
        Condition cond3 = new Condition(new AnatEntity("3"), null, null, null, null, spe1);
        Condition cond4 = new Condition(new AnatEntity("4"), null, null, null, null, spe1);
        CallService spyCallService = spy(new CallService(this.serviceFactory));
        doReturn(Stream.of(
                //The 2 genes are expressed in the same structure, observed data for only one of them,
                //should be used
                new ExpressionCall(g1, cond1,
                new DataPropagation(PropagationState.SELF, null, true),
                ExpressionSummary.EXPRESSED, SummaryQuality.SILVER,
                null, new ExpressionLevelInfo(new BigDecimal("1.0"))),
                new ExpressionCall(g2, cond1,
                new DataPropagation(PropagationState.DESCENDANT, null, false),
                ExpressionSummary.EXPRESSED, SummaryQuality.SILVER,
                null, null),
                //1 gene expressed, 1 gene not expressed, all observed data
                new ExpressionCall(g1, cond2,
                new DataPropagation(PropagationState.SELF, null, true),
                ExpressionSummary.EXPRESSED, SummaryQuality.SILVER,
                null, new ExpressionLevelInfo(new BigDecimal("1.0"))),
                new ExpressionCall(g2, cond2,
                new DataPropagation(PropagationState.SELF, null, true),
                ExpressionSummary.NOT_EXPRESSED, SummaryQuality.SILVER,
                null, new ExpressionLevelInfo(new BigDecimal("2.0"))),
                //Only one gene with data
                new ExpressionCall(g1, cond3,
                new DataPropagation(PropagationState.SELF, null, true),
                ExpressionSummary.EXPRESSED, SummaryQuality.SILVER,
                null, new ExpressionLevelInfo(new BigDecimal("1.0"))),
                //The 2 genes are expressed, but no observed data for none of them,
                //should be discarded
                new ExpressionCall(g1, cond4,
                new DataPropagation(PropagationState.DESCENDANT, null, false),
                ExpressionSummary.EXPRESSED, SummaryQuality.SILVER,
                null, null),
                new ExpressionCall(g2, cond4,
                new DataPropagation(PropagationState.DESCENDANT, null, false),
                ExpressionSummary.EXPRESSED, SummaryQuality.SILVER,
                null, null)))
        .when(spyCallService).loadExpressionCalls(callFilter, attributes, orderingAttributes);

        Map<Condition, MultiGeneExprCounts> condToCounts = new HashMap<>();
        //Counts in acond1
        Map<ExpressionSummary, Collection<Gene>> callTypeToGenes = new HashMap<>();
        callTypeToGenes.put(ExpressionSummary.EXPRESSED, Arrays.asList(g1, g2));
        Map<Gene, ExpressionLevelInfo> geneToMinRank = new HashMap<>();
        geneToMinRank.put(g1, new ExpressionLevelInfo(new BigDecimal("1.0")));
        geneToMinRank.put(g2, null);
        MultiGeneExprCounts count = new MultiGeneExprCounts(callTypeToGenes, null, geneToMinRank);
        condToCounts.put(cond1, count);
        //counts in cond2
        callTypeToGenes = new HashMap<>();
        callTypeToGenes.put(ExpressionSummary.EXPRESSED, Arrays.asList(g1));
        callTypeToGenes.put(ExpressionSummary.NOT_EXPRESSED, Arrays.asList(g2));
        geneToMinRank = new HashMap<>();
        geneToMinRank.put(g1, new ExpressionLevelInfo(new BigDecimal("1.0")));
        geneToMinRank.put(g2, new ExpressionLevelInfo(new BigDecimal("2.0")));
        count = new MultiGeneExprCounts(callTypeToGenes, null, geneToMinRank);
        condToCounts.put(cond2, count);
        //counts in cond3
        callTypeToGenes = new HashMap<>();
        callTypeToGenes.put(ExpressionSummary.EXPRESSED, Arrays.asList(g1));
        geneToMinRank = new HashMap<>();
        geneToMinRank.put(g1, new ExpressionLevelInfo(new BigDecimal("1.0")));
        count = new MultiGeneExprCounts(callTypeToGenes, Arrays.asList(g2), geneToMinRank);
        condToCounts.put(cond3, count);
        SingleSpeciesExprAnalysis expectedResult = new SingleSpeciesExprAnalysis(Arrays.asList(g1, g2),
                condToCounts);

        assertEquals(expectedResult, spyCallService.loadSingleSpeciesExprAnalysis(Arrays.asList(g1, g2)));
    }

    private static void assertCallsEquals(Collection<ExpressionCall> expectedCalls,
            Collection<ExpressionCall> actualCalls) {
        log.entry(expectedCalls, actualCalls);
        boolean equals = true;
        if (!Objects.equals(expectedCalls, actualCalls)) {
            equals = false;
        } else {
            Map<ExpressionCall, ExpressionLevelInfo> expectedLevelInfos = expectedCalls.stream()
                    .collect(Collectors.toMap(c -> c, c -> c.getExpressionLevelInfo()));
            Map<ExpressionCall, ExpressionLevelInfo> actualLevelInfos = actualCalls.stream()
                    .collect(Collectors.toMap(c -> c, c -> c.getExpressionLevelInfo()));
            if (!expectedLevelInfos.equals(actualLevelInfos)) {
                equals = false;
            }
        }
        if (!equals) {
            throw log.throwing(new AssertionError("Incorrect calls retrieved, expected: " + expectedCalls
                    + ", but was: " + actualCalls));
        }
        log.traceExit();
    }
}