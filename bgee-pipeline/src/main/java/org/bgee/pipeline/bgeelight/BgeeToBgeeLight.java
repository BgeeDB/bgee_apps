package org.bgee.pipeline.bgeelight;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO;
import org.bgee.model.dao.api.anatdev.StageDAO;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.ConditionDAO.Attribute;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTOResultSet;
import org.bgee.pipeline.CommandRunner;
import org.bgee.pipeline.Utils;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListWriter;

/**
 * Extract data from the Bgee database and generate one TSV file for each extracted table.
 * These TSV files will then be used to populate the bgee light database (initially created for the bioSoda project)
 * @author jwollbrett
 *
 */
public class BgeeToBgeeLight {
        
    private final static String GENE_OUTPUT_FILE ="genes_bgee_light.tsv";
    private final static String ANATENTITY_OUTPUT_FILE ="anat_entities_bgee_light.tsv";
    private final static String GLOBALCOND_OUTPUT_FILE ="global_cond_bgee_light.tsv";
    private final static String SPECIES_OUTPUT_FILE ="species_bgee_light.tsv";
    private final static String DEVSTAGE_OUTPUT_FILE ="dev_stages_bgee_light.tsv";
    private final static String GLOBALEXPRESSION_OUTPUT_FILE ="";
    private final static Logger log = LogManager.getLogger(BgeeToBgeeLight.class);
    protected final ServiceFactory serviceFactory = new ServiceFactory();
    private String outputDirectory;
    
    public static void main(String[] args) throws IllegalArgumentException{
        log.entry((Object[]) args);
        int expectedArgLength = 2;
        if (args.length != expectedArgLength) {
            throw log.throwing(new IllegalArgumentException(
                    "Incorrect number of arguments provided, expected " + 
                    expectedArgLength + " arguments, " + args.length + " provided."));
        }
        BgeeToBgeeLight bgeeToBgeeLight = new BgeeToBgeeLight(args[0]);
        bgeeToBgeeLight.deletePreviousOutputFiles();
        bgeeToBgeeLight.extractBgeeDatabase(CommandRunner.parseListArgumentAsInt(args[1]));
    }
    
    public BgeeToBgeeLight(String outputDirectory) throws IllegalArgumentException{
        this.outputDirectory = outputDirectory;
    }
    
    private void deletePreviousOutputFiles(){
        File file = new File(outputDirectory+File.separator+GENE_OUTPUT_FILE);
        file.delete();
        file = new File(outputDirectory+File.separator+ANATENTITY_OUTPUT_FILE);
        file.delete();
        file = new File(outputDirectory+File.separator+GLOBALCOND_OUTPUT_FILE);
        file.delete();
        file = new File(outputDirectory+File.separator+SPECIES_OUTPUT_FILE);
        file.delete();
        file = new File(outputDirectory+File.separator+DEVSTAGE_OUTPUT_FILE);
        file.delete();
        file = new File(outputDirectory+File.separator+GLOBALEXPRESSION_OUTPUT_FILE);
        file.delete();
    }
    
    private void extractBgeeDatabase(Collection <Integer> speciesIds) {
        log.entry(speciesIds);
        SpeciesTOResultSet speciesTOs = serviceFactory.getDAOManager().getSpeciesDAO().getSpeciesByIds(new HashSet<Integer>(speciesIds));
        extractSpeciesTable(speciesTOs);
        extractAnatEntityTable();
        extractStadeTable();
        for (Integer speciesId:speciesIds){
            extractGeneTable(speciesId);
            extractGlobalCondTable(speciesId);
        }
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
        File file = new File(outputDirectory+File.separator+ANATENTITY_OUTPUT_FILE);
        try {
            this.writeOutputFile(file, allAnatEntitiesInformation, header, processors);
        } catch (IOException e) {
            throw new UncheckedIOException("Can't write file "+outputDirectory+File.separator+ANATENTITY_OUTPUT_FILE, e);
        }
    }
    
    private void extractGeneTable(Integer speciesId){
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
        File file = new File(outputDirectory+File.separator+GENE_OUTPUT_FILE);
        try {
            this.writeOutputFile(file, allGenesInformation, header, processors);
        } catch (IOException e) {
            throw new UncheckedIOException("Can't write file "+outputDirectory+File.separator+GENE_OUTPUT_FILE, e);
        }
    }
    

