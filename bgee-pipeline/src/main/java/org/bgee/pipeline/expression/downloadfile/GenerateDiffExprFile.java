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
//TODO: stop using these awful Maps, use a BeanReader/BeanWriter instead, 
//see org.bgee.pipeline.annotations.SimilarityAnnotationUtils
public class GenerateDiffExprFile extends GenerateDownloadFile {

    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(GenerateDiffExprFile.class.getName());

    /**
     * A {@code String} that is the name of the column containing best p-value using Affymetrix, 
     * in the download file.
     */
    public final static String AFFYMETRIX_P_VALUE_COLUMN_NAME = "Affymetrix best supporting p-value";
    /**
     * A {@code String} that is the name of the column containing the number of analysis using 
     * Affymetrix data where the same call is found, in the download file.
     */
    //XXX: maybe we should also provide number of probesets, not only number of analysis
    public final static String AFFYMETRIX_CONSISTENT_DEA_COUNT_COLUMN_NAME = 
            "Affymetrix analysis count supporting Affymetrix call";
    /**
     * A {@code String} that is the name of the column containing the number of analysis using 
     * Affymetrix data where a different call is found, in the download file.
     */
    //XXX: maybe we should also provide number of probesets, not only number of analysis
    public final static String AFFYMETRIX_INCONSISTENT_DEA_COUNT_COLUMN_NAME = 
            "Affymetrix analysis count in conflict with Affymetrix call";
    /**
     * A {@code String} that is the name of the column containing best p-value using RNA-Seq, 
     * in the download file.
     */
    public final static String RNASEQ_P_VALUE_COLUMN_NAME = "RNA-Seq best supporting p-value";
    /**
     * A {@code String} that is the name of the column containing the number of analysis using 
     * RNA-Seq data where the same call is found, in the download file.
     */
    public final static String RNASEQ_CONSISTENT_DEA_COUNT_COLUMN_NAME = 
            "RNA-Seq analysis count supporting RNA-Seq call";
    /**
     * A {@code String} that is the name of the column containing the number of analysis using 
     * RNA-Seq data where a different call is found, in the download file.
     */
    public final static String RNASEQ_INCONSISTENT_DEA_COUNT_COLUMN_NAME = 
            "RNA-Seq analysis count in conflict with RNA-Seq call";

