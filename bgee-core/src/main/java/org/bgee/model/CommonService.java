package org.bgee.model;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.DevStageService;
import org.bgee.model.anatdev.TaxonConstraint;
import org.bgee.model.dao.api.anatdev.TaxonConstraintDAO.TaxonConstraintTO;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.ConditionDAO.ConditionTO;
import org.bgee.model.dao.api.expressiondata.ConditionDAO.ConditionTOResultSet;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.BaseConditionTO.Sex;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneBioTypeTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.Condition;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.gene.GeneNotFoundException;
import org.bgee.model.gene.GeneXRef;
import org.bgee.model.species.Species;
import org.bgee.model.species.SpeciesService;

/**
 * Parent class of several {@code Service}s needing to access common methods. 
 * Since we do not want to expose these methods to API users, we do not build this class 
 * as an "utils" that {@code Service}s could use as a dependency, but as a parent class to inherit from.
 * 
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 14, Apr. 2019
 * @since   Bgee 14, Feb. 2017
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
//        return log.traceExit(attributes.stream().map(attr -> {
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
     * @param anatEntity    The {@code AnatEntity} corresponding to the ID returned by
     *                      {@link ConditionTO#getAnatEntityId()}.
     * @param devStage      The {@code DevStage} corresponding to the ID returned by
     *                      {@link ConditionTO#getStageId()}.
     * @param species       A {@code Species} that is the species for which the {@code ConditionTO}s 
     *                      were retrieved. Allows to avoid requesting this attribute 
     *                      from the {@code ConditionDAO} if only one species was requested.
     * @return              The mapped {@code Condition}.
     */
    protected static Condition mapConditionTOToCondition(ConditionTO condTO,
            AnatEntity anatEntity, DevStage devStage, AnatEntity cellType, String sex,
            String strain, Species species) {
        log.traceEntry("{}, {}, {}, {}, {}, {}, {}", condTO, anatEntity, devStage, cellType, sex, 
                strain, species);
        if (condTO == null) {
            return log.traceExit((Condition) null);
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
        if (condTO.getCellTypeId() != null && cellType != null &&
                !condTO.getCellTypeId().equals(cellType.getId())) {
            throw log.throwing(new IllegalArgumentException(
                    "Incorrect cell type ID in ConditionTO, expected " + cellType.getId() + " but was "
                    + condTO.getCellTypeId()));
        }
        if (condTO.getSex() != null && sex != null &&
                !condTO.getSex().getStringRepresentation().equals(sex)) {
            throw log.throwing(new IllegalArgumentException(
                    "Incorrect sex in ConditionTO, expected " + sex + " but was "
                    + condTO.getSex().getStringRepresentation()));
        }
        if (condTO.getStrain() != null && strain != null &&
                !condTO.getStrain().equals(strain)) {
            throw log.throwing(new IllegalArgumentException(
                    "Incorrect strain in ConditionTO, expected " + strain + " but was "
                    + condTO.getStrain()));
        }
        return log.traceExit(new Condition(anatEntity, devStage, cellType, sex, strain, species));
    }
    
    protected static ConditionTO mapConditionToConditionTO(int condId, Condition cond) {
        log.traceEntry("{}, {}", condId, cond);
        return log.traceExit(new ConditionTO(condId, cond.getAnatEntityId(), cond.getDevStageId(),
                cond.getCellTypeId(), convertStringToSex(cond.getSex()), cond.getStrain(), 
                cond.getSpeciesId(), null));
    }
    
    /**
     * Convert a {@code String} to a {@code Sex}.
     *
     * @param String        A {@code String} that is the sex to map to {@code Sex}.
     * @return              The mapped {@code Sex}.
     */
    protected static Sex convertStringToSex(String sex) {
        Set<Sex> allowedSexes= EnumSet.allOf(Sex.class);
        return(allowedSexes.stream().filter(s -> s.getStringRepresentation().equals(sex))
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> {
                            if(list.size() == 0) {
                                throw new IllegalStateException("String [" + sex + "] does not "
                                        + "correspond to any Sex [" + allowedSexes + "]");
                            }else if(list.size() > 1) {
                                throw new IllegalStateException("String [" + sex + "] match more than"
                                        + "one Sex [" + allowedSexes +"]");
                            }
                            return list.get(0);
                        })));        
    }
    
    /**
     * Map {@code GeneTO} to a {@code Gene}.
     *
     * @param geneTO        A {@code GeneTO} that is the condition from data source
     *                      to map into {@code Gene}.
     * @param species       A {@code Species} that is the species of the gene.
     * @param synonyms      A {@code Collection} of {@code String}s that are synonyms of the gene.
     * @param xRefs         A {@code Collection} of {@code XRef}s that are cross-references of the gene.
     * @param geneBioType   The {@code GeneBioType} of that gene.
     * @return              The mapped {@code Gene}.
     */
    protected static Gene mapGeneTOToGene(GeneTO geneTO, Species species,
            Collection<String> synonyms, Collection<GeneXRef> xRefs, GeneBioType geneBioType) {
        log.entry(geneTO, species, synonyms, xRefs, geneBioType);
        if (geneTO == null) {
            return log.traceExit((Gene) null);
        }
        if (species == null) {
            throw log.throwing(new IllegalArgumentException("A Species must be provided."));
        }
        if (geneTO.getGeneMappedToGeneIdCount() == null) {
            throw log.throwing(new IllegalArgumentException(
                    "The number of genes with the same Ensembl gene ID must be provided."));
        }
        if (geneBioType == null) {
            throw log.throwing(new IllegalArgumentException("A GeneBioType must be provided."));
        }
        if (geneTO.getSpeciesId() != null && !geneTO.getSpeciesId().equals(species.getId())) {
            throw log.throwing(new IllegalArgumentException(
                    "Species ID of the gene does not match provided Species."));
        }
        return log.traceExit(new Gene(geneTO.getGeneId(), geneTO.getName(), geneTO.getDescription(),
                synonyms, xRefs, species, geneBioType, geneTO.getGeneMappedToGeneIdCount()));
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
            return log.traceExit((TaxonConstraint<T>) null);
        }

        return log.traceExit(new TaxonConstraint<T>(
                taxonConstraintTO.getEntityId(), taxonConstraintTO.getSpeciesId()));
    }

    protected static Map<Integer, GeneBioType> loadGeneBioTypeMap(GeneDAO geneDAO) {
        log.entry(geneDAO);
        return log.traceExit(geneDAO.getGeneBioTypes()
                .stream().collect(Collectors.toMap(to -> to.getId(), to -> mapGeneBioTypeTOToGeneBioType(to))));
    }
    protected static GeneBioType mapGeneBioTypeTOToGeneBioType(GeneBioTypeTO geneBioTypeTO) {
        log.entry(geneBioTypeTO);
        return log.traceExit(new GeneBioType(geneBioTypeTO.getName()));
    }

    protected static DataType convertDaoDataTypeToDataType(DAODataType dt) {
        log.entry(dt);
        switch(dt) {
            case AFFYMETRIX:
                return log.traceExit(DataType.AFFYMETRIX);
            case EST:
                return log.traceExit(DataType.EST);
            case IN_SITU:
                return log.traceExit(DataType.IN_SITU);
            case RNA_SEQ:
                return log.traceExit(DataType.RNA_SEQ);
            case FULL_LENGTH:
                return log.traceExit(DataType.FULL_LENGTH);
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
        return log.traceExit(speciesMap);
    }

    /**
     * Load a {@code Gene} {@code Map} from the provided {@code GeneFilter}s, retrieved from the data source.
     * 
     * @param geneFilters   A {@code Set} of {@code GeneFilter}s specifying the {@code Gene}s to retrieve
     *                      from the data source.
     * @param speciesMap    A {@code Map} where keys are species IDs, the associated value being
     *                      the corresponding {@code Species}.
     * @param geneDAO       A {@code GeneDAO} to query the data source for gene information.
     * @return              An unmodifiable {@code Map} where keys are Bgee internal gene IDs,
     *                      the associated value being the corresponding {@code Gene}.
     * @throws GeneNotFoundException    If some requested genes could not be found.
     * @see #loadSpeciesMapFromGeneFilters(Set, SpeciesService)
     */
    protected static Map<Integer, Gene> loadGeneMapFromGeneFilters(Set<GeneFilter> geneFilters,
            Map<Integer, Species> speciesMap, GeneDAO geneDAO) throws GeneNotFoundException {
        log.entry(geneFilters, speciesMap, geneDAO);

        final Map<Integer, Set<String>> requestedSpeToGeneIdsMap = Collections.unmodifiableMap(
                geneFilters.stream()
                .collect(Collectors.toMap(gf -> gf.getSpeciesId(), gf -> gf.getEnsemblGeneIds())));
        final Map<Integer, GeneBioType> geneBioTypeMap = Collections.unmodifiableMap(loadGeneBioTypeMap(geneDAO));

        //Make the DAO query and map GeneTOs to Genes. Store them in a Map to keep the bgeeGeneIds.
        final Map<Integer, Gene> geneMap = Collections.unmodifiableMap(geneDAO
                .getGenesBySpeciesAndGeneIds(requestedSpeToGeneIdsMap)
                .stream()
                .collect(Collectors.toMap(
                        gTO -> gTO.getId(),
                        gTO -> mapGeneTOToGene(gTO,
                                Optional.ofNullable(speciesMap.get(gTO.getSpeciesId()))
                                .orElseThrow(() -> new IllegalStateException("Missing species ID for gene")),
                                null, null,
                                Optional.ofNullable(geneBioTypeMap.get(gTO.getGeneBioTypeId()))
                                .orElseThrow(() -> new IllegalStateException("Missing gene biotype ID for gene")))
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

        return log.traceExit(geneMap);
    }

    /**
     * Retrieve from {@code GeneFilter}s and {@code geneMap} the relevant Bgee gene IDs and
     * species IDs to perform a query to a DAO. A species ID is returned only when it was requested
     * and when there is no gene ID specified for that species. For convenience, this method
     * returns an {@code Entry}, to allow to both return the Bgee gene IDs and the species IDs.
     *
     * @param geneFilters   A {@code Set} of {@code GeneFilter}s specifying the {@code Gene}s to retrieve
     *                      from the data source.
     * @param geneMap       A {@code Map} where keys are Bgee internal gene IDs,
     *                      the associated value being the corresponding {@code Gene}.
     * @return              An {@code Entry} where the key is a {@code Set} containing the Bgee gene IDs
     *                      to use, and the value is a {@code Set} containing the species IDs to use.
     * @see #loadGeneMapFromGeneFilters(Set, Map, GeneDAO)
     */
    protected static Entry<Set<Integer>, Set<Integer>> convertGeneFiltersToBgeeGeneIdsAndSpeciesIds(
            Set<GeneFilter> geneFilters, Map<Integer, Gene> geneMap) {
        log.entry(geneFilters, geneMap);

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
        final Map<Integer, Set<String>> requestedSpeToGeneIdsMap = Collections.unmodifiableMap(
                geneFilters.stream()
                .collect(Collectors.toMap(gf -> gf.getSpeciesId(), gf -> gf.getEnsemblGeneIds())));

        //now we retrieve the appropriate Bgee gene IDs
        Set<Integer> geneIdFilter = null;
        if (geneFilters.stream().anyMatch(gf -> !gf.getEnsemblGeneIds().isEmpty())) {
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

        return log.traceExit(new AbstractMap.SimpleEntry<>(geneIdFilter, speciesIds));
    }
    protected static Set<GeneFilter> convertGenesToGeneFilters(Collection<Gene> genes) {
        log.entry(genes);
        if (genes == null || genes.isEmpty()) {
            return log.traceExit(new HashSet<>());
        }
        return log.traceExit(genes.stream()
                .collect(Collectors.groupingBy(g -> g.getSpecies().getId(),
                        Collectors.mapping(g -> g.getEnsemblGeneId(), Collectors.toSet())))
                .entrySet().stream()
                .map(e -> new GeneFilter(e.getKey(), e.getValue()))
                .collect(Collectors.toSet()));
    }

    /**
     * 
     * @param species               A {@code Collection} of {@code Species}s that are the species 
     *                              allowing to filter the conditions to retrieve. If {@code null}
     *                              or empty, condition for all species are retrieved.
     * @param condParamCombination  A {@code Collection} of {@code ConditionDAO.Attribute}s defining
     *                              the combination of condition parameters that were requested
     *                              for queries, allowing to determine which condition and expression
     *                              results to target.
     * @param conditionDAOAttrs     A {@code Collection} of {@code ConditionDAO.Attribute}s defining
     *                              the attributes to populate in the retrieved {@code ConditionTO}s,
     *                              and thus, in the returned {@code Condition}s.
     *                              If {@code null} or empty, then all attributes are retrieved.
     * @param conditionDAO          A {@code ConditionDAO} to retrieve conditions from the data source.
     * @param anatEntityService     An {@code AnatEntityService} to retrieve the {@code AnatEntity}s
     *                              part of the returned {@code Condition}s.
     * @param devStageService       A {@code DevStageService} to retrieve the {@code DevStage}s
     *                              part of the returned {@code Condition}s.
     * @return                      A {@code Map} where keys are {@code Integer}s
     *                              that are condition IDs, the associated value being
     *                              the corresponding {@code Condition}.
     */
    protected static Map<Integer, Condition> loadGlobalConditionMap(Collection<Species> species,
            Collection<ConditionDAO.Attribute> condParamCombination,
            Collection<ConditionDAO.Attribute> conditionDAOAttrs, ConditionDAO conditionDAO,
            AnatEntityService anatEntityService, DevStageService devStageService) {
        log.entry(species, condParamCombination, conditionDAOAttrs, conditionDAO,
                anatEntityService, devStageService);

        return log.traceExit(loadConditionMapFromResultSet(
                (attrs) -> conditionDAO.getGlobalConditionsBySpeciesIds(
                        species.stream().map(s -> s.getId()).collect(Collectors.toSet()),
                        condParamCombination, attrs),
                conditionDAOAttrs, species, anatEntityService, devStageService));
    }
    protected static Map<Integer, Condition> loadConditionMapFromResultSet(
            Function<Collection<ConditionDAO.Attribute>, ConditionTOResultSet> rsFunc,
            Collection<ConditionDAO.Attribute> conditionDAOAttrs, Collection<Species> species,
            AnatEntityService anatEntityService, DevStageService devStageService) {
        log.entry(rsFunc, conditionDAOAttrs, species, anatEntityService, devStageService);

        if (species == null || species.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Some species must be provided"));
        }

        Map<Integer, Species> speMap = species.stream()
                .collect(Collectors.toMap(s -> s.getId(), s -> s, (s1, s2) -> s1));
        Set<String> anatEntityIds = new HashSet<>();
        Set<String> stageIds = new HashSet<>();
        Set<String> cellTypeIds = new HashSet<>();
        Set<String> sexes = new HashSet<>();
        Set<String> strains = new HashSet<>();
        Set<ConditionTO> conditionTOs = new HashSet<>();

        //we need to retrieve the attributes requested, plus the condition ID and species ID in all cases.
        Set<ConditionDAO.Attribute> clonedAttrs = conditionDAOAttrs == null || conditionDAOAttrs.isEmpty()?
                EnumSet.allOf(ConditionDAO.Attribute.class): EnumSet.copyOf(conditionDAOAttrs);
        clonedAttrs.addAll(EnumSet.of(ConditionDAO.Attribute.ID, ConditionDAO.Attribute.SPECIES_ID));
        ConditionTOResultSet rs = rsFunc.apply(clonedAttrs);

        while (rs.next()) {
            ConditionTO condTO = rs.getTO();
            if (!speMap.keySet().contains(condTO.getSpeciesId())) {
                throw log.throwing(new IllegalArgumentException(
                        "The retrieved ConditionTOs do not match the provided Species."));
            }
            conditionTOs.add(condTO);
            if (condTO.getAnatEntityId() != null) {
                anatEntityIds.add(condTO.getAnatEntityId());
            }
            if (condTO.getStageId() != null) {
                stageIds.add(condTO.getStageId());
            }
            if (condTO.getCellTypeId() != null) {
                cellTypeIds.add(condTO.getCellTypeId());
            }
            if (condTO.getSex() != null) {
                sexes.add(condTO.getSex().getStringRepresentation());
            }
            if (condTO.getStrain() != null) {
                strains.add(condTO.getStrain());
            }
        }

        final Map<String, AnatEntity> anatMap = anatEntityIds.isEmpty()? new HashMap<>():
            anatEntityService.loadAnatEntities(
                    speMap.keySet(), true, anatEntityIds, false)
            .collect(Collectors.toMap(a -> a.getId(), a -> a));
        if (!anatEntityIds.isEmpty() && anatMap.size() != anatEntityIds.size()) {
            anatEntityIds.removeAll(anatMap.keySet());
            throw log.throwing(new IllegalStateException("Some anat. entities used in a condition "
                    + "are not supposed to exist in the related species. Species: " + speMap.keySet()
                    + " - anat. entities: " + anatEntityIds));
        }
        final Map<String, DevStage> stageMap = stageIds.isEmpty()? new HashMap<>():
            devStageService.loadDevStages(
                    speMap.keySet(), true, stageIds, false)
            .collect(Collectors.toMap(s -> s.getId(), s -> s));
        if (!stageIds.isEmpty() && stageMap.size() != stageIds.size()) {
            stageIds.removeAll(stageMap.keySet());
            throw log.throwing(new IllegalStateException("Some stages used in a condition "
                    + "are not supposed to exist in the related species. Species: " + speMap.keySet()
                    + " - stages: " + stageIds));
        }
        final Map<String, AnatEntity> cellTypeMap = cellTypeIds.isEmpty()? new HashMap<>():
            anatEntityService.loadAnatEntities(
                    speMap.keySet(), true, cellTypeIds, false)
            .collect(Collectors.toMap(s -> s.getId(), s -> s));
        if (!cellTypeIds.isEmpty() && cellTypeMap.size() != cellTypeIds.size()) {
            cellTypeIds.removeAll(cellTypeMap.keySet());
            throw log.throwing(new IllegalStateException("Some cell types used in a condition "
                    + "are not supposed to exist in the related species. Species: " + speMap.keySet()
                    + " - cell types: " + cellTypeIds));
        }
        return log.traceExit(conditionTOs.stream()
                .collect(Collectors.toMap(cTO -> cTO.getId(), 
                        cTO -> mapConditionTOToCondition(cTO,
                                cTO.getAnatEntityId() == null? null:
                                    Optional.ofNullable(anatMap.get(cTO.getAnatEntityId())).orElseThrow(
                                        () -> new IllegalStateException("Anat. entity not found: "
                                                + cTO.getAnatEntityId())),
                                cTO.getStageId() == null? null:
                                    Optional.ofNullable(stageMap.get(cTO.getStageId())).orElseThrow(
                                        () -> new IllegalStateException("Stage not found: "
                                                + cTO.getStageId())),
                                cTO.getCellTypeId() == null? null:
                                    Optional.ofNullable(cellTypeMap.get(cTO.getCellTypeId())).orElseThrow(
                                        () -> new IllegalStateException("Cell type not found: "
                                                + cTO.getCellTypeId())),
                                cTO.getSex().getStringRepresentation(),
                                cTO.getStrain(),
                                Optional.ofNullable(speMap.get(cTO.getSpeciesId())).orElseThrow(
                                        () -> new IllegalStateException("Species not found: "
                                                + cTO.getSpeciesId())))
                        ))
                );
    }

    protected static Set<ConditionDAO.Attribute> convertCondParamAttrsToCondDAOAttrs(
            Collection<CallService.Attribute> attrs) {
        log.entry(attrs);
        return log.traceExit(attrs.stream()
                .filter(a -> a.isConditionParameter())
                .map(a -> {
                    switch (a) {
                        case ANAT_ENTITY_ID:
                            return ConditionDAO.Attribute.ANAT_ENTITY_ID;
                        case DEV_STAGE_ID: 
                            return ConditionDAO.Attribute.STAGE_ID;                        
                        default: 
                            throw log.throwing(new UnsupportedOperationException(
                                "Condition parameter not supported: " + a));
                    }
                }).collect(Collectors.toSet()));
    }
}