package org.bgee.model.expressiondata.call;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bgee.model.ServiceFactory;
import org.bgee.model.TestAncestor;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.expressiondata.ObservedExpressionDAO.ObservedExpressionTO;
import org.bgee.model.expressiondata.call.ConditionGraphCacheService.ConditionGraphCache;
import org.bgee.model.gene.Gene;
import org.junit.Test;

/**
 * Unit tests for {@link ExpressionCallLoader#propagateCalls(Map, ConditionGraphCache, Set, boolean)},
 * run over small synthetic condition graphs.
 * <p>
 * The condition graph is a DAG, not a tree: a same condition can be reached from an ancestor
 * through several distinct paths. The propagation must therefore take the observations of a
 * descendant into account exactly once per ancestor, whatever the number of paths
 * connecting them.
 * <p>
 * Except for {@link #shouldPruneRedundantAncestors()}, which targets that feature, the tests
 * run with the filtering of redundant ancestor calls disabled: every condition of the graph
 * is then present in the result and can be asserted on. Running them with the filtering
 * enabled would remove conditions from the result, and could hide a propagation error whose
 * incorrect value happens to be equal to that of a descendant.
 *
 * @author  Julien Wollbrett
 * @version Bgee 16
 */
public class ExpressionCallLoaderPropagationTest extends TestAncestor {

    private static final int GENE_ID = 100;

    //Values of the 'filterRedundantAncestorCalls' argument of propagateCalls,
    //named for readability at the call sites.
    private static final boolean FILTER_REDUNDANT_ANCESTORS = true;
    private static final boolean KEEP_REDUNDANT_ANCESTORS = false;

    //Global condition IDs used to build the synthetic graphs
    private static final int L1 = 1;
    private static final int L2 = 2;
    private static final int A  = 3;
    private static final int B  = 4;
    private static final int R  = 5;

    //*************************************************************************
    // TESTS
    //*************************************************************************

    /**
     * Control test over a simple chain {@code L1 -> A -> R}, where no condition can be reached
     * through more than one path. The single observation in {@code L1} must be propagated
     * unchanged to {@code A} and to {@code R}.
     */
    @Test
    public void shouldPropagateCallsOverAChain() {
        Map<Integer, Condition2> condMap = mockConditionMap(L1, A, R);
        Gene gene = mock(Gene.class);
        ExpressionCallLoader loader = mockLoader(condMap, Map.of(GENE_ID, gene));

        Map<Integer, Map<Integer, Set<ObservedExpressionTO>>> observations = Map.of(
                GENE_ID, Map.of(L1, Set.of(bulkObservation(L1, "0.01", "80", "10"))));

        Set<OTFExpressionCall> calls = loader.propagateCalls(observations, chainGraph(),
                Collections.emptySet(), KEEP_REDUNDANT_ANCESTORS).get(gene);

        assertEquals("All the conditions of the chain should have been propagated",
                3, calls.size());
        assertCall("intermediate condition A", calls, condMap.get(A), "10", "80");
        assertCall("root condition R", calls, condMap.get(R), "10", "80");
    }

    /**
     * Test over a diamond-shaped graph, where {@code L1} is a descendant of {@code R}
     * through two distinct paths:
     * <pre>
     *         R
     *        / \
     *       A   B
     *        \ / \
     *        L1   L2      L1 and L2 carry the observations of the gene
     * </pre>
     * {@code L1} contributes a weight of 10 and a score of 80, {@code L2} a weight of 10 and
     * a score of 60. In {@code R}, each observation must be counted exactly once: the expected
     * weight is therefore 10 + 10 = 20, and the expected weighted average score is
     * (80 * 10 + 60 * 10) / 20 = 70.
     * <p>
     * A propagation summing the values of the direct children instead counts {@code L1} twice
     * (once through {@code A}, once through {@code B}), yielding a weight of 30 and a score
     * of 73.33.
     */
    @Test
    public void shouldNotCountTwiceADescendantReachedThroughSeveralPaths() {
        Map<Integer, Condition2> condMap = mockConditionMap(L1, L2, A, B, R);
        Gene gene = mock(Gene.class);
        ExpressionCallLoader loader = mockLoader(condMap, Map.of(GENE_ID, gene));

        Map<Integer, Map<Integer, Set<ObservedExpressionTO>>> observations = Map.of(
                GENE_ID, Map.of(
                        L1, Set.of(bulkObservation(L1, "0.01", "80", "10")),
                        L2, Set.of(bulkObservation(L2, "0.02", "60", "10"))));

        Set<OTFExpressionCall> calls = loader.propagateCalls(observations, diamondGraph(),
                Collections.emptySet(), KEEP_REDUNDANT_ANCESTORS).get(gene);

        assertEquals("All the conditions of the diamond should have been propagated",
                5, calls.size());
        //A sees L1 only, B sees L1 and L2 through two distinct paths: both are references
        //for a correct aggregation, neither can be affected by a multi-path issue.
        assertCall("condition A", calls, condMap.get(A), "10", "80");
        assertCall("condition B", calls, condMap.get(B), "20", "70");
        //R sees the very same two observations as B, only through a diamond.
        assertCall("root condition R, whose descendant L1 is reachable through two paths",
                calls, condMap.get(R), "20", "70");
    }

