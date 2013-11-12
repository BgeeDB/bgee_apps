package org.bgee.pipeline.uberon;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.OntologyUtils;
import org.bgee.pipeline.Utils;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import owltools.graph.OWLGraphWrapper;
import owltools.mooncat.SpeciesSubsetterUtil;

/**
 * Generates a TSV files allowing to know, for each {@code OWLClass} in the Uberon 
 * {@code OWLOntology}, in which taxa it exits, among the taxa provided through 
 * another TSV file, containing their NCBI ID.
 * <p>
 * This class is based on the {@code owltools.mooncat.SpeciesSubsetterUtil} from 
 * owltools. This tool allows to produce a version of a source ontology, containing 
 * only the {@code OWLClass}es existing in a given taxon, for one taxon at a time. 
 * So our approach will be to generate a version of the Uberon ontology for each 
 * of the taxa provided through the TSV file, and to merge the generated information 
 * into a TSV files, where lines are {@code OWLClass}es and columns are taxa.
 * <p>
 * It is possible to request to store the intermediate ontologies generated 
 * for each taxon by the {@code SpeciesSubsetterUtil}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class GenerateTaxonConstraints {
    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = 
            LogManager.getLogger(GenerateTaxonConstraints.class.getName());
    
    /**
     * The {@code OWLReasonerFactory} used to obtain {@code OWLReasoner}s, 
     * used to produce the taxon-specific ontologies.
     */
    private OWLReasonerFactory reasonerFactory;
    
    /**
     * Main method to trigger the generation of a TSV files, allowing to know, 
     * for each {@code OWLClass} in the Uberon {@code OWLOntology}, in which taxa 
     * it exits, among the taxa provided through another TSV file, containing 
     * their NCBI ID. Parameters that must be provided in order in {@code args} are: 
     * <ol>
     * <li>path to the source Uberon OWL ontology file.
     * <li>path to the TSV files containing the ID of the species used in Bgee, 
     * corresponding to the NCBI taxonomy ID (e.g., 9606 for human). The first line 
     * should be a header line, and first column should be the IDs. A second column 
     * can be present for human readability. 
     * <li>path to the generated TSV files, output of the method.
     * <li>OPTIONNAL: a path to a directory where to store the intermediate generated 
     * ontologies. If this parameter is provided, an ontology will be generated 
     * for each taxon, and stored in this directory, containing only 
     * the {@code OWLClass}es existing in that taxon.
     * </ol>
     * 
     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
     */
    public static void main(String[] args) {
        
    }
    
    public void generateTaxonConstraints(String uberonFile, String speciesFile, 
            String outputFile, String storeOntologyDir) throws IllegalArgumentException, 
            FileNotFoundException, IOException, UnknownOWLOntologyException, 
            OWLOntologyCreationException, OBOFormatParserException {
        log.entry(uberonFile, speciesFile, outputFile, storeOntologyDir);
        
        Set<Integer> speciesIds = Utils.getSpeciesIds(speciesFile);
        Map<OWLClass, Set<Integer>> constraints = 
                this.generateTaxonConstraints(uberonFile, speciesIds, storeOntologyDir);
        
        
        
        log.exit();
    }
    
    /**
     * Returns a {@code Map} representing the taxon constraints generated from 
     * the ontology stored in {@code uberonFile}, for the taxa provided through 
     * {@code taxonIds}. The returned {@code Map} contains all {@code OWLClass}es 
     * present in the ontology, as keys, associated to a {@code Set} of {@code Integer}s, 
     * that are the IDs of the taxa in which it exists, among the provided taxa. 
     * If the {@code Set} is empty, then it means that the {@code OWLClass} 
     * exists in none of the provided taxa. 
     * 
     * @param uberonFile        A {@code String} that is the path to the Uberon 
     *                          ontology file.
     * @param taxonIds          A {@code Set} of {@code Integer}s that are the IDs 
     *                          from the NCBI website of the taxa to consider 
     *                          (for instance, 9606 for human).
     * @param storeOntologyDir  A {@code String} that is the path to a directory 
     *                          where to store intermediate ontologies. If {@code null} 
     *                          the generated ontologies will not be stored.
     * @return                  A {@code Map} where keys are {@code OWLClass}es 
     *                          from the provided ontology, and values are  
     *                          {@code Set}s of {@code Integer}s containing the IDs 
     *                          of taxa in which the {@code OWLClass} exists.
     * @throws UnknownOWLOntologyException  If the ontology stored in 
     *                                      {@code uberonFile} could not be used.
     * @throws OWLOntologyCreationException If the ontology stored in {@code uberonFile} 
     *                                      could not be used.
     * @throws OBOFormatParserException     If {@code uberonFile} could not be parsed. 
     * @throws IOException                  If {@code uberonFile} could not be opened. 
     */
    public Map<OWLClass, Set<Integer>> generateTaxonConstraints(String uberonFile, 
            Set<Integer> taxonIds, String storeOntologyDir) 
                    throws UnknownOWLOntologyException, OWLOntologyCreationException, 
                    OBOFormatParserException, IOException {
        log.entry(uberonFile, taxonIds, storeOntologyDir);

        OWLGraphWrapper uberonWrapper = 
                new OWLGraphWrapper(OntologyUtils.loadOntology(uberonFile));
        Map<OWLClass, Set<Integer>> taxonConstraints = new HashMap<OWLClass, Set<Integer>>();
        for (OWLClass currentClass: uberonWrapper.getAllOWLClasses()) {
            taxonConstraints.put(currentClass, new HashSet<Integer>());
        }
        
        for (int taxonId: taxonIds) {
            Set<OWLClass> classesDefined = this.getExistingOWLClass(
                    uberonFile, taxonId, storeOntologyDir);
            for (OWLClass classDefined: classesDefined) {
                taxonConstraints.get(classDefined).add(taxonId);
            }
        }
        
        return log.exit(taxonConstraints);
    }
    
    /**
     * Returns a {@code Set} of {@code OWLClass}es obtained from the ontology 
     * stored in {@code uberonFile}, and that actually exists in the taxon with ID 
     * {@code taxonId}. If {@code storeOntologyDir} is not {@code null}, then 
     * the intermediate ontology, corresponding to the filtered version of the source 
     * ontology for the provided taxon, will be saved in that directory.
     * 
     * @param uberonFile        A {@code String} that is the path to the Uberon 
     *                          ontology file.
     * @param taxonId           An {@code int} that is the ID on the NCBI website 
     *                          of the taxon to consider (for instance, 9606 for human).
     * @param storeOntologyDir  A {@code String} that is the path to a directory 
     *                          where to store intermediate ontologies. If {@code null} 
     *                          the generated ontologies will not be stored.
     * @return                  A {@code Set} containing {@code OWLClass}es 
     *                          from the provided ontology, that exists in 
     *                          the provided taxon.
     * @throws UnknownOWLOntologyException  If the ontology stored in 
     *                                      {@code uberonFile} could not be used.
     * @throws OWLOntologyCreationException If the ontology stored in {@code uberonFile} 
     *                                      could not be used.
     * @throws OBOFormatParserException     If {@code uberonFile} could not be parsed. 
     * @throws IOException                  If {@code uberonFile} could not be opened. 
     */
    private Set<OWLClass> getExistingOWLClass(String uberonFile, int taxonId, 
            String storeOntologyDir) throws UnknownOWLOntologyException, 
            OWLOntologyCreationException, OBOFormatParserException, IOException  {
        log.entry(uberonFile, taxonId, storeOntologyDir);
        
        //as there is no easy way to clone an ontology before modifying it, 
        //we need to reload it each time this method is called
        OWLGraphWrapper uberonWrapper = 
                new OWLGraphWrapper(OntologyUtils.loadOntology(uberonFile));
        
        //Get the OWLClass corresponding to the requested taxon
        String ontTaxonId = OntologyUtils.getTaxOntologyId(taxonId);
        OWLClass taxClass = uberonWrapper.getOWLClassByIdentifier(ontTaxonId);
        if (taxClass == null) {
            throw log.throwing(new IllegalArgumentException("A taxon ID " +
                    "provided could not be found in the provided ontology: " + 
                    taxonId));
        }
        
        //filter ontology
        SpeciesSubsetterUtil subSetter = new SpeciesSubsetterUtil(uberonWrapper);
        subSetter.taxClass = taxClass;
        subSetter.reasoner = this.createReasoner(uberonWrapper.getSourceOntology());
        subSetter.removeOtherSpecies();
        
        //if we want to store the intermediate ontology
        if (storeOntologyDir != null) {
            uberonWrapper.clearCachedEdges();
            String outputFilePath = new File("uberon_subset" + taxonId + ".obo", 
                    storeOntologyDir).getPath();
            new OntologyUtils(uberonWrapper).saveAsOBO(outputFilePath);
        }
        
        return log.exit(uberonWrapper.getAllOWLClasses());
    }
    

    /**
     * Creates and returns an {@code OWLReasoner} to reason on the provided 
     * {@code OWLOntology}. If no {@code OWLReasonerFactory} was previously 
     * provided (see {@link #setReasonerFactory(OWLReasonerFactory)}), 
     * then the {@code ElkReasonerFactory} will be used (thus this method 
     * would return {@code ElkReasoner}s).
     * 
     * @param ont  The {@code OWLOntology} which the returned {@code OWLReasoner} 
     *              should reason on.
     * @return      An <code>OWLReasoner</code> set to reason on {@code ont}.
     */
    public OWLReasoner createReasoner(OWLOntology ont) {
        log.entry(ont);
        if (this.reasonerFactory == null) {
            this.setReasonerFactory(new ElkReasonerFactory());
        }
        return log.exit(this.reasonerFactory.createReasoner(ont));
    }
    /**
     * Sets the {@code OWLReasonerFactory}, used to obtain {@code OWLReasoner}s, 
     * used to produce the taxon-specific ontologies. Otherwise, by default, 
     * the {@code ElkReasonerFactory} will be used (see 
     * {@link #createReasoner()}).
     * 
     * @param factory   the {@code OWLReasonerFactory} to use.
     */
    public void setReasonerFactory(OWLReasonerFactory factory) {
        log.entry(factory);
        this.reasonerFactory = factory;
        log.exit();
    }
}
