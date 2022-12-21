package org.bgee.model.expressiondata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;

//TODO: be able to EXCLUDE anat. entities/stages. It would be convenient to discard
//non-informative anat. entities.
public abstract class BaseConditionFilter2<T extends BaseCondition2> {
    private final static Logger log = LogManager.getLogger(BaseConditionFilter2.class.getName());

    public static class ComposedFilterIds<T extends Comparable<T>> {
        private final List<Set<T>> composedFilterIds;

        public static final <T extends Comparable<T>> ComposedFilterIds<T> of() {
            return new ComposedFilterIds<>((Collection<T>) null);
        }
        /**
         * 
         * @param <T>
         * @param filterIds Can be null or empty
         * @return
         */
        public static final <T extends Comparable<T>> ComposedFilterIds<T> of(
                Collection<T> filterIds) {
            return new ComposedFilterIds<>(filterIds);
        }
        /**
         * 
         * @param <T>
         * @param filterIds1 cannot be null nor empty
         * @param filterIds2 cannot be null nor empty
         * @return
         */
        public static final <T extends Comparable<T>> ComposedFilterIds<T> of(
                Collection<T> filterIds1, Collection<T> filterIds2) {
            return new ComposedFilterIds<>(
                    //List.of not tolerant to null elements, we create empty HashSet if collection null,
                    //correct check on null/empty collections will be performed in the main constructor
                    List.of(
                    //new HashSet is tolerant to null element, Set.of is not.
                    //Checks on null elements, empty collections, etc will be performed
                    //in the main constructor
                    filterIds1 == null? new HashSet<>(): new HashSet<>(filterIds1),
                    filterIds2 == null? new HashSet<>(): new HashSet<>(filterIds2)));
        }
        /**
         * 
         * @param <T>
         * @param filterIds1 cannot be null nor empty
         * @param filterIds2 cannot be null nor empty
         * @param filterIds3 cannot be null nor empty
         * @return
         */
        public static final <T extends Comparable<T>> ComposedFilterIds<T> of(
                Collection<T> filterIds1, Collection<T> filterIds2, Collection<T> filterIds3) {
            return new ComposedFilterIds<>(
                    //List.of not tolerant to null elements, we create empty HashSet if collection null,
                    //correct check on null/empty collections will be performed in the main constructor
                    List.of(
                    //new HashSet is tolerant to null element, Set.of is not.
                    //Checks on null elements, empty collections, etc will be performed
                    //in the main constructor
                    filterIds1 == null? new HashSet<>(): new HashSet<>(filterIds1),
                    filterIds2 == null? new HashSet<>(): new HashSet<>(filterIds2),
                    filterIds3 == null? new HashSet<>(): new HashSet<>(filterIds3)));
        }
        /**
         * Convenient constructor when no composition is required.
         *
         * @param filterIds         A {@code Collection} of {@code T}s to configure a filter.
         *                          Can be {@code null} or empty for no filtering.
         *                          Cannot contain null elements, otherwise an
         *                          {@code IllegalArgumentException} is thrown.
         */
        public ComposedFilterIds(Collection<T> filterIds) {
            this(filterIds == null || filterIds.isEmpty()? null:
                //new HashSet is tolerant to null element, Set.of is not.
                //Checks on null elements will be performed in the main constructor
                List.of(new HashSet<>(filterIds)));
        }
        /**
         * Constructor allowing to create a composed filter.
         *
         * @param composedFilterIds A {@code List} of {@code Set}s of {@code T}s,
         *                          where each inner {@code Set} is a filter,
         *                          the list representing the composition.
         *                          The {@code List} can be {@code null} or empty for no filtering.
         *                          The inner {@code Set}s cannot be null, empty,
         *                          or contain null elements, otherwise an
         *                          {@code IllegalArgumentException} is thrown.
         */
        public ComposedFilterIds(List<Set<T>> composedFilterIds) {
            if (composedFilterIds != null && composedFilterIds.stream()
                    .anyMatch(c -> c == null || c.isEmpty() || c.contains(null))) {
                throw log.throwing(new IllegalArgumentException(
                        "Invalid inner collection"));
            }
            this.composedFilterIds = Collections.unmodifiableList(composedFilterIds == null?
                    //List.of is already immutable, but not Collectors.toList() below
                    List.of():
                    composedFilterIds.stream()
                    .map(c -> Set.copyOf(c)) //Set.copyOf returns an immutableSet, so we're good
                    .collect(Collectors.toList()));
        }

        public List<Set<T>> getComposedFilterIds() {
            return composedFilterIds;
        }
        public Set<T> getFirstFilterIds() {
            if (composedFilterIds.isEmpty()) {
                return log.traceExit(Set.of());
            }
            return log.traceExit(composedFilterIds.get(0));
        }
        public boolean isComposed() {
            return getComposedFilterIds().size() > 1;
        }
        public boolean isEmpty() {
            return composedFilterIds.isEmpty();
        }

        @Override
        public int hashCode() {
            return Objects.hash(composedFilterIds);
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ComposedFilterIds<?> other = (ComposedFilterIds<?>) obj;
            return Objects.equals(composedFilterIds, other.composedFilterIds);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ComposedFilterIds [")
                   .append("composedFilterIds=").append(composedFilterIds)
                   .append("]");
            return builder.toString();
        }
    }

