package org.bgee.model.expressiondata.querytools;

import java.util.Set;

import org.bgee.model.expressiondata.CallData;
import org.bgee.model.expressiondata.DataDeclaration.CallType;

//XXX: do we need this class, or should we simply use a Map<ConditionFilter, Set<CallData>>?
public class CallDataConditionFilter {
    private final ConditionFilter conditionFilter;
    //XXX: all CallData would be OR conditions.
    //The only type of query not easily doable would be: 
    //affymetrixData = expressed high && rnaSeqData = expressed high
    private final Set<CallData<? extends CallType>> callDataFilters;
}
