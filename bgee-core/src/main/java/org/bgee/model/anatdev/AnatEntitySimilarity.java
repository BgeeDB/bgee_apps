package org.bgee.model.anatdev;

import java.util.Set;

/**
 * This class represents a similarity group of anatomic entities.
 * 
 * @author Philippe Moret
 * @version Bgee 13, Apr. 2016
 * @since   Bgee 13, Apr. 2016
 */
public class AnatEntitySimilarity {
    
    private final String groupId;
    
    private final Set<String> anatEntityIds;
    
    //XXX: we might need to include these fields this at some point
    //private String cioId;    
    //private String taxonId;
    
    public AnatEntitySimilarity(String groupId, Set<String> anatEntityIds) {
        this.groupId = groupId;
        this.anatEntityIds = anatEntityIds;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((anatEntityIds == null) ? 0 : anatEntityIds.hashCode());
        result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
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
        AnatEntitySimilarity other = (AnatEntitySimilarity) obj;
        if (anatEntityIds == null) {
            if (other.anatEntityIds != null) {
                return false;
            }
        } else if (!anatEntityIds.equals(other.anatEntityIds)) {
            return false;
        }
        if (groupId == null) {
            if (other.groupId != null) {
                return false;
            }
        } else if (!groupId.equals(other.groupId)) {
            return false;
        }
        return true;
    }
    
    

    /**
     * @return the {@code String} groupId
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * @return the {@code Set} of anatEntityIds
     */
    public Set<String> getAnatEntityIds() {
        return anatEntityIds;
    }
}
