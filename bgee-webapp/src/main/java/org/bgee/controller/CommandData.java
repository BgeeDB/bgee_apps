package org.bgee.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.InvalidRequestException;
import org.bgee.controller.user.User;
import org.bgee.controller.utils.LRUCache;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.Sex;
import org.bgee.model.anatdev.Sex.SexEnum;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.rawdata.RawDataConditionFilter;
import org.bgee.model.expressiondata.rawdata.RawDataContainer;
import org.bgee.model.expressiondata.rawdata.RawDataCountContainer;
import org.bgee.model.expressiondata.rawdata.RawDataFilter;
import org.bgee.model.expressiondata.rawdata.RawDataLoader;
import org.bgee.model.expressiondata.rawdata.RawDataLoader.InformationType;
import org.bgee.model.expressiondata.rawdata.RawDataProcessedFilter;
import org.bgee.model.expressiondata.rawdata.RawDataService;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.job.Job;
import org.bgee.model.job.JobService;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.species.Species;
import org.bgee.model.species.SpeciesService;
import org.bgee.view.DataDisplay;
import org.bgee.view.ViewFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

/**
 * Controller that handles requests for the raw data page.
 *
 * @author  Frederic Bastian
 * @version Bgee 15.0, Oct. 2022
 * @since   Bgee 15.0, Oct. 2022
 */
public class CommandData extends CommandParent {
    private final static Logger log = LogManager.getLogger(CommandData.class.getName());

    public static class DataFormDetails {
        private final Species requestedSpecies;
        private final Ontology<DevStage, String> requestedSpeciesDevStageOntology;
        private final List<Sex> requestedSpeciesSexes;
        private final List<AnatEntity> requestedAnatEntitesAndCellTypes;
        private final List<Gene> requestedGenes;

        public DataFormDetails(Species requestedSpecies,
                Ontology<DevStage, String> requestedSpeciesDevStageOntology,
                List<Sex> requestedSpeciesSexes, List<AnatEntity> requestedAnatEntitesAndCellTypes,
                List<Gene> requestedGenes) {

            this.requestedSpecies = requestedSpecies;
            this.requestedSpeciesDevStageOntology = requestedSpeciesDevStageOntology;
            this.requestedSpeciesSexes = Collections.unmodifiableList(requestedSpeciesSexes == null?
                    new ArrayList<>(): new ArrayList<>(requestedSpeciesSexes));
            this.requestedAnatEntitesAndCellTypes = Collections.unmodifiableList(
                    requestedAnatEntitesAndCellTypes == null?
                    new ArrayList<>(): new ArrayList<>(requestedAnatEntitesAndCellTypes));
            this.requestedGenes = Collections.unmodifiableList(requestedGenes == null?
                    new ArrayList<>(): new ArrayList<>(requestedGenes));
        }

        public Species getRequestedSpecies() {
            return requestedSpecies;
        }
        public Ontology<DevStage, String> getRequestedSpeciesDevStageOntology() {
            return requestedSpeciesDevStageOntology;
        }
        public List<Sex> getRequestedSpeciesSexes() {
            return requestedSpeciesSexes;
        }
        public List<AnatEntity> getRequestedAnatEntitesAndCellTypes() {
            return requestedAnatEntitesAndCellTypes;
        }
        public List<Gene> getRequestedGenes() {
            return requestedGenes;
        }

        public boolean containsAnyInformation() {
            return this.getRequestedSpeciesDevStageOntology() != null ||
                    !this.getRequestedSpeciesSexes().isEmpty() ||
                    !this.getRequestedAnatEntitesAndCellTypes().isEmpty() ||
                    !this.getRequestedGenes().isEmpty();
        }
    }

    /**
     * An {@code int} that is the maximum allowed number of results
     * to retrieve in one request, for each requested data type independently.
     * Value: 10,000.
     */
    private final static int LIMIT_MAX = 10000;
    /**
     * An {@code int} that is the default number of results
     * to retrieve in one request, for each requested data type independently.
     * Value: 100.
     */
    private final static int DEFAULT_LIMIT = 100;
    /**
     * A {@code Map} used as a LRU cache to retrieve {@code RawDataProcessedFilter} from a
     * {@code RawDataFilter}. The {@code Map} can hold max 20 entries.
     * The {@code Map} is thread-safe by using the method {@code Collections.synchronizedMap},
     * and is backed-up by a {@link org.bgee.controller.utils.LRUCache LRUCache}.
     * Maybe we should use a Guava cache instead.
     */
    private final static Map<RawDataFilter, RawDataProcessedFilter> RAW_DATA_PROCESSED_FILTER_CACHE =
            Collections.synchronizedMap(new LRUCache<RawDataFilter, RawDataProcessedFilter>(20));

