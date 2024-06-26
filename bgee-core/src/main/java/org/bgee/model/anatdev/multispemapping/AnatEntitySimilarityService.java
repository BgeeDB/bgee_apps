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
import org.bgee.model.species.Species;
import org.bgee.model.species.Taxon;

/**
 * A {@code Service} for {@code AnatEntitySimilarity}.
 *
 * @author Frederic Bastian
 * @version Bgee 15, Dec. 2021
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
     * See {@link #loadPositiveAnatEntitySimilarities(int, boolean, Collection)} for details.
     *
     * @param taxonId                   An {@code Integer} that is the NCBI ID of the taxon for which
     *                                  the similarity annotations should be valid for.
     * @param onlyTrusted               A {@code boolean} defining whether results should be restricted
     *                                  to "trusted" annotations. If {@code true}, only trusted annotations
     *                                  are returned.
     * @return                          The {@code Stream} of {@link AnatEntitySimilarity}s.
     * @see #loadPositiveAnatEntitySimilarities(int, boolean, Collection)
     */
    public Set<AnatEntitySimilarity> loadPositiveAnatEntitySimilarities(int taxonId, boolean onlyTrusted) {
        log.traceEntry("{}, {}", taxonId, onlyTrusted);
        return log.traceExit(this.loadPositiveAnatEntitySimilarities(taxonId, onlyTrusted, null));
    }
    /**
     * Load positive anatomical entity similarities valid for the requested {@code taxonId}.
     * Some species IDs can be provided for discarding annotation for which none
     * of the related anatomical entities exist in any of the requested species.
     * If this method is called to retrieve all similarity annotations valid for the requested taxon,
     * the {@code Collection} of species IDs should be {@code null} or empty.
     * <p>
     * The most basic purpose of this method is to retrieve similarity annotations annotated to
     * the requested taxon or one of its ancestors. There are however two subtilities, which are:
     * i) when to retrieve a multiple-entity annotation (for instance, "lung-swim bladder")
     * and when to retrieve the corresponding single-entity annotations ("lung" on one hand,
     * "swim bladder" on the other); and ii) adding to the similarity annotations anatomical entities
     * related by "transformation_of" relationships.
     * <p>
     * <strong>1) Multiple-entity/single-entity annotations</strong>: the point is to determine
     * when to keep a multiple-entity annotations and discard the corresponding single-entity
     * annotations, or the other way around. Criteria for using a multiple-entity annotation:
     * <ul>
     * <li>all the entities part of the annotation exist in the lineage of the requested taxon
     * (meaning, each entity has a corresponding single-entity annotation for the requested taxon,
     * or ancestors or descendants of the requested taxon).
     * <li>but the entities do not all exist in all the species of the requested taxon (meaning,
     * at least some entities are annotated to sub-taxa of the requested taxon).
     * </ul>
     * An example is the annotation "lung-swim bladder" to the taxon <i>Gnathostomata</i>
     * (with corresponding single-entity annotations being "lung" annotated to taxon <i>Gnathostomata</i>,
     * and "swim bladder" annotated to taxon <i>Actinopterygii</i>).
     * If the requested taxon is <i>Gnathostomata</i>, both "lung" and "swim bladder"
     * exists in this lineage (criteria 1 satisfied), and "swim bladder" is annotated to
     * the sub-taxon <i>Actinopterygii</i> (criteria 2 satisfied): some species in the requested taxon
     * possess a lung but no swim bladder, some possess a swim bladder. So we indeed want to use
     * the multiple-entity annotation "lung-swim bladder" to make comparisons accross
     * all the requested taxon.
     * <p>
     * If the requested taxon is <i>Sarcopterygii</i>, "lung" exists in this lineage
     * (because annotated to <i>Gnathostomata</i> as well), but not "swim bladder",
     * annotated to <i>Actinopterygii</i> (criteria 1 not satisfied). We then don't want to use
     * "lung-swim bladder" if the requested taxon is <i>Sarcopterygii</i>, but, rather, "lung" only,
     * since there is no swim bladder in this lineage.
     * <p>
     * If the requested taxon is <i>Actinopterygii</i>, both the single-entity annotations "lung"
     * and "swim bladder" satisfy criteria 1 (because "lung" is annotated to <i>Gnathostomata</i>),
     * but the criteria 2 is not satisfied, since no anatomical entities of the multiple-entity annotation
     * are annotated to a sub-taxon of <i>Actinopterygii</i>. It means that all <i>Actinopterygii</i>
     * could possess a swim bladder and/or a lung, so we don't want to use "lung-swim bladder"
     * to make comparisons, since we can have more granularity by using "swim bladder" on one hand
     * and "lung" on the other.
     * <p>
     * Another example for explaining why the criteria 2 is needed is the annotation "mouth-anus"
     * to taxon <i>Eumetazoa</i> (with corresponding single-entity annotations being "mouth"
     * to taxon <i>Eumetazoa</i> and "anus" to taxon <i>Bilateria</i>). If the requested taxon is
     * <i>Bilateria</i>, the criterion 1 is satisfied, but not criterion 2: no anatomical entitiy
     * is annotated to a sub-taxon of the requested taxon. It means that all <i>Bilateria</i>
     * could possess both a mouth and an anus, so, again, we don't want to use the multiple-entity
     * annotation "mouth-anus", while we can have a better level of details by comparing data
     * across species in this taxon in "mouth" on the one hand, "anus" on the other hand.
     * <p>
     * We still have another question to address, but it's about validation of
     * single-entity annotations: in the "lung-swim bladder" example, if the requested taxon
     * is <i>Actinopterygii</i>, the multiple-entity annotation is correctly discarded,
     * but the single-entity annotation "lung" is also still considered valid.
     * Is it really an issue? It is unclear whether some <i>Actinopterygii</i>
     * can have both a lung and a swim bladder; Wikipedia says it does not happen, other sources say
     * it does. Anyway, there's not much we can do about it:
     * <ul>
     * <li>using the taxon constraints in Uberon would not solve the problem, "lung" has a relationship
     * "only_in_taxon NCBITaxon:7776 ! <i>Gnathostomata</i>", which is correct. Could we add a relationship
     * "never_in_taxon NCBITaxon:7898 ! <i>Actinopterygii</i>"? We're not sure about that,
     * and moreover taxon constraints in Bgee are stored only for the species integrated, so we could give
     * an incorrect answer to the question "Can an <i>Actinopterygii</i> have a lung?" simply because of
     * our species sampling. So, users have the possibility to filter similarity annotations
     * for species IDs provided, up to them to decide depending on their needs.
     * <li>when discarding a multiple-entity annotation, we could maybe also discard
     * the related single-entity annotations annotated to an ancestor of the requested taxon?
     * In our case, that would allow to discard "lung" and to keep "swim bladder" only.
     * But, that would also discard "mouth" in the "mouth-anus" example => we don't want that.
     * Maybe discard the single-entity annotations if they are both annotated to an ancestor
     * of the requested taxon, AND annotated to the same taxon as the related discarded
     * multiple-entity annotation? Again, wouldn't work with the "mouth-anus" annotation...
     * </ul>
     * <p>
     * <strong>2) Adding related anatomical entities through "transformation_of" relations</strong>:
     * similarity annotations are created only for mature anatomical entities (for instance,
     * the term "brain" is annotated, not the term "future brain"). For this reason, we add
     * to the mappings the terms connected by 'transformation_of" relations to annotated
     * anatomical entities (so that we use data in both "brain" and "future brain").
     *
     * @param taxonId                   An {@code Integer} that is the NCBI ID of the taxon for which
     *                                  the similarity annotations should be valid for.
     * @param onlyTrusted               A {@code boolean} defining whether results should be restricted
     *                                  to "trusted" annotations. If {@code true}, only trusted annotations
     *                                  are considered.
     * @param speciesIdsForFiltering    A {@code Collection} of {@code Integer}s representing IDs
     *                                  of species to filter valid {@code AnatEntitySimilarity}s.
     *                                  If {@code null} or {@code empty}, all {@code AnatEntitySimilarity}s
     *                                  valid for {@code taxonId} are returned. Otherwise,
     *                                  we discard {@code AnatEntitySimilarity}s using only
     *                                  anatomical entities existing in none of the requested species.
     * @return                          The {@code Set} of {@link AnatEntitySimilarity}s.
     * @implSpec    Providing species IDs for filtering similarity annotations should not change
     *              the selection of multiple-entity vs. single-entitiy annotations, or the retrieval
     *              of additional entities through transformation_of relations, etc.
     *              It means that the filtering should be done <i>a posteriori</i>.
     *              Indeed, we want the annotations to be always the same for a given requested taxon,
     *              and to not vary based on the requested species; only to be filtered.
     *              For instance, if for the requested taxon the annotation "lung-swim bladder"
     *              should be returned, if you were to discard the single-entity annotation
     *              "lung", because not existing in the requested species, <strong>before</strong>
     *              validating "lung-swim bladder", it would lead to discard it and to return
     *              the single-entity annotation "swim bladder" instead. Or, if the "transformation_of"
     *              relations considered were retrieved only for the requested species,
     *              the entities added to source annotations could vary.
     */
    public Set<AnatEntitySimilarity> loadPositiveAnatEntitySimilarities(int taxonId, boolean onlyTrusted,
            Collection<Integer> speciesIdsForFiltering) {
        log.traceEntry("{}, {}, {}", taxonId, onlyTrusted, speciesIdsForFiltering);
        if (taxonId <= 0) {
            throw log.throwing(new IllegalArgumentException("Taxon ID must be stricly positive."));
        }
        Set<Integer> clonedSpeIds = speciesIdsForFiltering == null? new HashSet<>():
            new HashSet<>(speciesIdsForFiltering);

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
        //between anat. entities (see javadoc of this method).
        //We retrieve the ontology for any species, and not only the requested species,
        //Because we want the valid mappings to be consistent whatever the requested species.
        //They could vary if we were to discard some anat. entities and/or transformation_of relations
        //based on the requested species. (but this anat. ontology is going to be used below
        //to filter the annotations a posteriori).
        //Retrieve the anat. entity IDs for building the ontology
        Set<String> anatEntityIds = validAnnots.values().stream().flatMap(s -> s.stream())
                .collect(Collectors.toSet());
        log.debug("Anat entity IDs considered for loading anatomical ontology: {}", anatEntityIds);
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

        //Create the final AnatEntitySimilarity objects
        Stream<AnatEntitySimilarity> similarities = validAnnots.entrySet().stream()
                //We group the annotations by their anat. entity IDs to provide all the annotations
                //for the mapping to the AnatEntitySimilarity object
                .collect(Collectors.toMap(
                        e -> e.getValue(),
                        e -> new HashSet<>(Arrays.asList(e.getKey())),
                        (v1, v2) -> {v1.addAll(v2); return v1;}))
                //Now we can create the AnatEntitySimilarity objects.
                .entrySet().stream()
                .<AnatEntitySimilarity>map(e -> mapToAnatEntitySimilarity(e.getKey(), e.getValue(),
                        requestedTaxon, idToCIOStatementTOMap, taxonOnt, anatOnt, anatEntityIds));
        //This we were we filter the annotations a posteriori based on the requested species
        if (!clonedSpeIds.isEmpty()) {
            similarities = similarities.filter(aes -> aes.getAllAnatEntities().stream()
                    //Keep annotations that have at least one anat. entity existing
                    //in any of the requested species
                    .anyMatch(ae -> {
                        Set<Integer> validSpeIds = anatOnt.getSpeciesIdsWithElementValidIn(ae);
                        return validSpeIds == null ||
                                !Collections.disjoint(clonedSpeIds, validSpeIds);
                    }));
        }

        return log.traceExit(similarities.collect(Collectors.toSet()));
    }

    /**
     * Retrieve similarities for the requested anatomical entities and the requested species.
     * For instance, if you are studying data for "swim bladder" in zebrafish, and need to know what is
     * the homologous organ in human. You would call this method by providing the ID of the "swim bladder" tissue
     * and the IDs of zebrafish and human species. As a result you would retrieve the {@code AnatEntitySimilarity}
     * for "swim bladder - lung".
     *
     * @param speciesIds    A {@code Collection} of {@code Integer}s that are the NCBI ID of the species
     *                      which to retrieve similarities for. The similarities valid in the lineage
     *                      of their least common ancestor will be considered.
     * @param anatEntityIds A {@code Collection} of {@code String}s that are the IDs of the anatomical entities
     *                      which the similarity information need to be retrieved for. If {@code null} or empty,
     *                      all valid similarities are returned.
     * @param onlyTrusted   A {@code boolean} defining whether results should be restricted
     *                      to "trusted" annotations. If {@code true}, only trusted annotations are considered.
     * @return              The {@code Set} of {@link AnatEntitySimilarity}s for the requested anatomical entities
     *                      and valid for in the lineage of the requested species.
     */
    public Set<AnatEntitySimilarity> loadSimilarAnatEntities(Collection<Integer> speciesIds,
            Collection<String> anatEntityIds, boolean onlyTrusted) {
        log.traceEntry("{}, {}, {}", speciesIds, anatEntityIds, onlyTrusted);

        Set<Integer> clonedSpeIds = speciesIds == null? new HashSet<>(): new HashSet<>(speciesIds);
        Set<String> clonedAnatEntityIds = anatEntityIds == null? new HashSet<>(): new HashSet<>(anatEntityIds);

        //First, we find the common ancestor of the requested species
        Taxon lca = this.getServiceFactory().getTaxonService().loadLeastCommonAncestor(clonedSpeIds);
        //Now we query the anat. entity similarities for this common ancestor
        //and existing in at least one of the requested species
        Set<AnatEntitySimilarity> anatEntitySimilarities = this.loadPositiveAnatEntitySimilarities(
                lca.getId(), onlyTrusted, clonedSpeIds);
        //And now we filter the similarities to return only those related to the requested anat. entities
        if (clonedAnatEntityIds.isEmpty()) {
            return log.traceExit(anatEntitySimilarities);
        }
        return log.traceExit(anatEntitySimilarities.stream()
                .filter(s -> s.getAllAnatEntities().stream().anyMatch(ae -> clonedAnatEntityIds.contains(ae.getId())))
                .collect(Collectors.toSet()));
    }

    public AnatEntitySimilarityAnalysis loadPositiveAnatEntitySimilarityAnalysis(Collection<Integer> speciesIds,
            Collection<String> anatEntityIds, boolean onlyTrusted) {
        log.traceEntry("{}, {}, {}", speciesIds, anatEntityIds, onlyTrusted);

        Set<Integer> clonedSpeIds = speciesIds == null? new HashSet<>(): new HashSet<>(speciesIds);
        Set<String> clonedAnatEntityIds = anatEntityIds == null? new HashSet<>(): new HashSet<>(anatEntityIds);

        Map<Integer, Species> speciesMap = this.getServiceFactory().getSpeciesService()
                .loadSpeciesMap(null, false);
        Set<Integer> speciesIdsNotFound = clonedSpeIds.stream()
                .filter(id -> !speciesMap.containsKey(id))
                .collect(Collectors.toSet());
        Set<Species> requestedSpecies = clonedSpeIds.stream()
                .filter(id -> speciesMap.containsKey(id))
                .map(id -> speciesMap.get(id))
                .collect(Collectors.toSet());
        Set<Integer> speciesIdsFound = requestedSpecies.stream().map(s -> s.getId()).collect(Collectors.toSet());

        //First, we find the common ancestor of the requested species
        Taxon lca = this.getServiceFactory().getTaxonService().loadLeastCommonAncestor(speciesIdsFound);

        //Now we query the anat. entity similarities for this common ancestor
        //and existing in at least one of the requested species.
        //Keep only the similarities containing one of the requested anat. entity IDs
        Set<AnatEntitySimilarity> anatEntitySimilarities = this.loadPositiveAnatEntitySimilarities(
                lca.getId(), onlyTrusted, speciesIdsFound);
        //Keep only the similarities containing one of the requested anat. entity IDs
        if (!clonedAnatEntityIds.isEmpty()) {
            anatEntitySimilarities = anatEntitySimilarities.stream()
            .filter(s -> s.getAllAnatEntities().stream()
                    .anyMatch(ae -> clonedAnatEntityIds.contains(ae.getId())))
            .collect(Collectors.toSet());
        }

        Set<AnatEntity> anatEntitiesInSimilarities = anatEntitySimilarities.stream()
                .flatMap(aes -> aes.getAllAnatEntities().stream())
                .collect(Collectors.toSet());
        Set<String> anatEntityIdsInSimilarities = anatEntitiesInSimilarities.stream()
                .map(ae -> ae.getId()).collect(Collectors.toSet());
        //Get the IDs of all anat. entity in similarities plus requested anat. entity IDs
        Set<String> allAnatEntityIds = new HashSet<>(anatEntityIdsInSimilarities);
        allAnatEntityIds.addAll(clonedAnatEntityIds);
        //Get the IDs of anat. entities not part of similarities.
        Set<String> anatEntityIdsNotInSimilarities = new HashSet<>(clonedAnatEntityIds);
        anatEntityIdsNotInSimilarities.removeAll(anatEntityIdsInSimilarities);
        //Retrieve the anat. entities not part of similarities
        Set<AnatEntity> anatEntitiesNotInSimilarities = anatEntityIdsNotInSimilarities.isEmpty()?
                new HashSet<>(): this.getServiceFactory().getAnatEntityService()
                .loadAnatEntities(anatEntityIdsNotInSimilarities, false)
                .collect(Collectors.toSet());
        //Identify the requested IDs that were not found in the database
        Set<String> allFoundAnatEntityIds = new HashSet<>(anatEntityIdsInSimilarities);
        allFoundAnatEntityIds.addAll(anatEntitiesNotInSimilarities.stream().map(ae -> ae.getId())
                .collect(Collectors.toSet()));
        Set<String> notFoundAnatEntityIds = new HashSet<>(clonedAnatEntityIds);
        notFoundAnatEntityIds.removeAll(allFoundAnatEntityIds);
        //Retrieve taxon constraint for the retrieved anat. entities.
        //First, we create a Map anat. entity ID -> anat. entity
        Map<String, AnatEntity> idToAnatEntity = Stream.concat(
                anatEntitiesInSimilarities.stream(), anatEntitiesNotInSimilarities.stream())
                .collect(Collectors.toMap(ae -> ae.getId(), ae -> ae));
        Map<AnatEntity, Collection<Species>> anatEntityToSpecies = this.getServiceFactory()
                .getTaxonConstraintService().loadAnatEntityTaxonConstraintBySpeciesIds(null)
                .filter(tc -> idToAnatEntity.containsKey(tc.getEntityId()))
                .collect(Collectors.toMap(
                        tc -> idToAnatEntity.get(tc.getEntityId()),
                        tc -> {
                            if (tc.getSpeciesId() == null) {
                                return new HashSet<>(speciesMap.values());
                            }
                            return new HashSet<>(Arrays.asList(speciesMap.get(tc.getSpeciesId())));
                        },
                        (v1, v2) -> {v1.addAll(v2); return v1;}));

        return log.traceExit(new AnatEntitySimilarityAnalysis(clonedAnatEntityIds, notFoundAnatEntityIds,
                clonedSpeIds, speciesIdsNotFound, requestedSpecies,
                lca, anatEntitySimilarities.isEmpty()? null:
                    anatEntitySimilarities.iterator().next().getTaxonOntology(),
                anatEntitySimilarities, anatEntitiesNotInSimilarities,
                anatEntityToSpecies));
    }

    private Map<SummarySimilarityAnnotationTO, Set<String>> getValidAnnots(int taxonId,
            boolean onlyTrusted, Ontology<Taxon, Integer> taxonOnt) {
        log.traceEntry("{}, {}, {}", taxonId, onlyTrusted, taxonOnt);

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
                .getSummarySimilarityAnnotations(taxonId, true, true, true,
                        onlyTrusted? true: null,
                        null)
                .stream().collect(Collectors.toMap(a -> a.getId(), a -> a));
        //Then, we retrieve the links from similarity annotations to anatomical entities.
        //Since a same annotation can use several anat. entities, we store the links in a Map,
        //for easier retrieval, where each annotation in key is associated to a Set as value
        //containing all the anat. entity IDs it uses.
        Map<SummarySimilarityAnnotationTO, Set<String>> annotToAnatEntityIds = simAnnotDAO
                .getSimAnnotToAnatEntity(taxonId, true, true, true, onlyTrusted? true: null)
                .stream().collect(Collectors.toMap(
                        simToAnat -> Optional.ofNullable(
                                idToAnnots.get(simToAnat.getSummarySimilarityAnnotationId()))
                                //Needs compiler hint on some Java version
                                //(See https://stackoverflow.com/a/40865318/1768736)
                                .<IllegalStateException>orElseThrow(() -> new IllegalStateException((
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
                validTaxonIds, taxonOnt, anatEntityIdToSimAnnots, annotToAnatEntityIds);

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
        //OK, now we can retrieve all the valid annotations to consider for the requested taxon.
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

        return log.traceExit(finalAnnots);
    }

    private Set<SummarySimilarityAnnotationTO> getValidMultipleEntityAnnotations(Set<Integer> validTaxonIds,
            Ontology<Taxon, Integer> taxonOnt,
            Map<String, Set<SummarySimilarityAnnotationTO>> anatEntityIdToSimAnnots,
            Map<SummarySimilarityAnnotationTO, Set<String>> annotToAnatEntityIds) {
        log.traceEntry("{}, {}, {}, {}", validTaxonIds, taxonOnt, anatEntityIdToSimAnnots, annotToAnatEntityIds);

        //The aim here is to identify when to keep a multiple-entity annotations
        //and discard the corresponding single-entity annotations (see javadoc of method
        //'loadPositiveAnatEntitySimilarities' for details)
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

        //Basically, here, we want an anat. entity to be part of only one similarity annotation if possibl.
        //For instance, we have the annotations:
        // UBERON:0000152 pelvic fin | UBERON:0002103 hindlimb - homologous in Vertebrata
        // UBERON:0000152 pelvic fin | UBERON:0000978 leg - homologous in Sarcopterygii.
        // (and additionnaly, UBERON:0000152 pelvic fin - homologous in Gnathostomata)
        //If the two multiple-entity annotations are valid, we want to keep the one annotated
        //to the oldest taxon (Vertebrata in this example).
        //
        //First, we map each anat. entity to the groups of anat. entities it is associated to.
        Map<String, Set<Set<String>>> anatEntityToGroups = validAnatEntityIdsToMultipleEntityAnnots
                .keySet().stream()
                .flatMap(k -> k.stream().map(id -> {
                    Set<Set<String>> groupSet = new HashSet<>();
                    groupSet.add(k);
                    return new AbstractMap.SimpleEntry<>(id, groupSet);
                }))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(),
                        (v1, v2) -> {v1.addAll(v2); return v1;}));
        //Now we identify the groups of anat. entities to discard
        Set<Set<String>> keysToDiscard = anatEntityToGroups.entrySet().stream()
                .flatMap(e -> {
                    Set<Set<String>> toDiscard = new HashSet<>();
                    if (e.getValue().size() == 1) {
                        return toDiscard.stream();
                    }
                    //In case there is not a taxon of a group older than the taxa of another group
                    //we'll also discard based on group size if one group completely contains another one,
                    //this is why we order per Set size here.
                    List<Set<String>> orderedGroup = e.getValue().stream()
                            .sorted(Comparator.comparing(s -> s.size(), Comparator.reverseOrder()))
                            .collect(Collectors.toList());
                    GROUP1: for (int i = 0; i < orderedGroup.size(); i++) {
                        Set<String> anatEntityIds1 = orderedGroup.get(i);
                        if (toDiscard.contains(anatEntityIds1)) {
                            continue GROUP1;
                        }
                        Set<Taxon> taxa1 = validAnatEntityIdsToMultipleEntityAnnots
                                .get(anatEntityIds1).stream()
                                .map(annotTO -> taxonOnt.getElement(annotTO.getTaxonId()))
                                .collect(Collectors.toSet());
                        assert !taxa1.contains(null);
                        GROUP2: for (int j = i + 1; j < orderedGroup.size(); j++) {
                            Set<String> anatEntityIds2 = orderedGroup.get(j);
                            if (toDiscard.contains(anatEntityIds2)) {
                                continue GROUP2;
                            }
                            Set<Taxon> taxa2 = validAnatEntityIdsToMultipleEntityAnnots
                                    .get(anatEntityIds2).stream()
                                    .map(annotTO -> taxonOnt.getElement(annotTO.getTaxonId()))
                                    .collect(Collectors.toSet());
                            assert !taxa2.contains(null);
                            if (taxa1.stream().anyMatch(t1 -> taxa2.stream()
                                    .anyMatch(t2 -> taxonOnt.getAncestors(t2).contains(t1)))) {
                                toDiscard.add(anatEntityIds2);
                                continue GROUP2;
                            }
                            if (taxa2.stream().anyMatch(t2 -> taxa1.stream()
                                    .anyMatch(t1 -> taxonOnt.getAncestors(t1).contains(t2)))) {
                                toDiscard.add(anatEntityIds1);
                                continue GROUP1;
                            }
                            //In case there is not a more precise taxon,
                            //if a multiple-entity annotation have its anat. entities
                            //that are all contained in  the anat. entities of another
                            //multiple-entity annotation, then we discard the annotation
                            //with the lowest number of anat. entities.
                            if (anatEntityIds1.size() != anatEntityIds2.size() &&
                                    anatEntityIds1.containsAll(anatEntityIds2)) {
                                toDiscard.add(anatEntityIds2);
                            }
                        }
                    }
                    assert !toDiscard.equals(e.getValue());
                    return toDiscard.stream();
                })
                .filter(toDiscard -> !toDiscard.isEmpty())
                .collect(Collectors.toSet());

        //Get a final list of multiple-entity annotations to keep
        Set<SummarySimilarityAnnotationTO> validMultEntAnnots = validAnatEntityIdsToMultipleEntityAnnots
                .entrySet().stream()
                .filter(e -> !keysToDiscard.contains(e .getKey()))
                .flatMap(e -> e.getValue().stream())
                .collect(Collectors.toSet());

        return log.traceExit(validMultEntAnnots);
    }

    private static AnatEntitySimilarity mapToAnatEntitySimilarity(Set<String> anatEntityIds,
            Set<SummarySimilarityAnnotationTO> annotTOs, Taxon requestedTaxon,
            Map<String, CIOStatementTO> idToCIOStatementTOMap, Ontology<Taxon, Integer> taxonOnt,
            MultiSpeciesOntology<AnatEntity, String> anatOnt, Set<String> anatEntityIdsUsedInAnnots) {
        log.traceEntry("{}, {}, {}, {}, {}, {}, {}", anatEntityIds, annotTOs, requestedTaxon,
                idToCIOStatementTOMap, taxonOnt, anatOnt, anatEntityIdsUsedInAnnots);

        //Get the AnatEntity objects corresponding to the IDs
        Set<AnatEntity> anatEntities = anatEntityIds.stream()
                .map(id -> Optional.ofNullable(anatOnt.getElement(id))
                        //Needs compiler hint on some Java version
                        //(See https://stackoverflow.com/a/40865318/1768736)
                        .<IllegalStateException>orElseThrow(() -> new IllegalStateException(
                                "Anat entity could not be found in the ontology: " + id)))
                .collect(Collectors.toSet());
        //Transformation_of relationships
        Set<AnatEntity> transformationOfEntities = anatEntities.stream()
                .flatMap(ae -> Stream.concat(anatOnt.getAncestors(ae).stream(),
                                             anatOnt.getDescendants(ae).stream()))
                //Before adding the anatomical entities linked by transformation_of relations,
                //we need to check that they are not used in any valid annotations.
                .filter(transfOfEnt -> !anatEntityIdsUsedInAnnots.contains(transfOfEnt.getId()))
                .collect(Collectors.toSet());
        //AnatEntitySimilarityTaxonSummary
        Set<AnatEntitySimilarityTaxonSummary> summaries = annotTOs.stream()
                .map(a -> mapToAnatEntitySimilarityTaxonSummary(a, idToCIOStatementTOMap, taxonOnt))
                .collect(Collectors.toSet());

        return new AnatEntitySimilarity(anatEntities, transformationOfEntities, requestedTaxon,
                summaries, taxonOnt);
    }
    private static AnatEntitySimilarityTaxonSummary mapToAnatEntitySimilarityTaxonSummary(
            SummarySimilarityAnnotationTO annotTO, Map<String, CIOStatementTO> idToCIOStatementTOMap,
            Ontology<Taxon, Integer> taxonOnt) {
        log.traceEntry("{}, {}, {}", annotTO, idToCIOStatementTOMap, taxonOnt);

        Taxon taxon = Optional.ofNullable(taxonOnt.getElement(annotTO.getTaxonId()))
                .orElseThrow(() -> new IllegalStateException(
                        "Taxon was not found in the taxonomy: " + annotTO.getTaxonId()));
        boolean trusted = Optional.ofNullable(idToCIOStatementTOMap.get(annotTO.getCIOId()))
                .orElseThrow(() -> new IllegalStateException(
                        "CIO statement was not found: " + annotTO.getCIOId()))
                .isTrusted();

        return log.traceExit(new AnatEntitySimilarityTaxonSummary(taxon, trusted, !annotTO.isNegated()));
    }
}