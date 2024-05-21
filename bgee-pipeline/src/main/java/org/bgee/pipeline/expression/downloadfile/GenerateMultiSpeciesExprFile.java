package org.bgee.pipeline.expression.downloadfile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SimAnnotToAnatEntityTO;
import org.bgee.model.dao.api.expressiondata.call.CallDAO.CallTO;
import org.bgee.model.dao.api.expressiondata.call.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.expressiondata.call.Call.ExpressionCall;
import org.bgee.model.expressiondata.call.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.call.multispecies.MultiSpeciesCall;
import org.bgee.model.file.SpeciesDownloadFile.Category;
import org.bgee.pipeline.BgeeDBUtils;
import org.bgee.pipeline.CommandRunner;
import org.bgee.pipeline.Utils;
import org.bgee.pipeline.expression.downloadfile.GenerateDownloadFile.ObservedData;
import org.bgee.pipeline.expression.downloadfile.GenerateExprFile2.SingleSpExprFileType2;
import org.bgee.pipeline.expression.downloadfile.GenerateExprFile2.SingleSpeciesCompleteExprFileBean;
import org.bgee.pipeline.expression.downloadfile.GenerateExprFile2.SingleSpeciesSimpleExprFileBean;
import org.bgee.pipeline.expression.downloadfile.GenerateMultiSpeciesDiffExprFile.MultiSpeciesSimpleDiffExprFileBean;
import org.bgee.pipeline.expression.downloadfile.GenerateMultiSpeciesDiffExprFile.SpeciesDiffExprCounts;
import org.bgee.pipeline.expression.downloadfile.GenerateMultiSpeciesDownloadFile.MultiSpeciesCompleteFileBean;
import org.bgee.pipeline.expression.downloadfile.GenerateMultiSpeciesDownloadFile.MultiSpeciesFileBean;
import org.supercsv.cellprocessor.constraint.IsElementOf;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.io.dozer.CsvDozerBeanWriter;
import org.supercsv.io.dozer.ICsvDozerBeanWriter;


/**
 * Class used to generate multi-species expression download files (simple and advanced files) 
 * from the Bgee database.
 *
 * @author 	Valentine Rech de Laval
 * @version Bgee 14, Mar. 2017
 * @since 	Bgee 13
 */
