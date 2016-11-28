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
    private final Set<String> anatEntitieIds;
    /**
     * @see #getDevStageIds()
     */
    private final Set<String> devStageIds;

    /**
     * @param anatEntitieIds    A {@code Collection} of {@code String}s that are the IDs 
     *                          of the anatomical entities that this {@code ConditionFilter} 
     *                          will specify to use.
     * @param devStageIds       A {@code Collection} of {@code String}s that are the IDs 
     *                          of the developmental stages that this {@code ConditionFilter} 
     *                          will specify to use.
     * @throws IllegalArgumentException If no anatomical entity IDs, no developmental stage IDs,
     *                                  and no species ID are provided. 
     */
    public ConditionFilter(Collection<String> anatEntitieIds, Collection<String> devStageIds)
            throws IllegalArgumentException {
        if ((anatEntitieIds == null || anatEntitieIds.isEmpty()) && 
                (devStageIds == null || devStageIds.isEmpty())) {
            throw log.throwing(new IllegalArgumentException("Some anatatomical entity IDs,"
                    + " developmental stage IDs or species IDs must be provided."));
        }
        this.anatEntitieIds = Collections.unmodifiableSet(anatEntitieIds == null ? 
                new HashSet<>(): new HashSet<>(anatEntitieIds));
        this.devStageIds = Collections.unmodifiableSet(devStageIds == null? 
                new HashSet<>(): new HashSet<>(devStageIds));
    }


    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the IDs 
     *          of the anatomical entities that this {@code ConditionFilter} will specify to use.
     */
    public Set<String> getAnatEntityIds() {
        return anatEntitieIds;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the IDs 
     *          of the developmental stages that this {@code ConditionFilter} will specify to use.
     */
    public Set<String> getDevStageIds() {
        return devStageIds;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((anatEntitieIds == null) ? 0 : anatEntitieIds.hashCode());
        result = prime * result + ((devStageIds == null) ? 0 : devStageIds.hashCode());
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
        if (anatEntitieIds == null) {
            if (other.anatEntitieIds != null) {
                return false;
            }
        } else if (!anatEntitieIds.equals(other.anatEntitieIds)) {
            return false;
        }
        if (devStageIds == null) {
            if (other.devStageIds != null) {
                return false;
            }
        } else if (!devStageIds.equals(other.devStageIds)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ConditionFilter [anatEntitieIds=" + anatEntitieIds 
                + ", devStageIds=" + devStageIds + "]";
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
            log.debug("Dev. stage {} not validated: ", condition.getDevStageId());
            isValid = false;
        }
    
        // Check anat. entity ID 
        if (condition.getAnatEntityId() != null 
            && this.getAnatEntityIds() != null && !this.getAnatEntityIds().isEmpty()
            && !this.getAnatEntityIds().contains(condition.getAnatEntityId())) {
            log.debug("Anat. entity {} not validated", condition.getAnatEntityId());
            isValid = false;
        }
        
        return log.exit(isValid);
    }
}
