package org.bgee.pipeline.expression;

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

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTOResultSet;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.ComparisonFactor;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.DiffExprCallType;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTOResultSet;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallParams;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTOResultSet;
import org.bgee.model.dao.api.expressiondata.ExpressionCallParams;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTOResultSet;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallParams;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTOResultSet;
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
 * Class responsible to generate TSV download files (simple and complete files) 
 * from the Bgee database. 
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @since Bgee 13
 */
public class GenerateDownloadFile extends CallUser {
    
    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(GenerateDownloadFile.class.getName());
        
    /**
     * A {@code String} that is the name of the column containing gene IDs, in the download file.
     */
    public final static String GENE_ID_COLUMN_NAME = "Gene ID";

    /**
     * A {@code String} that is the name of the column containing gene names, in the download file.
     */
    public final static String GENE_NAME_COLUMN_NAME = "Gene name";

    /**
     * A {@code String} that is the name of the column containing developmental stage IDs, 
     * in the download file.
     */
    public final static String STAGE_ID_COLUMN_NAME = "Developmental stage ID";

    /**
     * A {@code String} that is the name of the column containing developmental stage names, 
     * in the download file.
     */
    public final static String STAGE_NAME_COLUMN_NAME = "Developmental stage name";

    /**
     * A {@code String} that is the name of the column containing anatomical entity IDs, 
     * in the download file.
     */
    public final static String ANATENTITY_ID_COLUMN_NAME = "Anatomical entity ID";

    /**
     * A {@code String} that is the name of the column containing anatomical entity names, 
     * in the download file.
     */
    public final static String ANATENTITY_NAME_COLUMN_NAME = "Anatomical entity name";

    /**
     * A {@code String} that is the name of the column containing expression/no-expression found 
     * with Affymetrix experiment, in the download file.
     */
    public final static String AFFYMETRIX_DATA_COLUMN_NAME = "Affymetrix data";

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
     * A {@code String} that is the name of the column containing expression/no-expression found 
     * with EST experiment, in the download file.
     */
    public final static String ESTDATA_COLUMN_NAME = "EST data";

    /**
     * A {@code String} that is the name of the column containing expression/no-expression found 
     * with <em>in situ</em> experiment, in the download file.
     */
    public final static String INSITUDATA_COLUMN_NAME = "In situ data";

    /**
     * A {@code String} that is the name of the column containing expression/no-expression found 
     * with relaxed <em>in situ</em> experiment, in the download file.
     */
    public final static String RELAXEDINSITUDATA_COLUMN_NAME = "Relaxed in situ data";
    
    /**
     * A {@code String} that is the name of the column containing expression/no-expression found 
     * with RNA-Seq experiment, in the download file.
     */
    public final static String RNASEQ_DATA_COLUMN_NAME = "RNA-Seq data";

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
     * A {@code String} that is the name of the column containing whether the call 
     * include observed data or not.
     */
    public final static String INCLUDING_OBSERVED_DATA_COLUMN_NAME = "Including observed data";

    /**
     * A {@code String} that is the name of the column containing merged expression/no-expression 
     * from different data types, in the download file.
     */
    public final static String EXPRESSION_COLUMN_NAME = "Expression";

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
    * An {@code Enum} used to define the possible file types to be generated, as class arguments.
    * <ul>
    * <li>{@code EXPR_SIMPLE}:                    presence/absence of expression in a simple 
    *                                             download file.
    * <li>{@code EXPR_COMPLETE}:                  presence/absence of expression in an advanced 
    *                                             download file.
    * <li>{@code DIFF_EXPR_SIMPLE_ANAT_ENTITY}:   differential expression across anat. entities 
    *                                             in a simple download file.
    * <li>{@code DIFF_EXPR_COMPLETE_ANAT_ENTITY}: differential expression across anat. entities 
    *                                             in an advanced download file.
    * <li>{@code DIFF_EXPR_SIMPLE_STAGE}:         differential expression across developmental 
    *                                             stages in a simple download file.
    * <li>{@code DIFF_EXPR_COMPLETE_STAGE}:       differential expression across developmental 
    *                                             stages in an advanced download file.
    * </ul>
    * 
    * @author Valentine Rech de Laval
    * @version Bgee 13
    * @since Bgee 13
    */
    public enum FileType {
        EXPR_SIMPLE("expr-simple", true, true, false), 
        EXPR_COMPLETE("expr-complete", false, true, false), 
        DIFF_EXPR_SIMPLE_ANAT_ENTITY("diffexpr-anat-entity-simple", true, false, true,
                ComparisonFactor.ANATOMY), 
        DIFF_EXPR_COMPLETE_ANAT_ENTITY("diffexpr-anat-entity-complete", false, false, true,
                ComparisonFactor.ANATOMY),
        DIFF_EXPR_SIMPLE_STAGE("diffexpr-stage-simple", true, false, true,
                ComparisonFactor.DEVELOPMENT), 
        DIFF_EXPR_COMPLETE_STAGE("diffexpr-stage-complete", false, false, true,
                ComparisonFactor.DEVELOPMENT);

        /**
         * A {@code String} that can be used to generate names of files of this type.
         */
        private final String stringRepresentation;
        /**
         * A {@code boolean} defining whether this {@code FileType} is a simple 
         * file type
         */
        private final boolean simpleFileType;
        /**
         * A {@code boolean} defining whether this {@code FileType} is an expression 
         * file type
         */
        private final boolean expressionFileType;
        /**
         * A {@code boolean} defining whether this {@code FileType} is a differential expression 
         * file type
         */
        private final boolean diffExpressionFileType;
        /**
         * A {@code ComparisonFactor} defining what is the experimental factor 
         * compared that generated the differential expression calls.
         */
        private final ComparisonFactor comparisonFactor;

        /**
         * Constructor providing the {@code String} representation of this {@code FileType}, a
         * {@code boolean} defining whether this {@code FileType} is a simple file type, a
         * {@code boolean} defining whether this {@code FileType} is an expression file type, and a
         * {@code boolean} defining whether this {@code FileType} is a differential expression file
         * type.
         * 
         * @param stringRepresentation   A {@code String} corresponding to this {@code FileType}.
         * @param simpleFileType         A {@code boolean} defining whether this {@code FileType} 
         *                               is a simple file type.
         * @param expressionFileType     A {@code boolean} defining whether this {@code FileType} 
         *                               is an expression file type.
         * @param diffExpressionFileType A {@code boolean} defining whether this {@code FileType} 
         *                               is a differential expression file type.
         */
        private FileType(String stringRepresentation, boolean simpleFileType, 
                boolean expressionFileType, boolean diffExpressionFileType) {
            this(stringRepresentation, simpleFileType, expressionFileType, 
                    diffExpressionFileType, null);
        }

        /**
         * Constructor providing the {@code String} representation of this {@code FileType}, a
         * {@code boolean} defining whether this {@code FileType} is a simple file type, a
         * {@code boolean} defining whether this {@code FileType} is an expression file type, a
         * {@code boolean} defining whether this {@code FileType} is a differential expression file
         * type, and a {@code ComparisonFactor} defining what is the experimental factor compared 
         * that generated the differential expression calls.
         * 
         * @param stringRepresentation   A {@code String} corresponding to this {@code FileType}.
         * @param simpleFileType         A {@code boolean} defining whether this {@code FileType} 
         *                               is a simple file type.
         * @param expressionFileType     A {@code boolean} defining whether this {@code FileType} 
         *                               is an expression file type.
         * @param diffExpressionFileType A {@code boolean} defining whether this {@code FileType} 
         *                               is a differential expression file type.
         * @param comparisonFactor       A {@code ComparisonFactor} defining what is the  
         *                               experimental factor compared that generated the  
         *                               differential expressioncalls.
         */
        private FileType(String stringRepresentation, boolean simpleFileType, 
                boolean expressionFileType, boolean diffExpressionFileType, 
                ComparisonFactor comparisonFactor) {
            this.stringRepresentation = stringRepresentation;
            this.simpleFileType = simpleFileType;
            this.expressionFileType = expressionFileType;
            this.diffExpressionFileType = diffExpressionFileType;
            this.comparisonFactor = comparisonFactor;
        }

        /**
         * @return   A {@code String} that can be used to generate names of files of this type.
         */
        public String getStringRepresentation() {
            return this.stringRepresentation;
        }

        /**
         * @return   A {@code boolean} defining whether this {@code FileType} is a simple file type.
         */
        public boolean isSimpleFileType() {
            return this.simpleFileType;
        }

        /**
         * @return   A {@code boolean} defining whether this {@code FileType} is an expression 
         *           file type.
         */
        public boolean isExpressionFileType() {
            return this.expressionFileType;
        }

