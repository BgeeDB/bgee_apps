package org.bgee.pipeline.gene;

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
 * Class responsible for extracting relevant information from the Gene Ontology. 
 * This class accepts the GO as a OBO or OWL file. This class is distinct 
 * from {@code InsertGO}, because it does not rely on the use of the Bgee database, 
 * and therefore, is not a {@code MySQLDAOUser}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class GOTools {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(GOTools.class.getName());
    
    /**
     * Default constructor. 
     */
    public GOTools() {
        
    }
    
    /**
     * Main method to trigger the extraction of relevant information from the Gene Ontology. 
     * Parameters that must be provided in order in {@code args} are: 
     * <ol>
     * <li>path to the file storing the GO ontology, either in OBO or in OWL.
     * <li>path to the file where to store the list of obsolete GO terms (used 
     * for other parts of the pipeline)
     * </ol>
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
        int expectedArgLength = 2;
        if (args.length != expectedArgLength) {
            throw log.throwing(new IllegalArgumentException("Incorrect number of arguments " +
                    "provided, expected " + expectedArgLength + " arguments, " + args.length + 
                    " provided."));
        }
        
        GOTools tools = new GOTools();
        tools.writeObsoletedTermsToFile(args[0], args[1]);
        
        log.exit();
    }
    
    /**
     * Extract the OBO-like IDs of obsoleted terms from the Gene Ontology, stored 
     * in the file {@code goFile}, and write them into the file {@code obsIdsFile}, 
     * one ID per line.
     * 
     * @param goFile        A {@code String} that is the path to the file storing 
     *                      the Gene Ontology, in OBO or OWL.
     * @param obsIdsFile    A {@code String} that is the path to the file where to write 
     *                      the obsolete IDs.
     * @throws UnknownOWLOntologyException      If the ontology could not be loaded.
     * @throws OWLOntologyCreationException     If the ontology could not be loaded.
     * @throws OBOFormatParserException         If the ontology file could not be parsed.
     * @throws IOException                      If the ontology file coud not be read, 
     *                                          or output file could not be written. 
     */
    public void writeObsoletedTermsToFile(String goFile, String obsIdsFile) 
            throws UnknownOWLOntologyException, OWLOntologyCreationException, 
            OBOFormatParserException, IOException {
        log.entry(goFile, obsIdsFile);
        
        Set<String> obsoleteIds = this.getObsoleteIds(goFile);
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
                obsIdsFile)))) {
            for (String obsoleteId: obsoleteIds) {
                out.println(obsoleteId);
            }
        }
        
        log.exit();
    }
    
    /**
     * Extract the OBO-like IDs of obsoleted terms from the Gene Ontology, stored 
     * in the file {@code goFile}.
     * 
     * @param goFile    A {@code String} that is the path to the file storing the Gene Ontology, 
     *                  in OBO or OWL.
     * @return          A {@code Set} of {@code String}s that are the OBO-like IDs of deprecated 
     *                  terms (for instance, 'GO:0000005').
     * @throws UnknownOWLOntologyException      If the ontology could not be loaded.
     * @throws OWLOntologyCreationException     If the ontology could not be loaded.
     * @throws OBOFormatParserException         If the ontology file could not be parsed.
     * @throws IOException                      If the ontology file coud not be read. 
     * @see #getObsoleteIds(OWLOntology)
     */
    public Set<String> getObsoleteIds(String goFile) throws UnknownOWLOntologyException, 
        OWLOntologyCreationException, OBOFormatParserException, IOException {
        log.entry(goFile);
        
        return log.exit(this.getObsoleteIds(OntologyUtils.loadOntology(goFile)));
    }
    
    /**
     * Extract the OBO-like IDs of obsoleted terms from {@code geneOntology}.
     * 
     * @param geneOntology                  An {@code OWLOntology} that is the Gene Ontology.
     * @return                              A {@code Set} of {@code String}s that are the 
     *                                      OBO-like IDs of deprecated terms (for instance, 
     *                                      'GO:0000005').
     * @throws UnknownOWLOntologyException  If the ontology could not be loaded.
     * @throws OWLOntologyCreationException If the ontology could not be loaded.
     */
    public Set<String> getObsoleteIds(OWLOntology geneOntology) throws UnknownOWLOntologyException, 
        OWLOntologyCreationException {
        log.entry(geneOntology);
        Set<String> obsoleteIds = new HashSet<String>();
        
        OWLGraphWrapper goWrapper = new OWLGraphWrapper(geneOntology);
        for (OWLOntology ont: goWrapper.getAllOntologies()) {
            //we do not use goWrapper.getAllOWLClasses(), because it does not return 
            //deprecated classes
            for (OWLClass goTerm: ont.getClassesInSignature()) {
                if (goWrapper.isObsolete(goTerm)) {
                    obsoleteIds.add(goWrapper.getIdentifier(goTerm));
                    obsoleteIds.addAll(goWrapper.getAltIds(goTerm));
                }
            }
        }
        
        return log.exit(obsoleteIds);
    }

}