    //Static initializer
    {
        if (LIMIT_MAX > RawDataLoader.LIMIT_MAX) {
            throw log.throwing(new IllegalStateException("The maximum limit allowed by this controller "
                    + "is greater than the maximum limit allowed by the RawDataLoader."));
        }
    }

    private final SpeciesService speciesService;

    /**
     * Constructor
     *
     * @param response          A {@code HttpServletResponse} that will be used to display the 
     *                          page to the client
     * @param requestParameters The {@code RequestParameters} that handles the parameters of the 
     *                          current request.
     * @param prop              A {@code BgeeProperties} instance that contains the properties to use.
     * @param viewFactory       A {@code ViewFactory} that provides the display type to be used.
     * @param serviceFactory    A {@code ServiceFactory} that provides the services to be used.
     */
    public CommandData(HttpServletResponse response, RequestParameters requestParameters,
                          BgeeProperties prop, ViewFactory viewFactory, ServiceFactory serviceFactory,
                          JobService jobService, User user) {
        super(response, requestParameters, prop, viewFactory, serviceFactory, jobService, user,
                null, null);
        this.speciesService = this.serviceFactory.getSpeciesService();
    }

    @Override
    public void processRequest() throws Exception {
        log.traceEntry();

        //Species list
        List<Species> speciesList = null;
        if (this.requestParameters.isGetSpeciesList()) {
            speciesList = this.loadSpeciesList();
        }

        //Form details
        DataFormDetails formDetails = null;
        if (this.requestParameters.isDetailedRequestParameters()) {
            Ontology<DevStage, String> requestedSpeciesDevStageOntology = null;
            List<Sex> requestedSpeciesSexes = null;
            List<AnatEntity> requestedAnatEntitesAndCellTypes = null;
            List<Gene> requestedGenes = null;

            Species requestedSpecies = this.loadRequestedSpecies();
            List<String> requestedGeneIds = this.requestParameters.getGeneIds();

            if (requestedSpecies != null) {
                int speciesId = requestedSpecies.getId();
                requestedSpeciesDevStageOntology = this.loadSpeciesStageOntology(speciesId);
                requestedSpeciesSexes = this.loadSpeciesSexes(speciesId);
                requestedGenes = this.loadRequestedGenes(speciesId, requestedGeneIds);

            } else if (requestedGeneIds != null && !requestedGeneIds.isEmpty()) {
                throw log.throwing(new InvalidRequestException(
                        "A species ID must be provided to query genes"));
            }
            requestedAnatEntitesAndCellTypes = this.loadRequestedAnatEntitesAndCellTypes();

            formDetails = new DataFormDetails(requestedSpecies, requestedSpeciesDevStageOntology,
                    requestedSpeciesSexes, requestedAnatEntitesAndCellTypes, requestedGenes);
        }

        //Actions: raw data results, processed expression values
        RawDataContainer rawDataContainer = null;
        RawDataCountContainer rawDataCountContainer = null;
        if (RequestParameters.ACTION_RAW_DATA_ANNOTS.equals(this.requestParameters.getAction()) ||
                RequestParameters.ACTION_PROC_EXPR_VALUES.equals(this.requestParameters.getAction())) {
            log.debug("Action identified: {}", this.requestParameters.getAction());

            EnumSet<DataType> dataTypes = this.checkAndGetDataTypes();

            //Queries that required a RawDataLoader
            if (this.requestParameters.isGetResults() || this.requestParameters.isGetResultCount() ||
                    this.requestParameters.isGetFilters()) {
                log.debug("Loading RawDataLoader");
                //try...finally block to manage number of jobs per users,
                //to limit the concurrent number of queries a user can make
                Job job = null;
                try {
                    job = this.jobService.registerNewJob(this.user.getUUID().toString());
                    job.startJob();
                    RawDataLoader rawDataLoader = this.loadRawDataLoader();

                    //Raw data results
                    if (this.requestParameters.isGetResults()) {
                        rawDataContainer = this.loadRawDataResults(rawDataLoader, dataTypes);
                    }
                    //Raw data counts
                    if (this.requestParameters.isGetResultCount()) {
                        rawDataCountContainer = this.loadRawDataCounts(rawDataLoader, dataTypes);
                    }

                    job.completeWithSuccess();
                } finally {
                    if (job != null) {
                        job.release();
                    }
                }
            }
        }

        DataDisplay display = viewFactory.getDataDisplay();
        display.displayDataPage(speciesList, formDetails, rawDataContainer, rawDataCountContainer);

        log.traceExit();
    }

    private List<Species> loadSpeciesList() {
        log.traceEntry();
        return log.traceExit(this.speciesService.loadSpeciesByIds(null, false)
                .stream()
                .sorted(Comparator.comparing(Species::getPreferredDisplayOrder))
                .collect(Collectors.toList()));
    }

