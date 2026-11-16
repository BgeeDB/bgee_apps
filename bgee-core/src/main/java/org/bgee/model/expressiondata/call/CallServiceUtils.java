package org.bgee.model.expressiondata.call;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.CommonService;
import org.bgee.model.ComposedEntity;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.DevStageService;
import org.bgee.model.anatdev.Sex;
import org.bgee.model.anatdev.SexService;
import org.bgee.model.anatdev.Strain;
import org.bgee.model.anatdev.StrainService;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.call.CallObservedDataDAOFilter2;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO.ConditionTO;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO.ConditionTOResultSet;
import org.bgee.model.dao.api.expressiondata.call.DAOConditionFilter2;
import org.bgee.model.dao.api.expressiondata.call.DAOFDRPValueFilter2;
import org.bgee.model.dao.api.expressiondata.call.DAOPropagationState;
import org.bgee.model.expressiondata.BaseConditionFilter2.FilterIds;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.expressiondata.call.CallFilter.ExpressionCallFilter2;
import org.bgee.model.ontology.OntologyService;
import org.bgee.model.species.Species;

public class CallServiceUtils {
    private final static Logger log = LogManager.getLogger(CallServiceUtils.class.getName());

    /**
     * {@code DAOConditionFilter2}s converted from {@code ConditionFilter2}s, together with
     * the values of condition parameters these filters exclude with their descendants,
     * by species ID. The conditions holding an excluded value are still retrieved through
     * the {@code DAOConditionFilter2}s: their observations are propagated to their ancestors,
     * so that excluding a sub-condition does not change the calls of its ancestors, and only
     * their own calls are discarded, after the propagation (see
     * {@link CallServiceUtils#loadExcludedConditionIds(ConvertedConditionFilters, Map,
     * OntologyService)}).
     */
    public static class ConvertedConditionFilters {
        private final Set<DAOConditionFilter2> daoConditionFilters;
        private final Map<Integer, List<ExcludedTerms>> excludedAnatEntities;
        private final Map<Integer, List<ExcludedTerms>> excludedCellTypes;
        private final Map<Integer, List<ExcludedTerms>> excludedDevStages;

        ConvertedConditionFilters(Set<DAOConditionFilter2> daoConditionFilters,
                Map<Integer, List<ExcludedTerms>> excludedAnatEntities,
                Map<Integer, List<ExcludedTerms>> excludedCellTypes,
                Map<Integer, List<ExcludedTerms>> excludedDevStages) {
            this.daoConditionFilters = Collections.unmodifiableSet(new HashSet<>(daoConditionFilters));
            //HashMaps, as a filter with no species ID stores its excluded values under a null key
            this.excludedAnatEntities = excludedAnatEntities == null? new HashMap<>():
                new HashMap<>(excludedAnatEntities);
            this.excludedCellTypes = excludedCellTypes == null? new HashMap<>():
                new HashMap<>(excludedCellTypes);
            this.excludedDevStages = excludedDevStages == null? new HashMap<>():
                new HashMap<>(excludedDevStages);
        }

        public Set<DAOConditionFilter2> getDAOConditionFilters() {
            return daoConditionFilters;
        }
    }
    /**
     * Values of a condition parameter excluded with their descendants by a condition filter.
     * Their descendants are retrieved only among the values of the conditions retrieved,
     * see {@link CallServiceUtils#loadExcludedConditionIds(ConvertedConditionFilters, Map,
     * OntologyService)}.
     */
    static class ExcludedTerms {
        private final Set<String> ids;
        private final Set<String> notToExcludeIds;

        /**
         * @param ids               A {@code Set} of {@code String}s that are the IDs of the values
         *                          excluded with their descendants.
         * @param notToExcludeIds   A {@code Set} of {@code String}s that are the IDs of the values
         *                          not to exclude, even if they are descendants of {@code ids}.
         */
        ExcludedTerms(Set<String> ids, Set<String> notToExcludeIds) {
            this.ids = Collections.unmodifiableSet(new HashSet<>(ids));
            this.notToExcludeIds = Collections.unmodifiableSet(new HashSet<>(notToExcludeIds));
        }

        public Set<String> getIds() {
            return ids;
        }
        public Set<String> getNotToExcludeIds() {
            return notToExcludeIds;
        }

        @Override
        public String toString() {
            return "ExcludedTerms [ids=" + ids + ", notToExcludeIds=" + notToExcludeIds + "]";
        }
    }


