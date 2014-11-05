package org.bgee.pipeline.expression;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTOResultSet;
import org.bgee.model.dao.api.anatdev.StageDAO;
import org.bgee.model.dao.api.anatdev.StageDAO.StageTOResultSet;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTOResultSet;
import org.bgee.model.dao.api.expressiondata.ExpressionCallParams;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTOResultSet;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallParams;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTOResultSet;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.pipeline.BgeeDBUtils;
import org.bgee.pipeline.CommandRunner;
import org.bgee.pipeline.MySQLDAOUser;
import org.bgee.pipeline.Utils;
import org.supercsv.cellprocessor.constraint.IsElementOf;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
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
public class GenerateDownladFile extends MySQLDAOUser {
    
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
     * with RNA-Seq experiment, in the download file.
     */
    public final static String RNASEQDATA_COLUMN_NAME = "RNA-Seq data";

    /**
     * A {@code String} that is the name of the column containing merged expression/no-expression 
     * from different data types, in the download file.
     */
    public final static String EXPRESSION_COLUMN_NAME = "Expression/No-expression";

    /**
     * A {@code String} that is the name of the column containing merged differential expressions 
     * from different data types, in the download file.
     */
    public final static String DIFFEXPRESSION_COLUMN_NAME = "Over/Under-expression";

    /**
     * A {@code String} that is the argument class for generate presence/absence of expression 
     * download simple file.
     */
    public final static String EXPR_SIMPLE = "expr-simple";

    /**
     * A {@code String} that is the argument class for generate presence/absence of expression 
     * download complete file.
     */
    public final static String EXPR_COMPLETE = "expr-complete"; 

    /**
     * A {@code String} that is the argument class for generate differential expression 
     * download simple file.
     */
    public final static String DIFFEXPR_SIMPLE = "diffexpr-simple";

    /**
     * A {@code String} that is the argument class for generate differential expression 
     * download complete file.
     */
   public final static String DIFFEXPR_COMPLETE = "diffexpr-complete";

   /**
    * A {@code String} that is the extension of download files to be generated.
    */
   public final static String EXTENSION = ".tsv";

   /**
    * A {@code List} of {@code Object} containing possible elements for expression data, 
    * in the download file.
    */
   public final static List<Object> EXPRESSIONDATA = 
           Arrays.asList(new Object[] {
                   ExpressionData.NODATA.getStringRepresentation(),
                   ExpressionData.NOEXPRESSION.getStringRepresentation(),
                   ExpressionData.LOWEXPRESSION.getStringRepresentation(),
                   ExpressionData.HIGHEXPRESSION.getStringRepresentation(),
                   ExpressionData.AMBIGUOUS.getStringRepresentation()});
   /**
    * A {@code List} of {@code Object} containing possible elements for differential expression data, 
    * in the download file.
    */
   public final static List<Object> DIFFEXPRESSIONDATA = 
           Arrays.asList(new Object[] {
                   DiffExpressionData.NODATA.getStringRepresentation(),
                   DiffExpressionData.OVEREXPRESSED.getStringRepresentation(),
                   DiffExpressionData.UNDEREXPRESSED.getStringRepresentation(),
                   DiffExpressionData.NOTDIFFEXPRESSED.getStringRepresentation()});

   public enum DiffExpressionData {
       NODATA("no data"), OVEREXPRESSED("over-expression"), UNDEREXPRESSED("under-expression"), 
       NOTDIFFEXPRESSED("no diff expression");
       
       private final String stringRepresentation;
       
       /**
        * Constructor providing the {@code String} representation 
        * of this {@code DiffCallType}.
        * 
        * @param stringRepresentation  A {@code String} corresponding to 
        *                              this {@code DiffCallType}.
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
     * An {@code Enum} used to define, for each data type (Affymetrix, RNA-Seq, ...), the 
     * expression/no-expression of the call.
     * <ul>
     * <li>{@code NODATA}:         no data from the associated data type allowed to produce the call.
     * <li>{@code NOEXPRESSION}:   no-expression was detected from the associated data type.
     * <li>{@code LOWEXPRESSION}:  low expression was detected from the associated data type.
     * <li>{@code HIGHEXPRESSION}: high expression was detected from the associated data type.
     * <li>{@code AMBIGUOUS}:      different data types are not coherent (for instance, Affymetrix 
     *                             data reveals an low expression while <em>in situ</em> data 
     *                             reveals a no-expression).
     * </ul>
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public enum ExpressionData {
        NODATA("no data"), NOEXPRESSION("no-expression"), LOWEXPRESSION("low quality expression"), 
        HIGHEXPRESSION("high quality expression"), AMBIGUOUS("ambiguous");

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
     * <li>the directory path that will be used to generate download files. So 
     * it must finish with {@code /}
     * </ol>
     * 
     * @param args          An {@code Array} of {@code String}s containing the requested parameters.
     * @throws IOException  If some files could not be used.
     */
    public static void main(String[] args) throws IOException {
        log.entry((Object[]) args);

        // TODO Manage with multi-species!
        
        int expectedArgLengthSingleSpecies = 3; // species list and file types to be generated
        if (args.length != expectedArgLengthSingleSpecies) {
            throw log.throwing(new IllegalArgumentException(
                    "Incorrect number of arguments provided, expected " + 
                    expectedArgLengthSingleSpecies + " arguments, " + args.length + " provided."));
        }

        List<String> speciesIds = CommandRunner.parseListArgument(args[0]);
        
        List<String> fileTypes = CommandRunner.parseListArgument(args[1]);    
        
        String directory = args[2];
        
        GenerateDownladFile generate = new GenerateDownladFile();
        generate.generateSingleSpeciesFiles(speciesIds, fileTypes, directory);
        
        log.exit();
    }
    
