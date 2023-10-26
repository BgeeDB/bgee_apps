package org.bgee.model.expressiondata.rawdata;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
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
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCountDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO.RawDataConditionTOResultSet;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCountDAO.RawDataCountContainerTO;
import org.bgee.model.dao.api.expressiondata.rawdata.est.ESTDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.est.ESTDAO.ESTTO;
import org.bgee.model.dao.api.expressiondata.rawdata.est.ESTDAO.ESTTOResultSet;
import org.bgee.model.dao.api.expressiondata.rawdata.est.ESTLibraryDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.est.ESTLibraryDAO.ESTLibraryTO;
import org.bgee.model.dao.api.expressiondata.rawdata.est.ESTLibraryDAO.ESTLibraryTOResultSet;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituEvidenceDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituEvidenceDAO.InSituEvidenceTO;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituEvidenceDAO.InSituEvidenceTOResultSet;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituExperimentDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituExperimentDAO.InSituExperimentTOResultSet;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituSpotDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituSpotDAO.InSituSpotTO;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituSpotDAO.InSituSpotTOResultSet;
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
import org.bgee.model.expressiondata.rawdata.baseelements.Assay;
import org.bgee.model.expressiondata.rawdata.baseelements.CellCompartment;
import org.bgee.model.expressiondata.rawdata.baseelements.Experiment;
import org.bgee.model.expressiondata.rawdata.baseelements.RawCall;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataAnnotation;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataCondition;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataContainer;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataContainerWithExperiment;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataCountContainer;
import org.bgee.model.expressiondata.rawdata.baseelements.RawCall.ExclusionReason;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataCondition.RawDataSex;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataDataType;
import org.bgee.model.expressiondata.rawdata.baseelements.SequencedTranscriptPart;
import org.bgee.model.expressiondata.rawdata.baseelements.Strand;
import org.bgee.model.expressiondata.rawdata.est.EST;
import org.bgee.model.expressiondata.rawdata.est.ESTContainer;
import org.bgee.model.expressiondata.rawdata.est.ESTCountContainer;
import org.bgee.model.expressiondata.rawdata.est.ESTDataType;
import org.bgee.model.expressiondata.rawdata.est.ESTLibrary;
import org.bgee.model.expressiondata.rawdata.insitu.InSituContainer;
import org.bgee.model.expressiondata.rawdata.insitu.InSituCountContainer;
import org.bgee.model.expressiondata.rawdata.insitu.InSituDataType;
import org.bgee.model.expressiondata.rawdata.insitu.InSituEvidence;
import org.bgee.model.expressiondata.rawdata.insitu.InSituExperiment;
import org.bgee.model.expressiondata.rawdata.insitu.InSituSpot;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixChip;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixChipPipelineSummary;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixExperiment;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixProbeset;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqContainer;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqCountContainer;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqDataType;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqExperiment;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqLibrary;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqLibraryAnnotatedSample;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqLibraryPipelineSummary;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqResultAnnotatedSample;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqTechnology;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqResultAnnotatedSample.AbundanceUnit;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixContainer;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixCountContainer;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixDataType;
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
 * @version Bgee 15.0, Jan. 2023
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
        // THE ORDER OF THE ELEMENTS IS IMPORTANT!
        // (it goes from more general to more specific, and this order is used in some code)
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
    private final ESTLibraryDAO estLibraryDAO;
    private final ESTDAO estDAO;
    private final InSituExperimentDAO inSituExperimentDAO;
    private final InSituEvidenceDAO inSituEvidenceDAO;
    private final InSituSpotDAO inSituSpotDAO;
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
    RawDataLoader(ServiceFactory serviceFactory,
            RawDataProcessedFilter rawDataProcessedFilter) {
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
        this.estLibraryDAO           = this.getDaoManager().getESTLibraryDAO();
        this.estDAO                  = this.getDaoManager().getESTDAO();
        this.inSituExperimentDAO     = this.getDaoManager().getInSituExperimentDAO();
        this.inSituEvidenceDAO       = this.getDaoManager().getInSituEvidenceDAO();
        this.inSituSpotDAO           = this.getDaoManager().getInSituSpotDAO();
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
        this.rawDataConditionMap.putAll(this.rawDataProcessedFilter.getRequestedConditionMap());
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
     * @param offset            A {@code Long} specifying at which index to start getting results
     *                          of the type {@code infoType}. If {@code null}, equivalent to {@code 0}
     *                          (first index). {@code Long} because sometimes the number of
     *                          potential results can be very large.
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
            RawDataDataType<T, ?> rawDataDataType, Long offset, Integer limit)
                    throws IllegalArgumentException {
        log.traceEntry("{}, {}, {}, {}", infoType, rawDataDataType, offset, limit);

        if (limit != null && limit > LIMIT_MAX) {
            throw log.throwing(new IllegalArgumentException("limit cannot be greater than "
                    + LIMIT_MAX));
        }
        long newOffset = offset == null? 0L: offset;
        int newLimit = limit == null? LIMIT_MAX: limit;

        return log.traceExit(this.loadDataInternal(infoType, rawDataDataType, newOffset, newLimit, false));
    }
    /**
     * The difference with the method {@link #loadData(InformationType, RawDataDataType, Long, Integer)}
     * is that there is no check on the {@code offset} and {@code limit} parameters,
     * we can internally request all results when necessary. And we can also request to retrieve
     * only some limited info with the argument {@code partialInfo}.
     *
     * @param <T>
     * @param infoType
     * @param rawDataDataType
     * @param offset
     * @param limit
     * @param partialInfo
     * @return
     * @throws IllegalArgumentException
     */
    private <T extends RawDataContainer<?, ?>> T loadDataInternal(InformationType infoType,
            RawDataDataType<T, ?> rawDataDataType, Long offset, Integer limit, boolean partialInfo)
                    throws IllegalArgumentException {
        log.traceEntry("{}, {}, {}, {}, {}", infoType, rawDataDataType, offset, limit, partialInfo);
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
        if (partialInfo && InformationType.CALL.equals(infoType)) {
            throw log.throwing(new IllegalArgumentException(
                    "partialInfo cannot be requested with CALL information type"));
        }

        DataType requestedDataType = rawDataDataType.getDataType();
        Class<T> rawDataContainerClass = rawDataDataType.getRawDataContainerClass();
        T rawDataContainer = null;
        switch (requestedDataType) {
        case AFFYMETRIX:
            rawDataContainer = rawDataContainerClass.cast(
                    this.loadAffymetrixData(infoType, offset, limit, partialInfo));
            break;
        case RNA_SEQ:
        case SC_RNA_SEQ:
            rawDataContainer = rawDataContainerClass.cast(
                    this.loadRnaSeqData(infoType,
                            requestedDataType.equals(DataType.SC_RNA_SEQ)? true: false,
                                    offset, limit, partialInfo));
            break;
        case EST:
            rawDataContainer = rawDataContainerClass.cast(
                    this.loadESTData(infoType, offset, limit, partialInfo));
            break;
        case IN_SITU:
            rawDataContainer = rawDataContainerClass.cast(
                    this.loadInSituData(infoType, offset, limit, partialInfo));
            break;
        default:
            throw log.throwing(new IllegalStateException("Unsupported data type: " + requestedDataType));
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
        case RNA_SEQ:
        case SC_RNA_SEQ:
            rawDataCountContainer = rawDataCountContainerClass.cast(
                    this.loadRnaSeqCount(
                            requestedDataType.equals(DataType.SC_RNA_SEQ)? true: false,
                            withExperiment, withAssay, withCall));
            break;
        case EST:
            rawDataCountContainer = rawDataCountContainerClass.cast(
                    this.loadESTCount(withExperiment, withAssay, withCall));
            break;
        case IN_SITU:
            rawDataCountContainer = rawDataCountContainerClass.cast(
                    this.loadInSituCount(withExperiment, withAssay, withCall));
            break;
        default:
            throw log.throwing(new IllegalStateException("Unsupported data type: " + requestedDataType));
        }

        return log.traceExit(rawDataCountContainer);
    }

    public RawDataPostFilter loadPostFilter(RawDataDataType<?, ?> rawDataDataType) {
        log.traceEntry("{}", rawDataDataType);
        return log.traceExit(this.loadPostFilter(rawDataDataType, true, true, true));
    }
    /**
     * Load anatomical entities, dev. stages, cell types, sexes and strains for the specified
     * data type using the condition IDs of the {@code DAORawDataFilter}s.
     * 
     * @param dataType              A {@code DataType} corresponding to the datatype for which
     *                              post filters have to be loaded
     * @param withConditionFilters  A {@code boolean} to define whether to populate the attributes
     *                              related to condition parameters in the returned {@code RawDataPostFilter}.
     * @param withExperimentFilters A {@code boolean} to define whether to populate the attributes
     *                              related to experiments in the returned {@code RawDataPostFilter}.
     * @param withAssayFilters      A {@code boolean} to define whether to populate the attributes
     *                              related to assays in the returned {@code RawDataPostFilter}.
     * @return                      A {@code RawDataPostFilter} containing condition parameters to filter on.
     * @throws IllegalArgumentException If {@code rawDataDataType}, or if all the {@code boolean}
     *                                  arguments are {@code false}.
     */
    //We also use a RawDataDataType, in case we create data-type-specific PostFilters
    //in the future. In that case, we could add a third generic type parameter to RawDataDataType,
    //specifying the type of RawDataPostFilter to return.
    public RawDataPostFilter loadPostFilter(RawDataDataType<?, ?> rawDataDataType,
            boolean withConditionFilters, boolean withExperimentFilters, boolean withAssayFilters)
                    throws IllegalArgumentException {
        log.traceEntry("{}, {}, {}, {}", rawDataDataType, withConditionFilters, withExperimentFilters,
                withAssayFilters);
        if (rawDataDataType == null) {
            throw log.throwing(new IllegalArgumentException("dataType can not be null"));
        }
        if (!withConditionFilters && !withExperimentFilters && !withAssayFilters) {
            throw log.throwing(new IllegalArgumentException("Some filters must be requested"));
        }

        //If the DaoRawDataFilters are null it means there was no matching conds
        //and thus no result for sure
        if (this.getRawDataProcessedFilter().getDaoFilters() == null) {
            return log.traceExit(new RawDataPostFilter(rawDataDataType));
        }

        DataType requestedDataType = rawDataDataType.getDataType();
        DAODataType requestedDAODataType = this.convertRawDataDataTypeToDAODataType(rawDataDataType);
        //Retrieve info necessary to create the filter.
        //First, experiment/assay info
        InformationType infoType = null;
        if (withAssayFilters) {
            infoType = InformationType.ASSAY;
        } else if (withExperimentFilters) {
            infoType = InformationType.EXPERIMENT;
        }
        RawDataPostFilter expAssayFilter = null;
        if (infoType != null) {
            RawDataContainer<?, ?> results = this.loadDataInternal(infoType, rawDataDataType,
                    null, null, true);
            expAssayFilter = new RawDataPostFilter(null, null, null, null, null, null,
                    !withExperimentFilters || !RawDataContainerWithExperiment.class.isInstance(results)?
                            null: ((RawDataContainerWithExperiment<?, ?, ?>) results).getExperiments()
                            .stream()
                            .map(e -> (Experiment<?>) e)
                            .collect(Collectors.toSet()),
                    !withAssayFilters? null: results.getAssays()
                            .stream()
                            .map(a -> (Assay) a)
                            .collect(Collectors.toSet()),
                    rawDataDataType);
        }

        //Now, condition info
        RawDataPostFilter condFilter = null;
        if (withConditionFilters) {
            condFilter = this.loadConditionPostFilter(
                    (attrs) -> this.rawDataConditionDAO.getRawDataConditionsLinkedToDataType(
                            this.getRawDataProcessedFilter().getDaoFilters(),
                            requestedDAODataType, requestedDataType.getSingleCell(), attrs),
                    rawDataDataType);
        }

        //Merge the experiment/assay filter and condition filter
        return log.traceExit(RawDataPostFilter.merge(expAssayFilter, condFilter));
    }

