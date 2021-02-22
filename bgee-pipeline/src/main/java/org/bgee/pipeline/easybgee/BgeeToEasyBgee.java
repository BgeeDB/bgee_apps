package org.bgee.pipeline.easybgee;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO;
import org.bgee.model.dao.api.anatdev.StageDAO;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.ConditionDAO.ConditionTO;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTOResultSet;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.Condition;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.PropagationState;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.species.Species;
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
// TODO: add javadoc to all methods
public class BgeeToEasyBgee extends MySQLDAOUser {

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
                put("GENOME_SPECIES_ID", "genomeSpeciesId");
            }
        }, new LinkedHashMap<String, Integer>() {
            {
                put("ID", Types.INTEGER);
                put("GENUS", Types.VARCHAR);
                put("SPECIES_NAME", Types.VARCHAR);
                put("COMMON_NAME", Types.VARCHAR);
                put("GENOME_VERSION", Types.VARCHAR);
                put("GENOME_SPECIES_ID", Types.INTEGER);
            }
        }, new LinkedHashMap<String, Boolean>() {
            {
                put("ID", false);
                put("GENUS", false);
                put("SPECIES_NAME", false);
                put("COMMON_NAME", true);
                put("GENOME_VERSION", false);
                put("GENOME_SPECIES_ID", false);
            }
        }), GENE_OUTPUT_FILE("genes_easy_bgee.tsv", "gene", new LinkedHashMap<String, String>() {
            {
                put("ID", "bgeeGeneId");
                put("ENSEMBL_ID", "geneId");
                put("NAME", "geneName");
                put("DESCRIPTION", "geneDescription");
                put("SPECIES_ID", "speciesId");
            }
        }, new LinkedHashMap<String, Integer>() {
            {
                put("ID", Types.INTEGER);
                put("ENSEMBL_ID", Types.VARCHAR);
                put("NAME", Types.VARCHAR);
                put("DESCRIPTION", Types.VARCHAR);
                put("SPECIES_ID", Types.INTEGER);
            }
        }, new LinkedHashMap<String, Boolean>() {
            {
                put("ID", false);
                put("ENSEMBL_ID", false);
                // geneName is defined in the database as not null with default
                // value = ''
                // to be compatible with the superCSV cellProcessor, isNullable
                // is defined to null
                put("NAME", null);
                put("DESCRIPTION", true);
                put("SPECIES_ID", false);
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
                put("SPECIES_ID", "speciesId");
            }
        }, new LinkedHashMap<String, Integer>() {
            {
                put("ID", Types.INTEGER);
                put("ANAT_ENTITY_ID", Types.VARCHAR);
                put("STAGE_ID", Types.VARCHAR);
                put("SPECIES_ID", Types.INTEGER);
            }
        }, new LinkedHashMap<String, Boolean>() {
            {
                put("ID", false);
                // according to the bgee schema anatEntityId could be null. In
                // reality it is never the case.
                put("ANAT_ENTITY_ID", true);
                put("STAGE_ID", true);
                put("SPECIES_ID", false);
            }
        }), GLOBALEXPRESSION_OUTPUT_FILE("global_expression_easy_bgee.tsv", "globalExpression",
                new LinkedHashMap<String, String>() {
                    {
                        put("BGEE_GENE_ID", "bgeeGeneId");
                        put("GLOBAL_CONDITION_ID", "globalConditionId");
                        put("SUMMARY_QUALITY", "summaryQuality");
                        put("MEAN_RANK", "rank");
                        put("MEAN_SCORE", "score");
                        put("ORIGIN", "propagationOrigin");
                        put("CALL_TYPE", "callType");
                    }
                }, new LinkedHashMap<String, Integer>() {
                    {
                        put("BGEE_GENE_ID", Types.INTEGER);
                        put("GLOBAL_CONDITION_ID", Types.INTEGER);
                        put("SUMMARY_QUALITY", Types.VARCHAR);
                        put("MEAN_RANK", Types.DECIMAL);
                        put("MEAN_SCORE", Types.DECIMAL);
                        put("ORIGIN", Types.VARCHAR);
                        put("CALL_TYPE", Types.VARCHAR);
                    }
                }, new LinkedHashMap<String, Boolean>() {
                    {
                        put("BGEE_GENE_ID", false);
                        put("GLOBAL_CONDITION_ID", false);
                        put("SUMMARY_QUALITY", false);
                        put("MEAN_RANK", false);
                        put("MEAN_SCORE", false);
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
    private static String GLOBAL_EXPRESSION_SUMMARY_CALL_TYPE = "CALL_TYPE";
    
    private final static Logger log = LogManager.getLogger(BgeeToEasyBgee.class);
    /**
     * The {@code ServiceFactory} used to acquire {@code Service}s (from the
     * {@code bgee-core} module).
     */
    protected final ServiceFactory serviceFactory;

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
        log.entry((Object[]) args);
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

    // XXX: should we use services when it is possible (at least when we don't
    // need internal IDs)?
    public BgeeToEasyBgee() {
        this(null, new ServiceFactory());
    }

    public BgeeToEasyBgee(MySQLDAOManager manager, ServiceFactory serviceFactory) {
        super(manager);
        this.serviceFactory = serviceFactory;
    }

    /**
     * Clean the output directory. If output ".tsv" files exist they are
     * deleted. This Method also create the output directory if it does not
     * already exist.
     * 
     * @param directory to the output directory to cleanpath 
     */
    private void cleanOutputDir(String directory) {
        log.entry(directory);
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
        log.entry(inputSpeciesIds, directory);
        SpeciesTOResultSet speciesTOs = getSpeciesDAO().getSpeciesByIds(new HashSet<>(inputSpeciesIds), null);
        // XXX: add check that all provided species IDs are found
        Set<Integer> speciesIds = extractSpeciesTable(speciesTOs, directory);
        extractAnatEntityTable(directory);
        extractStageTable(directory);
        for (Integer speciesId : speciesIds) {
            log.info("start to extract genes, conditions and expressions data for species {}", speciesId);
            // Note: we can map Ensembl ID to one Bgee gene ID because we use
            // data for only 1 species
            Map<String, Integer> ensemblIdToBgeeGeneId = extractGeneTable(speciesId, directory);
            Map<Condition, String> condToConditionId = extractGlobalCondTable(speciesId, directory);
            extractGlobalExpressionTable(ensemblIdToBgeeGeneId, condToConditionId, speciesId, directory);
        }
        log.traceExit();
    }

    private void extractGlobalExpressionTable(Map<String, Integer> ensemblIdToBgeeGeneId,
            Map<Condition, String> condToConditionId, Integer speciesId, String directory) {
        log.entry(ensemblIdToBgeeGeneId, condToConditionId, speciesId, directory);

        log.info("Start extracting global expressions for the species {}...", speciesId);

        String[] header = new String[] { GlobalExpressionCallDAO.Attribute.BGEE_GENE_ID.name(),
                GlobalExpressionCallDAO.Attribute.GLOBAL_CONDITION_ID.name(), GLOBAL_EXPRESSION_SUMMARY_QUALITY,
                GlobalExpressionCallDAO.Attribute.MEAN_RANK.name(), GLOBAL_EXPRESSION_MEAN_SCORE, 
                GLOBAL_EXPRESSION_ORIGIN, GLOBAL_EXPRESSION_SUMMARY_CALL_TYPE };

        // init ordering attributes for anatEntity and devStage
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> orderingAnatStage = new LinkedHashMap<>();
        orderingAnatStage.put(CallService.OrderingAttribute.GENE_ID, Service.Direction.ASC);
        orderingAnatStage.put(CallService.OrderingAttribute.ANAT_ENTITY_ID, Service.Direction.ASC);
        orderingAnatStage.put(CallService.OrderingAttribute.DEV_STAGE_ID, Service.Direction.ASC);

        // init ordering attributes for anatEntity only
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> orderingAnatOnly = new LinkedHashMap<>();
        orderingAnatOnly.put(CallService.OrderingAttribute.GENE_ID, Service.Direction.ASC);
        orderingAnatOnly.put(CallService.OrderingAttribute.ANAT_ENTITY_ID, Service.Direction.ASC);

        // return calls where both anatEntity and devStage are not null AND with
        // a SILVER quality
        final List<Map<String, String>> callsInformation = 
                getGlobalExpressionMap(speciesId, ensemblIdToBgeeGeneId, condToConditionId,
                        EnumSet.of(CallService.Attribute.GENE, CallService.Attribute.DATA_QUALITY,
                        		CallService.Attribute.ANAT_ENTITY_ID, CallService.Attribute.DEV_STAGE_ID, 
                                CallService.Attribute.MEAN_RANK, CallService.Attribute.EXPRESSION_SCORE,
                                CallService.Attribute.CALL_TYPE, CallService.Attribute.OBSERVED_DATA),
                        orderingAnatStage);

        // return calls with null devStage AND with a SILVER quality
        callsInformation.addAll(
        		getGlobalExpressionMap(speciesId,
                        ensemblIdToBgeeGeneId, condToConditionId, EnumSet.of(CallService.Attribute.GENE, 
                        		CallService.Attribute.DATA_QUALITY, CallService.Attribute.ANAT_ENTITY_ID, 
                        		CallService.Attribute.MEAN_RANK, 
                        		CallService.Attribute.EXPRESSION_SCORE, CallService.Attribute.CALL_TYPE, 
                        		CallService.Attribute.OBSERVED_DATA),
                        orderingAnatOnly));

        // use TsvFile enum to generate the CellProcessor
        final CellProcessor[] processors = createCellProcessor(TsvFile.GLOBALEXPRESSION_OUTPUT_FILE);

        File file = new File(directory, TsvFile.GLOBALEXPRESSION_OUTPUT_FILE.getFileName());
        writeOutputFile(file, callsInformation, header, processors);
        log.traceExit();
    }

    private List<Map<String, String>> getGlobalExpressionMap(Integer speciesId,
            Map<String, Integer> ensemblIdToBgeeGeneId, Map<Condition, String> condToConditionId,
            Collection<CallService.Attribute> attributes,
            LinkedHashMap<CallService.OrderingAttribute, Service.Direction> orderingAttributes) {
        log.entry(speciesId, ensemblIdToBgeeGeneId, condToConditionId, attributes, orderingAttributes);

        // init summaryCallTypeQualityFilter
        Map<SummaryCallType.ExpressionSummary, SummaryQuality> silverCallFilter = new HashMap<>();
        silverCallFilter.put(ExpressionSummary.EXPRESSED, SummaryQuality.SILVER);
        silverCallFilter.put(ExpressionSummary.NOT_EXPRESSED, SummaryQuality.SILVER);

        // init callObservedData
        Map<CallType.Expression, Boolean> obsDataFilter = new HashMap<>();
        obsDataFilter.put(null, true);
        return log.traceExit(serviceFactory.getCallService()
                .loadExpressionCalls(new ExpressionCallFilter(silverCallFilter,
                        Collections.singleton(new GeneFilter(speciesId, ensemblIdToBgeeGeneId.keySet())), null, null,
                        obsDataFilter, null, null), attributes, orderingAttributes)
                

                .map(call -> {
                    Map<String, String> headerToValue = new HashMap<>();
                    headerToValue.put(GlobalExpressionCallDAO.Attribute.BGEE_GENE_ID.name(),
                            String.valueOf(ensemblIdToBgeeGeneId.get(call.getGene().getEnsemblGeneId())));
                    Condition updatedCond = new Condition(new AnatEntity(call.getCondition().getAnatEntityId()),
                            call.getCondition().getDevStageId() == null ? null : new DevStage(call.getCondition().getDevStageId()),
                                    new Species(call.getCondition().getSpeciesId()));
                    String conditionId = condToConditionId.get(updatedCond);
                    headerToValue.put(GlobalExpressionCallDAO.Attribute.GLOBAL_CONDITION_ID.name(), conditionId);
                    headerToValue.put(GLOBAL_EXPRESSION_SUMMARY_QUALITY,
                            call.getSummaryQuality().getStringRepresentation());
                    headerToValue.put(GlobalExpressionCallDAO.Attribute.MEAN_RANK.name(),
                            call.getMeanRank().toString());
                    headerToValue.put(GLOBAL_EXPRESSION_MEAN_SCORE, call.getExpressionScore().toString());
                    headerToValue.put(GLOBAL_EXPRESSION_ORIGIN, dataPropagationToString(call.getDataPropagation()));
                    headerToValue.put(GLOBAL_EXPRESSION_SUMMARY_CALL_TYPE, call.getSummaryCallType().getStringRepresentation());
                    return headerToValue;
                }).collect(Collectors.toList()));
    }

    private Map<String, Integer> extractGeneTable(Integer speciesId, String directory) {
        log.entry(speciesId, directory);
        log.info("Start extracting genes for the species {}...", speciesId);
        String[] header = new String[] { GeneDAO.Attribute.ID.name(), GeneDAO.Attribute.ENSEMBL_ID.name(),
                GeneDAO.Attribute.NAME.name(), GeneDAO.Attribute.DESCRIPTION.name(),
                GeneDAO.Attribute.SPECIES_ID.name() };

        List<GeneDAO.GeneTO> allTOs = getManager().getGeneDAO()
                .getGenesBySpeciesIds(Collections.singleton(speciesId)).getAllTOs();
        List<Map<String, String>> allGenesInformation = allTOs.stream().map(gene -> {
            Map<String, String> headerToValue = new HashMap<>();
            headerToValue.put(GeneDAO.Attribute.ID.name(), String.valueOf(gene.getId()));
            headerToValue.put(GeneDAO.Attribute.ENSEMBL_ID.name(), gene.getGeneId());
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

    private void extractAnatEntityTable(String directory) {
        log.entry(directory);
        log.info("Start extracting anatomical entities...");
        String[] header = new String[] { AnatEntityDAO.Attribute.ID.name(), AnatEntityDAO.Attribute.NAME.name(),
                AnatEntityDAO.Attribute.DESCRIPTION.name() };
        List<Map<String, String>> allAnatEntitiesInformation = getManager().getAnatEntityDAO()
                .getAnatEntitiesByIds(null).getAllTOs().stream().map(ae -> {
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
        log.entry(directory);
        log.info("Start extracting developmental stages");
        String[] header = new String[] { StageDAO.Attribute.ID.name(), StageDAO.Attribute.NAME.name(),
                StageDAO.Attribute.DESCRIPTION.name() };
        List<Map<String, String>> allDevStagesInformation = getStageDAO().getStagesByIds(new HashSet<>())
                .getAllTOs().stream().map(stage -> {
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

    private Map<Condition, String> extractGlobalCondTable(Integer speciesId, String directory) {
        log.entry(speciesId, directory);
        log.info("Start extracting global conditions for the species {}...", speciesId);
        List<ConditionDAO.Attribute> condAttributesAnatAndStage = 
                Arrays.asList(ConditionDAO.Attribute.ANAT_ENTITY_ID, ConditionDAO.Attribute.STAGE_ID);
        List<ConditionDAO.Attribute> condAttributesAnat = Arrays.asList(ConditionDAO.Attribute.ANAT_ENTITY_ID);
        List<ConditionDAO.Attribute> attributes = Arrays.asList(ConditionDAO.Attribute.ID,
                ConditionDAO.Attribute.ANAT_ENTITY_ID, ConditionDAO.Attribute.STAGE_ID,
                ConditionDAO.Attribute.SPECIES_ID);
        String[] header = new String[] { ConditionDAO.Attribute.ID.name(), 
                ConditionDAO.Attribute.ANAT_ENTITY_ID.name(), ConditionDAO.Attribute.STAGE_ID.name(), 
                ConditionDAO.Attribute.SPECIES_ID.name() };
        
        // Retrieve conditions with devStage = null
        List<ConditionTO> conditionTOs = getManager().getConditionDAO()
                .getGlobalConditionsBySpeciesIds(Collections.singleton(speciesId), condAttributesAnat, attributes)
                .getAllTOs();
        
        // Retrieve conditions where both anatEntity and devStage are not null
        conditionTOs.addAll(getManager().getConditionDAO().getGlobalConditionsBySpeciesIds(
                Collections.singleton(speciesId), condAttributesAnatAndStage, attributes)
                .getAllTOs());
        
        //transformation from a List<ConditionTO> to a List<Map<String, String>> in order to easily write conditions in a file
        List<Map<String, String>> allGlobalCondInformation = conditionTOs.stream().map(cond -> {
            Map<String, String> headerToValue = new HashMap<>();
            headerToValue.put(ConditionDAO.Attribute.ID.name(), String.valueOf(cond.getId()));
            headerToValue.put(ConditionDAO.Attribute.ANAT_ENTITY_ID.name(), cond.getAnatEntityId());
            headerToValue.put(ConditionDAO.Attribute.STAGE_ID.name(), cond.getStageId());
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
        log.entry(speciesTOs, directory);
        Set<Integer> speciesIds = new HashSet<>();
        String[] header = new String[] { SpeciesDAO.Attribute.ID.name(), SpeciesDAO.Attribute.GENUS.name(),
                SpeciesDAO.Attribute.SPECIES_NAME.name(), SpeciesDAO.Attribute.COMMON_NAME.name(),
                SpeciesDAO.Attribute.GENOME_VERSION.name(), SpeciesDAO.Attribute.GENOME_SPECIES_ID.name() };
        // keep the order, to be able to compare files between 2 versions
        List<Map<String, String>> allSpeciesInformation = speciesTOs.getAllTOs().stream().map(species -> {
            speciesIds.add(species.getId());
            Map<String, String> headerToValue = new HashMap<>();
            headerToValue.put(SpeciesDAO.Attribute.ID.name(), String.valueOf(species.getId()));
            headerToValue.put(SpeciesDAO.Attribute.GENUS.name(), species.getGenus());
            headerToValue.put(SpeciesDAO.Attribute.SPECIES_NAME.name(), species.getSpeciesName());
            headerToValue.put(SpeciesDAO.Attribute.COMMON_NAME.name(), species.getName());
            headerToValue.put(SpeciesDAO.Attribute.GENOME_VERSION.name(), species.getGenomeVersion());
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
        log.entry(enumValue);
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
        log.entry(file, fileLines, header, processors);
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
        Matcher matcher = pattern.matcher(getManager().getJdbcUrl());
        if (matcher.find()) {
            serverName = matcher.group(1);
        }
        for (TsvFile tsvFile : TsvFile.values()) {
            log.info("delete all data in table {} of Easy Bgee from {}", tsvFile.getTableName(), serverName);
            String sql = "DELETE FROM " + tsvFile.getTableName();
            try (BgeePreparedStatement stmt = getManager().getConnection().prepareStatement(sql)) {
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw log.throwing(new IllegalStateException("Can not connect to the database"));
            }
        }
        log.traceExit();
    }

    /**
     * This method is needed because we can not access to the conditionId when
     * using {@code Condition}. The resulting {@code Map} is then useful to
     * retrieve the conditionId from one {@code Condition}.
     *
     * @param conditionTOs
     *            A {@code List} of {@code ConditionTO}s for which we want to be
     *            able to retrieve the conditionId.
     * @return A {@code Map} where keys are {@code Condition}s representing
     *         condition, the associated value being an {@code Integer} that is
     *         the ID of the associated condition.
     */
    private Map<Condition, String> createCondToConditionIdMap(List<ConditionTO> conditionTOs) {
        log.entry(conditionTOs);
        return log
                .exit(conditionTOs.stream()
                        .collect(
                                Collectors.toMap(
                                        
                                        p -> new Condition(new AnatEntity(p.getAnatEntityId()),
                                                p.getStageId() == null ? null : new DevStage(p.getStageId()),
                                                        new Species(p.getSpeciesId())),
                                        p -> String.valueOf(p.getId()))));
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
        log.entry(directory);

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

                try (BgeePreparedStatement stmt = getManager().getConnection().prepareStatement(sql)) {
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
                                                    + "to empty String in the database"));
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
    
    private String dataPropagationToString(DataPropagation dataPropagation) {
        log.entry(dataPropagation);
        //TODO: to remove when the API can return all types of propagation
        if(dataPropagation == null) {
            return log.traceExit("");
        }
        EnumSet<PropagationState> allPropagationState = dataPropagation.getAllPropagationStates();
        if (dataPropagation.isIncludingObservedData()) {
            if (allPropagationState.contains(PropagationState.ALL) || 
                    allPropagationState.contains(PropagationState.ANCESTOR_AND_DESCENDANT) ||
                    (allPropagationState.contains(PropagationState.SELF_AND_ANCESTOR) && 
                            allPropagationState.contains(PropagationState.SELF_AND_DESCENDANT))) {
                return log.traceExit("all");
            } else if (allPropagationState.contains(PropagationState.SELF_AND_ANCESTOR)) {
                return log.traceExit("self and ancestor");
            } else if (allPropagationState.contains(PropagationState.SELF_AND_DESCENDANT)) {
                return log.traceExit("self and descendant");
            } else if (allPropagationState.contains(PropagationState.SELF)) {
            	return log.traceExit("self");
            }
        } else {
            if ( (allPropagationState.contains(PropagationState.ANCESTOR) && 
                    allPropagationState.contains(PropagationState.DESCENDANT)) ||
                    allPropagationState.contains(PropagationState.ANCESTOR_AND_DESCENDANT)) {
                return log.traceExit("ancestor and descendant");
            } else if (allPropagationState.contains(PropagationState.ANCESTOR)) {
                return log.traceExit("ancestor");
            } else if (allPropagationState.contains(PropagationState.DESCENDANT)) {
                return log.traceExit("descendant");
            }
        }
        throw log.throwing(new IllegalArgumentException("Unknown data propagation status  "
                + dataPropagation));
    }
}
