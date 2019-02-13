package org.bgee.model.gene;

/**
 * This class encapsulates a result to a gene search. A match can either be on the gene name
 * or a synonym. 
 * @author Philippe Moret
 * @author Frederic Bastian
 * @version Bgee 13 July 2016
 *
 */
public class GeneMatch {
    public enum MatchSource {
        ID, NAME, SYNONYM;
    }

    private final Gene gene;
    private final String synonym;
    private final MatchSource matchSource;

	public GeneMatch(Gene gene, String synonym, MatchSource matchSource) {
	    if (gene == null || matchSource == null) {
	        throw new IllegalArgumentException("A Gene and a MatchSource must be provided.");
	    }
		this.gene = gene;
		this.synonym = synonym;
		this.matchSource = matchSource;
	}

	/**
	 * @return A {@code Gene} that was matched by the search.
	 */
	public Gene getGene() {
		return gene;
	}
	/**
	 * @return A {@code MatchSource} representing how the gene was identified from the search term.
	 */
	public MatchSource getMatchSource() {
	    return matchSource;
	}
	/**
	 * @return A {@code String} representing the matched synonym, null when there is no synonym match
	 */
	public String getMatchedSynonym() {
		return synonym;
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((gene == null) ? 0 : gene.hashCode());
        result = prime * result + ((synonym == null) ? 0 : synonym.hashCode());
        result = prime * result + ((matchSource == null) ? 0 : matchSource.hashCode());
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
        GeneMatch other = (GeneMatch) obj;
        if (gene == null) {
            if (other.gene != null) {
                return false;
            }
        } else if (!gene.equals(other.gene)) {
            return false;
        }
        if (synonym == null) {
            if (other.synonym != null) {
                return false;
            }
        } else if (!synonym.equals(other.synonym)) {
            return false;
        }
        if (matchSource == null) {
            if (other.matchSource != null) {
                return false;
            }
        } else if (!matchSource.equals(other.matchSource)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("GeneMatch [gene=").append(gene).append(", synonym=").append(synonym)
        .append(", matchSource=").append(matchSource)
        .append("]");
        return builder.toString();
    }
}
