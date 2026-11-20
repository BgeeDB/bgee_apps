package org.bgee.model.expressiondata.call.multispecies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.CommonService;
import org.bgee.model.ServiceFactory;

/**
 * Loads {@link SimilarityExpressionCall2}s with pagination. Expression calls are retrieved
 * once per species and mapped onto homology groups in memory.
 *
 * @author  Harald Detering
 * @version Bgee 16, Sep. 2026
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
    /**
     * The complete, ordered list of all {@link SimilarityExpressionCall2}s matching this loader's
     * filter, built lazily by {@link #getAllCalls()}. It is {@code null} until a full pass is
     * required. Once populated, {@link #loadData(Long, Integer)} serves pages directly from it.
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
        long startMs = System.currentTimeMillis();
        List<SimilarityExpressionCall2> calls = getAllCalls();
        List<SimilarityExpressionCall2> page = new ArrayList<>();
        for (long i = skip; i < skip + take && i < calls.size(); i++) {
            page.add(calls.get((int) i));
        }
        log.info("loadData: offset={} limit={} pageSize={} allCalls={} {} ms",
                skip, take, page.size(), calls.size(), System.currentTimeMillis() - startMs);
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
     * matching this loader's filter. The result is memoized in {@link #allCalls}.
     *
     * @return  An unmodifiable {@code List} of all matching {@code SimilarityExpressionCall2}s.
     */
    private List<SimilarityExpressionCall2> getAllCalls() {
        if (allCalls != null) {
            return allCalls;
        }
        long startMs = System.currentTimeMillis();
        allCalls = Collections.unmodifiableList(
                multiSpeciesCallService.loadOrderedSimilarityExpressionCalls(preparedFilter));
        log.info("getAllCalls: {} similarities, {} species, {} calls in {} ms",
                preparedFilter.getOrderedSimilarities().size(),
                preparedFilter.getOrderedGeneFilters().size(),
                allCalls.size(),
                System.currentTimeMillis() - startMs);
        return allCalls;
    }
}