//*****************************************************************************************
//                       METHODS LOADING AFFYMETRIX RAW DATA
//*****************************************************************************************

    //Long and Integer instead of long and int because used internally to retrieve all results for filtering
    private AffymetrixContainer loadAffymetrixData(InformationType infoType, Long offset, Integer limit,
            boolean partialInfo) {
        log.traceEntry("{}, {}, {}, {}", infoType, offset, limit, partialInfo);

        //If the DaoRawDataFilters are null it means there was no matching conds
        //and thus no result for sure
        if (this.getRawDataProcessedFilter().getDaoFilters() == null) {
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
                .getDaoFilters();

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
            assert !partialInfo;
            chipTORS = this.affymetrixChipDAO.getAffymetrixChipsFromBgeeChipIds(bgeeChipIds,
                    null);
        } else if (infoType == InformationType.ASSAY) {
            chipTORS = this.affymetrixChipDAO.getAffymetrixChips(daoRawDataFilters, offset, limit,
                    !partialInfo? null:
                        Set.of(AffymetrixChipDAO.Attribute.EXPERIMENT_ID,
                               AffymetrixChipDAO.Attribute.AFFYMETRIX_CHIP_ID,
                               AffymetrixChipDAO.Attribute.BGEE_AFFYMETRIX_CHIP_ID));
        }
        //We need to identify the species for providing download links for experiments.
        //If no gene and no conditions were retrieved, we have to make a special query for it.
        else if (infoType == InformationType.EXPERIMENT && !partialInfo) {
            chipTORS = this.affymetrixChipDAO.getAffymetrixChips(daoRawDataFilters, 0L, 1,
                    Set.of(AffymetrixChipDAO.Attribute.CONDITION_ID));
        }
        if (chipTORS != null) {
            while (chipTORS.next()) {
                AffymetrixChipTO chipTO = chipTORS.getTO();
                if (chipTO.getExperimentId() != null) {
                    affyExpIds.add(chipTO.getExperimentId());
                }
                if (chipTO.getConditionId() != null) {
                    rawDataCondIds.add(chipTO.getConditionId());
                }
                if (infoType != InformationType.EXPERIMENT) {
                    affyChipTOs.add(chipTO);
                }
            }
        }


        //************************************************************
        // Now, we load missing Genes and RawDataConditions
        //************************************************************
        this.updateRawDataConditionMap(rawDataCondIds);
        this.updateGeneMap(bgeeGeneIds);

        //We load the speciesId of the experiment either from a condition or a gene
        Integer speciesId = null;
        if (!rawDataCondIds.isEmpty()) {
            int condId = rawDataCondIds.iterator().next();
            speciesId = Optional.ofNullable(
                    this.rawDataConditionMap.get(condId))
            .orElseThrow(() -> new IllegalStateException(
                    "Missing RawDataCondition ID " + condId))
            .getSpeciesId();
        } else if (!bgeeGeneIds.isEmpty()) {
            int bgeeGeneid = bgeeGeneIds.iterator().next();
            speciesId = Optional.ofNullable(geneMap.get(bgeeGeneid))
            .orElseThrow(() -> new IllegalStateException(
                    "Missing gene ID " + bgeeGeneid))
            .getSpecies().getId();
        }
        Integer finalExpSpeciesId = speciesId;


        //*********** Experiments ***********
        MicroarrayExperimentTOResultSet expTORS = null;
        //Experiments should always be retrieved at this point if there is any result,
        //but we need this check in case there was no result returned when requesting
        //CALLs or ASSAYs.
        if (!affyExpIds.isEmpty()) {
            //we can use a new DAORawDataFilter to retrieve the requested experiments
            expTORS = this.microarrayExperimentDAO.getExperiments(
                    Set.of(new DAORawDataFilter(affyExpIds, null, null)), null, null,
                    !partialInfo? null:
                        Set.of(MicroarrayExperimentDAO.Attribute.ID,
                               MicroarrayExperimentDAO.Attribute.NAME));
        } else if (infoType == InformationType.EXPERIMENT) {
            //otherwise, it was the information requested originally
            expTORS = this.microarrayExperimentDAO.getExperiments(daoRawDataFilters, offset, limit,
                    !partialInfo? null:
                        Set.of(MicroarrayExperimentDAO.Attribute.ID,
                               MicroarrayExperimentDAO.Attribute.NAME));
        }
        LinkedHashMap<String, AffymetrixExperiment> expIdToAffyExp =
                expTORS == null? new LinkedHashMap<>():
                    expTORS.stream()
                    .collect(Collectors.toMap(
                            to -> to.getId(),
                            to -> new AffymetrixExperiment(to.getId(), to.getName(),
                                    to.getDescription(),
                                    to.getDataSourceId() == null? null: getSourceById(to.getDataSourceId()),
                                    finalExpSpeciesId == null? null: getAffymetrixExperimentDownloadURL(
                                            finalExpSpeciesId, to.getId()),
                                    0),
                            (v1, v2) -> {throw new IllegalStateException("No key collision possible");},
                            LinkedHashMap::new));


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
                                    to.getConditionId() == null? null: new RawDataAnnotation(
                                            Optional.ofNullable(
                                                    this.rawDataConditionMap.get(to.getConditionId()))
                                            .orElseThrow(() -> new IllegalStateException(
                                                    "Missing RawDataCondition ID "
                                                    + to.getConditionId()
                                                    + " for chip ID " + to.getAffymetrixChipId())),
                                            null, null, null),
                                    null,
                                    to.getDistinctRankCount() == null? null: new AffymetrixChipPipelineSummary(
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
    private String getAffymetrixExperimentDownloadURL(int speciesId, String experimentId) {
        log.traceEntry("{}, {}", speciesId, experimentId);
        String speciesLinkPart = this.getSpeciesNameWithoutSpace(speciesId);
        return log.traceExit(this.getServiceFactory().getBgeeProperties()
                .getDownloadAffyProcExprValueFilesRootDirectory()
                + speciesLinkPart + "/"
                + speciesLinkPart + "_Affymetrix_probesets_" + experimentId + ".tar.gz");
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
        if (this.getRawDataProcessedFilter().getDaoFilters() == null) {
            return log.traceExit(new AffymetrixCountContainer(
                    withExperiment? 0: null,
                    withAssay? 0: null,
                    withCall? 0: null));
        }

        RawDataCountContainerTO countTO = this.rawDataCountDAO.getAffymetrixCount(
                this.getRawDataProcessedFilter().getDaoFilters(),
                withExperiment, withAssay, withCall);

        return log.traceExit(new AffymetrixCountContainer(
                countTO.getExperimentCount(),
                countTO.getAssayCount(),
                countTO.getCallCount()));
    }

//*****************************************************************************************
//                         METHODS LOADING RNA-SEQ RAW DATA
//*****************************************************************************************

    //Long and Integer instead of long and int because used internally to retrieve all results for filtering
    private RnaSeqContainer loadRnaSeqData(InformationType infoType, boolean isSingleCell,
            Long offset, Integer limit, boolean partialInfo) {
        log.traceEntry("{}, {}, {}, {}, {}", infoType, isSingleCell, offset, limit, partialInfo);

        //If the DaoRawDataFilters are null it means there was no matching conds
        //and thus no result for sure
        if (this.getRawDataProcessedFilter().getDaoFilters() == null) {
            return log.traceExit(this.getNoResultRnaSeqContainer(infoType));
        }

        //************************************************************
        // First, we retrieve all necessary TransferObjects
        //************************************************************
        LinkedHashSet<RNASeqResultAnnotatedSampleTO> callTOs = new LinkedHashSet<>();
        LinkedHashSet<RNASeqLibraryAnnotatedSampleTO> assayTOs = new LinkedHashSet<>();
        LinkedHashSet<RNASeqLibraryTO> libTOs = new LinkedHashSet<>();
        Set<DAORawDataFilter> daoRawDataFilters = this.getRawDataProcessedFilter()
                .getDaoFilters();

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
            assert !partialInfo;
            assayTORS = this.rnaSeqAssayDAO.getLibraryAnnotatedSamplesFromLibraryAnnotatedSampleIds(
                    bgeeAnnotatedSampleIds,
                    null);
        } else if (infoType == InformationType.ASSAY) {
            assayTORS = this.rnaSeqAssayDAO.getLibraryAnnotatedSamples(daoRawDataFilters,
                    isSingleCell, offset, limit,
                    !partialInfo? null: Set.of(
                            RNASeqLibraryAnnotatedSampleDAO.Attribute.ID,
                            RNASeqLibraryAnnotatedSampleDAO.Attribute.RNASEQ_LIBRARY_ID));
        }
        //We need to identify the species for providing download links for experiments.
        //If no gene and no conditions were retrieved, we have to make a special query for it.
        else if (infoType == InformationType.EXPERIMENT && !partialInfo) {
            assayTORS = this.rnaSeqAssayDAO.getLibraryAnnotatedSamples(daoRawDataFilters,
                    isSingleCell, 0L, 1,
                    Set.of(RNASeqLibraryAnnotatedSampleDAO.Attribute.CONDITION_ID));
        }
        Set<String> libraryIds = new HashSet<>();
        Set<Integer> rawDataCondIds = new HashSet<>();
        if (assayTORS != null) {
            while (assayTORS.next()) {
                RNASeqLibraryAnnotatedSampleTO assayTO = assayTORS.getTO();
                if (assayTO.getLibraryId() != null) {
                    libraryIds.add(assayTO.getLibraryId());
                }
                if (assayTO.getConditionId() != null) {
                    rawDataCondIds.add(assayTO.getConditionId());
                }
                if (infoType != InformationType.EXPERIMENT) {
                    assayTOs.add(assayTO);
                }
            }
        }

        //************************************************************
        // Now, we load missing Genes and RawDataConditions
        //************************************************************
        this.updateRawDataConditionMap(rawDataCondIds);
        this.updateGeneMap(bgeeGeneIds);

        //We load the speciesId of the experiment either from a condition or a gene
        Integer speciesId = null;
        if (!rawDataCondIds.isEmpty()) {
            int condId = rawDataCondIds.iterator().next();
            speciesId = Optional.ofNullable(
                    this.rawDataConditionMap.get(condId))
            .orElseThrow(() -> new IllegalStateException(
                    "Missing RawDataCondition ID " + condId))
            .getSpeciesId();
        } else if (!bgeeGeneIds.isEmpty()) {
            int bgeeGeneid = bgeeGeneIds.iterator().next();
            speciesId = Optional.ofNullable(geneMap.get(bgeeGeneid))
            .orElseThrow(() -> new IllegalStateException(
                    "Missing gene ID " + bgeeGeneid))
            .getSpecies().getId();
        }
        Integer finalExpSpeciesId = speciesId;


        //*********** Libraries ***********
        Set<String> expIds = new HashSet<>();
        if (!libraryIds.isEmpty()) {
            //we can use a new DAORawDataFilter to retrieve the requested libraries
            DAORawDataFilter libFilter = new DAORawDataFilter(null, libraryIds, null);
            RNASeqLibraryTOResultSet libTORS = this.rnaSeqLibraryDAO.getRnaSeqLibrary(
                    Collections.singleton(libFilter), null, null, null,
                    !partialInfo? null: Set.of(
                            RNASeqLibraryDAO.Attribute.ID,
                            RNASeqLibraryDAO.Attribute.EXPERIMENT_ID));

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
                    Collections.singleton(expFilter), null, null, null,
                    !partialInfo? null: Set.of(
                            RNASeqExperimentDAO.Attribute.ID,
                            RNASeqExperimentDAO.Attribute.NAME));
        } else if (infoType == InformationType.EXPERIMENT) {
            //otherwise, it was the information requested originally
            expTORS = this.rnaSeqExperimentDAO.getExperiments(daoRawDataFilters, isSingleCell,
                    offset, limit,
                    !partialInfo? null: Set.of(
                            RNASeqExperimentDAO.Attribute.ID,
                            RNASeqExperimentDAO.Attribute.NAME));
        }
        LinkedHashMap<String, RnaSeqExperiment> expIdToExp =
                expTORS == null? new LinkedHashMap<>():
                    expTORS.stream()
                    .collect(Collectors.toMap(
                            to -> to.getId(),
                            to -> new RnaSeqExperiment(to.getId(), to.getName(),
                                  to.getDescription(),
                                  to.getDataSourceId() == null? null: getSourceById(to.getDataSourceId()),
                                  finalExpSpeciesId == null? null:
                                      getRNASeqExperimentDownloadURL(isSingleCell, finalExpSpeciesId, to.getId()),
                                  0),
                            (v1, v2) -> {throw new IllegalStateException("No key collision possible");},
                            LinkedHashMap::new));


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

                                    to.getTechnologyName() == null? null: new RnaSeqTechnology(
                                            to.getTechnologyName(),
                                            to.getSequencerName(),
                                            Strand.convertToStrand(to.getStrandSelection().name()),
                                            SequencedTranscriptPart.convertToSequencedTranscriptPart(
                                                    to.getSequencedTranscriptPart().name()),
                                            CellCompartment.convertToCellCompartment(
                                                    to.getCellCompartment().name()),
                                            to.getSampleMultiplexing(),
                                            to.getLibraryMultiplexing(),
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
                                    to.getConditionId() == null? null: new RawDataAnnotation(
                                            Optional.ofNullable(
                                                    this.rawDataConditionMap.get(
                                                            to.getConditionId()))
                                            .orElseThrow(() -> new IllegalStateException(
                                                    "Missing RawDataCondition ID "
                                                    + to.getConditionId()
                                                    + " for annotated sample ID " + to.getId())),
                                            null, null, null),
                                    to.getDistinctRankCount() == null? null: new RnaSeqLibraryPipelineSummary(
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
    private String getRNASeqExperimentDownloadURL(boolean isSingleCell, int speciesId, String experimentId) {
        log.traceEntry("{}, {}, {}", isSingleCell, speciesId, experimentId);
        String speciesLinkPart = this.getSpeciesNameWithoutSpace(speciesId);
        String urlStart = isSingleCell?
                this.getServiceFactory().getBgeeProperties()
                    .getDownloadSingleCellRNASeqFullLengthProcExprValueFilesRootDirectory():
                this.getServiceFactory().getBgeeProperties()
                    .getDownloadRNASeqProcExprValueFilesRootDirectory();
        String fileNamePart = isSingleCell?
                "_Full-Length_SC_RNA-Seq_read_counts_TPM_FPKM_":
                "_RNA-Seq_read_counts_TPM_FPKM_";
        return log.traceExit(urlStart
                + speciesLinkPart + "/"
                + speciesLinkPart + fileNamePart + experimentId + ".tsv.gz");
    }
    private String getSpeciesNameWithoutSpace(int speciesId) {
        log.traceEntry("{}", speciesId);
        Species species = Optional.ofNullable(this.getRawDataProcessedFilter().getSpeciesMap()
                .get(speciesId)).orElseThrow(() -> new IllegalStateException(
                        "Missing species for speciesId " + speciesId));
        String speciesLinkPart = species.getGenus() + "_" + species.getSpeciesName();
        return log.traceExit(speciesLinkPart.replace(" ", "_"));
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

    private RnaSeqCountContainer loadRnaSeqCount(boolean isSingleCell, boolean withExperiment,
            boolean withAssay, boolean withCall) {
        log.traceEntry("{}, {}, {}, {}", isSingleCell, withExperiment, withAssay, withCall);

        //If the DaoRawDataFilters are null it means there was no matching conds
        //and thus no result for sure
        if (this.getRawDataProcessedFilter().getDaoFilters() == null) {
            return log.traceExit(new RnaSeqCountContainer(
                    withExperiment? 0: null,
                    withAssay? 0: null,
                    withAssay? 0: null,
                    withCall? 0: null));
        }

        RawDataCountContainerTO countTO = rawDataCountDAO.getRnaSeqCount(
                this.getRawDataProcessedFilter().getDaoFilters(),
                isSingleCell, withExperiment, withAssay, withAssay, withCall);

        return log.traceExit(new RnaSeqCountContainer(
                countTO.getExperimentCount(),
                countTO.getRnaSeqLibraryCount(),
                countTO.getAssayCount(),
                countTO.getCallCount()));
    }

//*****************************************************************************************
//                           METHODS LOADING EST RAW DATA
//*****************************************************************************************

    //Long and Integer instead of long and int because used internally to retrieve all results for filtering
    private ESTContainer loadESTData(InformationType infoType, Long offset, Integer limit,
            boolean partialInfo) {
        log.traceEntry("{}, {}, {}, {}", infoType, offset, limit, partialInfo);

        //If the DaoRawDataFilters are null it means there was no matching conds
        //and thus no result for sure
        if (this.getRawDataProcessedFilter().getDaoFilters() == null) {
            return log.traceExit(this.getNoResultESTContainer(infoType));
        }

        //************************************************************
        // First, we retrieve all necessary TransferObjects
        //************************************************************
        LinkedHashSet<ESTTO> callTOs = new LinkedHashSet<>();
        LinkedHashSet<ESTLibraryTO> assayTOs = new LinkedHashSet<>();
        Set<DAORawDataFilter> daoRawDataFilters = this.getRawDataProcessedFilter()
                .getDaoFilters();

        //*********** Calls ***********
        Set<String> estLibraryIds = new HashSet<>();
        Set<Integer> bgeeGeneIds = new HashSet<>();
        if (infoType == InformationType.CALL) {
            ESTTOResultSet callTORS = this.estDAO.getESTs(daoRawDataFilters, offset, limit, null);
            while (callTORS.next()) {
                ESTTO callTO = callTORS.getTO();
                estLibraryIds.add(callTO.getAssayId());
                bgeeGeneIds.add(callTO.getBgeeGeneId());
                callTOs.add(callTO);
            }
        }

        //*********** Assays ***********
        ESTLibraryTOResultSet assayTORS = null;
        //We need to write the test in this way, in case CALLs were requested, but there was
        //no result retrieved
        if (!estLibraryIds.isEmpty()) {
            assert !partialInfo;
            //Create a new DAORawDataFilter for retrieving libraries based on their ID
            DAORawDataFilter daoFilter = new DAORawDataFilter(null, estLibraryIds, null);
            assayTORS = this.estLibraryDAO.getESTLibraries(Set.of(daoFilter), null, null,
                    null);

        // For EST, it is equivalent to request for assays or for experiments,
        //since there are no experiments
        } else if (infoType == InformationType.ASSAY || infoType == InformationType.EXPERIMENT) {
            assayTORS = this.estLibraryDAO.getESTLibraries(daoRawDataFilters, offset, limit,
                    !partialInfo? null: Set.of(
                            ESTLibraryDAO.Attribute.ID,
                            ESTLibraryDAO.Attribute.NAME));
        }
        Set<Integer> rawDataCondIds = new HashSet<>();
        if (assayTORS != null) {
            while (assayTORS.next()) {
                ESTLibraryTO assayTO = assayTORS.getTO();
                if (assayTO.getConditionId() != null) {
                    rawDataCondIds.add(assayTO.getConditionId());
                }
                assayTOs.add(assayTO);
            }
        }

        //************************************************************
        // Now, we load missing Genes and RawDataConditions
        //************************************************************
        this.updateRawDataConditionMap(rawDataCondIds);
        this.updateGeneMap(bgeeGeneIds);

        //************************************************************
        // Finally, we instantiate all bgee-core objects necessary
        //************************************************************

        LinkedHashMap<String, ESTLibrary> libIdToLib = assayTOs.stream()
                .collect(Collectors.toMap(
                        to -> to.getId(),

                        to -> new ESTLibrary(
                                to.getId(), to.getName(), to.getDescription(),
                                to.getConditionId() == null? null: new RawDataAnnotation(
                                        Optional.ofNullable(
                                                this.rawDataConditionMap.get(
                                                        to.getConditionId()))
                                        .orElseThrow(() -> new IllegalStateException(
                                                "Missing RawDataCondition ID "
                                                + to.getConditionId()
                                                + " for annotated sample ID " + to.getId())),
                                        null, null, null),
                                to.getDataSourceId() == null? null: getSourceById(to.getDataSourceId())),

                        (v1, v2) -> {throw new IllegalStateException("No key collision possible");},
                        LinkedHashMap::new));


        //Libraries are always needed
        LinkedHashSet<ESTLibrary> estLibraries =
                new LinkedHashSet<>(libIdToLib.values());

        //Now we load the LinkedHashSets only if needed, to distinguish between
        //null value = info not requested, and empty Collection = no result
        LinkedHashSet<EST> calls = null;
        if (infoType == InformationType.CALL) {
            calls = callTOs.stream()
                    .map(to -> new EST(
                            to.getId(),
                            Optional.ofNullable(libIdToLib.get(to.getAssayId()))
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
                                            to.getExclusionReason().name()))))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }

        return log.traceExit(new ESTContainer(estLibraries, calls));
    }
    private ESTContainer getNoResultESTContainer(InformationType infoType) {
        log.traceEntry("{}", infoType);

        return log.traceExit(new ESTContainer(
                //Libraries always end up being requested
                Set.of(),
                infoType == InformationType.CALL? Set.of(): null));
    }

    private ESTCountContainer loadESTCount(boolean withExperiment, boolean withAssay,
            boolean withCall) {
        log.traceEntry("{}, {}, {}", withExperiment, withAssay, withCall);

        //If the DaoRawDataFilters are null it means there was no matching conds
        //and thus no result for sure
        if (this.getRawDataProcessedFilter().getDaoFilters() == null) {
            return log.traceExit(new ESTCountContainer(
                    withExperiment || withAssay? 0: null,
                    withCall? 0: null));
        }

        RawDataCountContainerTO countTO = rawDataCountDAO.getESTCount(
                this.getRawDataProcessedFilter().getDaoFilters(),
                withExperiment || withAssay, withCall);

        return log.traceExit(new ESTCountContainer(
                countTO.getAssayCount(),
                countTO.getCallCount()));
    }

//*****************************************************************************************
//                             METHODS LOADING IN SITU RAW DATA
//*****************************************************************************************

    //Long and Integer instead of long and int because used internally to retrieve all results for filtering
    private InSituContainer loadInSituData(InformationType infoType, Long offset, Integer limit,
            boolean partialInfo) {
        log.traceEntry("{}, {}, {}, {}", infoType, offset, limit, partialInfo);

        //If the DaoRawDataFilters are null it means there was no matching conds
        //and thus no result for sure
        if (this.getRawDataProcessedFilter().getDaoFilters() == null) {
            return log.traceExit(this.getNoResultInSituContainer(infoType));
        }

        //************************************************************
        // First, we retrieve all necessary TransferObjects
        //************************************************************
        LinkedHashSet<InSituSpotTO> callTOs = new LinkedHashSet<>();
        LinkedHashSet<InSituEvidenceTO> assayTOs = new LinkedHashSet<>();
        Set<String> assayIds = new HashSet<>();
        Set<Integer> bgeeGeneIds = new HashSet<>();
        Set<Integer> rawDataCondIds = new HashSet<>();
        Set<DAORawDataFilter> daoRawDataFilters = this.getRawDataProcessedFilter()
                .getDaoFilters();

        //*********** Calls (and "fake" assays) ***********
        //If ASSAY is requested,
        //We create one evidence for each condition retrieved from a spot
        if (infoType == InformationType.CALL || infoType == InformationType.ASSAY && !partialInfo) {
            Collection<InSituSpotDAO.Attribute> attrs = null;
            if (infoType == InformationType.ASSAY) {
                attrs = EnumSet.of(InSituSpotDAO.Attribute.IN_SITU_EVIDENCE_ID,
                        InSituSpotDAO.Attribute.CONDITION_ID);
            }
            //The offset and limit should correctly apply even if infoType is ASSAY,
            //since in that case we request only IN_SITU_EVIDENCE_ID and CONDITION_ID
            InSituSpotTOResultSet callTORS = this.inSituSpotDAO.getInSituSpots(
                    daoRawDataFilters, offset, limit, attrs);
            while (callTORS.next()) {
                InSituSpotTO callTO = callTORS.getTO();
                assayIds.add(callTO.getAssayId());
                rawDataCondIds.add(callTO.getConditionId());
                //We don't request the genes if infoType is ASSAY
                if (callTO.getBgeeGeneId() != null) {
                    bgeeGeneIds.add(callTO.getBgeeGeneId());
                }
                callTOs.add(callTO);
            }
        }

        //*********** Assays ***********
        InSituEvidenceTOResultSet assayTORS = null;
        Set<String> expIds = new HashSet<>();
        //We need to write the test in this way, in case there was no result retrieved
        if (!assayIds.isEmpty()) {
            assert !partialInfo;
            assayTORS = this.inSituEvidenceDAO.getInSituEvidenceFromIds(assayIds, null);
        }
        else if (infoType == InformationType.ASSAY && partialInfo) {
            assayTORS = this.inSituEvidenceDAO.getInSituEvidences(daoRawDataFilters, offset, limit,
                    Set.of(InSituEvidenceDAO.Attribute.IN_SITU_EVIDENCE_ID,
                           InSituEvidenceDAO.Attribute.EXPERIMENT_ID));
        }
        if (assayTORS != null) {
            while (assayTORS.next()) {
                InSituEvidenceTO assayTO = assayTORS.getTO();
                expIds.add(assayTO.getExperimentId());
                assayTOs.add(assayTO);
            }
        }

        //*********** Experiments ***********
        InSituExperimentTOResultSet expTORS = null;
        //Experiments should always be retrieved at this point if there is any result,
        //but we need this check in case there was no result returned when requesting
        //CALLs or ASSAYs.
        if (!expIds.isEmpty()) {
            //we can use a new DAORawDataFilter to retrieve the requested experiments
            expTORS = this.inSituExperimentDAO.getInSituExperiments(
                    Set.of(new DAORawDataFilter(expIds, null, null)), null, null,
                    !partialInfo? null: Set.of(
                            InSituExperimentDAO.Attribute.ID,
                            InSituExperimentDAO.Attribute.NAME));
        } else if (infoType == InformationType.EXPERIMENT) {
            //otherwise, it was the information requested originally
            expTORS = this.inSituExperimentDAO.getInSituExperiments(daoRawDataFilters, offset, limit,
                    !partialInfo? null: Set.of(
                            InSituExperimentDAO.Attribute.ID,
                            InSituExperimentDAO.Attribute.NAME));
        }
        LinkedHashMap<String, InSituExperiment> expIdToExp =
                expTORS == null? new LinkedHashMap<>():
                    expTORS.stream()
                    .collect(Collectors.toMap(
                            to -> to.getId(),
                            to -> new InSituExperiment(to.getId(), to.getName(),
                                    to.getDescription(),
                                    to.getDataSourceId() == null? null: getSourceById(to.getDataSourceId()),
                                    0),
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
        LinkedHashSet<InSituExperiment> experiments =
                new LinkedHashSet<>(expIdToExp.values());

        //Now we load the LinkedHashSets only if needed, to distinguish between
        //null value = info not requested, and empty Collection = no result
        LinkedHashSet<InSituEvidence> assays = null;
        LinkedHashSet<InSituSpot> calls = null;
        if (infoType == InformationType.ASSAY || infoType == InformationType.CALL) {
            Map<String, InSituEvidenceTO> idToAssayTO = assayTOs.stream()
                    .collect(Collectors.toMap(to -> to.getId(), to -> to));
            //We create a Map Entry<condId, assayId> -> assay for easier instantiations
            LinkedHashMap<Entry<Integer, String>, InSituEvidence> assayMap = callTOs
                    .stream()
                    .collect(Collectors.toMap(
                            to -> Map.entry(to.getConditionId(), to.getAssayId()),

                            to -> new InSituEvidence(
                                    to.getAssayId(),
                                    Optional.ofNullable(expIdToExp.get(
                                            Optional.ofNullable(idToAssayTO.get(to.getAssayId()))
                                            .orElseThrow(() -> new IllegalStateException(
                                                    "Missing evidence ID " + to.getAssayId()
                                                    + " for spot ID " + to.getId()))
                                            .getExperimentId()
                                    ))
                                    .orElseThrow(() -> new IllegalStateException(
                                            "Missing experiment ID "
                                            + idToAssayTO.get(to.getAssayId()).getExperimentId()
                                            + " for assay ID " + to.getAssayId())),
                                    new RawDataAnnotation(
                                            Optional.ofNullable(
                                                    this.rawDataConditionMap.get(to.getConditionId()))
                                            .orElseThrow(() -> new IllegalStateException(
                                                    "Missing RawDataCondition ID "
                                                            + to.getConditionId()
                                                            + " for spot ID " + to.getId())),
                                            null, null, null)),

                            (v1, v2) -> {
                                //We do nothing special here, it means that an evidence
                                //included several spots for different genes in the same condition
                                return v1;
                            },
                            LinkedHashMap::new));

            assays = !assayMap.isEmpty()? new LinkedHashSet<>(assayMap.values()):
                //This case, where assayMap is empty but assayTOs is not,
                //only apply when partialInfo is true,
                //there is then no condition information to retrieve.
                assayTOs.stream()
                .map(to -> new InSituEvidence(
                        to.getId(),
                        Optional.ofNullable(expIdToExp.get(
                                to.getExperimentId()
                                ))
                        .orElseThrow(() -> new IllegalStateException(
                                "Missing experiment ID "
                                        + idToAssayTO.get(to.getId()).getExperimentId()
                                        + " for assay ID " + to.getId())),
                        null))
                .collect(Collectors.toCollection(() -> new LinkedHashSet<>()));
            assert !assayMap.isEmpty() || assayTOs.isEmpty() || partialInfo;

            if (infoType == InformationType.CALL) {
                calls = callTOs.stream()
                        .map(to -> new InSituSpot(
                                to.getId(),
                                Optional.ofNullable(assayMap.get(
                                        Map.entry(to.getConditionId(), to.getAssayId())))
                                .orElseThrow(() -> new IllegalStateException(
                                        "Missing evidence ID " + to.getAssayId() + " in cond ID "
                                        + to.getConditionId() + " for spot ID " + to.getId())),
                                new RawCall(
                                        Optional.ofNullable(geneMap.get(to.getBgeeGeneId()))
                                        .orElseThrow(() -> new IllegalStateException(
                                                "Missing gene ID " + to.getBgeeGeneId()
                                                + " for probeset ID " + to.getId())),
                                        to.getPValue(),
                                        to.getExpressionConfidence(),
                                        ExclusionReason.convertToExclusionReason(
                                                to.getExclusionReason().name()))))
                        .collect(Collectors.toCollection(LinkedHashSet::new));
            }
        }

        return log.traceExit(new InSituContainer(experiments, assays, calls));
    }
    private InSituContainer getNoResultInSituContainer(InformationType infoType) {
        log.traceEntry("{}", infoType);

        return log.traceExit(new InSituContainer(
                //Experiments always end up being requested
                Set.of(),
                //We also get the assays when we request the calls
                infoType == InformationType.CALL || infoType == InformationType.ASSAY? Set.of(): null,
                infoType == InformationType.CALL? Set.of(): null));
    }

    private InSituCountContainer loadInSituCount(boolean withExperiment,
            boolean withAssay, boolean withCall) {
        log.traceEntry("{}, {}, {}", withExperiment, withAssay, withCall);

        //If the DaoRawDataFilters are null it means there was no matching conds
        //and thus no result for sure
        if (this.getRawDataProcessedFilter().getDaoFilters() == null) {
            return log.traceExit(new InSituCountContainer(
                    withExperiment? 0: null,
                    withAssay? 0: null,
                    withCall? 0: null));
        }

        RawDataCountContainerTO countTO = rawDataCountDAO.getInSituCount(
                this.getRawDataProcessedFilter().getDaoFilters(),
                withExperiment, false, withAssay, withCall);

        return log.traceExit(new InSituCountContainer(
                countTO.getExperimentCount(),
                countTO.getInsituAssayConditionCount(),
                countTO.getCallCount()));
    }

//*****************************************************************************************
//                       METHODS NECESSARY FOR ALL DATA TYPES
//*****************************************************************************************

    private RawDataPostFilter loadConditionPostFilter(Function<
            Collection<RawDataConditionDAO.Attribute>, RawDataConditionTOResultSet> condRequest,
            RawDataDataType<?, ?> dataType) {
        log.traceEntry("{}, {}", condRequest, dataType);

        //If the DaoRawDataFilters are null it means there was no matching conds
        //and thus no result for sure
        if (this.getRawDataProcessedFilter().getDaoFilters() == null) {
            return log.traceExit(new RawDataPostFilter(dataType));
        }

        // retrieve anatEntities and cell types
        Set<String> anatEntityIds = condRequest.apply(
                Set.of(RawDataConditionDAO.Attribute.ANAT_ENTITY_ID)).stream()
                .map(a -> a.getAnatEntityId()).collect(Collectors.toSet());
        Set<String> cellTypeIds = condRequest.apply(
                Set.of(RawDataConditionDAO.Attribute.CELL_TYPE_ID))
                .stream()
                .map(c -> c.getCellTypeId())
                //cell type is the only condition param that can be NULL,
                //we end up requesting an anat. entity with ID "NULL"
                .filter(s -> s != null)
                .collect(Collectors.toSet());
        //We merge both to make only one request to the anatEntityService
        Set<String> anatEntityCellTypeIds = new HashSet<>(anatEntityIds);
        anatEntityCellTypeIds.addAll(cellTypeIds);
        Set<AnatEntity> anatEntityCellTypes = anatEntityCellTypeIds.isEmpty()?
                new HashSet<>() : anatEntityService.loadAnatEntities(anatEntityCellTypeIds, false)
                .collect(Collectors.toSet());
        Set<AnatEntity> anatEntities = anatEntityCellTypes.stream()
                .filter(ae -> anatEntityIds.contains(ae.getId()))
                .collect(Collectors.toSet());
        Set<AnatEntity> cellTypes = anatEntityCellTypes.stream()
                .filter(ae -> cellTypeIds.contains(ae.getId()))
                .collect(Collectors.toSet());

        //retrieve dev. stages
        Set<String> stageIds = condRequest.apply(
                Set.of(RawDataConditionDAO.Attribute.STAGE_ID))
                .stream().map(c -> c.getStageId()).collect(Collectors.toSet());
        Set<DevStage> stages = stageIds.isEmpty()?
                new HashSet<>() : devStageService.loadDevStages(null, null, stageIds, false)
                .collect(Collectors.toSet());

        // retrieve strains
        Set<String> strains = condRequest.apply(
                Set.of(RawDataConditionDAO.Attribute.STRAIN))
                .stream().map(c -> c.getStrainId()).collect(Collectors.toSet());

        //retrieve sexes
        Set<RawDataSex> sexes = condRequest.apply(
                Set.of(RawDataConditionDAO.Attribute.SEX)).stream()
                .map(c -> mapDAORawDataSexToRawDataSex(c.getSex())).collect(Collectors.toSet());

        //retrieve species
        Set<Integer> speciesIds = condRequest.apply(
                Set.of(RawDataConditionDAO.Attribute.SPECIES_ID))
                .stream().map(c -> c.getSpeciesId()).collect(Collectors.toSet());
        Set<Species> species = speciesIds.isEmpty()?
                new HashSet<>() : this.getRawDataProcessedFilter().getSpeciesMap().values()
                .stream().filter(s -> speciesIds.contains(s.getId()))
                .collect(Collectors.toSet());
        assert speciesIds.size() == species.size();

        return log.traceExit(new RawDataPostFilter(anatEntities, stages, cellTypes,
                sexes, strains, species, null, null, dataType));
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

    private DAODataType convertRawDataDataTypeToDAODataType(RawDataDataType<?, ?> dt) {
        log.traceEntry("{}", dt);
        if (dt == null) {
            return log.traceExit((DAODataType) null);
        }
        if (dt instanceof AffymetrixDataType) {
            return log.traceExit(DAODataType.AFFYMETRIX);
        } else if (dt instanceof RnaSeqDataType) {
            return log.traceExit(DAODataType.RNA_SEQ);
        } else if (dt instanceof ESTDataType) {
            return log.traceExit(DAODataType.EST);
        } else if (dt instanceof InSituDataType) {
            return log.traceExit(DAODataType.IN_SITU);
        }
        throw log.throwing(new IllegalStateException("Unsupported RawDataDataType: " + dt));
    }
}