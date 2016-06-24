package org.bgee.model.expressiondata;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.CallData.DiffExpressionCallData;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DiffExpressionFactor;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.*;

//XXX: and what if it was a multi-species query? Should we use something like a MultiSpeciesCondition?
//TODO: move inner classes to different files
public abstract class Call<T extends Enum<T> & SummaryCallType, U extends CallData<?>> {
    private final static Logger log = LogManager.getLogger(Call.class.getName());

    //**********************************************
    //   INNER CLASSES
    //**********************************************
    public static class ExpressionCall extends Call<ExpressionSummary, ExpressionCallData> {

        /**
         * A {@code double} that is the threshold of the distance between two expression scores, 
         * to consider them as part of different clusters. 
         * @see #getCallsToScoreGroupIndex(Collection)
         */
        public final static double DISTANCE_THRESHOLD = 0.35;
        /**
         * Generate a clustering of {@code ExpressionCall}s based on their expression score 
         * (see {@link #getGlobalMeanRank()}).
         * 
         * @param calls A {@code Collection} of {@code ExpressionCall}s with expression scores.
         * @return      A {@code Map} where keys are {@code ExpressionCall}s, the associated value 
         *              being the index of the group in which they are clustered, 
         *              based on their expression score. Group indexes are assigned in ascending 
         *              order of expression score, starting from 0.
         */
        public static Map<ExpressionCall, Integer> generateCallsToScoreGroupIndex(
                Collection<ExpressionCall> calls) {
            log.entry(calls);
            List<ExpressionCall> sortedCalls = new ArrayList<>((calls == null? 
                    new HashSet<ExpressionCall>(): new HashSet<ExpressionCall>(calls)));
            Collections.sort(sortedCalls, 
                    (c1, c2) -> c1.getGlobalMeanRank().compareTo(c2.getGlobalMeanRank()));
            return log.exit(generateCallsToScoreGroupIndex(sortedCalls));
        }
        /**
         * Generate a clustering of {@code ExpressionCall}s based on their expression score 
         * (see {@link #getGlobalMeanRank()}).
         * 
         * @param calls A {@code List} of {@code ExpressionCall}s ranked based on 
         *              their expression score.
         * @return      A {@code Map} where keys are {@code ExpressionCall}s, the associated value 
         *              being the index of the group in which they are clustered, 
         *              based on their expression score. Group indexes are assigned in ascending 
         *              order of expression score, starting from 0.
         */
        public static Map<ExpressionCall, Integer> generateCallsToScoreGroupIndex(
                List<ExpressionCall> calls) {
            log.entry(calls);
            
            Map<ExpressionCall, Integer> callsToGroup = new HashMap<>();
            int groupIndex = -1;
            List<ExpressionCall> groupMember = null;
            for (ExpressionCall call: calls) {
                if (groupMember == null || getDistance(
                        //compute the mean score of the current group
                        groupMember.stream().mapToDouble(c -> c.getGlobalMeanRank().doubleValue())
                        .average().getAsDouble(),
                        //compare it to the currently iterated score (will be less then the mean 
                        //of the next group, for sure)
                        call.getGlobalMeanRank().doubleValue()) > DISTANCE_THRESHOLD) {
                    groupIndex++;
                    groupMember = new ArrayList<>();
                }
                groupMember.add(call);
                callsToGroup.put(call, groupIndex);
            }
            
            return log.exit(callsToGroup);
        }
        /**
         * Compute a distance score for expression score clustering. 
         * 
         * @param score1    A {@code double} that is the first score to compare.
         * @param score2    A {@code double} that is the second score to compare.
         * @return          A {@code double} that is the distance score between the provided arguments.
         */
        private static double getDistance(double score1, double score2) {
            log.entry(score1, score2);
            //Canberra distance
            return log.exit( Math.abs(score1 - score2)/(Math.abs(score1) + Math.abs(score2)) );
        }
        
        
        /**
         * @see #getGlobalMeanRank()
         */
        private final BigDecimal globalMeanRank;
        /**
         * A {@code NumberFormat} used to format {@link #globalMeanRank} 
         * (see {@link #getFormattedGlobalMeanRank()}). It is not taken into account for equals/hashCode.
         */
        private final NumberFormat formatter;
        
