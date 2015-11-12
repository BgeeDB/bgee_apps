package org.bgee.model.expressiondata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bgee.model.expressiondata.CallData.DiffExpressionCallData;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DiffExpressionFactor;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.*;

//XXX: and what if it was a multi-species query? Should we use something like a MultiSpeciesCondition?
public abstract class Call<T extends Enum<T> & SummaryCallType, U extends CallData<?>> {

    //**********************************************
    //   INNER CLASSES
    //**********************************************
    public static class ExpressionCall extends Call<ExpressionSummary, ExpressionCallData> {
        public ExpressionCall(String geneId, Condition condition, DataPropagation dataPropagation, 
                ExpressionSummary summaryCallType, DataQuality summaryQual, 
                Collection<ExpressionCallData> callData) {
            super(geneId, condition, dataPropagation, summaryCallType, summaryQual, callData);
        }
    }
    
    //TODO: check that all DiffExpressionCallData 
    //have the same DiffExpressionFactor, consistent with the DiffExpressionCall
    public static class DiffExpressionCall extends Call<DiffExpressionSummary, DiffExpressionCallData> {
        private final DiffExpressionFactor diffExpressionFactor;
        
        public DiffExpressionCall(DiffExpressionFactor factor, String geneId, 
                Condition condition, DiffExpressionSummary summaryCallType, 
                DataQuality summaryQual, Collection<DiffExpressionCallData> callData) {
            super(geneId, condition, new DataPropagation(), summaryCallType, summaryQual, callData);
            this.diffExpressionFactor = factor;
        }

        public DiffExpressionFactor getDiffExpressionFactor() {
            return diffExpressionFactor;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((diffExpressionFactor == null) ? 0 : diffExpressionFactor.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!super.equals(obj)) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            DiffExpressionCall other = (DiffExpressionCall) obj;
            if (diffExpressionFactor != other.diffExpressionFactor) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "DiffExpressionCall [diffExpressionFactor=" + diffExpressionFactor 
                    + ", super Call=" + super.toString() + "]";
        }
    }
  //**********************************************
    //   INSTANCE ATTRIBUTES AND METHODS
    //**********************************************
    
    private final String geneId;
    
    private final Condition condition;
    
    private final DataPropagation dataPropagation;
    
    private final T summaryCallType;
    
    private final DataQuality summaryQuality;
    
    private final Set<U> callData;

    //Note: we cannot always know the DataPropagation status per data type, 
    //so we need to be able to provide a global DataPropagation status over all data types.
    protected Call(String geneId, Condition condition, DataPropagation dataPropagation, 
            T summaryCallType, DataQuality summaryQual, Collection<U> callData) {
        //TODO: sanity checks
        this.geneId = geneId;
        this.condition = condition;
        this.dataPropagation = dataPropagation;
        this.summaryCallType = summaryCallType;
        this.summaryQuality = summaryQual;
        this.callData = Collections.unmodifiableSet(
                callData == null? new HashSet<>(): new HashSet<>(callData));
    }

    public String getGeneId() {
        return geneId;
    }
    
    public Condition getCondition() {
        return condition;
    }
    public DataPropagation getDataPropagation() {
        return dataPropagation;
    }

    public T getSummaryCallType() {
        return summaryCallType;
    }
    public DataQuality getSummaryQuality() {
        return summaryQuality;
    }
    public Set<U> getCallData() {
        return callData;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((callData == null) ? 0 : callData.hashCode());
        result = prime * result + ((condition == null) ? 0 : condition.hashCode());
        result = prime * result + ((dataPropagation == null) ? 0 : dataPropagation.hashCode());
        result = prime * result + ((geneId == null) ? 0 : geneId.hashCode());
        result = prime * result + ((summaryCallType == null) ? 0 : summaryCallType.hashCode());
        result = prime * result + ((summaryQuality == null) ? 0 : summaryQuality.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Call<?, ?> other = (Call<?, ?>) obj;
        if (callData == null) {
            if (other.callData != null) {
                return false;
            }
        } else if (!callData.equals(other.callData)) {
            return false;
        }
        if (condition == null) {
            if (other.condition != null) {
                return false;
            }
        } else if (!condition.equals(other.condition)) {
            return false;
        }
        if (dataPropagation == null) {
            if (other.dataPropagation != null) {
                return false;
            }
        } else if (!dataPropagation.equals(other.dataPropagation)) {
            return false;
        }
        if (geneId == null) {
            if (other.geneId != null) {
                return false;
            }
        } else if (!geneId.equals(other.geneId)) {
            return false;
        }
        if (summaryCallType == null) {
            if (other.summaryCallType != null) {
                return false;
            }
        } else if (!summaryCallType.equals(other.summaryCallType)) {
            return false;
        }
        if (summaryQuality != other.summaryQuality) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [geneId=" + geneId 
                + ", condition=" + condition 
                + ", dataPropagation=" + dataPropagation
                + ", summaryCallType=" + summaryCallType 
                + ", summaryQuality=" + summaryQuality 
                + ", callData=" + callData + "]";
    }
}
