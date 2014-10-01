package org.bgee.pipeline.expression;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTOResultSet;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.GlobalExpressionToExpressionTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallParams;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.GlobalNoExpressionToNoExpressionTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTOResultSet;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallParams;
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
public class InsertCallPropagation extends MySQLDAOUser {

    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(InsertCallPropagation.class.getName());
    
    /**
     * An {@code int} that is a unique ID for each global expression calls.
     */
    private int globalExprId;

    /**
     * An {@code int} that is a unique ID for each global no-expression calls.
     */
    private int globalNoExprId;

    /**
     * Default constructor. 
     */
    public InsertCallPropagation() {
        this(null);
    }

    /**
     * Constructor providing the {@code MySQLDAOManager} that will be used by 
     * this object to perform queries to the database. This is useful for unit testing.
     * 
     * @param manager   the {@code MySQLDAOManager} to use.
     */
    public InsertCallPropagation(MySQLDAOManager manager) {
        super(manager);
        this.globalExprId = 1;
        this.globalNoExprId = 1;
    }

    /**
     * Main method to insert global expression or no-expression in Bgee database. 
     * Parameters that must be provided in order in {@code args} are: 
     * <ol>
     * <li> boolean defining whether the propagation is for expression or no-expression. 
     * If {@code true}, the propagation will be done for no-expression calls. 
     * <li> a list of species IDs that will be used to propagate (non-)expression, separated by the 
     * {@code String} {@link CommandRunner#LIST_SEPARATOR}. If it is not provided, all species 
     * contained in database will be used.
     * </ol>
     * 
     * @param args           An {@code Array} of {@code String}s containing the requested parameters.
     * @throws DAOException  If an error occurred while inserting the data into the Bgee database.
     */
    public static void main(String[] args) throws DAOException {
        log.entry((Object[]) args);
        
        int expectedArgLengthWithoutSpecies = 1;
        int expectedArgLengthWithSpecies = 2;

        if (args.length != expectedArgLengthWithSpecies ||
                args.length != expectedArgLengthWithoutSpecies) {
            throw log.throwing(new IllegalArgumentException("Incorrect number of arguments " +
                    "provided, expected " + expectedArgLengthWithoutSpecies + " or " + 
                    expectedArgLengthWithSpecies + " arguments, " + args.length + 
                    " provided."));
        }
        
        boolean isNoExpression = Boolean.valueOf(args[0]);
        
        List<String> speciesIds = null;
        if (args.length == expectedArgLengthWithSpecies) {
            speciesIds = CommandRunner.parseListArgument(args[1]);    
        }
        
        InsertCallPropagation insert = new InsertCallPropagation();
        insert.insert(speciesIds, isNoExpression);
        
        log.exit();
    }

