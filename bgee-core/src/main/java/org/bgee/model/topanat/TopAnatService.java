package org.bgee.model.topanat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.lang3.StringUtils;
import org.bgee.model.BgeeEnum.BgeeEnumField;
import org.bgee.model.BgeeProperties;
import org.bgee.model.CommonService;
import org.bgee.model.ComposedEntity;
import org.bgee.model.ManageReadWriteLocks;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.call.DAOConditionFilter2;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneBioTypeTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.expressiondata.call.CallFilter.ExpressionCallFilter2;
import org.bgee.model.expressiondata.call.CallServiceUtils;
import org.bgee.model.expressiondata.call.Condition2;
import org.bgee.model.expressiondata.call.ExpressionCallLoader;
import org.bgee.model.expressiondata.call.ExpressionCallProcessedFilter;
import org.bgee.model.expressiondata.call.ExpressionCallProcessedFilter.ExpressionCallProcessedFilterConditionPart;
import org.bgee.model.expressiondata.call.ExpressionCallProcessedFilter.ExpressionCallProcessedFilterInvariablePart;
import org.bgee.model.expressiondata.call.ExpressionCallService;
import org.bgee.model.expressiondata.call.OTFExpressionCall;
import org.bgee.model.expressiondata.call.OTFExpressionCallFilterEngine;
import org.bgee.model.function.PentaFunction;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.job.Job;
import org.bgee.model.species.Species;

/**
 * Runs the topAnat analyses, one {@link TopAnatAnalysis} for each provided {@link TopAnatParams},
 * returning {@link TopAnatResults} (see {@link #proceedToTopAnatAnalyses(List, Job)}), and
 * provides what they read: the files holding their expression calls and their conditions, six
 * per species, generated once, and the genes of the backgrounds offered for an analysis, read
 * from the database at each request (see {@link #loadBackgroundGeneIds(int, Background)}):
 * a list of genes is a simple read, unlike the calls of a whole species.
 * <p>
 * topAnat needs, for a whole species at once, which genes are expressed in which conditions —
 * a query shape the on-the-fly propagation cannot answer inside a web request (it propagates
 * gene by gene, and a species holds tens of thousands of genes). The calls are therefore
 * propagated once and written to files, which the analyses then read.
 * <p>
 * Both call files hold the same columns, and differ by the relations the observations are
 * propagated along. With a stage {@code A} descendant of {@code B} descendant of {@code C}, and
 * an anatomical entity {@code A'} descendant of {@code B'}:
 * <ul>
 * <li>the propagated file propagates the observations along every condition parameter, so an
 * observation made in {@code A' A} contributes to the call of {@code B' A} as well;
 * <li>the observed file only propagates them along the developmental stages: an observation made
 * in {@code A' A} produces the rows {@code A' A}, {@code A' B} and {@code A' C}, and contributes
 * to no row of {@code B'}. The call of {@code B' A} is computed from the observations made in
 * {@code B'} alone, and those observations are propagated to {@code B' B} and {@code B' C}
 * whether or not those hold observations of their own. Same behaviour for the cell types.
 * </ul>
 * The observed file is therefore not a subset of the propagated one: leaving the observations of
 * the descendant anatomical entities out changes the p-values, and can turn a call the propagated
 * file does not report into a present one — the p-values are aggregated as a weighted mean, which
 * the p-values of a descendant can pull either way.
 * <p>
 * Only the conditions using a meta stage are reported, in both call files.
 * <p>
 * A gene is reported in a condition when at least one combination of data types supports
 * a present call of at least SILVER quality there, with the quality of every combination: topAnat
 * lets the user select the data types to consider, and the quality of a combination cannot be
 * derived from the qualities of the data types taken separately — the p-values are aggregated
 * over the data types requested, so each combination is propagated on its own. A call file is
 * a gzip file with one member per stage, read one stage at a time, and an index next to it
 * (see {@link TopAnatCallFile}).
 * <p>
 * Two more files per species describe the conditions topAnat tests, generated by the same pass:
 * their IDs, names, anatomical entities and cell types, and the relations between them.
 * A condition of topAnat is an anatomical entity, possibly restricted to a cell type, every other
 * condition parameter being at its root: the stage is a filter applied to the calls rather than
 * a part of the graph. The call files identify a condition by the same ID, built once, at
 * generation (see {@link TopAnatUtils#conditionId(String, String)}), so that the files are joined
 * on it. They only hold public IDs, the internal IDs of the conditions being only used to
 * translate the relations.
 *
 * @author  Julien Wollbrett
 * @author  Mathieu Seppey
 * @author  Frederic Bastian
 * @version Bgee 16
 */
public class TopAnatService extends CommonService {
    private final static Logger log = LogManager.getLogger(TopAnatService.class.getName());

    /**
     * The directory, under {@code BgeeProperties#getTopAnatResultsWritingDirectory()}, holding
     * the generated files, and the patterns of their names.
     */
    public final static String CALL_FILE_DIRECTORY = "topanat_calls";

    /**
     * The kinds of file this service generates, one of each per species. Both hold the same
     * columns and differ by the relations the observations are propagated along, as described
     * in the documentation of this class.
     */
    public static enum CallFileType implements BgeeEnumField {
        /**
         * The file whose observations are propagated along every condition parameter.
         */
        PROPAGATED("propagated"),
        /**
         * The file whose observations are only propagated along the developmental stages, so that
         * the anatomical entities and cell types it reports are the observed ones.
         */
        OBSERVED("observed");

        private final String representation;

        private CallFileType(String representation) {
            this.representation = representation;
        }
        @Override
        public String getStringRepresentation() {
            return this.representation;
        }
        /**
         * @param speciesId An {@code int} that is the ID of a species.
         * @return          A {@code String} that is the name of the call file of this kind
         *                  holding the calls of that species, next to its index file (see
         *                  {@link TopAnatService#getCallIndexFilePath(String, CallFileType, int)}).
         */
        public String getFileName(int speciesId) {
            return "topanat_calls_" + this.representation + "_species_" + speciesId
                    + TopAnatCallFile.CALL_FILE_EXTENSION;
        }
    }

    /**
     * The backgrounds offered for a topAnat analysis. Whatever the background, an analysis only
     * keeps the genes of the background holding a call in its setting: the genes of
     * the background with no call take no part in it.
     */
    public static enum Background implements BgeeEnumField {
        /**
         * The genes of the biotypes targeted by the polyA libraries. Every RNA-Seq library targets
         * them, polyA as ribo-minus, so that whether such a gene is called expressed in a condition
         * does not depend on the kind of library sequenced in that condition.
         */
        POLY_A("polyA"),
        /**
         * The protein coding genes.
         */
        PROTEIN_CODING("protein_coding"),
        /**
         * The genes with at least one observation, whatever their biotype: almost every gene of
         * the species. An analysis only keeping the genes holding a call, it amounts to every
         * gene with a call.
         */
        OBSERVED("observed");

        private final String representation;

        private Background(String representation) {
            this.representation = representation;
        }
        @Override
        public String getStringRepresentation() {
            return this.representation;
        }
    }

