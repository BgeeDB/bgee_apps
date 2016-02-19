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
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;

/**
 * Class allowing to describe an ontology, or the sub-graph of an ontology.
 * 
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 13, Dec. 2015
 * @since   Bgee 13, Dec. 2015
 * @param <T>   The type of element in this ontology or sub-graph.
 */
public class Ontology<T extends NamedEntity & OntologyElement<T>> {

    private static final Logger log = LogManager.getLogger(Ontology.class.getName());
    
    /**
     * List the relation types considered in Bgee. 
     * Bgee makes no distinction between is_a and part_of relations, so they are merged 
     * into one single enum type. Enum types available: 
     * <ul>
     * <li>{@code ISA_PARTOF}
     * <li>{@code DEVELOPSFROM}
     * <li>{@code TRANSFORMATIONOF}
     * </ul>
     * 
     * @author Frederic Bastian
     * @version Bgee 13
     * @since Bgee 13
     */
    public enum RelationType {
        ISA_PARTOF, DEVELOPSFROM, TRANSFORMATIONOF;
    }

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
     * Constructor providing the elements, the relations, the relations types,
     * and the relations status of the ontology.
     * <p>
     * This constructor is protected to not expose the {@code RelationTO} objects 
     * from the {@code bgee-dao-api} layer. {@code Ontology} objects can only be obtained 
     * through {@code OntologyService}s.
     * 
     * @param elements          A {@code Collection} of {@code T}s that are
     *                          the elements of this ontology.
     * @param relations         A {@code Collection} of {@code RelationTO}s that are
     *                          the relations between elements of the ontology.
     * @param relationTypes     A {@code Collection} of {@code RelationType}s that were
     *                          considered to build this ontology or sub-graph.
     */
    //XXX: when needed, we could add a parameter 'directRelOnly', in case we only want 
    //to retrieve direct parents or children of terms. See method 'getRelatives' 
    //already capable of considering only direct relations.
    protected Ontology(Collection<T> elements, Collection<RelationTO> relations,
            Collection<RelationType> relationTypes) {
        log.entry(elements, relations, relationTypes);
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
        
        log.exit();
    }
    
    /**
     * @return  The {@code Set} of {@code T}s that are the elements that were considered to build 
     *          this ontology or sub-graph.
     */
    public Set<T> getElements() {
        return elements;
    }

