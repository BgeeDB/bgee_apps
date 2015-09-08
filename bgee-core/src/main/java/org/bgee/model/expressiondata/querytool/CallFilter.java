package org.bgee.model.expressiondata.querytool;

import java.util.Set;

import org.bgee.model.expressiondata.CallData;
import org.bgee.model.expressiondata.Condition;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.species.TaxonomyFilter;

//XXX: 
// - if both IDs and multiple species requested AND forceHomology is true 
//   => find missing orthologous genes/homologous organs/comparable stages
public class CallFilter {
    private final GeneFilter geneFilter;
    
    //XXX: each CallDataConditionFilter represents an AND condition.
    private final Set<CallDataConditionFilter> callDataConditionFilters;
    
    //XXX: impact both the gene filtering and the anat.entity and stage filtering.
    //Species should be always explicitly targeted.
    private final TaxonomyFilter taxonFilter;
    
    //XXX: with this boolean set to true, any multi-species query will search explicitly 
    //for homology/orthology relations, and will complete ID list provided to potentially 
    //add homolog/orthologs (i.e., impacting both ConditionFilters and GeneFilters).
    //if false, then any query is possible, without caring about homology/orthology.
    //XXX: If true, retrieve results only in homologous structure/comparable stages, always.
    private final boolean forceHomology;
    
    
    //XXX: here, it means that the DataType of CallData will be null. Is it valid? 
    //Or should we remove the DataType attribute from CallData, and always use a Map 
    //when we want to associate a CallData to a DataType?
//    public CallFilter(CallData<?> anyDataTypeCallData) {
//        
//    }
//    //XXX: Map<Datatype, CallData<?>> if we remove DataType field from CallData?
//    public CallFilter(Set<CallData<?>> callData) {
//        
//    }
//    public CallFilter(Set<String> geneIds, Set<CallData<?>> callData) {
//        
//    }
    
    public CallFilter() {
        this.geneFilter = null;
        this.callDataConditionFilters = null;
        this.taxonFilter = null;
        this.forceHomology = true;
    }
}
