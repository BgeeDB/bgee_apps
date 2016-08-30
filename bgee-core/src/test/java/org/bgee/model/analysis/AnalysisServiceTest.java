package org.bgee.model.analysis;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.bgee.model.Service.Direction;
import org.bgee.model.ServiceFactory;
import org.bgee.model.TestAncestor;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.anatdev.AnatEntitySimilarity;
import org.bgee.model.anatdev.DevStageService;
import org.bgee.model.anatdev.DevStageSimilarity;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.Condition;
import org.bgee.model.expressiondata.ConditionFilter;
import org.bgee.model.expressiondata.MultiSpeciesCall;
import org.bgee.model.expressiondata.baseelements.CallType.Expression;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.DataPropagation.PropagationState;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.gene.GeneService;
import org.bgee.model.ontology.MultiSpeciesOntology;
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
        
        String taxId1 = "9605";
        Taxon taxon1 = new Taxon(taxId1);
        String taxId2 = "9600";
        Taxon taxon2 = new Taxon(taxId2);

        String sp1Id = "9606";
        String sp2Id = "9601";
        Set<String> speciesIds = new HashSet<String>(Arrays.asList(sp1Id, sp2Id)); 

        @SuppressWarnings("unchecked")
        MultiSpeciesOntology<Taxon> taxonOnt = mock(MultiSpeciesOntology.class);
        when(ontService.getTaxonOntology(speciesIds, null, true, false)).thenReturn(taxonOnt);
        when(taxonOnt.getElements()).thenReturn(new HashSet<>(Arrays.asList(taxon1, taxon2)));
        when(taxonOnt.getElement(taxId1)).thenReturn(taxon1);
        when(taxonOnt.getElement(taxId2)).thenReturn(taxon2);

        Map<String, Set<String>> omaToGeneIds1 = new HashMap<>();
        omaToGeneIds1.put("oma1", new HashSet<>(Arrays.asList("geneId1", "geneId2")));
        omaToGeneIds1.put("oma2", new HashSet<>(Arrays.asList("geneId3", "geneId4")));
        when(geneService.getOrthologs(taxId1, speciesIds)).thenReturn(omaToGeneIds1);
        Map<String, Set<String>> omaToGeneIds2 = new HashMap<>();
        omaToGeneIds1.put("omaA", new HashSet<>(Arrays.asList("geneIdA", "geneIdB")));
        when(geneService.getOrthologs(taxId2, speciesIds)).thenReturn(omaToGeneIds2);

        AnatEntitySimilarity aeSim1 = new AnatEntitySimilarity("aeSim1", new HashSet<>(Arrays.asList("aeId1", "aeId2")));
        AnatEntitySimilarity aeSim2 = new AnatEntitySimilarity("aeSim2", new HashSet<>(Arrays.asList("aeId3", "aeId4")));
        AnatEntitySimilarity aeSimA = new AnatEntitySimilarity("aeSimA", new HashSet<>(Arrays.asList("aeIdA", "aeIdB")));
        when(anatEntityService.loadAnatEntitySimilarities(taxId1, speciesIds, true))
            .thenReturn(new HashSet<>(Arrays.asList(aeSim1, aeSim2)));
        when(anatEntityService.loadAnatEntitySimilarities(taxId2, speciesIds, true))
            .thenReturn(new HashSet<>(Arrays.asList(aeSimA)));

        DevStageSimilarity dsSim1 = new DevStageSimilarity("dsSim1", new HashSet<>(Arrays.asList("dsId1", "dsId2")));
        DevStageSimilarity dsSim2 = new DevStageSimilarity("dsSim2", new HashSet<>(Arrays.asList("dsId3", "dsId4")));
        DevStageSimilarity dsSimA = new DevStageSimilarity("dsSimA", new HashSet<>(Arrays.asList("dsIdA", "dsIdB")));
        when(devStageService.loadDevStageSimilarities(taxId1, speciesIds))
            .thenReturn(new HashSet<>(Arrays.asList(dsSim1, dsSim2)));
        when(devStageService.loadDevStageSimilarities(taxId1, speciesIds))
            .thenReturn(new HashSet<>(Arrays.asList(dsSimA)));

        LinkedHashMap<CallService.OrderingAttribute, Direction> orderAttrs = new LinkedHashMap<>();
        orderAttrs.put(CallService.OrderingAttribute.GENE_ID, Direction.ASC);

        Set<String> orthologousGeneIds1 = new HashSet<>(Arrays.asList("geneId1", "geneId2"));
        Set<String> orthologousGeneIds2 = new HashSet<>(Arrays.asList("geneIdA", "geneIdB"));
        Set<String> anatEntityIds1 = new HashSet<>(Arrays.asList("aeId1", "aeId2", "aeId3", "aeId4"));
        Set<String> anatEntityIds2 = new HashSet<>(Arrays.asList("aeIdA", "aeIdB"));
        Set<String> devStageIds1 = new HashSet<>(Arrays.asList("dsId1", "dsId2", "dsId3", "dsId4"));
        Set<String> devStageIds2 = new HashSet<>(Arrays.asList("dsIdA", "dsIdB"));

        ExpressionCallFilter callFilter1 = new ExpressionCallFilter(
                new GeneFilter(orthologousGeneIds1), 
                new HashSet<>(Arrays.asList(new ConditionFilter(anatEntityIds1, devStageIds1))),
                new HashSet<>(Arrays.asList(new ExpressionCallData(Expression.EXPRESSED))));

        ExpressionCallFilter callFilter2 = new ExpressionCallFilter(
                new GeneFilter(orthologousGeneIds2), 
                new HashSet<>(Arrays.asList(new ConditionFilter(anatEntityIds2, devStageIds2))),
                new HashSet<>(Arrays.asList(new ExpressionCallData(Expression.EXPRESSED))));

        ExpressionCall call1 = new ExpressionCall("geneId1", new Condition("aeId1", "dsId1", sp1Id),
                new DataPropagation(PropagationState.SELF, PropagationState.ANCESTOR), ExpressionSummary.EXPRESSED, DataQuality.HIGH,
                Arrays.asList(new ExpressionCallData(Expression.EXPRESSED, DataType.AFFYMETRIX)), null);
        ExpressionCall call2 = new ExpressionCall("geneId2", new Condition("aeId1", "dsId1", sp1Id),
                new DataPropagation(), ExpressionSummary.EXPRESSED, DataQuality.HIGH,
                Arrays.asList(new ExpressionCallData(Expression.EXPRESSED, DataType.RNA_SEQ)), null);
        ExpressionCall call3 = new ExpressionCall("geneId3", new Condition("aeId2", "dsId2", sp1Id),
                new DataPropagation(), ExpressionSummary.EXPRESSED, DataQuality.LOW,
                Arrays.asList(new ExpressionCallData(Expression.EXPRESSED, DataType.IN_SITU)), null);
        ExpressionCall call4 = new ExpressionCall("geneId3", new Condition("aeId1", "dsId4", sp1Id),
                new DataPropagation(), ExpressionSummary.EXPRESSED, DataQuality.LOW,
                Arrays.asList(new ExpressionCallData(Expression.EXPRESSED, DataType.IN_SITU)), null);
        ExpressionCall callA = new ExpressionCall("geneIdB", new Condition("aeIdB", "dsIdA", sp2Id),
                new DataPropagation(), ExpressionSummary.EXPRESSED, DataQuality.LOW,
                Arrays.asList(new ExpressionCallData(Expression.EXPRESSED, DataType.EST)), null);
        ExpressionCall callB = new ExpressionCall("geneIdB", new Condition("aeIdA", "dsIdB", sp2Id),
                new DataPropagation(), ExpressionSummary.STRONG_AMBIGUITY, DataQuality.HIGH,
                Arrays.asList(new ExpressionCallData(Expression.EXPRESSED, DataType.IN_SITU),
                        new ExpressionCallData(Expression.EXPRESSED, DataType.AFFYMETRIX),
                        new ExpressionCallData(Expression.NOT_EXPRESSED, DataType.RNA_SEQ)), null);
        Stream<ExpressionCall> exprCallStream1 = Arrays.asList(call1, call2, call3, call4).stream();
        Stream<ExpressionCall> exprCallStream2 = Arrays.asList(callA, callB).stream();
        when(callService.loadExpressionCalls(sp1Id, callFilter1, null, orderAttrs)).thenReturn(exprCallStream1);
        when(callService.loadExpressionCalls(sp2Id, callFilter2, null, orderAttrs)).thenReturn(exprCallStream2);
        
        AnalysisService analysisService = new AnalysisService(serviceFactory);
        Gene gene = new Gene("geneId1", sp1Id, null, null, new Species(sp1Id, null, null, null, null, null, taxId1, null, null));
