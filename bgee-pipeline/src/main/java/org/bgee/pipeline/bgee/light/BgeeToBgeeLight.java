package org.bgee.pipeline.bgee.light;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO;
import org.bgee.model.dao.api.anatdev.StageDAO;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.ConditionDAO.ConditionTO;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTOResultSet;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.species.Species;
import org.bgee.pipeline.CommandRunner;
import org.bgee.pipeline.MySQLDAOUser;
import org.bgee.pipeline.Utils;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.io.ICsvMapReader;


/**
 * Extract data from the Bgee database and generate one TSV file for each extracted table.
 * These TSV files will then be used to populate the bgee light database (initially created for the bioSoda project)
 * @author jwollbrett
 *
 */
public class BgeeToBgeeLight extends MySQLDAOUser{
        
    private enum TsvFile {
        SPECIES_OUTPUT_FILE("species_bgee_light.tsv", "species", "{GENOME_VERSION=genomeVersion, GENOME_SPECIES_ID=genomeSpeciesId, ID=speciesId, "
                + "COMMON_NAME=speciesCommonName, GENUS=genus, SPECIES_NAME=species}"),
        GENE_OUTPUT_FILE("genes_bgee_light.tsv", "gene", "{ID=bgeeGeneId, SPECIES_ID=speciesId, DESCRIPTION=geneDescription, "
                + "ENSEMBL_ID=geneId, NAME=geneName}"),
        ANATENTITY_OUTPUT_FILE("anat_entities_bgee_light.tsv", "anatEntity", "{ID=anatEntityId, DESCRIPTION=anatEntityDescription, "
                + "NAME=anatEntityName}"),
        DEVSTAGE_OUTPUT_FILE("dev_stages_bgee_light.tsv", "stage", "{ID=stageId, DESCRIPTION=stageDescription, "
                + "NAME=stageName}"), 
        GLOBALCOND_OUTPUT_FILE("global_cond_bgee_light.tsv", "globalCond", "{ID=globalConditionId, SPECIES_ID=speciesId, ANAT_ENTITY_ID=anatEntityId, "
                + "STAGE_ID=stageId}"),
        GLOBALEXPRESSION_OUTPUT_FILE("global_expression_bgee_light.tsv", "globalExpression", "{BGEE_GENE_ID=bgeeGeneId, CONDITION_ID=globalConditionId, SUMMARY_QUALITY=summaryQuality}");

        
        private String fileName = "";
        private String tableName = "";
        private Map<String, String> columnMapping = new HashMap<>();
        
        TsvFile(String fileName, String tableName, String columnMapping){
          this.fileName = fileName;
          this.tableName = tableName;
          this.columnMapping = getColumnMapping(columnMapping);
          
        }
        
        private Map<String, String> getColumnMapping(String StringRepOfMap){
            Properties props = new Properties();
            try {
                props.load(new StringReader(StringRepOfMap.substring(1, StringRepOfMap.length() - 1).replace(", ", "\n")));
            } catch (IOException e1) {
                throw log.throwing(new IllegalStateException("Can't access to Map representation of the Mapping"));
            }       
            Map<String, String> map2 = new HashMap<String, String>();
            for (Map.Entry<Object, Object> e : props.entrySet()) {
                map2.put((String)e.getKey(), (String)e.getValue());
            }
            return map2;
        }

    }

    private static String GLOBAL_EXPRESSION_SUMMARY_QUALITY = "SUMMARY_QUALITY";
    private final static Logger log = LogManager.getLogger(BgeeToBgeeLight.class);
    protected final ServiceFactory serviceFactory = new ServiceFactory();
    private String outputDirectory;
    private final GeneDAO geneDAO;
    private final SpeciesDAO speciesDAO;
    private final AnatEntityDAO anatEntityDAO;
    private final ConditionDAO conditionDAO;
    
