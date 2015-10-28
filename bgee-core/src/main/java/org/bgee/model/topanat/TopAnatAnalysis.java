package org.bgee.model.topanat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeProperties;
import org.bgee.model.QueryTool;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.expressiondata.CallFilter;
import org.bgee.model.expressiondata.CallService;

/**
 * @author Mathieu Seppey
 */
public class TopAnatAnalysis {

    /**
     * 
     */
    private final static Logger log = LogManager
            .getLogger(TopAnatAnalysis.class.getName());

    /**
     * 
     */
    private final TopAnatParams params;

    /**
     * 
     */
    private final TopAnatRManager rManager;

    /**
     * 
     */
    private final BgeeProperties props;


    /**
     * {@code ConcurrentMap} used to manage concurrent access to the
     * read/write locks that are used to manage concurrent reading and writing
     * of the files that can be simultaneously accessed by different threads. In
     * this {@code Map}, {@code keys} are file names, and
     * {@code values} are {@link ReentrantReadWriteLock}}.
     */
    private final static ConcurrentMap<String, ReentrantReadWriteLock> readWriteLocks =
            new ConcurrentHashMap<String, ReentrantReadWriteLock>();

    /**
     * 
     */
    private final CallService callService;

    /**
     * 
     */
    private final AnatEntityService anatEntityService;

    /**
     * 
     * @param params
     * @param props
     * @param serviceFactory
     */
    public TopAnatAnalysis(TopAnatParams params, BgeeProperties props, 
            ServiceFactory serviceFactory) {
        this(params, props, serviceFactory, new TopAnatRManager(props,params));
    }
    /**
     * @param params
     */
    public TopAnatAnalysis(TopAnatParams params, BgeeProperties props, 
            ServiceFactory serviceFactory, TopAnatRManager rManager) {
        log.entry(params, props, serviceFactory, rManager); 
        this.params = params;
        this.anatEntityService = 
                serviceFactory.getAnatEntityService(); 
        this.callService = serviceFactory.getCallService();
        this.rManager = rManager;
        this.props = props;
        log.exit();
    }

    /**
     * @throws IOException
     */
    public TopAnatResults proceedToAnalysis() throws IOException{
        log.entry();
        log.info("Result File: {}", this.params.getResultFileName());

        // Generate anatomic entities data
        this.generateAnatEntitiesFiles();

        // Generate call data
        this.generateGenesToAnatEntitiessAssociationFile();
        
        // perform R function and write all outputs
        try{
            // perform the R analysis
            try (PrintWriter out = new PrintWriter(new BufferedWriter(
                    new FileWriter(new File(
                            this.props.getTopAnatResultsWritingDirectory(),
                            this.params.getRScriptOutputFileName()))))) {
                out.println(this.rManager.generateRCode());
            }
            
            //create File to use its path as lock name (because the same name 
            //is used in other methods)
            File geneToAnatEntitiesAssociationFile = new File(
                    this.props.getTopAnatResultsWritingDirectory(),
                    this.params.getGeneToAnatEntitiesFileName());
            String geneToAnatEntitiesAssociationFilePath = geneToAnatEntitiesAssociationFile
                    .getPath();
            File namesFile = new File(
                    this.props.getTopAnatResultsWritingDirectory(),
                    this.params.getAnatEntitiesNamesFileName());
            String namesFileName = namesFile.getPath();
            File relsFile = new File(
                    this.props.getTopAnatResultsWritingDirectory(),
                    this.params.getAnatEntitiesRelationshipsFileName());
            String relsFileName = relsFile.getPath();

            try {
                this.acquireReadLock(namesFileName);
                this.acquireReadLock(relsFileName);
                this.acquireReadLock(geneToAnatEntitiesAssociationFilePath);

                this.rManager.performRFunction();

            } finally {
                this.releaseReadLock(namesFileName);
                this.releaseReadLock(relsFileName);
                this.releaseReadLock(geneToAnatEntitiesAssociationFilePath);
            }

        }
        finally{
            // delete tmp files
            // unlock lock
        }
        return log.exit(new TopAnatResults(
                this.props.getTopAnatResultsWritingDirectory()
                +this.params.getResultFileName()));
    }


