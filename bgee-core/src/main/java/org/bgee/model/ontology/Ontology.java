package org.bgee.model.ontology;

import java.util.Collection;

import org.bgee.model.NamedEntity;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;

/**
 * Class allowing to describe a single-species ontology or species-agnostic ontology (for instance, 
 * taxonomy ontology).
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, July 2016
 * @since   Bgee 13, July 2016
 * @param <T>   The type of element in this ontology or sub-graph.
 * @param <U>   The type of ID of the elements in this ontology or sub-graph.
 */
public class Ontology<T extends NamedEntity<U> & OntologyElement<T, U>, U extends Comparable<U>>
    extends OntologyBase<T, U> {
    
    /**
     * @see #getSpeciesId()
     */
    private final Integer speciesId;

    /** 
     * Constructor providing the species IDs, the elements, the relations, the relations types, 
     * the service factory, and the type of elements of this ontology.
     * 
     * @param speciesId         An {@code Integer} that is the ID of the species describing this 
     *                          single-species ontology. Can be {@code null} if the ontology 
     *                          is species-agnostic (for instance, taxonomy ontology).
     * @param elements          A {@code Collection} of {@code T}s that are
     *                          the elements of this ontology.
     * @param relations         A {@code Collection} of {@code RelationTO}s that are
     *                          the relations between elements of the ontology.
     * @param relationTypes     A {@code Collection} of {@code RelationType}s that were
     *                          considered to build this ontology or sub-graph.
     * @param serviceFactory    A {@code ServiceFactory} to acquire {@code Service}s from.
     * @param type              A {@code Class<T>} that is the type of {@code elements} 
     *                          to be store by this {@code Ontology}.
     */
    public Ontology(Integer speciesId, Collection<T> elements,
            Collection<RelationTO<U>> relations, Collection<RelationType> relationTypes,
            ServiceFactory serviceFactory, Class<T> type) {
        super(elements, relations, relationTypes, serviceFactory, type);
        this.speciesId = speciesId;
    }
    
    /**
     * @return  The {@code Integer} that is the ID of the species that was considered
     *          to build of this ontology or sub-graph. {@code null} if the ontology 
     *          is species-agnostic (for instance, taxonomy ontology).
     */
    public Integer getSpeciesId() {
        return speciesId;
    }
}