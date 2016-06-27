package org.bgee.model.expressiondata;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.distance.CanberraDistance;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
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

        //**********************************************************
        //   INNER CLASSES, STATIC ATTRIBUTES AND METHODS, 
        //   RELATED TO CLUSTERING BASED ON EXPRESSION RANK SCORE 
        //**********************************************************
        /**
         * Allows to specify the method to use to cluster {@code ExpressionCall}s based on 
         * their global mean rank score.
         * <ul>
         * <li>{@code CANBERRA_DIST_TO_MAX}: define clusters so that each member is connected 
         * to another member by a distance below the threshold, using Canberra distance. 
         * This allows to identify "shifts" or "jumps" of rank scores between {@code ExpressionCall}s. 
         * <li>{@code FIXED_CANBERRA_DIST_TO_MAX}: same as {@code CANBERRA_DIST_TO_MAX}, but each member 
         * is connected by a fixed score distance rather than by a Canberra distance threshold.  
         * The fixed score distance is the difference between the minimum score of the cluster, 
         * and a score defined so that their Canberra distance is equal to the threshold.
         * This avoids to have increasing differences of score corresponding to the same 
         * Canberra distance inside a given cluster. 
         * <li>{@code CANBERRA_DBSCAN}: perform a DBScan analysis, using Canberra distance 
         * as distance score, and 1 as the minimum cluster size. The distance threshold 
         * will correspond to the allowed "radius" of clusters. In practice, this is almost equivalent to 
         * {@code CANBERRA_DIST_TO_MAX}, except that outliers may be defined and clustered together. 
         * <li>{@code CANBERRA_DIST_TO_MEAN}: define clusters so that the distance between each member, 
         * and the mean of the cluster, is below the distance threshold, using Canberra distance. 
         * <li>{@code CANBERRA_DIST_TO_MEDIAN}: define clusters so that the distance between each member, 
         * and the median of the cluster, is below the distance threshold, using Canberra distance. 
         * <li>{@code CANBERRA_DIST_TO_MIN}: define clusters so that the distances between all members 
         * are all below the distance threshold, using Canberra distance. 
         * </ul>
         * 
         * @see #generateMeanRankScoreClustering(Collection, ClusteringMethod, double)
         */
        public static enum ClusteringMethod {
            FIXED_CANBERRA_DIST_TO_MAX, CANBERRA_DIST_TO_MAX, CANBERRA_DBSCAN, 
            CANBERRA_DIST_TO_MEAN, CANBERRA_DIST_TO_MEDIAN, CANBERRA_DIST_TO_MIN;
        }
        /**
         * Used internally to specify the reference score to use when performing 
         * a distance-based clustering. 
         * 
         * @see ClusteringMethod
         * @see #generateDistBasedClustering(Collection, double, DistanceMeasure, DistanceReference)
         */
        private static enum DistanceReference {
            MEAN, MEDIAN, MIN, MAX;
        }
        
        /**
         * A {@code Comparator} to sort {@code ExpressionCall}s based on their global mean rank 
         * (see {@link #getGlobalMeanRank()}), and provide consistent comparisons 
         * in case of rank equality. 
         */
        public final static Comparator<ExpressionCall> RANK_COMPARATOR = Comparator
                .comparing(ExpressionCall::getGlobalMeanRank, Comparator.nullsLast(BigDecimal::compareTo))
                //important in case of score equality
                .thenComparing(ExpressionCall::getGeneId, Comparator.nullsLast(String::compareTo))
                .thenComparing(ExpressionCall::getCondition, Comparator.nullsLast(Condition::compareTo));
        
        /**
         * Remove equal calls from the {@code Collection} and order them based on their global mean rank 
         * (see {@link #getGlobalMeanRank()}).
         * 
         * @param calls A {@code Collection} of {@code ExpressionCall}s to filter for redundant calls 
         *              and to order based on their global mean rank.
         * @return      A {@code List} of {@code ExpressionCall}s filtered and ordered. 
         *              {@code null} if {@code calls} was {@code null}.
         * @see #RANK_COMPARATOR
         */
        private static List<ExpressionCall> filterAndOrderByGlobalMeanRank(
                Collection<ExpressionCall> calls) {
            log.entry(calls);
            if (calls == null) {
                return log.exit(null);
            }
            List<ExpressionCall> sortedCalls = new ArrayList<>(new HashSet<ExpressionCall>(calls));
            Collections.sort(sortedCalls, RANK_COMPARATOR);
            return log.exit(sortedCalls);
        }
        
        /**
         * Generate a clustering of {@code ExpressionCall}s based on their global mean rank  
         * (see {@link #getGlobalMeanRank()}), using the {@code ClusteringMethod} {@code CANBERRA_DBSCAN} 
         * and the distance threshold {@code 0.19}.
         * 
         * @param calls A {@code Collection} of {@code ExpressionCall}s with global mean ranks defined.
         * @return      A {@code Map} where keys are {@code ExpressionCall}s, the associated value 
         *              being the index of the group in which they are clustered, 
         *              based on their expression score. Group indexes are assigned in ascending 
         *              order of expression score, starting from 0.
         */
        public static Map<ExpressionCall, Integer> generateMeanRankScoreClustering(
                Collection<ExpressionCall> calls) {
            log.entry(calls);
            return log.exit(generateMeanRankScoreClustering(
                    calls, ClusteringMethod.CANBERRA_DBSCAN, 0.19));
        }
        /**
         * Generate a clustering of {@code ExpressionCall}s based on their global mean rank 
         * (see {@link #getGlobalMeanRank()}).
         * 
         * @param calls             A {@code Collection} of {@code ExpressionCall}s 
         *                          with global mean ranks defined.
         * @param method            The {@code ClusteringMethod} to use for clustering. 
         * @param distanceThreshold A {@code double} that is the distance threshold applied to 
         *                          the {@code ClusteringMethod}. 
         * @return      A {@code Map} where keys are {@code ExpressionCall}s, the associated value 
         *              being the index of the group in which they are clustered, 
         *              based on their expression score. Group indexes are assigned in ascending 
         *              order of expression score, starting from 0.
         */
        public static Map<ExpressionCall, Integer> generateMeanRankScoreClustering(
                Collection<ExpressionCall> calls, ClusteringMethod method, double distanceThreshold) {
            log.entry(calls, method, distanceThreshold);
            
            switch(method) {
            case CANBERRA_DBSCAN: 
                return log.exit(generateDBScanClustering(calls, distanceThreshold, 1, 
                        new CanberraDistance()));
            case CANBERRA_DIST_TO_MEAN: 
                return log.exit(generateDistBasedClustering(calls, distanceThreshold, 
                        new CanberraDistance(), DistanceReference.MEAN));
            case CANBERRA_DIST_TO_MEDIAN: 
                return log.exit(generateDistBasedClustering(calls, distanceThreshold, 
                        new CanberraDistance(), DistanceReference.MEDIAN));
            case CANBERRA_DIST_TO_MIN: 
                return log.exit(generateDistBasedClustering(calls, distanceThreshold, 
                        new CanberraDistance(), DistanceReference.MIN));
            case CANBERRA_DIST_TO_MAX:
                return log.exit(generateDistBasedClustering(calls, distanceThreshold, 
                        new CanberraDistance(), DistanceReference.MAX));
            case FIXED_CANBERRA_DIST_TO_MAX:
                return log.exit(generateFixedCanberraDistToMaxClustering(calls, distanceThreshold));
            default: 
                throw log.throwing(new IllegalArgumentException("Unrecognized clustering method: " 
                        + method));
            }
        }
        /**
         * Generate a clustering of {@code ExpressionCall}s based on their global mean rank 
         * using DBScan (see {@link #getGlobalMeanRank()} and <a href='https://commons.apache.org/proper/commons-math/apidocs/org/apache/commons/math3/ml/clustering/DBSCANClusterer.html'>
         * org.apache.commons.math3.ml.clustering.DBSCANClusterer</a>).
         * 
         * @param calls     A {@code Collection} of {@code ExpressionCall}s with global mean ranks defined.
         * @param epislon   A {@code double} that is the distance radius to use for DBScan.
         * @param minSize   An {@code int} that is the minimum size of a resulting cluster.
         * @param measure   A {@code DistanceMeasure} to compute distances. 
         * @return          A {@code Map} where keys are {@code ExpressionCall}s, the associated value 
         *                  being the index of the group in which they are clustered, 
         *                  based on their expression score. Group indexes are assigned in ascending 
         *                  order of expression score, starting from 0.
         */
        private static Map<ExpressionCall, Integer> generateDBScanClustering(
                Collection<ExpressionCall> calls, double epislon, int minSize, DistanceMeasure measure) {
            log.entry(calls, epislon, minSize, measure);

            /**
             * A wrapper for {@code ExpressionCall} to implement the {@code Clusterable} interface.
             */
            final class ExpressionCallClusterable implements Clusterable {
                private final ExpressionCall refCall;
                
                private ExpressionCallClusterable(ExpressionCall call) {
                    this.refCall = call;
                }
                @Override
                public double[] getPoint() {
                    return new double[]{this.refCall.getGlobalMeanRank().doubleValue()};
                }
                public ExpressionCall getRefExpressionCall() {
                    return refCall;
                }
            }
            
            //DBScan doesn't use a sorted List, but we need one to "fill the gaps" 
            //caused by DBScan outliers, see later. 
            List<ExpressionCall> sortedCalls = filterAndOrderByGlobalMeanRank(calls);
            
            DBSCANClusterer<ExpressionCallClusterable> clusterer = 
                    new DBSCANClusterer<>(epislon, minSize, measure);
            List<Cluster<ExpressionCallClusterable>> clusters = clusterer.cluster(
                    sortedCalls.stream().map(ExpressionCallClusterable::new).collect(Collectors.toList()));
            
            //first, we extract a mapping ExpressionCall -> group index from DBScan clustering
            Map<ExpressionCall, Integer> tmpCallsToGroup = clusters.stream().flatMap(c -> {
                int groupIndex = clusters.indexOf(c);
                return c.getPoints().stream().map(p -> 
                    new AbstractMap.SimpleEntry<ExpressionCall, Integer>(
                        p.getRefExpressionCall(), groupIndex));
            }).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
            log.debug("Original DBScan clustering: {}", tmpCallsToGroup);
            
            //Now, we fill the ExpressionCalls missing from the clustering because of outliers.
            Map<ExpressionCall, Integer> callsToGroup = new HashMap<>();
            int computedGroupIndex = -1;
            Integer lastIndex = null;
            for (ExpressionCall call: sortedCalls) {
                Integer groupIndex = tmpCallsToGroup.get(call);
                //we create a new cluster for each outlier, or if we are really iterating a new group. 
                //So, in groupIndex is null, we are iterating an outlier, we create a cluster; 
                //if lastIndex is null, the last call was an outlier, so either we iterate 
                //another outlier, or a new group, in any case we create a new cluster; 
                //finally, if groupIndex != lastIndex, we are moving from a real cluster 
                //to another cluster.
                if (groupIndex == null || lastIndex == null || groupIndex != lastIndex) {
                    computedGroupIndex++;
                }
                callsToGroup.put(call, computedGroupIndex);
                lastIndex = groupIndex;
            }
            log.debug("Secondary DBScan clustering: {}", callsToGroup);
            
            return log.exit(callsToGroup);
        }
        /**
         * Generate a clustering of {@code ExpressionCall}s based on their global mean rank 
         * using distance from a reference score, defined depending on {@code ref} argument.
         * 
         * @param calls                 A {@code Collection} of {@code ExpressionCall}s 
         *                              with global mean ranks defined.
         * @param distanceThreshold     A {@code double} that is the distance threshold to the reference 
         *                              score of a cluster to consider a call outside of the cluster.
         * @param measure               A {@code DistanceMeasure} to compute distances.
         * @param ref                   A {@code DistanceReference} specifying what is the reference score 
         *                              of a cluster: either it is the minimum score of the cluster 
         *                              (then the distance between all scores must be below the threshold), 
         *                              or the maximum score (then all scores are connected to at least 
         *                              one other score below the threshold), or the mean of the cluster 
         *                              (the distance to the mean of all scores is below the threshold), 
         *                              or the median (distance to the median of all scores below 
         *                              the threshold). 
         * @return          A {@code Map} where keys are {@code ExpressionCall}s, the associated value 
         *                  being the index of the group in which they are clustered, 
         *                  based on their expression score. Group indexes are assigned in ascending 
         *                  order of expression score, starting from 0.
         */
        private static Map<ExpressionCall, Integer> generateDistBasedClustering(
                Collection<ExpressionCall> calls, double distanceThreshold, 
                DistanceMeasure measure, DistanceReference ref) {
            log.entry(calls, distanceThreshold, measure, ref);
            
            List<ExpressionCall> sortedCalls = filterAndOrderByGlobalMeanRank(calls);
            
            Map<ExpressionCall, Integer> callsToGroup = new HashMap<>();
            int groupIndex = -1;
            List<ExpressionCall> groupMember = null;
            for (ExpressionCall call: sortedCalls) {
                log.trace("Iterating call for distance-based clustering: {}", call);
                boolean createGroup = false;

                //create a new group if first iteration
                if (groupMember == null) {
                    createGroup = true;
                } else {
                    double[] refScore = new double[1];
                    boolean compareToMin = false;
                    switch (ref) {
                    case MIN: 
                        refScore[0] = groupMember.get(0).getGlobalMeanRank().doubleValue();
                        break;
                    case MAX: 
                        refScore[0] = groupMember.get(groupMember.size() - 1).getGlobalMeanRank()
                                       .doubleValue();
                        break;
                    case MEAN: 
                        refScore[0] = (groupMember.stream()
                                            .mapToDouble(c -> c.getGlobalMeanRank().doubleValue()).sum() 
                                        + call.getGlobalMeanRank().doubleValue())
                                        /(groupMember.size() + 1);
                        compareToMin = true;
                        break;
                    case MEDIAN: 
                        List<ExpressionCall> nextGroupMember = new ArrayList<>(groupMember);
                        nextGroupMember.add(call);
                        refScore[0] = getMedianMeanRankScore(nextGroupMember);
                        compareToMin = true;
                        break;
                    default: 
                        throw log.throwing(new IllegalArgumentException("Unsupported reference: " + ref));
                    }
                    assert refScore[0] != 0;
                    
                    double distance = measure.compute(refScore, 
                            new double[]{call.getGlobalMeanRank().doubleValue()});
                    if (log.isTraceEnabled()) {
                        log.trace("Reference score: {} - current score: {} - Distance: {} - "
                                + "Distance threshold: {} - Compare to min: {} - "
                                + "Rank of first member: {}, Distance to ref: {}", 
                              refScore[0], call.getGlobalMeanRank().doubleValue(), 
                              distance, distanceThreshold, compareToMin, 
                              groupMember.get(0).getGlobalMeanRank().doubleValue(), 
                              measure.compute(refScore, 
                                  new double[]{groupMember.get(0).getGlobalMeanRank().doubleValue()}));
                    }
                    
                        //if the distance between the ref score 
                        //and the currently iterated score is over the threshold 
                    if (distance > distanceThreshold || 
                        //or, in case of distance to mean or median, if, by adding the currently 
                        //iterated score to the group, the distance between the ref. score 
                        //and the minimum score of the group will be over the threshold.
                        (compareToMin && measure.compute(refScore, 
                                    new double[]{groupMember.get(0).getGlobalMeanRank().doubleValue()}) 
                            > distanceThreshold)) {
                        createGroup = true;
                    }
                }
                
                if (createGroup) {
                    log.trace("Create new group");
                    groupIndex++;
                    groupMember = new ArrayList<>();
                }
                groupMember.add(call);
                callsToGroup.put(call, groupIndex);
                log.trace("Assign Call {} to group index {}", call, groupIndex);
            }
            
            return log.exit(callsToGroup);
        }
        
        /**
         * Generates clustering for {@link ClusteringMethod FIXED_CANBERRA_DIST_TO_MAX}.
         * 
         * @param calls                 A {@code Collection} of {@code ExpressionCall}s 
         *                              with global mean ranks defined.
         * @param distanceThreshold     A {@code double} that is the distance threshold to the reference 
         *                              score of a cluster to consider a call outside of the cluster.
         * @return          A {@code Map} where keys are {@code ExpressionCall}s, the associated value 
         *                  being the index of the group in which they are clustered, 
         *                  based on their expression score. Group indexes are assigned in ascending 
         *                  order of expression score, starting from 0.
         */
        private static Map<ExpressionCall, Integer> generateFixedCanberraDistToMaxClustering(
                Collection<ExpressionCall> calls, double distanceThreshold) {
            log.entry(calls, distanceThreshold);

            List<ExpressionCall> sortedCalls = filterAndOrderByGlobalMeanRank(calls);
            
            Map<ExpressionCall, Integer> callsToGroup = new HashMap<>();
            int groupIndex = -1;
            double groupAllowedScoreDiff = 0;
            double previousScore = 0;
            
            for (ExpressionCall call: sortedCalls) {
                double currentScore = call.getGlobalMeanRank().doubleValue();

                // create a new group if first iteration, 
                // or if current score over the allowed score diff. 
                // 
                // Note: we could compute a new distance threshold for the max score of the cluster 
                // and the currently iterated score to be in the allowed score diff:
                // new_potential_max = cluster_max + allowed_score_diff
                // dist_threshold = (new_potential_max - cluster_max)/(new_potential_max + cluster_max)
                // dist_threshold = (allowed_score_diff)/(2 * cluster_max + allowed_score_diff)
                if (groupIndex == -1 || 
                        currentScore - previousScore - groupAllowedScoreDiff >= -0.000001) {
                    
                    groupIndex++;
                    // At cluster creation, we compute the allowed difference of score, 
                    // so that a score and the minimum score of the cluster are below 
                    // the distance threshold.
                    // Camberra distance: (score2 - score1)/(score2 + score1) = dist_threshold
                    // Score2 as a function of score1 and dist_threshold: 
                    // score2 = -score1(1 + dist_threshold)/(dist_threshold - 1)
                    // => allowed_score_diff = -score1 * ((1 + dist_threshold)/(dist_threshold - 1) + 1)
                    groupAllowedScoreDiff = -currentScore 
                            * ((1 + distanceThreshold)/(distanceThreshold - 1) + 1);
                } 
                callsToGroup.put(call, groupIndex);
                previousScore = currentScore;
            }
            
            return log.exit(callsToGroup);
        }
        
        /**
         * Get the median mean rank from {@code ExpressionCall}s ordered based on their rank. 
         * 
         * @param calls A {@code List} of {@code ExpressionCall}s ordered by their global mean rank 
         *              (see {@link #getGlobalMeanRank()}).
         * @return      A {@code double} that is the median mean rank. 
         */
        private static double getMedianMeanRankScore(List<ExpressionCall> calls) {
            log.entry(calls);
            int size = calls.size();
            if (size == 0) {
                throw log.throwing(new IllegalArgumentException("Can't compute mediam of empty list"));
            }
            if (size == 1) {
                return log.exit(calls.get(0).getGlobalMeanRank().doubleValue());
            }
            if (size % 2 == 0) {
                return log.exit((calls.get(size/2).getGlobalMeanRank().doubleValue() 
                           + calls.get(size/2 - 1).getGlobalMeanRank().doubleValue())
                           /2);
            } 
            return log.exit(calls.get((size - 1)/2).getGlobalMeanRank().doubleValue());
        }
        
        
        //*******************************************
        // INSTANCE ATTRIBUTES AND METHODS
        //*******************************************
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
            builder.append("ExpressionCall [getGlobalMeanRank()=").append(getGlobalMeanRank())
                   .append(", super Call=").append(super.toString())
                   .append("]");
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
            StringBuilder builder = new StringBuilder();
            builder.append("DiffExpressionCall [diffExpressionFactor()=").append(getDiffExpressionFactor())
                   .append(", super Call=").append(super.toString())
                   .append("]");
            return builder.toString();
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
        return    ", geneId=" + geneId 
                + ", condition=" + condition 
                + ", dataPropagation=" + dataPropagation
                + ", summaryCallType=" + summaryCallType 
                + ", summaryQuality=" + summaryQuality 
                + ", callData=" + callData;
    }
}
