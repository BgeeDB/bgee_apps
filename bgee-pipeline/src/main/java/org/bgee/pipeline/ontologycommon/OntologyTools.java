package org.bgee.pipeline.ontologycommon;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
 * @version Bgee 15 Mar. 2021
 * @since Bgee 13
 */
//TODO: merge this class with OntologyUtils? Not sure actually.
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
     *     the relations, and retrieving a first batch of fixes, but should not be used
     *     the second time this command is run, to fix absolutely all relations.
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
        log.traceEntry("{}", (Object[]) args);
        
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
        } else if (args[0].equalsIgnoreCase("deletePartOfIsARelations")) {
            if (args.length != 4) {
                throw log.throwing(new IllegalArgumentException(
                        "Incorrect number of arguments provided, expected " +
                        "4 arguments, " + args.length + " provided."));
            }
            tools.deletePartOfIsARelations(CommandRunner.parseMapArgument(args[1]),
                    CommandRunner.parseListArgumentAsInt(args[2]),
                    DAOManager.getDAOManager(), args[3]);
        } else if (args[0].equalsIgnoreCase("insertPartOfIsARelations")) {
            if (args.length != 4) {
                throw log.throwing(new IllegalArgumentException(
                        "Incorrect number of arguments provided, expected " +
                        "4 arguments, " + args.length + " provided."));
            }
            tools.insertPartOfIsARelations(CommandRunner.parseMapArgument(args[1]),
                    CommandRunner.parseListArgumentAsInt(args[2]),
                    DAOManager.getDAOManager(), args[3]);
        } else if (args[0].equalsIgnoreCase("reportOnOntologyRoots")) {
            if (args.length != 1) {
                throw log.throwing(new IllegalArgumentException(
                        "Incorrect number of arguments provided, expected " +
                        "1 argument, " + args.length + " provided."));
            }
            tools.reportOnOntologyRootsInDB(DAOManager.getDAOManager());
        } else if (args[0].equalsIgnoreCase("explainIndirectRelsFromDB")) {
            if (args.length != 3) {
                throw log.throwing(new IllegalArgumentException(
                        "Incorrect number of arguments provided, expected " +
                        "3 arguments, " + args.length + " provided."));
            }
            tools.explainIndirectRelsFromDB(CommandRunner.parseMapArgument(args[1]),
                    CommandRunner.parseListArgumentAsInt(args[2]),
                    DAOManager.getDAOManager());
        } else {
            throw log.throwing(new IllegalArgumentException("Unrecognized command " + 
                args[0]));
        }
        
        log.traceExit();
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
        log.traceEntry("{} - {}", ontFile, obsIdsFile);
        
        Set<String> obsoleteIds = this.getObsoleteIds(ontFile);
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
                obsIdsFile)))) {
            for (String obsoleteId: obsoleteIds) {
                out.println(obsoleteId);
            }
        }
        
        log.traceExit();
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
        log.traceEntry("{}", ontFile);
        
        return log.traceExit(this.getObsoleteIds(OntologyUtils.loadOntology(ontFile)));
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
        log.traceEntry("{}", ont);
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
        
        return log.traceExit(obsoleteIds);
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
        log.traceEntry("{}", ontFile);
        
        return log.traceExit(this.getAllRealOWLClassIds(
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
        log.traceEntry("{}", ontWrapper);
        Set<String> allIds = new HashSet<String>();
        
        for (OWLClass owlClass: ontWrapper.getAllRealOWLClasses()) {
            allIds.add(ontWrapper.getIdentifier(owlClass));
        }
        
        return log.traceExit(allIds);
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
        log.traceEntry("{} - {}", ontFile, outputFile);
        
        Set<String> ids = this.getAllRealOWLClassIds(ontFile);
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
                outputFile)))) {
            for (String id: ids) {
                out.println(id);
            }
        }
        
        log.traceExit();
    }

    public void explainIndirectRelsFromDB(Map<String, List<String>> indirectRelsToQuery,
            Collection<Integer> speciesIds, DAOManager daoManager) {
        log.traceEntry("{} - {} - {}", indirectRelsToQuery, speciesIds, daoManager);
        if (indirectRelsToQuery == null || indirectRelsToQuery.isEmpty()) {
            System.out.println("Nothing to be done");
            log.traceExit(); return;
        }

        Set<SpeciesTO> allSpeciesTOs = new HashSet<>(
                daoManager.getSpeciesDAO().getAllSpecies(null).getAllTOs());
        Set<SpeciesTO> selectedSpeciesTOs = checkAndGetSelectedSpeciesTOs(allSpeciesTOs, speciesIds);
        AnatEntityDAO anatEntityDAO = daoManager.getAnatEntityDAO();
        RelationDAO relDAO = daoManager.getRelationDAO();

        Map<PipelineRelationTO<String>, Map<List<PipelineRelationTO<String>>, Set<SpeciesTO>>>
        explanationChainsAndSpecies = new HashMap<>();
        Iterator<SpeciesTO> speciesTOIterator = selectedSpeciesTOs.iterator();
        while (speciesTOIterator.hasNext()) {
            SpeciesTO speciesTO = speciesTOIterator.next();
            Collection<Integer> speciesId = Collections.singleton(speciesTO.getId());

            //Get the anat. entity IDs valid in this species
            Set<String> anatEntityIdsForSpecies = anatEntityDAO.getAnatEntities(speciesId, true, null,
                    EnumSet.of(AnatEntityDAO.Attribute.ID))
                    .stream()
                    .map(aeTO -> aeTO.getId())
                    .collect(Collectors.toSet());
            //Get the direct relations valid in this species
            Set<RelationTO<String>> directRels = new HashSet<>(relDAO.getAnatEntityRelations(
                    speciesId, true, null, null, true,
                    EnumSet.of(RelationTO.RelationType.ISA_PARTOF),
                    EnumSet.of(RelationTO.RelationStatus.DIRECT),
                    null)
                    .getAllTOs());
            //Get the indirect relations valid in this species
            Set<RelationTO<String>> indirectRels = new HashSet<>(relDAO.getAnatEntityRelations(
                    speciesId, true, null, null, true,
                    EnumSet.of(RelationTO.RelationType.ISA_PARTOF),
                    EnumSet.of(RelationTO.RelationStatus.INDIRECT),
                    null)
                    .getAllTOs());

            Set<RelationTO<String>> relsToQuery = indirectRelsToQuery.entrySet().stream().flatMap(e ->
                    indirectRels.stream().filter(rel ->
                        rel.getSourceId().equals(e.getKey()) && e.getValue().contains(rel.getTargetId())
                    )
            ).collect(Collectors.toSet());
            if (relsToQuery.isEmpty()) {
                continue;
            }
            Map<PipelineRelationTO<String>, Set<List<PipelineRelationTO<String>>>> indirectRelsToExplain =
                    findChainOfDirectRelsWithNoCorrespondingIndirectRels(directRels, indirectRels,
                            anatEntityIdsForSpecies, null, relsToQuery);
            for (Entry<PipelineRelationTO<String>, Set<List<PipelineRelationTO<String>>>> relToExplain:
                indirectRelsToExplain.entrySet()) {
                Map<List<PipelineRelationTO<String>>, Set<SpeciesTO>> speciesPerChain =
                        relToExplain.getValue().stream()
                        .map(l -> new AbstractMap.SimpleEntry<>(l, new HashSet<>(Arrays.asList(speciesTO))))
                        .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
                explanationChainsAndSpecies.merge(relToExplain.getKey(),
                        speciesPerChain,
                    (existingSpeciesPerChain, newSpeciesPerChain) -> {
                        for (Entry<List<PipelineRelationTO<String>>, Set<SpeciesTO>> chain:
                            newSpeciesPerChain.entrySet()) {
                            existingSpeciesPerChain.merge(chain.getKey(), new HashSet<>(chain.getValue()),
                                    (s1, s2) -> {s1.addAll(s2); return s1;});
                        }
                        return existingSpeciesPerChain;
                    });
            }
        }

        Map<String, String> anatEntityNameMap = anatEntityDAO.getAnatEntities(
                selectedSpeciesTOs.stream().map(s -> s.getId()).collect(Collectors.toSet()),
                true, null,
                EnumSet.of(AnatEntityDAO.Attribute.ID, AnatEntityDAO.Attribute.NAME))
                .stream()
                .map(ae -> new AbstractMap.SimpleEntry<>(ae.getId(), ae.getName()))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

        explanationChainsAndSpecies.entrySet().stream().forEach(e -> {
            System.out.println("Explanations for relation from " + e.getKey().getSourceId()
                    + " \"" + anatEntityNameMap.get(e.getKey().getSourceId()) + "\" to "
                    + e.getKey().getTargetId() + " \""
                    + anatEntityNameMap.get(e.getKey().getTargetId()) + "\": ");
            e.getValue().entrySet().stream().forEach(e2 -> {
                System.out.println("In species: " + e2.getValue().stream()
                        .map(speTO -> speTO.getId() + " " + speTO.getGenus() + " " + speTO.getSpeciesName())
                        .collect(Collectors.joining(", ")));
                System.out.println(e2.getKey().stream().map(aeId -> aeId + " \""
                        + anatEntityNameMap.get(aeId) + "\" ")
                .collect(Collectors.joining(" - ", "\t", "")));
            });
            System.out.println();
        });
        

        log.traceExit();
    }
    public void insertPartOfIsARelations(Map<String, List<String>> relMapToAdd,
            Collection<Integer> speciesIds, DAOManager daoManager, String outputFile) throws IOException {
        log.traceEntry("{} - {} - {} - {}", relMapToAdd, speciesIds, daoManager, outputFile);

        if (relMapToAdd == null || relMapToAdd.isEmpty()) {
            log.traceExit(); return;
        }
        Set<SpeciesTO> allSpeciesTOs = new HashSet<>(
                daoManager.getSpeciesDAO().getAllSpecies(null).getAllTOs());
        Set<Integer> allSpeciesIds = allSpeciesTOs.stream().map(sTO -> sTO.getId())
                .collect(Collectors.toSet());
        Set<SpeciesTO> selectedSpeciesTOs = checkAndGetSelectedSpeciesTOs(allSpeciesTOs, speciesIds);
        AnatEntityDAO anatEntityDAO = daoManager.getAnatEntityDAO();
        RelationDAO relDAO = daoManager.getRelationDAO();
        TaxonConstraintDAO taxonConstraintDAO = daoManager.getTaxonConstraintDAO();

        Set<PipelineRelationTO<String>> relsToAdd = relMapToAdd.entrySet().stream().flatMap(e ->
                e.getValue().stream().map(v -> new PipelineRelationTO<String>(
                e.getKey(), v,
                RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT))
        ).collect(Collectors.toSet());

        // We go species by species, that's how the method
        // findChainOfDirectRelsWithNoCorrespondingIndirectRels works.
        Map<PipelineRelationTO<String>, Set<SpeciesTO>> speciesPerRelToAdd = new HashMap<>();
        Iterator<SpeciesTO> speciesTOIterator = selectedSpeciesTOs.iterator();
        while (speciesTOIterator.hasNext()) {
            SpeciesTO speciesTO = speciesTOIterator.next();
            Collection<Integer> speciesId = Collections.singleton(speciesTO.getId());

            //Get the anat. entity IDs valid in this species
            Set<String> anatEntityIdsForSpecies = anatEntityDAO.getAnatEntities(speciesId, true, null,
                    EnumSet.of(AnatEntityDAO.Attribute.ID))
                    .stream()
                    .map(aeTO -> aeTO.getId())
                    .collect(Collectors.toSet());
            //Get the direct relations valid in this species
            Set<RelationTO<String>> directRels = new HashSet<>(relDAO.getAnatEntityRelations(
                    speciesId, true, null, null, true,
                    EnumSet.of(RelationTO.RelationType.ISA_PARTOF),
                    EnumSet.of(RelationTO.RelationStatus.DIRECT),
                    null)
                    .getAllTOs());
            //Get the indirect relations valid in this species
            Set<RelationTO<String>> indirectRels = new HashSet<>(relDAO.getAnatEntityRelations(
                    speciesId, true, null, null, true,
                    EnumSet.of(RelationTO.RelationType.ISA_PARTOF),
                    EnumSet.of(RelationTO.RelationStatus.INDIRECT),
                    null)
                    .getAllTOs());

            Set<PipelineRelationTO<String>> missingRels =
                    findChainOfDirectRelsWithNoCorrespondingIndirectRels(directRels, indirectRels,
                            anatEntityIdsForSpecies, relsToAdd, null).keySet().stream()
                    .collect(Collectors.toSet());
            missingRels.addAll(relsToAdd);
            for (PipelineRelationTO<String> missingRel: missingRels) {
                speciesPerRelToAdd.merge(missingRel,
                    new HashSet<>(Arrays.asList(speciesTO)),
                    (existingSpeciesTOs, newSpeciesTOs) -> {
                        existingSpeciesTOs.addAll(newSpeciesTOs);
                        return existingSpeciesTOs;
                    });
            }
        }


        Set<RelationTO<String>> allDirectRels = getAllDirectRels(relDAO);
        Set<RelationTO<String>> allIndirectRels = getAllIndirectRels(relDAO);
        Map<Integer, Set<Integer>> allRelTaxonConstraints = getAllRelTaxonConstraints(taxonConstraintDAO);
        int maxRelationId = relDAO.getAnatEntityRelations(
                null, true, null, null, true, null, null,
                EnumSet.of(RelationDAO.Attribute.RELATION_ID))
                .stream().mapToInt(relTO -> relTO.getId())
                .max().getAsInt();
 
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
                outputFile)))) {
            writeAddRelsQueries(speciesPerRelToAdd, allDirectRels, allIndirectRels,
                    allRelTaxonConstraints, allSpeciesIds,
                    maxRelationId, out);
        }
    }

    private static Set<SpeciesTO> checkAndGetSelectedSpeciesTOs(Set<SpeciesTO> allSpeciesTOs,
            Collection<Integer> requestedSpeciesIds) throws IllegalArgumentException {
        log.traceEntry("{} - {}", allSpeciesTOs, requestedSpeciesIds);
        Set<Integer> allSpeciesIds = allSpeciesTOs.stream().map(sTO -> sTO.getId())
                .collect(Collectors.toSet());
        if (requestedSpeciesIds != null && !allSpeciesIds.containsAll(requestedSpeciesIds)) {
            throw log.throwing(new IllegalArgumentException("Unrecognized species IDs: "
                    + requestedSpeciesIds.stream().filter(id -> !allSpeciesIds.contains(id))
                    .map(id -> id.toString())
                    .collect(Collectors.joining(", "))));
        }
        return log.traceExit(allSpeciesTOs.stream().filter(speTO ->
        requestedSpeciesIds == null || requestedSpeciesIds.isEmpty() ||
        requestedSpeciesIds.contains(speTO.getId()))
                .collect(Collectors.toSet()));
    }

    /**
     * 
     * @param relsToRemove  A {@code Map} where the keys are {@code String}s that are the OBO-like IDs
     *                      of the source of the relation to delete, the associated value being
     *                      a {@code List} of {@code String}s that are the OBO-like IDs
     *                      of the target of the relations to delete.
     * @param speciesIds    A {@code Collection} of {@code Integer}s that are the NCBI IDs of the species
     *                      the relations should be deleted for. If null or empty, the relations
     *                      are removed for all species.
     * @param daoManager
     * @param uberon
     * @param outputFile
     */
    public void deletePartOfIsARelations(Map<String, List<String>> relMapToRemove,
            Collection<Integer> speciesIds, DAOManager daoManager, String outputFile)
                    throws IOException {
        log.traceEntry("{} - {} - {} - {} - {}", relMapToRemove, speciesIds, daoManager, outputFile);

        if (relMapToRemove == null || relMapToRemove.isEmpty()) {
            log.traceExit(); return;
        }
        Set<SpeciesTO> allSpeciesTOs = new HashSet<>(
                daoManager.getSpeciesDAO().getAllSpecies(null).getAllTOs());
        Set<Integer> allSpeciesIds = allSpeciesTOs.stream().map(sTO -> sTO.getId())
                .collect(Collectors.toSet());
        Set<SpeciesTO> selectedSpeciesTOs = checkAndGetSelectedSpeciesTOs(allSpeciesTOs, speciesIds);

        RelationDAO relDAO = daoManager.getRelationDAO();
        TaxonConstraintDAO taxonConstraintDAO = daoManager.getTaxonConstraintDAO();
        Map<Integer, Set<Integer>> allRelTaxonConstraints = getAllRelTaxonConstraints(taxonConstraintDAO);

        // We go species by species, that's how the method
        // findIndirectRelsNotReachedByChainOfDirectRels works.
        Map<Integer, Set<SpeciesTO>> relsToDelete = new HashMap<>();
        Iterator<SpeciesTO> speciesTOIterator = selectedSpeciesTOs.iterator();
        while (speciesTOIterator.hasNext()) {
            SpeciesTO speciesTO = speciesTOIterator.next();
            Collection<Integer> speciesId = Collections.singleton(speciesTO.getId());

            Set<RelationTO<String>> directRels = new HashSet<>(relDAO.getAnatEntityRelations(
                    speciesId, true, null, null, true,
                    EnumSet.of(RelationTO.RelationType.ISA_PARTOF),
                    EnumSet.of(RelationTO.RelationStatus.DIRECT),
                    null)
                    .getAllTOs());
            Set<RelationTO<String>> indirectRels = new HashSet<>(relDAO.getAnatEntityRelations(
                    speciesId, true, null, null, true,
                    EnumSet.of(RelationTO.RelationType.ISA_PARTOF),
                    EnumSet.of(RelationTO.RelationStatus.INDIRECT),
                    null)
                    .getAllTOs());
            Set<RelationTO<String>> allRels = new HashSet<>(directRels);
            allRels.addAll(indirectRels);
            
            Set<RelationTO<String>> relsToRemove = relMapToRemove.entrySet().stream()
                    .flatMap(e -> allRels.stream().filter(rel ->
                        rel.getSourceId().equals(e.getKey()) && e.getValue().contains(rel.getTargetId()))
                    ).collect(Collectors.toSet());
            if (relsToRemove.isEmpty()) {
                continue;
            }
            relsToRemove.addAll(findIndirectRelsNotReachedByChainOfDirectRels(directRels,
                    indirectRels, relsToRemove));

            for (RelationTO<String> relToRemove: relsToRemove) {
                relsToDelete.merge(relToRemove.getId(), new HashSet<>(Arrays.asList(speciesTO)),
                        (oldSet, newSet) -> {oldSet.addAll(newSet); return oldSet;});
            }
        }

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
                outputFile)))) {
            writeDeleteQueries(relsToDelete, allRelTaxonConstraints, allSpeciesIds, out);
        }
    }

    public void reportOnOntologyRootsInDB(DAOManager daoManager) {
        log.traceEntry("{}", daoManager);

        RelationDAO relDAO = daoManager.getRelationDAO();
        Map<Set<String>, Set<SpeciesTO>> speciesPerRoots = daoManager.getSpeciesDAO()
                .getAllSpecies(null).stream()
                .map(speciesTO -> new AbstractMap.SimpleEntry<>(
                        getOntologyRoots(relDAO.getAnatEntityRelations(
                                Collections.singleton(speciesTO.getId()), true, null, null, true,
                                EnumSet.of(RelationTO.RelationType.ISA_PARTOF),
                                EnumSet.of(RelationTO.RelationStatus.DIRECT),
                                null).getAllTOs()),
                        new HashSet<>(Arrays.asList(speciesTO))))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(),
                        (v1, v2) -> {v1.addAll(v2); return v1;}));

        AnatEntityDAO anatEntityDAO = daoManager.getAnatEntityDAO();
        Map<String, String> anatEntityNameMap = anatEntityDAO.getAnatEntities(null, true,
                speciesPerRoots.keySet().stream().flatMap(s -> s.stream()).collect(Collectors.toSet()),
                EnumSet.of(AnatEntityDAO.Attribute.ID, AnatEntityDAO.Attribute.NAME))
                .stream()
                .map(ae -> new AbstractMap.SimpleEntry<>(ae.getId(), ae.getName()))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

        if (speciesPerRoots.size() == 1) {
            System.out.println("All species have the same roots: ");
            speciesPerRoots.keySet().stream().flatMap(s -> s.stream())
            .forEach(rootId -> System.out.println(rootId + ": " + anatEntityNameMap.get(rootId)));
        } else {
            System.out.println("Different roots in different species: ");
            speciesPerRoots.entrySet().stream().forEach(e -> {
                e.getKey().stream()
                .forEach(rootId -> System.out.println(rootId + ": " + anatEntityNameMap.get(rootId)));
                System.out.println("In species: ");
                e.getValue().stream()
                .forEach(speId -> System.out.println(speId));
                System.out.println();
            });
        }

        log.traceExit();
    }

    private static <T> Set<T> getOntologyRoots(Collection<RelationTO<T>> directRels) {
        log.traceEntry("{}", directRels);
        Set<T> targetIds = directRels.stream()
                .map(rel -> rel.getTargetId())
                .collect(Collectors.toSet());
        Set<T> sourceIds = directRels.stream()
                .map(rel -> rel.getSourceId())
                .collect(Collectors.toSet());
        targetIds.removeAll(sourceIds);
        return log.traceExit(targetIds);
    }

    public void getFromDBAnatPartOfIsAIndirectRelsNotReachedByChainOfDirectRelsAndWriteToFile(
            DAOManager daoManager, Uberon uberon, boolean filterRels, String outputFile) throws IOException {
        log.traceEntry("{} - {} - {} - {}", daoManager, uberon, filterRels, outputFile);

        RelationDAO relDAO = daoManager.getRelationDAO();
        AnatEntityDAO anatEntityDAO = daoManager.getAnatEntityDAO();
        TaxonConstraintDAO taxonConstraintDAO = daoManager.getTaxonConstraintDAO();

        //We go species by species, it will be simpler,
        //but we will summarize as much as possible over all species afterwards
        Set<SpeciesTO> allSpeciesTOs = new HashSet<>(
                daoManager.getSpeciesDAO().getAllSpecies(null).getAllTOs());

        //We retrieve all direct relations, all indirect relations, all taxon constraints
        //of all relations, and the max relation ID, to generate the SQL commands to fix
        //the issues identified (rather than directly modifying the database with no review)
        Set<RelationTO<String>> allDirectRels = getAllDirectRels(relDAO);
        Set<RelationTO<String>> allIndirectRels = getAllIndirectRels(relDAO);
        Map<Integer, Set<Integer>> allRelTaxonConstraints = getAllRelTaxonConstraints(taxonConstraintDAO);
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
        //As a check, we'll also look for chains of direct relations with no corresponding indirect relations
        Map<PipelineRelationTO<String>, Set<SpeciesTO>> speciesPerMissingIndirectRels = new HashMap<>();
        Map<PipelineRelationTO<String>, Set<List<PipelineRelationTO<String>>>> allMissingIndirectRels =
                new HashMap<>();
        //To provide more information about the issues, we'll associate the incorrect relation IDs
        //in the Bgee database to the corresponding OWLGraphEdges retrieved from the ontology, if any.
        Map<Integer, Set<OWLGraphEdge>> owlGraphEdgesPerIncorrectIndirectRelId = new HashMap<>();
        //We'll also associate the incorrect relations to the newly created relations that will fix them
        Map<Integer, Set<PipelineRelationTO<String>>> newRelTOsPerIncorrectIndirectRelId = new HashMap<>();
 
        //OK, now we have everything, let's go for surgical strikes to fix the issues,
        //species by species.
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
                    findIndirectRelsNotReachedByChainOfDirectRels(directRels, indirectRels, null);
            log.info("{} indirect incorrect relations for species {}",
                    incorrectIndirectRelTOs.size(), speciesTO.getId());
            if (filterRels) {
                //We try to make these incorrect indirect rels non-redundant
                incorrectIndirectRelTOs = retainMostPreciseIndirectRelsNotReachedByChainOfDirectRels(
                                allRels, incorrectIndirectRelTOs);
                log.info("{} after filtering", incorrectIndirectRelTOs.size());
            }

            //Now we find the chains of direct relations with no corresponding indirect rels
            Map<PipelineRelationTO<String>, Set<List<PipelineRelationTO<String>>>> missingIndirectRels =
                    findChainOfDirectRelsWithNoCorrespondingIndirectRels(directRels, indirectRels,
                            anatEntityIdsForSpecies, null, null);
            log.info("{} missing indirect relations to add for species {}",
                    missingIndirectRels.size(), speciesTO.getId());
            //We merge the potential problems over several species,
            //and also store all possible chains producing the indirect rels over all species
            for (Entry<PipelineRelationTO<String>, Set<List<PipelineRelationTO<String>>>> missingIndirectRel:
                missingIndirectRels.entrySet()) {
                allMissingIndirectRels.merge(missingIndirectRel.getKey(),
                        new HashSet<>(missingIndirectRel.getValue()),
                        (existingChains, newChains) -> {
                            existingChains.addAll(newChains);
                            return existingChains;
                        });
                speciesPerMissingIndirectRels.merge(missingIndirectRel.getKey(),
                    new HashSet<>(Arrays.asList(speciesTO)),
                    (existingSpeciesTOs, newSpeciesTOs) -> {
                        existingSpeciesTOs.addAll(newSpeciesTOs);
                        return existingSpeciesTOs;
                    });
            }

            //Now we store the incorrect indirect rels over multiple species and try to find fixes
            for (RelationTO<String> incorrectIndirectRelTO: incorrectIndirectRelTOs) {
                relationTOIdMap.put(incorrectIndirectRelTO.getId(), incorrectIndirectRelTO);

                //We retrieve the edges directly from Uberon to try to find fixes.
                //Only edges valid in the requested species will be retrieved
                Map<Boolean, Set<OWLGraphEdge>> outgoingEdges =
                        uberon.getValidOutgoingEdgesFromOWLClassIds(incorrectIndirectRelTO.getSourceId(),
                        incorrectIndirectRelTO.getTargetId(), speciesId, true, new HashSet<>())
                        .entrySet().stream()
                        //for each edge we check whether it is a part_of/is_a edge
                        .collect(Collectors.toMap(
                                e -> e.getKey(),
                                e -> e.getValue().stream().filter(outgoingEdge ->
                                uberon.getOntologyUtils().isASubClassOfEdge(outgoingEdge) ||
                                uberon.getOntologyUtils().isPartOfRelation(outgoingEdge))
                                .collect(Collectors.toSet())
                        ));
                Set<OWLGraphEdge> directEdges = outgoingEdges.get(true);
                Set<OWLGraphEdge> indirectEdges = outgoingEdges.get(false);
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
                                        composedRelSuperClassId, speciesId, true, new HashSet<>())
                                .entrySet().stream()
                                //for each edge we check whether it is a part_of/is_a edge
                                .collect(Collectors.toMap(
                                        testEdgeEntry -> testEdgeEntry.getKey(),
                                        testEdgeEntry -> testEdgeEntry.getValue().stream().filter(outgoingEdge ->
                                        uberon.getOntologyUtils().isASubClassOfEdge(outgoingEdge) ||
                                        uberon.getOntologyUtils().isPartOfRelation(outgoingEdge))
                                        .collect(Collectors.toSet())
                                ));
                        if ((!testEdges.get(false).isEmpty() || !testEdges.get(true).isEmpty()) &&
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
                                            relTO.getTargetId(), speciesId, true, new HashSet<>())
                                            .entrySet().stream()
                                            //for each edge we check whether it is a part_of/is_a edge
                                            .collect(Collectors.toMap(
                                                    testEdgeEntry -> testEdgeEntry.getKey(),
                                                    testEdgeEntry -> testEdgeEntry.getValue().stream().filter(outgoingEdge ->
                                                    uberon.getOntologyUtils().isASubClassOfEdge(outgoingEdge) ||
                                                    uberon.getOntologyUtils().isPartOfRelation(outgoingEdge))
                                                    .collect(Collectors.toSet())
                                            ));
                            return !testEdges.get(false).isEmpty() || !testEdges.get(true).isEmpty();
                        })
                        .collect(Collectors.toSet());

                //we filter relations where the target is the target of another target's relation
                //(redundant shorter path)
                Map<String, Set<String>> relTOsToAddSourceToTargets = sourceToTargetsOrTargetToSources(
                        relTOsToAdd, true);
                relTOsToAdd = relTOsToAdd.stream()
                        .filter(relTO -> {
                            Set<String> sameSourceTargets = relTOsToAddSourceToTargets.get(
                                    relTO.getSourceId());
                            sameSourceTargets.remove(relTO.getTargetId());
                            return sameSourceTargets.stream()
                                    .noneMatch(otherTargetId -> allRelsSourceToTargets.get(otherTargetId) != null &&
                                            allRelsSourceToTargets.get(otherTargetId).contains(relTO.getTargetId()));
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


        Set<Integer> allSpeciesIds = allSpeciesTOs.stream().map(sTO -> sTO.getId())
                .collect(Collectors.toSet());

        //To know if there are some indirect relations that should be removed because replaced with
        //some direct relations
        Map<Integer, Set<SpeciesTO>> speciesPerIndirectRelsToRemove = new HashMap<>();
        for (Entry<PipelineRelationTO<String>, Set<SpeciesTO>> e: speciesPerRelTOToAdd.entrySet()) {
            RelationTO<String> newDirectRelTO = e.getKey();
            Set<SpeciesTO> speciesTOs = new HashSet<>(e.getValue());
            //try to find if the equivalent indirect relation already exists in some species
            RelationTO<String> foundRelTO = allIndirectRels.stream()
                    .filter(relTO -> Objects.equal(relTO.getSourceId(), newDirectRelTO.getSourceId()) &&
                            Objects.equal(relTO.getTargetId(), newDirectRelTO.getTargetId()))
                    .findAny().orElse(null);
            if (foundRelTO != null) {
                speciesPerIndirectRelsToRemove.put(foundRelTO.getId(), speciesTOs);
            }
        }

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
                outputFile)))) {
            out.println("INDIRECT RELS WITH MISSING CHAINS OF DIRECT RELS:");
            out.println();
            speciesPerIncorrectIndirectRelId.entrySet().stream()
            .sorted(Comparator
                    .<Entry<Integer, Set<SpeciesTO>>>comparingInt(e -> e.getValue().size()).reversed()
                    .thenComparing(e -> e.getKey()))
            .forEach(e -> {
                Set<Integer> invalidSpeciesIds = e.getValue().stream().map(sTO -> sTO.getId())
                        .collect(Collectors.toSet());
                Set<Integer> taxonConstraintsForRel = allRelTaxonConstraints.get(e.getKey());
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

            //Now we print the missing indirect rels
            out.println();
            out.println("MISSING INDIRECT RELS AS COMPARED TO CHAINS OF DIRECT RELS:");
            out.println();
            speciesPerMissingIndirectRels.entrySet().stream().forEach(e -> {
                out.println(e.getKey() + " - in species: " +
                e.getValue().stream().map(speTO -> speTO.getId().toString()).collect(Collectors.joining(", ")) +
                " - chains of direct rels: " +
                allMissingIndirectRels.get(e.getKey()).stream()
                .map(chain -> chain.stream().map(rel -> rel.getTargetId())
                        .collect(Collectors.joining("-")))
                .collect(Collectors.joining(", ")));
            });

            //Now we print the SQL commands to fix the issues (rather than modifying directly
            //the database with no review)
            out.println();
            out.println("FIXES:");
            out.println();

            Map<PipelineRelationTO<String>, Set<SpeciesTO>> allRelsToAdd = new HashMap<>();
            for (Entry<PipelineRelationTO<String>, Set<SpeciesTO>> e: speciesPerRelTOToAdd.entrySet()) {
                allRelsToAdd.put(e.getKey(), new HashSet<>(e.getValue()));
            }
            log.info("{} new direct RelationTOs to add", speciesPerRelTOToAdd.size());
            for (Entry<PipelineRelationTO<String>, Set<SpeciesTO>> e: speciesPerMissingIndirectRels.entrySet()) {
                allRelsToAdd.merge(e.getKey(), new HashSet<>(e.getValue()),
                        (existingSpeciesTOs, newSpeciesTOs) -> {
                            existingSpeciesTOs.addAll(newSpeciesTOs);
                            return existingSpeciesTOs;
                        });
            }
            log.info("{} new indirect RelationTOs to add", speciesPerMissingIndirectRels.size());
            maxRelationId = writeAddRelsQueries(allRelsToAdd, allDirectRels, allIndirectRels,
                    allRelTaxonConstraints, allSpeciesIds, maxRelationId, out);

            Map<Integer, Set<SpeciesTO>> allRelsToDelete = new HashMap<>();
            for (Entry<Integer, Set<SpeciesTO>> e: speciesPerPotentialCycle.entrySet()) {
                allRelsToDelete.put(e.getKey(), new HashSet<>(e.getValue()));
            }
            log.info("{} indirect RelationTOs to remove because chain could cause a cycle",
                    speciesPerPotentialCycle.size());
            for (Entry<Integer, Set<SpeciesTO>> e: speciesPerNotExistingClasses.entrySet()) {
                allRelsToDelete.merge(e.getKey(), new HashSet<>(e.getValue()),
                        (existingSpeciesTOs, newSpeciesTOs) -> {
                            existingSpeciesTOs.addAll(newSpeciesTOs);
                            return existingSpeciesTOs;
                        });
            }
            log.info("{} indirect RelationTOs to remove because chain go through absent classes",
                    speciesPerNotExistingClasses.size());
            for (Entry<Integer, Set<SpeciesTO>> e: speciesPerIndirectRelsToRemove.entrySet()) {
                allRelsToDelete.merge(e.getKey(), new HashSet<>(e.getValue()),
                        (existingSpeciesTOs, newSpeciesTOs) -> {
                            existingSpeciesTOs.addAll(newSpeciesTOs);
                            return existingSpeciesTOs;
                        });
            }
            log.info("{} indirect RelationTOs to remove because direct equivalent relation will be added",
                    speciesPerIndirectRelsToRemove.size());
            log.info("{} indirect RelationTOs to remove in total", allRelsToDelete.size());
            writeDeleteQueries(allRelsToDelete, allRelTaxonConstraints, allSpeciesIds, out);
        }
        log.traceExit();
    }

    private static Set<RelationTO<String>> getAllDirectRels(RelationDAO relDAO) {
        log.traceEntry("{}", relDAO);
        return log.traceExit(new HashSet<>(relDAO.getAnatEntityRelations(
                null, true, null, null, true,
                EnumSet.of(RelationTO.RelationType.ISA_PARTOF),
                EnumSet.of(RelationTO.RelationStatus.DIRECT),
                EnumSet.of(RelationDAO.Attribute.RELATION_ID,
                        RelationDAO.Attribute.SOURCE_ID, RelationDAO.Attribute.TARGET_ID))
                .getAllTOs()));
    }
    private static Set<RelationTO<String>> getAllIndirectRels(RelationDAO relDAO) {
        log.traceEntry("{}", relDAO);
        return log.traceExit(new HashSet<>(relDAO.getAnatEntityRelations(
                null, true, null, null, true,
                EnumSet.of(RelationTO.RelationType.ISA_PARTOF),
                EnumSet.of(RelationTO.RelationStatus.INDIRECT),
                EnumSet.of(RelationDAO.Attribute.RELATION_ID,
                        RelationDAO.Attribute.SOURCE_ID, RelationDAO.Attribute.TARGET_ID))
                .getAllTOs()));
    }
    private static Map<Integer, Set<Integer>> getAllRelTaxonConstraints(TaxonConstraintDAO taxonConstraintDAO) {
        log.traceEntry("{}", taxonConstraintDAO);
        return log.traceExit(taxonConstraintDAO
                .getAnatEntityRelationTaxonConstraints(null, null).stream()
                .map(tcTO -> new AbstractMap.SimpleEntry<>(tcTO.getEntityId(),
                        new HashSet<>(Arrays.asList(tcTO.getSpeciesId()))))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(),
                        (v1, v2) -> {v1.addAll(v2); return v1;})));
    }

    private static void writeDeleteQueries(Map<Integer, Set<SpeciesTO>> relsToDelete,
            Map<Integer, Set<Integer>> allRelTaxonConstraints, Set<Integer> allSpeciesIds,
            PrintWriter out) {
        log.traceEntry("{} - {} - {} - {}", relsToDelete, allRelTaxonConstraints, allSpeciesIds, out);

        relsToDelete.entrySet().stream().forEach(e -> {
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
            if (remainingSpeciesIds.equals(existingSpeciesIds)) {
                log.warn("Maybe it's weird that this relation doesn't have to be deleted. "
                        + "relId: {} - remainingSpeciesIds: {} - existingSpeciesIds: {}",
                        incorrectRelId, remainingSpeciesIds, existingSpeciesIds);
                return;
            }
            out.print("DELETE FROM anatEntityRelationTaxonConstraint "
                    + "WHERE anatEntityRelationId = " + incorrectRelId + "; ");
            if (!remainingSpeciesIds.isEmpty()) {
                out.print("INSERT INTO anatEntityRelationTaxonConstraint "
                        + "(anatEntityRelationId, speciesId) VALUES ");
                assert !allSpeciesIds.equals(remainingSpeciesIds):
                    "There should never be all species remaining";
//                if (allSpeciesIds.equals(remainingSpeciesIds)) {
//                    //Should never happen, just in case
//                    out.print("(" + incorrectRelId + ", NULL)");
//                } else {
                    out.print(remainingSpeciesIds.stream()
                            .map(id -> "(" + incorrectRelId + ", " + id + ")")
                            .collect(Collectors.joining(", ")));
//                }
                out.print("; ");
            } else {
                out.print("DELETE FROM anatEntityRelation "
                        + "WHERE anatEntityRelationId = " + incorrectRelId + "; ");
            }
            out.println();
        });

        log.traceExit();
    }

    private static int writeAddRelsQueries(
            Map<PipelineRelationTO<String>, Set<SpeciesTO>> allRelsToAdd,
            Set<RelationTO<String>> allDirectRels, Set<RelationTO<String>> allIndirectRels,
            Map<Integer, Set<Integer>> allRelTaxonConstraints, Set<Integer> allSpeciesIds,
            int maxRelationId, PrintWriter out) {
        log.traceEntry("{} - {} - {} - {} - {} - {} - {}", allRelsToAdd, allDirectRels,
                allIndirectRels, allRelTaxonConstraints, allSpeciesIds, maxRelationId, out);

        for (Entry<PipelineRelationTO<String>, Set<SpeciesTO>> e: allRelsToAdd.entrySet()) {
            RelationTO<String> newRelTO = e.getKey();
            Set<Integer> speciesIds = e.getValue().stream().map(speTO -> speTO.getId())
                    .collect(Collectors.toSet());
            Set<Integer> allNewRelSpeIds = new HashSet<>();
            allNewRelSpeIds.addAll(speciesIds);
            Integer relId = null;
            //try to find if the relation already exists in some species
            Set<RelationTO<String>> relsToConsider = allDirectRels;
            if (newRelTO.getRelationStatus().equals(RelationTO.RelationStatus.INDIRECT)) {
                relsToConsider = allIndirectRels;
            }
            RelationTO<String> foundRelTO = relsToConsider.stream()
                    .filter(relTO -> Objects.equal(relTO.getSourceId(), newRelTO.getSourceId()) &&
                            Objects.equal(relTO.getTargetId(), newRelTO.getTargetId()))
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
                        "VALUES (" + relId + ", '" + newRelTO.getSourceId()
                        + "', '" + newRelTO.getTargetId() + "', '"
                        + newRelTO.getRelationType() + "', '" + newRelTO.getRelationStatus() + "'); ");
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
        return log.traceExit(maxRelationId);
    }

    //TODO
    //TODO: add another check in another method to find roots of the ontology: we had a few terms
    //hanging at the root, disconnected from the graph
