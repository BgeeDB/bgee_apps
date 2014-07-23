package org.bgee.pipeline.uberon;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.Utils;
import org.bgee.pipeline.annotations.AnnotationCommon;
import org.bgee.pipeline.ontologycommon.OntologyUtils;
import org.bgee.pipeline.species.GenerateTaxonOntology;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.elk.owlapi.ElkReasonerConfiguration;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.elk.reasoner.config.ReasonerConfiguration;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.supercsv.cellprocessor.FmtBool;
import org.supercsv.cellprocessor.ParseBool;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvListReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.io.ICsvMapWriter;

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
public class TaxonConstraints {
    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = 
            LogManager.getLogger(TaxonConstraints.class.getName());
    
    /**
     * An {@code int} that is the maximum number of workers when using the 
     * {@code ElkResoner}, see {@link #createReasoner(OWLOntology)}.
     */
    private final static int MAX_WORKER_COUNT = 10;
    
    /**
     * A {@code String} that is the name of the column containing Uberon IDs, 
     * in the taxon constraints file.
     */
    public final static String UBERON_ID_COLUMN_NAME = "Uberon ID";
    /**
     * A {@code String} that is the name of the column containing Uberon names, 
     * in the taxon constraints file.
     */
    public final static String UBERON_NAME_COLUMN_NAME = "Uberon name";
    
    /**
     * Several actions can be launched from this main method, depending on the first 
     * element in {@code args}: 
     * <ul>
     * <li>If the first element in {@code args} is "generateTaxonConstraints", 
     * it will launch the generation of a TSV files, allowing to know, 
     * for each {@code OWLClass} in the Uberon ontology, in which taxa it exits, 
     * among the taxa provided through another TSV file, containing their NCBI IDs. 
     * See {@link #generateTaxonConstraints(String, String, String)} 
     * for more details.
     * Following elements in {@code args} must then be: 
     *   <ol>
     *   <li>path to the source Uberon OWL ontology file. This Uberon ontology must 
     *   contain the taxon constraints ("in taxon" and "only_in_taxon" relations, 
     *   not all Uberon versions contain them).
     *   <li>path to the NCBI taxonomy ontology. This taxonomy must contain disjoint 
     *   classes axioms between sibling taxa, as explained in a Chris Mungall 
     *   <a href='http://douroucouli.wordpress.com/2012/04/24/taxon-constraints-in-owl/'>
     *   blog post</a>, see also {@link org.bgee.pipeline.species.GenerateTaxonOntology}.
     *   <li>path to the TSV files containing the IDs of the taxa for which we want 
     *   to generate the taxon constraints, corresponding to the NCBI ID (e.g., 9606 
     *   for human). The first line should be a header line, defining a column to get 
     *   IDs from, named exactly "taxon ID" (other columns are optional and will be ignored).
     *   <li>path to the generated TSV file, output of the method.
     *   <li>OPTIONNAL: a path to a directory where to store the intermediate generated 
     *   ontologies. If this parameter is provided, an ontology will be generated 
     *   for each taxon, and stored in this directory, containing only 
     *   the {@code OWLClass}es existing in this taxon. If not provided, the intermediate 
     *   ontologies will not be stored. 
     *   </ol>
     * <li>If the first element in {@code args} is "explainTaxonConstraints", 
     * the action will be to display the explanation for the existence or absence 
     * of existence of a Uberon term in a given taxon. See {@link 
     * #explainTaxonExistence(Collection, Collection)} for more details. 
     * The explanation will be displayed using the logger of this class with an 
     * {@code info} level.
     * Following elements in {@code args} must then be: 
     *   <ol>
     *   <li>path to the source Uberon OWL ontology file. This Uberon ontology must 
     *   contain the taxon constraints ("in taxon" and "only_in_taxon" relations, 
     *   not all Uberon versions contain them).
     *   <li>path to the NCBI taxonomy ontology. It is not mandatory for this taxonomy 
     *   to include disjoint classes axioms between sibling taxa. 
     *   <li>OBO-like ID of the Uberon term for which we want an explanation
     *   <li>NCBI ID (for instance, 9606) of the taxon in which we want to explain 
     *   existence or nonexistence of the requested Uberon term.
     * </ul>
     * 
     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
     * @throws IllegalArgumentException     If some taxa in the taxon file could not
     *                                      be found in the ontology.
     * @throws FileNotFoundException        If some files could not be found.
     * @throws IOException                  If some files could not be read/written.
     * @throws UnknownOWLOntologyException  If the ontology provided could not be used.
     * @throws OWLOntologyCreationException If the ontology provided could not be used.
     * @throws OBOFormatParserException     If the ontology provided could not be parsed. 
     * @throws OWLOntologyStorageException  If an error occurred while saving the 
     *                                      intermediate ontologies in owl.
     */
    public static void main(String[] args) throws UnknownOWLOntologyException, 
        IllegalArgumentException, FileNotFoundException, OWLOntologyCreationException, 
        OBOFormatParserException, IOException, OWLOntologyStorageException {
        log.entry((Object[]) args);
        
        if (args[0].equalsIgnoreCase("explainTaxonConstraints")) {
            if (args.length != 5) {
                throw log.throwing(new IllegalArgumentException(
                        "Incorrect number of arguments provided, expected " + 
                        "3 arguments, " + args.length + " provided."));
            }
            
            String clsId = args[3];
            String taxId = args[4];
            Collection<List<OWLObject>> explanations = 
                    new TaxonConstraints(args[1], args[2]).explainTaxonExistence(
                            Arrays.asList(clsId), 
                            Arrays.asList(Integer.parseInt(taxId)));
            if (explanations.isEmpty()) {
                log.info("No specific explanation for existence of {} in taxon {}. " +
                		"If it is defined as non-existing, then there is a problem...", 
                		clsId, taxId);
            } else {
                log.info("--------------------");
                log.info("Explanations for existence/nonexistence of {} in taxon {}: ", 
                        clsId, taxId);
            }
            for (List<OWLObject> explanation: explanations) {
                log.info(explanation);
            }
        } else if (args[0].equalsIgnoreCase("generateTaxonConstraints")) {
        
            if (args.length < 5 || args.length > 6) {
                throw log.throwing(new IllegalArgumentException("Incorrect number of arguments " +
                        "provided, expected 5 to 6 arguments, " + args.length + 
                        " provided."));
            }
            
            String storeDir = null;
            if (args.length == 6) {
                storeDir = args[5];
            }
            TaxonConstraints generate = new TaxonConstraints(args[1], args[2]);
            generate.generateTaxonConstraints(args[3], args[4], storeDir);
        } else {
            throw log.throwing(new UnsupportedOperationException("The following action " +
                    "is not recognized: " + args[0]));
        }
        
        log.exit();
    }
    
