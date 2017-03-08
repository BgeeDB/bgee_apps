package org.bgee.model.expressiondata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.CallData.DiffExpressionCallData;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.DiffExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
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
public abstract class CallFilter<T extends CallData<?>> implements Predicate<Call<?, T>> {
    private final static Logger log = LogManager.getLogger(CallFilter.class.getName());
    
    /**
     * A {@code CallFilter} for {@code ExpressionCall}.
     * 
     * @author  Frederic Bastian
     * @author  Valentine Rech de Laval
     * @version Bgee 14, Mar. 2017
     * @since   Bgee 13
     */
    public static class ExpressionCallFilter extends CallFilter<ExpressionCallData> {
        
        private final Boolean observedDataOnly;
        
        /**
         * Basic constructor allowing to provide one {@code SummaryCallType} filter.
         * 
         * @param summaryCallTypeFilter A {@code ExpressionSummary} to configure the filtering 
         *                              based on the call type (for instance, 
         *                              {@link ExpressionSummary#EXPRESSED EXPRESSED).
         * @see ExpressionCallFilter#ExpressionCallFilter(GeneFilter, Collection, Collection,
         *                                  SummaryQuality, ExpressionSummary, DataPropagation)
         */
        public ExpressionCallFilter(ExpressionSummary summaryCallTypeFilter) {
            this(null, null, summaryCallTypeFilter);
        }
        
        public ExpressionCallFilter(GeneFilter geneFilter, Collection<ConditionFilter> conditionFilters,
            ExpressionSummary summaryCallTypeFilter) {
            this(geneFilter, conditionFilters, summaryCallTypeFilter, false);
        }
        
        public ExpressionCallFilter(GeneFilter geneFilter, Collection<ConditionFilter> conditionFilters,
            ExpressionSummary summaryCallTypeFilter, Boolean observedDataOnly) {
            this(geneFilter, conditionFilters, null, null, summaryCallTypeFilter, observedDataOnly);
        }

        /**
         * See {@link CallFilter#CallFilter(GeneFilter, Collection, Collection, SummaryQuality, SummaryCallType)}.
         */
        public ExpressionCallFilter(GeneFilter geneFilter, Collection<ConditionFilter> conditionFilters,
            Collection<DataType> dataTypeFilter, SummaryQuality summaryQualityFilter,
            ExpressionSummary summaryCallTypeFilter, Boolean observedDataOnly)
                throws IllegalArgumentException {
            super(geneFilter, conditionFilters, dataTypeFilter, summaryQualityFilter, summaryCallTypeFilter);
            if (this.checkEmptyFilters(geneFilter, conditionFilters, dataTypeFilter, summaryQualityFilter,
                summaryCallTypeFilter, observedDataOnly)) {
                throw log.throwing(new IllegalArgumentException("All filters could not be empty"));
            }
            this.observedDataOnly = observedDataOnly;
        }

        /** 
         * Check that all provided filters are not empty.
         * 
         * @param geneFilter            A {@code GeneFilter} to configure gene-related filtering.
         * @param conditionFilters      A {@code Collection} of {@code ConditionFilter}s to configure 
         *                              the filtering of conditions with expression data. If several 
         *                              {@code ConditionFilter}s are provided, they are seen as
         *                              "OR" conditions.
         * @param dataTypeFilter        A {@code Collection} of {@code DataType}s to configure 
         *                              the filtering of data types with expression data. If several 
         *                              {@code DataType}s are provided, they are seen as
         *                              "OR" conditions.
         * @param summaryQualityFilter  A {@code SummaryQuality} to configure quality-related filtering.
         * @param summaryCallTypeFilter An {@code ExpressionSummary} to configure call type-related filtering.
         * @param propagationFilter     A {@code DataPropagation} to configure propagation-related filtering.
         * @return                      The {@code boolean} defining whether all provided filters
         *                              are empty or not. If {@code true}, all filters are empty.
         */
        protected boolean checkEmptyFilters(GeneFilter geneFilter, Collection<ConditionFilter> conditionFilters,
            Collection<DataType> dataTypeFilter, SummaryQuality summaryQualityFilter,
            ExpressionSummary summaryCallTypeFilter, Boolean observedDataOnly) {
            log.entry(geneFilter, conditionFilters, dataTypeFilter, summaryQualityFilter,
                summaryCallTypeFilter, observedDataOnly);
            return log.exit(super.checkEmptyFilters(geneFilter, conditionFilters, dataTypeFilter,
                summaryQualityFilter, summaryCallTypeFilter) && observedDataOnly == null);
        }
        
        public boolean isObservedDataOnly() {
            return observedDataOnly;
        }

