package org.bgee.model.anatdev;

import java.util.Set;

/**
 * This class represents a group of developmental stages.
 * 
 * @author Philippe Moret
 * @version Bgee 13, Apr 2016 
 * @since Bgee 13, Apr 2016 

 */
public class DevStageSimilarity {
    
    private final String groupId;
    
    private final Set<String> stageIds;
    
    /**
     * 2-args constructor
     * @param groupId       {@code String} representation of the group id
     * @param devStageIds   {@code Set} of stage ids associated to this group
     */
    public DevStageSimilarity(String groupId, Set<String> devStageIds){
        this.groupId = groupId;
        this.stageIds = devStageIds;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((stageIds == null) ? 0 : stageIds.hashCode());
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
        DevStageSimilarity other = (DevStageSimilarity) obj;
        if (stageIds == null) {
            if (other.stageIds != null) {
                return false;
            }
        } else if (!stageIds.equals(other.stageIds)) {
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
     * Gets the group ID
     * @return A {@code String} representation of the group Id
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * The stages that are part of this group
     * @return The {@code Set} of stage ids.
     */
    public Set<String> getStageIds() {
        return stageIds;
    }
}
