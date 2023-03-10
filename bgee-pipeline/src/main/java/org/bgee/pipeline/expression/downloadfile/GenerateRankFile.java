package org.bgee.pipeline.expression.downloadfile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.Sex;
import org.bgee.model.anatdev.Strain;
import org.bgee.model.expressiondata.call.Call.ExpressionCall;
import org.bgee.model.expressiondata.call.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.call.CallService;
import org.bgee.model.expressiondata.call.Condition;
import org.bgee.model.expressiondata.call.ConditionGraph;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.species.Species;
import org.bgee.pipeline.CommandRunner;
import org.bgee.pipeline.Utils;
import org.bgee.pipeline.uberon.Uberon;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;

import owltools.graph.OWLGraphWrapper;

/**
 * Allows to generate download files providing rank score information. 
 * 
 * @author  Frederic Bastian
 * @author  Julien Wollbrett
 * @version Bgee 14 Oct 2018
 * @since   Bgee 13 July 2016
 */
//XXX: generate a simple file providing the max rank? A bit boring as we have nothing 
//in bgee-core to do it for now.
public class GenerateRankFile {
    private final static Logger log = LogManager.getLogger(GenerateRankFile.class.getName());
    
    /**
     * Functional interface created to be able to inject ConditionGraph by providing a supplier.
     * TODO: to remove once we'll have created an UtilsFactory in bgee-core 
     */
    @FunctionalInterface
    protected static interface TriFunction<T, U, V, R> {
        public R apply(T t, U u, V v);
    }
    
    /**
     * A bean to store all requested information to be written in output files. 
     */
    public static class ExpressionCallBean {
        private final String geneId;
        private final String geneName;
        private final String anatEntityId;
        private final String anatEntityName;
        private final String devStageId;
        private final String devStageName;
        private final String formattedRank;
        private final boolean affymetrixData;
        private final boolean estData;
        private final boolean inSituData;
        private final boolean rnaSeqData;
        private final boolean singleCellRnaSeqFullLengthData;
        private final boolean redundant;
        private final List<String> btoXRefs;
        
        public ExpressionCallBean(String geneId, String geneName, String anatEntityId, 
                String anatEntityName, String devStageId, String devStageName, String formattedRank, 
                boolean affymetrixData, boolean estData, boolean inSituData, boolean rnaSeqData, 
                boolean singleCellRnaSeqFullLengthData, boolean redundant, List<String> btoXRefs) {
            
            this.geneId = geneId;
            this.geneName = geneName;
            this.anatEntityId = anatEntityId;
            this.anatEntityName = anatEntityName;
            this.devStageId = devStageId;
            this.devStageName = devStageName;
            this.formattedRank = formattedRank;
            this.affymetrixData = affymetrixData;
            this.estData = estData;
            this.inSituData = inSituData;
            this.rnaSeqData = rnaSeqData;
            this.singleCellRnaSeqFullLengthData = singleCellRnaSeqFullLengthData;
            this.redundant = redundant;
            this.btoXRefs = btoXRefs;
        }
        
        public String getGeneId() {
            return geneId;
        }
        public String getGeneName() {
            return geneName;
        }
        public String getAnatEntityId() {
            return anatEntityId;
        }
        public String getAnatEntityName() {
            return anatEntityName;
        }
        public String getDevStageId() {
            return devStageId;
        }
        public String getDevStageName() {
            return devStageName;
        }
        public String getFormattedRank() {
            return formattedRank;
        }
        public boolean isAffymetrixData() {
            return affymetrixData;
        }
        public boolean isEstData() {
            return estData;
        }
        public boolean isInSituData() {
            return inSituData;
        }
        public boolean isRnaSeqData() {
            return rnaSeqData;
        }
        public boolean isSingleCellRnaSeqFullLengthData() {
            return singleCellRnaSeqFullLengthData;
        }
        public boolean isRedundant() {
            return redundant;
        }
        public List<String> getBtoXRefs() {
            return btoXRefs;
        }
    }
    
    /**
     * A {@code Function} accepting a {@code BigDecimal} and returning a formatted {@code String}. 
     * It is more convenient than directly using a {@code NumberFormat}, as we might need 
     * additional formatting operation. 
     */
    private static final Function<BigDecimal, String> FORMATTER = d -> {
        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        formatter.setMaximumFractionDigits(2);
        formatter.setMinimumFractionDigits(2);
        formatter.setGroupingUsed(false);
        formatter.setRoundingMode(RoundingMode.HALF_UP);
        log.debug(d);
        return formatter.format(d);
    };
    
