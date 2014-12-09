package org.bgee.pipeline.expression;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTOResultSet;
import org.bgee.model.dao.api.expressiondata.ExpressionCallParams;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTOResultSet;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallParams;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.pipeline.BgeeDBUtils;
import org.bgee.pipeline.CommandRunner;
import org.bgee.pipeline.Utils;
import org.supercsv.cellprocessor.constraint.IsElementOf;
import org.supercsv.cellprocessor.constraint.NotNull;
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
public class GenerateDownladFile extends CallUser {
    
    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(GenerateDownladFile.class.getName());
        
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
    public final static String AFFYMETRIXDATA_COLUMN_NAME = "Affymetrix data";

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
    public final static String RNASEQDATA_COLUMN_NAME = "RNA-Seq data";

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
    public final static String DIFFEXPRESSION_COLUMN_NAME = "Over/Under-expression";
   
   /**
    * An {@code Enum} used to define the possible file types as class arguments.
    * <ul>
    * <li>{@code EXPR_SIMPLE}:       to generate presence/absence of expression simple download file.
    * <li>{@code EXPR_COMPLETE}:     to generate presence/absence of expression advanced download file.
    * <li>{@code DIFFEXPR_SIMPLE}:   to generate differential expression simple download file.
    * <li>{@code DIFFEXPR_COMPLETE}: to generate differential expression advanced download file.
    * </ul>
    * 
    * @author Valentine Rech de Laval
    * @version Bgee 13
    * @since Bgee 13
    */
   public enum FileType {
       EXPR_SIMPLE("expr-simple"), EXPR_COMPLETE("expr-complete"), 
       DIFFEXPR_SIMPLE("diffexpr-simple"), DIFFEXPR_COMPLETE("diffexpr-complete");
       
       private final String stringRepresentation;

