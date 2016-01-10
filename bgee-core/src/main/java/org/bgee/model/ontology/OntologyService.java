package org.bgee.model.ontology;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.NamedEntity;
import org.bgee.model.Service;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.DevStageService;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationStatus;
import org.bgee.model.ontology.Ontology.RelationType;

/**
 * A {@link Service} to obtain {@link Ontology} objects.
 * Users should use the {@link ServiceFactory} to obtain {@code OntologyService}s.
 * 
 * @author  Valentine Rech de Laval
 * @author Frederic Bastian
 * @version Bgee 13, Jan. 2016
 * @since   Bgee 13, Dec. 2015
 * @param <T>
 */
//TODO: unit tests
public class OntologyService extends Service {

    private static final Logger log = LogManager.getLogger(OntologyService.class.getName());

    /**
     * 0-arg constructor that will cause this {@code OntologyService} to use
     * the default {@code DAOManager} returned by {@link DAOManager#getDAOManager()}.
     * 
     * @see #OntologyService(DAOManager)
     */
    public OntologyService() {
        this(DAOManager.getDAOManager());
    }

    /**
     * Constructs a {@code OntologyService}.
     * 
     * @param daoManager                The {@code DAOManager} to be used by this
     *                                  {@code OntologyService} to obtain {@code DAO}s.
     * @throws IllegalArgumentException If {@code daoManager} is {@code null}.
     */
    public OntologyService(DAOManager daoManager) {
        super(daoManager);
    }
        
    /**
     * Retrieve the {@code Ontology} of {@code AnatEntity}s for the requested species, anatomical entities,
     * relations types, and relation status. 
     * <p>
     * The returned {@code Ontology} contains ancestors and/or descendants of the selected anat. entites 
     * according to {@code getAncestors} and {@code getDescendants}, respectively. 
     * If both {@code getAncestors} and {@code getDescendants} are {@code false}, 
     * then only relations between the selected anat. entities are retrieved.
     * 
     * @param speciesIds        A {@code Collection} of {@code String}s that are IDs of species 
     *                          which to retrieve anat. entities for. If several IDs are provided, 
     *                          anat. entities existing in any of them will be retrieved. 
     *                          Can be {@code null} or empty.
     * @param anatEntityIds     A {@code Collection} of {@code String}s that are IDs of anat.
     *                          entity IDs to retrieve. Can be {@code null} or empty.
     * @param relationTypes     A {@code Collection} of {@code RelationType}s that are the relation
     *                          types allowing to filter the relations between elements
     *                          of the {@code Ontology}.
     * @param getAncestors      A {@code boolean} defining whether the ancestors of the selected 
     *                          anat. entities, and the relations leading to them, should be retrieved.
     * @param getDescendants    A {@code boolean} defining whether the descendants of the selected 
     *                          anat. entities, and the relations leading to them, should be retrieved.
     * @param service           An {@code AnatEntityService} to retrieve information about {@code AnatEntity}.
     * @return                  The {@code Ontology} of {@code AnatEntity}s for the requested species, 
     *                          anat. entity, relations types, and relation status.
     */
    public Ontology<AnatEntity> getAnatEntityOntology(Collection<String> speciesIds, 
            Collection<String> anatEntityIds, Collection<RelationType> relationTypes, 
            boolean getAncestors, boolean getDescendants, AnatEntityService service) {
        log.entry(speciesIds, anatEntityIds, getAncestors, getDescendants, relationTypes, service);
        
        return log.exit(this.loadOntology(AnatEntity.class, speciesIds, anatEntityIds, 
                relationTypes, getAncestors, getDescendants, 
                (speIds, entityIds) -> service.loadAnatEntities(speIds, true, entityIds)
                    .collect(Collectors.toSet())));
    }
    
    /**
     * Retrieve the {@code Ontology} of {@code AnatEntity}s for the requested species and anatomical entities. 
     * <p>
     * The returned {@code Ontology} contains only the selected anat. entites, and only the relations 
     * between them with a {@code RelationType} {@code ISA_PARTOF} are included.
     * 
     * @param speciesIds        A {@code Collection} of {@code String}s that are IDs of species 
     *                          which to retrieve anat. entities for. If several IDs are provided, 
     *                          anat. entities existing in any of them will be retrieved. 
     *                          Can be {@code null} or empty.
     * @param anatEntityIds     A {@code Collection} of {@code String}s that are IDs of anat.
     *                          entity IDs to retrieve. Can be {@code null} or empty.
     * @param service           An {@code AnatEntityService} to retrieve information about {@code AnatEntity}.
     * @return                  The {@code Ontology} of {@code AnatEntity}s for the requested species 
     *                          and anat. entity.
     */
    public Ontology<AnatEntity> getAnatEntityOntology(Collection<String> speciesIds, 
            Collection<String> anatEntityIds, AnatEntityService service) {
        log.entry(speciesIds, anatEntityIds, service);
        return log.exit(this.getAnatEntityOntology(speciesIds, anatEntityIds,
                EnumSet.of(RelationType.ISA_PARTOF), false, false, service));
    }
    
