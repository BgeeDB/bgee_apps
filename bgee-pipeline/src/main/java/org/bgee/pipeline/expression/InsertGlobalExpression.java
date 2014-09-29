package org.bgee.pipeline.expression;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO.OriginOfLine;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTOResultSet;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.GlobalExpressionToExpressionTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallParams;
import org.bgee.model.dao.api.ontologycommon.RelationDAO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationStatus;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationType;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTOResultSet;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTOResultSet;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.pipeline.CommandRunner;
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
     * An {@code int} that is a unique ID for each global expression calls.
     */
    private int globalExprId;

    /**
     * Default constructor. 
     */
    public InsertGlobalExpression() {
        this(null);
    }

    /**
     * Constructor providing the {@code MySQLDAOManager} that will be used by 
     * this object to perform queries to the database. This is useful for unit testing.
     * 
     * @param manager   the {@code MySQLDAOManager} to use.
     */
    public InsertGlobalExpression(MySQLDAOManager manager) {
        super(manager);
        this.globalExprId = 1;
    }

    /**
     * Main method to insert global expression in Bgee database. No parameters must be provided.
     * 
     * @param args           An {@code Array} of {@code String}s containing the requested parameters.
     * @throws DAOException  If an error occurred while inserting the data into the Bgee database.
     */
    public static void main(String[] args) throws DAOException {
        log.entry((Object[]) args);

        int expectedArgLength = 1;
        if (args.length != expectedArgLength) {
            throw log.throwing(new IllegalArgumentException("Incorrect number of arguments " +
                    "provided, expected " + expectedArgLength + " arguments, " + args.length + 
                    " provided."));
        }
        
        Set<String> speciesIds = new HashSet<String>(CommandRunner.parseListArgument(args[0]));
        
        InsertGlobalExpression insert = new InsertGlobalExpression();
        insert.insert(speciesIds);
        
        log.exit();
    }

    /**
     * Inserts the global expression calls into the Bgee database.
     * 
     * @throws DAOException   If an error occurred while inserting the data into the Bgee database.
     */
    public void insert(Set<String> speciesIds) throws DAOException {
        log.entry();

        if (speciesIds.size() == 0 ) {
            // Retrieve species IDs of the Bgee database to be able to one species by one.
            speciesIds = this.loadSpeciesIdsFromDb();
        }

        try {
            for (String speciesId: speciesIds) {
                // Retrieve all expression calls of the current species, with all fields.
                List<ExpressionCallTO> expressionTOs = this.loadExpressionCallFromDb(speciesId);

                // Retrieve all relations (as RelationTOs) with relation type as "is_a part_of" 
                // between anatomical structures of this species, source and target fields only.
                List<RelationTO> relationTOs = this.loadAnatEntityRelationFromDb(speciesId);

                // For each expression row, propagate to parents.
                Map<ExpressionCallTO, Set<ExpressionCallTO>> globalExprMap = 
                        this.generateGlobalExpressionTOs(expressionTOs, relationTOs);

                // Generate IDs of global expression calls and the globalExprToExprTO
                Set<GlobalExpressionToExpressionTO> globalExprToExprTOs = 
                        this.generateGlobalExprToExprTOs(globalExprMap);

                int nbInsertedExpressions = 0;
                int nbInsertedGlobalExprToExpr = 0;

                this.startTransaction();

                log.info("Start inserting of global expression calls for {}...", speciesId);
                nbInsertedExpressions += this.getExpressionCallDAO().
                        insertExpressionCalls(globalExprMap.keySet());
                log.info("Done inserting of global expression calls for {}.", speciesId);

                log.info("Start inserting of relation between an expression call " +
                        "and a global expression call for {}...", speciesId);
                nbInsertedGlobalExprToExpr += this.getExpressionCallDAO().
                        insertGlobalExpressionToExpression(globalExprToExprTOs);
                log.info("Done inserting of correspondances between an expression call " +
                        "and a global expression call for {}.", speciesId);

                this.commit();

                log.info("Done inserting for {}: {} global expression calls inserted " +
                        "and {} correspondances inserted", speciesId, 
                        nbInsertedExpressions, nbInsertedGlobalExprToExpr);
            }
        } finally {
            this.closeDAO();
        }

        log.exit();
    }

    /**
     * Generate the {code GlobalExpressionToExpressionTO}s from the given {@code Map} 
     * associating generated global expression calls to expression calls used to generate it.
     * 
     * @param globalExprMap A {@code Map} associating generated global expression calls to
     *                      expression calls as {@code ExpressionCallTO}s to be inserted into 
     *                      globalExpressionToExpression table of the Bgee database.
     * @return              A {@code Set} of {@code GlobalExpressionToExpressionTO} containing 
     *                      {@code GlobalExpressionToExpressionTO}s to be inserted into the 
     *                      Bgee database.
     */
    private Set<GlobalExpressionToExpressionTO> generateGlobalExprToExprTOs(
            Map<ExpressionCallTO, Set<ExpressionCallTO>> globalExprMap) {
        log.entry(globalExprMap);
        
        Set<GlobalExpressionToExpressionTO> globalExprToExprTOs = new HashSet<GlobalExpressionToExpressionTO>();
        for (Entry<ExpressionCallTO, Set<ExpressionCallTO>> entry: globalExprMap.entrySet()) {
            for (ExpressionCallTO expression : entry.getValue()) {
                globalExprToExprTOs.add(new GlobalExpressionToExpressionTO(
                        expression.getId(), entry.getKey().getId()));
            }
        }
        
        return log.exit(globalExprToExprTOs);
    }

    /**
     * Retrieves all species IDs present into the Bgee database.
     * 
     * @return A {@code Set} of {@code String}s containing species IDs of the Bgee database.
     * @throws DAOException If an error occurred while getting the data from the Bgee database.
     */
    private Set<String> loadSpeciesIdsFromDb() throws DAOException {
        log.entry();
        
        Set<String> speciesIdsInBgee = new HashSet<String>();
        log.info("Start retrieving species IDs...");
        SpeciesDAO dao = this.getSpeciesDAO();
        dao.setAttributes(SpeciesDAO.Attribute.ID);
        SpeciesTOResultSet rsSpecies = dao.getAllSpecies();
        while (rsSpecies.next()) {
            speciesIdsInBgee.add(rsSpecies.getTO().getId());
        }
        log.info("Done retrieving speciesIDs, {} genes found", speciesIdsInBgee.size());
    
        return log.exit(speciesIdsInBgee);        
    }

    /**
     * Retrieves all expression calls of a given species, present into the Bgee database.
     * 
     * @param speciesId     A {@code String} that is the ID of species allowing to filter 
     *                      the calls to use.
     * @return              A {@code List} of {@code ExpressionCallTO}s containing all expression 
     *                      calls of the given species.
     * @throws DAOException If an error occurred while getting the data from the Bgee database.
     */
    private List<ExpressionCallTO> loadExpressionCallFromDb(String speciesId) throws DAOException {
        log.entry(speciesId);
        
        log.info("Start retrieving expression calls for the species ID {}...", speciesId);
        
        ExpressionCallDAO dao = this.getExpressionCallDAO();
        
        ExpressionCallParams params = new ExpressionCallParams();
        params.addAllSpeciesIds(Arrays.asList(speciesId));
    
        ExpressionCallTOResultSet rsExpressionCalls = dao.getAllExpressionCalls(params);
        
        List<ExpressionCallTO> exprTOs = dao.getAllTOs(rsExpressionCalls);
        log.info("Done retrieving expression calls, {} expression calls found", exprTOs.size());
    
        return log.exit(exprTOs);        
    }

    /**
     * Retrieves all anatomical entity relations of a given species, present into the Bgee database.
     * 
     * @param species       A {@code String} that is the ID of species allowing to filter 
     *                      the calls to use.
     * @return              A {@code List} of {@code RelationTO}s containing source and target IDs 
     *                      of all anatomical entity relations of the given species.
     * @throws DAOException If an error occurred while getting the data from the Bgee database.
     */
    private List<RelationTO> loadAnatEntityRelationFromDb(String species) throws DAOException {
        log.entry(species);
        
        log.info("Start retrieving anatomical entity relations for the species ID {}...", species);
        
        RelationDAO dao = this.getRelationDAO();
        dao.setAttributes(RelationDAO.Attribute.SOURCEID, RelationDAO.Attribute.TARGETID);
        
        Set<String> speciesFilter = new HashSet<String>();
        speciesFilter.add(species);
    
        RelationTOResultSet rsRelations = dao.getAllAnatEntityRelations(
                speciesFilter, EnumSet.of(RelationType.ISA_PARTOF), 
                EnumSet.of(RelationStatus.DIRECT, RelationStatus.INDIRECT));
        
        List<RelationTO> relationTOs = dao.getAllTOs(rsRelations);
        log.info("Done retrieving anatomical entity relations, {} relations calls found",
                relationTOs.size());
    
        return log.exit(relationTOs);        
    }

    /**
     * Generate the global expression calls from expression calls filling the {@code Map} 
     * associating generated global expression calls to expression calls used to generate it.
     * <p>
     * This method fills the map with generic global expression call (which contains only 
     * gene ID, anatomical entity ID, and stage ID) as key and with all corresponding expression 
     * calls as values. Second, it merges expression calls and updates the global expression calls 
     * calling {@link #updateGlobalExpressions()}.
     * 
     * @param expressionTOs                 A {@code Set} of {@code ExpressionCallTO}s containing 
     *                                      all expression calls to propagate.
     * @param relationTOs                   A {@code List} of {@code RelationTO}s containing source 
     *                                      and target IDs of all anatomical entity relations.
     * @param nonInformativeAnatEntityTOs   A {@code List} of {@code String}s containing all non 
     *                                      informative anatomical entity IDs.
     * @return  A {@code Map} associating generated global expression calls as 
     *          {@code ExpressionCallTO}s to expression calls as {@code ExpressionCallTO}s                                     
     */
    private Map<ExpressionCallTO, Set<ExpressionCallTO>> generateGlobalExpressionTOs(
            List<ExpressionCallTO> expressionTOs, List<RelationTO> relationTOs) {
        log.entry(expressionTOs, relationTOs);
                
        Map<ExpressionCallTO, Set<ExpressionCallTO>> mapGlobalExpr = 
                new HashMap<ExpressionCallTO, Set<ExpressionCallTO>>();

        for (ExpressionCallTO curExpression : expressionTOs) {
            // Set ID to null to be able to compare keys of the map on 
            // gene ID, anatomical entity ID, and stage ID.
            ExpressionCallTO clearExpression = new ExpressionCallTO(
                    null, 
                    curExpression.getGeneId(),
                    curExpression.getAnatEntityId(),
                    curExpression.getStageId(),
                    DataState.NODATA,      
                    DataState.NODATA,
                    DataState.NODATA,
                    DataState.NODATA,
                    false,
                    false,
                    OriginOfLine.SELF);

            // Add the current expression call to the set with same 
            // gene ID, stage ID, and anatomical entity ID.
            Set<ExpressionCallTO> curExprAsSet = mapGlobalExpr.get(clearExpression);
            if (curExprAsSet == null) {
               curExprAsSet = new HashSet<ExpressionCallTO>();
               mapGlobalExpr.put(clearExpression, curExprAsSet);
            }
            curExprAsSet.add(curExpression);
            
            for (RelationTO curRelation : relationTOs) {
                if (curExpression.getAnatEntityId().equals(curRelation.getSourceId())) {
                    // Add propagated expression call (same gene ID and stage ID 
                    // but with anatomical entity ID of the current relation target ID).
                    ExpressionCallTO propagatedExpression = new ExpressionCallTO(
                            null, 
                            curExpression.getGeneId(),
                            curRelation.getTargetId(),
                            curExpression.getStageId(),
                            DataState.NODATA,      
                            DataState.NODATA,
                            DataState.NODATA,
                            DataState.NODATA,
                            false,
                            false,
                            OriginOfLine.SELF);
                    curExprAsSet = mapGlobalExpr.get(propagatedExpression);
                    if (curExprAsSet == null) {
                       curExprAsSet = new HashSet<ExpressionCallTO>();
                       mapGlobalExpr.put(propagatedExpression, curExprAsSet);
                    }
                    curExprAsSet.add(curExpression);

                }
            }
        }

        this.updateGlobalExpressions(mapGlobalExpr);
        
        return log.exit(mapGlobalExpr);        
    }

    /**
     * Update global expression calls of the given {@code Map} (keys) taking account 
     * {@code DataType}s and anatomical entity IDs of expression calls (to generate 
     * {@code OrigineOfLine} of global expression calls).
     * 
     * @param globalExprMap A {@code Map} associating generated global expression calls as 
     *                      {@code ExpressionCallTO}s to expression calls as 
     *                      {@code ExpressionCallTO}s to be updated.                                     
     */
    private void updateGlobalExpressions(Map<ExpressionCallTO, Set<ExpressionCallTO>> globalExprMap) {
        log.entry(globalExprMap);
        
        // Create a Set from keySet to be able to modify mapGlobalExpr.
        Set<ExpressionCallTO> tmpGlobalExpressions = 
                new HashSet<ExpressionCallTO>(globalExprMap.keySet());

        for (ExpressionCallTO globalExpression: tmpGlobalExpressions) {
            // Remove generic global expression which contains only 
            // gene ID, anatomical entity ID, and stage ID
            Set<ExpressionCallTO> expressions = globalExprMap.remove(globalExpression);
            
            // Define the best DataType of the global expression call according to all expression 
            // calls and get all anatomical entity ID
            DataState affymetrixData = DataState.NODATA, estData = DataState.NODATA, 
                    inSituData = DataState.NODATA, rnaSeqData = DataState.NODATA;
            Set<String> anatEntityIDs = new HashSet<String>();
            for (ExpressionCallTO expression: expressions) {
                affymetrixData = getBestDataState(affymetrixData, expression.getAffymetrixData());
                estData = getBestDataState(estData, expression.getESTData());
                inSituData = getBestDataState(inSituData, expression.getInSituData());
                rnaSeqData = getBestDataState(rnaSeqData, expression.getRNASeqData());
                anatEntityIDs.add(expression.getAnatEntityId());
            }

            // Define the OriginOfLine of the global expression call
            OriginOfLine origin = OriginOfLine.DESCENT;
            if (anatEntityIDs.contains(globalExpression.getAnatEntityId())) {
                if (anatEntityIDs.size() == 1) {
                    origin = OriginOfLine.SELF;
                } else {
                    origin = OriginOfLine.BOTH;
                }
            }

            // Add the updated global expression call
            globalExprMap.put(
                    new ExpressionCallTO(String.valueOf(this.globalExprId++), 
                            globalExpression.getGeneId(), globalExpression.getAnatEntityId(), 
                            globalExpression.getStageId(), 
                            affymetrixData, estData, inSituData, rnaSeqData, 
                            true, globalExpression.isIncludeSubStages(), origin),
                    expressions);
        }
        
        log.exit();
    }

    /**
     * Get the best {@code DataState} between two {@code DataState}s.
     * 
     * @param dataState1    A {@code DataState} to be compare to {@code dataState2}
     * @param dataState2    A {@code DataState} to be compare to {@code dataState1}
     * @return              The best {@code DataState} between {@code dataState1} 
     *                      and {@code dataState2}
     */
    private DataState getBestDataState(DataState dataState1, DataState dataState2) {
        log.entry(dataState1, dataState2);
        if (dataState1.ordinal() < dataState2.ordinal()) {
            return log.exit(dataState2);
        }
        return log.exit(dataState1);
    }
}
