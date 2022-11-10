package org.bgee.model.expressiondata.rawdata;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.CommonService;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.DevStageService;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataConditionFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.ontology.MultiSpeciesOntology;
import org.bgee.model.ontology.OntologyService;
import org.bgee.model.ontology.RelationType;
import org.bgee.model.source.Source;
import org.bgee.model.species.Species;

public class RawDataService extends CommonService {
    private final static Logger log = LogManager.getLogger(RawDataService.class.getName());

//    /**
//     * {@code Enum} used to define the attributes to populate in the experiments, assay and raw calls.
//     * Some {@code Enum} are specific to one dataType and one raw data category (experiment, assay
//     * or calls). If an {@code Attribute} is used to retrieve raw data for a datatype it does not apply to,
//     * then the {@code Attribute is not considered} (e.g {@code Attribute.TECHNOLOGY} is used only for
//     * RNA-Seq. If it is used to retrieve raw data from an other datatype, no error will be thrown)
//     * <ul>
//     * <li>{@code ASSAY_PIPELINE_SUMMARY}: define that information coming from Bgee pipeline have to be
//     * retrieved as part of an Assay.
//     * <li>{@code TECHNOLOGY}: define that protocol information of RNA-Seq assay have to be retrieved.
//     * <li>{@code ANNOTATION}: define that annotation have to be retrieved.
//     * <li>{@code RAWCALL_PIPELINE_SUMMARY}: define that information coming from Bgee pipeline have to be
//     * retrieved as part of a raw call.
//     * <li>{@code DATASOURCE}: define that datasource information have to be retrieved.
//     * </ul>
//     *
//     * @author Julien Wollbrett
//     * @version Bgee 15 Aug. 2022
//     *
//     */
//    public enum Attribute{
//        TECHNOLOGY, ANNOTATION, ASSAY_PIPELINE_SUMMARY, RAWCALL_PIPELINE_SUMMARY,
//        DATASOURCE;
//    }

//    public abstract class CommonRawDataSpliterator<T, U extends AssayTO<?>, V extends Assay<?>,
//    W extends ExperimentTO<?>, X extends Experiment<?>> extends Spliterators.AbstractSpliterator<T> implements Closeable {
//
//        protected final Stream<W> expTOStream;
//        protected final Stream<U> assayTOStream;
//        protected Iterator<W> expTOIterator;
//        protected Iterator<U> assayTOIterator;
//        protected U lastAssayTO;
//        protected W lastExpTO;
//        protected X lastExp;
//
//        protected boolean isInitiated;
//        protected boolean isClosed;
//
//        public CommonRawDataSpliterator(Stream<W> expTOStream, Stream<U> assayTOStream) {
//            //TODO: check this call to 'super'
//            super(Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.IMMUTABLE 
//                    | Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.SORTED);
//            if (assayTOStream == null) {
//                throw new IllegalArgumentException("Assay stream cannot be null");
//            }
//
//            this.expTOStream = expTOStream;
//            this.assayTOStream = assayTOStream;
//            this.expTOIterator = null;
//            this.assayTOIterator = null;
//            this.lastAssayTO = null;
//            this.lastExp = null;
//            this.lastExpTO = null;
//            this.isInitiated = false;
//            this.isClosed = false;
//        }
//
//        protected <Y> boolean checkLastAssayTOIsValid(Y expectedAssayTOId) {
//            log.traceEntry("{}", expectedAssayTOId);
//            if (expectedAssayTOId == null) {
//                throw log.throwing(new IllegalArgumentException("The expected AssayTO ID cannot be null"));
//            }
//            return log.traceExit(this.lastAssayTO != null && expectedAssayTOId.equals(this.lastAssayTO.getId()));
//        }
//
//        protected <Y> V loadAssayAdvanceTOIterator(Y expectedAssayTOId) {
//            log.traceEntry("{}", expectedAssayTOId);
//
//            try {
//                this.lastAssayTO = this.assayTOIterator.next();
//            } catch (NoSuchElementException e) {
//                log.catching(Level.DEBUG, e);
//            }
//            if (expectedAssayTOId != null && !this.checkLastAssayTOIsValid(expectedAssayTOId)) {
//                throw log.throwing(new IllegalStateException("No assay matching the call source assay ID "
//                        + expectedAssayTOId
//                        + ". Either the call sources and assays were not properly ordered, or problem in data retrieval"));
//            }
//            if (this.lastAssayTO == null) {
//                return log.traceExit((V) null);
//            }
//
//            assert this.expTOIterator == null && !(this.lastAssayTO instanceof AssayPartOfExpTO) ||
//                    this.expTOIterator != null && this.lastAssayTO instanceof AssayPartOfExpTO;
//            if (this.expTOIterator != null && this.lastAssayTO instanceof AssayPartOfExpTO) {
//                AssayPartOfExpTO<?, ?> assayPartOfExpTO = (AssayPartOfExpTO<?, ?>) this.lastAssayTO;
//                if (this.lastExpTO == null || !this.lastExpTO.getId().equals(assayPartOfExpTO.getExperimentId())) {
//                    this.lastExpTO = this.expTOIterator.next();
//                }
//                if (this.lastExpTO == null || !this.lastExpTO.getId().equals(assayPartOfExpTO.getExperimentId())) {
//                    throw log.throwing(new IllegalStateException("No experiment matching the assay experiment ID "
//                            + assayPartOfExpTO.getExperimentId()
//                            + ". Either the assays and experiment were not properly ordered, or problem in data retrieval"));
//                }
//                this.lastExp = mapExperimentTOToExperiment(this.lastExpTO);
//            }
//            if (this.lastAssayTO instanceof AssayPartOfExpTO) {
//                return log.traceExit("{}", mapAssayPartOfExpTOToAssayPartOfExp((AssayPartOfExpTO<?, ?>) this.lastAssayTO, this.lastExp));
//            }
//            return log.traceExit("{}",mapAssayTOToAssay(this.lastAssayTO));
//        }
//
//        protected boolean initializeTryAdvance() {
//            log.traceEntry();
//            if (this.isClosed) {
//                throw log.throwing(new IllegalStateException("Already close"));
//            }
//            // Lazy loading: we do not get stream iterators (terminal operation)
//            // before tryAdvance() is called.
//            if (!this.isInitiated) {
//                //set it first because method can return false and exit the block
//                this.isInitiated = true;
//
//                if (expTOStream != null) {
//                    this.expTOIterator = this.expTOStream.iterator();
//                }
//                this.assayTOIterator = this.assayTOStream.iterator();
//                return log.traceExit(true);
//            }
//            return log.traceExit(false);
//        }
//
//        /**
//         * Return {@code null}, because a {@code CallSpliterator} does not have 
//         * the capability of being accessed in parallel. 
//         * 
//         * @return  The {@code Spliterator} that is {@code null}.
//         */
//        @Override
//        public Spliterator<T> trySplit() {
//            log.traceEntry();
//            return log.traceExit((Spliterator<T>) null);
//        }
//
//        @Override
//        public Comparator<? super T> getComparator() {
//            log.traceEntry();
//            //TODO?
//            return log.traceExit((Comparator<? super T>) null);
//        }
//
//        /** 
//         * Close {@code Stream}s provided at instantiation.
//         */
//        @Override
//        public void close() {
//            log.traceEntry();
//            if (!this.isClosed){
//                try {
//                    this.assayTOStream.close();
//                    this.expTOStream.close();
//                } finally {
//                    this.isClosed = true;
//                }
//            }
//            log.traceExit();
//        }
//    }
//
//    public class AssaySpliterator<T extends Assay<?>, U extends AssayTO<?>,
//    W extends ExperimentTO<?>, X extends Experiment<?>> extends CommonRawDataSpliterator<T, U, T, W, X> {
//
//        private final Class<T> assayType;
//
//        public AssaySpliterator(Stream<W> expTOStream, Stream<U> assayTOStream, Class<T> assayType) {
//            super(expTOStream, assayTOStream);
//            this.assayType = assayType;
//        }
//
//        @Override
//        public boolean tryAdvance(Consumer<? super T> action) {
//            log.traceEntry("{}", action);
//
//            this.initializeTryAdvance();
//
//            T assay = this.loadAssayAdvanceTOIterator(null);
//            if (assay == null) {
//                return log.traceExit(false);
//            }
//            if (this.assayType.isInstance(assay)) {
//                action.accept(this.assayType.cast(assay));
//            } else {
//                throw log.throwing(new IllegalStateException("Unexpected class for Assay, expected "
//                        + this.assayType + " but was " + assay.getClass()));
//            }
//            return log.traceExit(true);
//        }
//    }
//
//    public class RawCallSourceSpliterator<T extends RawCallSource<?>, U extends CallSourceTO<?>,
//    V extends AssayTO<?>, W extends Assay<?>, X extends ExperimentTO<?>, Y extends Experiment<?>>
//    extends CommonRawDataSpliterator<T, V, W, X, Y> {
//
//        private final Stream<U> callSourceTOStream;
//        private Iterator<U> callSourceTOIterator;
//        private W lastAssay;
//        private Class<T> callRawSourceType;
//
//        public RawCallSourceSpliterator(Stream<X> expTOStream, Stream<V> assayTOStream,
//                Stream<U> callSourceTOStream, Class<T> callRawSourceType) {
//            super(expTOStream, assayTOStream);
//            if (assayTOStream == null || callSourceTOStream == null) {
//                throw new IllegalArgumentException("Assay and RawCallSource streams cannot be null");
//            }
//
//            this.callSourceTOStream = callSourceTOStream;
//            this.callSourceTOIterator = null;
//            this.lastAssay = null;
//            this.callRawSourceType = callRawSourceType;
//        }
//
//        @Override
//        protected boolean initializeTryAdvance() {
//            log.traceEntry();
//            if (super.initializeTryAdvance()) {
//                this.callSourceTOIterator = this.callSourceTOStream.iterator();
//                return log.traceExit(true);
//            }
//            return log.traceExit(false);
//        }
//
//        @Override
//        public boolean tryAdvance(Consumer<? super T> action) {
//            log.traceEntry("{}", action);
//
//            this.initializeTryAdvance();
//
//            U callSourceTO = null;
//            try {
//                callSourceTO = this.callSourceTOIterator.next();
//            } catch (NoSuchElementException e) {
//                log.catching(Level.DEBUG, e);
//                return log.traceExit(false);
//            }
//
//            if (!this.checkLastAssayTOIsValid(callSourceTO.getAssayId())) {
//                this.lastAssay = this.loadAssayAdvanceTOIterator(callSourceTO.getAssayId());
//            }
//            RawCallSource<?> callSource = mapRawCallSourceTOToRawCallSource(callSourceTO, this.lastAssay);
//            if (this.callRawSourceType.isInstance(callSource)) {
//                action.accept(this.callRawSourceType.cast(callSource));
//            } else {
//                throw log.throwing(new IllegalStateException("Unexpected class for RawCallSource, expected "
//                        + this.callRawSourceType + " but was " + callSource.getClass()));
//            }
//            return log.traceExit(true);
//        }
//
//        /**
//         * Close {@code Stream}s provided at instantiation.
//         */
//        @Override
//        public void close() {
//            log.traceEntry();
//            if (!this.isClosed){
//                try {
//                    this.callSourceTOStream.close();
//                } finally {
//                    super.close();
//                }
//            }
//            log.traceExit();
//        }
//    }

//    private static <T extends Experiment<?>, U extends ExperimentTO<?>> T mapExperimentTOToExperiment(U expTO) {
//        log.traceEntry("{}", expTO);
//        //TODO
//        return log.traceExit((T) null);
//    }
//    private static <T extends Assay<?>, U extends AssayTO<?>> T mapAssayTOToAssay(U assayTO) {
//        log.traceEntry("{}", assayTO);
//        //TODO
//        return log.traceExit((T) null);
//    }
//    private static <T extends AssayPartOfExp<?, V>, U extends AssayPartOfExpTO<?, ?>, V extends Experiment<?>>
//    T mapAssayPartOfExpTOToAssayPartOfExp(U assayTO, V exp) {
//        log.traceEntry("{}, {}", assayTO, exp);
//        //TODO
//        return log.traceExit((T) null);
//    }
//    private static <T extends  RawCallSource<V>, U extends CallSourceTO<?>, V extends Assay<?>>
//    T mapRawCallSourceTOToRawCallSource(U callSourceTO, V assay) {
//        log.traceEntry("{}, {}", callSourceTO, assay);
//        //TODO
//        return log.traceExit((T) null);
//    }
//
//    private static DAORawDataFilter convertRawDataFilterToDAORawDataFilter(RawDataFilter rawDataFilter,
//            Map<Integer, Gene> geneMap) {
//        log.traceEntry("{}, {}",rawDataFilter, geneMap);
//        if (rawDataFilter == null) {
//            return log.traceExit((DAORawDataFilter) null);
//        }
//        Entry<Set<Integer>, Set<Integer>> geneIdsSpeciesIdsForDAOs =
//                convertGeneFiltersToBgeeGeneIdsAndSpeciesIds(rawDataFilter.getGeneFilters(), geneMap);
//                return null;
//        return log.traceExit(new DAORawDataFilter(
//                geneIdsSpeciesIdsForDAOs.getKey(), geneIdsSpeciesIdsForDAOs.getValue(),
//
//                rawDataFilter.getConditionFilters().stream()
//                    .map(cf -> convertRawDataConditionFilterToDAORawDataConditionFilter(cf))
//                    .collect(Collectors.toSet())
//                ));
//    }

