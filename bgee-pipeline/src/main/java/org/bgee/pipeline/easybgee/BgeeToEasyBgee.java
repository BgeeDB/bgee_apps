package org.bgee.pipeline.easybgee;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.ComposedEntity;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.Sex;
import org.bgee.model.anatdev.Strain;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO;
import org.bgee.model.dao.api.anatdev.StageDAO;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO.ConditionTO;
import org.bgee.model.dao.api.expressiondata.call.DAOConditionFilter;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneXRefDAO.GeneXRefTOResultSet;
import org.bgee.model.dao.api.source.SourceDAO.SourceTO;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTOResultSet;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;
import org.bgee.model.expressiondata.baseelements.PropagationState;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.expressiondata.call.CallFilter.ExpressionCallFilter2;
import org.bgee.model.expressiondata.call.CallServiceParent;
import org.bgee.model.expressiondata.call.Condition2;
import org.bgee.model.expressiondata.call.ExpressionCallLoader;
import org.bgee.model.expressiondata.call.ExpressionCallProcessedFilter;
import org.bgee.model.expressiondata.call.ExpressionCallService;
import org.bgee.model.expressiondata.call.OTFExpressionCall;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.bgee.pipeline.CommandRunner;
import org.bgee.pipeline.MySQLDAOUser;
import org.bgee.pipeline.Utils;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.io.ICsvMapWriter;

/**
 * Extract data from the Bgee database and generate one TSV file for each
 * extracted table. These TSV files will then be used to populate the Easy Bgee
 * database (initially created for the bioSoda project)
 *
 * @author  Julien Wollbrett
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Dec. 2019
 * @since   Bgee 14, July 2018
 */
// TODO: add tests
public class BgeeToEasyBgee extends MySQLDAOUser{

    /**
     * Each entry of this enum corresponds to the export of one bgee table that
     * have to be integrated into the Easy Bgee database. Each entry contains 5
     * information :
     * <ol>
     * <li>name of the file containing all data,</li>
     * <li>name of the table in Easy Bgee,</li>
     * <li>mapping between the name of the columns in the file and the name of
     * the columns in the Easy Bgee database,</li>
     * <li>mapping between the name of the columns in the file and sql type of
     * the column in the Easy Bgee database,</li>
     * <li>mapping between the name of the columns in the file and the fact the
     * column is nullable. true means nullable, false means not nullable, and
     * null means not nullable with default value ''</li>
     * </ol>
     */
    @SuppressWarnings("serial")
    private enum TsvFile {
        SPECIES_OUTPUT_FILE("species_easy_bgee.tsv", "species", new LinkedHashMap<String, String>() {
            {
                put("ID", "speciesId");
                put("GENUS", "genus");
                put("SPECIES_NAME", "species");
                put("COMMON_NAME", "speciesCommonName");
                put("GENOME_VERSION", "genomeVersion");
                put("GENOME_ASSEMBLY_XREF", "genomeAssemblyXRef");
                put("GENOME_SPECIES_ID", "genomeSpeciesId");
            }
        }, new LinkedHashMap<String, Integer>() {
            {
                put("ID", Types.INTEGER);
                put("GENUS", Types.VARCHAR);
                put("SPECIES_NAME", Types.VARCHAR);
                put("COMMON_NAME", Types.VARCHAR);
                put("GENOME_VERSION", Types.VARCHAR);
                put("GENOME_ASSEMBLY_XREF", Types.VARCHAR);
                put("GENOME_SPECIES_ID", Types.INTEGER);
            }
        }, new LinkedHashMap<String, Boolean>() {
            {
                put("ID", false);
                put("GENUS", false);
                put("SPECIES_NAME", false);
                put("COMMON_NAME", true);
                put("GENOME_VERSION", false);
                put("GENOME_ASSEMBLY_XREF", false);
                put("GENOME_SPECIES_ID", false);
            }
        }), GENE_OUTPUT_FILE("genes_easy_bgee.tsv", "gene", new LinkedHashMap<String, String>() {
            {
                put("ID", "bgeeGeneId");
                put("GENE_ID", "geneId");
                put("NAME", "geneName");
                put("DESCRIPTION", "geneDescription");
                put("SPECIES_ID", "speciesId");
            }
        }, new LinkedHashMap<String, Integer>() {
            {
                put("ID", Types.INTEGER);
                put("GENE_ID", Types.VARCHAR);
                put("NAME", Types.VARCHAR);
                put("DESCRIPTION", Types.VARCHAR);
                put("SPECIES_ID", Types.INTEGER);
            }
        }, new LinkedHashMap<String, Boolean>() {
            {
                put("ID", false);
                put("GENE_ID", false);
                // geneName is defined in the database as not null with default
                // value = ''
                // to be compatible with the superCSV cellProcessor, isNullable
                // is defined to null
                put("NAME", null);
                put("DESCRIPTION", true);
                put("SPECIES_ID", false);
            }
        }), GENE_XREF_OUTPUT_FILE("gene_xrefs_easy_bgee.tsv", "geneXRef", new LinkedHashMap<String, String>() {
            {
                put("BGEE_GENE_ID", "bgeeGeneId");
                put("XREF_URL", "XRefUrl");
                put("DATASOURCE_NAME", "dataSourceName");
            }
        }, new LinkedHashMap<String, Integer>() {
            {
                put("BGEE_GENE_ID", Types.INTEGER);
                put("XREF_URL", Types.VARCHAR);
                put("DATASOURCE_NAME", Types.VARCHAR);
            }
        }, new LinkedHashMap<String, Boolean>() {
            {
                put("BGEE_GENE_ID", false);
                put("XREF_URL", null);
                put("DATASOURCE_NAME", false);
            }
        }), ANATENTITY_OUTPUT_FILE("anat_entities_easy_bgee.tsv", "anatEntity", new LinkedHashMap<String, String>() {
            {
                put("ID", "anatEntityId");
                put("NAME", "anatEntityName");
                put("DESCRIPTION", "anatEntityDescription");
            }
        }, new LinkedHashMap<String, Integer>() {
            {
                put("ID", Types.VARCHAR);
                put("NAME", Types.VARCHAR);
                put("DESCRIPTION", Types.VARCHAR);
            }
        }, new LinkedHashMap<String, Boolean>() {
            {
                put("ID", false);
                put("NAME", false);
                put("DESCRIPTION", true);
            }
        }), DEVSTAGE_OUTPUT_FILE("dev_stages_easy_bgee.tsv", "stage", new LinkedHashMap<String, String>() {
            {
                put("ID", "stageId");
                put("NAME", "stageName");
                put("DESCRIPTION", "stageDescription");
            }
        }, new LinkedHashMap<String, Integer>() {
            {
                put("ID", Types.VARCHAR);
                put("NAME", Types.VARCHAR);
                put("DESCRIPTION", Types.VARCHAR);
            }
        }, new LinkedHashMap<String, Boolean>() {
            {
                put("ID", false);
                put("NAME", false);
                put("DESCRIPTION", true);
            }
        }), GLOBALCOND_OUTPUT_FILE("global_cond_easy_bgee.tsv", "globalCond", new LinkedHashMap<String, String>() {
            {
                put("ID", "globalConditionId");
                put("ANAT_ENTITY_ID", "anatEntityId");
                put("STAGE_ID", "stageId");
                put("CELL_TYPE_ID", "cellTypeId");
                put("SEX_ID", "sex");
                put("STRAIN_ID", "strain");
                put("SPECIES_ID", "speciesId");
            }
        }, new LinkedHashMap<String, Integer>() {
            {
                put("ID", Types.INTEGER);
                put("ANAT_ENTITY_ID", Types.VARCHAR);
                put("STAGE_ID", Types.VARCHAR);
                put("CELL_TYPE_ID", Types.VARCHAR);
                put("SEX_ID", Types.VARCHAR);
                put("STRAIN_ID", Types.VARCHAR);
                put("SPECIES_ID", Types.INTEGER);
            }
        }, new LinkedHashMap<String, Boolean>() {
            {
                put("ID", false);
                // according to the bgee schema anatEntityId could be null. In
                // reality it is never the case.
                put("ANAT_ENTITY_ID", false);
                put("STAGE_ID", false);
                put("CELL_TYPE_ID", false);
                put("SEX_ID", false);
                put("STRAIN_ID", false);
                put("SPECIES_ID", false);
            }
        }), GLOBALEXPRESSION_OUTPUT_FILE("global_expression_easy_bgee.tsv", "globalExpression",
                new LinkedHashMap<String, String>() {
                    {
                        put("BGEE_GENE_ID", "bgeeGeneId");
                        put("GLOBAL_CONDITION_ID", "globalConditionId");
                        put("SUMMARY_QUALITY", "summaryQuality");
                        put("MEAN_SCORE", "score");
                        put("FDR_PVALUE", "pValue");
                        put("ORIGIN", "propagationOrigin");
                        put("CALL_TYPE", "callType");
                    }
                }, new LinkedHashMap<String, Integer>() {
                    {
                        put("BGEE_GENE_ID", Types.INTEGER);
                        put("GLOBAL_CONDITION_ID", Types.INTEGER);
                        put("SUMMARY_QUALITY", Types.VARCHAR);
                        put("MEAN_SCORE", Types.DECIMAL);
                        put("FDR_PVALUE", Types.DECIMAL);
                        put("ORIGIN", Types.VARCHAR);
                        put("CALL_TYPE", Types.VARCHAR);
                    }
                }, new LinkedHashMap<String, Boolean>() {
                    {
                        put("BGEE_GENE_ID", false);
                        put("GLOBAL_CONDITION_ID", false);
                        put("SUMMARY_QUALITY", false);
                        put("MEAN_SCORE", false);
                        put("FDR_PVALUE", true);
                        put("ORIGIN", false);
                        put("CALL_TYPE", false);
                    }
                });

