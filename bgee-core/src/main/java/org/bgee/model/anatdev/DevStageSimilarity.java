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
public class DevStageSimilarity {
    
    /**
     * A {@code Set} of {@code String}s that are developmental stage IDs of this group. 
     */
    private final Set<String> devStageIds;
    private final String taxonId;
    
    /**
     * Constructor providing the ID of this {@code DevStageSimilarity} and 
     * the IDs of dev. stages constituting that group.
     * 
     * @param id            A {@code String} that is the ID of this {@code DevStageSimilarity}.
     * @param devStageIds   A {@code Set} of {@code String}s that are dev. stage IDs of this group.
     */
    public DevStageSimilarity(String taxonId, Set<String> devStageIds) {
        this.taxonId = taxonId;
        this.devStageIds = devStageIds;
    }
    
    /**
     * @return The {@code Set} of {@code String}s that are developmental stage IDs of this group.
     */
    public Set<String> getDevStageIds() {
        return devStageIds;
    }
    
    /**
     * @return The {@code String} that is the taxon ID of this similarity group.
     */
    public String getTaxonId() {
        return taxonId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((taxonId == null) ? 0 : taxonId.hashCode());
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
        builder.append("DevStageSimilarity [dev. stage IDs = ").append(getDevStageIds())
        	.append(", taxonId = ").append(taxonId)
        	.append("]");
        return builder.toString();
    }
}
