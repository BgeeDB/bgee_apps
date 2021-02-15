package org.bgee.pipeline.expression.downloadfile.collaboration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.NamedEntity;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.ConditionFilter;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.ExpressionLevelCategory;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.ontology.OntologyElement;
import org.bgee.model.ontology.RelationType;
import org.bgee.pipeline.CommandRunner;
import org.bgee.pipeline.Utils;
import org.supercsv.cellprocessor.StrReplace;
import org.supercsv.cellprocessor.Trim;
import org.supercsv.cellprocessor.constraint.IsIncludedIn;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

/**
 * Class used to generate data for the OncoMX project.
 *
 * @author Frederic Bastian
 * @since Bgee 14 Feb. 2019
 * @version Bgee 14 Feb. 2019
 */
public class GenerateOncoMXFile {
    private final static Logger log = LogManager.getLogger(GenerateOncoMXFile.class.getName());

    // ************************************
    // STATIC ATTRIBUTES AND METHODS
    // ************************************
    /**
     * A {@code Set} of {DataType}s used to build the data for OncoMX.
     */
    private final static Set<DataType> DATA_TYPES = EnumSet.of(DataType.RNA_SEQ, DataType.AFFYMETRIX);

    /**
     * Launches the generation of the files used by OncoMX.
     * Parameters that must be provided in order in {@code args} are:
     * <ol>
     * <li>The path to the file containing the Uberon IDs to consider, provided by OncoMX.
     * An example of this file is provided in {@code src/test/resources/oncomx/human_doid_slim_uberon_mapping.csv}.
     * Children of the Uberon terms retrieved will also be considered.
     * <li>Path to the output directory where to store the generated files.
     * <li> a {@code Map} where keys are {@code int} that are the IDs of the species to generate the files for,
     * and each value is a set of strings, corresponding to developmental stage IDs to retrieve data for.
     * Children of these stages will also be considered.
     * Example: 9606//UBERON:0000113,10090//UBERON:0000113
     * </ol>
     *
     * @param args                      An {@code Array} of {@code String}s containing the requested parameters.
     * @throws FileNotFoundException    IF a file could not be found.
     * @throws IOException              If an error occurred while reading/writing a file.
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        log.entry((Object[]) args);

        int expectedArgLength = 3;
        if (args.length != expectedArgLength) {
            throw log.throwing(new IllegalArgumentException("Incorrect number of arguments " +
                "provided, expected " + expectedArgLength + " arguments, " + args.length + 
                " provided."));
        }

        String pathToOncoMXUberonTermFile = args[0];
        String outputDir = args[1];
        LinkedHashMap<Integer, List<String>> speIdToStageIds = CommandRunner.parseMapArgumentAsIntKeysStringValues(args[2]);
        GenerateOncoMXFile generateOncoMX = new GenerateOncoMXFile();
        for (Entry<Integer, List<String>> entry: speIdToStageIds.entrySet()) {
            generateOncoMX.generateFile(entry.getKey(), entry.getValue(), pathToOncoMXUberonTermFile, outputDir);
        }
        log.traceExit();
    }

    /**
     * Retrieve the Uberon IDs requested by OncoMX from the file they provide.
     *
     * @param oncomxUberonTermFile      A {@code String} that is the path to the file provided by OncoMX.
     * @return                          A {@code Set} of {@code String}s that are the IDs of the Uberon terms
     *                                  to use.
     * @throws FileNotFoundException    If the OncoMX file could not be found.
     * @throws IOException              If the OncoMX file could not be read.
     */
    private static final Set<String> retrieveRequestedUberonIds(String oncomxUberonTermFile)
            throws FileNotFoundException, IOException {
        log.entry(oncomxUberonTermFile);

        Set<String> uberonIds = new HashSet<>();

        //Load Uberon terms requested by OncoMX in the CSV file they provide.
        try (ICsvMapReader reader = new CsvMapReader(new FileReader(oncomxUberonTermFile), CsvPreference.STANDARD_PREFERENCE)) {

            final String uberonColName = "UBERON_doid.owl";
            reader.getHeader(true); // skip past the header (we're defining our own)
            //The file contains 4 columns, we're only interested in the second one, containing Uberon IDs.
            //Only map the second column - setting header elements to null means those columns are ignored
            final String[] header = new String[] {null, uberonColName, null, null};
            final CellProcessor[] processors = new CellProcessor[] { 
                    null,          //Slim DO ID
                    new NotNull(new Trim(new StrReplace("N/A", ""))), //Uberon ID
                    null,          //Uberon name
                    null           //Notes
            };
            Map<String, Object> rowMap;
            while( (rowMap = reader.read(header, processors)) != null ) {
                String uberonId = (String) rowMap.get(uberonColName);
                //We skip empty Uberon IDs (N/A replaced with empty strings)
                if (StringUtils.isBlank(uberonId)) {
                    continue;
                }
                uberonIds.add(uberonId.replace("_", ":"));
            }
        }

        return log.traceExit(uberonIds);
    }