    private final GeneDAO geneDAO;
    private final RawDataConditionDAO rawDataCondDAO;
    private final OntologyService ontService;
    private final AnatEntityService anatEntityService;
    private final DevStageService devStageService;

    public RawDataService(ServiceFactory serviceFactory) {
        super(serviceFactory);
        this.geneDAO = this.getDaoManager().getGeneDAO();
        this.rawDataCondDAO = this.getDaoManager().getRawDataConditionDAO();
        this.ontService = serviceFactory.getOntologyService();
        this.anatEntityService = serviceFactory.getAnatEntityService();
        this.devStageService = serviceFactory.getDevStageService();
    }

    public RawDataLoader loadRawDataLoader(RawDataFilter filter) {
        log.traceEntry("{}", filter);
        return log.traceExit(new RawDataLoader(this.getServiceFactory(),
                this.processRawDataFilter(filter)));
    }

    /**
     * Obtain a new {@code RawDataLoader} from parameters already pre-processed, rather than
     * from a {@code RawDataFilter}. This method exists for optimization purposes,
     * the default method to obtain a {@code RawDataLoader} is
     * {@link #loadRawDataLoader(RawDataFilter)}.
     * <p>
     * When a {@code RawDataProcessedFilter} has been computed, it is faster to obtain
     * a new {@code RawDataLoader} by calling this method than by calling
     * {@code loadRawDataLoader(RawDataFilter)}. A {@code RawDataProcessedFilter}
     * can be obtained either:
     * <ul>
     * <li>by calling {@link RawDataLoader#getRawDataProcessedFilter()},
     * the {@code RawDataProcessedFilter} would have been already computed
     * to create the {@code RawDataLoader}
     * <li>or by calling {@link #processRawDataFilter(RawDataFilter)}, which will trigger
     * the computation of a new {@code RawDataProcessedFilter}.
     * </ul>
     * <p>
     * Some computations will thus not be unnecessarily performed again.
     * The {@code RawDataProcessedFilter} could typically be stored in a {@code Map},
     * where the key is the source {@code RawDataFilter}, and the value the associated
     * {@code RawDataProcessedFilter}. When a new {@code RawDataFilter}
     * is configured by a client, before calling {@code loadRawDataLoader(RawDataFilter)}
     * or {@code processRawDataFilter(RawDataFilter)}, an attempt to retrieve from the {@code Map}
     * a {@code RawDataProcessedFilter} associated with an equal {@code RawDataFilter}
     * should be made, to use this method instead.
     * <p>
     * It is useful to store the pre-processed information outside of a {@code RawDataLoader},
     * in order to avoid risking to keep connections to data sources open
     * (as {@code RawDataLoader}s are {@code Service}s, holding connections to data sources).
     *
     * @param preProcessedInfo  A {@code RawDataProcessedFilter} obtained
     *                          from a {@code RawDataLoader}.
     * @return                  A {@code RawDataLoader} equivalent to the one that provided
     *                          {@code preProcessedInfo}.
     * @see #loadRawDataLoader(RawDataFilter)
     * @see #processRawDataFilter(RawDataFilter)
     * @see RawDataLoader#getRawDataProcessedFilter()
     */
    public RawDataLoader getRawDataLoader(RawDataProcessedFilter preProcessedInfo) {
        log.traceEntry("{}", preProcessedInfo);
        return log.traceExit(new RawDataLoader(this.getServiceFactory(), preProcessedInfo));
    }

