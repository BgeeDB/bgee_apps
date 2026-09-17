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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.CommonService;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.DAOObservedExpressionFilter;
import org.bgee.model.dao.api.expressiondata.ObservedExpressionDAO;
import org.bgee.model.dao.api.expressiondata.ObservedExpressionDAO.ObservedExpressionTO;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO.RawConditionToSelfGlobalConditionTO;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.PropagationState;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.expressiondata.call.CallFilter.ExpressionCallFilter2;
import org.bgee.model.expressiondata.call.ConditionGraphCacheService.ConditionGraphCache;
import org.bgee.model.gene.Gene;

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



    private final GeneDAO geneDAO;
    private final ConditionDAO condDAO;
    private final CallServiceUtils utils;
    /**
     * @see #getProcessedFilter()
     */
    private final ExpressionCallProcessedFilter processedFilter;

    //These attributes are mutable, it is acceptable for a Service.
    //We keep the speciesMap and geneBiotypeMap inside the rawDataProcessedFilter,
    //as there will be no update to them by this RawDataLoader.
    /**
     * An unmodifiable view of the {@code Condition2}s identified by the processed filter, where
     * keys are {@code Integer}s that are their internal IDs. Every condition reached by the
     * propagation is in this {@code Map}: the conditions queried, and the ancestors the
     * propagation is allowed to reach, are both bounded by its key set (see
     * {@link #loadDataOnTheFly()}). It is a view, and not a copy, so that several
     * {@code ExpressionCallLoader}s sharing a same processed filter do not each hold
     * a copy of the conditions of a whole species.
     */
    private final Map<Integer, Condition2> conditionMap;
    /**
     * A {@code Map} where keys are {@code Integer}s that are internal IDs of genes,
     * the value being the associated {@code Gene}. this {@code Map} is used
     * to store the retrieved {@code Gene}s over several independent calls
     * to this {@code ExpressionCallLoader}, in order to avoid querying multiple times for the same
     * genes.
     *
     * Unmodifiable, as {@link #conditionMap}.
     */
    private final Map<Integer, Gene> geneMap;

    ExpressionCallLoader(ExpressionCallProcessedFilter processedFilter, ServiceFactory serviceFactory) {
        this(processedFilter, serviceFactory, new CallServiceUtils());
    }
    //Constructor package protected so that only the RawDataService can instantiate this class
    ExpressionCallLoader(ExpressionCallProcessedFilter processedFilter,
            ServiceFactory serviceFactory, CallServiceUtils utils) {
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
        this.utils = utils;
        this.geneDAO = this.getDaoManager().getGeneDAO();
        this.condDAO = this.getDaoManager().getConditionDAO();
        this.processedFilter = processedFilter;
        //The conditions and genes identified by the processed filter are never updated by this
        //Loader, so they are exposed as unmodifiable views rather than copied.
        Map<Integer, Condition2> requestedCondMap = this.processedFilter.getRequestedConditionMap();
        Map<Integer, Gene> requestedGeneMap = this.processedFilter.getRequestedGeneMap();
        this.conditionMap = requestedCondMap == null? Map.of():
            Collections.unmodifiableMap(requestedCondMap);
        this.geneMap = requestedGeneMap == null? Map.of():
            Collections.unmodifiableMap(requestedGeneMap);
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
                geneToGlobalCondIdToRawExpressionCall, condGraphCache, filterConditionIds,
                this.processedFilter.getSourceFilter().isRedundantAncestorCallsFilter());
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

        //A call matches when its own summary call type and quality, inferred with the same
        //thresholds, is the requested one with a quality at least as good as the requested one
        //(the SummaryQuality enum is declared from the lowest to the highest quality, so that
        //its compareTo can be used).
        Entry<ExpressionSummary, SummaryQuality> callTypeQuality = OTFExpressionCallFilterEngine
                .inferSummaryCallTypeAndQuality(call,
                        this.processedFilter.getPresentHighThreshold(),
                        this.processedFilter.getPresentLowThreshold(),
                        this.processedFilter.getAbsentLowThreshold(),
                        this.processedFilter.getAbsentHighThreshold());
        if (callTypeQuality == null) {
            return log.traceExit(false);
        }
        boolean match = requestedSummaryCallTypeQualityFilter.entrySet().stream()
                .anyMatch(e -> callTypeQuality.getKey().equals(e.getKey()) &&
                        callTypeQuality.getValue().compareTo(e.getValue()) >= 0);

        return log.traceExit(match);
    }

    /**
     * @param filterRedundantAncestorCalls  A {@code boolean} defining whether ancestor conditions
     *                                      carrying exactly the score and p-value of one of their
     *                                      descendants should be discarded from the result
     *                                      (see {@code ExpressionCallFilter2
     *                                      #isRedundantAncestorCallsFilter()}). Redundant
     *                                      conditions are always identified, only their removal
     *                                      is conditioned by this argument.
     */
    //Package-private rather than private to allow unit testing of the propagation
    //over synthetic condition graphs (see ExpressionCallLoaderPropagationTest).
    Map<Gene, Set<OTFExpressionCall>> propagateCalls(
            Map<Integer, Map<Integer, Set<ObservedExpressionTO>>> geneToGlobalCondIdToRawExpressionCall,
            ConditionGraphCache condGraphCache, Set<Integer> filterConditionIds,
            boolean filterRedundantAncestorCalls) {
        log.traceEntry("{}, {}, {}, {}", geneToGlobalCondIdToRawExpressionCall, condGraphCache,
                filterConditionIds, filterRedundantAncestorCalls);

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
                    // Guard against re-processing, which would indicate a cycle.
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
                    //Every condition and gene reached by the propagation must have been
                    //identified by the processed filter: the conditions queried and the ancestors
                    //the propagation may reach are both bounded by conditionMap. A missing one
                    //would silently produce a call without condition or without gene, so we fail
                    //instead.
                    Condition2 propagatedCond = conditionMap.get(condId);
                    if (propagatedCond == null) {
                        throw log.throwing(new IllegalStateException("No Condition2 for the global "
                                + "condition ID " + condId + " reached by the propagation"));
                    }
                    Gene propagatedGene = geneMap.get(geneId);
                    if (propagatedGene == null) {
                        throw log.throwing(new IllegalStateException("No Gene for the Bgee gene ID "
                                + geneId + " reached by the propagation"));
                    }
                    OTFExpressionCall expressionCall = generateOTFExpressionCall(
                            propagatedGene, propagatedCond,
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
                if (filterRedundantAncestorCalls) {
                    log.debug("Pruning {} redundant ancestor condition(s) for gene {} "
                            + "(same score and p-value as a descendant)", redundantCondIds.size(), geneId);
                    globalCondIdToExpressionCall.keySet().removeAll(redundantCondIds);
                } else {
                    log.debug("Keeping {} redundant ancestor condition(s) for gene {}, "
                            + "the filtering of redundant ancestor calls was not requested",
                            redundantCondIds.size(), geneId);
                }
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
        //The p-value of a call is the weighted mean of the p-values of the observations of the
        //condition itself and of all its sub-conditions, each weighted by the weight of the
        //evidence backing it. It is accumulated the same way as the score below: a sum of the
        //values multiplied by their weight, and the sum of those weights. Weighted means compose:
        //a descendant call exposes the weighted mean of its own sub-tree and the total weight of
        //that sub-tree, so multiplying one by the other gives back its weighted sum, and the
        //observations of a sub-tree weigh exactly what they represent.
        //The mean is turned into a valid p-value when it is read, by OTFExpressionCall, not here:
        //see OTFExpressionCall#calibrate(BigDecimal).
        BigDecimal pValueByWeightSum = BigDecimal.ZERO;
        BigDecimal pValueWeightSum = BigDecimal.ZERO;
        //Only the data types trusted for absent calls feed these sums (see
        //OTFExpressionCallFilterEngine#inferSummaryCallTypeAndQuality).
        BigDecimal trustedPValueByWeightSum = BigDecimal.ZERO;
        BigDecimal trustedPValueWeightSum = BigDecimal.ZERO;
        //The number of observations aggregated, needed to know whether the mean must be
        //calibrated: a single observation is already a p-value.
        int observationCount = 0;
        //The weight of each data type is the total weight of the score it carries (for instance,
        //for RNA-Seq, the sum of the distinct rank counts of all the samples of that gene in that
        //condition), not a per-observation weight: it must therefore not be multiplied by the
        //number of observations here, which would count the number of observations twice. This is
        //also how the weight of the descendant calls is used below.
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
                pValueByWeightSum = pValueByWeightSum.add(
                        obsExpression.getBulkPValue().multiply(obsExpression.getBulkWeight()));
                pValueWeightSum = pValueWeightSum.add(obsExpression.getBulkWeight());
                observationCount += obsExpression.getBulkNumberObs();
                if (DataType.RNA_SEQ.isTrustedForAbsentCalls()) {
                    trustedPValueByWeightSum = trustedPValueByWeightSum.add(
                            obsExpression.getBulkPValue().multiply(obsExpression.getBulkWeight()));
                    trustedPValueWeightSum = trustedPValueWeightSum.add(obsExpression.getBulkWeight());
                }
                scoreByWeightSum = scoreByWeightSum
                        .add((obsExpression.getBulkScore()
                                .multiply(obsExpression.getBulkWeight())));
                weightSum = weightSum.
                        add(obsExpression.getBulkWeight());
                supportingDataTypes.add(DataType.RNA_SEQ);
            }
            if (obsExpression.getInSituNumberObs() != null && obsExpression.getInSituNumberObs() != 0) {
                pValueByWeightSum = pValueByWeightSum.add(
                        obsExpression.getInSituPValue().multiply(obsExpression.getInSituWeight()));
                pValueWeightSum = pValueWeightSum.add(obsExpression.getInSituWeight());
                observationCount += obsExpression.getInSituNumberObs();
                if (DataType.IN_SITU.isTrustedForAbsentCalls()) {
                    trustedPValueByWeightSum = trustedPValueByWeightSum.add(
                            obsExpression.getInSituPValue().multiply(obsExpression.getInSituWeight()));
                    trustedPValueWeightSum = trustedPValueWeightSum.add(obsExpression.getInSituWeight());
                }
                scoreByWeightSum = scoreByWeightSum
                        .add((obsExpression.getInSituScore()
                                .multiply(obsExpression.getInSituWeight())));
                weightSum = weightSum.
                        add(obsExpression.getInSituWeight());
                supportingDataTypes.add(DataType.IN_SITU);
            }
            if (obsExpression.getFullLengthNumberObs() != null && obsExpression.getFullLengthNumberObs() != 0) {
                pValueByWeightSum = pValueByWeightSum.add(
                        obsExpression.getFullLengthPValue().multiply(obsExpression.getFullLengthWeight()));
                pValueWeightSum = pValueWeightSum.add(obsExpression.getFullLengthWeight());
                observationCount += obsExpression.getFullLengthNumberObs();
                if (DataType.SC_RNA_SEQ.isTrustedForAbsentCalls()) {
                    trustedPValueByWeightSum = trustedPValueByWeightSum.add(
                            obsExpression.getFullLengthPValue().multiply(obsExpression.getFullLengthWeight()));
                    trustedPValueWeightSum = trustedPValueWeightSum.add(obsExpression.getFullLengthWeight());
                }
                scoreByWeightSum = scoreByWeightSum
                        .add((obsExpression.getFullLengthScore()
                                .multiply(obsExpression.getFullLengthWeight())));
                weightSum = weightSum.
                        add(obsExpression.getFullLengthWeight());
                supportingDataTypes.add(DataType.SC_RNA_SEQ);
            }
            if (obsExpression.getDropletNumberObs() != null && obsExpression.getDropletNumberObs() != 0) {
                pValueByWeightSum = pValueByWeightSum.add(
                        obsExpression.getDropletPValue().multiply(obsExpression.getDropletWeight()));
                pValueWeightSum = pValueWeightSum.add(obsExpression.getDropletWeight());
                observationCount += obsExpression.getDropletNumberObs();
                if (DataType.SC_RNA_SEQ.isTrustedForAbsentCalls()) {
                    trustedPValueByWeightSum = trustedPValueByWeightSum.add(
                            obsExpression.getDropletPValue().multiply(obsExpression.getDropletWeight()));
                    trustedPValueWeightSum = trustedPValueWeightSum.add(obsExpression.getDropletWeight());
                }
                scoreByWeightSum = scoreByWeightSum
                        .add((obsExpression.getDropletScore()
                                .multiply(obsExpression.getDropletWeight())));
                weightSum = weightSum.
                        add(obsExpression.getDropletWeight());
                supportingDataTypes.add(DataType.SC_RNA_SEQ);
            }
        }

        BigDecimal bestDescendantAllDataTypePValue = null;
        BigDecimal bestDescendantTrustedDataTypePValue = null;
        BigDecimal bestDescendantExpressionScore = null;
        BigDecimal bestDescendantExpressionScoreWeight = null;
        if (!usedDescExprCalls.isEmpty()) {

            dataPropagation = dataPropagation == null? PropagationState.DESCENDANT: PropagationState.SELF_AND_DESCENDANT;
            for (OTFExpressionCall childCall: usedDescExprCalls) {
                supportingDataTypes.addAll(childCall.getSupportingDataTypes());
                //The raw mean and the weight of the child, not its calibrated p-value: the
                //calibration applies once, to the value finally read, and doubling at every
                //condition would compound the factor at each level of the graph.
                if (childCall.getAllDataTypePValueRawMean() != null) {
                    pValueByWeightSum = pValueByWeightSum.add(childCall.getAllDataTypePValueRawMean()
                            .multiply(childCall.getAllDataTypePValueWeight()));
                    pValueWeightSum = pValueWeightSum.add(childCall.getAllDataTypePValueWeight());
                }
                if (childCall.getTrustedDataTypePValueRawMean() != null) {
                    trustedPValueByWeightSum = trustedPValueByWeightSum.add(
                            childCall.getTrustedDataTypePValueRawMean()
                            .multiply(childCall.getTrustedDataTypePValueWeight()));
                    trustedPValueWeightSum = trustedPValueWeightSum.add(
                            childCall.getTrustedDataTypePValueWeight());
                }
                observationCount += childCall.getObservationCount();
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
        }
        BigDecimal ultimateAllDataTypePValue = weightedMean(pValueByWeightSum, pValueWeightSum);
        BigDecimal ultimateTrustedDataTypePValue = weightedMean(trustedPValueByWeightSum,
                trustedPValueWeightSum);
//        log.debug("weightSum: {}, scoreByWeightSum: {}", weightSum, scoreByWeightSum);
        if (BigDecimal.ZERO.compareTo(weightSum) == 0) {
            log.warn("weightSum is zero for gene {} in condition {} - all observation counts are null/0. Defaulting score to 0.", gene, cond);
        }
        BigDecimal weightedAverageExpressionScore = BigDecimal.ZERO.compareTo(weightSum) == 0 ?
                BigDecimal.ZERO :
                scoreByWeightSum.divide(weightSum, 2, RoundingMode.HALF_UP);

        OTFExpressionCall resultingCall = new OTFExpressionCall(gene, cond, supportingDataTypes,
              ultimateAllDataTypePValue, pValueWeightSum,
              ultimateTrustedDataTypePValue, trustedPValueWeightSum, observationCount,
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

    /**
     * @param valueByWeightSum  A {@code BigDecimal} that is the sum of the values multiplied
     *                          by their weight.
     * @param weightSum         A {@code BigDecimal} that is the sum of the weights.
     * @return                  A {@code BigDecimal} that is the weighted mean, or {@code null}
     *                          if {@code weightSum} is zero, meaning that nothing contributed.
     */
    protected BigDecimal weightedMean(BigDecimal valueByWeightSum, BigDecimal weightSum) {
        log.traceEntry("{}, {}", valueByWeightSum, weightSum);
        if (weightSum == null || BigDecimal.ZERO.compareTo(weightSum) == 0) {
            return log.traceExit((BigDecimal) null);
        }
        return log.traceExit(valueByWeightSum.divide(weightSum, MathContext.DECIMAL128));
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


    public ExpressionCallProcessedFilter getProcessedFilter() {
        return processedFilter;
    }





}
