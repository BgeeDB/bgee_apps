package org.bgee.model.expressiondata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
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
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.CallDAOFilter;
import org.bgee.model.dao.api.expressiondata.DAOConditionFilter;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO.OriginOfLine;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTOResultSet;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTO;
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
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 13, July 2016
 * @since   Bgee 13, Nov. 2015
 */
public class CallServiceTest extends TestAncestor {
    
    private final static Logger log = LogManager.getLogger(CallServiceTest.class.getName());
    
    private final static DataPropagation dpSelfAndSelf = new DataPropagation(PropagationState.SELF, PropagationState.SELF, true);
    private final static DataPropagation dpSelfAndDesc = new DataPropagation(PropagationState.SELF, PropagationState.DESCENDANT, false);
    private final static DataPropagation dpSelfAndSelfDesc = new DataPropagation(PropagationState.SELF, PropagationState.SELF_AND_DESCENDANT, true);
    private final static DataPropagation dpSelfDescAndAll= new DataPropagation(PropagationState.SELF_AND_DESCENDANT, PropagationState.ALL, true);
    private final static DataPropagation dpDescAndSelf = new DataPropagation(PropagationState.DESCENDANT, PropagationState.SELF, false);
    private final static DataPropagation dpDescAndDesc = new DataPropagation(PropagationState.DESCENDANT, PropagationState.DESCENDANT, false);
    private final static DataPropagation dpAncAndSelf = new DataPropagation(PropagationState.ANCESTOR, PropagationState.SELF, false);
    private final static DataPropagation dpSelfAncAndSelf = new DataPropagation(PropagationState.SELF_AND_ANCESTOR, PropagationState.SELF, true);
    private final static DataPropagation dpSelfDescAndDesc = new DataPropagation(PropagationState.SELF_AND_DESCENDANT, PropagationState.DESCENDANT, false);
    private final static DataPropagation dpSelfDescAndSelf = new DataPropagation(PropagationState.SELF_AND_DESCENDANT, PropagationState.SELF, true);
    @Override
    protected Logger getLogger() {
        return log;
    } 
    
