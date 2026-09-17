package org.bgee.model.expressiondata.call;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.bgee.model.TestAncestor;
import org.bgee.model.expressiondata.baseelements.PropagationState;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.junit.Test;

/**
 * Unit tests for {@link OTFExpressionCallFilterEngine#inferSummaryCallTypeAndQuality(
 * OTFExpressionCall, BigDecimal, BigDecimal, BigDecimal, BigDecimal)}.
 * <p>
 * The summary call type and quality of a call are used both to filter the calls returned by
 * the OTF propagation (see {@code ExpressionCallLoader#matchesRequestedSummaryCallType(
 * OTFExpressionCall)}) and to label the rows exported to the EasyBgee database. This class
 * therefore also asserts that the two uses agree: {@link #shouldMatchTheRequestedCallTypeFilter()}
 * checks, over all the combinations of p-values, that requesting a summary call type and quality
 * accepts exactly the calls whose inferred quality is at least the requested one.
 *
 * @author  Julien Wollbrett
 * @version Bgee 16
 */
public class OTFSummaryCallTypeAndQualityTest extends TestAncestor {

    private final static BigDecimal PRESENT_HIGH = CallServiceParent.PRESENT_HIGH_LESS_THAN_OR_EQUALS_TO;
    private final static BigDecimal PRESENT_LOW = CallServiceParent.PRESENT_LOW_LESS_THAN_OR_EQUALS_TO;
    private final static BigDecimal ABSENT_LOW = CallServiceParent.ABSENT_LOW_GREATER_THAN;
    private final static BigDecimal ABSENT_HIGH = CallServiceParent.ABSENT_HIGH_GREATER_THAN;

    /**
     * Build a call carrying only the values the inference is based on.
     */
    private static OTFExpressionCall call(String allPValue, String trustedPValue,
            String bestDescAllPValue, String bestDescTrustedPValue, boolean observedData) {
        return new OTFExpressionCall(null, null, null,
                allPValue == null? null: new BigDecimal(allPValue),
                trustedPValue == null? null: new BigDecimal(trustedPValue),
                bestDescAllPValue == null? null: new BigDecimal(bestDescAllPValue),
                bestDescTrustedPValue == null? null: new BigDecimal(bestDescTrustedPValue),
                null, null, null, null,
                observedData? PropagationState.SELF: PropagationState.DESCENDANT);
    }
    private static Entry<ExpressionSummary, SummaryQuality> infer(OTFExpressionCall call) {
        return OTFExpressionCallFilterEngine.inferSummaryCallTypeAndQuality(call,
                PRESENT_HIGH, PRESENT_LOW, ABSENT_LOW, ABSENT_HIGH);
    }
    private static void assertTier(ExpressionSummary expectedSummary, SummaryQuality expectedQuality,
            OTFExpressionCall call) {
        Entry<ExpressionSummary, SummaryQuality> tier = infer(call);
        assertEquals("Incorrect summary call type", expectedSummary, tier == null? null: tier.getKey());
        assertEquals("Incorrect summary quality", expectedQuality, tier == null? null: tier.getValue());
    }

    @Test
    public void shouldInferPresentCalls() {
        //A p-value equal to the threshold is GOLD, the comparison is "less than or equals to"
        assertTier(ExpressionSummary.EXPRESSED, SummaryQuality.GOLD,
                call("0.01", "0.01", null, null, true));
        assertTier(ExpressionSummary.EXPRESSED, SummaryQuality.GOLD,
                call("0.001", null, null, null, true));
        //Above the GOLD threshold but at or below the SILVER one
        assertTier(ExpressionSummary.EXPRESSED, SummaryQuality.SILVER,
                call("0.011", null, null, null, true));
        assertTier(ExpressionSummary.EXPRESSED, SummaryQuality.SILVER,
                call("0.05", null, null, null, true));
        //Not present in the condition itself, but present in a descendant condition
        assertTier(ExpressionSummary.EXPRESSED, SummaryQuality.BRONZE,
                call("0.5", "0.5", "0.05", null, true));
        //A present call does not require observed data in the condition itself
        assertTier(ExpressionSummary.EXPRESSED, SummaryQuality.GOLD,
                call("0.001", null, null, null, false));
    }

    @Test
    public void shouldInferAbsentCalls() {
        //Absent over all data types and over the trusted ones, above the GOLD threshold
        assertTier(ExpressionSummary.NOT_EXPRESSED, SummaryQuality.GOLD,
                call("0.2", "0.2", null, null, true));
        //Above the SILVER threshold only
        assertTier(ExpressionSummary.NOT_EXPRESSED, SummaryQuality.SILVER,
                call("0.07", "0.07", null, null, true));
        //Above the GOLD threshold over all data types, but not over the trusted ones
        assertTier(ExpressionSummary.NOT_EXPRESSED, SummaryQuality.SILVER,
                call("0.2", "0.07", null, null, true));
        //No p-value over the data types trusted for absent calls
        assertTier(ExpressionSummary.NOT_EXPRESSED, SummaryQuality.BRONZE,
                call("0.2", null, null, null, true));
        //Present in a descendant condition when considering the trusted data types only
        assertTier(ExpressionSummary.NOT_EXPRESSED, SummaryQuality.BRONZE,
                call("0.2", "0.2", "0.5", "0.01", true));
    }

