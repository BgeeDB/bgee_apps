package org.bgee.model.expressiondata.querytools;

import java.util.Set;

import org.bgee.model.expressiondata.CallData;
import org.bgee.model.expressiondata.Condition;

public class CallFilter {
    //XXX: where to manage speciesIds? in a class GeneFilter? in a class ConditionFilter? in this class?
    //It should not be valid to provide several speciesIds, this should be formally managed 
    //by a multi-species filter, for managing organ homology and gene orthology.
    private final GeneFilter geneFilter;
    
    //XXX: each CallDataConditionFilter represents an AND condition.
    private final Set<CallDataConditionFilter> callDataConditionFilters;
    
    //---------------------
    
    private Set<CallData<?>> callData;
    
    //XXX: here, it means that the DataType of CallData will be null. Is it valid? 
    //Or should we remove the DataType attribute from CallData, and always use a Map 
    //when we want to associate a CallData to a DataType?
    public CallFilter(CallData<?> anyDataTypeCallData) {
        
    }
    //XXX: Map<Datatype, CallData<?>> if we remove DataType field from CallData?
    public CallFilter(Set<CallData<?>> callData) {
        
    }
    public CallFilter(Set<String> geneIds, Set<CallData<?>> callData) {
        
    }
}
