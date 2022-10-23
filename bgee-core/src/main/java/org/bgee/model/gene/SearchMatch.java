package org.bgee.model.gene;

import java.util.Comparator;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.NamedEntity;
import org.bgee.model.anatdev.AnatEntity;

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
public class SearchMatch<T> implements Comparable<SearchMatch<T>> {

    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(SearchMatch.class.getName());

    private static final Comparator<SearchMatch<Gene>> GENE_MATCH_COMPARATOR = Comparator
            .comparing(SearchMatch<Gene>::getMatchLength, Comparator.nullsLast(Integer::compareTo))
            .thenComparing(gm -> gm.getSearchedObject().getSpecies().getPreferredDisplayOrder(),
                    Comparator.nullsLast(Integer::compareTo))
            .thenComparing(gm -> gm.getMatchSource().ordinal(), Comparator.nullsLast(Integer::compareTo))
            .thenComparing(SearchMatch<Gene>::getMatch, Comparator.nullsLast(String::compareTo));

    private static final Comparator<SearchMatch<NamedEntity<?>>> NAMED_ENTITX_MATCH_COMPARATOR = Comparator
            .comparing(SearchMatch<NamedEntity<?>>::getMatchLength, Comparator.nullsLast(Integer::compareTo))
            .thenComparing(gm -> gm.getMatchSource().ordinal(), Comparator.nullsLast(Integer::compareTo))
            .thenComparing(SearchMatch<NamedEntity<?>>::getMatch, Comparator.nullsLast(String::compareTo));

    public enum MatchSource {
        ID, NAME, DESCRIPTION, SYNONYM, XREF, MULTIPLE
    }

    private final T searchedObject;

    private final MatchSource matchSource;
    
    private final String term;

    public SearchMatch(T searchedObject, String term, MatchSource matchSource) {
        this.term = term;
        if (searchedObject == null) {
            throw new IllegalArgumentException("A searched object must be provided.");
        }
        if(!(searchedObject instanceof Gene) && !(searchedObject instanceof AnatEntity)) {
            throw new IllegalArgumentException("searchedObject must be an instance of Gene"
                    + "or NamedEntity");
        }
        this.searchedObject = searchedObject;
        this.matchSource = matchSource;
    }

    /**
     * @return A {@code NamedEntity} that was matched by the search.
     */
    public T getSearchedObject() {
        return searchedObject;
    }

    /**
     * @return A {@code MatchSource} representing how the named entity was identified from the
     * search term.
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
        if(this.getSearchedObject() instanceof NamedEntity<?>) {
            NamedEntity<?> namedEntity = (NamedEntity<?>) this.searchedObject;
            switch (this.getMatchSource()) {
                case NAME:
                    return log.traceExit(namedEntity.getName());
                case ID:
                    return log.traceExit(namedEntity.getId().toString());
                case DESCRIPTION:
                    return log.traceExit(namedEntity.getDescription());
                default:
                    throw log.throwing(new IllegalStateException("Unrecognized MatchSource: "
                            + this.getMatchSource()));
            }
        }
        if (this.getSearchedObject() instanceof Gene) {
            Gene gene = (Gene) this.searchedObject;
            switch (this.getMatchSource()) {
                case NAME:
                    return log.traceExit(gene.getName());
                case ID:
                    return log.traceExit(gene.getGeneId());
                case DESCRIPTION:
                    return log.traceExit(gene.getDescription());
                case SYNONYM:
                case XREF:
                    return log.traceExit(this.getTerm());
                case MULTIPLE:
                    return log.traceExit((String) null);
                default:
                    throw log.throwing(new IllegalStateException("Unrecognized MatchSource: " +
                            this.getMatchSource()));
            }
        }
        throw log.throwing(new IllegalStateException("Not yet implemented for class" +
                this.getSearchedObject().getClass()));
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
    public int compareTo(SearchMatch<T> gm) {
        if (gm.getSearchedObject() instanceof Gene &&
                this.getSearchedObject() instanceof Gene) {
            return GENE_MATCH_COMPARATOR.compare( (SearchMatch<Gene>)this,
                    (SearchMatch<Gene>)gm);
        }
        if (gm.getSearchedObject() instanceof NamedEntity<?> &&
                this.getSearchedObject() instanceof NamedEntity<?>) {
            return NAMED_ENTITX_MATCH_COMPARATOR.compare( (SearchMatch<NamedEntity<?>>)this,
                    (SearchMatch<NamedEntity<?>>)gm);
        }
        throw new IllegalArgumentException("Not possible to compare 2 objects with searchedObject"
                + " being an instance of 2 different class");
    }

    @Override
    public int hashCode() {
        return Objects.hash(matchSource, searchedObject, term);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SearchMatch<T> other = (SearchMatch<T>) obj;
        return matchSource == other.matchSource && Objects.equals(searchedObject, other.searchedObject)
                && Objects.equals(term, other.term);
    }


}

