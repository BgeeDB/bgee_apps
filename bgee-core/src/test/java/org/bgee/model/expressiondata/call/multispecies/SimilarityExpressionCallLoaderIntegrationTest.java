package org.bgee.model.expressiondata.call.multispecies;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bgee.model.ServiceFactory;
import org.bgee.model.TestAncestor;
import org.bgee.model.anatdev.multispemapping.AnatEntitySimilarity;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.expressiondata.BaseConditionFilter2.ComposedFilterIds;
import org.bgee.model.expressiondata.BaseConditionFilter2.FilterIds;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.expressiondata.call.ConditionFilter2;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Integration tests for {@link SimilarityExpressionCallLoader}, run against a real database.
 * <p>
 * These tests are skipped unless a database connection is configured through the
 * {@code bgee.dao.jdbc.url} system property, e.g.:
 * <pre>
 * mvn -pl bgee-core -am test -Dtest=SimilarityExpressionCallLoaderIntegrationTest \
 *   -Dbgee.dao.jdbc.url='jdbc:mysql://host:3306/bgee_vXX_X' \
 *   -Dbgee.dao.jdbc.username=... -Dbgee.dao.jdbc.password=... \
 *   -Dbgee.dao.jdbc.driver.names=com.mysql.cj.jdbc.Driver
 * </pre>
 *
 * @author  Harald Detering
 * @version Bgee 16, Aug. 2026
 * @since   Bgee 16, Aug. 2026
 */
public class SimilarityExpressionCallLoaderIntegrationTest extends TestAncestor {

    /**
     * Vertebrata, the taxon used by the pre-existing integration tests with the same gene set.
     */
    private static final int TAXON_ID = 7742;
    /** Human HBB and APOE. */
    private static final GeneFilter HUMAN_GENES =
            new GeneFilter(9606, Arrays.asList("ENSG00000244734", "ENSG00000130208"));
    /** Mouse Hbb-bs and Apoe. */
    private static final GeneFilter MOUSE_GENES =
            new GeneFilter(10090, Arrays.asList("ENSMUSG00000052187", "ENSMUSG00000040564"));

    /**
     * The summary anatomical entity IDs used by the webapp for {@code anat_entity_id=SUMMARY}
     * (see {@code CommandData.loadMultispecConditionFilter}).
     */
    private static final Set<String> SUMMARY_ANAT_IDS = Set.of(
            "UBERON:0001062", "UBERON:0000010", "UBERON:0000211", "UBERON:0000309", "UBERON:0000468",
            "UBERON:0000949", "UBERON:0000990", "UBERON:0001004", "UBERON:0001007", "UBERON:0001008",
            "UBERON:0001009", "UBERON:0001015", "UBERON:0001017", "UBERON:0001032", "UBERON:0001434",
            "UBERON:0002193", "UBERON:0002330", "UBERON:0002384", "UBERON:0002405", "UBERON:0002416",
            "UBERON:0015204");

    @Before
    public void assumeDatabaseConfigured() {
        Assume.assumeTrue("Integration test skipped: no database configured "
                + "(system property bgee.dao.jdbc.url not set)",
                System.getProperty("bgee.dao.jdbc.url") != null);
    }

    private static SimilarityExpressionCallFilter newFilter(SummaryQuality quality,
            Collection<ConditionFilter2> condFilters) {
        return new SimilarityExpressionCallFilter(TAXON_ID,
                Arrays.asList(HUMAN_GENES, MOUSE_GENES), condFilters, false, quality);
    }

    /**
     * Builds a compact, human-readable identity key for a call: gene, anat/cell similarity
     * source entities, and summary call type. Used in assertions instead of full
     * {@code toString()}, whose output for a whole page is large enough to exhaust the heap.
     */
    private static String compactKey(SimilarityExpressionCall2 call) {
        return call.getGene().getGeneId() + "|" + call.getGene().getSpecies().getId()
                + "|" + simKey(call.getMultiSpeciesCondition().getAnatSimilarity())
                + "|" + simKey(call.getMultiSpeciesCondition().getCellTypeSimilarity())
                + "|" + call.getSummaryCallType();
    }

    private static String simKey(AnatEntitySimilarity sim) {
        return sim == null ? "-" : sim.getSourceAnatEntities().stream()
                .map(ae -> ae.getId()).sorted().collect(Collectors.joining(","));
    }