    /**
     * Generate a file name and return the related {@code File} according to the arguments. 
     * 
     * @param species           A {@code Species} which the file is created for.
     * @param anatEntityOnly    A {@code boolean} defining whether the file is generated  
     *                          by grouping data by gene-anatomical entity (if {@code true}), 
     *                          or by considering all condition information (such as developmental stage, 
     *                          if {@code false}).
     * @param dataType          The {@code DataType} that is considered to generate the file. 
     *                          If {@code null}, then all data types were considered. 
     * @param outputDir         A {@code String} that is the path to the directory where the file is stored.
     * @param tmpFile           A {@code boolean} defining whether a temp file name should be returned 
     *                          (if {@code true}), or the name of the final file (if {@code false}).
     * @return                  A {@code File} pointing to a file with a name capturing information 
     *                          about the arguments, to be stored in {@code outputDir}.
     */
    protected static File getOutputFile(Species species, boolean anatEntityOnly, DataType dataType, 
            String outputDir, boolean tmpFile) {
        log.traceEntry("{}, {}, {}, {}, {}", species, anatEntityOnly, dataType, outputDir, tmpFile);
        
        String fileName = species.getId().toString();
        if (anatEntityOnly) {
            fileName += "_anat_entity";
        } else {
            fileName += "_condition";
        }
        if (dataType == null) {
            fileName += "_all_data";
        } else {
            fileName += "_" + dataType.getStringRepresentation().toLowerCase(Locale.ENGLISH).replace(" ", "-");
        }
        fileName += "_" + species.getScientificName().replace(" ", "_");
        fileName += ".tsv";
        if (tmpFile) {
            fileName += ".tmp";
        }
        
        return log.traceExit(Paths.get(outputDir, fileName).toFile());
    }
    /**
     * Create the header to be used by SuperCSV for writing the file. 
     * 
     * @param anatEntityOnly    A {@code boolean} defining whether the file is generated  
     *                          by grouping data by gene-anatomical entity (if {@code true}), 
     *                          or by considering all condition information (such as developmental stage, 
     *                          if {@code false}).
     * @param dataType          The {@code DataType} that is considered to generate the file. 
     *                          If {@code null}, then all data types were considered. 
     * @return                  An {@code Array} of {@code String}s to be used as header of the file.
     * @see #getColToAttributeMapping(boolean, DataType)
     * @see #getCellProcessors(boolean, DataType)
     */
    protected static String[] getFileHeader(boolean anatEntityOnly, DataType dataType) {
        log.traceEntry("{}, {}", anatEntityOnly, dataType);
        
        int arrLength = 6 + (anatEntityOnly? 0: 2) + (dataType != null? 0: 0);
        String[] header = new String[arrLength];
        header[0] = "Gene ID";
        header[1] = "gene name";
        header[2] = "anatomical entity ID";
        header[3] = "anatomical entity name";
        int i = 4;
        if (!anatEntityOnly) {
            header[i] = "developmental stage ID";
            i++;
            header[i] = "developmental stage name";
            i++;
        }
        header[i] = "rank score";
        i++;
        //XXX: deactivate this until MySQLExpressionCallDAO is debugged. 
//        if (dataType == null) {
//            header[i] = "Affymetrix data";
//            i++;
//            header[i] = "EST data";
//            i++;
//            header[i] = "in situ hybridization data";
//            i++;
//            header[i] = "RNA-Seq data";
//            i++;
//        }
        //XXX: deactivate because too slow
//        header[i] = "is redundant";
//        i++;
        header[i] = "XRefs to BTO";
        i++;
        
        return log.traceExit(header);
    }
    /**
     * Create a mapping from columns of the TSV file to attributes of the {@code ExpressionCallBean}.
     *  
     * @param anatEntityOnly    A {@code boolean} defining whether the file is generated  
     *                          by grouping data by gene-anatomical entity (if {@code true}), 
     *                          or by considering all condition information (such as developmental stage, 
     *                          if {@code false}).
     * @param dataType          The {@code DataType} that is considered to generate the file. 
     *                          If {@code null}, then all data types were considered. 
     * @return                  An {@code Array} of {@code String}s providing the names of the attributes 
     *                          of {@code ExpressionCallBean} in the same order as the columns 
     *                          they should be written in.
     * @see #getFileHeader(boolean, DataType)
     * @see #getCellProcessors(boolean, DataType)
     */
    private static String[] getColToAttributeMapping(boolean anatEntityOnly, DataType dataType) {
        log.traceEntry("{}, {}", anatEntityOnly, dataType);
    
        int arrLength = 6 + (anatEntityOnly? 0: 2) + (dataType != null? 0: 0);
        String[] colToAttribute = new String[arrLength];
        colToAttribute[0] = "geneId";
        colToAttribute[1] = "geneName";
        colToAttribute[2] = "anatEntityId";
        colToAttribute[3] = "anatEntityName";
        int i = 4;
        if (!anatEntityOnly) {
            colToAttribute[i] = "devStageId";
            i++;
            colToAttribute[i] = "devStageName";
            i++;
        }
        colToAttribute[i] = "formattedRank";
        i++;
        //XXX: deactivate this until MySQLExpressionCallDAO is debugged. 
//        if (dataType == null) {
//            colToAttribute[i] = "affymetrixData";
//            i++;
//            colToAttribute[i] = "estData";
//            i++;
//            colToAttribute[i] = "inSituData";
//            i++;
//            colToAttribute[i] = "rnaSeqData";
//            i++;
//        }
        //XXX: deactivate because too slow
//        colToAttribute[i] = "redundant";
//        i++;
        colToAttribute[i] = "btoXRefs";
        i++;
        
        return log.traceExit(colToAttribute);
    }
    /**
     * Define the {@code CellProcessor}s for writing the TSV file. 
     * 
     * @param anatEntityOnly    A {@code boolean} defining whether the file is generated  
     *                          by grouping data by gene-anatomical entity (if {@code true}), 
     *                          or by considering all condition information (such as developmental stage, 
     *                          if {@code false}).
     * @param dataType          The {@code DataType} that is considered to generate the file. 
     *                          If {@code null}, then all data types were considered. 
     * @return                  An {@code Array} of {@code CellProcessor}s in the same order 
     *                          as the columns they should process.
     * @see #getFileHeader(boolean, DataType)
     * @see #getColToAttributeMapping(boolean, DataType)
     */
    private static CellProcessor[] getCellProcessors(boolean anatEntityOnly, DataType dataType) {
        log.traceEntry("{}, {}", anatEntityOnly, dataType);
        
        int arrLength = 6 + (anatEntityOnly? 0: 2) + (dataType != null? 0: 0);
        CellProcessor[] processors = new CellProcessor[arrLength];
        processors[0] = new StrNotNullOrEmpty();
        processors[1] = new Optional();
        processors[2] = new StrNotNullOrEmpty();
        processors[3] = new StrNotNullOrEmpty();
        int i = 4;
        if (!anatEntityOnly) {
            processors[i] = new StrNotNullOrEmpty();
            i++;
            processors[i] = new StrNotNullOrEmpty();
            i++;
        }
        processors[i] = new StrNotNullOrEmpty();
        i++;
        //XXX: deactivate this until MySQLExpressionCallDAO is debugged. 
//        if (dataType == null) {
//            processors[i] = new FmtBool("T", "F");
//            i++;
//            processors[i] = new FmtBool("T", "F");
//            i++;
//            processors[i] = new FmtBool("T", "F");
//            i++;
//            processors[i] = new FmtBool("T", "F");
//            i++;
//        }
        //XXX: deactivate because too slow
//        processors[i] = new FmtBool("T", "F");
//        i++;
        processors[i] = new Optional(new Utils.FmtMultipleStringValues());
        i++;
        
        return log.traceExit(processors);
    }
    /**
     * <ul>
     * <li>If the first element in {@code args} is "generateRankFiles", the action 
     * will be to generate download files providing rank information. 
     * Following elements in {@code args} must then be: 
     *   <ol>
     *   <li>A list of NCBI species IDs (e.g., {@code 9606}) to generate files for, separated by 
     *   {@link org.bgee.pipeline.CommandRunner#LIST_SEPARATOR}. To select all species, provide 
     *   an empty list by using {@link org.bgee.pipeline.CommandRunner#EMPTY_LIST}.
     *   <li>path to the Uberon ontology file to extract XRefs to BTO from.
     *   TODO: when Uberon xrefs will have been inserted into the database, use them, 
     *   rather than needing to provide an ontology. 
     *   <li>A boolean ("true", "false") defining whether results should present one row per gene-organ, 
     *   or one row per gene-condition (complete information)
     *   <li>path to the output directory where to store the generated files.
     *   <li>OPTIONAL: a data type for which to generate the files. If this argument is not provided, 
     *   then all possible files will be generated (one file considering all data types, 
     *   on file considering only Affymetrix, etc). If it is equal to {@code ALL}, then only 
     *   the file considering all data types will be generated, if it is equal to a specific data type 
     *   ({@code AFFYMETRIX}, {@code EST}, {@code IN_SITU}, {@code RNA_SEQ}, ), then only 
     *   the file for this data type will be generated. 
     *   </ol>
     * </ul>
     * 
     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
     * @throws OBOFormatParserException     If an error occurred while parsing an ontology file.
     * @throws OWLOntologyCreationException If an error occurred while loading an Ontology.
     * @throws IOException                  If an error occurred while parsing an ontology file 
     *                                      or writing output file.
     */
    public static void main(String[] args) 
            throws OBOFormatParserException, OWLOntologyCreationException, IOException {
        log.traceEntry("{}", (Object[]) args);
        
        if (args[0].equalsIgnoreCase("generateRankFiles")) {
            if (args.length != 5 && args.length != 6) {
                throw log.throwing(new IllegalArgumentException(
                        "Incorrect number of arguments provided, expected 5 or 6 arguments, "
                        + args.length + " provided."));
            }
            
            Set<Integer> speciesIds = new HashSet<>(CommandRunner.parseListArgumentAsInt(args[1]));
            String pathToUberon = args[2];
            boolean anatEntityOnly = Boolean.parseBoolean(args[3]);
            String outputDir = args[4];
            //no data type provided will generate all data type files: with all data, with affy only, etc
            //We don't use an EnumSet to be able to store "null", which means "one file with all data". 
            //TODO: to change when a DataType.ALL will be implemented. 
            Set<DataType> dataTypes = new HashSet<DataType>(Arrays.asList(DataType.values()));
            dataTypes.add(null);
            if (args.length == 6) {
                dataTypes = dataTypes.stream().filter(
                        type -> type == null && args[5].equalsIgnoreCase("ALL") || 
                                type != null && args[5].equalsIgnoreCase(type.name()))
                        .collect(Collectors.toSet());
                if (dataTypes.isEmpty()) {
                    throw log.throwing(new IllegalArgumentException("Unrecognized data type: " + args[5]));
                }
                log.info("datatype(s) for which rank files will be generated : {}", dataTypes);
            }
            
            GenerateRankFile generator = new GenerateRankFile(pathToUberon);
            generator.generateRankFiles(speciesIds, anatEntityOnly, dataTypes, outputDir);
            
        } else {
            throw log.throwing(new UnsupportedOperationException("The following action " +
                    "is not recognized: " + args[0]));
        }
        
        log.traceExit();
    }
    
    
    
