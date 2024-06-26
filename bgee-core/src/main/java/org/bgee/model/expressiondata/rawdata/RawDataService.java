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
import org.bgee.model.BgeeEnum;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataConditionFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO;
import org.bgee.model.expressiondata.ExpressionDataService;
import org.bgee.model.expressiondata.rawdata.RawDataProcessedFilter.RawDataProcessedFilterConditionPart;
import org.bgee.model.expressiondata.rawdata.RawDataProcessedFilter.RawDataProcessedFilterGeneSpeciesPart;
import org.bgee.model.expressiondata.rawdata.RawDataProcessedFilter.RawDataProcessedFilterInvariablePart;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataCondition;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataCondition.RawDataSex;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.ontology.MultiSpeciesOntology;
import org.bgee.model.ontology.OntologyService;
import org.bgee.model.ontology.RelationType;
import org.bgee.model.source.Source;
import org.bgee.model.species.Species;

public class RawDataService extends ExpressionDataService {
    private final static Logger log = LogManager.getLogger(RawDataService.class.getName());

    /**
     * Since the information contained in an {@code RawDataProcessedFilterInvariablePart}
     * will be the same in all {@code RawDataProcessedFilter}s, we store it statically
     * not to retrieve it for each {@code RawDataProcessedFilter} created.
     *
     * @see #loadIfNecessaryAndGetInvariablePart()
     * @see #processRawDataFilter(RawDataFilter,
     * RawDataProcessedFilterGeneSpeciesPart,
     * RawDataProcessedFilterConditionPart,
     * RawDataProcessedFilterInvariablePart)
     */
    private static RawDataProcessedFilterInvariablePart PROCESSED_FILTER_INVARIABLE_PART;
    /**
     * When an {@code RawDataProcessedFilter} is generated for a filtering requesting
     * all species and any gene, the {@code RawDataProcessedFilterGeneSpeciesPart}
     * is always the same, we store it here.
     *
     * @see #loadIfNecessaryAndGetGenericGeneSpeciesPart()
     * @see #processRawDataFilter(RawDataFilter,
     * RawDataProcessedFilterGeneSpeciesPart,
     * RawDataProcessedFilterConditionPart,
     * RawDataProcessedFilterInvariablePart)
     */
    private static RawDataProcessedFilterGeneSpeciesPart PROCESSED_ALL_SPECIES_NO_GENE_PART;

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


    private final RawDataConditionDAO rawDataCondDAO;

    public RawDataService(ServiceFactory serviceFactory) {
        super(serviceFactory);
        this.rawDataCondDAO = this.getDaoManager().getRawDataConditionDAO();
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
        return log.traceExit(new RawDataLoader(this.getServiceFactory(),
                preProcessedInfo));
    }

