package org.bgee.model.ontology;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.NamedEntity;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationStatus;
import org.bgee.model.species.Taxon;

/**
 * Abstract class allowing to describe an ontology, or the sub-graph of an ontology.
 * 
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 14, Mar. 2017
 * @since   Bgee 13, Dec. 2015
 * @param <T>   The type of element in this ontology or sub-graph.
 * @param <U>   The type of ID of the elements in this ontology or sub-graph.
 */
public abstract class OntologyBase<T extends NamedEntity<U> & OntologyElement<T, U>, U extends Comparable<U>> {

    private static final Logger log = LogManager.getLogger(OntologyBase.class.getName());
    
    /**
     * A {@code Map} associating IDs of elements as key to the corresponding element as value.
     */
    protected final Map<U, T> elements;

    /**
     * A {@code Set} of {@code RelationTO}s that are the relations between elements of the ontology.
     */
    private final Set<RelationTO<U>> relations;

    /**
     * @see #getRelationTypes()
     */
    private final Set<RelationType> relationTypes;
    
    /**
     * A {@code Map} where keys are {@code T}s, the associated value being
     * a {@code Set} of {@code RelationTO}s having the key as the source of the relation.
     * Not needed to be used in equals/hashCode methods, this attribute is derived from others.
     */
    private final Map<T, Set<RelationTO<U>>> relationsBySourceElement;
    /**
     * A {@code Map} where keys are {@code T}s, the associated value being
     * a {@code Set} of {@code RelationTO}s having the key as the target of the relation.
     * Not needed to be used in equals/hashCode methods, this attribute is derived from others.
     */
    private final Map<T, Set<RelationTO<U>>> relationsByTargetElement;
    
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
    protected OntologyBase(Collection<T> elements, Collection<RelationTO<U>> relations,
            Collection<RelationType> relationTypes, ServiceFactory serviceFactory, Class<T> type) {
        log.entry(elements, relations, relationTypes, serviceFactory, type);

        long startTimeInMs = System.currentTimeMillis();
        log.debug("Start creation of OntologyBase");

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
        this.elements = Collections.unmodifiableMap(elements.stream()
            .collect(Collectors.toMap(e -> e.getId(), e -> e, (e1, e2) -> e1)));
        this.relations = Collections.unmodifiableSet(
                relations == null? new HashSet<>(): new HashSet<>(relations));
        this.relationTypes = Collections.unmodifiableSet(new HashSet<>(relationTypes));
        this.serviceFactory = serviceFactory;
        this.type = type;

        //check for null elements after filtering redundancy thanks to Sets
        if (this.elements.values().stream().anyMatch(Objects::isNull)) {
            throw log.throwing(new IllegalArgumentException("No element can be null."));
        }
        if (this.relationTypes.stream().anyMatch(Objects::isNull)) {
            throw log.throwing(new IllegalArgumentException("No relation type can be null."));
        }
        if (type != null && this.elements.values().stream().anyMatch(e -> !e.getClass().isAssignableFrom(type))) {
            throw log.throwing(new IllegalArgumentException(
                    "The class of all elements should be equals to provided class " + type));
        }
        
        Map<T, Set<RelationTO<U>>> relationsBySourceElement = new HashMap<>();
        Map<T, Set<RelationTO<U>>> relationsByTargetElement = new HashMap<>();
        REL: for (RelationTO<U> relTO: this.relations) {
            if (relTO == null) {
                throw log.throwing(new IllegalArgumentException("No relation can be null."));
            }
            T sourceElement = this.getElement(relTO.getSourceId());
            T targetElement = this.getElement(relTO.getTargetId());
            if (sourceElement == null || targetElement == null) {
                continue REL;
            }
            
            relationsBySourceElement.merge(sourceElement, 
                    new HashSet<>(Arrays.asList(relTO)), (s1, s2) -> {s1.addAll(s2); return s1;});
            relationsByTargetElement.merge(targetElement, 
                    new HashSet<>(Arrays.asList(relTO)), (s1, s2) -> {s1.addAll(s2); return s1;});
        }
        this.relationsBySourceElement = Collections.unmodifiableMap(
            relationsBySourceElement.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), 
                e -> Collections.unmodifiableSet(e.getValue()))));
        this.relationsByTargetElement = Collections.unmodifiableMap(
            relationsByTargetElement.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), 
                e -> Collections.unmodifiableSet(e.getValue()))));

        log.debug("OntologyBase created in {} ms", System.currentTimeMillis() - startTimeInMs);
        log.traceExit();
    }
    
    //**********************************************
    //   GETTERS
    //**********************************************

    /**
     * @return  The {@code Set} of {@code T}s that are the elements that were considered to build 
     *          this ontology or sub-graph.
     */
    public Set<T> getElements() {
        return new HashSet<>(elements.values());
    }

    /**
     * @return  The {@code Set} of {@code RelationTO}s that are the relations that were considered
     *          to build this ontology or sub-graph.
     */
    protected Set<RelationTO<U>> getRelations() {
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
    public T getElement(U id) {
        return elements.get(id);
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
        return log.traceExit(this.getAncestors(element, false));
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
        return log.traceExit(this.getOrderedAncestors(element, null));
    }

    /**
     * Get ancestors of {@code element} in this ontology based on relations of types {@code relationTypes}.
     * 
     * @param element       A {@code T} that is the element for which ancestors are retrieved.
     * @param relationTypes A {@code Collection} of {@code RelationType}s that are the relation
     *                      types to consider.
     * @return              A {@code Set} of {@code T}s that are the ancestors
     *                      of {@code element} in this ontology. Can be empty if {@code element} 
     *                      has no ancestors according to the requested parameters.
     * @throws IllegalArgumentException If {@code element} is {@code null} or is not found 
     *                                  in this ontology.
     */
    public Set<T> getAncestors(T element, Collection<RelationType> relationTypes) {
        log.entry(element, relationTypes);
        return log.traceExit(this.getAncestors(element, relationTypes, false));
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
        return log.traceExit(this.getOrderedRelations(element).stream()
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
        return log.traceExit(this.getAncestors(element, null, directRelOnly));
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
        return log.traceExit(this.getRelatives(element, this.getElements(), true, relationTypes, directRelOnly, 
                this.getRelations()));
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
        return log.traceExit(this.getDescendants(element, false));
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
        return log.traceExit(this.getDescendants(element, relationTypes, false));
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
        return log.traceExit(this.getDescendants(element, null, directRelOnly));
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
        return log.traceExit(this.getRelatives(element, this.getElements(), false, relationTypes, directRelOnly, 
                this.getRelations()));
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
            throw log.throwing(new IllegalArgumentException("Sub-level should be stricly greater than 0"));
        }
        Set<T> allDescendants = new HashSet<>();
        Set<T> currentElements = new HashSet<>();
        currentElements.add(element);
        int i = 0;
        while (i < subLevelMax && !currentElements.isEmpty()) {
            Set<T> descendants = currentElements.stream()
                .map(d -> getDescendants(d, true))
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
            allDescendants.addAll(descendants);
            currentElements = new HashSet<>();
            currentElements.addAll(descendants);
            i++;
        }
        return log.traceExit(allDescendants);
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
     * @param speciesIds                A {@code Collection} of {@code Integer}s that is the IDs of species
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
    protected Set<T> getRelatives(T element, Set<T> elementsToConsider, boolean isAncestor, 
            Collection<RelationType> relationTypes, boolean directRelOnly, 
            Set<RelationTO<U>> relationsToConsider) {
        log.entry(element, elementsToConsider, isAncestor, relationTypes, directRelOnly, 
                relationsToConsider);
        log.trace("Start retrieving relatives for {}", element);
        
        if (elementsToConsider == null) {
            //could be empty if no valid relations with provided parameters, 
            //but should not be null
            throw log.throwing(new IllegalArgumentException("Valid entities not provided."));
        }
        if (!elementsToConsider.contains(element)) {
            throw log.throwing(new IllegalArgumentException(
                    "Element does not exist in the requested species or ontology: " + element));
        }
        assert element != null;

        final Set<RelationTO.RelationType> usedRelationTypes = (relationTypes == null? 
                EnumSet.noneOf(RelationType.class): relationTypes)
                .stream()
                .map(OntologyBase::convertRelationType)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(RelationTO.RelationType.class)));
        log.trace("Used relation types: {}", usedRelationTypes);
        
        Set<RelationTO<U>> toKeep = null;
        if (isAncestor) {
            toKeep = this.relationsBySourceElement.get(element);
        } else {
            toKeep = this.relationsByTargetElement.get(element);
        }
        if (toKeep == null || toKeep.isEmpty()) {
            return log.traceExit(new HashSet<>());
        }

        Stream<RelationTO<U>> filteredRelations1 = toKeep.stream()
                .filter(r -> relationsToConsider.contains(r));
        Stream<RelationTO<U>> filteredRelations2 = filteredRelations1;
        if (!usedRelationTypes.isEmpty()) {
            filteredRelations2 = filteredRelations1
                    .filter(r -> usedRelationTypes.contains(r.getRelationType()));
        }
        Stream<RelationTO<U>> filteredRelationsFinal = filteredRelations2;
        if (directRelOnly) {
            filteredRelationsFinal = filteredRelations2
                    .filter(r -> RelationTO.RelationStatus.DIRECT.equals(r.getRelationStatus()));
        }

        Stream<T> relatives = null;
        if (isAncestor) {
            relatives = filteredRelationsFinal.map(r -> this.getElement(r.getTargetId()));
        } else {
            relatives = filteredRelationsFinal.map(r -> this.getElement(r.getSourceId()));
        }
        
        Set<T> returned = relatives
                .filter(e -> e != null)
                .filter(e -> elementsToConsider.contains(e))
                .filter(e -> !e.getId().equals(element.getId()))
                .collect(Collectors.toSet());
        log.trace("Done retrieving relatives for {}: {}", element, returned.size());
        return log.traceExit(returned);
    }

    /**
     * Retrieve ordered relations from {@code element}.
     * <p>
     * Ancestors are ordered from {@code element} ID to farthest element ID.  
     * 
     * @param element   A {@code T} that is the element from which relations are retrieved.
     * @return          The {@code List} of ordered {@code RelationTO}s of this ontology.
     */
    protected List<RelationTO<U>> getOrderedRelations(T element) {
        log.entry(element);
        
        if (!this.type.equals(DevStage.class) && !this.type.equals(Taxon.class)) {
            //XXX: Each element of taxonomy and dev. stage ontology have only one parent
            throw log.throwing(new IllegalArgumentException(
                    "Ordering unsupported for OntologyElement " + this.type));
        }
        
        List<RelationTO<U>> orderedRels = new ArrayList<>();
        ArrayDeque<RelationTO<U>> queue = new ArrayDeque<>(this.relations);
        U id = element.getId();
        int initialQueueSize = queue.size();
        int countViewedRelations = 0;
        while (!queue.isEmpty() && countViewedRelations < initialQueueSize) {
            RelationTO<U> currentRel = queue.pop();
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

        return log.traceExit(orderedRels);
    }

    /**
     * Retrieve the least common ancestors of the provided elements. If this ontology is a tree
     * (each element has only one parent, and there is only one root to the ontology),
     * the returned {@code Set} is guaranteed to contain one and only one element. If this ontology
     * accepts several roots and elements can have several parents, the returned {@code Set}
     * can contain 0 or several elements.
     *
     * @param elements      A {@code Collection} of {@code T}s that are the elements to retrieve
     *                      least common ancestor for.
     * @param relationTypes A {@code Collection} of {@code RelationType}s that are the relation
     *                      types to consider.
     * @return              A {@code Set} of {@code T}s that are the least common ancestors
     *                      of the provided elements.
     * @throws IllegalArgumentException     If {@code elements} is {@code null} or contains less than
     *                                      2 elements, or if some of the elements are parent-child.
     */
    public Set<T> getLeastCommonAncestors(Collection<T> elements, Collection<RelationType> relationTypes)
            throws IllegalArgumentException {
        log.entry(elements, relationTypes);
        if (elements == null || elements.size() < 2) {
            throw log.throwing(new IllegalArgumentException(
                    "At least 2 elements must be provided to retrieve least common ancestors"));
        }
        Set<T> clonedElements = new HashSet<>(elements);
        //First, we retrieve the ancestors for each of the requested elements
        //and keep only those in common
        Set<T> ancestors = clonedElements.stream()
                .collect(Collectors.reducing(null,
                        e -> {
                            Set<T> ancs = this.getAncestors(e, relationTypes);
                            if (!Collections.disjoint(ancs, clonedElements)) {
                                throw log.throwing(new IllegalArgumentException(
                                        "Some elements are parent-child, elements requested: "
                                        + clonedElements + ", ancestors of element " + e + ": "
                                        + ancs));
                            }
                            return ancs;
                        },
                        (s1, s2) -> {
                            if (s1 == null) {
                                return s2;
                            }
                            if (s2 == null) {
                                return s1;
                            }
                            s1.retainAll(s2);
                            return s1;
                        }));
        if (ancestors == null || ancestors.isEmpty()) {
            //means that the provided elements have no ancestors in common,
            //it can happen if the ontology has several roots, the elements belong to
            //completely independent branches
            return log.traceExit(new HashSet<>());
        }
        //Now, we discard any ancestor that is itself an ancestor of an ancestor
        Iterator<T> ancestorIterator = ancestors.iterator();
        Set<T> discardedAncestors = new HashSet<>();
        while (ancestorIterator.hasNext() && discardedAncestors.size() < ancestors.size() - 1) {
            discardedAncestors.addAll(this.getAncestors(ancestorIterator.next(), relationTypes));
        }
        ancestors.removeAll(discardedAncestors);
        return log.traceExit(ancestors);
    }

    /**
     * Retrieve the elements that are not descendants of other terms among {@code elements}.
     * For instance, if C is_a B, B is_a A, if {@code elements} contains A, B, and C,
     * this method will return A.
     *
     * @param elements      A {@code Collection} of {@code T}s for which we want to identify the elements
     *                      that are not descendants of other elements in the {@code Collection}.
     *                      If {@code null} or empty, this method returns an empty {@code Set}.
     * @param relationTypes A {@code Collection} of {@code RelationType}s to specify the types of relations
     *                      to consider for identify ancestors/descendants. If {@code null} or empty,
     *                      all relation types present in this ontology are considered.
     * @return              A {@code Set} of {@code T}s that are the elements that are not the descendants
     *                      of other elements in {@code elements}.
     */
    public Set<T> getAncestorsAmongElements(Collection<T> elements, Collection<RelationType> relationTypes) {
        log.entry(elements, relationTypes);
        if (elements == null || elements.isEmpty()) {
            return log.traceExit(new HashSet<>());
        }
        Set<T> clonedElements = new HashSet<>(elements);
        return log.traceExit(clonedElements.stream()
                .filter(e -> Collections.disjoint(clonedElements, this.getAncestors(e, relationTypes)))
                .collect(Collectors.toSet()));
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
            return log.traceExit(RelationTO.RelationType.ISA_PARTOF);
        case DEVELOPSFROM: 
            return log.traceExit(RelationTO.RelationType.DEVELOPSFROM);
        case TRANSFORMATIONOF: 
            return log.traceExit(RelationTO.RelationType.TRANSFORMATIONOF);
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
        OntologyBase<?,?> other = (OntologyBase<?,?>) obj;
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
        StringBuilder builder = new StringBuilder();
        builder.append("OntologyBase [elements=").append(elements)
               .append(", relations=").append(relations)
               .append(", relationTypes=").append(relationTypes)
               .append(", type=").append(type).append("]");
        return builder.toString();
    }
}
