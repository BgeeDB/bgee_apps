package org.bgee.model.ontology;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.NamedEntity;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.TaxonConstraint;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationStatus;
import org.bgee.model.species.Taxon;

/**
 * Abstract class allowing to describe an ontology, or the sub-graph of an ontology.
 * 
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 14, Jan. 2017
 * @since   Bgee 13, Dec. 2015
 * @param <T>   The type of element in this ontology or sub-graph.
 */
public abstract class OntologyBase<T extends NamedEntity & OntologyElement<T>> {

    private static final Logger log = LogManager.getLogger(OntologyBase.class.getName());
    
    /**
     * @see #getElements()
     */
    private final Set<T> elements;

    /**
     * A {@code Set} of {@code RelationTO}s that are the relations between elements of the ontology.
     */
    private final Set<RelationTO> relations;

    /**
     * @see #getRelationTypes()
     */
    private final Set<RelationType> relationTypes;
    
    /**
     * The {@code ServiceFactory} to obtain {@code Service} objects.
     */
    private final ServiceFactory serviceFactory;

    /**
     * @see #getType()
     */
    private final Class<T> type;

    /**
     * Constructor providing the elements, the relations, the relations types, the service factory,
     * and the type of elements of the ontology.
     * <p>
     * This constructor is protected to not expose the {@code RelationTO} objects 
     * from the {@code bgee-dao-api} layer. {@code OntologyBase} objects can only be obtained 
     * through {@code OntologyService}s.
     * 
     * @param elements          A {@code Collection} of {@code T}s that are
     *                          the elements of this ontology.
     * @param relations         A {@code Collection} of {@code RelationTO}s that are
     *                          the relations between elements of the ontology.
     * @param relationTypes     A {@code Collection} of {@code RelationType}s that were
     *                          considered to build this ontology or sub-graph.
     * @param serviceFactory    A {@code ServiceFactory} to acquire {@code Service}s from.
     * @param type              A {@code Class<T>} that is the type of {@code elements} 
     *                          to be store by this {@code OntologyBase}.
     */
    //XXX: when needed, we could add a parameter 'directRelOnly', in case we only want 
    //to retrieve direct parents or children of terms. See method 'getRelatives' 
    //already capable of considering only direct relations.
    protected OntologyBase(Collection<T> elements, Collection<RelationTO> relations,
            Collection<RelationType> relationTypes, ServiceFactory serviceFactory, Class<T> type) {
        log.entry(elements, relations, relationTypes, serviceFactory, type);
        if (elements == null || elements.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Some elements must be considered."));
        }
        if (relationTypes == null || relationTypes.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Some relation types must be considered."));
        }
        if (serviceFactory == null) {
            throw log.throwing(new IllegalArgumentException("A ServiceFactory must be provided."));
        }
        
        //it is acceptable to have no relations provided: maybe there is no valid relations 
        //for the requested parameters.
        this.elements = Collections.unmodifiableSet(new HashSet<>(elements));
        this.relations = Collections.unmodifiableSet(
                relations == null? new HashSet<>(): new HashSet<>(relations));
        this.relationTypes = Collections.unmodifiableSet(new HashSet<>(relationTypes));
        this.serviceFactory = serviceFactory;
        this.type = type;

        //check for null elements after filtering redundancy thanks to Sets
        if (this.elements.stream().anyMatch(Objects::isNull)) {
            throw log.throwing(new IllegalArgumentException("No element can be null."));
        }
        if (this.relations.stream().anyMatch(Objects::isNull)) {
            throw log.throwing(new IllegalArgumentException("No relation can be null."));
        }
        if (this.relationTypes.stream().anyMatch(Objects::isNull)) {
            throw log.throwing(new IllegalArgumentException("No relation type can be null."));
        }
        if (type != null && this.elements.stream().anyMatch(e -> !e.getClass().isAssignableFrom(type))) {
            throw log.throwing(new IllegalArgumentException(
                    "The class of all elements should be equals to provided class " + type));
        }
        
        log.exit();
    }
    
    //**********************************************
    //   GETTERS
    //**********************************************

    /**
     * @return  The {@code Set} of {@code T}s that are the elements that were considered to build 
     *          this ontology or sub-graph.
     */
    public Set<T> getElements() {
        return elements;
    }

