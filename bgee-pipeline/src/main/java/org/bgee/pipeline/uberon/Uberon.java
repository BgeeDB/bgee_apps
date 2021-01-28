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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.anatdev.TaxonConstraintDAO.TaxonConstraintTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationStatus;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationType;
import org.bgee.pipeline.CommandRunner;
import org.bgee.pipeline.Utils;
import org.bgee.pipeline.ontologycommon.OntologyUtils;
import org.bgee.pipeline.ontologycommon.OntologyUtils.PipelineRelationTO;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.supercsv.cellprocessor.FmtBool;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.Unique;
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
 * @version Bgee 14.2 Oct. 2020
 * @since Bgee 13
 */
public class Uberon extends UberonCommon {
    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = 
            LogManager.getLogger(Uberon.class.getName());

    /**
     * An unmodifiable {@code Set} of {@code String}s that are the names 
     * of non-informative subsets in Uberon.
     * @see #INFORMATIVE_SUBSETS
     */
    public final static Set<String> NON_INFORMATIVE_SUBSETS = Collections.unmodifiableSet(
            new HashSet<String>(Arrays.asList("non_informative", "upper_level")));
    /**
     * An unmodifiable {@code Set} of {@code String}s that are the names 
     * of informative subsets in Uberon (terms belonging to these subsets should never 
     * be discarded, even if also part of a non-informative subset).
     * @see #NON_INFORMATIVE_SUBSETS
     */
    public final static Set<String> INFORMATIVE_SUBSETS = Collections.unmodifiableSet(
            new HashSet<String>(Arrays.asList("efo_slim", "uberon_slim", "organ_slim", 
                    "anatomical_site_slim", "cell_slim", "vertebrate_core")));

