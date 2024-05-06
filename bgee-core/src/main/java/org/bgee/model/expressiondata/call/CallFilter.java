package org.bgee.model.expressiondata.call;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.DataFilter;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
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
 * @param V The type of condition filter used by this {@code CallFilter}.
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
public abstract class CallFilter<T extends CallData<?>, U extends Enum<U> & SummaryCallType,
//TODO put back V extends BaseConditionFilter2 or something, once refactoring is done
V> extends DataFilter<V> {
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
    extends CallFilter<ExpressionCallData, SummaryCallType.ExpressionSummary, ConditionFilter> {

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
        // => DONE in ExpressionCallFilter2
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
         *                                      {@code EnumSet}s of condition parameters,
         *                                      defining the combination of condition parameters
         *                                      to target; the associated value being a {@code Boolean}
         *                                      defining whether the call must have been directly observed
         *                                      according to the condition parameter combination
         *                                      (if {@code true}), or not (if {@code false}, calls produced
         *                                      only from propagation on the related condition parameters).
         *                                      If a key is {@code null} or empty, or a value is {@code null},
         *                                      an {@code IllegalArgumentException} is thrown.
         * @throws IllegalArgumentException     If no {@code GeneFilter} is provided in {@code geneFilters}
         *                                      and no {@code ConditionFilter} in {@code conditionFilters}.
         *                                      Or if a same species ID is present
         *                                      in several {@code GeneFilter}s. Or if {@code geneFilters}
         *                                      or {@code conditionFilters} contains a {@code null} element.
         *                                      In {@code callObservedDataFilter} if a key is
         *                                      {@code null} or empty or a value is {@code null}.
         */
        public ExpressionCallFilter(
                Map<SummaryCallType.ExpressionSummary, SummaryQuality> summaryCallTypeQualityFilter,
                Collection<GeneFilter> geneFilters, Collection<ConditionFilter> conditionFilters,
                Collection<DataType> dataTypeFilter,
                Map<EnumSet<CallService.Attribute>, Boolean> callObservedDataFilter)
                        throws IllegalArgumentException {
            super(summaryCallTypeQualityFilter, geneFilters, conditionFilters, dataTypeFilter,
                    SummaryCallType.ExpressionSummary.class, null);

            if (callObservedDataFilter != null && callObservedDataFilter.entrySet().stream()
                    .anyMatch(e -> e.getKey() == null || e.getKey().isEmpty() ||e.getValue() == null ||
                            e.getKey().stream().anyMatch(a -> !a.isConditionParameter()))) {
                throw log.throwing(new IllegalArgumentException("Only condition parameters, non-null, "
                        + "and non-null Booleans are accepted in the Map of callObservedDataFilter"));
            }

            //we will use defensive copying, there is no unmodifiable EnumSet
            this.callObservedDataFilter = callObservedDataFilter == null? new HashMap<>():
                callObservedDataFilter.entrySet().stream()
                .collect(Collectors.toMap(e -> EnumSet.copyOf(e.getKey()), e -> e.getValue()));
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
        public Set<Integer> getSpeciesIdsConsidered() {
            throw log.throwing(new UnsupportedOperationException("Not implemented for this class"));
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + Objects.hash(callObservedDataFilter);
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!super.equals(obj))
                return false;
            if (getClass() != obj.getClass())
                return false;
            ExpressionCallFilter other = (ExpressionCallFilter) obj;
            return Objects.equals(callObservedDataFilter, other.callObservedDataFilter);
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
                   .append(", speciesIdsConsidered=").append(getSpeciesIdsConsidered())
                   .append("]");
            return builder.toString();
        }
    }
    /**
     * A {@code CallFilter} for {@code ExpressionCall}.
     *
     * @author  Frederic Bastian
     * @author  Valentine Rech de Laval
     * @version Bgee 15.0, Nov. 2022
     * @since Bgee 15.0, Nov. 2022
     */
    public static class ExpressionCallFilter2
    extends CallFilter<ExpressionCallData, SummaryCallType.ExpressionSummary, ConditionFilter2> {

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

        //TODO: once we get rid of the "ExpressionCallFilter"(1), probably this method
        //should go back to the CallFilter parent
        //NOTE: actually, we don't even use it now that only one GeneFilter is allowed,
        //and is mandatory in case any other parameters are requested
//        private static Set<Integer> checkAndLoadSpeciesIdsConsidered(
//                Collection<GeneFilter> geneFilters, Collection<ConditionFilter2> conditionFilters) {
//            log.traceEntry("{}, {}", geneFilters, conditionFilters);
//
//            Set<Integer> speciesIdsWithNoParams = new HashSet<>();
//            boolean allSpeciesWithNoParams = false;
//            Set<Integer> speciesIdsWithParams = new HashSet<>();
//            boolean allSpeciesWithParams = false;
//            boolean condAllSpeciesSelected = false;
//            for (ConditionFilter2 f: (conditionFilters == null?
//                    new HashSet<ConditionFilter2>(): conditionFilters)) {
//                Integer speciesId = f.getSpeciesId();
//                if (speciesId == null) {
//                    condAllSpeciesSelected = true;
//                }
//                if (f.areAllCondParamFiltersEmpty()) {
//                    if (speciesId == null) {
//                        allSpeciesWithNoParams = true;
//                    } else {
//                        speciesIdsWithNoParams.add(speciesId);
//                    }
//                } else {
//                    if (speciesId == null) {
//                        allSpeciesWithParams = true;
//                    } else {
//                        speciesIdsWithParams.add(speciesId);
//                    }
//                }
//            }
//            if (!Collections.disjoint(speciesIdsWithNoParams, speciesIdsWithParams) ||
//                    allSpeciesWithNoParams && (!speciesIdsWithParams.isEmpty() || allSpeciesWithParams) ||
//                    !speciesIdsWithNoParams.isEmpty() && allSpeciesWithParams) {
//                throw log.throwing(new IllegalArgumentException(
//                        "A ConditionFilter queries all conditions in a species, "
//                        + "while another ConditionFilter queries some more specific conditions "
//                        + "in that species"));
//            }
//            Set<Integer> condFilterSpeciesIds = new HashSet<>(speciesIdsWithNoParams);
//            condFilterSpeciesIds.addAll(speciesIdsWithParams);
//
//            Set<Integer> geneFilterSpeciesIds = (geneFilters == null?
//                    Stream.<GeneFilter>of(): geneFilters.stream())
//                    .map(gf -> gf.getSpeciesId())
//                    .collect(Collectors.toSet());
//
//            if (!geneFilterSpeciesIds.isEmpty() && !condFilterSpeciesIds.isEmpty() &&
//                    !condAllSpeciesSelected) {
//                Set<Integer> condSpeciesNotFoundInGene = condFilterSpeciesIds.stream()
//                        .filter(id -> !geneFilterSpeciesIds.contains(id))
//                        .collect(Collectors.toSet());
//                if (!condSpeciesNotFoundInGene.isEmpty()) {
//                    throw log.throwing(new IllegalArgumentException(
//                            "Some species IDs were requested in conditionFilters but not in geneFilters: "
//                                    + condSpeciesNotFoundInGene));
//                }
//                Set<Integer> geneSpeciesNotFoundInCond = geneFilterSpeciesIds.stream()
//                        .filter(id -> !condFilterSpeciesIds.contains(id))
//                        .collect(Collectors.toSet());
//                if (!geneSpeciesNotFoundInCond.isEmpty()) {
//                    throw log.throwing(new IllegalArgumentException(
//                            "Some species IDs were requested in geneFilters but not in conditionFilters: "
//                                    + geneSpeciesNotFoundInCond));
//                }
//            }
//
//            Set<Integer> speciesIdsConsidered = new HashSet<>(condFilterSpeciesIds);
//            speciesIdsConsidered.addAll(geneFilterSpeciesIds);
//            if (geneFilterSpeciesIds.isEmpty() && condAllSpeciesSelected) {
//                speciesIdsConsidered = new HashSet<>();
//            }
//
//            return log.traceExit(speciesIdsConsidered);
//        }
//        private static Set<Integer> loadSpeciesIdsWithNoParams(
//                Collection<GeneFilter> geneFilters, Collection<ConditionFilter2> conditionFilters) {
//            log.traceEntry("{}, {}", geneFilters, conditionFilters);
//
//            Set<Integer> geneFilterSpeciesIdsWithNoParams = (geneFilters == null?
//                    Stream.<GeneFilter>of(): geneFilters.stream())
//                    .filter(f -> f.getGeneIds().isEmpty())
//                    .map(gf -> gf.getSpeciesId())
//                    .collect(Collectors.toSet());
//            Set<Integer> condFilterSpeciesIdsWithNoParams = (conditionFilters == null?
//                    Stream.<ConditionFilter2>of(): conditionFilters.stream())
//                    .filter(f -> f.areAllCondParamFiltersEmpty() && f.getSpeciesId() != null)
//                    .map(f -> f.getSpeciesId())
//                    .collect(Collectors.toSet());
//            boolean condAllSpeciesSelected = (conditionFilters == null?
//                    Stream.<ConditionFilter2>of(): conditionFilters.stream())
//                    .anyMatch(f -> f.getSpeciesId() == null);
//
//            Set<Integer> speciesIdsWithNoParams = new HashSet<>(geneFilterSpeciesIdsWithNoParams);
//            speciesIdsWithNoParams.addAll(condFilterSpeciesIdsWithNoParams);
//            if (geneFilterSpeciesIdsWithNoParams.isEmpty() && condAllSpeciesSelected) {
//                speciesIdsWithNoParams = new HashSet<>();
//            }
//
//            return log.traceExit(speciesIdsWithNoParams);
//        }

        private final Set<ConditionParameter<?, ?>> condParamCombination;
        private final Set<ConditionParameter<?, ?>> callObservedDataCondParams;
        private final Boolean callObservedDataFilter;
        private final boolean emptyFilter;

        public ExpressionCallFilter2() {
            this(null, null, null, null, null, null, null);
        }
        /**
         * Either this filter can be totally empty without any parameter,
         * or a {@code GeneFilter} must be provided, otherwise an {@code IllegalArgumentException}
         * is thrown. When a {@code GeneFilter} is provided:
         * <ul>
         * <li>The {@code GeneFilter} must target some genes.
         * <li>all the provided {@code ConditionFilter}s must either target the same species,
         * or no species at all, otherwise an {@code IllegalArgumentException} is thrown.
         * <li> if some {@code ConditionFilter}s are provided without targeting a species,
         * they will be rewritten to target the same species as in the {@code GeneFilter}.
         * </ul>
         *
         * @param summaryCallTypeQualityFilter  A {@code Map} where keys are
         *                                      {@code SummaryCallType.ExpressionSummary}
         *                                      listing the call types requested,
         *                                      associated to the <strong>minimum</strong>
         *                                      {@code SummaryQuality} level
         *                                      requested for this call type.
         *                                      if {@code null} or empty, all
         *                                      {@code SummaryCallType.ExpressionSummary}s
         *                                      will be requested with the first level
         *                                      in {@code SummaryQuality} as minimum quality.
         * @param geneFilter                    A {@code GeneFilter} allowing to specify the genes
         *                                      or species to consider. Can be {@code null} only
         *                                      and only if no other non-default parameters
         *                                      are requested. If non-null it must target some
         *                                      specific genes.
         * @param conditionFilters              A {@code Collection} of {@code ConditionFilter2}s
         *                                      allowing to specify the requested parameters regarding
         *                                      conditions related to the expression calls. If several
         *                                      {@code ConditionFilter2}s are provided, they are seen
         *                                      as "OR" conditions. Can be {@code null} or empty.
         *                                      if contains a {@code null} element, an
         *                                      {@code IllegalArgumentException} is thrown.
         *                                      If a {@code geneFilter} is provided and some
         *                                      of the {@code conditionFilters} explicitly target
         *                                      a different species, an {@code IllegalArgumentException}
         *                                      is thrown. If a {@code geneFilter} is provided,
         *                                      the {@code ConditionFilters} targeting no specific
         *                                      species will be rewritten to target the same species
         *                                      as in the {@code geneFilter}.
         * @param dataTypeFilter                A {@code Collection} of {@code DataType}s
         *                                      allowing to specify the data types to consider
         *                                      to retrieve expression calls. If {@code null}
         *                                      or empty, all data types will be considered.
         * @param condParamCombination          A {@code Collection} of {@code ConditionParameter}s
         *                                      specifying the combination of condition parameters to target.
         *                                      If null or empty, all {@code ConditionParameter}s are used.
         * @param callObservedDataCondParams    A {@code Set} representing a combination of
         *                                      {@code ConditionParameter}s, to specify
         *                                      whether calls retrieved should have been observed or not
         *                                      in the raw data annotations according to that combination.
         *                                      Used in conjunction with the {@code Boolean} argument
         *                                      {@code callObservedDataFilter}. For instance, to retrieve
         *                                      expression calls that have been observed at least
         *                                      in the anat. entity condition parameter, this {@code Set}
         *                                      should contain exactly the value
         *                                      {@code ConditionParameter.ANAT_ENTITY}, and
         *                                      the argument {@code callObservedDataFilter} be {@code true}.
         *                                      If this {@code Set} is non-null and not empty,
         *                                      {@code callObservedDataFilter} must be non-null,
         *                                      otherwise an {@code IllegalArgumentException} is thrown.
         *                                      This {@code Set} is not to be confused with the {@code Set}
         *                                      provided at instantiation of {@code ConditionFilter2}s.
         *                                      In {@code ConditionFilter2}, it is to specify which
         *                                      type of {@code Condition}s will be considered.
         *                                      You could for instance target conditions with propagation
         *                                      computed for parameters {@code ANAT_ENTITY} and
         *                                      {@code DEV_STAGE}, but want to retrieve calls that
         *                                      have been observed only for {@code ANAT_ENTITY} parameter
         *                                      (an example is the use of TopAnat to retrieve
         *                                      observed anat. entits calls, but at a specific
         *                                      developmental stage, for instance "embryo" or "adult",
         *                                      meaning that the targeted conditions considered both
         *                                      anat. entity and dev. stage).
         * @param callObservedDataFilter        A {@code Boolean} used in conjunction with
         *                                      {@code callObservedDataCondParams} (refer to the javadoc
         *                                      of that argument). This argument is considered only when
         *                                      {@code callObservedDataCondParams} is not null nor empty.
         *                                      In that case, this {@code Boolean} must be non-null,
         *                                      otherwise an {@code IllegalArgumentException} is thrown.
         *                                      Must be {@code true} to target calls observed in the selected
         *                                      condition parameters, {@code false} to select calls
         *                                      not observed in the selected condition parameters
         *                                      (propagation only).
         * @throws IllegalArgumentException
         * @throws {@code NullPointerException} If {@code callObservedDataCondParams} is not null
         *                                      and contains a null value.
         */
        public ExpressionCallFilter2(Map<ExpressionSummary, SummaryQuality> summaryCallTypeQualityFilter,
                GeneFilter geneFilter, Collection<ConditionFilter2> conditionFilters,
                Collection<DataType> dataTypeFilter,
                Collection<ConditionParameter<?, ?>> condParamCombination,
                Collection<ConditionParameter<?, ?>> callObservedDataCondParams,
                Boolean callObservedDataFilter)
                throws IllegalArgumentException {
            super(summaryCallTypeQualityFilter, geneFilter == null? Set.of(): Set.of(geneFilter),
                    //For conditionFilters, if a geneFilter is provided, we recreate the condition filters
                    //so that null species IDs are replaced with the same species as in the gene filter
                    geneFilter == null || conditionFilters == null? conditionFilters:
                        conditionFilters.stream()
                        .filter(f -> !f.areAllFiltersExceptSpeciesEmpty())
                        .map(f -> f.getSpeciesId() != null? f: new ConditionFilter2(
                                geneFilter.getSpeciesId(), f.getCondParamToComposedFilterIds(),
                                f.getCondParamCombination(), f.getObservedCondForParams(),
                                f.isExcludeNonInformative()))
                        .collect(Collectors.toSet()),
                    dataTypeFilter, SummaryCallType.ExpressionSummary.class,
                    geneFilter == null? Set.of(): Set.of(geneFilter.getSpeciesId()));

            //ConditionParameter.copyOf also does a sanitary check for presence of null elements
            this.condParamCombination = condParamCombination == null || condParamCombination.isEmpty()?
                    ConditionParameter.allOf(): ConditionParameter.copyOf(condParamCombination);
            this.callObservedDataCondParams = callObservedDataCondParams == null?
                    ConditionParameter.noneOf(): ConditionParameter.copyOf(callObservedDataCondParams);
            this.callObservedDataFilter = callObservedDataFilter;

            if (!this.callObservedDataCondParams.isEmpty() && this.callObservedDataFilter == null) {
                throw log.throwing(new IllegalArgumentException(
                        "The boolean callObservedDataFilter must be non-null"));
            }
            if (this.callObservedDataCondParams.stream()
                    .anyMatch(param -> !this.condParamCombination.contains(param))) {
                throw log.throwing(new IllegalArgumentException("A condition parameter was targeted "
                        + "for observation status but was not part of the requested combination."));
            }
            Set<Set<ConditionParameter<?, ?>>> allCombParamCombs = this.getConditionFilters()
                    .stream().map(f -> f.getCondParamCombination())
                    .collect(Collectors.toSet());
            if (allCombParamCombs.size() > 1) {
                throw log.throwing(new IllegalArgumentException(
                        "The ConditionFilters do not all target the same condition parameter combination."));
            }
            if (!allCombParamCombs.isEmpty() &&
                    !this.condParamCombination.equals(allCombParamCombs.iterator().next())) {
                throw log.throwing(new IllegalArgumentException("Inconsistent condition parameter "
                        + "combination requested in ExpressionCallFilter and ConditionFilters."));
            }

            this.emptyFilter =
                    this.getDataTypeFilters().equals(EnumSet.allOf(DataType.class)) &&
                    this.getSummaryCallTypeQualityFilter().equals(ALL_CALLS) &&
                    this.getCondParamCombination().containsAll(ConditionParameter.allOf()) &&
                    this.getCallObservedDataCondParams().isEmpty() &&
                    this.getGeneFilters().isEmpty() &&
                    this.getConditionFilters().isEmpty();
            if (!this.emptyFilter && (this.getGeneFilters().isEmpty() ||
                    this.getGeneFilter().getGeneIds().isEmpty())) {
                throw log.throwing(new IllegalArgumentException(
                        "A GeneFilter must be provided and targets some genes if any other parameters are requested"));
            }
            if (this.getGeneFilter() != null) {
                Integer speciesId = this.getGeneFilter().getSpeciesId();
                if (this.getConditionFilters().stream()
                        .anyMatch(f -> f.getSpeciesId() == null || !f.getSpeciesId().equals(speciesId))) {
                    throw log.throwing(new IllegalArgumentException(
                            "Not possible to specify different species in GeneFiter and ConditionFilters"));
                }
            }
        }

        public Set<ConditionParameter<?, ?>> getCondParamCombination() {
            return condParamCombination;
        }
        public Set<ConditionParameter<?, ?>> getCallObservedDataCondParams() {
            return callObservedDataCondParams;
        }
        public Boolean getCallObservedDataFilter() {
            return callObservedDataFilter;
        }
        /**
         * Note that no more than 1 {@code GeneFilter} can be present in the returned {@code Set}.
         * This signature is conserved for compatibility with other classes,
         * but see {@link #getGeneFilter()}.
         */
        //Override to redefine javadoc
        @Override
        public Set<GeneFilter> getGeneFilters() {
            return super.getGeneFilters();
        }
        /**
         * @return  Either the {@code GeneFilter} configuring this {@code ExpressionCallFilter2},
         *          or {@code null} if none was provided.
         */
        public GeneFilter getGeneFilter() {
            return super.getGeneFilters().stream().findAny().orElse(null);
        }

        public boolean isEmptyFilter() {
            return emptyFilter;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + Objects.hash(condParamCombination,
                    callObservedDataCondParams, callObservedDataFilter);
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!super.equals(obj))
                return false;
            if (getClass() != obj.getClass())
                return false;
            ExpressionCallFilter2 other = (ExpressionCallFilter2) obj;
            return Objects.equals(condParamCombination, other.condParamCombination)
                    && Objects.equals(callObservedDataCondParams, other.callObservedDataCondParams)
                    && Objects.equals(callObservedDataFilter, other.callObservedDataFilter);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ExpressionCallFilter2 [")
                   .append("dataTypeFilters=").append(getDataTypeFilters())
                   .append(", summaryCallTypeQualityFilter=").append(getSummaryCallTypeQualityFilter())
                   .append(", geneFilters=").append(getGeneFilters())
                   .append(", conditionFilters=").append(getConditionFilters())
                   .append(", speciesIdsConsidered=").append(getSpeciesIdsConsidered())
                   .append(", condParamCombination=").append(condParamCombination)
                   .append(", callObservedDataCondParams=").append(callObservedDataCondParams)
                   .append(", callObservedDataFilter=").append(callObservedDataFilter)
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
    extends CallFilter<ExpressionCallData, SummaryCallType.DiffExpressionSummary, ConditionFilter> {
        /**
         * See {@link CallFilter#CallFilter(GeneFilter, Collection, Collection, SummaryQuality, SummaryCallType)}.
         */
        public DiffExpressionCallFilter(
                Map<SummaryCallType.DiffExpressionSummary, SummaryQuality> summaryCallTypeQualityFilter,
                Collection<GeneFilter> geneFilters, Collection<ConditionFilter> conditionFilters,
            Collection<DataType> dataTypeFilter) throws IllegalArgumentException {
            super(summaryCallTypeQualityFilter, geneFilters, conditionFilters, dataTypeFilter,
                    SummaryCallType.DiffExpressionSummary.class, null);
        }

        @Override
        public Set<Integer> getSpeciesIdsConsidered() {
            throw log.throwing(new UnsupportedOperationException("Not implemented for this class"));
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
    //TODO: once we get rid of the "ExpressionCallFilter"(1), probably this constructor
    //should not accept speciesIdsConsidered and speciesIdsWithNoParams,
    //but directly generating them by moving the methods
    //ExpressionCallFilter2#checkAndLoadSpeciesIdsConsidered and
    //ExpressionCallFilter2#loadSpeciesIdsWithNoParams
    protected CallFilter(Map<U, SummaryQuality> summaryCallTypeQualityFilter,
            Collection<GeneFilter> geneFilters, Collection<V> conditionFilters,
            Collection<DataType> dataTypeFilter, Class<U> callTypeCls,
            Collection<Integer> speciesIdsConsidered) throws IllegalArgumentException {
        super(geneFilters, conditionFilters, speciesIdsConsidered);

        if (dataTypeFilter != null && dataTypeFilter.stream().anyMatch(e -> e == null)) {
            throw log.throwing(new IllegalStateException("No DataTypeFilter can be null."));
        }
        if (summaryCallTypeQualityFilter != null &&
                summaryCallTypeQualityFilter.keySet().stream().anyMatch(e -> e == null)) {
            throw log.throwing(new IllegalStateException("No SummaryCallType can be null."));
        }
        if (summaryCallTypeQualityFilter != null &&
                summaryCallTypeQualityFilter.values().stream().anyMatch(e -> e == null)) {
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
        
        if (this.dataTypeFilters.stream().anyMatch(e -> e == null)) {
            throw log.throwing(new IllegalStateException("No DataTypeFilter can be null."));
        }
        if (this.summaryCallTypeQualityFilter.keySet().stream().anyMatch(e -> e == null)) {
            throw log.throwing(new IllegalStateException("No SummaryCallType can be null."));
        }
        if (this.summaryCallTypeQualityFilter.values().stream().anyMatch(e -> e == null)) {
            throw log.throwing(new IllegalStateException("No SummaryQuality can be null."));
        }

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
        result = prime * result + Objects.hash(dataTypeFilters, summaryCallTypeQualityFilter);
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        CallFilter<?, ?, ?> other = (CallFilter<?, ?, ?>) obj;
        return Objects.equals(dataTypeFilters, other.dataTypeFilters)
                && Objects.equals(summaryCallTypeQualityFilter, other.summaryCallTypeQualityFilter);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CallFilter [summaryCallTypeQualityFilter=").append(summaryCallTypeQualityFilter)
               .append(", dataTypeFilters=").append(dataTypeFilters)
               .append(", geneFilters=").append(getGeneFilters())
               .append(", conditionFilters=").append(getConditionFilters())
               .append(", speciesIdsConsidered=").append(getSpeciesIdsConsidered())
               .append("]");
        return builder.toString();
    }
}
