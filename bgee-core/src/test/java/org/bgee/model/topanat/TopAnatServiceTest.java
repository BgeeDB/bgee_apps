package org.bgee.model.topanat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import org.bgee.model.BgeeProperties;
import org.bgee.model.ComposedEntity;
import org.bgee.model.ServiceFactory;
import org.bgee.model.TestAncestor;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneBioTypeTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneBioTypeTOResultSet;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTOResultSet;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.expressiondata.call.Condition2;
import org.bgee.model.species.Species;
import org.bgee.model.topanat.TopAnatService.Background;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit tests for {@link TopAnatService}, of the columns of the generated files: topAnat
 * reads them by name, so a change of their number, their name or their order breaks it. And of
 * the content of the files describing the conditions, on a small graph, of the reading of
 * the call files, on a small file, and of the genes of the backgrounds, on a species with
 * a protein coding gene, a pseudogene targeted by the polyA libraries, and a lncRNA, which they
 * do not target.
 * <p>
 * And of the running of the analyses, checking that one analysis is run for every
 * {@code TopAnatParams} provided, that the exception thrown by RCaller in case of empty result
 * is correctly handled, and that the method for testing whether the analyses are completed works
 * fine. These tests are ignored until they are reenabled.
 *
 * @author  Julien Wollbrett
 * @author  Mathieu Seppey
 * @version Bgee 16
 */
public class TopAnatServiceTest extends TestAncestor {

    /**
     * Test {@link TopAnatService#dataTypeCombinations()}.
     */
    @Test
    public void shouldGetDataTypeCombinations() {
        List<EnumSet<DataType>> combinations = TopAnatService.dataTypeCombinations();

        //Every non-empty combination of the data types, so 2^n - 1 of them
        assertEquals("Incorrect number of data type combinations",
                (1 << DataType.values().length) - 1, combinations.size());
        assertEquals("The combinations should be distinct",
                combinations.size(), combinations.stream().distinct().count());
        assertEquals("The combination of all data types should be the last one",
                EnumSet.allOf(DataType.class), combinations.get(combinations.size() - 1));
        //Ordered by increasing number of data types
        for (int i = 1; i < combinations.size(); i++) {
            assertEquals("The combinations should be ordered by increasing size", true,
                    combinations.get(i - 1).size() <= combinations.get(i).size());
        }
    }

    /**
     * Test {@link TopAnatService#combinationColumnName(EnumSet)}.
     */
    @Test
    public void shouldGetCombinationColumnName() {
        assertEquals("Incorrect column name of a single data type",
                "RNA_SEQ",
                TopAnatService.combinationColumnName(EnumSet.of(DataType.RNA_SEQ)));
        //The separator must not be one that the names of the data types contain, otherwise
        //a combination could not be read back unambiguously
        assertEquals("Incorrect column name of a combination of data types",
                "SC_RNA_SEQ-RNA_SEQ",
                TopAnatService.combinationColumnName(
                        EnumSet.of(DataType.SC_RNA_SEQ, DataType.RNA_SEQ)));
        assertEquals("The column names of the combinations should all be distinct",
                TopAnatService.dataTypeCombinations().size(),
                TopAnatService.dataTypeCombinations().stream()
                        .map(TopAnatService::combinationColumnName)
                        .collect(Collectors.toSet()).size());
    }

    private final static AnatEntity ROOT = new AnatEntity(ConditionDAO.ANAT_ENTITY_ROOT_ID,
            "anatomical entity", null);
    private final static AnatEntity BRAIN = new AnatEntity("UBERON:0000955", "brain", null);
    private final static AnatEntity CELL_TYPE_ROOT = new AnatEntity(
            ConditionDAO.CELL_TYPE_ROOT_ID, "cellular_component", null);
    private final static AnatEntity NEURON = new AnatEntity("CL:0000540", "neuron", null);
    private final static AnatEntity CELL = new AnatEntity("CL:0000000", "cell", null);

