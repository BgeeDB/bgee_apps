package org.bgee.model.dao.api.expressiondata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A filter to parameterize queries using expression data conditions. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Oct. 2015
 * @since Bgee 13 Oct. 2015
 */
public class DAOConditionFilter {
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
     *                          of the anatomical entities that this {@code DAOConditionFilter} 
     *                          will specify to use.
     * @param devStageIds       A {@code Collection} of {@code String}s that are the IDs 
     *                          of the developmental stages that this {@code DAOConditionFilter} 
     *                          will specify to use.
     */
    public DAOConditionFilter(Collection<String> anatEntitieIds, Collection<String> devStageIds) {
        this.anatEntitieIds = anatEntitieIds == null ? null: Collections.unmodifiableSet(
                new HashSet<>(anatEntitieIds));
        this.devStageIds = devStageIds == null? null: Collections.unmodifiableSet(
                new HashSet<>(devStageIds));
    }

    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the IDs 
     *          of the anatomical entities that this {@code DAOConditionFilter} will specify to use.
     */
    public Set<String> getAnatEntitieIds() {
        return anatEntitieIds;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the IDs 
     *          of the developmental stages that this {@code DAOConditionFilter} will specify to use.
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
        DAOConditionFilter other = (DAOConditionFilter) obj;
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
        return "DAOConditionFilter [anatEntitieIds=" + anatEntitieIds 
                + ", devStageIds=" + devStageIds + "]";
    }
}
