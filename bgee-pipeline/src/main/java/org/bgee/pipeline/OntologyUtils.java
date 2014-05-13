package org.bgee.pipeline;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.obolibrary.oboformat.writer.OBOFormatWriter;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;

import owltools.graph.OWLGraphWrapper;
import owltools.io.ParserWrapper;

/**
 * This class provides convenient methods when to use or analyze an {@code OWLOntology}. 
 * The {@code OWLOntology} which operations must be performed on should be 
 * directly provided at instantiation, or wrapped into an {@code OWLGraphWrapper}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class OntologyUtils {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(OntologyUtils.class.getName());

    //****************************
    // OBJECT PROPERTIES
    //****************************
    /**
     * A {@code String} that is the OBO-like ID of the {@code part_of} relation.
     */
    public final static String PART_OF_ID = "BFO:0000050";
    /**
     * {@code IRI} of the {@code OWLObjectProperty} "developmentally_related_to".
     */
    public final static IRI DEVELOPMENTALLY_RELATED_TO_IRI = 
            IRI.create("http://purl.obolibrary.org/obo/RO_0002324");
    /**
     * A {@code String} that is the OBO-like ID of the {@code develops_from} relation.
     */
    public final static String DEVELOPS_FROM_ID = "RO:0002202";
    /**
     * {@code IRI} of the {@code OWLObjectProperty} "transformation_of".
     */
    public final static String TRANSFORMATION_OF_ID = 
            "http://semanticscience.org/resource/SIO_000657";
    /**
     * {@code IRI} of the {@code OWLObjectProperty} "in_taxon".
     */
    public final static IRI IN_TAXON_IRI = 
            IRI.create("http://purl.obolibrary.org/obo/RO_0002162");
    /**
     * {@code IRI} of the {@code OWLObjectProperty} "onl_in_taxon" (which is 
     * a sub-property of "in_taxon", see {@link #IN_TAXON_IRI}).
     */
    public final static IRI ONLY_IN_TAXON_IRI = 
            IRI.create("http://purl.obolibrary.org/obo/RO_0002160");
    /**
     * {@code IRI} of the {@code OWLObjectProperty} "evolved_multiple_times_in".
     */
    public final static IRI EVOLVED_MULTIPLE_TIMES_IRI = 
            IRI.create("http://purl.obolibrary.org/obo/uberon/core#evolved_multiple_times_in");
    //****************************
    // ANNOTATION PROPERTIES
    //****************************
    /**
     * {@code IRI} of the {@code OWLAnnotationProperty} "ambiguous_for_taxon".
     */
    public final static IRI AMBIGUOUS_FOR_TAXON_IRI = 
            IRI.create("http://purl.obolibrary.org/obo/uberon/core#ambiguous_for_taxon");
    /**
     * {@code IRI} of the {@code OWLAnnotationProperty} "dubious_for_taxon".
     */
    public final static IRI DUIOUS_FOR_TAXON_IRI = 
            IRI.create("http://purl.obolibrary.org/obo/uberon/core#dubious_for_taxon");
    /**
     * {@code IRI} of the {@code OWLAnnotationProperty} "homologous_in".
     */
    public final static IRI HOMOLOGOUS_IN_IRI = 
            IRI.create("http://purl.obolibrary.org/obo/uberon/core#homologous_in");
    /**
     * {@code IRI} of the {@code OWLAnnotationProperty} "never_in_taxon".
     */
    public final static IRI NEVER_IN_TAXON_IRI = 
            IRI.create("http://www.geneontology.org/formats/oboInOwl#never_in_taxon");
    /**
     * {@code IRI} of the {@code OWLAnnotationProperty} "never_in_taxon" (ID RO:0002161), 
     * different from {@link #NEVER_IN_TAXON_IRI}.
     */
    public final static IRI NEVER_IN_TAXON_BIS_IRI = 
            IRI.create("http://purl.obolibrary.org/obo/RO_0002161");
    /**
     * {@code IRI} of the {@code OWLAnnotationProperty} "present_in_taxon".
     */
    public final static IRI PRESENT_IN_TAXON_IRI = 
            IRI.create("http://purl.obolibrary.org/obo/uberon/core#present_in_taxon");
    /**
     * {@code IRI} of the {@code OWLAnnotationProperty} "taxon".
     */
    public final static IRI TAXON_IRI = 
            IRI.create("http://www.geneontology.org/formats/oboInOwl#taxon");
    /**
     * {@code IRI} of the {@code OWLAnnotationProperty} 
     * "treat-xrefs-as-reverse-genus-differentia".
     */
    public final static IRI GENUS_DIFFERENTIA_IRI = 
            IRI.create("http://www.geneontology.org/formats/oboInOwl#" +
            		"treat-xrefs-as-reverse-genus-differentia");
    /**
     * The {@code Pattern} used to parse the {@code OWLLiteral} associated to 
     * <i>genus-differentia</i> {@code AnnotationProperty} (see 
     * {@link #GENUS_DIFFERENTIA_IRI}). This {@code Pattern} allows to capture 2 groups: 
     * the first group is the prefix of the anatomical ontology annotated, 
     * the second group is the OBO-like ID of the taxon scoped. For instance, 
     * if we have "AAO part_of NCBITaxon:8292", the first group will be "AAO", 
     * the second will be "NCBITaxon:8292".
     */
    public final static Pattern GENUS_DIFFERENTIA_LITERAL_PATTERN = 
            Pattern.compile("^(.+?) part_of (.+?)$");
    /**
     * An {@code int} that is the index of the group capturing the taxon ID 
     * in the {@code Pattern} {@link #GENUS_DIFFERENTIA_LITERAL_PATTERN}.
     */
    public final static int GENUS_DIFFERENTIA_TAXON_GROUP = 2;
    /**
     * An {@code int} that is the index of the group capturing the anatomical ontology prefix 
     * in the {@code Pattern} {@link #GENUS_DIFFERENTIA_LITERAL_PATTERN}.
     */
    public final static int GENUS_DIFFERENTIA_ANAT_GROUP = 1;

    /**
     * A {@code String} representing the key to obtain left bound value 
     * of a taxon, in the {@code Map} storing parameters of the nested set model.
     * @see #computeNestedSetModelParams()
     * @see #computeNestedSetModelParams(List)
     */
    public static final String LEFT_BOUND_KEY = "left";
    /**
     * A {@code String} representing the key to obtain right bound value 
     * of a taxon, in the {@code Map} storing parameters of the nested set model.
     * @see #computeNestedSetModelParams()
     * @see #computeNestedSetModelParams(List)
     */
    public static final String RIGHT_BOUND_KEY = "right";
    /**
     * A {@code String} representing the key to obtain level value 
     * of a taxon, in the {@code Map} storing parameters of the nested set model.
     * @see #computeNestedSetModelParams()
     * @see #computeNestedSetModelParams(List)
     */
    public static final String LEVEL_KEY = "level";

    /**
     * A {@code String} that is the prefix to add to the NCBI taxonomy IDs 
     * (that are {@code Integer}s) to obtain IDs used in the taxonomy ontology. 
     * For instance, if a taxon has the ID {@code 9606} on the NCBI taxonomy website, 
     * it will have the ID {@code NCBITaxon:9606} in the ontology file.
     */
    private static final String TAX_ONTOLOGY_ID_PREFIX = "NCBITaxon:";

    /**
     * A {@code Set} of {@code String}s that are the string representations of the {@code IRI}s 
     * of {@code OWLAnnotationProperty}s to discard, to simplify the export in OBO. 
     * Most are annotations in OWL that are translated into relationships in OBO, 
     * or EquivalentClassesAxioms between owl:nothing and a class, translated into 
     * relationships, thus disturbing the graph structure.
     * 
     * @see #removeOBOProblematicAxioms()
     */
    private final static Set<String> discardedAnnotProps = 
            new HashSet<String>(Arrays.asList("http://xmlns.com/foaf/0.1/depicted_by", 
                    "http://purl.obolibrary.org/obo/RO_0002175", //present_in_taxon
                    "http://purl.obolibrary.org/obo/RO_0002161", //never_in_taxon
                    "http://purl.obolibrary.org/obo/RO_0002171", //mutually_spatially_disjoint_with
                    "http://purl.obolibrary.org/obo/RO_0002475", //has_no_connections_with
                    "http://purl.obolibrary.org/obo/RO_0002174", //dubious_for_taxon
                    "http://purl.obolibrary.org/obo/RO_0002173"));//ambiguous_for_taxon
    
    /**
     * Loads the ontology stored in the file {@code ontFile} and returns it 
     * as an {@code OWLOntology}.
     * 
     * @param ontFile   A {@code String} that is the path to the ontology file.
     * @return          An {@code OWLOntology} loaded from {@code ontFile}.
     * @throws OWLOntologyCreationException If an error occurred while loading 
     *                                      the ontology.
     * @throws OBOFormatParserException     If the file could not be parsed correctly.
     * @throws IOException                  If the file could not be read.
     */
    public static OWLOntology loadOntology(String ontFile) throws OWLOntologyCreationException, 
        OBOFormatParserException, IOException {
        ParserWrapper parserWrapper = new ParserWrapper();
        parserWrapper.setCheckOboDoc(false);
        return parserWrapper.parse(ontFile);
    }
    
    /**
     * Transforms a NCBI ID (which are integers, for instance, {@code 9606} for human) 
     * into the equivalent ID used in the generated taxonomy ontology (which are 
     * strings with a prefix).
     * 
     * @param ncbiId    An {@code int} that is the ID of a taxon or species as used 
     *                  on the NCBI website.
     * @return          A {@code String} that is the corresponding ID as used in 
     *                  the taxonomy ontology.
     */
    public static String getTaxOntologyId(int ncbiId) {
        return TAX_ONTOLOGY_ID_PREFIX + ncbiId;
    }
    /**
     * Transform the ID of a taxonomy term in the generated ontology (which are strings 
     * with a given prefix) into the equivalent ID used on the NCBI website (which 
     * are integers with no prefix).
     * 
     * @param ontologyTermId    A {@code String} that is the ID of a term in 
     *                          the taxonomy ontology.
     * @return                  An {@code int} that is the corresponding ID 
     *                          on the NCBI website. 
     */
    public static int getTaxNcbiId(String ontologyTermId) {
        return Integer.parseInt(ontologyTermId.substring(TAX_ONTOLOGY_ID_PREFIX.length()));
    }
    /**
     * Convert {@code taxNcbiIds} containing NCBI IDs (which are integers, 
     * for instance, {@code 9606} for human) into a {@code Set} of {@code String}s 
     * containing the equivalent IDs used in the generated taxonomy ontology (which are 
     * strings with a prefix).
     * 
     * @param taxNcbiIds    A {@code Collection} of {@code Integer}s that are the NCBI IDs 
     *                      to convert.
     * @return              A {@code Set} of {@code String}s that are the {@code taxNcbiIds} 
     *                      converted into IDs used in the taxonomy ontology.
     *                      
     */
    public static Set<String> convertToTaxOntologyIds(Collection<Integer> taxNcbiIds) {
        log.entry(taxNcbiIds);
        Set<String> ontTaxonIds = new HashSet<String>();
        for (int ncbiId: taxNcbiIds) {
            ontTaxonIds.add(OntologyUtils.getTaxOntologyId(ncbiId));
        }
        return log.exit(ontTaxonIds);
    }
    /**
     * Convert {@code taxOntIds} containing taxonomy ontology IDs (which are strings, 
     * for instance, {@code NCBITaxon:9606} for human) into a {@code Set} of {@code Integer}s 
     * containing the equivalent IDs used in NCBI (for instance, {@code 9606} for human).
     * 
     * @param taxOntIds     A {@code Collection} of {@code String}s that are the taxonomy IDs 
     *                      with a prefix, to convert.
     * @return              A {@code Set} of {@code Integer}s that are the {@code taxOntIds} 
     *                      converted into IDs used in NCBI.
     *                      
     */
    public static Set<Integer> convertToNcbiIds(Collection<String> taxOntIds) {
        log.entry(taxOntIds);
        Set<Integer> convertIds = new HashSet<Integer>();
        for (String id: taxOntIds) {
            convertIds.add(OntologyUtils.getTaxNcbiId(id));
        }
        return log.exit(convertIds);
    }

    /**
     * The {@code OWLGraphWrapper} wrapping the {@code OWLOntology} which operations 
     * should be performed on.
     */
    private OWLGraphWrapper wrapper;
    /**
     * The {@code OWLOntology} which operations should be performed on.
     */
    private OWLOntology ontology;
    
    /**
     * Constructor providing the {@code OWLOntology} which operations 
     * should be performed on.
     * @param ontology  the {@code OWLOntology} which operations 
     *                  should be performed on.
     */
    public OntologyUtils(OWLOntology ontology) { 
        this.ontology = ontology;
        this.wrapper = null;
    }
    /**
     * Constructor providing the {@code OWLGraphWrapper} wrapping 
     * the {@code OWLOntology} which operations should be performed on.
     * 
     * @param wrapper   the {@code OWLGraphWrapper} wrapping the {@code OWLOntology} 
     *                  which operations should be performed on.
     */
    public OntologyUtils(OWLGraphWrapper wrapper) {
        this.wrapper = wrapper;
        this.ontology = wrapper.getSourceOntology();
    }
    
    /**
     * Equivalent to calling {@link #computeNestedSetModelParams(List)} 
     * with a {@code null} argument (no specific ordering requested).
     * 
     * @return  See {@link #computeNestedSetModelParams(List)} 
     * @throws IllegalStateException    If the {@code OWLOntology} provided at instantiation 
     *                                  is not a simple tree.
     * @throws UnknownOWLOntologyException      If an {@code OWLGraphWrapper} was not 
     *                                          provided at instantiation, and an error 
     *                                          occurred while loading it.
     * @throws OWLOntologyCreationException     If an {@code OWLGraphWrapper} was not 
     *                                          provided at instantiation, and an error 
     *                                          occurred while loading it.
     * @see #computeNestedSetModelParams(List)
     */
    //TODO adapt as in org.bgee.pipeline.hierarchicalGroups.ParseOrthoXML.buildNestedSet(Node)
    public Map<OWLClass, Map<String, Integer>> computeNestedSetModelParams() 
            throws UnknownOWLOntologyException, IllegalStateException, 
            OWLOntologyCreationException {
        log.entry();
        return log.exit(this.computeNestedSetModelParams(null));
    }
    

    /**
     * Compute parameters to represent the {@code OWLOntology} provided at instantiation 
     * as a nested set model. This method returns a {@code Map} where each 
     * {@code OWLClass} of the ontology is a key, associated to a {@code Map} 
     * storing its left bound, right bound, and level. These values can be retrieved 
     * by using respectively the keys {@link #LEFT_BOUND_KEY}, {@link #RIGHT_BOUND_KEY}, 
     * and {@link #LEVEL_KEY}.
     * <p>
     * The argument {@code classOrder} allows to order the children of a same 
     * {@code OWLClass} according to their order in {@code classOrder}. If {@code null} 
     * or empty, children of an {@code OWLClass} will be ordered as they are retrieved.
     * <p>
     * If the {@code OWLOntology} provided at instantiation is not a simple tree 
     * that can be represented as a nested set model, an {@code IllegalStateException} 
     * is thrown. 
     * 
     * @param classOrder    A {@code List} that is used to order children 
     *                      of a same {@code OWLClass}. If {@code null} or empty, 
     *                      no specific order is specified.
     * @return              A {@code Map} associating {@code OWLClass}es of the ontology 
     *                      to a {@code Map} containing their left bound, right bound, 
     *                      and level.
     * @throws IllegalStateException    If the {@code OWLOntology} provided at instantiation 
     *                                  is not a simple tree.
     * @throws UnknownOWLOntologyException      If an {@code OWLGraphWrapper} was not 
     *                                          provided at instantiation, and an error 
     *                                          occurred while loading it.
     * @throws OWLOntologyCreationException     If an {@code OWLGraphWrapper} was not 
     *                                          provided at instantiation, and an error 
     *                                          occurred while loading it.
     */
    public Map<OWLClass, Map<String, Integer>> computeNestedSetModelParams(
            List<OWLClass> classOrder) throws IllegalStateException, UnknownOWLOntologyException, 
            OWLOntologyCreationException {
        log.entry(classOrder);
        
        Map<OWLClass, Map<String, Integer>> params = 
                new HashMap<OWLClass, Map<String, Integer>>();
        //get the root of the ontology, that should be unique.
        Set<OWLClass> roots = this.getWrapper().getOntologyRoots();
        if (roots.size() != 1) {
            throw log.throwing(new IllegalStateException("Incorrect number of roots " +
                    "in the taxonomy ontology"));
        }
        OWLClass root = roots.iterator().next();
        //we need to initialize the parameters for this root
        //right bound yet to be determined, after iterating all children
        params.put(root, this.getOWLClassNestedSetModelParams(1, 0, 1));
        //params will be populated along the walk
        this.recursiveNestedSetModelParams(params, root, classOrder);
        
        return log.exit(params);
    }
    
    /**
     * A recursive method use to walk the {@code OWLOntology} wrapped into 
     * {@link #wrapper}, to compute the parameters allowing to represent the ontology 
     * as a nested set model. This method is first called by {@link 
     * #computeNestedSetModelParams(List)} by providing the {@code OWLClass} 
     * root of the ontology, an initialized {@code Map} to store the parameters, 
     * and possibly a {@code List} to order {@code OWLClass}es. Following 
     * this first call, all the ontology will be recursively walked, and {@code params} 
     * filled with data along the way. 
     * 
     * @param params            The {@code Map} allowing to store computed parameters, 
     *                          that will be populated along the recursive calls. 
     *                          See returned value of {@link 
     *                          #computeNestedSetModelParams(List)} for 
     *                          a description.
     * @param classInspected    Current {@code OWLClass} walked, for which children 
     *                          will be iterated. 
     * @param classOrder        A {@code List} allowing to order children 
     *                          of a same {@code OWLClass}, if not {@code null} 
     *                          nor empty.
     * @throws IllegalStateException    If the {@code OWLOntology} wrapped into 
     *                                  {@link #wrapper} is not a simple tree that 
     *                                  can be represented by a nested set model.
     * @throws UnknownOWLOntologyException      If an {@code OWLGraphWrapper} was not 
     *                                          provided at instantiation, and an error 
     *                                          occurred while loading it.
     * @throws OWLOntologyCreationException     If an {@code OWLGraphWrapper} was not 
     *                                          provided at instantiation, and an error 
     *                                          occurred while loading it.
     */
    private void recursiveNestedSetModelParams(
            final Map<OWLClass, Map<String, Integer>> params, 
            final OWLClass classInspected,  final List<OWLClass> classOrder) 
        throws IllegalStateException, UnknownOWLOntologyException, OWLOntologyCreationException {
        log.entry(params, classInspected, classOrder);
        
        //we will iterate children of classInspected. 
        Set<OWLClass> children = this.getWrapper().getOWLClassDirectDescendants(classInspected);
        
        //if classOrder is not null nor empty, we use it to order the children. 
        if (classOrder != null && !classOrder.isEmpty()) {
            //So we use a comparator. The comparator will net be consistent with 
            //the equals method if classOrder contains duplicated elements, 
            //but it should not be a problem.
            TreeSet<OWLClass> sortedChildren = 
              new TreeSet<OWLClass>(new Comparator<OWLClass>() {
                @Override
                public int compare(OWLClass o1, OWLClass o2) {
                    //solution to get index from http://stackoverflow.com/a/7911697/1768736
                    return classOrder.indexOf(o1) - classOrder.indexOf(o2);
                }
              });
            sortedChildren.addAll(children);
            children = sortedChildren;
        }
        
        //leftBound and level of classInspected are already set before this method call; 
        //its rightBound is yet to be determined
        int leftBound = params.get(classInspected).get(LEFT_BOUND_KEY);
        int level = params.get(classInspected).get(LEVEL_KEY);
        
        //by default, if classInspected has no children, its right bound is equal 
        //to its left bound + 1.
        int rightBound = leftBound + 1;
        //the left bound of the first child (if any) should be the classInspected 
        //left bound + 1
        int currentChildLeftBound = leftBound + 1;
        for (OWLClass child: children) {
            //OWLClass already seen, the ontology is not a simple tree
            if (params.containsKey(child)) {
                throw log.throwing(new IllegalStateException("The OWLOntology is not " +
                        "a simple tree that can be represented as a nested set model."));
            }
            
            //storing parameters for the current child;
            //right bound yet to be determined, after iterating all its children
            params.put(child, 
                this.getOWLClassNestedSetModelParams(currentChildLeftBound, 0, level + 1));
            //recursive call, will walk all children of current child
            this.recursiveNestedSetModelParams(params, child, classOrder);
            //now we can get its right bound, to infer the left bound of the next child, 
            //or the right bound of classInspected if this is its last child
            int childRightBound = params.get(child).get(RIGHT_BOUND_KEY);
            currentChildLeftBound = childRightBound + 1;
            rightBound = childRightBound + 1;
        }
        
        log.trace("Done inspecting children for class {}, computed right bound: {}", 
                classInspected, rightBound);
        params.get(classInspected).put(RIGHT_BOUND_KEY, rightBound);
        
        
        log.exit();
    }

    /**
     * Returns a newly instantiated {@code Map} where {@code leftBound}, {@code rightBound}, 
     * and {@code level} are associated as values to their respective key 
     * {@link #LEFT_BOUND_KEY}, {@link #RIGHT_BOUND_KEY}, and {@link #LEVEL}.
     * 
     * @param leftBound     The {@code int} to be associated to {@link #LEFT_BOUND_KEY} 
     *                      in the returned {@code Map}.
     * @param rightBound    The {@code int} to be associated to {@link #RIGHT_BOUND_KEY} 
     *                      in the returned {@code Map}.
     * @param level         The {@code int} to be associated to {@link #LEVEL} 
     *                      in the returned {@code Map}.
     * @return  a newly instantiated {@code Map} containing the provided arguments.
     */
    private Map<String, Integer> getOWLClassNestedSetModelParams(int leftBound, 
            int rightBound, int level) {
        Map<String, Integer> params = new HashMap<String, Integer>();
        params.put(LEFT_BOUND_KEY, leftBound);
        params.put(RIGHT_BOUND_KEY, rightBound);
        params.put(LEVEL_KEY, level);
        return params;
    }
    
    /**
     * Saves the {@code OWLOntology} wrapped by this object into {@code outputFile} 
     * in OBO format.
     * 
     * @param outputFile    A {@code String} that is the path to the file to save 
     *                      the ontology in OBO format.
     * @throws OWLOntologyCreationException If the ontology could not be converted 
     *                                      to OBO.
     * @throws IOException                  If an error occurred while writing 
     *                                      in the file.
     * @throws IllegalArgumentException     if {@code outputFile} does not have 
     *                                      a correct name.
     */
    public void saveAsOBO(String outputFile) throws OWLOntologyCreationException, 
        IOException, IllegalArgumentException {
        log.entry(outputFile);

        if (!outputFile.endsWith(".obo")) {
            throw log.throwing(new IllegalArgumentException("The output file must be " +
                    "an OBO format: " + outputFile));
        }
        
        Owl2Obo converter = new Owl2Obo();
        OBODoc oboOntology = converter.convert(this.ontology);
        OBOFormatWriter writer = new OBOFormatWriter();
        writer.setCheckStructure(true);
        writer.write(oboOntology, outputFile);
        
        log.exit();
    }
    
    /**
     * Saves the {@code OWLOntology} wrapped by this object into {@code outputFile} 
     * in OWL format.
     * 
     * @param outputFile    A {@code String} that is the path to the file to save 
     *                      the ontology in OWL format.
     * @throws OWLOntologyStorageException  If an error occurred while saving 
     *                                      the ontology.
     * @throws IllegalArgumentException     if {@code outputFile} does not have 
     *                                      a correct name.
     */
    public void saveAsOWL(String outputFile) throws IllegalArgumentException, 
        OWLOntologyStorageException {
        log.entry(outputFile);

        if (!outputFile.endsWith(".owl")) {
            throw log.throwing(new IllegalArgumentException("The output file must be " +
                    "an OWL format: " + outputFile));
        }
        
        File rdfFile = new File(outputFile); 
        OWLOntologyManager manager = this.ontology.getOWLOntologyManager();
        RDFXMLOntologyFormat owlRdfFormat = new RDFXMLOntologyFormat();
        if (owlRdfFormat.isPrefixOWLOntologyFormat()) {
            owlRdfFormat.copyPrefixesFrom(owlRdfFormat.asPrefixOWLOntologyFormat());
        } 
        manager.saveOntology(this.ontology, 
                owlRdfFormat, IRI.create(rdfFile.toURI()));
        
        log.exit();
    }
    
    /**
     * Modifies the {@code OWLOntology} wrapped by this object to remove 
     * {@code OWLAnnotationAssertionAxiom}s that are problematic to convert 
     * the ontology in OBO format.
     * 
     * @throws OWLOntologyCreationException If an error occurred when using an 
     *                                      {@code OWLGraphWrapper} on the {@code OWLOntology} 
     *                                      wrapped by this object
     * @throws UnknownOWLOntologyException  If an error occurred when using an 
     *                                      {@code OWLGraphWrapper} on the {@code OWLOntology} 
     *                                      wrapped by this object
     */
    public void removeOBOProblematicAxioms() throws UnknownOWLOntologyException, 
        OWLOntologyCreationException {
        log.entry();
        
        for (OWLAnnotationAssertionAxiom ax: 
            this.ontology.getAxioms(AxiomType.ANNOTATION_ASSERTION)) {
            if (discardedAnnotProps.contains(
                    ax.getProperty().getIRI().toString()) || 
                    discardedAnnotProps.contains(
                            this.getWrapper().getIdentifier(
                                    ax.getProperty()))) {
                this.ontology.getOWLOntologyManager().removeAxiom(this.ontology, ax);
                log.debug("Discarded annotation: " + ax);
            }
        }
    }
    
    /**
     * Return the {@code OWLGraphWrapper} wrapping the ontology on which operations 
     * should be performed. If not provided at instantiation, it will be automatically 
     * loaded the first time this method is called, from the {@code OWLOntology} provided 
     * at instantiation.
     * 
     * @return  the {@code OWLGraphWrapper} wrapping the ontology on which operations 
     *          should be performed.
     * @throws UnknownOWLOntologyException      If an {@code OWLGraphWrapper} was not 
     *                                          provided at instantiation, and an error 
     *                                          occurred while loading it.
     * @throws OWLOntologyCreationException     If an {@code OWLGraphWrapper} was not 
     *                                          provided at instantiation, and an error 
     *                                          occurred while loading it.
     */
    private OWLGraphWrapper getWrapper() throws UnknownOWLOntologyException, 
        OWLOntologyCreationException {
        if (this.wrapper == null) {
            this.wrapper = new OWLGraphWrapper(this.ontology);
        }
        return this.wrapper;
    }
}