    /**
     * A {@code OWLGraphWrapper} provided at instantiation, wrapping the Uberon 
     * {@code OWLOntology}, used to generate or retrieve taxon constraints.
     */
    private final OWLGraphWrapper uberonOntWrapper;
    /**
     * A {@code OWLGraphWrapper} provided at instantiation, wrapping the taxonomy 
     * {@code OWLOntology}, used to generate or retrieve taxon constraints.
     */
    private final OWLGraphWrapper taxOntWrapper;

    /**
     * Constructor accepting the Uberon {@code OWLOntology} and the taxonomy 
     * {@code OWLOntology}, allowing to generate or retrieve taxon constraints.
     * 
     * @param uberonOnt The Uberon {@code OWLOntology}.
     * @param taxOnt    The taxonomy {@code OWLOntology}.
     * @throws UnknownOWLOntologyException      if {@code uberonOnt} or {@code taxOnt} 
     *                                          could not be used.
     * @throws OWLOntologyCreationException     if {@code uberonOnt} or {@code taxOnt} 
     *                                          could not be used.
     */
    public TaxonConstraints(OWLOntology uberonOnt, OWLOntology taxOnt) 
            throws UnknownOWLOntologyException, OWLOntologyCreationException {
        this.uberonOntWrapper = new OWLGraphWrapper(uberonOnt);
        this.taxOntWrapper    = new OWLGraphWrapper(taxOnt);
        
        this.prepareUberon();
    }
    /**
     * Constructor accepting the path to the Uberon ontology and the path to the taxonomy 
     * ontology, allowing to generate or retrieve taxon constraints.
     * 
     * @param uberonFile    A {@code String} that is the path to the Uberon ontology.
     * @param taxOntFile    A {@code String} that is the path to the taxonomy ontology.
     * @throws UnknownOWLOntologyException  If the provided ontologies could not be used.
     * @throws OWLOntologyCreationException If the provided ontologies could not be used.
     * @throws OBOFormatParserException     If the provided ontologies could not be used.
     * @throws IOException                  If the provided files could not be read.
     */
    public TaxonConstraints(String uberonFile, String taxOntFile) 
            throws UnknownOWLOntologyException, OWLOntologyCreationException, 
            OBOFormatParserException, IOException {
        this(OntologyUtils.loadOntology(uberonFile), OntologyUtils.loadOntology(taxOntFile));
    }
    /**
     * Constructor to be used when there is no need to use the Uberon or taxonomy 
     * ontologies.
     */
    public TaxonConstraints() {
        this.uberonOntWrapper = null;
        this.taxOntWrapper    = null;
    }
    