    private Species loadRequestedSpecies() throws InvalidRequestException {
        log.traceEntry();
        Integer requestedSpeciesId = this.requestParameters.getSpeciesId();
        if (requestedSpeciesId == null) {
            return log.traceExit((Species) null);
        }
        Species species = this.speciesService.loadSpeciesByIds(Set.of(requestedSpeciesId), false)
                .stream().findAny().orElse(null);
        if (species == null) {
            throw log.throwing(new InvalidRequestException("No species corresponding to ID "
                    + requestedSpeciesId));
        }
        return log.traceExit(species);
    }

    private Ontology<DevStage, String> loadSpeciesStageOntology(int speciesId) {
        log.traceEntry("{}", speciesId);

        Set<DevStage> stages = this.serviceFactory.getDevStageService()
                .loadGroupingDevStages(Set.of(speciesId), null);

        return log.traceExit(this.serviceFactory.getOntologyService()
                .getDevStageOntologyFromDevStages(Set.of(speciesId), stages, false, false)
                .getAsSingleSpeciesOntology(speciesId));
    }

    private List<Sex> loadSpeciesSexes(int speciesId) {
        log.traceEntry("{}", speciesId);

        return log.traceExit(this.serviceFactory.getSexService().loadSexesBySpeciesId(speciesId).stream()
                // We filter out the "any". Users will either:
                // * select no sex to retrieve all results, including "mixed" or "NA"
                // * select all sexes (male, female, hermaphrodite) to retrieve all defined information
                // => there will be no possibility to select "other" (for retrieving only annotations
                // such as "mixed" or "NA".
                .filter(sex -> !sex.getId().equalsIgnoreCase(SexEnum.ANY.getStringRepresentation()))
                //Sort by their EnumSex representation for consistent ordering
                .sorted((s1, s2) -> SexEnum.convertToSexEnum(s1.getId()).compareTo(SexEnum.convertToSexEnum(s2.getId())))
                .collect(Collectors.toList()));
    }

    private List<AnatEntity> loadRequestedAnatEntitesAndCellTypes() throws InvalidRequestException {
        log.traceEntry();

        Set<String> anatEntityAndCellTypeIds = new HashSet<>();
        if (this.requestParameters.getAnatEntity() != null) {
            anatEntityAndCellTypeIds.addAll(this.requestParameters.getAnatEntity());
        }
        if (this.requestParameters.getCellType() != null) {
            anatEntityAndCellTypeIds.addAll(this.requestParameters.getCellType());
        }
        if (anatEntityAndCellTypeIds.isEmpty()) {
            return log.traceExit((List<AnatEntity>) null);
        }

        List<AnatEntity> anatEntities = this.serviceFactory.getAnatEntityService()
                .loadAnatEntities(anatEntityAndCellTypeIds, false)
                .sorted(Comparator.comparing(ae -> ae.getName()))
                .collect(Collectors.toList());
        if (anatEntities.size() != anatEntityAndCellTypeIds.size()) {
            Set<String> retrievedIds = anatEntities.stream()
                    .map(ae -> ae.getId())
                    .collect(Collectors.toSet());
            anatEntityAndCellTypeIds.removeAll(retrievedIds);
            throw log.throwing(new InvalidRequestException(
                    "Some anatomical entities or cell types could not be identified: "
                    + anatEntityAndCellTypeIds));
        }

        return log.traceExit(anatEntities);
    }

    private List<Gene> loadRequestedGenes(int speciesId, Collection<String> requestedGeneIds) throws InvalidRequestException {
        log.traceEntry();
        if (requestedGeneIds == null || requestedGeneIds.isEmpty()) {
            return log.traceExit((List<Gene>) null);
        }
        Set<String> clonedGeneIds = new HashSet<>(requestedGeneIds);

        GeneFilter filter = new GeneFilter(speciesId, clonedGeneIds);
        List<Gene> genes = this.serviceFactory.getGeneService().loadGenes(filter)
                .sorted(Comparator.<Gene, String>comparing(g -> g.getName())
                        .thenComparing(Comparator.comparing(g -> g.getGeneId())))
                .collect(Collectors.toList());

        if (genes.size() != clonedGeneIds.size()) {
            Set<String> retrieveGeneIds = genes.stream().map(g -> g.getGeneId())
                    .collect(Collectors.toSet());
            clonedGeneIds.removeAll(retrieveGeneIds);
            throw log.throwing(new InvalidRequestException(
                    "Some genes could not be identified: " + clonedGeneIds));
        }
        return log.traceExit(genes);
    }

