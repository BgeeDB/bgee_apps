package org.bgee.model.anatdev;

import java.util.Set;

import org.bgee.model.Entity;

/**
 * This class represents a group of developmental stages.
 * 
 * @author  Philippe Moret
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Aug. 2016 
 * @since   Bgee 13, Apr. 2016 
 */
public class DevStageSimilarity extends Entity<String> {
    
    /**
     * A {@code Set} of {@code String}s that are developmental stage IDs of this group. 
     */
    private final Set<String> devStageIds;
    
    /**
     * Constructor providing the ID of this {@code DevStageSimilarity} and 
     * the IDs of dev. stages constituting that group.
     * 
     * @param id            A {@code String} that is the ID of this {@code DevStageSimilarity}.
     * @param devStageIds   A {@code Set} of {@code String}s that are dev. stage IDs of this group.
     */
    public DevStageSimilarity(String id, Set<String> devStageIds) {
        super(id);
        this.devStageIds = devStageIds;
    }
    
    /**
     * @return The {@code Set} of {@code String}s that are developmental stage IDs of this group.
     */
    public Set<String> getDevStageIds() {
        return devStageIds;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((devStageIds == null) ? 0 : devStageIds.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DevStageSimilarity other = (DevStageSimilarity) obj;
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
        return super.toString() + " - Dev. stage IDs: " + getDevStageIds();
    }
}