    /**
     * @return  A condition of topAnat built as the conditions loaded from the database:
     *          the cell type first, then the anat. entity.
     */
    private static Condition2 condition(AnatEntity anatEntity, AnatEntity cellType) {
        LinkedHashSet<AnatEntity> entities = new LinkedHashSet<>();
        if (cellType != null) {
            entities.add(cellType);
        }
        entities.add(anatEntity);
        return new Condition2(Map.of(ConditionParameter.ANAT_ENTITY_CELL_TYPE,
                new ComposedEntity<>(entities, AnatEntity.class)), new Species(1));
    }
    /**
     * @return  The conditions of a small graph, by internal ID: the root, the brain, the neurons,
     *          and the neurons of the brain.
     */
    private static Map<Integer, Condition2> conditions() {
        return Map.of(1, condition(ROOT, CELL_TYPE_ROOT), 2, condition(BRAIN, CELL_TYPE_ROOT),
                3, condition(ROOT, NEURON), 4, condition(BRAIN, NEURON));
    }
    /**
     * @return  The direct ancestors of the conditions of {@link #conditions()}, plus those of
     *          a condition of another combination of condition parameters.
     */
    private static Map<Integer, int[]> directAncestors() {
        return Map.of(1, new int[0], 2, new int[] {1}, 3, new int[] {1}, 4, new int[] {2, 3},
                5, new int[] {4});
    }

    /**
     * Test {@link TopAnatService#conditionLines(java.util.Collection)}.
     */
    @Test
    public void shouldGetConditionLines() {
        assertEquals("Incorrect lines of the condition file", List.of(
                "BGEE:0000000\tanatomical entity\tBGEE:0000000\tGO:0005575",
                "CL:0000540-BGEE:0000000\tneuron in anatomical entity\tBGEE:0000000\tCL:0000540",
                "CL:0000540-UBERON:0000955\tneuron in brain\tUBERON:0000955\tCL:0000540",
                "UBERON:0000955\tbrain\tUBERON:0000955\tGO:0005575"),
                TopAnatService.conditionLines(conditions().values()));

        //A cell type that is the anat. entity itself is held as a single entity by the condition,
        //it must not be confused with the anat. entity alone
        assertEquals("Incorrect lines of a cell type that is the anat. entity itself", List.of(
                "CL:0000000\tcell\tCL:0000000\tGO:0005575",
                "CL:0000000-CL:0000000\tcell in cell\tCL:0000000\tCL:0000000"),
                TopAnatService.conditionLines(
                        List.of(condition(CELL, CELL_TYPE_ROOT), condition(CELL, CELL))));

        assertThrows("Two conditions with the same ID should be rejected",
                IllegalStateException.class, () -> TopAnatService.conditionLines(List.of(
                        condition(BRAIN, CELL_TYPE_ROOT), condition(BRAIN, CELL_TYPE_ROOT))));
    }

    /**
     * Test {@link TopAnatService#relationLines(Map, Map)}.
     */
    @Test
    public void shouldGetRelationLines() {
        //The ancestors of the condition of another combination are ignored
        assertEquals("Incorrect lines of the relation file", List.of(
                "CL:0000540-BGEE:0000000\tBGEE:0000000",
                "CL:0000540-UBERON:0000955\tCL:0000540-BGEE:0000000",
                "CL:0000540-UBERON:0000955\tUBERON:0000955",
                "UBERON:0000955\tBGEE:0000000"),
                TopAnatService.relationLines(conditions(), directAncestors()));

        assertThrows("A parent that is not one of the conditions should be rejected",
                IllegalStateException.class, () -> TopAnatService.relationLines(
                        Map.of(2, condition(BRAIN, CELL_TYPE_ROOT)), directAncestors()));
        assertThrows("Conditions with several roots should be rejected",
                IllegalStateException.class, () -> TopAnatService.relationLines(
                        Map.of(1, condition(ROOT, CELL_TYPE_ROOT),
                                2, condition(BRAIN, CELL_TYPE_ROOT)),
                        Map.of(1, new int[0], 2, new int[0])));
    }

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    /**
     * The conditions of the call file of the tests, in the order of their indexes.
     */
    private final static List<String> CALL_FILE_CONDITIONS = List.of("CL:0000540-UBERON:0000955",
            "UBERON:0000955", "UBERON:0002107");
    /**
     * @return  The {@code Path} of a small call file. Only two combinations of data types:
     *          they are located by name.
     */
    private Path callFile() {
        Path callFile = this.tmpFolder.getRoot().toPath().resolve("calls.tsv.gz");
        TopAnatCallFile.CallFileWriter writer = new TopAnatCallFile.CallFileWriter(callFile,
                List.of("RNA_SEQ", "IN_SITU-RNA_SEQ"), CALL_FILE_CONDITIONS);
        writer.write("UBERON:0000104", "G1", entry(1, SummaryQuality.GOLD, SummaryQuality.SILVER));
        writer.write("UBERON:0000092", "G1", entry(1, SummaryQuality.SILVER, null));
        writer.write("UBERON:0000104", "G2", entry(0, SummaryQuality.SILVER, SummaryQuality.GOLD));
        writer.write("UBERON:0000092", "G3", entry(2, null, SummaryQuality.SILVER));
        writer.complete();
        return callFile;
    }
    /**
     * @return  The entry of one condition in a call file, with the quality of each combination
     *          of data types, {@code null} when it supports no call.
     */
    private static StringBuilder entry(int conditionIndex, SummaryQuality... qualities) {
        StringBuilder letters = new StringBuilder();
        for (SummaryQuality quality: qualities) {
            letters.append(TopAnatCallFile.qualityLetter(quality));
        }
        StringBuilder entries = new StringBuilder();
        TopAnatCallFile.appendEntry(entries, conditionIndex, letters);
        return entries;
    }

