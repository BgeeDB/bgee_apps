package org.bgee.model.anatdev;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;

/**
 * A {@link Service} to obtain {@link AnatEntity} objects. 
 * Users should use the {@link org.bgee.model.ServiceFactory} to obtain {@code AnatEntityService}s.
 * 
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 13, Nov. 2015
 * @since   Bgee 13, Nov. 2015
*/
public class AnatEntityService extends Service {
    private final static Logger log = LogManager.getLogger(AnatEntityService.class.getName());

    /**
     * 0-arg constructor that will cause this {@code AnatEntityService} to use 
     * the default {@code DAOManager} returned by {@link DAOManager#getDAOManager()}. 
     * 
     * @see #CallService(DAOManager)
     */
    public AnatEntityService() {
        this(DAOManager.getDAOManager());
    }
    /**
     * @param daoManager    The {@code DAOManager} to be used by this {@code AnatEntityService} 
     *                      to obtain {@code DAO}s.
     * @throws IllegalArgumentException If {@code daoManager} is {@code null}.
     */
    public AnatEntityService(DAOManager daoManager) {
        super(daoManager);
    }
    
    /**
     * Retrieve {@code AnatEntity}s for a given species IDs.
     *      
     * @param speciesIds    A {@code Collection} of {@code String}s that are IDs of species 
     *                      for which to return the {@code AnatEntity}s.
     * @return              A {@code Stream} over the {@code AnatEntity}s that are the
     *                      anatomical entities for the given species IDs.
     */
    public Stream<AnatEntity> loadAnatEntitiesBySpeciesIds(Collection<String> speciesIds) {
        log.entry(speciesIds);
        return log.exit(this.getDaoManager().getAnatEntityDAO().getAnatEntitiesBySpeciesIds(
                    Optional.ofNullable(speciesIds).map(e -> new HashSet<>(e)).orElse(new HashSet<>()))
                ).stream()
                .map(AnatEntityService::mapFromTO);
    }
    
    /**
     * Retrieve {@code AnatEntity}s for a given anatomical entities IDs.
     * 
     * @param anatEntitiesIds   A {@code Collection} of {@code String}s that are IDs of anatomical
     *                          entities for which to return the {@code AnatEntity}s.
     * @return                  A {@code Stream} over the {@code AnatEntity}s that are the
     *                          anatomical entities for the given anatomical entities IDs.
     */
    public Stream<AnatEntity> loadAnatEntitiesByIds(Collection<String> anatEntitiesIds) {
        log.entry(anatEntitiesIds);
        return log.exit(this.getDaoManager().getAnatEntityDAO().getAnatEntitiesByIds(
                    Optional.ofNullable(anatEntitiesIds)
                    .map(e -> new HashSet<>(e)).orElse(new HashSet<>()))
                ).stream()
                .map(AnatEntityService::mapFromTO);
    }

    //FIXME: we should have a proper 'Relation' class
    //TODO: at least, unit test
    public Map<String, Set<String>> loadDirectIsAPartOfRelationships(Collection<String> speciesIds) {
        log.entry(speciesIds);
        return log.exit(this.getDaoManager().getRelationDAO().getAnatEntityRelationsBySpeciesIds(
                    Optional.ofNullable(speciesIds).map(e -> new HashSet<>(e)).orElse(new HashSet<>()), 
                    EnumSet.of(RelationTO.RelationType.ISA_PARTOF), 
                    EnumSet.of(RelationTO.RelationStatus.DIRECT))
                ).stream()
                .map(AnatEntityService::mapFromTO)
                .collect(Collectors.toMap(e -> e.getKey(), 
                        e -> new HashSet<String>(Arrays.asList(e.getValue())), 
                        (v1, v2) -> {
                            Set<String> newSet = new HashSet<>(v1);
                            newSet.addAll(v2);
                            return newSet;
                        }));
    }

    /**
     * Maps a {@code AnatEntityTO} to an {@code AnatEntity} instance 
     * (Can be passed as a {@code Function}). 
     * 
     * @param anatEntityTO The {@code anatEntityTO} to be mapped
     * @return the mapped {@code AnatEntity}
     */
    private static AnatEntity mapFromTO(AnatEntityDAO.AnatEntityTO anatEntityTO) {
        log.entry(anatEntityTO);
        return log.exit(new AnatEntity(anatEntityTO.getId(), anatEntityTO.getName(), 
                anatEntityTO.getDescription()));
    }
    /**
     * Maps a {@code relationTO} to an {@code Entry} where the key is the ID of the parent 
     * anatomical entity, and the value the ID of the child. 
     * 
     * @param relationTO The {@code RelationTO} to be mapped
     * @return the mapped {@code Entry}
     */
    private static Entry<String, String> mapFromTO(RelationDAO.RelationTO relationTO) {
        log.entry(relationTO);
        return log.exit(new AbstractMap.SimpleEntry<>(relationTO.getTargetId(), relationTO.getSourceId()));
    }
}