    /**
     * The columns of the file holding the conditions of a species, see
     * {@link #getConditionFilePath(String, int)}: the ID of the condition, by the index of which
     * the call files refer to it, its name, the ID of its anatomical entity, and the ID of its
     * cell type, the root of the cell types when it targets no specific one. They bear the names
     * of the attributes of the R package API displaying them, which are the names BgeeDB reads,
     * so that the R code of the topAnat analyses reads the file as it is.
     */
    public final static String CONDITION_ID_COLUMN = "ID";
    public final static String CONDITION_NAME_COLUMN = "NAME";
    public final static String ANAT_ENTITY_ID_COLUMN = "ANAT_ENTITY_ID";
    public final static String CELL_TYPE_ID_COLUMN = "CELL_TYPE_ID";
    /**
     * The columns of the file holding the relations between the conditions of a species, see
     * {@link #getConditionRelationFilePath(String, int)}: the ID of a condition, then the ID
     * of one of its direct parents. Named as the columns of the condition file.
     */
    public final static String SOURCE_CONDITION_ID_COLUMN = "SOURCE_ID";
    public final static String TARGET_CONDITION_ID_COLUMN = "TARGET_ID";
    /**
     * Separates the columns of the generated files.
     */
    public final static String COLUMN_SEPARATOR = "\t";
    /**
     * The extension of the files being written. The final files are only created, by an atomic
     * move, once they are complete: a file that exists is a file that can be read.
     */
    final static String TMP_EXTENSION = ".tmp";

    /**
     * Separates the data types in the name of the column of a combination. Not an underscore,
     * which the names of the data types themselves contain.
     */
    public final static String COMBINATION_NAME_SEPARATOR = "-";
    /**
     * The prefix of the IDs of the meta stages, the developmental stages shared among species.
     * Only the conditions using one of them are reported, so that the files of different species
     * remain comparable (same convention as {@code BgeeToEasyBgee}).
     */
    private final static String META_STAGE_ID_PREFIX = "UBERON:";
    /**
     * The number of genes propagated per batch. A batch shares one {@code ExpressionCallLoader},
     * and therefore one retrieval of everything that does not depend on the genes.
     */
    private final static int GENE_BATCH_SIZE = 100;
    /**
     * The condition parameters the calls are propagated over: anatomical entity and cell type,
     * and developmental stage. Sex and strain are aggregated.
     */
    private final static Set<ConditionParameter<?, ?>> COND_PARAMS = Collections.unmodifiableSet(
            new LinkedHashSet<>(List.of(ConditionParameter.ANAT_ENTITY_CELL_TYPE,
                    ConditionParameter.DEV_STAGE)));
    /**
     * The only condition parameter the propagation of the observed file moves along: the
     * observations are propagated to the ancestor stages of the anatomical entity and cell type
     * they were made in, never to the ancestors of those.
     */
    private final static Set<ConditionParameter<?, ?>> STAGE_ONLY_PROPAGATION =
            Set.of(ConditionParameter.DEV_STAGE);
    /**
     * Only present calls of at least SILVER quality are reported.
     */
    private final static Map<ExpressionSummary, SummaryQuality> CALL_TYPE_FILTER =
            Map.of(ExpressionSummary.EXPRESSED, SummaryQuality.SILVER);
    /**
     * The ID of the RNA-Seq population capture of the polyA libraries.
     */
    private final static String POLY_A_POPULATION_CAPTURE_ID = "polyA";
    /**
     * The name of the biotype of the protein coding genes.
     */
    private final static String PROTEIN_CODING_BIOTYPE_NAME = "protein_coding";

    /**
     * Provides one {@code ServiceFactory} per thread: the genes of a species are propagated
     * by batches in parallel, and a {@code ServiceFactory} is not thread-safe. {@code null}
     * when this service does not generate the files.
     */
    private final Supplier<ServiceFactory> serviceFactorySupplier;
    /**
     * A {@code PentaFunction} allowing to obtain new {@code TopAnatAnalysis} instances.
     */
    private final PentaFunction<TopAnatParams, BgeeProperties, ServiceFactory, TopAnatRManager,
            TopAnatService, TopAnatAnalysis> topAnatAnalysisSupplier;
    /**
     * A {@code ManageReadWriteLocks} instance to manage the read/write locks of the files
     * of the analyses.
     */
    private final ManageReadWriteLocks readWriteLocks;

    /**
     * Constructor for the uses that do not generate the files, such as running analyses or
     * providing the genes of a background: {@link #generateFiles(Collection, String, boolean, int)}
     * then throws an {@code IllegalStateException}.
     *
     * @param serviceFactory    The {@code ServiceFactory} to obtain {@code Service}s
     *                          and {@code DAOManager} from, and the {@code BgeeProperties}
     *                          of the analyses.
     */
    public TopAnatService(ServiceFactory serviceFactory) {
        this(serviceFactory, null, TopAnatAnalysis::new);
    }
    /**
     * Constructor for the generation of the files.
     *
     * @param serviceFactory            The {@code ServiceFactory} to obtain {@code Service}s
     *                                  and {@code DAOManager} from.
     * @param serviceFactorySupplier    A {@code Supplier} of {@code ServiceFactory}s, one for
     *                                  each thread propagating genes.
     */
    public TopAnatService(ServiceFactory serviceFactory,
            Supplier<ServiceFactory> serviceFactorySupplier) {
        this(serviceFactory, serviceFactorySupplier, TopAnatAnalysis::new);
        if (serviceFactorySupplier == null) {
            throw log.throwing(new IllegalArgumentException(
                    "A ServiceFactory supplier must be provided"));
        }
    }
    /**
     * @param serviceFactory            The {@code ServiceFactory} to obtain {@code Service}s
     *                                  and {@code DAOManager} from, and the
     *                                  {@code BgeeProperties} of the analyses.
     * @param serviceFactorySupplier    A {@code Supplier} of {@code ServiceFactory}s, one for
     *                                  each thread propagating genes, {@code null} if this
     *                                  service does not generate the files.
     * @param topAnatAnalysisSupplier   A {@code PentaFunction} allowing to obtain new
     *                                  {@code TopAnatAnalysis} instances.
     */
    public TopAnatService(ServiceFactory serviceFactory,
            Supplier<ServiceFactory> serviceFactorySupplier,
            PentaFunction<TopAnatParams, BgeeProperties, ServiceFactory, TopAnatRManager,
            TopAnatService, TopAnatAnalysis> topAnatAnalysisSupplier) {
        super(serviceFactory);
        if (topAnatAnalysisSupplier == null) {
            throw log.throwing(new IllegalArgumentException("A supplier of TopAnatAnalysis "
                    + "must be provided"));
        }
        this.serviceFactorySupplier = serviceFactorySupplier;
        this.topAnatAnalysisSupplier = topAnatAnalysisSupplier;
        this.readWriteLocks = new ManageReadWriteLocks();
    }