    /**
     * Characterization test of the filtering of redundant ancestor calls, over the same chain
     * {@code L1 -> A -> R} and the same single observation as
     * {@link #shouldPropagateCallsOverAChain()}. Every ancestor then has exactly the score of
     * {@code L1}: when the filtering is requested, only the call for {@code L1} is returned.
     * <p>
     * This test does not endorse that behaviour - discarding rather than flagging a redundant
     * ancestor means a query filtering on an ancestor term returns nothing, and the equality
     * is tested on scores rounded to two decimals. It pins the current behaviour so that any
     * change to it fails here explicitly.
     */
    @Test
    public void shouldPruneRedundantAncestors() {
        Map<Integer, Condition2> condMap = mockConditionMap(L1, A, R);
        Gene gene = mock(Gene.class);
        ExpressionCallLoader loader = mockLoader(condMap, Map.of(GENE_ID, gene));

        Map<Integer, Map<Integer, Set<ObservedExpressionTO>>> observations = Map.of(
                GENE_ID, Map.of(L1, Set.of(bulkObservation(L1, "0.01", "80", "10"))));

        Set<OTFExpressionCall> calls = loader.propagateCalls(observations, chainGraph(),
                Collections.emptySet(), FILTER_REDUNDANT_ANCESTORS).get(gene);

        assertEquals("Both ancestors carrying the score of L1 should have been discarded",
                1, calls.size());
        assertCall("remaining condition L1", calls, condMap.get(L1), "10", "80");
    }

    //*************************************************************************
    // GRAPHS
    //*************************************************************************

    /**
     * @return  The graph {@code L1 -> A -> R}, where every condition has a single path
     *          to the root.
     */
    private static ConditionGraphCache chainGraph() {
        Map<Integer, int[]> directAncestors = new HashMap<>();
        directAncestors.put(L1, new int[] {A});
        directAncestors.put(A,  new int[] {R});
        directAncestors.put(R,  new int[0]);
        Map<Integer, int[]> directDescendants = new HashMap<>();
        directDescendants.put(A, new int[] {L1});
        directDescendants.put(R, new int[] {A});
        return new ConditionGraphCache(directAncestors, directDescendants, new int[] {L1, A, R});
    }

    /**
     * @return  The graph where {@code L1} is a descendant of {@code R} through {@code A}
     *          and through {@code B}, and {@code L2} a descendant of {@code R}
     *          through {@code B} only.
     */
    private static ConditionGraphCache diamondGraph() {
        Map<Integer, int[]> directAncestors = new HashMap<>();
        directAncestors.put(L1, new int[] {A, B});
        directAncestors.put(L2, new int[] {B});
        directAncestors.put(A,  new int[] {R});
        directAncestors.put(B,  new int[] {R});
        directAncestors.put(R,  new int[0]);
        Map<Integer, int[]> directDescendants = new HashMap<>();
        directDescendants.put(A, new int[] {L1});
        directDescendants.put(B, new int[] {L1, L2});
        directDescendants.put(R, new int[] {A, B});
        return new ConditionGraphCache(directAncestors, directDescendants,
                new int[] {L1, L2, A, B, R});
    }

    //*************************************************************************
    // HELPERS
    //*************************************************************************

    /**
     * Single-cell RNA-Seq is not trusted for absent calls ({@link DataType#SC_RNA_SEQ}), so a call
     * only supported by single-cell data must not carry any p-value over the data types trusted
     * for absent calls. It is what prevents such a call from being a better than BRONZE absent
     * call (see {@code OTFExpressionCallFilterEngine#inferSummaryCallTypeAndQuality(
     * OTFExpressionCall, BigDecimal, BigDecimal, BigDecimal, BigDecimal)}), while a call
     * supported by bulk RNA-Seq carries one.
     */
    @Test
    public void shouldNotTrustSingleCellDataForAbsentCalls() {
        Map<Integer, Condition2> condMap = mockConditionMap(L1, A, R);
        Gene gene = mock(Gene.class);
        ExpressionCallLoader loader = mockLoader(condMap, Map.of(GENE_ID, gene));

        Set<OTFExpressionCall> singleCellCalls = loader.propagateCalls(
                Map.of(GENE_ID, Map.of(L1, Set.of(singleCellObservation(L1, "0.8", "10", "10")))),
                chainGraph(), Collections.emptySet(), KEEP_REDUNDANT_ANCESTORS).get(gene);
        for (OTFExpressionCall call: singleCellCalls) {
            assertNotNull("A p-value over all data types is expected for every propagated call",
                    call.getAllDataTypePValue());
            assertNull("Single-cell data must not produce a p-value over the data types trusted "
                    + "for absent calls, call: " + call, call.getTrustedDataTypePValue());
            assertNull("Single-cell data must not produce a best descendant p-value over the data "
                    + "types trusted for absent calls, call: " + call,
                    call.getBestDirectDescendantTrustedDataTypePValue());
        }

        //Same propagation with bulk RNA-Seq, which is trusted for absent calls
        Set<OTFExpressionCall> bulkCalls = loader.propagateCalls(
                Map.of(GENE_ID, Map.of(L1, Set.of(bulkObservation(L1, "0.8", "10", "10")))),
                chainGraph(), Collections.emptySet(), KEEP_REDUNDANT_ANCESTORS).get(gene);
        for (OTFExpressionCall call: bulkCalls) {
            assertNotNull("Bulk RNA-Seq is trusted for absent calls, a p-value over the trusted "
                    + "data types is expected, call: " + call, call.getTrustedDataTypePValue());
        }
    }


