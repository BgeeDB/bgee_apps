package org.bgee.model.expressiondata.rawdata;


//RnaSeqTechnology technology = clonedAttrs.contains(RawDataService.Attribute.TECHNOLOGY)?
//        //TODO: implement protocol name in the database schema and in the dao
//        new RnaSeqTechnology( null, libIdToLibTO.get(to.getLibraryId()).getId(),
//                Strand.convertToStrand(to.getStrandSelection().getStringRepresentation()),
//                SequencedTranscriptPart.convertToSequencedTranscriptPart(
//                        to.getSequencedTranscriptPart().getStringRepresentation()),
//                CellCompartment.convertToCellCompartment(
//                        to.getCellCompartment().getStringRepresentation()),
//                libIdToLibTO.get(to.getLibraryId()).isSampleMultiplexing(),
//                libIdToLibTO.get(to.getLibraryId()).isLibraryMultiplexing(), 
//                false, to.getBarcode()):
//                    null;

//if (!attrs.contains(RawDataService.Attribute.TECHNOLOGY)) {
//    daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.CELL_COMPARTMENT);
//    daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.FRAGMENTATION);
//    daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.GENOTYPE_ID);
//    daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.STRAND_SELECTION);
//    daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.SEQUENCED_TRANSCRIPT_PART);
//    daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.POPULATION_CAPTURE_ID);
//    daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.BARCODE);
//}
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeEnum;
import org.bgee.model.CommonService;
import org.bgee.model.ServiceFactory;
import org.bgee.model.BgeeEnum.BgeeEnumField;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.DevStageService;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataConditionFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO.RawDataConditionTOResultSet;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO.AffymetrixChipTO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO.AffymetrixChipTOResultSet;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixProbesetDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixProbesetDAO.AffymetrixProbesetTO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixProbesetDAO.AffymetrixProbesetTOResultSet;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.MicroarrayExperimentDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqExperimentDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryAnnotatedSampleDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryDAO.RNASeqLibraryTO;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.rawdata.RawCall.ExclusionReason;
import org.bgee.model.expressiondata.rawdata.RawDataCondition.RawDataSex;
import org.bgee.model.expressiondata.rawdata.est.EST;
import org.bgee.model.expressiondata.rawdata.est.ESTLibrary;
import org.bgee.model.expressiondata.rawdata.insitu.InSituEvidence;
import org.bgee.model.expressiondata.rawdata.insitu.InSituExperiment;
import org.bgee.model.expressiondata.rawdata.insitu.InSituSpot;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixChip;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixExperiment;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixProbeset;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqExperiment;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqLibrary;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqLibraryAnnotatedSample;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqLibraryPipelineSummary;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqResultAnnotatedSample;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.source.Source;
import org.bgee.model.species.Species;

/**
 * This class allows for retrieving different types of raw data information,
 * while storing the query parameters common to these different types of information.
 * This avoids to unnecessarily regenerate several times the query parameters.
 * <p>
 * Indeed, the query parameters can take some resources to be generated, for instance,
 * to retrieve the IDs of raw data conditions corresponding to a specific organ
 * plus all its substructures.
 *
 * @author Frederic Bastian
 * @author Julien Wollbrett
 * @version Bgee 15.0, Nov. 2022
 * @since Bgee 15.0, Nov. 2022
 * @see RawDataService
 */
public class RawDataLoader extends CommonService {
    private final static Logger log = LogManager.getLogger(RawDataLoader.class.getName());

    /**
     * <ul>
     * <li>{@code EXPERIMENT}:
     *   <ul>
     *   <li>For Affymetrix data, corresponds to querying {@code AffymetrixExperiment}s, or their count.
     *   <li>For RNA-Seq data, corresponds to querying {@code RnaSeqExperiment}s, or their count.
     *   <li>For <i>in situ</i> hybridization data, corresponds to querying {@code InSituExperiment}s, or their count.
     *   <li>For EST data, does not correspond to anything (no concept of "experiment" for EST).
     *   </ul>
     * <li>{@code ASSAY}:
     *   <ul>
     *   <li>For Affymetrix data, corresponds to querying {@code AffymetrixChip}s, or their count.
     *   <li>For RNA-Seq data, corresponds to querying {@code RnaSeqLibraryAnnotatedSample}s, or their count.
     *   <li>For <i>in situ</i> hybridization data, corresponds to querying {@code InSituEvidence}s,
     *       or their count.
     *   <li>For EST data, corresponds to querying {@code ESTLibrary}s, or their count.
     *   </ul>
     * <li>{@code CALL}:
     *   <ul>
     *   <li>For Affymetrix data, corresponds to querying {@code AffymetrixProbeset}s, or their count.
     *   <li>For RNA-Seq data, corresponds to querying {@code RnaSeqResultAnnotatedSample}s, or their count.
     *   <li>For <i>in situ</i> hybridization data, corresponds to querying {@code InSituSpot}s,
     *       or their count.
     *   <li>For EST data, corresponds to querying {@code EST}s, or their count.
     *   </ul>
     * </ul>
     *
     * @author Frederic Bastian
     * @version Bgee 15.0, Nov. 2022
     * @since Bgee 15.0, Nov. 2022
     */
    public static enum InformationType implements BgeeEnumField {
        EXPERIMENT("experiment"), ASSAY("assay"), CALL("result");

        private final String representation;

        private InformationType(String representation) {
            this.representation = representation;
        }

        @Override
        public String getStringRepresentation() {
            return this.representation;
        }
        public static final EnumSet<InformationType> convertToDataTypeSet(Collection<String> representations) {
            return BgeeEnum.convertStringSetToEnumSet(InformationType.class, representations);
        }
    }

    /**
     * The {@code RawDataFilter} originally used to create this {@code RawDataLoader}.
     */
    private final RawDataFilter rawDataFilter;
    /**
     * A {@code Set} of {@code DAORawDataFilter}s that allow to configure the queries to DAOs,
     * that were generated by the {@link RawDataService} based on the provided {@link #rawDataFilter}.
     * Several {@code DAORawDataFilter}s will be treated as "OR" conditions.
     */
    private final Set<DAORawDataFilter> daoRawDataFilters;