    /**
     * Generates the AnatEntities ID to AnatEntities Name association file, and AnatEntities relationship file, 
     * only if they do not already exist.
     * <p>
     * The method will write into a file named 
     * {@link #AnatEntitiesNamesFileName}, the association between the AnatEntities IDs of the current
     * species and their name (see {@link #writeAnatEntitiesNamesToFile(String, String)}), and into 
     * a file named {@link #AnatEntitiesRelationshipsFileName}, the relations between the AnatEntitiess 
     * of the species (see {@link #writeAnatEntitiesRelationsToFile(String, String)}).
     * 
     * @throws IOException
     *             if the files cannot be opened or written to.
     * 
     * @see #AnatEntitiesNamesFileName
     * @see #AnatEntitiesRelationshipsFileName
     */
    private void generateAnatEntitiesFiles() throws IOException {
        log.entry();

        log.info("Generating AnatEntities files...");

        File namesFile = new File(
                this.props.getTopAnatResultsWritingDirectory(),
                this.params.getAnatEntitiesNamesFileName());
        String namesFileName = namesFile.getPath();

        File relsFile = new File(
                this.props.getTopAnatResultsWritingDirectory(),
                this.params.getAnatEntitiesRelationshipsFileName());
        String relsFileName = relsFile.getPath();

        //we will write results into a tmp file, moved at the end if everything 
        //went fine.
        String namesTmpFileName = namesFileName + ".tmp";
        Path namesTmpFile = Paths.get(namesTmpFileName);
        Path finalNamesFile = Paths.get(namesFileName);
        String relsTmpFileName = relsFileName + ".tmp";
        Path relsTmpFile = Paths.get(relsTmpFileName);
        Path finalRelsFile = Paths.get(relsFileName);

        try {
            this.acquireWriteLock(namesTmpFileName);
            this.acquireWriteLock(namesFileName);
            this.acquireWriteLock(relsTmpFileName);
            this.acquireWriteLock(relsFileName);

            //check, AFTER having acquired the locks, that the final files do not 
            //already exist (maybe another thread generated the files before this one 
            //acquires the lock)
            if (Files.exists(finalNamesFile) && Files.exists(finalRelsFile)) {
                log.info("AnatEntities files already generated.");
                log.exit(); return;
            }

            this.writeAnatEntitiesNamesToFile(namesTmpFileName);
            this.writeAnatEntitiesRelationsToFile(relsTmpFileName);

            //move tmp files if successful
            //We check that there were no database error that could have corrupted the results
            //            if (Database.getDatabase().isError()) {
            //                throw log.throwing(new IllegalStateException("A database error occurred, " +
            //                        "analysis canceled"));
            //            }

            Files.move(namesTmpFile, finalNamesFile, StandardCopyOption.REPLACE_EXISTING);
            Files.move(relsTmpFile, finalRelsFile, StandardCopyOption.REPLACE_EXISTING);

        } finally {
            Files.deleteIfExists(namesTmpFile);
            Files.deleteIfExists(relsTmpFile);
            this.releaseWriteLock(namesTmpFileName);
            this.releaseWriteLock(namesFileName);
            this.releaseWriteLock(relsTmpFileName);
            this.releaseWriteLock(relsFileName);
        }

        log.info("AnatEntitiesNamesFileName: {} - relationshipsFileName: {}", 
                this.params.getAnatEntitiesNamesFileName(), this.params.getAnatEntitiesRelationshipsFileName());
        log.exit();
    }

    /**
     * Write into the file {@code AnatEntitiesNameFile}, the association between names 
     * and IDs of AnatEntitiess, for the current species with the ID {@code speciesId}. It will be 
     * a TSV file with no header, with each AnatEntities corresponding to a line, with the ID 
     * in the first column, and the name in the second column.
     * <p>
     * Note that it is not the responsibility of this method to acquire a write lock 
     * on the file, it is the responsibility of the caller.
     * @param AnatEntitiesNameFile    A {@code String} that is the path to file where AnatEntities names 
     *                         will be written.
     * @throws IOException     If an error occurred while writing in the file.
     */
    private void writeAnatEntitiesNamesToFile(String AnatEntitiesNameFile) throws IOException {
        log.entry();

        try (PrintWriter out = new PrintWriter(new BufferedWriter(
                new FileWriter(AnatEntitiesNameFile)))) {
            this.anatEntityService.getAnatEntities(this.params.getSpeciesId())
            .forEach(entity 
                    -> out.println(entity.getId() + "\t" + entity.getName().replaceAll("'", "")));
        }

        log.exit();
    }

    /**
     * Write into the file {@code AnatEntitiesRelFile}, the direct relations between AnatEntitiess, 
     * for the current species with the ID {@code speciesId}. It will be a TSV file with 
     * no header, with each line corresponding to a relation, with the ID of the descent
     * AnatEntities in the first column, and the ID of the parent AnatEntities in the second column. 
     * Only part_of and is_a relations should be considered.
     * <p>
     * Note that it is not the responsibility of this method to acquire a write lock 
     * on the file, it is the responsibility of the caller.
     * 
     * @param AnatEntitiesRelFile     A {@code String} that is the path to file where AnatEntities relations 
     *                         will be written.
     *                         
     * @throws IOException     If an error occurred while writing in the file.
     */
    private void writeAnatEntitiesRelationsToFile(String AnatEntitiesRelFile)
            throws IOException {
        log.entry();

        try (PrintWriter out = new PrintWriter(new BufferedWriter(
                new FileWriter(AnatEntitiesRelFile)))) {
            this.anatEntityService.getAnatEntitiesRelationships(this.params.getSpeciesId())
            .forEach(
                    (id,descentIds) -> descentIds.forEach(
                            (descentId) -> out.println(descentId + '\t' + id)));
        }

        log.exit();
    }

