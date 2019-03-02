package org.bgee.model.anatdev.multispemapping;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SimAnnotToAnatEntityTO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SummarySimilarityAnnotationTO;
import org.bgee.model.species.Taxon;

public class AnatEntitySimilarityService extends Service {
    private final static Logger log = LogManager.getLogger(AnatEntitySimilarityService.class.getName());

    /**
     * @param serviceFactory            The {@code ServiceFactory} to be used to obtain {@code Service}s 
     *                                  and {@code DAOManager}.
     * @throws IllegalArgumentException If {@code serviceFactory} is {@code null}.
     */
    public AnatEntitySimilarityService(ServiceFactory serviceFactory) {
        super(serviceFactory);
    }

    /**
     * Load anatomical entity similarities from provided {@code taxonId}, {@code speciesIds},
     * and {@code onlyTrusted}.
     * 
     * @param taxonId       An {@code Integer} that is the NCBI ID of the taxon for which the similarity 
     *                      annotations should be valid, including all its ancestral taxa.
     * @param speciesIds    A {@code Set} of {@code Integer}s that are IDs of the species
     *                      for which the similarity annotations should be restricted.
     *                      If empty or {@code null} all available species are used.
     * @param onlyTrusted   A {@code boolean} defining whether results should be restricted 
     *                      to "trusted" annotations.
     * @return              The {@code Stream} of {@link AnatEntitySimilarity} that are anat. entity 
     *                      similarities from provided {@code taxonId}, {@code speciesIds},
     *                      and {@code onlyTrusted}.
     */
    public Stream<AnatEntitySimilarity> loadAnatEntitySimilarities(Integer taxonId,
            Set<Integer> speciesIds, boolean onlyTrusted) {
        log.entry(taxonId, speciesIds, onlyTrusted);
        //FIXME: update method
        final Map<Integer, SummarySimilarityAnnotationTO> simAnnotations = Collections.unmodifiableMap(
                this.getDaoManager().getSummarySimilarityAnnotationDAO()
                .getSummarySimilarityAnnotations(taxonId, true, false, true, onlyTrusted, null).stream()
                .filter(to -> taxonId.equals(to.getTaxonId()))
                .collect(Collectors.toMap(SummarySimilarityAnnotationTO::getId, Function.identity())));

        //Load the taxa we're gonna need in a Map where the key is the taxon ID
        Set<Integer> allTaxonIds = simAnnotations.values().stream()
                .map(annot -> annot.getTaxonId())
                .collect(Collectors.toSet());
        final Map<Integer, Taxon> taxa = Collections.unmodifiableMap(
                this.getServiceFactory().getTaxonService().loadTaxaByIds(allTaxonIds)
                .collect(Collectors.toMap(t -> t.getId(), t -> t)));

        //Now we need to get all mappings between anat. entities, to be able to load the anat. entities we need
        Map<Integer, List<SimAnnotToAnatEntityTO>> groupedMappingTOs = 
                this.getDaoManager().getSummarySimilarityAnnotationDAO()
                .getSimAnnotToAnatEntity(taxonId, true, false, true, onlyTrusted).stream()
                .collect(Collectors.groupingBy(SimAnnotToAnatEntityTO::getSummarySimilarityAnnotationId));
        //get the anat. entities in Map where the key is their ID
        Set<String> anatEntityIds = groupedMappingTOs.values().stream()
                .flatMap(l -> l.stream().map(s -> s.getAnatEntityId()))
                .collect(Collectors.toSet());
        final Map<String, AnatEntity> anatEntityMap = Collections.unmodifiableMap(
                this.getServiceFactory().getAnatEntityService()
                .loadAnatEntities(null, true, anatEntityIds, false)
                .collect(Collectors.toMap(a -> a.getId(), a -> a)));

        //Now we produce the AnatEntitySimilarities
        return log.exit(groupedMappingTOs.entrySet().stream().map(e -> {
            SummarySimilarityAnnotationTO annot = simAnnotations.get(e.getKey());
            if (annot == null) {
                throw log.throwing(new IllegalStateException(
                        "Missing annotation with ID " + e.getKey()));
            }
            Set<AnatEntity> anatEntities = e.getValue().stream()
                    .map(mapping -> {
                        AnatEntity anatEntity = anatEntityMap.get(mapping.getAnatEntityId());
                        if (anatEntity == null) {
                            throw log.throwing(new IllegalStateException(
                                    "Missing anat. entity with ID " + mapping.getAnatEntityId()));
                        }
                        return anatEntity;
                    })
                    .collect(Collectors.toSet());
            Taxon taxon = taxa.get(annot.getTaxonId());
            if (taxon == null) {
                throw log.throwing(new IllegalStateException(
                        "Missing taxon with ID " + annot.getTaxonId()));
            }
            return new AnatEntitySimilarity(taxon, anatEntities);
        }));
    }
}