    @Test
    public void shouldInferNoCall() {
        //No p-value over all the requested data types
        assertNull(infer(call(null, "0.2", null, null, true)));
        //An absent call is only valid in a condition where the gene was observed
        assertNull(infer(call("0.2", "0.2", null, null, false)));
    }

    /**
     * Assert that filtering on a requested summary call type and quality is equivalent to
     * inferring the call type and quality and comparing them to the requested ones. The filtering
     * is reproduced by {@link #acceptedByCallTypeFilter(OTFExpressionCall, ExpressionSummary,
     * SummaryQuality)}, and the equivalence is asserted over all the combinations of p-values
     * of {@link #P_VALUES}, observed or not, for each requested call type and quality.
     */
    @Test
    public void shouldMatchTheRequestedCallTypeFilter() {
        int comparisonCount = 0;
        for (String allPValue: P_VALUES) {
            for (String trustedPValue: P_VALUES) {
                for (String bestDescAllPValue: P_VALUES) {
                    for (String bestDescTrustedPValue: P_VALUES) {
                        for (boolean observedData: new boolean[] {true, false}) {
                            OTFExpressionCall call = call(allPValue, trustedPValue,
                                    bestDescAllPValue, bestDescTrustedPValue, observedData);
                            Entry<ExpressionSummary, SummaryQuality> tier = infer(call);
                            for (ExpressionSummary summary: Arrays.asList(
                                    ExpressionSummary.EXPRESSED, ExpressionSummary.NOT_EXPRESSED)) {
                                for (SummaryQuality quality: SummaryQuality.values()) {
                                    boolean matchingTier = tier != null &&
                                            tier.getKey().equals(summary) &&
                                            tier.getValue().compareTo(quality) >= 0;
                                    assertEquals("Filtering on " + summary + " " + quality
                                            + " disagrees with the inferred " + tier
                                            + " for the call " + call,
                                            acceptedByCallTypeFilter(call, summary, quality),
                                            matchingTier);
                                    comparisonCount++;
                                }
                            }
                        }
                    }
                }
            }
        }
        assertEquals("Incorrect number of comparisons",
                P_VALUES.size() * P_VALUES.size() * P_VALUES.size() * P_VALUES.size() * 2 * 2 * 3,
                comparisonCount);
    }
    /**
     * The p-values used to generate the calls of {@link #shouldMatchTheRequestedCallTypeFilter()}:
     * a missing value, and values on both sides of each threshold, including the thresholds
     * themselves.
     */
    private final static List<String> P_VALUES = Arrays.asList(
            null, "0.001", "0.01", "0.03", "0.05", "0.07", "0.1", "0.2");
    /**
     * Whether a call is accepted when requesting a summary call type and quality. Reproduces
     * {@code ExpressionCallLoader#matchesRequestedSummaryCallType(OTFExpressionCall)} for one
     * requested call type and quality.
     */
    private static boolean acceptedByCallTypeFilter(OTFExpressionCall call,
            ExpressionSummary summary, SummaryQuality quality) {
        BigDecimal allPValue = call.getAllDataTypePValue();
        if (allPValue == null) {
            return false;
        }
        if (ExpressionSummary.EXPRESSED.equals(summary)) {
            if (SummaryQuality.GOLD.equals(quality)) {
                return allPValue.compareTo(PRESENT_HIGH) <= 0;
            }
            if (allPValue.compareTo(PRESENT_LOW) <= 0) {
                return true;
            }
            return SummaryQuality.BRONZE.equals(quality)
                    && call.getBestDirectDescendantAllDataTypePValue() != null
                    && call.getBestDirectDescendantAllDataTypePValue().compareTo(PRESENT_LOW) <= 0;
        }
        BigDecimal absentThreshold = SummaryQuality.GOLD.equals(quality)? ABSENT_HIGH: ABSENT_LOW;
        if (allPValue.compareTo(absentThreshold) <= 0) {
            return false;
        }
        if (!Boolean.TRUE.equals(call.getDataPropagation().isIncludingObservedData())) {
            return false;
        }
        if (call.getBestDirectDescendantAllDataTypePValue() != null &&
                call.getBestDirectDescendantAllDataTypePValue().compareTo(PRESENT_LOW) <= 0) {
            return false;
        }
        if (SummaryQuality.BRONZE.equals(quality)) {
            return true;
        }
        BigDecimal trustedPValue = call.getTrustedDataTypePValue();
        if (trustedPValue == null || trustedPValue.compareTo(absentThreshold) <= 0) {
            return false;
        }
        return call.getBestDirectDescendantTrustedDataTypePValue() == null ||
                call.getBestDirectDescendantTrustedDataTypePValue().compareTo(PRESENT_LOW) > 0;
    }
}
