package org.bgee.model.gene;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.NamedEntity;

/**
 * This class encapsulates a result to a named entity search. A match can either be on id,
 * name or description.
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
public class NamedEntityMatch<T extends NamedEntity<U>, U extends Comparable<U>> extends SearchMatch implements Comparable<NamedEntityMatch<T,U>> {

    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(GeneMatch.class.getName());

    public enum MatchSource {
        ID, NAME, DESCRIPTION, MULTIPLE
    }

    private final T namedEntity;

    private final MatchSource matchSource;

    public NamedEntityMatch(T namedEntity, String term, MatchSource matchSource) {
        super(term);
        if (namedEntity == null) {
            throw new IllegalArgumentException("A Named Entity must be provided.");
        }
        this.namedEntity = namedEntity;
        this.matchSource = matchSource;
    }

    /**
     * @return A {@code NamedEntity} that was matched by the search.
     */
    public T getNamedEntity() {
        return namedEntity;
    }

    /**
     * @return A {@code MatchSource} representing how the named entity was identified from the search term.
     */
    public MatchSource getMatchSource() {
        return matchSource;
    }

    /**
     * @return  A {@code String} representing the match.
     */
    public String getMatch() {
        log.traceEntry();
        switch (this.getMatchSource()) {
            case NAME:
                return log.traceExit(this.getNamedEntity().getName());
            case ID:
                return log.traceExit(this.getNamedEntity().getId().toString());
            case DESCRIPTION:
                return log.traceExit(this.getNamedEntity().getDescription());
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
        int result = super.hashCode();
        result = prime * result + Objects.hash(matchSource, namedEntity);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        NamedEntityMatch<T,U> other = (NamedEntityMatch<T,U>) obj;
        return matchSource == other.matchSource && Objects.equals(namedEntity, other.namedEntity);
    }

    @Override
    //TODO: write a cleaner version of this comparator.
    //XXX: probably better to compare name than ID
    public int compareTo(NamedEntityMatch<T,U> gm) {
        if(this.getMatchLength().compareTo(gm.getMatchLength()) < 0) {
            return -1;
        }
        if(this.getMatchLength().compareTo(gm.getMatchLength()) > 0) {
            return 1;
        }
        if(this.getMatchSource().compareTo(gm.getMatchSource()) < 0) {
            return -1;
        }
        if(this.getMatchSource().compareTo(gm.getMatchSource()) > 0) {
            return 1;
        }
        if(this.getNamedEntity().getId().compareTo(gm.getNamedEntity().getId()) < 0) {
            return -1;
        }
        if(this.getNamedEntity().getId().compareTo(gm.getNamedEntity().getId()) > 0) {
            return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AnatEntityMatch [").append(super.toString()).append(", namedEntity=").append(namedEntity)
                .append(", matchSource=").append(matchSource)
                .append("]");
        return builder.toString();
    }
}

