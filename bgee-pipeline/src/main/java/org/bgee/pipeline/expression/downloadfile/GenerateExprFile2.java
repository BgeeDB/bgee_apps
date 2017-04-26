package org.bgee.pipeline.expression.downloadfile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.CallService.Attribute;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.CallType.Expression;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.ExperimentExpressionCount;
import org.bgee.model.expressiondata.baseelements.PropagationState;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.file.DownloadFile.CategoryEnum;
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
 * @version Bgee 14, Mar. 2017
 * @since   Bgee 13, Sept. 2017
 */
public class GenerateExprFile2 extends GenerateDownloadFile {

    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(GenerateExprFile2.class.getName());

    // We sort data types by name lengths from the longest to the shortest.
    /**
     * A {@code List} of {@code DataType}s used to define order of data type columns in files to be generated.
     **/
    private final static List<DataType> DATA_TYPE_ORDER = 
            Arrays.asList(DataType.AFFYMETRIX, DataType.EST, DataType.IN_SITU, DataType.RNA_SEQ);

    private final static LinkedHashMap<CallService.OrderingAttribute, Service.Direction> ORDERING_ATTRIBUTES;

    static {
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = 
                new LinkedHashMap<>();
        serviceOrdering.put(CallService.OrderingAttribute.GENE_ID, Service.Direction.ASC);
        serviceOrdering.put(CallService.OrderingAttribute.ANAT_ENTITY_ID, Service.Direction.ASC);
        serviceOrdering.put(CallService.OrderingAttribute.DEV_STAGE_ID, Service.Direction.ASC);
        
        ORDERING_ATTRIBUTES = serviceOrdering;
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
        EXPR_SIMPLE(CategoryEnum.EXPR_CALLS_SIMPLE, true),
        EXPR_COMPLETE(CategoryEnum.EXPR_CALLS_COMPLETE, false);

        /**
         * A {@code CategoryEnum} that is the category of files of this type.
         */
        private final CategoryEnum category;

        /**
         * A {@code boolean} defining whether this {@code ExprFileType} is a simple file
         * type
         */
        private final boolean simpleFileType;

        /**
         * Constructor providing the {@code CategoryEnum} of this {@code ExprFileType},
         * and a {@code boolean} defining whether this {@code ExprFileType} is a simple file type.
         */
        private SingleSpExprFileType2(CategoryEnum category, boolean simpleFileType) {
            this.category = category;
            this.simpleFileType = simpleFileType;
        }

        @Override
        public String getStringRepresentation() {
            return this.category.getStringRepresentation();
        }
        @Override
        public CategoryEnum getCategory() {
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
     * {@link SingleSpExprFileType2#EXPR_SIMPLE}, and 'expr-complete' for 
     * {@link SingleSpExprFileType2#EXPR_COMPLETE}), separated by the {@code String} 
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
    public static void main(String[] args) throws IllegalArgumentException, IOException {
        log.entry((Object[]) args);

        int expectedArgLength = 3;
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
            GenerateExprFile2.convertToAttributes(CommandRunner.parseListArgument(args[3])));
        generator.generateExprFiles();

        log.exit();
    }
    
    private static Set<Attribute> convertToAttributes(List<String> argumentList) {
        log.entry(argumentList);
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
        
        return log.exit(attrs);
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
     * Default constructor.
     */
    // suppress warning as this default constructor should not be used.
    @SuppressWarnings("unused")
    private GenerateExprFile2() {
        this(null, null, null, null);
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
     * @throws IllegalArgumentException If {@code directory} is {@code null} or blank.
     */
    public GenerateExprFile2(List<Integer> speciesIds, Set<SingleSpExprFileType2> fileTypes, 
            String directory, Set<Attribute> attributes) throws IllegalArgumentException {
        this(null, speciesIds, fileTypes, directory, attributes);
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
     * @throws IllegalArgumentException If {@code directory} is {@code null} or blank.
     */
    public GenerateExprFile2(MySQLDAOManager manager, List<Integer> speciesIds,
        Set<SingleSpExprFileType2> fileTypes, String directory, Set<Attribute> attributes)
                throws IllegalArgumentException {
        this(manager, speciesIds, fileTypes, directory, attributes, ServiceFactory::new);
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
     * @throws IllegalArgumentException If {@code directory} is {@code null} or blank.
     */
    public GenerateExprFile2(MySQLDAOManager manager, List<Integer> speciesIds,
        Set<SingleSpExprFileType2> fileTypes, String directory, Set<Attribute> attributes,
        Supplier<ServiceFactory> serviceFactorySupplier) throws IllegalArgumentException {
        super(manager, speciesIds, fileTypes, directory);
        this.serviceFactorySupplier = serviceFactorySupplier;
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
    //TODO: add OMA node ID in complete files
    public void generateExprFiles() throws UncheckedIOException {
        log.entry();

        Set<Integer> setSpecies = Collections.unmodifiableSet(this.speciesIds == null?
                new HashSet<>() : new HashSet<>(this.speciesIds));

        // Check user input, retrieve info for generating file names
        // Retrieve species names and IDs (all species names if speciesIds is null or empty)
        // FIXME use supplier?
        Map<Integer, String> speciesNamesForFilesByIds = this.checkAndGetLatinNamesBySpeciesIds(
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
        speciesNamesForFilesByIds.keySet().parallelStream().forEach(speciesId -> {
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
        });

        log.exit();
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
        log.entry(fileNamePrefix, speciesId);

        log.debug("Start generating expression files for the species {}, file types {}, and parameters {}...", 
                speciesId, this.fileTypes, this.params);

        //********************************
        // RETRIEVE DATA FROM DATA SOURCE
        //********************************
        log.trace("Start retrieving data for expression files for the species {}...", speciesId);

        ServiceFactory serviceFactory = this.serviceFactorySupplier.get();

        final Set<Integer> speciesFilter = Collections.singleton(speciesId);

        // Load non-informative anatomical entities: 
        // calls occurring in these anatomical entities, and generated from 
        // data propagation only (no observed data in them), will be discarded.
        // FIXME filter by non informative anat. entities in ExpressionCallFilter instead of filter stream 
        Set<String> nonInformativeAnatEntities = serviceFactory.getAnatEntityService()
                .loadNonInformativeAnatEntitiesBySpeciesIds(speciesFilter)
                .map(AnatEntity::getId)
                .collect(Collectors.toSet());

        ExpressionCallFilter callFilter = new ExpressionCallFilter(null,
                Collections.singleton(new GeneFilter(speciesId)), null, null, true, false, false);

        // We retrieve calls with all non-parametric attributes plus provided params.
        Set<Attribute> clnAttr = Arrays.stream(Attribute.values())
                .filter(a -> !a.isConditionParameter())
                .collect(Collectors.toSet());
        clnAttr.addAll(this.params);

        Stream<ExpressionCall> calls = serviceFactory.getCallService().loadExpressionCalls(
                callFilter, clnAttr, ORDERING_ATTRIBUTES)
                .filter(c-> !nonInformativeAnatEntities.contains(c.getCondition().getAnatEntityId()));

        log.trace("Done retrieving data for expression files for the species {}.", speciesId);

        //****************************
        // PRODUCE AND WRITE DATA
        //****************************
        log.trace("Start generating and writing file content for species {} and file types {}...",
            speciesId, this.fileTypes);

        // Now, we write all requested expression files at once. This way, we will generate the data
        // only once, and we will not have to store them in memory (the memory usage could be huge).
        
        // OK, first we allow to store file names, writers, etc, associated to a FileType, 
        // for the catch and finally clauses. 
        Map<FileType, String> generatedFileNames = new HashMap<>();

        // We will write results in temporary files that we will rename at the end
        // if everything is correct
        String tmpExtension = ".tmp";

        // In order to close all writers in a finally clause
        Map<SingleSpExprFileType2, ICsvDozerBeanWriter> writersUsed = new HashMap<>();
        int numberOfRows = 0;
        try {
            //**************************
            // OPEN FILES, CREATE WRITERS, WRITE HEADERS
            //**************************
            Map<SingleSpExprFileType2, CellProcessor[]> processors = new HashMap<>();
            Map<SingleSpExprFileType2, String[]> headers = new HashMap<>();

            for (FileType fileType : this.fileTypes) {
                SingleSpExprFileType2 currentFileType = (SingleSpExprFileType2) fileType;
                String[] fileTypeHeaders = this.generateExprFileHeader(currentFileType);
                headers.put(currentFileType, fileTypeHeaders);

                CellProcessor[] fileTypeProcessors = this.generateExprFileCellProcessors(
                        currentFileType, fileTypeHeaders);
                processors.put(currentFileType, fileTypeProcessors);

                // Create file name
                String fileName = this.formatString(fileNamePrefix + "_" +
                        currentFileType.getStringRepresentation() + "_" +
                        this.convertAttributeToFileName(this.params) + EXTENSION);
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
            numberOfRows = this.writeRows(writersUsed, processors, headers, calls);
        } catch (Exception e) {
            this.deleteTempFiles(generatedFileNames, tmpExtension);
            throw e;
        } finally {
            for (ICsvDozerBeanWriter writer : writersUsed.values()) {
                writer.close();
            }
        }
        // now, if everything went fine, we rename or delete the temporary files
        if (numberOfRows > 0) {
            this.renameTempFiles(generatedFileNames, tmpExtension);            
        } else {
            this.deleteTempFiles(generatedFileNames, tmpExtension);
        }

        log.exit();
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
        log.entry(attributes);
        
        assert attributes!= null && !attributes.isEmpty();
        
        List<Attribute> attributeList = new ArrayList<>(attributes);
        Collections.sort(attributeList);

        return log.exit(attributes.stream()
                .map(a -> {
                    switch (a) {
                        case ANAT_ENTITY_ID:
                            return "organ";
                        case DEV_STAGE_ID:
                            return "stage";
                        default: 
                            throw new IllegalArgumentException("Attribute not supported: " + a);
                    }})
                .collect(Collectors.joining("_")));
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
        log.entry(fileType, header);

        List<Object> expressionSummaries = new ArrayList<Object>();
        for (ExpressionSummary sum : ExpressionSummary.values()) {
            expressionSummaries.add(convertExpressionSummaryToString(sum));
        }
        
        List<Object> expressions = new ArrayList<Object>();
        for (Expression expr : Expression.values()) {
            expressions.add(convertExpressionToString(expr));
        }
        expressions.add(GenerateDownloadFile.NO_DATA_VALUE);

        List<Object> qualitySummaries = new ArrayList<Object>();
        qualitySummaries.add(convertSummaryQualityToString(SummaryQuality.GOLD));
        qualitySummaries.add(convertSummaryQualityToString(SummaryQuality.SILVER));
        qualitySummaries.add(convertSummaryQualityToString(SummaryQuality.BRONZE));
        
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
                    // XXX: it's a String to be able to write values such as '3.32e4' and NA_VALUE.
                    // It can be a Long if we don't want exponential values 
                    // and all rows have a score and a rank.
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
            	switch (header[i]) {
            	    case AFFYMETRIX_DATA_COLUMN_NAME:
            	    case EST_DATA_COLUMN_NAME:
            	    case IN_SITU_DATA_COLUMN_NAME:
            	    case RNASEQ_DATA_COLUMN_NAME:
            	        processors[i] = new IsElementOf(expressions);
            	        break;
            	    case AFFYMETRIX_PRESENT_HIGH_COUNT_COLUMN_NAME:
            	    case AFFYMETRIX_PRESENT_LOW_COUNT_COLUMN_NAME:
            	    case AFFYMETRIX_ABSENT_HIGH_COUNT_COLUMN_NAME:
            	    case AFFYMETRIX_ABSENT_LOW_COUNT_COLUMN_NAME:
                    case EST_PRESENT_HIGH_COUNT_COLUMN_NAME:
                    case EST_PRESENT_LOW_COUNT_COLUMN_NAME:
                    case IN_SITU_PRESENT_HIGH_COUNT_COLUMN_NAME:
                    case IN_SITU_PRESENT_LOW_COUNT_COLUMN_NAME:
                    case IN_SITU_ABSENT_HIGH_COUNT_COLUMN_NAME:
                    case IN_SITU_ABSENT_LOW_COUNT_COLUMN_NAME:
                    case RNASEQ_PRESENT_HIGH_COUNT_COLUMN_NAME:
                    case RNASEQ_PRESENT_LOW_COUNT_COLUMN_NAME:
                    case RNASEQ_ABSENT_HIGH_COUNT_COLUMN_NAME:
                    case RNASEQ_ABSENT_LOW_COUNT_COLUMN_NAME:
            	        processors[i] = new LMinMax(0, Long.MAX_VALUE);
            	        break;
            	    case AFFYMETRIX_OBSERVED_DATA_COLUMN_NAME:
            	    case EST_OBSERVED_DATA_COLUMN_NAME:
            	    case IN_SITU_OBSERVED_DATA_COLUMN_NAME:
            	    case RNASEQ_OBSERVED_DATA_COLUMN_NAME:
            	    case INCLUDING_OBSERVED_DATA_COLUMN_NAME:
            	        processors[i] = new IsElementOf(originValues);
            	        break;
            	}
            }
            if (processors[i] == null) {
                throw log.throwing(new IllegalArgumentException("Unrecognized header: " 
                        + header[i] + " for file type: " + fileType.getStringRepresentation()));
            }
        }
        return log.exit(processors);
    }

    /**
     * Generates an {@code Array} of {@code String}s used to generate the header of an
     * expression TSV file of type {@code fileType}.
     * 
     * @param fileType  An {@code ExprFileType} of the file to be generated.
     * @return          The {@code Array} of {@code String}s used to produce the header.
     */
    private String[] generateExprFileHeader(SingleSpExprFileType2 fileType) {
        log.entry(fileType);
        
        String[] headers = null; 
        int nbColumns = 10;
        if (!fileType.isSimpleFileType()) {
            nbColumns = 33;
        }
        headers = new String[nbColumns];

        // We use an index to avoid to change hard-coded column numbers when we change columns 
        int idx = 0;
        // *** Headers common to all file types ***
        headers[idx++] = GENE_ID_COLUMN_NAME;
        headers[idx++] = GENE_NAME_COLUMN_NAME;
        headers[idx++] = ANAT_ENTITY_ID_COLUMN_NAME;
        headers[idx++] = ANAT_ENTITY_NAME_COLUMN_NAME;
        headers[idx++] = STAGE_ID_COLUMN_NAME;
        headers[idx++] = STAGE_NAME_COLUMN_NAME;
        headers[idx++] = EXPRESSION_COLUMN_NAME;
        headers[idx++] = QUALITY_COLUMN_NAME;
        headers[idx++] = EXPRESSION_RANK_COLUMN_NAME;
        headers[idx++] = EXPRESSION_SCORE_COLUMN_NAME;
        if (!fileType.isSimpleFileType()) {
            // *** Headers specific to complete file ***
            headers[idx++] = INCLUDING_OBSERVED_DATA_COLUMN_NAME;
            headers[idx++] = AFFYMETRIX_DATA_COLUMN_NAME;
            headers[idx++] = AFFYMETRIX_PRESENT_HIGH_COUNT_COLUMN_NAME;
            headers[idx++] = AFFYMETRIX_PRESENT_LOW_COUNT_COLUMN_NAME;
            headers[idx++] = AFFYMETRIX_ABSENT_HIGH_COUNT_COLUMN_NAME;
            headers[idx++] = AFFYMETRIX_ABSENT_LOW_COUNT_COLUMN_NAME;
            headers[idx++] = AFFYMETRIX_OBSERVED_DATA_COLUMN_NAME;
            headers[idx++] = EST_DATA_COLUMN_NAME;
            headers[idx++] = EST_PRESENT_HIGH_COUNT_COLUMN_NAME;
            headers[idx++] = EST_PRESENT_LOW_COUNT_COLUMN_NAME;
            headers[idx++] = EST_OBSERVED_DATA_COLUMN_NAME;
            headers[idx++] = IN_SITU_DATA_COLUMN_NAME;
            headers[idx++] = IN_SITU_PRESENT_HIGH_COUNT_COLUMN_NAME;
            headers[idx++] = IN_SITU_PRESENT_LOW_COUNT_COLUMN_NAME;
            headers[idx++] = IN_SITU_ABSENT_HIGH_COUNT_COLUMN_NAME;
            headers[idx++] = IN_SITU_ABSENT_LOW_COUNT_COLUMN_NAME;
            headers[idx++] = IN_SITU_OBSERVED_DATA_COLUMN_NAME;
            headers[idx++] = RNASEQ_DATA_COLUMN_NAME;
            headers[idx++] = RNASEQ_PRESENT_HIGH_COUNT_COLUMN_NAME;
            headers[idx++] = RNASEQ_PRESENT_LOW_COUNT_COLUMN_NAME;
            headers[idx++] = RNASEQ_ABSENT_HIGH_COUNT_COLUMN_NAME;
            headers[idx++] = RNASEQ_ABSENT_LOW_COUNT_COLUMN_NAME;
            headers[idx++] = RNASEQ_OBSERVED_DATA_COLUMN_NAME;
        }
        return log.exit(headers);
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
        log.entry(fileType, header);
        
        //to do a sanity check on species columns in simple files
        Set<DataType> dataTypeFound = new HashSet<DataType>();

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
                for (DataType dataType: DATA_TYPE_ORDER) {
                    int index = DATA_TYPE_ORDER.indexOf(dataType);

                    if (header[i].toLowerCase().contains(dataType.getStringRepresentation().toLowerCase())) {
                        dataTypeFound.add(dataType);
                        if (header[i].startsWith(OBSERVED_DATA_COLUMN_NAME_PREFIX) 
                                && header[i].endsWith(OBSERVED_DATA_COLUMN_NAME_SUFFIX)) {
                            mapping[i] = "dataExprCounts[" + index + "].observedData";

                        } else if (header[i].endsWith(PRESENT_HIGH_COUNT_COLUMN_NAME_SUFFIX)) {
                            mapping[i] = "dataExprCounts[" + index + "].presentHighCount";
                            
                        } else if (header[i].endsWith(PRESENT_LOW_COUNT_COLUMN_NAME_SUFFIX)) {
                            mapping[i] = "dataExprCounts[" + index + "].presentLowCount";
                            
                        } else if (header[i].endsWith(ABSENT_HIGH_COUNT_COLUMN_NAME_SUFFIX)) {
                            mapping[i] = "dataExprCounts[" + index + "].absentHighCount";
                            
                        } else if (header[i].endsWith(ABSENT_LOW_COUNT_COLUMN_NAME_SUFFIX)) {
                            mapping[i] = "dataExprCounts[" + index + "].absentLowCount";
                            
                        } else if (header[i].endsWith(CALL_TYPE_COLUMN_NAME_SUFFIX)) {
                            // this should be the last tested because it's the least specific
                            mapping[i] = "dataExprCounts[" + index + "].callType";
                            
                        } else {
                            throw log.throwing(new IllegalArgumentException("Unrecognized header: " 
                                    + header[i] + " for file type: " + 
                                    fileType.getStringRepresentation()));
                        }
                        assert(mapping[i] != null);
                        break;
                    }
                }
            }
            if (mapping[i] == null) {
                throw log.throwing(new IllegalArgumentException("Unrecognized header: " 
                        + header[i] + " for file type: " + fileType.getStringRepresentation()));
            }
        }
        // Verify that we found all data types
        if (!fileType.isSimpleFileType()) {
            assert dataTypeFound.containsAll(DATA_TYPE_ORDER) && 
                DATA_TYPE_ORDER.containsAll(dataTypeFound): 
                "Some of data types were not found in the header: expected: " 
                + DATA_TYPE_ORDER + " - found: " + dataTypeFound;
        }

        return log.exit(mapping);
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
        log.entry((Object[]) headers);
        
        boolean[] quoteMode = new boolean[headers.length];
        for (int i = 0; i < headers.length; i++) {
            switch (headers[i]) {
                case GENE_ID_COLUMN_NAME:
                case ANAT_ENTITY_ID_COLUMN_NAME:
                case STAGE_ID_COLUMN_NAME:
                case EXPRESSION_COLUMN_NAME:
                case QUALITY_COLUMN_NAME:
                case EXPRESSION_RANK_COLUMN_NAME:
                case EXPRESSION_SCORE_COLUMN_NAME:
                case INCLUDING_OBSERVED_DATA_COLUMN_NAME:
                case AFFYMETRIX_DATA_COLUMN_NAME:
                case AFFYMETRIX_PRESENT_HIGH_COUNT_COLUMN_NAME: 
                case AFFYMETRIX_PRESENT_LOW_COUNT_COLUMN_NAME: 
                case AFFYMETRIX_ABSENT_HIGH_COUNT_COLUMN_NAME: 
                case AFFYMETRIX_ABSENT_LOW_COUNT_COLUMN_NAME: 
                case AFFYMETRIX_OBSERVED_DATA_COLUMN_NAME:
                case EST_DATA_COLUMN_NAME:
                case EST_PRESENT_HIGH_COUNT_COLUMN_NAME: 
                case EST_PRESENT_LOW_COUNT_COLUMN_NAME: 
                case EST_OBSERVED_DATA_COLUMN_NAME:
                case IN_SITU_DATA_COLUMN_NAME:
                case IN_SITU_PRESENT_HIGH_COUNT_COLUMN_NAME: 
                case IN_SITU_PRESENT_LOW_COUNT_COLUMN_NAME: 
                case IN_SITU_ABSENT_HIGH_COUNT_COLUMN_NAME: 
                case IN_SITU_ABSENT_LOW_COUNT_COLUMN_NAME: 
                case IN_SITU_OBSERVED_DATA_COLUMN_NAME:
                case RNASEQ_DATA_COLUMN_NAME:
                case RNASEQ_PRESENT_HIGH_COUNT_COLUMN_NAME: 
                case RNASEQ_PRESENT_LOW_COUNT_COLUMN_NAME: 
                case RNASEQ_ABSENT_HIGH_COUNT_COLUMN_NAME: 
                case RNASEQ_ABSENT_LOW_COUNT_COLUMN_NAME: 
                case RNASEQ_OBSERVED_DATA_COLUMN_NAME:
                    quoteMode[i] = false; 
                    break;
                case GENE_NAME_COLUMN_NAME:
                case ANAT_ENTITY_NAME_COLUMN_NAME:
                case STAGE_NAME_COLUMN_NAME:
                    quoteMode[i] = true; 
                    break;
                default:
                    throw log.throwing(new IllegalArgumentException(
                            "Unrecognized header: " + headers[i] + " for OMA TSV file."));
            }
        }
        
        return log.exit(quoteMode);
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
     * @param calls                 A {@code Stream} of {@code ExpressionCall}s that are expression
     *                              calls to be written into files.
     * @throws UncheckedIOException If an error occurred while trying to write the {@code outputFile}.
     */
    private int writeRows(Map<SingleSpExprFileType2, ICsvDozerBeanWriter> writersUsed, 
            Map<SingleSpExprFileType2, CellProcessor[]> processors, 
            Map<SingleSpExprFileType2, String[]> headers,
            Stream<ExpressionCall> calls) throws UncheckedIOException {
        log.entry(writersUsed, processors, headers, calls);

        return log.exit(calls.map(c -> {
            int i = 0;
            for (Entry<SingleSpExprFileType2, ICsvDozerBeanWriter> writerFileType : writersUsed.entrySet()) {
                String geneId = c.getGene().getEnsemblGeneId();
                String geneName = c.getGene().getName() == null? "": c.getGene().getName();
                String anatEntityId = c.getCondition().getAnatEntityId();
                String anatEntityName = c.getCondition().getAnatEntity().getName();
                String devStageId = c.getCondition().getDevStageId();
                String devStageName = c.getCondition().getDevStage().getId();
                String summaryCallType = convertExpressionSummaryToString(c.getSummaryCallType()); 
                String summaryQuality = convertSummaryQualityToString(c.getSummaryQuality());
                String expressionRank = c.getGlobalMeanRank() == null ? NA_VALUE :
                        c.getFormattedGlobalMeanRank();
                // FIXME use c.getExpressionScore()
                String expressionScore = NA_VALUE;// String.valueOf(c.getExpressionScore());
                Boolean includingObservedData = c.getDataPropagation().isIncludingObservedData();

                if (writerFileType.getKey().isSimpleFileType() && Boolean.TRUE.equals(includingObservedData)) {
                    SingleSpeciesSimpleExprFileBean simpleBean = new SingleSpeciesSimpleExprFileBean(
                        geneId, geneName, anatEntityId, anatEntityName, devStageId, devStageName,
                        summaryCallType, summaryQuality, expressionRank, expressionScore);
                    try {
                        writerFileType.getValue().write(simpleBean, processors.get(writerFileType.getKey()));
                        i++;
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                } else if (!writerFileType.getKey().isSimpleFileType()) {
                    List<DataExprCounts> counts = new ArrayList<>();
                    counts.add(getDataExprCountByDataType(c, DataType.AFFYMETRIX));
                    counts.add(getDataExprCountByDataType(c, DataType.EST));
                    counts.add(getDataExprCountByDataType(c, DataType.IN_SITU));
                    counts.add(getDataExprCountByDataType(c, DataType.RNA_SEQ));

                    SingleSpeciesCompleteExprFileBean completeBean = new SingleSpeciesCompleteExprFileBean(
                            geneId, geneName, anatEntityId, anatEntityName, devStageId, devStageName,
                            summaryCallType, summaryQuality,
                            expressionRank, expressionScore,
                            convertObservedDataToString(c.getDataPropagation().isIncludingObservedData()),
                            counts);
                    try {
                        writerFileType.getValue().write(completeBean, processors.get(writerFileType.getKey()));
                        i++;
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            }
            return i;
        }).mapToInt(Integer::intValue).sum());
    }

    /**
     * Get {@code DataExprCounts}s of a {@code DataType} from an {@code ExpressionCall}.
     * 
     * @param call      An {@code ExpressionCall} that is the expression call for which retrieve counts.
     * @param dataType  A {@code DataType} that is the data type allowing to filter counts to retrieve.
     * @return          A {@code DataExprCounts} that is the counts from {@code call} for {@code dataType}.
     */
    private DataExprCounts getDataExprCountByDataType(ExpressionCall call, DataType dataType) {
        log.entry(call, dataType);
        
        Set<ExpressionCallData> dataTypeCallData = call.getCallData().stream()
                .filter(d -> dataType.equals(d.getDataType())).collect(Collectors.toSet());
        
        if (dataTypeCallData.size() > 0) {
            ExpressionCallData data = dataTypeCallData.iterator().next();
            return log.exit(new DataExprCounts(data.getDataType(), convertExpressionToString(data.getCallType()),
                    this.getCountValue(data, CallType.Expression.EXPRESSED, DataQuality.HIGH,
                            PropagationState.ALL),
                    this.getCountValue(data, CallType.Expression.EXPRESSED, DataQuality.LOW,
                            PropagationState.ALL),
                    dataType.equals(DataType.EST)? 0: this.getCountValue(data,
                            CallType.Expression.NOT_EXPRESSED, DataQuality.HIGH, PropagationState.ALL),
                    dataType.equals(DataType.EST)? 0: this.getCountValue(data,
                            CallType.Expression.NOT_EXPRESSED, DataQuality.LOW, PropagationState.ALL),
                    this.resumeIncludingObservedData(data)));
        } else if (dataTypeCallData.size() == 0) {
            return log.exit(new DataExprCounts(dataType, NO_DATA_VALUE, 0L, 0L, 0L, 0L,
                    convertObservedDataToString(false)));
        } else {
            throw log.throwing(new IllegalStateException("Several ExpressionCallData for " +
                    dataType + " in: " + call));
        }
    }

    /** 
     * Get the count of an {@code ExpressionCallData} for an {@code Expression},
     * a {@code DataQuality}, and a {@code PropagationState}.
     * 
     * @param callData      An {@code ExpressionCallData} 
     * @param expression    An {@code Expression}
     * @param dataQuality   A {@code DataQuality}
     * @param propState     A {@code PropagationState}
     * @return              A {@code Long}
     */
    private Long getCountValue(ExpressionCallData callData, Expression expression,
            DataQuality dataQuality, PropagationState propState) {
        log.entry(callData, expression, dataQuality, propState);
        return Long.valueOf(callData.getExperimentCount(expression, dataQuality, propState).getCount());
    }

    /**
     * Check that all {@code CallType}s of a {code Set} of {@code ExpressionCallData},
     * check that are equals and return the unique value as {@code String}. 
     * 
     * @param callData  A {code Set} of {@code ExpressionCallData} that are the expression call data. 
     * @return          The {@code String} resuming {@code CallType}s.
     * @throws IllegalArgumentException If no {@code CallType} or more than one are found.
     */
    private String resumeCallType(Set<ExpressionCallData> callData) throws IllegalArgumentException {
        log.entry(callData);
        Set<Expression> types = callData.stream().map(d -> d.getCallType()).collect(Collectors.toSet());
        if (types.size() == 0) {
            throw new IllegalArgumentException("One call data type should be found");
        }
        if (types.size() > 1) {
            throw new IllegalArgumentException("Several call data types could not be found "
                    + "for the same data type: " + callData);
        }
        return log.exit(convertExpressionToString(types.iterator().next()));
    }

    /**
     * Return the resume of observed data values of an {@code ExpressionCallData}.
     * 
     * @param callData  An {@code ExpressionCallData} that is the expression call data.
     * @return          The {@code String} resuming observed data values.
     */
    private String resumeIncludingObservedData(ExpressionCallData callData) {
        log.entry(callData);
        return log.exit(convertObservedDataToString(
                callData.getExperimentCounts(PropagationState.SELF).stream()
                .map(ExperimentExpressionCount::getCount)
                .anyMatch(c -> c > 0)));
    }

    /**
     * Class parent of bean storing simple-species expression calls,
     * holding parameters common to all of them.
     *
     * @author  Valentine Rech de Laval
     * @version Bgee 14, Mar. 2017
     * @since   Bgee 13
     */
    public static abstract class SingleSpeciesExprFileBean {

        private String geneId;
        private String geneName;
        private String anatEntityId;
        private String anatEntityName;
        private String devStageId;
        private String devStageName;
        private String expression;
        private String callQuality;
        private String expressionRank;
        private String expressionScore;

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
                String expression, String callQuality, String expressionRank, String expressionScore) {
            this.geneId = geneId;
            this.geneName = geneName;
            this.anatEntityId = anatEntityId;
            this.anatEntityName = anatEntityName;
            this.devStageId = devStageId;
            this.devStageName = devStageName;
            this.expression = expression;
            this.callQuality = callQuality;
            this.expressionRank = expressionRank;
            this.expressionScore = expressionScore;
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
            result = prime * result + ((expression == null) ? 0 : expression.hashCode());
            result = prime * result + ((callQuality == null) ? 0 : callQuality.hashCode());
            result = prime * result + ((expressionRank == null) ? 0 : expressionRank.hashCode());
            result = prime * result + ((expressionScore == null) ? 0 : expressionScore.hashCode());
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
            return true;
        }
        
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("SingleSpeciesExprFileBean [geneId=").append(geneId)
                .append(", geneName=").append(geneName).append(", anatEntityId=").append(anatEntityId)
                .append(", anatEntityName=").append(anatEntityName)
                .append(", devStageId=").append(devStageId).append(", devStageName=").append(devStageName)
                .append(", expression=").append(expression).append(", callQuality=").append(callQuality)
                .append(", expressionRank=").append(expressionRank)
                .append(", expressionScore=").append(expressionScore).append("]");
            return builder.toString();
        }
    }
    
    /**
     * A bean representing a row of a single-species simple expression file. 
     * 
     * @author  Valentine Rech de Laval
     * @version Bgee 14, Mar. 2017
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
                String expression, String callQuality, String expressionRank, String expressionScore) {
            super(geneId, geneName, anatEntityId, anatEntityName, devStageId, devStageName,
                    expression, callQuality, expressionRank, expressionScore);
        }
    }
    
    /**
     * A bean representing a row of a single-species complete expression file. 
     *
     * @author  Valentine Rech de Laval
     * @version Bgee 14, Mar. 2017
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
                String expression, String callQuality, String expressionRank, String expressionScore,
                String includingObservedData, List<DataExprCounts> dataExprCounts) {
            super(geneId, geneName, anatEntityId, anatEntityName, devStageId, devStageName,
                    expression, callQuality, expressionRank, expressionScore);
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
     * Class used to store expression data by data types: present high count, 
     * present low count, absent high count, absent low count count,
     * and whether including observed data.
     *
     * @author  Valentine Rech de Laval
     * @version Bgee 14, Mar. 2017
     * @since   Bgee 14, Mar. 2017
     */
    public static class DataExprCounts {
        
        private DataType dataType;
        private String callType;
        private Long presentHighCount;
        private Long presentLowCount;
        private Long absentHighCount;
        private Long absentLowCount;
        private String observedData;
    
        /**
         * Constructor providing all arguments of the class.
         *
         * @param dataType
         * @param callType
         * @param presentHighCount
         * @param presentLowCount
         * @param absentLowCount
         * @param observedData
         */
        public DataExprCounts(DataType dataType, String callType, Long presentHighCount, Long presentLowCount,
                Long absentHighCount, Long absentLowCount, String observedData) {
            this.dataType = dataType;
            this.callType = callType;
            this.presentHighCount = presentHighCount;
            this.presentLowCount = presentLowCount;
            this.absentHighCount = absentHighCount;
            this.absentLowCount = absentLowCount;
            this.observedData = observedData;
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
        public Long getPresentHighCount() {
            return presentHighCount;
        }
        public void setPresentHighCount(Long presentHighCount) {
            this.presentHighCount = presentHighCount;
        }
        public Long getPresentLowCount() {
            return presentLowCount;
        }
        public void setPresentLowCount(Long presentLowCount) {
            this.presentLowCount = presentLowCount;
        }
        public Long getAbsentHighCount() {
            return absentHighCount;
        }
        public void setAbsentHighCount(Long absentHighCount) {
            this.absentHighCount = absentHighCount;
        }
        public Long getAbsentLowCount() {
            return absentLowCount;
        }
        public void setAbsentLowCount(Long absentLowCount) {
            this.absentLowCount = absentLowCount;
        }
        public String getObservedData() {
            return observedData;
        }
        public void setObservedData(String observedData) {
            this.observedData = observedData;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((dataType == null) ? 0 : dataType.hashCode());
            result = prime * result + ((callType == null) ? 0 : callType.hashCode());
            result = prime * result + ((presentHighCount == null) ? 0 : presentHighCount.hashCode());
            result = prime * result + ((presentLowCount == null) ? 0 : presentLowCount.hashCode());
            result = prime * result + ((absentHighCount == null) ? 0 : absentHighCount.hashCode());
            result = prime * result + ((absentLowCount == null) ? 0 : absentLowCount.hashCode());
            result = prime * result + ((observedData == null) ? 0 : observedData.hashCode());
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
            if (dataType == null) {
                if (other.dataType != null) {
                    return false;
                }
            } else if (!dataType.equals(other.dataType)) {
                return false;
            }
            if (callType == null) {
                if (other.callType != null) {
                    return false;
                }
            } else if (!callType.equals(other.callType)) {
                return false;
            }
            if (presentHighCount == null) {
                if (other.presentHighCount != null) {
                    return false;
                }
            } else if (!presentHighCount.equals(other.presentHighCount)) {
                return false;
            }
            if (presentLowCount == null) {
                if (other.presentLowCount != null) {
                    return false;
                }
            } else if (!presentLowCount.equals(other.presentLowCount)) {
                return false;
            }
            if (absentHighCount == null) {
                if (other.absentHighCount != null) {
                    return false;
                }
            } else if (!absentHighCount.equals(other.absentHighCount)) {
                return false;
            }
            if (absentLowCount == null) {
                if (other.absentLowCount != null) {
                    return false;
                }
            } else if (!absentLowCount.equals(other.absentLowCount)) {
                return false;
            }
            if (observedData == null) {
                if (other.observedData != null) {
                    return false;
                }
            } else if (!observedData.equals(other.observedData)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("DataExprCounts [dataType=").append(dataType)
                .append(", callType=").append(callType)
                .append(", presentHighCount=").append(presentHighCount)
                .append(", presentLowCount=").append(presentLowCount)
                .append(", absentHighCount=").append(absentHighCount)
                .append(", absentLowCount=").append(absentLowCount)
                .append(", observedData=").append(observedData).append("]");
            return builder.toString();
        }
    }
}