    /**
     *
     */
    private void writeToGeneToAnatEntitiesFile(String geneToAnatEntitiesFile)
            throws IOException {

        log.entry(geneToAnatEntitiesFile); 

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
                geneToAnatEntitiesFile)))) {
            this.callService.loadCalls(
                    this.params.getSpeciesId(),new HashSet<CallFilter<?>>(
                            Arrays.asList(this.params.rawParametersToCallFilter()))
                    ).forEach(
                            call -> out.println(
                                    call.getGeneId() + '\t' + 
                                    call.getCondition().getAnatEntityId()
                                    )
                            );
        }
        log.exit();
    }    

    /**
     * Writes association between genes and the anatomical entities where they are 
     * expressed in a TSV file, named according to the value returned by 
     * {@link #getGeneToAnatEntitiesFileName()}.
     * 
     * @throws IOException
     *             if the {@code geneToAnatEntitiesFileName} cannot be opened or
     *             written to.
     * 
     * @see #geneToAnatEntitiesFileName
     * @see #writeToGeneToAnatEntitiesFile(String)
     */
    private void generateGenesToAnatEntitiessAssociationFile() throws IOException {
        log.entry();
        log.info("Generating Gene to AnatEntities Association file...");

        File geneToAnatEntitiesAssociationFile = new File(
                this.props.getTopAnatResultsWritingDirectory(),
                this.params.getGeneToAnatEntitiesFileName());
        String geneToAnatEntitiesAssociationFilePath = geneToAnatEntitiesAssociationFile
                .getPath();

        //we will write results into a tmp file, moved at the end if everything 
        //went fine.
        String tmpFileName = geneToAnatEntitiesAssociationFilePath + ".tmp";
        Path tmpFile = Paths.get(tmpFileName);
        Path finalGeneToAnatEntitiesFile = Paths.get(geneToAnatEntitiesAssociationFilePath);

        try {
            this.acquireWriteLock(geneToAnatEntitiesAssociationFilePath);
            this.acquireWriteLock(tmpFileName);

            //check, AFTER having acquired the locks, that the final file does not 
            //already exist (maybe another thread generated the files before this one 
            //acquired the lock)
            if (Files.exists(finalGeneToAnatEntitiesFile)) {
                log.info("Gene to AnatEntities association file already generated.");
                log.exit(); return;
            }

            this.writeToGeneToAnatEntitiesFile(tmpFileName);
            //move tmp file if successful
            //We check that there were no database error that could have corrupted the results
            //            if (Database.getDatabase().isError()) {
            //                throw log.throwing(new IllegalStateException("A database error occurred, " +
            //                        "analysis canceled"));
            //            }
            Files.move(tmpFile, finalGeneToAnatEntitiesFile, StandardCopyOption.REPLACE_EXISTING);

        } finally {
            Files.deleteIfExists(tmpFile);
            this.releaseWriteLock(geneToAnatEntitiesAssociationFilePath);
            this.releaseWriteLock(tmpFileName);
        }

        log.info("GeneToAnatEntitiesAssociationFile: {}", this.params.getGeneToAnatEntitiesFileName());
        log.exit();
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
    private void acquireReadLock(String fileName) {
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
    private void acquireWriteLock(String fileName) {
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
    private void releaseWriteLock(String fileName) {
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
    private void acquireLock(String fileName, boolean readLock) {
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
    private void releaseReadLock(String fileName) {
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
    private void releaseLock(String fileName, boolean readLock) {
        ReentrantReadWriteLock lock = this.getReadWriteLock(fileName);
        if (readLock) {
            lock.readLock().unlock();
        } else {
            lock.writeLock().unlock();
        }
        this.removeLockIfPossible(fileName);
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
    private void removeLockIfPossible(String fileName) {
        // check if there is already a lock stored for this key
        ReentrantReadWriteLock lock = readWriteLocks.get(fileName);

        // there is a lock to remove
        if (lock != null) {
            // there is no thread with write lock, or read lock, or waiting to
            // acquire a lock
            if (!lock.isWriteLocked() && lock.getReadLockCount() == 0
                    && !lock.hasQueuedThreads()) {
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
    private ReentrantReadWriteLock getReadWriteLock(String fileName) {
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

}
