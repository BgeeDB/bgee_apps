package org.bgee.pipeline.expression.downloadfile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeProperties;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.expressiondata.call.CallFilter.ExpressionCallFilter2;
import org.bgee.model.expressiondata.call.Condition2;
import org.bgee.model.expressiondata.call.ExpressionCallLoader;
import org.bgee.model.expressiondata.call.ExpressionCallProcessedFilter;
import org.bgee.model.expressiondata.call.ExpressionCallProcessedFilter.ExpressionCallProcessedFilterConditionPart;
import org.bgee.model.expressiondata.call.ExpressionCallService;
import org.bgee.model.expressiondata.call.OTFExpressionCall;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.gene.GeneService;
import org.bgee.model.species.Species;
import org.bgee.model.species.SpeciesService;
import org.bgee.pipeline.TestAncestor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
* Unit tests for {@link GenerateXRefsFilesWithExprInfo}.
*
* @author  Julien Wollbrett
* @author  Frederic Bastian
* @since Bgee 14 Aug 2018
* @version Bgee 14 Nov 2018
*/

public class GenerateXRefsFilesWithExprInfoTest extends TestAncestor {

    private static final String XREF_FILE = "/downloadfile/XRefBgee.tsv";
    private final static Logger log = LogManager.getLogger(GenerateXRefsFilesWithExprInfoTest.class.getName());
    @Override
    protected Logger getLogger() {
        return log;
    }
    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder();

    /**
     * Test {@link GenerateXRefsFilesWithExprInfo#loadUniprotXrefFileWithoutExprInfo(String, Set)}.
     * Tested separately because the approach used to load Xref could change
     */
    @Test
    public void shouldLoadXRef() {
        String file = this.getClass().getResource(XREF_FILE).getFile();

        Map<Integer, Map<String, Set<String>>> expectedXrefs = Map.of(
                9606, Map.of(
                        "ENSG00000141198", Set.of("I3L1T2", "I3L367"),
                        "ENSG00000141219", Set.of("Q15615")),
                28377, Map.of(
                        "ENSACAG00000000004", Set.of("H9G367"),
                        "ENSACAG00000000006", Set.of("G1K846")));
        assertEquals(expectedXrefs, GenerateXRefsFilesWithExprInfo
                .loadUniprotXrefFileWithoutExprInfo(file, Set.of(9606, 28377)));

        //Only the XRefs of the requested species are loaded
        assertEquals(Map.of(9606, expectedXrefs.get(9606)), GenerateXRefsFilesWithExprInfo
                .loadUniprotXrefFileWithoutExprInfo(file, Set.of(9606)));
    }

