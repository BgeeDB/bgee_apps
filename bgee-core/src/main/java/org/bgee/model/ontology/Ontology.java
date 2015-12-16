package org.bgee.model.ontology;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Entity;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;

/**
 * Class allowing to describe an ontology. 
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Dec. 2013
 * @since   Bgee 13, Dec. 2013
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
        // TODO add sanity checks? elements and relations can be null?
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
     * @param childrenFromParents   A {@code boolean} defining whether the returned {@code Map} 
     *                              will associate a source to its targets, or a target 
     *                              to its sources. If {@code true}, it will associate 
     *                              a source to its targets.
     * @return                      A {@code Map} where keys are {@code String}s representing the  
     *                              {@code T}s of either the sources or the targets of relations,   
     *                              the associated value being {@code Set} of {@code T}s that are  
     *                              the elements of either the associated targets, or sources,  
     *                              respectively. If {@code childrenFromParents} is {@code true}, 
     *                              it will associate sources to their targets. 
     */
    // TODO DRY in BgeeUtils.getIsAPartOfRelativesFromDb()
    private Map<T, Set<T>> getRelatives(boolean childrenFromParents) {
        log.entry(childrenFromParents);

        Map<T, Set<T>> relativesMap = new HashMap<>();

        for (RelationTO relTO: this.relations) {
            T key = null;
            T value = null;
            if (childrenFromParents) {
                key = this.getElement(relTO.getTargetId());
                value = this.getElement(relTO.getSourceId());
            } else {
                key = this.getElement(relTO.getSourceId());
                value = this.getElement(relTO.getTargetId());
            }
            // TODO add sanity checks (if key and/or value are null)
            Set<T> relatives = relativesMap.get(key);
            if (relatives == null) {
                relatives = new HashSet<>();
                relativesMap.put(key, relatives);
            }
            relatives.add(value);
        }
        return log.exit(relativesMap);
    }

    /**
     * Get the element corresponding to the given {@code id}.
     * 
     * @param id    A {@code String} that is the ID of the element to be retrieved. 
     * @return      A {@code T} that is the element corresponding to the given {@code id}.
     */
    private T getElement(String id) {
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
     * @param element   A {@code T} that is the element for which ancestors are recovered. 
     * @return          A {@code Set} of {@code T}s that are the ancestors
     *                  of the given {@code element}.
     */
    public Set<T> getAncestors(T element) {
        log.entry(element);
        // TODO add sanity checks
        return log.exit(getRelatives(false).get(element));
    }

    /**
     * Get descendants of the given {@code element}.
     * 
     * @param element   A {@code T} that is the element for which descendants are recovered. 
     * @return          A {@code Set} of {@code T}s that are the descendants
     *                  of the given {@code element}.
     */
    public Set<T> getDescendants(T element) {
        log.entry(element);
        // TODO add sanity checks
        return log.exit(getRelatives(true).get(element));
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