        /**
         * @return   A {@code boolean} defining whether this {@code FileType} is a differential
         *           expression file type.
         */
        public boolean isDiffExprFileType() {
            return this.diffExpressionFileType;
        }

        /**
         * @return   A {@code ComparisonFactor} defining what is the experimental factor 
         *           compared that generated the differential expression calls.
         */
        public ComparisonFactor getComparisonFactor() {
            return this.comparisonFactor;
        }

        public String toString() {
            return this.getStringRepresentation();
        }
    }

    /**
     * A {@code String} that is the extension of download files to be generated.
     */
    public final static String EXTENSION = ".tsv";

    /**
     * An {@code Enum} used to define, for each data type (Affymetrix, RNA-Seq, ...),
     * as well as for the summary column, the data state of the call.
     * <ul>
     * <li>{@code NO_DATA}:        no data from the associated data type allowed to produce the call.
     * <li>{@code NOEXPRESSION}:   no-expression was detected from the associated data type.
     * <li>{@code LOWQUALITY}:     low-quality expression was detected from the associated data type.
     * <li>{@code HIGHQUALITY}:    high-quality expression was detected from the associated data type.
     * <li>{@code LOWAMBIGUITY}:   different data types are not coherent with an inferred  
     *                             no-expression call (for instance, Affymetrix data reveals an 
     *                             expression while <em>in situ</em> data reveals an inferred 
     *                             no-expression).
     * <li>{@code HIGHAMBIGUITY}:  different data types are not coherent without at least an 
     *                             inferred no-expression call (for instance, Affymetrix data   
     *                             reveals expression while <em>in situ</em> data reveals a  
     *                             no-expression without been inferred).
     * </ul>
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public enum ExpressionData {
        NO_DATA("no data"), NO_EXPRESSION("absent high quality"), 
        LOW_QUALITY("expression low quality"), HIGH_QUALITY("expression high quality"), 
        LOW_AMBIGUITY("low ambiguity"), HIGH_AMBIGUITY("high ambiguity");

        private final String stringRepresentation;

        /**
         * Constructor providing the {@code String} representation of this {@code ExpressionData}.
         * 
         * @param stringRepresentation  A {@code String} corresponding to this {@code ExpressionData}.
         */
        private ExpressionData(String stringRepresentation) {
            this.stringRepresentation = stringRepresentation;
        }

        public String getStringRepresentation() {
            return this.stringRepresentation;
        }

