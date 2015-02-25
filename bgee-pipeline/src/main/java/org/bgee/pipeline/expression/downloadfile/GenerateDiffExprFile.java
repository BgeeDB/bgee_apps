package org.bgee.pipeline.expression.downloadfile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.ComparisonFactor;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.DiffExprCallType;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallParams;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.pipeline.BgeeDBUtils;
import org.bgee.pipeline.CommandRunner;
import org.bgee.pipeline.Utils;
import org.supercsv.cellprocessor.constraint.DMinMax;
import org.supercsv.cellprocessor.constraint.IsElementOf;
import org.supercsv.cellprocessor.constraint.LMinMax;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;

/**
 * Class used to generate differential expression TSV download files (simple and advanced files) 
 * from the Bgee database. 
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @since Bgee 13
 */
public class GenerateDiffExprFile extends GenerateDownloadFile {

    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(GenerateDiffExprFile.class.getName());

    /**
     * A {@code String} that is the name of the column containing call quality found 
     * with Affymetrix experiment, in the download file.
     */
    public final static String AFFYMETRIX_CALL_QUALITY_COLUMN_NAME = "Affymetrix call quality";

    /**
     * A {@code String} that is the name of the column containing best p-value using Affymetrix, 
     * in the download file.
     */
    public final static String AFFYMETRIX_P_VALUE_COLUMN_NAME = "Best p-value using Affymetrix";

    /**
     * A {@code String} that is the name of the column containing the number of analysis using 
     * Affymetrix data where the same call is found, in the download file.
     */
    public final static String AFFYMETRIX_CONSISTENT_DEA_COUNT_COLUMN_NAME = 
            "Number of analysis using Affymetrix data where the same call is found";

    /**
     * A {@code String} that is the name of the column containing the number of analysis using 
     * Affymetrix data where a different call is found, in the download file.
     */
    public final static String AFFYMETRIX_INCONSISTENT_DEA_COUNT_COLUMN_NAME = 
            "Number of analysis using Affymetrix data where a different call is found";

    /**
     * A {@code String} that is the name of the column containing call quality found 
     * with RNA-Seq experiment, in the download file.
     */
    public final static String RNASEQ_CALL_QUALITY_COLUMN_NAME = "RNA-Seq call quality";

    /**
     * A {@code String} that is the name of the column containing best p-value using RNA-Seq, 
     * in the download file.
     */
    public final static String RNASEQ_P_VALUE_COLUMN_NAME = "Best p-value using RNA-Seq";

    /**
     * A {@code String} that is the name of the column containing the number of analysis using 
     * RNA-Seq data where the same call is found, in the download file.
     */
    public final static String RNASEQ_CONSISTENT_DEA_COUNT_COLUMN_NAME = 
            "Number of analysis using RNA-Seq data where the same call is found";

    /**
     * A {@code String} that is the name of the column containing the number of analysis using 
     * RNA-Seq data where a different call is found, in the download file.
     */
    public final static String RNASEQ_INCONSISTENT_DEA_COUNT_COLUMN_NAME = 
            "Number of analysis using RNA-Seq data where a different call is found";

    /**
     * A {@code String} that is the name of the column containing merged differential expressions 
     * from different data types, in the download file.
     */
    public final static String DIFFEXPRESSION_COLUMN_NAME = "Differential expression";

    /**
     * A {@code String} that is the name of the column containing the merged quality of the call,
     * in the download file.
     */
    public final static String QUALITY_COLUMN_NAME = "Call quality";

    /**
     * A {@code String} that is the value of the cell containing not applicable,
     * in the download file.
     */
    public final static String NA_VALUE = "N/A";

    /**
     * An {@code Enum} used to define the possible differential expression file types to be 
     * generated, as class arguments.
     * <ul>
     * <li>{@code DIFF_EXPR_ANATOMY_SIMPLE}:    differential expression based on comparison 
     *                                              of several anatomical entities at a same 
     *                                              (broad) developmental stage, 
     *                                              in a simple download file.
     * <li>{@code DIFF_EXPR_ANATOMY_COMPLETE}:  differential expression based on comparison 
     *                                              of several anatomical entities at a same 
     *                                              (broad) developmental stage, 
     *                                              in an advanced download file.
     * <li>{@code DIFF_EXPR_DEVELOPMENT_SIMPLE}:          differential expression based on comparison 
     *                                              of a same anatomical entity at different 
     *                                              developmental stages, 
     *                                              in a simple download file.
     * <li>{@code DIFF_EXPR_DEVELOPMENT_COMPLETE}:        differential expression based on comparison 
     *                                              of a same anatomical entity at different 
     *                                              developmental stages, 
     *                                              in an advanced download file.
     * </ul>
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public enum DiffExprFileType implements FileType {
        DIFF_EXPR_ANATOMY_SIMPLE("diffexpr-anatomy-simple", true, 
                ComparisonFactor.ANATOMY), 
        DIFF_EXPR_ANATOMY_COMPLETE("diffexpr-anatomy-complete", false, 
                ComparisonFactor.ANATOMY),
        DIFF_EXPR_DEVELOPMENT_SIMPLE("diffexpr-development-simple", true,
                ComparisonFactor.DEVELOPMENT), 
        DIFF_EXPR_DEVELOPMENT_COMPLETE("diffexpr-development-complete", false,
                ComparisonFactor.DEVELOPMENT);

        /**
         * A {@code String} that can be used to generate names of files of this type.
         */
        private final String stringRepresentation;

        /**
         * A {@code boolean} defining whether this {@code DiffExprFileType} is a simple file type.
         */
        private final boolean simpleFileType;

        /**
         * A {@code ComparisonFactor} defining what is the compared experimental factor that 
         * generated the differential expression calls.
         */
        //XXX: I find it a bit weird to use the ComparisonFactor of DiffExpressionCallTO at this point, 
        //because it is not a class related to a DAO...
        private final ComparisonFactor comparisonFactor;

