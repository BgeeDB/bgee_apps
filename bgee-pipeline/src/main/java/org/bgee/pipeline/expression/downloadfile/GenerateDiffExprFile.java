package org.bgee.pipeline.expression.downloadfile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.ComparisonFactor;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.DiffExprCallType;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTOResultSet;
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

//TODO: class javadoc
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
     * <li>{@code DIFF_EXPR_ANAT_ENTITY_SIMPLE}:   differential expression across anat. entities 
     *                                             in a simple download file.
     * <li>{@code DIFF_EXPR_ANAT_ENTITY_COMPLETE}: differential expression across anat. entities 
     *                                             in an advanced download file.
     * <li>{@code DIFF_EXPR_STAGE_SIMPLE}:         differential expression across developmental 
     *                                             stages in a simple download file.
     * <li>{@code DIFF_EXPR_STAGE_COMPLETE}:       differential expression across developmental 
     *                                             stages in an advanced download file.
     * </ul>
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public enum DiffExprFileType implements FileType {
        DIFF_EXPR_ANAT_ENTITY_SIMPLE("diffexpr-anat-entity-simple", true, 
                ComparisonFactor.ANATOMY), 
        DIFF_EXPR_ANAT_ENTITY_COMPLETE("diffexpr-anat-entity-complete", false, 
                ComparisonFactor.ANATOMY),
        DIFF_EXPR_STAGE_SIMPLE("diffexpr-stage-simple", true,
                ComparisonFactor.DEVELOPMENT), 
        DIFF_EXPR_STAGE_COMPLETE("diffexpr-stage-complete", false,
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
         * A {@code boolean} defining whether this {@code DiffExprFileType} is a differential 
         * expression file type.
         */
        //TODO: fix javadoc
        //XXX: I find it a bit weird to use the ComparisonFactor of DiffExpressionCallTO at this point, 
        //but maybe it's just me...
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
    public GenerateDiffExprFile() {
        this(null);
    }

    /**
     * Constructor providing the {@code MySQLDAOManager} that will be used by 
     * this object to perform queries to the database. This is useful for unit testing.
     * 
     * @param manager   the {@code MySQLDAOManager} to use.
     */
    public GenerateDiffExprFile(MySQLDAOManager manager) {
        super(manager);
    }

    /**
     * Main method to trigger the generate differential expression TSV download files (simple and 
     * advanced) from Bgee database. Parameters that must be provided in order in {@code args} are: 
     * <ol>
     * <li> a list of NCBI species IDs (for instance, {@code 9606} for human) that will be used to 
     * generate download files, separated by the {@code String} {@link CommandRunner#LIST_SEPARATOR}.
     * If an empty list is provided (see {@link CommandRunner#EMPTY_LIST}), all species 
     * contained in database will be used.
     * <li> a list of files types that will be generated ('diffexpr-anat-entity-simple' for 
     * {@link DiffExprFileType DIFF_EXPR_ANAT_ENTITY_SIMPLE}, 'diffexpr-anat-entity-complete' for 
     * {@link DiffExprFileType DIFF_EXPR_ANAT_ENTITY_COMPLETE}, 'diffexpr-stage-simple' for 
     * {@link DiffExprFileType DIFF_EXPR_STAGE_SIMPLE}, and 'diffexpr-stage-complete' for 
     * {@link DiffExprFileType DIFF_EXPR_STAGE_COMPLETE}), separated by the {@code String} 
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

        List<String> speciesIds = new ArrayList<String>();
        Set<String> fileTypes = new HashSet<String>();
        String directory = null;
        
        // Retrieve arguments and initialize speciesIds, fileTypes, and directory
        GenerateDownloadFile.getClassParameters(args, speciesIds, fileTypes, directory);
        
        // Retrieve DiffExprFileType from String argument
        Set<String> unknownFileTypes = new HashSet<String>();
        Set<DiffExprFileType> filesToBeGenerated = new HashSet<DiffExprFileType>();
        inputFiles: for (String inputFileType: fileTypes) {
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
        
        GenerateDiffExprFile generator = new GenerateDiffExprFile();
        generator.generateDiffExprFiles(speciesIds, filesToBeGenerated, directory);
        log.exit();
    }

    /**
     * Generate differential expression files, for the types defined by {@code fileTypes}, 
     * for species defined by {@code speciesIds}, in the directory {@code directory}.
     * 
     * @param speciesIds     A {@code List} of {@code String}s that are the IDs of species for 
     *                       which files are generated.
     * @param fileTypes      A {@code Set} of {@code FileType}s containing file types to be generated.
     * @param directory      A {@code String} that is the directory path directory to store the 
     *                       generated files. 
     * @throws IOException   If an error occurred while trying to write generated files.
     */
    public void generateDiffExprFiles(List<String> speciesIds, Set<DiffExprFileType> fileTypes, 
            String directory) throws IOException { 
        log.entry(speciesIds, fileTypes, directory);
    
        Set<String> setSpecies = new HashSet<String>();
        if (speciesIds != null) {
            setSpecies = new HashSet<String>(speciesIds);
        }
    
        // Check user input, retrieve info for generating file names
        Map<String, String> speciesNamesForFilesByIds = 
                checkAndGetLatinNamesBySpeciesIds(setSpecies);
        if (speciesIds == null || speciesIds.isEmpty()) {
            speciesIds = new ArrayList<String>(speciesNamesForFilesByIds.keySet());
        }
    
        // If no file types are given by user, we set all file types
        if (fileTypes == null || fileTypes.isEmpty()) {
            fileTypes = EnumSet.allOf(DiffExprFileType.class);
        } 
        
        // Retrieve gene names, stage names, anat. entity names, once for all species
        Map<String, String> geneNamesByIds = 
                BgeeDBUtils.getGeneNamesByIds(setSpecies, this.getGeneDAO());
        Map<String, String> stageNamesByIds = 
                BgeeDBUtils.getStageNamesByIds(setSpecies, this.getStageDAO());
        Map<String, String> anatEntityNamesByIds =
                BgeeDBUtils.getAnatEntityNamesByIds(setSpecies, this.getAnatEntityDAO());
    
        Set<FileType> anatEntityFileTypes = new HashSet<FileType>(), 
                stagesFilteTypes = new HashSet<FileType>();

        for (DiffExprFileType fileType : fileTypes) {
            //TODO: do this based on the ComparisonFactor attribute, not based on names
            if (fileType.equals(DiffExprFileType.DIFF_EXPR_ANAT_ENTITY_SIMPLE) ||
                    fileType.equals(DiffExprFileType.DIFF_EXPR_ANAT_ENTITY_COMPLETE)) {
                anatEntityFileTypes.add(fileType);
            } else if (fileType.equals(DiffExprFileType.DIFF_EXPR_STAGE_SIMPLE) ||
                    fileType.equals(DiffExprFileType.DIFF_EXPR_STAGE_COMPLETE)) {
                stagesFilteTypes.add(fileType);
            } else {
                throw log.throwing(new AssertionError(
                        "All logical conditions should have been checked."));

            }
        }

        // Generate differential expression files, species by species. 
        for (String speciesId: speciesIds) {
            log.info("Start generating of differential expresion files for the species {}...", 
                    speciesId);
            //TODO: actually, all of these could be done without using anatEntityFileTypes 
            //and stagesFilteTypes, simply by using the ComparisonFactor
            if (anatEntityFileTypes.size() > 0) {
                this.generateDiffExprFilesForOneSpecies(
                        directory, speciesNamesForFilesByIds.get(speciesId), 
                        fileTypes, speciesId, geneNamesByIds, stageNamesByIds, 
                        anatEntityNamesByIds, ComparisonFactor.ANATOMY);
            }
            if (stagesFilteTypes.size() > 0) {
                this.generateDiffExprFilesForOneSpecies(
                        directory, speciesNamesForFilesByIds.get(speciesId), 
                        fileTypes, speciesId, geneNamesByIds, stageNamesByIds, 
                        anatEntityNamesByIds, ComparisonFactor.DEVELOPMENT);
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
     * source, grouped by gene ID.
     * <p>
     * The returned {@code DiffExpressionCallTO}s have no ID set, to be able 
     * to compare calls based on gene, stage and anatomical entity IDs.
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
    private Map<String, Set<DiffExpressionCallTO>> loadDiffExprCallsByGeneId(
            Set<String> speciesIds, ComparisonFactor factor, boolean generateAdvancedFile) 
                    throws DAOException {
        log.entry(speciesIds, factor, generateAdvancedFile);
        
        log.debug("Start retrieving differential expression calls for the species IDs {}...", 
                speciesIds);
        Map<String, Set<DiffExpressionCallTO>> callsByGeneIds = 
                new HashMap<String, Set<DiffExpressionCallTO>>();
        DiffExpressionCallDAO dao = this.getDiffExpressionCallDAO();
        dao.setAttributes(EnumSet.complementOf(EnumSet.of(DiffExpressionCallDAO.Attribute.ID)));
    
        DiffExpressionCallParams params = new DiffExpressionCallParams();
        params.addAllSpeciesIds(speciesIds);
        params.setComparisonFactor(factor);
        //if the advanced file won't be generated we don't retrieve 'not expressed' calls 
        if (!generateAdvancedFile) {
            params.setSatisfyAllCallTypeConditions(true);
            params.setIncludeAffymetrixTypes(false);
            params.addAllAffymetrixDiffExprCallTypes(
                    EnumSet.of(DiffExprCallType.NOT_EXPRESSED, DiffExprCallType.NO_DATA));
            params.setIncludeRNASeqTypes(false);
            params.addAllRNASeqDiffExprCallTypes(
                    EnumSet.of(DiffExprCallType.NOT_EXPRESSED, DiffExprCallType.NO_DATA));
        }
        
        int i = 0;
        try (DiffExpressionCallTOResultSet rsDiffExpr = dao.getDiffExpressionCalls(params)) {
            while (rsDiffExpr.next()) {
                DiffExpressionCallTO to = rsDiffExpr.getTO();
                log.trace("Iterating DiffExpressionCallTO: {}", to);
                Set<DiffExpressionCallTO> diffExprTOs = callsByGeneIds.get(to.getGeneId());
                if (diffExprTOs == null) {
                    log.trace("Create new map key: {}", to.getGeneId());
                    diffExprTOs = new HashSet<DiffExpressionCallTO>();
                    callsByGeneIds.put(to.getGeneId(), diffExprTOs);
                }
                diffExprTOs.add(to);
                i++;
            }
        }
    
        log.debug("Done retrieving global expression calls, {} calls found", i);
        return log.exit(callsByGeneIds); 
    }

    /**
     * Generate download files (simple and/or advanced) containing differential expression calls.
     * This method is responsible for retrieving data from the data source, and then 
     * to write them into files.
     * 
     * @param directory             A {@code String} that is the directory to store  
     *                              the generated files. 
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
     * @throws IOException  If an error occurred while trying to write the {@code outputFile}.
     */
    private void generateDiffExprFilesForOneSpecies(String directory, String fileNamePrefix, 
            Set<DiffExprFileType> fileTypes, String speciesId, Map<String, String> geneNamesByIds, 
            Map<String, String> stageNamesByIds, Map<String, String> anatEntityNamesByIds,
            ComparisonFactor factor) 
                    throws IOException {
        log.entry(directory, fileNamePrefix, fileTypes, speciesId, 
                geneNamesByIds, stageNamesByIds, anatEntityNamesByIds);
        
        log.debug("Start generating download files for the species {} and file types {}...", 
                speciesId, fileTypes);

        //********************************
        // RETRIEVE DATA FROM DATA SOURCE
        //********************************
        Set<String> speciesFilter = new HashSet<String>();
        speciesFilter.add(speciesId);

        //Load differential expression calls grouped by geneIds.
        boolean generateCompleteFile = 
                fileTypes.contains(DiffExprFileType.DIFF_EXPR_ANAT_ENTITY_COMPLETE) || 
                fileTypes.contains(DiffExprFileType.DIFF_EXPR_STAGE_COMPLETE);
        
        Map<String, Set<DiffExpressionCallTO>> diffExprTOsByGeneIds =  
                this.loadDiffExprCallsByGeneId(speciesFilter, factor, generateCompleteFile);

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
                if (!fileType.getComparisonFactor().equals(factor)) {
                    continue;
                }
                CellProcessor[] fileTypeProcessors = null;
                String[] fileTypeHeaders = null;
                
                fileTypeProcessors = this.generateDiffExprFileCellProcessors(fileType);
                processors.put(fileType, fileTypeProcessors);
                fileTypeHeaders = this.generateDiffExprFileHeader(fileType);
                headers.put(fileType, fileTypeHeaders);

                //Create file name
                String fileName = fileNamePrefix + "_" + 
                        fileType.getStringRepresentation() + EXTENSION;
                generatedFileNames.put(fileType, fileName);
                
                //write in temp file
                File file = new File(directory, fileName + tmpExtension);
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
            //first, we retrieve and order all unique gene IDs, to have rows in files 
            //ordered by gene IDs
            Set<String> geneIds = new HashSet<String>(diffExprTOsByGeneIds.keySet());
            List<String> orderedGeneIds = new ArrayList<String>(geneIds);
            Collections.sort(orderedGeneIds);

            //now, we generate and write data one gene at a time to not overload memory.
            int geneCount = 0;
            for (String geneId: orderedGeneIds) {
                geneCount++;
                if (log.isDebugEnabled() && geneCount % 2000 == 0) {
                    log.debug("Iterating gene {} over {}", geneCount, orderedGeneIds.size());
                }

                //remove calls from Map to free some memory
                Set<CallTO> allCallTOs = new HashSet<CallTO>(diffExprTOsByGeneIds.remove(geneId));
                
                //and, we compute and write the rows in all files
                this.writeDiffExprRows(geneNamesByIds, stageNamesByIds, anatEntityNamesByIds,
                        writersUsed, processors, headers, geneId, allCallTOs);

            }
        } catch (Exception e) {
            this.deleteTempFiles(directory, generatedFileNames, tmpExtension);
            throw e;
        } finally {
            for (ICsvMapWriter writer: writersUsed.values()) {
                writer.close();
            }
        }
        //now, if everything went fine, we rename the temporary files
        this.renameTempFiles(directory, generatedFileNames, tmpExtension);

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
        
        if (!fileType.equals(DiffExprFileType.DIFF_EXPR_ANAT_ENTITY_SIMPLE) && 
                !fileType.equals(DiffExprFileType.DIFF_EXPR_ANAT_ENTITY_COMPLETE) && 
                !fileType.equals(DiffExprFileType.DIFF_EXPR_STAGE_SIMPLE) && 
                !fileType.equals(DiffExprFileType.DIFF_EXPR_STAGE_COMPLETE)) {
            throw log.throwing(new IllegalArgumentException("File type not handled " +
                    "by this method: " + fileType));
        }

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
     * notably use {@code callTOs} to produce differential expression information, that is different 
     * depending on {@code DiffExprFileType}. The results are returned as a {@code Map}; it can be 
     * {@code null} if the {@code callTOs} provided do not allow to generate information to be 
     * included in the file of the given {@code DiffExprFileType}.
     * <p>
     * {@code callTOs} must all have the same values returned by {@link CallTO#getGeneId()}, 
     * {@link CallTO#getAnatEntityId()}, {@link CallTO#getStageId()}, and they must be 
     * respectively equal to {@code geneId}, {@code anatEntityId}, {@code stageId}.
     * <p>
     * <ul>
     * <li>information that will be generated in any case: entries with keys equal to 
     * {@link #GENE_ID_COLUMN_NAME}, {@link #GENE_NAME_COLUMN_NAME}, 
     * {@link #STAGE_ID_COLUMN_NAME}, {@link #STAGE_NAME_COLUMN_NAME}, 
     * {@link #ANATENTITY_ID_COLUMN_NAME}, {@link #ANATENTITY_NAME_COLUMN_NAME}, 
     * {@link #DIFFEXPRESSION_COLUMN_NAME}, {@link #QUALITY_COLUMN_NAME}.
     * <li>information generated for files of the type 
     * {@link DiffExprFileType DIFF_EXPR_ANAT_ENTITY_COMPLETE} or 
     * {@link DiffExprFileType DIFF_EXPR_STAGE_COMPLETE}: 
     * entries with keys equal to {@link #AFFYMETRIX_DATA_COLUMN_NAME}, 
     * {@link #AFFYMETRIX_CALL_QUALITY_COLUMN_NAME}, {@link #AFFYMETRIX_P_VALUE_COLUMN_NAME}, 
     * {@link #AFFYMETRIX_CONSISTENT_DEA_COUNT_COLUMN_NAME}, 
     * {@link #AFFYMETRIX_INCONSISTENT_DEA_COUNT_COLUMN_NAME}, {@link #RNASEQ_DATA_COLUMN_NAME}, 
     * {@link #RNASEQ_CALL_QUALITY_COLUMN_NAME}, {@link #RNASEQ_P_VALUE_COLUMN_NAME}, 
     * {@link #RNASEQ_CONSISTENT_DEA_COUNT_COLUMN_NAME}
     * {@link #RNASEQ_INCONSISTENT_DEA_COUNT_COLUMN_NAME}.
     * </ul>
     * 
     * @param geneId            A {@code String} that is the ID of the gene considered.
     * @param geneName          A {@code String} that is the name of the gene considered.
     * @param stageId           A {@code String} that is the ID of the stage considered.
     * @param stageName         A {@code String} that is the name of the stage considered.
     * @param anatEntityId      A {@code String} that is the ID of the anatomical entity 
     *                          considered.
     * @param anatEntityName    A {@code String} that is the name of the anatomical entity 
     *                          considered.
     * @param to                A {@code DiffExpressionCallTO} that is the call to be written.
     * @param fileType          The {@code DiffExprFileType} defining which type of file should be 
     *                          generated.
     * @return                  A {@code Map} containing the generated information. {@code null} 
     *                          if no information should be generated for the provided 
     *                          {@code fileType}.
     */
    private Map<String, String> generateDiffExprRow(String geneId, String geneName, 
            String stageId, String stageName, String anatEntityId, String anatEntityName, 
            DiffExpressionCallTO to, DiffExprFileType fileType) {
        log.entry(geneId, geneName, stageId, stageName, anatEntityId, anatEntityName, to, fileType);
        
        Map<String, String> row = new HashMap<String, String>();
    
        // ********************************
        // Set IDs and names
        // ********************************        
        this.addIdsAndNames(row, geneId, geneName, anatEntityId, anatEntityName, stageId, stageName);
    
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

        if ((affymetrixType.equals(DiffExprCallType.NOT_EXPRESSED) ||
                affymetrixType.equals(DiffExprCallType.NO_DATA)) &&
            (rnaSeqType.equals(DiffExprCallType.NOT_EXPRESSED) ||
                    rnaSeqType.equals(DiffExprCallType.NO_DATA))) {
            throw log.throwing(new IllegalStateException("One call could't be only "+
                    DiffExprCallType.NOT_EXPRESSED.getStringRepresentation() + " and/or " + 
                    DiffExprCallType.NO_DATA.getStringRepresentation()));
        }

        if ((allType.contains(DiffExprCallType.UNDER_EXPRESSED) &&
                allType.contains(DiffExprCallType.OVER_EXPRESSED)) ||
            (allType.contains(DiffExprCallType.NOT_EXPRESSED) &&
                allType.contains(DiffExprCallType.NOT_DIFF_EXPRESSED))) {
            summary = DiffExpressionData.STRONG_AMBIGUITY;
            quality = GenerateDiffExprFile.NA_VALUE;

        } else if (affymetrixType.equals(rnaSeqType) || allType.contains(DiffExprCallType.NO_DATA)) {
            DiffExprCallType type = affymetrixType;
            if (affymetrixType.equals(DiffExprCallType.NO_DATA)) {
                type = rnaSeqType;
            }
            switch (type) {
                case OVER_EXPRESSED: 
                    summary = DiffExpressionData.OVER_EXPRESSION;
                    break;
                case UNDER_EXPRESSED: 
                    summary = DiffExpressionData.UNDER_EXPRESSION;
                    break;
                case NOT_DIFF_EXPRESSED: 
                    summary = DiffExpressionData.NOT_DIFF_EXPRESSION;
                    //we don't write 'not expressed' calls in simple file, we need to 
                    //check it again because when simple file is generated in same time as 
                    //advance file we don't filter these calls when retrieving calls from database 
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

        } else if ((allType.contains(DiffExprCallType.NOT_DIFF_EXPRESSED) && 
                        (allType.contains(DiffExprCallType.OVER_EXPRESSED) ||
                        allType.contains(DiffExprCallType.UNDER_EXPRESSED))) || 
                   (allType.contains(DiffExprCallType.NOT_EXPRESSED) && 
                        allType.contains(DiffExprCallType.OVER_EXPRESSED))) {
            summary = DiffExpressionData.WEAK_AMBIGUITY;
            quality = GenerateDiffExprFile.NA_VALUE;

        } else if (allType.contains(DiffExprCallType.NOT_EXPRESSED) && 
                allType.contains(DiffExprCallType.UNDER_EXPRESSED)) {
            summary = DiffExpressionData.UNDER_EXPRESSION;
            quality = DataState.LOWQUALITY.getStringRepresentation();
        }

        row.put(DIFFEXPRESSION_COLUMN_NAME, summary.getStringRepresentation());
        row.put(QUALITY_COLUMN_NAME, quality);

        return log.exit(true);
    }
    
    /**
     * Generate rows to be written and write them in a file. This methods will notably use 
     * {@code callTOs} to produce information, that is different depending on {@code fileType}.  
     * <p>
     * {@code callTOs} must all have the same values returned by {@link CallTO#getGeneId()}, 
     * {@link CallTO#getAnatEntityId()}, {@link CallTO#getStageId()}, and they must be 
     * respectively equal to {@code geneId}, {@code anatEntityId}, {@code stageId}.
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
     * @param allCallTOs            A {@code Set} of {@code CallTO}s that are calls to be written. 
     * @throws IOException  If an error occurred while trying to write the {@code outputFile}.
     */
    private void writeDiffExprRows(Map<String, String> geneNamesByIds, 
            Map<String, String> stageNamesByIds, Map<String, String> anatEntityNamesByIds, 
            Map<DiffExprFileType, ICsvMapWriter> writersUsed, 
            Map<DiffExprFileType, CellProcessor[]> processors, 
            Map<DiffExprFileType, String[]> headers, String geneId, Set<CallTO> allCallTOs)
                    throws IOException {
        log.entry(geneNamesByIds, stageNamesByIds, anatEntityNamesByIds, writersUsed, 
                processors, headers, geneId, allCallTOs);

        SortedMap<CallTO, Collection<CallTO>> groupedSortedCallTOs = 
                this.groupAndOrderByGeneAnatEntityStage(allCallTOs);

        for (Entry<CallTO, Collection<CallTO>> callGroup : groupedSortedCallTOs.entrySet()) {
            if (!geneId.equals(callGroup.getKey().getGeneId())) {
                throw log.throwing(new IllegalStateException("Grouped calls should " +
                        "have the gene ID " + geneId + ": " + callGroup));
            }
            String stageId = callGroup.getKey().getStageId();
            String anatEntityId = callGroup.getKey().getAnatEntityId();

            for (Entry<DiffExprFileType, ICsvMapWriter> writerFileType: writersUsed.entrySet()) {
                Map<String, String> row = null;
                try {
                    if (callGroup.getValue().size() > 1) {
                        throw log.throwing(new IllegalStateException(
                                "It should not have several calls for on triplet"));                                    
                    }
                    for (CallTO callTO: callGroup.getValue()) {
                        row = this.generateDiffExprRow(geneId, geneNamesByIds.get(geneId), 
                                stageId, stageNamesByIds.get(stageId), 
                                anatEntityId, anatEntityNamesByIds.get(anatEntityId), 
                                (DiffExpressionCallTO)callTO, writerFileType.getKey());
                    }
                } catch (IllegalArgumentException e) {
                    //any IllegalArgumentException thrown by generateExprRow should come 
                    //from a problem in the data, thus from an illegal state
                    throw log.throwing(new IllegalStateException("Incorrect data state", e));
                }

                if (row != null) {
                    log.trace("Write row: {} - using writer: {}", 
                            row, writerFileType.getValue());
                    writerFileType.getValue().write(row, 
                            headers.get(writerFileType.getKey()), 
                            processors.get(writerFileType.getKey()));
                }
            }
        }
        log.exit();
    }
}