       /**
        * Constructor providing the {@code String} representation of this {@code ExpressionData}.
        * 
        * @param stringRepresentation  A {@code String} corresponding to
        *                              this {@code ExpressionData}.
        */
       private FileType(String stringRepresentation) {
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
    * A {@code String} that is the extension of download files to be generated.
    */
   public final static String EXTENSION = ".tsv";
   
   /**
    * An {@code Enum} used to define, for each data type (Affymetrix, RNA-Seq, ...), the 
    * expression/no-expression of the call.
    * <ul>
    * <li>{@code NODATA}:         no data from the associated data type allowed to produce the call.
    * <li>{@code NOEXPRESSION}:   no-expression was detected from the associated data type.
    * <li>{@code LOWEXPRESSION}:  low-quality expression was detected from the associated data type.
    * <li>{@code HIGHEXPRESSION}: high-quality expression was detected from the associated data type.
    * <li>{@code LOWAMBIGUITY}:   different data types are not coherent with a no-expression call 
    *                             inferred (for instance, Affymetrix data reveals an expression 
    *                             while <em>in situ</em> data reveals an inferred no-expression).
    * <li>{@code HIGHAMBIGUITY}:  different data types are not coherent without at least an inferred
    *                             no-expression call (for instance, Affymetrix data reveals  
    *                             expression while <em>in situ</em> data reveals a no-expression 
    *                             without been inferred).
    * </ul>
    * 
    * @author Valentine Rech de Laval
    * @version Bgee 13
    * @since Bgee 13
    */
   public enum ExpressionData {
       NODATA("no data"), NOEXPRESSION("absent high quality"), 
       LOWQUALITY("expression low quality"), HIGHQUALITY("expression high quality"), 
       LOWAMBIGUITY("low ambiguity"), HIGHAMBIGUITY("high ambiguity");

       private final String stringRepresentation;

       /**
        * Constructor providing the {@code String} representation of this {@code ExpressionData}.
        * 
        * @param stringRepresentation  A {@code String} corresponding to
        *                              this {@code ExpressionData}.
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
    * An {@code Enum} used to define, for each data type (Affymetrix, RNA-Seq, ...), the 
    * differential expression of the call.
    * <ul>
    * <li>{@code NODATA}:           no data from the associated data type allowed to produce the call.
    * <li>{@code OVEREXPRESSED}:    over-expression was detected from the associated data type.
    * <li>{@code UNDEREXPRESSED}:   under-expression was detected from the associated data type.
    * <li>{@code NOTDIFFEXPRESSED}: not differential expression was detected from the associated 
    *                               data type.
    * </ul>
    * 
    * @author Valentine Rech de Laval
    * @version Bgee 13
    * @since Bgee 13
    */
   //TODO: needs to take into account quality levels, as for expression calls
   public enum DiffExpressionData {
       NODATA("no data"), OVEREXPRESSED("over-expression"), UNDEREXPRESSED("under-expression"), 
       NOTDIFFEXPRESSED("no diff expression");
       
       private final String stringRepresentation;
       
       /**
        * Constructor providing the {@code String} representation of this {@code DiffCallType}.
        * 
        * @param stringRepresentation  A {@code String} corresponding to this {@code DiffCallType}.
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
    * An {@code Enum} used to define if the call has been observed.
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
       OBSERVED("yes"), NOTOBSERVED("no");
       
       private final String stringRepresentation;
       
       /**
        * Constructor providing the {@code String} representation of this {@code InferredWords}.
        * 
        * @param stringRepresentation  A {@code String} corresponding to this {@code InferredWords}.
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
     * Default constructor. 
     */
    public GenerateDownladFile() {
        this(null);
    }

    /**
     * Constructor providing the {@code MySQLDAOManager} that will be used by 
     * this object to perform queries to the database. This is useful for unit testing.
     * 
     * @param manager   the {@code MySQLDAOManager} to use.
     */
    public GenerateDownladFile(MySQLDAOManager manager) {
        super(manager);
    }

    /**
     * Main method to trigger the generate TSV download files (simple and complete files) from Bgee 
     * database. Parameters that must be provided in order in {@code args} are: 
     * <ol>
     * <li> a list of NCBI species IDs (for instance, {@code 9606} for human) that will be used to 
     * generate download files, separated by the {@code String} {@link CommandRunner#LIST_SEPARATOR}.
     * If it is not provided, all species contained in database will be used.
     * <li> a list of files types that will be generated ({@code EXPR_SIMPLE}, {@code EXPR_COMPLETE}, 
     * {@code DIFFEXPR_SIMPLE}, and {@code DIFFEXPR_SIMPLE}), separated by the 
     * {@code String} {@link CommandRunner#LIST_SEPARATOR}.
     * <li>the directory path that will be used to generate download files. 
     * </ol>
     * 
     * @param args          An {@code Array} of {@code String}s containing the requested parameters.
     * @throws IOException  If some files could not be used.
     * @throws UnsupportedOperationException If in the given {@code ExpressionCallParams},
     *                                        {@code isIncludeSubStages} is set to {@code true},
     *                                        because it is not implemented yet.
     */
    public static void main(String[] args) throws IOException, UnsupportedOperationException {
        log.entry((Object[]) args);

        // TODO Manage with multi-species!
        
        int expectedArgLengthSingleSpecies = 3; // species list and file types to be generated
        if (args.length != expectedArgLengthSingleSpecies) {
            throw log.throwing(new IllegalArgumentException(
                    "Incorrect number of arguments provided, expected " + 
                    expectedArgLengthSingleSpecies + " arguments, " + args.length + " provided."));
        }

        List<String> speciesIds = CommandRunner.parseListArgument(args[0]);
        Set<String> fileTypes   = new HashSet<String>(CommandRunner.parseListArgument(args[1])); 
        String directory        = args[2];
        
        //retrieve FileType from String argument
        Set<String> unknownFileTypes = new HashSet<String>();
        Set<FileType> filesToBeGenerated = EnumSet.noneOf(FileType.class);
        for (String inputFileType: fileTypes) {
            if (inputFileType.equals(FileType.EXPR_SIMPLE.getStringRepresentation())) {
                filesToBeGenerated.add(FileType.EXPR_SIMPLE);  
                
            } else if (inputFileType.equals(FileType.EXPR_COMPLETE.getStringRepresentation())) {
                filesToBeGenerated.add(FileType.EXPR_COMPLETE);    
                
            } else if (inputFileType.equals(FileType.DIFFEXPR_SIMPLE.getStringRepresentation())) {
                filesToBeGenerated.add(FileType.DIFFEXPR_SIMPLE);  
                
            } else if (inputFileType.equals(FileType.DIFFEXPR_COMPLETE.getStringRepresentation())) {
                filesToBeGenerated.add(FileType.DIFFEXPR_COMPLETE);  
                
            } else {
                unknownFileTypes.add(inputFileType);
            }
        }
        if (!unknownFileTypes.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "Some file types do not exist: " + unknownFileTypes));
        }
        
        GenerateDownladFile generate = new GenerateDownladFile();
        generate.generateSingleSpeciesFiles(speciesIds, filesToBeGenerated, directory);
        
        log.exit();
    }
    
    /**
     * Generate single species files, for the types defined by {@code fileTypes}, 
     * for the species defined by {@code speciesIds}, in the directory {@code directory}.
     * 
     * @param speciesIds     A {@code List} of {@code String}s that are the IDs of species for 
     *                       which files are generated.
     * @param fileTypes      A {@code Set} of {@code FileType}s containing file types to be generated.
     * @param directory      A {@code String} that is the directory path directory to store the 
     *                       generated files. 
     * @throws IOException   If an error occurred while trying to write generated files.
     * @throws UnsupportedOperationException If in the given {@code ExpressionCallParams},
     *                                        {@code isIncludeSubStages} is set to {@code true},
     *                                        because it is not implemented yet.
     */
    public void generateSingleSpeciesFiles(List<String> speciesIds, Set<FileType> fileTypes, 
            String directory) throws IOException, UnsupportedOperationException { 
        log.entry(speciesIds, fileTypes, directory);
        
        // Check user input, or retrieve all species IDs
        List<String> speciesIdsToUse = BgeeDBUtils.checkAndGetSpeciesIds(
                speciesIds, this.getSpeciesDAO()); 

        if (fileTypes == null || fileTypes.isEmpty()) {
            // If no file types are given by user, we set all file types
            fileTypes = EnumSet.allOf(FileType.class);
        } 
        
        // Retrieve gene names, stage names, anat. entity names, once for all species
        Set<String> setSpecies = new HashSet<String>(speciesIds);
        Map<String, String> geneNamesByIds = 
                BgeeDBUtils.getGeneNamesByIds(setSpecies, this.getGeneDAO());
        Map<String, String> stageNamesByIds = 
                BgeeDBUtils.getStageNamesByIds(setSpecies, this.getStageDAO());
        Map<String, String> anatEntityNamesByIds =
                BgeeDBUtils.getAnatEntityNamesByIds(setSpecies, this.getAnatEntityDAO());

        for (String speciesId: speciesIdsToUse) {
            log.info("Start generating of download files for the species {}...", speciesId);
            
            if (fileTypes.contains(FileType.DIFFEXPR_SIMPLE) ||
                    fileTypes.contains(FileType.DIFFEXPR_COMPLETE)) {
//                this.generateDiffExprFiles(directory, fileTypes, speciesId, 
//                        geneNamesByIds, stageNamesByIds, anatEntityNamesByIds);
            }
            
            if (fileTypes.contains(FileType.EXPR_SIMPLE) || 
                    fileTypes.contains(FileType.EXPR_COMPLETE)) {
                this.generateExprFiles(directory, fileTypes, speciesId, 
                        geneNamesByIds, stageNamesByIds, anatEntityNamesByIds);
            }
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
        try (AnatEntityTOResultSet rs = dao.getNonInformativeAnatEntities(speciesIds)) {
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
     * including data propagated from anatomical substructures or not, 
     * depending on {@code includeSubstructures}, and propagated from sub-stages or not, 
     * depending on {@code includeSubstages}. When data propagation is requested, 
     * calls generated by data propagation only, and occurring in anatomical entities 
     * with ID present in {@code nonInformativesAnatEntityIds}, are discarded.
     * <p>
     * The returned {@code ExpressionCallTO}s have no ID set, to be able 
     * to compare calls based on gene, stage and anatomical entity IDs.
     * 
     * @param speciesIds                    A {@code Set} of {@code String}s that are the IDs of 
     *                                      species allowing to filter the expression calls 
     *                                      to retrieve.
     * @param includeSubstructures          A {@code boolean} defining whether the 
     *                                      {@code ExpressionCallTO}s returned should be 
     *                                      global expression calls with data propagated 
     *                                      from substructures, or basic calls with no propagation. 
     *                                      If {@code true}, data are propagated. 
     * @param includeSubstages              A {@code boolean} defining whether the 
     *                                      {@code ExpressionCallTO}s returned should be 
     *                                      global expression calls with data propagated 
     *                                      from sub-stages, or basic calls with no propagation. 
     *                                      If {@code true}, data are propagated. 
     * @param nonInformativesAnatEntityIds  A {@code Set} of {@code String}s that are the IDs of 
     *                                      non-informative anatomical entities. Calls in these 
     *                                      anatomical entities, generated by data propagation 
     *                                      only, will be discarded.
     * @return                              A {@code List} of {@code ExpressionCallTO}s containing 
     *                                      all expression calls for the requested species.
     * @throws DAOException   If an error occurred while getting the data from the Bgee data source.
     */
    private List<ExpressionCallTO> loadExprCallsFromDb(Set<String> speciesIds, 
            boolean includeSubstructures, boolean includeSubstages,
            Set<String> nonInformativesAnatEntityIds) throws DAOException {
        log.entry(speciesIds, includeSubstructures, includeSubstages, nonInformativesAnatEntityIds);
        
        log.debug("Start retrieving expression calls (include substructures: {}, include sub-stages: {}) for the species IDs {}...", 
                includeSubstructures, includeSubstages, speciesIds);
    
        ExpressionCallDAO dao = this.getExpressionCallDAO();
        // We don't retrieve expression call ID to be able to compare calls on gene, 
        // stage and anatomical IDs.
        dao.setAttributes(EnumSet.complementOf(EnumSet.of(ExpressionCallDAO.Attribute.ID)));

        ExpressionCallParams params = new ExpressionCallParams();
        params.addAllSpeciesIds(speciesIds);
        params.setIncludeSubstructures(includeSubstructures);
        params.setIncludeSubStages(includeSubstages);
    
        List<ExpressionCallTO> exprTOs = new ArrayList<ExpressionCallTO>();
        try (ExpressionCallTOResultSet rsExpr = dao.getExpressionCalls(params)) {
            while (rsExpr.next()) {
                ExpressionCallTO to = rsExpr.getTO();
                log.trace("Iterating ExpressionCallTO: {}", to);
                //if the call was generated from propagated data only, we discard it 
                //if present in a non-informative anatomical entity.
                if (to.getAnatOriginOfLine().equals(ExpressionCallTO.OriginOfLine.DESCENT) && 
                        nonInformativesAnatEntityIds.contains(to.getAnatEntityId())) {
                    log.trace("Discarding propagated calls because in non-informative anatomical entity.");
                    continue;
                }
                exprTOs.add(to);
            }
        }

        log.info("Done retrieving global expression calls, {} calls found", exprTOs.size());
        return log.exit(exprTOs); 
    }

    /**
     * Retrieves all no-expression calls for the requested species from the Bgee data source, 
     * including data propagated from parent anatomical structures or not, 
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
     * @return                              A {@code List} of {@code NoExpressionCallTO}s containing 
     *                                      all global no-expression calls of the given species.
     * @throws DAOException     If an error occurred while getting the data from the Bgee database.
     */
    private List<NoExpressionCallTO> loadNoExprCallsFromDb(Set<String> speciesIds, 
            boolean includeParentStructures, Set<String> nonInformativesAnatEntityIds) 
                    throws DAOException {
        log.entry(speciesIds, includeParentStructures, nonInformativesAnatEntityIds);
        log.info("Start retrieving no-expression calls (include parent structures: {}) for the species IDs {}...", 
                includeParentStructures, speciesIds);
    
        NoExpressionCallDAO dao = this.getNoExpressionCallDAO();
        // We don't retrieve no-expression call IDs to be able to compare calls on gene, 
        // stage and anatomical IDs.
        dao.setAttributes(EnumSet.complementOf(EnumSet.of(NoExpressionCallDAO.Attribute.ID)));
    
        NoExpressionCallParams params = new NoExpressionCallParams();
        params.addAllSpeciesIds(speciesIds);
        params.setIncludeParentStructures(includeParentStructures);

        List<NoExpressionCallTO> noExprTOs = new ArrayList<NoExpressionCallTO>();
        try (NoExpressionCallTOResultSet rsNoExpr = dao.getNoExpressionCalls(params)) {
            while (rsNoExpr.next()) {
                NoExpressionCallTO to = rsNoExpr.getTO();
                log.trace("Iterating NoExpressionCallTO: {}", to);
                //if the call was generated from propagated data only, we discard it 
                //if present in a non-informative anatomical entity.
                if (to.getOriginOfLine().equals(NoExpressionCallTO.OriginOfLine.PARENT) && 
                        nonInformativesAnatEntityIds.contains(to.getAnatEntityId())) {
                    log.trace("Discarding propagated calls because in non-informative anatomical entity.");
                    continue;
                }
                noExprTOs.add(to);
            }
        }
        
        log.info("Done retrieving basic no-expression calls, {} calls found", noExprTOs.size());
        return log.exit(noExprTOs);  
    }
    
    /**
     * Generate download files (simple and/or advanced) containing absence/presence of expression.
     * This method is responsible for retrieving data from the data source, and then 
     * to write them into files.
     * 
     * @param directory             A {@code String} that is the directory to store  
     *                              the generated files. 
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
     * @throws IOException  If an error occurred while trying to write the {@code outputFile}.
     */
    private void generateExprFiles(String directory, Set<FileType> fileTypes, String speciesId, 
            Map<String, String> geneNamesByIds, Map<String, String> stageNamesByIds, 
            Map<String, String> anatEntityNamesByIds) throws IOException {
        log.entry(directory, fileTypes, speciesId, geneNamesByIds, stageNamesByIds, 
                anatEntityNamesByIds);
        log.debug("Start generating expression files for the species {}...", speciesId);
        
        Set<String> speciesFilter = new HashSet<String>();
        speciesFilter.add(speciesId);

        log.trace("Start retrieving data for expression files for the species {}...", speciesId);
        // Load non-informative anatomical entities only for the complete file: 
        // calls occurring in these anatomical entities, and generated from 
        // data propagation only (no observed data in them), will be discarded. 
        // For the simple file, we will use only anat. entities with observed data, 
        // so it's not necessary. 
        Set<String> nonInformativesAnatEntities = new HashSet<String>();
        if (fileTypes.contains(FileType.EXPR_COMPLETE)) {
            nonInformativesAnatEntities = 
                    this.loadNonInformativeAnatEntities(speciesFilter);
        }
        
        // We always load global expression calls, because we always try 
        // to match expression calls with potentially conflicting no-expression calls
        // (generated by different data types, as there can be no conflict for a given 
        // data type).
        List<ExpressionCallTO> globalExprTOs = new ArrayList<ExpressionCallTO>(); 
        globalExprTOs = this.loadExprCallsFromDb(speciesFilter, true, true,
                nonInformativesAnatEntities);

        // We always load propagated global no-expression calls, because we always try 
        // to match no-expression calls with potentially conflicting expression calls 
        // (generated by different data types, as there can be no conflict for a given 
        // data type).
        List<NoExpressionCallTO> globalNoExprTOs = this.loadNoExprCallsFromDb(
                speciesFilter, true, nonInformativesAnatEntities);
        
        log.trace("Done retrieving data for expression files for the species {}.", speciesId);
        
        // Note that this Collection is a Set, it cannot 
        // contain global and basic calls at the same time  
        Set<CallTO> allCallTOs = new HashSet<CallTO>();
        allCallTOs.addAll(globalExprTOs);
        allCallTOs.addAll(globalNoExprTOs);

        if (fileTypes.contains(FileType.EXPR_SIMPLE)) {
            log.trace("Start generation of data for simple file for the species {}", speciesId);
            
            List<Map<String, String>> fileRows = this.generateExprFileRows(geneNamesByIds, 
                    stageNamesByIds, anatEntityNamesByIds, allCallTOs, true);

            this.writeDownloadFile(fileRows, directory + speciesId + "_" + 
                    FileType.EXPR_SIMPLE.getStringRepresentation() + EXTENSION, 
                    FileType.EXPR_SIMPLE);
            
            log.trace("Done generation of data for simple file for the species {}",
                    speciesId);
        }
        
        if (fileTypes.contains(FileType.EXPR_COMPLETE)) {
            log.trace("Start generation of data for advanced file for the species {}",
                    speciesId);
            
            List<Map<String, String>> fileRows = this.generateExprFileRows(geneNamesByIds, 
                    stageNamesByIds, anatEntityNamesByIds, allCallTOs, false);

            this.writeDownloadFile(fileRows, directory + speciesId + "_" + 
                    FileType.EXPR_COMPLETE.getStringRepresentation() + EXTENSION, 
                    FileType.EXPR_COMPLETE);

            log.trace("Done generation of data for advanced file for the species {}", 
                    speciesId);
        }
        
        log.debug("Done generating expression files for the species {}.", speciesId);
    }

    /**
     * Generate the rows of an expression download file, with expression data already 
     * retrieved and provided through {@code allCallTOs}. The rows to be written in the file 
     * are returned as a {@code List} of {@code Map}s, where each {@code Map} corresponds to 
     * a row, and with each {@code Entry} in the {@code Map}s corresponding to a column, with key 
     * as column name, and value as column value for the associated row.
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
     * @param allCallTOs            A {@code SortedMap} where keys are {@code CallTO}s providing 
     *                              the information of gene-anat.entity-stage, the associated  
     *                              values being {@code Collection} of {@code CallTO}s with the  
     *                              corresponding gene-anat.entity-stage.
     * @param isSimplifiedFile      A {@code boolean} defining whether the output file is a simple 
     *                              file. 
     * @return  A {@code List} of {@code Map}s, where each {@code Map} corresponds to 
     *          a row, and with each {@code Entry} in the {@code Map}s corresponding to a column.
     */
    private List<Map<String, String>> generateExprFileRows(Map<String, String> geneNamesByIds,
            Map<String, String> stageNamesByIds, Map<String, String> anatEntityNamesByIds, 
            Set<CallTO> allCallTOs, boolean isSimplifiedFile) {
        log.entry(geneNamesByIds, stageNamesByIds, 
                anatEntityNamesByIds, allCallTOs, isSimplifiedFile);
        
        log.debug("Start generating file content by merging calls...");
        List<Map<String, String>> allRows = new ArrayList<Map<String, String>>();
        
        SortedMap<CallTO, Collection<CallTO>> allCalls = 
                this.groupAndOrderByGeneAnatEntityStage(allCallTOs);
        for (Entry<CallTO, Collection<CallTO>> callGroup : allCalls.entrySet()) {
            log.trace("Start merging the group of calls: {}", callGroup);
            Map<String, String> row = new HashMap<String, String>();
            
            //the current version of this method assumes that there will never be a mixture 
            //of global propagated calls and of basic calls. This is why we only have 
            //one ExpressionCallTO, and one NoExpressionCallTO.
            ExpressionCallTO expressionTO = null;
            NoExpressionCallTO noExpressionTO = null;

            for (CallTO call: callGroup.getValue()) {
                if (call instanceof  ExpressionCallTO) {
                    if (expressionTO == null) {
                        expressionTO = (ExpressionCallTO) call;                        
                    } else {
                        throw log.throwing(new IllegalArgumentException("The provided CallTO list(" +
                                call.getClass() + ") contains severals expression calls"));
                    }
                } else if (call instanceof NoExpressionCallTO){
                    if (noExpressionTO == null) {
                        noExpressionTO = (NoExpressionCallTO) call;
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
                throw log.throwing(new IllegalStateException("No basic and global calls for the triplet "
                        + "gene(" + callGroup.getKey().getGeneId() + 
                        ") - organ (" + callGroup.getKey().getAnatEntityId() + 
                        ") - stage (" + callGroup.getKey().getStageId() + ")"));
            }
            
            // Define if the call include observed data
            ObservedData observedData = ObservedData.NOTOBSERVED; 
            if ((expressionTO != null && !isPropagatedOnly(expressionTO))
                    || (noExpressionTO != null && !isPropagatedOnly(noExpressionTO))) {
                // stage and anatomical entity not propagated in the expression call 
                // OR anatomical entity not propagated in the no-expression call
                observedData = ObservedData.OBSERVED;
            }
            
            // We do not write calls in simple file if there are not observed.
            if (isSimplifiedFile && observedData.equals(ObservedData.NOTOBSERVED)) {
                continue;                
            }
            
            // Define summary column
            ExpressionData summary = ExpressionData.NODATA;
            if (expressionTO != null && noExpressionTO != null) {
                // Note: when isIncludeParentStructures is true, it means that the callTO was 
                // generated by a query including parent structures, but the OriginOfLine could 
                // still be SELF.  
                if (noExpressionTO.getOriginOfLine().equals(NoExpressionCallTO.OriginOfLine.PARENT)) {
                    summary = ExpressionData.LOWAMBIGUITY;                    
                } else {
                    summary = ExpressionData.HIGHAMBIGUITY;
                }
            } else if (expressionTO != null) {
                Set<DataState> allDataState = EnumSet.of(
                        expressionTO.getAffymetrixData(), expressionTO.getESTData(), 
                        expressionTO.getInSituData(), expressionTO.getRNASeqData());
                if (allDataState.contains(DataState.HIGHQUALITY)) {
                    summary = ExpressionData.HIGHQUALITY;
                } else if (allDataState.contains(DataState.LOWQUALITY)) {
                    summary = ExpressionData.LOWQUALITY;
                } else {
                    throw log.throwing(new IllegalStateException("All data states of the expression "
                            + " are set to no data)"));
                }
            } else if (noExpressionTO != null) {
                summary = ExpressionData.NOEXPRESSION;
            } 
            row.put(EXPRESSION_COLUMN_NAME, summary.getStringRepresentation());
            
            // Set IDs
            String geneId = callGroup.getKey().getGeneId();
            String anatEntityId = callGroup.getKey().getAnatEntityId();
            String stageId = callGroup.getKey().getStageId();
            row.put(GENE_ID_COLUMN_NAME, geneId);
            row.put(ANATENTITY_ID_COLUMN_NAME, anatEntityId);
            row.put(STAGE_ID_COLUMN_NAME, stageId);
            
            // Set names
            String geneName = geneNamesByIds.get(geneId);
            if (StringUtils.isBlank(geneName)) {
                throw log.throwing(new IllegalArgumentException("No name provided " +
                        "for gene ID " + geneId));
            }
            String anatEntityName = anatEntityNamesByIds.get(anatEntityId);
            if (StringUtils.isBlank(anatEntityName)) {
                throw log.throwing(new IllegalArgumentException("No name provided " +
                        "for anatomical entity ID " + anatEntityId));
            }
            String stageName = stageNamesByIds.get(stageId);
            if (StringUtils.isBlank(stageName)) {
                throw log.throwing(new IllegalArgumentException("No name provided " +
                        "for stage ID " + stageId));
            }
            row.put(GENE_NAME_COLUMN_NAME, geneName);
            row.put(STAGE_NAME_COLUMN_NAME, stageName);
            row.put(ANATENTITY_NAME_COLUMN_NAME, anatEntityName);

            //following columns are generated only for complete files
            if (!isSimplifiedFile) {
                // Write if the call include observed data
                row.put(INCLUDING_OBSERVED_DATA_COLUMN_NAME, observedData.getStringRepresentation());
                
                // Define data state for each data types
                if(expressionTO == null) {
                    expressionTO = new ExpressionCallTO(null, null, null, null, 
                            DataState.NODATA, DataState.NODATA, DataState.NODATA, DataState.NODATA, 
                            null, null, null, null);
                }
                if (noExpressionTO == null) {
                    noExpressionTO = new NoExpressionCallTO(null, null, null, null, 
                            DataState.NODATA, DataState.NODATA, DataState.NODATA, DataState.NODATA, 
                            null, null);
                }
                
                // Define data state for each data type
                row.put(AFFYMETRIXDATA_COLUMN_NAME, mergeExprAndNoExprDataStates(
                        expressionTO.getAffymetrixData(), noExpressionTO.getAffymetrixData()).
                        getStringRepresentation());
                row.put(ESTDATA_COLUMN_NAME, this.mergeExprAndNoExprDataStates(
                        expressionTO.getESTData(), DataState.NODATA).
                        getStringRepresentation());
                row.put(INSITUDATA_COLUMN_NAME, this.mergeExprAndNoExprDataStates(
                        expressionTO.getInSituData(), noExpressionTO.getInSituData()).
                        getStringRepresentation());
                row.put(RELAXEDINSITUDATA_COLUMN_NAME, this.mergeExprAndNoExprDataStates
                        (DataState.NODATA, noExpressionTO.getRelaxedInSituData()).
                        getStringRepresentation());
                row.put(RNASEQDATA_COLUMN_NAME, this.mergeExprAndNoExprDataStates(
                        expressionTO.getRNASeqData(), noExpressionTO.getRNASeqData()).
                        getStringRepresentation());
            }
            
            // Add current row to the list of rows
            allRows.add(row);
            log.debug("Added row: {}", row);
        }

        log.debug("Done generating file content by merging calls.");
        return log.exit(allRows);
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
            return log.exit(ExpressionData.NODATA);
        }
        //no no-expression data, we use the expression data
        if (dataStateExpr != DataState.NODATA) {
            if (dataStateExpr.equals(DataState.HIGHQUALITY)) {
                return log.exit(ExpressionData.HIGHQUALITY);
            } 
            if (dataStateExpr.equals(DataState.LOWQUALITY)) {
                return log.exit(ExpressionData.LOWQUALITY); 
            } 
            throw log.throwing(new IllegalArgumentException(
                    "The DataState provided (" + dataStateExpr.getStringRepresentation() + 
                    ") is not supported"));  
        } else if (dataStateNoExpr != DataState.NODATA) {
            //no-expression data available
            return log.exit(ExpressionData.NOEXPRESSION);
        }
        throw log.throwing(new IllegalStateException("An expression call and a no-expression call " +
                "could be found for the same data type."));
    }

    /**
     * Write a download file. The data are provided by a {@code List} of {@code Map}s, 
     * with each {@code Map} corresponding to a row, and with keys as column names 
     * and values as data associated to the column name. 
     * <p>
     * The generated TSV file will have a header line. 
     * 
     * @param inputList         A {@code List} of {@code Map}s where keys are column names and 
     *                          values are data associated to the column name.
     * @param outputFile        A {@code String} that is the path to the simple output file
     *                          were data will be written as TSV.
     * @param fileType          The {@code FileType} to be generated.
     * @throws IOException      If an error occurred while trying to write the {@code outputFile}.
     */
    private void writeDownloadFile(List<Map<String, String>> inputList, String outputFile, 
            FileType fileType) throws IOException {
        log.entry(inputList, outputFile, fileType);
                
        CellProcessor[] processors = generateCellProcessor(fileType);
        final String[] headers = this.generateHeader(fileType);
        
        try (ICsvMapWriter mapWriter = new CsvMapWriter(new FileWriter(outputFile),
                Utils.TSVCOMMENTED)) {
    
            mapWriter.writeHeader(headers);
            for (Map<String, String> map: inputList) {                
                Map<String, Object> row = new HashMap<String, Object>();
                for (Entry<String, String> entry : map.entrySet()) {
                    if (entry.getKey().equals(RELAXEDINSITUDATA_COLUMN_NAME)) {
                        // TODO For the moment, we do not write relaxed in situ column 
                        // because there is no data in the database. 
                        continue;
                    }
                    row.put(entry.getKey(), entry.getValue());
                }
                log.trace("Write the row: {}", row);
                mapWriter.write(row, headers, processors);
            }
        }

        log.exit();
    }
    
    /**
     * Generate an {@code Array} of {@code String}s to generate the header of a download file. 
     * 
     * @param fileType          The {@code FileType} to generate a header for.
     * @return                  An {@code Array} of {@code String}s needed to write 
     *                          a download file.
     */
    private String[] generateHeader(FileType fileType) {
        if (fileType == FileType.EXPR_SIMPLE || fileType == FileType.DIFFEXPR_SIMPLE) {
            if (fileType == FileType.DIFFEXPR_SIMPLE) {
                return log.exit(new String[] { 
                        GENE_ID_COLUMN_NAME, GENE_NAME_COLUMN_NAME, 
                        STAGE_ID_COLUMN_NAME, STAGE_NAME_COLUMN_NAME,
                        ANATENTITY_ID_COLUMN_NAME, ANATENTITY_NAME_COLUMN_NAME,
                        DIFFEXPRESSION_COLUMN_NAME});
            } 
            return log.exit(new String[] { 
                    GENE_ID_COLUMN_NAME, GENE_NAME_COLUMN_NAME, 
                    STAGE_ID_COLUMN_NAME, STAGE_NAME_COLUMN_NAME,
                    ANATENTITY_ID_COLUMN_NAME, ANATENTITY_NAME_COLUMN_NAME,
                    EXPRESSION_COLUMN_NAME});
        } 
        //headers for complete file
        // TODO For the moment, we do not write relaxed in situ column 
        // because there is no data in the database. 
        return log.exit(new String[] {
                GENE_ID_COLUMN_NAME, GENE_NAME_COLUMN_NAME, 
                STAGE_ID_COLUMN_NAME, STAGE_NAME_COLUMN_NAME,   
                ANATENTITY_ID_COLUMN_NAME, ANATENTITY_NAME_COLUMN_NAME,
                AFFYMETRIXDATA_COLUMN_NAME, ESTDATA_COLUMN_NAME, INSITUDATA_COLUMN_NAME, 
                //                  RELAXEDINSITUDATA_COLUMN_NAME, 
                RNASEQDATA_COLUMN_NAME, 
                INCLUDING_OBSERVED_DATA_COLUMN_NAME, EXPRESSION_COLUMN_NAME});
    }
    
    /**
     * Generate an {@code Array} of {@code CellProcessor}s needed to write a download file. 
     * 
     * @param fileType          The {@code FileType} to generate {@code CellProcessor}s for.
     * @return                  An {@code Array} of {@code CellProcessor}s needed to write 
     *                          a download file.
     */
    private CellProcessor[] generateCellProcessor(FileType fileType) {
        log.entry(fileType);
        
        List<Object> dataElements = new ArrayList<Object>();
        if (fileType == FileType.DIFFEXPR_SIMPLE || fileType == FileType.DIFFEXPR_COMPLETE) {
            for (DiffExpressionData data : DiffExpressionData.values()) {
                dataElements.add(data.getStringRepresentation());
            } 
        } else {
            for (ExpressionData data : ExpressionData.values()) {
                dataElements.add(data.getStringRepresentation());
            } 
        }
        
        List<Object> originElement = new ArrayList<Object>();
        for (ObservedData data : ObservedData.values()) {
            originElement.add(data.getStringRepresentation());
        } 
    
        final CellProcessor[] processors;
        if (fileType == FileType.DIFFEXPR_SIMPLE || fileType == FileType.EXPR_SIMPLE) {
                processors = new CellProcessor[] { 
                        new NotNull(), // gene ID
                        new NotNull(), // gene Name
                        new NotNull(), // developmental stage ID
                        new NotNull(), // developmental stage name
                        new NotNull(), // anatomical entity ID
                        new NotNull(), // anatomical entity name
                        new IsElementOf(dataElements)}; // Diff expression or Expression/No-expression
        } else {
                processors = new CellProcessor[] { 
                        new NotNull(), // gene ID
                        new NotNull(), // gene Name
                        new NotNull(), // developmental stage ID
                        new NotNull(), // developmental stage name
                        new NotNull(), // anatomical entity ID
                        new NotNull(), // anatomical entity name
                        new IsElementOf(dataElements),  // Affymetrix data
                        new IsElementOf(dataElements),  // EST data
                        new IsElementOf(dataElements),  // In Situ data
//                        new IsElementOf(dataElements),  // Relaxed in Situ data
                        new IsElementOf(dataElements),  // RNA-seq data
                        new IsElementOf(originElement), // Including observed data 
                        new IsElementOf(dataElements)}; // Diff expression or Expression/No-expression
        }
        return log.exit(processors);
    }

    /**
     * Generate the {@code List} of {@code Map}s containing data to be written in download file
     * for differential expression.
     * 
     * @param speciesId     A {@code String} that is the ID of species for which data are retrieved.
     * @return              A {@code List} of {@code Map}s where keys are column names and 
     *                      values are data associated to the column name.
     * @throws UnsupportedOperationException Not yet implemented
     */
    private List<Map<String, String>> generateDiffExprRows(String speciesId) 
            throws UnsupportedOperationException {
        log.entry(speciesId);
        // TODO Auto-generated method stub
        throw log.throwing(new UnsupportedOperationException("Differential expression is not yet "
                + "supported, it's need to be implemented"));
    }
}