    /**
     * Retrieve the {@code Ontology} of {@code DevStage}s for the requested species, dev. stage IDs,
     * and relation status. 
     * <p>
     * The returned {@code Ontology} contains ancestors and/or descendants according to
     * {@code getAncestors} and {@code getDescendants}, respectively. 
     * If both {@code getAncestors} and {@code getDescendants} are {@code false}, 
     * then only relations between provided developmental stages are considered.
     * 
     * @param speciesIds        A {@code Collection} of {@code String}s that are IDs of species 
     *                          which to retrieve de. stages for. If several IDs are provided, 
     *                          dev. stages existing in any of them will be retrieved. 
     *                          Can be {@code null} or empty.
     * @param devStageIds       A {@code Collection} of {@code String}s that are dev. stages IDs
     *                          of the {@code Ontology} to retrieve. Can be {@code null} or empty.
     * @param getAncestors      A {@code boolean} defining whether the ancestors of the selected 
     *                          dev. stages, and the relations leading to them, should be retrieved.
     * @param getDescendants    A {@code boolean} defining whether the descendants of the selected 
     *                          dev. stages, and the relations leading to them, should be retrieved.
     * @param service           An {@code DevStageService} that provides bgee services.
     * @return                  The {@code Ontology} of the {@code DevStage}s for the requested species, 
     *                          dev. stages, and relation status. 
     */
    public Ontology<DevStage> getDevStageOntology(Collection<String> speciesIds, 
            Collection<String> devStageIds, boolean getAncestors, boolean getDescendants, 
            DevStageService service) {
        log.entry(speciesIds, devStageIds, getAncestors, getDescendants, service);
        
        return log.exit(this.loadOntology(DevStage.class, speciesIds, devStageIds, 
                EnumSet.of(RelationType.ISA_PARTOF), getAncestors, getDescendants, 
                (speIds, entityIds) -> service.loadDevStages(speIds, true, entityIds)
                    .collect(Collectors.toSet())));
    }

    /**
     * Retrieve the {@code Ontology} of {@code DevStage}s for the requested species and 
     * developmental stages IDs.
     * <p>
     * The returned {@code Ontology} contains only {@code DevStage}s corresponding to 
     * the provided dev. stages IDs, and only the relations between them 
     * with a {@code RelationType} {@code ISA_PARTOF} are included. 
     * 
     * @param speciesIds        A {@code Collection} of {@code String}s that are IDs of species 
     *                          which to retrieve de. stages for. If several IDs are provided, 
     *                          dev. stages existing in any of them will be retrieved. 
     *                          Can be {@code null} or empty.
     * @param devStageIds       A {@code Collection} of {@code String}s that are dev. stages IDs
     *                          of the {@code Ontology} to retrieve. Can be {@code null} or empty.
     * @param service           An {@code DevStageService} that provides bgee services.
     * @return                  The {@code Ontology} of the {@code DevStage}s for the requested species 
     *                          and dev. stages.
     */
    public Ontology<DevStage> getDevStageOntology(Collection<String> speciesIds, 
            Collection<String> devStageIds, DevStageService service) {
        log.entry(speciesIds, devStageIds, service);
        return this.getDevStageOntology(speciesIds, devStageIds, false, false, service);
    }