//    private static <T> Set<RelationTO<T>> findDirectRelsToRemoveBecauseLongerPath(Set<RelationTO<T>> directRels,
//            Set<RelationTO<T>> indirectRels) {
//        log.entry(directRels, indirectRels);
//    }

    /**
     * Retrieve indirect relations that are missing in the database, generated by composing relations
     * over chains of existing direct relations.
     *
     * @param directRels    A {@code Set} of {@code RelationTO}s representing the direct relations
     *                      between terms existing in the database.
     * @param indirectRels  A {@code Set} of {@code RelationTO}s representing the indirect relations
     *                      between terms existing in the database.
     * @param allIds        A {@code Set} of {@code T}s representing all terms existing in the database.
     * @return              A {@code Map} where keys are composed indirect "is_a part_of" relations
     *                      missing in the database, the associated value being a {@code Set}
     *                      containing the different chains of direct relations allowing to compose
     *                      this relation, represented as {@code List}s of {@code PipelineRelationTO}s.
     */
    private static <T> Map<PipelineRelationTO<T>, Set<List<PipelineRelationTO<T>>>>
    findChainOfDirectRelsWithNoCorrespondingIndirectRels(Set<RelationTO<T>> directRels,
            Set<RelationTO<T>> indirectRels, Set<T> allIds, Set<PipelineRelationTO<T>> newRelsToAdd,
            Set<RelationTO<T>> indirectRelsToExplain) {
        log.traceEntry("{} - {} - {} - {} - {}", directRels, indirectRels, allIds,
                newRelsToAdd, indirectRelsToExplain);

        //Retrieve the indirect relations and put them in a map where the key is the source ID,
        //and the value a Set of the target IDs
        final Map<T, Set<T>> indirectRelMap = sourceToTargetsOrTargetToSources(
                indirectRels, true);
        //Same with the direct rels
        Set<RelationTO<T>> directRelsWithNewRels = new HashSet<>(directRels);
        if (newRelsToAdd != null) {
            directRelsWithNewRels.addAll(newRelsToAdd);
        }
        final Map<T, Set<T>> directRelMap = sourceToTargetsOrTargetToSources(
                directRelsWithNewRels, true);
        Set<T> idsToStart = allIds;
        if (indirectRelsToExplain != null && !indirectRelsToExplain.isEmpty()) {
            idsToStart = indirectRelsToExplain.stream().map(rel -> rel.getSourceId())
                    .collect(Collectors.toSet());
        }

        //We store the composed rels as keys, and the chains of direct rels allowing to compose
        //this relation as value
        Map<PipelineRelationTO<T>, Set<List<PipelineRelationTO<T>>>> composedRels = new HashMap<>();
        for (T id: idsToStart) {
            Set<T> firstParentIds = Collections.unmodifiableSet(
                    directRelMap.getOrDefault(id, new HashSet<>()));
            Set<List<PipelineRelationTO<T>>> firstChains = firstParentIds.stream()
                    .filter(parentId -> !parentId.equals(id))
                    .map(parentId -> new ArrayList<>(Arrays.asList(new PipelineRelationTO<>(id, parentId,
                            RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT))))
                    .collect(Collectors.toSet());
            Deque<List<PipelineRelationTO<T>>> walker = new ArrayDeque<>(firstChains);
            List<PipelineRelationTO<T>> currentChain = null;
            while ((currentChain = walker.pollFirst()) != null) {
                T currentTermId = currentChain.get(currentChain.size() - 1).getTargetId();
                Set<T> parentIds = Collections.unmodifiableSet(
                        directRelMap.getOrDefault(currentTermId, new HashSet<>()));
                for (T parentId: parentIds) {
                    //to protect against cycles
                    if (currentChain.stream().map(relTO -> relTO.getTargetId())
                            .noneMatch(targetId -> targetId.equals(parentId)) &&
                            !parentId.equals(id) && !parentId.equals(currentTermId)) {
                        List<PipelineRelationTO<T>> newChain = new ArrayList<>(currentChain);
                        newChain.add(new PipelineRelationTO<>(currentTermId, parentId,
                                RelationTO.RelationType.ISA_PARTOF,
                                RelationTO.RelationStatus.DIRECT));
                        composedRels.merge(new PipelineRelationTO<>(id, parentId,
                                RelationTO.RelationType.ISA_PARTOF,
                                RelationTO.RelationStatus.INDIRECT),
                                new HashSet<>(Arrays.asList(newChain)),
                                (existingChains, newChains) -> {
                                    existingChains.addAll(newChains);
                                    return existingChains;
                                });
                        walker.offerLast(newChain);
                    }
                }
            }
        }
        final Map<T, Set<T>> newRelToAddMap = newRelsToAdd == null || newRelsToAdd.isEmpty()?
                new HashMap<>(): sourceToTargetsOrTargetToSources(newRelsToAdd, true);
        final Map<T, Set<T>> relToExplainMap = indirectRelsToExplain == null || indirectRelsToExplain.isEmpty()?
                new HashMap<>(): sourceToTargetsOrTargetToSources(indirectRelsToExplain, true);
        return log.traceExit(composedRels.entrySet().stream()
                //If we requested explanations for some indirect rels, we keep those.
                .filter(e -> indirectRelsToExplain == null || indirectRelsToExplain.isEmpty() || 
                        relToExplainMap.getOrDefault(e.getKey().getSourceId(), new HashSet<>())
                        .contains(e.getKey().getTargetId()))
                //Otherwise, we only keep the missing indirect rels
                .filter(e -> (indirectRelsToExplain != null && !indirectRelsToExplain.isEmpty()) ||
                        !indirectRelMap.getOrDefault(e.getKey().getSourceId(), new HashSet<>())
                        .contains(e.getKey().getTargetId()))
                //if we were adding new relations, we only want to retrieve the inferred indirect rels
                //caused by these new relations
                .filter(e -> newRelsToAdd == null || newRelsToAdd.isEmpty() || 
                        e.getValue().stream().flatMap(l -> l.stream())
                        .anyMatch(pathRel -> newRelToAddMap.getOrDefault(
                                pathRel.getSourceId(), new HashSet<>())
                                .contains(pathRel.getTargetId())))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));
    }

    private static <T> Set<RelationTO<T>> findIndirectRelsNotReachedByChainOfDirectRels(
            Set<RelationTO<T>> directRels, Set<RelationTO<T>> indirectRels,
            Set<RelationTO<T>> relsNotToUse) {
        log.traceEntry("{} - {} - {}", directRels, indirectRels, relsNotToUse);

        //Retrieve the direct relations and put them in a map where the key is the source ID,
        //and the value a Set of the target IDs
        final Map<T, Set<T>> directRelMap = sourceToTargetsOrTargetToSources(
                directRels, true);

        //Iterate the indirect relations to be checked
        return log.traceExit(indirectRels.stream().filter(indirectRelTO -> {
            log.trace("indirectRelTO iterated: {}", indirectRelTO);
            //Now we walk the chain of direct relations starting from the source
            //of this indirect relation, to check whether we can walk to the target.
            //We used to remove from the first parents the target of the current indirect rel:
            //if there is an indirect relation between A and B, it must be through a chain
            //of rel, not a direct relation between them (which might be totally valid by the way).
            //But actually, an indirect rel might be caused by a cycle, such as:
            //A part_of B part_of C part_of B => we both have a direct and an indirect relation
            //A part_of B. Because of that, in order to find the indirect relations caused by
            //a cycle we'd like to remove, we need to start from all parents, even if it is
            //the target of the indirect rel already.
            Set<T> firstParentIds = new HashSet<>(directRelMap.getOrDefault(
                    indirectRelTO.getSourceId(), new HashSet<>()));
            log.trace("First parent IDs: {}", firstParentIds);
            Set<List<PipelineRelationTO<T>>> firstChains = firstParentIds.stream()
                    .filter(parentId -> !parentId.equals(indirectRelTO.getSourceId()))
                    .map(parentId -> new ArrayList<>(Arrays.asList(
                            new PipelineRelationTO<>(indirectRelTO.getSourceId(), parentId,
                            RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT))))
                    .collect(Collectors.toSet());
            log.trace("First chains: {}", firstChains);

            Deque<List<PipelineRelationTO<T>>> walker = new ArrayDeque<>(firstChains);
            List<PipelineRelationTO<T>> currentChain = null;
            //We need to store all valid chains, because in case there are rels not to use,
            //we only want to remove indirect rels if they were supported only by chains
            //going through these relations not to use. Just knowing that we don't reach the target
            //of the indirect rels through a chain of direct rels is not enough.
            //It's only if the only way to reach the target is through a rel not to use
            //that we remove the indirect rel.
            Set<List<PipelineRelationTO<T>>> validatedChains = new HashSet<>();
            while ((currentChain = walker.pollFirst()) != null &&
                    //If there are rels not to use, we really need to walk all paths to the end
                    //to know if the indirect rel was supported ONLY by paths going through
                    //a relation not to use.
                    //Otherwise, we can stop as soon as we find a chain of direct rels
                    //supporting the indirect rel.
                    (validatedChains.isEmpty() || relsNotToUse != null && !relsNotToUse.isEmpty() &&
                            //but we can stop as soon as the indirect rel is supported
                            //by a chain of direct rel not going through a rel not to use
                            validatedChains.stream().noneMatch(chain -> chain.stream()
                                    .noneMatch(relInChain -> relsNotToUse.stream()
                                        .anyMatch(relNotToUse ->
                                            relNotToUse.getSourceId().equals(relInChain.getSourceId()) &&
                                            relNotToUse.getTargetId().equals(relInChain.getTargetId()))
                                    )
                                ))) {

                T currentTermId = currentChain.get(currentChain.size() - 1).getTargetId();
                log.trace("Current chain: {} - current term ID: {}", currentChain, currentTermId);

                //The chain is validated if we reached the target, but really through
                //several direct rels, so that the relation is indirect (in case of cycle,
                //we could directly reach the target through a direct relation, but also
                //through several direct relations, and this is the latter case we need to identify)
                if (currentChain.size() > 1 &&
                        currentTermId.equals(indirectRelTO.getTargetId())) {
                    log.trace("Chain reached target");
                    validatedChains.add(currentChain);
                } else {
                    Set<T> parentIds = directRelMap.getOrDefault(currentTermId, new HashSet<>());
                    for (T parentId: parentIds) {
                        PipelineRelationTO<T> newRelInChain = new PipelineRelationTO<>(
                                currentTermId, parentId,
                                RelationTO.RelationType.ISA_PARTOF,
                                RelationTO.RelationStatus.DIRECT);
                        //To protect against cycles, while still being able to infer
                        //indirect rels caused by cycles. This is important in case
                        //we are using this method to, well, remove a cycle.
                        if (!currentChain.contains(newRelInChain)) {
                            log.trace("Continue chain with relation: {}", newRelInChain);
                            List<PipelineRelationTO<T>> newChain = new ArrayList<>(currentChain);
                            newChain.add(newRelInChain);
                            walker.offerLast(newChain);
                        } else {
                            log.trace("Cycle, currentTermId {} - parentId {}", currentTermId, parentId);
                        }
                    }
                }
            }
            if (relsNotToUse != null && !relsNotToUse.isEmpty()) {
                //The indirect rel is not supported, but not because we remove a relation
                //not to use, so we don't retain this indirect relation and return false.
                if (validatedChains.isEmpty()) {
                    return log.traceExit(false);
                }
                //In case the indirect rel was supported (!validatedChains.isEmpty()),
                //but ONLY by chains going through rels not to use, then we retain this indirect rel
                //for deletion.
                return log.traceExit(validatedChains.stream().noneMatch(chain -> chain.stream()
                            .noneMatch(relInChain -> relsNotToUse.stream()
                                .anyMatch(relNotToUse ->
                                    relNotToUse.getSourceId().equals(relInChain.getSourceId()) &&
                                    relNotToUse.getTargetId().equals(relInChain.getTargetId()))
                            )
                        ));
            }
            return log.traceExit(validatedChains.isEmpty());
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
        log.traceEntry("{} - {}", allRels, incorrectIndirectRels);

        Map<T, Set<T>> incorrectRelsTargetToSources = sourceToTargetsOrTargetToSources(
                incorrectIndirectRels, false);
        Map<T, Set<T>> allRelMap = sourceToTargetsOrTargetToSources(
                allRels, true);

        return log.traceExit(incorrectIndirectRels.stream()
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
        log.traceEntry("{} - {}", rels, sourceToTargets);
        return log.traceExit(rels.stream()
                .collect(Collectors.toMap(
                relTO -> sourceToTargets ? relTO.getSourceId() : relTO.getTargetId(),
                relTO -> new HashSet<>(Arrays.asList(sourceToTargets ? relTO.getTargetId() : relTO.getSourceId())),
                (v1, v2) -> {v1.addAll(v2); return v1;})));
    }
}
