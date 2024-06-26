package org.bgee.pipeline.uberon;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.species.Taxon;
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
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;
import org.semanticweb.owlapi.model.parameters.ChangeApplied;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.supercsv.cellprocessor.FmtBool;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseBool;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.io.ICsvListReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.io.ICsvMapWriter;

import owltools.graph.OWLGraphEdge;
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
     * NOTE: we now use HermiT which does not allow this configuration.
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
     * A {@code String} that is the name of the column containing Taxon IDs
     * with constraints, in the taxon constraints file.
     */
    public final static String WITH_CONSTRAINTS_ID = "Only in Taxon IDs";
    
    /**
     * A {@code String} that is the name of the column containing Taxon Names
     * with constraints, in the taxon constraints file.
     */
    public final static String WITH_CONSTRAINTS_NAME = "Only in Taxon Names";
    
    /**
     * A {@code String} that is the name of the column containing Taxon IDs
     * without constraints, in the taxon constraints file.
     */
    public final static String WITHOUT_CONSTRAINTS_ID = "Never in Taxon IDs";
    
    /**
     * A {@code String} that is the name of the column containing Taxon Names
     * without constraints, in the taxon constraints file.
     */
    public final static String WITHOUT_CONSTRAINTS_NAME = "Never in Taxon Names";
    
    /**
     * A {@code String} that is the name of the column containing comments, 
     * in the taxon constraints file.
     */
    public final static String COMMENTS = "Comments";
    
    
    
    /**
     * Several actions can be launched from this main method, depending on the first 
     * element in {@code args}: 
     * <ul>
     * <li>If the first element in {@code args} is "generateTaxonConstraints" or
     * "generateTaxonConstraintsFromMergedOntology", it will launch the generation of a TSV files,
     * allowing to know, for each {@code OWLClass} in the Uberon ontology, in which taxa it exits,
     * among the taxa provided through another TSV file, containing their NCBI IDs.
     * See {@link #generateTaxonConstraints(String, Map, String, Map, String, String)} 
     * for more details. Following elements in {@code args} must then be:
     *   <ol>
     *   <li>path to the source Uberon OWL ontology file. This Uberon ontology must 
     *   contain the taxon constraints ("in taxon" and "never_in_taxon" relations, 
     *   not all Uberon versions contain them). If the first element in {@code args} is
     *   "generateTaxonConstraintsFromMergedOntology", it must also contains our version of
     *   the taxonomy ontology with proper disjoint classes axioms between sibling taxa.
     *   <li>If the first element in {@code args} is "generateTaxonConstraints",
     *   path to the NCBI taxonomy ontology. This taxonomy must contain disjoint
     *   classes axioms between sibling taxa, as explained in a Chris Mungall 
     *   <a href='http://douroucouli.wordpress.com/2012/04/24/taxon-constraints-in-owl/'>
     *   blog post</a>, see also {@link org.bgee.pipeline.species.GenerateTaxonOntology}.
     *   If the first element in {@code args} is "generateTaxonConstraintsFromMergedOntology",
     *   this argument must be empty (see {@link CommandRunner#EMPTY_ARG}).
     *   <li>path to the TSV files containing the IDs of the taxa for which we want 
     *   to generate the taxon constraints, corresponding to the NCBI ID (e.g., 9606 
     *   for human). The first line should be a header line, defining a column to get 
     *   IDs from, named exactly "taxon ID" (other columns are optional and will be ignored).
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
     *   <li>path to a version of the Uberon ontology different to the one 
     *   containing taxon constraints. The taxon constraints will be produced 
     *   for the classes present in this ontology. If equal to {@link CommandRunner#EMPTY_ARG}, 
     *   then the Uberon ontology containing taxon constraints will be used.
     *   <li>path to the generated TSV file, output of the method.
     *   <li>OPTIONNAL: a path to a directory where to store the intermediate generated 
     *   ontologies. If this parameter is provided, an ontology will be generated 
     *   for each taxon, and stored in this directory, containing only 
     *   the {@code OWLClass}es existing in this taxon. If not provided, the intermediate 
     *   ontologies will not be stored. 
     *   </ol>
     * <li>If the first element in {@code args} is "generateCuratedTaxonConstraints" or
     * "generateCuratedTaxonConstraintsFromMergedOntology", the action will be to generate
     * a tsv file allowing to know for each {@code OWLClass} in the Uberon ontology,
     * in which species it exists, among the species provided through another TSV file,
     * containing their NCBI IDs. The generation of this file takes as input two files containing
     * taxon constraints information. One file with taxon constraints automatically generated from the
     * uberon ontology and one file corresponding to manual curation of these automatically
     * generated taxon constraints.
     * See {@link #generateCuratedTaxonConstraints(String, String, String, String)}.
     * Following elements in {@code args} must then be:
     *   <ol>
     *   <li>path to the source Uberon OWL ontology file. This Uberon ontology must 
     *   contain the taxon constraints ("in taxon" and "only_in_taxon" relations, 
     *   not all Uberon versions contain them). If the first element in {@code args} is
     *   "explainCuratedTaxonConstraintsFromMergedOntology", it must also contains our version of
     *   the taxonomy ontology. It is not mandatory for this taxonomy to include
     *   disjoint classes axioms between sibling taxa.
     *   <li>If the first element in {@code args} is "explainCuratedTaxonConstraints",
     *   path to the NCBI taxonomy ontology. It is not mandatory for this taxonomy
     *   to include disjoint classes axioms between sibling taxa. If the first element in {@code args}
     *   is "explainCuratedTaxonConstraintsFromMergedOntology", this argument must be empty
     *   (see {@link CommandRunner#EMPTY_ARG}).
     *   <li> path to the taxon constraints tsv file automatically generated using the
     *   uberon ontology. See {@link generateTaxonConstraints(String,  Map, String, 
     *   Map, String, String)} for more details.
     *   <li> path to the curated taxon constraints tsv file manually created by Bgee curators.
     *   This file contains same columns than the automatically generated one and each 
     *   row correspond to a modification to the automatically generated file.
     *   <li>path to the TSV files containing the IDs of the species for which we want 
     *   to generate the taxon constraints, corresponding to the NCBI ID (e.g., 9606 
     *   for human). The first line should be a header line, defining a column to get 
     *   IDs from, named exactly "taxon ID" (other columns are optional and will be ignored).
     *   <li>path to the generated TSV file, output of the method.
     *   </ol>
     * <li>If the first element in {@code args} is "explainTaxonConstraints" or
     * "explainTaxonConstraintsFromMergedOntology", the action will be to display the explanation
     * for the existence or absence of existence of a Uberon term in a given taxon. See {@link
     * #explainTaxonExistence(Collection, Collection)} for more details. 
     * The explanation will be displayed using the logger of this class with an 
     * {@code info} level.
     * Following elements in {@code args} must then be:
     *   <ol>
     *   <li>path to the source Uberon OWL ontology file. This Uberon ontology must 
     *   contain the taxon constraints ("in taxon" and "only_in_taxon" relations, 
     *   not all Uberon versions contain them). If the first element in {@code args} is
     *   "explainTaxonConstraintsFromMergedOntology", it must also contains our version of
     *   the taxonomy ontology. It is not mandatory for this taxonomy to include
     *   disjoint classes axioms between sibling taxa.
     *   <li>If the first element in {@code args} is "explainTaxonConstraints",
     *   path to the NCBI taxonomy ontology. It is not mandatory for this taxonomy
     *   to include disjoint classes axioms between sibling taxa. If the first element in {@code args}
     *   is "explainTaxonConstraintsFromMergedOntology", this argument must be empty
     *   (see {@link CommandRunner#EMPTY_ARG}).
     *   <li>OBO-like ID of the Uberon term for which we want an explanation
     *   <li>NCBI ID (for instance, 9606) of the taxon in which we want to explain 
     *   existence or nonexistence of the requested Uberon term.
     *   </ol>
     * <li>If the first element in {@code args} is "mergeUberonAndTaxonomy", the action will be
     * to save an ontology merging the provided Uberon and taxonomy ontologies.
     * Uberon will be cleaned from redundant information present in the taxonomy not to have conflicts,
     * and the two ontologies will be merged, and saved into a OWL file and an OBO file.
     * Following elements in {@code args} must then be:
     *   <ol>
     *   <li>path to the source Uberon OWL ontology file.
     *   <li>path to the taxonomy ontology.
     *   <li>path to output file, without specifying ".owl" nor ".obo" at the end
     *   (the two formats will be automatically saved and the proper extensions added to the path)
     *   </ol>
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
        log.traceEntry("{}", (Object[]) args);

        //The path to the taxonomy ontology is for all calls the third argument provided
        String taxPath = CommandRunner.parseArgument(args[2]);
        TaxonConstraints taxonConstraints;
        if (taxPath != null) {
            if (args[0].equalsIgnoreCase("explainTaxonConstraintsFromMergedOntology") ||
                    args[0].equalsIgnoreCase("generateTaxonConstraintsFromMergedOntology") ||
                    args[0].equalsIgnoreCase("generateCuratedTaxonConstraintsFromMergedOntology") ||
                    args[0].equalsIgnoreCase("propagateCuratedTaxonConstraintsFromMergedOntology") ||
                    args[0].equalsIgnoreCase("convertSpeciesConstraintsFileToLCAConstraintsFileFromMergedOntology")) {
                throw log.throwing(new IllegalArgumentException("According to the arguments provided, "
                        + "the taxonomy ontology should already have been merged within Uberon"));
            }
            //The path to Uberon is for all calls the second argument provided
            taxonConstraints = new TaxonConstraints(args[1], taxPath);
        } else {
            if (args[0].equalsIgnoreCase("explainTaxonConstraints") ||
                    args[0].equalsIgnoreCase("generateTaxonConstraints") ||
                    args[0].equalsIgnoreCase("generateCuratedTaxonConstraints") ||
                    args[0].equalsIgnoreCase("mergeUberonAndTaxonomy") ||
                    args[0].equalsIgnoreCase("propagateCuratedTaxonConstraints") ||
                    args[0].equalsIgnoreCase("convertSpeciesConstraintsFileToLCAConstraintsFile")) {
                throw log.throwing(new IllegalArgumentException("The taxonomy ontology must be provided"));
            }
            //The path to Uberon is for all calls the second argument provided
            taxonConstraints = new TaxonConstraints(args[1]);
        }

        if (args[0].equalsIgnoreCase("explainTaxonConstraints") ||
                args[0].equalsIgnoreCase("explainTaxonConstraintsFromMergedOntology")) {
            if (args.length != 5) {
                throw log.throwing(new IllegalArgumentException(
                        "Incorrect number of arguments provided, expected " + 
                        "5 arguments, " + args.length + " provided."));
            }
            
            String clsId = args[3];
            String taxId = args[4];
            taxonConstraints.explainAndPrintTaxonExistence(
                            Arrays.asList(clsId), 
                            Arrays.asList(Integer.parseInt(taxId)), 
                            System.out::println);
            
        } else if (args[0].equalsIgnoreCase("generateTaxonConstraints") ||
                args[0].equalsIgnoreCase("generateTaxonConstraintsFromMergedOntology")) {
        
            if (args.length < 7 || args.length > 8) {
                throw log.throwing(new IllegalArgumentException("Incorrect number of arguments " +
                        "provided, expected 7 to 8 arguments, " + args.length + 
                        " provided."));
            }
            
            String storeDir = null;
            if (args.length == 8) {
                storeDir = args[7];
            }
            taxonConstraints.generateTaxonConstraints(args[3],
                    CommandRunner.parseMapArgumentAsAllInteger(args[4]), 
                    CommandRunner.parseArgument(args[5]),
                    args[6], storeDir);
        } else if (args[0].equalsIgnoreCase("generateCuratedTaxonConstraints") ||
                args[0].equalsIgnoreCase("generateCuratedTaxonConstraintsFromMergedOntology")) {
            
            if (args.length != 7) {
                throw log.throwing(new IllegalArgumentException("Incorrect number of arguments " +
                        "provided, expected 7 arguments, " + args.length +
                        " provided."));
            }
            String generatedTaxonConstraintsFile = args[3];
            String curatedTaxonConstraintsFile= args[4];
            String speciesFile = args[5];
            String outputFile = args[6];
            taxonConstraints.generateCuratedTaxonConstraints(generatedTaxonConstraintsFile,
                    curatedTaxonConstraintsFile, speciesFile, outputFile);
        } else if (args[0].equalsIgnoreCase("mergeUberonAndTaxonomy")) {
        
            if (args.length != 4) {
                throw log.throwing(new IllegalArgumentException("Incorrect number of arguments " +
                        "provided, expected 4 arguments, " + args.length + 
                        " provided."));
            }
            taxonConstraints.saveUberonToFile(args[3]);
            
        } else if (args[0].equalsIgnoreCase("propagateCuratedTaxonConstraints") ||
                args[0].equalsIgnoreCase("propagateCuratedTaxonConstraintsFromMergedOntology")) {

            if (args.length != 7) {
                throw log.throwing(new IllegalArgumentException("Incorrect number of arguments " +
                        "provided, expected 7 arguments, " + args.length +
                        " provided."));
            }
            String generatedTaxonConstraintsFile = args[3];
            String curatedTaxonConstraintsFile= args[4];
            String speciesFile = args[5];
            String outputFile = args[6];
            taxonConstraints.propagateCuratedTaxonConstraints(generatedTaxonConstraintsFile,
                    curatedTaxonConstraintsFile, speciesFile, outputFile);
        } else if (args[0].equalsIgnoreCase("convertSpeciesConstraintsFileToLCAConstraintsFile") ||
                args[0].equalsIgnoreCase("convertSpeciesConstraintsFileToLCAConstraintsFileFromMergedOntology")) {

            if (args.length != 6) {
                throw log.throwing(new IllegalArgumentException("Incorrect number of arguments " +
                        "provided, expected 7 arguments, " + args.length +
                        " provided."));
            }
            String speciesConstraintsFile = args[3];
            String speciesFile = args[4];
            String outputFile = args[5];
            taxonConstraints.convertFromUberonToSpeciesTCFileToTaxonTCFile(
                    speciesConstraintsFile, speciesFile, outputFile);
        } else {
            throw log.throwing(new UnsupportedOperationException("The following action " +
                    "is not recognized: " + args[0]));
        }
        
        log.traceExit();
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
     * Constructor accepting the path to the Uberon ontology and the path to the taxonomy 
     * ontology, allowing to generate or retrieve taxon constraints. 
     * If it is requested to generate constraints, a default {@code SpeciesSubsetterUtil}
     * will be used (see
     * {@link #TaxonConstraints(OWLGraphWrapper, OWLGraphWrapper, Function)}).
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
     * Constructor accepting the path to the ontology containing Uberon and the taxonomy
     * already merged, allowing to generate or retrieve taxon constraints.
     * If it is requested to generate constraints, a default {@code SpeciesSubsetterUtil}
     * will be used (see
     * {@link #TaxonConstraints(OWLGraphWrapper, OWLGraphWrapper, Function)}).
     *
     * @param mergedUberonAndTaxonomyFile   A {@code String} that is the path to the ontology
     *                                      containing Uberon and the taxonomy already merged.
     * @throws UnknownOWLOntologyException  If the provided ontology could not be used.
     * @throws OWLOntologyCreationException If the provided ontology could not be used.
     * @throws OBOFormatParserException     If the provided ontology could not be used.
     * @throws IOException                  If the provided file could not be read.
     * @see #TaxonConstraints(OWLGraphWrapper)
     */
    public TaxonConstraints(String mergedUberonAndTaxonomyFile)
            throws UnknownOWLOntologyException, OWLOntologyCreationException,
            OBOFormatParserException, IOException {
        this(new OWLGraphWrapper(OntologyUtils.loadOntology(mergedUberonAndTaxonomyFile)));
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
     * Constructor accepting an {@code OWLGraphWrapper} wrapping the ontology containing Uberon
     * and the taxonomy already merged, allowing to generate or retrieve taxon constraints.
     * If it is requested to generate constraints, default {@code SpeciesSubsetterUtil} 
     * class will be used (see 
     * {@link #TaxonConstraints(OWLGraphWrapper, OWLGraphWrapper, Function)}).
     *
     * @param mergedUberonAndTaxonomyOntGraph   An {@code OWLGraphWrapper} containing the ontology
     *                                          containing Uberon and the taxonomy already merged.
     * @throws UnknownOWLOntologyException      if {@code mergedUberonAndTaxonomyOntGraph}
     *                                          could not be used.
     * @throws OWLOntologyCreationException     if {@code mergedUberonAndTaxonomyOntGraph}
     *                                          could not be used.
     */
    public TaxonConstraints(OWLGraphWrapper mergedUberonAndTaxonomyOntGraph) 
            throws UnknownOWLOntologyException, OWLOntologyCreationException {
        this(mergedUberonAndTaxonomyOntGraph, SpeciesSubsetterUtil::new);
    }
    /**
     * Constructor accepting an {@code OWLGraphWrapper} wrapping the ontology containing Uberon
     * and the taxonomy already merged, allowing to generate or retrieve taxon constraints,
     * as well as a {@code Function} that will act as a supplier of {@code SpeciesSubsetterUtil}s, 
     * accepting an {@code OWLGraphWrapper} as input. This will be used if it is requested 
     * to generate taxon constraints, to obtain a fresh {@code SpeciesSubsetterUtil} 
     * for each taxon for which constraints must be analyzed. 
     * 
     * @param mergedUberonAndTaxonomyOntGraph   An {@code OWLGraphWrapper} containing the ontology
     *                                          containing Uberon and the taxonomy already merged.
     * @param subsetterUtilSupplier             A {@code Function} accepting an {@code OWLGraphWrapper} 
     *                                          as input and returning a fresh {@code SpeciesSubsetterUtil}, 
     *                                          used as supplier of new {@code SpeciesSubsetterUtil}s 
     *                                          for each taxon . 
     * @throws UnknownOWLOntologyException      if {@code mergedUberonAndTaxonomyOntGraph} 
     *                                          could not be used.
     * @throws OWLOntologyCreationException     if {@code mergedUberonAndTaxonomyOntGraph} 
     *                                          could not be used.
     */
    public TaxonConstraints(OWLGraphWrapper mergedUberonAndTaxonomyOntGraph,
            Function<OWLGraphWrapper, SpeciesSubsetterUtil> subsetterUtilSupplier) 
            throws UnknownOWLOntologyException, OWLOntologyCreationException {
        this(mergedUberonAndTaxonomyOntGraph, null, subsetterUtilSupplier);
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
        if (uberonOntGraph == null) {
            throw log.throwing(new IllegalStateException("You must provide the Uberon " +
                    "ontology at instantiation."));
        }
        this.uberonOntWrapper = uberonOntGraph;
        if (taxOntGraph == null) {
            log.info("The Uberon ontology provided should have been merged properly "
                    + "with the taxonomy ontology already.");
            //Uberon in that case will be also the source for taxonomy information.
            this.taxOntWrapper = this.uberonOntWrapper;
        } else {
            this.taxOntWrapper = taxOntGraph;
        }
        this.subsetterUtilSupplier = subsetterUtilSupplier;
        
        this.prepareUberon(taxOntGraph == null);
    }
    
    /**
     * Prepares {@link #uberonOntWrapper} to be used to generate or retrieve taxon constraints,
     * by merging the import closures and clearing the cached edges.
     * If the taxonomy ontology was not already merged within Uberon, this method will remove
     * any "is_a" relations and disjoint classes axioms between taxa that might be present in Uberon,
     * they can be inconsistent with the taxonomy ontology we use.
     * Uberon and the taxonomy ontology will then be merged, for a reasoner to work properly.
     *
     * @param alreadyMergedUberonAndTaxonomy    A {@code boolean} defining whether the taxonomy ontology
     *                                          is already properly merged within the provided Uberon
     *                                          ontology (if {@code true}).
     */
    private void prepareUberon(boolean alreadyMergedUberonAndTaxonomy) throws OWLOntologyCreationException {
        log.traceEntry("{}", alreadyMergedUberonAndTaxonomy);
        log.info("Preparing Uberon...");
        //we need to merge the import closure, otherwise the classes in the imported ontologies 
        //will be seen by the method #getAllRealOWLClasses(), but not by the reasoner.
        this.uberonOntWrapper.mergeImportClosure(true);

        if (!alreadyMergedUberonAndTaxonomy) {
            //then, we remove any "is_a" relations and disjoint classes axioms between taxa
            //that might be present in Uberon, they can be inconsistent with the taxonomy we use.
            this.filterUberonOntology();

            //now we merge the Uberon ontology and the taxonomy ontology for the reasoner
            //to work properly, just importing them in a same OWLGraphWrapper woud not
            //be enough
            this.uberonOntWrapper.mergeOntology(this.taxOntWrapper.getSourceOntology());
        }
        
        this.uberonOntWrapper.clearCachedEdges();
        log.info("Done preparing Uberon.");
        log.traceExit();
    }
    
    /**
     * 
     * @param fileNamePrefix            A {@code String} that is the prefix of the dir to use to store 
     *                                  merged Uberon version as OBO and OWL.
     * @throws IllegalArgumentException
     * @throws IOException
     * @throws OWLOntologyStorageException 
     */
    public void saveUberonToFile(String fileNamePrefix) throws IllegalArgumentException, IOException, 
    OWLOntologyStorageException {
        log.traceEntry("{}", fileNamePrefix);
        
        OntologyUtils utils = new OntologyUtils(this.uberonOntWrapper);
        utils.saveAsOBO(fileNamePrefix + ".obo", false);
        utils.saveAsOWL(fileNamePrefix + ".owl");
        
        log.traceExit();
    }
    
    /**
     * Remove any "is_a" relations and disjoint classes axioms between taxa 
     * that might be present in {@link #uberonOntWrapper}, they can be inconsistent 
     * with the taxonomy we use, stored in {@link #taxOntWrapper}.
     */
    private void filterUberonOntology() {
        log.traceEntry();
        log.debug("Removing all axioms betwen taxa from Uberon");
        
        //Remove any "is_a" relations and disjoint classes axioms between taxa 
        //that might be present in Uberon, they can be inconsistent with the taxonomy 
        //we use.
        OWLOntology uberonOnt = this.uberonOntWrapper.getSourceOntology();
        OWLDataFactory factory = uberonOnt.getOWLOntologyManager().getOWLDataFactory();
        GenerateTaxonOntology disjointAxiomGenerator = new GenerateTaxonOntology();
        Set<OWLAxiom> axiomsToRemove = new HashSet<OWLAxiom>();
        
        Set<OWLClass> taxClasses = this.taxOntWrapper.getAllRealOWLClasses();
        //first, we identify any taxon present in Uberon but not in our taxonomy. 
        //we will remove them afterwards (we cannot simply delete everything, otherwise GCI relations 
        //would be deleted)
        OWLClass rootTax = this.uberonOntWrapper.getOWLClassByIdentifierNoAltIds(UberonCommon.TAXONOMY_ROOT_ID);
        Set<OWLClass> taxClassesToRemove = new HashSet<>();
        if (rootTax != null) {
            for (OWLClass tax: this.uberonOntWrapper.getDescendantsThroughIsA(rootTax)) {
                if (!taxClasses.contains(tax)) {
                    taxClassesToRemove.add(tax);
                }
            }
        }
        
        //now, remove axioms related to the taxa in our taxonomy from Uberon
        for (OWLClass taxon: taxClasses) {
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
        ChangeApplied axiomsRemoved = uberonOnt.getOWLOntologyManager().removeAxioms(uberonOnt, 
                axiomsToRemove);
        

        //finally we remove tax classes present in Uberon not our taxonomy
        OWLEntityRemover remover = new OWLEntityRemover(this.uberonOntWrapper.getAllOntologies());
        for (OWLClass uberonTax: taxClassesToRemove) {
            log.debug("Deleting taxon absent from our taxonomy: {}", uberonTax);
            uberonTax.accept(remover);
        }
        ChangeApplied status = this.uberonOntWrapper.getManager().applyChanges(remover.getChanges());
        if (status == ChangeApplied.UNSUCCESSFULLY) {
            throw log.throwing(new IllegalStateException("Could not delete some taxa: " 
                    + taxClassesToRemove));
        }
        
        log.debug("Axioms between taxa removed from Uberon: {}", axiomsRemoved);
        log.traceExit();
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
     * otherwise, by default they will be considered as existing in all the requested taxa. 
     * In any case, the {@code OWLClass}es part of the taxonomy ontology provided at instantiation 
     * will not be considered.
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
     * @param outputFile        A {@code String} that is the path to the generated
     *                          TSV file, output of the method. It will have one header line. 
     *                          The columns will be: ID of the Uberon classes, IDs of taxa 
     *                          where the Uberon class exists, Names of taxa where the Uberon
     *                          class exists, IDS of taxa where Uberon class does not exist, 
     *                          Names of the taxa where the Uberon class does not exist.
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
            String outputFile, String storeOntologyDir) throws IllegalArgumentException, IOException, 
            OBOFormatParserException, OWLOntologyStorageException, OWLOntologyCreationException {
        log.traceEntry("{}, {}, {}, {}, {}", taxonIdFile, taxaSimplificationSteps,
                completeUberonFile, outputFile, storeOntologyDir);
        
        //retrieve all tax IDs in taxonIdFile
        Set<Integer> taxonIds = AnnotationCommon.getTaxonIds(taxonIdFile);
        
        //get the simplification steps associated to requested taxa. 
        //We first clone taxaSimplificationSteps to avoid changes while streaming, 
        //we use a LinkedHashMap in case the generation order must be predicatable. 
        final Map<Integer, List<Integer>> clonedSteps = 
                taxaSimplificationSteps == null? new LinkedHashMap<>(): 
                    new LinkedHashMap<>(taxaSimplificationSteps);
        //Now, we expand the keys of the simplification steps to use the same steps 
        //for all their sub-taxa
       Map<Integer, List<Integer>> propagatedSteps = clonedSteps.entrySet().stream().flatMap(e -> {
            int taxId = e.getKey();
            OWLClass taxCls = taxOntWrapper.getOWLClassByIdentifier(
                    OntologyUtils.getTaxOntologyId(taxId), true);
            if (taxCls == null) {
                throw log.throwing(new IllegalArgumentException("A taxon provided "
                        + "in overriding constraints is not present in the taxonomy ontology: "
                        + taxId));
            }
            Map<Integer, List<Integer>> newSteps = taxOntWrapper.getDescendantsThroughIsA(taxCls).stream()
                    .map(c -> OntologyUtils.getTaxNcbiId(taxOntWrapper.getIdentifier(c)))
                    //we add the simplification steps to a descendant only if not already defined
                    .filter(id -> !clonedSteps.containsKey(id))
                    .collect(Collectors.toMap(id -> id, id -> e.getValue()));
            newSteps.put(taxId, e.getValue());
            
            return newSteps.entrySet().stream();
        })
        .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), 
                //in case we have several taxa provided on a same lineage, 
                //we'll have duplicates; we take the longest simplification step, 
                //although it's not optimal as we can override steps provided as argument
                (u, v) -> u.size() > v.size()? u: v, 
                LinkedHashMap::new)
        );
        //Now, generate a Map associating each taxon in taxonIds to its potential simplification steps.
        //Again, use a LinkedHashMap in case the generation order must be predicatable. 
        Map<Integer, List<Integer>> taxIdsWithSteps = taxonIds.stream()
                .collect(Collectors.toMap(Function.identity(), 
                     e -> propagatedSteps.get(e) != null? propagatedSteps.get(e): new ArrayList<Integer>(), 
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
        Set<String> refClassIds = refWrapper.getAllRealOWLClasses().stream()
                //We don't simply check whether the taxonomy ontology contains the class,
                //because as of Bgee 15 we can use a version of Uberon already merged with the taxonomy
                .filter(cls -> !UberonCommon.TAXONOMY_ROOT_ID.equals(this.taxOntWrapper.getIdentifier(cls)) &&
                               this.taxOntWrapper.getAncestorsThroughIsA(cls).stream()
                                   .map(a -> this.taxOntWrapper.getIdentifier(a))
                                   .noneMatch(id -> UberonCommon.TAXONOMY_ROOT_ID.equals(id)))
                .map(refWrapper::getIdentifier).collect(Collectors.toSet());
        
        //launch the generation of taxon constraints. 
        Map<String, Set<Integer>> constraints = this.generateTaxonConstraints(
                taxIdsWithSteps, refClassIds, storeOntologyDir);

        this.prepareAndWriteTaxonLCAToFile(constraints, taxonIds, refWrapper, outputFile);

        log.traceExit();
    }

    private void prepareAndWriteTaxonLCAToFile(Map<String, Set<Integer>> constraints,
            Set<Integer> speciesIds, OWLGraphWrapper refUberonWrapper, String outputFile)
                    throws IOException {
        log.traceEntry("{}, {}, {}, {}", constraints, speciesIds, refUberonWrapper, outputFile);

        //create Map where values correspond to speciesIds without constraints.
        // this Map will be useful to generate column of output file containing IDs of taxa
        // where the Uberon term does not exist.
        Map<String, Set<Integer>> absentConstraints = constraints.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> {
                    Set<Integer> absConstraints = new HashSet<Integer>(speciesIds);
                    absConstraints.removeAll(e.getValue());
                    return absConstraints;
                }));
        

        LinkedHashMap<Taxon, Set<Integer>> taxonToDescendantSpeciesMap =
                generateTaxonToDescendantSpecies(speciesIds);
        
        // update constraints to only contains taxon where all descendant species are present.
        // This step is used to generate an "easy" to read/maintain taxonContraint file that Bgee
        // curators can check manually.
        Map<String, Set<Taxon>> constraintsLCA = fromSpeciesToTaxonConstraints(constraints, 
                taxonToDescendantSpeciesMap, speciesIds);
        Map<String, Set<Taxon>>  absentConstraintsLCA = fromSpeciesToTaxonConstraints(absentConstraints, 
                taxonToDescendantSpeciesMap, speciesIds);

        writeTaxonLCAToFile(constraintsLCA, absentConstraintsLCA, speciesIds, refUberonWrapper,
                outputFile);
        
        log.traceExit();
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
            Set<String> refClassIds, String storeOntologyDir) throws IllegalArgumentException, IOException, 
            OWLOntologyCreationException, OWLOntologyStorageException {
        log.traceEntry("{}, {}, {}", taxonIds, refClassIds, storeOntologyDir);
        log.info("Start generating taxon constraints: {}", taxonIds);
        
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

        //then we get the IDs of all OWLClasses existing in the Uberon ontology 
        //used to generate taxon constraints. 
        Set<String> existingClassIds = this.uberonOntWrapper.getAllRealOWLClasses().stream()
                //We don't simply check whether the taxonomy ontology contains the class,
                //because as of Bgee 15 we can use a version of Uberon already merged with the taxonomy
                .filter(cls -> !UberonCommon.TAXONOMY_ROOT_ID.equals(this.taxOntWrapper.getIdentifier(cls)) &&
                               this.taxOntWrapper.getAncestorsThroughIsA(cls).stream()
                                   .map(a -> this.taxOntWrapper.getIdentifier(a))
                                   .noneMatch(id -> UberonCommon.TAXONOMY_ROOT_ID.equals(id)))
                .map(this.uberonOntWrapper::getIdentifier).collect(Collectors.toSet());
        log.trace("Existing OWLClasses in source Uberon ontology: {}", existingClassIds);
        
        //OK, let's check each requested OWLClass
        for (String refClassId: refClassIds) {

            //now, if the requested class did not exist in the Uberon ontology used 
            //to generate constraints, then we consider that this class exist in all requested taxa
            if (!existingClassIds.contains(refClassId)) {
                log.debug("The class {} was not present in the Uberon ontology used to compute "
                        + "taxon constraints, it will be considered as valid in all the requested taxa",
                        refClassId);
                taxonConstraints.put(refClassId, new HashSet<Integer>(taxonIds.keySet()));
            } 
            //otherwise, we simply use the taxon constraints computed, as the class is present 
            //in the Uberon ontology used. 
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
        return log.traceExit(filteredConstraints);
    }
    
    /**
     * Generate the final taxon constraints file that will be used to insert taxon constraints
     * in the database. In this file each line correspond to one {@code OWLClass} of the uberon 
     * ontology and the list of species ID it is present (T) or absent(F) in.
     * <p>
     * Two files containing taxon constraints are used to generate this final file.
     * <p> 
     * The first one is the file generated automatically from the Uberon ontology ({@link 
     * generateTaxonConstraints(String,  Map, String, Map, String, String)})
     * <p>
     * The second one is the file provided by Bgee curators that contains only taxon constraints
     * not properly defined automatically. Each line of this file will be used to overwrite the
     * corresponding information in the automatically generated file.
     * 
     * @param generatedTaxonConstraintsFile     path to the file containing taxon constraints generated 
     *                                          automatically from the Uberon ontology ({@link 
     *                                          generateTaxonConstraints(String,  Map, String, 
     *                                          Map, String, String)})
     * @param curatedTaxonConstraintsFile       path to the file containing manually generated taxon
     *                                          constraints to overwrite.
     * @param speciesFile                       path to the file containing all species IDs
     * @param outputFile                        path to the file where corrected taxon constraints will
     *                                          be written
     * @throws IOException
     */
    public void generateCuratedTaxonConstraints(String generatedTaxonConstraintsFile,
            String curatedTaxonConstraintsFile, String speciesFile, String outputFile) 
                    throws IOException {
        log.traceEntry("{}, {}, {}, {}", generatedTaxonConstraintsFile,
                curatedTaxonConstraintsFile, speciesFile, outputFile);
        
        //retrieve all tax IDs in taxonIdFile
        Set<Integer> speciesIds = AnnotationCommon.getTaxonIds(speciesFile);
        
        // generate Mapping between LCA taxon IDs and descendant species IDs
        Map<Integer, Set<Integer>> taxonToSpecies = 
                generateTaxonToDescendantSpecies(speciesIds).entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().getId(), 
                        e -> new HashSet<>(e.getValue())));
        
        Map<String, Set<Integer>> uberonIdToSpeciesIdsGeneratedMap =
                fromTaxonTCFileToUberonToSpeciesMap(generatedTaxonConstraintsFile,
                        taxonToSpecies, speciesIds);

        if (curatedTaxonConstraintsFile != null) {
            Map<String, Set<Integer>> uberonIdToSpeciesIdsCuratedMap =
                    fromTaxonTCFileToUberonToSpeciesMap(curatedTaxonConstraintsFile,
                            taxonToSpecies, speciesIds);
            // apply curators updates
            uberonIdToSpeciesIdsGeneratedMap = applyCuratorUpdates(uberonIdToSpeciesIdsGeneratedMap,
                    uberonIdToSpeciesIdsCuratedMap);
        }
            
        writeToFile(uberonIdToSpeciesIdsGeneratedMap, speciesIds, this.uberonOntWrapper, 
                outputFile);
        log.traceExit();
    }

    private static Map<String, Set<Integer>> applyCuratorUpdates(
            Map<String, Set<Integer>> uberonIdToSpeciesIdsGeneratedMap,
            Map<String, Set<Integer>> uberonIdToSpeciesIdsCuratedMap) {
        log.traceEntry("{}, {}", uberonIdToSpeciesIdsGeneratedMap, uberonIdToSpeciesIdsCuratedMap);
        return log.traceExit(uberonIdToSpeciesIdsGeneratedMap.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, e -> {
                    String uberonId = e.getKey();
                    //iterate all curated constraints to find the longest match possible with uberonId
                    Set<Integer> replacementConstraints = null;
                    String matchingPrefix = "";
                    for (Entry<String, Set<Integer>> uberonIdToSpeciesIdsCuratedEntry:
                        uberonIdToSpeciesIdsCuratedMap.entrySet()) {
                        if (uberonId.startsWith(uberonIdToSpeciesIdsCuratedEntry.getKey()) &&
                                uberonIdToSpeciesIdsCuratedEntry.getKey().length() > matchingPrefix.length()) {
                            matchingPrefix = uberonIdToSpeciesIdsCuratedEntry.getKey();
                            log.trace("Uberon ID {} matching prefix {}, taxon constraints overriden: {}",
                                    uberonId, matchingPrefix, uberonIdToSpeciesIdsCuratedEntry.getValue());
                            replacementConstraints =
                                    new HashSet<Integer>(uberonIdToSpeciesIdsCuratedEntry.getValue());
                            //continue iterations anyway in case there is a longest match
                        }
                    }
                    if(replacementConstraints != null) {
                        log.debug("Use taxon constraints overriden for Uberon ID {}: {}",
                                uberonId, replacementConstraints);
                        return replacementConstraints;
                    }
                    log.trace("Use taxon constraints generated for Uberon ID {}: {}", uberonId, e.getValue());
                    return e.getValue();
                })));
    }

    public void propagateCuratedTaxonConstraints(String generatedTaxonConstraintsFile,
            String curatedTaxonConstraintsFile, String speciesFile, String outputFile)
                    throws IOException, OWLOntologyCreationException {
        log.traceEntry("{}, {}, {}, {}", generatedTaxonConstraintsFile,
                curatedTaxonConstraintsFile, speciesFile, outputFile);

        //retrieve all tax IDs in taxonIdFile
        Set<Integer> speciesIds = AnnotationCommon.getTaxonIds(speciesFile);
        // generate Mapping between LCA taxon IDs and descendant species IDs
        Map<Integer, Set<Integer>> taxonToSpecies =
                generateTaxonToDescendantSpecies(speciesIds).entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().getId(),
                        e -> new HashSet<>(e.getValue())));
        Uberon uberon = new Uberon(new OntologyUtils(this.uberonOntWrapper));

        //Retrieve the generated and curated taxon constraints
        Map<String, Set<Integer>> uberonIdToSpeciesIdsGeneratedMap =
                fromTaxonTCFileToUberonToSpeciesMap(generatedTaxonConstraintsFile,
                        taxonToSpecies, speciesIds);
        Map<String, Set<Integer>> uberonIdToSpeciesIdsCuratedMap =
                fromTaxonTCFileToUberonToSpeciesMap(curatedTaxonConstraintsFile,
                        taxonToSpecies, speciesIds);

        //Apply curators updates
        Map<String, Set<Integer>> uberonIdToSpeciesIdsUpdatedMap = applyCuratorUpdates(
                uberonIdToSpeciesIdsGeneratedMap, uberonIdToSpeciesIdsCuratedMap);

        //Now we iterate each Uberon ID, to find if there was a curated taxon constraint for it.
        //We could not simply use uberonIdToSpeciesIdsCuratedMap, as curated taxon constraints
        //can use the prefix of a class ID rather than the full ID.
        //And we propagate the curated taxon constraints to ancestors of the Uberon term
        //for which they are incorrect.
        Map<String, Set<Integer>> uberonIdToSpeciesIdsPropagatedMap =
                uberonIdToSpeciesIdsUpdatedMap.entrySet().stream()
                //First, we consider only the classes for which we have some curated taxon constraints
                .filter(e -> !e.getValue().equals(uberonIdToSpeciesIdsGeneratedMap.get(e.getKey())))
                //Ok, now we are going to propagate the curated taxon constraints to ancestors
                .flatMap(e -> {
                    String uberonId = e.getKey();
                    Set<Integer> constraintSpeciesIds = e.getValue();
                    log.info("Propagating curated taxon constraints for class {}, "
                            + "original taxon constraints {}, updated taxon constraints {}",
                            uberonId, uberonIdToSpeciesIdsGeneratedMap.get(uberonId),
                            constraintSpeciesIds);

                    //Now, we go species by species from the curated taxon constraints,
                    //it will be simpler to take into account GCI relations.
                    //We cannot use the most straightforward built-in methods of the Uberon class,
                    //as it would discard classes not existing in the targeted species,
                    //while we exactly want to retrieve those.
                    return constraintSpeciesIds.stream().flatMap(speciesId -> {
                        //Retrieve all the ancestors of this Uberon class in this species
                        Set<String> ancestorIds = getAncestorIds(uberon, uberonId, speciesIds, speciesId);
                        log.debug("Found {} ancestors", ancestorIds.size());
                        Set<String> allIds = new HashSet<>(ancestorIds);
                        allIds.add(uberonId);

                        return allIds.stream()
                            //and produce the Entries that will be merged between species
                            .map(id -> new AbstractMap.SimpleEntry<>(id,
                                    new HashSet<>(Arrays.asList(speciesId))));
                    });
                })
                //Now we merge all the curated species IDs for a same Uberon ID
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(),
                        (v1, v2) -> {v1.addAll(v2); return v1;}))
                //And now we will keep only entries where the taxon constraints
                //do not include all species of the curated propagated taxon constraints
                .entrySet().stream()
                .filter(e -> !uberonIdToSpeciesIdsUpdatedMap.getOrDefault(e.getKey(), new HashSet<>())
                        .containsAll(e.getValue()))
                .collect(Collectors.toMap(e -> e.getKey(),
                        e -> {
                            Set<Integer> allSpeciesIds = new HashSet<>(
                                    uberonIdToSpeciesIdsUpdatedMap.getOrDefault(e.getKey(),
                                            new HashSet<>()));
                            allSpeciesIds.addAll(e.getValue());
                            return allSpeciesIds;
                        }
                ));

        this.prepareAndWriteTaxonLCAToFile(uberonIdToSpeciesIdsPropagatedMap, speciesIds,
                this.uberonOntWrapper, outputFile);

        log.traceExit();
    }

    private boolean isEdgeValidInSpecies(OWLGraphEdge edge, int speciesId) {
        log.traceEntry("{}, {}", edge, speciesId);
        //If it is not a GCI relation, it is valid in any species and we consider it
        if (!edge.isGCI()) {
            return true;
        }
        //if it is a GCI, we retrieve the associated species
        Set<String> speciesClsIdsToConsider = new HashSet<>(Arrays.asList(
                this.taxOntWrapper.getIdentifier(edge.getGCIFiller())));
        for (OWLClass taxonGCIDescendants:
            this.taxOntWrapper.getDescendantsThroughIsA(edge.getGCIFiller())) {
            speciesClsIdsToConsider.add(
                    this.taxOntWrapper.getIdentifier(taxonGCIDescendants));
        }
        Set<Integer> speciesIdsToConsider = OntologyUtils.convertToNcbiIds(
                speciesClsIdsToConsider);
        //And we consider the relation if it is valid in the iterated species.
        return speciesIdsToConsider.contains(speciesId);
    }
    private Set<String> getAncestorIds(Uberon uberon, String sourceId,
            Set<Integer> allSpeciesIds, int targetedSpeciesId) {
        log.traceEntry("{}, {}, {}, {}", uberon, sourceId, allSpeciesIds, targetedSpeciesId);
        //Retrieve the part_of/is_a edges outgoing from this Uberon class.
        //And because owltools sometimes does not retrieve all indirect edges,
        //we retrieve also all edges outgoing from targets.
        Set<String> ancestorIds = new HashSet<>();
        Deque<String> targetWalker = new ArrayDeque<>(Arrays.asList(sourceId));
        Set<String> visitedTargetIds = new HashSet<>(Arrays.asList(sourceId));
        String currentTargetId = null;
        while ((currentTargetId = targetWalker.pollFirst()) != null) {
            log.debug("Retrieving edges outgoing from {}", currentTargetId);
            //We use 'allSpeciesIds' to retrieve edges valid in any species,
            //precisely for not filtering classes based on taxon constraints
            Map<Boolean, Set<OWLGraphEdge>> edgeMap = uberon.getValidOutgoingEdgesFromOWLClassIds(
                    currentTargetId, null, allSpeciesIds);
            Set<String> nextTargetIds = edgeMap == null ? new HashSet<>() :
                edgeMap.values().stream().flatMap(s -> s.stream())
                    //keep only part_of/is_a edges
                    .filter(edge -> uberon.getOntologyUtils().isASubClassOfEdge(edge) ||
                            uberon.getOntologyUtils().isPartOfRelation(edge))
                    //keep only edges valid in the requested species
                    .filter(edge -> this.isEdgeValidInSpecies(edge, targetedSpeciesId))
                    .map(edge -> uberon.getOntologyUtils().getWrapper().getIdentifier(edge.getTarget()))
                    .collect(Collectors.toSet());

            for (String nextTargetId: nextTargetIds) {
                if (!visitedTargetIds.contains(nextTargetId)) {
                    ancestorIds.add(nextTargetId);
                    targetWalker.offerLast(nextTargetId);
                }
                visitedTargetIds.add(nextTargetId);
            }
        }
        return log.traceExit(ancestorIds);
    }

    public void convertFromUberonToSpeciesTCFileToTaxonTCFile(String pathToTaxonConstraints,
            String speciesIdFile, String outputFile) throws FileNotFoundException, IOException {
        log.traceEntry("{}, {}, {}", pathToTaxonConstraints, speciesIdFile, outputFile);

        Map<String, Set<Integer>> constraints = TaxonConstraints.extractTaxonConstraints(
                pathToTaxonConstraints);
        Set<Integer> speciesIds = AnnotationCommon.getTaxonIds(speciesIdFile);
        this.prepareAndWriteTaxonLCAToFile(constraints, speciesIds,
                this.uberonOntWrapper, outputFile);

        log.traceExit();
    }
    
    /**
     * From a file containing Uberon terms associated to taxon IDs where the terms are present, create a 
     * {@code Map} with Uberon IDs as key and {@code Set} of all species IDs where the uberon ID is present 
     * as value.
     * <p> 
     * Basically this method use a mapping between taxon IDs to descendant species IDs to transform taxon IDs
     * present in the input file to all descendant species IDs.
     * 
     * @param pathToFile            Path to the file containing taxon constraints at taxon level
     * @param taxonToSpecies        A {@code Map} with {@code Integer} corresponding to taxon IDs as key and
     *                              {@code Set} of {@code Integer} corresponding to descendant species IDs as
     *                              value.
     * @param allSpeciesIds         A {@code Set} of {@code Integer} containing the IDs of all species.
     * 
     * @return                      A {@code Map} of {@code String} corresponding to Uberon IDs as key associated 
     *                              to a {@code Set} of {@code Integer} corresponding to all species IDs the uberon
     *                              term is present in as values. 
     * @throws IOException
     */
    private static Map<String, Set<Integer>> fromTaxonTCFileToUberonToSpeciesMap(String pathToFile,
            Map<Integer, Set<Integer>> taxonToSpecies, Set<Integer> allSpeciesIds) throws IOException {
        log.traceEntry("{}, {}, {}", pathToFile, taxonToSpecies, allSpeciesIds);
        
        Map<String, Set<Integer>> uberonToSpecies = new HashMap<String, Set<Integer>>();
        
        //read the uberon to taxon file
        try (ICsvBeanReader beanReader =
                new CsvBeanReader(new FileReader(pathToFile), Utils.TSVCOMMENTED)) {
            
            // using null value in header allows not to consider this column
            String[] fieldMapping = new String[] { "uberonId", null, "taxonIdWithConstraints",
                    null, "taxonIdWithoutConstraints", null, null};
            
            // using null as CellProcessor allows not to process data in this column
            final CellProcessor[] processors = new CellProcessor[] { 
                    new UniqueHashCode(), //uberon ID
                    null,  // uberon Name
                    new Optional(), // taxon IDs present
                    null, // taxon names present
                    new Optional(), // taxon IDs absent
                    null, // taxon names absent
                    null  // comment
            };
            beanReader.getHeader(true);
            TaxonTaxonConstraintsBean taxonTC;
            while ( (taxonTC = beanReader.read(TaxonTaxonConstraintsBean.class, fieldMapping,
                    processors)) != null ) {

                String uberonId = taxonTC.getUberonId();
                int lineNumber = beanReader.getLineNumber();

                //By default, without constraints the anat. entity exists in all species
                Set<Integer> inSpeciesIds = new HashSet<>(allSpeciesIds);
                //And we can exclude species from the list
                //(that is either all species if there is no constraint,
                //or the list of "only_in_taxon" species)
                Set<Integer> neverInSpeciesIds = new HashSet<>();
                //Retrieve taxon Ids with and/or without TC.
                if ( (taxonTC.getTaxonIdWithoutConstraints() == null ||
                        taxonTC.getTaxonIdWithoutConstraints().isEmpty()) &&
                        (taxonTC.getTaxonIdWithConstraints() == null ||
                        taxonTC.getTaxonIdWithConstraints().isEmpty()) ) {
                    throw log.throwing(new IllegalArgumentException("Both the columns "
                            + WITH_CONSTRAINTS_ID + " and " + WITHOUT_CONSTRAINTS_ID + " are empty. "
                            + "Uberon ID: " + uberonId + " - Line number: " + lineNumber));
                }
                if (taxonTC.getTaxonIdWithConstraints() != null &&
                        !taxonTC.getTaxonIdWithConstraints().isEmpty()) {
                    inSpeciesIds = getSpeciesIdsFromTaxonColumn(
                            taxonTC.getTaxonIdWithConstraints(), taxonToSpecies, lineNumber);
                }
                if (taxonTC.getTaxonIdWithoutConstraints() != null &&
                        !taxonTC.getTaxonIdWithoutConstraints().isEmpty()) {
                    neverInSpeciesIds = getSpeciesIdsFromTaxonColumn(
                            taxonTC.getTaxonIdWithoutConstraints(), taxonToSpecies, lineNumber);
                }

                if (!inSpeciesIds.containsAll(neverInSpeciesIds)) {
                    throw log.throwing(new IllegalArgumentException("The taxa in column "
                            + WITHOUT_CONSTRAINTS_ID
                            + " are not a subset of the taxa where the anat. entity exists. "
                            + "Uberon ID: " + uberonId + " - Line number: " + lineNumber
                            + " - In species IDs: " + inSpeciesIds.stream()
                                .map(id -> id.toString()).collect(Collectors.joining(", "))
                            + " - Never in species IDs: " + neverInSpeciesIds.stream()
                                .map(id -> id.toString()).collect(Collectors.joining(", "))));
                }
                inSpeciesIds.removeAll(neverInSpeciesIds);
                uberonToSpecies.put(uberonId, inSpeciesIds);
            }             
        }
        return log.traceExit(uberonToSpecies);
    }
    private static Set<Integer> getSpeciesIdsFromTaxonColumn(String taxColToSplit,
            Map<Integer, Set<Integer>> taxonToSpecies, int lineNumber) {
        log.traceEntry("{}, {}, {}", taxColToSplit, taxonToSpecies, lineNumber);

        Set<Integer> taxonIds = Arrays.stream(taxColToSplit.split(","))
                .map(e -> Integer.valueOf(e.trim()))
                .collect(Collectors.toSet());

        // retrieve species corresponding to taxa
        return log.traceExit(taxonIds.stream()
                .flatMap(id -> {
                    Set<Integer> taxSpeciesIds = taxonToSpecies.get(id);
                    if (taxSpeciesIds == null) {
                        throw log.throwing(new IllegalStateException("Could not find taxon ID: "
                                + id + ", line: " + lineNumber));
                    }
                    return taxSpeciesIds.stream();
                })
                .collect(Collectors.toSet()));
    }
    
    /**
     * Returns the Mapping between taxa in the taxonomy ontology and the Set
     * of all Bgee species descendant to this taxon.
     * This method is useful to generate an easier to read output file listing taxon constraints at LCA level
     * and not for each species (e.g if all Bgee mammalian species have a taxonConstraint for an anatomical 
     * entity, the output file will contain only Mammalia and not all species).
     * Return a linkedHashMap ordered by descending size of the Set of values. This ordering allows to always 
     * find the highest LCA taxon level first when iterating on the entries.
     * The returned {@code LinkedHashMap} also contains {@Taxon} corresponding to bgee species as keys mapped to
     * a {@code Set} of one {@Taxon} corresponding to this species as value. This one to one mapping is useful to
     * store species scientific name and then provide this information in the output file to help curators.
     * 
     * 
     * @param   speciesIds A {@code Set} containing all Bgee species IDs
     * @return  A {@code LinkedHashMap} with {@code Taxon} as key, and {@code Set} of {@code Integer}s
     *          as values that are the NCBI IDs of the descendant species of the associated {@code Taxon}.
     *          The {@code LinkedHashMap} is ordered by reversed size of the {@code Set} of values.
     */
    private LinkedHashMap<Taxon, Set<Integer>> generateTaxonToDescendantSpecies(Set<Integer> speciesIds) {
        log.traceEntry("{}", speciesIds);
        
        //define least common ancestor and corresponding Bgee species. Generate Taxon objects
        // to be able to provide taxon scientific name in the output file
        return log.traceExit(this.taxOntWrapper.getAllRealOWLClasses().stream()
        .map(cls -> {
            Integer taxId = OntologyUtils.getTaxNcbiId(this.taxOntWrapper.getIdentifier(cls));
            //getTaxNcbiId returns null if it is not a NCBITaxon ID
            if (taxId == null) {
                return null;
            }
            Taxon taxon = new Taxon(taxId,
                    null, null, this.taxOntWrapper.getLabel(cls),
                    1, false);
            Set<Integer> taxSpeIds = new HashSet<>();
            if (speciesIds.contains(taxId)) {
                taxSpeIds.add(taxId);
            } else {
                taxSpeIds = this.taxOntWrapper
                        .getDescendantsThroughIsA(cls).stream()
                        .map(d -> OntologyUtils.getTaxNcbiId(this.taxOntWrapper.getIdentifier(d)))
                        // Keep only Bgee species Ids
                        .filter(id -> id != null && speciesIds.contains(id))
                        .collect(Collectors.toSet());
                if (taxSpeIds.isEmpty()) {
                    return null;
                }
            }
            return new AbstractMap.SimpleEntry<>(taxon, taxSpeIds);
        })
        .filter(e -> e != null)
        .sorted(Entry.<Taxon, Set<Integer>>comparingByValue(
                Comparator.comparingInt(Set::size)).reversed())
        .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(),
                (v1, v2) -> v1, () -> new LinkedHashMap<Taxon, Set<Integer>>())));
    }
    
    /**
     * Return the mapping between an Uberon ID and all LCA taxon with taxon constraint information.
     * This information can be LCAs with presence of a taxon constraint for a given Uberon ID if the 
     * {@code constraints} Map provided as parameter correspond to the mapping between uberon IDs and all species 
     * with a taxon constraint OR LCAs with absence of taxon constraint for a given uberon ID if the
     * {@code constraints} Map provided as parameter correspond to the mapping between uberon IDs an all species
     * without a taxon constraint. The information of LCAs with absence of taxon constraints is useful
     * to generate an easier to read output file containing rather the LCAs with taxon constraints or
     * the LCAs without taxon constraints depending of the number of taxon associated. If for a given
     * uberon ID the size of the list of LCAs without taxon constraint is smaller than the one with 
     * taxon constraints then only LCAs without taxon constraints will be written in the output file.
     *  
     * 
     * @param constraints                       A {@code Map} where keys are {@code String} corresponding
     *                                          to uberon IDs and values are a {@code Set} of {@code Integer}
     *                                          corresponding to Bgee species IDs with presence or absence of
     *                                          taxon constraints
     * @param taxonToDescendantSpeciesMap       A {@code LinkedHashMap} with {@code Taxon} as key and
     *                                          {@code Set} of {@code Integer}s as values that are IDs
     *                                          of species belonging to the associated taxon. The {@code LinkedHashMap}
     *                                          is ordered by reversed size of the {@code Set} of values.
     * @param speciesIds                        A {@code Set} of {@code Integer}s corresponding to all Bgee
     *                                          species IDs
     * @return                                  A {@code Map} where keys are {@code String} correpsonding
     *                                          to uberon IDs and values are a {@code Set} of {@code Taxon}
     *                                          corresponding to LCAs taxons with presence or absence of
     *                                          taxon constraints
     */
    private Map<String, Set<Taxon>> fromSpeciesToTaxonConstraints(Map<String, Set<Integer>> constraints,
            LinkedHashMap<Taxon, Set<Integer>> taxonToDescendantSpeciesMap, Set<Integer> speciesIds) {
        log.traceEntry("{}, {}", constraints, speciesIds);
        
        Map<String, Set<Taxon>> uberonToTaxIds = new HashMap<String, Set<Taxon>>();
        for(Entry<String, Set<Integer>> uberonToSpeciesIds : constraints.entrySet()) {
            Set<Integer> remainingSpeciesIds = new HashSet<>(uberonToSpeciesIds.getValue());
            Set<Taxon> newTaxonIds = new HashSet<Taxon>();
            for(Entry<Taxon, Set<Integer>> taxonToDescendantSpeciesEntry:
                taxonToDescendantSpeciesMap.entrySet()) {
                Set<Integer> descendantspeciesIds = taxonToDescendantSpeciesEntry.getValue();
                if(remainingSpeciesIds.containsAll(descendantspeciesIds)) {
                    newTaxonIds.add(taxonToDescendantSpeciesEntry.getKey());
                    remainingSpeciesIds.removeAll(descendantspeciesIds);
                    if(remainingSpeciesIds.isEmpty()) {
                        break;
                    }
                }
            }
            assert remainingSpeciesIds.isEmpty():
                "No corresponding taxon found for species IDs: " + remainingSpeciesIds;
            uberonToTaxIds.put(uberonToSpeciesIds.getKey(), newTaxonIds);
        }
        
        return log.traceExit(uberonToTaxIds);
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
        log.traceEntry("{}, {}, {}", taxonId, intermediateTaxonIds, storeOntologyDir);
        
        //for each taxon, we clone our Uberon ontology merged with our taxonomy ontology, 
        //because the method getExistingOWLClasses will modified it.
        //we use a new OWLOntologyManager to be sure there is no memory leack.
        OWLOntology clonedUberon = OWLManager.createOWLOntologyManager().createOntology(
            IRI.create("Uberon_for_" + taxonId), 
            new HashSet<OWLOntology>(this.uberonOntWrapper.getAllOntologies()));
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
            return log.traceExit(this.getExistingOWLClasses(graph, taxClass, storeOntologyDir, true));
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
        log.traceEntry("{}, {}, {}, {}", ontWrapper, taxClass, storeOntologyDir, removeOtherTaxa);
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
        return log.traceExit(ontWrapper.getAllRealOWLClassesFromSource());
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
        log.traceEntry("{}", ont);
        ElkReasonerConfiguration config = new ElkReasonerConfiguration();
        //we need to set the number of workers because on our ubber machines,
        //we have too many processors, so that we have too many workers,
        //and too many memory consumed.
        if (config.getElkConfiguration().getParameterAsInt(
                ReasonerConfiguration.NUM_OF_WORKING_THREADS) > MAX_WORKER_COUNT) {
            config.getElkConfiguration().setParameter(
                ReasonerConfiguration.NUM_OF_WORKING_THREADS, String.valueOf(MAX_WORKER_COUNT));
        }
        return log.traceExit(new ElkReasonerFactory().createReasoner(ont, config));
    }
    

    private static void writeTaxonLCAToFile(Map<String, Set<Taxon>> uberonToTaxonConstraints, 
            Map<String, Set<Taxon>> uberonToNoTaxonConstraints, Set<Integer> taxonIds, 
            OWLGraphWrapper refUberonWrapper, String outputFile) throws IOException {
        log.traceEntry("{}, {}, {}, {}, {}", uberonToTaxonConstraints, uberonToNoTaxonConstraints,
                taxonIds, refUberonWrapper, outputFile);
        
        //order uberon IDs to have same row ordering between releases
        List<String> uberonIds = new ArrayList<String>(uberonToTaxonConstraints.keySet());
        Collections.sort(uberonIds);
        
        final CellProcessor[] processors = new CellProcessor[] { 
                new UniqueHashCode(new NotNull()), 
                new NotNull(), 
                new Optional(),
                new Optional(),
                new Optional(),
                new Optional(),
                new Optional()
        };
        String[] header= new String[] {
                UBERON_ID_COLUMN_NAME, UBERON_NAME_COLUMN_NAME,
                WITH_CONSTRAINTS_ID, WITH_CONSTRAINTS_NAME,
                WITHOUT_CONSTRAINTS_ID, WITHOUT_CONSTRAINTS_NAME, COMMENTS           
        };
        String[] fieldMapping = new String[] { "uberonId", "uberonName", "taxonIdWithConstraints",
                "taxonNameWithConstraints", "taxonIdWithoutConstraints", "taxonNameWithoutConstraints",
                "description"};
        try (ICsvBeanWriter beanWriter = new CsvBeanWriter(new FileWriter(outputFile),
                Utils.TSVCOMMENTED)) {
            
            beanWriter.writeHeader(header);
            for(String uberonId : uberonIds) {
                //boolean to remember in which columns to write taxon info
                boolean withConstraintsColumn = true;
                
                // retrieve taxa to write
                List<Taxon> taxaToWrite;
                int noTaxonConstraintsSize = uberonToNoTaxonConstraints.get(uberonId).size();
                int taxonConstraintsSize = uberonToTaxonConstraints.get(uberonId).size();
                if( (noTaxonConstraintsSize > 0 && noTaxonConstraintsSize < taxonConstraintsSize)
                        || taxonConstraintsSize == 0) {
                    taxaToWrite = new ArrayList<Taxon>(uberonToNoTaxonConstraints
                            .get(uberonId));
                    withConstraintsColumn = false;  
                } else {
                    taxaToWrite = new ArrayList<Taxon>(uberonToTaxonConstraints
                            .get(uberonId));
                }
                // sort taxa by Taxon Id
                taxaToWrite.sort((Taxon t1, Taxon t2) -> t1.getId()-t2.getId());
                
                OWLClass cls = refUberonWrapper.getOWLClassByIdentifier(uberonId, true);
                String label = "-";
                if (cls != null) {
                    label = refUberonWrapper.getLabelOrDisplayId(cls);
                }
                //generate text from list of taxon
                String taxonIdsText = String.join(", ", taxaToWrite.stream()
                        .map(t  -> t.getId().toString()).collect(Collectors.toList()));
                String taxonNamesText = String.join(", ", taxaToWrite.stream()
                        .map(t  -> t.getScientificName()).collect(Collectors.toList()));
                
                // add values to corresponding columns
                TaxonTaxonConstraintsBean taxonTCBean = null;
                if(withConstraintsColumn) {
                    taxonTCBean = new TaxonTaxonConstraintsBean(uberonId, label, taxonIdsText,
                            taxonNamesText, null, null, null);
                } else {
                    taxonTCBean = new TaxonTaxonConstraintsBean(uberonId, label, null, null, 
                            taxonIdsText, taxonNamesText, null);
                }
                beanWriter.write(taxonTCBean, fieldMapping, processors);
             
            }
        }
        
        log.traceExit();
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
        log.traceEntry("{}, {}, {}, {}", taxonConstraints, taxonIds, refUberonWrapper, outputFile);

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
        
        log.traceExit();
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
        log.traceEntry("{}, {}", owlClassIds, taxonIds);
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
        return log.traceExit(util.explainTaxonConstraint(owlClassIds, 
                OntologyUtils.convertToTaxOntologyIds(taxonIds)));
    }
    
    /**
     * Explain and make a pretty print of the taxon constraits. Constraints are explained using 
     * {@link #explainTaxonExistence(Collection, Collection)}), and are printed/captured 
     * using the {@code Consumer} {@code printMethod}. Explanations are also ordered 
     * from the most precise taxon to the less precise taxon.
     * {@code printMethod} is responsible for managing line return. 
     * 
     * @param owlClassIds   A {@code Collection} of {@code String}s that are the OBO-like 
     *                      IDs of the {@code OWLClass}es for which we want explanations 
     *                      of taxon constraints.
     * @param taxonIds      A {@code Collection} of {@code String}s that are the OBO-like 
     *                      IDs of the {@code OWLClass}es representing taxa, for which 
     *                      we want explanations of taxon constraints.
     * @param printMethod   A {@code Consumer} allowing to display or capture a {@code String}.
     * @throws IllegalArgumentException    If some of the requested {@code OWLClass}es 
     *                                     or requested taxa could not be found in 
     *                                     the provided ontologies.
     * @see #explainTaxonExistence(Collection, Collection)
     */
    public void explainAndPrintTaxonExistence(Collection<String> owlClassIds, 
            Collection<Integer> taxonIds, Consumer<String> printMethod) throws IllegalArgumentException {
        log.traceEntry("{}, {}, {}", owlClassIds, taxonIds, printMethod);
        
        Collection<List<OWLObject>> explanations = this.explainTaxonExistence(owlClassIds, taxonIds);
        
        if (explanations.isEmpty()) {
            printMethod.accept("No specific explanation for existence of " + owlClassIds 
                    + " in taxon " + taxonIds + ". " +
                    "If it is defined as non-existing, then there is a problem...");
        } else {
            printMethod.accept("Explanations for existence/nonexistence of " + owlClassIds 
                    + " in taxon " + taxonIds + ": ");
        }
        //we try to order the explanations, to have first the explanations targeting 
        //the most precise taxa
        List<List<OWLObject>> sortedExplanations = new ArrayList<>(explanations);
        sortedExplanations.sort((e1, e2) -> {
            //we take the last explanation of each list, and try to extract the taxon from it. 
            OWLClass taxon1 = this.extractTargetedTaxonFromExplanation(e1);
            OWLClass taxon2 = this.extractTargetedTaxonFromExplanation(e2);
            //try to find which one is the more precise (more ancestors = deeper level)
            int diff = this.taxOntWrapper.getAncestorsThroughIsA(taxon2).size() - 
                    this.taxOntWrapper.getAncestorsThroughIsA(taxon1).size();
            if (diff != 0) {
                return diff;
            }
            //if same depth, we simply order by alphabetical order of the taxon ID
            return taxon1.getIRI().toString().compareTo(taxon2.getIRI().toString());
        });
        
        for (List<OWLObject> explanation: sortedExplanations) {
            StringBuilder sb = new StringBuilder();
            for (OWLObject explainPart: explanation) {
                if (explainPart instanceof OWLClass) {
                    log.trace("Pretty printing of OWLClass {}", explainPart);
                    if (sb.length() != 0) {
                        sb.append("is_a ");
                    }
                    sb.append(this.uberonOntWrapper.getIdentifier(explainPart)) 
                            .append(" \"").append(this.uberonOntWrapper.getLabelOrDisplayId(explainPart))
                            .append("\" ");
                } else if (explainPart instanceof OWLObjectSomeValuesFrom) {
                    log.trace("Pretty printing of OWLObjectSomeValuesFrom {}", explainPart);
                    OWLClassExpression filler = ((OWLObjectSomeValuesFrom) explainPart).getFiller();
                    OWLObjectPropertyExpression rel = ((OWLObjectSomeValuesFrom) explainPart).getProperty();
                    sb.append(this.uberonOntWrapper.getLabelOrDisplayId(rel)) 
                            .append(" ").append(this.uberonOntWrapper.getIdentifier(filler)) 
                            .append(" \"").append(this.uberonOntWrapper.getLabelOrDisplayId(filler))
                            .append("\" ");
                } else if (explainPart instanceof OWLAnnotation) {
                    log.trace("Pretty printing of OWLAnnotation {}", explainPart);
                    OWLClass filler = this.uberonOntWrapper.getOWLClass(
                            ((OWLAnnotation) explainPart).getValue());
                    OWLAnnotationProperty rel = ((OWLAnnotation) explainPart).getProperty();
                    sb.append(this.uberonOntWrapper.getLabelOrDisplayId(rel)) 
                            .append(" ").append(this.uberonOntWrapper.getIdentifier(filler)) 
                            .append(" \"").append(this.uberonOntWrapper.getLabelOrDisplayId(filler))
                            .append("\" ");
                } else {
                    log.trace("Pretty printing of other type {}", explainPart);
                    sb.append(explainPart.toString()).append(" ");
                }
            }
            log.trace("Pretty printing of explanation: {}", sb); 
            printMethod.accept(sb.toString());
        }
    }
    
    /**
     * Retrieve the targeted taxon from the last {@code OWLObject} in {@code explanation}. 
     * Usually, the last {@code OWLObject} of an explanation of taxon contraints 
     * is either an {@code AnnotationProperty} (using "never_in_taxon"), 
     * or an {@code OWLObjectSomeValuesFrom} (using "only_in_taxon"), targeting a taxon. 
     * 
     * @param explanation   A {@code List} of {@code OWLObject}s that is an explanation 
     *                      about a taxon constraint, as returned by 
     *                      {@link #explainTaxonExistence(Collection, Collection)}.
     * @return              An {@code OWLClass} that is the taxon targeted by the explanation.
     * @throws IllegalArgumentException If no taxon could be retrieved from the last 
     *                                  {@code OWLObject} in {@code explanation}.
     */
    private OWLClass extractTargetedTaxonFromExplanation(List<OWLObject> explanation) {
        log.traceEntry("{}", explanation);
        
        //we take the last explanation of the list, and try to extract the taxon from it. 
        //the last explanation should either be an AnnotationProperty ("never_in_taxon") 
        //or an ObjectProperty ("only_in_taxon")
        OWLObject last = explanation.get(explanation.size() - 1);
        OWLClass taxon = null;
        
        if (last instanceof OWLObjectSomeValuesFrom) {
            taxon = (OWLClass) ((OWLObjectSomeValuesFrom) last).getFiller();
        } else if (last instanceof OWLAnnotation) {
            taxon = this.taxOntWrapper.getOWLClass(((OWLAnnotation) last).getValue());
        }
        if (taxon == null) {
            throw new IllegalArgumentException("Could not find taxon corresponding to last explanation: "
                    + taxon);
        }
        
        return log.traceExit(taxon);
    }
    

    public OWLGraphWrapper getUberonOntWrapper() {
        return uberonOntWrapper;
    }
    public OWLGraphWrapper getTaxOntWrapper() {
        return taxOntWrapper;
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
        log.traceEntry("{}", taxonConstraintsFile);
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
            
            return log.traceExit(taxonIds);
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
        log.traceEntry("{}", taxonConstraints);
        
        Set<Integer> taxIds = new HashSet<Integer>();
        for (Set<Integer> iterateTaxIds: taxonConstraints.values()) {
            taxIds.addAll(iterateTaxIds);
        }
        
        if (taxIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("The taxon constraints provided "
                    + "did not allow to retrieve taxon IDs"));
        }
        return log.traceExit(taxIds);
    }

    /**
     * Extracts taxon constraints from the file {@code taxonConstraintsFile}. 
     * The returned {@code Map} contains the OBO-like IDs of all Uberon terms 
     * present in the file, as keys, associated to a {@code Set} of {@code Integer}s, 
     * that are the IDs of the taxa in which it exists, among the taxa present in the file. 
     * If the {@code Set} is empty, then it means that the {@code OWLClass} existed 
     * in none of the taxa. IDs of the taxa are {@code Integer}s representing 
     * their NCBI IDs (for instance, 9606 for human).
     * 
     * @param taxonConstraintsFile          A {@code String} that is the path to the 
     *                                      taxon constraints file.
     * @return                          A {@code Map} where keys are IDs of the Uberon 
     *                                  {@code OWLClass}es, and values are {@code Set}s 
     *                                  of {@code Integer}s containing the IDs of taxa 
     *                                  in which the {@code OWLClass} exists.
     * @throws FileNotFoundException    If {@code taxonConstraintsFile} could not 
     *                                  be found.
     * @throws IOException              If {@code taxonConstraintsFile} could not 
     *                                  be read.
     */
    //TODO: unit test with non-null allClassIds
    public static Map<String, Set<Integer>> extractTaxonConstraints(String taxonConstraintsFile)
            throws FileNotFoundException, IOException {
        log.traceEntry("{}", taxonConstraintsFile);

        Map<String, Set<Integer>> constraints = new HashMap<String, Set<Integer>>();
        
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

            Map<String, Object> lineMap;
            while( (lineMap = mapReader.read(header, processors)) != null ) {
                
                String uberonId = (String) lineMap.get(UBERON_ID_COLUMN_NAME);

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

        return log.traceExit(constraints);
    }
    
    public static class TaxonTaxonConstraintsBean {
        
        private String uberonId;
        private String uberonName;
        private String taxonIdWithConstraints;
        private String taxonNameWithConstraints;
        private String taxonIdWithoutConstraints;
        private String taxonNameWithoutConstraints;
        private String description;
        
        public TaxonTaxonConstraintsBean() {}
        
        public TaxonTaxonConstraintsBean(String uberonId, String uberonName, String taxonIdWithConstraints,
                String taxonNameWithConstraints, String taxonIdWithoutConstraints, 
                String taxonNameWithoutConstraints, String description) {
            this.uberonId = uberonId;
            this.uberonName = uberonName;
            this.taxonIdWithConstraints = taxonIdWithConstraints;
            this.taxonNameWithConstraints = taxonNameWithConstraints;
            this.taxonIdWithoutConstraints = taxonIdWithoutConstraints;
            this.taxonNameWithoutConstraints = taxonNameWithoutConstraints;
            this.description = description;
        }

        //GETTERS
        public String getUberonId() {
            return uberonId;
        }

        public void setUberonId(String uberonId) {
            this.uberonId = uberonId;
        }

        public String getUberonName() {
            return uberonName;
        }

        public void setUberonName(String uberonName) {
            this.uberonName = uberonName;
        }

        public String getTaxonIdWithConstraints() {
            return taxonIdWithConstraints;
        }

        public void setTaxonIdWithConstraints(String taxonIdWithConstraints) {
            this.taxonIdWithConstraints = taxonIdWithConstraints;
        }

        public String getTaxonNameWithConstraints() {
            return taxonNameWithConstraints;
        }

        public void setTaxonNameWithConstraints(String taxonNameWithConstraints) {
            this.taxonNameWithConstraints = taxonNameWithConstraints;
        }

        public String getTaxonIdWithoutConstraints() {
            return taxonIdWithoutConstraints;
        }

        public void setTaxonIdWithoutConstraints(String taxonIdWithoutConstraints) {
            this.taxonIdWithoutConstraints = taxonIdWithoutConstraints;
        }

        public String getTaxonNameWithoutConstraints() {
            return taxonNameWithoutConstraints;
        }

        public void setTaxonNameWithoutConstraints(String taxonNameWithoutConstraints) {
            this.taxonNameWithoutConstraints = taxonNameWithoutConstraints;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return "TaxonTaxonConstraintsBean [uberonId=" + uberonId + ", uberonName=" + uberonName
                    + ", taxonIdWithConstraints=" + taxonIdWithConstraints + ", taxonNameWithConstraints="
                    + taxonNameWithConstraints + ", taxonIdWithoutConstraints=" + taxonIdWithoutConstraints
                    + ", taxonNameWithoutConstraints=" + taxonNameWithoutConstraints + ", description=" + description
                    + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((description == null) ? 0 : description.hashCode());
            result = prime * result + ((taxonIdWithConstraints == null) ? 0 : taxonIdWithConstraints.hashCode());
            result = prime * result + ((taxonIdWithoutConstraints == null) ? 0 : taxonIdWithoutConstraints.hashCode());
            result = prime * result + ((taxonNameWithConstraints == null) ? 0 : taxonNameWithConstraints.hashCode());
            result = prime * result
                    + ((taxonNameWithoutConstraints == null) ? 0 : taxonNameWithoutConstraints.hashCode());
            result = prime * result + ((uberonId == null) ? 0 : uberonId.hashCode());
            result = prime * result + ((uberonName == null) ? 0 : uberonName.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            TaxonTaxonConstraintsBean other = (TaxonTaxonConstraintsBean) obj;
            if (description == null) {
                if (other.description != null)
                    return false;
            } else if (!description.equals(other.description))
                return false;
            if (taxonIdWithConstraints == null) {
                if (other.taxonIdWithConstraints != null)
                    return false;
            } else if (!taxonIdWithConstraints.equals(other.taxonIdWithConstraints))
                return false;
            if (taxonIdWithoutConstraints == null) {
                if (other.taxonIdWithoutConstraints != null)
                    return false;
            } else if (!taxonIdWithoutConstraints.equals(other.taxonIdWithoutConstraints))
                return false;
            if (taxonNameWithConstraints == null) {
                if (other.taxonNameWithConstraints != null)
                    return false;
            } else if (!taxonNameWithConstraints.equals(other.taxonNameWithConstraints))
                return false;
            if (taxonNameWithoutConstraints == null) {
                if (other.taxonNameWithoutConstraints != null)
                    return false;
            } else if (!taxonNameWithoutConstraints.equals(other.taxonNameWithoutConstraints))
                return false;
            if (uberonId == null) {
                if (other.uberonId != null)
                    return false;
            } else if (!uberonId.equals(other.uberonId))
                return false;
            if (uberonName == null) {
                if (other.uberonName != null)
                    return false;
            } else if (!uberonName.equals(other.uberonName))
                return false;
            return true;
        }
        
        
        
        
    }
}