    public RawDataProcessedFilter processRawDataFilter(RawDataFilter filter) {
        log.traceEntry("{}", filter);

        //We load the GeneBioTypes to be used in this method and in RawDataLoader
        Map<Integer, GeneBioType> geneBioTypeMap = loadGeneBioTypeMap(this.geneDAO);
        //Sources to be used by the RawDataLoader
        Map<Integer, Source> sourceMap = this.getServiceFactory().getSourceService()
                .loadSourcesByIds(null);

        //It's OK that the filter is null if we want to retrieve any raw data
        if (filter == null) {
            return log.traceExit(new RawDataProcessedFilter(filter,
                    //Important to provide a HashSet here, a null value means
                    //"we could not find conditions matching the parameters,
                    //thus there will be no result and no query done".
                    //While here we want to say "give me all results".
                    new HashSet<>(),
                    null, null,
                    //load all Species, gene biotypes, and sources
                    this.loadSpeciesMap(null, false, null), geneBioTypeMap, sourceMap));
        }

        //we prepare the info the Loader will need when calling its various "load" methods.

        //First, we load all species that can potentially be queried:
        Set<Integer> speciesIds = filter.getGeneFilters().stream().map(f -> f.getSpeciesId())
                .collect(Collectors.toSet());
        speciesIds.addAll(filter.getConditionFilters().stream().map(f -> f.getSpeciesId())
                .collect(Collectors.toSet()));
        Map<Integer, Species> speciesMap = this.loadSpeciesMap(speciesIds, false, null);

        //Now, we load specific genes that can be queried (and not all genes of a species
        //if a GeneFilter contains no gene ID)
        Set<GeneFilter> geneFiltersToUse = filter.getGeneFilters().stream()
                .filter(f -> !f.getGeneIds().isEmpty())
                .collect(Collectors.toSet());
        Map<Integer, Gene> requestedGeneMap = geneFiltersToUse.isEmpty()? new HashMap<>():
            loadGeneMapFromGeneFilters(geneFiltersToUse, speciesMap, geneBioTypeMap, this.geneDAO);

        //Now, we load specific raw data conditions that can be queried (and not all conditions
        //of a species if a RawDataConditionFilter contains no filtering on condition parameters).
        Set<DAORawDataConditionFilter> daoCondFilters =
            convertRawDataConditionFilterToDAORawDataConditionFilter(filter.getConditionFilters(),
                    this.ontService);
        Set<DAORawDataConditionFilter> daoCondFiltersToUse = daoCondFilters.stream()
                .filter(f -> !f.areAllCondParamFiltersEmpty())
                .collect(Collectors.toSet());
        Map<Integer, RawDataCondition> requestedRawDataCondMap = daoCondFiltersToUse.isEmpty()? new HashMap<>():
            loadRawDataConditionMap(speciesMap.values(), daoCondFiltersToUse,
                null, this.rawDataCondDAO, this.anatEntityService, this.devStageService);
        //Maybe we have no matching conditions for some RawDataConditionFilters,
        //it means we should have no result in the related species.
        //we have to identify the species for which it is the case, to discard them,
        //otherwise, with no condition IDs specified, we could retrieve all results
        //for that species instead of no result.
        //We need this Set to implement the discarding below.
        //
        //Of note, we don't have this problem with gene IDs: users can only select valid gene IDs,
        //and loadGeneMapFromGeneFilters throws an exception of a gene ID is not found.
        Set<Integer> speciesIdsWithCondRequested = filter.getConditionFilters().stream()
                .filter(cf -> !cf.areAllCondParamFiltersEmpty())
                .map(f -> f.getSpeciesId())
                .collect(Collectors.toSet());

        //Finally, we produce the DAORawDataFilters that will be used by the RawDataLoader
        //when calling the raw data DAOs
        Set<DAORawDataFilter> daoFilters = speciesIds.stream().map(speciesId -> {
            Set<Integer> bgeeGeneIds = requestedGeneMap.entrySet().stream()
                    .filter(e -> speciesId.equals(e.getValue().getSpecies().getId()))
                    .map(e -> e.getKey())
                    .collect(Collectors.toSet());
            Set<Integer> rawCondIds = requestedRawDataCondMap.entrySet().stream()
                    .filter(e -> speciesId == e.getValue().getSpeciesId())
                    .map(e -> e.getKey())
                    .collect(Collectors.toSet());
            if (rawCondIds.isEmpty() && speciesIdsWithCondRequested.contains(speciesId)) {
                //there should be no results in that species, since there was no condition
                //matching the query. If we didn't return null, we could retrieve all results
                //in that species, instead of no result.
                //Of note, we don't have this problem with gene IDs: users can only select valid gene IDs,
                //and loadGeneMapFromGeneFilters throws an exception of a gene ID is not found.
                log.debug("No RawDataCondition matching the condition filters for species ID: {}",
                        speciesId);
                return null;
            }
            if (bgeeGeneIds.isEmpty() && rawCondIds.isEmpty()) {
                return new DAORawDataFilter(speciesId, filter.getExperimentIds(),
                    filter.getAssayIds(), filter.getExperimentOrAssayIds());
            }
            return new DAORawDataFilter(bgeeGeneIds, rawCondIds, filter.getExperimentIds(),
                    filter.getAssayIds(), filter.getExperimentOrAssayIds());
        })
        .filter(f -> f != null)
        .collect(Collectors.toSet());

        if (!speciesIds.isEmpty() && daoFilters.isEmpty()) {
            //it means that there is no conditions matching the query in all species,
            //we make the daoFilter null to signal there will be no result at all,
            //as opposed to when no filtering is requested (empty daoFilters as well)
            daoFilters = null;
        } else if (speciesIds.isEmpty() && filter.hasExperimentAssayIds()) {
            //In case no species ID was targeted, we need to add a DAO filter
            //for experiment/assay IDs if any was provided.
            daoFilters.add(new DAORawDataFilter(filter.getExperimentIds(),
                    filter.getAssayIds(), filter.getExperimentOrAssayIds()));
        }

        return log.traceExit(new RawDataProcessedFilter(filter, daoFilters,
                requestedGeneMap, requestedRawDataCondMap,
                speciesMap, geneBioTypeMap, sourceMap));
    }
    private static Set<DAORawDataConditionFilter> convertRawDataConditionFilterToDAORawDataConditionFilter(
            Collection<RawDataConditionFilter> condFilters, OntologyService ontService) {
        log.traceEntry("{}, {}", condFilters, ontService);
        if (condFilters == null || condFilters.isEmpty()) {
            return log.traceExit(new HashSet<>());
        }
    
        //First, in order to load appropriately the ontologies,
        //we retrieve terms and species for which we request to retrieve children terms.
        Set<String> anatEntityAndCellTypeIdsWithChildrenRequested = new HashSet<>();
        Set<Integer> speciesIdsWithAnatCellChildrenRequested = new HashSet<>();
        Set<String> devStageIdsWithChildrenRequested = new HashSet<>();
        Set<Integer> speciesIdsWithDevStageChildrenRequested = new HashSet<>();
        for (RawDataConditionFilter filter: condFilters) {
            if (filter.isIncludeSubAnatEntities() && !filter.getAnatEntityIds().isEmpty()) {
                anatEntityAndCellTypeIdsWithChildrenRequested.addAll(filter.getAnatEntityIds());
                speciesIdsWithAnatCellChildrenRequested.add(filter.getSpeciesId());
            }
            if (filter.isIncludeSubDevStages() && !filter.getDevStageIds().isEmpty()) {
                devStageIdsWithChildrenRequested.addAll(filter.getDevStageIds());
                speciesIdsWithDevStageChildrenRequested.add(filter.getSpeciesId());
            }
            if (filter.isIncludeSubCellTypes() && !filter.getCellTypeIds().isEmpty()) {
                anatEntityAndCellTypeIdsWithChildrenRequested.addAll(filter.getCellTypeIds());
                speciesIdsWithAnatCellChildrenRequested.add(filter.getSpeciesId());
            }
        }
    
        //Now we load the ontologies if needed
        MultiSpeciesOntology<AnatEntity, String> anatOntology = anatEntityAndCellTypeIdsWithChildrenRequested.isEmpty()?
                null: ontService.getAnatEntityOntology(
                        speciesIdsWithAnatCellChildrenRequested, anatEntityAndCellTypeIdsWithChildrenRequested,
                        EnumSet.of(RelationType.ISA_PARTOF), false, true);
        MultiSpeciesOntology<DevStage, String> stageOntology = devStageIdsWithChildrenRequested.isEmpty()?
                null: ontService.getDevStageOntology(
                        speciesIdsWithDevStageChildrenRequested, devStageIdsWithChildrenRequested, false, true);
        //There is no ontology for RawDataSex and RawDataStrain (String), really it's simply one root
        //with all other terms at the first level.
    
        //Now we have everything we need to create the DAO filters
        Set<DAORawDataConditionFilter> daoCondFilters = new HashSet<>();
        for (RawDataConditionFilter filter: condFilters) {
            Set<String> anatEntityIds = new HashSet<>();
            Set<String> devStageIds = new HashSet<>();
            Set<String> cellTypeIds = new HashSet<>();

            anatEntityIds.addAll(filter.getAnatEntityIds());
            if (filter.isIncludeSubAnatEntities()) {
                anatEntityIds.addAll(
                    filter.getAnatEntityIds().stream().flatMap(id -> anatOntology.getDescendantIds(
                              id, false, Collections.singleton(filter.getSpeciesId()))
                          .stream()).collect(Collectors.toSet())
                );
            }
    
            devStageIds.addAll(filter.getDevStageIds());
            if (filter.isIncludeSubDevStages()) {
                devStageIds.addAll(
                    filter.getDevStageIds().stream().flatMap(id -> stageOntology.getDescendantIds(
                              id, false, Collections.singleton(filter.getSpeciesId()))
                          .stream()).collect(Collectors.toSet())
                );
            }
    
            cellTypeIds.addAll(filter.getCellTypeIds());
            if (filter.isIncludeSubCellTypes()) {
                cellTypeIds.addAll(
                    filter.getCellTypeIds().stream().flatMap(id -> anatOntology.getDescendantIds(
                              id, false, Collections.singleton(filter.getSpeciesId()))
                          .stream()).collect(Collectors.toSet())
                );
            }

            DAORawDataConditionFilter daoFilter = new DAORawDataConditionFilter(
                    Collections.singleton(filter.getSpeciesId()),
                    anatEntityIds, devStageIds, cellTypeIds,
                    //If simply the sex or strain root was requested including children terms,
                    //it simply means any sex or any strain, and the RawDataConditionFilter would return
                    //empty Sets in that case.
                    filter.getSexes(), filter.getStrains());
            log.debug("DAORawDataConditionFilter: {}", daoFilter);
            daoCondFilters.add(daoFilter);
        }
    
        //Now we filter the daoCondFilters: if one of them target a species with no additional parameters,
        //then we discard any other filter targeting the same species
        Map<Integer, List<DAORawDataConditionFilter>> filtersPerSpecies = daoCondFilters.stream()
                //we always have exactly one species in getSpeciesIds() at this point
                .collect(Collectors.groupingBy(f -> f.getSpeciesIds().stream().findFirst().get()));
        return log.traceExit(
            filtersPerSpecies.values().stream().flatMap(l -> {
                    DAORawDataConditionFilter noFilter = l.stream()
                            .filter(f -> f.areAllCondParamFiltersEmpty())
                            .findAny().orElse(null);
                    if (noFilter != null) {
                        return Stream.of(noFilter);
                    }
                    return l.stream();
            }).collect(Collectors.toSet())
        );
    }

