package org.bgee.model.expressiondata;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.bgee.model.expressiondata.DataDeclaration.DataQuality;
import org.bgee.model.expressiondata.DataDeclaration.DataType;

//XXX: and what if it was a multi-species query? Should we use something like a MultiSpeciesCondition?
public abstract class Call<T extends SummaryCallType, U extends CallData<?>> {
    
    private String geneId;
    
    private Condition condition;

    //**********************************************
    //   INNER CLASSES
    //**********************************************
    public static interface SummaryCallType {
        public static enum ExpressionSummary implements SummaryCallType {
            EXPRESSED, NOT_EXPRESSED, AMBIGUITY_ETC;
        }
        public static enum DiffExpressionSummary implements SummaryCallType {
            DIFF_EXPRESSED, OVER_EXPRESSED, UNDER_EXPRESSED, NOT_DIFF_EXPRESSED, AMBIGUITY_ETC;
        }
    }
    
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
    /**
     * @see #getOverallCallType()
     */
    private final T overallCallType;
    /**
     * @see #getOverallQuality()
     */
    private final DataQuality overallQuality;
    
    private final Map<DataType, U> callDataPerDataTypes;
    
    
}
