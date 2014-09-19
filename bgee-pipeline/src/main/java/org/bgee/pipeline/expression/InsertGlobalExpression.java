package org.bgee.pipeline.expression;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
     * A {@code Map} associating generated global expression calls as {@code ExpressionCallTO}s to
     * expression calls as {@code ExpressionCallTO}s to be inserted into 
     * globalExpressionToExpression table of the Bgee database.
     * 
     * @see #generateGlobalExpressionTOs()
     */
    private Map<ExpressionCallTO, Set<ExpressionCallTO>> mapGlobalExpr;

    /**
     * A {@code Set} of {@code GlobalExpressionToExpressionTO} containing 
     * {@code GlobalExpressionToExpressionTO}s to be inserted into the Bgee database.
     * 
     * @see #generateGlobalExprToExprTOs()
     */
    private Set<GlobalExpressionToExpressionTO> globalExprToExprTOs;

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
        this.mapGlobalExpr = new HashMap<ExpressionCallTO, Set<ExpressionCallTO>>();
        this.globalExprToExprTOs = new HashSet<GlobalExpressionToExpressionTO>();
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

    /**
     * Inserts the global expression calls into the Bgee database.
     * 
     * @throws DAOException   If an error occurred while inserting the data into the Bgee database.
     */
    public void insert() throws DAOException {
        log.entry();

        // Retrieve species IDs of the Bgee database to be able to one species by one.
        Set<String> speciesIdsInBgee = this.loadSpeciesIdsFromDb();

        for (String species: speciesIdsInBgee) {
            // Retrieve all expression calls of the current species, with all fields.
            Set<ExpressionCallTO> expressionTOs = this.loadExpressionCallFromDb(species);

            // Retrieve all relations (as RelationTOs) with relation type as "is_a part_of" 
            // between anatomical structures of this species, source and target fields only.
            Set<RelationTO> relationTOs = this.loadAnatEntityRelationFromDb(species);

            // For each expression row, propagate to parents.
            this.generateGlobalExpressionTOs(expressionTOs, relationTOs);
        }

        // Generate IDs of global expression calls and the globalExprToExprTO
        this.generateGlobalExprToExprTOs();
        
        int nbInsertedExpressions = 0;
        int nbInsertedGlobalExprToExpr = 0;
        try {
            this.startTransaction();

            log.info("Start inserting of global expression calls...");
            nbInsertedExpressions = this.getExpressionCallDAO().
                    insertExpressionCalls(this.mapGlobalExpr.keySet());
            log.info("Done inserting of global expression calls.");

            log.info("Start inserting of relation between an expression call " +
                        "and a global expression call ...");
            nbInsertedGlobalExprToExpr = this.getExpressionCallDAO().
                    insertGlobalExpressionToExpression(globalExprToExprTOs);
            log.info("Done inserting of correspondances between an expression call " +
                    "and a global expression call ...");

            this.commit();

            log.info("Done inserting: {} global expression calls inserted " +
                    "and {} correspondances inserted", nbInsertedExpressions, nbInsertedGlobalExprToExpr);
        
        } finally {
            this.closeDAO();
        }
        log.exit();
    }

    /**
     * TODO Javadoc
     */
    private void generateGlobalExprToExprTOs() {
        log.entry();
        
        int globalExprId = 1;
        Set<ExpressionCallTO> tmpGlobalExpressions = new HashSet<ExpressionCallTO>(this.mapGlobalExpr.keySet());

        for (ExpressionCallTO globalExpr: tmpGlobalExpressions) {
            ExpressionCallTO finalGlobalExprTO = new ExpressionCallTO(String.valueOf(globalExprId), 
                    globalExpr.getGeneId(),
                    globalExpr.getAnatEntityId(),
                    globalExpr.getStageId(),
                    globalExpr.getAffymetrixData(),
                    globalExpr.getESTData(),
                    globalExpr.getInSituData(),
                    globalExpr.getRNASeqData(),
                    globalExpr.isIncludeSubstructures(),
                    globalExpr.isIncludeSubStages(),
                    globalExpr.getOriginOfLine());
            
            this.mapGlobalExpr.put(
                    finalGlobalExprTO,
                    this.mapGlobalExpr.remove(globalExpr));

            for (ExpressionCallTO expression : this.mapGlobalExpr.get(finalGlobalExprTO)) {
                this.globalExprToExprTOs.add(new GlobalExpressionToExpressionTO(
                        expression.getId(), finalGlobalExprTO.getId()));
            }
            globalExprId++;
        }
        
        log.exit();
    }

    /**
     * Retrieves all species IDs present into the Bgee database.
     * 
     * @return      A {@code Set} of {@code String}s containing species IDs of the Bgee database.
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
        if (log.isInfoEnabled()) {
            log.info("Done retrieving speciesIDs, {} genes found", speciesIdsInBgee.size());
        }
    
        return log.exit(speciesIdsInBgee);        
    }

    /**
     * Retrieves all expression calls of a given species, present into the Bgee database.
     * 
     * @param speciesId       A {@code String} that is the ID of species allowing to filter 
     *                      the calls to use
     * @return              A {@code Set} of {@code ExpressionCallTO}s containing all expression 
     *                      calls of the given species.
     * @throws DAOException If an error occurred while getting the data from the Bgee database.
     */
    private Set<ExpressionCallTO> loadExpressionCallFromDb(String speciesId) throws DAOException {
        log.entry();
        
        log.info("Start retrieving expression calls for the species ID {}...", speciesId);
        
        ExpressionCallDAO dao = this.getExpressionCallDAO();
        
        ExpressionCallParams params = new ExpressionCallParams();
        params.addAllSpeciesIds(Arrays.asList(speciesId));
    
        ExpressionCallTOResultSet rsExpressionCalls = dao.getAllExpressionCalls(params);
       
        log.debug("rsExpressionCalls:"+rsExpressionCalls);
        
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
     * Retrieves all anatomical entity relations of a given species, present into the Bgee database.
     * 
     * @param species       A {@code String} that is the ID of species allowing to filter 
     *                      the calls to use
     * @return              A {@code Set} of {@code RelationTO}s containing source and target IDs 
     *                      of all anatomical entity relations of the given species.
     * @throws DAOException If an error occurred while getting the data from the Bgee database.
     */
    private Set<RelationTO> loadAnatEntityRelationFromDb(String species) throws DAOException {
        log.entry();
        
        log.info("Start retrieving anatomical entity relations for the species ID {}...", species);
        
        RelationDAO dao = this.getRelationDAO();
        dao.setAttributes(Arrays.asList(
                RelationDAO.Attribute.SOURCEID, RelationDAO.Attribute.TARGETID));
        
        Set<String> speciesFilter = new HashSet<String>();
        speciesFilter.add(species);
    
        RelationTOResultSet rsRelations = dao.getAllAnatEntityRelations(
                speciesFilter, EnumSet.of(RelationType.ISA_PARTOF), 
                EnumSet.of(RelationStatus.DIRECT, RelationStatus.INDIRECT));
        
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
     * Generate the global expression calls from expression calls filling the {@code Map} 
     * associating generated global expression calls to expression calls used to generate it.
     * <p>
     * First, the method fills the map with generic global expression call (which contains only 
     * gene ID, anatomical entity ID, and stage ID) as key and as corresponding expression calls as 
     * values. Second, it merges expression calls and updates the global expression call calling
     * {@link #updateGlobalExpressions()}.
     * <p>
     * Warning: the global expression IDs are {@code null} because IDs will be generated during the 
     * insertion to be able to generate rows for GlobalExpressionToExpression table. 
     * 
     * @param expressionTOs                 A {@code Set} of {@code ExpressionCallTO}s containing 
     *                                      all expression calls to propagate.
     * @param relationTOs                   A {@code Set} of {@code RelationTO}s containing source 
     *                                      and target IDs of all anatomical entity relations.
     * @param nonInformativeAnatEntityTOs   A {@code Set} of {@code String}s containing all non 
     *                                      informative anatomical entity IDs.
     */
    private void generateGlobalExpressionTOs(
            Set<ExpressionCallTO> expressionTOs, Set<RelationTO> relationTOs) {
        log.entry(expressionTOs, relationTOs);
                
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
            this.addExpression(clearExpression, curExpression);
            
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
                    this.addExpression(propagatedExpression, curExpression);
                }
            }
        }
        this.updateGlobalExpressions();

        log.exit();        
    }

    /**
     * Add to a given global expression call a given {@code ExpressionCallTO} in the {@code Map} 
     * associating them.
     * 
     * @param key               An {@code ExpressionCallTO} that is the global expression call to 
     *                          use as key of the {@code Map}.
     * @param expressionToAdd   An {@code ExpressionCallTO} that is {@code ExpressionCallTO} to 
     *                          add in the {@code Map}.
     */
    private void addExpression(ExpressionCallTO key, ExpressionCallTO expressionToAdd) {
        log.entry(key, expressionToAdd);
        
        if (this.mapGlobalExpr.containsKey(key)) {
            this.mapGlobalExpr.get(key).add(expressionToAdd);
        } else {
            Set<ExpressionCallTO> curExprAsSet = new HashSet<ExpressionCallTO>();
            curExprAsSet.add(expressionToAdd);
            this.mapGlobalExpr.put(key, curExprAsSet);
        }

        log.exit();
    }

    /**
     * Update global expression calls of {@code mapGlobalExprToExpr} (keys) taking account 
     * {@code DataType}s and anatomical entity IDs of expression calls (to generate 
     * {@code OrigineOfLine} of global expression calls).    
     */
    private void updateGlobalExpressions() {
        log.entry();
        
        Set<ExpressionCallTO> tmpGlobalExpressions = new HashSet<ExpressionCallTO>(this.mapGlobalExpr.keySet());
        for (ExpressionCallTO globalExpression: tmpGlobalExpressions) {
            // Remove generic global expression which contains only 
            // gene ID, anatomical entity ID, and stage ID
            Set<ExpressionCallTO> expressions = this.mapGlobalExpr.remove(globalExpression);

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
            this.mapGlobalExpr.put(
                    new ExpressionCallTO(null, globalExpression.getGeneId(), 
                            globalExpression.getAnatEntityId(), globalExpression.getStageId(), 
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
