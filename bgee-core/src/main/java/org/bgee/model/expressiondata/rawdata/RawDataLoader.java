package org.bgee.model.expressiondata.rawdata;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeEnum;
import org.bgee.model.BgeeEnum.BgeeEnumField;
import org.bgee.model.CommonService;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.DevStageService;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCountDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO.RawDataConditionTOResultSet;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCountDAO.RawDataCountContainerTO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO.AffymetrixChipTO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO.AffymetrixChipTOResultSet;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixProbesetDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixProbesetDAO.AffymetrixProbesetTO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixProbesetDAO.AffymetrixProbesetTOResultSet;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.MicroarrayExperimentDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.MicroarrayExperimentDAO.MicroarrayExperimentTOResultSet;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqExperimentDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqExperimentDAO.RNASeqExperimentTOResultSet;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryAnnotatedSampleDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryAnnotatedSampleDAO.RNASeqLibraryAnnotatedSampleTO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryAnnotatedSampleDAO.RNASeqLibraryAnnotatedSampleTOResultSet;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryDAO.RNASeqLibraryTO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryDAO.RNASeqLibraryTOResultSet;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqResultAnnotatedSampleDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqResultAnnotatedSampleDAO.RNASeqResultAnnotatedSampleTO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqResultAnnotatedSampleDAO.RNASeqResultAnnotatedSampleTOResultSet;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.rawdata.baseelements.CellCompartment;
import org.bgee.model.expressiondata.rawdata.baseelements.RawCall;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataAnnotation;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataCondition;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataContainer;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataCountContainer;
import org.bgee.model.expressiondata.rawdata.baseelements.RawCall.ExclusionReason;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataCondition.RawDataSex;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataDataType;
import org.bgee.model.expressiondata.rawdata.baseelements.SequencedTranscriptPart;
import org.bgee.model.expressiondata.rawdata.baseelements.Strand;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixChip;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixChipPipelineSummary;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixExperiment;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixProbeset;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqContainer;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqExperiment;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqLibrary;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqLibraryAnnotatedSample;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqLibraryPipelineSummary;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqResultAnnotatedSample;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqTechnology;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqResultAnnotatedSample.AbundanceUnit;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixContainer;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixCountContainer;
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
 * <p>
 * This class also stores the {@code RawDataCondition}s and {@code Gene}s retrieved
 * over several independent calls to this {@code RawDataLoader}, in order to save resources
 * and not query them multiple times.
 * <p>
 * Note that this class is not thread-safe. And anyway, it would not make much sense to have
 * several threads to access the same {@code RawDataLoader}, since there would be only
 * one connection to the data source for all the calls. Each thread should use their own
 * {@code RawDataLoader}.
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
     * An {@code int} that is the maximum allowed number of results
     * to retrieve in one method call, for each requested data type independently.
     * Value: 10,000.
     * <p>
     * Note that this limit should remain above the max. number of assays in an experiment,
     * to be able to retrieve at once all assays part of one specific experiment
     * (specified through {@link RawDataFilter#getExperimentIds()}).
     */
    public static int LIMIT_MAX = 10000;
    /**
     * An {@code int} that is the maximum number of elements
     * in {@link #rawDataConditionMap} and {@link #geneMap} before starting
     * to flushing some existing entries. It is not a <strong>guarantee</strong>
     * that those {@code Map}s will never exceed that size, just a trigger
     * to flushing entries as much as possible.
     *
     * @see #updateRawDataConditionMap(Set)
     * @see #updateGeneMap(Set)
     */
    private static final int MAX_ELEMENTS_IN_MAP = 10000;


    /**
     * @see #getRawDataProcessedFilter()
     */
    private final RawDataProcessedFilter rawDataProcessedFilter;

    //DAOs and Services used by this class
    private final MicroarrayExperimentDAO microarrayExperimentDAO;
    private final AffymetrixChipDAO affymetrixChipDAO;
    private final AffymetrixProbesetDAO affymetrixProbesetDAO;
    private final RNASeqExperimentDAO rnaSeqExperimentDAO;
    private final RNASeqLibraryDAO rnaSeqLibraryDAO;
    private final RNASeqLibraryAnnotatedSampleDAO rnaSeqAssayDAO;
    private final RNASeqResultAnnotatedSampleDAO rnaSeqCallDAO;
    private final RawDataConditionDAO rawDataConditionDAO;
    private final RawDataCountDAO rawDataCountDAO;
    private final GeneDAO geneDAO;
    private final AnatEntityService anatEntityService;
    private final DevStageService devStageService;

    //These attributes are mutable, it is acceptable for a Service.
    //We keep the speciesMap and geneBiotypeMap inside the rawDataProcessedFilter,
    //as there will be no update to them by this RawDataLoader.
    /**
     * A {@code Map} where keys are {@code Integer}s that are internal IDs of raw data conditions,
     * the value being the associated {@code RawDataCondition}. this {@code Map} is used
     * to store the retrieved {@code RawDataCondition}s over several independent calls
     * to this {@code RawDataLoader}, in order to avoid querying multiple times for the same
     * conditions.
     *
     * @see #MAX_ELEMENTS_IN_MAP
     * @see #updateRawDataConditionMap(Set)
     */
    private final Map<Integer, RawDataCondition> rawDataConditionMap;
    /**
     * A {@code Map} where keys are {@code Integer}s that are internal IDs of genes,
     * the value being the associated {@code Gene}. this {@code Map} is used
     * to store the retrieved {@code Gene}s over several independent calls
     * to this {@code RawDataLoader}, in order to avoid querying multiple times for the same
     * conditions.
     *
     * @see #MAX_ELEMENTS_IN_MAP
     * @see #updateGeneMap(Set)
     */
    private final Map<Integer, Gene> geneMap;

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
        this.rnaSeqExperimentDAO     = this.getDaoManager().getRnaSeqExperimentDAO();
        this.rnaSeqLibraryDAO        = this.getDaoManager().getRnaSeqLibraryDAO();
        this.rnaSeqAssayDAO          = this.getDaoManager().getRnaSeqLibraryAnnotatedSampleDAO();
        this.rnaSeqCallDAO           = this.getDaoManager().getRnaSeqResultAnnotatedSampleDAO();
        this.rawDataConditionDAO     = this.getDaoManager().getRawDataConditionDAO();
        this.geneDAO                 = this.getDaoManager().getGeneDAO();
        this.anatEntityService       = this.getServiceFactory().getAnatEntityService();
        this.devStageService         = this.getServiceFactory().getDevStageService();
        this.rawDataCountDAO         = this.getDaoManager().getRawDataCountDAO();

        this.rawDataConditionMap = new HashMap<>();
        this.geneMap = new HashMap<>();
        //Seed the Maps with any condition or gene already identified
        //from the processed filter.
        //We keep the speciesMap and geneBiotypeMap inside the rawDataProcessedFilter,
        //as there will be no update to them by this RawDataLoader.
        this.rawDataConditionMap.putAll(this.rawDataProcessedFilter.getRequestedRawDataConditionMap());
        this.geneMap.putAll(this.rawDataProcessedFilter.getRequestedGeneMap());
    }

    /**
     * Load raw data of the specified {@code InformationType} and {@code RawDataDataType}.
     *
     * @param <T>               The type of {@code RawDataContainer} returned, dependent on
     *                          the {@code RawDataDataType} requested.
     * @param infoType          The {@code InformationType} to load.
     * @param rawDataDataType   A {@code RawDataDataType} for which to retrieve
     *                          {@code InformationType}.
     * @param offset            An {@code Integer} specifying at which index to start getting results
     *                          of the type {@code infoType}. If {@code null}, equivalent to {@code 0}
     *                          (first index).
     * @param limit             An {@code Integer} specifying the number of results of type
     *                          {@code infoType} to retrieve. Cannot be greater than {@link #LIMIT_MAX}.
     *                          If {@code null}, equivalent to {@link #LIMIT_MAX}.
     * @return                  A {@code RawDataContainer} containing the requested results.
     * @throws IllegalArgumentException If {@code infoType} is null,
     *                                  or {@code offset} is non-null and less than 0,
     *                                  or {@code limit} is non-null and less than or equal to 0,
     *                                  or greater than {@link #LIMIT_MAX}.
     */
    public <T extends RawDataContainer<?, ?>> T loadData(InformationType infoType,
            RawDataDataType<T, ?> rawDataDataType, Integer offset, Integer limit)
                    throws IllegalArgumentException {
        log.traceEntry("{}, {}, {}, {}", infoType, rawDataDataType, offset, limit);
        if (infoType == null) {
            throw log.throwing(new IllegalArgumentException("An InformationType must be provided"));
        }
        if (rawDataDataType == null) {
            throw log.throwing(new IllegalArgumentException("A RawDataDataType must be provided"));
        }
        if (offset != null && offset < 0) {
            throw log.throwing(new IllegalArgumentException("offset cannot be less than 0"));
        }
        if (limit != null && limit <= 0) {
            throw log.throwing(new IllegalArgumentException(
                    "limit cannot be less than or equal to 0"));
        }
        if (limit != null && limit > LIMIT_MAX) {
            throw log.throwing(new IllegalArgumentException("limit cannot be greater than "
                    + LIMIT_MAX));
        }
        int newOffset = offset == null? 0: offset;
        int newLimit = limit == null? LIMIT_MAX: limit;

        DataType requestedDataType = rawDataDataType.getDataType();
        Class<T> rawDataContainerClass = rawDataDataType.getRawDataContainerClass();
        T rawDataContainer = null;
        switch (requestedDataType) {
        case AFFYMETRIX:
            rawDataContainer = rawDataContainerClass.cast(
                    this.loadAffymetrixData(infoType, newOffset, newLimit));
            break;
        case RNA_SEQ:
        case FULL_LENGTH:
            rawDataContainer = rawDataContainerClass.cast(
                    this.loadRnaSeqData(infoType,
                            requestedDataType.equals(DataType.FULL_LENGTH)? true: false,
                            newOffset, newLimit));
            break;
        default:
            //TODO: reenable the exception when all data types supported
            //throw log.throwing(new IllegalStateException("Unsupported data type: " + requestedDataType));
        }

        return log.traceExit(rawDataContainer);
    }

    public <T extends RawDataCountContainer> T loadDataCount(Collection<InformationType> infoTypes,
            RawDataDataType<?, T> rawDataDataType) {
        log.traceEntry("{}, {}", infoTypes, rawDataDataType);

        EnumSet<InformationType> requestedInfoTypes = infoTypes == null || infoTypes.isEmpty()?
                EnumSet.allOf(InformationType.class): EnumSet.copyOf(infoTypes);
        if (rawDataDataType == null) {
            throw log.throwing(new IllegalArgumentException("A RawDataDataType must be provided"));
        }

        // create booleans used to query the DAO
        boolean withExperiment = requestedInfoTypes.contains(InformationType.EXPERIMENT);
        boolean withAssay = requestedInfoTypes.contains(InformationType.ASSAY);
        boolean withCall = requestedInfoTypes.contains(InformationType.CALL);

        DataType requestedDataType = rawDataDataType.getDataType();
        Class<T> rawDataCountContainerClass = rawDataDataType.getRawDataCountContainerClass();
        T rawDataCountContainer = null;

        switch (requestedDataType) {
        case AFFYMETRIX:
            rawDataCountContainer = rawDataCountContainerClass.cast(
                    this.loadAffymetrixCount(withExperiment, withAssay, withCall));
            break;
        default:
            //TODO: reenable the exception when all data types supported
            //throw log.throwing(new IllegalStateException("Unsupported data type: " + requestedDataType));
        }

        return log.traceExit(rawDataCountContainer);
    }

    /**
     * Load anatomical entities, dev. stages, cell types, sexes and strains for the specified
     * data type using the condition IDs of the {@code DAORawDataFilter}s.
     * 
     * @param dataType  A {@code DataType} corresponding to the datatype for which
     *                  post filters have to be loaded
     * @return          A {@code RawDataPostFilter} containing condition parameters to filter on.
     */
    //We also use a RawDataDataType, in case we create data-type-specific PostFilters
    //in the future. In that case, we could add a third generic type parameter to RawDataDataType,
    //specifying the type of RawDataPostFilter to return.
    public RawDataPostFilter loadPostFilter(RawDataDataType<?, ?> rawDataDataType) {
        log.traceEntry("{}", rawDataDataType);
        if (rawDataDataType == null) {
            throw log.throwing(new IllegalArgumentException("dataType can not be null"));
        }
        DataType requestedDataType = rawDataDataType.getDataType();
        switch (requestedDataType) {
        case AFFYMETRIX:
            return log.traceExit(this.loadAffymetrixPostFilter());
        default:
            //TODO: reenable the exception when all data types supported and remove the return null
            //throw log.throwing(new IllegalStateException("Unsupported data type: " + requestedDataType));
            return null;
        }
    }

//*****************************************************************************************
//                       METHODS LOADING AFFYMETRIX RAW DATA
//*****************************************************************************************

    private AffymetrixContainer loadAffymetrixData(InformationType infoType, int offset, int limit) {
        log.traceEntry("{}, {}, {}", infoType, offset, limit);

        //If the DaoRawDataFilters are null it means there was no matching conds
        //and thus no result for sure
        if (this.getRawDataProcessedFilter().getDaoRawDataFilters() == null) {
            return log.traceExit(this.getNoResultAffymetrixContainer(infoType));
        }

        //************************************************************
        // First, we retrieve all necessary TransferObjects
        //************************************************************
        LinkedHashSet<AffymetrixProbesetTO> affyProbesetTOs = new LinkedHashSet<>();
        LinkedHashSet<AffymetrixChipTO> affyChipTOs = new LinkedHashSet<>();
        Set<Integer> bgeeChipIds = new HashSet<>();
        Set<Integer> bgeeGeneIds = new HashSet<>();
        Set<DAORawDataFilter> daoRawDataFilters = this.getRawDataProcessedFilter()
                .getDaoRawDataFilters();

        //*********** Calls ***********
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

        //*********** Assays ***********
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

        //*********** Experiments ***********
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
        LinkedHashMap<String, AffymetrixExperiment> expIdToAffyExp =
                expTORS == null? new LinkedHashMap<>():
                    expTORS.stream()
                    .collect(Collectors.toMap(
                            to -> to.getId(),
                            to -> new AffymetrixExperiment(to.getId(), to.getName(),
                                    to.getDescription(), getSourceById(to.getDataSourceId()), 0),
                            (v1, v2) -> {throw new IllegalStateException("No key collision possible");},
                            LinkedHashMap::new));

        //************************************************************
        // Now, we load missing Genes and RawDataConditions
        //************************************************************
        this.updateRawDataConditionMap(rawDataCondIds);
        this.updateGeneMap(bgeeGeneIds);


        //************************************************************
        // Finally, we instantiate all bgee-core objects necessary
        //************************************************************
        //Experiments are always needed
        LinkedHashSet<AffymetrixExperiment> affymetrixExperiments =
                new LinkedHashSet<>(expIdToAffyExp.values());

        //Now we load the LinkedHashSets only if needed, to distinguish between
        //null value = info not requested, and empty Collection = no result
        LinkedHashSet<AffymetrixChip> affymetrixAssays = null;
        LinkedHashSet<AffymetrixProbeset> affymetrixCalls = null;
        if (infoType == InformationType.ASSAY || infoType == InformationType.CALL) {
            //We create a Map bgeeChipId -> AffymetrixChip for easier instantiation
            //of AffymetrixProbesets
            LinkedHashMap<Integer, AffymetrixChip> affyChipMap = affyChipTOs
                    .stream()
                    .collect(Collectors.toMap(
                            to -> to.getId(),

                            to -> new AffymetrixChip(
                                    to.getAffymetrixChipId(),
                                    Optional.ofNullable(expIdToAffyExp.get(to.getExperimentId()))
                                    .orElseThrow(() -> new IllegalStateException(
                                            "Missing experiment ID " + to.getExperimentId()
                                            + " for chip ID " + to.getAffymetrixChipId())), 
                                    new RawDataAnnotation(
                                            Optional.ofNullable(
                                                    this.rawDataConditionMap.get(to.getConditionId()))
                                            .orElseThrow(() -> new IllegalStateException(
                                                    "Missing RawDataCondition ID "
                                                    + to.getConditionId()
                                                    + " for chip ID " + to.getAffymetrixChipId())),
                                            null, null, null),
                                    null,
                                    new AffymetrixChipPipelineSummary(
                                            to.getDistinctRankCount(), to.getMaxRank(), to.getScanDate(),
                                            to.getNormalizationType().getStringRepresentation(),
                                            to.getQualityScore(), to.getPercentPresent())),

                            (v1, v2) -> {throw new IllegalStateException("No key collision possible");},
                            LinkedHashMap::new));
            affymetrixAssays = new LinkedHashSet<>(affyChipMap.values());

            if (infoType == InformationType.CALL) {
                affymetrixCalls = affyProbesetTOs
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
                                                to.getExclusionReason().name())),
                                to.getNormalizedSignalIntensity(), to.getqValue(), to.getRank()))
                        .collect(Collectors.toCollection(LinkedHashSet::new));
            }
        }

        return log.traceExit(new AffymetrixContainer(
                affymetrixExperiments, affymetrixAssays, affymetrixCalls));
    }
    private AffymetrixContainer getNoResultAffymetrixContainer(InformationType infoType) {
        log.traceEntry("{}", infoType);

        return log.traceExit(new AffymetrixContainer(
                //Experiments always end up being requested
                Set.of(),
                //We also get the chips when we request the probesets
                infoType == InformationType.CALL || infoType == InformationType.ASSAY? Set.of(): null,
                infoType == InformationType.CALL? Set.of(): null));
    }

    private AffymetrixCountContainer loadAffymetrixCount(boolean withExperiment,
            boolean withAssay, boolean withCall) {
        log.traceEntry("{}, {}, {}", withExperiment, withAssay, withCall);

        //If the DaoRawDataFilters are null it means there was no matching conds
        //and thus no result for sure
        if (this.getRawDataProcessedFilter().getDaoRawDataFilters() == null) {
            return log.traceExit(new AffymetrixCountContainer(
                    withExperiment? 0: null,
                    withAssay? 0: null,
                    withCall? 0: null));
        }

        RawDataFilter filter = this.getRawDataProcessedFilter().getRawDataFilter();
        //If we don't need any filtering on assay information,
        boolean noNeedChipInfo = filter.getAssayIds().isEmpty() &&
                filter.getExperimentIds().isEmpty() &&
                filter.getExperimentOrAssayIds().isEmpty() &&
                filter.getConditionFilters().stream().allMatch(cf -> cf.areAllCondParamFiltersEmpty());
        //and we don't need filtering on probeset information for counting assays or experiments,
        boolean noProbesetFilteringForAssayExpCount = !withExperiment && !withAssay ||
                filter.getGeneFilters().stream().allMatch(gf -> gf.getGeneIds().isEmpty());
        //then, if requested, we count the probesets in a separate query for faster results.
        //FIXME FB: actually, it should be the DAO doing this logic
        RawDataCountContainerTO probesetCountTO = null;
        boolean updatedWithCall = withCall;
        if (withCall && noNeedChipInfo && noProbesetFilteringForAssayExpCount) {
            probesetCountTO = rawDataCountDAO.getAffymetrixCount(
                    this.getRawDataProcessedFilter().getDaoRawDataFilters(),
                    false, false, true);
            updatedWithCall = false;
        }
        //Now we do the "requested" call if we don't have the counts we need yet
        RawDataCountContainerTO affyRemainingCountTO = null;
        if (updatedWithCall || withExperiment || withAssay) {
            affyRemainingCountTO = rawDataCountDAO.getAffymetrixCount(
                    this.getRawDataProcessedFilter().getDaoRawDataFilters(),
                    withExperiment, withAssay, updatedWithCall);
        }
        assert probesetCountTO != null || affyRemainingCountTO != null;
        assert probesetCountTO == null || probesetCountTO.getCallCount() != null;

        return log.traceExit(new AffymetrixCountContainer(
                //experiment count
                affyRemainingCountTO != null? affyRemainingCountTO.getExperimentCount(): null,
                //assay count
                affyRemainingCountTO != null? affyRemainingCountTO.getAssayCount(): null,
                //call count
                probesetCountTO != null? probesetCountTO.getCallCount():
                    affyRemainingCountTO.getCallCount()));
    }

    private RawDataPostFilter loadAffymetrixPostFilter() {
        log.traceEntry();
        return log.traceExit(this.loadConditionPostFilter(
                (filters, attrs) -> this.rawDataConditionDAO
                .getAffymetrixRawDataConditionsFromRawDataFilters(filters, attrs),
                DataType.AFFYMETRIX));
    }