    /**
     * Inserts the global expression or no-expression calls into the Bgee database.
     * 
     * @param speciesIds       A {@code Set} of {@code String}s containing species IDs that will 
     *                         be used to propagate (non-)expression.
     * @param isNoExpression   A {@code boolean} defining whether we propagate expression or 
     *                         no-expression. If {@code true}, the propagation will be done for 
     *                         no-expression calls.
     * @throws DAOException    If an error occurred while inserting the data into the Bgee database.
     */
    public void insert(List<String> speciesIds, boolean isNoExpression) throws DAOException {
        log.entry(speciesIds, isNoExpression);

        if (speciesIds == null || speciesIds.size() == 0) {
            // Retrieve species IDs of the Bgee database to be able to one species by one.
            speciesIds = this.loadSpeciesIdsFromDb();
        }
        
        try {
            for (String speciesId: speciesIds) {
                
                Set<String> speciesFilter = new HashSet<String>();
                speciesFilter.add(speciesId);

                // Retrieve all relations (as RelationTOs) with relation type as "is_a part_of" 
                // between anatomical structures of this species, source and target fields only.
                List<RelationTO> relationTOs = this.loadAnatEntityRelationFromDb(speciesFilter);
                
                if (isNoExpression) {
                    // Retrieve all no-expression calls of the current species.
                    List<NoExpressionCallTO> noExpressionTOs = 
                            this.loadNoExpressionCallFromDb(speciesFilter);

                  List<String> anatomicalEntityFilter = 
                          this.loadGlobalExprAnatomicalEntitiesFromDb(speciesFilter);

                  // For each expression row, propagate to children.
                  Map<NoExpressionCallTO, Set<NoExpressionCallTO>> globalNoExprMap =
                          this.generateGlobalNoExpressionTOs(noExpressionTOs, relationTOs, 
                                  anatomicalEntityFilter);
                    
                  // Generate the globalNoExprToNoExprTOs.
                  Set<GlobalNoExpressionToNoExpressionTO> globalNoExprToNoExprTOs = 
                          this.generateGlobalCallToCallTOs(globalNoExprMap, 
                                  GlobalNoExpressionToNoExpressionTO.class);

                  int nbInsertedNoExpressions = 0;
                  int nbInsertedGlobalNoExprToNoExpr = 0;

                  this.startTransaction();

                  log.info("Start inserting of global no-expression calls for {}...", speciesId);
                  nbInsertedNoExpressions += this.getNoExpressionCallDAO().
                          insertNoExpressionCalls(globalNoExprMap.keySet());
                  log.info("Done inserting of global no-expression calls for {}.", speciesId);

                  log.info("Start inserting of relation between a no-expression call " +
                          "and a global no-expression call for {}...", speciesId);
                  nbInsertedGlobalNoExprToNoExpr += this.getNoExpressionCallDAO().
                          insertGlobalNoExprToNoExpr(globalNoExprToNoExprTOs);
                  log.info("Done inserting of correspondances between a no-expression call " +
                          "and a global no-expression call for {}.", speciesId);

                  this.commit();

                  log.info("Done inserting for {}: {} global no-expression calls inserted " +
                          "and {} correspondances inserted", speciesId, 
                          nbInsertedNoExpressions, nbInsertedGlobalNoExprToNoExpr);
                } else {
                    // Retrieve all expression calls of the current species.
                    List<ExpressionCallTO> expressionTOs = 
                            this.loadExpressionCallFromDb(speciesFilter);

                    // For each expression row, propagate to parents.
                    Map<ExpressionCallTO, Set<ExpressionCallTO>> globalExprMap =
                            this.generateGlobalExpressionTOs(expressionTOs, relationTOs);

                    // Generate the globalExprToExprTOs.
                    Set<GlobalExpressionToExpressionTO> globalExprToExprTOs = 
                            this.generateGlobalCallToCallTOs(globalExprMap, 
                                    GlobalExpressionToExpressionTO.class);

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
                            "and {} correspondances inserted.", speciesId, 
                            nbInsertedExpressions, nbInsertedGlobalExprToExpr);
                }
            }
        } finally {
            this.closeDAO();
        }