    public RawDataProcessedFilter processRawDataFilter(RawDataFilter filter) {
        log.traceEntry("{}", filter);
        return log.traceExit(this.processRawDataFilter(filter, null, null, null));
    }
    //For explanations, see javadoc in org.bgee.model.expressiondata.call.ExpressionCallService
    //#processExpressionCallFilter(ExpressionCallFilter2, ExpressionCallProcessedFilterGeneSpeciesPart,
    //ExpressionCallProcessedFilterConditionPart, ExpressionCallProcessedFilterInvariablePart),
    //the logic is the same
    public RawDataProcessedFilter processRawDataFilter(RawDataFilter filter,
            RawDataProcessedFilterGeneSpeciesPart geneSpeciesPart,
            RawDataProcessedFilterConditionPart conditionPart,
            RawDataProcessedFilterInvariablePart invariablePart) {
        log.traceEntry("{}, {}, {}, {}", filter, geneSpeciesPart, conditionPart, invariablePart);

        if (geneSpeciesPart != null && filter != null &&
                !geneSpeciesPart.getGeneFilters().equals(filter.getGeneFilters())) {
            throw log.throwing(new IllegalArgumentException("The RawDataProcessedFilterGeneSpeciesPart "
                    + "does not correspond to the RawDataFilter"));
        }
        if (conditionPart != null && filter != null &&
                !conditionPart.getConditionFilters().equals(filter.getConditionFilters())) {
            throw log.throwing(new IllegalArgumentException("The RawDataProcessedFilterConditionPart "
                    + "does not correspond to the RawDataFilter"));
        }
        final RawDataProcessedFilterInvariablePart procInvariablePart = invariablePart != null?
                invariablePart: loadIfNecessaryAndGetInvariablePart();
        final RawDataProcessedFilterGeneSpeciesPart procGeneSpeciesPart =
                geneSpeciesPart != null?
                    geneSpeciesPart:
                    (filter == null?
                        loadIfNecessaryAndGetGenericGeneSpeciesPart():
                        loadGeneSpeciesPart(filter, procInvariablePart.getGeneBioTypeMap()));

        //It's OK that the filter is null if we want to retrieve any raw data
        if (filter == null) {
            return log.traceExit(new RawDataProcessedFilter(filter,
                    //Important to provide a HashSet here, a null value means
                    //"we could not find conditions matching the parameters,
                    //thus there will be no result and no query done".
                    //While here we want to say "give me all results".
                    new HashSet<>(),

                    procGeneSpeciesPart,
                    null,
                    procInvariablePart));
        }

        //At this point, the filter cannot be null and we can use the method loadConditionPart
        assert filter != null;
        final RawDataProcessedFilterConditionPart procConditionPart = conditionPart != null?
                conditionPart: loadConditionPart(filter, procGeneSpeciesPart.getSpeciesMap());


        //Maybe we have no matching conditions at all for some species,
        //it means we should have no result in the related species.
        //we have to identify the species for which it is the case, to discard them,
        //otherwise, with no condition IDs specified, we could retrieve all results
        //for that species instead of no result.
        //
        //Of note, we don't have this problem with gene IDs: users can only select valid gene IDs,
        //and loadGeneMapFromGeneFilters throws an exception if a gene ID is not found.
        Set<Integer> speciesIdsWithCondFound = procConditionPart.getRequestedConditionMap().values().stream()
                .map(c -> c.getSpeciesId())
                .collect(Collectors.toSet());
        //Thanks to checks in the original filter, we know for sure that if a species
        //has specific conds requested, there are no filter requesting any cond for that species.
        Set<Integer> speciesIdsWithCondRequested = filter.getConditionFilters().stream()
                .filter(cf -> !cf.areAllCondParamFiltersEmpty())
                //we use speciesMap.keySet() here, rather than filter.getSpeciesIdsConsidered(),
                //because if filter.getSpeciesIdsConsidered() is null, speciesMap will contain
                //all the species.
                .flatMap(f -> f.getSpeciesId() == null? procGeneSpeciesPart.getSpeciesMap().keySet().stream():
                    Stream.of(f.getSpeciesId()))
                .collect(Collectors.toSet());
        Set<Integer> speciesIdsWithNoResult = new HashSet<>(speciesIdsWithCondRequested);
        speciesIdsWithNoResult.removeAll(speciesIdsWithCondFound);

        //If some specific conditions were requested for all species, but no condition was found,
        //we can stop here, there will be no results (encoded by providing a null daoFilters collection
        //to the ProcessedFilter). We don't need to check what was provided
        //in the GeneFilters, because the class DataFilter check the consistency
        //between the species requested in GeneFilters and ConditionFilters
        if (procGeneSpeciesPart.getSpeciesMap().keySet().equals(speciesIdsWithCondRequested) &&
                speciesIdsWithCondFound.isEmpty()) {
            return log.traceExit(new RawDataProcessedFilter(filter, null,
                    procGeneSpeciesPart,
                    procConditionPart,
                    procInvariablePart));
        }

        //if filter.getSpeciesIdsConsidered() is empty, we can create just one DAORawDataFilter
        //it means that there was no GeneFilter provided, only either a ConditionFilter targeting any species,
        //and/or experiment/assay IDs targeting any species
        Set<DAORawDataFilter> daoFilters = new HashSet<>();
        if (filter.getSpeciesIdsConsidered().isEmpty()) {
            assert filter.getGeneFilters().isEmpty() && procGeneSpeciesPart.getRequestedGeneMap().isEmpty();

            if (procConditionPart.getRequestedConditionMap().isEmpty() && filter.hasExperimentAssayIds()) {
                daoFilters.add(new DAORawDataFilter(filter.getExperimentIds(),
                    filter.getAssayIds(), filter.getExperimentOrAssayIds(),
                    filter.getUsedInPropagatedCalls()));
                log.debug("DAORawDataFilter created for any species");
            } else if (!procConditionPart.getRequestedConditionMap().isEmpty()) {
                daoFilters.add(new DAORawDataFilter(null, procConditionPart.getRequestedConditionMap().keySet(),
                        filter.getExperimentIds(), filter.getAssayIds(),
                        filter.getExperimentOrAssayIds(), filter.getUsedInPropagatedCalls()));
                log.debug("DAORawDataFilter created with at least some condition IDs");
            } else if (filter.getUsedInPropagatedCalls() != null) {
                daoFilters.add(new DAORawDataFilter(null, null, null, null, null,
                        filter.getUsedInPropagatedCalls()));
                log.debug("DAORawDataFilter created with only usedInPropagatedCalls");
            } else {
                assert procConditionPart.getRequestedConditionMap().isEmpty() && !filter.hasExperimentAssayIds() &&
                    filter.getUsedInPropagatedCalls() == null;
                log.debug("No DAORawDataFilter created: no species, no genes, no conds, no exp/assay IDs,"
                        + " no usedInPropagatedCalls");
            }
        } else {
            //XXX: actually I think we could create one DAOFilter for all species at once
            //(since I have changed the SQL query to make a where clause `(speciesIds OR geneIds AND condIds)`)
            daoFilters.addAll(filter.getSpeciesIdsConsidered().stream()
                    .filter(speciesId -> !speciesIdsWithNoResult.contains(speciesId))
                    .map(speciesId -> {
                        Set<Integer> bgeeGeneIds = procGeneSpeciesPart.getRequestedGeneMap().entrySet().stream()
                                .filter(e -> speciesId.equals(e.getValue().getSpecies().getId()))
                                .map(e -> e.getKey())
                                .collect(Collectors.toSet());
                        Set<Integer> rawCondIds = procConditionPart.getRequestedConditionMap().entrySet().stream()
                                .filter(e -> speciesId.equals(e.getValue().getSpeciesId()))
                                .map(e -> e.getKey())
                                .collect(Collectors.toSet());
                        if (bgeeGeneIds.isEmpty() && rawCondIds.isEmpty()) {
                            log.debug("DAORawDataFilter created without genes nor cond. for species ID: {}",
                                    speciesId);
                            return new DAORawDataFilter(Set.of(speciesId), filter.getExperimentIds(),
                                    filter.getAssayIds(), filter.getExperimentOrAssayIds(),
                                    filter.getUsedInPropagatedCalls());
                        }
                        log.debug("Complete DAORawDataFilter created for species ID: {}", speciesId);
                        return new DAORawDataFilter(bgeeGeneIds, rawCondIds, filter.getExperimentIds(),
                                filter.getAssayIds(), filter.getExperimentOrAssayIds(),
                                filter.getUsedInPropagatedCalls());
                    })
                    .collect(Collectors.toSet()));
        }
        log.debug("daoFilters: {}", daoFilters);

        return log.traceExit(new RawDataProcessedFilter(filter, daoFilters,
                procGeneSpeciesPart,
                procConditionPart,
                procInvariablePart));
    }
    private static Set<DAORawDataConditionFilter> convertRawDataConditionFilterToDAORawDataConditionFilter(
            Collection<RawDataConditionFilter> condFilters, OntologyService ontService,
            Set<Integer> consideredSpeciesIds) {
        log.traceEntry("{}, {}, {}", condFilters, ontService, consideredSpeciesIds);
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
                    //consideredSpeciesIds might itself be null, but it could have
                    //the species IDs requested in GeneFilters, to query conditions
                    //only in those species
                    filter.getSpeciesId() == null? consideredSpeciesIds:
                        Collections.singleton(filter.getSpeciesId()),
                    anatEntityIds, devStageIds, cellTypeIds,
                    //If simply the sex or strain root was requested including children terms,
                    //it simply means any sex or any strain, and the RawDataConditionFilter would return
                    //empty Sets in that case.
                    filter.getSexes()
                          .stream()
                          .map(s -> BgeeEnum.convert(RawDataSex.class, s))
                          .map(sex -> convertRawDataSexToDAORawDataSex(sex))
                          .map(daoSex -> daoSex.getStringRepresentation())
                          .collect(Collectors.toSet()),
                    filter.getStrains());
            log.debug("DAORawDataConditionFilter: {}", daoFilter);
            daoCondFilters.add(daoFilter);
        }
    
        //Now we filter the daoCondFilters: if one of them target a species with no additional parameters,
        //then we discard any other filter targeting the same species
        Map<Set<Integer>, List<DAORawDataConditionFilter>> filtersPerSpecies = daoCondFilters.stream()
                .collect(Collectors.groupingBy(f -> f.getSpeciesIds()));
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

    private RawDataProcessedFilterInvariablePart loadIfNecessaryAndGetInvariablePart() {
        //We don't fear a race condition here, because this information is cheap to compute
        //and does not change, so no problem to retrieve and set it multiple times.
        if (PROCESSED_FILTER_INVARIABLE_PART == null) {
            //We load the GeneBioTypes to be used in this method and in RawDataLoader
            Map<Integer, GeneBioType> geneBioTypeMap = loadGeneBioTypeMap(this.geneDAO);
            //Sources to be used by the RawDataLoader
            Map<Integer, Source> sourceMap = this.getServiceFactory().getSourceService()
                    .loadSourcesByIds(null);
            PROCESSED_FILTER_INVARIABLE_PART =
                    new RawDataProcessedFilterInvariablePart(geneBioTypeMap, sourceMap);
        }
        return log.traceExit(PROCESSED_FILTER_INVARIABLE_PART);
    }
    private RawDataProcessedFilterGeneSpeciesPart loadIfNecessaryAndGetGenericGeneSpeciesPart() {

        //We don't fear a race condition here, because this information is cheap to compute
        //and does not change, so no problem to retrieve and set it multiple times.
        if (PROCESSED_ALL_SPECIES_NO_GENE_PART == null) {
            PROCESSED_ALL_SPECIES_NO_GENE_PART = new RawDataProcessedFilterGeneSpeciesPart(
                    null,
                    null,
                    this.loadSpeciesMap(null, false, null));
        }
        return log.traceExit(PROCESSED_ALL_SPECIES_NO_GENE_PART);
    }
    private RawDataProcessedFilterGeneSpeciesPart loadGeneSpeciesPart(RawDataFilter filter,
            Map<Integer, GeneBioType> geneBioTypeMap) {
        log.traceEntry("{}, {}", filter, geneBioTypeMap);

        Map<Integer, Species> speciesMap = this.loadSpeciesMap(filter.getSpeciesIdsConsidered(),
                false, null);

        //Now, we load specific genes that can be queried (and not all genes of a species
        //if a GeneFilter contains no gene ID)
        Set<GeneFilter> geneFiltersToUse = filter.getGeneFilters().stream()
                .filter(f -> !f.getGeneIds().isEmpty())
                .collect(Collectors.toSet());
        Map<Integer, Gene> requestedGeneMap = geneFiltersToUse.isEmpty()? new HashMap<>():
            loadGeneMapFromGeneFilters(geneFiltersToUse, speciesMap, geneBioTypeMap, this.geneDAO);

        return log.traceExit(new RawDataProcessedFilterGeneSpeciesPart(
                filter.getGeneFilters(),
                requestedGeneMap,
                speciesMap));
    }
    private RawDataProcessedFilterConditionPart loadConditionPart(RawDataFilter filter,
            Map<Integer, Species> speciesMap) {
        log.traceEntry("{}, {}", filter, speciesMap);

        //Now, we load specific raw data conditions that can be queried (and not all conditions
        //of a species if a RawDataConditionFilter contains no filtering on condition parameters).
        Set<DAORawDataConditionFilter> daoCondFilters =
            convertRawDataConditionFilterToDAORawDataConditionFilter(filter.getConditionFilters(),
                    this.ontService, filter.getSpeciesIdsConsidered());
        Set<DAORawDataConditionFilter> daoCondFiltersToUse = daoCondFilters.stream()
                .filter(f -> !f.areAllCondParamFiltersEmpty())
                .collect(Collectors.toSet());
        Map<Integer, RawDataCondition> requestedRawDataCondMap = daoCondFiltersToUse.isEmpty()?
                new HashMap<>():
                loadRawDataConditionMap(speciesMap.values(), daoCondFiltersToUse,
                        null, this.rawDataCondDAO, this.anatEntityService, this.devStageService);

        return log.traceExit(new RawDataProcessedFilterConditionPart(
                filter.getConditionFilters(),
                requestedRawDataCondMap));
    }
}
