package org.bgee.model.expressiondata;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;

//TODO: be able to EXCLUDE anat. entities/stages. It would be convenient to discard
//non-informative anat. entities.
public abstract class BaseConditionFilter2<T extends BaseCondition2> {
    private final static Logger log = LogManager.getLogger(BaseConditionFilter2.class.getName());

    public static class FilterIds<T extends Comparable<T>> {
        private final Set<T> filterIds;
        private final boolean includeChildTerms;
        private final Set<T> excludeTermsAndChildrenIds;
        private final Set<T> notToExcludeIds;

        public FilterIds() {
            this(null);
        }
        public FilterIds(T id) {
            this(id, false);
        }
        public FilterIds(T id, boolean includeChildTerms) {
            this(id == null? null: Set.of(id), includeChildTerms);
        }
        /**
         * @param filterIds         A {@code Collection} of {@code T}s to configure a filter.
         *                          Can be {@code null} or empty for no filtering.
         *                          Cannot contain null elements, otherwise an
         *                          {@code IllegalArgumentException} is thrown.
         * @param includeChildTerms A {@code boolean} to request, when {@code true},
         *                          to retrieve child terms of the IDs provided
         *                          in {@code filterIds}. Always considered as {@code false}
         *                          if {@code filterIds} is {@code null} or empty.
         */
        public FilterIds(Collection<T> filterIds, boolean includeChildTerms) {
            this(filterIds, includeChildTerms, null, null);
        }
        /**
         * To retrieve conditions including the terms with IDs in {@code filterIds},
         * <strong>including their children</strong>, but excluding all the terms with IDs
         * in {@code excludeTermsAndChildrenIds} and all of their children.
         *
         * @param filterIds                 A {@code Collection} of {@code T}s to configure a filter,
         *                                  including their child terms.
         *                                  Can be {@code null} or empty for no filtering.
         *                                  Cannot contain null elements, otherwise an
         *                                  {@code IllegalArgumentException} is thrown.
         * @param excludeTermsAndChildrenIds    A {@code Collection} of {@code T}s containing the term IDs
         *                                  which we want to exclude from the results,
         *                                  excluding also all of their children.
         *                                  Can be {@code null} or empty. Cannot contain null elements,
         *                                  otherwise an {@code IllegalArgumentException} is thrown.
         *                                  Cannot contain elements if {@code filterIds}
         *                                  is {@code null} or empty, otherwise an
         *                                  {@code IllegalArgumentException} is thrown.
         * @param notToExcludeIds           A {@code Collection} of {@code T}s containing the term IDs
         *                                  not to exclude, even if they are terms or children of terms in
         *                                  {@code excludeTermsAndChildrenIds}.
         *                                  Can be {@code null} or empty. Cannot contain null elements,
         *                                  otherwise an {@code IllegalArgumentException} is thrown.
         *                                  Cannot contain elements if {@code excludeTermsAndChildrenIds}
         *                                  is {@code null} or empty, otherwise an
         *                                  {@code IllegalArgumentException} is thrown.
         */
        public FilterIds(Collection<T> filterIds, Collection<T> excludeTermsAndChildrenIds,
                Collection<T> notToExcludeIds) {
            this(filterIds, true, excludeTermsAndChildrenIds, notToExcludeIds);
        }
        /**
         * @param filterIds                 A {@code Collection} of {@code T}s to configure a filter.
         *                                  Can be {@code null} or empty for no filtering.
         *                                  Cannot contain null elements, otherwise an
         *                                  {@code IllegalArgumentException} is thrown.
         * @param includeChildTerms         A {@code boolean} to request, when {@code true},
         *                                  to retrieve child terms of the IDs provided
         *                                  in {@code filterIds}. Always considered as {@code false}
         *                                  if {@code filterIds} is {@code null} or empty.
         * @param excludeTermsAndChildrenIds A {@code Collection} of {@code T}s containing the term IDs
         *                                  which we want to exclude from the results,
         *                                  excluding also all of their children.
         *                                  Can be {@code null} or empty. Cannot contain null elements,
         *                                  otherwise an {@code IllegalArgumentException} is thrown.
         *                                  Cannot contain elements if {@code filterIds}
         *                                  is {@code null} or empty, otherwise an
         *                                  {@code IllegalArgumentException} is thrown.
         * @param notToExcludeIds           A {@code Collection} of {@code T}s containing the term IDs
         *                                  not to exclude, even if they are terms or children of terms in
         *                                  {@code excludeTermsAndChildrenIds}.
         *                                  Can be {@code null} or empty. Cannot contain null elements,
         *                                  otherwise an {@code IllegalArgumentException} is thrown.
         *                                  Cannot contain elements if {@code excludeTermsAndChildrenIds}
         *                                  is {@code null} or empty, otherwise an
         *                                  {@code IllegalArgumentException} is thrown.
         */
        public FilterIds(Collection<T> filterIds, boolean includeChildTerms,
                Collection<T> excludeTermsAndChildrenIds, Collection<T> notToExcludeIds) {
            if (filterIds != null && filterIds.stream().anyMatch(e -> e == null)) {
                throw log.throwing(new IllegalArgumentException("No filter ID can be null"));
            }
            if (excludeTermsAndChildrenIds != null && excludeTermsAndChildrenIds.stream().anyMatch(e -> e == null)) {
                throw log.throwing(new IllegalArgumentException("No exclusion ID can be null"));
            }
            if (notToExcludeIds != null && notToExcludeIds.stream().anyMatch(e -> e == null)) {
                throw log.throwing(new IllegalArgumentException("No retain ID can be null"));
            }
            if ((filterIds == null || filterIds.isEmpty()) &&
                    excludeTermsAndChildrenIds != null && !excludeTermsAndChildrenIds.isEmpty()) {
                throw log.throwing(new IllegalArgumentException(
                        "Cannot exclude terms if no filter terms provided"));
            }
            if (!includeChildTerms && excludeTermsAndChildrenIds != null && !excludeTermsAndChildrenIds.isEmpty()) {
                throw log.throwing(new IllegalArgumentException(
                        "Not necessary to exclude terms if no child terms requested"));
            }
            if (notToExcludeIds != null && !notToExcludeIds.isEmpty() &&
                    (excludeTermsAndChildrenIds == null || excludeTermsAndChildrenIds.isEmpty())) {
                throw log.throwing(new IllegalArgumentException(
                        "Cannot retain terms if no term children are excluded"));
            }
            //Set.of and Set.copyOf already returns immutable Sets
            //(But Set.copyOf is not tolerant to null values, this is why we did a check first)
            this.filterIds = filterIds == null? Set.of(): Set.copyOf(filterIds);
            this.includeChildTerms = this.filterIds.isEmpty()? false: includeChildTerms;
            this.excludeTermsAndChildrenIds = excludeTermsAndChildrenIds == null? Set.of(): Set.copyOf(excludeTermsAndChildrenIds);
            this.notToExcludeIds = notToExcludeIds == null? Set.of(): Set.copyOf(notToExcludeIds);

            //Make the equals check after the collections are Sets.
            if (this.notToExcludeIds.isEmpty() && !this.filterIds.isEmpty() &&
                    Objects.equals(this.filterIds, this.excludeTermsAndChildrenIds)) {
                throw log.throwing(new IllegalArgumentException(
                        "IDs requested and excluded are the same and no exception is set"));
            }
        }

