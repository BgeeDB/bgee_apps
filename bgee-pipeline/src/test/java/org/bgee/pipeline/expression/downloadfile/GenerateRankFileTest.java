package org.bgee.pipeline.expression.downloadfile;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.Condition;
import org.bgee.model.expressiondata.ConditionGraph;
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
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.gene.GeneService;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.ontology.OntologyService;
import org.bgee.model.species.Species;
import org.bgee.model.species.SpeciesService;
import org.bgee.pipeline.TestAncestor;
import org.bgee.pipeline.Utils;
import org.bgee.pipeline.ontologycommon.OntologyUtils;
import org.bgee.pipeline.uberon.Uberon;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.semanticweb.owlapi.model.OWLClass;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;

import com.google.common.base.Supplier;

import owltools.graph.OWLGraphWrapper;

/**
 * Unit tests for {@link GenerateRankFile}.
 *
 * @author  Frederic Bastian
 * @author  Julien Wollbrett
 * @version Bgee 14 July 2018
 * @since   Bgee 13 July 2016
 */
public class GenerateRankFileTest extends TestAncestor {
    private final static Logger log = LogManager.getLogger(GenerateRankFileTest.class.getName());
    @Override
    protected Logger getLogger() {
        return log;
    }
    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder();
    
    /**
     * Test {@link GenerateRankFile#generateRankFiles(Set, boolean, Set, String)}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void shouldGenerateRankFiles() throws IOException {
        
        ///*** create objects require for test ***
        AnatEntity anatEntity1 = new AnatEntity("anat1", "Anat name 1", null);
        AnatEntity anatEntity2 = new AnatEntity("anat2", "Anat name 2", null);
        DevStage devStage1 = new DevStage("stage1", "Stage name 1", null);
        DevStage devStage2 = new DevStage("stage2", "Stage name 2", null);
        Species spe1 = new Species(1, "my species1", null, "my", "species1", null, null);
        Species spe2 = new Species(2, "my species2", null, "my", "species2", null, null);
        Condition cond1 = new Condition(anatEntity1, null, spe1);
        Condition cond2 = new Condition(anatEntity1, devStage1, spe1);
        Condition cond3 = new Condition(anatEntity2, devStage2, spe1);
        Condition cond4 = new Condition(anatEntity2, null, spe2);
        Condition cond5 = new Condition(anatEntity2, devStage1, spe2);

        Gene gene1 = new Gene("gene1", spe1);
        Gene gene2 = new Gene("gene2", spe1);
        Gene gene3 = new Gene("gene3", "gene3Name", null, spe2, 1);
        
      //conditions1 and 2 spe1 gene1
        ExpressionCall c1 = new ExpressionCall(gene1, cond1, new DataPropagation(), 
                ExpressionSummary.EXPRESSED, SummaryQuality.SILVER,  Arrays.asList(
                        new ExpressionCallData(DataType.AFFYMETRIX, Collections.singleton(
                                new ExperimentExpressionCount(Expression.EXPRESSED, DataQuality.LOW, 
                                        PropagationState.SELF, 2)), 2, new BigDecimal("5.0"), 
                                new BigDecimal("1.25"), new BigDecimal("1.25"), new DataPropagation())),
                        new BigDecimal("1.25"), new BigDecimal("1.25"));   
        
        ExpressionCall c2 = new ExpressionCall(gene1, cond2, new DataPropagation(), 
                ExpressionSummary.EXPRESSED, SummaryQuality.SILVER,  Arrays.asList(
                        new ExpressionCallData(DataType.EST, Collections.singleton(
                                new ExperimentExpressionCount(Expression.EXPRESSED, DataQuality.LOW, 
                                        PropagationState.SELF, 2)), 2, new BigDecimal("1.25"), 
                                new BigDecimal("1.25"), new BigDecimal("1.25"), new DataPropagation())),
                new BigDecimal("1.25"), new BigDecimal("1.25"));
        
        ExpressionCall c3 = new ExpressionCall(gene1, cond3, new DataPropagation(), 
                ExpressionSummary.EXPRESSED, SummaryQuality.SILVER,  Arrays.asList(
                        new ExpressionCallData(DataType.AFFYMETRIX, Collections.singleton(
                                new ExperimentExpressionCount(Expression.EXPRESSED, DataQuality.LOW, 
                                        PropagationState.SELF, 2)), 2, new BigDecimal("8.0"), 
                                new BigDecimal("8.0"), new BigDecimal("8.0"), new DataPropagation())),
                new BigDecimal("8.0"), new BigDecimal("8.0"));
        
        //condition3 spe1 gene2
        ExpressionCall c4 = new ExpressionCall(gene1, cond2, new DataPropagation(), 
                ExpressionSummary.EXPRESSED, SummaryQuality.BRONZE,  Arrays.asList(
                        new ExpressionCallData(DataType.AFFYMETRIX, Collections.singleton(
                                new ExperimentExpressionCount(Expression.EXPRESSED, DataQuality.LOW, 
                                        PropagationState.SELF, 2)), 2, new BigDecimal("1.25"), 
                                new BigDecimal("1.35"), new BigDecimal("1.35"), new DataPropagation())),
                new BigDecimal("1.35"), new BigDecimal("1.35"));
        
        //condition2 spe1 gene2
        ExpressionCall c5 = new ExpressionCall(gene2, cond2, new DataPropagation(), 
                ExpressionSummary.EXPRESSED, SummaryQuality.SILVER, 
                Arrays.asList(
                        new ExpressionCallData(DataType.AFFYMETRIX, Collections.singleton(
                                new ExperimentExpressionCount(Expression.EXPRESSED, DataQuality.LOW, 
                                        PropagationState.SELF, 2)), 2, new BigDecimal("10000.00"), 
                                new BigDecimal("10000.00"), new BigDecimal("10000.00"), new DataPropagation()), 
                        new ExpressionCallData(DataType.EST, Collections.singleton(
                                new ExperimentExpressionCount(Expression.EXPRESSED, DataQuality.LOW, 
                                        PropagationState.SELF, 2)), 2, new BigDecimal("10000.00"), 
                                new BigDecimal("10000.00"), new BigDecimal("10000.00"), new DataPropagation()),
                        new ExpressionCallData(DataType.IN_SITU, Collections.singleton(
                                new ExperimentExpressionCount(Expression.EXPRESSED, DataQuality.HIGH, 
                                        PropagationState.SELF, 2)), 2, new BigDecimal("10000.00"), 
                                new BigDecimal("10000.00"), new BigDecimal("10000.00"), new DataPropagation()), 
                        new ExpressionCallData(DataType.RNA_SEQ, Collections.singleton(
                                new ExperimentExpressionCount(Expression.EXPRESSED, DataQuality.HIGH, 
                                        PropagationState.SELF, 2)), 2, new BigDecimal("10000.00"), 
                                new BigDecimal("10000.00"), new BigDecimal("10000.00"), new DataPropagation())),  
                new BigDecimal("10000.00"), new BigDecimal("10000.00"));
        
        //condition4 spe2 gene3
        ExpressionCall c6 = new ExpressionCall(gene3, cond4, new DataPropagation(), 
                ExpressionSummary.EXPRESSED, SummaryQuality.SILVER, 
                Arrays.asList( 
                        new ExpressionCallData(DataType.AFFYMETRIX, Collections.singleton(
                                new ExperimentExpressionCount(Expression.EXPRESSED, DataQuality.LOW, 
                                        PropagationState.SELF, 2)), 2, new BigDecimal("10000.45"), 
                                new BigDecimal("10000.45"), new BigDecimal("10000.45"), new DataPropagation()),
                        new ExpressionCallData(DataType.IN_SITU, Collections.singleton(
                                new ExperimentExpressionCount(Expression.EXPRESSED, DataQuality.HIGH, 
                                        PropagationState.SELF, 2)), 2, new BigDecimal("10000.45"), 
                                new BigDecimal("10000.45"), new BigDecimal("10000.45"), new DataPropagation()), 
                        new ExpressionCallData(DataType.RNA_SEQ, Collections.singleton(
                                new ExperimentExpressionCount(Expression.EXPRESSED, DataQuality.HIGH, 
                                        PropagationState.SELF, 2)), 2, new BigDecimal("10000.45"), 
                                new BigDecimal("10000.45"), new BigDecimal("10000.45"), new DataPropagation())),
                new BigDecimal("10000.45"), new BigDecimal("10000.45"));
        
      //condition5 spe2 gene3
        ExpressionCall c7 = new ExpressionCall(gene3, cond5, new DataPropagation(), 
                ExpressionSummary.EXPRESSED, SummaryQuality.SILVER, 
                Arrays.asList( 
                        new ExpressionCallData(DataType.AFFYMETRIX, Collections.singleton(
                                new ExperimentExpressionCount(Expression.EXPRESSED, DataQuality.LOW, 
                                        PropagationState.SELF, 2)), 2, new BigDecimal("600.2"), 
                                new BigDecimal("600.2"), new BigDecimal("600.2"), new DataPropagation()),
                        new ExpressionCallData(DataType.IN_SITU, Collections.singleton(
                                new ExperimentExpressionCount(Expression.EXPRESSED, DataQuality.HIGH, 
                                        PropagationState.SELF, 2)), 2, new BigDecimal("10000.45"), 
                                new BigDecimal("10000.45"), new BigDecimal("10000.45"), new DataPropagation()), 
                        new ExpressionCallData(DataType.RNA_SEQ, Collections.singleton(
                                new ExperimentExpressionCount(Expression.EXPRESSED, DataQuality.HIGH, 
                                        PropagationState.SELF, 2)), 2, new BigDecimal("10000.45"), 
                                new BigDecimal("10000.45"), new BigDecimal("10000.45"), new DataPropagation())),
                new BigDecimal("600.2"), new BigDecimal("10000.45"));
        
        
        //*** Mock services and utils ***
        ConditionGraph condGraph = mock(ConditionGraph.class);
        Ontology<AnatEntity, String> anatEntityOntology = mock(Ontology.class);
        when(condGraph.getAnatEntityOntology()).thenReturn(anatEntityOntology);
        when(condGraph.getAnatEntityOntology().getElement(anatEntity1.getId())).thenReturn(anatEntity1);
        when(condGraph.getAnatEntityOntology().getElement(cond1.getAnatEntityId())).thenReturn(anatEntity1);
        when(condGraph.getAnatEntityOntology().getElement(cond2.getAnatEntityId())).thenReturn(anatEntity1);
        when(condGraph.getAnatEntityOntology().getElement(cond3.getAnatEntityId())).thenReturn(anatEntity2);
        when(condGraph.getAnatEntityOntology().getElement(cond4.getAnatEntityId())).thenReturn(anatEntity2);
        when(condGraph.getAnatEntityOntology().getElement(cond5.getAnatEntityId())).thenReturn(anatEntity2);
        Ontology<DevStage, String> stageOntology = mock(Ontology.class);
        when(condGraph.getDevStageOntology()).thenReturn(stageOntology);
        when(condGraph.getDevStageOntology().getElement(cond1.getDevStageId())).thenReturn(null);
        when(condGraph.getDevStageOntology().getElement(cond2.getDevStageId())).thenReturn(devStage1);
        when(condGraph.getDevStageOntology().getElement(cond3.getDevStageId())).thenReturn(devStage2);
        when(condGraph.getDevStageOntology().getElement(cond4.getDevStageId())).thenReturn(null);
        when(condGraph.getDevStageOntology().getElement(cond5.getDevStageId())).thenReturn(devStage1);


        OWLGraphWrapper wrapper = mock(OWLGraphWrapper.class);
        OntologyUtils ontUtils = mock(OntologyUtils.class);
        Uberon uberon = mock(Uberon.class);
        OWLClass cls = mock(OWLClass.class);
        when(ontUtils.getWrapper()).thenReturn(wrapper);
        when(uberon.getOntologyUtils()).thenReturn(ontUtils);
        when(wrapper.getOWLClassByIdentifier(anatEntity1.getId(), true)).thenReturn(cls);
        when(wrapper.getXref(cls)).thenReturn(Arrays.asList("BTO:0001", "NON_BTO:00001"));
        
        
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        
        GeneService geneService = mock(GeneService.class);
        when(serviceFactory.getGeneService()).thenReturn(geneService);

        when(geneService.loadGenes(new GeneFilter(spe1.getId())))
                .thenReturn(Stream.of(gene1, gene2))
                .thenReturn(Stream.of(gene1, gene2))
                .thenReturn(Stream.of(gene1, gene2))
                .thenReturn(Stream.of(gene1, gene2));
        when(geneService.loadGenes(new GeneFilter(spe2.getId())))
                .thenReturn(Stream.of(gene3))
                .thenReturn(Stream.of(gene3))
                .thenReturn(Stream.of(gene3))
                .thenReturn(Stream.of(gene3));
        
        SpeciesService speciesService = mock(SpeciesService.class);
        when(serviceFactory.getSpeciesService()).thenReturn(speciesService);
        when(speciesService.loadSpeciesByIds(null, false)).thenReturn(new HashSet<>(Arrays.asList(spe1, spe2)));
        when(speciesService.loadSpeciesByIds(new HashSet<>(Arrays.asList(spe1.getId())), false))
        .thenReturn(new HashSet<>(Arrays.asList(spe1)));
        when(speciesService.loadSpeciesByIds(new HashSet<>(Arrays.asList(spe2.getId())), false))
        .thenReturn(new HashSet<>(Arrays.asList(spe2)));
        
        OntologyService ontService = mock(OntologyService.class);
        when(serviceFactory.getOntologyService()).thenReturn(ontService);
        when(ontService.getAnatEntityOntology(any(Integer.class), any())).thenReturn(mock(Ontology.class));
        when(ontService.getDevStageOntology(any(Integer.class), any())).thenReturn(mock(Ontology.class));
        
        
        CallService callService = mock(CallService.class);
        when(serviceFactory.getCallService()).thenReturn(callService);
        
        Map<ExpressionSummary, SummaryQuality> summarySilverCallTypeQualityFilter = new HashMap<>();
        summarySilverCallTypeQualityFilter.put(ExpressionSummary.EXPRESSED, SummaryQuality.SILVER);
        Map<ExpressionSummary, SummaryQuality> summaryBronzeCallTypeQualityFilter = new HashMap<>();
        summaryBronzeCallTypeQualityFilter.put(ExpressionSummary.EXPRESSED, SummaryQuality.BRONZE);
        Map<CallType.Expression, Boolean> obsDataFilter = new HashMap<>();
        obsDataFilter.put(CallType.Expression.EXPRESSED, true);
        
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> anatServiceOrdering = 
                new LinkedHashMap<>();
        anatServiceOrdering.put(CallService.OrderingAttribute.GENE_ID, Service.Direction.ASC);
        anatServiceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.ASC);
        anatServiceOrdering.put(CallService.OrderingAttribute.ANAT_ENTITY_ID, Service.Direction.ASC);
        
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> condServiceOrdering = 
                new LinkedHashMap<>();
        condServiceOrdering.put(CallService.OrderingAttribute.GENE_ID, Service.Direction.ASC);
        condServiceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.ASC);
        condServiceOrdering.put(CallService.OrderingAttribute.ANAT_ENTITY_ID, Service.Direction.ASC);
        condServiceOrdering.put(CallService.OrderingAttribute.DEV_STAGE_ID, Service.Direction.ASC);
       
        Set<CallService.Attribute> attrsNoDev = EnumSet.of(
                CallService.Attribute.GENE, CallService.Attribute.ANAT_ENTITY_ID, 
                CallService.Attribute.GLOBAL_MEAN_RANK);
        Set<CallService.Attribute> attrsWithDev = EnumSet.of(
                CallService.Attribute.GENE, CallService.Attribute.ANAT_ENTITY_ID, 
                CallService.Attribute.DEV_STAGE_ID, CallService.Attribute.GLOBAL_MEAN_RANK);
        
        Supplier<Stream<ExpressionCall>> exprCallSupplier1 = () -> Stream.of(c1);
        Supplier<Stream<ExpressionCall>> exprCallSupplier24 = () -> Stream.of(c2, c4);
        Supplier<Stream<ExpressionCall>> exprCallSupplier235 = () -> Stream.of(c2, c3, c5);
        Supplier<Stream<ExpressionCall>> exprCallSupplier35 = () -> Stream.of(c3, c5);
        Supplier<Stream<ExpressionCall>> exprCallSupplier4 = () -> Stream.of(c4);
        Supplier<Stream<ExpressionCall>> exprCallSupplier6 = () -> Stream.of(c6);
        Supplier<Stream<ExpressionCall>> exprCallSupplier7 = () -> Stream.of(c7);
        
        //spe1, only anatEntity, all datatypes
        when(callService.loadExpressionCalls(eq(new ExpressionCallFilter(summarySilverCallTypeQualityFilter,
                Collections.singleton(new GeneFilter(spe1.getId())), null, null, obsDataFilter,
                true, null)), eq(attrsNoDev), eq(null)))
        .thenReturn(exprCallSupplier1.get());
        when(callService.loadExpressionCalls(eq(new ExpressionCallFilter(summaryBronzeCallTypeQualityFilter,
                Collections.singleton(new GeneFilter(spe1.getId())), null, null, obsDataFilter,
                true, true)), eq(attrsWithDev), eq(condServiceOrdering)))
        .thenReturn(exprCallSupplier24.get());
        //spe1, only anatEntity, affymetrix
        when(callService.loadExpressionCalls(eq(new ExpressionCallFilter(summarySilverCallTypeQualityFilter,
                Collections.singleton(new GeneFilter(spe1.getId())), null,
                Collections.singleton(DataType.AFFYMETRIX), obsDataFilter, true, null)), eq(attrsNoDev), eq(null)))
        .thenReturn(exprCallSupplier1.get());
        when(callService.loadExpressionCalls(eq(new ExpressionCallFilter(summaryBronzeCallTypeQualityFilter,
                Collections.singleton(new GeneFilter(spe1.getId())), null,
                Collections.singleton(DataType.AFFYMETRIX), obsDataFilter, true, true)), 
                eq(attrsWithDev), eq(condServiceOrdering)))
        .thenReturn(exprCallSupplier4.get());
        //spe2, only anatEntity, all datatypes
        when(callService.loadExpressionCalls(eq(new ExpressionCallFilter(summarySilverCallTypeQualityFilter,
                Collections.singleton(new GeneFilter(spe2.getId())), null, null, obsDataFilter,
                true, null)), eq(attrsNoDev), eq(null)))
        .thenReturn(exprCallSupplier6.get());
        when(callService.loadExpressionCalls(eq(new ExpressionCallFilter(summaryBronzeCallTypeQualityFilter,
                Collections.singleton(new GeneFilter(spe2.getId())), null, null, obsDataFilter,
                true, true)), eq(attrsWithDev), eq(condServiceOrdering)))
        .thenReturn(exprCallSupplier7.get());
        //spe2, only anatEntity, affymetrix
        when(callService.loadExpressionCalls(eq(new ExpressionCallFilter(summarySilverCallTypeQualityFilter,
                Collections.singleton(new GeneFilter(spe2.getId())), null,
                Collections.singleton(DataType.AFFYMETRIX), obsDataFilter, true, null)), eq(attrsNoDev), eq(null)))
        .thenReturn(exprCallSupplier6.get());
        when(callService.loadExpressionCalls(eq(new ExpressionCallFilter(summaryBronzeCallTypeQualityFilter,
                Collections.singleton(new GeneFilter(spe2.getId())), null, 
                Collections.singleton(DataType.AFFYMETRIX), obsDataFilter, true, true)), 
                eq(attrsWithDev), eq(condServiceOrdering)))
        .thenReturn(exprCallSupplier7.get());
        
        //spe1, anatEntity & stage, all datatypes
        when(callService.loadExpressionCalls(eq(new ExpressionCallFilter(summarySilverCallTypeQualityFilter,
                Collections.singleton(new GeneFilter(spe1.getId())), null, null, obsDataFilter,
                true, true)), eq(attrsWithDev), eq(condServiceOrdering)))
        .thenReturn(exprCallSupplier235.get());
        //spe1, anatEntity & stage, affymetrix
        when(callService.loadExpressionCalls(eq(new ExpressionCallFilter(summarySilverCallTypeQualityFilter,
                Collections.singleton(new GeneFilter(spe1.getId())), null,
                Collections.singleton(DataType.AFFYMETRIX), obsDataFilter, true, true)), 
                eq(attrsWithDev), eq(condServiceOrdering)))
        .thenReturn(exprCallSupplier35.get());
        //spe2, anatEntity & stage, all datatypes
        when(callService.loadExpressionCalls(eq(new ExpressionCallFilter(summarySilverCallTypeQualityFilter,
                Collections.singleton(new GeneFilter(spe2.getId())), null, null, obsDataFilter,
                true, true)), eq(attrsWithDev), eq(condServiceOrdering)))
        .thenReturn(exprCallSupplier7.get());
        //spe2, anatEntity & stage, affymetrix
        when(callService.loadExpressionCalls(eq(new ExpressionCallFilter(summarySilverCallTypeQualityFilter,
                Collections.singleton(new GeneFilter(spe2.getId())), null,
                Collections.singleton(DataType.AFFYMETRIX), obsDataFilter, true, true)), 
                eq(attrsWithDev), eq(condServiceOrdering)))
        .thenReturn(exprCallSupplier7.get());
       
        //*** Launch test ***
        GenerateRankFile generate = new GenerateRankFile(() -> serviceFactory, uberon, 
                ((conds, anatOnt, devOnt) -> condGraph));
        File folder1 = testFolder.newFolder("f1");
        File folder2 = testFolder.newFolder("f2");
        generate.generateRankFiles(Collections.singleton(1), false, 
                new HashSet<>(Arrays.asList(DataType.AFFYMETRIX, null)), folder1.getAbsolutePath());
        generate.generateRankFiles(Collections.singleton(2), false, 
                new HashSet<>(Arrays.asList(DataType.AFFYMETRIX, null)), folder1.getAbsolutePath());
        generate.generateRankFiles(Collections.singleton(1), true, 
                new HashSet<>(Arrays.asList(DataType.AFFYMETRIX, null)), folder2.getAbsolutePath());
        generate.generateRankFiles(Collections.singleton(2), true, 
                new HashSet<>(Arrays.asList(DataType.AFFYMETRIX, null)), folder2.getAbsolutePath());
        
        //species1
        this.checkFile(spe1, false, null, 
                Arrays.asList(
                        Arrays.asList("gene1", null, "anat1", "Anat name 1", "stage1", "Stage name 1", 
                                "1.25", "BTO:0001"), 
                        Arrays.asList("gene1", null, "anat2", "Anat name 2", "stage2", "Stage name 2", 
                                "8.00", null), 
                        Arrays.asList("gene2", null, "anat1", "Anat name 1", "stage1", "Stage name 1", 
                                "10000.00", "BTO:0001")), 
                folder1, "1_condition_all_data_my_species1.tsv");
        this.checkFile(spe1, true, null, 
                Arrays.asList(
                        Arrays.asList("gene1", null, "anat1", "Anat name 1", 
                                "1.25", "BTO:0001")), 
                folder2, "1_anat_entity_all_data_my_species1.tsv");
        this.checkFile(spe1, false, DataType.AFFYMETRIX, 
                Arrays.asList(
                        Arrays.asList("gene1", null, "anat2", "Anat name 2", "stage2", "Stage name 2", 
                                "8.00", null),
                        Arrays.asList("gene2", null, "anat1", "Anat name 1", "stage1", "Stage name 1", 
                                "10000.00", "BTO:0001")), 
                folder1, "1_condition_affymetrix_my_species1.tsv");
        this.checkFile(spe1, true, DataType.AFFYMETRIX, 
                Arrays.asList(
                        Arrays.asList("gene1", null, "anat1", "Anat name 1", 
                                "1.35", "BTO:0001")), 
                folder2, "1_anat_entity_affymetrix_my_species1.tsv");
        
        //species2
        this.checkFile(spe2, false, null, 
                Arrays.asList(
                        Arrays.asList("gene3", "gene3Name", "anat2", "Anat name 2", "stage1", "Stage name 1", 
                                "600.20", null)), 
                folder1, "2_condition_all_data_my_species2.tsv");
        this.checkFile(spe2, false, DataType.AFFYMETRIX, 
                Arrays.asList(
                        Arrays.asList("gene3", "gene3Name", "anat2", "Anat name 2", "stage1", "Stage name 1", 
                                "600.20", null)), 
                folder1, "2_condition_affymetrix_my_species2.tsv");
        this.checkFile(spe2, true, null, 
                Arrays.asList(
                        Arrays.asList("gene3", "gene3Name", "anat2", "Anat name 2", 
                                "600.20", null)), 
                folder2, "2_anat_entity_all_data_my_species2.tsv");
        this.checkFile(spe2, true, DataType.AFFYMETRIX, 
                Arrays.asList(
                        Arrays.asList("gene3", "gene3Name", "anat2", "Anat name 2", 
                                "600.20", null)), 
                folder2, "2_anat_entity_affymetrix_my_species2.tsv");
        File notExists = Paths.get(folder1.getAbsolutePath(), "spe2_byCondition_affymetrix_my_species2.tsv").toFile();
        assertFalse("File should not have been created: " + notExists.getName(), notExists.exists());
        notExists = Paths.get(folder2.getAbsolutePath(), "spe2_byAnatEntity_affymetrix_my_species2.tsv").toFile();
        assertFalse("File should not have been created: " + notExists.getName(), notExists.exists());
    }

    private void checkFile(Species species, boolean anatEntityOnly, DataType dataType, 
            List<List<String>> expectedValues, File folder, String expectedFileName) 
                    throws FileNotFoundException, IOException {
        log.entry(species, anatEntityOnly, dataType, expectedValues, folder, expectedFileName);
        
        File file = GenerateRankFile.getOutputFile(species, anatEntityOnly, dataType, 
                folder.getAbsolutePath(), false);
        log.debug("Checking file {}", file.getName());
        assertEquals("Incorrect generated file name", expectedFileName, file.getName());
        assertTrue("File not created: " + file.getName(), file.exists());
        
        try (ICsvListReader listReader = new CsvListReader(new FileReader(file), Utils.TSVCOMMENTED)) {
            String[] header = listReader.getHeader(true);
            assertArrayEquals("Incorrect header", GenerateRankFile.getFileHeader(anatEntityOnly, dataType), 
                    header);
            List<List<String>> allValues = new ArrayList<>();
            List<String> rowValues;
            while( (rowValues = listReader.read()) != null ) {
                allValues.add(rowValues);
            }
            assertEquals("Incorrect read values", expectedValues, allValues);
        }
        
        log.exit();
    }
}
