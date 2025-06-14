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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
import org.bgee.model.expressiondata.call.Call.ExpressionCall;
import org.bgee.model.expressiondata.call.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.call.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.call.CallService;
import org.bgee.model.expressiondata.call.CallService.Attribute;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.file.SpeciesDownloadFile.Category;
import org.bgee.model.gene.GeneFilter;
import org.bgee.pipeline.CommandRunner;
import org.bgee.pipeline.Utils;
import org.supercsv.cellprocessor.constraint.IsElementOf;
import org.supercsv.cellprocessor.constraint.LMinMax;
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
     * <li>a list of condition parameters that will be used to generate files. 
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

        // If no parameters are given by user, we set all file types
        if (this.params == null || this.params.isEmpty()) {
            this.params = EnumSet.allOf(Attribute.class).stream()
                    .filter(a -> a.isConditionParameter())
                    .collect(Collectors.toSet());
        } else if (this.params.stream().noneMatch(p -> p.isConditionParameter()) ||
                this.params.stream().anyMatch(p -> !p.isConditionParameter())) {
            throw log.throwing(new IllegalArgumentException(
                    "Some non-parametric attributes or no parametric attributes are provided"));
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

        // We retrieve calls with all attributes that are not condition parameters.
        Set<Attribute> attributes = Arrays.stream(Attribute.values())
                .filter(a -> !a.isConditionParameter())
                //we also don't want the qualitative expression levels
                .filter(a -> !a.equals(Attribute.ANAT_ENTITY_QUAL_EXPR_LEVEL) &&
                        !a.equals(Attribute.GENE_QUAL_EXPR_LEVEL))
                .collect(Collectors.toSet());

        // generate ordering attributes
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering =
                new LinkedHashMap<>();
        serviceOrdering.put(CallService.OrderingAttribute.GENE_ID, Service.Direction.ASC);

        // generate condition call filter
        EnumSet<CallService.Attribute> callsCondParameters = EnumSet.noneOf(CallService.Attribute.class);

        // update attributes, ordering attributes and observed data filter to add condition
        // parameters depending on this.param
        if (this.params.contains(CallService.Attribute.ANAT_ENTITY_ID)) {
            attributes.add(CallService.Attribute.ANAT_ENTITY_ID);
            attributes.add(CallService.Attribute.CELL_TYPE_ID);
            callsCondParameters.addAll(ExpressionCallFilter.ANAT_ENTITY_OBSERVED_DATA_ARGUMENT
                    .keySet().iterator().next());
            serviceOrdering.put(CallService.OrderingAttribute.ANAT_ENTITY_ID, Service.Direction.ASC);
            serviceOrdering.put(CallService.OrderingAttribute.CELL_TYPE_ID, Service.Direction.ASC);
        }
        if (this.params.contains(CallService.Attribute.DEV_STAGE_ID)) {
            attributes.add(CallService.Attribute.DEV_STAGE_ID);
            serviceOrdering.put(CallService.OrderingAttribute.DEV_STAGE_ID, Service.Direction.ASC);
            callsCondParameters.add(CallService.Attribute.DEV_STAGE_ID);
        }
        if (this.params.contains(CallService.Attribute.SEX_ID)) {
            attributes.add(CallService.Attribute.SEX_ID);
            serviceOrdering.put(CallService.OrderingAttribute.SEX_ID, Service.Direction.ASC);
            callsCondParameters.add(CallService.Attribute.SEX_ID);
        }
        if (this.params.contains(CallService.Attribute.STRAIN_ID)) {
            attributes.add(CallService.Attribute.STRAIN_ID);
            serviceOrdering.put(CallService.OrderingAttribute.STRAIN_ID, Service.Direction.ASC);
            callsCondParameters.add(CallService.Attribute.STRAIN_ID);
        }
        // The callObservedDataFilter is used to filter only observed data (if the Boolean associated
        // to the combination of condition parameter is true) or only non observed data (if the Boolean associated
        // to the combination of condition parameter is false). If the Boolean associated
        // to the combination of condition parameter is false please remember that observed data
        // are not retrieved. If all data (both observed and non observed) have to be retrieved
        // then the filter itself has to be null :
        //      Map<EnumSet<CallService.Attribute>, Boolean> callObservedDataFilter = null;
        Map<EnumSet<CallService.Attribute>, Boolean> callObservedDataFilter = new HashMap<>();
        callObservedDataFilter.put(callsCondParameters, true);

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

            //Load expression data by chunk of genes using several threads

            AtomicInteger index = new AtomicInteger(0);
            serviceFactory.getGeneService().loadGenes(new GeneFilter(speciesId))
            .map(g -> g.getGeneId())
            .collect(Collectors.groupingBy(x -> index.getAndIncrement() / genesChunk))
            .entrySet().parallelStream().forEach(g -> {
                //init thread safe service factory
                ServiceFactory threadServiceFactory = serviceFactorySupplier.get();
                ExpressionCallFilter callFilter = new ExpressionCallFilter(
                        summaryCallTypeQualityFilter,
                        Collections.singleton(new GeneFilter(speciesId,g.getValue())), null, null,
                        callObservedDataFilter);

                Set<ExpressionCall> calls = threadServiceFactory.getCallService()
                        .loadExpressionCalls(callFilter, attributes, null)
                        .filter(c-> !nonInformativeAnatEntities.contains(c.getCondition()
                                .getAnatEntityId()))
                        .collect(Collectors.toSet());
                this.writeRows(writersUsed, processors, headers, callsCondParameters, calls);

            });

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
                case EXPRESSION_RANK_COLUMN_NAME:
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
                if (header[i].equals(AFFYMETRIX_DATA_COLUMN_NAME) ||
                        header[i].equals(EST_DATA_COLUMN_NAME) ||
                        header[i].equals(IN_SITU_DATA_COLUMN_NAME) ||
                        header[i].equals(RNASEQ_DATA_COLUMN_NAME) ||
                        header[i].equals(SC_RNA_SEQ_DATA_COLUMN_NAME)) {
                    processors[i] = new IsElementOf(expressionSummaries);
                } else if (header[i].equals(AFFYMETRIX_QUAL_COLUMN_NAME) ||
                        header[i].equals(EST_QUAL_COLUMN_NAME) ||
                        header[i].equals(IN_SITU_QUAL_COLUMN_NAME) ||
                        header[i].equals(RNASEQ_QUAL_COLUMN_NAME) ||
                        header[i].equals(SC_RNA_SEQ_QUAL_COLUMN_NAME)) {
                    processors[i] = new IsElementOf(qualitySummaries);
                } else if (header[i].equals(AFFYMETRIX_SELF_OBSERVATION_COUNT_COLUMN_NAME) ||
                        header[i].equals(AFFYMETRIX_DESCENDANT_OBSERVATION_COUNT_COLUMN_NAME) ||
                        header[i].equals(EST_SELF_OBSERVATION_COUNT_COLUMN_NAME) ||
                        header[i].equals(EST_DESCENDANT_OBSERVATION_COUNT_COLUMN_NAME) ||
                        header[i].equals(IN_SITU_SELF_OBSERVATION_COUNT_COLUMN_NAME) ||
                        header[i].equals(IN_SITU_DESCENDANT_OBSERVATION_COUNT_COLUMN_NAME) ||
                        header[i].equals(RNASEQ_SELF_OBSERVATION_COUNT_COLUMN_NAME) ||
                        header[i].equals(RNASEQ_DESCENDANT_OBSERVATION_COUNT_COLUMN_NAME) ||
                        header[i].equals(SC_RNA_SEQ_SELF_OBSERVATION_COUNT_COLUMN_NAME) ||
                        header[i].equals(SC_RNA_SEQ_DESCENDANT_OBSERVATION_COUNT_COLUMN_NAME) ||
                        header[i].equals(SELF_OBSERVATION_COUNT_COLUMN_NAME) ||
                        header[i].equals(DESCENDANT_OBSERVATION_COUNT_COLUMN_NAME)) {
                    processors[i] = new LMinMax(0, Long.MAX_VALUE);
                } else if (header[i].equals(AFFYMETRIX_OBSERVED_DATA_COLUMN_NAME) ||
                        header[i].equals(EST_OBSERVED_DATA_COLUMN_NAME) ||
                        header[i].equals(IN_SITU_OBSERVED_DATA_COLUMN_NAME) ||
                        header[i].equals(RNASEQ_OBSERVED_DATA_COLUMN_NAME) ||
                        header[i].equals(SC_RNA_SEQ_OBSERVED_DATA_COLUMN_NAME) ||
                        header[i].equals(INCLUDING_OBSERVED_DATA_COLUMN_NAME)) {
                    processors[i] = new IsElementOf(originValues);
                } else if (header[i].equals(AFFYMETRIX_EXPRESSION_SCORE_COLUMN_NAME) ||
                        header[i].equals(AFFYMETRIX_EXPRESSION_RANK_COLUMN_NAME) ||
                        header[i].equals(AFFYMETRIX_WEIGHT_COLUMN_NAME) ||
                        header[i].equals(AFFYMETRIX_FDR_COLUMN_NAME) ||
                        header[i].equals(EST_EXPRESSION_SCORE_COLUMN_NAME) ||
                        header[i].equals(EST_EXPRESSION_RANK_COLUMN_NAME) ||
                        header[i].equals(EST_WEIGHT_COLUMN_NAME) ||
                        header[i].equals(EST_FDR_COLUMN_NAME) ||
                        header[i].equals(IN_SITU_EXPRESSION_SCORE_COLUMN_NAME) ||
                        header[i].equals(IN_SITU_EXPRESSION_RANK_COLUMN_NAME) ||
                        header[i].equals(IN_SITU_WEIGHT_COLUMN_NAME) ||
                        header[i].equals(IN_SITU_FDR_COLUMN_NAME) ||
                        header[i].equals(RNASEQ_EXPRESSION_SCORE_COLUMN_NAME) ||
                        header[i].equals(RNASEQ_EXPRESSION_RANK_COLUMN_NAME) ||
                        header[i].equals(RNASEQ_WEIGHT_COLUMN_NAME) ||
                        header[i].equals(RNASEQ_FDR_COLUMN_NAME) ||
                        header[i].equals(SC_RNA_SEQ_EXPRESSION_SCORE_COLUMN_NAME) ||
                        header[i].equals(SC_RNA_SEQ_EXPRESSION_RANK_COLUMN_NAME) ||
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

        String[] headers = null;
        int nbColumns = 7;
        if (!fileType.isSimpleFileType()) {
            nbColumns = 55;
        }
        for (Attribute attr : this.params) {
            switch (attr) {
            // *** attributes common to all file types ***
            case ANAT_ENTITY_ID:
            case DEV_STAGE_ID:
                nbColumns += 2;
                break;
            case SEX_ID:
            case STRAIN_ID:
                nbColumns++;
                break;
            default:
                throw log.throwing(new IllegalArgumentException("[" + attr +"] is not a valid "
                        + "condition parameter"));
            }
        }
        headers = new String[nbColumns];

        // We use an index to avoid to change hard-coded column numbers when we change columns
        int idx = 0;
        // *** Headers common to all file types ***
        headers[idx++] = GENE_ID_COLUMN_NAME;
        headers[idx++] = GENE_NAME_COLUMN_NAME;
        if (this.params.contains(CallService.Attribute.ANAT_ENTITY_ID)) {
            headers[idx++] = ANAT_ENTITY_ID_COLUMN_NAME;
            headers[idx++] = ANAT_ENTITY_NAME_COLUMN_NAME;
        }
        if (this.params.contains(CallService.Attribute.DEV_STAGE_ID)) {
            headers[idx++] = STAGE_ID_COLUMN_NAME;
            headers[idx++] = STAGE_NAME_COLUMN_NAME;
        }
        if (this.params.contains(CallService.Attribute.SEX_ID)) {
            headers[idx++] = SEX_COLUMN_NAME;
        }
        if (this.params.contains(CallService.Attribute.STRAIN_ID)) {
            headers[idx++] = STRAIN_COLUMN_NAME;
        }
        headers[idx++] = EXPRESSION_COLUMN_NAME;
        headers[idx++] = QUALITY_COLUMN_NAME;
        headers[idx++] = FDR_COLUMN_NAME;
        headers[idx++] = EXPRESSION_SCORE_COLUMN_NAME;
        headers[idx++] = EXPRESSION_RANK_COLUMN_NAME;
        if (!fileType.isSimpleFileType()) {
            // *** Headers specific to complete file ***
            headers[idx++] = INCLUDING_OBSERVED_DATA_COLUMN_NAME;
            headers[idx++] = SELF_OBSERVATION_COUNT_COLUMN_NAME;
            headers[idx++] = DESCENDANT_OBSERVATION_COUNT_COLUMN_NAME;
            headers[idx++] = AFFYMETRIX_DATA_COLUMN_NAME;
            headers[idx++] = AFFYMETRIX_QUAL_COLUMN_NAME;
            headers[idx++] = AFFYMETRIX_FDR_COLUMN_NAME;
            headers[idx++] = AFFYMETRIX_EXPRESSION_SCORE_COLUMN_NAME;
            headers[idx++] = AFFYMETRIX_EXPRESSION_RANK_COLUMN_NAME;
            headers[idx++] = AFFYMETRIX_WEIGHT_COLUMN_NAME;
            headers[idx++] = AFFYMETRIX_OBSERVED_DATA_COLUMN_NAME;
            headers[idx++] = AFFYMETRIX_SELF_OBSERVATION_COUNT_COLUMN_NAME;
            headers[idx++] = AFFYMETRIX_DESCENDANT_OBSERVATION_COUNT_COLUMN_NAME;
            headers[idx++] = EST_DATA_COLUMN_NAME;
            headers[idx++] = EST_QUAL_COLUMN_NAME;
            headers[idx++] = EST_FDR_COLUMN_NAME;
            headers[idx++] = EST_EXPRESSION_SCORE_COLUMN_NAME;
            headers[idx++] = EST_EXPRESSION_RANK_COLUMN_NAME;
            headers[idx++] = EST_WEIGHT_COLUMN_NAME;
            headers[idx++] = EST_OBSERVED_DATA_COLUMN_NAME;
            headers[idx++] = EST_SELF_OBSERVATION_COUNT_COLUMN_NAME;
            headers[idx++] = EST_DESCENDANT_OBSERVATION_COUNT_COLUMN_NAME;
            headers[idx++] = IN_SITU_DATA_COLUMN_NAME;
            headers[idx++] = IN_SITU_QUAL_COLUMN_NAME;
            headers[idx++] = IN_SITU_FDR_COLUMN_NAME;
            headers[idx++] = IN_SITU_EXPRESSION_SCORE_COLUMN_NAME;
            headers[idx++] = IN_SITU_EXPRESSION_RANK_COLUMN_NAME;
            headers[idx++] = IN_SITU_WEIGHT_COLUMN_NAME;
            headers[idx++] = IN_SITU_OBSERVED_DATA_COLUMN_NAME;
            headers[idx++] = IN_SITU_SELF_OBSERVATION_COUNT_COLUMN_NAME;
            headers[idx++] = IN_SITU_DESCENDANT_OBSERVATION_COUNT_COLUMN_NAME;
            headers[idx++] = RNASEQ_DATA_COLUMN_NAME;
            headers[idx++] = RNASEQ_QUAL_COLUMN_NAME;
            headers[idx++] = RNASEQ_FDR_COLUMN_NAME;
            headers[idx++] = RNASEQ_EXPRESSION_SCORE_COLUMN_NAME;
            headers[idx++] = RNASEQ_EXPRESSION_RANK_COLUMN_NAME;
            headers[idx++] = RNASEQ_WEIGHT_COLUMN_NAME;
            headers[idx++] = RNASEQ_OBSERVED_DATA_COLUMN_NAME;
            headers[idx++] = RNASEQ_SELF_OBSERVATION_COUNT_COLUMN_NAME;
            headers[idx++] = RNASEQ_DESCENDANT_OBSERVATION_COUNT_COLUMN_NAME;
            headers[idx++] = SC_RNA_SEQ_DATA_COLUMN_NAME;
            headers[idx++] = SC_RNA_SEQ_QUAL_COLUMN_NAME;
            headers[idx++] = SC_RNA_SEQ_FDR_COLUMN_NAME;
            headers[idx++] = SC_RNA_SEQ_EXPRESSION_SCORE_COLUMN_NAME;
            headers[idx++] = SC_RNA_SEQ_EXPRESSION_RANK_COLUMN_NAME;
            headers[idx++] = SC_RNA_SEQ_WEIGHT_COLUMN_NAME;
            headers[idx++] = SC_RNA_SEQ_OBSERVED_DATA_COLUMN_NAME;
            headers[idx++] = SC_RNA_SEQ_SELF_OBSERVATION_COUNT_COLUMN_NAME;
            headers[idx++] = SC_RNA_SEQ_DESCENDANT_OBSERVATION_COUNT_COLUMN_NAME;
        }
        return log.traceExit(headers);
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
                case EXPRESSION_RANK_COLUMN_NAME:
                    mapping[i] = "expressionRank";
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

                if (header[i].equals(SELF_OBSERVATION_COUNT_COLUMN_NAME)) {
                    mapping[i] = "selfObservationCount";
                }

                if (header[i].equals(DESCENDANT_OBSERVATION_COUNT_COLUMN_NAME)) {
                    mapping[i] = "descendantObservationCOunt";
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

                } else if (header[i].endsWith(EXPRESSION_RANK_COLUMN_NAME_SUFFIX)) {
                    mapping[i] = "dataExprCounts[" + index + "].expressionRank";

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

                } else if (header[i].startsWith(SELF_OBSERVATION_COUNT_PREFIX)) {
                    mapping[i] = "dataExprCounts[" + index + "].selfObservationCount";

                } else if (header[i].startsWith(DESCENDANT_OBSERVATION_COUNT_PREFIX)) {
                    mapping[i] = "dataExprCounts[" + index + "].descendantObservationCount";

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
                    headers[i].equals(EXPRESSION_RANK_COLUMN_NAME) ||
                    headers[i].equals(EXPRESSION_SCORE_COLUMN_NAME) ||
                    headers[i].equals(INCLUDING_OBSERVED_DATA_COLUMN_NAME) ||
                    headers[i].equals(AFFYMETRIX_DATA_COLUMN_NAME) ||
                    headers[i].equals(AFFYMETRIX_QUAL_COLUMN_NAME) ||
                    headers[i].equals(AFFYMETRIX_FDR_COLUMN_NAME) ||
                    headers[i].equals(AFFYMETRIX_EXPRESSION_RANK_COLUMN_NAME) ||
                    headers[i].equals(AFFYMETRIX_EXPRESSION_SCORE_COLUMN_NAME) ||
                    headers[i].equals(AFFYMETRIX_WEIGHT_COLUMN_NAME) ||
                    headers[i].equals(AFFYMETRIX_OBSERVED_DATA_COLUMN_NAME) ||
                    headers[i].equals(AFFYMETRIX_SELF_OBSERVATION_COUNT_COLUMN_NAME) ||
                    headers[i].equals(AFFYMETRIX_DESCENDANT_OBSERVATION_COUNT_COLUMN_NAME) ||

                    headers[i].equals(EST_DATA_COLUMN_NAME) ||
                    headers[i].equals(EST_QUAL_COLUMN_NAME) ||
                    headers[i].equals(EST_FDR_COLUMN_NAME) ||
                    headers[i].equals(EST_EXPRESSION_RANK_COLUMN_NAME) ||
                    headers[i].equals(EST_EXPRESSION_SCORE_COLUMN_NAME) ||
                    headers[i].equals(EST_WEIGHT_COLUMN_NAME) ||
                    headers[i].equals(EST_OBSERVED_DATA_COLUMN_NAME) ||
                    headers[i].equals(EST_SELF_OBSERVATION_COUNT_COLUMN_NAME) ||
                    headers[i].equals(EST_DESCENDANT_OBSERVATION_COUNT_COLUMN_NAME) ||

                    headers[i].equals(IN_SITU_DATA_COLUMN_NAME) ||
                    headers[i].equals(IN_SITU_QUAL_COLUMN_NAME) ||
                    headers[i].equals(IN_SITU_FDR_COLUMN_NAME) ||
                    headers[i].equals(IN_SITU_EXPRESSION_RANK_COLUMN_NAME) ||
                    headers[i].equals(IN_SITU_EXPRESSION_SCORE_COLUMN_NAME) ||
                    headers[i].equals(IN_SITU_WEIGHT_COLUMN_NAME) ||
                    headers[i].equals(IN_SITU_OBSERVED_DATA_COLUMN_NAME) ||
                    headers[i].equals(IN_SITU_SELF_OBSERVATION_COUNT_COLUMN_NAME) ||
                    headers[i].equals(IN_SITU_DESCENDANT_OBSERVATION_COUNT_COLUMN_NAME) ||

                    headers[i].equals(RNASEQ_DATA_COLUMN_NAME) ||
                    headers[i].equals(RNASEQ_QUAL_COLUMN_NAME) ||
                    headers[i].equals(RNASEQ_FDR_COLUMN_NAME) ||
                    headers[i].equals(RNASEQ_EXPRESSION_RANK_COLUMN_NAME) ||
                    headers[i].equals(RNASEQ_EXPRESSION_SCORE_COLUMN_NAME) ||
                    headers[i].equals(RNASEQ_WEIGHT_COLUMN_NAME) ||
                    headers[i].equals(RNASEQ_OBSERVED_DATA_COLUMN_NAME) ||
                    headers[i].equals(RNASEQ_SELF_OBSERVATION_COUNT_COLUMN_NAME) ||
                    headers[i].equals(RNASEQ_DESCENDANT_OBSERVATION_COUNT_COLUMN_NAME) ||

                    headers[i].equals(SC_RNA_SEQ_DATA_COLUMN_NAME) ||
                    headers[i].equals(SC_RNA_SEQ_QUAL_COLUMN_NAME) ||
                    headers[i].equals(SC_RNA_SEQ_FDR_COLUMN_NAME) ||
                    headers[i].equals(SC_RNA_SEQ_EXPRESSION_RANK_COLUMN_NAME) ||
                    headers[i].equals(SC_RNA_SEQ_EXPRESSION_SCORE_COLUMN_NAME) ||
                    headers[i].equals(SC_RNA_SEQ_WEIGHT_COLUMN_NAME) ||
                    headers[i].equals(SC_RNA_SEQ_OBSERVED_DATA_COLUMN_NAME) ||
                    headers[i].equals(SC_RNA_SEQ_SELF_OBSERVATION_COUNT_COLUMN_NAME) ||
                    headers[i].equals(SC_RNA_SEQ_DESCENDANT_OBSERVATION_COUNT_COLUMN_NAME) ||

                    headers[i].equals(SELF_OBSERVATION_COUNT_COLUMN_NAME) ||
                    headers[i].equals(DESCENDANT_OBSERVATION_COUNT_COLUMN_NAME) ||
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
     * Generate rows to be written and write them in a file. This methods will notably use
     * {@code ExpressionCall}s to produce information, that is different depending on {@code fileType}.
     * <p>
     * {@code ExpressionCall}s must all have the same values returned by {@link ExpressionCall#getGene()}, 
     * and {@link ExpressionCall#getCondition()}.
     * <p>
     * Information that will be generated is provided in the given {@code processors}.
     * 
     * @param writersUsed           A {@code Map} where keys are {@code SingleSpExprFileType2}s
     *                              corresponding to which type of file should be generated, the 
     *                              associated values being {@code ICsvDozerBeanWriter}s
     *                              corresponding to the writers.
     * @param processors            A {@code Map} where keys are {@code SingleSpExprFileType2}s 
     *                              corresponding to which type of file should be generated, the 
     *                              associated values being an {@code Array} of 
     *                              {@code CellProcessor}s used to process a file.
     * @param headers               A {@code Map} where keys are {@code SingleSpExprFileType2}s 
     *                              corresponding to which type of file should be generated, the 
     *                              associated values being an {@code Array} of {@code String}s used 
     *                              to produce the header.
     * @param condParamCombination  An {@code EnumSet} of {@code CallService.Attribute}s that are
     *                              condition parameters representing the targeted combination.
     * @param calls                 A {@code Collection} of {@code ExpressionCall}s that are expression
     *                              calls to be written into files.
     * @return                      An {@code int} that is the addition of number of rows added to files.
     * @throws UncheckedIOException If an error occurred while trying to write the {@code outputFile}.
     */
    private void writeRows(Map<SingleSpExprFileType2, ICsvDozerBeanWriter> writersUsed,
            Map<SingleSpExprFileType2, CellProcessor[]> processors,
            Map<SingleSpExprFileType2, String[]> headers,
            EnumSet<CallService.Attribute> condParamCombination,
            Collection<ExpressionCall> calls) throws UncheckedIOException {
        log.traceEntry("{}, {}, {}, {}, {}", writersUsed, processors, headers, condParamCombination, calls);

        for (Entry<SingleSpExprFileType2, ICsvDozerBeanWriter> writerFileType : writersUsed.entrySet()) {
            List<SingleSpeciesExprFileBean> exprFileBeans = new ArrayList<>();
            for(ExpressionCall call :calls) {
                String geneId = call.getGene().getGeneId();
                String geneName = call.getGene().getName() == null? "": call.getGene().getName();
                String anatEntityId = call.getCondition().getAnatEntityId();
                String anatEntityName = call.getCondition().getAnatEntity() == null? null:
                    call.getCondition().getAnatEntity().getName();
                String devStageId = call.getCondition().getDevStageId();
                String devStageName = call.getCondition().getDevStage() == null ? null:
                        call.getCondition().getDevStage().getName();
                String cellTypeId = call.getCondition().getCellTypeId();
                String cellTypeName = call.getCondition().getCellType() == null ? null:
                        call.getCondition().getCellType().getName();
                // manage post composition of anat entity and cell type.
                // use an intersect symbol to separate the 2 values
                if(!cellTypeId.equals(ConditionDAO.CELL_TYPE_ROOT_ID)) {
                    anatEntityId = cellTypeId + " \u2229 " + anatEntityId;
                    anatEntityName = cellTypeName + " in " + anatEntityName;
                }
                String sex = call.getCondition().getSexId();
                String strain = call.getCondition().getStrainId();
                String summaryCallType = convertExpressionSummaryToString(call.getSummaryCallType());
                String summaryQuality = convertSummaryQualityToString(call.getSummaryQuality());
                String expressionRank = call.getMeanRank() == null ? NA_VALUE :
                        call.getFormattedMeanRank();
                String expressionScore = call.getExpressionScore() == null ? NA_VALUE :
                    call.getFormattedExpressionScore();
                String fdr = call.getPValueWithEqualDataTypes(Arrays.asList(DataType.values()))
                        .getPValue().toString();
                //For Bgee 15.0 calls files only contain observed calls
                Boolean includingObservedData = true; //call.getCallData().stream()
//                        .map(ExpressionCallData::getSelfObservationCount).reduce(0, Integer::sum) > 0 ? true : false;

                if (writerFileType.getKey().isSimpleFileType() && Boolean.TRUE.equals(includingObservedData)) {
                    SingleSpeciesSimpleExprFileBean simpleBean = new SingleSpeciesSimpleExprFileBean(
                        geneId, geneName, anatEntityId, anatEntityName, devStageId, devStageName,
                        sex, strain, summaryCallType, summaryQuality, expressionRank, expressionScore, fdr);
                    exprFileBeans.add(simpleBean);
                } else if (!writerFileType.getKey().isSimpleFileType()) {
                    List<DataExprCounts> counts = EnumSet.allOf(DataType.class).stream()
                            .map(dt -> getDataExprCountByDataType(call, dt, condParamCombination))
                            .collect(Collectors.toList());

                    Long selfObservationCount = Long.valueOf(call.getDataPropagation()
                            .getSelfObservationCount(condParamCombination));
                    Long descendantObservationCount = Long.valueOf(call.getDataPropagation()
                            .getDescendantObservationCount(condParamCombination));

                    SingleSpeciesCompleteExprFileBean completeBean = new SingleSpeciesCompleteExprFileBean(
                            geneId, geneName, anatEntityId, anatEntityName, devStageId, devStageName,
                            sex, strain, summaryCallType, summaryQuality, expressionRank, expressionScore,
                            fdr, convertObservedDataToString(includingObservedData), selfObservationCount,
                            descendantObservationCount, counts);
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
     * Get {@code DataExprCounts}s of a {@code DataType} from an {@code ExpressionCall}.
     * 
     * @param call      An {@code ExpressionCall} that is the expression call for which retrieve counts.
     * @param dataType  A {@code DataType} that is the data type allowing to filter counts to retrieve.
     * @param condParamCombination  An {@code EnumSet} of {@code CallService.Attribute}s that are
     *                              condition parameters representing the targeted combination.
     * @return          A {@code DataExprCounts} that is the counts from {@code call} for {@code dataType}.
     */
    private DataExprCounts getDataExprCountByDataType(ExpressionCall call, DataType dataType,
            EnumSet<CallService.Attribute> condParamCombination) {
        log.traceEntry("{}, {}, {}", call, dataType, condParamCombination);

        ExpressionCall callFromDataType = CallService.deriveCallForDataType(call, dataType);

        if (callFromDataType != null) {
            assert callFromDataType.getCallData().size() == 1;
            ExpressionCallData data = callFromDataType.getCallData().iterator().next();
            assert dataType.equals(data.getDataType());

            return log.traceExit(new DataExprCounts(dataType,
                    convertExpressionSummaryToString(callFromDataType.getSummaryCallType()),
                    convertSummaryQualityToString(callFromDataType.getSummaryQuality()),
                    callFromDataType.getFirstPValue() == null ? NA_VALUE : callFromDataType.getFirstPValue()
                            .getPValue().toString(),
                    convertObservedDataToString(true),
                    Long.valueOf(callFromDataType.getDataPropagation()
                            .getSelfObservationCount(condParamCombination)),
                    Long.valueOf(callFromDataType.getDataPropagation()
                            .getDescendantObservationCount(condParamCombination)),
                    callFromDataType.getMeanRank() == null ? NA_VALUE : callFromDataType.getFormattedMeanRank(),
                    callFromDataType.getExpressionScore() == null ? NA_VALUE : callFromDataType.getFormattedExpressionScore(),
                    data.getWeightForMeanRank() == null ? NA_VALUE : data.getWeightForMeanRank().toPlainString()));
        }
        return log.traceExit(new DataExprCounts(dataType, NO_DATA_VALUE, NA_VALUE, NA_VALUE,
                    convertObservedDataToString(false), 0L, 0L, NA_VALUE, NA_VALUE, NA_VALUE));
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
        private String expressionRank;
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
                String callQuality, String expressionRank, String expressionScore, String fdr) {
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
            this.expressionRank = expressionRank;
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
        public String getExpressionRank() {
            return expressionRank;
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
        public void setExpressionRank(String expressionRank) {
            this.expressionRank = expressionRank;
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
            result = prime * result + ((expressionRank == null) ? 0 : expressionRank.hashCode());
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
            if (expressionRank == null) {
                if (other.expressionRank != null)
                    return false;
            } else if (!expressionRank.equals(other.expressionRank))
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
                .append(", expressionRank=").append(expressionRank)
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
                String expressionRank, String expressionScore, String fdr) {
            super(geneId, geneName, anatEntityId, anatEntityName, devStageId, devStageName, sex,
                    strain, expression, callQuality, expressionRank, expressionScore, fdr);
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
        private Long selfObservationCount;
        private Long descendantObservationCount;
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
                String sex, String strain, String expression, String callQuality, String expressionRank,
                String expressionScore, String fdr, String includingObservedData,
                Long selfObservationCount, Long descendantObservationCount,
                List<DataExprCounts> dataExprCounts) {
            super(geneId, geneName, anatEntityId, anatEntityName, devStageId, devStageName, sex, strain,
                    expression, callQuality, expressionRank, expressionScore, fdr);
            this.includingObservedData = includingObservedData;
            this.selfObservationCount = selfObservationCount;
            this.descendantObservationCount = descendantObservationCount;
            this.dataExprCounts = Collections.unmodifiableList(dataExprCounts == null ?
                    new ArrayList<>() : new ArrayList<>(dataExprCounts));
        }

        public String getIncludingObservedData() {
            return includingObservedData;
        }
        public void setIncludingObservedData(String includingObservedData) {
            this.includingObservedData = includingObservedData;
        }
        public Long getSelfObservationCount() {
            return selfObservationCount;
        }
        public void setSelfObservationCount(Long selfObservationCount) {
            this.selfObservationCount = selfObservationCount;
        }
        public Long getDescendantObservationCount() {
            return descendantObservationCount;
        }
        public void setDescendantObservationCount(Long descendantObservationCount) {
            this.descendantObservationCount = descendantObservationCount;
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
            result = prime * result + ((selfObservationCount == null) ? 0 : selfObservationCount.hashCode());
            result = prime * result + ((descendantObservationCount == null) ? 0 : descendantObservationCount.hashCode());
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
            if (selfObservationCount == null) {
                if (other.selfObservationCount != null) {
                    return false;
                }
            } else if (!selfObservationCount.equals(other.selfObservationCount)) {
                return false;
            }
            if (descendantObservationCount == null) {
                if (other.descendantObservationCount != null) {
                    return false;
                }
            } else if (!descendantObservationCount.equals(other.descendantObservationCount)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("SingleSpeciesCompleteExprFileBean [includingObservedData=").append(includingObservedData)
                    .append(", selfObservationCount=").append(selfObservationCount).append("]")
                    .append(", descendantObservationCount=").append(descendantObservationCount).append("]")
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
        private Long selfObservationCount;
        private Long descendantObservationCount;
        private String expressionRank;
        private String expressionScore;
        private String weight;

        public DataExprCounts(DataType dataType, String callType, String callQuality, String fdr,
                String observedData, Long selfObservationCount, Long descendantObservationCount,
                String expressionRank, String expressionScore, String weight) {
            this.dataType = dataType;
            this.callType = callType;
            this.callQuality = callQuality;
            this.fdr= fdr;
            this.observedData = observedData;
            this.selfObservationCount = selfObservationCount;
            this.descendantObservationCount = descendantObservationCount;
            this.expressionRank = expressionRank;
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
        public Long getSelfObservationCount() {
            return selfObservationCount;
        }
        public void setSelfObservationCount(Long selfObservationCount) {
            this.selfObservationCount = selfObservationCount;
        }
        public Long getDescendantObservationCount() {
            return descendantObservationCount;
        }
        public void setDescendantObservationCount(Long descendantObservationCount) {
            this.descendantObservationCount = descendantObservationCount;
        }
        public String getExpressionRank() {
            return expressionRank;
        }
        public void setExpressionRank(String expressionRank) {
            this.expressionRank = expressionRank;
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
            result = prime * result + ((descendantObservationCount == null) ? 0 :
                descendantObservationCount.hashCode());
            result = prime * result + ((expressionRank == null) ? 0 : expressionRank.hashCode());
            result = prime * result + ((expressionScore == null) ? 0 : expressionScore.hashCode());
            result = prime * result + ((fdr == null) ? 0 : fdr.hashCode());
            result = prime * result + ((observedData == null) ? 0 : observedData.hashCode());
            result = prime * result + ((selfObservationCount == null) ? 0 : selfObservationCount.hashCode());
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
            if (expressionRank == null) {
                if (other.expressionRank != null) {
                    return false;
                }
            } else if (!expressionRank.equals(other.expressionRank)) {
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
            if (selfObservationCount == null) {
                if (other.selfObservationCount != null) {
                    return false;
                }
            } else if (!selfObservationCount.equals(other.selfObservationCount)) {
                return false;
            }
            if (descendantObservationCount == null) {
                if (other.descendantObservationCount != null) {
                    return false;
                }
            } else if (!descendantObservationCount.equals(other.descendantObservationCount)) {
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
                   .append(", selfObservationCount=").append(selfObservationCount)
                   .append(", descendantObservationCount=").append(descendantObservationCount)
                   .append(", expressionRank=").append(expressionRank)
                   .append(", expressionScore=").append(expressionScore)
                   .append(", weight=").append(weight).append("]");
            return builder.toString();
        }
    }
}