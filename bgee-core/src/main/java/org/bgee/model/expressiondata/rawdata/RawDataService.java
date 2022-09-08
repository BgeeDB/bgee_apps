package org.bgee.model.expressiondata.rawdata;

import java.io.Closeable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.CommonService;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataConditionFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataAssayDAO.AssayPartOfExpTO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataAssayDAO.AssayTO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCallSourceDAO.CallSourceTO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataExperimentDAO.ExperimentTO;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixChip;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixExperiment;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixProbeset;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqExperiment;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqLibraryAnnotatedSample;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.species.Species;

public class RawDataService extends CommonService {
    private final static Logger log = LogManager.getLogger(RawDataService.class.getName());

    /**
     * {@code Enum} used to define the attributes to populate in the experiments, assay and raw calls.
     * Some {@code Enum} are specific to one dataType and one raw data category (experiment, assay
     * or calls). If an {@code Attribute} is used to retrieve raw data for a datatype it does not apply to,
     * then the {@code Attribute is not considered} (e.g {@code Attribute.TECHNOLOGY} is used only for
     * RNA-Seq. If it is used to retrieve raw data from an other datatype, no error will be thrown)
     * <ul>
     * <li>{@code ASSAY_PIPELINE_SUMMARY}: define that information coming from Bgee pipeline have to be
     * retrieved as part of an Assay.
     * <li>{@code TECHNOLOGY}: define that protocol information of RNA-Seq assay have to be retrieved.
     * <li>{@code ANNOTATION}: define that annotation have to be retrieved.
     * <li>{@code RAWCALL_PIPELINE_SUMMARY}: define that information coming from Bgee pipeline have to be
     * retrieved as part of a raw call.
     * <li>{@code DATASOURCE}: define that datasource information have to be retrieved.
     * </ul>
     *
     * @author Julien Wollbrett
     * @version Bgee 15 Aug. 2022
     *
     */
    public enum Attribute{
        TECHNOLOGY, ANNOTATION, ASSAY_PIPELINE_SUMMARY, RAWCALL_PIPELINE_SUMMARY,
        DATASOURCE;
    }