    /**
     * Generate single species files according the given {@code List} of species IDs 
     * in the given directory. 
     * 
     * @param speciesIds    A {@code List} of {@code String}s that are the IDs of species for which
     *                      files are generated.
     * @param fileTypes     A {@code List} of {@code String}s containing file types to be generated.
     * @param directory     A {@code String} that is the directory path directory to store the 
     *                      generated files. 
     * @throws IOException  If an error occurred while trying to write generated files.
     */
    public void generateSingleSpeciesFiles(
            List<String> speciesIds, List<String> fileTypes, String directory) throws IOException { 
        log.entry(speciesIds, fileTypes, directory);
        
        //get all species in Bgee even if some species IDs were provided, 
        //to check user input.
        List<String> speciesIdsToUse = BgeeDBUtils.checkAndGetSpeciesIds(speciesIds, 
                this.getSpeciesDAO()); 

        List<String> allFileTypes = 
                Arrays.asList(EXPR_SIMPLE, EXPR_COMPLETE, DIFFEXPR_SIMPLE, DIFFEXPR_COMPLETE);
        if (fileTypes.isEmpty()) {
            // If no file types are given by user, we set all file types
            fileTypes = allFileTypes;
        } else if (!allFileTypes.containsAll(fileTypes)) {
            List<String> debugFileTypes = new ArrayList<String>(fileTypes);
            debugFileTypes.removeAll(allFileTypes);
            throw log.throwing(new IllegalArgumentException(
                    "Some file types could not be generated: " + debugFileTypes));
        }
        
        for (String fileType: fileTypes) {
            for (String speciesId: speciesIdsToUse) {
                List<Map<String, String>> dataExpression = null;
                if (fileTypes.contains(EXPR_SIMPLE) || fileTypes.contains(EXPR_COMPLETE)) {
                    dataExpression = this.loadExprDataFromDB(speciesId);
                } 
                
                List<Map<String, String>> dataDiffExpression = null;
                if (fileTypes.contains(DIFFEXPR_SIMPLE) || fileTypes.contains(DIFFEXPR_COMPLETE)) {
                    dataDiffExpression = this.loadDiffExprDataFromDB(speciesId);
                }

                if (fileType.equals(EXPR_SIMPLE)) {
                    this.createDownloadFiles(
                            dataExpression, 
                            directory + speciesId + "_" + EXPR_SIMPLE + EXTENSION, 
                            true, false);
                } else if (fileType.equals(EXPR_COMPLETE)) {
                    this.createDownloadFiles(
                            dataExpression, 
                            directory + speciesId + "_" + EXPR_COMPLETE + EXTENSION, 
                            false, false);
                } else if (fileType.equals(DIFFEXPR_SIMPLE)) {
                    this.createDownloadFiles(
                            dataDiffExpression, 
                            directory + speciesId + "_" + DIFFEXPR_SIMPLE + EXTENSION, 
                            true, true);
                } else if (fileType.equals(DIFFEXPR_COMPLETE)) {
                    this.createDownloadFiles(
                            dataDiffExpression, 
                            directory + speciesId + "_" + DIFFEXPR_COMPLETE + EXTENSION, 
                            false, true);
                }
            }
        }
        log.exit();
    }

