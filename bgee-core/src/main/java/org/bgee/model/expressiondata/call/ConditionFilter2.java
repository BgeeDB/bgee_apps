package org.bgee.model.expressiondata.call;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.BaseConditionFilter2;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;

public class ConditionFilter2 extends BaseConditionFilter2<Condition2> {
    private final static Logger log = LogManager.getLogger(ConditionFilter2.class.getName());

    private final Set<ConditionParameter<?, ?>> condParamCombination;
    private final Set<ConditionParameter<?, ?>> condParamIncludeChildTerms;
    private final Set<ConditionParameter<?, ?>> observedCondForParams;
    /**
     * @param speciesId             An {@code Integer} that is the IDs
     *                              of the species that this {@code ConditionFilter}
     *                              will specify to use. Can be {@code null},
     *                              unless {@code condParamIncludeChildTerms} is not empty,
     *                              in which case it is mandatory to provide a species ID,
     *                              otherwise an {@code IllegalArgumentException} is thrown.
     * @param condParamToComposedFilterIds  A {@code Map} where keys are {@code ConditionParameter},
     *                                      the associated value being a {@code ComposedFilterIds}s
     *                                      to specify the requested IDs for the related condition parameter.
     *                                      For instance, to retrieve conditions having an anat. entity ID
     *                                      equals to "ID1", this {@code Map} will contain the key
     *                                      {@code ConditionParameter.ANAT_ENTITY}, associated with
     *                                      a {@code ComposedFilterIds} that could have been created by calling
     *                                      {@code ComposedFilterIds.of(Set.of("ID1"))}.
     *                                      The provided argument can be null, or empty, or not contain
     *                                      all {@code ConditionParameter}s, or have {@code null} values.
     *                                      It should not contain {@code null} keys.
     * @param condParamIncludeChildTerms    TODO javodoc: include child terms for requested cond. params.
     *                                      taken into accont for a cond. param only if there are IDs
     *                                      requested for this cond. param in condParamToFilterIds
     * @param condParamCombination          TODO javadoc: the cond parameter combination targeted
     * @param observedCondForParams A {@code Collection} of
     *                              {@code ExpressionCallService.ConditionParameter}s specifying
     *                              that the conditions considered should have been observed
     *                              in data annotations (not created only from propagation),
     *                              using the specified condition parameters to perform the check.
     *                              For instance, if this {@code Collection} contains only the parameter
     *                              {@code ConditionParameter.ANAT_ENTITY}, any condition
     *                              using an anat. entity used in an annotation will be valid
     *                              (but of course, the other attributes of this {@code ConditionFilter}
     *                              will also be considered). If {@code null}
     *                              or empty, no filtering will be performed on whether
     *                              the global conditions considered have been observed in annotations.
     * @throws IllegalArgumentException 
     */
    //XXX: should we use a Map<ConditionParameter<?, ?>, Boolean> condParamIncludeChildTerms
    //or something like that ("Direction" instead of Boolean) to allow to request for the parent terms?
    public ConditionFilter2(Integer speciesId,
            Map<ConditionParameter<?, ?>, ComposedFilterIds<String>> condParamToComposedFilterIds,
            Collection<ConditionParameter<?, ?>> condParamCombination,
            //TODO: the use of condParamIncludeChildTerms could be used to BaseConditionFilter,
            //to be use as well in the new RawDataConditionFilter: in the existing one,
            //we still provide one Boolean per condition parameter.
            Collection<ConditionParameter<?, ?>> condParamIncludeChildTerms,
            Collection<ConditionParameter<?, ?>> observedCondForParams)
                    throws IllegalArgumentException {
        super(speciesId, condParamToComposedFilterIds);
        //Of note, ConditionParameter.copyOf already checks for presence of null elements
        //in the collection
        this.condParamCombination = Collections.unmodifiableSet(
                condParamCombination == null || condParamCombination.isEmpty()?
                ConditionParameter.allOf(): ConditionParameter.copyOf(condParamCombination));
        this.condParamIncludeChildTerms = Collections.unmodifiableSet(
                condParamIncludeChildTerms == null || condParamIncludeChildTerms.isEmpty()?
                ConditionParameter.noneOf(): ConditionParameter.copyOf(condParamIncludeChildTerms));
        this.observedCondForParams = Collections.unmodifiableSet(
                observedCondForParams == null || observedCondForParams.isEmpty()?
                ConditionParameter.noneOf(): ConditionParameter.copyOf(observedCondForParams));
        if (condParamCombination != null && condParamCombination.contains(null) ) {
            throw log.throwing(new IllegalArgumentException(
                    "No ConditionParameter can be null in condParamCombination."));
        }
        if (speciesId == null && this.condParamIncludeChildTerms.stream()
                .anyMatch(param -> !this.getCondParamToComposedFilterIds().get(param).isEmpty())) {
            throw log.throwing(new IllegalArgumentException(
                    "A species ID must be provided if children terms of specified terms are requested."));
        }
    }

    //TODO javadoc
    public Set<ConditionParameter<?, ?>> getCondParamCombination() {
        return condParamCombination;
    }
    //TODO javadoc
    public Set<ConditionParameter<?, ?>> getCondParamIncludeChildTerms() {
        return condParamIncludeChildTerms;
    }
    /**
     * @return  An {@code Set} of condition parameters, allowing
     *          to request that the conditions considered should have been observed
     *          in data annotations (not created only from propagation),
     *          using the specified condition parameters to perform the check.
     *          For instance, if this {@code Set} contains only the parameter
     *          "anat. entity", any condition using an anat. entity
     *          used in an annotation will be valid (but of course, the other attributes of this
     *          {@code ConditionFilter} will also be considered). If empty,
     *          no filtering will be performed on whether the global conditions considered
     *          have been observed in annotations.
     */
    public Set<ConditionParameter<?, ?>> getObservedCondForParams() {
        return observedCondForParams;
    }

    @Override
    public boolean areAllCondParamFiltersEmpty() {
        log.traceEntry();
        return log.traceExit(super.areAllCondParamFiltersEmpty() &&
                this.observedCondForParams.isEmpty());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(condParamCombination,
                condParamIncludeChildTerms, observedCondForParams);
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        ConditionFilter2 other = (ConditionFilter2) obj;
        return Objects.equals(condParamCombination, other.condParamCombination)
                && Objects.equals(condParamIncludeChildTerms, other.condParamIncludeChildTerms)
                && Objects.equals(observedCondForParams, other.observedCondForParams);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ConditionFilter2 [")
               .append("speciesId=").append(getSpeciesId())
               .append(", condParamToFilterIds=").append(getCondParamToComposedFilterIds())
               .append(", condParamCombination=").append(condParamCombination)
               .append(", condParamIncludeChildTerms=").append(condParamIncludeChildTerms)
               .append(", observedCondForParams=").append(observedCondForParams)
               .append("]");
        return builder.toString();
    }
}
