package org.bgee.model.ontology;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.NamedEntity;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.TaxonConstraint;
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
     * The {@code AnatEntityService} to obtain {@code AnatEntity} objects.
     */
    private final ServiceFactory serviceFactory;

    private final Class<T> type;
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
     * @param serviceFactory    A {@code ServiceFactory} to acquire {@code Service}s from.
     */
    protected MultiSpeciesOntology(Collection<T> elements, Collection<RelationTO> relations,
            Collection<RelationType> relationTypes, ServiceFactory serviceFactory,
            Class<T> type) {
        super(elements, relations, relationTypes);
        this.serviceFactory = serviceFactory;
        this.type = type;
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

        // Get stage or anat. entity taxon constraints
        Set<String> entityIds = 
                this.getTaxonConstraintEntityIds(new HashSet<>(Arrays.asList(speciesId)));
                
        // Filter elements according to taxon constraints
        return log.exit(this.getElements().stream()
                .filter(e -> entityIds.contains(e.getId()))
                .collect(Collectors.toSet()));
    }
    
    private Set<String> getTaxonConstraintEntityIds(Set<String> speciesIds) {
        log.entry(speciesIds);

        Stream<TaxonConstraint> taxonConstraints;
        if (type.equals(AnatEntity.class)) {
            taxonConstraints = serviceFactory.getTaxonConstraintService()
                    .loadAnatEntityTaxonConstraintBySpeciesIds(speciesIds);
        } else if (type.equals(DevStage.class)) {
            taxonConstraints = serviceFactory.getTaxonConstraintService()
                    .loadDevStageTaxonConstraintBySpeciesIds(speciesIds);
        } else {
            throw log.throwing(new IllegalArgumentException("Unsupported OntologyElement"));
        }
        
        return log.exit(taxonConstraints.map(ds -> ds.getEntityId()).collect(Collectors.toSet()));
    }
    
    private Set<String> getTaxonConstraintRelationIds(String speciesId) {
        log.entry(speciesId);
        
        Stream<TaxonConstraint> taxonConstraints;
        if (type.equals(AnatEntity.class)) {
            taxonConstraints = serviceFactory.getTaxonConstraintService()
                    .loadAnatEntityRelationTaxonConstraintBySpeciesIds(new HashSet<>(Arrays.asList(speciesId)));
        } else {
            throw log.throwing(new IllegalArgumentException("Unsupported OntologyElement"));
        }
        
        return log.exit(taxonConstraints.map(ds -> ds.getEntityId()).collect(Collectors.toSet()));
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
        
        return log.exit(this.getRelatives(speciesId, element, true, relationTypes, directRelOnly));
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
        return log.exit(this.getRelatives(speciesId, element, false, relationTypes, directRelOnly));
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
     * @param speciesId             A {@code String} that is the ID of species allowing  
     *                              to filter the elements to retrieve.
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
    // TODO refactor with Ontology.getRelatives()
    private Set<T> getRelatives(String speciesId, T element, boolean isAncestor,
            Collection<RelationType> relationTypes, boolean directRelOnly) {
        log.entry(speciesId, element, isAncestor, relationTypes, directRelOnly);
        
        if (element == null || !this.getElements(speciesId).contains(element)) {
            throw log.throwing(new IllegalArgumentException("Unrecognized element: " + element));
        }
        
        Set<T> relatives = new HashSet<>();
        
        final EnumSet<RelationTO.RelationType> usedRelationTypes = (relationTypes == null?
                EnumSet.allOf(RelationType.class): new HashSet<>(relationTypes))
                .stream()
                .map(Ontology::convertRelationType)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(RelationTO.RelationType.class)));
        
        Set<String> allowedRelationIds = speciesId != null && type.equals(AnatEntity.class) ?
             this.getTaxonConstraintRelationIds(speciesId) : null;

        final Set<RelationTO> filteredRelations = this.getRelationTOs().stream()
                .filter(r -> usedRelationTypes.contains(r.getRelationType()) && 
                             (!directRelOnly || 
                                  RelationTO.RelationStatus.DIRECT.equals(r.getRelationStatus())))
                .filter(r -> allowedRelationIds == null || allowedRelationIds.contains(r.getId()))
                .collect(Collectors.toSet());
        
        Set<T> allowedElements = speciesId != null ? this.getElements(speciesId) : this.getElements();
        Set<String> allowedEntityIds = allowedElements.stream().map(e -> e.getId()).collect(Collectors.toSet());

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

}
