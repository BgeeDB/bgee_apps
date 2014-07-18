package org.bgee.pipeline.uberon;

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
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.CommandRunner;
import org.bgee.pipeline.ontologycommon.OntologyUtils;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLPropertyExpression;

import owltools.graph.OWLGraphEdge;
import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLQuantifiedProperty.Quantifier;

/**
 * Class related to the use of Uberon (as {@link Uberon}), but dedicated to manage 
 * the developmental stage ontology part.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class UberonDevStage extends UberonCommon {
    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(UberonDevStage.class.getName());
    
    /**
     * The {@code Pattern} used to parse comments of {@code OWLClass}es to search for 
     * temporal ordering in comments (used in FBdv). The group conrresponding to 
     * the ordering value is {@link #TEMPORAL_COMMENT_GROUP}.
     * @see #generateStageOntology()
     */
    public final static Pattern TEMPORAL_COMMENT_PATTERN = 
            Pattern.compile(".*?Temporal ordering number - ([0-9]+?)\\D*?$");
    /**
     * An {@code int} that is the index of the group capturing the temporal ordering 
     * in the {@code Pattern} {@link #TEMPORAL_COMMENT_PATTERN}.
     */
    public final static int TEMPORAL_COMMENT_GROUP = 1;
    
    /**
     * Several actions can be launched from this main method, depending on the first 
     * element in {@code args}: 
     * <ul>
     * <li>If the first element in {@code args} is "generateStageOntology", the action 
     * will be to extract from the Uberon ontology the developmental stages subgraph, 
     * and to save it to files in OBO and OWL formats, 
     * see {@link #generateStageOntologyAndSaveToFile()}.
     * Following elements in {@code args} must then be: 
     *   <ol>
     *   <li>path to the file storing the Uberon ontology, see {@link #setPathToUberonOnt(String)}.
     *   <li>path to use to generate the files storing the resulting 
     *   ontology in OBO and OWL. The prefixes ".owl" or ".obo" will be automatically added. 
     *   See {@link #setModifiedOntPath(String)}.
     *   <li>A list of OBO-like IDs of {@code OWLClass}es to remove from the ontology, 
     *   and to propagate their incoming edges to their outgoing edges. These IDs must be 
     *   separated by the {@code String} {@link CommandRunner#LIST_SEPARATOR}. 
     *   See {@link #setClassIdsToRemove(Collection)}.
     *   <li>A list of OBO-like IDs of {@code OWLClass}es for which we want to remove 
     *   all their children, reachable by any path in their graph closure. 
     *   The {@code OWLClass}es themselves will not be removed. 
     *   See {@link #setChildrenOfToRemove(Collection)}.
     *   <li>A list of OBO-like IDs of the {@code OWLClass}es that are the roots 
     *   of the subgraphs that will be kept in the ontology. These IDs must be 
     *   separated by the {@code String} {@link CommandRunner#LIST_SEPARATOR}. 
     *   See {@link #setToFilterSubgraphRootIds(Collection)}.
     *   </ol>
     *   Example of command line usage for this task (assuming taxon constraints were 
     *   already generated, so that you don't need the "in_taxon" relations): 
     *   {@code java -Xmx2g -jar myJar 
     *   Uberon generateStageOntology ext.owl dev_stage_ont  
     *   UBERON:0000067,UBERON:0000071,UBERON:0000105,UBERON:0000000 
     *   UBERON:0000069 
     *   BFO:0000050,BFO:0000062,RO:0002087
     *   UBERON:0000104,FBdv:00000000}
     * </ul>
     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
     * @throws IllegalArgumentException If {@code args} does not contain the proper 
     *                                  parameters or does not allow to obtain 
     *                                  correct information.
     */
    public static void main(String[] args) throws OWLOntologyCreationException, 
        OBOFormatParserException, IOException, IllegalArgumentException, 
        OWLOntologyStorageException {
        log.entry((Object[]) args);
        
        if (args[0].equalsIgnoreCase("generateStageOntology")) {
            if (args.length != 7) {
                throw log.throwing(new IllegalArgumentException(
                        "Incorrect number of arguments provided, expected " + 
                        "7 arguments, " + args.length + " provided."));
            }
            
            UberonDevStage ub = new UberonDevStage(args[1]);
            ub.setModifiedOntPath(args[2]);
            ub.setClassIdsToRemove(CommandRunner.parseListArgument(args[3]));
            ub.setChildrenOfToRemove(CommandRunner.parseListArgument(args[4]));
            ub.setRelIds(CommandRunner.parseListArgument(args[5]));
            ub.setToFilterSubgraphRootIds(CommandRunner.parseListArgument(args[6]));
            
            
            ub.generateStageOntologyAndSaveToFile();
            
        } else {
            throw log.throwing(new UnsupportedOperationException("The following action " +
                    "is not recognized: " + args[0]));
        }
        
        log.exit();
    }

    /**
     * A {@code Collection} of {@code String}s that are the OBO-like IDs of {@code OWLClass}es 
     * for which we want to remove all their children, reachable by any path 
     * in their graph closure. The {@code OWLClass}es will not be removed. 
     */
    private Collection<String> childrenOfToRemove;
    /**
     * A {@code Map} associating a nested set model (as values), to the least common ancestor 
     * it was computed for (as keys). This will allow to avoid recomputing a nested set model 
     * for each query.
     * @see #generateStageNestedSetModel(OWLClass)
     */
    //TODO: create a NestedSetModel class, rather than this ugly 
    //Map<OWLClass, Map<String, Integer>>
    private final Map<OWLClass, Map<OWLClass, Map<String, Integer>>> nestedSetModels;
    
    /**
     * A {@code Set} of {@code OWLPropertyExpression} that can be conveniently used 
     * to query for relations and/or relatives only over part_of relations,
     */
    //suppress warning because the getAncestors method of owltools uses unparameterized 
    //generic OWLPropertyExpression, so we need to do the same. 
    @SuppressWarnings("rawtypes")
    private final Set<OWLPropertyExpression> overPartOf;
    
    
    /**
     * Default constructor private in purpose, an ontology should always be provided somehow.
     */
    @SuppressWarnings("unused")
    private UberonDevStage() {
        this((OntologyUtils) null);
    }
    
    /**
     * Constructor providing the path to the Uberon ontology to used to perform operations.
     * 
     * @param pathToUberon  A {@code String} that is the path to the Uberon ontology. 
     * @throws OWLOntologyCreationException If an error occurred while loading the ontology.
     * @throws OBOFormatParserException     If the ontology is malformed.
     * @throws IOException                  If the file could not be read. 
     */
    public UberonDevStage(String pathToUberon) throws OWLOntologyCreationException, 
    OBOFormatParserException, IOException {
        this(new OntologyUtils(pathToUberon));
    }
    /**
     * Constructor providing the path to the Uberon ontology to used to perform operations, 
     * and the path the a file containing taxon constraints, as parsable by 
     * {@link TaxonConstraints#extractTaxonConstraints(String)}.
     * 
     * @param pathToUberon  A {@code String} that is the path to the Uberon ontology. 
     * @param pathToUberon  A {@code String} that is the path to the taxon constraints. 
     * @throws OWLOntologyCreationException If an error occurred while loading the ontology.
     * @throws OBOFormatParserException     If the ontology is malformed.
     * @throws IOException                  If the file could not be read. 
     */
    public UberonDevStage(String pathToUberon, String pathToTaxonConstraints) 
            throws OWLOntologyCreationException, OBOFormatParserException, IOException {
        this(new OntologyUtils(pathToUberon), 
                TaxonConstraints.extractTaxonConstraints(pathToTaxonConstraints));
    }
    /**
     * Constructor providing the {@code OntologyUtils} used to perform operations, 
     * wrapping the Uberon ontology that will be used. 
     * 
     * @param ontUtils  the {@code OntologyUtils} that will be used. 
     */
    public UberonDevStage(OntologyUtils ontUtils) {
        this(ontUtils, null);
    }
    /**
     * Constructor providing the {@code OntologyUtils} used to perform operations, 
     * wrapping the Uberon ontology that will be used, and the taxon constraints 
     * that will be used to identify to which species stages belong. It is not necessary 
     * to provide those if the ontology contains only one species.  
     * 
     * @param ontUtils          the {@code OntologyUtils} that will be used. 
     * @param taxonConstraints  A {@code Map} where keys are IDs of the Uberon 
     *                          {@code OWLClass}es, and values are {@code Set}s 
     *                          of {@code Integer}s containing the IDs of taxa 
     *                          in which the {@code OWLClass} exists.
     */
    //suppress warning because OWLGraphWrapper uses non-parameterized generic types, 
    //so we need to do the same.
    @SuppressWarnings("rawtypes")
    public UberonDevStage(OntologyUtils ontUtils, Map<String, Set<Integer>> taxonConstraints) {
        super(ontUtils);
        this.nestedSetModels = new HashMap<OWLClass, Map<OWLClass, Map<String, Integer>>>();
        this.overPartOf = Collections.unmodifiableSet(new HashSet<OWLPropertyExpression>(
                Arrays.asList(this.getOntologyUtils().getWrapper().
                        getOWLObjectPropertyByIdentifier(OntologyUtils.PART_OF_ID))));
        
        this.setTaxonConstraints(taxonConstraints);
    }
    
    
    

    /**
     * Modify the Uberon ontology stored in the file provided through 
     * {@link #setPathToUberonOnt(String)} to make a deelopmental stage ontology, and saves it 
     * in OWL and OBO format in the path provided through {@link #setModifiedOntPath(String)}. 
     * <p>
     * This method calls {@link #generateStageOntology(OWLOntology)}, by loading the 
     * {@code OWLOntology} provided, and using attributes set before calling this method. 
     * Attributes that are used can be set prior to calling this method through the methods: 
     * {@link #setClassIdsToRemove(Collection)}, {@link #setToRemoveSubgraphRootIds(Collection)}, 
     * {@link #setToFilterSubgraphRootIds(Collection)}, and 
     * {@link #setChildrenOfToRemove(Collection)}.
     * <p>
     * The resulting {@code OWLOntology} is then saved, in OBO (with a ".obo" extension 
     * to the path provided through {@link #setModifiedOntPath(String)}), 
     * and in OWL (with a ".owl" extension to the path {@link #setModifiedOntPath(String)}).
     * 
     * @throws IOException                      If an error occurred while reading the file 
     *                                          {@code pathToUberonOnt}.
     * @throws OWLOntologyCreationException     If an error occurred while loading 
     *                                          the ontology to modify it.
     * @throws UnknownOWLOntologyException      If an error occurred while loading 
     *                                          the ontology to modify it.
     * @throws OWLOntologyStorageException      If an error occurred while saving the resulting 
     *                                          ontology in OWL.
     */
    public void generateStageOntologyAndSaveToFile() throws OWLOntologyCreationException, 
            IOException, IllegalArgumentException, OWLOntologyStorageException {
        //we provide to the entry methods all class attributes that will be used 
        //(use to be arguments of this method)
        log.entry(this.getPathToUberonOnt(), this.getModifiedOntPath(), 
                this.getClassIdsToRemove(), this.getChildrenOfToRemove(), 
                this.getRelIds(), this.getToFilterSubgraphRootIds());
        
        this.generateStageOntology();

        //save ontology
        this.getOntologyUtils().saveAsOWL(this.getModifiedOntPath() + ".owl");
        this.getOntologyUtils().saveAsOBO(this.getModifiedOntPath() + ".obo", false);
        
        log.exit();
    }
    
    /**
     * Generates a developmental stage ontology extracted from {@code uberonOnt}, then 
     * removes the {@code OWLAnnotationAssertionAxiom}s that are problematic to convert 
     * the ontology in OBO (using 
     * {@link org.bgee.pipeline.OntologyUtils#removeOBOProblematicAxioms()}). 
     * This method is very similar to {@link #simplifyUberon(OWLOntology)}, 
     * but the simplification process for the developmental stages is much simpler and faster. 
     * Also, all {@code OWLEquivalentClassesAxiom}s are removed before modifying the ontology.
     * This is because there is a bug /where species-specific stages are dangling 
     * because of their EC axioms. By removing these axioms, these classes will be removed 
     * during graph filtering.
     * <p>
     * Note that the {@code OWLOntology} passed as argument will be modified as a result 
     * of the call to this method.
     * <p>
     * Operations that are performed, in order:
     * <ul>
     * <li>{@code OWLGraphManipulator#removeClassAndPropagateEdges(String)} on each of the 
     * {@code String} part of the {@code Collection} returned by {@link #getClassIdsToRemove()}.
     * <li>remove all children, reachable by any path in their graph closure, 
     * of the {@code OWLClass}es with their OBO-like IDs returned by {@link #getChildrenOfToRemove()}. 
     * <li>{@code OWLGraphManipulator#filterSubgraphs(Collection)} with value returned by 
     * {@link #getToFilterSubgraphRootIds()}.
     * <li>{@code OWLGraphManipulator#reducePartOfIsARelations()}
     * <li>{@link org.bgee.pipeline.OntologyUtils#removeOBOProblematicAxioms()}
     */
    public void generateStageOntology() {
        //we provide to the entry methods all class attributes that will be used 
        //(use to be arguments of this method)
        log.entry(this.getClassIdsToRemove(), this.getChildrenOfToRemove(), 
                this.getRelIds(), this.getToFilterSubgraphRootIds());
        
        //before using OWLGraphManipulator, we remove all EquivalentClass axioms. 
        //This is because there is a bug 
        //where species-specific stages are dangling thanks to their EC axioms. 
        //they will be converted by the manipulator as is_a relations, while we want 
        //to merge these classes, so, to make them disappear.
        log.debug("Removing all OWLEquivalentClassesAxioms...");
        for (OWLOntology ont: this.getOntologyUtils().getWrapper().getAllOntologies()) {
            ont.getOWLOntologyManager().removeAxioms(ont, 
                    ont.getAxioms(AxiomType.EQUIVALENT_CLASSES));
        }
        log.debug("Done removing OWLEquivalentClassesAxioms.");
        
        //now, we can safely use the manipulator
        OWLGraphManipulator manipulator = this.getOntologyUtils().getManipulator();

        //potential terms to call this code on: 
        //UBERON:0000067 embryo stage part
        //UBERON:0000071 death stage
        //UBERON:0000105 life cycle stage
        //UBERON:0000000 processual entity
        if (this.getClassIdsToRemove() != null) {
            for (String classIdToRemove: this.getClassIdsToRemove()) {
                manipulator.removeClassAndPropagateEdges(classIdToRemove);
            }
        }
        
        //remove all children of childrenOfToRemove
        //potential terms to call this code on: UBERON:0000069 "larval stage"
        if (this.getChildrenOfToRemove() != null) {
            for (String parentId: this.getChildrenOfToRemove()) {
                OWLClass parent = manipulator.getOwlGraphWrapper().getOWLClassByIdentifier(parentId);
                //in case it was an IRI and not an OBO-like ID
                if (parent == null) {
                    parent = manipulator.getOwlGraphWrapper().getOWLClass(parentId);
                }
                if (parent == null) {
                    throw log.throwing(new IllegalArgumentException("A parent term whose children " +
                            "shoud be removed could not be found: " + parentId));
                }
                
                //remove children
                for (OWLClass child: manipulator.getOwlGraphWrapper().getOWLClassDescendants(parent)) {
                    if (!manipulator.removeClass(child)) {
                        throw log.throwing(new AssertionError("An OWLClass could not be removed: " + 
                                child));
                    }
                    log.debug("Child of {} removed: {}", parent, child);
                } 
            }
        }
        
        //potential rel IDs to keep (assuming taxon constraints were already generated, 
        //so that you don't need the "in_taxon" relations): 
        //OntologyUtils.PART_OF_ID
        //OntologyUtils.PRECEDED_BY_ID
        //OntologyUtils.IMMEDIATELY_PRECEDED_BY_ID
        if (this.getRelIds() != null && !this.getRelIds().isEmpty()) {
            manipulator.mapRelationsToParent(this.getRelIds());
            manipulator.filterRelations(this.getRelIds(), true);
        }
        
        //potential subgraph root to keep: UBERON:0000104 life cycle, FBdv:00000000 Drosophila life 
        if (this.getToFilterSubgraphRootIds() != null && !this.getToFilterSubgraphRootIds().isEmpty()) {
            for (String classIdRemoved: manipulator.filterSubgraphs(this.getToFilterSubgraphRootIds())) {
                this.getClassesRemoved().put(classIdRemoved, 
                        "Filtering of subgraph with root IDs" + this.getToFilterSubgraphRootIds());
            }
        }   
        
        manipulator.reduceRelations();
        manipulator.reducePartOfIsARelations();
        
        this.getOntologyUtils().removeOBOProblematicAxioms();
        
        log.exit();
    }
    
    /**
     * Generate preceded_by relation from temporal ordering in comments. This is the only way 
     * to properly order FBdv classes. The comment pattern to retrieve is 
     * {@link #TEMPORAL_COMMENT_PATTERN}. The {@code OWLOntology} wrapped by this object 
     * will be modified as a result, by adding preceded_by relations between 
     * {@code OWLClass}es in {@code classesToOrder}.
     * 
     * @param classesToOrder    A {@code Set} of {@code OWLClass}es that needs to be ordered 
     *                          thanks to temporal ordering in comments. 
     */
    public void generatePrecededByFromComments(Set<OWLClass> classesToOrder) {
        log.entry(classesToOrder);
        
        OWLGraphManipulator manipulator = this.getOntologyUtils().getManipulator();
        
        //now, we transform temporal ordering in comments into preceded_by relations 
        //(it is the only way to correctly use FBdv)
        NavigableMap<Integer, OWLClass> commentOrdering = new TreeMap<Integer, OWLClass>();
        for (OWLClass cls: classesToOrder) {
             String comment = this.getOntologyUtils().getWrapper().getComment(cls);
             if (StringUtils.isNotBlank(comment)) {
                 log.trace("Examining comment to search for temporal ordering: {}", 
                         comment);
                 Matcher m = UberonDevStage.TEMPORAL_COMMENT_PATTERN.matcher(comment);
                 if (m.matches()) {
                     int order = Integer.parseInt(m.group(UberonDevStage.TEMPORAL_COMMENT_GROUP));
                     commentOrdering.put(order, cls);
                     log.trace("Valid comment, order extracted: {}", order);
                 }
             }
        }
        log.trace("Classes ordered from comments: {}", commentOrdering);
        
        //create the relations by iterating in reversed order. Wd don't use 
        //a descendingMap, because we will need to use a List anyway, to retrieve 
        //an OWLClass along with its predecessor.
        List<OWLClass> orderedClasses = new ArrayList<OWLClass>(commentOrdering.values());
        OWLObjectPropertyExpression precededBy = 
                manipulator.getOwlGraphWrapper().getOWLObjectPropertyByIdentifier(
                        OntologyUtils.PRECEDED_BY_ID);
        Set<OWLGraphEdge> edgesToAdd = new HashSet<OWLGraphEdge>();
        for (int i = orderedClasses.size() - 1; i > 0; i--) {
            edgesToAdd.add(new OWLGraphEdge(
                    orderedClasses.get(i), orderedClasses.get(i-1), 
                    precededBy, Quantifier.SOME, 
                    manipulator.getOwlGraphWrapper().getSourceOntology()));
        }
        log.trace("{} edges to add: {}", edgesToAdd.size(), edgesToAdd);
        manipulator.addEdges(edgesToAdd);
        
        log.exit();
    }
    
    /**
     * Compute a nested set model from a developmental stage ontology. The stage ontology 
     * should have been provided at instantiation. {@code root} will be considered 
     * as the root which to start nested set model computations from (as if it was 
     * the actual root of the ontology). {@code OWLClass}es will be ordered 
     * according to their immediately_preceded_by and preceded_by relations, and 
     * the nested set model computed using the method {@link 
     * org.bgee.pipeline.OntologyUtils#computeNestedSetModelParams(List)}.
     * <p>
     * As the developmental stage ontology can include several species, 
     * {@link #getTaxonConstraints()} will be used so that stages from different species 
     * are not tried to be ordered relative to each others (they will not have any precedence 
     * relations between them). If {@link #getTaxonConstraints()} returns {@code null}, 
     * then it should be possible to order all stages relative to each others. 
     * <p>
     * The generated nested set models will be stored, associated to {@code root}, 
     * to avoid recomputing them for each query. 
     * 
     * @param root              An {@code OWLClass} that will be considered as the root 
     *                          of the ontology to start the conputations from.
     * @return      See {@link org.bgee.pipeline.OntologyUtils#computeNestedSetModelParams(List)} 
     *              for details about values returned. 
     * @see org.bgee.pipeline.OntologyUtils#computeNestedSetModelParams(List)
     */
    public Map<OWLClass, Map<String, Integer>> generateStageNestedSetModel(OWLClass root) {
        log.entry(root, this.getTaxonConstraints());
        
        //check if we have a nested set model in cache for this root
        Map<OWLClass, Map<String, Integer>> nestedSetModel = this.nestedSetModels.get(root);
        if (nestedSetModel != null) {
            log.trace("Retrieving nested set model from cache of class {}", root);
            return log.exit(nestedSetModel);
        }
        //then check if we have a nested set model in cache for one of its ancestor
        Set<OWLObject> ancestors = 
                this.getOntologyUtils().getWrapper().getAncestors(root, this.overPartOf);
        ancestors.retainAll(this.nestedSetModels.keySet());
        if (!ancestors.isEmpty()) {
            //select any ancestor with a nested set model in cache
            OWLObject ancestor = ancestors.iterator().next();
            log.trace("Retrieving nested set model from cache of class {}", ancestor);
            return log.exit(this.nestedSetModels.get(ancestor));
        }
        
        //otherwise, let's compute the nested set model for this root.
        OWLGraphWrapper wrapper = this.getOntologyUtils().getWrapper();
        
        //get the ordering between sibling OWLClasses according to preceded_by relations. 
        //This is enough to compute the nested set model. To do that, we walk each level 
        //starting from root, using a Deque
        Deque<OWLClass> walker = new ArrayDeque<OWLClass>();
        walker.add(root);
        //we don't care of the ordering of non-sibling taxa, 
        //as long as sibling taxa are ordered. 
        List<OWLClass> globalOrdering = new ArrayList<OWLClass>();
        OWLClass classWalked = null;
        while ((classWalked = walker.pollFirst()) != null) {
            //order the direct children of a same species, or multi-species children together.
            //store the children associated to their NCBI tax ID (or to null, if they are 
            //multi-species children).
            //Use a TreeMap so that ordering between species is predictable
            Map<Integer, Set<OWLClass>> children = new TreeMap<Integer, Set<OWLClass>>();
            for (OWLGraphEdge incomingEdge: wrapper.getIncomingEdges(classWalked)) {
                if ((this.getOntologyUtils().isPartOfRelation(incomingEdge) || 
                        this.getOntologyUtils().isASubClassOfEdge(incomingEdge)) && 
                        incomingEdge.isSourceNamedObject()) {
                  
                    OWLClass child = (OWLClass) incomingEdge.getSource();
                    Set<Integer> speciesIds = null;
                    if (this.getTaxonConstraints() != null) {
                        speciesIds = this.getTaxonConstraints().get(
                            this.getOntologyUtils().getWrapper().getIdentifier(child));
                    }
                    Integer speciesKey = 0;
                    if (speciesIds != null && speciesIds.size() == 1) {
                        speciesKey = speciesIds.iterator().next();
                    }
                    log.trace("Child {} assigned to species key {}", child, speciesKey);
                    if (!children.containsKey(speciesKey)) {
                        children.put(speciesKey, new HashSet<OWLClass>());
                    }
                    children.get(speciesKey).add(child);
                    walker.offerLast(child);
                }
            }
            if (!children.isEmpty()) {
                for (Set<OWLClass> sameSpeciesChildren: children.values()) {
                    globalOrdering.addAll(this.orderByPrecededBy(sameSpeciesChildren));
                }
            }
        }
        
        nestedSetModel = this.getOntologyUtils().computeNestedSetModelParams(root, 
                globalOrdering, this.overPartOf);
        this.nestedSetModels.put(root, nestedSetModel);
        
        return log.exit(nestedSetModel);
    }
    
    /**
     * Delegates to {@link #getStageIdsBetween(String, String, int)} with the last {@code int} 
     * parameter equals to 0. In that case, the returned stage IDs will belong to any species. 
     * 
     * @param startStageId  See same argument in {@link #getStageIdsBetween(String, String, int)}
     * @param endStageId    See same argument in {@link #getStageIdsBetween(String, String, int)}
     * @return              See returned value in {@link #getStageIdsBetween(String, String, int)}
     */
    public List<String> getStageIdsBetween(String startStageId, String endStageId) {
        log.entry(startStageId, endStageId);
        return log.exit(this.getStageIdsBetween(startStageId, endStageId, 0));
    }
    
    /**
     * Delegates to {@link #getStageIdsBetween(String, String, Map, int)} with the last {@code Map} 
     * parameter {@code null}. This method allows to specify to which species 
     * the stage IDs returned should belong to. And, as the {@code Map} argument 
     * will be {@code null}, the nested set model will be computed directly 
     * (see documentation of delegate method). 
     * 
     * @param startStageId  See same argument in {@link #getStageIdsBetween(String, String, Map, int)}
     * @param endStageId    See same argument in {@link #getStageIdsBetween(String, String, Map, int)}
     * @param speciesId     See same argument in {@link #getStageIdsBetween(String, String, Map, int)}
     * @return              See returned value in {@link #getStageIdsBetween(String, String, Map, int)}
     */
    public List<String> getStageIdsBetween(String startStageId, String endStageId, 
            int speciesId) {
        log.entry(startStageId, endStageId, speciesId);
        return log.exit(this.getStageIdsBetween(startStageId, endStageId, null, speciesId));
    }
    
    /**
     * Retrieve the OBO-like IDs of the developmental stages occurring between the stages 
     * with IDs {@code startStageId} and {@code endStageId}, and belonging to species 
     * with ID {@code speciesId}. If {@code speciesId} is equal to 0, then stages 
     * from any species will be returned. To achieve this task, 
     * either the {@code providedNestedModel} will be used, or, if {@code null}, 
     * a nested set model is computed for the ontology wrapped by this object 
     * (provided before calling this method through {@link #setPathToUberonOnt(String)}), 
     * starting from the least common ancestor of the start and end stages, using 
     * part_of relations for ancestry between stages, and immediately_preceded_by 
     * and preceded_by for chronological relations between stages (see {@link 
     * #generateStageNestedSetModel(OWLClass)}). 
     * <p>
     * Note that if it is needed to compute a nested set model, taxon constraints 
     * should have been provided, unless the ontology used contains only one species. 
     * See {@link #generateStageNestedSetModel(OWLClass)} for more details.
     * 
     * @param startStageId  A {@code String} that is the OBO-like ID of the start  
     *                      developmental stage.
     * @param endStageId    A {@code String} that is the OBO-like ID of the end  
     *                      developmental stage.
     * @param providedNestedModel   A {@code Map} associating {@code OWLClass}es 
     *                              of the ontology to a {@code Map} containing 
     *                              their left bound, right bound, and level, see 
     *                              {@link org.bgee.pipeline.OntologyUtils#computeNestedSetModelParams(
     *                              OWLClass, List, Set)} 
     *                              for more details.
     * @param speciesId     An {@code int} that is the NCBI ID of the species for which 
     *                      we want to retrieve stages. 
     * @return              A {@code List} of {@code String}s that are the OBO-like IDs 
     *                      of stages occurring between start and end stages, ordered 
     *                      by chronological order. 
     * @see #generateStageNestedSetModel(OWLClass)
     */
    public List<String> getStageIdsBetween(String startStageId, String endStageId, 
            Map<OWLClass, Map<String, Integer>> providedNestedModel, int speciesId) {
        log.entry(startStageId, endStageId, providedNestedModel, speciesId);
        
        List<String> stageIdsBetween = new ArrayList<String>();
        OWLGraphWrapper wrapper = this.getOntologyUtils().getWrapper();

        OWLClass startStage = this.getOntologyUtils().getOWLClass(startStageId);
        OWLClass endStage = this.getOntologyUtils().getOWLClass(endStageId);
        if (startStage == null) {
            throw log.throwing(new IllegalArgumentException("Could not find any OWLClass " +
                    "corresponding to " + startStageId));
        }
        if (endStage == null) {
            throw log.throwing(new IllegalArgumentException("Could not find any OWLClass " +
                    "corresponding to " + endStageId));
        }
        if (!this.existsInSpecies(startStage, speciesId)) {
            throw log.throwing(new IllegalArgumentException("Start stage " + startStageId + 
                    " does not belong to the requested species " + speciesId));
        }
        if (!this.existsInSpecies(endStage, speciesId)) {
            throw log.throwing(new IllegalArgumentException("End stage " + endStageId + 
                    " does not belong to the requested species " + speciesId));
        }
        
        //identity case
        if (startStage.equals(endStage)) {
            stageIdsBetween.add(startStageId);
        } else {
            //now we obtain a nested set model 
            //this nested set model will be used in a comparator, we make it final.
            final Map<OWLClass, Map<String, Integer>> nestedModel;
            if (providedNestedModel != null) {
                nestedModel = providedNestedModel;
            } else {
                //first, get the least common ancestor of the two stages over part_of relation
                Set<OWLClass> lcas = this.getOntologyUtils().getLeastCommonAncestors(startStage, endStage, 
                        this.overPartOf);
                
                //the part_of graph should be a tree, so, only one OWLClass lca
                if (lcas.size() != 1) {
                    throw log.throwing(new IllegalStateException("The developmental stages " +
                            "used does not represent a tree over part_of relations, " +
                            "cannot continue. Least common ancestors of start and end stages :" + 
                            lcas));
                }
                OWLClass lca = lcas.iterator().next();
                
                nestedModel = this.generateStageNestedSetModel(lca);
            }
            //get the parameters related to startStage and endStage
            int startLeftBound = nestedModel.get(startStage).get(OntologyUtils.LEFT_BOUND_KEY);
            int startRightBound = nestedModel.get(startStage).get(OntologyUtils.RIGHT_BOUND_KEY);
            int startLevel = nestedModel.get(startStage).get(OntologyUtils.LEVEL_KEY);
            int endLeftBound = nestedModel.get(endStage).get(OntologyUtils.LEFT_BOUND_KEY);
            int endRightBound = nestedModel.get(endStage).get(OntologyUtils.RIGHT_BOUND_KEY);
            int endLevel = nestedModel.get(endStage).get(OntologyUtils.LEVEL_KEY);
            int maxLevel = Math.max(startLevel, endLevel);
            
            //now we get all stages belonging to the requested species, 
            //between start and end stages, with a level not greater 
            //than the max level between start and end stage. 
            Set<OWLClass> selectedStages = new HashSet<OWLClass>();
            for (Entry<OWLClass, Map<String, Integer>> entry: nestedModel.entrySet()) {
                OWLClass stage = entry.getKey();
                Map<String, Integer> params = entry.getValue();
                if (this.existsInSpecies(stage, speciesId) && 
                    params.get(OntologyUtils.LEFT_BOUND_KEY) >= startLeftBound && 
                        params.get(OntologyUtils.LEFT_BOUND_KEY) <= endLeftBound && 
                    params.get(OntologyUtils.RIGHT_BOUND_KEY) >= startRightBound && 
                            params.get(OntologyUtils.RIGHT_BOUND_KEY) <= endRightBound && 
                    params.get(OntologyUtils.LEVEL_KEY) <= maxLevel) {
                    
                    selectedStages.add(stage);
                }
            }
            
            //now remove the ancestors of the selected stages, so that we keep only 
            //the most precise and independent selected stages
            Set<OWLObject> ancestors = new HashSet<OWLObject>();
            for (OWLClass selectedStage: selectedStages) {
                ancestors.addAll(wrapper.getAncestors(selectedStage, this.overPartOf));
            }
            selectedStages.removeAll(ancestors);
            
            //finally, order the stages using their left bound
            List<OWLClass> sortedStages = new ArrayList<OWLClass>(selectedStages);
            Collections.sort(sortedStages, new Comparator<OWLClass>() {
                @Override
                public int compare(OWLClass o1, OWLClass o2) {
                    return nestedModel.get(o1).get(OntologyUtils.LEFT_BOUND_KEY) - 
                            nestedModel.get(o2).get(OntologyUtils.LEFT_BOUND_KEY);
                }
              });
            
            //transform the List of OWLClasses into a List of Strings
            for (OWLClass sortedStage: sortedStages) {
                stageIdsBetween.add(wrapper.getIdentifier(sortedStage));
            }
        }
        
        return log.exit(stageIdsBetween);
    }
    
    /**
     * Order the provided {@code OWLClass}es according to their immediately_preceded_by 
     * or preceded_by relations. Usually, {@code classesToOrder} should contain sibling 
     * {@code OWLClass}es, direct descendants of a same {@code OWLClass} by part_of relations.
     * But it can be used with any {@code Set} of {@code OWLClass}es, as long as relations 
     * are consistent (no cycles of preceded_by, no missing preceded_by between the provided 
     * {@code OWLClass}es, etc).
     * <p>
     * Additionnaly, additional preceded_by relations are faked thanks to temporal ordering 
     * in comments (see {@link #generatePrecededByFromComments(Set)})
     * 
     * @param classesToOrder    A {@code Set} of {@code OWLClass}es to order according to 
     *                          their immediately_preceded_by or preceded_by relations.
     * @return                  A {@code List} where {@code OWLClass}es are ordered, with 
     *                          first occurring {@code OWLClass} at first position, last 
     *                          occurring {@code OWLClass} at last position.
     */
    public List<OWLClass> orderByPrecededBy(Set<OWLClass> classesToOrder) {
        log.entry(classesToOrder);
        
        OWLGraphWrapper wrapper = this.getOntologyUtils().getWrapper();
        List<OWLClass> orderedClasses = new ArrayList<OWLClass>();
        
        this.generatePrecededByFromComments(classesToOrder);
        
        //first, we look for the last class, the only one with no preceded_by  
        //or immediately_preceded_by relations incoming from other OWLClass in classesToOrder. 
        //getLastClassByPrecededBy never returns null, it throws and exception otherwise.
        OWLClass lastClass = this.getLastClassByPrecededBy(classesToOrder);
        
        //now, we walk from lastClass, following the preceded_by relations
        OWLClass precedingClass = lastClass;
        while (precedingClass != null) {
            log.debug("Walking {}", precedingClass);
            
            if (orderedClasses.contains(precedingClass)) {
                throw log.throwing(new IllegalStateException("Cycle of preceded_by relations " +
                        "among the following OWLClasses: " + classesToOrder));
            }
            
            orderedClasses.add(0, precedingClass);
            //we check everything even if we have reached the beginning of the chain 
            //(classesToOrder.size() == orderedClasses.size()), to be sure there is 
            //no cycle of preceded_by relations
            log.trace("All ordered classes so far ({}/{}): {}", orderedClasses.size(), 
                    classesToOrder.size(), orderedClasses);
            
            OWLClass potentialPrecedingClass = null;
            //again, we want to use direct outgoing edges, but sometimes we need to check 
            //also indirect outgoing edges (this is slow). So we'll try indirect edges 
            //only if conditions are not satisfied with direct edges.
            boolean directEdgesAlreadyTried = false;
            //- if potentialPrecedingClass is still null after having tried indirect edges, 
            //while we haven't reach the end of the chain 
            //(classesToOrder.size() != orderedClasses.size()), an exception will be thrown.
            //- if we have reached the end of the chain 
            //(classesToOrder.size() == orderedClasses.size()), we still want to do a last 
            //iteration on the first class to make sure there are no cycles. The loop 
            //will be break if potentialPrecedingClass is null just after having tried 
            //direct edges, if classesToOrder.size() == orderedClasses.size()
            potentialPrecedingClass: while (potentialPrecedingClass == null) {
                log.trace("Trying to find predecessor for {} (using indirect edges? {})", 
                        precedingClass, directEdgesAlreadyTried);
                Set<OWLGraphEdge> outgoingEdges;
                if (!directEdgesAlreadyTried) {
                    outgoingEdges = wrapper.getOutgoingEdges(precedingClass);
                } else {
                    outgoingEdges = wrapper.getOutgoingEdgesNamedClosureOverSupProps(precedingClass);
                }
                
                //check first the immediately_preceded_by relations, there should be only one 
                //leading to sibling OWLClasses
                for (OWLGraphEdge outgoingEdge: outgoingEdges) {
                    if (this.getOntologyUtils().isImmediatelyPrecededByRelation(outgoingEdge)) {
                        
                        Set<OWLClass> classesMatching = this.getEqualOrParentsBelongingTo(
                                outgoingEdge.getTarget(), classesToOrder);
                        if (classesMatching.isEmpty()) {
                            continue;
                        }
                        boolean problem = false;
                        if (classesMatching.size() > 1) {
                            problem = true;
                        } else {
                            OWLClass clsMatching = classesMatching.iterator().next();
                            if (potentialPrecedingClass != null && 
                                    !potentialPrecedingClass.equals(clsMatching)) {
                                problem = true;
                            }
                            potentialPrecedingClass = clsMatching;
                        }
                        if (problem) {
                            //several immediately_preceded_by relations leading to 
                            //different OWLClasses; this is bad whatever it is 
                            //with direct or indirect edges
                            throw log.throwing(new IllegalStateException(
                                    "An OWLClass has several immediately_preceded_by relations " +
                                    "to several same level OWLClasses (" + precedingClass + 
                                    "), among the following OWLClasses: " + classesToOrder));
                        }
                        
                        log.debug("Preceding class found by immediately_preceded_by (through indirect edges?: {}): {}", 
                                directEdgesAlreadyTried, potentialPrecedingClass);
                        //continue iteration anyway to check for several immediately_preceded_by
                    }
                }
                
                //no immediately_preceded_by to sibling OWLClasses, maybe we have 
                //simple preceded_by relations? Check only if we haven't reach the end 
                //of the chain (classesToOrder.size() != orderedClasses.size())
                if (potentialPrecedingClass == null && 
                        classesToOrder.size() != orderedClasses.size()) {
                    log.debug("No preceding class identified by immediately_preceded_by, checking preceded_by");
                    Set<OWLClass> allPrecededBy = new HashSet<OWLClass>();
                    //iterate once again the outgoing edges
                    for (OWLGraphEdge outgoingEdge: outgoingEdges) {
                        //but check also for simple preceded_by relations 
                        if (this.getOntologyUtils().isPrecededByRelation(outgoingEdge)) {
                            
                            Set<OWLClass> classesMatching = this.getEqualOrParentsBelongingTo(
                                    outgoingEdge.getTarget(), classesToOrder);
                            if (!classesMatching.isEmpty()) {
                                allPrecededBy.addAll(classesMatching);
                                log.debug("Potential preceding class found by preceded_by: {}", 
                                        classesMatching);
                            }
                        }
                    }
                    if (allPrecededBy.size() == 1) {
                        potentialPrecedingClass = allPrecededBy.iterator().next();
                    } else if (allPrecededBy.size() > 1) {
                        log.debug("Several potential preceding classes, try to find the last one.");
                        //we can have preceded_by relations to several terms. In that case, we need 
                        //to know what is the last of these stages. 
                        potentialPrecedingClass = this.getLastClassByPrecededBy(allPrecededBy);
                    }
                    if (potentialPrecedingClass != null) {
                        log.debug("Preceding class found by preceded_by (through indirect edges?: {}): {}", 
                                directEdgesAlreadyTried, potentialPrecedingClass);
                    }
                }
                
                
                //if we haven't found any predecessor, while it is not yet the end 
                //of the chain, and while we have already tested both direct and indirect 
                //edges, then it means we have a problem
                if (potentialPrecedingClass == null && 
                        classesToOrder.size() != orderedClasses.size() && 
                        directEdgesAlreadyTried) {
                    throw log.throwing(new IllegalStateException(
                            "An OWLClass has no preceded_by relations " +
                                    "to same level OWLClasses (" + precedingClass + "), " +
                                    "among the following " + classesToOrder));
                }
                
                
                //otherwise, if we haven't found any predecessor here, and if it was 
                //the end of the chain, it means it was the last iteration to check for cycles, 
                //that there was none and we can break the loop
                if (potentialPrecedingClass == null && 
                        classesToOrder.size() == orderedClasses.size()) {
                    log.trace("End of the chain, everything is fine.");
                    precedingClass = potentialPrecedingClass;//set precedingClass to null
                    break potentialPrecedingClass;
                } else if (potentialPrecedingClass == null) {
                    //here it means that it is not the end of the chain, but we haven't try 
                    //indirect edges yet, the potentialPrecedingClass loop will now 
                    //be iterated once again to use indirect edges
                    log.trace("No predecessor found, trying with indirect edges...");
                    directEdgesAlreadyTried = true;
                } else {
                    //otherwise, we have found a predecessor, potentialPrecedingClass loop 
                    //will exit
                    precedingClass = potentialPrecedingClass;
                    log.trace("Predecessor found: {}", precedingClass);
                }
            }
        }
        
        return log.exit(orderedClasses);
    }
    
    /**
     * Identify the last {@code OWLClass} according to preceded_by relations 
     * among {@code classesToOrder}. The last class is the only one with no preceded_by  
     * or immediately_preceded_by relations incoming from other {@code OWLClass}es 
     * in {@code classesToOrder}. A subtlety is that the incoming preceded_by 
     * relations can actually be propagated from a child.
     * @param classesToOrder    A {@code Set} of {@code OWLClass}es for which we want 
     *                          to identify the last one according to 
     *                          their immediately_preceded_by or preceded_by relations.
     * @return                  The {@code OWLClass} that is the last occurring one 
     *                          among {@code classesToOrder}. This returned value 
     *                          is never {@code null} (an exception would be thrown otherwise).  
     */
    public OWLClass getLastClassByPrecededBy(Set<OWLClass> classesToOrder) {
        log.entry(classesToOrder);
        
        OWLGraphWrapper wrapper = this.getOntologyUtils().getWrapper();
        
        OWLClass lastClass = null;
        //we want to use direct outgoing edges, but sometimes we need to check 
        //also indirect outgoing edges (this is slow). So we'll try indirect edges 
        //only if conditions are not satisfied with direct edges.
        boolean directEdgesAlreadyTried = false;
        //if lastClass is still null after having tried both direct and indirect edges,
        //an exception will be thrown
        while (lastClass == null) {
            Set<OWLClass> withSuccessors = new HashSet<OWLClass>();
            for (OWLClass classToOrder: classesToOrder) {
                log.trace("Examining OWLClass {} - Second try with indirect edges: {}", 
                        classToOrder, directEdgesAlreadyTried);
                Set<OWLGraphEdge> outgoingEdges;
                if (!directEdgesAlreadyTried) {
                    outgoingEdges = wrapper.getOutgoingEdges(classToOrder);
                } else {
                    outgoingEdges = wrapper.getOutgoingEdgesNamedClosureOverSupProps(classToOrder);
                }
                for (OWLGraphEdge outgoingEdge: outgoingEdges) {
                    log.trace("Testing if edge is valid preceded_by relation: {}", 
                            outgoingEdge);
                    if (this.getOntologyUtils().isPrecededByRelation(outgoingEdge)) {
                        Set<OWLClass> predecessors = this.getEqualOrParentsBelongingTo(
                                outgoingEdge.getTarget(), classesToOrder);
                        if (!predecessors.isEmpty()) {
                            withSuccessors.addAll(predecessors);
                            log.trace("Valid preceded_by relation leading to predecessors: {}", 
                                    predecessors);
                        }
                    }
                }
            }
            log.trace("All classes with successors: {}", withSuccessors);
            
            Set<OWLClass> classesSubstracted = new HashSet<OWLClass>(classesToOrder);
            classesSubstracted.removeAll(withSuccessors);
            log.trace("Potential last classes: {}", classesSubstracted);
            
            if (classesSubstracted.size() > 1 && directEdgesAlreadyTried) {
                throw log.throwing(new IllegalStateException("The provided ontology " +
                        "is missing some preceded_by relations: several OWLClasses " +
                        "with no preceded_by relations incoming from same level OWLClasses: " +
                        classesSubstracted + " (among the following: " + classesToOrder + ")"));
            }
            //this one is bad whatever the try is with direct or indirect edges
            if (classesSubstracted.isEmpty()) {
                throw log.throwing(new IllegalStateException("Cycle of preceded_by relations " +
                        "among same level OWLClasses, not possible to determine the last one, " +
                        "among: " + classesToOrder));
            }
            if (classesSubstracted.size() == 1) {
                lastClass = classesSubstracted.iterator().next();
                log.debug("Last class of the chain identified: {}", lastClass);
            }
            directEdgesAlreadyTried = true;
        }
        return log.exit(lastClass);
    }
    
    /**
     * Returns the {@code OWLClass}es in {@code classes} that are equal to {@code cls} 
     * or that are ancestors of {@code cls} over part_of relations.
     * 
     * @param cls       An {@code OWLObject} to use to identify equal or ancestral 
     *                  {@code OWLClass}es present in {@code classes}.
     * @param classes   A {@code Set} of {@code OWLClass}es that are equal to or ancestor of 
     *                  {@code cls} over part_of relations.
     * @return          A {@code Set} of {@code OWLClass}es that are a subset of {@code classes}, 
     *                  equal to or ancestor of {@code cls} over part_of relations.
     */
    private Set<OWLClass> getEqualOrParentsBelongingTo(OWLObject cls, Set<OWLClass> classes) {
        log.entry(cls, classes);
        Set<OWLClass> matches = new HashSet<OWLClass>();
        if (classes.contains(cls)) {
            matches.add((OWLClass) cls);
        } else {
            //test if cls is a child of some of the OWLClasses in classes
            Set<OWLObject> partOfAncestors = this.getOntologyUtils().getWrapper().getAncestors(cls, 
                    this.overPartOf);
            //partOfAncestors.retainAll(classes);
            for (OWLObject ancestor: partOfAncestors) {
                if (classes.contains(ancestor)) {
                    matches.add((OWLClass) ancestor);
                }
            }
        }
        return log.exit(matches);
    }


    /**
     * @return  A {@code Collection} of {@code String}s that are the OBO-like IDs 
     *          of {@code OWLClass}es for which we want to remove all their children, 
     *          reachable by any path in their graph closure. 
     *          The {@code OWLClass}es will not be removed. 
     * @see #setChildrenOfToRemove(Collection)
     */
    public Collection<String> getChildrenOfToRemove() {
        return childrenOfToRemove;
    }
    /**
     * Sets the parameter returned by {@link #getChildrenOfToRemove()}.
     * 
     * @param childrenOfToRemove    See {@link #getChildrenOfToRemove()}.
     * @see #getChildrenOfToRemove()
     */
    public void setChildrenOfToRemove(Collection<String> childrenOfToRemove) {
        this.childrenOfToRemove = childrenOfToRemove;
    }
}
