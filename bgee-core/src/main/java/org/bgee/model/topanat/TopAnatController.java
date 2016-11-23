package org.bgee.model.topanat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeProperties;
import org.bgee.model.ServiceFactory;
import org.bgee.model.function.PentaFunction;
import org.bgee.model.job.Job;

/**
 * @author Mathieu Seppey
 * @author Frederic Bastian
 * @version Bgee 13 Oct. 2016
 * @since Bgee 13
 */
//XXX: rename to TopAnatService, to be obtain through the ServiceFactory?
//Or create another TopAnatService class?
//This TopAnatService should receive the JobService instance at instantiation?
public class TopAnatController {
    private final static Logger log = LogManager.getLogger(TopAnatController.class.getName()); 

    /**
     * 
     */
    private final static ConcurrentMap<String, ReentrantReadWriteLock> readWriteLocks =
            new ConcurrentHashMap<String, ReentrantReadWriteLock>();

    /**
     * 
     */
    private final List<TopAnatParams> topAnatParams;

    /**
     * 
     */
    private final BgeeProperties props;

    /**
     * 
     */
    private final ServiceFactory serviceFactory;

    /**
     * A {@code PentaFunction} allowing to obtain new {@code TopAnatAnalysis} instances.
     */
    private final PentaFunction<TopAnatParams, BgeeProperties, ServiceFactory, TopAnatRManager,TopAnatController, TopAnatAnalysis> 
    topAnatAnalysisSupplier;
    
    /**
     * An {@code Optional} {@code Job} allowing to track advancement of the analyses.
     */
    private final Optional<Job> job;

    /**
     * 
     * @param topAnatParams
     */
    public TopAnatController(List<TopAnatParams> topAnatParams) {
        this(topAnatParams, BgeeProperties.getBgeeProperties(), new ServiceFactory());
    }
    /**
     * 
     * @param topAnatParams
     * @param props
     */
    public TopAnatController(List<TopAnatParams> topAnatParams, BgeeProperties props, 
            ServiceFactory serviceFactory) {
        this(topAnatParams, props, serviceFactory, TopAnatAnalysis::new);
    }
    public TopAnatController(List<TopAnatParams> topAnatParams, BgeeProperties props, 
            ServiceFactory serviceFactory, Job job) {
        this(topAnatParams, props, serviceFactory, TopAnatAnalysis::new, job);
    }
    
    public TopAnatController(List<TopAnatParams> topAnatParams, BgeeProperties props, 
            ServiceFactory serviceFactory, 
            PentaFunction<TopAnatParams, BgeeProperties, ServiceFactory, TopAnatRManager, TopAnatController,
            TopAnatAnalysis> topAnatAnalysisSupplier) {
        this(topAnatParams, props, serviceFactory, topAnatAnalysisSupplier, null);
    }

    /**
     * 
     * @param params
     */
    public TopAnatController(List<TopAnatParams> topAnatParams, BgeeProperties props, 
            ServiceFactory serviceFactory, 
            PentaFunction<TopAnatParams, BgeeProperties, ServiceFactory, TopAnatRManager, TopAnatController,
            TopAnatAnalysis> topAnatAnalysisSupplier, Job job) {
        log.entry(topAnatParams, props, serviceFactory, topAnatAnalysisSupplier, job);

        if (topAnatParams == null || topAnatParams.isEmpty() || 
                topAnatParams.stream().anyMatch(Objects::isNull)) {
            throw log.throwing(new IllegalArgumentException("At least one TopAnatParams "
                    + "must be provided, and none should be null"));
        }
        if (topAnatAnalysisSupplier == null) {
            throw log.throwing(new IllegalArgumentException("A supplier of TopAnatAnalysis "
                    + "must be provided"));
        }
        if (props == null) {
            throw log.throwing(new IllegalArgumentException("A BgeeProperties object must be provided."));
        }
        if (serviceFactory == null) {
            throw log.throwing(new IllegalArgumentException("A ServiceFactory must be provided."));
        }
        this.topAnatParams = Collections.unmodifiableList(new ArrayList<>(topAnatParams));
        this.topAnatAnalysisSupplier = topAnatAnalysisSupplier;
        this.props = props;
        this.serviceFactory = serviceFactory;
        this.job = Optional.ofNullable(job);

        log.exit();
    }

