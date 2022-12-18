package org.bgee.model.expressiondata.call;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.BaseConditionFilter;

public abstract class ConditionFilterCondParam<T extends Enum<T>> extends BaseConditionFilter<Condition> {
    private final static Logger log = LogManager.getLogger(ConditionFilterCondParam.class.getName());

    private final EnumSet<T> observedCondForParams;

    private final Set<Integer> speciesIds;
    /**
     * @see #getSexeIds()
     */
    private final Set<String> sexIds;
    /**
     * @see #getStrainIds()
     */
    private final Set<String> strainIds;

    //XXX: Should we add two booleans to ask for considering sub-structures and sub-stages?
    //Because it seems it can be managed through query of data propagation in CallFilter
    //XXX: should we accept Sex as arguments rather than Strings for sexes?
    protected ConditionFilterCondParam(Collection<Integer> speciesIds, Collection<String> anatEntityIds,
            Collection<String> devStageIds, Collection<String> cellTypeIds,
            Collection<String> sexIds, Collection<String> strainIds,
            Collection<T> observedCondForParams, Class<T> condParamType) throws IllegalArgumentException {
        super(anatEntityIds, devStageIds, cellTypeIds);
        if ((speciesIds == null || speciesIds.isEmpty()) &&
                (anatEntityIds == null || anatEntityIds.isEmpty()) &&
                (devStageIds == null || devStageIds.isEmpty()) &&
                (cellTypeIds == null || cellTypeIds.isEmpty()) &&
                (sexIds == null || sexIds.isEmpty()) &&
                (strainIds == null || strainIds.isEmpty()) &&
                (observedCondForParams == null || observedCondForParams.isEmpty())) {
            throw log.throwing(new IllegalArgumentException("Some species IDs, anatatomical entity IDs, "
                + "developmental stage IDs, cell type IDs, sexe, strain IDs or observed data "
                + "status must be provided."));
        }
        this.speciesIds = Collections.unmodifiableSet(speciesIds == null?
                new HashSet<>(): new HashSet<>(speciesIds));
        this.sexIds = Collections.unmodifiableSet(sexIds == null? 
                new HashSet<>(): new HashSet<>(sexIds));
        this.strainIds = Collections.unmodifiableSet(strainIds == null? 
                new HashSet<>(): new HashSet<>(strainIds));
        this.observedCondForParams = observedCondForParams == null || observedCondForParams.isEmpty()?
                EnumSet.noneOf(condParamType): EnumSet.copyOf(observedCondForParams);
    }

