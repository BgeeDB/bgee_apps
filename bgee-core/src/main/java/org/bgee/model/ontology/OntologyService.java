package org.bgee.model.ontology;

import java.util.EnumSet;
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
     * @see #GeneService(DAOManager)
     */
    public OntologyService() {
        this(DAOManager.getDAOManager());
    }

    /**
     * @param daoManager                The {@code DAOManager} to be used by this 
     *                                  {@code OntologyService} to obtain {@code DAO}s.
     * @throws IllegalArgumentException If {@code daoManager} is {@code null}.
     */
    public OntologyService(DAOManager daoManager) {
        super(daoManager);
    }
    
    // TODO remove AnatEntityService.loadDirectIsAPartOfRelationships()
    public Ontology<AnatEntity> getAnatEntityOntology(Set<AnatEntity> anatEntities) {
        log.entry(anatEntities);
        
        Set<AnatEntity> filteredEntities = anatEntities == null? new HashSet<>(): new HashSet<>(anatEntities);
        Set<String> ids = filteredEntities.stream().map(AnatEntity::getId).collect(Collectors.toSet());
        
        Set<RelationTO> relations = getDaoManager().getRelationDAO().
                    getAnatEntityRelations(null, ids, EnumSet.of(RelationType.ISA_PARTOF), null).stream()
                    .collect(Collectors.toSet()); 

        return new Ontology<AnatEntity>(anatEntities, relations);
    }

    public Ontology<DevStage> getStageOntology(Set<DevStage> devStages) {
        log.entry(devStages);
        
        Set<DevStage> filteredEntities = devStages == null? new HashSet<>(): new HashSet<>(devStages);
        Set<String> ids = filteredEntities.stream().map(DevStage::getId).collect(Collectors.toSet());
        
        Set<RelationTO> relations = getDaoManager().getRelationDAO().
                    getStageRelations(null, ids, null).stream().collect(Collectors.toSet()); 

        return new Ontology<DevStage>(devStages, relations);
    }
}
