package org.bgee.model.expressiondata;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.NamedEntity;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;
import org.bgee.model.species.Species;

public abstract class BaseCondition2 implements Comparable<BaseCondition2> {
    private final static Logger log = LogManager.getLogger(BaseCondition2.class.getName());

    /**
     * A {@code Comparator} of {@code BaseCondition2}s used for {@link #compareTo(BaseCondition2)}.
     */
    protected static final Comparator<BaseCondition2> COND_COMPARATOR = ConditionParameter.allOf().stream()
            .map(param -> Comparator.<BaseCondition2, String>comparing(
                    (cond) -> cond.getConditionParameterId(param),
                    Comparator.nullsLast(String::compareTo)))
            .reduce((a, b) -> a.thenComparing(b))
            .orElseThrow(() -> new IllegalStateException(
                    "ConditionParameter.allOf() always return values."))
            .thenComparing(c -> c.getSpecies().getId(), Comparator.nullsLast(Integer::compareTo));


    protected final Map<ConditionParameter<?, ?>, ? extends Object> conditionParameterObjects;
    private final Species species;

    public BaseCondition2(Map<ConditionParameter<?, ?>, ? extends Object> conditionParameterObjects,
            Species species) {
        if (conditionParameterObjects == null || conditionParameterObjects.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "Some condition parameter objects must be provided"));
        }
        if (conditionParameterObjects.entrySet().stream()
                .anyMatch(e -> e.getKey() == null || e.getValue() == null)) {
            throw log.throwing(new IllegalArgumentException(
                    "No condition parameter key or object can be null"));
        }
        //Check deactivated on purpose
        if (species == null) {
            throw log.throwing(new IllegalArgumentException("The species cannot be null."));
        }
        //Specific subclasses will have to check the validity of the class types
        //in values, in relation to their key
        this.conditionParameterObjects = Collections.unmodifiableMap(
                new HashMap<>(conditionParameterObjects));
        this.species = species;
    }

    //Subclasses will have to override this method to use the correct generic type
    //of ConditionParameter for having the proper returned class in method signature
    public Object getUncastConditionParameterValue(ConditionParameter<?, ?> condParam) {
        return conditionParameterObjects.get(condParam);
    }
    //We could still another generic type to ConditionParameter if we wanted to specify
    //the type of ID of each condition parameter value
    public abstract <T extends NamedEntity<?>, U> String getConditionParameterId(
            ConditionParameter<T, U> condParam);

    public Species getSpecies() {
        return species;
    }
    public int getSpeciesId() {
        return species.getId();
    }

    @Override
    /**
     * throws IllegalArgumentException  if {@code other} is {@code null}
     *                                  or of a different class than this object.
     */
    public int compareTo(BaseCondition2 other) throws IllegalArgumentException {
        if (other == null) {
            throw log.throwing(new IllegalArgumentException("other cannot be null"));
        }
        if (!this.getClass().equals(other.getClass())) {
            throw log.throwing(new IllegalArgumentException("wrong type"));
        }
        return COND_COMPARATOR.compare(this, other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(conditionParameterObjects, species);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BaseCondition2 other = (BaseCondition2) obj;
        return Objects.equals(conditionParameterObjects, other.conditionParameterObjects)
                && Objects.equals(species, other.species);
    }
}