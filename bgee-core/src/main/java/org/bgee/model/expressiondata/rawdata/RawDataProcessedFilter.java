package org.bgee.model.expressiondata.rawdata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.source.Source;
import org.bgee.model.species.Species;

/**
 * Class holding pre-processed information generated from a {@link RawDataFilter},
 * in order not to re-process this information when obtaining different {@link RawDataLoader}s
 * for the same parameters.
 * <p>
 * This class has been created to store this information outside of a {@link RawDataLoader},
 * because a {@code RawDataLoader} is a {@code Service}, and holds a connection to a data source.
 * If we wanted to store this pre-processed information to be reused by different threads,
 * storing it in a {@code RawDataLoader} could maintain the connection open.
 *
 * @author Frederic Bastian
 * @version Bgee 15.0, Nov. 2022
 * @since Bgee 15.0, Nov. 2022
 * @see #getRawDataFilter()
 * @see RawDataLoader#getRawDataProcessedFilter()
 * @see RawDataService#getRawDataLoader(RawDataProcessedFilter)
 * @see RawDataService#loadRawDataLoader(RawDataFilter)
 */
//Most methods and constructors are package private, so that only the {@link RawDataService}
//can instantiate this class, and only {@link RawDataLoader} use it.
public class RawDataProcessedFilter {
    private final static Logger log = LogManager.getLogger(RawDataProcessedFilter.class.getName());

    /**
     * @see #getRawDataFilter()
     */
    private final RawDataFilter rawDataFilter;
    /**
     * @see #getDaoRawDataFilters()
     */
    private final Set<DAORawDataFilter> daoRawDataFilters;

    /**
     * @see #getRequestedGenesMap()
     */
    private final Map<Integer, Gene> requestedGeneMap;
    /**
     * @see #getRequestedRawDataConditionsMap()
     */
    private final Map<Integer, RawDataCondition> requestedRawDataConditionMap;
    /**
     * @see #getSpeciesMap()
     */
    private final Map<Integer, Species> speciesMap;
    /**
     * @see #getGeneBioTypeMap()
     */
    private final Map<Integer, GeneBioType> geneBioTypeMap;
    /**
     * @see #getSourceMap()
     */
    private final Map<Integer, Source> sourceMap;

