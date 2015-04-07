package org.bgee.pipeline.expression.downloadfile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.anatdev.mapping.StageGroupingDAO;
import org.bgee.model.dao.api.anatdev.mapping.StageGroupingDAO.GroupToStageTO;
import org.bgee.model.dao.api.anatdev.mapping.StageGroupingDAO.GroupToStageTOResultSet;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SimAnnotToAnatEntityTO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SimAnnotToAnatEntityTOResultSet;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SummarySimilarityAnnotationTO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SummarySimilarityAnnotationTOResultSet;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.ComparisonFactor;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.DiffExprCallType;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTOResultSet;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallParams;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTOResultSet;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalGroupToGeneTO;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalGroupToGeneTOResultSet;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTO;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTOResultSet;
import org.bgee.model.dao.api.species.TaxonDAO;
import org.bgee.model.dao.api.species.TaxonDAO.TaxonTOResultSet;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.pipeline.BgeeDBUtils;
import org.bgee.pipeline.CommandRunner;
import org.bgee.pipeline.Utils;
import org.supercsv.cellprocessor.Collector;
import org.supercsv.cellprocessor.constraint.DMinMax;
import org.supercsv.cellprocessor.constraint.IsElementOf;
import org.supercsv.cellprocessor.constraint.LMinMax;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.dozer.CsvDozerBeanWriter;
import org.supercsv.io.dozer.ICsvDozerBeanWriter;


