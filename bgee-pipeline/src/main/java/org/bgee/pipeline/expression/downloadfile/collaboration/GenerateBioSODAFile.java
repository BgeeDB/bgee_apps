package org.bgee.pipeline.expression.downloadfile.collaboration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.ElementGroupFromListSpliterator;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.multispemapping.AnatEntitySimilarity;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.multispecies.SimilarityExpressionCall;
import org.bgee.model.gene.Gene;
import org.bgee.model.species.Species;
import org.bgee.model.species.Taxon;
import org.bgee.pipeline.CommandRunner;
import org.bgee.pipeline.Utils;
import org.supercsv.cellprocessor.constraint.IsIncludedIn;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.constraint.Unique;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListWriter;

/**
 * Class used to generate data for the BIO-SODA project.
 *
 * @author Frederic Bastian
 * @since Bgee 14 Mar. 2019
 * @version Bgee 14 Mar. 2019
 */
//TODO: unit tests
public class GenerateBioSODAFile {
    private final static Logger log = LogManager.getLogger(GenerateBioSODAFile.class.getName());

    private static final String NO_DATA = "NO DATA";
    /**
     * Launches the generation of the files used by Bio-SODA.
     * Parameters that must be provided in order in {@code args} are:
     * <ol>
     * <li>Path to the output directory where to store the generated files.
     * <li> a {@code List} of {@code int}s that are the NCBI IDs of the taxa to generate the files for.
     * </ol>
     *
     * @param args                      An {@code Array} of {@code String}s containing the requested parameters.
     */
    public static void main(String[] args) throws IOException {
        log.entry((Object[]) args);

        int expectedArgLength = 2;
        if (args.length != expectedArgLength) {
            throw log.throwing(new IllegalArgumentException("Incorrect number of arguments " +
                "provided, expected " + expectedArgLength + " arguments, " + args.length + 
                " provided."));
        }

        String outputDir = args[0];
        List<Integer> taxonIds = CommandRunner.parseListArgumentAsInt(args[1]);
        GenerateBioSODAFile generateBioSODA = new GenerateBioSODAFile();
        for (Integer taxonId: taxonIds) {
            generateBioSODA.generateFile(taxonId, outputDir);
        }
        log.traceExit();
    }

    // ************************************
    // INSTANCE ATTRIBUTES AND METHODS
    // ************************************
    private final ServiceFactory serviceFactory;

    public GenerateBioSODAFile() {
        this(new ServiceFactory());
    }
    //XXX: allows one thread per species by provided a Supplier<ServiceFactory>?
    public GenerateBioSODAFile(ServiceFactory serviceFactory) {
        if (serviceFactory == null) {
            throw log.throwing(new IllegalArgumentException("ServiceFactory cannot be null"));
        }
        this.serviceFactory = serviceFactory;
    }