    /**
     * Test {@link GenerateXRefsFilesWithExprInfo#generate(String, String, Set, List)}
     * for the UniProt XRef file.
     */
    @Test
    public void shouldGenerateUniprotXRefFile() throws IOException {

        Species sp1 = new Species(9606);
        Species sp2 = new Species(28377);

        Gene g1 = new Gene("ENSG00000141198", sp1, new GeneBioType("type1"));
        Gene g2 = new Gene("ENSG00000141219", sp1, new GeneBioType("type1"));
        Gene g3 = new Gene("ENSACAG00000000004", sp2, new GeneBioType("type1"));
        Gene g4 = new Gene("ENSACAG00000000006", sp2, new GeneBioType("type1"));

        AnatEntity ae1 = new AnatEntity("anat1", "anat1Name", "anat1Desc");
        AnatEntity ae2 = new AnatEntity("anat2", "anat2Name", "anat2Desc");
        AnatEntity ae3 = new AnatEntity("anat3", "anat3Name", "anat3Desc");
        AnatEntity ae4 = new AnatEntity("anat4", "anat4Name", "anat4Desc");
        AnatEntity ae5 = new AnatEntity("anat5", "anat5Name", "anat5Desc");
        //A condition with no specific cell type has the root of the cell types
        AnatEntity cellTypeRoot = new AnatEntity(ConditionDAO.CELL_TYPE_ROOT_ID,
                "cellular_component", "cellTypeRootDesc");
        AnatEntity cellType = new AnatEntity("CL:0000540", "neuron", "neuronDesc");

        //The loader returns the calls of each gene ordered by expression score
        Map<Gene, List<OTFExpressionCall>> callsSpe1 = Map.of(
                g1, List.of(mockCall(ae1, cellTypeRoot), mockCall(ae2, cellTypeRoot),
                        mockCall(ae3, cellTypeRoot), mockCall(ae4, cellTypeRoot),
                        mockCall(ae5, cellTypeRoot)),
                g2, List.of(mockCall(ae5, cellTypeRoot), mockCall(ae2, cellTypeRoot),
                        mockCall(ae3, cellTypeRoot), mockCall(ae4, cellTypeRoot)));
        Map<Gene, List<OTFExpressionCall>> callsSpe2 = Map.of(
                g3, List.of(mockCall(ae3, cellType), mockCall(ae1, cellTypeRoot)),
                g4, List.of(mockCall(ae3, cellTypeRoot)));

        // Mock services
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        SpeciesService speciesService = mock(SpeciesService.class);
        when(serviceFactory.getSpeciesService()).thenReturn(speciesService);
        GeneService geneService = mock(GeneService.class);
        when(serviceFactory.getGeneService()).thenReturn(geneService);
        ExpressionCallService callService = mock(ExpressionCallService.class);
        when(serviceFactory.getExpressionCallService()).thenReturn(callService);

        // Mock methods
        //The UniProt XRefs are generated for all species
        when(speciesService.loadSpeciesByIds(Set.of(), false)).thenReturn(Set.of(sp1, sp2));
        when(geneService.loadGenes(Set.of(new GeneFilter(sp1.getId()), new GeneFilter(sp2.getId())),
                false, false, true, false)).thenReturn(Stream.of(g1, g2, g3, g4));

        //The condition part of the processed filter is computed once per species
        ExpressionCallProcessedFilterConditionPart condPart =
                mock(ExpressionCallProcessedFilterConditionPart.class);
        ExpressionCallProcessedFilter condPartProcessedFilter = mock(ExpressionCallProcessedFilter.class);
        when(condPartProcessedFilter.getConditionPart()).thenReturn(condPart);
        when(callService.processExpressionCallFilter(any(ExpressionCallFilter2.class)))
                .thenReturn(condPartProcessedFilter);

        //Then the calls of a batch of genes are loaded with a filter reusing it
        ExpressionCallProcessedFilter processedFilterSpe1 = mock(ExpressionCallProcessedFilter.class);
        ExpressionCallProcessedFilter processedFilterSpe2 = mock(ExpressionCallProcessedFilter.class);
        //The batches are processed in parallel
        List<ExpressionCallFilter2> callFilters = Collections.synchronizedList(new ArrayList<>());
        when(callService.processExpressionCallFilter(any(ExpressionCallFilter2.class), isNull(),
                eq(condPart), isNull())).thenAnswer(invocation -> {
                    ExpressionCallFilter2 callFilter = invocation.getArgument(0);
                    callFilters.add(callFilter);
                    return callFilter.getGeneFilter().getSpeciesId() == sp1.getId()?
                            processedFilterSpe1: processedFilterSpe2;
                });
        ExpressionCallLoader callLoaderSpe1 = mock(ExpressionCallLoader.class);
        when(callLoaderSpe1.loadDataOnTheFly()).thenReturn(callsSpe1);
        when(callService.getCallLoader(processedFilterSpe1)).thenReturn(callLoaderSpe1);
        ExpressionCallLoader callLoaderSpe2 = mock(ExpressionCallLoader.class);
        when(callLoaderSpe2.loadDataOnTheFly()).thenReturn(callsSpe2);
        when(callService.getCallLoader(processedFilterSpe2)).thenReturn(callLoaderSpe2);

        //method to test
        //The URL of the gene pages requires the Bgee version
        System.setProperty(BgeeProperties.MAJOR_VERSION_KEY, "16");
        System.setProperty(BgeeProperties.MINOR_VERSION_KEY, "0");
        BgeeProperties.removeFromBgeePropertiesPool();
        try {
            new GenerateXRefsFilesWithExprInfo(() -> serviceFactory).generate(
                    this.getClass().getResource(XREF_FILE).getFile(),
                    testFolder.getRoot().getPath(), new HashSet<>(), List.of("UNIPROT"));
        } finally {
            System.clearProperty(BgeeProperties.MAJOR_VERSION_KEY);
            System.clearProperty(BgeeProperties.MINOR_VERSION_KEY);
            BgeeProperties.removeFromBgeePropertiesPool();
        }

        //The calls are those of the gene page: SILVER present calls, in the propagated
        //conditions whose anat. entity and cell type are found in observed conditions, whether
        //or not they match an observed condition of the gene, with the redundant calls discarded
        Set<ConditionParameter<?, ?>> condParams = Set.of(ConditionParameter.ANAT_ENTITY_CELL_TYPE);
        assertEquals(2, callFilters.size());
        for (ExpressionCallFilter2 callFilter: callFilters) {
            assertEquals(Map.of(ExpressionSummary.EXPRESSED, SummaryQuality.SILVER),
                    callFilter.getSummaryCallTypeQualityFilter());
            assertEquals(condParams, callFilter.getCondParamCombination());
            assertEquals(1, callFilter.getConditionFilters().size());
            assertEquals(condParams,
                    callFilter.getConditionFilters().iterator().next().getObservedCondForParams());
            assertTrue(callFilter.getCallObservedDataCondParams().isEmpty());
            assertNull(callFilter.getCallObservedDataFilter());
            assertTrue(callFilter.isRedundantAncestorCallsFilter());
        }

        //check file generation
        Path outputFile = testFolder.getRoot().toPath().resolve("XRefsBgee.txt");
        log.debug("Checking file {}", outputFile);
        assertTrue("File not created: " + outputFile, Files.exists(outputFile));

        List<String> expectedLines = List.of(
                "H9G367   DR   Bgee; ENSACAG00000000004; Expressed in neuron in anat3Name "
                + "and 1 other cell type or tissue.",
                "G1K846   DR   Bgee; ENSACAG00000000006; Expressed in anat3Name.",
                "I3L1T2   DR   Bgee; ENSG00000141198; Expressed in anat1Name "
                + "and 4 other cell types or tissues.",
                "I3L367   DR   Bgee; ENSG00000141198; Expressed in anat1Name "
                + "and 4 other cell types or tissues.",
                "Q15615   DR   Bgee; ENSG00000141219; Expressed in anat5Name "
                + "and 3 other cell types or tissues.");
        //The lines are sorted by gene ID, then by UniProt ID
        assertEquals(expectedLines, Files.readAllLines(outputFile));
    }

    /**
     * Test that {@link GenerateXRefsFilesWithExprInfo#generate(String, String, Set, List)}
     * rejects an unknown XRef file type rather than silently skipping it.
     */
    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectUnknownXrefFileType() {
        new GenerateXRefsFilesWithExprInfo(() -> mock(ServiceFactory.class)).generate(
                this.getClass().getResource(XREF_FILE).getFile(),
                testFolder.getRoot().getPath(), new HashSet<>(), List.of("UNIPROT", "GENECARDS"));
    }

    /**
     * @param anatEntity    The {@code AnatEntity} of the condition of the call.
     * @param cellType      The {@code AnatEntity} that is the cell type of the condition
     *                      of the call.
     * @return              A mock {@code OTFExpressionCall} in a condition with
     *                      {@code anatEntity} and {@code cellType}.
     */
    private static OTFExpressionCall mockCall(AnatEntity anatEntity, AnatEntity cellType) {
        Condition2 cond = mock(Condition2.class);
        when(cond.getAnatEntity()).thenReturn(anatEntity);
        when(cond.getCellType()).thenReturn(cellType);
        OTFExpressionCall call = mock(OTFExpressionCall.class);
        when(call.getCondition()).thenReturn(cond);
        return call;
    }
}
