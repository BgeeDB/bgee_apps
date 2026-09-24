package org.bgee.model.expressiondata.call;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
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
    /**
     * The {@code DataType}s indexed by their ordinal, to rebuild the data types supporting a call
     * from the bit mask they are aggregated into.
     */
    private final static DataType[] DATA_TYPES = DataType.values();



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

        int condCount = condGraphCache.getConditionCount();
        //Dense view of the filter, so that the propagation recognises the filter boundary with
        //an array read rather than with a lookup in a Set of boxed IDs. Left null when
        //the propagation is not restricted.
        boolean[] inFilter = null;
        if (!filterConditionIds.isEmpty()) {
            inFilter = new boolean[condCount];
            for (int filterCondId: filterConditionIds) {
                int filterIndex = condGraphCache.getIndex(filterCondId);
                if (filterIndex >= 0) {
                    inFilter[filterIndex] = true;
                }
            }
        }
        //Scratch buffers reused for every gene of this call: a gene only touches a small part
        //of them, and the stamps make the reset proportional to what was touched rather than
        //to the size of the graph.
        PropagationBuffers buffers = new PropagationBuffers(condCount);

        Map<Gene, Set<OTFExpressionCall>> geneToExpressionCall = new HashMap<>();

        // For each gene independently
        for (Map.Entry<Integer, Map<Integer, Set<ObservedExpressionTO>>> geneEntry :
                geneToGlobalCondIdToRawExpressionCall.entrySet()) {
            long startTimeGene = System.currentTimeMillis();

            //the Id of the gene for which we propagate calls
            Integer geneId = geneEntry.getKey();
            //Every gene reached by the propagation must have been identified by the processed
            //filter. A missing one would silently produce calls without gene, so we fail instead.
            Gene propagatedGene = geneMap.get(geneId);
            if (propagatedGene == null) {
                throw log.throwing(new IllegalStateException("No Gene for the Bgee gene ID "
                        + geneId + " reached by the propagation"));
            }

            buffers.startGene();
            //Aggregate the observations into every condition they contribute to, before walking
            //the graph: each observed condition contributes to each of its ancestors exactly
            //once, whatever the number of paths connecting them.
            for (Entry<Integer, Set<ObservedExpressionTO>> observedEntry:
                    geneEntry.getValue().entrySet()) {
                int observedIndex = condGraphCache.getIndex(observedEntry.getKey());
                if (observedIndex < 0) {
                    //A condition that is part of no relation is absent from the topological
                    //order: it has no ancestor and produces no call.
                    continue;
                }
                buffers.accumulateUpward(condGraphCache, inFilter, observedIndex,
                        ObservedCondAggregate.of(observedEntry.getValue()));
            }

            //The conditions touched by the accumulation are exactly the conditions to process:
            //the observed ones, and their ancestors within the filter. Because the index of
            //a condition is its position in the topological order, sorting their indexes walks
            //them children before parents, and no condition outside that set is ever visited.
            int[] toProcess = buffers.sortedTouchedIndexes();
            int toProcessCount = buffers.touchedCount;
            // Redundancy is only collected during the walk; the redundant calls are discarded
            // after it, so that every ancestor can still read a child's call while computing
            // its own. Transitive redundancy (A==B score/pval, B==C) is handled correctly:
            // B still has its call when C is processed, so C is also detected.
            int redundantCount = 0;
            for (int i = 0; i < toProcessCount; i++) {
                int index = toProcess[i];
                int condId = condGraphCache.getCondId(index);
                //Every condition reached by the propagation must have been identified by
                //the processed filter: the conditions queried and the ancestors the propagation
                //may reach are both bounded by conditionMap. A missing one would silently
                //produce a call without condition, so we fail instead.
                Condition2 propagatedCond = conditionMap.get(condId);
                if (propagatedCond == null) {
                    throw log.throwing(new IllegalStateException("No Condition2 for the global "
                            + "condition ID " + condId + " reached by the propagation"));
                }
                OTFExpressionCall expressionCall = generateOTFExpressionCall(propagatedGene,
                        propagatedCond, buffers, index, condGraphCache);
                buffers.calls[index] = expressionCall;

                // Check immediately whether the condition is redundant by comparing it to
                // the best descendant score/p-value already computed in expressionCall.
                if (expressionCall.getExpressionScore() != null
                        && expressionCall.getBestDirectDescendantExpressionScore() != null
                        && expressionCall.getExpressionScore().compareTo(
                                expressionCall.getBestDirectDescendantExpressionScore()) == 0) {
                    buffers.flags[index] |= PropagationBuffers.REDUNDANT;
                    redundantCount++;
                }
            }

            if (redundantCount > 0) {
                if (filterRedundantAncestorCalls) {
                    log.debug("Pruning {} redundant ancestor condition(s) for gene {} "
                            + "(same score and p-value as a descendant)", redundantCount, geneId);
                    for (int i = 0; i < toProcessCount; i++) {
                        int index = toProcess[i];
                        if ((buffers.flags[index] & PropagationBuffers.REDUNDANT) != 0) {
                            buffers.calls[index] = null;
                        }
                    }
                } else {
                    log.debug("Keeping {} redundant ancestor condition(s) for gene {}, "
                            + "the filtering of redundant ancestor calls was not requested",
                            redundantCount, geneId);
                }
            }

            Set<OTFExpressionCall> geneCalls = new HashSet<>();
            for (int i = 0; i < toProcessCount; i++) {
                OTFExpressionCall call = buffers.calls[toProcess[i]];
                if (call != null) {
                    geneCalls.add(call);
                }
            }
            geneToExpressionCall.put(propagatedGene, geneCalls);
            log.debug("Propagation for gene {} completed in {} ms, {} calls generated",
                    geneId, System.currentTimeMillis() - startTimeGene, geneCalls.size());
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

    /**
     * The observations of a gene in one condition, reduced once into the quantities the
     * propagation sums. The reduction is what allows the upward walk to contribute to
     * an ancestor with a handful of additions, rather than iterating the observations and
     * multiplying again for each of the ancestors reached.
     * <p>
     * Weights are stored as {@code bigint} and scores as {@code decimal(9,2)}, so summing
     * the weights as {@code long}, and the scores as their unscaled value at scale 2, is exact:
     * the quantities aggregated over thousands of conditions are the very same as the ones
     * a {@code BigDecimal} accumulation would produce. Only the p-values, stored as
     * {@code decimal(31,30)}, need {@code BigDecimal} to be summed without any loss.
     */
    private static final class ObservedCondAggregate {
        /**
         * @param observations  The observations made in one condition for one gene.
         * @return              An {@code ObservedCondAggregate} reducing them. Never {@code null}:
         *                      observations that all have a number of observations of 0 yield
         *                      an aggregate contributing nothing, so that the condition still
         *                      produces a call, as it did when the observations were aggregated
         *                      one condition at a time.
         */
        private static ObservedCondAggregate of(Collection<ObservedExpressionTO> observations) {
            BigDecimal pValueByWeightSum = BigDecimal.ZERO;
            BigDecimal trustedPValueByWeightSum = BigDecimal.ZERO;
            long pValueWeightSum = 0;
            long trustedPValueWeightSum = 0;
            long scoreByWeightSumUnscaled = 0;
            long weightSum = 0;
            int observationCount = 0;
            int dataTypeMask = 0;

            for (ObservedExpressionTO obs: observations) {
                for (int dataTypeIndex = 0; dataTypeIndex < 4; dataTypeIndex++) {
                    Integer numberObs = null;
                    BigDecimal pValue = null;
                    BigDecimal score = null;
                    BigDecimal weight = null;
                    DataType dataType = null;
                    switch (dataTypeIndex) {
                    case 0:
                        numberObs = obs.getBulkNumberObs();
                        pValue = obs.getBulkPValue();
                        score = obs.getBulkScore();
                        weight = obs.getBulkWeight();
                        dataType = DataType.RNA_SEQ;
                        break;
                    case 1:
                        numberObs = obs.getInSituNumberObs();
                        pValue = obs.getInSituPValue();
                        score = obs.getInSituScore();
                        weight = obs.getInSituWeight();
                        dataType = DataType.IN_SITU;
                        break;
                    case 2:
                        numberObs = obs.getFullLengthNumberObs();
                        pValue = obs.getFullLengthPValue();
                        score = obs.getFullLengthScore();
                        weight = obs.getFullLengthWeight();
                        dataType = DataType.SC_RNA_SEQ;
                        break;
                    default:
                        numberObs = obs.getDropletNumberObs();
                        pValue = obs.getDropletPValue();
                        score = obs.getDropletScore();
                        weight = obs.getDropletWeight();
                        dataType = DataType.SC_RNA_SEQ;
                        break;
                    }
                    if (numberObs == null || numberObs == 0) {
                        continue;
                    }
                    //Exact: the weight is an integer, and the product of a score of scale 2
                    //by an integer is of scale 2. An input that would not fit these assumptions
                    //makes longValueExact() or setScale() throw, rather than silently changing
                    //the aggregated values.
                    long weightValue = weight.longValueExact();
                    long scoreByWeightUnscaled = Math.multiplyExact(
                            score.setScale(2, RoundingMode.UNNECESSARY).unscaledValue()
                                .longValueExact(),
                            weightValue);
                    BigDecimal pValueByWeight = pValue.multiply(weight);

                    pValueByWeightSum = pValueByWeightSum.add(pValueByWeight);
                    pValueWeightSum = Math.addExact(pValueWeightSum, weightValue);
                    //Only the data types trusted for absent calls feed the trusted p-value:
                    //an absent call cannot be better than BRONZE when it is not supported
                    //by any of them.
                    if (dataType.isTrustedForAbsentCalls()) {
                        trustedPValueByWeightSum = trustedPValueByWeightSum.add(pValueByWeight);
                        trustedPValueWeightSum = Math.addExact(trustedPValueWeightSum, weightValue);
                    }
                    scoreByWeightSumUnscaled = Math.addExact(scoreByWeightSumUnscaled,
                            scoreByWeightUnscaled);
                    weightSum = Math.addExact(weightSum, weightValue);
                    observationCount += numberObs;
                    dataTypeMask |= 1 << dataType.ordinal();
                }
            }
            return new ObservedCondAggregate(pValueByWeightSum, trustedPValueByWeightSum,
                    pValueWeightSum, trustedPValueWeightSum, scoreByWeightSumUnscaled, weightSum,
                    observationCount, dataTypeMask);
        }

        private final BigDecimal pValueByWeightSum;
        private final BigDecimal trustedPValueByWeightSum;
        private final long pValueWeightSum;
        private final long trustedPValueWeightSum;
        private final long scoreByWeightSumUnscaled;
        private final long weightSum;
        private final int observationCount;
        private final int dataTypeMask;

        private ObservedCondAggregate(BigDecimal pValueByWeightSum,
                BigDecimal trustedPValueByWeightSum, long pValueWeightSum,
                long trustedPValueWeightSum, long scoreByWeightSumUnscaled, long weightSum,
                int observationCount, int dataTypeMask) {
            this.pValueByWeightSum = pValueByWeightSum;
            this.trustedPValueByWeightSum = trustedPValueByWeightSum;
            this.pValueWeightSum = pValueWeightSum;
            this.trustedPValueWeightSum = trustedPValueWeightSum;
            this.scoreByWeightSumUnscaled = scoreByWeightSumUnscaled;
            this.weightSum = weightSum;
            this.observationCount = observationCount;
            this.dataTypeMask = dataTypeMask;
        }
    }

    /**
     * The quantities aggregated over each condition and all its sub-conditions, from which
     * the calls of one gene are produced.
     * <p>
     * The condition graph is a DAG, not a tree: a same condition can be reached from an ancestor
     * through several distinct paths. Aggregating by summing the aggregates of the direct
     * children would then count such a condition once per path. {@link #accumulateUpward(
     * ConditionGraphCache, boolean[], int, ObservedCondAggregate)} walks up from each observed
     * condition instead, and contributes to each of its ancestors exactly once.
     * <p>
     * The quantities are held in arrays indexed by condition index (see
     * {@code ConditionGraphCache#getIndex(int)}), and not in a {@code Map} keyed by condition ID:
     * contributing to an ancestor then costs a few additions into arrays, with no boxing,
     * no hash lookup and no allocation. This matters because the propagation of a single gene
     * contributes to the order of a million condition-ancestor pairs.
     * <p>
     * The arrays are sized for the whole graph but only the conditions touched by the current
     * gene are ever read or written, so a same instance is reused for all the genes propagated
     * together: {@link #startGene()} only has to invalidate the previous stamp.
     */
    private static final class PropagationBuffers {
        private static final byte SELF_OBSERVATION = 1;
        private static final byte DESCENDANT_OBSERVATION = 2;
        private static final byte REDUNDANT = 4;

        private final BigDecimal[] pValueByWeightSum;
        private final BigDecimal[] trustedPValueByWeightSum;
        private final long[] pValueWeightSum;
        private final long[] trustedPValueWeightSum;
        private final long[] scoreByWeightSumUnscaled;
        private final long[] weightSum;
        private final int[] observationCount;
        private final int[] dataTypeMask;
        private final byte[] flags;
        private final OTFExpressionCall[] calls;

        /**
         * The indexes of the conditions touched by the gene being propagated. Sorted in place
         * by {@link #sortedTouchedIndexes()} once the accumulation is over.
         */
        private final int[] touched;
        private int touchedCount;
        /**
         * {@link #geneStamp} holds, for each condition, the stamp of the last gene that touched
         * it. Comparing it to {@link #currentGeneStamp} tells whether the values held for
         * that condition belong to the gene being propagated, which makes starting a new gene
         * a single increment instead of a reset of the whole arrays.
         */
        private final int[] geneStamp;
        private int currentGeneStamp;
        /**
         * Same mechanism as {@link #geneStamp}, to visit an ancestor exactly once while walking
         * up from one observed condition.
         */
        private final int[] visitStamp;
        private int currentVisitStamp;
        /**
         * The stack of the upward walk. Sized for the whole graph, which no walk can exceed
         * since a condition is pushed at most once.
         */
        private final int[] stack;

        private PropagationBuffers(int condCount) {
            this.pValueByWeightSum = new BigDecimal[condCount];
            this.trustedPValueByWeightSum = new BigDecimal[condCount];
            this.pValueWeightSum = new long[condCount];
            this.trustedPValueWeightSum = new long[condCount];
            this.scoreByWeightSumUnscaled = new long[condCount];
            this.weightSum = new long[condCount];
            this.observationCount = new int[condCount];
            this.dataTypeMask = new int[condCount];
            this.flags = new byte[condCount];
            this.calls = new OTFExpressionCall[condCount];
            this.touched = new int[condCount];
            this.geneStamp = new int[condCount];
            this.visitStamp = new int[condCount];
            this.stack = new int[condCount];
        }

        /**
         * Start propagating a new gene. The values held for the previous gene are not erased:
         * they are invalidated by the new stamp, and overwritten when a condition is touched
         * again (see {@link #touch(int)}).
         */
        private void startGene() {
            this.touchedCount = 0;
            this.currentGeneStamp++;
        }
        /**
         * @param index An {@code int} that is the index of a condition.
         * @return      {@code true} if the values held for that condition belong to the gene
         *              being propagated.
         */
        private boolean isTouched(int index) {
            return this.geneStamp[index] == this.currentGeneStamp;
        }
        /**
         * Make the values held for a condition belong to the gene being propagated, resetting
         * them if they do not yet.
         */
        private void touch(int index) {
            if (this.isTouched(index)) {
                return;
            }
            this.geneStamp[index] = this.currentGeneStamp;
            this.touched[this.touchedCount++] = index;
            this.pValueByWeightSum[index] = BigDecimal.ZERO;
            this.trustedPValueByWeightSum[index] = BigDecimal.ZERO;
            this.pValueWeightSum[index] = 0;
            this.trustedPValueWeightSum[index] = 0;
            this.scoreByWeightSumUnscaled[index] = 0;
            this.weightSum[index] = 0;
            this.observationCount[index] = 0;
            this.dataTypeMask[index] = 0;
            this.flags[index] = 0;
            this.calls[index] = null;
        }

        /**
         * Contribute the observations made in one condition to that condition and to all
         * its ancestors within the filter, each of them exactly once.
         *
         * @param graph             The {@code ConditionGraphCache} to walk up.
         * @param inFilter          A {@code boolean[]} telling, for each condition index, whether
         *                          the propagation is allowed to reach it, {@code null} when
         *                          it is not restricted.
         * @param observedIndex     An {@code int} that is the index of the condition
         *                          the observations were made in.
         * @param aggregate         The {@code ObservedCondAggregate} of those observations.
         */
        private void accumulateUpward(ConditionGraphCache graph, boolean[] inFilter,
                int observedIndex, ObservedCondAggregate aggregate) {
            this.currentVisitStamp++;
            this.visitStamp[observedIndex] = this.currentVisitStamp;
            this.contribute(observedIndex, aggregate, true);

            int stackSize = 0;
            this.stack[stackSize++] = observedIndex;
            while (stackSize > 0) {
                int index = this.stack[--stackSize];
                for (int parentIndex: graph.getDirectAncestorIndexes(index)) {
                    //Propagation stops at the filter boundary: an ancestor outside the filter
                    //(e.g. "nervous system" when brain was queried) is not reached, and neither
                    //is anything only reachable through it.
                    if (inFilter != null && !inFilter[parentIndex]) {
                        continue;
                    }
                    //Already contributed to through another path
                    if (this.visitStamp[parentIndex] == this.currentVisitStamp) {
                        continue;
                    }
                    this.visitStamp[parentIndex] = this.currentVisitStamp;
                    this.contribute(parentIndex, aggregate, false);
                    this.stack[stackSize++] = parentIndex;
                }
            }
        }
        /**
         * @param self  Whether the condition being contributed to is the one the observations
         *              were made in, or one of its ancestors.
         */
        private void contribute(int index, ObservedCondAggregate aggregate, boolean self) {
            this.touch(index);
            this.flags[index] |= self? SELF_OBSERVATION: DESCENDANT_OBSERVATION;
            if (aggregate.dataTypeMask == 0) {
                //Nothing was observed, only the propagation state is contributed
                return;
            }
            this.pValueByWeightSum[index] = this.pValueByWeightSum[index]
                    .add(aggregate.pValueByWeightSum);
            this.trustedPValueByWeightSum[index] = this.trustedPValueByWeightSum[index]
                    .add(aggregate.trustedPValueByWeightSum);
            this.pValueWeightSum[index] = Math.addExact(this.pValueWeightSum[index],
                    aggregate.pValueWeightSum);
            this.trustedPValueWeightSum[index] = Math.addExact(this.trustedPValueWeightSum[index],
                    aggregate.trustedPValueWeightSum);
            this.scoreByWeightSumUnscaled[index] = Math.addExact(
                    this.scoreByWeightSumUnscaled[index], aggregate.scoreByWeightSumUnscaled);
            this.weightSum[index] = Math.addExact(this.weightSum[index], aggregate.weightSum);
            this.observationCount[index] += aggregate.observationCount;
            this.dataTypeMask[index] |= aggregate.dataTypeMask;
        }

        /**
         * @return  The {@code int[]} of the indexes touched by the gene being propagated, sorted
         *          in ascending order, which is the topological order, children before parents.
         *          Only its {@link #touchedCount} first elements are meaningful.
         */
        private int[] sortedTouchedIndexes() {
            Arrays.sort(this.touched, 0, this.touchedCount);
            return this.touched;
        }
        /**
         * @return  The {@code OTFExpressionCall} already generated for a condition of the gene
         *          being propagated, {@code null} if it has none. Values left by a previously
         *          propagated gene are never returned.
         */
        private OTFExpressionCall getCall(int index) {
            return this.isTouched(index)? this.calls[index]: null;
        }
    }

    /**
     * Generate the call of one condition, from the quantities already aggregated over that
     * condition and all its sub-conditions, and from the calls of its direct sub-conditions.
     * The aggregated quantities are not recomputed from the calls of the sub-conditions, which
     * would count a sub-condition reachable through several paths once per path. The calls of
     * the direct sub-conditions are only used for the "best descendant" values, which are minima
     * and maxima, and are therefore insensitive to a condition being seen several times.
     */
    private OTFExpressionCall generateOTFExpressionCall(Gene gene, Condition2 cond,
            PropagationBuffers buf, int index, ConditionGraphCache graph) {
        log.traceEntry("{}, {}, {}, {}, {}", gene, cond, buf, index, graph);

        if (!buf.isTouched(index)) {
            throw log.throwing(new IllegalArgumentException(
                    "No observation aggregated for the condition " + cond));
        }

        byte flags = buf.flags[index];
        PropagationState dataPropagation = (flags & PropagationBuffers.SELF_OBSERVATION) != 0?
                ((flags & PropagationBuffers.DESCENDANT_OBSERVATION) != 0?
                        PropagationState.SELF_AND_DESCENDANT: PropagationState.SELF):
                PropagationState.DESCENDANT;

        BigDecimal bestDescendantAllDataTypePValue = null;
        BigDecimal bestDescendantTrustedDataTypePValue = null;
        BigDecimal bestDescendantExpressionScore = null;
        BigDecimal bestDescendantExpressionScoreWeight = null;
        for (int childIndex: graph.getDirectDescendantIndexes(index)) {
            //A child always comes first in the topological order, and the conditions are
            //processed in that order: a child with a call has necessarily been processed already.
            if (childIndex >= index) {
                throw log.throwing(new IllegalStateException("Condition index " + childIndex
                        + " is not before its parent " + index + " — cycle or propagation bug"));
            }
            OTFExpressionCall childCall = buf.getCall(childIndex);
            if (childCall == null) {
                continue;
            }
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

        long weightSum = buf.weightSum[index];
        BigDecimal allDataTypePValueWeight = BigDecimal.valueOf(buf.pValueWeightSum[index]);
        BigDecimal trustedDataTypePValueWeight = BigDecimal.valueOf(buf.trustedPValueWeightSum[index]);
        BigDecimal ultimateAllDataTypePValue = weightedMean(buf.pValueByWeightSum[index],
                allDataTypePValueWeight);
        BigDecimal ultimateTrustedDataTypePValue = weightedMean(buf.trustedPValueByWeightSum[index],
                trustedDataTypePValueWeight);
        if (weightSum == 0) {
            log.warn("weightSum is zero for gene {} in condition {} - all observation counts are null/0. Defaulting score to 0.", gene, cond);
        }
        BigDecimal weightedAverageExpressionScore = weightSum == 0? BigDecimal.ZERO:
                //The sum of the scores multiplied by their weight is held as its unscaled value
                //at scale 2, the scale the scores are stored with.
                BigDecimal.valueOf(buf.scoreByWeightSumUnscaled[index], 2)
                    .divide(BigDecimal.valueOf(weightSum), 2, RoundingMode.HALF_UP);

        EnumSet<DataType> supportingDataTypes = EnumSet.noneOf(DataType.class);
        for (int mask = buf.dataTypeMask[index], ordinal = 0; mask != 0; mask >>>= 1, ordinal++) {
            if ((mask & 1) != 0) {
                supportingDataTypes.add(DATA_TYPES[ordinal]);
            }
        }

        OTFExpressionCall resultingCall = new OTFExpressionCall(gene, cond,
              supportingDataTypes,
              ultimateAllDataTypePValue, allDataTypePValueWeight,
              ultimateTrustedDataTypePValue, trustedDataTypePValueWeight,
              buf.observationCount[index],
              bestDescendantAllDataTypePValue, bestDescendantTrustedDataTypePValue,
              BigDecimal.valueOf(weightSum), weightedAverageExpressionScore,
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



    public ExpressionCallProcessedFilter getProcessedFilter() {
        return processedFilter;
    }





}