    /**
     * @throws IOException
     */
    public Stream<TopAnatResults> proceedToTopAnatAnalyses() {
        log.entry();

        // Create TopAnatAnalysis for each TopAnatParams
        //TODO: TopAnatAnalysis should be provided with the Job instance to be able to use 
        //'checkInterrupted'

        return log.exit(this.topAnatParams.stream()
                .map(params -> this.topAnatAnalysisSupplier.apply(params, this.props, 
                        this.serviceFactory, new TopAnatRManager(this.props, params),this))
                .map(analysis -> {
                    try {
                        //if task in job not yet started (first analysis), start it.
                        if (this.job.map(t -> !t.isStarted()).orElse(false)) {
                            this.job.ifPresent(t -> t.startJob());
                        } else {
                            //otherwise, check if job was interrupted and move to next subtask
                            this.job.ifPresent(t -> {
                                try {
                                    t.checkInterrupted();
                                    t.nextTask();
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                        
                        TopAnatResults results = analysis.proceedToAnalysis();
                        
                        //end job if last analysis
                        if (this.job.map(t -> t.getCurrentTaskIndex()).orElse(-1) == 
                                (this.topAnatParams.size() - 1)) {
                            this.job.ifPresent(t -> t.completeWithSuccess());
                        }
                        
                        return results;
                    } catch (Throwable e) {
                        //catch and throw this error in DEBUG level because we don't want 
                        //to log those as errors when we requested a Thread interruption
                        log.catching(Level.DEBUG, e);
                        this.job.ifPresent(t -> t.complete());
                        throw log.throwing(Level.DEBUG, new RuntimeException(e));
                    }
                }));
    }
    
    public BgeeProperties getBgeeProperties() {
        return this.props;
    }
    /**
     * @return  An {@code Optional} {@code Job} allowing to track advancement of the analyses.
     */
    public Optional<Job> getJob() {
        return job;
    }
    
    public List<TopAnatParams> getTopAnatParams() {
        return this.topAnatParams;
    }

    // *************************************************
    // FILE LOCKING
    // *************************************************
    /**
     * Acquires a write lock corresponding to the {@code fileName} by
     * calling the {@link #acquireLock(String, boolean)} method
     * 
     * @param fileName
     *            a {@code String} corresponding to the fileName to acquire
     *            the read lock.
     * @see #acquireLock(String, boolean)
     */
    void acquireReadLock(String fileName) {
        this.acquireLock(fileName, true);
    }

    /**
     * Acquires a write lock corresponding to the {@code fileName} by
     * calling the {@link #acquireLock(String, boolean)} method
     * 
     * @param fileName
     *            a {@code String} corresponding to the fileName to acquire
     *            the write lock.
     * @see #acquireLock(String, boolean)
     */
    void acquireWriteLock(String fileName) {
        this.acquireLock(fileName, false);
    }

    /**
     * Releases the write lock corresponding to the {@code fileName} by
     * calling the {@link #releaseLock(String, boolean)} method
     * 
     * @param fileName
     *            a {@code String} corresponding to the fileName to release
     *            the write lock
     * @see #releaseLock(String, boolean)
     */
    void releaseWriteLock(String fileName) {
        this.releaseLock(fileName, false);
    }

    /**
     * Method to acquire a lock on a file, corresponding to the param
     * {@code fileName}
     * 
     * @param fileName
     *            a {@code String} corresponding to the fileName to
     *            retrieve the lock from {@code readWriteLocks}
     * @param readLock
     *            {@code true} if a read lock should be acquired.
     *            {@code false} if it should be a read lock
     * @see #readWriteLocks
     */
    void acquireLock(String fileName, boolean readLock) {
        ReentrantReadWriteLock lock = this.getReadWriteLock(fileName);

        if (readLock) {
            lock.readLock().lock();
        } else {
            lock.writeLock().lock();
        }
        // {@code removeLockIfPossible(String)} determines whether the lock
        // could be removed
        // from the {@code ConcurrentHashMap} {@code readWriteLocks}.
        // The problem is that {@code removeLockIfPossible(String)} could
        // remove the lock from the map,
        // AFTER this method acquire a lock and put it in the map
        // (this.getReadWriteLock(this.getGeneratedKey())),
        // but BEFORE actually locking it (lock.readLock().lock()).
        // To solve this issue, this method will test after locking the lock
        // whether it is still in the map,
        // or whether the element present in the map is equal to the "locked"
        // lock.
        // If it is not, it will call again
        // {@code getReadWriteLock(String)}
        // to generate a new lock to be put in the map, or to obtain the lock
        // generated by another thread.
        while (readWriteLocks.get(fileName) == null
                || !readWriteLocks.get(fileName).equals(lock)) {

            lock = this.getReadWriteLock(fileName);
            if (readLock) {
                lock.readLock().lock();
            } else {
                lock.writeLock().lock();
            }
        }
    }

    /**
     * Releases the read lock corresponding to the {@code fileName} by
     * calling the {@link #releaseLock(String, boolean)} method
     * 
     * @param fileName
     *            a {@code String} corresponding to the fileName to release
     *            the read lock
     * @see #releaseLock(String, boolean)
     */
    void releaseReadLock(String fileName) {
        this.releaseLock(fileName, true);
    }

    /**
     * Method to release a lock on a file, corresponding to the param
     * {@code fileName}
     * 
     * @param fileName
     *            a {@code String} corresponding to the fileName to release
     *            the lock from {@code readWriteLocks}
     * @param readLock
     *            {@code true} if a read lock should be acquired.
     *            {@code false} if it should be a read lock
     * @see #readWriteLocks
     */
    void releaseLock(String fileName, boolean readLock) {
        ReentrantReadWriteLock lock = this.getReadWriteLock(fileName);
        this.removeLockIfPossible(fileName, readLock? 1: 0);
        if (readLock) {
            lock.readLock().unlock();
        } else {
            lock.writeLock().unlock();
        }
    }

    /**
     * Try to remove the {@code ReentrantReadWriteLock} corresponding to
     * the param {@code fileName}, from the {@code ConcurrentHashMap}
     * {@code readWriteLocks}. The lock will be removed from the map only
     * if there are no read or write locks, and no ongoing request for a read or
     * write lock.
     * <p>
     * Note: there might be here a race, where another thread acquired the lock
     * and actually locked it, i) just after this method tests the presence of
     * read or write locks and ongoing requests for a read or write lock, and
     * ii) just before removing it from the map. To solve this issue, methods
     * acquiring a lock must check after locking it whether it is still in the
     * readWriteLocks map, or whether the element present in the map for the key
     * is equal to the acquired lock. If it is not, they must generate a new
     * lock to be used.
     * 
     * @param fileName
     *            a {@code String} corresponding to the fileName to
     *            retrieve the lock from {@code readWriteLocks}, to remove
     *            it.
     * @see #readWriteLocks
     */
    void removeLockIfPossible(String fileName, int expectedReadLockCount) {
        log.entry(fileName, expectedReadLockCount);
        // check if there is already a lock stored for this key
        ReentrantReadWriteLock lock = readWriteLocks.get(fileName);

        // there is a lock to remove
        if (lock != null) {
            // there is no thread with write lock, or read lock, or waiting to
            // acquire a lock
            if (lock.getReadLockCount() <= expectedReadLockCount && !lock.hasQueuedThreads()) {
                log.trace("Removing Lock from Map");
                // there might be here a race, where another thread acquired the
                // lock and
                // actually locked it, just after the precedent condition test,
                // and just before the following remove statement.
                // to solve this issue, methods acquiring a lock must check
                // after locking it
                // whether it is still in the readWriteLocks map.
                // if it is not, they must generate a new lock to be used.
                readWriteLocks.remove(fileName);
            }
        }
        log.exit();
    }

    /**
     * Obtain a {@code ReentrantReadWriteLock}, for the param
     * {@code fileName}.
     * 
     * This method tries to obtain {@code ReentrantReadWriteLock}
     * corresponding to the fileName, from the {@code ConcurrentHashMap}
     * {@code readWriteLocks}. If the lock is not already stored, create a
     * new one, and put it in {@code readWriteLocks}, to be used by other
     * threads.
     * 
     * @param fileName
     *            a {@code String} corresponding to the fileName to
     *            retrieve the lock from {@code readWriteLocks}.
     * 
     * @return a {@code ReentrantReadWriteLock} corresponding to the
     *         fileName.
     * 
     * @see #readWriteLocks
     */
    ReentrantReadWriteLock getReadWriteLock(String fileName) {
        // check if there is already a lock stored for this key
        ReentrantReadWriteLock readWritelock = readWriteLocks.get(fileName);

        // no lock already stored
        if (readWritelock == null) {
            ReentrantReadWriteLock newReadWriteLock = new ReentrantReadWriteLock(
                    true);
            // try to put the new lock in the ConcurrentHashMap
            readWritelock = readWriteLocks.putIfAbsent(fileName,
                    newReadWriteLock);
            // if readWritelock is null, the newLock has been successfully put
            // in the map, and we use it.
            // otherwise, it means that another thread has inserted a new lock
            // for this key in the mean time.
            // readWritelock then corresponds to this value, that we should use.
            if (readWritelock == null) {
                readWritelock = newReadWriteLock;
            }
        }
        return readWritelock;
    }

    /**
     * 
     * @return
     */
    public boolean areAnalysesDone(){
        log.entry();
        return log.exit(this.topAnatParams.stream()
                .map(params -> this.topAnatAnalysisSupplier.apply(params, this.props, 
                        this.serviceFactory, new TopAnatRManager(this.props, params), this))
                .allMatch(a -> a.isAnalysisDone()));
    }
}
