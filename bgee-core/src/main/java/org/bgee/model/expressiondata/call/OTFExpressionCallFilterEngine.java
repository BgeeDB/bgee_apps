package org.bgee.model.expressiondata.call;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.BaseConditionFilter2.FilterIds;
import org.bgee.model.anatdev.AnatEntity;
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
        BigDecimal bestDescAllPValue = call.getBestDescendantAllDataTypePValue();
        BigDecimal bestDescTrustedPValue = call.getBestDescendantTrustedDataTypePValue();

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
        // ANAT ENTITY
        // =========================================================
        {
            FilterIds<String> anat =
                    filter.getComposedFilterIds(ConditionParameter.ANAT_ENTITY_CELL_TYPE)
                          .getFilterIds(0);

            if (anat != null && !anat.isEmpty()) {

                Set<String> allowed = anat.getIds();
                Set<String> excluded = anat.getExcludeTermsAndChildrenIds();

                p = p.and(call -> {

                    //When the anat. entity and the cell type of a condition are the same term,
                    //the composition holds a single entity: reading it by index here used to
                    //throw a NullPointerException.
                    AnatEntity anatEntity = call.getCondition().getAnatEntity();
                    if (anatEntity == null) {
                        //No anatomical entity to compare: the filter cannot accept the call
                        log.trace("Call rejected, no anat. entity in condition {}",
                                call.getCondition());
                        return false;
                    }
                    String anatId = anatEntity.getId();

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

                    //Read through the accessor as above, the single place reading
                    //the composition.
                    AnatEntity cellType = call.getCondition().getCellType();
                    if (cellType == null) {
                        log.trace("Call rejected, no cell type in condition {}",
                                call.getCondition());
                        return false;
                    }
                    String cellId = cellType.getId();

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

        // =========================================================
        // SEX AND STRAIN
        // =========================================================
        p = p.and(compileFlatHierarchy(filter, ConditionParameter.SEX));
        p = p.and(compileFlatHierarchy(filter, ConditionParameter.STRAIN));

        return p;
    }

    /**
     * Sexes and strains have no ontology: all their values are right below their root.
     * Requesting the root retrieves the conditions of all values, for their observations
     * to be propagated to it (see {@code CallServiceUtils#convertConditionFilters}):
     * the calls of the values that were not requested are discarded here.
     *
     * @param filter    The {@code ConditionFilter2} to compile.
     * @param condParam The {@code ConditionParameter} to compile, sex or strain.
     * @return          A {@code Predicate} accepting the calls of the requested values. IDs are
     *                  compared ignoring case, as the database does when retrieving
     *                  the conditions.
     */
    private static Predicate<OTFExpressionCall> compileFlatHierarchy(ConditionFilter2 filter,
            ConditionParameter<?, ?> condParam) {

        FilterIds<String> values = filter.getComposedFilterIds(condParam).getFilterIds(0);
        //When child terms are requested, all the conditions retrieved were requested
        if (values == null || values.isEmpty() || values.isIncludeChildTerms()) {
            return call -> true;
        }
        Set<String> allowed = values.getIds().stream()
                .map(id -> id.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        return call -> {
            String valueId = call.getCondition().getConditionParameterId(condParam);
            boolean accepted = valueId != null && allowed.contains(valueId.toLowerCase(Locale.ROOT));

            log.debug(
                    "Gene={} Condition={} {}={} -> {}",
                    call.getGene().getGeneId(),
                    call.getCondition(),
                    condParam,
                    valueId,
                    accepted ? "KEEP" : "REJECT"
            );

            return accepted;
        };
    }

    /**
     * Anatomical entity and cell type of {@code call}. A composed value with a single member,
     * or with none, is resolved by {@link CallServiceUtils#resolveAnatEntityAndCellType}:
     * the missing member is the root of that parameter, so a call stored with only an
     * anatomical entity is filtered as being in the cell-type root.
     */
    private static CallServiceUtils.AnatEntityAndCellType anatEntityAndCellType(OTFExpressionCall call) {
        return CallServiceUtils.resolveAnatEntityAndCellType(call.getCondition()
                .getConditionParameterValue(ConditionParameter.ANAT_ENTITY_CELL_TYPE));
    }

}
