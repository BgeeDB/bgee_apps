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
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;

/**
 * Class used to generate expression TSV download files (simple and advanced files) 
 * from the Bgee database. 
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @since Bgee 13
 */
public class GenerateExprFile extends GenerateDownloadFile {

    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(GenerateExprFile.class.getName());

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
    public enum ExprFileType implements FileType {
        EXPR_SIMPLE("expr-simple", true), EXPR_COMPLETE("expr-complete", false);

        /**
         * A {@code String} that can be used to generate names of files of this type.
         */
        private final String stringRepresentation;
        
        /**
         * A {@code boolean} defining whether this {@code ExprFileType} is a simple 
         * file type
         */
        private final boolean simpleFileType;

        /**
         * Constructor providing the {@code String} representation of this 
         * {@code ExprFileType}, and a {@code boolean} defining whether this 
         * {@code ExprFileType} is a simple file type.
         */
        private ExprFileType(String stringRepresentation, boolean simpleFileType) {
            this.stringRepresentation = stringRepresentation;
            this.simpleFileType = simpleFileType;
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
        public String toString() {
            return this.getStringRepresentation();
        }
    }

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

        @Override
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
    private GenerateExprFile() {
        this(null, null, null, null);
    }
    /**
     * Constructor providing parameters to generate files, and using the default {@code DAOManager}.
     * 
     * @param speciesIds    A {@code List} of {@code String}s that are the IDs of species 
     *                      we want to generate data for. If {@code null} or empty, all species 
     *                      are used.
     * @param fileTypes     A {@code Set} of {@code ExprFileType}s that are the types
     *                      of files we want to generate. If {@code null} or empty, 
     *                      all {@code ExprFileType}s are generated .
     * @param directory     A {@code String} that is the directory where to store files.
     * @throws IllegalArgumentException If {@code directory} is {@code null} or blank.
     */
    public GenerateExprFile(List<String> speciesIds, Set<ExprFileType> fileTypes, 
            String directory) throws IllegalArgumentException {
        this(null, speciesIds, fileTypes, directory);
    }
    /**
     * Constructor providing the {@code MySQLDAOManager} that will be used by 
     * this object to perform queries to the database. This is useful for unit testing.
     * 
     * @param manager       the {@code MySQLDAOManager} to use.
     * @param speciesIds    A {@code List} of {@code String}s that are the IDs of species 
     *                      we want to generate data for. If {@code null} or empty, all species 
     *                      are used.
     * @param fileTypes     A {@code Set} of {@code ExprFileType}s that are the types
     *                      of files we want to generate. If {@code null} or empty, 
     *                      all {@code ExprFileType}s are generated .
     * @param directory     A {@code String} that is the directory where to store files.
     * @throws IllegalArgumentException If {@code directory} is {@code null} or blank.
     */
    public GenerateExprFile(MySQLDAOManager manager, List<String> speciesIds, 
            Set<ExprFileType> fileTypes, String directory) {
        super(manager, speciesIds, fileTypes, directory);
    }

