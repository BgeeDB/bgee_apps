package org.bgee.pipeline;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.uberon.OWLGraphManipulator;
import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.obolibrary.oboformat.writer.OBOFormatWriter;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;
import org.semanticweb.owlapi.model.UnloadableImportException;

import owltools.graph.OWLGraphEdge;
import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLQuantifiedProperty;
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
    
    /**
     * An unmodifiable {@code Set} of {@code String}s that are the names 
     * of non-informative subsets in Uberon.
     */
    public final static Set<String> NON_INFORMATIVE_SUBSETS = Collections.unmodifiableSet(
            new HashSet<String>(Arrays.asList("grouping_class", "non_informative", 
                    "ubprop:upper_level", "upper_level")));

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
    public final static String TRANSFORMATION_OF_ID = "RO:0002494";
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
    /**
     * A {@code String} that is the OBO-like ID of the {@code preceded_by} relation.
     */
    public final static String PRECEDED_BY_ID = "BFO:0000062";
    /**
     * A {@code String} that is the OBO-like ID of the {@code immediately_preceded_by} relation.
     */
    public final static String IMMEDIATELY_PRECEDED_BY_ID = "RO:0002087";
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
     * A {@code Comparator} allowing to order OBO-like IDs in ontologies according to 
     * their natural ordering. This can be used for instance with the method 
     * {@code Collections.sort} to order a {@code List} of OBO-like IDs.
     */
    public static final Comparator<String> ID_COMPARATOR = new Comparator<String>() {
        //match classical IDs, e.g. ID:00001, but also weird IDs, e.g. Flybase:FBgn_00001, 
        //and IDs with digits in their prefix, e.g. EHDAA2:00001, and with letters 
        //in their numeric part, e.g. AEO:0000119f (last letter note taken into account 
        //in such cases)
        private final Pattern ID_PATTERN = Pattern.compile("^(.+?\\D)([0-9]+?)\\D*$");
        @Override
        public int compare(String id1, String id2) {
            
            Matcher m1 = ID_PATTERN.matcher(id1);
            Matcher m2 = ID_PATTERN.matcher(id2);
            if (!m1.matches()) {
                throw log.throwing(new IllegalArgumentException("Malformed ID in ontology, " +
                        "could not be parsed: " + id1));
            }
            if (!m2.matches()) {
                throw log.throwing(new IllegalArgumentException("Malformed ID in ontology, " +
                        "could not be parsed: " + id2));
            }
            
            String prefix1 = m1.group(1);
            String prefix2 = m2.group(1);
            int numeric1 = Integer.parseInt(m1.group(2));
            int numeric2 = Integer.parseInt(m2.group(2));
            
            if (!prefix1.equals(prefix2)) {
                return prefix1.compareTo(prefix2);
            }
            return (numeric1-numeric2);
        }
      };

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
            new HashSet<String>(Arrays.asList(
                    "http://www.w3.org/2000/01/rdf-schema#seeAlso",
                    "http://xmlns.com/foaf/0.1/depicted_by", 
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
        try {
            return parserWrapper.parse(ontFile);
        } catch(UnloadableImportException e) {
            //we sometimes have the problem that an import ontology cannot be accessed 
            //because of network errors. In that case, we retry 3 times before throwing 
            //the exception
            log.catching(e);
            log.debug("Error while importing ontologies, trying again...");
            for (int i = 0; i < 3; i++) {
                try {
                    //wait 1 sec. before retrying
                    Thread.sleep(1000);
                    return parserWrapper.parse(ontFile);
                } catch(UnloadableImportException e2) {
                    //do nothing here
                    log.catching(e2);
                } catch(InterruptedException ex) {
                    //propagate the interruption flag
                    Thread.currentThread().interrupt();
                }
            }
            
            throw log.throwing(e);
        }
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
     * The {@code OWLGraphManipulator} wrapping the {@code OWLOntology} which operations 
     * should be performed on to modify it.
     */
    private OWLGraphManipulator manipulator;
    /**
     * The {@code OWLOntology} which operations should be performed on.
     */
    private OWLOntology ontology;
    /**
     * A {@code Map} storing the XRef mappings lazy loaded by the method 
     * {@link #getXRefMappings()}. See this method for details.
     * @see #getXRefMappings()
     */
    private Map<String, Set<String>> xRefMappings;
    /**
     * A {@code Map} storing the mappings from OBO-like IDs of obsolete {@code OWLClass}es 
     * to IDs associated to their {@code consider} annotation. It is lazy loaded by the method 
     * {@link #getConsiderMappings()}. See this method for details.
     * @see #getConsiderMappings()
     */
    private Map<String, Set<String>> considerMappings;
    /**
     * A {@code Map} storing the mappings from OBO-like IDs of obsolete {@code OWLClass}es 
     * to IDs associated to their {@code replaced_by} annotation. It is lazy loaded by the method 
     * {@link #getReplacedByMappings()}. See this method for details.
     * @see #getReplacedByMappings()
     */
    private Map<String, Set<String>> replacedByMappings;
    /**
     * A {@code Set} of {@code OWLObjectPropertyExpression}s containing 
     * the sub-properties of the "part_of" property (for instance, "in_deep_part_of"), 
     * and the "part_of" property itself (see {@link #PART_OF_ID}).
     * <p>
     * This attribute is lazy loaded when {@link #getIsAPartOfOutgoingEdges(OWLObject)} 
     * is called. 
     * 
     * @see #getIsAPartOfOutgoingEdges(OWLObject)
     */
    private Set<OWLObjectPropertyExpression> partOfRels;
    /**
     * A {@code Set} of {@code OWLObjectPropertyExpression}s containing 
     * the sub-properties of the "preceded_by" property (for instance, "immediately_preceded_by"), 
     * and the "preceded_by" property itself (see {@link #PRECEDED_BY_ID}).
     * <p>
     * This attribute is lazy loaded when {@link #isPrecededByRelation(OWLGraphEdge)} 
     * is called. 
     * 
     * @see #isPrecededByRelation(OWLGraphEdge)
     */
    private Set<OWLObjectPropertyExpression> precededByRels;
    
    /**
     * Constructor providing the {@code OWLOntology} which operations 
     * should be performed on.
     * @param ontology  the {@code OWLOntology} which operations 
     *                  should be performed on.
     */
    public OntologyUtils(OWLOntology ontology) { 
        this.ontology = ontology;
        this.wrapper = null;
        this.xRefMappings = null;
        this.partOfRels = null;
        this.precededByRels = null;
    }
    /**
     * Constructor providing the {@code OWLGraphWrapper} wrapping 
     * the {@code OWLOntology} which operations should be performed on.
     * 
     * @param wrapper   the {@code OWLGraphWrapper} wrapping the {@code OWLOntology} 
     *                  which operations should be performed on.
     */
    public OntologyUtils(OWLGraphWrapper wrapper) {
        this(wrapper.getSourceOntology());
        this.wrapper = wrapper;
    }
    /**
     * Constructor providing the path to the file storing the ontology  
     * which operations should be performed on. It can be either obo or owl.
     * 
     * @param pathToOntology    A {@code String} that is the path to the ontology. 
     * @throws IOException                  If {@code pathToOntology} could not be read.
     * @throws OBOFormatParserException     If {@code pathToOntology} was in OBO and could not 
     *                                      be parsed.
     * @throws OWLOntologyCreationException If {@code pathToOntology} was in OWL and could not 
     *                                      be parsed.
     */
    public OntologyUtils(String pathToOntology) throws OWLOntologyCreationException, 
        OBOFormatParserException, IOException {
        this(OntologyUtils.loadOntology(pathToOntology));
    }
    
    /**
     * Equivalent to calling {@link #computeNestedSetModelParams(List)} 
     * with a {@code null} argument (no specific ordering requested).
     * 
     * @param root  An {@code OWLClass} that will be considered as the root of the ontology 
     *              to start the computations from.
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
    public Map<OWLClass, Map<String, Integer>> computeNestedSetModelParams(OWLClass root) 
            throws UnknownOWLOntologyException, IllegalStateException {
        log.entry(root);
        return log.exit(this.computeNestedSetModelParams(root, null));
    }
    
    /**
     * Delegates to {@link #computeNestedSetModelParams(List, Set)} with the second 
     * {@code Set} argument set to {@code null}.
     * 
     * @param root  An {@code OWLClass} that will be considered as the root of the ontology 
     *              to start the computations from.
     * @param classOrder    See same argument in {@link #computeNestedSetModelParams(List, Set)}.
     * @return              See returned value of {@link #computeNestedSetModelParams(List, Set)}
     * @throws UnknownOWLOntologyException
     */
    public Map<OWLClass, Map<String, Integer>> computeNestedSetModelParams(OWLClass root, 
            List<OWLClass> classOrder) throws UnknownOWLOntologyException {
        log.entry(root, classOrder);
        return log.exit(this.computeNestedSetModelParams(root, classOrder, null));
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
     * @param root  An {@code OWLClass} that will be considered as the root of the ontology 
     *              to start the computations from.
     * @param classOrder    A {@code List} that is used to order children 
     *                      of a same {@code OWLClass}. If {@code null} or empty, 
     *                      no specific order is specified.
     * @param overProps     A {@code Set} of {@code OWLPropertyExpression}s allowing 
     *                      to restrain the relations considered to retrieved direct 
     *                      descendants of {@code OWLClass}es.
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
    public Map<OWLClass, Map<String, Integer>> computeNestedSetModelParams(OWLClass root, 
            List<OWLClass> classOrder, Set<OWLPropertyExpression> overProps) 
                    throws UnknownOWLOntologyException {
        log.entry(root, classOrder, overProps);
        
        Map<OWLClass, Map<String, Integer>> params = 
                new HashMap<OWLClass, Map<String, Integer>>();
        
        if (root == null) {
            //get the root of the ontology, that should be unique.
            Set<OWLClass> roots = this.getWrapper().getOntologyRoots();
            if (roots.size() != 1) {
                throw log.throwing(new IllegalStateException("Incorrect number of roots " +
                        "in the taxonomy ontology"));
            }
            root = roots.iterator().next();
        }
        //we need to initialize the parameters for this root
        //right bound yet to be determined, after iterating all children
        params.put(root, this.getOWLClassNestedSetModelParams(1, 0, 1));
        //params will be populated along the walk
        this.recursiveNestedSetModelParams(params, root, classOrder, overProps);
        
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
     * @param overProps         A {@code Set} of {@code OWLPropertyExpression}s allowing 
     *                          to restrain the relations considered to retrieved direct 
     *                          descendants of {@code OWLClass}es.
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
            final OWLClass classInspected,  final List<OWLClass> classOrder, 
            Set<OWLPropertyExpression> overProps) 
        throws IllegalStateException, UnknownOWLOntologyException {
        log.entry(params, classInspected, classOrder, overProps);
        
        //we will iterate children of classInspected. 
        Set<OWLClass> children = new HashSet<OWLClass>();
        if (overProps != null && !overProps.isEmpty()) {
            for (OWLGraphEdge incomingEdge: this.getWrapper().getIncomingEdges(classInspected)) {
                OWLQuantifiedProperty qp = incomingEdge.getSingleQuantifiedProperty();
                if (incomingEdge.isSourceNamedObject() && 
                    (qp.isSomeValuesFrom() && overProps.contains(qp.getProperty()) || 
                            this.isASubClassOfEdge(incomingEdge))) {
                    
                    children.add((OWLClass) incomingEdge.getSource());
                }
            }
        } else {
            children = this.getWrapper().getOWLClassDirectDescendants(classInspected);
        }
        
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
                        "a simple tree that can be represented as a nested set model. " +
                        "Class already seen: " + child));
            }
            
            //storing parameters for the current child;
            //right bound yet to be determined, after iterating all its children
            params.put(child, 
                this.getOWLClassNestedSetModelParams(currentChildLeftBound, 0, level + 1));
            //recursive call, will walk all children of current child
            this.recursiveNestedSetModelParams(params, child, classOrder, overProps);
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
     * Delegates to {@link #saveAsOBO(String, boolean)}, with the first argument being 
     * {@code outputFile}, and with the boolean argument set to {@code true}.
     * 
     * @param outputFile    See same name argument in {@link #saveAsOBO(String, boolean)}.
     * @see #saveAsOBO(String, boolean)
     * @throws OWLOntologyCreationException
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public void saveAsOBO(String outputFile) 
            throws OWLOntologyCreationException, IOException, IllegalArgumentException {
        log.entry(outputFile);
        this.saveAsOBO(outputFile, true);
        log.exit();
    }
    
    /**
     * Saves the {@code OWLOntology} wrapped by this object into {@code outputFile} 
     * in OBO format.
     * 
     * @param outputFile        A {@code String} that is the path to the file to save 
     *                          the ontology in OBO format.
     * @param checkStructure    A {@code boolean} defining whether structure of the ontology 
     *                          should be checked (see method 
     *                          {@code OBOFormatWriter.setCheckStructure(true)}).
     * @throws OWLOntologyCreationException If the ontology could not be converted 
     *                                      to OBO.
     * @throws IOException                  If an error occurred while writing 
     *                                      in the file.
     * @throws IllegalArgumentException     if {@code outputFile} does not have 
     *                                      a correct name.
     */
    public void saveAsOBO(String outputFile, boolean checkStructure) 
            throws OWLOntologyCreationException, IOException, IllegalArgumentException {
        log.entry(outputFile);

        if (!outputFile.endsWith(".obo")) {
            throw log.throwing(new IllegalArgumentException("The output file must be " +
                    "an OBO format: " + outputFile));
        }
        
        Owl2Obo converter = new Owl2Obo();
        OBODoc oboOntology = converter.convert(this.ontology);
        OBOFormatWriter writer = new OBOFormatWriter();
        writer.setCheckStructure(checkStructure);
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
     * @throws UnknownOWLOntologyException  If an error occurred when using an 
     *                                      {@code OWLGraphWrapper} on the {@code OWLOntology} 
     *                                      wrapped by this object
     */
    public void removeOBOProblematicAxioms() throws UnknownOWLOntologyException {
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
     * Obtains a unmodifiable mapping from all XRef IDs, present in the {@code OWLOntology} 
     * wrapped by this object, to the OBO-like IDs of the {@code OWLClass}es they were 
     * associated to. Each XRef ID can be associated to several OBO-like IDs 
     * of {@code OWLClass}es, to let opened this possibility, even if unlikely. Only 
     * non obsolete {@code OWLClass}es are considered.
     * <p>
     * This mapping is lazy loaded the first time this method is called. 
     * 
     * @return  A unmodifiable {@code Map} where keys are {@code String}s 
     *          representing XRef IDs, the associated value being an unmodifiable {@code Set} 
     *          of {@code String}s representing the OBO-like IDs of the {@code OWLClass}es 
     *          they were associated to.
     */
    public Map<String, Set<String>> getXRefMappings() {
        log.entry();
        
        //lazy loading
        if (this.xRefMappings == null) {
            log.trace("Lazy loading of XRefMappings...");
            //first, we obtained the mappings, then we will make them unmodifiable
            Map<String, Set<String>> modifiableXRefMappings = 
                    new HashMap<String, Set<String>>();
            for (OWLClass cls: this.getWrapper().getAllOWLClasses()) {
                
                String clsId = this.getWrapper().getIdentifier(cls);
                for (String xref: this.getWrapper().getXref(cls)) {
                    Set<String> associatedClassIds = modifiableXRefMappings.get(xref);
                    if (associatedClassIds == null) {
                        associatedClassIds = new HashSet<String>();
                        modifiableXRefMappings.put(xref, associatedClassIds);
                    }
                    associatedClassIds.add(clsId);
                }
            }

            this.xRefMappings = new HashMap<String, Set<String>>();
            //now, perform a deep transformation of the mappings to be unmodifiable
            for (Entry<String, Set<String>> entry: modifiableXRefMappings.entrySet()) {
                this.xRefMappings.put(entry.getKey(), 
                        Collections.unmodifiableSet(entry.getValue()));
            }
            this.xRefMappings = Collections.unmodifiableMap(this.xRefMappings);
            log.trace("Lazy loading done, {} xrefs loaded.", this.xRefMappings.size());
        }
        
        return log.exit(this.xRefMappings);
    }
    
    /**
     * Obtains from the ontology wrapped in this object the {@code OWLClass} corresponding to 
     * {@code id}. The {@code OWLClass} will be tried to be identified first by assuming 
     * {@code id} is an OBO-like ID (using 
     * {@code OWLGraphWrapperExtended.getOWLClassByIdentifier(String)}), then if not identified, 
     * by assuming it is the {@code String} representation of an {@code IRI} (using 
     * {@code OWLGraphWrapperExtended.getOWLClass(String)}), then if not identified, 
     * by assuming it is an xref (in that case, xref mappings are retrieved calling 
     * {@link #getXRefMappings()}, then if one and only one ID is associated to the xref, 
     * the corresponding {@code OWLClass} is retrieved).
     * 
     * @param id    A {@code String} that can be either an OBO-like ID, or an {@code IRI}, 
     *              or an XRef mapped to an {@code OWLClass}.
     * @return      The {@code OWLClass} retrieved based on {@code id}.
     */
    public OWLClass getOWLClass(String id) {
        log.entry(id);
        
        OWLClass cls = this.getWrapper().getOWLClassByIdentifier(id);
        //if id was not an OBO-like ID, but an IRI
        if (cls == null) {
            cls = this.getWrapper().getOWLClass(id);
        }
        //in case the id provided was actually an xref
        if (cls == null) {
            Set<String> classIdsMapped = this.getXRefMappings().get(id);
            if (classIdsMapped != null) {
                if (classIdsMapped.size() == 1) {
                    String idMapped = classIdsMapped.iterator().next();
                    cls = this.getWrapper().getOWLClassByIdentifier(idMapped);
                    if (cls == null) {
                        cls = this.getWrapper().getOWLClass(idMapped);
                    }
                } else {
                    log.warn("Xref {} mapped to more than one class ID, cannot choose: {}", 
                            id, classIdsMapped);
                }
            }
        }
        //or if it was an obsolete ID with a replaced_by annotation
        if (cls == null) {
            Set<String> replacedByIds = this.getReplacedByMappings().get(id);
            if (replacedByIds != null && replacedByIds.size() == 1) {
                String idMapped = replacedByIds.iterator().next();
                cls = this.getWrapper().getOWLClassByIdentifier(idMapped);
                if (cls == null) {
                    cls = this.getWrapper().getOWLClass(idMapped);
                }
            }
        }
        
        return log.exit(cls);
    }
    
    /**
     * Obtains a unmodifiable mapping from OBO-like IDs of obsolete {@code OWLClass}es, 
     * present in the {@code OWLOntology} wrapped by this object, to the IDs associated to 
     * their {@code consider} annotation. Only obsolete {@code OWLClass}es are considered.
     * <p>
     * This mapping is lazy loaded the first time this method is called, or the first time 
     * {@link #getReplacedByMappings()} is called. 
     * 
     * @return  A unmodifiable {@code Map} where keys are {@code String}s representing 
     *          OBO-like IDs of obsolete {@code OWLClass}es, the associated value being 
     *          an unmodifiable {@code Set} of {@code String}s representing the IDs 
     *          associated to their {@code consider} annotations.
     * @see #getReplacedByMappings()
     */
    public Map<String, Set<String>> getConsiderMappings() {
        log.entry();
        
        //lazy loading
        if (this.considerMappings == null) {
            this.loadConsiderReplacedByMappings();
        }
        
        return log.exit(this.considerMappings);
    }
    
    /**
     * Obtains a unmodifiable mapping from OBO-like IDs of obsolete {@code OWLClass}es, 
     * present in the {@code OWLOntology} wrapped by this object, to the IDs associated to 
     * their {@code replaced_by} annotation. Only obsolete {@code OWLClass}es are considered.
     * <p>
     * This mapping is lazy loaded the first time this method is called, or the first time 
     * {@link #getConsiderMappings()} is called. 
     * 
     * @return  A unmodifiable {@code Map} where keys are {@code String}s representing 
     *          OBO-like IDs of obsolete {@code OWLClass}es, the associated value being 
     *          an unmodifiable {@code Set} of {@code String}s representing the IDs 
     *          associated to their {@code replaced_by} annotations.
     * @see #getConsiderMappings()
     */
    public Map<String, Set<String>> getReplacedByMappings() {
        log.entry();
        
        //lazy loading
        if (this.replacedByMappings == null) {
            this.loadConsiderReplacedByMappings();
        }
        
        return log.exit(this.replacedByMappings);
    }
    
    /**
     * Load the mappings from OBO-like IDs of obsolete {@code OWLClass}es to the IDs 
     * associated to their {@code replaced_by} annotations (into {@link #replacedByMappings}), 
     * and to their {@code consider} annotations (into {@link #considerMappings}). 
     * <p>
     * We load both types of annotations at the same time, as it requires identical code, 
     * and consider obsolete classes only in both cases. 
     */
    private void loadConsiderReplacedByMappings() {
        log.entry();
        
        log.trace("Lazy loading of replaced_by and consider mappings...");
        this.replacedByMappings = new HashMap<String, Set<String>>();
        this.considerMappings = new HashMap<String, Set<String>>();
        
        for (OWLOntology ont: this.getWrapper().getAllOntologies()) {
            for (OWLClass cls: ont.getClassesInSignature()) {
                
                //consider only obsolete OWLClasses
                if (!this.getWrapper().isObsolete(cls) && 
                        !this.getWrapper().getIsObsolete(cls)) {
                    continue;
                }
                
                String clsId = this.getWrapper().getIdentifier(cls);
                this.replacedByMappings.put(clsId, Collections.unmodifiableSet(
                                new HashSet<String>(this.getWrapper().getReplacedBy(cls))));
                this.considerMappings.put(clsId, Collections.unmodifiableSet(
                                new HashSet<String>(this.getWrapper().getConsider(cls))));
            }
        }
        this.replacedByMappings = Collections.unmodifiableMap(this.replacedByMappings);
        this.considerMappings = Collections.unmodifiableMap(this.considerMappings);
        log.trace("Lazy loading done, {} replaced_by mappings loaded, {} consider mappings loaded.", 
                this.replacedByMappings.size(), this.considerMappings.size());
        
        log.exit();
    }
    
    /**
     * Similar to the method from owltools 
     * {@code owltools.graph.OWLGraphWrapperEdges.getOutgoingEdges(OWLObject)}, 
     * but returns only {@code OWLGraphEdge}s representing an {@code is_a} 
     * ({@code SubClassOf}) relation, or a {@code part_of} related relation (including 
     * child {@code ObjectProperty}s, for instance {@code in_deep_part_of}), and 
     * that lead to a named target ({@code owltools.graph.OWLGraphEdge.isTargetNamedObject()} 
     * returns {@code true}). 
     * 
     * @param object    the {@code OWLObject} for which we want the filtered outgoing 
     *                  {@code OWLGraphEdge}s.
     * @return          A {@code Set} of {@code OWLGraphEdge}s, outgoing from {@code object}, 
     *                  leading to a named target, and representing only is_a or 
     *                  part_of related relations.
     */
    public Set<OWLGraphEdge> getIsAPartOfOutgoingEdges(OWLObject object) {
        log.entry(object);
        
        Set<OWLGraphEdge> isAPartOfEdges = new HashSet<OWLGraphEdge>();
        for (OWLGraphEdge edge: wrapper.getOutgoingEdges(object)) {
            //consider only named target
            if (!edge.isTargetNamedObject()) {
                continue;
            }
            if (//if it is a part_of related relation
                this.isPartOfRelation(edge) || 
               //or an is_a relation
               this.isASubClassOfEdge(edge)) {
                
                isAPartOfEdges.add(edge);
            } 
        }
        
        return log.exit(isAPartOfEdges);
    }
    
    /**
     * Determines whether {@code edge} is a is_a relation (SubClassOf relation with no 
     * {@code OWLObjectProperty}).
     * 
     * @param edge  The {@code OWLGraphEdge} to test for being a is_a relation.
     * @return      {@code true} if {@code edge} is a is_a relation.
     */
    public boolean isASubClassOfEdge(OWLGraphEdge edge) {
        log.entry(edge);
        
        return log.exit(edge.getSingleQuantifiedProperty().getProperty() == null && 
                            edge.getSingleQuantifiedProperty().isSubClassOf());
    }
    
    /**
     * Get "part_of" related {@code OWLObjectPropertyExpression}s, that are lazy loaded 
     * when needed. See {@link #partOfRels}.
     * 
     * @return  A {@code Set} of {@code OWLObjectPropertyExpression}s containing the "part_of" 
     *          {@code OWLObjectPropertyExpression}, and all its children.
     */
    private Set<OWLObjectPropertyExpression> getPartOfProps() {
        log.entry();
        if (this.partOfRels == null) {
            this.partOfRels = this.getWrapper().getSubPropertyReflexiveClosureOf(
                    this.getWrapper().getOWLObjectPropertyByIdentifier(PART_OF_ID));
        }
        return log.exit(this.partOfRels);
    }
    
    /**
     * Determines whether {@code edge} is a immediately_preceded_by relation (see 
     * {@link #IMMEDIATELY_PRECEDED_BY_ID}).
     * 
     * @param edge  The {@code OWLGraphEdge} to test for being a immediately_preceded_by relation.
     * @return      {@code true} if {@code edge} is a immediately_preceded_by-related relation.
     */
    public boolean isImmediatelyPrecededByRelation(OWLGraphEdge edge) {
        log.entry(edge);
        
        return log.exit(edge.getQuantifiedPropertyList().size() == 1 && 
                
                this.getWrapper().getOWLObjectPropertyByIdentifier(
                IMMEDIATELY_PRECEDED_BY_ID).equals(
                        edge.getSingleQuantifiedProperty().getProperty()) &&
                        
                    edge.getSingleQuantifiedProperty().isSomeValuesFrom());
    }
    
    /**
     * Determines whether {@code edge} is a preceded_by-related relation, meaning, 
     * having a {@code OWLObjectProperty} corresponding to "preceded_by" (see 
     * {@link #PRECEDED_BY_ID}) or any of its {@code OWLObjectProperty} children.
     * 
     * @param edge  The {@code OWLGraphEdge} to test for being a preceded_by-related relation.
     * @return      {@code true} if {@code edge} is a preceded_by-related relation.
     */
    public boolean isPrecededByRelation(OWLGraphEdge edge) {
        log.entry(edge);
        
        if (edge.getQuantifiedPropertyList().size() == 1) {
            return log.exit(this.getPrecededByProps().contains(
                    edge.getSingleQuantifiedProperty().getProperty()) && 
                    edge.getSingleQuantifiedProperty().isSomeValuesFrom());
        } 
        if (edge.getQuantifiedPropertyList().size() > 1) {
            //if we have only part_of, subClassOf, preceded_by or immediately_preceded_by 
            //relations, with at least one preceded_by or immediately_preeded_by relation, 
            //then it is valid (I don't know why those properties are not compacted). 
            boolean atLeastOnePrecededBy = false;
            for (OWLQuantifiedProperty qp: edge.getQuantifiedPropertyList()) {
                log.trace("QP examined: {}", qp);
                if ((qp.getProperty() == null && qp.isSubClassOf()) || //subClassOf
                    //part_of or preceded_by or immediately_preceded_by
                    (qp.getProperty() != null && qp.isSomeValuesFrom() && 
                            (this.getPartOfProps().contains(qp.getProperty()) || 
                            this.getPrecededByProps().contains(qp.getProperty())))
                 ) {
                    log.trace("Valid QP");
                    if (this.getPrecededByProps().contains(qp.getProperty())) {
                        atLeastOnePrecededBy = true;
                    }
                } else {
                    log.trace("Invalid QP");
                    return log.exit(false);
                }
            }
            return log.exit(atLeastOnePrecededBy);
        }
        
        return log.exit(false);
    }

    /**
     * Determines whether {@code edge} is a part_of-related relation, meaning, 
     * having a {@code OWLObjectProperty} corresponding to "part_of" (see 
     * {@link #PART_OF_ID}) or any of its {@code OWLObjectProperty} children.
     * 
     * @param edge  The {@code OWLGraphEdge} to test for being a part_of-related relation.
     * @return      {@code true} if {@code edge} is a part_of-related relation.
     */
    public boolean isPartOfRelation(OWLGraphEdge edge) {
        log.entry(edge);
        
        return log.exit(edge.getQuantifiedPropertyList().size() == 1 && 
                
                this.getPartOfProps().contains(
                    edge.getSingleQuantifiedProperty().getProperty()) && 
                    
                    edge.getSingleQuantifiedProperty().isSomeValuesFrom());
    }
    
    /**
     * Get "preceded_by" related {@code OWLObjectPropertyExpression}s, that are lazy loaded 
     * when needed. See {@link #precededByRels}.
     * 
     * @return  A {@code Set} of {@code OWLObjectPropertyExpression}s containing the "preceded_by" 
     *          {@code OWLObjectPropertyExpression}, and all its children.
     */
    private Set<OWLObjectPropertyExpression> getPrecededByProps() {
        log.entry();
        if (this.precededByRels == null) {
            this.precededByRels = this.getWrapper().getSubPropertyReflexiveClosureOf(
                    this.getWrapper().getOWLObjectPropertyByIdentifier(PRECEDED_BY_ID));
        }
        return log.exit(this.precededByRels);
    }
    
    /**
     * Determines whether {@code object} is a member of a non-informative subset.
     * 
     * @param object    the {@code OWLObject} which we want subset information about.
     * @return          {@code true} if {@code object} is member of a non-informative subset.
     * @see #NON_INFORMATIVE_SUBSETS
     */
    public boolean isNonInformativeSubsetMember(OWLObject object) {
        log.entry(object);
        
        return log.exit(!Collections.disjoint(NON_INFORMATIVE_SUBSETS, 
                this.getWrapper().getSubsets(object)));
    }
    
    /**
     * Returns the number of steps to walk on the shortest path from {@code source} to 
     * {@code target}, following any relations if {@code overProps} is {@code null} 
     * or empty, otherwise, following only relations specified by {@code overProps}.
     * <p>
     * For instance, if A is_a B is_a C, and A is_a B' is_a B'' is_a C, then then min 
     * distance between A and C is 2.
     * 
     * @param source        The {@code OWLClass} which to start the walk from.
     * @param target        The {@code OWLClass} which to finish the walk to.
     * @param overProps     A {@code Set} of {@code OWLPropertyExpression}s allowing 
     *                      to restrain the relations considered. 
     * @return              An {@code int} that is the number of {@code OWLClass}es 
     *                      to walk from {@code source} to {@code target} 
     *                      on the shortest path.
     */
    public int getMinDistance(OWLClass source, OWLClass target, 
            Set<OWLPropertyExpression> overProps) {
        log.entry(source, target, overProps);
        
        //identity
        if (source.equals(target)) {
            return log.exit(0);
        }
        
        //we will walk each OWLClass on the path from source to target, 
        //and for each step, we will store the current OWLClass walked along with 
        //the distance from source, using a singleton Map. 
        Deque<Map<OWLClass, Integer>> walks = new ArrayDeque<Map<OWLClass, Integer>>();
        walks.offerFirst(Collections.singletonMap(source, 0));
        
        Map<OWLClass, Integer> currentStep;
        int minDistance = 0;
        while ((currentStep = walks.pollFirst()) != null) {
            OWLClass currentClass = currentStep.keySet().iterator().next();
            int nextDistance = currentStep.values().iterator().next() + 1;
            
            for (OWLGraphEdge outgoingEdge: this.getWrapper().getOutgoingEdges(currentClass)) {
                if (outgoingEdge.getQuantifiedPropertyList().size() > 1) {
                    continue;
                }
                OWLQuantifiedProperty qp = outgoingEdge.getSingleQuantifiedProperty();
                if (outgoingEdge.isTargetNamedObject() && 
                        (overProps == null || overProps.isEmpty() || 
                        (qp.isSomeValuesFrom() && overProps.contains(qp.getProperty())))) {
                    
                    OWLClass potentialNextStep = (OWLClass) outgoingEdge.getTarget();
                    //reach target
                    if (potentialNextStep.equals(target)) {
                        //first time we reach target
                        if (minDistance == 0) {
                            minDistance = nextDistance;
                        } else {
                            //target already reached through another path, let's see 
                            //what was the shortest path
                            minDistance = Math.min(minDistance, nextDistance);
                        }
                    } else if (this.getWrapper().getAncestors(potentialNextStep, overProps).
                            contains(target)) {
                        //target on path, continue walk
                        walks.offerFirst(Collections.singletonMap(potentialNextStep, 
                                nextDistance));
                    }
                }
            }
        }
        if (minDistance == 0) {
            throw log.throwing(new IllegalArgumentException("The target " + target + 
                    " is not reachable from source " + source));
        }
        
        return log.exit(minDistance);
    }
    
    /**
     * Returns the least common ancestor of {@code cls1} and {@code cls2} over relations 
     * specified by {@code overProps}. Only named {@code OWLClass} common ancestors  
     * will be retrieved. If {@code overProps} is {@code null}, then any relations 
     * are considered. 
     * 
     * @param cls1          An {@code OWLClass} for which we want the LCAs with {@code cls2}.
     * @param cls2          An {@code OWLClass} for which we want the LCAs with {@code cls1}.
     * @param overProps     A {@code Set} of {@code OWLPropertyExpression}s allowing 
     *                      to restrain the relations considered.
     * @return              A {@code Set} of {@code OWLClass}es that are the named 
     *                      least common ancestors of {@code cls1} and {@code cls2}.
     */
    public Set<OWLClass> getLeastCommonAncestors(OWLClass cls1, OWLClass cls2, 
            Set<OWLPropertyExpression> overProps) {
        log.entry(cls1, cls2, overProps);
        
        Set<OWLClass> commonAncestors = new HashSet<OWLClass>();
        Set<OWLObject> ancestorsStart = this.getWrapper().getAncestors(cls1, overProps);
        Set<OWLObject> ancestorsEnd = this.getWrapper().getAncestors(cls2, overProps);
        ancestorsStart.retainAll(ancestorsEnd);
        for (OWLObject ancestor: ancestorsStart) {
            if (ancestor instanceof OWLClass) {
                commonAncestors.add((OWLClass) ancestor);
            }
        }
        Set<OWLClass> lcas = new HashSet<OWLClass>(commonAncestors);
        for (OWLObject ancestor: commonAncestors) {
            lcas.removeAll(this.getWrapper().getAncestors(ancestor, overProps));
        }
        
        return log.exit(lcas);
    }
    
    /**
     * Return the {@code OWLGraphWrapper} wrapping the ontology on which operations 
     * should be performed. If not provided at instantiation, it will be automatically 
     * loaded the first time this method is called, from the {@code OWLOntology} provided 
     * at instantiation.
     * 
     * @return  the {@code OWLGraphWrapper} wrapping the ontology on which operations 
     *          should be performed.
     */
    public OWLGraphWrapper getWrapper() {
        if (this.wrapper == null) {
            if (this.manipulator != null) {
                this.wrapper = this.manipulator.getOwlGraphWrapper();
            } else {
                this.wrapper = new OWLGraphWrapper(this.ontology);
            }
        }
        return this.wrapper;
    }
    
    /**
     * Return the {@code OWLGraphManipulator} wrapping the ontology on which operations 
     * should be performed. If not provided at instantiation, it will be automatically 
     * loaded the first time this method is called, from the {@code OWLOntology} provided 
     * at instantiation.
     * 
     * @return  the {@code OWLGraphManipulator} wrapping the ontology on which operations 
     *          should be performed.
     */
    public OWLGraphManipulator getManipulator() {
        if (this.manipulator == null) {
            this.manipulator = new OWLGraphManipulator(this.getWrapper());
        }
        return this.manipulator;
    }
    
    @Override
    public String toString() {
        if (this.wrapper != null) {
            return "[OntologyUtils wrapping " + this.wrapper.toString() + "]";
        } 
        if (this.ontology != null) {
            return "[OntologyUtils wrapping " + this.ontology.toString() + "]";
        } 
        return super.toString();
    }
}