        private String fileName;
        private String tableName;
        private Map<String, String> columnName;
        private Map<String, Integer> datatypes;
        private Map<String, Boolean> isNullable;

        TsvFile(String fileName, String tableName, Map<String, String> columnName, Map<String, Integer> datatypes,
                Map<String, Boolean> isNullable) {
            this.fileName = fileName;
            this.tableName = tableName;
            this.columnName = columnName;
            this.datatypes = datatypes;
            this.isNullable = isNullable;

        }

        public String getFileName() {
            return fileName;
        }

        public String getTableName() {
            return tableName;
        }

        public Map<String, String> getColumnName() {
            return columnName;
        }

        public Map<String, Integer> getDatatypes() {
            return datatypes;
        }

        public Map<String, Boolean> getIsNullable() {
            return isNullable;
        }
    }

    // column to add in Easy Bgee for which no Enum exist in the Bgee API (because
    // information is not stored in the Bgee RDB)
    private static String GLOBAL_EXPRESSION_SUMMARY_QUALITY = "SUMMARY_QUALITY";
    private static String GLOBAL_EXPRESSION_MEAN_SCORE = "MEAN_SCORE";
    private static String GLOBAL_EXPRESSION_ORIGIN = "ORIGIN";
    private static String GLOBAL_EXPRESSION_FDR_PVALUE = "FDR_PVALUE";
    private static String GLOBAL_EXPRESSION_SUMMARY_CALL_TYPE = "CALL_TYPE";

    private final Function<DAOManager, ServiceFactory> serviceFactoryProvider;
    private final Supplier<DAOManager> daoManagerSupplier;

    private final static Logger log = LogManager.getLogger(BgeeToEasyBgee.class);

    /**
     * Several actions can be launched from this main method, depending on the
     * first element in {@code args}:
     * <ul>
     * <li>If the first element in {@code args} is "extractBgeeDatabase", the
     * action will be to export data from the Bgee database to TSV files (see
     * {@link #extractBgeeDatabase(Collection, String)}). Following elements in
     * {@code args} must then be:
     * <ol>
     * <li>path to the output directory,
     * <li>a list of NCBI species IDs (for instance, '9606' for human) that will
     * be used to extract data, separated by the {@code String}
     * {@link CommandRunner#LIST_SEPARATOR}. If empty (see
     * {@link CommandRunner#EMPTY_LIST}), all species in database will be
     * exported.
     * </ol>
     * </li>
     * <li>If the first element in {@code args} is "tsvToEasyBgee", the action
     * will be to import exported TSV files into Easy Bgee database (see
     * {@link #tsvToEasyBgee(String)}). Following elements in {@code args} must
     * then be:
     * <ol>
     * <li>path to the input directory containing all TSV files,
     * </ol>
     * </li>
     * <li>If the first element in {@code args} is "emptyDatabaseTables", the
     * action will be to deletes rows of tables of Easy Bgee database (see
     * {@link #emptyDatabaseTables()}).</li>
     * </ul>
     *
     * @param args
     *            An {@code Array} of {@code String}s containing the requested
     *            parameters.
     */
    public static void main(String[] args) {
        if (args == null || args[0] == null) {
            throw log.throwing(new IllegalArgumentException("No arguments are provided. At least one argument"
                    + "corresponding to the action to do should be provided"));
        }
        BgeeToEasyBgee bgeeToEasyBgee = new BgeeToEasyBgee();
        if (args[0].equals("extractFromBgee")) {
            int expectedArgLength = 3;
            if (args.length != expectedArgLength) {
                throw log.throwing(new IllegalArgumentException("Incorrect number of arguments provided, expected "
                        + expectedArgLength + " arguments, " + args.length + " provided."));
            }
            bgeeToEasyBgee.cleanOutputDir(args[1]);
            bgeeToEasyBgee.extractBgeeDatabase(CommandRunner.parseListArgumentAsInt(args[2]), args[1]);
        } else if (args[0].equals("tsvToEasyBgee")) {
            int expectedArgLength = 2;
            if (args.length != expectedArgLength) {
                throw log.throwing(new IllegalArgumentException("Incorrect number of arguments provided, expected "
                        + expectedArgLength + " arguments, " + args.length + " provided."));
            }
            bgeeToEasyBgee.tsvToEasyBgee(args[1]);
        } else if (args[0].equals("emptyDatabaseTables")) {
            int expectedArgLength = 1;
            if (args.length != expectedArgLength) {
                throw log.throwing(new IllegalArgumentException("Incorrect number of arguments provided, expected "
                        + expectedArgLength + " arguments, " + args.length + " provided."));
            }
            bgeeToEasyBgee.emptyDatabaseTables();
        } else {
            throw log.throwing(new IllegalArgumentException(args[0] + " is not recognized as an action"));
        }
    }

    public BgeeToEasyBgee() {
        this(DAOManager::getDAOManager, ServiceFactory::new);
    }

