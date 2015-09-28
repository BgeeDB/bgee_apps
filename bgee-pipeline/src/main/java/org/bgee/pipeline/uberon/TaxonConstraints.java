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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.CommandRunner;
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
     * See {@link #generateTaxonConstraints(String, Map, String, Map, String, String)} 
     * for more details. Following elements in {@code args} must then be: 
     *   <ol>
     *   <li>path to the source Uberon OWL ontology file. This Uberon ontology must 
     *   contain the taxon constraints ("in taxon" and "never_in_taxon" relations, 
     *   not all Uberon versions contain them).
     *   <li>path to the NCBI taxonomy ontology. This taxonomy must contain disjoint 
     *   classes axioms between sibling taxa, as explained in a Chris Mungall 
     *   <a href='http://douroucouli.wordpress.com/2012/04/24/taxon-constraints-in-owl/'>
     *   blog post</a>, see also {@link org.bgee.pipeline.species.GenerateTaxonOntology}.
     *   <li>path to the TSV files containing the IDs of the taxa for which we want 
     *   to generate the taxon constraints, corresponding to the NCBI ID (e.g., 9606 
     *   for human). The first line should be a header line, defining a column to get 
     *   IDs from, named exactly "taxon ID" (other columns are optional and will be ignored).
     *   <li>path to a version of the Uberon ontology different to the one 
     *   containing taxon constraints. The taxon constraints will be produced 
     *   for the classes present in this ontology. If equal to {@link CommandRunner#EMPTY_ARG}, 
     *   then the Uberon ontology containing taxon constraints will be used. 
     *   <li>A {@code Map} to potentially override taxon constraints, see {@link 
     *   org.bgee.pipeline.CommandRunner#parseMapArgumentAsInteger(String)} to see 
     *   how to provide it. Can be empty (see {@link CommandRunner#EMPTY_LIST}). Example 
     *   of command line argument: {@code EV:/9606,MA:/10090}.
     *   <li>a map specifying whether the ontology should first be simplified in several steps 
     *   before generating the constraints, for some taxa. Keys should be the NCBI IDs 
     *   of taxa for which constraints will be requested, values should be a list 
     *   of NCBI IDs to use to first simplify step by step the ontology before generating 
     *   the constraints, for the associated key taxon, by removing terms specific 
     *   to these taxa, in the order of the list. If a taxon is absent from the keyset, 
     *   or its associated list is empty, then no simplification steps is requested 
     *   before generating the constraints for this taxon.
     *   Key-value pairs must be separated by {@link CommandRunner#LIST_SEPARATOR}, keys must be  
     *   separated from their associated value by {@link CommandRunner#KEY_VALUE_SEPARATOR}, 
     *   values must be separated by {@link CommandRunner#VALUE_SEPARATOR}. 
     *   Example of command line argument: 
     *   {@code 7712/7742--89593,6040/7742--89593--33511--33213--6072}.
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
    //XXX: currently, only the ontology ext.owl allows to correctly infer taxon constraints
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
        
            if (args.length < 8 || args.length > 9) {
                throw log.throwing(new IllegalArgumentException("Incorrect number of arguments " +
                        "provided, expected 8 to 9 arguments, " + args.length + 
                        " provided."));
            }
            
            String storeDir = null;
            if (args.length == 9) {
                storeDir = args[8];
            }
            TaxonConstraints generate = new TaxonConstraints(args[1], args[2]);
            generate.generateTaxonConstraints(args[3], 
                    CommandRunner.parseMapArgumentAsAllInteger(args[4]), 
                    CommandRunner.parseArgument(args[5]), 
                    CommandRunner.parseMapArgumentAsInteger(args[6]).entrySet().stream()
                    .collect(Collectors.toMap(Entry::getKey, e -> new HashSet<Integer>(e.getValue()))), 
                    args[7], storeDir);
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
     * A {@code Function} accepting an {@code OWLGraphWrapper} as input and returning 
     * a {@code SpeciesSubsetterUtil} using it in return. This is useful for injecting 
     * the {@code SpeciesSubsetterUtil}s to use. 
     */
    private final Function<OWLGraphWrapper, SpeciesSubsetterUtil> subsetterUtilSupplier;

    /**
     * Constructor private, the Uberon and the taxonomy ontologies must be provided.
     * @see #TaxonConstraints(OWLOntology, OWLOntology)
     * @see #TaxonConstraints(String, String)
     */
    @SuppressWarnings("unused")
    private TaxonConstraints() throws UnknownOWLOntologyException, OWLOntologyCreationException {
        this((OWLGraphWrapper) null, (OWLGraphWrapper) null);
    }
    /**
     * Constructor accepting the path to the Uberon ontology and the path to the taxonomy 
     * ontology, allowing to generate or retrieve taxon constraints. 
     * If it is requested to generate constraints, a default {@code SpeciesSubsetterUtil} 
     * will be used (see 
     * {@link #TaxonConstraints(OWLGraphWrapper, OWLGraphWrapper, SpeciesSubsetterUtil)}).
     * 
     * @param uberonFile    A {@code String} that is the path to the Uberon ontology.
     * @param taxOntFile    A {@code String} that is the path to the taxonomy ontology.
     * @throws UnknownOWLOntologyException  If the provided ontologies could not be used.
     * @throws OWLOntologyCreationException If the provided ontologies could not be used.
     * @throws OBOFormatParserException     If the provided ontologies could not be used.
     * @throws IOException                  If the provided files could not be read.
     * @see #TaxonConstraints(OWLGraphWrapper, OWLGraphWrapper)
     */
    public TaxonConstraints(String uberonFile, String taxOntFile) 
            throws UnknownOWLOntologyException, OWLOntologyCreationException, 
            OBOFormatParserException, IOException {
        this(new OWLGraphWrapper(OntologyUtils.loadOntology(uberonFile)), 
                new OWLGraphWrapper(OntologyUtils.loadOntology(taxOntFile)));
    }
    /**
     * Constructor accepting the Uberon {@code OWLGraphWrapper} and the taxonomy 
     * {@code OWLGraphWrapper} allowing to generate or retrieve taxon constraints. 
     * If it is requested to generate constraints, default {@code SpeciesSubsetterUtil} 
     * class will be used (see 
     * {@link #TaxonConstraints(OWLGraphWrapper, OWLGraphWrapper, Function)}).
     * 
     * @param uberonOntGraph    An {@code OWLGraphWrapper} containing the Uberon ontology.
     * @param taxOntGraph       An {@code OWLGraphWrapper} containing the taxonomy ontology. 
     * @throws UnknownOWLOntologyException      if {@code uberonOnt} or {@code taxOnt} 
     *                                          could not be used.
     * @throws OWLOntologyCreationException     if {@code uberonOnt} or {@code taxOnt} 
     *                                          could not be used.
     */
    public TaxonConstraints(OWLGraphWrapper uberonOntGraph, OWLGraphWrapper taxOntGraph) 
            throws UnknownOWLOntologyException, OWLOntologyCreationException {
        this(uberonOntGraph, taxOntGraph, SpeciesSubsetterUtil::new);
    }
    /**
     * Constructor accepting the Uberon {@code OWLGraphWrapper} and the taxonomy 
     * {@code OWLGraphWrapper} allowing to generate or retrieve taxon constraints, 
     * as well as a {@code Function} that will act as a supplier of {@code SpeciesSubsetterUtil}s, 
     * accepting an {@code OWLGraphWrapper} as input. This will be used if it is requested 
     * to generate taxon constraints, to obtain a fresh {@code SpeciesSubsetterUtil} 
     * for each taxon for which constraints must be analyzed. 
     * 
     * @param uberonOntGraph        An {@code OWLGraphWrapper} containing the Uberon ontology.
     * @param taxOntGraph           An {@code OWLGraphWrapper} containing the taxonomy ontology.
     * @param subsetterUtilSupplier A {@code Function} accepting an {@code OWLGraphWrapper} 
     *                              as input and returning a fresh {@code SpeciesSubsetterUtil}, 
     *                              used as supplier of new {@code SpeciesSubsetterUtil}s 
     *                              for each taxon . 
     * @throws UnknownOWLOntologyException      if {@code uberonOnt} or {@code taxOnt} 
     *                                          could not be used.
     * @throws OWLOntologyCreationException     if {@code uberonOnt} or {@code taxOnt} 
     *                                          could not be used.
     */
    public TaxonConstraints(OWLGraphWrapper uberonOntGraph, OWLGraphWrapper taxOntGraph, 
            Function<OWLGraphWrapper, SpeciesSubsetterUtil> subsetterUtilSupplier) 
            throws UnknownOWLOntologyException, OWLOntologyCreationException {
        if (uberonOntGraph == null || taxOntGraph == null) {
            throw log.throwing(new IllegalStateException("You must provide the Uberon " +
                    "ontology and the taxonomy ontology at instantiation."));
        }
        this.uberonOntWrapper = uberonOntGraph;
        this.taxOntWrapper = taxOntGraph;
        this.subsetterUtilSupplier = subsetterUtilSupplier;
        
        this.prepareUberon();
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
        //we also need to merge the import closure, otherwise the classes in the imported ontologies 
        //will be seen by the method #getAllOWLClasses(), but not by the reasoner.
        this.uberonOntWrapper.mergeImportClosure(true);
        
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
     * storing the taxon constraints generated using the Uberon ontology provided at instantiation, 
     * defining in which taxa the Uberon {@code OWLClass}es exist, among the taxa 
     * provided through the TSV file {@code taxonIdFile}. If {@code completeUberonFile} 
     * is not blank, the {@code OWLClass}es which to define taxon constraints for 
     * will be retrieved from this file. This is useful if the Uberon version 
     * containing taxon constraints does not include all possible classes part of Uberon. 
     * In that case, it is recommended to define overriding taxon constraints 
     * for the {@code OWLClass}es not part of the Uberon version provided at instantiation, 
     * otherwise, by default they will be considered as existing in all the requested taxa 
     * (see explanations below to override taxon constraints). 
     * In any case, the {@code OWLClass}es part of the taxonomy ontology provided at instantiation 
     * will not be considered. 
     * <p>
     * When {@code idStartsToOverridingTaxonIds} is not null, it allows to override the constraints 
     * generated: when the OBO-like ID of an Uberon term starts with 
     * one of the key of {@code idStartsToOverridingTaxonIds}, its taxon constraints 
     * are replaced with the taxon IDs defined in the associated value, expanded to also include 
     * all their ancestral taxon IDs. If the start of the OBO-like ID matches 
     * several keys, then the longest match will be considered.
     * <p>
     * To provide the requested taxon IDs through {@code taxonIdFile}, the first line 
     * of this file should be a header line, defining a column to get IDs from, 
     * named exactly "taxon ID" (other columns are optional and will be ignored). 
     * These IDs must correspond to the NCBI IDs (for instance, 9606 for human). 
     * <p>
     * This method also needs to be provided at instantiation with a taxonomy ontology, 
     * that must contain disjoint classes axioms between sibling taxa, as explained in 
     * a Chris Mungall <a href='http://douroucouli.wordpress.com/2012/04/24/taxon-constraints-in-owl/'>
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
     * <p>
     * For some taxa with lots of unsatisfiable classes, there is an issue with the reasoner, 
     * that never ends its work. To avoid that, before generating the constraints for such a taxon, 
     * we can first filter out anatomical entities specific to completely unrelated taxa. 
     * These taxa to be used to simplify the ontology, step by step, are provided 
     * as values of the {@code taxaSimplificationSteps} {@code Map}. If a taxon is absent 
     * from this {@code Map}, or if its associated {@code List} is {@code null} 
     * or empty, then no pre-fitering is requested for this taxon. 
     * 
     * @param taxonIdFile       A {@code String} that is the path to the TSV file 
     *                          containing the IDs from the NCBI website of the taxa 
     *                          to consider (for instance, 9606 for human). The first line 
     *                          should be a header line, defining a column to get IDs from, 
     *                          named exactly "taxon ID" (other columns are optional and 
     *                          will be ignored).
     * @param taxaSimplificationSteps       A {@code Map} where keys are {@code Integer}s that are 
     *                                      the NCBI IDs for which we want to first simplify 
     *                                      the ontology before generating taxon constraints, 
     *                                      the associated value being a {@code List} of 
     *                                      {@code Integer}s that are the NCBI IDs of unrelated 
     *                                      taxa, to be used to progressively simplify the ontology, 
     *                                      in the order in which the simplifications 
     *                                      should be performed.
     * @param completeUberonFile            A {@code String} that is the path to a version 
     *                                      of the Uberon ontology different to the one 
     *                                      provided at instantiation, containing the classes 
     *                                      that should be considered to define taxon constraints 
     *                                      for. If blank, the Uberon version provided 
     *                                      at instantiation is used. 
     * @param idStartsToOverridingTaxonIds  A {@code Map} where keys are {@code String}s 
     *                                      representing prefixes of uberon terms to match, 
     *                                      the associated value being a {@code Set} 
     *                                      of {@code Integer}s to replace taxon constraints 
     *                                      of matching terms.
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
     *                                      be found in the taxonomy ontology provided 
     *                                      at instantiation, or if a taxa in a value 
     *                                      of the {@code taxaSimplifySteps} {@code Map} 
     *                                      is not independent from the associated taxon stored 
     *                                      as key.
     * @throws IOException                  If some files could not be read/written.
     * @throws OBOFormatParserException     If the ontology stored in {@code completeUberonFile} 
     *                                      was in OBO and could not be parsed. 
     * @throws OWLOntologyCreationException If it was not possible to clone the ontology 
     *                                      before modifying it, or to load the ontology stored 
     *                                      in {@code completeUberonFile}.
     * @throws OWLOntologyStorageException  If an error occurred while saving the 
     *                                      intermediate ontologies in owl.
     */
    public void generateTaxonConstraints(String taxonIdFile, 
            Map<Integer, List<Integer>> taxaSimplificationSteps, String completeUberonFile, 
            Map<String, Set<Integer>> idStartsToOverridingTaxonIds, String outputFile, 
            String storeOntologyDir) throws IllegalArgumentException, IOException, 
            OBOFormatParserException, OWLOntologyStorageException, OWLOntologyCreationException {
        log.entry(taxonIdFile, taxaSimplificationSteps, completeUberonFile, idStartsToOverridingTaxonIds, 
                outputFile, storeOntologyDir);
        
        //retrieve all tax IDs in taxonIdFile
        Set<Integer> taxonIds = AnnotationCommon.getTaxonIds(taxonIdFile);
        
        //get the simplification steps associated to requeted taxa. 
        //We first clone taxaSimplificationSteps to avoid changes while streaming, 
        //we use a LinkedHashMap in case the generation order must be predicatable. 
        final Map<Integer, List<Integer>> clonedSteps = 
                taxaSimplificationSteps == null? new LinkedHashMap<>(): 
                    new LinkedHashMap<>(taxaSimplificationSteps);
        //Now, generate a Map associating each taxon in taxonIds to its potential simplification steps.
        //Again, use a LinkedHashMap in case the generation order must be predicatable. 
        Map<Integer, List<Integer>> taxIdsWithSteps = taxonIds.stream()
                .collect(Collectors.toMap(Function.identity(), 
                     e -> clonedSteps.get(e) != null? clonedSteps.get(e): new ArrayList<Integer>(), 
                     (u, v) -> {throw new IllegalStateException("Duplicate key: " + u);}, 
                     LinkedHashMap::new));
        
        //get the OWLClass IDs for which we want the taxon constraints. 
        //By default, we'll get all OWLClasses from the Uberon ontology that are not 
        //in the taxonomy ontology. But if completeUberonFile is provided, it means 
        //that we should retrieve the OWLClasses from this ontology, this is useful 
        //if the ontology with taxon constraints does not contain all OWLClasses 
        //for which we want taxon constraints (in that case, it is important to specify 
        //identifiers with overriding taxon constraints (see idStartsToOverridingTaxonIds))
        OWLGraphWrapper refWrapper = this.uberonOntWrapper;
        if (StringUtils.isNotBlank(completeUberonFile)) {
            refWrapper = new OWLGraphWrapper(OntologyUtils.loadOntology(completeUberonFile));
        }
        Set<String> refClassIds = refWrapper.getAllOWLClasses().stream()
                .filter(e ->!this.taxOntWrapper.getSourceOntology().containsClassInSignature(e.getIRI()))
                .map(refWrapper::getIdentifier).collect(Collectors.toSet());
        
        //launch the generation of taxon constraints and write them to file. 
        Map<String, Set<Integer>> constraints = this.generateTaxonConstraints(
                taxIdsWithSteps, refClassIds, idStartsToOverridingTaxonIds, storeOntologyDir);
        writeToFile(constraints, taxonIds, refWrapper, outputFile);
        
        log.exit();
    }
    
    /**
     * Returns a {@code Map} representing the taxon constraints generated using  
     * the Uberon ontology and taxonomy ontology provided at instantiation, 
     * for the taxa provided through {@code taxonIds}, and for the {@code OWLClass}es 
     * with their OBO-like ID in {@code refClassIds}. The returned {@code Map} 
     * contains these OBO-like IDs as keys, associated to a {@code Set} of {@code Integer}s 
     * that are the NCBI IDs of the taxa in which the {@code OWLClass} exists, 
     * among the provided taxa in {@code taxonIds}. If a {@code Set} value is empty, 
     * then it means that the associated {@code OWLClass} existed in none of the provided taxa.
     * <p> 
     * If an {@code OWLClass} defined in {@code refClassIds} does not exist in the Uberon ontology 
     * provided at instantiation, it will be considered as existing in all the requested taxa. 
     * To change this behavior, it is possible to override taxon constraints for such terms, 
     * see below. 
     * <p>
     * When {@code idStartsToOverridingTaxonIds} is not null, it allows to override the constraints 
     * generated: when the OBO-like ID of an Uberon term starts with 
     * one of the key of {@code idStartsToOverridingTaxonIds}, its taxon constraints 
     * are replaced with the taxon IDs defined in the associated value, expanded to also include 
     * all their ancestral taxon IDs. If the start of the OBO-like ID matches 
     * several keys, then the longest match will be considered.
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
     * <p>
     * For some taxa with lots of unsatisfiable classes, there is an issue with the reasoner, 
     * that never ends its work. To avoid that, before generating the constraints for such a taxon, 
     * we can first filter out anatomical entities specific to completely unrelated taxa. 
     * These taxa to be used to simplify the ontology, step by step, are provided 
     * as values of the {@code taxonIds} {@code Map}. If the {@code List} is {@code null} 
     * or empty, then no pre-fitering is requested for the associated taxon stored as key. 
     * 
     * @param taxonIds          A {@code Map} where keys are {@code Integer}s that are the NCBI IDs 
     *                          of taxa for which we want to generate taxon constraints, 
     *                          the associated value being a {@code List} of {@code Integer}s 
     *                          that are the NCBI IDs of unrelated taxa, to be used 
     *                          to progressively simplify the ontology, in the order 
     *                          in which the simplifications should be performed.
     * @param refClassIds                   A {@code Set} of {@code String}s that are 
     *                                      the OBO-like IDs of the classes that should be considered 
     *                                      to define taxon constraints for. 
     * @param idStartsToOverridingTaxonIds  A {@code Map} where keys are {@code String}s 
     *                                      representing prefixes of uberon terms to match, 
     *                                      the associated value being a {@code Set} 
     *                                      of {@code Integer}s to replace taxon constraints 
     *                                      of matching terms.
     * @param storeOntologyDir  A {@code String} that is the path to a directory 
     *                          where to store intermediate ontologies. If {@code null} 
     *                          the generated ontologies will not be stored.
     * @return                  A {@code Map} where keys are IDs of the {@code OWLClass}es 
     *                          from the provided ontology, and values are  
     *                          {@code Set}s of {@code Integer}s containing the IDs 
     *                          of taxa in which the {@code OWLClass} exists.
     * @throws IllegalArgumentException     If some taxa in {@code taxonIds} could not 
     *                                      be found in the taxonomy ontology provided at instantiation, 
     *                                      or if a taxa in a value of the {@code taxonIds} {@code Map} 
     *                                      is not independent from the associated taxon stored 
     *                                      as key.
     * @throws IOException                  If an error occurred while releasing an 
     *                                      {@code OWLGraphWrapper} used to generate constraints.
     * @throws OWLOntologyCreationException If it was not possible to clone the ontology 
     *                                      before modifying it.
     * @throws OWLOntologyStorageException  If an error occurred while saving the 
     *                                      intermediate ontologies in owl. 
     */
    public Map<String, Set<Integer>> generateTaxonConstraints(Map<Integer, List<Integer>> taxonIds, 
            Set<String> refClassIds, Map<String, Set<Integer>> idStartsToOverridingTaxonIds, 
            String storeOntologyDir) throws IllegalArgumentException, IOException, 
            OWLOntologyCreationException, OWLOntologyStorageException {
        log.entry(taxonIds, refClassIds, idStartsToOverridingTaxonIds, storeOntologyDir);
        log.info("Start generating taxon constraints...");
        
        //if we want to store the intermediate ontologies
        if (storeOntologyDir != null) {
            String outputFilePath = new File(storeOntologyDir, 
                    "uberon_reasoning_source.owl").getPath();
            new OntologyUtils(this.uberonOntWrapper).saveAsOWL(outputFilePath);
        }
        
        //taxonConstraints will store the association between an Uberon term, 
        //and the taxa it exists in. 
        Map<String, Set<Integer>> taxonConstraints = new HashMap<String, Set<Integer>>();
        
        //now, generate the constraints one taxon at a time.
        for (Entry<Integer, List<Integer>> taxonEntry: taxonIds.entrySet()) {
            int taxonId = taxonEntry.getKey();
            
            Set<OWLClass> classesDefined = this.getExistingOWLClasses(
                    taxonId, taxonEntry.getValue(), storeOntologyDir);
            
            //store results in taxonConstraints
            for (OWLClass classDefined: classesDefined) {
                String classId = this.uberonOntWrapper.getIdentifier(classDefined);
                log.trace("Defining existence of {} in taxon {}", classId, taxonId);
                Set<Integer> existsInTaxa = taxonConstraints.get(classId);
                if (existsInTaxa == null) {
                    existsInTaxa = new HashSet<Integer>();
                    taxonConstraints.put(classId, existsInTaxa);
                }
                existsInTaxa.add(taxonId);
            }
        }
        
        
        //Now, replace/add taxon constraints when needed. 
        
        //we first expand all overriding taxa to also include their ancestors
        Map<String, Set<Integer>> idsToExpandedTaxIds = expandOverridingTaxonIds(
                this.taxOntWrapper, idStartsToOverridingTaxonIds);
        //then we get the IDs of all OWLClasses existing in the Uberon ontology 
        //used to generate taxon constraints. 
        Set<String> existingClassIds = this.uberonOntWrapper.getAllOWLClasses().stream()
                .filter(e ->!this.taxOntWrapper.getSourceOntology().containsClassInSignature(e.getIRI()))
                .map(this.uberonOntWrapper::getIdentifier).collect(Collectors.toSet());
        log.trace("Existing OWLClasses in source Uberon ontology: {}", existingClassIds);
        
        //OK, let's check each requested OWLClass
        for (String refClassId: refClassIds) {
            
            //first, check whether it was requested to override the taxon constraints 
            //for this class
            Set<Integer> overridingConstraints = getOverridingTaxonIds(refClassId, 
                    idsToExpandedTaxIds);
            if (overridingConstraints != null) {
                //we replace taxon constraints of this class with the provided taxa, 
                //that are part of the requested taxa in taxonIds
                Set<Integer> replacement = overridingConstraints.stream()
                        .filter(taxonIds.keySet()::contains).collect(Collectors.toSet());
                log.trace("Overriding taxon constraints for class {} with constraints {}", 
                        refClassId, replacement);
                taxonConstraints.put(refClassId, replacement);
            } 
            //now, if the requested class did not exist in the Uberon ontology used 
            //to generate constraints, then we consider that this class exist in all requested taxa
            else if (!existingClassIds.contains(refClassId)) {
                log.warn("The class {} was not present in the Uberon ontology used to compute "
                        + "taxon constraints, and no override of taxon constraints was requested for it, "
                        + "it will be considered as valid in all the requested taxa", refClassId);
                taxonConstraints.put(refClassId, new HashSet<Integer>(taxonIds.keySet()));
            } 
            //otherwise, we simply use the taxon constraints computed, as no override is requested, 
            //and as the class is present in the Uberon ontology used. 
            //If it is not present in taxonConstraints, it means it exists in none 
            //of the requested taxa, but we need to add it to the Map anyway.
            else if (!taxonConstraints.keySet().contains(refClassId)) {
                log.trace("OWLClass existing in none of the requested taxa, stored anyway: {}", 
                        refClassId);
                taxonConstraints.put(refClassId, new HashSet<Integer>());
            }
        }
        //Now, remove all constraints related to a class not in refClassIds
        Map<String, Set<Integer>> filteredConstraints = taxonConstraints.entrySet().stream()
                .filter(e -> {
                    if (!refClassIds.contains(e.getKey())) {
                        log.trace("Discarding non-requested class {}", e.getKey());
                        return false;
                    }
                    return true;
                }).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        
        log.info("Done generating taxon constraints.");
        return log.exit(filteredConstraints);
    }
    
    /**
     * Returns the {@code OWLClass}es existing in the requested taxon. This methods 
     * returns the {@code OWLClass}es, present in the Uberon ontology provided at instantiation, 
     * and that actually exists in the taxon with ID {@code taxonId}, using the taxonomy ontology 
     * provided at instantiation. {@code taxonId} must corresponds to a taxon present in this 
     * taxonomy ontology, otherwise, an {@code IllegalArgumentException} is thrown.
     * <p>
     * This class needs to be provided at instantiation with an Uberon version containing 
     * taxon constraints, and with a taxonomy ontology containing disjoint classes axioms 
     * between sibling taxa, as explained in a Chris Mungall 
     * <a href='http://douroucouli.wordpress.com/2012/04/24/taxon-constraints-in-owl/'>
     * blog post</a>, see also {@link org.bgee.pipeline.species.GenerateTaxonOntology}.
     * <p>
     * For some taxa with lots of unsatisfiable classes, there is an issue with the reasoner, 
     * that never ends its work. To avoid that, before generating the constraints for such a taxon, 
     * we can first filter out anatomical entities specific to unrelated taxa. 
     * These taxa to be used to simplify the ontology, step by step, are provided 
     * through {@code intermediateTaxonIds}. If the {@code List} is {@code null} 
     * or empty, then no pre-fitering is requested for {@code taxonId}. 
     * <p>
     * If {@code storeOntologyDir} is not {@code null}, then the intermediate ontology, 
     * corresponding to the version of the ontology used as the source of reasoning, 
     * filtered for the requested taxon, will be saved in that directory.
     * 
     * @param taxonId               An {@code int} that is the NCBI ID of the taxon for which 
     *                              we want to retrieve {@code OWLClass}es exiting in it.
     * @param intermediateTaxonIds  A {@code List} of {@code Integer}s that are the NCBI IDs 
     *                              of taxa unrelated to {@code taxonId}, to be used 
     *                              to progressively simplify the ontology, in the order 
     *                              in which the simplifications should be performed. 
     *                              If {@code null} or empty, then no pre-filtering is requested. 
     * @param storeOntologyDir      A {@code String} that is the path to a directory 
     *                              where to store the generated ontology. If {@code null}, 
     *                              the generated ontology will not be stored.
     * @return                      A {@code Set} containing the {@code OWLClass}es 
     *                              existing in the taxon with ID {@code taxonId}.
     * @throws IllegalArgumentException     If some taxa could not be found 
     *                                      in the taxonomy ontology provided at instantiation, 
     *                                      or if some taxa in {@code intermediateTaxonIds} 
     *                                      are not independent from {@code taxonId}.
     * @throws IOException                  If an error occurred while loading an ontology.
     * @throws OWLOntologyCreationException If an error occurred while loading an ontology.
     * @throws OWLOntologyStorageException  If an error occurred while saving an ontology.
     * @throws UnknownOWLOntologyException  If an error occurred while loading an ontology.
     */
    private Set<OWLClass> getExistingOWLClasses(int taxonId, List<Integer> intermediateTaxonIds, 
            String storeOntologyDir) throws IllegalArgumentException, IOException, 
            OWLOntologyCreationException, OWLOntologyStorageException, UnknownOWLOntologyException {
        log.entry(taxonId, intermediateTaxonIds, storeOntologyDir);
        
        //for each taxon, we clone our Uberon ontology merged with our taxonomy ontology, 
        //because the method getExistingOWLClasses will modified it.
        //we use a new OWLOntologyManager to be sure there is no memory leack.
        OWLOntology clonedUberon = OWLManager.createOWLOntologyManager().createOntology(
            IRI.create("Uberon_for_" + taxonId), 
            new HashSet<OWLOntology>(Arrays.asList(
                    this.uberonOntWrapper.getSourceOntology())));
        try (OWLGraphWrapper graph = new OWLGraphWrapper(clonedUberon)) {
            
            //Get the OWLClass corresponding to the requested taxon
            OWLClass taxClass = graph.getOWLClassByIdentifier(
                    OntologyUtils.getTaxOntologyId(taxonId), true);
            if (taxClass == null || 
                    graph.isObsolete(taxClass) || graph.getIsObsolete(taxClass)) {
                throw log.throwing(new IllegalArgumentException("A taxon ID " +
                        "provided could not be found or was deprecated in " +
                        "the provided ontology: " + taxonId));
            }
            
            //For some taxa, there are so many unsatisfiable classes that the reasoner never ends.
            //To avoid that, we first remove classes absolutely not related to the targeted taxon,
            //in several steps, to make easy the work of the reasoner. 
            if (intermediateTaxonIds != null && !intermediateTaxonIds.isEmpty()) {
                //verify independence of the intermediate taxa relative to the key taxon
                Set<OWLObject> checkClasses = new HashSet<OWLObject>();
                checkClasses.addAll(graph.getAncestorsThroughIsA(taxClass));
                checkClasses.addAll(graph.getDescendantsThroughIsA(taxClass));
                checkClasses.add(taxClass);
                
                for (int intermediateTaxonId: intermediateTaxonIds) {
                    OWLClass intermediateTaxClass = graph.getOWLClassByIdentifier(
                            OntologyUtils.getTaxOntologyId(intermediateTaxonId), true);
                    if (intermediateTaxClass == null || 
                            graph.isObsolete(intermediateTaxClass) || 
                            graph.getIsObsolete(intermediateTaxClass)) {
                        throw log.throwing(new IllegalArgumentException("A taxon ID " +
                                "provided could not be found or was deprecated in " +
                                "the provided ontology: " + intermediateTaxonId));
                    }
                    if (checkClasses.contains(intermediateTaxClass)) {
                        throw log.throwing(new IllegalArgumentException("The Taxon ID "
                                + intermediateTaxonId + " provided to first perform a simplification "
                                + "is not independent from the main taxon to examine " + taxonId));
                    }
                    
                    //we do not care about the classes existing in this intermediate ontology, 
                    //but the getExistingOWLClasses method will filter the classes 
                    //from the ontology all the same... we do not request to store this ontology.
                    this.getExistingOWLClasses(graph, intermediateTaxClass, null, false);
                }
            }
            
            //Use the OWLGraphWrapper that was potentially already filtered for structures 
            //specific to completely unrelated taxa. 
            return log.exit(this.getExistingOWLClasses(graph, taxClass, storeOntologyDir, true));
        }
    }
    
    /**
     * Returns a {@code Set} of {@code OWLClass}es obtained from the ontology 
     * wrapped into {@code ontWrapper}, and that actually exists in the taxon corresponding to  
     * {@code taxClass} if {@code removeOtherTaxa} is {@code true}, or that are all the classes 
     * not specific to {@code taxClass} if {@code removeOtherTaxa} is {@code false}. 
     * If {@code storeOntologyDir} is not {@code null}, then the intermediate ontology, 
     * corresponding to the filtered version of the source ontology 
     * for the provided taxon, will be saved in that directory.
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
     * @param taxClass          An {@code OWLClass} corresponding to the taxon to consider.
     * @param storeOntologyDir  A {@code String} that is the path to a directory 
     *                          where to store intermediate ontologies. If {@code null} 
     *                          the generated ontologies will not be stored.
     * @param removeOtherTaxa   A {@code boolean} defining whether classes not existing 
     *                          in {@code taxClass} should be removed, or classes specific to 
     *                          {@code taxClass}.
     * @return                  A {@code Set} containing the {@code OWLClass}es 
     *                          existing in the taxon {@code taxClass}.
     * @throws UnknownOWLOntologyException  If the ontology stored in 
     *                                      {@code uberonFile} could not be used.
     * @throws OWLOntologyStorageException  If an error occurred while saving the 
     *                                      intermediate ontology in owl.
     * @throws OBOFormatParserException     If {@code uberonFile} could not be parsed. 
     * @throws IOException                  If {@code uberonFile} could not be opened. 
     */
    private Set<OWLClass> getExistingOWLClasses(OWLGraphWrapper ontWrapper, OWLClass taxClass, 
            String storeOntologyDir, boolean removeOtherTaxa) throws UnknownOWLOntologyException, 
            IllegalArgumentException, OWLOntologyStorageException  {
        log.entry(ontWrapper, taxClass, storeOntologyDir, removeOtherTaxa);
        log.info("Examining ontology for taxon {} - removeOtherTaxa: {}...", taxClass, removeOtherTaxa);
        log.debug("Before reasoning - Total memory: {} Go - Memory free: {} Go - Memory used: {} Go", 
                Runtime.getRuntime().totalMemory()/(1024*1024*1024), 
                Runtime.getRuntime().freeMemory()/(1024*1024*1024), 
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/(1024*1024*1024));
        
        
        //filter ontology
        SpeciesSubsetterUtil subSetter = this.subsetterUtilSupplier.apply(ontWrapper);
        subSetter.taxClass = taxClass;
        subSetter.reasoner = this.createReasoner(ontWrapper.getSourceOntology());
        if (removeOtherTaxa) {
            subSetter.removeOtherSpecies();
        } else {
            subSetter.removeSpecies();
        }
        log.debug("After reasoning before dispose - Total memory: {} Go - Memory free: {} Go - Memory used: {} Go", 
                Runtime.getRuntime().totalMemory()/(1024*1024*1024), 
                Runtime.getRuntime().freeMemory()/(1024*1024*1024), 
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/(1024*1024*1024));
        subSetter.reasoner.dispose();
        log.debug("After reasoning after dispose - Total memory: {} Go - Memory free: {} Go - Memory used: {} Go", 
                Runtime.getRuntime().totalMemory()/(1024*1024*1024), 
                Runtime.getRuntime().freeMemory()/(1024*1024*1024), 
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/(1024*1024*1024));
        
        //if we want to store the intermediate ontology
        if (storeOntologyDir != null) {
            ontWrapper.clearCachedEdges();
            String outputFilePath = new File(storeOntologyDir, 
                    "uberon_subset" 
                    + OntologyUtils.getTaxNcbiId(ontWrapper.getIdentifier(taxClass)) + ".owl")
                .getPath();
            new OntologyUtils(ontWrapper).saveAsOWL(outputFilePath);
        }

        log.info("Done examining ontology for taxon {} - removeOtherTaxa: {}.", taxClass, removeOtherTaxa);
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
     * @param refUberonWrapper  An {@code OWLGraphWrapper} wrapping the Uberon ontology used 
     *                          to retrieve labels of {@code OWLClass}es 
     *                          defined in {@code taxonConstraints}.
     * @param outputFile        A {@code String} that is the path to the output file 
     *                          were constraints will be written as TSV.
     * @throws IOException      If an error occurred while trying to write to 
     *                          {@code outputFile}.
     */
    private static void writeToFile(Map<String, Set<Integer>> taxonConstraints, Set<Integer> taxonIds, 
            OWLGraphWrapper refUberonWrapper, String outputFile) throws IOException {
        log.entry(taxonConstraints, taxonIds, refUberonWrapper, outputFile);

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
                OWLClass cls = refUberonWrapper.getOWLClassByIdentifier(uberonId, true);
                String label = "-";
                if (cls != null) {
                    label = refUberonWrapper.getLabelOrDisplayId(cls);
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
                    OntologyUtils.getTaxOntologyId(taxonId), true) == null) {
                throw log.throwing(new IllegalArgumentException("The requested taxon " + 
                    taxonId + " does not exist in the provided taxonomy ontology."));
            }
        }

        SpeciesSubsetterUtil util = this.subsetterUtilSupplier.apply(this.uberonOntWrapper);
        return log.exit(util.explainTaxonConstraint(owlClassIds, 
                OntologyUtils.convertToTaxOntologyIds(taxonIds)));
    }

    /**
     * Expand the providing overriding taxon constraints to also include all parent taxa 
     * of the taxa stored as values in {@code idStartsToOverridingTaxonIds}. This method returns 
     * a {@code Map} identical to {@code idStartsToOverridingTaxonIds}, except that 
     * each {@code Set} stored as value is modified to also include the ancestor IDs 
     * of the taxa in the {@code Set}, retrieved using the provided taxonomy ontology. 
     * 
     * @param taxOntWrapper                 An {@code OWLGraphWrapper} storing the taxonomy ontology 
     *                                      used to retrieve ancestors of taxa.
     * @param idStartsToOverridingTaxonIds  A {@code Map} where keys are {@code String}s 
     *                                      representing prefixes of uberon terms to match, 
     *                                      the associated value being a {@code Set} 
     *                                      of {@code Integer}s to replace taxon constraints 
     *                                      of matching terms.
     * @return                              A {@code Map} identical to {@code idStartsToOverridingTaxonIds}, 
     *                                      except that it is expanded to also include parent taxa. 
     * @throws IllegalArgumentException If one of the provided overriding taxa is not present 
     *                                  in the taxonomy ontology.
     */
    private static Map<String, Set<Integer>> expandOverridingTaxonIds(OWLGraphWrapper taxOntWrapper, 
            Map<String, Set<Integer>> idStartsToOverridingTaxonIds) throws IllegalArgumentException {
        log.entry(taxOntWrapper, idStartsToOverridingTaxonIds);
        
        //retrieve ancestors of all overriding taxa. 
        Map<Integer, Set<Integer>> taxonIdToAncestorIds = new HashMap<>();
        //Collect all tax IDs in all overridden constraints, and iterate them one by one, 
        //to retrieve their ancestors
        for (int taxId: idStartsToOverridingTaxonIds.values().stream()
                .flatMap(Set::stream).collect(Collectors.toSet())) {
            
            OWLClass taxCls = taxOntWrapper.getOWLClassByIdentifier(
                    OntologyUtils.getTaxOntologyId(taxId), true);
            if (taxCls == null) {
                throw log.throwing(new IllegalArgumentException("A taxon provided "
                        + "in overriding constraints is not present in the taxonomy ontology: "
                        + taxId));
            }
            Set<Integer> ancestorIds = taxOntWrapper.getAncestorsThroughIsA(taxCls).stream()
                    .map(e -> OntologyUtils.getTaxNcbiId(taxOntWrapper.getIdentifier(e)))
                    .collect(Collectors.toSet());
            taxonIdToAncestorIds.put(taxId, ancestorIds);
        }
        
        //now, re replace the overriding taxa to also include their ancestors
        Map<String, Set<Integer>> expanded = idStartsToOverridingTaxonIds.entrySet().stream()
            .collect(Collectors.toMap(Entry::getKey, 
                    e -> {
                        Set<Integer> expandedOverridingTaxIds = new HashSet<Integer>();
                        for (int overridingTaxId: e.getValue()) {
                            expandedOverridingTaxIds.add(overridingTaxId);
                            Set<Integer> ancestorIds = taxonIdToAncestorIds.get(overridingTaxId);
                            if (ancestorIds != null) {
                                expandedOverridingTaxIds.addAll(ancestorIds);
                            }
                        }
                        log.trace("For class {}, original constraints: {} - expanded constraints: {}", 
                                e.getKey(), e.getValue(), expandedOverridingTaxIds);
                        return expandedOverridingTaxIds;
                    }
            ));
        
        return log.exit(expanded);
    }
    /**
     * Return the taxon IDs to override the taxon constraints for the OWLClass with ID {@code clsId}.
     * This method tries to find the longest matching prefix of {@code clsId} in the keys 
     * of {@code idStartsToOverridingTaxonIds}, and returns the associated value. 
     * For instance, if {@clsId} is "EV:00001" and {@code idStartsToOverridingTaxonIds} 
     * contains some entries {@code "EV:" -> {9605}, "EV:00001" -> {9606}}, this methods 
     * will return {@code {9606}}. If there is no match found, this method will return {@code null}.
     * 
     * @param clsId                         A {@code String} that is the ID of a term to find 
     *                                      overriding taxon constraints for. 
     * @param idStartsToOverridingTaxonIds  A {@code Map} where keys are {@code String}s 
     *                                      representing prefixes of uberon terms to match, 
     *                                      the associated value being a {@code Set} 
     *                                      of {@code Integer}s to replace taxon constraints 
     *                                      of matching terms.
     * @return                              A {@code Set} of {@code Integer}s that are the IDs 
     *                                      of taxa to override taxon constraints for class 
     *                                      with ID {@code clsId}, {@code null} if there was 
     *                                      no overriding constraints for {@code clsId}.
     */
    private static Set<Integer> getOverridingTaxonIds(String clsId, 
            Map<String, Set<Integer>> idStartsToOverridingTaxonIds) {
        log.entry(clsId, idStartsToOverridingTaxonIds);
        if (idStartsToOverridingTaxonIds == null) {
            return log.exit(null);
        }
        
        Set<Integer> replacementConstraints = null;
        String matchingPrefix = "";
        //iterate all overriding constraint to find the longest match possible with clsId
        for (Entry<String, Set<Integer>> idStartToTaxonIds: idStartsToOverridingTaxonIds.entrySet()) {
            
            if (clsId.startsWith(idStartToTaxonIds.getKey()) && 
                    idStartToTaxonIds.getKey().length() > matchingPrefix.length()) {
                matchingPrefix = idStartToTaxonIds.getKey();
                log.trace("Uberon ID {} matching prefix {}, taxon constraints overriden: {}", 
                        clsId, matchingPrefix, idStartToTaxonIds.getValue());
                replacementConstraints = 
                        new HashSet<Integer>(idStartToTaxonIds.getValue());
                //continue iterations anyway in case there is a longest match
            }
        }
        
        return log.exit(replacementConstraints);
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
     * Extract from the taxon constraints the taxon IDs used. 
     * 
     * @param taxonConstraints          A {@code Map} where keys are IDs of the Uberon 
     *                                  {@code OWLClass}es, and values are {@code Set}s 
     *                                  of {@code Integer}s containing the IDs of taxa 
     *                                  in which the {@code OWLClass} exists.
     * @return                          A {@code Set} of {@code Integer}s representing 
     *                                  the NCBI IDs (e.g., 9606) of the taxa present 
     *                                  in {@code taxonConstraints}
     * @throws IllegalArgumentException If {@code taxonConstraints} does not allow 
     *                                  to retrieve any taxon ID.
     */
    public static Set<Integer> extractTaxonIds(Map<String, Set<Integer>> taxonConstraints) {
        log.entry(taxonConstraints);
        
        Set<Integer> taxIds = new HashSet<Integer>();
        for (Set<Integer> iterateTaxIds: taxonConstraints.values()) {
            taxIds.addAll(iterateTaxIds);
        }
        
        if (taxIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("The taxon constraints provided "
                    + "did not allow to retrieve taxon IDs"));
        }
        return log.exit(taxIds);
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
     * When {@code idStartsToOverridingTaxonIds} is not null, it allows to override constraints 
     * retrieved from the file: when the OBO-like ID of an Uberon term starts with 
     * one of the key of {@code idStartsToOverridingTaxonIds}, its taxon constraints 
     * are replaced with the associated value. If the start of the OBO-like ID matches 
     * several keys, then the longest match will be considered. 
     * 
     * @param taxonConstraintsFile          A {@code String} that is the path to the 
     *                                      taxon constraints file.
     * @param idStartsToOverridingTaxonIds   A {@code Map} where keys are {@code String}s 
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
    //XXX: should we keep this method, now that taxon constraints are directly produced 
    //with overriding criteria? Maybe no other classes shoud now be allowed to override 
    //constraints. Or do we consider that it is still a useful feature, to be able 
    //to test different constraints without formally regenerating them all?
    public static Map<String, Set<Integer>> extractTaxonConstraints(String taxonConstraintsFile, 
            Map<String, Set<Integer>> idStartsToOverridingTaxonIds) 
                throws FileNotFoundException, IOException {
        log.entry(taxonConstraintsFile, idStartsToOverridingTaxonIds);
        
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
                Set<Integer> replacementConstraints = getOverridingTaxonIds(uberonId, 
                        idStartsToOverridingTaxonIds);
                
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
