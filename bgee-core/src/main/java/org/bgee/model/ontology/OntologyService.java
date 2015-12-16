package org.bgee.model.ontology;

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
     * Retrieve the {@code Ontology} of the anatomical entities for given anat. entities,
     * {@code RelationType}s, and {@code RelationStatus}.
     * 
     * @param anatEntities      A {@code Set} of {@code AnatEntity}s that are anatomical entities 
     *                          of the {@code Ontology}.
     * @param relationTypes     A {@code Set} of {@code RelationType}s that are the relation 
     *                          types allowing to filter the relations between elements
     *                          of the {@code Ontology}.
     * @param service
     * @return                  The {@code Ontology} of the anatomical entities.
     */
    public Ontology<AnatEntity> getAnatEntityOntology(Set<String> anatEntityIds,
            Set<RelationType> relationTypes, boolean getAncestors, boolean getDescendants,
            AnatEntityService service) {
        log.entry(anatEntityIds, service, relationTypes);
        
        if (anatEntityIds == null || anatEntityIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No IDs for anatamical entities"));            
        }
        
        // 
        Stream<RelationTO> relations = getDaoManager().getRelationDAO().
                    getAnatEntityRelations(null, anatEntityIds, relationTypes, 
                            EnumSet.complementOf(EnumSet.of(RelationStatus.REFLEXIVE))).stream();
        
        Set<RelationTO> filteredRelations;
        if (getAncestors && getDescendants) {
            // Get relations between provided entities and their relatives (no filter)
            filteredRelations = relations.collect(Collectors.toSet());
        } else if (getAncestors) {
            // Get relations between provided entities and their ancestors (sourceId in anatEntityIds)
            filteredRelations = relations
                    .filter(e -> anatEntityIds.contains(e.getSourceId()))
                    .collect(Collectors.toSet());
        } else if (getDescendants) {
            // Get relations between provided entities and their descendants (targetId in anatEntityIds)
            filteredRelations = relations
                    .filter(e -> anatEntityIds.contains(e.getTargetId()))
                    .collect(Collectors.toSet());
        } else {
            // Get relations between provided entities (sourceId and targetId in anatEntityIds)
            filteredRelations = relations
                    .filter(e -> anatEntityIds.contains(e.getTargetId()) 
                            && anatEntityIds.contains(e.getSourceId()))
                    .collect(Collectors.toSet());
        }
        
        Set<AnatEntity> anatEntities = 
                service.loadAnatEntitiesByIds(anatEntityIds).collect(Collectors.toSet());
        
        Set<AnatEntity> filteredEntities = anatEntities == null? 
                new HashSet<>(): new HashSet<>(anatEntities);
        return log.exit(new Ontology<AnatEntity>(anatEntities, filteredRelations));
    }
    
    public Ontology<AnatEntity> getAnatEntityOntology(
            Set<String> anatEntityIds, AnatEntityService service) {
        log.entry(anatEntityIds, service);
        return log.exit(this.getAnatEntityOntology(anatEntityIds,
                EnumSet.of(RelationType.ISA_PARTOF), false, false, service));
    }
    
    /**
     * Retrieve the {@code Ontology} of the developmental stages for given dev. stages,
     * and {@code RelationStatus}.
     * 
     * @param devStages         A {@code Set} of {@code DevStage}s that are dev. stages 
     *                          of the {@code Ontology}.
     * @param relationStatus    A {@code Set} of {@code RelationStatus}s that are the relation 
     *                          status allowing to filter the relations between elements
     *                          of the {@code Ontology}.
     * @return                  The {@code Ontology} of the developmental stages.
     */
    public Ontology<DevStage> getStageOntology(Set<String> devStageIds,
            boolean getAncestors, boolean getDescendants, DevStageService service) {
        log.entry(devStageIds, getAncestors, getDescendants, service);
        
        Set<RelationTO> relations = getDaoManager().getRelationDAO().
                    getStageRelations(null, devStageIds, null).stream().collect(Collectors.toSet()); 

        return new Ontology<DevStage>(null, relations);
    }
    
    public Ontology<DevStage> getStageOntology(Set<String> devStageIds, DevStageService service) {
        log.entry(devStageIds, service);
        return this.getStageOntology(devStageIds, false, false, service);
    }
}
