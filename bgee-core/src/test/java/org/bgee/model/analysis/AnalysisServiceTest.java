package org.bgee.model.analysis;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bgee.model.ServiceFactory;
import org.bgee.model.TestAncestor;
import org.bgee.model.Service.Direction;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.anatdev.AnatEntitySimilarity;
import org.bgee.model.anatdev.DevStageService;
import org.bgee.model.anatdev.DevStageSimilarity;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.baseelements.CallType.Expression;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.DataPropagation.PropagationState;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.Condition;
import org.bgee.model.expressiondata.ConditionFilter;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.gene.GeneService;
import org.bgee.model.ontology.MultiSpeciesOntology;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.ontology.OntologyService;
import org.bgee.model.species.Species;
import org.bgee.model.species.Taxon;
import org.junit.Test;

/**
 * This class holds the unit tests for the {@code AnalysisService} class.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Aug. 2016
 * @since   Bgee 13, Aug. 2016
 */
public class AnalysisServiceTest extends TestAncestor {

    @Test
    public void shouldLoadMultiSpeciesExpressionCalls() {
        // initialize mocks
        DAOManager managerMock = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
        OntologyService ontService = mock(OntologyService.class);
        when(serviceFactory.getOntologyService()).thenReturn(ontService);
        GeneService geneService = mock(GeneService.class);
        when(serviceFactory.getGeneService()).thenReturn(geneService);
        AnatEntityService anatEntityService = mock(AnatEntityService.class);
        when(serviceFactory.getAnatEntityService()).thenReturn(anatEntityService);
        DevStageService devStageService = mock(DevStageService.class);
        when(serviceFactory.getDevStageService()).thenReturn(devStageService);
        CallService callService = mock(CallService.class);
        when(serviceFactory.getCallService()).thenReturn(callService);
        
        String taxId = "9605";
        Taxon taxon = new Taxon(taxId);
        String sp1Id = "9606";
        Set<String> speciesIds = new HashSet<String>(Arrays.asList(sp1Id)); 

        @SuppressWarnings("unchecked")
        MultiSpeciesOntology<Taxon> taxonOnt = mock(MultiSpeciesOntology.class);
        when(ontService.getTaxonOntology(speciesIds, null, true, false)).thenReturn(taxonOnt);
        when(taxonOnt.getElements()).thenReturn(new HashSet<>(Arrays.asList(taxon)));
        when(taxonOnt.getElement(taxId)).thenReturn(taxon);

        Map<String, Set<String>> omaToGeneIds = new HashMap<>();
        omaToGeneIds.put("oma1", new HashSet<>(Arrays.asList("geneId1", "geneId2")));
        omaToGeneIds.put("oma2", new HashSet<>(Arrays.asList("geneId3", "geneId4")));
        when(geneService.getOrthologies(taxId, speciesIds)).thenReturn(omaToGeneIds);

        Set<AnatEntitySimilarity> aeSim = new HashSet<>();
        aeSim.add(new AnatEntitySimilarity("aeSim1", new HashSet<>(Arrays.asList("aeId1", "aeId2"))));
        aeSim.add(new AnatEntitySimilarity("aeSim2", new HashSet<>(Arrays.asList("aeId3", "aeId4"))));
        when(anatEntityService.loadAnatEntitySimilarities(taxId, speciesIds, true)).thenReturn(aeSim);

        Set<DevStageSimilarity> dsSim = new HashSet<>();
        dsSim.add(new DevStageSimilarity("dsSim1", new HashSet<>(Arrays.asList("dsId1", "dsId2"))));
        dsSim.add(new DevStageSimilarity("dsSim2", new HashSet<>(Arrays.asList("dsId3", "dsId4"))));
        when(devStageService.loadDevStageSimilarities(taxId, speciesIds)).thenReturn(dsSim);

        LinkedHashMap<CallService.OrderingAttribute, Direction> orderAttrs = new LinkedHashMap<>();
        orderAttrs.put(CallService.OrderingAttribute.GENE_ID, Direction.ASC);

        Set<String> orthologousGeneIds = new HashSet<>(Arrays.asList("geneId1", "geneId2"));
        Set<String> anatEntityIds = new HashSet<>(Arrays.asList("aeId1", "aeId2", "aeId3", "aeId4"));
        Set<String> devStageIds = new HashSet<>(Arrays.asList("dsId1", "dsId2", "dsId3", "dsId4"));

        ExpressionCallFilter callFilter = new ExpressionCallFilter(
                new GeneFilter(orthologousGeneIds), 
                new HashSet<>(Arrays.asList(new ConditionFilter(anatEntityIds, devStageIds))),
                new HashSet<>(Arrays.asList(new ExpressionCallData(Expression.EXPRESSED))));
        Stream<ExpressionCall> exprCallStream = Arrays.asList(
                new ExpressionCall("geneId1", new Condition("aeId1", "dsId1", sp1Id),
                        new DataPropagation(PropagationState.SELF, PropagationState.ANCESTOR), ExpressionSummary.EXPRESSED, DataQuality.HIGH,
                        Arrays.asList(new ExpressionCallData(Expression.EXPRESSED, DataType.AFFYMETRIX)), null),
                new ExpressionCall("geneId2", new Condition("aeId1", "dsId1", sp1Id),
                        new DataPropagation(), ExpressionSummary.EXPRESSED, DataQuality.HIGH,
                        Arrays.asList(new ExpressionCallData(Expression.EXPRESSED, DataType.RNA_SEQ)), null),
                new ExpressionCall("geneId3", new Condition("aeId2", "dsId2", sp1Id),
                        new DataPropagation(), ExpressionSummary.EXPRESSED, DataQuality.LOW,
                        Arrays.asList(new ExpressionCallData(Expression.EXPRESSED, DataType.IN_SITU)), null),
                new ExpressionCall("geneId3", new Condition("aeId1", "dsId4", sp1Id),
                        new DataPropagation(), ExpressionSummary.EXPRESSED, DataQuality.LOW,
                        Arrays.asList(new ExpressionCallData(Expression.EXPRESSED, DataType.IN_SITU)), null)
                ).stream();
        when(callService.loadExpressionCalls(sp1Id, callFilter, null, orderAttrs)).thenReturn(exprCallStream);
        
        AnalysisService analysisService = new AnalysisService(serviceFactory);
        Gene gene = new Gene("geneId1", sp1Id, null, null, new Species(sp1Id, null, null, null, null, null, taxId, null, null));
        analysisService.loadMultiSpeciesExpressionCalls(gene, speciesIds);
    }
}
