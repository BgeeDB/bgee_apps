package org.bgee.model.expressiondata;

import java.util.Set;

import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.gene.GeneFilter;

/**
 * A filter to parameterize queries to {@link CallService}. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Oct. 2015
 * @since Bgee 13 Oct. 2015
 *
 * @param T The type of {@code CallData} to be used by this {@code CallFilter}. 
 *          Can be declared as {@code CallData}, to include a mixture of {@code CallData} subtypes, 
 *          or as a specific subtype, for instance, {@code ExpressionCallData}.
 */
//XXX: would several CallFilters represent AND or OR conditions.
//If OR conditions, we could provide a Set<Set<CallFilter>> to CallService methods, 
//to provide AND/OR conditions.
//IF AND conditions, then we cannot easily target different CallDatas for different ConditionFilters, e.g.: 
//((brain adult and affymetrixQual >= high) OR (liver adult and rnaSeqQual >= high). 
//=> I don't think we'd often like to do such a query, most of the time we would target 
//the same CallDatas. If we really needed it, then we could still do it in several queries 
//(even if it is less optimized).
//=> let's consider several CallFilters as AND conditions for now, and let's see what happens in the future.  
public class CallFilter<T extends CallData<? extends CallType>> {
    //XXX: The only problem with using directly ConditionFilters and CallDatas in this class, 
    //is that GeneFilters are costly to use in a query; using the class CallDataConditionFilter 
    //was allowing to have a same GeneFilter to target several conditions/call data combinations. 
    //Now, the same query would be doable by using several CallFilters, but with a same GeneFilter 
    //reused several times. This is costly, but we could have a mechanism to provide a global GeneFilter 
    //to the DAO when we see it is always the same GeneFilter used. 
    //I think it's worth it for the simplification it allows in the class CallFilter.
    private final GeneFilter geneFilter;
    
    //XXX: all parameters are OR conditions
    private final Set<ConditionFilter> conditionFilters;
    //XXX: all CallData would be OR conditions.
    //The only type of query not easily doable would be: 
    //affymetrixData = expressed high && rnaSeqData = expressed high
    //XXX: should we force the CallData to be either ExpressionCallData or DiffExpressionCallData 
    //using generic types? And even, for the ExpressionCallData, should we force the call type 
    //to be either EXPRESSED or NOT_EXPRESSED. They are not in the same tables, so this would help...
    //XXX: again, where to accept the diffExpressionFactor
    private final Set<T> callDataFilters;
    
    
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
        this.conditionFilters = null;
        this.callDataFilters = null;
    }
}
