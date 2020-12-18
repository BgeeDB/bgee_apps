package org.bgee.pipeline.ontologycommon;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO;
import org.bgee.model.dao.api.anatdev.TaxonConstraintDAO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.ontologycommon.RelationDAO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.pipeline.CommandRunner;
import org.bgee.pipeline.ontologycommon.OntologyUtils.PipelineRelationTO;
import org.bgee.pipeline.uberon.Uberon;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;

import com.google.common.base.Objects;

import owltools.graph.OWLGraphEdge;
import owltools.graph.OWLGraphWrapper;

/**
 * Class responsible for various generic operations on ontologies.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
//TODO: merge this class with OntologyUtils
public class OntologyTools {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(OntologyTools.class.getName());
    
    /**
     * Default constructor. 
     */
    public OntologyTools() {
        
    }
    
    /**
     * Main method to trigger various generic operations on any ontology. 
     * Parameters that must be provided in order in {@code args} are: 
     * <ul>
     * <li>If the first element in {@code args} is {@code ExtractObsoleteIds}, 
     * the action will be to retrieve obsolete IDs from an ontology, 
     * and to write them into an output file, see 
     * {@link #writeObsoletedTermsToFile(String, String)}. 
     * Following elements in {@code args} must then be: 
     *   <ol>
     *     <li>path to the file storing the ontology, either in OBO or in OWL.
     *     <li>path to the file where to store the list of IDs of obsolete terms.
     *   </ol>
     * </li>
     * <li>If the first element in {@code args} is {@code ExtractAllIds}, 
     * the action will be to retrieve IDs of "real" {@code OWLClass} from an ontology, 
     * and to write them into an output file, see 
     * {@link #writeOWLClassIdsToFile(String, String)}. 
     * Following elements in {@code args} must then be: 
     *   <ol>
     *     <li>path to the file storing the ontology, either in OBO or in OWL.
     *     <li>path to the file where to store the list of IDs.
     *   </ol>
     * </li>
     * <li>If the first element in {@code args} is {@code RetrieveAnatIncorrectIndirectRels}, 
     * the action will be to retrieve from the database indirect relations that cannot be reached
     * through a chain of direct relations, to find explanations from the Uberon ontology,
     * and to write them into an output file, see 
     * {@link #getFromDBAnatPartOfIsAIndirectRelsNotReachedByChainOfDirectRelsAndWriteToFile(
     *      DAOManager, Uberon, String)}. 
     * Following elements in {@code args} must then be: 
     *   <ol>
     *     <li>path to the Uberon ontology.
     *     <li>a boolean defining, when {@code true}, to filter the retrieval of
     *     incorrect indirect relations to display only the non-redundant. It is useful when examining
     *     the relations, but should not be used to generate the fixes to apply to the database.
     *     <li>path to the file where to store the list of relations.
     *   </ol>
     * </li>
     * </ul>
     * 
     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
     * @throws FileNotFoundException        If some files could not be found.
     * @throws IOException                  If some files could not be used.
     * @throws OWLOntologyCreationException If an error occurred while loading 
     *                                      the ontology.
     * @throws OBOFormatParserException     If an error occurred while loading 
     *                                      the ontology.
     * @throws IllegalArgumentException     If the files used provided invalid information.
     * @throws DAOException                 If an error occurred while inserting 
     *                                      the data into the Bgee database.
     */
    public static void main(String[] args) throws FileNotFoundException, 
        OWLOntologyCreationException, OBOFormatParserException, IllegalArgumentException, 
        DAOException, IOException {
        log.entry((Object[]) args);
        
        OntologyTools tools = new OntologyTools();
        if (args[0].equalsIgnoreCase("ExtractObsoleteIds")) {
            if (args.length != 3) {
                throw log.throwing(new IllegalArgumentException(
                        "Incorrect number of arguments provided, expected " + 
                        "3 arguments, " + args.length + " provided."));
            }
            tools.writeObsoletedTermsToFile(args[1], args[2]);
        } else if (args[0].equalsIgnoreCase("ExtractAllIds")) {
            if (args.length != 3) {
                throw log.throwing(new IllegalArgumentException(
                        "Incorrect number of arguments provided, expected " + 
                        "3 arguments, " + args.length + " provided."));
            }
            tools.writeOWLClassIdsToFile(args[1], args[2]);
        } else if (args[0].equalsIgnoreCase("RetrieveAnatIncorrectIndirectRels")) {
            if (args.length != 4) {
                throw log.throwing(new IllegalArgumentException(
                        "Incorrect number of arguments provided, expected " +
                        "4 arguments, " + args.length + " provided."));
            }
            tools.getFromDBAnatPartOfIsAIndirectRelsNotReachedByChainOfDirectRelsAndWriteToFile(
                    DAOManager.getDAOManager(), new Uberon(args[1]),
                    CommandRunner.parseArgumentAsBoolean(args[2]), args[3]);
        } else {
            throw log.throwing(new IllegalArgumentException("Unrecognized command " + 
                args[0]));
        }
        
        log.exit();
    }
    
    /**
     * Extract the OBO-like IDs of obsoleted terms from the provided ontology, stored 
     * in the file {@code ontFile}, and write them into the file {@code obsIdsFile}, 
     * one ID per line.
     * 
     * @param ontFile       A {@code String} that is the path to the file storing 
     *                      the ontology, in OBO or OWL.
     * @param obsIdsFile    A {@code String} that is the path to the file where to write 
     *                      the obsolete IDs.
     * @throws UnknownOWLOntologyException      If the ontology could not be loaded.
     * @throws OWLOntologyCreationException     If the ontology could not be loaded.
     * @throws OBOFormatParserException         If the ontology file could not be parsed.
     * @throws IOException                      If the ontology file coud not be read, 
     *                                          or output file could not be written. 
     */
    public void writeObsoletedTermsToFile(String ontFile, String obsIdsFile) 
            throws UnknownOWLOntologyException, OWLOntologyCreationException, 
            OBOFormatParserException, IOException {
        log.entry(ontFile, obsIdsFile);
        
        Set<String> obsoleteIds = this.getObsoleteIds(ontFile);
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
                obsIdsFile)))) {
            for (String obsoleteId: obsoleteIds) {
                out.println(obsoleteId);
            }
        }
        
        log.exit();
    }
    
    /**
     * Extract the OBO-like IDs of obsoleted terms from the provided ontology, stored 
     * in the file {@code ontFile}.
     * 
     * @param ontFile   A {@code String} that is the path to the file storing an ontology, 
     *                  in OBO or OWL.
     * @return          A {@code Set} of {@code String}s that are the OBO-like IDs of deprecated 
     *                  terms (for instance, 'GO:0000005').
     * @throws UnknownOWLOntologyException      If the ontology could not be loaded.
     * @throws OWLOntologyCreationException     If the ontology could not be loaded.
     * @throws OBOFormatParserException         If the ontology file could not be parsed.
     * @throws IOException                      If the ontology file coud not be read. 
     * @see #getObsoleteIds(OWLOntology)
     */
    public Set<String> getObsoleteIds(String ontFile) throws UnknownOWLOntologyException, 
        OWLOntologyCreationException, OBOFormatParserException, IOException {
        log.entry(ontFile);
        
        return log.exit(this.getObsoleteIds(OntologyUtils.loadOntology(ontFile)));
    }
    
    /**
     * Extract the OBO-like IDs of obsoleted terms from {@code ont}.
     * 
     * @param ont   An {@code OWLOntology} storing an ontology.
     * @return      A {@code Set} of {@code String}s that are the 
     *              OBO-like IDs of deprecated terms (for instance, 
     *              'GO:0000005').
     * @throws UnknownOWLOntologyException  If the ontology could not be loaded.
     */
    public Set<String> getObsoleteIds(OWLOntology ont) 
            throws UnknownOWLOntologyException, IOException {
        log.entry(ont);
        Set<String> obsoleteIds = new HashSet<String>();
        
        try (OWLGraphWrapper goWrapper = new OWLGraphWrapper(ont)) {
            for (OWLClass goTerm: goWrapper.getAllOWLClasses()) {
                if (goWrapper.isOboAltId(goTerm)) {
                    continue;
                }
                if (goWrapper.isObsolete(goTerm) || goWrapper.getIsObsolete(goTerm)) {
                    obsoleteIds.add(goWrapper.getIdentifier(goTerm));
                    obsoleteIds.addAll(goWrapper.getAltIds(goTerm));
                }
            }
        }
        
        return log.exit(obsoleteIds);
    }
    
    /**
     * Same method as {@link #getAllRealOWLClassIds(OWLGraphWrapper)}, except 
     * that it is the path to the ontology file that is provided, rather than 
     * an ontology already loaded into an {@code OWLGraphWrapper}. 
     * 
     * @param ontFile   A {@code String} that is the path to the ontology file.
     * @return          A {@code Set} of {@code String}s that are the OBO-like IDs 
     *                  of the "real" {@code OWLClass}es present in the ontology 
     *                  (see {@link #getAllRealOWLClassIds(OWLGraphWrapper)} for details).
     * @throws UnknownOWLOntologyException      If an error occurred while 
     *                                          loading the ontology.
     * @throws OWLOntologyCreationException     If an error occurred while 
     *                                          loading the ontology.
     * @throws OBOFormatParserException         If an error occurred while 
     *                                          loading the ontology.
     * @throws IOException                      If an error occurred while 
     *                                          loading the ontology.
     * @see #getAllRealOWLClassIds(OWLGraphWrapper)
     */
    public Set<String> getAllRealOWLClassIds(String ontFile) 
            throws UnknownOWLOntologyException, OWLOntologyCreationException, 
            OBOFormatParserException, IOException {
        log.entry(ontFile);
        
        return log.exit(this.getAllRealOWLClassIds(
                new OWLGraphWrapper(OntologyUtils.loadOntology(ontFile))));
    }
    
    /**
     * Returns the OBO-like IDs of all the "real" {@code OWLClass}es present 
     * in the ontology wrapped into {@code ontWrapper} (meaning, {@code OWLClass}es 
     * neither top entity (owl:thing), nor bottom entity (owl:nothing), 
     * nor deprecated). 
     * 
     * @param ontWrapper    A {@code OWLGraphWrapper} wrapping the {@code OWLOntology} 
     *                      for which we want class IDs.
     * @return              A {@code Set} of {@code String}s that are the OBO-like IDs 
     *                      of the "real" {@code OWLClass}es present in {@code ontWrapper}.
     * @see #getAllRealOWLClassIds(String)
     */
    public Set<String> getAllRealOWLClassIds(OWLGraphWrapper ontWrapper) {
        log.entry(ontWrapper);
        Set<String> allIds = new HashSet<String>();
        
        for (OWLClass owlClass: ontWrapper.getAllRealOWLClasses()) {
            allIds.add(ontWrapper.getIdentifier(owlClass));
        }
        
        return log.exit(allIds);
    }
    
    /**
     * Extract the OBO-like IDs of all "real" {@code OWLClass}es present 
     * in the ontology provided through {@code ontFile}, and write them 
     * into the file {@code outputFile}, one ID per line. 
     * See {@link #getAllRealOWLClassIds(OWLGraphWrapper)} for more details.
     * 
     * @param ontFile       A {@code String} that is the path to the file storing 
     *                      the ontology, in OBO or OWL.
     * @param outputFile    A {@code String} that is the path to the file where to write 
     *                      the IDs.
     * @throws UnknownOWLOntologyException      If the ontology could not be loaded.
     * @throws OWLOntologyCreationException     If the ontology could not be loaded.
     * @throws OBOFormatParserException         If the ontology file could not be parsed.
     * @throws IOException                      If the ontology file coud not be read, 
     *                                          or output file could not be written. 
     * @see #getAllRealOWLClassIds(OWLGraphWrapper)
     */
    public void writeOWLClassIdsToFile(String ontFile, String outputFile) 
            throws UnknownOWLOntologyException, OWLOntologyCreationException, 
            OBOFormatParserException, IOException {
        log.entry(ontFile, outputFile);
        
        Set<String> ids = this.getAllRealOWLClassIds(ontFile);
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
                outputFile)))) {
            for (String id: ids) {
                out.println(id);
            }
        }
        
        log.exit();
    }

    public void getFromDBAnatPartOfIsAIndirectRelsNotReachedByChainOfDirectRelsAndWriteToFile(
            DAOManager daoManager, Uberon uberon, boolean filterRels, String outputFile) throws IOException {
        log.entry(daoManager, uberon, filterRels, outputFile);

        RelationDAO relDAO = daoManager.getRelationDAO();
        AnatEntityDAO anatEntityDAO = daoManager.getAnatEntityDAO();
        TaxonConstraintDAO taxonConstraintDAO = daoManager.getTaxonConstraintDAO();

        //We go species by species, it will be simpler,
        //but we will summarize as much as possible over all species afterwards
        Set<SpeciesTO> allSpeciesTOs = new HashSet<>(
                daoManager.getSpeciesDAO().getAllSpecies(null).getAllTOs());

        //We retrieve all direct relations, all taxon constraints of all relations, and the max relation ID,
        //to generate the SQL commands to fix the issues identified
        //(rather than directly modifying the database with no review)
        Set<RelationTO<String>> allDirectRels = new HashSet<>(relDAO.getAnatEntityRelations(
                null, true, null, null, true,
                EnumSet.of(RelationTO.RelationType.ISA_PARTOF),
                EnumSet.of(RelationTO.RelationStatus.DIRECT),
                EnumSet.of(RelationDAO.Attribute.RELATION_ID,
                        RelationDAO.Attribute.SOURCE_ID, RelationDAO.Attribute.TARGET_ID))
                .getAllTOs());
        Map<Integer, Set<Integer>> allRelTaxonConstraints = taxonConstraintDAO
                .getAnatEntityRelationTaxonConstraints(null, null).stream()
                .map(tcTO -> new AbstractMap.SimpleEntry<>(tcTO.getEntityId(),
                        new HashSet<>(Arrays.asList(tcTO.getSpeciesId()))))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(),
                        (v1, v2) -> {v1.addAll(v2); return v1;}));
        int maxRelationId = relDAO.getAnatEntityRelations(
                null, true, null, null, true, null, null,
                EnumSet.of(RelationDAO.Attribute.RELATION_ID))
                .stream().mapToInt(relTO -> relTO.getId())
                .max().getAsInt();

        //Get all the anat. entity IDs existing in any species
        Set<String> allAnatEntityIds = anatEntityDAO.getAnatEntities(null, true, null,
                EnumSet.of(AnatEntityDAO.Attribute.ID))
                .stream()
                .map(aeTO -> aeTO.getId())
                .collect(Collectors.toSet());

        //TOs don't implement equals/hashCode, so we'll use relation IDs and need a Map
        //to find back the RelationTO associated to each ID
        Map<Integer, RelationTO<String>> relationTOIdMap = new HashMap<>();
        //We'll associate IDs of incorrect relations to a Set containing the species
        //the relation is incorrect in.
        Map<Integer, Set<SpeciesTO>> speciesPerIncorrectIndirectRelId = new HashMap<>();
        //We'll also store the relations where the fix could cause a cycle, to delete completely
        //the incorrect indirect relation for the species involved.
        Map<Integer, Set<SpeciesTO>> speciesPerPotentialCycle = new HashMap<>();
        //We'll also store the relations where no fix could be produced because of classes
        //not existing in the species, to delete completely the incorrect indirect relation
        //for the species involved.
        Map<Integer, Set<SpeciesTO>> speciesPerNotExistingClasses = new HashMap<>();
        //We'll associate the newly created relations to fix the issues to a Set containing the species
        //the newly created relation can be applied in.
        Map<PipelineRelationTO<String>, Set<SpeciesTO>> speciesPerRelTOToAdd = new HashMap<>();
        //To provide more information about the issues, we'll associate the incorrect relation IDs
        //in the Bgee database to the corresponding OWLGraphEdges retrieved from the ontology, if any.
        Map<Integer, Set<OWLGraphEdge>> owlGraphEdgesPerIncorrectIndirectRelId = new HashMap<>();
        //We'll also associate the incorrect relations to the newly created relations that will fix them
        Map<Integer, Set<PipelineRelationTO<String>>> newRelTOsPerIncorrectIndirectRelId = new HashMap<>();
 
        //OK, now we have everything, let's go for surgical strikes to fix the issues,
        //spcies by species.
        Iterator<SpeciesTO> speciesTOIterator = allSpeciesTOs.iterator();
        while (speciesTOIterator.hasNext()) {
            SpeciesTO speciesTO = speciesTOIterator.next();
            Collection<Integer> speciesId = Collections.singleton(speciesTO.getId());

            //Get the anat. entity IDs valid in this species
            Set<String> anatEntityIdsForSpecies = anatEntityDAO.getAnatEntities(speciesId, true, null,
                    EnumSet.of(AnatEntityDAO.Attribute.ID))
                    .stream()
                    .map(aeTO -> aeTO.getId())
                    .collect(Collectors.toSet());
            //Get the direct relations valid in this species, put them also in a convenience Map
            Set<RelationTO<String>> directRels = new HashSet<>(relDAO.getAnatEntityRelations(
                    speciesId, true, null, null, true,
                    EnumSet.of(RelationTO.RelationType.ISA_PARTOF),
                    EnumSet.of(RelationTO.RelationStatus.DIRECT),
                    EnumSet.of(RelationDAO.Attribute.SOURCE_ID, RelationDAO.Attribute.TARGET_ID))
                    .getAllTOs());
            //put them also in a convenience Map
            Map<String, Set<String>> directRelsSourceToTargets = sourceToTargetsOrTargetToSources(
                    directRels, true);
            //Get the indirect relations valid in this species
            Set<RelationTO<String>> indirectRels = new HashSet<>(relDAO.getAnatEntityRelations(
                    speciesId, true, null, null, true,
                    EnumSet.of(RelationTO.RelationType.ISA_PARTOF),
                    EnumSet.of(RelationTO.RelationStatus.INDIRECT),
                    null)
                    .getAllTOs());
            //And we also store direct and indirect rels in a same Set
            Set<RelationTO<String>> allRels = new HashSet<>(directRels);
            allRels.addAll(indirectRels);
            //put them also in a convenience Map
            Map<String, Set<String>> allRelsSourceToTargets = sourceToTargetsOrTargetToSources(
                    allRels, true);

            //Now we find the incorrect indirect relations
            Set<RelationTO<String>> incorrectIndirectRelTOs =
                    findIndirectRelsNotReachedByChainOfDirectRels(directRels, indirectRels);
            log.info("{} indirect incorrect relations for species {}",
                    incorrectIndirectRelTOs.size(), speciesTO.getId());
            if (filterRels) {
                //We try to make these incorrect indirect rels non-redundant
                incorrectIndirectRelTOs = retainMostPreciseIndirectRelsNotReachedByChainOfDirectRels(
                                allRels, incorrectIndirectRelTOs);
                log.info("{} after filtering", incorrectIndirectRelTOs.size());
            }

            //Now we store the incorrect indirect rels over multiple species and try to find fixes
            for (RelationTO<String> incorrectIndirectRelTO: incorrectIndirectRelTOs) {
                relationTOIdMap.put(incorrectIndirectRelTO.getId(), incorrectIndirectRelTO);

                //We retrieve the edges directly from Uberon to try to find fixes
                Map<Boolean, Set<OWLGraphEdge>> outgoingEdges =
                        uberon.getValidOutgoingEdgesFromOWLClassIds(incorrectIndirectRelTO.getSourceId(),
                        incorrectIndirectRelTO.getTargetId(), speciesId);
                Set<OWLGraphEdge> directEdges = outgoingEdges.get(true).stream()
                        .filter(e -> uberon.getOntologyUtils().isASubClassOfEdge(e) || 
                                uberon.getOntologyUtils().isPartOfRelation(e))
                        .collect(Collectors.toSet());
                Set<OWLGraphEdge> indirectEdges = outgoingEdges.get(false).stream()
                        .filter(e -> uberon.getOntologyUtils().isASubClassOfEdge(e) || 
                                uberon.getOntologyUtils().isPartOfRelation(e))
                        .collect(Collectors.toSet());
                Set<OWLGraphEdge> allEdges = new HashSet<>(directEdges);
                allEdges.addAll(indirectEdges);
                //We store all edges associated to an incorrect indirect rel for logging purpose
                owlGraphEdgesPerIncorrectIndirectRelId.merge(incorrectIndirectRelTO.getId(),
                        allEdges,
                        (existingEdges, newEdges) -> {
                            existingEdges.addAll(newEdges);
                            return existingEdges;
                        });
                //Will store the new relations to create to fix the incorrect indirect rel
                Set<PipelineRelationTO<String>> relTOsToAdd = new HashSet<>();
                boolean potentiallyIncludeACycle = false;
                boolean includeClassesNotExistingInSpecies = false;

                //OK, we iterate each indirect outgoing edge corresponding to
                //this incorrect indirect relation
                edge: for (OWLGraphEdge e: indirectEdges) {
                    Set<PipelineRelationTO<String>> newRelsToAdd = new HashSet<>();
                    String composedRelSuperClassId = null;
                    String composedRelSubClassId = null;
                    //We go axiom by axiom to find THE one missing direct relation,
                    //rather than blankly making all incorrect indirect rels direct.
                    //We might also try to compose a new relation over axioms going through
                    //a class not existing in Bgee (why, I don't know). See
                    //composedRelSubClassId and composedRelSuperClassId above.
                    for (OWLSubClassOfAxiom ax: e.getSubClassOfAxioms()) {
                        //We extract the OWLClasses from the OWLClassExpressions
                        Set<OWLClass> subClsSet = ax.getSubClass()
                                .getClassesInSignature().stream()
                                .filter(cls -> uberon.isValidClass(cls, new HashSet<>(),
                                        speciesId) && !uberon.isTaxonomyClass(cls))
                                .collect(Collectors.toSet());
                        Set<OWLClass> superClsSet = ax.getSuperClass()
                                .getClassesInSignature().stream()
                                .filter(cls -> uberon.isValidClass(cls, new HashSet<>(),
                                        speciesId) && !uberon.isTaxonomyClass(cls))
                                .collect(Collectors.toSet());
                        if (subClsSet.size() == 1 && superClsSet.size() == 1) {
                            String sourceId = uberon.getOntologyUtils().getWrapper()
                                    .getIdentifier(subClsSet.iterator().next());
                            String targetId = uberon.getOntologyUtils().getWrapper()
                                    .getIdentifier(superClsSet.iterator().next());
                            //If the axiom could cause a cycle as compared to the relations
                            //in the database, we don't use it and we tag the relation
                            //to investigate further
                            if (allRelsSourceToTargets.containsKey(targetId) &&
                                    allRelsSourceToTargets.get(targetId).contains(sourceId)) {
                                potentiallyIncludeACycle = true;
                                continue edge;
                            }
                            
                            if (//If the rel does not exist in the requested species
                                    (!directRelsSourceToTargets.containsKey(sourceId) ||
                                            !directRelsSourceToTargets.get(sourceId)
                                            .contains(targetId)) &&
                                    //and if it is not a reflexive relation
                                    !sourceId.equals(targetId)) {
                                
                                //if the anat. entities do exists in the requested species,
                                //we just have to suggest a new relation
                                if (anatEntityIdsForSpecies.contains(sourceId) &&
                                        anatEntityIdsForSpecies.contains(targetId)) {
                                    //NEW RELATION TO CONTRIBUTE FIXING THE ISSUE
                                    newRelsToAdd.add(new PipelineRelationTO<String>(
                                            sourceId, targetId,
                                            RelationTO.RelationType.ISA_PARTOF,
                                            RelationTO.RelationStatus.DIRECT));
                                }
                                //Otherwise, if an anat. entity does not exist in Bgee AT ALL
                                //(bug, incorrect modif?), we'll try to compose
                                //a new relation over terms that do exit
                                else if (!allAnatEntityIds.contains(sourceId) &&
                                        anatEntityIdsForSpecies.contains(targetId) &&
                                        composedRelSuperClassId == null) {
                                    composedRelSuperClassId = targetId;
                                } else if (!allAnatEntityIds.contains(targetId) &&
                                        anatEntityIdsForSpecies.contains(sourceId) &&
                                        composedRelSubClassId == null) {
                                    composedRelSubClassId = sourceId;
                                //Otherwise, if it doesn't exist in this species, but in other species,
                                //we tag the relation to know why we couldn't produce a fix.
                                } else {
                                    includeClassesNotExistingInSpecies = true;
                                }
                            }
                        } else {
                            log.warn("Could not generate a RelationTO from axiom: {} - "
                                    + "SubClassSet: {} - SuperClassSet: {}", ax, subClsSet,
                                    superClsSet);
                        }
                    }
                    //If we could compose a new relation over anat. entities not existing
                    if (composedRelSuperClassId != null && composedRelSubClassId != null) {
                        //We check that indeed there exists a valid edge between these two classes,
                        //that does not exist in the Bgee database
                        Map<Boolean, Set<OWLGraphEdge>> testEdges =
                                uberon.getValidOutgoingEdgesFromOWLClassIds(composedRelSubClassId,
                                        composedRelSuperClassId, speciesId);
                        if ((testEdges.get(false).stream().anyMatch(testEdge ->
                                uberon.getOntologyUtils().isASubClassOfEdge(testEdge) ||
                                uberon.getOntologyUtils().isPartOfRelation(testEdge)) ||
                             testEdges.get(true).stream().anyMatch(testEdge ->
                                uberon.getOntologyUtils().isASubClassOfEdge(testEdge) ||
                                uberon.getOntologyUtils().isPartOfRelation(testEdge))) &&
                             (!directRelsSourceToTargets.containsKey(composedRelSubClassId) ||
                                        !directRelsSourceToTargets.get(composedRelSubClassId)
                                        .contains(composedRelSuperClassId)) &&
                                //and if it is not a reflexive relation
                                !composedRelSubClassId.equals(composedRelSuperClassId)) {
                            
                            //If the axiom could cause a cycle as compared to the relations
                            //in the database, we don't use it and we tag the relation
                            //to investigate further
                            if (allRelsSourceToTargets.containsKey(composedRelSuperClassId) &&
                                    allRelsSourceToTargets.get(composedRelSuperClassId)
                                    .contains(composedRelSubClassId)) {
                                potentiallyIncludeACycle = true;
                                continue edge;
                            }
                            //NEW RELATION TO CONTRIBUTE FIXING THE ISSUE
                            newRelsToAdd.add(new PipelineRelationTO<String>(
                                    composedRelSubClassId, composedRelSuperClassId,
                                    RelationTO.RelationType.ISA_PARTOF,
                                    RelationTO.RelationStatus.DIRECT));
                        }
                    }
                    //Will be added only if we haven't seen a potential cycle
                    relTOsToAdd.addAll(newRelsToAdd);
                }

                edge: for (OWLGraphEdge e: directEdges) {
                    if (//If the rel does not exist in the requested species
                        (!directRelsSourceToTargets.containsKey(e.getSourceId()) ||
                                !directRelsSourceToTargets.get(e.getSourceId()).contains(e.getTargetId())) &&
                        //but the anat. entities do exists in the requested species
                        anatEntityIdsForSpecies.contains(e.getSourceId()) && anatEntityIdsForSpecies.contains(e.getTargetId()) &&
                        //and if is not a reflexive relation
                        !e.getSourceId().equals(e.getTargetId())) {
                        //If the edge could cause a cycle as compared to the relations
                        //in the database, we don't use it and we tag the relation
                        //to investigate further
                        if (allRelsSourceToTargets.containsKey(e.getTargetId()) &&
                                allRelsSourceToTargets.get(e.getTargetId()).contains(e.getSourceId())) {
                            potentiallyIncludeACycle = true;
                            continue edge;
                        }
                        //NEW RELATION TO CONTRIBUTE FIXING THE ISSUE
                        relTOsToAdd.add(new PipelineRelationTO<>(
                                e.getSourceId(), e.getTargetId(),
                                RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT));
                    } else if (!anatEntityIdsForSpecies.contains(e.getSourceId()) ||
                            !anatEntityIdsForSpecies.contains(e.getTargetId())) {
                        includeClassesNotExistingInSpecies = true;
                    }
                }

                //If there are multiple relations to add, we discard the ones corresponding to
                //exactly the incorrect indirect rel to fix, to keep only the longest path
                if (relTOsToAdd.size() > 1) {
                    relTOsToAdd = relTOsToAdd.stream()
                        .filter(relTO -> !incorrectIndirectRelTO.getSourceId().equals(relTO.getSourceId()) ||
                                !incorrectIndirectRelTO.getTargetId().equals(relTO.getTargetId()))
                        .collect(Collectors.toSet());
                }
                //We check once again that the new relation could be retrieved from Uberon,
                //notably for the part where we iterate OWLSubClassAxioms one by one
                relTOsToAdd = relTOsToAdd.stream()
                        .filter(relTO -> {
                            Map<Boolean, Set<OWLGraphEdge>> testEdges =
                                    uberon.getValidOutgoingEdgesFromOWLClassIds(relTO.getSourceId(),
                                            relTO.getTargetId(), speciesId);
                            return testEdges.get(false).stream().anyMatch(testEdge ->
                                    uberon.getOntologyUtils().isASubClassOfEdge(testEdge) ||
                                    uberon.getOntologyUtils().isPartOfRelation(testEdge)) ||
                                   testEdges.get(true).stream().anyMatch(testEdge ->
                                    uberon.getOntologyUtils().isASubClassOfEdge(testEdge) ||
                                    uberon.getOntologyUtils().isPartOfRelation(testEdge));
                        })
                        .collect(Collectors.toSet());

                //we filter relations where the target has the target of another relation as target
                //(redundant shorter path)
                Map<String, Set<String>> relTOsToAddSourceToTargets = sourceToTargetsOrTargetToSources(
                        relTOsToAdd, true);
                relTOsToAdd = relTOsToAdd.stream()
                        .filter(relTO -> {
                            Set<String> sameSourceTargets = relTOsToAddSourceToTargets.get(
                                    relTO.getSourceId());
                            sameSourceTargets.remove(relTO.getTargetId());
                            Set<String> targetTargets = allRelsSourceToTargets.get(relTO.getTargetId());
                            return targetTargets == null ||
                                    Collections.disjoint(sameSourceTargets, targetTargets);
                        })
                        .collect(Collectors.toSet());

                if (relTOsToAdd.size() > 1) {
                    log.warn("Multiple new relations for one incorrectIndirectRelTO. "
                            + "incorrectIndirectRelTO: {} - Direct OWLGraphEdges: {} - "
                            + "Indirect OWLGraphEdges: {} - RelationTOs to add: {}",
                            incorrectIndirectRelTO, directEdges, indirectEdges, relTOsToAdd);
                } else if (relTOsToAdd.isEmpty() && !potentiallyIncludeACycle && !includeClassesNotExistingInSpecies) {
                    log.warn("No new relations produced from an incorrectIndirectRelTO. "
                            + "incorrectIndirectRelTO: {} - Direct OWLGraphEdges: {} - "
                            + "Indirect OWLGraphEdges: {}",
                            incorrectIndirectRelTO, directEdges, indirectEdges);
                }

                //We merge the species IDs this indirect relation is incorrect in
                speciesPerIncorrectIndirectRelId.merge(incorrectIndirectRelTO.getId(),
                        new HashSet<>(Arrays.asList(speciesTO)),
                        (existingSpeciesTOs, newSpeciesTOs) -> {
                            existingSpeciesTOs.addAll(newSpeciesTOs);
                            return existingSpeciesTOs;
                        });
                //We merge the potential fixes to this indirect relation over several species
                newRelTOsPerIncorrectIndirectRelId.put(incorrectIndirectRelTO.getId(), relTOsToAdd);
                for (PipelineRelationTO<String> relToAdd: relTOsToAdd) {
                    speciesPerRelTOToAdd.merge(relToAdd,
                        new HashSet<>(Arrays.asList(speciesTO)),
                        (existingSpeciesTOs, newSpeciesTOs) -> {
                            existingSpeciesTOs.addAll(newSpeciesTOs);
                            return existingSpeciesTOs;
                        });
                }
                //We store the relations where the fix might cause a cycle,
                //to completely delete them in the species involved.
                if (relTOsToAdd.isEmpty() && potentiallyIncludeACycle) {
                    speciesPerPotentialCycle.merge(incorrectIndirectRelTO.getId(),
                            new HashSet<>(Arrays.asList(speciesTO)),
                            (existingSpeciesTOs, newSpeciesTOs) -> {
                                existingSpeciesTOs.addAll(newSpeciesTOs);
                                return existingSpeciesTOs;
                            });
                }
                //We store the relations with no fix because of classes not existing in the species,
                //to completely delete them in the species involved.
                if (relTOsToAdd.isEmpty() && includeClassesNotExistingInSpecies) {
                    speciesPerNotExistingClasses.merge(incorrectIndirectRelTO.getId(),
                            new HashSet<>(Arrays.asList(speciesTO)),
                            (existingSpeciesTOs, newSpeciesTOs) -> {
                                existingSpeciesTOs.addAll(newSpeciesTOs);
                                return existingSpeciesTOs;
                            });
                }
            }
        }

        //To know if there are species in which the indirect relations can be retrieved
        //through a chain of direct relations
        Map<Integer, Set<Integer>> taxonConstraintsPerIncorrectIndirectRelId = taxonConstraintDAO
                .getAnatEntityRelationTaxonConstraints(null, relationTOIdMap.keySet(), null)
                .stream().map(tcTO -> new AbstractMap.SimpleEntry<>(tcTO.getEntityId(),
                        new HashSet<>(Arrays.asList(tcTO.getSpeciesId()))))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(),
                        (v1, v2) -> {v1.addAll(v2); return v1;}));
        Set<Integer> allSpeciesIds = allSpeciesTOs.stream().map(sTO -> sTO.getId())
                .collect(Collectors.toSet());

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
                outputFile)))) {
            speciesPerIncorrectIndirectRelId.entrySet().stream()
            .sorted(Comparator
                    .<Entry<Integer, Set<SpeciesTO>>>comparingInt(e -> e.getValue().size()).reversed()
                    .thenComparing(e -> e.getKey()))
            .forEach(e -> {
                Set<Integer> invalidSpeciesIds = e.getValue().stream().map(sTO -> sTO.getId())
                        .collect(Collectors.toSet());
                Set<Integer> taxonConstraintsForRel = taxonConstraintsPerIncorrectIndirectRelId.get(e.getKey());
                boolean taxonConstraintAllSpecies = taxonConstraintsForRel.contains(null);
                Set<Integer> validSpeciesIds = allSpeciesIds.stream()
                        .filter(speId -> !invalidSpeciesIds.contains(speId) &&
                                (taxonConstraintAllSpecies || taxonConstraintsForRel.contains(speId)))
                        .collect(Collectors.toSet());


                out.print(relationTOIdMap.get(e.getKey())
                        + " - incorrect for species (" + invalidSpeciesIds.size() + "): "
                        + invalidSpeciesIds.stream().map(speId -> speId.toString())
                            .collect(Collectors.joining(", "))
                        + " - valid for species (" + validSpeciesIds.size() + "): "
                        + validSpeciesIds.stream().map(speId -> speId == null? "All species": speId.toString())
                            .collect(Collectors.joining(", "))
                        );
                Set<OWLGraphEdge> correspondingEdges = owlGraphEdgesPerIncorrectIndirectRelId.get(e.getKey());
                if (correspondingEdges == null || correspondingEdges.isEmpty()) {
                    out.print(" - No corresponding OWLGraphEdges found");
                } else {
                    out.print(" - Corresponding OWLGraphEdges found: ");
                    out.print(correspondingEdges.stream().map(edge -> edge.toString())
                            .collect(Collectors.joining(" - ")));
                }
                if (speciesPerPotentialCycle.containsKey(e.getKey())) {
                    out.print(" - Some of the fix could cause a cycle in species: " +
                    speciesPerPotentialCycle.get(e.getKey())
                    .stream().map(speTO -> speTO.getId().toString()).collect(Collectors.joining(", ")));
                }
                if (speciesPerNotExistingClasses.containsKey(e.getKey())) {
                    out.print(" - No fix because of classes not existing in the species: " +
                    speciesPerNotExistingClasses.get(e.getKey())
                    .stream().map(speTO -> speTO.getId().toString()).collect(Collectors.joining(", ")));
                }
                Set<PipelineRelationTO<String>> relTOsToAdd = newRelTOsPerIncorrectIndirectRelId.get(e.getKey());
                if (relTOsToAdd.isEmpty()) {
                    out.println(" - No new RelationTO to fix issue");
                } else {
                    out.print(" - New RelationTOs to fix issues: ");
                    out.print(relTOsToAdd.stream().map(relTO -> relTO.toString())
                            .collect(Collectors.joining(" - ")));
                }
                out.println();
                
            });

            //Now we print the SQL commands to fix the issues (rather than modifying directly
            //the database with no review)
            out.println();
            out.println("FIXES:");
            out.println();

            for (Entry<PipelineRelationTO<String>, Set<SpeciesTO>> e: speciesPerRelTOToAdd.entrySet()) {
                RelationTO<String> newDirectRelTO = e.getKey();
                Set<Integer> speciesIds = e.getValue().stream().map(speTO -> speTO.getId())
                        .collect(Collectors.toSet());
                Set<Integer> allNewRelSpeIds = new HashSet<>();
                allNewRelSpeIds.addAll(speciesIds);
                Integer relId = null;
                //try to find if the direct relation already exists in some species
                RelationTO<String> foundRelTO = allDirectRels.stream()
                        .filter(relTO -> Objects.equal(relTO.getSourceId(), newDirectRelTO.getSourceId()) &&
                                Objects.equal(relTO.getTargetId(), newDirectRelTO.getTargetId()))
                        .findAny().orElse(null);
                if (foundRelTO != null) {
                    Set<Integer> foundRelTOSpeciesIds = allRelTaxonConstraints.get(foundRelTO.getId());
                    if (foundRelTOSpeciesIds.contains(null)) {
                        foundRelTOSpeciesIds = allSpeciesIds;
                    }
                    allNewRelSpeIds.addAll(foundRelTOSpeciesIds);
                    relId = foundRelTO.getId();
                } else {
                    maxRelationId++;
                    relId = maxRelationId;
                    out.print("INSERT INTO anatEntityRelation " +
                            "(anatEntityRelationId, anatEntitySourceId, anatEntityTargetId, " +
                            " relationType, relationStatus) " +
                            "VALUES (" + relId + ", '" + newDirectRelTO.getSourceId()
                            + "', '" + newDirectRelTO.getTargetId() + "', '"
                            + newDirectRelTO.getRelationType() + "', '" + newDirectRelTO.getRelationStatus() + "'); ");
                }

                if (allSpeciesIds.equals(allNewRelSpeIds)) {
                    if (foundRelTO != null) {
                        out.print("DELETE FROM anatEntityRelationTaxonConstraint WHERE "
                                + "anatEntityRelationId = " + relId + "; ");
                    }
                    out.println("INSERT INTO anatEntityRelationTaxonConstraint "
                            + "(anatEntityRelationId, speciesId) VALUES (" + relId + ", NULL);");
                } else {
                    out.print("INSERT INTO anatEntityRelationTaxonConstraint "
                            + "(anatEntityRelationId, speciesId) VALUES ");
                    boolean firstSpecies = true;
                    for (Integer speId: speciesIds) {
                        if (!firstSpecies) {
                            out.print(", ");
                        }
                        out.print("(" + relId + ", " + speId + ")");
                        firstSpecies = false;
                    }
                    out.println(";");
                }
            }
            Map<Integer, Set<SpeciesTO>> allRelsToDelete = new HashMap<>();
            for (Entry<Integer, Set<SpeciesTO>> e: speciesPerPotentialCycle.entrySet()) {
                allRelsToDelete.put(e.getKey(), new HashSet<>(e.getValue()));
            }
            for (Entry<Integer, Set<SpeciesTO>> e: speciesPerNotExistingClasses.entrySet()) {
                allRelsToDelete.merge(e.getKey(), new HashSet<>(e.getValue()),
                        (existingSpeciesTOs, newSpeciesTOs) -> {
                            existingSpeciesTOs.addAll(newSpeciesTOs);
                            return existingSpeciesTOs;
                        });
            }
            allRelsToDelete.entrySet().stream().forEach(e -> {
                int incorrectRelId = e.getKey();
                Set<Integer> toRemoveSpeciesIds = e.getValue().stream().map(speTO -> speTO.getId())
                        .collect(Collectors.toSet());
                Set<Integer> existingSpeciesIds = allRelTaxonConstraints.get(incorrectRelId);
                if (existingSpeciesIds.contains(null)) {
                    existingSpeciesIds = allSpeciesIds;
                }
                Set<Integer> remainingSpeciesIds = existingSpeciesIds.stream()
                        .filter(id -> !toRemoveSpeciesIds.contains(id))
                        .collect(Collectors.toSet());
                out.print("DELETE FROM anatEntityRelationTaxonConstraint "
                        + "WHERE anatEntityRelationId = " + incorrectRelId + "; ");
                if (!remainingSpeciesIds.isEmpty()) {
                    out.print("INSERT INTO anatEntityRelationTaxonConstraint "
                            + "(anatEntityRelationId, speciesId) VALUES ");
                    if (allSpeciesIds.equals(remainingSpeciesIds)) {
                        //Should never happen, just in case
                        out.print("(" + incorrectRelId + ", NULL)");
                    } else {
                        out.print(remainingSpeciesIds.stream()
                                .map(id -> "(" + incorrectRelId + ", " + id + ")")
                                .collect(Collectors.joining(", ")));
                    }
                    out.print("; ");
                } else {
                    out.print("DELETE FROM anatEntityRelation "
                            + "WHERE anatEntityRelationId = " + incorrectRelId + "; ");
                }
                out.println();
            });
        }
        log.exit();
    }

    private static <T> Set<RelationTO<T>> findIndirectRelsNotReachedByChainOfDirectRels(
            Set<RelationTO<T>> directRels, Set<RelationTO<T>> indirectRels) {
        log.entry(directRels, indirectRels);

        //Retrieve the direct relations and put them in a map where the key is the source ID,
        //and the value a Set of the target IDs
        final Map<T, Set<T>> directRelMap = sourceToTargetsOrTargetToSources(
                directRels, true);

        //Iterate the indirect relations to be checked
        return log.exit(indirectRels.stream().filter(indirectRelTO -> {
            //Now we walk the chain of direct relations starting from the source
            //of this indirect relation, to check whether we can walk to the target
            boolean targetReached = false;
            Deque<T> walker = new ArrayDeque<>(directRelMap.getOrDefault(
                    indirectRelTO.getSourceId(), new HashSet<>()));
            T currentTermId = null;
            //to protect against cycles
            Set<T> visitedParentIds = new HashSet<>();
            visitedParentIds.add(indirectRelTO.getSourceId());
            while ((currentTermId = walker.pollFirst()) != null && !targetReached) {
                visitedParentIds.add(currentTermId);
                if (currentTermId.equals(indirectRelTO.getTargetId())) {
                    targetReached = true;
                } else {
                    Set<T> parentIds = directRelMap.getOrDefault(currentTermId, new HashSet<>());
                    for (T parentId: parentIds) {
                        if (!visitedParentIds.contains(parentId)) {
                            log.trace("Cycle, currentTermId {} - parentId {}", currentTermId, parentId);
                            walker.offerLast(parentId);
                        }
                    }
                }
            }
            return !targetReached;
        })
        .collect(Collectors.toSet()));
    }

    /**
     * Filter the relations identified by {@link #findIndirectRelsNotReachedByChainOfDirectRels(Set, Set)}
     * to keep only the most precise non-redundant incorrect indirect relations.
     * <p>
     * if the source of an incorrect indirect rel has a direct relation to another source
     * of an incorrect indirect rel to the same target, then the relation with this source
     * is not the most precise non-redundant incorrect indirect rel.
     * For instance, if the there are incorrect indirect rels C part_of A and
     * B part_of A, but there is a direct rel C part_of B, then by fixing the incorrect rel
     * B part_of A we will also fix the incorrect rel C part_of A.
     *
     * @param allRels                   A {@code Set} of {@code RelationTO}s representing
     *                                  all the relations between terms in the graph,
     *                                  direct or indirect.
     * @param incorrectIndirectRels     A {@code Set} of {@code RelationTO}s representing
     *                                  the incorrect indirect relations in the graph
     *                                  (see {@link #findIndirectRelsNotReachedByChainOfDirectRels(Set, Set)}).
     * @return                          A {@code Set} of {@code RelationTO}s that are the most precise
     *                                  non-redundant incorrect indirect relations.
     * @see #findIndirectRelsNotReachedByChainOfDirectRels(Set, Set)
     */
    private static <T> Set<RelationTO<T>> retainMostPreciseIndirectRelsNotReachedByChainOfDirectRels(
            Set<RelationTO<T>> allRels, Set<RelationTO<T>> incorrectIndirectRels) {
        log.entry(allRels, incorrectIndirectRels);

        Map<T, Set<T>> incorrectRelsTargetToSources = sourceToTargetsOrTargetToSources(
                incorrectIndirectRels, false);
        Map<T, Set<T>> allRelMap = sourceToTargetsOrTargetToSources(
                allRels, true);

        return log.exit(incorrectIndirectRels.stream()
        .filter(relTO -> {
            Set<T> sourceIds = new HashSet<>(incorrectRelsTargetToSources.get(relTO.getTargetId()));
            sourceIds.remove(relTO.getSourceId());
            Set<T> directRelsTargetIds = allRelMap.get(relTO.getSourceId());
            return directRelsTargetIds == null || Collections.disjoint(sourceIds, directRelsTargetIds);
        })
        .collect(Collectors.toSet()));
    }

    private static <T extends RelationTO<U>, U> Map<U, Set<U>> sourceToTargetsOrTargetToSources(
            Collection<T> rels, boolean sourceToTargets) {
        log.entry(rels, sourceToTargets);
        return log.exit(rels.stream().collect(Collectors.toMap(
                relTO -> sourceToTargets ? relTO.getSourceId() : relTO.getTargetId(),
                relTO -> new HashSet<>(Arrays.asList(sourceToTargets ? relTO.getTargetId() : relTO.getSourceId())),
                (v1, v2) -> {v1.addAll(v2); return v1;})));
    }
}
