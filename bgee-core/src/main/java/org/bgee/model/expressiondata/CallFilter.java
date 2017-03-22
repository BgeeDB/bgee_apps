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
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.gene.GeneFilter;

/**
 * A filter to parameterize queries to {@link CallService}. 
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Feb. 2017
 * @since   Bgee 13, Oct. 2015
 *
 * @param T The type of {@code CallData} to be used by this {@code CallFilter}. 
 *          Can be declared as {@code CallData}, to include a mixture of {@code CallData} subtypes, 
 *          or as a specific subtype, for instance, {@code ExpressionCallData}.
 * @param U The type of {@code SummaryCallType} to be used by this {@code CallFilter}.
 */
//XXX: would several CallFilters represent AND or OR conditions.
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
//***********************
//XXX: update FEB. 2017. We decided to remove the CallData from this class.
//Because the quality of a call is now computed over all data types, 
//so we don't want to filter on data quality per data type any more. 
//Also, so far we don't need to filter calls based on propagation per data type 
//(e.g., calls including substructures for Affymetrix, not including substructures for RNA-Seq).
//If these two points wanted to be achieved, we could use the new fields of, e.g., ExpressionCallData: 
// absentHighParentExpCount, presentHighDescExpCount, etc.
public abstract class CallFilter<T extends CallData<?>, U extends Enum<U> & SummaryCallType> implements Predicate<Call<?, T>> {
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
    extends CallFilter<ExpressionCallData, SummaryCallType.ExpressionSummary> {
        
        private final Boolean conditionObservedData;

        private final Boolean anatEntityObservedData;
        private final Boolean devStageObservedData;

        public ExpressionCallFilter(
                Map<SummaryCallType.ExpressionSummary, SummaryQuality> summaryCallTypeQualityFilter,
                Set<GeneFilter> geneFilters, Collection<ConditionFilter> conditionFilters,
                Collection<DataType> dataTypeFilter, Boolean conditionObservedData,
                Boolean anatEntityObservedData, Boolean devStageObservedData)
                        throws IllegalArgumentException {
            super(summaryCallTypeQualityFilter, geneFilters, conditionFilters, dataTypeFilter,
                    SummaryCallType.ExpressionSummary.class);
            this.conditionObservedData = conditionObservedData;
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
        
        public Boolean getConditionObservedData() {
            return conditionObservedData;
        }
        public Boolean getAnatEntityObservedData() {
            return anatEntityObservedData;
        }
        public Boolean getDevStageObservedData() {
            return devStageObservedData;
        }

        @Override
        public boolean test(Call<?, ExpressionCallData> call) {
            // Filter on common fields of Calls
            if (!super.test(call)) {
                return log.exit(false);
            }
            // Filter on observed data
            //XXX: actually, we can now filter calls based on this information directly in the DAO,
            //so maybe we should force to retrieve this information in the Call solely to test it.
            if (conditionObservedData != null || anatEntityObservedData != null ||
                    devStageObservedData != null) {

                if (call.getDataPropagation() == null) {
                    throw log.throwing(new IllegalArgumentException(
                            "The provided Call does not allow to retrieve observedData information"));
                }
                if (conditionObservedData != null &&
                        call.getDataPropagation().isIncludingObservedData() == null) {
                    throw log.throwing(new IllegalArgumentException(
                            "The provided Call does not allow to retrieve observedData information"));
                }
                if (!conditionObservedData.equals(call.getDataPropagation().isIncludingObservedData())) {
                    return log.exit(false);
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
            result = prime * result + ((conditionObservedData == null) ? 0 : conditionObservedData.hashCode());
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
            if (conditionObservedData == null) {
                if (other.conditionObservedData != null) {
                    return false;
                }
            } else if (!conditionObservedData.equals(other.conditionObservedData)) {
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
            builder.append("ExpressionCallFilter [conditionObservedData=").append(conditionObservedData)
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
    extends CallFilter<ExpressionCallData, SummaryCallType.DiffExpressionSummary> {
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
    }
    
    /**
     * @see #getGeneFilters()
     */
    //XXX: The only problem with using directly ConditionFilters and CallDatas in this class, 
    //is that GeneFilters are costly to use in a query; using the class CallDataConditionFilter 
    //was allowing to have a same GeneFilter to target several conditions/call data combinations. 
    //Now, the same query would be doable by using several CallFilters, but with a same GeneFilter 
    //reused several times. This is costly, but we could have a mechanism to provide a global GeneFilter 
    //to the DAO when we see it is always the same GeneFilter used. 
    //I think it's worth it for the simplification it allows in the class CallFilter.
    private final Set<GeneFilter> geneFilters;
    
    /**
     * @see #getConditionFilters()
     */
    //XXX: all parameters are OR conditions
    private final Set<ConditionFilter> conditionFilters;

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

        this.geneFilters = Collections.unmodifiableSet(
                geneFilters == null? new HashSet<>(): new HashSet<>(geneFilters));
        this.conditionFilters = Collections.unmodifiableSet(
            conditionFilters == null? new HashSet<>(): new HashSet<>(conditionFilters));
        this.dataTypeFilters = Collections.unmodifiableSet(
            dataTypeFilter == null? new HashSet<>(): new HashSet<>(dataTypeFilter));
        this.summaryCallTypeQualityFilter = Collections.unmodifiableMap(
                summaryCallTypeQualityFilter == null?

                        EnumSet.allOf(callTypeCls).stream()
                        .map(c -> new AbstractMap.SimpleEntry<>(c, SummaryQuality.BRONZE))
                        .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())):

                        new HashMap<>(summaryCallTypeQualityFilter));
        //just to make sure bronze quality is the lowest quality/that qualities are correctly ordered
        assert SummaryQuality.BRONZE.equals(SummaryQuality.values()[0]);

        if (this.conditionFilters.contains(null)) {
            throw log.throwing(new IllegalStateException("No ConditionFilter can be null."));
        }
        if (this.dataTypeFilters.contains(null)) {
            throw log.throwing(new IllegalStateException("No DataTypeFilter can be null."));
        }
        if (this.summaryCallTypeQualityFilter.keySet().contains(null)) {
            throw log.throwing(new IllegalStateException("No SummaryCallType can be null."));
        }
        if (this.summaryCallTypeQualityFilter.values().contains(null)) {
            throw log.throwing(new IllegalStateException("No SummaryQuality can be null."));
        }

        //make sure we don't have a same species in different GeneFilters
        if (this.geneFilters.stream().collect(Collectors.groupingBy(gf -> gf.getSpeciesId()))
                .values().stream().anyMatch(l -> l.size() > 1)) {
            throw log.throwing(new IllegalArgumentException(
                    "A species ID must be present in only one GeneFilter."));
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
        if (this.geneFilters.isEmpty() && this.conditionFilters.isEmpty()) {
            throw log.throwing(new IllegalStateException(
                    "At least a GeneFilter or a ConditionFilter must be provided."));
        }
        log.exit();
    }
    
    /**
     * @return  An unmodifiable {@code Set} {@code GeneFilter}s allowing to configure gene-related
     *          filtering. If several {@code GeneFilter}s are configured, they are seen as "OR" conditions.
     */
    public Set<GeneFilter> getGeneFilters() {
        return geneFilters;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code ConditionFilter}s, allowing to configure 
     *          the filtering of conditions with expression data. If several 
     *          {@code ConditionFilter}s are configured, they are seen as "OR" conditions.
     */
    public Set<ConditionFilter> getConditionFilters() {
        return conditionFilters;
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
        int result = 1;
        result = prime * result + ((conditionFilters == null) ? 0 : conditionFilters.hashCode());
        result = prime * result + ((dataTypeFilters == null) ? 0 : dataTypeFilters.hashCode());
        result = prime * result + ((geneFilters == null) ? 0 : geneFilters.hashCode());
        result = prime * result
                + ((summaryCallTypeQualityFilter == null) ? 0 : summaryCallTypeQualityFilter.hashCode());
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
        CallFilter<?, ?> other = (CallFilter<?, ?>) obj;
        if (conditionFilters == null) {
            if (other.conditionFilters != null) {
                return false;
            }
        } else if (!conditionFilters.equals(other.conditionFilters)) {
            return false;
        }
        if (dataTypeFilters == null) {
            if (other.dataTypeFilters != null) {
                return false;
            }
        } else if (!dataTypeFilters.equals(other.dataTypeFilters)) {
            return false;
        }
        if (geneFilters == null) {
            if (other.geneFilters != null) {
                return false;
            }
        } else if (!geneFilters.equals(other.geneFilters)) {
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
               .append(", geneFilters=").append(geneFilters)
               .append(", conditionFilters=").append(conditionFilters)
               .append("]");
        return builder.toString();
    }

    @Override
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
        if (geneFilters != null && !geneFilters.isEmpty()
                && geneFilters.stream().noneMatch(f -> f.test(call.getGene()))) {
            return log.exit(false);
        }

        // Filter according ConditionFilters
        if (conditionFilters != null && !conditionFilters.isEmpty()
                && conditionFilters.stream().noneMatch(f -> f.test(call.getCondition()))) {
            return log.exit(false);
        }
        
        // Filter according DataTypeFilter
        final Set<DataType> dataTypes = call.getCallData().stream()
            .map(cd -> cd.getDataType())
            .collect(Collectors.toSet());
        if (!dataTypes.isEmpty() 
            && dataTypeFilters != null && !dataTypeFilters.isEmpty()
            && !dataTypeFilters.stream().anyMatch(f -> dataTypes.contains(f))) {
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