//*****************************************************************************************
//                         METHODS LOADING RNA-SEQ RAW DATA
//*****************************************************************************************

    private RnaSeqContainer loadRnaSeqData(InformationType infoType, boolean isSingleCell,
            int offset, int limit) {
        log.traceEntry("{}, {}, {}, {}", infoType, isSingleCell, offset, limit);

        //If the DaoRawDataFilters are null it means there was no matching conds
        //and thus no result for sure
        if (this.getRawDataProcessedFilter().getDaoRawDataFilters() == null) {
            return log.traceExit(this.getNoResultRnaSeqContainer(infoType));
        }

        //************************************************************
        // First, we retrieve all necessary TransferObjects
        //************************************************************
        LinkedHashSet<RNASeqResultAnnotatedSampleTO> callTOs = new LinkedHashSet<>();
        LinkedHashSet<RNASeqLibraryAnnotatedSampleTO> assayTOs = new LinkedHashSet<>();
        LinkedHashSet<RNASeqLibraryTO> libTOs = new LinkedHashSet<>();
        Set<DAORawDataFilter> daoRawDataFilters = this.getRawDataProcessedFilter()
                .getDaoRawDataFilters();

        //*********** Calls ***********
        Set<Integer> bgeeAnnotatedSampleIds = new HashSet<>();
        Set<Integer> bgeeGeneIds = new HashSet<>();
        if (infoType == InformationType.CALL) {
            RNASeqResultAnnotatedSampleTOResultSet callTORS = this.rnaSeqCallDAO.getResultAnnotatedSamples(
                    daoRawDataFilters, isSingleCell, offset, limit, null);
            while (callTORS.next()) {
                RNASeqResultAnnotatedSampleTO callTO = callTORS.getTO();
                bgeeAnnotatedSampleIds.add(callTO.getAssayId());
                bgeeGeneIds.add(callTO.getBgeeGeneId());
                callTOs.add(callTO);
            }
        }

        //*********** Assays ***********
        RNASeqLibraryAnnotatedSampleTOResultSet assayTORS = null;
        //We need to write the test in this way, in case CALLs were requested, but there was
        //no result retrieved
        if (!bgeeAnnotatedSampleIds.isEmpty()) {
            assayTORS = this.rnaSeqAssayDAO.getLibraryAnnotatedSamplesFromLibraryAnnotatedSampleIds(
                    bgeeAnnotatedSampleIds, null);
        } else if (infoType == InformationType.ASSAY) {
            assayTORS = this.rnaSeqAssayDAO.getLibraryAnnotatedSamples(daoRawDataFilters,
                    isSingleCell, offset, limit, null);
        }
        Set<String> libraryIds = new HashSet<>();
        Set<Integer> rawDataCondIds = new HashSet<>();
        if (assayTORS != null) {
            while (assayTORS.next()) {
                RNASeqLibraryAnnotatedSampleTO assayTO = assayTORS.getTO();
                libraryIds.add(assayTO.getLibraryId());
                rawDataCondIds.add(assayTO.getConditionId());
                assayTOs.add(assayTO);
            }
        }

        //*********** Libraries ***********
        Set<String> expIds = new HashSet<>();
        if (!libraryIds.isEmpty()) {
            //we can use a new DAORawDataFilter to retrieve the requested libraries
            DAORawDataFilter libFilter = new DAORawDataFilter(null, libraryIds, null);
            RNASeqLibraryTOResultSet libTORS = this.rnaSeqLibraryDAO.getRnaSeqLibrary(
                    Collections.singleton(libFilter), null, null, null);

            while (libTORS.next()) {
                RNASeqLibraryTO libTO = libTORS.getTO();
                expIds.add(libTO.getExperimentId());
                libTOs.add(libTO);
            }
        }

        //*********** Experiments ***********
        RNASeqExperimentTOResultSet expTORS = null;
        //Experiments should always be retrieved at this point if there is any result,
        //but we need this check in case there was no result returned when requesting
        //CALLs or ASSAYs.
        if (!expIds.isEmpty()) {
            //we can use a new DAORawDataFilter to retrieve the requested experiments
            DAORawDataFilter expFilter = new DAORawDataFilter(expIds, null, null);
            expTORS = this.rnaSeqExperimentDAO.getExperiments(
                    Collections.singleton(expFilter), null, null, null);
        } else if (infoType == InformationType.EXPERIMENT) {
            //otherwise, it was the information requested originally
            expTORS = this.rnaSeqExperimentDAO.getExperiments(daoRawDataFilters, isSingleCell,
                    offset, limit, null);
        }
        LinkedHashMap<String, RnaSeqExperiment> expIdToExp =
                expTORS == null? new LinkedHashMap<>():
                    expTORS.stream()
                    .collect(Collectors.toMap(
                            to -> to.getId(),
                            to -> new RnaSeqExperiment(to.getId(), to.getName(),
                                  to.getDescription(), getSourceById(to.getDataSourceId()), 0),
                            (v1, v2) -> {throw new IllegalStateException("No key collision possible");},
                            LinkedHashMap::new));

        //************************************************************
        // Now, we load missing Genes and RawDataConditions
        //************************************************************
        this.updateRawDataConditionMap(rawDataCondIds);
        this.updateGeneMap(bgeeGeneIds);


        //************************************************************
        // Finally, we instantiate all bgee-core objects necessary
        //************************************************************
        //Experiments are always needed
        LinkedHashSet<RnaSeqExperiment> experiments =
                new LinkedHashSet<>(expIdToExp.values());

        //Now we load the LinkedHashSets only if needed, to distinguish between
        //null value = info not requested, and empty Collection = no result
        LinkedHashSet<RnaSeqLibraryAnnotatedSample> assays = null;
        LinkedHashSet<RnaSeqLibrary> libs = null;
        LinkedHashSet<RnaSeqResultAnnotatedSample> calls = null;
        if (infoType == InformationType.ASSAY || infoType == InformationType.CALL) {
            //We create a Map libId -> lib for easier instantiation
            //of the assays
            LinkedHashMap<String, RnaSeqLibrary> libMap = libTOs
                    .stream()
                    .collect(Collectors.toMap(
                            to -> to.getId(),

                            to -> new RnaSeqLibrary(
                                    to.getId(),

                                    new RnaSeqTechnology(
                                            to.getTechnologyName(),
                                            to.getSequencerName(),
                                            Strand.convertToStrand(to.getStrandSelection().name()),
                                            SequencedTranscriptPart.convertToSequencedTranscriptPart(
                                                    to.getSequencedTranscriptPart().name()),
                                            CellCompartment.convertToCellCompartment(
                                                    to.getCellCompartment().name()),
                                            to.isSampleMultiplexing(),
                                            to.isLibraryMultiplexing(),
                                            to.getFragmentation(),
                                            to.getLibraryType().getStringRepresentation()),

                                    Optional.ofNullable(expIdToExp.get(to.getExperimentId()))
                                    .orElseThrow(() -> new IllegalStateException(
                                            "Missing experiment ID " + to.getExperimentId()
                                            + " for lib ID " + to.getId()))),

                            (v1, v2) -> {throw new IllegalStateException("No key collision possible");},
                            LinkedHashMap::new));
            libs = new LinkedHashSet<>(libMap.values());

            //We create a Map assayId -> assay for easier instantiation
            //of calls
            LinkedHashMap<Integer, RnaSeqLibraryAnnotatedSample> assayMap = assayTOs
                    .stream()
                    .collect(Collectors.toMap(
                            to -> to.getId(),

                            to -> new RnaSeqLibraryAnnotatedSample(
                                    Optional.ofNullable(libMap.get(to.getLibraryId()))
                                    .orElseThrow(() -> new IllegalStateException(
                                            "Missing library ID " + to.getLibraryId()
                                            + " for annotated sample ID " + to.getId())),
                                    new RawDataAnnotation(
                                            Optional.ofNullable(
                                                    this.rawDataConditionMap.get(
                                                            to.getConditionId()))
                                            .orElseThrow(() -> new IllegalStateException(
                                                    "Missing RawDataCondition ID "
                                                    + to.getConditionId()
                                                    + " for annotated sample ID " + to.getId())),
                                            null, null, null),
                                    new RnaSeqLibraryPipelineSummary(
                                            to.getMeanAbundanceRefIntergenicDistribution(),
                                            to.getSdAbundanceRefIntergenicDistribution(),
                                            to.getpValueThreshold(),
                                            to.getAllReadCount(),
                                            to.getAllUMIsCount(),
                                            to.getMappedReadCount(),
                                            to.getMappedUMIsCount(),
                                            to.getMinReadLength(),
                                            to.getMaxReadLength(),
                                            to.getMaxRank(),
                                            to.getDistinctRankCount()),
                                    to.getBarcode(),
                                    to.getGenotype()),

                            (v1, v2) -> {throw new IllegalStateException("No key collision possible");},
                            LinkedHashMap::new));
            assays = new LinkedHashSet<>(assayMap.values());

            if (infoType == InformationType.CALL) {
                calls = callTOs
                        .stream()
                        .map(to -> new RnaSeqResultAnnotatedSample(
                                Optional.ofNullable(assayMap.get(to.getAssayId()))
                                .orElseThrow(() -> new IllegalStateException(
                                        "Missing assay ID " + to.getAssayId()
                                        + " for Bgee gene ID " + to.getBgeeGeneId())),
                                new RawCall(
                                        Optional.ofNullable(this.geneMap.get(to.getBgeeGeneId()))
                                        .orElseThrow(() -> new IllegalStateException(
                                                "Missing gene ID " + to.getBgeeGeneId()
                                                + " for assay ID " + to.getAssayId())),
                                        to.getPValue(),
                                        to.getExpressionConfidence(),
                                        ExclusionReason.convertToExclusionReason(
                                                to.getExclusionReason().name())),
                                AbundanceUnit.convertToAbundanceUnit(
                                        to.getAbundanceUnit().name()),
                                to.getAbundance(),
                                to.getRank(),
                                to.getReadCount(),
                                to.getUmiCount(),
                                to.getzScore()))
                        .collect(Collectors.toCollection(LinkedHashSet::new));
            }
        }

        return log.traceExit(new RnaSeqContainer(experiments, libs, assays, calls));
    }
    private RnaSeqContainer getNoResultRnaSeqContainer(InformationType infoType) {
        log.traceEntry("{}", infoType);

        return log.traceExit(new RnaSeqContainer(
                //Experiments always end up being requested
                Set.of(),
                //We also get the libraries and the AnnotatedSamples when we request the calls
                infoType == InformationType.CALL || infoType == InformationType.ASSAY? Set.of(): null,
                infoType == InformationType.CALL || infoType == InformationType.ASSAY? Set.of(): null,
                infoType == InformationType.CALL? Set.of(): null));
    }