    /**
     * Generate the {@code List} of {@code Map}s containing data to be written in download file
     * for a given species.
     * 
     * @param speciesId     A {@code String} that is the ID of species for which data are retrieved.
     * @return              A {@code List} of {@code Map}s where keys are column names and 
     *                      values are data associated to the column name.
     */
    private List<Map<String, String>> loadExprDataFromDB(String speciesId) {
        log.entry(speciesId);
        
        Set<String> speciesFilter = new HashSet<String>();
        speciesFilter.add(speciesId);

        List<Map<String, String>> dataList = new ArrayList<Map<String, String>>();
        
        List<String> nonInformativesAnatEntities = this.loadNonInformativeAnatEntities(speciesFilter);
        
        // Load expression
        List<ExpressionCallTO> exprTOs = this.loadGlobalExprCallsFromDb(speciesFilter);
                
        // Load no-expression
        List<NoExpressionCallTO> noExprTOs = this.loadGlobalNoExprCallsFromDb(speciesFilter);
        
        for (ExpressionCallTO exprTO : exprTOs) {
            if (!nonInformativesAnatEntities.contains(exprTO.getAnatEntityId())) {
                boolean isFound = false;
                for (NoExpressionCallTO noExprTO : noExprTOs) {
                    // TOs are retrieved without ID then comparison is done on gene, stage and 
                    // anatomical entity IDs.
                    if (exprTO.equals(noExprTO)) {
                        // Merge expression/no-expression
                        dataList.add(this.createDataMap(exprTO, noExprTO));
                        isFound = true;
                    }
                }
                if (!isFound) {
                    // Expression not in no-expression
                    dataList.add(this.createDataMap(exprTO, null));
                }
            }
        }
        
        for (NoExpressionCallTO noExprTO : noExprTOs) {
            if (!nonInformativesAnatEntities.contains(noExprTO.getAnatEntityId())) {
                for (ExpressionCallTO exprTO : exprTOs) {
                    // TOs are retrieved without ID then comparison is done on gene, stage and 
                    // anatomical entity IDs.
                    if (!exprTO.equals(noExprTO)) {
                        // No-expression not in expression
                        dataList.add(this.createDataMap(null, noExprTO));
                    }
                }
            }
        }
        
        // Add gene, stage and anatomical entity names
        this.addGeneNames(dataList, speciesId);
        this.addStageNames(dataList, speciesId);
        this.addAnatEntityNames(dataList, speciesId);

        return log.exit(dataList);
    }

    /**
     * Retrieves all global expression calls for given species, present into the Bgee database.
     * 
     * @param speciesIds        A {@code Set} of {@code String}s that are the IDs of species 
     *                          allowing to filter the global expression calls to use.
     * @return                  A {@code List} of {@code ExpressionCallTO}s containing all 
     *                          global expression calls of the given species.
     * @throws DAOException     If an error occurred while getting the data from the Bgee database.
     */
    private List<ExpressionCallTO> loadGlobalExprCallsFromDb(Set<String> speciesIds)
            throws DAOException {
        log.entry(speciesIds);

        log.info("Start retrieving global expression calls for the species IDs {}...", speciesIds);

        ExpressionCallDAO dao = this.getExpressionCallDAO();
        // We don't retrieve ID to be able to compare calls on gene, stage and anatomical IDs.
        // We don't need INCLUDESUBSTAGES and INCLUDESUBSTRUCTURES. 
        dao.setAttributes(ExpressionCallDAO.Attribute.GENEID, 
                ExpressionCallDAO.Attribute.STAGEID, ExpressionCallDAO.Attribute.ANATENTITYID, 
                ExpressionCallDAO.Attribute.AFFYMETRIXDATA, ExpressionCallDAO.Attribute.ESTDATA,
                ExpressionCallDAO.Attribute.INSITUDATA, ExpressionCallDAO.Attribute.RNASEQDATA);
        
        ExpressionCallParams params = new ExpressionCallParams();
        params.addAllSpeciesIds(speciesIds);
        params.setUseAnatDescendants(true);

        ExpressionCallTOResultSet rsGlobalExprCalls = dao.getExpressionCalls(params);

        List<ExpressionCallTO> globalExprTOs = rsGlobalExprCalls.getAllTOs();
        //no need for a try with resource or a finally, the insert method will close everything 
        //at the end in any case.
        // No need to close the ResultSet, it's done by getAllTOs().
        log.info("Done retrieving global expression calls, {} calls found", globalExprTOs.size());

        return log.exit(globalExprTOs);        
    }