    /**
     * @return  The {@code Set} of {@code RelationTO}s that are the relations that were considered
     *          to build this ontology or sub-graph.
     */
    protected Set<RelationTO> getRelations() {
        return relations;
    }

    /**
     * @return  The {@code Set} of {@code RelationType}s that were considered to build 
     *          this ontology or sub-graph.
     */
    public Set<RelationType> getRelationTypes() {
        return relationTypes;
    }
    
    /**
     * @return  The {@code ServiceFactory} to acquire {@code Service}s from.
     */
    protected ServiceFactory getServiceFactory() {
        return serviceFactory;
    }

    /**
     * @return  The {@code Class<T>} that is the type of {@code elements} stored by this {@code OntologyBase}.
     */
    protected Class<T> getType() {
        return type;
    }

    //**********************************************
    //   INSTANCE METHODS
    //**********************************************

    /**
     * Get the element corresponding to the given {@code id}.
     * 
     * @param id    A {@code String} that is the ID of the element to be retrieved.
     * @return      A {@code T} that is the element corresponding to the given {@code id}.
     *              Return {@code null} if the element is not found in the ontology.
     */
    public T getElement(String id) {
        log.entry(id);
        for (T t: elements) {
            if (t.getId().equals(id))
                return log.exit(t);
        }
        return log.exit(null);
    }

    /**
     * Get ancestors of {@code element} in this ontology based on any relations that were loaded.
     * 
     * @param element       A {@code T} that is the element for which ancestors are retrieved.
     * @return              A {@code Set} of {@code T}s that are the ancestors
     *                      of {@code element} in this ontology. Can be empty if {@code element} 
     *                      has no ancestors according to the loaded information.
     * @throws IllegalArgumentException If {@code element} is {@code null} or is not found 
     *                                  in this ontology.
     */
    public Set<T> getAncestors(T element) {
        log.entry(element);
        return log.exit(this.getAncestors(element, false));
    }

    /**
     * Get ordered ancestors of {@code element} in this ontology based on any relations that were loaded.
     * <p>
     * Only direct relations incoming from or outgoing to {@code element} are considered,
     * in order to only retrieve direct parents of {@code element}.
     * <p>
     * Ancestors are ordered from closest to farthest of {@code element}.  
     * 
     * @param element       A {@code T} that is the element for which ancestors are retrieved.
     * @return              A {@code Set} of {@code T}s that are the ancestors
     *                      of {@code element} in this ontology. Can be empty if {@code element} 
     *                      has no ancestors according to the loaded information.
     * @throws IllegalArgumentException If {@code element} is {@code null} or is not found 
     *                                  in this ontology.
     */
    public List<T> getOrderedAncestors(T element) {
        log.entry(element);
        return log.exit(this.getOrderedAncestors(element, null));
    }

    /**
     * Get ancestors of {@code element} in this ontology based on relations of types {@code relationTypes}.
     * 
     * @param element       A {@code T} that is the element for which ancestors are retrieved.
     * @param relationTypes A {@code Set} of {@code RelationType}s that are the relation
     *                      types to consider.
     * @return              A {@code Set} of {@code T}s that are the ancestors
     *                      of {@code element} in this ontology. Can be empty if {@code element} 
     *                      has no ancestors according to the requested parameters.
     * @throws IllegalArgumentException If {@code element} is {@code null} or is not found 
     *                                  in this ontology.
     */
    public Set<T> getAncestors(T element, Collection<RelationType> relationTypes) {
        log.entry(element, relationTypes);
        return log.exit(this.getAncestors(element, relationTypes, false));
    }
    
