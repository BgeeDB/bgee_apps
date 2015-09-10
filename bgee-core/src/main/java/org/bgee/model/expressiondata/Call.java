package org.bgee.model.expressiondata;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bgee.model.expressiondata.CallData.DiffExpressionCallData;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.*;

//XXX: and what if it was a multi-species query? Should we use something like a MultiSpeciesCondition?
public abstract class Call<T extends SummaryCallType, U extends CallData<?>> {

    //**********************************************
    //   INNER CLASSES
    //**********************************************
    public static class ExpressionSummaryCall 
        extends Call<ExpressionSummary, ExpressionCallData> {
        
    }
    //XXX: DiffExpressionFactor managed here?
    public static class DiffExpressionSummaryCall 
        extends Call<DiffExpressionSummary, DiffExpressionCallData> {
        
    }
  //**********************************************
    //   INSTANCE ATTRIBUTES AND METHODS
    //**********************************************
    
    private final String geneId;
    
    private final Condition condition;
    
    /**
     * @see #getOverallCallType()
     */
    private final T summaryCallType;
    /**
     * @see #getOverallQuality()
     */
    private final DataQuality summaryQuality;
    
    private final Set<U> callData;
    //XXX: or rather: 
    //private final Map<DataType, U> callDataPerDataTypes;
    //?
    
    private Call() {
        this(null, null, null, null, null);
    }
    protected Call(String geneId, Condition condition, 
            T summaryCallType, DataQuality summaryQual, Set<U> callData) {
        //TODO: sanity checks
        this.geneId = geneId;
        this.condition = condition;
        this.summaryCallType = summaryCallType;
        this.summaryQuality = summaryQual;
        this.callData = Collections.unmodifiableSet(new HashSet<U>(callData));
    }
}
