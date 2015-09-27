package org.bgee.model.expressiondata.querytool;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ConditionFilter {
    private final Set<String> devStageIds;
    
    public ConditionFilter(Set<String> devStageIds) {
        this.devStageIds = Collections.unmodifiableSet(
                devStageIds == null ? new HashSet<String>(): new HashSet<String>(devStageIds));
    }

    /**
     * @return the devStageIds
     */
    public Set<String> getDevStageIds() {
        return devStageIds;
    }
}