    /**
     * A {@code Supplier} of {@code ServiceFactory}s to be able to provide one to each thread.
     */
    private final Supplier<ServiceFactory> serviceFactorySupplier;
    /**
     * The {@code Uberon} utility to extract XRefs to BTO.
     */
    //TODO: when Uberon xrefs will have been inserted into the database, use them, 
    //rather than needing to provide an ontology. 
    private final Uberon uberonOnt;
  
    
    
    /**
     * @param pathToUberon                  A {@code String} that is the path to the Uberon ontology.
     * @throws OBOFormatParserException     If an error occurred while parsing the ontology file.
     * @throws OWLOntologyCreationException If an error occurred while loading the Ontology.
     * @throws IOException                  If an error occurred while parsing the ontology file
     */
    public GenerateRankFile(String pathToUberon) 
            throws OBOFormatParserException, OWLOntologyCreationException, IOException {
        this(ServiceFactory::new, new Uberon(pathToUberon));
    }
    /**
     * @param serviceFactorySupplier    A {@code Supplier} of {@code ServiceFactory}s 
     *                                  to be able to provide one to each thread.
     * @param uberonOnt                 An {@code Uberon} utiliy to extract XRefs to BTO from.
     */
    public GenerateRankFile(Supplier<ServiceFactory> serviceFactorySupplier, Uberon uberonOnt) {
        this.serviceFactorySupplier = serviceFactorySupplier;
        this.uberonOnt = uberonOnt;
    }
    
    
    