        public String toString() {
            return this.getStringRepresentation();
        }
    }

    /**
     * An {@code Enum} used to define, for each data type (Affymetrix and RNA-Seq), 
     * as well as for the summary column, the data state of the call.
     * <ul>
     * <li>{@code NO_DATA}:              no data from the associated data type allowed to produce 
     *                                   the call.
     * <li>{@code NOT_EXPRESSED}:        means that the call was never seen as 'expressed'.
     * <li>{@code OVER_EXPRESSED}:       means that the call is seen as over-expressed.
     * <li>{@code UNDER_EXPRESSED}:      means that  the call is seen as under-expressed.
     * <li>{@code NOT_DIFF_EXPRESSED}:   means that the gene has expression, but 
     *                                   <strong>no</strong> significant fold change observe.
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
        NO_DATA("no data"), NOT_EXPRESSED("not expressed"), OVER_EXPRESSED("over-expression"), 
        UNDER_EXPRESSED("under-expression"), NOT_DIFF_EXPRESSED("no diff expression"), 
        WEAK_AMBIGUITY("weak ambiguity"), STRONG_AMBIGUITY("strong ambiguity");

        private final String stringRepresentation;

        /**
         * Constructor providing the {@code String} representation of this {@code DiffExpressionData}.
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

        public String toString() {
            return this.getStringRepresentation();
        }
    }

    /**
     * An {@code Enum} used to define whether the call has been observed. This is to distinguish 
     * from propagated data only, that should provide a lower confidence in the call. 
     * <ul>
     * <li>{@code OBSERVED}:     the call has been observed at least once.
     * <li>{@code NOTOBSERVED}:  the call has never been observed.
     * </ul>
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public enum ObservedData {
        OBSERVED("yes"), NOT_OBSERVED("no");

        private final String stringRepresentation;

        /**
         * Constructor providing the {@code String} representation of this {@code ObservedData}.
         * 
         * @param stringRepresentation  A {@code String} corresponding to this {@code ObservedData}.
         */
        private ObservedData(String stringRepresentation) {
            this.stringRepresentation = stringRepresentation;
        }

        public String getStringRepresentation() {
            return this.stringRepresentation;
        }

        public String toString() {
            return this.getStringRepresentation();
        }
    }

    /**
     * Main method to trigger the generate TSV download files (simple and advanced files) from Bgee 
     * database. Parameters that must be provided in order in {@code args} are: 
     * <ol>
     * <li> a list of NCBI species IDs (for instance, {@code 9606} for human) that will be used to 
     * generate download files, separated by the {@code String} {@link CommandRunner#LIST_SEPARATOR}.
     * If it is not provided, all species contained in database will be used.
     * <li> a list of files types that will be generated ('expr-simple' for 
     * {@link FileType EXPR_SIMPLE}, 'expr-complete' for {@link FileType EXPR_COMPLETE}, 
     * 'diffexpr-anat-entity-simple' for {@link FileType DIFF_EXPR_SIMPLE_ANAT_ENTITY},
     * 'diffexpr-anat-entity-complete' for {@link FileType DIFF_EXPR_COMPLETE_ANAT_ENTITY}, 
     * 'diffexpr-stage-simple' for {@link FileType DIFF_EXPR_SIMPLE_STAGE}, and  
     * 'diffexpr-stage-complete' for {@link FileType DIFF_EXPR_COMPLETE_STAGE}), 
     * separated by the {@code String} {@link CommandRunner#LIST_SEPARATOR}.
     * <li>the directory path that will be used to generate download files. 
     * </ol>
     * 
     * @param args          An {@code Array} of {@code String}s containing the requested parameters.
     * @throws IOException  If an error occurred while trying to write generated files.
     */
    public static void main(String[] args) throws IOException {
        log.entry((Object[]) args);

        // TODO Manage with multi-species!

        // Arguments: species list, file types to be generated, and directory path
        // FIXME be able to not provide a species list to use all species contained in database
        int expectedArgLengthSingleSpecies = 3; 
        if (args.length != expectedArgLengthSingleSpecies) {
            throw log.throwing(new IllegalArgumentException(
                    "Incorrect number of arguments provided, expected " + 
                    expectedArgLengthSingleSpecies + " arguments, " + args.length + " provided."));
        }

        List<String> speciesIds = CommandRunner.parseListArgument(args[0]);
        Set<String> fileTypes   = new HashSet<String>(CommandRunner.parseListArgument(args[1])); 
        String directory        = args[2];

        // Retrieve FileType from String argument
        Set<String> unknownFileTypes = new HashSet<String>();
        Set<FileType> filesToBeGenerated = EnumSet.noneOf(FileType.class);
        for (String inputFileType: fileTypes) {
            if (inputFileType.equals(FileType.EXPR_SIMPLE.getStringRepresentation())) {
                filesToBeGenerated.add(FileType.EXPR_SIMPLE);  

            } else if (inputFileType.equals(FileType.EXPR_COMPLETE.getStringRepresentation())) {
                filesToBeGenerated.add(FileType.EXPR_COMPLETE);    

            } else if (inputFileType.equals(
                    FileType.DIFF_EXPR_SIMPLE_ANAT_ENTITY.getStringRepresentation())) {
                filesToBeGenerated.add(FileType.DIFF_EXPR_SIMPLE_ANAT_ENTITY);  

            } else if (inputFileType.equals(
                    FileType.DIFF_EXPR_COMPLETE_ANAT_ENTITY.getStringRepresentation())) {
                filesToBeGenerated.add(FileType.DIFF_EXPR_COMPLETE_ANAT_ENTITY);

            } else if (inputFileType.equals(
                    FileType.DIFF_EXPR_SIMPLE_STAGE.getStringRepresentation())) {
                filesToBeGenerated.add(FileType.DIFF_EXPR_SIMPLE_STAGE);

            } else if (inputFileType.equals(
                    FileType.DIFF_EXPR_COMPLETE_STAGE.getStringRepresentation())) {
                filesToBeGenerated.add(FileType.DIFF_EXPR_COMPLETE_STAGE);

            } else {
                unknownFileTypes.add(inputFileType);
            }
        }
        if (!unknownFileTypes.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "Some file types do not exist: " + unknownFileTypes));
        }

        GenerateDownloadFile generate = new GenerateDownloadFile();
        generate.generateSingleSpeciesFiles(speciesIds, filesToBeGenerated, directory);

        log.exit();
    }

    /**
     * Default constructor. 
     */
    public GenerateDownloadFile() {
        this(null);
    }

    /**
     * Constructor providing the {@code MySQLDAOManager} that will be used by 
     * this object to perform queries to the database. This is useful for unit testing.
     * 
     * @param manager   the {@code MySQLDAOManager} to use.
     */
    public GenerateDownloadFile(MySQLDAOManager manager) {
        super(manager);
    }

    /**
     * Generate single species files, for the types defined by {@code fileTypes}, 
     * for species defined by {@code speciesIds}, in the directory {@code directory}.
     * 
     * @param speciesIds     A {@code List} of {@code String}s that are the IDs of species for 
     *                       which files are generated.
     * @param fileTypes      A {@code Set} of {@code FileType}s containing file types to be generated.
     * @param directory      A {@code String} that is the directory path directory to store the 
     *                       generated files. 
     * @throws IOException   If an error occurred while trying to write generated files.
     */
    public void generateSingleSpeciesFiles(List<String> speciesIds, Set<FileType> fileTypes, 
            String directory) throws IOException { 
        log.entry(speciesIds, fileTypes, directory);

        Set<String> setSpecies = new HashSet<String>();
        if (speciesIds != null) {
            setSpecies = new HashSet<String>(speciesIds);
        }

        // Check user input, retrieve info for generating file names
        Map<String, String> speciesNamesForFilesByIds = this.checkAndGetLatinNamesBySpeciesIds(
                setSpecies);
        if (speciesIds == null || speciesIds.isEmpty()) {
            speciesIds = new ArrayList<String>(speciesNamesForFilesByIds.keySet());
        }

        if (fileTypes == null || fileTypes.isEmpty()) {
            // If no file types are given by user, we set all file types
            fileTypes = EnumSet.allOf(FileType.class);
        } 
        
        // Retrieve gene names, stage names, anat. entity names, once for all species
        Map<String, String> geneNamesByIds = 
                BgeeDBUtils.getGeneNamesByIds(setSpecies, this.getGeneDAO());
        Map<String, String> stageNamesByIds = 
                BgeeDBUtils.getStageNamesByIds(setSpecies, this.getStageDAO());
        Map<String, String> anatEntityNamesByIds =
                BgeeDBUtils.getAnatEntityNamesByIds(setSpecies, this.getAnatEntityDAO());

        Set<FileType> diffexprAnatEntityFT = new HashSet<FileType>(), 
                diffExprStagesFT = new HashSet<FileType>(), exprFT = new HashSet<FileType>();
        for (FileType fileType : fileTypes) {
            if (fileType.equals(FileType.DIFF_EXPR_SIMPLE_ANAT_ENTITY) ||
                    fileType.equals(FileType.DIFF_EXPR_COMPLETE_ANAT_ENTITY)) {
                diffexprAnatEntityFT.add(fileType);
            } else if (fileType.equals(FileType.DIFF_EXPR_SIMPLE_STAGE) ||
                    fileType.equals(FileType.DIFF_EXPR_COMPLETE_STAGE)) {
                diffExprStagesFT.add(fileType);
            } else if (fileType.equals(FileType.EXPR_SIMPLE) ||
                    fileType.equals(FileType.EXPR_COMPLETE)) {
                exprFT.add(fileType);
            }
        }
        
        for (String speciesId: speciesIds) {
            log.info("Start generating of download files for the species {}...", speciesId);
            
            // Differential expression files across anatomical entities 
            if (diffexprAnatEntityFT.size() > 0) {
                this.generateSingleSpeciesFilesByFileTypes(
                        directory, speciesNamesForFilesByIds.get(speciesId), 
                        diffexprAnatEntityFT, speciesId, geneNamesByIds, stageNamesByIds, 
                        anatEntityNamesByIds, ComparisonFactor.ANATOMY,
                        fileTypes.contains(FileType.DIFF_EXPR_COMPLETE_STAGE));
            }
            
            // Differential expression files across stages 
            if (diffExprStagesFT.size() > 0) {
                this.generateSingleSpeciesFilesByFileTypes(
                        directory, speciesNamesForFilesByIds.get(speciesId), 
                        diffExprStagesFT, speciesId, geneNamesByIds, stageNamesByIds, 
                        anatEntityNamesByIds, ComparisonFactor.DEVELOPMENT, 
                        fileTypes.contains(FileType.DIFF_EXPR_COMPLETE_STAGE));
            }
            
            // Presence/absence expression files 
            if (exprFT.size() > 0) {
                this.generateSingleSpeciesFilesByFileTypes(
                        directory, speciesNamesForFilesByIds.get(speciesId), 
                        exprFT, speciesId, geneNamesByIds, stageNamesByIds, 
                        anatEntityNamesByIds, null, fileTypes.contains(FileType.EXPR_COMPLETE));
            }
            
            //close connection to database between each species, to avoid idle connection reset
            this.getManager().releaseResources();
            log.info("Done generating of download files for the species {}.", speciesId);
        }
        log.exit();
    }

    /**
     * Retrieves non-informative anatomical entities for the requested species. They correspond 
     * to anatomical entities belonging to non-informative subsets in Uberon, and with 
     * no observed data from Bgee (no basic calls of any type in them).
     * 
     * @param speciesIds     A {@code Set} of {@code String}s that are the IDs of species 
     *                       allowing to filter the non-informative anatomical entities to use.
     * @return               A {@code Set} of {@code String}s containing all 
     *                       non-informative anatomical entitiy IDs of the given species.
     * @throws DAOException  If an error occurred while getting the data from the Bgee data source.
     */
    private Set<String> loadNonInformativeAnatEntities(Set<String> speciesIds) 
            throws DAOException {
        log.entry(speciesIds);
        log.debug("Start retrieving non-informative anatomical entities for the species IDs {}...",
                speciesIds);
    
        AnatEntityDAO dao = this.getAnatEntityDAO();
        dao.setAttributes(AnatEntityDAO.Attribute.ID);
        Set<String> anatEntities = new HashSet<String>();
        try (AnatEntityTOResultSet rs = dao.getNonInformativeAnatEntitiesBySpeciesIds(speciesIds)) {
            while (rs.next()) {
                anatEntities.add(rs.getTO().getId());
            }
        }
        
        log.debug("Done retrieving non-informative anatomical entities, {} entities found",
                anatEntities.size());
        return log.exit(anatEntities);        
    }

    /**
     * Retrieves all expression calls for the requested species from the Bgee data source, 
     * grouped by gene IDs, including data propagated from anatomical substructures or not, 
     * depending on {@code includeSubstructures}. When data propagation is requested, 
     * calls generated by data propagation only, and occurring in anatomical entities 
     * with ID present in {@code nonInformativesAnatEntityIds}, are discarded.
     * <p>
     * The returned {@code ExpressionCallTO}s have no ID set, to be able 
     * to compare calls based on gene, stage and anatomical entity IDs.
     * <p>
     * Note that it is currently not possible to request for data propagated from sub-stages: 
     * Propagating such data for a whole species can have a huge memory cost and is slow. 
     * The propagation to parent stages will be done directly when writing files, 
     * to not overload the memory. 
     * 
     * @param speciesIds                    A {@code Set} of {@code String}s that are the IDs of 
     *                                      species allowing to filter the expression calls 
     *                                      to retrieve.
     * @param includeSubstructures          A {@code boolean} defining whether the 
     *                                      {@code ExpressionCallTO}s returned should be 
     *                                      global expression calls with data propagated 
     *                                      from substructures, or basic calls with no propagation. 
     *                                      If {@code true}, data are propagated. 
     * @param nonInformativesAnatEntityIds  A {@code Set} of {@code String}s that are the IDs of 
     *                                      non-informative anatomical entities. Calls in these 
     *                                      anatomical entities, generated by data propagation 
     *                                      only, will be discarded.
     * @return                              A {@code Map} where keys are {@code String}s that 
     *                                      are gene IDs, the associated values being a {@code Set} 
     *                                      of {@code ExpressionCallTO}s associated to this gene. 
     *                                      all expression calls for the requested species.
     * @throws DAOException                 If an error occurred while getting the data from the 
     *                                      Bgee data source.
     */
    private Map<String, Set<ExpressionCallTO>> loadExprCallsByGeneIds(Set<String> speciesIds, 
            boolean includeSubstructures, Set<String> nonInformativesAnatEntityIds) 
                    throws DAOException {
        log.entry(speciesIds, includeSubstructures, nonInformativesAnatEntityIds);
        
        log.debug("Start retrieving expression calls (include substructures: {}) for the species IDs {}...", 
                includeSubstructures, speciesIds);
    
        Map<String, Set<ExpressionCallTO>> callsByGeneIds = 
                new HashMap<String, Set<ExpressionCallTO>>();
        ExpressionCallDAO dao = this.getExpressionCallDAO();
        // We need all attributes but ID, stageOriginOfLine and observedData
        dao.setAttributes(EnumSet.complementOf(EnumSet.of(ExpressionCallDAO.Attribute.ID, 
                ExpressionCallDAO.Attribute.OBSERVED_DATA, 
                ExpressionCallDAO.Attribute.STAGE_ORIGIN_OF_LINE)));

        ExpressionCallParams params = new ExpressionCallParams();
        params.addAllSpeciesIds(speciesIds);
        params.setIncludeSubstructures(includeSubstructures);
        params.setIncludeSubStages(false);
    
        int i = 0;
        try (ExpressionCallTOResultSet rsExpr = dao.getExpressionCalls(params)) {
            while (rsExpr.next()) {
                ExpressionCallTO to = rsExpr.getTO();
                log.trace("Iterating ExpressionCallTO: {}", to);
                //if present in a non-informative anatomical entity.
                if (nonInformativesAnatEntityIds.contains(to.getAnatEntityId())) {
                    log.trace("Discarding propagated calls because in non-informative anatomical entity: {}.", to);
                    continue;
                }
                Set<ExpressionCallTO> exprTOs = callsByGeneIds.get(to.getGeneId());
                if (exprTOs == null) {
                    log.trace("Create new map key: {}", to.getGeneId());
                    exprTOs = new HashSet<ExpressionCallTO>();
                    callsByGeneIds.put(to.getGeneId(), exprTOs);
                }
                exprTOs.add(to);
                i++;
            }
        }

        log.debug("Done retrieving global expression calls, {} calls found", i);
        return log.exit(callsByGeneIds); 
    }

    /**
     * Retrieves all no-expression calls for the requested species from the Bgee data source,
     * grouped by gene IDs, including data propagated from parent anatomical structures or not, 
     * depending on {@code includeParentStructures}. When data propagation is requested, 
     * calls generated by data propagation only, and occurring in anatomical entities 
     * with ID present in {@code nonInformativesAnatEntityIds}, are discarded.
     * <p>
     * The returned {@code NoExpressionCallTO}s have no ID set, to be able 
     * to compare calls based on gene, stage and anatomical entity IDs.
     * 
     * @param speciesIds                    A {@code Set} of {@code String}s that are the IDs of 
     *                                      species allowing to filter the no-expression 
     *                                      calls to use.
     * @param includeParentStructures       A {@code boolean} defining whether the 
     *                                      {@code NoExpressionCallTO}s returned should be global
     *                                      no-expression calls with data propagated from
     *                                      parent anatomical structures, or basic calls with
     *                                      no propagation. If {@code true}, data are propagated. 
     * @param nonInformativesAnatEntityIds  A {@code Set} of {@code String}s that are the IDs of 
     *                                      non-informative anatomical entities. Calls in these 
     *                                      anatomical entities, generated by data propagation 
     *                                      only, will be discarded.
     * @return                              A {@code Map} where keys are {@code String}s that 
     *                                      are gene IDs, the associated values being a {@code Set} 
     *                                      of {@code NoExpressionCallTO}s associated to this gene.
     * @throws DAOException                 If an error occurred while getting the data from the 
     *                                      Bgee database.
     */
    private Map<String, Set<NoExpressionCallTO>> loadNoExprCallsByGeneIds(Set<String> speciesIds, 
            boolean includeParentStructures, Set<String> nonInformativesAnatEntityIds) 
                    throws DAOException {
        log.entry(speciesIds, includeParentStructures, nonInformativesAnatEntityIds);
        log.debug("Start retrieving no-expression calls (include parent structures: {}) for the species IDs {}...", 
                includeParentStructures, speciesIds);
    
        Map<String, Set<NoExpressionCallTO>> callsByGeneIds = 
                new HashMap<String, Set<NoExpressionCallTO>>();
        NoExpressionCallDAO dao = this.getNoExpressionCallDAO();
        // We don't retrieve no-expression call IDs to be able to compare calls on gene, 
        // stage and anatomical IDs.
        dao.setAttributes(EnumSet.complementOf(EnumSet.of(NoExpressionCallDAO.Attribute.ID)));
    
        NoExpressionCallParams params = new NoExpressionCallParams();
        params.addAllSpeciesIds(speciesIds);
        params.setIncludeParentStructures(includeParentStructures);

        int i = 0;
        try (NoExpressionCallTOResultSet rsNoExpr = dao.getNoExpressionCalls(params)) {
            while (rsNoExpr.next()) {
                NoExpressionCallTO to = rsNoExpr.getTO();
                log.trace("Iterating NoExpressionCallTO: {}", to);
                //if present in a non-informative anatomical entity.
                if (nonInformativesAnatEntityIds.contains(to.getAnatEntityId())) {
                    log.trace("Discarding propagated calls because in non-informative anatomical entity: {}.", to);
                    continue;
                }
                Set<NoExpressionCallTO> noExprTOs = callsByGeneIds.get(to.getGeneId());
                if (noExprTOs == null) {
                    log.trace("Create new map key: {}", to.getGeneId());
                    noExprTOs = new HashSet<NoExpressionCallTO>();
                    callsByGeneIds.put(to.getGeneId(), noExprTOs);
                }
                noExprTOs.add(to);
                i++;
            }
        }
        
        log.debug("Done retrieving no-expression calls, {} calls found", i);
        return log.exit(callsByGeneIds);  
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
            params.setSatisfyAllCallTypeCondition(true);
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
     * Generate download files (simple and/or advanced) containing differential <strong>OR</strong> 
     * presence/absence expression calls. This method is responsible for retrieving data from the 
     * data source, and then to write them into files.
     * <p>
     * It generates differential <strong>OR</strong> presence/absence expression calls according to 
     * {@code factor}. If {@code factor} is {@code null}, presence/absence expression calls will be 
     * generated else differential expression calls will be generated.
     * 
     * @param directory             A {@code String} that is the directory to store  
     *                              the generated files. 
     * @param fileNamePrefix        A {@code String} to be used as a prefix of the names 
     *                              of the generated files. 
     * @param fileTypes             An {@code Set} of {@code FileType}s that are the file types 
     *                              to be generated.
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
     * @param factor                A {@code ComparisonFactor}s that is the comparison factor 
     *                              allowing to filter the calls to use.
     * @param generateAdvancedFile  A {@code boolean} defining whether these {@code fileTypes} 
     *                              contains an advanced differential expression file.
     * @throws IOException          If an error occurred while trying to write the output file.
     */    
    private void generateSingleSpeciesFilesByFileTypes(String directory, String fileNamePrefix, 
            Set<FileType> fileTypes, String speciesId, Map<String, String> geneNamesByIds, 
            Map<String, String> stageNamesByIds, Map<String, String> anatEntityNamesByIds,
            ComparisonFactor factor, boolean generateAdvancedFile) throws IOException {
        log.entry(directory, fileNamePrefix, fileTypes, speciesId, 
                geneNamesByIds, stageNamesByIds, anatEntityNamesByIds, factor, generateAdvancedFile);
        
        log.debug("Start generating download files for the species {} and file types {}...", 
                    speciesId, fileTypes);
        
        //********************************
        // RETRIEVE DATA FROM DATA SOURCE
        //********************************
        Set<String> speciesFilter = new HashSet<String>();
        speciesFilter.add(speciesId);
    
        Set<String> nonInformativesAnatEntities = null;
        Map<String, Set<String>> stageParentsFromChildren = null; 
        Map<String, Set<ExpressionCallTO>> globalExprTOsByGeneIds =  null;
        Map<String, Set<NoExpressionCallTO>> globalNoExprTOsByGeneIds = null; 
        Map<String, Set<DiffExpressionCallTO>> diffExprTOsByGeneIds = null;  
    
        if (factor == null) {
            log.trace("Start retrieving data for expression files for the species {}...", speciesId);
            // Load non-informative anatomical entities: 
            // calls occurring in these anatomical entities, and generated from 
            // data propagation only (no observed data in them), will be discarded. 
            nonInformativesAnatEntities = 
                    this.loadNonInformativeAnatEntities(speciesFilter);
            
            //we retrieve expression and no-expression calls grouped by geneIds. This is because, 
            //to correctly propagate expression calls to parent stages, we need to examine all calls 
            //related to a gene at the same time. We cannot propagate everything at once because 
            //it can use too much memory (several hundreds of GB). So, we propagate everything 
            //for a given gene, write results in files, and move to the next gene.
            
            // We always load global expression calls, because we always try 
            // to match expression calls with potentially conflicting no-expression calls
            // (generated by different data types, as there can be no conflict for a given 
            // data type).
            globalExprTOsByGeneIds = 
                    this.loadExprCallsByGeneIds(speciesFilter, true, nonInformativesAnatEntities);
    
            // We always load propagated global no-expression calls, because we always try 
            // to match no-expression calls with potentially conflicting expression calls 
            // (generated by different data types, as there can be no conflict for a given 
            // data type).
            globalNoExprTOsByGeneIds = 
                    this.loadNoExprCallsByGeneIds(speciesFilter, true, nonInformativesAnatEntities);
            
            //In order to propagate expression calls to parent stages, we need to retrieve 
            //relations between stages.
            stageParentsFromChildren = 
                    BgeeDBUtils.getStageParentsFromChildren(speciesFilter, this.getRelationDAO());
            
            log.trace("Done retrieving data for expression files for the species {}.", speciesId);
    
        } else {
            log.trace("Start retrieving data for differential expression files for the species {}...", 
                    speciesId);
    
            //we retrieve differential expression calls grouped by geneIds. 
            diffExprTOsByGeneIds =  
                    this.loadDiffExprCallsByGeneId(speciesFilter, factor, generateAdvancedFile);
    
            log.trace("Done retrieving data for differential expression files for the species {}.", 
                    speciesId);
        }
        
        //****************************
        // PRODUCE AND WRITE DATA
        //****************************
        log.trace("Start generating and writing file content for species {} and file types {}...", 
                speciesId, fileTypes);
        
        //now, we write all requested differential expression files at once. This way, we will 
        //generate the data only once, and we will not have to store them in memory (the memory  
        //usage could be huge).
        //OK, first we allow to store file names, writers, etc, associated to a FileType, 
        //for the catch and finally clauses. 
        Map<FileType, String> generatedFileNames = new HashMap<FileType, String>();
        //we will write results in temporary files that we will rename at the end 
        //if everything is correct
        String tmpExtension = ".tmp";
        //in order to close all writers in a finally clause
        Map<FileType, ICsvMapWriter> writersUsed = new HashMap<FileType, ICsvMapWriter>();
        try {
            //**************************
            // OPEN FILES, CREATE WRITERS, WRITE HEADERS
            //**************************
            Map<FileType, CellProcessor[]> processors = new HashMap<FileType, CellProcessor[]>();
            Map<FileType, String[]> headers = new HashMap<FileType, String[]>();
            
            for (FileType fileType: fileTypes) {
                CellProcessor[] fileTypeProcessors = null;
                String[] fileTypeHeaders = null;
                if (!fileType.isExpressionFileType() && 
                    !(fileType.isDiffExprFileType() && fileType.getComparisonFactor().equals(factor))) {
                    log.trace("This file type ({}) is not manage", fileType.getStringRepresentation());
                    continue;
                }
                if (fileType.isDiffExprFileType() && fileType.getComparisonFactor().equals(factor)) {
                    fileTypeProcessors = this.generateDiffExprFileCellProcessors(fileType);
                    processors.put(fileType, fileTypeProcessors);
                    fileTypeHeaders = this.generateDiffExprFileHeader(fileType);
                    headers.put(fileType, fileTypeHeaders);
                    log.trace("headers.put("+fileType+", "+fileTypeHeaders+");");
                } else if (fileType.isExpressionFileType()) {
                    fileTypeProcessors = this.generateExprFileCellProcessors(fileType);
                    processors.put(fileType, fileTypeProcessors);
                    fileTypeHeaders = this.generateExprFileHeader(fileType);
                    headers.put(fileType, fileTypeHeaders);
                    log.trace("headers.put("+fileType+", "+fileTypeHeaders+");");
                }
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
            List<String> orderedGeneIds = null;
            if (factor == null) {
                Set<String> geneIds = new HashSet<String>();
                geneIds.addAll(globalExprTOsByGeneIds.keySet());
                geneIds.addAll(globalNoExprTOsByGeneIds.keySet());
                orderedGeneIds = new ArrayList<String>(geneIds);
            } else {
                orderedGeneIds =
                        new ArrayList<String>(new HashSet<String>(diffExprTOsByGeneIds.keySet()));
            }
            Collections.sort(orderedGeneIds);
            
            //now, we generate and write data one gene at a time to not overload memory.
            int geneCount = 0;
            for (String geneId: orderedGeneIds) {
                geneCount++;
                if (log.isDebugEnabled() && geneCount % 2000 == 0) {
                    log.debug("Iterating gene {} over {}", geneCount, orderedGeneIds.size());
                }
                
                SortedMap<CallTO, Collection<CallTO>> groupedSortedCallTOs = null;
                Set<CallTO> allCallTOs = null;
                if (factor == null) {
                    //OK, first, we need to propagate expression calls to parent stages 
                    //(calls were retrieved with propagation to parent organs already performed)
                    Set<ExpressionCallTO> stagePropagatedExprCallTOs = new HashSet<ExpressionCallTO>();
                    //remove calls from Map to free some memory
                    Set<ExpressionCallTO> exprCallTOs = globalExprTOsByGeneIds.remove(geneId);
                    if (exprCallTOs != null && !exprCallTOs.isEmpty()) {
                        stagePropagatedExprCallTOs = this.updateGlobalExpressions(
                                this.groupExpressionCallTOsByPropagatedCalls(
                                    exprCallTOs, stageParentsFromChildren, false), 
                                false, true).keySet();
                    }
                    //now, we need to aggregate expression and no-expression calls, and to order them.
                    allCallTOs = new HashSet<CallTO>();
                    allCallTOs.addAll(stagePropagatedExprCallTOs);
                    //remove calls from Map to free some memory
                    Set<NoExpressionCallTO> noExprCallTOs = globalNoExprTOsByGeneIds.remove(geneId);
                    if (noExprCallTOs != null) {
                        allCallTOs.addAll(noExprCallTOs);
                    }
                    groupedSortedCallTOs = this.groupAndOrderByGeneAnatEntityStage(allCallTOs);
    
                } else {
                    //remove calls from Map to free some memory
                    allCallTOs = new HashSet<CallTO>(diffExprTOsByGeneIds.remove(geneId));
                }
                groupedSortedCallTOs = this.groupAndOrderByGeneAnatEntityStage(allCallTOs);                    
    
                //and now, we compute and write the rows in all files
                for (Entry<CallTO, Collection<CallTO>> callGroup : groupedSortedCallTOs.entrySet()) {
                    if (!geneId.equals(callGroup.getKey().getGeneId())) {
                        throw log.throwing(new IllegalStateException("Grouped calls should " +
                                "have the gene ID " + geneId + ": " + callGroup));
                    }
                    String stageId = callGroup.getKey().getStageId();
                    String anatEntityId = callGroup.getKey().getAnatEntityId();
                    
                    for (Entry<FileType, ICsvMapWriter> writerFileType: writersUsed.entrySet()) {
                        Map<String, String> row = null;
                        try {
                            if (factor == null) {
                                row = this.generateExprRow(geneId, geneNamesByIds.get(geneId), 
                                        stageId, stageNamesByIds.get(stageId), 
                                        anatEntityId, anatEntityNamesByIds.get(anatEntityId), 
                                        callGroup.getValue(), writerFileType.getKey());
                            } else {
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
            }
        } catch (Exception e) {
            for (String fileName: generatedFileNames.values()) {
                //if tmp file exists, remove it.
                File file = new File(directory, fileName + tmpExtension);
                if (file.exists()) {
                    file.delete();
                }
            }
            throw e;
        } finally {
            for (ICsvMapWriter writer: writersUsed.values()) {
                writer.close();
            }
        }
        
        //now, if everything went fine, we rename the temp files
        for (String fileName: generatedFileNames.values()) {
            //if temporary file exists, rename it.
            File tmpFile = new File(directory, fileName + tmpExtension);
            File file = new File(directory, fileName);
            if (tmpFile.exists()) {
                tmpFile.renameTo(file);
            }
        }
    
        log.debug("Done generating differential expression files for the species {} and file types {}.", 
                speciesId, fileTypes);
        log.exit();
    }

    /**
     * Generates an {@code Array} of {@code CellProcessor}s used to process 
     * an expression TSV file of type {@code fileType}.
     * 
     * @param fileType  The {@code FileType} of the file to be generated.
     * @return          An {@code Array} of {@code CellProcessor}s used to process 
     *                  an expression file.
     * @throw IllegalArgumentException  If {@code fileType} is not managed by this method.
     */
    private CellProcessor[] generateExprFileCellProcessors(FileType fileType) 
            throws IllegalArgumentException {
        log.entry(fileType);
        
        if (!fileType.equals(FileType.EXPR_SIMPLE) && 
                !fileType.equals(FileType.EXPR_COMPLETE)) {
            throw log.throwing(new IllegalArgumentException("File type not handled " +
            		"by this method: " + fileType));
        }

        List<Object> dataElements = new ArrayList<Object>();
        for (ExpressionData data : ExpressionData.values()) {
            dataElements.add(data.getStringRepresentation());
        } 
        List<Object> originElement = new ArrayList<Object>();
        for (ObservedData data : ObservedData.values()) {
            originElement.add(data.getStringRepresentation());
        } 
        if (fileType.isSimpleFileType()) {
            return log.exit(new CellProcessor[] { 
                    new StrNotNullOrEmpty(), // gene ID
                    new NotNull(), // gene Name
                    new StrNotNullOrEmpty(), // developmental stage ID
                    new StrNotNullOrEmpty(), // developmental stage name
                    new StrNotNullOrEmpty(), // anatomical entity ID
                    new StrNotNullOrEmpty(), // anatomical entity name
                    new IsElementOf(dataElements)}); // Expression
        } 
        return log.exit(new CellProcessor[] { 
                new StrNotNullOrEmpty(), // gene ID
                new NotNull(), // gene Name
                new StrNotNullOrEmpty(), // developmental stage ID
                new StrNotNullOrEmpty(), // developmental stage name
                new StrNotNullOrEmpty(), // anatomical entity ID
                new StrNotNullOrEmpty(), // anatomical entity name
                new IsElementOf(dataElements),  // Affymetrix data
                new IsElementOf(dataElements),  // EST data
                new IsElementOf(dataElements),  // In Situ data
                //TODO: when relaxed in situ will be used, uncomment following line
                //                        new IsElementOf(dataElements),  // Relaxed in Situ data
                new IsElementOf(dataElements),  // RNA-seq data
                new IsElementOf(originElement), // Including observed data 
                new IsElementOf(dataElements)}); // Expression
    }

    /**
     * Generates an {@code Array} of {@code String}s used to generate the header of  
     * an expression TSV file of type {@code fileType}.
     * 
     * @param fileType  The {@code FileType} of the file to be generated.
     * @return          An {@code Array} of {@code String}s used to produce the header.
     * @throw IllegalArgumentException  If {@code fileType} is not managed by this method.
     */
    private String[] generateExprFileHeader(FileType fileType) 
            throws IllegalArgumentException {
        log.entry(fileType);
        
        if (fileType.isSimpleFileType()) {
            return log.exit(new String[] { 
                    GENE_ID_COLUMN_NAME, GENE_NAME_COLUMN_NAME, 
                    STAGE_ID_COLUMN_NAME, STAGE_NAME_COLUMN_NAME,
                    ANATENTITY_ID_COLUMN_NAME, ANATENTITY_NAME_COLUMN_NAME,
                    EXPRESSION_COLUMN_NAME});
        } 
        // TODO For the moment, we do not write relaxed in situ column 
        // because there is no data in the database. 
        return log.exit(new String[] {
                GENE_ID_COLUMN_NAME, GENE_NAME_COLUMN_NAME, 
                STAGE_ID_COLUMN_NAME, STAGE_NAME_COLUMN_NAME,   
                ANATENTITY_ID_COLUMN_NAME, ANATENTITY_NAME_COLUMN_NAME,
                AFFYMETRIX_DATA_COLUMN_NAME, ESTDATA_COLUMN_NAME, INSITUDATA_COLUMN_NAME, 
                //                  RELAXEDINSITUDATA_COLUMN_NAME, 
                RNASEQ_DATA_COLUMN_NAME, 
                INCLUDING_OBSERVED_DATA_COLUMN_NAME, EXPRESSION_COLUMN_NAME});
        
    }
    
    /**
     * Generate a row to be written in an expression download file. This methods will notably 
     * use {@code callTOs} to produce expression information, that is different depending on 
     * {@code fileType}. The results are returned as a {@code Map}; it can be {@code null} 
     * if the {@code callTOs} provided do not allow to generate information to be included 
     * in the file of the given {@code FileType}.
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
     * {@link #EXPRESSION_COLUMN_NAME}.
     * <li>information generated for files of the type {@link FileType EXPR_COMPLETE}: 
     * entries with keys equal to {@link #INCLUDING_OBSERVED_DATA_COLUMN_NAME}, 
     * {@link #AFFYMETRIX_DATA_COLUMN_NAME}, {@link #ESTDATA_COLUMN_NAME}, 
     * {@link #INSITUDATA_COLUMN_NAME}, {@link #RELAXEDINSITUDATA_COLUMN_NAME}, 
     * {@link #RNASEQDATA_COLUMN_NAME}.
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
     * @param callTOs           A {@code Collection} of {@code CallTOs}, either 
     *                          {@code ExpressionCallTOs} or {@code NoExpressionCallTOs}, 
     *                          occurring in a same gene-anat. entity-stage triplet, 
     *                          corresponding to {@code geneId}, {@code stageId}, 
     *                          {@code anatEntityId}.
     * @param fileType          The {@code FileType} defining which type of file should be 
     *                          generated.
     * @return                  A {@code Map} containing the generated information. {@code null} 
     *                          if no information should be generated for the provided 
     *                          {@code fileType}.
     * @throw IllegalArgumentException  If some information is missing, or data provided 
     *                                  are inconsistent. 
     */
    //TODO: divide the Expression column into two columns: Expression and Quality columns
    private Map<String, String> generateExprRow(String geneId, String geneName, 
            String stageId, String stageName, String anatEntityId, String anatEntityName, 
            Collection<CallTO> callTOs, FileType fileType) throws IllegalArgumentException {
        log.entry(geneId, geneName, stageId, stageName, anatEntityId, anatEntityName, 
                callTOs, fileType);
        
        Map<String, String> row = new HashMap<String, String>();

        // ********************************
        // Set IDs and names
        // ********************************        
        this.addIdsAndNames(row, geneId, geneName, anatEntityId, anatEntityName, stageId, stageName);

        // ********************************
        // Set simple file columns
        // ********************************
        //the current version of this method assumes that there will never be a mixture 
        //of global propagated calls and of basic calls. This is why we only have 
        //one ExpressionCallTO, and one NoExpressionCallTO.
        ExpressionCallTO expressionTO = null;
        NoExpressionCallTO noExpressionTO = null;

        for (CallTO call: callTOs) {
            if (!call.getGeneId().equals(geneId) || 
                    !call.getAnatEntityId().equals(anatEntityId) || 
                    !call.getStageId().equals(stageId)) {
                throw log.throwing(new IllegalArgumentException("Incorrect correspondances " +
                		"between calls and IDs provided, for call: " + call));
            }
            if (call instanceof  ExpressionCallTO) {
                if (expressionTO == null) {
                    expressionTO = (ExpressionCallTO) call;  
                    if (!expressionTO.isIncludeSubstructures() || 
                            !expressionTO.isIncludeSubStages()) {
                        throw log.throwing(new IllegalArgumentException("The provided " +
                                "ExpressionCallTO should be a global expression call"));
                    }
                } else {
                    throw log.throwing(new IllegalArgumentException("The provided CallTO list(" +
                            call.getClass() + ") contains severals expression calls"));
                }
            } else if (call instanceof NoExpressionCallTO){
                if (noExpressionTO == null) {
                    noExpressionTO = (NoExpressionCallTO) call;
                    if (!noExpressionTO.isIncludeParentStructures()) {
                        throw log.throwing(new IllegalArgumentException("The provided " +
                                "NoExpressionCallTO should be a global no-expression call"));
                    }
                } else {
                    throw log.throwing(new IllegalArgumentException("The provided CallTO list(" +
                            call.getClass() + ") contains severals no-expression calls"));
                }
            } else {
                throw log.throwing(new IllegalArgumentException("The CallTO provided (" +
                        call.getClass() + ") is not managed for expression/no-expression data: " + 
                        call));
            }
        }

        if (expressionTO == null && noExpressionTO == null) {
            throw log.throwing(new IllegalArgumentException("No global call " +
                    "for the triplet gene (" + geneId + 
                    ") - organ (" + anatEntityId + 
                    ") - stage (" + stageId + ")"));
        }
        if (expressionTO != null) {
            if (isCallWithNoData(expressionTO)) {
                throw log.throwing(new IllegalArgumentException("All data states of " +
                		"the expression call (" + expressionTO + ") are set to no data"));
            }
            if (expressionTO.isObservedData() == null) {
                throw log.throwing(new IllegalArgumentException("An ExpressionCallTO " +
                		"does not allow to determine origin of the data: " + expressionTO));
            }
        }
        if (noExpressionTO != null) {
            if (isCallWithNoData(noExpressionTO)) {
                throw log.throwing(new IllegalArgumentException("All data states of " +
                		"the no-expression call (" + noExpressionTO + ") are set to no data"));
            }
            if (noExpressionTO.getOriginOfLine() == null) {
                throw log.throwing(new IllegalArgumentException("An NoExpressionCallTO " +
                		"does not allow to determine origin of the data: " + noExpressionTO));
            }
        }

        // Define if the call include observed data
        ObservedData observedData = ObservedData.NOT_OBSERVED; 
        if ((expressionTO != null && !isPropagatedOnly(expressionTO))
                || (noExpressionTO != null && !isPropagatedOnly(noExpressionTO))) {
            // stage and anatomical entity not propagated in the expression call 
            // OR anatomical entity not propagated in the no-expression call
            observedData = ObservedData.OBSERVED;
        }

        // We do not write calls in simple file if there are not observed.
        if (fileType.isSimpleFileType() && observedData.equals(ObservedData.NOT_OBSERVED)) {
            return log.exit(null);                
        }

        // Define summary column
        ExpressionData summary = ExpressionData.NO_DATA;
        if (expressionTO != null && noExpressionTO != null) {
            if (noExpressionTO.getOriginOfLine().equals(NoExpressionCallTO.OriginOfLine.PARENT)) {
                summary = ExpressionData.LOW_AMBIGUITY;                    
            } else {
                summary = ExpressionData.HIGH_AMBIGUITY;
            }
        } else if (expressionTO != null) {
            Set<DataState> allDataState = EnumSet.of(
                    expressionTO.getAffymetrixData(), expressionTO.getESTData(), 
                    expressionTO.getInSituData(), expressionTO.getRNASeqData());
            if (allDataState.contains(DataState.HIGHQUALITY)) {
                summary = ExpressionData.HIGH_QUALITY;
            } else {
                summary = ExpressionData.LOW_QUALITY;
            }
        } else if (noExpressionTO != null) {
            summary = ExpressionData.NO_EXPRESSION;
        } 
        row.put(EXPRESSION_COLUMN_NAME, summary.getStringRepresentation());

        //following columns are generated only for complete files
        if (fileType.isSimpleFileType()) {
            return log.exit(row);
        }
        

        // ********************************
        // Set complete file columns
        // ********************************
        // Write if the call include observed data
        row.put(INCLUDING_OBSERVED_DATA_COLUMN_NAME, observedData.getStringRepresentation());

        // Define data state for each data types
        if(expressionTO == null) {
            expressionTO = new ExpressionCallTO(null, null, null, null, 
                    DataState.NODATA, DataState.NODATA, DataState.NODATA, DataState.NODATA, 
                    null, null, null, null, null);
        }
        if (noExpressionTO == null) {
            noExpressionTO = new NoExpressionCallTO(null, null, null, null, 
                    DataState.NODATA, DataState.NODATA, DataState.NODATA, DataState.NODATA, 
                    null, null);
        }

        // Define data state for each data type
        try {
            row.put(AFFYMETRIX_DATA_COLUMN_NAME, mergeExprAndNoExprDataStates(
                    expressionTO.getAffymetrixData(), noExpressionTO.getAffymetrixData()).
                    getStringRepresentation());
            row.put(ESTDATA_COLUMN_NAME, this.mergeExprAndNoExprDataStates(
                    expressionTO.getESTData(), DataState.NODATA).
                    getStringRepresentation());
            row.put(INSITUDATA_COLUMN_NAME, this.mergeExprAndNoExprDataStates(
                    expressionTO.getInSituData(), noExpressionTO.getInSituData()).
                    getStringRepresentation());
            // TODO For the moment, we do not write relaxed in situ column 
            // because there is no data in the database. 
//            row.put(RELAXEDINSITUDATA_COLUMN_NAME, this.mergeExprAndNoExprDataStates
//                    (DataState.NODATA, noExpressionTO.getRelaxedInSituData()).
//                    getStringRepresentation());
            row.put(RNASEQ_DATA_COLUMN_NAME, this.mergeExprAndNoExprDataStates(
                    expressionTO.getRNASeqData(), noExpressionTO.getRNASeqData()).
                    getStringRepresentation());
        } catch (Exception e) {
            throw log.throwing(new IllegalArgumentException("Incorrect data states, " +
                    "ExpressionCallTO: " + expressionTO + ", NoExpressionCallTo: " + 
                    noExpressionTO, e));
        }
        
        return log.exit(row);
    }

    /**
     * Add to the provides row gene, anatomical entity, and stage IDs and names.
     * <p>
     * The provided {@code Map} will be modified.
     *
     * @param row               A {@code Map} where keys are {@code String}s that are column names,
     *                          the associated values being a {@code String} that is the value
     *                          for the call. 
     * @param geneId            A {@code String} that is the ID of the gene.
     * @param geneName          A {@code String} that is the name of the gene.
     * @param anatEntityId      A {@code String} that is the ID of the anatomical entity.
     * @param anatEntityName    A {@code String} that is the name of the anatomical entity.
     * @param stageId           A {@code String} that is the ID of the stage.
     * @param stageName         A {@code String} that is the name of the stage.
     */
    private void addIdsAndNames(Map<String, String> row, String geneId, String geneName, 
            String anatEntityId, String anatEntityName, String stageId, String stageName) {
        log.entry(row, geneId, geneName, anatEntityId, anatEntityName, stageId, stageName);
        
        if (StringUtils.isBlank(geneId)) {
            throw log.throwing(new IllegalArgumentException("No Id provided for gene."));
        }
        if (StringUtils.isBlank(stageId)) {
            throw log.throwing(new IllegalArgumentException("No Id provided for stage."));
        }
        if (StringUtils.isBlank(anatEntityId)) {
            throw log.throwing(new IllegalArgumentException("No Id provided for anat entity."));
        }
        //gene name can sometimes be empty, we don't check it
        if (StringUtils.isBlank(anatEntityName)) {
            throw log.throwing(new IllegalArgumentException("No name provided " +
                    "for anatomical entity ID " + anatEntityId));
        }
        if (StringUtils.isBlank(stageName)) {
            throw log.throwing(new IllegalArgumentException("No name provided " +
                    "for stage ID " + stageId));
        }
        row.put(GENE_ID_COLUMN_NAME, geneId);
        row.put(GENE_NAME_COLUMN_NAME, geneName);
        row.put(ANATENTITY_ID_COLUMN_NAME, anatEntityId);
        row.put(ANATENTITY_NAME_COLUMN_NAME, anatEntityName);
        row.put(STAGE_ID_COLUMN_NAME, stageId);
        row.put(STAGE_NAME_COLUMN_NAME, stageName);

        log.exit();
    }

    /**
     * Merge {@code DataState}s of one expression call and one no-expression call into 
     * an {@code ExpressionData}. 
     * 
     * @param dataStateExpr     A {@code DataState} from an expression call. 
     * @param dataStateNoExpr   A {@code DataState} from a no-expression call.
     * @return                  An {@code ExpressionData} combining 
     *                          {@code DataState}s of one expression call and one no-expression call.
     * @throws IllegalStateException    If an expression call and a no-expression call are found 
     *                                  for the same data type.
     */
    private ExpressionData mergeExprAndNoExprDataStates(DataState dataStateExpr, 
            DataState dataStateNoExpr) throws IllegalStateException {
        log.entry(dataStateExpr, dataStateNoExpr);
    
        //no data at all
        if (dataStateExpr == DataState.NODATA && dataStateNoExpr == DataState.NODATA) {
            return log.exit(ExpressionData.NO_DATA);
        }
        if (dataStateExpr != DataState.NODATA && dataStateNoExpr != DataState.NODATA) {
            throw log.throwing(new IllegalStateException("An expression call and " +
            		"a no-expression call could be found for the same data type."));
        }
        //no no-expression data, we use the expression data
        if (dataStateExpr != DataState.NODATA) {
            if (dataStateExpr.equals(DataState.HIGHQUALITY)) {
                return log.exit(ExpressionData.HIGH_QUALITY);
            } 
            if (dataStateExpr.equals(DataState.LOWQUALITY)) {
                return log.exit(ExpressionData.LOW_QUALITY); 
            } 
            throw log.throwing(new IllegalArgumentException(
                    "The DataState provided (" + dataStateExpr.getStringRepresentation() + 
                    ") is not supported"));  
        } 
        
        if (dataStateNoExpr != DataState.NODATA) {
            //no-expression data available
            return log.exit(ExpressionData.NO_EXPRESSION);
        }
        
        throw log.throwing(new AssertionError("All logical conditions should have been checked."));
    }
    
    /**
     * Validate and retrieve information for the provided species IDs, or for all species 
     * if {@code speciesIds} is {@code null} or empty, and returns a {@code Map} 
     * where keys are the species IDs, the associated values being a {@code String} that can be 
     * conveniently used to construct download file names for the associated species. 
     * It is the latin name with all whitespace replaced by "_".
     * <p>
     * If a species ID could not be identified, an {@code IllegalArgumentException} is thrown.
     * 
     * @param speciesIds    A {@code Set} of {@code String}s that are the species IDs 
     *                      to be checked, and for which to generate a {@code String} 
     *                      used to construct download file names. Can be {@code null} or empty 
     *                      to retrieve information for all species. 
     * @return              A {@code Map} where keys are {@code String}s that are the species IDs, 
     *                      the associated values being a {@code String} that is its latin name, 
     *                      with whitespace replaced by "_".
     */
    private Map<String, String> checkAndGetLatinNamesBySpeciesIds(Set<String> speciesIds) {
        log.entry(speciesIds);
        
        Map<String, String> namesByIds = new HashMap<String, String>();
        SpeciesDAO speciesDAO = this.getSpeciesDAO();
        speciesDAO.setAttributes(SpeciesDAO.Attribute.ID, SpeciesDAO.Attribute.GENUS, 
                SpeciesDAO.Attribute.SPECIES_NAME);
        
        try (SpeciesTOResultSet rs = speciesDAO.getSpeciesByIds(speciesIds)) {
            while (rs.next()) {
                SpeciesTO speciesTO = rs.getTO();
                if (StringUtils.isBlank(speciesTO.getId()) || 
                        StringUtils.isBlank(speciesTO.getGenus()) || 
                        StringUtils.isBlank(speciesTO.getSpeciesName())) {
                    throw log.throwing(new IllegalStateException("Incorrect species " +
                    		"information retrieved: " + speciesTO));
                    
                }
                //in case there is a white space in a species name, we do not simply 
                //concatenate using "_", we replace all white spaces
                String latinNameForFile = 
                        speciesTO.getGenus() + " " + speciesTO.getSpeciesName();
                latinNameForFile = latinNameForFile.replace(" ", "_");
                namesByIds.put(speciesTO.getId(), latinNameForFile);
            }
        }
        if (namesByIds.size() < speciesIds.size()) {
            //copy to avoid modifying user input, maybe the caller 
            //will recover from the exception
            Set<String> copySpeciesIds = new HashSet<String>(speciesIds);
            copySpeciesIds.removeAll(namesByIds.keySet());
            throw log.throwing(new IllegalArgumentException("Some species IDs provided " +
            		"do not correspond to any species: " + copySpeciesIds));
        } else if (namesByIds.size() > speciesIds.size()) {
            throw log.throwing(new IllegalStateException("An ID should always be associated " +
            		"to only one species..."));
        }
        
        return log.exit(namesByIds);
    }

    /**
     * Generates an {@code Array} of {@code CellProcessor}s used to process 
     * a differential expression TSV file of type {@code fileType}.
     * 
     * @param fileType  The {@code FileType} of the file to be generated.
     * @return          An {@code Array} of {@code CellProcessor}s used to process 
     *                  a differential expression file.
     * @throw IllegalArgumentException  If {@code fileType} is not managed by this method.
     */
    private CellProcessor[] generateDiffExprFileCellProcessors(FileType fileType) 
            throws IllegalArgumentException {
        log.entry(fileType);
        
        if (!fileType.equals(FileType.DIFF_EXPR_SIMPLE_ANAT_ENTITY) && 
                !fileType.equals(FileType.DIFF_EXPR_COMPLETE_ANAT_ENTITY) && 
                !fileType.equals(FileType.DIFF_EXPR_SIMPLE_STAGE) && 
                !fileType.equals(FileType.DIFF_EXPR_COMPLETE_STAGE)) {
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
        resumeQualities.add(GenerateDownloadFile.NA_VALUE);
        
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
     * @param fileType  The {@code FileType} of the file to be generated.
     * @return          An {@code Array} of {@code String}s used to produce the header.
     * @throw IllegalArgumentException  If {@code fileType} is not managed by this method.
     */
    private String[] generateDiffExprFileHeader(FileType fileType) throws IllegalArgumentException {
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
     * depending on {@code fileType}. The results are returned as a {@code Map}; it can be 
     * {@code null} if the {@code callTOs} provided do not allow to generate information to be 
     * included in the file of the given {@code FileType}.
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
     * {@link FileType DIFF_EXPR_COMPLETE_ANAT_ENTITY} or {@link FileType DIFF_EXPR_COMPLETE_STAGE}: 
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
     * @param to                A {@DiffExpressionCallTO} corresponding to {@code geneId}, 
     *                          {@code stageId}, {@code anatEntityId}.
     * @param fileType          The {@code FileType} defining which type of file should be 
     *                          generated.
     * @return                  A {@code Map} containing the generated information. {@code null} 
     *                          if no information should be generated for the provided 
     *                          {@code fileType}.
     */
    private Map<String, String> generateDiffExprRow(String geneId, String geneName, 
            String stageId, String stageName, String anatEntityId, String anatEntityName, 
            DiffExpressionCallTO to, FileType fileType) {
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
     * Add, to the provides {@ code row}, merged {@code DataState}s and qualities.
     * <p>
     * The provided {@code Map} will be modified.
     *
     * @param fileType          The {@code FileType} defining which type of file should be 
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
     */
    private boolean addDiffExprCallMergedDataToRow(FileType fileType, Map<String, String> row,
            DiffExprCallType affymetrixType, DataState affymetrixQuality, 
            DiffExprCallType rnaSeqType, DataState rnaSeqQuality) {
        log.entry(row, affymetrixType, affymetrixQuality, rnaSeqType, rnaSeqQuality);
        
        DiffExpressionData summary = DiffExpressionData.NO_DATA;
        String quality = GenerateDownloadFile.NA_VALUE;

        Set<DiffExprCallType> allType = EnumSet.of(affymetrixType, rnaSeqType);
        Set<DataState> allDataQuality = EnumSet.of(affymetrixQuality, rnaSeqQuality);
        
        if (allType.size() == 1 && 
                (affymetrixType.equals(DiffExprCallType.NOT_EXPRESSED) ||
                        affymetrixType.equals(DiffExprCallType.NO_DATA))) {
            throw log.throwing(new IllegalArgumentException("One call could't be only "+
                    DiffExprCallType.NOT_EXPRESSED.getStringRepresentation() + " or " + 
                    DiffExprCallType.NO_DATA.getStringRepresentation()));
        }

        if ((allType.contains(DiffExprCallType.UNDER_EXPRESSED) &&
                allType.contains(DiffExprCallType.OVER_EXPRESSED)) ||
            (allType.contains(DiffExprCallType.NOT_EXPRESSED) &&
                allType.contains(DiffExprCallType.NOT_DIFF_EXPRESSED))) {
            summary = DiffExpressionData.STRONG_AMBIGUITY;
            quality = GenerateDownloadFile.NA_VALUE;

        } else if (affymetrixType.equals(rnaSeqType) || allType.contains(DiffExprCallType.NO_DATA)) {
            DiffExprCallType type = affymetrixType;
            if (affymetrixType.equals(DiffExprCallType.NO_DATA)) {
                type = rnaSeqType;
            }
            switch (type) {
                case OVER_EXPRESSED: 
                    summary = DiffExpressionData.OVER_EXPRESSED;
                    break;
                case UNDER_EXPRESSED: 
                    summary = DiffExpressionData.UNDER_EXPRESSED;
                    break;
                case NOT_DIFF_EXPRESSED: 
                    summary = DiffExpressionData.NOT_DIFF_EXPRESSED;
                    //we don't write 'not expressed' calls in simple file 
                    if (fileType.isSimpleFileType()) {
                        return log.exit(false);
                    }
                    break;
                case NOT_EXPRESSED:
                    summary = DiffExpressionData.NOT_EXPRESSED;
                    break;
                default:
                    throw log.throwing(new IllegalArgumentException(
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
            quality = GenerateDownloadFile.NA_VALUE;

        } else if (allType.contains(DiffExprCallType.NOT_EXPRESSED) && 
                allType.contains(DiffExprCallType.UNDER_EXPRESSED)) {
            summary = DiffExpressionData.UNDER_EXPRESSED;
            quality = DataState.LOWQUALITY.getStringRepresentation();
        }

        row.put(DIFFEXPRESSION_COLUMN_NAME, summary.getStringRepresentation());
        row.put(QUALITY_COLUMN_NAME, quality);

        return log.exit(true);
    }
}