        @Override
        public boolean test(Call<?, ExpressionCallData> call) {
            // Filter on common fields of Calls
            if (!super.test(call)) {
                return log.exit(false);
            }
            // Filter on observed data
            if (observedDataOnly != null && observedDataOnly != call.getIsObservedData()) {
                return log.exit(false);
            }
            return log.exit(true);
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
    public static class DiffExpressionCallFilter extends CallFilter<DiffExpressionCallData> {
        /**
         * Basic constructor allowing to provide one {@code DiffExpressionCallData} filter.
         * 
         * @param summaryCallTypeFilter A {@code DiffExpressionSummary} to configure the filtering 
         *                              based on the call type (for instance,
                                        {@link DiffExpressionSummary#OVER_EXPRESSED OVER_EXPRESSED}).
         * @see DiffExpressionCallFilter#DiffExpressionCallFilter(
         *              GeneFilter, Collection, Collection, SummaryQuality, DiffExpressionSummary)
         */
        public DiffExpressionCallFilter(DiffExpressionSummary summaryCallTypeFilter) {
            this(null, null, null, null, summaryCallTypeFilter);
        }
        /**
         * See {@link CallFilter#CallFilter(GeneFilter, Collection, Collection, SummaryQuality, SummaryCallType)}.
         */
        public DiffExpressionCallFilter(GeneFilter geneFilter, Collection<ConditionFilter> conditionFilters, 
            Collection<DataType> dataTypeFilter, SummaryQuality summaryQualityFilter,
            DiffExpressionSummary summaryCallTypeFilter) throws IllegalArgumentException {
            super(geneFilter, conditionFilters, dataTypeFilter, summaryQualityFilter, summaryCallTypeFilter);
            if (super.checkEmptyFilters(geneFilter, conditionFilters, dataTypeFilter, summaryQualityFilter,
                summaryCallTypeFilter)) {
                throw log.throwing(new IllegalArgumentException("All filters could not be empty"));
            }
        }
    }
    
    /**
     * @see #getGeneFilter()
     */
    //XXX: The only problem with using directly ConditionFilters and CallDatas in this class, 
    //is that GeneFilters are costly to use in a query; using the class CallDataConditionFilter 
    //was allowing to have a same GeneFilter to target several conditions/call data combinations. 
    //Now, the same query would be doable by using several CallFilters, but with a same GeneFilter 
    //reused several times. This is costly, but we could have a mechanism to provide a global GeneFilter 
    //to the DAO when we see it is always the same GeneFilter used. 
    //I think it's worth it for the simplification it allows in the class CallFilter.
    private final GeneFilter geneFilter;
    
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
    private final Set<DataType> dataTypeFilter;
    
    /**
     * @see #getSummaryQualityFilter()
     */
    private final SummaryQuality summaryQualityFilter;

    /**
     * @see #getSummaryCallTypeFilter()
     */
    private final SummaryCallType summaryCallTypeFilter;
    
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
     * @param dataTypeFilter        TODO javadoc
     * @param summaryQualityFilter  TODO javadoc
     * @throws IllegalArgumentException If {@code callDataFilters} is {@code null} or empty, 
     *                                  or contains a {@code null} {@code CallData}; 
     *                                  or if the {@code ExpressionCallData}s provided target 
     *                                  a redundant combination of {@code CallType} and {@code DataType}; 
     *                                  or if the {@code DiffExpressionCallData}s provided target 
     *                                  a redundant combination of {@code CallType}, {@code DataType}, 
     *                                  and {@code DiffExpressionFactor}.
     */
    public CallFilter(GeneFilter geneFilter, Collection<ConditionFilter> conditionFilters,
        Collection<DataType> dataTypeFilter, SummaryQuality summaryQualityFilter,
        SummaryCallType summaryCallTypeFilter) throws IllegalArgumentException {        
        this.geneFilter = geneFilter;
        this.conditionFilters = Collections.unmodifiableSet(
            conditionFilters == null? new HashSet<>(): new HashSet<>(conditionFilters));
        this.dataTypeFilter = Collections.unmodifiableSet(
            dataTypeFilter == null? new HashSet<>(): new HashSet<>(dataTypeFilter));
        this.summaryQualityFilter = summaryQualityFilter;
        this.summaryCallTypeFilter = summaryCallTypeFilter;
    }
    
    /** 
     * Check that all provided filters are not empty.
     * 
     * @param geneFilter            A {@code GeneFilter} to configure gene-related filtering.
     * @param conditionFilters      A {@code Collection} of {@code ConditionFilter}s to configure 
     *                              the filtering of conditions with expression data. If several 
     *                              {@code ConditionFilter}s are provided, they are seen as
     *                              "OR" conditions.
     * @param dataTypeFilter        A {@code Collection} of {@code DataType}s to configure 
     *                              the filtering of data types with expression data. If several 
     *                              {@code DataType}s are provided, they are seen as
     *                              "OR" conditions.
     * @param summaryQualityFilter  A {@code SummaryQuality} to configure quality-related filtering.
     * @param summaryCallTypeFilter An {@code ExpressionSummary} to configure call type-related filtering.
     * @return                      The {@code boolean} defining whether all provided filters
     *                              are empty or not. If {@code true}, all filters are empty.
     */
    protected boolean checkEmptyFilters(GeneFilter geneFilter, Collection<ConditionFilter> conditionFilters,
        Collection<DataType> dataTypeFilter, SummaryQuality summaryQualityFilter,
        SummaryCallType summaryCallTypeFilter) {
        log.entry(geneFilter, conditionFilters, dataTypeFilter, summaryQualityFilter, summaryCallTypeFilter);
        return log.exit(geneFilter == null &&
            (conditionFilters == null || conditionFilters.isEmpty() || conditionFilters.contains(null)) &&
            (dataTypeFilter == null || dataTypeFilter.isEmpty() || dataTypeFilter.contains(null)) &&
            summaryQualityFilter == null && summaryCallTypeFilter == null);
    }
    
    /**
     * @return  The {@code GeneFilter} allowing to configure gene-related filtering.
     */
    public GeneFilter getGeneFilter() {
        return geneFilter;
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
    public Set<DataType> getDataTypeFilter() {
        return dataTypeFilter;
    }

    /**
     * @return  The {@code SummaryQuality} allowing to configure summary quality filtering.
     */
    public SummaryQuality getSummaryQualityFilter() {
        return summaryQualityFilter;
    }
    
    /**
     * @return  The {@code SummaryCallType} allowing to configure summary call type filtering.
     */
    public SummaryCallType getSummaryCallTypeFilter() {
        return summaryCallTypeFilter;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((geneFilter == null) ? 0 : geneFilter.hashCode());
        result = prime * result + ((conditionFilters == null) ? 0 : conditionFilters.hashCode());
        result = prime * result + ((dataTypeFilter == null) ? 0 : dataTypeFilter.hashCode());
        result = prime * result + ((summaryQualityFilter == null) ? 0 : summaryQualityFilter.hashCode());
        result = prime * result + ((summaryCallTypeFilter == null) ? 0 : summaryCallTypeFilter.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CallFilter<?> other = (CallFilter<?>) obj;
        if (geneFilter == null) {
            if (other.geneFilter != null)
                return false;
        } else if (!geneFilter.equals(other.geneFilter))
            return false;
        if (conditionFilters == null) {
            if (other.conditionFilters != null)
                return false;
        } else if (!conditionFilters.equals(other.conditionFilters))
            return false;
        if (dataTypeFilter == null) {
            if (other.dataTypeFilter != null)
                return false;
        } else if (!dataTypeFilter.equals(other.dataTypeFilter))
            return false;
        if (summaryQualityFilter != other.summaryQualityFilter)
            return false;
        if (summaryCallTypeFilter != other.summaryCallTypeFilter)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "CallFilter [geneFilter=" + geneFilter 
            + ", conditionFilters=" + conditionFilters 
            + ", dataTypeFilter=" + dataTypeFilter
            + ", summaryQualityFilter=" + summaryQualityFilter
            + ", summaryCallTypeFilter=" + summaryCallTypeFilter + "]";
    }

    @Override
    // FIXME add unit test
    public boolean test(Call<?, T> call) {
        log.entry(call);
        
        if (call == null) {
            throw log.throwing(new IllegalArgumentException("ExpressionCall could not be null"));
        }
        if (call.getCallData() == null || call.getCallData().isEmpty()) {
            throw log.throwing(new IllegalArgumentException("ExpressionCallData could not be null or empty"));
        }

        // Filter according GeneFilter
        if (geneFilter != null && !geneFilter.test(call.getGene())) {
            log.debug("Gene {} not validated: not in {}", call.getGene(), geneFilter.getGeneIds());
            return log.exit(false);
        }

        // Filter according ConditionFilters
        if (conditionFilters != null && !conditionFilters.isEmpty()
                && !conditionFilters.stream().anyMatch(f -> f.test(call.getCondition()))) {
            return log.exit(false);
        }
        
        // Filter according DataTypeFilter
        final Set<DataType> dataTypes = call.getCallData().stream()
            .map(cd -> cd.getDataType())
            .collect(Collectors.toSet());
        if (!dataTypes.isEmpty() 
            && dataTypeFilter != null && !dataTypeFilter.isEmpty()
            && !dataTypeFilter.stream().anyMatch(f -> dataTypes.contains(f))) {
            log.debug("Data type {} not validated: not in {}", dataTypes, dataTypeFilter);
            return log.exit(false);
        }

        // Filter according SummaryQualityFilter
        if (summaryQualityFilter != null && call.getSummaryQuality() != null
            && summaryQualityFilter.compareTo(call.getSummaryQuality()) > 0) {
            log.debug(summaryQualityFilter.compareTo(call.getSummaryQuality()));
            log.debug("Summary quality {} not validated: should be at least {}",
                call.getSummaryQuality(), summaryQualityFilter);
            return log.exit(false);
        }

        // Filter according SummaryCallTypeFilter (EXPRESSED, NOT_EXPRESSED, etc.)
        if (summaryCallTypeFilter != null 
            && summaryCallTypeFilter != call.getSummaryCallType()) {
            log.debug("Summary call type {} not validated: should be {}",
                call.getSummaryCallType(), summaryCallTypeFilter);
            return log.exit(false);
        }
        
        return log.exit(true);
    }
}