    public BgeeToEasyBgee(final Supplier<DAOManager> daoManagerSupplier,
            final Function<DAOManager, ServiceFactory> serviceFactoryProvider) {
        this.daoManagerSupplier = daoManagerSupplier;
        this.serviceFactoryProvider = serviceFactoryProvider;

    }

    /**
     * Clean the output directory. If output ".tsv" files exist they are
     * deleted. This Method also create the output directory if it does not
     * already exist.
     *
     * @param directory to the output directory to cleanpath
     */
    private void cleanOutputDir(String directory) {
        log.traceEntry("{}", directory);
        File dir = new File(directory);
        dir.mkdir();
        for (TsvFile fileName : TsvFile.values()) {
            File file = new File(directory, fileName.fileName);
            if (file.exists()) {
                file.delete();
            }
        }
        log.traceExit();
    }

    /**
     * Extract data from the bgee database to intermediate TSV files
     *
     * @param inputSpeciesIds
     *            A {@code Collection} of {@code Integer}s that are IDs of
     *            species for which to generate files.
     * @param directory
     *            A {@code String} that is the directory where to store files.
     */
    private void extractBgeeDatabase(Collection<Integer> inputSpeciesIds, String directory) {
        log.traceEntry("{}, {}", inputSpeciesIds, directory);
        SpeciesTOResultSet speciesTOs = daoManagerSupplier.get().getSpeciesDAO()
                .getSpeciesByIds(new HashSet<>(inputSpeciesIds), null);
        // XXX: add check that all provided species IDs are found
        Set<Integer> speciesIds = extractSpeciesTable(speciesTOs, directory);
        extractAnatEntityTable(directory);
        extractStageTable(directory);
        extractGeneXRefTable(directory);
        for (Integer speciesId : speciesIds) {
            log.info("start to extract genes, conditions and expressions data for species {}", speciesId);
            // Note: we can map ID to one Bgee gene ID because we use
            // data for only 1 species
            Map<String, Integer> idToBgeeGeneId = extractGeneTable(speciesId, directory);
            Map<String, String> condKeyToConditionId = extractGlobalCondTable(speciesId, directory);
            extractGlobalExpressionTable(idToBgeeGeneId, condKeyToConditionId, speciesId, directory);
        }
        log.traceExit();
    }

    private void extractGlobalExpressionTable(Map<String, Integer> idToBgeeGeneIds,
            Map<String, String> condKeyToConditionId, Integer speciesId, String directory) {
        log.traceEntry("{}, {}, {}, {}",idToBgeeGeneIds, condKeyToConditionId, speciesId, directory);

        log.info("Start extracting global expressions for the species {}...", speciesId);

        // use TsvFile enum to generate the CellProcessor
        final CellProcessor[] processors = createCellProcessor(TsvFile.GLOBALEXPRESSION_OUTPUT_FILE);

        String[] header = new String[] { "BGEE_GENE_ID", "GLOBAL_CONDITION_ID", GLOBAL_EXPRESSION_SUMMARY_QUALITY,
                GLOBAL_EXPRESSION_MEAN_SCORE, GLOBAL_EXPRESSION_FDR_PVALUE,
                GLOBAL_EXPRESSION_ORIGIN, GLOBAL_EXPRESSION_SUMMARY_CALL_TYPE };

        // init summaryCallTypeQualityFilter: only export calls of at least SILVER quality.
        // With OTF propagation this is applied by ExpressionCallLoader itself as a
        // post-propagation filter (see ExpressionCallLoader#matchesRequestedSummaryCallType),
        // so we don't need to separately filter the results below.
        Map<ExpressionSummary, SummaryQuality> summaryCallTypeQualityFilter = new HashMap<>();
        summaryCallTypeQualityFilter.put(ExpressionSummary.EXPRESSED, SummaryQuality.SILVER);
        summaryCallTypeQualityFilter.put(ExpressionSummary.NOT_EXPRESSED, SummaryQuality.SILVER);

        // Unlike the previous implementation, which explicitly restricted results to the "root"
        // cell type, sex and strain (Collections.singleton("GO:0005575")/"any"/"wild-type" in
        // the old ConditionFilter), the new condition parameter model merges anat. entity and
        // cell type into a single ConditionParameter (ANAT_ENTITY_CELL_TYPE), so it is no longer
        // possible to request "anat. entity without cell type" as a condition parameter
        // combination. As implemented below, calls for specific cell types are now also
        // exported (not just whole-organ calls) -- confirmed intentional: cell types are
        // important data and should be part of EasyBgee.
        Collection<ConditionParameter<?, ?>> condParamCombination =
                List.of(ConditionParameter.ANAT_ENTITY_CELL_TYPE, ConditionParameter.DEV_STAGE);

        File file = new File(directory, TsvFile.GLOBALEXPRESSION_OUTPUT_FILE.getFileName());

        try {
            boolean writeHeader = false;
            if (!file.exists()) {
                writeHeader = true;
            }
            try (ICsvMapWriter mapWriter = new CsvMapWriter(new FileWriter(file, true), Utils.TSVCOMMENTED)) {
                if(writeHeader) {
                    file.createNewFile();
                    mapWriter.writeHeader(header);
                }

                idToBgeeGeneIds.keySet()
                .parallelStream().forEach(geneId -> {
                    ExpressionCallService callService = serviceFactoryProvider
                            .apply(this.daoManagerSupplier.get())
                            .getExpressionCallService();
                    ExpressionCallFilter2 filter = new ExpressionCallFilter2(summaryCallTypeQualityFilter,
                            new GeneFilter(speciesId, geneId), null, null,
                            condParamCombination, null, null, false);
                    ExpressionCallProcessedFilter processedFilter =
                            callService.processExpressionCallFilter(filter);
                    ExpressionCallLoader loader = callService.getCallLoader(processedFilter);
                    Map<Gene, List<OTFExpressionCall>> callsByGene = loader.loadDataOnTheFly();
                    Stream<OTFExpressionCall> expressedCalls = callsByGene.values().stream()
                            .flatMap(List::stream);
                    generateGlobalExpressionLines(expressedCalls,
                            idToBgeeGeneIds, condKeyToConditionId, header, processors, mapWriter, file);
                });

            }
        } catch (IOException e) {
            throw log.throwing(new UncheckedIOException("Can't write file " + file, e));
        }
        log.traceExit();
    }

