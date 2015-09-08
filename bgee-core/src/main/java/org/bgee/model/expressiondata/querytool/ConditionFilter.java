package org.bgee.model.expressiondata.querytool;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ConditionFilter {
    private final Set<String> anatEntitieIds;
    private final Set<String> devStageIds;
    
    public ConditionFilter(Set<String> anatEntitieIds, Set<String> devStageIds) {
        this.anatEntitieIds = Collections.unmodifiableSet(
            anatEntitieIds == null ? new HashSet<String>(): new HashSet<String>(anatEntitieIds));
        this.devStageIds = Collections.unmodifiableSet(
                devStageIds == null ? new HashSet<String>(): new HashSet<String>(devStageIds));
    }
}