    /**
     * Generate rank files for all requested species and data types, in parallel. 
     *
     * @param speciesIds        A {@code Set} of {@code String}s that are the IDs of the species 
     *                          for which the files should be generated. If {@code null} or empty, 
     *                          all species are considered. 
     * @param anatEntityOnly    A {@code boolean} defining whether the files should be generated  
     *                          by grouping data by gene-anatomical entity (if {@code true}), 
     *                          or by considering all condition information (such as developmental stage, 
     *                          if {@code false}).
     * @param dataTypes         A {@code Set} of {@code DataType}s for which a file should be generated, 
     *                          considering only this data type. If it contains a {@code null} value, 
     *                          then a file considering all data types will be generated. 
     *                          Cannot be {@code null} nor empty. 
     * @param outputDir         A {@code String} that is the path to the directory where to store 
     *                          the generated files.
     * @throws IllegalArgumentException If a species ID is not recognized, or {@code dataTypes} 
     *                                  is {@code null} or empty. 
     * @throws RuntimeException         If an error occurs while generating the files. 
     *                                  It is not a checked exception because we use parallel {@code Stream}s.
     */
    public void generateRankFiles(Set<Integer> speciesIds, boolean anatEntityOnly, Set<DataType> dataTypes, 
            String outputDir) throws IllegalArgumentException {
        log.traceEntry("{}, {}, {}, {}", speciesIds, anatEntityOnly, dataTypes, outputDir);
        
        if (dataTypes == null || dataTypes.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Data types must be specified."));
        }
        
        ServiceFactory serviceFactory = this.serviceFactorySupplier.get();
        //Retrieve requested species, or all species if none were requested
        Set<Integer> retrievedSpeciesIds = serviceFactory.getSpeciesService().loadSpeciesByIds(
                speciesIds, false).stream().map(s -> s.getId()).collect(Collectors.toSet());
        if (speciesIds != null && !speciesIds.isEmpty() && !retrievedSpeciesIds.containsAll(speciesIds)) {
            throw log.throwing(new IllegalArgumentException("Some species IDs were not recognized: "
                    + speciesIds.stream().filter(id -> !retrievedSpeciesIds.contains(id))
                    .collect(Collectors.toSet()).toString()));
        }
        
        //generation of files are independent, so we can safely go multi-threading
        retrievedSpeciesIds.parallelStream().forEach(speciesId -> dataTypes.parallelStream().forEach(
                dataType -> {
                    try {
                        this.generateSpeciesRankFile(speciesId, anatEntityOnly, dataType, outputDir);
                    } catch (IOException e) {
                        throw log.throwing(new RuntimeException(e));
                    }
                }));
        
        log.traceExit();
    }
    
