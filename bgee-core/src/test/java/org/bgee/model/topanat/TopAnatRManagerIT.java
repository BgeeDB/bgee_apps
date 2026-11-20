package org.bgee.model.topanat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.distribution.HypergeometricDistribution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeProperties;
import org.bgee.model.ManageReadWriteLocks;
import org.bgee.model.TestAncestor;
import org.bgee.model.expressiondata.baseelements.DecorrelationType;
import org.bgee.model.expressiondata.baseelements.StatisticTest;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.topanat.TopAnatResults.TopAnatResultRow;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.rcaller.rstuff.FailurePolicy;
import com.github.rcaller.rstuff.RCaller;
import com.github.rcaller.rstuff.RCallerOptions;
import com.github.rcaller.rstuff.RCode;
import com.github.rcaller.util.Globals;

/**
 * Integration tests for {@link TopAnatRManager}: the R code of the analyses run with R and
 * BgeeDB, on small files written as {@code TopAnatAnalysis} writes them, and the results read
 * as the web page reads them. The tests are skipped when R, BiocManager, Rgraphviz, or BgeeDB
 * of the minimum version is missing, which the R code would install.
 * <p>
 * The genes G01 to G40 are expressed in an anatomical structure, G01 to G20 in an organ,
 * G01 to G10 in the brain and G11 to G16 in the liver, both organs; G01 to G08 are the genes
 * of interest, all of them expressed in the brain.
 *
 * @author  Julien Wollbrett
 * @version Bgee 16
 * @since   Bgee 13
 */
public class TopAnatRManagerIT extends TestAncestor {

    private final static Logger log = LogManager.getLogger(TopAnatRManagerIT.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }

    private final static String STRUCTURE = "UBERON:0000061";
    private final static String ORGAN = "UBERON:0000062";
    private final static String BRAIN = "UBERON:0000955";
    private final static String LIVER = "UBERON:0002107";
    private final static int GENE_COUNT = 40;
    private final static Set<String> FOREGROUND = genes(1, 8);

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private String rscript;
    private BgeeProperties props;
    private Path conditionFile;
    private Path relationFile;
    private Path backgroundFile;

    /**
     * Skips the tests when the R code would install a package, and writes the files that do not
     * depend on the kind of calls.
     */
    @Before
    public void init() throws IOException {
        String property = System.getProperty(BgeeProperties.TOP_ANAT_R_SCRIPT_EXECUTABLE_KEY);
        this.rscript = StringUtils.isBlank(property) || property.contains("${")?
                "Rscript": property;
        String check = "cat(all(nzchar(sapply(c('BiocManager', 'BgeeDB', 'Rgraphviz'), "
                + "function(p) system.file(package = p)))) && packageVersion('BgeeDB') >= '"
                + BgeeProperties.BGEEDB_MINIMUM_VERSION_DEFAULT + "')";
        String output;
        try {
            Process process = new ProcessBuilder(this.rscript, "-e", check).start();
            output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            output = e.getMessage();
        }
        Assume.assumeTrue("R, BiocManager, Rgraphviz and BgeeDB "
                + BgeeProperties.BGEEDB_MINIMUM_VERSION_DEFAULT + " or later are needed: " + output,
                output.trim().endsWith("TRUE"));

        this.props = mock(BgeeProperties.class);
        when(this.props.getTopAnatResultsWritingDirectory())
                .thenReturn(this.tmpFolder.getRoot().getPath());
        when(this.props.getTopAnatRScriptExecutable()).thenReturn(this.rscript);
        when(this.props.getBioconductorReleaseNumber()).thenReturn("3.24");
        when(this.props.getBgeeDBMinimumVersion())
                .thenReturn(BgeeProperties.BGEEDB_MINIMUM_VERSION_DEFAULT);

        this.conditionFile = this.write("conditions.tsv", List.of(
                "ID\tNAME\tANAT_ENTITY_ID\tCELL_TYPE_ID",
                STRUCTURE + "\tanatomical structure\t" + STRUCTURE + "\tGO:0005575",
                ORGAN + "\torgan\t" + ORGAN + "\tGO:0005575",
                BRAIN + "\tbrain\t" + BRAIN + "\tGO:0005575",
                LIVER + "\tliver\t" + LIVER + "\tGO:0005575"));
        this.relationFile = this.write("relations.tsv", List.of("SOURCE_ID\tTARGET_ID",
                ORGAN + "\t" + STRUCTURE, BRAIN + "\t" + ORGAN, LIVER + "\t" + ORGAN));
        List<String> background = new ArrayList<>(List.of("GENE_ID"));
        background.addAll(genes(1, GENE_COUNT));
        this.backgroundFile = this.write("background.tsv", background);
    }

