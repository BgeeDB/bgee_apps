package org.bgee.model.ontology;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
 * @version Bgee 13, July 2016
 * @since   Bgee 13, July 2016
 * @param <T>   The type of element in this ontology or sub-graph.
 */
public class MultiSpeciesOntology<T extends NamedEntity & OntologyElement<T>> 
    extends SpeciesNeutralOntology<T> {

    private static final Logger log = LogManager.getLogger(MultiSpeciesOntology.class.getName());

    /**
     * A {@code Set} of {@code TaxonConstraint}s that are taxon constrains on relations 
     * between {@code OntologyElement}s in this ontology.
     */
    private final Set<TaxonConstraint> relationTaxonConstraints;

    /**
     * A {@code Set} of {@code TaxonConstraint}s that are taxon constrains on 
     * {@code OntologyElement}s in this ontology.
     */
    private final Set<TaxonConstraint> taxonConstraints;

    /**
     * @see #getSpeciesIds()
     */
    private final Set<String> speciesIds;

    /** 
     * Constructor providing the species IDs, the elements, the relations, the relations types, 
     * the service factory, and the type of elements of this ontology.
     * 
     * @param speciesIds        A {@code Collection} of {@code String}s that are the IDs of 
     *                          the species describing this multi-species ontology.
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
    protected MultiSpeciesOntology(Collection<String> speciesIds, Collection<T> elements, 
            Collection<RelationTO> relations, Collection<RelationType> relationTypes,
            ServiceFactory serviceFactory, Class<T> type) {
        super(elements, relations, relationTypes, serviceFactory, type);
        this.speciesIds =  Collections.unmodifiableSet(
                speciesIds == null? new HashSet<>(): new HashSet<>(speciesIds));

        Set<TaxonConstraint> relationTaxonConstraints = new HashSet<>();
        Set<TaxonConstraint> taxonConstraints = new HashSet<>();
        if (type.equals(AnatEntity.class)) {
            relationTaxonConstraints = serviceFactory.getTaxonConstraintService()
                    .loadAnatEntityRelationTaxonConstraintBySpeciesIds(speciesIds)
                    .collect(Collectors.toSet());
            taxonConstraints = serviceFactory.getTaxonConstraintService()
                    .loadAnatEntityTaxonConstraintBySpeciesIds(speciesIds)
                    .collect(Collectors.toSet());
        } else if (type.equals(DevStage.class)) {
            taxonConstraints = serviceFactory.getTaxonConstraintService()
                    .loadDevStageTaxonConstraintBySpeciesIds(speciesIds)
                    .collect(Collectors.toSet());
        } else {
            throw log.throwing(new IllegalArgumentException("Unsupported OntologyElement"));
        }
        this.relationTaxonConstraints = Collections.unmodifiableSet(
                relationTaxonConstraints == null? new HashSet<>(): new HashSet<>(relationTaxonConstraints));
        this.taxonConstraints = Collections.unmodifiableSet(
                taxonConstraints == null? new HashSet<>(): new HashSet<>(taxonConstraints));
    }
    
    /**
     * @return  The {@code Set} of {@code String}s that are the IDs of the species
     *          that were considered to build this ontology or sub-graph.
     */
    public Set<String> getSpeciesIds() {
        return speciesIds;
    }

    /**
     * Get elements that were considered to build this ontology or sub-graph,
     * filtered by {@code speciesIds}.
     * 
     * @param speciesIds    A {@code Collection} of {@code String}s that is the IDs of species
     *                      allowing to filter the elements to retrieve.
     * @return              The {@code Set} of {@code T}s that are the elements that were considered 
     *                      to build this ontology or sub-graph, filtered by {@code speciesId}.
     */
    public  Set<T> getElements(Collection<String> speciesIds) {
        log.entry(speciesIds);
        
        // Get stage or anat. entity IDs according to taxon constraints
        Set<String> entityIds = taxonConstraints.stream()
                .filter(tc -> tc.getSpeciesId() == null || speciesIds.contains(tc.getSpeciesId()))
                .map(tc -> tc.getEntityId())
                .collect(Collectors.toSet());
                
        // Filter elements according to taxon constraints
        return log.exit(this.getElements().stream()
                .filter(e -> entityIds.contains(e.getId()))
                .collect(Collectors.toSet()));
    }
    
    /**
     * Get relations that were considered to build this ontology or sub-graph,
     * filtered by {@code speciesIds}.
     * 
     * @param speciesIds    A {@code Collection} of {@code String}s that is the IDs of species
     *                      allowing to filter the relations to retrieve.
     * @return              The {@code Set} of {@code RelationTO}s that are the relations that were
     *                      considered to build this ontology or sub-graph, filtered by {@code speciesId}.
     */
    public  Set<RelationTO> getRelations(Collection<String> speciesIds) {
        log.entry(speciesIds);
        
        // Get relation IDs according to taxon constraints
        Set<String> relationsIds = relationTaxonConstraints.stream()
                .filter(tc -> tc.getSpeciesId() == null || speciesIds.contains(tc.getSpeciesId()))
                .map(tc -> tc.getEntityId())
                .collect(Collectors.toSet());
                
        // Filter relations according to taxon constraints
        return log.exit(this.getRelations().stream()
                .filter(r -> relationsIds.contains(r.getId()))
                .collect(Collectors.toSet()));
    }
    
    /**
     * Get ancestors of the given {@code element} in this ontology and in {@code speciesIds},
     * according to any {@code RelationType}s that were considered to build this {@code SpeciesNeutralOntology}.
     * <p>
     * If {@code directRelOnly} is {@code true}, only direct relations incoming from or outgoing to 
     * {@code element} are considered, in order to only retrieve direct parents of {@code element}.
     * 
     * @param element       A {@code T} that is the element for which ancestors are retrieved.
     * @param directRelOnly A {@code boolean} defining whether only direct parents
     *                      of {@code element} should be returned.
     * @param speciesIds    A {@code Collection} of {@code String}s that is the IDs of species
     *                      allowing to filter the elements to retrieve.
     * @return              A {@code Set} of {@code T}s that are the ancestors
     *                      of the given {@code element}. Can be empty if {@code element} 
     *                      has no ancestors according to the requested parameters.
     * @throws IllegalArgumentException If {@code element} is {@code null} or is not found 
     *                                  in this {@code SpeciesNeutralOntology}.
     */
    public Set<T> getAncestors(T element, boolean directRelOnly, Collection<String> speciesIds) {
        log.entry(element, directRelOnly, speciesIds);
        return log.exit(this.getAncestors(element, null, directRelOnly, speciesIds));
    }

    /**
     * Get ancestors of {@code element} in this ontology based on relations of types 
     * {@code relationTypes}, filtered by {@code speciesIds}.  
     * <p>
     * If {@code directRelOnly} is {@code true}, only direct relations incoming from or outgoing to 
     * {@code element} are considered, in order to only retrieve direct parents of {@code element}.
     * 
     * @param element       A {@code T} that is the element for which ancestors are retrieved.
     * @param relationTypes A {@code Set} of {@code RelationType}s that are the relation
     *                      types to consider.
     * @param directRelOnly A {@code boolean} defining whether only direct parents
     *                      of {@code element} should be returned.
     * @param speciesIds    A {@code Collection} of {@code String}s that is the IDs of species
     *                      allowing to filter the elements to retrieve.
     * @return              A {@code Set} of {@code T}s that are the ancestors
     *                      of {@code element} in this ontology. Can be empty if {@code element} 
     *                      has no ancestors according to the requested parameters.
     * @throws IllegalArgumentException If {@code element} is {@code null} or is not found 
     *                                  in this {@code SpeciesNeutralOntology}.
     */
    public Set<T> getAncestors(T element, Collection<RelationType> relationTypes, 
            boolean directRelOnly, Collection<String> speciesIds) {
        log.entry(element, relationTypes, directRelOnly, speciesIds);
        return log.exit(this.getRelatives(element, true, relationTypes, directRelOnly, 
                speciesIds, relationTaxonConstraints));
        
        
    }
    
    /**
     * Get descendants of the given {@code element} in this ontology in {@code speciesIds}, 
     * according to any {@code RelationType}s that were considered to build this {@code SpeciesNeutralOntology}.
     * <p>
     * If {@code directRelOnly} is {@code true}, only direct relations incoming from or outgoing to 
     * {@code element} are considered, in order to only retrieve direct children of {@code element}.
     * 
     * @param element       A {@code T} that is the element for which descendants are retrieved.
     * @param directRelOnly A {@code boolean} defining whether only direct children
     *                      of {@code element} should be returned.
     * @param speciesIds    A {@code Collection} of {@code String}s that is the IDs of species
     *                      allowing to filter the elements to retrieve.
     * @return              A {@code Set} of {@code T}s that are the descendants
     *                      of the given {@code element} in this ontology. Can be empty if {@code element} 
     *                      has no descendants according to the requested parameters.
     * @throws IllegalArgumentException If {@code element} is {@code null} or is not found 
     *                                  in this {@code SpeciesNeutralOntology}.
     */
    public Set<T> getDescendants(T element, boolean directRelOnly, Collection<String> speciesIds) {
        log.entry(element, directRelOnly, speciesIds);
        return log.exit(this.getDescendants(element, null, directRelOnly,speciesIds));
    }
    
    /**
     * Get descendants of {@code element} in this ontology based on relations of types
     * {@code relationTypes}, filtered by {@code speciesIds}.
     * <p>
     * If {@code directRelOnly} is {@code true}, only direct relations incoming from or outgoing to 
     * {@code element} are considered, in order to only retrieve direct children of {@code element}.
     * 
     * @param element       A {@code T} that is the element for which descendants are retrieved.
     * @param relationTypes A {@code Set} of {@code RelationType}s that are the relation
     *                      types allowing to filter the relations to retrieve.
     * @param directRelOnly A {@code boolean} defining whether only direct children
     *                      of {@code element} should be returned.
     * @param speciesIds    A {@code Collection} of {@code String}s that are the IDs of species
     *                      allowing to filter the elements to retrieve.
     * @return              A {@code Collection} of {@code T}s that are the descendants
     *                      of the given {@code element}. Can be empty if {@code element} 
     *                      has no descendants according to the requested parameters.
     * @throws IllegalArgumentException If {@code element} is {@code null} or is not found 
     *                                  in this {@code SpeciesNeutralOntology}.
     */
    public Set<T> getDescendants(T element, Collection<RelationType> relationTypes, 
            boolean directRelOnly, Collection<String> speciesIds) {
        log.entry(element, relationTypes, directRelOnly, speciesIds);
        return log.exit(this.getRelatives(element, false, relationTypes, directRelOnly, 
                speciesIds, relationTaxonConstraints));
    }

    /** 
     * Get the {@code Ontology} of the provided {@code speciesId} 
     * from this {@code MultiSpeciesOntology}.
     * 
     * @param speciesId A {@code String} that is the ID of species of 
     *                  which the ontology should be retrieved.
     * @return          The {@code Ontology} of the provided {@code speciesId}.
     * @throws IllegalArgumentException If {@code speciesId} is {@code null} or if 
     *                                  the {@code speciesId} is not in this {@code SpeciesNeutralOntology}.
     */
    public Ontology<T> getAsSingleSpeciesOntology(String speciesId) {
        log.entry(speciesId);
        if (speciesId == null) {
            throw log.throwing(new IllegalArgumentException("A species ID should be provided"));
        }

        if (!this.getSpeciesIds().contains(speciesId)) {
            throw log.throwing(new IllegalArgumentException(
                    "Species ID should be in this multi-species ontology"));
        }
       
        return log.exit(new Ontology<>(speciesId, this.getElements(Arrays.asList(speciesId)),
                this.getRelations(Arrays.asList(speciesId)), this.getRelationTypes(),
                this.getServiceFactory(), this.getType()));
    }
}