    /**
     * Test the method {@link CallService#loadExpressionCalls(String, ExpressionCallFilter, 
     * Collection, LinkedHashMap)}.
     */
    @Test
    public void shouldLoadExpressionCallsForBasicGene() {
        //First test for one gene, no substructures no sub-stages. 
        //Retrieving geneId, anatEntityId, stageId, and data qualities, ordered by mean rank. 
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
                new ExpressionCall("geneId1", new Condition("anatEntityId1", "stageId1", "speciesId1"), 
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
                                new DataPropagation(PropagationState.SELF, PropagationState.SELF, true))), 
                    null), 
                new ExpressionCall("geneId1", new Condition("anatEntityId1", "stageId2", "speciesId1"), 
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
                                    new DataPropagation(PropagationState.SELF, PropagationState.SELF, true))), 
                        null)
            );
        
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = 
                new LinkedHashMap<>();
        serviceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.DESC);
        
        CallService service = new CallService(serviceFactory);
        List<ExpressionCall> actualResults = service.loadExpressionCalls("speciesId1", 
                new ExpressionCallFilter(new GeneFilter("geneId1"), null, 
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
                collectionEq(Arrays.asList(new ExpressionCallTO(
                        null, null, null, null, null, null, true))),
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
    public void shouldLoadExpressionCallsForSeveralGenes() {
        //Retrieving geneId, anatEntityId, unordered. 
        DAOManager manager = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(manager);
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
                new ExpressionCall("geneId1", new Condition("anatEntityId1", null, "speciesId1"), 
                    new DataPropagation(PropagationState.SELF, PropagationState.SELF, true), 
                    ExpressionSummary.EXPRESSED, null, 
                    new HashSet<>(), null), 
                new ExpressionCall("geneId1", new Condition("anatEntityId2", null, "speciesId1"), 
                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true), 
                        ExpressionSummary.EXPRESSED, null, 
                        new HashSet<>(), null), 
                new ExpressionCall("geneId2", new Condition("anatEntityId1", null, "speciesId1"), 
                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true), 
                        ExpressionSummary.EXPRESSED, null, 
                        new HashSet<>(), null)
            );
        
        CallService service = new CallService(serviceFactory);
        List<ExpressionCall> actualResults = service.loadExpressionCalls("speciesId1", 
                new ExpressionCallFilter(null, null, 
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
                collectionEq(Arrays.asList(new ExpressionCallTO(
                        null, null, null, null, null, null, true))),
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
        
        List<ExpressionCall> expectedResults = Arrays.asList(
                new ExpressionCall("geneId1", new Condition("anatEntityId1", "stageId1", "speciesId1"), 
                    new DataPropagation(PropagationState.SELF, PropagationState.SELF, true), 
                    ExpressionSummary.EXPRESSED, DataQuality.HIGH, 
                    Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, 
                            new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)), 
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.EST, 
                                new DataPropagation(PropagationState.SELF, 
                                        PropagationState.SELF, true)), 
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, 
                                new DataPropagation(PropagationState.SELF, 
                                        PropagationState.SELF, true)), 
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, 
                                new DataPropagation(PropagationState.SELF, 
                                        PropagationState.SELF, true))), 
                    new BigDecimal(1257.34)), 
                new ExpressionCall("geneId1", new Condition("anatEntityId1", "stageId2", "speciesId1"), 
                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true), 
                        ExpressionSummary.EXPRESSED, DataQuality.HIGH, 
                        Arrays.asList(
                            new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, 
                                    new DataPropagation(PropagationState.SELF, 
                                            PropagationState.SELF, true)), 
                            new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.EST, 
                                    new DataPropagation(PropagationState.SELF, 
                                            PropagationState.SELF, true)), 
                            new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, 
                                    new DataPropagation(PropagationState.SELF, 
                                            PropagationState.SELF, true)), 
                            new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, 
                                    new DataPropagation(PropagationState.SELF, 
                                            PropagationState.SELF, true))), 
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
                        Arrays.asList(new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.EST), 
                                new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX))), 
                EnumSet.of(CallService.Attribute.GENE_ID, CallService.Attribute.ANAT_ENTITY_ID, 
                        CallService.Attribute.DEV_STAGE_ID, CallService.Attribute.CALL_DATA, 
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
                collectionEq(EnumSet.allOf(ExpressionCallDAO.Attribute.class).stream()
                        .filter(attr -> attr != ExpressionCallDAO.Attribute.ID && 
                                        attr != ExpressionCallDAO.Attribute.IN_SITU_DATA && 
                                        attr != ExpressionCallDAO.Attribute.RNA_SEQ_DATA && 
                                        !attr.isPropagationAttribute() && 
                                        (!attr.isRankAttribute() || attr == ExpressionCallDAO.Attribute.GLOBAL_MEAN_RANK))
                        .collect(Collectors.toSet())), 
                eq(orderingAttrs));
    }
    
    /**
     * Test the method 
     * {@link CallService#propagateExpressionTOs(Collection, Collection, ConditionUtils, String)}.
     */
    @Test
    public void shouldPropagateExpressionTOs() {
        DAOManager manager = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(manager);

        CallService service = new CallService(serviceFactory);
        try {
            service.propagateExpressionTOs(null, null, null, null);
            fail("Should throw an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // test passed
        }

        ConditionUtils mockConditionUtils = mock(ConditionUtils.class);
        String speciesId = "speciesId1";
        
        Set<Condition> conditions = new HashSet<>(Arrays.asList(
                new Condition("organA", "stageA",speciesId),
                new Condition("organA", "parentStageA1", speciesId),
                new Condition("parentOrganA1", "stageA", speciesId),
                new Condition("parentOrganA1", "parentStageA1", speciesId),
                new Condition("organB", "stageB", speciesId)));
        when(mockConditionUtils.getConditions()).thenReturn(conditions);
        when(mockConditionUtils.isInferredAncestralConditions()).thenReturn(true);
        
        Condition childCond = new Condition("organA", "stageA", speciesId);
        Set<Condition> ancestorConds = new HashSet<>(Arrays.asList(
                new Condition("organA", "parentStageA1", speciesId),
                new Condition("parentOrganA1", "stageA", speciesId),
                new Condition("parentOrganA1", "parentStageA1", speciesId)));
        when(mockConditionUtils.getAncestorConditions(childCond, true)).thenReturn(ancestorConds);
        
        childCond = new Condition("organA", "parentStageA1", speciesId);
        ancestorConds = new HashSet<>(Arrays.asList(
                new Condition("parentOrganA1", "parentStageA1", speciesId)));
        when(mockConditionUtils.getAncestorConditions(childCond, true)).thenReturn(ancestorConds);

        childCond = new Condition("parentOrganA1", "parentStageA1", speciesId);
        ancestorConds = new HashSet<>();
        when(mockConditionUtils.getAncestorConditions(childCond, true)).thenReturn(ancestorConds);

        childCond = new Condition("organB", "stageB", speciesId);
        ancestorConds = new HashSet<>(Arrays.asList(new Condition("organB", "parentStageB1", speciesId)));
        when(mockConditionUtils.getAncestorConditions(childCond, true)).thenReturn(ancestorConds);

        Collection<ExpressionCallTO> exprTOs = Arrays.asList(
                // ExpressionCallTO 1
                new ExpressionCallTO("1", "geneA", "organA", "stageA", null,
                        DataState.LOWQUALITY, null, DataState.NODATA, null,
                        DataState.NODATA, null, DataState.HIGHQUALITY, null,
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                // ExpressionCallTO 2
                new ExpressionCallTO("2", "geneA", "organA", "parentStageA1", null,
                        DataState.NODATA,  null, DataState.HIGHQUALITY, null,
                        DataState.HIGHQUALITY, null, DataState.NODATA, null,
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                // ExpressionCallTO 3
                new ExpressionCallTO("3", "geneB", "parentOrganA1", "parentStageA1", null,
                        DataState.NODATA, null, DataState.NODATA, null,
                        DataState.NODATA, null, DataState.LOWQUALITY, null,
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                // ExpressionCallTO 4
                new ExpressionCallTO("4", "geneB", "organB", "stageB", null,
                        DataState.HIGHQUALITY, null, DataState.NODATA, null,
                        DataState.NODATA, null, DataState.NODATA, null,
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true));

        Set<ExpressionCall> allResults = new HashSet<>(Arrays.asList(
                // From ExpressionCallTO 1
                new ExpressionCall("geneA", new Condition("organA", "stageA", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)), 
                        null), 
                
                new ExpressionCall("geneA", new Condition("organA", "parentStageA1", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndDesc),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndDesc)), 
                        null), 

                new ExpressionCall("geneA", new Condition("parentOrganA1", "stageA", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpDescAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpDescAndSelf)), 
                        null), 

                new ExpressionCall("geneA", new Condition("parentOrganA1", "parentStageA1", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpDescAndDesc),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpDescAndDesc)), 
                        null), 

                // From ExpressionCallTO 2
                new ExpressionCall("geneA", new Condition("organA", "parentStageA1", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.EST, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpSelfAndSelf)), 
                        null), 
                new ExpressionCall("geneA", new Condition("parentOrganA1", "parentStageA1", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.EST, dpDescAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpDescAndSelf)), 
                        null), 

                // From ExpressionCallTO 3
                new ExpressionCall("geneB", new Condition("parentOrganA1", "parentStageA1", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, dpSelfAndSelf)), 
                        null), 

                // From ExpressionCallTO 4
                new ExpressionCall("geneB", new Condition("organB", "stageB", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelf)), 
                        null), 
                new ExpressionCall("geneB", new Condition("organB", "parentStageB1", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndDesc)), 
                        null)));

        Set<ExpressionCall> actualResults = service.propagateExpressionTOs(
                exprTOs, null, mockConditionUtils, speciesId);
        assertEquals("Incorrect ExpressionCalls generated", allResults, actualResults);
        
        Set<String> allowedOrganIds = new HashSet<>(Arrays.asList("organA"));
        Set<ExpressionCall> expectedResults = allResults.stream()
                .filter(c -> allowedOrganIds.contains(c.getCondition().getAnatEntityId()))
                .collect(Collectors.toSet());
        actualResults = service.propagateExpressionTOs(exprTOs, Arrays.asList(
                new ConditionFilter(allowedOrganIds, null)), mockConditionUtils, speciesId);
        assertEquals("Incorrect ExpressionCalls generated", expectedResults, actualResults);

        Set<String> allowedStageIds = new HashSet<>(Arrays.asList("parentStageA1"));
        expectedResults = allResults.stream()
                .filter(c -> allowedStageIds.contains(c.getCondition().getDevStageId()))
                .collect(Collectors.toSet());
        actualResults = service.propagateExpressionTOs(exprTOs, Arrays.asList(
                new ConditionFilter(null, allowedStageIds)), mockConditionUtils, speciesId);
        assertEquals("Incorrect ExpressionCalls generated", expectedResults, actualResults);

        expectedResults = allResults.stream()
                .filter(c -> allowedOrganIds.contains(c.getCondition().getAnatEntityId()))
                .filter(c -> allowedStageIds.contains(c.getCondition().getDevStageId()))
                .collect(Collectors.toSet());
        actualResults = service.propagateExpressionTOs(exprTOs, Arrays.asList(
                new ConditionFilter(allowedOrganIds, allowedStageIds)), mockConditionUtils, speciesId);
        assertEquals("Incorrect ExpressionCalls generated", expectedResults, actualResults);
    }
    
    /**
     * Test the method {@link CallService#propagateNoExpressionTOs(Collection, Set, Set, ConditionUtils)}.
     */
    @Test
    public void shouldPropagateNoExpressionTOs() {
        DAOManager manager = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(manager);

        CallService service = new CallService(serviceFactory);
        try {
            service.propagateNoExpressionTOs(null, null, null, null);
            fail("Should throw an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // test passed
        }

        ConditionUtils mockConditionUtils = mock(ConditionUtils.class);
        String speciesId = "speciesId1";

        Set<Condition> conditions = new HashSet<>(Arrays.asList(
                new Condition("organA", "stageA", speciesId),
                new Condition("organA", "parentStageA1", speciesId),
                new Condition("parentOrganA1", "parentStageA1", speciesId),
                new Condition("parentOrganA2", "parentStageA1", speciesId),
                new Condition("organB", "stageB", speciesId),
                new Condition("parentOrganB1", "stageB", speciesId),
                new Condition("parentOrganB2", "stageB", speciesId)));
        when(mockConditionUtils.getConditions()).thenReturn(conditions);
        when(mockConditionUtils.isInferredAncestralConditions()).thenReturn(true);
        
        Condition parentCond = new Condition("organA", "stageA", speciesId);
        Set<Condition> descendantConds = new HashSet<>();
        when(mockConditionUtils.getDescendantConditions(parentCond, true)).thenReturn(descendantConds);
        
        parentCond = new Condition("organA", "parentStageA1", speciesId);
        descendantConds = new HashSet<>();
        when(mockConditionUtils.getDescendantConditions(parentCond, true)).thenReturn(descendantConds);

        parentCond = new Condition("parentOrganA2", "parentStageA1", speciesId);
        descendantConds = new HashSet<>(Arrays.asList(
                new Condition("parentOrganA1", "parentStageA1", speciesId),
                new Condition("organA", "parentStageA1", speciesId)));
        when(mockConditionUtils.getDescendantConditions(parentCond, true)).thenReturn(descendantConds);

        parentCond = new Condition("parentOrganB2", "stageB", speciesId);
        descendantConds = new HashSet<>(Arrays.asList(
                new Condition("parentOrganB1", "stageB", speciesId),
                new Condition("organB", "stageB", speciesId)));
        when(mockConditionUtils.getDescendantConditions(parentCond, true)).thenReturn(descendantConds);

        Collection<NoExpressionCallTO> noExprTOs = Arrays.asList(
                // NoExpressionCallTO 1
                new NoExpressionCallTO("1", "geneA", "organA", "stageA",
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.NODATA, DataState.NODATA,
                        false, NoExpressionCallTO.OriginOfLine.SELF),
                // NoExpressionCallTO 2
                new NoExpressionCallTO("2", "geneA", "organA", "parentStageA1",
                        DataState.LOWQUALITY, DataState.NODATA,  DataState.NODATA, DataState.HIGHQUALITY,
                        false, NoExpressionCallTO.OriginOfLine.SELF),
                // NoExpressionCallTO 3
                new NoExpressionCallTO("3", "geneA", "parentOrganA2", "parentStageA1",
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, DataState.LOWQUALITY,
                        false, NoExpressionCallTO.OriginOfLine.SELF),
                // NoExpressionCallTO 4
                new NoExpressionCallTO("4", "geneB", "parentOrganB2", "stageB",
                        DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, DataState.NODATA,
                        false, NoExpressionCallTO.OriginOfLine.SELF));

        Set<ExpressionCall> allResults = new HashSet<>(Arrays.asList(
                // From NoExpressionCallTO 1
                new ExpressionCall("geneA", new Condition("organA", "stageA", speciesId), 
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndSelf),
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpSelfAndSelf)), 
                        null), 
                
                // From NoExpressionCallTO 2
                new ExpressionCall("geneA", new Condition("organA", "parentStageA1", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndSelf),
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)), 
                        null), 

                // From NoExpressionCallTO 3
                new ExpressionCall("geneA", new Condition("parentOrganA2", "parentStageA1", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelf),
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpSelfAndSelf), 
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, dpSelfAndSelf)), 
                        null), 

                new ExpressionCall("geneA", new Condition("parentOrganA1", "parentStageA1", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpAncAndSelf),
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpAncAndSelf), 
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, dpAncAndSelf)), 
                        null), 

                new ExpressionCall("geneA", new Condition("organA", "parentStageA1", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpAncAndSelf),
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpAncAndSelf), 
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, dpAncAndSelf)), 
                        null), 

                // From NoExpressionCallTO 4
                new ExpressionCall("geneB", new Condition("parentOrganB2", "stageB", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelf)), 
                        null), 

                new ExpressionCall("geneB", new Condition("parentOrganB1", "stageB", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpAncAndSelf)), 
                        null), 

                new ExpressionCall("geneB", new Condition("organB", "stageB", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpAncAndSelf)), 
                        null)));

        Set<ExpressionCall> actualResults = service.propagateNoExpressionTOs(
                noExprTOs, null, mockConditionUtils, speciesId);
        assertEquals("Incorrect ExpressionCalls generated", allResults, actualResults);
        
        Set<String> allowedOrganIds = new HashSet<>(Arrays.asList("organA"));
        Set<ExpressionCall> expectedResults = allResults.stream()
                .filter(c -> allowedOrganIds.contains(c.getCondition().getAnatEntityId()))
                .collect(Collectors.toSet());
        actualResults = service.propagateNoExpressionTOs(
                noExprTOs, Arrays.asList(new ConditionFilter(allowedOrganIds, null)),
                mockConditionUtils, speciesId);
        assertEquals("Incorrect ExpressionCalls generated", expectedResults, actualResults);

        Set<String> allowedStageIds = new HashSet<>(Arrays.asList("parentStageA1"));
        expectedResults = allResults.stream()
                .filter(c -> allowedStageIds.contains(c.getCondition().getDevStageId()))
                .collect(Collectors.toSet());
        actualResults = service.propagateNoExpressionTOs(
                noExprTOs, Arrays.asList(new ConditionFilter(null, allowedStageIds)),
                mockConditionUtils, speciesId);
        assertEquals("Incorrect ExpressionCalls generated", expectedResults, actualResults);

        expectedResults = allResults.stream()
                .filter(c -> allowedOrganIds.contains(c.getCondition().getAnatEntityId()))
                .filter(c -> allowedStageIds.contains(c.getCondition().getDevStageId()))
                .collect(Collectors.toSet());
        actualResults = service.propagateNoExpressionTOs(
                noExprTOs, Arrays.asList(new ConditionFilter(allowedOrganIds, allowedStageIds)),
                mockConditionUtils, speciesId);
        assertEquals("Incorrect ExpressionCalls generated", expectedResults, actualResults);
    }
    
    /**
     * @return  the {@code Set} of {@code ExpressionCall}s from propagated {@code ExpressionCallTO}s,
     */
    private Set<ExpressionCall> getPropagationFromNoExpressionTOs(String speciesId) {
        return new HashSet<>(Arrays.asList(
                new ExpressionCall("ID1", new Condition("Anat_id1", "Stage_id1", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelf)), 
                        null), 
                new ExpressionCall("ID1", new Condition("Anat_id2", "Stage_id1", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelf),
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpAncAndSelf)), 
                        null), 

                new ExpressionCall("ID2", new Condition("Anat_id1", "Stage_id2", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)), 
                        null), 
                new ExpressionCall("ID2", new Condition("Anat_id2", "Stage_id2", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpAncAndSelf)), 
                        null), 
                new ExpressionCall("ID2", new Condition("Anat_id3", "ParentStage_id2", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)), 
                        null), 
                new ExpressionCall("ID2", new Condition("Anat_id3", "Stage_id2", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpAncAndSelf)), 
                        null), 

                new ExpressionCall("ID4", new Condition("Anat_id1", "Stage_id5", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpSelfAndSelf),
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)), 
                        null), 
                new ExpressionCall("ID4", new Condition("Anat_id4", "Stage_id5", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAncAndSelf), 
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpSelfAncAndSelf), 
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAncAndSelf)), 
                        null), 
                new ExpressionCall("ID4", new Condition("Anat_id5", "Stage_id5", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpAncAndSelf), 
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpAncAndSelf), 
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpAncAndSelf)), 
                        null), 

                new ExpressionCall("ID5", new Condition("Anat_id1", "Stage_id5", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)), 
                        null), 
                new ExpressionCall("ID5", new Condition("Anat_id5", "Stage_id5", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpAncAndSelf)), 
                        null),
                
                new ExpressionCall("ID6", new Condition("Anat_id9", "Stage_id7", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)), 
                        null),
                new ExpressionCall("ID6", new Condition("Anat_id8", "Stage_id6", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpAncAndSelf)), 
                        null)));
    }
    
    /**
     * @return  the {@code Set} of {@code NoExpressionCall}s from propagated {@code ExpressionCallTO}s,
     */
    private Set<ExpressionCall> getPropagationFromExpressionTOs(String speciesId) {
        
        return new HashSet<>(Arrays.asList(
                new ExpressionCall("ID1", new Condition("Anat_id1", "Stage_id1", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, dpSelfAndSelf)), 
                        null), 
                new ExpressionCall("ID1", new Condition("Anat_id1", "ParentStage_id1", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndDesc),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpSelfAndDesc),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, dpSelfAndDesc)), 
                        null), 
                new ExpressionCall("ID1", new Condition("Anat_id1", "ParentStage_id2", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.EST, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpSelfAndSelf)), 
                        null), 
                new ExpressionCall("ID1", new Condition("Anat_id1", "Stage_id2", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpSelfAndSelf)), 
                        null), 
                
                new ExpressionCall("ID2", new Condition("Anat_id1", "Stage_id2", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndSelf)), 
                        null), 
                new ExpressionCall("ID2", new Condition("Anat_id1", "ParentStage_id2", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfDescAndDesc),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfDescAndDesc),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpSelfDescAndDesc)), 
                        null), 
                new ExpressionCall("ID2", new Condition("Anat_id2", "ParentStage_id2", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpDescAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpDescAndSelf)), 
                        null), 
                new ExpressionCall("ID2", new Condition("Anat_id3", "ParentStage_id2", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpSelfAndSelf)), 
                        null), 
                new ExpressionCall("ID2", new Condition("NonInfoAnatEnt1", "ParentStage_id2", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpSelfAndSelf)), 
                        null), 

                new ExpressionCall("ID3", new Condition("Anat_id1", "Stage_id2", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfDescAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfDescAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpSelfDescAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfDescAndSelf)), 
                        null), 
                new ExpressionCall("ID3", new Condition("Anat_id4", "Stage_id2", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpDescAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpDescAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpDescAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, dpDescAndSelf)), 
                        null), 
                new ExpressionCall("ID3", new Condition("Anat_id5", "Stage_id2", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, dpSelfAndSelf)), 
                        null), 

                new ExpressionCall("ID5", new Condition("Anat_id1", "Stage_id5", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpDescAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpDescAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpDescAndSelf)), 
                        null), 
                new ExpressionCall("ID5", new Condition("Anat_id1", "ParentStage_id5", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpDescAndDesc),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpDescAndDesc),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpDescAndDesc)), 
                        null), 
                new ExpressionCall("ID5", new Condition("Anat_id4", "Stage_id5", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpSelfAndSelf)), 
                        null), 
                new ExpressionCall("ID5", new Condition("Anat_id4", "ParentStage_id5", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndDesc),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndDesc),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpSelfAndDesc)), 
                        null)));
    }
    
    /**
     * Test the method {@link CallService#reconcileSingleGeneCalls(Set)}.
     */
    @Test
    public void shouldReconcileCalls() {
        String speciesId = "speciesId1";

        // EXPRESSED - HIGH quality - observed
        Set<ExpressionCall> inputCalls = new HashSet<>(Arrays.asList(
                new ExpressionCall("geneA", new Condition("organA", "stageA", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)), 
                        null), 
                new ExpressionCall("geneA", new Condition("organA", "stageA", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndSelf)), 
                        null), 
                new ExpressionCall("geneA", new Condition("organB", "parentStageB1", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndDesc)), 
                        null)));
        ExpressionCall expectedResult = new ExpressionCall("geneA", null, 
                new DataPropagation(PropagationState.SELF, PropagationState.SELF_AND_DESCENDANT, true), 
                ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndDesc),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)), 
                null);
        ExpressionCall actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect ExpressionCall generated", expectedResult, actualResult);
        
        inputCalls = new HashSet<>(Arrays.asList(
                new ExpressionCall("geneA", new Condition("organA", "stageA", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpDescAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)), 
                        null), 
                new ExpressionCall("geneA", new Condition("organB", "parentStageB1", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelfDesc)), 
                        null)));
        expectedResult = new ExpressionCall("geneA", null, 
                new DataPropagation(PropagationState.SELF_AND_DESCENDANT, PropagationState.SELF_AND_DESCENDANT, true), 
                ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpDescAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelfDesc)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect ExpressionCall generated", expectedResult, actualResult);

        // EXPRESSED - LOW quality - not observed
        // FIXME this does not include observed data, no?
        inputCalls = new HashSet<>(Arrays.asList(
                new ExpressionCall("geneA", new Condition("organA", "stageA", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpDescAndSelf)), 
                        null), 
                new ExpressionCall("geneA", new Condition("organB", "parentStageB1", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndDesc)), 
                        null)));
        expectedResult = new ExpressionCall("geneA", null, 
                new DataPropagation(PropagationState.SELF_AND_DESCENDANT, PropagationState.SELF_AND_DESCENDANT, true), 
                ExpressionSummary.EXPRESSED, DataQuality.LOW, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpDescAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndDesc)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect ExpressionCall generated", expectedResult, actualResult);

        // NOT_EXPRESSED - HIGH quality - not observed
        inputCalls = new HashSet<>(Arrays.asList(
                new ExpressionCall("geneA", new Condition("organA", "stageA", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpAncAndSelf)), 
                        null), 
                new ExpressionCall("geneA", new Condition("organB", "parentStageB1", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpAncAndSelf)), 
                        null)));
        expectedResult = new ExpressionCall("geneA", null, 
                new DataPropagation(PropagationState.ANCESTOR, PropagationState.SELF, false), 
                ExpressionSummary.NOT_EXPRESSED, DataQuality.HIGH, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpAncAndSelf),
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpAncAndSelf)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect ExpressionCall generated", expectedResult, actualResult);

        // WEAK_AMBIGUITY - null - observed
        // FIXME this does not include observed data, no?
        inputCalls = new HashSet<>(Arrays.asList(
                new ExpressionCall("geneA", new Condition("organA", "stageA", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpAncAndSelf)), 
                        null), 
                new ExpressionCall("geneA", new Condition("organB", "parentStageB1", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndDesc)), 
                        null)));
        expectedResult = new ExpressionCall("geneA", null, 
                new DataPropagation(PropagationState.SELF_AND_ANCESTOR, PropagationState.SELF_AND_DESCENDANT, true), 
                ExpressionSummary.WEAK_AMBIGUITY, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpAncAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndDesc)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect ExpressionCall generated", expectedResult, actualResult);

        // WEAK_AMBIGUITY - null - observed
        inputCalls = new HashSet<>(Arrays.asList(
                new ExpressionCall("geneA", new Condition("organA", "stageA", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpAncAndSelf)), 
                        null), 
                new ExpressionCall("geneA", new Condition("organA", "parentStageB1", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpAncAndSelf)), 
                        null), 
                new ExpressionCall("geneA", new Condition("organB", "parentStageB1", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfDescAndAll)), 
                        null)));
        expectedResult = new ExpressionCall("geneA", null, 
                new DataPropagation(PropagationState.ALL, PropagationState.ALL, true), 
                ExpressionSummary.WEAK_AMBIGUITY, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpAncAndSelf),
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpAncAndSelf), 
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfDescAndAll)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect ExpressionCall generated", expectedResult, actualResult);

        // STRONG_AMBIGUITY - null - observed
        inputCalls = new HashSet<>(Arrays.asList(
                new ExpressionCall("geneA", new Condition("organA", "stageA", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndSelf)), 
                        null), 
                new ExpressionCall("geneA", new Condition("organB", "parentStageB1", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndDesc)), 
                        null)));
        expectedResult = new ExpressionCall("geneA", null, 
                new DataPropagation(PropagationState.SELF, PropagationState.SELF_AND_DESCENDANT, true), 
                ExpressionSummary.STRONG_AMBIGUITY, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndDesc)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect ExpressionCall generated", expectedResult, actualResult);

        // WEAK_AMBIGUITY - null - not observed
        inputCalls = new HashSet<>(Arrays.asList(
                new ExpressionCall("geneA", new Condition("organA", "stageA", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpAncAndSelf)), 
                        null), 
                new ExpressionCall("geneA", new Condition("organB", "parentStageB1", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpDescAndDesc)), 
                        null)));
        expectedResult = new ExpressionCall("geneA", null, 
                new DataPropagation(PropagationState.ANCESTOR_AND_DESCENDANT, PropagationState.SELF_AND_DESCENDANT, false), 
                ExpressionSummary.WEAK_AMBIGUITY, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpAncAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpDescAndDesc)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect ExpressionCall generated", expectedResult, actualResult);

        // Two different gene IDs
        inputCalls = new HashSet<>(Arrays.asList(
                new ExpressionCall("geneA", new Condition("organA", "stageA", speciesId),
                        null, null, null, null, null), 
                new ExpressionCall("geneB", new Condition("organA", "stageA", speciesId),
                        null, null, null, null, null)));
        try {
            CallService.reconcileSingleGeneCalls(inputCalls);
            fail("Should throw an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Test passed
        }

        // Reconciliation of DataPropagation.ANCESTOR with DataPropagation.DESCENDANT
        inputCalls = new HashSet<>(Arrays.asList(
                new ExpressionCall("geneA", new Condition("organA", "stageA", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, 
                                new DataPropagation(PropagationState.SELF_OR_ANCESTOR, PropagationState.SELF, false))), 
                        null), 
                new ExpressionCall("geneA", new Condition("organB", "parentStageB1", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpDescAndDesc)), 
                        null)));
        try {
            CallService.reconcileSingleGeneCalls(inputCalls);
            fail("Should throw an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Test passed
        }
    }
    
    /**
     * Test the method {@link CallService#reconcileSingleGeneCalls(Set)}.
     */
    @Test
    public void shouldReconcileCalls_pipelineTest() {
        String geneId = "ID1";
        String speciesId = "speciesId1";
        Condition cond = new Condition("Anat_id1", "Stage_id1", speciesId);
        Set<ExpressionCall> inputCalls = filterExprTOs(geneId, cond, speciesId);
        inputCalls.addAll(filterNoExprTOs(geneId, cond, speciesId));
        ExpressionCall expectedResult = new ExpressionCall(geneId, null, 
                dpSelfAndSelf, ExpressionSummary.STRONG_AMBIGUITY, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, dpSelfAndSelf)), 
                null);
        ExpressionCall actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);
        
        cond = new Condition("Anat_id1", "ParentStage_id1", speciesId);
        inputCalls = filterExprTOs(geneId, cond, speciesId);
        inputCalls.addAll(filterNoExprTOs(geneId, cond, speciesId));
        expectedResult = new ExpressionCall(geneId, null, 
                dpSelfAndDesc, ExpressionSummary.EXPRESSED, DataQuality.LOW, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndDesc),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpSelfAndDesc),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, dpSelfAndDesc)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);

        cond = new Condition("Anat_id1", "ParentStage_id2", speciesId);
        inputCalls = filterExprTOs(geneId, cond, speciesId);
        inputCalls.addAll(filterNoExprTOs(geneId, cond, speciesId));
        expectedResult = new ExpressionCall(geneId, null, 
                dpSelfAndSelf, ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.EST, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpSelfAndSelf)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);

        cond = new Condition("Anat_id1", "Stage_id2", speciesId);
        inputCalls = filterExprTOs(geneId, cond, speciesId);
        inputCalls.addAll(filterNoExprTOs(geneId, cond, speciesId));
        expectedResult = new ExpressionCall(geneId, null, 
                dpSelfAndSelf, ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpSelfAndSelf)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);
        
        geneId = "ID2";
        cond = new Condition("Anat_id1", "Stage_id2", speciesId);
        inputCalls = filterExprTOs(geneId, cond, speciesId);
        inputCalls.addAll(filterNoExprTOs(geneId, cond, speciesId));
        expectedResult = new ExpressionCall(geneId, null, 
                dpSelfAndSelf, ExpressionSummary.STRONG_AMBIGUITY, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndSelf), 
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);

        cond = new Condition("Anat_id1", "ParentStage_id2", speciesId);
        inputCalls = filterExprTOs(geneId, cond, speciesId);
        inputCalls.addAll(filterNoExprTOs(geneId, cond, speciesId));
        expectedResult = new ExpressionCall(geneId, null, 
                dpSelfDescAndDesc, ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfDescAndDesc),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfDescAndDesc),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpSelfDescAndDesc)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);

        cond = new Condition("Anat_id2", "Stage_id2", speciesId);
        inputCalls = filterExprTOs(geneId, cond, speciesId);
        inputCalls.addAll(filterNoExprTOs(geneId, cond, speciesId));
        expectedResult = new ExpressionCall(geneId, null, 
                dpAncAndSelf, ExpressionSummary.NOT_EXPRESSED, DataQuality.HIGH, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpAncAndSelf)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);

        cond = new Condition("Anat_id2", "ParentStage_id2", speciesId);
        inputCalls = filterExprTOs(geneId, cond, speciesId);
        inputCalls.addAll(filterNoExprTOs(geneId, cond, speciesId));
        expectedResult = new ExpressionCall(geneId, null, 
                dpDescAndSelf, ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpDescAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpDescAndSelf)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);

        cond = new Condition("Anat_id3", "ParentStage_id2", speciesId);
        inputCalls = filterExprTOs(geneId, cond, speciesId);
        inputCalls.addAll(filterNoExprTOs(geneId, cond, speciesId));
        expectedResult = new ExpressionCall(geneId, null, 
                dpSelfAndSelf, ExpressionSummary.STRONG_AMBIGUITY, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf), 
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpSelfAndSelf)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);

        geneId = "ID3";
        cond = new Condition("Anat_id1", "Stage_id2", speciesId);
        inputCalls = filterExprTOs(geneId, cond, speciesId);
        inputCalls.addAll(filterNoExprTOs(geneId, cond, speciesId));
        expectedResult = new ExpressionCall(geneId, null, 
                dpSelfDescAndSelf, ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfDescAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfDescAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpSelfDescAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfDescAndSelf)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);

        cond = new Condition("Anat_id4", "Stage_id2", speciesId);
        inputCalls = filterExprTOs(geneId, cond, speciesId);
        inputCalls.addAll(filterNoExprTOs(geneId, cond, speciesId));
        expectedResult = new ExpressionCall(geneId, null, 
                dpDescAndSelf, ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpDescAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpDescAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpDescAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, dpDescAndSelf)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);
        
        cond = new Condition("Anat_id5", "Stage_id2", speciesId);
        inputCalls = filterExprTOs(geneId, cond, speciesId);
        inputCalls.addAll(filterNoExprTOs(geneId, cond, speciesId));
        expectedResult = new ExpressionCall(geneId, null, 
                dpSelfAndSelf, ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, dpSelfAndSelf)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);

        geneId = "ID4";
        cond = new Condition("Anat_id1", "Stage_id5", speciesId);
        inputCalls = filterExprTOs(geneId, cond, speciesId);
        inputCalls.addAll(filterNoExprTOs(geneId, cond, speciesId));
        expectedResult = new ExpressionCall(geneId, null, 
                dpSelfAndSelf, ExpressionSummary.NOT_EXPRESSED, DataQuality.HIGH, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpSelfAndSelf),
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);

        cond = new Condition("Anat_id4", "Stage_id5", speciesId);
        inputCalls = filterExprTOs(geneId, cond, speciesId);
        inputCalls.addAll(filterNoExprTOs(geneId, cond, speciesId));
        expectedResult = new ExpressionCall(geneId, null, 
                dpSelfAncAndSelf, ExpressionSummary.NOT_EXPRESSED, DataQuality.HIGH, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAncAndSelf), 
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpSelfAncAndSelf), 
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAncAndSelf)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);
        
        cond = new Condition("Anat_id5", "Stage_id5", speciesId);
        inputCalls = filterExprTOs(geneId, cond, speciesId);
        inputCalls.addAll(filterNoExprTOs(geneId, cond, speciesId));
        expectedResult = new ExpressionCall(geneId, null, 
                dpAncAndSelf, ExpressionSummary.NOT_EXPRESSED, DataQuality.HIGH, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpAncAndSelf), 
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpAncAndSelf), 
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpAncAndSelf)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);

        geneId = "ID5";
        cond = new Condition("Anat_id1", "Stage_id5", speciesId);
        inputCalls = filterExprTOs(geneId, cond, speciesId);
        inputCalls.addAll(filterNoExprTOs(geneId, cond, speciesId));
        expectedResult = new ExpressionCall(geneId, null, 
                dpSelfDescAndSelf, ExpressionSummary.STRONG_AMBIGUITY, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpDescAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpDescAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpDescAndSelf), 
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);

        cond = new Condition("Anat_id1", "ParentStage_id5", speciesId);
        inputCalls = filterExprTOs(geneId, cond, speciesId);
        inputCalls.addAll(filterNoExprTOs(geneId, cond, speciesId));
        expectedResult = new ExpressionCall(geneId, null, 
                dpDescAndDesc, ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpDescAndDesc),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpDescAndDesc),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpDescAndDesc)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);
        
        cond = new Condition("Anat_id4", "Stage_id5", speciesId);
        inputCalls = filterExprTOs(geneId, cond, speciesId);
        inputCalls.addAll(filterNoExprTOs(geneId, cond, speciesId));
        expectedResult = new ExpressionCall(geneId, null, 
                dpSelfAndSelf, ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpSelfAndSelf)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);

        cond = new Condition("Anat_id4", "ParentStage_id5", speciesId);
        inputCalls = filterExprTOs(geneId, cond, speciesId);
        inputCalls.addAll(filterNoExprTOs(geneId, cond, speciesId));
        expectedResult = new ExpressionCall(geneId, null, 
                dpSelfAndDesc, ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndDesc),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndDesc),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpSelfAndDesc)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);

        geneId = "ID6";
        cond = new Condition("Anat_id9", "Stage_id7", speciesId);
        inputCalls = filterExprTOs(geneId, cond, speciesId);
        inputCalls.addAll(filterNoExprTOs(geneId, cond, speciesId));
        expectedResult = new ExpressionCall(geneId, null, 
                dpSelfAndSelf, ExpressionSummary.NOT_EXPRESSED, DataQuality.HIGH, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);

        cond = new Condition("Anat_id8", "Stage_id6", speciesId);
        inputCalls = filterExprTOs(geneId, cond, speciesId);
        inputCalls.addAll(filterNoExprTOs(geneId, cond, speciesId));
        expectedResult = new ExpressionCall(geneId, null, 
                dpAncAndSelf, ExpressionSummary.NOT_EXPRESSED, DataQuality.HIGH, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpAncAndSelf)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);
    }

    private Set<ExpressionCall> filterExprTOs(String geneId, Condition cond, String speciesId) {
        Set<ExpressionCall> inputCalls = getPropagationFromExpressionTOs(speciesId).stream()
                .filter(c -> c.getGeneId().equals(geneId))
                .filter(c -> c.getCondition().equals(cond))
                .collect(Collectors.toSet());
        return inputCalls;
    }
    
    private Set<ExpressionCall> filterNoExprTOs(String geneId, Condition cond, String speciesId) {
        Set<ExpressionCall> inputCalls = getPropagationFromNoExpressionTOs(speciesId).stream()
                .filter(c -> c.getGeneId().equals(geneId))
                .filter(c -> c.getCondition().equals(cond))
                .collect(Collectors.toSet());
        return inputCalls;
    }
}
