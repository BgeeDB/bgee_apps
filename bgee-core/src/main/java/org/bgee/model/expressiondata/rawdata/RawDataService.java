package org.bgee.model.expressiondata.rawdata;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.CommonService;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataAssayDAO.AssayPartOfExpTO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataAssayDAO.AssayTO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCallSourceDAO.CallSourceTO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataExperimentDAO.ExperimentTO;

public class RawDataService extends CommonService {
    private final static Logger log = LogManager.getLogger(RawDataService.class.getName());

    public RawDataService(ServiceFactory serviceFactory) {
        super(serviceFactory);
        // TODO Auto-generated constructor stub
    }

    public class CallSpliterator<T extends RawCallSource<?>> extends Spliterators.AbstractSpliterator<T> {

        private final Stream<ExperimentTO> expTOStream;
        private final Stream<AssayTO> assayTOStream;
        private final Stream<CallSourceTO> callSourceTOStream;
        private Iterator<ExperimentTO> expTOIterator;
        private Iterator<AssayTO> assayTOIterator;
        private Iterator<CallSourceTO> callSourceTOIterator;
        private AssayTO lastAssayTO;
        private Assay lastAssay;
        private ExperimentTO lastExpTO;
        private Experiment lastExp;

        private boolean isInitiated;
        private boolean isClosed;

        public CallSpliterator(Stream<ExperimentTO> expTOStream, Stream<AssayTO> assayTOStream,
                Stream<CallSourceTO> callSourceTOStream, Class<T> callRawSourceType) {
            //TODO: check this call to 'super'
            super(Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.IMMUTABLE 
                    | Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.SORTED);
            if (assayTOStream == null || callSourceTOStream == null) {
                throw new IllegalArgumentException("Assay and RawCallSource streams cannot be null");
            }

            this.expTOStream = expTOStream;
            this.assayTOStream = assayTOStream;
            this.callSourceTOStream = callSourceTOStream;
            this.expTOIterator = null;
            this.assayTOIterator = null;
            this.callSourceTOIterator = null;
            this.lastAssay = null;
            this.lastAssayTO = null;
            this.lastExp = null;
            this.lastExpTO = null;
            this.isInitiated = false;
            this.isClosed = false;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            log.entry(action);

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
                this.callSourceTOIterator = this.callSourceTOStream.iterator();
            }

            CallSourceTO callSourceTO = null;
            try {
                callSourceTO = this.callSourceTOIterator.next();
            } catch (NoSuchElementException e) {
                log.catching(Level.DEBUG, e);
                return log.exit(false);
            }

            if (this.lastAssayTO == null || !this.lastAssayTO.getId().equals(callSourceTO.getAssayId())) {
                this.lastAssayTO = this.assayTOIterator.next();
                if (this.lastAssayTO == null || !this.lastAssayTO.getId().equals(callSourceTO.getAssayId())) {
                    throw log.throwing(new IllegalStateException("No assay matching the call source assay ID "
                            + callSourceTO.getAssayId()
                            + ". Either the call sources and assays were not properly ordered, or problem in data retrieval"));
                }

                assert this.expTOIterator == null && !(this.lastAssayTO instanceof AssayPartOfExpTO) ||
                        this.expTOIterator != null && this.lastAssayTO instanceof AssayPartOfExpTO;
                if (this.expTOIterator != null && this.lastAssayTO instanceof AssayPartOfExpTO) {
                    AssayPartOfExpTO<?> assayTO = (AssayPartOfExpTO<?>) this.lastAssayTO;
                    if (this.lastExpTO == null || !this.lastExpTO.getId().equals(assayTO.getExperimentId())) {
                        this.lastExpTO = this.expTOIterator.next();
                    }
                    if (this.lastExpTO == null || !this.lastExpTO.getId().equals(assayTO.getExperimentId())) {
                        throw log.throwing(new IllegalStateException("No experiment matching the call source experiment ID "
                                + assayTO.getExperimentId()
                                + ". Either the assays and experiment were not properly ordered, or problem in data retrieval"));
                    }
                    this.lastExp = mapExperimentTOToExperiment(this.lastExpTO);
                }
                if (this.lastAssayTO instanceof AssayPartOfExpTO) {
                    this.lastAssay = mapAssayPartOfExpTOToAssayPartOfExp((AssayPartOfExpTO<?>) this.lastAssayTO,
                            this.lastExp);
                } else {
                    this.lastAssay = mapAssayTOToAssay(this.lastAssayTO);
                }
            }
            RawCallSource<?> callSource = mapRawCallSourceTOToRawCallSource(callSourceTO, this.lastAssay);
            action.accept((T) callSource);
            return log.exit(true);
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
        public void close() {
            log.entry();
            if (!this.isClosed){
                try {
                    this.callSourceTOStream.close();
                    this.assayTOStream.close();
                    this.expTOStream.close();
                } finally {
                    this.isClosed = true;
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
    private static <T extends AssayPartOfExp<?, V>, U extends AssayPartOfExpTO<?>, V extends Experiment<?>>
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
}
