package org.bgee.pipeline.topanat;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.ServiceFactory;
import org.bgee.model.topanat.TopAnatCallFileService;
import org.bgee.pipeline.CommandRunner;

/**
 * Generates the files topAnat reads its expression calls from, two per species: the calls are
 * propagated once by {@code TopAnatCallFileService} and written to files, the analyses then
 * reading them rather than propagating the calls of a whole species inside a web request.
 * <p>
 * The web application generates the missing files at start-up as well. This entry point exists
 * to produce them from the pipeline, once, when a release is being prepared.
 *
 * @author  Julien Wollbrett
 * @version Bgee 16
 * @see     TopAnatCallFileService
 */
public class GenerateTopAnatFiles {
    private final static Logger log = LogManager.getLogger(GenerateTopAnatFiles.class.getName());

    /**
     * Main method generating the topAnat call files. The parameters that must be provided
     * in order in {@code args} are:
     * <ol>
     * <li>The path to the directory the files are written into. The files themselves are written
     * into the sub-directory {@code TopAnatCallFileService#CALL_FILE_DIRECTORY} of that directory.
     * <li>A list of IDs of the species to generate the files of, separated by
     * {@code Utils.VALUE_SEPARATOR}, or {@code CommandRunner#EMPTY_LIST} to generate the files
     * of every species of the database.
     * <li>A boolean (see {@code CommandRunner#parseArgumentAsBoolean(String)}) telling whether
     * the species whose files already exist must be generated again, rather than skipped.
     * <li>The number of gene batches to propagate at the same time, an empty argument meaning
     * every core of the machine. Both the memory the propagation needs and the number of database
     * connections it opens grow with it.
     * </ol>
     * Example of use:
     * <pre>{@code GenerateTopAnatFiles /var/bgee/topanat - false 8}</pre>
     *
     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
     * @throws IllegalArgumentException If {@code args} does not contain the expected parameters.
     */
    public static void main(String[] args) {
        log.traceEntry("{}", (Object[]) args);

        int expectedArgLength = 4;
        if (args == null || args.length != expectedArgLength) {
            throw log.throwing(new IllegalArgumentException("Incorrect number of arguments "
                    + "provided, expected " + expectedArgLength + " arguments, "
                    + (args == null? 0: args.length) + " provided."));
        }
        String directory = args[0];
        List<Integer> speciesIds = CommandRunner.parseListArgumentAsInt(args[1]);
        boolean overwriteExistingFiles = CommandRunner.parseArgumentAsBoolean(args[2]);
        Integer requestedParallelism = CommandRunner.parseArgumentAsInteger(args[3]);
        int parallelism = requestedParallelism == null?
                Runtime.getRuntime().availableProcessors(): requestedParallelism;
        log.info("Generating the topAnat call files with a parallelism of {}", parallelism);

        try (ServiceFactory serviceFactory = new ServiceFactory()) {
            //One ServiceFactory per thread: the genes of a species are propagated by batches
            //in parallel, and a ServiceFactory is not thread-safe
            new TopAnatCallFileService(serviceFactory, ServiceFactory::new)
            .generateFiles(speciesIds, directory, overwriteExistingFiles, parallelism);
        }

        log.traceExit();
    }
}