    /**
     * Main method to export data from the Bgee database (see {@link #extractBgeeDatabase(Collection)})
     * to tsv files OR to import exported tsv files to Bgee light (see {@link #importToBgeeLight()})
     * Parameters that must be provided for {@link extractBgeeDatabase(Collection)} in order in {@code args} are: 
     * <ol>
     * <li> path to the output directory,
     * <li> a list of NCBI species IDs (for instance, {@code 9606} for human) that will be used to
     * extract data, separated by the {@code String} {@link CommandRunner#LIST_SEPARATOR}.
     * If empty (see {@link CommandRunner#EMPTY_LIST}), all species in database will be exported.
     * </ol>
     * Parameters that must be provided for {@link #importToBgeeLight()} in order in {@code args} are: 
     * <ol>
     * <li> path to the output directory containing all tsv files,
     * </ol>
     * 
     * @param args           An {@code Array} of {@code String}s containing the requested parameters.
     * @throws Exception 
     */
    public static void main(String[] args){
        log.entry((Object[]) args);
        if(args[0].equals("extractFromBgee")){
            int expectedArgLength = 3;
            if (args.length != expectedArgLength) {
                throw log.throwing(new IllegalArgumentException(
                        "Incorrect number of arguments provided, expected " + 
                        expectedArgLength + " arguments, " + args.length + " provided."));
            }
            BgeeToBgeeLight bgeeToBgeeLight = new BgeeToBgeeLight(args[1]);
            bgeeToBgeeLight.cleanOutputDir();
            bgeeToBgeeLight.extractBgeeDatabase(CommandRunner.parseListArgumentAsInt(args[2]));
        }
        else if(args[0].equals("tsvToBgeeLight")){
            int expectedArgLength = 2;
            if (args.length != expectedArgLength) {
                throw log.throwing(new IllegalArgumentException(
                        "Incorrect number of arguments provided, expected " + 
                        expectedArgLength + " arguments, " + args.length + " provided."));
            }
            BgeeToBgeeLight bgeeToBgeeLight = new BgeeToBgeeLight(args[1]);
            bgeeToBgeeLight.tsvToBgeeLight();
        }else if(args[0].equals("emptyDatabaseTables")){
            int expectedArgLength = 2;
            if (args.length != expectedArgLength) {
                throw log.throwing(new IllegalArgumentException(
                        "Incorrect number of arguments provided, expected " + 
                        expectedArgLength + " arguments, " + args.length + " provided."));
            }
            BgeeToBgeeLight bgeeToBgeeLight = new BgeeToBgeeLight();
            bgeeToBgeeLight.emptyDatabaseTables();
        }else{
            throw log.throwing(new IllegalArgumentException(args[0] + " is not recognized as an action"));
        }
    }
    
    public BgeeToBgeeLight(String outputDirectory){
        this.outputDirectory = outputDirectory;
        this.geneDAO = serviceFactory.getDAOManager().getGeneDAO();
        this.speciesDAO = serviceFactory.getDAOManager().getSpeciesDAO();
        this.anatEntityDAO = serviceFactory.getDAOManager().getAnatEntityDAO();
        this.conditionDAO = serviceFactory.getDAOManager().getConditionDAO();
    }
    
    public BgeeToBgeeLight(){
        this.geneDAO = serviceFactory.getDAOManager().getGeneDAO();
        this.speciesDAO = serviceFactory.getDAOManager().getSpeciesDAO();
        this.anatEntityDAO = serviceFactory.getDAOManager().getAnatEntityDAO();
        this.conditionDAO = serviceFactory.getDAOManager().getConditionDAO();
    }
    
    /**
     * Method used to clean the output directory. If output ".tsv" files exist they are deleted.
     * This Method also create the output directory if it does not already exist.
     */
    private void cleanOutputDir(){
        File dir = new File(outputDirectory);
        dir.mkdir();
        for(TsvFile fileName:TsvFile.values()){
            File file = new File(outputDirectory+fileName.fileName);
            if(file.exists()){
                file.delete();
            }
        }
    }
    
    /**
     * extract to an intermediate TSV file data contained in wanted Bgee tables.
     * @param speciesIds 
     */
    private void extractBgeeDatabase(Collection <Integer> speciesIds) {
        log.entry(speciesIds);
        
        SpeciesTOResultSet speciesTOs = this.speciesDAO.getSpeciesByIds(new HashSet<Integer>(speciesIds));
        extractSpeciesTable(speciesTOs);
        Set<Species> speciesSet = serviceFactory.getSpeciesService().loadSpeciesByIds(speciesIds, false);
        extractAnatEntityTable();
        extractStadeTable();
        for (Species species:speciesSet){
            log.info("start to export data for species {}",species.getId());
            Map <String, Integer> ensemblIdToBgeeGeneId= extractGeneTable(species.getId());
            Map<String, Integer>condUniqKeyToConditionId = extractGlobalCondTable(species.getId());
            extractGlobalExpressionTable(ensemblIdToBgeeGeneId, condUniqKeyToConditionId, species);
        }
    }
    
