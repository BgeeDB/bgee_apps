package org.bgee.controller;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.InvalidRequestException;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.controller.user.User;
import org.bgee.controller.utils.LRUCache;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.Sex;
import org.bgee.model.anatdev.Sex.SexEnum;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.rawdata.baseelements.Assay;
import org.bgee.model.expressiondata.rawdata.baseelements.Experiment;
import org.bgee.model.expressiondata.rawdata.baseelements.ExperimentAssay;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataContainer;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataContainerWithExperiment;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataCountContainer;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataDataType;
import org.bgee.model.expressiondata.rawdata.RawDataConditionFilter;
import org.bgee.model.expressiondata.rawdata.RawDataFilter;
import org.bgee.model.expressiondata.rawdata.RawDataLoader;
import org.bgee.model.expressiondata.rawdata.RawDataLoader.InformationType;
import org.bgee.model.expressiondata.rawdata.RawDataPostFilter;
import org.bgee.model.expressiondata.rawdata.RawDataProcessedFilter;
import org.bgee.model.expressiondata.rawdata.RawDataService;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.job.Job;
import org.bgee.model.job.JobService;
import org.bgee.model.job.exception.ThreadAlreadyWorkingException;
import org.bgee.model.job.exception.TooManyJobsException;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.search.SearchMatchResultService;
import org.bgee.model.species.Species;
import org.bgee.model.species.SpeciesService;
import org.bgee.view.DataDisplay;
import org.bgee.view.ViewFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
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

    public static class ColumnDescription {
        public static enum ColumnType {
            STRING, NUMERIC, INTERNAL_LINK, EXTERNAL_LINK, ANAT_ENTITY, DEV_STAGE
        }
        public static final String INTERNAL_LINK_TARGET_EXP = "experiment";
        public static final String INTERNAL_LINK_TARGET_GENE = "gene";
        private static final Set<String> INTERNAL_LINK_TARGETS = Set.of(
                INTERNAL_LINK_TARGET_EXP, INTERNAL_LINK_TARGET_GENE);

        private final String title;
        private final String infoBubble;
        private final List<String> attributes;
        private final ColumnType columnType;
        /**
         * Only applicable when {@code columnType} is {@code INTERNAL_LINK}.
         */
        private final String linkTarget;

        public ColumnDescription(String title, String infoBubble, List<String> attributes,
                ColumnType columnType, String linkTarget) {
            if (columnType == null) {
                throw log.throwing(new IllegalArgumentException(
                        "a column type is mandatory"));
            }
            if (!ColumnType.INTERNAL_LINK.equals(columnType) && StringUtils.isNotBlank(linkTarget)) {
                throw log.throwing(new IllegalArgumentException(
                        "linkTarget only applicable when columnType is INTERNAL_LINK"));
            } else if (ColumnType.INTERNAL_LINK.equals(columnType) && StringUtils.isBlank(linkTarget)) {
                throw log.throwing(new IllegalArgumentException(
                        "linkTarget must be defined when columnType is INTERNAL_LINK"));
            }
            if (StringUtils.isNotBlank(linkTarget) && !INTERNAL_LINK_TARGETS.contains(linkTarget)) {
                throw log.throwing(new IllegalArgumentException(
                        "Invalid value for linkTarget: " + linkTarget));
            }
            if (StringUtils.isBlank(title)) {
                throw log.throwing(new IllegalArgumentException(
                        "title of the column is mandatory"));
            }
            if (attributes == null || attributes.isEmpty()) {
                throw log.throwing(new IllegalArgumentException(
                        "a list of attributes is mandatory"));
            }
            this.title = title;
            this.infoBubble = infoBubble;
            this.attributes = Collections.unmodifiableList(attributes);
            this.columnType = columnType;
            this.linkTarget = linkTarget;
        }

        public String getTitle() {
            return title;
        }
        public String getInfoBubble() {
            return infoBubble;
        }
        public List<String> getAttributes() {
            return attributes;
        }
        public ColumnType getColumnType() {
            return columnType;
        }
        public String getLinkTarget() {
            return linkTarget;
        }
    }

    public static class DataFormDetails {
        private final Species requestedSpecies;
        private final Ontology<DevStage, String> requestedSpeciesDevStageOntology;
        private final List<Sex> requestedSpeciesSexes;
        private final List<AnatEntity> requestedAnatEntitesAndCellTypes;
        private final List<Gene> requestedGenes;
        private final List<ExperimentAssay> requestedExperimentAndAssays;

        public DataFormDetails(Species requestedSpecies,
                Ontology<DevStage, String> requestedSpeciesDevStageOntology,
                List<Sex> requestedSpeciesSexes, List<AnatEntity> requestedAnatEntitesAndCellTypes,
                List<Gene> requestedGenes, List<ExperimentAssay> requestedExperimentAndAssays) {

            this.requestedSpecies = requestedSpecies;
            this.requestedSpeciesDevStageOntology = requestedSpeciesDevStageOntology;
            this.requestedSpeciesSexes = Collections.unmodifiableList(requestedSpeciesSexes == null?
                    new ArrayList<>(): new ArrayList<>(requestedSpeciesSexes));
            this.requestedAnatEntitesAndCellTypes = Collections.unmodifiableList(
                    requestedAnatEntitesAndCellTypes == null?
                    new ArrayList<>(): new ArrayList<>(requestedAnatEntitesAndCellTypes));
            this.requestedGenes = Collections.unmodifiableList(requestedGenes == null?
                    new ArrayList<>(): new ArrayList<>(requestedGenes));
            this.requestedExperimentAndAssays = Collections.unmodifiableList(
                    requestedExperimentAndAssays == null?
                    new ArrayList<>(): new ArrayList<>(requestedExperimentAndAssays));
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
        public List<ExperimentAssay> getRequestedExperimentAndAssays() {
            return requestedExperimentAndAssays;
        }

        public boolean containsAnyInformation() {
            return this.getRequestedSpeciesDevStageOntology() != null ||
                    !this.getRequestedSpeciesSexes().isEmpty() ||
                    !this.getRequestedAnatEntitesAndCellTypes().isEmpty() ||
                    !this.getRequestedGenes().isEmpty() ||
                    !this.getRequestedExperimentAndAssays().isEmpty();
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
        if (DEFAULT_LIMIT > LIMIT_MAX) {
            throw log.throwing(new IllegalStateException("The default limit is greater than the max. limit."));
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
        DataFormDetails formDetails = this.loadFormDetails();

        //Actions: experiment list, raw data results, processed expression values
        if (RequestParameters.ACTION_EXPERIMENTS.equals(this.requestParameters.getAction()) ||
                RequestParameters.ACTION_RAW_DATA_ANNOTS.equals(this.requestParameters.getAction()) ||
                RequestParameters.ACTION_PROC_EXPR_VALUES.equals(this.requestParameters.getAction())) {

            this.processRawDataPage(speciesList, formDetails);

        } else if (this.requestParameters.getExperimentId() != null) {

            this.processExperimentPage();

        } else if (speciesList != null || formDetails != null) {
            DataDisplay display = viewFactory.getDataDisplay();
            display.displayDataPage(speciesList, formDetails);
        } else {
            throw log.throwing(new InvalidRequestException(
                    "The request does not have any mandatory parameter"));
        }

        log.traceExit();
    }

    private void processRawDataPage(List<Species> speciesList, DataFormDetails formDetails)
            throws InvalidRequestException, ThreadAlreadyWorkingException,
            TooManyJobsException, IOException {
        log.traceEntry("{}, {}", speciesList, formDetails);

        log.debug("Action identified: {}", this.requestParameters.getAction());
        EnumMap<DataType, RawDataContainer<?, ?>> rawDataContainers = null;
        EnumMap<DataType, RawDataCountContainer> rawDataCountContainers = null;
        EnumMap<DataType, RawDataPostFilter> rawDataPostFilters = null;
        EnumMap<DataType, List<ColumnDescription>> colDescriptions = null;

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
                    rawDataContainers = this.loadRawDataResults(rawDataLoader, dataTypes);
                }
                //Raw data counts
                if (this.requestParameters.isGetResultCount()) {
                    rawDataCountContainers = this.loadRawDataCounts(rawDataLoader, dataTypes);
                }
                //Filters
                if (this.requestParameters.isGetFilters()) {
                    rawDataPostFilters = this.loadRawDataPostFilters(rawDataLoader, dataTypes);
                }

                job.completeWithSuccess();
            } finally {
                if (job != null) {
                    job.release();
                }
            }
        }
        if (this.requestParameters.isGetColumnDefinition()) {
            colDescriptions = this.getColumnDescriptions(
                    this.requestParameters.getAction(), dataTypes);
        }
        DataDisplay display = viewFactory.getDataDisplay();
        display.displayDataPage(speciesList, formDetails, colDescriptions,
                rawDataContainers, rawDataCountContainers, rawDataPostFilters);

        log.traceExit();
    }

    private void processExperimentPage() throws PageNotFoundException, IOException {
        log.traceEntry();

        //We don't use the loadRawDataLoader method, because there is no complex processing
        //of the RawDataFilter, so that we don't want to put it in cache
        RawDataLoader rawDataLoader = this.serviceFactory.getRawDataService()
                .loadRawDataLoader(this.loadRawDataFilter());

        //We don't know which data type the experiment belongs to,
        //so we test them all
        DataType dataTypeWithResults = null;
        RawDataContainerWithExperiment<?, ?, ?> container = null;
        for (DataType dt: EnumSet.allOf(DataType.class)) {
            RawDataDataType<? extends RawDataContainerWithExperiment<?, ?, ?>, ?> rdt =
                    RawDataDataType.getRawDataDataTypeWithExperiment(dt);
            //we skip data types that have no experiments
            if (rdt == null) {
                continue;
            }

            container = rawDataLoader.loadData(
                    //We want the assays to be displayed on the experiment page,
                    //The experiment itself will be retrieved along the way
                    InformationType.ASSAY,
                    rdt,
                    0,
                    //RawDataLoader.LIMIT_MAX should always be defined to remain above
                    //the max number of assays in an experiment
                    RawDataLoader.LIMIT_MAX);
            if (!container.getAssays().isEmpty()) {
                //we found our result
                dataTypeWithResults = dt;
                break;
            }
            //otherwise we continue to search
            container = null;
        }
        if (container == null) {
            throw log.throwing(new PageNotFoundException("The experiment ID "
                    + this.requestParameters.getExperimentId() + " does not exist in Bgee."));
        }

        if (container.getExperiments().size() != 1) {
            throw log.throwing(new IllegalStateException(
                    "Ambiguous experiment ID, should not happen. Experiments retrieved: "
                    + container.getExperiments()));
        }
        assert dataTypeWithResults != null;
        Experiment<?> experiment = container.getExperiments().iterator().next();
        LinkedHashSet<Assay> assays = new LinkedHashSet<>(container.getAssays());
        List<ColumnDescription> colDescr;
        try {
            colDescr = this.getColumnDescriptions(
                    RequestParameters.ACTION_RAW_DATA_ANNOTS, EnumSet.of(dataTypeWithResults))
                    .get(dataTypeWithResults);
        } catch (InvalidRequestException e) {
            //here it means we didn't correctly called the method getColumnDescriptions,
            //it is not an InvalidRequestException
            throw log.throwing(new IllegalStateException(e));
        }

        DataDisplay display = viewFactory.getDataDisplay();
        display.displayExperimentPage(experiment, assays, dataTypeWithResults, colDescr);

        log.traceExit();
    }

    private List<Species> loadSpeciesList() {
        log.traceEntry();
        return log.traceExit(this.speciesService.loadSpeciesByIds(null, false)
                .stream()
                .sorted(Comparator.comparing(Species::getPreferredDisplayOrder))
                .collect(Collectors.toList()));
    }

    private DataFormDetails loadFormDetails() throws InvalidRequestException {
        log.traceEntry();

        DataFormDetails formDetails = null;
        if (this.requestParameters.isDetailedRequestParameters()) {
            Ontology<DevStage, String> requestedSpeciesDevStageOntology = null;
            List<Sex> requestedSpeciesSexes = null;
            List<AnatEntity> requestedAnatEntitesAndCellTypes = null;
            List<Gene> requestedGenes = null;
            List<ExperimentAssay> expAssays = null;

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
            expAssays = this.loadRequestedExperimentsAndAssays();

            formDetails = new DataFormDetails(requestedSpecies, requestedSpeciesDevStageOntology,
                    requestedSpeciesSexes, requestedAnatEntitesAndCellTypes, requestedGenes, expAssays);
        }

        return log.traceExit(formDetails);
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

    private List<ExperimentAssay> loadRequestedExperimentsAndAssays() throws InvalidRequestException {
        log.traceEntry();

        Set<String> expAssayIds = this.requestParameters.getExpAssayId() == null? new HashSet<>():
            new HashSet<>(this.requestParameters.getExpAssayId());
        if (expAssayIds.isEmpty()) {
            return log.traceExit((List<ExperimentAssay>) null);
        }
        //ExperimentAssay is not a real object in the data source,
        //it was created for convenience for autocomplete searches.
        //For this reason, for now we keep on using the search tool,
        //rather than formally using a RawDataLoader to query experiments and assays
        //for each data type
        SearchMatchResultService service = this.serviceFactory.getSearchMatchResultService(this.prop);
        List<ExperimentAssay> results = expAssayIds.stream()
                .flatMap(id -> service.searchExperimentsAndAssaysByTerm(id, null, null)
                        .getSearchMatches().stream().map(sm -> sm.getSearchedObject()))
                .filter(ea -> expAssayIds.contains(ea.getId()))
                .collect(Collectors.toList());
        if (results.size() != expAssayIds.size()) {
            Set<String> retrievedIds = results.stream()
                    .map(ea -> ea.getId())
                    .collect(Collectors.toSet());
            expAssayIds.removeAll(retrievedIds);
            throw log.throwing(new InvalidRequestException(
                    "Some experiment or assay IDs could not be identified: "
                    + expAssayIds));
        }

        return log.traceExit(results);
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
            log.debug("Cache miss for filter: {}", filter);
            processedFilter = rawDataService.processRawDataFilter(filter);
            RAW_DATA_PROCESSED_FILTER_CACHE.putIfAbsent(filter, processedFilter);
        } else {
            log.debug("Cache hit for filter: {} - value: {}", filter, processedFilter);
        }
        return log.traceExit(rawDataService.getRawDataLoader(processedFilter));
    }

    private RawDataFilter loadRawDataFilter() {
        log.traceEntry();

        GeneFilter geneFilter = null;
        RawDataConditionFilter condFilter = null;
        Collection<String> expOrAssayIds = this.requestParameters.getExpAssayId();
        String experimentId = this.requestParameters.getExperimentId();

        if (this.requestParameters.getSpeciesId() != null) {
            int speciesId = this.requestParameters.getSpeciesId();

            geneFilter = new GeneFilter(speciesId, this.requestParameters.getGeneIds());

            List<String> sexes = this.requestParameters.getSex();
            if (sexes.contains(RequestParameters.ALL_VALUE) ||
                    sexes.containsAll(
                            EnumSet.allOf(SexEnum.class)
                            .stream()
                            .map(e -> e.name())
                            .collect(Collectors.toSet()))) {
                sexes = null;
            }

            condFilter = new RawDataConditionFilter(speciesId,
                    this.requestParameters.getAnatEntity(),
                    this.requestParameters.getDevStage(),
                    this.requestParameters.getCellType(),
                    sexes,
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
                experimentId != null? Collections.singleton(experimentId): null,
                null, //assay IDs
                expOrAssayIds));
    }

    private EnumMap<DataType, RawDataContainer<?, ?>> loadRawDataResults(RawDataLoader rawDataLoader,
            EnumSet<DataType> dataTypes) throws InvalidRequestException {
        log.traceEntry("{}, {}", rawDataLoader, dataTypes);

        Integer limit = this.requestParameters.getLimit() == null? DEFAULT_LIMIT:
            this.requestParameters.getLimit();
        if (limit > LIMIT_MAX) {
            throw log.throwing(new InvalidRequestException("It is not possible to request more than "
                    + LIMIT_MAX + " results."));
        }
        Integer offset = this.requestParameters.getOffset();
        if (offset != null && offset < 0) {
            throw log.throwing(new InvalidRequestException("Offset cannot be less than 0."));
        }

        if (this.requestParameters.getAction() == null) {
            throw log.throwing(new IllegalStateException("Wrong null value for parameter action"));
        }
        InformationType infoType = null;
        switch (this.requestParameters.getAction()) {
        case RequestParameters.ACTION_EXPERIMENTS:
            infoType = InformationType.EXPERIMENT;
            break;
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
        InformationType finalInfoType = infoType;
        return log.traceExit(dataTypes.stream()
                //TODO: remove this filter when all data types will be implemented
                .filter(dt -> dt == DataType.AFFYMETRIX ||
                              dt == DataType.RNA_SEQ || dt == DataType.FULL_LENGTH)
                .collect(Collectors.toMap(
                        dt -> dt,
                        dt -> rawDataLoader.loadData(finalInfoType,
                                RawDataDataType.getRawDataDataType(dt), offset, limit),
                        (v1, v2) -> {throw new IllegalStateException("Key collision impossible");},
                        () -> new EnumMap<>(DataType.class))));
    }

    private EnumMap<DataType, RawDataCountContainer> loadRawDataCounts(RawDataLoader rawDataLoader,
            EnumSet<DataType> dataTypes) {
        log.traceEntry("{}, {}", rawDataLoader, dataTypes);

        if (this.requestParameters.getAction() == null) {
            throw log.throwing(new IllegalStateException("Wrong null value for parameter action"));
        }
        EnumSet<InformationType> infoTypes = EnumSet.of(InformationType.EXPERIMENT);
        if (RequestParameters.ACTION_RAW_DATA_ANNOTS.equals(this.requestParameters.getAction()) ||
                RequestParameters.ACTION_PROC_EXPR_VALUES.equals(this.requestParameters.getAction())) {
            infoTypes.add(InformationType.ASSAY);
        }
        if (RequestParameters.ACTION_PROC_EXPR_VALUES.equals(this.requestParameters.getAction())) {
            infoTypes.add(InformationType.CALL);
        }
        return log.traceExit(dataTypes.stream()
                //TODO: remove this filter when all data types will be implemented
                .filter(dt -> dt == DataType.AFFYMETRIX ||
                        dt == DataType.RNA_SEQ || dt == DataType.FULL_LENGTH)
                .collect(Collectors.toMap(
                        dt -> dt,
                        dt -> rawDataLoader.loadDataCount(infoTypes,
                                RawDataDataType.getRawDataDataType(dt)),
                        (v1, v2) -> {throw new IllegalStateException("Key collision impossible");},
                        () -> new EnumMap<>(DataType.class))));
    }

    private EnumMap<DataType, RawDataPostFilter> loadRawDataPostFilters(RawDataLoader rawDataLoader,
            EnumSet<DataType> dataTypes) {
        log.traceEntry("{}, {}", rawDataLoader, dataTypes);

        return log.traceExit(dataTypes.stream()
                //TODO: remove this filter when all data types will be implemented
                .filter(dt -> dt == DataType.AFFYMETRIX ||
                        dt == DataType.RNA_SEQ || dt == DataType.FULL_LENGTH)
                .collect(Collectors.toMap(
                        dt -> dt,
                        dt -> rawDataLoader.loadPostFilter(RawDataDataType.getRawDataDataType(dt)),
                        (v1, v2) -> {throw new IllegalStateException("Key collision impossible");},
                        () -> new EnumMap<>(DataType.class))));
    }

    private EnumMap<DataType, List<ColumnDescription>> getColumnDescriptions(String action,
            EnumSet<DataType> dataTypes) throws InvalidRequestException {
        log.traceEntry("{}, {}", action, dataTypes);
        EnumMap<DataType, List<ColumnDescription>> dataTypeToColDescr = new EnumMap<>(DataType.class);
        EnumMap<DataType, Supplier<List<ColumnDescription>>> dataTypeTolDescrSupplier =
                new EnumMap<>(DataType.class);

        if (RequestParameters.ACTION_RAW_DATA_ANNOTS.equals(action)) {
            dataTypeTolDescrSupplier.put(DataType.AFFYMETRIX,
                    () -> getAffymetrixRawDataAnnotsColumnDescriptions());
            dataTypeTolDescrSupplier.put(DataType.RNA_SEQ,
                    () -> getRnaSeqRawDataAnnotsColumnDescriptions(false));
            dataTypeTolDescrSupplier.put(DataType.FULL_LENGTH,
                    () -> getRnaSeqRawDataAnnotsColumnDescriptions(true));
        } else if (RequestParameters.ACTION_PROC_EXPR_VALUES.equals(action)) {
            dataTypeTolDescrSupplier.put(DataType.AFFYMETRIX,
                    () -> getAffymetrixProcExprValuesColumnDescriptions());
            dataTypeTolDescrSupplier.put(DataType.RNA_SEQ,
                    () -> getRnaSeqProcExprValuesColumnDescriptions());
            dataTypeTolDescrSupplier.put(DataType.FULL_LENGTH,
                    () -> getRnaSeqProcExprValuesColumnDescriptions());
        } else if (RequestParameters.ACTION_EXPERIMENTS.equals(action)) {
            dataTypeTolDescrSupplier.put(DataType.AFFYMETRIX,
                    () -> getAffymetrixExperimentsColumnDescriptions());
            dataTypeTolDescrSupplier.put(DataType.RNA_SEQ,
                    () -> getRnaSeqExperimentsColumnDescriptions());
            dataTypeTolDescrSupplier.put(DataType.FULL_LENGTH,
                    () -> getRnaSeqExperimentsColumnDescriptions());
        } else {
            throw log.throwing(new InvalidRequestException("Unsupported action for column definition: " +
                    this.requestParameters.getAction()));
        }

        for (DataType dataType: dataTypes) {
            //TODO: remove this check when all data types will be implemented
            if (dataType != DataType.AFFYMETRIX
                    && dataType != DataType.RNA_SEQ && dataType != DataType.FULL_LENGTH) {
                continue;
            }
            Supplier<List<ColumnDescription>> supplier = dataTypeTolDescrSupplier.get(dataType);
            if (supplier == null) {
                throw log.throwing(new IllegalStateException(
                        "No column supplier for data type: " + dataType));
            }
            dataTypeToColDescr.put(dataType, supplier.get());
        }

        return log.traceExit(dataTypeToColDescr);
    }

    private List<ColumnDescription> getAffymetrixRawDataAnnotsColumnDescriptions() {
        log.traceEntry();
        List<ColumnDescription> colDescr = new ArrayList<>();
        colDescr.add(new ColumnDescription("Experiment ID", null,
                List.of("result.experiment.id"),
                ColumnDescription.ColumnType.INTERNAL_LINK,
                ColumnDescription.INTERNAL_LINK_TARGET_EXP));
        colDescr.add(new ColumnDescription("Experiment name", null,
                List.of("result.experiment.name"),
                ColumnDescription.ColumnType.STRING,
                null));
        colDescr.add(new ColumnDescription("Chip ID", "Identifier of the Affymetrix chip",
                List.of("result.id"),
                ColumnDescription.ColumnType.STRING,
                null));

        colDescr.addAll(getConditionColumnDescriptions("result"));

        return log.traceExit(colDescr);
    }
    private List<ColumnDescription> getRnaSeqRawDataAnnotsColumnDescriptions(boolean isSingleCell) {
        log.traceEntry("{}", isSingleCell);
        List<ColumnDescription> colDescr = new ArrayList<>();
        colDescr.add(new ColumnDescription("Experiment ID", null,
                List.of("result.library.experiment.id"),
                ColumnDescription.ColumnType.INTERNAL_LINK,
                ColumnDescription.INTERNAL_LINK_TARGET_EXP));
        colDescr.add(new ColumnDescription("Experiment name", null,
                List.of("result.library.experiment.name"),
                ColumnDescription.ColumnType.STRING,
                null));
        colDescr.add(new ColumnDescription("Library ID", "Identifier of the RNA-Seq library",
                List.of("result.library.id"),
                ColumnDescription.ColumnType.STRING,
                null));

        colDescr.addAll(getConditionColumnDescriptions("result"));

        colDescr.add(new ColumnDescription("Technology", null,
                List.of("result.library.technology.protocolName"),
                ColumnDescription.ColumnType.STRING,
                null));
        colDescr.add(new ColumnDescription("Sequencing platform", null,
                List.of("result.library.technology.sequencingPlatfomName"),
                ColumnDescription.ColumnType.STRING,
                null));
        colDescr.add(new ColumnDescription("Sequenced transcript part",
                "Possible values are: full length, all parts of the transcript are sequenced; "
                + "3': only the 3' end of the transcript is sequenced; "
                + "5': only the 5' end of the transcript is sequenced.",
                List.of("result.library.technology.sequencedTranscriptPart"),
                ColumnDescription.ColumnType.STRING,
                null));
        if (isSingleCell) {
            colDescr.add(new ColumnDescription("Fractionation",
                    "Possible values are: cell, transcripts are extracted from the cell; "
                    + "nuclei, transcripts are extracted from the nucleus.",
                            List.of("result.library.technology.cellCompartment"),
                            ColumnDescription.ColumnType.STRING,
                            null));
        }
        colDescr.add(new ColumnDescription("Fragmentation",
                "Size of the RNA fragmentation",
                List.of("result.library.technology.fragmentation"),
                ColumnDescription.ColumnType.NUMERIC,
                null));
        colDescr.add(new ColumnDescription("Run sequencing type",
                "Paired-end or single-read run",
                List.of("result.library.technology.libraryType"),
                ColumnDescription.ColumnType.STRING,
                null));

        colDescr.add(new ColumnDescription("Total read count",
                "Total number of reads for the annotated sample.",
                List.of("result.pipelineSummary.allReadsCount"),
                ColumnDescription.ColumnType.NUMERIC,
                null));
        colDescr.add(new ColumnDescription("Mapped read count",
                "Number of reads that could be mapped to the transcriptome.",
                List.of("result.pipelineSummary.mappedReadsCount"),
                ColumnDescription.ColumnType.NUMERIC,
                null));
        colDescr.add(new ColumnDescription("Total UMI count",
                "Total number of individual RNA molecules (UMI) for the annotated sample. "
                + "Only applicable for libraries producing UMIs.",
                List.of("result.pipelineSummary.allUMIsCount"),
                ColumnDescription.ColumnType.NUMERIC,
                null));
        colDescr.add(new ColumnDescription("Mapped UMI count",
                "Number of UMIs that could be mapped to the transcriptome. "
                + "Only applicable for libraries producing UMIs.",
                List.of("result.pipelineSummary.mappedUMIsCount"),
                ColumnDescription.ColumnType.NUMERIC,
                null));

        colDescr.add(new ColumnDescription("Distinct rank count",
                "When performing a fractional ranking of the genes in the annotated sample, "
                + "based on their expression level, number of distinct ranks observed, "
                + "to have a value of the power for distinguishing expression levels. "
                + "Used as a weight to compute a weighted mean rank accross samples for each gene and "
                + "compute expression scores in Bgee.",
                List.of("result.pipelineSummary.distinctRankCount"),
                ColumnDescription.ColumnType.NUMERIC,
                null));
        colDescr.add(new ColumnDescription("Max rank",
                "When performing a fractional ranking of the genes in the annotated sample, "
                + "based on their expression level, maximum rank attained in the sample. "
                + "Used to normalize ranks accross samples and compute expression scores in Bgee.",
                List.of("result.pipelineSummary.maxRank"),
                ColumnDescription.ColumnType.NUMERIC,
                null));

        return log.traceExit(colDescr);
    }

    private List<ColumnDescription> getAffymetrixProcExprValuesColumnDescriptions() {
        log.traceEntry();
        List<ColumnDescription> colDescr = new ArrayList<>();
        colDescr.add(new ColumnDescription("Experiment ID", null,
                List.of("result.assay.experiment.id"),
                ColumnDescription.ColumnType.INTERNAL_LINK,
                ColumnDescription.INTERNAL_LINK_TARGET_EXP));
        colDescr.add(new ColumnDescription("Chip ID", "Identifier of the Affymetrix chip",
                List.of("result.assay.id"),
                ColumnDescription.ColumnType.STRING,
                null));
        colDescr.add(new ColumnDescription("Probeset ID", "Identifier of the probeset for the chip type",
                List.of("result.id"),
                ColumnDescription.ColumnType.STRING,
                null));
        colDescr.add(new ColumnDescription("Gene ID", null,
                List.of("result.expressionCall.gene.geneId"),
                ColumnDescription.ColumnType.INTERNAL_LINK,
                ColumnDescription.INTERNAL_LINK_TARGET_GENE));
        colDescr.add(new ColumnDescription("Gene name", null,
                List.of("result.expressionCall.gene.name"),
                ColumnDescription.ColumnType.STRING,
                null));
        colDescr.add(new ColumnDescription("Signal intensity",
                "Normalized signal intensity of the probeset",
                List.of("result.normalizedSignalIntensity"),
                ColumnDescription.ColumnType.NUMERIC,
                null));
        colDescr.add(new ColumnDescription("Expression p-value",
                "P-value for the test of expression signal of the gene "
                + "significantly different from background expression",
                List.of("result.expressionCall.pValue"),
                ColumnDescription.ColumnType.NUMERIC,
                null));

        colDescr.addAll(getConditionColumnDescriptions("result.assay"));

        return log.traceExit(colDescr);
    }
    private List<ColumnDescription> getRnaSeqProcExprValuesColumnDescriptions() {
        log.traceEntry();
        List<ColumnDescription> colDescr = new ArrayList<>();
        colDescr.add(new ColumnDescription("Experiment ID", null,
                List.of("result.annotatedSample.library.experiment.id"),
                ColumnDescription.ColumnType.INTERNAL_LINK,
                ColumnDescription.INTERNAL_LINK_TARGET_EXP));
        colDescr.add(new ColumnDescription("Library ID", null,
                List.of("result.annotatedSample.library.id"),
                ColumnDescription.ColumnType.STRING,
                null));
        colDescr.add(new ColumnDescription("Gene ID", null,
                List.of("result.rawCall.gene.geneId"),
                ColumnDescription.ColumnType.INTERNAL_LINK,
                ColumnDescription.INTERNAL_LINK_TARGET_GENE));
        colDescr.add(new ColumnDescription("Gene name", null,
                List.of("result.rawCall.gene.name"),
                ColumnDescription.ColumnType.STRING,
                null));
        colDescr.add(new ColumnDescription("Expression level",
                "Expression level in the unit specified.",
                List.of("result.abundance"),
                ColumnDescription.ColumnType.NUMERIC,
                null));
        colDescr.add(new ColumnDescription("Expression level unit",
                "Unit to apply to the expression levels.",
                List.of("result.abundanceUnit"),
                ColumnDescription.ColumnType.STRING,
                null));
        colDescr.add(new ColumnDescription("Read count",
                "Number of reads mapped to this gene.",
                List.of("result.readCounts"),
                ColumnDescription.ColumnType.NUMERIC,
                null));
        colDescr.add(new ColumnDescription("UMI count",
                "Number of UMIs mapped to this gene. "
                + "Only applicable for libraries producing UMIs.",
                List.of("result.umiCounts"),
                ColumnDescription.ColumnType.NUMERIC,
                null));
        colDescr.add(new ColumnDescription("Expression p-value",
                "P-value for the test of expression signal of the gene "
                + "significantly different from background expression",
                List.of("result.rawCall.pValue"),
                ColumnDescription.ColumnType.NUMERIC,
                null));

        colDescr.addAll(getConditionColumnDescriptions("result.assay"));

        return log.traceExit(colDescr);
    }

    private List<ColumnDescription> getAffymetrixExperimentsColumnDescriptions() {
        log.traceEntry();

        List<ColumnDescription> colDescr = new ArrayList<>();
        colDescr.add(new ColumnDescription("Experiment ID", null,
                List.of("result.id"),
                ColumnDescription.ColumnType.INTERNAL_LINK,
                ColumnDescription.INTERNAL_LINK_TARGET_EXP));
        colDescr.add(new ColumnDescription("Experiment name", null,
                List.of("result.name"),
                ColumnDescription.ColumnType.STRING,
                null));
        colDescr.add(new ColumnDescription("Description", null,
                List.of("result.description"),
                ColumnDescription.ColumnType.STRING,
                null));
        return log.traceExit(colDescr);
    }
    private List<ColumnDescription> getRnaSeqExperimentsColumnDescriptions() {
        log.traceEntry();

        List<ColumnDescription> colDescr = new ArrayList<>();
        colDescr.add(new ColumnDescription("Experiment ID", null,
                List.of("result.id"),
                ColumnDescription.ColumnType.INTERNAL_LINK,
                ColumnDescription.INTERNAL_LINK_TARGET_EXP));
        colDescr.add(new ColumnDescription("Experiment name", null,
                List.of("result.name"),
                ColumnDescription.ColumnType.STRING,
                null));
        colDescr.add(new ColumnDescription("Description", null,
                List.of("result.description"),
                ColumnDescription.ColumnType.STRING,
                null));
        return log.traceExit(colDescr);
    }

    private static List<ColumnDescription> getConditionColumnDescriptions(String attributeStart) {
        log.traceEntry("{}", attributeStart);
        List<ColumnDescription> colDescr = new ArrayList<>();
        colDescr.add(new ColumnDescription("Anat. entity",
                "Annotation of the anatomical localization of the sample",
                List.of(attributeStart + ".annotation.rawDataCondition.cellType.id",
                        attributeStart + ".annotation.rawDataCondition.cellType.name",
                        attributeStart + ".annotation.rawDataCondition.anatEntity.id",
                        attributeStart + ".annotation.rawDataCondition.anatEntity.name"),
                ColumnDescription.ColumnType.ANAT_ENTITY,
                null));
        colDescr.add(new ColumnDescription("Stage",
                "Annotation of the developmental and life stage of the sample",
                List.of(attributeStart + ".annotation.rawDataCondition.devStage.id",
                        attributeStart + ".annotation.rawDataCondition.devStage.name"),
                ColumnDescription.ColumnType.DEV_STAGE,
                null));
        colDescr.add(new ColumnDescription("Sex",
                "Annotation of the sex of the sample",
                List.of(attributeStart + ".annotation.rawDataCondition.sex"),
                ColumnDescription.ColumnType.STRING,
                null));
        colDescr.add(new ColumnDescription("Strain",
                "Annotation of the strain of the sample",
                List.of(attributeStart + ".annotation.rawDataCondition.strain"),
                ColumnDescription.ColumnType.STRING,
                null));
        colDescr.add(new ColumnDescription("Species", null,
                List.of(attributeStart + ".annotation.rawDataCondition.species.genus",
                        attributeStart + ".annotation.rawDataCondition.species.speciesName"),
                ColumnDescription.ColumnType.STRING,
                null));
        return log.traceExit(colDescr);
    }
}