    /**
     * A {@code String} that is the name of the column containing merged differential expressions 
     * from different data types, in the download file.
     */
    public final static String DIFFEXPRESSION_COLUMN_NAME = "Differential expression";

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
    public enum SingleSpDiffExprFileType implements DiffExprFileType {
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
        private SingleSpDiffExprFileType(String stringRepresentation, boolean isSimpleFileType, 
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
        @Override
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
    //TODO: what level of ambiguity of 'no diff expressed' vs. 'not expressed'? no ambiguity? 
    //lower qual?
    //TODO: actually, for weak ambiguity, shouldn't we provide the direction of the diff expression? 
    //There is kind of a "winning" call in case of weak ambiguity
    public enum DiffExpressionData {
        NO_DATA("no data"), NOT_EXPRESSED("not expressed"), OVER_EXPRESSION("over-expression"), 
        UNDER_EXPRESSION("under-expression"), NOT_DIFF_EXPRESSION("no diff expression"), 
        WEAK_AMBIGUITY("low ambiguity"), STRONG_AMBIGUITY("high ambiguity");

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
     * Main method to trigger the generate differential expression TSV download files (simple and 
     * advanced) from Bgee database. Parameters that must be provided in order in {@code args} are: 
     * <ol>
     * <li> a list of NCBI species IDs (for instance, {@code 9606} for human) that will be used to 
     * generate download files, separated by the {@code String} {@link CommandRunner#LIST_SEPARATOR}.
     * If an empty list is provided (see {@link CommandRunner#EMPTY_LIST}), all species 
     * contained in database will be used.
     * <li> a list of files types that will be generated ('diffexpr-anatomy-simple' for 
     * {@link SingleSpDiffExprFileType DIFF_EXPR_ANATOMY_SIMPLE}, 'diffexpr-anatomy-complete' for 
     * {@link SingleSpDiffExprFileType DIFF_EXPR_ANATOMY_COMPLETE}, 'diffexpr-development-simple' for 
     * {@link SingleSpDiffExprFileType DIFF_EXPR_DEVELOPMENT_SIMPLE}, and 'diffexpr-development-complete' for 
     * {@link SingleSpDiffExprFileType DIFF_EXPR_DEVELOPMENT_COMPLETE}), separated by the {@code String} 
     * {@link CommandRunner#LIST_SEPARATOR}. If an empty list is provided 
     * (see {@link CommandRunner#EMPTY_LIST}), all possible file types will be generated.
     * <li>the directory path that will be used to generate download files. 
     * </ol>
     * 
     * @param args          An {@code Array} of {@code String}s containing the requested parameters.
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
        
        GenerateDiffExprFile generator = new GenerateDiffExprFile(
                    CommandRunner.parseListArgument(args[0]), 
                    GenerateDownloadFile.convertToFileTypes(
                            CommandRunner.parseListArgument(args[1]), SingleSpDiffExprFileType.class), 
                    args[2]);
        generator.generateDiffExprFiles();
        log.exit();
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
    public GenerateDiffExprFile(List<String> speciesIds, Set<SingleSpDiffExprFileType> fileTypes, 
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
            Set<SingleSpDiffExprFileType> fileTypes, String directory) 
                    throws IllegalArgumentException {
        super(manager, speciesIds, fileTypes, directory);     
    }

    /**
     * Generate differential expression files, for the species and file types 
     * provided at instantiation, in the directory provided at instantiation.
     * 
     * @throws IOException  If an error occurred while trying to write generated files.
     */
    //TODO: add OMA node ID in complete files
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
            this.fileTypes = EnumSet.allOf(SingleSpDiffExprFileType.class);
        } 
        
        // Retrieve gene names, stage names, anat. entity names, once for all species
        Map<String, String> geneNamesByIds = 
                BgeeDBUtils.getGeneNamesByIds(setSpecies, this.getGeneDAO());
        Map<String, String> stageNamesByIds = 
                BgeeDBUtils.getStageNamesByIds(setSpecies, this.getStageDAO());
        Map<String, String> anatEntityNamesByIds =
                BgeeDBUtils.getAnatEntityNamesByIds(setSpecies, this.getAnatEntityDAO());
    
        // Split file types according to comparison factor 
        Map<ComparisonFactor, Set<SingleSpDiffExprFileType>> factorsToFileTypes = 
                new HashMap<ComparisonFactor, Set<SingleSpDiffExprFileType>>();
        for (FileType fileType: this.fileTypes) {
            Set<SingleSpDiffExprFileType> types = factorsToFileTypes.get(
                    ((SingleSpDiffExprFileType) fileType).getComparisonFactor());
            if (types == null) {
                types = EnumSet.noneOf(SingleSpDiffExprFileType.class);
                factorsToFileTypes.put(((SingleSpDiffExprFileType) fileType).getComparisonFactor(), types);
            }
            types.add((SingleSpDiffExprFileType) fileType);
        }

        // Generate differential expression files, species by species. 
        for (String speciesId: speciesNamesForFilesByIds.keySet()) {
            log.info("Start generating of differential expresion files for the species {}...", 
                    speciesId);
            
            try {
                //generate files grouped by ComparisonFactor (the queries are not the same 
                //depending on the comparison factor)
                for (Set<SingleSpDiffExprFileType> groupedFileTypes: factorsToFileTypes.values()) {
                    this.generateDiffExprFiles(
                        speciesNamesForFilesByIds.get(speciesId), groupedFileTypes, speciesId, 
                        geneNamesByIds, stageNamesByIds, anatEntityNamesByIds);
                }
            } finally {
                //close connection to database between each species, to avoid idle connection reset
                this.getManager().releaseResources();
            }
            log.info("Done generating of differential expresion files for the species {}.", 
                    speciesId);
        }
        
        log.exit();
    }

    /**
     * Generate download files containing differential expression calls 
     * for a single species and a single comparison factor.
     * This method is responsible for retrieving data from the data source, and then 
     * to write them into files. Files are written in directory provided at instantiation.
     * <p>
     * Note that all {@code DiffExprFileType}s in {@code fileTypes} should have the same value 
     * returned by {@code getComparisonFactor}, otherwise an {@code IllegalArgumentException} 
     * is thrown. This is because the queries used are not the same for different 
     * comparison factors. For several comparison factors, you must call this method 
     * several times.
     * 
     * @param fileNamePrefix        A {@code String} to be used as a prefix of the names 
     *                              of the generated files (usually containing the species name). 
     * @param fileTypes             A {@code Set} of {@code DiffExprFileType}s that are the file 
     *                              types to be generated, with equal comparison factors, 
     *                              as returned by {@link SingleSpDiffExprFileType#getComparisonFactor()}.
     * @param speciesId             A {@code String} that is the ID of the species for which 
     *                              files are being generated. 
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
     * @throws IllegalArgumentException If incorrect {@code DiffExprFileType}s provided.
     */
    private void generateDiffExprFiles(String fileNamePrefix, 
            Set<SingleSpDiffExprFileType> fileTypes, String speciesId, Map<String, String> geneNamesByIds, 
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
        for (SingleSpDiffExprFileType fileType: fileTypes) {
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
    
        //TODO test no generated file with absolutely no data in it.
        if (diffExprTOs.isEmpty()) {
            log.trace("No data retrieved for differential expression files for the species {} and file types {}...", 
                    speciesId, fileTypes);
            return;
        }
        //****************************
        // PRODUCE AND WRITE DATA
        //****************************
        log.trace("Start generating and writing file content for species {} and file types {}...", 
                speciesId, fileTypes);
    
        //now, we write all requested differential expression files at once. This way, we will 
        //generate the data only once, and we will not have to store them in memory (the memory  
        //usage could be huge).
        //XXX: well, you do store them in memory here, all CallTOs are loaded into a List.
        //Should we retrieve the DAOResultSet instead, and make the query use an ORDER BY? 
        //(This would require to implement a same mechanism as DAO#setAttributes 
        //for setting ORDER BY clause - it seems the best way to go, rather than storing 
        //results in memory just by laziness of creating this mechanism :p)
        
        //OK, first we allow to store file names, writers, etc, associated to a DiffExprFileType, 
        //for the catch and finally clauses. 
        Map<FileType, String> generatedFileNames = new HashMap<FileType, String>();
        
        //we will write results in temporary files that we will rename at the end 
        //if everything is correct
        String tmpExtension = ".tmp";
        
        //in order to close all writers in a finally clause
        Map<SingleSpDiffExprFileType, ICsvMapWriter> writersUsed = 
                new HashMap<SingleSpDiffExprFileType, ICsvMapWriter>();
        try {
            //**************************
            // OPEN FILES, CREATE WRITERS, WRITE HEADERS
            //**************************
            Map<SingleSpDiffExprFileType, CellProcessor[]> processors = 
                    new HashMap<SingleSpDiffExprFileType, CellProcessor[]>();
            Map<SingleSpDiffExprFileType, String[]> headers = new HashMap<SingleSpDiffExprFileType, String[]>();
            
            for (SingleSpDiffExprFileType fileType: fileTypes) {
                assert fileType.getComparisonFactor().equals(factor);
                
                CellProcessor[] fileTypeProcessors = 
                        this.generateDiffExprFileCellProcessors(fileType);
                processors.put(fileType, fileTypeProcessors);
                String[] fileTypeHeaders = this.generateDiffExprFileHeader(fileType);
                headers.put(fileType, fileTypeHeaders);

                //Create file name
                String fileName = this.formatString(fileNamePrefix + "_" + fileType.getStringRepresentation() 
                        + EXTENSION);
                generatedFileNames.put(fileType, fileName);
                
                //write in temp file
                File file = new File(this.directory, fileName + tmpExtension);
                //override any existing file
                if (file.exists()) {
                    file.delete();
                }
                
                //create writer and write header
                ICsvMapWriter mapWriter = new CsvMapWriter(new FileWriter(file), 
                        Utils.getCsvPreferenceWithQuote(this.generateQuoteMode(fileTypeHeaders)));
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
     * Retrieves all differential expression calls for the requested species from the Bgee data 
     * source.
     * 
     * @param speciesIds            A {@code Set} of {@code String}s that are the IDs of species 
     *                              allowing to filter the expression calls to retrieve.
     * @param factor                A {@code ComparisonFactor}s that is the comparison factor 
     *                              allowing to filter the calls to use.
     * @param generateAdvancedFile  A {@code boolean} defining whether data for an advanced 
     *                              differential expression file are necessary.
     * @return                      A {@code List} of {@code DiffExpressionCallTO}s that are 
     *                              all differential expression calls for the requested species. 
     * @throws DAOException If an error occurred while getting the data from the Bgee data source.
     */
    private List<DiffExpressionCallTO> loadDiffExprCalls(Set<String> speciesIds, 
            ComparisonFactor factor, boolean generateAdvancedFile) throws DAOException {
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
        // one data type with over- or under-expression. 
        if (!generateAdvancedFile) {
            params.setSatisfyAllCallTypeConditions(false);
            params.setIncludeAffymetrixTypes(true);
            params.addAllAffymetrixDiffExprCallTypes(
                    EnumSet.of(DiffExprCallType.OVER_EXPRESSED, DiffExprCallType.UNDER_EXPRESSED));
            params.setIncludeRNASeqTypes(true);
            params.addAllRNASeqDiffExprCallTypes(
                    EnumSet.of(DiffExprCallType.OVER_EXPRESSED, DiffExprCallType.UNDER_EXPRESSED));
        }
        
        List<DiffExpressionCallTO> diffExpressionCallTOs = 
                dao.getDiffExpressionCalls(params).getAllTOs();

        log.debug("Done retrieving global expression calls, {} calls found", 
                diffExpressionCallTOs.size());
        
        return log.exit(diffExpressionCallTOs); 
    }

    /**
     * Generates an {@code Array} of {@code CellProcessor}s used to process 
     * a differential expression TSV file of type {@code DiffExprFileType}.
     * 
     * @param fileType  The {@code DiffExprFileType} of the file to be generated.
     * @return          An {@code Array} of {@code CellProcessor}s used to process 
     *                  a differential expression file.
     */
    //TODO: this should be adapted to the system used elsewhere: providing a String[] 
    //to determine the columns
    private CellProcessor[] generateDiffExprFileCellProcessors(SingleSpDiffExprFileType fileType) {
        log.entry(fileType);
        
        List<Object> data = new ArrayList<Object>();
        for (DiffExpressionData diffExprData: DiffExpressionData.values()) {
            data.add(diffExprData.getStringRepresentation());
        }
        
        List<Object> specificTypeQualities = new ArrayList<Object>();
        specificTypeQualities.add(this.convertDataStateToString(DataState.HIGHQUALITY));
        specificTypeQualities.add(this.convertDataStateToString(DataState.LOWQUALITY));
        specificTypeQualities.add(this.convertDataStateToString(DataState.NODATA));
        
        List<Object> resumeQualities = new ArrayList<Object>();
        resumeQualities.add(this.convertDataStateToString(DataState.HIGHQUALITY));
        resumeQualities.add(this.convertDataStateToString(DataState.LOWQUALITY));
        resumeQualities.add(this.convertDataStateToString(DataState.NODATA));
        
        if (fileType.isSimpleFileType()) {
            return log.exit(new CellProcessor[] { 
                    new StrNotNullOrEmpty(),            // gene ID
                    new NotNull(),                      // gene Name
                    new StrNotNullOrEmpty(),            // anatomical entity ID
                    new StrNotNullOrEmpty(),            // anatomical entity name
                    new StrNotNullOrEmpty(),            // developmental stage ID
                    new StrNotNullOrEmpty(),            // developmental stage name
                    new IsElementOf(data),              // Differential expression
                    new IsElementOf(resumeQualities)}); // Quality
        } 
        
        return log.exit(new CellProcessor[] { 
                new StrNotNullOrEmpty(),                // gene ID
                new NotNull(),                          // gene Name
                new StrNotNullOrEmpty(),                // anatomical entity ID
                new StrNotNullOrEmpty(),                // anatomical entity name
                new StrNotNullOrEmpty(),                // developmental stage ID
                new StrNotNullOrEmpty(),                // developmental stage name
                new IsElementOf(data),                  // Differential expression
                new IsElementOf(resumeQualities),       // Quality
                new IsElementOf(data),                  // Affymetrix data
                new IsElementOf(specificTypeQualities), // Affymetrix call quality
                new DMinMax(0, 1),                      // Best p-value using Affymetrix
                new LMinMax(0, Long.MAX_VALUE),         // Consistent DEA count using Affymetrix
                new LMinMax(0, Long.MAX_VALUE),         // Inconsistent DEA count using Affymetrix
                new IsElementOf(data),                  // RNA-seq data
                new IsElementOf(specificTypeQualities), // RNA-seq call quality
                new DMinMax(0, 1),                      // Best p-value using RNA-Seq
                new LMinMax(0, Long.MAX_VALUE),         // Consistent DEA count using RNA-Seq
                new LMinMax(0, Long.MAX_VALUE)});       // Inconsistent DEA count using RNA-Seq
    }
    
    /**
     * Generates an {@code Array} of {@code String}s used to generate the header of  
     * a differential expression TSV file of type {@code fileType}.
     * 
     * @param fileType  The {@code DiffExprFileType} of the file to be generated.
     * @return          An {@code Array} of {@code String}s used to produce the header.
     */
    private String[] generateDiffExprFileHeader(SingleSpDiffExprFileType fileType) {
        log.entry(fileType);
        
        if (fileType.isSimpleFileType()) {
            return log.exit(new String[] { 
                    GENE_ID_COLUMN_NAME, GENE_NAME_COLUMN_NAME, 
                    ANATENTITY_ID_COLUMN_NAME, ANATENTITY_NAME_COLUMN_NAME,
                    STAGE_ID_COLUMN_NAME, STAGE_NAME_COLUMN_NAME,
                    DIFFEXPRESSION_COLUMN_NAME, QUALITY_COLUMN_NAME});
        } 
        return log.exit(new String[] {
                GENE_ID_COLUMN_NAME, GENE_NAME_COLUMN_NAME, 
                ANATENTITY_ID_COLUMN_NAME, ANATENTITY_NAME_COLUMN_NAME,
                STAGE_ID_COLUMN_NAME, STAGE_NAME_COLUMN_NAME,   
                DIFFEXPRESSION_COLUMN_NAME, QUALITY_COLUMN_NAME, 
                AFFYMETRIX_DATA_COLUMN_NAME, AFFYMETRIX_CALL_QUALITY_COLUMN_NAME,
                AFFYMETRIX_P_VALUE_COLUMN_NAME, AFFYMETRIX_CONSISTENT_DEA_COUNT_COLUMN_NAME, 
                AFFYMETRIX_INCONSISTENT_DEA_COUNT_COLUMN_NAME,
                RNASEQ_DATA_COLUMN_NAME, RNASEQ_CALL_QUALITY_COLUMN_NAME,
                RNASEQ_P_VALUE_COLUMN_NAME, RNASEQ_CONSISTENT_DEA_COUNT_COLUMN_NAME, 
                RNASEQ_INCONSISTENT_DEA_COUNT_COLUMN_NAME});
    }

    /**
     * Generate {@code Array} of {@code booleans} (one per CSV column) indicating 
     * whether each column should be quoted or not.
     *
     * @param headers   An {@code Array} of {@code String}s representing the names of the columns.
     * @return          the {@code Array} of {@code booleans} (one per CSV column) indicating 
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
                case DIFFEXPRESSION_COLUMN_NAME:
                case QUALITY_COLUMN_NAME:
                case AFFYMETRIX_DATA_COLUMN_NAME:
                case AFFYMETRIX_CALL_QUALITY_COLUMN_NAME:
                case AFFYMETRIX_P_VALUE_COLUMN_NAME:
                case AFFYMETRIX_CONSISTENT_DEA_COUNT_COLUMN_NAME:
                case AFFYMETRIX_INCONSISTENT_DEA_COUNT_COLUMN_NAME:
                case RNASEQ_DATA_COLUMN_NAME:
                case RNASEQ_CALL_QUALITY_COLUMN_NAME:
                case RNASEQ_P_VALUE_COLUMN_NAME:
                case RNASEQ_CONSISTENT_DEA_COUNT_COLUMN_NAME:
                case RNASEQ_INCONSISTENT_DEA_COUNT_COLUMN_NAME:
                    quoteMode[i] = false; 
                    break;
                case GENE_NAME_COLUMN_NAME:
                case ANATENTITY_NAME_COLUMN_NAME:
                case STAGE_NAME_COLUMN_NAME:
                    quoteMode[i] = true; 
                    break;
                default:
                    throw log.throwing(new IllegalArgumentException(
                            "Unrecognized header: " + headers[i] + " for diff. expression file."));
            }
        }
        
        return log.exit(quoteMode);
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
            Map<SingleSpDiffExprFileType, ICsvMapWriter> writersUsed, 
            Map<SingleSpDiffExprFileType, CellProcessor[]> processors, 
            Map<SingleSpDiffExprFileType, String[]> headers, List<DiffExpressionCallTO> allCallTOs)
                    throws IOException {
        log.entry(geneNamesByIds, stageNamesByIds, anatEntityNamesByIds, writersUsed, 
                processors, headers, allCallTOs);
    
    
        for (Entry<SingleSpDiffExprFileType, ICsvMapWriter> writerFileType: writersUsed.entrySet()) {
            // We order TOs, according to the values returned by the methods {@code CallTO#getGeneId()}, 
            // {@code CallTO#getAnatEntityId()}, and {@code CallTO#getStageId()}. 
            // we do not copy the List to save memory, so the provided argument will be modified.
            // We do not order in the same way depending on the comparison factor.
            //XXX: we could optimize by sorting only once for a given comparison factor, 
            //and without duplicating the List. But it is not a big deal for now, execution 
            //time is small.
            Boolean orderByAnatomy = null;
            if (writerFileType.getKey().getComparisonFactor().equals(ComparisonFactor.ANATOMY)) {
                //ComparisonFactor = anatomy means that we compared different organs 
                //at a same stage, so we want to group the organs by stage, thus, ordering 
                //by stage first.
                orderByAnatomy = false;
            } else if (writerFileType.getKey().getComparisonFactor().equals(
                    ComparisonFactor.DEVELOPMENT)) {
                //ComparisonFactor = development means that we compared a same organ 
                //at different stages, so we want to group by organs
                orderByAnatomy = true;
            } else {
                throw log.throwing(new AssertionError("Unsupported ComparisonFactor."));
            }
            Collections.sort(allCallTOs, new CallTOComparator(orderByAnatomy));
            
            Map<String, String> row = null;
            try {
                int callCount      = 0;
                int callTotalCount = allCallTOs.size();
                for (DiffExpressionCallTO callTO: allCallTOs) {
                    callCount++;
                    if (log.isDebugEnabled() && callCount % 10000 == 0) {
                        log.debug("Iterating call {} over {}", callCount, callTotalCount);
                    }
                    row = this.generateDiffExprRow(geneNamesByIds, stageNamesByIds, 
                            anatEntityNamesByIds, callTO, writerFileType.getKey());
                    if (row != null) {
                        log.trace("Write row: {} - using writer: {}", row, 
                                writerFileType.getValue());
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
    /**
     * Generate a row to be written in a differential expression download file. This methods will 
     * notably use {@code to} to produce differential expression information, that is different 
     * depending on {@code fileType}. The results are returned as a {@code Map}; it can be 
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
     * {@link SingleSpDiffExprFileType DIFF_EXPR_ANATOMY_COMPLETE} or 
     * {@link SingleSpDiffExprFileType DIFF_EXPR_DEVELOPMENT_COMPLETE}: 
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
     * @throws IllegalArgumentException If the {@code DiffExpressionCallTO} provided 
     *                                  provides inconsistent data.
     */
    private Map<String, String> generateDiffExprRow(Map<String, String> geneNamesByIds, 
            Map<String, String> stageNamesByIds, Map<String, String> anatEntityNamesByIds, 
            DiffExpressionCallTO to, SingleSpDiffExprFileType fileType) throws IllegalArgumentException {
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
            //TODO: OK, now I know why I didn't like the idea of using an Enum 
            //directly from a TO...
            row.put(AFFYMETRIX_CALL_QUALITY_COLUMN_NAME, 
                    //to.getAffymetrixData().getStringRepresentation());
                    this.convertDataStateToString(to.getAffymetrixData()));
            
            row.put(AFFYMETRIX_P_VALUE_COLUMN_NAME, String.valueOf(to.getBestPValueAffymetrix()));
            row.put(AFFYMETRIX_CONSISTENT_DEA_COUNT_COLUMN_NAME, 
                    String.valueOf(to.getConsistentDEACountAffymetrix()));
            row.put(AFFYMETRIX_INCONSISTENT_DEA_COUNT_COLUMN_NAME, 
                    String.valueOf(to.getInconsistentDEACountAffymetrix()));

            row.put(RNASEQ_DATA_COLUMN_NAME,
                    to.getDiffExprCallTypeRNASeq().getStringRepresentation());
            row.put(RNASEQ_CALL_QUALITY_COLUMN_NAME,
                    //to.getRNASeqData().getStringRepresentation());
                    this.convertDataStateToString(to.getRNASeqData()));
            
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
     * @throws IllegalArgumentException If call data are inconsistent (for instance, without any data).
     */
    private boolean addDiffExprCallMergedDataToRow(SingleSpDiffExprFileType fileType, 
            Map<String, String> row, DiffExprCallType affymetrixType, DataState affymetrixQuality, 
            DiffExprCallType rnaSeqType, DataState rnaSeqQuality) throws IllegalArgumentException {
        log.entry(fileType, row, affymetrixType, affymetrixQuality, rnaSeqType, rnaSeqQuality);
        
        DiffExpressionData summary = DiffExpressionData.NO_DATA;
        String quality = this.convertDataStateToString(DataState.NODATA);

        Set<DiffExprCallType> allType = EnumSet.of(affymetrixType, rnaSeqType);

        // Sanity check on data: one call should't be only no data and/or not_expressed data.
        if ((affymetrixType.equals(DiffExprCallType.NOT_EXPRESSED) ||
                affymetrixType.equals(DiffExprCallType.NO_DATA)) &&
            (rnaSeqType.equals(DiffExprCallType.NOT_EXPRESSED) ||
                    rnaSeqType.equals(DiffExprCallType.NO_DATA))) {
            throw log.throwing(new IllegalArgumentException("One call should not be only "+
                    DiffExprCallType.NOT_EXPRESSED.getStringRepresentation() + " and/or " + 
                    DiffExprCallType.NO_DATA.getStringRepresentation()));
        }

        // One call containing over- AND under- expression returns STRONG_AMBIGUITY.
        if ((allType.contains(DiffExprCallType.UNDER_EXPRESSED) &&
                allType.contains(DiffExprCallType.OVER_EXPRESSED))) {
            summary = DiffExpressionData.STRONG_AMBIGUITY;
            quality = this.convertDataStateToString(DataState.NODATA);

        // Both data types are equals or only one is set to 'no data': 
        // we choose the data which is not 'no data'.
        } else if (affymetrixType.equals(rnaSeqType) || allType.contains(DiffExprCallType.NO_DATA)) {
            DiffExprCallType type = affymetrixType;
            if (affymetrixType.equals(DiffExprCallType.NO_DATA)) {
                type = rnaSeqType;
            }
            assert !type.equals(DiffExprCallType.NO_DATA);
            
            //store only quality of data different from NO_DATA
            Set<DataState> allDataQuality = EnumSet.noneOf(DataState.class);
            if (!affymetrixType.equals(DiffExprCallType.NO_DATA)) {
                allDataQuality.add(affymetrixQuality);
            }
            if (!rnaSeqType.equals(DiffExprCallType.NO_DATA)) {
                allDataQuality.add(rnaSeqQuality);
            }
            assert allDataQuality.size() >=1 && allDataQuality.size() <= 2;
            
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
                default:
                    throw log.throwing(new AssertionError(
                            "Both DiffExprCallType are set to 'no data' or 'not expressed'"));
            }
            if (allDataQuality.contains(DataState.HIGHQUALITY)) {
                //TODO: OK, now I know why I didn't like the idea of using an Enum 
                //directly from a TO...
                //quality = DataState.HIGHQUALITY.getStringRepresentation();
                quality = this.convertDataStateToString(DataState.HIGHQUALITY);
            } else {
                //quality = DataState.LOWQUALITY.getStringRepresentation();
                quality = this.convertDataStateToString(DataState.LOWQUALITY);
            }

        // All possible cases where the summary is WEAK_AMBIGUITY:
        // - NOT_DIFF_EXPRESSED and (OVER_EXPRESSED or UNDER_EXPRESSED)
        // - NOT_EXPRESSED and OVER_EXPRESSED
        // - NOT_EXPRESSED and NOT_DIFF_EXPRESSED
        //XXX: actually, I think that there are no NOT_EXPRESSED case inserted, 
        //but it doesn't hurt to keep this code
        } else if ((allType.contains(DiffExprCallType.NOT_DIFF_EXPRESSED) && 
                        (allType.contains(DiffExprCallType.OVER_EXPRESSED) ||
                        allType.contains(DiffExprCallType.UNDER_EXPRESSED))) || 
                   (allType.contains(DiffExprCallType.NOT_EXPRESSED) && 
                        (allType.contains(DiffExprCallType.OVER_EXPRESSED)) || 
                        allType.contains(DiffExprCallType.NOT_DIFF_EXPRESSED))) {
            summary = DiffExpressionData.WEAK_AMBIGUITY;
            quality = this.convertDataStateToString(DataState.NODATA);

        // One call containing NOT_EXPRESSED and UNDER_EXPRESSED returns 
        // UNDER_EXPRESSION with LOWQUALITY 
        //XXX: actually, I think that there are no NOT_EXPRESSED case inserted, 
        //but it doesn't hurt to keep this code
        } else if (allType.contains(DiffExprCallType.NOT_EXPRESSED) && 
                allType.contains(DiffExprCallType.UNDER_EXPRESSED)) {
            summary = DiffExpressionData.UNDER_EXPRESSION;
            //quality = DataState.LOWQUALITY.getStringRepresentation();
            quality = this.convertDataStateToString(DataState.LOWQUALITY);
            
        } else {
            throw log.throwing(new AssertionError("All logical conditions should have been checked."));
        }
        assert !summary.equals(DiffExpressionData.NO_DATA);

        row.put(DIFFEXPRESSION_COLUMN_NAME, summary.getStringRepresentation());
        row.put(QUALITY_COLUMN_NAME, quality);

        return log.exit(true);
    }
    
    /**
     * Convert a {@code org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState} 
     * into a {@code String}. This is because its method {@code getStringRepresentation} 
     * is not convenient for display in diff expression files.
     * 
     * @param dataState A {@code DataState} to be converted.
     * @return          A {@code String} corresponding to {@code dataState}, to be used 
     *                  in diff expression files.
     */
    private String convertDataStateToString(DataState dataState) {
        log.entry(dataState);
        if (DataState.HIGHQUALITY.equals(dataState)) {
            return log.exit("high quality");
        }
        if (DataState.LOWQUALITY.equals(dataState)) {
            return log.exit("low quality");
        }
        return log.exit(GenerateDiffExprFile.NA_VALUE);
    }
}