    /**
     * Retrieves all global no-expression calls for given species, present into the Bgee database.
     * 
     * @param speciesIds        A {@code Set} of {@code String}s that are the IDs of species 
     *                          allowing to filter the global no-expression calls to use.
     * @return                  A {@code List} of {@code NoExpressionCallTO}s containing all 
     *                          global no-expression calls of the given species.
     * @throws DAOException     If an error occurred while getting the data from the Bgee database.
     */
    private List<NoExpressionCallTO> loadGlobalNoExprCallsFromDb(Set<String> speciesIds) 
            throws DAOException {
        log.entry(speciesIds);

        log.info("Start retrieving global no-expression calls for the species IDs {}...", speciesIds);

        NoExpressionCallDAO dao = this.getNoExpressionCallDAO();
        // We don't retrieve ID to be able to compare calls on gene, stage and anatomical IDs.
        // We don't need INCLUDEPARENTSTRUCTURES and ORIGINOFLINE. 
        dao.setAttributes(NoExpressionCallDAO.Attribute.GENEID, 
                NoExpressionCallDAO.Attribute.DEVSTAGEID, NoExpressionCallDAO.Attribute.ANATENTITYID, 
                NoExpressionCallDAO.Attribute.AFFYMETRIXDATA, 
                NoExpressionCallDAO.Attribute.INSITUDATA, NoExpressionCallDAO.Attribute.RNASEQDATA);

        NoExpressionCallParams params = new NoExpressionCallParams();
        params.addAllSpeciesIds(speciesIds);
        params.setIncludeParentStructures(true);

        NoExpressionCallTOResultSet rsGlobalNoExprCalls = dao.getNoExpressionCalls(params);

        List<NoExpressionCallTO> globalNoExprTOs = rsGlobalNoExprCalls.getAllTOs();
        //no need for a try with resource or a finally, the insert method will close everything 
        //at the end in any case.
        // No need to close the ResultSet, it's done by getAllTOs().
        log.info("Done retrieving global no-expression calls, {} calls found",
                globalNoExprTOs.size());

        return log.exit(globalNoExprTOs);        
    }
    
    /**
     * Retrieves non-informative anatomical entities for given species, 
     * present into the Bgee database.
     * 
     * @param speciesIds        A {@code Set} of {@code String}s that are the IDs of species 
     *                          allowing to filter the non-informative anatomical entities to use.
     * @return                  A {@code List} of {@code String}s containing all 
     *                          non-informative anatomical entities of the given species.
     * @throws DAOException     If an error occurred while getting the data from the Bgee database.
     */
    private List<String> loadNonInformativeAnatEntities(Set<String> speciesIds) 
            throws DAOException {
        log.entry(speciesIds);

        log.info("Start retrieving non-informative anatomical entities for the species IDs {}...",
                speciesIds);

        AnatEntityDAO dao = this.getAnatEntityDAO();
        dao.setAttributes(AnatEntityDAO.Attribute.ID);
        
        List<String> anatEntities = new ArrayList<String>();
        try (AnatEntityTOResultSet rs = dao.getNonInformativeAnatEntities(speciesIds)) {
            while (rs.next()) {
                anatEntities.add(rs.getTO().getId());
            }
        }
        
        log.info("Done retrieving non-informative anatomical entities, {} entities found",
                anatEntities.size());

        return log.exit(anatEntities);        
    }


    /**
     * Creates a {@code Map} associating file column names with data associated to the column name
     * merging an {@code ExpressionCallTO} with a {@code NoExpressionCallTO}.
     * 
     * @param expressionCallTO      An {@code ExpressionCallTO} that is an expression call.
     * @param noExpressionCallTO    A {@code NoExpressionCallTO}
     * @return
     */
    private Map<String, String> createDataMap(
            ExpressionCallTO expressionCallTO, NoExpressionCallTO noExpressionCallTO) {
        log.entry(expressionCallTO, expressionCallTO);
        
        Map<String, String> row = new HashMap<String, String>();
    
        if (expressionCallTO != null && noExpressionCallTO != null) {
            row.put(AFFYMETRIXDATA_COLUMN_NAME, this.mergeDataStatesInExprData(
                    expressionCallTO.getAffymetrixData(), noExpressionCallTO.getAffymetrixData()));
            row.put(INSITUDATA_COLUMN_NAME, this.mergeDataStatesInExprData(
                    expressionCallTO.getInSituData(), noExpressionCallTO.getInSituData()));
            row.put(RNASEQDATA_COLUMN_NAME, this.mergeDataStatesInExprData(
                    expressionCallTO.getRNASeqData(), noExpressionCallTO.getRNASeqData()));
        } else if (expressionCallTO == null) {
            row.put(AFFYMETRIXDATA_COLUMN_NAME, noExpressionCallTO.getAffymetrixData().getStringRepresentation());
            row.put(ESTDATA_COLUMN_NAME, ExpressionData.NODATA.getStringRepresentation());
            row.put(INSITUDATA_COLUMN_NAME, noExpressionCallTO.getInSituData().getStringRepresentation());
            row.put(RNASEQDATA_COLUMN_NAME, noExpressionCallTO.getRNASeqData().getStringRepresentation());
        } else {
            row.put(AFFYMETRIXDATA_COLUMN_NAME, expressionCallTO.getAffymetrixData().getStringRepresentation());
            row.put(ESTDATA_COLUMN_NAME, expressionCallTO.getESTData().getStringRepresentation());
            row.put(INSITUDATA_COLUMN_NAME, expressionCallTO.getInSituData().getStringRepresentation());
            row.put(RNASEQDATA_COLUMN_NAME, expressionCallTO.getRNASeqData().getStringRepresentation());            
        }
        
        if (expressionCallTO != null) {
            row.put(GENE_ID_COLUMN_NAME, expressionCallTO.getGeneId());
            row.put(STAGE_ID_COLUMN_NAME, expressionCallTO.getStageId());
            row.put(ANATENTITY_ID_COLUMN_NAME, expressionCallTO.getAnatEntityId());
        } else {
            row.put(GENE_ID_COLUMN_NAME, noExpressionCallTO.getGeneId());
            row.put(STAGE_ID_COLUMN_NAME, noExpressionCallTO.getStageId());
            row.put(ANATENTITY_ID_COLUMN_NAME, noExpressionCallTO.getAnatEntityId());
        }        
        return log.exit(row);
    }

