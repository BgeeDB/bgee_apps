package org.bgee.model.expressiondata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.Condition.ConditionEntities;

/**
 * A filter to parameterize queries using expression data {@link Condition}s.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 15.0, Mar. 2021
 * @since   Bgee 13, Oct. 2015
 */
//TODO: be able to EXCLUDE anat. entities/stages. It would be convenient to discard
//non-informative anat. entities.
public class ConditionFilter extends BaseConditionFilter<Condition> {
    private final static Logger log = LogManager.getLogger(ConditionFilter.class.getName());

    /**
     * @see #getSexeIds()
     */
    private final Set<String> sexIds;
    /**
     * @see #getStrainIds()
     */
    private final Set<String> strainIds;
    /**
     * @see #getObservedConditions()
     */
    private final Boolean observedConditions;

    /**
     * @param anatEntityIds        A {@code Collection} of {@code String}s that are the IDs 
     * @param anatEntityIds         A {@code Collection} of {@code String}s that are the IDs 
     *                              of the anatomical entities that this {@code ConditionFilter} 
     *                              will specify to use.
     * @throws IllegalArgumentException If no anatomical entity IDs are provided. 
     */
    public ConditionFilter(Collection<String> anatEntityIds)
            throws IllegalArgumentException {
        this(anatEntityIds, null, null, null, null, null);
    }

    /**
     * @param anatEntityIds         A {@code Collection} of {@code String}s that are the IDs 
     *                              of the anatomical entities that this {@code ConditionFilter} 
     *                              will specify to use.
     * @param devStageIds           A {@code Collection} of {@code String}s that are the IDs 
     *                              of the developmental stages that this {@code ConditionFilter} 
     *                              will specify to use.
     * @param cellTypeIds           A {@code Collection} of {@code String}s that are the IDs 
     *                              of the anatomical entities describing cell types that this 
     *                              {@code ConditionFilter} will specify to use.
     * @param sexes                 A {@code Collection} of {@code String}s that are the names
     *                              of the sexes that this {@code ConditionFilter} will specify 
     *                              to use.
     * @param strains               A {@code Collection} of {@code String}s that are the names
     *                              of the strains that this {@code ConditionFilter} will 
     *                              specify to use.
     * @throws IllegalArgumentException If no anatomical entity IDs nor developmental stage IDs
     *                              are provided. 
     */
    public ConditionFilter(Collection<String> anatEntityIds, Collection<String> devStageIds,
            Collection<String> cellTypeIds, Collection<String> sexes, Collection<String> strains)
            throws IllegalArgumentException {
        this(anatEntityIds, devStageIds, cellTypeIds, sexes, strains, null);
    }
    public ConditionFilter(ConditionEntities condEntities, Boolean observedConditions) {
        this(condEntities.getAnatEntityIds(), condEntities.getDevStageIds(),
                condEntities.getCellTypeIds(), condEntities.getSexIds(),
                condEntities.getStrainIds(), observedConditions);
    }
    /**
     * @param anatEntityIds        A {@code Collection} of {@code String}s that are the IDs 
     *                              of the anatomical entities that this {@code ConditionFilter} 
     *                              will specify to use.
     * @param devStageIds           A {@code Collection} of {@code String}s that are the IDs 
     *                              of the developmental stages that this {@code ConditionFilter} 
     *                              will specify to use.
     * @param cellTypeIds           A {@code Collection} of {@code String}s that are the IDs 
     *                              of the anatomical entities describing cell types that this 
     *                              {@code ConditionFilter} will specify to use.
     * @param sexIds                A {@code Collection} of {@code String}s that are the names
     *                              of the sexes that this {@code ConditionFilter} will specify 
     *                              to use.
     * @param strainIds             A {@code Collection} of {@code String}s that are the names
     *                              of the strains that this {@code ConditionFilter} will 
     *                              specify to use.
     * @param observedConditions    A {@code Boolean} defining whether the conditions considered
     *                              should have been observed in expression data in any species.
     *                              See {@link #getObservedConditions()} for more details.
     * @throws IllegalArgumentException If no anatomical entity IDs nor developmental stage IDs
     *                                  nor observed status are provided. 
     */
    //XXX: Should we add two booleans to ask for considering sub-structures and sub-stages?
    //Because it seems it can be managed through query of data propagation in CallFilter
    public ConditionFilter(Collection<String> anatEntityIds, Collection<String> devStageIds,
            Collection<String> cellTypeIds, Collection<String> sexIds, Collection<String> strainIds, 
            Boolean observedConditions) throws IllegalArgumentException {
        super(anatEntityIds, devStageIds, cellTypeIds);
        if ((anatEntityIds == null || anatEntityIds.isEmpty()) &&
                (devStageIds == null || devStageIds.isEmpty()) &&
                (cellTypeIds == null || cellTypeIds.isEmpty()) &&
                (sexIds == null || sexIds.isEmpty()) &&
                (strainIds == null || strainIds.isEmpty()) &&
                observedConditions == null) {
            throw log.throwing(new IllegalArgumentException("Some anatatomical entity IDs, "
                + "developmental stage IDs, cell type IDs, sexe, strain IDs or observed data "
                + "status must be provided."));
        }
        this.sexIds = Collections.unmodifiableSet(sexIds == null? 
                new HashSet<>(): new HashSet<>(sexIds));
        this.strainIds = Collections.unmodifiableSet(strainIds == null? 
                new HashSet<>(): new HashSet<>(strainIds));
        this.observedConditions = observedConditions;
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
     * @return  A {@code Boolean} defining whether the conditions considered should have been
     *          observed in expression data. If {@code true}, only conditions
     *          observed in expression data are considered, not resulting
     *          only from a data propagation; if {@code false}, only conditions resulting
     *          from data propagation, never observed in expression data,
     *          are considered; if {@code null}, conditions are considered whatever
     *          their observed data status.
     */
    public Boolean getObservedConditions() {
        return observedConditions;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((sexIds == null) ? 0 : sexIds.hashCode());
        result = prime * result + ((strainIds == null) ? 0 : strainIds.hashCode());
        result = prime * result + ((observedConditions == null) ? 0 : observedConditions.hashCode());
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
        ConditionFilter other = (ConditionFilter) obj;
        if (sexIds == null) {
            if (other.sexIds != null)
                return false;
        } else if (!sexIds.equals(other.sexIds))
            return false;
        if (strainIds == null) {
            if (other.strainIds != null)
                return false;
        } else if (!strainIds.equals(other.strainIds))
            return false;
        if (observedConditions == null) {
            if (other.observedConditions != null)
                return false;
        } else if (!observedConditions.equals(other.observedConditions))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ConditionFilter [anatEntityIds=").append(getAnatEntityIds())
               .append(", devStageIds=").append(getDevStageIds())
               .append(", cellTypeIds=").append(getCellTypeIds())
               .append(", sexIds=").append(getSexIds())
               .append(", strainIds=").append(getStrainIds())
               .append(", observedConditions=").append(observedConditions).append("]");
        return builder.toString();
    }


    //Since we cannot use the attribute "observedConditions" to check for the validity of the Condition.

    /**
     * Evaluates this {@code RawDataConditionFilter} on the given {@code RawDataCondition}.
     * 
     * @param condition A {@code RawDataCondition} that is the condition to be evaluated.
     * @return          {@code true} if the {@code condition} matches the {@code RawDataConditionFilter}.
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
        
        return log.traceExit(true);
    }
}