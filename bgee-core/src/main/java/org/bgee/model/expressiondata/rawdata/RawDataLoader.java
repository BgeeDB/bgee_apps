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
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeEnum;
import org.bgee.model.CommonService;
import org.bgee.model.ServiceFactory;
import org.bgee.model.BgeeEnum.BgeeEnumField;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.anatdev.DevStageService;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCountDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCountDAO.RawDataCountContainerTO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO.AffymetrixChipTO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO.AffymetrixChipTOResultSet;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixProbesetDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixProbesetDAO.AffymetrixProbesetTO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixProbesetDAO.AffymetrixProbesetTOResultSet;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.MicroarrayExperimentDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.MicroarrayExperimentDAO.MicroarrayExperimentTOResultSet;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.rawdata.RawCall.ExclusionReason;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixChip;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixExperiment;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixProbeset;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.source.Source;
import org.bgee.model.species.Species;

/**
 * This class allows for retrieving different types of raw data information,
 * while storing the query parameters common to these different types of information.
 * This avoids to unnecessarily process several times the query parameters.
 * <p>
 * Indeed, the query parameters can take some resources to be generated, for instance,
 * to retrieve the IDs of raw data conditions corresponding to a specific organ
 * plus all its substructures. See {@link #getRawDataProcessedFilter()} and
 * {@link RawDataService#getRawDataLoader(RawDataProcessedFilter)} for more information.
 *
 * @author Frederic Bastian
 * @author Julien Wollbrett
 * @version Bgee 15.0, Nov. 2022
 * @since Bgee 15.0, Nov. 2022
 * @see #getRawDataProcessedFilter()
 * @see RawDataService
 * @see RawDataService#getRawDataLoader(RawDataProcessedFilter)
 * @see RawDataService#loadRawDataLoader(RawDataFilter)
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
     * A simple container to store results for one data type, in order to aggregate
     * the results for different data types, and optimize some downstream queries,
     * such as retrieving all {@code RawDataCondition}s or {@code Gene}s
     * in one query at once for all requested data type.
     *
     * @author Frederic Bastian
     * @version Bgee 15.0, Nov. 2022
     * @since Bgee 15.0, Nov. 2022
     * @param <T>   The type of ID for {@code Experiment} {@code U}, extending {@code Comparable}.
     * @param <U>   {@code Experiment} type for a data type.
     * @param <V>   {@code TransferObject} representing an assay for a data type.
     * @param <W>   {@code TransferObject} representing a call for a data type.
     */
    private static class TempRawDataContainer<T extends Comparable<T>, U extends Experiment<T>,
    V extends TransferObject, W extends TransferObject> {
        public final LinkedHashMap<T, U> experimentMap;
        public final LinkedHashSet<V> assayTOs;
        public final LinkedHashSet<W> callTOs;
        public final Set<Integer> rawDataConditionIds;
        public final Set<Integer> bgeeGeneIds;

        protected TempRawDataContainer(LinkedHashMap<T, U> experimentMap, LinkedHashSet<V> assayTOs,
                LinkedHashSet<W> callTOs, Set<Integer> rawDataConditionIds, Set<Integer> bgeeGeneIds) {
            this.experimentMap = experimentMap;
            this.assayTOs = assayTOs;
            this.callTOs = callTOs;
            this.rawDataConditionIds = rawDataConditionIds;
            this.bgeeGeneIds = bgeeGeneIds;
        }
    }
    private static class AffyTempRawDataContainer extends TempRawDataContainer<String, AffymetrixExperiment,
    AffymetrixChipTO, AffymetrixProbesetTO> {

        protected AffyTempRawDataContainer(LinkedHashMap<String, AffymetrixExperiment> experimentMap,
                LinkedHashSet<AffymetrixChipTO> assayTOs, LinkedHashSet<AffymetrixProbesetTO> callTOs,
                Set<Integer> rawDataConditionIds, Set<Integer> bgeeGeneIds) {
            super(experimentMap, assayTOs, callTOs, rawDataConditionIds, bgeeGeneIds);
        }
    }

    /**
     * An {@code int} that is the maximum allowed number of results
     * to retrieve in one method call, for each requested data type independently.
     * Value: 10,000.
     */
    public static int LIMIT_MAX = 10000;
    /**
     * Check the validity of the {@code offset} and {@code limit} arguments.
     *
     * @param offset    An {@code int} that is the offset to check.
     * @param limit     An {@code int} that is the limit to check.
     * @throws IllegalArgumentException If {@code offset} is less than 0,
     *                                  or {@code limit} is less than or equal to 0,
     *                                  or {@code limit} is greater than {@link #LIMIT_MAX}.
     * @see #LIMIT_MAX
     */
    private static void checkOffsetLimit(int offset, int limit) throws IllegalArgumentException {
        log.traceEntry("{}, {}", offset, limit);
        if (offset < 0) {
            throw log.throwing(new IllegalArgumentException("offset cannot be less than 0"));
        }
        if (limit <= 0) {
            throw log.throwing(new IllegalArgumentException(
                    "limit cannot be less than or equal to 0"));
        }
        if (limit > LIMIT_MAX) {
            throw log.throwing(new IllegalArgumentException("limit cannot be greater than "
                    + LIMIT_MAX));
        }
        log.traceExit();
    }


    /**
     * @see #getRawDataProcessedFilter()
     */
    private final RawDataProcessedFilter rawDataProcessedFilter;

    //DAOs and Services used by this class
    private final MicroarrayExperimentDAO microarrayExperimentDAO;
    private final AffymetrixChipDAO affymetrixChipDAO;
    private final AffymetrixProbesetDAO affymetrixProbesetDAO;
    private final RawDataConditionDAO rawDataConditionDAO;
    private final RawDataCountDAO rawDataCountDAO;
    private final GeneDAO geneDAO;
    private final AnatEntityService anatEntityService;
    private final DevStageService devStageService;

    //Constructor package protected so that only the RawDataService can instantiate this class
    RawDataLoader(ServiceFactory serviceFactory, RawDataProcessedFilter rawDataProcessedFilter) {
        super(serviceFactory);

        if (rawDataProcessedFilter == null) {
            //we need it at least to retrieve, species, gene biotypes, and sources
            throw log.throwing(new IllegalArgumentException(
                    "A RawDataProcessedFilter must be provided"));
        }
        this.rawDataProcessedFilter = rawDataProcessedFilter;

        this.microarrayExperimentDAO = this.getDaoManager().getMicroarrayExperimentDAO();
        this.affymetrixChipDAO       = this.getDaoManager().getAffymetrixChipDAO();
        this.affymetrixProbesetDAO   = this.getDaoManager().getAffymetrixProbesetDAO();
        this.rawDataConditionDAO     = this.getDaoManager().getRawDataConditionDAO();
        this.geneDAO                 = this.getDaoManager().getGeneDAO();
        this.anatEntityService       = this.getServiceFactory().getAnatEntityService();
        this.devStageService         = this.getServiceFactory().getDevStageService();
        this.rawDataCountDAO         = this.getDaoManager().getRawDataCountDAO();
    }

    /**
     * Load raw data of the specified {@code InformationType}. The {@code offset} and {@code limit}
     * parameters apply independently to each {@code DataType} requested. For instance,
     * if {@code limit} was set to 1000, the returned {@code RawDataContainer} could contain
     * 1000 Affymetrix probesets and 1000 bulk RNA-Seq calls.
     *
     * @param infoType  The {@code InformationType} to load.
     * @param offset    An {@code int} specifying at which index to start getting results
     *                  of the type {@code infoType} for each requested data type independently.
     *                  First index is {@code 0}.
     * @param limit     An {@code int} specifying the number of results of type {@code infoType}
     *                  to retrieve for each requested data type independently. Cannot be greater
     *                  than {@link #LIMIT_MAX}.
     * @return          A {@code RawDataContainer} containing the requested results.
     * @throws IllegalArgumentException If {@code infoType} is null,
     *                                  or {@code offset} is less than 0,
     *                                  or {@code limit} is less than or equal to 0,
     *                                  or {@code limit} is greater than {@link #LIMIT_MAX}.
     */
    public RawDataContainer loadData(InformationType infoType, int offset, int limit)
            throws IllegalArgumentException {
        log.traceEntry("{}, {}, {}", infoType, offset, limit);
        if (infoType == null) {
            throw log.throwing(new IllegalArgumentException("An InformationType must be provided"));
        }
        checkOffsetLimit(offset, limit);

        AffyTempRawDataContainer affyTempRawDataContainer = null;
        Set<TempRawDataContainer<?, ?, ?, ?>> allTempContainers = new HashSet<>();

        //*****************************************************************************************
        //For each data type, we will load a TempRawDataContainer, holding the information,
        //notably TransferObjects, allowing to instantiate the final objects.
        //This will allow us to process some downstream information only once for all data types,
        //and not once for each data type.
        //*****************************************************************************************
        final Set<DataType> dataTypes = this.getRawDataProcessedFilter().getDataTypes();
        if (dataTypes.contains(DataType.AFFYMETRIX)) {
            affyTempRawDataContainer = this.loadAffymetrixData(infoType, offset, limit);
            allTempContainers.add(affyTempRawDataContainer);
        }

        //*****************************************************************************************
        //Now we retrieve all RawDataConditions and Genes necessary to instantiate objects,
        //over all data types
        //*****************************************************************************************
        Set<Integer> allRawDataCondIds = new HashSet<>();
        Set<Integer> allBgeeGeneIds = new HashSet<>();
        for (TempRawDataContainer<?, ?, ?, ?> tempContainer: allTempContainers) {
            allRawDataCondIds.addAll(tempContainer.rawDataConditionIds);
            allBgeeGeneIds.addAll(tempContainer.bgeeGeneIds);
        }
        Map<Integer, RawDataCondition> condMap = this.loadCompleteRawDataConditionMap(allRawDataCondIds);
        Map<Integer, Gene> geneMap = this.loadCompleteGeneMap(allBgeeGeneIds);

        //*****************************************************************************************
        //Now we load the actual data for each data type
        //*****************************************************************************************
        RawDataContainer partialAffyContainer = this.loadPartialAffymetrixRawDataContainer(
                    affyTempRawDataContainer, infoType, condMap, geneMap);

        //*****************************************************************************************
        //And finally we merge all results in one single container
        //*****************************************************************************************
        return log.traceExit(new RawDataContainer(dataTypes,
                //Affymetrix
                partialAffyContainer.getAffymetrixExperiments(),
                partialAffyContainer.getAffymetrixAssays(),
                partialAffyContainer.getAffymetrixCalls(),
                //RNA-Seq
                null, null, null, null,
                //In situ
                null, null, null,
                //EST
                null, null));
    }

    public RawDataCountContainer loadDataCount(EnumSet<InformationType> infoTypes) {
        log.traceEntry("{}", infoTypes);

        // create booleans used to query the DAO
        boolean withCalls = infoTypes.contains(InformationType.CALL) ? true: false;
        boolean withAssay = infoTypes.contains(InformationType.ASSAY) ? true: false;
        boolean withExperiment = infoTypes.contains(InformationType.EXPERIMENT) ? true: false;

        RawDataCountContainerTO affyCountTO = this.rawDataProcessedFilter.getDataTypes()
                .contains(DataType.AFFYMETRIX) ? rawDataCountDAO.getAffymetrixCount(
                        this.getRawDataProcessedFilter().getDaoRawDataFilters(), withExperiment,
                        withAssay, withCalls): new RawDataCountContainerTO(null, null, null);

        //TODO: count for est, insitu, bulk rnaseq and single cell rnaseq are not yet implemented
        // in the DAO
        RawDataCountContainerTO estCountTO = this.rawDataProcessedFilter.getDataTypes()
                .contains(DataType.EST) ? null: new RawDataCountContainerTO(null, null, null);
        RawDataCountContainerTO inSituCountTO = this.rawDataProcessedFilter.getDataTypes()
                .contains(DataType.IN_SITU) ? null: new RawDataCountContainerTO(null, null, null);
        RawDataCountContainerTO bulkRnaSeqCountTO = this.rawDataProcessedFilter.getDataTypes()
                .contains(DataType.RNA_SEQ) ? null: new RawDataCountContainerTO(null, null, null);
        RawDataCountContainerTO singleCellRnaSeqCountTO = this.rawDataProcessedFilter.getDataTypes()
                .contains(DataType.RNA_SEQ) ? null: new RawDataCountContainerTO(null, null, null);

        return log.traceExit(new RawDataCountContainer(
                affyCountTO.getExperimentCount(), affyCountTO.getAssayCount(),
                affyCountTO.getCallsCount(), inSituCountTO.getExperimentCount(),
                inSituCountTO.getAssayCount(), inSituCountTO.getCallsCount(),
                estCountTO.getAssayCount(), estCountTO.getCallsCount(),
                bulkRnaSeqCountTO.getExperimentCount(), bulkRnaSeqCountTO.getAssayCount(),
                bulkRnaSeqCountTO.getCallsCount(), singleCellRnaSeqCountTO.getExperimentCount(),
                singleCellRnaSeqCountTO.getAssayCount(), singleCellRnaSeqCountTO.getRnaSeqLibraryCount(),
                singleCellRnaSeqCountTO.getCallsCount()));
    }

