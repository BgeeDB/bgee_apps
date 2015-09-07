package org.bgee.model.expressiondata.querytools;

import java.util.Set;

import org.bgee.model.expressiondata.CallData;
import org.bgee.model.expressiondata.Condition;

public class CallQueryFilter {
    //XXX: or create a class GeneFilter?
    //XXX: where to manage speciesIds? in a class GeneFilter? in a class ConditionFilter? in this class?
    private final Set<String> geneIds;
    
    //XXX: or simply use a Map<ConditionFilter, Set<CallData>> instead of the class CallDataConditionFilter?
    private final Set<CallDataConditionFilter> callDataConditionFilters;
    
    //---------------------
    
    //XXX: Or we going to accept several CallFilters? E.g.: 
    //genes expressed in organA or organB (1 CallFiter) and over-expressed in organ C (another CallQueryFilter)
    ///XXX: then maybe we should have a class only to associate a Set of Conditions to a Set of CallData?
    //And accepting a Set of objects of this class in the CallQueryFilter?
    
    //XXX: should we force the CallData to be either ExpressionCallData or DiffExpressionCallData 
    //using generic types? And even, for the ExpressionCallData, should we force the call type 
    //to be either EXPRESSED or NOT_EXPRESSED. They are not in the same tables, so this would help...
    private Set<CallData<?>> callData;
    
    //XXX: here, it means that the DataType of CallData will be null. Is it valid? 
    //Or should we remove the DataType attribute from CallData, and always use a Map 
    //when we want to associate a CallData to a DataType?
    public CallQueryFilter(CallData<?> anyDataTypeCallData) {
        
    }
    //XXX: Map<Datatype, CallData<?>> if we remove DataType field from CallData?
    public CallQueryFilter(Set<CallData<?>> callData) {
        
    }
    public CallQueryFilter(Set<String> geneIds, Set<CallData<?>> callData) {
        
    }
}
