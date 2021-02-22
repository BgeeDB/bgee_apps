package org.bgee.pipeline.expression;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.multispemapping.AnatEntitySimilarity;
import org.bgee.model.anatdev.multispemapping.AnatEntitySimilarityAnalysis;
import org.bgee.model.anatdev.multispemapping.AnatEntitySimilarityService;
import org.bgee.pipeline.CommandRunner;
import org.bgee.pipeline.Utils;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.constraint.Unique;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListWriter;

/**
 * Generate data necessary for GenoFish-related project with Tina.
 *
 * @author Frederic Bastian
 * @version Bgee 14 May 2019
 * @since Bgee 14 May 2019
 */
public class GenoFishProject {
    private final static Logger log = LogManager.getLogger(GenoFishProject.class.getName());

    private static final Function<AnatEntitySimilarity, String> EXTRACT_IDS =
            aes -> aes.getSourceAnatEntitiesSortedById().stream()
            .map(ae -> ae.getId()).collect(Collectors.joining(" - "));
    private static final Function<AnatEntitySimilarity, String> EXTRACT_NAMES =
            aes -> aes.getSourceAnatEntitiesSortedById().stream()
            .map(ae -> ae.getName()).collect(Collectors.joining(" - "));
    private static final BiFunction<AnatEntitySimilarity, AnatEntitySimilarityAnalysis, String> EXTRACT_SPECIES_NAMES =
            (aes, analysis) -> aes.getAllAnatEntities().stream()
            .flatMap(ae -> analysis.getAnatEntitiesExistInSpecies().get(ae).stream())
            .map(spe -> spe.getName())
            .distinct()
            .sorted()
            .collect(Collectors.joining(", "));

    public static void main(String[] args) throws IOException {
        log.entry((Object[]) args);
        if (args.length < 1) {
            throw log.throwing(new IllegalArgumentException("Arguments must be provided"));
        }

        String action = args[0];
        GenoFishProject generate = new GenoFishProject();

        if ("homology".equals(action)) {
            int expectedArgLength = 4;
            if (args.length != expectedArgLength) {
                throw log.throwing(new IllegalArgumentException("Incorrect number of arguments " +
                    "provided, expected " + expectedArgLength + " arguments, " + args.length +
                    " provided."));
            }
            Collection<Integer> speciesIds = CommandRunner.parseListArgumentAsInt(args[1]);
            Collection<String> anatEntityIds = CommandRunner.parseListArgument(args[2]);
            String outputDir = args[3];
            generate.writeHomologousAnatEntities(speciesIds, anatEntityIds, outputDir);
        }

        log.traceExit();
    }

    // ************************************
    // INSTANCE ATTRIBUTES AND METHODS
    // ************************************

    private final ServiceFactory serviceFactory;

    public GenoFishProject() {
        this(new ServiceFactory());
    }
    public GenoFishProject(ServiceFactory serviceFactory) {
        if (serviceFactory == null) {
            throw log.throwing(new IllegalArgumentException("ServiceFactory cannot be null"));
        }
        this.serviceFactory = serviceFactory;
    }

    public void writeHomologousAnatEntities(Collection<Integer> speciesIds, Collection<String> anatEntityIds,
            String outputDirectory) throws IOException {
        log.entry(speciesIds, anatEntityIds, outputDirectory);

        AnatEntitySimilarityService service = new AnatEntitySimilarityService(this.serviceFactory);
        AnatEntitySimilarityAnalysis similarityAnalysis = service.loadPositiveAnatEntitySimilarityAnalysis(
                speciesIds, anatEntityIds, false);

        writeHomologousStructures(similarityAnalysis, outputDirectory);
        writeNonHomologousStructures(similarityAnalysis, outputDirectory);

        log.traceExit();
    }

    private static void writeHomologousStructures(AnatEntitySimilarityAnalysis similarityAnalysis,
            String outputDirectory) throws IOException {
        log.entry(similarityAnalysis, outputDirectory);

        String tmpExtension = ".tmp";
        String fileExtension = ".tsv";
        String fileName = ("homologous_structures_lca_"
                + similarityAnalysis.getLeastCommonAncestor().getScientificName())
                + "_" + similarityAnalysis.getRequestedAnatEntityIds().size() + "_anat_entities_"
                + similarityAnalysis.getRequestedSpeciesIds().size() + "_species"
                .replaceAll(" ", "_");
        File tmpFile = new File(outputDirectory, fileName + fileExtension + tmpExtension);
        // override any existing file
        if (tmpFile.exists()) {
            tmpFile.delete();
        }

        try (ICsvListWriter listWriter = new CsvListWriter(new FileWriter(tmpFile), Utils.TSVCOMMENTED)) {
            CellProcessor[] processors = new CellProcessor[]{new StrNotNullOrEmpty(new Unique()),
                    new StrNotNullOrEmpty(new Unique()), new StrNotNullOrEmpty()};
            String[] header = new String[] {"Anat. entity IDs", "Anat. entity names", "Exists in selected species"};
            // write the header
            listWriter.writeHeader(header);

            for (AnatEntitySimilarity aes: similarityAnalysis.getAnatEntitySimilarities()) {
                List<Object> toWrite = new ArrayList<>();
                toWrite.add(EXTRACT_IDS.apply(aes));
                toWrite.add(EXTRACT_NAMES.apply(aes));
                toWrite.add(EXTRACT_SPECIES_NAMES.apply(aes, similarityAnalysis));
                listWriter.write(toWrite, processors);
            }
        } catch (Exception e) {
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
            throw e;
        }
        File file = new File(outputDirectory, fileName + fileExtension);
        if (tmpFile.exists()) {
            tmpFile.renameTo(file);
        }
    }
    private static void writeNonHomologousStructures(AnatEntitySimilarityAnalysis similarityAnalysis,
            String outputDirectory) throws IOException {
        log.entry(similarityAnalysis, outputDirectory);

        String tmpExtension = ".tmp";
        String fileExtension = ".tsv";
        String fileName = ("non_matching_structures_lca_"
                + similarityAnalysis.getLeastCommonAncestor().getScientificName())
                + "_" + similarityAnalysis.getRequestedAnatEntityIds().size() + "_anat_entities_"
                + similarityAnalysis.getRequestedSpeciesIds().size() + "_species"
                .replaceAll(" ", "_");
        File tmpFile = new File(outputDirectory, fileName + fileExtension + tmpExtension);
        // override any existing file
        if (tmpFile.exists()) {
            tmpFile.delete();
        }

        try (ICsvListWriter listWriter = new CsvListWriter(new FileWriter(tmpFile), Utils.TSVCOMMENTED)) {
            CellProcessor[] processors = new CellProcessor[]{new StrNotNullOrEmpty(new Unique()),
                    new StrNotNullOrEmpty(new Unique())};
            String[] header = new String[] {"Anat. entity ID", "Anat. entity name"};
            // write the header
            listWriter.writeHeader(header);

            for (AnatEntity ae: similarityAnalysis.getAnatEntitiesWithNoSimilarities()) {
                List<Object> toWrite = new ArrayList<>();
                toWrite.add(ae.getId());
                toWrite.add(ae.getName());
                listWriter.write(toWrite, processors);
            }
        } catch (Exception e) {
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
            throw e;
        }
        File file = new File(outputDirectory, fileName + fileExtension);
        if (tmpFile.exists()) {
            tmpFile.renameTo(file);
        }
    }
}