    public abstract class CommonRawDataSpliterator<T, U extends AssayTO<?>, V extends Assay<?>,
    W extends ExperimentTO<?>, X extends Experiment<?>> extends Spliterators.AbstractSpliterator<T> implements Closeable {

        protected final Stream<W> expTOStream;
        protected final Stream<U> assayTOStream;
        protected Iterator<W> expTOIterator;
        protected Iterator<U> assayTOIterator;
        protected U lastAssayTO;
        protected W lastExpTO;
        protected X lastExp;

        protected boolean isInitiated;
        protected boolean isClosed;

        public CommonRawDataSpliterator(Stream<W> expTOStream, Stream<U> assayTOStream) {
            //TODO: check this call to 'super'
            super(Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.IMMUTABLE 
                    | Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.SORTED);
            if (assayTOStream == null) {
                throw new IllegalArgumentException("Assay stream cannot be null");
            }

            this.expTOStream = expTOStream;
            this.assayTOStream = assayTOStream;
            this.expTOIterator = null;
            this.assayTOIterator = null;
            this.lastAssayTO = null;
            this.lastExp = null;
            this.lastExpTO = null;
            this.isInitiated = false;
            this.isClosed = false;
        }

        protected <Y> boolean checkLastAssayTOIsValid(Y expectedAssayTOId) {
            log.traceEntry("{}", expectedAssayTOId);
            if (expectedAssayTOId == null) {
                throw log.throwing(new IllegalArgumentException("The expected AssayTO ID cannot be null"));
            }
            return log.traceExit(this.lastAssayTO != null && expectedAssayTOId.equals(this.lastAssayTO.getId()));
        }

        protected <Y> V loadAssayAdvanceTOIterator(Y expectedAssayTOId) {
            log.traceEntry("{}", expectedAssayTOId);

            try {
                this.lastAssayTO = this.assayTOIterator.next();
            } catch (NoSuchElementException e) {
                log.catching(Level.DEBUG, e);
            }
            if (expectedAssayTOId != null && !this.checkLastAssayTOIsValid(expectedAssayTOId)) {
                throw log.throwing(new IllegalStateException("No assay matching the call source assay ID "
                        + expectedAssayTOId
                        + ". Either the call sources and assays were not properly ordered, or problem in data retrieval"));
            }
            if (this.lastAssayTO == null) {
                return log.traceExit((V) null);
            }

            assert this.expTOIterator == null && !(this.lastAssayTO instanceof AssayPartOfExpTO) ||
                    this.expTOIterator != null && this.lastAssayTO instanceof AssayPartOfExpTO;
            if (this.expTOIterator != null && this.lastAssayTO instanceof AssayPartOfExpTO) {
                AssayPartOfExpTO<?, ?> assayPartOfExpTO = (AssayPartOfExpTO<?, ?>) this.lastAssayTO;
                if (this.lastExpTO == null || !this.lastExpTO.getId().equals(assayPartOfExpTO.getExperimentId())) {
                    this.lastExpTO = this.expTOIterator.next();
                }
                if (this.lastExpTO == null || !this.lastExpTO.getId().equals(assayPartOfExpTO.getExperimentId())) {
                    throw log.throwing(new IllegalStateException("No experiment matching the assay experiment ID "
                            + assayPartOfExpTO.getExperimentId()
                            + ". Either the assays and experiment were not properly ordered, or problem in data retrieval"));
                }
                this.lastExp = mapExperimentTOToExperiment(this.lastExpTO);
            }
            if (this.lastAssayTO instanceof AssayPartOfExpTO) {
                return log.traceExit("{}", mapAssayPartOfExpTOToAssayPartOfExp((AssayPartOfExpTO<?, ?>) this.lastAssayTO, this.lastExp));
            }
            return log.traceExit("{}",mapAssayTOToAssay(this.lastAssayTO));
        }

        protected boolean initializeTryAdvance() {
            log.traceEntry();
            if (this.isClosed) {
                throw log.throwing(new IllegalStateException("Already close"));
            }
            // Lazy loading: we do not get stream iterators (terminal operation)
            // before tryAdvance() is called.
            if (!this.isInitiated) {
                //set it first because method can return false and exit the block
                this.isInitiated = true;

                if (expTOStream != null) {
                    this.expTOIterator = this.expTOStream.iterator();
                }
                this.assayTOIterator = this.assayTOStream.iterator();
                return log.traceExit(true);
            }
            return log.traceExit(false);
        }

        /**
         * Return {@code null}, because a {@code CallSpliterator} does not have 
         * the capability of being accessed in parallel. 
         * 
         * @return  The {@code Spliterator} that is {@code null}.
         */
        @Override
        public Spliterator<T> trySplit() {
            log.traceEntry();
            return log.traceExit((Spliterator<T>) null);
        }

        @Override
        public Comparator<? super T> getComparator() {
            log.traceEntry();
            //TODO?
            return log.traceExit((Comparator<? super T>) null);
        }

        /** 
         * Close {@code Stream}s provided at instantiation.
         */
        @Override
        public void close() {
            log.traceEntry();
            if (!this.isClosed){
                try {
                    this.assayTOStream.close();
                    this.expTOStream.close();
                } finally {
                    this.isClosed = true;
                }
            }
            log.traceExit();
        }
    }

    public class AssaySpliterator<T extends Assay<?>, U extends AssayTO<?>,
    W extends ExperimentTO<?>, X extends Experiment<?>> extends CommonRawDataSpliterator<T, U, T, W, X> {

        private final Class<T> assayType;

        public AssaySpliterator(Stream<W> expTOStream, Stream<U> assayTOStream, Class<T> assayType) {
            super(expTOStream, assayTOStream);
            this.assayType = assayType;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            log.traceEntry("{}", action);

            this.initializeTryAdvance();

            T assay = this.loadAssayAdvanceTOIterator(null);
            if (assay == null) {
                return log.traceExit(false);
            }
            if (this.assayType.isInstance(assay)) {
                action.accept(this.assayType.cast(assay));
            } else {
                throw log.throwing(new IllegalStateException("Unexpected class for Assay, expected "
                        + this.assayType + " but was " + assay.getClass()));
            }
            return log.traceExit(true);
        }
    }

