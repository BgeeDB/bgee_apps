package org.bgee.model.expressiondata;

import static org.junit.Assert.assertEquals;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.TestAncestor;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.CallDAOFilter;
import org.bgee.model.dao.api.expressiondata.DAOConditionFilter;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTOResultSet;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.baseelements.CallType.Expression;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.DataPropagation.PropagationState;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.gene.GeneFilter;
import org.junit.Test;

/**
 * Unit tests for {@link CallService}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Nov. 2015
 * @since Bgee 13 Nov. 2015
 */
public class CallServiceTest extends TestAncestor {
    private final static Logger log = LogManager.getLogger(CallServiceTest.class.getName());
    @Override
    protected Logger getLogger() {
        return log;
    } 
    
    /**
     * Test the method {@link CallService#loadExpressionCalls(String, ExpressionCallFilter, 
     * Collection, LinkedHashMap)}.
     */
    @Test
    public void shoudLoadExpressionCallsForBasicGene() {
        //First test for one gene, no substructures no sub-stages. 
        //Retrieving geneId, anatEntityId, stageId, and data qualities, ordered by mean rank. 
        DAOManager manager = mock(DAOManager.class);
        ExpressionCallDAO dao = mock(ExpressionCallDAO.class);
        when(manager.getExpressionCallDAO()).thenReturn(dao);
        
        LinkedHashMap<ExpressionCallDAO.OrderingAttribute, DAO.Direction> orderingAttrs = 
                new LinkedHashMap<>();
        orderingAttrs.put(ExpressionCallDAO.OrderingAttribute.MEAN_RANK, DAO.Direction.DESC);
        
        ExpressionCallTOResultSet resultSetMock = getMockResultSet(ExpressionCallTOResultSet.class, 
                Arrays.asList(
                    new ExpressionCallTO(null, "geneId1", "anatEntityId1", "stageId1", 
                        null, CallTO.DataState.LOWQUALITY, null, CallTO.DataState.HIGHQUALITY, 
                        null, CallTO.DataState.LOWQUALITY, null, CallTO.DataState.LOWQUALITY, null, 
                        null, null, null, null, null), 
                    new ExpressionCallTO(null, "geneId1", "anatEntityId1", "stageId2", 
                            null, CallTO.DataState.LOWQUALITY, null, CallTO.DataState.HIGHQUALITY, 
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
        
        List<ExpressionCall> expectedResults = Arrays.asList(
                new ExpressionCall("geneId1", new Condition("anatEntityId1", "stageId1"), 
                    new DataPropagation(PropagationState.SELF, PropagationState.SELF, true), 
                    ExpressionSummary.EXPRESSED, DataQuality.HIGH, 
                    Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, 
                            new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)), 
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.EST, 
                                new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)), 
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, 
                                new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)), 
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, 
                                new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)))), 
                new ExpressionCall("geneId1", new Condition("anatEntityId1", "stageId2"), 
                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true), 
                        ExpressionSummary.EXPRESSED, DataQuality.HIGH, 
                        Arrays.asList(
                            new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, 
                                new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)), 
                            new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.EST, 
                                    new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)), 
                            new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, 
                                    new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)), 
                            new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, 
                                    new DataPropagation(PropagationState.SELF, PropagationState.SELF, true))))
            );
        
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = 
                new LinkedHashMap<>();
        serviceOrdering.put(CallService.OrderingAttribute.RANK, Service.Direction.DESC);
        
        CallService service = new CallService(manager);
        List<ExpressionCall> actualResults = service.loadExpressionCalls("speciesId1", 
                new ExpressionCallFilter(new GeneFilter("geneId1"), null, new DataPropagation(), 
                        Arrays.asList(new ExpressionCallData(Expression.EXPRESSED))), 
                EnumSet.of(CallService.Attribute.GENE_ID, CallService.Attribute.ANAT_ENTITY_ID, 
                        CallService.Attribute.DEV_STAGE_ID, CallService.Attribute.CALL_DATA, 
                        CallService.Attribute.GLOBAL_DATA_QUALITY), 
                serviceOrdering)
                .collect(Collectors.toList());
        assertEquals("Incorrect ExpressionCalls generated", expectedResults, actualResults);
        
        verify(dao).getExpressionCalls(
                //CallDAOFilters
                collectionEq(Arrays.asList(
                    new CallDAOFilter(null, Arrays.asList("speciesId1"), null))
                ), 
                //CallTOs
                collectionEq(new HashSet<ExpressionCallTO>()),
                //propagation
                eq(false), eq(false), 
                //genes
                collectionEq(Arrays.asList("geneId1")), 
                //orthology
                eq(null), 
                //attributes
                collectionEq(EnumSet.allOf(ExpressionCallDAO.Attribute.class).stream()
                        .filter(attr -> attr != ExpressionCallDAO.Attribute.ID && 
                                        !attr.isPropagationAttribute() && 
                                        !attr.isRankAttribute())
                        .collect(Collectors.toSet())), 
                eq(orderingAttrs));
    }
    
    /**
     * Test the method {@link CallService#loadExpressionCalls(String, ExpressionCallFilter, 
     * Collection, LinkedHashMap)}.
     */
    @Test
    public void shoudLoadExpressionCallsForSeveralGenes() {
        //Retrieving geneId, anatEntityId, unordered. 
        DAOManager manager = mock(DAOManager.class);
        ExpressionCallDAO dao = mock(ExpressionCallDAO.class);
        when(manager.getExpressionCallDAO()).thenReturn(dao);
        
        ExpressionCallTOResultSet resultSetMock = getMockResultSet(ExpressionCallTOResultSet.class, 
                Arrays.asList(
                    new ExpressionCallTO(null, "geneId1", "anatEntityId1", null,  
                        null, null, null, null, null, null, null, null, null, 
                        null, null, null, null, null), 
                    new ExpressionCallTO(null, "geneId1", "anatEntityId2", null,  
                            null, null, null, null, null, null, null, null, null, 
                            null, null, null, null, null), 
                    new ExpressionCallTO(null, "geneId2", "anatEntityId1", null,  
                        null, null, null, null, null, null, null, null, null, 
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
        
        List<ExpressionCall> expectedResults = Arrays.asList(
                new ExpressionCall("geneId1", new Condition("anatEntityId1", null), 
                    new DataPropagation(PropagationState.SELF, PropagationState.SELF, true), 
                    ExpressionSummary.EXPRESSED, null, 
                    new HashSet<>()), 
                new ExpressionCall("geneId1", new Condition("anatEntityId2", null), 
                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true), 
                        ExpressionSummary.EXPRESSED, null, 
                        new HashSet<>()), 
                new ExpressionCall("geneId2", new Condition("anatEntityId1", null), 
                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true), 
                        ExpressionSummary.EXPRESSED, null, 
                        new HashSet<>())
            );
        
        CallService service = new CallService(manager);
        List<ExpressionCall> actualResults = service.loadExpressionCalls("speciesId1", 
                new ExpressionCallFilter(null, null, new DataPropagation(), 
                        Arrays.asList(new ExpressionCallData(Expression.EXPRESSED))), 
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
                collectionEq(new HashSet<ExpressionCallTO>()),
                //propagation
                eq(false), eq(false), 
                //genes
                collectionEq(new HashSet<>()), 
                //orthology
                eq(null), 
                //attributes
                collectionEq(EnumSet.allOf(ExpressionCallDAO.Attribute.class).stream()
                        .filter(attr -> attr == ExpressionCallDAO.Attribute.GENE_ID ||  
                                        attr == ExpressionCallDAO.Attribute.ANAT_ENTITY_ID)
                        .collect(Collectors.toSet())), 
                eq(new LinkedHashMap<>()));
    }

    /**
     * Test the method {@link CallService#loadExpressionCalls(String, ExpressionCallFilter, 
     * Collection, LinkedHashMap)}.
     */
    @Test
    public void shoudLoadExpressionCallsWithFiltering() {
        //More complex query
        DAOManager manager = mock(DAOManager.class);
        ExpressionCallDAO dao = mock(ExpressionCallDAO.class);
        when(manager.getExpressionCallDAO()).thenReturn(dao);
        
        LinkedHashMap<ExpressionCallDAO.OrderingAttribute, DAO.Direction> orderingAttrs = 
                new LinkedHashMap<>();
        orderingAttrs.put(ExpressionCallDAO.OrderingAttribute.MEAN_RANK, DAO.Direction.DESC);
        
        ExpressionCallTOResultSet resultSetMock = getMockResultSet(ExpressionCallTOResultSet.class, 
                Arrays.asList(
                    new ExpressionCallTO(null, "geneId1", "anatEntityId1", "stageId1", 
                        null, CallTO.DataState.LOWQUALITY, null, CallTO.DataState.HIGHQUALITY, 
                        null, CallTO.DataState.LOWQUALITY, null, CallTO.DataState.LOWQUALITY, null, 
                        null, null, null, null, null), 
                    new ExpressionCallTO(null, "geneId1", "anatEntityId1", "stageId2", 
                            null, CallTO.DataState.LOWQUALITY, null, CallTO.DataState.HIGHQUALITY, 
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
        
        List<ExpressionCall> expectedResults = Arrays.asList(
                new ExpressionCall("geneId1", new Condition("anatEntityId1", "stageId1"), 
                    new DataPropagation(PropagationState.SELF_OR_DESCENDANT, 
                            PropagationState.SELF_OR_DESCENDANT, null), 
                    ExpressionSummary.EXPRESSED, DataQuality.HIGH, 
                    Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, 
                            new DataPropagation(PropagationState.SELF_OR_DESCENDANT, 
                                    PropagationState.SELF_OR_DESCENDANT, null)), 
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.EST, 
                                new DataPropagation(PropagationState.SELF_OR_DESCENDANT, 
                                        PropagationState.SELF_OR_DESCENDANT, null)), 
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, 
                                new DataPropagation(PropagationState.SELF_OR_DESCENDANT, 
                                        PropagationState.SELF_OR_DESCENDANT, null)), 
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, 
                                new DataPropagation(PropagationState.SELF_OR_DESCENDANT, 
                                        PropagationState.SELF_OR_DESCENDANT, null)))), 
                new ExpressionCall("geneId1", new Condition("anatEntityId1", "stageId2"), 
                        new DataPropagation(PropagationState.SELF_OR_DESCENDANT, 
                                PropagationState.SELF_OR_DESCENDANT, null), 
                        ExpressionSummary.EXPRESSED, DataQuality.HIGH, 
                        Arrays.asList(
                            new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, 
                                    new DataPropagation(PropagationState.SELF_OR_DESCENDANT, 
                                            PropagationState.SELF_OR_DESCENDANT, null)), 
                            new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.EST, 
                                    new DataPropagation(PropagationState.SELF_OR_DESCENDANT, 
                                            PropagationState.SELF_OR_DESCENDANT, null)), 
                            new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, 
                                    new DataPropagation(PropagationState.SELF_OR_DESCENDANT, 
                                            PropagationState.SELF_OR_DESCENDANT, null)), 
                            new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, 
                                    new DataPropagation(PropagationState.SELF_OR_DESCENDANT, 
                                            PropagationState.SELF_OR_DESCENDANT, null))))
            );
        
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = 
                new LinkedHashMap<>();
        serviceOrdering.put(CallService.OrderingAttribute.RANK, Service.Direction.DESC);
        
        CallService service = new CallService(manager);
        List<ExpressionCall> actualResults = service.loadExpressionCalls("speciesId1", 
                new ExpressionCallFilter(new GeneFilter("geneId1"), 
                        Arrays.asList(new ConditionFilter(Arrays.asList("anatEntityId1"), 
                                Arrays.asList("stageId1", "stageId2"))), 
                        new DataPropagation(PropagationState.SELF_OR_DESCENDANT, 
                               PropagationState.SELF_OR_DESCENDANT, true), 
                        Arrays.asList(new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.EST), 
                                new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX))), 
                EnumSet.of(CallService.Attribute.GENE_ID, CallService.Attribute.ANAT_ENTITY_ID, 
                        CallService.Attribute.DEV_STAGE_ID, CallService.Attribute.CALL_DATA, 
                        CallService.Attribute.GLOBAL_DATA_QUALITY), 
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
                eq(true), eq(true), 
                //genes
                collectionEq(Arrays.asList("geneId1")), 
                //orthology
                eq(null), 
                //attributes
                collectionEq(EnumSet.allOf(ExpressionCallDAO.Attribute.class).stream()
                        .filter(attr -> attr != ExpressionCallDAO.Attribute.ID && 
                                        attr != ExpressionCallDAO.Attribute.IN_SITU_DATA && 
                                        attr != ExpressionCallDAO.Attribute.RNA_SEQ_DATA && 
                                        !attr.isPropagationAttribute() && 
                                        !attr.isRankAttribute())
                        .collect(Collectors.toSet())), 
                eq(orderingAttrs));
    }
}