    /**
     * Test the classic Fisher test, run without topGO on the propagated calls.
     */
    @Test
    public void shouldRunTheClassicFisherTest() throws Exception {
        TopAnatParams params = params(DecorrelationType.NONE, FOREGROUND);
        Map<String, TopAnatResultRow> rows = this.runAnalysis(params, Map.of(
                STRUCTURE, genes(1, 40), ORGAN, genes(1, 20), BRAIN, genes(1, 10),
                LIVER, genes(11, 16)));

        //The brain, then the organ, the most significant conditions, drawn in the graph
        assertCondition(rows.get(BRAIN), "brain", 10, 8);
        assertCondition(rows.get(ORGAN), "organ", 20, 8);
        //The root added by BgeeDB holds the 40 genes of the anatomical structure, its child:
        //it is kept in the result file, flagged as redundant
        assertTrue("The root should be flagged as redundant", rows.get("BGEE:0").isRedundant());
        assertFalse("The anatomical structure should not be redundant",
                rows.get(STRUCTURE).isRedundant());
        assertTrue("The graph should be drawn", Files.size(
                this.tmpFolder.getRoot().toPath().resolve("analysis/topAnat_results.pdf")) > 0);
    }

    /**
     * Test the elim algorithm, run with topGO on the observed calls: once the brain is
     * significant, its genes are removed from the organ.
     */
    @Test
    public void shouldRunTheElimAlgorithm() throws Exception {
        TopAnatParams params = params(DecorrelationType.ELIM, FOREGROUND);
        Map<String, TopAnatResultRow> rows = this.runAnalysis(params, Map.of(
                STRUCTURE, genes(21, 40), ORGAN, genes(17, 20), BRAIN, genes(1, 10),
                LIVER, genes(11, 16)));

        //The brain has no child: its p-value is the one of the classic algorithm
        assertCondition(rows.get(BRAIN), "brain", 10, 8);
        assertEquals("The genes of the brain should be removed from the organ", 1.0,
                rows.get(ORGAN).getPval(), 0.0);
    }

    /**
     * Test an analysis whose genes of interest have no call: the result file is empty,
     * and the last line of the console tells it to {@code TopAnatAnalysis}.
     */
    @Test
    public void shouldWriteAnEmptyResultFileWithoutResult() throws Exception {
        TopAnatParams params = params(DecorrelationType.NONE, Set.of("G99"));
        Path directory = this.runR(params, Map.of(STRUCTURE, genes(1, 40), ORGAN, genes(1, 20),
                BRAIN, genes(1, 10), LIVER, genes(11, 16)));

        assertEquals("The result file should be empty", 0,
                Files.size(directory.resolve("topAnat_results.tsv")));
        List<String> console = Files.readAllLines(directory.resolve("topAnat_log.R_console"));
        assertTrue("The console should end with the message of an analysis without result",
                console.get(console.size() - 1).contains(TopAnatRManager.NO_RESULT_MESSAGE_PREFIX));
    }

    /**
     * Test that interrupting the thread running the R code, as the cancellation of a job does,
     * stops R, rather than letting it run to its end.
     */
    @Test
    public void shouldStopRWhenInterrupted() throws Exception {
        RCode code = RCode.create();
        code.addRCode("Sys.sleep(120)");
        code.addRCode("resultExist <- TRUE");
        Globals.setRscriptCurrent(this.rscript);
        TopAnatRManager rManager = new TopAnatRManager(
                RCaller.create(RCallerOptions.create(FailurePolicy.RETRY_1, Long.MAX_VALUE)), code,
                this.props, params(DecorrelationType.NONE, FOREGROUND));
        String consoleFile = this.tmpFolder.getRoot().toPath().resolve("console.txt").toString();
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        Thread analysis = new Thread(() -> {
            try {
                rManager.performRFunction(consoleFile);
            } catch (Throwable e) {
                thrown.set(e);
            }
        });
        analysis.start();

        //R, the only process started by the tests at this point
        ProcessHandle r = null;
        for (int i = 0; i < 100 && r == null; i++) {
            r = ProcessHandle.current().children().findFirst().orElse(null);
            if (r == null) {
                Thread.sleep(100);
            }
        }
        assertNotNull("R should have been started", r);
        analysis.interrupt();
        analysis.join(10000);
        assertFalse("The analysis should end once interrupted", analysis.isAlive());
        assertNotNull("The interrupted analysis should end with an exception", thrown.get());
        //R ends right after being stopped, long before its 120 seconds
        r.onExit().get(10, TimeUnit.SECONDS);
        assertFalse("R should be stopped", r.isAlive());
    }

