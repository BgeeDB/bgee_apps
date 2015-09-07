package org.bgee.model.expressiondata.querytools;

import java.util.Set;

//XXX: this is a bit frustrating to not use directly the Condition class, 
//but I don't see how to express: expression in (organA || organB || organC) && (stageA || stageB) 
//using plain Condition objects.
public class ConditionFilter {
    private final Set<String> anatEntitieIds;
    private final Set<String> devStageIds;
}