    /**
     * Generate a rank file for a given species and data type specification. 
     * 
     * @param speciesId         A {@code String} that are the ID of the species which to generate 
     *                          the file for. 
     * @param anatEntityOnly    A {@code boolean} defining whether the file should be generated  
     *                          by grouping data by gene-anatomical entity (if {@code true}), 
     *                          or by considering all condition information (such as developmental stage, 
     *                          if {@code false}).
     * @param dataType          The {@code DataType} that should be considered to generate the file. 
     *                          If {@code null}, then all data types are considered. 
     * @param outputDir         A {@code String} that is the path to the directory where to store 
     *                          the generated file.
     * @throws IllegalArgumentException If the species ID is not recognized. 
     * @throws IOException             If an error occurs while writing the file. 
     */
    public void generateSpeciesRankFile(Integer speciesId, boolean anatEntityOnly, DataType dataType, 
            String outputDir) throws IllegalArgumentException, IOException {
        log.traceEntry("{}, {}, {}, {}", speciesId, anatEntityOnly, dataType, outputDir);

        //********************
        // DATA RETRIEVAL
        //********************
        ServiceFactory serviceFactory = this.serviceFactorySupplier.get();
        //Retrieve the species 
        Species species = null;
        try {
            species = serviceFactory.getSpeciesService().loadSpeciesByIds(
                    new HashSet<>(Arrays.asList(speciesId)), false).stream().findFirst().get();
        } catch (NoSuchElementException e) {
            throw log.throwing(new IllegalArgumentException("No species with ID " + speciesId));
        }
        assert species != null;
        
        //Retrieve the genes of the species, mapped to their gene IDs, notably to display their names
        Map<String, Gene> genes = serviceFactory.getGeneService()
                .loadGenes(new GeneFilter(speciesId))
                .collect(Collectors.toMap(g -> g.getGeneId(), g -> g));
        
        //Load ontologies with all data for the requested species, will avoid to make one query 
        //for each gene
        //If only anat. entities are needed devStageOntology does not need to be created
        Ontology<AnatEntity, String> anatEntityOnt = serviceFactory.getOntologyService()
                .getAnatEntityOntology(speciesId, null);
        Ontology<DevStage, String> devStageOnt = null;
        Ontology<Sex, String> sexOnt = null;
        Ontology<Strain, String> strainOnt = null;
        if (!anatEntityOnly) {
            devStageOnt = serviceFactory.getOntologyService().getDevStageOntology(speciesId, null);
            //TODO: change how to manage sex ontology. here ontology with all potential sexes is retrieved
            // even if the sex is not present in the species (e.g hermaphrodite)
            sexOnt = serviceFactory.getOntologyService().getSexOntology();
            strainOnt = serviceFactory.getOntologyService().getStrainOntology(speciesId);
        }
        
        //Query expression data for the species. 
        Iterator<ExpressionCall> callIt = this.getExpressionCalls(speciesId, anatEntityOnly, 
                dataType, serviceFactory).iterator();
        

        //********************
        // COMPUTATIONS AND WRITING INTO FILE
        //********************
        //Open TSV file for writing. We'll write in tmp file and will move it at the end 
        //if everything worked fine
        File tmpOutputFile = getOutputFile(species, anatEntityOnly, dataType, outputDir, true);
        Files.deleteIfExists(tmpOutputFile.toPath());
        String[] header = getFileHeader(anatEntityOnly, dataType);
        String[] colToAttribute = getColToAttributeMapping(anatEntityOnly, dataType);
        CellProcessor[] processors = getCellProcessors(anatEntityOnly, dataType);
        boolean rowWritten = false;
        try (ICsvBeanWriter beanWriter = new CsvBeanWriter(new FileWriter(tmpOutputFile), 
                Utils.TSVCOMMENTED)) {
            
            // write the header
            beanWriter.writeHeader(header);
        
            
            //We load all data from one gene at a time in memory, for clustering and redundancy discovery, 
            //and write them into file
            List<ExpressionCall> singleGeneExprCalls = new ArrayList<>();
            String geneId = null;
            String previousGeneId = null;
            while (callIt.hasNext()) {
                rowWritten = true;
                ExpressionCall call = callIt.next();
                geneId = call.getGene().getGeneId();
                
                if (previousGeneId != null && !geneId.equals(previousGeneId)) {
                    assert previousGeneId.compareTo(geneId) < 0: 
                        "Calls should be ordered by ascending gene IDs";
                    //launch the computations and writing into file for the previous gene. 
                    //Note that the List will be reordered
                    this.processAndWriteToFile(singleGeneExprCalls, genes.get(previousGeneId), 
                            anatEntityOnt, devStageOnt, sexOnt, strainOnt,
                            beanWriter, colToAttribute, processors);
                    //start a new accumulation of calls for the new gene
                    singleGeneExprCalls = new ArrayList<>();
                }
                singleGeneExprCalls.add(call);
                previousGeneId = geneId;
            }
            //computation and writing for last iterated gene
            if (!singleGeneExprCalls.isEmpty()) {
                assert geneId != null && geneId.equals(previousGeneId);
                this.processAndWriteToFile(singleGeneExprCalls, genes.get(geneId), 
                        anatEntityOnt, devStageOnt, sexOnt, strainOnt,
                        beanWriter, colToAttribute, processors);
            }
        }
        if (!rowWritten) {
            Files.deleteIfExists(tmpOutputFile.toPath());
        } else {
            File outputFile = getOutputFile(species, anatEntityOnly, dataType, outputDir, false);
            Files.deleteIfExists(outputFile.toPath());
            Files.move(tmpOutputFile.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        
        log.traceExit();
    }
    
    /**
     * Retrieve {@code ExpressionCall}s from a {@code CallService}.
     * 
     * @param speciesId         A {@code String} that are the ID of the species which to retrieve 
     *                          the calls for.
     * @param anatEntityOnly    A {@code boolean} defining whether the calls should be retrieved  
     *                          by grouping data by gene-anatomical entity (if {@code true}), 
     *                          or by considering all condition information (such as developmental stage, 
     *                          if {@code false}).
     * @param dataType          The {@code DataType} that should be considered to retrieve the calls. 
     *                          If {@code null}, then all data types are considered. 
     * @param serviceFactory    A {@code ServiceFactory} to retrieve Bgee services from.
     * @return                  A {@code Stream} of the {@code ExpressionCall}s retrieved. 
     */
    private Stream<ExpressionCall> getExpressionCalls(Integer speciesId, boolean anatEntityOnly, 
            DataType dataType, ServiceFactory serviceFactory) {
        log.traceEntry("{}, {}, {}, {}", speciesId, anatEntityOnly, dataType, serviceFactory);
        
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = 
                new LinkedHashMap<>();
        //The ordering by gene ID is essential here, because we will load into memory 
        //all data from one gene at a time, for clustering and redundancy discovery. 
        //The ordering by rank is not mandatory, for a given gene we are going to reorder anyway
        serviceOrdering.put(CallService.OrderingAttribute.GENE_ID, Service.Direction.ASC);
        serviceOrdering.put(CallService.OrderingAttribute.MEAN_RANK, Service.Direction.ASC);
        //XXX: originally we wanted to order using ExpressionCall.RankComparator, 
        //to detect redundant calls and order most precise conditions first, 
        //but it is too slow, so we do a basic ordering in the query
        serviceOrdering.put(CallService.OrderingAttribute.ANAT_ENTITY_ID, Service.Direction.ASC);
        serviceOrdering.put(CallService.OrderingAttribute.CELL_TYPE_ID, Service.Direction.ASC);
        if (!anatEntityOnly) {
            serviceOrdering.put(CallService.OrderingAttribute.DEV_STAGE_ID, Service.Direction.ASC);
        }
        
        Set<CallService.Attribute> attrs = EnumSet.of(
                CallService.Attribute.GENE, CallService.Attribute.ANAT_ENTITY_ID,
                CallService.Attribute.CELL_TYPE_ID,
                CallService.Attribute.MEAN_RANK);
        if (!anatEntityOnly) {
            attrs.add(CallService.Attribute.DEV_STAGE_ID);
        }

        // Do not want to create a List containing only one null value
        List<DataType> dataTypeFilters = null;
        if(dataType != null){
            dataTypeFilters = Arrays.asList(dataType);
        }
        
        // XXX: deactivate this until MySQLExpressionCallDAO is debugged.
        // if (dataType == null) {
        // attrs.add(CallService.Attribute.CALL_DATA);
        // attrs.add(CallService.Attribute.GLOBAL_DATA_QUALITY);
        // }
        CallService service = serviceFactory.getCallService();
        // if anatEntity only we need to retrieve silver calls for anat entity and then retrieve
        // bronze calls for condition in order to retrieve the smallest rank for all dev stages.
        if (anatEntityOnly) {
            return log.traceExit(service.loadExpressionCalls(
                    new ExpressionCallFilter(ExpressionCallFilter.SILVER_PRESENT_ARGUMENT,
                            Collections.singleton(new GeneFilter(speciesId)), null, dataTypeFilters,
                            ExpressionCallFilter.ANAT_ENTITY_OBSERVED_DATA_ARGUMENT),
                    //service ordering is null because we need to retrieve highest rank for bronze organ-stage calls
                    //before sorting by rank
                    attrs, null));
        }

        // Now, we manage the retrieving of calls by considering anatEntity and devStage
        serviceOrdering.put(CallService.OrderingAttribute.DEV_STAGE_ID, Service.Direction.ASC);
        attrs.add(CallService.Attribute.DEV_STAGE_ID);
        Map<EnumSet<CallService.Attribute>, Boolean> callObservedDataFilter = new HashMap<>();
        callObservedDataFilter.put(EnumSet.of(CallService.Attribute.ANAT_ENTITY_ID,
                CallService.Attribute.CELL_TYPE_ID, CallService.Attribute.DEV_STAGE_ID), true);
        return log.traceExit(service.loadExpressionCalls(
                new ExpressionCallFilter(ExpressionCallFilter.SILVER_PRESENT_ARGUMENT,
                        Collections.singleton(new GeneFilter(speciesId)),
                        null, dataTypeFilters,
                        callObservedDataFilter),
                attrs,
                serviceOrdering));

    }

    /**
     * Process and write to file all the {@code ExpressionCall}s related to one gene.
     * 
     * @param singleGeneExprCalls   A {@code List} of all {@code ExpressionCall}s related to one gene. 
     *                              Note that it will be reordered. 
     * @param gene                  The {@code Gene} the {@code ExpressionCall}s are related to.
     * @param anatEntityOnt         An {@code Ontology} containing all the {@code AnatEntity}s 
     *                              of the related species. Will be used to obtain {@code ConditionGraph}s. 
     * @param devStageOnt           An {@code Ontology} containing all the {@code DevStage}s 
     *                              of the related species. Will be used to obtain {@code ConditionGraph}s.
     * @param beanWriter            An {@code ICsvBeanWriter} used to write {@code ExpressionCallBean}s 
     *                              into a TSV file.
     * @param colToAttribute        An {@code Array} of {@code String}s providing the names 
     *                              of the attributes of {@code ExpressionCallBean} in the same order 
     *                              as the columns they should be written in. 
     *                              See {@link #getColToAttributeMapping(boolean, DataType)}.
     * @param processors            An {@code Array} of {@code CellProcessor}s in the same order 
     *                              as the columns they should process.
     *                              See {@link #getCellProcessors(boolean, DataType)}.
     * @throws RuntimeException         If an error occurs while generating the file. 
     *                                  It is not a checked exception because we use {@code Stream}s.
     */
    private void processAndWriteToFile(List<ExpressionCall> singleGeneExprCalls, 
            Gene gene, Ontology<AnatEntity, String> anatEntityOnt, Ontology<DevStage, String> devStageOnt, 
            Ontology<Sex, String> sexOnt, Ontology<Strain, String> strainOnt, 
            ICsvBeanWriter beanWriter, String[] colToAttribute, CellProcessor[] processors) 
                    throws RuntimeException {
        log.traceEntry("{}, {}, {}, {}, {}, {}, {}", singleGeneExprCalls, gene, anatEntityOnt, devStageOnt, 
                beanWriter, colToAttribute, processors);
        
        this.mapCallsToBeans(singleGeneExprCalls, gene, anatEntityOnt, devStageOnt, sexOnt, strainOnt)
        .forEachOrdered(bean -> {
            try {
                beanWriter.write(bean, colToAttribute, processors);
            } catch (Exception e) {
                throw log.throwing(new RuntimeException(e));
            }
        });

        log.traceExit();
    }

    /**
     * Process the {@code ExpressionCall}s related to one gene and map them to {@code ExpressionCallBean}s.
     * 
     * @param singleGeneExprCalls   A {@code List} of all {@code ExpressionCall}s related to one gene. 
     *                              Note that it will be reordered. 
     * @param gene                  The {@code Gene} the {@code ExpressionCall}s are related to.
     * @param anatEntityOnt         An {@code Ontology} containing all the {@code AnatEntity}s 
     *                              of the related species. Will be used to obtain {@code ConditionGraph}s. 
     * @param devStageOnt           An {@code Ontology} containing all the {@code DevStage}s 
     *                              of the related species. Will be used to obtain {@code ConditionGraph}s.
     * @return                      A {@code Stream} of {@code ExpressionCallBean}s. The {@code Stream} 
     *                              is sorted, it is important, and the sort might be different 
     *                              than the input list of {@code ExpressionCall}s.
     */
    private Stream<ExpressionCallBean> mapCallsToBeans(List<ExpressionCall> singleGeneExprCalls, 
            Gene gene, Ontology<AnatEntity, String> anatEntityOnt, Ontology<DevStage, String> devStageOnt,
            Ontology<Sex, String> sexOnt, Ontology<Strain, String> strainOnt) {
        log.traceEntry("{}, {}, {}, {}", singleGeneExprCalls, gene, anatEntityOnt, devStageOnt);

        //Instantiate a ConditionGraph for computations and for display purpose
        ServiceFactory serviceFactory = this.serviceFactorySupplier.get();
        ConditionGraph conditionGraph = serviceFactory.getConditionGraphService().loadConditionGraph( 
                singleGeneExprCalls.stream().map(ExpressionCall::getCondition).collect(Collectors.toSet()), 
                anatEntityOnt, devStageOnt, anatEntityOnt, sexOnt, strainOnt);

        //XXX: deactivate because too slow
//      //first, we rank the calls with the ExpressionCall.RankComparator, it is mandatory 
//      //for correct detection of redundant calls and consistency with the display. 
//      Collections.sort(singleGeneExprCalls, this.rankComparatorSupplier.apply(conditionGraph));
//      //identify redundant calls
//      Set<ExpressionCall> redundantCalls = this.redundantCallsFuncSupplier.apply(
//              singleGeneExprCalls, conditionGraph);
        
        //map ExpressionCalls to ExpressionCallBean to be written in output file
        return log.traceExit(singleGeneExprCalls.stream().map(c -> {
            if (!gene.getGeneId().equals(c.getGene().getGeneId())) {
                throw log.throwing(new IllegalArgumentException("The provided gene does not correspond to "
                        + "the expression calls."));
            }
            
            Condition cond = c.getCondition();
            AnatEntity anatEntity = conditionGraph.getAnatEntityOntology().getElement(cond.getAnatEntityId());
            DevStage devStage = null;
            // if only anat. entities are requested, devStage can be null
            if(cond.getDevStageId() != null){
                devStage = conditionGraph.getDevStageOntology().getElement(cond.getDevStageId());
            }
            //generate a Map DataType -> presence/absence of data
            Map<DataType, Boolean> dataTypeToStatus = Arrays.stream(DataType.values())
                    .collect(Collectors.toMap(
                            type -> type, 
                            type -> c.getCallData().stream().anyMatch(
                                    callData -> type.equals(callData.getDataType()) && 
                                    c.getSummaryQuality() != null && 
                                    !DataQuality.NODATA.equals(c.getSummaryQuality()))));
            return new ExpressionCallBean(
                c.getGene().getGeneId(), gene.getName(), 
                cond.getAnatEntityId(), anatEntity == null? null: anatEntity.getName(), 
                cond.getDevStageId(), devStage == null? null: devStage.getName(), 
                FORMATTER.apply(c.getMeanRank()), 
                dataTypeToStatus.get(DataType.AFFYMETRIX), 
                dataTypeToStatus.get(DataType.EST), 
                dataTypeToStatus.get(DataType.IN_SITU), 
                dataTypeToStatus.get(DataType.RNA_SEQ),
                dataTypeToStatus.get(DataType.SC_RNA_SEQ), 
                /*redundantCalls.contains(c)*/ false, 
                this.getBTOXRefs(c));
        }));
    }

    /**
     * Retrieve the XRefs to BTO for the anatomical entity retrieved from an {@code ExpressionCall}.
     * @param call  The {@code ExpressionCall} to retrieve the anat. entity ID from.
     * @return      A {@code List} of {@code String}s that are XRefs to BTO for the call, 
     *              in the order they were retrieved from Uberon.
     */
    //TODO: when Uberon xrefs will have been inserted into the database, use them, 
    //rather than needing to provide an ontology. 
    private List<String> getBTOXRefs(ExpressionCall call) {
        log.traceEntry("{}", call);
        
        //hack for adult mammalian kidney UBERON:0000082
        if ("UBERON:0000082".equals(call.getCondition().getAnatEntityId())) {
            return log.traceExit(Arrays.asList("BTO:0000671"));
        }

        OWLGraphWrapper wrapper = this.uberonOnt.getOntologyUtils().getWrapper();
        OWLClass cls = wrapper.getOWLClassByIdentifier(call.getCondition().getAnatEntityId(), true);
        List<String> xrefs = null;
        if (cls != null) {
            xrefs = wrapper.getXref(cls).stream().filter(xref -> xref.startsWith("BTO:"))
                    .collect(Collectors.toList());
        }
        return log.traceExit(xrefs == null || xrefs.isEmpty()? null: xrefs);
    }
}