    private RawDataLoader loadRawDataLoader() {
        log.traceEntry();

        RawDataFilter filter = this.loadRawDataFilter();
        RawDataService rawDataService = this.serviceFactory.getRawDataService();
        //Try to get the processed filter from the cache.
        //We don't use the method computeIfAbsent, because that would probably block
        //the whole cache while the computation of RawDataProcessedFilter is done,
        //since we simply used Collections.synchronizedMap to make the cache thread-safe.
        //It is a simple optimization, we don't care so much if several threads
        //are computing the same RawDataProcessedFilter.
        RawDataProcessedFilter processedFilter = RAW_DATA_PROCESSED_FILTER_CACHE.get(filter);
        if (processedFilter == null) {
            processedFilter = rawDataService.processRawDataFilter(filter);
            RAW_DATA_PROCESSED_FILTER_CACHE.putIfAbsent(filter, processedFilter);
        }
        return log.traceExit(rawDataService.getRawDataLoader(processedFilter));
    }

    private RawDataFilter loadRawDataFilter() {
        log.traceEntry();

        GeneFilter geneFilter = null;
        RawDataConditionFilter condFilter = null;
        Collection<String> expOrAssayIds = this.requestParameters.getExpAssayId();

        if (this.requestParameters.getSpeciesId() != null) {
            int speciesId = this.requestParameters.getSpeciesId();

            geneFilter = new GeneFilter(speciesId, this.requestParameters.getGeneIds());

            condFilter = new RawDataConditionFilter(speciesId,
                    this.requestParameters.getAnatEntity(),
                    this.requestParameters.getDevStage(),
                    this.requestParameters.getCellType(),
                    this.requestParameters.getSex(),
                    this.requestParameters.getStrain(),
                    //includeSubAnatEntities
                    Boolean.TRUE.equals(this.requestParameters.getFirstValue(
                            this.requestParameters.getUrlParametersInstance().getParamAnatEntityDescendant())),
                    //includeSubDevStages
                    Boolean.TRUE.equals(this.requestParameters.getFirstValue(
                            this.requestParameters.getUrlParametersInstance().getParamStageDescendant())),
                    //includeSubCellTypes
                    Boolean.TRUE.equals(this.requestParameters.getFirstValue(
                            this.requestParameters.getUrlParametersInstance().getParamCellTypeDescendant())),
                    //sex descendant always false: requesting descendants of the root is equivalent
                    //to request all sexes, in which case we don't provide requested sex IDs
                    false,
                    //strain descendant always false: requesting descendants of the root is equivalent
                    //to request all strains, in which case we don't provide requested strains
                    false);
        }

        return log.traceExit(new RawDataFilter(
                geneFilter != null? Collections.singleton(geneFilter): null,
                condFilter != null? Collections.singleton(condFilter): null,
                null, null, expOrAssayIds));
    }

    private RawDataContainer loadRawDataResults(RawDataLoader rawDataLoader, EnumSet<DataType> dataTypes)
            throws InvalidRequestException {
        log.traceEntry("{}, {}", rawDataLoader, dataTypes);

        Integer limit = this.requestParameters.getLimit();
        if (limit == null) {
            //The validity of DEFAULT_LIMIT is checked already in the static initializer
            limit = DEFAULT_LIMIT;
        } else if (limit > LIMIT_MAX) {
            throw log.throwing(new InvalidRequestException("It is not possible to request more than "
                    + LIMIT_MAX + " results."));
        }
        Integer offset = this.requestParameters.getOffset();
        if (offset == null) {
            offset = 0;
        } else if (offset < 0) {
            throw log.throwing(new InvalidRequestException("Offset cannot be less than 0."));
        }

        if (this.requestParameters.getAction() == null) {
            throw log.throwing(new IllegalStateException("Wrong null value for parameter action"));
        }
        InformationType infoType = null;
        switch (this.requestParameters.getAction()) {
        case RequestParameters.ACTION_RAW_DATA_ANNOTS:
            infoType = InformationType.ASSAY;
            break;
        case RequestParameters.ACTION_PROC_EXPR_VALUES:
            infoType = InformationType.CALL;
            break;
        default:
            throw log.throwing(new UnsupportedOperationException("Unsupported action: "
                    + this.requestParameters.getAction()));
        }

        return log.traceExit(rawDataLoader.loadData(infoType, dataTypes, offset, limit));
    }

    private RawDataCountContainer loadRawDataCounts(RawDataLoader rawDataLoader,
            EnumSet<DataType> dataTypes) {
        log.traceEntry("{}, {}", rawDataLoader, dataTypes);

        if (this.requestParameters.getAction() == null) {
            throw log.throwing(new IllegalStateException("Wrong null value for parameter action"));
        }
        EnumSet<InformationType> infoTypes = EnumSet.of(InformationType.EXPERIMENT, InformationType.ASSAY);
        if (RequestParameters.ACTION_PROC_EXPR_VALUES.equals(this.requestParameters.getAction())) {
            infoTypes.add(InformationType.CALL);
        }
        return log.traceExit(rawDataLoader.loadDataCount(infoTypes, dataTypes));
    }
}