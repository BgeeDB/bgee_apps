package org.bgee.model.expressiondata;

import java.util.Set;

import org.bgee.model.expressiondata.baseelements.CallType;

//XXX: all parameters are OR conditions
public class CallDataConditionFilter {
    private final Set<ConditionFilter> conditionFilters;
    //XXX: all CallData would be OR conditions.
    //The only type of query not easily doable would be: 
    //affymetrixData = expressed high && rnaSeqData = expressed high
    //XXX: should we force the CallData to be either ExpressionCallData or DiffExpressionCallData 
    //using generic types? And even, for the ExpressionCallData, should we force the call type 
    //to be either EXPRESSED or NOT_EXPRESSED. They are not in the same tables, so this would help...
    //XXX: again, where to accept the diffExpressionFactor
    private final Set<CallData<? extends CallType>> callDataFilters;
    
    public CallDataConditionFilter() {
        this.conditionFilters = null;
        this.callDataFilters = null;
    }
}
