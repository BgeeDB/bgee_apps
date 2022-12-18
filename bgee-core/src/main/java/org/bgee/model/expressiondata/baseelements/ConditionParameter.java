package org.bgee.model.expressiondata.baseelements;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.bgee.model.BgeeEnum;
import org.bgee.model.BgeeEnum.BgeeEnumField;
import org.bgee.model.expressiondata.call.Call;
import org.bgee.model.expressiondata.call.Condition;

/**
 * {@code Enum} defining the condition parameters that can be requested.
 * Results are different depending on the condition parameters, it is not
 * a simple "masking" of parameters.
 * <ul>
 * <li>{@code ANAT_ENTITY}: corresponds to {@link Condition#getAnatEntityId()} from {@link Call#getCondition()}.
 * <li>{@code DEV_STAGE}: corresponds to {@link Condition#getDevStageId()} from {@link Call#getCondition()}.
 * <li>{@code CELL_TYPE}: corresponds to {@link Condition#getCellTypeId()} from {@link Call#getCondition()}.
 * <li>{@code SEX}: corresponds to {@link Condition#getSexId()} from {@link Call#getCondition()}.
 * <li>{@code STRAIN}: corresponds to {@link Condition#getStrainId()} from {@link Call#getCondition()}.
 * </ul>
 */
public enum ConditionParameter implements BgeeEnumField {
    ANAT_ENTITY("anat_entity", "Anat. entity"), DEV_STAGE("dev_stage", "Dev. stage"),
    CELL_TYPE("cell_type", "Cell type"), SEX("sex", "Sex"), STRAIN("strain", "Strain");


    private final static Set<EnumSet<ConditionParameter>> ALL_COND_PARAM_COMBINATIONS =
            BgeeEnum.getAllPossibleEnumCombinations(ConditionParameter.class,
                    EnumSet.allOf(ConditionParameter.class));

    public static final Set<EnumSet<ConditionParameter>> getAllPossibleCondParamCombinations() {
        //defensive copying
        return ALL_COND_PARAM_COMBINATIONS.stream()
                .map(s -> EnumSet.copyOf(s))
                .collect(Collectors.toSet());
    }
    public static final Set<EnumSet<ConditionParameter>> getAllPossibleCondParamCombinations(
            Collection<ConditionParameter> condParams) {
        if (condParams == null || condParams.isEmpty() ||
                condParams.containsAll(EnumSet.allOf(ConditionParameter.class))) {
            return getAllPossibleCondParamCombinations();
        }
        return BgeeEnum.getAllPossibleEnumCombinations(ConditionParameter.class,
                condParams);
    }

    private final String representation;
    private final String displayName;

    private ConditionParameter(String representation, String displayName) {
        this.representation = representation;
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }
    @Override
    public String getStringRepresentation() {
        return this.representation;
    }
}