    /**
     * Convenience method to load any ontology. 
     * 
     * @param elementType           A {@code Class<T>} that is the type of the elements 
     *                              in the returned {@code Ontology}.
     * @param speciesIds            A {@code Collection} of {@code String}s that are IDs of species 
     *                              which to retrieve entities for. If several IDs are provided, 
     *                              entities existing in any of them will be retrieved. 
     *                              Can be {@code null} or empty.
     * @param entityIds             A {@code Collection} of {@code String}s that are IDs of 
     *                              entities to retrieve. Can be {@code null} or empty.
     * @param relationTypes         A {@code Collection} of {@code RelationType}s that are the relation
     *                              types allowing to filter the relations between elements
     *                              of the {@code Ontology}.
     * @param getAncestors          A {@code boolean} defining whether the ancestors of the selected 
     *                              entities, and the relations leading to them, should be retrieved.
     * @param getDescendants        A {@code boolean} defining whether the descendants of the selected 
     *                              entities, and the relations leading to them, should be retrieved.
     * @param loadEntityFunction    A {@code BiFunction} responsible for returning a {@code Collection} 
     *                              of {@code T}s, accepting as first argument a {@code Collection} 
     *                              of {@code String}s that are the IDs of requested species, 
     *                              and as second argument a {@code Collection} of {@code String}s 
     *                              that are the IDs of selected entities.
     * @return                      An {@code Ontology} of {@code T} properly loaded according to 
     *                              the requested parameters.
     * @param <T>                   The type of elements in the returned {@code Ontology}.
     */
    private <T extends NamedEntity & OntologyElement<T>> Ontology<T> loadOntology(Class<T> elementType, 
            Collection<String> speciesIds, Collection<String> entityIds, 
            Collection<RelationType> relationTypes, boolean getAncestors, boolean getDescendants, 
            BiFunction<Collection<String>, Collection<String>, Collection<T>> loadEntityFunction) {
        log.entry(speciesIds, entityIds, getAncestors, getDescendants, relationTypes, loadEntityFunction);
        
        final Set<String> filteredEntities = Collections.unmodifiableSet(
                entityIds == null? new HashSet<>(): new HashSet<>(entityIds));
        final Set<String> clonedSpeIds = Collections.unmodifiableSet(
                speciesIds == null? new HashSet<>(): new HashSet<>(speciesIds));
    
        // Currently, we use all non reflexive relations.
        Set<RelationStatus> relationStatus = EnumSet.complementOf(EnumSet.of(RelationStatus.REFLEXIVE));
        
        //by default, include all ancestors and descendants of selected entities
        Set<String> sourceAnatEntityIds = filteredEntities;
        Set<String> targetAnatEntityIds = filteredEntities;
        boolean sourceOrTarget = true;
        if (!getAncestors && !getDescendants) {
            //request only relations between selected entities (constraints both sources and targets 
            //of considered relations to be one of the selected entities).
            sourceOrTarget = false;
        } else if (!getAncestors) {
            //to not get ancestors, we don't select relations where selected entites are sources
            sourceAnatEntityIds = null;
        } else if (!getDescendants) {
            //opposite if we don't want the descendants
            targetAnatEntityIds = null;
        }
        
        Collection<RelationTO> relations = null;
        if (AnatEntity.class.isAssignableFrom(elementType)) {
            relations = getDaoManager().getRelationDAO().getAnatEntityRelations(clonedSpeIds, true, 
                        sourceAnatEntityIds, targetAnatEntityIds, sourceOrTarget, 
                        relationTypes.stream()
                                .map(Ontology::convertRelationType)
                                .collect(Collectors.toCollection(() -> 
                                    EnumSet.noneOf(RelationTO.RelationType.class))), 
                        relationStatus, 
                        null)
                    .getAllTOs();
        } else if (DevStage.class.isAssignableFrom(elementType)) {
            relations = getDaoManager().getRelationDAO().getStageRelations(clonedSpeIds, true, 
                        sourceAnatEntityIds, targetAnatEntityIds, sourceOrTarget, relationStatus, null)
                    .getAllTOs();
        } else {
            throw log.throwing(new IllegalArgumentException("Unsupported type: " + elementType));
        }
        
        //we retrieve objects corresponding to all the requested entities, 
        //plus their ancestors/descendants depending on the parameters. 
        //We cannot simply use the retrieved relations, as some entities 
        //might have no relations according to the requested parameters. 
        Set<String> requestedEntityIds = new HashSet<>(filteredEntities);
        //Warning: if filteredEntities is empty, then all entities are requested 
        //and we should not restrain the entities using the relations
        if (!requestedEntityIds.isEmpty()) {
            requestedEntityIds.addAll(relations.stream()
                    .flatMap(rel -> Stream.of(rel.getSourceId(), rel.getTargetId()))
                    .collect(Collectors.toSet()));
        }
        
        return log.exit(new Ontology<T>(
                loadEntityFunction.apply(clonedSpeIds, requestedEntityIds), 
                relations, relationTypes));
    }
}
