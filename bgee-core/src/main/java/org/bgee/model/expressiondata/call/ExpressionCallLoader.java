package org.bgee.model.expressiondata.call;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.CommonService;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.DevStageService;
import org.bgee.model.anatdev.Sex;
import org.bgee.model.anatdev.SexService;
import org.bgee.model.anatdev.Strain;
import org.bgee.model.anatdev.StrainService;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.DAOObservedExpressionFilter;
import org.bgee.model.dao.api.expressiondata.ObservedExpressionDAO;
import org.bgee.model.dao.api.expressiondata.ObservedExpressionDAO.ObservedExpressionTO;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO.ConditionTOResultSet;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO.RawConditionToSelfGlobalConditionTO;
import org.bgee.model.dao.api.expressiondata.call.GlobalExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.call.GlobalExpressionCallDAO.GlobalExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.call.GlobalExpressionCallDAO.GlobalExpressionCallTOResultSet;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.PropagationState;
import org.bgee.model.expressiondata.call.Call.ExpressionCall2;
import org.bgee.model.expressiondata.call.CallFilter.ExpressionCallFilter2;
import org.bgee.model.expressiondata.call.ConditionGraphCacheService.ConditionGraphCache;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.species.Species;

public class ExpressionCallLoader extends CommonService {
    private final static Logger log = LogManager.getLogger(ExpressionCallLoader.class.getName());

    /**
     * An {@code int} that is the maximum allowed number of results
     * to retrieve in one method call.
     * Value: 10,000.
     */
    public static int LIMIT_MAX = 10000;
    public final static BigDecimal EXPRESSION_SCORE_MAX_VALUE = new BigDecimal("100");
    private final static BigDecimal ZERO_BIGDECIMAL = new BigDecimal("0");
    private final static BigDecimal ABOVE_ZERO_BIGDECIMAL = new BigDecimal("0.000000000000000000000000000001");
    private final static BigDecimal MIN_FDR_BIGDECIMAL = new BigDecimal("0.00000000000001");
    /**
     * An {@code int} that is the maximum number of elements
     * in {@link #conditionMap} and {@link #geneMap} before starting
     * to flushing some existing entries. It is not a <strong>guarantee</strong>
     * that those {@code Map}s will never exceed that size, just a trigger
     * to flushing entries as much as possible.
     *
     * @see #updateConditionMap(Set)
     * @see #updateGeneMap(Set)
     */
    private static final int MAX_ELEMENTS_IN_MAP = 10000;



    private final GlobalExpressionCallDAO globalExprCallDAO;
    private final GeneDAO geneDAO;
    private final ConditionDAO condDAO;
    private final AnatEntityService anatEntityService;
    private final DevStageService devStageService;
    private final SexService sexService;
    private final StrainService strainService;
    private final CallServiceUtils utils;
    private final CallMapping callMapping;
    /**
     * @see #getProcessedFilter()
     */
    private final ExpressionCallProcessedFilter processedFilter;

    //These attributes are mutable, it is acceptable for a Service.
    //We keep the speciesMap and geneBiotypeMap inside the rawDataProcessedFilter,
    //as there will be no update to them by this RawDataLoader.
    /**
     * A {@code Map} where keys are {@code Integer}s that are internal IDs of raw data conditions,
     * the value being the associated {@code Condition2}. this {@code Map} is used
     * to store the retrieved {@code Condition2}s over several independent calls
     * to this {@code ExpressionCallLoader}, in order to avoid querying multiple times for the same
     * conditions.
     *
     * @see #MAX_ELEMENTS_IN_MAP
     * @see #updateRawDataConditionMap(Set)
     */
    private final Map<Integer, Condition2> conditionMap;
    /**
     * A {@code Map} where keys are {@code Integer}s that are internal IDs of genes,
     * the value being the associated {@code Gene}. this {@code Map} is used
     * to store the retrieved {@code Gene}s over several independent calls
     * to this {@code ExpressionCallLoader}, in order to avoid querying multiple times for the same
     * genes.
     *
     * @see #MAX_ELEMENTS_IN_MAP
     * @see #updateGeneMap(Set)
     */
    private final Map<Integer, Gene> geneMap;

    ExpressionCallLoader(ExpressionCallProcessedFilter processedFilter, ServiceFactory serviceFactory) {
        this(processedFilter, serviceFactory, new CallServiceUtils(),
                new CallMapping(processedFilter));
    }
    //Constructor package protected so that only the RawDataService can instantiate this class
    ExpressionCallLoader(ExpressionCallProcessedFilter processedFilter,
            ServiceFactory serviceFactory, CallServiceUtils utils, CallMapping callMapping) {
        super(serviceFactory);

        if (processedFilter == null) {
            //we need it at least to retrieve, species, gene biotypes, and sources
            throw log.throwing(new IllegalArgumentException(
                    "A processedFilter must be provided"));
        }
        if (utils == null) {
            throw log.throwing(new IllegalArgumentException(
                    "A CallServiceUtils must be provided"));
        }
        if (callMapping == null) {
            throw log.throwing(new IllegalArgumentException(
                    "A CallMapping must be provided"));
        }
        this.utils = utils;
        this.callMapping = callMapping;
        this.globalExprCallDAO = this.getDaoManager().getGlobalExpressionCallDAO();
        this.geneDAO = this.getDaoManager().getGeneDAO();
        this.condDAO = this.getDaoManager().getConditionDAO();
        this.anatEntityService = this.getServiceFactory().getAnatEntityService();
        this.devStageService = this.getServiceFactory().getDevStageService();
        this.sexService = this.getServiceFactory().getSexService();
        this.strainService = this.getServiceFactory().getStrainService();
        this.processedFilter = processedFilter;
        this.conditionMap = new HashMap<>();
        this.geneMap = new HashMap<>();
        //Seed the Maps with any condition or gene already identified
        //from the processed filter.
        //We keep the speciesMap and geneBiotypeMap inside the processedFilter,
        //as there will be no update to them by this Loader.
        this.conditionMap.putAll(this.processedFilter.getRequestedConditionMap());
        this.geneMap.putAll(this.processedFilter.getRequestedGeneMap());
    }

