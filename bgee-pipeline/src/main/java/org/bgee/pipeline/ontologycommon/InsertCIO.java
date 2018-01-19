package org.bgee.pipeline.ontologycommon;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTO;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTO.ConfidenceLevel;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTO.EvidenceConcordance;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTO.EvidenceTypeConcordance;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.pipeline.MySQLDAOUser;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * Class responsible for inserting the CIO ontology into the database.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Apr. 2015
 * @since Bgee 13
 */
public class InsertCIO extends MySQLDAOUser {
    
    private final static Logger log = LogManager.getLogger(InsertCIO.class.getName());
    

    
    /**
     * Main method to trigger the insertion of the CIO ontology into the Bgee 
     * database. Parameters that must be provided in order in {@code args} are: 
     * <ol>
     * <li>path to the file storing the CIO ontology, either in OBO or in OWL.
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
        
        InsertCIO insert = new InsertCIO();
        insert.insert(OntologyUtils.loadOntology(args[0]));
        
        log.exit();
    }
    
    /**
     * Default constructor. 
     */
    public InsertCIO() {
        this(null);
    }
    /**
     * Constructor providing the {@code MySQLDAOManager} that will be used by 
     * this object to perform queries to the database. This is useful for unit testing.
     * 
     * @param manager   The {@code MySQLDAOManager} to use.
     */
    public InsertCIO(MySQLDAOManager manager) {
        super(manager);
    }
    
    /**
     * Insert the provided CIO ontology into the database.
     * 
     * @param cioOnt    An {@code OWLOntology} that is the CIO ontology.
     * @throws DAOException             If an error occurred while inserting the terms 
     *                                  in the database.
     * @throws IllegalArgumentException If the provided CIO ontology was invalid and 
     *                                  did not allow to retrieve some required information.
     */
    public void insert(OWLOntology cioOnt) throws DAOException, IllegalArgumentException {
        log.entry(cioOnt);

        try {
            log.info("Start inserting CIO into database...");
            CIOWrapper cioWrapper = new CIOWrapper(cioOnt);
            this.startTransaction();
            
            Set<CIOStatementTO> tos = this.getCIOTOs(cioWrapper);
            this.getCIOStatementDAO().insertCIOStatements(tos);
            
            this.commit();
            log.info("Done inserting CIO into database, inserted {} terms.", tos.size());
        } finally {
            this.closeDAO();
        }
        
        log.exit();
    }
    
    /**
     * Extract from the provided CIO all valid terms that are CIO statements 
     * usable for annotations and return them as a {@code Set} of {@code CIOStatementTO}s.
     * 
     * @param cioWrapper    A {@code CIOWrapper} wrapping the CIO Ontology.
     * @return              A {@code Set} of {@code CIOStatementTO}s corresponding to 
     *                      valid terms in the ontology wrapped by {@code cioWrapper}.
     * @throws IllegalArgumentException If the provided CIO ontology was invalid and 
     *                                  did not allow to retrieve some required information.
     */
    private Set<CIOStatementTO> getCIOTOs(CIOWrapper cioWrapper) 
            throws IllegalArgumentException {
        log.entry(cioWrapper);
        
        Set<CIOStatementTO> cioTOs = new HashSet<CIOStatementTO>();
        
        for (OWLClass cls: cioWrapper.getOWLGraphWrapper().getAllRealOWLClasses()) {
            log.trace("Examining class {}", cls);
            //we only insert CIO statements, and only CIO statements that either have 
            //a confidence level, or that are the strongly conflicting statements, 
            //with no confidence level
            if (cioWrapper.getOWLGraphWrapper().isObsolete(cls) || 
                    cioWrapper.getOWLGraphWrapper().getIsObsolete(cls) || 
                    !cioWrapper.isConfidenceStatement(cls) || 
                    (!cioWrapper.hasLeafConfidenceLevel(cls) && 
                            !cioWrapper.isStronglyConflicting(cls))) {
                log.trace("Not a valid CIO statement, discarded.");
                continue;
            }
            //retrieve information for insertion
            
            String id = cioWrapper.getOWLGraphWrapper().getIdentifier(cls);
            String label = cioWrapper.getOWLGraphWrapper().getLabel(cls);
            String description = cioWrapper.getOWLGraphWrapper().getDef(cls);
            if (StringUtils.isBlank(id) || StringUtils.isBlank(label) || 
                    StringUtils.isBlank(description)) {
                throw log.throwing(new IllegalArgumentException("Missing ID, label "
                        + "or description for a class in the provided ontology, "
                        + "offending class: " + cls));
            }
            
            boolean isTrusted = !cioWrapper.isBgeeNotTrustedStatement(cls);
            
            //confidence level
            //there is none for strongly conflicting statements
            ConfidenceLevel confLevel = null;
            if (!cioWrapper.isStronglyConflicting(cls)) {
                confLevel = ConfidenceLevel.convertToConfidenceLevel(
                         cioWrapper.getOWLGraphWrapper().getLabel(
                                 cioWrapper.getConfidenceLevel(cls)));
            }
            
            OWLClass evidenceConcordCls = cioWrapper.getEvidenceConcordance(cls);
            EvidenceConcordance evidenceConcordance = 
                    EvidenceConcordance.convertToEvidenceConcordance(
                            cioWrapper.getOWLGraphWrapper().getLabel(evidenceConcordCls));
            
            //evidence type concordance
            //there is none for single evidence statements
            EvidenceTypeConcordance evidenceTypeConcordance = null;
            if (!cioWrapper.isSingleEvidenceConcordance(evidenceConcordCls)) {
                evidenceTypeConcordance = 
                        EvidenceTypeConcordance.convertToEvidenceTypeConcordance(
                            cioWrapper.getOWLGraphWrapper().getLabel(
                                    cioWrapper.getEvidenceTypeConcordance(cls)));
            }
            
            CIOStatementTO newTO = new CIOStatementTO(id, label, description, isTrusted, 
                    confLevel, evidenceConcordance, evidenceTypeConcordance);
            log.trace("Generating TO: {}", newTO);
            cioTOs.add(newTO);
        }
        
        if (cioTOs.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("The provided ontology "
                    + "did not allow to retrieve any valid CIO statements."));
        }
        return log.exit(cioTOs);
    }
}
