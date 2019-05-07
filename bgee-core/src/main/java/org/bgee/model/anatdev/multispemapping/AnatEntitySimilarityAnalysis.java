package org.bgee.model.anatdev.multispemapping;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.species.Species;
import org.bgee.model.species.Taxon;

/**
 * A class allowing to store information about the similarities between a requested list
 * of anatomical entities and about the species they exist in.
 *
 * @author Frederic Bastian
 * @version Bgee 14 May 2019
 * @since Bgee 14 May 2019
 */
public class AnatEntitySimilarityAnalysis {
    private final static Logger log = LogManager.getLogger(AnatEntitySimilarityAnalysis.class.getName());

    private final Set<String> requestedAnatEntityIds;
    private final Set<String> requestedAnatEntityIdsNotFound;
    private final Set<Integer> requestedSpeciesIds;
    private final Set<Integer> requestedSpeciesIdsNotFound;
    private final Set<Species> requestedSpecies;

    private final Taxon leastCommonAncestor;
    private final Set<AnatEntitySimilarity> anatEntitySimilarities;
    private final Set<AnatEntity> anatEntitiesWithNoSimilarities;
    private final Map<AnatEntity, Set<Species>> anatEntitiesExistInSpecies;

    /**
     * @param requestedAnatEntityIds
     * @param requestedSpeciesIds
     * @param leastCommonAncestor
     * @param anatEntitySimilarities
     * @param anatEntitiesWithNoSimilarities
     * @param anatEntitiesExistInSpecies
     */
    public AnatEntitySimilarityAnalysis(Collection<String> requestedAnatEntityIds,
            Collection<String> requestedAnatEntityIdsNotFound,
            Collection<Integer> requestedSpeciesIds,
            Collection<Integer> requestedSpeciesIdsNotFound, Collection<Species> requestedSpecies,
            Taxon leastCommonAncestor, Collection<AnatEntitySimilarity> anatEntitySimilarities,
            Collection<AnatEntity> anatEntitiesWithNoSimilarities,
            Map<AnatEntity, Set<Species>> anatEntitiesExistInSpecies) {
        this.requestedAnatEntityIds = Collections.unmodifiableSet(
                requestedAnatEntityIds == null? new HashSet<>(): new HashSet<>(requestedAnatEntityIds));
        this.requestedAnatEntityIdsNotFound = Collections.unmodifiableSet(
                requestedAnatEntityIdsNotFound == null? new HashSet<>(): new HashSet<>(requestedAnatEntityIdsNotFound));
        this.requestedSpeciesIds = Collections.unmodifiableSet(
                requestedSpeciesIds == null? new HashSet<>(): new HashSet<>(requestedSpeciesIds));
        this.requestedSpeciesIdsNotFound = Collections.unmodifiableSet(
                requestedSpeciesIdsNotFound == null? new HashSet<>(): new HashSet<>(requestedSpeciesIdsNotFound));
        this.requestedSpecies = Collections.unmodifiableSet(
                requestedSpecies == null? new HashSet<>(): new HashSet<>(requestedSpecies));

        if (leastCommonAncestor == null) {
            throw log.throwing(new IllegalArgumentException(
                    "A least common ancestor taxon must be provided"));
        }
        this.leastCommonAncestor = leastCommonAncestor;
        this.anatEntitySimilarities = Collections.unmodifiableSet(
                anatEntitySimilarities == null? new HashSet<>(): new HashSet<>(anatEntitySimilarities));
        this.anatEntitiesWithNoSimilarities = Collections.unmodifiableSet(
                anatEntitiesWithNoSimilarities == null? new HashSet<>(): new HashSet<>(anatEntitiesWithNoSimilarities));
        if (anatEntitiesExistInSpecies != null && anatEntitiesExistInSpecies.values().stream()
                .anyMatch(speSet -> speSet == null || speSet.isEmpty())) {
            throw log.throwing(new IllegalArgumentException(
                    "Some anat. entities do not have species defined"));
        }
        this.anatEntitiesExistInSpecies = Collections.unmodifiableMap(
                anatEntitiesExistInSpecies == null? new HashMap<>():
                    anatEntitiesExistInSpecies.entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> e.getKey(),
                            e -> Collections.unmodifiableSet(new HashSet<>(e.getValue())))
                    ));
        if (Stream.concat(this.anatEntitySimilarities.stream()
                              .flatMap(aes -> aes.getAllAnatEntities().stream()),
                          this.anatEntitiesWithNoSimilarities.stream())
                .anyMatch(ae -> !this.anatEntitiesExistInSpecies.containsKey(ae))) {
            throw log.throwing(new IllegalArgumentException(
                    "Some anat. entities are missing from the species existence Map"));
        }
    }

    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the IDs
     *          of the anatomical entities for which similarity relations were requested.
     *          Can be empty if all similarity relations were requested for a common ancestor.
     * @see #getRequestedAnatEntityIdsNotFound()
     */
    public Set<String> getRequestedAnatEntityIds() {
        return requestedAnatEntityIds;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the IDs of anatomical entities
     *          that were requested (see {@link #getRequestedAnatEntityIds()}) but that could not
     *          be found in Bgee.
     */
    public Set<String> getRequestedAnatEntityIdsNotFound() {
        return requestedAnatEntityIdsNotFound;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code Integer}s that are the IDs of the species
     *          for which similarity relations were requested, meaning that their least common ancestor
     *          taxon was used to retrieve similarity relations (see {@link #getLeastCommonAncestor()}).
     *          Can be empty if all species in Bgee were requested, meaning that the least common
     *          ancestor of all species in Bgee was considered to retrieve the similarity relations.
     * @see #getRequestedSpeciesIdsNotFound()
     * @see #getRequestedSpecies()
     */
    public Set<Integer> getRequestedSpeciesIds() {
        return requestedSpeciesIds;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code Integer}s that are the IDs of species
     *          that were requested (see {@link #getRequestedSpeciesIds()}) but that could not
     *          be found in Bgee.
     */
    public Set<Integer> getRequestedSpeciesIdsNotFound() {
        return requestedSpeciesIdsNotFound;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code Species} corresponding to
     *          the requested species IDs (see {@link #getRequestedSpeciesIds()}) found in Bgee.
     *          If {@link #getRequestedSpeciesIds()} returns an empty {@code Set}, it means
     *          that all species in Bgee were requested, and all species in Bgee are returned by
     *          this method.
     */
    public Set<Species> getRequestedSpecies() {
        return requestedSpecies;
    }
    /**
     * @return  A {@code Taxon} that is the least common ancestor of the requested species
     *          (See {@link #getRequestedSpecies()}), and that was considered to retrieve
     *          the similarity relations.
     */
    public Taxon getLeastCommonAncestor() {
        return leastCommonAncestor;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code AnatEntitySimilarity}s for the requested
     *          anatomical entity IDs (see {@link #getRequestedAnatEntityIds()}) and requested
     *          species (see {@link #getRequestedSpecies()}).
     */
    public Set<AnatEntitySimilarity> getAnatEntitySimilarities() {
        return anatEntitySimilarities;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code AnatEntity}s for which no similarity relations
     *          was found (see {@link #getAnatEntitySimilarities()}) for the least common ancestor used
     *          (see {@link #getLeastCommonAncestor()} and requested anat. entity IDs (see
     *          {@link #getRequestedAnatEntityIds()}).
     */
    public Set<AnatEntity> getAnatEntitiesWithNoSimilarities() {
        return anatEntitiesWithNoSimilarities;
    }
    /**
     * @return  A unmodifiable {@code Map} where keys are {@code AnatEntity}s that were requested
     *          or that are part of the identified similarity relations (see
     *          {@link #getAnatEntitySimilarities()} and {@link #getAnatEntitiesWithNoSimilarities()},
     *          the associated value being an unmodifiable {@code Set} of {@code Species}
     *          where the {@code AnatEntity} exists. All {@code Species} in Bgee are considered
     *          for these taxon constraints, not only the requested {@code Species} (see 
     *          {@link #getRequestedSpecies()}).
     */
    public Map<AnatEntity, Set<Species>> getAnatEntitiesExistInSpecies() {
        return anatEntitiesExistInSpecies;
    }
}