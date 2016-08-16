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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SimAnnotToAnatEntityTO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SummarySimilarityAnnotationTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;

/**
 * A {@link Service} to obtain {@link AnatEntity} objects. 
 * Users should use the {@link org.bgee.model.ServiceFactory} to obtain {@code AnatEntityService}s.
 * 
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @author Philippe Moret
 * @version Bgee 13, Apr. 2016
 * @since   Bgee 13, Nov. 2015
*/
public class AnatEntityService extends Service {
    private final static Logger log = LogManager.getLogger(AnatEntityService.class.getName());

    /**
     * @param serviceFactory            The {@code ServiceFactory} to be used to obtain {@code Service}s 
     *                                  and {@code DAOManager}.
     * @throws IllegalArgumentException If {@code serviceFactory} is {@code null}.
     */
    public AnatEntityService(ServiceFactory serviceFactory) {
        super(serviceFactory);
    }
    
    /**
     * Retrieve {@code AnatEntity}s for the requested species IDs. If several species IDs 
     * are provided, the {@code AnatEntity}s existing in any of them are retrieved. 
     *      
     * @param speciesIds    A {@code Collection} of {@code String}s that are IDs of species 
     *                      for which to return the {@code AnatEntity}s.
     * @return              A {@code Stream} of {@code AnatEntity}s retrieved for the requested 
     *                      species IDs.
     */
    public Stream<AnatEntity> loadAnatEntitiesBySpeciesIds(Collection<String> speciesIds) {
        log.entry(speciesIds);
        return log.exit(this.loadAnatEntities(speciesIds, true, null));
    }
    
    /**
     * Retrieve {@code AnatEntity}s for the requested species filtering and anatomical entity IDs. 
     * If an entity in {@code anatEntitiesIds} does not exists according to the species filtering, 
     * it will not be returned.
     * 
     * @param speciesIds        A {@code Collection} of {@code String}s that are the IDs of species 
     *                          to filter anatomical entities to retrieve. Can be {@code null} or empty.
     * @param anySpecies        A {@code Boolean} defining, when {@code speciesIds} contains several IDs, 
     *                          whether the entities retrieved should be valid in any 
     *                          of the requested species (if {@code true}), or in all 
     *                          of the requested species (if {@code false} or {@code null}).
     * @param anatEntitiesIds   A {@code Collection} of {@code String}s that are IDs of anatomical
     *                          entities to retrieve. Can be {@code null} or empty.
     * @return                  A {@code Stream} of {@code AnatEntity}s retrieved for the requested parameters.
     */
    public Stream<AnatEntity> loadAnatEntities(Collection<String> speciesIds, 
            Boolean anySpecies, Collection<String> anatEntitiesIds) {
        log.entry(speciesIds, anySpecies, anatEntitiesIds);
        
        return log.exit(this.getDaoManager().getAnatEntityDAO().getAnatEntities(
                    speciesIds == null? null: new HashSet<>(speciesIds), 
                    anySpecies, 
                    anatEntitiesIds == null? null: new HashSet<>(anatEntitiesIds), 
                    null)
                .stream()
                .map(AnatEntityService::mapFromTO));
    }

    //FIXME: a similar method should be part of the Ontology or OntologyService classes, 
    //with a better returned value. To be written.
    @Deprecated
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
     * Load anatomical entity similarities from provided {@code taxonId}, {@code speciesIds},
     * and {@code onlyTrusted}.
     * 
     * @param taxonId       A {@code String} that is the NCBI ID of the taxon for which the similarity 
     *                      annotations should be valid, including all its ancestral taxa.
     * @param speciesIds    A {@code Set} of IDs of the species for which the similarity 
     *                      annotations should be restricted.
     *                      If empty or {@code null} all available species are used.
     * @param onlyTrusted   A {@code boolean} defining whether results should be restricted 
     *                      to "trusted" annotations.
     * @return              The {@code Set} of {@link AnatEntitySimilarity} that are anat. entity 
     *                      similarities from provided {@code taxonId}, {@code speciesIds},
     *                      and {@code onlyTrusted}.
     */
    public Set<AnatEntitySimilarity> loadAnatEntitySimilarities(String taxonId,
            Set<String> speciesIds, boolean onlyTrusted) {
        log.entry(taxonId, speciesIds, onlyTrusted);
        
        Map<String, SummarySimilarityAnnotationTO> simAnnotations = 
                this.getDaoManager().getSummarySimilarityAnnotationDAO()
                .getSummarySimilarityAnnotations(taxonId, onlyTrusted).stream()
                .filter(to -> taxonId.equals(to.getTaxonId()))
                .collect(Collectors.toMap(SummarySimilarityAnnotationTO::getId, Function.identity()));
        
        Set<AnatEntitySimilarity> results = this.getDaoManager().getSummarySimilarityAnnotationDAO()
                .getSimAnnotToAnatEntity(taxonId, speciesIds).stream()
                // group by group id
                .collect(Collectors.groupingBy(SimAnnotToAnatEntityTO::getSummarySimilarityAnnotationId)) 
                .entrySet().stream().map(
                        e -> new AnatEntitySimilarity(simAnnotations.get(e.getKey()).getId(), 
                                // collect anatEntities as a set
                                e.getValue().stream()
                                            .map(SimAnnotToAnatEntityTO::getAnatEntityId)
                                            .collect(Collectors.toSet())))
                .collect(Collectors.toSet());
        
        return log.exit(results);
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