    /**
     * @return  The {@code Set} of {@code RelationType}s that were considered to build 
     *          this ontology or sub-graph.
     */
    public Set<RelationType> getRelationTypes() {
        return relationTypes;
    }

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
     * Get ancestors of {@code element} in this ontology based on relations of types {@code relationTypes}.
     * 
     * @param element       A {@code T} that is the element for which ancestors are retrieved.
     * @param relationTypes A {@code Set} of {@code RelationType}s that are the relation
     *                      types to consider.
     * @return              A {@code Set} of {@code T}s that are the ancestors
     *                      of {@code element} in this ontology. Can be empty if {@code element} 
     *                      has no ancestors according to the requested parameters.
     * @throws IllegalArgumentException If {@code element} is {@code null} or is not found 
     *                                  in this {@code Ontology}.
     */
    public Set<T> getAncestors(T element, Collection<RelationType> relationTypes) {
        log.entry(element, relationTypes);
        return log.exit(this.getAncestors(element, relationTypes, false));
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
     *                                  in this {@code Ontology}.
     */
    //TODO: update javadoc for directRelOnly. 
    //TODO: unit test
    public Set<T> getAncestors(T element, Collection<RelationType> relationTypes, boolean directRelOnly) {
        log.entry(element, relationTypes, directRelOnly);
        return log.exit(this.getRelatives(element, true, relationTypes, directRelOnly));
    }

    /**
     * Get ancestors of {@code element} in this ontology based on any relations that were loaded.
     * 
     * @param element       A {@code T} that is the element for which ancestors are retrieved.
     * @return              A {@code Set} of {@code T}s that are the ancestors
     *                      of {@code element} in this ontology. Can be empty if {@code element} 
     *                      has no ancestors according to the loaded information.
     * @throws IllegalArgumentException If {@code element} is {@code null} or is not found 
     *                                  in this {@code Ontology}.
     */
    public Set<T> getAncestors(T element) {
        log.entry(element);
        return log.exit(this.getAncestors(element, false));
    }
    /**
     * Get ancestors of the given {@code element} in this ontology, according to 
     * any {@code RelationType}s that were considered to build this {@code Ontology}.
     * 
     * @param element       A {@code T} that is the element for which ancestors are retrieved.
     * @return              A {@code Set} of {@code T}s that are the ancestors
     *                      of the given {@code element}. Can be empty if {@code element} 
     *                      has no ancestors according to the requested parameters.
     * @throws IllegalArgumentException If {@code element} is {@code null} or is not found 
     *                                  in this {@code Ontology}.
     */
    //TODO: update javadoc for directRelOnly. 
    public Set<T> getAncestors(T element, boolean directRelOnly) {
        log.entry(element, directRelOnly);
        return log.exit(this.getAncestors(element, null, directRelOnly));
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
     *                                  in this {@code Ontology}.
     */
    //TODO: update javadoc for directRelOnly
    public Set<T> getDescendants(T element, Collection<RelationType> relationTypes, boolean directRelOnly) {
        log.entry(element, relationTypes, directRelOnly);
        return log.exit(this.getRelatives(element, false, relationTypes, directRelOnly));
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
     *                                  in this {@code Ontology}.
     */
    public Set<T> getDescendants(T element, Collection<RelationType> relationTypes) {
        log.entry(element, relationTypes);
        return log.exit(this.getDescendants(element, relationTypes, false));
    }
    
    /**
     * Get descendants of the given {@code element} in this ontology, according to 
     * any {@code RelationType}s that were considered to build this {@code Ontology}.
     * 
     * @param element       A {@code T} that is the element for which descendants are retrieved.
     * @return              A {@code Set} of {@code T}s that are the descendants
     *                      of the given {@code element} in this ontology. Can be empty if {@code element} 
     *                      has no descendants according to the requested parameters.
     * @throws IllegalArgumentException If {@code element} is {@code null} or is not found 
     *                                  in this {@code Ontology}.
     */
    public Set<T> getDescendants(T element) {
        log.entry(element);
        return log.exit(this.getDescendants(element, false));
    }
    /**
     * Get descendants of the given {@code element} in this ontology, according to 
     * any {@code RelationType}s that were considered to build this {@code Ontology}.
     * 
     * @param element       A {@code T} that is the element for which descendants are retrieved.
     * @return              A {@code Set} of {@code T}s that are the descendants
     *                      of the given {@code element} in this ontology. Can be empty if {@code element} 
     *                      has no descendants according to the requested parameters.
     * @throws IllegalArgumentException If {@code element} is {@code null} or is not found 
     *                                  in this {@code Ontology}.
     */
    //TODO: update javadoc for directRelOnly
    public Set<T> getDescendants(T element, boolean directRelOnly) {
        log.entry(element, directRelOnly);
        return log.exit(this.getDescendants(element, null, directRelOnly));
    }

    /**
     * Get relatives from the {@code Ontology}. The returned {@code Set} contains
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
     * @return                      A {@code Set} of {@code T}s that are either the sources 
     *                              or the ancestors or descendants of {@code element}, 
     *                              depending on {@code isAncestor}.
     * @throws IllegalArgumentException If {@code element} is {@code null} or is not found 
     *                                  in this {@code Ontology}.
     */
    // XXX could be used in BgeeDBUtils.getIsAPartOfRelativesFromDb()
    private Set<T> getRelatives(T element, boolean isAncestor, Collection<RelationType> relationTypes, 
            boolean directRelOnly) {
        log.entry(element, isAncestor, relationTypes, directRelOnly);
        if (element == null || !this.elements.contains(element)) {
            throw log.throwing(new IllegalArgumentException("Unrecognized element: " + element));
        }
        
        Set<T> relatives = new HashSet<>();
        
        final EnumSet<RelationTO.RelationType> usedRelationTypes = (relationTypes == null?
                EnumSet.allOf(RelationType.class): new HashSet<>(relationTypes))
                .stream()
                .map(Ontology::convertRelationType)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(RelationTO.RelationType.class)));
    
        final Set<RelationTO> filteredRelations = this.relations.stream()
                .filter(r -> usedRelationTypes.contains(r.getRelationType()) && 
                             (!directRelOnly || 
                                  RelationTO.RelationStatus.DIRECT.equals(r.getRelationStatus())))
                .collect(Collectors.toSet());
        
        if (isAncestor) {
            relatives = filteredRelations.stream()
                    .filter(r -> r.getSourceId().equals(element.getId()))
                    .map(r -> this.getElement(r.getTargetId()))
                    .filter(e -> e != null)
                    .collect(Collectors.toSet());
        } else {
            relatives = filteredRelations.stream()
                    .filter(r-> r.getTargetId().equals(element.getId()))
                    .map(r -> this.getElement(r.getSourceId()))
                    .filter(e -> e != null)
                    .collect(Collectors.toSet());
        }
        return log.exit(relatives);
    }
    
    /**
     * Convert a {@code RelationType} from {@code Ontology} to a {@code RelationType} 
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
        Ontology<?> other = (Ontology<?>) obj;
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
        return true;
    }

    @Override
    public String toString() {
        return "Elements: " + elements + " - Relations: " + relations +
                " - Relation types: " + relationTypes;
    }
}