    private final Integer speciesId;
    //We could still another generic type to ConditionParameter if we wanted to specify
    //the type of ID of each condition parameter value
    private final Map<ConditionParameter<?, ?>, ComposedFilterIds<String>> condParamToComposedFilterIds;

    /**
     * @param speciesId                     An {@code Integer} that is the ID of the species
     *                                      that this {@code ConditionFilter} will specify to use.
     *                                      Can be {@code null}.
     * @param condParamToComposedFilterIds  A {@code Map} where keys are {@code ConditionParameter},
     *                                      the associated value being a {@code ComposedFilterIds}s
     *                                      to specify the requested IDs for the related condition parameter.
     *                                      For instance, to retrieve conditions having an anat. entity ID
     *                                      equals to "ID1", this {@code Map} will contain the key
     *                                      {@code ConditionParameter.ANAT_ENTITY}, associated with
     *                                      a {@code ComposedFilterIds} that could have been created by calling
     *                                      {@code ComposedFilterIds.of(Set.of("ID1"))}.
     *                                      The provided argument can be null, or empty, or not contain
     *                                      all {@code ConditionParameter}s, or have {@code null} values.
     *                                      It should not contain {@code null} keys.
     * @throws IllegalArgumentException
     */
    protected BaseConditionFilter2(Integer speciesId,
            Map<ConditionParameter<?, ?>, ComposedFilterIds<String>> condParamToComposedFilterIds)
                    throws IllegalArgumentException {
        if (speciesId != null && speciesId < 1) {
            throw log.throwing(new IllegalArgumentException("No species ID can be less than 1"));
        }
        this.speciesId = speciesId;

        if (condParamToComposedFilterIds != null && condParamToComposedFilterIds.containsKey(null)) {
            throw log.throwing(new IllegalArgumentException(
                    "condParamToComposedFilterIds cannot contain null keys."));
        }
        this.condParamToComposedFilterIds = Collections.unmodifiableMap(
                ConditionParameter.allOf().stream()
                .collect(Collectors.toMap(
                        c -> c,
                        c -> {
                            if (condParamToComposedFilterIds == null) {
                                return ComposedFilterIds.of();
                            }
                            ComposedFilterIds<String> f = condParamToComposedFilterIds.get(c);
                            if (f == null) {
                                return ComposedFilterIds.of();
                            }
                            return f;
                        })));
    }

    /**
     * @return  An {@code Integer} that is the ID of the species that
     *          this {@code ConditionFilter} will specify to use. Can be {@code null}.
     */
    public Integer getSpeciesId() {
        return speciesId;
    }
    /**
     * @return  A {@code Map} where keys are {@code ConditionParameter},
     *          the associated value being a {@code ComposedFilterIds}s
     *          to specify the requested IDs for the related condition parameter.
     *          For instance, to retrieve conditions having an anat. entity ID
     *          equals to "ID1", this {@code Map} will contain the key
     *          {@code ConditionParameter.ANAT_ENTITY}, associated with
     *          a {@code ComposedFilterIds} that could have been created by calling
     *          {@code ComposedFilterIds.of(Set.of("ID1"))}.
     *          The returned {@code Map} is unmodifiable.
     *          It is guaranteed to contain an entry for each {@code ConditionParameter},
     *          (although associated with a {@code ComposedFilterIds} object that returns {@code true}
     *          when calling {@code ComposedFilterIds#isEmpty()}, when no filtering
     *          is requested for the associated condition parameter).
     */
    public Map<ConditionParameter<?, ?>, ComposedFilterIds<String>> getCondParamToComposedFilterIds() {
        return condParamToComposedFilterIds;
    }

    public boolean areAllCondParamFiltersEmpty() {
        log.traceEntry();
        return log.traceExit(this.getCondParamToComposedFilterIds().values()
                .stream()
                .allMatch(composedIds -> composedIds.isEmpty()));
    }

    public String toParamString() {
        log.traceEntry();
        StringBuilder sb = new StringBuilder();

        sb.append(ConditionParameter.allOf().stream()
                .map(param -> this.getCondParamToComposedFilterIds().get(param))
                .filter(composedIds -> !composedIds.isEmpty())
                .map(composedIds -> composedIds.getComposedFilterIds().stream()
                        .flatMap(ids -> ids.stream().sorted())
                        .collect(Collectors.joining("_"))
                        .replaceAll(" ", "_").replaceAll(":", "_"))
                .collect(Collectors.joining("_")));

        if (getSpeciesId() != null) {
            if (!sb.isEmpty()) {
                sb.append("_");
            }
            sb.append(getSpeciesId().toString());
        }
        return log.traceExit(sb.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(condParamToComposedFilterIds, speciesId);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BaseConditionFilter2<?> other = (BaseConditionFilter2<?>) obj;
        return Objects.equals(condParamToComposedFilterIds, other.condParamToComposedFilterIds)
                && Objects.equals(speciesId, other.speciesId);
    }
}
