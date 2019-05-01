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
public class GeneMatchResult {

    private final int totalMatchCount;
    private final List<GeneMatch> geneMatches;

	public GeneMatchResult(int totalMatchCount, List<GeneMatch> geneMatches) {
        if (totalMatchCount < 0) {
	        throw new IllegalArgumentException("The count of matches must be provided.");
	    }
        if (geneMatches != null && geneMatches.size() > totalMatchCount) {
            throw new IllegalArgumentException(
                    "The number of matches must be equals or inferior to the match count.");
        }
        this.totalMatchCount = totalMatchCount;
        this.geneMatches = geneMatches == null? null : Collections.unmodifiableList(geneMatches);
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
    public List<GeneMatch> getGeneMatches() {
        return geneMatches;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeneMatchResult that = (GeneMatchResult) o;
        return totalMatchCount == that.totalMatchCount && Objects.equals(geneMatches, that.geneMatches);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalMatchCount, geneMatches);
    }

    @Override
    public String toString() {
        return "GeneMatchResult{matchCount=" + totalMatchCount 
                + ", geneMatches=" + geneMatches + "}";
    }
}
