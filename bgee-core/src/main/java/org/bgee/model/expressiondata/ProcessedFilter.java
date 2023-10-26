package org.bgee.model.expressiondata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.DAODataFilter2;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.source.Source;
import org.bgee.model.species.Species;

/**
 * Class holding pre-processed information generated from a {@code DataFilter},
 * in order not to re-process this information when obtaining different {@code Loader}s
 * for the same parameters.
 * <p>
 * This class has been created to store this information outside of a {@code Loader},
 * because a {@code Loader} is a {@code Service}, and holds a connection to a data source.
 * If we wanted to store this pre-processed information to be reused by different threads,
 * storing it in a {@code Loader} could maintain the connection open.
 *
 * @author Frederic Bastian
 * @version Bgee 15.0, Dec. 2022
 * @since Bgee 15.0, Dec. 2022
 * @see #getSourceFilter()
 */
public abstract class ProcessedFilter<T extends DataFilter<?>, U extends DAODataFilter2,
//TODO: reestablish V extends BaseCondition<V> when code will be refactored
V> {
    private final static Logger log = LogManager.getLogger(ProcessedFilter.class.getName());

    private final T sourceFilter;
    private final Set<U> daoFilters;

    private final Map<Integer, Gene> requestedGeneMap;
    private final Map<Integer, V> requestedConditionMap;
    private final Map<Integer, Species> speciesMap;
    private final Map<Integer, GeneBioType> geneBioTypeMap;
    private final Map<Integer, Source> sourceMap;

    protected ProcessedFilter(T sourceFilter, Collection<U> daoFilters,
            Map<Integer, Gene> requestedGeneMap, Map<Integer, V> requestedConditionMap,
            Map<Integer, Species> speciesMap, Map<Integer, GeneBioType> geneBioTypeMap,
            Map<Integer, Source> sourceMap) {
        log.traceEntry("{}, {}, {}, {}, {}, {}, {}", sourceFilter, daoFilters,
                requestedGeneMap, requestedConditionMap, speciesMap, geneBioTypeMap,
                sourceMap);

        this.sourceFilter = sourceFilter;

        //When daoFilters are null, it means that there was no matching conditions
        //for the parameters, and that there will be no results
        this.daoFilters = daoFilters == null? null:
            Collections.unmodifiableSet(new HashSet<>(daoFilters));

        this.requestedGeneMap = Collections.unmodifiableMap(requestedGeneMap == null?
                new HashMap<>(): new HashMap<>(requestedGeneMap));
        if (this.requestedGeneMap.entrySet().stream()
                .anyMatch(e -> e.getKey() == null || e.getKey() < 1 || e.getValue() == null)) {
            throw log.throwing(new IllegalArgumentException("Invalid gene Map: "
                    + this.requestedGeneMap));
        }
        this.requestedConditionMap = Collections.unmodifiableMap(
                requestedConditionMap == null? new HashMap<>():
                    new HashMap<>(requestedConditionMap));
        if (this.requestedConditionMap.entrySet().stream()
                .anyMatch(e -> e.getKey() == null || e.getKey() < 1 || e.getValue() == null)) {
            throw log.throwing(new IllegalArgumentException("Invalid raw data condition Map: "
                    + this.requestedConditionMap));
        }
        if (speciesMap == null || speciesMap.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "All Species that can potentially be queried must be provided"));
        }
        this.speciesMap = Collections.unmodifiableMap(new HashMap<>(speciesMap));
        if (this.speciesMap.entrySet().stream()
                .anyMatch(e -> e.getKey() == null || e.getKey() < 1 || e.getValue() == null)) {
            throw log.throwing(new IllegalArgumentException("Invalid species Map: "
                    + this.speciesMap));
        }
        if (geneBioTypeMap == null || geneBioTypeMap.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Gene biotypes must be provided"));
        }
        this.geneBioTypeMap = Collections.unmodifiableMap(new HashMap<>(geneBioTypeMap));
        if (this.geneBioTypeMap.entrySet().stream()
                .anyMatch(e -> e.getKey() == null || e.getKey() < 1 || e.getValue() == null)) {
            throw log.throwing(new IllegalArgumentException("Invalid gene biotype Map: "
                    + this.geneBioTypeMap));
        }
        if (sourceMap == null || sourceMap.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Sources must be provided"));
        }
        this.sourceMap = Collections.unmodifiableMap(new HashMap<>(sourceMap));
        if (this.sourceMap.entrySet().stream()
                .anyMatch(e -> e.getKey() == null || e.getKey() < 1 || e.getValue() == null)) {
            throw log.throwing(new IllegalArgumentException("Invalid source Map: "
                    + this.sourceMap));
        }
        log.traceExit();
    }

    /**
     * @return  The {@code T} filter originally used to create this {@code ProcessedFilter}.
     *          Can be {@code null} (equivalent to requesting all results).
     */
    public T getSourceFilter() {
        return this.sourceFilter;
    }
    /**
     * @return  A {@code Set} of {@code U}s DAO filters that allow to configure
     *          the queries to DAOs, that were generated by the {@code Service}
     *          based on a provided {@code T} filter. Several {@code U}s
     *          will be treated as "OR" conditions. If {@code null}, it means that
     *          no conditions were matching the parameters, thus there will be no result
     *          and no query done.
     */
    protected Set<U> getDaoFilters() {
        return daoFilters;
    }
    /**
     * @return  A {@code Map} where keys are {@code Integer}s corresponding to
     *          Bgee internal gene IDs, the associated value being the corresponding
     *          {@code Gene}, that were requested to retrieve raw data. Only genes
     *          that were explicitly requested are present in this {@code Map},
     *          for easier instantiation of the objects returned by this class.
     *          If all genes of a species were requested, they are not present
     *          in this {@code Map} and they should be retrieved as needed.
     */
    protected Map<Integer, Gene> getRequestedGeneMap() {
        return requestedGeneMap;
    }
    /**
     * @return  A {@code Map} where keys are {@code Integer}s corresponding to Bgee internal
     *          condition IDs, the associated value being the corresponding
     *          {@code V} condition, that were requested to retrieve data.
     *          Only conditions that were explicitly requested are present
     *          in this {@code Map}, for easier instantiation of the objects returned by this class.
     *          If all conditions of a species were requested, they are not present
     *          in this {@code Map} and they should be retrieved as needed.
     */
    protected Map<Integer, V> getRequestedConditionMap() {
        return requestedConditionMap;
    }
    /**
     * @return  A {@code Map} where keys are species IDs, the associated value being
     *          the corresponding {@code Species}. It is stored in order to more
     *          efficiently instantiate objects returned by this class. Only {@code Species}
     *          that can potentially be queried are stored in this {@code Map}.
     */
    protected Map<Integer, Species> getSpeciesMap() {
        return speciesMap;
    }
    /**
     * @return  A {@code Map} where keys are gene biotype IDs, the associated value being
     *          the corresponding {@code GeneBioType}. It is stored in order to
     *          more efficiently create new {@code Gene}s.
     */
    protected Map<Integer, GeneBioType> getGeneBioTypeMap() {
        return geneBioTypeMap;
    }
    /**
     * @return  A {@code Map} where keys are source IDs, the associated value being
     *          the corresponding {@code Source}. It is stored for easier instantiation
     *          of the objects returned by this class.
     */
    protected Map<Integer, Source> getSourceMap() {
        return sourceMap;
    }

    @Override
    public int hashCode() {
        return Objects.hash(daoFilters, geneBioTypeMap, requestedConditionMap,
                requestedGeneMap, sourceFilter, sourceMap, speciesMap);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProcessedFilter<?, ?, ?> other = (ProcessedFilter<?, ?, ?>) obj;
        return Objects.equals(daoFilters, other.daoFilters)
                && Objects.equals(geneBioTypeMap, other.geneBioTypeMap)
                && Objects.equals(requestedConditionMap, other.requestedConditionMap)
                && Objects.equals(requestedGeneMap, other.requestedGeneMap)
                && Objects.equals(sourceFilter, other.sourceFilter)
                && Objects.equals(sourceMap, other.sourceMap)
                && Objects.equals(speciesMap, other.speciesMap);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ProcessedFilter [")
               .append("sourceFilter=").append(sourceFilter)
               .append(", daoFilters=").append(daoFilters)
               .append(", requestedGeneMap=").append(requestedGeneMap)
               .append(", requestedConditionMap=").append(requestedConditionMap)
               .append(", speciesMap=").append(speciesMap)
               .append(", geneBioTypeMap=").append(geneBioTypeMap)
               .append(", sourceMap=").append(sourceMap)
               .append("]");
        return builder.toString();
    }
}