        /**
         * Constructor providing the {@code String} representation of this {@code DiffExprFileType},
         * a {@code boolean} defining whether this {@code DiffExprFileType} is a simple file type, 
         * and a {@code ComparisonFactor} defining what is the experimental factor compared 
         * that generated the differential expression calls.
         * 
         * @param stringRepresentation  A {@code String} corresponding to this 
         *                              {@code DiffExprFileType}.
         * @param isSimpleFileType      A {@code boolean} defining whether this 
         *                              {@code DiffExprFileType} is a simple file type.
         * @param comparisonFactor      A {@code ComparisonFactor} defining what is the  
         *                              experimental factor compared that generated the  
         *                              differential expression calls.
         */
        private DiffExprFileType(String stringRepresentation, boolean isSimpleFileType, 
                ComparisonFactor comparisonFactor) {
            this.stringRepresentation = stringRepresentation;
            this.simpleFileType = isSimpleFileType;
            this.comparisonFactor = comparisonFactor;
        }

        @Override
        public String getStringRepresentation() {
            return this.stringRepresentation;
        }
        @Override
        public boolean isSimpleFileType() {
            return this.simpleFileType;
        }
        /**
         * @return   A {@code ComparisonFactor} defining what is the experimental factor 
         *           compared that generated the differential expression calls.
         */
        public ComparisonFactor getComparisonFactor() {
            return this.comparisonFactor;
        }
        @Override
        public String toString() {
            return this.getStringRepresentation();
        }
    }

    /**
     * An {@code Enum} used to define, for each data type (Affymetrix and RNA-Seq), 
     * as well as for the summary column, the data state of the call.
     * <ul>
     * <li>{@code NO_DATA}:              means that the call has never been observed 
     *                                   for the related data type.
     * <li>{@code NOT_EXPRESSED}:        means that the related gene was never seen 
     *                                   as 'expressed' in any of the samples used 
     *                                   in the analysis for the related data type, 
     *                                   it was then not tested for differential expression.
     * <li>{@code OVER_EXPRESSION}:      over-expressed calls.
     * <li>{@code UNDER_EXPRESSION}:     under-expressed calls.
     * <li>{@code NOT_DIFF_EXPRESSION}:  means that the gene was tested for differential 
     *                                   expression, but no significant fold change observed.
     * <li>{@code WEAK_AMBIGUITY}:       different data types are not completely coherent: a data 
     *                                   type says over or under-expressed, while the other says 
     *                                   'not differentially expressed'; or a data type says 
     *                                   over-expressed, while the other data type says 'not 
     *                                   expressed'.
     * <li>{@code STRONG_AMBIGUITY}:     different data types are not coherent: a data type says over 
     *                                   or under-expressed, while the other data says the opposite.
     * </ul>
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public enum DiffExpressionData {
        NO_DATA("no data"), NOT_EXPRESSED("not expressed"), OVER_EXPRESSION("over-expression"), 
        UNDER_EXPRESSION("under-expression"), NOT_DIFF_EXPRESSION("no diff expression"), 
        WEAK_AMBIGUITY("weak ambiguity"), STRONG_AMBIGUITY("strong ambiguity");

        private final String stringRepresentation;

        /**
         * Constructor providing the {@code String} representation 
         * of this {@code DiffExpressionData}.
         * 
         * @param stringRepresentation   A {@code String} corresponding to this 
         *                               {@code DiffExpressionData}.
         */
        private DiffExpressionData(String stringRepresentation) {
            this.stringRepresentation = stringRepresentation;
        }

        public String getStringRepresentation() {
            return this.stringRepresentation;
        }

        @Override
        public String toString() {
            return this.getStringRepresentation();
        }
    }
    
    /**
     * Default constructor. 
     */
    //suppress warning as this default constructor should not be used.
    @SuppressWarnings("unused")
    private GenerateDiffExprFile() {
        this(null, null, null, null);
    }
    /**
     * Constructor providing parameters to generate files, and using the default {@code DAOManager}.
     * 
     * @param speciesIds    A {@code List} of {@code String}s that are the IDs of species 
     *                      we want to generate data for. If {@code null} or empty, all species 
     *                      are used.
     * @param fileTypes     A {@code Set} of {@code DiffExprFileType}s that are the types
     *                      of files we want to generate. If {@code null} or empty, 
     *                      all {@code DiffExprFileType}s are generated .
     * @param directory     A {@code String} that is the directory where to store files.
     * @throws IllegalArgumentException If {@code directory} is {@code null} or blank.
     */
    public GenerateDiffExprFile(List<String> speciesIds, Set<DiffExprFileType> fileTypes, 
            String directory) throws IllegalArgumentException {
        this(null, speciesIds, fileTypes, directory);
    }
    /**
     * Constructor providing the {@code MySQLDAOManager} that will be used by 
     * this object to perform queries to the database. This is useful for unit testing.
     * 
     * @param manager   the {@code MySQLDAOManager} to use.
     * @param speciesIds    A {@code List} of {@code String}s that are the IDs of species 
     *                      we want to generate data for. If {@code null} or empty, all species 
     *                      are used.
     * @param fileTypes     A {@code Set} of {@code DiffExprFileType}s that are the types
     *                      of files we want to generate. If {@code null} or empty, 
     *                      all {@code DiffExprFileType}s are generated .
     * @param directory     A {@code String} that is the directory where to store files.
     * @throws IllegalArgumentException If {@code directory} is {@code null} or blank.
     */
    public GenerateDiffExprFile(MySQLDAOManager manager, List<String> speciesIds, 
            Set<DiffExprFileType> fileTypes, String directory) throws IllegalArgumentException {
        super(manager, speciesIds, fileTypes, directory);     
    }

    /**
     * Main method to trigger the generate differential expression TSV download files (simple and 
     * advanced) from Bgee database. Parameters that must be provided in order in {@code args} are: 
     * <ol>
     * <li> a list of NCBI species IDs (for instance, {@code 9606} for human) that will be used to 
     * generate download files, separated by the {@code String} {@link CommandRunner#LIST_SEPARATOR}.
     * If an empty list is provided (see {@link CommandRunner#EMPTY_LIST}), all species 
     * contained in database will be used.
     * <li> a list of files types that will be generated ('diffexpr-anatomy-simple' for 
     * {@link DiffExprFileType DIFF_EXPR_ANATOMY_SIMPLE}, 'diffexpr-anatomy-complete' for 
     * {@link DiffExprFileType DIFF_EXPR_ANATOMY_COMPLETE}, 'diffexpr-development-simple' for 
     * {@link DiffExprFileType DIFF_EXPR_DEVELOPMENT_SIMPLE}, and 'diffexpr-development-complete' for 
     * {@link DiffExprFileType DIFF_EXPR_DEVELOPMENT_COMPLETE}), separated by the {@code String} 
     * {@link CommandRunner#LIST_SEPARATOR}. If an empty list is provided 
     * (see {@link CommandRunner#EMPTY_LIST}), all possible file types will be generated.
     * <li>the directory path that will be used to generate download files. 
     * </ol>
     * 
     * @param args          An {@code Array} of {@code String}s containing the requested parameters.
     * @throws IOException  If an error occurred while trying to write generated files.
     */
    public static void main(String[] args) throws IOException {
        log.entry((Object[]) args);

        //TODO: refactor as compared to GenerateExprFile
        int expectedArgLengthWithoutSpecies = 2;
        int expectedArgLengthWithSpecies = 3;
    
        if (args.length != expectedArgLengthWithSpecies &&
                args.length != expectedArgLengthWithoutSpecies) {
            throw log.throwing(new IllegalArgumentException(
                    "Incorrect number of arguments provided, expected " + 
                    expectedArgLengthWithoutSpecies + " or " + expectedArgLengthWithSpecies + 
                    " arguments, " + args.length + " provided."));
        }

        List<String> speciesIds          = new ArrayList<String>();
        List<String> fileTypeNames       = new ArrayList<String>();
        String directory = null;
              
        if (args.length == expectedArgLengthWithSpecies) {
            speciesIds.addAll(CommandRunner.parseListArgument(args[0]));
            fileTypeNames.addAll(CommandRunner.parseListArgument(args[1])); 
            directory  = args[2];
        } else {
            fileTypeNames.addAll(CommandRunner.parseListArgument(args[0])); 
            directory  = args[1];
        }
        
        // Retrieve DiffExprFileType from String argument
        Set<String> unknownFileTypes = new HashSet<String>();
        Set<DiffExprFileType> filesToBeGenerated = new HashSet<DiffExprFileType>();
        inputFiles: for (String inputFileType: fileTypeNames) {
            for (DiffExprFileType fileType: DiffExprFileType.values()) {
                if (inputFileType.equals(fileType.getStringRepresentation())) {
                    filesToBeGenerated.add(fileType);
                    continue inputFiles;
                }
            }
            //if no correspondence found
            unknownFileTypes.add(inputFileType);
        }
        if (!unknownFileTypes.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "Some file types do not exist: " + unknownFileTypes));
        }
        
        GenerateDiffExprFile generator = 
                new GenerateDiffExprFile(speciesIds, filesToBeGenerated, directory);
        generator.generateDiffExprFiles();
        log.exit();
    }

    /**
     * Generate differential expression files, for the types defined by {@code fileTypes}, 
     * for species defined by {@code speciesIds}, in the directory {@code directory}. 
     * These parameters are provided at instantiation.
     * 
     * @throws IOException  If an error occurred while trying to write generated files.
     */
    public void generateDiffExprFiles() throws IOException { 
        log.entry(this.speciesIds, this.fileTypes, this.directory);
    
        Set<String> setSpecies = new HashSet<String>();
        if (this.speciesIds != null) {
            setSpecies = new HashSet<String>(this.speciesIds);
        }

        // Check user input, retrieve info for generating file names
        // Retrieve species names and IDs (all species names if speciesIds is null or empty)
        Map<String, String> speciesNamesForFilesByIds = 
                checkAndGetLatinNamesBySpeciesIds(setSpecies);
        assert speciesNamesForFilesByIds.size() >= setSpecies.size();
    
        // If no file types are given by user, we set all file types
        if (this.fileTypes == null || this.fileTypes.isEmpty()) {
            this.fileTypes = EnumSet.allOf(DiffExprFileType.class);
        } 
        
        // Retrieve gene names, stage names, anat. entity names, once for all species
        Map<String, String> geneNamesByIds = 
                BgeeDBUtils.getGeneNamesByIds(setSpecies, this.getGeneDAO());
        Map<String, String> stageNamesByIds = 
                BgeeDBUtils.getStageNamesByIds(setSpecies, this.getStageDAO());
        Map<String, String> anatEntityNamesByIds =
                BgeeDBUtils.getAnatEntityNamesByIds(setSpecies, this.getAnatEntityDAO());
    
        // Split file types according to comparison factor 
        Set<DiffExprFileType> anatEntityFileTypes = new HashSet<DiffExprFileType>(), 
                stagesFileTypes = new HashSet<DiffExprFileType>();
        for (FileType fileType : this.fileTypes) {
            if (((DiffExprFileType) fileType).getComparisonFactor().equals(
                    ComparisonFactor.ANATOMY)) {
                anatEntityFileTypes.add((DiffExprFileType) fileType);
            } else if (((DiffExprFileType) fileType).getComparisonFactor().equals(
                    ComparisonFactor.DEVELOPMENT)) {
                stagesFileTypes.add((DiffExprFileType) fileType);
            } else {
                throw log.throwing(new AssertionError(
                        "All logical conditions should have been checked."));
            }
        }

        // Generate differential expression files, species by species. 
        for (String speciesId: speciesNamesForFilesByIds.keySet()) {
            log.info("Start generating of differential expresion files for the species {}...", 
                    speciesId);
            
            if (!anatEntityFileTypes.isEmpty()) {
                this.generateDiffExprFilesForOneSpecies(
                        speciesNamesForFilesByIds.get(speciesId), anatEntityFileTypes, speciesId, 
                        geneNamesByIds, stageNamesByIds, anatEntityNamesByIds);
            }
            if (!stagesFileTypes.isEmpty()) {
                this.generateDiffExprFilesForOneSpecies(
                        speciesNamesForFilesByIds.get(speciesId), stagesFileTypes, speciesId, 
                        geneNamesByIds, stageNamesByIds, anatEntityNamesByIds);
            }

            //close connection to database between each species, to avoid idle connection reset
            this.getManager().releaseResources();
            log.info("Done generating of differential expresion files for the species {}.", 
                    speciesId);
        }
        
        log.exit();
    }

    /**
     * Retrieves all differential expression calls for the requested species from the Bgee data 
     * source.
     * 
     * @param speciesIds            A {@code Set} of {@code String}s that are the IDs of species 
     *                              allowing to filter the expression calls to retrieve.
     * @param factor                A {@code ComparisonFactor}s that is the comparison factor 
     *                              allowing to filter the calls to use.
     * @param generateAdvancedFile  A {@code boolean} defining whether data for an advanced 
     *                              differential expression file are necessary.
     * @return                      A {@code Map} where keys are {@code String}s that are gene IDs, 
     *                              the associated values being a {@code Set} of 
     *                              {@code DiffExpressionCallTO}s, associated to this gene, that are 
     *                              all differential expression calls for the requested species. 
     * @throws DAOException If an error occurred while getting the data from the Bgee data source.
     */
    private List<DiffExpressionCallTO> loadDiffExprCalls(
            Set<String> speciesIds, ComparisonFactor factor, boolean generateAdvancedFile) 
                    throws DAOException {
        log.entry(speciesIds, factor, generateAdvancedFile);
        
        log.debug("Start retrieving differential expression calls for the species IDs {}...", 
                speciesIds);

        DiffExpressionCallDAO dao = this.getDiffExpressionCallDAO();
        //do not retrieve the internal diff. expression IDs
        dao.setAttributes(EnumSet.complementOf(EnumSet.of(DiffExpressionCallDAO.Attribute.ID)));
    
        DiffExpressionCallParams params = new DiffExpressionCallParams();
        params.addAllSpeciesIds(speciesIds);
        params.setComparisonFactor(factor);
        // If the advanced file won't be generated, we do not retrieve calls without at least
        // one data type with over- or under-expressed. 
        if (!generateAdvancedFile) {
            params.setSatisfyAllCallTypeConditions(false);
            params.setIncludeAffymetrixTypes(true);
            params.addAllAffymetrixDiffExprCallTypes(
                    EnumSet.of(DiffExprCallType.OVER_EXPRESSED, DiffExprCallType.UNDER_EXPRESSED));
            params.setIncludeRNASeqTypes(true);
            params.addAllRNASeqDiffExprCallTypes(
                    EnumSet.of(DiffExprCallType.OVER_EXPRESSED, DiffExprCallType.UNDER_EXPRESSED));
        }
        
        List<DiffExpressionCallTO> diffExpressionCallTOs = dao.getDiffExpressionCalls(params).getAllTOs();

        log.debug("Done retrieving global expression calls, {} calls found", 
                diffExpressionCallTOs.size());
        
        return log.exit(diffExpressionCallTOs); 
    }

    /**
     * Generate download files (simple and/or advanced) containing differential expression calls.
     * This method is responsible for retrieving data from the data source, and then 
     * to write them into files. Files are written in directory provided at instantiation.
     * 
     * @param fileNamePrefix        A {@code String} to be used as a prefix of the names 
     *                              of the generated files. 
     * @param fileTypes             An {@code Set} of {@code DiffExprFileType}s that are the file 
     *                              types to be generated.
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
     * @throws IOException              If an error occurred while trying to write the 
     *                                  {@code outputFile}.
     * @throws IllegalArgumentException If all provided {@code DiffExprFileType}s do not have the   
     *                                  same {@code ComparisonFactor}.
     */
    private void generateDiffExprFilesForOneSpecies(String fileNamePrefix, 
            Set<DiffExprFileType> fileTypes, String speciesId, Map<String, String> geneNamesByIds, 
            Map<String, String> stageNamesByIds, Map<String, String> anatEntityNamesByIds)
                    throws IOException, IllegalArgumentException {
        log.entry(this.directory, fileNamePrefix, fileTypes, speciesId, 
                geneNamesByIds, stageNamesByIds, anatEntityNamesByIds);
        
        log.debug("Start generating download files for the species {} and file types {}...", 
                speciesId, fileTypes);

        if (fileTypes == null || fileTypes.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No provided file types to be generated"));
        }
        
        // We check that all file types have the same comparison factor and retrieve informations: 
        // comparison factor and if there is an advanced file to be generated.
        boolean generateCompleteFile = false; 
        ComparisonFactor factor = null;
        for (DiffExprFileType fileType: fileTypes) {
            if (factor == null) {
                factor = fileType.getComparisonFactor(); 
            } else if (!fileType.getComparisonFactor().equals(factor)) {
                throw log.throwing(new IllegalArgumentException(
                        "All file types do not have the same comparison factor: " + fileTypes));
            }
            if (!fileType.isSimpleFileType()) {
                generateCompleteFile = true;
            }
        }
        assert factor != null;

        //********************************
        // RETRIEVE DATA FROM DATA SOURCE
        //********************************
        Set<String> speciesFilter = new HashSet<String>();
        speciesFilter.add(speciesId);

        //Load differential expression calls.
        List<DiffExpressionCallTO> diffExprTOs =  
                this.loadDiffExprCalls(speciesFilter, factor, generateCompleteFile);

        log.trace("Done retrieving data for differential expression files for the species {}.", 
                speciesId);

        //****************************
        // PRODUCE AND WRITE DATA
        //****************************
        log.trace("Start generating and writing file content for species {} and file types {}...", 
                speciesId, fileTypes);

        //now, we write all requested differential expression files at once. This way, we will 
        //generate the data only once, and we will not have to store them in memory (the memory  
        //usage could be huge).
        //XXX: well, you do store them in memory here :p All CallTOs are loaded into a List.
        
        //OK, first we allow to store file names, writers, etc, associated to a DiffExprFileType, 
        //for the catch and finally clauses. 
        Map<FileType, String> generatedFileNames = new HashMap<FileType, String>();
        
        //we will write results in temporary files that we will rename at the end 
        //if everything is correct
        String tmpExtension = ".tmp";
        
        //in order to close all writers in a finally clause
        Map<DiffExprFileType, ICsvMapWriter> writersUsed = new HashMap<DiffExprFileType, ICsvMapWriter>();
        try {
            //**************************
            // OPEN FILES, CREATE WRITERS, WRITE HEADERS
            //**************************
            Map<DiffExprFileType, CellProcessor[]> processors = 
                    new HashMap<DiffExprFileType, CellProcessor[]>();
            Map<DiffExprFileType, String[]> headers = new HashMap<DiffExprFileType, String[]>();
            
            for (DiffExprFileType fileType: fileTypes) {
                assert fileType.getComparisonFactor().equals(factor);
                
                CellProcessor[] fileTypeProcessors = 
                        this.generateDiffExprFileCellProcessors(fileType);
                processors.put(fileType, fileTypeProcessors);
                String[] fileTypeHeaders = this.generateDiffExprFileHeader(fileType);
                headers.put(fileType, fileTypeHeaders);

                //Create file name
                String fileName = fileNamePrefix + "_" + fileType.getStringRepresentation() 
                        + EXTENSION;
                generatedFileNames.put(fileType, fileName);
                
                //write in temp file
                File file = new File(this.directory, fileName + tmpExtension);
                //override any existing file
                if (file.exists()) {
                    file.delete();
                }
                
                //create writer and write header
                ICsvMapWriter mapWriter = new CsvMapWriter(new FileWriter(file), Utils.TSVCOMMENTED);
                mapWriter.writeHeader(fileTypeHeaders);
                writersUsed.put(fileType, mapWriter);
            }
            
            //****************************
            // WRITE ROWS
            //****************************
            // Now, we write the rows in all files
            this.writeDiffExprRows(geneNamesByIds, stageNamesByIds, anatEntityNamesByIds,
                    writersUsed, processors, headers, diffExprTOs);

        } catch (Exception e) {
            this.deleteTempFiles(generatedFileNames, tmpExtension);
            throw e;
        } finally {
            for (ICsvMapWriter writer: writersUsed.values()) {
                writer.close();
            }
        }
        //now, if everything went fine, we rename the temporary files
        this.renameTempFiles(generatedFileNames, tmpExtension);

        log.exit();
    }

    /**
     * Generates an {@code Array} of {@code CellProcessor}s used to process 
     * a differential expression TSV file of type {@code DiffExprFileType}.
     * 
     * @param fileType  The {@code DiffExprFileType} of the file to be generated.
     * @return          An {@code Array} of {@code CellProcessor}s used to process 
     *                  a differential expression file.
     * @throw IllegalArgumentException  If {@code fileType} is not managed by this method.
     */
    private CellProcessor[] generateDiffExprFileCellProcessors(DiffExprFileType fileType) 
            throws IllegalArgumentException {
        log.entry(fileType);
        
        List<Object> data = new ArrayList<Object>();
        for (DiffExpressionData diffExprData: DiffExpressionData.values()) {
            data.add(diffExprData.getStringRepresentation());
        }
        
        List<Object> specificTypeQualities = new ArrayList<Object>();
        specificTypeQualities.add(DataState.HIGHQUALITY.getStringRepresentation());
        specificTypeQualities.add(DataState.LOWQUALITY.getStringRepresentation());
        specificTypeQualities.add(DataState.NODATA.getStringRepresentation());
        
        List<Object> resumeQualities = new ArrayList<Object>();
        resumeQualities.add(DataState.HIGHQUALITY.getStringRepresentation());
        resumeQualities.add(DataState.LOWQUALITY.getStringRepresentation());
        resumeQualities.add(GenerateDiffExprFile.NA_VALUE);
        
        if (fileType.isSimpleFileType()) {
            return log.exit(new CellProcessor[] { 
                    new StrNotNullOrEmpty(),      // gene ID
                    new NotNull(),                // gene Name
                    new StrNotNullOrEmpty(),      // developmental stage ID
                    new StrNotNullOrEmpty(),      // developmental stage name
                    new StrNotNullOrEmpty(),      // anatomical entity ID
                    new StrNotNullOrEmpty(),      // anatomical entity name
                    new IsElementOf(data),        // Differential expression
                    new IsElementOf(resumeQualities)});     // Quality
        } 
        
        return log.exit(new CellProcessor[] { 
                new StrNotNullOrEmpty(),        // gene ID
                new NotNull(),                  // gene Name
                new StrNotNullOrEmpty(),        // developmental stage ID
                new StrNotNullOrEmpty(),        // developmental stage name
                new StrNotNullOrEmpty(),        // anatomical entity ID
                new StrNotNullOrEmpty(),        // anatomical entity name
                new IsElementOf(data),          // Affymetrix data
                new IsElementOf(specificTypeQualities),     // Affymetrix call quality
                new DMinMax(0, 1),              // Best p-value using Affymetrix
                new LMinMax(0, Long.MAX_VALUE), // Consistent DEA count using Affymetrix
                new LMinMax(0, Long.MAX_VALUE), // Inconsistent DEA count using Affymetrix
                new IsElementOf(data),          // RNA-seq data
                new IsElementOf(specificTypeQualities),     // RNA-seq call quality
                new DMinMax(0, 1),              // Best p-value using RNA-Seq
                new LMinMax(0, Long.MAX_VALUE), // Consistent DEA count using RNA-Seq
                new LMinMax(0, Long.MAX_VALUE), // Inconsistent DEA count using RNA-Seq
                new IsElementOf(data),          // Differential expression
                new IsElementOf(resumeQualities)});         // Quality
    }
    
    /**
     * Generates an {@code Array} of {@code String}s used to generate the header of  
     * a differential expression TSV file of type {@code fileType}.
     * 
     * @param fileType  The {@code DiffExprFileType} of the file to be generated.
     * @return          An {@code Array} of {@code String}s used to produce the header.
     * @throw IllegalArgumentException  If {@code fileType} is not managed by this method.
     */
    private String[] generateDiffExprFileHeader(DiffExprFileType fileType) 
            throws IllegalArgumentException {
        log.entry(fileType);
        
        if (fileType.isSimpleFileType()) {
            return log.exit(new String[] { 
                    GENE_ID_COLUMN_NAME, GENE_NAME_COLUMN_NAME, 
                    STAGE_ID_COLUMN_NAME, STAGE_NAME_COLUMN_NAME,
                    ANATENTITY_ID_COLUMN_NAME, ANATENTITY_NAME_COLUMN_NAME,
                    DIFFEXPRESSION_COLUMN_NAME, QUALITY_COLUMN_NAME});
        } 
        return log.exit(new String[] {
                GENE_ID_COLUMN_NAME, GENE_NAME_COLUMN_NAME, 
                STAGE_ID_COLUMN_NAME, STAGE_NAME_COLUMN_NAME,   
                ANATENTITY_ID_COLUMN_NAME, ANATENTITY_NAME_COLUMN_NAME,
                AFFYMETRIX_DATA_COLUMN_NAME, AFFYMETRIX_CALL_QUALITY_COLUMN_NAME,
                AFFYMETRIX_P_VALUE_COLUMN_NAME, AFFYMETRIX_CONSISTENT_DEA_COUNT_COLUMN_NAME, 
                AFFYMETRIX_INCONSISTENT_DEA_COUNT_COLUMN_NAME,
                RNASEQ_DATA_COLUMN_NAME, RNASEQ_CALL_QUALITY_COLUMN_NAME,
                RNASEQ_P_VALUE_COLUMN_NAME, RNASEQ_CONSISTENT_DEA_COUNT_COLUMN_NAME, 
                RNASEQ_INCONSISTENT_DEA_COUNT_COLUMN_NAME,
                DIFFEXPRESSION_COLUMN_NAME, QUALITY_COLUMN_NAME});
    }

    /**
     * Generate a row to be written in a differential expression download file. This methods will 
     * notably use {@code to} to produce differential expression information, that is different 
     * depending on {@code DiffExprFileType}. The results are returned as a {@code Map}; it can be 
     * {@code null} if the {@code DiffExpressionCallTO} provided do not allow to generate information 
     * to be included in the file of the given {@code DiffExprFileType}.
     * <p>
     * <ul>
     * <li>information that will be generated in any case: entries with keys equal to 
     * {@link #GENE_ID_COLUMN_NAME}, {@link #GENE_NAME_COLUMN_NAME}, 
     * {@link #STAGE_ID_COLUMN_NAME}, {@link #STAGE_NAME_COLUMN_NAME}, 
     * {@link #ANATENTITY_ID_COLUMN_NAME}, {@link #ANATENTITY_NAME_COLUMN_NAME}, 
     * {@link #DIFFEXPRESSION_COLUMN_NAME}, {@link #QUALITY_COLUMN_NAME}.
     * <li>information generated for files of the type 
     * {@link DiffExprFileType DIFF_EXPR_ANATOMY_COMPLETE} or 
     * {@link DiffExprFileType DIFF_EXPR_DEVELOPMENT_COMPLETE}: 
     * entries with keys equal to {@link #AFFYMETRIX_DATA_COLUMN_NAME}, 
     * {@link #AFFYMETRIX_CALL_QUALITY_COLUMN_NAME}, {@link #AFFYMETRIX_P_VALUE_COLUMN_NAME}, 
     * {@link #AFFYMETRIX_CONSISTENT_DEA_COUNT_COLUMN_NAME}, 
     * {@link #AFFYMETRIX_INCONSISTENT_DEA_COUNT_COLUMN_NAME}, {@link #RNASEQ_DATA_COLUMN_NAME}, 
     * {@link #RNASEQ_CALL_QUALITY_COLUMN_NAME}, {@link #RNASEQ_P_VALUE_COLUMN_NAME}, 
     * {@link #RNASEQ_CONSISTENT_DEA_COUNT_COLUMN_NAME}
     * {@link #RNASEQ_INCONSISTENT_DEA_COUNT_COLUMN_NAME}.
     * </ul>
     * 
     * @param geneNamesByIds        A {@code Map} where keys are {@code String}s corresponding to 
     *                              gene IDs, the associated values being {@code String}s 
     *                              corresponding to gene names. 
     * @param stageNamesByIds       A {@code Map} where keys are {@code String}s corresponding to 
     *                              stage IDs, the associated values being {@code String}s 
     *                              corresponding to stage names. 
     * @param anatEntityNamesByIds  A {@code Map} where keys are {@code String}s corresponding to 
     *                              anatomical entity IDs, the associated values being 
     *                              {@code String}s corresponding to anatomical entity names.
     * @param to                    A {@code DiffExpressionCallTO} that is the call to be written.
     * @param fileType              The {@code DiffExprFileType} defining which type of file should 
     *                              be generated.
     * @return                      A {@code Map} containing the generated information. {@code null} 
     *                              if no information should be generated for the provided 
     *                              {@code fileType}.
     */
    private Map<String, String> generateDiffExprRow(Map<String, String> geneNamesByIds, 
            Map<String, String> stageNamesByIds, Map<String, String> anatEntityNamesByIds, 
            DiffExpressionCallTO to, DiffExprFileType fileType) {
        log.entry(geneNamesByIds, stageNamesByIds, anatEntityNamesByIds, to, fileType);
        
        Map<String, String> row = new HashMap<String, String>();
    
        // ********************************
        // Set IDs and names
        // ********************************    
        this.addIdsAndNames(row, to.getGeneId(), geneNamesByIds.get(to.getGeneId()), 
                to.getAnatEntityId(), anatEntityNamesByIds.get(to.getAnatEntityId()),
                to.getStageId(), stageNamesByIds.get(to.getStageId()));
    
        // ********************************
        // Set simple file columns
        // ********************************
        boolean dataAdded = this.addDiffExprCallMergedDataToRow(fileType, row, 
                to.getDiffExprCallTypeAffymetrix(), to.getAffymetrixData(), 
                to.getDiffExprCallTypeRNASeq(), to.getRNASeqData());
    
        if (!dataAdded) {
            return log.exit(null);
        }

        // ********************************
        // Set advance file columns
        // ********************************
        if (!fileType.isSimpleFileType()) {
            row.put(AFFYMETRIX_DATA_COLUMN_NAME, 
                    to.getDiffExprCallTypeAffymetrix().getStringRepresentation());
            row.put(AFFYMETRIX_CALL_QUALITY_COLUMN_NAME, 
                    to.getAffymetrixData().getStringRepresentation());
            row.put(AFFYMETRIX_P_VALUE_COLUMN_NAME, String.valueOf(to.getBestPValueAffymetrix()));
            row.put(AFFYMETRIX_CONSISTENT_DEA_COUNT_COLUMN_NAME, 
                    String.valueOf(to.getConsistentDEACountAffymetrix()));
            row.put(AFFYMETRIX_INCONSISTENT_DEA_COUNT_COLUMN_NAME, 
                    String.valueOf(to.getInconsistentDEACountAffymetrix()));

            row.put(RNASEQ_DATA_COLUMN_NAME,
                    to.getDiffExprCallTypeRNASeq().getStringRepresentation());
            row.put(RNASEQ_CALL_QUALITY_COLUMN_NAME,
                    to.getRNASeqData().getStringRepresentation());
            row.put(RNASEQ_P_VALUE_COLUMN_NAME, String.valueOf(to.getBestPValueRNASeq()));
            row.put(RNASEQ_CONSISTENT_DEA_COUNT_COLUMN_NAME,
                    String.valueOf(to.getConsistentDEACountRNASeq()));
            row.put(RNASEQ_INCONSISTENT_DEA_COUNT_COLUMN_NAME, 
                    String.valueOf(to.getInconsistentDEACountRNASeq()));
        }

        return log.exit(row);
    }

    /**
     * Add to the provided {@code row} merged {@code DataState}s and qualities.
     * <p>
     * The provided {@code Map} will be modified.
     *
     * @param fileType          The {@code DiffExprFileType} defining which type of file should be 
     *                          generated.
     * @param row               A {@code Map} where keys are {@code String}s that are column names,
     *                          the associated values being a {@code String} that is the value 
     *                          for the call. 
     * @param affymetrixType    A {@code DiffExprCallType} that is the differential expression call
     *                          type of Affymetrix data to be merged with {@code rnaSeqType}.
     * @param affymetrixQuality A {@code DataState} that is the Affymetrix call quality to be merged
     *                          with {@code rnaSeqQuality}.
     * @param rnaSeqType        A {@code DiffExprCallType} that is the differential expression call
     *                          type of RNA-Seq data to be merged with {@code affymetrixType}.
     * @param rnaSeqQuality     A {@code DataState} that is the RNA-Seq call quality to be merged
     *                          with {@code affymetrixQuality}.
     * @return                  A {@code boolean} that is {@code true} if data added to {@code row}.
     * @throws IllegalStateException If call data are inconsistent (for instance, without any data).
     */
    private boolean addDiffExprCallMergedDataToRow(DiffExprFileType fileType, 
            Map<String, String> row, DiffExprCallType affymetrixType, DataState affymetrixQuality, 
            DiffExprCallType rnaSeqType, DataState rnaSeqQuality) throws IllegalStateException {
        log.entry(fileType, row, affymetrixType, affymetrixQuality, rnaSeqType, rnaSeqQuality);
        
        DiffExpressionData summary = DiffExpressionData.NO_DATA;
        String quality = GenerateDiffExprFile.NA_VALUE;

        Set<DiffExprCallType> allType = EnumSet.of(affymetrixType, rnaSeqType);
        Set<DataState> allDataQuality = EnumSet.of(affymetrixQuality, rnaSeqQuality);

        // Sanity check on data: one call should't be only no data and/or not_expressed data.
        if ((affymetrixType.equals(DiffExprCallType.NOT_EXPRESSED) ||
                affymetrixType.equals(DiffExprCallType.NO_DATA)) &&
            (rnaSeqType.equals(DiffExprCallType.NOT_EXPRESSED) ||
                    rnaSeqType.equals(DiffExprCallType.NO_DATA))) {
            throw log.throwing(new IllegalStateException("One call should not be only "+
                    DiffExprCallType.NOT_EXPRESSED.getStringRepresentation() + " and/or " + 
                    DiffExprCallType.NO_DATA.getStringRepresentation()));
        }

        // One call containing over- AND under- expression returns STRONG_AMBIGUITY.
        if ((allType.contains(DiffExprCallType.UNDER_EXPRESSED) &&
                allType.contains(DiffExprCallType.OVER_EXPRESSED))) {
            summary = DiffExpressionData.STRONG_AMBIGUITY;
            quality = GenerateDiffExprFile.NA_VALUE;

        // Both data types are equals or only one is set to 'no data': 
        // we choose the data which is not 'no data'.
        } else if (affymetrixType.equals(rnaSeqType) || allType.contains(DiffExprCallType.NO_DATA)) {
            DiffExprCallType type = affymetrixType;
            if (affymetrixType.equals(DiffExprCallType.NO_DATA)) {
                type = rnaSeqType;
            }
            assert !type.equals(DiffExprCallType.NO_DATA);
            
            switch (type) {
                case OVER_EXPRESSED: 
                    summary = DiffExpressionData.OVER_EXPRESSION;
                    break;
                case UNDER_EXPRESSED: 
                    summary = DiffExpressionData.UNDER_EXPRESSION;
                    break;
                case NOT_DIFF_EXPRESSED: 
                    summary = DiffExpressionData.NOT_DIFF_EXPRESSION;
                    // We don't write 'not expressed' calls in simple file, we need to check it
                    // again because when simple file is generated in same time as advanced file  
                    // we do not filter these calls when retrieving calls from database.
                    if (fileType.isSimpleFileType()) {
                        return log.exit(false);
                    }
                    break;
                case NOT_EXPRESSED:
                    summary = DiffExpressionData.NOT_EXPRESSED;
                    break;
                default:
                    throw log.throwing(new AssertionError(
                            "Both DiffExprCallType are set to 'no data'"));
            }
            if (allDataQuality.contains(DataState.HIGHQUALITY)) {
                quality = DataState.HIGHQUALITY.getStringRepresentation();
            } else {
                quality = DataState.LOWQUALITY.getStringRepresentation();
            }

        // All possible cases where the summary is WEAK_AMBIGUITY:
        // - NOT_DIFF_EXPRESSED and (OVER_EXPRESSED or UNDER_EXPRESSED)
        // - NOT_EXPRESSED and OVER_EXPRESSED
        // - NOT_EXPRESSED and NOT_DIFF_EXPRESSED
        } else if ((allType.contains(DiffExprCallType.NOT_DIFF_EXPRESSED) && 
                        (allType.contains(DiffExprCallType.OVER_EXPRESSED) ||
                        allType.contains(DiffExprCallType.UNDER_EXPRESSED))) || 
                   (allType.contains(DiffExprCallType.NOT_EXPRESSED) && 
                        allType.contains(DiffExprCallType.OVER_EXPRESSED)) ||
                   (allType.contains(DiffExprCallType.NOT_EXPRESSED) &&
                        allType.contains(DiffExprCallType.NOT_DIFF_EXPRESSED))) {
            summary = DiffExpressionData.WEAK_AMBIGUITY;
            quality = GenerateDiffExprFile.NA_VALUE;

        // One call containing NOT_EXPRESSED and UNDER_EXPRESSED returns 
        // UNDER_EXPRESSION with LOWQUALITY 
        } else if (allType.contains(DiffExprCallType.NOT_EXPRESSED) && 
                allType.contains(DiffExprCallType.UNDER_EXPRESSED)) {
            summary = DiffExpressionData.UNDER_EXPRESSION;
            quality = DataState.LOWQUALITY.getStringRepresentation();
            
        } else {
            throw log.throwing(new AssertionError("All logical conditions should have been checked."));
        }
        assert !summary.equals(DiffExpressionData.NO_DATA);

        row.put(DIFFEXPRESSION_COLUMN_NAME, summary.getStringRepresentation());
        row.put(QUALITY_COLUMN_NAME, quality);

        return log.exit(true);
    }
    
    /**
     * Generate rows to be written and write them in a file. This methods will notably use 
     * {@code callTOs} to produce information, that is different depending on {@code fileType}.  
     * Note that order of elements in {{@code callTOs} will be modified as a result of 
     * the call to this method.
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
     * @param processors            A {@code Map} where keys are {@code DiffExprFileType}s 
     *                              corresponding to which type of file should be generated, the 
     *                              associated values being an {@code Array} of 
     *                              {@code CellProcessor}s used to process a file.
     * @param headers               A {@code Map} where keys are {@code DiffExprFileType}s 
     *                              corresponding to which type of file should be generated, the 
     *                              associated values being an {@code Array} of {@code String}s 
     *                              used to produce the header.
     * @param writersUsed           A {@code Map} where keys are {@code DiffExprFileType}s 
     *                              corresponding to which type of file should be generated, the 
     *                              associated values being {@code ICsvMapWriter}s corresponding to 
     *                              the writers.
     * @param allCallTOs            A {@code List} of {@code DiffExpressionCallTO}s that are 
     *                              calls to be written. Elements in this {@code List} will be 
     *                              re-ordered.
     * @throws IOException  If an error occurred while trying to write the {@code outputFile}.
     */
    private void writeDiffExprRows(Map<String, String> geneNamesByIds, 
            Map<String, String> stageNamesByIds, Map<String, String> anatEntityNamesByIds, 
            Map<DiffExprFileType, ICsvMapWriter> writersUsed, 
            Map<DiffExprFileType, CellProcessor[]> processors, 
            Map<DiffExprFileType, String[]> headers, List<DiffExpressionCallTO> allCallTOs)
                    throws IOException {
        log.entry(geneNamesByIds, stageNamesByIds, anatEntityNamesByIds, writersUsed, 
                processors, headers, allCallTOs);

        // We order TOs, according to the values returned by the methods {@code CallTO#getGeneId()}, 
        // {@code CallTO#getAnatEntityId()}, and {@code CallTO#getStageId()}. 
        // we do not copy the List to save memory, so the provided argument will be modified.
        Collections.sort(allCallTOs, new CallTOComparator());

        for (Entry<DiffExprFileType, ICsvMapWriter> writerFileType: writersUsed.entrySet()) {
            Map<String, String> row = null;
            try {
                int callCount = 0;
                for (DiffExpressionCallTO callTO: allCallTOs) {
                    callCount++;
                    if (log.isDebugEnabled() && callCount % 2000 == 0) {
                        log.debug("Iterating call {} over {}", callCount, allCallTOs.size());
                    }
                    row = this.generateDiffExprRow(geneNamesByIds, stageNamesByIds, 
                            anatEntityNamesByIds, callTO, writerFileType.getKey());
                    if (row != null) {
                        log.trace("Write row: {} - using writer: {}", row, writerFileType.getValue());
                        writerFileType.getValue().write(row, 
                                headers.get(writerFileType.getKey()), 
                                processors.get(writerFileType.getKey()));
                    }
                }
            } catch (IllegalArgumentException e) {
                //any IllegalArgumentException thrown by generateExprRow should come 
                //from a problem in the data, thus from an illegal state
                throw log.throwing(new IllegalStateException("Incorrect data state", e));
            }
        }
        log.exit();
    }
}
