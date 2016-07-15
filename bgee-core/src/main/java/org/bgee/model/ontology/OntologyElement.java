package org.bgee.model.ontology;

import java.util.Collection;
import java.util.Set;

import org.bgee.model.NamedEntity;

/**
 * Parent interface of all elements that can be used in ontologies. 
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Déc. 2015
 * @since   Bgee 13, Déc. 2015
 * @see Ontology
 * @see MultiSpeciesOntology
 * @param <T>
 */
public interface OntologyElement<T extends NamedEntity & OntologyElement<T>> {

    /**
     * Get ancestors of the given {@code element} in the given {@code ontology}.
     * 
     * @param ontology      An {@code OntologyBase} that is the ontology in which
     *                      the ancestors are retrieved. 
     * @param relationTypes A {@code Collection} of {@code RelationType}s that are the relation 
     *                      types allowing to filter the relations to retrieve.
     * @return              The {@code Set} of {@code T}s thats are the ancestors of
     *                      {@code element} in {@code ontology}.
     */
    default public Set<T> getAncestors(OntologyBase<T> ontology, Collection<RelationType> relationTypes) {
        //XXX: we need to ensure that the type of this OntologyElement is indeed T 
        //(i.e., to guarantee that we never have DevStage implements OntologyElement<AnatEntity>). 
        //leaving the warning for now.
        return ontology.getAncestors((T) this, relationTypes);
    }
    
    /**
     * Get descendants of the given {@code element} in the given {@code ontology}.
     * 
     * @param ontology      An {@code OntologyBase} that is the ontology in which
     *                      the descendants are retrieved. 
     * @param relationTypes A {@code Set} of {@code RelationType}s that are the relation 
     *                      types allowing to filter the relations to retrieve.
     * @return              The {@code Set} of {@code T}s thats are the descendants of
     *                      {@code element} in {@code ontology}.
     */
    default public Set<T> getDescendants(OntologyBase<T> ontology, Set<RelationType> relationTypes) {
        //XXX: we need to ensure that the type of this OntologyElement is indeed T 
        //(i.e., to guarantee that we never have DevStage implements OntologyElement<AnatEntity>). 
        //leaving the warning for now.
        return ontology.getDescendants((T) this, relationTypes);
    }
}