    /**
     * Get ordered ancestors of {@code element} in this ontology based on relations
     * of types {@code relationTypes}.
     * <p>
     * Only direct relations incoming from or outgoing to {@code element} are considered,
     * in order to only retrieve direct parents of {@code element}.
     * <p>
     * Ancestors are ordered from closest to farthest of {@code element}.  
     * 
     * @param element       A {@code T} that is the element for which ancestors are retrieved.
     * @param relationTypes A {@code Set} of {@code RelationType}s that are the relation
     *                      types to consider.
     * @return              A {@code Set} of {@code T}s that are the ancestors
     *                      of {@code element} in this ontology. Can be empty if {@code element} 
     *                      has no ancestors according to the requested parameters.
     * @throws IllegalArgumentException If {@code element} is {@code null} or is not found 
     *                                  in this ontology.
     */
    public List<T> getOrderedAncestors(T element, Collection<RelationType> relationTypes) {
        log.entry(element, relationTypes);
        if (element == null) {
            throw new IllegalArgumentException("Provided element is null");
        }
        if (getElement(element.getId()) == null) {
            throw new IllegalArgumentException("Provided element is not found in this ontology");
        }
        Set<T> ancestors = this.getAncestors(element, relationTypes, false);
        return log.exit(this.getOrderedRelations(element).stream()
            .map(r -> getElement(r.getTargetId()))
            .filter(e -> ancestors.contains(e))
            .collect(Collectors.toList()));
    }

    /**
     * Get ancestors of the given {@code element} in this ontology, according to 
     * any {@code RelationType}s that were considered to build this ontology.
     * <p>
     * If {@code directRelOnly} is {@code true}, only direct relations incoming from or outgoing to 
     * {@code element} are considered, in order to only retrieve direct parents of {@code element}.
     * 
     * @param element       A {@code T} that is the element for which ancestors are retrieved.
     * @param directRelOnly A {@code boolean} defining whether only direct parents
     *                      of {@code element} should be returned.
     * @return              A {@code Set} of {@code T}s that are the ancestors
     *                      of the given {@code element}. Can be empty if {@code element} 
     *                      has no ancestors according to the requested parameters.
     * @throws IllegalArgumentException If {@code element} is {@code null} or is not found 
     *                                  in this ontology.
     */
    public Set<T> getAncestors(T element, boolean directRelOnly) {
        log.entry(element, directRelOnly);
        return log.exit(this.getAncestors(element, null, directRelOnly));
    }
    
    /**
     * Get ancestors of {@code element} in this ontology based on relations of types {@code relationTypes}.
     * <p>
     * If {@code directRelOnly} is {@code true}, only direct relations incoming from or outgoing to 
     * {@code element} are considered, in order to only retrieve direct parents of {@code element}.
     * 
     * @param element       A {@code T} that is the element for which ancestors are retrieved.
     * @param relationTypes A {@code Set} of {@code RelationType}s that are the relation
     *                      types to consider.
     * @param directRelOnly A {@code boolean} defining whether only direct parents
     *                      of {@code element} should be returned.
     * @return              A {@code Set} of {@code T}s that are the ancestors
     *                      of {@code element} in this ontology. Can be empty if {@code element} 
     *                      has no ancestors according to the requested parameters.
     * @throws IllegalArgumentException If {@code element} is {@code null} or is not found 
     *                                  in this ontology.
     */
    public Set<T> getAncestors(T element, Collection<RelationType> relationTypes, boolean directRelOnly) {
        log.entry(element, relationTypes, directRelOnly);
        return log.exit(this.getRelatives(element, this.getElements(), true, relationTypes, directRelOnly, 
                null, null));
    }

    /**
     * Get descendants of the given {@code element} in this ontology, according to 
     * any {@code RelationType}s that were considered to build this ontology.
     * 
     * @param element       A {@code T} that is the element for which descendants are retrieved.
     * @return              A {@code Set} of {@code T}s that are the descendants
     *                      of the given {@code element} in this ontology. Can be empty if {@code element} 
     *                      has no descendants according to the requested parameters.
     * @throws IllegalArgumentException If {@code element} is {@code null} or is not found 
     *                                  in this ontology.
     */
    public Set<T> getDescendants(T element) {
        log.entry(element);
        return log.exit(this.getDescendants(element, false));
    }

    /**
     * Get descendants of {@code element} in this ontology based on relations of types {@code relationTypes}.
     * 
     * @param element       A {@code T} that is the element for which descendants are retrieved.
     * @param relationTypes A {@code Set} of {@code RelationType}s that are the relation
     *                      types allowing to filter the relations to retrieve.
     * @return              A {@code Collection} of {@code T}s that are the descendants
     *                      of the given {@code element}. Can be empty if {@code element} 
     *                      has no descendants according to the requested parameters.
     * @throws IllegalArgumentException If {@code element} is {@code null} or is not found 
     *                                  in this ontology.
     */
    public Set<T> getDescendants(T element, Collection<RelationType> relationTypes) {
        log.entry(element, relationTypes);
        return log.exit(this.getDescendants(element, relationTypes, false));
    }