    //If we want to let users decide which of the anat. entity, dev. stage, etc, to retrieve
    //in the Conditions of the ExpressionCall, we should let them set Attributes.
    //Currently, the condition parameters to return are determined by the combination
    //selected in the source ExpressionCallFilter. It's a bit weird that is the a filter
    //that determine the attributes visualized in return.
    //But if there were attributes, we would still need to provide the condition parameters
    //to the filter, because it is important to configure the query.
    //So, maybe that should be its own argument of the method, rather than being in both the filter
    //and the attributes?
    //TODO But then it should be provided at the level of ExpressionCallService.loadCallLoader!
    //(because this is where the ExpressionCallFilter is provided, some of the Conditions retrieved, etc)
    //One of the Attribute could be "CONDITION", rather than the detail of the condition parameters.
    //And then there would be another argument, the condition parameters, that would affect both
    //the filtering in the query and the fields retrieved in the returned Conditions.
    //
    //offset is a Long because sometimes the number of potential results can be very large.
    public List<ExpressionCall2> loadData(Long offset, Integer limit) {
        log.traceEntry("{}, {}", offset, limit);

        //If the DAOCallFilters are null (different from: not-null and empty)
        //it means there was no matching conds and thus no result for sure
        if (this.processedFilter.getDaoFilters() == null) {
            return log.traceExit(new ArrayList<>());
        }

        if (offset != null && offset < 0) {
            throw log.throwing(new IllegalArgumentException("offset cannot be less than 0"));
        }
        if (limit != null && limit <= 0) {
            throw log.throwing(new IllegalArgumentException(
                    "limit cannot be less than or equal to 0"));
        }
        if (limit != null && limit > LIMIT_MAX) {
            throw log.throwing(new IllegalArgumentException("limit cannot be greater than "
                    + LIMIT_MAX));
        }
        long newOffset = offset == null? 0L: offset;
        int newLimit = limit == null? LIMIT_MAX: limit;

        //We obtain the results from the data source
        ExpressionCallFilter2 callFilter = this.processedFilter.getSourceFilter();
        EnumSet<CallService.Attribute> attrs = this.getAttributes(callFilter);
        GlobalExpressionCallTOResultSet rs = this.globalExprCallDAO
                .getGlobalExpressionCalls2(
                        this.processedFilter.getDaoFilters(),
                        convertServiceAttrToGlobalExprDAOAttr(attrs, callFilter),
                        //for now we always order by bgeeGeneId, conditionId
                        convertServiceOrderingAttrToGlobalExprDAOOrderingAttr(callFilter),
                        newOffset,
                        newLimit);

        //We iterate a first time the calls to retrieve the bgeeGeneIds and the condIds,
        //and we store them along the way
        Set<Integer> bgeeGeneIds = new HashSet<>();
        Set<Integer> condIds = new HashSet<>();
        List<GlobalExpressionCallTO> callTOs = new ArrayList<>();
        while (rs.next()) {
            GlobalExpressionCallTO callTO = rs.getTO();
            if (callTO.getBgeeGeneId() != null) {
                bgeeGeneIds.add(callTO.getBgeeGeneId());
            }
            if (callTO.getConditionId() != null) {
                condIds.add(callTO.getConditionId());
            }
            callTOs.add(callTO);
        }
        //Now we update the geneMap and condMap
        this.updateConditionMap(condIds);
        this.updateGeneMap(bgeeGeneIds);

        //Now we generate the final result
        return log.traceExit(callTOs.stream()
                .map(cTO -> this.callMapping.mapGlobalCallTOToExpressionCall(cTO,
                        this.geneMap, this.conditionMap, callFilter,
                        this.processedFilter.getMaxRankPerSpecies(), attrs))
                .collect(Collectors.toList()));
    }

