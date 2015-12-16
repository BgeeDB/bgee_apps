package org.bgee.model.ontology;

import java.util.Set;

import org.bgee.model.Entity;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationType;

/**
 * Parent interface of all elements that can be used to define an {@code Ontology}. 
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Déc. 2015
 * @since   Bgee 13, Déc. 2015
 * @param <T>
 */
public interface OntologyElement<T extends Entity & OntologyElement<T>> {

    /**
     * Get ancestors of the given {@code element} in the given {@code ontology}.
     * 
     * @param ontology      An {@code Ontology} that is the ontology in which
     *                      the ancestors are retrieved. 
     * @param element       A {@code T} that is the element for which the ancestors are retrieved.
     * @param relationTypes A {@code Set} of {@code RelationType}s that are the relation 
     *                      types allowing to filter the relations to retrieve.
     * @return              The {@code Set} of {@code T}s thats are the ancestors of
     *                      {@code element} in {@code ontology}.
     */
    default public Set<T> getAncestors(Ontology<T> ontology, T element, Set<RelationType> relationTypes) {
        return ontology.getAncestors(element, relationTypes);
    }
    
    /**
     * Get descendants of the given {@code element} in the given {@code ontology}.
     * 
     * @param ontology      An {@code Ontology} that is the ontology in which
     *                      the descendants are retrieved. 
     * @param element       A {@code T} that is the element for which the descendants are retrieved.
     * @param relationTypes A {@code Set} of {@code RelationType}s that are the relation 
     *                      types allowing to filter the relations to retrieve.
     * @return              The {@code Set} of {@code T}s thats are the descendants of
     *                      {@code element} in {@code ontology}.
     */
    default public Set<T> getDescendants(Ontology<T> ontology, T element, Set<RelationType> relationTypes) {
        return ontology.getDescendants(element, relationTypes);
    }
}
