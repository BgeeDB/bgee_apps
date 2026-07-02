package org.bgee.model.expressiondata.call;

import java.util.Set;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.BaseConditionFilter2.FilterIds;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;

public class OTFExpressionCallFilterEngine {
    private static final Logger log = LogManager.getLogger(OTFExpressionCallFilterEngine.class.getName());

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