    private void extractGlobalExpressionTable(Map<String, Integer> ensemblIdToBgeeGeneId, Map<String, Integer> condUniqKeyToConditionId,
            Species species){
       log.entry(ensemblIdToBgeeGeneId, condUniqKeyToConditionId, species);
       Map<Integer, Set<String>> requestedSpeToGeneIdsMap = new HashMap<>();
       requestedSpeToGeneIdsMap.put(species.getId(), ensemblIdToBgeeGeneId.keySet());
       log.debug("Start extracting global expressions for the species {}...", species.getId());
        String [] header = new String[] {GlobalExpressionCallDAO.Attribute.BGEE_GENE_ID.name(),
                GlobalExpressionCallDAO.Attribute.CONDITION_ID.name(), GLOBAL_EXPRESSION_SUMMARY_QUALITY};
        Map<SummaryCallType.ExpressionSummary, SummaryQuality> silverExpressedCallFilter = new HashMap<>();
        silverExpressedCallFilter.put(ExpressionSummary.EXPRESSED, SummaryQuality.SILVER);
        Map<CallType.Expression, Boolean> obsDataFilter = new HashMap<>();
        obsDataFilter.put(CallType.Expression.EXPRESSED, true);
        final Set<List<String>> CallsInformation =  serviceFactory.getCallService().loadExpressionCalls(
                new ExpressionCallFilter(silverExpressedCallFilter,
                        Collections.singleton(new GeneFilter(species.getId(), ensemblIdToBgeeGeneId.keySet())),
                        null, null, obsDataFilter, null, null),
                EnumSet.of(CallService.Attribute.GENE, CallService.Attribute.ANAT_ENTITY_ID,
                        CallService.Attribute.DEV_STAGE_ID, CallService.Attribute.DATA_QUALITY), 
                           //CallService.Attribute.GLOBAL_MEAN_RANK),
                new LinkedHashMap<>())
            .map(c -> {
            return Arrays.asList(ensemblIdToBgeeGeneId.get(c.getGene().getEnsemblGeneId()).toString(),
                    condUniqKeyToConditionId.get(c.getCondition().getAnatEntityId()
                            + "_" + c.getCondition().getDevStageId()).toString(),
                    c.getSummaryQuality().toString()
                    
                    );
        }).collect(Collectors.toSet());
        final CellProcessor[] processors = new CellProcessor[] {new Optional(), new Optional(), new NotNull()};
        
        File file = new File(outputDirectory + TsvFile.GLOBALEXPRESSION_OUTPUT_FILE.fileName);
        try {
            this.writeOutputFile(file, CallsInformation, header, processors);
        } catch (IOException e) {
            throw new UncheckedIOException("Can't write file "+file, e);
        }

    }
    
    private  Map<String, Integer> extractGeneTable(Integer speciesId){
        log.entry(speciesId);
        log.debug("Start extracting genes for the species {}...", speciesId);
        String [] header = new String[] { GeneDAO.Attribute.ID.name(), GeneDAO.Attribute.ENSEMBL_ID.name(),
                GeneDAO.Attribute.NAME.name(), GeneDAO.Attribute.DESCRIPTION.name(),
                GeneDAO.Attribute.SPECIES_ID.name()};
        Set<List<String>> allGenesInformation = this.geneDAO
                .getGenesBySpeciesIds(Collections.singleton(speciesId))
                .getAllTOs().stream().map(s -> {
                    return Arrays.asList(s.getId().toString(), s.getGeneId(), s.getName(), s.getDescription(), 
                    s.getSpeciesId().toString());
                }).collect(Collectors.toSet());
        final CellProcessor[] processors = new CellProcessor[] { new ParseInt(), new Optional(), new Optional(),
                new NotNull(), new ParseInt()};
        File file = new File(outputDirectory + TsvFile.GENE_OUTPUT_FILE.fileName);
        try {
            this.writeOutputFile(file, allGenesInformation, header, processors);
        } catch (IOException e) {
            throw new UncheckedIOException("Can't write file "+file, e);
        }
        return allGenesInformation.stream().collect(Collectors.toMap(x -> x.get(1), x -> Integer.valueOf(x.get(0))));
    }
    
    
    private void extractAnatEntityTable(){
        log.debug("Start extracting anatomical entities...");
        String [] header = new String[] { AnatEntityDAO.Attribute.ID.name(), AnatEntityDAO.Attribute.NAME.name(),
                AnatEntityDAO.Attribute.DESCRIPTION.name()};
        Set<List<String>> allAnatEntitiesInformation = anatEntityDAO
                .getAnatEntitiesByIds(null)
                .getAllTOs().stream().map(s -> {
                    return Arrays.asList(s.getId(), s.getName(), s.getDescription());
                }).collect(Collectors.toSet());
        final CellProcessor[] processors = new CellProcessor[] { new NotNull(), new NotNull(), new Optional()};
        File file = new File(outputDirectory + TsvFile.ANATENTITY_OUTPUT_FILE.fileName);
        try {
            this.writeOutputFile(file, allAnatEntitiesInformation, header, processors);
        } catch (IOException e) {
            throw new UncheckedIOException("Can't write file "+file, e);
        }
    }