    /**
     * Asserts that two supporting-call collections are equal as sets, failing with a
     * bounded message showing only the symmetric difference (truncated samples),
     * instead of the default assertEquals message that renders both full sets.
     */
    private static void assertSupportingCallSetsEqual(int itemIndex, String itemKey,
            Collection<org.bgee.model.expressiondata.call.Call.ExpressionCall2> memoCalls,
            Collection<org.bgee.model.expressiondata.call.Call.ExpressionCall2> lazyCalls) {
        Set<org.bgee.model.expressiondata.call.Call.ExpressionCall2> memoSet = new HashSet<>(memoCalls);
        Set<org.bgee.model.expressiondata.call.Call.ExpressionCall2> lazySet = new HashSet<>(lazyCalls);
        if (memoSet.equals(lazySet)) {
            return;
        }
        Set<org.bgee.model.expressiondata.call.Call.ExpressionCall2> onlyMemo = new HashSet<>(memoSet);
        onlyMemo.removeAll(lazySet);
        Set<org.bgee.model.expressiondata.call.Call.ExpressionCall2> onlyLazy = new HashSet<>(lazySet);
        onlyLazy.removeAll(memoSet);
        fail("Supporting call sets differ for item " + itemIndex + " (" + itemKey + "): "
                + "memoized has " + memoSet.size() + " calls, lazy has " + lazySet.size()
                + ", only in memoized: " + onlyMemo.size() + ", only in lazy: " + onlyLazy.size()
                + "; sample only-in-memoized: " + sample(onlyMemo)
                + "; sample only-in-lazy: " + sample(onlyLazy));
    }

    private static String sample(Set<org.bgee.model.expressiondata.call.Call.ExpressionCall2> calls) {
        return calls.stream().findFirst().map(c -> {
            String s = c.toString();
            return s.length() <= 2500 ? s : s.substring(0, 2500) + "... [truncated]";
        }).orElse("none");
    }

    /**
     * Builds the same condition filter as the webapp for
     * {@code anat_entity_id=SUMMARY, cell_type_id=SUMMARY}.
     */
    private static Collection<ConditionFilter2> summaryConditionFilter() {
        ComposedFilterIds<String> anatCellComposed = new ComposedFilterIds<>(List.of(
                new FilterIds<>(SUMMARY_ANAT_IDS, false),
                new FilterIds<>(Set.of(ConditionDAO.CELL_TYPE_ROOT_ID), false)));
        Map<ConditionParameter<?, ?>, ComposedFilterIds<String>> condParamToFilter = new HashMap<>();
        condParamToFilter.put(ConditionParameter.ANAT_ENTITY_CELL_TYPE, anatCellComposed);
        return Collections.singletonList(new ConditionFilter2(
                null, condParamToFilter,
                Set.of(ConditionParameter.ANAT_ENTITY_CELL_TYPE), null, false));
    }

    /**
     * Basic functionality: known orthologous genes from two species must yield non-empty,
     * structurally complete results, with both species represented, and the total count
     * consistent with the retrieved results.
     */
    @Test
    public void shouldLoadBasicCallsViaLoaderIntegration() {
        try (ServiceFactory serviceFactory = new ServiceFactory()) {
            SimilarityExpressionCallLoader loader = serviceFactory.getMultiSpeciesCallService()
                    .loadSimilarityCallLoader(newFilter(SummaryQuality.BRONZE, null));

            long count = loader.loadDataCount();
            //Served from the memoized list built by loadDataCount(), no second pass.
            List<SimilarityExpressionCall2> results = loader.loadData(null, null);

            assertTrue("Expected results for known orthologous genes", count > 0);
            assertEquals("Count and result size should be consistent",
                    Math.min(count, SimilarityExpressionCallLoader.LIMIT_MAX), results.size());

            Set<Integer> speciesInResults = results.stream()
                    .map(c -> c.getGene().getSpecies().getId())
                    .collect(Collectors.toSet());
            assertTrue("Both queried species should be present in the results, found: "
                    + speciesInResults, speciesInResults.containsAll(Set.of(9606, 10090)));

            for (SimilarityExpressionCall2 call : results) {
                assertNotNull("Gene must not be null", call.getGene());
                assertNotNull("Condition must not be null", call.getMultiSpeciesCondition());
                assertNotNull("Summary call type must not be null", call.getSummaryCallType());
                assertNotNull("Supporting calls must not be null", call.getCalls());
                assertFalse("Supporting calls must not be empty", call.getCalls().isEmpty());
            }
        }
    }