    /**
     * Main method to trigger the generate expression TSV download files (simple and advanced 
     * files) from Bgee database. Parameters that must be provided in order in {@code args} are: 
     * <ol>
     * <li> a list of NCBI species IDs (for instance, {@code 9606} for human) that will be used to 
     * generate download files, separated by the {@code String} {@link CommandRunner#LIST_SEPARATOR}.
     * If it is not provided, all species contained in database will be used.
     * <li> a list of files types that will be generated ('expr-simple' for 
     * {@link FileType EXPR_SIMPLE}, and 'expr-complete' for {@link FileType EXPR_COMPLETE}), 
     * separated by the {@code String} {@link CommandRunner#LIST_SEPARATOR}.
     * <li>the directory path that will be used to generate download files. 
     * </ol>
     * 
     * @param args          An {@code Array} of {@code String}s containing the requested parameters.
     * @throws IOException  If an error occurred while trying to write generated files.
     */
    public static void main(String[] args) throws IOException {
        log.entry((Object[]) args);

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

        // Retrieve ExprFileType from String argument
        Set<String> unknownFileTypes = new HashSet<String>();
        Set<ExprFileType> filesToBeGenerated = new HashSet<ExprFileType>();
        inputFiles: for (String inputFileType: fileTypeNames) {
            for (ExprFileType fileType: ExprFileType.values()) {
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

        GenerateExprFile generator = new GenerateExprFile(speciesIds, filesToBeGenerated, 
                directory);
        generator.generateExprFiles();

        log.exit();
    }

    /**
     * Generate expression files, for the types defined by {@code fileTypes}, 
     * for species defined by {@code speciesIds}, in the directory {@code directory}.
     * 
     * @param speciesIds    A {@code List} of {@code String}s that are the IDs of species for 
     *                      which files are generated.
     * @param fileTypes     A {@code Set} of {@code FileType}s containing file types to be 
     *                      generated.
     * @param directory     A {@code String} that is the directory path directory to store the 
     *                      generated files. 
     * @throws IOException  If an error occurred while trying to write generated files.
     */
    public void generateExprFiles() throws IOException { 
        log.entry(this.speciesIds, this.fileTypes, this.directory);

        Set<String> setSpecies = new HashSet<String>();
        if (this.speciesIds != null) {
            setSpecies = new HashSet<String>(this.speciesIds);
        }

        // Check user input, retrieve info for generating file names
        // Retrieve species names and IDs (all species names if speciesIds is null or empty)
        Map<String, String> speciesNamesForFilesByIds = 
                this.checkAndGetLatinNamesBySpeciesIds(setSpecies);
        assert speciesNamesForFilesByIds.size() >= setSpecies.size();

        // If no file types are given by user, we set all file types
        if (this.fileTypes == null || this.fileTypes.isEmpty()) {
            this.fileTypes = EnumSet.allOf(ExprFileType.class);
        } 

        // Retrieve gene names, stage names, anat. entity names, once for all species
        Map<String, String> geneNamesByIds = 
                BgeeDBUtils.getGeneNamesByIds(setSpecies, this.getGeneDAO());
        Map<String, String> stageNamesByIds = 
                BgeeDBUtils.getStageNamesByIds(setSpecies, this.getStageDAO());
        Map<String, String> anatEntityNamesByIds =
                BgeeDBUtils.getAnatEntityNamesByIds(setSpecies, this.getAnatEntityDAO());

        // Generate expression files, species by species. 
        for (String speciesId: speciesNamesForFilesByIds.keySet()) {
            log.info("Start generating of expression files for the species {}...", 
                    speciesId);
            
            this.generateExprFilesForOneSpecies(speciesNamesForFilesByIds.get(speciesId), 
                    speciesId, geneNamesByIds, stageNamesByIds, 
                    anatEntityNamesByIds);

            //close connection to database between each species, to avoid idle connection reset
            this.getManager().releaseResources();
            log.info("Done generating of expression files for the species {}.", speciesId);
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
     *                       non-informative anatomical entity IDs of the given species.
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
        // We need all attributes except ID, stageOriginOfLine and observedData
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
     * Generate download files (simple and/or advanced) containing absence/presence of expression, 
     * for species defined by {@code speciesId}. This method is responsible for retrieving data 
     * from the data source, and then to write them into files, in the directory provided 
     * at instantiation. File types to be generated are provided at instantiation.
     * 
     * @param fileNamePrefix        A {@code String} to be used as a prefix of the names 
     *                              of the generated files. 
     * @param fileTypes             A {@code Set} of {@code ExprFileType}s that are the file 
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
    private void generateExprFilesForOneSpecies(String fileNamePrefix, 
            String speciesId, Map<String, String> geneNamesByIds, 
            Map<String, String> stageNamesByIds, Map<String, String> anatEntityNamesByIds) 
                    throws IOException {
        log.entry(this.directory, fileNamePrefix, this.fileTypes, speciesId, 
                geneNamesByIds, stageNamesByIds, anatEntityNamesByIds);

        log.debug("Start generating expression files for the species {} and file types {}...", 
                speciesId, fileTypes);

        //********************************
        // RETRIEVE DATA FROM DATA SOURCE
        //********************************
        Set<String> speciesFilter = new HashSet<String>();
        speciesFilter.add(speciesId);

        log.trace("Start retrieving data for expression files for the species {}...", 
                speciesId);
        // Load non-informative anatomical entities: 
        // calls occurring in these anatomical entities, and generated from 
        // data propagation only (no observed data in them), will be discarded. 
        Set<String> nonInformativesAnatEntities = 
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
        Map<String, Set<ExpressionCallTO>> globalExprTOsByGeneIds = 
                this.loadExprCallsByGeneIds(speciesFilter, true, nonInformativesAnatEntities);

        // We always load propagated global no-expression calls, because we always try 
        // to match no-expression calls with potentially conflicting expression calls 
        // (generated by different data types, as there can be no conflict for a given 
        // data type).
        Map<String, Set<NoExpressionCallTO>> globalNoExprTOsByGeneIds = 
                this.loadNoExprCallsByGeneIds(speciesFilter, true, nonInformativesAnatEntities);

        //In order to propagate expression calls to parent stages, we need to retrieve 
        //relations between stages.
        Map<String, Set<String>> stageParentsFromChildren = 
                BgeeDBUtils.getStageParentsFromChildren(speciesFilter, this.getRelationDAO());

        log.trace("Done retrieving data for expression files for the species {}.", speciesId);

        //****************************
        // PRODUCE AND WRITE DATA
        //****************************
        log.trace("Start generating and writing file content for species {} and file types {}...", 
                speciesId, this.fileTypes);

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

            for (FileType fileType: this.fileTypes) {
                CellProcessor[] fileTypeProcessors = null;
                String[] fileTypeHeaders = null;

                fileTypeProcessors = this.generateExprFileCellProcessors((ExprFileType) fileType);
                processors.put(fileType, fileTypeProcessors);
                fileTypeHeaders = this.generateExprFileHeader((ExprFileType) fileType);
                headers.put(fileType, fileTypeHeaders);

                //Create file name
                String fileName = fileNamePrefix + "_" + 
                        fileType.getStringRepresentation() + EXTENSION;
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
            //first, we retrieve and order all unique gene IDs, to have rows in files 
            //ordered by gene IDs
            Set<String> geneIds = new HashSet<String>();
            geneIds.addAll(globalExprTOsByGeneIds.keySet());
            geneIds.addAll(globalNoExprTOsByGeneIds.keySet());
            List<String> orderedGeneIds = new ArrayList<String>(geneIds);
            Collections.sort(orderedGeneIds);

            //now, we generate and write data one gene at a time to not overload memory.
            int geneCount = 0;
            for (String geneId: orderedGeneIds) {
                geneCount++;
                if (log.isDebugEnabled() && geneCount % 2000 == 0) {
                    log.debug("Iterating gene {} over {}", geneCount, orderedGeneIds.size());
                }

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
                Set<CallTO> allCallTOs = new HashSet<CallTO>();
                allCallTOs.addAll(stagePropagatedExprCallTOs);
                //remove calls from Map to free some memory
                Set<NoExpressionCallTO> noExprCallTOs = globalNoExprTOsByGeneIds.remove(geneId);
                if (noExprCallTOs != null) {
                    allCallTOs.addAll(noExprCallTOs);
                }

                //and now, we compute and write the rows in all files
                this.writeExprRows(geneNamesByIds, stageNamesByIds, anatEntityNamesByIds,
                        writersUsed, processors, headers, geneId, allCallTOs);
            }
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
     * an expression TSV file of type {@code fileType}.
     * 
     * @param fileType  The {@code ExprFileType} of the file to be generated.
     * @return          An {@code Array} of {@code CellProcessor}s used to process 
     *                  an expression file.
     * @throw IllegalArgumentException  If {@code fileType} is not managed by this method.
     */
    private CellProcessor[] generateExprFileCellProcessors(ExprFileType fileType) 
            throws IllegalArgumentException {
        log.entry(fileType);

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
                // TODO: when relaxed in situ will be in the database, uncomment following line
                // new IsElementOf(dataElements),  // Relaxed in Situ data
                new IsElementOf(dataElements),  // RNA-seq data
                new IsElementOf(originElement), // Including observed data 
                new IsElementOf(dataElements)}); // Expression
    }

    /**
     * Generates an {@code Array} of {@code String}s used to generate the header of  
     * an expression TSV file of type {@code fileType}.
     * 
     * @param fileType  The {@code ExprFileType} of the file to be generated.
     * @return          An {@code Array} of {@code String}s used to produce the header.
     * @throw IllegalArgumentException  If {@code fileType} is not managed by this method.
     */
    private String[] generateExprFileHeader(ExprFileType fileType) throws IllegalArgumentException {
        log.entry(fileType);

        if (fileType.isSimpleFileType()) {
            return log.exit(new String[] { 
                    GENE_ID_COLUMN_NAME, GENE_NAME_COLUMN_NAME, 
                    STAGE_ID_COLUMN_NAME, STAGE_NAME_COLUMN_NAME,
                    ANATENTITY_ID_COLUMN_NAME, ANATENTITY_NAME_COLUMN_NAME,
                    EXPRESSION_COLUMN_NAME});
        } 

        return log.exit(new String[] {
                GENE_ID_COLUMN_NAME, GENE_NAME_COLUMN_NAME, 
                STAGE_ID_COLUMN_NAME, STAGE_NAME_COLUMN_NAME,   
                ANATENTITY_ID_COLUMN_NAME, ANATENTITY_NAME_COLUMN_NAME,
                AFFYMETRIX_DATA_COLUMN_NAME, ESTDATA_COLUMN_NAME, INSITUDATA_COLUMN_NAME, 
                // TODO: when relaxed in situ will be in the database, uncomment following line
                //                  RELAXEDINSITUDATA_COLUMN_NAME, 
                RNASEQ_DATA_COLUMN_NAME, 
                INCLUDING_OBSERVED_DATA_COLUMN_NAME, EXPRESSION_COLUMN_NAME});
    }

    /**
     * Generate a row to be written in an expression download file. This methods will notably 
     * use {@code callTOs} to produce expression information, that is different depending on 
     * {@code fileType}. The results are returned as a {@code Map}; it can be {@code null} 
     * if the {@code callTOs} provided do not allow to generate information to be included 
     * in the file of the given {@code ExprFileType}.
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
     * <li>information generated for files of the type {@link ExprFileType EXPR_COMPLETE}: 
     * entries with keys equal to {@link #AFFYMETRIX_DATA_COLUMN_NAME}, 
     * {@link #ESTDATA_COLUMN_NAME}, {@link #INSITUDATA_COLUMN_NAME},
     * {@link #RELAXEDINSITUDATA_COLUMN_NAME}, {@link #RNASEQDATA_COLUMN_NAME}, 
     * {@link #INCLUDING_OBSERVED_DATA_COLUMN_NAME}.
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
     * @param fileType          The {@code ExprFileType} defining which type of file should be 
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
            // TODO: when relaxed in situ will be in the database, uncomment following line
            //  row.put(RELAXEDINSITUDATA_COLUMN_NAME, this.mergeExprAndNoExprDataStates
            //          (DataState.NODATA, noExpressionTO.getRelaxedInSituData()).
            //          getStringRepresentation());
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
     * Merge {@code DataState}s of one expression call and one no-expression call into 
     * an {@code ExpressionData}. 
     * 
     * @param dataStateExpr     A {@code DataState} from an expression call. 
     * @param dataStateNoExpr   A {@code DataState} from a no-expression call.
     * @return                  An {@code ExpressionData} combining {@code DataState}s of
     *                          one expression call and one no-expression call.
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
     * Generate rows to be written and write them in a file. This methods will 
     * notably use {@code callTOs} to produce information, that is different depending on 
     * {@code fileType}.  
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
     * @param allCallTOs            A {@code Set} of {@code CallTO}s that are calls to be written. 
     * @throws IOException  If an error occurred while trying to write the {@code outputFile}.
     */
    private void writeExprRows(Map<String, String> geneNamesByIds, 
            Map<String, String> stageNamesByIds, Map<String, String> anatEntityNamesByIds, 
            Map<FileType, ICsvMapWriter> writersUsed, 
            Map<FileType, CellProcessor[]> processors, 
            Map<FileType, String[]> headers, String geneId, Set<CallTO> allCallTOs) 
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

            for (Entry<FileType, ICsvMapWriter> writerFileType: writersUsed.entrySet()) {
                Map<String, String> row = null;
                try {
                    row = this.generateExprRow(geneId, geneNamesByIds.get(geneId), 
                            stageId, stageNamesByIds.get(stageId), 
                            anatEntityId, anatEntityNamesByIds.get(anatEntityId), 
                            callGroup.getValue(), writerFileType.getKey());
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