    /**
     * Asserts the counts of a condition, and its p-value: the upper tail of the hypergeometric
     * distribution, with the 3 significant digits written by the R code.
     */
    private static void assertCondition(TopAnatResultRow row, String name, int annotated,
            int significant) {
        assertEquals("Incorrect name", name, row.getAnatEntitiesName());
        assertEquals("Incorrect number of genes", annotated, row.getAnnotated(), 0.0);
        assertEquals("Incorrect number of genes of interest", significant,
                row.getSignificant(), 0.0);
        double p = new HypergeometricDistribution(GENE_COUNT, annotated, FOREGROUND.size())
                .upperCumulativeProbability(significant);
        assertEquals("Incorrect p-value of the " + name, Double.parseDouble(
                String.format(Locale.ROOT, "%.3g", p)), row.getPval(), 0.0);
    }

    /**
     * Runs the R code of an analysis and reads its results as the web page does.
     *
     * @return  The rows of the results, by condition ID.
     */
    private Map<String, TopAnatResultRow> runAnalysis(TopAnatParams params,
            Map<String, Set<String>> calls) throws Exception {
        this.runR(params, calls);
        TopAnatService topAnatService = mock(TopAnatService.class);
        when(topAnatService.getBgeeProperties()).thenReturn(this.props);
        when(topAnatService.getReadWriteLocks()).thenReturn(new ManageReadWriteLocks());
        List<TopAnatResultRow> rows = new TopAnatResults(params, "analysis", "topAnat_results.tsv",
                null, null, null, null, null, topAnatService).getRows();
        assertTrue("The results should be sorted by p-value", IntStream.range(1, rows.size())
                .allMatch(i -> rows.get(i - 1).getPval() <= rows.get(i).getPval()));
        return rows.stream().collect(Collectors.toMap(TopAnatResultRow::getAnatEntitiesId,
                Function.identity()));
    }

    /**
     * Writes the calls, then generates and runs the R code of an analysis.
     *
     * @param calls The genes expressed in each condition.
     * @return      The {@code Path} of the directory of the analysis.
     */
    private Path runR(TopAnatParams params, Map<String, Set<String>> calls) throws Exception {
        List<String> lines = new ArrayList<>(List.of("GENE_ID\tCONDITION_ID"));
        calls.forEach((condition, genes) -> genes.forEach(gene -> lines.add(gene + "\t" + condition)));
        Path geneToConditionsFile = this.write("calls.tsv", lines);
        Path directory = Files.createDirectories(this.tmpFolder.getRoot().toPath().resolve("analysis"));

        TopAnatRManager rManager = new TopAnatRManager(this.props, params);
        rManager.generateRCode(directory.toString() + "/", "topAnat_results.tsv",
                "topAnat_results.pdf", this.conditionFile, this.relationFile, geneToConditionsFile,
                this.backgroundFile, params.getSubmittedForegroundIds());
        rManager.performRFunction(directory.resolve("topAnat_log.R_console").toString());
        return directory;
    }

    /**
     * @return  The {@code TopAnatParams} of an analysis with the genes {@code foreground},
     *          of conditions holding at least one gene, the conditions with a p-value below 0.01
     *          drawn in the graph.
     */
    private static TopAnatParams params(DecorrelationType decorrelationType,
            Set<String> foreground) throws Exception {
        return new TopAnatParams.Builder(foreground, genes(1, GENE_COUNT), 6239,
                SummaryCallType.ExpressionSummary.EXPRESSED)
                .decorrelationType(decorrelationType).statisticTest(StatisticTest.FISHER)
                .nodeSize(1).fdrThreshold(1.0).pvalueThreshold(0.01).numberOfSignificantNode(10)
                .build();
    }

    /**
     * @return  The IDs of the genes from {@code first} to {@code last}, sorted.
     */
    private static Set<String> genes(int first, int last) {
        return IntStream.rangeClosed(first, last).mapToObj(i -> String.format("G%02d", i))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private Path write(String fileName, List<String> lines) throws IOException {
        return Files.write(this.tmpFolder.getRoot().toPath().resolve(fileName), lines);
    }
}
