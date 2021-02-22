package org.bgee.model.gene;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;

/**
 * This class encapsulates a result to a gene search. A match can either be on gene id,
 * gene name, synonym, description or x-ref.
 * <p>
 * Note that this class implements {@code Comparable<GeneMatch>}, allowing to perform 
 * simple comparisons based on the attributes of this class 
 * (matched term length, then species preferred display order, then term matched).
 * 
 * @author  Philippe Moret
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, May 2019
 * @since   Bgee 13, July 2016
 * @see GeneMatchResult
 */
public class GeneMatch implements Comparable<GeneMatch> {

    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(GeneMatch.class.getName());

    private static final Comparator<GeneMatch> GENE_MATCH_COMPARATOR = Comparator
            .comparing(GeneMatch::getMatchLength, Comparator.nullsLast(Integer::compareTo))
            .thenComparing(gm -> gm.getGene().getSpecies().getPreferredDisplayOrder(), Comparator.nullsLast(Integer::compareTo))
            .thenComparing(gm -> gm.getMatchSource().ordinal(), Comparator.nullsLast(Integer::compareTo))
            .thenComparing(GeneMatch::getMatch, Comparator.nullsLast(String::compareTo));

    public enum MatchSource {
        ID, NAME, DESCRIPTION, SYNONYM, XREF, MULTIPLE
    }

    private final Gene gene;
    private final String term;
    private final MatchSource matchSource;

	public GeneMatch(Gene gene, String term, MatchSource matchSource) {
	    if (gene == null || matchSource == null) {
	        throw new IllegalArgumentException("A Gene and a MatchSource must be provided.");
	    }
		this.gene = gene;
		this.term = term;
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
	 * @return  A {@code String} representing the matched synonym or x-ref.
     *          It is null when there is no synonym or x-ref match.
	 */
	public String getTerm() {
		return term;
	}

    /**
     * @return  A {@code String} representing the match.
     */
    public String getMatch() {
	    log.traceEntry();
        switch (this.getMatchSource()) {
            case NAME:
                return log.traceExit(this.getGene().getName());
            case ID:
                return log.traceExit(this.getGene().getEnsemblGeneId());
            case DESCRIPTION:
                return log.traceExit(this.getGene().getDescription());
            case SYNONYM:
            case XREF:
                return log.traceExit(this.getTerm());
            case MULTIPLE:
                return log.traceExit((String) null);
            default:
                throw log.throwing(new IllegalStateException("Unrecognized MatchSource: " + this.getMatchSource()));
        }
    }

    /**
     * @return  An {@code Integer} that is the length of the match.
     *          Returns {@code null}, if the match comes from multiple sources.
     */
    private Integer getMatchLength() {
	    log.traceEntry();
	    if (this.getMatch() == null) {
	        return null;
        }
        return log.traceExit(this.getMatch().length());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + gene.hashCode();
        result = prime * result + ((term == null) ? 0 : term.hashCode());
        result = prime * result + matchSource.hashCode();
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
        if (!gene.equals(other.gene)) {
            return false;
        }
        if (term == null) {
            if (other.term != null) {
                return false;
            }
        } else if (!term.equals(other.term)) {
            return false;
        }
        if (!matchSource.equals(other.matchSource)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(GeneMatch gm) {
        return GENE_MATCH_COMPARATOR.compare(this, gm);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("GeneMatch [gene=").append(gene).append(", term=").append(term)
        .append(", matchSource=").append(matchSource)
        .append("]");
        return builder.toString();
    }
}
