package org.bgee.pipeline.ontologycommon;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
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
import owltools.graph.OWLGraphManipulator;
import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.obolibrary.oboformat.writer.OBOFormatWriter;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLNamedObject;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
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
import owltools.graph.OWLQuantifiedProperty.Quantifier;
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
     * {@code IRI} of the {@code OWLObjectProperty} "immediate_transformation_of".
     */
    public final static String IMMEDIATE_TRANSFORMATION_OF_ID = "RO:0002495";
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
        log.entry(ontFile);
        ParserWrapper parserWrapper = new ParserWrapper();
        parserWrapper.setCheckOboDoc(false);
        try {
            return log.exit(parserWrapper.parse(ontFile));
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
        log.entry(ncbiId);
        return log.exit(TAX_ONTOLOGY_ID_PREFIX + ncbiId);
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
        log.entry(ontologyTermId);
        return log.exit(Integer.parseInt(
                ontologyTermId.substring(TAX_ONTOLOGY_ID_PREFIX.length())));
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
     * A {@code String} that is the path to the ontology wrapped by this object, 
     * if loaded from a file.
     */
    private String pathToOntology;
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
     * This attribute is lazy loaded when {@link #getPrecededByProps()} 
     * is called. 
     * 
     * @see #getPrecededByProps()
     * @see #isPrecededByRelation(OWLGraphEdge)
     */
    private Set<OWLObjectPropertyExpression> precededByRels;
    /**
     * A {@code Set} of {@code OWLObjectPropertyExpression}s containing 
     * the sub-properties of the "transformation_of" property, 
     * and the "transformation_of" property itself (see {@link #TRANSFORMATION_OF_ID}).
     * <p>
     * This attribute is lazy loaded when {@link #getTransformationOfProps()} is called. 
     * 
     * @see #getTransformationOfProps()
     * @see #isTransformationOfRelation(OWLGraphEdge)
     */
    private Set<OWLObjectPropertyExpression> transformationOfRels;
    /**
     * A {@code Set} of {@code OWLObjectPropertyExpression}s containing 
     * the sub-properties of the "develops_from" property, 
     * and the "develops_from" property itself (see {@link #DEVELOPS_FROM_ID}).
     * <p>
     * This attribute is lazy loaded when {@link #getDevelopsFromProps()} is called. 
     * 
     * @see #getDevelopsFromProps()
     * @see #isTransformationOfRelation(OWLGraphEdge)
     */
    private Set<OWLObjectPropertyExpression> developsFromRels;
    
    /**
     * Constructor providing the {@code OWLOntology} which operations 
     * should be performed on.
     * @param ontology  the {@code OWLOntology} which operations 
     *                  should be performed on.
     */
    public OntologyUtils(OWLOntology ontology) { 
        this.ontology = ontology;
        this.wrapper = null;
        this.pathToOntology = null;
        this.xRefMappings = null;
        this.partOfRels = null;
        this.precededByRels = null;
        this.transformationOfRels = null;
        this.developsFromRels = null;
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
        this.pathToOntology = pathToOntology;
    }
    
    /**
     * Equivalent to calling {@link #computeNestedSetModelParams(List)} 
     * with a {@code null} argument (no specific ordering requested).
     * 
     * @param root  An {@code OWLClass} that will be considered as the root of the ontology 
     *              to start the computations from.
     * @return  See {@link #computeNestedSetModelParams(OWLClass, List)} 
     * @throws IllegalStateException    If the {@code OWLOntology} provided at instantiation 
     *                                  is not a simple tree.
     * @throws UnknownOWLOntologyException      If an {@code OWLGraphWrapper} was not 
     *                                          provided at instantiation, and an error 
     *                                          occurred while loading it.
     * @throws OWLOntologyCreationException     If an {@code OWLGraphWrapper} was not 
     *                                          provided at instantiation, and an error 
     *                                          occurred while loading it.
     * @see #computeNestedSetModelParams(OWLClass, List)
     */
    //TODO adapt as in org.bgee.pipeline.hierarchicalGroups.ParseOrthoXML.buildNestedSet(Node)
    public Map<OWLClass, Map<String, Integer>> computeNestedSetModelParams(OWLClass root) 
            throws UnknownOWLOntologyException, IllegalStateException {
        log.entry(root);
        return log.exit(this.computeNestedSetModelParams(root, null));
    }
    
    /**
     * Delegates to {@link #computeNestedSetModelParams(OWLClass, List, Set)} with the second 
     * {@code Set} argument set to {@code null}.
     * 
     * @param root  An {@code OWLClass} that will be considered as the root of the ontology 
     *              to start the computations from.
     * @param classOrder    See same argument in {@link #computeNestedSetModelParams(OWLClass, List, Set)}.
     * @return              See returned value of {@link #computeNestedSetModelParams(OWLClass, List, Set)}
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
    //suppress warning because the getAncestors method of owltools uses unparameterized 
    //generic OWLPropertyExpression, so we need to do the same. 
    public Map<OWLClass, Map<String, Integer>> computeNestedSetModelParams(OWLClass root, 
            List<OWLClass> classOrder, 
            @SuppressWarnings("rawtypes") Set<OWLPropertyExpression> overProps) 
                    throws UnknownOWLOntologyException {
        log.entry(root, classOrder, overProps);
        
        Map<OWLClass, Map<String, Integer>> params = 
                new HashMap<OWLClass, Map<String, Integer>>();
        
        if (root == null) {
            //get the root of the ontology, that should be unique.
            Set<OWLClass> roots = this.getWrapper().getOntologyRoots();
            if (roots.size() != 1) {
                throw log.throwing(new IllegalStateException("Incorrect number of roots " +
                        "in the ontology: " + roots.size() + " - " + roots));
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
    //suppress warning because the getAncestors method of owltools uses unparameterized 
    //generic OWLPropertyExpression, so we need to do the same. 
    private void recursiveNestedSetModelParams(
            final Map<OWLClass, Map<String, Integer>> params, 
            final OWLClass classInspected,  final List<OWLClass> classOrder, 
            @SuppressWarnings("rawtypes") Set<OWLPropertyExpression> overProps) 
        throws IllegalStateException, UnknownOWLOntologyException {
        log.entry(params, classInspected, classOrder, overProps);
        
        //we will iterate children of classInspected. 
        Set<OWLClass> children = new HashSet<OWLClass>();
        if (overProps != null && !overProps.isEmpty()) {
            for (OWLGraphEdge incomingEdge: this.getWrapper().getIncomingEdgesWithGCI(classInspected)) {
                OWLQuantifiedProperty qp = incomingEdge.getSingleQuantifiedProperty();
                if (incomingEdge.isSourceNamedObject() && 
                    (qp.isSomeValuesFrom() && overProps.contains(qp.getProperty()) || 
                            this.isASubClassOfEdge(incomingEdge))) {
                    
                    children.add((OWLClass) incomingEdge.getSource());
                }
            }
        } else {
            children = this.getWrapper().getOWLClassDirectDescendantsWithGCI(classInspected);
        }
        //we discard children that are not in classOrder
        if (classOrder != null) {
            children.retainAll(classOrder);
        }
        log.trace("Asserted children of {}: {}", classInspected, children);
        
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
     * Merge {@code list1} and {@code list2} by identifying common {@code OWLClass}es 
     * and keeping the order consistent for both provided lists. So for instance, 
     * if {@code list1} contains {@code cls1}, {@code cls2}, and {@code cls3}, and 
     * if {@code list1} contains {@code cls4}, {@code cls2}, and {@code cls5}, 
     * then the resulted mergeds list would be: {@code cls1}, {@code cls4}, 
     * {@code cls2}, {@code cls3}, {@code cls5}.
     * 
     * @param list1 A first {@code List} of {@code OWLClass}es to be merged. 
     * @param list2 A second {@code List} of {@code OWLClass}es to be merged. 
     * @return      The resulting merged {@code List} of {@code OWLClass}es properly 
     *              ordered. 
     * @throw IllegalArgumentException  If some of the {@code List}s contains several 
     *                                  equal elements. 
     */
    public static <T> List<T> mergeLists(List<T> list1, List<T> list2) 
        throws IllegalArgumentException {
        log.entry(list1, list2);
        
        List<T> mergedList = new ArrayList<T>();
        
        //we need to identify at least one common element, other common element will be considered 
        //during recursive iterations.
        Set<T> set1 = new HashSet<T>(list1);
        if (set1.size() != list1.size()) {
            throw log.throwing(new IllegalArgumentException("The following list contains " +
            		"non-unique elements: " + list1));
        }
        Set<T> set2 = new HashSet<T>(list2);
        if (set2.size() != list2.size()) {
            throw log.throwing(new IllegalArgumentException("The following list contains " +
                    "non-unique elements: " + list2));
        }
        set1.retainAll(set2);
        
        //now, reorder the elements that are before and after the common elements
        if (!set1.isEmpty()) {
            log.trace("Elements in common.");
            //get any common element
            T commonElement = set1.iterator().next();
            log.trace("Element in common used as anchor: {}", commonElement);
            
            int commonClassIndex1 = list1.indexOf(commonElement);
            List<T> firstPartList1 = list1.subList(0, commonClassIndex1);
            List<T> secondPartList1 = list1.subList(commonClassIndex1 + 1, list1.size());
            int commonClassIndex2 = list2.indexOf(commonElement);
            List<T> firstPartList2 = list2.subList(0, commonClassIndex2);
            List<T> secondPartList2 = list2.subList(commonClassIndex2 + 1, list2.size());
            log.trace("First part list 1: {} - first part list 2: {} --- second part list1 {} - second part list2: {}", 
                    firstPartList1, firstPartList2, secondPartList1, secondPartList2);
            
            //merge the first parts and the seconds parts, add the common element in between
            List<T> firstPart = OntologyUtils.mergeLists(firstPartList1, firstPartList2);
            List<T> secondPart = OntologyUtils.mergeLists(secondPartList1, secondPartList2);
            mergedList.addAll(firstPart);
            mergedList.add(commonElement);
            mergedList.addAll(secondPart);
        } else {
            log.trace("No elements in common, simply adding the two lists.");
            mergedList.addAll(list1);
            mergedList.addAll(list2);
        }
        
        return log.exit(mergedList);
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
            throws IOException, IllegalArgumentException {
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
            throws IOException, IllegalArgumentException {
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
     * Determines whether {@code obj} is obsolete.
     * @param obj   An {@code OWLObject} to be checked for deprecation
     * @return      {@code true} if {@code obj} is obsolete.
     */
    public boolean isObsolete(OWLObject obj) {
        log.entry(obj);
        return log.exit(this.getWrapper().isObsolete(obj) || 
                this.getWrapper().getIsObsolete(obj));
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
            for (OWLOntology ont: this.getWrapper().getAllOntologies()) {
                for (OWLClass cls: ont.getClassesInSignature()) {

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
            }
            log.trace("Original XRef mapping: {}", modifiableXRefMappings);
            
            //now, we review each xref mapping: if an xref mapped to several classes, 
            //we check whether it maps to at least one non-obsolete OWLClass, and in that case 
            //we remove all mappings to obsolete classes; if there is no mapping to 
            //non-obsolete class, we keep everything
            for (Entry<String, Set<String>> mapping: modifiableXRefMappings.entrySet()) {
                Set<String> associatedClassIds = mapping.getValue();
                if (associatedClassIds.size() > 1) {
                    Set<String> nonObsoleteClassIds = new HashSet<String>();
                    Set<String> obsoleteClassIds = new HashSet<String>();
                    for (String clsId: associatedClassIds) {
                        OWLClass cls = this.getWrapper().getOWLClassByIdentifier(clsId, true);
                        if (cls == null) {
                            cls = this.getWrapper().getOWLClass(clsId);
                        }
                        if (this.isObsolete(cls)) {
                            obsoleteClassIds.add(clsId);
                        } else {
                            nonObsoleteClassIds.add(clsId);
                        }
                    }
                    
                    if (!nonObsoleteClassIds.isEmpty()) {
                        mapping.setValue(nonObsoleteClassIds);
                    }
                }
            }
            log.trace("XRef mapping filtered for obsolete terms: {}", modifiableXRefMappings);

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
     * Retrieves {@code OWLClass}es corresponding to {@code cls}. 
     * "Corresponding" {@code OWLClass}es are the targets retrieved by searching for 
     * taxonomy Equivalent Classes Axioms, see {@link #getECAIntersectionOf(OWLClass, 
     * OWLObjectPropertyExpression, OWLClass)} for details.
     * 
     * @param cls               See see {@link #getECAIntersectionOf(OWLClass, 
     *                          OWLObjectPropertyExpression, OWLClass)}
     * @param prop              See see {@link #getECAIntersectionOf(OWLClass, 
     *                          OWLObjectPropertyExpression, OWLClass)}
     * @param fillerParentClass See see {@link #getECAIntersectionOf(OWLClass, 
     *                          OWLObjectPropertyExpression, OWLClass)}
     * @return                  A {@code Set} of {@code OWLClass}es that are the class operands 
     *                          in the {@code OWLObjectSomeValuesFrom}s of the 
     *                          {@code OWLObjectIntersectionOf}s considered. 
     */
    public Set<OWLClass> getECAIntersectionOfTargets(OWLClass cls, 
            OWLObjectPropertyExpression prop, OWLClass fillerParentClass) {
        log.entry(cls, prop, fillerParentClass);
        Set<OWLClass> targets = new HashSet<OWLClass>();
        for (OWLEquivalentClassesAxiom eca: 
            this.getECAIntersectionOf(cls, prop, fillerParentClass)) {
            targets.add((OWLClass) this.convertECAIntersectionOfToEdge(eca, null).getTarget());
        }
        return log.exit(targets);
    }
    
    /**
     * Retrieves {@code OWLEquivalentClassesAxiom}s with a specific structure: 
     * Equivalent Classes Axioms where one of the class expressions is {@code cls}, 
     * the other expression being an {@code OWLObjectIntersectionOf}; where
     * one of its operands is an {@code OWLClass}, and the other operand is 
     * an {@code OWLObjectSomeValuesFrom}; where the {@code OWLObjectSomeValuesFrom} 
     * have {@code prop} as property, the filler being equal to, 
     * or a subClassOf of {@code fillerParentClass} (through direct or indirect is_a relations). 
     * <p>
     * A typical use case is to retrieved taxonomy equivalence, where, for instance, 
     * an EHDAA2 class is considered to be equivalent to an Uberon class in the human taxon 
     * (for instance, {@code EHDAA2:xxx EquivalentTo(UBERON:xxx and part_of some NCBITaxon:9606)}; 
     * here, {@code cls} would correspond to "EHDAA2:xxx", {@code prop} to "part_of", 
     * {@code fillerParentClass} to "NCBITaxon:1"; the returned {@code Set} would contain 
     * the class "UBERON:xxx").
     * <p>
     * If {@code prop} is {@code null}, then {@code OWLObjectSomeValuesFrom}s with 
     * any property will be considered. If {@code fillerParentClass} is {@code null}, 
     * then {@code OWLObjectSomeValuesFrom}s with any filler will be considered. 
     * 
     * @param cls               An {@code OWLClass} for which we want related 
     *                          {@code OWLEquivalentClassesAxiom}s.
     * @param prop              An {@code OWLObjectPropertyExpression} retraining the 
     *                          {@code OWLObjectSomeValuesFrom}s considered.
     * @param fillerParentClass An {@code OWLClass}  retraining the 
     *                          {@code OWLObjectSomeValuesFrom}s considered.
     * @return                  A {@code Set} of {@code OWLEquivalentClassesAxiom}es 
     *                          matching the requested structure.
     */
    public Set<OWLEquivalentClassesAxiom> getECAIntersectionOf(OWLClass cls, 
            OWLObjectPropertyExpression prop, OWLClass fillerParentClass) {
        log.entry(cls, prop, fillerParentClass);
        
        Set<OWLEquivalentClassesAxiom> ecas = new HashSet<OWLEquivalentClassesAxiom>();
        for (OWLOntology ont: this.getWrapper().getAllOntologies()) {
            //we search for Equivalent Classes Axioms where one of the class expressions 
            //is cls, the other expression being an OWLObjectIntersectionOf. 
            //We expect one of its operands to be an OWLClass, and the other 
            //operand to be an OWLObjectSomeValuesFrom. The OWLObjectSomeValuesFrom 
            //should have prop as property, and the filler should be a subClassOf of 
            //fillerParentClass (through direct or indirect is_a relations).
            for (OWLEquivalentClassesAxiom eca: ont.getEquivalentClassesAxioms(cls)) {
                log.trace("Examining ECA: {}", eca);
                //we use convertECAIntersectionToEdge to check that the ECA is valid
                try {
                    OWLGraphEdge edge = this.convertECAIntersectionOfToEdge(eca, ont);
                    //If the ECA satisfies all requirements
                    if (edge.getSource().equals(cls) && 
                            (prop == null || prop.equals(edge.getGCIRelation())) && 
                            (fillerParentClass == null || 
                            fillerParentClass.equals(edge.getGCIFiller()) ||
                             this.getWrapper().getAncestorsThroughIsA(
                                     edge.getGCIFiller()).contains(fillerParentClass))) {
                        log.trace("Satisfying ECA: {}", eca);
                        ecas.add(eca);
                    } else {
                        log.trace("Discarded.");
                    }
                } catch (IllegalArgumentException e) {
                    //do nothing, this is simply not a valid ECA
                    log.trace("Discarded.");
                }
            }
        }
        return log.exit(ecas);
    }
    
    /**
     * Convert an EquivalentClasses axiom with IntersectionOf class expression into 
     * an {@code OWLGraphEdge}. The axioms should be the same as the axioms retrieved by 
     * {@link #getECAIntersectionOf(OWLClass, OWLObjectPropertyExpression, OWLClass)}, 
     * otherwise, an {@code IllegalArgumentException} is thrown. 
     * <p>
     * The {@code OWLClass} expression in the eca will be returned by 
     * {@code OWLGraphEdge#getSource()}, the {@code OWLClass} expression in the IntersectionOf 
     * will be returned by {@code OWLGraphEdge#getTarget()}, the filler and property 
     * in the {@code OWLObjectSomeValuesFrom} will be returned respectively by 
     * {@code OWLGraphEdge#getGCIFiller()} and {@code OWLGraphEdge#getGCIRelation()}. 
     * {@code eca} will be returned by {@code OWLGraphEdge#getAxioms()}.
     * <p>
     * Note that this is not the proper meaning of {@code OWLGraphEdge#getGCIFiller()} 
     * and {@code OWLGraphEdge#getGCIRelation()} (as the anonymous class expression 
     * is not on the left side, as for a GCI, but on the right side), but we use them 
     * for convenience.
     * 
     * @param eca   An {@code OWLEquivalentClassesAxiom} with the same structure as 
     *              the axioms returned by {@link #getECAIntersectionOf(OWLClass, 
     *              OWLObjectPropertyExpression, OWLClass)}.
     * @return      An {@code OWLGraphEdge} storing the information relative to {@code eca}.
     * @throws IllegalArgumentException If {@code eca} does not have the expected structure 
     *                                  (see {@link #getECAIntersectionOf(OWLClass, 
     *                                  OWLObjectPropertyExpression, OWLClass)})
     */
    public OWLGraphEdge convertECAIntersectionOfToEdge(OWLEquivalentClassesAxiom eca, 
            OWLOntology ont) {
        log.entry(eca, ont);
        
        if (eca.getClassExpressions().size() != 2) {
            //we do not log the exception because it used to test for ECA validity 
            //by other methods, so this would generate annoying logs.
            throw new IllegalArgumentException("Incorrect EquivalentClasses " +
            		"axiom provided, expecting 2 class expressions: " + eca);
        }
        OWLClass sourceCls = null;
        OWLObjectIntersectionOf intersect = null;
        for (OWLClassExpression clsExpr: eca.getClassExpressions()) {
            if (clsExpr instanceof OWLClass) {
                sourceCls = (OWLClass) clsExpr;
            } else if (clsExpr instanceof OWLObjectIntersectionOf) {
                intersect = (OWLObjectIntersectionOf) clsExpr;
            }
        }
        if (sourceCls == null || intersect == null) {
            //we do not log the exception because it used to test for ECA validity 
            //by other methods, so this would generate annoying logs.
            throw new IllegalArgumentException("Incorrect EquivalentClasses " +
                    "axiom provided, expecting one of the class expressions being OWLClass, " +
                    "and other class expression being an OWLObjectIntersectionOf: " + eca);
        }
        if (intersect.getOperands().size() != 2) {
            //we do not log the exception because it used to test for ECA validity 
            //by other methods, so this would generate annoying logs.
            throw new IllegalArgumentException("Incorrect EquivalentClasses " +
                    "axiom provided, expecting one of the class expressions being " +
                    "an OWLObjectIntersectionOf with two operands: " + eca);
        }

        OWLClass clsOperand = null;
        OWLObjectPropertyExpression gciRel = null;
        OWLClass filler = null;
        for (OWLClassExpression operand: intersect.getOperands()) {
            if (operand instanceof OWLClass) {
                clsOperand = (OWLClass) operand;
            } else if (operand instanceof OWLObjectSomeValuesFrom &&
                    ((OWLObjectSomeValuesFrom)operand).getFiller()
                    instanceof OWLClass) {
                filler = (OWLClass) ((OWLObjectSomeValuesFrom)operand).getFiller();
                gciRel = ((OWLObjectSomeValuesFrom)operand).getProperty();
            }
        }
        if (clsOperand == null || filler == null || gciRel == null) {
            //we do not log the exception because it used to test for ECA validity 
            //by other methods, so this would generate annoying logs.
            throw new IllegalArgumentException("Incorrect EquivalentClasses " +
                    "axiom provided, expecting one of the class expressions being " +
                    "an OWLObjectIntersectionOf, with one operand being an OWLClass, " +
                    "the other operand being an OWLObjectSomeValuesFrom with " +
                    "a filler and a property: " + eca);
        }
        
        return log.exit(new OWLGraphEdge(sourceCls, clsOperand, null, Quantifier.IDENTITY, 
                ont, eca, filler, gciRel));
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
                List<String> replacedBy = this.getWrapper().getReplacedBy(cls);
                if (!replacedBy.isEmpty()) {
                    this.replacedByMappings.put(clsId, Collections.unmodifiableSet(
                            new HashSet<String>(replacedBy)));
                }
                List<String> consider = this.getWrapper().getConsider(cls);
                if (!consider.isEmpty()) {
                    this.considerMappings.put(clsId, Collections.unmodifiableSet(
                            new HashSet<String>(consider)));
                }
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
        for (OWLGraphEdge edge: wrapper.getOutgoingEdgesWithGCI(object)) {
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
     * Retrieve all {@code OWLClass} members of the subgraph from roots with their ID 
     * in {@code subgraphRootIds}. This method will retrieve the {@code OWLClass}es 
     * with their OBO-like ID in {@code subgraphRootIds}, as well as 
     * all their {@code OWLClass} descendants and ancestors.
     * 
     * @param subgraphRootIds   A {@code Collection} of {@code String}s that are the OBO-like IDs 
     *                          of {@code OWLClass}es for which we want to retrieve members 
     *                          of their subgraph.
     * @return      A {@code Set} of {@code OWLClass}es that have their OBO-like ID 
     *              in {@code subgraphRootIds}, or that are their ancestors or descendants.
     *              
     */
    public Set<OWLClass> getSubgraphMembers(Collection<String> subgraphRootIds) {
        log.entry(subgraphRootIds);
        
        Set<OWLClass> subgraphMembers = new HashSet<OWLClass>();
        if (subgraphRootIds != null) {
            for (String subgraphRootId: new HashSet<String>(subgraphRootIds)) {
                OWLClass subgraphRoot = this.getWrapper().getOWLClassByIdentifier(
                        subgraphRootId);
                
                if (subgraphRoot != null) {
                    subgraphMembers.add(subgraphRoot);
                    subgraphMembers.addAll(this.getWrapper().getOWLClassDescendantsWithGCI(
                                    subgraphRoot));
                    subgraphMembers.addAll(this.getWrapper().getOWLClassAncestorsWithGCI(
                                    subgraphRoot));
                }
            }
        }
        
        return log.exit(subgraphMembers);
    }
    
    /**
     * Get "part_of" related {@code OWLObjectPropertyExpression}s, that are lazy loaded 
     * when needed. 
     * 
     * @return  A {@code Set} of {@code OWLObjectPropertyExpression}s containing the "part_of" 
     *          {@code OWLObjectPropertyExpression}, and all its children.
     */
    //TODO: add unit test
    public Set<OWLObjectPropertyExpression> getPartOfProps() {
        log.entry();
        if (this.partOfRels == null) {
            this.partOfRels = this.getWrapper().getSubPropertyReflexiveClosureOf(
                    this.getWrapper().getOWLObjectPropertyByIdentifier(PART_OF_ID));
        }
        return log.exit(this.partOfRels);
    }
    
    /**
     * Return the {@code Set} returned by {@link #getPartOfProps()} as a {@code Set} 
     * of {@code OWLPropertyExpression}s (rather than {@code OWLObjectPropertyExpression}s).
     * 
     * @return  A {@code Set} of {@code OWLPropertyExpression}s containing the "part_of" 
     *          {@code OWLObjectPropertyExpression}, and all its children.
     */
    @SuppressWarnings("rawtypes")
    //TODO: add unit test
    public Set<OWLPropertyExpression> getGenericPartOfProps() {
        log.entry();
        Set<OWLPropertyExpression> props = new HashSet<OWLPropertyExpression>();
        for (OWLObjectPropertyExpression prop: this.getPartOfProps()) {
            props.add(prop);
        }
        return log.exit(props);
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
     * Get "transformation_of" related {@code OWLObjectPropertyExpression}s, that are lazy loaded 
     * when needed. See {@link #transformationOfRels}.
     * 
     * @return  A {@code Set} of {@code OWLObjectPropertyExpression}s containing the "transformation_of" 
     *          {@code OWLObjectPropertyExpression}, and all its children.
     */
    //TODO: add unit test
    public Set<OWLObjectPropertyExpression> getTransformationOfProps() {
        log.entry();
        if (this.transformationOfRels == null) {
            this.transformationOfRels = this.getWrapper().getSubPropertyReflexiveClosureOf(
                    this.getWrapper().getOWLObjectPropertyByIdentifier(TRANSFORMATION_OF_ID));
        }
        return log.exit(this.transformationOfRels);
    }
    /**
     * Get "develops_from" related {@code OWLObjectPropertyExpression}s, that are lazy loaded 
     * when needed. See {@link #developsFromRels}.
     * 
     * @return  A {@code Set} of {@code OWLObjectPropertyExpression}s containing the "develops_from" 
     *          {@code OWLObjectPropertyExpression}, and all its children.
     */
    private Set<OWLObjectPropertyExpression> getDevelopsFromProps() {
        log.entry();
        if (this.developsFromRels == null) {
            this.developsFromRels = this.getWrapper().getSubPropertyReflexiveClosureOf(
                    this.getWrapper().getOWLObjectPropertyByIdentifier(DEVELOPS_FROM_ID));
        }
        return log.exit(this.developsFromRels);
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
     * Determines whether {@code edge} is a immediately_preceded_by relation (see 
     * {@link #IMMEDIATELY_PRECEDED_BY_ID}).
     * 
     * @param edge  The {@code OWLGraphEdge} to test for being a immediately_preceded_by relation.
     * @return      {@code true} if {@code edge} is a immediately_preceded_by-related relation.
     */
    public boolean isImmediatelyPrecededByRelation(OWLGraphEdge edge) {
        log.entry(edge);
        
        return log.exit(edge.getQuantifiedPropertyList().size() == 1 && 

                edge.getSingleQuantifiedProperty().getProperty() != null &&
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
            return log.exit(
                    edge.getSingleQuantifiedProperty().getProperty() != null &&
                    this.getPrecededByProps().contains(
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

                edge.getSingleQuantifiedProperty().getProperty() != null &&
                this.getPartOfProps().contains(
                    edge.getSingleQuantifiedProperty().getProperty()) && 
                    
                    edge.getSingleQuantifiedProperty().isSomeValuesFrom());
    }

    /**
     * Determines whether {@code edge} is a transformation_of-related relation, meaning, 
     * having a {@code OWLObjectProperty} corresponding to "transformation_of" (see 
     * {@link #TRANSFORMATION_OF_ID}) or any of its {@code OWLObjectProperty} children.
     * 
     * @param edge  The {@code OWLGraphEdge} to test for being a transformation_of-related relation.
     * @return      {@code true} if {@code edge} is a transformation_of-related relation.
     */
    public boolean isTransformationOfRelation(OWLGraphEdge edge) {
        log.entry(edge);
        
        return log.exit(edge.getQuantifiedPropertyList().size() == 1 && 
                
                edge.getSingleQuantifiedProperty().getProperty() != null &&
                this.getTransformationOfProps().contains(
                    edge.getSingleQuantifiedProperty().getProperty()) && 
                    
                    edge.getSingleQuantifiedProperty().isSomeValuesFrom());
    }

    /**
     * Determines whether {@code edge} is a develops_from-related relation, meaning, 
     * having a {@code OWLObjectProperty} corresponding to "develops_from" (see 
     * {@link #DEVELOPS_FROM_ID}) or any of its {@code OWLObjectProperty} children.
     * 
     * @param edge  The {@code OWLGraphEdge} to test for being a develops_from-related relation.
     * @return      {@code true} if {@code edge} is a develops_from-related relation.
     */
    public boolean isDevelopsFromRelation(OWLGraphEdge edge) {
        log.entry(edge);
        
        return log.exit(edge.getQuantifiedPropertyList().size() == 1 && 
                
                edge.getSingleQuantifiedProperty().getProperty() != null &&
                this.getDevelopsFromProps().contains(
                    edge.getSingleQuantifiedProperty().getProperty()) && 
                    
                    edge.getSingleQuantifiedProperty().isSomeValuesFrom());
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
    //suppress warning because the getAncestors method of owltools uses unparameterized 
    //generic OWLPropertyExpression, so we need to do the same. 
    public int getMinDistance(OWLClass source, OWLClass target, 
            @SuppressWarnings("rawtypes") Set<OWLPropertyExpression> overProps) {
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
            
            for (OWLGraphEdge outgoingEdge: this.getWrapper().getOutgoingEdgesWithGCI(currentClass)) {
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
                    } else if (this.getWrapper().getNamedAncestorsWithGCI(potentialNextStep, overProps).
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
    //suppress warning because the getAncestors method of owltools uses unparameterized 
    //generic OWLPropertyExpression, so we need to do the same. 
    public Set<OWLClass> getLeastCommonAncestors(OWLClass cls1, OWLClass cls2, 
            @SuppressWarnings("rawtypes") Set<OWLPropertyExpression> overProps) {
        log.entry(cls1, cls2, overProps);
        
        //in case one of the class is the ancestor of the other
        Set<OWLClass> providedClasses = new HashSet<OWLClass>();
        providedClasses.add(cls1);
        providedClasses.add(cls2);
        this.retainParentClasses(providedClasses, overProps);
        //if collection has changed as a result
        if (providedClasses.size() == 1) {
            return log.exit(providedClasses);
        }
        
        Set<OWLClass> commonAncestors = new HashSet<OWLClass>();
        Set<OWLNamedObject> ancestorsStart = this.getWrapper().getNamedAncestorsWithGCI(cls1, overProps);
        Set<OWLNamedObject> ancestorsEnd = this.getWrapper().getNamedAncestorsWithGCI(cls2, overProps);
        ancestorsStart.retainAll(ancestorsEnd);
        for (OWLObject ancestor: ancestorsStart) {
            if (ancestor instanceof OWLClass && !this.isObsolete(ancestor)) {
                commonAncestors.add((OWLClass) ancestor);
            }
        }
        Set<OWLClass> lcas = new HashSet<OWLClass>(commonAncestors);
        for (OWLObject ancestor: commonAncestors) {
            lcas.removeAll(this.getWrapper().getNamedAncestorsWithGCI(ancestor, overProps));
        }
        
        return log.exit(lcas);
    }

    /**
     * Retain only independent leaf {@code OWLClass}es over the specified properties 
     * from the provided {@code Set} {@code classes}. This method will remove 
     * from {@code classes} any {@code OWLClass} that is the ancestor of one of 
     * the other {@code OWLClass}es in the {@code Set}, over the properties {@code overProps}.
     * 
     * @param classes       A {@code Set} of {@code OWLClass}es to filter to remove 
     *                      ancestors of other {@code OWLClass}es in the {@code Set}.
     * @param overProps     A {@code Set} of {@code OWLPropertyExpression}s allowing 
     *                      to restrain the relations considered. Can be {@code null} 
     *                      for no restrictions.
     */
    //suppress warning because the getAncestors method of owltools uses unparameterized 
    //generic OWLPropertyExpression, so we need to do the same. 
    public void retainLeafClasses(Set<OWLClass> classes,  
            @SuppressWarnings("rawtypes") Set<OWLPropertyExpression> overProps) {
        log.entry(classes, overProps);
        this.retainRelativeClasses(classes, overProps, true);
        log.exit();
    }

    /**
     * Retain only parent {@code OWLClass}es over the specified properties 
     * from the provided {@code Set} {@code classes}. This method will remove 
     * from {@code classes} any {@code OWLClass} that is the child of one of 
     * the other {@code OWLClass}es in the {@code Set}, over the properties {@code overProps}.
     * 
     * @param classes       A {@code Set} of {@code OWLClass}es to filter to remove 
     *                      children of other {@code OWLClass}es in the {@code Set}.
     * @param overProps     A {@code Set} of {@code OWLPropertyExpression}s allowing 
     *                      to restrain the relations considered. Can be {@code null} 
     *                      for no restrictions.
     */
    //suppress warning because the getAncestors method of owltools uses unparameterized 
    //generic OWLPropertyExpression, so we need to do the same. 
    public void retainParentClasses(Set<OWLClass> classes,  
            @SuppressWarnings("rawtypes") Set<OWLPropertyExpression> overProps) {
        log.entry(classes, overProps);
        this.retainRelativeClasses(classes, overProps, false);
        log.exit();
    }

    /**
     * Modify {@code classes} to either retain leaf classes or parent classes over 
     * the specified {@code Set} of {@code OWLPropertyExpression}, depending on 
     * {@code retainLeaves}. {@code classes} will be modified as a result of this call 
     * (optional operation).
     * 
     * @param classes       A {@code Set} of {@code OWLClass}es to be filtered.
     * @param overProps     A {@code Set} of {@code OWLPropertyExpression}s allowing 
     *                      to restrain the relations considered. Can be {@code null} 
     *                      for no restrictions.
     * @param retainLeaves  A {@code boolean} indicating, when {@code true}, that leaves 
     *                      should be retained. Otherwise, parent classes should be retain 
     *                      and child classes removed.
     * @see #retainLeafClasses(Set, Set)
     * @see #retainParentClasses(Set, Set)
     */
    //suppress warning because the getAncestors method of owltools uses unparameterized 
    //generic OWLPropertyExpression, so we need to do the same. 
    private void retainRelativeClasses(Set<OWLClass> classes,  
            @SuppressWarnings("rawtypes") Set<OWLPropertyExpression> overProps, 
            boolean retainLeaves) {
        log.entry(classes, overProps, retainLeaves);
        
        Set<OWLObject> toRemove = new HashSet<OWLObject>();
        //we need to take cycles into account, otherwise we could exclude all classes. 
        //to do that we will associate each class to its ancestors, and filter afterwards
        Map<OWLNamedObject, Set<OWLNamedObject>> clsToAncestors = 
                new HashMap<OWLNamedObject, Set<OWLNamedObject>>();
        
        for (OWLClass cls: classes) {
            Set<OWLNamedObject> ancestors = 
                    this.getWrapper().getNamedAncestorsWithGCI(cls, overProps);
            //just to be sure, in case of cycles?
            ancestors.remove(cls);
            //discard obsolete classes
            Set<OWLNamedObject> obsoletes = new HashSet<OWLNamedObject>();
            for (OWLNamedObject obj: ancestors) {
                if (this.isObsolete(obj)) {
                    obsoletes.add(obj);
                }
            }
            ancestors.removeAll(obsoletes);
            clsToAncestors.put(cls, ancestors);
            log.trace("Relatives retrieved for {}: {}", cls, ancestors);
        }
        for (Entry<OWLNamedObject, Set<OWLNamedObject>> clsToAncestor: clsToAncestors.entrySet()) {
            OWLNamedObject iteratedObj = clsToAncestor.getKey();
            //if an ancestor of iteratedObj has also iteratedObj as an ancestor, 
            //discard the ancestor. This is needed only in case retainLeaves is false.
            if (!retainLeaves) {
                Set<OWLNamedObject> ancestorsCopy = 
                        new HashSet<OWLNamedObject>(clsToAncestor.getValue());
                for (OWLNamedObject ancestor: clsToAncestor.getValue()) {
                    Set<OWLNamedObject> ancestorsAncestor = clsToAncestors.get(ancestor);
                    if (ancestorsAncestor != null && 
                            ancestorsAncestor.contains(iteratedObj)) {
                        ancestorsCopy.remove(ancestor);
                        log.trace("Discaring {}, ancestor of {}, because of cycles.", ancestor, 
                                iteratedObj);
                    }
                } 
                if (!Collections.disjoint(classes, ancestorsCopy)) {
                    toRemove.add(iteratedObj);
                }
            } else {
                toRemove.addAll(clsToAncestor.getValue());
            }
        }
          
        classes.removeAll(toRemove);
        log.trace("Resulting Set after filtering: {}", classes);
        
        log.exit();
    }
    
    /**
     * Determine whether {@code classes} contain unrelated {@code OWLClass}es. 
     * This means that there is at least one {@code OWLClass} in {@code classes} that is 
     * neither the ancestor nor the descendant of any other {@code OWLClass} in {@code classes}.
     * 
     * @param classes   A {@code Collection} of {@code OWLClass}es to check for presence 
     *                  of unrelated {@code OWLClass}es.
     * @return  A {@code boolean} that is {@code true} if {@code classes}
     *          contain unrelated {@code OWLClass}es.
     * @see #containsUnrelatedClassesByIsAPartOf(Collection, Collection)
     */
    public boolean containsUnrelatedClassesByIsAPartOf(Collection<OWLClass> classes) {
        log.entry(classes);
        return log.exit(this.containsUnrelatedClassesByIsAPartOf(classes, classes));
    }
    
    /**
     * Compare two {@code Collection}s of {@code OWLClass}es to determine whether 
     * they contain unrelated {@code OWLClass}es. This means that there is at least one 
     * {@code OWLClass} in {@code classes1} that is absent from {@code classes2}, 
     * and that has no ancestor or descendant through is_a or part_of relations 
     * in {@code classes2}. GCI relations are taken into account.
     * <p>
     * {@code classes1} and {@code classes2} are interchangeable, the {@code boolean} 
     * returned by this method will be the same.
     * 
     * @param classes1  A {@code Collection} of {@code OWLClass}es to check for presence 
     *                  of unrelated {@code OWLClass}es in {@code classes2}.
     * @param classes2  A {@code Collection} of {@code OWLClass}es to check for presence 
     *                  of unrelated {@code OWLClass}es in {@code classes1}.
     * @return  A {@code boolean} that is {@code true} if {@code classes1} and {@code classes2} 
     *          contain unrelated {@code OWLClass}es.
     * @see #containsUnrelatedClassesByIsAPartOf(Collection)
     */
    public boolean containsUnrelatedClassesByIsAPartOf(Collection<OWLClass> classes1, 
            Collection<OWLClass> classes2) {
        log.entry(classes1, classes2);
        
        for (OWLClass cls1: classes1) {
            log.trace("Examining : {}", cls1);
            //retrieve ancestors and descendants of cls1, and add itself to the Collection
            Set<OWLNamedObject> relatedClasses = new HashSet<OWLNamedObject>();
            relatedClasses.addAll(this.getWrapper().getNamedAncestorsWithGCI(cls1, 
                    this.getGenericPartOfProps()));
            relatedClasses.addAll(this.getWrapper().getOWLClassDescendantsWithGCI(cls1, 
                    this.getGenericPartOfProps()));
            relatedClasses.add(cls1);
            log.trace("Related OWLClasses: {}", relatedClasses);
            //copy OWLClasses to compare to, to be able to remove terms from it
            Set<OWLClass> testClasses = new HashSet<OWLClass>(classes2);
            testClasses.removeAll(relatedClasses);
            if (!testClasses.isEmpty()) {
                log.trace("Unrelated OWLClasses in Collection to compare to: {}", testClasses);
                return log.exit(true);
            }
            log.trace("No Unrelated OWLClasses in Collection to compare to.");
        }
        
        return log.exit(false);
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
    
    /**
     * @return  A {@code String} that is the path to the ontology wrapped by this object, 
     *          if loaded from a file.
     */
    public String getPathToOntology() {
        return this.pathToOntology;
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
