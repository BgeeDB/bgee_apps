package org.bgee.model.anatdev.multispemapping;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
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
        Set<Integer> validTaxonIds = taxonOnt.getAncestors(taxonOnt.getElement(taxonId))
                .stream().map(t -> t.getId()).collect(Collectors.toSet());
        validTaxonIds.add(taxonId);

        //Now, we retrieve all similarity annotations annotated to the requested taxon,
        //but also to its ancestors and descendants, to then filter them appropriately.
        //
        //First, we retrieve the similarity annotations
        SummarySimilarityAnnotationDAO simAnnotDAO = this.getDaoManager().getSummarySimilarityAnnotationDAO();
        Map<Integer, SummarySimilarityAnnotationTO> idToAnnots = simAnnotDAO
                .getSummarySimilarityAnnotations(taxonId, true, true, true, onlyTrusted, null)
                .stream().collect(Collectors.toMap(a -> a.getId(), a -> a));
        //Then, we retrieve the links from similarity annotations to anatomical entities.
        //Since an anatomical entity can be part of several different annotations,
        //we store the links in a Map, for easier retrieval, where each anat. entity ID in key
        //is associated to a Set as value containing all the annotations it is part of.
        Map<String, Set<SummarySimilarityAnnotationTO>> anatEntityIdToSimAnnots = simAnnotDAO
                .getSimAnnotToAnatEntity(taxonId, true, true, true, onlyTrusted)
                .stream().collect(Collectors.toMap(
                        simToAnat -> simToAnat.getAnatEntityId(),
                        simToAnat -> new HashSet<>(Arrays.asList(Optional.ofNullable(
                                idToAnnots.get(simToAnat.getSummarySimilarityAnnotationId()))
                                .orElseThrow(() -> new IllegalStateException((
                                        "Annotation could not be found for ID: "
                                        + simToAnat.getSummarySimilarityAnnotationId()))))),
                        (v1, v2) -> {v1.addAll(v2); return v1;}));
        //Here, we simply reverse the Map for easier retrieval. Since an annotation can have
        //several associated anat. entity IDs, we store the annotation as key in the Map,
        //associated to a Set as value containing all the anat. entity IDs for this annotation.
        Map<SummarySimilarityAnnotationTO, Set<String>> annotToAnatEntities =
                anatEntityIdToSimAnnots.entrySet().stream()
                .flatMap(e -> e.getValue().stream().map(a ->
                        new AbstractMap.SimpleEntry<SummarySimilarityAnnotationTO, String>(a, e.getKey())))
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> new HashSet<>(Arrays.asList(e.getValue())),
                        (v1, v2) -> {v1.addAll(v2); return v1;}));

        //Now, perform filtering. The aim is to identify when to keep a multiple-entity annotations
        //and discard the corresponding single-entity annotations, or the other way around.
        //Multiple-entity annotations are valid when:
        //* all the entities part of the annotation exist in the requested taxon or in some sub-taxa
        //(meaning, each entity has a corresponding single-entity annotation for the requested taxon,
        //or ancestors or descendants of the requested taxon)
        //* but the entities do not all exist in all the sub-taxa of the requested taxon (meaning, some entity
        //are annotated to different sub-taxa of the requested taxon, including the requested taxon itself).
        Map<Set<String>, Set<SummarySimilarityAnnotationTO>> validAnatEntityIdsToMultipleEntityAnnots =
                new HashMap<>();
        Set<Set<String>> examinedAnatEntityIdsInMultipleEntityAnnots = new HashSet<>();
        Set<SummarySimilarityAnnotationTO> annotsInValidTaxa = new HashSet<>();
        ANNOT: for (Entry<SummarySimilarityAnnotationTO, Set<String>> entry: annotToAnatEntities.entrySet()) {
            SummarySimilarityAnnotationTO annotTO = entry.getKey();
            Set<String> anatEntityIds = entry.getValue();
            //We examine only annotations that are valid for the requested taxon
            if (!validTaxonIds.contains(annotTO.getTaxonId())) {
                continue ANNOT;
            }
            //Store all valid annotations, single- or multiple-entity annots
            annotsInValidTaxa.add(annotTO);
            //If it is not a multiple-entity annotation, no need to examine further here
            if (anatEntityIds.size() == 1) {
                continue ANNOT;
            }
            //Maybe it's a redundant multiple-entity annotations we have already examined
            //no need to redo it
            if (!examinedAnatEntityIdsInMultipleEntityAnnots.add(anatEntityIds)) {
                //If already validated, we add this one to the list of validated multiple-entity annots
                validAnatEntityIdsToMultipleEntityAnnots.computeIfPresent(anatEntityIds,
                        (k, v) -> {v.add(annotTO); return v;});
                continue ANNOT;
            }

            //For multiple-entity annotations, we retrieve for each anat. entity
            //the single-entity annotations using it, valid in the requested taxon
            //(then including annotations mapped to ancestor taxa) or its descendants.
            boolean anatEntityNotInValidTaxon = false;
            ANATENTITY: for (String anatEntityId: anatEntityIds) {
                Set<SummarySimilarityAnnotationTO> singleEntityAnnotsForAnat =
                        //we retrieve the annotations using this anat. entity
                        anatEntityIdToSimAnnots.get(anatEntityId)
                        //and we retrieve all the anat. entities associated to the annotation,
                        //in order to keep only single-entity annotations
                        .stream().filter(a -> annotToAnatEntities.get(a).size() == 1)
                        .collect(Collectors.toSet());
                //If no single-entity annotation for this anat. entity exists at all
                //in this lineage, then we discard the multiple-entity annotation:
                //it would be meaningless to make comparison using an anat. entity
                //that doesn't exist at all in the species part of the requested taxon.
                //An example is the annotation "lung-swim bladder" to the taxon Gnathostomata.
                //If the requested taxon is Sarcopterygii, "lung" exists in this taxon
                //(because annotated to Gnathostomata as well), but not "swim bladder",
                //annotated to Actinopterygii. We then don't want to use "lung-swim bladder"
                //if the requested taxon is Sarcopterygii, but rather, "lung" only.
                if (singleEntityAnnotsForAnat.isEmpty()) {
                    continue ANNOT;
                }
                //Now we check whether the single-entity annotations for this anat. entity
                //are invalid for the requested taxon (meaning, annotated to descendants
                //of the requested taxon; meaning, potentially present in some but not all
                //the species of this taxon, yielding interesting comparison).
                //Since an anat. entity can be redundantly annotated to different taxa
                //=> All the taxa of the single-entity annotations have to be invalid
                //for the anat. entity to be considered as not annotated to the requested taxon
                //or one of its ancestors.
                //And if all the anat. entities of this multiple-entity annotation have valid
                //single-entity annotations for the requested taxon, we will discard
                //the multiple-entity annotation. Indeed, if all the anat. entities part of
                //this multiple-entity annotations potentially exist in all the species
                //of the requested taxon, it will be meaningless to regroup the anat. entities
                //to make comparison, while we could compare the data in each anat. entity individually.
                //An example is the annotation "mouth-anus" annotated to Bilateria.
                //"mouth" is annotated to Eumetazoa and "anus" annotated to Bilateria,
                //which means that both "mouth" and "anus" exist in bilateria.
                //It would be meaningless to regroup data in "mouth-anus" while we can use both
                //independently.
                //At the opposite, in the case of the "lung-swim bladder" example, annotated to
                //Gnathostomata: if the requested taxon is Gnathostomata, some species have
                //a swim bladder (annotated to Actinopterygii), some species have a lung
                //(annotated to Gnathostomata), thus we want to use the annotation "lung-swim bladder"
                //to make comparisons, as these structures are all present in this lineage,
                //but in different species.
                if (singleEntityAnnotsForAnat.stream()
                        //Check that *all* taxa annotated are invalid
                        .allMatch(a -> !validTaxonIds.contains(a.getTaxonId()))) {
                    //OK, the multiple-entity annotation will be validated
                    anatEntityNotInValidTaxon = true;
                    break ANATENTITY;
                }
            }
            if (!anatEntityNotInValidTaxon) {
                continue ANNOT;
            }
            log.debug("Validated multiple-entity annotations: {}", annotTO);
            if (validAnatEntityIdsToMultipleEntityAnnots
                    .put(anatEntityIds, new HashSet<>(Arrays.asList(annotTO))) != null) {
                throw log.throwing(new AssertionError("This group of anat. entities was already "
                        + "validated and should not have been reexamined: " + anatEntityIds));
            }
        }
        //Retrieve all anat. entity IDs part of a validated multiple-entity annotations
        //associated to the annotations
        Map<String, Set<SummarySimilarityAnnotationTO>> idsToValidatedMultEntAnnots =
                validAnatEntityIdsToMultipleEntityAnnots.entrySet().stream()
                .flatMap(e -> e.getKey().stream()
                        .map(id -> new AbstractMap.SimpleEntry<>(id, e.getValue())))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(),
                        (v1, v2) -> {v1.addAll(v2); return v1;}));
        //If several multiple-entity annotations overlap for a same anat. entity,
        //we keep the annotations with less anat. entities
//        Set<SummarySimilarityAnnotationTO> toDiscard = idsToValidatedMultEntAnnots.entrySet().stream()
//                .map(e -> {
//                    List<SummarySimilarityAnnotationTO> annots = new ArrayList<>(e.getValue());
//                    for (int i = 0; i < annots.size(); i++) {
//                        for (int j = i + 1; j < annots.size(); j++) {
//                            SummarySimilarityAnnotationTO annot1 = annots.get(i);
//                            SummarySimilarityAnnotationTO annot2 = annots.get(j);
//                            Set<String> anatEntityIds1 = annotToAnatEntities.get(annot1);
//                            Set<String> anatEntityIds2 = annotToAnatEntities.get(annot2);
//                        }
//                    }
//                }).collect(Collectors.toSet());
        //Filter the annotations to use now.
//        Set<SummarySimilarityAnnotationTO> finalAnnots = annotsInValidTaxa.stream().filter(a -> {
//            Set<String> anatEntityIds = annotToAnatEntities.get(a);
//        })
        //Now, we need to discard multiple-entity annotations that were not validated,
        //and single-entity annotations for the anat. entities part of a validated
        //multiple-entity annotation

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