    //right now 
    public Map<Gene, List<OTFExpressionCall>> loadDataOnTheFly() {
      //If the DAOCallFilters are null (different from: not-null and empty)
        //it means there was no matching conds and thus no result for sure
        if (this.processedFilter.getDaoFilters() == null) {
            return log.traceExit(new HashMap<>());
        }

        EnumSet<ConditionDAO.ConditionParameter> daoCondParams =
                this.utils.convertCondParamsToDAOCondParams(
                        this.processedFilter.getSourceFilter().getCondParamCombination());

        EnumSet<DataType> queriedDataTypes = this.processedFilter.getSourceFilter().getDataTypeFilters();
        EnumSet<DAODataType> queriedDaoDataTypes = queriedDataTypes
                .stream()
                .map(dt -> convertDataTypeToDAODataType(dt)).collect(() -> 
                        EnumSet.noneOf(DAODataType.class),
                        EnumSet::add,
                        EnumSet::addAll);

        //FIXME: at this point we assume a single species per request (validated by processExprCallPage)
        int speciesId = this.processedFilter.getGeneSpeciesPart()
                .getSpeciesMap().keySet().iterator().next();
        long startTimeCondGraph = System.currentTimeMillis();
        ConditionGraphCache condGraphCache = new ConditionGraphCacheService(this.getServiceFactory())
                .getOrLoadGraph(speciesId);
        log.debug("Condition graph retrieved for species {} in {} ms",
                speciesId, System.currentTimeMillis() - startTimeCondGraph);

        //2. retrieve rawconditionIds from the globalCond and the condition parameters.
        //   If conditionMap is empty (no condition filter provided), use all global conditions
        //   from the graph so that observed expressions are not missed.
        //   Snapshot filter-matching condition IDs before any ancestor expansion so that
        //   propagateCalls() can stop propagating upward at the filter boundary.
        final Set<Integer> filterConditionIds = conditionMap.isEmpty()?
                Collections.emptySet(): new HashSet<>(conditionMap.keySet());
        Set<Integer> globalCondIdsToQuery = conditionMap.isEmpty()?
                condGraphCache.getGlobalCondToDirectAncestors().keySet():
                conditionMap.keySet();
        long startTimeRawConds = System.currentTimeMillis();
        List<RawConditionToSelfGlobalConditionTO> rawCondToSeflGlobalCondTOs = this.condDAO
        .getRawConditionToSelfGlobalConditionFromGlobalConditionIds(globalCondIdsToQuery,
                daoCondParams).getAllTOs();
        Map<Integer, Integer> rawCondIdToGlobalCondIds = rawCondToSeflGlobalCondTOs.stream()
                .collect(Collectors.toMap(
                        RawConditionToSelfGlobalConditionTO::getRawConditionId,
                        RawConditionToSelfGlobalConditionTO::getGlobalConditionId));
        log.debug("Raw condition IDs retrieved ({} entries) in {} ms",
                rawCondIdToGlobalCondIds.size(), System.currentTimeMillis() - startTimeRawConds);

        if (rawCondIdToGlobalCondIds.isEmpty()) {
            log.debug("No raw conditions matched the requested global conditions; returning empty result");
            return log.traceExit(new HashMap<>());
        }

        //3. retrieve the rawExpressionCalls filtering on rawConditionIds and datatypes
        ObservedExpressionDAO obsExprDAO = this.getDaoManager().getObservedExpressionDAO();
        // generate the filter from all info we already have
        //XXX: Could be created directly when instantiating the ExpressionCallLoader, Didn't want to touch the Loader while testing the new approach
        DAOObservedExpressionFilter obsExprFilter = new DAOObservedExpressionFilter(this.geneMap.keySet(),
                queriedDaoDataTypes, rawCondIdToGlobalCondIds.keySet());

        // first key -> bgeeGeneId, 2nd key globalConditionId
        long startTimeObsExpr = System.currentTimeMillis();
        List<ObservedExpressionTO> observedExpressionTOs =
            obsExprDAO.getObservedExpression(obsExprFilter, null).stream().toList();
        Set<Integer> unmatchedRawCondIds = observedExpressionTOs.stream()
            .map(ObservedExpressionTO::getConditionId)
            .filter(id -> !rawCondIdToGlobalCondIds.containsKey(id))
            .collect(Collectors.toSet());
        if (!unmatchedRawCondIds.isEmpty()) {
            throw log.throwing(new IllegalStateException(
                "Observed expression rows reference raw condition IDs missing from "
                + "raw-to-global mapping: " + unmatchedRawCondIds));
        }
        Map<Integer, Map<Integer, Set<ObservedExpressionTO>>> geneToGlobalCondIdToRawExpressionCall =
            observedExpressionTOs.stream()
                    .collect(Collectors.groupingBy(
                        ObservedExpressionTO::getBgeeGeneId,
                        Collectors.groupingBy(
                            to -> rawCondIdToGlobalCondIds.get(to.getConditionId()),
                            Collectors.toSet()
                        )
                    ));
        log.debug("Observed expression calls retrieved ({} genes) in {} ms",
                geneToGlobalCondIdToRawExpressionCall.size(), System.currentTimeMillis() - startTimeObsExpr);

        //5. use the topological order and the map<condId, Set<directParentCondId>> to propagate the calls.
        //   filterConditionIds restricts score computation to the queried conditions;
        //   propagation stops at the filter boundary so no wasteful scores are computed
        //   for ancestor conditions (e.g. "nervous system" when only "brain" was requested).
        long startTimePropagation = System.currentTimeMillis();
        Map<Gene, Set<OTFExpressionCall>> propagatedExpressionCalls = propagateCalls(
                geneToGlobalCondIdToRawExpressionCall, condGraphCache, filterConditionIds);
        log.debug("Calls propagated ({} genes) in {} ms",
                propagatedExpressionCalls.size(), System.currentTimeMillis() - startTimePropagation);
        // filter condition needed for on-the-fly propagation but not requested by the condition filters
        // happens when a condition parameter value is provided for anat. entity, cell type of dev. stage
        // but child terms are not expected.
        // ALSO filter on the requested summary call type (present/absent) if any.
        //TODO: benchmark advantage of doing these steps during propagation. It would probably be harder to debug
        //      but would be faster
        Predicate<OTFExpressionCall> filter =
                OTFExpressionCallFilterEngine.compile(this.processedFilter.getSourceFilter().getConditionFilters());
        Map<Gene, Set<OTFExpressionCall>> filtered =
                propagatedExpressionCalls.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream()
                            .filter(filter)
                            .filter(this::matchesRequestedSummaryCallType)
                            .collect(Collectors.toSet())
                        ));
        //order result and filter present/absent if required.
        Map<Gene, List<OTFExpressionCall>> sortedCalls =
                filtered.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().stream()
                                        .sorted(Comparator.comparing(
                                                OTFExpressionCall::getExpressionScore,
                                                Comparator.nullsLast(Comparator.reverseOrder())
                                        ))
                                        .toList()
                        ));

        return log.traceExit(sortedCalls);
    }

    private boolean matchesRequestedSummaryCallType(OTFExpressionCall call) {
        log.traceEntry("{}", call);

        Map<ExpressionSummary, SummaryQuality> requestedSummaryCallTypeQualityFilter =
                this.processedFilter.getSourceFilter().getSummaryCallTypeQualityFilter();
        if (requestedSummaryCallTypeQualityFilter == null ||
                requestedSummaryCallTypeQualityFilter.isEmpty() ||
                requestedSummaryCallTypeQualityFilter.equals(ExpressionCallFilter2.ALL_CALLS)) {
            return log.traceExit(true);
        }

        BigDecimal allPValue = call.getAllDataTypePValue();
        if (allPValue == null) {
            return log.traceExit(false);
        }

        boolean match = requestedSummaryCallTypeQualityFilter.entrySet().stream()
                .anyMatch(e -> {
                    ExpressionSummary summary = e.getKey();
                    SummaryQuality quality = e.getValue();

                    if (ExpressionSummary.EXPRESSED.equals(summary)) {
                        if (SummaryQuality.GOLD.equals(quality)) {
                            return allPValue.compareTo(this.processedFilter.getPresentHighThreshold()) <= 0;
                        }
                        // SILVER and BRONZE: present in self+descendant.
                        if (allPValue.compareTo(this.processedFilter.getPresentLowThreshold()) <= 0) {
                            return true;
                        }
                        // BRONZE also accepts calls present in at least one descendant condition.
                        return SummaryQuality.BRONZE.equals(quality)
                                && call.getBestDirectDescendantAllDataTypePValue() != null
                                && call.getBestDirectDescendantAllDataTypePValue()
                                        .compareTo(this.processedFilter.getPresentLowThreshold()) <= 0;
                    }
                    if (ExpressionSummary.NOT_EXPRESSED.equals(summary)) {
                        BigDecimal absentThreshold = SummaryQuality.GOLD.equals(quality)?
                                this.processedFilter.getAbsentHighThreshold():
                                this.processedFilter.getAbsentLowThreshold();

                        // Must be absent in self+descendant considering all requested data types.
                        if (allPValue.compareTo(absentThreshold) <= 0) {
                            return false;
                        }
                        // Must have observed data in self condition.
                        if (!Boolean.TRUE.equals(call.getDataPropagation().isIncludingObservedData())) {
                            return false;
                        }
                        // Must have no PRESENT evidence in descendants (all requested data types).
                        if (call.getBestDirectDescendantAllDataTypePValue() != null &&
                                call.getBestDirectDescendantAllDataTypePValue()
                                        .compareTo(this.processedFilter.getPresentLowThreshold()) <= 0) {
                            return false;
                        }

                        if (SummaryQuality.BRONZE.equals(quality)) {
                            return true;
                        }

                        // SILVER/GOLD: same constraints must hold on trusted data types.
                        BigDecimal trustedPValue = call.getTrustedDataTypePValue();
                        if (trustedPValue == null || trustedPValue.compareTo(absentThreshold) <= 0) {
                            return false;
                        }
                        return call.getBestDirectDescendantTrustedDataTypePValue() == null ||
                                call.getBestDirectDescendantTrustedDataTypePValue()
                                        .compareTo(this.processedFilter.getPresentLowThreshold()) > 0;
                    }
                    return false;
                });

        return log.traceExit(match);
    }

    private Map<Gene, Set<OTFExpressionCall>> propagateCalls(
            Map<Integer, Map<Integer, Set<ObservedExpressionTO>>> geneToGlobalCondIdToRawExpressionCall,
            ConditionGraphCache condGraphCache, Set<Integer> filterConditionIds) {
        log.traceEntry("{}, {}, {}", geneToGlobalCondIdToRawExpressionCall, condGraphCache, filterConditionIds);

        Map<Integer, int[]> parentCondIds = condGraphCache.getGlobalCondToDirectAncestors();
        Map<Integer, int[]> descendantCondIds = condGraphCache.getGlobalCondToDirectDescendants();
        int[] topoOrder = condGraphCache.getTopoOrder();

        Map<Gene, Set<OTFExpressionCall>> geneToExpressionCall = new HashMap<>();

        // For each gene independently
        for (Map.Entry<Integer, Map<Integer, Set<ObservedExpressionTO>>> geneEntry :
                geneToGlobalCondIdToRawExpressionCall.entrySet()) {
            long startTimeGene = System.currentTimeMillis();

            //key globalCondId value ExpressionCall to retrieve at the end
            Map<Integer, OTFExpressionCall> globalCondIdToExpressionCall = new HashMap<>();

            //the Id of the gene for which we propagate calls
            Integer geneId = geneEntry.getKey();
            //Map that contains all self observed expression from the database per condition
            Map<Integer, Set<ObservedExpressionTO>> globalCondIdToObservedExpressionTOs = geneEntry.getValue();

            //Init the Set of conditions to parse. Once empty, propagation is over.
            Set<Integer> conditionToParse = new HashSet<>(globalCondIdToObservedExpressionTOs.keySet());
//            // Pre-load the initial leaf conditions into conditionMap so that
//            // generateOTFExpressionCall can look them up even when conditionMap was
//            // seeded from an empty condition filter.
//            updateConditionMap(conditionToParse);

            Set<Integer> parsedConditions = new HashSet<>();
            // Collected during propagation; removed after the loop so that every ancestor
            // can still read a child's entry while computing its own call.
            // Transitive redundancy (A==B score/pval, B==C) is handled correctly: B stays
            // in the map when C is processed, so C is also detected and collected.
            Set<Integer> redundantCondIds = new HashSet<>();

            // Topological propagation (child → parents) + redundancy detection
            for (int condId : topoOrder) {
                if (conditionToParse.isEmpty()) {
                    break;
                }
                if (conditionToParse.contains(condId)) {
                    conditionToParse.remove(condId);
                    // Guard against re-processing (indicates a cycle) and lazily load
                    // Condition2 objects for parent conditions not yet in conditionMap.
                    // Both are done per-parent to avoid a separate full-set scan.
                    // Only propagate to parents that are within the filter. When a filter
                    // is active, parents outside it (e.g. "nervous system" when brain was
                    // queried) are skipped: their scores are never computed and they are
                    // never added to conditionToParse, so the loop terminates early.
                    Set<Integer> parentIdSet = new HashSet<>();
                    for (int parentId : parentCondIds.get(condId)) {
                        if (filterConditionIds.isEmpty() || filterConditionIds.contains(parentId)) {
                            if (parsedConditions.contains(parentId)) {
                                throw log.throwing(new IllegalStateException(
                                        "Condition " + parentId + " already parsed — cycle or propagation bug"));
                            }
                            parentIdSet.add(parentId);
                            conditionToParse.add(parentId);
                        }
                    }
//                    updateConditionMap(parentIdSet);

                    // retrieve self expression
                    Set<ObservedExpressionTO> selfExpressionTOs = globalCondIdToObservedExpressionTOs.get(condId);
                    int[] children = descendantCondIds.get(condId);
                    List<OTFExpressionCall> descendantCalls = new ArrayList<>(children == null ? 0 : children.length);
                    if (children != null) {
                        for (int childId: children) {
                            if (!parsedConditions.contains(childId)) {
                                continue;
                            }
                            OTFExpressionCall childCall = globalCondIdToExpressionCall.get(childId);
                            if (childCall != null) {
                                descendantCalls.add(childCall);
                            }
                        }
                    }
                    OTFExpressionCall expressionCall = generateOTFExpressionCall(
                            geneMap.get(geneId), conditionMap.get(condId),
                            selfExpressionTOs, descendantCalls);
                    globalCondIdToExpressionCall.put(condId, expressionCall);

                    // Check immediately whether condId is redundant by comparing it to the best
                    // descendant score/p-value already computed in expressionCall.
                    // We only collect here; actual removal is deferred to after the loop.
                    if (expressionCall.getExpressionScore() != null
                            && expressionCall.getBestDirectDescendantExpressionScore() != null
                            && expressionCall.getExpressionScore().compareTo(
                                    expressionCall.getBestDirectDescendantExpressionScore()) == 0) {
                        redundantCondIds.add(condId);
                    }
                }
                parsedConditions.add(condId);
            }

            if (!redundantCondIds.isEmpty()) {
                log.debug("Pruning {} redundant ancestor condition(s) for gene {} "
                        + "(same score and p-value as a descendant)", redundantCondIds.size(), geneId);
                globalCondIdToExpressionCall.keySet().removeAll(redundantCondIds);
            }

            geneToExpressionCall.put(geneMap.get(geneId),
                    globalCondIdToExpressionCall.values().stream().collect(Collectors.toSet()));
            log.debug("Propagation for gene {} completed in {} ms, {} calls generated",
                    geneId, System.currentTimeMillis() - startTimeGene,
                    globalCondIdToExpressionCall.size());
        }

        return log.traceExit(geneToExpressionCall);
    }

    /**
     * 
     * @param unsortedCalls                 A {@code Set} of {@code ExpressionCallOTF} that contains all calls to filter and/or order
     * @param keepOnlyParentsMoreExpressed  A boolean used to filter (true) or not filter (false) calls that have a descendant call with
     *                                      higher or equal expression score. It allows to avoid showing lots of generic terms
     * @param orderingAttribute
     * @return
     */
    //TODO: investigate why keepOnlyParentsMoreExpressed is useful and choosing the summary quality is not enough. SummaryQuality.SILVER allows to remove all
    //      condition for which a gene does not have delf observation. The only calls this filtering removes compared to SummaryQuality.BRONZE are the calls
    //      that have self expression lower than the descendant condition. Isn't it an interesting info to provide?
