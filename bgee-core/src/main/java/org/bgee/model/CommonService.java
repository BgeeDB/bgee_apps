package org.bgee.model;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.TaxonConstraint;
import org.bgee.model.dao.api.anatdev.TaxonConstraintDAO.TaxonConstraintTO;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.ConditionDAO.ConditionTO;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.expressiondata.Condition;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.gene.GeneNotFoundException;
import org.bgee.model.species.Species;
import org.bgee.model.species.SpeciesService;

/**
 * Parent class of several {@code Service}s needing to access common methods. 
 * Since we do not want to expose these methods to API users, we do not build this class 
 * as an "utils" that {@code Service}s could use as a dependency, but as a parent class to inherit from.
 * 
 * @author Valentine Rech de Laval
 * @author Frederic Bastian
 * @version Bgee 14 Nov. 2017
 * @since Bgee 14 Feb. 2017
 *
 */
public class CommonService extends Service {
    private final static Logger log = LogManager.getLogger(CommonService.class.getName());

    /**
     * @param serviceFactory    The {@code ServiceFactory} that instantiated this {@code Service}.
     * @throws IllegalArgumentException If {@code serviceFactory} is {@code null}.
     */
    protected CommonService(ServiceFactory serviceFactory) {
        super(serviceFactory);
    }
    
    //NOTE: there shouldn't be any ConditionService for now
    //This method should rather map selected CallService attributes to ConditionDAO.Attribute
//    protected static Set<ConditionDAO.Attribute> convertConditionServiceAttrsToConditionDAOAttrs(
//        Collection<ConditionService.Attribute> attributes) {
//        log.entry(attributes);
//
//        return log.exit(attributes.stream().map(attr -> {
//            switch (attr) {
//                case ANAT_ENTITY_ID: 
//                    return ConditionDAO.Attribute.ANAT_ENTITY_ID;
//                case DEV_STAGE_ID: 
//                    return ConditionDAO.Attribute.STAGE_ID;
//                case SPECIES_ID:
//                    return ConditionDAO.Attribute.SPECIES_ID;
//                default: 
//                    throw log.throwing(new IllegalStateException(
//                        "Unsupported Attributes from ConditionService: " + attr));
//            }
//        }).collect(Collectors.toCollection(() -> EnumSet.noneOf(ConditionDAO.Attribute.class))));
//    }
    /**
     * Map {@code ConditionTO} to a {@code Condition}.
     * 
     * @param condTO        A {@code ConditionTO} that is the condition from db
     *                      to map into {@code Condition}.
     * @param speciesId     An {@code Integer} that is the ID of the species for which
     *                      the {@code ConditionTO}s were retrieved. Allows to avoid requesting
     *                      this attribute from the {@code ConditionDAO} if only one species was requested.
     * @param anatEntity    The {@code AnatEntity} corresponding to the ID returned by
     *                      {@link ConditionTO#getAnatEntityId()}.
     * @param devStage      The {@code DevStage} corresponding to the ID returned by
     *                      {@link ConditionTO#getStageId()}.
     * @return              The mapped {@code Condition}.
     */
    protected static Condition mapConditionTOToCondition(ConditionTO condTO,
            AnatEntity anatEntity, DevStage devStage, Species species) {
        log.entry(condTO, anatEntity, devStage, species);
        if (condTO == null) {
            return log.exit(null);
        }
        if (species == null) {
            throw log.throwing(new IllegalArgumentException("The Species must be provided."));
        }
        if (condTO.getSpeciesId() != null && !condTO.getSpeciesId().equals(species.getId())) {
            throw log.throwing(new IllegalArgumentException(
                    "Incorrect species ID in ConditionTO, expected " + species.getId() + " but was "
                    + condTO.getSpeciesId()));
        }
        if (condTO.getAnatEntityId() != null && anatEntity != null &&
                !condTO.getAnatEntityId().equals(anatEntity.getId())) {
            throw log.throwing(new IllegalArgumentException(
                    "Incorrect anat. entity ID in ConditionTO, expected " + anatEntity.getId() + " but was "
                    + condTO.getAnatEntityId()));
        }
        if (condTO.getStageId() != null && devStage != null &&
                !condTO.getStageId().equals(devStage.getId())) {
            throw log.throwing(new IllegalArgumentException(
                    "Incorrect dev. stage ID in ConditionTO, expected " + devStage.getId() + " but was "
                    + condTO.getStageId()));
        }
        return log.exit(new Condition(anatEntity, devStage, species));
    }
    protected static ConditionTO mapConditionToConditionTO(int condId, Condition cond) {
        log.entry(condId, cond);
        return log.exit(new ConditionTO(condId, cond.getAnatEntityId(), cond.getDevStageId(),
                cond.getSpeciesId()));
    }
    