    private void extractStadeTable(){
        log.debug("Start extracting developmental stages");
        String [] header = new String[] { StageDAO.Attribute.ID.name(), StageDAO.Attribute.NAME.name(),
                StageDAO.Attribute.DESCRIPTION.name()};
        Set<List<String>> allDevStagesInformation = serviceFactory.getDAOManager().getStageDAO()
                .getStagesByIds(new HashSet<>())
                .getAllTOs().stream().map(s -> {
                    return Arrays.asList(s.getId(), s.getName(), s.getDescription());
                }).collect(Collectors.toSet());
        final CellProcessor[] processors = new CellProcessor[] { new NotNull(), new NotNull(), new Optional()};
        File file = new File(outputDirectory + TsvFile.DEVSTAGE_OUTPUT_FILE.fileName);
        try {
            this.writeOutputFile(file, allDevStagesInformation, header, processors);
        } catch (IOException e) {
            throw new UncheckedIOException("Can't write file "+file, e);
        }
    }
    
    private Map<String, Integer> extractGlobalCondTable(Integer speciesId){
        log.entry(speciesId);
        log.debug("Start extracting global conditions for the species {}...", speciesId);
        List<ConditionDAO.Attribute> condAttributes = Arrays.asList(ConditionDAO.Attribute.ANAT_ENTITY_ID, ConditionDAO.Attribute.STAGE_ID);
        List<ConditionDAO.Attribute> attributes = Arrays.asList(ConditionDAO.Attribute.ID, 
                ConditionDAO.Attribute.ANAT_ENTITY_ID, ConditionDAO.Attribute.STAGE_ID, ConditionDAO.Attribute.SPECIES_ID);
        String [] header = new String[] { ConditionDAO.Attribute.ID.name(), ConditionDAO.Attribute.ANAT_ENTITY_ID.name(), 
                ConditionDAO.Attribute.STAGE_ID.name(), ConditionDAO.Attribute.SPECIES_ID.name()};
        List<ConditionTO> conditionTOs = this.conditionDAO.getGlobalConditionsBySpeciesIds(Collections.singleton(speciesId), 
                condAttributes, attributes).getAllTOs();
        Set<List<String>> allglobalCondInformation = conditionTOs.stream().map(s -> {
                    return Arrays.asList(s.getId().toString(), s.getAnatEntityId(), s.getStageId(), 
                            s.getSpeciesId().toString());
                }).collect(Collectors.toSet());
        final CellProcessor[] processors = new CellProcessor[] { new ParseInt(), new NotNull(), new Optional(),
                new ParseInt()};
        File file = new File(outputDirectory + TsvFile.GLOBALCOND_OUTPUT_FILE.fileName);
        try {
            this.writeOutputFile(file, allglobalCondInformation, header, processors);
        } catch (IOException e) {
            throw new UncheckedIOException("Can't write file "+file, e);
        }
        return log.exit(conditionTOs.stream().collect(Collectors.toMap(
                p -> p.getAnatEntityId()+"_"+p.getStageId(), p -> p.getId())));
        
    }
    
    
    