//    public List<OTFExpressionCall> filterAndOrderExpressionCalls(Set<OTFExpressionCall> unsortedCalls, boolean keepOnlyParentsMoreExpressed,
//            EnumSet<OTFExpressionCall.OrderingAttribute> orderingAttribute) {
//        log.traceEntry("{}, {}, {}", unsortedCalls, keepOnlyParentsMoreExpressed, orderingAttribute);
//        
//        return null;
//    }

    private OTFExpressionCall generateOTFExpressionCall(Gene gene, Condition2 cond,
            Collection<ObservedExpressionTO> selfObservations,
            Collection<OTFExpressionCall> descExpressionCalls) {
        log.traceEntry("{}, {}, {}, {}", gene, cond, selfObservations, descExpressionCalls);
        Collection<ObservedExpressionTO> usedSelfObservations = selfObservations == null ?
            Collections.emptySet() : selfObservations;
        Collection<OTFExpressionCall> usedDescExprCalls = descExpressionCalls == null ?
            Collections.emptyList() : descExpressionCalls;

        if (usedSelfObservations.isEmpty() && usedDescExprCalls.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Raw data and child calls cannot be both empty"));
        }

        //rawData can be empty if no raw data in the condition itself.
        //first, compute information from data in the condition itself.
        //We use Lists not to loose equals PValues
        List<BigDecimal> allDataTypePValues = new ArrayList<>();
        List<BigDecimal> trustedDataTypePValues = new ArrayList<>();
        BigDecimal scoreByWeightSum = BigDecimal.ZERO;
        BigDecimal weightSum = BigDecimal.ZERO;
        //XXX: The number of observation is propbably useful to calculate the pvalue. Right now we add the pvalue as many time as number of observation.
        //TODO: To discuss with Fred
        EnumSet<DataType> supportingDataTypes = EnumSet.noneOf(DataType.class);
        PropagationState dataPropagation = usedSelfObservations.isEmpty()?
                null: PropagationState.SELF;

        // retrieve self info of the expression call
        for (ObservedExpressionTO obsExpression : usedSelfObservations) {
            if (obsExpression.getBulkNumberObs() != null && obsExpression.getBulkNumberObs() != 0) {
                allDataTypePValues.addAll(Collections.nCopies(obsExpression.getBulkNumberObs(), obsExpression.getBulkPValue()));
                trustedDataTypePValues.addAll(Collections.nCopies(obsExpression.getBulkNumberObs(), obsExpression.getBulkPValue()));
                scoreByWeightSum = scoreByWeightSum
                        .add((obsExpression.getBulkScore()
                                .multiply(obsExpression.getBulkWeight())
                                .multiply(BigDecimal.valueOf(obsExpression.getBulkNumberObs()))));
                weightSum = weightSum.
                        add(obsExpression.getBulkWeight()
                                .multiply(BigDecimal.valueOf(obsExpression.getBulkNumberObs())));
                supportingDataTypes.add(DataType.RNA_SEQ);
            }
            if (obsExpression.getInSituNumberObs() != null && obsExpression.getInSituNumberObs() != 0) {
                allDataTypePValues.addAll(Collections.nCopies(obsExpression.getInSituNumberObs(), obsExpression.getInSituPValue()));
                trustedDataTypePValues.addAll(Collections.nCopies(obsExpression.getInSituNumberObs(), obsExpression.getInSituPValue()));
                scoreByWeightSum = scoreByWeightSum
                        .add((obsExpression.getInSituScore()
                                .multiply(obsExpression.getInSituWeight())
                                .multiply(BigDecimal.valueOf(obsExpression.getInSituNumberObs()))));
                weightSum = weightSum.
                        add(obsExpression.getInSituWeight()
                                .multiply(BigDecimal.valueOf(obsExpression.getInSituNumberObs())));
                supportingDataTypes.add(DataType.IN_SITU);
            }
            if (obsExpression.getFullLengthNumberObs() != null && obsExpression.getFullLengthNumberObs() != 0) {
                allDataTypePValues.addAll(Collections.nCopies(obsExpression.getFullLengthNumberObs(), obsExpression.getFullLengthPValue()));
                trustedDataTypePValues.addAll(Collections.nCopies(obsExpression.getFullLengthNumberObs(), obsExpression.getFullLengthPValue()));
                scoreByWeightSum = scoreByWeightSum
                        .add((obsExpression.getFullLengthScore()
                                .multiply(obsExpression.getFullLengthWeight())
                                .multiply(BigDecimal.valueOf(obsExpression.getFullLengthNumberObs()))));
                weightSum = weightSum.
                        add(obsExpression.getFullLengthWeight()
                                .multiply(BigDecimal.valueOf(obsExpression.getFullLengthNumberObs())));
                supportingDataTypes.add(DataType.SC_RNA_SEQ);
            }
            if (obsExpression.getDropletNumberObs() != null && obsExpression.getDropletNumberObs() != 0) {
                allDataTypePValues.addAll(Collections.nCopies(obsExpression.getDropletNumberObs(), obsExpression.getDropletPValue()));
                trustedDataTypePValues.addAll(Collections.nCopies(obsExpression.getDropletNumberObs(), obsExpression.getDropletPValue()));
                scoreByWeightSum = scoreByWeightSum
                        .add((obsExpression.getDropletScore()
                                .multiply(obsExpression.getDropletWeight())
                                .multiply(BigDecimal.valueOf(obsExpression.getDropletNumberObs()))));
                weightSum = weightSum.
                        add(obsExpression.getDropletWeight()
                                .multiply(BigDecimal.valueOf(obsExpression.getDropletNumberObs())));
                supportingDataTypes.add(DataType.SC_RNA_SEQ);
            }
        }

        BigDecimal bestDescendantAllDataTypePValue = null;
        BigDecimal bestDescendantTrustedDataTypePValue = null;
        BigDecimal bestDescendantExpressionScore = null;
        BigDecimal bestDescendantExpressionScoreWeight = null;
        if (!usedDescExprCalls.isEmpty()) {

            dataPropagation = dataPropagation == null? PropagationState.DESCENDANT: PropagationState.SELF_AND_DESCENDANT;
            List<BigDecimal> descAllDataTypePValues = new ArrayList<>(usedDescExprCalls.size());
            List<BigDecimal> descTrustedDataTypePValues = new ArrayList<>(usedDescExprCalls.size());
            for (OTFExpressionCall childCall: usedDescExprCalls) {
                supportingDataTypes.addAll(childCall.getSupportingDataTypes());
                descAllDataTypePValues.add(childCall.getAllDataTypePValue());
                if (childCall.getTrustedDataTypePValue() != null) {
                    descTrustedDataTypePValues.add(childCall.getTrustedDataTypePValue());
                }
                BigDecimal scoreByWeight = childCall.getExpressionScoreWeight().multiply(childCall.getExpressionScore());
                scoreByWeightSum = scoreByWeightSum.add(scoreByWeight);
                weightSum = weightSum.add(childCall.getExpressionScoreWeight());

                bestDescendantAllDataTypePValue = getBestDescendantValue(bestDescendantAllDataTypePValue,
                        childCall.getAllDataTypePValue(), childCall.getBestDirectDescendantAllDataTypePValue());
                bestDescendantTrustedDataTypePValue = getBestDescendantValue(bestDescendantTrustedDataTypePValue,
                        childCall.getTrustedDataTypePValue(), childCall.getBestDirectDescendantTrustedDataTypePValue());
                if (bestDescendantExpressionScore == null ||
                        childCall.getExpressionScore().compareTo(bestDescendantExpressionScore) > 0) {
                    bestDescendantExpressionScore = childCall.getExpressionScore();
                    bestDescendantExpressionScoreWeight = childCall.getExpressionScoreWeight();
                }
                if (childCall.getBestDirectDescendantExpressionScore() != null &&
                        childCall.getBestDirectDescendantExpressionScore().compareTo(bestDescendantExpressionScore) > 0) {
                    bestDescendantExpressionScore = childCall.getBestDirectDescendantExpressionScore();
                    bestDescendantExpressionScoreWeight = childCall.getBestDirectDescendantExpressionScoreWeight();
                }
            }
            allDataTypePValues.add(computeFDRCorrectedPValue(descAllDataTypePValues));
            if (!descTrustedDataTypePValues.isEmpty()) {
                trustedDataTypePValues.add(computeFDRCorrectedPValue(descTrustedDataTypePValues));
            }
        }
        BigDecimal ultimateAllDataTypePValue = computeMean(allDataTypePValues);
        BigDecimal ultimateTrustedDataTypePValue = computeMean(trustedDataTypePValues);
//        log.debug("weightSum: {}, scoreByWeightSum: {}", weightSum, scoreByWeightSum);
        if (BigDecimal.ZERO.compareTo(weightSum) == 0) {
            log.warn("weightSum is zero for gene {} in condition {} - all observation counts are null/0. Defaulting score to 0.", gene, cond);
        }
        BigDecimal weightedAverageExpressionScore = BigDecimal.ZERO.compareTo(weightSum) == 0 ?
                BigDecimal.ZERO :
                scoreByWeightSum.divide(weightSum, 2, RoundingMode.HALF_UP);

        OTFExpressionCall resultingCall = new OTFExpressionCall(gene, cond, supportingDataTypes,
              ultimateAllDataTypePValue, ultimateTrustedDataTypePValue,
              bestDescendantAllDataTypePValue, bestDescendantTrustedDataTypePValue,
              weightSum, weightedAverageExpressionScore,
              bestDescendantExpressionScoreWeight, bestDescendantExpressionScore,
              dataPropagation);

        return log.traceExit(resultingCall);
    }

    private static BigDecimal getBestDescendantValue(BigDecimal currentBestDescendantValue,
            BigDecimal descendantValue, BigDecimal descendantBestDescendantValue) {
        log.traceEntry("{}, {}, {}", currentBestDescendantValue, descendantValue, descendantBestDescendantValue);

        if (descendantValue != null && (currentBestDescendantValue == null ||
                descendantValue.compareTo(currentBestDescendantValue) < 0)) {
            currentBestDescendantValue = descendantValue;
        }
        if (descendantBestDescendantValue != null && (currentBestDescendantValue == null ||
                descendantBestDescendantValue.compareTo(currentBestDescendantValue) < 0)) {
            currentBestDescendantValue = descendantBestDescendantValue;
        }
        return log.traceExit(currentBestDescendantValue);
    }

    protected BigDecimal computeFDRCorrectedPValue(List<BigDecimal> pValues) {
        log.traceEntry("{}", pValues);

        int m = pValues.size();
        Double[] pValuesDouble = 
                pValues.stream()
                .map(p -> p.compareTo(ZERO_BIGDECIMAL) == 0 ? ABOVE_ZERO_BIGDECIMAL : p)
                .map(p -> p.doubleValue())
                .toArray(length -> new Double[length]);
        double[] adjustedPValues = new double[m];

        Arrays.sort(pValuesDouble);
        // iterate through all p-values:  largest to smallest
        for (int i = m - 1; i >= 0; i--) {
            if (i == m - 1) {
                adjustedPValues[i] = pValuesDouble[i];
            } else {
                double unadjustedPvalue = pValuesDouble[i];
                int divideByM = i + 1;
                double left = adjustedPValues[i + 1];
                double right = (m / (double) divideByM) * unadjustedPvalue;
                adjustedPValues[i] = Math.min(left, right);
            }
        }
        //Find the smallest corrected p-value
        BigDecimal fdr = BigDecimal.valueOf(Arrays.stream(adjustedPValues).min().getAsDouble());
        //If the FDR is less than MIN_FDR_BIGDECIMAL, change it to MIN_FDR_BIGDECIMAL
        //(in order to avoid having fields in the globalExpression table with too  much precision)
        if (fdr.compareTo(MIN_FDR_BIGDECIMAL) < 0) {
            fdr = MIN_FDR_BIGDECIMAL;
        }
        return log.traceExit(fdr);
    }

    protected BigDecimal computeMean(List<BigDecimal> pValues) {
        log.traceEntry("{}", pValues);
        if (pValues == null || pValues.isEmpty()) {
            return null;
        }

        BigDecimal sum = pValues.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(
                BigDecimal.valueOf(pValues.size()),
                //34 significant digits and RoundingMode.HALF_EVEN
                MathContext.DECIMAL128
        );
    }


    public long loadDataCount() {
        log.traceEntry();

        //If the DAOCallFilters are null (different from: not-null and empty)
        //it means there was no matching conds and thus no result for sure
        if (this.processedFilter.getDaoFilters() == null) {
            return log.traceExit(0L);
        }
        //FIXME: this value, and maybe also per species, must be inserted in a new table of the database,
        //and getGlobalExpressionCallsCount to detect when the filter is empty and use that table
        if (this.processedFilter.getSourceFilter().isEmptyFilter()) {
            return log.traceExit(7111443197L);
        }
        return log.traceExit(this.globalExprCallDAO.getGlobalExpressionCallsCount(
                this.processedFilter.getDaoFilters()));
    }

    public ExpressionCallPostFilter loadPostFilter() {
        log.traceEntry();
        //If the DAOCallFilters are null (different from: not-null and empty)
        //it means there was no matching conds and thus no result for sure
        if (this.processedFilter.getDaoFilters() == null) {
            return log.traceExit(new ExpressionCallPostFilter());
        }

        Function<Collection<ConditionDAO.Attribute>, ConditionTOResultSet> condRequestFun = (attrs) ->
        this.condDAO.getGlobalConditionsFromCallFilters(this.getProcessedFilter().getDaoFilters(), attrs);
        Map<ConditionParameter<?, ?>, Set<? extends Object>> condParamEntities = new HashMap<>();

        // retrieve anatEntities and cell types
        if (this.getProcessedFilter().getSourceFilter().getCondParamCombination()
                .contains(ConditionParameter.ANAT_ENTITY_CELL_TYPE)) {
            Set<String> anatEntityIds = condRequestFun.apply(
                    Set.of(ConditionDAO.Attribute.ANAT_ENTITY_ID)).stream()
                    .map(a -> a.getAnatEntityId()).collect(Collectors.toSet());
            Set<String> cellTypeIds = condRequestFun.apply(
                    Set.of(ConditionDAO.Attribute.CELL_TYPE_ID))
                    .stream()
                    .map(c -> c.getCellTypeId())
                    //cell type is the only condition param that can be NULL,
                    //we end up requesting an anat. entity with ID "NULL"
                    .filter(s -> s != null)
                    .collect(Collectors.toSet());
            Set<String> anatEntityCellTypeIds = new HashSet<>(anatEntityIds);
            anatEntityCellTypeIds.addAll(cellTypeIds);
            Set<AnatEntity> anatEntityCellTypes = anatEntityCellTypeIds.isEmpty()?
                    new HashSet<>() : anatEntityService.loadAnatEntities(anatEntityCellTypeIds, false)
                    .collect(Collectors.toSet());
            condParamEntities.put(ConditionParameter.ANAT_ENTITY_CELL_TYPE, anatEntityCellTypes);
        }

        //retrieve dev. stages
        if (this.getProcessedFilter().getSourceFilter().getCondParamCombination()
                .contains(ConditionParameter.DEV_STAGE)) {
            Set<String> stageIds = condRequestFun.apply(
                    Set.of(ConditionDAO.Attribute.STAGE_ID))
                    .stream().map(c -> c.getStageId()).collect(Collectors.toSet());
            Set<DevStage> stages = stageIds.isEmpty()?
                    new HashSet<>() : devStageService.loadDevStages(null, null, stageIds, false)
                    .collect(Collectors.toSet());
            condParamEntities.put(ConditionParameter.DEV_STAGE, stages);
        }

        // retrieve strains
        if (this.getProcessedFilter().getSourceFilter().getCondParamCombination()
                .contains(ConditionParameter.STRAIN)) {
            Set<String> strainIds = condRequestFun.apply(
                    Set.of(ConditionDAO.Attribute.STRAIN_ID))
                    .stream().map(c -> c.getStrainId()).collect(Collectors.toSet());
            Set<Strain> strains = strainIds.isEmpty()? new HashSet<>():
                this.strainService.loadStrains(strainIds).collect(Collectors.toSet());
            condParamEntities.put(ConditionParameter.STRAIN, strains);
        }

        //retrieve sexes
        if (this.getProcessedFilter().getSourceFilter().getCondParamCombination()
                .contains(ConditionParameter.SEX)) {
            Set<String> sexIds = condRequestFun.apply(
                    Set.of(ConditionDAO.Attribute.SEX_ID))
                    .stream().map(c -> c.getSex().getStringRepresentation()).collect(Collectors.toSet());
            Set<Sex> sexes = this.sexService.loadSexes(sexIds).collect(Collectors.toSet());
            condParamEntities.put(ConditionParameter.SEX, sexes);
        }

        return log.traceExit(new ExpressionCallPostFilter(condParamEntities));
    }

    public ExpressionCallProcessedFilter getProcessedFilter() {
        return processedFilter;
    }

    private void updateConditionMap(Set<Integer> condIds) {
        log.traceEntry("{}", condIds);

        Set<Integer> missingCondIds = new HashSet<>(condIds);
        missingCondIds.removeAll(this.conditionMap.keySet());
        if (missingCondIds.isEmpty()) {
            log.traceExit();
            return;
        }
        Map<Integer, Species> speciesMap = this.processedFilter.getSpeciesMap();
        Map<Integer, Condition2> missingCondMap = this.utils.loadConditionMapFromResultSet(
                        (attrs) -> this.condDAO.getGlobalConditionsFromIds(missingCondIds, attrs),
                        this.utils.convertCondParamsToDAOCondAttributes(
                                this.processedFilter.getSourceFilter().getCondParamCombination()),
                        speciesMap.values(), this.anatEntityService, this.devStageService,
                        this.sexService, this.strainService);
        //If the Map is going to grow too big, we keep only the entries needed
        //for this method call
        if (this.conditionMap.size() + missingCondMap.size() > MAX_ELEMENTS_IN_MAP) {
            this.conditionMap.keySet().retainAll(condIds);
        }
        this.conditionMap.putAll(missingCondMap);

        log.traceExit();
        return;
    }
    private void updateGeneMap(Set<Integer> bgeeGeneIds) {
        log.traceEntry("{}", bgeeGeneIds);

        Set<Integer> missingGeneIds = new HashSet<>(bgeeGeneIds);
        missingGeneIds.removeAll(this.geneMap.keySet());
        if (missingGeneIds.isEmpty()) {
            log.traceExit(); return;
        }
        Map<Integer, Species> speciesMap = this.processedFilter.getSpeciesMap();
        Map<Integer, GeneBioType> geneBioTypeMap = this.processedFilter.getGeneBioTypeMap();
        Map<Integer, Gene> missingGeneMap = this.geneDAO.getGenesByBgeeIds(missingGeneIds).stream()
                .collect(Collectors.toMap(gTO -> gTO.getId(), gTO -> mapGeneTOToGene(gTO,
                        Optional.ofNullable(speciesMap.get(gTO.getSpeciesId()))
                        .orElseThrow(() -> new IllegalStateException("Missing species ID for gene")),
                        null, null,
                        Optional.ofNullable(geneBioTypeMap.get(gTO.getGeneBioTypeId()))
                        .orElseThrow(() -> new IllegalStateException("Missing gene biotype ID for gene")))));
        //If the Map is going to grow too big, we keep only the entries needed
        //for this method call
        if (this.geneMap.size() + missingGeneMap.size() > MAX_ELEMENTS_IN_MAP) {
            this.geneMap.keySet().retainAll(bgeeGeneIds);
        }
        this.geneMap.putAll(missingGeneMap);

        log.traceExit(); return;
    }

    private EnumSet<CallService.Attribute> getAttributes(ExpressionCallFilter2 callFilter) {
        log.traceEntry("{}", callFilter);
      //For now we define the attributes ourselves, and we still use the Attributes
        //from the CallService
        //TODO: implement Attributes in ExpressionCallLoader
        EnumSet<CallService.Attribute> attributes = EnumSet.of(
                CallService.Attribute.GENE,
                CallService.Attribute.CALL_TYPE,
                CallService.Attribute.DATA_QUALITY,
                CallService.Attribute.EXPRESSION_SCORE,
                //to know how the propagation status of the call
                CallService.Attribute.OBSERVED_DATA,
                //We need the p-value info per data type to know which data types
                //produced the calls
                CallService.Attribute.P_VALUE_INFO_EACH_DATA_TYPE,
                //We also want to know the global FDR-corrected p-value
                CallService.Attribute.P_VALUE_INFO_ALL_DATA_TYPES);
        attributes.addAll(callFilter.getCondParamCombination().stream()
                .flatMap(param -> {
                    //Any condition parameter attribute would do to retrieve the condition IDs,
                    //but we map properly anyway.
                    if (ConditionParameter.ANAT_ENTITY_CELL_TYPE.equals(param)) {
                        return Stream.of(CallService.Attribute.ANAT_ENTITY_ID,
                                CallService.Attribute.CELL_TYPE_ID);
                    } else if (ConditionParameter.DEV_STAGE.equals(param)) {
                        return Stream.of(CallService.Attribute.DEV_STAGE_ID);
                    } else if (ConditionParameter.SEX.equals(param)) {
                        return Stream.of(CallService.Attribute.SEX_ID);
                    } else if (ConditionParameter.STRAIN.equals(param)) {
                        return Stream.of(CallService.Attribute.STRAIN_ID);
                    }
                    throw log.throwing(new UnsupportedOperationException(
                            "Unsupported ConditionParameter: " + param));
                })
                .collect(Collectors.toSet()));
        return log.traceExit(attributes);
    }
    private Set<GlobalExpressionCallDAO.AttributeInfo> convertServiceAttrToGlobalExprDAOAttr(
            EnumSet<CallService.Attribute> attributes, ExpressionCallFilter2 callFilter) {
        log.traceEntry("{}, {}", attributes, callFilter);

        EnumSet<DAODataType> daoDataTypes = this.utils.convertDataTypeToDAODataType(callFilter == null? null:
            callFilter.getDataTypeFilters());
        EnumSet<DAODataType> daoDataTypesTrustedForAbsentCalls =
                this.utils.convertTrustedAbsentDataTypesToDAODataTypes(callFilter == null? null:
                    callFilter.getDataTypeFilters());
        //TODO to upate to use ConditionDAO.ConditionParameter
        EnumSet<ConditionDAO.Attribute> daoCondParamComb = this.utils
                .convertCondParamsToDAOCondAttributes(callFilter.getCondParamCombination());

        return log.traceExit(attributes.stream().flatMap(attr -> {
            if (attr.isConditionParameter()) {

                return Stream.of(new GlobalExpressionCallDAO.AttributeInfo(
                        GlobalExpressionCallDAO.Attribute.GLOBAL_CONDITION_ID));

            } else if (attr.equals(CallService.Attribute.P_VALUE_INFO_ALL_DATA_TYPES) ||
                    attr.equals(CallService.Attribute.CALL_TYPE) ||
                    attr.equals(CallService.Attribute.DATA_QUALITY)) {

                Set<GlobalExpressionCallDAO.AttributeInfo> pValAttributes = new HashSet<>();
                pValAttributes.add(new GlobalExpressionCallDAO.AttributeInfo(
                        GlobalExpressionCallDAO.Attribute.FDR_P_VALUE_COND_INFO,
                        daoDataTypes, null));
                pValAttributes.add(new GlobalExpressionCallDAO.AttributeInfo(
                        GlobalExpressionCallDAO.Attribute.FDR_P_VALUE_DESCENDANT_COND_INFO,
                        daoDataTypes, null));
                if (!daoDataTypesTrustedForAbsentCalls.isEmpty()) {
                    pValAttributes.add(new GlobalExpressionCallDAO.AttributeInfo(
                            GlobalExpressionCallDAO.Attribute.FDR_P_VALUE_COND_INFO,
                            daoDataTypesTrustedForAbsentCalls, null));
                    pValAttributes.add(new GlobalExpressionCallDAO.AttributeInfo(
                            GlobalExpressionCallDAO.Attribute.FDR_P_VALUE_DESCENDANT_COND_INFO,
                            daoDataTypesTrustedForAbsentCalls, null));
                }
                return pValAttributes.stream();

            } else if (attr.equals(CallService.Attribute.P_VALUE_INFO_EACH_DATA_TYPE)) {

                return daoDataTypes.stream()
                        .flatMap(dt -> Stream.of(
                                new GlobalExpressionCallDAO.AttributeInfo(
                                        GlobalExpressionCallDAO.Attribute.FDR_P_VALUE_COND_INFO,
                                        EnumSet.of(dt), null),
                                new GlobalExpressionCallDAO.AttributeInfo(
                                        GlobalExpressionCallDAO.Attribute.FDR_P_VALUE_DESCENDANT_COND_INFO,
                                        EnumSet.of(dt), null)));

            } else if (attr.equals(CallService.Attribute.GENE)) {

                return Stream.of(new GlobalExpressionCallDAO.AttributeInfo(
                        GlobalExpressionCallDAO.Attribute.BGEE_GENE_ID));

            } else if (attr.equals(CallService.Attribute.OBSERVED_DATA)) {

                //TODO: actually why do we use getAllPossibleCondParamCombinations in DAO?
                //We could generate the combination in bgee-core and just convert them
                return ConditionDAO.Attribute.getAllPossibleCondParamCombinations(daoCondParamComb)
                        .stream().map(comb -> new GlobalExpressionCallDAO.AttributeInfo(
                                GlobalExpressionCallDAO.Attribute.DATA_TYPE_OBSERVATION_COUNT_INFO,
                                daoDataTypes, comb));

            } else if (attr.equals(CallService.Attribute.MEAN_RANK) ||
                    attr.equals(CallService.Attribute.EXPRESSION_SCORE) ||
                    attr.equals(CallService.Attribute.GENE_QUAL_EXPR_LEVEL) ||
                    attr.equals(CallService.Attribute.ANAT_ENTITY_QUAL_EXPR_LEVEL)) {

                Set<GlobalExpressionCallDAO.AttributeInfo> rankAttributes = new HashSet<>();
                rankAttributes.add(new GlobalExpressionCallDAO.AttributeInfo(
                        GlobalExpressionCallDAO.Attribute.MEAN_RANK,
                        daoDataTypes, null));
                //We need to know the species to compute expression scores,
                //in order to retrieve the max rank in that species.
                if (attr.equals(CallService.Attribute.EXPRESSION_SCORE)) {
                    //The species info can be retrieved either from the gene or from the condition
                    if (!attributes.contains(CallService.Attribute.GENE) &&
                            Collections.disjoint(attributes,
                                    CallService.Attribute.getAllConditionParameters())) {
                        rankAttributes.add(new GlobalExpressionCallDAO.AttributeInfo(
                                GlobalExpressionCallDAO.Attribute.BGEE_GENE_ID));
                    }
                }
                return rankAttributes.stream();

            } else if (attr.equals(CallService.Attribute.DATA_TYPE_RANK_INFO)) {

                return Stream.of(new GlobalExpressionCallDAO.AttributeInfo(
                        GlobalExpressionCallDAO.Attribute.DATA_TYPE_RANK_INFO,
                        daoDataTypes, null));

            } else {
                throw log.throwing(new IllegalStateException(
                        "Unsupported Attributes from CallService: " + attr));
            }
        }).collect(Collectors.toSet()));
    }

    private LinkedHashMap<GlobalExpressionCallDAO.OrderingAttributeInfo, DAO.Direction>
    convertServiceOrderingAttrToGlobalExprDAOOrderingAttr(ExpressionCallFilter2 callFilter) {
        log.traceEntry("{}", callFilter);
        //for now we always order by bgeeGeneId, conditionId
        LinkedHashMap<GlobalExpressionCallDAO.OrderingAttributeInfo, DAO.Direction> orderAttrs =
                new LinkedHashMap<>();
        orderAttrs.put(
                new GlobalExpressionCallDAO.OrderingAttributeInfo(
                        GlobalExpressionCallDAO.OrderingAttribute.BGEE_GENE_ID),
                DAO.Direction.ASC);
        orderAttrs.put(
                new GlobalExpressionCallDAO.OrderingAttributeInfo(
                        GlobalExpressionCallDAO.OrderingAttribute.GLOBAL_CONDITION_ID),
                DAO.Direction.ASC);

        return log.traceExit(orderAttrs);
    }


}