    public class RawCallSourceSpliterator<T extends RawCallSource<?>, U extends CallSourceTO<?>,
    V extends AssayTO<?>, W extends Assay<?>, X extends ExperimentTO<?>, Y extends Experiment<?>>
    extends CommonRawDataSpliterator<T, V, W, X, Y> {

        private final Stream<U> callSourceTOStream;
        private Iterator<U> callSourceTOIterator;
        private W lastAssay;
        private Class<T> callRawSourceType;

        public RawCallSourceSpliterator(Stream<X> expTOStream, Stream<V> assayTOStream,
                Stream<U> callSourceTOStream, Class<T> callRawSourceType) {
            super(expTOStream, assayTOStream);
            if (assayTOStream == null || callSourceTOStream == null) {
                throw new IllegalArgumentException("Assay and RawCallSource streams cannot be null");
            }

            this.callSourceTOStream = callSourceTOStream;
            this.callSourceTOIterator = null;
            this.lastAssay = null;
            this.callRawSourceType = callRawSourceType;
        }

        @Override
        protected boolean initializeTryAdvance() {
            log.traceEntry();
            if (super.initializeTryAdvance()) {
                this.callSourceTOIterator = this.callSourceTOStream.iterator();
                return log.traceExit(true);
            }
            return log.traceExit(false);
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            log.traceEntry("{}", action);

            this.initializeTryAdvance();

            U callSourceTO = null;
            try {
                callSourceTO = this.callSourceTOIterator.next();
            } catch (NoSuchElementException e) {
                log.catching(Level.DEBUG, e);
                return log.traceExit(false);
            }

            if (!this.checkLastAssayTOIsValid(callSourceTO.getAssayId())) {
                this.lastAssay = this.loadAssayAdvanceTOIterator(callSourceTO.getAssayId());
            }
            RawCallSource<?> callSource = mapRawCallSourceTOToRawCallSource(callSourceTO, this.lastAssay);
            if (this.callRawSourceType.isInstance(callSource)) {
                action.accept(this.callRawSourceType.cast(callSource));
            } else {
                throw log.throwing(new IllegalStateException("Unexpected class for RawCallSource, expected "
                        + this.callRawSourceType + " but was " + callSource.getClass()));
            }
            return log.traceExit(true);
        }

        /**
         * Close {@code Stream}s provided at instantiation.
         */
        @Override
        public void close() {
            log.traceEntry();
            if (!this.isClosed){
                try {
                    this.callSourceTOStream.close();
                } finally {
                    super.close();
                }
            }
            log.traceExit();
        }
    }

    private static <T extends Experiment<?>, U extends ExperimentTO<?>> T mapExperimentTOToExperiment(U expTO) {
        log.traceEntry("{}", expTO);
        //TODO
        return log.traceExit((T) null);
    }
    private static <T extends Assay<?>, U extends AssayTO<?>> T mapAssayTOToAssay(U assayTO) {
        log.traceEntry("{}", assayTO);
        //TODO
        return log.traceExit((T) null);
    }
    private static <T extends AssayPartOfExp<?, V>, U extends AssayPartOfExpTO<?, ?>, V extends Experiment<?>>
    T mapAssayPartOfExpTOToAssayPartOfExp(U assayTO, V exp) {
        log.traceEntry("{}, {}", assayTO, exp);
        //TODO
        return log.traceExit((T) null);
    }
    private static <T extends  RawCallSource<V>, U extends CallSourceTO<?>, V extends Assay<?>>
    T mapRawCallSourceTOToRawCallSource(U callSourceTO, V assay) {
        log.traceEntry("{}, {}", callSourceTO, assay);
        //TODO
        return log.traceExit((T) null);
    }

