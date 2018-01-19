package org.bgee.model.expressiondata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A filter to parameterize queries using expression data conditions. 
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 13, July 2016
 * @since   Bgee 13, Oct. 2015
 */
public class ConditionFilter implements Predicate<Condition> {
    private final static Logger log = LogManager.getLogger(ConditionFilter.class.getName());
    
    /**
     * @see #getAnatEntityIds()
     */
    private final Set<String> anatEntityIds;
    /**
     * @see #getDevStageIds()
     */
    private final Set<String> devStageIds;
    /**
     * @see #getObservedConditions()
     */
    private final Boolean observedConditions;

    /**
     * @param anatEntityIds        A {@code Collection} of {@code String}s that are the IDs 
     *                              of the anatomical entities that this {@code ConditionFilter} 
     *                              will specify to use.
     * @param devStageIds           A {@code Collection} of {@code String}s that are the IDs 
     *                              of the developmental stages that this {@code ConditionFilter} 
     *                              will specify to use.
     * @throws IllegalArgumentException If no anatomical entity IDs nor developmental stage IDs
     *                                  are provided. 
     */
    public ConditionFilter(Collection<String> anatEntityIds, Collection<String> devStageIds)
            throws IllegalArgumentException {
        this(anatEntityIds, devStageIds, null);
    }
    /**
     * @param anatEntityIds        A {@code Collection} of {@code String}s that are the IDs 
     *                              of the anatomical entities that this {@code ConditionFilter} 
     *                              will specify to use.
     * @param devStageIds           A {@code Collection} of {@code String}s that are the IDs 
     *                              of the developmental stages that this {@code ConditionFilter} 
     *                              will specify to use.
     * @param observedConditions    A {@code Boolean} defining whether the conditions considered
     *                              should have been observed in expression data in any species.
     *                              See {@link #getObservedConditions()} for more details.
     * @throws IllegalArgumentException If no anatomical entity IDs nor developmental stage IDs
     *                                  nor observed status are provided. 
     */
    //XXX: Should we add two booleans to ask for considering sub-structures and sub-stages?
    //Because it seems it can be managed through query of data propagation in CallFilter
    public ConditionFilter(Collection<String> anatEntityIds, Collection<String> devStageIds,
            Boolean observedConditions) throws IllegalArgumentException {
        if ((anatEntityIds == null || anatEntityIds.isEmpty()) && 
                (devStageIds == null || devStageIds.isEmpty()) &&
                observedConditions == null) {
            throw log.throwing(new IllegalArgumentException("Some anatatomical entity IDs"
                    + " or developmental stage IDs or observed data status must be provided."));
        }
        this.anatEntityIds = Collections.unmodifiableSet(anatEntityIds == null ? 
                new HashSet<>(): new HashSet<>(anatEntityIds));
        this.devStageIds = Collections.unmodifiableSet(devStageIds == null? 
                new HashSet<>(): new HashSet<>(devStageIds));
        this.observedConditions = observedConditions;
    }


    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the IDs 
     *          of the anatomical entities that this {@code ConditionFilter} will specify to use.
     */
    public Set<String> getAnatEntityIds() {
        return anatEntityIds;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the IDs 
     *          of the developmental stages that this {@code ConditionFilter} will specify to use.
     */
    public Set<String> getDevStageIds() {
        return devStageIds;
    }
    /**
     * @return  A {@code Boolean} defining whether the conditions considered should have been
     *          observed in expression data in any species. If {@code true}, only conditions
     *          observed in expression data in any species are considered, not resulting
     *          only from a data propagation; if {@code false}, only conditions resulting
     *          from data propagation, never observed in expression data of any species,
     *          are considered; if {@code null}, conditions are considered whatever
     *          their observed data status.
     */
    public Boolean getObservedConditions() {
        return observedConditions;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((anatEntityIds == null) ? 0 : anatEntityIds.hashCode());
        result = prime * result + ((devStageIds == null) ? 0 : devStageIds.hashCode());
        result = prime * result + ((observedConditions == null) ? 0 : observedConditions.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ConditionFilter other = (ConditionFilter) obj;
        if (anatEntityIds == null) {
            if (other.anatEntityIds != null) {
                return false;
            }
        } else if (!anatEntityIds.equals(other.anatEntityIds)) {
            return false;
        }
        if (devStageIds == null) {
            if (other.devStageIds != null) {
                return false;
            }
        } else if (!devStageIds.equals(other.devStageIds)) {
            return false;
        }
        if (observedConditions == null) {
            if (other.observedConditions != null) {
                return false;
            }
        } else if (!observedConditions.equals(other.observedConditions)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ConditionFilter [anatEntityIds=").append(anatEntityIds)
               .append(", devStageIds=").append(devStageIds)
               .append(", observedConditions=").append(observedConditions).append("]");
        return builder.toString();
    }


    /**
     * Evaluates this {@code ConditionFilter} on the given {@code Condition}.
     * 
     * @param condition A {@code Condition} that is the condition to be evaluated.
     * @return          {@code true} if the {@code condition} matches the {@code ConditionFilter}.
     */
    @Override
    public boolean test(Condition condition) {
        log.entry(condition);

        boolean isValid = true;
        
        // Check dev. stage ID 
        if (condition.getDevStageId() != null 
            && this.getDevStageIds() != null && !this.getDevStageIds().isEmpty()
            && !this.getDevStageIds().contains(condition.getDevStageId())) {
            log.debug("Dev. stage {} not validated: not in {}",
                condition.getDevStageId(), this.getDevStageIds());
            isValid = false;
        }
    
        // Check anat. entity ID 
        if (condition.getAnatEntityId() != null 
            && this.getAnatEntityIds() != null && !this.getAnatEntityIds().isEmpty()
            && !this.getAnatEntityIds().contains(condition.getAnatEntityId())) {
            log.debug("Anat. entity {} not validated: not in {}",
                condition.getAnatEntityId(), this.getAnatEntityIds());
            isValid = false;
        }
        
        return log.exit(isValid);
    }
}