    private void extractStadeTable(){
        log.debug("Start extracting developmental stages");
        String [] header = new String[] { StageDAO.Attribute.ID.name(), StageDAO.Attribute.NAME.name(),
                StageDAO.Attribute.DESCRIPTION.name()};
        Set<List<String>> allDevStagesInformation = serviceFactory.getDAOManager().getStageDAO()
                .getStagesByIds(null)
                .getAllTOs().stream().map(s -> {
                    return Arrays.asList(s.getId(), s.getName(), s.getDescription());
                }).collect(Collectors.toSet());
        final CellProcessor[] processors = new CellProcessor[] { new NotNull(), new NotNull(), new Optional()};
        File file = new File(outputDirectory+File.separator+DEVSTAGE_OUTPUT_FILE);
        try {
            this.writeOutputFile(file, allDevStagesInformation, header, processors);
        } catch (IOException e) {
            throw new UncheckedIOException("Can't write file "+outputDirectory+File.separator+DEVSTAGE_OUTPUT_FILE, e);
        }
    }
    
    private void extractGlobalCondTable(Integer speciesId){
        log.entry(speciesId);
        log.debug("Start extracting global conditions for the species {}...", speciesId);
        List<Attribute> attributes = Arrays.asList(ConditionDAO.Attribute.ID, 
                ConditionDAO.Attribute.ANAT_ENTITY_ID, ConditionDAO.Attribute.STAGE_ID, ConditionDAO.Attribute.SPECIES_ID);
        String [] header = new String[] { ConditionDAO.Attribute.ID.name(), ConditionDAO.Attribute.ANAT_ENTITY_ID.name(), 
                ConditionDAO.Attribute.STAGE_ID.name(), ConditionDAO.Attribute.SPECIES_ID.name()};
        Set<List<String>> allglobalCondInformation = serviceFactory.getDAOManager().getConditionDAO()
                .getGlobalConditionsBySpeciesIds(Collections.singleton(speciesId), new HashSet<Attribute>(), attributes)
                .getAllTOs().stream().map(s -> {
                    return Arrays.asList(s.getId().toString(), s.getAnatEntityId(), s.getStageId(), 
                            s.getSpeciesId().toString());
                }).collect(Collectors.toSet());
        final CellProcessor[] processors = new CellProcessor[] { new ParseInt(), new NotNull(), new Optional(),
                new ParseInt()};
        File file = new File(outputDirectory+File.separator+GLOBALCOND_OUTPUT_FILE);
        try {
            this.writeOutputFile(file, allglobalCondInformation, header, processors);
        } catch (IOException e) {
            throw new UncheckedIOException("Can't write file "+outputDirectory+File.separator+GLOBALCOND_OUTPUT_FILE, e);
        }
    }
    
    private void extractGlobalExpressionTable(){
        
    }
    
    private void extractSpeciesTable(SpeciesTOResultSet speciesTOs){
        log.entry(speciesTOs);
        String [] header = new String[] { SpeciesDAO.Attribute.ID.name(), SpeciesDAO.Attribute.GENUS.name(),
                SpeciesDAO.Attribute.SPECIES_NAME.name(), SpeciesDAO.Attribute.COMMON_NAME.name(),
                SpeciesDAO.Attribute.GENOME_VERSION.name(), SpeciesDAO.Attribute.GENOME_SPECIES_ID.name() };
        Set<List<String>> allSpeciesInformation = speciesTOs.getAllTOs().stream().map(s -> {
            return Arrays.asList(s.getId().toString(), s.getGenus(), s.getSpeciesName(), s.getName(),
                    s.getGenomeVersion(),s.getGenomeSpeciesId().toString());
            }).collect(Collectors.toSet());
        final CellProcessor[] processors = new CellProcessor[] { new ParseInt(), new NotNull(), new NotNull(),
                new Optional(), new ParseInt()};
        File file = new File(outputDirectory+File.separator+SPECIES_OUTPUT_FILE);
        try {
            this.writeOutputFile(file, allSpeciesInformation, header, processors);
        } catch (IOException e) {
            throw new UncheckedIOException("Can't write file "+outputDirectory+File.separator+SPECIES_OUTPUT_FILE, e);
        }
    }

       
    private void writeOutputFile(File file, Set<List<String>> fileLines, String [] header, CellProcessor[] processors) throws IOException{
        log.entry(file, fileLines, header, processors);
        ICsvListWriter listWriter = null;
        try {
            listWriter = new CsvListWriter(new FileWriter(file, true),
                        Utils.TSVCOMMENTED);
            if(!file.exists()){
                file.createNewFile();
                listWriter.write(header);
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
    
}
