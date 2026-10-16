package org.bgee.model.topanat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bgee.model.ComposedEntity;
import org.bgee.model.TestAncestor;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneBioTypeTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneBioTypeTOResultSet;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTOResultSet;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;
import org.bgee.model.expressiondata.baseelements.DataType;
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
 *
 * @author  Julien Wollbrett
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
     * Test {@link TopAnatService#subsetCalls(Path, EnumSet, SummaryQuality,
     * java.util.Collection)}.
     */
    @Test
    public void shouldSubsetCalls() throws IOException {
        //Only two combination columns: the columns are located by name
        Path callFile = this.tmpFolder.newFile("calls.tsv").toPath();
        Files.write(callFile, List.of(
                "geneId\tstageId\tconditionId\tRNA_SEQ\tIN_SITU-RNA_SEQ",
                "G1\tUBERON:0000104\tUBERON:0000955\tGOLD\tSILVER",
                "G1\tUBERON:0000092\tUBERON:0000955\tSILVER\tNA",
                "G2\tUBERON:0000104\tCL:0000540-UBERON:0000955\tSILVER\tGOLD",
                "G3\tUBERON:0000092\tUBERON:0002107\tNA\tSILVER"));

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
                        SummaryQuality.SILVER, List.of("UBERON:0000092")));
        //A gene holding calls in a condition at several of the requested stages is returned once
        assertEquals("Incorrect calls at several stages", List.of(
                List.of("G1", "UBERON:0000955"),
                List.of("G2", "CL:0000540-UBERON:0000955")),
                subsetCalls(callFile, EnumSet.of(DataType.RNA_SEQ), SummaryQuality.SILVER,
                        List.of("UBERON:0000104", "UBERON:0000092")));

        assertThrows("A combination of data types missing from the file should be rejected",
                IllegalStateException.class, () -> subsetCalls(callFile,
                        EnumSet.of(DataType.IN_SITU), SummaryQuality.SILVER, null));
    }
    private static List<List<String>> subsetCalls(Path callFile, EnumSet<DataType> dataTypes,
            SummaryQuality quality, List<String> stageIds) throws IOException {
        try (Stream<String[]> calls = TopAnatService.subsetCalls(callFile, dataTypes,
                quality, stageIds)) {
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
}
