package org.bgee.model.anatdev.multispemapping;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SimAnnotToAnatEntityTO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SummarySimilarityAnnotationTO;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.species.Taxon;

/**
 * A {@code Service} for {@code AnatEntitySimilarity}.
 *
 * @author Frederic Bastian
 * @version Bgee 14 Mar. 2019
 * @since Bgee 14 Mar. 2019
 */
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
     * Load positive anatomical entity similarities valid for the requested {@code taxonId}.
     * 
     * @param taxonId       An {@code Integer} that is the NCBI ID of the taxon for which the similarity 
     *                      annotations should be valid for.
     * @param onlyTrusted   A {@code boolean} defining whether results should be restricted 
     *                      to "trusted" annotations. If {@code true}, only trusted annotations are returned.
     * @return              The {@code Stream} of {@link AnatEntitySimilarity}s.
     */
    public Stream<AnatEntitySimilarity> loadPositiveAnatEntitySimilarities(int taxonId, boolean onlyTrusted) {
        log.entry(taxonId, onlyTrusted);
        if (taxonId <= 0) {
            throw log.throwing(new IllegalArgumentException("Taxon ID must be stricly positive."));
        }

        //We need the taxon ontology for the requested taxon and its ancestors and descendants,
        //in order to correctly filter the similarity annotations to return.
        Ontology<Taxon, Integer> taxonOnt = this.getServiceFactory().getOntologyService()
                .getTaxonOntologyFromTaxonIds(Collections.singleton(taxonId), false, true, true);
        if (taxonOnt.getElement(taxonId) == null) {
            throw log.throwing(new IllegalArgumentException("Taxon ID not found."));
        }

        //Now, we retrieve all similarity annotations annotated to the requested taxon,
        //but also to its ancestors and descendants, to then filter them appropriately
        SummarySimilarityAnnotationDAO simAnnotDAO = this.getDaoManager().getSummarySimilarityAnnotationDAO();
        Map<Integer, SummarySimilarityAnnotationTO> idToAnnots = simAnnotDAO
                .getSummarySimilarityAnnotations(taxonId, true, true, true, onlyTrusted, null)
                .stream().collect(Collectors.toMap(a -> a.getId(), a -> a));
        Map<Integer, Set<String>> idToAnatEntities = simAnnotDAO
                .getSimAnnotToAnatEntity(taxonId, true, true, true, onlyTrusted)
                .stream().collect(Collectors.toMap(
                        simToAnat -> simToAnat.getSummarySimilarityAnnotationId(),
                        simToAnat -> new HashSet<>(Arrays.asList(simToAnat.getAnatEntityId())),
                        (v1, v2) -> {v1.addAll(v2); return v1;}));

        //Now, perform filtering. The aim is to identify when to keep a multiple-entity annotations
        //and discard the corresponding single-entity annotations, or the other way around.
        //Multiple-entity annotations are valid when:
        //* all the entities part of the annotation exist in the requested taxon or in some sub-taxa
        //(meaning, each entity has a corresponding single-entity annotation for the requested taxon,
        //or ancestors or descendants of the requested taxon)
        //* but the entities do not all exist in all the sub-taxa of the requested taxon (meaning, some entity
        //are annotated to different sub-taxa of the requested taxon, including the requested taxon itself).
        Set<SummarySimilarityAnnotationTO> toDiscard = new HashSet<>();

        //Load the taxa we're gonna need in a Map where the key is the taxon ID
//        Set<Integer> allTaxonIds = simAnnotations.values().stream()
//                .map(annot -> annot.getTaxonId())
//                .collect(Collectors.toSet());
//        final Map<Integer, Taxon> taxa = Collections.unmodifiableMap(
//                this.getServiceFactory().getTaxonService().loadTaxaByIds(allTaxonIds)
//                .collect(Collectors.toMap(t -> t.getId(), t -> t)));

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
            SummarySimilarityAnnotationTO annot = idToAnnots.get(e.getKey());
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
            Taxon taxon = taxonOnt.getElement(annot.getTaxonId());
            if (taxon == null) {
                throw log.throwing(new IllegalStateException(
                        "Missing taxon with ID " + annot.getTaxonId()));
            }
            return new AnatEntitySimilarity(taxon, anatEntities);
        }));
    }
}
