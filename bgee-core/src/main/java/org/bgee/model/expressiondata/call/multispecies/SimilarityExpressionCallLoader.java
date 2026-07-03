package org.bgee.model.expressiondata.call.multispecies;

import java.util.ArrayList;
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
    private Long cachedDataCount;

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
        if (cachedDataCount != null) {
            return log.traceExit(cachedDataCount);
        }
        long count = 0;
        for (AnatEntitySimilarity sim : preparedFilter.getOrderedSimilarities()) {
            count += multiSpeciesCallService.loadSimilarityExpressionCallsForSimilarity(sim,
                    preparedFilter, fallbackAnatSimsById, fallbackCellSimsById).size();
        }
        count += multiSpeciesCallService.loadFallbackSimilarityExpressionCalls(preparedFilter,
                fallbackAnatSimsById, fallbackCellSimsById).size();
        cachedDataCount = count;
        return log.traceExit(count);
    }

    /**
     * @return  A {@code Stream} of all {@link SimilarityExpressionCall2}s matching this loader's filter.
     */
    public Stream<SimilarityExpressionCall2> stream() {
        log.traceEntry();
        long total = loadDataCount();
        if (total == 0) {
            return log.traceExit(Stream.empty());
        }
        Stream.Builder<SimilarityExpressionCall2> builder = Stream.builder();
        long offset = 0;
        while (offset < total) {
            int batchLimit = (int) Math.min(LIMIT_MAX, total - offset);
            loadData(offset, batchLimit).forEach(builder::add);
            offset += batchLimit;
        }
        return log.traceExit(builder.build());
    }
}
