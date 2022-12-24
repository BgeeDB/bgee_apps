package org.bgee.model.expressiondata.call;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.bgee.model.NamedEntity;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;

public class ExpressionCallPostFilter {
    private final LinkedHashMap<ConditionParameter<?, ?>, Set<Object>> conditionParameterEntities;

    public ExpressionCallPostFilter(Map<ConditionParameter<?, ?>, Set<Object>> conditionParameterEntities) {
        
        if (conditionParameterEntities != null && conditionParameterEntities.entrySet().stream()
                .anyMatch(e -> e.getKey() == null || e.getValue() == null || e.getValue().stream()
                        .anyMatch(o -> o == null || !e.getKey().getCondValueType().isInstance(o)))) {
            throw new IllegalArgumentException(
                    "Incorrect object for the associated condition parameter: "
                    + conditionParameterEntities);
        }
        this.conditionParameterEntities = ConditionParameter.allOf().stream()
            .collect(Collectors.toMap(
                    param -> param,
                    param -> Collections.unmodifiableSet(
                            (conditionParameterEntities == null? Set.of():
                            conditionParameterEntities.getOrDefault(param, Set.of())).stream()
                            .map(o -> param.getCondValueType().cast(o))
                            .sorted(Comparator.comparing(o -> o.getId()))
                            .collect(Collectors.toCollection(() -> new LinkedHashSet<>()))),
                    (v1, v2) -> {throw new IllegalStateException("No key collision possible");},
                    () -> new LinkedHashMap<>()));
    }

    //We already checked the correct types in the constructor
    public <T extends NamedEntity<?>> LinkedHashSet<T> getEntities(ConditionParameter<T, ?> param) {
        //need to iterate the Stream to cast
        return this.conditionParameterEntities.get(param).stream()
                .map(o -> param.getCondValueType().cast(o))
                .collect(Collectors.toCollection(() -> new LinkedHashSet<>()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(conditionParameterEntities);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ExpressionCallPostFilter other = (ExpressionCallPostFilter) obj;
        return Objects.equals(conditionParameterEntities, other.conditionParameterEntities);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ExpressionCallPostFilter [")
               .append("conditionParameterEntities=").append(conditionParameterEntities)
               .append("]");
        return builder.toString();
    }
}