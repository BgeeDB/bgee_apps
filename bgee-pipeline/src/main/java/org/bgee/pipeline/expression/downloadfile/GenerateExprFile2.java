package org.bgee.pipeline.expression.downloadfile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.ComposedEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.expressiondata.call.CallFilter.ExpressionCallFilter2;
import org.bgee.model.expressiondata.call.CallService;
import org.bgee.model.expressiondata.call.CallService.Attribute;
import org.bgee.model.expressiondata.call.Condition2;
import org.bgee.model.expressiondata.call.ExpressionCallLoader;
import org.bgee.model.expressiondata.call.ExpressionCallProcessedFilter;
import org.bgee.model.expressiondata.call.ExpressionCallProcessedFilter.ExpressionCallProcessedFilterConditionPart;
import org.bgee.model.expressiondata.call.ExpressionCallProcessedFilter.ExpressionCallProcessedFilterInvariablePart;
import org.bgee.model.expressiondata.call.ExpressionCallService;
import org.bgee.model.expressiondata.call.OTFExpressionCall;
import org.bgee.model.expressiondata.call.OTFExpressionCallFilterEngine;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.file.SpeciesDownloadFile.Category;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.bgee.pipeline.CommandRunner;
import org.bgee.pipeline.Utils;
import org.supercsv.cellprocessor.constraint.IsElementOf;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.dozer.CsvDozerBeanWriter;
import org.supercsv.io.dozer.ICsvDozerBeanWriter;

/**
 * Class used to generate expression TSV download files (simple and advanced files) from
 * the Bgee database.
 * 
 * @author  Valentine Rech de Laval
 * @author  Julien Wollbrett
 * @version Bgee 15, May. 2021
 * @since   Bgee 13, Sept. 2016
 */
public class GenerateExprFile2 extends GenerateDownloadFile {

    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(GenerateExprFile2.class.getName());

    private final static Map<Attribute, String> CONDITION_FILE_NAME;

    static {
        CONDITION_FILE_NAME =  new LinkedHashMap<Attribute, String>();
        //XXX: what about cell type?
        CONDITION_FILE_NAME.put(Attribute.ANAT_ENTITY_ID, "anat");
        CONDITION_FILE_NAME.put(Attribute.DEV_STAGE_ID, "development");
        CONDITION_FILE_NAME.put(Attribute.SEX_ID, "sex");
        CONDITION_FILE_NAME.put(Attribute.STRAIN_ID, "strain");
    }


    /**
     * An {@code Enum} used to define the possible expression file types to be generated.
     * <ul>
     * <li>{@code EXPR_SIMPLE}:     presence/absence of expression in a simple download file.
     * <li>{@code EXPR_COMPLETE}:   presence/absence of expression in an advanced download file.
     * </ul>
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public enum SingleSpExprFileType2 implements FileType {
        EXPR_SIMPLE(Category.EXPR_CALLS_SIMPLE, true),
        EXPR_ADVANCED(Category.EXPR_CALLS_COMPLETE, false);

        /**
         * A {@code Category} that is the category of files of this type.
         */
        private final Category category;

        /**
         * A {@code boolean} defining whether this {@code ExprFileType} is a simple file
         * type
         */
        private final boolean simpleFileType;

        /**
         * Constructor providing the {@code Category} of this {@code ExprFileType},
         * and a {@code boolean} defining whether this {@code ExprFileType} is a simple file type.
         */
        private SingleSpExprFileType2(Category category, boolean simpleFileType) {
            this.category = category;
            this.simpleFileType = simpleFileType;
        }

        @Override
        public String getStringRepresentation() {
            return this.category.getStringRepresentation();
        }
        @Override
        public Category getCategory() {
            return this.category;
        }
        @Override
        public boolean isSimpleFileType() {
            return this.simpleFileType;
        }
        @Override
        public String toString() {
            return this.getStringRepresentation();
        }
    }

    /**
     * Main method to trigger the generate expression TSV download files (simple and advanced 
     * files) from Bgee database. Parameters that must be provided in order in {@code args} are:
     * <ol>
     * <li>a list of NCBI species IDs (for instance, {@code 9606} for human) that will be used to 
     * generate download files, separated by the {@code String} {@link CommandRunner#LIST_SEPARATOR}. 
     * If an empty list is provided (see {@link CommandRunner#EMPTY_LIST}), all species contained 
     * in database will be used.
     * <li>a list of files types that will be generated ('expr-simple' for
     * {@link SingleSpExprFileType2#EXPR_SIMPLE}, and 'expr_advanced' for
     * {@link SingleSpExprFileType2#EXPR_ADVANCED}), separated by the {@code String}
     * {@link CommandRunner#LIST_SEPARATOR}. If an empty list is provided 
     * (see {@link CommandRunner#EMPTY_LIST}), all possible file types will be generated.
     * <li>the directory path that will be used to generate download files.
     * <li>a list of condition parameters that will be used to generate files. Only two
     * combinations are supported: {@code ANAT_ENTITY_ID} alone, to aggregate the data of
     * an anatomical entity and cell type whatever the developmental stage, sex and strain, or
     * {@code ANAT_ENTITY_ID}, {@code DEV_STAGE_ID}, {@code SEX_ID} and {@code STRAIN_ID} together.
     * If an empty list is provided, the latter is used. Only the conditions using a meta stage
     * (a developmental stage shared among species) are reported.
     * </ol>
     * 
     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
     * @throws IllegalArgumentException If incorrect parameters were provided.
     * @throws IOException              If an error occurred while trying to write generated files.
     */
    public static void main(String[] args) throws IllegalArgumentException, UncheckedIOException {
        log.traceEntry("{}", (Object[]) args);

        int expectedArgLength = 5;
        if (args.length != expectedArgLength) {
            throw log.throwing(new IllegalArgumentException(
                    "Incorrect number of arguments provided, expected " +
                    expectedArgLength + " arguments, " + args.length + " provided."));
        }

        GenerateExprFile2 generator = new GenerateExprFile2(
            CommandRunner.parseListArgumentAsInt(args[0]),
            GenerateDownloadFile.convertToFileTypes(
                CommandRunner.parseListArgument(args[1]), SingleSpExprFileType2.class),
            args[2],
            GenerateExprFile2.convertToAttributes(CommandRunner.parseListArgument(args[3])),
            Integer.valueOf(CommandRunner.parseArgument(args[4])));
        generator.generateExprFiles();

        log.traceExit();
    }

    private static Set<Attribute> convertToAttributes(List<String> argumentList) {
        log.traceEntry("{}", argumentList);
        Set<Attribute> attrs = EnumSet.noneOf(Attribute.class);
        fileTypeName: for (String argument: argumentList) {
            for (Attribute element: Attribute.values()) {
                if (element.name().equalsIgnoreCase(argument) || 
                        element.name().equals(argument)) {
                    attrs.add(element);
                    continue fileTypeName;
                }
            }
            throw log.throwing(new IllegalArgumentException("\"" + argument + 
                    "\" does not correspond to any element of " + Attribute.class.getName()));
        }

        return log.traceExit(attrs);
    }

    /**
     * A {@code Collection} of {@code Attribute}s defining the condition parameters to be used 
     * to retrieve {@code ExpressionCall}s. If {@code null} or empty, all parameters will be used. 
     */
    private Set<Attribute> params;