    /**
     * Test {@link TopAnatCallFile.CallFileWriter}: one gzip member per stage, in the order of
     * the stages, a gene being written once per stage, and its index.
     */
    @Test
    public void shouldWriteCallFile() throws IOException {
        //The conditions of a gene at a stage, separated by commas
        StringBuilder entries = entry(1, SummaryQuality.GOLD, null);
        TopAnatCallFile.appendEntry(entries, 2, "SN");
        assertEquals("Incorrect conditions of a gene at a stage", "1:GN,2:SN", entries.toString());

        Path callFile = this.callFile();

        //Read whole, as zcat would, the members being decompressed in sequence
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new GZIPInputStream(Files.newInputStream(callFile)), StandardCharsets.UTF_8))) {
            assertEquals("Incorrect content of the call file", List.of(
                    "UBERON:0000092\tG1\t1:SN",
                    "UBERON:0000092\tG3\t2:NS",
                    "UBERON:0000104\tG1\t1:GS",
                    "UBERON:0000104\tG2\t0:SG"),
                    reader.lines().collect(Collectors.toList()));
        }

        Path indexFile = this.tmpFolder.getRoot().toPath().resolve("calls.index.tsv");
        List<String> index = Files.readAllLines(indexFile, StandardCharsets.UTF_8);
        assertEquals("Incorrect size in the index", "size\t" + Files.size(callFile), index.get(0));
        assertEquals("Incorrect combinations in the index",
                "combinations\tRNA_SEQ\tIN_SITU-RNA_SEQ", index.get(1));
        //The members follow each other in the order of the stages
        String[] firstStage = index.get(2).split("\t");
        String[] secondStage = index.get(3).split("\t");
        assertEquals("Incorrect first stage", List.of("stage", "UBERON:0000092", "0"),
                List.of(firstStage).subList(0, 3));
        assertEquals("Incorrect second stage", List.of("stage", "UBERON:0000104", firstStage[3]),
                List.of(secondStage).subList(0, 3));
        assertEquals("Incorrect end of the last member", Files.size(callFile),
                Long.parseLong(secondStage[2]) + Long.parseLong(secondStage[3]));
        assertEquals("Incorrect conditions in the index", List.of(
                "condition\t0\tCL:0000540-UBERON:0000955", "condition\t1\tUBERON:0000955",
                "condition\t2\tUBERON:0002107"), index.subList(4, index.size()));
        assertFalse("The temporary members should have been deleted", Files.exists(
                this.tmpFolder.getRoot().toPath().resolve("calls.tsv.gz.members.tmp")));
        assertEquals("Incorrect index file of a call file", indexFile,
                TopAnatCallFile.indexFile(callFile));
        assertEquals("Incorrect path of an index file",
                Paths.get("root", TopAnatService.CALL_FILE_DIRECTORY,
                        "topanat_calls_propagated_species_1.index.tsv"),
                TopAnatService.getCallIndexFilePath("root",
                        TopAnatService.CallFileType.PROPAGATED, 1));
    }

    /**
     * Test {@link TopAnatService#subsetCalls(Path, EnumSet, SummaryQuality, String)}.
     */
    @Test
    public void shouldSubsetCalls() throws IOException {
        Path callFile = this.callFile();

        //No stage requested: the calls of the root of the stages, G3 only holding a call
        //at another stage
        assertEquals("Incorrect calls of at least SILVER quality with no stage requested",
                List.of(List.of("G1", "UBERON:0000955"),
                        List.of("G2", "CL:0000540-UBERON:0000955")),
                subsetCalls(callFile, EnumSet.of(DataType.IN_SITU, DataType.RNA_SEQ),
                        SummaryQuality.SILVER, null));
        assertEquals("Incorrect calls of GOLD quality", List.of(List.of("G1", "UBERON:0000955")),
                subsetCalls(callFile, EnumSet.of(DataType.RNA_SEQ), SummaryQuality.GOLD, null));
        assertEquals("Incorrect calls at one stage", List.of(List.of("G3", "UBERON:0002107")),
                subsetCalls(callFile, EnumSet.of(DataType.IN_SITU, DataType.RNA_SEQ),
                        SummaryQuality.SILVER, "UBERON:0000092"));

        assertEquals("A stage with no call should return no call", List.of(),
                subsetCalls(callFile, EnumSet.of(DataType.RNA_SEQ), SummaryQuality.SILVER,
                        "UBERON:0000066"));

        assertThrows("A combination of data types missing from the file should be rejected",
                IllegalStateException.class, () -> subsetCalls(callFile,
                        EnumSet.of(DataType.IN_SITU), SummaryQuality.SILVER, null));
        //A call file that does not match its index, for instance while it is being replaced
        Files.write(callFile, new byte[] {0}, StandardOpenOption.APPEND);
        assertThrows("A call file not matching its index should be rejected",
                IllegalStateException.class, () -> subsetCalls(callFile,
                        EnumSet.of(DataType.RNA_SEQ), SummaryQuality.SILVER, null));
    }
    private static List<List<String>> subsetCalls(Path callFile, EnumSet<DataType> dataTypes,
            SummaryQuality quality, String stageId) throws IOException {
        try (Stream<String[]> calls = TopAnatService.subsetCalls(callFile, dataTypes,
                quality, stageId)) {
            return calls.map(Arrays::asList).collect(Collectors.toList());
        }
    }

    private final static int SPECIES_ID = 1;

    private final static GeneBioTypeTO PROTEIN_CODING = new GeneBioTypeTO(1, "protein_coding");
    private final static GeneBioTypeTO LNC_RNA = new GeneBioTypeTO(2, "lncRNA");
    private final static GeneBioTypeTO PSEUDOGENE = new GeneBioTypeTO(3, "processed_pseudogene");

    private static GeneTO gene(int bgeeGeneId, String geneId, GeneBioTypeTO bioType) {
        return new GeneTO(bgeeGeneId, geneId, null, null, SPECIES_ID, bioType.getId(), true,
                null, null, 1, null);
    }
    private final static GeneTO CODING_1 = gene(1, "G1", PROTEIN_CODING);
    private final static GeneTO LNC = gene(2, "G2", LNC_RNA);
    private final static GeneTO PSEUDO = gene(3, "G3", PSEUDOGENE);
    private final static GeneTO CODING_2 = gene(4, "G4", PROTEIN_CODING);

    /**
     * Configure the {@code GeneDAO} to return the genes of the backgrounds.
     */
    private void mockBackgroundGenes() {
        //A result set can only be read once: a new one for each call
        when(this.geneDAO.getGenesBySpeciesIds(Set.of(SPECIES_ID))).thenAnswer(
                invocation -> getMockResultSet(GeneTOResultSet.class,
                        List.of(CODING_2, LNC, PSEUDO, CODING_1)));
        when(this.geneDAO.getGeneBioTypes()).thenAnswer(
                invocation -> getMockResultSet(GeneBioTypeTOResultSet.class,
                        List.of(PROTEIN_CODING, LNC_RNA, PSEUDOGENE)));
        when(this.geneDAO.getGeneBioTypesByPopulationCaptureIds(Set.of("polyA"))).thenAnswer(
                invocation -> getMockResultSet(GeneBioTypeTOResultSet.class,
                        List.of(PROTEIN_CODING, PSEUDOGENE)));
        //The genes with an observation, the pseudogene having none, so that each background
        //holds different genes
        when(this.geneDAO.getGenesWithDataBySpeciesIdsOrdered(Set.of(SPECIES_ID), 0, 0))
        .thenAnswer(invocation -> getMockResultSet(GeneTOResultSet.class,
                List.of(CODING_1, LNC, CODING_2)));
    }

    /**
     * Test {@link TopAnatService#loadBackgroundGeneIds(int, Background)}.
     */
    @Test
    public void shouldLoadBackgroundGeneIds() {
        this.mockBackgroundGenes();
        TopAnatService service = new TopAnatService(this.serviceFactory);

        assertEquals("Incorrect protein coding genes", List.of("G1", "G4"),
                service.loadBackgroundGeneIds(SPECIES_ID, Background.PROTEIN_CODING));
        assertEquals("Incorrect genes of the biotypes targeted by the polyA libraries",
                List.of("G1", "G3", "G4"),
                service.loadBackgroundGeneIds(SPECIES_ID, Background.POLY_A));
        assertEquals("Incorrect genes with an observation", List.of("G1", "G2", "G4"),
                service.loadBackgroundGeneIds(SPECIES_ID, Background.OBSERVED));

        assertThrows("A background must be provided", IllegalArgumentException.class,
                () -> service.loadBackgroundGeneIds(SPECIES_ID, null));
        assertThrows("The genes of a custom background are unknown to Bgee",
                IllegalArgumentException.class,
                () -> service.loadBackgroundGeneIds(SPECIES_ID, Background.CUSTOM));
        assertThrows("An invalid species ID should be rejected", IllegalArgumentException.class,
                () -> service.loadBackgroundGeneIds(0, Background.POLY_A));
    }

    /**
     * Test {@link TopAnatService#loadBackgroundGeneIds(int, Background)} when no biotype
     * defines the requested background: returning no gene would look like a species with
     * no gene.
     */
    @Test
    public void shouldRejectABackgroundDefinedByNoBiotype() {
        this.mockBackgroundGenes();
        when(this.geneDAO.getGeneBioTypes()).thenAnswer(
                invocation -> getMockResultSet(GeneBioTypeTOResultSet.class, List.of(LNC_RNA)));
        assertThrows("A background defined by no biotype should be rejected",
                IllegalStateException.class,
                () -> new TopAnatService(this.serviceFactory).loadBackgroundGeneIds(
                        SPECIES_ID, Background.PROTEIN_CODING));
    }

    /**
     * Test that a {@code TopAnatService} created without {@code ServiceFactory} supplier
     * does not generate the files.
     */
    @Test
    public void shouldNotGenerateFilesWithoutServiceFactorySupplier() {
        assertThrows("A service with no ServiceFactory supplier should not generate the files",
                IllegalStateException.class,
                () -> new TopAnatService(this.serviceFactory).generateFiles(
                        List.of(SPECIES_ID), this.tmpFolder.getRoot().getPath(), false, 1));
    }

    /**
     * Test {@link TopAnatService#proceedToTopAnatAnalyses(List, org.bgee.model.job.Job)} and
     * {@link TopAnatService#areAnalysesDone(List)}: each analysis is run, in the order of its
     * parameters, and analyses are done when each of them is.
     */
    @Test
    public void shouldProceedToTopAnatAnalyses() throws Exception {
        TopAnatParams params1 = new TopAnatParams.Builder(Set.of("G1"), SPECIES_ID,
                SummaryCallType.ExpressionSummary.EXPRESSED).build();
        TopAnatParams params2 = new TopAnatParams.Builder(Set.of("G1"), SPECIES_ID,
                SummaryCallType.ExpressionSummary.EXPRESSED).devStageId("UBERON:0000092").build();
        TopAnatAnalysis analysis1 = mock(TopAnatAnalysis.class);
        TopAnatAnalysis analysis2 = mock(TopAnatAnalysis.class);
        TopAnatResults results1 = mock(TopAnatResults.class);
        TopAnatResults results2 = mock(TopAnatResults.class);
        when(analysis1.proceedToAnalysis()).thenReturn(results1);
        when(analysis2.proceedToAnalysis()).thenReturn(results2);
        when(analysis1.isAnalysisDone()).thenReturn(true);
        when(analysis2.isAnalysisDone()).thenReturn(false);
        Map<TopAnatParams, TopAnatAnalysis> analyses = Map.of(params1, analysis1,
                params2, analysis2);
        //The R manager of each analysis is created from the properties of the service
        BgeeProperties props = mock(BgeeProperties.class);
        when(props.getTopAnatRScriptExecutable()).thenReturn("Rscript");
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getBgeeProperties()).thenReturn(props);
        TopAnatService service = new TopAnatService(serviceFactory, null,
                (params, p, factory, rManager, topAnatService) -> analyses.get(params));

        assertEquals("Incorrect results", List.of(results1, results2),
                service.proceedToTopAnatAnalyses(List.of(params1, params2), null)
                .collect(Collectors.toList()));
        assertTrue("The first analysis should be done",
                service.areAnalysesDone(List.of(params1)));
        assertFalse("The second analysis should not be done",
                service.areAnalysesDone(List.of(params1, params2)));
    }
}