//*****************************************************************************************
//                       METHODS NECESSARY FOR ALL DATA TYPES
//*****************************************************************************************

    private RawDataPostFilter loadConditionPostFilter(BiFunction<Collection<DAORawDataFilter>,
            Collection<RawDataConditionDAO.Attribute>, RawDataConditionTOResultSet> condRequest,
            DataType dataType) {
        log.traceEntry("{}, {}", condRequest, dataType);

        // retrieve anatEntities
        Set<String> anatEntityIds = condRequest.apply(this.getRawDataProcessedFilter()
        .getDaoRawDataFilters(), Set.of(RawDataConditionDAO.Attribute.ANAT_ENTITY_ID)).stream()
        .map(a -> a.getAnatEntityId()).collect(Collectors.toSet());
        Set<AnatEntity> anatEntities = anatEntityIds.isEmpty()?
                new HashSet<>() : anatEntityService.loadAnatEntities(anatEntityIds, false)
                .collect(Collectors.toSet());

        // retrieve cellTypes
        Set<String> cellTypeIds = condRequest.apply(this.getRawDataProcessedFilter()
                        .getDaoRawDataFilters(), Set.of(RawDataConditionDAO.Attribute.CELL_TYPE_ID))
                .stream().map(c -> c.getCellTypeId()).collect(Collectors.toSet());
        Set<AnatEntity> cellTypes = cellTypeIds.isEmpty()?
                new HashSet<>() : anatEntityService.loadAnatEntities(cellTypeIds, false)
                .collect(Collectors.toSet());

        //retrieve dev. stages
        Set<String> stageIds = condRequest.apply(this.getRawDataProcessedFilter()
                        .getDaoRawDataFilters(), Set.of(RawDataConditionDAO.Attribute.STAGE_ID))
                .stream().map(c -> c.getStageId()).collect(Collectors.toSet());
        Set<DevStage> stages = stageIds.isEmpty()?
                new HashSet<>() : devStageService.loadDevStages(null, null, stageIds, false)
                .collect(Collectors.toSet());

        // retrieve strains
        Set<String> strains = condRequest.apply(this.getRawDataProcessedFilter()
                        .getDaoRawDataFilters(), Set.of(RawDataConditionDAO.Attribute.STRAIN))
                .stream().map(c -> c.getStrainId()).collect(Collectors.toSet());

        //retrieve sexes
        Set<RawDataSex> sexes = condRequest.apply(this.getRawDataProcessedFilter()
                        .getDaoRawDataFilters(), Set.of(RawDataConditionDAO.Attribute.SEX)).stream()
                .map(c -> mapDAORawDataSexToRawDataSex(c.getSex())).collect(Collectors.toSet());

        return log.traceExit(new RawDataPostFilter(anatEntities, stages, cellTypes,
                sexes, strains, dataType));
    }

    private void updateRawDataConditionMap(Set<Integer> condIds) {
        log.traceEntry("{}", condIds);

        Set<Integer> missingCondIds = new HashSet<>(condIds);
        missingCondIds.removeAll(this.rawDataConditionMap.keySet());
        if (missingCondIds.isEmpty()) {
            log.traceExit(); return;
        }
        Map<Integer, Species> speciesMap = this.getRawDataProcessedFilter().getSpeciesMap();
        Map<Integer, RawDataCondition> missingCondMap = loadRawDataConditionMapFromResultSet(
                        (attrs) -> this.rawDataConditionDAO.getRawDataConditionsFromIds(missingCondIds, attrs),
                        null, speciesMap.values(), anatEntityService, devStageService);
        //If the Map is going to grow too big, we keep only the entries needed
        //for this method call
        if (this.rawDataConditionMap.size() + missingCondMap.size() > MAX_ELEMENTS_IN_MAP) {
            this.rawDataConditionMap.keySet().retainAll(condIds);
        }
        this.rawDataConditionMap.putAll(missingCondMap);

        log.traceExit(); return;
    }
    private void updateGeneMap(Set<Integer> bgeeGeneIds) {
        log.traceEntry("{}", bgeeGeneIds);

        Set<Integer> missingGeneIds = new HashSet<>(bgeeGeneIds);
        missingGeneIds.removeAll(this.geneMap.keySet());
        if (missingGeneIds.isEmpty()) {
            log.traceExit(); return;
        }
        Map<Integer, Species> speciesMap = this.getRawDataProcessedFilter()
                .getSpeciesMap();
        Map<Integer, GeneBioType> geneBioTypeMap = this.getRawDataProcessedFilter()
                .getGeneBioTypeMap();
        Map<Integer, Gene> missingGeneMap = this.geneDAO.getGenesByBgeeIds(missingGeneIds).stream()
                .collect(Collectors.toMap(gTO -> gTO.getId(), gTO -> mapGeneTOToGene(gTO,
                        Optional.ofNullable(speciesMap.get(gTO.getSpeciesId()))
                        .orElseThrow(() -> new IllegalStateException("Missing species ID for gene")),
                        null, null,
                        Optional.ofNullable(geneBioTypeMap.get(gTO.getGeneBioTypeId()))
                        .orElseThrow(() -> new IllegalStateException("Missing gene biotype ID for gene")))));
        //If the Map is going to grow too big, we keep only the entries needed
        //for this method call
        if (this.geneMap.size() + missingGeneMap.size() > MAX_ELEMENTS_IN_MAP) {
            this.geneMap.keySet().retainAll(bgeeGeneIds);
        }
        this.geneMap.putAll(missingGeneMap);

        log.traceExit(); return;
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