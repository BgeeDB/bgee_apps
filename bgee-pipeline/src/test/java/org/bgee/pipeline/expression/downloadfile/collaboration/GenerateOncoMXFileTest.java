package org.bgee.pipeline.expression.downloadfile.collaboration;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.baseelements.EntityMinMaxRanks;
import org.bgee.model.expressiondata.baseelements.ExpressionLevelCategory;
import org.bgee.model.expressiondata.baseelements.ExpressionLevelInfo;
import org.bgee.model.expressiondata.baseelements.QualitativeExpressionLevel;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.Condition;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.ontology.OntologyService;
import org.bgee.model.ontology.RelationType;
import org.bgee.model.source.Source;
import org.bgee.model.species.Species;
import org.bgee.model.species.SpeciesService;
import org.bgee.pipeline.TestAncestor;
import org.bgee.pipeline.Utils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;

/**
* Unit tests for {@link GenerateOncoMXFile}.
*
* @author  Frederic Bastian
* @since Bgee 14 Feb. 2019
* @version Bgee 14 Feb. 2019
*/
public class GenerateOncoMXFileTest extends TestAncestor {
    private final static Logger log = LogManager.getLogger(GenerateOncoMXFileTest.class.getName());
    @Override
    protected Logger getLogger() {
        return log;
    }

    private static final String UBERON_TERM_FILE = "/oncomx/human_doid_slim_uberon_mapping.csv";

    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder();