    /**
     * A {@code String} that is the OBO-like ID of the term "hermaphroditic organism".
     */
    public final static String HERMAPHRODITE_ORGANISM_ID = "UBERON:0007197";
    /**
     * A {@code String} that is the OBO-like ID of the term "female organism".
     */
    public final static String FEMALE_ORGANISM_ID = "UBERON:0003100";
    /**
     * A {@code String} that is the OBO-like ID of the term "male organism".
     */
    public final static String MALE_ORGANISM_ID = "UBERON:0003101";

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
     * <li>If the first element in {@code args} is "extractSexInfo", the action 
     * will be to extract from the Uberon ontology sex-related information about anatomical terms, 
     * and to write them in a file (see {@link #extractSexInfoToFile(String)}).
     * Following elements in {@code args} must then be: 
     *   <ol>
     *   <li>path to the Uberon ontology.
     *   <li>path to the output file where to write sex-related info.
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
     *   <li>a map specifying specific relations to remove between pairs of {@code OWLClass}es. 
     *   In a key-value pair, the key should be the OBO-like ID of the source of relations 
     *   to remove, the value being the target of the relations to remove. Key-value pairs 
     *   must be separated by {@link CommandRunner#LIST_SEPARATOR}, keys must be  
     *   separated from their associated value by {@link CommandRunner#KEY_VALUE_SEPARATOR}. 
     *   A key can be associated to several values. See {@link #setRelsBetweenToRemove(Map)}.
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
     *   </ol>
     *   Example of command line usage for this task: {@code java -Xmx2g -jar myJar 
     *   Uberon simplifyUberon composite-metazoan.owl custom_composite simplification_composite.tsv 
     *   UBERON:0001062,UBERON:0000465,UBERON:0000061,UBERON:0010000,UBERON:0008979 
     *   BFO:0000050,RO:0002202,RO:0002494 
     *   UBERON:0000922/UBERON:0002050,UBERON:0004716/UBERON:0000922,UBERON:0000467/UBERON:0000468,UBERON:0000475/UBERON:0000468,UBERON:0000479/UBERON:0000468,UBERON:0000480/UBERON:0000468,UBERON:0007688/UBERON:0000468,UBERON:0010707/UBERON:0000468,UBERON:0012641/UBERON:0000468,UBERON:0002199/UBERON:0000468,UBERON:0002416/UBERON:0000468,UBERON:0007376/UBERON:0000468,UBERON:0000463/UBERON:0000468,UBERON:0001048/UBERON:0000468,UBERON:0007567/UBERON:0000468,UBERON:0015119/UBERON:0000468 
     *   NBO:0000313,GO:0008150,ENVO:01000254,BFO:0000040,GO:0003674,PATO:0000001,CHEBI:24431,UBERON:0004458,UBERON:0000466,SO:0000704 
     *   UBERON:0013701,UBERON:0000026,UBERON:0000480,UBERON:0000479,UBERON:0000468,GO:0005575 
     *   grouping_class,non_informative,ubprop:upper_level,upper_level 
     *   UBERON:0013701,UBERON:0000026,UBERON:0000480,UBERON:0000479,UBERON:0011676,GO:0005575}
     *   
     * <li>If the first element in {@code args} is "extractXRefMappings", the action will be 
     * to retrieve mappings from XRef IDs to Uberon IDs from Uberon, and to save them 
     * to a TSV file, see {@link #saveXRefMappingsToFile(String)} for details.
     * Following elements in {@code args} must then be: 
     *   <ol>
     *   <li>path to the Uberon ontology.
     *   <li>path to the output file where to save the mappings.
     *   </ol>
     *
     * <li>If the first element in {@code args} is "explainRelation", the action will be 
     * to retrieve edges outgoing from an OWLClass from Uberon, and to display information
     * about them in the console.
     * Following elements in {@code args} must then be: 
     *   <ol>
     *   <li>path to the Uberon ontology.
     *   <li>OBO-like ID or IRI of the {@code OWLClass} to retrieve outgoing edges for.
     *   <li>OBO-like ID or IRI of the {@code OWLClass} that must be the target of the outgoing edges.
     *       can be an empty arg (see {@link org.bgee.pipeline.CommandRunner#EMPTY_ARG EMPTY_ARG}).
     *   <li>A list of species IDs the source and target of the outgoing edges must be valid in
     *       (see {@link #explainRelationForOWLClass(String, String, Collection)} for details).
     *       Cannot be empty, species IDs must be provided.
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
            ub.setRelsBetweenToRemove(CommandRunner.parseMapArgument(args[6]).entrySet().stream()
                    .collect(Collectors.toMap(Entry::getKey, e -> new HashSet<String>(e.getValue()))));
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
//        } else if (args[0].equalsIgnoreCase("test")) {
//            Uberon.test();
        } else if (args[0].equalsIgnoreCase("extractSexInfo")) {
            if (args.length != 3) {
                throw log.throwing(new IllegalArgumentException(
                        "Incorrect number of arguments provided, expected " + 
                        "3 arguments, " + args.length + " provided."));
            }
            new Uberon(args[1]).extractSexInfoToFile(args[2]);
        } else if (args[0].equalsIgnoreCase("explainRelation")) {
            if (args.length != 5) {
                throw log.throwing(new IllegalArgumentException(
                        "Incorrect number of arguments provided, expected " + 
                        "5 arguments, " + args.length + " provided."));
            }
            new Uberon(args[1]).explainRelationFromOWLClassIds(args[2],
                    CommandRunner.parseArgument(args[3]),
                    CommandRunner.parseListArgumentAsInt(args[4]));
        } else {
            throw log.throwing(new UnsupportedOperationException("The following action " +
                    "is not recognized: " + args[0]));
        }
        
        log.exit();
    }

//    public static void test() throws FileNotFoundException, IOException, 
//    OBOFormatParserException, OWLOntologyCreationException {
//        log.entry();
//        
//        String sourceOntName = "composite-metazoan.owl";
//        
//        Set<String> nonHumanAnatEntityIds = AnnotationCommon.extractAnatEntityIdsFromFile(
//                "anatEntityIds_non_human.txt", true);
//        Set<String> noHumanExpressionAnatEntityIds = AnnotationCommon.extractAnatEntityIdsFromFile(
//                "anatEntityIds_no_human_global_expression.txt", true);
//        
//        Set<String> relIds = new HashSet<String>(
//                Arrays.asList("BFO:0000050", "RO:0002202", "RO:0002494"));
//        Map<String, Set<String>> relsBetweenToRemove = CommandRunner.parseMapArgument("UBERON:0000922/UBERON:0002050,UBERON:0004716/UBERON:0000922,UBERON:0001640/UBERON:0001194");
//        List<String> toRemoveSubgraphRootIds = CommandRunner.parseListArgument("NBO:0000313,GO:0008150,ENVO:01000254,BFO:0000040,GO:0003674,PATO:0000001,CHEBI:24431,SO:0000704");
//        List<String> toFilterSubgraphRootIds = CommandRunner.parseListArgument("NCBITaxon:1,UBERON:0001062,GO:0005575");
//        
//        
//        Uberon ub = new Uberon(sourceOntName);
//        ub.convertTaxonECAs();
//        ub.getOntologyUtils().removeOBOProblematicAxioms();
//        
//        ub.getOntologyUtils().saveAsOBO(sourceOntName + "_base.obo", false);
//        
////        Uberon ub2 = new Uberon(ub.getOntologyUtils());
////        OWLGraphManipulator manipulator = ub2.getOntologyUtils().getManipulator();
////        
////        manipulator.removeUnrelatedRelations(relIds);
////        for (Entry<String, Set<String>> relsToRemove: relsBetweenToRemove.entrySet()) {
////            for (String targetId: relsToRemove.getValue()) {
////                manipulator.removeDirectEdgesBetween(relsToRemove.getKey(), targetId);
////            }
////        }
////        manipulator.mapRelationsToParent(relIds);
////        manipulator.filterRelations(relIds, true);
////        for (String subgraphRootId: toRemoveSubgraphRootIds) {
////            manipulator.removeSubgraphs(Arrays.asList(subgraphRootId), true, 
////                    toFilterSubgraphRootIds);
////        }
////        manipulator.filterSubgraphs(toFilterSubgraphRootIds);
////    
////        ub2.getOntologyUtils().removeOBOProblematicAxioms();
////        ub2.getOntologyUtils().saveAsOBO(sourceOntName + "_base_simple.obo", false);
////        
////        
////        ub2 = new Uberon(ub.getOntologyUtils());
////        manipulator = ub2.getOntologyUtils().getManipulator();
////        
////        manipulator.removeUnrelatedRelations(relIds);
////        for (Entry<String, Set<String>> relsToRemove: relsBetweenToRemove.entrySet()) {
////            for (String targetId: relsToRemove.getValue()) {
////                manipulator.removeDirectEdgesBetween(relsToRemove.getKey(), targetId);
////            }
////        }
////        manipulator.reduceRelations();
////        manipulator.reducePartOfIsARelations();
////        manipulator.mapRelationsToParent(relIds);
////        manipulator.filterRelations(relIds, true);
////        for (String subgraphRootId: toRemoveSubgraphRootIds) {
////            manipulator.removeSubgraphs(Arrays.asList(subgraphRootId), true, 
////                    toFilterSubgraphRootIds);
////        }
////        manipulator.filterSubgraphs(toFilterSubgraphRootIds);
////    
////        ub2.getOntologyUtils().removeOBOProblematicAxioms();
////        ub2.getOntologyUtils().saveAsOBO(sourceOntName + "_base_simple_reduced.obo", false);
////        
////        Uberon ub2 = new Uberon(ub.getOntologyUtils());
////        OWLGraphManipulator manipulator = ub2.getOntologyUtils().getManipulator();
////        
////        manipulator.removeUnrelatedRelations(relIds);
////        for (Entry<String, Set<String>> relsToRemove: relsBetweenToRemove.entrySet()) {
////            for (String targetId: relsToRemove.getValue()) {
////                manipulator.removeDirectEdgesBetween(relsToRemove.getKey(), targetId);
////            }
////        }
////        manipulator.mapRelationsToParent(relIds);
////        manipulator.filterRelations(relIds, true);
////        for (String subgraphRootId: toRemoveSubgraphRootIds) {
////            manipulator.removeSubgraphs(Arrays.asList(subgraphRootId), true, 
////                    toFilterSubgraphRootIds);
////        }
////        manipulator.filterSubgraphs(toFilterSubgraphRootIds);
////        for (String classIdToRemove: nonHumanAnatEntityIds) {
////            if (manipulator.getOwlGraphWrapper().getOWLClassByIdentifierNoAltIds(classIdToRemove) != null) {
////                manipulator.removeClassAndPropagateEdges(classIdToRemove);
////            }
////        }
////    
////        ub2.getOntologyUtils().removeOBOProblematicAxioms();
////        ub2.getOntologyUtils().saveAsOBO(sourceOntName + "_base_simple_human_only.obo", false);
////
////        
////        ub2 = new Uberon(ub.getOntologyUtils());
////        manipulator = ub2.getOntologyUtils().getManipulator();
////        
////        manipulator.removeUnrelatedRelations(relIds);
////        for (Entry<String, Set<String>> relsToRemove: relsBetweenToRemove.entrySet()) {
////            for (String targetId: relsToRemove.getValue()) {
////                manipulator.removeDirectEdgesBetween(relsToRemove.getKey(), targetId);
////            }
////        }
////        manipulator.mapRelationsToParent(relIds);
////        manipulator.filterRelations(relIds, true);
////        for (String subgraphRootId: toRemoveSubgraphRootIds) {
////            manipulator.removeSubgraphs(Arrays.asList(subgraphRootId), true, 
////                    toFilterSubgraphRootIds);
////        }
////        manipulator.filterSubgraphs(toFilterSubgraphRootIds);
////        for (String classIdToRemove: noHumanExpressionAnatEntityIds) {
////            if (manipulator.getOwlGraphWrapper().getOWLClassByIdentifierNoAltIds(classIdToRemove) != null) {
////                manipulator.removeClassAndPropagateEdges(classIdToRemove);
////            }
////        }
////    
////        ub2.getOntologyUtils().removeOBOProblematicAxioms();
////        ub2.getOntologyUtils().saveAsOBO(sourceOntName + "_base_simple_human_expression_only.obo", false);
//        
//        
//        Uberon ub2 = new Uberon(ub.getOntologyUtils());
//        OWLGraphManipulator manipulator = ub2.getOntologyUtils().getManipulator();
//        
//        manipulator.removeUnrelatedRelations(relIds);
//        for (String classIdToRemove: nonHumanAnatEntityIds) {
//            if (manipulator.getOwlGraphWrapper().getOWLClassByIdentifierNoAltIds(classIdToRemove) != null) {
//                manipulator.removeClassAndPropagateEdges(classIdToRemove);
//            }
//        }
//        for (Entry<String, Set<String>> relsToRemove: relsBetweenToRemove.entrySet()) {
//            for (String targetId: relsToRemove.getValue()) {
//                if (manipulator.getOwlGraphWrapper().getOWLClassByIdentifierNoAltIds(relsToRemove.getKey()) != null && 
//                        manipulator.getOwlGraphWrapper().getOWLClassByIdentifierNoAltIds(targetId) != null) {
//                    manipulator.removeDirectEdgesBetween(relsToRemove.getKey(), targetId);
//                }
//            }
//        }
//        manipulator.reduceRelations();
//        manipulator.reducePartOfIsARelations();
//        manipulator.mapRelationsToParent(relIds);
//        manipulator.filterRelations(relIds, true);
//        for (String subgraphRootId: toRemoveSubgraphRootIds) {
//            manipulator.removeSubgraphs(Arrays.asList(subgraphRootId), true, 
//                    toFilterSubgraphRootIds);
//        }
//        manipulator.filterSubgraphs(toFilterSubgraphRootIds);
//    
//        ub2.getOntologyUtils().removeOBOProblematicAxioms();
//        ub2.getOntologyUtils().saveAsOBO(sourceOntName + "_base_simple_reduced_human_only.obo", false);
//        
//        
//        ub2 = new Uberon(ub.getOntologyUtils());
//        manipulator = ub2.getOntologyUtils().getManipulator();
//        
//        manipulator.removeUnrelatedRelations(relIds);
//        for (String classIdToRemove: noHumanExpressionAnatEntityIds) {
//            if (manipulator.getOwlGraphWrapper().getOWLClassByIdentifierNoAltIds(classIdToRemove) != null) {
//                manipulator.removeClassAndPropagateEdges(classIdToRemove);
//            }
//        }
//        for (Entry<String, Set<String>> relsToRemove: relsBetweenToRemove.entrySet()) {
//            for (String targetId: relsToRemove.getValue()) {
//                if (manipulator.getOwlGraphWrapper().getOWLClassByIdentifierNoAltIds(relsToRemove.getKey()) != null && 
//                        manipulator.getOwlGraphWrapper().getOWLClassByIdentifierNoAltIds(targetId) != null) {
//                    manipulator.removeDirectEdgesBetween(relsToRemove.getKey(), targetId);
//                }
//            }
//        }
//        manipulator.reduceRelations();
//        manipulator.reducePartOfIsARelations();
//        manipulator.mapRelationsToParent(relIds);
//        manipulator.filterRelations(relIds, true);
//        for (String subgraphRootId: toRemoveSubgraphRootIds) {
//            manipulator.removeSubgraphs(Arrays.asList(subgraphRootId), true, 
//                    toFilterSubgraphRootIds);
//        }
//        manipulator.filterSubgraphs(toFilterSubgraphRootIds);
//    
//        ub2.getOntologyUtils().removeOBOProblematicAxioms();
//        ub2.getOntologyUtils().saveAsOBO(sourceOntName + "_base_simple_reduced_human_expression_only.obo", false);
//        
//        log.exit();
//    }

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
     * wrapping the Uberon ontology that will be used, and the taxon constraints 
     * that will be used to identify to which species classes belong.
     * 
     * @param ontUtils  the {@code OntologyUtils} that will be used. 
     * @throws OWLOntologyCreationException If an error occurred while merging 
     *                                      the import closure of the ontology.
     */
    public Uberon(OntologyUtils ontUtils) throws OWLOntologyCreationException {
        super(ontUtils);
    }
    /**
     * Constructor providing the path to the Uberon ontology to used to perform operations, 
     * the path the a file containing taxon constraints, as parsable by 
     * {@link TaxonConstraints#extractTaxonConstraints(String)}. 
     * This argument can be {@code null}, but as usage of the ontology 
     * requires precise taxon constraints, this is unlikely. 
     * 
     * @param ontUtils                  An {@code OntologyUtils} containing the Uberon ontology. 
     * @param pathToTaxonConstraints    A {@code String} that is the path to the taxon constraints.
     * @throws OWLOntologyCreationException If an error occurred while loading the ontology.
     * @throws OBOFormatParserException     If the ontology is malformed.
     * @throws IOException                  If the file could not be read. 
     */
    public Uberon(OntologyUtils ontUtils, String pathToTaxonConstraints) 
            throws OWLOntologyCreationException, OBOFormatParserException, IOException {
        this(ontUtils, TaxonConstraints.extractTaxonConstraints(pathToTaxonConstraints));
    }
    /**
     * Constructor providing the {@code OntologyUtils} used to perform operations, 
     * wrapping the Uberon ontology that will be used. 
     * 
     * @param ontUtils  the {@code OntologyUtils} that will be used. 
     * @param taxonConstraints  A {@code Map} where keys are IDs of the Uberon 
     *                          {@code OWLClass}es, and values are {@code Set}s 
     *                          of {@code Integer}s containing the IDs of taxa 
     *                          in which the {@code OWLClass} exists.
     * @throws OWLOntologyCreationException If an error occurred while merging 
     *                                      the import closure of the ontology.
     */
    public Uberon(OntologyUtils ontUtils, Map<String, Set<Integer>> taxonConstraints) 
            throws OWLOntologyCreationException {
        super(ontUtils);
        this.setTaxonConstraints(taxonConstraints);
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
     * This method calls {@link #simplifyUberon()}, by loading the {@code OWLOntology} 
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
     * @see #simplifyUberon()
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
     * {@link org.bgee.pipeline.ontologycommon.OntologyUtils#removeOBOProblematicAxioms()}.
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
     * <li>{@link org.bgee.pipeline.ontologycommon.OntologyUtils#removeOBOProblematicAxioms()}
     * </ul>
     *  
     * @throws UnknownOWLOntologyException      If an error occurred while wrapping 
     *                                          the {@code uberonOnt} into an 
     *                                          {@code OWLGraphManipulator}.
     * @throws IllegalStateException            If the modifications are incorrect, 
     *                                          because of the original state of the ontology.
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
        
        //remove completely unrelated relations to simplify relation reuction
        if (this.getRelIds() != null && !this.getRelIds().isEmpty()) {
            manipulator.removeUnrelatedRelations(this.getRelIds());
        }

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
        
        //in order to identify problems related to cycles: after relation reduction, 
        //terms in cycles can be "disconnected" from subgraphs to keep, without being seen 
        //as root of the ontology, because of the cycle, thus being removed 
        //when filtering graph. We try to detect such terms
        Set<OWLClass> originalClassesToKeep = this.getOntologyUtils().getSubgraphMembers(
                this.getToFilterSubgraphRootIds());
        
        manipulator.reduceRelations();
        manipulator.reducePartOfIsARelations();
        
        //Search for "disconnected" terms.
        Set<OWLClass> afterReductionClassesToKeep = this.getOntologyUtils().getSubgraphMembers(
                this.getToFilterSubgraphRootIds());
        originalClassesToKeep.removeAll(afterReductionClassesToKeep);
        if (!originalClassesToKeep.isEmpty()) {
            this.getOntologyUtils().retainParentClasses(originalClassesToKeep, null);
            throw new IllegalStateException(
                    "Modifications of the ontology resulted in terms being disconnected " +
                    "from subgraphs to be kept. Such terms will be erroneously removed " +
                    "by subgraph filtering. This is often due to cycles between them. " +
                    "Erroneously disconnected terms: " + originalClassesToKeep);
        }
        
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
        
        if (log.isWarnEnabled()) {
            String msg = "";
            for (OWLClass root: manipulator.getOwlGraphWrapper().getOntologyRoots(
                    this.getOntologyUtils().getGenericPartOfProps())) {
                msg += System.lineSeparator() + 
                        manipulator.getOwlGraphWrapper().getIdentifier(root) + 
                        " \"" + manipulator.getOwlGraphWrapper().getLabel(root) + "\"";
            }
            log.warn("For your information, roots of the generated ontology " +
            		"by is_a and part_of relations: {} {}", msg, 
            		System.lineSeparator() + "(Logged with a WARN level because this information " +
            				"needs to be examined)");
        }
        
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
                OWLClass cls = wrapper.getOWLClassByIdentifier(uberonId, true);
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
                if (wrapper.isOboAltId(cls)) {
                    continue;
                }
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
                taxonIds.addAll(EntitySearcher.getAnnotations(cls, ont).stream()
                        .map(annot -> {
                            if (annotProps.contains(annot.getProperty()) &&
                                    annot.getValue() instanceof IRI) {
                                log.trace("Taxon {} captured through annotation property in annotation {}",
                                        annot.getValue(), annot);
                                return wrapper.getIdentifier(annot.getValue());
                            }
                            return null;
                        })
                        .filter(id -> id != null)
                        .collect(Collectors.toSet()));
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
     * {@link org.bgee.pipeline.ontologycommon.OntologyUtils#getXRefMappings()}.
     * 
     * @param outputFile        A {@code String} that is the path to the generated output file.
     * @throws IOException      If an error occurred while writing in the output file, 
     *                          or when reading the ontology file.
     * 
     * @see org.bgee.pipeline.ontologycommon.OntologyUtils#getXRefMappings()
     */
    public void saveXRefMappingsToFile(String outputFile) throws IOException {
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
     * Extract sex information about anatomical terms to a TSV file. 
     * Retrieves all descendants of the terms with OBO-like IDs {@link #HERMAPHRODITE_ORGANISM_ID}, 
     * {@link #FEMALE_ORGANISM_ID}, {@link #MALE_ORGANISM_ID}, then write a TSV file 
     * containing all classes, and for each class, 3 columns indicating whether the class 
     * is a descendant of the hermaphrodite, male, female terms.
     * 
     * @param outputFile    A {@code String} that is the path the the output TSV file.
     * @throws IOException  If an error occurred while writing in the output file.
     */
    public void extractSexInfoToFile(String outputFile) throws IOException {
        log.entry(outputFile);
        
        final OWLGraphWrapper wrapper = this.getOntologyUtils().getWrapper();
        
        //Retrieve root classes of sex-related terms
        OWLClass hermaphroditeCls = wrapper.getOWLClassByIdentifierNoAltIds(HERMAPHRODITE_ORGANISM_ID);
        OWLClass femaleCls = wrapper.getOWLClassByIdentifierNoAltIds(FEMALE_ORGANISM_ID);
        OWLClass maleCls = wrapper.getOWLClassByIdentifierNoAltIds(MALE_ORGANISM_ID);
        if (hermaphroditeCls == null || femaleCls == null || maleCls == null) {
            throw log.throwing(new IllegalStateException("Could not find some sex-related terms"));
        }
        
        //Retrieve descendants of sex-related terms
        Set<OWLClass> hermaphroditeClasses = wrapper.getOWLClassDescendantsWithGCI(hermaphroditeCls, 
                this.getOntologyUtils().getGenericPartOfProps());
        Set<OWLClass> femaleClasses = wrapper.getOWLClassDescendantsWithGCI(femaleCls, 
                this.getOntologyUtils().getGenericPartOfProps());
        Set<OWLClass> maleClasses = wrapper.getOWLClassDescendantsWithGCI(maleCls, 
                this.getOntologyUtils().getGenericPartOfProps());
        if (hermaphroditeClasses.isEmpty() || femaleClasses.isEmpty() || maleClasses.isEmpty()) {
            throw log.throwing(new IllegalStateException("No descendants for some sex-related terms"));
        }
        hermaphroditeClasses.add(hermaphroditeCls);
        femaleClasses.add(femaleCls);
        maleClasses.add(maleCls);
        
        //Retrieve all OWLClasses and order them by ID for consistent diffs between releases
        List<OWLClass> allClasses = wrapper.getAllRealOWLClasses().stream()
                .sorted(Comparator.comparing(c -> wrapper.getIdentifier(c), OntologyUtils.ID_COMPARATOR))
                .collect(Collectors.toList());
        
        //generate output file containing all classes
        //create the header of the file, and the conditions on the columns
        String[] header = new String[5];
        header[0] = UBERON_ENTITY_ID_COL;
        header[1] = ANAT_ENTITY_NAME_COL;
        header[2] = "female";
        header[3] = "male";
        header[4] = "hermaphrodite";
        CellProcessor[] processors = new CellProcessor[5];
        processors[0] = new NotNull(new Unique());
        processors[1] = null;
        processors[2] = new NotNull(new FmtBool("T", "F"));
        processors[3] = new NotNull(new FmtBool("T", "F"));
        processors[4] = new NotNull(new FmtBool("T", "F"));
        
        //write output file
        try (ICsvMapWriter mapWriter = new CsvMapWriter(new FileWriter(outputFile),
                Utils.TSVCOMMENTED)) {
            
            mapWriter.writeHeader(header);
            
            for (OWLClass cls: allClasses) {
                Map<String, Object> row = new HashMap<String, Object>();
                row.put(header[0], wrapper.getIdentifier(cls));
                row.put(header[1], wrapper.getLabel(cls));
                row.put(header[2], femaleClasses.contains(cls));
                row.put(header[3], maleClasses.contains(cls));
                row.put(header[4], hermaphroditeClasses.contains(cls));
                
                mapWriter.write(row, header, processors);
            }
            
            mapWriter.flush();
        }
        
        log.exit();
    }
    
    /**
     * Determines whether {@code object} is a member of a non-informative subset.
     * 
     * @param object    the {@code OWLObject} which we want subset information about.
     * @return          {@code true} if {@code object} is member of a non-informative subset.
     * @see #NON_INFORMATIVE_SUBSETS
     * @see #INFORMATIVE_SUBSETS
     */
    public boolean isNonInformativeSubsetMember(OWLObject object) {
        log.entry(object);
        
        Collection<String> subsets = this.getOntologyUtils().getWrapper().getSubsets(object);
        return log.exit(!Collections.disjoint(NON_INFORMATIVE_SUBSETS, subsets) && 
                Collections.disjoint(INFORMATIVE_SUBSETS, subsets));
    }

    /**
     * Method to explain relations outgoing from an {@code OWLClass}, potentially incoming to
     * another specified {@code OWLClass}. Information will be basically displayed in the console.
     *
     * @param sourceClassId A {@code String} representing the OBO-like ID or IRI of an {@code OWLClass}
     *                      for which outgoing edges must be detailed.
     * @param targetClassId A {@code String} representing the OBO-like ID or IRI of an {@code OWLClass}
     *                      for which edges outgoing from {@code sourceClassId} must target.
     *                      Can be {@code null}.
     * @param speciesIds    A {@code Collection} of {@code Integer}s that are the NCBI IDs 
     *                      of the species to consider.
     *                      Only outgoing edges with the source and target existing 
     *                      in at least one of these species will be considered.
     *                      The validity of the relation itself in these species will not be checked
     *                      (GCI relation). Cannot be {@code null} nor empty.
     */
    public void explainRelationFromOWLClassIds(String sourceClassId, String targetClassId,
            Collection<Integer> speciesIds) {
        log.entry(sourceClassId, targetClassId, speciesIds);

        Map<Boolean, Set<OWLGraphEdge>> directIndirectValidOutgoingEdges =
                this.getValidOutgoingEdgesFromOWLClassIds(sourceClassId, targetClassId, speciesIds);

        if (directIndirectValidOutgoingEdges == null) {
            System.out.println("Could not find OWLClass corresponding to " + sourceClassId);
            log.exit(); return;
        }
        OntologyUtils utils = this.getOntologyUtils();
        OWLGraphWrapper wrapper = utils.getWrapper();
        //Just to easily retrieve the source class from the edges
        OWLClass sourceClass = directIndirectValidOutgoingEdges.values().stream()
                .flatMap(set -> set.stream())
                .findAny()
                .map(outgoingEdge -> this.getOWLClass(wrapper.getIdentifier(outgoingEdge.getSource())))
                .get();
        directIndirectValidOutgoingEdges.entrySet().stream().forEach(e -> {
            e.getValue().stream().forEach(outgoingEdge -> {
                OWLClass target = this.getOWLClass(wrapper.getIdentifier(outgoingEdge.getTarget()));
                String targetId = wrapper.getIdentifier(target);
                System.out.println((e.getKey() ? "Direct" : "Indirect")
                + " outgoing edge from " + sourceClassId + " to " + targetId + ": "
                        + outgoingEdge);
            });
        });

        if (directIndirectValidOutgoingEdges.values().stream().allMatch(s -> s.isEmpty())) {
            System.out.println("No outgoing edge found from " + sourceClassId + " to " + targetClassId);
            log.exit(); return;
        }

        System.out.println("First pass RelationTOs: ");
        Map<PipelineRelationTO<String>, Set<Integer>> directRelationTOs = new HashMap<>();
        Map<PipelineRelationTO<String>, Set<Integer>> indirectRelationTOs = new HashMap<>();
        this.generateRelationTOsFirstPassForOWLClass(sourceClass,
                directIndirectValidOutgoingEdges, directRelationTOs, indirectRelationTOs,
                new HashSet<>(), speciesIds);
        directRelationTOs.entrySet().stream()
        .forEach(e -> System.out.println("First pass direct RelationTOs: " + e.getKey() + " - "
                + " valid in species IDs: " + e.getValue()));
        indirectRelationTOs.entrySet().stream()
        .forEach(e -> System.out.println("First pass indirect RelationTOs: " + e.getKey() + " - "
                + " valid in species IDs: " + e.getValue()));

        System.out.println("Second pass RelationTOs: ");
        Map<PipelineRelationTO<String>, Set<TaxonConstraintTO<Integer>>> secondPassRelationTOs =
                this.generateRelationTOsSecondPass(directRelationTOs, indirectRelationTOs, speciesIds, 0);
        secondPassRelationTOs.entrySet().stream().forEach(e -> {
            System.out.print("Second pass RelationTOs: " + e.getKey() + " - TaxonConstraintTOs: ");
            e.getValue().stream().forEach(v -> System.out.print(v + " "));
            System.out.println();
        });

        log.exit();
    }

    public Map<Boolean, Set<OWLGraphEdge>> getValidOutgoingEdgesFromOWLClassIds(String sourceClassId,
            String targetClassId, Collection<Integer> speciesIds) {
        log.entry(sourceClassId, targetClassId, speciesIds);
        return log.exit(this.getValidOutgoingEdgesFromOWLClassIds(sourceClassId, targetClassId,
                speciesIds, false, new HashSet<>()));
    }

    public Map<Boolean, Set<OWLGraphEdge>> getValidOutgoingEdgesFromOWLClassIds(String sourceClassId,
            String targetClassId, Collection<Integer> speciesIds, boolean twoPassesValidation,
            Set<OWLClass> classesToIgnore) {
        log.entry(sourceClassId, targetClassId, speciesIds, twoPassesValidation, classesToIgnore);

        OWLClass sourceClass = this.getOWLClass(sourceClassId);
        if (sourceClass == null) {
            log.debug("Could not find OWLClass corresponding to {}", sourceClassId);
            return log.exit(null);
        }

        OntologyUtils utils = this.getOntologyUtils();
        OWLGraphWrapper wrapper = utils.getWrapper();
        return log.exit(
            this.getValidOutgoingEdgesForOWLClass(sourceClass, classesToIgnore, speciesIds)
                .entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> e.getValue().stream().filter(outgoingEdge -> {
                            if (targetClassId != null) {
                                OWLClass target = this.getOWLClass(wrapper.getIdentifier(
                                        outgoingEdge.getTarget()));
                                String targetId = wrapper.getIdentifier(target);
                                if (!targetClassId.equals(targetId)) {
                                    return false;
                                }
                            }
                            if (!twoPassesValidation) {
                                return true;
                            }

                            Map<PipelineRelationTO<String>, Set<Integer>> directRelationTOs = new HashMap<>();
                            Map<PipelineRelationTO<String>, Set<Integer>> indirectRelationTOs = new HashMap<>();
                            Map<Boolean, Set<OWLGraphEdge>> tempEdges = new HashMap<>();
                            tempEdges.put(e.getKey(), new HashSet<>(Arrays.asList(outgoingEdge)));
                            tempEdges.put(!e.getKey(), new HashSet<>());
                            this.generateRelationTOsFirstPassForOWLClass(sourceClass,
                                    tempEdges, directRelationTOs, indirectRelationTOs,
                                    classesToIgnore, speciesIds);
                            Map<PipelineRelationTO<String>, Set<TaxonConstraintTO<Integer>>>
                            secondPassRelationTOs = this.generateRelationTOsSecondPass(
                                    directRelationTOs, indirectRelationTOs, speciesIds, 0);
                            return !secondPassRelationTOs.isEmpty();
                        }).collect(Collectors.toSet())
        )));
    }

    /**
     * Retrieves edges outgoing from {@code cls} valid for Bgee, present in the ontologies wrapped by
     * this {@code uberon}. A {@code Map} with a {@code Boolean} key is returned to be able to distinguish
     * between direct and indirect outgoing edges. Of note, in this method, an outgoing edge
     * going through multiple classes can still be considered direct. A check on the number of valid
     * classes used in underlying axioms must be performed by methods calling this one
     * (see for instance method {@code InsertUberon#generateRelationTOsFirstPass}).
     *
     * @param cls               An {@code OWLClass} for which to retrieve outgoing direct
     *                          and indirect edges.
     * @param classesToIgnore   A {@code Set} of {@code OWLClass}es to be discarded, 
     *                          generated by the method {@code insertAnatOntologyIntoDataSource}.
     * @param speciesIds        A {@code Collection} of {@code Integer}s that are the NCBI IDs 
     *                          of the species to consider.
     *                          Only outgoing edges with the source and target existing 
     *                          in at least one of these species will be considered.
     *                          The validity of the relation itself in these species will not be checked
     *                          (GCI relation). Cannot be {@code null} nor empty.
     * @return                  A {@code Map} with a {@code Boolean} key, where a {@code Set} of
     *                          {@code OWLGraphEdge}s representing direct outgoing edges will be
     *                          associated to the key {@code true}, and a {@code Set} of
     *                          {@code OWLGraphEdge}s representing indirect outgoing edges will be
     *                          associated to the key {@code false}. Of note, in this method,
     *                          an outgoing edge going through multiple classes can still be
     *                          considered direct.
     * @throws IllegalArgumentException If it was not possible to retrieve an OBO-like ID 
     *                                  and a label for an {@code OWLClass} that should 
     *                                  have been considered.
     */
    public Map<Boolean, Set<OWLGraphEdge>> getValidOutgoingEdgesForOWLClass(OWLClass cls,
            Set<OWLClass> classesToIgnore, Collection<Integer> speciesIds) {
        log.entry(cls, classesToIgnore, speciesIds);

        OntologyUtils utils = this.getOntologyUtils();
        OWLGraphWrapper wrapper = utils.getWrapper();
        OWLObjectProperty partOf = wrapper.getOWLObjectPropertyByIdentifier(
                OntologyUtils.PART_OF_ID);

        Map<Boolean, Set<OWLGraphEdge>> directIndirectValidOutgoingEdges = new HashMap<>();
        Set<OWLGraphEdge> directValidOutgoingEdges = new HashSet<>();
        Set<OWLGraphEdge> indirectValidOutgoingEdges = new HashSet<>();
        directIndirectValidOutgoingEdges.put(true, directValidOutgoingEdges);
        directIndirectValidOutgoingEdges.put(false, indirectValidOutgoingEdges);

        //get equivalent class
        OWLClass mappedCls = this.getOWLClass(wrapper.getIdentifier(cls));
        if (mappedCls == null || 
                !this.isValidClass(mappedCls, classesToIgnore, speciesIds)) {
            return log.exit(directIndirectValidOutgoingEdges);
        }

        for (OWLOntology ont: wrapper.getAllOntologies()) {
            Set<OWLClass> allClasses = ont.getClassesInSignature(Imports.INCLUDED);
            if (!allClasses.contains(cls)) {
                continue;
            }

            //************************************
            // Relations outgoing from cls
            //************************************
            //we generate TOs relative to relations between terms. 
            //here we retrieve the graph closure outgoing from cls
            Set<OWLGraphEdge> allOutgoingEdges = 
                    wrapper.getOutgoingEdgesNamedClosureOverSupPropsWithGCI(cls);

            //we do not want to include develops_from or transformation_of relations 
            //propagated through is_a relations AFTER we reached the first 
            //developmental precursor. So, if A is_a B develops_from C is_a D, 
            //we will accept the edge A develops_from C, but not A develops_from D.
            //To filter such edges, we retrieve all targets of a transformation_of 
            //or develops_from relation, and remove among them the ancestors by is_a 
            //(we retain only the "leaves" by is_a). This is not bullet-proof 
            //(for instance, we surely would like to propagate to equivalent classes, 
            //that are seen as linked by an is_a relation), but this avoid to walk 
            //each edge, as the method getOutgoingEdgesNamedClosureOverSupPropsWithGCI does.
            //So, we first retrieve all targets of such relations: 
            Set<OWLClass> transfOfTargets = new HashSet<OWLClass>();
            Set<OWLClass> devFromTargets = new HashSet<OWLClass>();
            for (OWLGraphEdge outgoingEdge: allOutgoingEdges) {
                if (!(outgoingEdge.getTarget() instanceof OWLClass) || 
                        !wrapper.isRealClass(outgoingEdge.getTarget())) {
                    continue;
                }
                //make sure to call isTransformationOfRelation before 
                //isDevelopsFromRelation, because a transformation_of relation is also 
                //a develops_from relation.
                if (utils.isTransformationOfRelation(outgoingEdge)) {
                    transfOfTargets.add((OWLClass) outgoingEdge.getTarget());
                } else if (utils.isDevelopsFromRelation(outgoingEdge) && 
                        //just to be sure, in case the order of the code changes
                        !utils.isTransformationOfRelation(outgoingEdge)) {
                    devFromTargets.add((OWLClass) outgoingEdge.getTarget());
                }
            }
            //here we do something borderline: filter using a fake ObjectProperty, 
            //to retain leaves only through is_a relations.
            Set<OWLPropertyExpression> fakeProps = new HashSet<OWLPropertyExpression>(
                    Arrays.asList(wrapper.getManager().getOWLDataFactory().
                            getOWLObjectProperty(IRI.create(""))));
            utils.retainLeafClasses(transfOfTargets, fakeProps);
            utils.retainLeafClasses(devFromTargets, fakeProps);
            log.trace("Valid transformation_of targets of edges outgoing from {}: {}", 
                    cls, transfOfTargets);
            log.trace("Valid develops_from targets of edges outgoing from {}: {}", 
                    cls, devFromTargets);
            
            
            //we also get direct outgoingEdges to be able to know if a relation 
            //is direct or indirect
            Set<OWLGraphEdge> directOutgoingEdges = 
                    wrapper.getOutgoingEdgesWithGCI(cls);
            //and finally, we also create a fake edge to be an "identity" relation 
            //(because we want to insert reflexive relations into Bgee)
            OWLGraphEdge fakeEdge = new OWLGraphEdge(mappedCls, mappedCls, ont);
            allOutgoingEdges.add(fakeEdge);
            directOutgoingEdges.add(fakeEdge);
            
            edge: for (OWLGraphEdge outgoingEdge: allOutgoingEdges) {
                log.trace("Iterating outgoing edge {}", outgoingEdge);
                
                //to distinguish direct and indirect relations
                boolean isDirect = directOutgoingEdges.contains(outgoingEdge);
                
                //-------------Test validity of edge---------------
                if (outgoingEdge.getQuantifiedPropertyList().size() != 1) {
                    log.trace("Edge discarded because multiple or no property.");
                    continue edge;
                }
                //if it is a GCI relation, with make sure it is actually 
                //a taxonomy GCI relation
                if (outgoingEdge.isGCI() && 
                        (!isTaxonomyClass(outgoingEdge.getGCIFiller()) || 
                                !partOf.equals(outgoingEdge.getGCIRelation()))) {
                    log.trace("Edge discarded because it is a non-taxonomy GCI");
                    continue edge;
                }
                
                //-------------Test validity of target---------------
                if (!(outgoingEdge.getTarget() instanceof OWLClass)) {
                    log.trace("Edge discarded because target is not an OWLClass");
                    continue edge;
                }
                
                //if this is a transformation_of or develops_from edge, 
                //and this is not a direct edge, 
                //we check whether the target is allowed for propagation.
                //make sure to call isTransformationOfRelation before 
                //isDevelopsFromRelation, because a transformation_of relation is also 
                //a develops_from relation.
                if (!isDirect && 
                        ((utils.isTransformationOfRelation(outgoingEdge) && 
                                !transfOfTargets.contains(outgoingEdge.getTarget())) || 
                                (utils.isDevelopsFromRelation(outgoingEdge) && 
                                        !utils.isTransformationOfRelation(outgoingEdge) && 
                                        !devFromTargets.contains(outgoingEdge.getTarget()))) ) {
                    
                    log.trace("Edge discarded because target is invalid for develops_from or transformation_of relation");
                    continue edge;
                }
                
                OWLClass target = (OWLClass) outgoingEdge.getTarget();
                if (!this.isValidClass(target, classesToIgnore, speciesIds)) {
                    log.trace("Edge discarded because target is invalid");
                    continue edge;
                }
                //get equivalent class
                target = this.getOWLClass(wrapper.getIdentifier(target));
                if (target == null || 
                        !this.isValidClass(target, classesToIgnore, speciesIds)) {
                    log.trace("Edge discarded because target is invalid");
                    continue edge;
                }

                if (isDirect) {
                    directValidOutgoingEdges.add(outgoingEdge);
                } else {
                    indirectValidOutgoingEdges.add(outgoingEdge);
                }
            }
        }
        return log.exit(directIndirectValidOutgoingEdges);
    }

    /**
     * Checks whether {@code cls} is a valid {@code OWLClass} to be considered for insertion.
     * 
     * @param cls               An {@code OWLClass} representing an anatomical entity 
     *                          to be considered for insertion.
     * @param classesToIgnore   A {@code Set} of {@code OWLClass}es to be discarded, 
     *                          generated by the method {@code insertAnatOntologyIntoDataSource}.
     * @param speciesIds        A {@code Collection} of {@code Integer}s that are the NCBI IDs 
     *                          of the species to consider, as provided to the method 
     *                          {@code insertAnatOntologyIntoDataSource}. 
     *                          Only anatomical entities existing 
     *                          in at least one of these species will be considered.
     *                          Cannot be {@code null} or empty.
     * @return                  {@code true} if {@code cls} is a valid {@code OWLClass} 
     *                          considered for insertion, {@code false} if {@code cls} 
     *                          was discarded (because member of {@code classesToIgnore}, 
     *                          or not a member of the provided species, ...).
     * @throws IllegalArgumentException If it was not possible to retrieve an OBO-like ID 
     *                                  and a label for {@code cls}.
     */
    public boolean isValidClass(OWLClass cls, Set<OWLClass> classesToIgnore, Collection<Integer> speciesIds) {
        log.entry(cls, classesToIgnore, speciesIds);
        
        OntologyUtils utils = this.getOntologyUtils();
        OWLGraphWrapper wrapper = utils.getWrapper();
        
        //keep the stage only if exists in one of the requested species, 
        //and if not obsolete, and if not a class to ignore
        if (classesToIgnore.contains(cls) || 
                !this.existsInAtLeastOneSpecies(cls, speciesIds) || 
                !wrapper.isRealClass(cls)) {
            log.trace("Class discarded");
            return log.exit(false);
        }
        
        //check that we always have an ID and a name, only for class that will not be 
        //replaced by another one
        if (cls.equals(this.getOWLClass(wrapper.getIdentifier(cls)))) {
            String id = wrapper.getIdentifier(cls);
            if (StringUtils.isBlank(id)) {
                throw log.throwing(new IllegalArgumentException("No OBO-like ID retrieved for " + 
                        cls));
            }
            String name = wrapper.getLabel(cls);
            if (StringUtils.isBlank(name)) {
                throw log.throwing(new IllegalArgumentException("No label retrieved for " + 
                        cls));
            }
        }
        
        return log.exit(true);
    }

    /**
     * Method to determine whether an {@code OWLObject} is an {@code OWLClass}
     * part of the taxonomy ontology.
     *
     * @param o An {@code OWLObject} to test.
     * @return  {@code true} if {@code o} is an {@code OWLClass} part of the taxonomy ontology.
     */
    public boolean isTaxonomyClass(OWLObject o) {
        log.entry(o);
        OWLGraphWrapper wrapper = this.getOntologyUtils().getWrapper();
        return log.exit(wrapper.getAncestorsThroughIsA(o).contains(
                wrapper.getOWLClassByIdentifier(UberonCommon.TAXONOMY_ROOT_ID, true)));
    }

    /**
     * Execute the first pass generating {@code RelationTO}s representing relations between 
     * {@code OWLClass}es and their related {@code TaxonConstraintTO}s, present 
     * in the ontologies wrapped by {@code uberon}. The {@code Map}s {@code directRelationTOs} 
     * and {@code indirectRelationTOs} provided as arguments will be modified as a result 
     * of the call to this method.
     * <p>
     * This method will store {@code RelationTO}s representing 
     * direct relations into the {@code Map} {@code directRelationTOs}, those representing 
     * indirect relations into the {@code Map} {@code indirectRelationTOs}. In these 
     * {@code Map}s, the {@code RelationTO}s will be associated to a {@code Set} of 
     * {@code Integer}s, representing the NCBI IDs of the species they are valid for. 
     * We need to store them separately to latter be able to identify  
     * direct relations with different taxon constraints as compared to an equivalent 
     * indirect relations, in order to keep only the taxa not defined for the indirect 
     * relation.
     * <p>
     * This method is called by {@link #generateRelationInformation(Uberon, Set, Collection)}, 
     * and will be followed by a call to 
     * {@link #generateRelationTOsSecondPass(Map, Map, Collection)}. We need to do a first pass 
     * through all relations, to be able to detect relations leading to equivalent classes, 
     * or relations equivalent, but with different taxon constraints. The second pass 
     * will assign proper relation IDs and {@code RelationStatus}. They are split into 
     * two separated methods only for clarity.
     * 
     * @param directRelationTOs A {@code Map} where keys are {@code RelationTO}s, that will 
     *                          store direct relations between anatomical entities, 
     *                          the associated value being a {@code Set} of {@code Integer}s, 
     *                          that will store the NCBI IDs of the species the relation 
     *                          is valid for. This {@code Map} will be filled as a result 
     *                          of the call to this method.
     * @param indirectRelationTOs A {@code Map} where keys are {@code RelationTO}s, that will 
     *                          store indirect relations between anatomical entities, 
     *                          the associated value being a {@code Set} of {@code Integer}s, 
     *                          that will store the NCBI IDs of the species the relation 
     *                          is valid for. This {@code Map} will be filled as a result 
     *                          of the call to this method.
     * @param uberon            An {@code Uberon} wrapping the anatomical ontology 
     *                          to be inserted.
     * @param classesToIgnore   A {@code Set} of {@code OWLClass}es to be discarded, 
     *                          generated by the method {@code insertAnatOntologyIntoDataSource}.
     * @param speciesIds        A {@code Collection} of {@code Integer}s that are the NCBI IDs 
     *                          of the species to consider, as provided to the method 
     *                          {@code insertAnatOntologyIntoDataSource}. 
     *                          Only anatomical entities and relations existing 
     *                          in at least one of these species will be considered. 
     * @see #generateRelationInformation(Uberon, Set, Collection)
     * @see #generateRelationTOsSecondPass(Map, Map, Collection)
     * @throws IllegalArgumentException If it was not possible to retrieve an OBO-like ID 
     *                                  and a label for an {@code OWLClass} that should 
     *                                  have been considered.
     */
    public void generateRelationTOsFirstPassForOWLClass(OWLClass cls,
            Map<Boolean, Set<OWLGraphEdge>> directIndirectValidOutgoingEdges,
            Map<PipelineRelationTO<String>, Set<Integer>> directRelationTOs, 
            Map<PipelineRelationTO<String>, Set<Integer>> indirectRelationTOs, 
            Set<OWLClass> classesToIgnore, Collection<Integer> speciesIds) {
        log.entry(directRelationTOs, indirectRelationTOs, classesToIgnore, speciesIds);
        
        OntologyUtils utils = this.getOntologyUtils();
        OWLGraphWrapper wrapper = utils.getWrapper();
        
        OWLClass mappedCls = this.getOWLClass(wrapper.getIdentifier(cls));
        if (mappedCls == null || !this.isValidClass(mappedCls, classesToIgnore, speciesIds)) {
            log.exit(); return;
        }
        String id = wrapper.getIdentifier(mappedCls);

        Set<OWLGraphEdge> allOutgoingEdges = new HashSet<>(directIndirectValidOutgoingEdges.get(false));
        allOutgoingEdges.addAll(directIndirectValidOutgoingEdges.get(true));
        Set<OWLGraphEdge> directOutgoingEdges = directIndirectValidOutgoingEdges.get(true);
        
        edge: for (OWLGraphEdge outgoingEdge: allOutgoingEdges) {
            boolean isDirect = directOutgoingEdges.contains(outgoingEdge);
            OWLClass target = this.getOWLClass(wrapper.getIdentifier(outgoingEdge.getTarget()));
            String targetId = wrapper.getIdentifier(target);
            
            //-------------Generate RelationTOs and taxon constraints---------------
            RelationType relType = null;
            if (utils.isASubClassOfEdge(outgoingEdge) || 
                    utils.isPartOfRelation(outgoingEdge)) {
                relType = RelationType.ISA_PARTOF;
            }
            //make sure to call isTransformationOfRelation before 
            //isDevelopsFromRelation, because a transformation_of relation is also 
            //a develops_from relation.
            else if (utils.isTransformationOfRelation(outgoingEdge)) {
                relType = RelationType.TRANSFORMATIONOF;
            } else if (utils.isDevelopsFromRelation(outgoingEdge) && 
                    //just to be sure, in case the order of the code changes
                    !utils.isTransformationOfRelation(outgoingEdge)) {
                relType = RelationType.DEVELOPSFROM;
            } else {
                throw log.throwing(new IllegalArgumentException("The provided ontology " +
                        "contains a relation that is not recognized: " + outgoingEdge));
            }
            
            //now, get the taxon constraints for this relation 
            Set<Integer> speciesIdsToConsider = new HashSet<Integer>(speciesIds);
            if (outgoingEdge.isGCI()) {
                //if it is a GCI, we retrieve the associated species
                Set<String> speciesClsIdsToConsider = new HashSet<String>();
                speciesClsIdsToConsider.add(
                        wrapper.getIdentifier(outgoingEdge.getGCIFiller()));
                for (OWLClass taxonGCIDescendants: 
                    wrapper.getDescendantsThroughIsA(outgoingEdge.getGCIFiller())) {
                    speciesClsIdsToConsider.add(
                            wrapper.getIdentifier(taxonGCIDescendants));
                }
                speciesIdsToConsider = 
                        OntologyUtils.convertToNcbiIds(speciesClsIdsToConsider);
                speciesIdsToConsider.retainAll(speciesIds);
                
            } 
            //in any case, we apply the maximal taxon constraints from all OWLClasses 
            //that were walked on the path
            Set<OWLClass> classesWalked = new HashSet<OWLClass>();
            for (OWLAxiom ax: outgoingEdge.getAxioms()) {
                classesWalked.addAll(ax.getClassesInSignature());
            }
            int classCount = 0;
            for (OWLClass clsWalked: classesWalked) {
                OWLClass mappedClsWalked = 
                        this.getOWLClass(wrapper.getIdentifier(clsWalked));
                if (mappedClsWalked == null || 
                        //FIXME: Do we want to completely discard an edge simply because
                        //it walks through a class to ignore?
                        //Apparently, lots of indirect relations were invalid in the database,
                        //they could not be retrieved by walking a chain of direct relation,
                        //which causes inconsistencies in the analyses. This code here should have avoided
                        //that, by discarding both the direct and indirect relations going through
                        //a class to ignore. Did this code work correctly?
                        !this.isValidClass(mappedClsWalked, classesToIgnore, speciesIds) ||
                        outgoingEdge.isGCI() && mappedClsWalked.equals(outgoingEdge.getGCIFiller())) {
                    continue;
                }
                Set<Integer> inSpecies = this.existsInSpecies(mappedClsWalked, 
                        speciesIds);
                log.trace("OWLClass walked to produce the edge: {} - Mapped to OWLClass: {} - Exists in species: {}", 
                        clsWalked, mappedClsWalked, inSpecies);
                boolean changed = speciesIdsToConsider.retainAll(inSpecies);
                if (log.isDebugEnabled() && changed && outgoingEdge.isGCI()) {
                    //It's actually OK, the OWLGraphWrapper does not use taxon constraints, 
                    //so it can't determine this. Here, this is an improvement 
                    //for the Bgee pipeline.
                    log.debug("A GCI relation is supposed to exist in taxon {}, "
                            + "but it was produced by combining edges going through "
                            + "classes not existing in this taxon, notably the class {}, " +
                            "existing in species {}. Offending GCI relation: {}", 
                            outgoingEdge.getGCIFiller(), mappedClsWalked, 
                            inSpecies, outgoingEdge);
                }
                classCount++;
            }
            //we sometimes have relations like:
            //SubClassOf(ObjectIntersectionOf(<http://purl.obolibrary.org/obo/UBERON_0010011>
            //ObjectSomeValuesFrom(<http://purl.obolibrary.org/obo/BFO_0000050>
            //<http://purl.obolibrary.org/obo/NCBITaxon_9443>))
            //ObjectSomeValuesFrom(<http://purl.obolibrary.org/obo/BFO_0000050>
            //<http://purl.obolibrary.org/obo/UBERON_0010011>)),
            //SubClassOf(Annotation(<http://www.geneontology.org/formats/oboInOwl#source> "FMA"^^xsd:string)
            //<http://purl.obolibrary.org/obo/UBERON_0010011> <http://purl.obolibrary.org/obo/UBERON_0010009>)]
            //It would be considered as an indirect relation, but we don't want that.
            if (classCount <= 2) {
                isDirect = true;
            }
            //and now, in case it was a fake relation with no axioms, e.g., 
            //reflexive edge
            speciesIdsToConsider.retainAll(this.existsInSpecies(mappedCls, speciesIds));
            speciesIdsToConsider.retainAll(this.existsInSpecies(target, speciesIds));
            
            if (speciesIdsToConsider.isEmpty()) {
                //exists in no species, discard
                log.trace("Discarding edge because exists in no species: {}", 
                        outgoingEdge);
                continue edge;
            }
            
            //create RelationTO.
            //we create a RelationTO with null RelationStatus in any case, 
            //to be able to compare relations. Correct RelationStatus and relation ID 
            //will be assigned during the second pass.
            PipelineRelationTO<String> relTO = new PipelineRelationTO<>(null, id, targetId, relType, null);
            log.trace("RelationTO generated: {} - is direct relation: {}", 
                    relTO, isDirect);
            //generate taxon constraints
            Set<Integer> inSpecies = null;
            if (isDirect) {
                inSpecies = directRelationTOs.get(relTO);
                if (inSpecies == null) {
                    inSpecies = new HashSet<Integer>();
                    directRelationTOs.put(relTO, inSpecies);
                }
            } else {
                inSpecies = indirectRelationTOs.get(relTO);
                if (inSpecies == null) {
                    inSpecies = new HashSet<Integer>();
                    indirectRelationTOs.put(relTO, inSpecies);
                }
            }
            inSpecies.addAll(speciesIdsToConsider);
            log.trace("Complete taxon constraints generated so far for this RelationTO: {}", 
                    inSpecies);
        }
        
        log.exit();
    }

    /**
     * Execute the second pass generating {@code RelationTO}s representing relations between 
     * {@code OWLClass}es and their related {@code TaxonConstraintTO}s. 
     * The {@code Map}s {@code directRelationTOs} and {@code indirectRelationTOs} 
     * are used to generate the final {@code RelationTOs} and {@code TaxonConstraintTO}s,  
     * that will be stored in {@link #anatRelationTOs} and {@link #anatRelTaxonConstraintTOs}.
     * <p>
     * This method is called by {@link #generateRelationInformation(Uberon, Set, Collection)}, 
     * after a call to 
     * {@link #generateRelationTOsFirstPass(Map, Map, Uberon, Set, Collection)}. 
     * We need to do a first pass 
     * through all relations, to be able to detect relations leading to equivalent classes, 
     * or relations equivalent, but with different taxon constraints. The second pass 
     * will assign proper relation IDs and {@code RelationStatus}. They are split into 
     * two separated methods only for clarity.
     * 
     * @param directRelationTOs A {@code Map} where keys are {@code RelationTO}s, that are 
     *                          the direct relations between anatomical entities, 
     *                          the associated value being a {@code Set} of {@code Integer}s, 
     *                          that are the NCBI IDs of the species the relation 
     *                          is valid for. 
     * @param indirectRelationTOs A {@code Map} where keys are {@code RelationTO}s, that are 
     *                          the indirect relations between anatomical entities, 
     *                          the associated value being a {@code Set} of {@code Integer}s, 
     *                          that are the NCBI IDs of the species the relation 
     *                          is valid for. 
     * @param speciesIds        A {@code Collection} of {@code Integer}s that are the NCBI IDs 
     *                          of the species to consider, as provided to the method 
     *                          {@code insertAnatOntologyIntoDataSource}. 
     *                          Only anatomical entities and relations existing 
     *                          in at least one of these species will be considered.
     * @param relationIdStart   A {@code int} that is the ID where to start the increment of relation IDs
     *                          from.
     * @return                  A {@code Map} where keys are the generated {@code PipelineRelationTO}s,
     *                          the associated value being a {@code Set} storing the {@code TaxonConstraintTO}s
     *                          for the related relation.
     * @see #generateRelationTOsFirstPassForOWLClass(OWLClass, Map, Map, Map, Set, Set, Collection)
     */
    public Map<PipelineRelationTO<String>, Set<TaxonConstraintTO<Integer>>>
    generateRelationTOsSecondPass(Map<PipelineRelationTO<String>, Set<Integer>> directRelationTOs, 
            Map<PipelineRelationTO<String>, Set<Integer>> indirectRelationTOs,
            Collection<Integer> speciesIds, int relationIdStart) {
        log.entry(directRelationTOs, indirectRelationTOs, speciesIds, relationIdStart);
        
        int relationId = relationIdStart;
        Set<RelationTO<String>> allRelationTOs = new HashSet<>(directRelationTOs.keySet());
        allRelationTOs.addAll(indirectRelationTOs.keySet());
        Map<PipelineRelationTO<String>, Set<TaxonConstraintTO<Integer>>> relsToTCs = new HashMap<>();
        
        for (RelationTO<String> relTO: allRelationTOs) {
            log.trace("Iterating relation: {}", relTO);
            
            RelationStatus relStatus = null;
            Set<Integer> inSpecies = null;
            if (relTO.getSourceId().equals(relTO.getTargetId())) {
                //if it is not an is_a relation, it is not a reflexive relation, 
                //but an incorrect cycle.
                if (!relTO.getRelationType().equals(RelationType.ISA_PARTOF)) {
                    log.trace("Discarding relationTO because it is a cycle: {}", relTO);
                    continue;
                }
                
                log.trace("Relation is reflexive");
                relStatus = RelationStatus.REFLEXIVE;
                //reflexive relations must be stored as direct relations
                inSpecies = directRelationTOs.get(relTO);
                assert inSpecies != null;
                
            } else if (indirectRelationTOs.containsKey(relTO)) {
                log.trace("Relation is indirect");
                relStatus = RelationStatus.INDIRECT;
                inSpecies = indirectRelationTOs.get(relTO);
                
                //if there is also an equivalent direct relation with different taxon constraints, 
                //we keep only the taxon constraints not present in the indirect relation
                Set<Integer> directInSpecies = directRelationTOs.get(relTO);
                if (directInSpecies != null) {
                    directInSpecies.removeAll(inSpecies);
                    if (!directInSpecies.isEmpty()) {
                        
                        relationId++;
                        PipelineRelationTO<String> newRelTO = new PipelineRelationTO<>(relationId,
                                relTO.getSourceId(), relTO.getTargetId(), 
                                relTO.getRelationType(), RelationStatus.DIRECT);
                        log.trace("An equivalent direct relation also exists, but with different " +
                                "taxon constraints, generating direct redundant relation: {}", 
                                newRelTO);
                        
                        //such a relation should never be defined for all taxa at this point
                        if (directInSpecies.containsAll(speciesIds)) {
                            throw log.throwing(new AssertionError("Incorrect taxon constraints " +
                                    "for direct redundant relation: " + relTO));
                        }
                        relsToTCs.put(newRelTO, getRelationTaxonConstraints(relationId, directInSpecies, 
                                speciesIds));
                    } else {
                        log.trace("An equivalent direct relation also exists, but taxon constraints " +
                                "are a subset of the indirect relation, discarding direct relation");
                    }
                }
            } else if (directRelationTOs.containsKey(relTO)) {
                log.trace("Relation is direct");
                relStatus = RelationStatus.DIRECT;
                inSpecies = directRelationTOs.get(relTO);
            }
            
            relationId++;
            PipelineRelationTO<String> newRelTO = new PipelineRelationTO<>(relationId,
                    relTO.getSourceId(), relTO.getTargetId(), 
                    relTO.getRelationType(), relStatus);
            log.trace("Generating proper RelationTO: {}", newRelTO);

            relsToTCs.put(newRelTO, getRelationTaxonConstraints(relationId, inSpecies, 
                    speciesIds));
        }
        return log.exit(relsToTCs);
    }

    /**
     * Convenient method to generate the {@code TaxonConstraintTO}s associated to 
     * a relation between anatomical entities.
     * 
     * @param relationId        An {@code int} that is the ID of a relation for which 
     *                          to associate taxon constraints to.
     * @param inSpecies         A {@code Set} of {@code Integer}s that the NCBI IDs of the species 
     *                          the relation is defined for.
     * @param allowedSpeciesIds A {@code Collection} of {@code Integer}s that the NCBI IDs 
     *                          of the allowed species to be inserted into the data source.
     * @return                  A {@code Set} of {@code TaxonConstraintTO}s generated from
     *                          the arguments.
     * @see #generateRelationTOsSecondPass(Map, Map, Collection, int)
     */
    private static Set<TaxonConstraintTO<Integer>> getRelationTaxonConstraints(int relationId, Set<Integer> inSpecies, 
            Collection<Integer> allowedSpeciesIds) {
        log.entry(relationId, inSpecies, allowedSpeciesIds);

        Set<TaxonConstraintTO<Integer>> taxonConstraints = new HashSet<>();
        if (inSpecies.containsAll(allowedSpeciesIds)) {
            //a null speciesId means: exists in all species
            TaxonConstraintTO<Integer> taxConstraintTO = 
                    new TaxonConstraintTO<>(relationId, null);
            log.trace("Taxon constraint: {}", taxConstraintTO);
            taxonConstraints.add(taxConstraintTO);
        } else if (!inSpecies.isEmpty()) {
            for (int speciesId: inSpecies) {
                TaxonConstraintTO<Integer> taxConstraintTO = new TaxonConstraintTO<>(
                        relationId, speciesId);
                log.trace("Taxon constraint: {}", taxConstraintTO);
                taxonConstraints.add(taxConstraintTO);
            }
        } else {
            throw log.throwing(new AssertionError("Relation with no taxon constraints defined."));
        }
        
        return log.exit(taxonConstraints);
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
//        for (OWLClass iterateClass: wrapper.getAllRealOWLClasses()) {
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
