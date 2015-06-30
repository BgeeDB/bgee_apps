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
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO.OriginOfLine;
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
 * Class used to generate expression TSV download files (simple and advanced files) from
 * the Bgee database.
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @since Bgee 13
 */
//TODO: stop using these awful Maps, use a BeanReader/BeanWriter instead,
//see org.bgee.pipeline.annotations.SimilarityAnnotationUtils
//FIXME: use "low quality" instead of "poor quality"
public class GenerateExprFile extends GenerateDownloadFile {

    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(GenerateExprFile.class.getName());

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
    public enum SingleSpExprFileType implements FileType {
        EXPR_SIMPLE("expr-simple", true), EXPR_COMPLETE("expr-complete", false);

        /**
         * A {@code String} that can be used to generate names of files of this type.
         */
        private final String stringRepresentation;

        /**
         * A {@code boolean} defining whether this {@code ExprFileType} is a simple file
         * type
         */
        private final boolean simpleFileType;

        /**
         * Constructor providing the {@code String} representation of this
         * {@code ExprFileType}, and a {@code boolean} defining whether this
         * {@code ExprFileType} is a simple file type.
         */
        private SingleSpExprFileType(String stringRepresentation,
            boolean simpleFileType) {
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
     * <li>{@code NO_DATA}: no data from the associated data type allowed to produce the
     * call.
     * <li>{@code NOEXPRESSION}:    no-expression was detected from the associated data type.
     * <li>{@code LOWQUALITY}:      low-quality expression was detected from the associated
     *                              data type.
     * <li>{@code HIGHQUALITY}:     high-quality expression was detected from the associated
     *                              data type.
     * <li>{@code LOWAMBIGUITY}:    different data types are not coherent with an inferred
     *                              no-expression call (for instance, Affymetrix data reveals an 
     *                              expression while <em>in situ</em> data reveals an inferred
     *                              no-expression).
     * <li>{@code HIGHAMBIGUITY}:   different data types are not coherent without at least
     *                              an inferred no-expression call (for instance, Affymetrix data 
     *                              reveals expression while <em>in situ</em> data reveals a 
     *                              no-expression without been inferred).
     * </ul>
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public enum ExpressionData {
        NO_DATA("no data"), NO_EXPRESSION("absent"), EXPRESSION("present"),
        LOW_AMBIGUITY("low ambiguity"), HIGH_AMBIGUITY("high ambiguity");

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
     * An {@code Enum} used to define whether the call has been observed. This is to distinguish
     * from propagated data only, that should provide a lower confidence in the call.
     * <ul>
     * <li>{@code OBSERVED}:    the call has been observed at least once.
     * <li>{@code NOTOBSERVED}: the call has never been observed.
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
         * @param stringRepresentation A {@code String} corresponding to this {@code ObservedData}.
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
     * Main method to trigger the generate expression TSV download files (simple and advanced 
     * files) from Bgee database. Parameters that must be provided in order in {@code args} are:
     * <ol>
     * <li>a list of NCBI species IDs (for instance, {@code 9606} for human) that will be used to 
     * generate download files, separated by the {@code String} {@link CommandRunner#LIST_SEPARATOR}. 
     * If an empty list is provided (see {@link CommandRunner#EMPTY_LIST}), all species contained 
     * in database will be used.
     * <li>a list of files types that will be generated ('expr-simple' for
     * {@link FileType EXPR_SIMPLE}, and 'expr-complete' for {@link FileType EXPR_COMPLETE}), 
     * separated by the {@code String} {@link CommandRunner#LIST_SEPARATOR}. If an empty list is 
     * provided (see {@link CommandRunner#EMPTY_LIST}), all possible file types will be generated.
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

        GenerateExprFile generator = new GenerateExprFile(
            CommandRunner.parseListArgument(args[0]),
            GenerateDownloadFile.convertToFileTypes(
                CommandRunner.parseListArgument(args[1]), SingleSpExprFileType.class),
            args[2]);
        generator.generateExprFiles();

        log.exit();
    }

    /**
     * A {@code boolean} defining whether the filter for simple file keeps observed data only 
     * if {@code true} or organ observed data only (propagated stages are allowed) if {@code false}.
     */
    protected final boolean observedDataOnly;

    /**
     * Default constructor.
     */
    // suppress warning as this default constructor should not be used.
    @SuppressWarnings("unused")
    private GenerateExprFile() {
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
    public GenerateExprFile(List<String> speciesIds, Set<SingleSpExprFileType> fileTypes, 
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
    public GenerateExprFile(MySQLDAOManager manager, List<String> speciesIds,
        Set<SingleSpExprFileType> fileTypes, String directory) throws IllegalArgumentException {
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
    public GenerateExprFile(MySQLDAOManager manager, List<String> speciesIds,
        Set<SingleSpExprFileType> fileTypes, String directory, boolean observedDataOnly) 
                throws IllegalArgumentException {
        super(manager, speciesIds, fileTypes, directory);
        this.observedDataOnly = observedDataOnly;
    }
    
    /**
     * Generate expression files, for the types defined by {@code fileTypes}, for species
     * defined by {@code speciesIds}, in the directory {@code directory}.
     * 
     * @param speciesIds    A {@code List} of {@code String}s that are the IDs of species for
     *                      which files are generated.
     * @param fileTypes     A {@code Set} of {@code FileType}s containing file types to be generated.
     * @param directory     A {@code String} that is the directory path directory to store the
     *                      generated files.
     * @throws IOException  If an error occurred while trying to write generated files.
     */
    //TODO: add OMA node ID in complete files
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
            this.fileTypes = EnumSet.allOf(SingleSpExprFileType.class);
        }

        // Retrieve gene names, stage names, anat. entity names, once for all species
        Map<String, String> geneNamesByIds = 
                BgeeDBUtils.getGeneNamesByIds(setSpecies, this.getGeneDAO());
        Map<String, String> stageNamesByIds = 
                BgeeDBUtils.getStageNamesByIds(setSpecies, this.getStageDAO());
        Map<String, String> anatEntityNamesByIds = 
                BgeeDBUtils.getAnatEntityNamesByIds(setSpecies, this.getAnatEntityDAO());

        // Generate expression files, species by species.
        for (String speciesId : speciesNamesForFilesByIds.keySet()) {
            log.info("Start generating of expression files for the species {}...", speciesId);

            try {
                this.generateExprFilesForOneSpecies(speciesNamesForFilesByIds.get(speciesId), 
                        speciesId, geneNamesByIds, stageNamesByIds, anatEntityNamesByIds);
            } finally {
                // close connection to database between each species, to avoid idle
                // connection reset
                this.getManager().releaseResources();
            }
            log.info("Done generating of expression files for the species {}.", speciesId);
        }

        log.exit();
    }

    /**
     * Retrieves non-informative anatomical entities for the requested species. They
     * correspond to anatomical entities belonging to non-informative subsets in Uberon,
     * and with no observed data from Bgee (no basic calls of any type in them).
     * 
     * @param speciesIds    A {@code Set} of {@code String}s that are the IDs of species
     *                      allowing to filter the non-informative anatomical entities to use.
     * @return              A {@code Set} of {@code String}s containing all non-informative 
     *                      anatomical entity IDs of the given species.
     * @throws DAOException If an error occurred while getting the data from the Bgee data source.
     */
    private Set<String> loadNonInformativeAnatEntities(Set<String> speciesIds) throws DAOException {
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
     * grouped by gene IDs, including data propagated from anatomical substructures or
     * not, depending on {@code includeSubstructures}. When data propagation is requested,
     * calls generated by data propagation only, and occurring in anatomical entities with
     * ID present in {@code nonInformativesAnatEntityIds}, are discarded.
     * <p>
     * The returned {@code ExpressionCallTO}s have no ID set, to be able to compare calls
     * based on gene, stage and anatomical entity IDs.
     * <p>
     * Note that it is currently not possible to request for data propagated from
     * sub-stages: Propagating such data for a whole species can have a huge memory cost
     * and is slow. The propagation to parent stages will be done directly when writing
     * files, to not overload the memory.
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

        Map<String, Set<ExpressionCallTO>> callsByGeneIds = new HashMap<String, Set<ExpressionCallTO>>();
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
                // if present in a non-informative anatomical entity.
                if (nonInformativesAnatEntityIds.contains(to.getAnatEntityId())) {
                    log.trace("Discarding propagated calls because in non-informative anatomical entity: {}.",
                        to);
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
     * The returned {@code NoExpressionCallTO}s have no ID set, to be able to compare
     * calls based on gene, stage and anatomical entity IDs.
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
     * @param allConditions                 A {@code Set} of {@code SingleSpeciesCondition}s that
     *                                      are the allowed conditions. Calls not in these conditions, 
     *                                      will be discarded. If it is {@code null} or empty, 
     *                                      there is no filter.
     * @return                              A {@code Map} where keys are {@code String}s that 
     *                                      are gene IDs, the associated values being a {@code Set} 
     *                                      of {@code NoExpressionCallTO}s associated to this gene.
     * @throws DAOException                 If an error occurred while getting the data from the 
     *                                      Bgee database.
     */
    private Map<String, Set<NoExpressionCallTO>> loadNoExprCallsByGeneIds(Set<String> speciesIds, 
            boolean includeParentStructures, Set<String> nonInformativesAnatEntityIds,
            Set<SingleSpeciesCondition> allConditions) throws DAOException {
        log.entry(speciesIds, includeParentStructures, nonInformativesAnatEntityIds, allConditions);

        log.debug("Start retrieving no-expression calls (include parent structures: {}) for the species IDs {}...",
            includeParentStructures, speciesIds);

        Map<String, Set<NoExpressionCallTO>> callsByGeneIds = new HashMap<String, Set<NoExpressionCallTO>>();
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
                // if present in a non-informative anatomical entity.
                if (nonInformativesAnatEntityIds.contains(to.getAnatEntityId())) {
                    log.trace("Discarding propagated calls because in non-informative anatomical entity: {}.", to);
                    continue;
                }
                if (allConditions != null && !allConditions.isEmpty() && 
                        !allConditions.contains(new SingleSpeciesCondition(to))) {
                    log.trace("Discarding propagated calls because not in condition: {}.", to);
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

        // We always load expression calls and no-expression calls, because we need to find 
        // if the call is "observed data" for each data type.
        Map<String, Set<ExpressionCallTO>> exprTOsByGeneIds =
                this.loadExprCallsByGeneIds(speciesFilter, false, nonInformativesAnatEntities);
        Map<String, Set<NoExpressionCallTO>> noExprTOsByGeneIds =
                this.loadNoExprCallsByGeneIds(speciesFilter, false, nonInformativesAnatEntities, null);

        // In order to propagate expression calls to parent stages, we need to retrieve
        // relations between stages.
        Map<String, Set<String>> stageParentsFromChildren = 
                BgeeDBUtils.getStageParentsFromChildren(speciesFilter, this.getRelationDAO());


        //we retrieve expression and no-expression calls grouped by geneIds. This is because, 
        //to correctly propagate expression calls to parent stages, we need to examine all calls 
        //related to a gene at the same time. We cannot propagate everything at once because 
        //it can use too much memory (several hundreds of GB). So, we propagate everything 
        //for a given gene, write results in files, and move to the next gene.

        // We always load global expression calls, because we always try
        // to match expression calls with potentially conflicting no-expression calls
        // (generated by different data types, as there can be no conflict for a given data type).
        Map<String, Set<ExpressionCallTO>> globalExprTOsByGeneIds =
                this.loadExprCallsByGeneIds(speciesFilter, true, nonInformativesAnatEntities);

        // We always load propagated global no-expression calls, because we always try
        // to match no-expression calls with potentially conflicting expression calls
        // (generated by different data types, as there can be no conflict for a given
        // data type).
        // We retrieve stage and anatomical entity conditions from expression and no-expression calls, 
        // and retrieve their parent anatomical entities/ parent stages, 
        // in order to get the valid conditions to propagate no-expression calls 
        // (to propagate them to child anatomical entities; yes, this is complicated...). 
        Set<SingleSpeciesCondition> conditions = new HashSet<SingleSpeciesCondition>();
        conditions.addAll(this.getConditions(exprTOsByGeneIds));
        conditions.addAll(this.getConditions(noExprTOsByGeneIds));
        Set<SingleSpeciesCondition> allConditions = this.propagateConditions(conditions, 
                stageParentsFromChildren, BgeeDBUtils.getAnatEntityParentsFromChildren(
                        speciesFilter, this.getRelationDAO()));
        //Now, get the propagated no-expression calls.
        Map<String, Set<NoExpressionCallTO>> globalNoExprTOsByGeneIds = 
                this.loadNoExprCallsByGeneIds(speciesFilter, true, 
                        nonInformativesAnatEntities, allConditions);

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

        // we will write results in temporary files that we will rename at the end
        // if everything is correct
        String tmpExtension = ".tmp";

        // in order to close all writers in a finally clause
        Map<FileType, ICsvMapWriter> writersUsed = new HashMap<FileType, ICsvMapWriter>();
        try {
            //**************************
            // OPEN FILES, CREATE WRITERS, WRITE HEADERS
            //**************************
            Map<FileType, CellProcessor[]> processors = new HashMap<FileType, CellProcessor[]>();
            Map<FileType, String[]> headers = new HashMap<FileType, String[]>();

            for (FileType fileType : this.fileTypes) {
                CellProcessor[] fileTypeProcessors = null;
                String[] fileTypeHeaders = null;

                fileTypeProcessors = 
                        this.generateExprFileCellProcessors((SingleSpExprFileType) fileType);
                processors.put(fileType, fileTypeProcessors);
                fileTypeHeaders = 
                        this.generateExprFileHeader((SingleSpExprFileType) fileType);
                headers.put(fileType, fileTypeHeaders);

                // Create file name
                String fileName = this.formatString(fileNamePrefix + "_" +
                        fileType.getStringRepresentation() + EXTENSION);
                generatedFileNames.put(fileType, fileName);

                // write in temp file
                File file = new File(this.directory, fileName + tmpExtension);
                // override any existing file
                if (file.exists()) {
                    file.delete();
                }

                // create writer and write header
                ICsvMapWriter mapWriter = new CsvMapWriter(new FileWriter(file), 
                        Utils.getCsvPreferenceWithQuote(this.generateQuoteMode(fileTypeHeaders)));
                mapWriter.writeHeader(fileTypeHeaders);
                writersUsed.put(fileType, mapWriter);
            }

            // ****************************
            // WRITE ROWS
            // ****************************
            // first, we retrieve and order all unique gene IDs, to have rows in files
            // ordered by gene IDs
            Set<String> geneIds = new HashSet<String>();
            geneIds.addAll(globalExprTOsByGeneIds.keySet());
            geneIds.addAll(globalNoExprTOsByGeneIds.keySet());
            List<String> orderedGeneIds = new ArrayList<String>(geneIds);
            Collections.sort(orderedGeneIds);

            // now, we generate and write data one gene at a time to not overload memory.
            int geneCount = 0;
            for (String geneId : orderedGeneIds) {
                geneCount++;
                if (log.isDebugEnabled() && geneCount % 2000 == 0) {
                    log.debug("Iterating gene {} over {}", geneCount,
                        orderedGeneIds.size());
                }

                // OK, first, we need to propagate expression calls to parent stages
                // (calls were retrieved with propagation to parent organs already
                // performed)
                Set<ExpressionCallTO> stagePropagatedExprCallTOs = new HashSet<ExpressionCallTO>();
                // remove calls from Map to free some memory
                Set<ExpressionCallTO> globalExprCallTOs = globalExprTOsByGeneIds.remove(geneId);
                if (globalExprCallTOs != null && !globalExprCallTOs.isEmpty()) {
                    stagePropagatedExprCallTOs = this.updateGlobalExpressions(
                            this.groupExpressionCallTOsByPropagatedCalls(
                                    globalExprCallTOs, stageParentsFromChildren, false), 
                                    false, true).keySet();
                }

                // now, we need to aggregate expression and no-expression calls (basic and global), 
                // and to order them.
                Collection<CallTO> allCallTOs = new ArrayList<CallTO>();
                allCallTOs.addAll(stagePropagatedExprCallTOs);
                // remove calls from Map to free some memory
                Set<NoExpressionCallTO> globalNoExprCallTOs = globalNoExprTOsByGeneIds.remove(geneId);
                if (globalNoExprCallTOs != null) {
                    allCallTOs.addAll(globalNoExprCallTOs);
                }
                Set<NoExpressionCallTO> noExprCallTOs = noExprTOsByGeneIds.remove(geneId);
                if (noExprCallTOs != null) {
                    allCallTOs.addAll(noExprCallTOs);
                }
                Set<ExpressionCallTO> exprCallTOs = exprTOsByGeneIds.remove(geneId);
                if (exprCallTOs != null) {
                    allCallTOs.addAll(exprCallTOs);
                }

                // and now, we compute and write the rows in all files
                this.writeExprRows(geneNamesByIds, stageNamesByIds, anatEntityNamesByIds,
                        writersUsed, processors, headers, geneId, allCallTOs);
            }
        } catch (Exception e) {
            this.deleteTempFiles(generatedFileNames, tmpExtension);
            throw e;
        } finally {
            for (ICsvMapWriter writer : writersUsed.values()) {
                writer.close();
            }
        }
        // now, if everything went fine, we rename the temporary files
        this.renameTempFiles(generatedFileNames, tmpExtension);

        log.exit();
    }

    /**
     * Propagate {@code SingleSpeciesCondition}s to parent anatomical entities and to parent 
     * developmental stages, using the relations provided through {@code stageParentsFromChildren} 
     * and {@code anatEntityParentsFromChildren}. The returned {@code Set} will also include 
     * the non-propagated {@code conditions}.
     *
     * @param conditions                    A {@code Set} of {@code SingleSpeciesCondition}s that 
     *                                      are conditions to be propagated.
     * @param stageParentsFromChildren      A {@code Map} where keys are {@code String}s 
     *                                      representing the IDs of stages that are the source of 
     *                                      a relation, the associated value being a {@code Set} of 
     *                                      {@code String}s that are the IDs of their associated 
     *                                      targets. Only relations with a {@code RelationType} 
     *                                      {@code ISA_PARTOF} should be provided, but with 
     *                                      any {@code RelationStatus} ({@code REFLEXIVE}, 
     *                                      {@code DIRECT}, {@code INDIRECT}).
     * @param anatEntityParentsFromChildren A {@code Map} where keys are {@code String}s 
     *                                      representing the IDs of anatomical entities that are  
     *                                      the source of a relation, the associated value being a 
     *                                      {@code Set} of {@code String}s that are the IDs of 
     *                                      their associated targets. Only relations with a 
     *                                      {@code RelationType} {@code ISA_PARTOF} should be 
     *                                      provided, but with any {@code RelationStatus} (
     *                                      {@code REFLEXIVE}, {@code DIRECT}, {@code INDIRECT}).
     * @return                              A {@code Set} of {@code SingleSpeciesCondition}s that 
     *                                      are stage and anatomical propagated conditions, 
     *                                      also including the provided {@code conditions}.
     */
    private Set<SingleSpeciesCondition> propagateConditions(
            Set<SingleSpeciesCondition> conditions, 
            Map<String, Set<String>> stageParentsFromChildren,
            Map<String, Set<String>> anatEntityParentsFromChildren) {
        log.entry(conditions, stageParentsFromChildren, anatEntityParentsFromChildren);
        log.debug("conditions= {}", conditions);
        log.debug("stageParentsFromChildren= {}", stageParentsFromChildren);
        log.debug("anatEntityParentsFromChildren= {}", anatEntityParentsFromChildren);

        Set<SingleSpeciesCondition> propagatedConditions = new HashSet<SingleSpeciesCondition>();
        
        for (SingleSpeciesCondition currentCondition : conditions) {
            assert currentCondition.getAnatEntityId() != null;
            assert currentCondition.getStageId() != null;
            
            Set<String> stageParentIds = new HashSet<String>() ;
            if (stageParentsFromChildren != null) {
                stageParentIds = stageParentsFromChildren.get(currentCondition.getStageId());
            }
            stageParentIds.add(currentCondition.getStageId());

            Set<String> anatEntityParentIds = new HashSet<String>() ;
            if (anatEntityParentsFromChildren != null) {
                anatEntityParentIds = anatEntityParentsFromChildren.get(currentCondition.getAnatEntityId());
            }
            anatEntityParentIds.add(currentCondition.getAnatEntityId());

            for (String anatEntityParentId: anatEntityParentIds) {
                for (String stageParentId: stageParentIds) {
                    propagatedConditions.add(
                            new SingleSpeciesCondition(anatEntityParentId, stageParentId));
                }                
            }
        }
        
        return log.exit(propagatedConditions);
    }

    /**
     * Retrieves all conditions (anatomical entity/stage) of the provided calls.
     *
     * @param callTOsByGeneIds  A {@code Map} where keys are {@code String}s that are gene IDs, 
     *                          the associated values being a {@code Set} of {@code T}s 
     *                          associated to this gene.
     * @return                  A {@code Set} of {@code SingleSpeciesCondition}s that are 
     *                          conditions (anatomical entity/stage) contains in the provided calls.
     */
    private <T extends CallTO> Set<SingleSpeciesCondition> getConditions(
            Map<String, Set<T>> callTOsByGeneIds) {
      log.entry(callTOsByGeneIds);
      
      Set<SingleSpeciesCondition> allConditions = new HashSet<SingleSpeciesCondition>();
      for (Set<T> groupedCallTOs: callTOsByGeneIds.values()) {
          for (T callTO : groupedCallTOs) {
              allConditions.add(new SingleSpeciesCondition(callTO));
          }
      }

      return log.exit(allConditions);
    }

    /**
     * Generates an {@code Array} of {@code CellProcessor}s used to process an expression
     * TSV file of type {@code fileType}.
     * 
     * @param fileType  The {@code ExprFileType} of the file to be generated.
     * @return          An {@code Array} of {@code CellProcessor}s used to process 
     *                  an expression file.
     * @throw IllegalArgumentException If {@code fileType} is not managed by this method.
     */
    private CellProcessor[] generateExprFileCellProcessors(SingleSpExprFileType fileType) 
            throws IllegalArgumentException {
        log.entry(fileType);

        List<Object> expressionValues = new ArrayList<Object>();
        for (ExpressionData data : ExpressionData.values()) {
            expressionValues.add(data.getStringRepresentation());
        }

        List<Object> specificTypeQualities = new ArrayList<Object>();
        specificTypeQualities.add(DataState.HIGHQUALITY.getStringRepresentation());
        specificTypeQualities.add(DataState.LOWQUALITY.getStringRepresentation());
        specificTypeQualities.add(DataState.NODATA.getStringRepresentation());

        List<Object> resumeQualities = new ArrayList<Object>();
        resumeQualities.add(DataState.HIGHQUALITY.getStringRepresentation());
        resumeQualities.add(DataState.LOWQUALITY.getStringRepresentation());
        resumeQualities.add(GenerateDiffExprFile.NA_VALUE);

        List<Object> originValues = new ArrayList<Object>();
        for (ObservedData data : ObservedData.values()) {
            originValues.add(data.getStringRepresentation());
        }

        if (fileType.isSimpleFileType()) {
            return log.exit(new CellProcessor[] { 
                    new StrNotNullOrEmpty(),    // gene ID
                    new NotNull(),              // gene Name
                    new StrNotNullOrEmpty(),    // anatomical entity ID
                    new StrNotNullOrEmpty(),    // anatomical entity name
                    new StrNotNullOrEmpty(),    // developmental stage ID
                    new StrNotNullOrEmpty(),    // developmental stage name
                    new IsElementOf(expressionValues),   // Expression
                    new IsElementOf(resumeQualities) }); // Call quality
        }

        return log.exit(new CellProcessor[] {
                new StrNotNullOrEmpty(),                // gene ID
                new NotNull(),                          // gene Name
                new StrNotNullOrEmpty(),                // anatomical entity ID
                new StrNotNullOrEmpty(),                // anatomical entity name
                new StrNotNullOrEmpty(),                // developmental stage ID
                new StrNotNullOrEmpty(),                // developmental stage name
                new IsElementOf(expressionValues),      // Expression
                new IsElementOf(resumeQualities),       // Call quality
                new IsElementOf(originValues),          // Including observed data
                new IsElementOf(expressionValues),      // Affymetrix data
                new IsElementOf(specificTypeQualities), // Affymetrix quality
                new IsElementOf(originValues),          // Including Affymetrix data
                new IsElementOf(expressionValues),      // EST data
                new IsElementOf(specificTypeQualities), // EST quality
                new IsElementOf(originValues),          // Including EST data
                new IsElementOf(expressionValues),      // In Situ data
                new IsElementOf(specificTypeQualities), // In Situ quality
                new IsElementOf(originValues),          // Including in Situ data
                // TODO: when relaxed in situ will be in the database, uncomment following line
                // new IsElementOf(dataElements),        // Relaxed in Situ data
                // new IsElementOf(qualityValues),       // Relaxed in Situ quality
                // new IsElementOf(originValues),        // Including relaxed in Situ data
                new IsElementOf(expressionValues),      // RNA-seq data
                new IsElementOf(specificTypeQualities), // RNA-seq quality
                new IsElementOf(originValues)});        // Including RNA-seq data
    }

    /**
     * Generates an {@code Array} of {@code String}s used to generate the header of an
     * expression TSV file of type {@code fileType}.
     * 
     * @param fileType The {@code ExprFileType} of the file to be generated.
     * @return An {@code Array} of {@code String}s used to produce the header.
     * @throw IllegalArgumentException If {@code fileType} is not managed by this method.
     */
    private String[] generateExprFileHeader(SingleSpExprFileType fileType)
        throws IllegalArgumentException {
        log.entry(fileType);

        if (fileType.isSimpleFileType()) {
            return log.exit(new String[] { 
                    GENE_ID_COLUMN_NAME, GENE_NAME_COLUMN_NAME,
                    ANATENTITY_ID_COLUMN_NAME, ANATENTITY_NAME_COLUMN_NAME,
                    STAGE_ID_COLUMN_NAME, STAGE_NAME_COLUMN_NAME,
                    EXPRESSION_COLUMN_NAME, QUALITY_COLUMN_NAME });
        }

        return log.exit(new String[] { 
                GENE_ID_COLUMN_NAME, GENE_NAME_COLUMN_NAME,
                ANATENTITY_ID_COLUMN_NAME, ANATENTITY_NAME_COLUMN_NAME,
                STAGE_ID_COLUMN_NAME, STAGE_NAME_COLUMN_NAME, 
                EXPRESSION_COLUMN_NAME, QUALITY_COLUMN_NAME, INCLUDING_OBSERVED_DATA_COLUMN_NAME, 
                AFFYMETRIX_DATA_COLUMN_NAME, AFFYMETRIX_CALL_QUALITY_COLUMN_NAME, 
                AFFYMETRIX_OBSERVED_DATA_COLUMN_NAME,
                EST_DATA_COLUMN_NAME, EST_CALL_QUALITY_COLUMN_NAME, EST_OBSERVED_DATA_COLUMN_NAME,
                INSITU_DATA_COLUMN_NAME, INSITU_CALL_QUALITY_COLUMN_NAME, 
                INSITU_OBSERVED_DATA_COLUMN_NAME,
                // TODO: when relaxed in situ will be in the database, uncomment following line
                // RELAXED_INSITU_DATA_COLUMN_NAME, RELAXED_INSITU_DATA_COLUMN_NAME,
                // RELAXED_INSITU_OBSERVED_DATA_COLUMN_NAME,
                RNASEQ_DATA_COLUMN_NAME, RNASEQ_CALL_QUALITY_COLUMN_NAME, 
                RNASEQ_OBSERVED_DATA_COLUMN_NAME });
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
     * Generate a row to be written in an expression download file. This methods will
     * notably use {@code callTOs} to produce expression information, that is different
     * depending on {@code fileType}. The results are returned as a {@code Map}; it can be
     * {@code null} if the {@code callTOs} provided do not allow to generate information
     * to be included in the file of the given {@code ExprFileType}.
     * <p>
     * {@code callTOs} must all have the same values returned by
     * {@link CallTO#getGeneId()}, {@link CallTO#getAnatEntityId()},
     * {@link CallTO#getStageId()}, and they must be respectively equal to {@code geneId},
     * {@code anatEntityId}, {@code stageId}.
     * <p>
     * <ul>
     * <li>information that will be generated in any case: entries with keys equal to
     * {@link #GENE_ID_COLUMN_NAME}, {@link #GENE_NAME_COLUMN_NAME},
     * {@link #STAGE_ID_COLUMN_NAME}, {@link #STAGE_NAME_COLUMN_NAME},
     * {@link #ANATENTITY_ID_COLUMN_NAME}, {@link #ANATENTITY_NAME_COLUMN_NAME},
     * {@link #EXPRESSION_COLUMN_NAME}.
     * <li>information generated for files of the type {@link SingleSpExprFileType
     * EXPR_COMPLETE}: entries with keys equal to {@link #AFFYMETRIX_DATA_COLUMN_NAME},
     * {@link #EST_DATA_COLUMN_NAME}, {@link #INSITU_DATA_COLUMN_NAME},
     * {@link #RELAXED_INSITU_DATA_COLUMN_NAME}, {@link #RNASEQ_DATA_COLUMN_NAME},
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
        // the current version of this method assumes that it is possible to have a mixture of 
        // global propagated calls and of basic calls to be able to add "observed data" for each 
        // data type. 
        ExpressionCallTO globalExpressionTO = null, expressionTO = null;
        NoExpressionCallTO globalNoExpressionTO = null, noExpressionTO = null;

        for (CallTO call : callTOs) {
            if (!call.getGeneId().equals(geneId) || 
                    !call.getAnatEntityId().equals(anatEntityId) || 
                    !call.getStageId().equals(stageId)) {
                throw log.throwing(new IllegalArgumentException("Incorrect correspondances " +
                        "between calls and IDs provided, for call: " + call));
            }
            if (call instanceof ExpressionCallTO) {
                ExpressionCallTO currentCallTO = (ExpressionCallTO) call;
                if (isGlobal(currentCallTO)) {
                    if (globalExpressionTO == null) {
                        globalExpressionTO = currentCallTO;
                    } else {
                        throw log.throwing(new IllegalArgumentException("The provided CallTO list(" + 
                                call.getClass() + ") contains several global expression calls"));
                    }
                } else {
                    if (expressionTO == null) {
                        expressionTO = currentCallTO;
                    } else {
                        throw log.throwing(new IllegalArgumentException("The provided CallTO list(" + 
                                call.getClass() + ") contains several basic expression calls"));
                    }
                }
            } else if (call instanceof NoExpressionCallTO) {
                NoExpressionCallTO currentCallTO = (NoExpressionCallTO) call;
                if (isGlobal(currentCallTO)) {
                    if (globalNoExpressionTO == null) {
                        globalNoExpressionTO = currentCallTO;
                    } else {
                        throw log.throwing(new IllegalArgumentException("The provided CallTO list(" + 
                                call.getClass() + ") contains several global no-expression calls"));
                    }
                } else {
                    if (noExpressionTO == null) {
                        noExpressionTO = currentCallTO;
                    } else {
                        throw log.throwing(new IllegalArgumentException("The provided CallTO list(" + 
                                call.getClass() + ") contains several basic no-expression calls"));
                    }
                }
            } else {
                throw log.throwing(new IllegalArgumentException("The CallTO provided (" +
                        call.getClass() + ") is not managed for expression/no-expression data: " + 
                        call));
            }
        }

        if (globalExpressionTO == null && globalNoExpressionTO == null) {
            throw log.throwing(new IllegalArgumentException("No global call " +
                                "for the triplet gene (" + geneId + 
                                ") - organ (" + anatEntityId + 
                                ") - stage (" + stageId + ")"));
        }
        if (globalExpressionTO != null) {
            if (isCallWithNoData(globalExpressionTO)) {
                throw log.throwing(new IllegalArgumentException("All data states of " + 
                        "the expression call (" + globalExpressionTO + ") are set to no data"));
            }
            // sanity check, we need to be able to determine the origin of the data latter 
            // using isPropagatedOnly()
            if (globalExpressionTO.isObservedData() == null) {
                throw log.throwing(new IllegalArgumentException("An ExpressionCallTO " + 
                        "does not allow to determine origin of the data: " + globalExpressionTO));
            }
        }
        if (globalNoExpressionTO != null) {
            if (isCallWithNoData(globalNoExpressionTO)) {
                throw log.throwing(new IllegalArgumentException("All data states of " + 
                        "the no-expression call (" + globalNoExpressionTO + ") are set to no data"));
            }
            // sanity check, we need to be able to determine the origin of the data latter 
            // using isPropagatedOnly()
            if (globalNoExpressionTO.getOriginOfLine() == null) {
                throw log.throwing(new IllegalArgumentException("A NoExpressionCallTO " +
                        "does not allow to determine origin of the data: " + globalNoExpressionTO));
            }
        }
        
        // Define if the call include observed data
        ObservedData observedData = ObservedData.NOT_OBSERVED;
        if ((globalExpressionTO != null && !isPropagatedOnly(globalExpressionTO)) || 
                (globalNoExpressionTO != null && !isPropagatedOnly(globalNoExpressionTO))) {
            // stage and anatomical entity not propagated in the expression call
            // OR anatomical entity not propagated in the no-expression call
            observedData = ObservedData.OBSERVED;
        }

        // We do not write all calls in simple file: 
        // - if OBSERVED_DATA_ONLY is true:  NOT_OBSERVED calls are not written 
        //                                   (stage and organ not observed)
        // - if OBSERVED_DATA_ONLY is false: NOT_OBSERVED calls without observed organ are not written.
        if (fileType.isSimpleFileType() && observedData.equals(ObservedData.NOT_OBSERVED)) {
            if (this.observedDataOnly) {
                return log.exit(null);                
            } else if ((globalExpressionTO != null && 
                            globalExpressionTO.getAnatOriginOfLine().equals(OriginOfLine.DESCENT)) ||
                       (globalNoExpressionTO != null && isPropagatedOnly(globalNoExpressionTO))) {
                log.debug("globalExpressionTO:{}", globalExpressionTO);
                return log.exit(null);
            }
        }

        // Define summary column
        ExpressionData summary = ExpressionData.NO_DATA;
        String callQuality = GenerateDownloadFile.NA_VALUE;
        if (globalExpressionTO != null && globalNoExpressionTO != null) {
            if (globalNoExpressionTO.getOriginOfLine().equals(NoExpressionCallTO.OriginOfLine.PARENT)) {
                summary = ExpressionData.LOW_AMBIGUITY;
            } else {
                summary = ExpressionData.HIGH_AMBIGUITY;
            }
        } else if (globalExpressionTO != null) {
            Set<DataState> allDataState = EnumSet.of(
                    globalExpressionTO.getAffymetrixData(), globalExpressionTO.getESTData(), 
                    globalExpressionTO.getInSituData(), globalExpressionTO.getRNASeqData());

            if (allDataState.contains(DataState.HIGHQUALITY)) {
                callQuality = DataState.HIGHQUALITY.getStringRepresentation();
                // summary = ExpressionData.HIGH_QUALITY;
            } else {
                callQuality = DataState.LOWQUALITY.getStringRepresentation();
                // summary = ExpressionData.LOW_QUALITY;
            }
            summary = ExpressionData.EXPRESSION;
        } else if (globalNoExpressionTO != null) {
            summary = ExpressionData.NO_EXPRESSION;
            callQuality = DataState.HIGHQUALITY.getStringRepresentation();
        }
        row.put(EXPRESSION_COLUMN_NAME, summary.getStringRepresentation());
        row.put(QUALITY_COLUMN_NAME, callQuality);

        // following columns are generated only for complete files
        if (fileType.isSimpleFileType()) {
            return log.exit(row);
        }

        // ********************************
        // Set complete file columns
        // ********************************
        // Write if the call include observed data
        row.put(INCLUDING_OBSERVED_DATA_COLUMN_NAME, observedData.getStringRepresentation());

        // Define data state for each data types
        if(globalExpressionTO == null) {
            globalExpressionTO = new ExpressionCallTO(null, null, null, null, 
                    DataState.NODATA, DataState.NODATA, DataState.NODATA, DataState.NODATA, 
                    null, null, null, null, null);
        }
        if (globalNoExpressionTO == null) {
            globalNoExpressionTO = new NoExpressionCallTO(null, null, null, null,
                    DataState.NODATA, DataState.NODATA, DataState.NODATA, DataState.NODATA,
                    null, null);
        }

        // Define data state for each data type
        try {
            String[] affyData = this.mergeExprAndNoExprDataStates(
                globalExpressionTO.getAffymetrixData(), globalNoExpressionTO.getAffymetrixData());
            row.put(AFFYMETRIX_DATA_COLUMN_NAME, affyData[0]);
            row.put(AFFYMETRIX_CALL_QUALITY_COLUMN_NAME, affyData[1]);
            if (noExpressionTO != null && !noExpressionTO.getAffymetrixData().equals(DataState.NODATA) ||
                    expressionTO != null && !expressionTO.getAffymetrixData().equals(DataState.NODATA)) {
                row.put(AFFYMETRIX_OBSERVED_DATA_COLUMN_NAME, ObservedData.OBSERVED.getStringRepresentation());
            } else {
                row.put(AFFYMETRIX_OBSERVED_DATA_COLUMN_NAME, ObservedData.NOT_OBSERVED.getStringRepresentation());
            }

            String[] estData = this.mergeExprAndNoExprDataStates(
                globalExpressionTO.getESTData(), DataState.NODATA);
            row.put(EST_DATA_COLUMN_NAME, estData[0]);
            row.put(EST_CALL_QUALITY_COLUMN_NAME, estData[1]);
            if (expressionTO != null && !expressionTO.getESTData().equals(DataState.NODATA)) {
                row.put(EST_OBSERVED_DATA_COLUMN_NAME, ObservedData.OBSERVED.getStringRepresentation());
            } else {
                row.put(EST_OBSERVED_DATA_COLUMN_NAME, ObservedData.NOT_OBSERVED.getStringRepresentation());
            }

            String[] inSituData = this.mergeExprAndNoExprDataStates(
                globalExpressionTO.getInSituData(), globalNoExpressionTO.getInSituData());
            row.put(INSITU_DATA_COLUMN_NAME, inSituData[0]);
            row.put(INSITU_CALL_QUALITY_COLUMN_NAME, inSituData[1]);
            if (noExpressionTO != null && !noExpressionTO.getInSituData().equals(DataState.NODATA) ||
                    expressionTO != null && !expressionTO.getInSituData().equals(DataState.NODATA)) {
                row.put(INSITU_OBSERVED_DATA_COLUMN_NAME, ObservedData.OBSERVED.getStringRepresentation());
            } else {
                row.put(INSITU_OBSERVED_DATA_COLUMN_NAME, ObservedData.NOT_OBSERVED.getStringRepresentation());
            }

            // TODO: when relaxed in situ will be in the database, uncomment following line
            // String[] relaxedInSituData = this.mergeExprAndNoExprDataStates(
            //      DataState.NODATA, noExpressionTO.getRelaxedInSituData());
            // row.put(RELAXED_INSITU_DATA_COLUMN_NAME, relaxedInSituData[0]);
            // row.put(RELAXED_INSITU_CALL_QUALITY_COLUMN_NAME, relaxedInSituData[1]);
            // if (noExpressionTO != null && !noExpressionTO.getRelaxedInSituData().equals(DataState.NODATA)) {
            //  row.put(RELAXED_INSITU_OBSERVED_DATA_COLUMN_NAME, ObservedData.OBSERVED);
            // } else {
            //     row.put(RELAXED_INSITU_OBSERVED_DATA_COLUMN_NAME, ObservedData.NOT_OBSERVED);
            // }

            String[] rnaSeqData = this.mergeExprAndNoExprDataStates(
                globalExpressionTO.getRNASeqData(), globalNoExpressionTO.getRNASeqData());
            row.put(RNASEQ_DATA_COLUMN_NAME, rnaSeqData[0]);
            row.put(RNASEQ_CALL_QUALITY_COLUMN_NAME, rnaSeqData[1]);
            if (noExpressionTO != null && !noExpressionTO.getRNASeqData().equals(DataState.NODATA) ||
                    expressionTO != null && !expressionTO.getRNASeqData().equals(DataState.NODATA)) {
                row.put(RNASEQ_OBSERVED_DATA_COLUMN_NAME, ObservedData.OBSERVED.getStringRepresentation());
            } else {
                row.put(RNASEQ_OBSERVED_DATA_COLUMN_NAME, ObservedData.NOT_OBSERVED.getStringRepresentation());
            }

        } catch (Exception e) {
            throw log.throwing(new IllegalArgumentException("Incorrect data states, " +
                "ExpressionCallTO: " + globalExpressionTO + ", NoExpressionCallTo: " + globalNoExpressionTO, e));
        }

        return log.exit(row);
    }

    /**
     * Merge {@code DataState}s of one expression call and one no-expression call into an
     * {@code ExpressionData}.
     * 
     * @param dataStateExpr     A {@code DataState} from an expression call.
     * @param dataStateNoExpr   A {@code DataState} from a no-expression call.
     * @return                  An {@code ExpressionData} combining {@code DataState}s of one 
     *                          expression call and one no-expression call.
     * @throws IllegalStateException If an expression call and a no-expression call are
     *             found for the same data type.
     */
    private String[] mergeExprAndNoExprDataStates(DataState dataStateExpr, DataState dataStateNoExpr) 
            throws IllegalStateException {
        log.entry(dataStateExpr, dataStateNoExpr);

        String[] data = new String[2];
        data[0] = ExpressionData.NO_DATA.getStringRepresentation();
        data[1] = DataState.NODATA.getStringRepresentation();

        // no data at all
        if (dataStateExpr == DataState.NODATA && dataStateNoExpr == DataState.NODATA) {
            return log.exit(data);
        }
        if (dataStateExpr != DataState.NODATA && dataStateNoExpr != DataState.NODATA) {
            throw log.throwing(new IllegalStateException("An expression call and " +
                    "a no-expression call could be found for the same data type."));
        }
        // no no-expression data, we use the expression data
        if (dataStateExpr != DataState.NODATA) {
            data[0] = ExpressionData.EXPRESSION.getStringRepresentation();
            if (dataStateExpr.equals(DataState.HIGHQUALITY)) {
                data[1] = DataState.HIGHQUALITY.getStringRepresentation();
                return log.exit(data);
            }
            if (dataStateExpr.equals(DataState.LOWQUALITY)) {
                data[1] = DataState.LOWQUALITY.getStringRepresentation();
                return log.exit(data);
            }
            throw log.throwing(new IllegalArgumentException("The DataState provided (" + 
                    dataStateExpr.getStringRepresentation() + ") is not supported"));
        }

        if (dataStateNoExpr != DataState.NODATA) {
            // no-expression data available
            data[0] = ExpressionData.NO_EXPRESSION.getStringRepresentation();
            data[1] = DataState.HIGHQUALITY.getStringRepresentation();
            return log.exit(data);
        }

        throw log.throwing(new AssertionError("All logical conditions should have been checked."));
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
     * @param allCallTOs            A {@code Set} of {@code CallTO}s that are calls to be written. 
     * @throws IOException  If an error occurred while trying to write the {@code outputFile}.
     */
    private void writeExprRows(Map<String, String> geneNamesByIds, 
            Map<String, String> stageNamesByIds, Map<String, String> anatEntityNamesByIds, 
            Map<FileType, ICsvMapWriter> writersUsed, 
            Map<FileType, CellProcessor[]> processors, 
            Map<FileType, String[]> headers, String geneId, Collection<CallTO> allCallTOs) 
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

            for (Entry<FileType, ICsvMapWriter> writerFileType : writersUsed.entrySet()) {
                Map<String, String> row = null;
                try {
                    row = this.generateExprRow(geneId, geneNamesByIds.get(geneId),
                        stageId, stageNamesByIds.get(stageId), anatEntityId,
                        anatEntityNamesByIds.get(anatEntityId), callGroup.getValue(),
                        writerFileType.getKey());
                } catch (IllegalArgumentException e) {
                    // any IllegalArgumentException thrown by generateExprRow should come
                    // from a problem in the data, thus from an illegal state
                    throw log.throwing(new IllegalStateException("Incorrect data state", e));
                }

                if (row != null) {
                    log.trace("Write row: {} - using writer: {}", row, writerFileType.getValue());
                    writerFileType.getValue().write(row, headers.get(writerFileType.getKey()),
                        processors.get(writerFileType.getKey()));
                }
            }
        }
        log.exit();
    }
}