    /**
     * A {@code Map} where keys are {@code Integer}s corresponding to Bgee internal gene IDs,
     * the associated value being the corresponding {@code Gene}s, that were requested
     * to retrieve raw data. Only genes that were explicitly requested are present in this {@code Map},
     * for easier instantiation of the objects returned by this class.
     * If all genes of a species were requested, they are not present in this {@code Map} and they should be
     * retrieved as needed.
     */
    private final Map<Integer, Gene> requestedGenesMap;
    /**
     * A {@code Map} where keys are {@code Integer}s corresponding to Bgee internal
     * raw data condition IDs, the associated value being the corresponding {@code RawDataCondition}s,
     * that were requested to retrieve raw data. Only raw data conditions that were explicitly requested
     * are present in this {@code Map}, for easier instantiation of the objects returned by this class.
     * If all conditions of a species were requested, they are not present in this {@code Map} and they should be
     * retrieved as needed.
     */
    private final Map<Integer, RawDataCondition> requestedRawDataConditionsMap;
    /**
     * A {@code Map} where keys are species IDs, the associated value being
     * the corresponding {@code Species}. It is stored in order to more efficiently instantiate
     * objects returned by this class. Only {@code Species} that can potentially be queried
     * are stored in this {@code Map}.
     */
    private final Map<Integer, Species> speciesMap;
    /**
     * A {@code Map} where keys are gene biotype IDs, the associated value being
     * the corresponding {@code GeneBioType}. It is stored in order to more efficiently create
     * new {@code Gene}s.
     */
    private final Map<Integer, GeneBioType> geneBioTypeMap;
    /**
     * A {@code Map} where keys are source IDs, the associated value being
     * the corresponding {@code Source}. It is stored for easier instantiation
     * of the objects returned by this class.
     */
    private final Map<Integer, Source> sourceMap;


    private final MicroarrayExperimentDAO microarrayExperimentDAO;
    private final AffymetrixChipDAO affymetrixChipDAO;
    private final AffymetrixProbesetDAO affymetrixProbesetDAO;
    private final RawDataConditionDAO rawDataConditionDAO;
    private final AnatEntityService anatEntityService;
    private final DevStageService devStageService;


    //Constructor package protected so that only the RawDataService can instantiate this class
    RawDataLoader(ServiceFactory serviceFactory, RawDataFilter rawDataFilter,
            Collection<DAORawDataFilter> daoFilters, Map<Integer, Gene> requestedGenesMap,
            Map<Integer, RawDataCondition> requestedRawDataConditionsMap,
            //These three Maps are always used to instantiate the returned objects
            Map<Integer, Species> speciesMap, Map<Integer, GeneBioType> geneBioTypeMap,
            Map<Integer, Source> sourceMap) {
        super(serviceFactory);

        //Can be null if no parameters at all were provided
        //(to retrieve any raw data)
        this.rawDataFilter = rawDataFilter;
        this.daoRawDataFilters = Collections.unmodifiableSet(daoFilters == null? new HashSet<>():
            new HashSet<>(daoFilters));

        this.requestedGenesMap = Collections.unmodifiableMap(requestedGenesMap == null?
                new HashMap<>(): new HashMap<>(requestedGenesMap));
        if (this.requestedGenesMap.entrySet().stream()
                .anyMatch(e -> e.getKey() == null || e.getKey() < 1 || e.getValue() == null)) {
            throw log.throwing(new IllegalArgumentException("Invalid gene Map: "
                    + this.requestedGenesMap));
        }
        this.requestedRawDataConditionsMap = Collections.unmodifiableMap(
                requestedRawDataConditionsMap == null? new HashMap<>():
                    new HashMap<>(requestedRawDataConditionsMap));
        if (this.requestedRawDataConditionsMap.entrySet().stream()
                .anyMatch(e -> e.getKey() == null || e.getKey() < 1 || e.getValue() == null)) {
            throw log.throwing(new IllegalArgumentException("Invalid raw data condition Map: "
                    + this.requestedRawDataConditionsMap));
        }
        if (speciesMap == null || speciesMap.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "All Species that can potentially be queried must be provided"));
        }
        this.speciesMap = Collections.unmodifiableMap(new HashMap<>(speciesMap));
        if (this.speciesMap.entrySet().stream()
                .anyMatch(e -> e.getKey() == null || e.getKey() < 1 || e.getValue() == null)) {
            throw log.throwing(new IllegalArgumentException("Invalid species Map: "
                    + this.speciesMap));
        }
        if (geneBioTypeMap == null || geneBioTypeMap.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Gene biotypes must be provided"));
        }
        this.geneBioTypeMap = Collections.unmodifiableMap(new HashMap<>(geneBioTypeMap));
        if (this.geneBioTypeMap.entrySet().stream()
                .anyMatch(e -> e.getKey() == null || e.getKey() < 1 || e.getValue() == null)) {
            throw log.throwing(new IllegalArgumentException("Invalid gene biotype Map: "
                    + this.geneBioTypeMap));
        }
        if (sourceMap == null || sourceMap.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Sources must be provided"));
        }
        this.sourceMap = Collections.unmodifiableMap(new HashMap<>(sourceMap));
        if (this.sourceMap.entrySet().stream()
                .anyMatch(e -> e.getKey() == null || e.getKey() < 1 || e.getValue() == null)) {
            throw log.throwing(new IllegalArgumentException("Invalid source Map: "
                    + this.sourceMap));
        }

        this.microarrayExperimentDAO = this.getDaoManager().getMicroarrayExperimentDAO();
        this.affymetrixChipDAO       = this.getDaoManager().getAffymetrixChipDAO();
        this.affymetrixProbesetDAO   = this.getDaoManager().getAffymetrixProbesetDAO();
        this.rawDataConditionDAO     = this.getDaoManager().getRawDataConditionDAO();
        this.anatEntityService       = this.getServiceFactory().getAnatEntityService();
        this.devStageService         = this.getServiceFactory().getDevStageService();
    }

    public RawDataContainer loadData(InformationType infoType, int offset, int limit) {
        log.traceEntry("{}, {}, {}", infoType, offset, limit);

        RawDataContainer affyRawDataContainer = null;
        RawDataContainer rnaSeqRawDataContainer = null;
        RawDataContainer inSituRawDataContainer = null;
        RawDataContainer estRawDataContainer = null;

        //For each data type, we will load a partial RawDataContainer containing only the results
        //for that data type. We will merge all results from all data types at the end.
        if (this.getRawDataFilter().getDataTypes().contains(DataType.AFFYMETRIX)) {
            
        }
    }