    private static DAORawDataFilter convertRawDataFilterToDAORawDataFilter(RawDataFilter rawDataFilter,
            Map<Integer, Gene> geneMap) {
        log.traceEntry("{}, {}",rawDataFilter, geneMap);
        if (rawDataFilter == null) {
            return log.traceExit((DAORawDataFilter) null);
        }
        Entry<Set<Integer>, Set<Integer>> geneIdsSpeciesIdsForDAOs =
                convertGeneFiltersToBgeeGeneIdsAndSpeciesIds(rawDataFilter.getGeneFilters(), geneMap);
        return log.traceExit(new DAORawDataFilter(
                geneIdsSpeciesIdsForDAOs.getKey(), geneIdsSpeciesIdsForDAOs.getValue(),

                rawDataFilter.getConditionFilters().stream()
                    .map(cf -> convertRawDataConditionFilterToDAORawDataConditionFilter(cf))
                    .collect(Collectors.toSet())
                ));
    }
    protected static DAORawDataConditionFilter convertRawDataConditionFilterToDAORawDataConditionFilter(
            RawDataConditionFilter condFilter) {
        log.traceEntry("{}", condFilter);
        if (condFilter == null) {
            return log.traceExit((DAORawDataConditionFilter) null);
        }
        return log.traceExit(new DAORawDataConditionFilter(condFilter.getAnatEntityIds(), condFilter.getDevStageIds(),
                condFilter.getCellTypeIds(), condFilter.getSexes(), condFilter.getStrains(),
                condFilter.getIncludeSubConditions(), condFilter.getIncludeParentConditions()));
    }

    private final GeneDAO geneDAO;

    public RawDataService(ServiceFactory serviceFactory) {
        super(serviceFactory);
        this.geneDAO = this.getDaoManager().getGeneDAO();
    }

    public RawDataLoader getRawDataLoader(RawDataFilter filter) {
        log.traceEntry("{}", filter);
        return log.traceExit(this.getRawDataLoader(Collections.singleton(filter)));
    }
    private RawDataLoader getRawDataLoader(Collection<RawDataFilter> filters) {
        log.traceEntry("{}", filters);
        if (filters == null || filters.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("A RawDataFilter must be provided"));
        }
//        if (filters.contains(null)) {
//            throw log.throwing(new IllegalArgumentException("No RawDataFilter can be null"));
//        }
        final Set<RawDataFilter> clonedFilters = Collections.unmodifiableSet(new HashSet<>(filters));

        //we prepare the info the Loader will need when calling its various "load" methods.
        final Set<GeneFilter> geneFilters = Collections.unmodifiableSet(clonedFilters.stream()
                .flatMap(f -> f.getGeneFilters().stream())
                .collect(Collectors.toSet()));
        final Map<Integer, Species> speciesMap = this.getServiceFactory().getSpeciesService()
                .loadSpeciesMapFromGeneFilters(geneFilters, true);
        final Map<Integer, Gene> geneMap = loadGeneMapFromGeneFilters(geneFilters, speciesMap, null, this.geneDAO);
        Set<DAORawDataFilter> daoRawDataFilters = Collections.unmodifiableSet(clonedFilters.stream()
                .map(f -> convertRawDataFilterToDAORawDataFilter(f, geneMap))
                .collect(Collectors.toSet()));