    public EnumSet<DAODataType> convertDataTypeToDAODataType(Collection<DataType> dts)
            throws IllegalStateException{
        log.traceEntry("{}", dts);
        
        if (dts == null || dts.isEmpty()) {
            return log.traceExit(EnumSet.allOf(DAODataType.class));
        }
        return log.traceExit(
                //We create an EnumSet not to iterate over potentially redundant elements
                EnumSet.copyOf(dts).stream()
                //XXX: we shouldn't use a static method here, every dependency should be injected.
                .map(dt -> CommonService.convertDataTypeToDAODataType(dt))
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(DAODataType.class))));
    }

    public EnumSet<ConditionDAO.ConditionParameter> convertCondParamsToDAOCondParams(
            Collection<ConditionParameter<?, ?>> condParams) {
        log.traceEntry("{}", condParams);
        if (condParams == null) {
            return log.traceExit((EnumSet<ConditionDAO.ConditionParameter>) null);
        }
        if (condParams.isEmpty()) {
            return log.traceExit(EnumSet.noneOf(ConditionDAO.ConditionParameter.class));
        }
        if (condParams.stream().anyMatch(e -> e == null)) {
            throw log.throwing(new UnsupportedOperationException(
                    "No Condition parameter can be null"));
        }

        return log.traceExit(condParams.stream()
                .flatMap(param -> {
                    if (ConditionParameter.ANAT_ENTITY_CELL_TYPE.equals(param)) {
                        return Stream.of(ConditionDAO.ConditionParameter.ANAT_ENTITY,
                                ConditionDAO.ConditionParameter.CELL_TYPE);
                    } else if (ConditionParameter.DEV_STAGE.equals(param)) {
                        return Stream.of(ConditionDAO.ConditionParameter.STAGE);
                    } else if (ConditionParameter.SEX.equals(param)) {
                        return Stream.of(ConditionDAO.ConditionParameter.SEX);
                    } else if (ConditionParameter.STRAIN.equals(param)) {
                        return Stream.of(ConditionDAO.ConditionParameter.STRAIN);
                    }
                    throw log.throwing(new UnsupportedOperationException(
                            "Condition parameter not supported: " + param));
                })
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(
                        ConditionDAO.ConditionParameter.class))));
    }
    public EnumSet<ConditionDAO.Attribute> convertCondParamsToDAOCondAttributes(
            Collection<ConditionParameter<?, ?>> condParams) {
        log.traceEntry("{}", condParams);
        if (condParams == null) {
            return log.traceExit((EnumSet<ConditionDAO.Attribute>) null);
        }
        if (condParams.isEmpty()) {
            return log.traceExit(EnumSet.noneOf(ConditionDAO.Attribute.class));
        }
        if (condParams.stream().anyMatch(e -> e == null)) {
            throw log.throwing(new UnsupportedOperationException(
                    "No Condition parameter can be null"));
        }

        return log.traceExit(condParams.stream()
                .flatMap(param -> {
                    if (ConditionParameter.ANAT_ENTITY_CELL_TYPE.equals(param)) {
                        return Stream.of(ConditionDAO.Attribute.ANAT_ENTITY_ID,
                                ConditionDAO.Attribute.CELL_TYPE_ID);
                    } else if (ConditionParameter.DEV_STAGE.equals(param)) {
                        return Stream.of(ConditionDAO.Attribute.STAGE_ID);
                    } else if (ConditionParameter.SEX.equals(param)) {
                        return Stream.of(ConditionDAO.Attribute.SEX_ID);
                    } else if (ConditionParameter.STRAIN.equals(param)) {
                        return Stream.of(ConditionDAO.Attribute.STRAIN_ID);
                    }
                    throw log.throwing(new UnsupportedOperationException(
                            "Condition parameter not supported: " + param));
                })
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(
                        ConditionDAO.Attribute.class))));
    }

    public Set<Set<DAOFDRPValueFilter2>> generateExprQualDAOPValFilters(
            ExpressionCallFilter2 callFilter, BigDecimal presentLowThreshold,
            BigDecimal presentHighThreshold, BigDecimal absentLowThreshold,
            BigDecimal absentHighThreshold) {
        log.traceEntry("{}, {}, {}, {}, {}", callFilter, presentLowThreshold,
                presentHighThreshold, absentLowThreshold, absentHighThreshold);

        if (callFilter == null ||
                (callFilter.getSummaryCallTypeQualityFilter().equals(ExpressionCallFilter2.ALL_CALLS) &&
                //We still need the PValueFilters if only some data types are requested:
                //it is how we filter for data types supporting a call
                (callFilter.getDataTypeFilters().isEmpty() ||
                        callFilter.getDataTypeFilters().equals(EnumSet.allOf(DataType.class)))) &&
                //Same if only specific condition parameter combination is requested
                (callFilter.getCondParamCombination().isEmpty() ||
                        callFilter.getCondParamCombination().containsAll(ConditionParameter.allOf()))) {
            return new HashSet<>();
        }

        EnumSet<DAODataType> daoDataTypes = this.convertDataTypeToDAODataType(
                callFilter == null? null: callFilter.getDataTypeFilters());
        Set<ConditionParameter<?, ?>> condParams = callFilter == null? ConditionParameter.allOf():
            callFilter.getCondParamCombination();
        EnumSet<ConditionDAO.ConditionParameter> daoCondParams =
                this.convertCondParamsToDAOCondParams(condParams);

        return log.traceExit(callFilter.getSummaryCallTypeQualityFilter()
        .entrySet().stream()
        .flatMap(e -> {
            SummaryCallType.ExpressionSummary callType = e.getKey();
            SummaryQuality qual = e.getValue();
            //DAOFDRPValueFilters in the inner sets are seen as "AND" conditions,
            //the Sets in the outer Set are seen as "OR" conditions.
            Set<Set<DAOFDRPValueFilter2>> pValFilters = new HashSet<>();

            if (callType.equals(SummaryCallType.ExpressionSummary.EXPRESSED)) {
                if (qual.equals(SummaryQuality.GOLD)) {
                    //If minimum GOLD is requested, we only want calls with FDR-corrected p-value <= 0.01
                    pValFilters.add(Collections.singleton(
                            new DAOFDRPValueFilter2(presentHighThreshold,
                                    daoDataTypes,
                                    DAOFDRPValueFilter2.Qualifier.LESS_THAN_OR_EQUALS_TO,
                                    DAOPropagationState.SELF_AND_DESCENDANT,
                                    false, false, daoCondParams)));
                } else {
                    //If minimum SILVER is requested, we want calls with FDR-corrected p-value <= 0.05,
                    //we'll get calls SILVER or GOLD
                    pValFilters.add(Collections.singleton(
                            new DAOFDRPValueFilter2(presentLowThreshold,
                                    daoDataTypes,
                                    DAOFDRPValueFilter2.Qualifier.LESS_THAN_OR_EQUALS_TO,
                                    DAOPropagationState.SELF_AND_DESCENDANT,
                                    false, false, daoCondParams)));
                    //Then, if minimum BRONZE is requested, we also accept calls that are SILVER or GOLD
                    //in a descendant condition. We end up with the following conditions:
                    // * FDR-corrected p-value in condition including sub-conditions <= 0.05
                    //   (SILVER or GOLD)
                    // * OR FDR-corrected p-value in at least one sub-condition <= 0.05 (BRONZE)
                    //
                    // We don't accept absence of data in sub-conditions because of the OR,
                    // this would lead to return any data:
                    // FDR-corrected p-value in condition including sub-conditions <= 0.05
                    // OR (FDR-corrected p-value in at least one sub-condition <= 0.05 OR NO DATA)
                    if (qual.equals(SummaryQuality.BRONZE)) {
                        pValFilters.add(Collections.singleton(
                                new DAOFDRPValueFilter2(presentLowThreshold,
                                        daoDataTypes,
                                        DAOFDRPValueFilter2.Qualifier.LESS_THAN_OR_EQUALS_TO,
                                        DAOPropagationState.DESCENDANT,
                                        false, false, daoCondParams)));
                    }
                }

            } else if (callType.equals(SummaryCallType.ExpressionSummary.NOT_EXPRESSED)) {
                //For NOT_EXPRESSED, we request that the p-value of the call is non-significant,
                //But also that it is still non-significant when removing data types
                //that we don't trust to produce absent calls (except for BRONZE absent calls).
                //Requirement both for the p-value coming from the condition and its sub-conditions,
                //and the best p-value among the sub-conditions.
                EnumSet<DAODataType> daoDataTypesTrustedForNotExpressed =
                        convertTrustedAbsentDataTypesToDAODataTypes(callFilter.getDataTypeFilters());
                Set<DAOFDRPValueFilter2> absentAndFilters = new HashSet<>();
                //If we request SILVER or GOLD, and there is no data type requested
                //that we trust for generating ABSENT calls, we make an impossible condition
                //so that it returns no result
                //FIXME: the use of this boolean selfObservationRequired in DAOFDRPValueFilter
                //is maybe problematic, probably we should allow to target a specific combination
                //of condition parameters to assess whether there are observed data? I'm not sure,
                //to think about. Or actually, do we still really want this filtering that we must
                //have observed data? (yes, maybe)
                if (daoDataTypesTrustedForNotExpressed.isEmpty() && !qual.equals(SummaryQuality.BRONZE)) {
                    absentAndFilters.add(new DAOFDRPValueFilter2(new BigDecimal("1"),
                                        daoDataTypes,
                                        DAOFDRPValueFilter2.Qualifier.GREATER_THAN,
                                        DAOPropagationState.SELF_AND_DESCENDANT,
                                        true, false, daoCondParams));
                } else {
                    if (qual.equals(SummaryQuality.GOLD)) {
                        absentAndFilters.add(new DAOFDRPValueFilter2(absentHighThreshold,
                                daoDataTypes,
                                DAOFDRPValueFilter2.Qualifier.GREATER_THAN,
                                DAOPropagationState.SELF_AND_DESCENDANT,
                                true, false, daoCondParams));
                        //we want the same condition without considering
                        //the data types that we don't trust to produce absent calls
                        absentAndFilters.add(new DAOFDRPValueFilter2(absentHighThreshold,
                                daoDataTypesTrustedForNotExpressed,
                                DAOFDRPValueFilter2.Qualifier.GREATER_THAN,
                                DAOPropagationState.SELF_AND_DESCENDANT,
                                true, false, daoCondParams));
                    } else {
                        absentAndFilters.add(new DAOFDRPValueFilter2(absentLowThreshold,
                                daoDataTypes,
                                DAOFDRPValueFilter2.Qualifier.GREATER_THAN,
                                DAOPropagationState.SELF_AND_DESCENDANT,
                                true, false, daoCondParams));
                        //Unless we request BRONZE quality, we want the same condition without considering
                        //the data types that we don't trust to produce absent calls
                        if (qual.equals(SummaryQuality.SILVER)) {
                            absentAndFilters.add(new DAOFDRPValueFilter2(absentLowThreshold,
                                    daoDataTypesTrustedForNotExpressed,
                                    DAOFDRPValueFilter2.Qualifier.GREATER_THAN,
                                    DAOPropagationState.SELF_AND_DESCENDANT,
                                    true, false, daoCondParams));
                        }
                    }
                    //in all cases, we don't want PRESENT calls in a sub-condition
                    absentAndFilters.add(new DAOFDRPValueFilter2(presentLowThreshold,
                            daoDataTypes,
                            DAOFDRPValueFilter2.Qualifier.GREATER_THAN,
                            DAOPropagationState.DESCENDANT,
                            false, true, daoCondParams));
                    //And unless we request BRONZE, we want the same to hold true
                    //with only the data types we trust to produce ABSENT calls
                    if (!qual.equals(SummaryQuality.BRONZE)) {
                        absentAndFilters.add(new DAOFDRPValueFilter2(presentLowThreshold,
                                daoDataTypesTrustedForNotExpressed,
                                DAOFDRPValueFilter2.Qualifier.GREATER_THAN,
                                DAOPropagationState.DESCENDANT,
                                false, true, daoCondParams));
                    }
                }
                pValFilters.add(absentAndFilters);
            }
            return pValFilters.stream();
        }).collect(Collectors.toSet()));
    }

    public EnumSet<DAODataType> convertTrustedAbsentDataTypesToDAODataTypes(
            Collection<DataType> dts) throws IllegalStateException {
        log.traceEntry("{}", dts);

        //Find DataTypes that can be trusted for absent calls. Maybe there will be none among
        //the requested data types. So we need to convert to DAODataTypes in two steps,
        //by checking if dataTypesToConsider is empty, because the method
        //convertDataTypeToDAODataType returns all DAODataTypes when the provided argument
        //of DataTypes is empty or null.
        Set<DataType> dataTypesToConsider =
                (dts == null || dts.isEmpty()? EnumSet.allOf(DataType.class): EnumSet.copyOf(dts))
                .stream()
                .filter(dt -> dt.isTrustedForAbsentCalls())
                .collect(Collectors.toSet());
        return log.traceExit(dataTypesToConsider.isEmpty()? EnumSet.noneOf(DAODataType.class):
            this.convertDataTypeToDAODataType(dataTypesToConsider));
    }

    /**
     * Exclude-seed IDs that should actually be applied for {@code filterIds}.
     * <p>
     * A seed is applied only when it lies in the descendant closure of an include ID
     * that is not itself an exclude seed. That punches out other SUMMARY organs under
     * the expanded term (central nervous system and brain under multicellular organism)
     * and leaves ancestor buckets alone (multicellular organism is not a descendant of
     * central nervous system, so expanding CNS does not discard the organism tree).
     * Checking the other direction — whether the include ID is a descendant of the
     * seed — treats a parent as a child and drops the seeds that should be applied.
     *
     * @param filterIds      A {@code FilterIds} whose exclude seeds are considered.
     *                       Cannot be {@code null}.
     * @param descendantIds  A {@code Function} returning descendant IDs of a term.
     *                       A {@code null} return for a given ID is treated as no descendants.
     *                       If {@code descendantIds} itself is {@code null}, all exclude
     *                       seeds are returned unchanged.
     * @return               A new {@code Set} of exclude seeds to apply. Never {@code null}.
     */
    public static Set<String> selectApplicableExcludeSeeds(FilterIds<String> filterIds,
            Function<String, Set<String>> descendantIds) {
        log.traceEntry("{}, {}", filterIds, descendantIds);
        Set<String> excludeSeeds = new HashSet<>(filterIds.getExcludeTermsAndChildrenIds());
        if (excludeSeeds.isEmpty() || descendantIds == null) {
            return log.traceExit(excludeSeeds);
        }
        Set<String> protectedIncludeIds = new HashSet<>(filterIds.getIds());
        protectedIncludeIds.removeAll(excludeSeeds);
        if (protectedIncludeIds.isEmpty()) {
            return log.traceExit(excludeSeeds);
        }
        Set<String> underProtectedIncludes = new HashSet<>();
        for (String includeId : protectedIncludeIds) {
            underProtectedIncludes.addAll(descendantIdsOrEmpty(descendantIds, includeId));
        }
        excludeSeeds.retainAll(underProtectedIncludes);
        return log.traceExit(excludeSeeds);
    }

    /**
     * Expands applicable exclude seeds of {@code filterIds} to those IDs plus their
     * descendants, then removes {@link FilterIds#getNotToExcludeIds()}.
     * Only exclude seeds that are descendants of a protected include term are applied; see
     * {@link #selectApplicableExcludeSeeds(FilterIds, Function)}.
     *
     * @param filterIds      A {@code FilterIds} to expand. If {@code null} or with no
     *                       exclude seeds, an empty {@code Set} is returned.
     * @param descendantIds  A {@code Function} returning descendant IDs of a term,
     *                       or {@code null} to exclude only the seed IDs themselves
     *                       (no descendant expansion and no ancestor skip).
     * @return               A new {@code Set} of IDs to subtract from the include set.
     */
    public static Set<String> expandApplicableExcludeIds(FilterIds<String> filterIds,
            Function<String, Set<String>> descendantIds) {
        log.traceEntry("{}, {}", filterIds, descendantIds);
        if (filterIds == null || filterIds.getExcludeTermsAndChildrenIds().isEmpty()) {
            return log.traceExit(Set.of());
        }
        Set<String> seeds = descendantIds == null
                ? new HashSet<>(filterIds.getExcludeTermsAndChildrenIds())
                : selectApplicableExcludeSeeds(filterIds, descendantIds);
        Set<String> idsToExclude = new HashSet<>(seeds);
        if (descendantIds != null) {
            for (String seed : seeds) {
                idsToExclude.addAll(descendantIdsOrEmpty(descendantIds, seed));
            }
        }
        idsToExclude.removeAll(filterIds.getNotToExcludeIds());
        return log.traceExit(idsToExclude);
    }

    private static Set<String> descendantIdsOrEmpty(Function<String, Set<String>> descendantIds,
            String id) {
        if (descendantIds == null || id == null) {
            return Set.of();
        }
        Set<String> descendants = descendantIds.apply(id);
        return descendants == null ? Set.of() : descendants;
    }

    /**
     * Sexes and strains have no ontology: all their values are right below their root.
     * As for the other condition parameters, the conditions of the descendants of a requested
     * value must be retrieved, for their observations to be propagated to it: requesting the root
     * requires to retrieve the conditions of all values. The calls of the values not requested
     * are discarded after the propagation, see {@link OTFExpressionCallFilterEngine}.
     *
     * @param ids       A {@code Set} of {@code String}s that are the requested IDs of sexes
     *                  or of strains. Can be {@code null}.
     * @param rootId    A {@code String} that is the ID of the root of that condition parameter.
     *                  It is compared ignoring case, as the database compares the requested IDs.
     * @return          {@code null}, meaning that the conditions of all values are retrieved,
     *                  if {@code ids} contains {@code rootId}, {@code ids} otherwise.
     */
    private static Set<String> expandFlatHierarchyRoot(Set<String> ids, String rootId) {
        log.traceEntry("{}, {}", ids, rootId);
        if (ids != null && ids.stream().anyMatch(rootId::equalsIgnoreCase)) {
            return log.traceExit((Set<String>) null);
        }
        return log.traceExit(ids);
    }

    /**
     * @param condParamCombination  A {@code Collection} of {@code ConditionParameter}s that is
     *                              the condition parameter combination requested. The condition
     *                              parameters it does not contain are restricted to their root,
     *                              as the conditions of the other combinations hold no data for
     *                              this query. When no {@code ConditionFilter2} is provided, it
     *                              is the only information restricting the conditions retrieved,
     *                              besides the species.
     */
    public Set<DAOConditionFilter2> convertConditionFiltersToDAOConditionFilters(
            Collection<ConditionFilter2> condFilters, OntologyService ontService,
            AnatEntityService anatEntityService, Set<Integer> consideredSpeciesIds,
            Collection<ConditionParameter<?, ?>> condParamCombination) {
        log.traceEntry("{}, {}, {}, {}, {}", condFilters, ontService, anatEntityService,
                consideredSpeciesIds, condParamCombination);
        return log.traceExit(this.convertConditionFilters(condFilters, ontService,
                anatEntityService, consideredSpeciesIds, condParamCombination)
                .getDAOConditionFilters());
    }
    /**
     * Same as {@link #convertConditionFiltersToDAOConditionFilters(Collection, OntologyService,
     * AnatEntityService, Set, Collection)}, also returning the values of condition parameters
     * excluded with their descendants by {@code condFilters}: the {@code DAOConditionFilter2}s
     * still retrieve the conditions holding these values, see {@link ConvertedConditionFilters}.
     */
    public ConvertedConditionFilters convertConditionFilters(
            Collection<ConditionFilter2> condFilters, OntologyService ontService,
            AnatEntityService anatEntityService, Set<Integer> consideredSpeciesIds,
            Collection<ConditionParameter<?, ?>> condParamCombination) {
        log.traceEntry("{}, {}, {}, {}, {}", condFilters, ontService, anatEntityService,
                consideredSpeciesIds, condParamCombination);
        if (condFilters == null || condFilters.isEmpty()) {
            if(consideredSpeciesIds == null || consideredSpeciesIds.isEmpty()) {
                return log.traceExit(new ConvertedConditionFilters(new HashSet<>(),
                        null, null, null));
            }
            //Restrict to the requested condition parameter combination, exactly as it is done
            //below when ConditionFilter2s are provided.
            Set<ConditionParameter<?, ?>> condParamComb =
                    condParamCombination == null || condParamCombination.isEmpty()?
                    ConditionParameter.allOf(): new HashSet<>(condParamCombination);
            return log.traceExit(new ConvertedConditionFilters(Set.of(new DAOConditionFilter2(
                    consideredSpeciesIds,
                    condParamComb.contains(ConditionParameter.ANAT_ENTITY_CELL_TYPE)? null:
                        Collections.singleton(ConditionDAO.ANAT_ENTITY_ROOT_ID),
                    condParamComb.contains(ConditionParameter.DEV_STAGE)? null:
                        Collections.singleton(ConditionDAO.DEV_STAGE_ROOT_ID),
                    condParamComb.contains(ConditionParameter.ANAT_ENTITY_CELL_TYPE)? null:
                        Collections.singleton(ConditionDAO.CELL_TYPE_ROOT_ID),
                    condParamComb.contains(ConditionParameter.SEX)? null:
                        Collections.singleton(ConditionDAO.SEX_ROOT_ID),
                    condParamComb.contains(ConditionParameter.STRAIN)? null:
                        Collections.singleton(ConditionDAO.STRAIN_ROOT_ID),
                    null, null)), null, null, null));
        }
    
        long t0 = System.currentTimeMillis();
        Map<Integer, Set<String>> nonInformativePerSpeciesId = condFilters.stream()
                .filter(f -> f.isExcludeNonInformative())
                .map(f -> f.getSpeciesId()).distinct()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> anatEntityService.loadNonInformativeAnatEntitiesBySpeciesIds(Set.of(id), true)
                              .map(ae -> ae.getId())
                              //we don't store the anatomy root and cell type root, otherwise we would exclude
                              //all results that are only in an anat. entity or only in a cell type
                              //(in that case, the roots are used for the other cond. params)
                              .filter(aeid -> !aeid.equals(ConditionDAO.ANAT_ENTITY_ROOT_ID) &&
                                      !aeid.equals(ConditionDAO.CELL_TYPE_ROOT_ID))
                              .collect(Collectors.toSet())));
        log.debug("loadNonInformativeAnatEntities() completed in {} ms ({} species with exclusion)",
                System.currentTimeMillis() - t0, nonInformativePerSpeciesId.size());

        //Now we have everything we need to create the DAO filters
        t0 = System.currentTimeMillis();
        Set<DAOConditionFilter2> daoCondFilters = new HashSet<>();
        //The values excluded with their descendants, by species ID. The conditions holding them
        //are still retrieved through the DAO filters, for their observations to be propagated
        //to their ancestors: only their own calls are discarded after the propagation.
        Map<Integer, List<ExcludedTerms>> excludedAnatEntities = new HashMap<>();
        Map<Integer, List<ExcludedTerms>> excludedCellTypes = new HashMap<>();
        Map<Integer, List<ExcludedTerms>> excludedDevStages = new HashMap<>();
        for (ConditionFilter2 filter: condFilters) {
            Set<ConditionParameter<?, ?>> condParamComb = filter.getCondParamCombination();
            //consideredSpeciesIds might itself be null, but it could have
            //the species IDs requested in GeneFilters, to query conditions
            //only in those species
            Set<Integer> speciesIds = filter.getSpeciesId() == null? consideredSpeciesIds:
                Collections.singleton(filter.getSpeciesId());

            //For the time being, we still use the DAOConditionFilter using different arguments
            //for anat. entity and cell type. For now we will consider that the first FilterIds
            //in the new ComposedFilterIds are the anatEntityIds, and the second FilterIds the cellTypeIds.
            FilterIds<String> anatEntityFilterIds = filter.getComposedFilterIds(
                    ConditionParameter.ANAT_ENTITY_CELL_TYPE).getFilterIds(0);
            FilterIds<String> cellTypeFilterIds = filter.getComposedFilterIds(
                    ConditionParameter.ANAT_ENTITY_CELL_TYPE).getFilterIds(1);
            //For now we consider there is no composition for dev. stages
            assert !filter.getComposedFilterIds(ConditionParameter.DEV_STAGE).isComposed();
            FilterIds<String> devStageFilterIds = filter.getComposedFilterIds(
                    ConditionParameter.DEV_STAGE).getFilterIds(0);

            BiFunction<Set<String>, Set<String>, Map<String, Set<String>>> anatEntityDescendantIds =
                    (ids, descendantIdFilter) -> ontService.getAnatEntityDescendantIds(
                            speciesIds, ids, descendantIdFilter);
            ExcludedTerms excludedAnatEntityTerms = getExcludedTerms(anatEntityFilterIds,
                    anatEntityDescendantIds, "anat. entity");
            ExcludedTerms excludedCellTypeTerms = getExcludedTerms(cellTypeFilterIds,
                    anatEntityDescendantIds, "cell type");
            BiFunction<Set<String>, Set<String>, Map<String, Set<String>>> devStageDescendantIds =
                    (ids, descendantIdFilter) -> ontService.getDevStageDescendantIds(
                            speciesIds, ids, descendantIdFilter);
            ExcludedTerms excludedDevStageTerms = getExcludedTerms(devStageFilterIds,
                    devStageDescendantIds, "dev. stage");
            if (condParamComb.contains(ConditionParameter.ANAT_ENTITY_CELL_TYPE)) {
                addExcludedTerms(excludedAnatEntities, filter.getSpeciesId(), excludedAnatEntityTerms);
                addExcludedTerms(excludedCellTypes, filter.getSpeciesId(), excludedCellTypeTerms);
            }
            if (condParamComb.contains(ConditionParameter.DEV_STAGE)) {
                addExcludedTerms(excludedDevStages, filter.getSpeciesId(), excludedDevStageTerms);
            }

            //For now we consider there is no composition for sexes and strains
            assert !filter.getComposedFilterIds(ConditionParameter.SEX).isComposed();
            assert !filter.getComposedFilterIds(ConditionParameter.STRAIN).isComposed();
            //The calls are generated on the fly: the conditions of the descendants
            //of a requested term must be retrieved, whether or not its child terms
            //are requested, for their observations to be propagated to it. The conditions
            //that were not requested are discarded after the propagation. The DAO retrieves
            //the descendants of the anat. entities and cell types. Those of the dev. stages,
            //few, are listed here: a list of stages lets the DAO use the index of
            //the conditions. The roots used for the condition parameters not in
            //the combination are retrieved without their descendants.
            DAOConditionFilter2 daoCondFilter = new DAOConditionFilter2(
                    speciesIds,
                    !condParamComb.contains(ConditionParameter.ANAT_ENTITY_CELL_TYPE)?
                            Collections.singleton(ConditionDAO.ANAT_ENTITY_ROOT_ID):
                                anatEntityFilterIds == null? null: anatEntityFilterIds.getIds(),
                    !condParamComb.contains(ConditionParameter.DEV_STAGE)?
                            Collections.singleton(ConditionDAO.DEV_STAGE_ROOT_ID):
                                getIdsWithDescendants(devStageFilterIds, devStageDescendantIds),
                    !condParamComb.contains(ConditionParameter.ANAT_ENTITY_CELL_TYPE)?
                            Collections.singleton(ConditionDAO.CELL_TYPE_ROOT_ID):
                                cellTypeFilterIds == null? null: cellTypeFilterIds.getIds(),
                    !condParamComb.contains(ConditionParameter.SEX)?
                            Collections.singleton(ConditionDAO.SEX_ROOT_ID):
                                expandFlatHierarchyRoot(
                                        filter.getComposedFilterIds(ConditionParameter.SEX).getIds(0),
                                        ConditionDAO.SEX_ROOT_ID),
                    !condParamComb.contains(ConditionParameter.STRAIN)?
                            Collections.singleton(ConditionDAO.STRAIN_ROOT_ID):
                                expandFlatHierarchyRoot(
                                        filter.getComposedFilterIds(ConditionParameter.STRAIN).getIds(0),
                                        ConditionDAO.STRAIN_ROOT_ID),

                    convertCondParamsToDAOCondParams(filter.getObservedCondForParams()),

                    filter.isExcludeNonInformative()?
                            nonInformativePerSpeciesId.get(filter.getSpeciesId()): null,

                    condParamComb.contains(ConditionParameter.ANAT_ENTITY_CELL_TYPE));
            log.debug("ConditionFilter: {} - condParamCombination: {} - Generated DAOConditionFilter: {}",
                    filter, condParamComb, daoCondFilter);
            daoCondFilters.add(daoCondFilter);
        }
        log.debug("DAOConditionFilter2 construction loop completed in {} ms ({} filters built)",
                System.currentTimeMillis() - t0, daoCondFilters.size());
    
        //Now we filter the daoCondFilters: if one of them target a species with no additional parameters,
        //then we discard any other filter targeting the same species
        Map<Set<Integer>, List<DAOConditionFilter2>> filtersPerSpecies = daoCondFilters.stream()
                .collect(Collectors.groupingBy(f -> f.getSpeciesIds()));
        return log.traceExit(new ConvertedConditionFilters(
            filtersPerSpecies.values().stream().flatMap(l -> {
                DAOConditionFilter2 noFilter = l.stream()
                            .filter(f -> f.areAllFiltersExceptSpeciesEmpty())
                            .findAny().orElse(null);
                    if (noFilter != null) {
                        return Stream.of(noFilter);
                    }
                    return l.stream();
            }).collect(Collectors.toSet()),
            excludedAnatEntities, excludedCellTypes, excludedDevStages));
    }

    /**
     * @param filterIds     A {@code FilterIds} of the terms requested for a condition parameter.
     *                      Can be {@code null}.
     * @param descendantIds A {@code BiFunction} returning, for the terms it is provided with,
     *                      the {@code Map} of their descendants, among the terms it is
     *                      provided with secondly, or all of them if this second argument is
     *                      {@code null} or empty.
     * @param termType      A {@code String} that is the name of the type of the terms,
     *                      for the exception message.
     * @return              The {@code ExcludedTerms} of {@code filterIds}: its exclude seeds
     *                      applicable, see {@link #selectApplicableExcludeSeeds(FilterIds,
     *                      Function)}. {@code null} if {@code filterIds} excludes no term.
     * @throws IllegalArgumentException If the exclusions leave no requested term
     *                                  nor any of their descendants.
     */
    private static ExcludedTerms getExcludedTerms(FilterIds<String> filterIds,
            BiFunction<Set<String>, Set<String>, Map<String, Set<String>>> descendantIds,
            String termType) throws IllegalArgumentException {
        log.traceEntry("{}, {}, {}", filterIds, descendantIds, termType);
        if (filterIds == null || filterIds.getExcludeTermsAndChildrenIds().isEmpty()) {
            return log.traceExit((ExcludedTerms) null);
        }
        Set<String> protectedIncludeIds = new HashSet<>(filterIds.getIds());
        protectedIncludeIds.removeAll(filterIds.getExcludeTermsAndChildrenIds());
        //Selecting the applicable exclude seeds only requires the descendants
        //of the protected includes that are exclude seeds
        Map<String, Set<String>> includeDescendants = protectedIncludeIds.isEmpty()? Map.of():
            descendantIds.apply(protectedIncludeIds, filterIds.getExcludeTermsAndChildrenIds());
        Set<String> excludeSeeds = selectApplicableExcludeSeeds(filterIds,
                id -> includeDescendants.getOrDefault(id, Set.of()));

        //A requested term that is not an exclude seed is never excluded: the applicable seeds
        //and their descendants are below it. So nothing can remain only when all the requested
        //terms are excluded, but for the terms not to exclude that are requested terms
        //or their descendants.
        if (protectedIncludeIds.isEmpty()) {
            Set<String> notToExcludeIds = filterIds.getNotToExcludeIds();
            boolean remaining = notToExcludeIds.stream().anyMatch(filterIds.getIds()::contains) ||
                    !notToExcludeIds.isEmpty() &&
                    descendantIds.apply(filterIds.getIds(), notToExcludeIds).values().stream()
                    .anyMatch(ids -> !ids.isEmpty());
            if (!remaining) {
                throw log.throwing(new IllegalArgumentException(
                        "No result should be retrieved because of " + termType + " exclusion"));
            }
        }
        return log.traceExit(new ExcludedTerms(excludeSeeds, filterIds.getNotToExcludeIds()));
    }
    /**
     * @param filterIds     A {@code FilterIds} of the terms requested for a condition parameter.
     *                      Can be {@code null}.
     * @param descendantIds A {@code BiFunction} returning, for the terms it is provided with,
     *                      the {@code Map} of all their descendants when its second argument
     *                      is {@code null}.
     * @return              A {@code Set} of {@code String}s that are the IDs of the terms
     *                      of {@code filterIds} and of all their descendants, {@code null}
     *                      if {@code filterIds} requests no term.
     */
    private static Set<String> getIdsWithDescendants(FilterIds<String> filterIds,
            BiFunction<Set<String>, Set<String>, Map<String, Set<String>>> descendantIds) {
        log.traceEntry("{}, {}", filterIds, descendantIds);
        if (filterIds == null || filterIds.getIds().isEmpty()) {
            return log.traceExit((Set<String>) null);
        }
        Set<String> ids = new HashSet<>(filterIds.getIds());
        descendantIds.apply(filterIds.getIds(), null).values().forEach(ids::addAll);
        return log.traceExit(ids);
    }
    private static void addExcludedTerms(Map<Integer, List<ExcludedTerms>> excludedTermsBySpeciesId,
            Integer speciesId, ExcludedTerms excludedTerms) {
        log.traceEntry("{}, {}, {}", excludedTermsBySpeciesId, speciesId, excludedTerms);
        if (excludedTerms != null) {
            excludedTermsBySpeciesId.computeIfAbsent(speciesId, k -> new ArrayList<>())
                    .add(excludedTerms);
        }
        log.traceExit();
    }

    /**
     * Identifies the conditions holding a value excluded with its descendants by the condition
     * filters converted to {@code convertedFilters}: these conditions are retrieved
     * for their observations to be propagated to their ancestors, but their own calls must be
     * discarded after the propagation. The descendants of the excluded values are retrieved
     * only among the values of {@code conditions}.
     *
     * @param convertedFilters  The {@code ConvertedConditionFilters} the conditions were
     *                          retrieved with.
     * @param conditions        A {@code Map} where keys are {@code Integer}s that are the IDs
     *                          of the conditions retrieved, the associated value being
     *                          the {@code Condition2}.
     * @param ontService        The {@code OntologyService} to retrieve the descendants
     *                          of the excluded values.
     * @return                  A {@code Set} of {@code Integer}s that are the IDs of the conditions
     *                          of {@code conditions} holding an excluded value.
     */
    public Set<Integer> loadExcludedConditionIds(ConvertedConditionFilters convertedFilters,
            Map<Integer, Condition2> conditions, OntologyService ontService) {
        log.traceEntry("{}, {}, {}", convertedFilters, conditions, ontService);
        if (convertedFilters.excludedAnatEntities.isEmpty() &&
                convertedFilters.excludedCellTypes.isEmpty() &&
                convertedFilters.excludedDevStages.isEmpty()) {
            return log.traceExit(new HashSet<>());
        }
        Set<Integer> excludedCondIds = new HashSet<>();
        //The descendants of the excluded values must be valid in the species of the conditions
        Map<Integer, Map<Integer, Condition2>> conditionsBySpeciesId = conditions.entrySet().stream()
                .collect(Collectors.groupingBy(e -> e.getValue().getSpeciesId(),
                        Collectors.toMap(e -> e.getKey(), e -> e.getValue())));
        for (Entry<Integer, Map<Integer, Condition2>> speciesEntry: conditionsBySpeciesId.entrySet()) {
            Set<Integer> speciesIds = Set.of(speciesEntry.getKey());
            Collection<Condition2> speciesConditions = speciesEntry.getValue().values();
            Set<String> excludedAnatEntityIds = loadExcludedIds(convertedFilters.excludedAnatEntities,
                    speciesEntry.getKey(),
                    speciesConditions.stream().map(c -> c.getAnatEntity())
                    .filter(ae -> ae != null).map(ae -> ae.getId()).collect(Collectors.toSet()),
                    (ids, descendantIdFilter) -> ontService.getAnatEntityDescendantIds(
                            speciesIds, ids, descendantIdFilter));
            Set<String> excludedCellTypeIds = loadExcludedIds(convertedFilters.excludedCellTypes,
                    speciesEntry.getKey(),
                    speciesConditions.stream().map(c -> c.getCellType())
                    .filter(ct -> ct != null).map(ct -> ct.getId()).collect(Collectors.toSet()),
                    (ids, descendantIdFilter) -> ontService.getAnatEntityDescendantIds(
                            speciesIds, ids, descendantIdFilter));
            Set<String> excludedDevStageIds = loadExcludedIds(convertedFilters.excludedDevStages,
                    speciesEntry.getKey(),
                    speciesConditions.stream()
                    .map(c -> c.getConditionParameterId(ConditionParameter.DEV_STAGE))
                    .filter(id -> id != null).collect(Collectors.toSet()),
                    (ids, descendantIdFilter) -> ontService.getDevStageDescendantIds(
                            speciesIds, ids, descendantIdFilter));
            speciesEntry.getValue().entrySet().stream()
            .filter(e -> {
                AnatEntity anatEntity = e.getValue().getAnatEntity();
                AnatEntity cellType = e.getValue().getCellType();
                String devStageId = e.getValue().getConditionParameterId(ConditionParameter.DEV_STAGE);
                return anatEntity != null && excludedAnatEntityIds.contains(anatEntity.getId()) ||
                        cellType != null && excludedCellTypeIds.contains(cellType.getId()) ||
                        devStageId != null && excludedDevStageIds.contains(devStageId);
            })
            .forEach(e -> excludedCondIds.add(e.getKey()));
        }
        return log.traceExit(excludedCondIds);
    }
    /**
     * @param excludedTermsBySpeciesId  A {@code Map} of the {@code ExcludedTerms} by species ID,
     *                                  those of the filters with no species being associated
     *                                  to the {@code null} key.
     * @param speciesId                 An {@code Integer} that is the ID of the species
     *                                  of the conditions.
     * @param termIds                   A {@code Set} of {@code String}s that are the IDs
     *                                  of the values of the conditions.
     * @param descendantIds             A {@code BiFunction} returning, for the terms
     *                                  it is provided with, the {@code Map} of their descendants
     *                                  among the terms it is provided with secondly.
     * @return                          A {@code Set} of {@code String}s that are the IDs
     *                                  of {@code termIds} excluded.
     */
    private static Set<String> loadExcludedIds(
            Map<Integer, List<ExcludedTerms>> excludedTermsBySpeciesId, Integer speciesId,
            Set<String> termIds,
            BiFunction<Set<String>, Set<String>, Map<String, Set<String>>> descendantIds) {
        log.traceEntry("{}, {}, {}, {}", excludedTermsBySpeciesId, speciesId, termIds,
                descendantIds);
        Set<String> excludedIds = new HashSet<>();
        //Without terms to search them among, all the descendants would be retrieved
        if (termIds.isEmpty()) {
            return log.traceExit(excludedIds);
        }
        List<ExcludedTerms> excludedTermsList = new ArrayList<>(
                excludedTermsBySpeciesId.getOrDefault(speciesId, List.of()));
        excludedTermsList.addAll(excludedTermsBySpeciesId.getOrDefault(null, List.of()));
        for (ExcludedTerms excludedTerms: excludedTermsList) {
            if (excludedTerms.getIds().isEmpty()) {
                continue;
            }
            Set<String> ids = new HashSet<>(excludedTerms.getIds());
            descendantIds.apply(excludedTerms.getIds(), termIds).values().forEach(ids::addAll);
            ids.removeAll(excludedTerms.getNotToExcludeIds());
            ids.retainAll(termIds);
            excludedIds.addAll(ids);
        }
        return log.traceExit(excludedIds);
    }

    public CallObservedDataDAOFilter2 convertCallObservedDataToDAO(ExpressionCallFilter2 filter) {
        log.traceEntry("{}", filter);
        if (filter == null || filter.getCallObservedDataCondParams().isEmpty()) {
            return log.traceExit((CallObservedDataDAOFilter2) null);
        }
        return log.traceExit(new CallObservedDataDAOFilter2(
                this.convertDataTypeToDAODataType(filter.getDataTypeFilters()),
                this.convertCondParamsToDAOCondParams(filter.getCallObservedDataCondParams()),
                filter.getCallObservedDataFilter()));
    }

    public Map<Integer, Condition2> loadGlobalConditionMap(Collection<Species> species,
            Collection<DAOConditionFilter2> conditionFilters,
            Collection<ConditionDAO.Attribute> conditionDAOAttrs, ConditionDAO conditionDAO,
            AnatEntityService anatEntityService, DevStageService devStageService,
            SexService sexService, StrainService strainService) {
        log.traceEntry("{}, {}, {}, {}, {}, {}, {}, {}", species, conditionFilters, conditionDAOAttrs, 
                conditionDAO, anatEntityService, devStageService, sexService, strainService);

        return log.traceExit(loadConditionMapFromResultSet(
                (attrs) -> conditionDAO.getGlobalConditions(conditionFilters, attrs),
                conditionDAOAttrs, species, anatEntityService, devStageService, sexService,
                strainService));
    }
    
    public Map<Integer, Condition2> loadConditionMapFromResultSet(
            Function<Collection<ConditionDAO.Attribute>, ConditionTOResultSet> rsFunc,
            Collection<ConditionDAO.Attribute> conditionDAOAttrs, Collection<Species> species,
            AnatEntityService anatEntityService, DevStageService devStageService,
            SexService sexService, StrainService strainService) {
        log.traceEntry("{}, {}, {}, {}, {}, {}, {}", rsFunc, conditionDAOAttrs, species, 
                anatEntityService, devStageService, sexService, strainService);

        if (species == null || species.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Some species must be provided"));
        }

        Map<Integer, Species> speMap = species.stream()
                .collect(Collectors.toMap(s -> s.getId(), s -> s, (s1, s2) -> s1));
        Set<Integer> usedSpeciesIds = new HashSet<>();
        Set<String> anatEntityIds = new HashSet<>();
        Set<String> stageIds = new HashSet<>();
        Set<String> cellTypeIds = new HashSet<>();
        Set<String> sexIds = new HashSet<>();
        Set<String> strainIds = new HashSet<>();
        Set<ConditionTO> conditionTOs = new HashSet<>();

        //we need to retrieve the attributes requested, plus the condition ID and species ID in all cases.
        Set<ConditionDAO.Attribute> clonedAttrs = conditionDAOAttrs == null || conditionDAOAttrs.isEmpty()?
                EnumSet.allOf(ConditionDAO.Attribute.class): EnumSet.copyOf(conditionDAOAttrs);
        clonedAttrs.addAll(EnumSet.of(ConditionDAO.Attribute.ID, ConditionDAO.Attribute.SPECIES_ID));
        ConditionTOResultSet rs = rsFunc.apply(clonedAttrs);

        while (rs.next()) {
            ConditionTO condTO = rs.getTO();
            if (!speMap.keySet().contains(condTO.getSpeciesId())) {
                throw log.throwing(new IllegalArgumentException(
                        "The retrieved ConditionTOs do not match the provided Species."));
            }
            conditionTOs.add(condTO);

            usedSpeciesIds.add(condTO.getSpeciesId());

            if (condTO.getAnatEntityId() != null) {
                anatEntityIds.add(condTO.getAnatEntityId());
            }
            if (condTO.getStageId() != null) {
                stageIds.add(condTO.getStageId());
            }
            if (condTO.getCellTypeId() != null) {
                cellTypeIds.add(condTO.getCellTypeId());
            }
            if (condTO.getSex() != null) {
                sexIds.add(condTO.getSex().getStringRepresentation());
            }
            if (condTO.getStrainId() != null) {
                strainIds.add(condTO.getStrainId());
            }
        }
        
        //merge anat entities and cell types to call only once loadAnatEntities
        Set<String> anatAndCellIds = new HashSet<String>(anatEntityIds);
        anatAndCellIds.addAll(cellTypeIds);

        final Map<String, AnatEntity> anatAndCellMap = anatAndCellIds.isEmpty()? new HashMap<>():
            anatEntityService.loadAnatEntities(
                    usedSpeciesIds, true, anatAndCellIds, false)
            .collect(Collectors.toMap(a -> a.getId(), a -> a));
        if (!anatAndCellIds.isEmpty() && anatAndCellMap.size() != anatAndCellIds.size()) {
            anatAndCellIds.removeAll(anatAndCellMap.keySet());
            throw log.throwing(new IllegalStateException("Some anat. entities or cell type used in a condition "
                    + "are not supposed to exist in the related species. Species: " + usedSpeciesIds
                    + " - anat. entities: " + anatAndCellIds));
        }
        final Map<String, DevStage> stageMap = stageIds.isEmpty()? new HashMap<>():
            devStageService.loadDevStages(
                    usedSpeciesIds, true, stageIds, false)
            .collect(Collectors.toMap(s -> s.getId(), s -> s));
        if (!stageIds.isEmpty() && stageMap.size() != stageIds.size()) {
            stageIds.removeAll(stageMap.keySet());
            throw log.throwing(new IllegalStateException("Some stages used in a condition "
                    + "are not supposed to exist in the related species. Species: " + usedSpeciesIds
                    + " - stages: " + stageIds));
        }
        final Map<String, Sex> sexMap = sexIds.isEmpty()? new HashMap<>():
            //if a sex is not supported, an exception will be immediately thrown
            sexService.loadSexes(sexIds)
            .collect(Collectors.toMap(s -> s.getId(), s -> s));
        
        final Map<String, Strain> strainMap = strainIds.isEmpty()? new HashMap<>():
            strainService.loadStrains(strainIds)
            .collect(Collectors.toMap(s -> s.getId(), s -> s));
        //In case we retrieve strains from the database
        if (!strainIds.isEmpty() && strainMap.size() != strainIds.size()) {
            strainIds.removeAll(strainMap.keySet());
            throw log.throwing(new IllegalStateException("Some strains used in a condition "
                    + "are not supposed to exist in the related species. Species: " + usedSpeciesIds
                    + " - strains: " + strainIds));
        }
        
        return log.traceExit(conditionTOs.stream()
                .collect(Collectors.toMap(
                        cTO -> cTO.getId(), 
                        cTO -> {
                            Map<ConditionParameter<?, ?>, ComposedEntity<?>> condParamEntities =
                                    new HashMap<>();

                            AnatEntity anatEntity = cTO.getAnatEntityId() == null? null:
                                    Optional.ofNullable(anatAndCellMap.get(cTO.getAnatEntityId()))
                                    .orElseThrow(() -> new IllegalStateException(
                                            "Anat. entity not found: " + cTO.getAnatEntityId()));
                            AnatEntity cellType = cTO.getCellTypeId() == null? null:
                                Optional.ofNullable(anatAndCellMap.get(cTO.getCellTypeId()))
                                .orElseThrow(() -> new IllegalStateException(
                                        "Anat. entity not found: " + cTO.getCellTypeId()));
                            LinkedHashSet<AnatEntity> anatEntitiesCellTypes = new LinkedHashSet<>();
                            if (cellType != null) {
                                anatEntitiesCellTypes.add(cellType);
                            }
                            if (anatEntity != null) {
                                anatEntitiesCellTypes.add(anatEntity);
                            }
                            if (!anatEntitiesCellTypes.isEmpty()) {
                                condParamEntities.put(ConditionParameter.ANAT_ENTITY_CELL_TYPE,
                                        new ComposedEntity<>(anatEntitiesCellTypes, AnatEntity.class));
                            }

                            DevStage stage = cTO.getStageId() == null? null:
                                Optional.ofNullable(stageMap.get(cTO.getStageId()))
                                .orElseThrow(() -> new IllegalStateException("Stage not found: "
                                                + cTO.getStageId()));
                            if (stage != null) {
                                condParamEntities.put(ConditionParameter.DEV_STAGE,
                                        new ComposedEntity<>(stage, DevStage.class));
                            }

                            Sex sex = cTO.getSex() == null? null:
                                Optional.ofNullable(sexMap.get(cTO.getSex().getStringRepresentation()))
                                .orElseThrow(() -> new IllegalStateException("sex not found: "
                                            + cTO.getSex().getStringRepresentation()));
                            if (sex != null) {
                                condParamEntities.put(ConditionParameter.SEX,
                                        new ComposedEntity<>(sex, Sex.class));
                            }

                            Strain strain = cTO.getStrainId() == null? null:
                                Optional.ofNullable(strainMap.get(cTO.getStrainId()))
                                .orElseThrow(() -> new IllegalStateException("strain not found: "
                                            + cTO.getStrainId()));
                            if (strain != null) {
                                condParamEntities.put(ConditionParameter.STRAIN,
                                        new ComposedEntity<>(strain, Strain.class));
                            }

                            return new Condition2(condParamEntities,
                                    Optional.ofNullable(speMap.get(cTO.getSpeciesId()))
                                    .orElseThrow(() -> new IllegalStateException(
                                            "Species not found: " + cTO.getSpeciesId())));
                        })));
    }
}
