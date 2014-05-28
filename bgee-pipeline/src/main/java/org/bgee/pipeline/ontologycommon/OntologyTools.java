package org.bgee.pipeline.ontologycommon;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.pipeline.OntologyUtils;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;

import owltools.graph.OWLGraphWrapper;

/**
 * Class responsible for various generic operations on ontologies.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
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
    public Set<String> getObsoleteIds(OWLOntology ont) throws UnknownOWLOntologyException {
        log.entry(ont);
        Set<String> obsoleteIds = new HashSet<String>();
        
        OWLGraphWrapper goWrapper = new OWLGraphWrapper(ont);
        for (OWLOntology myOnt: goWrapper.getAllOntologies()) {
            //we do not use goWrapper.getAllOWLClasses(), because it does not return 
            //deprecated classes
            for (OWLClass goTerm: myOnt.getClassesInSignature()) {
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
        
        for (OWLClass owlClass: ontWrapper.getAllOWLClasses()) {
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

}