        return log.traceExit(new RawDataLoader(clonedFilters, this, geneMap, daoRawDataFilters));
    }

    Stream<AffymetrixProbeset> loadAffymetrixProbesets(Set<RawDataFilter> filters) {
        log.traceEntry("{}", filters);
        if (filters == null || filters.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("A RawDataFilter must be provided"));
        }
        if (filters.contains(null)) {
            throw log.throwing(new IllegalArgumentException("No RawDataFilter can be null"));
        }

        //TODO: to continue
        return null;
        
    }

    //*************************************************************************
    // METHODS PERFORMING THE QUERIES TO THE DAOs
    //*************************************************************************
    

    public RawDataContainer loadExperiments(RawDataFilter dataFilter,
            Collection<RawDataDataType> dataTypes, Collection<String> experimentIds, 
            Collection<String> assayIds, EnumSet<Attribute> attrs) {
        log.traceEntry("{}, {}, {}, {}, {}", dataFilter, dataTypes, assayIds, experimentIds, attrs);
        RawDataLoader loader = this.getRawDataLoader(dataFilter);
        Stream<AffymetrixExperiment> affyExp = dataTypes.contains(RawDataDataType.AFFYMETRIX)?
                loader.loadAffymetrixExperiments(experimentIds, assayIds, attrs)
                : null;
        Stream<RnaSeqExperiment> rnaSeqExp = dataTypes.contains(RawDataDataType.RNASEQ)?
                loader.loadRnaSeqExperiments(experimentIds, assayIds, attrs)
                : null;
        return log.traceExit(new RawDataContainer(dataTypes, affyExp, null, null, rnaSeqExp,
                null, null, null, null, null, null, null, null));
    }
    
    //XXX: If each datatype specifique loading function retrieve a RawDataContainer, it si then
    //     possible to define in this function if we want only Assays or also to populate
    //     Experiments using a boolean that could be called withSeparateExperiment.
    //     It will allow not to consume the Assay streams neither to manipulate the Assay streams
    //     to retrieve unique experiments.
    // 
    // Not providing assay and exp IDs at instantiation of RawDataLoader allows to reuse the same
    // RawDataLoader when filtering on different assays or experiments.
    public RawDataContainer loadAssays(RawDataFilter dataFilter,
            Collection<RawDataDataType> dataTypes, Collection<String> experimentIds,
            Collection<String> assayIds, EnumSet<Attribute> attrs) {
        log.traceEntry("{}, {}, {}, {}, {}", dataFilter, dataTypes, assayIds, experimentIds, attrs);
        RawDataLoader loader = this.getRawDataLoader(dataFilter);
        Stream<AffymetrixChip> affyAssays = dataTypes.contains(RawDataDataType.AFFYMETRIX)?
                loader.loadAffymetrixChips(experimentIds, assayIds, attrs)
                : null;
        Stream<RnaSeqLibraryAnnotatedSample> rnaSeqLibraries= dataTypes.contains(RawDataDataType.RNASEQ)?
                loader.loadRnaSeqLibraryAnnotatedSample(experimentIds, assayIds, attrs)
                : null;
        //TODO continue once all datatype specific methods have been implemented
        return log.traceExit(new RawDataContainer(dataTypes, null, affyAssays, null, null, null,
                rnaSeqLibraries, null, null, null, null, null, null));
        }

  //XXX: If each datatype specifique loading function retrieve a RawDataContainer, it si then
    //     possible to define in this function if we want only Calls or also to populate
    //     Experiments and Assays using booleans that could be called withSeparateExperiment and
    //     withSeparateAssays. It will allow not to consume the Assay streams neither to manipulate
    //     the Assay streams to retrieve unique experiments.
    // 
    public RawDataContainer loadRawCalls(RawDataFilter dataFilter,
            EnumSet<RawDataDataType> dataTypes, Set<String> experimentIds, Set<String> assayIds,
            EnumSet<Attribute> attrs) {
        log.traceEntry("{}, {}, {}, {}, {}", dataFilter, dataTypes, assayIds, experimentIds, attrs);
        RawDataLoader loader = this.getRawDataLoader(dataFilter);
        Stream<AffymetrixProbeset> affyCalls = dataTypes.contains(RawDataDataType.AFFYMETRIX)?
                loader.loadAffymetrixProbesets(experimentIds, assayIds, null, attrs)
                : null;
        //TODO continue once all datatype specific methods have been implemented
        return log.traceExit(new RawDataContainer(dataTypes, null, null, affyCalls, null,
                null, null, null, null, null, null, null, null));
    }

}