    private void generateGlobalExpressionLines(Stream<OTFExpressionCall> expressionCalls,
            Map<String, Integer> geneToBgeeGeneId, Map<String, String> condKeyToConditionId,
            String[] header, CellProcessor[] processors, ICsvMapWriter mapWriter, File file) {
        log.traceEntry("{}, {}, {}, {}, {}, {}, {}", expressionCalls, geneToBgeeGeneId,
                condKeyToConditionId, header, processors, mapWriter, file);

        List<Map<String, String>> headerToValuePerGene = expressionCalls.map(call -> {
            Map<String, String> headerToValuePerCall = new HashMap<>();
            headerToValuePerCall.put("BGEE_GENE_ID",
                    String.valueOf(geneToBgeeGeneId.get(call.getGene().getGeneId())));

            String condKey = buildConditionKeyFromCondition2(call.getCondition());
            String conditionId = condKeyToConditionId.get(condKey);
            if (conditionId == null) {
                // Expected: OTF propagation runs over the whole condition graph, while
                // extractGlobalCondTable only exports a subset of it (notably only "meta"
                // UBERON: stages). Calls in a non-exported condition have no global condition
                // to point to and are skipped.
                return null;
            }
            headerToValuePerCall.put("GLOBAL_CONDITION_ID", conditionId);

            Map.Entry<ExpressionSummary, SummaryQuality> callQual = inferOTFSummaryCallTypeAndQuality(call);
            headerToValuePerCall.put(GLOBAL_EXPRESSION_SUMMARY_QUALITY,
                    callQual.getValue().getStringRepresentation());
            headerToValuePerCall.put(GLOBAL_EXPRESSION_SUMMARY_CALL_TYPE,
                    callQual.getKey().getStringRepresentation());

            headerToValuePerCall.put(GLOBAL_EXPRESSION_MEAN_SCORE,
                    call.getExpressionScore() == null? null: call.getExpressionScore().toString());
            headerToValuePerCall.put(GLOBAL_EXPRESSION_ORIGIN,
                    dataPropagationToString(call.getDataPropagation()));
            headerToValuePerCall.put(GLOBAL_EXPRESSION_FDR_PVALUE,
                    call.getAllDataTypePValue() == null? null: call.getAllDataTypePValue().toString());
            return headerToValuePerCall;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        try {
            writeExpressionPerGeneToFile(headerToValuePerGene, header, processors, mapWriter);
        } catch (IOException e) {
            throw log.throwing(new UncheckedIOException("Can't write file " + file, e));
        }
    }

    /**
     * Port, adapted to {@code OTFExpressionCall}, of the inference logic previously implemented
     * in {@code CallService.inferSummaryCallTypeAndQuality(Set, Set, Set)}: same thresholds
     * ({@link CallServiceParent#PRESENT_HIGH_LESS_THAN_OR_EQUALS_TO} etc.), same GOLD/SILVER/
     * BRONZE cascade, but operating directly on the single "all requested data types" and
     * "trusted data types" p-values already carried by {@code OTFExpressionCall}, instead of a
     * {@code Set<FDRPValue>} per data type combination (OTF does not currently precompute a
     * value per combination the way the old pipeline did).
     * TODO: this exists here only because {@code ExpressionCallLoader} currently only exposes a
     * "does this call match a REQUESTED tier" predicate ({@code matchesRequestedSummaryCallType}),
     * not a "what is the actual tier of this call" method. Consider promoting this to bgee-core
     * (e.g. next to {@code OTFExpressionCallFilterEngine}) so other callers do not have to
     * duplicate it.
     */
    private static Map.Entry<ExpressionSummary, SummaryQuality> inferOTFSummaryCallTypeAndQuality(
            OTFExpressionCall call) {
        log.traceEntry("{}", call);

        BigDecimal allPValue = call.getAllDataTypePValue();
        BigDecimal trustedPValue = call.getTrustedDataTypePValue();
        BigDecimal bestDescAllPValue = call.getBestDirectDescendantAllDataTypePValue();
        BigDecimal bestDescTrustedPValue = call.getBestDirectDescendantTrustedDataTypePValue();

        if (allPValue == null) {
            throw log.throwing(new IllegalStateException(
                    "Could not infer ExpressionSummary/SummaryQuality, no p-value available for "
                    + call));
        }

        //The order of the comparisons is important, mirrors the old CallService logic.
        if (allPValue.compareTo(CallServiceParent.PRESENT_HIGH_LESS_THAN_OR_EQUALS_TO) <= 0) {
            return log.traceExit(new AbstractMap.SimpleEntry<>(ExpressionSummary.EXPRESSED, SummaryQuality.GOLD));
        }
        if (allPValue.compareTo(CallServiceParent.PRESENT_LOW_LESS_THAN_OR_EQUALS_TO) <= 0) {
            return log.traceExit(new AbstractMap.SimpleEntry<>(ExpressionSummary.EXPRESSED, SummaryQuality.SILVER));
        }
        if (bestDescAllPValue != null &&
                bestDescAllPValue.compareTo(CallServiceParent.PRESENT_LOW_LESS_THAN_OR_EQUALS_TO) <= 0) {
            return log.traceExit(new AbstractMap.SimpleEntry<>(ExpressionSummary.EXPRESSED, SummaryQuality.BRONZE));
        }
        //From here, allPValue is necessarily > PRESENT_LOW_LESS_THAN_OR_EQUALS_TO
        //(= ABSENT_LOW_GREATER_THAN), so we are considering a NOT_EXPRESSED call.
        boolean absCallCannotBeBetterThanBronze = trustedPValue == null ||
                (bestDescTrustedPValue != null && bestDescTrustedPValue
                        .compareTo(CallServiceParent.PRESENT_LOW_LESS_THAN_OR_EQUALS_TO) <= 0);
        if (trustedPValue != null &&
                allPValue.compareTo(CallServiceParent.ABSENT_HIGH_GREATER_THAN) > 0 &&
                trustedPValue.compareTo(CallServiceParent.ABSENT_HIGH_GREATER_THAN) > 0) {
            return log.traceExit(new AbstractMap.SimpleEntry<>(ExpressionSummary.NOT_EXPRESSED,
                    absCallCannotBeBetterThanBronze? SummaryQuality.BRONZE: SummaryQuality.GOLD));
        }
        if (allPValue.compareTo(CallServiceParent.ABSENT_LOW_GREATER_THAN) > 0) {
            if (trustedPValue != null &&
                    trustedPValue.compareTo(CallServiceParent.ABSENT_LOW_GREATER_THAN) > 0) {
                return log.traceExit(new AbstractMap.SimpleEntry<>(ExpressionSummary.NOT_EXPRESSED,
                        absCallCannotBeBetterThanBronze? SummaryQuality.BRONZE: SummaryQuality.SILVER));
            }
            return log.traceExit(new AbstractMap.SimpleEntry<>(ExpressionSummary.NOT_EXPRESSED, SummaryQuality.BRONZE));
        }
        throw log.throwing(new IllegalStateException(
                "Could not infer ExpressionSummary/SummaryQuality for " + call));
    }

    /**
     * Synchronized method taking care of writing in a thread-safe approach the expression information
     * retrieved from the database into a unique file.
     */
    private synchronized void writeExpressionPerGeneToFile(List<Map<String,String>> headerToValuePerGene,
            String [] header, CellProcessor[] processors, ICsvMapWriter mapWriter) throws IOException {
        for(Map<String,String> headerToValuePerCall : headerToValuePerGene) {
            mapWriter.write(headerToValuePerCall, header, processors);
        }
    }

    private Map<String, Integer> extractGeneTable(Integer speciesId, String directory) {
        log.traceEntry("{}, {}", speciesId, directory);
        log.info("Start extracting genes for the species {}...", speciesId);
        String[] header = new String[] { GeneDAO.Attribute.ID.name(), GeneDAO.Attribute.GENE_ID.name(),
                GeneDAO.Attribute.NAME.name(), GeneDAO.Attribute.DESCRIPTION.name(),
                GeneDAO.Attribute.SPECIES_ID.name() };

       List<GeneTO> allTOs = daoManagerSupplier.get().getGeneDAO()
                .getGenesBySpeciesIds(Collections.singleton(speciesId)).getAllTOs();
        List<Map<String, String>> allGenesInformation = allTOs.stream().map(gene -> {
            Map<String, String> headerToValue = new HashMap<>();
            headerToValue.put(GeneDAO.Attribute.ID.name(), String.valueOf(gene.getId()));
            headerToValue.put(GeneDAO.Attribute.GENE_ID.name(), gene.getGeneId());
            headerToValue.put(GeneDAO.Attribute.NAME.name(), gene.getName());
            headerToValue.put(GeneDAO.Attribute.DESCRIPTION.name(), gene.getDescription());
            headerToValue.put(GeneDAO.Attribute.SPECIES_ID.name(), String.valueOf(gene.getSpeciesId()));
            return headerToValue;
        }).collect(Collectors.toList());
        // use TsvFile Enum to generate the CellProcessor
        final CellProcessor[] processors = createCellProcessor(TsvFile.GENE_OUTPUT_FILE);
        File file = new File(directory, TsvFile.GENE_OUTPUT_FILE.fileName);
        writeOutputFile(file, allGenesInformation, header, processors);
        return log.traceExit(allTOs.stream().collect(Collectors.toMap(to -> to.getGeneId(), to -> to.getId())));
    }

    private void extractGeneXRefTable(String directory) {
        log.traceEntry("{}", directory);
        log.info("Start extracting gene XRefs");
        String[] header = TsvFile.GENE_XREF_OUTPUT_FILE.columnName.keySet().toArray(String[]::new);


        GeneXRefTOResultSet allGeneXRefsTOs = daoManagerSupplier.get().getGeneXRefDAO()
                .getAllGeneXRefs(null);
        Map<Integer, SourceTO> dataSourceById = daoManagerSupplier.get().getSourceDAO()
                .getAllDataSources(null).stream().collect(Collectors.toMap(s -> s.getId(), s -> s));
        Map<Integer, GeneTO> geneByBgeeGeneId = daoManagerSupplier.get().getGeneDAO()
                .getAllGenes().stream().collect(Collectors.toMap(s -> s.getId(), s -> s));
        Map<Integer, SpeciesTO> speciesBySpeciesId = daoManagerSupplier.get().getSpeciesDAO()
                .getAllSpecies(null).stream().collect(Collectors.toMap(s -> s.getId(), s -> s));
        // In easybgee we do not insert same info as in the dataSource table of Bgee DB. Then, it is possible
        // to have duplicated XRef URLs for the same gene. As XRefURLs are part of the primary key it is problematic.
        // This problem appears when a dataSource has an XRefUrl with the pattern [gene_id] and several
        // xref_id exist for the same gene and datasource.
        // In order to solve that issue we check xrefs coming from a datasource with [gene_id] pattern
        // If one gene already has an xrefUrl associated to it, then the corresponding xref is not inserted again.
        Map<Integer, Set<String>> manageDuplicatedXrefs = new HashMap<>();
        List<Map<String, String>> allGeneXRefsInformation = allGeneXRefsTOs.stream().map(geneXRef -> {
            Map<String, String> headerToValue = new HashMap<>();
            headerToValue.put(header[0], String.valueOf(geneXRef.getBgeeGeneId()));
            String dataSourceXRefUrl = dataSourceById.get(geneXRef.getDataSourceId()).getXRefUrl();
            if(dataSourceXRefUrl == null || dataSourceXRefUrl.isEmpty()) {
                return null;
            }
            if (dataSourceXRefUrl != null && dataSourceXRefUrl.contains("[species_ensembl_link]")) {
                SpeciesTO species = speciesBySpeciesId.get(geneByBgeeGeneId.get(geneXRef.getBgeeGeneId())
                        .getSpeciesId());
                dataSourceXRefUrl = dataSourceXRefUrl.replace("[species_ensembl_link]",
                        species.getGenus().replace(" ", "_") + "_" + species.getSpeciesName().replace(" ", "_"));
            }
            if (dataSourceXRefUrl != null && dataSourceXRefUrl.contains("[gene_id]")) {
                dataSourceXRefUrl = dataSourceXRefUrl.replace("[gene_id]",
                        geneByBgeeGeneId.get(geneXRef.getBgeeGeneId()).getGeneId());
                if (manageDuplicatedXrefs.get(geneXRef.getBgeeGeneId()) != null &&
                        manageDuplicatedXrefs.get(geneXRef.getBgeeGeneId()).contains(dataSourceXRefUrl)) {
                    return null;
                }
                Set<String> xrefs = manageDuplicatedXrefs.containsKey(geneXRef.getBgeeGeneId()) ?
                        manageDuplicatedXrefs.get(geneXRef.getBgeeGeneId()) :
                            new HashSet<>();
                xrefs.add(dataSourceXRefUrl);
                manageDuplicatedXrefs.put(geneXRef.getBgeeGeneId(), xrefs);
            }
            if (dataSourceXRefUrl != null && dataSourceXRefUrl.contains("[xref_id]")) {
                dataSourceXRefUrl = dataSourceXRefUrl.replace("[xref_id]", geneXRef.getXRefId());
            }
            headerToValue.put(header[1], dataSourceXRefUrl);
            headerToValue.put(header[2], dataSourceById.get(geneXRef.getDataSourceId()).getName());
            return headerToValue;
        }).filter(e -> e != null).collect(Collectors.toList());
        // use TsvFile Enum to generate the CellProcessor
        final CellProcessor[] processors = createCellProcessor(TsvFile.GENE_XREF_OUTPUT_FILE);
        File file = new File(directory, TsvFile.GENE_XREF_OUTPUT_FILE.fileName);
        writeOutputFile(file, allGeneXRefsInformation, header, processors);
        log.traceExit();
    }

    private void extractAnatEntityTable(String directory) {
        log.traceEntry("{}", directory);
        log.info("Start extracting anatomical entities...");
        String[] header = new String[] { AnatEntityDAO.Attribute.ID.name(), AnatEntityDAO.Attribute.NAME.name(),
                AnatEntityDAO.Attribute.DESCRIPTION.name() };
        List<Map<String, String>> allAnatEntitiesInformation = daoManagerSupplier.get().getAnatEntityDAO()
                .getAnatEntitiesByIds(null).stream().map(ae -> {
                    Map<String, String> headerToValue = new HashMap<>();
                    headerToValue.put(AnatEntityDAO.Attribute.ID.name(), ae.getId());
                    headerToValue.put(AnatEntityDAO.Attribute.NAME.name(), ae.getName());
                    headerToValue.put(AnatEntityDAO.Attribute.DESCRIPTION.name(), ae.getDescription());
                    return headerToValue;
                }).collect(Collectors.toList());
        // use TsvFile Enum to generate the CellProcessor
        final CellProcessor[] processors = createCellProcessor(TsvFile.ANATENTITY_OUTPUT_FILE);
        File file = new File(directory, TsvFile.ANATENTITY_OUTPUT_FILE.getFileName());
        writeOutputFile(file, allAnatEntitiesInformation, header, processors);
        log.traceExit();
    }

    private void extractStageTable(String directory) {
        log.traceEntry("{}", directory);
        log.info("Start extracting developmental stages");
        String[] header = new String[] { StageDAO.Attribute.ID.name(), StageDAO.Attribute.NAME.name(),
                StageDAO.Attribute.DESCRIPTION.name() };
        List<Map<String, String>> allDevStagesInformation = daoManagerSupplier.get().getStageDAO()
                .getStagesByIds(new HashSet<>())
                .stream().map(stage -> {
                    Map<String, String> headerToValue = new HashMap<>();
                    headerToValue.put(StageDAO.Attribute.ID.name(), String.valueOf(stage.getId()));
                    headerToValue.put(StageDAO.Attribute.NAME.name(), stage.getName());
                    headerToValue.put(StageDAO.Attribute.DESCRIPTION.name(), stage.getDescription());
                    return headerToValue;
                }).collect(Collectors.toList());
        // use TsvFile Enum to generate the CellProcessor
        final CellProcessor[] processors = createCellProcessor(TsvFile.DEVSTAGE_OUTPUT_FILE);
        File file = new File(directory, TsvFile.DEVSTAGE_OUTPUT_FILE.fileName);
        writeOutputFile(file, allDevStagesInformation, header, processors);
        log.traceExit();
    }

    private Map<String, String> extractGlobalCondTable(Integer speciesId, String directory) {
        log.traceEntry("{}, {}", speciesId, directory);
        log.info("Start extracting global conditions for the species {}...", speciesId);

        List<ConditionDAO.Attribute> attributes = Arrays.asList(ConditionDAO.Attribute.ID,
                ConditionDAO.Attribute.ANAT_ENTITY_ID, ConditionDAO.Attribute.STAGE_ID,
                ConditionDAO.Attribute.CELL_TYPE_ID, ConditionDAO.Attribute.SEX_ID,
                ConditionDAO.Attribute.STRAIN_ID, ConditionDAO.Attribute.SPECIES_ID);
        String[] header = new String[] { ConditionDAO.Attribute.ID.name(),
                ConditionDAO.Attribute.ANAT_ENTITY_ID.name(), ConditionDAO.Attribute.STAGE_ID.name(),
                ConditionDAO.Attribute.CELL_TYPE_ID.name(), ConditionDAO.Attribute.SEX_ID.name(),
                ConditionDAO.Attribute.STRAIN_ID.name(), ConditionDAO.Attribute.SPECIES_ID.name() };

        // Condition filter using root of sex and strain only: those two parameters are not
        // part of the OTF condParamCombination requested in extractGlobalExpressionTable, so
        // they always stay collapsed to root there. Cell type, unlike sex/strain, now IS part
        // of that combination (cell types are exported, not just whole-organ calls -- see
        // extractGlobalExpressionTable), so it must NOT be restricted to root here either,
        // otherwise cell-type-specific calls would have no matching row in this table and
        // buildConditionKeyFromCondition2's lookup would fail for every one of them.
        DAOConditionFilter condFilter = new DAOConditionFilter(null, null, null,
                Collections.singleton(ConditionDAO.SEX_ROOT_ID),
                Collections.singleton(ConditionDAO.STRAIN_ROOT_ID), null);

        //XXX: With the increasing number of data it is not realistic to generate easybgee
        // for all developmental stages. It would result in billions of rows. We decided
        // to only propagate among meta stages as they are shared among species. Meta stages
        // are all stage IDs with the namespace "UBERON:"
        List<ConditionTO> conditionTOs = daoManagerSupplier.get().getConditionDAO()
                .getGlobalConditions(Collections.singleton(speciesId),
                        Collections.singleton(condFilter), attributes).stream()
                .filter(c -> c.getStageId().startsWith("UBERON:"))
                .toList();

        //transformation from a List<ConditionTO> to a List<Map<String, String>> in order to easily write conditions in a file
        List<Map<String, String>> allGlobalCondInformation = conditionTOs.stream().map(cond -> {
            Map<String, String> headerToValue = new HashMap<>();
            headerToValue.put(ConditionDAO.Attribute.ID.name(), String.valueOf(cond.getId()));
            headerToValue.put(ConditionDAO.Attribute.ANAT_ENTITY_ID.name(), cond.getAnatEntityId());
            headerToValue.put(ConditionDAO.Attribute.STAGE_ID.name(), cond.getStageId());
            headerToValue.put(ConditionDAO.Attribute.CELL_TYPE_ID.name(), cond.getCellTypeId());
            headerToValue.put(ConditionDAO.Attribute.SEX_ID.name(), cond.getSex().getStringRepresentation());
            headerToValue.put(ConditionDAO.Attribute.STRAIN_ID.name(), cond.getStrainId());
            headerToValue.put(ConditionDAO.Attribute.SPECIES_ID.name(), String.valueOf(cond.getSpeciesId()));
            return headerToValue;
        })
        .collect(Collectors.toList());


        //use TsvFile Enum to generate the CellProcessor
        final CellProcessor[] processors = createCellProcessor(TsvFile.GLOBALCOND_OUTPUT_FILE);

        // write global condition tsv file
        File file = new File(directory, TsvFile.GLOBALCOND_OUTPUT_FILE.getFileName());
        writeOutputFile(file, allGlobalCondInformation, header, processors);
        return log.traceExit(createCondToConditionIdMap(conditionTOs));
    }

    private Set<Integer> extractSpeciesTable(SpeciesTOResultSet speciesTOs, String directory) {
        log.traceEntry("{}, {}", speciesTOs, directory);
        Set<Integer> speciesIds = new HashSet<>();
        String[] header = new String[] { SpeciesDAO.Attribute.ID.name(), SpeciesDAO.Attribute.GENUS.name(),
                SpeciesDAO.Attribute.SPECIES_NAME.name(), SpeciesDAO.Attribute.COMMON_NAME.name(),
                SpeciesDAO.Attribute.GENOME_VERSION.name(), SpeciesDAO.Attribute.GENOME_ASSEMBLY_XREF.name(),
                SpeciesDAO.Attribute.GENOME_SPECIES_ID.name() };
        // keep the order, to be able to compare files between 2 versions
        List<Map<String, String>> allSpeciesInformation = speciesTOs.stream().map(species -> {
            speciesIds.add(species.getId());
            Map<String, String> headerToValue = new HashMap<>();
            headerToValue.put(SpeciesDAO.Attribute.ID.name(), String.valueOf(species.getId()));
            headerToValue.put(SpeciesDAO.Attribute.GENUS.name(), species.getGenus());
            headerToValue.put(SpeciesDAO.Attribute.SPECIES_NAME.name(), species.getSpeciesName());
            headerToValue.put(SpeciesDAO.Attribute.COMMON_NAME.name(), species.getName());
            headerToValue.put(SpeciesDAO.Attribute.GENOME_VERSION.name(), species.getGenomeVersion());
            headerToValue.put(SpeciesDAO.Attribute.GENOME_ASSEMBLY_XREF.name(), species.getGenomeAssemblyXRef());
            headerToValue.put(SpeciesDAO.Attribute.GENOME_SPECIES_ID.name(),
                    String.valueOf(species.getGenomeSpeciesId()));
            return headerToValue;
        }).collect(Collectors.toList());
        final CellProcessor[] processors = createCellProcessor(TsvFile.SPECIES_OUTPUT_FILE);
        File file = new File(directory, TsvFile.SPECIES_OUTPUT_FILE.getFileName());
        writeOutputFile(file, allSpeciesInformation, header, processors);
        return log.traceExit(speciesIds);
    }

    /**
     * Create A table of {@code CellProcessor} used by superCSV
     *
     * @param enumValue
     *            The {@code TsvFile} Enum values specific to
     * @return A table of {@code CellProcessor}
     */
    private CellProcessor[] createCellProcessor(TsvFile enumValue) {
        log.traceEntry("{}", enumValue);
        CellProcessor[] cellProcessor = new CellProcessor[enumValue.getDatatypes().size()];
        int index = 0;
        for (Map.Entry<String, Integer> entry : enumValue.getDatatypes().entrySet()) {
            Integer dataType = entry.getValue();
            if (enumValue.getIsNullable().get(entry.getKey()) == null
                    || enumValue.getIsNullable().get(entry.getKey())) {
                // if isNullable equals to true or null
                if (dataType.equals(Types.INTEGER)) {
                    cellProcessor[index] = new Optional(new ParseInt());
                } else if (dataType.equals(Types.VARCHAR)) {
                    cellProcessor[index] = new Optional();
                } else if (dataType.equals(Types.DECIMAL)) {
                    cellProcessor[index] = new Optional(new ParseDouble());
                } else {
                    throw log.throwing(new IllegalArgumentException(
                            "The sql.Types equals to " + dataType + " is not currently implemented"));
                }
            } else {
                if (dataType.equals(Types.INTEGER)) {
                    cellProcessor[index] = new NotNull(new ParseInt());
                } else if (dataType.equals(Types.VARCHAR)) {
                    cellProcessor[index] = new NotNull();
                } else if (dataType.equals(Types.DECIMAL)) {
                    cellProcessor[index] = new NotNull(new ParseDouble());
                } else {
                    throw log.throwing(new IllegalArgumentException(
                            "The sql.Types equals to " + dataType + " is not currently implemented"));
                }
            }
            index++;
        }
        return log.traceExit(cellProcessor);
    }

    /**
     * Write a tsv file
     *
     * @param file
     *            The {@code File} where data will be written
     * @param fileLines
     *            A {@code Collection} of {@code Map}. Each element of the
     *            {@code List} corresponds to one line. Each key of the
     *            {@code Map} corresponds to one column and each value
     *            corresponds to the data that has to be written
     * @param header
     *            The table of {@code String} used as header by superCSV
     * @param processors
     *            The table of {@code CellProcessor} used by superCSV
     */
    private void writeOutputFile(File file, Collection<Map<String, String>> fileLines, String[] header,
            CellProcessor[] processors) {
        log.traceEntry("{}, {}, {}, {}", file, fileLines, header, processors);
        try {
            boolean writeHeader = false;
            if (!file.exists()) {
                writeHeader = true;
            }
            try (ICsvMapWriter mapWriter = new CsvMapWriter(new FileWriter(file, true), Utils.TSVCOMMENTED)) {
                if(writeHeader) {
                    file.createNewFile();
                    mapWriter.writeHeader(header);
                }
                for (Map<String, String> line : fileLines) {
                    mapWriter.write(line, header, processors);
                }
            }
        } catch (IOException e) {
            throw log.throwing(new UncheckedIOException("Can't write file " + file, e));
        }
        log.traceExit();
    }

    /**
     * method used to delete all data from all tables of the Easy Bgee database
     */
    private void emptyDatabaseTables() {
        log.traceEntry();
        String serverName = "";
        Pattern pattern = Pattern.compile("^.*//(.*?):");
        Matcher matcher = pattern.matcher(this.getManager().getJdbcUrl());
        if (matcher.find()) {
            serverName = matcher.group(1);
        }
        for (TsvFile tsvFile : TsvFile.values()) {
            log.info("delete all data in table {} of Easy Bgee from {}", tsvFile.getTableName(), serverName);
            String sql = "DELETE FROM " + tsvFile.getTableName();
            try (BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql)) {
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw log.throwing(new IllegalStateException("Can not connect to the database"));
            }
        }
        log.traceExit();
    }

    /**
     * This method is needed because we cannot access the conditionId from a {@code Condition2}
     * (calls returned by OTF propagation only carry the condition's parameter values, not the
     * internal ID it was exported with in {@link #extractGlobalCondTable(Integer, String)}).
     * The resulting {@code Map} is then useful to retrieve the conditionId from a condition,
     * identified by the {@code String} key built by {@link #buildConditionKey(String, String,
     * String, String, String, int)}.
     *
     * @param conditionTOs
     *            A {@code List} of {@code ConditionTO}s for which we want to be
     *            able to retrieve the conditionId.
     * @return A {@code Map} where keys are the {@code String} keys built by
     *         {@link #buildConditionKeyFromConditionTO(ConditionTO)}, the associated value
     *         being a {@code String} that is the ID of the associated condition.
     */
    private static Map<String, String> createCondToConditionIdMap(List<ConditionTO> conditionTOs) {
        log.traceEntry("{}", conditionTOs);
        return log
            .traceExit(conditionTOs.stream()
                .collect(Collectors.toMap(
                    BgeeToEasyBgee::buildConditionKeyFromConditionTO,
                    p -> String.valueOf(p.getId()))));
    }

    /**
     * Builds the same kind of {@code String} key as {@link #buildConditionKeyFromCondition2(
     * Condition2)}, but from a {@code ConditionTO} as retrieved by
     * {@link #extractGlobalCondTable(Integer, String)}. Both methods must build the key the
     * exact same way, substituting the same "root" sentinel for {@code null}/empty values on
     * both sides, so that a condition returned by OTF propagation can be matched back to the
     * exported {@code globalCond} row it corresponds to.
     */
    private static String buildConditionKeyFromConditionTO(ConditionTO conditionTO) {
        log.traceEntry("{}", conditionTO);
        return log.traceExit(buildConditionKey(conditionTO.getAnatEntityId(), conditionTO.getCellTypeId(),
                conditionTO.getStageId(),
                conditionTO.getSex() == null? null: conditionTO.getSex().getStringRepresentation(),
                conditionTO.getStrainId(), conditionTO.getSpeciesId()));
    }

    /**
     * See {@link #buildConditionKeyFromConditionTO(ConditionTO)}.
     * <p>
     * The extraction of the anat. entity and cell type IDs from the composed
     * {@code ANAT_ENTITY_CELL_TYPE} condition parameter relies on the ordering convention
     * established in {@code OTFExpressionCallFilterEngine} (index 1 = anat. entity, index 0 =
     * cell type when both are present). This is confirmed by
     * {@code CallServiceUtils.loadConditionMapFromResultSet}, which is what builds the
     * {@code Condition2}s returned by OTF propagation: it always inserts the cell type before
     * the anat. entity into the underlying {@code LinkedHashSet} (in that order, only when
     * non-null), so a single-entity composed value only ever contains the anat. entity (a
     * whole-organ condition, no specific cell type), never a cell type alone.
     */
    private static String buildConditionKeyFromCondition2(Condition2 cond) {
        log.traceEntry("{}", cond);

        ComposedEntity<AnatEntity> anatEntityCellType =
                cond.getConditionParameterValue(ConditionParameter.ANAT_ENTITY_CELL_TYPE);
        String anatEntityId;
        String cellTypeId;
        if (anatEntityCellType == null || anatEntityCellType.isEmpty()) {
            throw log.throwing(new IllegalStateException(
                    "A Condition2 must always have an anat. entity: " + cond));
        } else if (anatEntityCellType.size() == 1) {
            //Only one entity present: the anat. entity, no specific cell type
            //(whole-organ condition) -- see the confirmed convention documented above.
            anatEntityId = anatEntityCellType.getEntity(0).getId();
            cellTypeId = null;
        } else if (anatEntityCellType.size() == 2) {
            AnatEntity anatEntity = anatEntityCellType.getEntity(1);
            AnatEntity cellType = anatEntityCellType.getEntity(0);
            if (anatEntity == null) {
                throw log.throwing(new IllegalStateException(
                        "Unexpected composed anat. entity/cell type entity: " + anatEntityCellType));
            }
            anatEntityId = anatEntity.getId();
            cellTypeId = cellType == null? null: cellType.getId();
        } else {
            throw log.throwing(new IllegalStateException(
                    "Unexpected number of entities composing anat. entity/cell type: "
                    + anatEntityCellType));
        }
        String stageId = cond.getConditionParameterId(ConditionParameter.DEV_STAGE);
        String sexId = cond.getConditionParameterId(ConditionParameter.SEX);
        String strainId = cond.getConditionParameterId(ConditionParameter.STRAIN);
        return log.traceExit(buildConditionKey(anatEntityId, cellTypeId, stageId, sexId, strainId,
                cond.getSpeciesId()));
    }

    /**
     * Builds a stable {@code String} key identifying a global condition from its component IDs.
     * Used on both sides of the lookup: from the {@code ConditionTO}s exported by
     * {@link #extractGlobalCondTable(Integer, String)} ({@link #buildConditionKeyFromConditionTO(
     * ConditionTO)}), and from the {@code Condition2}s of the calls returned by OTF propagation
     * ({@link #buildConditionKeyFromCondition2(Condition2)}). {@code null} or empty values are
     * substituted with the corresponding "root" sentinel ID, matching how
     * {@link #extractGlobalCondTable(Integer, String)} restricts its own query to root sex/
     * strain (cell type is intentionally not restricted to root there, since cell-type-specific
     * conditions are exported too -- see {@link #extractGlobalExpressionTable(Map, Map, Integer,
     * String)}).
     */
    private static String buildConditionKey(String anatEntityId, String cellTypeId, String stageId,
            String sexId, String strainId, int speciesId) {
        return String.join("|",
                anatEntityId,
                cellTypeId == null || cellTypeId.isEmpty()? ConditionDAO.CELL_TYPE_ROOT_ID: cellTypeId,
                stageId == null || stageId.isEmpty()? ConditionDAO.DEV_STAGE_ROOT_ID: stageId,
                sexId == null || sexId.isEmpty()? ConditionDAO.SEX_ROOT_ID: sexId,
                strainId == null || strainId.isEmpty()? ConditionDAO.STRAIN_ROOT_ID: strainId,
                String.valueOf(speciesId));
    }

    /**
     * Use all tsv files generated in the previous step of Easy Bgee creation and
     * integrate data in the Easy Bgee relational database
     *
     * @param directory
     *            A {@code String} corresponding to the directory where tsv
     *            files are stored
     */
    private void tsvToEasyBgee(String directory) {
        log.traceEntry("{}", directory);

        for (TsvFile tsvFile : TsvFile.values()) {
            log.info("start integration of data from file {}", tsvFile.getFileName());
            File file = new File(directory, tsvFile.getFileName());

            try (ICsvMapReader mapReader = new CsvMapReader(new FileReader(file), Utils.TSVCOMMENTED)) {
                // the header columns are used as the keys of the Mapping
                String[] header = mapReader.getHeader(true);
                CellProcessor[] processors = createCellProcessor(tsvFile);

                // create SQL query
                String sql = "INSERT INTO " + tsvFile.getTableName();
                sql += " " + tsvFile.getColumnName().values().stream().collect(Collectors.joining(", ", "(", ")"));
                sql += " VALUES " + Collections.nCopies(tsvFile.getColumnName().size(), "?").stream()
                        .collect(Collectors.joining(", ", "(", ")"));
                log.info("SQL query : {}", sql);

                try (BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql)) {
                    // save all data contained in a tsv file using the same
                    // transaction
                    startTransaction();
                    Map<String, Object> customerMap;

                    // read all lines of the tsv file
                    while ((customerMap = mapReader.read(header, processors)) != null) {
                        int columnNumber = 1;
                        for (String columnId : tsvFile.getColumnName().keySet()) {
                            Object columnValue = customerMap.get(columnId);
                            if (columnValue instanceof Integer) {
                                stmt.setInt(columnNumber, Integer.valueOf(String.valueOf(columnValue)));
                            } else if (columnValue instanceof Number) {
                                stmt.setBigDecimal(columnNumber, String.valueOf(columnValue));
                            } else if (columnValue instanceof String) {
                                stmt.setString(columnNumber, String.valueOf(columnValue));
                            } else if (columnValue == null) {
                                // if isNullable equals to null it means that
                                // the schema does not allow
                                // null values but default value of this column
                                // is an empty String.
                                if (tsvFile.getIsNullable().get(columnId) == null) {
                                    stmt.setString(columnNumber, "");
                                } else if (tsvFile.getIsNullable().get(columnId)) {
                                    stmt.setNull(columnNumber, tsvFile.getDatatypes().get(columnId));
                                } else {
                                    throw log.throwing(new IllegalArgumentException(
                                            "For the moment we only take into account VARCHAR and TEXT "
                                                    + "sql data types to transform null column in the TSV file "
                                                    + "to empty String in the database " + customerMap.toString()
                                                    + " For column " + tsvFile.getColumnName().get(columnId)
                                                    + " columnId " + columnId + " header " + header.toString() + " processor "
                                                    + processors.toString() + " columnNumber " + columnNumber));
                                }
                            } else {
                                throw log.throwing(new IllegalArgumentException(
                                        "Column "+columnValue+" not taken into account. Each column should be"
                                                + " an instance of Integer, BigDecimal, String, or null."));
                            }
                            columnNumber++;
                        }
                        stmt.executeUpdate();
                    }
                    // commit once all lines of the file have been parsed
                    commit();
                } catch (SQLException e) {
                    throw log.throwing(new IllegalStateException("Can not insert at least one " + tsvFile.getTableName()
                            + " in the database. Please verify that both the database "
                            + "and the table exist, and that the table is empty."));
                }
            } catch (FileNotFoundException e) {
                throw log.throwing(
                        new IllegalStateException("Can not find the file " + directory + tsvFile.getFileName()));
            } catch (IOException e) {
                throw log.throwing(
                        new IllegalStateException("Can not read the file " + directory + tsvFile.getFileName()));
            }
        }
        log.traceExit();
    }

    /**
     * Unlike the old {@code DataPropagation}, {@code OTFExpressionCall#getDataPropagation()}
     * already returns a {@code PropagationState} directly, so no condition parameter
     * combination is needed here anymore to derive it.
     */
    private static String dataPropagationToString(PropagationState propState) {
        log.traceEntry("{}", propState);
        if (propState == null) {
            throw log.throwing(
                    new IllegalStateException("no data propagation retrieved"));
        }
        if (PropagationState.SELF_AND_DESCENDANT.equals(propState)) {
            return log.traceExit("self and descendant");
        }
        if (PropagationState.SELF.equals(propState)) {
            return log.traceExit("self");
        }
        if (PropagationState.DESCENDANT.equals(propState)) {
            return log.traceExit("descendant");
        }

        throw log.throwing(new IllegalArgumentException("Unknown data propagation status  "
                + propState));
    }
}
