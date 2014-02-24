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
     * <li>For retrieving obsolete IDs from am ontology: 
     *   <ol>
     *     <li>The keyword {@code extractObsoleteIds}.
     *     <li>path to the file storing the GO ontology, either in OBO or in OWL.
     *     <li>path to the file where to store the list of obsolete GO terms (used 
     *         for other parts of the pipeline)
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
        int expectedArgLength = 3;
        if (args.length != expectedArgLength) {
            throw log.throwing(new IllegalArgumentException("Incorrect number of arguments " +
                    "provided, expected " + expectedArgLength + " arguments, " + args.length + 
                    " provided."));
        }
        
        OntologyTools tools = new OntologyTools();
        switch(args[0]) {
        case "extractObsoleteIds":
            tools.writeObsoletedTermsToFile(args[1], args[2]);
            break;
        default: 
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
     * @throws OWLOntologyCreationException If the ontology could not be loaded.
     */
    public Set<String> getObsoleteIds(OWLOntology ont) throws UnknownOWLOntologyException, 
        OWLOntologyCreationException {
        log.entry(ont);
        Set<String> obsoleteIds = new HashSet<String>();
        
        OWLGraphWrapper goWrapper = new OWLGraphWrapper(ont);
        for (OWLOntology myOnt: goWrapper.getAllOntologies()) {
            //we do not use goWrapper.getAllOWLClasses(), because it does not return 
            //deprecated classes
            for (OWLClass goTerm: myOnt.getClassesInSignature()) {
                if (goWrapper.isObsolete(goTerm)) {
                    obsoleteIds.add(goWrapper.getIdentifier(goTerm));
                    obsoleteIds.addAll(goWrapper.getAltIds(goTerm));
                }
            }
        }
        
        return log.exit(obsoleteIds);
    }

}
