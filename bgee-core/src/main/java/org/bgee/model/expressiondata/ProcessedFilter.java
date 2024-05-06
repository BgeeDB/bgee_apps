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
import org.bgee.model.gene.GeneFilter;
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
 * <p>
 * The processed information is structured in 3 parts:
 * <ul>
 * <li>{@link ProcessedFilterGeneSpeciesPart}
 * <li>{@link ProcessedFilterConditionPart}
 * <li>{@link ProcessedFilterInvariablePart}
 * </ul>
 * This allows for instance to cache different parts of the processed information independently.
 *
 * @author Frederic Bastian
 * @version Bgee 15.1, May 2024
 * @since Bgee 15.0, Dec. 2022
 * @see #getSourceFilter()
 */
public abstract class ProcessedFilter<T extends DataFilter<?>, U extends DAODataFilter2,
//TODO: reestablish V extends BaseCondition<V> when code will be refactored
//TODO: put back W extends BaseConditionFilter2 or something, once refactoring is done
V, W> {
    private final static Logger log = LogManager.getLogger(ProcessedFilter.class.getName());

    /**
     * Class holding pre-processed information generated from a {@code DataFilter},
     * with respect to the gene and species filtering, to offer the possibility
     * not to re-process this information for the same parameters.
     *
     * @author Frederic Bastian
     * @version Bgee 15.1, May 2024
     * @since Bgee 15.1, May 2024
     */
    public static class ProcessedFilterGeneSpeciesPart {

        private final Set<GeneFilter> geneFilters;
        private final Map<Integer, Gene> requestedGeneMap;
        private final Map<Integer, Species> speciesMap;

        protected ProcessedFilterGeneSpeciesPart(Collection<GeneFilter> geneFilters,
                Map<Integer, Gene> requestedGeneMap, Map<Integer, Species> speciesMap) {

            this.geneFilters = Collections.unmodifiableSet(geneFilters == null?
                    new HashSet<>(): new HashSet<>(geneFilters));

            this.requestedGeneMap = Collections.unmodifiableMap(requestedGeneMap == null?
                    new HashMap<>(): new HashMap<>(requestedGeneMap));
            if (this.requestedGeneMap.entrySet().stream()
                    .anyMatch(e -> e.getKey() == null || e.getKey() < 1 || e.getValue() == null)) {
                throw log.throwing(new IllegalArgumentException("Invalid gene Map: "
                        + this.requestedGeneMap));
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
        }

        /**
         * @return  An unmodifiable {@code Set} {@code GeneFilter}s allowing to configure gene-related
         *          filtering. If several {@code GeneFilter}s are configured, they are seen as "OR" conditions.
         *          A same species ID is not used in several {@code GeneFilter}s of this {@code Set}.
         */
        public Set<GeneFilter> getGeneFilters() {
            return geneFilters;
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
         * @return  A {@code Map} where keys are species IDs, the associated value being
         *          the corresponding {@code Species}. It is stored in order to more
         *          efficiently instantiate objects returned by this class. Only {@code Species}
         *          that can potentially be queried are stored in this {@code Map}.
         */
        protected Map<Integer, Species> getSpeciesMap() {
            return speciesMap;
        }

        @Override
        public int hashCode() {
            return Objects.hash(geneFilters, requestedGeneMap, speciesMap);
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ProcessedFilterGeneSpeciesPart other = (ProcessedFilterGeneSpeciesPart) obj;
            return Objects.equals(geneFilters, other.geneFilters)
                    && Objects.equals(requestedGeneMap, other.requestedGeneMap)
                    && Objects.equals(speciesMap, other.speciesMap);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ProcessedFilterGeneSpeciesPart [")
                   .append("geneFilters=").append(geneFilters)
                   .append(", requestedGeneMap=").append(requestedGeneMap)
                   .append(", speciesMap=").append(speciesMap)
                   .append("]");
            return builder.toString();
        }
    }

    /**
     * Class holding pre-processed information generated from a {@code DataFilter},
     * with respect to the condition filtering, to offer the possibility
     * not to re-process this information for the same parameters..
     *
     * @author Frederic Bastian
     * @version Bgee 15.1, May 2024
     * @since Bgee 15.1, May 2024
     */
    //TODO: put back T extends BaseConditionFilter2 or something, once refactoring is done
    //TODO: reestablish U extends BaseCondition<V> when code will be refactored
    public static abstract class ProcessedFilterConditionPart<T, U> {

        //XXX: all parameters are OR conditions
        private final Set<T> conditionFilters;
        private final Map<Integer, U> requestedConditionMap;

        protected ProcessedFilterConditionPart(Collection<T> conditionFilters,
                Map<Integer, U> requestedConditionMap) {
            this.conditionFilters = Collections.unmodifiableSet(conditionFilters == null?
                    new HashSet<>(): new HashSet<>(conditionFilters));
            this.requestedConditionMap = Collections.unmodifiableMap(
                    requestedConditionMap == null? new HashMap<>():
                        new HashMap<>(requestedConditionMap));
            if (this.requestedConditionMap.entrySet().stream()
                    .anyMatch(e -> e.getKey() == null || e.getKey() < 1 || e.getValue() == null)) {
                throw log.throwing(new IllegalArgumentException("Invalid raw data condition Map: "
                        + this.requestedConditionMap));
            }
        }
        /**
         * @return  An unmodifiable {@code Set} of condition filters, that were used
         *          to configure the filtering of conditions with expression data. If several 
         *          filters are configured, they were seen as "OR" conditions.
         */
        public Set<T> getConditionFilters() {
            return conditionFilters;
        }
        /**
         * @return  An unmodifiable {@code Map} where keys are {@code Integer}s corresponding to Bgee internal
         *          condition IDs, the associated value being the corresponding
         *          {@code U} condition, that were requested to retrieve data.
         *          Only conditions that were explicitly requested are present
         *          in this {@code Map}, for easier instantiation of the objects returned by this class.
         *          If all conditions of a species were requested, they are not present
         *          in this {@code Map} and they should be retrieved as needed.
         */
        protected Map<Integer, U> getRequestedConditionMap() {
            return requestedConditionMap;
        }

        @Override
        public int hashCode() {
            return Objects.hash(conditionFilters, requestedConditionMap);
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ProcessedFilterConditionPart<?, ?> other = (ProcessedFilterConditionPart<?, ?>) obj;
            return Objects.equals(conditionFilters, other.conditionFilters)
                    && Objects.equals(requestedConditionMap, other.requestedConditionMap);
        }
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ProcessedFilterConditionPart [")
                   .append("conditionFilters=").append(conditionFilters)
                   .append(", requestedConditionMap=").append(requestedConditionMap)
                   .append("]");
            return builder.toString();
        }
    }

    /**
     * Class holding pre-processed information used to retrieve and instantiate results,
     * that is always retrieved independently of other filterings, such as gene or condition filtering.
     *
     * @author Frederic Bastian
     * @version Bgee 15.1, May 2024
     * @since Bgee 15.1, May 2024
     */
    public static class ProcessedFilterInvariablePart {
        private final Map<Integer, GeneBioType> geneBioTypeMap;
        private final Map<Integer, Source> sourceMap;

        protected ProcessedFilterInvariablePart(Map<Integer, GeneBioType> geneBioTypeMap,
                Map<Integer, Source> sourceMap) {

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
            return Objects.hash(geneBioTypeMap, sourceMap);
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ProcessedFilterInvariablePart other = (ProcessedFilterInvariablePart) obj;
            return Objects.equals(geneBioTypeMap, other.geneBioTypeMap)
                    && Objects.equals(sourceMap, other.sourceMap);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ProcessedFilterInvariablePart [")
                   .append("geneBioTypeMap=").append(geneBioTypeMap)
                   .append(", sourceMap=").append(sourceMap)
                   .append("]");
            return builder.toString();
        }
    }

    private final T sourceFilter;
    private final Set<U> daoFilters;

    private final ProcessedFilterConditionPart<W, V> conditionPart;
    private final ProcessedFilterGeneSpeciesPart geneSpeciesPart;
    private final ProcessedFilterInvariablePart invariablePart;

    protected ProcessedFilter(T sourceFilter, Collection<U> daoFilters,
            ProcessedFilterGeneSpeciesPart geneSpeciesPart,
            ProcessedFilterConditionPart<W, V> conditionPart,
            ProcessedFilterInvariablePart invariablePart) {
        log.traceEntry("{}, {}, {}, {}, {}", sourceFilter, daoFilters,
                geneSpeciesPart, conditionPart, invariablePart);

        this.sourceFilter = sourceFilter;

        //When daoFilters are null, it means that there was no matching conditions
        //for the parameters, and that there will be no results
        this.daoFilters = daoFilters == null? null:
            Collections.unmodifiableSet(new HashSet<>(daoFilters));

        this.geneSpeciesPart = geneSpeciesPart;
        this.conditionPart = conditionPart;
        this.invariablePart = invariablePart;

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
     * @return  A {@code ProcessedFilterGeneSpeciesPart} holding pre-processed information
     *          generated from a {@code DataFilter}, with respect to the gene and species filtering,
     *          to offer the possibility not to re-process this information for the same parameters.
     */
    protected ProcessedFilterGeneSpeciesPart getGeneSpeciesPart() {
        return geneSpeciesPart;
    }
    /**
     * @return  A {@code ProcessedFilterConditionPart} holding pre-processed information generated
     *          from a {@code DataFilter}, with respect to the condition filtering,
     *          to offer the possibility not to re-process this information
     *          for the same condition filter parameters {@code W}.
     */
    protected ProcessedFilterConditionPart<W, V>  getConditionPart() {
        return conditionPart;
    }
    /**
     * @return  A {@code ProcessedFilterInvariablePart} holding pre-processed information
     *          used to retrieve and instantiate results, that is always retrieved independently
     *          of other filterings, such as gene or condition filtering.
     */
    protected ProcessedFilterInvariablePart  getInvariablePart() {
        return invariablePart;
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
        return geneSpeciesPart.getRequestedGeneMap();
    }
    /**
     * @return  A {@code Map} where keys are species IDs, the associated value being
     *          the corresponding {@code Species}. It is stored in order to more
     *          efficiently instantiate objects returned by this class. Only {@code Species}
     *          that can potentially be queried are stored in this {@code Map}.
     */
    protected Map<Integer, Species> getSpeciesMap() {
        return geneSpeciesPart.getSpeciesMap();
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
        return conditionPart.getRequestedConditionMap();
    }
    /**
     * @return  A {@code Map} where keys are gene biotype IDs, the associated value being
     *          the corresponding {@code GeneBioType}. It is stored in order to
     *          more efficiently create new {@code Gene}s.
     */
    protected Map<Integer, GeneBioType> getGeneBioTypeMap() {
        return invariablePart.geneBioTypeMap;
    }
    /**
     * @return  A {@code Map} where keys are source IDs, the associated value being
     *          the corresponding {@code Source}. It is stored for easier instantiation
     *          of the objects returned by this class.
     */
    protected Map<Integer, Source> getSourceMap() {
        return invariablePart.getSourceMap();
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceFilter, daoFilters,
                geneSpeciesPart, conditionPart, invariablePart);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProcessedFilter<?, ?, ?, ?> other = (ProcessedFilter<?, ?, ?, ?>) obj;
        return Objects.equals(sourceFilter, other.sourceFilter)
                && Objects.equals(daoFilters, other.daoFilters)
                && Objects.equals(geneSpeciesPart, other.geneSpeciesPart)
                && Objects.equals(conditionPart, other.conditionPart)
                && Objects.equals(invariablePart, other.invariablePart);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ProcessedFilter [")
               .append("sourceFilter=").append(sourceFilter)
               .append(", daoFilters=").append(daoFilters)
               .append(", geneSpeciesPart=").append(geneSpeciesPart)
               .append(", conditionPart=").append(conditionPart)
               .append(", invariablePart=").append(invariablePart)
               .append("]");
        return builder.toString();
    }
}