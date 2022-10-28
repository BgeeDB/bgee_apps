package org.bgee.model.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.bgee.model.NamedEntity;
import org.bgee.model.gene.Gene;

/**
 * This class encapsulates a result of a search.
 *
 *
 * @param <T>   {@code T} must be either an instance of {@code Gene}, or of {@code NamedEntity},
 *              and although we cannot declare such a generic type extending either one class or another,
 *              a check is performed in the constructor.
 * @author  Valentine Rech de Laval
 * @author  Julien Wollbrett
 * @version Bgee 15, Oct. 2022
 * @since   Bgee 14, Jan. 2019
 * @see SearchMatch
 */
public class SearchMatchResult<T> {

    private final int totalMatchCount;
    private final List<SearchMatch<T>> searchMatches;
    private final Class<T> type;

    /**
     * @param totalMatchCount   An {@code int} that is the total number of results,
     *                          that could not have all been retrieved in {@code searchMatches}.
     * @param searchMatches     The retrieved {@code SearchMatch}es.
     * @param type              The {@code Class} of the objects retrieved in the {@code SearchMatch}es.
     * @throws IllegalArgumentException If {@code totalMatchCount} is less than 0,
     *                                  or the size of {@code searchMatches} is greater than {@code totalMatchCount},
     *                                  or {@code type} is not assignable either to {@code Gene}
     *                                  or to {@code NamedEntity}.
     */
	public SearchMatchResult(int totalMatchCount, List<SearchMatch<T>> searchMatches, Class<T> type)
	throws IllegalArgumentException {
        if (totalMatchCount < 0) {
	        throw new IllegalArgumentException("The count of matches must be provided.");
	    }
        if (searchMatches != null && searchMatches.size() > totalMatchCount) {
            throw new IllegalArgumentException(
                    "The number of matches must be equals or inferior to the match count.");
        }
        if (type == null) {
            throw new IllegalArgumentException("A type must be provided.");
        }
        if(!Gene.class.isAssignableFrom(type) && !NamedEntity.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException("The type must be assignable either to Gene class"
                    + "or NamedEntity class");
        }
        this.totalMatchCount = totalMatchCount;
        this.searchMatches = Collections.unmodifiableList(searchMatches == null? new ArrayList<>():
            new ArrayList<>(searchMatches));
        this.type = type;
	}

    /**
     * @return An {@code int} that is the count of all matches.
     */
    public int getTotalMatchCount() {
        return totalMatchCount;
    }

    /**
     * @return A {@code List} of {@code T} that are the ordered found matches.
     */
    public List<SearchMatch<T>> getSearchMatches() {
        return searchMatches;
    }

    public Class<T> getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(searchMatches, totalMatchCount, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SearchMatchResult<?> other = (SearchMatchResult<?>) obj;
        return Objects.equals(searchMatches, other.searchMatches) && totalMatchCount == other.totalMatchCount
                && type == other.type;
    }

    @Override
    public String toString() {
        return "SearchMatchResult [totalMatchCount=" + totalMatchCount + ", searchMatches=" + searchMatches + ", type="
                + type + "]";
    }
}