    /**
     * @param rootDirectory The path to the directory holding {@link #CALL_FILE_DIRECTORY}.
     * @param fileType      The {@code CallFileType} of the file to locate.
     * @param speciesId     An {@code int} that is the ID of a species.
     * @return              The {@code Path} of the file holding the calls of that species,
     *                      whether or not it has been generated yet.
     */
    public static Path getCallFilePath(String rootDirectory, CallFileType fileType,
            int speciesId) {
        log.traceEntry("{}, {}, {}", rootDirectory, fileType, speciesId);
        if (StringUtils.isBlank(rootDirectory) || fileType == null) {
            throw log.throwing(new IllegalArgumentException(
                    "A root directory and a file type must be provided"));
        }
        return log.traceExit(Paths.get(rootDirectory, CALL_FILE_DIRECTORY,
                fileType.getFileName(speciesId)));
    }
    /**
     * @param rootDirectory The path to the directory holding {@link #CALL_FILE_DIRECTORY}.
     * @param fileType      The {@code CallFileType} of the call file whose index to locate.
     * @param speciesId     An {@code int} that is the ID of a species.
     * @return              The {@code Path} of the index of the call file holding the calls of
     *                      that species, whether or not it has been generated yet.
     */
    public static Path getCallIndexFilePath(String rootDirectory, CallFileType fileType,
            int speciesId) {
        log.traceEntry("{}, {}, {}", rootDirectory, fileType, speciesId);
        return log.traceExit(TopAnatCallFile.indexFile(
                getCallFilePath(rootDirectory, fileType, speciesId)));
    }
    /**
     * @param rootDirectory The path to the directory holding {@link #CALL_FILE_DIRECTORY}.
     * @param speciesId     An {@code int} that is the ID of a species.
     * @return              The {@code Path} of the file holding the IDs and names of
     *                      the conditions of that species, whether or not it has been
     *                      generated yet.
     */
    public static Path getConditionFilePath(String rootDirectory, int speciesId) {
        log.traceEntry("{}, {}", rootDirectory, speciesId);
        return log.traceExit(getFilePath(rootDirectory,
                "topanat_conditions_species_" + speciesId + ".tsv"));
    }
    /**
     * @param rootDirectory The path to the directory holding {@link #CALL_FILE_DIRECTORY}.
     * @param speciesId     An {@code int} that is the ID of a species.
     * @return              The {@code Path} of the file holding the relations between
     *                      the conditions of that species, whether or not it has been
     *                      generated yet.
     */
    public static Path getConditionRelationFilePath(String rootDirectory, int speciesId) {
        log.traceEntry("{}, {}", rootDirectory, speciesId);
        return log.traceExit(getFilePath(rootDirectory,
                "topanat_condition_relations_species_" + speciesId + ".tsv"));
    }
    private static Path getFilePath(String rootDirectory, String fileName) {
        if (StringUtils.isBlank(rootDirectory)) {
            throw log.throwing(new IllegalArgumentException("A root directory must be provided"));
        }
        return Paths.get(rootDirectory, CALL_FILE_DIRECTORY, fileName);
    }

