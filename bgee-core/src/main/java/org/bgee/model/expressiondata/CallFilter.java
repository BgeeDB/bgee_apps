package org.bgee.model.expressiondata;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.Call.DiffExpressionCall;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.gene.GeneFilter;

/**
 * A filter to parameterize queries to {@link CallService}. 
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Sep. 2018
 * @since   Bgee 13, Oct. 2015
 *
 * @param T The type of {@code CallData} to be used by this {@code CallFilter}. 
 *          Can be declared as {@code CallData}, to include a mixture of {@code CallData} subtypes, 
 *          or as a specific subtype, for instance, {@code ExpressionCallData}.
 * @param U The type of {@code SummaryCallType} to be used by this {@code CallFilter}.
 */
//Note: would several CallFilters represent AND or OR conditions.
//If OR conditions, we could provide a Set<Set<CallFilter>> to CallService methods, 
//to provide AND/OR conditions.
//IF AND conditions, then we cannot easily target different CallDatas for different ConditionFilters, e.g.: 
//((brain adult and affymetrixQual >= high) OR (liver adult and rnaSeqQual >= high) OR (heart adult over-expressed). 
//=> I don't think we'd often like to do such a query, most of the time we would target 
//the same CallDatas. If we really needed it, then we could still do it in several queries 
//(even if it is less optimized (only when it targets the same CallType)).
//=> let's consider several CallFilters as AND conditions for now, and let's see what happens in the future.  
//Note that even if they were OR conditions, they should be used in several queries, 
//as it is not possible from the DAO to make one query applying a different Set 
//of CallData filters to different Sets of GeneFilters, ConditionFilters, etc.
//XXX: Actually, if CallFilters are AND conditions, it might be a problem for multispecies queries:
//we might want to precisely link some GeneFilters and ConditionFilters, so that the Conditions to use
//are not the same for each species.
//***********************
//Note: update FEB. 2017. We decided to remove the CallData from this class.
//Because the quality of a call is now computed over all data types, 
//so we don't want to filter on data quality per data type any more. 
//Also, so far we don't need to filter calls based on propagation per data type 
//(e.g., calls including substructures for Affymetrix, not including substructures for RNA-Seq).
//If these two points wanted to be achieved, we could use the new fields of, e.g., ExpressionCallData: 
// absentHighParentExpCount, presentHighDescExpCount, etc.
public abstract class CallFilter<T extends CallData<?>, U extends Enum<U> & SummaryCallType>
extends DataFilter<ConditionFilter> {
    private final static Logger log = LogManager.getLogger(CallFilter.class.getName());
    
    /**
     * A {@code CallFilter} for {@code ExpressionCall}.
     * 
     * @author  Frederic Bastian
     * @author  Valentine Rech de Laval
     * @version Bgee 14, Mar. 2017
     * @since   Bgee 13
     */
    public static class ExpressionCallFilter
    extends CallFilter<ExpressionCallData, SummaryCallType.ExpressionSummary> implements Predicate<ExpressionCall> {
        
        private final Map<CallType.Expression, Boolean> callObservedData;

        private final Boolean anatEntityObservedData;
        private final Boolean devStageObservedData;

        public ExpressionCallFilter(
                Map<SummaryCallType.ExpressionSummary, SummaryQuality> summaryCallTypeQualityFilter,
                Set<GeneFilter> geneFilters, Collection<ConditionFilter> conditionFilters, Collection<DataType> dataTypeFilter,
                Map<CallType.Expression, Boolean> callObservedData,
                Boolean anatEntityObservedData, Boolean devStageObservedData)
                        throws IllegalArgumentException {
            super(summaryCallTypeQualityFilter, geneFilters, conditionFilters, dataTypeFilter,
                    SummaryCallType.ExpressionSummary.class);

            this.callObservedData = Collections.unmodifiableMap(
                    callObservedData == null? new HashMap<>(): new HashMap<>(callObservedData));
            this.anatEntityObservedData = anatEntityObservedData;
            this.devStageObservedData = devStageObservedData;
            try {
                this.checkEmptyFilters();
            } catch (IllegalStateException e) {
                throw log.throwing(new IllegalArgumentException("Incorrect filters provided", e));
            }
        }

        @Override
        protected void checkEmptyFilters() throws IllegalStateException {
            log.entry();
            //nothing special in this subclass, the observedData filter alone is not enough,
            //so we let the superclass decide whether it's happy about the filters it manages.
            super.checkEmptyFilters();
            log.exit();
        }

        /**
         * Provides filtering on observation of data or of specific call types in the condition.
         * Keys in this {@code Map} defines the call type on which to apply the filtering. If a key is {@code null},
         * the filtering applies to any call type. The associated value is a {@code Boolean} defining whether
         * the data or the call type should have been observed in the condition itself. For instance,
         * if you set a key to {@code EXPRESSED} {@code ExpressionSummary}, and set the associated value to {@code true},
         * it will allow to retrieve only calls including expression in the condition itself,
         * and not simply calls with any data observed in the condition itself (such as a reported absence of expression).
         * If you set a key to {@code null}, and set the associated value to {@code true}, it will allow
         * to retrieve only calls with observed data in the condition itself, (whether it is presence or absence
         * of expression).
         * <p>
         * <strong>Important:</strong> data quality is not taken into account,
         * {@code ExpressionSummary}s (if any) will be considered with any quality. <strong>But only considering
         * the data types requested at instantiation (if any).</strong> Note: this attribute could simply be
         * a {@code Boolean}, based on the {@code ExpressionSummary}s provided at instantiation (in the same way
         * it is based on the data types provided at instantiation). But there is more flexibility this way,
         * as you can request, for instance, to retrieve only {@code EXPRESSED} calls, but having any call type
         * directly observed in the condition.
         * <p>
         * This is independent from {@link #getAnatEntityObservedData()} and {@link #getDevStageObservedData()},
         * to be able to distinguish between whether data were observed in, for instance, the anatomical entity,
         * and propagated along the dev. stage ontology. For instance, you might want to retrieve expression calls
         * at a given dev. stage (using any propagation states), only if observed in the anatomical structure itself.
         * The "callObservedData" filter does not permit solely to perform such a query.
         *
         * @return  A {@code Map} where keys are {@code ExpressionSummary} for which we want a filtering based on data propagation,
         *          the associated value being a {@code Boolean} defining the requested observed data state.
         *          If a key is {@code null}, the associated filtering applies to any call type.
         * @see #getAnatEntityObservedData()
         * @see #getDevStageObservedData()
         */
        public Map<CallType.Expression, Boolean> getCallObservedData() {
            return callObservedData;
        }
        /**
         * Provides filtering based on observation of data in the anatomical entities.
         * <p>
         * Note that, as opposed to {@link #getCallObservedData()}, it is not at the moment possible
         * to add a filter for this information based on different {@code ExpressionSummary}s,
         * because we do not distinguish source of different call types along every condition parameters,
         * but only associated to the condition at a whole.
         *
         * @return  A {@code Boolean} defining whether the retrieved data should have been observed
         *          in the anatomical entity itself. This is for any call type, whether {@code EXPRESSED} or
         *          {@code NOT_EXPRESSED}.
         * @see #getCallObservedData()
         * @see #getDevStageObservedData()
         */
        public Boolean getAnatEntityObservedData() {
            return anatEntityObservedData;
        }
        /**
         *
         * @return
         * @see #getCallObservedData()
         * @see #getAnatEntityObservedData()
         */
        public Boolean getDevStageObservedData() {
            return devStageObservedData;
        }

        @Override
        public boolean test(ExpressionCall call) {
            // Filter on common fields of Calls
            if (!super.test(call)) {
                return log.exit(false);
            }
            // Filter on observed data
            //XXX: actually, we can now filter calls based on this information directly in the DAO,
            //so maybe we should force to retrieve this information in the Call solely to test it.
            //TODO: there is more work to do to manage callObservedData when non-null keys are provided
            if (callObservedData != null && callObservedData.keySet().stream().anyMatch(k -> k != null)) {
                throw log.throwing(new UnsupportedOperationException("Test not implemented for callObservedData non-null keys"));
            }
            if (callObservedData != null && callObservedData.containsKey(null) || anatEntityObservedData != null ||
                    devStageObservedData != null) {

                if (call.getDataPropagation() == null) {
                    throw log.throwing(new IllegalArgumentException(
                            "The provided Call does not allow to retrieve observedData information"));
                }
                //TODO: there is more work to do to manage callObservedData when non-null keys are provided
                if (callObservedData != null && callObservedData.containsKey(null)) {
                    if (call.getDataPropagation().isIncludingObservedData() == null) {
                        throw log.throwing(new IllegalArgumentException(
                                "The provided Call does not allow to retrieve observedData information"));
                    }
                    if (!call.getDataPropagation().isIncludingObservedData().equals(callObservedData.get(null))) {
                        return log.exit(false);
                    }
                }

                if (anatEntityObservedData != null &&
                        (call.getDataPropagation().getAnatEntityPropagationState() == null ||
                        call.getDataPropagation().getAnatEntityPropagationState()
                        .isIncludingObservedData() == null)) {
                    throw log.throwing(new IllegalArgumentException(
                            "The provided Call does not allow to retrieve observedData information"));
                }
                if (!anatEntityObservedData.equals(call.getDataPropagation()
                        .getAnatEntityPropagationState().isIncludingObservedData())) {
                    return log.exit(false);
                }

                if (devStageObservedData != null &&
                        (call.getDataPropagation().getDevStagePropagationState() == null ||
                        call.getDataPropagation().getDevStagePropagationState()
                        .isIncludingObservedData() == null)) {
                    throw log.throwing(new IllegalArgumentException(
                            "The provided Call does not allow to retrieve observedData information"));
                }
                if (!devStageObservedData.equals(call.getDataPropagation()
                        .getDevStagePropagationState().isIncludingObservedData())) {
                    return log.exit(false);
                }
            }
            return log.exit(true);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((callObservedData == null) ? 0 : callObservedData.hashCode());
            result = prime * result
                    + ((anatEntityObservedData == null) ? 0 : anatEntityObservedData.hashCode());
            result = prime * result + ((devStageObservedData == null) ? 0 : devStageObservedData.hashCode());
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
            ExpressionCallFilter other = (ExpressionCallFilter) obj;
            if (callObservedData == null) {
                if (other.callObservedData != null) {
                    return false;
                }
            } else if (!callObservedData.equals(other.callObservedData)) {
                return false;
            }
            if (anatEntityObservedData == null) {
                if (other.anatEntityObservedData != null) {
                    return false;
                }
            } else if (!anatEntityObservedData.equals(other.anatEntityObservedData)) {
                return false;
            }
            if (devStageObservedData == null) {
                if (other.devStageObservedData != null) {
                    return false;
                }
            } else if (!devStageObservedData.equals(other.devStageObservedData)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ExpressionCallFilter [callObservedData=").append(callObservedData)
                   .append(", anatEntityObservedData=").append(anatEntityObservedData)
                   .append(", devStageObservedData=").append(devStageObservedData)
                   .append(", geneFilters=").append(getGeneFilters())
                   .append(", conditionFilters=").append(getConditionFilters())
                   .append(", dataTypeFilters=").append(getDataTypeFilters())
                   .append(", summaryCallTypeQualityFilter=").append(getSummaryCallTypeQualityFilter())
                   .append("]");
            return builder.toString();
        }
    }
    
    /**
     * A {@code CallFilter} for {@code DiffExpressionCall}.
     * 
     * @author  Frederic Bastian
     * @author  Valentine Rech de Laval
     * @version Bgee 14, Feb. 2017
     * @since   Bgee 13
     */
    public static class DiffExpressionCallFilter
    extends CallFilter<ExpressionCallData, SummaryCallType.DiffExpressionSummary> implements Predicate<DiffExpressionCall> {
        /**
         * See {@link CallFilter#CallFilter(GeneFilter, Collection, Collection, SummaryQuality, SummaryCallType)}.
         */
        public DiffExpressionCallFilter(
                Map<SummaryCallType.DiffExpressionSummary, SummaryQuality> summaryCallTypeQualityFilter,
                Set<GeneFilter> geneFilters, Collection<ConditionFilter> conditionFilters, 
            Collection<DataType> dataTypeFilter) throws IllegalArgumentException {
            super(summaryCallTypeQualityFilter, geneFilters, conditionFilters, dataTypeFilter,
                    SummaryCallType.DiffExpressionSummary.class);
            try {
                this.checkEmptyFilters();
            } catch (IllegalStateException e) {
                throw log.throwing(new IllegalArgumentException("Incorrect filters provided", e));
            }
        }

        @Override
        //just to remember to implement this method in case we add attributes
        public int hashCode() {
            return super.hashCode();
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
            return true;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("DiffExpressionCallFilter [geneFilters=").append(getGeneFilters())
                    .append(", conditionFilters=").append(getConditionFilters())
                    .append(", dataTypeFilters=").append(getDataTypeFilters())
                    .append(", summaryCallTypeQualityFilter=").append(getSummaryCallTypeQualityFilter())
                    .append("]");
            return builder.toString();
        }

        @Override
        public boolean test(DiffExpressionCall arg0) {
            // TODO Auto-generated method stub
            throw log.throwing(new UnsupportedOperationException("Method not implemented"));
        }
    }

    /**
     * @see #getCallDataFilters()
     */
    //XXX: all CallData are OR conditions. The only type of query not easily doable is: 
    //affymetrixData = expressed high && rnaSeqData = expressed high. 
    //Note that they *must* remain OR conditions, because the DataPropagation 
    //is part of these CallData, and we need to do one query
    //XXX: again, where to accept the diffExpressionFactor
//    private final Set<T> callDataFilters;
    
    // Only OR is allowed
    /**
     * @see #getDataTypeFilter()
     */
    private final Set<DataType> dataTypeFilters;

    /**
     * @see #getSummaryCallTypeQualityFilter()
     */
    private final Map<U, SummaryQuality> summaryCallTypeQualityFilter;
    
    /**FIXME javadoc
     * Constructor accepting all requested parameters to build a new {@code CallFilter}. 
     * {@code geneFilter} and {@code conditionFilters} can be {@code null} or empty, 
     * but {@code callDataFilters} cannot, otherwise an {@code IllegalArgumentException} is thrown. 
     * Indeed, at least one  {@code CallType} should be targeted through at least one {@code CallData}, 
     * and the origin of the data along the ontologies used to capture conditions should be specified.
     * <p>
     * If the method {@link CallData#getDataType()} returns {@code null} for a {@code CallData}, 
     * then it means that it targets any {@code DataType}, otherwise, it means that it targets only 
     * that specific {@code DataType}. It is not possible to provide several {@code ExpressionCallData}s
     * targeting the same combination of {@code CallType} (see {@link CallData#getCallType()}) 
     * and {@code DataType} (see {@link CallData#getDataType()}), or targeting 
     * for a same {@code CallType} both a {@code null} {@code DataType} and a non-null {@code DataType};
     * for {@code DiffExpressionCallData}, it is similarly not possible to target a redundant combination 
     * of {@code CallType}, {@code DataType}, and {@code DiffExpressionFactor} (see 
     * {@link DiffExpressionCallData#getDiffExpressionFactor()}); otherwise, 
     * an {@code IllegalArgumentException} is thrown. 
     * <p>
     * Note that the {@code DataPropagation}s provided in {@code callDataFilters} 
     * are <strong>not</strong> considered. This is because this information cannot be inferred 
     * for each data type individually from one single query. This information is provided 
     * at the level of a {@code Call}. 
     * 
     * @param geneFilter            A {@code GeneFilter} to configure gene-related filtering.
     * @param conditionFilters      A {@code Collection} of {@code ConditionFilter}s to configure 
     *                              the filtering of conditions with expression data. If several 
     *                              {@code ConditionFilter}s are provided, they are seen as "OR" conditions.
     * @param dataTypeFilters        TODO javadoc
     * @param summaryQualityFilter  TODO javadoc
     * @throws IllegalArgumentException If any filter contains {@code null} elements,
     *                                  or if no filter is defined at all.
     */
    //IMPORTANT: note that subclasses must override checkEmptyFilters as needed,
    //use in it super.checkEmptyFilters(), and call it in their constructor (this cannot be done
    //in this constructor, as subclasses might need to set their own attributes before calling checkEmptyFilters)
    protected CallFilter(Map<U, SummaryQuality> summaryCallTypeQualityFilter,
            Set<GeneFilter> geneFilters, Collection<ConditionFilter> conditionFilters,
            Collection<DataType> dataTypeFilter, Class<U> callTypeCls) throws IllegalArgumentException {
        super(geneFilters, conditionFilters);

        this.dataTypeFilters = Collections.unmodifiableSet(
            dataTypeFilter == null? new HashSet<>(): new HashSet<>(dataTypeFilter));
        this.summaryCallTypeQualityFilter = Collections.unmodifiableMap(
                summaryCallTypeQualityFilter == null || summaryCallTypeQualityFilter.isEmpty()?

                        EnumSet.allOf(callTypeCls).stream()
                        .map(c -> new AbstractMap.SimpleEntry<>(c, SummaryQuality.values()[0]))
                        .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())):

                        new HashMap<>(summaryCallTypeQualityFilter));

        if (this.dataTypeFilters.contains(null)) {
            throw log.throwing(new IllegalStateException("No DataTypeFilter can be null."));
        }
        if (this.summaryCallTypeQualityFilter.keySet().contains(null)) {
            throw log.throwing(new IllegalStateException("No SummaryCallType can be null."));
        }
        if (this.summaryCallTypeQualityFilter.values().contains(null)) {
            throw log.throwing(new IllegalStateException("No SummaryQuality can be null."));
        }
    }
    
    /** 
     * Check fitlers.
     * 
     * @throws IllegalStateException    If some filters are not satisfactory.
     */
    //IMPORTANT: note that subclasses must override checkEmptyFilters as needed,
    //use in it super.checkEmptyFilters(), and call it in their constructor
    //(this cannot be done in the sub-class constructor, as subclasses might need
    //to set their own attributes before calling checkEmptyFilters)
    protected void checkEmptyFilters() throws IllegalStateException {
        log.entry();
        //To make sure we never pull all data in the database at once.
        if (this.getGeneFilters().isEmpty() && this.getConditionFilters().isEmpty()) {
            throw log.throwing(new IllegalStateException(
                    "At least a GeneFilter or a ConditionFilter must be provided."));
        }
        log.exit();
    }

    /**
     * @return  An unmodifiable {@code Set} of {@code DataType}s, allowing to configure 
     *          the filtering of data types with expression data.
     *          If several {@code DataType}s are configured, they are seen as "OR" conditions.
     */
    public Set<DataType> getDataTypeFilters() {
        return dataTypeFilters;
    }
    /**
     * @return  The {@code SummaryCallType} allowing to configure summary call type filtering.
     */
    public Map<U, SummaryQuality> getSummaryCallTypeQualityFilter() {
        return summaryCallTypeQualityFilter;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((dataTypeFilters == null) ? 0 : dataTypeFilters.hashCode());
        result = prime * result
                + ((summaryCallTypeQualityFilter == null) ? 0 : summaryCallTypeQualityFilter.hashCode());
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
        CallFilter<?, ?> other = (CallFilter<?, ?>) obj;
        if (dataTypeFilters == null) {
            if (other.dataTypeFilters != null) {
                return false;
            }
        } else if (!dataTypeFilters.equals(other.dataTypeFilters)) {
            return false;
        }
        if (summaryCallTypeQualityFilter == null) {
            if (other.summaryCallTypeQualityFilter != null) {
                return false;
            }
        } else if (!summaryCallTypeQualityFilter.equals(other.summaryCallTypeQualityFilter)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CallFilter [summaryCallTypeQualityFilter=").append(summaryCallTypeQualityFilter)
               .append(", dataTypeFilters=").append(dataTypeFilters)
               .append(", geneFilters=").append(getGeneFilters())
               .append(", conditionFilters=").append(getConditionFilters())
               .append("]");
        return builder.toString();
    }

    // TODO add unit test
    public boolean test(Call<?, T> call) {
        log.entry(call);
        
        if (call == null) {
            throw log.throwing(new IllegalArgumentException("ExpressionCall could not be null"));
        }
        if (call.getCallData() == null || call.getCallData().isEmpty()) {
            throw log.throwing(new IllegalArgumentException("ExpressionCallData could not be null or empty"));
        }

        // Filter according GeneFilter
        if (getGeneFilters() != null && !getGeneFilters().isEmpty()
                && getGeneFilters().stream().noneMatch(f -> f.test(call.getGene()))) {
            return log.exit(false);
        }

        // Filter according ConditionFilters
        if (getConditionFilters() != null && !getConditionFilters().isEmpty()
                && getConditionFilters().stream().noneMatch(f -> f.test(call.getCondition()))) {
            return log.exit(false);
        }
        
        // Filter according DataTypeFilter
        final Set<DataType> dataTypes = call.getCallData().stream()
            .map(cd -> cd.getDataType())
            .collect(Collectors.toSet());
        if (!dataTypes.isEmpty() 
            && dataTypeFilters != null && !dataTypeFilters.isEmpty()
            && dataTypeFilters.stream().noneMatch(f -> dataTypes.contains(f))) {
            log.debug("Data type {} not validated: not in {}", dataTypes, dataTypeFilters);
            return log.exit(false);
        }
        
        // Filter according SummaryCallTypeQualityFilter
        if (summaryCallTypeQualityFilter.entrySet().stream()
                .noneMatch(e -> e.getKey().equals(call.getSummaryCallType()) &&
                        call.getSummaryQuality().compareTo(e.getValue()) >= 0)) {

            log.debug("Summary call type and quality {}-{} not validated, should be one of {}",
                call.getSummaryCallType(), call.getSummaryQuality(), summaryCallTypeQualityFilter);
            return log.exit(false);
        }
        
        return log.exit(true);
    }
}
