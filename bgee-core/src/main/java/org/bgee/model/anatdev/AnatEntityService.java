package org.bgee.model.anatdev;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;

/**
 * A {@link Service} to obtain {@link AnatEntity} objects. 
 * Users should use the {@link org.bgee.model.ServiceFactory} to obtain {@code AnatEntityService}s.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @author  Philippe Moret
 * @author  Julien Wollbrett
 * @version Bgee 14, Mar. 2019
 * @since   Bgee 13, Nov. 2015
*/
public class AnatEntityService extends Service {
    private final static Logger log = LogManager.getLogger(AnatEntityService.class.getName());

    /**
     * {@code Enum} used to define the attributes to populate in the {@code AnatEntity}s
     * obtained from this {@code AnatEntityService}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link AnatEntity#getId()}.
     * <li>{@code NAME}: corresponds to {@link AnatEntity#getName()}.
     * <li>{@code DESCRIPTION}: corresponds to {@link AnatEntity#getDescription()}.
     * </ul>
     */
    public static enum Attribute implements Service.Attribute {
        ID, NAME, DESCRIPTION;
    }

    /**
     * @param serviceFactory            The {@code ServiceFactory} to be used to obtain {@code Service}s 
     *                                  and {@code DAOManager}.
     * @throws IllegalArgumentException If {@code serviceFactory} is {@code null}.
     */
    public AnatEntityService(ServiceFactory serviceFactory) {
        super(serviceFactory);
    }

    /**
     * Retrieve {@code AnatEntity}s for the requested IDs.
     *
     * @param anatEntityIds     A {@code Collection} of {@code String}s that are the IDs of the anatomical entities 
     *                          to retrieve.
     * @param withDescription   A {@code boolean} defining whether the description of the {@code AnatEntity}s
     *                          should be retrieved (higher memory usage).
     * @return                  A {@code Stream} of {@code AnatEntity}s retrieved for the requested IDs.
     */
    public Stream<AnatEntity> loadAnatEntities(Collection<String> anatEntityIds, boolean withDescription) {
        log.entry(anatEntityIds, withDescription);
        return log.traceExit(this.loadAnatEntities(null, true, anatEntityIds, withDescription));
    }
    /**
     * Retrieve {@code AnatEntity}s for the requested species IDs, with all descriptions loaded.
     * If several species IDs are provided, the {@code AnatEntity}s existing in any of them are retrieved. 
     *      
     * @param speciesIds    A {@code Collection} of {@code Integer}s that are IDs of species 
     *                      for which to return the {@code AnatEntity}s.
     * @return              A {@code Stream} of {@code AnatEntity}s retrieved for the requested 
     *                      species IDs, with all descriptions loaded.
     */
    public Stream<AnatEntity> loadAnatEntitiesBySpeciesIds(Collection<Integer> speciesIds) {
        log.entry(speciesIds);
        return log.traceExit(this.loadAnatEntities(speciesIds, true, null, true));
    }
    /**
     * Retrieve {@code AnatEntity}s for the requested species IDs.
     * If several species IDs are provided, the {@code AnatEntity}s existing in any of them are retrieved. 
     *      
     * @param speciesIds        A {@code Collection} of {@code Integer}s that are IDs of species 
     *                          for which to return the {@code AnatEntity}s.
     * @param withDescription   A {@code boolean} defining whether the description of the {@code AnatEntity}s
     *                          should be retrieved (higher memory usage).
     * @return                  A {@code Stream} of {@code AnatEntity}s retrieved for the requested 
     *                          species IDs.
     */
    public Stream<AnatEntity> loadAnatEntitiesBySpeciesIds(Collection<Integer> speciesIds,
            boolean withDescription) {
        log.entry(speciesIds);
        return log.traceExit(this.loadAnatEntities(speciesIds, true, null, withDescription));
    }
    
    /**
     * Retrieve {@code AnatEntity}s for the requested species filtering and anatomical entity IDs.
     * If an entity in {@code anatEntityIds} does not exists according to the species filtering,
     * it will not be returned.
     *
     * @param speciesIds        A {@code Collection} of {@code Integer}s that are the IDs of species
     *                          to filter anatomical entities to retrieve. Can be {@code null} or empty.
     * @param anySpecies        A {@code Boolean} defining, when {@code speciesIds} contains several IDs,
     *                          whether the entities retrieved should be valid in any
     *                          of the requested species (if {@code true}), or in all
     *                          of the requested species (if {@code false} or {@code null}).
     * @param anatEntityIds   A {@code Collection} of {@code String}s that are IDs of anatomical
     *                          entities to retrieve. Can be {@code null} or empty.
     * @param withDescription   A {@code boolean} defining whether the description of the {@code AnatEntity}s
     *                          should be retrieved (higher memory usage).
     * @return                  A {@code Stream} of {@code AnatEntity}s retrieved for the requested parameters.
     */
    //TODO: unit test with/without description
    public Stream<AnatEntity> loadAnatEntities(Collection<Integer> speciesIds,
            Boolean anySpecies, Collection<String> anatEntityIds, boolean withDescription) {
        log.entry(speciesIds, anySpecies, anatEntityIds, withDescription);
        return log.traceExit(this.loadAnatEntities(speciesIds, anySpecies, anatEntityIds,
                withDescription? null: EnumSet.complementOf(EnumSet.of(Attribute.DESCRIPTION))));
    }