//*****************************************************************************************
//                     METHODS LOADING AFFYMETRIX RAW DATA
//*****************************************************************************************

    private AffyTempRawDataContainer loadAffymetrixData(InformationType infoType, int offset, int limit) {
        log.traceEntry("{}, {}, {}", infoType, offset, limit);
    
        LinkedHashSet<AffymetrixProbesetTO> affyProbesetTOs = new LinkedHashSet<>();
        LinkedHashSet<AffymetrixChipTO> affyChipTOs = new LinkedHashSet<>();
        LinkedHashMap<String, AffymetrixExperiment> expIdToAffyExp = new LinkedHashMap<>();

        Set<DAORawDataFilter> daoRawDataFilters = this.getRawDataProcessedFilter()
                .getDaoRawDataFilters();

        Set<Integer> bgeeChipIds = new HashSet<>();
        Set<Integer> bgeeGeneIds = new HashSet<>();
        if (infoType == InformationType.CALL) {
            AffymetrixProbesetTOResultSet probesetTORS = this.affymetrixProbesetDAO.getAffymetrixProbesets(
                    daoRawDataFilters, offset, limit, null);
            while (probesetTORS.next()) {
                AffymetrixProbesetTO probesetTO = probesetTORS.getTO();
                bgeeChipIds.add(probesetTO.getAssayId());
                bgeeGeneIds.add(probesetTO.getBgeeGeneId());
                affyProbesetTOs.add(probesetTO);
            }
        }

        AffymetrixChipTOResultSet chipTORS = null;
        Set<String> affyExpIds = new HashSet<>();
        Set<Integer> rawDataCondIds = new HashSet<>();
        //We need to write the test in this way, in case CALLs were requested, but there was
        //no result retrieved
        if (!bgeeChipIds.isEmpty()) {
            chipTORS = this.affymetrixChipDAO.getAffymetrixChipsFromBgeeChipIds(bgeeChipIds, null);
        } else if (infoType == InformationType.ASSAY) {
            chipTORS = this.affymetrixChipDAO.getAffymetrixChips(daoRawDataFilters, offset, limit, null);
        }
        if (chipTORS != null) {
            while (chipTORS.next()) {
                AffymetrixChipTO chipTO = chipTORS.getTO();
                affyExpIds.add(chipTO.getExperimentId());
                rawDataCondIds.add(chipTO.getConditionId());
                affyChipTOs.add(chipTO);
            }
        }

        MicroarrayExperimentTOResultSet expTORS = null;
        //Experiments should always be retrieved at this point if there is any result,
        //but we need this check in case there was no result returned when requesting
        //CALLs or ASSAYs.
        if (!affyExpIds.isEmpty()) {
            //we can use a new DAORawDataFilter to retrieve the requested experiments
            expTORS = this.microarrayExperimentDAO.getExperiments(
                    Set.of(new DAORawDataFilter(affyExpIds, null, null)), null, null, null);
        } else if (infoType == InformationType.EXPERIMENT) {
            //otherwise, it was the information requested originally
            expTORS = this.microarrayExperimentDAO.getExperiments(daoRawDataFilters, offset, limit, null);
        }
        if (expTORS != null) {
            //Now we create a map expId -> AffymetrixExperiment
            expIdToAffyExp = expTORS.stream()
                    .collect(Collectors.toMap(
                            to -> to.getId(),
                            to -> new AffymetrixExperiment(to.getId(), to.getName(),
                                    to.getDescription(), getSourceById(to.getDataSourceId())),
                            (v1, v2) -> {throw new IllegalStateException("No key collision possible");},
                            LinkedHashMap::new));
        }
    
        return log.traceExit(new AffyTempRawDataContainer(expIdToAffyExp, affyChipTOs, affyProbesetTOs,
                rawDataCondIds, bgeeGeneIds));
    }

    private RawDataContainer loadPartialAffymetrixRawDataContainer(
            AffyTempRawDataContainer tempRawDataContainer, InformationType infoType,
            Map<Integer, RawDataCondition> condMap, Map<Integer, Gene> geneMap) {
        log.traceEntry("{}, {}, {}, {}", tempRawDataContainer, infoType, condMap, geneMap);

        if (tempRawDataContainer == null) {
            return log.traceExit(new RawDataContainer(EnumSet.of(DataType.AFFYMETRIX),
                    null, null, null,
                    null, null, null, null,
                    null, null, null,
                    null, null));
        }

        //Experiments are always needed
        LinkedHashSet<AffymetrixExperiment> affymetrixExperiments =
                new LinkedHashSet<>(tempRawDataContainer.experimentMap.values());


        //Now we load the LinkedHashSets only if needed, to distinguish between
        //null value = info not requested, and empty Collection = no result
        LinkedHashSet<AffymetrixChip> affymetrixAssays = null;
        LinkedHashSet<AffymetrixProbeset> affymetrixCalls = null;
        if (infoType == InformationType.ASSAY || infoType == InformationType.CALL) {
            //We create a Map bgeeChipId -> AffymetrixChip for easier instantiation
            //of AffymetrixProbesets
            LinkedHashMap<Integer, AffymetrixChip> affyChipMap = tempRawDataContainer.assayTOs
                    .stream()
                    .collect(Collectors.toMap(
                            to -> to.getId(),

                            to -> new AffymetrixChip(
                                    to.getAffymetrixChipId(),
                                    Optional.ofNullable(tempRawDataContainer.experimentMap
                                            .get(to.getExperimentId()))
                                    .orElseThrow(() -> new IllegalStateException(
                                            "Missing experiment ID " + to.getExperimentId()
                                            + " for chip ID " + to.getAffymetrixChipId())), 
                                    new RawDataAnnotation(
                                            Optional.ofNullable(condMap.get(to.getConditionId()))
                                            .orElseThrow(() -> new IllegalStateException(
                                                    "Missing RawDataCondition ID "
                                                    + to.getConditionId()
                                                    + " for chip ID " + to.getAffymetrixChipId())),
                                            null, null, null)),

                            (v1, v2) -> {throw new IllegalStateException("No key collision possible");},
                            LinkedHashMap::new));
            affymetrixAssays = new LinkedHashSet<>(affyChipMap.values());

            if (infoType == InformationType.CALL) {
                affymetrixCalls = tempRawDataContainer.callTOs
                        .stream()
                        .map(to -> new AffymetrixProbeset(
                                to.getId(),
                                Optional.ofNullable(affyChipMap.get(to.getAssayId()))
                                .orElseThrow(() -> new IllegalStateException(
                                        "Missing chip ID " + to.getAssayId()
                                        + " for probeset ID " + to.getId())),
                                new RawCall(
                                        Optional.ofNullable(geneMap.get(to.getBgeeGeneId()))
                                        .orElseThrow(() -> new IllegalStateException(
                                                "Missing gene ID " + to.getBgeeGeneId()
                                                + " for probeset ID " + to.getId())),
                                        to.getPValue(),
                                        to.getExpressionConfidence(),
                                        ExclusionReason.convertToExclusionReason(
                                                to.getExclusionReason().getStringRepresentation())),
                                to.getNormalizedSignalIntensity(), to.getqValue(), to.getRank()))
                        .collect(Collectors.toCollection(LinkedHashSet::new));
            }
        }

        return log.traceExit(new RawDataContainer(EnumSet.of(DataType.AFFYMETRIX),
                affymetrixExperiments, affymetrixAssays, affymetrixCalls,
                null, null, null, null,
                null, null, null,
                null, null));
    }