    /**
     * Adds gene names in given {@code List} of {@code Map}s where keys are column names and 
     * values are data associated to the column name.
     * <p>
     * The provided {@code List} will be modified.
     * 
     * @param inputList     A {@code List} of {@code Map}s where keys are column names and 
     *                      values are data associated to the column name.
     * @param speciesId     A {@code String} that is the ID of species allowing to filter 
     *                      the genes to use.
     * @throws DAOException If an error occurred while getting the data from the Bgee database.
     */
    private void addGeneNames(List<Map<String, String>> list, String speciesId) throws DAOException {
        log.entry(list, speciesId);
        
        log.info("Start retrieving gene names...");

        GeneDAO dao = this.getGeneDAO();
        dao.setAttributes(GeneDAO.Attribute.ID, GeneDAO.Attribute.NAME);
        
        Set<String> speciesFilter = new HashSet<String>();
        speciesFilter.add(speciesId);

        GeneTOResultSet rsGenes = dao.getGenes(speciesFilter);
        
        while (rsGenes.next()) {
            for (Map<String, String> map : list) {
                if (map.get(GENE_ID_COLUMN_NAME).equals(rsGenes.getTO().getId())) {
                    map.put(GENE_NAME_COLUMN_NAME, rsGenes.getTO().getName());
                }
            }
        }
        rsGenes.close();
        log.info("Done retrieving gene names");
    
        log.exit();        
    }

    /**
     * Adds stage names in given {@code List} of {@code Map}s where keys are column names and 
     * values are data associated to the column name.
     * <p>
     * The provided {@code List} will be modified.
     * 
     * @param inputList     A {@code List} of {@code Map}s where keys are column names and 
     *                      values are data associated to the column name.
     * @param speciesId     A {@code String} that is the ID of species allowing to filter 
     *                      the stages to use.
     * @throws DAOException If an error occurred while getting the data from the Bgee database.
     */
    private void addStageNames(List<Map<String, String>> list, String species) throws DAOException {
        log.entry(list, species);
        
        log.info("Start retrieving stage names...");

        StageDAO dao = this.getStageDAO();
        dao.setAttributes(StageDAO.Attribute.ID, StageDAO.Attribute.NAME);
        
        Set<String> speciesFilter = new HashSet<String>();
        speciesFilter.add(species);

        StageTOResultSet rsStages = dao.getStages(speciesFilter);
        
        while (rsStages.next()) {
            for (Map<String, String> map : list) {
                if (map.get(STAGE_ID_COLUMN_NAME).equals(rsStages.getTO().getId())) {
                    map.put(STAGE_NAME_COLUMN_NAME, rsStages.getTO().getName());
                }
            }
        }
        rsStages.close();
        log.info("Done retrieving stage names");
    
        log.exit();        
    }
    
    /**
     * Adds anatomical entity names in given {@code List} of {@code Map}s where keys are column 
     * names and values are data associated to the column name.
     * <p>
     * The provided {@code List} will be modified.
     * 
     * @param inputList     A {@code List} of {@code Map}s where keys are column names and 
     *                      values are data associated to the column name.
     * @param speciesId     A {@code String} that is the ID of species allowing to filter 
     *                      the anatomical entities to use.
     * @throws DAOException If an error occurred while getting the data from the Bgee database.
     */
    private void addAnatEntityNames(List<Map<String, String>> list, String species) throws DAOException {
        log.entry(list, species);
        
        log.info("Start retrieving anatomical entity names...");

        AnatEntityDAO dao = this.getAnatEntityDAO();
        dao.setAttributes(AnatEntityDAO.Attribute.ID, AnatEntityDAO.Attribute.NAME);
        
        Set<String> speciesFilter = new HashSet<String>();
        speciesFilter.add(species);

        AnatEntityTOResultSet rsAnatEntities = dao.getAnatEntities(speciesFilter);
        
        while (rsAnatEntities.next()) {
            for (Map<String, String> map : list) {
                if (map.get(ANATENTITY_ID_COLUMN_NAME).equals(rsAnatEntities.getTO().getId())) {
                    map.put(ANATENTITY_NAME_COLUMN_NAME, rsAnatEntities.getTO().getName());
                }
            }
        }
        rsAnatEntities.close();
        log.info("Done retrieving anatomical entity names");
    
        log.exit();        
    }

