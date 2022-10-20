package org.bgee.model.gene;

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
public class SearchMatchResult<T extends SearchMatch> {

    private final int totalMatchCount;
    private final List<T> searchMatches;

	public SearchMatchResult(int totalMatchCount, List<T> searchMatches) {
        if (totalMatchCount < 0) {
	        throw new IllegalArgumentException("The count of matches must be provided.");
	    }
        if (searchMatches != null && searchMatches.size() > totalMatchCount) {
            throw new IllegalArgumentException(
                    "The number of matches must be equals or inferior to the match count.");
        }
        this.totalMatchCount = totalMatchCount;
        this.searchMatches = searchMatches == null? null : Collections.unmodifiableList(searchMatches);
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
    public List<T> getSearchMatches() {
        return searchMatches;
    }

    @Override
    public int hashCode() {
        return Objects.hash(searchMatches, totalMatchCount);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SearchMatchResult other = (SearchMatchResult) obj;
        return Objects.equals(searchMatches, other.searchMatches) && totalMatchCount == other.totalMatchCount;
    }

    @Override
    public String toString() {
        return "SearchMatchResult{matchCount=" + totalMatchCount
                + ", searchMatches=" + searchMatches + "}";
    }
}
