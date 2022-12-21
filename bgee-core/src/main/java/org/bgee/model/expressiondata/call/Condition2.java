package org.bgee.model.expressiondata.call;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.ComposedEntity;
import org.bgee.model.Entity;
import org.bgee.model.expressiondata.BaseCondition2;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.species.Species;

public class Condition2 extends BaseCondition2 {
    private final static Logger log = LogManager.getLogger(Condition2.class.getName());

    private final Map<DataType, BigDecimal> maxRanksByDataType;
    private final Map<DataType, BigDecimal> globalMaxRanksByDataType;

    public Condition2(Map<ConditionParameter<?, ?>, ComposedEntity<?>> conditionParameterObjects,
            Species species) {
        this(conditionParameterObjects, species, null, null);
    }
    //XXX: maybe create a new Map implementation to make sure the generic type
    //of ConditionParameter and of ComposedEntity are consistent,
    //to raise an exception at compilation and not only at runtime?
    public Condition2(Map<ConditionParameter<?, ?>, ComposedEntity<?>> conditionParameterObjects,
            Species species, Map<DataType, BigDecimal> maxRanksByDataType,
            Map<DataType, BigDecimal> globalMaxRanksByDataType) {
        super(// we do this t case from Map<ConditionParameter<?, ?>, ComposedEntity<?>>
              //to Map<ConditionParameter<?, ?>, Object>.
              //we'll check correct types anyway in this constructor
              conditionParameterObjects == null? null:
                  conditionParameterObjects.entrySet()
                  .stream()
                  .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())),
              species);
        if (this.conditionParameterObjects.entrySet().stream()
                .anyMatch(e -> !ComposedEntity.class.isInstance(e.getValue()) ||
                        !e.getKey().getCondValueType().isAssignableFrom(
                                ((ComposedEntity<?>) e.getValue()).getEntityType()))) {
            throw log.throwing(new IllegalArgumentException(
                    "Incorrect object for the associated condition parameter: "
                    + this.conditionParameterObjects));
        }
        this.maxRanksByDataType = Collections.unmodifiableMap(maxRanksByDataType == null?
                new HashMap<>(): maxRanksByDataType);
        if (this.maxRanksByDataType.entrySet().stream()
                .anyMatch(e -> e.getKey() == null || e.getValue() == null)) {
            throw log.throwing(new IllegalArgumentException(
                    "No key or value can be null in maxRanksByDataType"));
        }
        this.globalMaxRanksByDataType = Collections.unmodifiableMap(
                globalMaxRanksByDataType == null? new HashMap<>(): globalMaxRanksByDataType);
        if (this.globalMaxRanksByDataType.entrySet().stream()
                .anyMatch(e -> e.getKey() == null || e.getValue() == null)) {
            throw log.throwing(new IllegalArgumentException(
                    "No key or value can be null in globalMaxRanksByDataType"));
        }
    }

    //Suppress unchecked because we make the verification of proper casting
    //in the constructor
    @SuppressWarnings("unchecked")
    public <T extends Entity<?>> ComposedEntity<T> getConditionParameterValue(
            ConditionParameter<T, ?> condParam) {
        log.traceEntry("{}", condParam);
        Object value = this.conditionParameterObjects.get(condParam);
        if (value == null) {
            return log.traceExit((ComposedEntity<T>) null);
        }
        return log.traceExit((ComposedEntity<T>) value);
    }

    @Override
    public <T extends Entity<?>, U> String getConditionParameterId(
            ConditionParameter<T, U> condParam) {
        log.traceEntry("{}", condParam);
        ComposedEntity<T> value = this.getConditionParameterValue(condParam);
        if (value == null) {
            return log.traceExit((String) null);
        }
        return log.traceExit(value.getId());
    }

    /**
     * @return   A {@code Map} where keys are {@code DataType}s, the associated values being 
     *           {@code BigDecimal}s corresponding to the max rank for this data type,
     *           solely in this condition, not taking into account child conditions.
     */
    public Map<DataType, BigDecimal> getMaxRanksByDataType() {
        return maxRanksByDataType;
    }
    /**
     * @return  A {@code Map} where keys are {@code DataType}s, the associated values being
     *          {@code BigDecimal}s corresponding to the max rank for this data type,
     *          taking into account this condition, but also all its child conditions.
     */
    public Map<DataType, BigDecimal> getGlobalMaxRanksByDataType() {
        return globalMaxRanksByDataType;
    }

    /**
     * Determine whether the other {@code Condition} is more precise than this {@code Condition}. 
     * This method is only used for convenience, and actually delegates to 
     * {@link ConditionGraph#isConditionMorePrecise(Condition, Condition)}, with this {@code Condition} 
     * as first argument, and {@code other} as second argument. See this other method's description 
     * for more information.
     * 
     * @param other     A {@code Condition} to be checked whether it is more precise 
     *                  than this {@code Condition}.
     * @param graph     A {@code ConditionGraph} used to determine relations between {@code Condition}s. 
     *                  It should contain this {@code Condition} and {@code other}.
     * @return          {@code true} if {@code other} is more precise than this {@code Condition}. 
     * @throws IllegalArgumentException If this {@code Condition}, or {@code other}, are not registered to 
     *                                  {@code graph}.
     */
    public boolean isConditionMorePrecise(Condition other, ConditionGraph graph) throws IllegalArgumentException {
        throw log.throwing(new UnsupportedOperationException(
                "ConditionGraph needs to be reimplemented to manage this new type of Condition"));
    }

    //****************************
    //maxRanksByDataType and globalMaxRanksByDataType not taken into account in hashCode/equals,
    //so we don't need to reimplement them
    //****************************

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Condition2 [")
               .append("conditionParameterObjects=").append(conditionParameterObjects)
               .append(", species=").append(getSpecies())
               .append(", maxRanksByDataType=").append(maxRanksByDataType)
               .append(", globalMaxRanksByDataType=").append(globalMaxRanksByDataType)
               .append("]");
        return builder.toString();
    }
}