        public ExpressionCall(String geneId, Condition condition, DataPropagation dataPropagation, 
                ExpressionSummary summaryCallType, DataQuality summaryQual, 
                Collection<ExpressionCallData> callData, BigDecimal globalMeanRank) {
            super(geneId, condition, dataPropagation, summaryCallType, summaryQual, callData);
            
            //BigDecimal are immutable, no need to copy it
            this.globalMeanRank = globalMeanRank;
            //set up a formatter for nice display of the score
            if (globalMeanRank != null) {
                NumberFormat formatter = NumberFormat.getInstance(Locale.US);
                formatter.setMaximumFractionDigits(2);
                formatter.setMinimumFractionDigits(2);
                formatter.setRoundingMode(RoundingMode.HALF_UP);
                this.formatter = formatter;
            } else {
                this.formatter = null;
            }
        }
        
        /**
         * @return  The {@code BigDecimal} corresponding to the score allowing to rank 
         *          this {@code ExpressionCall}.
         *          
         * @see #getFormattedGlobalMeanRank()
         * @see #getFormattedGlobalMeanRank(NumberFormat)
         */
        public BigDecimal getGlobalMeanRank() {
            return this.globalMeanRank;
        }
        /**
         * @return  A {@code String} formatted by default, corresponding to the {@code BigDecimal} 
         *          allowing to rank this {@code ExpressionCall}. 
         *          
         * @see #getGlobalMeanRank()
         * @see #getFormattedGlobalMeanRank(NumberFormat)
         */
        public String getFormattedGlobalMeanRank() {
            log.entry();
            return log.exit(this.getFormattedGlobalMeanRank(this.formatter));
        }
        /**
         * Format the score allowing to rank this {@code ExpressionCall}, according to the provided 
         * {@code NumberFormat}.
         * 
         * @param formatter The {@code NumberFormat} used to format the score returned by 
         *                  {@link #getGlobalMeanRank()}.
         * @return          A {@code String} formatted by {@code formatter}, corresponding to 
         *                  the {@code BigDecimal} allowing to rank this {@code ExpressionCall}. 
         * @throws IllegalStateException    If no ranking score was provided at instantiation.
         * @see #getGlobalMeanRank()
         * @see #getFormattedGlobalMeanRank()
         */
        public String getFormattedGlobalMeanRank(NumberFormat formatter) {
            if (this.globalMeanRank == null) {
                throw log.throwing(new IllegalStateException("No rank was provided for this call."));
            }
            return log.exit(formatter.format(this.globalMeanRank));
        }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((globalMeanRank == null) ? 0 : globalMeanRank.hashCode());
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
            ExpressionCall other = (ExpressionCall) obj;
            if (globalMeanRank == null) {
                if (other.globalMeanRank != null) {
                    return false;
                }
            } else if (!globalMeanRank.equals(other.globalMeanRank)) {
                return false;
            }
            return true;
        }
        
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ExpressionCall [globalMeanRank=").append(globalMeanRank).append("]");
            return builder.toString();
        }
    }
    
    //TODO: check that all DiffExpressionCallData 
    //have the same DiffExpressionFactor, consistent with the DiffExpressionCall
    public static class DiffExpressionCall extends Call<DiffExpressionSummary, DiffExpressionCallData> {
        /**
         * @see #getDiffExpressionFactor()
         */
        private final DiffExpressionFactor diffExpressionFactor;
        
        public DiffExpressionCall(DiffExpressionFactor factor, String geneId, 
                Condition condition, DiffExpressionSummary summaryCallType, 
                DataQuality summaryQual, Collection<DiffExpressionCallData> callData) {
            super(geneId, condition, new DataPropagation(), summaryCallType, summaryQual, callData);
            this.diffExpressionFactor = factor;
        }

        /**
         * @return  A {@code DiffExpressionFactor} defining the criteria on which comparisons 
         *          of expression levels were made. 
         */
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
        if (DataQuality.NODATA.equals(summaryQual)) {
            throw log.throwing(new IllegalArgumentException("An actual DataQuality must be provided."));
        }
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
