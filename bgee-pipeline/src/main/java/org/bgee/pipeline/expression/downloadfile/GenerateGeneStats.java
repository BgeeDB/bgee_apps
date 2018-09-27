package org.bgee.pipeline.expression.downloadfile;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.ServiceFactory;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.species.Species;

/**
 * Class used to generate statistics about expression data of genes and gene biotypes.
 *
 * @author Frederic Bastian
 * @version Bgee 14 Sep. 2018
 * @since Bgee 14 Sep. 2018
 */
public class GenerateGeneStats {
    private final static Logger log = LogManager.getLogger(GenerateGeneStats.class.getName());

    /**
     * A {@code Supplier} of {@code ServiceFactory}s to be able to provide one to each thread.
     */
    private final Supplier<ServiceFactory> serviceFactorySupplier;

    /**
     * Default constructor. 
     */
    public GenerateGeneStats() {
        this(ServiceFactory::new);
    }
    /**
     * Constructor providing the {@code ServiceFactory} that will be used by 
     * this object to perform queries to the database. This is useful for unit testing.
     *
     * @param serviceFactorySupplier        A {@code Supplier} of {@code ServiceFactory}s 
     *                                      to be able to provide one to each thread.
     */
    public GenerateGeneStats(Supplier<ServiceFactory> serviceFactorySupplier) {
        this.serviceFactorySupplier = serviceFactorySupplier;
    }

    /**
     * Main method to generate stats files. Parameters that must be provided in order in {@code args} are:
     * <ol>
     * <li>path where to store the files
     * <li>suffix of the file names to store stats per biotype. Prefix will be the species name.
     * <li>suffix of the file names to store stats per gene. Prefix will be the species name.
     * </ol>
     *
     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
     * @throws IllegalArgumentException     If the arguments are incorrect.
     */
    public static void main(String[] args) throws IllegalArgumentException {
        if (args.length != 3) {
            throw log.throwing(new IllegalArgumentException("Incorrect number of arguments."));
        }

        GenerateGeneStats statsGenerator = new GenerateGeneStats();
        statsGenerator.generate(args[0], args[1], args[2]);
        
        log.exit();
    }

    /**
     * Generate statistics about expression data, with one file for stats about gene biotypes per species,
     * and one file for stats about genes per species.
     *
     * @param path                      A {@code String} that is the path where to store the files.
     * @param bioTypeStatsFileSuffix    A {@code String} that is the suffix of the file names to store stats
     *                                  per biotype. Prefix will be the species name.
     * @param geneStatsFileSuffix       A {@code String} that is the suffix of the file names to store stats
     *                                  per gene. Prefix will be the species name.
     */
    public void generate(String path, String bioTypeStatsFileSuffix, String geneStatsFileSuffix) {
        log.entry(path, bioTypeStatsFileSuffix, geneStatsFileSuffix);

        ServiceFactory serviceFactory = this.serviceFactorySupplier.get();
        Set<Species> allSpecies = serviceFactory.getSpeciesService().loadSpeciesByIds(null, false)
                .stream().collect(Collectors.toSet());
        //launch the computation for each species independently
        for (Species species: allSpecies) {
            //Retrieve all genes for that species
            Set<Gene> genes = serviceFactory.getGeneService().loadGenes(new GeneFilter(species.getId()))
                    .collect(Collectors.toSet());

            this.generatePerSpecies(species, genes);
        }
        
        log.exit();
    }

    private void generatePerSpecies(Species species, Set<Gene> genes) {
        log.entry(species, genes);

        //Now we use parallel streams for each gene independently
        genes.parallelStream().map(gene -> {
            //We need one ServiceFactory per thread
            ServiceFactory serviceFactory = this.serviceFactorySupplier.get();
            CallService callService = serviceFactory.getCallService();

            //For each gene, we retrieve: "present" expression calls (min qual bronze) per anat. entity, and per condition,
            //and "absent" expression calls (min qual bronze) per anat. entity, and per condition.
            //Plus also the number of anat. entities with expression filtered as on the gene page
            int presentBronzeAnatEntity = 0;
            int presentSilverAnatEntity = 0;
            int presentGoldAnatEntity = 0;
            int absentBronzeAnatEntity = 0;
            int absentSilverAnatEntity = 0;
            int absentGoldAnatEntity = 0;
            int presentBronzeCond = 0;
            int presentSilverCond = 0;
            int presentGoldCond = 0;
            int absentBronzeCond = 0;
            int absentSilverCond = 0;
            int absentGoldCond = 0;
            int filteredGenePagePresentAnatEntity = 0;
        });
    }
}
