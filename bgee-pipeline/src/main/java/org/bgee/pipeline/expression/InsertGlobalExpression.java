package org.bgee.pipeline.expression;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTOResultSet;
import org.bgee.model.dao.api.expressiondata.ExpressionCallParams;
import org.bgee.model.dao.api.ontologycommon.RelationDAO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationType;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTOResultSet;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTOResultSet;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.pipeline.MySQLDAOUser;


/**
 * Class responsible for inserting the global expression into the Bgee database.
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @since Bgee 13
 */
public class InsertGlobalExpression extends MySQLDAOUser {

    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(InsertGlobalExpression.class.getName());

    /**
     * A {@code Set} of {@code String}s containing species IDs of the Bgee database. See this
     * method for details.
     * 
     * @see #loadSpeciesIdsFromDb()
     */
    private Set<String> speciesIdsInBgee;

    /**
     * Default constructor. 
     */
    public InsertGlobalExpression() {
        super();
    }

    /**
     * Constructor providing the {@code MySQLDAOManager} that will be used by 
     * this object to perform queries to the database. This is useful for unit testing.
     * 
     * @param manager   the {@code MySQLDAOManager} to use.
     */
    public InsertGlobalExpression(MySQLDAOManager manager) {
        super(manager);
    }


    /**
     * Main method to trigger the generate TSV download files (simple and complete files) from Bgee 
     * database. Parameters that must be provided in order in {@code args} are: 
     * <ol>
     * <li>path to the single download file to generate.
     * <li>path to the complete download file to generate.
     * </ol>
     * 
     * @param args          An {@code Array} of {@code String}s containing the requested parameters.
     * @throws IOException
     */
    public static void main(String[] args) {
        log.entry((Object[]) args);

        int expectedArgLength = 0;
        if (args.length != expectedArgLength) {
            throw log.throwing(new IllegalArgumentException("Incorrect number of arguments " +
                    "provided, expected " + expectedArgLength + " arguments, " + args.length + 
                    " provided."));
        }
        
        InsertGlobalExpression insert = new InsertGlobalExpression();
        insert.insert();
        
        log.exit();
    }

    public void insert() {
        log.entry();

        // Retrieve species IDs of the Bgee database to be able to one species by one.
        this.loadSpeciesIdsFromDb();

        Set<ExpressionCallTO> expressionTOs = new HashSet<ExpressionCallTO>();
        for (String species: speciesIdsInBgee) {
            // Retrieve all expression rows of the current species, with all fields
            Set<ExpressionCallTO> expTOs = this.loadExpressionCallFromDb(species);

            // Retrieve all relations (as RelationTOs) with relation type as "is_a part_of" 
            // between anatomical structures of this species
            Set<RelationTO> relationTOs = this.loadAnatEntityRelationFromDb(species);

            // For each expression row, propagate to parents
            //TODO Indeed, I didn't arrive up to there...
            expressionTOs.addAll(this.generateGlobalExpressionTOs(expTOs, relationTOs));
        }

        int nbInsertedExpressions = 0;
        try {
            this.startTransaction();

            log.info("Start inserting of global expressions...");

            nbInsertedExpressions= this.getExpressionCallDAO().
                    insertExpressionCalls(expressionTOs);

            this.commit();
        } finally {
            this.closeDAO();
        }
        log.info("Done inserting global expressions : {} global expression inserted.", 
                nbInsertedExpressions);
        log.exit();
    }

    /**
     * Retrieves all anatomical entity relations of a given species, present into the Bgee database.
     * 
     * @param species       A {@code String} that is the ID of species allowing to filter 
     *                      the calls to use
     * @return              A {@code Set} of {@code RelationTO}s containing all anatomical entity 
     *                      relations of the given species.
     * @throws DAOException If an error occurred while getting the data from the Bgee database.
     */
    private Set<RelationTO> loadAnatEntityRelationFromDb(String species) {
        log.entry();
        
        log.info("Start retrieving anatomical entity relations for {}...", species);
        
        RelationDAO dao = this.getRelationDAO();
        
        Set<String> speciesFilter = new HashSet<String>();
        speciesFilter.add(species);

        RelationTOResultSet rsRelations = dao.getAllAnatEntityRelations(
                speciesFilter, EnumSet.of(RelationType.ISA_PARTOF));
        
        Set<RelationTO> relationTOs = new HashSet<RelationTO>();
        while (rsRelations.next()) {
            relationTOs.add(rsRelations.getTO());
        }
        if (log.isInfoEnabled()) {
            log.info("Done retrieving anatomical entity relations, {} relations calls found",
                    relationTOs.size());
        }

        return log.exit(relationTOs);        
    }

    /**
     * Retrieves all expression calls of a given species, present into the Bgee database.
     * 
     * @param species       A {@code String} that is the ID of species allowing to filter 
     *                      the calls to use
     * @return              A {@code Set} of {@code ExpressionCallTO}s containing all expression 
     *                      calls of the given species.
     * @throws DAOException If an error occurred while getting the data from the Bgee database.
     */
    private Set<ExpressionCallTO> loadExpressionCallFromDb(String species) throws DAOException {
        log.entry();
        
        log.info("Start retrieving expression calls for {}...", species);
        
        ExpressionCallDAO dao = this.getExpressionCallDAO();
        
        ExpressionCallParams params = new ExpressionCallParams();
        params.addAllSpeciesIds(Arrays.asList(species));

        ExpressionCallTOResultSet rsExpressionCalls = dao.getAllExpressionCalls(params);
        
        Set<ExpressionCallTO> exprTOs = new HashSet<ExpressionCallTO>();
        while (rsExpressionCalls.next()) {
            exprTOs.add(rsExpressionCalls.getTO());
        }
        if (log.isInfoEnabled()) {
            log.info("Done retrieving expression calls, {} expression calls found", exprTOs.size());
        }

        return log.exit(exprTOs);        
    }

    /**
     * Retrieves all species IDs present into the Bgee database.
     * 
     * @throws DAOException If an error occurred while getting the data from the Bgee database.
     */
    private void loadSpeciesIdsFromDb() throws DAOException {
        log.entry();
        
        log.info("Start retrieving species IDs...");
        SpeciesDAO dao = this.getSpeciesDAO();
        dao.setAttributes(SpeciesDAO.Attribute.ID);
        SpeciesTOResultSet rsSpecies = dao.getAllSpecies();
        while (rsSpecies.next()) {
            this.speciesIdsInBgee.add(rsSpecies.getTO().getId());
        }
        if (log.isInfoEnabled()) {
            log.info("Done retrieving speciesIDs, {} genes found", this.speciesIdsInBgee.size());
        }

        log.exit();        
    }

    private Set<ExpressionCallTO> generateGlobalExpressionTOs(
            Set<ExpressionCallTO> exprTOs, Set<RelationTO>relTOs) {
        // TODO Auto-generated method stub
        log.entry();
        return log.exit(null);        
    }

}
