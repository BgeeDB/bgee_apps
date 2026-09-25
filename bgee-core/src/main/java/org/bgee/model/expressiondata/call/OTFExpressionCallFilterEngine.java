package org.bgee.model.expressiondata.call;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.BaseConditionFilter2.FilterIds;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;

public class OTFExpressionCallFilterEngine {
    private static final Logger log = LogManager.getLogger(OTFExpressionCallFilterEngine.class.getName());

    /**
     * Infer the {@code ExpressionSummary} and {@code SummaryQuality} of an {@code OTFExpressionCall},
     * using the same thresholds and the same cascade as
     * {@code CallMapping#inferSummaryCallTypeAndQuality(Set, Set, Set)} does for the calls
     * retrieved from the precomputed global expression table.
     * <p>
     * A {@code null} is returned when no summary call type applies to {@code call}: when it has
     * no p-value over all the requested data types, or when it is an absent call in a condition
     * with no observed data (absent calls are only valid in a condition where the gene
     * was actually observed).
     *
     * @param call                              The {@code OTFExpressionCall} to infer
     *                                          the summary call type and quality of.
     * @param presentHighThreshold              A {@code BigDecimal} that is the p-value threshold
     *                                          for GOLD present calls.
     * @param presentLowThreshold               A {@code BigDecimal} that is the p-value threshold
     *                                          for SILVER present calls.
     * @param absentLowThreshold                A {@code BigDecimal} that is the p-value threshold
     *                                          for SILVER absent calls.
     * @param absentHighThreshold               A {@code BigDecimal} that is the p-value threshold
     *                                          for GOLD absent calls.
     * @return                                  An {@code Entry} where the key is
     *                                          the {@code ExpressionSummary} and the value
     *                                          the {@code SummaryQuality} of {@code call},
     *                                          or {@code null} if no summary call type applies.
     */
    public static Entry<ExpressionSummary, SummaryQuality> inferSummaryCallTypeAndQuality(
            OTFExpressionCall call, BigDecimal presentHighThreshold, BigDecimal presentLowThreshold,
            BigDecimal absentLowThreshold, BigDecimal absentHighThreshold) {
        log.traceEntry("{}, {}, {}, {}, {}", call, presentHighThreshold, presentLowThreshold,
                absentLowThreshold, absentHighThreshold);

        BigDecimal allPValue = call.getAllDataTypePValue();
        if (allPValue == null) {
            return log.traceExit((Entry<ExpressionSummary, SummaryQuality>) null);
        }
        BigDecimal trustedPValue = call.getTrustedDataTypePValue();
        BigDecimal bestDescAllPValue = call.getBestDirectDescendantAllDataTypePValue();
        BigDecimal bestDescTrustedPValue = call.getBestDirectDescendantTrustedDataTypePValue();

        //The order of the comparisons is important
        if (allPValue.compareTo(presentHighThreshold) <= 0) {
            return log.traceExit(summary(ExpressionSummary.EXPRESSED, SummaryQuality.GOLD));
        }
        if (allPValue.compareTo(presentLowThreshold) <= 0) {
            return log.traceExit(summary(ExpressionSummary.EXPRESSED, SummaryQuality.SILVER));
        }
        if (bestDescAllPValue != null &&
                bestDescAllPValue.compareTo(presentLowThreshold) <= 0) {
            return log.traceExit(summary(ExpressionSummary.EXPRESSED, SummaryQuality.BRONZE));
        }

        //From here we are considering an absent call, only valid in a condition where the gene
        //was observed.
        if (!Boolean.TRUE.equals(call.getDataPropagation().isIncludingObservedData())) {
            return log.traceExit((Entry<ExpressionSummary, SummaryQuality>) null);
        }
        //If there is no p-value over the data types trusted for absent calls (no such data type
        //was requested, or none produced data), or if there is PRESENT LOW expression in a
        //sub-condition when only considering those data types, the quality cannot be better
        //than BRONZE.
        boolean absCallCannotBeBetterThanBronze = trustedPValue == null ||
                bestDescTrustedPValue != null &&
                        bestDescTrustedPValue.compareTo(presentLowThreshold) <= 0;
        if (trustedPValue != null &&
                allPValue.compareTo(absentHighThreshold) > 0 &&
                trustedPValue.compareTo(absentHighThreshold) > 0) {
            return log.traceExit(summary(ExpressionSummary.NOT_EXPRESSED,
                    absCallCannotBeBetterThanBronze? SummaryQuality.BRONZE: SummaryQuality.GOLD));
        }
        if (allPValue.compareTo(absentLowThreshold) > 0) {
            if (trustedPValue != null &&
                    trustedPValue.compareTo(absentLowThreshold) > 0) {
                return log.traceExit(summary(ExpressionSummary.NOT_EXPRESSED,
                        absCallCannotBeBetterThanBronze? SummaryQuality.BRONZE: SummaryQuality.SILVER));
            }
            return log.traceExit(summary(ExpressionSummary.NOT_EXPRESSED, SummaryQuality.BRONZE));
        }
        //Unreachable as long as absentLowThreshold equals presentLowThreshold, but the thresholds
        //are parameters: an unexpected combination must not be silently labelled.
        return log.traceExit((Entry<ExpressionSummary, SummaryQuality>) null);
    }
    private static Entry<ExpressionSummary, SummaryQuality> summary(ExpressionSummary summary,
            SummaryQuality quality) {
        return new AbstractMap.SimpleEntry<>(summary, quality);
    }