///////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////// METHODS LOADING AFFYMETRIX RAW DATA ///////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
    private RawDataContainer loadAffymetrixData(InformationType infoType, int offset, int limit) {
        log.traceEntry("{}, {}, {}", infoType, offset, limit);

//        new AffymetrixProbeset(to.getId(), 
//              bgeeChipIdToAssay.get(to.getAssayId()),
//                  new RawCall(geneMap.get(to.getBgeeGeneId()), to.getPValue(),
//                          to.getExpressionConfidence(),
//                          ExclusionReason.convertToExclusionReason(to.getExclusionReason()
//                                  .getStringRepresentation())),
//                  to.getNormalizedSignalIntensity(), to.getqValue(), to.getRank())
        
//        .getAffymetrixChipDAO()
//      .getAffymetrixChips(clonedExpIds, clonedChipIds, filter, daoAttrs)
//.stream().map(to -> {
//  return new AffymetrixChip( to.getAffymetrixChipId(), expIdToExperiments.get(to.getExperimentId()), 
//          new RawDataAnnotation(condIdToCond.get(to.getConditionId()), null, null, null))

        LinkedHashSet<AffymetrixProbeset> affyProbesets = null;
        LinkedHashSet<AffymetrixChip> affyChips = null;

        Set<String> affyExpIds = new HashSet<>();
        Set<Integer> bgeeChipIds = new HashSet<>();
        LinkedHashSet<AffymetrixProbesetTO> affyProbesetTOs = new LinkedHashSet<>();
        LinkedHashSet<AffymetrixChipTO> affyChipTOs = null;

        if (infoType == InformationType.CALL) {
            affyProbesets = new LinkedHashSet<>();
            affyChips     = new LinkedHashSet<>();
            Map<Integer, AffymetrixChip> bgeeChipIdToAssay = new HashMap<>();

            //First, retrieve the probesets
            AffymetrixProbesetTOResultSet probesetTORS = this.affymetrixProbesetDAO.getAffymetrixProbesets(
                    daoRawDataFilters, offset, limit, null);
            while (probesetTORS.next()) {
                AffymetrixProbesetTO probesetTO = probesetTORS.getTO();
                bgeeChipIds.add(probesetTO.getAssayId());
                affyProbesetTOs.add(probesetTO);
            }

        }

        if (infoType == InformationType.ASSAY || !bgeeChipIds.isEmpty()) {
            affyChipTOs = new LinkedHashSet<>();
            AffymetrixChipTOResultSet chipTORS = !bgeeChipIds.isEmpty()?
                    this.affymetrixChipDAO.getAffymetrixChips(bgeeChipIds):
                    this.affymetrixChipDAO.getAffymetrixChips(daoRawDataFilters, offset, limit, null);
            while (chipTORS.next()) {
                AffymetrixChipTO chipTO = chipTORS.getTO();
                affyExpIds.add(chipTO.getExperimentId());
                affyChipTOs.add(chipTO);
            }
        }

        assert infoType == InformationType.EXPERIMENT || !affyExpIds.isEmpty():
            "Experiments should always be requested in any case";
        LinkedHashMap<String, AffymetrixExperiment> expIdToAffyExp =
                //First, define how to retrieve the experiments:
                (!affyExpIds.isEmpty()?
                //based on a list of IDs: we can use a new DAORawDataFilter to provide them
                this.microarrayExperimentDAO.getExperiments(
                        Set.of(new DAORawDataFilter(affyExpIds, null, null)), null, null, null):
                //or because it is the information requested, we use the computed DAORawDataFilters
                this.microarrayExperimentDAO.getExperiments(daoRawDataFilters, offset, limit, null))
                //Then we create a map expId -> AffymetrixExperiment
                .stream()
                .map(to -> Map.entry(to.getId(), new AffymetrixExperiment(to.getId(),
                        to.getName(), to.getDescription(),
                        getSourceById(to.getDataSourceId()))))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(),
                        (v1, v2) -> {throw new AssertionError("No key collision possible");},
                        LinkedHashMap::new));

        //Now, if we had AffymetrixChipTOs, we can convert them to AffymetrixChip
        //with its AffymetrixExperiment.
        //We first check if we have any before using a Stream, because we want affyChips
        //to remain null if the info was not requested.
        if (affyChipTOs != null) {
            
            affyChips = affyChipTOs.stream()
                    .map(chipTO -> new AffymetrixChip(to.getAffymetrixChipId(),
                            Optional.ofNullable(expIdToAffyExp.get(to.getExperimentId()))
                            .orElseThrow(new IllegalStateException(
                                    "No experiment found corresponding to ID " + to.getExperimentId())), 
          new RawDataAnnotation(condIdToCond.get(to.getConditionId()), null, null, null))
        }
        
    }

    TODO: we should call this method only once over all data type requested, not once per data type...
    this require refactoring of the code I implemented
    private Map<Integer, RawDataCondition> loadCompleteRawDataConditionMap(Set<Integer> condIds) {
        log.traceEntry("{}", condIds);
        Set<Integer> missingCondIds = new HashSet<>(condIds);
        missingCondIds.removeAll(this.requestedRawDataConditionsMap.keySet());
        if (missingCondIds.isEmpty()) {
            return log.traceExit(this.requestedRawDataConditionsMap);
        }
        Map<Integer, RawDataCondition> missingCondMap = loadRawDataConditionMapFromResultSet(
                        (attrs) -> this.rawDataConditionDAO.getRawDataConditionsFromIds(missingCondIds, attrs),
                        null, this.speciesMap.values(), anatEntityService, devStageService);
        missingCondMap.putAll(this.requestedRawDataConditionsMap);

        return log.traceExit(missingCondMap);
    }

    private Source getSourceById(Integer sourceId) {
        log.traceEntry("{}", sourceId);
        if (sourceId == null) {
            return log.traceExit((Source) null);
        }
        Source source = sourceMap.get(sourceId);
        if (source == null) {
            throw log.throwing(new IllegalStateException("No Source found corresponding to ID " + sourceId
                    + " - original sourceMap: " + sourceMap));
        }
        return log.traceExit(source);
    }