        /**
         * @return  An immutable {@code Set} containing the IDs requested for filtering.
         *          Can be empty.
         * @see #isIncludeChildTerms()
         */
        public Set<T> getIds() {
            return filterIds;
        }
        /**
         * @return  A {@code boolean} to request, when {@code true}, to retrieve child terms
         *          of the IDs provided in {@link #getIds()}. Always {@code false}
         *          when {@link #getIds()} returns an empty {@code Set}.
         * @see #getIds()
         */
        public boolean isIncludeChildTerms() {
            return includeChildTerms;
        }
        /**
         * @return  An immutable {@code Set} containing the term IDs which we want
         *          to exclude from the results, excluding also all of their children.
         *          Can be empty. Terms in the {@code Set} returned by {@link #getNotToExcludeIds()}
         *          are not excluded.
         */
        public Set<T> getExcludeTermsAndChildrenIds() {
            return excludeTermsAndChildrenIds;
        }
        /**
         * @return  An immutable {@code Set} containing the term IDs not to exclude,
         *          even if they are terms or children of terms in in the {@code Set} returned by
         *          {@link #getExcludeTermsAndChildrenIds()}. Can be empty.
         */
        public Set<T> getNotToExcludeIds() {
            return notToExcludeIds;
        }
        /**
         * Convenient shortcut method.
         *
         * @return  A {@code boolean} that is the value returned by
         *          {@code getFilterIds().isEmpty()}.
         */
        public boolean isEmpty() {
            return this.getIds().isEmpty();
        }