    public static Predicate<OTFExpressionCall> compile(
            Set<ConditionFilter2> filters) {

        return filters.stream()
                .map(OTFExpressionCallFilterEngine::compile)
                .reduce(x -> true, Predicate::and);
    }

    private static Predicate<OTFExpressionCall> compile(ConditionFilter2 filter) {

        Predicate<OTFExpressionCall> p = call -> true;

        // =========================================================
        // ANAT ENTITY (index 0 = anat entity)
        // =========================================================
        {
            FilterIds<String> anat =
                    filter.getComposedFilterIds(ConditionParameter.ANAT_ENTITY_CELL_TYPE)
                          .getFilterIds(0);

            if (anat != null && !anat.isEmpty()) {

                Set<String> allowed = anat.getIds();
                Set<String> excluded = anat.getExcludeTermsAndChildrenIds();

                p = p.and(call -> {

                    String anatId = call.getCondition()
                            .getConditionParameterValue(ConditionParameter.ANAT_ENTITY_CELL_TYPE)
                            //Here index 1 = anat. entity :-/ 
                            .getEntity(1)
                            .getId();

                    boolean allowedOk = true;
                    boolean excludedOk = true;

                    // include constraint
                    if (!anat.isIncludeChildTerms()) {
                        allowedOk = allowed.contains(anatId);
                    }

                    // exclude constraint
                    if (excluded != null && !excluded.isEmpty()) {
                        excludedOk = !excluded.contains(anatId);
                    }

                    boolean accepted = allowedOk && excludedOk;

                    log.debug(
                            "Gene={} Condition={} ANAT={} allowedOk={} excludedOk={} -> {}",
                            call.getGene().getGeneId(),
                            call.getCondition(),
                            anatId,
                            allowedOk,
                            excludedOk,
                            accepted ? "KEEP" : "REJECT"
                    );

                    return accepted;
                });
            }
        }

        // =========================================================
        // CELL TYPE (index 1 = cell type)
        // =========================================================
        {
            FilterIds<String> cell =
                    filter.getComposedFilterIds(ConditionParameter.ANAT_ENTITY_CELL_TYPE)
                          .getFilterIds(1);

            if (cell != null && !cell.isEmpty()) {

                Set<String> allowed = cell.getIds();
                Set<String> excluded = cell.getExcludeTermsAndChildrenIds();

                p = p.and(call -> {

                    String cellId = call.getCondition()
                            .getConditionParameterValue(ConditionParameter.ANAT_ENTITY_CELL_TYPE)
                            //Here index 0 = anat. entity :-/ 
                            .getEntity(0)
                            .getId();

                    boolean allowedOk = true;
                    boolean excludedOk = true;

                    // include constraint
                    if (!cell.isIncludeChildTerms()) {
                        allowedOk = allowed.contains(cellId);
                    }

                    // exclude constraint
                    if (excluded != null && !excluded.isEmpty()) {
                        excludedOk = !excluded.contains(cellId);
                    }

                    boolean accepted = allowedOk && excludedOk;

                    log.debug(
                            "Gene={} Condition={} CELL={} allowedOk={} excludedOk={} -> {}",
                            call.getGene().getGeneId(),
                            call.getCondition(),
                            cellId,
                            allowedOk,
                            excludedOk,
                            accepted ? "KEEP" : "REJECT"
                    );

                    return accepted;
                });
            }
        }

        // =========================================================
        // DEV STAGE
        // =========================================================
        {
            FilterIds<String> stage =
                    filter.getComposedFilterIds(ConditionParameter.DEV_STAGE)
                          .getFilterIds(0);

            if (stage != null && !stage.isEmpty()) {

                Set<String> allowed = stage.getIds();
                Set<String> excluded = stage.getExcludeTermsAndChildrenIds();

                p = p.and(call -> {

                    String stageId = call.getCondition()
                            .getConditionParameterId(ConditionParameter.DEV_STAGE);

                    boolean allowedOk = true;
                    boolean excludedOk = true;

                    // include constraint
                    if (!stage.isIncludeChildTerms()) {
                        allowedOk = allowed.contains(stageId);
                    }

                    // exclude constraint
                    if (excluded != null && !excluded.isEmpty()) {
                        excludedOk = !excluded.contains(stageId);
                    }

                    boolean accepted = allowedOk && excludedOk;

                    log.debug(
                            "Gene={} Condition={} STAGE={} allowedOk={} excludedOk={} -> {}",
                            call.getGene().getGeneId(),
                            call.getCondition(),
                            stageId,
                            allowedOk,
                            excludedOk,
                            accepted ? "KEEP" : "REJECT"
                    );

                    return accepted;
                });
            }
        }

        return p;
    }

}