//
//    /**
//     * Load affymetrix experiments from the database
//     * 
//     * @param affymetrixExperimentIds   A {@code Collection} of {@code String} allowing to filter
//     *                                  on affymetrix experiment IDs
//     * @param affymetrixChipIds         A {@code Collection} of {@code String} allowing to filter
//     *                                  on affymetrix chip IDs
//     * @param attrs                     A {@code Collection}  of {@code RawDataService.Attribute}
//     *                                  defining information to retrieve in the 
//     *                                  {@code MicroarrayExperiment}s.
//     * @return  A {@code Stream} of {@code AffymetrixExperiment}s.
//     *          If the {@code Stream} contains no element, it means that there were no data
//     *          of this type for the requested parameters.
//     */
//    public Stream<AffymetrixExperiment> loadAffymetrixExperiments(Collection<String> affymetrixExperimentIds,
//            Collection<String> affymetrixChipIds, Collection<RawDataService.Attribute> attrs) {
//        log.traceEntry("{}, {}, {}, {}", affymetrixExperimentIds, affymetrixChipIds, attrs);
//        final Set<String> clonedExpIds =  Collections.unmodifiableSet(affymetrixExperimentIds == null?
//                new HashSet<String>(): new HashSet<String>(affymetrixExperimentIds));
//        final Set<String> clonedChipIds =  Collections.unmodifiableSet(affymetrixChipIds == null?
//                new HashSet<String>(): new HashSet<String>(affymetrixChipIds));
//        final Set<RawDataService.Attribute> clonedAttrs=  Collections.unmodifiableSet(attrs == null?
//                EnumSet.allOf(RawDataService.Attribute.class) : EnumSet.copyOf(attrs));
//
//        MicroarrayExperimentDAO expDAO = this.rawDataService.getServiceFactory()
//                .getDAOManager().getMicroarrayExperimentDAO();
//
//        // return all datasources if required
//        final Map<Integer,Source> dataSourceIdToDataSource = 
//                clonedAttrs.contains(RawDataService.Attribute.DATASOURCE) == true?
//                this.rawDataService.getServiceFactory().getSourceService().loadSourcesByIds(null):
//                    new HashMap<>();
//
//        // transform RawDataService Attributes to DAO attributes
//        Set<MicroarrayExperimentDAO.Attribute> daoAttrs = EnumSet.allOf(MicroarrayExperimentDAO
//                .Attribute.class);
//        if (!clonedAttrs.contains(RawDataService.Attribute.DATASOURCE)) {
//            daoAttrs.remove(MicroarrayExperimentDAO.Attribute.DATA_SOURCE_ID);
//        }
//
//        //generate AffymetrixExperiment objects and retrieve them
//        //XXX FB: this should be managed through one single query to the DAO
//        return log.traceExit(this.daoRawDataFilters.stream().map( filter -> {
//            return expDAO.getExperiments(clonedExpIds, clonedChipIds, filter, 
//                    daoAttrs).stream()
//                    .map(to -> new AffymetrixExperiment( to.getId(), to.getName(), 
//                            to.getDescription(), to.getDataSourceId() == null ? null: 
//                                dataSourceIdToDataSource.get(to.getDataSourceId())))
//                    .collect(Collectors.toSet());
//            }).flatMap(e -> e.stream()));
//    }
//
//    /**
//     * Load affymetrix chips from the database
//     * 
//     * @param affymetrixExperimentIds   A {@code Collection} of {@code String} allowing to filter
//     *                                  on affymetrix experiment IDs
//     * @param affymetrixChipIds         A {@code Collection} of {@code String} allowing to filter
//     *                                  on affymetrix chip IDs
//     * @param attrs                     A {@code Collection}  of {@code RawDataService.Attribute}
//     *                                  defining information to retrieve in the 
//     *                                  {@code MicroarrayExperiment}s.
//     * @return  A {@code Stream} of {@code AffymetrixChip}s.
//     *          If the {@code Stream} contains no element, it means that there were no data
//     *          of this type for the requested parameters.
//     */
//    public Stream<AffymetrixChip> loadAffymetrixChips(Collection<String> affymetrixExperimentIds, 
//            Collection<String> affymetrixChipIds, Set<RawDataService.Attribute> attrs) {
//        log.traceEntry("{}, {}, {}", affymetrixExperimentIds, affymetrixChipIds, attrs);
//        final Set<String> clonedExpIds =  Collections.unmodifiableSet(affymetrixExperimentIds == null?
//                new HashSet<String>(): new HashSet<String>(affymetrixExperimentIds));
//        final Set<String> clonedChipIds =  Collections.unmodifiableSet(affymetrixChipIds == null?
//                new HashSet<String>(): new HashSet<String>(affymetrixChipIds));
//        final Set<RawDataService.Attribute> clonedAttrs=  Collections.unmodifiableSet(attrs == null?
//                EnumSet.allOf(RawDataService.Attribute.class) : EnumSet.copyOf(attrs));
//
//        // load Experiments
//        final Map <String, AffymetrixExperiment> expIdToExperiments = 
//                loadAffymetrixExperiments(clonedExpIds, clonedChipIds, clonedAttrs)
//                .collect(Collectors.toMap(e -> e.getId(), e -> e));
//
//        // load conditions based on rawDataFilters if required
//        Map<Integer, RawDataCondition> condIdToCond = 
//                clonedAttrs.contains(RawDataService.Attribute.ANNOTATION) == true?
//                        rawDataConditionMap:
//                    null;
//
//      //create dao affymetrix chip attributes from service attributes
//        Set<AffymetrixChipDAO.Attribute> daoAttrs = fromAttrsToAffyChipDAOAttrs(clonedAttrs);
//
//        return log.traceExit(this.daoRawDataFilters.stream().map( filter -> {
//            return this.rawDataService.getServiceFactory().getDAOManager().getAffymetrixChipDAO()
//                    .getAffymetrixChips(clonedExpIds, clonedChipIds, filter, daoAttrs)
//            .stream().map(to -> {
//                return new AffymetrixChip( to.getAffymetrixChipId(), expIdToExperiments.get(to.getExperimentId()), 
//                        new RawDataAnnotation(condIdToCond.get(to.getConditionId()), null, null, null));
//            }).collect(Collectors.toSet());
//        }).flatMap(c -> c.stream()));
//    }
//
//    private Map<Integer, AffymetrixChip> loadAffymetrixChipsByBgeeChipIds(Collection<String> affyExpIds,
//            Collection<String> affyChipIds, Set<RawDataService.Attribute> attrs) {
//        log.traceEntry("{}, {}, {}", affyExpIds, affyChipIds, attrs);
//
//        // load Experiments
//        final Map <String, AffymetrixExperiment> expIdToExperiments = 
//                loadAffymetrixExperiments(affyExpIds, affyChipIds, attrs)
//                .collect(Collectors.toMap(e -> e.getId(), e -> e));
//
//        // load conditions based on rawDataFilters if required
//        final Map<Integer, RawDataCondition> condIdToCond = 
//                attrs.contains(RawDataService.Attribute.ANNOTATION) == true?
//                        rawDataConditionMap:
//                    null;
//
//        //create dao affymetrix chip attributes from service attributes
//        Set<AffymetrixChipDAO.Attribute> daoAttrs = fromAttrsToAffyChipDAOAttrs(attrs);
//
//        return log.traceExit(this.daoRawDataFilters.stream().map( filter -> {
//            return this.rawDataService.getServiceFactory().getDAOManager().getAffymetrixChipDAO()
//                    .getAffymetrixChips(affyExpIds, affyChipIds, filter, daoAttrs)
//                    .stream().collect(Collectors.toMap(to -> {Integer id = to.getId(); 
//                    return id;
//                    }, to -> {
//                        RawDataAnnotation annotation = null;
//                        if (to.getConditionId() != null) {
//                            annotation = new RawDataAnnotation(condIdToCond.get(to.getConditionId()),
//                                    null, null, null);
//                        }
//                        AffymetrixChip chip = new AffymetrixChip(to.getAffymetrixChipId(),
//                                expIdToExperiments.containsKey(to.getExperimentId())? expIdToExperiments.get(to.getExperimentId()):null, 
//                                        annotation);
//                        return chip;
//                    }));
//        }).flatMap(a -> a.entrySet().stream())
//                .collect(Collectors.toMap(q -> q.getKey(), q -> q.getValue())));
//    }
//
//    /**
//     * Load affymetrix probesets from the database
//     * 
//     * @param affymetrixProbesetIds     A {@code Collection} of {@code String} allowing to filter
//     *                                  on affymetrix probeset IDs
//     * @param affymetrixChipIds         A {@code Collection} of {@code String} allowing to filter
//     *                                  on affymetrix chip IDs
//     * @param affymetrixExperimentIds   A {@code Collection} of {@code String} allowing to filter
//     *                                  on affymetrix experiment IDs
//     * @param attrs                     A {@code Collection}  of {@code RawDataService.Attribute}
//     *                                  defining information to retrieve in the 
//     *                                  {@code MicroarrayExperiment}s.
//     * @return  A {@code Stream} of {@code AffymetrixProbeset}s.
//     *          If the {@code Stream} contains no element, it means that there were no data
//     *          of this type for the requested parameters.
//     */
//    public Stream<AffymetrixProbeset> loadAffymetrixProbesets(Collection<String> affymetrixExperimentIds,
//            Collection<String> affymetrixChipIds, Collection<String> affymetrixProbesetIds,
//            Collection<RawDataService.Attribute> attrs) {
//        log.traceEntry("{}, {}, {}, {}", affymetrixExperimentIds, affymetrixChipIds,
//                affymetrixProbesetIds, attrs);
//        final Set<String> clonedProbesetIds =  Collections.unmodifiableSet(affymetrixProbesetIds == null?
//                new HashSet<String>(): new HashSet<String>(affymetrixProbesetIds));
//        final Set<String> clonedChipIds =  Collections.unmodifiableSet(affymetrixChipIds == null?
//                new HashSet<String>(): new HashSet<String>(affymetrixChipIds));
//        final Set<String> clonedExpIds =  Collections.unmodifiableSet(affymetrixExperimentIds == null?
//                new HashSet<String>(): new HashSet<String>(affymetrixExperimentIds));
//        final Set<RawDataService.Attribute> clonedAttrs=  Collections.unmodifiableSet(attrs == null?
//                EnumSet.allOf(RawDataService.Attribute.class) : EnumSet.copyOf(attrs));
//
//        // return all Assays by bgee chip ids
//        final Map <Integer, AffymetrixChip> bgeeChipIdToAssay = 
//            this.loadAffymetrixChipsByBgeeChipIds(clonedExpIds, clonedChipIds,clonedAttrs);
//
//        //from service attributes to dao attributes
//        Set<AffymetrixProbesetDAO.Attribute> daoAttrs = fromAttrsToAffyProbesetDAOAttrs(clonedAttrs);
//        
//        return this.daoRawDataFilters.stream().map( filter -> {
//            return this.rawDataService.getServiceFactory().getDAOManager().getAffymetrixProbesetDAO()
//                    .getAffymetrixProbesets(clonedExpIds, clonedChipIds, clonedProbesetIds, filter, daoAttrs)
//            .stream().map(to -> {
//                return new AffymetrixProbeset(to.getId(), 
//                        bgeeChipIdToAssay.get(to.getAssayId()),
//                            new RawCall(geneMap.get(to.getBgeeGeneId()), to.getPValue(),
//                                    to.getExpressionConfidence(),
//                                    ExclusionReason.convertToExclusionReason(to.getExclusionReason()
//                                            .getStringRepresentation())),
//                            to.getNormalizedSignalIntensity(), to.getqValue(), to.getRank());
//            }).collect(Collectors.toSet());
//        }).flatMap(c -> c.stream());
//    }
//    
//    private Map<Integer, RawDataCondition> loadRawDataConditionMap(
//            Collection<RawDataFilter> dataFilters) {
//        log.traceEntry("{}, {}", dataFilters);
//        // combine condition filters coming from all dataFilters and retrieve corresponding 
//        // dao condition filters
//        AnatEntityService aeService = rawDataService.getServiceFactory().getAnatEntityService();
//        DevStageService dsService = rawDataService.getServiceFactory().getDevStageService();
//        Set<DAORawDataConditionFilter> condFiltersDAO = 
//                convertRawDataConditionFiltersToDAORawDataConditionFilters(
//                        dataFilters.stream().map(df -> df.getConditionFilters())
//                        .flatMap(df -> df.stream()).collect(Collectors.toSet()));
//        // combine geneFilters coming from all dataFilters and retrieve corresponding speciesMap
//        final Map<Integer, Species> speciesMap = rawDataService.getServiceFactory()
//                .getSpeciesService().loadSpeciesMapFromGeneFilters(dataFilters.stream()
//                        .map(df -> df.getGeneFilters())
//                        .flatMap(gf -> gf.stream()).collect(Collectors.toSet()), false);
//
//        final Map<String, AnatEntity> anatEntitiesMap = aeService.loadAnatEntities(
//                speciesMap.keySet(), true, condFiltersDAO.stream()
//                    .map(cf -> cf.getAnatEntityIds()).flatMap(aes -> aes.stream())
//                    .collect(Collectors.toSet()), false)
//                .collect(Collectors.toMap(ae -> ae.getId(), ae -> ae));
//        final Map<String, AnatEntity> cellTypesMap = aeService.loadAnatEntities(
//                speciesMap.keySet(), true, condFiltersDAO.stream()
//                    .map(cf -> cf.getCellTypeIds()).flatMap(aes -> aes.stream())
//                    .collect(Collectors.toSet()), false)
//                .collect(Collectors.toMap(ct -> ct.getId(), ct -> ct));
//        final Map<String,DevStage> devStagesMap = dsService.loadDevStages(
//                speciesMap.keySet(), true, condFiltersDAO.stream()
//                    .map(cf -> cf.getDevStageIds()).flatMap(aes -> aes.stream())
//                    .collect(Collectors.toSet()), false)
//                .collect(Collectors.toMap(ds -> ds.getId(), ds -> ds));
//        final Map<String,RawDataSex> sexesMap = EnumSet.allOf(RawDataSex.class)
//                .stream().collect(Collectors.toMap(s -> s.getStringRepresentation(), s -> s));
//        //load raw data conditions
//        RawDataConditionTOResultSet rawDataCondTOs = rawDataService.getServiceFactory().getDAOManager()
//                .getRawDataConditionDAO().getRawDataConditions(speciesMap.keySet(),
//                        condFiltersDAO, null);
//        //create Map with condition ID as key and RawDataCondition as value
//        return log.traceExit(rawDataCondTOs.stream().collect(Collectors
//                .toMap(condTO -> condTO.getId(),
//                        condTO -> 
//                new RawDataCondition( anatEntitiesMap.get(condTO.getAnatEntityId()),
//                        devStagesMap.get(condTO.getStageId()),
//                        cellTypesMap.get(condTO.getCellTypeId()),
//                        sexesMap.get(condTO.getSex().getStringRepresentation()),
//                        condTO.getStrainId(), null)
//                )));
//    }
//    
//    private static Set<DAORawDataConditionFilter> 
//    convertRawDataConditionFiltersToDAORawDataConditionFilters(
//            Collection<RawDataConditionFilter> condFilters) {
//        log.traceEntry("{}", condFilters);
//        if (condFilters == null || condFilters.isEmpty()) {
//            return log.traceExit(Set.of());
//        }
//        if (condFilters.contains(null)) {
//            throw log.throwing(new IllegalArgumentException("no condition filter can be null"));
//        }
//        return log.traceExit(condFilters.stream().map(cf -> new DAORawDataConditionFilter(
//                cf.getAnatEntityIds(),cf.getCellTypeIds(), cf.getDevStageIds(),
//                cf.getSexes(), cf.getStrains(),
//                cf.getIncludeSubConditions(), cf.getIncludeParentConditions()))
//        .collect(Collectors.toSet()));
//    }
//
//    // generate dao attributes from service Attribute
//    private static Set<AffymetrixChipDAO.Attribute> fromAttrsToAffyChipDAOAttrs(Set<RawDataService.Attribute> attrs) {
//        log.traceEntry("{}", attrs);
//        Set<AffymetrixChipDAO.Attribute> daoAttrs = EnumSet.allOf(AffymetrixChipDAO.Attribute.class);
//        if (!attrs.contains(RawDataService.Attribute.ANNOTATION)) {
//            daoAttrs.remove(AffymetrixChipDAO.Attribute.CONDITION_ID);
//        }
//        if (!attrs.contains(RawDataService.Attribute.ASSAY_PIPELINE_SUMMARY)) {
//            daoAttrs.removeAll(Set.of(AffymetrixChipDAO.Attribute.DISTINCT_RANK_COUNT,
//                    AffymetrixChipDAO.Attribute.MAX_RANK, 
//                    AffymetrixChipDAO.Attribute.PERCENT_PRESENT));
//        }
//        return log.traceExit(daoAttrs);
//    }
//    
//    // generate dao attributes from service Attribute
//    private static Set<AffymetrixProbesetDAO.Attribute> fromAttrsToAffyProbesetDAOAttrs(Set<RawDataService.Attribute> attrs) {
//        log.traceEntry("{}", attrs);
//        Set<AffymetrixProbesetDAO.Attribute> daoAttrs = EnumSet.allOf(AffymetrixProbesetDAO.Attribute.class);
//        if (!attrs.contains(RawDataService.Attribute.RAWCALL_PIPELINE_SUMMARY)) {
//            daoAttrs.remove(AffymetrixProbesetDAO.Attribute.NORMALIZED_SIGNAL_INTENSITY);
//            daoAttrs.remove(AffymetrixProbesetDAO.Attribute.QVALUE);
//            daoAttrs.remove(AffymetrixProbesetDAO.Attribute.RANK);
//        }
//        return log.traceExit(daoAttrs);
//    }
//        
/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// METHODS LOADING RNA-SEQ RAW DATA ////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
//
//    /**
//     * Load rna-seq experiments from the database
//     * 
//     * @param rnaSeqExperimentIds       A {@code Collection} of {@code String} allowing to filter
//     *                                  on RNA-Seq experiment IDs
//     * @param rnaSeqLibraryIds          A {@code Collection} of {@code String} allowing to filter
//     *                                  on RNA-Seq library IDs
//     * @param attrs                     A {@code Collection}  of {@code RawDataService.Attribute}
//     *                                  defining information to retrieve in the 
//     *                                  {@code RnaSeqExperiment}s. 
//     * @return  A {@code Stream} of {@code RnaSeqExperiment}s.
//     *          If the {@code Stream} contains no element, it means that there were no data
//     *          of this type for the requested parameters.
//     */
//    public Stream<RnaSeqExperiment> loadRnaSeqExperiments(Collection<String> rnaSeqExperimentIds,
//            Collection<String> rnaSeqLibraryIds, Collection<RawDataService.Attribute> attrs) {
//        log.traceEntry("{}, {}, {}", rnaSeqExperimentIds, rnaSeqLibraryIds, attrs);
//        final Set<String> clonedExpIds =  Collections.unmodifiableSet(rnaSeqExperimentIds == null?
//                new HashSet<String>(): new HashSet<String>(rnaSeqExperimentIds));
//        final Set<String> clonedLibIds =  Collections.unmodifiableSet(rnaSeqLibraryIds == null?
//                new HashSet<String>(): new HashSet<String>(rnaSeqLibraryIds));
//        final Set<RawDataService.Attribute> clonedAttrs=  Collections.unmodifiableSet(attrs == null?
//                EnumSet.allOf(RawDataService.Attribute.class) : EnumSet.copyOf(attrs));
//
//        RNASeqExperimentDAO expDAO = this.rawDataService.getServiceFactory()
//                .getDAOManager().getRnaSeqExperimentDAO();
//
//        // return all datasources if required
//        final Map<Integer,Source> dataSourceIdToDataSource = 
//                clonedAttrs.contains(RawDataService.Attribute.DATASOURCE) == true?
//                this.rawDataService.getServiceFactory().getSourceService().loadSourcesByIds(null):
//                    new HashMap<>();
//
//        // transform RnaSeqService Attributes to DAO attributes
//        Set<RNASeqExperimentDAO.Attribute> daoAttrs = EnumSet.allOf(RNASeqExperimentDAO
//                .Attribute.class);
//        if (!clonedAttrs.contains(RawDataService.Attribute.DATASOURCE)) {
//            daoAttrs.remove(RNASeqExperimentDAO.Attribute.DATA_SOURCE_ID);
//        }
//
//        // load experiments
//        return log.traceExit(this.daoRawDataFilters.stream().map( filter -> {
//            return expDAO.getExperiments(clonedExpIds, clonedLibIds, filter, 
//                    daoAttrs).stream()
//                    .map(to -> new RnaSeqExperiment( to.getId(), to.getName(), 
//                            to.getDescription(), to.getDataSourceId() == null ? null: 
//                                dataSourceIdToDataSource.get(to.getDataSourceId())))
//                    .collect(Collectors.toSet());
//            }).flatMap(e -> e.stream()));
//    }
//
//    /**
//     * Load Annotated libraries from the database.
//     * 
//     * @param rnaSeqExperimentIds       A {@code Collection} of {@code String} allowing to filter
//     *                                  on rna-seq experiment IDs
//     * @param affymetrixChipIds         A {@code Collection} of {@code String} allowing to filter
//     *                                  on affymetrix chip IDs
//     * @param attrs                     A {@code Collection}  of {@code RawDataService.Attribute}
//     *                                  defining information to retrieve in the 
//     *                                  {@code MicroarrayExperiment}s.
//     * @return  A {@code Stream} of {@code AffymetrixChip}s.
//     *          If the {@code Stream} contains no element, it means that there were no data
//     *          of this type for the requested parameters.
//     */
//    public Stream<RnaSeqLibraryAnnotatedSample> loadRnaSeqLibraryAnnotatedSample(
//            Collection<String> rnaSeqExperimentIds, Collection<String> rnaSeqLibraryIds,
//            Set<RawDataService.Attribute> attrs) {
//        log.traceEntry("{}, {}, {}", rnaSeqExperimentIds, rnaSeqLibraryIds, attrs);
//        final Set<String> clonedExpIds =  Collections.unmodifiableSet(rnaSeqExperimentIds == null?
//                new HashSet<String>(): new HashSet<String>(rnaSeqExperimentIds));
//        final Set<String> clonedLibIds =  Collections.unmodifiableSet(rnaSeqLibraryIds == null?
//                new HashSet<String>(): new HashSet<String>(rnaSeqLibraryIds));
//        final Set<RawDataService.Attribute> clonedAttrs=  Collections.unmodifiableSet(attrs == null?
//                EnumSet.allOf(RawDataService.Attribute.class) : EnumSet.copyOf(attrs));
//
//        // load Experiments if required
//        final Map <String, RnaSeqExperiment> expIdToExperiments = 
//                loadRnaSeqExperiments(clonedExpIds, clonedLibIds, clonedAttrs)
//                .collect(Collectors.toMap(e -> e.getId(), e -> e));
//
//        // load conditions based on rawDataFilters if required
//        Map<Integer, RawDataCondition> condIdToCond = 
//                clonedAttrs.contains(RawDataService.Attribute.ANNOTATION)?
//                        rawDataConditionMap: null;
//
//        //generate dao attributes based on RawDataServiceAttributes
//        final Set<RNASeqLibraryAnnotatedSampleDAO.Attribute> daoAttrs = 
//                fromAttrsToRnaSeqLibAnnotSampleDAOAttrs(clonedAttrs);
//        
//        // load libraries 
//        Map<String, RNASeqLibraryTO> libIdToLibTO = daoRawDataFilters.stream()
//                .map(filter -> {
//                    return rawDataService.getServiceFactory().getDAOManager().getRnaSeqLibraryDAO()
//                            .getRnaSeqLibraries( clonedExpIds, clonedLibIds, filter, null)
//                            .stream().collect(Collectors.toMap(l -> l.getId(), l -> l));
//                }).flatMap(l -> l.entrySet().stream())
//                .collect(Collectors.toMap(l -> l.getKey(), l -> l.getValue()));
//        return log.traceExit(this.daoRawDataFilters.stream().map( filter -> {
//            return this.rawDataService.getServiceFactory().getDAOManager()
//                    .getRnaSeqLibraryAnnotatedSampleDAO()
//                    .getRnaSeqLibraryAnnotatedSamples(clonedExpIds, clonedLibIds, filter, daoAttrs)
//            .stream().map(to -> {
//                //TODO: To continue. 
//                
//                RawDataAnnotation annotation = clonedAttrs.contains(RawDataService.Attribute.ANNOTATION)?
//                        new RawDataAnnotation(condIdToCond.get(to.getConditionId()), null, null, null):
//                            null;
//                RnaSeqLibraryPipelineSummary pipelineSummary = clonedAttrs.contains(RawDataService
//                        .Attribute.ASSAY_PIPELINE_SUMMARY)?
//                        new RnaSeqLibraryPipelineSummary( to.getMeanAbundanceRefIntergenicDistribution(),
//                                to.getSdAbundanceRefIntergenicDistribution(), to.getpValueThreshold(),
//                                to.getAllReadCount(), to.getAllUMIsCount(), to.getMappedReadCount(),
//                                to.getMappedUMIsCount(), to.getMinReadLength(), to.getMaxReadLength(),
//                                to.getMaxRank(), to.getDistinctRankCount()):
//                            null;
//                return new RnaSeqLibraryAnnotatedSample( to.getLibraryId(),
//                        expIdToExperiments.get(libIdToLibTO.get(to.getLibraryId()).getExperimentId()),
//                        annotation, pipelineSummary, to.getBarcode(), to.getGenotype());
//            }).collect(Collectors.toSet());
//        }).flatMap(c -> c.stream()));
//    }
//
//    // generate dao attributes from service Attribute
//    private static Set<RNASeqLibraryAnnotatedSampleDAO.Attribute> fromAttrsToRnaSeqLibAnnotSampleDAOAttrs(
//            Set<RawDataService.Attribute> attrs) {
//        log.traceEntry("{}", attrs);
//        Set<RNASeqLibraryAnnotatedSampleDAO.Attribute> daoAttrs = 
//                EnumSet.allOf(RNASeqLibraryAnnotatedSampleDAO.Attribute.class);
//        if (!attrs.contains(RawDataService.Attribute.ASSAY_PIPELINE_SUMMARY)) {
//            daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.ABUNDANCE_THRESHOLD);
//            daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.ABUNDANCE_UNIT);
//            daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.ALL_GENES_PERCENT_PRESENT);
//            daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.ALL_READ_COUNT);
//            daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.ALL_UMIS_COUNT);
//            daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.DISTINCT_RANK_COUNT);
//            daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.INTERGENIC_REGION_PERCENT_PRESENT);
//            daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.MAPPED_READ_COUNT);
//            daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.MAPPED_UMIS_COUNT);
//            daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.MAX_RANK);
//            daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.MAX_READ_LENGTH);
//            daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.MEAN_ABUNDANCE_REF_INTERGENIC_DISCTRIBUTION);
//            daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.MIN_READ_LENGTH);
//            daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.PROTEIN_CODING_GENES_PERCENT_PRESENT);
//            daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.PVALUE_THRESHOLD);
//            daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.SD_ABUNDANCE_REF_INTERGENIC_DISCTRIBUTION);
//            daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.TMM_FACTOR);
//        }
//        
//        if (!attrs.contains(RawDataService.Attribute.ANNOTATION)) {
//            daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.CONDITION_ID);
//        }
//        return log.traceExit(daoAttrs);
//    }

    /**
     * @return  The {@code RawDataFilter} originally used to create this {@code RawDataLoader}.
     */
    public RawDataFilter getRawDataFilter() {
        return rawDataFilter;
    }

    @Override
    public int hashCode() {
        return Objects.hash(daoRawDataFilters, geneBioTypeMap, rawDataFilter, requestedGenesMap,
                requestedRawDataConditionsMap, speciesMap);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RawDataLoader other = (RawDataLoader) obj;
        return Objects.equals(daoRawDataFilters, other.daoRawDataFilters)
                && Objects.equals(geneBioTypeMap, other.geneBioTypeMap)
                && Objects.equals(rawDataFilter, other.rawDataFilter)
                && Objects.equals(requestedGenesMap, other.requestedGenesMap)
                && Objects.equals(requestedRawDataConditionsMap, other.requestedRawDataConditionsMap)
                && Objects.equals(speciesMap, other.speciesMap);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RawDataLoader [rawDataFilter=").append(rawDataFilter)
               .append(", daoRawDataFilters=").append(daoRawDataFilters)
               .append("]");
        return builder.toString();
    }
}