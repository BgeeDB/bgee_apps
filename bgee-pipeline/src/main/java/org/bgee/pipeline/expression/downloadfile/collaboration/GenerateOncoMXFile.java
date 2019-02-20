package org.bgee.pipeline.expression.downloadfile.collaboration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.ontology.Ontology;
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

    /**
     * The different categories of expression level requested by OnoMX.
     *
     * @author Frederic Bastian
     * @since Bgee 14 Feb. 2019
     * @version Bgee 14 Feb. 2019
     */
    public static enum ExpressionLevelCat {
        //Note: order is from high expression to low expression on purpose,
        //used in method getExpressionLevelCat
        HIGH, MEDIUM, LOW;
    }

    // ************************************
    // STATIC ATTRIBUTES AND METHODS
    // ************************************
    /**
     * A {@code Set} of {DataType}s used to build the data for OncoMX.
     */
    private final static Set<DataType> DATA_TYPES = EnumSet.of(DataType.RNA_SEQ);

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
        log.exit();
    }

    private static ExpressionLevelCat getExpressionLevelCat(BigDecimal minRank, BigDecimal maxRank, BigDecimal currentRank)
    throws IllegalArgumentException {
        log.entry(minRank, maxRank, currentRank);
        if (minRank == null || maxRank == null || currentRank == null) {
            throw log.throwing(new IllegalArgumentException("All ranks must be provided"));
        }
        if (minRank.compareTo(maxRank) > 0) {
            throw log.throwing(new IllegalArgumentException("Inconsistent min and max rank, min rank: "
                    + minRank + ", max rank: " + maxRank));
        }
        if (currentRank.compareTo(minRank) < 0 || currentRank.compareTo(maxRank) > 0) {
            throw log.throwing(new IllegalArgumentException("Inconsistent current rank, min rank: "
                    + minRank + ", max rank: " + maxRank + ", current rank: " + currentRank));
        }

        //Get the category threshold.
        BigDecimal diff = maxRank.subtract(minRank);
        //First, if maxRank - minRank <= 100, everything is considered highest expression
        if (diff.compareTo(new BigDecimal("100")) <= 0) {
            return log.exit(ExpressionLevelCat.values()[0]);
        }
        //Otherwise, we compute the threshold and categories
        BigDecimal catCount = new BigDecimal(ExpressionLevelCat.values().length);
        int scale = Math.max(currentRank.scale(), Math.max(minRank.scale(), maxRank.scale()));
        BigDecimal threshold = diff.divide(catCount, scale, RoundingMode.HALF_UP);
        for (int i = 0 ; i < ExpressionLevelCat.values().length; i++) {
            BigDecimal catMax = minRank.add(threshold.multiply(new BigDecimal(i + 1)));
            if (currentRank.compareTo(catMax) <= 0) {
                return log.exit(ExpressionLevelCat.values()[i]);
            }
        }
        throw log.throwing(new IllegalStateException("No expression category could be assigned, min rank: "
                    + minRank + ", max rank: " + maxRank + ", current rank: " + currentRank));
    }
    private static BigDecimal getMinMax(BigDecimal a, BigDecimal b, boolean getMin) {
        log.entry(a, b, getMin);
        if (a == null) {
            return log.exit(b);
        }
        if (b == null) {
            return log.exit(a);
        }
        BigDecimal min = a.compareTo(b) < 0? a: b;
        BigDecimal max = a.compareTo(b) > 0? a: b;
        if (getMin) {
            return log.exit(min);
        }
        return log.exit(max);
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

        return log.exit(uberonIds);
    }
    private static List<ExpressionCall> getExpressionCalls(final CallService callService, int speciesId) {
        log.entry(callService, speciesId);

        //We want observed data for both expressed and not-expressed calls
        Map<CallType.Expression, Boolean> obsDataFilter = new HashMap<>();
        obsDataFilter.put(null, true);
        //For ordering by gene and rank score
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = 
                new LinkedHashMap<>();
        serviceOrdering.put(CallService.OrderingAttribute.GENE_ID, Service.Direction.ASC);
        serviceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.ASC);
        List<ExpressionCall> calls = callService.loadExpressionCalls(
                new ExpressionCallFilter(
                        null, //summaryCallTypeQualityFilter: we want expressed/not-expressed calls from all qualities
                        Collections.singleton(new GeneFilter(speciesId)), //all genes for the requested species
                        null, //all conditions
                        DATA_TYPES, //requested data for OncoMX only
                        obsDataFilter, //only observed data
                        null, null //no filter on observed data in anat. entity and stage, it will anyway be both from the previous filter
                        ),
                EnumSet.of(CallService.Attribute.GENE, CallService.Attribute.ANAT_ENTITY_ID,
                        CallService.Attribute.DEV_STAGE_ID, CallService.Attribute.CALL_TYPE, 
                        CallService.Attribute.DATA_QUALITY, CallService.Attribute.GLOBAL_MEAN_RANK,
                        CallService.Attribute.EXPERIMENT_COUNTS),
                serviceOrdering //we order by gene and rank
                ).collect(Collectors.toList());

        return log.exit(calls);
    }


    // ************************************
    // INSTANCE ATTRIBUTES AND METHODS
    // ************************************
    private final ServiceFactory serviceFactory;

    public GenerateOncoMXFile() {
        this(new ServiceFactory());
    }
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
        //Now we load the anatomical ontology in order to retrieve child terms of the requested terms
        final Ontology<AnatEntity, String> anatOnt = this.serviceFactory.getOntologyService()
                .getAnatEntityOntology(speciesId, uberonIds, EnumSet.of(RelationType.ISA_PARTOF), false, true);
        Set<AnatEntity> anatEntities = uberonIds.stream()
                .map(id -> {
                    AnatEntity anatEntity = anatOnt.getElement(id);
                    if (anatEntity == null) {
                        log.warn("Could not find Uberon ID in Ontology: {}", id);
                    }
                    return anatEntity;
                })
                .filter(ae -> ae != null)
                .collect(Collectors.toSet());
        Set<String> allUberonIds = anatEntities.stream()
                .flatMap(ae -> anatOnt.getDescendants(ae).stream())
                .map(anatEntity -> anatEntity.getId())
                .collect(Collectors.toSet());
        allUberonIds.addAll(anatEntities.stream().map(ae -> ae.getId()).collect(Collectors.toSet()));

        //Now we load the developmental stage ontology to retrieve all children terms
        //of the stage selected for OncoMX.
        final Ontology<DevStage, String> devStageOnt = this.serviceFactory.getOntologyService()
                .getDevStageOntology(speciesId, selectedDevStageIds, false, true);
        Set<DevStage> devStages = selectedDevStageIds.stream()
                .map(id -> {
                    DevStage devStage = devStageOnt.getElement(id);
                    if (devStage == null) {
                        log.warn("Could not find stage ID in Ontology: {}", id);
                    }
                    return devStage;
                })
                .filter(ds -> ds != null)
                .collect(Collectors.toSet());
        Set<String> allDevStageIds = devStages.stream()
                .flatMap(ds -> devStageOnt.getDescendants(ds).stream())
                .map(devStage -> devStage.getId())
                .collect(Collectors.toSet());
        allDevStageIds.addAll(devStages.stream().map(ds -> ds.getId()).collect(Collectors.toSet()));
        log.info("Done.");
        

        log.info("Retrieval of expression calls...");
        //Now we retrieve all calls per organ-stage, to be able to create the classification
        //high expression level/medium/low for genes and organs
        List<ExpressionCall> calls = getExpressionCalls(this.serviceFactory.getCallService(), speciesId);
        log.info("Done.");

        log.info("Grouping calls and retrieving min/max ranks per aggregate...");
        //We create a Collector function to retrieve min and max ranks from an aggregate of calls
        Collector<ExpressionCall, ?, Pair<BigDecimal, BigDecimal>> aggregateRank = 
                Collectors.mapping(c -> ImmutablePair.of(c.getGlobalMeanRank(), c.getGlobalMeanRank()),
                    Collectors.reducing(ImmutablePair.of(null, null),
                        (a, b) -> ImmutablePair.of(
                                getMinMax(a.getLeft(), b.getLeft(), true),
                                getMinMax(a.getRight(), b.getRight(), false))
                    )
                );
        //Now for each organ we retrieve the min rank and max rank of expressed calls.
        Map<AnatEntity, Pair<BigDecimal, BigDecimal>> minMaxRanksPerAnatEntity = calls.stream()
                .filter(c -> c.getSummaryCallType().equals(ExpressionSummary.EXPRESSED))
                .collect(Collectors.groupingBy(c -> c.getCondition().getAnatEntity(), aggregateRank));
        //We do the same for genes
        Map<Gene, Pair<BigDecimal, BigDecimal>> minMaxRanksPerGene = calls.stream()
                .filter(c -> c.getSummaryCallType().equals(ExpressionSummary.EXPRESSED))
                .collect(Collectors.groupingBy(c -> c.getGene(), aggregateRank));
        log.info("Done.");


        log.info("Start iterating the calls and writing to output file...");
        //Now, we iterate the calls to write in the output file after retrieving the expression categories,
        //and filtering for requested anatomical entities/dev. stages only.
        // We will write results in temporary files that we will rename at the end
        // if everything is correct
        String tmpExtension = ".tmp";
        String fileName = (speciesLatinName
                + selectedDevStageIds.stream().collect(Collectors.joining("_"))
                + DATA_TYPES.stream().map(d -> d.toString()).collect(Collectors.joining("_")))
                .replaceAll(" ", "_") + ".tsv";
        File tmpFile = new File(outputDirectory, fileName + tmpExtension);
        // override any existing file
        if (tmpFile.exists()) {
            tmpFile.delete();
        }
        //We use a quick and dirty ListWriter
        int dataRowCount = 0;
        try (ICsvListWriter listWriter = new CsvListWriter(new FileWriter(tmpFile), Utils.TSVCOMMENTED)) {
            final String[] header = new String[] { "Ensembl gene ID", "Gene name",
                    "Anatomical entity ID", "Anatomical entity name",
                    "Developmental stage ID", "Developmental stage name",
                    "Gene expression category", "Anatomical entity expression category",
                    "Call quality", "Expression rank score" };
            // write the header
            listWriter.writeHeader(header);

            //allowed expression categories
            Set<Object> allowedExpressionCategories = Arrays.stream(ExpressionLevelCat.values())
                    .map(c -> c.toString())
                    .collect(Collectors.toSet());
            allowedExpressionCategories.add(ExpressionSummary.NOT_EXPRESSED.toString());
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
                    new StrNotNullOrEmpty() // rank score
            };

            //Write the calls
            for (ExpressionCall call: calls) {
                //Check whether it is a call to be written
                if (!allUberonIds.contains(call.getCondition().getAnatEntity().getId())) {
                    continue;
                }
                if (!allDevStageIds.contains(call.getCondition().getDevStage().getId())) {
                    continue;
                }
                List<Object> toWrite = new ArrayList<>();
                try {
                    toWrite.add(call.getGene().getEnsemblGeneId());
                    toWrite.add(call.getGene().getName());
                    toWrite.add(call.getCondition().getAnatEntity().getId());
                    toWrite.add(call.getCondition().getAnatEntity().getName());
                    toWrite.add(call.getCondition().getDevStage().getId());
                    toWrite.add(call.getCondition().getDevStage().getName());
                    Pair<BigDecimal, BigDecimal> geneMinMaxRank = minMaxRanksPerGene.get(call.getGene());
                    toWrite.add(getExpressionLevelCat(geneMinMaxRank.getLeft(), geneMinMaxRank.getRight(),
                            call.getGlobalMeanRank()).toString());
                    Pair<BigDecimal, BigDecimal> anatEntityMinMaxRank = minMaxRanksPerAnatEntity.get(
                            call.getCondition().getAnatEntity());
                    toWrite.add(getExpressionLevelCat(anatEntityMinMaxRank.getLeft(), anatEntityMinMaxRank.getRight(),
                            call.getGlobalMeanRank()).toString());
                    toWrite.add(call.getSummaryQuality().toString());
                    toWrite.add(call.getFormattedGlobalMeanRank());
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
        
        log.exit();
    }
}