    /**
     * Prepares {@link #uberonOntWrapper} to be used to generate or retrieve taxon constraints, 
     * by using {@link #taxOntWrapper}. This method notably removes any "is_a" relations 
     * and disjoint classes axioms between taxa that might be present in Uberon, 
     * they can be inconsistent with the taxonomy we use. Then it merges the Uberon 
     * ontology and the taxonomy ontology, for a reasoner to work properly.
     */
    private void prepareUberon() throws OWLOntologyCreationException {
        log.entry();
        //first, we remove any "is_a" relations and disjoint classes axioms between taxa 
        //that might be present in Uberon, they can be inconsistent with the taxonomy we use.
        this.filterUberonOntology();
        
        //now we merge the Uberon ontology and the taxonomy ontology for the reasoner 
        //to work properly, just importing them in a same OWLGraphWrapper woud not 
        //be enough
        this.uberonOntWrapper.mergeOntology(this.taxOntWrapper.getSourceOntology());
        this.uberonOntWrapper.clearCachedEdges();
        log.exit();
    }
    
    /**
     * Remove any "is_a" relations and disjoint classes axioms between taxa 
     * that might be present in {@link #uberonOntWrapper}, they can be inconsistent 
     * with the taxonomy we use, stored in {@link #taxOntWrapper}.
     */
    private void filterUberonOntology() {
        log.entry();
        log.debug("Removing all axioms betwen taxa from Uberon");
        
        //Remove any "is_a" relations and disjoint classes axioms between taxa 
        //that might be present in Uberon, they can be inconsistent with the taxonomy 
        //we use.
        OWLOntology uberonOnt = this.uberonOntWrapper.getSourceOntology();
        OWLDataFactory factory = uberonOnt.getOWLOntologyManager().getOWLDataFactory();
        GenerateTaxonOntology disjointAxiomGenerator = new GenerateTaxonOntology();
        Set<OWLAxiom> axiomsToRemove = new HashSet<OWLAxiom>();
        
        for (OWLClass taxon: this.taxOntWrapper.getAllOWLClasses()) {
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
     * Generates taxon constraints. Launches the generation of a TSV files, 
     * allowing to know, for each {@code OWLClass} in the Uberon ontology provided 
     * at instantiation (taxonomy classes excepted), in which taxa it exits, 
     * among the taxa provided through the TSV file {@code taxonIdFile}. The first 
     * line of this file should be a header line, defining a column to get IDs from, 
     * named exactly "taxon ID" (other columns are optional and will be ignored). 
     * These IDs must correspond to the NCBI IDs (for instance, 9606 for human). 
     * <p>
     * This method also needs to be provided at instantiation with a taxonomy ontology, 
     * that must contain disjoint classes axioms between sibling taxa, as explained in 
     * a Chris Mungall 
     * <a href='http://douroucouli.wordpress.com/2012/04/24/taxon-constraints-in-owl/'>
     * blog post</a>, see also {@link org.bgee.pipeline.species.GenerateTaxonOntology}. 
     * <p>
     * The results will be stored in the TSV file {@code outputFile}. 
     * <p>
     * The approach is, for each taxon provided, to generate a custom version 
     * of the ontology, that will contain only the {@code OWLClass}es existing 
     * in this taxon. If you want to keep these intermediate generated ontologies, 
     * you need to provide the path {@code storeOntologyDir} where to store them. 
     * The ontology files will be named <code>uberon_subset_TAXONID.owl</code>. 
     * If {@code storeOntologyDir} is {@code null}, the intermediate ontology files 
     * will not be saved. 
     * 
     * @param taxonIdFile       A {@code String} that is the path to the TSV file 
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
     * @throws IOException                  If some files could not be read/written.
     * @throws OWLOntologyCreationException If it was not possible to clone the ontology 
     *                                      before modifying it.
     * @throws OWLOntologyStorageException  If an error occurred while saving the 
     *                                      intermediate ontologies in owl.
     */
    public void generateTaxonConstraints(String taxonIdFile, String outputFile, 
            String storeOntologyDir) throws IOException, IllegalArgumentException, 
            OWLOntologyStorageException, OWLOntologyCreationException  {
        
        log.entry(taxonIdFile, outputFile, storeOntologyDir);
        
        Set<Integer> taxonIds = AnnotationCommon.getTaxonIds(taxonIdFile);
        Map<String, Set<Integer>> constraints = 
                this.generateTaxonConstraints(taxonIds, storeOntologyDir);
        this.writeToFile(constraints, taxonIds, outputFile);
        
        log.exit();
    }
    
    /**
     * Returns a {@code Map} representing the taxon constraints generated using  
     * the Uberon ontology and taxonomy ontology provided at instantiation, 
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
     * named <code>uberon_subset_TAXONID.owl</code>. If {@code storeOntologyDir} is 
     * {@code null}, the intermediate ontology files will not be saved. 
     * <p>
     * The Uberon ontology provided at instantiation must be a version containing the taxon 
     * constraints allowing to define in which taxa a structure exists. The taxonomy ontology 
     * provided at instantiation must be a version of the taxonomy ontology containing 
     * disjoint classes axioms between sibling taxa, as explained in a Chris Mungall 
     * <a href='http://douroucouli.wordpress.com/2012/04/24/taxon-constraints-in-owl/'>
     * blog post</a>, see also {@link org.bgee.pipeline.species.GenerateTaxonOntology}. 
     * All IDs in {@code taxonIds} must corresponds to a taxon present 
     * in this taxonomy ontology, otherwise, an {@code IllegalArgumentException} 
     * is thrown.
     * 
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
     * @throws OWLOntologyCreationException If it was not possible to clone the ontology 
     *                                      before modifying it.
     * @throws OWLOntologyStorageException  If an error occurred while saving the 
     *                                      intermediate ontologies in owl. 
     */
    public Map<String, Set<Integer>> generateTaxonConstraints(Set<Integer> taxonIds, 
            String storeOntologyDir) throws IllegalArgumentException, 
            OWLOntologyStorageException, OWLOntologyCreationException {
        log.entry(taxonIds, storeOntologyDir);
        
        if (this.uberonOntWrapper == null || this.taxOntWrapper == null) {
            throw log.throwing(new IllegalStateException("You must provide the Uberon " +
            		"ontology and the taxonomy ontology at instantiation."));
        }
        log.info("Start generating taxon constraints...");
        
        //if we want to store the intermediate ontologies
        if (storeOntologyDir != null) {
            String outputFilePath = new File(storeOntologyDir, 
                    "uberon_reasoning_source.owl").getPath();
            new OntologyUtils(this.uberonOntWrapper).saveAsOWL(outputFilePath);
        }
        
        //taxonConstraints will store the association between an Uberon OWLClass, 
        //and the taxa it exists in. So, first, we get all OWLClasses for which 
        //we want to generate taxon constraints (taxa are excluded)
        Map<String, Set<Integer>> taxonConstraints = new HashMap<String, Set<Integer>>();
        for (OWLClass cls: this.uberonOntWrapper.getAllOWLClasses()) {
            //we do not want information about the taxa
            if (this.taxOntWrapper.getSourceOntology().containsClassInSignature(cls.getIRI())) {
                continue;
            }
            log.trace("Taxon constraints will be generated for: {}", cls);
            taxonConstraints.put(this.uberonOntWrapper.getIdentifier(cls), 
                    new HashSet<Integer>());
        }
        
        //now, generate the constraints one taxon at a time.
        for (int taxonId: taxonIds) {
            
            //for each taxon, we clone our Uberon ontology merged with our taxonomy ontology, 
            //because the method getExistingOWLClasses will modified it.
            //we use a new OWLOntologyManager to be sure there is no memory leack.
            OWLOntology clonedUberon = OWLManager.createOWLOntologyManager().createOntology(
                IRI.create("Uberon_for_" + taxonId), 
                new HashSet<OWLOntology>(Arrays.asList(
                        this.uberonOntWrapper.getSourceOntology())));
            
            Set<OWLClass> classesDefined = this.getExistingOWLClasses(
                    new OWLGraphWrapper(clonedUberon), taxonId, storeOntologyDir);
            
            for (OWLClass classDefined: classesDefined) {
                Set<Integer> existsInTaxa = taxonConstraints.get(
                        this.uberonOntWrapper.getIdentifier(classDefined));
                //if existsInTaxa is null,  it means it is not an OWLClass for which 
                //we want the taxon constraints (e.g., an OWLClass representing a taxon)
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
     * @throws OWLOntologyStorageException  If an error occurred while saving the 
     *                                      intermediate ontology in owl.
     * @throws OBOFormatParserException     If {@code uberonFile} could not be parsed. 
     * @throws IOException                  If {@code uberonFile} could not be opened. 
     */
    private Set<OWLClass> getExistingOWLClasses(OWLGraphWrapper ontWrapper, int taxonId, 
            String storeOntologyDir) throws UnknownOWLOntologyException, 
            IllegalArgumentException, OWLOntologyStorageException  {
        log.entry(ontWrapper, taxonId, storeOntologyDir);
        log.info("Generating constraints for taxon {}...", taxonId);
        
        //Get the OWLClass corresponding to the requested taxon
        String ontTaxonId = OntologyUtils.getTaxOntologyId(taxonId);
        OWLClass taxClass = ontWrapper.getOWLClassByIdentifier(ontTaxonId);
        if (taxClass == null || 
                ontWrapper.isObsolete(taxClass) || ontWrapper.getIsObsolete(taxClass)) {
            throw log.throwing(new IllegalArgumentException("A taxon ID " +
                    "provided could not be found or was deprecated in " +
                    "the provided ontology: " + taxonId));
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
                    "uberon_subset" + taxonId + ".owl").getPath();
            new OntologyUtils(ontWrapper).saveAsOWL(outputFilePath);
        }
        
        log.info("Done generating constraints for taxon {}.", taxonId);
        return log.exit(ontWrapper.getAllOWLClassesFromSource());
    }
    

    /**
     * Creates and returns an {@code OWLReasoner} to reason on the provided 
     * {@code OWLOntology}. 
     * <p>
     * As of Bgee 13, the reasoner used is the {@code ElkReasoner}, configured 
     * to use a maximum number of workers of {@link #MAX_WORKER_COUNT} (can be less 
     * depending on your number of processors).
     * 
     * @param ont  The {@code OWLOntology} which the returned {@code OWLReasoner} 
     *              should reason on.
     * @return      An <code>OWLReasoner</code> set to reason on {@code ont}.
     */
    private OWLReasoner createReasoner(OWLOntology ont) {
        log.entry(ont);
        ElkReasonerConfiguration config = new ElkReasonerConfiguration();
        //we need to set the number of workers because on our ubber machines, 
        //we have too many processors, so that we have too many workers, 
        //and too many memory consumed. 
        if (config.getElkConfiguration().getParameterAsInt(
                ReasonerConfiguration.NUM_OF_WORKING_THREADS) > MAX_WORKER_COUNT) {
            config.getElkConfiguration().setParameter(
                ReasonerConfiguration.NUM_OF_WORKING_THREADS, String.valueOf(MAX_WORKER_COUNT));
        }
        return log.exit(new ElkReasonerFactory().createReasoner(ont, config));
    }
    
    /**
     * Write the taxon constraints in a TSV file. The taxon constraints are provided 
     * by {@code taxonConstraints} as a {@code Map} where keys are the OBO-like IDs 
     * of all {@code OWLClass}es examined, and are associated to a {@code Set} of 
     * {@code Integer}s, that are the NCBI IDs of the taxon in which the {@code OWLClass} 
     * exists, among all the taxa that were examined, listed in {@code taxonIds}. 
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
            Set<Integer> taxonIds, String outputFile) throws IOException {
        log.entry(taxonConstraints, taxonIds, outputFile);

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
        header[0] = UBERON_ID_COLUMN_NAME;
        header[1] = UBERON_NAME_COLUMN_NAME;
        //boolean defining for each taxon if the OWLClass exists in it
        for (int i = 0; i < taxonCount; i++) {
            processors[i + 2] = new NotNull(new FmtBool("T", "F"));
            header[i + 2] = sortedTaxonIds.get(i).toString();
        }
        
        
        try (ICsvMapWriter mapWriter = new CsvMapWriter(new FileWriter(outputFile),
                Utils.TSVCOMMENTED)) {
            
            mapWriter.writeHeader(header);
            
            for (String uberonId: sortedClassIds) {
                Map<String, Object> row = new HashMap<String, Object>();
                row.put(header[0], uberonId);
                OWLClass cls = this.uberonOntWrapper.getOWLClassByIdentifier(uberonId);
                String label = "-";
                if (cls != null) {
                    label = this.uberonOntWrapper.getLabelOrDisplayId(cls);
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
    
    /**
     * Provides explanations about the sources of some taxon constraints on 
     * the {@code OWLClass}es provided through {@code owlClassIds}, related to the taxa 
     * provided through {@code taxonIds}. This method allows to know why a given term, 
     * in the Uberon ontology provided at instantiation, is defined as existing 
     * or not existing, in some given taxa, by using information about taxonomy 
     * from the taxonomy ontology provided at instantiation.
     * <p>
     * For each requested {@code OWLClass}, explanations are provided as paths going from 
     * the {@code OWLClass}, to a taxon constraint pertinent to any of the requested taxa. 
     * A path is represented as a {@code List} of {@code OWLObject}s. The first 
     * {@code OWLObject} is always one of the requested {@code OWLClass}. 
     * Following {@code OWLObject}s are either {@code OWLClass}es, or anonymous 
     * {@code OWLClassExpression}s, representing the targets of {@code SubClassOfAxiom}s. 
     * The final {@code OWLObject} is either an anonymous {@code OWLClassExpression}s 
     * representing a "only_in_taxon" relation, or an {@code OWLAnnotation} 
     * representing a "never_in_taxon" annotation.
     * <p>
     * If some of the requested {@code OWLClass}es are not found in the returned 
     * explanations, or their explanations do not cover all requested taxa, it means 
     * there is no particular explanation for existence of these {@code OWLClass}es 
     * in the taxon, they simply exist by default.
     * <p>
     * See the owltools javadoc for {@code 
     * owltools.mooncat.SpeciesSubsetterUtil#explainTaxonConstraint(Collection, Collection)} 
     * for more details.
     * 
     * @param owlClassIds   A {@code Collection} of {@code String}s that are the OBO-like 
     *                      IDs of the {@code OWLClass}es for which we want explanations 
     *                      of taxon constraints.
     * @param taxonIds      A {@code Collection} of {@code String}s that are the OBO-like 
     *                      IDs of the {@code OWLClass}es representing taxa, for which 
     *                      we want explanations of taxon constraints.
     * @return              A {@code Collection} of {@code List}s of {@code OWLObject}s, 
     *                      where each {@code List} correspond to a walk explaining 
     *                      a taxon constraint.
     * @throws IllegalArgumentException    If some of the requested {@code OWLClass}es 
     *                                     or requested taxa could not be found in 
     *                                     the provided ontologies.
     */
    public Collection<List<OWLObject>> explainTaxonExistence(Collection<String> owlClassIds, 
            Collection<Integer> taxonIds) throws IllegalArgumentException {
        log.entry(owlClassIds, taxonIds);
        if (this.uberonOntWrapper == null || this.taxOntWrapper == null) {
            throw log.throwing(new IllegalStateException("You must provide the Uberon " +
                    "ontology and the taxonomy ontology at instantiation."));
        }
        
        for (int taxonId: taxonIds) {
            if (taxOntWrapper.getOWLClassByIdentifier(
                    OntologyUtils.getTaxOntologyId(taxonId)) == null) {
                throw log.throwing(new IllegalArgumentException("The requested taxon " + 
                    taxonId + " does not exist in the provided taxonomy ontology."));
            }
        }

        SpeciesSubsetterUtil util = new SpeciesSubsetterUtil(this.uberonOntWrapper);
        return log.exit(util.explainTaxonConstraint(owlClassIds, 
                OntologyUtils.convertToTaxOntologyIds(taxonIds)));
    }

    
    /**
     * Extract from the taxon constraints file {@code taxonConstraintsFile} 
     * the taxon IDs included. 
     * 
     * @param taxonConstraintsFile      A {@code String} that is the path to the 
     *                                  tqxon constraints file.
     * @return                          A {@code Set} of {@code Integer}s representing 
     *                                  the NCBI IDs (e.g., 9606) of the taxa present 
     *                                  in {@code taxonConstraintsFile}
     * @throws FileNotFoundException    If {@code taxonConstraintsFile} could not 
     *                                  be found.
     * @throws IOException              If {@code taxonConstraintsFile} could not 
     *                                  be read.
     */
    public static Set<Integer> extractTaxonIds(String taxonConstraintsFile) 
            throws FileNotFoundException, IOException {
        log.entry(taxonConstraintsFile);
        try (ICsvListReader listReader = new CsvListReader(
                new FileReader(taxonConstraintsFile), Utils.TSVCOMMENTED)) {
            //find the index of the column with name columnName
            String[] headers = listReader.getHeader(true);
            Set<Integer> taxonIds = new HashSet<Integer>();
            //the first two columns are Uberon ID and Uberon name. Following columns 
            //are taxon IDs
            for (int i = 2; i < headers.length; i++) {
                if (headers[i] != null) {
                    taxonIds.add(Integer.parseInt(headers[i]));
                }
            }
            
            return log.exit(taxonIds);
        }
    }
    
    /**
     * Delegates to {@link #extractTaxonConstraints(String, Map)} with the {@code Map} 
     * argument {@code null} (no replacement of taxon constaints, just get them from the file).
     * 
     * @param taxonConstraintsFile  See same name argument in 
     *                              {@link #extractTaxonConstraints(String, Map)}
     * @return                      See returned value in 
     *                              {@link #extractTaxonConstraints(String, Map)}
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static Map<String, Set<Integer>> extractTaxonConstraints(String taxonConstraintsFile) 
            throws FileNotFoundException, IOException {
        log.entry(taxonConstraintsFile);
        return log.exit(TaxonConstraints.extractTaxonConstraints(taxonConstraintsFile, null));
    }
    
    /**
     * Extracts taxon constraints from the file {@code taxonConstraintsFile} and potentially 
     * overrides some of them. 
     * The returned {@code Map} contains the OBO-like IDs of all Uberon terms 
     * present in the file, as keys, associated to a {@code Set} of {@code Integer}s, 
     * that are the IDs of the taxa in which it exists, among the taxa present in the file. 
     * If the {@code Set} is empty, then it means that the {@code OWLClass} existed 
     * in none of the taxa. IDs of the taxa are {@code Integer}s representing 
     * their NCBI IDs (for instance, 9606 for human).
     * <p>
     * When {@code idStartsToOverridenTaxonIds} is not null, it allows to override constraints 
     * retrieved from the file: when the OBO-like ID of an Uberon terms starts with 
     * one of the key of {@code idStartsToOverridenTaxonIds}, its taxon constraints 
     * are replaced with the associated value. If the start of the OBO-like ID matches 
     * several keys, then the longest match will be considered. 
     * 
     * @param taxonConstraintsFile          A {@code String} that is the path to the 
     *                                      taxon constraints file.
     * @param idStartsToOverridenTaxonIds   A {@code Map} where keys are {@code String}s 
     *                                      representing prefixes of uberon terms to match, 
     *                                      the associated value being a {@code Set} 
     *                                      of {@code Integer}s to replace taxon constraints 
     *                                      of matching terms.
     * @return                          A {@code Map} where keys are IDs of the Uberon 
     *                                  {@code OWLClass}es, and values are {@code Set}s 
     *                                  of {@code Integer}s containing the IDs of taxa 
     *                                  in which the {@code OWLClass} exists.
     * @throws FileNotFoundException    If {@code taxonConstraintsFile} could not 
     *                                  be found.
     * @throws IOException              If {@code taxonConstraintsFile} could not 
     *                                  be read.
     */
    public static Map<String, Set<Integer>> extractTaxonConstraints(String taxonConstraintsFile, 
            Map<String, Set<Integer>> idStartsToOverridenTaxonIds) 
                throws FileNotFoundException, IOException {
        log.entry(taxonConstraintsFile, idStartsToOverridenTaxonIds);
        
        try (ICsvMapReader mapReader = new CsvMapReader(new FileReader(taxonConstraintsFile), 
                Utils.TSVCOMMENTED)) {
            
            String[] header = mapReader.getHeader(true);
            CellProcessor[] processors = new CellProcessor[header.length];
            for (int i = 0; i < header.length; i++) {
                if (header[i].equals(UBERON_ID_COLUMN_NAME)) {
                    processors[i] = new NotNull(new UniqueHashCode());
                } else if (header[i].equals(UBERON_NAME_COLUMN_NAME)) {
                    processors[i] = null;
                } else {
                    processors[i] = new NotNull(new ParseBool());
                }
            }

            Map<String, Set<Integer>> constraints = new HashMap<String, Set<Integer>>();
            Map<String, Object> lineMap;
            while( (lineMap = mapReader.read(header, processors)) != null ) {
                
                String uberonId = (String) lineMap.get(UBERON_ID_COLUMN_NAME);
                Set<Integer> replacementConstraints = null;
                String matchingPrefix = "";
                if (idStartsToOverridenTaxonIds != null) {
                    for (Entry<String, Set<Integer>> idStartToTaxonIds: 
                        idStartsToOverridenTaxonIds.entrySet()) {
                        
                        if (uberonId.startsWith(idStartToTaxonIds.getKey()) && 
                                idStartToTaxonIds.getKey().length() > matchingPrefix.length()) {
                            matchingPrefix = idStartToTaxonIds.getKey();
                            log.trace("Uberon ID {} matching prefix {}, taxon constraints overriden: {}", 
                                    uberonId, matchingPrefix, idStartToTaxonIds.getValue());
                            replacementConstraints = 
                                    new HashSet<Integer>(idStartToTaxonIds.getValue());
                            //continue iterations anyway in case there is a longest match
                        }
                    }
                }
                
                if (replacementConstraints != null) {
                    constraints.put(uberonId, replacementConstraints);
                } else {
                    Set<Integer> existingConstraints = new HashSet<Integer>();
                    constraints.put(uberonId, existingConstraints);
                    
                    for (int i = 0; i < header.length; i++) {
                        if (!header[i].equals(UBERON_ID_COLUMN_NAME) && 
                                !header[i].equals(UBERON_NAME_COLUMN_NAME)) {
                            if ((Boolean) lineMap.get(header[i])) {
                                existingConstraints.add(Integer.parseInt(header[i]));
                            }
                        }
                    }
                }
            }
            return log.exit(constraints);
        }
    }
}