    RawDataProcessedFilter(RawDataFilter rawDataFilter,
            Collection<DAORawDataFilter> daoFilters,
            Map<Integer, Gene> requestedGeneMap,
            Map<Integer, RawDataCondition> requestedRawDataConditionMap,
            Map<Integer, Species> speciesMap, Map<Integer, GeneBioType> geneBioTypeMap,
            Map<Integer, Source> sourceMap) {

        this.rawDataFilter = rawDataFilter;

        //When daoFilters are null, it means that there was no matching conditions
        //for the parameters, and that there will be no results
        this.daoRawDataFilters = daoFilters == null? null:
            Collections.unmodifiableSet(new HashSet<>(daoFilters));

        this.requestedGeneMap = Collections.unmodifiableMap(requestedGeneMap == null?
                new HashMap<>(): new HashMap<>(requestedGeneMap));
        if (this.requestedGeneMap.entrySet().stream()
                .anyMatch(e -> e.getKey() == null || e.getKey() < 1 || e.getValue() == null)) {
            throw log.throwing(new IllegalArgumentException("Invalid gene Map: "
                    + this.requestedGeneMap));
        }
        this.requestedRawDataConditionMap = Collections.unmodifiableMap(
                requestedRawDataConditionMap == null? new HashMap<>():
                    new HashMap<>(requestedRawDataConditionMap));
        if (this.requestedRawDataConditionMap.entrySet().stream()
                .anyMatch(e -> e.getKey() == null || e.getKey() < 1 || e.getValue() == null)) {
            throw log.throwing(new IllegalArgumentException("Invalid raw data condition Map: "
                    + this.requestedRawDataConditionMap));
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
    }

    /**
     * @return  The {@code RawDataFilter} originally used to create this {@code RawDataProcessedFilter}.
     *          Can be {@code null} (equivalent to requesting all results).
     */
    public RawDataFilter getRawDataFilter() {
        return this.rawDataFilter;
    }
    /**
     * @return  A {@code Set} of {@code DAORawDataFilter}s that allow to configure
     *          the queries to DAOs, that were generated by the {@link RawDataService}
     *          based on a provided {@code RawDataFilter}. Several {@code DAORawDataFilter}s
     *          will be treated as "OR" conditions. If {@code null}, it means that
     *          no conditions were matching the parameters, thus there will be no result
     *          and no query done.
     */
    Set<DAORawDataFilter> getDaoRawDataFilters() {
        return daoRawDataFilters;
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
    Map<Integer, Gene> getRequestedGeneMap() {
        return requestedGeneMap;
    }
    /**
     * @return  A {@code Map} where keys are {@code Integer}s corresponding to Bgee internal
     *          raw data condition IDs, the associated value being the corresponding
     *          {@code RawDataCondition}s, that were requested to retrieve raw data.
     *          Only raw data conditions that were explicitly requested are present
     *          in this {@code Map}, for easier instantiation of the objects returned by this class.
     *          If all conditions of a species were requested, they are not present 
     *          in this {@code Map} and they should be retrieved as needed.
     */
    Map<Integer, RawDataCondition> getRequestedRawDataConditionMap() {
        return requestedRawDataConditionMap;
    }
    /**
     * @return  A {@code Map} where keys are species IDs, the associated value being
     *          the corresponding {@code Species}. It is stored in order to more
     *          efficiently instantiate objects returned by this class. Only {@code Species}
     *          that can potentially be queried are stored in this {@code Map}.
     */
    Map<Integer, Species> getSpeciesMap() {
        return speciesMap;
    }
    /**
     * @return  A {@code Map} where keys are gene biotype IDs, the associated value being
     *          the corresponding {@code GeneBioType}. It is stored in order to
     *          more efficiently create new {@code Gene}s.
     */
    Map<Integer, GeneBioType> getGeneBioTypeMap() {
        return geneBioTypeMap;
    }
    /**
     * @return  A {@code Map} where keys are source IDs, the associated value being
     *          the corresponding {@code Source}. It is stored for easier instantiation
     *          of the objects returned by this class.
     */
    Map<Integer, Source> getSourceMap() {
        return sourceMap;
    }

    @Override
    public int hashCode() {
        return Objects.hash(daoRawDataFilters, geneBioTypeMap, rawDataFilter, requestedGeneMap,
                requestedRawDataConditionMap, sourceMap, speciesMap);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RawDataProcessedFilter other = (RawDataProcessedFilter) obj;
        return Objects.equals(daoRawDataFilters, other.daoRawDataFilters)
                && Objects.equals(geneBioTypeMap, other.geneBioTypeMap)
                && Objects.equals(rawDataFilter, other.rawDataFilter)
                && Objects.equals(requestedGeneMap, other.requestedGeneMap)
                && Objects.equals(requestedRawDataConditionMap, other.requestedRawDataConditionMap)
                && Objects.equals(sourceMap, other.sourceMap) && Objects.equals(speciesMap, other.speciesMap);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RawDataProcessedFilter [")
               .append("rawDataFilter=").append(rawDataFilter)
               .append(", daoRawDataFilters=").append(daoRawDataFilters)
               .append(", requestedGenesMap=").append(requestedGeneMap)
               .append(", requestedRawDataConditionsMap=").append(requestedRawDataConditionMap)
               .append(", speciesMap=").append(speciesMap)
               .append(", geneBioTypeMap=").append(geneBioTypeMap)
               .append(", sourceMap=").append(sourceMap)
               .append("]");
        return builder.toString();
    }
}