    /**
     * Write the download TSV files (simple and complete files). The data are provided 
     * by a {@code List} of {@code Map}s where keys are column names and values are data associated 
     * to the column name. 
     * <p>
     * The generated TSV file will have one header line. Both files do not have the same headers:
     * in the simple file, expression/no-expression from different data types are merged in a single
     * column called Expression/No-expression.
     * 
     * @param inputList         A {@code List} of {@code Map}s where keys are column names and 
     *                          values are data associated to the column name.
     * @param outputFile        A {@code String} that is the path to the simple output file
     *                          were data will be written as TSV.
     * @param isSimplifiedFile  A {@code boolean} defining whether the output file is a simple file.
     *                          If {code true}, data from different data types are merged
     *                          in a single column.
     * @param isDiffExpr        A {@code boolean} defining whether the output file contains 
     *                          expression/no-expression data or differential expression data.
     *                          If {code true}, output file will contain differential expression 
     *                          data.
     * @throws IOException      If an error occurred while trying to write the {@code outputFile}.
     */
    // TODO set to private when tests are modified
    public void createDownloadFiles(List<Map<String, String>> inputList, String outputFile, 
            boolean isSimplifiedFile, boolean isDiffExpr) throws IOException {
        log.entry(inputList, outputFile, isSimplifiedFile, isDiffExpr);
                
        CellProcessor[] processorFile =
                generateCellProcessor(isSimplifiedFile, isDiffExpr);
        
        final String[] headerFile;
        if (isSimplifiedFile) {
            if (isDiffExpr) {
                headerFile= new String[] { 
                        GENE_ID_COLUMN_NAME, GENE_NAME_COLUMN_NAME, 
                        STAGE_ID_COLUMN_NAME, STAGE_NAME_COLUMN_NAME,
                        ANATENTITY_ID_COLUMN_NAME, ANATENTITY_NAME_COLUMN_NAME,
                        DIFFEXPRESSION_COLUMN_NAME};
            } else {
                headerFile= new String[] { 
                        GENE_ID_COLUMN_NAME, GENE_NAME_COLUMN_NAME, 
                        STAGE_ID_COLUMN_NAME, STAGE_NAME_COLUMN_NAME,
                        ANATENTITY_ID_COLUMN_NAME, ANATENTITY_NAME_COLUMN_NAME,
                        EXPRESSION_COLUMN_NAME};
            }
        } else {
            headerFile = new String[] {
                    GENE_ID_COLUMN_NAME, GENE_NAME_COLUMN_NAME, 
                    STAGE_ID_COLUMN_NAME, STAGE_NAME_COLUMN_NAME,
                    ANATENTITY_ID_COLUMN_NAME, ANATENTITY_NAME_COLUMN_NAME,
                    AFFYMETRIXDATA_COLUMN_NAME, ESTDATA_COLUMN_NAME, 
                    INSITUDATA_COLUMN_NAME, RNASEQDATA_COLUMN_NAME};
        }
            
        writeFileContent(inputList, outputFile, headerFile, processorFile,
                isSimplifiedFile, isDiffExpr);

        log.exit();
    }
    
