package org.bgee.model.search;

import java.util.Comparator;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.NamedEntity;
import org.bgee.model.gene.Gene;

/**
 * This class encapsulates a result to a {@code NamedEntity} or a {@code Gene} search.
 * The sources of match are listed in {@link MatchSource}.
 * <p>
 * Note that this class implements the {@code Comparable} interface.
 *
 * @param <T>   {@code T} must be either an instance of {@code Gene}, or of {@code NamedEntity},
 *              and although we cannot declare such a generic type extending either one class or another,
 *              a check is performed in the constructor.
 * @author  Philippe Moret
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @author  Julien Wollbrett
 * @version Bgee 15.0, Oct. 2022
 * @since   Bgee 13, July 2016
 * @see SearchMatchResult
 */
public class SearchMatch<T> implements Comparable<SearchMatch<T>> {
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

    //The order of these Enum fields is important as it is used in the above Comparators
    public enum MatchSource {
        ID, NAME, DESCRIPTION, SYNONYM, XREF, MULTIPLE
    }

    private final T searchedObject;

    private final MatchSource matchSource;

    private final String term;

    private final Class<T> type;

    /**
     * @param searchedObject    An {@code Object} of type {@code T}. {@code T} must be an instance either
     *                          of {@code Gene}, or of {@code NamedEntity}, a check is performed.
     * @param term              A {@code String} that is the matching term that allowed to retrieve
     *                          {@code searchedObject}.
     * @param matchSource       A {@code MatchSource} indicating the source of the matched {@code term}.
     * @param type              The {@code Class} {@code T} of {@code searchedObject}.
     * @throws IllegalArgumentException If {@code searchedObject} or {@code type} are null,
     *                                  or if {@code T} is not assignable to {@code Gene}
     *                                  or {@code NamedEntity}.
     */
    public SearchMatch(T searchedObject, String term, MatchSource matchSource, Class<T> type)
    throws IllegalArgumentException {
        this.term = term;
        if (searchedObject == null) {
            throw new IllegalArgumentException("A searched object must be provided.");
        }
        if (type == null) {
            throw new IllegalArgumentException("A type must be provided.");
        }
        if(!Gene.class.isAssignableFrom(type) && !NamedEntity.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException("searchedObject must be an instance of Gene"
                    + "or NamedEntity");
        }
        this.searchedObject = searchedObject;
        this.matchSource = matchSource;
        this.type = type;
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
     * @return  A {@code Class} representing the type of T.
     */
    public Class<T> getType() {
        return type;
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
                case SYNONYM:
                case XREF:
                    return log.traceExit(this.getTerm());
                case MULTIPLE:
                    return log.traceExit((String) null);
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

    //The generic types are actually checked, thanks to the getType() method,
    //the compiler just does not know it
    @SuppressWarnings("unchecked")
    @Override
    public int compareTo(SearchMatch<T> sm) {
        if (Gene.class.isAssignableFrom(this.getType()) &&
                Gene.class.isAssignableFrom(sm.getType())) {
            return GENE_MATCH_COMPARATOR.compare( (SearchMatch<Gene>)this,
                    (SearchMatch<Gene>)sm);
        }
        if (NamedEntity.class.isAssignableFrom(this.getType()) &&
                NamedEntity.class.isAssignableFrom(sm.getType())) {
            return NAMED_ENTITX_MATCH_COMPARATOR.compare( (SearchMatch<NamedEntity<?>>)this,
                    (SearchMatch<NamedEntity<?>>) sm);
        }
        throw new IllegalArgumentException("Not possible to compare 2 objects with searchedObject"
                + " being an instance of 2 different class");
    }

    @Override
    public int hashCode() {
        return Objects.hash(matchSource, searchedObject, term, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SearchMatch<?> other = (SearchMatch<?>) obj;
        return matchSource == other.matchSource && Objects.equals(searchedObject, other.searchedObject)
                && Objects.equals(term, other.term) && Objects.equals(type, other.type);
    }


}

