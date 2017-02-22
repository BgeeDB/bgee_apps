package org.bgee.model.anatdev;

import java.util.Set;

import org.bgee.model.Entity;

/**
 * This class represents a similarity group of anatomical entities.
 * 
 * @author  Philippe Moret
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Aug. 2016
 * @since   Bgee 13, Apr. 2016
 */
public class AnatEntitySimilarity extends Entity<String> {
    
    /**
     * @return The {@code Set} of {@code String}s that are anatomical entity IDs of this group.
     */
    private final Set<String> anatEntityIds;
    
    //XXX: we might need to include these fields this at some point
    //private String cioId;    
    //private String taxonId;
    
    /**
     * Constructor providing the ID of this {@code AnatEntitySimilarity} and 
     * the IDs of anat. entities constituting that group.
     * 
     * @param id            A {@code String} that is the ID of this {@code AnatEntitySimilarity}.
     * @param anatEntityIds A {@code Set} of {@code String}s that are anatomical entity IDs of this group.
     */
    public AnatEntitySimilarity(String id, Set<String> anatEntityIds) {
        super(id);
        this.anatEntityIds = anatEntityIds;
    }

    /**
     * @return The {@code Set} of {@code String}s that are anatomical entity IDs of this group.
     */
    public Set<String> getAnatEntityIds() {
        return anatEntityIds;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((anatEntityIds == null) ? 0 : anatEntityIds.hashCode());
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
        AnatEntitySimilarity other = (AnatEntitySimilarity) obj;
        if (anatEntityIds == null) {
            if (other.anatEntityIds != null) {
                return false;
            }
        } else if (!anatEntityIds.equals(other.anatEntityIds)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + " - Anat. entity IDs: " + getAnatEntityIds();
    }
}