    /**
     * Write a download TSV file according to the data are provided by a {@code List} of 
     * {@code Map}s where keys are column names and values are data associated to the column name, 
     * the given output file path, headers, cell processors and {@code boolean} defining whether 
     * it is a simple download file.
     * <p>
     * If the {@code isSimplifiedFile} is {code true}, expression data from different data types 
     * are merged in a single column called Expression.
     * 
     * @param inputList         A {@code List} of {@code Map}s where keys are column names and 
     *                          values are data associated to the column name. 
     * @param outputFile        A {@code String} that is the path to the output file
     *                          were data will be written as TSV.
     * @param headers           An {@code Array} of {@code String}s containing headers of the file.
     * @param processors        An {@code Array} of {@code CellProcessor}s containing cell 
     *                          processors which automates the data type conversions, and enforce 
     *                          column constraints.
     * @param isSimplifiedFile  A {@code boolean} defining whether the output file is a simple file.
     *                          If {code true}, data from different data types are merged
     *                          in a single column.
     * @param isDiffExpr        A {@code boolean} defining whether the output file contains 
     *                          expression/no-expression data or differential expression data.
     *                          If {code true}, output file will contain differential expression 
     *                          data.
     * @throws IOException      If an error occurred while trying to write {@code outputFile}.
     */
    private void writeFileContent(List<Map<String, String>> inputList, String outputFile, 
            String[] headers, CellProcessor[] processors, 
            boolean isSimplifiedFile, boolean isDiffExpr) throws IOException {
        log.entry(inputList, outputFile, headers, processors, isSimplifiedFile, isDiffExpr);
        
        try (ICsvMapWriter mapWriter = new CsvMapWriter(new FileWriter(outputFile),
                Utils.TSVCOMMENTED)) {
            
            mapWriter.writeHeader(headers);
            
            for (Map<String, String> map: inputList) {
                Map<String, Object> row = new HashMap<String, Object>();
                if (isSimplifiedFile) {
                    if (isDiffExpr) {
                        String mergedData = this.mergeDiffExprData(
                                map.get(AFFYMETRIXDATA_COLUMN_NAME), 
                                map.get(RNASEQDATA_COLUMN_NAME));
                        row.put(DIFFEXPRESSION_COLUMN_NAME, mergedData);
                    } else {
                        String mergedData = this.mergeExprData(
                                map.get(AFFYMETRIXDATA_COLUMN_NAME), 
                                map.get(ESTDATA_COLUMN_NAME), 
                                map.get(INSITUDATA_COLUMN_NAME), 
                                map.get(RNASEQDATA_COLUMN_NAME));
                        row.put(EXPRESSION_COLUMN_NAME, mergedData);
                    }
                }
                for (String key : map.keySet()) {
                    if (!isSimplifiedFile ||
                            (!key.equals(AFFYMETRIXDATA_COLUMN_NAME) &&
                             !key.equals(ESTDATA_COLUMN_NAME) &&
                             !key.equals(INSITUDATA_COLUMN_NAME) && 
                             !key.equals(RNASEQDATA_COLUMN_NAME))) {
                        row.put(key, map.get(key));
                    }
                }
                mapWriter.write(row, headers, processors);
            }
        }
        
        log.exit();
    }

    /**
     * Generate a {@code CellProcessor} needed to write a download file. 
     * 
     * @param isSimplifiedFile  A {@code boolean} defining whether the output file is a simple file.
     *                          If {code true}, data from different data types are merged
     *                          in a single column.
     * @param isDiffExpr        A {@code boolean} defining whether the output file contains 
     *                          expression/no-expression data or differential expression data.
     *                          If {code true}, output file will contain differential expression 
     *                          data.
     * @return                  A {@code CellProcessor} needed to write a simple or complete 
     *                          download file.
     */
    public static CellProcessor[] generateCellProcessor(boolean isSimplifiedFile, boolean isDiffExpr) {
        log.entry(isSimplifiedFile, isDiffExpr);
        
        List<Object> elements = EXPRESSIONDATA;
        if (isDiffExpr) {
            elements = DIFFEXPRESSIONDATA;
        }
        
        final CellProcessor[] processors;
        if (isSimplifiedFile) {
                processors = new CellProcessor[] { 
                        new UniqueHashCode(new NotNull()), // gene ID (must be unique)
                        new NotNull(), // gene Name
                        new NotNull(), // developmental stage ID
                        new NotNull(), // developmental stage name
                        new NotNull(), // anatomical entity ID
                        new NotNull(), // anatomical entity name
                        new IsElementOf(elements)}; // Differential expression or Expression/No-expression
        } else {
                processors = new CellProcessor[] { 
                        new UniqueHashCode(new NotNull()), // gene ID (must be unique)
                        new NotNull(), // gene Name
                        new NotNull(), // developmental stage ID
                        new NotNull(), // developmental stage name
                        new NotNull(), // anatomical entity ID
                        new NotNull(), // anatomical entity name
                        new IsElementOf(elements),  // Affymetrix data
                        new IsElementOf(elements),  // EST data
                        new IsElementOf(elements),  // In Situ data
                        new IsElementOf(elements)}; // RNA-seq data
        }
        return log.exit(processors);
    }

