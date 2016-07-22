package org.bgee.pipeline.gene;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.gene.GeneOntologyDAO.GOTermTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.pipeline.MySQLDAOUser;
import org.bgee.pipeline.ontologycommon.OntologyUtils;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import owltools.graph.OWLGraphEdge;
import owltools.graph.OWLGraphManipulator;
import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLQuantifiedProperty.Quantifier;

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
     * @throws IllegalArgumentException     If the arguments provided invalid information.
     * @throws DAOException                 If an error occurred while inserting 
     *                                      the data into the Bgee database.
     */
    public void insert(OWLOntology geneOntology) 
            throws IllegalArgumentException, DAOException {
        log.entry(geneOntology);
        log.info("Starting insertion of GO ontology...");
        
        //catch any IllegalStateException to wrap it into a IllegalArgumentException 
        //(a IllegalStateException would be generated because the OWLOntology loaded 
        //from the file would be invalid, so it would be a wrong argument)
        try {
            OWLGraphWrapper goWrapper = new OWLGraphWrapper(geneOntology);
            //get the GOTermTOs 
            Set<GOTermTO> goTermTOs = this.getGOTermTOs(goWrapper);
            //get the RelationTOs
            Set<RelationTO> relTOs = this.getRelationTOs(goWrapper);
                        
            //now we start a transaction to insert GOTermTOs in the Bgee data source.
            //note that we do not need to call rollback if an error occurs, calling 
            //closeDAO will rollback any ongoing transaction
            try {
                this.startTransaction();
                
                log.info("Start inserting of terms...");
                this.getGeneOntologyDAO().insertTerms(goTermTOs);
                log.info("Done inserting terms");
                
                log.info("Start inserting relations...");
                this.getRelationDAO().insertGeneOntologyRelations(relTOs);
                log.info("Done inserting relations.");
                
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
        log.entry(goWrapper);
        log.info("Retrieving terms...");
        
        Set<GOTermTO> goTermTOs = new HashSet<GOTermTO>();
        for (OWLClass goTerm: goWrapper.getAllRealOWLClasses()) {
            goTermTOs.add(new GOTermTO(
                    goWrapper.getIdentifier(goTerm), 
                    goWrapper.getLabel(goTerm), 
                    this.namespaceToDomain(goWrapper.getNamespace(goTerm)), 
                    goWrapper.getAltIds(goTerm)));
        }
        
        log.info("Done retrieving terms.");
        return log.exit(goTermTOs);
    }
    
    /**
     * Extract the relations between terms from the Gene Ontology wrapped into 
     * {@code goWrapper}. All relations are retrieved, whether there are direct 
     * or indirect (for instance, if A is_a B is_a C, then 3 relations will be retrieved: 
     * the direct relations A is_a B and B is_a C, and the indirect relation, following 
     * standard composition rules, A is_a C). This will allow the application to retrieve 
     * all children of a term, even indirect, in a single query.
     * <p>
     * Note that only is_a and part_of relations are considered, and that we do not 
     * care about distinguishing them. So the ontology is simplified before retrieving 
     * the relations: all sub-relations of part_of are mapped to part_of, all relations 
     * that are neither part_of nor is_a are then removed.
     * 
     * @param goWrapper A {@code OWLGraphWrapper} wrapping the Gene Ontology.
     * @return          A {@code Set} of {@code RelationTO} corresponding to 
     *                  the simplified relations between terms, retrieved from 
     *                  {@code goWrapper}.
     */
    private Set<RelationTO> getRelationTOs(OWLGraphWrapper goWrapper) {
        log.entry(goWrapper);
        log.info("Retrieving relations between terms...");
        Set<RelationTO> rels = new HashSet<RelationTO>();
        
        //modify the GO to reduce the relations, to map sub-relations of part_of 
        //to part_of, and to keep only is_a and part_of relations in the ontology. 
        OWLGraphManipulator manipulator = new OWLGraphManipulator(goWrapper);
        manipulator.mapRelationsToParent(Arrays.asList(OntologyUtils.PART_OF_ID));
        manipulator.filterRelations(Arrays.asList(OntologyUtils.PART_OF_ID), true);
        //actually, we don't care about the relation reduction, because here 
        //the RelationTOs will not take into account the type of the relation, 
        //and we retrieve all relations, even indirect. So, redundant relations 
        //would appear on purpose anyway.
        //manipulator.reduceRelations();
        //manipulator.reducePartOfIsARelations();
        
        //to later check whether a relation is a part_of relation
        OWLObjectProperty partOf = 
                goWrapper.getOWLObjectPropertyByIdentifier(OntologyUtils.PART_OF_ID);
        
        //now we get each term relations.
        for (OWLClass goTerm: goWrapper.getAllRealOWLClasses()) {
            Set<OWLGraphEdge> edges = goWrapper.getOutgoingEdgesNamedClosureOverSupProps(goTerm);
            //generate the RelationTOs from the OWLGraphEdges. For the GO, we do not 
            //care about the exact type of the relation, and whether the relations 
            //are direct or indirect.
            for (OWLGraphEdge edge: edges) {
                //we consider the edge only if there is only one QuantifiedProperty at most
                //(otherwise it means the relation could not be reduced, meaning 
                //there is no "actual" relations between the terms)
                if (edge.getQuantifiedPropertyList().size() > 1) {
                    log.trace("Skipping edge with more than 1 QuantifiedProperty: {}", 
                            edge);
                    continue;
                }
                //to make sure that this edge is a is_a or part_of relation
                if ((edge.getSingleQuantifiedProperty().getProperty() == null && 
                  edge.getSingleQuantifiedProperty().getQuantifier() == Quantifier.SUBCLASS_OF) || 
                  edge.getSingleQuantifiedProperty().getProperty().equals(partOf)) {
                    
                    RelationTO rel = new RelationTO(goWrapper.getIdentifier(edge.getSource()), 
                            goWrapper.getIdentifier(edge.getTarget()));
                    rels.add(rel);
                    log.debug("Adding relation: {}", rel);
                } else {
                    throw log.throwing(new AssertionError("A relation that is neither is_a " +
                    		"nor part_of is still present in the ontology: " + edge));
                }
            }
        }
        
        log.info("Done retrieving relations between terms.");
        return log.exit(rels);
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