    public void generateFile(int taxonId, String outputDirectory) throws IOException {
        log.entry(taxonId, outputDirectory);
        log.info("Generating file for taxon {}", taxonId);

        //Retrieving the taxon for file name generation
        Taxon taxon = this.serviceFactory.getTaxonService().loadTaxa(Collections.singleton(taxonId), false)
                .findFirst().orElseThrow(() -> new IllegalArgumentException(
                        "Could not find taxon ID: " + taxonId));
        //Retrieving the anat. entity similarities for defining column names,
        //ordered by alphabetical order of the anat. entity names
        List<AnatEntitySimilarity> anatSimilarities = this.serviceFactory.getAnatEntitySimilarityService()
                .loadPositiveAnatEntitySimilarities(taxonId, false).stream()
                  //XXX: Maybe we'll use getAllAnatEntities() someday rather than getSourceAnatEntities()
                .sorted(Comparator.comparing(s -> s.getSourceAnatEntityNames()))
                .collect(Collectors.toList());
        List<String> anatSimilarityNames = anatSimilarities.stream()
                .map(s -> s.getSourceAnatEntityNames())
                .collect(Collectors.toList());
        //Retrieving the calls
        Stream<SimilarityExpressionCall> callStream = this.serviceFactory.getMultiSpeciesCallService()
                .loadSimilarityExpressionCalls(taxonId, null, null, false);
        //We're going to group the calls per Gene, to be able to write all the columns
        //in the row for a gene
        Stream<List<SimilarityExpressionCall>> callsByGene = StreamSupport.stream(
                new ElementGroupFromListSpliterator<>(callStream, SimilarityExpressionCall::getGene,
                        Gene.COMPARATOR),
                false);

        log.info("Start retrieving the calls and writing to output file...");
        String tmpExtension = ".tmp";
        String fileExtension = ".tsv";
        String fileName = ("homologous_expr_calls_" + taxon.getScientificName())
                .replaceAll(" ", "_");
        File tmpFile = new File(outputDirectory, fileName + fileExtension + tmpExtension);
        // override any existing file
        if (tmpFile.exists()) {
            tmpFile.delete();
        }

        //We'll store various statistics about the calls
        Map<AnatEntitySimilarity, Set<Species>> anatSimToSpecies = anatSimilarities.stream()
                //Needs a compiler hint for some Java 8 compiler
                .map(s -> new AbstractMap.SimpleEntry<>(s, new HashSet<Species>()))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
        Map<AnatEntitySimilarity, Integer> anatSimToGeneCount = anatSimilarities.stream()
                .map(s -> new AbstractMap.SimpleEntry<>(s, 0))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
        Map<Gene, Integer> geneToSimCount = new HashMap<>();
        Map<Species, Integer> speToGeneCount = new HashMap<>();
        Map<Species, Set<AnatEntitySimilarity>> speToSims = new HashMap<>();
        //We use a quick and dirty ListWriter
        int dataRowCount = 0;
        try (ICsvListWriter listWriter = new CsvListWriter(new FileWriter(tmpFile), Utils.TSVCOMMENTED)) {
            CellProcessor[] processors = getProcessors(anatSimilarities.size());
            final String[] header = getHeader(anatSimilarityNames);
            // write the header
            listWriter.writeHeader(header);
 
            Iterator<List<SimilarityExpressionCall>> callIterator = callsByGene.iterator();
            while (callIterator.hasNext()) {
                List<SimilarityExpressionCall> calls = callIterator.next();
                Gene gene = calls.get(0).getGene();
                //XXX: for now we don't manage parameters in the MultiSpeciesCondition
                //other than the AnatEntitySimilarity, but if it changes in the future,
                //we'll have to change this Map and this logic
                Map<AnatEntitySimilarity, SimilarityExpressionCall> simToCall = calls.stream()
                        .collect(Collectors.toMap(c -> c.getMultiSpeciesCondition().getAnatSimilarity(),
                                c -> c));

                List<Object> toWrite = new ArrayList<>();
                toWrite.add(gene.getEnsemblGeneId() + " - " + gene.getSpecies().getName());
                boolean hasData = false;
                for (AnatEntitySimilarity sim: anatSimilarities) {
                    SimilarityExpressionCall callSim = simToCall.get(sim);
                    if (callSim == null) {
                        toWrite.add(NO_DATA);
                    } else {
                        toWrite.add(callSim.getSummaryCallType().toString());

                        hasData = true;
                        anatSimToSpecies.get(sim).add(gene.getSpecies());
                        anatSimToGeneCount.computeIfPresent(sim, (k, v) -> new Integer(v + 1));
                        geneToSimCount.compute(gene, (k, v) -> v == null? 1: new Integer(v + 1));
                        speToSims.compute(gene.getSpecies(),
                                (k, v) -> {
                                    if (v == null) {
                                        return new HashSet<>(Arrays.asList(sim));
                                    }
                                    v.add(sim);
                                    return v;
                                });
                    }
                }
                if (hasData) {
                    speToGeneCount.compute(gene.getSpecies(),
                            (k, v) -> v == null? 1: new Integer(v + 1));
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
            log.info("Done, file for the taxon {} contains {} rows without including header.",
                    taxonId, dataRowCount);
            File file = new File(outputDirectory, fileName + fileExtension);
            if (tmpFile.exists()) {
                tmpFile.renameTo(file);
            }

            //Generate a file with statistics
            writeStatsFile(outputDirectory, fileName, anatSimToSpecies, anatSimToGeneCount,
                    geneToSimCount, speToGeneCount, speToSims);
        } else {
            log.warn("Done, file for the taxon {} contains no rows.", taxonId);
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
        }

        log.traceExit();
    }

    protected static String[] getHeader(List<String> anatSimilarityNames) {
        log.entry(anatSimilarityNames);

        List<String> cols = new ArrayList<>(Arrays.asList("Ensembl gene ID"));
        cols.addAll(anatSimilarityNames);
        return log.traceExit(cols.toArray(new String[0]));
    }
    protected static CellProcessor[] getProcessors(int similarityCount) {
        log.entry(similarityCount);
        Set<Object> allowedExpressionCategories = Arrays.stream(SummaryCallType.ExpressionSummary.values())
                .map(c -> c.toString())
                .collect(Collectors.toSet());
        allowedExpressionCategories.add(NO_DATA);

        List<CellProcessor> processors = new ArrayList<>(Arrays.asList(new StrNotNullOrEmpty(new Unique())));
        for (int i = 0; i < similarityCount; i++) {
            processors.add(new IsIncludedIn(allowedExpressionCategories));
        }
        return log.traceExit(processors.toArray(new CellProcessor[0]));
    }
    private static void writeStatsFile(String outputDirectory, String fileName,
            Map<AnatEntitySimilarity, Set<Species>> anatSimToSpecies,
            Map<AnatEntitySimilarity, Integer> anatSimToGeneCount,
            Map<Gene, Integer> geneToSimCount,
            Map<Species, Integer> speToGeneCount,
            Map<Species, Set<AnatEntitySimilarity>> speToSims) throws IOException {
        log.entry(outputDirectory, fileName, anatSimToSpecies, anatSimToGeneCount, geneToSimCount,
                speToGeneCount, speToSims);

        File statsFile = new File(outputDirectory, "stats_" + fileName + ".txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(statsFile))) {
            writer.write("Species with data per anat. entity similarities");
            writer.newLine();
            anatSimToSpecies.entrySet().stream()
            .sorted(Comparator.comparing(e -> e.getValue().size(), Comparator.reverseOrder()))
            .forEach(e -> {
                try {
                    //XXX: Maybe we'll use getAllAnatEntities() someday rather than getSourceAnatEntities()
                    writer.write(e.getKey().getSourceAnatEntityNames() + ": "
                    + e.getValue().stream().map(s -> s.getScientificName())
                    .sorted().collect(Collectors.joining(" - ")));
                    writer.newLine();
                } catch (Exception e1) {
                    log.catching(e1);
                    throw log.throwing(new RuntimeException(e1));
                }
            });

            writer.newLine();
            writer.write("Count of genes with data per anat. entity similarities");
            writer.newLine();
            anatSimToGeneCount.entrySet().stream()
            .sorted(Comparator.comparing(e -> e.getValue(), Comparator.reverseOrder()))
            .forEach(e -> {
                try {
                    //XXX: Maybe we'll use getAllAnatEntities() someday rather than getSourceAnatEntities()
                    writer.write(e.getKey().getSourceAnatEntityNames() + ": " + e.getValue());
                    writer.newLine();
                } catch (Exception e1) {
                    log.catching(e1);
                    throw log.throwing(new RuntimeException(e1));
                }
            });

            writer.newLine();
            writer.write("Mean number of anat. entity similarities with data per gene in each species");
            writer.newLine();
            Map<String, Double> speToMean = geneToSimCount.entrySet().stream()
                    .collect(Collectors.groupingBy(e -> e.getKey().getSpecies().getScientificName(),
                    Collectors.averagingInt(e -> e.getValue())));
            speToMean.entrySet().stream().sorted(Comparator.comparing(e -> e.getValue(), Comparator.reverseOrder()))
            .forEach(e -> {
                try {
                    writer.write(e.getKey() + ": " + String.valueOf(e.getValue()));
                    writer.newLine();
                } catch (Exception e1) {
                    log.catching(e1);
                    throw log.throwing(new RuntimeException(e1));
                }
            });

            writer.newLine();
            writer.write("Count of genes with data per species");
            writer.newLine();
            speToGeneCount.entrySet().stream()
            .sorted(Comparator.comparing(e -> e.getValue(), Comparator.reverseOrder()))
            .forEach(e -> {
                try {
                    writer.write(e.getKey().getScientificName() + ": " + e.getValue());
                    writer.newLine();
                } catch (Exception e1) {
                    log.catching(e1);
                    throw log.throwing(new RuntimeException(e1));
                }
            });

            writer.newLine();
            writer.write("Count of anat. entity similarities with data per species");
            writer.newLine();
            speToSims.entrySet().stream()
            .sorted(Comparator.comparing(e -> e.getValue().size(), Comparator.reverseOrder()))
            .forEach(e -> {
                try {
                    writer.write(e.getKey().getScientificName() + ": " + e.getValue().size());
                    writer.newLine();
                } catch (Exception e1) {
                    log.catching(e1);
                    throw log.throwing(new RuntimeException(e1));
                }
            });
        }
        log.traceExit();
    }
}