/**
 * Class used to generate multi-species differential expression TSV download files 
 * (simple and advanced files) from the Bgee database. 
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
     * A {@code Map} where keys are {@code String}s that are taxon IDs, the associated values 
     * being {@code Set} of {@code String}s corresponding to species IDs of the given taxon ID.
     */
    private Map<String,Set<String>> providedGroups;
    
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
    //XXX: why doesn't it reuse code from single species download files?
    public enum DiffExpressionData {
        NO_DATA("no data"), NOT_EXPRESSED("not expressed"), OVER_EXPRESSION("over-expression"), 
        UNDER_EXPRESSION("under-expression"), NOT_DIFF_EXPRESSION("no diff expression"), 
        WEAK_AMBIGUITY("weak ambiguity"), STRONG_AMBIGUITY("strong ambiguity");

        private final String stringRepresentation;

        /**
         * Convert the {@code String} representation of a differential expression data 
         * into a {@code DiffExpressionData}. Operation performed by calling 
         * {@link TransferObject#convert(Class, String)} with {@code DiffExpressionData} as the 
         * {@code Class} argument, and {@code representation} as the {@code String} argument.
         * 
         * @param representation    A {@code String} representing a differential expression data.
         * @return                  A {@code DiffExpressionData} corresponding to 
         *                          {@code representation}.
         * @throw IllegalArgumentException  If {@code representation} does not correspond 
         *                                  to any {@code DiffExprCallType}.
         * @see #convert(Class, String)
         */
        //DRY from TransfertObject
        public static final DiffExpressionData convertToDiffExpressionData(String representation) {
            log.entry(representation);
            
            if (representation == null) {
                return log.exit(null);
            }
            for (DiffExpressionData element: DiffExpressionData.class.getEnumConstants()) {
                if (element.getStringRepresentation().equals(representation) || 
                        element.name().equals(representation)) {
                    return log.exit(element);
                }
            }
            throw log.throwing(new IllegalArgumentException("\"" + representation + 
                    "\" does not correspond to any element of " + 
                    DiffExpressionData.class.getName()));
        }
        

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
    //XXX: alternatively, if you use the Bean principle, you could simply use different bean types, 
    //so that you don't need this Enum
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
        this(null, null, null);
    }

    /**
     * Constructor providing parameters to generate files, using the default {@code DAOManager}.
     * 
     * @param providedGroups    A {@code Map} where keys are {@code String}s that are names 
     *                          given to groups of species, the associated value being 
     *                          a {@code Set} of {@code String}s that are the IDs 
     *                          of the species composing the group.
     * @param fileTypes         A {@code Set} of {@code MultiSpDiffExprFileType}s that are the types
     *                          of files we want to generate. If {@code null} or empty, 
     *                          all {@code MultiSpDiffExprFileType}s are generated.
     * @param directory         A {@code String} that is the directory where to store files.
     * @throws IllegalArgumentException If {@code directory} is {@code null} or blank.
     */
    public GenerateMultiSpeciesDiffExprFile(Map<String,Set<String>> providedGroups, 
            Set<MultiSpDiffExprFileType> fileTypes, String directory) 
                    throws IllegalArgumentException {
        this(null, providedGroups, fileTypes, directory);
    }

    /**
     * Constructor providing parameters to generate files, and the {@code MySQLDAOManager} that will  
     * be used by this object to perform queries to the database. This is useful for unit testing.
     * 
     * @param manager           The {@code MySQLDAOManager} to use.
     * @param providedGroups    A {@code Map} where keys are {@code String}s that are names 
     *                          given to groups of species, the associated value being 
     *                          a {@code Set} of {@code String}s that are the IDs 
     *                          of the species composing the group.
     * @param fileTypes         A {@code Set} of {@code MultiSpDiffExprFileType}s that are the types
     *                          of files we want to generate. If {@code null} or empty, 
     *                          all {@code MultiSpDiffExprFileType}s are generated.
     * @param directory         A {@code String} that is the directory where to store files.
     * @throws IllegalArgumentException If {@code directory} is {@code null} or blank.
     */
    public GenerateMultiSpeciesDiffExprFile(MySQLDAOManager manager, 
            Map<String,Set<String>> providedGroups, Set<MultiSpDiffExprFileType> fileTypes, 
            String directory) throws IllegalArgumentException {
        super(manager, null, fileTypes, directory);
        this.providedGroups = providedGroups;
    }
    
    /**
     * Main method to trigger the generate multi-species differential expression TSV download files 
     * (simple and advanced) from Bgee database. Parameters that must be provided in order in 
     * {@code args} are: 
     * <ol>
     * <li>a {@code Map} where keys are {@code String}s that are names given 
     *     to groups of species, the associated value being a {@code Set} of {@code String}s 
     *     that are the IDs of the species composing the group. Entries of the {@code Map} 
     *     must be separated by {@link CommandRunner#LIST_SEPARATOR}, keys must be  
     *     separated from their associated value by {@link CommandRunner#KEY_VALUE_SEPARATOR}, 
     *     values must be separated using {@link CommandRunner#VALUE_SEPARATOR}.
     * <li>a list of files types that will be generated ('multi-diffexpr-anatomy-simple' for 
     *     {@link MultiSpDiffExprFileType MULTI_DIFF_EXPR_ANATOMY_SIMPLE}, 
     *     'multi-diffexpr-anatomy-complete' for 
     *     {@link MultiSpDiffExprFileType MULTI_DIFF_EXPR_ANATOMY_COMPLETE}, 
     *     'multi-diffexpr-development-simple' for 
     *     {@link MultiSpDiffExprFileType MULTI_DIFF_EXPR_DEVELOPMENT_SIMPLE}, and 
     *     'multi-diffexpr-development-complete' for 
     *     {@link MultiSpDiffExprFileType MULTI_DIFF_EXPR_DEVELOPMENT_COMPLETE}), separated by the 
     *     {@code String} {@link CommandRunner#LIST_SEPARATOR}. If an empty list is provided 
     *     (see {@link CommandRunner#EMPTY_LIST}), all possible file types will be generated.
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
        GenerateMultiSpeciesDiffExprFile generator = new GenerateMultiSpeciesDiffExprFile(
                CommandRunner.parseMapArgument(args[0]),
                GenerateDownloadFile.convertToFileTypes(
                    CommandRunner.parseListArgument(args[1]), MultiSpDiffExprFileType.class),
                args[2]);
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
    //TODO: re-write javadoc, either by specifying that thee parameters are provided at instantiation, 
    //or by pointing to public getters
    public void generateMultiSpeciesDiffExprFiles() throws IOException {
        //TODO: actually, use another log, these are not method arguments, e.g.: 
        //log.entry();
        //log.info("Start generating blabla with parameters blabla {}", ...)
        log.entry(this.providedGroups, this.fileTypes, this.directory);

        //XXX: should these checks perform at instantiation?
        if (this.providedGroups == null || this.providedGroups.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No group is provided"));
        }

        //XXX: check already performed at instantiation
        if (this.directory == null || this.directory.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No directory is provided"));
        }

        // If no file types are given by user, we set all file types
        if (this.fileTypes == null || this.fileTypes.isEmpty()) {
            this.fileTypes = EnumSet.allOf(MultiSpDiffExprFileType.class);
        }
        
        // We retrieve all CIO so it's common to all groups
        Map<String, CIOStatementTO> cioNamesByIds = this.getCIOStatementsByIds();
        
        for (Entry<String, Set<String>> currentGroup : this.providedGroups.entrySet()) {
            Set<String> setSpecies = currentGroup.getValue();
            if (setSpecies == null || setSpecies.isEmpty()) {
                throw log.throwing(new IllegalArgumentException("No species ID is provided"));
            }
            //Validate provided species, and retrieve species names
            Map<String,String> speciesNamesByIds = 
                    this.checkAndGetLatinNamesBySpeciesIds(setSpecies);
            
            String currentPrefix = currentGroup.getKey();
            String taxonId = this.getLeastCommonAncestor(setSpecies);
            
            // Retrieve gene names, stage names, anat. entity names, and cio names 
            // for all species
            Map<String,String> geneNamesByIds = 
                    BgeeDBUtils.getGeneNamesByIds(setSpecies, this.getGeneDAO());
            Map<String,String> stageNamesByIds = 
                    BgeeDBUtils.getStageNamesByIds(setSpecies, this.getStageDAO());
            Map<String,String> anatEntityNamesByIds = 
                    BgeeDBUtils.getAnatEntityNamesByIds(setSpecies, this.getAnatEntityDAO());

            // Generate multi-species differential expression files
            log.info("Start generating of multi-species diff. expression files for the group {} " +
                    "with the species {} and the ancestral taxon ID {}...", 
                    currentPrefix, speciesNamesByIds.values(), taxonId);

            try {
                //XXX: maybe all the xxxByIds could be stored in class attributes, 
                //to simplify this method signature
                this.generateMultiSpeciesDiffExprFilesForOneGroup(currentPrefix, taxonId, 
                        speciesNamesByIds, geneNamesByIds, stageNamesByIds, anatEntityNamesByIds, 
                        cioNamesByIds);
            } finally {
                // Release resources after each group. 
                this.getManager().releaseResources();
            }
            
            log.info("Done generating of multi-species diff. expression files for the group {}.", 
                    currentGroup);
        }
        log.exit();
    }

    //TODO: javadoc
    private void generateMultiSpeciesDiffExprFilesForOneGroup(String prefix, String taxonId, 
            Map<String,String> speciesNamesByIds, Map<String,String> geneNamesByIds, 
            Map<String,String> stageNamesByIds, Map<String,String> anatEntityNamesByIds, 
            Map<String,CIOStatementTO> cioStatementsByIds) throws IOException {
        //TODO: use actual attributes in logging
        log.entry(this.directory, prefix, this.fileTypes, taxonId, speciesNamesByIds,
                geneNamesByIds, stageNamesByIds, anatEntityNamesByIds, cioStatementsByIds);

        Set<String> speciesFilter = speciesNamesByIds.keySet();

        log.debug("Start generating multi-species differential expression files for:" + 
                " prefix={}, taxon ID={}, species IDs={} and file types {}...", 
                prefix, taxonId, speciesFilter, this.fileTypes);

        // We check that all file types have the same comparison factor and we retrieve it 
        //XXX: why should they all have a same comparison factor?
        ComparisonFactor factor = null;
        for (FileType fileType: this.fileTypes) {
            if (factor == null) {
                factor = ((MultiSpDiffExprFileType)fileType).getComparisonFactor(); 
            } else if (!((MultiSpDiffExprFileType)fileType).getComparisonFactor().equals(factor)) {
                throw log.throwing(new IllegalArgumentException(
                        "All file types do not have the same comparison factor: " + this.fileTypes));
            }
        }
        assert factor != null;

        //********************************
        // RETRIEVE DATA FROM DATA SOURCE
        //********************************
        log.trace("Start retrieving data...");
        
        // Get homologous genes 
        Map<String, String> mapGeneOMANode = this.getMappingGeneIdOMANodeId(taxonId, 
                speciesFilter);
        
        // Get comparable stages
        List<Map<String, List<String>>> mapStageGroup = 
                this.getComparableStages(taxonId, speciesFilter);
        Map<String, List<String>> mapStageIdToStageGroup = mapStageGroup.get(0);
        Map<String, List<String>> mapStageGroupToStageId = mapStageGroup.get(1);

        // Get summary similarity annotations with CIO Ids
        Map<String, String> mapSumSimCIO = this.getSummarySimilarityAnnotations(taxonId);

        // Get 
        List<Map<String, List<String>>> simAnnotToAnatEntity = 
                this.getSimAnnotToAnatEntities(taxonId, speciesFilter);
        Map<String, List<String>> mapSimAnnotToAnatEntities = simAnnotToAnatEntity.get(0);
        Map<String, List<String>> mapAnatEntityToSimAnnot = simAnnotToAnatEntity.get(1);
                
        // Get species ID according to gene ID
        Map<String, String> mapGeneSpecies = this.getMappingGeneSpecies(speciesFilter);

        // Load differential expression calls order by OMA node ID
        //XXX: shouldn't there be a try-with-resources or a try/finally here?
        DiffExpressionCallTOResultSet diffExprRs = 
                this.getDiffExpressionCallsOrderByOMANodeId(taxonId, speciesFilter, factor);
        
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
        Map<MultiSpDiffExprFileType, ICsvDozerBeanWriter> writersUsed = 
                new HashMap<MultiSpDiffExprFileType, ICsvDozerBeanWriter>();
        try {
            //**************************
            // OPEN FILES, CREATE WRITERS, WRITE HEADERS
            //**************************
            Map<MultiSpDiffExprFileType, CellProcessor[]> processors = 
                    new HashMap<MultiSpDiffExprFileType, CellProcessor[]>();
            Map<MultiSpDiffExprFileType, String[]> headers = 
                    new HashMap<MultiSpDiffExprFileType, String[]>();

            // Get ordered species names
            List<String> orderedSpeciesNames = this.getOrderedSpeciesName(
                    speciesFilter, speciesNamesByIds);
            
            for (FileType fileType : this.fileTypes) {
                MultiSpDiffExprFileType currentFileType = (MultiSpDiffExprFileType) fileType;
                if (currentFileType.getComparisonFactor().equals(ComparisonFactor.DEVELOPMENT)) {
                    continue;
                }

                CellProcessor[] fileTypeProcessors = this.generateCellProcessors(
                        currentFileType, speciesFilter.size());
                processors.put(currentFileType, fileTypeProcessors);
                
                String[] fileTypeHeaders = this.generateHeader(currentFileType, orderedSpeciesNames);
                headers.put(currentFileType, fileTypeHeaders);

                // Create file name
                String fileName = prefix + "_" +
                        currentFileType.getStringRepresentation() + EXTENSION;
                generatedFileNames.put(currentFileType, fileName);

                // write in temp file
                File file = new File(this.directory, fileName + tmpExtension);
                // override any existing file
                if (file.exists()) {
                    file.delete();
                }

                // create writer and write header
                ICsvDozerBeanWriter beanWriter = new CsvDozerBeanWriter(new FileWriter(file),
                        Utils.TSVCOMMENTED);
                // configure the mapping from the fields to the CSV columns
                beanWriter.configureBeanMapping(SpeciesCounts.class, 
                        this.generateFieldMapping(currentFileType, speciesNamesByIds.size()));
                beanWriter.writeHeader(fileTypeHeaders);
                writersUsed.put(currentFileType, beanWriter);
            }

            // ****************************
            // WRITE ROWS
            // ****************************
            //we will consider all together the calls mapped to a same OMA group. 
            //This Set will store all calls from the iterated OMA node.
            Set<DiffExpressionCallTO> omaGroupCalls = new HashSet<DiffExpressionCallTO>();
            String previousOMANodeId = null;
            //we iterate the ResultSet, then we do a last iteration after the last TO is retrieved, 
            //to properly group all the calls.
            boolean doIteration = true;
            while (doIteration) {
                doIteration = diffExprRs.next();
                DiffExpressionCallTO currentTO = null;
                String currentOMANodeId = null;
                if (doIteration) {
                    currentTO = diffExprRs.getTO();
                    currentOMANodeId = mapGeneOMANode.get(currentTO.getGeneId());
                }
                //if the OMA group changes, or if it is the latest iteration
                if (!doIteration || //doIteration is false for the latest iteration, AFTER retrieving the last TO
                                    //(ResultSet.isAfterLast would return true)
                    (previousOMANodeId != null && !previousOMANodeId.equals(currentOMANodeId))) {
                    assert (doIteration && currentOMANodeId != null && currentTO != null) || 
                           (!doIteration && currentOMANodeId == null && currentTO == null);
                    //the calls are supposed to be ordered by ascending OMA group ID
                    if (currentOMANodeId != null && 
                            currentOMANodeId.compareTo(previousOMANodeId) <= 0) {
                        throw log.throwing(new IllegalStateException("The expression calls "
                                + "were not retrieved by OMA group ID ascending order, "
                                + "which is mandatory for proper generation of multi-species data."));
                    }
                            
                    if (previousOMANodeId == null) {
                        //if we reach this code block, it means there were no results at all 
                        //retrieved from the ResultSet. This is not formally an error, 
                        //maybe there is no homologous genes with expression 
                        //for the selected species...
                        log.warn("No Expression data retrieved for group {}, taxon LCA {}, composed of species {}", 
                                prefix, taxonId, speciesFilter);
                        break;
                    }
                    
                    // We group calls (without propagation) by comparable anat entities/comparable stages
                    Map<MultiSpeciesCondition, Collection<DiffExpressionCallTO>> 
                        callsGroupByCondition = this.groupByMultiSpeciesCondition(
                                omaGroupCalls, mapAnatEntityToSimAnnot, mapStageIdToStageGroup);
                    //TODO: here, it should be possible to have several calls for a same gene
                    //in a same multi-species condition; in that case, calls for a same gene 
                    //should be "merged".
                    for (Entry<MultiSpeciesCondition, Collection<DiffExpressionCallTO>> entry : 
                        callsGroupByCondition.entrySet()) {
                        this.filterAndWriteConditionGroup(geneNamesByIds, mapGeneSpecies, 
                                stageNamesByIds, anatEntityNamesByIds, cioStatementsByIds,
                                speciesNamesByIds, writersUsed, processors, currentOMANodeId, entry, 
                                mapSumSimCIO, mapSimAnnotToAnatEntities, mapStageGroupToStageId);
                        
                    }
                    // We clear the set containing TOs with the previous OMA Node ID
                    omaGroupCalls.clear();
                }
                if (doIteration) {
                    // We add the current TOs to the group
                    omaGroupCalls.add(currentTO);
                    // We store the current OMA Node ID to be compare with the next one
                    previousOMANodeId = currentOMANodeId;
                }
            }

        } catch (Exception e) {
            this.deleteTempFiles(generatedFileNames, tmpExtension);
            throw e;
        } finally {
            for (ICsvDozerBeanWriter writer : writersUsed.values()) {
                writer.close();
            }
        }

        // Now, if everything went fine, we rename the temporary files
        this.renameTempFiles(generatedFileNames, tmpExtension);

        log.exit();
    }

    /**
     * Retrieve from the data source a mapping from CIO IDs to names. 
     * 
     * @return                  A {@code Map} where keys are {@code String}s corresponding to 
     *                          CIO IDs, the associated values being {@code String}s 
     *                          corresponding to CIO names. 
     * @throws DAOException   If an error occurred while getting the data from the Bgee data source.
     */
    //XXX: to move to BgeeDBUtils? 
    //XXX: Implement something more generic for any EntityTOs?
    private Map<String, CIOStatementTO> getCIOStatementsByIds() throws DAOException {
        log.entry();
        
        log.debug("Start retrieving all CIO names");
        
        CIOStatementDAO dao = this.getCIOStatementDAO();
        dao.setAttributes(CIOStatementDAO.Attribute.ID, CIOStatementDAO.Attribute.NAME, 
                CIOStatementDAO.Attribute.TRUSTED);
        
        Map<String, CIOStatementTO> cioByIds = new HashMap<String, CIOStatementTO>();
        try (CIOStatementTOResultSet rs = dao.getAllCIOStatements()) {
            while (rs.next()) {
                CIOStatementTO cioTO = rs.getTO();
                cioByIds.put(cioTO.getId(), cioTO);
            }
        }
        
        log.debug("Done retrieving CIO names, {} names retrieved", cioByIds.size());
        
        return log.exit(cioByIds);
    }

    /**
     * Retrieve from the data source the last common ancestor of providen species. 
     *
     * @param speciesIds    A {@code Set} of {@code String}s that are the IDs of species
     *                      allowing to retrieve the LCA.
     * @return              the {@code String} that is the last common ancestor of providen species.
     * @throws DAOException If an error occurred while getting the data from the Bgee data source.
     */
    private String getLeastCommonAncestor(Set<String> speciesIds) throws DAOException {
        log.entry(speciesIds);
        
        log.debug("Start retrieving least common ancestor for the species IDs {}...", speciesIds);
    
        TaxonDAO dao = this.getTaxonDAO();
        dao.setAttributes(TaxonDAO.Attribute.ID);
    
        String lca = null;
        try (TaxonTOResultSet rs = dao.getLeastCommonAncestor(speciesIds, false)) {
            boolean isFirst = true;
            while (rs.next()) {
                if (isFirst) {
                    lca = rs.getTO().getId();
                    isFirst = false;
                } else {
                    throw log.throwing(new IllegalStateException("Severals LCA returns"));
                }
            }
        }
    
        log.debug("Done retrieving least common ancestor, the taxon found is {}", lca);
    
        return log.exit(lca);
    }

    /**
     * Retrieves mapping between OMA node IDs and gene IDs for the requested species,
     * present into the Bgee database.
     * 
     * @param taxonId       A {@code String} that is the ID of the common ancestor taxon
     *                      we want to into account.
     * @param speciesIds    A {@code Set} of {@code String}s that are the IDs of species
     *                      allowing to retrieve the mapping.
     * @return              A {@code Map} where keys are {@code String}s that are gene IDs, the  
     *                      associated values being {@code String}s corresponding to OMA group ID 
     *                      of the given taxon ID.
     * @throws DAOException If an error occurred while getting the data from the Bgee data source.
     * @throws IllegalStateException If we retrieve several LCA.
     */
    private Map<String,String> getMappingGeneIdOMANodeId(String taxonId, Set<String> speciesIds)
            throws DAOException, IllegalArgumentException {
        log.entry(taxonId, speciesIds);
    
        log.debug("Start retrieving homologous genes for the taxon ID {}...", taxonId);
    
        HierarchicalGroupDAO dao = this.getHierarchicalGroupDAO();
        
        Map<String,String> mapping = new HashMap<String,String>();
        try (HierarchicalGroupToGeneTOResultSet rs = dao.getGroupToGene(taxonId, speciesIds)) {
            boolean hasResult = false;
            while (rs.next()) {
                HierarchicalGroupToGeneTO to = rs.getTO();
                mapping.put(to.getGeneId(), to.getGroupId());
                hasResult = true;
            }
            if (!hasResult) {
                throw log.throwing(new IllegalStateException("No retrieved homologous genes"));
            }
        }
    
        log.debug("Done retrieving homologous genes, {} genes found", mapping.size());
    
        return log.exit(mapping);
    }

    /**
     * Retrieves comparable stages for the requested species in the requested taxon,
     * present into the Bgee database.
     *
     * @param taxonId       A {@code String} that is the ID of the common ancestor taxon 
     *                      we want to into account. 
     * @param speciesIds    A {@code Set} of {@code String}s that are the IDs of species
     *                      allowing to filter the comparable stages to use.
     * @return              the {@code List} of {@code Map}s. The first {@code Map} is the 
     *                      {@code Map} where keys are {@code String}s that are stage IDs, the 
     *                      associated values being {@code Set} of {@code String}s corresponding to 
     *                      stage group IDs. And the second {@code Map} is the {@code Map} where 
     *                      keys are {@code String}s that are stage group IDs, the associated values
     *                      being {@code Set} of {@code String}s corresponding to stage IDs.
     * @throws DAOException If an error occurred while getting the data from the Bgee data source.
     * @throws IllegalStateException IF an error is detected in data source.
     */
    //TODO: this is really an ugly design :p 
    //Implements a first method retrieving information from database, then two other methods 
    //to generate the proper mappings you need. 
    private List<Map<String,List<String>>> getComparableStages(
            String taxonId, Set<String> speciesIds) throws DAOException, IllegalStateException {
        log.entry(taxonId, speciesIds);
        
        log.debug("Start retrieving comparable stages for the taxon ID {} and the species IDs {}...", 
                taxonId, speciesIds);
        
       StageGroupingDAO dao = this.getStageGroupingDAO();
        //not attribute to set
        
       Map<String, List<String>> mappingStageIdToStageGroup = new HashMap<String, List<String>>();
       Map<String, List<String>> mappingStageGroupToStageId = new HashMap<String, List<String>>();
        try (GroupToStageTOResultSet rs = dao.getGroupToStage(taxonId, speciesIds)) {
            while (rs.next()) {
                GroupToStageTO to = rs.getTO();
    
                if (mappingStageIdToStageGroup.containsKey(to.getStageId())) {
                    throw log.throwing(new IllegalStateException(
                            "One stage ID souldn't be reported to severals stage groups"));
                }
                mappingStageIdToStageGroup.put(to.getStageId(), Arrays.asList(to.getGroupId()));
    
                List<String> stageIds = mappingStageGroupToStageId.get(to.getGroupId());
                if (stageIds == null) {
                    stageIds = new ArrayList<String>();
                    mappingStageGroupToStageId.put(to.getGroupId(), stageIds);
                }
                stageIds.add(to.getStageId());
            }
        }
    
        log.debug("Done retrieving relation from stage ID to " + 
                "stage group, {} found", mappingStageIdToStageGroup.size());
        log.debug("Done retrieving relation from stage group to " + 
                "stage IDs, {} found", mappingStageGroupToStageId.size());
    
        return log.exit(Arrays.asList(mappingStageIdToStageGroup, mappingStageGroupToStageId));
    }

    /**
     * Retrieves summary similarity annotations with the CIO ID for the requested taxon,
     * present into the Bgee database.
     *
     * @param taxonId       A {@code String} that is the ID of the common ancestor taxon 
     *                      we want to take into account. 
     * @return              A {@code Map} where keys are {@code String}s corresponding to summary 
     *                      similarity annotation IDs, the associated values being {@code String}s 
     *                      corresponding to CIO IDs.
     * @throws DAOException If an error occurred while getting the data from the Bgee data source.
     */
    private Map<String,String> getSummarySimilarityAnnotations(String taxonId) throws DAOException {
        log.entry(taxonId);
        
        log.debug("Start retrieving summary similarity annotations for the taxon ID {}...", taxonId);
    
        SummarySimilarityAnnotationDAO dao = this.getSummarySimilarityAnnotationDAO();
    
        Map<String,String> mapping = new HashMap<String,String>();
        try (SummarySimilarityAnnotationTOResultSet rs = dao.getSummarySimilarityAnnotations(taxonId)) {
            while (rs.next()) {
                SummarySimilarityAnnotationTO to = rs.getTO();
                mapping.put(to.getId(), to.getCIOId());
            }
        }
    
        log.debug("Done retrieving summary similarity annotations, {} found", mapping.size());
    
        return log.exit(mapping);
    }

    /**
     * Retrieves relation between summary similarity annotation and anatomical entity 
     * for the provided taxon ID and species IDs. 
     *
     * @param taxonId       A {@code String} that is the ID of the common ancestor taxon 
     *                      we want to into account. 
     * @param speciesIds    A {@code Set} of {@code String}s that are the IDs of species
     *                      allowing to filter the comparable stages to use.
     * @return              the {@code List} of {@code Map}s. The first {@code Map} is the 
     *                      {@code Map} where keys are {@code String}s that are summary similarity 
     *                      annotation IDs, the associated values being {@code Set} of 
     *                      {@code String}s corresponding to anat. entity IDs. And the second 
     *                      {@code Map} is the {@code Map} where keys are {@code String}s that are 
     *                      anat. entity IDs, the associated values being {@code Set} of 
     *                      {@code String}s corresponding to summary similarity annotation IDs.  
     * @throws DAOException If an error occurred while getting the data from the Bgee data source.
     */
    //TODO: same ugliness than for getComparableStages, to fix.
    private List<Map<String, List<String>>> getSimAnnotToAnatEntities(
            String taxonId, Set<String> speciesIds) throws DAOException {
        log.entry(taxonId, speciesIds);
    
        log.debug("Start retrieving relation between summary similarity annotation and " + 
                "anatomical entity for the taxon ID {} and species IDs {}...", 
                taxonId, speciesIds);
    
        SummarySimilarityAnnotationDAO dao = this.getSummarySimilarityAnnotationDAO();
        // setAttributes methods has no effect on attributes retrieved  
    
        Map<String,List<String>> mappingSimAnnotToAnatEntity = new HashMap<String,List<String>>();
        Map<String,List<String>> mappingAnatEntityToSimAnnot = new HashMap<String,List<String>>();
        //note that we retrieve all organs, even those not existing in all species
        try (SimAnnotToAnatEntityTOResultSet rs = dao.getSimAnnotToAnatEntity(taxonId, null)) {
            while (rs.next()) {
                SimAnnotToAnatEntityTO to = rs.getTO();
                
                List<String> sumAnatEntIds = mappingSimAnnotToAnatEntity.get(
                        to.getSummarySimilarityAnnotationId());
                if (sumAnatEntIds == null) {
                    sumAnatEntIds = new ArrayList<String>();
                    mappingSimAnnotToAnatEntity.put(to.getSummarySimilarityAnnotationId(), 
                            sumAnatEntIds);
                }
                sumAnatEntIds.add(to.getAnatEntityId());
                
                if (mappingAnatEntityToSimAnnot.containsKey(to.getAnatEntityId())) {
                    throw log.throwing(new IllegalStateException(
                    "An anatomical entity sould not be reported to severals similarity groups"));
                }
                mappingAnatEntityToSimAnnot.put(to.getAnatEntityId(), 
                        Arrays.asList(to.getSummarySimilarityAnnotationId()));
    
            }
        }
    
        log.debug("Done retrieving relation from summary similarity annotation to " + 
                "anatomical entities, {} found", mappingSimAnnotToAnatEntity.size());
        log.debug("Done retrieving relation from anatomical entity to " + 
                "summary similarity annotations, {} found", mappingAnatEntityToSimAnnot.size());
    
        return log.exit(Arrays.asList(mappingSimAnnotToAnatEntity, mappingAnatEntityToSimAnnot));
    }

    /**
     * Retrieves mapping between gene IDs and species IDs for the requested species,
     * present into the Bgee database.
     *
     * @param speciesIds    A {@code Set} of {@code String}s that are the IDs of species
     *                      allowing the mapping.
     * @return              the {@code Map} where keys are {@code String}s that are gene IDs, the  
     *                      associated values being {@code String}s corresponding to species ID.
     * @throws DAOException If an error occurred while getting the data from the Bgee data source.
     */
    private Map<String, String> getMappingGeneSpecies(Set<String> speciesIds) throws DAOException {
        log.entry(speciesIds);
        
        log.debug("Start retrieving gene-species mapping for the species IDs {}...", speciesIds);
        
        GeneDAO dao = this.getGeneDAO();
        dao.setAttributes(GeneDAO.Attribute.ID, GeneDAO.Attribute.SPECIES_ID);
        
        Map<String,String> mapping = new HashMap<String,String>();
        try (GeneTOResultSet rs = dao.getGenesBySpeciesIds(speciesIds)) {
            while (rs.next()) {
                GeneTO to = rs.getTO();
                mapping.put(to.getId(), String.valueOf(to.getSpeciesId()));
            }
        }
    
        log.debug("Done retrieving gene-species mapping, {} genes found", mapping.size());
    
        return log.exit(mapping);
    }

    /**
     * Retrieve differential expression calls for genes homologous in the provided taxon ID, 
     * order by groups of homologous genes.
     * 
     * @param taxonId       A {@code String} that is the ID of the common ancestor taxon
     *                      allowing to filter the calls to retrieve.
     * @param speciesIds    A {@code Set} of {@code String}s that are the IDs of species 
     *                      allowing to filter the calls to retrieve.
     * @param factor        A {@code ComparisonFactor}s that is the comparison factor 
     *                      allowing to filter the calls to retrieve.
     * @return              A {@code List} of {@code DiffExpressionCallTO}s that are 
     *                      all differential expression calls for the requested species, in
     *                      in requested taxon. 
     * @throws DAOException If an error occurred while getting the data from the Bgee data source.
     */
    private DiffExpressionCallTOResultSet getDiffExpressionCallsOrderByOMANodeId(
            String taxonId, Set<String> speciesIds, ComparisonFactor factor) throws DAOException {
        log.entry(taxonId, speciesIds, factor);
    
        log.debug("Start retrieving differential expression calls (factor {}) for the taxon ID {} and species IDs {}...", 
                factor, taxonId, speciesIds);
        
        DiffExpressionCallDAO dao = this.getDiffExpressionCallDAO();
        // do not retrieve the internal diff. expression IDs
        dao.setAttributes(EnumSet.complementOf(EnumSet.of(DiffExpressionCallDAO.Attribute.ID)));
    
        DiffExpressionCallParams params = new DiffExpressionCallParams();
        params.addAllSpeciesIds(speciesIds);
        params.setComparisonFactor(factor);
    
        DiffExpressionCallTOResultSet rs = 
                dao.getOrderedHomologousGenesDiffExpressionCalls(taxonId, params);
    
        log.debug("Done retrieving differential expression calls");
    
        return log.exit(rs);
    }

    /**
     * Return species names in the alphabetical order from the provided species IDs. 
     *
     * @param speciesIds        A {@code Set} of {@code String}s that are the IDs of species.
     * @param speciesNamesByIds A {@code Map} where keys are {@code String}s corresponding to 
     *                          species IDs, the associated values being {@code String}s 
     *                          corresponding to species names. 
     * @return                  the {@code List} of {@code String}s that are species names in the 
     *                          alphabetical order from the provided species IDs.
     */
    private List<String> getOrderedSpeciesName(
            Set<String> speciesIds, Map<String, String> speciesNamesByIds) {
        log.entry();
        
        List<String> names = new ArrayList<String>();
        for (String id : speciesIds) {
            names.add(speciesNamesByIds.get(id));
        }
        assert names.size() == speciesIds.size();
    
        Collections.sort(names);
        
        return log.exit(names);
    }

    /**
     * Groups provided differential expression calls by condition (summary similarity annotation 
     * and stage group).
     *
     * @param callTOs                   A {@code Set} of {@code DiffExpressionCallTO}s that are 
     *                                  the calls to be grouped.
     * @param mapAnatEntityToSimAnnot   A {@code Map} where keys are {@code String}s that are 
     *                                  anat. entity IDs, the associated values being {@code Set} of 
     *                                  {@code String}s corresponding to summary similarity 
     *                                  annotation IDs. 
     * @param mapStageIdToStageGroup    A {@code Map} where keys are {@code String}s that are stage 
     *                                  IDs, the associated values being {@code Set} of 
     *                                  {@code String}s corresponding to stage group IDs. 
     * @return                          the Map of where keys are {@code MultiSpeciesCondition}s 
     *                                  that are condition, the associated values being 
     *                                  {@code Collection} of {@code DiffExpressionCallTO}s 
     *                                  corresponding to grouped differential expression calls.
     */
    //XXX: maybe we should also provide the mapping gene ID -> OMA group ID, 
    //to check that all calls are from a same OMA group ID?
    private Map<MultiSpeciesCondition, Collection<DiffExpressionCallTO>>
            groupByMultiSpeciesCondition(Set<DiffExpressionCallTO> groupedCallTOs, 
                    Map<String, List<String>> mapAnatEntityToSimAnnot, 
                    Map<String, List<String>> mapStageIdToStageGroup) {
        log.entry(groupedCallTOs, mapAnatEntityToSimAnnot, mapStageIdToStageGroup);
        
        Map<MultiSpeciesCondition, Collection<DiffExpressionCallTO>> groupedCalls = 
                new HashMap<MultiSpeciesCondition, Collection<DiffExpressionCallTO>>();
        //for sanity check: currently, this class only supports having one call per gene 
        //per multi-species condition. This might change in the future.
        //this Map will store association from a multi-species condition to the genes considered
        Map<MultiSpeciesCondition, Set<String>> condToGeneIds = 
                new HashMap<MultiSpeciesCondition, Set<String>>();
        
        for (DiffExpressionCallTO diffExpressionCallTO : groupedCallTOs) {
            //each anat entity is supposed to be mapped to one and only one group.
            //same for stages.
            assert mapAnatEntityToSimAnnot.get(diffExpressionCallTO.getAnatEntityId()).size() <= 1;
            assert mapStageIdToStageGroup.get(diffExpressionCallTO.getStageId()).size() <= 1;
            String sumSimAnnotId = null;
            if (!mapAnatEntityToSimAnnot.get(diffExpressionCallTO.getAnatEntityId()).isEmpty()) {
                sumSimAnnotId = 
                        mapAnatEntityToSimAnnot.get(diffExpressionCallTO.getAnatEntityId()).get(0);
            }
            String stageGroupId = null;
            if (!mapStageIdToStageGroup.get(diffExpressionCallTO.getStageId()).isEmpty()) {
                stageGroupId = 
                        mapStageIdToStageGroup.get(diffExpressionCallTO.getStageId()).get(0);
            }
            
            //OK, both the anat entity and the stage have a multi-species mapping
            if (sumSimAnnotId != null && stageGroupId != null) {
                MultiSpeciesCondition condition = 
                        new MultiSpeciesCondition(sumSimAnnotId, stageGroupId);
                
                //sanity check, only one call per gene per condition. 
                //this might change in the future.
                Set<String> associatedGeneIds = condToGeneIds.get(condition);
                if (associatedGeneIds == null) {
                    associatedGeneIds = new HashSet<String>();
                    condToGeneIds.put(condition, associatedGeneIds);
                } else if (associatedGeneIds.contains(diffExpressionCallTO.getGeneId())) {
                    throw log.throwing(new IllegalStateException("Several calls were retrieved "
                            + "for a same gene in a same multi-species condition. "
                            + "Condition: " + condition 
                            + " - Gene: " + diffExpressionCallTO.getGeneId() 
                            + " - Iterated call: " + diffExpressionCallTO));
                }
                associatedGeneIds.add(diffExpressionCallTO.getGeneId());
                
                
                Collection<DiffExpressionCallTO> calls = groupedCalls.get(condition);
                if (calls == null) {
                    log.trace("Create new map key: {}", condition);
                    calls = new HashSet<DiffExpressionCallTO>();
                    groupedCalls.put(condition, calls);
                }
                calls.add(diffExpressionCallTO);
            }
        }
        return log.exit(groupedCalls);
    }

    //NOTE: we should filter and write in the same time because filter could be according to file type
    /**
     * Filter provided call group to be written and write them in a file. 
     *
     * @param geneNamesByIds            A {@code Map} where keys are {@code String}s corresponding  
     *                                  to gene IDs, the associated values being {@code String}s 
     *                                  corresponding to gene names. 
     * @param mapGeneSpecies            A {@code Map} where keys are {@code String}s that are gene   
     *                                  IDs, the associated values being {@code String}s 
     *                                  corresponding to species ID.
     * @param stageNamesByIds           A {@code Map} where keys are {@code String}s corresponding  
     *                                  to stage IDs, the associated values being {@code String}s 
     *                                  corresponding to stage names. 
     * @param anatEntityNamesByIds      A {@code Map} where keys are {@code String}s corresponding  
     *                                  to anatomical entity IDs, the associated values being 
     *                                  {@code String}s corresponding to anatomical entity names.
     * @param cioStatementByIds         A {@code Map} where keys are {@code String}s corresponding  
     *                                  to CIO IDs, the associated values being {@code String}s 
     *                                  corresponding to CIO names.
     * @param speciesNamesByIds         A {@code Map} where keys are {@code String}s corresponding 
     *                                  to species IDs, the associated values being {@code String}s 
     *                                  corresponding to species names. 
     * @param writersUsed               A {@code Map} where keys are {@code MultiSpDiffExprFileType}s 
     *                                  corresponding to which type of file should be generated, the 
     *                                  associated values being {@code ICsvDozerBeanWriter}s 
     *                                  corresponding to the writers.
     * @param processors                A {@code Map} where keys are {@code MultiSpDiffExprFileType}s 
     *                                  corresponding to which type of file should be generated, the 
     *                                  associated values being an {@code Array} of 
     *                                  {@code CellProcessor}s used to process a file.
     * @param omaNodeId                 A {@code String} that is the OMA node ID.
     * @param entry                     An {@code Entry} where keys are {@code MultiSpeciesCondition},
     *                                  corresponding to the condition, the associated values being
     *                                  a {@code Collection} of {@code DiffExpressionCallTO}s that 
     *                                  are the differential expression calls to be filtered and 
     *                                  written.
     * @param mapSumSimCIO              A {@code Map} where keys are {@code String}s corresponding 
     *                                  to summary similarity annotation IDs, the associated values 
     *                                  being {@code String}s corresponding to CIO IDs.
     * @param mapSimAnnotToAnatEntities A {@code Map} is the {@code Map} where keys are 
     *                                  {@code String}s that are summary similarity annotation IDs, 
     *                                  the associated values being {@code Set} of {@code String}s 
     *                                  corresponding to anat. entity IDs.
     * @param mapStageGroupToStageId    A {@code Map} is the {@code Map} where keys are 
     *                                  {@code String}s that are stage group IDs, the associated 
     *                                  values being {@code Set} of {@code String}s corresponding 
     *                                  to stage IDs.
     * @throws IllegalArgumentException If call data are inconsistent (for instance, without any data).
     * @throws IOException              If an error occurred while trying to write generated files.
     */
    //XXX: I'm not a fan of methods with so many arguments, maybe the mappings 
    //should be stored in class attributes. A good design would be to have *another* class 
    //to hold these params...
    //XXX: not a big fan of the Entry argument, this might be splitted into two distinct arguments
    //XXX: do we really need the mapSumSimCIO mapping? It is easy to retrieve it from cioStatementByIds...
    private void filterAndWriteConditionGroup(Map<String,String> geneNamesByIds,
            Map<String,String> mapGeneSpecies, Map<String,String> stageNamesByIds,
            Map<String,String> anatEntityNamesByIds, Map<String,CIOStatementTO> cioStatementByIds,
            Map<String,String> speciesNamesByIds,
            Map<MultiSpDiffExprFileType, ICsvDozerBeanWriter> writersUsed,
            Map<MultiSpDiffExprFileType, CellProcessor[]> processors,
            String omaNodeId, Entry<MultiSpeciesCondition, Collection<DiffExpressionCallTO>> entry,
            Map<String,String> mapSumSimCIO, Map<String, List<String>> mapSimAnnotToAnatEntities,
            Map<String, List<String>> mapStageGroupToStageId)
                    throws IllegalArgumentException, IOException {
        log.entry(geneNamesByIds, mapGeneSpecies, stageNamesByIds, anatEntityNamesByIds, 
                cioStatementByIds, speciesNamesByIds, writersUsed, processors, omaNodeId, entry, 
                mapSumSimCIO, mapSimAnnotToAnatEntities, mapStageGroupToStageId);
        
        // First, we get some data to be able to build beans 
        MultiSpeciesCondition condition = entry.getKey();

        String cioId = mapSumSimCIO.get(condition.getSummarySimilarityAnnotationId());
        
        List<String> stageIds = mapStageGroupToStageId.get(condition.getStageGroupId());
        assert stageIds != null && !stageIds.isEmpty();
        //sort IDs for consistent diff between releases
        Collections.sort(stageIds);
        List<String> stageNames = new ArrayList<String>();
        for (String stageId: stageIds) {
            stageNames.add(stageNamesByIds.get(stageId));
        }
        assert stageIds.size() == stageNames.size();
        
        List<String> organIds = 
                mapSimAnnotToAnatEntities.get(condition.getSummarySimilarityAnnotationId());
        assert organIds != null && !organIds.isEmpty();
        //sort IDs for consistent diff between releases
        Collections.sort(organIds);
        List<String> organNames = new ArrayList<String>();
        for (String entityId: organIds) {
            organNames.add(anatEntityNamesByIds.get(entityId));
        }
        assert organIds.size() == organNames.size();

        List<String> geneIds = new ArrayList<String>(), geneNames = new ArrayList<String>();
        
        // Then, we compute data for simple file for each species
        // and we create in same time complete multi-species diff. expression file beans 
        List<CompleteMultiSpeciesDiffExprFileBean> completeBeans = 
                new ArrayList<CompleteMultiSpeciesDiffExprFileBean>();
        int totalOver = 0, totalUnder = 0, totalNotDiffExpr = 0, totalNotExpr = 0;
        //to count number of species with data (we validate only conditions 
        //with a least two species); the data we accept are not the same for simple 
        //and complete files, so we use two Sets.
        Set<String> speciesIdsWithDataForSimple   = new HashSet<String>();
        Set<String> speciesIdsWithDataForComplete = new HashSet<String>();
        //TODO: add comment, what is this Map allSpeciesCounts?
        Map<String, SpeciesCounts> allSpeciesCounts = new HashMap<String, SpeciesCounts>();
        
        for (DiffExpressionCallTO to : entry.getValue()) {
            
            // We create a complete bean with null differential expression and call quality
            CompleteMultiSpeciesDiffExprFileBean currentBean = 
                    new CompleteMultiSpeciesDiffExprFileBean(
                            omaNodeId, this.getOmaNodeDescription(omaNodeId),
                            Arrays.asList(to.getGeneId()), 
                            Arrays.asList(geneNamesByIds.get(to.getGeneId())), 
                            organIds, organNames,
                            Arrays.asList(to.getStageId()), 
                            Arrays.asList(stageNamesByIds.get(to.getStageId())), 
                            mapGeneSpecies.get(to.getGeneId()), 
                            speciesNamesByIds.get(mapGeneSpecies.get(to.getGeneId())), 
                            cioId, cioStatementByIds.get(cioId).getName(), 
                            to.getDiffExprCallTypeAffymetrix().getStringRepresentation(),
                            to.getAffymetrixData().getStringRepresentation(), 
                            to.getBestPValueAffymetrix(), 
                            Double.valueOf(to.getConsistentDEACountAffymetrix()), 
                            Double.valueOf(to.getInconsistentDEACountAffymetrix()), 
                            to.getDiffExprCallTypeRNASeq().getStringRepresentation(), 
                            to.getRNASeqData().getStringRepresentation(), to.getBestPValueRNASeq(), 
                            Double.valueOf(to.getConsistentDEACountRNASeq()), 
                            Double.valueOf(to.getInconsistentDEACountRNASeq()),
                            null, null); // Differential expression and call quality

            // We add differential expression and call quality to the complete bean
            this.addDiffExprCallMergedDataToRow(currentBean);
            // And add it to the set of bean to be written
            completeBeans.add(currentBean);
            
            // We store gene data to be able to create simple bean later
            geneIds.add(to.getGeneId());
            geneNames.add(geneNamesByIds.get(to.getGeneId()));
            
            // We finish by count gene types
            SpeciesCounts currentCounts = allSpeciesCounts.get(currentBean.getSpeciesId());
            if (currentCounts == null) {
                currentCounts = new SpeciesCounts(currentBean.getSpeciesId(), 0, 0, 0, 0, 0);
                allSpeciesCounts.put(currentBean.getSpeciesId(), currentCounts);                
            }

            boolean hasDataForSimple = false;
            boolean hasDataForComplete = false;
            switch (DiffExpressionData.convertToDiffExpressionData(
                    currentBean.getDifferentialExpression())) {
                case NO_DATA:
                    currentCounts.setNbNAGenes(currentCounts.getNbNAGenes() + 1);
                    break;
                case NOT_EXPRESSED:
                    currentCounts.setNbNotExprGenes(currentCounts.getNbNotExprGenes() + 1);
                    totalNotExpr++;
                    break;
                case OVER_EXPRESSION:
                    currentCounts.setNbOverExprGenes(currentCounts.getNbOverExprGenes() + 1);
                    totalOver++;
                    hasDataForSimple   = true;
                    hasDataForComplete = true;
                    break;
                case UNDER_EXPRESSION:
                    currentCounts.setNbUnderExprGenes(currentCounts.getNbUnderExprGenes() + 1);
                    totalUnder++;
                    hasDataForSimple   = true;
                    hasDataForComplete = true;
                    break;
                case NOT_DIFF_EXPRESSION:
                case WEAK_AMBIGUITY:
                case STRONG_AMBIGUITY:
                    currentCounts.setNbNotDiffExprGenes(currentCounts.getNbNotDiffExprGenes() + 1);
                    totalNotDiffExpr++;
                    hasDataForComplete = true;
                    break;
                default:
                    throw log.throwing(new AssertionError(
                            "All logical conditions should have been checked."));
            } 

            String speciesId = mapGeneSpecies.get(to.getGeneId());
            if (hasDataForSimple) {
                speciesIdsWithDataForSimple.add(speciesId);
            }
            if (hasDataForComplete) {
                speciesIdsWithDataForComplete.add(speciesId);
            }
        }
        
        // We filter when there is no data in at least 2 species 
        if (speciesIdsWithDataForComplete.size() < 2) {
            log.trace("This OMA group doesn't have data in at least 2 species");
            return;
        }
        assert totalOver + totalUnder + totalNotDiffExpr >= 2;
        assert completeBeans != null && !completeBeans.isEmpty();
        
        // We filter poor quality homologous annotations in simple file (CIO), 
        // and we do not have the same criteria for counting species with data 
        // as for the complete file.
        SimpleMultiSpeciesDiffExprFileBean simpleBean = null; 
        if (cioStatementByIds.get(cioId).isTrusted() && speciesIdsWithDataForSimple.size() >= 2) {
            simpleBean = new SimpleMultiSpeciesDiffExprFileBean(omaNodeId, 
                            this.getOmaNodeDescription(omaNodeId), geneIds, geneNames, 
                            organIds, organNames, stageIds, stageNames, cioId, 
                            cioStatementByIds.get(cioId).getName(), null);

            // We order species IDs to keep the same order when we regenerate files.
            List<String> speciesIds = new ArrayList<String>(allSpeciesCounts.keySet());
            Collections.sort(speciesIds);
            for (String speciesId: speciesIds) {                
                SpeciesCounts speciesCounts = allSpeciesCounts.get(speciesId);
                simpleBean.getSpeciesCounts().add(speciesCounts);
            }
        }

        
        // Then we write beans
        for (Entry<MultiSpDiffExprFileType, ICsvDozerBeanWriter> writerFileType: writersUsed.entrySet()) {

            if (writerFileType.getKey().isSimpleFileType() && simpleBean != null) {
                writerFileType.getValue().write(simpleBean, processors.get(writerFileType.getKey()));
            } else {
                // We order calls according to OMA ID, entity IDs, stage IDs, species ID, gene IDs
                //TODO: delegates the sorting to another method, this will make the code easier to read
                Collections.sort(completeBeans, new Comparator<CompleteMultiSpeciesDiffExprFileBean>(){
                    @Override
                    public int compare(CompleteMultiSpeciesDiffExprFileBean bean1,
                            CompleteMultiSpeciesDiffExprFileBean bean2) {
                        log.entry();
                        
                        int omaIdComp = bean1.getOmaId().compareToIgnoreCase(bean2.getOmaId());
                        if (omaIdComp != 0)
                            return omaIdComp;

                        int uberonIdComp = compareTwoLists(bean1.getEntityIds(), bean2.getEntityIds());
                        if (uberonIdComp != 0)
                            return uberonIdComp;

                        int stageIdComp = compareTwoLists(bean1.getStageIds(), bean2.getStageIds());
                        if (stageIdComp != 0)
                            return stageIdComp;
                        
                        int speciesIdComp = bean1.getSpeciesId().compareToIgnoreCase(bean2.getSpeciesId());
                        if (speciesIdComp != 0)
                            return speciesIdComp;

                        int geneIdComp = compareTwoLists(bean1.getGeneIds(), bean2.getGeneIds());
                        if (geneIdComp != 0)
                            return geneIdComp;

                        return log.exit(0);
                    }
                });
                
                // We write gene IDs and names of the OMA group in a comment
                writerFileType.getValue().writeComment("//OMA node ID " + omaNodeId + 
                        " contains gene IDs " + geneIds +" with gene names " + geneNames);

                // We write rows
                for (CompleteMultiSpeciesDiffExprFileBean completeBean: completeBeans) {
                    writerFileType.getValue().write(completeBean, processors.get(writerFileType.getKey()));
                }
                
                // We finish by a comment to separate groups
                writerFileType.getValue().writeComment("//");
            }
        }

        log.debug("Done writing calls of OMA node ID", omaNodeId);

        log.exit();
    }
    
    /**
     * Add to the provided {@code CompleteMultiSpeciesDiffExprFileBean} merged 
     * {@code DataState}s and qualities.
     * <p>
     * The provided {@code CompleteMultiSpeciesDiffExprFileBean} will be modified.
     *
     * @param bean  A {@code CompleteMultiSpeciesDiffExprFileBean} that is the bean to be modified.
     * @throws IllegalArgumentException If call data are inconsistent (for instance, without any data).
     */
    //TODO: DRY
    private void addDiffExprCallMergedDataToRow(CompleteMultiSpeciesDiffExprFileBean bean) 
                    throws IllegalArgumentException {
        log.entry(bean);
        
        DiffExpressionData summary = DiffExpressionData.NO_DATA;
        String quality = GenerateDiffExprFile.NA_VALUE;
    
        DiffExprCallType affymetrixType = 
                DiffExprCallType.convertToDiffExprCallType(bean.getAffymetrixData()); 
        DiffExprCallType rnaSeqType = 
                DiffExprCallType.convertToDiffExprCallType(bean.getRNASeqData()); 
        
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
            quality = GenerateDiffExprFile.NA_VALUE;
    
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
                allDataQuality.add(DataState.convertToDataState(bean.getAffymetrixQuality()));
            }
            if (!rnaSeqType.equals(DiffExprCallType.NO_DATA)) {
                allDataQuality.add(DataState.convertToDataState(bean.getRNASeqQuality()));
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
                    break;
                default:
                    throw log.throwing(new AssertionError(
                            "Both DiffExprCallType are set to 'no data' or 'not expressed'"));
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
        //XXX: actually, I think that there are no NOT_EXPRESSED case inserted, 
        //but it doesn't hurt to keep this code
        } else if ((allType.contains(DiffExprCallType.NOT_DIFF_EXPRESSED) && 
                        (allType.contains(DiffExprCallType.OVER_EXPRESSED) ||
                        allType.contains(DiffExprCallType.UNDER_EXPRESSED))) || 
                   (allType.contains(DiffExprCallType.NOT_EXPRESSED) && 
                        (allType.contains(DiffExprCallType.OVER_EXPRESSED)) || 
                        allType.contains(DiffExprCallType.NOT_DIFF_EXPRESSED))) {
            summary = DiffExpressionData.WEAK_AMBIGUITY;
            quality = GenerateDiffExprFile.NA_VALUE;
    
        // One call containing NOT_EXPRESSED and UNDER_EXPRESSED returns 
        // UNDER_EXPRESSION with LOWQUALITY 
        //XXX: actually, I think that there are no NOT_EXPRESSED case inserted, 
        //but it doesn't hurt to keep this code
        } else if (allType.contains(DiffExprCallType.NOT_EXPRESSED) && 
                allType.contains(DiffExprCallType.UNDER_EXPRESSED)) {
            summary = DiffExpressionData.UNDER_EXPRESSION;
            quality = DataState.LOWQUALITY.getStringRepresentation();
            
        } else {
            throw log.throwing(new AssertionError("All logical conditions should have been checked."));
        }
        assert !summary.equals(DiffExpressionData.NO_DATA);
    
        // Add diff. expression and quality to the bean
        bean.setDifferentialExpression(summary.getStringRepresentation());
        bean.setCallQuality(quality);
    
        log.exit();
    }

    /**
     * Compare two {@code List}s of {@code String}s, elements by elements. When two elements (with 
     * the same index), are different, the comparison returns an integer whose sign is that of 
     * calling {@link java.lang.String.compareToIgnoreCase(String)}.
     *
     * @param list1 A {@code List} to be compared to {@code list2}.
     * @param list2 A {@code List} to be compared to {@code list1}.
     * @return      A negative integer, zero, or a positive integer as {@code list1} is greater than, 
     *              equal to, or less than this {@code list2}, ignoring case considerations.
     */
    //TODO find a better place ?
    //TODO: transform this method into a Comparator
    private int compareTwoLists(List<String> list1, List<String> list2) {
        log.entry(list1, list2);
        int minLength = Math.min(list1.size(), list2.size());
        Collections.sort(list1);
        Collections.sort(list2);

        for (int i = 0; i < minLength; i++) {
            final int compareValue = list1.get(i).compareToIgnoreCase(list2.get(i));
            if (compareValue != 0) {
                return compareValue; // They are already not equal
            }
        }
        if (list1.size() == list2.size()) {
            return 0; // They are equal
        } else if (list1.size() < list2.size()) {
            return -1; // list 1 is smaller
        } else {
            return 1;
        }
    }
    
    /**
     * Retrieve the OMA description according to the provided OMA node ID.
     *
     *@param omaNodeId  A {@code String} that is the ID of the OMA node to be used to retrieve 
     *                  its description.
     * @return          the {@code String} that is the description of the provided OMA node. 
     */
    //TODO: when we will have generated descriptions for OMA nodes, we will need to change this logic.
    private String getOmaNodeDescription(String omaNodeId) {
        log.entry(omaNodeId);
        // TODO Auto-generated method stub
        return log.exit(null);
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
        log.entry(fileType, nbSpecies);

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
            processors[0] = new StrNotNullOrEmpty();    // OMA id
            processors[1] = new StrNotNullOrEmpty(new Collector(new ArrayList<Object>())); // gene ID list
            processors[2] = new NotNull(new Collector(new ArrayList<Object>()));           // gene name list
            processors[3] = new StrNotNullOrEmpty(new Collector(new ArrayList<Object>())); // anatomical entity ID list
            processors[4] = new StrNotNullOrEmpty(new Collector(new ArrayList<Object>())); // anatomical entity name list
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
                new StrNotNullOrEmpty(),            // OMA id
                new StrNotNullOrEmpty(new Collector(new ArrayList<Object>())), // anatomical entity ID list
                new StrNotNullOrEmpty(new Collector(new ArrayList<Object>())), // anatomical entity name list
                new StrNotNullOrEmpty(),            // developmental stage ID
                new StrNotNullOrEmpty(),            // developmental stage name
                new StrNotNullOrEmpty(),            // species latin name
                new StrNotNullOrEmpty(),            // gene ID
                new NotNull(),                      // gene name
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
     * @param fileType      The {@code MultiSpDiffExprFileType} of the file to be generated.
     * @param speciesNames  A {@code List} of {@code String}s that are the names of species 
     *                      we want to generate data for.
     * @return              An {@code Array} of {@code String}s used to produce the header.
     * @throw IllegalArgumentException If {@code fileType} is not managed by this method.
     */
    private String[] generateHeader(MultiSpDiffExprFileType fileType, List<String> speciesNames)
        throws IllegalArgumentException {
        log.entry(fileType, speciesNames);

        if (fileType.isSimpleFileType()) {
            int nbColumns = 7 + 5 * speciesNames.size();
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
                int columnIndex = 7 + 5 * i;
                String endHeader = " for " + speciesNames.get(i);
                headers[columnIndex] = NB_OVER_EXPR_GENES_COLUMN_NAME + endHeader;
                headers[columnIndex+1] = NB_UNDER_EXPR_GENES_COLUMN_NAME + endHeader;
                headers[columnIndex+2] = NB_NO_DIFF_EXPR_GENES_COLUMN_NAME + endHeader;
                headers[columnIndex+2] = NB_NOT_EXPR_GENES_COLUMN_NAME + endHeader;
                headers[columnIndex+3] = NB_NA_GENES_COLUMN_NAME + endHeader;
            }
            return log.exit(headers);
        }

        return log.exit(new String[] { 
                OMA_ID_COLUMN_NAME, 
                ANAT_ENTITY_ID_LIST_ID_COLUMN_NAME, ANAT_ENTITY_NAME_LIST_ID_COLUMN_NAME,
                STAGE_ID_COLUMN_NAME, STAGE_NAME_COLUMN_NAME, 
                SPECIES_LATIN_NAME_COLUMN_NAME,                
                GENE_ID_COLUMN_NAME, GENE_NAME_COLUMN_NAME,
                CIO_ID_ID_COLUMN_NAME, CIO_NAME_ID_COLUMN_NAME, 
                DIFFEXPRESSION_COLUMN_NAME, QUALITY_COLUMN_NAME,
                AFFYMETRIX_DATA_COLUMN_NAME, AFFYMETRIX_CALL_QUALITY_COLUMN_NAME,
                AFFYMETRIX_P_VALUE_COLUMN_NAME, AFFYMETRIX_CONSISTENT_DEA_COUNT_COLUMN_NAME, 
                AFFYMETRIX_INCONSISTENT_DEA_COUNT_COLUMN_NAME,
                RNASEQ_DATA_COLUMN_NAME, RNASEQ_CALL_QUALITY_COLUMN_NAME,
                RNASEQ_P_VALUE_COLUMN_NAME, RNASEQ_CONSISTENT_DEA_COUNT_COLUMN_NAME, 
                RNASEQ_INCONSISTENT_DEA_COUNT_COLUMN_NAME});
    }
    
    private String[] generateFieldMapping(MultiSpDiffExprFileType fileType, int nbSpecies) {
        log.entry(fileType, nbSpecies);

        if (fileType.isSimpleFileType()) {
            int nbColumns = 7 + 5 * nbSpecies;
            String[] fieldMapping = new String[nbColumns];
            fieldMapping[0] = OMA_ID_COLUMN_NAME;
            fieldMapping[1] = GENE_ID_LIST_ID_COLUMN_NAME;
            fieldMapping[2] = GENE_NAME_LIST_ID_COLUMN_NAME;
            fieldMapping[3] = ANAT_ENTITY_ID_LIST_ID_COLUMN_NAME;
            fieldMapping[4] = ANAT_ENTITY_NAME_LIST_ID_COLUMN_NAME;
            fieldMapping[5] = STAGE_ID_COLUMN_NAME;
            fieldMapping[6] = STAGE_NAME_COLUMN_NAME;
            // the number of columns depends on the number of species
            for (int i = 0; i < nbSpecies; i++) {
                int columnIndex = 7 + 5 * i;
                fieldMapping[columnIndex] = "speciesCounts[i].nbOverExprGenes";
                fieldMapping[columnIndex+1] = "speciesCounts[i].nbUnderExprGenes";
                fieldMapping[columnIndex+2] = "speciesCounts[i].nbNotDiffExprGenes";
                fieldMapping[columnIndex+2] = "speciesCounts[i].nbNotExprGenes";
                fieldMapping[columnIndex+3] = "speciesCounts[i].nbNAGenes";
            }
            return log.exit(fieldMapping);
        }

        return log.exit(new String[] { 
                OMA_ID_COLUMN_NAME, 
                ANAT_ENTITY_ID_LIST_ID_COLUMN_NAME, ANAT_ENTITY_NAME_LIST_ID_COLUMN_NAME,
                STAGE_ID_COLUMN_NAME, STAGE_NAME_COLUMN_NAME, 
                SPECIES_LATIN_NAME_COLUMN_NAME,
                GENE_ID_COLUMN_NAME, GENE_NAME_COLUMN_NAME,
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
