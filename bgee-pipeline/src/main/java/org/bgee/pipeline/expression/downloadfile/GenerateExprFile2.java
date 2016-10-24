package org.bgee.pipeline.expression.downloadfile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.baseelements.CallType.Expression;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.file.DownloadFile.CategoryEnum;
import org.bgee.pipeline.BgeeDBUtils;
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
 * @version Bgee 13, Sept 2016
 * @since   Bgee 13
 */
public class GenerateExprFile2 extends GenerateDownloadFile {

    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(GenerateExprFile2.class.getName());

    /**
     * A {@code String} that is the name of the column containing expression/no-expression found
     * with EST experiment, in the download file.
     */
    public final static String EST_DATA_COLUMN_NAME = "EST data";

    /**
     * A {@code String} that is the name of the column containing call quality found with
     * EST experiment, in the download file.
     */
    public final static String EST_CALL_QUALITY_COLUMN_NAME = "EST call quality";

    /**
     * A {@code String} that is the name of the column containing if an EST experiment is observed, 
     * in the download file.
     */
    public final static String EST_OBSERVED_DATA_COLUMN_NAME = "Including EST observed data";
    
    /**
     * A {@code String} that is the name of the column containing expression/no-expression
     * found with <em>in situ</em> experiment, in the download file.
     */
    public final static String INSITU_DATA_COLUMN_NAME = "In situ data";

    /**
     * A {@code String} that is the name of the column containing call quality found with
     * <em>in situ</em> experiment, in the download file.
     */
    public final static String INSITU_CALL_QUALITY_COLUMN_NAME = "In situ call quality";

    /**
     * A {@code String} that is the name of the column containing if an <em>in situ</em> experiment 
     * is observed, in the download file.
     */
    public final static String INSITU_OBSERVED_DATA_COLUMN_NAME = "Including in situ observed data";
    
    /**
     * A {@code String} that is the name of the column containing expression/no-expression
     * found with relaxed <em>in situ</em> experiment, in the download file.
     */
    public final static String RELAXED_INSITU_DATA_COLUMN_NAME = "Relaxed in situ data";

    /**
     * A {@code String} that is the name of the column containing call quality found with relaxed
     * <em>in situ</em> experiment, in the download file.
     */
    public final static String RELAXED_INSITU_CALL_QUALITY_COLUMN_NAME = 
            "Relaxed in situ call quality";

    /**
     * A {@code String} that is the name of the column containing if a relaxed
     * <em>in situ</em> experiment is observed, in the download file.
     */
    public final static String RELAXED_INSITU_OBSERVED_DATA_COLUMN_NAME = 
            "Including relaxed in situ observed data";
    
    /**
     * A {@code String} that is the name of the column containing whether the call include
     * observed data or not.
     */
    public final static String INCLUDING_OBSERVED_DATA_COLUMN_NAME = "Including observed data";

