package org.bgee.model.search;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * This class encapsulates a result of a search.
 * 
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

	public SearchMatchResult(int totalMatchCount, List<SearchMatch<T>> searchMatches, Class<T> type) {
        if (totalMatchCount < 0) {
	        throw new IllegalArgumentException("The count of matches must be provided.");
	    }
        if (searchMatches != null && searchMatches.size() > totalMatchCount) {
            throw new IllegalArgumentException(
                    "The number of matches must be equals or inferior to the match count.");
        }
        this.totalMatchCount = totalMatchCount;
        this.searchMatches = searchMatches == null? null : Collections.unmodifiableList(searchMatches);
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
        SearchMatchResult<T> other = (SearchMatchResult<T>) obj;
        return Objects.equals(searchMatches, other.searchMatches) && totalMatchCount == other.totalMatchCount
                && type == other.type;
    }

    @Override
    public String toString() {
        return "SearchMatchResult [totalMatchCount=" + totalMatchCount + ", searchMatches=" + searchMatches + ", type="
                + type + "]";
    }
}
