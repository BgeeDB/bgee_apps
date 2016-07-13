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
     * @see #getAnatEntitieIds()
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
     * @throws IllegalArgumentException If no anatomical entity IDs and no developmental stage IDs 
     *                                  are provided. 
     */
    public ConditionFilter(Collection<String> anatEntitieIds, Collection<String> devStageIds) 
            throws IllegalArgumentException {
        if ((anatEntitieIds == null || anatEntitieIds.isEmpty()) && 
                (devStageIds == null || devStageIds.isEmpty())) {
            throw log.throwing(new IllegalArgumentException(
                    "Some anatatomical entity IDs or developmental stage IDs must be provided."));
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
    public Set<String> getAnatEntitieIds() {
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

        if ((this.getAnatEntitieIds() != null && !this.getAnatEntitieIds().isEmpty() && 
                this.getDevStageIds() != null && !this.getDevStageIds().isEmpty())) {
            // Filter has to be apply on anat. entity IDs and dev. stage IDs
            if (this.getAnatEntitieIds().contains(condition.getAnatEntityId()) &&
                    this.getDevStageIds().contains(condition.getDevStageId()) ) {
                return log.exit(true);
            }

        } else  if (this.getAnatEntitieIds() != null && !this.getAnatEntitieIds().isEmpty()) {
            // Filter has to be apply only on anat. entity IDs 
            if (this.getAnatEntitieIds().contains(condition.getAnatEntityId())) {
                return log.exit(true);
            }

        } else  if (this.getDevStageIds() != null && !this.getDevStageIds().isEmpty()) {
            // Filter has to be apply only on dev. stage IDs 
            if (this.getDevStageIds().contains(condition.getDevStageId())) {
                return log.exit(true);
            }
        }
        return log.exit(false);
    }
}
