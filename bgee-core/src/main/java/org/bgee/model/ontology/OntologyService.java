package org.bgee.model.ontology;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Entity;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationStatus;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationType;

/**
 * A {@link Service} to obtain {@link Ontology} objects. 
 * Users should use the {@link ServiceFactory} to obtain {@code OntologyService}s.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Dec. 2013
 * @since   Bgee 13, Dec. 2013
 * @param <T>
 */
public class OntologyService<T extends Entity & OntologyElement<T>> extends Service {

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
     * @param relationStatus    A {@code Set} of {@code RelationStatus}s that are the relation 
     *                          status allowing to filter the relations between elements
     *                          of the {@code Ontology}.
     * @return                  The {@code Ontology} of the anatomical entities.
     */
    // TODO call it in AnatEntityService.loadDirectIsAPartOfRelationships()? 
    // or remove completely loadDirectIsAPartOfRelationships()?
    public Ontology<AnatEntity> getAnatEntityOntology(Set<AnatEntity> anatEntities, 
            Set<RelationType> relationTypes, Set<RelationStatus> relationStatus) {
        log.entry(anatEntities, relationTypes, relationStatus);
        
        Set<AnatEntity> filteredEntities = anatEntities == null? 
                new HashSet<>(): new HashSet<>(anatEntities);
        Set<String> ids = filteredEntities.stream()
                .map(AnatEntity::getId)
                .collect(Collectors.toSet());
        
        Set<RelationTO> relations = getDaoManager().getRelationDAO().
                    getAnatEntityRelations(null, ids, relationTypes, relationStatus).stream()
                    .collect(Collectors.toSet()); 

        return new Ontology<AnatEntity>(anatEntities, relations);
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
    public Ontology<DevStage> getStageOntology(
            Set<DevStage> devStages, Set<RelationStatus> relationStatus) {
        log.entry(devStages, relationStatus);
        
        Set<DevStage> filteredStages = devStages == null? new HashSet<>(): new HashSet<>(devStages);
        Set<String> ids = filteredStages.stream().map(DevStage::getId).collect(Collectors.toSet());
        
        Set<RelationTO> relations = getDaoManager().getRelationDAO().
                    getStageRelations(null, ids, relationStatus).stream().collect(Collectors.toSet()); 

        return new Ontology<DevStage>(devStages, relations);
    }
}