    /**
     * Retrieve {@code AnatEntity}s for the requested species filtering and anatomical entity IDs. 
     * If an entity in {@code anatEntityIds} does not exists according to the species filtering, 
     * it will not be returned.
     * 
     * @param speciesIds        A {@code Collection} of {@code Integer}s that are the IDs of species 
     *                          to filter anatomical entities to retrieve. Can be {@code null} or empty.
     * @param anySpecies        A {@code Boolean} defining, when {@code speciesIds} contains several IDs, 
     *                          whether the entities retrieved should be valid in any 
     *                          of the requested species (if {@code true}), or in all 
     *                          of the requested species (if {@code false} or {@code null}).
     * @param anatEntityIds   A {@code Collection} of {@code String}s that are IDs of anatomical
     *                          entities to retrieve. Can be {@code null} or empty.
     * @param attrs             A {@code Collection} of {@code Attribute}s defining the
     *                          attributes to populate in the returned {@code AnatEntity}s.
     * @return                  A {@code Stream} of {@code AnatEntity}s retrieved for the requested parameters.
     */
    //TODO: unit test with/without description
    public Stream<AnatEntity> loadAnatEntities(Collection<Integer> speciesIds,
            Boolean anySpecies, Collection<String> anatEntityIds, Collection<Attribute> attrs) {
        log.entry(speciesIds, anySpecies, anatEntityIds, attrs);
        return log.traceExit(this.getDaoManager().getAnatEntityDAO().getAnatEntities(
                    speciesIds, 
                    anySpecies, 
                    anatEntityIds,
                    attrs == null? null: convertAttrsToDAOAttrs(attrs))
                .stream()
                .map(AnatEntityService::mapFromTO));
    }
    
    /**
     * Retrieves non-informative anatomical entities for the requested species. They
     * correspond to anatomical entities belonging to non-informative subsets in Uberon,
     * and with no observed data from Bgee (no basic calls of any type in them).
     * 
     * @param speciesIds    A {@code Collection} of {@code Integer}s that are the IDs of species 
     *                      allowing to filter the non-informative anatomical entities to use
     * @return              A {@code Stream} of {@code AnatEntity}s retrieved for
     *                      the requested species IDs.
     */
    public Stream<AnatEntity> loadNonInformativeAnatEntitiesBySpeciesIds(Collection<Integer> speciesIds) {
        log.entry(speciesIds);
        
        return log.traceExit(this.getDaoManager().getAnatEntityDAO().getNonInformativeAnatEntitiesBySpeciesIds(
                    speciesIds).stream()
                .map(AnatEntityService::mapFromTO));
    }

    //TODO: replace all use of this method by use of AnatEntityOntology loaded
    //with correct relation types and status
    @Deprecated
    public Map<String, Set<String>> loadDirectIsAPartOfRelationships(Collection<Integer> speciesIds) {
        log.entry(speciesIds);
        return log.traceExit(this.getDaoManager().getRelationDAO().getAnatEntityRelationsBySpeciesIds(
                    speciesIds == null? new HashSet<>(): new HashSet<>(speciesIds), 
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
        return log.traceExit(new AnatEntity(anatEntityTO.getId(), anatEntityTO.getName(), 
                anatEntityTO.getDescription()));
    }
    /**
     * Maps a {@code relationTO} to an {@code Entry} where the key is the ID of the parent 
     * anatomical entity, and the value the ID of the child. 
     * 
     * @param relationTO The {@code RelationTO} to be mapped
     * @return the mapped {@code Entry}
     */
    private static Entry<String, String> mapFromTO(RelationDAO.RelationTO<String> relationTO) {
        log.entry(relationTO);
        return log.traceExit(new AbstractMap.SimpleEntry<>(relationTO.getTargetId(), relationTO.getSourceId()));
    }

    private static Set<AnatEntityDAO.Attribute> convertAttrsToDAOAttrs(Collection<Attribute> attrs) {
        log.entry(attrs);
        if (attrs == null || attrs.isEmpty()) {
            return log.traceExit(EnumSet.allOf(AnatEntityDAO.Attribute.class));
        }
        return log.traceExit(attrs.stream()
                .map(a -> {
                    switch (a) {
                        case ID:
                            return AnatEntityDAO.Attribute.ID;
                        case NAME:
                            return AnatEntityDAO.Attribute.NAME;
                        case DESCRIPTION:
                            return AnatEntityDAO.Attribute.DESCRIPTION;
                        default:
                            throw log.throwing(new UnsupportedOperationException(
                                "Anatomical entity parameter not supported: " + a));
                    }
                }).collect(Collectors.toSet()));
    }
}