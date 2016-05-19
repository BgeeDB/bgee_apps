package org.bgee.model.ontology;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.NamedEntity;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;

/**
 * Class allowing to describe a multi-species ontology, or the sub-graph of a multi-species ontology.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, May 2016
 * @version Bgee 13, May 2016
 * @param <T>   The type of element in this ontology or sub-graph.
 */
public class MultiSpeciesOntology<T extends NamedEntity & OntologyElement<T>> extends Ontology<T> {

    private static final Logger log = LogManager.getLogger(MultiSpeciesOntology.class.getName());

    /**
    * Constructor providing the elements, the relations, the relations types,
    * and the relations status of the multi-species ontology.
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
    protected MultiSpeciesOntology(Collection<T> elements, Collection<RelationTO> relations,
            Collection<RelationType> relationTypes) {
        super(elements, relations, relationTypes);
    }
    
    /**
     * Get elements that were considered to build this ontology or sub-graph,
     * filtered by provided species ID.
     * 
     * @param speciesId A {@code String} that is the ID of species allowing  
     *                  to filter the elements to retrieve.
     * @return          The {@code Set} of {@code T}s that are the elements that were considered 
     *                  to build this ontology or sub-graph, filtered by {@code speciesId}.
     */
    public Set<T> getElements(String speciesId) {
        log.entry(speciesId);
        
        // TODO Get stage and anat. entity taxon constraints
        
        // TODO Filter elements according to constraints and species ID
        Set<T> filteredElements = new HashSet<>(this.getElements());
        
        return log.exit(filteredElements);
    }

    /**
     * Get ancestors of {@code element} in this ontology based on relations of types 
     * {@code relationTypes} and  
     * <p>
     * If {@code directRelOnly} is {@code true}, only direct relations incoming from or outgoing to 
     * {@code element} are considered, in order to only retrieve direct parents of {@code element}.
     * 
     * @param speciesId     A {@code String} that is the ID of species allowing  
     *                      to filter the elements to retrieve.
     * @param element       A {@code T} that is the element for which ancestors are retrieved.
     * @param relationTypes A {@code Set} of {@code RelationType}s that are the relation
     *                      types to consider.
     * @param directRelOnly A {@code boolean} defining whether only direct parents
     *                      of {@code element} should be returned.
     * @return              A {@code Set} of {@code T}s that are the ancestors
     *                      of {@code element} in this ontology. Can be empty if {@code element} 
     *                      has no ancestors according to the requested parameters.
     * @throws IllegalArgumentException If {@code element} is {@code null} or is not found 
     *                                  in this {@code Ontology}.
     */
    public Set<T> getAncestors(String speciesId, T element,
            Collection<RelationType> relationTypes, boolean directRelOnly) {
        log.entry(speciesId, element, relationTypes, directRelOnly);
        
        // TODO Get stage and anat. entity taxon constraints
        
        // TODO Filter elements according to constraints and species ID
        Set<T> filteredElements = new HashSet<>(this.getAncestors(element, relationTypes, directRelOnly));

        return log.exit(filteredElements);
    }
    
    /**
     * Get descendants of {@code element} in this ontology based on relations of types {@code relationTypes}.
     * <p>
     * If {@code directRelOnly} is {@code true}, only direct relations incoming from or outgoing to 
     * {@code element} are considered, in order to only retrieve direct children of {@code element}.
     * 
     * @param speciesId     A {@code String} that is the ID of species allowing  
     *                      to filter the elements to retrieve.
     * @param element       A {@code T} that is the element for which descendants are retrieved.
     * @param relationTypes A {@code Set} of {@code RelationType}s that are the relation
     *                      types allowing to filter the relations to retrieve.
     * @param directRelOnly A {@code boolean} defining whether only direct children
     *                      of {@code element} should be returned.
     * @return              A {@code Collection} of {@code T}s that are the descendants
     *                      of the given {@code element}. Can be empty if {@code element} 
     *                      has no descendants according to the requested parameters.
     * @throws IllegalArgumentException If {@code element} is {@code null} or is not found 
     *                                  in this {@code Ontology}.
     */
    public Set<T> getDescendants(String speciesId, T element, Collection<RelationType> relationTypes,
            boolean directRelOnly) {
        log.entry(speciesId, element, relationTypes, directRelOnly);
        
        // TODO Get stage and anat. entity taxon constraints
        
        // TODO Filter elements according to constraints and species ID
        Set<T> filteredElements = new HashSet<>(this.getDescendants(element, relationTypes, directRelOnly));

        return log.exit(filteredElements);
    }
}