    /**
     * Map {@code GeneTO} to a {@code Gene}.
     * 
     * @param geneTO    A {@code GeneTO} that is the condition from data source
     *                  to map into {@code Gene}.
     * @param species   A {@code Species} that is the species of the gene.
     * @return          The mapped {@code Gene}.
     */
    protected static Gene mapGeneTOToGene(GeneTO geneTO, Species species) {
        log.entry(geneTO, species);
        if (geneTO == null) {
            return log.exit(null);
        }
        if (species == null) {
            throw log.throwing(new IllegalArgumentException("A Species must be provided."));
        }
        if (geneTO.getSpeciesId() != null && !geneTO.getSpeciesId().equals(species.getId())) {
            throw log.throwing(new IllegalArgumentException(
                    "Species ID of the gene does not match provided Species."));
        }
        return log.exit(new Gene(geneTO.getGeneId(), geneTO.getName(), geneTO.getDescription(),
                species, geneTO.getGeneMappedToGeneIdCount()));
    }

    /**
     * Map {@link TaxonConstraintTO} to a {@link TaxonConstraint}.
     *
     * @param taxonConstraintTO A {@code TaxonConstraintTO} that is the transfert object to be mapped.
     * @return                  The mapped {@link TaxonConstraint}.
     */
    protected static <T> TaxonConstraint<T> mapTaxonConstraintTOToTaxonConstraint(TaxonConstraintTO<T> taxonConstraintTO) {
        log.entry(taxonConstraintTO);
        if (taxonConstraintTO == null) {
            return log.exit(null);
        }

        return log.exit(new TaxonConstraint<T>(
                taxonConstraintTO.getEntityId(), taxonConstraintTO.getSpeciesId()));
    }

    protected static DataType convertDaoDataTypeToDataType(DAODataType dt) {
        log.entry(dt);
        switch(dt) {
            case AFFYMETRIX:
                return log.exit(DataType.AFFYMETRIX);
            case EST:
                return log.exit(DataType.EST);
            case IN_SITU:
                return log.exit(DataType.IN_SITU);
            case RNA_SEQ:
                return log.exit(DataType.RNA_SEQ);
        default:
            throw log.throwing(new IllegalStateException("Unsupported SourceToSpeciesTO.DataType: " + dt));
        }
    }

    /**
     * Load a {@code Species} {@code Map} from the provided {@code GeneFilter}s, retrieved from the data source.
     *
     * @param geneFilters       A {@code Set} of {@code GeneFilter}s containing the IDs of the {@code Species} to load.
     * @param speciesService    A {@code SpeciesService} to load {@code Species} from their IDs.
     * @return                  An unmodifiable {@code Map} where keys are species IDs, the associated value being
     *                          the corresponding {@code Species}.
     * @throws IllegalArgumentException If a {@code Species} could not be retrieved based on a ID
     *                                  provided in {@code geneFilter}s.
     */
    protected static Map<Integer, Species> loadSpeciesMapFromGeneFilters(Set<GeneFilter> geneFilters,
            SpeciesService speciesService) throws IllegalArgumentException {
        log.entry(geneFilters, speciesService);
     // Retrieve species, get a map species ID -> Species
        final Set<Integer> clnSpeIds =  Collections.unmodifiableSet(
                geneFilters.stream().map(f -> f.getSpeciesId())
                .collect(Collectors.toSet()));
        final Map<Integer, Species> speciesMap = Collections.unmodifiableMap(
                speciesService.loadSpeciesMap(clnSpeIds, false));
        if (speciesMap.size() != clnSpeIds.size()) {
            throw new IllegalArgumentException("Some provided species not found in data source");
        }
        return log.exit(speciesMap);
    }

