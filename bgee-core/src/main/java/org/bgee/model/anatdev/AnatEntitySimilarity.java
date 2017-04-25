package org.bgee.model.anatdev;

import java.util.Set;

/**
 * This class represents a similarity group of anatomical entities.
 * 
 * @author  Philippe Moret
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Aug. 2016
 * @since   Bgee 13, Apr. 2016
 */
public class AnatEntitySimilarity {
    
    /**
     * @return The {@code Set} of {@code String}s that are anatomical entity IDs of this group.
     */
    private final Set<String> anatEntityIds;
    
    private String taxonId;
    
    //XXX: we might need to include these fields this at some point
    //private String cioId;    
    //
    
    /**
     * Constructor providing the ID of this {@code AnatEntitySimilarity} and 
     * the IDs of anat. entities constituting that group.
     * 
     * @param id            A {@code String} that is the ID of this {@code AnatEntitySimilarity}.
     * @param anatEntityIds A {@code Set} of {@code String}s that are anatomical entity IDs of this group.
     */
    public AnatEntitySimilarity(String taxonId, Set<String> anatEntityIds) {
        this.taxonId = taxonId;
        this.anatEntityIds = anatEntityIds;
    }

    /**
     * @return The {@code Set} of {@code String}s that are anatomical entity IDs of this group.
     */
    public Set<String> getAnatEntityIds() {
        return anatEntityIds;
    }
    
    /**
     * @return The {@code String}s that is the taxon ID of this similarity group.
     */
    public String getTaxonId() {
        return taxonId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((anatEntityIds == null) ? 0 : anatEntityIds.hashCode());
        result = prime * result + ((taxonId == null) ? 0 : taxonId.hashCode());
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
        if (taxonId == null) {
            if (other.taxonId != null) {
                return false;
            }
        } else if (!taxonId.equals(other.taxonId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
    	StringBuilder builder = new StringBuilder();
        builder.append("AnatEntitySimilarity [Anat. entity IDs = ").append(getAnatEntityIds())
        	.append(", taxonId = ").append(taxonId)
        	.append("]");
        return builder.toString();
    }
}
