package org.bgee.model;

import java.util.AbstractMap;
import java.util.Arrays;
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

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.DevStageService;
import org.bgee.model.anatdev.Sex;
import org.bgee.model.anatdev.SexService;
import org.bgee.model.anatdev.Strain;
import org.bgee.model.anatdev.StrainService;
import org.bgee.model.anatdev.TaxonConstraint;
import org.bgee.model.anatdev.Sex.SexEnum;
import org.bgee.model.dao.api.anatdev.TaxonConstraintDAO.TaxonConstraintTO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.exception.QueryInterruptedException;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.ConditionDAO.ConditionTO;
import org.bgee.model.dao.api.expressiondata.ConditionDAO.ConditionTO.DAOSex;
import org.bgee.model.dao.api.expressiondata.ConditionDAO.ConditionTOResultSet;
import org.bgee.model.dao.api.expressiondata.DAOConditionFilter;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneBioTypeTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.source.SourceToSpeciesDAO.SourceToSpeciesTO;
import org.bgee.model.dao.api.source.SourceToSpeciesDAO.SourceToSpeciesTO.InfoType;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTOResultSet;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.Condition;
import org.bgee.model.expressiondata.ConditionFilter;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.gene.GeneNotFoundException;
import org.bgee.model.gene.GeneXRef;
import org.bgee.model.source.Source;
import org.bgee.model.species.Species;

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
     * @param cellType      The {@code AnatEntity} corresponding to the ID returned by
     *                      {@link ConditionTO#getCellTypeId()}.
     * @param sex           The {@code Sex} corresponding to the ID returned by
     *                      {@link ConditionTO#getSexId()}.
     * @param strain        The {@code Strain} corresponding to the ID returned by
     *                      {@link ConditionTO#getStrainId()}.
     * @param species       A {@code Species} that is the species for which the {@code ConditionTO}s 
     *                      were retrieved. Allows to avoid requesting this attribute 
     *                      from the {@code ConditionDAO} if only one species was requested.
     * @return              The mapped {@code Condition}.
     */
    protected static Condition mapConditionTOToCondition(ConditionTO condTO,
            AnatEntity anatEntity, DevStage devStage, AnatEntity cellType, Sex sex,
            Strain strain, Species species) {
        log.traceEntry("{}, {}, {}, {}, {}, {},{}", condTO, anatEntity, devStage, cellType, 
                sex, strain, species);
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
                !condTO.getSex().equals(convertSexToDAOSex(sex))) {
            throw log.throwing(new IllegalArgumentException(
                    "Incorrect sex ID in ConditionTO, expected " + sex.getId() + " but was "
                    + condTO.getSex().getStringRepresentation()));
        }
        if (condTO.getStrainId() != null && strain != null &&
                !condTO.getStrainId().equals(strain.getId())) {
            throw log.throwing(new IllegalArgumentException(
                    "Incorrect strain ID in ConditionTO, expected " + strain.getId() + " but was "
                    + condTO.getStrainId()));
        }
        return log.traceExit(new Condition(anatEntity, devStage, cellType, sex,
                strain, species));
    }
    
    protected static ConditionTO mapConditionToConditionTO(int condId, Condition cond) {
        log.traceEntry("{}, {}", condId, cond);
        return log.traceExit(new ConditionTO(condId, cond.getAnatEntityId(), cond.getDevStageId(),
                cond.getCellTypeId(), convertSexToDAOSex(cond.getSex()), cond.getStrain().getId(), 
                cond.getSpeciesId(), null));
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
        log.traceEntry("{}, {}, {}, {}, {}", geneTO, species, synonyms, xRefs, geneBioType);
        if (geneTO == null) {
            return log.traceExit((Gene) null);
        }
        if (species == null) {
            throw log.throwing(new IllegalArgumentException("A Species must be provided."));
        }
        if (geneTO.getGeneMappedToGeneIdCount() == null) {
            throw log.throwing(new IllegalArgumentException(
                    "The number of genes with the same gene ID must be provided."));
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
        log.traceEntry("{}", taxonConstraintTO);
        if (taxonConstraintTO == null) {
            return log.traceExit((TaxonConstraint<T>) null);
        }

        return log.traceExit(new TaxonConstraint<T>(
                taxonConstraintTO.getEntityId(), taxonConstraintTO.getSpeciesId()));
    }

    protected static Map<Integer, GeneBioType> loadGeneBioTypeMap(GeneDAO geneDAO) {
        log.traceEntry("{}", geneDAO);
        return log.traceExit(geneDAO.getGeneBioTypes()
                .stream().collect(Collectors.toMap(to -> to.getId(), to -> mapGeneBioTypeTOToGeneBioType(to))));
    }
    protected static GeneBioType mapGeneBioTypeTOToGeneBioType(GeneBioTypeTO geneBioTypeTO) {
        log.traceEntry("{}", geneBioTypeTO);
        return log.traceExit(new GeneBioType(geneBioTypeTO.getName()));
    }

    protected static DataType convertDaoDataTypeToDataType(DAODataType dt) {
        log.traceEntry("{}", dt);
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
    
    protected static DAOSex convertSexToDAOSex(Sex sex) {
        log.traceEntry("{}", sex);
        SexEnum sexEnum = SexEnum.convertToSexEnum(sex.getId());
        switch(sexEnum) {
            case MALE:
                return log.traceExit(DAOSex.MALE);
            case FEMALE:
                return log.traceExit(DAOSex.FEMALE);
            case HERMAPHRODITE:
                return log.traceExit(DAOSex.HERMAPHRODITE);
            case ANY:
                return log.traceExit(DAOSex.ANY);
        default:
            throw log.throwing(new IllegalStateException("Unsupported Condition.Sex: " + sex));
        }
    }

    protected Map<Integer, Species> loadSpeciesMap(Set<Integer> speciesIds, boolean withSpeciesSourceInfo,
            Map<Integer, Source> sourceMap) {
        log.traceEntry("{}, {}, {}", speciesIds, withSpeciesSourceInfo, sourceMap);
        return log.traceExit(this.loadSpecies(
                ids -> this.getDaoManager().getSpeciesDAO().getSpeciesByIds(ids, null),
                speciesIds, withSpeciesSourceInfo, sourceMap)
                .stream().collect(Collectors.toMap(s -> s.getId(), s -> s)));
    }
    /**
     * @param daoCall           A {@code Function} accepting a {@code Set} of {@code Integer}s
     *                          that are, in our case, IDs of species or of taxa, and returning
     *                          a {@code SpeciesTOResultSet}, by calling, in our case, a method
     *                          of {@code SpeciesDAO} to retrieve species either by species IDs
     *                          or taxon IDs.
     * @param speOrTaxIds
     * @param withSpeciesSourceInfo
     * @return
     * @throws DAOException
     * @throws QueryInterruptedException
     */
    protected Set<Species> loadSpecies(Function<Set<Integer>, SpeciesTOResultSet> daoCall,
            Collection<Integer> speOrTaxIds, boolean withSpeciesSourceInfo,
            Map<Integer, Source> sourceMap) throws DAOException, QueryInterruptedException {
        log.traceEntry("{}, {}, {}, {}", daoCall, speOrTaxIds, withSpeciesSourceInfo, sourceMap);

        Set<Integer> filteredIds = speOrTaxIds == null? new HashSet<>(): new HashSet<>(speOrTaxIds);
        Set<SpeciesTO> speciesTOs = daoCall.apply(filteredIds).stream().collect(Collectors.toSet());
        Set<Integer> sourceIds = new HashSet<>();
        Set<Integer> speciesIds = new HashSet<>();
        for (SpeciesTO speciesTO: speciesTOs) {
            sourceIds.add(speciesTO.getDataSourceId());
            speciesIds.add(speciesTO.getId());
        }

        Set<SourceToSpeciesTO> sourceToSpeciesTOs = !withSpeciesSourceInfo? new HashSet<>():
            getDaoManager().getSourceToSpeciesDAO()
                .getSourceToSpecies(null, speciesIds, null, null, null)
                .stream().collect(Collectors.toSet());
        sourceIds.addAll(sourceToSpeciesTOs.stream()
                .map(s -> s.getDataSourceId()).collect(Collectors.toSet()));

        Map<Integer, Source> sourceMapToUse = sourceMap != null && !sourceMap.isEmpty()? sourceMap:
            !sourceIds.isEmpty()? getServiceFactory().getSourceService().loadSourcesByIds(sourceIds):
                new HashMap<>();

        Set<Species> species = speciesTOs.stream().map(speciesTO -> {
            Map<Source, Set<DataType>> forData = getDataTypesByDataSource(
                    sourceToSpeciesTOs, sourceMapToUse, speciesTO.getId(), InfoType.DATA);
            Map<Source, Set<DataType>> forAnnotation = getDataTypesByDataSource(
                    sourceToSpeciesTOs, sourceMapToUse, speciesTO.getId(), InfoType.ANNOTATION);

            return new Species(speciesTO.getId(), speciesTO.getName(), speciesTO.getDescription(),
                    speciesTO.getGenus(), speciesTO.getSpeciesName(), speciesTO.getGenomeVersion(),
                    //Genome source
                    Optional.ofNullable(sourceMapToUse.get(speciesTO.getDataSourceId()))
                    .orElseThrow(() -> new IllegalStateException(
                            "Could not find source with ID " + speciesTO.getDataSourceId())),
                    speciesTO.getGenomeSpeciesId(),
                    speciesTO.getParentTaxonId(),
                    forData, forAnnotation, speciesTO.getDisplayOrder());
        }).collect(Collectors.toSet());

        return log.traceExit(species);
    }
    /** 
     * Retrieve data types by species from {@code SourceToSpeciesTO}.
     * 
     * @param sourceToSpeciesTOs    A {@code Collection} of {@code SourceToSpeciesTO}s that are sources 
     *                              to species to be grouped.
     * @param sources               A {@code List} of {@code Source}s that are sources to be grouped.
     * @param infoType              An {@code InfoType} that is the information type for which
     *                              to return data types by species.
     * @return                      A {@code Map} where keys are {@code String}s corresponding to 
     *                              species IDs, the associated values being a {@code Set} of 
     *                              {@code DataType}s corresponding to data types of {@code infoType}
     *                              data of the provided {@code sourceId}.
     */
    private Map<Source, Set<DataType>> getDataTypesByDataSource(
            Collection<SourceToSpeciesTO> sourceToSpeciesTOs, Map<Integer, Source> sourceMap, 
            Integer speciesId, InfoType infoType) {
        log.traceEntry("{}, {}, {}, {}", sourceToSpeciesTOs, sourceMap, speciesId, infoType);

        Map<Source, Set<DataType>> map = sourceToSpeciesTOs.stream()
                .filter(to -> to.getInfoType().equals(infoType))
                .filter(to -> to.getSpeciesId().equals(speciesId))
                .collect(Collectors.toMap(
                        to -> Optional.ofNullable(sourceMap.get(to.getDataSourceId()))
                                      .orElseThrow(() -> new IllegalStateException(
                                              "Could not find source with ID " + to.getDataSourceId())), 
                        to -> new HashSet<DataType>(Arrays.asList(convertDaoDataTypeToDataType(
                                to.getDataType()))), 
                        (v1, v2) -> {v1.addAll(v2); return v1;}));
        return log.traceExit(map);
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
            Map<Integer, Species> speciesMap, Map<Integer, GeneBioType> geneBioTypeMap, GeneDAO geneDAO)
                    throws GeneNotFoundException {
        log.traceEntry("{}, {}, {}", geneFilters, speciesMap, geneDAO);

        final Map<Integer, Set<String>> requestedSpeToGeneIdsMap = Collections.unmodifiableMap(
                geneFilters.stream()
                .collect(Collectors.toMap(gf -> gf.getSpeciesId(), gf -> gf.getGeneIds())));
        Map<Integer, GeneBioType> geneBioTypeMapToUse = geneBioTypeMap == null || geneBioTypeMap.isEmpty()?
                loadGeneBioTypeMap(geneDAO): geneBioTypeMap;

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
                                Optional.ofNullable(geneBioTypeMapToUse.get(gTO.getGeneBioTypeId()))
                                .orElseThrow(() -> new IllegalStateException("Missing gene biotype ID for gene")))
                        )));

        //check that we get all specifically requested genes.
        //First, build a Map Species ID -> gene IDs for the retrieved genes.
        final Map<Integer, Set<String>> retrievedSpeToGeneIdsMap = Collections.unmodifiableMap(
                geneMap.values().stream()
                .collect(Collectors.toMap(g -> g.getSpecies().getId(),
                        g -> Stream.of(g.getGeneId()).collect(Collectors.toSet()),
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
                    Set<String> offendingGeneIds =
                            //Maybe no gene at all were retrieved based on the GeneFilter
                            retrievedGeneIds == null? new HashSet<>(e.getValue()):
                            //Otherwise, find the missing genes
                            e.getValue().stream()
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
        log.traceEntry("{}, {}", geneFilters, geneMap);

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
                .collect(Collectors.toMap(gf -> gf.getSpeciesId(), gf -> gf.getGeneIds())));

        //now we retrieve the appropriate Bgee gene IDs
        Set<Integer> geneIdFilter = null;
        if (geneFilters.stream().anyMatch(gf -> !gf.getGeneIds().isEmpty())) {
            geneIdFilter = geneMap.entrySet().stream()
                    .filter(entry -> {
                        Set<String> speReqGeneIds = requestedSpeToGeneIdsMap.get(entry.getValue().getSpecies().getId());
                        if (speReqGeneIds == null || speReqGeneIds.isEmpty()) return false;
                        return speReqGeneIds.contains(entry.getValue().getGeneId());
                    })
                    .map(entry -> entry.getKey())
                    .collect(Collectors.toSet());

        }
        //Identify the species IDs for which no gene IDs were specifically requested.
        //It is needed to provide the species ID only if no specific genes are requested for that species.
        Set<Integer> speciesIds = geneFilters.stream()
                .filter(gf -> gf.getGeneIds().isEmpty())
                .map(gf -> gf.getSpeciesId())
                .collect(Collectors.toSet());

        return log.traceExit(new AbstractMap.SimpleEntry<>(geneIdFilter, speciesIds));
    }
    protected static Set<GeneFilter> convertGenesToGeneFilters(Collection<Gene> genes) {
        log.traceEntry("{}", genes);
        if (genes == null || genes.isEmpty()) {
            return log.traceExit(new HashSet<>());
        }
        return log.traceExit(genes.stream()
                .collect(Collectors.groupingBy(g -> g.getSpecies().getId(),
                        Collectors.mapping(g -> g.getGeneId(), Collectors.toSet())))
                .entrySet().stream()
                .map(e -> new GeneFilter(e.getKey(), e.getValue()))
                .collect(Collectors.toSet()));
    }

    /**
     * 
     * @param species               A {@code Collection} of {@code Species}s that are the species 
     *                              allowing to filter the conditions to retrieve. If {@code null}
     *                              or empty, condition for all species are retrieved.
     * @param conditionFilters      A {@code Collection} of {@code DAOConditionFilter}s defining
     *                              to determine which conditions to target.
     * @param conditionDAOAttrs     A {@code Collection} of {@code ConditionDAO.Attribute}s defining
     *                              the attributes to populate in the retrieved {@code ConditionTO}s,
     *                              and thus, in the returned {@code Condition}s.
     *                              If {@code null} or empty, then all attributes are retrieved.
     * @param conditionDAO          A {@code ConditionDAO} to retrieve conditions from the data source.
     * @param anatEntityService     An {@code AnatEntityService} to retrieve the {@code AnatEntity}s
     *                              part of the returned {@code Condition}s.
     * @param devStageService       A {@code DevStageService} to retrieve the {@code DevStage}s
     *                              part of the returned {@code Condition}s.
     * @param sexService            A {@code SexService} to retrieve the {@code Sex}s
     *                              part of the returned {@code Condition}s.
     * @param strainService         A {@code StrainService} to retrieve the {@code Strain}s
     *                              part of the returned {@code Condition}s.
     * @return                      A {@code Map} where keys are {@code Integer}s
     *                              that are condition IDs, the associated value being
     *                              the corresponding {@code Condition}.
     */
    protected static Map<Integer, Condition> loadGlobalConditionMap(Collection<Species> species,
            Collection<DAOConditionFilter> conditionFilters,
            Collection<ConditionDAO.Attribute> conditionDAOAttrs, ConditionDAO conditionDAO,
            AnatEntityService anatEntityService, DevStageService devStageService,
            SexService sexService, StrainService strainService) {
        log.traceEntry("{}, {}, {}, {}, {}, {}, {}, {}", species, conditionFilters, conditionDAOAttrs, 
                conditionDAO, anatEntityService, devStageService, sexService, strainService);

        return log.traceExit(loadConditionMapFromResultSet(
                (attrs) -> conditionDAO.getGlobalConditions(
                        species.stream().map(s -> s.getId()).collect(Collectors.toSet()),
                        conditionFilters, attrs),
                conditionDAOAttrs, species, anatEntityService, devStageService, sexService,
                strainService));
    }
    
    protected static Map<Integer, Condition> loadConditionMapFromResultSet(
            Function<Collection<ConditionDAO.Attribute>, ConditionTOResultSet> rsFunc,
            Collection<ConditionDAO.Attribute> conditionDAOAttrs, Collection<Species> species,
            AnatEntityService anatEntityService, DevStageService devStageService,
            SexService sexService, StrainService strainService) {
        log.traceEntry("{}, {}, {}, {}, {}, {}, {}", rsFunc, conditionDAOAttrs, species, 
                anatEntityService, devStageService, sexService, strainService);

        if (species == null || species.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Some species must be provided"));
        }

        Map<Integer, Species> speMap = species.stream()
                .collect(Collectors.toMap(s -> s.getId(), s -> s, (s1, s2) -> s1));
        Set<String> anatEntityIds = new HashSet<>();
        Set<String> stageIds = new HashSet<>();
        Set<String> cellTypeIds = new HashSet<>();
        Set<String> sexIds = new HashSet<>();
        Set<String> strainIds = new HashSet<>();
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
                sexIds.add(condTO.getSex().getStringRepresentation());
            }
            if (condTO.getStrainId() != null) {
                strainIds.add(condTO.getStrainId());
            }
        }
        
        //merge anat entities and cell types to call only once loadAnatEntities
        Set<String> anatAndCellIds = new HashSet<String>(anatEntityIds);
        anatAndCellIds.addAll(cellTypeIds);

        final Map<String, AnatEntity> anatAndCellMap = anatAndCellIds.isEmpty()? new HashMap<>():
            anatEntityService.loadAnatEntities(
                    speMap.keySet(), true, anatAndCellIds, false)
            .collect(Collectors.toMap(a -> a.getId(), a -> a));
        if (!anatAndCellIds.isEmpty() && anatAndCellMap.size() != anatAndCellIds.size()) {
            anatAndCellIds.removeAll(anatAndCellMap.keySet());
            throw log.throwing(new IllegalStateException("Some anat. entities or cell type used in a condition "
                    + "are not supposed to exist in the related species. Species: " + speMap.keySet()
                    + " - anat. entities: " + anatAndCellIds));
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
        final Map<String, Sex> sexMap = sexIds.isEmpty()? new HashMap<>():
            //if a sex is not supported, an exception will be immediately thrown
            sexService.loadSexes(sexIds)
            .collect(Collectors.toMap(s -> s.getId(), s -> s));
        
        final Map<String, Strain> strainMap = strainIds.isEmpty()? new HashMap<>():
            strainService.loadStrains(strainIds)
            .collect(Collectors.toMap(s -> s.getId(), s -> s));
        //In case we retrieve strains from the database
        if (!strainIds.isEmpty() && strainMap.size() != strainIds.size()) {
            strainIds.removeAll(strainMap.keySet());
            throw log.throwing(new IllegalStateException("Some strains used in a condition "
                    + "are not supposed to exist in the related species. Species: " + speMap.keySet()
                    + " - strains: " + strainIds));
        }
        
        return log.traceExit(conditionTOs.stream()
                .collect(Collectors.toMap(cTO -> cTO.getId(), 
                        cTO -> mapConditionTOToCondition(cTO,
                                cTO.getAnatEntityId() == null? null:
                                    Optional.ofNullable(anatAndCellMap.get(cTO.getAnatEntityId())).orElseThrow(
                                        () -> new IllegalStateException("Anat. entity not found: "
                                                + cTO.getAnatEntityId())),
                                cTO.getStageId() == null? null:
                                    Optional.ofNullable(stageMap.get(cTO.getStageId())).orElseThrow(
                                        () -> new IllegalStateException("Stage not found: "
                                                + cTO.getStageId())),
                                cTO.getCellTypeId() == null? null:
                                    Optional.ofNullable(anatAndCellMap.get(cTO.getCellTypeId())).orElseThrow(
                                        () -> new IllegalStateException("Cell type not found: "
                                                + cTO.getCellTypeId())),
                                cTO.getSex() == null? null:
                                    Optional.ofNullable(sexMap.get(cTO.getSex().getStringRepresentation()))
                                    .orElseThrow(() -> new IllegalStateException("sex not found: "
                                                + cTO.getSex().getStringRepresentation())),
                                cTO.getStrainId() == null? null:
                                    Optional.ofNullable(strainMap.get(cTO.getStrainId()))
                                    .orElseThrow(() -> new IllegalStateException("strain not found: "
                                                + cTO.getStrainId())),
                                Optional.ofNullable(speMap.get(cTO.getSpeciesId())).orElseThrow(
                                        () -> new IllegalStateException("Species not found: "
                                                + cTO.getSpeciesId())))
                        ))
                );
    }

    protected static EnumSet<ConditionDAO.Attribute> convertCondParamAttrsToCondDAOAttrs(
            Collection<CallService.Attribute> attrs) {
        log.traceEntry("{}", attrs);
        return log.traceExit(attrs.stream()
                .filter(a -> a.isConditionParameter())
                .map(a -> convertCondParamAttrToCondDAOAttr(a))
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(ConditionDAO.Attribute.class))));
    }
    protected static ConditionDAO.Attribute convertCondParamAttrToCondDAOAttr(
            CallService.Attribute attr) {
        log.traceEntry("{}", attr);
        switch (attr) {
            case ANAT_ENTITY_ID:
                return log.traceExit(ConditionDAO.Attribute.ANAT_ENTITY_ID);
            case DEV_STAGE_ID:
                return log.traceExit(ConditionDAO.Attribute.STAGE_ID);
            case CELL_TYPE_ID:
                return log.traceExit(ConditionDAO.Attribute.CELL_TYPE_ID);
            case SEX_ID:
                return log.traceExit(ConditionDAO.Attribute.SEX_ID);
            case STRAIN_ID:
                return log.traceExit(ConditionDAO.Attribute.STRAIN_ID);
            default:
                throw log.throwing(new UnsupportedOperationException(
                    "Condition parameter not supported: " + attr));
        }
    }
    protected static Set<DAOConditionFilter> generateDAOConditionFilters(
            Collection<ConditionFilter> condFilters, EnumSet<ConditionDAO.Attribute> condParamCombination) {
        log.traceEntry("{}, {}", condFilters, condParamCombination);
        if (condFilters == null || condFilters.isEmpty()) {
            DAOConditionFilter filter = generateDAOConditionFilter(null, condParamCombination);
            if (filter == null) {
                return log.traceExit(new HashSet<>());
            }
            return log.traceExit(new HashSet<>(Collections.singleton(filter)));
        }
        return log.traceExit(condFilters.stream()
                .map(condFilter -> generateDAOConditionFilter(condFilter, condParamCombination))
                .filter(f -> f != null)
                .collect(Collectors.toSet()));
    }
    protected static DAOConditionFilter generateDAOConditionFilter(ConditionFilter condFilter,
            EnumSet<ConditionDAO.Attribute> condParamCombination) {
        log.traceEntry("{}, {}", condFilter, condParamCombination);

        if (condFilter == null && condParamCombination.containsAll(
                ConditionDAO.Attribute.getCondParams())) {
            return log.traceExit((DAOConditionFilter) null);
        }

        DAOConditionFilter daoCondFilter = new DAOConditionFilter(
                !condParamCombination.contains(ConditionDAO.Attribute.ANAT_ENTITY_ID)?
                        Collections.singleton(ConditionDAO.ANAT_ENTITY_ROOT_ID):
                            condFilter != null? condFilter.getAnatEntityIds(): null,
                !condParamCombination.contains(ConditionDAO.Attribute.STAGE_ID)?
                        Collections.singleton(ConditionDAO.DEV_STAGE_ROOT_ID):
                            condFilter != null? condFilter.getDevStageIds(): null,
                !condParamCombination.contains(ConditionDAO.Attribute.CELL_TYPE_ID)?
                        Collections.singleton(ConditionDAO.CELL_TYPE_ROOT_ID):
                            condFilter != null? condFilter.getCellTypeIds(): null,
                !condParamCombination.contains(ConditionDAO.Attribute.SEX_ID)?
                        Collections.singleton(ConditionDAO.SEX_ROOT_ID):
                            condFilter != null? condFilter.getSexIds(): null,
                !condParamCombination.contains(ConditionDAO.Attribute.STRAIN_ID)?
                        Collections.singleton(ConditionDAO.STRAIN_ROOT_ID):
                            condFilter != null? condFilter.getStrainIds(): null,
                condFilter != null?
                        convertCondParamAttrsToCondDAOAttrs(condFilter.getObservedCondForParams()): null);
        log.debug("ConditionFilter: {} - condParamCombination: {} - Generated DAOConditionFilter: {}",
                condFilter, condParamCombination, daoCondFilter);
        return log.traceExit(daoCondFilter);
    }

    protected static Strain mapRawDataStrainToStrain(String strain) {
        log.traceEntry("{}", strain);
        if (StringUtils.isBlank(strain)) {
            log.traceExit(); return null;
        }
        Function<String, String> replacement = s -> s
                .toLowerCase()
                .trim()
                .replace("-", " ")
                .replace("_", " ")
                .replace("(", "");
        String simplifiedStrain = replacement.apply(strain);

        if (RawDataConditionDAO.NO_INFO_STRAINS.stream().map(replacement)
                .anyMatch(s -> s.equals(simplifiedStrain))) {
            return log.traceExit(new Strain(ConditionDAO.STRAIN_ROOT_ID));
        }
        return log.traceExit(new Strain(strain));
    }
}