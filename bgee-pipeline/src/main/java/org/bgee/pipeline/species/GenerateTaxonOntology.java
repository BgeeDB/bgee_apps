package org.bgee.pipeline.species;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.model.OWLOntology;

import owltools.ncbi.NCBI2OWL;

/**
 * Use the NCBI taxonomy data files to generate a taxonomy ontology, and stored it 
 * in OBO format. This taxonomy will include only taxa related to a specified 
 * taxon.
 * <p>
 * This class uses the {@code owltools.ncbi.NCBI2OWL} class written by James A. Overton 
 * to generate the ontology. It is needed to provide the {@code taxonomy.dat} file 
 * that can be found at {@code ftp://ftp.ebi.ac.uk/pub/databases/taxonomy/}. 
 * This approach is based on the 
 * <a href='http://sourceforge.net/p/obo/svn/HEAD/tree/ncbitaxon/trunk/src/ontology/Makefile'>
 * OBOFoundry Makefile</a>, used to generate the <a 
 * href='http://www.obofoundry.org/cgi-bin/detail.cgi?id=ncbi_taxonomy'>OBOFoundry 
 * ontology</a>. We need to generate the ontology ourselves because, as of Bgee 13, 
 * the official ontology does not include the last modifications that we requested 
 * to NCBI, and that were accepted (e.g., addition of a <i>Dipnotetrapodomorpha</i> term).
 * <p>
 * To avoid to regenerate this ontology at each Bgee release, this custom 
 * ontology will not contain only taxa related to the species currently included 
 * in Bgee; and to avoid storing a too large ontology, it will contain only taxa 
 * related to a specified taxon (for instance, <i>metazoa</i>, NCBI ID <a 
 * href='http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?mode=Info&id=33208'>
 * 33208</a>).
 * <p>
 * The generated ontology will be stored in OBO format, to keep the file as small 
 * as possible (RDF OWL is verbose).
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class GenerateTaxonOntology {
    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = 
            LogManager.getLogger(GenerateTaxonOntology.class.getName());
    
    /**
     * Main method to trigger the generation of a taxonomy ontology, stored in 
     * OBO format, based on the NCBI taxonomy data. Parameters that must be provided 
     * in order in {@code args} are: 
     * <ol>
     * <li>path to the {@code taxonomy.dat} file.
     * <li>NCBI ID of the taxon for which we want to keep related taxa in the 
     * generated ontology (meaning, only descendants and ancestors of this taxon 
     * will be kept in the ontology). ID provided with the {@code NCBITaxon:} 
     * prefix (for instance, {@code NCBITaxon:33208} for <i>metazoa</i>).
     * <li>path to the file to store the generated ontology in OBO format. So 
     * it must finish with {@code .obo} ;)
     * </ol>
     * 
     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
     * @throws IllegalArgumentException If {@code args} does not contain the proper 
     *                                  parameters.
     */
    public static void main(String[] args) {
        log.entry((Object[]) args);
        
        int expectedArgLength = 3;
        if (args.length != expectedArgLength) {
            throw log.throwing(new IllegalArgumentException("Incorrect number of arguments " +
            		"provided, expected " + expectedArgLength + " arguments, " + args.length + 
            		" provided."));
        }
        if (!args[0].endsWith(".dat")) {
            throw log.throwing(new IllegalArgumentException("No .dat file provided "));
        }
        if (!args[1].startsWith("NCBITaxon:")) {
            throw log.throwing(new IllegalArgumentException("No NCBI Taxon ID provided " +
            		"to restrain the scope of the ontology generated."));
        }
        if (!args[2].endsWith(".obo")) {
            throw log.throwing(new IllegalArgumentException("The output file must be " +
            		"an OBO format."));
        }
        
        GenerateTaxonOntology generate = new GenerateTaxonOntology();
        generate.ncbi2owl(args[0]);
        
        log.exit();
    }
    
    /**
     * Generate the NCBI taxonomy ontology using files obtained from the NCBI FTP 
     * ({@code taxonomy.dat}). This method uses the class {@code owltools.ncbi.NCBI2OWL} 
     * written by James A. Overton. 
     * 
     * @param pathToTaxonomyData    A {@code String} representing the path to the 
     *                              {@code taxonomy.dat} file, used by {@code 
     *                              owltools.ncbi.NCBI2OWL}
     */
    private OWLOntology ncbi2owl(String pathToTaxonomyData) {
        
    }
}