    /**
     * Get descendants of the given {@code element} in this ontology, according to 
     * any {@code RelationType}s that were considered to build this ontology.
     * <p>
     * If {@code directRelOnly} is {@code true}, only direct relations incoming from or outgoing to 
     * {@code element} are considered, in order to only retrieve direct children of {@code element}.
     * 
     * @param element       A {@code T} that is the element for which descendants are retrieved.
     * @param directRelOnly A {@code boolean} defining whether only direct children
     *                      of {@code element} should be returned.
     * @return              A {@code Set} of {@code T}s that are the descendants
     *                      of the given {@code element} in this ontology. Can be empty if {@code element} 
     *                      has no descendants according to the requested parameters.
     * @throws IllegalArgumentException If {@code element} is {@code null} or is not found 
     *                                  in this ontology.
     */
    public Set<T> getDescendants(T element, boolean directRelOnly) {
        log.entry(element, directRelOnly);
        return log.exit(this.getDescendants(element, null, directRelOnly));
    }


    /**
     * Get descendants of {@code element} in this ontology based on relations of types {@code relationTypes}.
     * <p>
     * If {@code directRelOnly} is {@code true}, only direct relations incoming from or outgoing to 
     * {@code element} are considered, in order to only retrieve direct children of {@code element}.
     * 
     * @param element       A {@code T} that is the element for which descendants are retrieved.
     * @param relationTypes A {@code Set} of {@code RelationType}s that are the relation
     *                      types allowing to filter the relations to retrieve.
     * @param directRelOnly A {@code boolean} defining whether only direct children
     *                      of {@code element} should be returned.
     * @return              A {@code Set} of {@code T}s that are the descendants
     *                      of the given {@code element}. Can be empty if {@code element} 
     *                      has no descendants according to the requested parameters.
     * @throws IllegalArgumentException If {@code element} is {@code null} or is not found 
     *                                  in this ontology.
     */
    public Set<T> getDescendants(T element, Collection<RelationType> relationTypes, boolean directRelOnly) {
        log.entry(element, relationTypes, directRelOnly);
        return log.exit(this.getRelatives(element, this.getElements(), false, relationTypes, directRelOnly, 
                null, null));
    }

    /**
     * Get descendants of {@code element} in this ontology until maximum sub-level
     * in which children are retrieved.
     * 
     * @param element       A {@code T} that is the element for which descendants are retrieved.
     * @param subLevelMax   An {@code int} that is the maximum sub-level in which children are retrieved.
     * @return              A {@code Set} of {@code T}s that are the descendants
     *                      of the given {@code element}
     */
    public Set<T> getDescendantsUntilSubLevel(T element, int subLevelMax) {
        log.entry(element, subLevelMax);
        if (subLevelMax < 1) {
            throw log.throwing(new IllegalArgumentException("Sub-level should be positif"));
        }
        Set<T> descendants = new HashSet<>();
        descendants.add(element);
        for (int i = 0; i < subLevelMax; i++) {
            descendants.addAll(descendants.stream()
                .map(d -> getDescendants(d, true))
                .flatMap(Set::stream)
                .collect(Collectors.toSet()));
        }
        descendants.remove(element);
        return log.exit(descendants);
    }
    