    /**
     * A {@code String} that is the name of the column containing merged
     * expression/no-expression from different data types, in the download file.
     */
    public final static String EXPRESSION_COLUMN_NAME = "Expression";

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
            CommandRunner.parseListArgument(args[0]),
            GenerateDownloadFile.convertToFileTypes(
                CommandRunner.parseListArgument(args[1]), SingleSpExprFileType2.class),
            args[2]);
        generator.generateExprFiles();

        log.exit();
    }

    /**
     * A {@code boolean} defining whether the filter for simple file keeps observed data only 
     * if {@code true} or organ observed data only (propagated stages are allowed) if {@code false}.
     */
    // FIXME not used do we need to manage that?
    protected final boolean observedDataOnly;

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
     * @throws IllegalArgumentException If {@code directory} is {@code null} or blank.
     */
    public GenerateExprFile2(List<String> speciesIds, Set<SingleSpExprFileType2> fileTypes, 
            String directory) throws IllegalArgumentException {
        this(null, speciesIds, fileTypes, directory);
    }

    /**
     * Constructor providing the {@code MySQLDAOManager} that will be used by this object
     * to perform queries to the database. This is useful for unit testing.
     * 
     * @param manager       the {@code MySQLDAOManager} to use.
     * @param speciesIds    A {@code List} of {@code String}s that are the IDs of species we want 
     *                      to generate data for. If {@code null} or empty, all species are used.
     * @param fileTypes     A {@code Set} of {@code ExprFileType}s that are the types of files
     *                      we want to generate. If {@code null} or empty, all {@code ExprFileType}s
     *                      are generated.
     * @param directory     A {@code String} that is the directory where to store files.
     * @throws IllegalArgumentException If {@code directory} is {@code null} or blank.
     */
    public GenerateExprFile2(MySQLDAOManager manager, List<String> speciesIds,
        Set<SingleSpExprFileType2> fileTypes, String directory) throws IllegalArgumentException {
        this(manager, speciesIds, fileTypes, directory, false);
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
     * @param observedDataOnly  A {@code boolean} defining whether the filter for simple file keeps 
     *                          observed data only if {@code true} or organ observed data only 
     *                          (propagated stages are allowed) if {@code false}..
     * @throws IllegalArgumentException If {@code directory} is {@code null} or blank.
     */
    public GenerateExprFile2(MySQLDAOManager manager, List<String> speciesIds,
        Set<SingleSpExprFileType2> fileTypes, String directory, boolean observedDataOnly) 
                throws IllegalArgumentException {
        this(manager, speciesIds, fileTypes, directory, observedDataOnly, ServiceFactory::new);
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
     * @param observedDataOnly          A {@code boolean} defining whether the filter for simple file keeps 
     *                                  observed data only if {@code true} or organ observed data only 
     *                                  (propagated stages are allowed) if {@code false}.
     * @param serviceFactorySupplier    A {@code Supplier} of {@code ServiceFactory}s 
     *                                  to be able to provide one to each thread.
     * @throws IllegalArgumentException If {@code directory} is {@code null} or blank.
     */
    public GenerateExprFile2(MySQLDAOManager manager, List<String> speciesIds,
        Set<SingleSpExprFileType2> fileTypes, String directory, boolean observedDataOnly,
        Supplier<ServiceFactory> serviceFactorySupplier) throws IllegalArgumentException {
        super(manager, speciesIds, fileTypes, directory);
        this.observedDataOnly = observedDataOnly;
        this.serviceFactorySupplier = serviceFactorySupplier;
    }

    /**
     * Generate expression files, for the types defined by {@code fileTypes}, for species
     * defined by {@code speciesIds}, in the directory {@code directory}.
     * 
     * @param serviceFactory    A {@code ServiceFactory} to retrieve Bgee services from.
     * @throws IOException  If an error occurred while trying to write generated files.
     */
    //TODO: add OMA node ID in complete files
    public void generateExprFiles() throws IOException {
        log.entry();

        Set<String> setSpecies = Collections.unmodifiableSet(this.speciesIds == null?
                new HashSet<>() : new HashSet<>(this.speciesIds));

        // Check user input, retrieve info for generating file names
        // Retrieve species names and IDs (all species names if speciesIds is null or empty)
        Map<String, String> speciesNamesForFilesByIds = 
                this.checkAndGetLatinNamesBySpeciesIds(setSpecies);
        assert speciesNamesForFilesByIds.size() >= setSpecies.size();

        // If no file types are given by user, we set all file types
        if (this.fileTypes == null || this.fileTypes.isEmpty()) {
            this.fileTypes = EnumSet.allOf(SingleSpExprFileType2.class);
        }

        // Retrieve gene names, stage names, anat. entity names, once for all species
        Map<String, String> geneNamesByIds = 
                BgeeDBUtils.getGeneNamesByIds(setSpecies, this.getGeneDAO());
        Map<String, String> stageNamesByIds = 
                BgeeDBUtils.getStageNamesByIds(setSpecies, this.getStageDAO());
        Map<String, String> anatEntityNamesByIds = 
                BgeeDBUtils.getAnatEntityNamesByIds(setSpecies, this.getAnatEntityDAO());


        // Generate expression files, species by species.
        // The generation of files are independent, so we can safely go multi-threading
        speciesNamesForFilesByIds.keySet().parallelStream().forEach(speciesId -> {
            log.info("Start generating of expression files for the species {}...", speciesId);

            try {
                this.generateExprFilesForOneSpecies(speciesNamesForFilesByIds.get(speciesId), 
                        speciesId, geneNamesByIds, stageNamesByIds, anatEntityNamesByIds);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
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
     * @param speciesId             A {@code String} that is the ID of species for which files are
     *                              generated. 
     * @param geneNamesByIds        A {@code Map} where keys are {@code String}s corresponding to 
     *                              gene IDs, the associated values being {@code String}s 
     *                              corresponding to gene names. 
     * @param stageNamesByIds       A {@code Map} where keys are {@code String}s corresponding to 
     *                              stage IDs, the associated values being {@code String}s 
     *                              corresponding to stage names. 
     * @param anatEntityNamesByIds  A {@code Map} where keys are {@code String}s corresponding to 
     *                              anatomical entity IDs, the associated values being 
     *                              {@code String}s corresponding to anatomical entity names. 
     * @param anatEntityNamesByIds  A {@code Map} where keys are {@code String}s corresponding to 
     *                              anatomical entity IDs, the associated values being 
     *                              {@code String}s corresponding to anatomical entity names. 
     * @throws IOException  If an error occurred while trying to write the {@code outputFile}.
     */
    private void generateExprFilesForOneSpecies(String fileNamePrefix, String speciesId,
            Map<String, String> geneNamesByIds, Map<String, String> stageNamesByIds,
            Map<String, String> anatEntityNamesByIds) throws IOException {
        log.entry(fileNamePrefix, speciesId, geneNamesByIds, stageNamesByIds, anatEntityNamesByIds);

        log.debug("Start generating expression files for the species {} and file types {}...", 
                speciesId, this.fileTypes);

        //********************************
        // RETRIEVE DATA FROM DATA SOURCE
        //********************************
        log.trace("Start retrieving data for expression files for the species {}...", speciesId);

        ServiceFactory serviceFactory = this.serviceFactorySupplier.get();

        Set<String> speciesFilter = new HashSet<String>();
        speciesFilter.add(speciesId);

        // Load non-informative anatomical entities: 
        // calls occurring in these anatomical entities, and generated from 
        // data propagation only (no observed data in them), will be discarded.
        // FIXME filter by non informative anat. entities in ExpressionCallFilter instead of filter stream 
        Set<String> nonInformativeAnatEntities = serviceFactory.getAnatEntityService()
                .loadNonInformativeAnatEntitiesBySpeciesIds(speciesFilter)
                .map(AnatEntity::getId)
                .collect(Collectors.toSet());

        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = 
                new LinkedHashMap<>();
        //The ordering by gene ID is essential here, because we will load into memory 
        //all data from one gene at a time, for clustering and redundancy discovery. 
        //The ordering by rank is not mandatory, for a given gene we are going to reorder anyway
        serviceOrdering.put(CallService.OrderingAttribute.GENE_ID, Service.Direction.ASC);
        serviceOrdering.put(CallService.OrderingAttribute.ANAT_ENTITY_ID, Service.Direction.ASC);
        serviceOrdering.put(CallService.OrderingAttribute.DEV_STAGE_ID, Service.Direction.ASC);
        
        ExpressionCallFilter callFilter = new ExpressionCallFilter(null, null, new HashSet<>(Arrays.asList(
                new ExpressionCallData(Expression.EXPRESSED),
                new ExpressionCallData(Expression.NOT_EXPRESSED))));

        Stream<ExpressionCall> calls = serviceFactory.getCallService().loadExpressionCalls(
                speciesId, callFilter, null, serviceOrdering, true)
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
                        currentFileType.getStringRepresentation() + EXTENSION);
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
            this.writeRows(geneNamesByIds, stageNamesByIds, anatEntityNamesByIds,
                    writersUsed, processors, headers, calls);
        } catch (Exception e) {
            this.deleteTempFiles(generatedFileNames, tmpExtension);
            throw e;
        } finally {
            for (ICsvDozerBeanWriter writer : writersUsed.values()) {
                writer.close();
            }
        }
        // now, if everything went fine, we rename the temporary files
        this.renameTempFiles(generatedFileNames, tmpExtension);

        log.exit();
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

        List<Object> qualities = new ArrayList<Object>();
        for (DataQuality quality : DataQuality.values()) {
            qualities.add(convertDataQualityToString(quality));
        }
        List<Object> qualitySummaries = new ArrayList<Object>();
        qualitySummaries.add(convertDataQualityToString(DataQuality.HIGH));
        qualitySummaries.add(convertDataQualityToString(DataQuality.LOW));
        qualitySummaries.add(GenerateDownloadFile.NA_VALUE);
        
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
                case ANATENTITY_ID_COLUMN_NAME:
                case ANATENTITY_NAME_COLUMN_NAME:
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
            	    case INSITU_DATA_COLUMN_NAME:
            	    case RNASEQ_DATA_COLUMN_NAME:
            	        processors[i] = new IsElementOf(expressions);
            	        break;
            	    case AFFYMETRIX_CALL_QUALITY_COLUMN_NAME:
            	    case EST_CALL_QUALITY_COLUMN_NAME:
            	    case INSITU_CALL_QUALITY_COLUMN_NAME:
            	    case RNASEQ_CALL_QUALITY_COLUMN_NAME:
            	        processors[i] = new IsElementOf(qualities);
            	        break;
            	    case AFFYMETRIX_OBSERVED_DATA_COLUMN_NAME:
            	    case EST_OBSERVED_DATA_COLUMN_NAME:
            	    case INSITU_OBSERVED_DATA_COLUMN_NAME:
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
        int nbColumns = 8;
        if (!fileType.isSimpleFileType()) {
            nbColumns = 21;
        }
        headers = new String[nbColumns];

        // *** Headers common to all file types ***
        headers[0] = GENE_ID_COLUMN_NAME;
        headers[1] = GENE_NAME_COLUMN_NAME;
        headers[2] = ANATENTITY_ID_COLUMN_NAME;
        headers[3] = ANATENTITY_NAME_COLUMN_NAME;
        headers[4] = STAGE_ID_COLUMN_NAME;
        headers[5] = STAGE_NAME_COLUMN_NAME;
        headers[6] = EXPRESSION_COLUMN_NAME;
        headers[7] = QUALITY_COLUMN_NAME;


        if (!fileType.isSimpleFileType()) {
            // *** Headers specific to complete file ***
            headers[8] = INCLUDING_OBSERVED_DATA_COLUMN_NAME;
            headers[9] = AFFYMETRIX_DATA_COLUMN_NAME;
            headers[10] = AFFYMETRIX_CALL_QUALITY_COLUMN_NAME;
            headers[11] = AFFYMETRIX_OBSERVED_DATA_COLUMN_NAME;
            headers[12] = EST_DATA_COLUMN_NAME;
            headers[13] = EST_CALL_QUALITY_COLUMN_NAME;
            headers[14] = EST_OBSERVED_DATA_COLUMN_NAME;
            headers[15] = INSITU_DATA_COLUMN_NAME;
            headers[16] = INSITU_CALL_QUALITY_COLUMN_NAME;
            headers[17] = INSITU_OBSERVED_DATA_COLUMN_NAME;
//            headers[] = RELAXED_INSITU_DATA_COLUMN_NAME;
//            headers[] = RELAXED_INSITU_DATA_COLUMN_NAME;
//            headers[] = RELAXED_INSITU_OBSERVED_DATA_COLUMN_NAME;
            headers[18] = RNASEQ_DATA_COLUMN_NAME;
            headers[19] = RNASEQ_CALL_QUALITY_COLUMN_NAME;
            headers[20] = RNASEQ_OBSERVED_DATA_COLUMN_NAME;
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
            case ANATENTITY_ID_COLUMN_NAME: 
                mapping[i] = "anatEntityId";
                break;
            case ANATENTITY_NAME_COLUMN_NAME: 
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
            }
            
            //if it was one of the column common to all beans, 
            //iterate next column name
            if (mapping[i] != null) {
                continue;
            }

            if (!fileType.isSimpleFileType()) {
                // *** Attributes specific to complete file ***
                switch (header[i]) {

                case INCLUDING_OBSERVED_DATA_COLUMN_NAME: 
                    mapping[i] = "includingObservedData";
                    break;
                case AFFYMETRIX_DATA_COLUMN_NAME: 
                    mapping[i] = "affymetrixData";
                    break;
                case AFFYMETRIX_CALL_QUALITY_COLUMN_NAME: 
                    mapping[i] = "affymetrixCallQuality";
                    break;
                case AFFYMETRIX_OBSERVED_DATA_COLUMN_NAME: 
                    mapping[i] = "includingAffymetrixObservedData";
                    break;
                case EST_DATA_COLUMN_NAME: 
                    mapping[i] = "estData";
                    break;
                case EST_CALL_QUALITY_COLUMN_NAME: 
                    mapping[i] = "estCallQuality";
                    break;
                case EST_OBSERVED_DATA_COLUMN_NAME: 
                    mapping[i] = "includingEstObservedData";
                    break;
                case INSITU_DATA_COLUMN_NAME: 
                    mapping[i] = "inSituData";
                    break;
                case INSITU_CALL_QUALITY_COLUMN_NAME: 
                    mapping[i] = "inSituCallQuality";
                    break;
                case INSITU_OBSERVED_DATA_COLUMN_NAME: 
                    mapping[i] = "includingInSituObservedData";
                    break;
                case RNASEQ_DATA_COLUMN_NAME: 
                    mapping[i] = "rnaSeqData";
                    break;
                case RNASEQ_CALL_QUALITY_COLUMN_NAME: 
                    mapping[i] = "rnaSeqCallQuality";
                    break;
                case RNASEQ_OBSERVED_DATA_COLUMN_NAME: 
                    mapping[i] = "includingRnaSeqObservedData";
                    break;
                }
            }
            if (mapping[i] == null) {
                throw log.throwing(new IllegalArgumentException("Unrecognized header: " 
                        + header[i] + " for file type: " + fileType.getStringRepresentation()));
            }
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
                case ANATENTITY_ID_COLUMN_NAME:
                case STAGE_ID_COLUMN_NAME:
                case EXPRESSION_COLUMN_NAME:
                case QUALITY_COLUMN_NAME:
                case INCLUDING_OBSERVED_DATA_COLUMN_NAME:
                case AFFYMETRIX_DATA_COLUMN_NAME:
                case AFFYMETRIX_CALL_QUALITY_COLUMN_NAME:
                case AFFYMETRIX_OBSERVED_DATA_COLUMN_NAME:
                case EST_DATA_COLUMN_NAME:
                case EST_CALL_QUALITY_COLUMN_NAME:
                case EST_OBSERVED_DATA_COLUMN_NAME:
                case INSITU_DATA_COLUMN_NAME:
                case INSITU_CALL_QUALITY_COLUMN_NAME:
                case INSITU_OBSERVED_DATA_COLUMN_NAME:
                case RNASEQ_DATA_COLUMN_NAME:
                case RNASEQ_CALL_QUALITY_COLUMN_NAME:
                case RNASEQ_OBSERVED_DATA_COLUMN_NAME:
                    quoteMode[i] = false; 
                    break;
                case GENE_NAME_COLUMN_NAME:
                case ANATENTITY_NAME_COLUMN_NAME:
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
     * {@code callTOs} to produce information, that is different depending on {@code fileType}.
     * <p>
     * {@code callTOs} must all have the same values returned by {@link CallTO#getGeneId()}, 
     * {@link CallTO#getAnatEntityId()}, {@link CallTO#getStageId()}, and they must be respectively 
     * equal to {@code geneId}, {@code anatEntityId}, {@code stageId}.
     * <p>
     * Information that will be generated is provided in the given {@code processors}.
     * 
     * @param geneId                A {@code String} that is the ID of the gene considered.
     * @param geneNamesByIds        A {@code Map} where keys are {@code String}s corresponding to 
     *                              gene IDs, the associated values being {@code String}s 
     *                              corresponding to gene names. 
     * @param stageNamesByIds       A {@code Map} where keys are {@code String}s corresponding to 
     *                              stage IDs, the associated values being {@code String}s 
     *                              corresponding to stage names. 
     * @param anatEntityNamesByIds  A {@code Map} where keys are {@code String}s corresponding to 
     *                              anatomical entity IDs, the associated values being 
     *                              {@code String}s corresponding to anatomical entity names. 
     * @param writersUsed           A {@code Map} where keys are {@code ExprFileType}s
     *                              corresponding to which type of file should be generated, the 
     *                              associated values being {@code ICsvMapWriter}s corresponding to 
     *                              the writers.
     * @param processors            A {@code Map} where keys are {@code ExprFileType}s 
     *                              corresponding to which type of file should be generated, the 
     *                              associated values being an {@code Array} of 
     *                              {@code CellProcessor}s used to process a file.
     * @param headers               A {@code Map} where keys are {@code ExprFileType}s 
     *                              corresponding to which type of file should be generated, the 
     *                              associated values being an {@code Array} of {@code String}s used 
     *                              to produce the header.
     * @param calls                 A {@code Stream} of {@code ExpressionCall}s that are propagated
     *                              and reconciled expression calls.
     * @throws IOException  If an error occurred while trying to write the {@code outputFile}.
     */
    private void writeRows(Map<String, String> geneNamesByIds, 
            Map<String, String> stageNamesByIds, Map<String, String> anatEntityNamesByIds, 
            Map<SingleSpExprFileType2, ICsvDozerBeanWriter> writersUsed, 
            Map<SingleSpExprFileType2, CellProcessor[]> processors, 
            Map<SingleSpExprFileType2, String[]> headers,
            Stream<ExpressionCall> calls) throws IOException {
        log.entry(geneNamesByIds, stageNamesByIds, anatEntityNamesByIds, writersUsed, 
                processors, headers, calls);

        calls.forEachOrdered(c -> {
            for (Entry<SingleSpExprFileType2, ICsvDozerBeanWriter> writerFileType : writersUsed.entrySet()) {
                String geneId = c.getGeneId();
                String geneName = geneNamesByIds.containsKey(geneId)? geneNamesByIds.get(geneId) : "";
                String anatEntityId = c.getCondition().getAnatEntityId();
                String anatEntityName = anatEntityNamesByIds.get(anatEntityId);
                String devStageId = c.getCondition().getDevStageId();
                String devStageName = stageNamesByIds.get(c.getCondition().getDevStageId());
                String summaryCallType = convertExpressionSummaryToString(c.getSummaryCallType()); 
                String summaryQuality = convertDataQualityToString(c.getSummaryQuality());
                Boolean includingObservedData =c.getDataPropagation().getIncludingObservedData();
                
                if (writerFileType.getKey().isSimpleFileType()  
                        && Boolean.TRUE.equals(includingObservedData)) {
                    SingleSpeciesSimpleExprFileBean bean = new SingleSpeciesSimpleExprFileBean(
                            geneId, geneName, anatEntityId, anatEntityName,
                            devStageId, devStageName, summaryCallType, summaryQuality);
                    try {
                        writerFileType.getValue().write(bean, processors.get(writerFileType.getKey()));
                    } catch (Exception e) {
                        // TODO Manage exception in stream
                        e.printStackTrace();
                    }
                } else if (!writerFileType.getKey().isSimpleFileType()) {
                    String affymetrixData = NO_DATA_VALUE, affymetrixCallQuality = NA_VALUE,
                            estData = NO_DATA_VALUE, estCallQuality = NA_VALUE,
                            inSituData = NO_DATA_VALUE, inSituCallQuality = NA_VALUE,
                            rnaSeqData = NO_DATA_VALUE, rnaSeqCallQuality = NA_VALUE,
                            includingAffymetrixObservedData = ObservedData.NOT_OBSERVED.getStringRepresentation(),
                            includingEstObservedData = ObservedData.NOT_OBSERVED.getStringRepresentation(),
                            includingInSituObservedData =  ObservedData.NOT_OBSERVED.getStringRepresentation(),
                            includingRnaSeqObservedData = ObservedData.NOT_OBSERVED.getStringRepresentation();
                    Set<ExpressionCallData> callData = c.getCallData();

                    Set<ExpressionCallData> affyCallData = callData.stream()
                            .filter(d -> DataType.AFFYMETRIX.equals(d.getDataType())).collect(Collectors.toSet());
                    if (affyCallData.size() > 0) {
                        affymetrixData = resumeType(affyCallData);
                        affymetrixCallQuality = resumeQualities(affyCallData);
                        includingAffymetrixObservedData = resumeIncludingObservedData(affyCallData);
                    }
                    
                    Set<ExpressionCallData> estCallData = callData.stream()
                            .filter(d -> DataType.EST.equals(d.getDataType())).collect(Collectors.toSet());
                    if (estCallData.size() > 0) {
                        estData = resumeType(estCallData);
                        estCallQuality = resumeQualities(estCallData);
                        includingEstObservedData = resumeIncludingObservedData(estCallData);
                    }
                    
                    Set<ExpressionCallData> inSituCallData = callData.stream()
                            .filter(d -> DataType.IN_SITU.equals(d.getDataType())).collect(Collectors.toSet());
                    if (inSituCallData.size() > 0) {
                        inSituData = resumeType(inSituCallData);
                        inSituCallQuality = resumeQualities(inSituCallData);
                        includingInSituObservedData = resumeIncludingObservedData(inSituCallData);
                    }
                    
                    Set<ExpressionCallData> rnaSeqCallData = callData.stream()
                            .filter(d -> DataType.RNA_SEQ.equals(d.getDataType())).collect(Collectors.toSet());
                    if (rnaSeqCallData.size() > 0) {
                        rnaSeqData = resumeType(rnaSeqCallData);
                        rnaSeqCallQuality = resumeQualities(rnaSeqCallData);
                        includingRnaSeqObservedData = resumeIncludingObservedData(rnaSeqCallData);
                    }

                    SingleSpeciesCompleteExprFileBean bean = new SingleSpeciesCompleteExprFileBean(
                            geneId, geneName, anatEntityId, anatEntityName,
                            devStageId, devStageName, summaryCallType, summaryQuality,
                            convertObservedDataToString(c.getDataPropagation().getIncludingObservedData()),
                            affymetrixData, affymetrixCallQuality, includingAffymetrixObservedData,
                            estData, estCallQuality, includingEstObservedData,
                            inSituData, inSituCallQuality, includingInSituObservedData,
                            rnaSeqData, rnaSeqCallQuality, includingRnaSeqObservedData);
                    try {
                        writerFileType.getValue().write(bean, processors.get(writerFileType.getKey()) );
                    } catch (IOException e) {
                        // TODO Manage exception in stream
                    }
                }
            }            
        });
        log.exit();
    }

    private String resumeType(Set<ExpressionCallData> callData) {
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

    private String resumeQualities(Set<ExpressionCallData> callData) {
        log.entry(callData);
        Set<DataQuality> curQualities = callData.stream()
                .map(d -> d.getDataQuality())
                .collect(Collectors.toSet());
        if (curQualities.contains(DataQuality.HIGH)) {
            return log.exit(convertDataQualityToString(DataQuality.HIGH));
        }
        return log.exit(convertDataQualityToString(DataQuality.LOW));
    }
    
    private String resumeIncludingObservedData(Set<ExpressionCallData> callData) {
        log.entry(callData);
        return log.exit(convertObservedDataToString(callData.parallelStream()
                .anyMatch(d -> Boolean.TRUE.equals(d.getDataPropagation().getIncludingObservedData()))));
    }

    /**
     * Class parent of bean storing simple-species expression calls, holding parameters common to all of them.
     *
     * @author  Valentine Rech de Laval
     * @version Bgee 13, Sept. 2016
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

        /**
         * 0-argument constructor of the bean.
         */
        protected SingleSpeciesExprFileBean() {
        }

        /**
         * Constructor providing all arguments of the class.
         *
         * @param geneId            A {@code String} that is the ID of the gene.
         * @param geneName          A {@code String} that is the name of the gene.
         * @param anatEntityId      A {@code String} that is the ID of the anatomical entity.
         * @param anatEntityName    A {@code String} that is the name of the anatomical entity.
         * @param devStageId        A {@code String} that is the ID of the developmental stage.
         * @param devStageName      A {@code String} that is the name of the developmental stage.
         * @param expression        A {@code String} that is the expression.
         * @param callQuality       A {@code String} that is the call quality.
         */
        protected SingleSpeciesExprFileBean(String geneId, String geneName,
                String anatEntityId, String anatEntityName, String devStageId, String devStageName,
                String expression, String callQuality) {
            this.geneId = geneId;
            this.geneName = geneName;
            this.anatEntityId = anatEntityId;
            this.anatEntityName = anatEntityName;
            this.devStageId = devStageId;
            this.devStageName = devStageName;
            this.expression = expression;
            this.callQuality = callQuality;
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
            return true;
        }

        @Override
        public String toString() {
            return " Gene ID: " + getGeneId() + " - Gene name()=" + getGeneName()
                    + " - Anat. entity ID: " + getAnatEntityId() + " - Anat. entity name: " + getAnatEntityName()
                    + " - Dev. stage ID: " + getDevStageId() + " - Dev. stage name: " + getDevStageName()
                    + " - Expression: " + getExpression() + " - Call quality: " + getCallQuality();
        }
    }
    
    /**
     * A bean representing a row of a single-species simple expression file. 
     * 
     * @author  Valentine Rech de Laval
     * @version Bgee 13, Sept. 2016
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
         * @param geneId            A {@code String} that is the ID of the gene.
         * @param geneName          A {@code String} that is the name of the gene.
         * @param anatEntityId      A {@code String} that is the ID of the anatomical entity.
         * @param anatEntityName    A {@code String} that is the name of the anatomical entity.
         * @param devStageId        A {@code String} that is the ID of the developmental stage.
         * @param devStageName      A {@code String} that is the name of the developmental stage.
         * @param expression        A {@code String} that is the expression.
         * @param callQuality       A {@code String} that is the call quality.
         */
        protected SingleSpeciesSimpleExprFileBean(String geneId, String geneName,
                String anatEntityId, String anatEntityName, String devStageId, String devStageName,
                String expression, String callQuality) {
            super(geneId, geneName, anatEntityId, anatEntityName, devStageId, devStageName, expression, callQuality);
        }
    }
    /**
     * A bean representing a row of a single-species complete expression file. 
     *
     * @author  Valentine Rech de Laval
     * @version Bgee 13, Sept. 2016
     * @since   Bgee 13, Sept. 2016
     */
    public static class SingleSpeciesCompleteExprFileBean extends SingleSpeciesExprFileBean {

        private String includingObservedData;
        private String affymetrixData;
        private String affymetrixCallQuality;
        private String includingAffymetrixObservedData;
        private String estData;
        private String estCallQuality;
        private String includingEstObservedData;
        private String inSituData;
        private String inSituCallQuality;
        private String includingInSituObservedData;
        private String rnaSeqData;
        private String rnaSeqCallQuality;
        private String includingRnaSeqObservedData;

        /**
         * 0-argument constructor of the bean.
         */
        protected SingleSpeciesCompleteExprFileBean() {
        }

        /**
         * Constructor providing all arguments of the class.
         *
         * @param geneId                            A {@code String} that is the ID of the gene.
         * @param geneName                          A {@code String} that is the name of the gene.
         * @param anatEntityId                      A {@code String} that is the ID of the anatomical entity.
         * @param anatEntityName                    A {@code String} that is the name of the anatomical entity.
         * @param devStageId                        A {@code String} that is the ID of the developmental stage.
         * @param devStageName                      A {@code String} that is the name of the developmental stage.
         * @param expression                        A {@code String} that is the expression.
         * @param callQuality                       A {@code String} that is the call quality.
         * @param includingObservedData             A {@code String} defining whether include observed data.
         * @param affymetrixData                    A {@code String} that is the affymetrix data.
         * @param affymetrixCallQuality             A {@code String} that is the affymetrix call quality.
         * @param includingAffymetrixObservedData   A {@code String} defining whether include affymetrix observed data.
         * @param estData                           A {@code String} that is the EST data.
         * @param estCallQuality                    A {@code String} that is the EST call quality.
         * @param includingEstObservedData          A {@code String} defining whether include EST observed data.
         * @param inSituData                        A {@code String} that is the <em>in situ</em> data.
         * @param inSituCallQuality                 A {@code String} that is the <em>in situ</em> call quality.
         * @param includingInSituObservedData       A {@code String} defining whether include <em>in situ</em> observed data.
         * @param rnaSeqData                        A {@code String} that is the RNA-seq data.
         * @param rnaSeqCallQuality                 A {@code String} that is the RNA-seq call quality.
         * @param includingRnaSeqObservedData       A {@code String} defining whether include RNA-seq observed data.
         */
        protected SingleSpeciesCompleteExprFileBean(String geneId, String geneName,
                String anatEntityId, String anatEntityName, String devStageId, String devStageName,
                String expression, String callQuality, String includingObservedData,
                String affymetrixData, String affymetrixCallQuality, String includingAffymetrixObservedData,
                String estData, String estCallQuality, String includingEstObservedData,
                String inSituData, String inSituCallQuality, String includingInSituObservedData,
                String rnaSeqData, String rnaSeqCallQuality, String includingRnaSeqObservedData) {
            super(geneId, geneName, anatEntityId, anatEntityName, devStageId, devStageName, expression, callQuality);
            this.includingObservedData = includingObservedData;
            this.affymetrixData = affymetrixData;
            this.affymetrixCallQuality = affymetrixCallQuality;
            this.includingAffymetrixObservedData = includingAffymetrixObservedData;
            this.estData = estData;
            this.estCallQuality = estCallQuality;
            this.includingEstObservedData = includingEstObservedData;
            this.inSituData = inSituData;
            this.inSituCallQuality = inSituCallQuality;
            this.includingInSituObservedData = includingInSituObservedData;
            this.rnaSeqData = rnaSeqData;
            this.rnaSeqCallQuality = rnaSeqCallQuality;
            this.includingRnaSeqObservedData = includingRnaSeqObservedData;
        }

        public String getIncludingObservedData() {
            return includingObservedData;
        }
        public String getAffymetrixData() {
            return affymetrixData;
        }
        public String getAffymetrixCallQuality() {
            return affymetrixCallQuality;
        }
        public String getIncludingAffymetrixObservedData() {
            return includingAffymetrixObservedData;
        }
        public String getEstData() {
            return estData;
        }
        public String getEstCallQuality() {
            return estCallQuality;
        }
        public String getIncludingEstObservedData() {
            return includingEstObservedData;
        }
        public String getInSituData() {
            return inSituData;
        }
        public String getInSituCallQuality() {
            return inSituCallQuality;
        }
        public String getIncludingInSituObservedData() {
            return includingInSituObservedData;
        }
        public String getRnaSeqData() {
            return rnaSeqData;
        }
        public String getRnaSeqCallQuality() {
            return rnaSeqCallQuality;
        }
        public String getIncludingRnaSeqObservedData() {
            return includingRnaSeqObservedData;
        }
        public void setIncludingObservedData(String includingObservedData) {
            this.includingObservedData = includingObservedData;
        }
        public void setAffymetrixData(String affymetrixData) {
            this.affymetrixData = affymetrixData;
        }
        public void setAffymetrixCallQuality(String affymetrixCallQuality) {
            this.affymetrixCallQuality = affymetrixCallQuality;
        }
        public void setIncludingAffymetrixObservedData(String includingAffymetrixObservedData) {
            this.includingAffymetrixObservedData = includingAffymetrixObservedData;
        }
        public void setEstData(String estData) {
            this.estData = estData;
        }
        public void setEstCallQuality(String estCallQuality) {
            this.estCallQuality = estCallQuality;
        }
        public void setIncludingEstObservedData(String includingEstObservedData) {
            this.includingEstObservedData = includingEstObservedData;
        }
        public void setInSituData(String inSituData) {
            this.inSituData = inSituData;
        }
        public void setInSituCallQuality(String inSituCallQuality) {
            this.inSituCallQuality = inSituCallQuality;
        }
        public void setIncludingInSituObservedData(String includingInSituObservedData) {
            this.includingInSituObservedData = includingInSituObservedData;
        }
        public void setRnaSeqData(String rnaSeqData) {
            this.rnaSeqData = rnaSeqData;
        }
        public void setRnaSeqCallQuality(String rnaSeqCallQuality) {
            this.rnaSeqCallQuality = rnaSeqCallQuality;
        }
        public void setIncludingRnaSeqObservedData(String includingRnaSeqObservedData) {
            this.includingRnaSeqObservedData = includingRnaSeqObservedData;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((includingObservedData == null) ? 0 : includingObservedData.hashCode());
            result = prime * result + ((affymetrixData == null) ? 0 : affymetrixData.hashCode());
            result = prime * result + ((affymetrixCallQuality == null) ? 0 : affymetrixCallQuality.hashCode());
            result = prime * result + ((includingAffymetrixObservedData == null) ? 0 : includingAffymetrixObservedData.hashCode());
            result = prime * result + ((estData == null) ? 0 : estData.hashCode());
            result = prime * result + ((estCallQuality == null) ? 0 : estCallQuality.hashCode());
            result = prime * result + ((includingEstObservedData == null) ? 0 : includingEstObservedData.hashCode());
            result = prime * result + ((inSituData == null) ? 0 : inSituData.hashCode());
            result = prime * result + ((inSituCallQuality == null) ? 0 : inSituCallQuality.hashCode());
            result = prime * result + ((includingInSituObservedData == null) ? 0 : includingInSituObservedData.hashCode());
            result = prime * result + ((rnaSeqData == null) ? 0 : rnaSeqData.hashCode());
            result = prime * result + ((rnaSeqCallQuality == null) ? 0 : rnaSeqCallQuality.hashCode());
            result = prime * result + ((includingRnaSeqObservedData == null) ? 0 : includingRnaSeqObservedData.hashCode());
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
            SingleSpeciesCompleteExprFileBean other = (SingleSpeciesCompleteExprFileBean) obj;
            if (includingObservedData == null) {
                if (other.includingObservedData != null)
                    return false;
            } else if (!includingObservedData.equals(other.includingObservedData))
                return false;
            if (affymetrixData == null) {
                if (other.affymetrixData != null)
                    return false;
            } else if (!affymetrixData.equals(other.affymetrixData))
                return false;
            if (affymetrixCallQuality == null) {
                if (other.affymetrixCallQuality != null)
                    return false;
            } else if (!affymetrixCallQuality.equals(other.affymetrixCallQuality))
                return false;
            if (includingAffymetrixObservedData == null) {
                if (other.includingAffymetrixObservedData != null)
                    return false;
            } else if (!includingAffymetrixObservedData.equals(other.includingAffymetrixObservedData))
                return false;
            if (estData == null) {
                if (other.estData != null)
                    return false;
            } else if (!estData.equals(other.estData))
                return false;
            if (estCallQuality == null) {
                if (other.estCallQuality != null)
                    return false;
            } else if (!estCallQuality.equals(other.estCallQuality))
                return false;
            if (includingEstObservedData == null) {
                if (other.includingEstObservedData != null)
                    return false;
            } else if (!includingEstObservedData.equals(other.includingEstObservedData))
                return false;
            if (inSituData == null) {
                if (other.inSituData != null)
                    return false;
            } else if (!inSituData.equals(other.inSituData))
                return false;
            if (inSituCallQuality == null) {
                if (other.inSituCallQuality != null)
                    return false;
            } else if (!inSituCallQuality.equals(other.inSituCallQuality))
                return false;
            if (includingInSituObservedData == null) {
                if (other.includingInSituObservedData != null)
                    return false;
            } else if (!includingInSituObservedData.equals(other.includingInSituObservedData))
                return false;
            if (rnaSeqData == null) {
                if (other.rnaSeqData != null)
                    return false;
            } else if (!rnaSeqData.equals(other.rnaSeqData))
                return false;
            if (rnaSeqCallQuality == null) {
                if (other.rnaSeqCallQuality != null)
                    return false;
            } else if (!rnaSeqCallQuality.equals(other.rnaSeqCallQuality))
                return false;
            if (includingRnaSeqObservedData == null) {
                if (other.includingRnaSeqObservedData != null)
                    return false;
            } else if (!includingRnaSeqObservedData.equals(other.includingRnaSeqObservedData))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return super.toString() + " - Including observed data()=" + getIncludingObservedData()
                    + " - Affymetrix data: " + getAffymetrixData()
                    + " - Affymetrix call quality: " + getAffymetrixCallQuality()
                    + " - Including affymetrix observed data: " + getIncludingAffymetrixObservedData()
                    + " - EST data: " + getEstData() + " - EST call quality: " + getEstCallQuality()
                    + " - Including EST observed data: " + getIncludingEstObservedData()
                    + " - In situ data: " + getInSituData() + " - In situ call quality: " + getInSituCallQuality() 
                    + " - Including in situ observed data: " + getIncludingInSituObservedData()
                    + " - RNA-seq data: " + getRnaSeqData() + " - RNA-seq call quality: " + getRnaSeqCallQuality()
                    + " - Including RNA-seq observed data: " + getIncludingRnaSeqObservedData();
        }
    }
}