    /**
     * Merge expression data from different data types.
     * 
     * @param affymetrixData    A {@code String} that is the expression/no-expression data from 
     *                          Affymetrix experiment.
     * @param estData           A {@code String} that is the expression/no-expression data from 
     *                          EST experiment.
     * @param inSituData        A {@code String} that is the expression/no-expression data from 
     *                          <em> in situ</em> experiment.
     * @param rnaSeqData        A {@code String} that is the expression/no-expression data from
     *                          RNA-seq experiment.
     * @return                  A {@code String} that is the merged expression/no-expression data. 
     */
    private String mergeExprData(
            String affymetrixData, String estData, String inSituData, String rnaSeqData) {
        log.entry(affymetrixData, estData, inSituData, rnaSeqData);
        
        Set<String> allData = new HashSet<String>(
                Arrays.asList(affymetrixData, estData, inSituData, rnaSeqData));
        
        if (allData.contains(ExpressionData.NOEXPRESSION.getStringRepresentation()) &&
                (allData.contains(ExpressionData.LOWEXPRESSION.getStringRepresentation()) ||
                 allData.contains(ExpressionData.HIGHEXPRESSION.getStringRepresentation()))) {
            // No-expression AND expression from different data types
            return log.exit(ExpressionData.AMBIGUOUS.getStringRepresentation());
        } else if (allData.contains(ExpressionData.HIGHEXPRESSION.getStringRepresentation())) {
            // At least one 'high expression' (with or without 'low expression' and/or 'no data')
            return log.exit(ExpressionData.HIGHEXPRESSION.getStringRepresentation());
        } else if (allData.contains(ExpressionData.LOWEXPRESSION.getStringRepresentation())) {
            // At least one 'low expression' (with or without 'no data')
            return log.exit(ExpressionData.LOWEXPRESSION.getStringRepresentation());
        } else if (allData.contains(ExpressionData.NOEXPRESSION.getStringRepresentation())) {
            // At least one 'no-expression' (with or without 'no data')
            return log.exit(ExpressionData.NOEXPRESSION.getStringRepresentation());
        }
        // Only 'no data'        
        return log.exit(ExpressionData.NODATA.getStringRepresentation());
    }

    /**
     * Merge {@code DataState}s of one expression call and one no-expression call into a 
     * {@code String} representation of an {@code ExpressionData}. 
     * 
     * @param dataStateExpr     A {@code DataState} from an expression call. 
     * @param dataStateNoExpr   A {@code DataState} from a no-expression call.
     * @return                  A {@code String} combining {@code DataState}s of one expression call
     *                          and one no-expression call. 
     */
    private String mergeDataStatesInExprData(DataState dataStateExpr, DataState dataStateNoExpr) {
        log.entry(dataStateExpr, dataStateNoExpr);

        if (dataStateExpr == dataStateNoExpr) {
            return log.exit(convertDataStateToExprData(dataStateExpr).getStringRepresentation());
        }
        if (dataStateExpr == DataState.NODATA) {
            return log.exit(convertDataStateToExprData(dataStateNoExpr).getStringRepresentation());
        }
        if (dataStateNoExpr == DataState.NODATA) {
            return log.exit(convertDataStateToExprData(dataStateExpr).getStringRepresentation());
        }
        return log.exit(ExpressionData.AMBIGUOUS.getStringRepresentation());
    }
    
    /**
     * Converts a {@code DataState} into an {@code ExpressionData}.
     * 
     * @param dataState A {@code DataState} to be converted. 
     * @return          An {@code ExpressionData} corresponding to the given {@code DataState}.
     */
    private ExpressionData convertDataStateToExprData(DataState dataState) {
        log.entry(dataState);
        
        switch (dataState) {
            case NODATA:
                return log.exit(ExpressionData.NODATA);
            case LOWQUALITY:
                return log.exit(ExpressionData.LOWEXPRESSION);
            case HIGHQUALITY:
                return log.exit(ExpressionData.HIGHEXPRESSION);
            default:
                throw log.throwing(new IllegalArgumentException("The DataState " + 
                        dataState.getStringRepresentation() + "doesn't exist in Bgee" ));
        }
    }

    /**
     * Generate the {@code List} of {@code Map}s containing data to be written in download file
     * for differential expression.
     * 
     * @param speciesId     A {@code String} that is the ID of species for which data are retrieved.
     * @return              A {@code List} of {@code Map}s where keys are column names and 
     *                      values are data associated to the column name.
     */
    private List<Map<String, String>> loadDiffExprDataFromDB(String speciesId) {
        log.entry(speciesId);
        
        // TODO Auto-generated method stub
    
        return log.exit(null);
    }

    /**
     * Merge differential expression data from different data types.
     * 
     * @param affymetrixData    A {@code String} that is the differential expression data from 
     *                          Affymetrix experiment.
     * @param rnaSeqData        A {@code String} that is the differential expression data from
     *                          RNA-seq experiment.
     * @return                  A {@code String} that is the merged differential expression data. 
     */
    private String mergeDiffExprData(String affymetrixData, String rnaSeqData) {
        log.entry(affymetrixData, rnaSeqData);
        
        if (affymetrixData.equals(rnaSeqData)) {
            return log.exit(affymetrixData);            
        } else if (affymetrixData.equals(DiffExpressionData.NODATA.getStringRepresentation())) {
            return log.exit(rnaSeqData);
        }
        // TODO is it possible to have over and under ? 
        return log.exit(affymetrixData);
    }
}