//    /**
//     * A {@code boolean} defining whether the filter for simple file keeps observed data only 
//     * if {@code true} or organ observed data only (propagated stages are allowed) if {@code false}.
//     */
//    protected final boolean observedDataOnly;

    /**
     * The only two combinations of condition parameters the files are generated for: the data
     * of an anatomical entity and cell type, whatever the developmental stage, sex and strain...
     */
    private final static Set<Attribute> ANAT_ENTITY_COND_PARAMS =
            Collections.unmodifiableSet(EnumSet.of(Attribute.ANAT_ENTITY_ID));
    /**
     * ...and the data of every condition parameter. The cell type is not part of it: it is always
     * composed with the anatomical entity in the generated files, never a column of its own.
     */
    private final static Set<Attribute> ALL_COND_PARAMS = Collections.unmodifiableSet(
            EnumSet.of(Attribute.ANAT_ENTITY_ID, Attribute.DEV_STAGE_ID, Attribute.SEX_ID,
                    Attribute.STRAIN_ID));
    /**
     * The prefix of the IDs of the meta stages, the developmental stages shared among species.
     * The generated files only report conditions using one of them, so that the files of
     * different species remain comparable (same convention as
     * {@code BgeeToEasyBgee#META_STAGE_ID_PREFIX}).
     */
    //TODO: the logic of UBERON meta stages should be implemented once in the model, and not in each class that needs it.
    private final static String META_STAGE_ID_PREFIX = "UBERON:";

    /**
     * A {@code Supplier} of {@code ServiceFactory}s to be able to provide one to each thread.
     */
    private final Supplier<ServiceFactory> serviceFactorySupplier;

    /**
     * A {@code Integer} corresponding to the number of genes to query per thread.
     */
    private Integer genesChunk;

    /**
     * Default constructor.
     */
    // suppress warning as this default constructor should not be used.
    @SuppressWarnings("unused")
    private GenerateExprFile2() {
        this(null, null, null, null,100);
    }

    /**
     * Constructor providing parameters to generate files, and using the default
     * {@code DAOManager}.
     * 
     * @param speciesIds    A {@code List} of {@code String}s that are the IDs of species we want 
     *                      to generate data for. If {@code null} or empty, all species are used.
     * @param fileTypes     A {@code Set} of {@code ExprFileType}s that are the types of files
     *                      we want to generate. If {@code null} or empty, all {@code ExprFileType}s
     *                      are generated.
     * @param directory     A {@code String} that is the directory where to store files.
     * @param attributes    A {@code Set} of {@code Attribute}s defining the condition parameters
     *                      to be used to retrieve {@code ExpressionCall}s.
     *                      If {@code null} or empty, all parameters will be used. 
     * @param genesChunk    An {@code Integer} that is the number of genes processed per thread
     * @throws IllegalArgumentException If {@code directory} is {@code null} or blank.
     */
    public GenerateExprFile2(List<Integer> speciesIds, Set<SingleSpExprFileType2> fileTypes, 
            String directory, Set<Attribute> attributes, Integer genesChunk) throws IllegalArgumentException {
        this(null, speciesIds, fileTypes, directory, attributes, genesChunk);
    }

    /**
     * Constructor providing the {@code MySQLDAOManager} that will be used by this object
     * to perform queries to the database. This is useful for unit testing.
     * 
     * @param manager           the {@code MySQLDAOManager} to use.
     * @param speciesIds        A {@code List} of {@code String}s that are the IDs of species we want 
     *                          to generate data for. If {@code null} or empty, all species are used.
     * @param fileTypes         A {@code Set} of {@code ExprFileType}s that are the types of files
     *                          we want to generate. If {@code null} or empty, 
     *                          all {@code ExprFileType}s are generated.
     * @param directory         A {@code String} that is the directory where to store files.
     * @param attributes        A {@code Set} of {@code Attribute}s defining the condition parameters
     *                          to be used to retrieve {@code ExpressionCall}s.
     *                          If {@code null} or empty, all parameters will be used. 
     * @param genesChunk    An {@code Integer} that is the number of genes processed per thread
     * @throws IllegalArgumentException If {@code directory} is {@code null} or blank.
     */
    public GenerateExprFile2(MySQLDAOManager manager, List<Integer> speciesIds,
        Set<SingleSpExprFileType2> fileTypes, String directory, Set<Attribute> attributes,
        Integer genesChunk)
                throws IllegalArgumentException {
        this(manager, speciesIds, fileTypes, directory, attributes, ServiceFactory::new, genesChunk);
    }

    /**
     * Constructor providing the {@code MySQLDAOManager} that will be used by this object
     * to perform queries to the database. This is useful for unit testing.
     * 
     * @param manager                   The {@code MySQLDAOManager} to use.
     * @param speciesIds                A {@code List} of {@code String}s that are the IDs of
     *                                  species we want to generate data for.
     *                                  If {@code null} or empty, all species are used.
     * @param fileTypes                 A {@code Set} of {@code ExprFileType}s that are the types
     *                                  of files we want to generate. If {@code null} or empty, 
     *                                  all {@code ExprFileType}s are generated.
     * @param directory                 A {@code String} that is the directory where to store files.
     * @param attributes                A {@code Set} of {@code Attribute}s defining the condition 
     *                                  parameters to be used to retrieve {@code ExpressionCall}s.
     *                                  If {@code null} or empty, all parameters will be used. 
     * @param serviceFactorySupplier    A {@code Supplier} of {@code ServiceFactory}s 
     *                                  to be able to provide one to each thread.
     * @param genesChunk                An {@code Integer} that is the number of genes processed per
     *                                  thread
     * @throws IllegalArgumentException If {@code directory} is {@code null} or blank.
     */
    public GenerateExprFile2(MySQLDAOManager manager, List<Integer> speciesIds,
        Set<SingleSpExprFileType2> fileTypes, String directory, Set<Attribute> attributes,
        Supplier<ServiceFactory> serviceFactorySupplier, Integer genesChunk)
                throws IllegalArgumentException {
        super(manager, speciesIds, fileTypes, directory);
        this.serviceFactorySupplier = serviceFactorySupplier;
        this.genesChunk = genesChunk;
        this.params = Collections.unmodifiableSet(attributes == null?
                new HashSet<>(): new HashSet<>(attributes));
    }

    /**
     * Generate expression files, for the types defined by {@code fileTypes}, for species
     * defined by {@code speciesIds}, in the directory {@code directory}.
     * 
     * @param serviceFactory    A {@code ServiceFactory} to retrieve Bgee services from.
     * @throws UncheckedIOException If an error occurred while trying to write the {@code outputFile}.
     */
    public void generateExprFiles() throws UncheckedIOException {
        log.traceEntry();

        Set<Integer> setSpecies = Collections.unmodifiableSet(this.speciesIds == null?
                new HashSet<>() : new HashSet<>(this.speciesIds));

        // Check user input, retrieve info for generating file names
        // Retrieve species names and IDs (all species names if speciesIds is null or empty)
        // FIXME use supplier?
        Map<Integer, String> speciesNamesForFilesByIds = Utils.checkAndGetLatinNamesBySpeciesIds(
                setSpecies, serviceFactorySupplier.get().getSpeciesService());
        assert speciesNamesForFilesByIds.size() >= setSpecies.size();

        // If no file types are given by user, we set all file types
        if (this.fileTypes == null || this.fileTypes.isEmpty()) {
            this.fileTypes = EnumSet.allOf(SingleSpExprFileType2.class);
        }

        // If no parameters are given by user, all condition parameters are used
        if (this.params == null || this.params.isEmpty()) {
            this.params = EnumSet.copyOf(ALL_COND_PARAMS);
        }
        if (!ANAT_ENTITY_COND_PARAMS.equals(this.params) && !ALL_COND_PARAMS.equals(this.params)) {
            throw log.throwing(new IllegalArgumentException("Files are only generated for "
                    + "the condition parameters " + ANAT_ENTITY_COND_PARAMS + " or "
                    + ALL_COND_PARAMS + ", provided: " + this.params));
        }

        // Generate expression files, species by species.
        // The generation of files are independent, so we can safely go multi-threading
        for(Integer speciesId: speciesNamesForFilesByIds.keySet()) {
            log.info("Start generating of expression files for the species {}...", speciesId);

            try {
                this.generateExprFilesForOneSpecies(speciesNamesForFilesByIds.get(speciesId), 
                        speciesId);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } finally {
                // close connection to database between each species, to avoid idle
                // connection reset
                this.getManager().releaseResources();
            }
            log.info("Done generating of expression files for the species {}.", speciesId);
        }

        log.traceExit();
    }

    /**
     * Generate download files (simple and/or advanced) containing absence/presence of
     * expression, for species defined by {@code speciesId}. This method is responsible
     * for retrieving data from the data source, and then to write them into files, in the
     * directory provided at instantiation. File types to be generated are provided at
     * instantiation.
     * 
     * @param fileNamePrefix        A {@code String} to be used as a prefix of the names 
     *                              of the generated files. 
     * @param speciesId             A {@code Integer} that is the ID of species for which files are
     *                              generated. 
     * @throws UncheckedIOException If an error occurred while trying to write the {@code outputFile}.
     * @throws IOException          If an error occurred while trying to delete the {@code outputFile}.
     */
    private void generateExprFilesForOneSpecies(String fileNamePrefix, Integer speciesId)
            throws UncheckedIOException, IOException {
        log.traceEntry("{}, {}",fileNamePrefix, speciesId);

        log.debug("Start generating expression files for the species {}, file types {}, and parameters {}...", 
                speciesId, this.fileTypes, this.params);

        //********************************
        // RETRIEVE DATA FROM DATA SOURCE
        //********************************


        Map<SummaryCallType.ExpressionSummary, SummaryQuality> summaryCallTypeQualityFilter =
                new HashMap<>();
        summaryCallTypeQualityFilter.put(SummaryCallType.ExpressionSummary.EXPRESSED,
                SummaryQuality.SILVER);
        summaryCallTypeQualityFilter.put(SummaryCallType.ExpressionSummary.NOT_EXPRESSED,
                SummaryQuality.SILVER);

        //The combination of condition parameters the calls are propagated over, derived from
        //the columns requested (see generateExprFiles for the two combinations supported).
        Set<ConditionParameter<?, ?>> condParamCombination =
                ALL_COND_PARAMS.equals(this.params)?
                        new LinkedHashSet<>(ConditionParameter.allOf()):
                        Set.of(ConditionParameter.ANAT_ENTITY_CELL_TYPE);

        // Ordering attributes, used to sort the content of the files once written
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering =
                new LinkedHashMap<>();
        serviceOrdering.put(CallService.OrderingAttribute.GENE_ID, Service.Direction.ASC);
        serviceOrdering.put(CallService.OrderingAttribute.ANAT_ENTITY_ID, Service.Direction.ASC);
        if (this.params.contains(CallService.Attribute.DEV_STAGE_ID)) {
            serviceOrdering.put(CallService.OrderingAttribute.DEV_STAGE_ID, Service.Direction.ASC);
        }
        if (this.params.contains(CallService.Attribute.SEX_ID)) {
            serviceOrdering.put(CallService.OrderingAttribute.SEX_ID, Service.Direction.ASC);
        }
        if (this.params.contains(CallService.Attribute.STRAIN_ID)) {
            serviceOrdering.put(CallService.OrderingAttribute.STRAIN_ID, Service.Direction.ASC);
        }

        //****************************
        // PRODUCE AND WRITE DATA
        //****************************
        log.trace("Start generating and writing file content for species {} and file types {}...",
            speciesId, this.fileTypes);

        // Now, we write all requested expression files at once. This way, we will generate
        // the data only once, and we will not have to store them in memory
        // (the memory usage could be huge).

        // OK, first we allow to store file names, writers, etc, associated to a FileType,
        // for the catch and finally clauses.
        Map<FileType, String> generatedFileNames = new HashMap<>();

        // We will write results in temporary files that we will rename at the end
        // if everything is correct
        String tmpExtension = ".tmp";

        long numberOfRows = 0;

        // In order to close all writers in a finally clause
        Map<SingleSpExprFileType2, ICsvDozerBeanWriter> writersUsed = new HashMap<>();

        Map<SingleSpExprFileType2, CellProcessor[]> processors = new HashMap<>();
        Map<SingleSpExprFileType2, String[]> headers = new HashMap<>();

        try {
            //**************************
            // OPEN FILES, CREATE WRITERS, WRITE HEADERS
            //**************************

            for (FileType fileType : this.fileTypes) {
                SingleSpExprFileType2 currentFileType = (SingleSpExprFileType2) fileType;
                String[] fileTypeHeaders = this.generateExprFileHeader(currentFileType);
                headers.put(currentFileType, fileTypeHeaders);

                CellProcessor[] fileTypeProcessors = this.generateExprFileCellProcessors(
                        currentFileType, fileTypeHeaders);
                processors.put(currentFileType, fileTypeProcessors);

                // Create file name
                String suffix = this.convertAttributeToFileName(this.params);
                if (StringUtils.isBlank(suffix)) {
                    suffix = "";
                } else {
                    suffix = "_" + suffix;
                }
                String fileName = this.formatString(fileNamePrefix + "_" +
                        currentFileType.getStringRepresentation() + suffix + EXTENSION);
                generatedFileNames.put(currentFileType, fileName);

                // write in temp file
                File file = new File(this.directory, fileName + tmpExtension);
                // override any existing file
                if (file.exists()) {
                    file.delete();
                }

                // create writer and write header
                ICsvDozerBeanWriter beanWriter = new CsvDozerBeanWriter(new FileWriter(file),
                        Utils.getCsvPreferenceWithQuote(this.generateQuoteMode(fileTypeHeaders)));
                // configure the mapping from the fields to the CSV columns
                if (currentFileType.isSimpleFileType()) {
                    beanWriter.configureBeanMapping(SingleSpeciesSimpleExprFileBean.class, 
                            this.generateFieldMapping(currentFileType, fileTypeHeaders));
                } else {
                    beanWriter.configureBeanMapping(SingleSpeciesCompleteExprFileBean.class, 
                            this.generateFieldMapping(currentFileType, fileTypeHeaders));
                }

                beanWriter.writeHeader(fileTypeHeaders);
                writersUsed.put(currentFileType, beanWriter);
            }

            // ****************************
            // WRITE ROWS
            // ****************************

            log.trace("Start retrieving data for expression files for the species {}.", speciesId);

            ServiceFactory serviceFactory = this.serviceFactorySupplier.get();

            final Set<Integer> speciesFilter = Collections.singleton(speciesId);

            // Load non-informative anatomical entities:
            // calls occurring in these anatomical entities, and generated from
            // data propagation only (no observed data in them), will be discarded.
            // TODO: filter by non informative anat. entities in ExpressionCallFilter instead of
            // filter stream, see comment in ConditionFilter class.
            Set<String> nonInformativeAnatEntities = serviceFactory.getAnatEntityService()
                    .loadNonInformativeAnatEntitiesBySpeciesIds(speciesFilter, false)
                    .map(AnatEntity::getId)
                    .collect(Collectors.toSet());

            //The calls are propagated on the fly, gene by gene: genes are therefore processed
            //by batches, and everything that does not depend on the genes is computed once and
            //reused for all of them - the condition part of the processed filter holds all
            //the conditions of the species, it is by far the most expensive part.
            List<String> geneIds = serviceFactory.getGeneService()
                    .loadGenes(new GeneFilter(speciesId))
                    .map(Gene::getGeneId)
                    .collect(Collectors.toList());
            log.info("Species {}: {} genes to process by batches of at most {}.", speciesId,
                    geneIds.size(), this.genesChunk);
            if (!geneIds.isEmpty()) {
                ExpressionCallProcessedFilter seedProcessedFilter = serviceFactory
                        .getExpressionCallService().processExpressionCallFilter(
                                buildCallFilter(speciesId, geneIds.subList(0, 1),
                                        condParamCombination, summaryCallTypeQualityFilter, null));
                ExpressionCallProcessedFilterConditionPart condPart =
                        seedProcessedFilter.getConditionPart();
                ExpressionCallProcessedFilterInvariablePart invariablePart =
                        seedProcessedFilter.getInvariablePart();

                //The complete file reports the call of each data type separately, and
                //the propagation aggregates the data types it is given: it is therefore run once
                //per data type as well, in addition to the run over all of them.
                boolean completeFileRequested = this.fileTypes.stream()
                        .anyMatch(ft -> !((SingleSpExprFileType2) ft).isSimpleFileType());

                AtomicInteger index = new AtomicInteger(0);
                geneIds.stream()
                .collect(Collectors.groupingBy(x -> index.getAndIncrement() / this.genesChunk))
                .values().parallelStream().forEach(geneBatch -> {
                    //init thread safe service factory
                    ExpressionCallService callService = serviceFactorySupplier.get()
                            .getExpressionCallService();
                    Map<Gene, List<OTFExpressionCall>> callsByGene = loadCalls(callService,
                            speciesId, geneBatch, condParamCombination,
                            summaryCallTypeQualityFilter, null, condPart, invariablePart);
                    //Per data type, no filter is applied on the summary call type, so that
                    //the call of a data type is reported even when that data type alone would not
                    //produce the call the gene is summarized with, and none on the observed data,
                    //so that the observed data column of a data type says what it did observe.
                    Map<DataType, Map<Gene, List<OTFExpressionCall>>> callsByDataType =
                            new EnumMap<>(DataType.class);
                    if (completeFileRequested) {
                        for (DataType dataType: DataType.values()) {
                            callsByDataType.put(dataType, loadCalls(callService, speciesId,
                                    geneBatch, condParamCombination, null, EnumSet.of(dataType),
                                    condPart, invariablePart));
                        }
                    }

                    for (Entry<Gene, List<OTFExpressionCall>> geneEntry: callsByGene.entrySet()) {
                        List<OTFExpressionCall> calls = geneEntry.getValue().stream()
                                .filter(GenerateExprFile2::isMetaStageCall)
                                .filter(c -> !nonInformativeAnatEntities.contains(
                                        anatEntityAndCellType(c.getCondition())[0].getId()))
                                .collect(Collectors.toList());
                        if (calls.isEmpty()) {
                            continue;
                        }
                        Map<DataType, Map<Condition2, OTFExpressionCall>> dataTypeCallsByCond =
                                new EnumMap<>(DataType.class);
                        for (Entry<DataType, Map<Gene, List<OTFExpressionCall>>> dataTypeEntry:
                                callsByDataType.entrySet()) {
                            dataTypeCallsByCond.put(dataTypeEntry.getKey(),
                                    dataTypeEntry.getValue()
                                    .getOrDefault(geneEntry.getKey(), List.of()).stream()
                                    .collect(Collectors.toMap(OTFExpressionCall::getCondition,
                                            c -> c)));
                        }
                        this.writeRows(writersUsed, processors, headers, calls,
                                dataTypeCallsByCond, seedProcessedFilter);
                    }
                });
            }

            log.trace("Done retrieving expression data for expression files for the species {}.",
                    speciesId);

        } catch (Exception e) {
            this.deleteTempFiles(generatedFileNames, tmpExtension);
            throw e;
        } finally {
            for (ICsvDozerBeanWriter writer : writersUsed.values()) {
                writer.close();
            }
        }

      //as file is written by chunk it is faster to count rows once it was written
        numberOfRows = Files.lines(Paths.get(this.directory,
                generatedFileNames.values().iterator().next() + tmpExtension))
                .count();

        for (Entry<FileType, String> generatedFileName: generatedFileNames.entrySet()) {
            Path sortedFile = Paths.get(this.directory, generatedFileName.getValue() + ".sorted");
            Path tempFile = Paths.get(this.directory, generatedFileName.getValue()  + tmpExtension);
            try {
                String header =Files.lines(tempFile)
                        .findFirst().get();
                Stream<CharSequence> sortedLines =Files.lines(tempFile)
                        .skip(1)
                        .map(s ->  s.split("\t"))
                        .sorted(generateComparator(headers.get(generatedFileName.getKey()),
                                serviceOrdering))
                        .map(s -> String.join("\t", s));
                //write header
                Files.write(sortedFile, (header + System.lineSeparator()).getBytes(),
                        StandardOpenOption.CREATE,StandardOpenOption.APPEND);
                //write sorted content
                Files.write(sortedFile, sortedLines::iterator, StandardOpenOption.APPEND);
                Files.move(sortedFile, tempFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        // now, if everything went fine, we rename or delete the temporary files
        if (numberOfRows > 1) {
            log.info("Each expression file for the species {} contains {} rows.",
                    speciesId, numberOfRows);
            this.renameTempFiles(generatedFileNames, tmpExtension);
        } else {
            log.info("Expression files for the species {} contains no rows.", speciesId);
            this.deleteTempFiles(generatedFileNames, tmpExtension);
        }

        log.traceExit();
    }

    private Comparator<String[]> generateComparator(String[] headers,
            Map<CallService.OrderingAttribute, Service.Direction> serviceOrdering) {
        List<String> headersList = Arrays.asList(headers);
        //always ordering first by geneId
        Comparator<String[]> linesComparator = Comparator
                .comparing(s -> s[headersList.indexOf(GENE_ID_COLUMN_NAME)]);
        if(serviceOrdering.get(CallService.OrderingAttribute.GENE_ID)
                .equals(Service.Direction.DESC)) {
            linesComparator = linesComparator.reversed();
        }
        if(serviceOrdering.containsKey(CallService.OrderingAttribute.ANAT_ENTITY_ID)) {
            linesComparator = linesComparator.thenComparing(s -> s[2]);
            if(serviceOrdering.get(CallService.OrderingAttribute.ANAT_ENTITY_ID)
                    .equals(Service.Direction.DESC)) {
                linesComparator = linesComparator.reversed();
            }
        }
        if(serviceOrdering.containsKey(CallService.OrderingAttribute.DEV_STAGE_ID)) {
            linesComparator = linesComparator.thenComparing(s -> s[headersList.indexOf(STAGE_ID_COLUMN_NAME)]);
            if(serviceOrdering.get(CallService.OrderingAttribute.DEV_STAGE_ID)
                    .equals(Service.Direction.DESC)) {
                linesComparator = linesComparator.reversed();
            }
        }
        if(serviceOrdering.containsKey(CallService.OrderingAttribute.SEX_ID)) {
            linesComparator = linesComparator.thenComparing(s -> s[headersList.indexOf(SEX_COLUMN_NAME)]);
            if(serviceOrdering.get(CallService.OrderingAttribute.SEX_ID)
                    .equals(Service.Direction.DESC)) {
                linesComparator = linesComparator.reversed();
            }
        }
        if(serviceOrdering.containsKey(CallService.OrderingAttribute.STRAIN_ID)) {
            linesComparator = linesComparator.thenComparing(s -> s[headersList.indexOf(STRAIN_COLUMN_NAME)]);
            if(serviceOrdering.get(CallService.OrderingAttribute.STRAIN_ID)
                    .equals(Service.Direction.DESC)) {
                linesComparator = linesComparator.reversed();
            }
        }
        return linesComparator;
    }


    /**
     * Convert attributes into a {@code String} to be used to generate file names.
     * 
     * @param attributes    A {@code Set} of {@code Attribute}s defining the condition 
     *                      parameters to be used to retrieve {@code ExpressionCall}s.
     *                      It should not be {@code null} nor empty. 
     * @return              The {@code String} to be used to generate file names.
     */
    private String convertAttributeToFileName(Set<Attribute> attributes) {
        log.traceEntry("{}", attributes);

        assert attributes!= null && !attributes.isEmpty();

        List<Attribute> attributeList = new ArrayList<>(attributes);
        Collections.sort(attributeList);

        String joinedAttributes = attributeList.stream()
                //we use a flatMap to be able to return an empty Stream (avoid having double '_' in file name)
                .flatMap(a -> {
                    switch (a) {
                        case ANAT_ENTITY_ID:
                            return Stream.of(CONDITION_FILE_NAME.get(Attribute.ANAT_ENTITY_ID));
                        case DEV_STAGE_ID:
                            return Stream.of(CONDITION_FILE_NAME.get(Attribute.DEV_STAGE_ID));
                        case SEX_ID:
                            return Stream.of(CONDITION_FILE_NAME.get(Attribute.SEX_ID));
                        case STRAIN_ID:
                            return Stream.of(CONDITION_FILE_NAME.get(Attribute.STRAIN_ID));
                        default:
                            throw new IllegalArgumentException("Attribute not supported: " + a);
                    }})
                .collect(Collectors.joining("_"));
        if(joinedAttributes.equals(CONDITION_FILE_NAME.get(Attribute.ANAT_ENTITY_ID))) {
            return null;
        } else if (joinedAttributes.equals(CONDITION_FILE_NAME.values()
                .stream().collect(Collectors.joining("_")))) {
            return "all_conditions";
        }
        return joinedAttributes;


    }

    /**
     * Generates an {@code Array} of {@code CellProcessor}s used to process an expression
     * TSV file of type {@code fileType}.
     * 
     * @param fileType  The {@code ExprFileType} of the file to be generated.
     * @param header    An {@code Array} of {@code String}s representing the names 
     *                  of the columns of an expression file.
     * @return          An {@code Array} of {@code CellProcessor}s used to process 
     *                  an expression file.
     * @throw IllegalArgumentException If {@code fileType} is not managed by this method.
     */
    private CellProcessor[] generateExprFileCellProcessors(
            SingleSpExprFileType2 fileType, String[] header) throws IllegalArgumentException {
        log.traceEntry("{}, {}", fileType, header);

        List<Object> expressionSummaries = new ArrayList<Object>();
        for (ExpressionSummary sum : ExpressionSummary.values()) {
            expressionSummaries.add(convertExpressionSummaryToString(sum));
        }
        expressionSummaries.add(GenerateDownloadFile.NO_DATA_VALUE);

        List<Object> qualitySummaries = new ArrayList<Object>();
        qualitySummaries.add(convertSummaryQualityToString(SummaryQuality.GOLD));
        qualitySummaries.add(convertSummaryQualityToString(SummaryQuality.SILVER));
        qualitySummaries.add(convertSummaryQualityToString(SummaryQuality.BRONZE));
        qualitySummaries.add(NA_VALUE);

        List<Object> originValues = new ArrayList<Object>();
        for (ObservedData data : ObservedData.values()) {
            originValues.add(data.getStringRepresentation());
        }

        //Then, we build the CellProcessor
        CellProcessor[] processors = new CellProcessor[header.length];
        for (int i = 0; i < header.length; i++) {
            switch (header[i]) {
            // *** CellProcessors common to all file types ***
                case GENE_ID_COLUMN_NAME:
                case ANAT_ENTITY_ID_COLUMN_NAME:
                case ANAT_ENTITY_NAME_COLUMN_NAME:
                case STAGE_ID_COLUMN_NAME:
                case STAGE_NAME_COLUMN_NAME:
                case STRAIN_COLUMN_NAME:
                case SEX_COLUMN_NAME:
                    processors[i] = new StrNotNullOrEmpty();
                    break;
                case GENE_NAME_COLUMN_NAME:
                    processors[i] = new NotNull();
                    break;
                case EXPRESSION_COLUMN_NAME:
                    processors[i] = new IsElementOf(expressionSummaries);
                    break;
                case QUALITY_COLUMN_NAME:
                    processors[i] = new IsElementOf(qualitySummaries);
                    break;
                case EXPRESSION_SCORE_COLUMN_NAME:
                case FDR_COLUMN_NAME:
                    // It is a String to be able to write values such as '3.32e4' and NA_VALUE.
                    // It could be a Long if we didn't want exponential values
                    // and if all rows had a score and a rank => not the case
//                    processors[i] = new LMinMax(0, Long.MAX_VALUE);
                    processors[i] = new StrNotNullOrEmpty();
                    break;
            }

            // If it was one of the column common to all file types,
            // iterate next column name
            if (processors[i] != null) {
                continue;
            }

            if (!fileType.isSimpleFileType()) {
                // *** Attributes specific to complete file ***
                if (header[i].equals(IN_SITU_DATA_COLUMN_NAME) ||
                        header[i].equals(RNASEQ_DATA_COLUMN_NAME) ||
                        header[i].equals(SC_RNA_SEQ_DATA_COLUMN_NAME)) {
                    processors[i] = new IsElementOf(expressionSummaries);
                } else if (header[i].equals(IN_SITU_QUAL_COLUMN_NAME) ||
                        header[i].equals(RNASEQ_QUAL_COLUMN_NAME) ||
                        header[i].equals(SC_RNA_SEQ_QUAL_COLUMN_NAME)) {
                    processors[i] = new IsElementOf(qualitySummaries);
                } else if (header[i].equals(IN_SITU_OBSERVED_DATA_COLUMN_NAME) ||
                        header[i].equals(RNASEQ_OBSERVED_DATA_COLUMN_NAME) ||
                        header[i].equals(SC_RNA_SEQ_OBSERVED_DATA_COLUMN_NAME) ||
                        header[i].equals(INCLUDING_OBSERVED_DATA_COLUMN_NAME)) {
                    processors[i] = new IsElementOf(originValues);
                } else if (header[i].equals(IN_SITU_EXPRESSION_SCORE_COLUMN_NAME) ||
                        header[i].equals(IN_SITU_WEIGHT_COLUMN_NAME) ||
                        header[i].equals(IN_SITU_FDR_COLUMN_NAME) ||
                        header[i].equals(RNASEQ_EXPRESSION_SCORE_COLUMN_NAME) ||
                        header[i].equals(RNASEQ_WEIGHT_COLUMN_NAME) ||
                        header[i].equals(RNASEQ_FDR_COLUMN_NAME) ||
                        header[i].equals(SC_RNA_SEQ_EXPRESSION_SCORE_COLUMN_NAME) ||
                        header[i].equals(SC_RNA_SEQ_WEIGHT_COLUMN_NAME) ||
                        header[i].equals(SC_RNA_SEQ_FDR_COLUMN_NAME)) {
                    // It is a String to be able to write values such as '3.32e4' and NA_VALUE.
                    // It could be a Long if we didn't want exponential values
                    // and if all columns had a score and a rank => not the case
//                    processors[i] = new LMinMax(0, Long.MAX_VALUE);
                    processors[i] = new StrNotNullOrEmpty();
                }
            }
            if (processors[i] == null) {
                throw log.throwing(new IllegalArgumentException("Unrecognized header: "
                        + header[i] + " for file type: " + fileType.getStringRepresentation()));
            }
        }
        return log.traceExit(processors);
    }

    /**
     * Generates an {@code Array} of {@code String}s used to generate the header of an
     * expression TSV file of type {@code fileType}.
     * 
     * @param fileType  An {@code ExprFileType} of the file to be generated.
     * @return          The {@code Array} of {@code String}s used to produce the header.
     */
    private String[] generateExprFileHeader(SingleSpExprFileType2 fileType) {
        log.traceEntry("{}",fileType);

        for (Attribute attr: this.params) {
            if (!attr.isConditionParameter()) {
                throw log.throwing(new IllegalArgumentException("[" + attr +"] is not a valid "
                        + "condition parameter"));
            }
        }
        //The columns are collected in a List rather than assigned at computed indexes in an array
        //of hard-coded size, so that adding or removing a column is a one-line change.
        List<String> headers = new ArrayList<>();
        // *** Headers common to all file types ***
        headers.add(GENE_ID_COLUMN_NAME);
        headers.add(GENE_NAME_COLUMN_NAME);
        if (this.params.contains(CallService.Attribute.ANAT_ENTITY_ID)) {
            headers.add(ANAT_ENTITY_ID_COLUMN_NAME);
            headers.add(ANAT_ENTITY_NAME_COLUMN_NAME);
        }
        if (this.params.contains(CallService.Attribute.DEV_STAGE_ID)) {
            headers.add(STAGE_ID_COLUMN_NAME);
            headers.add(STAGE_NAME_COLUMN_NAME);
        }
        if (this.params.contains(CallService.Attribute.SEX_ID)) {
            headers.add(SEX_COLUMN_NAME);
        }
        if (this.params.contains(CallService.Attribute.STRAIN_ID)) {
            headers.add(STRAIN_COLUMN_NAME);
        }
        headers.add(EXPRESSION_COLUMN_NAME);
        headers.add(QUALITY_COLUMN_NAME);
        headers.add(FDR_COLUMN_NAME);
        headers.add(EXPRESSION_SCORE_COLUMN_NAME);
        if (!fileType.isSimpleFileType()) {
            // *** Headers specific to complete file ***
            headers.add(INCLUDING_OBSERVED_DATA_COLUMN_NAME);
            headers.add(IN_SITU_DATA_COLUMN_NAME);
            headers.add(IN_SITU_QUAL_COLUMN_NAME);
            headers.add(IN_SITU_FDR_COLUMN_NAME);
            headers.add(IN_SITU_EXPRESSION_SCORE_COLUMN_NAME);
            headers.add(IN_SITU_WEIGHT_COLUMN_NAME);
            headers.add(IN_SITU_OBSERVED_DATA_COLUMN_NAME);
            headers.add(RNASEQ_DATA_COLUMN_NAME);
            headers.add(RNASEQ_QUAL_COLUMN_NAME);
            headers.add(RNASEQ_FDR_COLUMN_NAME);
            headers.add(RNASEQ_EXPRESSION_SCORE_COLUMN_NAME);
            headers.add(RNASEQ_WEIGHT_COLUMN_NAME);
            headers.add(RNASEQ_OBSERVED_DATA_COLUMN_NAME);
            headers.add(SC_RNA_SEQ_DATA_COLUMN_NAME);
            headers.add(SC_RNA_SEQ_QUAL_COLUMN_NAME);
            headers.add(SC_RNA_SEQ_FDR_COLUMN_NAME);
            headers.add(SC_RNA_SEQ_EXPRESSION_SCORE_COLUMN_NAME);
            headers.add(SC_RNA_SEQ_WEIGHT_COLUMN_NAME);
            headers.add(SC_RNA_SEQ_OBSERVED_DATA_COLUMN_NAME);
        }
        return log.traceExit(headers.toArray(new String[0]));
    }

    /**
     * Generate the field mapping for each column of the header of a single-species
     * expression TSV file of type {@code fileType}.
     * 
     * @param fileType  A {@code SingleSpExprFileType2} defining the type of file 
     *                  that will be written.
     * @param header    An {@code Array} of {@code String}s representing the names 
     *                  of the columns of a single-species expression file.
     * @return          The {@code Array} of {@code String}s that is the field mapping, 
     *                  put in the {@code Array} at the same index as the column they 
     *                  are supposed to process.
     * @throws IllegalArgumentException If a {@code String} in {@code header} is not recognized.
     */
    private String[] generateFieldMapping(SingleSpExprFileType2 fileType, String[] header)
            throws IllegalArgumentException {
        log.traceEntry("{}, {}", fileType, header);

        //to do a sanity check on species columns in simple files
        EnumSet<DataType> dataTypeFound = EnumSet.noneOf(DataType.class);

        String[] mapping = new String[header.length];
        for (int i = 0; i < header.length; i++) {
            switch (header[i]) {
                // *** attributes common to all file types ***
                case GENE_ID_COLUMN_NAME:
                    mapping[i] = "geneId";
                    break;
                case GENE_NAME_COLUMN_NAME:
                    mapping[i] = "geneName";
                    break;
                case ANAT_ENTITY_ID_COLUMN_NAME:
                    mapping[i] = "anatEntityId";
                    break;
                case ANAT_ENTITY_NAME_COLUMN_NAME:
                    mapping[i] = "anatEntityName";
                    break;
                case STAGE_ID_COLUMN_NAME:
                    mapping[i] = "devStageId";
                    break;
                case STAGE_NAME_COLUMN_NAME:
                    mapping[i] = "devStageName";
                    break;
                case SEX_COLUMN_NAME:
                    mapping[i] = "sex";
                    break;
                case STRAIN_COLUMN_NAME:
                    mapping[i] = "strain";
                    break;
                case EXPRESSION_COLUMN_NAME:
                    mapping[i] = "expression";
                    break;
                case QUALITY_COLUMN_NAME:
                    mapping[i] = "callQuality";
                    break;
                case EXPRESSION_SCORE_COLUMN_NAME:
                    mapping[i] = "expressionScore";
                    break;
                case FDR_COLUMN_NAME:
                    mapping[i] = "fdr";
                    break;
            }

            //if it was one of the column common to all beans,
            //iterate next column name
            if (mapping[i] != null) {
                continue;
            }

            if (!fileType.isSimpleFileType()) {
                // *** Attributes specific to complete file ***

                if (header[i].equals(INCLUDING_OBSERVED_DATA_COLUMN_NAME)) {
                    mapping[i] = "includingObservedData";
                }

                //if header found, iterate next column name
                if (mapping[i] != null) {
                    continue;
                }

                // We need to find the data type contains in the header to be able to
                // assign the good index to DataExprCounts.
                // For that, we iterate all data types to retrieve the data.
                int index = -1;
                DataType dataType = null;
                DataType[] dataTypes = DataType.values();
                for (int dtIndex = 0; dtIndex < dataTypes.length; dtIndex++) {
                    DataType dt = dataTypes[dtIndex];
                    if (header[i].toLowerCase().contains(dt.getStringRepresentation().toLowerCase()) &&
                            //Need this check because the name of full-length includes the name
                            //of RNA-Seq
                            (!dt.equals(DataType.RNA_SEQ) || !header[i].toLowerCase().contains(
                                    DataType.SC_RNA_SEQ.getStringRepresentation().toLowerCase()))) {
                        index = dtIndex;
                        dataType = dt;
                        break;
                    }
                }
                if (dataType == null) {
                    throw log.throwing(new IllegalArgumentException("Column does not correspond to "
                            + "any datatype."));
                }

                dataTypeFound.add(dataType);
                if (header[i].startsWith(OBSERVED_DATA_COLUMN_NAME_PREFIX)
                        && header[i].endsWith(OBSERVED_DATA_COLUMN_NAME_SUFFIX)) {
                    mapping[i] = "dataExprCounts[" + index + "].observedData";

                } else if (header[i].endsWith(EXPRESSION_SCORE_COLUMN_NAME_SUFFIX)) {
                    mapping[i] = "dataExprCounts[" + index + "].expressionScore";

                } else if (header[i].endsWith(WEIGHT_COLUMN_NAME_SUFFIX)) {
                    mapping[i] = "dataExprCounts[" + index + "].weight";

                } else if (header[i].endsWith(CALL_QUALITY_COLUMN_NAME_SUFFIX)) {
                    mapping[i] = "dataExprCounts[" + index + "].callQuality";

                } else if (header[i].endsWith(CALL_TYPE_COLUMN_NAME_SUFFIX)) {
                    // this should be the last tested because it's the least specific
                    mapping[i] = "dataExprCounts[" + index + "].callType";

                } else if (header[i].endsWith(FDR_COLUMN_NAME_SUFFIX)) {
                    mapping[i] = "dataExprCounts[" + index + "].fdr";

                } else {
                    throw log.throwing(new IllegalArgumentException("Unrecognized header: "
                            + header[i] + " for file type: " +
                            fileType.getStringRepresentation()));
                }
                assert(mapping[i] != null);
            }

            if (mapping[i] == null) {
                throw log.throwing(new IllegalArgumentException("Unrecognized header: "
                        + header[i] + " for file type: " + fileType.getStringRepresentation()));
            }
        }
        // Verify that we found all data types
        assert fileType.isSimpleFileType() || dataTypeFound.containsAll(EnumSet.allOf(DataType.class)) &&
        EnumSet.allOf(DataType.class).containsAll(dataTypeFound):
            "Some of data types were not found in the header: expected: "
            + EnumSet.allOf(DataType.class) + " - found: " + dataTypeFound;

        return log.traceExit(mapping);
    }

    /**
     * Generate {@code Array} of {@code booleans} (one per CSV column) indicating 
     * whether each column should be quoted or not.
     *
     * @param headers   An {@code Array} of {@code String}s representing the names of the columns.
     * @return          the {@code Array } of {@code booleans} (one per CSV column) indicating 
     *                  whether each column should be quoted or not.
     */
    private boolean[] generateQuoteMode(String[] headers) {
        log.traceEntry("{}", (Object[]) headers);

        boolean[] quoteMode = new boolean[headers.length];
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].equals(GENE_ID_COLUMN_NAME) ||
                    headers[i].equals(ANAT_ENTITY_ID_COLUMN_NAME) ||
                    headers[i].equals(STAGE_ID_COLUMN_NAME) ||
                    headers[i].equals(CELL_TYPE_ID_COLUMN_NAME) ||
                    headers[i].equals(SEX_COLUMN_NAME) ||
                    headers[i].equals(STRAIN_COLUMN_NAME) ||
                    headers[i].equals(EXPRESSION_COLUMN_NAME) ||
                    headers[i].equals(QUALITY_COLUMN_NAME) ||
                    headers[i].equals(EXPRESSION_SCORE_COLUMN_NAME) ||
                    headers[i].equals(INCLUDING_OBSERVED_DATA_COLUMN_NAME) ||

                    headers[i].equals(IN_SITU_DATA_COLUMN_NAME) ||
                    headers[i].equals(IN_SITU_QUAL_COLUMN_NAME) ||
                    headers[i].equals(IN_SITU_FDR_COLUMN_NAME) ||
                    headers[i].equals(IN_SITU_EXPRESSION_SCORE_COLUMN_NAME) ||
                    headers[i].equals(IN_SITU_WEIGHT_COLUMN_NAME) ||
                    headers[i].equals(IN_SITU_OBSERVED_DATA_COLUMN_NAME) ||

                    headers[i].equals(RNASEQ_DATA_COLUMN_NAME) ||
                    headers[i].equals(RNASEQ_QUAL_COLUMN_NAME) ||
                    headers[i].equals(RNASEQ_FDR_COLUMN_NAME) ||
                    headers[i].equals(RNASEQ_EXPRESSION_SCORE_COLUMN_NAME) ||
                    headers[i].equals(RNASEQ_WEIGHT_COLUMN_NAME) ||
                    headers[i].equals(RNASEQ_OBSERVED_DATA_COLUMN_NAME) ||

                    headers[i].equals(SC_RNA_SEQ_DATA_COLUMN_NAME) ||
                    headers[i].equals(SC_RNA_SEQ_QUAL_COLUMN_NAME) ||
                    headers[i].equals(SC_RNA_SEQ_FDR_COLUMN_NAME) ||
                    headers[i].equals(SC_RNA_SEQ_EXPRESSION_SCORE_COLUMN_NAME) ||
                    headers[i].equals(SC_RNA_SEQ_WEIGHT_COLUMN_NAME) ||
                    headers[i].equals(SC_RNA_SEQ_OBSERVED_DATA_COLUMN_NAME) ||

                    headers[i].equals(FDR_COLUMN_NAME)) {

                quoteMode[i] = false;
            } else if (headers[i].equals(GENE_NAME_COLUMN_NAME) ||
                        headers[i].equals(ANAT_ENTITY_NAME_COLUMN_NAME) ||
                        headers[i].equals(STAGE_NAME_COLUMN_NAME) ||
                        headers[i].equals(CELL_TYPE_NAME_COLUMN_NAME)) {

                quoteMode[i] = true;
            } else {
                    throw log.throwing(new IllegalArgumentException(
                            "Unrecognized header: " + headers[i] + " for OMA TSV file."));
            }
        }

        return log.traceExit(quoteMode);
    }

    /**
     * @param speciesId                     An {@code Integer} that is the ID of the species
     *                                      the calls are retrieved for.
     * @param geneIds                       A {@code Collection} of {@code String}s that are
     *                                      the IDs of the genes to retrieve the calls of.
     * @param condParamCombination          The combination of condition parameters the calls
     *                                      are propagated over.
     * @param summaryCallTypeQualityFilter  The summary call types and minimum qualities
     *                                      to retrieve, {@code null} to retrieve them all.
     * @param dataTypes                     The {@code DataType}s to consider, {@code null}
     *                                      to consider them all.
     * @return                              The {@code ExpressionCallFilter2} to propagate with.
     */
    private static ExpressionCallFilter2 buildCallFilter(Integer speciesId,
            Collection<String> geneIds, Set<ConditionParameter<?, ?>> condParamCombination,
            Map<SummaryCallType.ExpressionSummary, SummaryQuality> summaryCallTypeQualityFilter,
            EnumSet<DataType> dataTypes) {
        log.traceEntry("{}, {}, {}, {}, {}", speciesId, geneIds, condParamCombination,
                summaryCallTypeQualityFilter, dataTypes);
        return log.traceExit(new ExpressionCallFilter2(summaryCallTypeQualityFilter,
                new GeneFilter(speciesId, geneIds), null, dataTypes, condParamCombination,
                //Only the calls observed in the requested conditions are reported, as they were
                //by the files generated from the precomputed calls. The observation is assessed
                //over the very combination of condition parameters the calls are propagated over.
                summaryCallTypeQualityFilter == null? null: condParamCombination,
                summaryCallTypeQualityFilter == null? null: true,
                //Redundant ancestor calls are kept: these files are an exhaustive export,
                //the calls to display are selected by their consumers, not here.
                false));
    }

    /**
     * Propagate the calls of a batch of genes, reusing the parts of the processed filter that
     * do not depend on the genes.
     *
     * @param condPart          The condition part of the processed filter, computed once
     *                          for the species.
     * @param invariablePart    The invariable part of the processed filter, computed once.
     * @return                  The propagated calls, by {@code Gene}.
     * @see #buildCallFilter(Integer, Collection, Set, Map, EnumSet)
     */
    private static Map<Gene, List<OTFExpressionCall>> loadCalls(ExpressionCallService callService,
            Integer speciesId, Collection<String> geneIds,
            Set<ConditionParameter<?, ?>> condParamCombination,
            Map<SummaryCallType.ExpressionSummary, SummaryQuality> summaryCallTypeQualityFilter,
            EnumSet<DataType> dataTypes, ExpressionCallProcessedFilterConditionPart condPart,
            ExpressionCallProcessedFilterInvariablePart invariablePart) {
        log.traceEntry("{}, {}, {}, {}, {}, {}, {}, {}", callService, speciesId, geneIds,
                condParamCombination, summaryCallTypeQualityFilter, dataTypes, condPart,
                invariablePart);
        ExpressionCallProcessedFilter processedFilter = callService.processExpressionCallFilter(
                buildCallFilter(speciesId, geneIds, condParamCombination,
                        summaryCallTypeQualityFilter, dataTypes),
                null, condPart, invariablePart);
        ExpressionCallLoader loader = callService.getCallLoader(processedFilter);
        return log.traceExit(loader.loadDataOnTheFly());
    }

    /**
     * @param call  An {@code OTFExpressionCall} to check.
     * @return      {@code true} if the condition of {@code call} uses a meta stage, or no stage
     *              at all - the combination of condition parameters aggregating the data of all
     *              the stages carries none, and its calls are all reported.
     */
    private static boolean isMetaStageCall(OTFExpressionCall call) {
        log.traceEntry("{}", call);
        String stageId = call.getCondition().getConditionParameterId(ConditionParameter.DEV_STAGE);
        return log.traceExit(StringUtils.isBlank(stageId) ||
                stageId.startsWith(META_STAGE_ID_PREFIX));
    }

    /**
     * @param cond  A {@code Condition2} to read.
     * @return      An {@code AnatEntity[]} of two elements: the anatomical entity of
     *              {@code cond} first, then its cell type, {@code null} when the condition
     *              targets no specific cell type.
     */
    private static AnatEntity[] anatEntityAndCellType(Condition2 cond) {
        log.traceEntry("{}", cond);
        ComposedEntity<AnatEntity> anatEntityCellType =
                cond.getConditionParameterValue(ConditionParameter.ANAT_ENTITY_CELL_TYPE);
        if (anatEntityCellType == null || anatEntityCellType.isEmpty()) {
            throw log.throwing(new IllegalStateException(
                    "A condition must always have an anat. entity: " + cond));
        }
        if (anatEntityCellType.size() == 1) {
            //Only one entity: the anat. entity, the condition targets no specific cell type
            return log.traceExit(new AnatEntity[] {anatEntityCellType.getEntity(0), null});
        }
        if (anatEntityCellType.size() == 2) {
            //The cell type comes first in the composition, the anat. entity second
            return log.traceExit(new AnatEntity[] {anatEntityCellType.getEntity(1),
                    anatEntityCellType.getEntity(0)});
        }
        throw log.throwing(new IllegalStateException("Unexpected number of entities composing "
                + "the anat. entity and cell type: " + anatEntityCellType));
    }

    /**
     * @return  The name of the developmental stage of {@code cond}, {@code null} if it has none.
     */
    private static String devStageName(Condition2 cond) {
        log.traceEntry("{}", cond);
        ComposedEntity<DevStage> devStage =
                cond.getConditionParameterValue(ConditionParameter.DEV_STAGE);
        if (devStage == null || devStage.isEmpty() || devStage.getEntity(0) == null) {
            return log.traceExit((String) null);
        }
        return log.traceExit(devStage.getEntity(0).getName());
    }

    /**
     * @return  The summary call type and quality of {@code call}, inferred with the very
     *          thresholds the calls were filtered with by the propagation.
     */
    private static Entry<ExpressionSummary, SummaryQuality> inferCallTypeAndQuality(
            OTFExpressionCall call, ExpressionCallProcessedFilter processedFilter) {
        log.traceEntry("{}, {}", call, processedFilter);
        return log.traceExit(OTFExpressionCallFilterEngine.inferSummaryCallTypeAndQuality(call,
                processedFilter.getPresentHighThreshold(),
                processedFilter.getPresentLowThreshold(),
                processedFilter.getAbsentLowThreshold(),
                processedFilter.getAbsentHighThreshold()));
    }

    /**
     * Generate the rows of the calls of one gene and write them into each requested file,
     * with the columns of its file type.
     *
     * @param writersUsed       A {@code Map} where keys are {@code SingleSpExprFileType2}s
     *                          corresponding to which type of file should be generated, the
     *                          associated values being {@code ICsvDozerBeanWriter}s
     *                          corresponding to the writers.
     * @param processors        A {@code Map} where keys are {@code SingleSpExprFileType2}s
     *                          corresponding to which type of file should be generated, the
     *                          associated values being an {@code Array} of
     *                          {@code CellProcessor}s used to process a file.
     * @param headers           A {@code Map} where keys are {@code SingleSpExprFileType2}s
     *                          corresponding to which type of file should be generated, the
     *                          associated values being an {@code Array} of {@code String}s used
     *                          to produce the header.
     * @param calls             The {@code OTFExpressionCall}s of one gene, in the conditions
     *                          to report, propagated over all the requested data types.
     * @param callsByDataType   For each {@code DataType}, the call propagated over that data type
     *                          alone, by {@code Condition2}: the complete file reports each data
     *                          type separately. Empty when no complete file is requested, and
     *                          holding no call for the conditions where a data type has no data.
     * @param processedFilter   The {@code ExpressionCallProcessedFilter} the calls were retrieved
     *                          with, holding the p-value thresholds to infer their summary call
     *                          type and quality with.
     * @throws UncheckedIOException If an error occurred while trying to write the file.
     */
    private void writeRows(Map<SingleSpExprFileType2, ICsvDozerBeanWriter> writersUsed,
            Map<SingleSpExprFileType2, CellProcessor[]> processors,
            Map<SingleSpExprFileType2, String[]> headers,
            Collection<OTFExpressionCall> calls,
            Map<DataType, Map<Condition2, OTFExpressionCall>> callsByDataType,
            ExpressionCallProcessedFilter processedFilter) throws UncheckedIOException {
        log.traceEntry("{}, {}, {}, {}, {}, {}", writersUsed, processors, headers, calls,
                callsByDataType, processedFilter);

        for (Entry<SingleSpExprFileType2, ICsvDozerBeanWriter> writerFileType : writersUsed.entrySet()) {
            List<SingleSpeciesExprFileBean> exprFileBeans = new ArrayList<>();
            for (OTFExpressionCall call: calls) {
                Condition2 cond = call.getCondition();
                String geneId = call.getGene().getGeneId();
                String geneName = call.getGene().getName() == null? "": call.getGene().getName();
                AnatEntity[] anatEntityCellType = anatEntityAndCellType(cond);
                String anatEntityId = anatEntityCellType[0].getId();
                String anatEntityName = anatEntityCellType[0].getName();
                AnatEntity cellType = anatEntityCellType[1];
                // manage post composition of anat entity and cell type.
                // use an intersect symbol to separate the 2 values
                if (cellType != null && !cellType.getId().equals(ConditionDAO.CELL_TYPE_ROOT_ID)) {
                    anatEntityId = cellType.getId() + " \u2229 " + anatEntityId;
                    anatEntityName = cellType.getName() + " in " + anatEntityName;
                }
                String devStageId = cond.getConditionParameterId(ConditionParameter.DEV_STAGE);
                String devStageName = devStageName(cond);
                String sex = cond.getConditionParameterId(ConditionParameter.SEX);
                String strain = cond.getConditionParameterId(ConditionParameter.STRAIN);
                Entry<ExpressionSummary, SummaryQuality> callTypeQuality =
                        inferCallTypeAndQuality(call, processedFilter);
                if (callTypeQuality == null) {
                    throw log.throwing(new IllegalStateException("No summary call type for a call "
                            + "returned by the propagation, it should have been filtered out: "
                            + call));
                }
                String summaryCallType = convertExpressionSummaryToString(callTypeQuality.getKey());
                String summaryQuality = convertSummaryQualityToString(callTypeQuality.getValue());
                String expressionScore = call.getExpressionScore() == null? NA_VALUE:
                    call.getExpressionScore().toPlainString();
                String fdr = call.getAllDataTypePValue() == null? NA_VALUE:
                    call.getFormattedAllDatatypePValue();
                //Only the calls observed in the requested conditions are retrieved
                //(see buildCallFilter), so the propagation of every call of these files
                //includes observed data.
                Boolean includingObservedData = Boolean.TRUE.equals(
                        call.getDataPropagation().isIncludingObservedData());

                if (writerFileType.getKey().isSimpleFileType()) {
                    SingleSpeciesSimpleExprFileBean simpleBean = new SingleSpeciesSimpleExprFileBean(
                        geneId, geneName, anatEntityId, anatEntityName, devStageId, devStageName,
                        sex, strain, summaryCallType, summaryQuality, expressionScore, fdr);
                    exprFileBeans.add(simpleBean);
                } else {
                    List<DataExprCounts> counts = EnumSet.allOf(DataType.class).stream()
                            .map(dt -> getDataExprCounts(dt,
                                    callsByDataType.getOrDefault(dt, Map.of()).get(cond),
                                    processedFilter))
                            .collect(Collectors.toList());

                    SingleSpeciesCompleteExprFileBean completeBean = new SingleSpeciesCompleteExprFileBean(
                            geneId, geneName, anatEntityId, anatEntityName, devStageId, devStageName,
                            sex, strain, summaryCallType, summaryQuality, expressionScore,
                            fdr, convertObservedDataToString(includingObservedData), counts);
                    exprFileBeans.add(completeBean);
                }
            }
            syncWriteRows(exprFileBeans, writerFileType, processors);
        }
    }

    synchronized private void syncWriteRows(List<SingleSpeciesExprFileBean> exprFilebeans,
            Entry<SingleSpExprFileType2, ICsvDozerBeanWriter> writerFileType,
            Map<SingleSpExprFileType2, CellProcessor[]> processors) {
        try {
            for(SingleSpeciesExprFileBean exprFileBean : exprFilebeans)
            writerFileType.getValue().write(exprFileBean, processors.get(writerFileType.getKey()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Get the {@code DataExprCounts} of one {@code DataType}, from the call propagated over
     * that data type alone.
     *
     * @param dataType          A {@code DataType} that is the data type to report.
     * @param dataTypeCall      The {@code OTFExpressionCall} propagated over {@code dataType}
     *                          alone, in the condition of the row being written. {@code null}
     *                          when that data type produced no call in that condition.
     * @param processedFilter   The {@code ExpressionCallProcessedFilter} the calls were retrieved
     *                          with, holding the p-value thresholds.
     * @return                  A {@code DataExprCounts} that is what {@code dataType} reports
     *                          in that condition.
     */
    private DataExprCounts getDataExprCounts(DataType dataType, OTFExpressionCall dataTypeCall,
            ExpressionCallProcessedFilter processedFilter) {
        log.traceEntry("{}, {}, {}", dataType, dataTypeCall, processedFilter);

        if (dataTypeCall == null) {
            return log.traceExit(new DataExprCounts(dataType, NO_DATA_VALUE, NA_VALUE, NA_VALUE,
                    convertObservedDataToString(false), NA_VALUE, NA_VALUE));
        }
        //Can be null: a data type not trusted for absent calls produces no p-value over
        //the trusted data types, and a non-significant call in a condition with no observed data
        //for that data type is no valid absent call either.
        Entry<ExpressionSummary, SummaryQuality> callTypeQuality =
                inferCallTypeAndQuality(dataTypeCall, processedFilter);
        return log.traceExit(new DataExprCounts(dataType,
                callTypeQuality == null? NO_DATA_VALUE:
                    convertExpressionSummaryToString(callTypeQuality.getKey()),
                callTypeQuality == null? NA_VALUE:
                    convertSummaryQualityToString(callTypeQuality.getValue()),
                dataTypeCall.getAllDataTypePValue() == null? NA_VALUE:
                    dataTypeCall.getFormattedAllDatatypePValue(),
                convertObservedDataToString(Boolean.TRUE.equals(
                        dataTypeCall.getDataPropagation().isIncludingObservedData())),
                dataTypeCall.getExpressionScore() == null? NA_VALUE:
                    dataTypeCall.getExpressionScore().toPlainString(),
                dataTypeCall.getExpressionScoreWeight() == null? NA_VALUE:
                    dataTypeCall.getExpressionScoreWeight().toPlainString()));
    }

    /**
     * Class parent of bean storing simple-species expression calls,
     * holding parameters common to all of them.
     *
     * @author  Valentine Rech de Laval
     * @version Bgee 15, May. 2021
     * @since   Bgee 13
     */
    public static abstract class SingleSpeciesExprFileBean {

        private String geneId;
        private String geneName;
        private String anatEntityId;
        private String anatEntityName;
        private String devStageId;
        private String devStageName;
        private String sex;
        private String strain;
        private String expression;
        private String callQuality;
        private String expressionScore;
        private String fdr;

        /**
         * 0-argument constructor of the bean.
         */
        protected SingleSpeciesExprFileBean() {
        }

        /**
         * Constructor providing all arguments of the class.
         *
         * @param call  An {@code ExpressionCall} that is the call to store.
         */
        protected SingleSpeciesExprFileBean(String geneId, String geneName,
                String anatEntityId, String anatEntityName, String devStageId, String devStageName,
                String sex, String strain, String expression,
                String callQuality, String expressionScore, String fdr) {
            this.geneId = geneId;
            this.geneName = geneName;
            this.anatEntityId = anatEntityId;
            this.anatEntityName = anatEntityName;
            this.devStageId = devStageId;
            this.devStageName = devStageName;
            this.sex = sex;
            this.strain = strain;
            this.devStageName = devStageName;
            this.expression = expression;
            this.callQuality = callQuality;
            this.expressionScore = expressionScore;
            this.fdr = fdr;
        }

        public String getGeneId() {
            return geneId;
        }
        public String getGeneName() {
            return geneName;
        }
        public String getAnatEntityId() {
            return anatEntityId;
        }
        public String getAnatEntityName() {
            return anatEntityName;
        }
        public String getDevStageId() {
            return devStageId;
        }
        public String getDevStageName() {
            return devStageName;
        }
        public String getSex() {
            return sex;
        }
        public String getStrain() {
            return strain;
        }
        public String getExpression() {
            return expression;
        }
        public String getCallQuality() {
            return callQuality;
        }
        public String getExpressionScore() {
            return expressionScore;
        }
        public String getFdr() {
            return fdr;
        }
        public void setGeneId(String geneId) {
            this.geneId = geneId;
        }
        public void setGeneName(String geneName) {
            this.geneName = geneName;
        }
        public void setAnatEntityId(String anatEntityId) {
            this.anatEntityId = anatEntityId;
        }
        public void setAnatEntityName(String anatEntityName) {
            this.anatEntityName = anatEntityName;
        }
        public void setDevStageId(String devStageId) {
            this.devStageId = devStageId;
        }
        public void setDevStageName(String devStageName) {
            this.devStageName = devStageName;
        }
        public void setSex(String sex) {
            this.sex = sex;
        }
        public void setStrain(String strain) {
            this.strain = strain;
        }
        public void setExpression(String expression) {
            this.expression = expression;
        }
        public void setCallQuality(String callQuality) {
            this.callQuality = callQuality;
        }
        public void setExpressionScore(String expressionScore) {
            this.expressionScore = expressionScore;
        }
        public void setFdr(String fdr) {
            this.fdr = fdr;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((geneId == null) ? 0 : geneId.hashCode());
            result = prime * result + ((geneName == null) ? 0 : geneName.hashCode());
            result = prime * result + ((anatEntityId == null) ? 0 : anatEntityId.hashCode());
            result = prime * result + ((anatEntityName == null) ? 0 : anatEntityName.hashCode());
            result = prime * result + ((devStageId == null) ? 0 : devStageId.hashCode());
            result = prime * result + ((devStageName == null) ? 0 : devStageName.hashCode());
            result = prime * result + ((sex == null) ? 0 : sex.hashCode());
            result = prime * result + ((strain == null) ? 0 : strain.hashCode());
            result = prime * result + ((expression == null) ? 0 : expression.hashCode());
            result = prime * result + ((callQuality == null) ? 0 : callQuality.hashCode());
            result = prime * result + ((expressionScore == null) ? 0 : expressionScore.hashCode());
            result = prime * result + ((fdr == null) ? 0 : fdr.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!super.equals(obj))
                return false;
            if (getClass() != obj.getClass())
                return false;
            SingleSpeciesExprFileBean other = (SingleSpeciesExprFileBean) obj;
            if (geneId == null) {
                if (other.geneId != null)
                    return false;
            } else if (!geneId.equals(other.geneId))
                return false;
            if (geneName == null) {
                if (other.geneName != null)
                    return false;
            } else if (!geneName.equals(other.geneName))
                return false;
            if (anatEntityId == null) {
                if (other.anatEntityId != null)
                    return false;
            } else if (!anatEntityId.equals(other.anatEntityId))
                return false;
            if (anatEntityName == null) {
                if (other.anatEntityName != null)
                    return false;
            } else if (!anatEntityName.equals(other.anatEntityName))
                return false;
            if (devStageId == null) {
                if (other.devStageId != null)
                    return false;
            } else if (!devStageId.equals(other.devStageId))
                return false;
            if (devStageName == null) {
                if (other.devStageName != null)
                    return false;
            } else if (!devStageName.equals(other.devStageName))
                return false;
            if (sex == null) {
                if (other.sex != null)
                    return false;
            } else if (!sex.equals(other.sex))
                return false;
            if (strain == null) {
                if (other.strain != null)
                    return false;
            } else if (!strain.equals(other.strain))
                return false;
            if (expression == null) {
                if (other.expression != null)
                    return false;
            } else if (!expression.equals(other.expression))
                return false;
            if (callQuality == null) {
                if (other.callQuality != null)
                    return false;
            } else if (!callQuality.equals(other.callQuality))
                return false;
            if (expressionScore == null) {
                if (other.expressionScore != null)
                    return false;
            } else if (!expressionScore.equals(other.expressionScore))
                return false;
            if (fdr == null) {
                if (other.fdr != null)
                    return false;
            } else if (!fdr.equals(other.fdr))
                return false;
            return true;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("SingleSpeciesExprFileBean [geneId=").append(geneId)
                .append(", geneName=").append(geneName).append(", anatEntityId=").append(anatEntityId)
                .append(", anatEntityName=").append(anatEntityName)
                .append(", devStageId=").append(devStageId).append(", devStageName=").append(devStageName)
                .append(", sex=").append(sex).append(", strain=").append(strain)
                .append(", expression=").append(expression).append(", callQuality=").append(callQuality)
                .append(", expressionScore=").append(expressionScore)
                .append(", fdr=").append(fdr).append("]");
            return builder.toString();
        }
    }

    /**
     * A bean representing a row of a single-species simple expression file. 
     * 
     * @author  Valentine Rech de Laval
     * @version Bgee 15, May. 2021
     * @since   Bgee 13, Sept. 2016
     */
    public static class SingleSpeciesSimpleExprFileBean extends SingleSpeciesExprFileBean {

        /**
         * 0-argument constructor of the bean.
         */
        protected SingleSpeciesSimpleExprFileBean() {
        }

        /**
         * Constructor providing all arguments of the class.
         *
         * @param call  An {@code ExpressionCall} that is the call to store.
         */
        protected SingleSpeciesSimpleExprFileBean(String geneId, String geneName,
                String anatEntityId, String anatEntityName, String devStageId, String devStageName,
                String sex, String strain, String expression, String callQuality,
                String expressionScore, String fdr) {
            super(geneId, geneName, anatEntityId, anatEntityName, devStageId, devStageName, sex,
                    strain, expression, callQuality, expressionScore, fdr);
        }
    }

    /**
     * A bean representing a row of a single-species complete expression file. 
     *
     * @author  Valentine Rech de Laval
     * @version Bgee 15, May. 2021
     * @since   Bgee 13, Sept. 2016
     */
    public static class SingleSpeciesCompleteExprFileBean extends SingleSpeciesExprFileBean {

        private String includingObservedData;
        /**
         * See {@link #getDataExprCounts()}.
         */
        private List<DataExprCounts> dataExprCounts;

        /**
         * 0-argument constructor of the bean.
         */
        protected SingleSpeciesCompleteExprFileBean() {
        }

        /**
         * Constructor providing all arguments of the class.
         *
         * @param call  An {@code ExpressionCall} that is the call to store.
         */
        protected SingleSpeciesCompleteExprFileBean(String geneId, String geneName,
                String anatEntityId, String anatEntityName, String devStageId, String devStageName,
                String sex, String strain, String expression, String callQuality,
                String expressionScore, String fdr, String includingObservedData,
                List<DataExprCounts> dataExprCounts) {
            super(geneId, geneName, anatEntityId, anatEntityName, devStageId, devStageName, sex, strain,
                    expression, callQuality, expressionScore, fdr);
            this.includingObservedData = includingObservedData;
            this.dataExprCounts = Collections.unmodifiableList(dataExprCounts == null ?
                    new ArrayList<>() : new ArrayList<>(dataExprCounts));
        }

        public String getIncludingObservedData() {
            return includingObservedData;
        }
        public void setIncludingObservedData(String includingObservedData) {
            this.includingObservedData = includingObservedData;
        }
        public List<DataExprCounts> getDataExprCounts() {
            return dataExprCounts;
        }
        public void setDataExprCounts(List<DataExprCounts> dataExprCounts) {
            this.dataExprCounts = dataExprCounts;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((dataExprCounts == null) ? 0 : dataExprCounts.hashCode());
            result = prime * result + ((includingObservedData == null) ? 0 : includingObservedData.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!super.equals(obj)) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            SingleSpeciesCompleteExprFileBean other = (SingleSpeciesCompleteExprFileBean) obj;
            if (dataExprCounts == null) {
                if (other.dataExprCounts != null) {
                    return false;
                }
            } else if (!dataExprCounts.equals(other.dataExprCounts)) {
                return false;
            }
            if (includingObservedData == null) {
                if (other.includingObservedData != null) {
                    return false;
                }
            } else if (!includingObservedData.equals(other.includingObservedData)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("SingleSpeciesCompleteExprFileBean [includingObservedData=").append(includingObservedData)
                    .append(", dataExprCounts=").append(dataExprCounts).append("]");
            return builder.toString();
        }
    }

    /**
     * Class used to store expression data by data types.
     *
     * @author  Valentine Rech de Laval
     * @author Frederic Bastian
     * @version Bgee 15, May. 2021
     * @since   Bgee 14, Mar. 2017
     */
    public static class DataExprCounts {

        private DataType dataType;
        private String callType;
        private String callQuality;
        private String fdr;
        private String observedData;
        private String expressionScore;
        private String weight;

        public DataExprCounts(DataType dataType, String callType, String callQuality, String fdr,
                String observedData, String expressionScore, String weight) {
            this.dataType = dataType;
            this.callType = callType;
            this.callQuality = callQuality;
            this.fdr= fdr;
            this.observedData = observedData;
            this.expressionScore = expressionScore;
            this.weight = weight;
        }

        public DataType getDataType() {
            return dataType;
        }
        public void setDataType(DataType dataType) {
            this.dataType = dataType;
        }
        public String getCallType() {
            return callType;
        }
        public void setCallType(String callType) {
            this.callType = callType;
        }
        public String getCallQuality() {
            return callQuality;
        }
        public void setCallQuality(String callQuality) {
            this.callQuality = callQuality;
        }
        public String getFdr() {
            return fdr;
        }
        public void setFdr(String fdr) {
            this.fdr = fdr;
        }
        public String getObservedData() {
            return observedData;
        }
        public void setObservedData(String observedData) {
            this.observedData = observedData;
        }
        public String getExpressionScore() {
            return expressionScore;
        }
        public void setExpressionScore(String expressionScore) {
            this.expressionScore = expressionScore;
        }
        public String getWeight() {
            return weight;
        }
        public void setWeight(String weight) {
            this.weight = weight;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((callType == null) ? 0 : callType.hashCode());
            result = prime * result + ((callQuality == null) ? 0 : callQuality.hashCode());
            result = prime * result + ((dataType == null) ? 0 : dataType.hashCode());
            result = prime * result + ((expressionScore == null) ? 0 : expressionScore.hashCode());
            result = prime * result + ((fdr == null) ? 0 : fdr.hashCode());
            result = prime * result + ((observedData == null) ? 0 : observedData.hashCode());
            result = prime * result + ((weight == null) ? 0 : weight.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            DataExprCounts other = (DataExprCounts) obj;
            if (callType == null) {
                if (other.callType != null) {
                    return false;
                }
            } else if (!callType.equals(other.callType)) {
                return false;
            }
            if (callQuality == null) {
                if (other.callQuality != null) {
                    return false;
                }
            } else if (!callQuality.equals(other.callQuality)) {
                return false;
            }
            if (fdr == null) {
                if (other.fdr != null) {
                    return false;
                }
            } else if (!fdr.equals(other.fdr)) {
                return false;
            }
            if (dataType != other.dataType) {
                return false;
            }
            if (expressionScore == null) {
                if (other.expressionScore != null) {
                    return false;
                }
            } else if (!expressionScore.equals(other.expressionScore)) {
                return false;
            }
            if (observedData == null) {
                if (other.observedData != null) {
                    return false;
                }
            } else if (!observedData.equals(other.observedData)) {
                return false;
            }
            if (weight == null) {
                if (other.weight != null) {
                    return false;
                }
            } else if (!weight.equals(other.weight)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("DataExprCounts [dataType=").append(dataType)
                   .append(", callType=").append(callType)
                   .append(", callQuality=").append(callQuality)
                   .append(", fdr=").append(fdr)
                   .append(", observedData=").append(observedData)
                   .append(", expressionScore=").append(expressionScore)
                   .append(", weight=").append(weight).append("]");
            return builder.toString();
        }
    }
}