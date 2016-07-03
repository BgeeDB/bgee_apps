package org.bgee.pipeline.expression.downloadfile;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
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
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service.Direction;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.CallService.OrderingAttribute;
import org.bgee.model.expressiondata.Condition;
import org.bgee.model.expressiondata.ConditionUtils;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.CallType.Expression;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.gene.Gene;
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

import owltools.graph.OWLGraphWrapper;

/**
 * Unit tests for {@link GenerateRankFile}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 July 2016
 * @since Bgee 13 July 2016
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
        
        //*** create objects require for test ***
        AnatEntity anatEntity1 = new AnatEntity("anat1", "Anat name 1", null);
        AnatEntity anatEntity2 = new AnatEntity("anat2", "Anat name 2", null);
        DevStage devStage1 = new DevStage("stage1", "Stage name 1", null);
        DevStage devStage2 = new DevStage("stage2", "Stage name 2", null);
        Condition cond1 = new Condition(anatEntity1.getId(), devStage1.getId());
        Condition cond2 = new Condition(anatEntity2.getId(), devStage2.getId());
        Species spe1 = new Species("spe1", "my species1", null, "my", "species1", null);
        Species spe2 = new Species("spe2", "my species2", null, "my", "species2", null);
        Gene gene1 = new Gene("gene1", spe1.getId(), "gene 1");
        Gene gene2 = new Gene("gene2", spe1.getId(), "gene 2");
        Gene gene3 = new Gene("gene3", spe2.getId(), "gene 3");
        ExpressionCall c1 = new ExpressionCall(gene1.getId(), cond1, new DataPropagation(), 
                ExpressionSummary.EXPRESSED, DataQuality.LOW, 
                Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX), 
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST)), 
                new BigDecimal("1.25"));
        ExpressionCall c2 = new ExpressionCall(gene2.getId(), cond2, new DataPropagation(), 
                ExpressionSummary.EXPRESSED, DataQuality.HIGH, 
                Arrays.asList( 
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST), 
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU)), 
                new BigDecimal("1.25"));
        ExpressionCall c3 = new ExpressionCall(gene2.getId(), cond1, new DataPropagation(), 
                ExpressionSummary.EXPRESSED, DataQuality.HIGH, 
                Arrays.asList(
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.AFFYMETRIX), 
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST), 
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU), 
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ)), 
                new BigDecimal("10"));
        ExpressionCall c4 = new ExpressionCall(gene3.getId(), cond2, new DataPropagation(), 
                ExpressionSummary.EXPRESSED, DataQuality.HIGH, 
                Arrays.asList( 
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.LOW, DataType.EST), 
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.IN_SITU), 
                        new ExpressionCallData(Expression.EXPRESSED, DataQuality.HIGH, DataType.RNA_SEQ)), 
                new BigDecimal("10"));
        
        
        //*** Mock services and utils ***
        ConditionUtils condUtils = mock(ConditionUtils.class);
        when(condUtils.getAnatEntity(cond1)).thenReturn(anatEntity1);
        when(condUtils.getAnatEntity(anatEntity1.getId())).thenReturn(anatEntity1);
        when(condUtils.getAnatEntity(cond2)).thenReturn(anatEntity2);
        when(condUtils.getAnatEntity(anatEntity2.getId())).thenReturn(anatEntity2);
        when(condUtils.getDevStage(cond1)).thenReturn(devStage1);
        when(condUtils.getDevStage(devStage1.getId())).thenReturn(devStage1);
        when(condUtils.getDevStage(cond2)).thenReturn(devStage2);
        when(condUtils.getDevStage(devStage2.getId())).thenReturn(devStage2);
        
        ExpressionCall.RankComparator comparator = mock(ExpressionCall.RankComparator.class);
        when(comparator.compare(c1, c1)).thenReturn(0);
        when(comparator.compare(c2, c2)).thenReturn(0);
        when(comparator.compare(c3, c3)).thenReturn(0);
        when(comparator.compare(c4, c4)).thenReturn(0);
        when(comparator.compare(c1, c2)).thenReturn(-1);
        when(comparator.compare(c2, c1)).thenReturn(1);
        when(comparator.compare(c1, c3)).thenReturn(-1);
        when(comparator.compare(c3, c1)).thenReturn(1);
        when(comparator.compare(c2, c3)).thenReturn(-1);
        when(comparator.compare(c3, c2)).thenReturn(1);
        
        BiFunction<List<ExpressionCall>, ConditionUtils, Set<ExpressionCall>> redundantCallsFuncSupplier = 
                (list, utils) -> list.contains(c3)? new HashSet<>(Arrays.asList(c3)): new HashSet<>();

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
        when(geneService.loadGenesByIdsAndSpeciesIds(null, Arrays.asList(spe1.getId())))
                .thenReturn(Arrays.asList(gene1, gene2));
        when(geneService.loadGenesByIdsAndSpeciesIds(null, Arrays.asList(spe2.getId())))
                .thenReturn(Arrays.asList(gene3));
        
        SpeciesService speciesService = mock(SpeciesService.class);
        when(serviceFactory.getSpeciesService()).thenReturn(speciesService);
        when(speciesService.loadSpeciesByIds(null)).thenReturn(new HashSet<>(Arrays.asList(spe1, spe2)));
        when(speciesService.loadSpeciesByIds(new HashSet<>(Arrays.asList(spe1.getId()))))
        .thenReturn(new HashSet<>(Arrays.asList(spe1)));
        when(speciesService.loadSpeciesByIds(new HashSet<>(Arrays.asList(spe2.getId()))))
        .thenReturn(new HashSet<>(Arrays.asList(spe2)));
        
        OntologyService ontService = mock(OntologyService.class);
        when(serviceFactory.getOntologyService()).thenReturn(ontService);
        when(ontService.getAnatEntityOntology(any(), any(), any())).thenReturn(mock(Ontology.class));
        when(ontService.getDevStageOntology(any(), any(), any())).thenReturn(mock(Ontology.class));
        
        
        CallService callService = mock(CallService.class);
        when(serviceFactory.getCallService()).thenReturn(callService);
        Set<CallService.Attribute> attrsNoDev = EnumSet.of(
                CallService.Attribute.GENE_ID, CallService.Attribute.ANAT_ENTITY_ID, 
                CallService.Attribute.CALL_DATA, CallService.Attribute.GLOBAL_DATA_QUALITY, 
                CallService.Attribute.GLOBAL_RANK);
        Set<CallService.Attribute> attrsWithDev = EnumSet.of(
                CallService.Attribute.GENE_ID, CallService.Attribute.ANAT_ENTITY_ID, 
                CallService.Attribute.CALL_DATA, CallService.Attribute.GLOBAL_DATA_QUALITY, 
                CallService.Attribute.GLOBAL_RANK, CallService.Attribute.DEV_STAGE_ID);
        
        when(callService.loadExpressionCalls(eq(spe1.getId()), 
                eq(new ExpressionCallFilter(new ExpressionCallData(Expression.EXPRESSED, null))), 
                eq(attrsNoDev), (LinkedHashMap<OrderingAttribute, Direction>) anyObject()))
        .thenReturn(Stream.of(c1, c2, c3));
        when(callService.loadExpressionCalls(eq(spe1.getId()), 
                eq(new ExpressionCallFilter(new ExpressionCallData(Expression.EXPRESSED, DataType.AFFYMETRIX))), 
                eq(attrsNoDev), (LinkedHashMap<OrderingAttribute, Direction>) anyObject()))
        .thenReturn(Stream.of(c1, c3));
        when(callService.loadExpressionCalls(eq(spe2.getId()), 
                eq(new ExpressionCallFilter(new ExpressionCallData(Expression.EXPRESSED, null))), 
                eq(attrsNoDev), (LinkedHashMap<OrderingAttribute, Direction>) anyObject()))
        .thenReturn(Stream.of(c4));
        when(callService.loadExpressionCalls(eq(spe2.getId()), 
                eq(new ExpressionCallFilter(new ExpressionCallData(Expression.EXPRESSED, DataType.AFFYMETRIX))), 
                eq(attrsNoDev), (LinkedHashMap<OrderingAttribute, Direction>) anyObject()))
        .thenReturn(Stream.of());
        when(callService.loadExpressionCalls(eq(spe1.getId()), 
                eq(new ExpressionCallFilter(new ExpressionCallData(Expression.EXPRESSED, null))), 
                eq(attrsWithDev), (LinkedHashMap<OrderingAttribute, Direction>) anyObject()))
        .thenReturn(Stream.of(c1, c2, c3));
        when(callService.loadExpressionCalls(eq(spe1.getId()), 
                eq(new ExpressionCallFilter(new ExpressionCallData(Expression.EXPRESSED, DataType.AFFYMETRIX))), 
                eq(attrsWithDev), (LinkedHashMap<OrderingAttribute, Direction>) anyObject()))
        .thenReturn(Stream.of(c1, c3));
        when(callService.loadExpressionCalls(eq(spe2.getId()), 
                eq(new ExpressionCallFilter(new ExpressionCallData(Expression.EXPRESSED, null))), 
                eq(attrsWithDev), (LinkedHashMap<OrderingAttribute, Direction>) anyObject()))
        .thenReturn(Stream.of(c4));
        when(callService.loadExpressionCalls(eq(spe2.getId()), 
                eq(new ExpressionCallFilter(new ExpressionCallData(Expression.EXPRESSED, DataType.AFFYMETRIX))), 
                eq(attrsWithDev), (LinkedHashMap<OrderingAttribute, Direction>) anyObject()))
        .thenReturn(Stream.of());
        
        
        //*** Launch test ***
        GenerateRankFile generate = new GenerateRankFile(serviceFactory, uberon, 
                ((speId, conds, anatOnt, devOnt) -> condUtils), 
                (cu -> comparator), redundantCallsFuncSupplier);
        File folder1 = testFolder.newFolder("f1");
        File folder2 = testFolder.newFolder("f2");
        generate.generateRankFiles(null, false, new HashSet<>(Arrays.asList(null, DataType.AFFYMETRIX)), 
                folder1.getAbsolutePath());
        generate.generateRankFiles(null, true, new HashSet<>(Arrays.asList(null, DataType.AFFYMETRIX)), 
                folder2.getAbsolutePath());
        
        //species1
        this.checkFile(spe1, false, null, 
                Arrays.asList(
                        Arrays.asList("gene1", "gene 1", "anat1", "Anat name 1", "stage1", "Stage name 1", 
                                "1.25", "T", "T", "F", "F", "F", "BTO:0001"), 
                        Arrays.asList("gene2", "gene 2", "anat2", "Anat name 2", "stage2", "Stage name 2", 
                                "1.25", "F", "T", "T", "F", "F", null), 
                        Arrays.asList("gene2", "gene 2", "anat1", "Anat name 1", "stage1", "Stage name 1", 
                                "10.00", "T", "T", "T", "T", "T", "BTO:0001")), 
                folder1, "spe1_byCondition_allData_my_species1.tsv");
        this.checkFile(spe1, true, null, 
                Arrays.asList(
                        Arrays.asList("gene1", "gene 1", "anat1", "Anat name 1", 
                                "1.25", "T", "T", "F", "F", "F", "BTO:0001"), 
                        Arrays.asList("gene2", "gene 2", "anat2", "Anat name 2", 
                                "1.25", "F", "T", "T", "F", "F", null), 
                        Arrays.asList("gene2", "gene 2", "anat1", "Anat name 1", 
                                "10.00", "T", "T", "T", "T", "T", "BTO:0001")), 
                folder2, "spe1_byAnatEntity_allData_my_species1.tsv");
        this.checkFile(spe1, false, DataType.AFFYMETRIX, 
                Arrays.asList(
                        Arrays.asList("gene1", "gene 1", "anat1", "Anat name 1", "stage1", "Stage name 1", 
                                "1.25", "F", "BTO:0001"), 
                        Arrays.asList("gene2", "gene 2", "anat1", "Anat name 1", "stage1", "Stage name 1", 
                                "10.00", "T", "BTO:0001")), 
                folder1, "spe1_byCondition_affymetrix_my_species1.tsv");
        this.checkFile(spe1, true, DataType.AFFYMETRIX, 
                Arrays.asList(
                        Arrays.asList("gene1", "gene 1", "anat1", "Anat name 1", 
                                "1.25", "F", "BTO:0001"), 
                        Arrays.asList("gene2", "gene 2", "anat1", "Anat name 1", 
                                "10.00", "T", "BTO:0001")), 
                folder2, "spe1_byAnatEntity_affymetrix_my_species1.tsv");
        
        //species2
        this.checkFile(spe2, false, null, 
                Arrays.asList(
                        Arrays.asList("gene3", "gene 3", "anat2", "Anat name 2", "stage2", "Stage name 2", 
                                "10.00", "F", "T", "T", "T", "F", null)), 
                folder1, "spe2_byCondition_allData_my_species2.tsv");
        this.checkFile(spe2, true, null, 
                Arrays.asList(
                        Arrays.asList("gene3", "gene 3", "anat2", "Anat name 2", 
                                "10.00", "F", "T", "T", "T", "F", null)), 
                folder2, "spe2_byAnatEntity_allData_my_species2.tsv");
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
                folder.getAbsolutePath());
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
