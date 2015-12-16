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
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.DevStageService;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationStatus;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationType;

/**
 * A {@link Service} to obtain {@link Ontology} objects.
 * Users should use the {@link ServiceFactory} to obtain {@code OntologyService}s.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Dec. 2015
 * @since   Bgee 13, Dec. 2015
 * @param <T>
 */
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
     * Retrieve the {@code Ontology} of {@code AnatEntity}s for given anatomical entity IDs,
     * relations types, and relation status.
     * <p>
     * Return {@code Ontology} contains ancestors and/or descendants according to
     * {@code getAncestors} and {@code getDescendants}, respectively.
     * 
     * @param anatEntityIds     A {@code Collection} of {@code String}s that are anat.
     *                          entity IDs of the {@code Ontology} to retrieve.
     * @param relationTypes     A {@code Collection} of {@code RelationType}s that are the relation
     *                          types allowing to filter the relations between elements
     *                          of the {@code Ontology}.
     * @param getAncestors      A {@code boolean} defining whether ancestors are retrieved.
     * @param getDescendants    A {@code boolean} defining whether descendants are retrieved.
     * @param service           An {@code AnatEntityService} that provides bgee services.
     * @return                  The {@code Ontology} of the {@code AnatEntity}s for given
     *                          anat. entity IDs, relations types, and relation status.
     * @throw IllegalArgumentException  If {@code anatEntityIds} is {@code null} or {@code empty}.
     */
    public Ontology<AnatEntity> getAnatEntityOntology(Collection<String> anatEntityIds,
            Collection<RelationType> relationTypes, boolean getAncestors, boolean getDescendants,
            AnatEntityService service) {
        log.entry(anatEntityIds, getAncestors, getDescendants, relationTypes, service);
        
        if (anatEntityIds == null || anatEntityIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No anatomical entity IDs"));
        }
        
        Set<RelationStatus> relationStatus = EnumSet.complementOf(EnumSet.of(RelationStatus.REFLEXIVE));
        
        // Currently, we do not manage RelationStatus. We retrieve all non reflexive relations.
        Stream<RelationTO> relations = getDaoManager().getRelationDAO().
                    getAnatEntityRelations(null, new HashSet<>(anatEntityIds),
                            new HashSet<>(relationTypes), relationStatus).stream();
        
        Set<RelationTO> filteredRelations = this.filterRelations(anatEntityIds, relations,
                getAncestors, getDescendants);

        Set<String> filteredAnatEntityIds = this.getElementIds(filteredRelations);
        
        return log.exit(new Ontology<AnatEntity>(
                service.loadAnatEntitiesByIds(filteredAnatEntityIds).collect(Collectors.toSet()),
                filteredRelations, relationTypes, relationStatus));
    }
    
    /**
     * Retrieve the {@code Ontology} of {@code AnatEntity}s for given anatomical entity IDs.
     * <p>
     * Return {@code Ontology} contains only {@code AnatEntity}s for given anat. entities IDs. 
     * Only relations with a {@code RelationType} {@code ISA_PARTOF} are considered. 
     * 
     * @param anatEntityIds     A {@code Collection} of {@code String}s that are anat.
     *                          entity IDs of the {@code Ontology} to retrieve.
     * @param service           An {@code AnatEntityService} that provides bgee services.
     * @return                  The {@code Ontology} of the {@code AnatEntity}s for given
     *                          anat. entity IDs.
     * @throw IllegalArgumentException  If {@code anatEntityIds} is {@code null} or {@code empty}.
     */
    public Ontology<AnatEntity> getAnatEntityOntology(
            Collection<String> anatEntityIds, AnatEntityService service) {
        log.entry(anatEntityIds, service);
        return log.exit(this.getAnatEntityOntology(anatEntityIds,
                EnumSet.of(RelationType.ISA_PARTOF), false, false, service));
    }
    
    /**
     * Retrieve the {@code Ontology} of {@code DevStage}s for given developmental stages IDs.
     * <p>
     * Return {@code Ontology} contains ancestors and/or descendants according to
     * {@code getAncestors} and {@code getDescendants}, respectively.
     * 
     * @param devStageIds       A {@code Collection} of {@code String}s that are dev. stages IDs
     *                          of the {@code Ontology} to retrieve.
     * @param getAncestors      A {@code boolean} defining whether ancestors are retrieved.
     * @param getDescendants    A {@code boolean} defining whether descendants are retrieved.
     * @param service           An {@code DevStageService} that provides bgee services.
     * @return                  The {@code Ontology} of the {@code DevStage}s for given
     *                          dev. stages IDs.
     * @throw IllegalArgumentException  If {@code devStageIds} is {@code null} or {@code empty}.
     */
    public Ontology<DevStage> getDevStageOntology(Collection<String> devStageIds,
            boolean getAncestors, boolean getDescendants, DevStageService service) {
        log.entry(devStageIds, getAncestors, getDescendants, service);
        
        if (devStageIds == null || devStageIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No developmental stages IDs"));
        }
        
        Set<RelationStatus> relationStatus = EnumSet.complementOf(EnumSet.of(RelationStatus.REFLEXIVE));
        
        // Currently, we do not manage RelationStatus. We retrieve all non reflexive relations.
        Stream<RelationTO> relations = getDaoManager().getRelationDAO().
                    getStageRelations(null, new HashSet<>(devStageIds), null).stream();
        
        Set<RelationTO> filteredRelations = this.filterRelations(devStageIds, relations,
                getAncestors, getDescendants);

        Set<String> filteredDevStageIds = this.getElementIds(filteredRelations);
        
        return log.exit(new Ontology<DevStage>(
                service.loadDevStagesByIds(filteredDevStageIds).stream().collect(Collectors.toSet()),
                filteredRelations, null, relationStatus));
    }

    /**
     * Retrieve the {@code Ontology} of {@code DevStage}s for given developmental stages IDs.
     * <p>
     * Return {@code Ontology} contains only {@code DevStage}s for given dev. stages IDs. 
     * 
     * @param devStageIds       A {@code Collection} of {@code String}s that are dev. stages IDs
     *                          of the {@code Ontology} to retrieve.
     * @param service           An {@code DevStageService} that provides bgee services.
     * @return                  The {@code Ontology} of the {@code DevStage}s for given
     *                          dev. stages IDs.
     * @throw IllegalArgumentException  If {@code devStageIds} is {@code null} or {@code empty}.
     */
    public Ontology<DevStage> getDevStageOntology(Collection<String> devStageIds, DevStageService service) {
        log.entry(devStageIds, service);
        return this.getDevStageOntology(devStageIds, false, false, service);
    }


    /**
     * Get element IDs (source and target IDs) defining {@code relations}.
     * 
     * @param relations A {@code Set} of {@code RelationTO}s that are relations for which
     *                  element IDs are retrieved.
     * @return          A {@code Set} of {@code String}s that are element IDs
     *                  defining {@code relations}.
     */
    private Set<String> getElementIds(Set<RelationTO> relations) {
        return relations.stream()
                .flatMap(r -> Arrays.asList(r.getSourceId(), r.getTargetId()).stream())
                .collect(Collectors.toSet());
    }

    /**
     * Filter relations to keep only ancestors and or descendants.
     * 
     * @param ids               A {@code Collection} of {@code String}s that are element IDs
     *                          to be kept.
     * @param relations         A {@code Stream} of {@code RelationTO}s that are relations
     *                           to be filtered.
     * @param getAncestors      A {@code boolean} defining whether ancestors are kept.
     * @param getDescendants    A {@code boolean} defining whether descendants are kept.
     * @return                  A Set of {@code RelationTO}s that are filtered relations.
     */
    private Set<RelationTO> filterRelations(Collection<String> ids, Stream<RelationTO> relations, 
            boolean getAncestors, boolean getDescendants) {
        log.entry(ids, relations, getAncestors, getDescendants);
        
        Set<RelationTO> filteredRelations;
        if (getAncestors && getDescendants) {
            // Get relations between provided entities and their relatives (no filter)
            filteredRelations = relations.collect(Collectors.toSet());
        } else if (getAncestors) {
            // Get relations between provided entities and their ancestors (sourceId in ids)
            filteredRelations = relations
                    .filter(e -> ids.contains(e.getSourceId()))
                    .collect(Collectors.toSet());
        } else if (getDescendants) {
            // Get relations between provided entities and their descendants (targetId in ids)
            filteredRelations = relations
                    .filter(e -> ids.contains(e.getTargetId()))
                    .collect(Collectors.toSet());
        } else {
            // Get relations between provided entities (sourceId and targetId in ids)
            filteredRelations = relations
                    .filter(e -> ids.contains(e.getTargetId())
                            && ids.contains(e.getSourceId()))
                    .collect(Collectors.toSet());
        }
        return log.exit(filteredRelations);
    }
}
