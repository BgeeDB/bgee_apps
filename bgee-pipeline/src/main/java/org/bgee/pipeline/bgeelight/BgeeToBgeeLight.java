package org.bgee.pipeline.bgeelight;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
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
import org.bgee.model.dao.api.expressiondata.CallDAOFilter;
import org.bgee.model.dao.api.expressiondata.CallDataDAOFilter;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.ConditionDAO.Attribute;
import org.bgee.model.dao.api.expressiondata.DAOConditionFilter;
import org.bgee.model.dao.api.expressiondata.DAOExperimentCount;
import org.bgee.model.dao.api.expressiondata.DAOExperimentCount.CallType;
import org.bgee.model.dao.api.expressiondata.DAOExperimentCount.DataQuality;
import org.bgee.model.dao.api.expressiondata.DAOExperimentCountFilter;
import org.bgee.model.dao.api.expressiondata.DAOExperimentCountFilter.Qualifier;
import org.bgee.model.dao.api.expressiondata.DAOPropagationState;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO.GlobalExpressionCallTOResultSet;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTOResultSet;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
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
                + "STAGE_ID=stageId}"); 
//        GLOBALEXPRESSION_OUTPUT_FILE("global_expression_bgee_light.tsv", "globalExpression");

        
             
                  
                       
               
        
                 
               
        
             
        
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

    private final static Logger log = LogManager.getLogger(BgeeToBgeeLight.class);
    protected final ServiceFactory serviceFactory = new ServiceFactory();
    private String outputDirectory;
    
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
    public static void main(String[] args) throws Exception{
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
        if(args[0].equals("tsvToBgeeLight")){
            int expectedArgLength = 2;
            if (args.length != expectedArgLength) {
                throw log.throwing(new IllegalArgumentException(
                        "Incorrect number of arguments provided, expected " + 
                        expectedArgLength + " arguments, " + args.length + " provided."));
            }
            BgeeToBgeeLight bgeeToBgeeLight = new BgeeToBgeeLight(args[1]);
            bgeeToBgeeLight.tsvToBgeeLight();
        }else{
            throw log.throwing(new IllegalArgumentException("The value of args[0] " + args[0] + " is not reconized as an action"));
        }
    }
    
    public BgeeToBgeeLight(String outputDirectory) throws IllegalArgumentException{
        this.outputDirectory = outputDirectory;
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
        
        SpeciesTOResultSet speciesTOs = serviceFactory.getDAOManager().getSpeciesDAO().getSpeciesByIds(new HashSet<Integer>(speciesIds));
        speciesIds = extractSpeciesTable(speciesTOs);
        extractAnatEntityTable();
        extractStadeTable();
        for (Integer speciesId:speciesIds){
            Set <Integer> geneSet = extractGeneTable(speciesId);
            extractGlobalCondTable(speciesId);
            extractGlobalExpressionTable(geneSet, speciesId);
        }
    }
    
    private void extractGlobalExpressionTable(Set<Integer> geneSet, Integer speciesId){
        log.entry(geneSet, speciesId);
        log.debug("Start extracting global expressions for the species {}...", speciesId);
        String [] header = new String[] { GlobalExpressionCallDAO.Attribute.ID.name(), GlobalExpressionCallDAO.Attribute.BGEE_GENE_ID.name(),
                GlobalExpressionCallDAO.Attribute.CONDITION_ID.name()};
      //EXPERIMENT COUNT FILTER        
        Set<DAOExperimentCountFilter> countFilters = new HashSet<>();
        countFilters.add(new DAOExperimentCountFilter(new DAOExperimentCount(CallType.PRESENT, 
                DataQuality.HIGH, DAOPropagationState.SELF, 1), Qualifier.GREATER_THAN));
        countFilters.add(new DAOExperimentCountFilter(new DAOExperimentCount(CallType.PRESENT, 
                DataQuality.LOW, DAOPropagationState.SELF, 3), Qualifier.GREATER_THAN));
        Set<Set <DAOExperimentCountFilter> > allCountFilters = Collections.singleton(countFilters);
        // CONDITION PARAMETERS
        Set<Attribute> conditionParameters = EnumSet.of(Attribute.ANAT_ENTITY_ID, Attribute.STAGE_ID);
        // ATTRIBUTES TO RETRIEVE
        Set<GlobalExpressionCallDAO.Attribute> attributes = EnumSet.of(GlobalExpressionCallDAO.Attribute.ID,
                GlobalExpressionCallDAO.Attribute.BGEE_GENE_ID, GlobalExpressionCallDAO.Attribute.CONDITION_ID);
        GlobalExpressionCallTOResultSet globalExpressionCallTO  = serviceFactory.getDAOManager().getGlobalExpressionCallDAO()
                .getGlobalExpressionCalls(Collections.singleton(
                        new CallDAOFilter(geneSet, null, Collections.singleton(
                                new DAOConditionFilter(null, null, true)
                                ), Collections.singleton(
                                        new CallDataDAOFilter(allCountFilters, null)
                                        ),
                                true, null)), 
                        conditionParameters, attributes, null);
        
        Set<List<String>> CallsInformation = globalExpressionCallTO.getAllTOs().stream().map(s -> {
                    return Arrays.asList(s.getId().toString(), s.getBgeeGeneId().toString(), s.getConditionId().toString());
                }).collect(Collectors.toSet());
        final CellProcessor[] processors = new CellProcessor[] { new ParseInt(), new ParseInt(), new ParseInt()};
        File file = new File(outputDirectory + TsvFile.GLOBALEXPRESSION_OUTPUT_FILE.fileName);
        try {
            this.writeOutputFile(file, CallsInformation, header, processors);
        } catch (IOException e) {
            throw new UncheckedIOException("Can't write file "+file, e);
        }

    }
    
    private  Set<Integer> extractGeneTable(Integer speciesId){
        log.entry(speciesId);
        log.debug("Start extracting genes for the species {}...", speciesId);
        String [] header = new String[] { GeneDAO.Attribute.ID.name(), GeneDAO.Attribute.ENSEMBL_ID.name(),
                GeneDAO.Attribute.NAME.name(), GeneDAO.Attribute.DESCRIPTION.name(),
                GeneDAO.Attribute.SPECIES_ID.name()};
        Set<List<String>> allGenesInformation = serviceFactory.getDAOManager().getGeneDAO()
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
        return allGenesInformation.stream().map(g -> {
            return Integer.parseInt(g.get(0));
        }).collect(Collectors.toSet());
    }
    
    private void extractAnatEntityTable(){
        log.debug("Start extracting anatomical entities...");
        String [] header = new String[] { AnatEntityDAO.Attribute.ID.name(), AnatEntityDAO.Attribute.NAME.name(),
                AnatEntityDAO.Attribute.DESCRIPTION.name()};
        Set<List<String>> allAnatEntitiesInformation = serviceFactory.getDAOManager().getAnatEntityDAO()
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
    
    private void extractGlobalCondTable(Integer speciesId){
        log.entry(speciesId);
        log.debug("Start extracting global conditions for the species {}...", speciesId);
        List<Attribute> condAttributes = Arrays.asList(ConditionDAO.Attribute.ANAT_ENTITY_ID, ConditionDAO.Attribute.STAGE_ID);
        List<Attribute> attributes = Arrays.asList(ConditionDAO.Attribute.ID, 
                ConditionDAO.Attribute.ANAT_ENTITY_ID, ConditionDAO.Attribute.STAGE_ID, ConditionDAO.Attribute.SPECIES_ID);
        String [] header = new String[] { ConditionDAO.Attribute.ID.name(), ConditionDAO.Attribute.ANAT_ENTITY_ID.name(), 
                ConditionDAO.Attribute.STAGE_ID.name(), ConditionDAO.Attribute.SPECIES_ID.name()};
        Set<List<String>> allglobalCondInformation = serviceFactory.getDAOManager().getConditionDAO()
                .getGlobalConditionsBySpeciesIds(Collections.singleton(speciesId), condAttributes, attributes)
                .getAllTOs().stream().map(s -> {
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
    }
    
    
    
    private Set<Integer> extractSpeciesTable(SpeciesTOResultSet speciesTOs){
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
        return speciesIds;
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
            
        }
    }
    
    private void tsvToBgeeLight() throws Exception{
        for(TsvFile tsvFile : TsvFile.values()){
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
                    }
                    //commit once all lines of the file have been parsed
                    stmt.executeUpdate();
                    this.commit();
                }
            }
            finally {
                if( mapReader != null ) {
                    mapReader.close();
                }
            }
        }
    }
    
}
