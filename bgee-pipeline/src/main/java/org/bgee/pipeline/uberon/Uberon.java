package org.bgee.pipeline.uberon;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.CommandRunner;
import org.bgee.pipeline.Utils;
import org.bgee.pipeline.ontologycommon.OntologyUtils;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;

import owltools.graph.OWLGraphEdge;
import owltools.graph.OWLGraphManipulator;
import owltools.graph.OWLGraphWrapper;

/**
 * Class for general operations using Uberon. For operations specific 
 * to developmental stage ontology, see {@link UberonDevStage}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class Uberon extends UberonCommon {
    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = 
            LogManager.getLogger(Uberon.class.getName());
    

    /**
     * Several actions can be launched from this main method, depending on the first 
     * element in {@code args}: 
     * <ul>
     * <li>If the first element in {@code args} is "extractTaxonIds", the action 
     * will be to extract from the Uberon ontology all NCBI taxon IDs that are the targets 
     * of {@code OWLRestriction}s over the object properties "in taxon" (or any 
     * sub-properties), or that are used in ontology annotations 
     * "treat-xrefs-as-reverse-genus-differentia", and to write them in a file.
     * Following elements in {@code args} must then be: 
     *   <ol>
     *   <li>path to the Uberon ontology (a version making use of such restrictions...).
     *   <li>path to the output file where to write taxon IDs into, one per line.
     *   </ol>
     * <li>If the first element in {@code args} is "simplifyUberon", the action 
     * will be to simplify the Uberon ontology and to save it to files in OBO and OWL formats, 
     * see {@link #simplifyUberonAndSaveToFile()}.
     * Following elements in {@code args} must then be: 
     *   <ol>
     *   <li>path to the file storing the Uberon ontology, see {@link #setPathToUberonOnt(String)}.
     *   <li>path to use to generate the files storing the resulting 
     *   ontology in OBO and OWL. The prefixes ".owl" or ".obo" will be automatically added. 
     *   See {@link #setModifiedOntPath(String)}.
     *   <li>path to a file storing information about classes removed. 
     *   See {@link #setClassesRemovedFilePath(String)}.
     *   <li>A list of OBO-like IDs of {@code OWLClass}es to remove from the ontology, 
     *   and to propagate their incoming edges to their outgoing edges. These IDs must be 
     *   separated by the {@code String} {@link CommandRunner#LIST_SEPARATOR}. 
     *   See {@link #setClassIdsToRemove(Collection)}.
     *   <li>A list of OBO-like IDs or {@code IRI}s of relations to be filtered 
     *   and mapped to parent relations. These IDs must be separated by the {@code String} 
     *   {@link CommandRunner#LIST_SEPARATOR}. See {@link #setRelIds(Collection)}.
     *   <li>A list of OBO-like IDs of the {@code OWLClass}es that are the roots 
     *   of the subgraphs to be removed from the ontology. These IDs must be 
     *   separated by the {@code String} {@link CommandRunner#LIST_SEPARATOR}. 
     *   See {@link #setToRemoveSubgraphRootIds(Collection)}.
     *   <li>A list of OBO-like IDs of the {@code OWLClass}es that are the roots 
     *   of the subgraphs that will be kept in the ontology. These IDs must be 
     *   separated by the {@code String} {@link CommandRunner#LIST_SEPARATOR}. 
     *   See {@link #setToFilterSubgraphRootIds(Collection)}.
     *   <li>A list of names of targeted subsets, for which member {@code OWLClass}es 
     *   should have their is_a/part_of incoming edges removed. These IDs must be 
     *   separated by the {@code String} {@link CommandRunner#LIST_SEPARATOR}. 
     *   See {@link #setSubsetNames(Collection)}.
     *   <li>A list of OBO-like IDs of {@code OWLClass}es whose incoming edges 
     *   should not be removed, even if member of a subset listed in the previous argument. 
     *   See {@link #setClassIdsExcludedFromSubsetRemoval(Collection)}.
     *   <li>a map specifying specific relations to remove between pairs of {@code OWLClass}es. 
     *   In a key-value pair, the key should be the OBO-like ID of the source of relations 
     *   to remove, the value being the target of the relations to remove. Key-value pairs 
     *   must be separated by {@link CommandRunner#LIST_SEPARATOR}, keys must be  
     *   separated from their associated value by {@link CommandRunner#KEY_VALUE_SEPARATOR}. 
     *   A key can be associated to several values. See {@link #setRelsBetweenToRemove(Map)}.
     *   </ol>
     *   Example of command line usage for this task: {@code java -Xmx2g -jar myJar 
     *   Uberon simplifyUberon composite-metazoan.owl custom_composite simplification_composite.tsv 
     *   UBERON:0001062,UBERON:0000465,UBERON:0000061,UBERON:0010000,UBERON:0008979 
     *   BFO:0000050,RO:0002202,RO:0002494 
     *   UBERON:0000467/UBERON:0000468,UBERON:0000475/UBERON:0000468,UBERON:0000479/UBERON:0000468,UBERON:0000480/UBERON:0000468,UBERON:0007688/UBERON:0000468,UBERON:0010707/UBERON:0000468,UBERON:0012641/UBERON:0000468,UBERON:0002199/UBERON:0000468,UBERON:0002416/UBERON:0000468,UBERON:0007376/UBERON:0000468,UBERON:0000463/UBERON:0000468,UBERON:0001048/UBERON:0000468,UBERON:0007567/UBERON:0000468,UBERON:0015119/UBERON:0000468 
     *   NBO:0000313,GO:0008150,ENVO:01000254,BFO:0000040,GO:0003674,PATO:0000001,CHEBI:24431,UBERON:0004458,UBERON:0000466,SO:0000704 
     *   UBERON:0013701,UBERON:0000026,UBERON:0000480,UBERON:0000479,UBERON:0000468,GO:0005575 
     *   grouping_class,non_informative,ubprop:upper_level,upper_level 
     *   UBERON:0013701,UBERON:0000026,UBERON:0000480,UBERON:0000479,UBERON:0011676,GO:0005575}
     *   
     * <li>If the first element in {@code args} is "extractXRefMappings", the action will be 
     * to retrieve mappings from XRef IDs to Uberon IDs from Uberon, and to save them 
     * to a TSV file, see {@link #saveXRefMappingsToFile(String, String)} for details.
     * Following elements in {@code args} must then be: 
     *   <ol>
     *   <li>path to the Uberon ontology.
     *   <li>path to the output file where to save the mappings.
     *   </ol>
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
        
        if (args[0].equalsIgnoreCase("extractTaxonIds")) {
            if (args.length != 3) {
                throw log.throwing(new IllegalArgumentException(
                        "Incorrect number of arguments provided, expected " + 
                        "3 arguments, " + args.length + " provided."));
            }
            
            new Uberon(args[1]).extractTaxonIds(args[2]);

// NOTE May 13 2014: this method seems now completely useless, to remove if it is confirmed.
//        } else if (args[0].equalsIgnoreCase("extractDevelopmentRelatedRelations")) {
//            if (args.length != 4) {
//                throw log.throwing(new IllegalArgumentException(
//                        "Incorrect number of arguments provided, expected " + 
//                        "4 arguments, " + args.length + " provided."));
//            }
//            
//            new Uberon().extractRelatedEdgesToOutputFile(args[1], args[2], args[3]);
            
        } else if (args[0].equalsIgnoreCase("simplifyUberon")) {
            if (args.length != 11) {
                throw log.throwing(new IllegalArgumentException(
                        "Incorrect number of arguments provided, expected " + 
                        "11 arguments, " + args.length + " provided."));
            }
            
            Uberon ub = new Uberon(args[1]);
            ub.setModifiedOntPath(args[2]);
            ub.setClassesRemovedFilePath(args[3]);
            ub.setClassIdsToRemove(CommandRunner.parseListArgument(args[4]));
            ub.setRelIds(CommandRunner.parseListArgument(args[5]));
            ub.setRelsBetweenToRemove(CommandRunner.parseMapArgument(args[6]));
            ub.setToRemoveSubgraphRootIds(CommandRunner.parseListArgument(args[7]));
            ub.setToFilterSubgraphRootIds(CommandRunner.parseListArgument(args[8]));
            ub.setSubsetNames(CommandRunner.parseListArgument(args[9]));
            ub.setClassIdsExcludedFromSubsetRemoval(CommandRunner.parseListArgument(args[10]));
            
            
            ub.simplifyUberonAndSaveToFile();
            
        } else if (args[0].equalsIgnoreCase("extractXRefMappings")) {
            if (args.length != 3) {
                throw log.throwing(new IllegalArgumentException(
                        "Incorrect number of arguments provided, expected " + 
                        "3 arguments, " + args.length + " provided."));
            }
            new Uberon(args[1]).saveXRefMappingsToFile(args[2]);
        } else {
            throw log.throwing(new UnsupportedOperationException("The following action " +
                    "is not recognized: " + args[0]));
        }
        
        log.exit();
    }

    /**
     * A {@code Collection} of {@code String}s that are the names of the targeted subsets, 
     * for which member {@code OWLClass}es should have their is_a/part_of 
     * incoming edges removed (only if the source of the incoming edge 
     * will not be left orphan of other is_a/part_of relations to {@code OWLClass}es 
     * not in {@code subsets}. First argument when calling 
     * {@code OWLGraphManipulator#removeRelsToSubsets(Collection, Collection)}.
     */
    private Collection<String> subsetNames;
    /**
     * A {@code Collection} of {@code String}s that are the OBO-like IDs 
     * of {@code OWLClass}es whose incoming edges should not be removed even if member 
     * of a subset listed in {@link #subsetNames}. Second argument when calling 
     * {@code OWLGraphManipulator#removeRelsToSubsets(Collection, Collection)}.
     */
    private Collection<String> classIdsExcludedFromSubsetRemoval;
    
    
    /**
     * Default constructor private in purpose, an ontology should always be provided somehow.
     */
    @SuppressWarnings("unused")
    private Uberon() {
        this((OntologyUtils) null);
    }
    
    /**
     * Constructor providing the path to the Uberon ontology to used to perforn operations.
     * 
     * @param pathToUberon  A {@code String} that is the path to the Uberon ontology. 
     * @throws OWLOntologyCreationException If an error occurred while loading the ontology.
     * @throws OBOFormatParserException     If the ontology is malformed.
     * @throws IOException                  If the file could not be read. 
     */
    public Uberon(String pathToUberon) throws OWLOntologyCreationException, 
    OBOFormatParserException, IOException {
        this(new OntologyUtils(pathToUberon));
    }
    /**
     * Constructor providing the {@code OntologyUtils} used to perform operations, 
     * wrapping the Uberon ontology that will be used. 
     * 
     * @param ontUtils  the {@code OntologyUtils} that will be used. 
     */
    public Uberon(OntologyUtils ontUtils) {
        super(ontUtils);
    }
    
    /**
     * Simplifies the Uberon ontology stored in the file provided through 
     * {@link #setPathToUberonOnt(String)}, and saves it in OWL and OBO format 
     * in the path provided through {@link #setModifiedOntPath(String)}. Information about 
     * the {@code OWLClass}es that were removed as a result of the simplification is stored 
     * in a separated file, provided through {@link #setClassesRemovedFilePath(String)} (see 
     * {@code #saveSimplificationInfo} method). This attribute can be left {@code null} 
     * or blank if this information does not need to be stored. 
     * <p>
     * This method calls {@link #simplifyUberon(OWLOntology)}, by loading the {@code OWLOntology} 
     * provided, and using attributes set before calling this method. Attributes that are used 
     * can be set prior to calling this method through the methods: 
     * {@link #setClassIdsToRemove(Collection)}, {@link #setToRemoveSubgraphRootIds(Collection)}, 
     * {@link #setToFilterSubgraphRootIds(Collection)}, {@link #setSubsetNames(Collection)}, 
     * {@link #setClassIdsExcludedFromSubsetRemoval(Collection)}, and 
     * {@link #setRelsBetweenToRemove(Map)}.
     * <p>
     * The resulting {@code OWLOntology} is then saved, in OBO (with a ".obo" extension 
     * to the path provided through {@link #setModifiedOntPath(String)}), 
     * and in OWL (with a ".owl" extension to the path {@link #setModifiedOntPath(String)}).
     * 
     * @throws IOException                      If an error occurred while reading the file 
     *                                          returned by {@link #getPathToUberonOnt()}.
     * @throws OBOFormatParserException         If the ontology was provided in OBO format 
     *                                          and a parser error occurred. 
     * @throws OWLOntologyCreationException     If an error occurred while loading 
     *                                          the ontology to modify it.
     * @throws UnknownOWLOntologyException      If an error occurred while loading 
     *                                          the ontology to modify it.
     * @throws OWLOntologyStorageException      If an error occurred while saving the resulting 
     *                                          ontology in OWL.
     * @see #simplifyUberon(OWLOntology)
     */
    public void simplifyUberonAndSaveToFile() throws UnknownOWLOntologyException, 
            OWLOntologyCreationException, OBOFormatParserException, IOException, 
            OWLOntologyStorageException {
        //we provide to the entry methods all class attributes that will be used 
        //(use to be arguments of this method)
        log.entry(this.getPathToUberonOnt(), this.getModifiedOntPath(), 
                this.getClassesRemovedFilePath(), this.getClassIdsToRemove(), 
                this.getRelsBetweenToRemove(), this.getRelIds(), 
                this.getToRemoveSubgraphRootIds(), 
                this.getToFilterSubgraphRootIds(), this.getSubsetNames(), 
                this.getClassIdsExcludedFromSubsetRemoval());
        
        this.simplifyUberon();

        //save ontology
        this.getOntologyUtils().saveAsOWL(this.getModifiedOntPath() + ".owl");
        //we do not check the structure of the ontology to generate the OBO version, 
        //with the composite ontology there are too many problems.
        this.getOntologyUtils().saveAsOBO(this.getModifiedOntPath() + ".obo", false);
        
        //save information about the simplification process if requested
        if (StringUtils.isNotBlank(this.getClassesRemovedFilePath())) {
            //we need the original ontology, as before the simplification, 
            //so we reload the ontology
            this.saveSimplificationInfo(OntologyUtils.loadOntology(this.getPathToUberonOnt()), 
                    this.getClassesRemovedFilePath(), this.getClassesRemoved());
        }
        
        log.exit();
    }
    
    /**
     * Simplifies {@code uberonOnt} by using an {@code OWLGraphManipulator}. This method 
     * calls various methods of {@code owltools.graph.OWLGraphManipulator} using the attributes 
     * set before calling this method, then removes the {@code OWLAnnotationAssertionAxiom}s 
     * that are problematic to convert the ontology in OBO, using 
     * {@link org.bgee.pipeline.OntologyUtils#removeOBOProblematicAxioms()}.
     * <p>
     * Note that the {@code OWLOntology} passed as argument will be modified as a result 
     * of the call to this method. Information about the simplification process 
     * can be retrieved afterwards, (see {@link #getClassesRemoved()}), or saved to a file (see 
     * {@link #saveSimplificationInfo(OWLOntology, String, Map)}).
     * <p>
     * Operations that are performed, in order:
     * <ul>
     * <li>Convert taxonomy Equivalent Classes Axioms into Xrefs and remove the targeted classes.
     * <li>{@code OWLGraphManipulator#removeClassAndPropagateEdges(String)} on each of the 
     * {@code String} part of the {@code Collection} returned by {@link #getClassIdsToRemove()}.
     * <li>{@code OWLGraphManipulator#removeDirectEdgesBetween(String, String)}, called for 
     * each {@code Entry}s in the {@code Map} returned by {@link #getRelsBetweenToRemove()}.
     * <li>{@code OWLGraphManipulator#reduceRelations()} and 
     * {@code OWLGraphManipulator#reducePartOfIsARelations()}
     * <li>{@code OWLGraphManipulator#mapRelationsToParent(Collection)} using value returned by 
     * {@link #getRelIds()}.
     * <li>{@code OWLGraphManipulator#filterRelations(Collection, boolean)} using value 
     * returned by {@link #getRelIds()}, with second argument {@code true}.
     * <li>{@code OWLGraphManipulator#removeSubgraphs(Collection, boolean, Collection)} 
     * with value returned by {@link #getToRemoveSubgraphRootIds()} as first argument, 
     * with second argument {@code true}, and with value returned by 
     * {@link #getToFilterSubgraphRootIds()} as third argument.
     * <li>{@code OWLGraphManipulator#filterSubgraphs(Collection)} with value returned by 
     * {@link #getToFilterSubgraphRootIds()}.
     * <li>{@code OWLGraphManipulator#removeRelsToSubsets(Collection, Collection)} using 
     * value returned by {@link #getSubsetNames()} as first argument, returned by 
     * {@link #getClassIdsExcludedFromSubsetRemoval()} as second argument.
     * <li>{@link org.bgee.pipeline.OntologyUtils#removeOBOProblematicAxioms()}
     * </ul>
     *  
     * @param uberonOnt                         The {@code OWLOntology} to simplify.
     * @throws UnknownOWLOntologyException      If an error occurred while wrapping 
     *                                          the {@code uberonOnt} into an 
     *                                          {@code OWLGraphManipulator}.
     */
    public void simplifyUberon() throws UnknownOWLOntologyException {
        //we provide to the entry methods all class attributes that will be used 
        //(use to be arguments of this method)
        log.entry(this.getClassIdsToRemove(), this.getRelsBetweenToRemove(), 
                this.getRelIds(), this.getToRemoveSubgraphRootIds(), 
                this.getToFilterSubgraphRootIds(), this.getSubsetNames(), 
                this.getClassIdsExcludedFromSubsetRemoval());
        
        //convert taxon ECAs
        this.convertTaxonECAs();
        
        OWLGraphManipulator manipulator = this.getOntologyUtils().getManipulator();

        if (this.getClassIdsToRemove() != null) {
            for (String classIdToRemove: this.getClassIdsToRemove()) {
                manipulator.removeClassAndPropagateEdges(classIdToRemove);
            }
        }
        
        if (this.getRelsBetweenToRemove() != null) {
            for (Entry<String, Set<String>> relsToRemove: this.getRelsBetweenToRemove().entrySet()) {
                for (String targetId: relsToRemove.getValue()) {
                    manipulator.removeDirectEdgesBetween(relsToRemove.getKey(), targetId);
                }
            }
        }
        
        manipulator.reduceRelations();
        manipulator.reducePartOfIsARelations();
        
        if (this.getRelIds() != null && !this.getRelIds().isEmpty()) {
            manipulator.mapRelationsToParent(this.getRelIds());
            manipulator.filterRelations(this.getRelIds(), true);
        }
        
        if (this.getToRemoveSubgraphRootIds() != null) {
            for (String subgraphRootId: this.getToRemoveSubgraphRootIds()) {
                for (String classIdRemoved: 
                    manipulator.removeSubgraphs(Arrays.asList(subgraphRootId), true, 
                            this.getToFilterSubgraphRootIds())) {
                    this.getClassesRemoved().put(classIdRemoved, 
                            "Removal of subgraph with root ID " + subgraphRootId + 
                            " - subgraphs excluded from removal: " + 
                            this.getToFilterSubgraphRootIds());
                }
            }
        }
        if (this.getToFilterSubgraphRootIds() != null && 
                !this.getToFilterSubgraphRootIds().isEmpty()) {
            for (String classIdRemoved: 
                manipulator.filterSubgraphs(this.getToFilterSubgraphRootIds())) {
                this.getClassesRemoved().put(classIdRemoved, 
                        "Filtering of subgraph with root IDs: " + this.getToFilterSubgraphRootIds());
            }
        }
        if (this.getSubsetNames() != null && !this.getSubsetNames().isEmpty()) {
            manipulator.removeRelsToSubsets(this.getSubsetNames(), 
                    this.getClassIdsExcludedFromSubsetRemoval());
        }

        this.getOntologyUtils().removeOBOProblematicAxioms();
        
        log.exit();
    }
    
    /**
     * Save information about the simplification process of the original 
     * {@code OWLOntology} {@code ont}. The information is provided through 
     * this method arguments: {@code classesRemoved} a listing of the {@code OWLClass}es 
     * that were removed as a result of the simplification performed by 
     * the {@code simplifyUberon} method, associated to the reason for removal. 
     * This information will be written to the file {@code subgraphFilteredFilePath}.
     * </ul>
     * <p>
     * If more information was to be stored in the future, it should be stored in separate 
     * files (thus, modifying this method signature).
     * 
     * @param ont                       The original {@code OWLOntology}, 
     *                                  as before simplification.
     * @param classesRemovedFilePath    A {@code String} that is the path to the file that will 
     *                                  store information about the {@code OWLClass}es 
     *                                  that were removed as a result of simplification.
     * @param classesRemoved            A {@code Collection} of {@code String}s that are 
     *                                  the OBO-like IDs of {@code OWLClass}es 
     *                                  removed as a result of simplification.
     * @throws IOException  If an error occurred while writing information.
     */
    public void saveSimplificationInfo(OWLOntology ont, String classesRemovedFilePath, 
            Map<String, String> classesRemoved) throws IOException {
        log.entry(ont, classesRemovedFilePath, classesRemoved);
        
        //get a OWLGraphWrapper to obtain information about classes
        OWLGraphWrapper wrapper = new OWLGraphWrapper(ont);
        //we will also need an OntologyUtils to retrieve is_a/part_of outgoing edges
        OntologyUtils utils = new OntologyUtils(wrapper);
        
        //Write IDs removed as a result of graph filtering
        //first, filter potential redundancy 
        Set<String> filteredIds = new HashSet<String>(classesRemoved.keySet());
        //then, order the IDs. 
        List<String> orderedIds = new ArrayList<String>(filteredIds);
        //To get a natural ordering
        Collections.sort(orderedIds, OntologyUtils.ID_COMPARATOR);
        //create the header of the file, and the conditions on the columns
        String[] header = new String[4];
        header[0] = UBERON_ENTITY_ID_COL;
        header[1] = ANAT_ENTITY_NAME_COL;
        header[2] = RELATIONS_COL;
        header[3] = REASON_FOR_REMOVAL;
        CellProcessor[] processors = new CellProcessor[4];
        //ID of the OWLClass (must be unique)
        processors[0] = new UniqueHashCode(new NotNull());
        //label of the OWLClass
        processors[1] = new NotNull();
        //is_a/part_of relations, can be empty if no relations
        processors[2] = new Optional();
        //reason for removal, there is always one
        processors[3] = new NotNull();
        
        try (ICsvMapWriter mapWriter = 
                new CsvMapWriter(new FileWriter(classesRemovedFilePath),
                Utils.TSVCOMMENTED)) {
            mapWriter.writeHeader(header);
            
            for (String uberonId: orderedIds) {
                OWLClass cls = wrapper.getOWLClassByIdentifier(uberonId);
                if (cls != null) {
                    //check that the class is not obsolete
                    if (wrapper.isObsolete(cls) || wrapper.getIsObsolete(cls)) {
                        continue;
                    }
                    //get the is_a/part_of relations, translated to a String
                    String relations = "";
                    for (OWLGraphEdge edge: utils.getIsAPartOfOutgoingEdges(cls)) {
                        if (!relations.equals("")) {
                            relations += " - ";
                        }
                        String relationType = "is_a";
                        if (edge.getSingleQuantifiedProperty().getProperty() != null) {
                            relationType = wrapper.getLabelOrDisplayId(
                                    edge.getSingleQuantifiedProperty().getProperty());
                        }
                        relations += relationType + " " + 
                                wrapper.getIdentifier(edge.getTarget()) + " " + 
                                wrapper.getLabelOrDisplayId(edge.getTarget());
                    }
                    
                    Map<String, Object> row = new HashMap<String, Object>();
                    row.put(header[0], uberonId);
                    row.put(header[1], wrapper.getLabelOrDisplayId(cls));
                    row.put(header[2], relations);
                    row.put(header[3], classesRemoved.get(uberonId));
                    
                    mapWriter.write(row, header, processors);
                } //else {
                    //we disable this assertion error, there are weird case 
                    //were getOWLClassByIdentifier does not find the OWLClass, 
                    //for instance, ID "biological:modeling".
                    //throw log.throwing(new AssertionError("Could not find class " +
                    //      "with ID " + uberonId));
                //}
            }
        }
        
        log.exit();
    }
    
    /**
     * Extract from the Uberon ontology all NCBI taxon IDs that are the targets 
     * of {@code OWLRestriction}s over the object properties "in taxon" (or any 
     * sub-properties), or that are used in ontology annotations 
     * "treat-xrefs-as-reverse-genus-differentia", and to write them in a file.
     * The IDs used are {@code Integer}s that are the NCBI IDs (for instance, 
     * 9606 for human), not the ontology IDs with a prefix ("NCBITaxon:").
     * 
     * @param outputFile    A {@code String} that is the path to the file where 
     *                      to write IDs into.
     * @throws IllegalArgumentException     If {@code uberonFile} did not allow to obtain 
     *                                      any valid taxon ID, or was incorrectly formatted.
     * @throws OWLOntologyCreationException If an error occurred while loading 
     *                                      the ontology.
     * @throws OBOFormatParserException     If an error occurred while loading 
     *                                      the ontology.
     * @throws IOException                  If {@code uberonFile} could not be read, 
     *                                      or the output could not be written in file.
     */
    public void extractTaxonIds(String outputFile) 
            throws IllegalArgumentException, IOException {
        log.entry(outputFile);
        
        Set<Integer> taxonIds = this.extractTaxonIds();
        try(PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outputFile), "utf-8")))) {
            for (int taxonId: taxonIds) {
                writer.println(taxonId);
            }
        }
        
        log.exit();
    }
    
    /**
     * Extract from the Uberon ontology all taxon IDs that are the targets 
     * of {@code OWLRestriction}s over the object properties "in taxon" (or any 
     * sub-properties), or that are used in ontology annotations 
     * "treat-xrefs-as-reverse-genus-differentia". The IDs returned are {@code Integer}s 
     * that are the NCBI IDs (for instance, 9606 for human), not the ontology IDs 
     * with a prefix ("NCBITaxon:").
     * 
     * @return              A {@code Set} of {@code Integer}s that are the NCBI IDs 
     *                      of the taxa used in Uberon as target of restrictions over 
     *                      "in taxon" object properties, or any sub-properties.
     * @throws IllegalArgumentException     If {@code uberonFile} did not allow to obtain 
     *                                      any valid taxon ID or was incorrectly formatted.
     * @throws OWLOntologyCreationException If an error occurred while loading 
     *                                      the ontology.
     * @throws OBOFormatParserException     If an error occurred while loading 
     *                                      the ontology.
     * @throws IOException                  If {@code uberonFile} could not be read.
     */
    public Set<Integer> extractTaxonIds() throws IllegalArgumentException {
        log.entry();

        OWLGraphWrapper wrapper = this.getOntologyUtils().getWrapper();
        Set<String> taxonIds = new HashSet<String>();
        
        for (OWLOntology ont: wrapper.getAllOntologies()) {
            //will get taxon IDs from axioms over object properties "in_taxon", 
            //"evolved_multiple_times_in" (or any sub-properties)
            Set<OWLObjectPropertyExpression> objectProps = this.getTaxonObjectProperties(wrapper);
            //will also get the taxon IDs from annotation axioms over annotation properties
            //"ambiguous_for_taxon", "dubious_for_taxon", "homologous_in","never_in_taxon", 
            //"RO:0002161", "present_in_taxon", "taxon" (or any sub-properties)
            Set<OWLAnnotationProperty> annotProps = this.getTaxonAnnotationProperties(wrapper);
            
            for (OWLClass cls: ont.getClassesInSignature()) {
                //try to get taxa from any object properties that can lead to a taxon.
                //this is will also capture taxon used in equivalence axioms to owl:nothing 
                //(formal way of representing never_in_taxon in owl)
                for (OWLGraphEdge edge: wrapper.getOutgoingEdges(cls)) {
                    
                    if (!edge.getQuantifiedPropertyList().isEmpty() && 
                            edge.getFinalQuantifiedProperty().isSomeValuesFrom() && 
                            objectProps.contains(edge.getFinalQuantifiedProperty().getProperty()) && 
                            edge.getTarget() instanceof OWLClass) {
                        log.trace("Taxon {} captured through object property in axiom {}", 
                                edge.getTarget(), edge.getAxioms());
                        taxonIds.add(wrapper.getIdentifier(edge.getTarget()));
                    }
                }
                //and from any annotation properties that can lead to a taxon
                for (OWLAnnotation annot: cls.getAnnotations(ont)) {
                    if (annotProps.contains(annot.getProperty()) && 
                            annot.getValue() instanceof IRI) {
                        log.trace("Taxon {} captured through annotation property in annotation {}", 
                                annot.getValue(), annot);
                        taxonIds.add(wrapper.getIdentifier(annot.getValue()));
                    }
                }
            }
            
            //now we get the "treat-xrefs-as-reverse-genus-differentia" ontology annotations
            OWLAnnotationProperty genusDifferentia = wrapper.getManager().getOWLDataFactory().
                    getOWLAnnotationProperty(OntologyUtils.GENUS_DIFFERENTIA_IRI);
            for (OWLAnnotation annot: ont.getAnnotations()) {
                if (annot.getProperty().equals(genusDifferentia)) {
                    String value = ((OWLLiteral) annot.getValue()).getLiteral();
                    Matcher m = OntologyUtils.GENUS_DIFFERENTIA_LITERAL_PATTERN.matcher(value);
                    if (m.matches()) {
                        String taxId = m.group(OntologyUtils.GENUS_DIFFERENTIA_TAXON_GROUP);
                        log.trace("Taxon {} captured through treat-xrefs-as-reverse-genus-differentia {}", 
                                taxId, value);
                        taxonIds.add(m.group(OntologyUtils.GENUS_DIFFERENTIA_TAXON_GROUP));
                    } else {
                        throw log.throwing(new IllegalArgumentException("The provided ontology " +
                                "contains genus-differentia annotations that does not match " +
                                "the expected pattern"));
                    }
                }
            }
        }
        
        
        if (taxonIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("The provided ontology " +
                    " did not allow to acquire any taxon ID"));
        }
        return log.exit(OntologyUtils.convertToNcbiIds(taxonIds));
    }
    
    /**
     * Retrieves mappings from XRef IDs to Uberon IDs from the Uberon ontology, and save them 
     * to {@code outputFile}. {@code outputFile} will be a TSV file with a header, 
     * and two columns, that are in order: {@link #XREF_ID_COL} and 
     * {@link #UBERON_ENTITY_ID_COL}. The XRef mappings are obtained using the method 
     * {@link org.bgee.pipeline.OntologyUtils#getXRefMappings()}.
     * 
     * @param outputFile        A {@code String} that is the path to the generated output file.
     * @throws IOException      If an error occurred while writing in the output file, 
     *                          or when reading the ontology file.
     * @throws OBOFormatParserException     If {@code pathToUberonOnt} was in OBO and could not 
     *                                      be parsed.
     * @throws OWLOntologyCreationException If {@code pathToUberonOnt} was in OWL and could not 
     *                                      be parsed.
     * 
     * @see org.bgee.pipeline.OntologyUtils#getXRefMappings()
     */
    public void saveXRefMappingsToFile(String outputFile) 
            throws IOException {
        log.entry(outputFile);
        
        //create the header of the file, and the conditions on the columns
        String[] header = new String[2];
        header[0] = XREF_ID_COL;
        header[1] = UBERON_ENTITY_ID_COL;
        CellProcessor[] processors = new CellProcessor[2];
        processors[0] = new NotNull();
        processors[1] = new NotNull();
        
        try (ICsvMapWriter mapWriter = new CsvMapWriter(new FileWriter(outputFile),
                Utils.TSVCOMMENTED)) {
            
            mapWriter.writeHeader(header);
            
            for (Entry<String, Set<String>> mappings: 
                this.getOntologyUtils().getXRefMappings().entrySet()) {
                for (String uberonId: mappings.getValue()) {
                    Map<String, Object> row = new HashMap<String, Object>();
                    row.put(header[0], mappings.getKey());
                    row.put(header[1], uberonId);
                    mapWriter.write(row, header, processors);
                }
            }
        }
        
        log.exit();
    }

    /**
     * Obtain from the {@code OWLOntology} wrapped into {@code wrapper} all its 
     * {@code OWLObjectProperty}s that can lead to {@code OWLClass}es representing taxa.
     * 
     * @param wrapper   The {@code OWLGraphWrapper} to use to obtain the object properties.
     * @return          A {@code Set} of {@code OWLObjectPropertyExpression}s that 
     *                  can be used to retrieve {@code OWLClass}es representing taxa.
     */
    private Set<OWLObjectPropertyExpression> getTaxonObjectProperties(OWLGraphWrapper wrapper) {
        log.entry(wrapper);
        
        //get object properties "in_taxon" and "evolved_multiple_times_in", 
        //and any sub-properties
        OWLDataFactory factory = wrapper.getManager().getOWLDataFactory();
        Set<OWLObjectPropertyExpression> objectProps = 
                new HashSet<OWLObjectPropertyExpression>();
        
        OWLObjectProperty inTaxon = 
                factory.getOWLObjectProperty(OntologyUtils.IN_TAXON_IRI);
        if (inTaxon != null) {
            objectProps.addAll(wrapper.getSubPropertyReflexiveClosureOf(inTaxon));
        }
        
        OWLObjectProperty evolved = 
                factory.getOWLObjectProperty(OntologyUtils.EVOLVED_MULTIPLE_TIMES_IRI);
        if (evolved != null) {
            objectProps.addAll(wrapper.getSubPropertyReflexiveClosureOf(evolved));
        }
        
        return log.exit(objectProps);
    }
    
    /**
     * Obtain from the {@code OWLOntology} wrapped into {@code wrapper} all its 
     * {@code OWLAnnotationProperty}s that can lead to {@code OWLClass}es representing taxa.
     * 
     * @param wrapper   The {@code OWLGraphWrapper} to use to obtain the annotation properties.
     * @return          A {@code Set} of {@code OWLAnnotationProperty}s that can be used 
     *                  to retrieve {@code OWLClass}es representing taxa.
     */
    private Set<OWLAnnotationProperty> getTaxonAnnotationProperties(OWLGraphWrapper wrapper) {
        log.entry(wrapper);
        
        //get object properties "ambiguous_for_taxon", "dubious_for_taxon", 
        //"homologous_in","never_in_taxon", "RO:0002161", "present_in_taxon", 
        //"taxon", and any sub-properties
        OWLDataFactory factory = wrapper.getManager().getOWLDataFactory();
        Set<OWLAnnotationProperty> annotProps = new HashSet<OWLAnnotationProperty>();
        
        OWLAnnotationProperty prop = 
                factory.getOWLAnnotationProperty(OntologyUtils.AMBIGUOUS_FOR_TAXON_IRI);
        if (prop != null) {
            annotProps.addAll(wrapper.getSubAnnotationPropertyReflexiveClosureOf(prop));
        }
        prop = factory.getOWLAnnotationProperty(OntologyUtils.DUIOUS_FOR_TAXON_IRI);
        if (prop != null) {
            annotProps.addAll(wrapper.getSubAnnotationPropertyReflexiveClosureOf(prop));
        }
        prop = factory.getOWLAnnotationProperty(OntologyUtils.HOMOLOGOUS_IN_IRI);
        if (prop != null) {
            annotProps.addAll(wrapper.getSubAnnotationPropertyReflexiveClosureOf(prop));
        }
        prop = factory.getOWLAnnotationProperty(OntologyUtils.NEVER_IN_TAXON_IRI);
        if (prop != null) {
            annotProps.addAll(wrapper.getSubAnnotationPropertyReflexiveClosureOf(prop));
        }
        prop = factory.getOWLAnnotationProperty(OntologyUtils.NEVER_IN_TAXON_BIS_IRI);
        if (prop != null) {
            annotProps.addAll(wrapper.getSubAnnotationPropertyReflexiveClosureOf(prop));
        }
        prop = factory.getOWLAnnotationProperty(OntologyUtils.PRESENT_IN_TAXON_IRI);
        if (prop != null) {
            annotProps.addAll(wrapper.getSubAnnotationPropertyReflexiveClosureOf(prop));
        }
        prop = factory.getOWLAnnotationProperty(OntologyUtils.TAXON_IRI);
        if (prop != null) {
            annotProps.addAll(wrapper.getSubAnnotationPropertyReflexiveClosureOf(prop));
        }
        
        return log.exit(annotProps);
    }

    /**
     * @return  A {@code Collection} of {@code String}s that are the names of the targeted subsets, 
     *          for which member {@code OWLClass}es should have their is_a/part_of 
     *          incoming edges removed (only if the source of the incoming edge 
     *          will not be left orphan of other is_a/part_of relations to {@code OWLClass}es 
     *          not in {@code subsets}. First argument when calling 
     *          {@code OWLGraphManipulator#removeRelsToSubsets(Collection, Collection)}.
     * @see #getClassIdsExcludedFromSubsetRemoval()
     * @see #setSubsetNames(Collection)
     */
    public Collection<String> getSubsetNames() {
        return subsetNames;
    }
    /**
     * Sets the parameter returned by {@link #getSubsetNames()}.
     * 
     * @param subsetNames   See {@link #getSubsetNames()}.
     * @see #setClassIdsExcludedFromSubsetRemoval(Collection)
     * @see #getSubsetNames()
     */
    public void setSubsetNames(Collection<String> subsetNames) {
        this.subsetNames = subsetNames;
    }

    /**
     * @return  A {@code Collection} of {@code String}s that are the OBO-like IDs 
     *          of {@code OWLClass}es whose incoming edges should not be removed even if member 
     *          of a subset listed in {@link #subsetNames}. Second argument when calling 
     *          {@code OWLGraphManipulator#removeRelsToSubsets(Collection, Collection)}.
     * @see #getSubsetNames()
     * @see #setClassIdsExcludedFromSubsetRemoval(Collection)
     */
    public Collection<String> getClassIdsExcludedFromSubsetRemoval() {
        return classIdsExcludedFromSubsetRemoval;
    }
    /**
     * Sets the parameter returned by {@link #getClassIdsExcludedFromSubsetRemoval()}.
     * 
     * @param classIdsExcludedFromSubsetRemoval See {@link #getClassIdsExcludedFromSubsetRemoval()}.
     * @see #setSubsetNames(Collection)
     * @see #getClassIdsExcludedFromSubsetRemoval()
     */
    public void setClassIdsExcludedFromSubsetRemoval(
            Collection<String> classIdsExcludedFromSubsetRemoval) {
        this.classIdsExcludedFromSubsetRemoval = classIdsExcludedFromSubsetRemoval;
    }
    
    
    