    public Set<Integer> getSpeciesIds() {
        return speciesIds;
    }

    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the sexes that this 
     * {@code ConditionFilter} will specify to use.
     */
    public Set<String> getSexIds() {
        return sexIds;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the strains that this 
     * {@code ConditionFilter} will specify to use.
     */
    public Set<String> getStrainIds() {
        return strainIds;
    }

    /**
     * @return  An {@code EnumSet} of condition parameters, allowing
     *          to request that the conditions considered should have been observed
     *          in data annotations (not created only from propagation),
     *          using the specified condition parameters to perform the check.
     *          For instance, if this {@code EnumSet} contains only the parameter
     *          "anat. entity", any condition using an anat. entity
     *          used in an annotation will be valid (but of course, the other attributes of this
     *          {@code ConditionFilter} will also be considered). If empty,
     *          no filtering will be performed on whether the global conditions considered
     *          have been observed in annotations.
     */
    public EnumSet<T> getObservedCondForParams() {
        return observedCondForParams;
    }

    @Override
    public boolean areAllCondParamFiltersEmpty() {
        log.traceEntry();
        return log.traceExit(this.getAnatEntityIds().isEmpty() &&
                this.getDevStageIds().isEmpty() &&
                this.getCellTypeIds().isEmpty() &&
                this.getSexIds().isEmpty() &&
                this.getStrainIds().isEmpty() &&
                this.getObservedCondForParams().isEmpty());
    }

    public String toParamString() {
        log.traceEntry();
        StringBuilder sb = new StringBuilder();
        boolean previousParams = false;
        if (!getAnatEntityIds().isEmpty()) {
            sb.append(getAnatEntityIds().stream().sorted().collect(Collectors.joining("_"))
                    .replaceAll(" ", "_").replaceAll(":", "_"));
            previousParams = true;
        }
        if (!getDevStageIds().isEmpty()) {
            if (previousParams) {
                sb.append("_");
            }
            sb.append(getDevStageIds().stream().sorted().collect(Collectors.joining("_"))
                    .replaceAll(" ", "_").replaceAll(":", "_"));
            previousParams = true;
        }
        if (!getCellTypeIds().isEmpty()) {
            if (previousParams) {
                sb.append("_");
            }
            sb.append(getCellTypeIds().stream().sorted().collect(Collectors.joining("_"))
                    .replaceAll(" ", "_").replaceAll(":", "_"));
            previousParams = true;
        }
        if (!getSexIds().isEmpty()) {
            if (previousParams) {
                sb.append("_");
            }
            sb.append(getSexIds().stream().sorted().collect(Collectors.joining("_"))
                    .replaceAll(" ", "_").replaceAll(":", "_"));
            previousParams = true;
        }
        if (!getStrainIds().isEmpty()) {
            if (previousParams) {
                sb.append("_");
            }
            sb.append(getStrainIds().stream().sorted().collect(Collectors.joining("_"))
                    .replaceAll(" ", "_").replaceAll(":", "_"));
            previousParams = true;
        }
        if (!getSpeciesIds().isEmpty()) {
            if (previousParams) {
                sb.append("_");
            }
            sb.append(getSpeciesIds().stream().sorted().map(i -> i.toString())
                    .collect(Collectors.joining("_"))
                    .replaceAll(" ", "_").replaceAll(":", "_"));
            previousParams = true;
        }
        return log.traceExit(sb.toString());
    }

    //Since we cannot use the attribute "observedConditions" to check for the validity of the Condition.

    /**
     * Evaluates this {@code ConditionFilter} on the given {@code Condition}.
     * 
     * @param condition A {@code Condition} that is the condition to be evaluated.
     * @return          {@code true} if the {@code condition} matches the {@code ConditionFilter}.
     */
    @Override
    public boolean test(Condition condition) {
        log.traceEntry("{}", condition);

        if (!super.test(condition)) {
            return log.traceExit(false);
        }

        // Check Sex ID
        if (condition.getSex() != null 
            && this.getSexIds() != null && !this.getSexIds().isEmpty()
            && !this.getSexIds().contains(condition.getSex().getId())) {
            log.debug("Sex {} not validated: not in {}",
                condition.getSex().getId(), this.getSexIds());
            return log.traceExit(false);
        }
        // Check Strain ID 
        if (condition.getStrain() != null 
            && this.getStrainIds() != null && !this.getStrainIds().isEmpty()
            && !this.getStrainIds().contains(condition.getStrain().getId())) {
            log.debug("Strain {} not validated: not in {}",
                condition.getStrain().getId(), this.getStrainIds());
            return log.traceExit(false);
        }
        // Check Species ID
        if (condition.getSpecies() != null
            && this.getSpeciesIds() != null && !this.getSpeciesIds().isEmpty()
            && !this.getSpeciesIds().contains(condition.getSpecies().getId())) {
            log.debug("Species {} not validated: not in {}",
                condition.getSpecies().getId(), this.getSpeciesIds());
            return log.traceExit(false);
        }
        
        return log.traceExit(true);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(observedCondForParams, sexIds, speciesIds, strainIds);
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
        ConditionFilterCondParam<?> other = (ConditionFilterCondParam<?>) obj;
        return Objects.equals(observedCondForParams, other.observedCondForParams)
                && Objects.equals(sexIds, other.sexIds) && Objects.equals(speciesIds, other.speciesIds)
                && Objects.equals(strainIds, other.strainIds);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ConditionFilter [anatEntityIds=").append(getAnatEntityIds())
               .append(", devStageIds=").append(getDevStageIds())
               .append(", cellTypeIds=").append(getCellTypeIds())
               .append(", sexIds=").append(getSexIds())
               .append(", strainIds=").append(getStrainIds())
               .append(", speciesIds=").append(getSpeciesIds())
               .append(", observedCondForParams=").append(observedCondForParams).append("]");
        return builder.toString();
    }
}
