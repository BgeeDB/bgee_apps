package org.bgee.model.expressiondata.querytools;

import java.util.Set;

import org.bgee.model.expressiondata.CallData;
import org.bgee.model.expressiondata.Condition;

public class CallFilter {
    private final Set<String> geneIds;
    
    //XXX: here, how are we going to say: 
    //expression in (organA || organB || organC) && devStageA
    //?
    //With 4 different Condition objects, and using a way to manage and/or?
    private final Set<Condition> conditions;
    
    //or simply one Set per conditon parameter?, e.g.: 
    private final Set<AnatEntity> anatEntities;
    private final Set<DevStage> devStages;
    //and add new fields as the Condition class evolves?
    
    //XXX: Or we going to accept several CallFilters? E.g.: 
    //genes expressed in organA or organB (1 CallFiter) and over-expressed in organ C (another CallFilter)
    ///XXX: then maybe we should have a class only to associate a Set of Conditions to a Set of CallData?
    //And accepting a Set of objects of this class in the CallFilter?
    
    //XXX: callDatas?
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
