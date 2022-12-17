package org.bgee.model.expressiondata.call;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.DataFilter;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.call.Call.DiffExpressionCall;
import org.bgee.model.expressiondata.call.Call.ExpressionCall;
import org.bgee.model.expressiondata.call.CallData.ExpressionCallData;
import org.bgee.model.gene.GeneFilter;

/**
 * A filter to parameterize queries to {@link CallService}. 
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 15.0, Apr. 2021
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

        /**
         * Convenient {@code Map} to provide to {@code ExpressionCallFilter} constructor
         * to request PRESENT expression calls of at least SILVER quality.
         */
        public static final Map<ExpressionSummary, SummaryQuality> SILVER_PRESENT_ARGUMENT =
                Collections.singletonMap(ExpressionSummary.EXPRESSED, SummaryQuality.SILVER);
        /**
         * Convenient {@code Map} to provide to {@code ExpressionCallFilter} constructor
         * to request ABSENT expression calls of at least SILVER quality.
         */
        public static final Map<ExpressionSummary, SummaryQuality> SILVER_ABSENT_ARGUMENT =
                Collections.singletonMap(ExpressionSummary.NOT_EXPRESSED, SummaryQuality.SILVER);
        /**
         * Convenient {@code Map} to provide to {@code ExpressionCallFilter} constructor
         * to request PRESENT expression calls of at least BRONZE quality.
         */
        public static final Map<ExpressionSummary, SummaryQuality> BRONZE_PRESENT_ARGUMENT =
                Collections.singletonMap(ExpressionSummary.EXPRESSED, SummaryQuality.BRONZE);
        /**
         * Convenient {@code Map} to request all calls (present and absent of at least bronze quality).
         * Not necessary to instantiate an {@code ExpressionCallFilter}, has a {@code null} value
         * can be provided as argument and would do the same, but useful in other contexts.
         */
        public static final Map<ExpressionSummary, SummaryQuality> ALL_CALLS =
                Collections.unmodifiableMap(EnumSet.allOf(SummaryCallType.ExpressionSummary.class)
                        .stream()
                        .collect(Collectors.toMap(c -> c, c -> SummaryQuality.values()[0])));
        /**
         * Convenient {@code Map} to provide to {@code ExpressionCallFilter} constructor
         * to request calls observed in the anatomical entity and cell type.
         */
        public static final Map<EnumSet<CallService.Attribute>, Boolean> ANAT_ENTITY_OBSERVED_DATA_ARGUMENT =
                //XXX: to replace with Java 9 Map.of
                Optional.of(EnumSet.of(
                        CallService.Attribute.ANAT_ENTITY_ID,
                        CallService.Attribute.CELL_TYPE_ID
                )).map(es -> {
                    Map<EnumSet<CallService.Attribute>, Boolean> map = new HashMap<>();
                    map.put(es, true);
                    return map;
                }).get();
        /**
         * Convenient {@code Map} to provide to {@code ExpressionCallFilter} constructor
         * to request calls observed in conditions considering all condition parameters.
         */
        public static final Map<EnumSet<CallService.Attribute>, Boolean> ALL_COND_PARAMS_OBSERVED_DATA_ARGUMENT =
                //XXX: to replace with Java 9 Map.of
                Optional.of(CallService.Attribute.getAllConditionParameters())
                .map(es -> {
                    Map<EnumSet<CallService.Attribute>, Boolean> map = new HashMap<>();
                    map.put(es, true);
                    return map;
                }).get();

        //XXX: maybe we can only allow to request observed calls (and not as well non-observed calls)
        //so that this Map would simply be an EnumSet (easier to use to instantiate a CallFilter)
        private final Map<EnumSet<CallService.Attribute>, Boolean> callObservedDataFilter;

        public ExpressionCallFilter(
                Map<SummaryCallType.ExpressionSummary, SummaryQuality> summaryCallTypeQualityFilter,
                Collection<GeneFilter> geneFilters,
                Map<EnumSet<CallService.Attribute>, Boolean> callObservedDataFilter) {
            this(summaryCallTypeQualityFilter, geneFilters, null, null, callObservedDataFilter);
        }
        /**
         * @param summaryCallTypeQualityFilter  A {@code Map} where keys are
         *                                      {@code SummaryCallType.ExpressionSummary}
         *                                      listing the call types requested,
         *                                      associated to the <strong>minimum</strong>
         *                                      {@code SummaryQuality} level
         *                                      requested for this call type.
         *                                      if {@code null} or empty, all
         *                                      {@code SummaryCallType.ExpressionSummary}s
         *                                      will be requested with the first level of
         *                                      in {@code SummaryQuality} as minimum quality.
         * @param geneFilters                   A {@code Collection} of {@code GeneFilter}s
         *                                      allowing to specify the genes or species
         *                                      to consider. If a same species ID is present
         *                                      in several {@code GeneFilter}s, or if {@code geneFilters}
         *                                      contains a {@code null} element, an
         *                                      {@code IllegalArgumentException} is thrown.
         *                                      At least one {@code GeneFilter} or one
         *                                      {@code CoditionFilter} must be provided,
         *                                      otherwise an {@code IllegalArgumentException} is thrown.
         * @param conditionFilters              A {@code Collection} of {@code ConditionFilter}s
         *                                      allowing to specify the requested parameters regarding
         *                                      conditions related to the expression calls. If several
         *                                      {@code ConditionFilter}s are provided, they are seen
         *                                      as "OR" conditions. Can be {@code null} or empty
         *                                      if some {@code GeneFilter}s are provided.
         *                                      if contains a {@code null} element, an
         *                                      {@code IllegalArgumentException} is thrown.
         *                                      At least one {@code GeneFilter} or one
         *                                      {@code CoditionFilter} must be provided,
         *                                      otherwise an {@code IllegalArgumentException} is thrown.
         * @param dataTypeFilter                A {@code Collection} of {@code DataType}s
         *                                      allowing to specify the data types to consider
         *                                      to retrieve expression calls. If {@code null}
         *                                      or empty, all data types will be considered.
         * @param callObservedDataFilter        A {@code Map} to specify whether calls retrieved
         *                                      should have been observed according to combinations
         *                                      of condition parameters. Keys in the {@code Map} are
         *                                      {@code EnumSet}s of {@code CallService.Attribute}s
         *                                      that must be condition parameters (see
         *                                      {@link CallService.Attribute#isConditionParameter()}),
         *                                      defining the combination of condition parameters
         *                                      to target; the associated value being a {@code Boolean}
         *                                      defining whether the call must have been directly observed
         *                                      according to the condition parameter combination
         *                                      (if {@code true}), or not (if {@code false}, calls produced
         *                                      only from propagation on the related condition parameters).
         *                                      If a key is {@code null} or empty or contains
         *                                      {@code CallService.Attribute}s that are not
         *                                      condition parameters, or a value is {@code null},
         *                                      an {@code IllegalArgumentException} is thrown.
         * @throws IllegalArgumentException     If no {@code GeneFilter} is provided in {@code geneFilters}
         *                                      and no {@code ConditionFilter} in {@code conditionFilters}.
         *                                      Or if a same species ID is present
         *                                      in several {@code GeneFilter}s. Or if {@code geneFilters}
         *                                      or {@code conditionFilters} contains a {@code null} element.
         *                                      In {@code callObservedDataFilter} if a key is
         *                                      {@code null} or empty or contains
         *                                      {@code CallService.Attribute}s that are not
         *                                      condition parameters, or a value is {@code null}.
         */
        public ExpressionCallFilter(
                Map<SummaryCallType.ExpressionSummary, SummaryQuality> summaryCallTypeQualityFilter,
                Collection<GeneFilter> geneFilters, Collection<ConditionFilter> conditionFilters,
                Collection<DataType> dataTypeFilter,
                Map<EnumSet<CallService.Attribute>, Boolean> callObservedDataFilter)
                        throws IllegalArgumentException {
            super(summaryCallTypeQualityFilter, geneFilters, conditionFilters, dataTypeFilter,
                    SummaryCallType.ExpressionSummary.class);

            if (callObservedDataFilter != null && callObservedDataFilter.entrySet().stream()
                    .anyMatch(e -> e.getKey() == null || e.getKey().isEmpty() || e.getValue() == null ||
                                  e.getKey().stream().anyMatch(a -> !a.isConditionParameter()))) {
                throw log.throwing(new IllegalArgumentException("Only condition parameters, non-null, "
                        + "and non-null Booleans are accepted in the Map of callObservedDataFilter"));
            }

            //we will use defensive copying, there is no unmodifiable EnumSet
            this.callObservedDataFilter = callObservedDataFilter == null? new HashMap<>():
                callObservedDataFilter.entrySet().stream()
                .collect(Collectors.toMap(e -> EnumSet.copyOf(e.getKey()), e -> e.getValue()));

            try {
                this.checkEmptyFilters();
            } catch (IllegalStateException e) {
                throw log.throwing(new IllegalArgumentException("Incorrect filters provided", e));
            }
        }

        @Override
        protected void checkEmptyFilters() throws IllegalStateException {
            log.traceEntry();
            //nothing special in this subclass, the observedData filter alone is not enough,
            //so we let the superclass decide whether it's happy about the filters it manages.
            super.checkEmptyFilters();
            log.traceExit();
        }

        /**
         * @return  A {@code Map} to specify whether calls retrieved should have been observed
         *          according to combinations of condition parameters. Keys in the {@code Map} are
         *          {@code EnumSet}s of {@code CallService.Attribute}s that are condition parameters
         *          (see {@link CallService.Attribute#isConditionParameter()}), defining
         *          the combination of condition parameters to target; the associated value
         *          being a {@code Boolean} defining whether the call must have been directly observed
         *          according to the condition parameter combination (if {@code true}), or not
         *          (if {@code false}, calls produced only from propagation on the related
         *          condition parameters).
         *          The {@code Boolean} values are never {@code null}.
         *          The filtering used only the data types defined in this {@code ExpressionCallFilter}.
         */
        public Map<EnumSet<CallService.Attribute>, Boolean> getCallObservedDataFilter() {
            //defensive copying, there is no unmodifiable EnumSet
            return callObservedDataFilter.entrySet().stream()
                    .collect(Collectors.toMap(e -> EnumSet.copyOf(e.getKey()), e -> e.getValue()));
        }

        @Override
        public boolean test(ExpressionCall call) {
            // Filter on common fields of Calls
            if (!super.test(call)) {
                return log.traceExit(false);
            }
            //If no filter needed on observed data, that's it.
            if (callObservedDataFilter.isEmpty()) {
                return log.traceExit(true);
            }
            // Filter on observed data
            if (call.getDataPropagation() == null) {
                throw log.throwing(new IllegalArgumentException(
                        "The provided Call does not allow to retrieve observedData information"));
            }
            //We need to rethink this DataPropagation object
            throw log.throwing(new UnsupportedOperationException("test of callObservedDataFilter to implement"));

//            BiFunction<Boolean, PropagationState, Boolean> filterAndStateMatch =
//                    (observedDataFilter, condParamPropState) -> {
//                        if (observedDataFilter != null) {
//                            if (condParamPropState == null || condParamPropState
//                                    .isIncludingObservedData() == null) {
//                                throw log.throwing(new IllegalArgumentException(
//                                        "The provided Call does not allow to retrieve observedData information"));
//                            }
//                            if (!observedDataFilter.equals(condParamPropState.isIncludingObservedData())) {
//                                return log.traceExit(false);
//                            }
//                        }
//                        return log.traceExit(true);
//                    };
//            //TODO: probably we need to change DataPropagation to also use a Map
//            //where keys are condition parameters and the value a propagation state
//            if (callObservedDataFilter.entrySet().stream().anyMatch(e -> {
//                switch (e.getKey()) {
//                case ANAT_ENTITY_ID:
//                    return !filterAndStateMatch.apply(e.getValue(),
//                            call.getDataPropagation().getAnatEntityPropagationState());
//                case DEV_STAGE_ID:
//                    return !filterAndStateMatch.apply(e.getValue(),
//                            call.getDataPropagation().getDevStagePropagationState());
//                case CELL_TYPE_ID:
//                    return !filterAndStateMatch.apply(e.getValue(),
//                            call.getDataPropagation().getCellTypePropagationState());
//                case SEX_ID:
//                    return !filterAndStateMatch.apply(e.getValue(),
//                            call.getDataPropagation().getSexPropagationState());
//                case STRAIN_ID:
//                    return !filterAndStateMatch.apply(e.getValue(),
//                            call.getDataPropagation().getStrainPropagationState());
//                default:
//                    throw log.throwing(new IllegalStateException("Unsupported condition parameter: "
//                            + e.getKey()));
//                }
//            })) {
//                return log.traceExit(false);
//            }
//
//            return log.traceExit(true);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result
                    + ((callObservedDataFilter == null) ? 0 : callObservedDataFilter.hashCode());
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
            if (callObservedDataFilter == null) {
                if (other.callObservedDataFilter != null) {
                    return false;
                }
            } else if (!callObservedDataFilter.equals(other.callObservedDataFilter)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ExpressionCallFilter [")
                   .append("callObservedDataFilter=").append(callObservedDataFilter.entrySet().stream()
                           .map(e -> e.getKey() + ": " + e.getValue())
                           .collect(Collectors.joining(" - ")))
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
                Collection<GeneFilter> geneFilters, Collection<ConditionFilter> conditionFilters,
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
    private final EnumSet<DataType> dataTypeFilters;

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
            Collection<GeneFilter> geneFilters, Collection<ConditionFilter> conditionFilters,
            Collection<DataType> dataTypeFilter, Class<U> callTypeCls) throws IllegalArgumentException {
        super(geneFilters, conditionFilters);

        if (dataTypeFilter != null && dataTypeFilter.contains(null)) {
            throw log.throwing(new IllegalStateException("No DataTypeFilter can be null."));
        }
        if (summaryCallTypeQualityFilter != null &&
                summaryCallTypeQualityFilter.keySet().contains(null)) {
            throw log.throwing(new IllegalStateException("No SummaryCallType can be null."));
        }
        if (summaryCallTypeQualityFilter != null &&
                summaryCallTypeQualityFilter.values().contains(null)) {
            throw log.throwing(new IllegalStateException("No SummaryQuality can be null."));
        }

        this.dataTypeFilters = dataTypeFilter == null || dataTypeFilter.isEmpty()?
                EnumSet.allOf(DataType.class): EnumSet.copyOf(dataTypeFilter);
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
        log.traceEntry();
        //To make sure we never pull all data in the database at once.
        if (this.getGeneFilters().isEmpty() && this.getConditionFilters().isEmpty()) {
            throw log.throwing(new IllegalStateException(
                    "At least a GeneFilter or a ConditionFilter must be provided."));
        }
        log.traceExit();
    }

    /**
     * @return  An {@code EnumSet} of {@code DataType}s, allowing to configure
     *          the filtering of data types with expression data.
     *          This {@code EnumSet} is a copy, modifying it will not be reflected in this class.
     */
    public EnumSet<DataType> getDataTypeFilters() {
        //defensive copying, no unmodifiableEnumSet
        return EnumSet.copyOf(dataTypeFilters);
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
        log.traceEntry("{}", call);
        
        if (call == null) {
            throw log.throwing(new IllegalArgumentException("ExpressionCall could not be null"));
        }
        if (call.getCallData() == null || call.getCallData().isEmpty()) {
            throw log.throwing(new IllegalArgumentException("ExpressionCallData could not be null or empty"));
        }

        // Filter according GeneFilter
        if (getGeneFilters() != null && !getGeneFilters().isEmpty()
                && getGeneFilters().stream().noneMatch(f -> f.test(call.getGene()))) {
            return log.traceExit(false);
        }

        // Filter according ConditionFilters
        if (getConditionFilters() != null && !getConditionFilters().isEmpty()
                && getConditionFilters().stream().noneMatch(f -> f.test(call.getCondition()))) {
            return log.traceExit(false);
        }
        
        // Filter according DataTypeFilter
        final Set<DataType> dataTypes = call.getCallData().stream()
            .map(cd -> cd.getDataType())
            .collect(Collectors.toSet());
        if (!dataTypes.isEmpty() 
            && dataTypeFilters != null && !dataTypeFilters.isEmpty()
            && dataTypeFilters.stream().noneMatch(f -> dataTypes.contains(f))) {
            log.debug("Data type {} not validated: not in {}", dataTypes, dataTypeFilters);
            return log.traceExit(false);
        }
        
        // Filter according SummaryCallTypeQualityFilter
        if (summaryCallTypeQualityFilter.entrySet().stream()
                .noneMatch(e -> e.getKey().equals(call.getSummaryCallType()) &&
                        call.getSummaryQuality().compareTo(e.getValue()) >= 0)) {

            log.debug("Summary call type and quality {}-{} not validated, should be one of {}",
                call.getSummaryCallType(), call.getSummaryQuality(), summaryCallTypeQualityFilter);
            return log.traceExit(false);
        }
        
        return log.traceExit(true);
    }
}