    /**
     * Determinism and lazy-vs-memoized equivalence: a fresh loader answering
     * {@code loadData} through lazy iteration (stopping as soon as the page is filled)
     * must return exactly the same ordered page as a loader that first performed
     * a full pass through {@code loadDataCount()} and then serves pages from its
     * memoized list.
     * <p>
     * Only a first page is compared (rather than two full result lists) to keep
     * the memory footprint of the test itself low; page identity plus the stable
     * count assertion is sufficient to establish equivalence of the two paths.
     */
    @Test
    public void shouldReturnIdenticalResultsForLazyAndMemoizedPathsIntegration() {
        Collection<ConditionFilter2> condFilters = summaryConditionFilter();
        final int pageSize = 500;
        try (ServiceFactory serviceFactory = new ServiceFactory()) {
            //Memoized path: loadDataCount performs the full pass, the page is then
            //served from the memoized list.
            SimilarityExpressionCallLoader memoLoader = serviceFactory.getMultiSpeciesCallService()
                    .loadSimilarityCallLoader(newFilter(SummaryQuality.SILVER, condFilters));
            long count = memoLoader.loadDataCount();
            assertTrue("Expected results for known orthologous genes", count > 0);
            List<SimilarityExpressionCall2> memoPage = memoLoader.loadData(0L, pageSize);

            //Lazy path: loadData on a fresh loader, no prior full pass, iteration
            //stops once the page is filled.
            SimilarityExpressionCallLoader lazyLoader = serviceFactory.getMultiSpeciesCallService()
                    .loadSimilarityCallLoader(newFilter(SummaryQuality.SILVER, condFilters));
            List<SimilarityExpressionCall2> lazyPage = lazyLoader.loadData(0L, pageSize);

            assertEquals("Page size must be consistent with the total count",
                    Math.min(count, pageSize), memoPage.size());

            //Layered comparison to precisely diagnose any difference without building
            //gigantic assertion messages (full toString of pages can exhaust the heap):
            //1. item identity and ordering (what pagination stability depends on),
            assertEquals("Lazy and memoized paths must return pages with identical item ordering",
                    memoPage.stream().map(SimilarityExpressionCallLoaderIntegrationTest::compactKey)
                            .collect(Collectors.toList()),
                    lazyPage.stream().map(SimilarityExpressionCallLoaderIntegrationTest::compactKey)
                            .collect(Collectors.toList()));
            //2. supporting calls, order-insensitively, then 3. strict equality (order-sensitively).
            for (int i = 0; i < memoPage.size(); i++) {
                SimilarityExpressionCall2 memoCall = memoPage.get(i);
                SimilarityExpressionCall2 lazyCall = lazyPage.get(i);
                assertSupportingCallSetsEqual(i, compactKey(memoCall),
                        memoCall.getCalls(), lazyCall.getCalls());
                assertTrue("Item " + i + " (" + compactKey(memoCall) + ") differs between lazy "
                        + "and memoized paths although supporting call sets are equal; "
                        + "most likely the supporting-call ordering is not deterministic",
                        memoCall.equals(lazyCall));
            }
            //Repeated count on the same loader must be stable.
            assertEquals("Repeated loadDataCount() must return the same value",
                    count, memoLoader.loadDataCount());
        }
    }

    /**
     * Quality threshold monotonicity: every (gene, condition) association returned with
     * a stricter minimum quality must also be returned with a more lenient one
     * (GOLD \subseteq SILVER \subseteq BRONZE). Only the association identity is compared,
     * since supporting calls and derived summary type may legitimately differ
     * between thresholds.
     */
    @Test
    public void shouldRespectQualityThresholdMonotonicityIntegration() {
        Collection<ConditionFilter2> condFilters = summaryConditionFilter();
        try (ServiceFactory serviceFactory = new ServiceFactory()) {
            Map<SummaryQuality, Set<SimpleImmutableEntry<Gene, MultiSpeciesCondition>>> keysByQuality =
                    new HashMap<>();
            for (SummaryQuality quality : List.of(SummaryQuality.BRONZE, SummaryQuality.SILVER,
                    SummaryQuality.GOLD)) {
                SimilarityExpressionCallLoader loader = serviceFactory.getMultiSpeciesCallService()
                        .loadSimilarityCallLoader(newFilter(quality, condFilters));
                keysByQuality.put(quality, loader.loadData(null, null).stream()
                        .map(c -> new SimpleImmutableEntry<>(c.getGene(), c.getMultiSpeciesCondition()))
                        .collect(Collectors.toSet()));
            }

            assertFalse("Expected results at BRONZE quality",
                    keysByQuality.get(SummaryQuality.BRONZE).isEmpty());
            assertTrue("SILVER results must be a subset of BRONZE results",
                    keysByQuality.get(SummaryQuality.BRONZE)
                            .containsAll(keysByQuality.get(SummaryQuality.SILVER)));
            assertTrue("GOLD results must be a subset of SILVER results",
                    keysByQuality.get(SummaryQuality.SILVER)
                            .containsAll(keysByQuality.get(SummaryQuality.GOLD)));
        }
    }
}