//        Map<String, Set<MultiSpeciesCall<ExpressionCall>>> actual = 
//                analysisService.loadMultiSpeciesExpressionCalls(gene, speciesIds);

        Map<String, Set<MultiSpeciesCall<ExpressionCall>>> expected = new HashMap<>();
        Set<MultiSpeciesCall<ExpressionCall>> multiSpCalls1 = new HashSet<>();
        multiSpCalls1.add(new MultiSpeciesCall<>(aeSim1, dsSim1, taxId1, "oma1", orthologousGeneIds1,
                new HashSet<>(Arrays.asList(call1, call2)), null, serviceFactory));
        multiSpCalls1.add(new MultiSpeciesCall<>(aeSim1, dsSim1, taxId1, "oma2", orthologousGeneIds1,
                new HashSet<>(Arrays.asList(call3)), null, serviceFactory));
        multiSpCalls1.add(new MultiSpeciesCall<>(aeSim1, dsSim2, taxId1, "oma2", orthologousGeneIds1, 
                new HashSet<>(Arrays.asList(call4)), null, serviceFactory));
        expected.put(taxId1, multiSpCalls1);
        Set<MultiSpeciesCall<ExpressionCall>> multiSpCalls2 = new HashSet<>();
        multiSpCalls2.add(new MultiSpeciesCall<>(aeSimA, dsSimA, taxId2, "omaA", orthologousGeneIds2,
                new HashSet<>(Arrays.asList(callA, callB)), null, serviceFactory));
        expected.put(taxId2, multiSpCalls2);

//        assertEquals("Incorrect multi-species expression calls", expected, actual);
    }
}
