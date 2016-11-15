package org.bgee.model.analysis;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bgee.model.Service.Direction;
import org.bgee.model.ServiceFactory;
import org.bgee.model.TestAncestor;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.anatdev.AnatEntitySimilarity;
import org.bgee.model.anatdev.DevStageService;
import org.bgee.model.anatdev.DevStageSimilarity;
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
 * @since   Bgee 13, Nov. 2016
 */
public class AnalysisServiceTest extends TestAncestor {

    /**
     * Test the method {@link AnalysisService#loadMultiSpeciesExpressionCalls(Gene, Collection)}.
     */
    @Test
    public void shouldLoadMultiSpeciesExpressionCalls() {
        // Initialize mocks
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        // OntologyService to get ordered relevant taxa from the gene taxa
        OntologyService ontService = mock(OntologyService.class);
        when(serviceFactory.getOntologyService()).thenReturn(ontService);
        // GeneService to retrieve homologous organ groups
        GeneService geneService = mock(GeneService.class);
        when(serviceFactory.getGeneService()).thenReturn(geneService);
        // AnatEntityService to retrieve anat. entity similarities
        AnatEntityService anatEntityService = mock(AnatEntityService.class);
        when(serviceFactory.getAnatEntityService()).thenReturn(anatEntityService);
        // DevStageService to retrieve dev. stage similarities
        DevStageService devStageService = mock(DevStageService.class);
        when(serviceFactory.getDevStageService()).thenReturn(devStageService);
        // CallService to retrieve propagated and reconciled calls
        CallService callService = mock(CallService.class);
        when(serviceFactory.getCallService()).thenReturn(callService);
        
        String taxId1 = "taxId1";
        Taxon taxon1 = new Taxon(taxId1);
        String taxId2 = "taxId2";
        Taxon taxon2 = new Taxon(taxId2);
        String taxId10 = "taxId10";
        Taxon taxon10 = new Taxon(taxId10);
        String taxId100 = "taxId100";
        Taxon taxon100 = new Taxon(taxId100);
        // tax100--
        // |        \
        // tax10     \
        // |     \    \
        // tax1  tax2  tax3
        // |     |     |
        // sp1   sp2   sp3

        String spId1 = "spId1";
        String spId2 = "spId2";
        String spId3 = "spId3";
        Set<String> speciesIds = new HashSet<String>(Arrays.asList(spId1, spId2, spId3)); 

        @SuppressWarnings("unchecked")
        MultiSpeciesOntology<Taxon> taxonOnt = mock(MultiSpeciesOntology.class);
        when(ontService.getTaxonOntology(speciesIds, null, true, false)).thenReturn(taxonOnt);
        when(taxonOnt.getElements()).thenReturn(new HashSet<>(Arrays.asList(taxon1, taxon2)));
        when(taxonOnt.getElement(taxId1)).thenReturn(taxon1);
        when(taxonOnt.getOrderedAncestors(taxon1)).thenReturn(Arrays.asList(taxon10, taxon100));
        
        Map<String, Set<String>> omaToGeneIds1 = new HashMap<>();
        omaToGeneIds1.put("oma1", new HashSet<>(Arrays.asList("sp1g1", "sp2g1")));
        when(geneService.getOrthologs(taxId10, speciesIds)).thenReturn(omaToGeneIds1);
        Map<String, Set<String>> omaToGeneIds2 = new HashMap<>();
        omaToGeneIds2.put("oma2", new HashSet<>(Arrays.asList("sp1g1", "sp2g1", "sp3g1")));
        when(geneService.getOrthologs(taxId100, speciesIds)).thenReturn(omaToGeneIds2);

        AnatEntitySimilarity aeSim1 = new AnatEntitySimilarity("aeSim1", new HashSet<>(Arrays.asList("aeId1", "aeId2")));
        AnatEntitySimilarity aeSim2 = new AnatEntitySimilarity("aeSimA", new HashSet<>(Arrays.asList("aeId3", "aeId4")));
        when(anatEntityService.loadAnatEntitySimilarities(taxId10, speciesIds, true))
            .thenReturn(new HashSet<>(Arrays.asList(aeSim1)));
        when(anatEntityService.loadAnatEntitySimilarities(taxId100, speciesIds, true))
            .thenReturn(new HashSet<>(Arrays.asList(aeSim1, aeSim2)));

        DevStageSimilarity dsSim1 = new DevStageSimilarity("dsSim1", new HashSet<>(Arrays.asList("dsId1", "dsId2")));
        DevStageSimilarity dsSim2 = new DevStageSimilarity("dsSim2", new HashSet<>(Arrays.asList("dsId3", "dsId4")));
        DevStageSimilarity dsSim2b = new DevStageSimilarity("dsSim2b", new HashSet<>(Arrays.asList("dsId3", "dsId4", "dsId5")));
        when(devStageService.loadDevStageSimilarities(taxId10, speciesIds))
            .thenReturn(new HashSet<>(Arrays.asList(dsSim1, dsSim2)));
        when(devStageService.loadDevStageSimilarities(taxId100, speciesIds))
            .thenReturn(new HashSet<>(Arrays.asList(dsSim1, dsSim2b)));

        LinkedHashMap<CallService.OrderingAttribute, Direction> orderAttrs = new LinkedHashMap<>();
        orderAttrs.put(CallService.OrderingAttribute.GENE_ID, Direction.ASC);
        
        Set<String> orthologousGeneIds10 = new HashSet<>();
        orthologousGeneIds10.addAll(omaToGeneIds1.values().stream().flatMap(Set::stream).collect(Collectors.toSet()));
        Set<String> anatEntityIds10 = new HashSet<>();
        anatEntityIds10.addAll(aeSim1.getAnatEntityIds());
        Set<String> devStageIds10 = new HashSet<>();
        devStageIds10.addAll(dsSim1.getDevStageIds());
        devStageIds10.addAll(dsSim2.getDevStageIds());

        ExpressionCallFilter callFilter10 = new ExpressionCallFilter(
                new GeneFilter(orthologousGeneIds10), 
                new HashSet<>(Arrays.asList(new ConditionFilter(anatEntityIds10, devStageIds10))),
                new HashSet<>(Arrays.asList(new ExpressionCallData(Expression.EXPRESSED))));

        Set<String> orthologousGeneIds100 = new HashSet<>();
        orthologousGeneIds100.addAll(omaToGeneIds1.values().stream().flatMap(Set::stream).collect(Collectors.toSet()));
        orthologousGeneIds100.addAll(omaToGeneIds2.values().stream().flatMap(Set::stream).collect(Collectors.toSet()));
        Set<String> anatEntityIds100 = new HashSet<>();
        anatEntityIds100.addAll(aeSim1.getAnatEntityIds());
        anatEntityIds100.addAll(aeSim2.getAnatEntityIds());
        Set<String> devStageIds100 = new HashSet<>();
        devStageIds100.addAll(dsSim1.getDevStageIds());
        devStageIds100.addAll(dsSim2b.getDevStageIds());
        ExpressionCallFilter callFilter100 = new ExpressionCallFilter(
                new GeneFilter(orthologousGeneIds100), 
                new HashSet<>(Arrays.asList(new ConditionFilter(anatEntityIds100, devStageIds100))),
                new HashSet<>(Arrays.asList(new ExpressionCallData(Expression.EXPRESSED))));

        // aeSim1 - dsSim1
        ExpressionCall call1 = new ExpressionCall("sp1g1", new Condition("aeId1", "dsId1", spId1),
                new DataPropagation(PropagationState.SELF, PropagationState.ANCESTOR), ExpressionSummary.EXPRESSED, DataQuality.HIGH,
                Arrays.asList(new ExpressionCallData(Expression.EXPRESSED, DataType.AFFYMETRIX)), null);
        // aeSim1 - dsSim1
        ExpressionCall call2 = new ExpressionCall("sp2g1", new Condition("aeId2", "dsId2", spId2),
            new DataPropagation(), ExpressionSummary.NOT_EXPRESSED, DataQuality.HIGH,
            Arrays.asList(new ExpressionCallData(Expression.NOT_EXPRESSED, DataType.RNA_SEQ)), null);
        // aeSim1 - dsSim2
        ExpressionCall call3 = new ExpressionCall("sp1g1", new Condition("aeId1", "dsId4", spId1),
                new DataPropagation(PropagationState.SELF, PropagationState.ANCESTOR), ExpressionSummary.EXPRESSED, DataQuality.HIGH,
                Arrays.asList(new ExpressionCallData(Expression.EXPRESSED, DataType.AFFYMETRIX)), null);
        // aeSim2 - dsSim2 & dsSim2b
        ExpressionCall call4 = new ExpressionCall("sp1g1", new Condition("aeId4", "dsId3", spId1),
            new DataPropagation(), ExpressionSummary.EXPRESSED, DataQuality.HIGH,
            Arrays.asList(new ExpressionCallData(Expression.EXPRESSED, DataType.RNA_SEQ)), null);
        // aeSim2 - dsSim2 & dsSim2b
        ExpressionCall call5 = new ExpressionCall("sp2g1", new Condition("aeId3", "dsId4", spId2),
            new DataPropagation(), ExpressionSummary.EXPRESSED, DataQuality.LOW,
            Arrays.asList(new ExpressionCallData(Expression.EXPRESSED, DataType.IN_SITU)), null);
        // aeSim2 - dsSim2b
        ExpressionCall call6 = new ExpressionCall("sp3g1", new Condition("aeId4", "dsId5", spId3),
            new DataPropagation(), ExpressionSummary.EXPRESSED, DataQuality.LOW,
            Arrays.asList(new ExpressionCallData(Expression.EXPRESSED, DataType.IN_SITU)), null);
        
        when(callService.loadExpressionCalls(spId1, callFilter10, null, orderAttrs, false))
            .thenReturn(Arrays.asList(call1, call3).stream());
        when(callService.loadExpressionCalls(spId2, callFilter10, null, orderAttrs, false))
            .thenReturn(Arrays.asList(call2).stream());
        when(callService.loadExpressionCalls(spId3, callFilter10, null, orderAttrs, false))
            .thenReturn(Stream.empty());

        when(callService.loadExpressionCalls(spId1, callFilter100, null, orderAttrs, false))
            .thenReturn(Arrays.asList(call1, call3, call4).stream());
        when(callService.loadExpressionCalls(spId2, callFilter100, null, orderAttrs, false))
            .thenReturn(Arrays.asList(call2, call5).stream());
        when(callService.loadExpressionCalls(spId3, callFilter100, null, orderAttrs, false))
            .thenReturn(Arrays.asList(call6).stream());
        
        AnalysisService analysisService = new AnalysisService(serviceFactory);
        Gene gene = new Gene("sp1g1", spId1, null, null, new Species(spId1, null, null, null, null, null, taxId1, null, null));
        Map<String, Set<MultiSpeciesCall<ExpressionCall>>> actual = 
            analysisService.loadMultiSpeciesExpressionCalls(gene, speciesIds);

        LinkedHashMap<String, Set<MultiSpeciesCall<ExpressionCall>>> expected = new LinkedHashMap<>();
        
        Set<MultiSpeciesCall<ExpressionCall>> multiSpCalls1 = new HashSet<>();
        multiSpCalls1.add(new MultiSpeciesCall<>(aeSim1, dsSim1, taxId10, "oma1", orthologousGeneIds10,
            new HashSet<>(Arrays.asList(call1, call2)), null, serviceFactory));
        multiSpCalls1.add(new MultiSpeciesCall<>(aeSim1, dsSim2, taxId10, "oma1", orthologousGeneIds10,
            new HashSet<>(Arrays.asList(call3)), null, serviceFactory));
        expected.put(taxId10, multiSpCalls1);

        Set<MultiSpeciesCall<ExpressionCall>> multiSpCalls100 = new HashSet<>();
        multiSpCalls100.add(new MultiSpeciesCall<>(aeSim1, dsSim1, taxId100, "oma2", orthologousGeneIds100,
            new HashSet<>(Arrays.asList(call1, call2)), null, serviceFactory));
        multiSpCalls100.add(new MultiSpeciesCall<>(aeSim1, dsSim2b, taxId100, "oma2", orthologousGeneIds100,
            new HashSet<>(Arrays.asList(call3)), null, serviceFactory));
        multiSpCalls100.add(new MultiSpeciesCall<>(aeSim2, dsSim2b, taxId100, "oma2", orthologousGeneIds100, 
            new HashSet<>(Arrays.asList(call4, call5, call6)), null, serviceFactory));
        expected.put(taxId100, multiSpCalls100);

        assertEquals("Incorrect multi-species expression calls", new ArrayList<>(expected.keySet()),
            new ArrayList<>(actual.keySet()));
        assertEquals("Incorrect multi-species expression calls for tax ID 10",
            expected.get(taxId10), actual.get(taxId10));
        assertEquals("Incorrect multi-species expression calls for tax ID 100",
            expected.get(taxId100), actual.get(taxId100));
    }
}
