package org.bgee.pipeline.uberon;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.CommandRunner;
import org.bgee.pipeline.OntologyUtils;
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

import owltools.graph.OWLGraphEdge;
import owltools.graph.OWLGraphWrapper;

/**
 * Class related to the use, and insertion into the database of the ontology Uberon.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class Uberon {
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
     * <li>If the first element in {@code args} is "extractRelatedRelations", the action 
     * will be to retrieve all {@code OWLGraphEdge}s related to the relation specified, 
     * or any of its sub-property, and write them into an output file, 
     * see {@link #extractRelatedEdgesToOutputFile(String, String, IRI)}.
     * Following elements in {@code args} must then be: 
     *   <ol>
     *   <li>path to the Uberon ontology with all relations used
     *   <li>path to the output file where to write the relations
     *   <li>IRI of the relation, for instance 
     *   {@code http://purl.obolibrary.org/obo/RO_0002324}
     *   </ol>
     * <li>If the first element in {@code args} is "simplifyUberon", the action 
     * will be to simplify the Uberon ontology and to save it to files in OBO and OWL formats, 
     * see {@link #simplifyUberonAndSaveToFile(String, String, Collection, Collection, 
     * Collection, Collection, Collection) simplifyUberonAndSaveToFile}.
     * Following elements in {@code args} must then be: 
     *   <ol>
     *   <li>path to the file storing the Uberon ontology.
     *   <li>path to use to generate the files storing the resulting 
     *   ontology in OBO and OWL. The prefixes ".owl" or ".obo" willb e automatically added.
     *   <li>A list of OBO-like IDs of {@code OWLClass}es to remove from the ontology, 
     *   and to propagate their incoming edges to their outgoing edges. These IDs must be 
     *   separated by the {@code String} {@link CommandRunner#LIST_SEPARATOR}.
     *   <li>A list of OBO-like IDs or {@code IRI}s of relations to be filtered 
     *   and mapped to parent relations. These IDs must be separated by the {@code String} 
     *   {@link CommandRunner#LIST_SEPARATOR}.
     *   <li>A list of OBO-like IDs of the {@code OWLClass}es that are the roots 
     *   of the subgraphs that will be kept in the ontology. These IDs must be 
     *   separated by the {@code String} {@link CommandRunner#LIST_SEPARATOR}.
     *   <li>A list of OBO-like IDs of the {@code OWLClass}es that are the roots 
     *   of the subgraphs to be removed from the ontology. These IDs must be 
     *   separated by the {@code String} {@link CommandRunner#LIST_SEPARATOR}.
     *   <li>A list of names of targeted subsets, for which member {@code OWLClass}es 
     *   should have their is_a/part_of incoming edges removed. These IDs must be 
     *   separated by the {@code String} {@link CommandRunner#LIST_SEPARATOR}.
     *   </ol>
     *   Example of command line usage for this task: {@code java -Xmx2g -jar myJar 
     *   Uberon simplifyUberon ext.owl custom_ext 
     *   UBERON:0000480,UBERON:0000061,UBERON:0000465,UBERON:0001062,UBERON:0000475,UBERON:0000468,UBERON:0010000,UBERON:0003103,UBERON:0000062,UBERON:0000489 
     *   BFO:0000050,RO:0002202,http://semanticscience.org/resource/SIO_000657 
     *   UBERON:0013701,UBERON:0000026,UBERON:0000467,UBERON:0011676 
     *   none 
     *   grouping_class,non_informative,ubprop:upper_level,upper_level}
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
            
            new Uberon().extractTaxonIds(args[1], args[2]);

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
            if (args.length != 9) {
                throw log.throwing(new IllegalArgumentException(
                        "Incorrect number of arguments provided, expected " + 
                        "9 arguments, " + args.length + " provided."));
            }
            new Uberon().simplifyUberonAndSaveToFile(args[1], args[2], args[3], 
                    CommandRunner.parseListArgument(args[4]), 
                    CommandRunner.parseListArgument(args[5]), 
                    CommandRunner.parseListArgument(args[6]), 
                    CommandRunner.parseListArgument(args[7]), 
                    CommandRunner.parseListArgument(args[8]));
        }
        
        log.exit();
    }
    
    /**
     * A {@code Set} of {@code String}s that are the OBO-like IDs of {@code OWLClass}es 
     * removed as a result of graph filtering. Graph filtering is performed in the 
     * {@code simplifyUberon} method by calling 
     * {@code owltools.graph.OWLGraphManipulator#filterSubgraphs(Collection)} and 
     * {@code owltools.graph.OWLGraphManipulator#removeSubgraphs(Collection, boolean)}
     */
    private final Set<String> subgraphClassesRemoved;
    
    /**
     * Default constructor.
     */
    public Uberon() {
        this.subgraphClassesRemoved = new HashSet<String>();
    }
    
    /**
     * Simplifies the Uberon ontology stored in {@code pathToUberonOnt}, and saves it in OWL 
     * and OBO format using {@code modifiedOntPath}. Various information about 
     * the simplification process can be stored in a separate file, provided through 
     * {@code infoFilePath} (for instance, list of {@code OWLClass}es that were removed 
     * following a subgraph filtering). This argument can be left {@code null} or blank 
     * if this information does not need to be stored. 
     * <p>
     * This method first calls  
     * {@link #simplifyUberon(OWLOntology, Collection, Collection, Collection, Collection, 
     * Collection)}, by loading the {@code OWLOntology} provided through {@code pathToUberonOnt}, 
     * and using arguments of same names as in this method. The resulting {@code OWLOntology} 
     * is then saved, in OBO (with a ".obo" extension to the path {@code exportPath}), 
     * and in OWL (with a ".owl" extension to the path {@code exportPath}).
     * 
     * @param pathToUberonOnt           A {@code String} that is the path to the file 
     *                                  storing the Uberon ontology (recommended version is OWL, 
     *                                  but OBO versions can be used as well).
     * @param modifiedOntPath           A {@code String} that is the path to use to save 
     *                                  the resulting {@code OWLOntology} in files 
     *                                  (suffixes ".obo" and ".owl" will be automatically added).
     * @param infoFilePath              A {@code String} that is the path to a file that will 
     *                                  store various information about the simplification 
     *                                  process.
     * @param classIdsToRemove          See same name argument in method {@code simplifyUberon}.
     * @param relIds                    See same name argument in method {@code simplifyUberon}.
     * @param toFilterSubgraphRootIds   See same name argument in method {@code simplifyUberon}.
     * @param toRemoveSubgraphRootIds   See same name argument in method {@code simplifyUberon}.
     * @param subsetNames               See same name argument in method {@code simplifyUberon}.
     * @throws IOException                      If an error occurred while reading the file 
     *                                          {@code pathToUberonOnt}.
     * @throws OBOFormatParserException         If the ontology was provided in OBO format 
     *                                          and a parser error occurred. 
     * @throws OWLOntologyCreationException     If an error occurred while loading 
     *                                          the ontology to modify it.
     * @throws UnknownOWLOntologyException      If an error occurred while loading 
     *                                          the ontology to modify it.
     * @throws OWLOntologyStorageException      If an error occurred while saving the resulting 
     *                                          ontology in OWL.
     * @see #simplifyUberon(OWLOntology, Collection, Collection, Collection, Collection, 
     * Collection)
     */
    public void simplifyUberonAndSaveToFile(String pathToUberonOnt, String modifiedOntPath, 
            String infoFilePath, 
            Collection<String> classIdsToRemove, Collection<String> relIds, 
            Collection<String> toFilterSubgraphRootIds, 
            Collection<String> toRemoveSubgraphRootIds, Collection<String> subsetNames) 
                    throws UnknownOWLOntologyException, OWLOntologyCreationException, 
                    OBOFormatParserException, IOException, OWLOntologyStorageException {
        log.entry(pathToUberonOnt, modifiedOntPath, infoFilePath, classIdsToRemove, relIds, 
                toFilterSubgraphRootIds, toRemoveSubgraphRootIds, subsetNames);
        
        OWLOntology ont = OntologyUtils.loadOntology(pathToUberonOnt);
        
        this.simplifyUberon(ont, classIdsToRemove, relIds, toFilterSubgraphRootIds, 
                toRemoveSubgraphRootIds, subsetNames);

        OntologyUtils utils = new OntologyUtils(ont);
        utils.saveAsOWL(modifiedOntPath + ".owl");
        utils.saveAsOBO(modifiedOntPath + ".obo");
        
        log.exit();
    }
    
    /**
     * Simplifies {@code uberonOnt} by using an {@code OWLGraphManipulator}. This method 
     * calls {@code owltools.graph.OWLGraphManipulator#simplifies(Collection, Collection, 
     * Collection, Collection, Collection)} using the arguments of this method, then 
     * removes the {@code OWLAnnotationAssertionAxiom}s that are problematic to convert 
     * the ontology in OBO, using 
     * {@link org.bgee.pipeline.OntologyUtils#removeOBOProblematicAxioms()}.
     * <p>
     * Note that the {@code OWLOntology} passed as argument will be modified as a result 
     * of the call to this method.
     *  
     * @param uberonOnt                         The {@code OWLOntology} to simplify.
     * @param classIdsToRemove                  A {@code Collection} of {@code String}s that 
     *                                          are the OBO-like IDs of {@code OWLClass}es 
     *                                          to remove from the ontology, and to propagate 
     *                                          their incoming edges to their outgoing edges. 
                                                (see same argument in 
     *                                          {@code OWLGraphManipulator.simplifies} method).
     * @param relIds                            A {@code Collection} of {@code String}s that 
     *                                          are the OBO-like IDs or {@code IRI}s of relations 
     *                                          to be filtered and mapped to parent relations 
     *                                          (see same argument in 
     *                                          {@code OWLGraphManipulator.simplifies} method).
     * @param toFilterSubgraphRootIds           A {@code Collection} of {@code String}s that 
     *                                          are the OBO-like IDs of the {@code OWLClass}es 
     *                                          that are the roots of the subgraphs that 
     *                                          will be kept in the ontology. Their ancestors 
     *                                          will be kept as well. (see same argument in 
     *                                          {@code OWLGraphManipulator.simplifies} method).
     * @param toRemoveSubgraphRootIds           A {@code Collection} of {@code String}s that 
     *                                          are the OBO-like IDs of the {@code OWLClass}es 
     *                                          that are the roots of the subgraphs to be 
     *                                          removed from the ontology. Classes part both of 
     *                                          a subgraph to remove and a subgraph not 
     *                                          to be removed will be kept (see same argument in 
     *                                          {@code OWLGraphManipulator.simplifies} method).
     * @param subsetNames                       A {@code Collection} of {@code String}s that 
     *                                          are the names of the targeted subsets, for which 
     *                                          member {@code OWLClass}es should have 
     *                                          their is_a/part_of incoming edges removed. 
                                                (only if the source of the incoming edge 
     *                                          will not be left orphan of other is_a/part_of 
     *                                          relations to {@code OWLClass}es not in 
     *                                          {@code subsets}, see same argument in 
     *                                          {@code OWLGraphManipulator.simplifies} method).
     * @throws OWLOntologyCreationException     If an error occurred while wrapping 
     *                                          the {@code uberonOnt} into an 
     *                                          {@code OWLGraphManipulator}.
     * @throws UnknownOWLOntologyException      If an error occurred while wrapping 
     *                                          the {@code uberonOnt} into an 
     *                                          {@code OWLGraphManipulator}.
     */
    public void simplifyUberon(OWLOntology uberonOnt, Collection<String> classIdsToRemove, 
            Collection<String> relIds, Collection<String> toFilterSubgraphRootIds, 
            Collection<String> toRemoveSubgraphRootIds, Collection<String> subsetNames) 
                    throws UnknownOWLOntologyException, OWLOntologyCreationException {
        log.entry(uberonOnt, classIdsToRemove, relIds, toFilterSubgraphRootIds, 
                toRemoveSubgraphRootIds, subsetNames);
        //TODO: dependency injection?
        OWLGraphManipulator manipulator = new OWLGraphManipulator(uberonOnt);
        
        manipulator.reduceRelations();
        manipulator.reducePartOfIsARelations();
        for (String classIdToRemove: classIdsToRemove) {
            manipulator.removeClassAndPropagateEdges(classIdToRemove);
        }
        if (relIds != null && !relIds.isEmpty()) {
            manipulator.mapRelationsToParent(relIds);
            manipulator.filterRelations(relIds, true);
        }
        if (toFilterSubgraphRootIds != null && !toFilterSubgraphRootIds.isEmpty()) {
            this.subgraphClassesRemoved.addAll(
                    manipulator.filterSubgraphs(toFilterSubgraphRootIds));
        }
        if (toRemoveSubgraphRootIds != null && !toRemoveSubgraphRootIds.isEmpty()) {
            this.subgraphClassesRemoved.addAll(
                    manipulator.removeSubgraphs(toRemoveSubgraphRootIds, true));
        }
        if (subsetNames != null && !subsetNames.isEmpty()) {
            manipulator.removeRelsToSubsets(subsetNames, toFilterSubgraphRootIds);
        }
        
//        manipulator.simplifies(
//                Arrays.asList(OntologyUtils.PART_OF_ID),//, 
//                              //OntologyUtils.DEVELOPS_FROM_ID, 
//                              //OntologyUtils.TRANSFORMATION_OF_ID), 
//                Arrays.asList("UBERON:0013701",  //main body axis
//                              "UBERON:0000026",  //appendage
//                              "UBERON:0000467", //anatomical system
//                              "UBERON:0011676"), //subdivision of organism along main body axis
//                null, 
//                Arrays.asList("UBERON:0000480", //anatomical group
//                              "UBERON:0000061", //anatomical structure
//                              "UBERON:0000465", //material anatomical entity
//                              "UBERON:0001062", //anatomical entity
//                              "UBERON:0000475", //organism subdivision
//                              "UBERON:0000468", //multi-cellular organism
//                              "UBERON:0010000", //multicellular anatomical structure
//                              "UBERON:0003103", //compound organ
//                              "UBERON:0000062", //organ
//                              "UBERON:0000489"), //cavitated compound organ
//                
//                Arrays.asList("grouping_class", "non_informative", "ubprop:upper_level", 
//                        "upper_level"));
        
        OntologyUtils utils = new OntologyUtils(manipulator.getOwlGraphWrapper());
        utils.removeOBOProblematicAxioms();
        
        log.exit();
    }
    
    public void getSimplificationInfo() {
        
    }
    
    /**
     * Extract from the Uberon ontology all NCBI taxon IDs that are the targets 
     * of {@code OWLRestriction}s over the object properties "in taxon" (or any 
     * sub-properties), or that are used in ontology annotations 
     * "treat-xrefs-as-reverse-genus-differentia", and to write them in a file.
     * The IDs used are {@code Integer}s that are the NCBI IDs (for instance, 
     * 9606 for human), not the ontology IDs with a prefix ("NCBITaxon:").
     * 
     * @param uberonFile    A {@code String} that is the path to the Uberon ontology file.
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
    public void extractTaxonIds(String uberonFile, String outputFile) 
            throws OWLOntologyCreationException, OBOFormatParserException, 
            IllegalArgumentException, IOException {
        log.entry(uberonFile, outputFile);
        
        Set<Integer> taxonIds = this.extractTaxonIds(uberonFile);
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
     * @param uberonFile    A {@code String} that is the path to the Uberon ontology file.
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
    public Set<Integer> extractTaxonIds(String uberonFile) 
            throws OWLOntologyCreationException, OBOFormatParserException, 
            IOException, IllegalArgumentException {
        log.entry(uberonFile);

        OWLOntology ont = OntologyUtils.loadOntology(uberonFile);
        OWLGraphWrapper wrapper = new OWLGraphWrapper(ont);
        
        Set<String> taxonIds = new HashSet<String>();
        
        //will get taxon IDs from axioms over object properties "in_taxon", 
        //"evolved_multiple_times_in" (or any sub-properties)
        Set<OWLObjectPropertyExpression> objectProps = this.getTaxonObjectProperties(wrapper);
        //will also get the taxon IDs from annotation axioms over annotation properties
        //"ambiguous_for_taxon", "dubious_for_taxon", "homologous_in","never_in_taxon", 
        //"RO:0002161", "present_in_taxon", "taxon" (or any sub-properties)
        Set<OWLAnnotationProperty> annotProps = this.getTaxonAnnotationProperties(wrapper);
        
        for (OWLClass cls: ont.getClassesInSignature()) {
            //try to get taxa from any object properties that can lead to a taxon
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
        
        
        if (taxonIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("The provided ontology " +
                    " did not allow to acquire any taxon ID"));
        }
        return log.exit(OntologyUtils.convertToNcbiIds(taxonIds));
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