public class GenerateMultiSpeciesExprFile   extends GenerateDownloadFile 
                                            implements GenerateMultiSpeciesDownloadFile {

    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(
            GenerateMultiSpeciesExprFile.class.getName());
    
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
     * A {@code String} that is the name of the column containing expression/no-expression
     * found with relaxed <em>in situ</em> experiment, in the download file.
     */
    public final static String RELAXED_INSITU_DATA_COLUMN_NAME = "Relaxed in situ data";

    /**
     * A {@code String} that is the name of the column containing call quality found with
     * <em>in situ</em> experiment, in the download file.
     */
    public final static String RELAXED_INSITU_CALL_QUALITY_COLUMN_NAME = 
            "Relaxed in situ call quality";

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

//    /**
//     * Main method to trigger the generate multi-species expression TSV download files
//     * (simple and advanced files) from Bgee database. Parameters that must be provided
//     * in order in {@code args} are:
//     * <ol>
//     * <li>a list of NCBI species IDs (for instance, {@code 9606} for human) that will be used to
//     * generate download files, separated by the {@code String} {@link CommandRunner#LIST_SEPARATOR}.
//     * If an empty list is provided (see {@link CommandRunner#EMPTY_LIST}), TODO to be decided.
//     * <li>a taxon ID (for instance, {@code 40674} for Mammalia) that will be used t
//     * generate download files. If an empty list is provided (see {@link CommandRunner#EMPTY_LIST}),
//     * TODO To be decided.
//     * <li>a list of files types that will be generated ('multi-expr-simple' for
//     * {@link MultiSpExprFileType MULTI_EXPR_SIMPLE}, and 'multi-expr-complete' for
//     * {@link MultiSpExprFileType MULTI_EXPR_COMPLETE}), separated by the {@code String}
//     * {@link CommandRunner#LIST_SEPARATOR}. If an empty list is provided
//     * (see {@link CommandRunner#EMPTY_LIST}), all possible file types will be generated.
//     * <li>the directory path that will be used to generate download files.
//     * <li>the prefix that will be used to generate multi-species file names. If {@code null} or
//     * empty, TODO to be decided.
//     * </ol>
//     * 
//     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
//     * @throws IllegalArgumentException If incorrect parameters were provided.
//     * @throws IOException              If an error occurred while trying to write generated files.
//     */
//    public static void main(String[] args) throws IllegalArgumentException, IOException {
//        log.entry((Object[]) args);
//    
//        int expectedArgLength = 5;
//        if (args.length != expectedArgLength) {
//            throw log.throwing(new IllegalArgumentException(
//                    "Incorrect number of arguments provided, expected " +
//                    expectedArgLength + " arguments, " + args.length + " provided."));
//        }
//    
//        GenerateMultiSpeciesExprFile generator = new GenerateMultiSpeciesExprFile(
//            CommandRunner.parseListArgumentAsInt(args[0]),
//            args[0],
//            GenerateDownloadFile.convertToFileTypes(
//                CommandRunner.parseListArgument(args[1]), MultiSpExprFileType.class),
//            args[2],
//            args[3]);
//        generator.generateMultiSpeciesExprFiles();
//    
//        log.traceExit();
//    }

    /**
     * A bean representing a row of a simple multi-species expression file. 
     * Getter and setter names must follow standard bean definitions.
     * 
     * @author  Valentine Rech de Laval
     * @version Bgee 14, Mar. 2017
     * @since   Bgee 14, Mar. 2017
     */
    public static class MultiSpeciesSimpleExprFileBean extends MultiSpeciesFileBean {
        
        /**
         * See {@link #getGeneIds()}
         */
        private List<String> geneIds;
        /**
         * See {@link #getGeneNames()}
         */
        private List<String> geneNames;
        /**
         * See {@link #getSpWithExprCount()}
         */
        private Long spWithExprCount;
        /**
         * See {@link #getSpWithNoExprCount()}
         */
        private Long spWithNoExprCount;
        /**
         * See {@link #getSpWithoutExpr()}
         */
        private Long spWithoutExpr;
        /**
         * See {@link #getConservationScore()}
         */
        private Long conservationScore;
        
        /**
         * 0-argument constructor of the bean.
         */
        public MultiSpeciesSimpleExprFileBean() {
        }
    
        /**
         * Constructor providing all arguments of the class.
         *
         * @param omaId                 See {@link #getOmaId()}.
         * @param geneIds               See {@link #getGeneIds()}.
         * @param geneNames             See {@link #getGeneNames()}.
         * @param entityIds             See {@link #getEntityIds()}.
         * @param entityNames           See {@link #getEntityNames()}.
         * @param stageIds              See {@link #getStageIds()}.
         * @param stageNames            See {@link #getStageNames()}.
         * @param spWithExprCount       See {@link #getSpWithExprCount()}.
         * @param spWithNoExprCount     See {@link #getSpWithNoExprCount()}.
         * @param spWithoutExpr         See {@link #getSpWithoutExpr()}.
         * @param conservationScore     See {@link #getConservationScore()}.
         */
        public MultiSpeciesSimpleExprFileBean(String omaId, List<String> geneIds, 
                List<String> geneNames, List<String> entityIds, List<String> entityNames, 
                List<String> stageIds, List<String> stageNames, Long spWithExprCount,
                Long spWithNoExprCount, Long spWithoutExpr, Long conservationScore) {
            super(omaId, entityIds, entityNames, stageIds, stageNames);
            this.geneIds = geneIds;
            this.geneNames = geneNames;
            this.spWithExprCount = spWithExprCount;
            this.spWithNoExprCount = spWithNoExprCount;
            this.spWithoutExpr = spWithoutExpr;
            this.conservationScore = conservationScore;
        }
    
        /**
         * @return  the {@code List} of {@code String}s that are the IDs of the genes.
         *          When there is several genes, they are provided in alphabetical order.
         */
        public List<String> getGeneIds() {
            return geneIds;
        }
        /** 
         * @param geneIds   A {@code List} of {@code String}s that are the IDs of the genes.
         * @see #getGeneIds()
         */
        public void setGeneIds(List<String> geneIds) {
            this.geneIds = geneIds;
        }

        /**
         * @return  the {@code List} of {@code String}s that are the names of the genes.
         *          When there is several genes, they are provided in same order as their 
         *          corresponding ID, as returned by {@link #getGeneIds()}.
         */
        public List<String> getGeneNames() {
            return geneNames;
        }
        /**
         * @param geneNames A {@code List} of {@code String}s that are the names of genes.
         * @see #getGeneNames()
         */
        public void setGeneNames(List<String> geneNames) {
            this.geneNames = geneNames;
        }

        /**
         * @return  the {@code Long} that is the number of species with expression.
         */
        public Long getSpWithExprCount() {
            return spWithExprCount;
        }
        /**
         * @param spWithExprCount   A {@code Long} that is the number of species with expression.
         * @see #getSpWithExprCount()
         */
        public void setSpWithExprCount(Long spWithExprCount) {
            this.spWithExprCount = spWithExprCount;
        }

        /**
         * @return  the {@code Long} that is the number of species with no-expression.
         */
        public Long getSpWithNoExprCount() {
            return spWithNoExprCount;
        }
        /**
         * @param spWithNoExprCount   A {@code Long} that is the number of species with no-expression.
         * @see #getSpWithNoExprCount()
         */
        public void setSpWithNoExprCount(Long spWithNoExprCount) {
            this.spWithNoExprCount = spWithNoExprCount;
        }

        /**
         * @return  the {@code Long} that is the number of species without expression.
         */
        public Long getSpWithoutExpr() {
            return spWithoutExpr;
        }
        /**
         * @param spWithoutExpr   A {@code Long} that is the number of species without expression.
         * @see #getSpWithoutExpr()
         */
        public void setSpWithoutExpr(Long spWithoutExpr) {
            this.spWithoutExpr = spWithoutExpr;
        }

        /**
         * @return  the {@code Long} that is the conservation score.
         */
        public Long getConservationScore() {
            return conservationScore;
        }
        /**
         * @param conservationScore   A {@code Long} that is the conservation score.
         * @see #getConservationScore()
         */
        public void setConservationScore(Long conservationScore) {
            this.conservationScore = conservationScore;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((geneIds == null) ? 0 : geneIds.hashCode());
            result = prime * result + ((geneNames == null) ? 0 : geneNames.hashCode());
            result = prime * result + ((spWithExprCount == null) ? 0 : spWithExprCount.hashCode());
            result = prime * result + ((spWithNoExprCount == null) ? 0 : spWithNoExprCount.hashCode());
            result = prime * result + ((spWithoutExpr == null) ? 0 : spWithoutExpr.hashCode());
            result = prime * result + ((conservationScore == null) ? 0 : conservationScore.hashCode());
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
            MultiSpeciesSimpleExprFileBean other = (MultiSpeciesSimpleExprFileBean) obj;
            if (geneIds == null) {
                if (other.geneIds != null)
                    return false;
            } else if (!geneIds.equals(other.geneIds))
                return false;
            if (geneNames == null) {
                if (other.geneNames != null)
                    return false;
            } else if (!geneNames.equals(other.geneNames))
                return false;
            if (spWithExprCount == null) {
                if (other.spWithExprCount != null)
                    return false;
            } else if (!spWithExprCount.equals(other.spWithExprCount))
                return false;
            if (spWithNoExprCount == null) {
                if (other.spWithNoExprCount != null)
                    return false;
            } else if (!spWithNoExprCount.equals(other.spWithNoExprCount))
                return false;
            if (spWithoutExpr == null) {
                if (other.spWithoutExpr != null)
                    return false;
            } else if (!spWithoutExpr.equals(other.spWithoutExpr))
                return false;
            if (conservationScore == null) {
                if (other.conservationScore != null)
                    return false;
            } else if (!conservationScore.equals(other.conservationScore))
                return false;
            return true;
        }
        
        
        @Override
        public String toString() {
            
            StringBuilder builder = new StringBuilder();
            builder.append("MultiSpeciesSimpleExprFileBean [geneIds=").append(geneIds)
                .append(", geneNames=").append(geneNames).append(", spWithExprCount=").append(spWithExprCount)
                .append(", spWithNoExprCount=").append(spWithNoExprCount)
                .append(", spWithoutExpr=").append(spWithoutExpr)
                .append(", conservationScore=").append(conservationScore).append("]");
            return builder.toString();
        }
    }
    
    /**
     * A bean representing a row of a complete multi-species expression file. 
     * Getter and setter names must follow standard bean definitions.
     * 
     * @author  Valentine Rech de Laval
     * @version Bgee 14, Mar. 2017
     * @since   Bgee 14, Mar. 2017
     */
    public static class MultiSpeciesCompleteExprFileBean extends MultiSpeciesCompleteFileBean {
//        Expression  1   present
//        Call quality    1   high quality
//        Including observed data 1   yes
//        Affymetrix data 1   expressed
//        Affymetrix counts?      
//        Including Affymetrix observed data  1   yes
//        Affymetrix data 1   present
//        Affymetrix counts?      
//        Including Affymetrix observed data  1   yes
//        EST data    1   present
//        EST counts?     
//        Including EST observed data 1   yes
//        In situ data    1   present
//        In situ counts?     
//        Including in situ observed data 1   yes
//        RNA-Seq data    1   no data
//        RNA-Seq counts?     NA
//        Including RNA-Seq observed data 1   no
    }
    
    /**
     * An {@code Enum} used to define, for each data type (Affymetrix, RNA-Seq, ...),
     * as well as for the summary column, the data state of the call.
     * <ul>
     * <li>{@code NO_DATA}:         no data from the associated data type allowed to produce the call.
     * <li>{@code EXPRESSION}:      expression was detected from the associated data type.
     * <li>{@code NO_EXPRESSION}:   no-expression was detected from the associated data type.
     * </ul>
     * 
     * @author  Valentine Rech de Laval
     * @version Bgee 14, Mar. 2017
     * @since   Bgee 13
     */
    public enum ExpressionData {
        NO_DATA("no data"), NO_EXPRESSION("absent"), EXPRESSION("expression");

        private final String stringRepresentation;

        /**
         * Constructor providing the {@code String} representation of this {@code ExpressionData}.
         * 
         * @param stringRepresentation A {@code String} corresponding to this {@code ExpressionData}.
         */
        private ExpressionData(String stringRepresentation) {
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
     * An {@code Enum} used to define the possible multi-species expression file types 
     * to be generated.
     * <ul>
     * <li>{@code MULTI_EXPR_SIMPLE}:   presence/absence of expression in multi-species 
     *                                  in a simple download file.
     * <li>{@code MULTI_EXPR_COMPLETE}: presence/absence of expression in multi-species
     *                                  in an advanced download file.
     * </ul>
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public enum MultiSpExprFileType implements FileType {
        MULTI_EXPR_SIMPLE(Category.EXPR_CALLS_SIMPLE, true),
        MULTI_EXPR_COMPLETE(Category.EXPR_CALLS_COMPLETE, false);
    
        /**
         * A {@code Category} that is the category of files of this type.
         */
        private final Category category;
        
        /**
         * A {@code boolean} defining whether this {@code MultiSpeciesExprFileType} is a simple 
         * file type.
         */
        private final boolean simpleFileType;
    
        /**
         * Constructor providing the {@code Category} of this {@code MultiSpeciesExprFileType},
         * and a {@code boolean} defining whether this {@code MultiSpeciesExprFileType}
         * is a simple file type.
         */
        private MultiSpExprFileType(Category category, boolean simpleFileType) {
            this.category = category;
            this.simpleFileType = simpleFileType;
        }
    
        @Override
        public String getStringRepresentation() {
            return this.category.getStringRepresentation();
        }
        @Override
        public boolean isSimpleFileType() {
            return this.simpleFileType;
        }
        @Override
        public String toString() {
            return this.getStringRepresentation();
        }
        @Override
        public Category getCategory() {
            return this.category;
        }
    }

    /**
     * An {@code Integer} that is the IDs of the common ancestor taxon we want to into account.
     * Cannot be {@code null} or empty.
     */
    final private Integer taxonId;

    /**
     * A {@code String} that is the prefix that will be used to generate multi-species file names.
     * Cannot be {@code null} or empty.
     */
    final private String groupPrefix;

    /**
     * Default constructor. 
     */
    //suppress warning as this default constructor should not be used.
    @SuppressWarnings("unused")
    private GenerateMultiSpeciesExprFile() {
        this(null, null, null, null, null);
    }

    /**
     * A {@code Supplier} of {@code ServiceFactory}s to be able to provide one to each thread.
     */
    private final Supplier<ServiceFactory> serviceFactorySupplier;

    /**
     * Constructor providing parameters to generate files, using the default {@code DAOManager}.
     * 
     * @param speciesIds    A {@code List} of {@code Integer}s that are the IDs of species 
     *                      we want to generate data for. If {@code null} or empty, all species 
     *                      are used.
     * @param taxonId       An {@code Integer} that is the ID of the common ancestor taxon
     *                      we want to into account. If {@code null} or empty, TODO to be decided
     * @param fileTypes     A {@code Set} of {@code MultiSpeciesExprFileType}s that are the types
     *                      of files we want to generate. If {@code null} or empty, 
     *                      all {@code MultiSpeciesExprFileType}s are generated.
     * @param directory     A {@code String} that is the directory where to store files.
     * @param groupPrefix   A {@code String} that is the prefix of the group we want to use 
     *                      for files names. If {@code null} or empty, TODO  to be decided.
     * @throws IllegalArgumentException If {@code directory} is {@code null} or blank.
     */
    public GenerateMultiSpeciesExprFile(List<Integer> speciesIds, Integer taxonId, 
            Set<MultiSpExprFileType> fileTypes, String directory, String groupPrefix) 
                    throws IllegalArgumentException {
        this(null, speciesIds, taxonId, fileTypes, directory, groupPrefix, ServiceFactory::new);
    }

    /**
     * Constructor providing parameters to generate files, and the {@code MySQLDAOManager} that will  
     * be used by this object to perform queries to the database. This is useful for unit testing.
     * 
     * @param manager       the {@code MySQLDAOManager} to use.
     * @param speciesIds    A {@code List} of {@code Integer}s that are the IDs of species 
     *                      we want to generate data for. If {@code null} or empty, all species 
     *                      are used.
     * @param taxonId       An {@code Integer} that is the ID of the common ancestor taxon
     *                      we want to into account. Cannot be {@code null} or empty.
     * @param fileTypes     A {@code Set} of {@code MultiSpeciesExprFileType}s that are the types
     *                      of files we want to generate. If {@code null} or empty, 
     *                      all {@code MultiSpeciesExprFileType}s are generated.
     * @param directory     A {@code String} that is the directory where to store files.
     * @param groupPrefix   A {@code String} that is the prefix the group we want to use 
     *                      for files names. If {@code null} or empty, TODO  to be decided.
     * @param serviceFactorySupplier    A {@code Supplier} of {@code ServiceFactory}s 
     *                                  to be able to provide one to each thread.
     * @throws IllegalArgumentException If {@code directory} is {@code null} or blank.
     */
    public GenerateMultiSpeciesExprFile(MySQLDAOManager manager, List<Integer> speciesIds, 
            Integer taxonId, Set<MultiSpExprFileType> fileTypes, String directory, 
            String groupPrefix, Supplier<ServiceFactory> serviceFactorySupplier) throws IllegalArgumentException {
        super(manager, speciesIds, fileTypes, directory);
        this.taxonId = taxonId;
        this.groupPrefix = groupPrefix;
        this.serviceFactorySupplier = serviceFactorySupplier;
    }
    
    /**
     * Generate multi-species expression files, for the types defined by {@code fileTypes}, 
     * for species defined by {@code speciesIds} with ancestral taxon defined by {@code taxonId},
     * in the directory {@code directory}.
     * 
     * @throws IllegalArgumentException If no species ID or taxon ID is provided.
     * @throws IOException              If an error occurred while trying to write generated files.
     * 
     */
    public void generateMultiSpeciesExprFiles() throws IOException {
        log.entry(this.speciesIds, this.taxonId, this.fileTypes, this.directory, this.groupPrefix);

        Set<Integer> setSpecies = new HashSet<>();
        if (this.speciesIds != null && !this.speciesIds.isEmpty()) {
            setSpecies = new HashSet<>(this.speciesIds);
        } else {
            throw log.throwing(new IllegalArgumentException("No species ID is provided"));
        }
        
        if (this.taxonId == null) {
            throw log.throwing(new IllegalArgumentException("No taxon ID is provided"));
        }

        // If no file types are given by user, we set all file types
        if (this.fileTypes == null || this.fileTypes.isEmpty()) {
            this.fileTypes = EnumSet.allOf(MultiSpExprFileType.class);
        }

        // Retrieve species names, gene names, stage names, anat. entity names, for all species
        // XXX: retrieve only for speciesIds? 
        Map<Integer, String> speciesNamesByIds = Utils.checkAndGetLatinNamesBySpeciesIds(setSpecies,
                serviceFactorySupplier.get().getSpeciesService());
        Map<Integer, String> geneNamesByIds = 
                BgeeDBUtils.getGeneNamesByIds(setSpecies, this.getGeneDAO());
        Map<String, String> stageNamesByIds = 
                BgeeDBUtils.getStageNamesByIds(setSpecies, this.getStageDAO());
        Map<String, String> anatEntityNamesByIds = 
                BgeeDBUtils.getAnatEntityNamesByIds(setSpecies, this.getAnatEntityDAO());
        Map<String, String> cioNamesByIds = null;
//                BgeeDBUtils.getCIOStatementNamesByIds(this.getCIOStatementDAO(), true); //TODO

        // Generate multi-species expression files
        log.info("Start generating of multi-species expression files for the group {} with " +
                "the species {} and the ancestral taxon ID {}...", 
                this.groupPrefix, speciesNamesByIds.values(), this.taxonId);

        try {
            this.generateMultiSpeciesExprFiles(speciesNamesByIds, 
                    geneNamesByIds, stageNamesByIds, anatEntityNamesByIds, cioNamesByIds);
        } finally {
            // close connection to database
            this.getManager().releaseResources();
        }
        
        log.info("Done generating of multi-species expression files for the group {}.", 
                this.groupPrefix);

        log.traceExit();
    }

    /**
     * TODO Javadoc
     * @throws IOException 
     *
     */
    private void generateMultiSpeciesExprFiles(Map<Integer, String> speciesNamesByIds, 
            Map<Integer, String> geneNamesByIds, Map<String, String> stageNamesByIds, 
            Map<String, String> anatEntityNamesByIds,  Map<String, String> cioNamesByIds) 
                    throws IOException {
        log.entry(this.directory, this.groupPrefix, this.fileTypes, this.taxonId, speciesNamesByIds,
                geneNamesByIds, stageNamesByIds, anatEntityNamesByIds, cioNamesByIds);

        log.debug("Start generating multi-species expression files for the group {} with the taxon {} and file types {}...", 
                this.groupPrefix, this.taxonId, this.fileTypes);

        //********************************
        // RETRIEVE DATA FROM DATA SOURCE
        //********************************
        final Set<Integer> speciesFilter =  new HashSet<>(this.speciesIds);

        log.trace("Start retrieving data...");
        
        ServiceFactory serviceFactory = this.serviceFactorySupplier.get();

        // Load expression and no-expression calls
        // FIXME use new signature
        Stream<MultiSpeciesCall<ExpressionCall>> calls = null;
//                serviceFactory.getAnalysisService().loadMultiSpeciesExpressionCalls(null, speciesFilter);
        
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
        Map<MultiSpExprFileType, ICsvDozerBeanWriter> writersUsed = new HashMap<>();
        int numberOfRows = 0;
        try {
            //**************************
            // OPEN FILES, CREATE WRITERS, WRITE HEADERS
            //**************************
            Map<MultiSpExprFileType, CellProcessor[]> processors = new HashMap<>();
            Map<MultiSpExprFileType, String[]> headers = new HashMap<>();

            // Get ordered species names
//            List<String> orderedSpeciesNames = this.getSpeciesNameAsList(
//                    this.speciesIds, speciesNamesByIds);
            
            for (FileType fileType : this.fileTypes) {
                MultiSpExprFileType currentFileType = (MultiSpExprFileType) fileType;

                CellProcessor[] fileTypeProcessors = null;
                String[] fileTypeHeaders = null;

                fileTypeProcessors = this.generateCellProcessors(currentFileType);
                processors.put(currentFileType, fileTypeProcessors);
                
                fileTypeHeaders = this.generateHeader(currentFileType);
                headers.put(currentFileType, fileTypeHeaders);

                // Create file name
                String fileName = this.groupPrefix + "_" +
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
                        Utils.getCsvPreferenceWithQuote(this.generateQuoteMode(fileTypeHeaders)));
                // configure the mapping from the fields to the CSV columns
                if (currentFileType.isSimpleFileType()) {
                    beanWriter.configureBeanMapping(MultiSpeciesSimpleExprFileBean.class, 
                            this.generateFieldMapping(currentFileType, fileTypeHeaders));
                } else {
                    beanWriter.configureBeanMapping(MultiSpeciesCompleteExprFileBean.class, 
                            this.generateFieldMapping(currentFileType, fileTypeHeaders));
                }

                beanWriter.writeHeader(fileTypeHeaders);
                writersUsed.put(currentFileType, beanWriter);

            }

            // ****************************
            // WRITE ROWS
            // ****************************
            numberOfRows = this.writeRows(geneNamesByIds, stageNamesByIds, anatEntityNamesByIds,
                    writersUsed, processors, headers, calls);
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

        log.traceExit();
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
     * @param geneNamesByIds        A {@code Map} where keys are {@code Integer}s corresponding to 
     *                              gene IDs, the associated values being {@code String}s 
     *                              corresponding to gene names. 
     * @param stageNamesByIds       A {@code Map} where keys are {@code String}s corresponding to 
     *                              stage IDs, the associated values being {@code String}s 
     *                              corresponding to stage names. 
     * @param anatEntityNamesByIds  A {@code Map} where keys are {@code String}s corresponding to 
     *                              anatomical entity IDs, the associated values being 
     *                              {@code String}s corresponding to anatomical entity names. 
     * @param writersUsed           A {@code Map} where keys are {@code MultiSpExprFileType}s
     *                              corresponding to which type of file should be generated, the 
     *                              associated values being {@code ICsvDozerBeanWriter}s
     *                              corresponding to the writers.
     * @param processors            A {@code Map} where keys are {@code MultiSpExprFileType}s 
     *                              corresponding to which type of file should be generated, the 
     *                              associated values being an {@code Array} of 
     *                              {@code CellProcessor}s used to process a file.
     * @param headers               A {@code Map} where keys are {@code MultiSpExprFileType}s 
     *                              corresponding to which type of file should be generated, the 
     *                              associated values being an {@code Array} of {@code String}s used 
     *                              to produce the header.
     * @param calls                 A {@code Stream} of {@code ExpressionCall}s that are propagated
     *                              and reconciled expression calls.
     * @throws UncheckedIOException If an error occurred while trying to write the {@code outputFile}.
     */
    private int writeRows(Map<Integer, String> geneNamesByIds, 
            Map<String, String> stageNamesByIds, Map<String, String> anatEntityNamesByIds, 
            Map<MultiSpExprFileType, ICsvDozerBeanWriter> writersUsed, 
            Map<MultiSpExprFileType, CellProcessor[]> processors, 
            Map<MultiSpExprFileType, String[]> headers,
            Stream<MultiSpeciesCall<ExpressionCall>> calls) throws UncheckedIOException {
        log.entry(geneNamesByIds, stageNamesByIds, anatEntityNamesByIds, writersUsed, 
                processors, headers, calls);

        return log.traceExit(calls.map(c -> {
            int i = 0;
            for (Entry<MultiSpExprFileType, ICsvDozerBeanWriter> writerFileType : writersUsed.entrySet()) {
//                String geneName = geneNamesByIds.containsKey(geneId)? geneNamesByIds.get(geneId) : "";
//                String anatEntityId = c.getCondition().getAnatEntityId();
//                String anatEntityName = anatEntityNamesByIds.get(anatEntityId);
//                String devStageId = c.getCondition().getDevStageId();
//                String devStageName = stageNamesByIds.get(c.getCondition().getDevStageId());
//                String summaryCallType = convertExpressionSummaryToString(c.getSummaryCallType()); 
//                String summaryQuality = convertSummaryQualityToString(c.getSummaryQuality());
//                String expressionRank = c.getFormattedGlobalMeanRank();
//                String expressionScore = null; // FIXME use c.getGlobalScore();
//                Boolean includingObservedData = c.getIsObservedData();
//
//                if (writerFileType.getKey().isSimpleFileType() && Boolean.TRUE.equals(includingObservedData)) {
//                    SingleSpeciesSimpleExprFileBean bean = new SingleSpeciesSimpleExprFileBean(
//                        geneId, geneName, anatEntityId, anatEntityName, devStageId, devStageName,
//                        summaryCallType, summaryQuality, expressionRank, expressionScore);
//                    try {
//                        writerFileType.getValue().write(bean, processors.get(writerFileType.getKey()));
//                        i++;
//                    } catch (IOException e) {
//                        throw new UncheckedIOException(e);
//                    }
//                } else if (!writerFileType.getKey().isSimpleFileType()) {
//                    String affymetrixData = NO_DATA_VALUE, estData = NO_DATA_VALUE, 
//                            inSituData = NO_DATA_VALUE, rnaSeqData = NO_DATA_VALUE,
//                            includingAffymetrixObservedData = ObservedData.NOT_OBSERVED.getStringRepresentation(),
//                            includingEstObservedData = ObservedData.NOT_OBSERVED.getStringRepresentation(),
//                            includingInSituObservedData =  ObservedData.NOT_OBSERVED.getStringRepresentation(),
//                            includingRnaSeqObservedData = ObservedData.NOT_OBSERVED.getStringRepresentation();
//                    Set<ExpressionCallData> callData = c.getCallData();
//
//                    Set<ExpressionCallData> affyCallData = callData.stream()
//                            .filter(d -> DataType.AFFYMETRIX.equals(d.getDataType())).collect(Collectors.toSet());
//                    if (affyCallData.size() > 0) {
//                        affymetrixData = resumeCallType(affyCallData);
//                        includingAffymetrixObservedData = resumeIncludingObservedData(affyCallData);
//                    }
//                    
//                    Set<ExpressionCallData> estCallData = callData.stream()
//                            .filter(d -> DataType.EST.equals(d.getDataType())).collect(Collectors.toSet());
//                    if (estCallData.size() > 0) {
//                        estData = resumeCallType(estCallData);
//                        includingEstObservedData = resumeIncludingObservedData(estCallData);
//                    }
//                    
//                    Set<ExpressionCallData> inSituCallData = callData.stream()
//                            .filter(d -> DataType.IN_SITU.equals(d.getDataType())).collect(Collectors.toSet());
//                    if (inSituCallData.size() > 0) {
//                        inSituData = resumeCallType(inSituCallData);
//                        includingInSituObservedData = resumeIncludingObservedData(inSituCallData);
//                    }
//                    
//                    Set<ExpressionCallData> rnaSeqCallData = callData.stream()
//                            .filter(d -> DataType.RNA_SEQ.equals(d.getDataType())).collect(Collectors.toSet());
//                    if (rnaSeqCallData.size() > 0) {
//                        rnaSeqData = resumeCallType(rnaSeqCallData);
//                        includingRnaSeqObservedData = resumeIncludingObservedData(rnaSeqCallData);
//                    }
//
//                    SingleSpeciesCompleteExprFileBean bean = new SingleSpeciesCompleteExprFileBean(
//                            geneId, geneName, anatEntityId, anatEntityName, devStageId, devStageName,
//                            summaryCallType, summaryQuality, expressionRank, expressionScore,
//                            convertObservedDataToString(c.getIsObservedData()),
//                            affymetrixData, includingAffymetrixObservedData,
//                            estData, includingEstObservedData,
//                            inSituData, includingInSituObservedData,
//                            rnaSeqData, includingRnaSeqObservedData);
//                    try {
//                        writerFileType.getValue().write(bean, processors.get(writerFileType.getKey()) );
//                        i++;
//                    } catch (IOException e) {
//                        throw new UncheckedIOException(e);
//                    }
//                }
            }
            return i;
        }).mapToInt(Integer::intValue).sum());
    }

    /**
     * Generates an {@code Array} of {@code String}s used to generate the header of a multi-species
     * expression TSV file of type {@code fileType}.
     * 
     * @param fileType  The {@code MultiSpeciesExprFileType} of the file to be generated.
     * @param nbSpecies A {@code List} of {@code String}s that are the names of species 
     *                  we want to generate data for.
     * @return          An {@code Array} of {@code String}s used to produce the header.
     * @throw IllegalArgumentException If {@code fileType} is not managed by this method.
     */
    private String[] generateHeader(MultiSpExprFileType fileType)
        throws IllegalArgumentException {
        log.entry(fileType);
    
        if (fileType.isSimpleFileType()) {
            return log.traceExit(new String[] {
                    OMA_ID_COLUMN_NAME, GENE_ID_LIST_COLUMN_NAME ,GENE_NAME_LIST_COLUMN_NAME,
                    ANAT_ENTITY_ID_LIST_COLUMN_NAME, ANAT_ENTITY_NAME_LIST_COLUMN_NAME,
                    STAGE_ID_COLUMN_NAME, STAGE_NAME_COLUMN_NAME, SPECIES_WITH_EXPRESSION_COUNT_COLUMN_NAME,
                    SPECIES_WITH_NO_EXPRESSION_COUNT_COLUMN_NAME, SPECIES_WITHOUT_CALLS_COUNT_COLUMN_NAME,
                    CONSERVATION_SCORE_COLUMN_NAME});
        }
    
        return log.traceExit(new String[] { 
                OMA_ID_COLUMN_NAME, GENE_ID_COLUMN_NAME, GENE_NAME_COLUMN_NAME,
                ANAT_ENTITY_ID_LIST_COLUMN_NAME, ANAT_ENTITY_NAME_LIST_COLUMN_NAME,
                STAGE_ID_COLUMN_NAME, STAGE_NAME_COLUMN_NAME, SPECIES_LATIN_NAME_COLUMN_NAME,
                CIO_ID_COLUMN_NAME, CIO_NAME_ID_COLUMN_NAME, EXPRESSION_COLUMN_NAME,
                QUALITY_COLUMN_NAME, INCLUDING_OBSERVED_DATA_COLUMN_NAME,
                AFFYMETRIX_DATA_COLUMN_NAME, AFFYMETRIX_QUAL_COLUMN_NAME,
                EST_DATA_COLUMN_NAME, EST_CALL_QUALITY_COLUMN_NAME,
                INSITU_DATA_COLUMN_NAME, INSITU_CALL_QUALITY_COLUMN_NAME,
                RNASEQ_DATA_COLUMN_NAME, RNASEQ_QUAL_COLUMN_NAME});
    }

    /**
     * Generates an {@code Array} of {@code CellProcessor}s used to process a multi-species 
     * expression TSV file of type {@code fileType}.
     * 
     * @param fileType  A {@code MultiSpExprFileType} that is the type of the file to be generated.
     * @return          An {@code Array} of {@code CellProcessor}s used to process 
     *                  a multi-species expression file.
     * @throw IllegalArgumentException If {@code fileType} is not managed by this method.
     */
    private CellProcessor[] generateCellProcessors(MultiSpExprFileType fileType) 
            throws IllegalArgumentException {
        log.entry(fileType);

        //First, we define all set of possible values
        List<Object> expressionValues = new ArrayList<Object>();
        for (ExpressionData data : ExpressionData.values()) {
            expressionValues.add(data.getStringRepresentation());
        }

        List<Object> specificTypeQualities = new ArrayList<Object>();
        specificTypeQualities.add(GenerateDownloadFile.convertDataStateToString(DataState.HIGHQUALITY));
        specificTypeQualities.add(GenerateDownloadFile.convertDataStateToString(DataState.LOWQUALITY));
        specificTypeQualities.add(GenerateDownloadFile.NA_VALUE);

        List<Object> resumeQualities = new ArrayList<Object>();
        resumeQualities.add(GenerateDownloadFile.convertDataStateToString(DataState.HIGHQUALITY));
        resumeQualities.add(GenerateDownloadFile.convertDataStateToString(DataState.LOWQUALITY));
        resumeQualities.add(GenerateDownloadFile.NA_VALUE);

        List<Object> originValues = new ArrayList<Object>();
        for (ObservedData data : ObservedData.values()) {
            originValues.add(data.getStringRepresentation());
        }
        
        //Second, we build the CellProcessor
        if (fileType.isSimpleFileType()) {
            return log.traceExit(new CellProcessor[] {
                    new StrNotNullOrEmpty(),    // OMA ID
                    new StrNotNullOrEmpty(),    // gene ID list
                    new NotNull(),              // gene name list
                    new StrNotNullOrEmpty(),    // anatomical entity ID list
                    new StrNotNullOrEmpty(),    // anatomical entity name list
                    new StrNotNullOrEmpty(),    // developmental stage ID
                    new StrNotNullOrEmpty(),    // developmental stage name
                    new StrNotNullOrEmpty(),    // species with expressed genes count
                    new StrNotNullOrEmpty(),    // species with not expressed genes count
                    new StrNotNullOrEmpty(),    // species without expressed genes count
                    new StrNotNullOrEmpty()});  // conservation score
        }
        return log.traceExit(new CellProcessor[] {
                new StrNotNullOrEmpty(),    // OMA ID
                new StrNotNullOrEmpty(),    // gene ID
                new NotNull(),              // gene name
                new StrNotNullOrEmpty(),    // anatomical entity ID list
                new StrNotNullOrEmpty(),    // anatomical entity name list
                new StrNotNullOrEmpty(),    // developmental stage ID
                new StrNotNullOrEmpty(),    // developmental stage name
                new StrNotNullOrEmpty(),    // species latin name
                new StrNotNullOrEmpty(),    // CIO ID
                new StrNotNullOrEmpty(),    // CIO name
                new IsElementOf(expressionValues),      // Expression
                new IsElementOf(resumeQualities),       // Call quality
                new IsElementOf(originValues),          // Including observed data
                new IsElementOf(expressionValues),      // Affymetrix data
                new IsElementOf(expressionValues),      // EST data
                new IsElementOf(expressionValues),      // In Situ data
                new IsElementOf(expressionValues)});    // RNA-seq data
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
            if (headers[i].equals(OMA_ID_COLUMN_NAME) ||
                    headers[i].equals(GENE_ID_LIST_COLUMN_NAME) ||
                    headers[i].equals(ANAT_ENTITY_ID_LIST_COLUMN_NAME) ||
                    headers[i].equals(ANAT_ENTITY_NAME_LIST_COLUMN_NAME) ||
                    headers[i].equals(STAGE_ID_COLUMN_NAME) ||
                    headers[i].equals(SPECIES_WITH_EXPRESSION_COUNT_COLUMN_NAME) ||
                    headers[i].equals(SPECIES_WITH_NO_EXPRESSION_COUNT_COLUMN_NAME) ||
                    headers[i].equals(SPECIES_WITHOUT_CALLS_COUNT_COLUMN_NAME) ||
                    headers[i].equals(CONSERVATION_SCORE_COLUMN_NAME) ||
                    headers[i].equals(GENE_ID_COLUMN_NAME) ||
                    headers[i].equals(SPECIES_LATIN_NAME_COLUMN_NAME) ||
                    headers[i].equals(CIO_ID_COLUMN_NAME) ||
                    headers[i].equals(CIO_NAME_ID_COLUMN_NAME) ||
                    headers[i].equals(EXPRESSION_COLUMN_NAME) ||
                    headers[i].equals(QUALITY_COLUMN_NAME) ||
                    headers[i].equals(INCLUDING_OBSERVED_DATA_COLUMN_NAME) ||
                    headers[i].equals(AFFYMETRIX_DATA_COLUMN_NAME) ||
                    headers[i].equals(AFFYMETRIX_QUAL_COLUMN_NAME) ||
                    headers[i].equals(EST_DATA_COLUMN_NAME) ||
                    headers[i].equals(EST_CALL_QUALITY_COLUMN_NAME) ||
                    headers[i].equals(INSITU_DATA_COLUMN_NAME) ||
                    headers[i].equals(INSITU_CALL_QUALITY_COLUMN_NAME) ||
                    headers[i].equals(RNASEQ_DATA_COLUMN_NAME) ||
                    headers[i].equals(RNASEQ_QUAL_COLUMN_NAME)) {
                quoteMode[i] = false; 
            } else if (headers[i].equals(GENE_NAME_COLUMN_NAME) ||
                    headers[i].equals(GENE_NAME_LIST_COLUMN_NAME) ||
                    headers[i].equals(ANAT_ENTITY_NAME_COLUMN_NAME) ||
                    headers[i].equals(STAGE_NAME_COLUMN_NAME)) {
                quoteMode[i] = true; 
            } else {
                    throw log.throwing(new IllegalArgumentException(
                            "Unrecognized header: " + headers[i] + " for OMA TSV file."));
            }
        }
        return log.traceExit(quoteMode);
    }

    /**
     * Generate the field mapping for each column of the header of a multi-species
     * expression TSV file of type {@code fileType}.
     * 
     * @param fileType  A {@code MultiSpExprFileType} defining the type of file 
     *                  that will be written.
     * @param header    An {@code Array} of {@code String}s representing the names 
     *                  of the columns of a multi-species expression file.
     * @return          The {@code Array} of {@code String}s that is the field mapping, 
     *                  put in the {@code Array} at the same index as the column they 
     *                  are supposed to process.
     * @throws IllegalArgumentException If a {@code String} in {@code header} is not recognized.
     */
    private String[] generateFieldMapping(MultiSpExprFileType fileType, String[] header) {
        log.entry(fileType, header);
        
        String[] mapping = new String[header.length];
        for (int i = 0; i < header.length; i++) {
            switch (header[i]) {
                // *** attributes common to all file types ***
                case OMA_ID_COLUMN_NAME: 
                    mapping[i] = "omaId";
                    break;
                case ANAT_ENTITY_ID_LIST_COLUMN_NAME: 
                    mapping[i] = "entityIds";
                    break;
                case ANAT_ENTITY_NAME_LIST_COLUMN_NAME: 
                    mapping[i] = "entityNames";
                    break;
                case STAGE_ID_COLUMN_NAME: 
                    mapping[i] = "stageIds";
                    break;
                case STAGE_NAME_COLUMN_NAME: 
                    mapping[i] = "stageNames";
                    break;
            }
            
            //if it was one of the column common to all beans, 
            //iterate next column name
            if (mapping[i] != null) {
                continue;
            }

            if (fileType.isSimpleFileType()) {
                // *** Attributes specific to simple file ***
                switch (header[i]) {
                    case GENE_ID_LIST_COLUMN_NAME: 
                        mapping[i] = "geneIds";
                        break;
                    case GENE_NAME_LIST_COLUMN_NAME: 
                        mapping[i] = "geneNames";
                        break;
                    case SPECIES_WITH_EXPRESSION_COUNT_COLUMN_NAME: 
                        mapping[i] = "spWithExprCount";
                        break;
                    case SPECIES_WITH_NO_EXPRESSION_COUNT_COLUMN_NAME: 
                        mapping[i] = "spWithNoExprCount";
                        break;
                    case SPECIES_WITHOUT_CALLS_COUNT_COLUMN_NAME: 
                        mapping[i] = "spWithoutExpr";
                        break;
                    case CONSERVATION_SCORE_COLUMN_NAME: 
                        mapping[i] = "conservationScore";
                        break;
                }
            } else {
                // *** Attributes specific to complete file ***
                if (header[i].equals(INCLUDING_OBSERVED_DATA_COLUMN_NAME)) {
                    mapping[i] = "includingObservedData";
                } else if (header[i].equals(AFFYMETRIX_DATA_COLUMN_NAME)) {
                    mapping[i] = "affymetrixData";
                } else if (header[i].equals(AFFYMETRIX_QUAL_COLUMN_NAME)) {
                    mapping[i] = "affymetrixCallQuality";
                } else if (header[i].equals(AFFYMETRIX_OBSERVED_DATA_COLUMN_NAME)) { 
                    mapping[i] = "includingAffymetrixObservedData";
                } else if (header[i].equals(EST_DATA_COLUMN_NAME)) {
                    mapping[i] = "estData";
                } else if (header[i].equals(EST_CALL_QUALITY_COLUMN_NAME)) {
                    mapping[i] = "estCallQuality";
                } else if (header[i].equals(EST_OBSERVED_DATA_COLUMN_NAME)) {
                    mapping[i] = "includingEstObservedData";
                } else if (header[i].equals(INSITU_DATA_COLUMN_NAME)) {
                    mapping[i] = "inSituData";
                } else if (header[i].equals(INSITU_CALL_QUALITY_COLUMN_NAME)) {
                    mapping[i] = "inSituCallQuality";
                } else if (header[i].equals(IN_SITU_OBSERVED_DATA_COLUMN_NAME)) {
                    mapping[i] = "includingInSituObservedData";
                } else if (header[i].equals(RNASEQ_DATA_COLUMN_NAME)) {
                    mapping[i] = "rnaSeqData";
                } else if (header[i].equals(RNASEQ_QUAL_COLUMN_NAME)) {
                    mapping[i] = "rnaSeqCallQuality";
                } else if (header[i].equals(RNASEQ_OBSERVED_DATA_COLUMN_NAME)) {
                    mapping[i] = "includingRnaSeqObservedData";
                }
            }
            if (mapping[i] == null) {
                throw log.throwing(new IllegalArgumentException("Unrecognized header: " 
                        + header[i] + " for file type: " + fileType.getStringRepresentation()));
            }
        }
        return log.traceExit(mapping);
    }
}
