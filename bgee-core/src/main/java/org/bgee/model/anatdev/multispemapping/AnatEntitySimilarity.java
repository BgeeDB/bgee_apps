package org.bgee.model.anatdev.multispemapping;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.species.Taxon;

/**
 * This class represents a relation of similarity between anatomical entities.
 * An {@code AnatEntitySimilarity} can hold several {@code AnatEntity}s (to report, for instance,
 * the relation of historical homology between "lung" and "swim bladder" in the Gnathostomata taxon),
 * or just one {@code AnatEntity} (to report, for instance, the homology of "brain" in the Bilateria taxon).
 * There can be several supporting information summaries provided as {@code AnatEntitySimilarityTaxonSummary}s,
 * one for each {@code Taxon} annotated for this relation: indeed, the relations can be reported
 * at several taxonomic levels depending on the available information (for instance,
 * the historical homology for the term "foregut" is reported both at the Bilateria
 * and Vertebrata levels; this adds even more support for the homology of "foregut" in Vertebrata,
 * since it benefits from the annotation in the parent taxon Bilateria).
 * 
 * @author  Philippe Moret
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 14 Mar. 2019
 * @since   Bgee 13, Apr. 2016
 */
//XXX: For now, only positive similarity annotations are meant to be used for this class,
//considering the algorithm in AnatEntitySimilarityService to retrieve them.
//Maybe negated supporting information can be retrieved independently, and never part of this class?
//Or maybe the algorithm could be changed for this class to also return negated similarity relations.
//In that case, a method 'isPositive' should be added to this class.
//See AnatEntitySimilarityTaxonSummary#isPositive() for details.
public class AnatEntitySimilarity {
    private final static Logger log = LogManager.getLogger(AnatEntitySimilarity.class.getName());

    /**
     * @see #getSourceAnatEntities()
     */
    private final Set<AnatEntity> sourceAnatEntities;
    /**
     * @see #getTransformationOfAnatEntities()
     */
    private final Set<AnatEntity> transformationOfAnatEntities;
    /**
     * @see #getRequestedTaxon()
     */
    private final Taxon requestedTaxon;
    /**
     * @see #getAnnotTaxonSummaries()
     */
    private final List<AnatEntitySimilarityTaxonSummary> annotTaxonSummaries;

