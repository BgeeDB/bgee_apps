package org.bgee.model.expressiondata.rawdata;

import java.io.Closeable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
import org.bgee.model.expressiondata.Condition;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixProbeset;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.species.Species;

//XXX: use Java 9 modules to allow access to loading methods only to RawDataLoader and CountLoaders
public class RawDataService extends CommonService {
    private final static Logger log = LogManager.getLogger(RawDataService.class.getName());

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
            log.entry(expectedAssayTOId);
            if (expectedAssayTOId == null) {
                throw log.throwing(new IllegalArgumentException("The expected AssayTO ID cannot be null"));
            }
            return log.exit(this.lastAssayTO != null && expectedAssayTOId.equals(this.lastAssayTO.getId()));
        }

        protected <Y> V loadAssayAdvanceTOIterator(Y expectedAssayTOId) {
            log.entry(expectedAssayTOId);

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
                return log.exit(null);
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
                return log.exit(mapAssayPartOfExpTOToAssayPartOfExp((AssayPartOfExpTO<?, ?>) this.lastAssayTO, this.lastExp));
            }
            return log.exit(mapAssayTOToAssay(this.lastAssayTO));
        }

        protected boolean initializeTryAdvance() {
            log.entry();
            if (this.isClosed) {
                throw log.throwing(new IllegalStateException("Already close"));
            }
            // Lazy loading: we do not get stream iterators (terminal operation)
            // before tryAdvance() is called.
            if (!this.isInitiated) {
                //set it first because method can return false and exist the block
                this.isInitiated = true;

                if (expTOStream != null) {
                    this.expTOIterator = this.expTOStream.iterator();
                }
                this.assayTOIterator = this.assayTOStream.iterator();
                return log.exit(true);
            }
            return log.exit(false);
        }

        /**
         * Return {@code null}, because a {@code CallSpliterator} does not have 
         * the capability of being accessed in parallel. 
         * 
         * @return  The {@code Spliterator} that is {@code null}.
         */
        @Override
        public Spliterator<T> trySplit() {
            log.entry();
            return log.exit(null);
        }

        @Override
        public Comparator<? super T> getComparator() {
            log.entry();
            //TODO?
            return log.exit(null);
        }

        /** 
         * Close {@code Stream}s provided at instantiation.
         */
        @Override
        public void close() {
            log.entry();
            if (!this.isClosed){
                try {
                    this.assayTOStream.close();
                    this.expTOStream.close();
                } finally {
                    this.isClosed = true;
                }
            }
            log.exit();
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
            log.entry(action);

            this.initializeTryAdvance();

            T assay = this.loadAssayAdvanceTOIterator(null);
            if (assay == null) {
                return log.exit(false);
            }
            if (this.assayType.isInstance(assay)) {
                action.accept(this.assayType.cast(assay));
            } else {
                throw log.throwing(new IllegalStateException("Unexpected class for Assay, expected "
                        + this.assayType + " but was " + assay.getClass()));
            }
            return log.exit(true);
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
            log.entry();
            if (super.initializeTryAdvance()) {
                this.callSourceTOIterator = this.callSourceTOStream.iterator();
                return log.exit(true);
            }
            return log.exit(false);
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            log.entry(action);

            this.initializeTryAdvance();

            U callSourceTO = null;
            try {
                callSourceTO = this.callSourceTOIterator.next();
            } catch (NoSuchElementException e) {
                log.catching(Level.DEBUG, e);
                return log.exit(false);
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
            return log.exit(true);
        }

        /**
         * Close {@code Stream}s provided at instantiation.
         */
        @Override
        public void close() {
            log.entry();
            if (!this.isClosed){
                try {
                    this.callSourceTOStream.close();
                } finally {
                    super.close();
                }
            }
            log.exit();
        }
    }

    private static <T extends Experiment<?>, U extends ExperimentTO<?>> T mapExperimentTOToExperiment(U expTO) {
        log.entry(expTO);
        //TODO
        return log.exit(null);
    }
    private static <T extends Assay<?>, U extends AssayTO<?>> T mapAssayTOToAssay(U assayTO) {
        log.entry(assayTO);
        //TODO
        return log.exit(null);
    }
    private static <T extends AssayPartOfExp<?, V>, U extends AssayPartOfExpTO<?, ?>, V extends Experiment<?>>
    T mapAssayPartOfExpTOToAssayPartOfExp(U assayTO, V exp) {
        log.entry(assayTO, exp);
        //TODO
        return log.exit(null);
    }
    private static <T extends  RawCallSource<V>, U extends CallSourceTO<?>, V extends Assay<?>>
    T mapRawCallSourceTOToRawCallSource(U callSourceTO, V assay) {
        log.entry(callSourceTO, assay);
        //TODO
        return log.exit(null);
    }

    private static DAORawDataFilter convertRawDataFilterToDAORawDataFilter(RawDataFilter rawDataFilter,
            Map<Integer, Gene> geneMap) {
        log.entry(rawDataFilter, geneMap);
        if (rawDataFilter == null) {
            return log.exit(null);
        }
        Entry<Set<Integer>, Set<Integer>> geneIdsSpeciesIdsForDAOs =
                convertGeneFiltersToBgeeGeneIdsAndSpeciesIds(rawDataFilter.getGeneFilters(), geneMap);
        return log.exit(new DAORawDataFilter(
                geneIdsSpeciesIdsForDAOs.getKey(), geneIdsSpeciesIdsForDAOs.getValue(),

                rawDataFilter.getConditionFilters().stream()
                    .map(cf -> convertRawDataConditionFilterToDAORawDataConditionFilter(cf))
                    .collect(Collectors.toSet())
                ));
    }
    private static DAORawDataConditionFilter convertRawDataConditionFilterToDAORawDataConditionFilter(
            RawDataConditionFilter condFilter) {
        log.entry(condFilter);
        if (condFilter == null) {
            return log.exit(null);
        }
        return log.exit(new DAORawDataConditionFilter(condFilter.getAnatEntityIds(), condFilter.getDevStageIds(),
                condFilter.getIncludeSubConditions(), condFilter.getIncludeParentConditions()));
    }

    private final GeneDAO geneDAO;

    public RawDataService(ServiceFactory serviceFactory) {
        super(serviceFactory);
        this.geneDAO = this.getDaoManager().getGeneDAO();
    }

    public RawDataLoader getRawDataLoader(RawDataFilter filter) {
        log.entry(filter);
        return log.exit(this.getRawDataLoader(Collections.singleton(filter)));
    }
    private RawDataLoader getRawDataLoader(Collection<RawDataFilter> filters) {
        log.entry(filters);
        if (filters == null || filters.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("A RawDataFilter must be provided"));
        }
        if (filters.contains(null)) {
            throw log.throwing(new IllegalArgumentException("No RawDataFilter can be null"));
        }
        final Set<RawDataFilter> clonedFilters = Collections.unmodifiableSet(new HashSet<>(filters));

        //we prepare the info the Loader will need when calling its various "load" methods.
        final Set<GeneFilter> geneFilters = Collections.unmodifiableSet(clonedFilters.stream()
                .flatMap(f -> f.getGeneFilters().stream())
                .collect(Collectors.toSet()));
        final Map<Integer, Species> speciesMap = loadSpeciesMapFromGeneFilters(geneFilters,
                this.getServiceFactory().getSpeciesService());
        final Map<Integer, Gene> geneMap = loadGeneMapFromGeneFilters(geneFilters, speciesMap, this.geneDAO);
        Set<DAORawDataFilter> daoRawDataFilters = Collections.unmodifiableSet(clonedFilters.stream()
                .map(f -> convertRawDataFilterToDAORawDataFilter(f, geneMap))
                .collect(Collectors.toSet()));

        return log.exit(new RawDataLoader(clonedFilters, this));
    }

    Stream<AffymetrixProbeset> loadAffymetrixProbesets(Set<RawDataFilter> filters) {
        log.entry(filters);
        if (filters == null || filters.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("A RawDataFilter must be provided"));
        }
        if (filters.contains(null)) {
            throw log.throwing(new IllegalArgumentException("No RawDataFilter can be null"));
        }

        
    }


    //*************************************************************************
    // METHODS PERFORMING THE QUERIES TO THE DAOs
    //*************************************************************************
    private Map<Integer, RawDataCondition> loadRawDataConditionMap(Collection<Species> species) {
        
    }
}