    /**
     * @return  An {@code ExpressionCallLoader} with its condition and gene maps seeded with
     *          {@code condMap} and {@code geneMap}, all its {@code DAO}s and {@code Service}s
     *          being mocks: {@code propagateCalls} performs no query.
     */
    private static ExpressionCallLoader mockLoader(Map<Integer, Condition2> condMap,
            Map<Integer, Gene> geneMap) {
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(mock(DAOManager.class));
        ExpressionCallProcessedFilter processedFilter = mock(ExpressionCallProcessedFilter.class);
        when(processedFilter.getRequestedConditionMap()).thenReturn(condMap);
        when(processedFilter.getRequestedGeneMap()).thenReturn(geneMap);

        return new ExpressionCallLoader(processedFilter, serviceFactory,
                mock(CallServiceUtils.class), mock(CallMapping.class));
    }

    private static Map<Integer, Condition2> mockConditionMap(int... condIds) {
        Map<Integer, Condition2> condMap = new HashMap<>();
        for (int condId: condIds) {
            condMap.put(condId, mock(Condition2.class));
        }
        return condMap;
    }

    /**
     * @return  An {@code ObservedExpressionTO} holding one droplet-based single-cell RNA-Seq
     *          observation, all other data types being absent from the condition.
     */
    private static ObservedExpressionTO singleCellObservation(int condId, String pValue,
            String score, String weight) {
        return new ObservedExpressionTO(null, condId, GENE_ID,
                null, null, null, null,
                null, null, null, null,
                new BigDecimal(score), new BigDecimal(pValue), new BigDecimal(weight), 1,
                null, null, null, null);
    }

    /**
     * @return  An {@code ObservedExpressionTO} holding one bulk RNA-Seq observation,
     *          all other data types being absent from the condition.
     */
    private static ObservedExpressionTO bulkObservation(int condId, String pValue, String score,
            String weight) {
        return new ObservedExpressionTO(null, condId, GENE_ID,
                new BigDecimal(score), new BigDecimal(pValue), new BigDecimal(weight), 1,
                null, null, null, null,
                null, null, null, null,
                null, null, null, null);
    }

    /**
     * Asserts the weight and the expression score of the call propagated for {@code cond}.
     */
    private static void assertCall(String condDescription, Set<OTFExpressionCall> calls,
            Condition2 cond, String expectedWeight, String expectedScore) {
        OTFExpressionCall call = callForCondition(condDescription, calls, cond);
        assertBigDecimalEquals("Unexpected weight for the " + condDescription,
                expectedWeight, call.getExpressionScoreWeight());
        assertBigDecimalEquals("Unexpected expression score for the " + condDescription,
                expectedScore, call.getExpressionScore());
    }

    /**
     * Compares {@code actual} to {@code expected} using {@code compareTo}, so that the scale
     * of the {@code BigDecimal}s is not taken into account, and reports the actual value
     * on failure.
     */
    private static void assertBigDecimalEquals(String message, String expected,
            BigDecimal actual) {
        assertNotNull(message + " - null value", actual);
        assertEquals(message + " - expected: " + expected + ", actual: " + actual.toPlainString(),
                0, new BigDecimal(expected).compareTo(actual));
    }

    private static OTFExpressionCall callForCondition(String condDescription,
            Set<OTFExpressionCall> calls, Condition2 cond) {
        assertNotNull("No call propagated for the gene", calls);
        OTFExpressionCall found = null;
        for (OTFExpressionCall call: calls) {
            if (cond.equals(call.getCondition())) {
                assertTrue("Several calls propagated for the " + condDescription, found == null);
                found = call;
            }
        }
        assertNotNull("No call propagated for the " + condDescription, found);
        return found;
    }
}
