package org.bgee.model.ontology;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.CommonService;
import org.bgee.model.NamedEntity;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.TaxonConstraint;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationStatus;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTOResultSet;
import org.bgee.model.function.QuadriFunction;
import org.bgee.model.species.Taxon;

/**
 * A {@link Service} to obtain {@link Ontology} and {@link MultiSpeciesOntology} objects.
 * Users should use the {@link ServiceFactory} to obtain {@code OntologyService}s.
 * 
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 13, Nov. 2016
 * @since   Bgee 13, Dec. 2015
 */
//TODO: unit tests for all getTaxonOntology... methods
public class OntologyService extends CommonService {

    private static final Logger log = LogManager.getLogger(OntologyService.class.getName());
    
    /**
     * The only purpose of this class is to provide an implementation of equals/hashCode
     * for {@code RelationTO}. {@code TransferObject}s do not implement these method
     * to return whatever results from the data source.
     * 
     * @author Frederic Bastian
     * @version Bgee 14 Mar. 2017
     * @version Bgee 14 Mar. 2017
     *
     * @param <T>   the type of ID of the related entities
     */
    protected static class WrapperRelationTO<T> extends RelationTO<T> {
        private static final long serialVersionUID = 4746776041672427443L;
        
        public WrapperRelationTO(RelationTO<T> relTO) {
            super(relTO.getId(), relTO.getSourceId(), relTO.getTargetId(), 
                    relTO.getRelationType(), relTO.getRelationStatus());
        }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.getId() == null) ? 0 : 
                this.getId().hashCode());
            result = prime * result + ((this.getRelationStatus() == null) ? 0 : 
                this.getRelationStatus().hashCode());
            result = prime * result + ((this.getRelationType() == null) ? 0 : 
                this.getRelationType().hashCode());
            result = prime * result + ((this.getSourceId() == null) ? 0 : 
                this.getSourceId().hashCode());
            result = prime * result + ((this.getTargetId() == null) ? 0 : 
                this.getTargetId().hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            RelationTO<?> other = (RelationTO<?>) obj;
            if (getId() == null) {
                if (other.getId() != null) {
                    return false;
                }
            } else if (!getId().equals(other.getId())) {
                return false;
            }
            if (getRelationStatus() != other.getRelationStatus()) {
                return false;
            }
            if (getRelationType() != other.getRelationType()) {
                return false;
            }
            if (getSourceId() == null) {
                if (other.getSourceId() != null) {
                    return false;
                }
            } else if (!getSourceId().equals(other.getSourceId())) {
                return false;
            }
            if (getTargetId() == null) {
                if (other.getTargetId() != null) {
                    return false;
                }
            } else if (!getTargetId().equals(other.getTargetId())) {
                return false;
            }
            return true;
        }
    }

    /**
     * Constructs a {@code OntologyService}.
     * 
     * @param serviceFactory            The {@code ServiceFactory} to be used to obtain {@code Service}s 
     *                                  and {@code DAOManager}.
     * @throws IllegalArgumentException If {@code serviceFactory} is {@code null}.
     */
    public OntologyService(ServiceFactory serviceFactory) {
        super(serviceFactory);
    }
        
    /**
     * Retrieve the {@code Ontology} of {@code AnatEntity}s for the requested species. 
     * <p>
     * The returned {@code Ontology} contains only the selected anat. entities, and only the
     * relations between them with a {@code RelationType} {@code ISA_PARTOF} are included.
     * 
     * @param speciesId         An {@code Integer} that is the ID of species 
     *                          which to retrieve anat. entities for. Can be {@code null}.
     * @param anatEntityIds     A {@code Collection} of {@code String}s that are IDs of anat.
     *                          entity IDs to retrieve. Can be {@code null} or empty.
     * @return                  The {@code Ontology} of {@code AnatEntity}s for the requested species, 
     *                          anat. entity, relations types, and relation status.
     */
    public Ontology<AnatEntity, String> getAnatEntityOntology(Integer speciesId, Collection<String> anatEntityIds) {
        log.entry(speciesId, anatEntityIds);
        return log.exit(this.getAnatEntityOntology(speciesId, anatEntityIds,
                EnumSet.of(RelationType.ISA_PARTOF), false, false));
    }

    /**
     * Retrieve the {@code Ontology} of {@code AnatEntity}s for the requested species, anatomical entities,
     * relations types, and relation status. 
     * <p>
     * The returned {@code Ontology} contains ancestors and/or descendants of the selected anat. entities 
     * according to {@code getAncestors} and {@code getDescendants}, respectively. 
     * If both {@code getAncestors} and {@code getDescendants} are {@code false}, 
     * then only relations between the selected anat. entities are retrieved.
     * 
     * @param speciesId         An {@code Integer} that is the ID of species 
     *                          which to retrieve anat. entities for. Can be {@code null}.
     * @param anatEntityIds     A {@code Collection} of {@code String}s that are IDs of anat.
     *                          entity IDs to retrieve. Can be {@code null} or empty.
     * @param relationTypes     A {@code Collection} of {@code RelationType}s that are the relation
     *                          types allowing to filter the relations between elements
     *                          of the {@code Ontology}.
     * @param getAncestors      A {@code boolean} defining whether the ancestors of the selected 
     *                          anat. entities, and the relations leading to them, should be retrieved.
     * @param getDescendants    A {@code boolean} defining whether the descendants of the selected 
     *                          anat. entities, and the relations leading to them, should be retrieved.
     * @return                  The {@code Ontology} of {@code AnatEntity}s for the requested species, 
     *                          anat. entity, relations types, and relation status.
     */
    public Ontology<AnatEntity, String> getAnatEntityOntology(Integer speciesId, Collection<String> anatEntityIds, 
            Collection<RelationType> relationTypes, boolean getAncestors, boolean getDescendants) {
        log.entry(speciesId, anatEntityIds, getAncestors, getDescendants, relationTypes);
        
        return log.exit(this.getAnatEntityOntology(Arrays.asList(speciesId), anatEntityIds, 
                relationTypes, getAncestors, getDescendants)
                .getAsSingleSpeciesOntology(speciesId));
    }

    /**
     * Retrieve the {@code MultiSpeciesOntology} of {@code AnatEntity}s for the requested species 
     * and anatomical entities. 
     * <p>
     * The returned {@code MultiSpeciesOntology} contains only the selected anat. entities, 
     * and only the relations between them with a {@code RelationType} {@code ISA_PARTOF} are included.
     * 
     * @param speciesIds        A {@code Collection} of {@code Integer}s that are IDs of species 
     *                          which to retrieve anat. entities for. If several IDs are provided, 
     *                          anat. entities existing in any of them will be retrieved. 
     *                          Can be {@code null} or empty.
     * @param anatEntityIds     A {@code Collection} of {@code String}s that are IDs of anat.
     *                          entity IDs to retrieve. Can be {@code null} or empty.
     * @return                  The {@code MultiSpeciesOntology} of {@code AnatEntity}s for the requested species 
     *                          and anat. entity.
     */
    public MultiSpeciesOntology<AnatEntity, String> getAnatEntityOntology(Collection<Integer> speciesIds, 
            Collection<String> anatEntityIds) {
        log.entry(speciesIds, anatEntityIds);
        return log.exit(this.getAnatEntityOntology(speciesIds, anatEntityIds,
                EnumSet.of(RelationType.ISA_PARTOF), false, false));
    }

    /**
     * Retrieve the {@code MultiSpeciesOntology} of {@code AnatEntity}s for the requested species, 
     * anatomical entities, relations types, and relation status. 
     * <p>
     * The returned {@code MultiSpeciesOntology} contains ancestors and/or descendants of the selected anat. entities 
     * according to {@code getAncestors} and {@code getDescendants}, respectively. 
     * If both {@code getAncestors} and {@code getDescendants} are {@code false}, 
     * then only relations between the selected anat. entities are retrieved.
     * 
     * @param speciesIds        A {@code Collection} of {@code Integer}s that are IDs of species 
     *                          which to retrieve anat. entities for. If several IDs are provided, 
     *                          anat. entities existing in any of them will be retrieved. 
     *                          Can be {@code null} or empty.
     * @param anatEntityIds     A {@code Collection} of {@code String}s that are IDs of anat.
     *                          entity IDs to retrieve. Can be {@code null} or empty.
     * @param relationTypes     A {@code Collection} of {@code RelationType}s that are the relation
     *                          types allowing to filter the relations between elements
     *                          of the {@code MultiSpeciesOntology}.
     * @param getAncestors      A {@code boolean} defining whether the ancestors of the selected 
     *                          anat. entities, and the relations leading to them, should be retrieved.
     * @param getDescendants    A {@code boolean} defining whether the descendants of the selected 
     *                          anat. entities, and the relations leading to them, should be retrieved.
     * @return                  The {@code MultiSpeciesOntology} of {@code AnatEntity}s for the requested species, 
     *                          anat. entity, relations types, and relation status.
     */
    public MultiSpeciesOntology<AnatEntity, String> getAnatEntityOntology(Collection<Integer> speciesIds, 
            Collection<String> anatEntityIds, Collection<RelationType> relationTypes, 
            boolean getAncestors, boolean getDescendants) {
        log.entry(speciesIds, anatEntityIds, getAncestors, getDescendants, relationTypes);

        long startTimeInMs = System.currentTimeMillis();
        log.debug("Start creation of AnatEntityOntology");

        Set<RelationTO<String>> rels = this.getAnatEntityRelationTOs(speciesIds, anatEntityIds,
                relationTypes, getAncestors, getDescendants);
        long startTimeInMs2 = System.currentTimeMillis();
        Set<Integer> relIds = rels.stream().map(rel -> rel.getId()).collect(Collectors.toSet());
        //Here, we don't want to expose the internal relation IDs as part of the Bgee API, so rather than
        //using the TaxonConstraintService, we directly use the TaxonConstraintDAO
        //(we provide the relation IDs to retrieve only a subset of the constraints, for improved performances)
        Set<TaxonConstraint<Integer>> relationTaxonConstraints = getDaoManager().getTaxonConstraintDAO()
                .getAnatEntityRelationTaxonConstraints(speciesIds, relIds, null).stream()
                .map(CommonService::mapTaxonConstraintTOToTaxonConstraint)
                .collect(Collectors.toSet());
        //Previous (slow) version of the code:
        //---
//        Set<TaxonConstraint<Integer>> relationTaxonConstraints = getServiceFactory().getTaxonConstraintService()
//                    .loadAnatEntityRelationTaxonConstraintBySpeciesIds(speciesIds)
//                    .collect(Collectors.toSet());
        //---
        //We use a Map notably in order to filter the taxon constraints for only the retrieved anat, entities
        //XXX: should we allow the method loadAnatEntityTaxonConstraintBySpeciesIds to accept requested
        //anat. entity IDs as arguments?
        Map<String, AnatEntity> requestedAnatEntities = this.getServiceFactory().getAnatEntityService()
                .loadAnatEntities(speciesIds, true,
                        this.getRequestedEntityIds(anatEntityIds, rels), true)
                .collect(Collectors.toMap(ae -> ae.getId(), ae -> ae));
        log.debug("Requested anat. entity IDs: {}", requestedAnatEntities.keySet());
        MultiSpeciesOntology<AnatEntity, String> ont = new MultiSpeciesOntology<AnatEntity, String>(speciesIds,
                requestedAnatEntities.values(), rels,
                this.getServiceFactory().getTaxonConstraintService()
                        .loadAnatEntityTaxonConstraintBySpeciesIds(speciesIds)
                        .filter(tc -> requestedAnatEntities.containsKey(tc.getEntityId()))
                        .collect(Collectors.toSet()),
                relationTaxonConstraints, relationTypes,
                this.getServiceFactory(), AnatEntity.class);

        log.debug("AnatEntityOntology created in {} ms", System.currentTimeMillis() - startTimeInMs);
        return log.exit(ont);
    }
    
    /**
     * Retrieve the {@code Ontology} of {@code DevStage}s for the requested species and 
     * developmental stages IDs.
     * <p>
     * The returned {@code Ontology} contains only {@code DevStage}s corresponding to 
     * the provided dev. stages IDs, and only the relations between them 
     * with a {@code RelationType} {@code ISA_PARTOF} are included. 
     * 
     * @param speciesId         An {@code Integer} that is the ID of species which to retrieve 
     *                          dev. stages for. Can be {@code null}.
     * @param devStageIds       A {@code Collection} of {@code String}s that are dev. stages IDs
     *                          of the {@code Ontology} to retrieve. Can be {@code null} or empty.
     * @return                  The {@code Ontology} of the {@code DevStage}s for the requested species 
     *                          and dev. stages.
     */
    public Ontology<DevStage, String> getDevStageOntology(Integer speciesId, Collection<String> devStageIds) {
        log.entry(speciesId, devStageIds);
        return this.getDevStageOntology(Arrays.asList(speciesId), devStageIds, false, false)
                .getAsSingleSpeciesOntology(speciesId);
    }

    /**
     * Retrieve the {@code Ontology} of {@code DevStage}s for the requested species, dev. stage IDs,
     * and relation status. 
     * <p>
     * The returned {@code Ontology} contains ancestors and/or descendants according to
     * {@code getAncestors} and {@code getDescendants}, respectively. 
     * If both {@code getAncestors} and {@code getDescendants} are {@code false}, 
     * then only relations between provided developmental stages are considered.
     * 
     * @param speciesId         An {@code Integer} that is the ID of species which to retrieve 
     *                          dev. stages for. Can be {@code null}.
     * @param devStageIds       A {@code Collection} of {@code String}s that are dev. stages IDs
     *                          of the {@code Ontology} to retrieve. Can be {@code null} or empty.
     * @param getAncestors      A {@code boolean} defining whether the ancestors of the selected 
     *                          dev. stages, and the relations leading to them, should be retrieved.
     * @param getDescendants    A {@code boolean} defining whether the descendants of the selected 
     *                          dev. stages, and the relations leading to them, should be retrieved.
     * @return                  The {@code Ontology} of the {@code DevStage}s for the requested species, 
     *                          dev. stages, and relation status. 
     */
    public Ontology<DevStage, String> getDevStageOntology(Integer speciesId, Collection<String> devStageIds, 
            boolean getAncestors, boolean getDescendants) {
        log.entry(speciesId, devStageIds, getAncestors, getDescendants);
        return log.exit(getDevStageOntology(Arrays.asList(speciesId), devStageIds, getAncestors, 
                getDescendants).getAsSingleSpeciesOntology(speciesId));
    }

    /**
     * Retrieve the {@code MultiSpeciesOntology} of {@code DevStage}s for the requested species and 
     * developmental stages IDs.
     * <p>
     * The returned {@code MultiSpeciesOntology} contains only {@code DevStage}s corresponding to 
     * the provided dev. stages IDs, and only the relations between them 
     * with a {@code RelationType} {@code ISA_PARTOF} are included. 
     * 
     * @param speciesIds        A {@code Collection} of {@code Integer}s that are IDs of species 
     *                          which to retrieve dev. stages for. If several IDs are provided, 
     *                          dev. stages existing in any of them will be retrieved. 
     *                          Can be {@code null} or empty.
     * @param devStageIds       A {@code Collection} of {@code String}s that are dev. stages IDs
     *                          of the {@code MultiSpeciesOntology} to retrieve. Can be {@code null} or empty.
     * @return                  The {@code MultiSpeciesOntology} of the {@code DevStage}s for the requested species 
     *                          and dev. stages.
     */
    public MultiSpeciesOntology<DevStage, String> getDevStageOntology(Collection<Integer> speciesIds, 
            Collection<String> devStageIds) {
        log.entry(speciesIds, devStageIds);
        return this.getDevStageOntology(speciesIds, devStageIds, false, false);
    }

    /**
     * Retrieve the {@code MultiSpeciesOntology} of {@code DevStage}s for the requested species, dev. stage IDs,
     * and relation status. 
     * <p>
     * The returned {@code MultiSpeciesOntology} contains ancestors and/or descendants according to
     * {@code getAncestors} and {@code getDescendants}, respectively. 
     * If both {@code getAncestors} and {@code getDescendants} are {@code false}, 
     * then only relations between provided developmental stages are considered.
     * 
     * @param speciesIds        A {@code Collection} of {@code Integer}s that are IDs of species 
     *                          which to retrieve dev. stages for. If several IDs are provided, 
     *                          dev. stages existing in any of them will be retrieved. 
     *                          Can be {@code null} or empty.
     * @param devStageIds       A {@code Collection} of {@code String}s that are dev. stages IDs
     *                          of the {@code MultiSpeciesOntology} to retrieve. Can be {@code null} or empty.
     * @param getAncestors      A {@code boolean} defining whether the ancestors of the selected 
     *                          dev. stages, and the relations leading to them, should be retrieved.
     * @param getDescendants    A {@code boolean} defining whether the descendants of the selected 
     *                          dev. stages, and the relations leading to them, should be retrieved.
     * @return                  The {@code MultiSpeciesOntology} of the {@code DevStage}s for the requested species, 
     *                          dev. stages, and relation status. 
     */
    public MultiSpeciesOntology<DevStage, String> getDevStageOntology(Collection<Integer> speciesIds, 
            Collection<String> devStageIds, boolean getAncestors, boolean getDescendants) {
        log.entry(speciesIds, devStageIds, getAncestors, getDescendants);

        long startTimeInMs = System.currentTimeMillis();
        log.debug("Start creation of DevStageOntology");

        Set<RelationTO<String>> rels = this.getDevStageRelationTOs(speciesIds, devStageIds, 
                getAncestors, getDescendants);
        //We use a Map notably in order to filter the taxon constraints for only the retrieved stages
        //XXX: should we allow the method loadDevStageTaxonConstraintBySpeciesIds to accept requested
        //anat. entity IDs as arguments?
        Map<String, DevStage> requestedDevStages = this.getServiceFactory().getDevStageService()
                .loadDevStages(speciesIds, true, this.getRequestedEntityIds(devStageIds, rels))
                .collect(Collectors.toMap(s -> s.getId(), s -> s));
        //there is no relation IDs for nested set models, so no TaxonConstraints. 
        //Relations simply exist if both the source and target of the relations 
        //exists in the targeted species.
        MultiSpeciesOntology<DevStage, String> ont = new MultiSpeciesOntology<DevStage, String>(speciesIds, 
                requestedDevStages.values(), rels,
                getServiceFactory().getTaxonConstraintService().loadDevStageTaxonConstraintBySpeciesIds(speciesIds)
                        .filter(tc -> requestedDevStages.containsKey(tc.getEntityId()))
                        .collect(Collectors.toSet()),
                new HashSet<>(), EnumSet.of(RelationType.ISA_PARTOF),
                this.getServiceFactory(), DevStage.class);

        log.debug("DevStageOntology created in {} ms", System.currentTimeMillis() - startTimeInMs);
        return log.exit(ont);
    }

    /**
     * Returns the {@code Taxon} {@code Ontology} going from the root of the taxonomy
     * to the leaves genus leading to species included in Bgee.
     *
     * @return  A {@code Taxon} {@code Ontology} corresponding to the entire taxonomy
     *          for all the species included in Bgee.
     */
    public Ontology<Taxon, Integer> getTaxonOntology() {
        log.entry();
        return log.exit(this.getTaxonOntologyFromTaxonIds(null, false, false, false));
    }
    /**
     * Returns the {@code Taxon} {@code Ontology} going from the root of the taxonomy
     * to the leaves genus leading to the species requested in arguments.
     * If {@code lca} is {@code true}, additionally to the genus taxa of the requested species,
     * only the least common ancestors of some of the requested species are returned
     * (and not only <strong>the</strong> least common ancestor of all requested species).
     *
     * @param speciesIds        A {@code Collection} of {@code Integer}s that are IDs of the species 
     *                          to retrieve the taxonomy for. Can be {@code null} or empty to retrieve
     *                          the entire taxonomy for all the species included in Bgee.
     * @param lcaRequestSpecies A {@code boolean} defining whether to only retrieve,
     *                          additionally to the genus taxa of the requested species, taxa that are
     *                          least common ancestors of the requested species. If {@code true},
     *                          only least common ancestors of the requested species are retrieved,
     *                          if {@code false}, all ancestor taxa are retrieved.
     * @param lcaBgeeSpecies    A {@code boolean} defining whether to only retrieve,
     *                          additionally to the genus taxa of the requested species, taxa that are
     *                          least common ancestors of some species in Bgee. Taken into account
     *                          only if {@code lcaRequestSpecies} is {@code false}.
     *                          If {@code lcaRequestSpecies} is {@code false} and this argument
     *                          is {@code true}, only least common ancestors of species in Bgee
     *                          are retrieved.
     * @return                  A {@code Taxon} {@code Ontology} corresponding to the taxonomy
     *                          for the requested species, always including at least the genus taxa
     *                          of the requested species, including only the least common ancestors
     *                          of the requested species if {@code lcaRequestSpecies} is {@code true},
     *                          or only the least common ancestors of some species in Bgee
     *                          if {@code lcaRequestSpecies} is {@code false} and {@code lcaBgeeSpecies}
     *                          is {@code true}, or all the ancestor taxa otherwise.
     */
    public Ontology<Taxon, Integer> getTaxonOntologyLeadingToSpecies(Collection<Integer> speciesIds,
            boolean lcaRequestSpecies, boolean lcaBgeeSpecies) {
        log.entry(speciesIds, lcaRequestSpecies, lcaBgeeSpecies);

        Set<Integer> clonedSpeIds = speciesIds == null? new HashSet<>(): new HashSet<>(speciesIds);

        //First, we need to retrieve the requested species and extract their genus taxon IDs
        List<Integer> genusIds = this.getServiceFactory().getSpeciesService()
                .loadSpeciesByIds(clonedSpeIds, false).stream()
                .map(s -> s.getParentTaxonId())
                .collect(Collectors.toList());

        //Now, we retrieve relations from genus to ancestor taxa, taking into account only LCAs
        //if lcaRequestSpecies or lcaBgeeSpecies is true
        Ontology<Taxon, Integer> sourceOnt = this.getTaxonOntologyFromTaxonIds(genusIds,
                lcaRequestSpecies || lcaBgeeSpecies, true, false);
        if (!lcaRequestSpecies) {
            return log.exit(sourceOnt);
        }

        //Now, if lcaRequestSpecies is true, we identify the taxa that are LCA of
        //some of the requested species
        Set<Taxon> validTaxa = new HashSet<>();
        List<Taxon> genusTaxa = genusIds.stream().map(id -> sourceOnt.getElement(id))
                .collect(Collectors.toList());
        validTaxa.addAll(genusTaxa);
        for (int i = 0; i < genusTaxa.size(); i++) {
            for (int y = i + 1; y < genusTaxa.size(); y++) {
                Set<Taxon> commonAncestors = sourceOnt.getLeastCommonAncestors(
                        Arrays.asList(genusTaxa.get(i), genusTaxa.get(y)), null);
                assert commonAncestors.size() == 1;
                validTaxa.addAll(commonAncestors);
            }
        }
        //create a new ontology subset
        Set<Integer> validTaxIds = validTaxa.stream().map(t -> t.getId()).collect(Collectors.toSet());
        return log.exit(new Ontology<Taxon, Integer>(null, validTaxa,
                sourceOnt.getRelations().stream()
                .filter(r -> validTaxIds.contains(r.getSourceId()) &&
                        validTaxIds.contains(r.getTargetId()))
                .collect(Collectors.toSet()),
                EnumSet.of(RelationType.ISA_PARTOF), this.getServiceFactory(), Taxon.class));
    }
    /**
     * Returns the {@code Taxon} {@code Ontology} for the taxa that is the least common ancestor
     * of the requested species. This method is a helper method delegating to {@link
     * #getTaxonOntologyFromTaxon(Collection, boolean, boolean, boolean)} after having found
     * the least common ancestor of the species by calling {@link org.bgee.model.species.TaxonService
     * #loadLeastCommonAncestor(Collection)}.
     * <p>
     * The returned {@code Ontology} contains ancestors and/or descendants of the least common ancestor
     * according to {@code getAncestors} and {@code getDescendants}, respectively. 
     * If both {@code getAncestors} and {@code getDescendants} are {@code false}, 
     * then only the least common ancestor is considered.
     *
     * @param speciesIds        A {@code Collection} of {@code Integer}s that are the species IDs
     *                          which to retrieve the least common ancestor for, in order
     *                          to populate the returned {@code Ontology}.
     *                          Can be {@code null} or empty to seed the {@code Ontology} with
     *                          the root of the ontology.
     * @param lcaBgeeSpecies    A {@code boolean} defining whether to only retrieve ancestor
     *                          (if {@code getAncestors} is {@code true}) and/or descendant taxa
     *                          (if {@code getDescendants} is {@code true}) that are
     *                          least common ancestors of some species in Bgee. If {@code true},
     *                          only least common ancestors are retrieved, if {@code false},
     *                          all taxa are retrieved. This argument matters only if
     *                          {@code getAncestors} and/or {@code getDescendants} are {@code true}.
     * @param getAncestors      A {@code boolean} defining whether the ancestors of the requested taxa, 
     *                          and the relations leading to them, should be retrieved.
     * @param getDescendants    A {@code boolean} defining whether the descendants of the requested taxa, 
     *                          and the relations leading to them, should be retrieved.
     * @return                  A {@code Taxon} {@code Ontology} corresponding to the taxonomy
     *                          for the requested taxa, including only the least common ancestors
     *                          of species in Bgee if {@code lcaBgeeSpecies} is {@code true}.
     */
    public Ontology<Taxon, Integer> getTaxonOntologyFromSpeciesLCA(Collection<Integer> speciesIds,
            boolean lcaBgeeSpecies, boolean getAncestors, boolean getDescendants) {
        log.entry(speciesIds, lcaBgeeSpecies, getAncestors, getDescendants);
        Taxon lcaTax = this.getServiceFactory().getTaxonService().loadLeastCommonAncestor(speciesIds);
        return log.exit(this.getTaxonOntologyFromTaxonIds(Collections.singleton(lcaTax.getId()),
                lcaBgeeSpecies, getAncestors, getDescendants));
    }
    /**
     * Returns the {@code Taxon} {@code Ontology} for the requested taxa. 
     * <p>
     * The returned {@code Ontology} contains ancestors and/or descendants of the requested taxa
     * according to {@code getAncestors} and {@code getDescendants}, respectively. 
     * If both {@code getAncestors} and {@code getDescendants} are {@code false}, 
     * then only relations between provided taxa are considered.
     *
     * @param taxonIds          A {@code Collection} of {@code Integer}s that are taxon IDs
     *                          to be retrieved in the returned {@code Ontology}.
     *                          Can be {@code null} or empty to retrieve all taxa.
     * @param lcaBgeeSpecies    A {@code boolean} defining whether to only retrieve ancestor
     *                          (if {@code getAncestors} is {@code true}) and/or descendant taxa
     *                          (if {@code getDescendants} is {@code true}) that are
     *                          least common ancestors of some species in Bgee. If {@code true},
     *                          only least common ancestors are retrieved, if {@code false},
     *                          all taxa are retrieved. If some specific taxa are requested
     *                          in {@code taxonIds}, they are always retrieved whatever
     *                          the status of this argument and whether they are LCA.
     *                          So This argument matters only if {@code getAncestors} and/or
     *                          {@code getDescendants} are {@code true}.
     * @param getAncestors      A {@code boolean} defining whether the ancestors of the requested taxa, 
     *                          and the relations leading to them, should be retrieved.
     * @param getDescendants    A {@code boolean} defining whether the descendants of the requested taxa, 
     *                          and the relations leading to them, should be retrieved.
     * @return                   A {@code Taxon} {@code Ontology} corresponding to the taxonomy
     *                          for the requested taxa, including only the least common ancestors
     *                          of species in Bgee if {@code lcaBgeeSpecies} is {@code true}.
     */
    public Ontology<Taxon, Integer> getTaxonOntologyFromTaxonIds(Collection<Integer> taxonIds,
            boolean lcaBgeeSpecies, boolean getAncestors, boolean getDescendants) {
        log.entry(taxonIds, lcaBgeeSpecies, getAncestors, getDescendants);

        Set<Integer> clonedTaxIds = taxonIds == null? new HashSet<>(): new HashSet<>(taxonIds);
        Set<RelationTO<Integer>> rels = this.getTaxonRelationTOs(clonedTaxIds, lcaBgeeSpecies,
                getAncestors, getDescendants);
        return log.exit(new Ontology<Taxon, Integer>(null,
                this.getServiceFactory().getTaxonService()
                .loadTaxa(this.getRequestedEntityIds(clonedTaxIds, rels), false)
                //The method TaxonService.loadTaxa does not have a mechanism to specify
                //"returns me only LCAs unless they are the seed requested taxon IDs",
                //so we request all taxon IDs, including non-lca even if lca is true,
                //and we filer afterwards
                .filter(t -> !lcaBgeeSpecies || t.isLca() || clonedTaxIds.contains(t.getId()))
                .collect(Collectors.toSet()),
            rels, EnumSet.of(RelationType.ISA_PARTOF), this.getServiceFactory(), Taxon.class));
    }

    private Set<RelationTO<String>> getAnatEntityRelationTOs(Collection<Integer> speciesIds,
        Collection<String> entityIds,  Collection<RelationType> relationTypes,
        boolean getAncestors, boolean getDescendants) {
        log.entry(speciesIds, entityIds, relationTypes, getAncestors, getDescendants);
        QuadriFunction<Set<String>, Set<String>, Boolean, Set<RelationStatus>, RelationTOResultSet<String>> fun =
            (s, t, b, r) ->
            getDaoManager().getRelationDAO().getAnatEntityRelations(
                speciesIds, true, s, t, b, 
                relationTypes.stream()
                    .map(OntologyBase::convertRelationType)
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(RelationTO.RelationType.class))), 
                r,
                //We want to retrieve the relation IDs to link to the relation taxon constraints,
                //so we retrieve all attributes
                null);
        return log.exit(getRelationTOs(fun, entityIds, getAncestors, getDescendants));
    }
    private Set<RelationTO<String>> getDevStageRelationTOs(Collection<Integer> speciesIds, 
            Collection<String> entityIds, boolean getAncestors, boolean getDescendants) {
        log.entry(speciesIds, entityIds, getAncestors, getDescendants);
        QuadriFunction<Set<String>, Set<String>, Boolean, Set<RelationStatus>, RelationTOResultSet<String>> fun =
            (s, t, b, r) -> getDaoManager().getRelationDAO().getStageRelations(
                speciesIds, true, s, t, b, r, null);
        return log.exit(getRelationTOs(fun, entityIds, getAncestors, getDescendants));
    }
    private Set<RelationTO<Integer>> getTaxonRelationTOs(Collection<Integer> entityIds,
            final boolean lca, boolean getAncestors, boolean getDescendants) {
        log.entry(entityIds, lca, getAncestors, getDescendants);
        QuadriFunction<Set<Integer>, Set<Integer>, Boolean, Set<RelationStatus>, RelationTOResultSet<Integer>> fun =
            (s, t, b, r) -> getDaoManager().getRelationDAO().getTaxonRelations(s, t, b, r, lca, null);
        return log.exit(getRelationTOs(fun, entityIds, getAncestors, getDescendants));
    }

    /**
     * Convenience method to retrieve {@code RelationTO}s for any {@code OntologyElement} type. 
     * 
     * @param elementType           A {@code Class<T>} that is the type of the elements 
     *                              for which to retrieve {@code RelationTO}s.
     * @param speciesIds            A {@code Collection} of {@code String}s that are IDs of species 
     *                              which to retrieve relations for. If several IDs are provided, 
     *                              relations valid in any of them will be retrieved. 
     *                              Can be {@code null} or empty.
     * @param entityIds             A {@code Collection} of {@code String}s that are IDs of 
     *                              entities to retrieve relations for. Can be {@code null} or empty.
     * @param relationTypes         A {@code Collection} of {@code RelationType}s that are the relation
     *                              types allowing to filter the relations to retrieve.
     * @param getAncestors          A {@code boolean} defining whether the ancestors of the selected 
     *                              entities, and the relations leading to them, should be retrieved.
     * @param getDescendants        A {@code boolean} defining whether the descendants of the selected 
     *                              entities, and the relations leading to them, should be retrieved.
     * @return                      A {@code Set} of {@code RelationTO}s that are relations between 
     *                              requested entities, and potentially also to their ancestors 
     *                              and/or their descendants.
     * @param <T>                   The type of elements for which to retrieve {@code RelationTO}s.
     * @param <U>                   The type of ID of the elements in this ontology or sub-graph.
     */
    private <T extends NamedEntity<U> & OntologyElement<T, U>, U extends Comparable<U>> Set<RelationTO<U>> getRelationTOs(
            QuadriFunction<Set<U>, Set<U>, Boolean, Set<RelationStatus>,
            RelationTOResultSet<U>> relationRetrievalFun, 
            Collection<U> entityIds, boolean getAncestors, boolean getDescendants) {
        log.entry(relationRetrievalFun, entityIds, getAncestors, getDescendants);
        
        final Set<U> filteredEntities = Collections.unmodifiableSet(
                entityIds == null? new HashSet<>(): new HashSet<>(entityIds));
    
        // Currently, we use all non reflexive relations.
        //Warning: we absolutely need to retrieve indirect relations in case getAncestors is true 
        //or getDescendants is true
        Set<RelationStatus> relationStatus = EnumSet.complementOf(EnumSet.of(RelationStatus.REFLEXIVE));
        
        //by default, include all ancestors and descendants of selected entities
        Set<U> sourceIds = filteredEntities;
        Set<U> targetIds = filteredEntities;
        boolean sourceOrTarget = true;
        if (!getAncestors && !getDescendants) {
            //request only relations between selected entities (constraints both sources and targets 
            //of considered relations to be one of the selected entities).
            sourceOrTarget = false;
        } else if (!getAncestors) {
            //to not get ancestors, we don't select relations where selected entities are sources
            sourceIds = null;
        } else if (!getDescendants) {
            //opposite if we don't want the descendants
            targetIds = null;
        }
        
        Set<RelationTO<U>> relations = new HashSet<>();
        log.debug("sourceIds: {}, targetIds: {}, sourceOrTarget: {}, relationStatus: {}",
                sourceIds, targetIds, sourceOrTarget, relationStatus);
        relations.addAll(relationRetrievalFun.apply(sourceIds, targetIds, sourceOrTarget, relationStatus)
                    //need to wrap the RelationTOs to get hashCode/equals
                    .stream().map(relTO -> new WrapperRelationTO<>(relTO))
                    .collect(Collectors.toSet()));
        //if it is requested to infer entities,  
        if (getAncestors || getDescendants) {
            assert sourceOrTarget: "Incorrect source/target condition status: sourceOrTarget should be true";
        
            Set<U> newSourceIds = new HashSet<>();
            Set<U> newTargetIds = new HashSet<>();
            if (getAncestors) {
                //get targets IDs of retrieved relations that become new source IDs
                newSourceIds.addAll(relations.stream().map(r -> r.getTargetId()).collect(Collectors.toSet()));
            }
            if (getDescendants) {
                //get source IDs of retrieved relations that become new target IDs
                newTargetIds.addAll(relations.stream().map(r -> r.getSourceId()).collect(Collectors.toSet()));
            }
            if (getAncestors && getDescendants) {
                // if we infer ancestors and descendants, we need to retrieve all relations of 
                // retrieved ancestors and descendants.
                newTargetIds.addAll(relations.stream().map(r -> r.getTargetId()).collect(Collectors.toSet()));
                newSourceIds.addAll(relations.stream().map(r -> r.getSourceId()).collect(Collectors.toSet()));
            }

            if (!newSourceIds.isEmpty()) newSourceIds.removeAll(sourceIds);
            if (!newTargetIds.isEmpty()) newTargetIds.removeAll(targetIds);

            //Query only if new terms have been discovered
            if (!newSourceIds.isEmpty() || !newTargetIds.isEmpty()) {
                log.debug("new sourceIds: {}, new targetIds: {}, sourceOrTarget: {}, relationStatus: {}",
                        newSourceIds, newTargetIds, sourceOrTarget, relationStatus);
                relations.addAll(relationRetrievalFun.apply(newSourceIds, newTargetIds, 
                                sourceOrTarget, relationStatus)
                        //need to wrap the RelationTOs to get hashCode/equals
                        .stream().map(relTO -> new WrapperRelationTO<>(relTO))
                        .collect(Collectors.toSet()));
            }
        }
        return log.exit(relations);
    }
    /**
     * Convenience method to retrieve IDs of {@code OntologyElement}s to load, 
     * based on requested {@code OntologyElement} IDs and relations 
     * leading to other {@code OntologyElement}s
     * 
     * @param entityIds             A {@code Collection} of {@code String}s that are IDs of 
     *                              requested {@code OntologyElement}s. Can be {@code null} or empty.
     * @param relations             A {@code Collection} of {@code RelationTO}s that are relations 
     *                              between {@code OntologyElement}s.
     * @return                      A {@code Set} of {@code String}s that are IDs of 
     *                              {@code OntologyElement}s to load.
     */
    private <U> Set<U> getRequestedEntityIds(Collection<U> entityIds, Collection<RelationTO<U>> relations) {
        log.entry(entityIds, relations);
        //we retrieve objects corresponding to all the requested entities, 
        //plus their ancestors/descendants depending on the parameters. 
        //We cannot simply use the retrieved relations, as some entities 
        //might have no relations according to the requested parameters
        Set<U> requestedEntityIds = entityIds == null? new HashSet<>(): new HashSet<>(entityIds);
        //Warning: if filteredEntities is empty, then all entities are requested 
        //and we should not restrain the entities using the relations
        if (!requestedEntityIds.isEmpty()) {
            requestedEntityIds.addAll(relations.stream()
                    .flatMap(rel -> Stream.of(rel.getSourceId(), rel.getTargetId()))
                    .collect(Collectors.toSet()));
        }
        return log.exit(requestedEntityIds);
    }
}