    /**
     * @param sourceAnatEntities            See {@link #getSourceAnatEntities()}
     * @param transformationOfAnatEntities  See {@link #getTransformationOfAnatEntities()}
     * @param requestedTaxon                See {@link #getRequestedTaxon()}
     * @param annotTaxonSummaries           A {@code Collection} of {@code AnatEntitySimilarityTaxonSummary}s
     *                                      supporting this {@code AnatEntitySimilarity}
     *                                      for the requested {@code Taxon}.
     */
    public AnatEntitySimilarity(Collection<AnatEntity> sourceAnatEntities,
            Collection<AnatEntity> transformationOfAnatEntities, Taxon requestedTaxon,
            Collection<AnatEntitySimilarityTaxonSummary> annotTaxonSummaries) {
        log.traceEntry("{}, {}, {}, {}", sourceAnatEntities, transformationOfAnatEntities,
                requestedTaxon, annotTaxonSummaries);
        if (sourceAnatEntities == null || sourceAnatEntities.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "Summary of supporting information for this annotation must be provided"));
        }
        if (annotTaxonSummaries == null || annotTaxonSummaries.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "Source anatomical entities cannot be null or empty"));
        }
        if (requestedTaxon == null) {
            throw log.throwing(new IllegalArgumentException("Requested taxon cannot be null"));
        }

        this.sourceAnatEntities = Collections.unmodifiableSet(new HashSet<>(sourceAnatEntities));
        this.transformationOfAnatEntities = Collections.unmodifiableSet(
                transformationOfAnatEntities == null? new HashSet<>():
                    new HashSet<>(transformationOfAnatEntities));
        this.requestedTaxon = requestedTaxon;
        this.annotTaxonSummaries = Collections.unmodifiableList(
                //first, filter the summaries by using an HashSet
                new HashSet<>(annotTaxonSummaries)
                //them sort the summaries from the level of their annotated taxon
                .stream()
                .sorted(Comparator.<AnatEntitySimilarityTaxonSummary>comparingInt(
                        summary -> summary.getTaxon().getLevel()).reversed())
                .collect(Collectors.toList()));
    }

    /**
     * Return the {@code AnatEntity}s that were originally annotated for this relation of similarity.
     * Other {@code AnatEntity}s can then be added to the group thanks to 'transformation_of'
     * relations (see {@link #getTransformationOfAnatEntities()} for more details; see also
     * {@link #getAllAnatEntities()} to retrieve all {@code AnatEntity}s finally part of
     * this similarity relation).
     *
     * @return  An unmodifiable {@code Set} containing the {@code AnatEntity}s that were originally
     *          annotated for this relation of similarity. Cannot be {@code null} or empty.
     * @see #getAllAnatEntities()
     * @see #getTransformationOfAnatEntities()
     */
    public Set<AnatEntity> getSourceAnatEntities() {
        return sourceAnatEntities;
    }
    public List<AnatEntity> getSourceAnatEntitiesSortedById() {
        return sourceAnatEntities.stream().sorted(Comparator.comparing(ae -> ae.getId())).collect(Collectors.toList());
    }
    /**
     * @return  A {@code String} containing the names of the {@code AnatEntity}s
     *          returned by {@link #getSourceAnatEntities()} ordered by alphabetical orders
     *          and separated with the string ' - '.
     */
    public String getSourceAnatEntityNames() {
        return sourceAnatEntities.stream().map(ae -> ae.getName())
                .sorted()
                .collect(Collectors.joining(" - "));
    }
    /**
     * Return the {@code AnatEntity}s that were added to this relation of similarity
     * thanks to'transformation_of' relations. Indeed, similarity annotations are created only
     * for mature anatomical entities (for instance, the term "brain" is annotated,
     * not the term "future brain"). For this reason, we add to the mappings the terms
     * connected by 'transformation_of" relations to annotated anatomical entities
     * (so that we use data in both "brain" and "future brain").
     * <p>
     * See {@link #getAllAnatEntities()} to retrieve all {@code AnatEntity}s finally part of
     * this similarity relation, and {@link #getSourceAnatEntities()} to retrieve the source
     * {@code AnatEntity}s originally annotated.
     *
     * @return  An unmodifiable {@code Set} containing the {@code AnatEntity}s that were added
     *          to this relation of similarity thanks to'transformation_of' relations. Can be empty
     *          if not anatomical entities were added this way. 
     * @see #getAllAnatEntities()
     * @see #getSourceAnatEntities()
     */
    public Set<AnatEntity> getTransformationOfAnatEntities() {
        return transformationOfAnatEntities;
    }
    /**
     * @return  A {@code String} containing the names of the {@code AnatEntity}s
     *          returned by {@link #getTransformationOfAnatEntities()} ordered by alphabetical orders
     *          and separated with the string ' - '.
     */
    public String getTransformationOfAnatEntityNames() {
        return transformationOfAnatEntities.stream().map(ae -> ae.getName())
                .sorted()
                .collect(Collectors.joining(" - "));
    }
    /**
     * Return all {@code AnatEntity}s that are part of this {@code AnatEntitySimilarity} relation.
     * This is a helper method to retrieve the union of the {@code AnatEntity}s returned by
     * {@link #getSourceAnatEntities()} and {@link #getTransformationOfAnatEntities()}.
     *
     * @return  A {@code Set} containing all {@code AnatEntity}s that are part of
     *          this {@code AnatEntitySimilarity} relation, by considering the source
     *          {@code AnatEntity}s annotated (see {@link #getSourceAnatEntities()}),
     *          and the {@code AnatEntity}s added thanks to 'transformation_of' relations
     *          (see {@link #getTransformationOfAnatEntities()}). Cannot be {@code null}
     *          or empty.
     */
    public Set<AnatEntity> getAllAnatEntities() {
        return Stream.concat(
                this.getSourceAnatEntities().stream(),
                this.getTransformationOfAnatEntities().stream())
                .collect(Collectors.toSet());
    }
    public List<AnatEntity> getAllAnatEntitiesSortedById() {
        return getAllAnatEntities().stream().sorted(Comparator.comparing(ae -> ae.getId())).collect(Collectors.toList());
    }
    /**
     * @return  A {@code String} containing the names of the {@code AnatEntity}s
     *          returned by {@link #getAllAnatEntities()} ordered by alphabetical orders
     *          and separated with the string ' - '.
     */
    public String getAllAnatEntityNames() {
        return getAllAnatEntities().stream().map(ae -> ae.getName())
                .sorted()
                .collect(Collectors.joining(" - "));
    }
    /**
     * Return the {@code Taxon} that was considered to retrieve the anatomical entity similarity.
     * This impacts the supporting information provided (see {@link #getAnnotTaxonSummaries()}),
     * since only the information annotated to the requested taxon or its ancestors are retrieved
     * (and not potential information annotated to descendant taxa of the requested taxon).
     *
     * @return  The {@code Taxon} that was considered to build this {@code AnatEntitySimilarity}.
     *          Cannot be {@code null}.
     */
    public Taxon getRequestedTaxon() {
        return requestedTaxon;
    }
    /**
     * Returns the {@code AnatEntitySimilarityTaxonSummary}s supporting this anatomical entity similarity.
     * This can contain several supporting information summaries provided, one for each {@code Taxon}
     * annotated for this relation: indeed, the relations can be reported at several taxonomic levels
     * depending on the available information (for instance, the historical homology for the term
     * "foregut" is reported both at the Bilateria and Vertebrata levels; this adds even more support
     * for the homology of "foregut" in Vertebrata, since it benefits from the annotation
     * in the parent taxon Bilateria).
     * <p>
     * Note that only summary information annotated to the requested taxon or one of its ancestors
     * are considered here, and not information that may exist annotated to descendant taxa
     * of the requested taxon.
     * <p>
     * Only positive supporting information are stored (while others, negated, supporting information
     * can exist).
     * <p>
     * The {@code AnatEntitySimilarityTaxonSummary}s are returned as a {@code List} sorted from
     * the lowest taxa (the taxa closest to the requested taxon, that can be the requested taxon themselves)
     * to the highest taxa (the taxa closest to the root of the taxonomy). For instance,
     * in the case of the similarity of "foregut", the first summary information will be for
     * the Vertebrata level, the second one for the Bilateria level. So we should see the information
     * with most support first, since it benefits from the supporting information in higher taxa.
     *
     * @return  An unmodifiable {@code List} of {@code AnatEntitySimilarityTaxonSummary}s sorted by
     *          the taxon they annotate, sorted from the lowest taxa to the highest taxa.
     *          Should always be non-{@code null} and containing at least one
     *          {@code AnatEntitySimilarityTaxonSummary}.
     */
    public List<AnatEntitySimilarityTaxonSummary> getAnnotTaxonSummaries() {
        return annotTaxonSummaries;
    }

    /**
     * @return  A {@code boolean} that is {@code true} if the similarity relation have enough support
     *          to be considered reliable, {@code false} otherwise. {@code true} if at least
     *          one of the underlying {@code AnatEntitySimilarityTaxonSummary}s (see {@link 
     *          #getAnnotTaxonSummaries()}) is trusted.
     * @see #getAnnotTaxonSummaries()
     */
    public boolean isTrusted() {
        return this.getAnnotTaxonSummaries().stream()
                //XXX: for now, this class is meant to only manage positive similarity annotations,
                //but just in case... This might change, see comment at the begining of this class
                .filter(summary -> summary.isPositive())
                .anyMatch(summary -> summary.isTrusted());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((annotTaxonSummaries == null) ? 0 : annotTaxonSummaries.hashCode());
        result = prime * result + ((requestedTaxon == null) ? 0 : requestedTaxon.hashCode());
        result = prime * result + ((sourceAnatEntities == null) ? 0 : sourceAnatEntities.hashCode());
        result = prime * result
                + ((transformationOfAnatEntities == null) ? 0 : transformationOfAnatEntities.hashCode());
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
        AnatEntitySimilarity other = (AnatEntitySimilarity) obj;
        if (annotTaxonSummaries == null) {
            if (other.annotTaxonSummaries != null) {
                return false;
            }
        } else if (!annotTaxonSummaries.equals(other.annotTaxonSummaries)) {
            return false;
        }
        if (requestedTaxon == null) {
            if (other.requestedTaxon != null) {
                return false;
            }
        } else if (!requestedTaxon.equals(other.requestedTaxon)) {
            return false;
        }
        if (sourceAnatEntities == null) {
            if (other.sourceAnatEntities != null) {
                return false;
            }
        } else if (!sourceAnatEntities.equals(other.sourceAnatEntities)) {
            return false;
        }
        if (transformationOfAnatEntities == null) {
            if (other.transformationOfAnatEntities != null) {
                return false;
            }
        } else if (!transformationOfAnatEntities.equals(other.transformationOfAnatEntities)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AnatEntitySimilarity [sourceAnatEntities=").append(sourceAnatEntities)
               .append(", transformationOfAnatEntities=").append(transformationOfAnatEntities)
               .append(", requestedTaxon=").append(requestedTaxon)
               .append(", annotTaxonSummaries=").append(annotTaxonSummaries)
               .append("]");
        return builder.toString();
    }
}