    /**
     * Load a {@code Gene} {@code Map} from the provided {@code GeneFilter}s, retrieved from the data source.
     * 
     * @param geneFilters   A {@code Set} of {@code GeneFilter}s specifying the {@code Gene}s to retrieve
     *                      from the data source.
     * @param speciesMap    A {@code Map} where keys are species IDs, the associated value being
     *                      the corresponding {@code Species}.
     * @param geneDAO       A {@code GeneDAO} to query the data source for gene information.
     * @return              An unmodifiable {@code Map} where keys are Bgee internal gene IDs, the associated value being
     *                      the corresponding {@code Gene}.
     * @throws GeneNotFoundException    If some requested genes could not be found.
     * @see #loadSpeciesMapFromGeneFilters(Set, SpeciesService)
     */
    protected static Map<Integer, Gene> loadGeneMapFromGeneFilters(Set<GeneFilter> geneFilters,
            Map<Integer, Species> speciesMap, GeneDAO geneDAO) throws GeneNotFoundException {
        log.entry(geneFilters, speciesMap, geneDAO);

        final Map<Integer, Set<String>> requestedSpeToGeneIdsMap = Collections.unmodifiableMap(
                geneFilters.stream()
                .collect(Collectors.toMap(gf -> gf.getSpeciesId(), gf -> gf.getEnsemblGeneIds())));

        //Make the DAO query and map GeneTOs to Genes. Store them in a Map to keep the bgeeGeneIds.
        final Map<Integer, Gene> geneMap = Collections.unmodifiableMap(geneDAO
                .getGenesBySpeciesAndGeneIds(requestedSpeToGeneIdsMap)
                .stream()
                .collect(Collectors.toMap(
                        gTO -> gTO.getId(),
                        gTO -> mapGeneTOToGene(gTO,
                                Optional.ofNullable(speciesMap.get(gTO.getSpeciesId()))
                                .orElseThrow(() -> new IllegalStateException("Missing species ID for gene")))
                        )));

        //check that we get all specifically requested genes.
        //First, build a Map Species ID -> Ensembl gene IDs for the retrieved genes.
        final Map<Integer, Set<String>> retrievedSpeToGeneIdsMap = Collections.unmodifiableMap(
                geneMap.values().stream()
                .collect(Collectors.toMap(g -> g.getSpecies().getId(),
                        g -> Stream.of(g.getEnsemblGeneId()).collect(Collectors.toSet()),
                        (s1, s2) -> {s1.addAll(s2); return s1;})));
        //now, check that we found all requested genes.
        Map<Integer, Set<String>> notFoundSpeToGeneIdsMap = requestedSpeToGeneIdsMap.entrySet().stream()
                .map(e -> {
                    Set<String> retrievedGeneIds = retrievedSpeToGeneIdsMap.get(e.getKey());
                    if (e.getValue().isEmpty()) {
                        //if no genes for the requested species, the whole species is offending
                        if (retrievedGeneIds == null || retrievedGeneIds.isEmpty()) {
                            return e;
                        }
                        //otherwise, it's OK, we found some genes for that species
                        return null;
                    }
                    //Now, if some specific IDs were requested, check we got all of them
                    if (e.getValue().equals(retrievedGeneIds)) {
                        return null;
                    }
                    Set<String> offendingGeneIds = e.getValue().stream()
                            .filter(id -> !retrievedGeneIds.contains(id))
                            .collect(Collectors.toSet());
                    return new AbstractMap.SimpleEntry<>(e.getKey(), offendingGeneIds);
                })
                .filter(e -> e != null)
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
        if (!notFoundSpeToGeneIdsMap.isEmpty()) {
            throw log.throwing(new GeneNotFoundException(notFoundSpeToGeneIdsMap));
        }

        return log.exit(geneMap);
    }

    //TODO: javadoc
    protected static Entry<Set<Integer>, Set<Integer>> convertGeneFiltersToBgeeGeneIdsAndSpeciesIds(Set<GeneFilter> geneFilters,
            Map<Integer, Gene> geneMap) {
        
        final Map<Integer, Set<String>> requestedSpeToGeneIdsMap = Collections.unmodifiableMap(
                geneFilters.stream()
                .collect(Collectors.toMap(gf -> gf.getSpeciesId(), gf -> gf.getEnsemblGeneIds())));
        //To create the CallDAOFilter, it is important to provide a species ID only if it means:
        //give me calls for all genes in that species. Otherwise, if specific genes are targeted,
        //only their bgee Gene IDs should be provided, and without their corresponding species ID.
        //
        //Note: if several GeneFilters are provided in a CallFilter, they are seen as OR condition,
        //so we should be good. And there is even a check in CallFilter to prevent a user to provide
        //a same species ID in different GeneFilters, so it's not possible to create a non-sense query.
        //
        //OK, so we need to provide either bgeeGeneIds if specific genes were requested,
        //or species IDs if all genes of a species were requested, but not both.

        //BUG FIX: we used to do simply: 'geneIdFilter = geneMap.keySet();'. But actually,
        //if only a species ID was provided in a GeneFilter, all genes from this species would be present
        //in the geneMap (see method 'loadGeneMap'). So we need to retrieve bgeeGeneIds only corresponding to
        //specific genes requested.
        //First, if specific genes were requested, to identify them faster in the geneMap,
        //from the GeneFilters we create a Map<speciesId, Set<geneId>> for requested genes
        Set<Integer> geneIdFilter = null;
        if (geneFilters.stream().anyMatch(gf -> !gf.getEnsemblGeneIds().isEmpty())) {
            //now we retrieve the appropriate Bgee gene IDs
            geneIdFilter = geneMap.entrySet().stream()
                    .filter(entry -> {
                        Set<String> speReqGeneIds = requestedSpeToGeneIdsMap.get(entry.getValue().getSpecies().getId());
                        if (speReqGeneIds == null || speReqGeneIds.isEmpty()) return false;
                        return speReqGeneIds.contains(entry.getValue().getEnsemblGeneId());
                    })
                    .map(entry -> entry.getKey())
                    .collect(Collectors.toSet());

        }
        //Identify the species IDs for which no gene IDs were specifically requested.
        //It is needed to provide the species ID only if no specific genes are requested for that species.
        Set<Integer> speciesIds = geneFilters.stream()
                .filter(gf -> gf.getEnsemblGeneIds().isEmpty())
                .map(gf -> gf.getSpeciesId())
                .collect(Collectors.toSet());

        return log.exit(new AbstractMap.SimpleEntry<>(geneIdFilter, speciesIds));
    }
}