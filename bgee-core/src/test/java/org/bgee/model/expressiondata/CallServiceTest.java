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
 * @version Bgee 13, Apr. 2016
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
    
    /**
     * Test the method {@link CallService#propagateExpressionTOs(java.util.Set, java.util.Set, java.util.Set)}.
     */
    @Test
    public void shoudPropagateExpressionTOs() {
        DAOManager manager = mock(DAOManager.class);

        CallService service = new CallService(manager);
        try {
            service.propagateExpressionTOs(null, null, null, null);
            fail("Should throw an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // test passed
        }

        ConditionUtils mockConditionUtils = mock(ConditionUtils.class);
        
        Set<Condition> conditions = new HashSet<>(Arrays.asList(
                new Condition("organA", "stageA"),
                new Condition("organA", "parentStageA1"),
                new Condition("parentOrganA1", "stageA"),
                new Condition("parentOrganA1", "parentStageA1")));
        when(mockConditionUtils.getConditions()).thenReturn(conditions);
        when(mockConditionUtils.isInferredAncestralConditions()).thenReturn(true);
        
        Condition childCond = new Condition("organA", "stageA");
        Set<Condition> ancestorConds = new HashSet<>(Arrays.asList(
                new Condition("organA", "parentStageA1"),
                new Condition("parentOrganA1", "stageA"),
                new Condition("parentOrganA1", "parentStageA1")));
        when(mockConditionUtils.getAncestorConditions(childCond, true)).thenReturn(ancestorConds);
        
        childCond = new Condition("organA", "parentStageA1");
        ancestorConds = new HashSet<>(Arrays.asList(
                new Condition("parentOrganA1", "parentStageA1")));
        when(mockConditionUtils.getAncestorConditions(childCond, true)).thenReturn(ancestorConds);

        childCond = new Condition("parentOrganA1", "parentStageA1");
        ancestorConds = new HashSet<>();
        when(mockConditionUtils.getAncestorConditions(childCond, true)).thenReturn(ancestorConds);

        childCond = new Condition("organB", "stageB");
        ancestorConds = new HashSet<>(Arrays.asList(new Condition("organB", "parentStageB1")));
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

        DataPropagation dpSelfSelf = new DataPropagation(PropagationState.SELF, PropagationState.SELF);
        DataPropagation dpSelfDesc = new DataPropagation(PropagationState.SELF, PropagationState.DESCENDANT);
        DataPropagation dpDescSelf = new DataPropagation(PropagationState.DESCENDANT, PropagationState.SELF);
        DataPropagation dpDescDesc = new DataPropagation(PropagationState.DESCENDANT, PropagationState.DESCENDANT);
        
        Set<ExpressionCall> allResults = new HashSet<>(Arrays.asList(
                // From ExpressionCallTO 1
                new ExpressionCall("geneA", new Condition("organA", "stageA"), null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfSelf))), 
                
                new ExpressionCall("geneA", new Condition("organA", "parentStageA1"), null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfDesc),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfDesc))), 

                new ExpressionCall("geneA", new Condition("parentOrganA1", "stageA"), null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpDescSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpDescSelf))), 

                new ExpressionCall("geneA", new Condition("parentOrganA1", "parentStageA1"), null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpDescDesc),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpDescDesc))), 

                // From ExpressionCallTO 2
                new ExpressionCall("geneA", new Condition("organA", "parentStageA1"), null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.EST, dpSelfSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpSelfSelf))), 
                new ExpressionCall("geneA", new Condition("parentOrganA1", "parentStageA1"), null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.EST, dpDescSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpDescSelf))), 

                // From ExpressionCallTO 3
                new ExpressionCall("geneB", new Condition("parentOrganA1", "parentStageA1"), null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, dpSelfSelf))), 

                // From ExpressionCallTO 4
                new ExpressionCall("geneB", new Condition("organB", "stageB"), null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfSelf))), 
                new ExpressionCall("geneB", new Condition("organB", "parentStageB1"), null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfDesc)))));

        Set<ExpressionCall> actualResults = service.propagateExpressionTOs(
                exprTOs, null, null, mockConditionUtils);
        assertEquals("Incorrect ExpressionCalls generated", allResults, actualResults);
        
        Set<String> allowedOrganIds = new HashSet<>(Arrays.asList("organA"));
        Set<ExpressionCall> expectedResults = allResults.stream()
                .filter(c -> allowedOrganIds.contains(c.getCondition().getAnatEntityId()))
                .collect(Collectors.toSet());
        actualResults = service.propagateExpressionTOs(
                exprTOs, allowedOrganIds, null, mockConditionUtils);
        assertEquals("Incorrect ExpressionCalls generated", expectedResults, actualResults);

        Set<String> allowedStageIds = new HashSet<>(Arrays.asList("parentStageA1"));
        expectedResults = allResults.stream()
                .filter(c -> allowedStageIds.contains(c.getCondition().getDevStageId()))
                .collect(Collectors.toSet());
        actualResults = service.propagateExpressionTOs(
                exprTOs, null, allowedStageIds, mockConditionUtils);
        assertEquals("Incorrect ExpressionCalls generated", expectedResults, actualResults);

        expectedResults = allResults.stream()
                .filter(c -> allowedOrganIds.contains(c.getCondition().getAnatEntityId()))
                .filter(c -> allowedStageIds.contains(c.getCondition().getDevStageId()))
                .collect(Collectors.toSet());
        actualResults = service.propagateExpressionTOs(
                exprTOs, allowedOrganIds, allowedStageIds, mockConditionUtils);
        assertEquals("Incorrect ExpressionCalls generated", expectedResults, actualResults);
    }
    
    /**
     * Test the method {@link CallService#reconcileCalls(Set)}.
     */
    @Test
    public void shoudReconcileCalls() {
        DAOManager manager = mock(DAOManager.class);
        CallService service = new CallService(manager);
        
        DataPropagation dpSelfAndSelf = new DataPropagation(PropagationState.SELF, PropagationState.SELF, true);
        DataPropagation dpSelfAndDesc = new DataPropagation(PropagationState.SELF, PropagationState.DESCENDANT, false);
        DataPropagation dpSelfAndSelfDesc = new DataPropagation(PropagationState.SELF, PropagationState.SELF_AND_DESCENDANT, true);
        DataPropagation dpSelfDescAndAll= new DataPropagation(PropagationState.SELF_AND_DESCENDANT, PropagationState.ALL, true);
        DataPropagation dpDescAndSelf = new DataPropagation(PropagationState.DESCENDANT, PropagationState.SELF, false);
        DataPropagation dpDescAndDesc = new DataPropagation(PropagationState.DESCENDANT, PropagationState.DESCENDANT, false);
        DataPropagation dpAncAndSelf = new DataPropagation(PropagationState.ANCESTOR, PropagationState.SELF, false);

        // EXPRESSED - HIGH quality - observed
        Set<ExpressionCall> inputCalls = new HashSet<>(Arrays.asList(
                new ExpressionCall("geneA", new Condition("organA", "stageA"), null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf))), 

                new ExpressionCall("geneA", new Condition("organB", "parentStageB1"), null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndDesc)))));
        ExpressionCall expectedResult = new ExpressionCall("geneA", null, 
                new DataPropagation(PropagationState.SELF, PropagationState.SELF_AND_DESCENDANT, true), 
                ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
                new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndSelf),
                new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndDesc),
                new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)));
        ExpressionCall actualResult = service.reconcileCalls(inputCalls);
        assertEquals("Incorrect ExpressionCall generated", expectedResult, actualResult);
        
        //
        inputCalls = new HashSet<>(Arrays.asList(
                new ExpressionCall("geneA", new Condition("organA", "stageA"), null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpDescAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf))), 

                new ExpressionCall("geneA", new Condition("organB", "parentStageB1"), null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelfDesc)))));
        expectedResult = new ExpressionCall("geneA", null, 
                new DataPropagation(PropagationState.SELF_AND_DESCENDANT, PropagationState.SELF_AND_DESCENDANT, true), 
                ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
                new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpDescAndSelf),
                new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf),
                new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelfDesc)));
        actualResult = service.reconcileCalls(inputCalls);
        assertEquals("Incorrect ExpressionCall generated", expectedResult, actualResult);

        // EXPRESSED - LOW quality - not observed
        inputCalls = new HashSet<>(Arrays.asList(
                new ExpressionCall("geneA", new Condition("organA", "stageA"), null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpDescAndSelf))), 
                new ExpressionCall("geneA", new Condition("organB", "parentStageB1"), null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndDesc)))));
        expectedResult = new ExpressionCall("geneA", null, 
                new DataPropagation(PropagationState.SELF_AND_DESCENDANT, PropagationState.SELF_AND_DESCENDANT, true), 
                ExpressionSummary.EXPRESSED, DataQuality.LOW, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpDescAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndDesc)));
        actualResult = service.reconcileCalls(inputCalls);
        assertEquals("Incorrect ExpressionCall generated", expectedResult, actualResult);

        // NOT_EXPRESSED - HIGH quality - not observed
        inputCalls = new HashSet<>(Arrays.asList(
                new ExpressionCall("geneA", new Condition("organA", "stageA"), null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpAncAndSelf))), 
                new ExpressionCall("geneA", new Condition("organB", "parentStageB1"), null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpAncAndSelf)))));
        expectedResult = new ExpressionCall("geneA", null, 
                new DataPropagation(PropagationState.ANCESTOR, PropagationState.SELF, false), 
                ExpressionSummary.NOT_EXPRESSED, DataQuality.HIGH, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpAncAndSelf),
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpAncAndSelf)));
        actualResult = service.reconcileCalls(inputCalls);
        assertEquals("Incorrect ExpressionCall generated", expectedResult, actualResult);

        // WEAK_AMBIGUITY - null - observed
        inputCalls = new HashSet<>(Arrays.asList(
                new ExpressionCall("geneA", new Condition("organA", "stageA"), null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpAncAndSelf))), 
                new ExpressionCall("geneA", new Condition("organB", "parentStageB1"), null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndDesc)))));
        expectedResult = new ExpressionCall("geneA", null, 
                new DataPropagation(PropagationState.SELF_AND_ANCESTOR, PropagationState.SELF_AND_DESCENDANT, true), 
                ExpressionSummary.WEAK_AMBIGUITY, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpAncAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndDesc)));
        actualResult = service.reconcileCalls(inputCalls);
        assertEquals("Incorrect ExpressionCall generated", expectedResult, actualResult);
        
        //
        inputCalls = new HashSet<>(Arrays.asList(
                new ExpressionCall("geneA", new Condition("organA", "stageA"), null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpAncAndSelf))), 
                new ExpressionCall("geneA", new Condition("organB", "parentStageB1"), null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfDescAndAll)))));
        expectedResult = new ExpressionCall("geneA", null, 
                new DataPropagation(PropagationState.ALL, PropagationState.ALL, true), 
                ExpressionSummary.WEAK_AMBIGUITY, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpAncAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfDescAndAll)));
        actualResult = service.reconcileCalls(inputCalls);
        assertEquals("Incorrect ExpressionCall generated", expectedResult, actualResult);
        
        // STRONG_AMBIGUITY - null - not observed
        inputCalls = new HashSet<>(Arrays.asList(
                new ExpressionCall("geneA", new Condition("organA", "stageA"), null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndSelf))), 
                new ExpressionCall("geneA", new Condition("organB", "parentStageB1"), null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndDesc)))));
        expectedResult = new ExpressionCall("geneA", null, 
                new DataPropagation(PropagationState.SELF, PropagationState.SELF_AND_DESCENDANT, true), 
                ExpressionSummary.STRONG_AMBIGUITY, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndDesc)));
        actualResult = service.reconcileCalls(inputCalls);
        assertEquals("Incorrect ExpressionCall generated", expectedResult, actualResult);
        
        // Two different gene IDs
        inputCalls = new HashSet<>(Arrays.asList(
                new ExpressionCall("geneA", new Condition("organA", "stageA"), null, null, null, null), 
                new ExpressionCall("geneB", new Condition("organB", "parentStageB1"), null, null, null, null)));
        try {
            service.reconcileCalls(inputCalls);
            fail("Should throw an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Test passed
        }
        
        // Reconciliation of DataPropagation.ANCESTOR with DataPropagation.DESCENDANT
        inputCalls = new HashSet<>(Arrays.asList(
                new ExpressionCall("geneA", new Condition("organA", "stageA"), null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpAncAndSelf))), 
                new ExpressionCall("geneA", new Condition("organB", "parentStageB1"), null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpDescAndDesc)))));
        try {
            service.reconcileCalls(inputCalls);
            fail("Should throw an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Test passed
        }
    }
}
