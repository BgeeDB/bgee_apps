package org.bgee.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A class holding the results of a search, for instance, the search
 * for {@code AnatEntity} based on requested IDs. Or the search for {@code Gene}
 * based on {@code GeneFilter}s. A convenient aspect of this class is that
 * it allows to retrieve the requested elements (for instance, IDs) that could not be found
 * in Bgee.
 *
 * @author Frederic Bastian
 * @version Bgee 14 May 2019
 * @since Bgee 14 May 2019
 *
 * @param <T>   The type of elements to perform the query, for instance, {@code String}
 *              when querying for elements based on IDs that are strings.
 * @param <U>   The type of elements returned by the query, for instance, {@code AnatEntity}
 *              when querying for {@code AnatEntity} elements based on their IDs.
 */
public class SearchResult<T, U> {
    /**
     * @see #getRequestElements()
     */
    private final Set<T> requestElements;
    /**
     * @see #getRequestElementsNotFound()
     */
    private final Set<T> requestElementsNotFound;
    /**
     * @see #getResults()
     */
    private final Set<U> results;

    public SearchResult(Collection<T> requestElements, Collection<T> requestElementsNotFound,
            Collection<U> results) {
        this.requestElements = Collections.unmodifiableSet(
                requestElements == null? new HashSet<>(): new HashSet<>(requestElements));
        this.requestElementsNotFound = Collections.unmodifiableSet(
                requestElementsNotFound == null? new HashSet<>(): new HashSet<>(requestElementsNotFound));
        this.results = Collections.unmodifiableSet(
                results == null? new HashSet<>(): new HashSet<>(results));
    }

    /**
     * @return  An unmodifiable {@code Set} of {@code T}s that are the elements used for the request.
     *          {@code U}s that were retrieved can be obtained by calling {@link #getResults()}.
     *          Request elements {@code T} that were not found in Bgee are listed by
     *          {@link #getRequestElementsNotFound()}.
     * @see #getResults()
     * @see #getRequestElementsNotFound()
     */
    public Set<T> getRequestElements() {
        return requestElements;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code T}s that are the request elements,
     *          among {@link #getRequestElements()}, that were not found in Bgee.
     * @see #getRequestElements()
     */
    public Set<T> getRequestElementsNotFound() {
        return requestElementsNotFound;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code U}s that are the results that were retrieved
     *          based on the request elements (see {@link #getRequestElements()}).
     *          Request elements that could not be found are listed in
     *          {@link #getRequestElementsNotFound()}.
     * @see #getRequestElements()
     * @see #getRequestElementsNotFound()
     */
    public Set<U> getResults() {
        return results;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((requestElements == null) ? 0 : requestElements.hashCode());
        result = prime * result + ((requestElementsNotFound == null) ? 0 : requestElementsNotFound.hashCode());
        result = prime * result + ((results == null) ? 0 : results.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SearchResult<?, ?> other = (SearchResult<?, ?>) obj;
        if (requestElements == null) {
            if (other.requestElements != null) {
                return false;
            }
        } else if (!requestElements.equals(other.requestElements)) {
            return false;
        }
        if (requestElementsNotFound == null) {
            if (other.requestElementsNotFound != null) {
                return false;
            }
        } else if (!requestElementsNotFound.equals(other.requestElementsNotFound)) {
            return false;
        }
        if (results == null) {
            if (other.results != null) {
                return false;
            }
        } else if (!results.equals(other.results)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SearchResult [requestElements=").append(requestElements)
               .append(", requestElementsNotFound=") .append(requestElementsNotFound)
               .append(", results=").append(results)
               .append("]");
        return builder.toString();
    }
}