    /**
     * Get relatives from this ontology. The returned {@code Set} contains
     * ancestor or descendants of the provided {@code element} retrieved from
     * {@code relations} of this ontology. If {@code isAncestors} is {@code true}, the returned
     * {@code Set} contains ancestors the {@code element}. If it is {@code false}, the returned
     * {@code Set} contains descendants the {@code element}. 
     * <p>
     * If {@code directRelOnly} is {@code true}, only direct relations incoming from or 
     * outgoing to {@code element} are considered, in order to only retrieve direct parents 
     * or direct children of {@code element}.
     * 
     * @param element                   A {@code T} that is the element for which relatives are retrieved.
     * @param elements                  A {@code Set} of {@code T}s that are all elements 
     *                                  that can be considered as relatives. 
     * @param isAncestor                A {@code boolean} defining whether the returned {@code Set}
     *                                  are ancestors or descendants. If {@code true},
     *                                  it will retrieved ancestors.
     * @param relationTypes             A {@code Collection} of {@code RelationType}s that are the
     *                                  relation types allowing to filter the relations to consider.
     * @param directRelOnly             A {@code boolean} defining whether only direct parents 
     *                                  or children of {@code element} should be returned.
     * @param speciesIds                A {@code Collection} of {@code String}s that is the IDs of species
     *                                  allowing to filter the elements to retrieve.
     * @param relationTaxonConstraints  A {@code Set} of {@code TaxonConstraint}s about relations. 
     *                                  Can be {@code null} for relations inside a nested set model 
     *                                  (e.g., for dev. stage ontology), or for single species ontologies.
     * @return                          A {@code Set} of {@code T}s that are either the sources 
     *                                  or the ancestors or descendants of {@code element}, 
     *                                  depending on {@code isAncestor}.
     * @throws IllegalArgumentException If {@code element} is {@code null} or is not found 
     *                                  in this ontology.
     */
    // XXX could be used in BgeeDBUtils.getIsAPartOfRelativesFromDb()
    //TODO: unit test with multi-species nested set model ontologies (e.g., DevStageOntology)
    protected Set<T> getRelatives(T element, Set<T> elements, boolean isAncestor, 
            Collection<RelationType> relationTypes, boolean directRelOnly, Collection<String> speciesIds, 
            Set<TaxonConstraint> relationTaxonConstraints) {
        log.entry(element, elements, isAncestor, relationTypes, directRelOnly, speciesIds, 
                relationTaxonConstraints);
        
        final Set<String> filteredSpeciesIds = speciesIds == null? null: new HashSet<>(speciesIds);
        boolean isMultiSpecies = filteredSpeciesIds != null && !filteredSpeciesIds.isEmpty();
        
        if (isMultiSpecies && relationTaxonConstraints == null) {
            //could be empty if no valid relations with provided parameters, 
            //but should not be null
            throw log.throwing(new IllegalArgumentException("Relation taxon constraints not provided."));
        }
        if (elements == null) {
            //could be empty if no valid relations with provided parameters, 
            //but should not be null
            throw log.throwing(new IllegalArgumentException("Valid entities not provided."));
        }
        if (!elements.contains(element)) {
            throw log.throwing(new IllegalArgumentException(
                    "Element does not exist in the requested species or ontology: " + element));
        }

        final Set<RelationTO.RelationType> usedRelationTypes = (relationTypes == null?
                EnumSet.allOf(RelationType.class): relationTypes)
                .stream()
                .map(OntologyBase::convertRelationType)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(RelationTO.RelationType.class)));

        final Set<String> allowedEntityIds = elements.stream().map(e -> e.getId()).collect(Collectors.toSet());   
        final Set<String> allowedRelIds;
        final boolean relIdFiltering;
        if (isMultiSpecies && !relationTaxonConstraints.isEmpty()) {
            allowedRelIds = relationTaxonConstraints.stream()
                    .filter(tc -> tc.getSpeciesId() == null || filteredSpeciesIds.contains(tc.getSpeciesId()))
                    .map(tc -> tc.getEntityId()).collect(Collectors.toSet());
            relIdFiltering = true;
        } else {
            //there is no relation IDs for nested set models, so no TaxonConstraints for relations. 
            //Relations simply exist if both the source and target of the relations 
            //exists in the targeted species.
            allowedRelIds = null;
            relIdFiltering = false;
        }

        final Set<RelationTO> filteredRelations = relations.stream()
                .filter(r -> usedRelationTypes.contains(r.getRelationType()) && 
                             (!directRelOnly || 
                                  RelationTO.RelationStatus.DIRECT.equals(r.getRelationStatus())))
                .filter(r -> !isMultiSpecies || //not multi-species, take all
                        relIdFiltering && allowedRelIds.contains(r.getId()) || //allowed rel
                        !relIdFiltering && allowedEntityIds.contains(r.getSourceId()) && 
                                allowedEntityIds.contains(r.getTargetId())) //or, both the source and target 
                                                                            //are allowed entities
                .collect(Collectors.toSet());


        Set<T> relatives = new HashSet<>();
        if (isAncestor) {
            relatives = filteredRelations.stream()
                    .filter(r -> r.getSourceId().equals(element.getId()))
                    .map(r -> this.getElement(r.getTargetId()))
                    .filter(e -> e != null)
                    .filter(e -> allowedEntityIds.contains(e.getId()))
                    .collect(Collectors.toSet());
        } else {
            relatives = filteredRelations.stream()
                    .filter(r-> r.getTargetId().equals(element.getId()))
                    .map(r -> this.getElement(r.getSourceId()))
                    .filter(e -> e != null)
                    .filter(e -> allowedEntityIds.contains(e.getId()))
                     .collect(Collectors.toSet());
         }
         return log.exit(relatives);
    }

    /**
     * Retrieve ordered relations from {@code element}.
     * <p>
     * Ancestors are ordered from {@code element} ID to farthest element ID.  
     * 
     * @param element   A {@code T} that is the element from which relations are retrieved.
     * @return          The {@code List} of ordered {@code RelationTO}s of this ontology.
     */
    protected List<RelationTO> getOrderedRelations(T element) {
        log.entry(element);
        
        if (!this.type.equals(DevStage.class) && !this.type.equals(Taxon.class)) {
            //XXX: Each element of taxonomy and dev. stage ontology have only one parent
            throw log.throwing(new IllegalArgumentException(
                    "Ordering unsupported for OntologyElement " + this.type));
        }
        
        List<RelationTO> orderedRels = new ArrayList<>();
        ArrayDeque<RelationTO> queue = new ArrayDeque<>(this.relations);
        String id = element.getId();
        int initialQueueSize = queue.size();
        int countViewedRelations = 0;
        while (!queue.isEmpty() && countViewedRelations < initialQueueSize) {
            RelationTO currentRel = queue.pop();
            if (currentRel.getSourceId().equals(id) 
                    && currentRel.getRelationStatus().equals(RelationStatus.DIRECT)) {
                orderedRels.add(currentRel);
                id = currentRel.getTargetId();
                countViewedRelations = 0;
                continue;
            }
            queue.add(currentRel);
            countViewedRelations++;
        }

        return log.exit(orderedRels);
    }

    /**
     * Convert a {@code RelationType} from {@code OntologyBase} to a {@code RelationType} 
     * from {@code RelationTO}.
     * 
     * @param relType   The {@code RelationType} to convert.
     * @return          The converted {@code RelationTO.RelationType}.
     * @throws IllegalStateException    If {@code relType} is not supported.
     */
    protected static RelationTO.RelationType convertRelationType(RelationType relType) throws IllegalStateException{
        log.entry(relType);
        switch (relType) {
        case ISA_PARTOF: 
            return log.exit(RelationTO.RelationType.ISA_PARTOF);
        case DEVELOPSFROM: 
            return log.exit(RelationTO.RelationType.DEVELOPSFROM);
        case TRANSFORMATIONOF: 
            return log.exit(RelationTO.RelationType.TRANSFORMATIONOF);
        default: 
            throw log.throwing(new IllegalStateException("Unsupported TO relation type: " + relType));
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((elements == null) ? 0 : elements.hashCode());
        result = prime * result + ((relations == null) ? 0 : relations.hashCode());
        result = prime * result + ((relationTypes == null) ? 0 : relationTypes.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OntologyBase<?> other = (OntologyBase<?>) obj;
        if (elements == null) {
            if (other.elements != null)
                return false;
        } else if (!elements.equals(other.elements))
            return false;
        if (relations == null) {
            if (other.relations != null)
                return false;
        } else if (!relations.equals(other.relations))
            return false;
        if (relationTypes == null) {
            if (other.relationTypes != null)
                return false;
        } else if (!relationTypes.equals(other.relationTypes))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;

        return true;
    }

    @Override
    public String toString() {
        return "Elements: " + elements + " - Relations: " + relations +
                " - Relation types: " + relationTypes +
                " - Type: " + type;
    }
}