        @Override
        public int hashCode() {
            return Objects.hash(filterIds, includeChildTerms, excludeTermsAndChildrenIds, notToExcludeIds);
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            FilterIds<?> other = (FilterIds<?>) obj;
            return Objects.equals(filterIds, other.filterIds)
                    && includeChildTerms == other.includeChildTerms
                    && Objects.equals(excludeTermsAndChildrenIds, other.excludeTermsAndChildrenIds)
                    && Objects.equals(notToExcludeIds, other.notToExcludeIds);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("FilterIds [")
                   .append("filterIds=").append(filterIds)
                   .append(", includeChildTerms=").append(includeChildTerms)
                   .append(", excludeTermsAndChildrenIds=").append(excludeTermsAndChildrenIds)
                   .append(", notToExcludeIds=").append(notToExcludeIds)
                   .append("]");
            return builder.toString();
        }
    }
    
    public static class ComposedFilterIds<T extends Comparable<T>> {
        private final List<FilterIds<T>> composedFilterIds;

        /**
         * Creates an empty {@code ComposedFilterIds}.
         */
        public ComposedFilterIds() {
            this((List<FilterIds<T>>) null);
        }
        /**
         * Convenient constructor when no composition is required.
         *
         * @param filterIds         A {@code Collection} of {@code T}s to configure a filter.
         *                          Can be {@code null} or empty for no filtering.
         */
        public ComposedFilterIds(FilterIds<T> filterIds) {
            this(filterIds == null || filterIds.isEmpty()? null:
                //new HashSet is tolerant to null element, Set.of is not.
                //Checks on null elements will be performed in the main constructor
                List.of(filterIds));
        }
        /**
         * Constructor allowing to create a composed filter.
         *
         * @param composedFilterIds A {@code List} of {@code FilterIds}s of {@code T}s,
         *                          used to create a composed filter. Each {@code FilterIds} element
         *                          itself contains potentially multiple IDs, seen as "OR" conditions
         *                          between them, while this {@code ComposedFilterIds} generates
         *                          "AND" condition between the {@code FilterIds}s.
         *                          The {@code List} can be {@code null} or empty for no filtering.
         *                          The inner {@code FilterIds}s cannot be null, or empty
         *                          according to their {@code isEmpty()} method, otherwise an
         *                          {@code IllegalArgumentException} is thrown.
         */
        public ComposedFilterIds(List<FilterIds<T>> composedFilterIds) {
            if (composedFilterIds != null && composedFilterIds.stream()
                    .anyMatch(c -> c == null || c.isEmpty())) {
                throw log.throwing(new IllegalArgumentException(
                        "Invalid inner FilterIds"));
            }
            //List.of and List.copyOf already returns immutable Lists
            this.composedFilterIds = composedFilterIds == null? List.of():
                List.copyOf(composedFilterIds);
        }

