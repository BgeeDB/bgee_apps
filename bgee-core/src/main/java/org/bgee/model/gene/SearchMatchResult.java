package org.bgee.model.gene;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * This class encapsulates a result to a gene search.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Mar. 2019
 * @since   Bgee 14, Jan. 2019
 * @see GeneMatch
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
     * @return A {@code List} of {@code GeneMatch}es that are the ordered found genes.
     */
    public List<T> getSearchMatches() {
        return searchMatches;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchMatchResult<T> that = (SearchMatchResult<T>) o;
        return totalMatchCount == that.totalMatchCount && Objects.equals(searchMatches, that.searchMatches);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalMatchCount, searchMatches);
    }

    @Override
    public String toString() {
        return "GeneMatchResult{matchCount=" + totalMatchCount 
                + ", geneMatches=" + searchMatches + "}";
    }
}