        log.exit();
    }

    /**
     * Retrieves all species IDs present into the Bgee database.
     * 
     * @return A {@code Set} of {@code String}s containing species IDs of the Bgee database.
     * @throws DAOException If an error occurred while getting the data from the Bgee database.
     */
    private List<String> loadSpeciesIdsFromDb() throws DAOException {
        log.entry();
        
        log.info("Start retrieving species IDs...");

        SpeciesDAO dao = this.getSpeciesDAO();
        dao.setAttributes(SpeciesDAO.Attribute.ID);
        
        SpeciesTOResultSet rsSpecies = dao.getAllSpecies();
        List<String> speciesIdsInBgee = new ArrayList<String>();
        while (rsSpecies.next()) {
            speciesIdsInBgee.add(rsSpecies.getTO().getId());
        }
        
        log.info("Done retrieving species IDs, {} species found", speciesIdsInBgee.size());
    
        return log.exit(speciesIdsInBgee);        
    }

    /**
     * Retrieves all anatomical entity relations for given species, present into the Bgee database.
     * 
     * @param speciesIds    A {@code Set} of {@code String}s that are the IDs of species 
     *                      allowing to filter the anatomical entities to use.
     * @return              A {@code List} of {@code RelationTO}s containing source and target IDs 
     *                      of all anatomical entity relations of the given species.
     * @throws DAOException If an error occurred while getting the data from the Bgee database.
     */
    private List<RelationTO> loadAnatEntityRelationFromDb(Set<String> speciesIds) 
            throws DAOException {
        log.entry(speciesIds);
        
        log.info("Start retrieving anatomical entity relations for the species IDs {}...", 
                speciesIds);
        
        RelationDAO dao = this.getRelationDAO();
        dao.setAttributes(RelationDAO.Attribute.SOURCEID, RelationDAO.Attribute.TARGETID);
    
        RelationTOResultSet rsRelations = dao.getAllAnatEntityRelations(
                speciesIds, EnumSet.of(RelationType.ISA_PARTOF), 
                EnumSet.of(RelationStatus.DIRECT, RelationStatus.INDIRECT));
        
        List<RelationTO> relationTOs = dao.getAllTOs(rsRelations);
        
        log.info("Done retrieving anatomical entity relations, {} relations found",
                relationTOs.size());
    
        return log.exit(relationTOs);        
    }

    /**
     * Retrieves all expression calls for given species, present into the Bgee database.
     * 
     * @param speciesIds        A {@code Set} of {@code String}s that are the IDs of species 
     *                          allowing to filter the anatomical entities to use.
     * @return                  A {@code List} of {@code ExpressionCallTO}s containing all 
     *                          expression calls of the given species.
     * @throws DAOException     If an error occurred while getting the data from the Bgee database.
     */
    private List<ExpressionCallTO> loadExpressionCallFromDb(Set<String> speciesIds) 
            throws DAOException {
        log.entry(speciesIds);

        log.info("Start retrieving expression calls for the species IDs {}...", speciesIds);

        ExpressionCallDAO dao = this.getExpressionCallDAO();

        ExpressionCallParams params = new ExpressionCallParams();
        params.addAllSpeciesIds(speciesIds);

        ExpressionCallTOResultSet rsExpressionCalls = dao.getAllExpressionCalls(params);

        List<ExpressionCallTO> exprTOs = dao.getAllTOs(rsExpressionCalls);
        log.info("Done retrieving expression calls, {} calls found", exprTOs.size());

        return log.exit(exprTOs);        
    }

    /**
     * Retrieves all no-expression calls of a given species, present into the Bgee database.
     * 
     * @param speciesIds    A {@code Set} of {@code String}s that are the IDs of species 
     *                      allowing to filter the anatomical entities to use.
     * @return              A {@code List} of {@code NoExpressionCallTO}s containing all 
     *                      no-expression calls of the given species.
     * @throws DAOException If an error occurred while getting the data from the Bgee database.
     */
    private List<NoExpressionCallTO> loadNoExpressionCallFromDb(Set<String> speciesIds) 
            throws DAOException {
        log.entry(speciesIds);
        
        log.info("Start retrieving no-expression calls for the species IDs {}...", speciesIds);

        NoExpressionCallDAO dao = this.getNoExpressionCallDAO();

        NoExpressionCallParams params = new NoExpressionCallParams();
        params.addAllSpeciesIds(speciesIds);

        NoExpressionCallTOResultSet rsNoExpressionCalls = dao.getAllNoExpressionCalls(params);

        List<NoExpressionCallTO> noExprTOs = dao.getAllTOs(rsNoExpressionCalls);
        
        log.info("Done retrieving no-expression calls, {} calls found", 
                noExprTOs.size());

        return log.exit(noExprTOs);        
    }
    
    /**
     * Retrieves all anatomical entity IDs having at least one global expression call,
     * into the Bgee database.
     * 
     * @param speciesIds    A {@code Set} of {@code String}s that are the IDs of species 
     *                      allowing to filter the anatomical entities to use.
     * @return              A {@code List} of {@code String}s containing anatomical entity IDs
     *                      having at least a global expression call.
     * @throws DAOException If an error occurred while getting the data from the Bgee database.
     */
    private List<String> loadGlobalExprAnatomicalEntitiesFromDb(Set<String> speciesIds) 
            throws DAOException {
        log.entry(speciesIds);
        
        log.info("Start retrieving expression calls for the species IDs {}...", speciesIds);
        
        ExpressionCallDAO dao = this.getExpressionCallDAO();
        dao.setAttributes(ExpressionCallDAO.Attribute.ANATENTITYID);
        
        ExpressionCallParams params = new ExpressionCallParams();
        params.addAllSpeciesIds(speciesIds);
    
        ExpressionCallTOResultSet rsExpressionCalls = dao.getAllExpressionCalls(params);
        
        List<String> anatEntIdsInBgee = new ArrayList<String>();
        while (rsExpressionCalls.next()) {
            anatEntIdsInBgee.add(rsExpressionCalls.getTO().getAnatEntityId());
        }

        log.info("Done retrieving anatomical entity IDs having at least one global expression " +
                "call, {} entities found", anatEntIdsInBgee.size());
    
        return log.exit(anatEntIdsInBgee);        
    }

    /**
     * Generate the global expression calls from given expression calls filling the return 
     * {@code Map} associating each generated global expression call to expression calls used 
     * to generate it.
     * <p>
     * This method fills the map with generic global expression calls (which contain only 
     * gene ID, anatomical entity ID, and stage ID) as key and with all corresponding expression 
     * calls as values. Second, it merges expression calls and updates the global expression calls 
     * calling {@link #updateGlobalExpressions()}.
     * 
     * @param expressionTOs A {@code List} of {@code ExpressionCallTO}s containing 
     *                      all expression calls to propagate.
     * @param relationTOs   A {@code List} of {@code RelationTO}s containing source 
     *                      and target IDs of all anatomical entity relations.
     * @return              A {@code Map} associating generated global expression calls ( 
     *                      {@code ExpressionCallTO}s) to expression calls.
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
                    ExpressionCallTO.OriginOfLine.SELF);

            // Add the current expression call to the set with same 
            // gene ID, stage ID, and anatomical entity ID.
            log.trace("Add current expression: " + curExpression);
            Set<ExpressionCallTO> curExprAsSet = mapGlobalExpr.get(clearExpression);
            if (curExprAsSet == null) {
               curExprAsSet = new HashSet<ExpressionCallTO>();
               mapGlobalExpr.put(clearExpression, curExprAsSet);
            }
            curExprAsSet.add(curExpression);
            
            for (RelationTO curRelation : relationTOs) {
                log.trace("Propagation of the current expression for the relation: " + curRelation);
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
                            ExpressionCallTO.OriginOfLine.SELF);
                    
                    log.trace("Add the propagated expression: " + propagatedExpression);
                    curExprAsSet = mapGlobalExpr.get(propagatedExpression);
                    if (curExprAsSet == null) {
                       curExprAsSet = new HashSet<ExpressionCallTO>();
                       mapGlobalExpr.put(propagatedExpression, curExprAsSet);
                    }
                    curExprAsSet.add(curExpression);
                }
            }
        }

        this.updateGlobalExpressions(mapGlobalExpr, ExpressionCallTO.class);
        
        return log.exit(mapGlobalExpr);        
    }

    /**
     * Generate the global no-expression calls from given no-expression calls filling the return
     * {@code Map} associating each generated global no-expression calls to no-expression calls 
     * used to generate it.
     * <p>
     * This method fills the map with generic global no-expression calls (which contains only 
     * gene ID, anatomical entity ID, and stage ID) as key and with all corresponding no-expression 
     * calls as values. Second, it merges no-expression calls and updates the global no-expression
     * calls calling {@link #updateGlobalExpressions()}. The propagation is done only in anatomical 
     * entities having at least one global expression call to avoid having too many
     * propagated expression.
     * 
     * @param noExpressionTOs           A {@code List} of {@code NoExpressionCallTO}s containing 
     *                                  all no-expression calls to be propagated.
     * @param relationTOs               A {@code List} of {@code RelationTO}s containing source 
     *                                  and target IDs of all anatomical entity relations.
     * @param expressedAnatEntities     A {@code List} of {@code String}s containing anatomical 
     *                                  entity IDs having at least a global expression call.
     * @return                          A {@code Map} associating generated global no-expression 
     *                                  calls ({@code NoExpressionCallTO}s) to no-expression calls.
     */
    private Map<NoExpressionCallTO, Set<NoExpressionCallTO>>
            generateGlobalNoExpressionTOs(List<NoExpressionCallTO> noExpressionTOs,
                    List<RelationTO> relationTOs, List<String> expressedAnatEntities) {
        log.entry(noExpressionTOs, relationTOs, expressedAnatEntities);

        Map<NoExpressionCallTO, Set<NoExpressionCallTO>> mapGlobalNoExpr = 
                new HashMap<NoExpressionCallTO, Set<NoExpressionCallTO>>();

        for (NoExpressionCallTO curExpression : noExpressionTOs) {
            // Set ID to null to be able to compare keys of the map on 
            // gene ID, anatomical entity ID, and stage ID.
            NoExpressionCallTO clearExpression = new NoExpressionCallTO(
                    null, 
                    curExpression.getGeneId(),
                    curExpression.getAnatEntityId(),
                    curExpression.getStageId(),
                    DataState.NODATA,      
                    DataState.NODATA,
                    DataState.NODATA,
                    DataState.NODATA,
                    false,
                    NoExpressionCallTO.OriginOfLine.SELF);

            // Add the current no-expression call to the set with same 
            // gene ID, stage ID, and anatomical entity ID.
            log.trace("Add current no-expression: " + curExpression);
            Set<NoExpressionCallTO> curExprAsSet = mapGlobalNoExpr.get(clearExpression);
            if (curExprAsSet == null) {
               curExprAsSet = new HashSet<NoExpressionCallTO>();
               mapGlobalNoExpr.put(clearExpression, curExprAsSet);
            }
            curExprAsSet.add(curExpression);
            
            for (RelationTO curRelation : relationTOs) {
                log.trace("Propagation of the current no-expression for the relation: " + 
                        curRelation);
                if (curExpression.getAnatEntityId().equals(curRelation.getTargetId()) && 
                        expressedAnatEntities.contains(curRelation.getSourceId())) {
                    // Add propagated no-expression call (same gene ID and stage ID 
                    // but with anatomical entity ID of the current relation source ID) 
                    // only in anatomical entities having at least a global expression call.
                    NoExpressionCallTO propagatedExpression = new NoExpressionCallTO(
                            null, 
                            curExpression.getGeneId(),
                            curRelation.getSourceId(),
                            curExpression.getStageId(),
                            DataState.NODATA,      
                            DataState.NODATA,
                            DataState.NODATA,
                            DataState.NODATA,
                            false,
                            NoExpressionCallTO.OriginOfLine.SELF);

                    log.trace("Add the propagated no-expression: " + propagatedExpression);
                    curExprAsSet = mapGlobalNoExpr.get(propagatedExpression);
                    if (curExprAsSet == null) {
                       curExprAsSet = new HashSet<NoExpressionCallTO>();
                       mapGlobalNoExpr.put(propagatedExpression, curExprAsSet);
                    }
                    curExprAsSet.add(curExpression);
                }
            }
        }

        this.updateGlobalExpressions(mapGlobalNoExpr, NoExpressionCallTO.class);

        return log.exit(mapGlobalNoExpr);        
    }

    /**
     * Update global {@code T} calls of the given {@code Map} taking account {@code DataType}s and 
     * anatomical entity IDs of calls to generate {@code OrigineOfLine} of global calls. 
     * Are currently supported: {@code ExpressionCallTO.class}, {@code NoExpressionCallTO.class}.
     * 
     * @param globalMap         A {@code Map} associating generated global calls to calls
     *                          to be updated.
     * @param isNoExpression    A {@code boolean} defining whether it is propagation of expression 
     *                          or no-expression. If {@code true}, the propagation will be done for 
     *                          no-expression calls.
     * @param type              The desired {@code CallTO} type.
     * @param <T>               A {@code CallTO} type parameter.
     */
    private <T extends CallTO> void updateGlobalExpressions(Map<T, Set<T>> globalMap, Class<T> type) {
        log.entry(globalMap, type);
        
        // Create a Set from keySet to be able to modify globalMap.
        Set<T> tmpGlobalCalls = new HashSet<T>(globalMap.keySet());

        for (T globalCall: tmpGlobalCalls) {
            // Remove generic global call which contains only 
            // gene ID, anatomical entity ID, and stage ID
            Set<T> calls = globalMap.remove(globalCall);

            log.trace("Update global call (" + type.getSimpleName() + "): " + globalCall + 
                    " ; with: " + calls);
            
            // Define the best DataType of the global call according to all calls
            // and get anatomical entity IDs to be able to define OriginOfLine later.
            DataState affymetrixData = DataState.NODATA, estData = DataState.NODATA, 
                    relaxedinSituData = DataState.NODATA, inSituData = DataState.NODATA, 
                    rnaSeqData = DataState.NODATA;
            Set<String> anatEntityIDs = new HashSet<String>();
            for (T call: calls) {
                affymetrixData = getBestDataState(affymetrixData, call.getAffymetrixData());
                estData = getBestDataState(estData, call.getESTData());
                inSituData = getBestDataState(inSituData, call.getInSituData());
                relaxedinSituData = getBestDataState(relaxedinSituData, call.getRelaxedInSituData());
                rnaSeqData = getBestDataState(rnaSeqData, call.getRNASeqData());
                anatEntityIDs.add(call.getAnatEntityId());
            }

            if (type.equals(NoExpressionCallTO.class)) {
                // Define the OriginOfLine of the global no-expression call according to all calls
               NoExpressionCallTO.OriginOfLine origin = NoExpressionCallTO.OriginOfLine.PARENT;
                if (anatEntityIDs.contains(globalCall.getAnatEntityId())) {
                    if (anatEntityIDs.size() == 1) {
                        origin = NoExpressionCallTO.OriginOfLine.SELF;
                    } else {
                        origin = NoExpressionCallTO.OriginOfLine.BOTH;
                    }
                }
                
                NoExpressionCallTO updatedGlobalCall =
                        new NoExpressionCallTO(String.valueOf(this.globalNoExprId++), 
                                globalCall.getGeneId(), globalCall.getAnatEntityId(), 
                                globalCall.getStageId(), 
                                affymetrixData, inSituData, relaxedinSituData, rnaSeqData,
                                true, origin);
                
                log.trace("Updated global no-expression call: " + updatedGlobalCall);
                
                // Add the updated global no-expression call
                globalMap.put(type.cast(updatedGlobalCall), calls);
                
            } else if (type.equals(ExpressionCallTO.class)) {
                // Define the OriginOfLine of the global expression call according to all calls
                ExpressionCallTO.OriginOfLine origin = ExpressionCallTO.OriginOfLine.DESCENT;
                if (anatEntityIDs.contains(globalCall.getAnatEntityId())) {
                    if (anatEntityIDs.size() == 1) {
                        origin = ExpressionCallTO.OriginOfLine.SELF;
                    } else {
                        origin = ExpressionCallTO.OriginOfLine.BOTH;
                    }
                }
                ExpressionCallTO updatedGlobalCall =
                        new ExpressionCallTO(String.valueOf(this.globalExprId++), 
                                globalCall.getGeneId(), globalCall.getAnatEntityId(), 
                                globalCall.getStageId(), 
                                affymetrixData, estData, inSituData, rnaSeqData, true,
                                ((ExpressionCallTO) globalCall).isIncludeSubStages(), origin);
                
                log.trace("Updated global expression call: " + updatedGlobalCall);
                
                // Add the updated global expression call
                globalMap.put(type.cast(updatedGlobalCall), calls);
            } 
        }
        
        log.exit();
    }

    /**
     * Get the best {@code DataState} between two {@code DataState}s.
     * 
     * @param dataState1    A {@code DataState} to be compare to {@code dataState2}.
     * @param dataState2    A {@code DataState} to be compare to {@code dataState1}.
     * @return              The best {@code DataState} between {@code dataState1} 
     *                      and {@code dataState2}.
     */
    private DataState getBestDataState(DataState dataState1, DataState dataState2) {
        log.entry(dataState1, dataState2);
        
        if (dataState1.ordinal() < dataState2.ordinal()) {
            return log.exit(dataState2);
        }
        
        return log.exit(dataState1);
    }

    /**
     * Generate the {@code Set} of {@code V}s associating generated global calls to calls, 
     * according the given {@code Map}. Values will be casted to the same type as {@code type}. 
     * Are currently supported (for {@code V}): {@code GlobalExpressionToExpressionTO.class}, 
     * {@code GlobalNoExpressionToNoExpressionTO.class}.
     * 
     * @param globalExprMap A {@code Map} associating generated global calls to calls to be 
     *                      inserted into Bgee database.
     * @param type          The desired returned type of values.
     * @return              A {@code Set} of {@code V}s containing associations between global calls 
     *                      to calls to be inserted into the Bgee database.
     * @param <T>           A {@code CallTO} type parameter.
     * @param <V>           A {@code TransferObject} type parameter.
     */
    private <T extends CallTO, V extends TransferObject> Set<V> generateGlobalCallToCallTOs(
            Map<T, Set<T>> globalExprMap, Class<V> type) {
        log.entry(globalExprMap, type);
        
        Set<V> globalExprToExprTOs = new HashSet<V>();
        for (Entry<T, Set<T>> entry: globalExprMap.entrySet()) {
            for (T expression : entry.getValue()) {
                if (type.equals(GlobalExpressionToExpressionTO.class)) {
                    globalExprToExprTOs.add(type.cast(new GlobalExpressionToExpressionTO(
                            expression.getId(), entry.getKey().getId())));
                } else if (type.equals(GlobalNoExpressionToNoExpressionTO.class)) {
                    globalExprToExprTOs.add(type.cast(new GlobalNoExpressionToNoExpressionTO(
                            expression.getId(), entry.getKey().getId())));
                } else {
                    throw log.throwing(new IllegalArgumentException("There is no propagation " +
                        "implemented for TransferObject " + expression.getClass() + "."));
                }
            }
        }

        return log.exit(globalExprToExprTOs);
    }
}