    private void extractSpeciesTable(SpeciesTOResultSet speciesTOs){
        log.entry(speciesTOs);
        Set<Integer> speciesIds = new HashSet<>();
        String [] header = new String[] { SpeciesDAO.Attribute.ID.name(), SpeciesDAO.Attribute.GENUS.name(),
                SpeciesDAO.Attribute.SPECIES_NAME.name(), SpeciesDAO.Attribute.COMMON_NAME.name(),
                SpeciesDAO.Attribute.GENOME_VERSION.name(), SpeciesDAO.Attribute.GENOME_SPECIES_ID.name() };
        Set<List<String>> allSpeciesInformation = speciesTOs.getAllTOs().stream().map(s -> {
            speciesIds.add(s.getId());
            return Arrays.asList(s.getId().toString(), s.getGenus(), s.getSpeciesName(), s.getName(),
                    s.getGenomeVersion(),s.getGenomeSpeciesId().toString());
            }).collect(Collectors.toSet());
        final CellProcessor[] processors = new CellProcessor[] { new ParseInt(), new NotNull(), new NotNull(),
                new Optional(), new ParseInt()};
        File file = new File(outputDirectory + TsvFile.SPECIES_OUTPUT_FILE.fileName);
        try {
            this.writeOutputFile(file, allSpeciesInformation, header, processors);
        } catch (IOException e) {
            throw new UncheckedIOException("Can't write file "+file, e);
        }
    }

       
    private void writeOutputFile(File file, Set<List<String>> fileLines, String [] header, CellProcessor[] processors) throws IOException{
        log.entry(file, fileLines, header, processors);
        ICsvListWriter listWriter = null;
        try {
            if(!file.exists()){
                file.createNewFile();
                listWriter = new CsvListWriter(new FileWriter(file, true),
                        Utils.TSVCOMMENTED); 
                listWriter.write(header);
            }else{
                listWriter = new CsvListWriter(new FileWriter(file, true),
                        Utils.TSVCOMMENTED); 
            }
            
            for (List <String> line:fileLines){
                listWriter.write(line);
            }
        }
        finally {
            if( listWriter != null ) {
                listWriter.close();
            }
        }
    }
    
    private void emptyDatabaseTables(){
        for(TsvFile tsvFile : TsvFile.values()){
            String sql = "DELETE FROM "+tsvFile.tableName;
            try (BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql.toString())) {
                stmt.executeUpdate();
                this.commit();
            } catch (SQLException e) {
                log.error("Can not connect to the database");
                e.printStackTrace();
            }
        }
    }
    
    private void tsvToBgeeLight(){
        for(TsvFile tsvFile : TsvFile.values()){
            log.info("start integration of data from file {}",tsvFile.fileName);
            this.startTransaction();
            ICsvMapReader mapReader = null;
            try {               
                mapReader = new CsvMapReader(new FileReader(outputDirectory + tsvFile.fileName), Utils.TSVCOMMENTED);
                // the header columns are used as the keys to the Map
                String[] header = mapReader.getHeader(true);
                // create the processor. No null values in the tsv file so we create a generic processor defining only
                // NotNull() columns
                CellProcessor[] processors = new CellProcessor[header.length];
                String sql = "INSERT INTO " + tsvFile.tableName + " (";
                String variables = "(";
                for(int i = 0; i<header.length ; i++){
                    processors[i] = new Optional();
                    sql += tsvFile.columnMapping.get(header[i])+", ";
                    variables += "?, ";
                }
                //sql.length() -2 because we remove last curl and space
                sql = sql.substring(0, sql.length() -2) +") VALUES " + variables.substring(0, variables.length() -2) +")";
                log.debug("SQL query : {}", sql);
                Map<String, Object> customerMap;
                try (BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql.toString())) {
                    while( (customerMap = mapReader.read(header, processors)) != null ) {
                        for(int i = 0; i<header.length ; i++){
                            Object columnValue = customerMap.get(header[i]);
                            if (columnValue instanceof Integer){
                                stmt.setInt(i+1 ,Integer.valueOf(columnValue.toString()));            
                            }else if (columnValue instanceof String) {
                                stmt.setString(i+1,String.valueOf(columnValue));   
                            }else if(columnValue == null){
                                stmt.setString(i+1, "");
                            }
                            else {
                                throw log.throwing(new IllegalArgumentException(
                                        "Each column should be an Integer or a String"));
                            }
                        }
                        stmt.executeUpdate();
                    }
                    
                    //commit once all lines of the file have been parsed
                    
                    this.commit();
                } catch (SQLException e) {
                    log.error("Can not connect to the database");
                    e.printStackTrace();
                }
            }catch (FileNotFoundException e) {
                log.error("Can not find the file " + outputDirectory + tsvFile.fileName);
                e.printStackTrace();
            }catch(IOException e){
                log.error("Can not read the file " + outputDirectory + tsvFile.fileName);
                e.printStackTrace();
            }
            finally {
                if( mapReader != null ) {
                    try {
                        mapReader.close();
                    } catch (IOException e) {
                        log.error("Can not close the file " + outputDirectory + tsvFile.fileName);
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
}