    /**
     * Test {@link GenerateOncoMXFile#generateFile(int, Collection, String, String)}.
     */
    @Test
    public void shouldGenerateOncoMXFile() throws IOException {
        //*****************************************
        // MOCK CREATION
        //*****************************************
        //*** Mock services ***
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        SpeciesService speciesService = mock(SpeciesService.class);
        when(serviceFactory.getSpeciesService()).thenReturn(speciesService);
        OntologyService ontologyService = mock(OntologyService.class);
        when(serviceFactory.getOntologyService()).thenReturn(ontologyService);
//        AnatEntityService anatEntityService = mock(AnatEntityService.class);
//        when(serviceFactory.getAnatEntityService()).thenReturn(anatEntityService);
//        DevStageService devStageService = mock(DevStageService.class);
//        when(serviceFactory.getDevStageService()).thenReturn(devStageService);
//        GeneService geneService = mock(GeneService.class);
//        when(serviceFactory.getGeneService()).thenReturn(geneService);
        CallService callService = mock(CallService.class);
        when(serviceFactory.getCallService()).thenReturn(callService);

        @SuppressWarnings("unchecked")
        Ontology<AnatEntity, String> anatEntOnt = mock(Ontology.class);
        @SuppressWarnings("unchecked")
        Ontology<DevStage, String> devStageOnt = mock(Ontology.class);

        //*** Create objects returned by the mock services ***
        Species human = new Species(9606, "human", "uberHuman", "Homo", "sapiens",
                "whateverGenome", new Source(1), 0, 9605, null, null, 1);

        //Ids need to be provided in alphabetical order for proper ordering of calls returned
        List<String> requestedUberonIds = Arrays.asList("UBERON:0000002", "UBERON:0000007");
        AnatEntity anatEntity1 = new AnatEntity(requestedUberonIds.get(0), "anatEntity1Name", "anat1");
        AnatEntity anatEntity2 = new AnatEntity(requestedUberonIds.get(1), "anatEntity2Name", "anat2");
        //We will consider this term as a descendant of a valid Uberon term
        //in human_doid_slim_uberon_mapping.csv, so that it will be valid as well
        AnatEntity anatEntity3 = new AnatEntity("child1", "anatEntity3Name", "anat3");
        Set<String> validAnatEntityIds = new HashSet<>(Arrays.asList(requestedUberonIds.get(0),
                requestedUberonIds.get(1), anatEntity3.getId()));

        List<String> requestedStageIds = Arrays.asList("devStage1", "devStage2");
        DevStage devStage1 = new DevStage(requestedStageIds.get(0), "devStage1Name", "stage1");
        DevStage devStage2 = new DevStage(requestedStageIds.get(1), "devStage2Name", "stage2");
        //We will consider this term as a descendant of a valid requested stage,
        //so that it will be valid as well
        DevStage devStage3 = new DevStage("stageChild1", "devStage3Name", "stage3");
        Set<String> validDevStageIds = new HashSet<>(Arrays.asList(requestedStageIds.get(0),
                requestedStageIds.get(1), devStage3.getId()));

        // *** Configure the calls to the mock services ***
        when(speciesService.loadSpeciesByIds(Collections.singleton(9606), false))
            .thenReturn(Collections.singleton(human));

        when(ontologyService.getAnatEntityOntology(9606, new HashSet<>(requestedUberonIds),
                EnumSet.of(RelationType.ISA_PARTOF), false, true))
            .thenReturn(anatEntOnt);
        when(anatEntOnt.getElement(requestedUberonIds.get(0))).thenReturn(anatEntity1);
        when(anatEntOnt.getDescendants(anatEntity1)).thenReturn(new HashSet<>());
        when(anatEntOnt.getElement(requestedUberonIds.get(1))).thenReturn(anatEntity2);
        when(anatEntOnt.getDescendants(anatEntity2)).thenReturn(new HashSet<>(
                Arrays.asList(anatEntity3)));
        when(anatEntOnt.getElement(anatEntity3.getId())).thenReturn(anatEntity3);
        when(anatEntOnt.getDescendants(anatEntity3)).thenReturn(new HashSet<>());


        when(ontologyService.getDevStageOntology(9606, new HashSet<>(requestedStageIds),
                false, true)).thenReturn(devStageOnt);
        when(devStageOnt.getElement(requestedStageIds.get(0))).thenReturn(devStage1);
        when(devStageOnt.getDescendants(devStage1)).thenReturn(new HashSet<>());
        when(devStageOnt.getElement(requestedStageIds.get(1))).thenReturn(devStage2);
        when(devStageOnt.getDescendants(devStage2)).thenReturn(new HashSet<>(Arrays.asList(devStage3)));
        when(devStageOnt.getElement(devStage3.getId())).thenReturn(devStage3);
        when(devStageOnt.getDescendants(devStage3)).thenReturn(new HashSet<>());


        Condition cond1_1 = new Condition(anatEntity1, devStage1, human);
        Condition cond1_2 = new Condition(anatEntity1, devStage2, human);
        Condition cond1_3 = new Condition(anatEntity1, devStage3, human);
        Condition cond2_1 = new Condition(anatEntity2, devStage1, human);
        Condition cond3_1 = new Condition(anatEntity3, devStage1, human);
        Condition cond3_2 = new Condition(anatEntity3, devStage2, human);
        Condition cond3_3 = new Condition(anatEntity3, devStage3, human);
        Gene gene1 = new Gene("gene1", "geneName1", "g1", null, null, human, new GeneBioType("biotype1"), 1);
        Gene gene2 = new Gene("gene2", "geneName2", "g2", null, null, human, new GeneBioType("biotype1"), 1);
        Gene gene3 = new Gene("gene3", "geneName3", "g3", null, null, human, new GeneBioType("biotype1"), 1);

        EntityMinMaxRanks<Gene> gene1MinMaxRanks = new EntityMinMaxRanks<>(
                new BigDecimal("1"), new BigDecimal("500"), gene1);
        EntityMinMaxRanks<Gene> gene2MinMaxRanks = new EntityMinMaxRanks<>(
                new BigDecimal("100"), new BigDecimal("1000"), gene2);
        EntityMinMaxRanks<Gene> gene3MinMaxRanks = new EntityMinMaxRanks<>(
                new BigDecimal("300"), new BigDecimal("300"), gene3);

        EntityMinMaxRanks<AnatEntity> anat1MinMaxRanks = new EntityMinMaxRanks<>(
                new BigDecimal("10.1"), new BigDecimal("300"), anatEntity1);
        EntityMinMaxRanks<AnatEntity> anat2MinMaxRanks = new EntityMinMaxRanks<>(
                new BigDecimal("1"), new BigDecimal("1"), anatEntity2);
        EntityMinMaxRanks<AnatEntity> anat3MinMaxRanks = new EntityMinMaxRanks<>(
                new BigDecimal("200"), new BigDecimal("1000"), anatEntity3);

        //*** Gene calls ***
        ExpressionCall gene1Call1 = new ExpressionCall(gene1, cond2_1, null, 
                ExpressionSummary.EXPRESSED, SummaryQuality.BRONZE, 
                null, new ExpressionLevelInfo(new BigDecimal("1"), new BigDecimal("1"),
                        new QualitativeExpressionLevel<>(
                                ExpressionLevelCategory.getExpressionLevelCategory(gene1MinMaxRanks,
                                        new BigDecimal("1")),
                                gene1MinMaxRanks),
                        new QualitativeExpressionLevel<>(
                                ExpressionLevelCategory.getExpressionLevelCategory(anat2MinMaxRanks,
                                        new BigDecimal("1")),
                                anat2MinMaxRanks)));
        ExpressionCall gene1Call2 = new ExpressionCall(gene1, cond1_1, null, 
                ExpressionSummary.EXPRESSED, SummaryQuality.BRONZE, 
                null, new ExpressionLevelInfo(new BigDecimal("10.1"), new BigDecimal("99.98319"),
                        new QualitativeExpressionLevel<>(
                                ExpressionLevelCategory.getExpressionLevelCategory(gene1MinMaxRanks,
                                        new BigDecimal("10.1")),
                                gene1MinMaxRanks),
                        new QualitativeExpressionLevel<>(
                                ExpressionLevelCategory.getExpressionLevelCategory(anat1MinMaxRanks,
                                        new BigDecimal("10.1")),
                                anat1MinMaxRanks)));
        ExpressionCall gene1Call3 = new ExpressionCall(gene1, cond3_1, null, 
                ExpressionSummary.EXPRESSED, SummaryQuality.BRONZE, 
                null, new ExpressionLevelInfo(new BigDecimal("200"), new BigDecimal("99.63242"),
                        new QualitativeExpressionLevel<>(
                                ExpressionLevelCategory.getExpressionLevelCategory(gene1MinMaxRanks,
                                        new BigDecimal("200")),
                                gene1MinMaxRanks),
                        new QualitativeExpressionLevel<>(
                                ExpressionLevelCategory.getExpressionLevelCategory(anat3MinMaxRanks,
                                        new BigDecimal("200")),
                                anat3MinMaxRanks)));
        //not expressed call, should not have been seen in the anat. entity calls
        ExpressionCall gene1Call4 = new ExpressionCall(gene1, cond3_2, null, 
                ExpressionSummary.NOT_EXPRESSED, SummaryQuality.SILVER, 
                null, new ExpressionLevelInfo(new BigDecimal("500"), new BigDecimal("99.07828"),
                        new QualitativeExpressionLevel<>(
                                ExpressionLevelCategory.getExpressionLevelCategory(gene1MinMaxRanks,
                                        new BigDecimal("500")),
                                gene1MinMaxRanks),
                        new QualitativeExpressionLevel<>(
                                ExpressionLevelCategory.getExpressionLevelCategory(anat3MinMaxRanks,
                                        new BigDecimal("500")),
                                anat3MinMaxRanks)));
        ExpressionCall gene2Call1 = new ExpressionCall(gene2, cond1_2, null, 
                ExpressionSummary.EXPRESSED, SummaryQuality.BRONZE, 
                null, new ExpressionLevelInfo(new BigDecimal("100"), new BigDecimal("99.81713"),
                        new QualitativeExpressionLevel<>(
                                ExpressionLevelCategory.getExpressionLevelCategory(gene2MinMaxRanks,
                                        new BigDecimal("100")),
                                gene2MinMaxRanks),
                        new QualitativeExpressionLevel<>(
                                ExpressionLevelCategory.getExpressionLevelCategory(anat1MinMaxRanks,
                                        new BigDecimal("100")),
                                anat1MinMaxRanks)));
        ExpressionCall gene2Call2 = new ExpressionCall(gene2, cond3_3, null, 
                ExpressionSummary.EXPRESSED, SummaryQuality.BRONZE, 
                null, new ExpressionLevelInfo(new BigDecimal("1000"), new BigDecimal("98.15472"),
                        new QualitativeExpressionLevel<>(
                                ExpressionLevelCategory.getExpressionLevelCategory(gene2MinMaxRanks,
                                        new BigDecimal("1000")),
                                gene2MinMaxRanks),
                        new QualitativeExpressionLevel<>(
                                ExpressionLevelCategory.getExpressionLevelCategory(anat3MinMaxRanks,
                                        new BigDecimal("1000")),
                                anat3MinMaxRanks)));
        ExpressionCall gene3Call1 = new ExpressionCall(gene3, cond1_3, null, 
                ExpressionSummary.EXPRESSED, SummaryQuality.BRONZE, 
                null, new ExpressionLevelInfo(new BigDecimal("300"), new BigDecimal("99.44771"),
                        new QualitativeExpressionLevel<>(
                                ExpressionLevelCategory.getExpressionLevelCategory(gene3MinMaxRanks,
                                        new BigDecimal("300")),
                                gene3MinMaxRanks),
                        new QualitativeExpressionLevel<>(
                                ExpressionLevelCategory.getExpressionLevelCategory(anat1MinMaxRanks,
                                        new BigDecimal("300")),
                                anat1MinMaxRanks)));

        List<ExpressionCall> geneCalls = Arrays.asList(gene1Call1, gene1Call2,
                gene1Call3, gene1Call4,
                gene2Call1, gene2Call2,
                gene3Call1);
        when(callService.loadExpressionCalls(
                GenerateOncoMXFile.getGeneCallFilter(9606, validAnatEntityIds, validDevStageIds),
                GenerateOncoMXFile.getGeneCallAttributes(),
                GenerateOncoMXFile.getGeneServiceOrdering()
        )).thenReturn(geneCalls.stream());

        //*****************************************
        // ACTUAL TEST
        //*****************************************
        File createdFolder= testFolder.newFolder("subfolder");
        GenerateOncoMXFile generateFileTest = new GenerateOncoMXFile(serviceFactory);
        generateFileTest.generateFile(9606, requestedStageIds,
                getClass().getResource(UBERON_TERM_FILE).getFile(),
                createdFolder.getPath());

        //Read the generated file to check it's correct
        String fileName = "Homo_sapiens_devStage1_devStage2_RNA_SEQ.tsv";
        File file = new File(createdFolder.getPath(), fileName);
        assertTrue("File  not generated", file.exists());

        final CellProcessor[] processors = new CellProcessor[] { 
                new StrNotNullOrEmpty(), // gene ID
                new NotNull(), // gene name
                new StrNotNullOrEmpty(), // anat. entity ID
                new StrNotNullOrEmpty(), // anat. entity name
                new StrNotNullOrEmpty(), // dev. stage ID
                new StrNotNullOrEmpty(), // dev. stage name
                new StrNotNullOrEmpty(), // gene expression cat.
                new StrNotNullOrEmpty(), // anat. entity expression cat.
                new StrNotNullOrEmpty(), // call qual.
                new StrNotNullOrEmpty(), // rank score
                new StrNotNullOrEmpty()  // expression score
        };
        List<List<Object>> allLines = new ArrayList<>();
        String[] headers;
        try (ICsvListReader listReader = new CsvListReader(new FileReader(file),
                Utils.TSVCOMMENTED)) {
            List<Object>  rowList;
            headers = listReader.getHeader(true);
            while ((rowList = listReader.read(processors)) != null) {
                allLines.add(rowList);
            }
        }
        String[] expectedHeaders = GenerateOncoMXFile.getHeader();
        assertArrayEquals("Incorrect header", expectedHeaders, headers);
        List<List<String>> expectedLines = geneCalls.stream().map(c -> Arrays.asList(
                c.getGene().getEnsemblGeneId(), c.getGene().getName(),
                c.getCondition().getAnatEntity().getId(), c.getCondition().getAnatEntity().getName(),
                c.getCondition().getDevStage().getId(), c.getCondition().getDevStage().getName(),
                c.getExpressionLevelInfo().getQualExprLevelRelativeToGene().getExpressionLevelCategory().toString(),
                c.getExpressionLevelInfo().getQualExprLevelRelativeToAnatEntity().getExpressionLevelCategory().toString(),
                c.getSummaryQuality().toString(), c.getFormattedMeanRank(), c.getFormattedExpressionScore()))
                .collect(Collectors.toList());
        assertEquals("Inccorect data written in file", expectedLines, allLines);
    }
}
