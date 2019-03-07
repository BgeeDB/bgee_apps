package org.bgee.model.anatdev.multispemapping;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
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
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SummarySimilarityAnnotationTO;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTO;
import org.bgee.model.ontology.MultiSpeciesOntology;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.ontology.RelationType;
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
    public Set<AnatEntitySimilarity> loadPositiveAnatEntitySimilarities(int taxonId, boolean onlyTrusted) {
        log.entry(taxonId, onlyTrusted);
        if (taxonId <= 0) {
            throw log.throwing(new IllegalArgumentException("Taxon ID must be stricly positive."));
        }

        //We need the taxon ontology for the requested taxon and its ancestors and descendants,
        //mainly in order to correctly filter the similarity annotations to return.
        //It will also allow us to retrieve the taxa used in annotations for creating
        //the AnatEntitySimilarity objects
        Ontology<Taxon, Integer> taxonOnt = this.getServiceFactory().getOntologyService()
                .getTaxonOntologyFromTaxonIds(Collections.singleton(taxonId), false, true, true);
        Taxon requestedTaxon = taxonOnt.getElement(taxonId);
        if (requestedTaxon == null) {
            throw log.throwing(new IllegalArgumentException("Taxon ID not found: " + taxonId));
        }

        //Retrieve the similarity annotations
        Map<SummarySimilarityAnnotationTO, Set<String>> validAnnots = this.getValidAnnots(taxonId,
                onlyTrusted, taxonOnt);

        //Now we need the anatomical ontology for retrieving 'transformation_of' relations
        //between anat. entities.
        //Indeed, similarity annotations are created only for mature anatomical entities
        //(for instance, the term "brain" is annotated, not the term "future brain").
        //For this reason, we add to the mappings the terms connected by 'transformation_of"
        //relations to annotated anatomical entities (so that we use data in both "brain"
        //and "future brain").
        //We retrieve the ontology for any species, and not only the requested species,
        //Because we want the valid mappings to be consistent whatever the requested species.
        //Retrieve the anat. entity IDs for building the ontology
        //XXX: actually maybe retrieve the relations for all species of the taxon? How reliable is it?
        Set<String> anatEntityIds = validAnnots.values().stream().flatMap(s -> s.stream())
                .collect(Collectors.toSet());
        MultiSpeciesOntology<AnatEntity, String> anatOnt = this.getServiceFactory().getOntologyService()
                .getAnatEntityOntology(
                        (Collection<Integer>) null,
                        anatEntityIds,
                        EnumSet.of(RelationType.TRANSFORMATIONOF),
                        //Retrieve "ancestors" and "descendants" of the anat. entities,
                        //meaning, additional anat. entities connected to the annotated ones
                        //by 'transformation_of' relations.
                        true, true);

        //We also need the CIOStatements to know whether an annotation is trusted or not,
        //we store them in a Map where the key is their ID.
        //TODO: add a proper service for CIOStatements, so that we can store them directly
        //in the AnatEntitySimilarityTaxonSummarys used in AnatEntitySimilarity
        Map<String, CIOStatementTO> idToCIOStatementTOMap = this.getDaoManager().getCIOStatementDAO()
                .getAllCIOStatements().stream()
                .collect(Collectors.toMap(s -> s.getId(), s -> s));

        //Now we can create the AnatEntitySimilarity objects.
        Set<AnatEntitySimilarity> similarities = validAnnots.entrySet()
                //We group the annotations by their anat. entity IDs to provide all the annotations
                //for the mapping to the AnatEntitySimilarity object
                .stream().collect(Collectors.toMap(
                        e -> e.getValue(),
                        e -> new HashSet<>(Arrays.asList(e.getKey())),
                        (v1, v2) -> {v1.addAll(v2); return v1;}))
                //And now we create the AnatEntitySimilarity objects
                .entrySet().stream()
                .<AnatEntitySimilarity>map(e -> mapToAnatEntitySimilarity(e.getKey(), e.getValue(),
                        requestedTaxon, idToCIOStatementTOMap, taxonOnt, anatOnt))
                .collect(Collectors.toSet());

        return log.exit(similarities);
    }

    private Map<SummarySimilarityAnnotationTO, Set<String>> getValidAnnots(int taxonId,
            boolean onlyTrusted, Ontology<Taxon, Integer> taxonOnt) {
        log.entry(taxonId, onlyTrusted, taxonOnt);

        //*******************************************
        // DATA RETRIEVAL
        //*******************************************
        //We need the taxon ontology for the requested taxon and its ancestors and descendants,
        //in order to correctly filter the similarity annotations to return.
        Set<Integer> validTaxonIds = Stream.concat(Stream.of(taxonId),
                taxonOnt.getAncestors(taxonOnt.getElement(taxonId))
                .stream().map(t -> t.getId())).collect(Collectors.toSet());

        //Now, we retrieve all similarity annotations annotated to the requested taxon,
        //but also to its ancestors and descendants, to then filter the valid ones appropriately.
        //
        //First, we retrieve the similarity annotations
        SummarySimilarityAnnotationDAO simAnnotDAO = this.getDaoManager().getSummarySimilarityAnnotationDAO();
        Map<Integer, SummarySimilarityAnnotationTO> idToAnnots = simAnnotDAO
                .getSummarySimilarityAnnotations(taxonId, true, true, true, onlyTrusted, null)
                .stream().collect(Collectors.toMap(a -> a.getId(), a -> a));
        //Then, we retrieve the links from similarity annotations to anatomical entities.
        //Since a same annotation can use several anat. entities, we store the links in a Map,
        //for easier retrieval, where each annotation in key is associated to a Set as value
        //containing all the anat. entity IDs it uses.
        Map<SummarySimilarityAnnotationTO, Set<String>> annotToAnatEntityIds = simAnnotDAO
                .getSimAnnotToAnatEntity(taxonId, true, true, true, onlyTrusted)
                .stream().collect(Collectors.toMap(
                        simToAnat -> Optional.ofNullable(
                                idToAnnots.get(simToAnat.getSummarySimilarityAnnotationId()))
                                .orElseThrow(() -> new IllegalStateException((
                                        "Annotation could not be found for ID: "
                                        + simToAnat.getSummarySimilarityAnnotationId()))),
                        simToAnat -> new HashSet<>(Arrays.asList(simToAnat.getAnatEntityId())),
                        (v1, v2) -> {v1.addAll(v2); return v1;}));

        //Here, we simply reverse the Map for easier retrieval. Since an anat. entity ID can be part of
        //several annotations, we store the anat. entity ID as key in the Map,
        //associated to a Set as value containing all the annotations using them.
        Map<String, Set<SummarySimilarityAnnotationTO>> anatEntityIdToSimAnnots =
                annotToAnatEntityIds.entrySet().stream()
                .flatMap(e -> e.getValue().stream().map(id ->
                        new AbstractMap.SimpleEntry<>(id, e.getKey())))
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> new HashSet<>(Arrays.asList(e.getValue())),
                        (v1, v2) -> {v1.addAll(v2); return v1;}));


        //*******************************************
        // ANNOTATION FILTERING
        //*******************************************
        //--------------------------------------
        // MULTIPLE-ENTITY ANNOTATION FILTERING
        //--------------------------------------
        //Get a final list of multiple-entity annotations to keep
        Set<SummarySimilarityAnnotationTO> validMultEntAnnots = this.getValidMultipleEntityAnnotations(
                validTaxonIds, anatEntityIdToSimAnnots, annotToAnatEntityIds);

        //-------------------------------------
        // SINGLE-ENTITY ANNOTATION FILTERING
        //-------------------------------------
        //Retrieve all single-entity annotations of anat. entities part of a validated
        //multiple-entity annotation, we will discard them
        Set<SummarySimilarityAnnotationTO> singleEntAnnotsToDiscard = validMultEntAnnots.stream()
                //Retrieve the anat. entity IDs part of this multiple-entity annotation
                .flatMap(a -> annotToAnatEntityIds.get(a).stream())
                //Now map these anat. entity IDs to the single-entity annotations to discard.
                .flatMap(id -> anatEntityIdToSimAnnots.get(id).stream()
                        .filter(a -> annotToAnatEntityIds.get(a).size() == 1))
                .collect(Collectors.toSet());

        //-------------
        // FINAL LIST
        //-------------
        //OK, now we can retrieve all the valid annotations to consider for the requested taxon
        Map<SummarySimilarityAnnotationTO, Set<String>> finalAnnots = annotToAnatEntityIds
                .entrySet().stream().filter(e -> {
                    SummarySimilarityAnnotationTO annot = e.getKey();
                    if (!validTaxonIds.contains(annot.getTaxonId())) {
                        return false;
                    }
                    int anatEntityCount = e.getValue().size();
                    if ((anatEntityCount > 1 && !validMultEntAnnots.contains(annot)) ||
                            (anatEntityCount == 1 && singleEntAnnotsToDiscard.contains(annot))) {
                        return false;
                    }
                    return true;
                }).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

        return log.exit(finalAnnots);
    }

    private Set<SummarySimilarityAnnotationTO> getValidMultipleEntityAnnotations(Set<Integer> validTaxonIds,
            Map<String, Set<SummarySimilarityAnnotationTO>> anatEntityIdToSimAnnots,
            Map<SummarySimilarityAnnotationTO, Set<String>> annotToAnatEntityIds) {
        log.entry(validTaxonIds, anatEntityIdToSimAnnots, annotToAnatEntityIds);

        //The aim here is to identify when to keep a multiple-entity annotations
        //and discard the corresponding single-entity annotations, or the other way around.
        //Multiple-entity annotations are valid when:
        //* all the entities part of the annotation exist in the requested taxon or in some sub-taxa
        //(meaning, each entity has a corresponding single-entity annotation for the requested taxon,
        //or ancestors or descendants of the requested taxon)
        //* but the entities do not all exist in all the species of the requested taxon (meaning,
        //at least some entities are annotated to sub-taxa of the requested taxon).
        Map<Set<String>, Set<SummarySimilarityAnnotationTO>> validAnatEntityIdsToMultipleEntityAnnots =
                new HashMap<>();
        Set<Set<String>> examinedAnatEntityIdsInMultipleEntityAnnots = new HashSet<>();
        ANNOT: for (Entry<SummarySimilarityAnnotationTO, Set<String>> entry: annotToAnatEntityIds.entrySet()) {
            SummarySimilarityAnnotationTO annotTO = entry.getKey();
            Set<String> anatEntityIds = entry.getValue();
            //We examine only annotations that are valid for the requested taxon
            if (!validTaxonIds.contains(annotTO.getTaxonId())) {
                continue ANNOT;
            }
            //If it is not a multiple-entity annotation, no need to examine further here
            if (anatEntityIds.size() == 1) {
                continue ANNOT;
            }
            //Maybe it's a redundant multiple-entity annotations we have already examined,
            //no need to redo it
            if (!examinedAnatEntityIdsInMultipleEntityAnnots.add(anatEntityIds)) {
                //If already validated, we add this one to the list of validated multiple-entity annots,
                //if not validated, this one will not be added to the Map
                validAnatEntityIdsToMultipleEntityAnnots.computeIfPresent(anatEntityIds,
                        (k, v) -> {v.add(annotTO); return v;});
                continue ANNOT;
            }

            //For multiple-entity annotations, we retrieve for each anat. entity
            //the single-entity annotations using it, valid in the requested taxon
            //(then including annotations mapped to ancestor taxa) or its descendants.
            //We check that all the anat. entities exist in some species of the requested taxon,
            //and that at least some of the anat. entities do not exist in all species
            //of the requested taxon (meaning that they are annotated to sub-taxa
            //of the requested taxon, meaning that it's interesting to group them with other
            //homologous anat. entities for performing comparisons)
            boolean anatEntityNotInValidTaxon = false;
            for (String anatEntityId: anatEntityIds) {
                Set<SummarySimilarityAnnotationTO> singleEntityAnnotsForAnat =
                        //we retrieve the annotations using this anat. entity
                        anatEntityIdToSimAnnots.get(anatEntityId)
                        //and we retrieve all the anat. entities associated to the annotation,
                        //in order to keep only single-entity annotations
                        .stream().filter(a -> annotToAnatEntityIds.get(a).size() == 1)
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
                    //We won't store this multiple-entity annotation as we move directly
                    //to the next iteration of ANNOT
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
                //which means that both "mouth" and "anus" exist in all bilateria.
                //It would be meaningless to regroup data in "mouth-anus" while we can use both
                //independently.
                //At the opposite, in the "lung-swim bladder" example, annotated to Gnathostomata:
                //if the requested taxon is Gnathostomata, some species have a swim bladder
                //(annotated to Actinopterygii), some species have a lung (annotated to Gnathostomata),
                //thus we want to use the annotation "lung-swim bladder" to make comparisons,
                //as these structures are all present in this lineage, but in different species.
                if (!anatEntityNotInValidTaxon && singleEntityAnnotsForAnat.stream()
                        //Check that *all* taxa annotated are invalid
                        .allMatch(a -> !validTaxonIds.contains(a.getTaxonId()))) {
                    //OK, the multiple-entity annotation will be validated,
                    //unless we don't find for another anat. entity any single-entity annotation
                    //for requested taxon or ancestors or descendants (see 'continue ANNOT' statement
                    //above). This is why we don't break the anatEntityIds loop immediately.
                    anatEntityNotInValidTaxon = true;
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

        //If a multiple-entity annotation have its anat. entities that are all contained
        //in  the anat. entities of another multiple-entity annotation, then we discard the annotation
        //with the highest number of anat. entities.
        //So, first, we order validAnatEntityIdsToMultipleEntityAnnots by the number of anat. entity IDs
        //in each Entry.
        List<Entry<Set<String>, Set<SummarySimilarityAnnotationTO>>> orderedMultEntAnnots =
                validAnatEntityIdsToMultipleEntityAnnots.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().size()))
                .collect(Collectors.toList());
        Set<SummarySimilarityAnnotationTO> multEntAnnotsToDiscard = new HashSet<>();
        for (int i = 0; i < orderedMultEntAnnots.size(); i++) {
            Entry<Set<String>, Set<SummarySimilarityAnnotationTO>> entry1 = orderedMultEntAnnots.get(i);
            Set<SummarySimilarityAnnotationTO> annots1 = entry1.getValue();
            if (multEntAnnotsToDiscard.contains(annots1.iterator().next())) {
                //If this annot was already discarded, any other annotation with more anat. entities
                //and containing all the anat. entities of this annotation would have already
                //been discarded as well.
                continue;
            }
            for (int j = i + 1; j < orderedMultEntAnnots.size(); j++) {
                Entry<Set<String>, Set<SummarySimilarityAnnotationTO>> entry2 = orderedMultEntAnnots.get(j);
                Set<SummarySimilarityAnnotationTO> annots2 = entry2.getValue();
                Set<String> anatEntityIds1 = entry1.getKey();
                Set<String> anatEntityIds2 = entry2.getKey();
                assert anatEntityIds1.size() > 1 && anatEntityIds2.size() > 1 &&
                        anatEntityIds1.size() <= anatEntityIds2.size();
                if (anatEntityIds1.size() != anatEntityIds2.size() && anatEntityIds2.containsAll(anatEntityIds1)) {
                    multEntAnnotsToDiscard.addAll(annots2);
                }
            }
        }

        //Get a final list of multiple-entity annotations to keep
        Set<SummarySimilarityAnnotationTO> validMultEntAnnots = validAnatEntityIdsToMultipleEntityAnnots
                .values().stream()
                .flatMap(s -> s.stream().filter(a -> !multEntAnnotsToDiscard.contains(a)))
                .collect(Collectors.toSet());

        return log.exit(validMultEntAnnots);
    }

    private static AnatEntitySimilarity mapToAnatEntitySimilarity(Set<String> anatEntityIds,
            Set<SummarySimilarityAnnotationTO> annotTOs, Taxon requestedTaxon,
            Map<String, CIOStatementTO> idToCIOStatementTOMap, Ontology<Taxon, Integer> taxonOnt,
            MultiSpeciesOntology<AnatEntity, String> anatOnt) {
        log.entry(anatEntityIds, annotTOs, requestedTaxon, idToCIOStatementTOMap, taxonOnt, anatOnt);

        Set<AnatEntity> anatEntities = anatEntityIds.stream()
                .map(id -> Optional.ofNullable(anatOnt.getElement(id))
                        .orElseThrow(() -> new IllegalStateException(
                                "Anat entity could not be found in the ontology: " + id)))
                .collect(Collectors.toSet());
        Set<AnatEntity> transformationOfEntities = anatEntities.stream()
                .flatMap(ae -> Stream.concat(anatOnt.getAncestors(ae).stream(),
                                             anatOnt.getDescendants(ae).stream()))
                .collect(Collectors.toSet());
        Set<AnatEntitySimilarityTaxonSummary> summaries = annotTOs.stream()
                .map(a -> mapToAnatEntitySimilarityTaxonSummary(a, idToCIOStatementTOMap, taxonOnt))
                .collect(Collectors.toSet());

        return new AnatEntitySimilarity(anatEntities, transformationOfEntities, requestedTaxon,
                summaries);
    }
    private static AnatEntitySimilarityTaxonSummary mapToAnatEntitySimilarityTaxonSummary(
            SummarySimilarityAnnotationTO annotTO, Map<String, CIOStatementTO> idToCIOStatementTOMap,
            Ontology<Taxon, Integer> taxonOnt) {
        log.entry(annotTO, idToCIOStatementTOMap, taxonOnt);

        Taxon taxon = Optional.ofNullable(taxonOnt.getElement(annotTO.getTaxonId()))
                .orElseThrow(() -> new IllegalStateException(
                        "Taxon was not found in the taxonomy: " + annotTO.getTaxonId()));
        boolean trusted = Optional.ofNullable(idToCIOStatementTOMap.get(annotTO.getCIOId()))
                .orElseThrow(() -> new IllegalStateException(
                        "CIO statement was not found: " + annotTO.getCIOId()))
                .isTrusted();

        return log.exit(new AnatEntitySimilarityTaxonSummary(taxon, trusted, !annotTO.isNegated()));
    }
}