        public List<FilterIds<T>> getComposedFilterIds() {
            return composedFilterIds;
        }
        /**
         * Returns the {@code FilterIds} of {@code T}s at the index provided,
         * from the {@code List} returns by {@link #getComposedFilterIds()}.
         * Unlike the method {@code List.get(int)}, this method returns {@code null}
         * if the index is out of bond, instead of throwing an {@code IndexOutOfBoundsException}.
         *
         * @param index The {@code int} that is the index of the {@code FilterIds} to return.
         * @return      The {@code FilterIds} of {@code T}s at the specified position
         *              in the composed filter list.
         */
        public FilterIds<T> getFilterIds(int index) {
            log.traceEntry();
            try {
                return log.traceExit(composedFilterIds.get(index));
            } catch (IndexOutOfBoundsException e) {
                log.catching(Level.DEBUG, e);
                return log.traceExit((FilterIds<T>) null);
            }
        }
        public Set<T> getIds(int index) {
            log.traceEntry();
            FilterIds<T> filterIds = this.getFilterIds(index);
            if (filterIds == null) {
                return log.traceExit((Set<T>) null);
            }
            return log.traceExit(filterIds.getIds());
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
    private final boolean excludeNonInformative;

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
     * @param excludeNonInformative         A {@code boolean} defining whether to exclude non-informative
     *                                      conditions from results. If {@code true}, non-informative conditions
     *                                      are excluded.
     * @throws IllegalArgumentException
     */
    protected BaseConditionFilter2(Integer speciesId,
            Map<ConditionParameter<?, ?>, ComposedFilterIds<String>> condParamToComposedFilterIds,
            boolean excludeNonInformative) throws IllegalArgumentException {
        if (speciesId != null && speciesId < 1) {
            throw log.throwing(new IllegalArgumentException("No species ID can be less than 1"));
        }
        this.speciesId = speciesId;

        if (condParamToComposedFilterIds != null &&
                //Cannot call containsKey(null) if the Map does not accept null values
                condParamToComposedFilterIds.keySet().stream().anyMatch(k -> k == null)) {
            throw log.throwing(new IllegalArgumentException(
                    "condParamToComposedFilterIds cannot contain null keys."));
        }
        this.condParamToComposedFilterIds = Collections.unmodifiableMap(
                ConditionParameter.allOf().stream()
                .collect(Collectors.toMap(
                        c -> c,
                        c -> {
                            if (condParamToComposedFilterIds == null) {
                                return new ComposedFilterIds<>();
                            }
                            ComposedFilterIds<String> f = condParamToComposedFilterIds.get(c);
                            if (f == null) {
                                return new ComposedFilterIds<>();
                            }
                            return f;
                        })));
        this.excludeNonInformative = excludeNonInformative;
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
    public ComposedFilterIds<String> getComposedFilterIds(ConditionParameter<?, ?> condParam) {
        return condParamToComposedFilterIds.get(condParam);
    }
    /**
     * @return  A {@code boolean} defining whether to exclude non-informative
     *          conditions from results. If {@code true}, non-informative conditions
     *          are excluded.
     */
    public boolean isExcludeNonInformative() {
        return excludeNonInformative;
    }

    public boolean areAllFiltersExceptSpeciesEmpty() {
        log.traceEntry();
        return log.traceExit(!this.isExcludeNonInformative() &&
                this.getCondParamToComposedFilterIds().values().stream()
                .allMatch(composedIds -> composedIds.isEmpty()));
    }

    public String toParamString() {
        log.traceEntry();
        StringBuilder sb = new StringBuilder();

        sb.append(ConditionParameter.allOf().stream()
                .map(param -> this.getCondParamToComposedFilterIds().get(param))
                .filter(composedIds -> !composedIds.isEmpty())
                .map(composedIds -> composedIds.getComposedFilterIds().stream()
                        .map(filterIds -> filterIds.getIds().stream().sorted()
                                .collect(Collectors.joining("_"))
                                + (filterIds.isIncludeChildTerms()? "_with_child_terms": ""))
                        .collect(Collectors.joining("_"))
                        .replaceAll(" ", "_").replaceAll(":", "_"))
                .collect(Collectors.joining("_")));

        if (getSpeciesId() != null) {
            if (sb.length() != 0) {
                sb.append("_");
            }
            sb.append(getSpeciesId().toString());
        }
        if (this.isExcludeNonInformative()) {
            if (sb.length() != 0) {
                sb.append("_");
            }
            sb.append(this.isExcludeNonInformative());
        }
        return log.traceExit(sb.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(condParamToComposedFilterIds, excludeNonInformative, speciesId);
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
                && excludeNonInformative == other.excludeNonInformative
                && Objects.equals(speciesId, other.speciesId);
    }
}
