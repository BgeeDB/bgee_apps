package org.bgee.pipeline.expression.downloadfile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SimAnnotToAnatEntityTO;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.ComparisonFactor;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
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
 * TODO Javadoc
 *
 * @author 	Valentine Rech de Laval
 * @version Bgee 13
 * @since 	Bgee 13
 */
public class GenerateMultiSpeciesDiffExprFile   extends GenerateDownloadFile 
                                                implements GenerateMultiSpeciesDownloadFile {

    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(
            GenerateMultiSpeciesDiffExprFile.class.getName());
    
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
     * A {@code String} that is the ID of the common ancestor taxon we want to into account. 
     * If {@code null} or empty, TODO  to be decided.
     */
    private String taxonId;

    /**
     * A {@code String} that is the prefix that will be used to generate multi-species file names.
     * If {@code null} or empty, TODO  to be decided.
     */
    private String groupPrefix;
    
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
     * An {@code Enum} used to define the possible differential expression in multi-species file
     * types to be generated.
     * <ul>
     * <li>{@code MULTI_DIFF_EXPR_ANATOMY_SIMPLE}:
     *          differential expression in multi-species based on comparison of several anatomical 
     *          entities at a same (broad) developmental stage, in a simple download file.
     * <li>{@code MULTI_DIFF_EXPR_ANATOMY_COMPLETE}:
     *          differential expression in multi-species based on comparison of several anatomical 
     *          entities at a same (broad) developmental stage, in a complete download file.
     * <li>{@code MULTI_DIFF_EXPR_DEVELOPMENT_SIMPLE}:
     *          differential expression in multi-species based on comparison of a same anatomical 
     *          entity at different developmental stages, in a simple download file.
     * <li>{@code MULTI_DIFF_EXPR_DEVELOPMENT_COMPLETE}:    
     *          differential expression in multi-species based on comparison of a same anatomical  
     *          entity at different developmental stages, in a complete download file
     * </ul>
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public enum MultiSpDiffExprFileType implements DiffExprFileType {
        MULTI_DIFF_EXPR_ANATOMY_SIMPLE(
                "multi-expr-anatomy-simple", true, ComparisonFactor.ANATOMY), 
        MULTI_DIFF_EXPR_ANATOMY_COMPLETE(
                "multi-expr-anatomy-complete", false, ComparisonFactor.ANATOMY),
        MULTI_DIFF_EXPR_DEVELOPMENT_SIMPLE(
                "multi-expr-anatomy-simple", true, ComparisonFactor.DEVELOPMENT), 
        MULTI_DIFF_EXPR_DEVELOPMENT_COMPLETE(
                "multi-expr-anatomy-complete", false, ComparisonFactor.DEVELOPMENT);

        /**
         * A {@code String} that can be used to generate names of files of this type.
         */
        private final String stringRepresentation;
        
        /**
         * A {@code boolean} defining whether this {@code MultiSpDiffExprFileType} is a simple 
         * file type
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
         * Constructor providing the {@code String} representation of this 
         * {@code MultiSpDiffExprFileType}, a {@code boolean} defining whether this 
         * {@code MultiSpDiffExprFileType} is a simple file type, and a 
         * {@code ComparisonFactor} defining what is the experimental factor compared 
         * that generated the differential expression calls.
         */
        private MultiSpDiffExprFileType(String stringRepresentation, boolean simpleFileType,
                ComparisonFactor comparisonFactor) {
            this.stringRepresentation = stringRepresentation;
            this.simpleFileType = simpleFileType;
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
     * Default constructor. 
     */
    //suppress warning as this default constructor should not be used.
    @SuppressWarnings("unused")
    private GenerateMultiSpeciesDiffExprFile() {
        this(null, null, null, null, null);
    }

    /**
     * Constructor providing parameters to generate files, using the default {@code DAOManager}.
     * 
     * @param speciesIds    A {@code List} of {@code String}s that are the IDs of species 
     *                      we want to generate data for. If {@code null} or empty, all species 
     *                      are used.
     * @param taxonId       A {@code String} that is the ID of the common ancestor taxon
     *                      we want to into account. If {@code null} or empty, TODO to be decided.
     * @param fileTypes     A {@code Set} of {@code MultiSpDiffExprFileType}s that are the types
     *                      of files we want to generate. If {@code null} or empty, 
     *                      all {@code MultiSpDiffExprFileType}s are generated.
     * @param directory     A {@code String} that is the directory where to store files.
     * @param groupPrefix   A {@code String} that is the prefix of the group we want to use 
     *                      for files names. If {@code null} or empty, TODO  to be decided.
     * @throws IllegalArgumentException If {@code directory} is {@code null} or blank.
     */
    public GenerateMultiSpeciesDiffExprFile(List<String> speciesIds, String taxonId, 
            Set<MultiSpDiffExprFileType> fileTypes, String directory, String groupPrefix) 
                    throws IllegalArgumentException {
        this(null, speciesIds, taxonId, fileTypes, directory, groupPrefix);
    }

    /**
     * Constructor providing parameters to generate files, and the {@code MySQLDAOManager} that will  
     * be used by this object to perform queries to the database. This is useful for unit testing.
     * 
     * @param manager       the {@code MySQLDAOManager} to use.
     * @param speciesIds    A {@code List} of {@code String}s that are the IDs of species 
     *                      we want to generate data for. If {@code null} or empty, all species 
     *                      are used.
     * @param taxonId       A {@code String} that is the ID of the common ancestor taxon
     *                      we want to into account. If {@code null} or empty, TODO to be decided.
     * @param fileTypes     A {@code Set} of {@code MultiSpDiffExprFileType}s that are the types
     *                      of files we want to generate. If {@code null} or empty, 
     *                      all {@code MultiSpDiffExprFileType}s are generated.
     * @param directory     A {@code String} that is the directory where to store files.
     * @throws IllegalArgumentException If {@code directory} is {@code null} or blank.
     */
    public GenerateMultiSpeciesDiffExprFile(MySQLDAOManager manager, List<String> speciesIds, 
            String taxonId, Set<MultiSpDiffExprFileType> fileTypes, String directory, 
            String groupPrefix) throws IllegalArgumentException {
        super(manager, speciesIds, fileTypes, directory);
        this.taxonId = taxonId;
        this.groupPrefix = groupPrefix;
    }
    /**
     * Main method to trigger the generate multi-species differential expression TSV download files 
     * (simple and advanced) from Bgee database. Parameters that must be provided in order in 
     * {@code args} are: 
     * <ol>
     * <li> a list of NCBI species IDs (for instance, {@code 9606} for human) that will be used to 
     * generate download files, separated by the {@code String} {@link CommandRunner#LIST_SEPARATOR}.
     * If an empty list is provided (see {@link CommandRunner#EMPTY_LIST}), TODO to be decided.
     * <li>a taxon ID (for instance, {@code 40674} for Mammalia) that will be used to 
     * generate download files. If an empty list is provided (see {@link CommandRunner#EMPTY_LIST}),
     * TODO To be decided.
     * <li> a list of files types that will be generated ('multi-diffexpr-anatomy-simple' for 
     * {@link MultiSpDiffExprFileType MULTI_DIFF_EXPR_ANATOMY_SIMPLE}, 
     * 'multi-diffexpr-anatomy-complete' for 
     * {@link MultiSpDiffExprFileType MULTI_DIFF_EXPR_ANATOMY_COMPLETE}, 
     * 'multi-diffexpr-development-simple' for 
     * {@link MultiSpDiffExprFileType MULTI_DIFF_EXPR_DEVELOPMENT_SIMPLE}, and 
     * 'multi-diffexpr-development-complete' for 
     * {@link MultiSpDiffExprFileType MULTI_DIFF_EXPR_DEVELOPMENT_COMPLETE}), separated by the 
     * {@code String} {@link CommandRunner#LIST_SEPARATOR}. If an empty list is provided 
     * (see {@link CommandRunner#EMPTY_LIST}), all possible file types will be generated.
     * <li>the directory path that will be used to generate download files. 
     * <li>the prefix that will be used to generate multi-species file names. If {@code null} or 
     * empty, TODO to be decided.
     * </ol>
     * 
     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
     * @throws IllegalArgumentException If incorrect parameters were provided.
     * @throws IOException              If an error occurred while trying to write generated files.
     */
    public static void main(String[] args) throws IllegalArgumentException, IOException {
        log.entry((Object[]) args);
    
        int expectedArgLength = 5;
        if (args.length != expectedArgLength) {
            throw log.throwing(new IllegalArgumentException(
                    "Incorrect number of arguments provided, expected " + 
                    expectedArgLength + " arguments, " + args.length + " provided."));
        }
        
        GenerateMultiSpeciesDiffExprFile generator = new GenerateMultiSpeciesDiffExprFile(
                CommandRunner.parseListArgument(args[0]),
                args[0],
                GenerateDownloadFile.convertToFileTypes(
                    CommandRunner.parseListArgument(args[1]), MultiSpDiffExprFileType.class),
                args[2],
                args[3]);
        generator.generateMultiSpeciesDiffExprFiles();
        log.exit();
    }

    /**
     * Generate multi-species differential expression files, for the types defined by 
     * {@code fileTypes}, for species defined by {@code speciesIds} with ancestral taxon defined by 
     * {@code taxonId}, in the directory {@code directory}.
     * 
     * @throws IllegalArgumentException If no species ID or taxon ID is provided.
     * @throws IOException              If an error occurred while trying to write generated files.
     * 
     */
    public void generateMultiSpeciesDiffExprFiles() throws IOException {
        log.entry(this.speciesIds, this.taxonId, this.fileTypes, this.directory, this.groupPrefix);

        Set<String> setSpecies = new HashSet<String>();
        if (this.speciesIds != null && !this.speciesIds.isEmpty()) {
            setSpecies = new HashSet<String>(this.speciesIds);
        } else {
            throw log.throwing(new IllegalArgumentException("No species ID is provided"));
        }
        
        if (this.taxonId == null) {
            throw log.throwing(new IllegalArgumentException("No taxon ID is provided"));
        }

        // If no file types are given by user, we set all file types
        if (this.fileTypes == null || this.fileTypes.isEmpty()) {
            this.fileTypes = EnumSet.allOf(MultiSpDiffExprFileType.class);
        }

        // Retrieve species names, gene names, stage names, anat. entity names, for all species
        // XXX: retrieve only for speciesIds? 
        Map<String, String> speciesNamesByIds = this.checkAndGetLatinNamesBySpeciesIds(setSpecies);
        Map<String, String> geneNamesByIds = 
                BgeeDBUtils.getGeneNamesByIds(setSpecies, this.getGeneDAO());
        Map<String, String> stageNamesByIds = 
                BgeeDBUtils.getStageNamesByIds(setSpecies, this.getStageDAO());
        Map<String, String> anatEntityNamesByIds = 
                BgeeDBUtils.getAnatEntityNamesByIds(setSpecies, this.getAnatEntityDAO());
        Map<String, String> cioNamesByIds = 
                BgeeDBUtils.getCIOStatementNamesByIds(this.getCIOStatementDAO());

        // Generate multi-species differential expression files
        log.info("Start generating of multi-species diff. expression files for the group {} with " +
                "the species {} and the ancestral taxon ID {}...", 
                this.groupPrefix, speciesNamesByIds.values(), this.taxonId);

        try {
            this.generateMultiSpeciesDiffExprFiles(speciesNamesByIds, 
                    geneNamesByIds, stageNamesByIds, anatEntityNamesByIds, cioNamesByIds);
        } finally {
            // close connection to database
            this.getManager().releaseResources();
        }
        
        log.info("Done generating of multi-species diff. expression files for the group {}.", 
                this.groupPrefix);

        log.exit();
    }

    /**
     * TODO Javadoc
     *
     * @param speciesNamesForFilesByIds
     * @param geneNamesByIds
     * @param stageNamesByIds
     * @param anatEntityNamesByIds
     * @param cioNamesByIds
     * @throws IOException 
     */
    private void generateMultiSpeciesDiffExprFiles(Map<String, String> speciesNamesByIds,
            Map<String, String> geneNamesByIds, Map<String, String> stageNamesByIds,
            Map<String, String> anatEntityNamesByIds, Map<String, String> cioNamesByIds) throws IOException {
        log.entry(this.directory, this.groupPrefix, this.fileTypes, this.taxonId, speciesNamesByIds,
                geneNamesByIds, stageNamesByIds, anatEntityNamesByIds, cioNamesByIds);

        log.debug("Start generating multi-species differential expression files for the group {} with the taxon {} and file types {}...", 
                this.groupPrefix, this.taxonId, this.fileTypes);

        //********************************
        // RETRIEVE DATA FROM DATA SOURCE
        //********************************
        Set<String> speciesFilter = new HashSet<String>();
        speciesFilter.addAll(this.speciesIds);

        log.trace("Start retrieving data...");
        
        // We load homologous genes 
        List<GeneTO> homologousGenes = BgeeDBUtils.getHomologousGenes(speciesFilter,
                this.getManager().getGeneDAO());
        
        // We load homologous organs 
        List<SimAnnotToAnatEntityTO> homologousAnatEntities = 
                BgeeDBUtils.getHomologousAnatEntities(speciesFilter, this.taxonId, 
                        this.getManager().getSummarySimilarityAnnotationDAO());
        
        // Load differential expression calls order by OMA node ID
        // TODO to be implemented
        
        log.trace("Done retrieving data.");
        
        //****************************
        // PRODUCE AND WRITE DATA
        //****************************
        log.trace("Start generating and writing file content");

        // Now, we write all requested files at once. This way, we will generate the data only once, 
        // and we will not have to store them in memory.
        
        // First we allow to store file names, writers, etc, associated to a FileType, 
        // for the catch and finally clauses. 
        Map<FileType, String> generatedFileNames = new HashMap<FileType, String>();

        // We will write results in temporary files that we will rename at the end
        // if everything is correct
        String tmpExtension = ".tmp";

        // In order to close all writers in a finally clause.
        // We use ICsvMapWriter because the number of columns depends on the number of species for 
        // the simple file (3 columns by species)
        Map<FileType, ICsvMapWriter> writersUsed = new HashMap<FileType, ICsvMapWriter>();
        try {
            //**************************
            // OPEN FILES, CREATE WRITERS, WRITE HEADERS
            //**************************
            Map<FileType, CellProcessor[]> processors = new HashMap<FileType, CellProcessor[]>();
            Map<FileType, String[]> headers = new HashMap<FileType, String[]>();

            // Get ordered species names
            List<String> orderedSpeciesNames = this.getSpeciesNameAsList(
                    this.speciesIds, speciesNamesByIds);
            
            for (FileType fileType : this.fileTypes) {
                CellProcessor[] fileTypeProcessors = null;
                String[] fileTypeHeaders = null;

                fileTypeProcessors = this.generateCellProcessors(
                        (MultiSpDiffExprFileType) fileType, this.speciesIds.size());
                processors.put(fileType, fileTypeProcessors);
                
                fileTypeHeaders = this.generateHeader(
                        (MultiSpDiffExprFileType) fileType, orderedSpeciesNames);
                headers.put(fileType, fileTypeHeaders);

                // Create file name
                String fileName = this.groupPrefix + "_" +
                        fileType.getStringRepresentation() + EXTENSION;
                generatedFileNames.put(fileType, fileName);

                // write in temp file
                File file = new File(this.directory, fileName + tmpExtension);
                // override any existing file
                if (file.exists()) {
                    file.delete();
                }

                // create writer and write header
                ICsvMapWriter mapWriter = new CsvMapWriter(new FileWriter(file), Utils.TSVCOMMENTED);
                mapWriter.writeHeader(fileTypeHeaders);
                writersUsed.put(fileType, mapWriter);
            }

            // ****************************
            // WRITE ROWS
            // ****************************
            // TODO to be implemented
            
//            CallTOResultSet rs = null;
//            Set<CallTO> groupedCallTOs = new HashSet<CallTO>();
//            
//            CallTO previousTO = null;
//            while (rs.next()) {
//                CallTO currentTO = rs.getTO();
//                if (previousTO != null && currentTO.getOMANodeId() != previousTO.getOMANodeId()) {
//                    // We propagate differential expression calls and order them
//                          - when there is no data in a condition, but the no-expression in  
//                            a sub-stage => no expression low quality 
//                          - propagation on high-level stages
//                          - propagation to children only for anatomy comparison
//                    // We compute and write the rows in all files
//                          - we filter families with only no-expression low quality            
//                          - we filter families with only no diff expression
//                          - do system to keep only the 'Observed' in single file
//                            but for the first generation we do not active.
//                          - filter poor quality homology annotations in simple file
//                    // We clear the set containing TO of an unique OMA Node ID
//                    groupedCallTOs.clear();
//                }
//                groupedCallTOs.add(to);
//                previousTO = to;
//            }

        } catch (Exception e) {
            this.deleteTempFiles(generatedFileNames, tmpExtension);
            throw e;
        } finally {
            for (ICsvMapWriter writer : writersUsed.values()) {
                writer.close();
            }
        }

        // Now, if everything went fine, we rename the temporary files
        this.renameTempFiles(generatedFileNames, tmpExtension);

        log.exit();
    }

    /**
     * TODO Javadoc
     *
     * @param speciesIds
     * @param speciesNamesByIds
     * @return
     */
    //TODO: DRY
    private List<String> getSpeciesNameAsList(
            List<String> speciesIds, Map<String, String> speciesNamesByIds) {
        log.entry();
        
        List<String> names = new ArrayList<String>();
        for (String id : speciesIds) {
            names.add(speciesNamesByIds.get(id));
        }
        assert names.size() == speciesIds.size();

        return log.exit(names);
    }


    /**
     * Generates an {@code Array} of {@code CellProcessor}s used to process a multi-species 
     * differential expression TSV file of type {@code fileType}.
     * 
     * @param fileType  The {@code MultiSpDiffExprFileType} of the file to be generated.
     * @param nbSpecies An {@code int} that is the number of species in the file.
     * @return          An {@code Array} of {@code CellProcessor}s used to process 
     *                  a multi-species differential expression file.
     * @throw IllegalArgumentException If {@code fileType} is not managed by this method.
     */
    private CellProcessor[] generateCellProcessors(MultiSpDiffExprFileType fileType, int nbSpecies) 
            throws IllegalArgumentException {
        log.entry(fileType);

        //First, we define all set of possible values
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
        
        //Second, we build the CellProcessor
        if (fileType.isSimpleFileType()) {
            int nbColumns = 7 + 3 * nbSpecies;
            CellProcessor[] processors = new CellProcessor[nbColumns];
            processors[0] = new StrNotNullOrEmpty(); // oma id
            processors[1] = new StrNotNullOrEmpty(); // gene ID list
            processors[2] = new NotNull();              // gene name list
            processors[3] = new StrNotNullOrEmpty();    // anatomical entity ID list
            processors[4] = new StrNotNullOrEmpty();    // anatomical entity name list
            processors[5] = new StrNotNullOrEmpty();    // developmental stage ID
            processors[6] = new StrNotNullOrEmpty();    // developmental stage name
            // the number of columns depends on the number of species
            for (int i = 0; i < nbSpecies; i++) {
                int columnIndex = 7 + 4 * i;
                // we use StrNotNullOrEmpty() ant not LMinMax() condition because 
                // there is N/A when homologous organ is lost in a species
                processors[columnIndex] = new StrNotNullOrEmpty();   // nb expressed genes
                processors[columnIndex+1] = new StrNotNullOrEmpty(); // nb not expressed genes
                processors[columnIndex+2] = new StrNotNullOrEmpty(); // nb not diff expressed genes
                processors[columnIndex+3] = new StrNotNullOrEmpty(); // nb N/A genes
            }
            return log.exit(processors);
        }
        return log.exit(new CellProcessor[] {
                new StrNotNullOrEmpty(),            // oma id
                new StrNotNullOrEmpty(),            // gene ID list
                new NotNull(),                      // gene name list
                new StrNotNullOrEmpty(),            // anatomical entity ID list
                new StrNotNullOrEmpty(),            // anatomical entity name list
                new StrNotNullOrEmpty(),            // developmental stage ID
                new StrNotNullOrEmpty(),            // developmental stage name
                new StrNotNullOrEmpty(),            // species latin name
                new StrNotNullOrEmpty(),            // cio id
                new StrNotNullOrEmpty(),            // cio name
                new IsElementOf(data),              // Differential expression
                new IsElementOf(resumeQualities),   // Quality
                new IsElementOf(data),              // Affymetrix data
                new IsElementOf(specificTypeQualities), // Affymetrix call quality
                new DMinMax(0, 1),                  // Best p-value using Affymetrix
                new LMinMax(0, Long.MAX_VALUE),     // Consistent DEA count using Affymetrix
                new LMinMax(0, Long.MAX_VALUE),     // Inconsistent DEA count using Affymetrix
                new IsElementOf(data),              // RNA-seq data
                new IsElementOf(specificTypeQualities), // RNA-seq call quality
                new DMinMax(0, 1),                  // Best p-value using RNA-Seq
                new LMinMax(0, Long.MAX_VALUE),     // Consistent DEA count using RNA-Seq
                new LMinMax(0, Long.MAX_VALUE)});   // Inconsistent DEA count using RNA-Seq
    }

    /**
     * Generates an {@code Array} of {@code String}s used to generate the header of a multi-species
     * differential expression TSV file of type {@code fileType}.
     * 
     * @param fileType  The {@code MultiSpDiffExprFileType} of the file to be generated.
     * @param nbSpecies A {@code List} of {@code String}s that are the names of species 
     *                  we want to generate data for.
     * @return          An {@code Array} of {@code String}s used to produce the header.
     * @throw IllegalArgumentException If {@code fileType} is not managed by this method.
     */
    private String[] generateHeader(MultiSpDiffExprFileType fileType, List<String> speciesNames)
        throws IllegalArgumentException {
        log.entry(fileType);

        if (fileType.isSimpleFileType()) {
            int nbColumns = 7 + 4 * speciesNames.size();
            String[] headers = new String[nbColumns];
            headers[0] = OMA_ID_COLUMN_NAME;
            headers[1] = GENE_ID_LIST_ID_COLUMN_NAME;
            headers[2] = GENE_NAME_LIST_ID_COLUMN_NAME;
            headers[3] = ANAT_ENTITY_ID_LIST_ID_COLUMN_NAME;
            headers[4] = ANAT_ENTITY_NAME_LIST_ID_COLUMN_NAME;
            headers[5] = STAGE_ID_COLUMN_NAME;
            headers[6] = STAGE_NAME_COLUMN_NAME;
            // the number of columns depends on the number of species
            for (int i = 0; i < speciesNames.size(); i++) {
                int columnIndex = 7 + 4 * i;
                String endHeader = " for " + speciesNames.get(i);
                headers[columnIndex] = NB_OVER_EXPR_GENES_COLUMN_NAME + endHeader;
                headers[columnIndex+1] = NB_UNDER_EXPR_GENES_COLUMN_NAME + endHeader;
                headers[columnIndex+2] = NB_NO_DIFF_EXPR_GENES_COLUMN_NAME + endHeader;
                headers[columnIndex+3] = NB_NA_GENES_COLUMN_NAME + endHeader;
            }
            return log.exit(headers);
        }

        return log.exit(new String[] { 
                OMA_ID_COLUMN_NAME, GENE_ID_COLUMN_NAME, GENE_NAME_COLUMN_NAME,
                ANAT_ENTITY_ID_LIST_ID_COLUMN_NAME, ANAT_ENTITY_NAME_LIST_ID_COLUMN_NAME,
                STAGE_ID_COLUMN_NAME, STAGE_NAME_COLUMN_NAME, SPECIES_LATIN_NAME_COLUMN_NAME,
                CIO_ID_ID_COLUMN_NAME, CIO_NAME_ID_COLUMN_NAME, 
                DIFFEXPRESSION_COLUMN_NAME, QUALITY_COLUMN_NAME,
                AFFYMETRIX_DATA_COLUMN_NAME, AFFYMETRIX_CALL_QUALITY_COLUMN_NAME,
                AFFYMETRIX_P_VALUE_COLUMN_NAME, AFFYMETRIX_CONSISTENT_DEA_COUNT_COLUMN_NAME, 
                AFFYMETRIX_INCONSISTENT_DEA_COUNT_COLUMN_NAME,
                RNASEQ_DATA_COLUMN_NAME, RNASEQ_CALL_QUALITY_COLUMN_NAME,
                RNASEQ_P_VALUE_COLUMN_NAME, RNASEQ_CONSISTENT_DEA_COUNT_COLUMN_NAME, 
                RNASEQ_INCONSISTENT_DEA_COUNT_COLUMN_NAME});
    }
}