// NOTE May 13 2014: this method seems now completely useless, to remove if it is confirmed.
//    /**
//     * Retrieve all {@code OWLGraphEdge}s related to the relation {@code relationToUse}, 
//     * or any of its sub-property, from the ontology stored in {@code uberonFile}, 
//     * and write them into {@code outputFile}.
//     * 
//     * @param uberonFile    A {@code String} that is the path to the Uberon ontology.
//     * @param outputFile    A {@code String} that is the output file to be written.
//     * @param relationToUse A {@code String} that is the OBO-like ID of the IRI of 
//     *                      the relation to use. 
//     * @throws IOException                      If an error occurred while reading the file 
//     *                                          {@code uberonFile}.
//     * @throws OBOFormatParserException         If the ontology was provided in OBO format 
//     *                                          and a parser error occurred. 
//     * @throws OWLOntologyCreationException     If an error occurred while loading 
//     *                                          the ontology.
//     */
//    public void extractRelatedEdgesToOutputFile(
//            String uberonFile, String outputFile, String relationToUseId)  throws OWLOntologyCreationException, 
//            OBOFormatParserException, IOException {
//        log.entry(uberonFile, outputFile, relationToUseId);
//        
//        OWLOntology ont = OntologyUtils.loadOntology(uberonFile);
//        OWLGraphWrapper wrapper = new OWLGraphWrapper(ont);
//        
//        //try to get the relation by its IRI
//        OWLObjectProperty relProp = wrapper.getOWLObjectProperty(relationToUseId);
//        //try to get it from its OBO-like ID
//        if (relProp == null) {
//            relProp = wrapper.getOWLObjectPropertyByIdentifier(relationToUseId);
//        }
//        if (relProp == null) {
//            throw log.throwing(new IllegalArgumentException("The provided ontology did not " +
//            		"contain the relation " + relationToUseId));
//        }
//        Set<OWLObjectPropertyExpression> props = wrapper.getSubPropertyReflexiveClosureOf(relProp);
//        Set<OWLGraphEdge> edges = new HashSet<OWLGraphEdge>();
//        
//        for (OWLClass iterateClass: wrapper.getAllOWLClasses()) {
//            for (OWLGraphEdge edge: wrapper.getOutgoingEdges(iterateClass)) {
//                if (edge.getSingleQuantifiedProperty() != null && 
//                        props.contains(edge.getSingleQuantifiedProperty().getProperty())) {
//                    edges.add(edge);
//                }
//            }
//        }
//        
//        //write edges to file
//        String[] header = new String[] {"Uberon source ID", "Uberon source name", 
//                "Relation ID", "Relation name", "Uberon target ID", "Uberon target name"};
//        CellProcessor[] processors = new CellProcessor[] {new NotNull(), new NotNull(), 
//                new NotNull(), new NotNull(), new NotNull(), new NotNull()};
//        try (ICsvMapWriter mapWriter = new CsvMapWriter(new FileWriter(outputFile),
//                Utils.TSVCOMMENTED)) {
//            
//            mapWriter.writeHeader(header);
//            for (OWLGraphEdge edge: edges) {
//                Map<String, String> line = new HashMap<String, String>();
//                line.put("Uberon source ID", wrapper.getIdentifier(edge.getSource()));
//                line.put("Uberon source name", wrapper.getLabel(edge.getSource()));
//                line.put("Relation ID", wrapper.getIdentifier(
//                        edge.getSingleQuantifiedProperty().getProperty()));
//                line.put("Relation name", wrapper.getLabel(
//                        edge.getSingleQuantifiedProperty().getProperty()));
//                line.put("Uberon target ID", wrapper.getIdentifier(edge.getTarget()));
//                line.put("Uberon target name", wrapper.getLabel(edge.getTarget()));
//                mapWriter.write(line, header, processors);
//            }
//        }
//        
//        log.exit();
//    }
}
