package org.bgee.pipeline.gene;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.gene.GOTermTO;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.pipeline.MySQLDAOUser;
import org.bgee.pipeline.OntologyUtils;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import owltools.graph.OWLGraphWrapper;

/**
 * Class responsible for inserting the Gene Ontology into 
 * the Bgee database. This class accepts the GO as a OBO or OWL file.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class InsertGO extends MySQLDAOUser {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(InsertGO.class.getName());
    
    /**
     * Default constructor. 
     */
    public InsertGO() {
        super();
    }
    /**
     * Constructor providing the {@code MySQLDAOManager} that will be used by 
     * this object to perform queries to the database. This is useful for unit testing.
     * 
     * @param manager   the {@code MySQLDAOManager} to use.
     */
    public InsertGO(MySQLDAOManager manager) {
        super(manager);
    }
    
    /**
     * Main method to trigger the insertion of the GO ontology into the Bgee 
     * database. Parameters that must be provided in order in {@code args} are: 
     * <ol>
     * <li>path to the file storing the GO ontology, either in OBO or in OWL.
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
        int expectedArgLength = 1;
        if (args.length != expectedArgLength) {
            throw log.throwing(new IllegalArgumentException("Incorrect number of arguments " +
                    "provided, expected " + expectedArgLength + " arguments, " + args.length + 
                    " provided."));
        }
        
        InsertGO insert = new InsertGO();
        insert.insert(args[0]);
        
        log.exit();
    }
    
    /**
     * Inserts the GO ontology into the Bgee database. This method uses the path 
     * to the file storing the GO ontology, either in OBO or in OWL. 
     * 
     * @param goFile   A {@code String} that is the path to the GO ontology.
     * @throws FileNotFoundException        If some files could not be found.
     * @throws IOException                  If some files could not be used.
     * @throws OWLOntologyCreationException If an error occurred while loading 
     *                                      the ontology.
     * @throws OBOFormatParserException     If an error occurred while loading 
     *                                      theontology.
     * @throws IllegalArgumentException     If the files used provided invalid information.
     * @throws DAOException                 If an error occurred while inserting 
     *                                      the data into the Bgee database.
     */
    public void insert(String goFile) throws FileNotFoundException, IOException, 
            OWLOntologyCreationException, OBOFormatParserException, 
            IllegalArgumentException, DAOException {
        log.entry(goFile);
        
        this.insert(OntologyUtils.loadOntology(goFile));
        
        log.exit();
    }
    
    /**
     * Inserts the GO ontology into the Bgee database. The argument is 
     * an {@code OWLOntology} representing the GO ontology, to be used in Bgee.
     * 
     * @param geneOntology    An {@code OWLOntology} that is the GO ontology.
     * @throws OWLOntologyCreationException If an error occurred while loading 
     *                                      the ontology.
     * @throws IllegalArgumentException     If the arguments provided invalid information.
     * @throws DAOException                 If an error occurred while inserting 
     *                                      the data into the Bgee database.
     */
    public void insert(OWLOntology geneOntology) 
            throws OWLOntologyCreationException, IllegalArgumentException, DAOException {
        log.entry(geneOntology);
        log.info("Starting insertion of GO ontology...");
        
        //catch any IllegalStateException to wrap it into a IllegalArgumentException 
        //(a IllegalStateException would be generated because the OWLOntology loaded 
        //from the file would be invalid, so it would be a wrong argument)
        try {
            
            //get the GOTermTOs to insert their information into the database
            Set<GOTermTO> goTermTOs = this.getGOTermTOs(new OWLGraphWrapper(geneOntology));
                        
            //now we start a transaction to insert GOTermTOs in the Bgee data source.
            //note that we do not need to call rollback if an error occurs, calling 
            //closeDAO will rollback any ongoing transaction
            try {
                this.startTransaction();
                //insert terms
                this.getGeneOntologyDAO().insertTerms(goTermTOs);
                this.commit();
            } finally {
                this.closeDAO();
            }
            log.info("Done inserting GO ontology, {} terms inserted", goTermTOs.size());
        } catch (IllegalStateException e) {
            log.catching(e);
            throw log.throwing(new IllegalArgumentException(
                    "The OWLOntology provided is invalid", e));
        }

        log.exit();
    }
    
    /**
     * Extract all terms from the Gene Ontology wrapped into {@code goWrapper}, 
     * and return them as a {@code Set} of {@code GOTermTO}s.
     * 
     * @param goWrapper A {@code OWLGraphWrapper} wrapping the Gene Ontology.
     * @return  a {@code Set} of {@code GOTermTO}s corresponding to the terms 
     *          in {@code goWrapper}.
     */
    private Set<GOTermTO> getGOTermTOs(OWLGraphWrapper goWrapper) {
        Set<GOTermTO> goTermTOs = new HashSet<GOTermTO>();
        for (OWLClass goTerm: goWrapper.getAllOWLClasses()) {
            goTermTOs.add(new GOTermTO(
                    goWrapper.getIdentifier(goTerm), 
                    goWrapper.getLabel(goTerm), 
                    this.namespaceToDomain(goWrapper.getNamespace(goTerm))));
        }
        return goTermTOs;
    }
    
    /**
     * Given a namespace in the Gene Ontology (either "biological_process", or 
     * "cellular_component", or "molecular_function"), return the corresponding 
     * {@code GOTermTO.Domain}.
     * 
     * @param namespace A {@code String} that is the namespace of a term in the Gene Ontology.
     * @return  A {@code GOTermTO.Domain} corresponding to {@code namespace}
     * @throw IllegalArgumentException    If {@code namespace} is not recognized.
     */
    private GOTermTO.Domain namespaceToDomain(String namespace) throws IllegalArgumentException {
        switch(namespace) {
        case "biological_process": 
            return GOTermTO.Domain.BP;
        case "cellular_component": 
            return GOTermTO.Domain.CC;
        case "molecular_function": 
            return GOTermTO.Domain.MF;
         default: 
             throw new IllegalArgumentException("Unknown namespace " + namespace); 
        }
    }
}
