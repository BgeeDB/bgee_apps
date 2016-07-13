package org.bgee.model.ontology;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.NamedEntity;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.TaxonConstraint;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;

/**
 * Abstract class allowing to describe an ontology, or the sub-graph of an ontology.
 * 
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 13, July 2016
 * @since   Bgee 13, Dec. 2015
 * @param <T>   The type of element in this ontology or sub-graph.
 */
public abstract class SpeciesNeutralOntology<T extends NamedEntity & OntologyElement<T>> {

    private static final Logger log = LogManager.getLogger(SpeciesNeutralOntology.class.getName());
    
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
     * from the {@code bgee-dao-api} layer. {@code SpeciesNeutralOntology} objects can only be obtained 
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
     *                          to be store by this {@code SpeciesNeutralOntology}.
     */
    //XXX: when needed, we could add a parameter 'directRelOnly', in case we only want 
    //to retrieve direct parents or children of terms. See method 'getRelatives' 
    //already capable of considering only direct relations.
    protected SpeciesNeutralOntology(Collection<T> elements, Collection<RelationTO> relations,
            Collection<RelationType> relationTypes, ServiceFactory serviceFactory, Class<T> type) {
        log.entry(elements, relations, relationTypes, serviceFactory, type);
        if (elements == null || elements.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Some elements must be considered."));
        }
        if (relationTypes == null || relationTypes.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Some relation types must be considered."));
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
     * @return  The {@code Class<T>} that is the type of {@code elements} stored by this {@code SpeciesNeutralOntology}.
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
        return log.exit(this.getRelatives(element, true, relationTypes, directRelOnly, null, null));
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
     * @return              A {@code Collection} of {@code T}s that are the descendants
     *                      of the given {@code element}. Can be empty if {@code element} 
     *                      has no descendants according to the requested parameters.
     * @throws IllegalArgumentException If {@code element} is {@code null} or is not found 
     *                                  in this ontology.
     */
    public Set<T> getDescendants(T element, Collection<RelationType> relationTypes, boolean directRelOnly) {
        log.entry(element, relationTypes, directRelOnly);
        return log.exit(this.getRelatives(element, false, relationTypes, directRelOnly, null, null));
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
     * @param element               A {@code T} that is the element for which relatives are retrieved.
     * @param isAncestor            A {@code boolean} defining whether the returned {@code Set}
     *                              are ancestors or descendants. If {@code true},
     *                              it will retrieved ancestors.
     * @param relationTypes         A {@code Collection} of {@code RelationType}s that are the
     *                              relation types allowing to filter the relations to consider.
     * @param directRelOnly         A {@code boolean} defining whether only direct parents 
     *                              or children of {@code element} should be returned.
     * @param speciesIds            A {@code Collection} of {@code String}s that is the IDs of species
     *                              allowing to filter the elements to retrieve.
     * @return                      A {@code Set} of {@code T}s that are either the sources 
     *                              or the ancestors or descendants of {@code element}, 
     *                              depending on {@code isAncestor}.
     * @throws IllegalArgumentException If {@code element} is {@code null} or is not found 
     *                                  in this ontology.
     */
    // XXX could be used in BgeeDBUtils.getIsAPartOfRelativesFromDb()
    protected Set<T> getRelatives(T element, boolean isAncestor, Collection<RelationType> relationTypes,
            boolean directRelOnly, Collection<String> speciesIds, 
            Collection<TaxonConstraint> anatEntityRelationTaxonConstraints) {
        log.entry(element, isAncestor, relationTypes, directRelOnly, speciesIds);
        
        boolean isSpeciesSpecific = speciesIds != null && !speciesIds.isEmpty();
        
        final Set<T> curElements = isSpeciesSpecific ? 
                ((MultiSpeciesOntology<T>)this).getElements(speciesIds) : Collections.unmodifiableSet(elements); 

        if (!curElements.contains(element)) {
            throw log.throwing(new IllegalArgumentException("Unrecognized element: " + element));
        }

        final EnumSet<RelationTO.RelationType> usedRelationTypes = (relationTypes == null?
                EnumSet.allOf(RelationType.class): new HashSet<>(relationTypes))
                .stream()
                .map(SpeciesNeutralOntology::convertRelationType)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(RelationTO.RelationType.class)));

        final Set<String> allowedRelationIds;
        if (isSpeciesSpecific && type.equals(AnatEntity.class)) {
            allowedRelationIds = anatEntityRelationTaxonConstraints.stream()
                    .filter(tc -> tc.getSpeciesId() == null || speciesIds.contains(tc.getSpeciesId()))
                    .map(ds -> ds.getEntityId()).collect(Collectors.toSet());
        } else {
            allowedRelationIds = null;
        }

        final Set<RelationTO> filteredRelations = relations.stream()
                .filter(r -> usedRelationTypes.contains(r.getRelationType()) && 
                             (!directRelOnly || 
                                  RelationTO.RelationStatus.DIRECT.equals(r.getRelationStatus())))
                .filter(r -> allowedRelationIds == null || allowedRelationIds.contains(r.getId()))
                .collect(Collectors.toSet());

        Set<String> allowedEntityIds = curElements.stream().map(e -> e.getId()).collect(Collectors.toSet());

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
     * Convert a {@code RelationType} from {@code SpeciesNeutralOntology} to a {@code RelationType} 
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
    // TODO : Is hashCode() should be calculated considering serviceFactory and type?
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((elements == null) ? 0 : elements.hashCode());
        result = prime * result + ((relations == null) ? 0 : relations.hashCode());
        result = prime * result + ((relationTypes == null) ? 0 : relationTypes.hashCode());
        result = prime * result + ((serviceFactory == null) ? 0 : serviceFactory.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    // TODO : Is equals() should be calculated considering serviceFactory and type?
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SpeciesNeutralOntology<?> other = (SpeciesNeutralOntology<?>) obj;
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
        if (serviceFactory == null) {
            if (other.serviceFactory != null)
                return false;
        } else if (!serviceFactory.equals(other.serviceFactory))
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
                " - Relation types: " + relationTypes +" - Service factory: " + serviceFactory +
                " - Type: " + type;
    }
}
