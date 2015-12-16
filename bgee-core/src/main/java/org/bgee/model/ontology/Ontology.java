package org.bgee.model.ontology;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Entity;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationType;

/**
 * Class allowing to describe an ontology. 
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Dec. 2015
 * @since   Bgee 13, Dec. 2015
 * @param <T>
 */
public class Ontology<T extends Entity & OntologyElement<T>> {

    private static final Logger log = LogManager.getLogger(Ontology.class.getName());

    /**
     * A {@code Collection} of {@code T}s that are the elements of the ontology.
     */
    private Set<T> elements;

    /**
     * A {@code Collection} of {@code RelationTO}s that are the relations
     * between elements of the ontology.
     */
    private Set<RelationTO> relations;

    /**
     * Constructor providing the {@code elements} and the {relations} of the ontology.
     * 
     * @param elements  A {@code Collection} of {@code T}s that are the elements of the ontology. 
     * @param relations A {@code Collection} of {@code RelationTO}s that are the relations
     *                  between elements of the ontology.
     */
    protected Ontology(Collection<T> elements, Collection<RelationTO> relations) {
        // XXX elements and relations can be null?
        this.elements = Collections.unmodifiableSet(
                elements == null? new HashSet<>(): new HashSet<>(elements));
        this.relations = Collections.unmodifiableSet(
                relations == null? new HashSet<>(): new HashSet<>(relations));
    }

    /**
     * Get relatives from the {@code Ontology}. The returned {@code Map} contains 
     * either elements of the ontology retrieved from {@code relations} of the ontology.
     * If {@code childrenFromParents} is {@code true}, the keys in the returned {@code Map} are
     * source elements of the relations, the associated value being a {@code Set} that are the
     * elements of their associated targets.
     * If {@code childrenFromParents} is {@code false}, the keys in the returned {@code Map}
     * are target elements of the relations, the associated value being a {@code Set} that are
     * the elements of their associated sources.
     * 
     * @param element               A {@code T} that is the element for which relatives are recovered. 
     * @param isAncestors           A {@code boolean} defining whether the returned {@code Set}
     *                              are ancestors or descendants. If {@code true},
     *                              it will retrieved ancestors.
     * @param relationTypes         A {@code Set} of {@code RelationType}s that are the relation 
     *                              types allowing to filter the relations to retrieve.
     * @return                      A {@code Map} where keys are {@code String}s representing the  
     *                              {@code T}s of either the sources or the targets of relations,   
     *                              the associated value being {@code Set} of {@code T}s that are  
     *                              the elements of either the associated targets, or sources,  
     *                              respectively. If {@code childrenFromParents} is {@code true}, 
     *                              it will associate sources to their targets. 
     * @throws IllegalStateException    If no element is found for a source or a target ID of
     *                                  a relation of the ontology.
     */
    // TODO DRY in BgeeUtils.getIsAPartOfRelativesFromDb()
    private Set<T> getRelatives(T element, boolean isAncestors, Set<RelationType> relationTypes) {
        log.entry(element, isAncestors, relationTypes);

        Set<T> relatives = new HashSet<>();
        
        Set<RelationType> usedRelationTypes = relationTypes == null? 
                new HashSet<>(EnumSet.allOf(RelationType.class)): new HashSet<>(relationTypes);

        Set<RelationTO> filteredRelations = this.relations.stream()
                .filter(r -> usedRelationTypes.contains(r.getRelationType()))
                .collect(Collectors.toSet());
        
        if (isAncestors) {
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
     * Get ancestors of the given {@code element}.
     * 
     * @param element       A {@code T} that is the element for which ancestors are recovered. 
     * @param relationTypes A {@code Set} of {@code RelationType}s that are the relation 
     *                      types allowing to filter the relations to retrieve.
     * @return              A {@code Set} of {@code T}s that are the ancestors
     *                      of the given {@code element}.
     * @throws IllegalArgumentException If {@code element} is {@code null}.
     */
    public Set<T> getAncestors(T element, Set<RelationType> relationTypes) {
        log.entry(element, relationTypes);
        if (element == null) {
            throw log.throwing(new IllegalArgumentException("Element is null"));
        }
        return log.exit(this.getRelatives(element, true, relationTypes));
    }

    /**
     * Get descendants of the given {@code element}.
     * 
     * @param element       A {@code T} that is the element for which descendants are recovered. 
     * @param relationTypes A {@code Set} of {@code RelationType}s that are the relation 
     *                      types allowing to filter the relations to retrieve.
     * @return              A {@code Set} of {@code T}s that are the descendants
     *                      of the given {@code element}.
     * @throws IllegalArgumentException If {@code element} is {@code null}.
     */
    public Set<T> getDescendants(T element, Set<RelationType> relationTypes) {
        log.entry(element, relationTypes);
        if (element == null) {
            throw log.throwing(new IllegalArgumentException("Element is null"));
        }
        return log.exit(this.getRelatives(element, false, relationTypes));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((elements == null) ? 0 : elements.hashCode());
        result = prime * result + ((relations == null) ? 0 : relations.hashCode());
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
        return true;
    }

    @Override
    public String toString() {
        return "Elements: " + elements + " - Relations: " + relations;
    } 
}
