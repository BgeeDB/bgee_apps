package org.bgee.model.expressiondata;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.CallDAOFilter;
import org.bgee.model.dao.api.expressiondata.DAOConditionFilter;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTOResultSet;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTOResultSet;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.ontology.OntologyService;
import org.bgee.model.ontology.RelationType;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit tests for {@link CallService}.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Feb. 2017
 * @since   Bgee 13, Nov. 2015
 */
public class CallServiceTest extends TestAncestor {
    
    private final static Logger log = LogManager.getLogger(CallServiceTest.class.getName());
        
    @Override
    protected Logger getLogger() {
        return log;
    } 
    
    /**
     * Test the method {@link CallService#loadExpressionCalls(String, ExpressionCallFilter, 
     * Collection, LinkedHashMap, boolean)}.
     */
    // Keep in this test only one gene: it allows to detect if only one iteration is possible
    @Test
    @Ignore("Test ignored until it is re-implemented following many modifications.")
    public void shouldLoadExpressionCallsForBasicGene() {
        //First test for one gene, with sub-stages but without substructures. 
        //Retrieving all attributes, ordered by mean rank. 
        DAOManager manager = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(manager);
        ExpressionCallDAO dao = mock(ExpressionCallDAO.class);
        when(manager.getExpressionCallDAO()).thenReturn(dao);
        
        LinkedHashMap<ExpressionCallDAO.OrderingAttribute, DAO.Direction> orderingAttrs = 
                new LinkedHashMap<>();
        orderingAttrs.put(ExpressionCallDAO.OrderingAttribute.MEAN_RANK, DAO.Direction.DESC);
        
        ExpressionCallTOResultSet resultSetMock = getMockResultSet(ExpressionCallTOResultSet.class, 
                Arrays.asList(
                        // To not overload tests, we put null for not used attributes 
                        // but, real query return all attributes
                    new ExpressionCallTO(null, "geneId1", "anatEntityId1", "stageId1", 
                        new BigDecimal(1257.34), CallTO.DataState.LOWQUALITY, null,
                        CallTO.DataState.HIGHQUALITY, null, CallTO.DataState.NODATA, null,
                        CallTO.DataState.NODATA, null, false, false, ExpressionCallTO.OriginOfLine.SELF, 
                        ExpressionCallTO.OriginOfLine.SELF, true), 
                    new ExpressionCallTO(null, "geneId1", "anatEntityId1", "stageId2", 
                        new BigDecimal(125.00), CallTO.DataState.NODATA, null,
                        CallTO.DataState.LOWQUALITY, null, CallTO.DataState.LOWQUALITY, null,
                        CallTO.DataState.NODATA, null, false, false, ExpressionCallTO.OriginOfLine.SELF, 
                        ExpressionCallTO.OriginOfLine.SELF, true)));
        
        //we'll do the verify afterwards, it's easier to catch a problem in the parameters
        when(dao.getExpressionCalls(
                //CallDAOFilters
                anyCollectionOf(CallDAOFilter.class), 
                //CallTOs
                anyCollectionOf(ExpressionCallTO.class),
                //propagation
                anyBoolean(), anyBoolean(), 
                //genes
                anyCollectionOf(String.class), 
                //orthology
                anyObject(), 
                //attributes
                anyCollectionOf(ExpressionCallDAO.Attribute.class), 
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
        Ontology<AnatEntity> anatEntityOnt = mock(Ontology.class);
        //suppress warning as we cannot specify generic type for a mock
        @SuppressWarnings("unchecked")
        Ontology<DevStage> devStageOnt = mock(Ontology.class);

        when(ontService.getAnatEntityOntology("speciesId1", new HashSet<>(Arrays.asList(
                "anatEntityId1")), EnumSet.of(RelationType.ISA_PARTOF), true, false))
        .thenReturn(anatEntityOnt);
        when(ontService.getDevStageOntology("speciesId1", new HashSet<>(Arrays.asList(
                "stageId1", "stageId2")), true, false)).thenReturn(devStageOnt);
        String anatEntityId1 = "anatEntityId1";
        AnatEntity anatEntity1 = new AnatEntity(anatEntityId1);
        String stageId1 = "stageId1";
        DevStage stage1 = new DevStage(stageId1);
        String stageId2 = "stageId2";
        DevStage stage2 = new DevStage(stageId2);
        String stageId3 = "stageId3";
        DevStage stage3 = new DevStage(stageId3);

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

        List<ExpressionCall> expectedResults = Arrays.asList(
                new ExpressionCall("geneId1", new Condition("anatEntityId1", "stageId1", "speciesId1"), 
                    true, ExpressionSummary.EXPRESSED, SummaryQuality.GOLD, 
                    Arrays.asList(
                        new ExpressionCallData(DataType.AFFYMETRIX, 0, 1 /*presentLowSelfExpCount*/,
                            0, 0, 0, 1 /*presentLowTotalCount*/, 0, 0), 
                        new ExpressionCallData(DataType.EST, 
                            2 /*presentHighSelfExpCount*/, 1 /*presentLowSelfExpCount*/,
                            0, 0, 2 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0)), 
                    new BigDecimal(1257.34)),
                new ExpressionCall("geneId1", new Condition("anatEntityId1", "stageId2", "speciesId1"), 
                    true, ExpressionSummary.EXPRESSED, SummaryQuality.GOLD, 
                    Arrays.asList(
                        new ExpressionCallData(DataType.AFFYMETRIX,
                            0 /*presentHighSelfExpCount*/, 0 /*presentLowSelfExpCount*/, 0, 0,
                            3 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0), 
                        new ExpressionCallData(DataType.EST,
                            0 /*presentHighSelfExpCount*/, 0 /*presentLowSelfExpCount*/, 0, 0,
                            0 /*presentHighTotalCount*/, 0 /*presentLowTotalCount*/, 0, 0), 
                        new ExpressionCallData(DataType.EST,
                            0 /*presentHighSelfExpCount*/, 1 /*presentLowSelfExpCount*/, 0, 0,
                            0 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0), 
                        new ExpressionCallData(DataType.IN_SITU,
                            0 /*presentHighSelfExpCount*/, 1 /*presentLowSelfExpCount*/, 0, 0,
                            0 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0)), 
//                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, 
//                            new DataPropagation(PropagationState.SELF, PropagationState.DESCENDANT, false)), 
//                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.EST, 
//                            new DataPropagation(PropagationState.SELF, PropagationState.DESCENDANT, false)),
//                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST,
//                            new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)), 
//                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU,
//                            new DataPropagation(PropagationState.SELF, PropagationState.SELF, true))), 
                    new BigDecimal(125.00)),
                new ExpressionCall("geneId1", new Condition("anatEntityId1", "stageId3", "speciesId1"), 
                    false, ExpressionSummary.EXPRESSED, SummaryQuality.GOLD, 
                    Arrays.asList(
                        new ExpressionCallData(DataType.AFFYMETRIX,
                            0 /*presentHighSelfExpCount*/, 0 /*presentLowSelfExpCount*/, 0, 0,
                            0 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0), 
                        new ExpressionCallData(DataType.EST,
                            0 /*presentHighSelfExpCount*/, 0 /*presentLowSelfExpCount*/, 0, 0,
                            2 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0), 
                        new ExpressionCallData(DataType.EST,
                            0 /*presentHighSelfExpCount*/, 0 /*presentLowSelfExpCount*/, 0, 0,
                            0 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0), 
                        new ExpressionCallData(DataType.IN_SITU,
                            0 /*presentHighSelfExpCount*/, 0 /*presentLowSelfExpCount*/, 0, 0,
                            0 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0)), 

//                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, 
//                            new DataPropagation(PropagationState.SELF, PropagationState.DESCENDANT, false)),                     
//                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.EST, 
//                            new DataPropagation(PropagationState.SELF, PropagationState.DESCENDANT, false)),
//                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST,
//                            new DataPropagation(PropagationState.SELF, PropagationState.DESCENDANT, false)), 
//                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU,
//                            new DataPropagation(PropagationState.SELF, PropagationState.DESCENDANT, false))), 
                    null));
        
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = 
                new LinkedHashMap<>();
        serviceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.DESC);

        CallService service = new CallService(serviceFactory);
        List<ExpressionCall> actualResults = service.loadExpressionCalls("speciesId1", 
                new ExpressionCallFilter(new GeneFilter("geneId1"), null, null, null,
                    ExpressionSummary.EXPRESSED, new DataPropagation()), 
                null, // all attributes 
                serviceOrdering)
                .collect(Collectors.toList());
        assertEquals("Incorrect ExpressionCalls generated", expectedResults, actualResults);
        
        verify(dao).getExpressionCalls(
                //CallDAOFilters
                collectionEq(Arrays.asList(
                    new CallDAOFilter(null, Arrays.asList("speciesId1"), null))
                ), 
                //CallTOs
                collectionEq(Arrays.asList(new ExpressionCallTO(
                        null, null, null, null, null, null, true))),
                //propagation
                eq(false), eq(false), 
                //genes
                collectionEq(Arrays.asList("geneId1")), 
                //orthology
                eq(null), 
                //attributes
                collectionEq(this.getAllExpressionCallDAOAttributes()), 
                eq(orderingAttrs));
    }
    
    @Test
    @Ignore("Test ignored until it is re-implemented following many modifications.")
    public void shouldLoadExpressionCallsForTwoExpressionSummary() {
        DAOManager manager = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(manager);
        ExpressionCallDAO exprDao = mock(ExpressionCallDAO.class);
        when(manager.getExpressionCallDAO()).thenReturn(exprDao);
        NoExpressionCallDAO noExprDao = mock(NoExpressionCallDAO.class);
        when(manager.getNoExpressionCallDAO()).thenReturn(noExprDao);
        
        LinkedHashMap<ExpressionCallDAO.OrderingAttribute, DAO.Direction> orderingAttrs = 
                new LinkedHashMap<>();
        orderingAttrs.put(ExpressionCallDAO.OrderingAttribute.GENE_ID, DAO.Direction.ASC);
        orderingAttrs.put(ExpressionCallDAO.OrderingAttribute.ANAT_ENTITY_ID, DAO.Direction.ASC);
        orderingAttrs.put(ExpressionCallDAO.OrderingAttribute.STAGE_ID, DAO.Direction.ASC);
        
        ExpressionCallTOResultSet resultSetMock = getMockResultSet(ExpressionCallTOResultSet.class, 
                Arrays.asList(
                        // To not overload tests, we put null for not used attributes 
                        // but, real query return all attributes
                    new ExpressionCallTO(null, "geneId1", "anatEntityId1", "stageId1", 
                        new BigDecimal(1257.34), CallTO.DataState.LOWQUALITY, null,
                        CallTO.DataState.HIGHQUALITY, null, CallTO.DataState.NODATA, null,
                        CallTO.DataState.NODATA, null, false, false, ExpressionCallTO.OriginOfLine.SELF, 
                        ExpressionCallTO.OriginOfLine.SELF, true), 
                    new ExpressionCallTO(null, "geneId1", "anatEntityId1", "stageId2", 
                        new BigDecimal(125.00), CallTO.DataState.NODATA, null,
                        CallTO.DataState.LOWQUALITY, null, CallTO.DataState.LOWQUALITY, null,
                        CallTO.DataState.NODATA, null, false, false, ExpressionCallTO.OriginOfLine.SELF, 
                        ExpressionCallTO.OriginOfLine.SELF, true), 
                    new ExpressionCallTO(null, "geneId2", "anatEntityId2", "stageId2", 
                        new BigDecimal(125.00), CallTO.DataState.NODATA, null,
                        CallTO.DataState.NODATA, null, CallTO.DataState.NODATA, null,
                        CallTO.DataState.HIGHQUALITY, null, false, false, ExpressionCallTO.OriginOfLine.SELF, 
                        ExpressionCallTO.OriginOfLine.SELF, true)));
        
        //we'll do the verify afterwards, it's easier to catch a problem in the parameters
        when(exprDao.getExpressionCalls(
                //CallDAOFilters
                anyCollectionOf(CallDAOFilter.class), 
                //CallTOs
                anyCollectionOf(ExpressionCallTO.class),
                //propagation
                anyBoolean(), anyBoolean(), 
                //genes
                anyCollectionOf(String.class), 
                //orthology
                anyObject(), 
                //attributes
                anyCollectionOf(ExpressionCallDAO.Attribute.class), 
                anyObject()))
        .thenReturn(resultSetMock);
        
        NoExpressionCallTOResultSet resultSetNoExprMock = getMockResultSet(NoExpressionCallTOResultSet.class, 
                Arrays.asList(
                        // To not overload tests, we put null for not used attributes 
                        // but, real query return all attributes
                    new NoExpressionCallTO(null, "geneId1", "anatEntityId1", "stageId1",
                            CallTO.DataState.NODATA, CallTO.DataState.NODATA,  
                            CallTO.DataState.NODATA, CallTO.DataState.HIGHQUALITY, false,
                            NoExpressionCallTO.OriginOfLine.SELF),
                    new NoExpressionCallTO(null, "geneId2", "anatEntityId1", "stageId2", 
                        CallTO.DataState.NODATA, CallTO.DataState.LOWQUALITY,
                        CallTO.DataState.NODATA, CallTO.DataState.NODATA, false,
                        NoExpressionCallTO.OriginOfLine.SELF)));
        //we'll do the verify afterwards, it's easier to catch a problem in the parameters
        when(noExprDao.getNoExpressionCalls(anyObject())) //NoExpressionCallParams
        .thenReturn(resultSetNoExprMock);

        OntologyService ontService = mock(OntologyService.class);
        AnatEntityService anatEntityService = mock(AnatEntityService.class);
        DevStageService devStageService = mock(DevStageService.class);
        when(serviceFactory.getOntologyService()).thenReturn(ontService);
        when(serviceFactory.getAnatEntityService()).thenReturn(anatEntityService);
        when(serviceFactory.getDevStageService()).thenReturn(devStageService);
        //suppress warning as we cannot specify generic type for a mock
        @SuppressWarnings("unchecked")
        Ontology<AnatEntity> anatEntityOnt = mock(Ontology.class);
        //suppress warning as we cannot specify generic type for a mock
        @SuppressWarnings("unchecked")
        Ontology<DevStage> devStageOnt = mock(Ontology.class);

        when(ontService.getAnatEntityOntology("speciesId1", new HashSet<>(Arrays.asList(
                "anatEntityId1", "anatEntityId2")), EnumSet.of(RelationType.ISA_PARTOF), true, false))
        .thenReturn(anatEntityOnt);
        when(ontService.getDevStageOntology("speciesId1", new HashSet<>(Arrays.asList(
                "stageId1", "stageId2")), true, false)).thenReturn(devStageOnt);
        String anatEntityId1 = "anatEntityId1";
        AnatEntity anatEntity1 = new AnatEntity(anatEntityId1);
        String anatEntityId2 = "anatEntityId2";
        AnatEntity anatEntity2 = new AnatEntity(anatEntityId2);
        String stageId1 = "stageId1";
        DevStage stage1 = new DevStage(stageId1);
        String stageId2 = "stageId2";
        DevStage stage2 = new DevStage(stageId2);
        String stageId3 = "stageId3";
        DevStage stage3 = new DevStage(stageId3);

        when(anatEntityOnt.getElements()).thenReturn(new HashSet<>(Arrays.asList(anatEntity1, anatEntity2)));
        when(anatEntityOnt.getElement(anatEntityId1)).thenReturn(anatEntity1);
        when(anatEntityOnt.getElement(anatEntityId2)).thenReturn(anatEntity2);
        when(anatEntityOnt.getAncestors(anatEntity1)).thenReturn(new HashSet<>());
        when(anatEntityOnt.getAncestors(anatEntity1, false)).thenReturn(new HashSet<>());
        when(anatEntityOnt.getAncestors(anatEntity2)).thenReturn(new HashSet<>(Arrays.asList(anatEntity1)));
        when(anatEntityOnt.getAncestors(anatEntity2, false)).thenReturn(new HashSet<>(Arrays.asList(anatEntity1)));
        when(anatEntityOnt.getDescendants(anatEntity1)).thenReturn(new HashSet<>(Arrays.asList(anatEntity2)));
        when(anatEntityOnt.getDescendants(anatEntity1, false)).thenReturn(new HashSet<>(Arrays.asList(anatEntity2)));

        when(devStageOnt.getElements()).thenReturn(new HashSet<>(Arrays.asList(stage1, stage2, stage3)));
        when(devStageOnt.getElement(stageId1)).thenReturn(stage1);
        when(devStageOnt.getElement(stageId2)).thenReturn(stage2);
        when(devStageOnt.getElement(stageId3)).thenReturn(stage3);
        when(devStageOnt.getAncestors(stage1)).thenReturn(new HashSet<>(Arrays.asList(stage2, stage3)));
        when(devStageOnt.getAncestors(stage2)).thenReturn(new HashSet<>(Arrays.asList(stage3)));
        when(devStageOnt.getAncestors(stage3)).thenReturn(new HashSet<>());
        when(devStageOnt.getAncestors(stage1, false)).thenReturn(new HashSet<>(Arrays.asList(stage2, stage3)));
        when(devStageOnt.getAncestors(stage2, false)).thenReturn(new HashSet<>(Arrays.asList(stage3)));
        when(devStageOnt.getAncestors(stage3, false)).thenReturn(new HashSet<>());

        List<ExpressionCall> expectedResults = Arrays.asList(
            new ExpressionCall("geneId1", new Condition("anatEntityId1", "stageId1", "speciesId1"), 
                true, ExpressionSummary.STRONG_AMBIGUITY, null, Arrays.asList(
                    new ExpressionCallData(DataType.AFFYMETRIX,
                        0 /*presentHighSelfExpCount*/, 1 /*presentLowSelfExpCount*/, 0, 0,
                        0 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0), 
                    new ExpressionCallData(DataType.EST,
                        1 /*presentHighSelfExpCount*/, 0 /*presentLowSelfExpCount*/, 0, 0,
                        1 /*presentHighTotalCount*/, 0 /*presentLowTotalCount*/, 0, 0), 
                    new ExpressionCallData(DataType.RNA_SEQ,
                        0 , 0 , 2 /*absentHighSelfExpCount*/, 0,
                        0 , 0 , 2 /*absentHighTotalCount*/, 0)), 
//                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, 
//                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)), 
//                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.EST, 
//                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)), 
//                    new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, 
//                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true))), 
                new BigDecimal(1257.34)),
            new ExpressionCall("geneId1", new Condition("anatEntityId1", "stageId2", "speciesId1"), 
                true, ExpressionSummary.EXPRESSED, SummaryQuality.GOLD, Arrays.asList(
                    new ExpressionCallData(DataType.AFFYMETRIX,
                        0 /*presentHighSelfExpCount*/, 0 /*presentLowSelfExpCount*/, 0, 0,
                        0 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0), 
                    new ExpressionCallData(DataType.EST,
                        0 /*presentHighSelfExpCount*/, 0 /*presentLowSelfExpCount*/, 0, 0,
                        2 /*presentHighTotalCount*/, 0 /*presentLowTotalCount*/, 0, 0), 
                    new ExpressionCallData(DataType.EST,
                        0 /*presentHighSelfExpCount*/, 1 /*presentLowSelfExpCount*/, 0, 0,
                        0 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0), 
                    new ExpressionCallData(DataType.IN_SITU,
                        0 /*presentHighSelfExpCount*/, 1 /*presentLowSelfExpCount*/, 0, 0,
                        0 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0)), 
//                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, 
//                        new DataPropagation(PropagationState.SELF, PropagationState.DESCENDANT, false)), 
//                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.EST, 
//                        new DataPropagation(PropagationState.SELF, PropagationState.DESCENDANT, false)),
//                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST,
//                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)), 
//                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU,
//                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true))), 
                new BigDecimal(125.00))
            ,
            new ExpressionCall("geneId1", new Condition("anatEntityId1", "stageId3", "speciesId1"), 
                false, ExpressionSummary.EXPRESSED, SummaryQuality.GOLD, Arrays.asList(
                    new ExpressionCallData(DataType.AFFYMETRIX,
                        0 /*presentHighSelfExpCount*/, 0 /*presentLowSelfExpCount*/, 0, 0,
                        0 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0), 
                    new ExpressionCallData(DataType.EST,
                        0 /*presentHighSelfExpCount*/, 0 /*presentLowSelfExpCount*/, 0, 0,
                        1 /*presentHighTotalCount*/, 0 /*presentLowTotalCount*/, 0, 0), 
                    new ExpressionCallData(DataType.EST,
                        0 /*presentHighSelfExpCount*/, 0 /*presentLowSelfExpCount*/, 0, 0,
                        0 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0), 
                    new ExpressionCallData(DataType.IN_SITU,
                        0 /*presentHighSelfExpCount*/, 0 /*presentLowSelfExpCount*/, 0, 0,
                        0 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0)), 
//                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, 
//                        new DataPropagation(PropagationState.SELF, PropagationState.DESCENDANT, false)), 
//                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.EST, 
//                        new DataPropagation(PropagationState.SELF, PropagationState.DESCENDANT, false)),
//                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST,
//                        new DataPropagation(PropagationState.SELF, PropagationState.DESCENDANT, false)), 
//                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU,
//                        new DataPropagation(PropagationState.SELF, PropagationState.DESCENDANT, false))), 
                null),
            // We propagate no-expression only in where at least one expression call
            // (propagated or not) is found. So no ExpressionCall geneId1 / anatEntityId2 / stageId1 
            new ExpressionCall("geneId2", new Condition("anatEntityId1", "stageId2", "speciesId1"), 
                true, ExpressionSummary.STRONG_AMBIGUITY, null, Arrays.asList(
                    new ExpressionCallData(DataType.RNA_SEQ,
                        0 /*presentHighSelfExpCount*/, 0 /*presentLowSelfExpCount*/, 0, 0,
                        3 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0), 
                    new ExpressionCallData(DataType.IN_SITU,
                        0 , 0 , 0 /*absentHighSelfExpCount*/, 1 /*absentLowSelfExpCount*/,
                        0 , 0 , 0 /*absentHighTotalCount*/, 1 /*absentLowTotalCount*/)), 
//                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.High, DataType.RNA_SEQ,
//                        new DataPropagation(PropagationState.DESCENDANT, PropagationState.SELF, false)),
//                    new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.IN_SITU,
//                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true))), 
                null),
            new ExpressionCall("geneId2", new Condition("anatEntityId1", "stageId3", "speciesId1"), 
                false, ExpressionSummary.EXPRESSED, SummaryQuality.GOLD, Arrays.asList(
                    new ExpressionCallData(DataType.RNA_SEQ,
                        0 /*presentHighSelfExpCount*/, 0 /*presentLowSelfExpCount*/, 0, 0,
                        3 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0)), 
//                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ,
//                        new DataPropagation(PropagationState.DESCENDANT, PropagationState.DESCENDANT, false))), 
                null),
            new ExpressionCall("geneId2", new Condition("anatEntityId2", "stageId2", "speciesId1"), 
                true, ExpressionSummary.WEAK_AMBIGUITY, null, Arrays.asList(
                    new ExpressionCallData(DataType.RNA_SEQ,
                        2 /*presentHighSelfExpCount*/, 0 /*presentLowSelfExpCount*/, 0, 0,
                        3 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0), 
                    new ExpressionCallData(DataType.IN_SITU,
                        0 , 0 , 0 /*absentHighSelfExpCount*/, 0 /*absentLowSelfExpCount*/,
                        0 , 0 , 0 /*absentHighTotalCount*/, 1 /*absentLowTotalCount*/)), 
//                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ,
//                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)),
//                    new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.IN_SITU,
//                        new DataPropagation(PropagationState.ANCESTOR, PropagationState.SELF, false))), 
                new BigDecimal(125.00)),
            new ExpressionCall("geneId2", new Condition("anatEntityId2", "stageId3", "speciesId1"), 
                false, ExpressionSummary.EXPRESSED, SummaryQuality.GOLD, Arrays.asList(
                    new ExpressionCallData(DataType.RNA_SEQ,
                        0 /*presentHighSelfExpCount*/, 0 /*presentLowSelfExpCount*/, 0, 0,
                        1 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0)), 
//                    new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ,
//                        new DataPropagation(PropagationState.SELF, PropagationState.DESCENDANT, false))), 
                null));

        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = 
                new LinkedHashMap<>();
        serviceOrdering.put(CallService.OrderingAttribute.GENE_ID, Service.Direction.ASC);
        serviceOrdering.put(CallService.OrderingAttribute.ANAT_ENTITY_ID, Service.Direction.ASC);
        serviceOrdering.put(CallService.OrderingAttribute.DEV_STAGE_ID, Service.Direction.ASC);
        

        CallService service = new CallService(serviceFactory);
        List<ExpressionCall> actualResults = service.loadExpressionCalls("speciesId1", 
                new ExpressionCallFilter(null, null, null, null, null, new DataPropagation()),
                null, // all attributes 
                serviceOrdering)
                .collect(Collectors.toList());
        assertEquals("Incorrect ExpressionCalls generated", expectedResults, actualResults);
        
        verify(exprDao).getExpressionCalls(
                //CallDAOFilters
                collectionEq(Arrays.asList(
                    new CallDAOFilter(null, Arrays.asList("speciesId1"), null))
                ), 
                //CallTOs
                collectionEq(Arrays.asList(new ExpressionCallTO(
                        null, null, null, null, null, null, true))),
                //propagation
                eq(false), eq(false), 
                //genes
                collectionEq(Arrays.asList()), 
                //orthology
                eq(null), 
                //attributes
                collectionEq(this.getAllExpressionCallDAOAttributes()), 
                eq(orderingAttrs));
        // TODO NoExpressionCallParams has no equals method so we cannot set something like eq(params)
        verify(noExprDao).getNoExpressionCalls(anyObject());
    }

    /**
     * Test the method {@link CallService#loadExpressionCalls(String, ExpressionCallFilter, 
     * Collection, LinkedHashMap, boolean)}.
     */    
    // Keep in this test empty stream for no-expression calls:
    // it allows to detect if all calls are read when only one query has calls
    @Test
    @Ignore("Test ignored until it is re-implemented following many modifications.")
    public void shouldLoadExpressionCallsForSeveralGenes() {
        //Retrieving geneId, anatEntityId, unordered, with substructures but without sub-stages. 
        DAOManager manager = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(manager);
        ExpressionCallDAO dao = mock(ExpressionCallDAO.class);
        when(manager.getExpressionCallDAO()).thenReturn(dao);
        
        ExpressionCallTOResultSet resultSetMock = getMockResultSet(ExpressionCallTOResultSet.class, 
                Arrays.asList(
                    // To not overload tests, we put null for not used attributes 
                    // but, real query return all attributes
                    new ExpressionCallTO(null, "geneId1", "anatEntityId1", "stageId1",  
                        null, CallTO.DataState.HIGHQUALITY, null, null, null, null, null, null, null, 
                        false, false, ExpressionCallTO.OriginOfLine.SELF,
                        ExpressionCallTO.OriginOfLine.SELF, true), 
                    new ExpressionCallTO(null, "geneId1", "anatEntityId2", "stageId1",  
                            null, CallTO.DataState.HIGHQUALITY, null, null, null, null, null, null, null, 
                            false, false, ExpressionCallTO.OriginOfLine.SELF,
                            ExpressionCallTO.OriginOfLine.SELF, true), 
                    new ExpressionCallTO(null, "geneId2", "anatEntityId1", "stageId2",  
                        null, CallTO.DataState.HIGHQUALITY, null, null, null, null, null, null, null, 
                        false, false, ExpressionCallTO.OriginOfLine.SELF,
                        ExpressionCallTO.OriginOfLine.SELF, true)));

        //we'll do the verify afterwards, it's easier to catch a problem in the parameters
        when(dao.getExpressionCalls(
                //CallDAOFilters
                anyCollectionOf(CallDAOFilter.class), 
                //CallTOs
                anyCollectionOf(ExpressionCallTO.class),
                //propagation
                anyBoolean(), anyBoolean(), 
                //genes
                anyCollectionOf(String.class), 
                //orthology
                anyObject(), 
                //attributes
                anyCollectionOf(ExpressionCallDAO.Attribute.class), 
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
        Ontology<AnatEntity> anatEntityOnt = mock(Ontology.class);
        //suppress warning as we cannot specify generic type for a mock
        @SuppressWarnings("unchecked")
        Ontology<DevStage> devStageOnt = mock(Ontology.class);

        when(ontService.getAnatEntityOntology("speciesId1", new HashSet<>(Arrays.asList(
                "anatEntityId1", "anatEntityId2")), EnumSet.of(RelationType.ISA_PARTOF), true, false))
            .thenReturn(anatEntityOnt);
        when(ontService.getDevStageOntology("speciesId1", new HashSet<>(Arrays.asList(
                "stageId1", "stageId2")), true, false)).thenReturn(devStageOnt);
        String anatEntityId1 = "anatEntityId1";
        AnatEntity anatEntity1 = new AnatEntity(anatEntityId1);
        String anatEntityId2 = "anatEntityId2";
        AnatEntity anatEntity2 = new AnatEntity(anatEntityId2);
        String stageId1 = "stageId1";
        DevStage stage1 = new DevStage(stageId1);
        String stageId2 = "stageId2";
        DevStage stage2 = new DevStage(stageId2);

        when(anatEntityOnt.getElements()).thenReturn(new HashSet<>(Arrays.asList(anatEntity1, anatEntity2)));
        when(devStageOnt.getElements()).thenReturn(new HashSet<>(Arrays.asList(stage1, stage2)));
        when(anatEntityOnt.getElement(anatEntityId1)).thenReturn(anatEntity1);
        when(devStageOnt.getElement(stageId1)).thenReturn(stage1);
        when(devStageOnt.getElement(stageId2)).thenReturn(stage2);
        when(anatEntityOnt.getAncestors(anatEntity1)).thenReturn(new HashSet<>(Arrays.asList(anatEntity2)));
        when(anatEntityOnt.getAncestors(anatEntity2)).thenReturn(new HashSet<>());
        when(devStageOnt.getAncestors(stage1)).thenReturn(new HashSet<>(Arrays.asList(stage2)));
        when(devStageOnt.getAncestors(stage2)).thenReturn(new HashSet<>());
        when(anatEntityOnt.getAncestors(anatEntity1, false)).thenReturn(new HashSet<>(Arrays.asList(anatEntity2)));
        when(anatEntityOnt.getAncestors(anatEntity2, false)).thenReturn(new HashSet<>());
        when(devStageOnt.getAncestors(stage1, false)).thenReturn(new HashSet<>(Arrays.asList(stage2)));
        when(devStageOnt.getAncestors(stage2, false)).thenReturn(new HashSet<>());

        List<ExpressionCall> expectedResults = Arrays.asList(
                new ExpressionCall("geneId1", new Condition("anatEntityId1", null, "speciesId1"),
                        null, ExpressionSummary.EXPRESSED, null, null, null), 
                new ExpressionCall("geneId1", new Condition("anatEntityId2", null, "speciesId1"),
                        null, ExpressionSummary.EXPRESSED, null, null, null), 
                new ExpressionCall("geneId2", new Condition("anatEntityId1", null, "speciesId1"), 
                        null, ExpressionSummary.EXPRESSED, null, null, null), 
                new ExpressionCall("geneId2", new Condition("anatEntityId2", null, "speciesId1"), 
                        null, ExpressionSummary.EXPRESSED, null, null, null)
            );
        
        CallService service = new CallService(serviceFactory);
        List<ExpressionCall> actualResults = service.loadExpressionCalls("speciesId1", 
                new ExpressionCallFilter(null, null, null, null,
                    ExpressionSummary.EXPRESSED, new DataPropagation()),
                EnumSet.of(CallService.Attribute.GENE_ID, CallService.Attribute.ANAT_ENTITY_ID), 
                null)
                .collect(Collectors.toList());
        assertEquals("Incorrect ExpressionCalls generated", expectedResults, actualResults);
        
        verify(dao).getExpressionCalls(
                //CallDAOFilters
                collectionEq(Arrays.asList(
                    new CallDAOFilter(null, Arrays.asList("speciesId1"), null))
                ), 
                //CallTOs
                collectionEq(Arrays.asList(new ExpressionCallTO(
                        null, null, null, null, null, null, true))),
                //propagation
                eq(false), eq(false), 
                //genes
                collectionEq(new HashSet<>()), 
                //orthology
                eq(null), 
                //attributes
                collectionEq(this.getAllExpressionCallDAOAttributes()), 
                eq(new LinkedHashMap<>()));
    }

    private Set<ExpressionCallDAO.Attribute> getAllExpressionCallDAOAttributes() {
        return EnumSet.of(ExpressionCallDAO.Attribute.GENE_ID, ExpressionCallDAO.Attribute.ANAT_ENTITY_ID,
                ExpressionCallDAO.Attribute.STAGE_ID, ExpressionCallDAO.Attribute.GLOBAL_MEAN_RANK,
                ExpressionCallDAO.Attribute.AFFYMETRIX_DATA, ExpressionCallDAO.Attribute.EST_DATA,
                ExpressionCallDAO.Attribute.IN_SITU_DATA, ExpressionCallDAO.Attribute.RNA_SEQ_DATA,
                ExpressionCallDAO.Attribute.ANAT_ORIGIN_OF_LINE, 
                ExpressionCallDAO.Attribute.STAGE_ORIGIN_OF_LINE, ExpressionCallDAO.Attribute.OBSERVED_DATA);
    }

    /**
     * Test the method {@link CallService#loadExpressionCalls(String, ExpressionCallFilter, 
     * Collection, LinkedHashMap, boolean)}.
     */
    @Test
    @Ignore("Test ignored until it is re-implemented following many modifications.")
    public void shouldLoadExpressionCallsWithFiltering() {
        //More complex query
        DAOManager manager = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(manager);
        ExpressionCallDAO dao = mock(ExpressionCallDAO.class);
        when(manager.getExpressionCallDAO()).thenReturn(dao);
        
        LinkedHashMap<ExpressionCallDAO.OrderingAttribute, DAO.Direction> orderingAttrs = 
                new LinkedHashMap<>();
        orderingAttrs.put(ExpressionCallDAO.OrderingAttribute.MEAN_RANK, DAO.Direction.DESC);
        
        ExpressionCallTOResultSet resultSetMock = getMockResultSet(ExpressionCallTOResultSet.class, 
                Arrays.asList(
                    new ExpressionCallTO(null, "geneId1", "anatEntityId1", "stageId1", 
                        new BigDecimal(1257.34), CallTO.DataState.LOWQUALITY, null, CallTO.DataState.HIGHQUALITY, 
                        null, CallTO.DataState.LOWQUALITY, null, CallTO.DataState.LOWQUALITY, null, 
                        null, null, null, null, null), 
                    new ExpressionCallTO(null, "geneId1", "anatEntityId1", "stageId2", 
                            new BigDecimal(125.42), CallTO.DataState.LOWQUALITY, null, CallTO.DataState.HIGHQUALITY, 
                            null, CallTO.DataState.LOWQUALITY, null, CallTO.DataState.LOWQUALITY, null, 
                            null, null, null, null, null)));
        
        //we'll do the verify afterwards, it's easier to catch a problem in the parameters
        when(dao.getExpressionCalls(
                //CallDAOFilters
                anyCollectionOf(CallDAOFilter.class), 
                //CallTOs
                anyCollectionOf(ExpressionCallTO.class),
                //propagation
                anyBoolean(), anyBoolean(), 
                //genes
                anyCollectionOf(String.class), 
                //orthology
                anyObject(), 
                //attributes
                anyCollectionOf(ExpressionCallDAO.Attribute.class), 
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
        Ontology<AnatEntity> anatEntityOnt = mock(Ontology.class);
        //suppress warning as we cannot specify generic type for a mock
        @SuppressWarnings("unchecked")
        Ontology<DevStage> devStageOnt = mock(Ontology.class);

        when(ontService.getAnatEntityOntology("speciesId1", new HashSet<>(Arrays.asList(
                "anatEntityId1")), EnumSet.of(RelationType.ISA_PARTOF), false, false))
        .thenReturn(anatEntityOnt);
        when(ontService.getDevStageOntology("speciesId1", new HashSet<>(Arrays.asList(
                "stageId1", "stageId2")), false, false)).thenReturn(devStageOnt);
        String anatEntityId1 = "anatEntityId1";
        AnatEntity anatEntity1 = new AnatEntity(anatEntityId1);
        String stageId1 = "stageId1";
        DevStage stage1 = new DevStage(stageId1);
        String stageId2 = "stageId2";
        DevStage stage2 = new DevStage(stageId2);

        when(anatEntityOnt.getElements()).thenReturn(new HashSet<>(Arrays.asList(anatEntity1)));
        when(devStageOnt.getElements()).thenReturn(new HashSet<>(Arrays.asList(stage1, stage2)));
        when(anatEntityOnt.getElement(anatEntityId1)).thenReturn(anatEntity1);
        when(devStageOnt.getElement(stageId1)).thenReturn(stage1);
        when(devStageOnt.getElement(stageId2)).thenReturn(stage2);
        // No relation to not propagate calls
        when(anatEntityOnt.getAncestors(anatEntity1, true)).thenReturn(new HashSet<>());
        when(devStageOnt.getAncestors(stage1, true)).thenReturn(new HashSet<>());
        when(devStageOnt.getAncestors(stage2, true)).thenReturn(new HashSet<>());

        List<ExpressionCall> expectedResults = Arrays.asList(
                new ExpressionCall("geneId1", null, 
                    true, ExpressionSummary.EXPRESSED, SummaryQuality.GOLD, 
                    Arrays.asList(
                        new ExpressionCallData(DataType.AFFYMETRIX,
                            0 /*presentHighSelfExpCount*/, 1 /*presentLowSelfExpCount*/, 0, 0,
                            0 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0), 
                        new ExpressionCallData(DataType.EST,
                            2 /*presentHighSelfExpCount*/, 0 /*presentLowSelfExpCount*/, 0, 0,
                            3 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0), 
                        new ExpressionCallData(DataType.IN_SITU,
                            0 /*presentHighSelfExpCount*/, 1 /*presentLowSelfExpCount*/, 0, 0,
                            0 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0), 
                        new ExpressionCallData(DataType.RNA_SEQ,
                            0 /*presentHighSelfExpCount*/, 1 /*presentLowSelfExpCount*/, 0, 0,
                            0 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0)), 
//                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, 
//                            new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)), 
//                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.EST, 
//                                new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)), 
//                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, 
//                                new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)), 
//                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, 
//                                new DataPropagation(PropagationState.SELF, PropagationState.SELF, true))), 
                    new BigDecimal(1257.34)), 
                new ExpressionCall("geneId1", null, 
                        true, ExpressionSummary.EXPRESSED, SummaryQuality.GOLD, 
                        Arrays.asList(
                            new ExpressionCallData(DataType.AFFYMETRIX,
                                0 /*presentHighSelfExpCount*/, 1 /*presentLowSelfExpCount*/, 0, 0,
                                0 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0), 
                            new ExpressionCallData(DataType.EST,
                                2 /*presentHighSelfExpCount*/, 0 /*presentLowSelfExpCount*/, 0, 0,
                                3 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0), 
                            new ExpressionCallData(DataType.IN_SITU,
                                0 /*presentHighSelfExpCount*/, 1 /*presentLowSelfExpCount*/, 0, 0,
                                0 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0), 
                            new ExpressionCallData(DataType.RNA_SEQ,
                                0 /*presentHighSelfExpCount*/, 1 /*presentLowSelfExpCount*/, 0, 0,
                                0 /*presentHighTotalCount*/, 1 /*presentLowTotalCount*/, 0, 0)), 
//                            new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, 
//                                    new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)), 
//                            new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.EST, 
//                                    new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)), 
//                            new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, 
//                                    new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)), 
//                            new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, 
//                                    new DataPropagation(PropagationState.SELF, PropagationState.SELF, true))), 
                        new BigDecimal(125.42))
            );
        
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = 
                new LinkedHashMap<>();
        serviceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.DESC);

        CallService service = new CallService(serviceFactory);
        List<ExpressionCall> actualResults = service.loadExpressionCalls("speciesId1", 
                new ExpressionCallFilter(new GeneFilter("geneId1"), 
                        Arrays.asList(new ConditionFilter(Arrays.asList("anatEntityId1"), 
                        Arrays.asList("stageId1", "stageId2"))),
                        Arrays.asList(DataType.EST, DataType.AFFYMETRIX),
                        SummaryQuality.GOLD, ExpressionSummary.EXPRESSED, new DataPropagation()),
                EnumSet.of(CallService.Attribute.GENE_ID, CallService.Attribute.GLOBAL_ANAT_PROPAGATION, 
                        CallService.Attribute.GLOBAL_STAGE_PROPAGATION, CallService.Attribute.CALL_DATA, 
                        CallService.Attribute.GLOBAL_DATA_QUALITY, CallService.Attribute.GLOBAL_RANK),
                serviceOrdering)
                .collect(Collectors.toList());
        assertEquals("Incorrect ExpressionCalls generated", expectedResults, actualResults);
        
        verify(dao).getExpressionCalls(
                //CallDAOFilters
                collectionEq(Arrays.asList(
                    new CallDAOFilter(null, 
                            Arrays.asList("speciesId1"), 
                            Arrays.asList(new DAOConditionFilter(Arrays.asList("anatEntityId1"), 
                                          Arrays.asList("stageId1", "stageId2")))))
                ), 
                //CallTOs
                collectionEq(Arrays.asList(new ExpressionCallTO(null, DataState.HIGHQUALITY, 
                            null, null, null, null, true), 
                        new ExpressionCallTO(DataState.LOWQUALITY, null, null, null, null, null, true))),
                //propagation
                eq(false), eq(false), 
                //genes
                collectionEq(Arrays.asList("geneId1")), 
                //orthology
                eq(null), 
                //attributes
                collectionEq(EnumSet.of(ExpressionCallDAO.Attribute.GENE_ID,
                        ExpressionCallDAO.Attribute.ANAT_ENTITY_ID,
                        ExpressionCallDAO.Attribute.STAGE_ID,
                        ExpressionCallDAO.Attribute.GLOBAL_MEAN_RANK,
                        ExpressionCallDAO.Attribute.AFFYMETRIX_DATA,
                        ExpressionCallDAO.Attribute.EST_DATA,
                        ExpressionCallDAO.Attribute.ANAT_ORIGIN_OF_LINE, 
                        ExpressionCallDAO.Attribute.STAGE_ORIGIN_OF_LINE,
                        ExpressionCallDAO.Attribute.OBSERVED_DATA)), 
                eq(orderingAttrs));
    }
    
    /**
     * Test the method {@link CallService#convertServiceOrdering(LinkedHashMap)}.
     */
    @Test
    public void shouldConvertServiceOrdering() {
        List<ExpressionCall> inputList = Arrays.asList(
                new ExpressionCall("geneId2", new Condition("anatEntityId2", "stageId3", "speciesId1"),
                        null, ExpressionSummary.EXPRESSED, null, null, null), 
                new ExpressionCall("geneId2", new Condition("anatEntityId1", null, "speciesId1"), 
                        null, ExpressionSummary.EXPRESSED, null, null, null), 
                new ExpressionCall("geneId2", new Condition("anatEntityId1", "stageId1", "speciesId1"),
                        null, ExpressionSummary.EXPRESSED, null, null, null), 
                new ExpressionCall("geneId2", new Condition("anatEntityId1", "stageId3", "speciesId1"),
                        null, ExpressionSummary.EXPRESSED, null, null, null), 
                new ExpressionCall("geneId1", new Condition("anatEntityId1", "stageId3", "speciesId1"),
                        null, ExpressionSummary.EXPRESSED, null, null, null), 
                new ExpressionCall("geneId3", new Condition("anatEntityId2", "stageId3", "speciesId1"),
                        null, ExpressionSummary.EXPRESSED, null, null, null));
        
        // Test provided order
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> orderingAttributes = new LinkedHashMap<>();
        orderingAttributes.put(CallService.OrderingAttribute.DEV_STAGE_ID, Service.Direction.DESC);
        orderingAttributes.put(CallService.OrderingAttribute.GENE_ID, Service.Direction.ASC);
        orderingAttributes.put(CallService.OrderingAttribute.ANAT_ENTITY_ID, Service.Direction.DESC);
        Comparator<ExpressionCall> comp = CallService.convertServiceOrdering(orderingAttributes);
        List<ExpressionCall> sortedList = inputList.stream().sorted(comp).collect(Collectors.toList());
        List<ExpressionCall> expectedList = Arrays.asList(
                new ExpressionCall("geneId1", new Condition("anatEntityId1", "stageId3", "speciesId1"),
                        null, ExpressionSummary.EXPRESSED, null, null, null), 
                new ExpressionCall("geneId2", new Condition("anatEntityId2", "stageId3", "speciesId1"),
                        null, ExpressionSummary.EXPRESSED, null, null, null), 
                new ExpressionCall("geneId2", new Condition("anatEntityId1", "stageId3", "speciesId1"),
                        null, ExpressionSummary.EXPRESSED, null, null, null), 
                new ExpressionCall("geneId3", new Condition("anatEntityId2", "stageId3", "speciesId1"),
                        null, ExpressionSummary.EXPRESSED, null, null, null), 
                new ExpressionCall("geneId2", new Condition("anatEntityId1", "stageId1", "speciesId1"),
                        null, ExpressionSummary.EXPRESSED, null, null, null), 
                new ExpressionCall("geneId2", new Condition("anatEntityId1", null, "speciesId1"), 
                        null, ExpressionSummary.EXPRESSED, null, null, null));
        assertEquals("Incorrect order of ExpressionCalls", expectedList, sortedList);
        
        // Test default order (natural order of enum CallService.OrderingAttribute: 
        // GENE_ID, ANAT_ENTITY_ID, DEV_STAGE_ID, GLOBAL_RANK)
        comp = CallService.convertServiceOrdering(null);
        sortedList = inputList.stream().sorted(comp).collect(Collectors.toList());
        expectedList = Arrays.asList(
                new ExpressionCall("geneId1", new Condition("anatEntityId1", "stageId3", "speciesId1"),
                        null, ExpressionSummary.EXPRESSED, null, null, null), 
                new ExpressionCall("geneId2", new Condition("anatEntityId1", "stageId1", "speciesId1"),
                        null, ExpressionSummary.EXPRESSED, null, null, null), 
                new ExpressionCall("geneId2", new Condition("anatEntityId1", "stageId3", "speciesId1"),
                        null, ExpressionSummary.EXPRESSED, null, null, null), 
                new ExpressionCall("geneId2", new Condition("anatEntityId1", null, "speciesId1"), 
                        null, ExpressionSummary.EXPRESSED, null, null, null), 
                new ExpressionCall("geneId2", new Condition("anatEntityId2", "stageId3", "speciesId1"),
                        null, ExpressionSummary.EXPRESSED, null, null, null), 
                new ExpressionCall("geneId3", new Condition("anatEntityId2", "stageId3", "speciesId1"),
                        null, ExpressionSummary.EXPRESSED, null, null, null));
    }
    
}