    /**
     * Subset a generated call file: retain the rows supporting a present call of at least
     * the requested quality for the requested combination of data types, at the requested
     * stage. This is the reading of the call files shared by the R package API and the topAnat
     * analyses.
     * <p>
     * A single stage is read: a stage aggregates the calls of its substages, and a gene and
     * condition pair appears once per stage in a file, so that the retained rows need no
     * deduplication, which would hold every returned pair in memory. Only the member of that
     * stage is decompressed (see {@link TopAnatCallFile}).
     *
     * @param callFile      The {@code Path} of the call file to read, of either type.
     * @param dataTypes     An {@code EnumSet} of {@code DataType}s that is the combination of data
     *                      types requested: the quality is read in the column of that very
     *                      combination, the quality of a combination not being derivable from
     *                      the qualities of its data types taken separately.
     * @param quality       The {@code SummaryQuality} requested. {@code GOLD} retains the GOLD
     *                      rows only, any other quality the SILVER and GOLD ones, the files
     *                      holding no BRONZE call.
     * @param stageId       A {@code String} that is the ID of the developmental stage to retain.
     *                      When {@code null} or blank, the root of the stages, whose calls
     *                      aggregate every stage.
     * @return              A {@code Stream} of {@code String[]}s holding the gene ID and
     *                      the condition ID of each retained row. The file is read lazily,
     *                      so the {@code Stream} must be closed.
     * @throws IOException              If the file cannot be read.
     * @throws IllegalArgumentException If {@code callFile}, {@code dataTypes} or {@code quality}
     *                                  is not provided.
     * @throws IllegalStateException    If the file lacks the requested combination of data types,
     *                                  or does not match its index.
     */
    public static Stream<String[]> subsetCalls(Path callFile, EnumSet<DataType> dataTypes,
            SummaryQuality quality, String stageId) throws IOException {
        log.traceEntry("{}, {}, {}, {}", callFile, dataTypes, quality, stageId);
        if (callFile == null || dataTypes == null || dataTypes.isEmpty() || quality == null) {
            throw log.throwing(new IllegalArgumentException(
                    "A call file, data types and a quality must be provided"));
        }

        //A request for SILVER means "at least SILVER", as everywhere else in Bgee
        boolean goldOnly = SummaryQuality.GOLD.equals(quality);
        //No stage requested means every stage aggregated: the calls of the root of the stages
        String requestedStage = StringUtils.isBlank(stageId)?
                ConditionDAO.DEV_STAGE_ROOT_ID: stageId;
        //The combinations are located by name, so that the generated files can gain some
        //without breaking this reading. Closing the returned stream closes the file.
        return log.traceExit(TopAnatCallFile.readPairs(callFile,
                combinationColumnName(dataTypes), goldOnly, requestedStage));
    }
    /**
     * Read some columns of a generated file, for instance the conditions, or the relations
     * between them.
     *
     * @param file      The {@code Path} of the file to read.
     * @param columns   A {@code List} of {@code String}s that are the names of the columns
     *                  to read.
     * @return          A {@code Stream} of {@code String[]}s holding, for each row of the file,
     *                  the values of {@code columns}, in that order. The file is read lazily,
     *                  so the {@code Stream} must be closed.
     * @throws IOException              If the file cannot be read.
     * @throws IllegalArgumentException If {@code file} or {@code columns} is not provided.
     * @throws IllegalStateException    If the file lacks one of {@code columns}.
     */
    public static Stream<String[]> readColumns(Path file, List<String> columns)
            throws IOException {
        log.traceEntry("{}, {}", file, columns);
        if (file == null || columns == null || columns.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "A file and the columns to read must be provided"));
        }
        //Located by name, as the columns of the call files
        Map<String, Integer> colIndexes = columnIndexes(file);
        final int[] indexes = columns.stream()
                .mapToInt(column -> columnIndex(colIndexes, column, file))
                .toArray();
        //Closing the returned stream closes the file
        return log.traceExit(Files.lines(file, StandardCharsets.UTF_8)
                .skip(1)
                .map(l -> l.split(COLUMN_SEPARATOR, -1))
                .map(v -> Arrays.stream(indexes).mapToObj(i -> v[i]).toArray(String[]::new)));
    }
    /**
     * @return  A {@code Map} where keys are the names of the columns of {@code file}, read from
     *          its header, the associated value being their index.
     */
    private static Map<String, Integer> columnIndexes(Path file) throws IOException {
        String headerLine;
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            headerLine = reader.readLine();
        }
        if (headerLine == null) {
            throw log.throwing(new IllegalStateException("Empty file: " + file));
        }
        String[] header = headerLine.split(COLUMN_SEPARATOR, -1);
        Map<String, Integer> colIndexes = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            colIndexes.put(header[i], i);
        }
        return colIndexes;
    }
    private static int columnIndex(Map<String, Integer> colIndexes, String column, Path file) {
        Integer index = colIndexes.get(column);
        if (index == null) {
            throw log.throwing(new IllegalStateException("No column " + column
                    + " in the file " + file));
        }
        return index;
    }

    /**
     * @param speciesId     An {@code int} that is the ID of the species to retrieve
     *                      the background of.
     * @param background    The {@code Background} to retrieve the genes of.
     * @return              A {@code List} of {@code String}s that are the IDs of the genes
     *                      of {@code background} in the species, sorted.
     * @throws IllegalArgumentException If {@code speciesId} is not a valid species ID,
     *                                  or {@code background} is {@code null}.
     * @throws IllegalStateException    If no biotype defines {@code background}.
     */
    public List<String> loadBackgroundGeneIds(int speciesId, Background background)
            throws IllegalArgumentException, IllegalStateException {
        log.traceEntry("{}, {}", speciesId, background);
        if (speciesId <= 0) {
            throw log.throwing(new IllegalArgumentException("Invalid species ID: " + speciesId));
        }
        if (background == null) {
            throw log.throwing(new IllegalArgumentException("A background must be provided"));
        }
        GeneDAO geneDAO = this.getDaoManager().getGeneDAO();
        Stream<GeneTO> genes;
        switch (background) {
        case OBSERVED:
            genes = geneDAO.getGenesWithDataBySpeciesIdsOrdered(Set.of(speciesId), 0, 0).stream();
            break;
        case POLY_A:
        case PROTEIN_CODING:
            Stream<GeneBioTypeTO> bioTypes = background == Background.POLY_A?
                    geneDAO.getGeneBioTypesByPopulationCaptureIds(
                            Set.of(POLY_A_POPULATION_CAPTURE_ID)).stream():
                    geneDAO.getGeneBioTypes().stream()
                    .filter(to -> PROTEIN_CODING_BIOTYPE_NAME.equals(to.getName()));
            Set<Integer> bioTypeIds = bioTypes.map(GeneBioTypeTO::getId)
                    .collect(Collectors.toSet());
            //An empty set would silently return no gene
            if (bioTypeIds.isEmpty()) {
                throw log.throwing(new IllegalStateException("No biotype defines the background "
                        + background.getStringRepresentation()));
            }
            genes = geneDAO.getGenesBySpeciesIds(Set.of(speciesId)).stream()
                    .filter(to -> bioTypeIds.contains(to.getGeneBioTypeId()));
            break;
        default:
            throw log.throwing(new IllegalArgumentException("Unsupported background: "
                    + background));
        }
        return log.traceExit(genes.map(GeneTO::getGeneId).distinct().sorted()
                .collect(Collectors.toList()));
    }

    /**
     * Proceed to the analyses and return their results.
     *
     * @param topAnatParams A {@code List} of {@code TopAnatParams} that will produce one
     *                      {@code TopAnatAnalysis} each.
     * @param job           A {@code Job} allowing to track advancement of the analyses,
     *                      {@code null} if they are not run by a job.
     * @return              A {@code Stream} of {@code TopAnatResults}, one per analysis,
     *                      the analyses being run as the {@code Stream} is consumed.
     * @throws IllegalArgumentException If {@code topAnatParams} is {@code null} or empty,
     *                                  or contains {@code null}.
     */
    public Stream<TopAnatResults> proceedToTopAnatAnalyses(List<TopAnatParams> topAnatParams,
            Job job) throws IllegalArgumentException {
        log.traceEntry("{}, {}", topAnatParams, job);
        List<TopAnatParams> clonedParams = checkTopAnatParams(topAnatParams);
        Optional<Job> trackedJob = Optional.ofNullable(job);

        // Create TopAnatAnalysis for each TopAnatParams
        //TODO: TopAnatAnalysis should be provided with the Job instance to be able to use 
        //'checkInterrupted'

        return log.traceExit(clonedParams.stream()
                .map(params -> this.getTopAnatAnalysis(params))
                .map(analysis -> {
                    try {
                        //if task in job not yet started (first analysis), start it.
                        if (trackedJob.map(t -> !t.isStarted()).orElse(false)) {
                            trackedJob.ifPresent(t -> t.startJob());
                        } else {
                            //otherwise, check if job was interrupted and move to next subtask
                            trackedJob.ifPresent(t -> {
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
                        if (trackedJob.map(t -> t.getCurrentTaskIndex()).orElse(-1) == 
                                (clonedParams.size() - 1)) {
                            trackedJob.ifPresent(t -> t.completeWithSuccess());
                        }

                        return results;
                    } catch (Throwable e) {
                        //catch and throw this error in DEBUG level because we don't want 
                        //to log those as errors when we requested a Thread interruption
                        log.catching(Level.DEBUG, e);
                        trackedJob.ifPresent(t -> t.complete());
                        throw log.throwing(Level.DEBUG, new RuntimeException(e));
                    }
                }));
    }

    /**
     * @param topAnatParams A {@code List} of {@code TopAnatParams} that will produce one
     *                      {@code TopAnatAnalysis} each.
     * @return              A {@code boolean} that tells whether all analyses are done.
     * @throws IllegalArgumentException If {@code topAnatParams} is {@code null} or empty,
     *                                  or contains {@code null}.
     */
    public boolean areAnalysesDone(List<TopAnatParams> topAnatParams)
            throws IllegalArgumentException {
        log.traceEntry("{}", topAnatParams);
        return log.traceExit(checkTopAnatParams(topAnatParams).stream()
                .map(params -> this.getTopAnatAnalysis(params))
                .allMatch(a -> a.isAnalysisDone()));
    }

    /**
     * @return  A {@code BgeeProperties} instance to provide all properties values
     *          of the analyses, the one of the {@code ServiceFactory} of this service.
     */
    public BgeeProperties getBgeeProperties() {
        return this.getServiceFactory().getBgeeProperties();
    }
    /**
     * @return A {@code ManageReadWriteLocks} instance to provide all read/write lock info
     */
    public ManageReadWriteLocks getReadWriteLocks() {
        return this.readWriteLocks;
    }

    private TopAnatAnalysis getTopAnatAnalysis(TopAnatParams params) {
        BgeeProperties props = this.getBgeeProperties();
        return this.topAnatAnalysisSupplier.apply(params, props, this.getServiceFactory(),
                new TopAnatRManager(props, params), this);
    }
    private static List<TopAnatParams> checkTopAnatParams(List<TopAnatParams> topAnatParams) {
        if (topAnatParams == null || topAnatParams.isEmpty() || 
                topAnatParams.stream().anyMatch(Objects::isNull)) {
            throw log.throwing(new IllegalArgumentException("At least one TopAnatParams "
                    + "must be provided, and none should be null"));
        }
        return Collections.unmodifiableList(new ArrayList<>(topAnatParams));
    }

    /**
     * Generate the files of each species that does not have them yet. A species whose six files
     * already exist is skipped, so that this can be called at every server start.
     *
     * @param speciesIds    The IDs of the species to generate the files of, all the species
     *                      of the database when {@code null} or empty.
     * @param rootDirectory The path to the directory holding {@link #CALL_FILE_DIRECTORY},
     *                      where the files are written.
     * @param parallelism   The number of genes batches to propagate at the same time, 1 to
     *                      propagate them one after the other.
     */
    public void generateFilesIfMissing(Collection<Integer> speciesIds, String rootDirectory,
            int parallelism) {
        log.traceEntry("{}, {}, {}", speciesIds, rootDirectory, parallelism);
        this.generateFiles(speciesIds, rootDirectory, false, parallelism);
        log.traceExit();
    }

    /**
     * Generate the files of each requested species. The six files of a species are produced by
     * the same pass, so a species missing any of them is processed again: the two call files,
     * their indexes, and the two files describing the conditions.
     *
     * @param speciesIds                The IDs of the species to generate the files of, all the
     *                                  species of the database when {@code null} or empty.
     * @param rootDirectory             The path to the directory holding
     *                                  {@link #CALL_FILE_DIRECTORY}, where the files are written.
     * @param overwriteExistingFiles    Whether the species whose files already exist must be
     *                                  processed again, rather than skipped.
     * @param parallelism               The number of gene batches to propagate at the same time,
     *                                  1 to propagate them one after the other. Both the memory
     *                                  the propagation needs and the number of database
     *                                  connections it opens grow with it: a batch holds buffers
     *                                  sized to the number of conditions of the species, and
     *                                  a {@code ServiceFactory} of its own.
     */
    public void generateFiles(Collection<Integer> speciesIds, String rootDirectory,
            boolean overwriteExistingFiles, int parallelism) {
        log.traceEntry("{}, {}, {}, {}", speciesIds, rootDirectory, overwriteExistingFiles,
                parallelism);
        if (StringUtils.isBlank(rootDirectory)) {
            throw log.throwing(new IllegalArgumentException(
                    "A directory to write the files into must be provided"));
        }
        if (parallelism < 1) {
            throw log.throwing(new IllegalArgumentException(
                    "At least one gene batch must be propagated at a time, requested: "
                    + parallelism));
        }
        if (this.serviceFactorySupplier == null) {
            throw log.throwing(new IllegalStateException(
                    "A ServiceFactory supplier is needed to generate the files"));
        }
        //No species requested means every species of the database: the files of a species
        //are independent of each other, this service simply generates them all.
        Collection<Integer> requestedSpeciesIds = speciesIds != null && !speciesIds.isEmpty()?
                speciesIds:
                this.getServiceFactory().getSpeciesService().loadSpeciesByIds(null, false)
                .stream().map(s -> s.getId()).collect(Collectors.toList());

        Path directory = Paths.get(rootDirectory, CALL_FILE_DIRECTORY);
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw log.throwing(new UncheckedIOException(
                    "Could not create the directory " + directory, e));
        }

        for (Integer speciesId: requestedSpeciesIds) {
            Path propagatedFile = getCallFilePath(rootDirectory, CallFileType.PROPAGATED,
                    speciesId);
            Path observedFile = getCallFilePath(rootDirectory, CallFileType.OBSERVED, speciesId);
            Path conditionFile = getConditionFilePath(rootDirectory, speciesId);
            Path relationFile = getConditionRelationFilePath(rootDirectory, speciesId);
            if (!overwriteExistingFiles &&
                    Files.exists(propagatedFile) && Files.exists(observedFile) &&
                    Files.exists(TopAnatCallFile.indexFile(propagatedFile)) &&
                    Files.exists(TopAnatCallFile.indexFile(observedFile)) &&
                    Files.exists(conditionFile) && Files.exists(relationFile)) {
                log.info("topAnat call files already generated for species {}", speciesId);
                continue;
            }
            long startTime = System.currentTimeMillis();
            log.info("Generating the topAnat call files of species {}...", speciesId);
            this.generateFiles(speciesId, propagatedFile, observedFile, conditionFile,
                    relationFile, parallelism);
            log.info("Done generating the topAnat call files of species {} in {} ms",
                    speciesId, System.currentTimeMillis() - startTime);
        }
        log.traceExit();
    }

    /**
     * Generate the files of one species. The rows are written to temporary files that are moved
     * to their final path once complete, so that an interrupted generation never leaves a partial
     * file behind that the next start would consider done.
     */
    private void generateFiles(int speciesId, Path propagatedFile, Path observedFile,
            Path conditionFile, Path relationFile, int parallelism) {
        log.traceEntry("{}, {}, {}, {}, {}, {}", speciesId, propagatedFile, observedFile,
                conditionFile, relationFile, parallelism);

        List<String> geneIds = this.getServiceFactory().getGeneService()
                .loadGenes(new GeneFilter(speciesId))
                .map(Gene::getGeneId)
                .collect(Collectors.toList());
        if (geneIds.isEmpty()) {
            log.warn("No gene for species {}, no topAnat call file generated", speciesId);
            log.traceExit(); return;
        }
        //The conditions are written first: they are quick to produce, and the rows of the call
        //files are checked against them, topAnat being unable to place a condition missing
        //from its graph. The call files refer to them by their index in the order of their IDs,
        //the order of the condition file.
        List<String> conditionIds = new ArrayList<>(new TreeSet<>(this.generateConditionFiles(
                speciesId, conditionFile, relationFile)));
        Map<String, Integer> conditionIndexes = TopAnatCallFile.conditionIndexes(conditionIds);
        List<EnumSet<DataType>> combinations = dataTypeCombinations();
        List<String> combinationNames = combinations.stream()
                .map(TopAnatService::combinationColumnName)
                .collect(Collectors.toList());
        //Everything that does not depend on the genes is computed once for the species
        //and reused by every batch and every data type combination: the condition part holds
        //all the conditions of the species and is by far the most expensive to build.
        ExpressionCallProcessedFilter seedFilter = this.getServiceFactory()
                .getExpressionCallService().processExpressionCallFilter(
                        buildCallFilter(speciesId, geneIds.subList(0, 1), null));
        ExpressionCallProcessedFilterConditionPart condPart = seedFilter.getConditionPart();
        ExpressionCallProcessedFilterInvariablePart invariablePart = seedFilter.getInvariablePart();

        AtomicInteger geneCount = new AtomicInteger(0);
        AtomicInteger propagatedRowCount = new AtomicInteger(0);
        AtomicInteger observedRowCount = new AtomicInteger(0);
        TopAnatCallFile.CallFileWriter propagatedWriter = new TopAnatCallFile.CallFileWriter(
                propagatedFile, combinationNames, conditionIds);
        TopAnatCallFile.CallFileWriter observedWriter = new TopAnatCallFile.CallFileWriter(
                observedFile, combinationNames, conditionIds);

        //The members already written are deleted if the propagation fails
        try {
            AtomicInteger index = new AtomicInteger(0);
            Collection<List<String>> geneBatches = geneIds.stream()
            .collect(Collectors.groupingBy(id -> index.getAndIncrement() / GENE_BATCH_SIZE))
            .values();
            Consumer<List<String>> propagateBatch = geneBatch -> {
                ServiceFactory batchServiceFactory = this.serviceFactorySupplier.get();
                try {
                    ExpressionCallService callService =
                            batchServiceFactory.getExpressionCallService();
                    //The two files are two propagations of the same observations, one along
                    //every condition parameter, the other along the developmental stages only.
                    propagatedRowCount.addAndGet(this.writeGeneRows(
                            this.propagateAllCombinations(callService, speciesId, geneBatch,
                                    combinations, condPart, invariablePart, null),
                            combinations, seedFilter, conditionIndexes, propagatedWriter));
                    observedRowCount.addAndGet(this.writeGeneRows(
                            this.propagateAllCombinations(callService, speciesId, geneBatch,
                                    combinations, condPart, invariablePart,
                                    STAGE_ONLY_PROPAGATION),
                            combinations, seedFilter, conditionIndexes, observedWriter));
                } finally {
                    //A DAOManager belongs to a thread: the one of a thread of the pool would stay
                    //open with its connection once the pool is shut down, and every species
                    //would leak as many connections as threads. Closed after each batch, the next
                    //batch run by that thread reopening one. In the calling thread, it is
                    //the DAOManager of this service, which must stay open.
                    if (batchServiceFactory.getDAOManager() !=
                            this.getServiceFactory().getDAOManager()) {
                        batchServiceFactory.close();
                    }
                }

                int count = geneCount.addAndGet(geneBatch.size());
                log.debug("Species {}: {}/{} genes processed, {} propagated rows, "
                        + "{} observed rows written", speciesId, count, geneIds.size(),
                        propagatedRowCount.get(), observedRowCount.get());
            };

            if (parallelism > 1) {
                //A pool of this generation's own rather than the common ForkJoinPool: that one is
                //shared by the whole JVM, and this generation runs for far too long to be allowed
                //to starve anything else using a parallel stream.
                ForkJoinPool pool = new ForkJoinPool(parallelism);
                try {
                    pool.submit(() -> geneBatches.parallelStream().forEach(propagateBatch)).get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw log.throwing(new IllegalStateException("Interrupted while generating "
                            + "the topAnat call files of species " + speciesId, e));
                } catch (ExecutionException e) {
                    //The propagation of a batch only throws unchecked exceptions, rethrown as
                    //they were rather than wrapped into one more layer
                    Throwable cause = e.getCause();
                    if (cause instanceof RuntimeException) {
                        throw log.throwing((RuntimeException) cause);
                    }
                    throw log.throwing(new IllegalStateException("Could not generate the topAnat "
                            + "call files of species " + speciesId, cause));
                } finally {
                    pool.shutdown();
                }
            } else {
                geneBatches.forEach(propagateBatch);
            }
        } catch (RuntimeException e) {
            propagatedWriter.discard();
            observedWriter.discard();
            throw log.throwing(e);
        }
        propagatedWriter.complete();
        observedWriter.complete();
        log.info("Species {}: {} genes processed, {} propagated rows, {} observed rows written",
                speciesId, geneCount.get(), propagatedRowCount.get(), observedRowCount.get());
        log.traceExit();
    }

    static Path tmpFile(Path file) {
        return file.resolveSibling(file.getFileName() + TMP_EXTENSION);
    }
    static void move(Path tmpFile, Path file) {
        try {
            Files.move(tmpFile, file, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw log.throwing(new UncheckedIOException(
                    "Could not move " + tmpFile + " to " + file, e));
        }
    }
    /**
     * Write a whole file through a temporary file, moved to its final path once complete,
     * as the call files.
     */
    private static void writeFile(Path file, String header, List<String> lines) {
        Path tmpFile = tmpFile(file);
        List<String> content = new ArrayList<>(lines.size() + 1);
        content.add(header);
        content.addAll(lines);
        try {
            Files.write(tmpFile, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw log.throwing(new UncheckedIOException("Could not write " + tmpFile, e));
        }
        move(tmpFile, file);
    }

    /**
     * Generate the files describing the conditions of one species: their IDs and names, and
     * the relations between them. The conditions are the anatomical entities and cell types
     * the calls are propagated over, every other condition parameter being at its root:
     * a condition of topAnat aggregates every stage, sex and strain.
     *
     * @return  A {@code Set} of {@code String}s that are the IDs of the conditions written,
     *          for the rows of the call files to be checked against them.
     */
    private Set<String> generateConditionFiles(int speciesId, Path conditionFile,
            Path relationFile) {
        log.traceEntry("{}, {}, {}", speciesId, conditionFile, relationFile);
        ServiceFactory serviceFactory = this.getServiceFactory();

        Set<Species> species = serviceFactory.getSpeciesService().loadSpeciesByIds(
                Set.of(speciesId), false);
        DAOConditionFilter2 rootFilter = new DAOConditionFilter2(Set.of(speciesId), null,
                Set.of(ConditionDAO.DEV_STAGE_ROOT_ID), null, Set.of(ConditionDAO.SEX_ROOT_ID),
                Set.of(ConditionDAO.STRAIN_ROOT_ID), null, null);
        //Loaded by the method loading the conditions of the calls, so that the IDs and names
        //of both are built from the same objects
        Map<Integer, Condition2> conditions = new CallServiceUtils().loadGlobalConditionMap(
                species, Set.of(rootFilter),
                EnumSet.of(ConditionDAO.Attribute.ANAT_ENTITY_ID,
                        ConditionDAO.Attribute.CELL_TYPE_ID),
                this.getDaoManager().getConditionDAO(), serviceFactory.getAnatEntityService(),
                serviceFactory.getDevStageService(), serviceFactory.getSexService(),
                serviceFactory.getStrainService());
        //The relations the propagation walks, between internal IDs: they are only translated
        //into the IDs of the conditions, the files hold no internal ID.
        Map<Integer, int[]> directAncestors = serviceFactory.getConditionGraphCacheService()
                .getOrLoadGraph(speciesId).getGlobalCondToDirectAncestors();

        List<String> conditionLines = conditionLines(conditions.values());
        List<String> relationLines = relationLines(conditions, directAncestors);
        writeFile(conditionFile, String.join(COLUMN_SEPARATOR, CONDITION_ID_COLUMN,
                CONDITION_NAME_COLUMN, ANAT_ENTITY_ID_COLUMN, CELL_TYPE_ID_COLUMN), conditionLines);
        writeFile(relationFile, SOURCE_CONDITION_ID_COLUMN + COLUMN_SEPARATOR
                + TARGET_CONDITION_ID_COLUMN, relationLines);
        log.info("Species {}: {} conditions and {} relations written", speciesId,
                conditionLines.size(), relationLines.size());

        return log.traceExit(conditions.values().stream()
                .map(TopAnatService::conditionId)
                .collect(Collectors.toSet()));
    }
    /**
     * @param conditions    A {@code Collection} of {@code Condition2}s that are the conditions
     *                      of a species.
     * @return              A {@code List} of {@code String}s that are the lines of the condition
     *                      file, sorted by condition ID: the ID of a condition, its name, the ID
     *                      of its anat. entity and the ID of its cell type.
     * @throws IllegalStateException    If several conditions have the same ID.
     */
    static List<String> conditionLines(Collection<Condition2> conditions) {
        log.traceEntry("{}", conditions);
        SortedMap<String, String> linesById = new TreeMap<>();
        for (Condition2 condition: conditions) {
            AnatEntity[] anatEntityCellType = anatEntityAndCellType(condition);
            String conditionId = conditionId(condition);
            String line = String.join(COLUMN_SEPARATOR, conditionId,
                    TopAnatUtils.conditionName(anatEntityCellType[0], anatEntityCellType[1]),
                    anatEntityCellType[0].getId(), anatEntityCellType[1].getId());
            if (linesById.put(conditionId, line) != null) {
                throw log.throwing(new IllegalStateException(
                        "Several conditions have the ID " + conditionId));
            }
        }
        return log.traceExit(new ArrayList<>(linesById.values()));
    }
    /**
     * @param conditions        A {@code Map} where keys are the internal IDs of the conditions
     *                          of a species, the associated value being the condition.
     * @param directAncestors   A {@code Map} where keys are internal IDs of conditions,
     *                          the associated value being the internal IDs of their direct
     *                          ancestors. It can hold other conditions than {@code conditions},
     *                          those of the other combinations of condition parameters,
     *                          which are ignored.
     * @return                  A {@code List} of {@code String}s that are the lines of the
     *                          relation file, sorted: the ID of a condition, then the ID of one
     *                          of its direct parents.
     * @throws IllegalStateException    If a direct ancestor of a condition is not one of
     *                                  {@code conditions}, or if {@code conditions} do not have
     *                                  exactly one root: topAnat walks a closed graph with
     *                                  a single root.
     */
    static List<String> relationLines(Map<Integer, Condition2> conditions,
            Map<Integer, int[]> directAncestors) {
        log.traceEntry("{}, {}", conditions, directAncestors);
        Map<Integer, String> conditionIds = conditions.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> conditionId(e.getValue())));
        SortedSet<String> lines = new TreeSet<>();
        SortedSet<String> roots = new TreeSet<>();
        for (Entry<Integer, String> idEntry: conditionIds.entrySet()) {
            int[] parentIds = directAncestors.get(idEntry.getKey());
            if (parentIds == null || parentIds.length == 0) {
                roots.add(idEntry.getValue());
                continue;
            }
            for (int parentId: parentIds) {
                //Moving up from the root of the stages, sexes and strains only changes
                //the anat. entity or the cell type: a parent is always one of the conditions
                String parentConditionId = conditionIds.get(parentId);
                if (parentConditionId == null) {
                    throw log.throwing(new IllegalStateException("The condition "
                            + idEntry.getValue() + " has a parent that is not a condition "
                            + "of topAnat, internal ID: " + parentId));
                }
                lines.add(idEntry.getValue() + COLUMN_SEPARATOR + parentConditionId);
            }
        }
        if (roots.size() != 1) {
            throw log.throwing(new IllegalStateException(
                    "The conditions of topAnat must have exactly one root, found: " + roots));
        }
        return log.traceExit(new ArrayList<>(lines));
    }

    /**
     * Propagate the calls of a batch of genes, once per combination of data types: the p-values
     * are aggregated over the data types requested, so the call of a combination is not derivable
     * from the calls of the data types taken separately.
     *
     * @param propagationCondParams The {@code ConditionParameter}s the propagation may move along,
     *                              {@code null} to move along all of them.
     */
    private Map<EnumSet<DataType>, Map<Gene, List<OTFExpressionCall>>> propagateAllCombinations(
            ExpressionCallService callService, int speciesId, Collection<String> geneIds,
            List<EnumSet<DataType>> combinations,
            ExpressionCallProcessedFilterConditionPart condPart,
            ExpressionCallProcessedFilterInvariablePart invariablePart,
            Set<ConditionParameter<?, ?>> propagationCondParams) {
        log.traceEntry("{}, {}, {}, {}, {}, {}, {}", callService, speciesId, geneIds, combinations,
                condPart, invariablePart, propagationCondParams);
        Map<EnumSet<DataType>, Map<Gene, List<OTFExpressionCall>>> callsByCombination =
                new LinkedHashMap<>();
        for (EnumSet<DataType> combination: combinations) {
            callsByCombination.put(combination, this.propagate(callService, speciesId, geneIds,
                    combination, condPart, invariablePart, propagationCondParams));
        }
        return log.traceExit(callsByCombination);
    }

    /**
     * Propagate the calls of a batch of genes over one combination of data types, reusing the
     * parts of the processed filter that do not depend on the genes.
     */
    private Map<Gene, List<OTFExpressionCall>> propagate(ExpressionCallService callService,
            int speciesId, Collection<String> geneIds, EnumSet<DataType> dataTypes,
            ExpressionCallProcessedFilterConditionPart condPart,
            ExpressionCallProcessedFilterInvariablePart invariablePart,
            Set<ConditionParameter<?, ?>> propagationCondParams) {
        log.traceEntry("{}, {}, {}, {}, {}, {}, {}", callService, speciesId, geneIds, dataTypes,
                condPart, invariablePart, propagationCondParams);
        ExpressionCallProcessedFilter processedFilter = callService.processExpressionCallFilter(
                buildCallFilter(speciesId, geneIds, dataTypes), null, condPart, invariablePart);
        ExpressionCallLoader loader = callService.getCallLoader(processedFilter,
                propagationCondParams);
        return log.traceExit(loader.loadDataOnTheFly());
    }

    /**
     * @param dataTypes The {@code DataType}s to consider, {@code null} to consider them all.
     * @return          The {@code ExpressionCallFilter2} the calls of the files are retrieved with.
     */
    private static ExpressionCallFilter2 buildCallFilter(int speciesId, Collection<String> geneIds,
            EnumSet<DataType> dataTypes) {
        log.traceEntry("{}, {}, {}", speciesId, geneIds, dataTypes);
        return log.traceExit(new ExpressionCallFilter2(CALL_TYPE_FILTER,
                new GeneFilter(speciesId, geneIds), null, dataTypes, COND_PARAMS,
                //No filter on observed data: the calls propagated from sub-conditions are
                //precisely what topAnat tests, in the ancestors of the conditions holding
                //the observations.
                null, null,
                //No filtering of the calls redundant with a more precise one either: topAnat
                //tests every condition of the ontology, including the ancestors.
                false));
    }

    /**
     * Write the calls of the genes of one batch into one call file.
     *
     * @param callsByCombination    The calls propagated for each combination of data types.
     * @param combinations          The combinations of data types, in the order of the quality
     *                              letters written for each condition (see
     *                              {@link TopAnatCallFile}): the i-th letter is the quality of
     *                              the i-th combination.
     * @param processedFilter       The {@code ExpressionCallProcessedFilter} holding the p-value
     *                              thresholds the qualities are inferred with.
     * @param conditionIndexes      The index of each condition of the condition file, by ID.
     * @param writer                The {@code CallFileWriter} of the call file.
     * @return                      The number of calls written, one per gene, stage and
     *                              condition.
     * @throws IllegalStateException    If a call is in a condition missing from
     *                                  {@code conditionIndexes}.
     */
    private int writeGeneRows(Map<EnumSet<DataType>, Map<Gene, List<OTFExpressionCall>>>
            callsByCombination, List<EnumSet<DataType>> combinations,
            ExpressionCallProcessedFilter processedFilter, Map<String, Integer> conditionIndexes,
            TopAnatCallFile.CallFileWriter writer) {
        log.traceEntry("{}, {}, {}, {}, {}", callsByCombination, combinations, processedFilter,
                conditionIndexes, writer);

        //The rows to write are the union over the combinations: a condition is reported as soon
        //as one combination supports a present call in it, the columns of the other combinations
        //then saying that they do not.
        Map<Gene, Map<Condition2, Map<EnumSet<DataType>, SummaryQuality>>> rows = new HashMap<>();
        for (Entry<EnumSet<DataType>, Map<Gene, List<OTFExpressionCall>>> combinationEntry:
                callsByCombination.entrySet()) {
            for (Entry<Gene, List<OTFExpressionCall>> geneEntry:
                    combinationEntry.getValue().entrySet()) {
                for (OTFExpressionCall call: geneEntry.getValue()) {
                    if (!isMetaStageCall(call)) {
                        continue;
                    }
                    Entry<ExpressionSummary, SummaryQuality> callTypeQuality =
                            OTFExpressionCallFilterEngine.inferSummaryCallTypeAndQuality(call,
                                    processedFilter.getPresentHighThreshold(),
                                    processedFilter.getPresentLowThreshold(),
                                    processedFilter.getAbsentLowThreshold(),
                                    processedFilter.getAbsentHighThreshold());
                    //The loader already discarded everything but the present calls of at least
                    //SILVER quality, this is only the GOLD/SILVER distinction.
                    if (callTypeQuality == null ||
                            !ExpressionSummary.EXPRESSED.equals(callTypeQuality.getKey())) {
                        continue;
                    }
                    rows.computeIfAbsent(geneEntry.getKey(), k -> new HashMap<>())
                        .computeIfAbsent(call.getCondition(), k -> new HashMap<>())
                        .put(combinationEntry.getKey(), callTypeQuality.getValue());
                }
            }
        }

        //The letters of each condition of each gene at each stage. Sorted, so that the content
        //of a file does not depend on the order the genes and conditions happened to be
        //iterated in.
        Map<String, Map<String, Map<Integer, String>>> stageGeneConditions = new TreeMap<>();
        int callCount = 0;
        for (Entry<Gene, Map<Condition2, Map<EnumSet<DataType>, SummaryQuality>>> geneEntry:
                rows.entrySet()) {
            for (Entry<Condition2, Map<EnumSet<DataType>, SummaryQuality>> condEntry:
                    geneEntry.getValue().entrySet()) {
                String conditionId = conditionId(condEntry.getKey());
                Integer conditionIndex = conditionIndexes.get(conditionId);
                if (conditionIndex == null) {
                    throw log.throwing(new IllegalStateException("The condition " + conditionId
                            + " holds calls but is missing from the condition file"));
                }
                StringBuilder letters = new StringBuilder(combinations.size());
                for (EnumSet<DataType> combination: combinations) {
                    letters.append(TopAnatCallFile.qualityLetter(
                            condEntry.getValue().get(combination)));
                }
                String stageId = condEntry.getKey()
                        .getConditionParameterId(ConditionParameter.DEV_STAGE);
                if (stageGeneConditions.computeIfAbsent(stageId, k -> new TreeMap<>())
                        .computeIfAbsent(geneEntry.getKey().getGeneId(), k -> new TreeMap<>())
                        .put(conditionIndex, letters.toString()) != null) {
                    throw log.throwing(new IllegalStateException("Two calls of the gene "
                            + geneEntry.getKey().getGeneId() + " in the condition " + conditionId
                            + " at the stage " + stageId));
                }
                callCount++;
            }
        }
        for (Entry<String, Map<String, Map<Integer, String>>> stageEntry:
                stageGeneConditions.entrySet()) {
            for (Entry<String, Map<Integer, String>> geneEntry:
                    stageEntry.getValue().entrySet()) {
                StringBuilder entries = new StringBuilder();
                geneEntry.getValue().forEach((conditionIndex, letters) ->
                        TopAnatCallFile.appendEntry(entries, conditionIndex, letters));
                writer.write(stageEntry.getKey(), geneEntry.getKey(), entries);
            }
        }
        return log.traceExit(callCount);
    }

    /**
     * @return  The name of the column of a combination of data types, for instance
     *          {@code IN_SITU-RNA_SEQ}. Built from the names of the {@code DataType}s in
     *          their declaration order, so that it is stable. The separator is not the one used
     *          inside the names of the data types themselves, so that a combination can always
     *          be read back unambiguously.
     */
    public static String combinationColumnName(EnumSet<DataType> combination) {
        return combination.stream().map(DataType::name)
                .collect(Collectors.joining(COMBINATION_NAME_SEPARATOR));
    }

    /**
     * @return  Every non-empty combination of {@code DataType}s, ordered by increasing number of
     *          data types, each combination holding its data types in declaration order. These
     *          are the columns of the generated files: topAnat lets the user select the data
     *          types to consider, and the quality of a combination cannot be derived from the
     *          qualities of its data types taken separately.
     */
    public static List<EnumSet<DataType>> dataTypeCombinations() {
        List<EnumSet<DataType>> combinations =
                new ArrayList<>(DataType.getAllPossibleDataTypeCombinations());
        //The combinations are produced unordered, they are sorted so that the columns of
        //the generated files are always the same, in the same order
        combinations.sort((c1, c2) -> {
            int compareSize = Integer.compare(c1.size(), c2.size());
            return compareSize != 0? compareSize:
                combinationColumnName(c1).compareTo(combinationColumnName(c2));
        });
        return Collections.unmodifiableList(combinations);
    }

    /**
     * @return  {@code true} if the condition of {@code call} uses a meta stage, the developmental
     *          stages shared among species being the only ones these files report.
     */
    private static boolean isMetaStageCall(OTFExpressionCall call) {
        String stageId = call.getCondition()
                .getConditionParameterId(ConditionParameter.DEV_STAGE);
        return stageId != null && stageId.startsWith(META_STAGE_ID_PREFIX);
    }

    /**
     * @return  The ID of {@code cond} in topAnat, see
     *          {@link TopAnatUtils#conditionId(String, String)}.
     */
    private static String conditionId(Condition2 cond) {
        AnatEntity[] anatEntityCellType = anatEntityAndCellType(cond);
        return TopAnatUtils.conditionId(anatEntityCellType[0].getId(),
                anatEntityCellType[1].getId());
    }

    /**
     * @return  An {@code AnatEntity[]} of two elements: the anatomical entity of {@code cond}
     *          first, then its cell type, the root of the cell types when the condition targets
     *          no specific cell type.
     */
    private static AnatEntity[] anatEntityAndCellType(Condition2 cond) {
        ComposedEntity<AnatEntity> anatEntityCellType =
                cond.getConditionParameterValue(ConditionParameter.ANAT_ENTITY_CELL_TYPE);
        if (anatEntityCellType == null || anatEntityCellType.isEmpty()) {
            throw log.throwing(new IllegalStateException(
                    "A condition must always have an anat. entity: " + cond));
        }
        if (anatEntityCellType.size() == 1) {
            //The conditions always have a cell type, the root of the cell types when they target
            //none: a single entity is a cell type that is the anat. entity itself, for instance
            //"cell" in "cell", the composition holding its entities as a set. It is a condition
            //of its own, distinct from the anat. entity alone, of which it is a descendant.
            return new AnatEntity[] {anatEntityCellType.getEntity(0),
                    anatEntityCellType.getEntity(0)};
        }
        if (anatEntityCellType.size() == 2) {
            //The cell type comes first in the composition, the anat. entity second
            return new AnatEntity[] {anatEntityCellType.getEntity(1),
                    anatEntityCellType.getEntity(0)};
        }
        throw log.throwing(new IllegalStateException("Unexpected number of entities composing "
                + "the anat. entity and cell type: " + anatEntityCellType));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TopAnatService [readWriteLocks=").append(this.readWriteLocks)
        .append(", serviceFactory=").append(this.getServiceFactory()).append("]");
        return builder.toString();
    }
}