    //*************************************************************************
    // METHODS PERFORMING THE QUERIES TO THE DAOs
    //*************************************************************************

//    Stream<AffymetrixProbeset> loadAffymetrixProbesets(Set<RawDataFilter> filters) {
//        log.traceEntry("{}", filters);
//        if (filters == null || filters.isEmpty()) {
//            throw log.throwing(new IllegalArgumentException("A RawDataFilter must be provided"));
//        }
//        if (filters.contains(null)) {
//            throw log.throwing(new IllegalArgumentException("No RawDataFilter can be null"));
//        }
//
//        //TODO: to continue
//        return null;
//        
//    }
    

//    public RawDataContainer loadExperiments(RawDataFilter dataFilter,
//            Collection<RawDataDataType> dataTypes, Collection<String> experimentIds, 
//            Collection<String> assayIds, EnumSet<Attribute> attrs) {
//        log.traceEntry("{}, {}, {}, {}, {}", dataFilter, dataTypes, assayIds, experimentIds, attrs);
//        RawDataLoader loader = this.getRawDataLoader(dataFilter);
//        Stream<AffymetrixExperiment> affyExp = dataTypes.contains(RawDataDataType.AFFYMETRIX)?
//                loader.loadAffymetrixExperiments(experimentIds, assayIds, attrs)
//                : null;
//        Stream<RnaSeqExperiment> rnaSeqExp = dataTypes.contains(RawDataDataType.RNASEQ)?
//                loader.loadRnaSeqExperiments(experimentIds, assayIds, attrs)
//                : null;
//        return log.traceExit(new RawDataContainer(dataTypes, affyExp, null, null, rnaSeqExp,
//                null, null, null, null, null, null, null, null));
//    }
//    
//    //XXX: If each datatype specifique loading function retrieve a RawDataContainer, it si then
//    //     possible to define in this function if we want only Assays or also to populate
//    //     Experiments using a boolean that could be called withSeparateExperiment.
//    //     It will allow not to consume the Assay streams neither to manipulate the Assay streams
//    //     to retrieve unique experiments.
//    // 
//    // Not providing assay and exp IDs at instantiation of RawDataLoader allows to reuse the same
//    // RawDataLoader when filtering on different assays or experiments.
//    public RawDataContainer loadAssays(RawDataFilter dataFilter,
//            Collection<RawDataDataType> dataTypes, Collection<String> experimentIds,
//            Collection<String> assayIds, EnumSet<Attribute> attrs) {
//        log.traceEntry("{}, {}, {}, {}, {}", dataFilter, dataTypes, assayIds, experimentIds, attrs);
//        RawDataLoader loader = this.getRawDataLoader(dataFilter);
//        Stream<AffymetrixChip> affyAssays = dataTypes.contains(RawDataDataType.AFFYMETRIX)?
//                loader.loadAffymetrixChips(experimentIds, assayIds, attrs)
//                : null;
//        Stream<RnaSeqLibraryAnnotatedSample> rnaSeqLibraries= dataTypes.contains(RawDataDataType.RNASEQ)?
//                loader.loadRnaSeqLibraryAnnotatedSample(experimentIds, assayIds, attrs)
//                : null;
//        //TODO continue once all datatype specific methods have been implemented
//        return log.traceExit(new RawDataContainer(dataTypes, null, affyAssays, null, null, null,
//                rnaSeqLibraries, null, null, null, null, null, null));
//        }
//
//  //XXX: If each datatype specifique loading function retrieve a RawDataContainer, it si then
//    //     possible to define in this function if we want only Calls or also to populate
//    //     Experiments and Assays using booleans that could be called withSeparateExperiment and
//    //     withSeparateAssays. It will allow not to consume the Assay streams neither to manipulate
//    //     the Assay streams to retrieve unique experiments.
//    // 
//    public RawDataContainer loadRawCalls(RawDataFilter dataFilter,
//            EnumSet<RawDataDataType> dataTypes, Set<String> experimentIds, Set<String> assayIds,
//            EnumSet<Attribute> attrs) {
//        log.traceEntry("{}, {}, {}, {}, {}", dataFilter, dataTypes, assayIds, experimentIds, attrs);
//        RawDataLoader loader = this.getRawDataLoader(dataFilter);
//        Stream<AffymetrixProbeset> affyCalls = dataTypes.contains(RawDataDataType.AFFYMETRIX)?
//                loader.loadAffymetrixProbesets(experimentIds, assayIds, null, attrs)
//                : null;
//        //TODO continue once all datatype specific methods have been implemented
//        return log.traceExit(new RawDataContainer(dataTypes, null, null, affyCalls, null,
//                null, null, null, null, null, null, null, null));
//    }

}