//*****************************************************************************************
//                       METHODS NECESSARY FOR ALL DATA TYPES
//*****************************************************************************************

    private Map<Integer, RawDataCondition> loadCompleteRawDataConditionMap(Set<Integer> condIds) {
        log.traceEntry("{}", condIds);

        Map<Integer, RawDataCondition> requestedRawDataConditionMap =
                this.getRawDataProcessedFilter().getRequestedRawDataConditionMap();
        Map<Integer, Species> speciesMap = this.getRawDataProcessedFilter().getSpeciesMap();

        Set<Integer> missingCondIds = new HashSet<>(condIds);
        missingCondIds.removeAll(requestedRawDataConditionMap.keySet());
        if (missingCondIds.isEmpty()) {
            return log.traceExit(requestedRawDataConditionMap);
        }
        Map<Integer, RawDataCondition> missingCondMap = loadRawDataConditionMapFromResultSet(
                        (attrs) -> this.rawDataConditionDAO.getRawDataConditionsFromIds(missingCondIds, attrs),
                        null, speciesMap.values(), anatEntityService, devStageService);
        missingCondMap.putAll(requestedRawDataConditionMap);

        return log.traceExit(missingCondMap);
    }
    private Map<Integer, Gene> loadCompleteGeneMap(Set<Integer> bgeeGeneIds) {
        log.traceEntry("{}", bgeeGeneIds);

        Map<Integer, Gene> requestedGeneMap = this.getRawDataProcessedFilter()
                .getRequestedGeneMap();
        Map<Integer, Species> speciesMap = this.getRawDataProcessedFilter()
                .getSpeciesMap();
        Map<Integer, GeneBioType> geneBioTypeMap = this.getRawDataProcessedFilter()
                .getGeneBioTypeMap();

        Set<Integer> missingGeneIds = new HashSet<>(bgeeGeneIds);
        missingGeneIds.removeAll(requestedGeneMap.keySet());
        if (missingGeneIds.isEmpty()) {
            return log.traceExit(requestedGeneMap);
        }
        Map<Integer, Gene> missingGeneMap = this.geneDAO.getGenesByBgeeIds(missingGeneIds).stream()
                .collect(Collectors.toMap(gTO -> gTO.getId(), gTO -> mapGeneTOToGene(gTO,
                        Optional.ofNullable(speciesMap.get(gTO.getSpeciesId()))
                        .orElseThrow(() -> new IllegalStateException("Missing species ID for gene")),
                        null, null,
                        Optional.ofNullable(geneBioTypeMap.get(gTO.getGeneBioTypeId()))
                        .orElseThrow(() -> new IllegalStateException("Missing gene biotype ID for gene")))));
        missingGeneMap.putAll(requestedGeneMap);

        return log.traceExit(missingGeneMap);
    }

    private Source getSourceById(Integer sourceId) {
        log.traceEntry("{}", sourceId);
        if (sourceId == null) {
            return log.traceExit((Source) null);
        }
        Source source = this.getRawDataProcessedFilter().getSourceMap().get(sourceId);
        if (source == null) {
            throw log.throwing(new IllegalStateException("No Source found corresponding to ID " + sourceId
                    + " - original sourceMap: " + this.getRawDataProcessedFilter().getSourceMap()));
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
     * Pre-processed information based on the {@code RawDataFilter} used to obtain
     * this {@code RawDataLoader}. After obtaining a {@code RawDataLoader} by calling
     * {@link RawDataService#loadRawDataLoader(RawDataFilter)}, it will be faster,
     * to obtain other {@code RawDataLoader}s for the same parameters, to call the method
     * {@link RawDataService#getRawDataLoader(RawDataProcessedFilter)} instead.
     * See {@link RawDataService#getRawDataLoader(RawDataProcessedFilter)}
     * for more details.
     *
     * @return  The {@code RawDataProcessedFilter} storing information pre-processed
     *          based on the provided {@code RawDataFilter} (see {@link
     *          RawDataProcessedFilter#getRawDataFilter()}).
     * @see RawDataProcessedFilter#getRawDataFilter()
     * @see RawDataService#getRawDataLoader(RawDataProcessedFilter)
     * @see RawDataService#loadRawDataLoader(RawDataFilter)
     */
    public RawDataProcessedFilter getRawDataProcessedFilter() {
        return this.rawDataProcessedFilter;
    }
}