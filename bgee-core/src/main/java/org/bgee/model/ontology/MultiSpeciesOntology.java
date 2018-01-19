package org.bgee.model.ontology;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.NamedEntity;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.TaxonConstraint;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;

/**
 * Class allowing to describe a multi-species ontology, or the sub-graph of a multi-species ontology.
 * 
 * @author  Valentine Rech de Laval
 * @author Frederic Bastian
 * @version Bgee 14 Mar. 2017
 * @since   Bgee 13, July 2016
 * @param <T>   The type of element in this ontology or sub-graph.
 * @param <U>   The type of ID of the elements in this ontology or sub-graph.
*/
public class MultiSpeciesOntology<T extends NamedEntity<U> & OntologyElement<T, U>, U> 
    extends OntologyBase<T, U> {

    private static final Logger log = LogManager.getLogger(MultiSpeciesOntology.class.getName());

    /**
     * A {@code Set} of {@code TaxonConstraint}s that are taxon constrains on relations 
     * between {@code OntologyElement}s of this ontology.
     */
    private final Set<TaxonConstraint<Integer>> relationTaxonConstraints;
    
    /**
     * A {@code Map} where keys are {@code Integer}s representing species IDs,
     * the associated value being a {@code Set} of {@code RelationTO}s present in {@link #relations},
     * valid in the related species.
     * <p>
     * A {@code null} key means: relations valid in any species.
     */
    private final Map<Integer, Set<RelationTO<U>>> relationsBySpeciesId;

    /**
     * A {@code Set} of {@code TaxonConstraint}s that are taxon constrains on 
     * {@code OntologyElement}s of this ontology.
     */
    private final Set<TaxonConstraint<U>> entityTaxonConstraints;
    /**
     * A {@code Map} where keys are {@code Integer}s representing species IDs,
     * the associated value being a {@code Set} of {@code T}s present in {@link #getElements()},
     * valid in the related species.
     * <p>
     * A {@code null} key means: entities valid in any species.
     */
    private final Map<Integer, Set<T>> entitiesBySpeciesId;

    /**
     * @see #getSpeciesIds()
     */
    private final Set<Integer> speciesIds;

    /**
     * Constructor providing all parameters of this multi-species ontology.
     * 
     * @param speciesIds                A {@code Collection} of {@code Integer}s that are the IDs of 
     *                                  the species describing this multi-species ontology.
     * @param elements                  A {@code Collection} of {@code T}s that are
     *                                  the elements of this ontology.
     * @param relations                 A {@code Collection} of {@code RelationTO}s that are
     *                                  the relations between elements of the ontology.
     * @param taxonConstraints          A {@code Collection} of {@code TaxonConstraint}s that are 
     *                                  taxon constrains on {@code OntologyElement}s of this ontology.
     * @param relationTaxonConstraints  A {@code Collection} of {@code TaxonConstraint}s that are
     *                                  taxon constrains on relations between {@code OntologyElement}s
     *                                  of this ontology.
     * @param relationTypes             A {@code Collection} of {@code RelationType}s that were
     *                                  considered to build this ontology or sub-graph.
     * @param serviceFactory            A {@code ServiceFactory} to acquire {@code Service}s from.
     * @param type                      A {@code Class<T>} that is the type of {@code elements} 
     *                                  to be store by this {@code MultiSpeciesOntology}.
     */
    protected MultiSpeciesOntology(Collection<Integer> speciesIds, Collection<T> elements, 
            Collection<RelationTO<U>> relations, Collection<TaxonConstraint<U>> taxonConstraints, 
            Collection<TaxonConstraint<Integer>> relationTaxonConstraints, 
            Collection<RelationType> relationTypes,
            ServiceFactory serviceFactory, Class<T> type) {
        super(elements, relations, relationTypes, serviceFactory, type);
        log.entry(speciesIds, elements, relations, taxonConstraints, relationTaxonConstraints, 
                relationTypes, serviceFactory, type);
        long startTimeInMs = System.currentTimeMillis();
        log.debug("Start creation of MultiSpeciesOntology");

        this.speciesIds = Collections.unmodifiableSet(
                speciesIds == null? new HashSet<>(): new HashSet<>(speciesIds));
        this.relationTaxonConstraints = Collections.unmodifiableSet(
                relationTaxonConstraints == null? new HashSet<>(): new HashSet<>(relationTaxonConstraints));
        this.entityTaxonConstraints = Collections.unmodifiableSet(
                taxonConstraints == null? new HashSet<>(): new HashSet<>(taxonConstraints));

        //then store valid entities per species
        Map<Integer, Set<T>> entitiesBySpeciesId = new HashMap<>();
        if (this.entityTaxonConstraints.isEmpty()) {
            //no taxon constraints provided, we consider all entities are valid in all species.
            entitiesBySpeciesId.put(null, this.getElements());
        } else {
            //Sadly, Collectors.groupingBy methods do not accept null keys
            entitiesBySpeciesId = this.entityTaxonConstraints.stream()
                .collect(Collectors.toMap(
                        tc -> tc.getSpeciesId(),
                        tc -> {
                            T element = this.getElement(tc.getEntityId());
                            if (element != null) {
                                return new HashSet<>(Arrays.asList(element));
                            }
                            return new HashSet<>();
                        },
                        (v1, v2) -> {v1.addAll(v2); return v1;}
                 ));
        }
        this.entitiesBySpeciesId = Collections.unmodifiableMap(
                entitiesBySpeciesId.entrySet().stream()
                    .collect(Collectors.toMap(e -> e.getKey(), 
                            e -> Collections.unmodifiableSet((e.getValue())))));
        log.trace("Entities by speciesId: {}", this.entitiesBySpeciesId);
        
        Map<Integer, Set<RelationTO<U>>> relationsBySpeciesId = null;
        if (this.relationTaxonConstraints.isEmpty()) {
            log.trace("Inferring relation taxon constraints from entity taxon constraints");
            //no relation taxon constraints provided, so we'll try to use the entity taxon constraints:
            //both the source and the target need to be valid in a species for the relation to be valid
            //in that species.
            //We generate a Map to easily retrieve all species an entity is valid in from its ID.
            //we use entitiesBySpeciesId because we already made modifications above using it.
            //Sadly, Collectors.groupingBy methods do not accept null keys,
            //and Collectors.toMap does not support null values (see http://stackoverflow.com/a/24634007/1768736)
            final Map<U, Set<Integer>> speIdsByEntityId =  this.entitiesBySpeciesId.entrySet().stream()
                    .flatMap(e -> e.getValue().stream()
                              .map(entity -> new AbstractMap.SimpleEntry<>(entity.getId(), e.getKey())))
                    .collect(HashMap::new, 
                            (m, e2) -> m.put(e2.getKey(), new HashSet<>(Arrays.asList(e2.getValue()))),
                            (m1, m2) -> {
                                for (Entry<U, Set<Integer>> e2: m2.entrySet()) {
                                    Set<Integer> m1Value = m1.get(e2.getKey());
                                    if (m1Value != null) {
                                        m1Value.addAll(e2.getValue());
                                    } else {
                                        m1.put(e2.getKey(), e2.getValue());
                                    }
                                }
                            });
            log.trace("Species IDs by entity ID: {}", speIdsByEntityId);
            
            //Sadly, Collectors.groupingBy methods do not accept null keys, so we use toMap
            relationsBySpeciesId = this.getRelations().stream()
            .flatMap(rel -> {
                final Set<Integer> speIdsSource = speIdsByEntityId.get(rel.getSourceId());
                if (speIdsSource == null) {
                    throw log.throwing(new IllegalArgumentException(
                            "Missing taxon constraints for source of relation " + rel));
                }
                final Set<Integer> speIdsTarget = speIdsByEntityId.get(rel.getTargetId());
                if (speIdsTarget == null) {
                    throw log.throwing(new IllegalArgumentException(
                            "Missing taxon constraints for target of relation " + rel));
                }
                
                if (speIdsSource.contains(null) && speIdsTarget.contains(null)) {
                    //if both source and target valid in all species
                    return Stream.of(new AbstractMap.SimpleEntry<Integer, RelationTO<U>>(null, rel));
                } else if (speIdsSource.contains(null)) {
                    //if source valid in all species but not target, restrain the relation
                    //based on target restriction
                    return speIdsTarget.stream().map(id -> new AbstractMap.SimpleEntry<Integer, RelationTO<U>>(id, rel));
                } else if (speIdsTarget.contains(null)) {
                    //if target valid in all species but not source, restrain the relation
                    //based on source restriction
                    return speIdsSource.stream().map(id -> new AbstractMap.SimpleEntry<Integer, RelationTO<U>>(id, rel));
                } else {
                    //both source and target have restrictions, keep the intersection of valid species
                    return speIdsSource.stream()
                            .filter(sourceSpeId -> speIdsTarget.contains(sourceSpeId))
                            .map(id -> new AbstractMap.SimpleEntry<Integer, RelationTO<U>>(id, rel));
                }
            }).collect(Collectors.toMap(
                    e -> e.getKey(), 
                    e -> new HashSet<>(Arrays.asList(e.getValue())),
                    (v1, v2) -> {v1.addAll(v2); return v1;}
            ));
            
        } else {
            log.trace("Retrieving relation taxon constraints from data source.");
            //first, build a Map to easily retrieve a relation from its ID
            final Map<Integer, RelationTO<U>> relMap = this.getRelations().stream()
                    .collect(Collectors.toMap(r -> r.getId(), r -> r));
            //then store valid relations per species
            //Sadly, Collectors.groupingBy methods do not accept null keys, so we use toMap
            relationsBySpeciesId = this.relationTaxonConstraints.stream()
                    .collect(Collectors.toMap(
                            tc -> tc.getSpeciesId(),
                            tc -> {
                                RelationTO<U> rel = relMap.get(tc.getEntityId());
                                //maybe the relation taxon constraint is about a relation
                                //that we filtered out (e.g., develops_from relation),
                                //so we just return empty Set in that case.
                                if (rel == null) {
                                    return new HashSet<>();
                                }
                                return new HashSet<>(Arrays.asList(rel));
                            },
                            (v1, v2) -> {v1.addAll(v2); return v1;}
                    ));
        }
        log.trace("Relations by species ID: {}", relationsBySpeciesId);
        this.relationsBySpeciesId = Collections.unmodifiableMap(
                relationsBySpeciesId.entrySet().stream()
                    .collect(Collectors.toMap(e -> e.getKey(), 
                            e -> Collections.unmodifiableSet(e.getValue()))));

        log.debug("MultiSpeciesOntology created in {} ms", System.currentTimeMillis() - startTimeInMs);
        log.exit();
    }
    
    /**
     * @return  The {@code Set} of {@code Integer}s that are the IDs of the species
     *          that were considered to build this ontology or sub-graph.
     */
    public Set<Integer> getSpeciesIds() {
        return speciesIds;
    }

    /**
     * Get elements that were considered to build this ontology or sub-graph,
     * filtered by {@code speciesIds}.
     * 
     * @param speciesIds    A {@code Collection} of {@code Integer}s that is the IDs of species
     *                      allowing to filter the elements to retrieve.
     * @return              The {@code Set} of {@code T}s that are the elements that were considered 
     *                      to build this ontology or sub-graph, filtered by {@code speciesId}.
     */
    public  Set<T> getElements(Collection<Integer> speciesIds) {
        log.entry(speciesIds);
        
        if (speciesIds == null || speciesIds.isEmpty()) {
            //copy the Set so that it becomes modifiable, to be consistent with use of Streams
            //in other methods
            return new HashSet<>(this.getElements());
        }
        
        Set<T> retrievedElements = speciesIds.stream()
                .flatMap(id -> {
                    Set<T> speElements = this.entitiesBySpeciesId.get(id);
                    if (speElements == null) {
                        return Stream.empty();
                    }
                    return speElements.stream();
                })
                .collect(Collectors.toSet());

        //finally, add elements valid in all species (mapped to key null)
        Set<T> allSpeElements = this.entitiesBySpeciesId.get(null);
        if (allSpeElements != null) {
            retrievedElements.addAll(allSpeElements);
        }

        return log.exit(retrievedElements);
    }
    
    /**
     * Get relations that were considered to build this ontology or sub-graph,
     * filtered by {@code speciesIds}.
     * 
     * @param speciesIds    A {@code Collection} of {@code Integer}s that is the IDs of species
     *                      allowing to filter the relations to retrieve.
     * @return              The {@code Set} of {@code RelationTO}s that are the relations that were
     *                      considered to build this ontology or sub-graph, filtered by {@code speciesId}.
     */
    private  Set<RelationTO<U>> getRelations(Collection<Integer> speciesIds) {
        log.entry(speciesIds);
        
        if (speciesIds == null || speciesIds.isEmpty()) {
            //copy the Set so that it becomes modifiable, to be consistent with use of Streams below
            return new HashSet<>(this.getRelations());
        }
        
        Set<RelationTO<U>> retrievedRels = speciesIds.stream()
                .flatMap(id -> {
                    Set<RelationTO<U>> speRels = this.relationsBySpeciesId.get(id);
                    if (speRels == null) {
                        return Stream.empty();
                    }
                    return speRels.stream();
                })
                .collect(Collectors.toSet());
        
        //finally, add elements valid in all species (mapped to key null)
        Set<RelationTO<U>> allSpeRels = this.relationsBySpeciesId.get(null);
        if (allSpeRels != null) {
            retrievedRels.addAll(allSpeRels);
        }

        return log.exit(retrievedRels);
    }
    
    /**
     * Get ancestors of the given {@code element} in this ontology and in {@code speciesIds},
     * according to any {@code RelationType}s that were considered to build this {@code MultiSpeciesOntology}.
     * <p>
     * If {@code directRelOnly} is {@code true}, only direct relations incoming from or outgoing to 
     * {@code element} are considered, in order to only retrieve direct parents of {@code element}.
     * 
     * @param element       A {@code T} that is the element for which ancestors are retrieved.
     * @param directRelOnly A {@code boolean} defining whether only direct parents
     *                      of {@code element} should be returned.
     * @param speciesIds    A {@code Collection} of {@code Integer}s that is the IDs of species
     *                      allowing to filter the elements to retrieve.
     * @return              A {@code Set} of {@code T}s that are the ancestors
     *                      of the given {@code element}. Can be empty if {@code element} 
     *                      has no ancestors according to the requested parameters.
     * @throws IllegalArgumentException If {@code element} is {@code null} or is not found 
     *                                  in this {@code MultiSpeciesOntology}.
     */
    public Set<T> getAncestors(T element, boolean directRelOnly, Collection<Integer> speciesIds) {
        log.entry(element, directRelOnly, speciesIds);
        return log.exit(this.getAncestors(element, null, directRelOnly, speciesIds));
    }

    /**
     * Get ancestors of {@code element} in this ontology based on relations of types 
     * {@code relationTypes}, filtered by {@code speciesIds}.  
     * <p>
     * If {@code directRelOnly} is {@code true}, only direct relations incoming from or outgoing to 
     * {@code element} are considered, in order to only retrieve direct parents of {@code element}.
     * 
     * @param element       A {@code T} that is the element for which ancestors are retrieved.
     * @param relationTypes A {@code Set} of {@code RelationType}s that are the relation
     *                      types to consider.
     * @param directRelOnly A {@code boolean} defining whether only direct parents
     *                      of {@code element} should be returned.
     * @param speciesIds    A {@code Collection} of {@code Integer}s that is the IDs of species
     *                      allowing to filter the elements to retrieve.
     * @return              A {@code Set} of {@code T}s that are the ancestors
     *                      of {@code element} in this ontology. Can be empty if {@code element} 
     *                      has no ancestors according to the requested parameters.
     * @throws IllegalArgumentException If {@code element} is {@code null} or is not found 
     *                                  in this {@code MultiSpeciesOntology}.
     */
    public Set<T> getAncestors(T element, Collection<RelationType> relationTypes, 
            boolean directRelOnly, Collection<Integer> speciesIds) {
        log.entry(element, relationTypes, directRelOnly, speciesIds);
        return log.exit(this.getRelatives(element, this.getElements(speciesIds), 
                true, relationTypes, directRelOnly, this.getRelations(speciesIds)));
    }
    
    /**
     * Get descendants of the given {@code element} in this ontology in {@code speciesIds}, 
     * according to any {@code RelationType}s that were considered to build this {@code MultiSpeciesOntology}.
     * <p>
     * If {@code directRelOnly} is {@code true}, only direct relations incoming from or outgoing to 
     * {@code element} are considered, in order to only retrieve direct children of {@code element}.
     * 
     * @param element       A {@code T} that is the element for which descendants are retrieved.
     * @param directRelOnly A {@code boolean} defining whether only direct children
     *                      of {@code element} should be returned.
     * @param speciesIds    A {@code Collection} of {@code Integer}s that is the IDs of species
     *                      allowing to filter the elements to retrieve.
     * @return              A {@code Set} of {@code T}s that are the descendants
     *                      of the given {@code element} in this ontology. Can be empty if {@code element} 
     *                      has no descendants according to the requested parameters.
     * @throws IllegalArgumentException If {@code element} is {@code null} or is not found 
     *                                  in this {@code MultiSpeciesOntology}.
     */
    public Set<T> getDescendants(T element, boolean directRelOnly, Collection<Integer> speciesIds) {
        log.entry(element, directRelOnly, speciesIds);
        return log.exit(this.getDescendants(element, null, directRelOnly, speciesIds));
    }
    
    /**
     * Get descendants of {@code element} in this ontology based on relations of types
     * {@code relationTypes}, filtered by {@code speciesIds}.
     * <p>
     * If {@code directRelOnly} is {@code true}, only direct relations incoming from or outgoing to 
     * {@code element} are considered, in order to only retrieve direct children of {@code element}.
     * 
     * @param element       A {@code T} that is the element for which descendants are retrieved.
     * @param relationTypes A {@code Set} of {@code RelationType}s that are the relation
     *                      types allowing to filter the relations to retrieve.
     * @param directRelOnly A {@code boolean} defining whether only direct children
     *                      of {@code element} should be returned.
     * @param speciesIds    A {@code Collection} of {@code Integer}s that are the IDs of species
     *                      allowing to filter the elements to retrieve.
     * @return              A {@code Collection} of {@code T}s that are the descendants
     *                      of the given {@code element}. Can be empty if {@code element} 
     *                      has no descendants according to the requested parameters.
     * @throws IllegalArgumentException If {@code element} is {@code null} or is not found 
     *                                  in this {@code MultiSpeciesOntology}.
     */
    public Set<T> getDescendants(T element, Collection<RelationType> relationTypes, 
            boolean directRelOnly, Collection<Integer> speciesIds) {
        log.entry(element, relationTypes, directRelOnly, speciesIds);
        return log.exit(this.getRelatives(element, this.getElements(speciesIds), false, 
                relationTypes, directRelOnly, this.getRelations(speciesIds)));
    }

    /** 
     * Get the {@code Ontology} of the provided {@code speciesId} 
     * from this {@code MultiSpeciesOntology}.
     * 
     * @param speciesId An {@code Integer} that is the ID of species of 
     *                  which the ontology should be retrieved.
     * @return          The {@code Ontology} of the provided {@code speciesId}.
     * @throws IllegalArgumentException If {@code speciesId} is {@code null} or if 
     *                                  the {@code speciesId} is not in this {@code MultiSpeciesOntology}.
     */
    public Ontology<T, U> getAsSingleSpeciesOntology(Integer speciesId) {
        log.entry(speciesId);
        if (speciesId == null || speciesId <= 0) {
            throw log.throwing(new IllegalArgumentException("A species ID should be provided"));
        }

        if (!this.getSpeciesIds().contains(speciesId)) {
            throw log.throwing(new IllegalArgumentException(
                    "Species ID should be in this multi-species ontology"));
        }
       
        return log.exit(new Ontology<>(speciesId, this.getElements(Arrays.asList(speciesId)),
                this.getRelations(Arrays.asList(speciesId)), this.getRelationTypes(),
                this.getServiceFactory(), this.getType()));
    }
}