    private final static <T extends NamedEntity<U> & OntologyElement<T, U>, U extends Comparable<U>>
    Set<U> getAllEntityIds(Ontology<T, U> ont, Set<U> seedEntityIds) {
        log.entry(ont, seedEntityIds);
        Set<T> entities = seedEntityIds.stream()
                .map(id -> {
                    T entity = ont.getElement(id);
                    if (entity == null) {
                        log.warn("Could not find ID in Ontology: {}", id);
                    }
                    return entity;
                })
                .filter(e -> e != null)
                .collect(Collectors.toSet());
        Set<U> allEntityIds = entities.stream()
                .flatMap(e -> ont.getDescendants(e).stream())
                .map(e -> e.getId())
                .collect(Collectors.toSet());
        allEntityIds.addAll(entities.stream().map(e -> e.getId()).collect(Collectors.toSet()));
        return log.traceExit(allEntityIds);
    }

    protected static ExpressionCallFilter getGeneCallFilter(int speciesId, Collection<String> anatEntityIds,
            Collection<String> devStageIds) {
        log.entry(speciesId, anatEntityIds, devStageIds);
        //We want observed data only for any call type
        Map<CallType.Expression, Boolean> obsDataFilter = new HashMap<>();
        obsDataFilter.put(null, true);
        return log.traceExit(new ExpressionCallFilter(
                        null, //we want expressed/not-expressed calls of any quality
                        //all genes for the requested species
                        Collections.singleton(new GeneFilter(speciesId)),
                        //calls for the requested anat. entities and dev. stages
                        Collections.singleton(new ConditionFilter(anatEntityIds, devStageIds)),
                        DATA_TYPES, //data requested by OncoMX only
                        obsDataFilter, //only observed data
                        //no filter on observed data in anat. entity and stage,
                        //it will anyway be both from the previous filter
                        null, null
                        ));
    }
    protected static Set<CallService.Attribute> getGeneCallAttributes() {
        log.entry();
        return log.traceExit(EnumSet.of(CallService.Attribute.GENE,
                CallService.Attribute.ANAT_ENTITY_ID,
                CallService.Attribute.DEV_STAGE_ID,
                CallService.Attribute.CALL_TYPE, CallService.Attribute.DATA_QUALITY,
                CallService.Attribute.MEAN_RANK, CallService.Attribute.EXPRESSION_SCORE,
                CallService.Attribute.GENE_QUAL_EXPR_LEVEL,
                CallService.Attribute.ANAT_ENTITY_QUAL_EXPR_LEVEL));
    }
    protected static LinkedHashMap<CallService.OrderingAttribute, Service.Direction>
    getGeneServiceOrdering() {
        log.entry();
        //For ordering by gene and rank score, to retrieve the min and max ranks per gene
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = 
                new LinkedHashMap<>();
        serviceOrdering.put(CallService.OrderingAttribute.GENE_ID, Service.Direction.ASC);
        serviceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.ASC);
        serviceOrdering.put(CallService.OrderingAttribute.ANAT_ENTITY_ID, Service.Direction.ASC);
        serviceOrdering.put(CallService.OrderingAttribute.DEV_STAGE_ID, Service.Direction.ASC);
        return log.traceExit(serviceOrdering);
    }
    protected static String[] getHeader() {
        log.entry();
        return log.traceExit(new String[] { "Ensembl gene ID", "Gene name",
                    "Anatomical entity ID", "Anatomical entity name",
                    "Developmental stage ID", "Developmental stage name",
                    "Expression level relative to gene",
                    "Expression level relative to anatomical entity",
                    "Call quality", "Expression rank score", "Expression score" });
    }

    // ************************************
    // INSTANCE ATTRIBUTES AND METHODS
    // ************************************
    private final ServiceFactory serviceFactory;

    public GenerateOncoMXFile() {
        this(new ServiceFactory());
    }
    //XXX: allows one thread per species by provided a Supplier<ServiceFactory>?
    public GenerateOncoMXFile(ServiceFactory serviceFactory) {
        if (serviceFactory == null) {
            throw log.throwing(new IllegalArgumentException("ServiceFactory cannot be null"));
        }
        this.serviceFactory = serviceFactory;
    }

    public void generateFile(int speciesId, Collection<String> devStageIds,
            String oncomxUberonTermFile, String outputDirectory) throws FileNotFoundException, IOException {
        log.entry(speciesId, devStageIds, oncomxUberonTermFile, outputDirectory);

        log.info("Generating OncoMX file for species {}", speciesId);
        if (devStageIds == null || devStageIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Some stage IDs must be provided"));
        }
        //We use a LinkedHashSet for consistent generation of file name
        Set<String> selectedDevStageIds = devStageIds.stream().sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
        //Check that the species ID is valid and retrieve its latin name
        String speciesLatinName = Utils.checkAndGetLatinNamesBySpeciesIds(Collections.singleton(speciesId),
                this.serviceFactory.getSpeciesService()).get(speciesId);

        //Load Uberon terms requested by OncoMX in the CSV file they provide.
        Set<String> uberonIds = retrieveRequestedUberonIds(oncomxUberonTermFile);

        log.info("Retrieval of ontology terms...");
        //We load the anatomical entity ontology to retrieve all children terms
        //of the anatomical entities selected for OncoMX.
        final Ontology<AnatEntity, String> anatOnt = this.serviceFactory.getOntologyService()
                .getAnatEntityOntology(speciesId, uberonIds, EnumSet.of(RelationType.ISA_PARTOF), false, true);
        Set<String> allUberonIds = getAllEntityIds(anatOnt, uberonIds);
        //Now we load the developmental stage ontology to retrieve all children terms
        //of the stage selected for OncoMX.
        final Ontology<DevStage, String> devStageOnt = this.serviceFactory.getOntologyService()
                .getDevStageOntology(speciesId, selectedDevStageIds, false, true);
        Set<String> allDevStageIds = getAllEntityIds(devStageOnt, selectedDevStageIds);
        log.info("Done.");

        log.info("Retrieving expression calls...");
        try (Stream<ExpressionCall> callStream = this.serviceFactory.getCallService().loadExpressionCalls(
                getGeneCallFilter(speciesId, allUberonIds, allDevStageIds),
                //Attributes requested
                getGeneCallAttributes(),
                getGeneServiceOrdering() //we order by gene and rank
                )) {
            log.info("Done.");


            log.info("Start iterating the calls and writing to output file...");
            //Now, we iterate the calls to write in the output file after retrieving the expression categories,
            //and filtering for requested anatomical entities/dev. stages only.
            // We will write results in temporary files that we will rename at the end
            // if everything is correct
            String tmpExtension = ".tmp";
            String fileName = (speciesLatinName + "_"
                    + selectedDevStageIds.stream().collect(Collectors.joining("_")) + "_"
                    + DATA_TYPES.stream().map(d -> d.toString()).sorted().collect(Collectors.joining("_")))
                    .replaceAll(" ", "_") + ".tsv";
            File tmpFile = new File(outputDirectory, fileName + tmpExtension);
            // override any existing file
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
            //We use a quick and dirty ListWriter
            int dataRowCount = 0;
            try (ICsvListWriter listWriter = new CsvListWriter(new FileWriter(tmpFile), Utils.TSVCOMMENTED)) {
                final String[] header = getHeader();
                // write the header
                listWriter.writeHeader(header);

                //allowed expression categories
                Set<Object> allowedExpressionCategories = Arrays.stream(ExpressionLevelCategory.values())
                        .map(c -> c.toString())
                        .collect(Collectors.toSet());
                //allowed call qualities
                Set<Object> allowedCallQualities = Arrays.stream(SummaryQuality.values())
                        .map(q -> q.toString())
                        .collect(Collectors.toSet());
                //CellProcessors
                final CellProcessor[] processors = new CellProcessor[] { 
                        new StrNotNullOrEmpty(), // gene ID
                        new NotNull(), // gene name
                        new StrNotNullOrEmpty(), // anat. entity ID
                        new StrNotNullOrEmpty(), // anat. entity name
                        new StrNotNullOrEmpty(), // dev. stage ID
                        new StrNotNullOrEmpty(), // dev. stage name
                        new IsIncludedIn(allowedExpressionCategories), // gene expression cat.
                        new IsIncludedIn(allowedExpressionCategories), // anat. entity expression cat.
                        new IsIncludedIn(allowedCallQualities), // call qual.
                        new StrNotNullOrEmpty(), // rank score
                        new StrNotNullOrEmpty()  // expression score
                };

                //Write the calls
                Iterator<ExpressionCall> callIterator = callStream.iterator();
                while (callIterator.hasNext()) {
                    ExpressionCall call = callIterator.next();
                    assert allUberonIds.contains(call.getCondition().getAnatEntity().getId()) &&
                    allDevStageIds.contains(call.getCondition().getDevStage().getId());

                    List<Object> toWrite = new ArrayList<>();
                    try {
                        toWrite.add(call.getGene().getEnsemblGeneId());
                        toWrite.add(call.getGene().getName());
                        toWrite.add(call.getCondition().getAnatEntity().getId());
                        toWrite.add(call.getCondition().getAnatEntity().getName());
                        toWrite.add(call.getCondition().getDevStage().getId());
                        toWrite.add(call.getCondition().getDevStage().getName());
                        toWrite.add(call.getExpressionLevelInfo().getQualExprLevelRelativeToGene()
                                .getExpressionLevelCategory().toString());
                        toWrite.add(call.getExpressionLevelInfo().getQualExprLevelRelativeToAnatEntity()
                                .getExpressionLevelCategory().toString());
                        toWrite.add(call.getSummaryQuality().toString());
                        toWrite.add(call.getFormattedMeanRank());
                        toWrite.add(call.getFormattedExpressionScore());
                    } catch (IllegalArgumentException e) {
                        log.error("Error with call: {}", call);
                        throw log.throwing(e);
                    }

                    listWriter.write(toWrite, processors);
                    dataRowCount++;
                }
            } catch (Exception e) {
                if (tmpFile.exists()) {
                    tmpFile.delete();
                }
                throw e;
            }
            // now, if everything went fine, we rename or delete the temporary files
            if (dataRowCount > 0) {
                log.info("Done, file for the species {} contains {} rows without including header.",
                        speciesId, dataRowCount);
                File file = new File(outputDirectory, fileName);
                if (tmpFile.exists()) {
                    tmpFile.renameTo(file);
                }            
            } else {
                log.warn("Done, file for the species {} contains no rows.", speciesId);
                if (tmpFile.exists()) {
                    tmpFile.delete();
                }
            }
        }
        
        log.traceExit();
    }
}