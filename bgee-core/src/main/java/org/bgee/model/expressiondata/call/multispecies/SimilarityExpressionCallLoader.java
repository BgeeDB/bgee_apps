package org.bgee.model.expressiondata.call.multispecies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.CommonService;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.multispemapping.AnatEntitySimilarity;

/**
 * Loads {@link SimilarityExpressionCall2}s with pagination, driven by
 * {@link AnatEntitySimilarity} iteration rather than loading all expression calls
 * into memory at once.
 *
 * @author  Harald Detering
 * @version Bgee 16, Jul. 2026
 * @since   Bgee 16, Jul. 2026
 * @see     SimilarityExpressionCallFilter
 * @see     MultiSpeciesCallService#loadSimilarityCallLoader(SimilarityExpressionCallFilter)
 */
public class SimilarityExpressionCallLoader extends CommonService {

    private static final Logger log = LogManager.getLogger(SimilarityExpressionCallLoader.class.getName());

    /**
     * An {@code int} that is the maximum allowed number of results
     * to retrieve in one method call.
     * Value: 10,000.
     */
    public static int LIMIT_MAX = 10000;

    private final SimilarityExpressionCallPreparedFilter preparedFilter;
    private final MultiSpeciesCallService multiSpeciesCallService;
    private final Map<String, AnatEntitySimilarity> fallbackAnatSimsById = new HashMap<>();
    private final Map<String, AnatEntitySimilarity> fallbackCellSimsById = new HashMap<>();
    /**
     * The complete, ordered list of all {@link SimilarityExpressionCall2}s matching this loader's
     * filter, built lazily by {@link #getAllCalls()}. It is {@code null} until a full pass is
     * required (by {@link #loadDataCount()} or {@link #stream()}). Once populated, {@link #loadData(Long,
     * Integer)} serves pages directly from it instead of re-scanning the data source.
     */
    private List<SimilarityExpressionCall2> allCalls;

    SimilarityExpressionCallLoader(SimilarityExpressionCallPreparedFilter preparedFilter,
            ServiceFactory serviceFactory, MultiSpeciesCallService multiSpeciesCallService) {
        super(serviceFactory);
        if (preparedFilter == null) {
            throw log.throwing(new IllegalArgumentException("A preparedFilter must be provided"));
        }
        if (multiSpeciesCallService == null) {
            throw log.throwing(new IllegalArgumentException("A multiSpeciesCallService must be provided"));
        }
        this.preparedFilter = preparedFilter;
        this.multiSpeciesCallService = multiSpeciesCallService;
    }

    /**
     * @return  The {@link SimilarityExpressionCallFilter} used to build this loader.
     */
    public SimilarityExpressionCallFilter getFilter() {
        return preparedFilter.getSourceFilter();
    }

    /**
     * Loads a page of {@link SimilarityExpressionCall2}s.
     * <p>
     * Results are ordered deterministically: {@code AnatEntitySimilarity} (stable sort),
     * then gene ({@link org.bgee.model.gene.Gene#COMPARATOR}), then
     * {@link MultiSpeciesCondition}.
     *
     * @param offset    A {@code Long} that is the number of similarity calls to skip.
     *                  If {@code null}, defaults to 0.
     * @param limit     An {@code Integer} that is the maximum number of similarity calls
     *                  to return. If {@code null}, defaults to {@link #LIMIT_MAX}.
     * @return          A {@code List} of {@code SimilarityExpressionCall2}s for the requested page.
     */
    public List<SimilarityExpressionCall2> loadData(Long offset, Integer limit) {
        log.traceEntry("{}, {}", offset, limit);

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

        long skip = offset == null ? 0L : offset;
        int take = limit == null ? LIMIT_MAX : limit;

        //If a full pass has already been performed (e.g. by loadDataCount()), serve the page
        //directly from the cached list rather than re-scanning the data source.
        if (allCalls != null) {
            List<SimilarityExpressionCall2> page = new ArrayList<>();
            for (long i = skip; i < skip + take && i < allCalls.size(); i++) {
                page.add(allCalls.get((int) i));
            }
            return log.traceExit(page);
        }

        //Otherwise iterate lazily and stop as soon as the requested page is filled,
        //so a results-only request does not have to build the entire result set.
        List<SimilarityExpressionCall2> page = new ArrayList<>();
        long seen = 0;

        outer: for (AnatEntitySimilarity sim : preparedFilter.getOrderedSimilarities()) {
            for (SimilarityExpressionCall2 call : multiSpeciesCallService
                    .loadSimilarityExpressionCallsForSimilarity(sim, preparedFilter,
                            fallbackAnatSimsById, fallbackCellSimsById)) {
                if (seen >= skip + take) {
                    break outer;
                }
                if (seen >= skip) {
                    page.add(call);
                }
                seen++;
            }
        }

        if (seen < skip + take) {
            for (SimilarityExpressionCall2 call : multiSpeciesCallService
                    .loadFallbackSimilarityExpressionCalls(preparedFilter,
                            fallbackAnatSimsById, fallbackCellSimsById)) {
                if (seen >= skip + take) {
                    break;
                }
                if (seen >= skip) {
                    page.add(call);
                }
                seen++;
            }
        }

        return log.traceExit(page);
    }

    /**
     * @return  The total number of {@link SimilarityExpressionCall2}s matching this loader's filter.
     */
    public long loadDataCount() {
        log.traceEntry();
        return log.traceExit((long) getAllCalls().size());
    }

    /**
     * @return  A {@code Stream} of all {@link SimilarityExpressionCall2}s matching this loader's filter.
     */
    public Stream<SimilarityExpressionCall2> stream() {
        log.traceEntry();
        return log.traceExit(getAllCalls().stream());
    }

    /**
     * Builds (once) and returns the complete ordered list of {@link SimilarityExpressionCall2}s
     * matching this loader's filter. The result is memoized in {@link #allCalls}, so subsequent
     * calls, as well as {@link #loadData(Long, Integer)}, reuse it without re-scanning the data
     * source. Determinism of the ordering matches {@link #loadData(Long, Integer)}: ordered
     * similarities first, then fallback calls.
     *
     * @return  An unmodifiable {@code List} of all matching {@code SimilarityExpressionCall2}s.
     */
    private List<SimilarityExpressionCall2> getAllCalls() {
        if (allCalls != null) {
            return allCalls;
        }
        List<SimilarityExpressionCall2> calls = new ArrayList<>();
        for (AnatEntitySimilarity sim : preparedFilter.getOrderedSimilarities()) {
            calls.addAll(multiSpeciesCallService.loadSimilarityExpressionCallsForSimilarity(sim,
                    preparedFilter, fallbackAnatSimsById, fallbackCellSimsById));
        }
        calls.addAll(multiSpeciesCallService.loadFallbackSimilarityExpressionCalls(preparedFilter,
                fallbackAnatSimsById, fallbackCellSimsById));
        allCalls = Collections.unmodifiableList(calls);
        return allCalls;
    }
}
