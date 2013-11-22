package org.bgee.pipeline.uberon;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.OntologyUtils;
import org.bgee.pipeline.Utils;
import org.bgee.pipeline.species.GenerateTaxonOntology;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.supercsv.cellprocessor.FmtBool;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import owltools.graph.OWLGraphWrapper;
import owltools.mooncat.SpeciesSubsetterUtil;

/**
 * Generates a TSV files allowing to know, for each {@code OWLClass} in the Uberon 
 * {@code OWLOntology}, in which taxa it exits, among the taxa provided through 
 * another TSV file, containing their NCBI IDs.
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
 * <p>
 * For the {@code SpeciesSubsetterUtil} to work, it is needed to: 
 * <ol>
 * <li>use a version of Uberon containing taxon constraints ("in_taxon" and "only_in_taxon" 
 * relations).
 * <li>remove from this Uberon version any "is_a" relations and disjoint classes axioms 
 * between classes corresponding to taxa (they could mess up the next step).
 * <li>merge this Uberon ontology with a taxonomy ontology containing disjoint classes 
 * axioms between sibling taxa, as explained in a Chris Mungall 
 * <a href='http://douroucouli.wordpress.com/2012/04/24/taxon-constraints-in-owl/'>
 * blog post</a>, see also {@link org.bgee.pipeline.species.GenerateTaxonOntology}. 
 * We need to do it ourselves because the taxonomy ontology provided online are outdated.
 * </ol>
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
     * for each {@code OWLClass} in the Uberon ontology, in which taxa it exits, 
     * among the taxa provided through another TSV file, containing their NCBI IDs. 
     * Parameters that must be provided in order in {@code args} are: 
     * <ol>
     * <li>path to the source Uberon OWL ontology file. This Uberon ontology must 
     * contain the taxon constraints ("in taxon" and "only_in_taxon" relations, 
     * not all Uberon versions contain them).
     * <li>path to the NCBI taxonomy ontology. This taxonomy must contain disjoint 
     * classes axioms between sibling taxa, as explained in a Chris Mungall 
     * <a href='http://douroucouli.wordpress.com/2012/04/24/taxon-constraints-in-owl/'>
     * blog post</a>, see also {@link org.bgee.pipeline.species.GenerateTaxonOntology}.
     * <li>path to the TSV files containing the IDs of the taxa for which we want 
     * to generate the taxon constraints, corresponding to the NCBI ID (e.g., 9606 
     * for human). The first line should be a header line, defining a column to get 
     * IDs from, named exactly "taxon ID" (other columns are optional and will be ignored).
     * <li>path to the generated TSV file, output of the method.
     * <li>OPTIONNAL: a path to a directory where to store the intermediate generated 
     * ontologies. If this parameter is provided, an ontology will be generated 
     * for each taxon, and stored in this directory, containing only 
     * the {@code OWLClass}es existing in this taxon. If not provided, the intermediate 
     * ontologies will not be stored. 
     * </ol>
     * 
     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
     * @throws IllegalArgumentException     If some taxa in the taxon file could not
     *                                      be found in the ontology.
     * @throws FileNotFoundException        If some files could not be found.
     * @throws IOException                  If some files could not be read/written.
     * @throws UnknownOWLOntologyException  If the ontology provided could not be used.
     * @throws OWLOntologyCreationException If the ontology provided could not be used.
     * @throws OBOFormatParserException     If the ontology provided could not be parsed. 
     */
    public static void main(String[] args) throws UnknownOWLOntologyException, 
        IllegalArgumentException, FileNotFoundException, OWLOntologyCreationException, 
        OBOFormatParserException, IOException {
        log.entry((Object[]) args);
        
        if (args.length < 4 || args.length > 5) {
            throw log.throwing(new IllegalArgumentException("Incorrect number of arguments " +
                    "provided, expected 4 to 5 arguments, " + args.length + 
                    " provided."));
        }
        
        String storeDir = null;
        if (args.length == 5) {
            storeDir = args[4];
        }
        GenerateTaxonConstraints generate = new GenerateTaxonConstraints();
        generate.generateTaxonConstraints(args[0], args[1], args[2], args[3], storeDir);
        
        log.exit();
    }
    
    /**
     * Generates taxon constraints. Launches the generation of a TSV files, 
     * allowing to know, for each {@code OWLClass} in the Uberon ontology stored in 
     * {@code uberonFile} (taxonomy classes excepted), in which taxa it exits, 
     * among the taxa provided through the TSV file {@code taxonIdFile}. The first 
     * line of this file should be a header line, defining a column to get IDs from, 
     * named exactly "taxon ID" (other columns are optional and will be ignored). 
     * These IDs must correspond to the NCBI IDs (for instance, 9606 for human). 
     * <p>
     * This method also needs to be provided with a taxonomy ontology, that must contain 
     * disjoint classes axioms between sibling taxa, as explained in a Chris Mungall 
     * <a href='http://douroucouli.wordpress.com/2012/04/24/taxon-constraints-in-owl/'>
     * blog post</a>, see also {@link org.bgee.pipeline.species.GenerateTaxonOntology}. 
     * <p>
     * The results will be stored in the TSV file {@code outputFile}. 
     * <p>
     * The approach is, for each taxon provided, to generate a custom version 
     * of the ontology, that will contain only the {@code OWLClass}es existing 
     * in this taxon. If you want to keep these intermediate generated ontologies, 
     * you need to provide the path {@code storeOntologyDir} where to store them. 
     * The ontology files will be named <code>uberon_subset_TAXONID.obo</code>. 
     * If {@code storeOntologyDir} is {@code null}, the intermediate ontology files 
     * will not be saved. 
     * 
     * @param uberonFile        A {@code String} that is the path to the Uberon 
     *                          ontology file.
     * @param taxOntFile        A {@code String} that is the path to the NCBI 
     *                          taxonomy ontology file.
     * @param taxonIdFile         A {@code String} that is the path to the TSV file 
     *                          containing the IDs from the NCBI website of the taxa 
     *                          to consider (for instance, 9606 for human). The first line 
     *                          should be a header line, defining a column to get IDs from, 
     *                          named exactly "taxon ID" (other columns are optional and 
     *                          will be ignored).
     * @param outputFile        A {@code String} that is the path to the generated 
     *                          TSV file, output of the method. It will have one header line. 
     *                          The columns will be: ID of the Uberon classes, IDs of each 
     *                          of the taxa that were examined. For each of the taxon column, 
     *                          a boolean is provided as "T" or "F", to define whether 
     *                          the associated Uberon class exists in it.
     * @param storeOntologyDir  A {@code String} that is the path to a directory 
     *                          where to store intermediate ontologies. If {@code null} 
     *                          the generated ontologies will not be stored.
     * @throws IllegalArgumentException     If some taxa in {@code taxonIdFile} could not
     *                                      be found in the ontology.
     * @throws FileNotFoundException        If some files could not be found.
     * @throws IOException                  If some files could not be read/written.
     * @throws UnknownOWLOntologyException  If the ontology stored in 
     *                                      {@code uberonFile} could not be used.
     * @throws OWLOntologyCreationException If the ontology stored in {@code uberonFile} 
     *                                      could not be used.
     * @throws OBOFormatParserException     If {@code uberonFile} could not be parsed. 
     */
    public void generateTaxonConstraints(String uberonFile, String taxOntFile, 
            String taxonIdFile, String outputFile, String storeOntologyDir) 
            throws IllegalArgumentException, FileNotFoundException, IOException, 
            UnknownOWLOntologyException, OWLOntologyCreationException, 
            OBOFormatParserException {
        
        log.entry(uberonFile, taxOntFile, taxonIdFile, outputFile, storeOntologyDir);
        
        Set<Integer> taxonIds = new Utils().getTaxonIds(taxonIdFile);
        OWLOntology uberontOnt = OntologyUtils.loadOntology(uberonFile);
        Map<String, Set<Integer>> constraints = 
                this.generateTaxonConstraints(uberontOnt, 
                        OntologyUtils.loadOntology(taxOntFile), 
                        taxonIds, storeOntologyDir);
        //TODO: we should also provide the OWLGrahWrappers to the method 
        //generateTaxonConstraints, to avoid loading them twice.
        this.writeToFile(constraints, taxonIds, outputFile, 
                new OWLGraphWrapper(uberontOnt));
        
        log.exit();
    }
    
    /**
     * Returns a {@code Map} representing the taxon constraints generated using  
     * the Uberon ontology {@code uberonOnt}, and the taxonomy ontology {@code taxOnt}, 
     * for the taxa provided through {@code taxonIds}. The returned {@code Map} 
     * contains the OBO-like IDs of all {@code OWLClass}es present in Uberon (taxonomy 
     * classes excepted), as keys, associated to a {@code Set} of {@code Integer}s 
     * that are the IDs of the taxa in which it exists, among the provided taxa. 
     * If the {@code Set} is empty, then it means that the {@code OWLClass} existed 
     * in none of the provided taxa. IDs of the taxa are {@code Integer}s representing 
     * their NCBI IDs (for instance, 9606 for human).
     * <p>
     * The approach is, for each taxon provided, to generate a custom version of 
     * the ontology, that will contain only the {@code OWLClass}es existing in this taxon. 
     * If you want to keep these intermediate  generated ontologies, you need to provide 
     * the path {@code storeOntologyDir} where to store them. The ontology files will be 
     * named <code>uberon_subset_TAXONID.obo</code>. If {@code storeOntologyDir} is 
     * {@code null}, the intermediate ontology files will not be saved. 
     * <p>
     * {@code uberonOnt} must be a version of the Uberon ontology containing the taxon 
     * constraints allowing to define in which taxa a structure exists. {@code taxOnt} 
     * must be a version of the taxonomy ontology containing disjoint classes axioms 
     * between sibling taxa, as explained in a Chris Mungall 
     * <a href='http://douroucouli.wordpress.com/2012/04/24/taxon-constraints-in-owl/'>
     * blog post</a>, see also {@link org.bgee.pipeline.species.GenerateTaxonOntology}. 
     * All IDs in {@code taxonIds} must corresponds to a taxon present 
     * in this taxonomy ontology, otherwise, an {@code IllegalArgumentException} 
     * is thrown.
     * 
     * @param uberonOnt         An {@code OWLOntology} that is the Uberon ontology 
     *                          containing taxon constraints.
     * @param taxOnt            An {@code OWLOntology} that is the taxonomy ontology 
     *                          containing disjoint classes axioms between sibling taxa.
     * @param taxonIds          A {@code Set} of {@code Integer}s that are the IDs 
     *                          from the NCBI website of the taxa to consider 
     *                          (for instance, 9606 for human).
     * @param storeOntologyDir  A {@code String} that is the path to a directory 
     *                          where to store intermediate ontologies. If {@code null} 
     *                          the generated ontologies will not be stored.
     * @return                  A {@code Map} where keys are IDs of the {@code OWLClass}es 
     *                          from the provided ontology, and values are  
     *                          {@code Set}s of {@code Integer}s containing the IDs 
     *                          of taxa in which the {@code OWLClass} exists.
     * @throws IllegalArgumentException     If some taxa in {@code taxonIds} could not 
     *                                      be found in {@code taxOnt}.
     * @throws UnknownOWLOntologyException  If the ontology stored in 
     *                                      {@code uberonFile} could not be used.
     * @throws OWLOntologyCreationException If the ontology stored in {@code uberonFile} 
     *                                      could not be used.
     * @throws IOException                  If {@code uberonFile} could not be opened. 
     */
    public Map<String, Set<Integer>> generateTaxonConstraints(OWLOntology uberonOnt, 
            OWLOntology taxOnt, Set<Integer> taxonIds, String storeOntologyDir) 
                    throws UnknownOWLOntologyException, OWLOntologyCreationException, 
                    IOException, IllegalArgumentException {
        log.entry(uberonOnt, taxOnt, taxonIds, storeOntologyDir);
        log.info("Start generating taxon constraints...");
        
        OWLGraphWrapper uberonWrapper = new OWLGraphWrapper(uberonOnt);
        OWLGraphWrapper taxOntWrapper = new OWLGraphWrapper(taxOnt);
        
        //first, we remove any "is_a" relations and disjoint classes axioms between taxa 
        //that might be present in Uberon, they can be inconsistent with the taxonomy we use.
        this.filterUberonOntology(uberonWrapper, taxOntWrapper);
        
        //now we merge the Uberon ontology and the taxonomy ontology for the reasoner 
        //to work properly, just importing them in a same OWLGraphWrapper woud not 
        //be enough
        uberonWrapper.mergeOntology(taxOntWrapper.getSourceOntology());
        
        //taxonConstraints will store the association between an Uberon OWLClass, 
        //and the taxa it exists in. So, first, we get all OWLClasses for which 
        //we want to generate taxon constraints (taxa are excluded)
        Map<String, Set<Integer>> taxonConstraints = new HashMap<String, Set<Integer>>();
        for (OWLClass cls: uberonWrapper.getAllOWLClasses()) {
            //skip owl:thing and owl:nothing
            if (cls.isTopEntity() || cls.isBottomEntity()) {
                continue;
            }
            //we do not want information about the taxa
            if (taxOntWrapper.getSourceOntology().containsClassInSignature(cls.getIRI())) {
                continue;
            }
            log.trace("Taxon constraints will be generated for: {}", cls);
            taxonConstraints.put(uberonWrapper.getIdentifier(cls), new HashSet<Integer>());
        }
        
        for (int taxonId: taxonIds) {
            //for each taxon, we clone our Uberon ontology merged with our taxonomy ontology, 
            //because the method getExistingOWLClasses will modified it.
            OWLOntology clonedUberon = OWLManager.createOWLOntologyManager().createOntology(
                IRI.create("Uberon for " + taxonId), 
                new HashSet<OWLOntology>(Arrays.asList(uberonWrapper.getSourceOntology())));
            
            Set<OWLClass> classesDefined = this.getExistingOWLClasses(
                    new OWLGraphWrapper(clonedUberon), taxonId, storeOntologyDir);
            for (OWLClass classDefined: classesDefined) {
                Set<Integer> existsInTaxa = taxonConstraints.get(
                        uberonWrapper.getIdentifier(classDefined));
                //if existsInTaxa is null,  it means it is not an OWLClass for which 
                //we want the taxon constraints (e.g., an OWLClass representin a taxon)
                if (existsInTaxa != null) {
                    log.trace("Defining existence of {} in taxon {}", classDefined, taxonId);
                    existsInTaxa.add(taxonId);
                }
            }
        }
        
        log.info("Done generating taxon constraints.");
        return log.exit(taxonConstraints);
    }
    
    /**
     * Remove any "is_a" relations and disjoint classes axioms between taxa 
     * that might be present in Uberon, they can be inconsistent with the taxonomy 
     * we use.
     * 
     * @param uberonOntWrapper  A {@code OWLGraphWrapper} wrapping the Uberon 
     *                          {@code OWLOntology} that will be modified.
     * @param taxOntWrapper     A {@code OWLGraphWrapper} wrapping our own custom 
     *                          taxonomy {@code OWLOntology}.
     */
    private void filterUberonOntology(OWLGraphWrapper uberonOntWrapper, 
            OWLGraphWrapper taxOntWrapper) {
        log.entry(uberonOntWrapper, taxOntWrapper);
        log.debug("Removing all axioms betwen taxa from Uberon");
        
        //Remove any "is_a" relations and disjoint classes axioms between taxa 
        //that might be present in Uberon, they can be inconsistent with the taxonomy 
        //we use.
        OWLOntology uberonOnt = uberonOntWrapper.getSourceOntology();
        OWLDataFactory factory = uberonOnt.getOWLOntologyManager().getOWLDataFactory();
        GenerateTaxonOntology disjointAxiomGenerator = new GenerateTaxonOntology();
        Set<OWLAxiom> axiomsToRemove = new HashSet<OWLAxiom>();
        
        for (OWLClass taxon: taxOntWrapper.getAllOWLClasses()) {
            //check that this taxon exists in Uberon
            if (!uberonOnt.containsClassInSignature(taxon.getIRI())) {
                continue;
            }

            //remove "is_a" relations beteen taxa and store the parent classes
            Set<OWLClass> parents = new HashSet<OWLClass>();
            Set<OWLSubClassOfAxiom> axioms = uberonOnt.getSubClassAxiomsForSubClass(taxon);
            for (OWLSubClassOfAxiom ax : axioms) {
                OWLClassExpression ce = ax.getSuperClass();
                if (!ce.isAnonymous()) {
                    parents.add(ce.asOWLClass());
                    axiomsToRemove.add(ax);
                }
            }
            
            //remove potential disjoint classes axioms to sibling taxa, 
            //and is_a relations to sub-taxa.
            for (OWLClass parent: parents) {
                Set<OWLClass> siblings = new HashSet<OWLClass>();
                for (OWLSubClassOfAxiom ax : 
                        uberonOnt.getSubClassAxiomsForSuperClass(parent)) {
                    OWLClassExpression ce = ax.getSubClass();
                    if (!ce.isAnonymous()) {
                        siblings.add(ce.asOWLClass());
                        axiomsToRemove.add(ax);
                    }
                }
                if (siblings.size() > 1) {
                    axiomsToRemove.addAll(
                            disjointAxiomGenerator.getCompactDisjoints(siblings, factory));
                    axiomsToRemove.addAll(
                            disjointAxiomGenerator.getVerboseDisjoints(siblings, factory));
                }
            }
            
        }
        int axiomsRemoved = uberonOnt.getOWLOntologyManager().removeAxioms(uberonOnt, 
                axiomsToRemove).size();
        
        log.debug("{} axioms between taxa removed from Uberon.", 
                axiomsRemoved);
        log.exit();
    }
    
    /**
     * Returns a {@code Set} of {@code OWLClass}es obtained from the ontology 
     * wrapped into {@code ontWrapper}, and that actually exists in the taxon with ID 
     * {@code taxonId}. If {@code storeOntologyDir} is not {@code null}, then 
     * the intermediate ontology, corresponding to the filtered version of the source 
     * ontology for the provided taxon, will be saved in that directory.
     * <p>
     * The {@code OWLOntology} wrapped in {@code ontWrapper} must be a version 
     * of the Uberon ontology containing the taxon constraints allowing to define 
     * in which taxa a structure exists, and merged with a taxonomy ontology containing 
     * disjoint classes axioms between sibling taxa, as explained in a Chris Mungall 
     * <a href='http://douroucouli.wordpress.com/2012/04/24/taxon-constraints-in-owl/'>
     * blog post</a>, see also {@link org.bgee.pipeline.species.GenerateTaxonOntology}. 
     * {@code taxonId} must corresponds to a taxon present in this 
     * taxonomy ontology, otherwise, an {@code IllegalArgumentException} is thrown.
     * <p>
     * The Uberon ontology and the taxonomy ontology must be actually merged for 
     * the reasoner to work correctly, not just imported in the {@code OWLGraphWrapper}.
     * 
     * @param ontWrapper        An {@code OWLGraphWrapper} wrapping the {@code OWLOntology} 
     *                          containing Uberon with taxon constraints, merged with 
     *                          the NCBI taxonomy containing disjoint classes axioms.
     * @param taxonId           An {@code int} that is the ID on the NCBI website 
     *                          of the taxon to consider (for instance, 9606 for human).
     * @param storeOntologyDir  A {@code String} that is the path to a directory 
     *                          where to store intermediate ontologies. If {@code null} 
     *                          the generated ontologies will not be stored.
     * @return                  A {@code Set} containing {@code OWLClass}es 
     *                          from the provided ontology, that exists in 
     *                          the provided taxon.
     * @throws IllegalArgumentException     If the taxon with ID {@code taxonId} 
     *                                      could not be found in the ontology.
     * @throws UnknownOWLOntologyException  If the ontology stored in 
     *                                      {@code uberonFile} could not be used.
     * @throws OWLOntologyCreationException If the ontology stored in {@code uberonFile} 
     *                                      could not be used.
     * @throws OBOFormatParserException     If {@code uberonFile} could not be parsed. 
     * @throws IOException                  If {@code uberonFile} could not be opened. 
     */
    private Set<OWLClass> getExistingOWLClasses(OWLGraphWrapper ontWrapper, int taxonId, 
            String storeOntologyDir) throws UnknownOWLOntologyException, 
            OWLOntologyCreationException, IOException, IllegalArgumentException  {
        log.entry(ontWrapper, taxonId, storeOntologyDir);
        log.info("Generating constraints for taxon {}...", taxonId);
        
        //Get the OWLClass corresponding to the requested taxon
        String ontTaxonId = OntologyUtils.getTaxOntologyId(taxonId);
        OWLClass taxClass = ontWrapper.getOWLClassByIdentifier(ontTaxonId);
        if (taxClass == null) {
            throw log.throwing(new IllegalArgumentException("A taxon ID " +
                    "provided could not be found in the provided ontology: " + 
                    taxonId));
        }
        
        //filter ontology
        SpeciesSubsetterUtil subSetter = new SpeciesSubsetterUtil(ontWrapper);
        subSetter.taxClass = taxClass;
        subSetter.reasoner = this.createReasoner(ontWrapper.getSourceOntology());
        subSetter.removeOtherSpecies();
        
        //if we want to store the intermediate ontology
        if (storeOntologyDir != null) {
            ontWrapper.clearCachedEdges();
            String outputFilePath = new File(storeOntologyDir, 
                    "uberon_subset" + taxonId + ".obo").getPath();
            new OntologyUtils(ontWrapper).saveAsOBO(outputFilePath);
        }
        
        log.info("Done generating constraints for taxon {}.", taxonId);
        return log.exit(ontWrapper.getOWLClassesFromSource());
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
    private OWLReasoner createReasoner(OWLOntology ont) {
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
     * {@link #createReasoner(OWLOntology)}).
     * 
     * @param factory   the {@code OWLReasonerFactory} to use.
     */
    public void setReasonerFactory(OWLReasonerFactory factory) {
        log.entry(factory);
        this.reasonerFactory = factory;
        log.exit();
    }
    
    /**
     * Write the taxon constraints in a TSV file. The taxon constraints are provided 
     * by {@code taxonConstraints} as a {@code Map} where keys are the OBO-like IDs 
     * of all {@code OWLClass}es examined, and are associated to a {@code Set} of 
     * {@code Integer}s, that are the NCBI IDs of the taxon in which the {@code OWLClass} 
     * exists, among all the taxa that were examined, listed in {@code taxonIds}. 
     * Additionally, it is provided an {@code OWLGraphWrapper} wrapping the 
     * {@code OWLOntology} were the {@code OWLClass}es examined come from, in order 
     * to be able to print their name.
     * <p>
     * The generated TSV file will have one header line. The columns will be: ID 
     * of the {@code OWLClass}, name of the {@code OWLClass}, IDs of each of the taxa 
     * that were examined. For each of the taxon column, a boolean is provided as 
     * "T" or "F", to define whether the associated {@code OWLClass} exists in it.
     * 
     * @param taxonConstraints  A {@code Map} where keys are IDs of the {@code OWLClass}es 
     *                          from the ontology that was examined, and values are  
     *                          {@code Set}s of {@code Integer}s containing the IDs 
     *                          of taxa in which the {@code OWLClass} exists.
     * @param taxonIds          A {@code Set} of {@code Integer}s that are the IDs 
     *                          from the NCBI website of the taxa that were considered 
     *                          (for instance, 9606 for human).
     * @param outputFile        A {@code String} that is the path to the output file 
     *                          were constraints will be written as TSV.
     * @throws IOException      If an error occurred while trying to write to 
     *                          {@code outputFile}.
     */
    private void writeToFile(Map<String, Set<Integer>> taxonConstraints, 
            Set<Integer> taxonIds, String outputFile, OWLGraphWrapper ontWrapper) 
            throws IOException {
        log.entry(taxonConstraints, taxonIds, outputFile, ontWrapper);

        //order the taxon IDs to get consistent column ordering between releases
        List<Integer> sortedTaxonIds = new ArrayList<Integer>(taxonIds);
        Collections.sort(sortedTaxonIds);
        //also, ordered the IDs of the OWLClasses, for easier comparison between 
        //releases
        List<String> sortedClassIds = new ArrayList<String>(taxonConstraints.keySet());
        Collections.sort(sortedClassIds);
        
        //create the header of the file, and the conditions on the columns
        int taxonCount = taxonIds.size();
        CellProcessor[] processors = new CellProcessor[taxonCount + 2];
        String[] header = new String[taxonCount + 2];
        //ID of the OWLClass (must be unique)
        processors[0] = new UniqueHashCode(new NotNull());
        processors[1] = new NotNull();
        header[0] = "Uberon ID";
        header[1] = "Uberon name";
        //boolean defining for each taxon if the OWLClass exists in it
        for (int i = 0; i < taxonCount; i++) {
            processors[i + 2] = new NotNull(new FmtBool("T", "F"));
            header[i + 2] = sortedTaxonIds.get(i).toString();
        }
        
        
        try (ICsvMapWriter mapWriter = new CsvMapWriter(new FileWriter(outputFile),
                CsvPreference.TAB_PREFERENCE)) {
            
            mapWriter.writeHeader(header);
            
            for (String uberonId: sortedClassIds) {
                Map<String, Object> row = new HashMap<String, Object>();
                row.put(header[0], uberonId);
                OWLClass cls = ontWrapper.getOWLClassByIdentifier(uberonId);
                String label = "-";
                if (cls != null) {
                    label = ontWrapper.getLabelOrDisplayId(cls);
                } else {
                    //we disable this assertion error, there are weird case 
                    //were getOWLClassByIdentifier does not find the OWLClass, 
                    //for instance, ID "biological:modeling".
                    //throw log.throwing(new AssertionError("Could not find class " +
                    //		"with ID " + uberonId));
                }
                row.put(header[1], label);
                for (Integer taxonId: taxonIds) {
                    row.put(taxonId.toString(), 
                            taxonConstraints.get(uberonId).contains(taxonId));
                }
                
                mapWriter.write(row, header, processors);
            }
        }
        
        log.exit();
    }
}
