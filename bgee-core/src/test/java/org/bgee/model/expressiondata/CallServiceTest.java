package org.bgee.model.expressiondata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO.OriginOfLine;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTOResultSet;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.CallService.CallSpliterator;
import org.bgee.model.expressiondata.baseelements.CallType.Expression;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.DataPropagation.PropagationState;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.ontology.OntologyService;
import org.bgee.model.ontology.RelationType;
import org.junit.Test;

/**
 * Unit tests for {@link CallService}.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Oct. 2016
 * @since   Bgee 13, Nov. 2015
 */
public class CallServiceTest extends TestAncestor {
    
    private final static Logger log = LogManager.getLogger(CallServiceTest.class.getName());
    
    private final static DataPropagation dpSelfAndSelf = 
            new DataPropagation(PropagationState.SELF, PropagationState.SELF, true);
    private final static DataPropagation dpSelfAndDesc = 
            new DataPropagation(PropagationState.SELF, PropagationState.DESCENDANT, false);
    private final static DataPropagation dpSelfAndSelfDesc = 
            new DataPropagation(PropagationState.SELF, PropagationState.SELF_AND_DESCENDANT, true);
    private final static DataPropagation dpSelfDescAndAll= 
            new DataPropagation(PropagationState.SELF_AND_DESCENDANT, PropagationState.ALL, true);
    private final static DataPropagation dpDescAndSelf = 
            new DataPropagation(PropagationState.DESCENDANT, PropagationState.SELF, false);
    private final static DataPropagation dpDescAndDesc = 
            new DataPropagation(PropagationState.DESCENDANT, PropagationState.DESCENDANT, false);
    private final static DataPropagation dpAncAndSelf = 
            new DataPropagation(PropagationState.ANCESTOR, PropagationState.SELF, false);
    private final static DataPropagation dpSelfAncAndSelf = 
            new DataPropagation(PropagationState.SELF_AND_ANCESTOR, PropagationState.SELF, true);
    private final static DataPropagation dpSelfDescAndDesc = 
            new DataPropagation(PropagationState.SELF_AND_DESCENDANT, PropagationState.DESCENDANT, false);
    private final static DataPropagation dpSelfDescAndSelf = 
            new DataPropagation(PropagationState.SELF_AND_DESCENDANT, PropagationState.SELF, true);
    
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
                        CallTO.DataState.NODATA, null, false, false, OriginOfLine.SELF, 
                        OriginOfLine.SELF, true), 
                    new ExpressionCallTO(null, "geneId1", "anatEntityId1", "stageId2", 
                        new BigDecimal(125.00), CallTO.DataState.NODATA, null,
                        CallTO.DataState.LOWQUALITY, null, CallTO.DataState.LOWQUALITY, null,
                        CallTO.DataState.NODATA, null, false, false, OriginOfLine.SELF, 
                        OriginOfLine.SELF, true)));
        
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
                    new DataPropagation(PropagationState.SELF, PropagationState.SELF, true), 
                    ExpressionSummary.EXPRESSED, DataQuality.HIGH, 
                    Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, 
                            new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)), 
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.EST, 
                            new DataPropagation(PropagationState.SELF, PropagationState.SELF, true))), 
                    new BigDecimal(1257.34)),
                new ExpressionCall("geneId1", new Condition("anatEntityId1", "stageId2", "speciesId1"), 
                    new DataPropagation(PropagationState.SELF, PropagationState.SELF_AND_DESCENDANT, true),
                    ExpressionSummary.EXPRESSED, DataQuality.HIGH, 
                    Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, 
                            new DataPropagation(PropagationState.SELF, PropagationState.DESCENDANT, false)), 
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.EST, 
                            new DataPropagation(PropagationState.SELF, PropagationState.DESCENDANT, false)),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST,
                            new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)), 
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU,
                            new DataPropagation(PropagationState.SELF, PropagationState.SELF, true))), 
                    new BigDecimal(125.00)),
                new ExpressionCall("geneId1", new Condition("anatEntityId1", "stageId3", "speciesId1"), 
                    new DataPropagation(PropagationState.SELF, PropagationState.DESCENDANT, false),
                    ExpressionSummary.EXPRESSED, DataQuality.HIGH, 
                    Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, 
                            new DataPropagation(PropagationState.SELF, PropagationState.DESCENDANT, false)), 
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.EST, 
                            new DataPropagation(PropagationState.SELF, PropagationState.DESCENDANT, false)),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST,
                            new DataPropagation(PropagationState.SELF, PropagationState.DESCENDANT, false)), 
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU,
                            new DataPropagation(PropagationState.SELF, PropagationState.DESCENDANT, false))), 
                    null));
        
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = 
                new LinkedHashMap<>();
        serviceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.DESC);
        
        CallService service = new CallService(serviceFactory);
        List<ExpressionCall> actualResults = service.loadExpressionCalls("speciesId1", 
                new ExpressionCallFilter(new GeneFilter("geneId1"), null, 
                        Arrays.asList(new ExpressionCallData(Expression.EXPRESSED))), 
                null, // all attributes 
                serviceOrdering,
                true)
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
        ExpressionCallDAO dao = mock(ExpressionCallDAO.class);
        when(manager.getExpressionCallDAO()).thenReturn(dao);
        
        ExpressionCallTOResultSet resultSetMock = getMockResultSet(ExpressionCallTOResultSet.class, 
                Arrays.asList(
                    // To not overload tests, we put null for not used attributes 
                    // but, real query return all attributes
                    new ExpressionCallTO(null, "geneId1", "anatEntityId1", "stageId1",  
                        null, CallTO.DataState.HIGHQUALITY, null, null, null, null, null, null, null, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true), 
                    new ExpressionCallTO(null, "geneId1", "anatEntityId2", "stageId1",  
                            null, CallTO.DataState.HIGHQUALITY, null, null, null, null, null, null, null, 
                            false, false, OriginOfLine.SELF, OriginOfLine.SELF, true), 
                    new ExpressionCallTO(null, "geneId2", "anatEntityId1", "stageId2",  
                        null, CallTO.DataState.HIGHQUALITY, null, null, null, null, null, null, null, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true)));

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
                new ExpressionCallFilter(null, null, 
                        Arrays.asList(new ExpressionCallData(Expression.EXPRESSED))), 
                EnumSet.of(CallService.Attribute.GENE_ID, CallService.Attribute.ANAT_ENTITY_ID), 
                null, true)
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
                    new DataPropagation(PropagationState.SELF, PropagationState.SELF, null), 
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
                new ExpressionCall("geneId1", null, 
                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, null), 
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
                EnumSet.of(CallService.Attribute.GENE_ID, CallService.Attribute.GLOBAL_ANAT_PROPAGATION, 
                        CallService.Attribute.GLOBAL_STAGE_PROPAGATION, CallService.Attribute.CALL_DATA, 
                        CallService.Attribute.GLOBAL_DATA_QUALITY, CallService.Attribute.GLOBAL_RANK),
                serviceOrdering, true)
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
    
    /**
     * Test the method {@link CallService#testCallFilter(ExpressionCall, ExpressionCallFilter)}.
     */
    @Test
    public void shouldTestCallFilter() {
        Collection<ExpressionCallData> callData = new HashSet<>();
        callData.add(new ExpressionCallData(
                Expression.EXPRESSED, DataQuality.HIGH, DataType.EST, new DataPropagation()));
        callData.add(new ExpressionCallData(
                Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, new DataPropagation()));

        ExpressionCall call1 = new ExpressionCall("g1", new Condition("ae1", "ds1", "sp1"),
                new DataPropagation(PropagationState.SELF, PropagationState.SELF, true),
                ExpressionSummary.EXPRESSED, DataQuality.HIGH, callData, new BigDecimal(125.00));
        
        // Test with no ExpressionCallFilter 
        try {
            CallService.testCallFilter(call1, null);
            fail("An IllegalArgumentException should be thrown when call filter is null");
        } catch (IllegalArgumentException e){
            // Test passed
        }
        
        ExpressionCallData validCallDataFilter1 = new ExpressionCallData(
                Expression.EXPRESSED, DataQuality.LOW, DataType.EST, new DataPropagation());
        ExpressionCallData validCallDataFilter2 = new ExpressionCallData(
                Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, 
                new DataPropagation(PropagationState.DESCENDANT, PropagationState.SELF));
        ExpressionCallData validCallDataFilter3 = new ExpressionCallData(
                Expression.EXPRESSED, DataQuality.HIGH, null, new DataPropagation());

        ExpressionCallData notValidCallDataFilter1 = new ExpressionCallData(
                Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, new DataPropagation());
        ExpressionCallData notValidCallDataFilter2 = new ExpressionCallData(
                Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, new DataPropagation());

        // Test with no GeneFilter and ConditionFilters 
        Set<ExpressionCallData> callDataFilters = new HashSet<>(Arrays.asList(validCallDataFilter1));
        ExpressionCallFilter callFilter = new ExpressionCallFilter(null, null, callDataFilters);
        assertTrue("Call should pass the filter", CallService.testCallFilter(call1, callFilter));

        callDataFilters = new HashSet<>(Arrays.asList(validCallDataFilter2));
        callFilter = new ExpressionCallFilter(null, null, callDataFilters);
        assertTrue("Call should pass the filter", CallService.testCallFilter(call1, callFilter));

        callDataFilters = new HashSet<>(Arrays.asList(validCallDataFilter3));
        callFilter = new ExpressionCallFilter(null, null, callDataFilters);
        assertTrue("Call should pass the filter", CallService.testCallFilter(call1, callFilter));

        callDataFilters = new HashSet<>(Arrays.asList(validCallDataFilter1, notValidCallDataFilter1));
        callFilter = new ExpressionCallFilter(null, null, callDataFilters);
        assertTrue("Call should pass the filter", CallService.testCallFilter(call1, callFilter));

        callDataFilters = new HashSet<>(Arrays.asList(notValidCallDataFilter1));
        callFilter = new ExpressionCallFilter(null, null, callDataFilters);
        assertFalse("Call should not pass the filter", CallService.testCallFilter(call1, callFilter));

        callDataFilters = new HashSet<>(Arrays.asList(notValidCallDataFilter2));
        callFilter = new ExpressionCallFilter(null, null, callDataFilters);
        assertFalse("Call should not pass the filter", CallService.testCallFilter(call1, callFilter));

        callData.clear();
        callData.add(new ExpressionCallData(
                Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, new DataPropagation()));
        ExpressionCall call2 = new ExpressionCall("g1", new Condition("ae1", "ds1", "sp1"),
                new DataPropagation(PropagationState.SELF, PropagationState.SELF, true),
                ExpressionSummary.EXPRESSED, DataQuality.HIGH, callData, new BigDecimal(125.00));
        ExpressionCallData notValidCallDataFilter3 = new ExpressionCallData(
                Expression.EXPRESSED, DataQuality.HIGH, null, new DataPropagation());
        callDataFilters = new HashSet<>(Arrays.asList(notValidCallDataFilter3));
        callFilter = new ExpressionCallFilter(null, null, callDataFilters);
        assertFalse("Call should not pass the filter", CallService.testCallFilter(call2, callFilter));

        // Test condition filter
        callFilter = new ExpressionCallFilter(null, null, callDataFilters);
        Set<ConditionFilter> validConditionFilters = new HashSet<>();
        validConditionFilters.add(new ConditionFilter(Arrays.asList("ae1", "ae2"), Arrays.asList("ds1", "ds2")));
        callFilter = new ExpressionCallFilter(new GeneFilter("g1"), validConditionFilters, 
                new HashSet<>(Arrays.asList(validCallDataFilter1)));
        assertTrue("Call should pass the filter", CallService.testCallFilter(call1, callFilter));

        Set<ConditionFilter> notValidConditionFilters = new HashSet<>();
        notValidConditionFilters.add(new ConditionFilter(Arrays.asList("ae1", "ae2"), Arrays.asList("ds2")));
        callFilter = new ExpressionCallFilter(new GeneFilter("g1"), notValidConditionFilters, 
                new HashSet<>(Arrays.asList(validCallDataFilter1)));
        assertFalse("Call should not pass the filter", CallService.testCallFilter(call1, callFilter));

        // Test gene filter
        callFilter = new ExpressionCallFilter(new GeneFilter("g2"), validConditionFilters, 
                new HashSet<>(Arrays.asList(validCallDataFilter1)));
        assertFalse("Call should not pass the filter", CallService.testCallFilter(call1, callFilter));
    }

    /**
     * Test the method 
     * {@link CallService#propagateExpressionCalls(Collection, Collection, ConditionUtils, String)}.
     */
    @Test
    public void shouldPropagateExpressedCalls() {
        DAOManager manager = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(manager);

        CallService service = new CallService(serviceFactory);
        try {
            service.propagateExpressionCalls(null, null, null, null);
            fail("Should throw an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // test passed
        }

        ConditionUtils mockConditionUtils = mock(ConditionUtils.class);
        String speciesId = "speciesId1";
        
        Set<Condition> conditions = new HashSet<>(Arrays.asList(
                new Condition("organA", "stageA", speciesId),
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
        when(mockConditionUtils.getAncestorConditions(childCond, false)).thenReturn(ancestorConds);
        
        childCond = new Condition("organA", "parentStageA1", speciesId);
        ancestorConds = new HashSet<>(Arrays.asList(
                new Condition("parentOrganA1", "parentStageA1", speciesId)));
        when(mockConditionUtils.getAncestorConditions(childCond, false)).thenReturn(ancestorConds);

        childCond = new Condition("parentOrganA1", "parentStageA1", speciesId);
        ancestorConds = new HashSet<>();
        when(mockConditionUtils.getAncestorConditions(childCond, false)).thenReturn(ancestorConds);

        childCond = new Condition("organB", "stageB", speciesId);
        ancestorConds = new HashSet<>(Arrays.asList(new Condition("organB", "parentStageB1", speciesId)));
        when(mockConditionUtils.getAncestorConditions(childCond, false)).thenReturn(ancestorConds);

        Collection<ExpressionCall> exprCalls = Arrays.asList(
                // ExpressionCall 1
                new ExpressionCall("geneA", new Condition("organA", "stageA", speciesId),
                        dpSelfAndSelf, ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
                                new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW,
                                        DataType.AFFYMETRIX, dpSelfAndSelf),
                                new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH,
                                        DataType.RNA_SEQ, dpSelfAndSelf)),
                        new BigDecimal("1.25")),
                // ExpressionCall 2
                new ExpressionCall("geneA", new Condition("organA", "parentStageA1", speciesId),
                        dpSelfAndSelf, ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
                                new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH,
                                        DataType.EST, dpSelfAndSelf),
                                new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH,
                                        DataType.IN_SITU, dpSelfAndSelf)),
                        new BigDecimal("1250")),
                // ExpressionCall 3
                new ExpressionCall("geneB", new Condition("parentOrganA1", "parentStageA1", speciesId),
                        dpSelfAndSelf, ExpressionSummary.EXPRESSED, DataQuality.LOW, Arrays.asList(
                                new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW,
                                        DataType.RNA_SEQ, dpSelfAndSelf)),
                        new BigDecimal("10")),
                // ExpressionCall 4
                new ExpressionCall("geneB", new Condition("organB", "stageB", speciesId),
                        dpSelfAndSelf, ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
                                new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH,
                                        DataType.AFFYMETRIX, dpSelfAndSelf)),
                        new BigDecimal("111")));

        Set<ExpressionCall> allResults = new HashSet<>(Arrays.asList(
                // From ExpressionCall 1
                new ExpressionCall("geneA", new Condition("organA", "stageA", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)), 
                        new BigDecimal("1.25")), 
                
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

                // From ExpressionCall 2
                new ExpressionCall("geneA", new Condition("organA", "parentStageA1", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.EST, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpSelfAndSelf)), 
                        new BigDecimal("1250")),
                
                new ExpressionCall("geneA", new Condition("parentOrganA1", "parentStageA1", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.EST, dpDescAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpDescAndSelf)), 
                        null), 

                // From ExpressionCall 3
                new ExpressionCall("geneB", new Condition("parentOrganA1", "parentStageA1", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, dpSelfAndSelf)), 
                        new BigDecimal("10")), 

                // From ExpressionCall 4
                new ExpressionCall("geneB", new Condition("organB", "stageB", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelf)), 
                        new BigDecimal("111")), 
                new ExpressionCall("geneB", new Condition("organB", "parentStageB1", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndDesc)), 
                        null)));

        Set<ExpressionCall> actualResults = service.propagateExpressionCalls(
                exprCalls, null, mockConditionUtils, speciesId);
        assertEquals("Incorrect ExpressionCalls generated", allResults, actualResults);
        
        Set<String> allowedOrganIds = new HashSet<>(Arrays.asList("organA"));
        Set<ExpressionCall> expectedResults = allResults.stream()
                .filter(c -> allowedOrganIds.contains(c.getCondition().getAnatEntityId()))
                .collect(Collectors.toSet());
        actualResults = service.propagateExpressionCalls(exprCalls, Arrays.asList(
                new ConditionFilter(allowedOrganIds, null)), mockConditionUtils, speciesId);
        assertEquals("Incorrect ExpressionCalls generated", expectedResults, actualResults);

        Set<String> allowedStageIds = new HashSet<>(Arrays.asList("parentStageA1"));
        expectedResults = allResults.stream()
                .filter(c -> allowedStageIds.contains(c.getCondition().getDevStageId()))
                .collect(Collectors.toSet());
        actualResults = service.propagateExpressionCalls(exprCalls, Arrays.asList(
                new ConditionFilter(null, allowedStageIds)), mockConditionUtils, speciesId);
        assertEquals("Incorrect ExpressionCalls generated", expectedResults, actualResults);

        expectedResults = allResults.stream()
                .filter(c -> allowedOrganIds.contains(c.getCondition().getAnatEntityId()))
                .filter(c -> allowedStageIds.contains(c.getCondition().getDevStageId()))
                .collect(Collectors.toSet());
        actualResults = service.propagateExpressionCalls(exprCalls, Arrays.asList(
                new ConditionFilter(allowedOrganIds, allowedStageIds)), mockConditionUtils, speciesId);
        assertEquals("Incorrect ExpressionCalls generated", expectedResults, actualResults);
    }
    
    /**
     * Test the method {@link CallService#propagateExpressionCalls(Collection, Collection, ConditionUtils, String)}.
     */
    @Test
    public void shouldPropagateNotExpressedCalls() {
        DAOManager manager = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(manager);

        CallService service = new CallService(serviceFactory);
        try {
            service.propagateExpressionCalls(null, null, null, null);
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
        when(mockConditionUtils.getDescendantConditions(parentCond, false)).thenReturn(descendantConds);
        
        parentCond = new Condition("organA", "parentStageA1", speciesId);
        descendantConds = new HashSet<>();
        when(mockConditionUtils.getDescendantConditions(parentCond, false)).thenReturn(descendantConds);

        parentCond = new Condition("parentOrganA2", "parentStageA1", speciesId);
        descendantConds = new HashSet<>(Arrays.asList(
                new Condition("parentOrganA1", "parentStageA1", speciesId),
                new Condition("organA", "parentStageA1", speciesId)));
        when(mockConditionUtils.getDescendantConditions(parentCond, false)).thenReturn(descendantConds);

        parentCond = new Condition("parentOrganB2", "stageB", speciesId);
        descendantConds = new HashSet<>(Arrays.asList(
                new Condition("parentOrganB1", "stageB", speciesId),
                new Condition("organB", "stageB", speciesId)));
        when(mockConditionUtils.getDescendantConditions(parentCond, false)).thenReturn(descendantConds);

        Collection<ExpressionCall> noExprCalls = Arrays.asList(
                // ExpressionCall 1
                new ExpressionCall("geneA", new Condition("organA", "stageA", speciesId),
                        dpSelfAndSelf, ExpressionSummary.NOT_EXPRESSED, DataQuality.LOW, Arrays.asList(
                                new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW,
                                        DataType.AFFYMETRIX, dpSelfAndSelf),
                                new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW,
                                        DataType.IN_SITU, dpSelfAndSelf)),
                        new BigDecimal("2.25")),
                // ExpressionCall 2
                new ExpressionCall("geneA", new Condition("organA", "parentStageA1", speciesId),
                        dpSelfAndSelf, ExpressionSummary.NOT_EXPRESSED, DataQuality.HIGH, Arrays.asList(
                                new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW,
                                        DataType.AFFYMETRIX, dpSelfAndSelf),
                                new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH,
                                        DataType.RNA_SEQ, dpSelfAndSelf)),
                        new BigDecimal("2250")),
                // ExpressionCall 3
                new ExpressionCall("geneA", new Condition("parentOrganA2", "parentStageA1", speciesId),
                        dpSelfAndSelf, ExpressionSummary.NOT_EXPRESSED, DataQuality.HIGH, Arrays.asList(
                                new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH,
                                        DataType.AFFYMETRIX, dpSelfAndSelf),
                                new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW,
                                        DataType.IN_SITU, dpSelfAndSelf),
                                new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW,
                                        DataType.RNA_SEQ, dpSelfAndSelf)),
                        new BigDecimal("20")),
                // ExpressionCall 4
                new ExpressionCall("geneB", new Condition("parentOrganB2", "stageB", speciesId),
                        dpSelfAndSelf, ExpressionSummary.NOT_EXPRESSED, DataQuality.HIGH, Arrays.asList(
                                new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH,
                                        DataType.AFFYMETRIX, dpSelfAndSelf)),
                        new BigDecimal("211")));

        Set<ExpressionCall> allResults = new HashSet<>(Arrays.asList(
                // From ExpressionCall 1
                new ExpressionCall("geneA", new Condition("organA", "stageA", speciesId), 
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndSelf),
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpSelfAndSelf)), 
                        new BigDecimal("2.25")), 
                
                // From ExpressionCall 2
                new ExpressionCall("geneA", new Condition("organA", "parentStageA1", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndSelf),
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)), 
                        new BigDecimal("2250")), 

                // From ExpressionCall 3
                new ExpressionCall("geneA", new Condition("parentOrganA2", "parentStageA1", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelf),
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpSelfAndSelf), 
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, dpSelfAndSelf)), 
                        new BigDecimal("20")), 

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

                // From ExpressionCall 4
                new ExpressionCall("geneB", new Condition("parentOrganB2", "stageB", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelf)), 
                        new BigDecimal("211")), 

                new ExpressionCall("geneB", new Condition("parentOrganB1", "stageB", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpAncAndSelf)), 
                        null), 

                new ExpressionCall("geneB", new Condition("organB", "stageB", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpAncAndSelf)), 
                        null)));

        Set<ExpressionCall> actualResults = service.propagateExpressionCalls(
                noExprCalls, null, mockConditionUtils, speciesId);
        assertEquals("Incorrect ExpressionCalls generated", allResults, actualResults);
        
        Set<String> allowedOrganIds = new HashSet<>(Arrays.asList("organA"));
        Set<ExpressionCall> expectedResults = allResults.stream()
                .filter(c -> allowedOrganIds.contains(c.getCondition().getAnatEntityId()))
                .collect(Collectors.toSet());
        actualResults = service.propagateExpressionCalls(
                noExprCalls, Arrays.asList(new ConditionFilter(allowedOrganIds, null)),
                mockConditionUtils, speciesId);
        assertEquals("Incorrect ExpressionCalls generated", expectedResults, actualResults);

        Set<String> allowedStageIds = new HashSet<>(Arrays.asList("parentStageA1"));
        expectedResults = allResults.stream()
                .filter(c -> allowedStageIds.contains(c.getCondition().getDevStageId()))
                .collect(Collectors.toSet());
        actualResults = service.propagateExpressionCalls(
                noExprCalls, Arrays.asList(new ConditionFilter(null, allowedStageIds)),
                mockConditionUtils, speciesId);
        assertEquals("Incorrect ExpressionCalls generated", expectedResults, actualResults);

        expectedResults = allResults.stream()
                .filter(c -> allowedOrganIds.contains(c.getCondition().getAnatEntityId()))
                .filter(c -> allowedStageIds.contains(c.getCondition().getDevStageId()))
                .collect(Collectors.toSet());
        actualResults = service.propagateExpressionCalls(
                noExprCalls, Arrays.asList(new ConditionFilter(allowedOrganIds, allowedStageIds)),
                mockConditionUtils, speciesId);
        assertEquals("Incorrect ExpressionCalls generated", expectedResults, actualResults);
    }
    
    /**
     * @return  the {@code Set} of {@code ExpressionCall}s from 
     * not expressed propagated {@code ExpressionCall}s,
     */
    private Set<ExpressionCall> getPropagationFromNotExpressedCalls(String speciesId) {
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
     * @return  the {@code Set} of not expressed {@code ExpressionCall}s 
     * from not expressed propagated {@code ExpressionCall}s,
     */
    private Set<ExpressionCall> getPropagationFromExpressedCalls(String speciesId) {
        
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
    public void shouldReconcileSingleGeneCalls() {
        String speciesId = "speciesId1";

        // EXPRESSED - HIGH quality - observed
        Set<ExpressionCall> inputCalls = new HashSet<>(Arrays.asList(
                new ExpressionCall("geneA", new Condition("organA", "stageA", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)), 
                        new BigDecimal("1.25")), 
                new ExpressionCall("geneA", new Condition("organA", "stageA", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndSelf)), 
                         new BigDecimal("12.5")), 
                new ExpressionCall("geneA", new Condition("organB", "parentStageB1", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndDesc)), 
                        new BigDecimal("125"))));
        ExpressionCall expectedResult = new ExpressionCall("geneA", null, 
                new DataPropagation(PropagationState.SELF, PropagationState.SELF_AND_DESCENDANT, true), 
                ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndDesc),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)), 
                new BigDecimal("1.25"));
        ExpressionCall actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect ExpressionCall generated", expectedResult, actualResult);
        
        inputCalls = new HashSet<>(Arrays.asList(
                new ExpressionCall("geneA", new Condition("organA", "stageA", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpDescAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)), 
                        new BigDecimal("1.25")), 
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
                new BigDecimal("1.25"));
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect ExpressionCall generated", expectedResult, actualResult);

        // EXPRESSED - LOW quality - not observed
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
                new DataPropagation(PropagationState.SELF_AND_DESCENDANT, PropagationState.SELF_AND_DESCENDANT, false), 
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
                        new BigDecimal("1.25")), 
                new ExpressionCall("geneA", new Condition("organB", "parentStageB1", speciesId),
                        null, null, null, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpAncAndSelf)), 
                        new BigDecimal("1.25"))));
        expectedResult = new ExpressionCall("geneA", null, 
                new DataPropagation(PropagationState.ANCESTOR, PropagationState.SELF, false), 
                ExpressionSummary.NOT_EXPRESSED, DataQuality.HIGH, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpAncAndSelf),
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpAncAndSelf)), 
                new BigDecimal("1.25"));
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
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndDesc)), 
                        null)));
        expectedResult = new ExpressionCall("geneA", null, 
                new DataPropagation(PropagationState.SELF_AND_ANCESTOR, PropagationState.SELF_AND_DESCENDANT, false), 
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
    public void shouldReconcileSingleGeneCalls_pipelineTest() {
        String geneId = "ID1";
        String speciesId = "speciesId1";
        Condition cond = new Condition("Anat_id1", "Stage_id1", speciesId);
        Set<ExpressionCall> inputCalls = filterExprCalls(geneId, cond, speciesId);
        inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
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
        inputCalls = filterExprCalls(geneId, cond, speciesId);
        inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
        expectedResult = new ExpressionCall(geneId, null, 
                dpSelfAndDesc, ExpressionSummary.EXPRESSED, DataQuality.LOW, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndDesc),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpSelfAndDesc),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.RNA_SEQ, dpSelfAndDesc)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);

        cond = new Condition("Anat_id1", "ParentStage_id2", speciesId);
        inputCalls = filterExprCalls(geneId, cond, speciesId);
        inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
        expectedResult = new ExpressionCall(geneId, null, 
                dpSelfAndSelf, ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.EST, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpSelfAndSelf)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);

        cond = new Condition("Anat_id1", "Stage_id2", speciesId);
        inputCalls = filterExprCalls(geneId, cond, speciesId);
        inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
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
        inputCalls = filterExprCalls(geneId, cond, speciesId);
        inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
        expectedResult = new ExpressionCall(geneId, null, 
                dpSelfAndSelf, ExpressionSummary.STRONG_AMBIGUITY, null, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndSelf), 
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);

        cond = new Condition("Anat_id1", "ParentStage_id2", speciesId);
        inputCalls = filterExprCalls(geneId, cond, speciesId);
        inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
        expectedResult = new ExpressionCall(geneId, null, 
                dpSelfDescAndDesc, ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfDescAndDesc),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfDescAndDesc),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpSelfDescAndDesc)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);

        cond = new Condition("Anat_id2", "Stage_id2", speciesId);
        inputCalls = filterExprCalls(geneId, cond, speciesId);
        inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
        expectedResult = new ExpressionCall(geneId, null, 
                dpAncAndSelf, ExpressionSummary.NOT_EXPRESSED, DataQuality.HIGH, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpAncAndSelf)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);

        cond = new Condition("Anat_id2", "ParentStage_id2", speciesId);
        inputCalls = filterExprCalls(geneId, cond, speciesId);
        inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
        expectedResult = new ExpressionCall(geneId, null, 
                dpDescAndSelf, ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpDescAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpDescAndSelf)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);

        cond = new Condition("Anat_id3", "ParentStage_id2", speciesId);
        inputCalls = filterExprCalls(geneId, cond, speciesId);
        inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
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
        inputCalls = filterExprCalls(geneId, cond, speciesId);
        inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
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
        inputCalls = filterExprCalls(geneId, cond, speciesId);
        inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
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
        inputCalls = filterExprCalls(geneId, cond, speciesId);
        inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
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
        inputCalls = filterExprCalls(geneId, cond, speciesId);
        inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
        expectedResult = new ExpressionCall(geneId, null, 
                dpSelfAndSelf, ExpressionSummary.NOT_EXPRESSED, DataQuality.HIGH, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpSelfAndSelf),
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);

        cond = new Condition("Anat_id4", "Stage_id5", speciesId);
        inputCalls = filterExprCalls(geneId, cond, speciesId);
        inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
        expectedResult = new ExpressionCall(geneId, null, 
                dpSelfAncAndSelf, ExpressionSummary.NOT_EXPRESSED, DataQuality.HIGH, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAncAndSelf), 
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.IN_SITU, dpSelfAncAndSelf), 
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAncAndSelf)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);
        
        cond = new Condition("Anat_id5", "Stage_id5", speciesId);
        inputCalls = filterExprCalls(geneId, cond, speciesId);
        inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
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
        inputCalls = filterExprCalls(geneId, cond, speciesId);
        inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
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
        inputCalls = filterExprCalls(geneId, cond, speciesId);
        inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
        expectedResult = new ExpressionCall(geneId, null, 
                dpDescAndDesc, ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpDescAndDesc),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpDescAndDesc),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpDescAndDesc)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);
        
        cond = new Condition("Anat_id4", "Stage_id5", speciesId);
        inputCalls = filterExprCalls(geneId, cond, speciesId);
        inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
        expectedResult = new ExpressionCall(geneId, null, 
                dpSelfAndSelf, ExpressionSummary.EXPRESSED, DataQuality.HIGH, Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.AFFYMETRIX, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST, dpSelfAndSelf),
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU, dpSelfAndSelf)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);

        cond = new Condition("Anat_id4", "ParentStage_id5", speciesId);
        inputCalls = filterExprCalls(geneId, cond, speciesId);
        inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
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
        inputCalls = filterExprCalls(geneId, cond, speciesId);
        inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
        expectedResult = new ExpressionCall(geneId, null, 
                dpSelfAndSelf, ExpressionSummary.NOT_EXPRESSED, DataQuality.HIGH, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpSelfAndSelf)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);

        cond = new Condition("Anat_id8", "Stage_id6", speciesId);
        inputCalls = filterExprCalls(geneId, cond, speciesId);
        inputCalls.addAll(filterNotExprCalls(geneId, cond, speciesId));
        expectedResult = new ExpressionCall(geneId, null, 
                dpAncAndSelf, ExpressionSummary.NOT_EXPRESSED, DataQuality.HIGH, Arrays.asList(
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ, dpAncAndSelf)), 
                null);
        actualResult = CallService.reconcileSingleGeneCalls(inputCalls);
        assertEquals("Incorrect generated ExpressionCalls", expectedResult, actualResult);
    }

    private Set<ExpressionCall> filterExprCalls(String geneId, Condition cond, String speciesId) {
        Set<ExpressionCall> inputCalls = getPropagationFromExpressedCalls(speciesId).stream()
                .filter(c -> c.getGeneId().equals(geneId))
                .filter(c -> c.getCondition().equals(cond))
                .collect(Collectors.toSet());
        return inputCalls;
    }
    
    private Set<ExpressionCall> filterNotExprCalls(String geneId, Condition cond, String speciesId) {
        Set<ExpressionCall> inputCalls = getPropagationFromNotExpressedCalls(speciesId).stream()
                .filter(c -> c.getGeneId().equals(geneId))
                .filter(c -> c.getCondition().equals(cond))
                .collect(Collectors.toSet());
        return inputCalls;
    }
    
    @Test
    public void shouldTryAdvance() {
        
        // Two streams well defined
        List<ExpressionCall> calls1 = new ArrayList<ExpressionCall>();
        calls1.add(new ExpressionCall("ID1", new Condition("ae1", "s1", "1"), null, null, null, null, null));
        calls1.add(new ExpressionCall("ID1", new Condition("ae2", "s1", "1"), null, null, null, null, null));
        calls1.add(new ExpressionCall("ID2", new Condition("ae1", "s1", "1"), null, null, null, null, null));
        calls1.add(new ExpressionCall("ID2", new Condition("ae2", "s1", "1"), null, null, null, null, null));
        Stream<ExpressionCall> stream1 = calls1.stream();

        List<ExpressionCall> calls2 = new ArrayList<ExpressionCall>(); 
        calls2.add(new ExpressionCall("ID1", new Condition("ae3", "s1", "1"), null, null, null, null, null));
        calls2.add(new ExpressionCall("ID4", new Condition("ae1", "s1", "1"), null, null, null, null, null));
        Stream<ExpressionCall> stream2 = calls2.stream();

        DAOManager manager = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(manager);
        CallService service = new CallService(serviceFactory);
        final CallSpliterator<Set<ExpressionCall>, ExpressionCall> spliterator1 = service.new CallSpliterator<>(
                stream1, stream2,
                Comparator.comparing(ExpressionCall::getGeneId, Comparator.nullsLast(Comparator.naturalOrder())));
        List<Set<ExpressionCall>> callsByGene = StreamSupport.stream(spliterator1, false)
                .onClose(() -> spliterator1.close())
                .collect(Collectors.toList());
        
        assertEquals(3, callsByGene.size());
        Set<ExpressionCall> gp1 = new HashSet<>();
        gp1.add(new ExpressionCall("ID1", new Condition("ae1", "s1", "1"), null, null, null, null, null));
        gp1.add(new ExpressionCall("ID1", new Condition("ae2", "s1", "1"), null, null, null, null, null));
        gp1.add(new ExpressionCall("ID1", new Condition("ae3", "s1", "1"), null, null, null, null, null));
        assertEquals(gp1, callsByGene.get(0));
     
        Set<ExpressionCall> gp2 = new HashSet<>();
        gp2.add(new ExpressionCall("ID2", new Condition("ae1", "s1", "1"), null, null, null, null, null));
        gp2.add(new ExpressionCall("ID2", new Condition("ae2", "s1", "1"), null, null, null, null, null));
        assertEquals(gp2, callsByGene.get(1));
        
        Set<ExpressionCall> gp3 = new HashSet<>();
        gp3.add(new ExpressionCall("ID4", new Condition("ae1", "s1", "1"), null, null, null, null, null));
        assertEquals(gp3, callsByGene.get(2));

        // One stream well defined and an empty stream
        stream1 = calls1.stream();
        stream2 = Stream.empty();
        final CallSpliterator<Set<ExpressionCall>, ExpressionCall> spliterator2 =
                service.new CallSpliterator<>(stream1, stream2, Comparator.comparing(
                        ExpressionCall::getGeneId, Comparator.nullsLast(Comparator.naturalOrder())));
        callsByGene = StreamSupport.stream(spliterator2, false)
                .onClose(() -> spliterator2.close())
                .collect(Collectors.toList());
        assertEquals(2, callsByGene.size());
        gp1.clear();
        gp1.add(new ExpressionCall("ID1", new Condition("ae1", "s1", "1"), null, null, null, null, null));
        gp1.add(new ExpressionCall("ID1", new Condition("ae2", "s1", "1"), null, null, null, null, null));
        assertEquals(gp1, callsByGene.get(0));
        assertEquals(gp2, callsByGene.get(1));
        
        // Different comparator
        calls1.clear();
        calls1.add(new ExpressionCall("ID2", new Condition("ae2", "s1", "1"), null, null, null, null, null));
        calls1.add(new ExpressionCall("ID1", new Condition("ae2", "s1", "1"), null, null, null, null, null));
        calls1.add(new ExpressionCall("ID2", new Condition("ae1", "s1", "1"), null, null, null, null, null));
        calls1.add(new ExpressionCall("ID1", new Condition("ae1", "s1", "1"), null, null, null, null, null));
        calls2.clear(); 
        calls2.add(new ExpressionCall("ID4", new Condition("ae2", "s1", "1"), null, null, null, null, null));
        calls2.add(new ExpressionCall("ID1", new Condition("ae1", "s1", "1"), null, null, null, null, null));

        stream1 = calls1.stream();
        stream2 = calls2.stream();
        final CallSpliterator<Set<ExpressionCall>, ExpressionCall> spliterator3 =
                service.new CallSpliterator<>(stream1, stream2, Comparator.comparing(
                        (call) -> call.getCondition().getAnatEntityId(),
                        Comparator.nullsLast(Comparator.reverseOrder())));
        callsByGene = StreamSupport.stream(spliterator3, false)
                .onClose(() -> spliterator3.close())
                .collect(Collectors.toList());
        assertEquals(2, callsByGene.size());
        
        gp1.clear();
        gp2.clear();
        gp1.add(new ExpressionCall("ID2", new Condition("ae2", "s1", "1"), null, null, null, null, null));
        gp1.add(new ExpressionCall("ID1", new Condition("ae2", "s1", "1"), null, null, null, null, null));
        gp1.add(new ExpressionCall("ID4", new Condition("ae2", "s1", "1"), null, null, null, null, null));
        gp2.add(new ExpressionCall("ID2", new Condition("ae1", "s1", "1"), null, null, null, null, null));
        gp2.add(new ExpressionCall("ID1", new Condition("ae1", "s1", "1"), null, null, null, null, null));
        gp2.add(new ExpressionCall("ID1", new Condition("ae1", "s1", "1"), null, null, null, null, null));

        assertEquals(gp1, callsByGene.get(0));
        assertEquals(gp2, callsByGene.get(1));
        
        
        // Bad order
        final CallSpliterator<Set<ExpressionCall>, ExpressionCall> spliterator4 =
                service.new CallSpliterator<>(stream1, stream2, Comparator.comparing(
                        ExpressionCall::getGeneId, Comparator.nullsLast(Comparator.naturalOrder())));
        try {
            callsByGene = StreamSupport.stream(spliterator4, false)
                    .onClose(() -> spliterator3.close())
                    .collect(Collectors.toList());
            fail("Should throw an exception due to bad orderof calls");
        } catch (IllegalStateException e) {
            // Test passed
        }
    }
}
