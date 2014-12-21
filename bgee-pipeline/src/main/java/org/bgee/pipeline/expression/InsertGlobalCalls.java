package org.bgee.pipeline.expression;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.pipeline.BgeeDBUtils;
import org.bgee.pipeline.CommandRunner;


/**
 * Class responsible for inserting the global expression into the Bgee database.
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @since Bgee 13
 */
public class InsertGlobalCalls extends CallUser {

    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(InsertGlobalCalls.class.getName());
    
    /**
     * A {@code String} that is the argument class for expression propagation.
     */
    public final static String EXPRESSION_ARG = "expression";

    /**
     * A {@code String} that is the argument class for no-expression propagation.
     */
    public final static String NOEXPRESSION_ARG = "no-expression";

    /**
     * Main method to insert global expression or no-expression in Bgee database, see 
     * {@link #insert(List, boolean)}
     * Parameters that must be provided in order in {@code args} are: 
     * <ol>
     * <li>A {@code String} defining whether the propagation is for expression or no-expression. 
     * If equals to {@code no-expression}, the propagation will be done for no-expression calls, 
     * if equals to  {@code expression}, the propagation will be done for expression calls.
     * <li> a list of NCBI species IDs (for instance, {@code 9606} for human) 
     * that will be used to propagate (non-)expression, separated by the 
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
    
        if (args.length != expectedArgLengthWithSpecies &&
                args.length != expectedArgLengthWithoutSpecies) {
            throw log.throwing(new IllegalArgumentException("Incorrect number of arguments " +
                    "provided, expected " + expectedArgLengthWithoutSpecies + " or " + 
                    expectedArgLengthWithSpecies + " arguments, " + args.length + 
                    " provided."));
        }
        
        boolean isNoExpression = args[0].equalsIgnoreCase(NOEXPRESSION_ARG);
        if (!isNoExpression && !args[0].equalsIgnoreCase(EXPRESSION_ARG)) {
            throw log.throwing(new IllegalArgumentException("Unrecognized argument: " + 
                    args[0]));
        }
        
        List<String> speciesIds = null;
        if (args.length == expectedArgLengthWithSpecies) {
            speciesIds = CommandRunner.parseListArgument(args[1]);    
        }
        
        InsertGlobalCalls insert = new InsertGlobalCalls();
        insert.insert(speciesIds, isNoExpression);
        
        log.exit();
    }

    /**
     * An {@code int} used to generate IDs of global expression or no-expression calls.
     */        
    private int globalId;
    /**
     * A {@code FilterNoExprCalls} used to make sure no-expression calls were filtered 
     * before propagating them. See {@link FilterNoExprCalls#filterNoExpressionCalls(List)} 
     * for more details.
     */
    private final FilterNoExprCalls filterNoExprCalls;

    /**
     * Default constructor, using a default {@code MySQLDAOManager} to perform queries 
     * on the data source, and a default {@code FilterNoExprCalls} for cleaning 
     * conflicting no-expression calls before propagation (see 
     * {@link #InsertGlobalCalls(MySQLDAOManager)} and #InsertGlobalCalls(FilterNoExprCalls)}).
     */
    public InsertGlobalCalls() {
        this((MySQLDAOManager) null);
    }
    /**
     * Constructor providing the {@code MySQLDAOManager} that will be used by 
     * this object to perform queries to the database. The default {@link FilterNoExprCalls} 
     * will be used for cleaning conflicting no-expression calls before propagation 
     * (see {@link #InsertGlobalCalls(FilterNoExprCalls)}).
     * 
     * @param manager   the {@code MySQLDAOManager} to use.
     */
    public InsertGlobalCalls(MySQLDAOManager manager) {
        this(new FilterNoExprCalls(manager));
    }

    /**
     * Constructor providing the {@code FilterNoExprCalls} used for cleaning 
     * conflicting no-expression calls before propagation. The {@code MySQLDAOManager} 
     * used to perform queries to the data source will also be retrieved 
     * from it (to make sure {@code filterNoExprCalls} and this object use the same). 
     * If {@code filterNoExprCalls} is {@code null}, or if it does not allow 
     * to retrieve a {@code MySQLDAOManager}, an {@code IllegalArgumentException} is thrown. 
     * If you need to use the default implementation of {@code FilterNoExprCalls}, 
     * use {@link #InsertGlobalCalls()} or {@link #InsertGlobalCalls(MySQLDAOManager)}.
     * 
     * @param filterNoExprCalls The {@code FilterNoExprCalls} used to make sure 
     *                          no-expression calls are filtered before propagating them, 
     *                          and that will also provide the {@code MySQLDAOManager} 
     *                          to be used by this object.
     * @throws IllegalArgumentException If {@code filterNoExprCalls} is {@code null} or 
     *                                  does not allow to retrieve a valid {@code MySQLDAOManager}.
     */
    public InsertGlobalCalls(FilterNoExprCalls filterNoExprCalls) {
        //we make this statement to not throw an NullPointerException here 
        //if filterNoExprCalls is null, but we will throw an IllegalArgumentException 
        //afterwards. 
        super((filterNoExprCalls != null) ? filterNoExprCalls.getManager(): null);
        if (filterNoExprCalls == null) {
            throw log.throwing(new IllegalArgumentException("Provided FilterNoExprCalls " +
            		"cannot be null."));
        }
        //also, filterNoExprCalls should already have a MySQLDAOManager instantiated, 
        //otherwise we cannot guarantee that filterNoExprCalls and this object will use 
        //the same. 
        if (filterNoExprCalls.getManager() == null) {
            throw log.throwing(new IllegalArgumentException("Provided FilterNoExprCalls " +
                    "should allow to retrieve a valid DAOManager."));
        }
        //so at this point, DAOManager of filterNoExprCalls and of this object should be 
        //the same...
        if (this.getManager() != filterNoExprCalls.getManager()) {
            throw log.throwing(new AssertionError("Provided FilterNoExprCalls " +
                    "and this object use a different DAOManager."));
        }
        this.filterNoExprCalls = filterNoExprCalls;
        this.globalId = 0;
    }

    /**
     * Inserts the global expression or no-expression calls into the Bgee database. 
     * <p>
     * Note that if {@code isNoExpression} is {@code true}, conflicting no-expression calls 
     * will be first filtered, to ensure a consistent propagation, 
     * using {@link FilterNoExprCalls#filterNoExpressionCalls(List)}}. This slows down 
     * the propagation a lot, and should be even slower if you have already filtered 
     * conflicting no-expression calls. But this is necessary, that's it. You can provide 
     * a custom implementation of {@code FilterNoExprCalls} at instantiation, or rely on 
     * the default one (recommended). 
     * 
     * @param speciesIds       A {@code Set} of {@code String}s containing species IDs that will 
     *                         be used to propagate (non-)expression.
     * @param isNoExpression   A {@code boolean} defining whether we propagate expression or 
     *                         no-expression. If {@code true}, the propagation will be done for 
     *                         no-expression calls.
     * @throws DAOException    If an error occurred while inserting the data into the Bgee database.
     * @throws IllegalArgumentException If a species ID does not correspond to any species 
     *                                  in the Bgee data source.
     */
    public void insert(List<String> speciesIds, boolean isNoExpression) throws DAOException {
        log.entry(speciesIds, isNoExpression);

        try {
            // Get the maximum of global call IDs to get start index for new inserted global calls. 
            if (isNoExpression) {
                this.globalId = this.getNoExpressionCallDAO().getMaxNoExpressionCallId(true) + 1;
            } else {
                this.globalId = this.getExpressionCallDAO().getMaxExpressionCallId(true) + 1;
            }

            //get all species in Bgee even if some species IDs were provided, 
            //to check user input.
            List<String> speciesIdsToUse = BgeeDBUtils.checkAndGetSpeciesIds(speciesIds, 
                    this.getSpeciesDAO());
            
            //retrieve IDs of all anatomical entities allowed for no-expression call 
            //propagation, see loadAllowedAnatEntities method for details. 
            //This is done once for all species, as we want all no-expression calls 
            //to be propagated in the same way for any species.
            Set<String> anatEntityFilter = null;
            if (isNoExpression) {
                anatEntityFilter = this.loadAllowedAnatEntities();
            }

            for (String speciesId: speciesIdsToUse) {

                Set<String> speciesFilter = new HashSet<String>();
                speciesFilter.add(speciesId);
                
                this.startTransaction();

                if (isNoExpression) {
                    log.info("Start propagating no-expression calls for species {}", 
                            speciesId);
                    
                    log.info("Cleaning conflicting no-expression calls before propagation...");
                    this.filterNoExprCalls.filterNoExpressionCalls(speciesId);
                    log.info("Done cleaning conflicting no-expression calls before propagation.");
                    
                    LinkedHashMap<String, List<NoExpressionCallTO>> noExprTOs = 
                            this.getNoExpressionCallsByGeneId(speciesFilter);
                    
                    Map<String, Set<String>> anatEntityChildrenFromParents = 
                            BgeeDBUtils.getAnatEntityChildrenFromParents(speciesFilter, 
                                    this.getRelationDAO());

                    int noExprCallsExamined = 0;
                    int nbInsertedNoExpressions = 0;
                    int nbInsertedGlobalNoExprToNoExpr = 0;
                    int geneCount = noExprTOs.size();
                    int geneIterated = 0;
                    Iterator<Entry<String, List<NoExpressionCallTO>>> noExprTOIterator = 
                            noExprTOs.entrySet().iterator();
                    while (noExprTOIterator.hasNext()) {
                        Entry<String, List<NoExpressionCallTO>> entry = noExprTOIterator.next();
                        geneIterated++;
                        noExprCallsExamined += entry.getValue().size();
                        if (log.isDebugEnabled() && geneIterated % 1000 == 0) {
                            log.debug("{}/{} genes examined.", geneIterated, geneCount);
                        }
                        // For each no-expression row, propagate to allowed children.
                        Map<NoExpressionCallTO, Set<String>> globalNoExprMap =
                                this.generateGlobalNoExpressionTOs(
                                        entry.getValue(), 
                                        anatEntityChildrenFromParents, 
                                        anatEntityFilter);

                        // Generate the globalNoExprToNoExprTOs.
                        Set<GlobalNoExpressionToNoExpressionTO> globalNoExprToNoExprTOs = 
                                this.generateGlobalCallToCallTOs(globalNoExprMap, 
                                        GlobalNoExpressionToNoExpressionTO.class);
                        
                        log.debug("Start inserting global no-expression calls for species {} and gene ID {}...", 
                                speciesId, entry.getKey());
                        int nbCurInsertedNoExpressions = this.getNoExpressionCallDAO().
                                insertNoExpressionCalls(globalNoExprMap.keySet());
                        if (nbCurInsertedNoExpressions != globalNoExprMap.keySet().size()) {
                            throw log.throwing(new AssertionError(
                                    "Global no-expression calls incorrectly inserted: " + 
                                     nbCurInsertedNoExpressions +" vs "+ 
                                     globalNoExprMap.keySet().size()));
                        }

                        log.debug("Done inserting of global no-expression calls for species {} and gene ID {}.",
                                speciesId, entry.getKey());

                        // Empty memory to free up some memory. We don't use clear() 
                        // because it empty ArgumentCaptor values in test in same time.
                        globalNoExprMap = null;
                        nbInsertedNoExpressions += nbCurInsertedNoExpressions;
                        
                        log.debug("Start inserting relations between no-expression calls " +
                                "and global no-expression calls for species {} and gene ID {}...", 
                                speciesId, entry.getKey());
                        int nbCurInsertedGlobalNoExprToNoExpr = this.getNoExpressionCallDAO().
                                insertGlobalNoExprToNoExpr(globalNoExprToNoExprTOs);
                        log.debug("Done inserting relations between no-expression calls " +
                                "and global no-expression calls for species {} and gene ID {}.", 
                                speciesId, entry.getKey());
                        
                        nbInsertedGlobalNoExprToNoExpr += nbCurInsertedGlobalNoExprToNoExpr;
                        //free memory
                        noExprTOIterator.remove();
                    }
                    
                    log.info("Done propagating no-expression calls for species {}: {} global no-expression calls inserted " +
                            "and {} correspondances inserted, from {} basic no-expression calls examined", speciesId, 
                            nbInsertedNoExpressions, nbInsertedGlobalNoExprToNoExpr, noExprCallsExamined);
                } else {

                    log.info("Start propagating expression calls for species {}", 
                            speciesId);
                    
                    LinkedHashMap<String, List<ExpressionCallTO>> exprTOs = 
                            this.getExpressionCallsByGeneId(speciesFilter);
                    
                    Map<String, Set<String>> anatEntityParentsFromChildren = 
                            BgeeDBUtils.getAnatEntityParentsFromChildren(speciesFilter, 
                            this.getRelationDAO());

                    int nbInsertedExpressions = 0;
                    int nbInsertedGlobalExprToExpr = 0;
                    int geneIterated = 0;
                    int geneCount = exprTOs.size();
                    Iterator<Entry<String, List<ExpressionCallTO>>> exprTOIterator = 
                            exprTOs.entrySet().iterator();
                    while (exprTOIterator.hasNext()) {
                        Entry<String, List<ExpressionCallTO>> entry = exprTOIterator.next();
                        geneIterated++;
                        if (log.isDebugEnabled() && geneIterated % 1000 == 0) {
                            log.debug("{}/{} genes examined.", geneIterated, geneCount);
                        }
                        // For each expression row, propagate to parents.
                        Map<ExpressionCallTO, Set<String>> globalExprMap =
                                this.generateGlobalExpressionTOs(
                                        entry.getValue(), anatEntityParentsFromChildren);

                        // Generate the globalExprToExprTOs.
                        Set<GlobalExpressionToExpressionTO> globalExprToExprTOs = 
                                this.generateGlobalCallToCallTOs(globalExprMap, 
                                        GlobalExpressionToExpressionTO.class);
                        
                        log.debug("Start inserting global expression calls for species {} and gene ID {}", 
                                speciesId, entry.getKey());
                        int nbCurInsertedExpr = this.getExpressionCallDAO().
                                insertExpressionCalls(globalExprMap.keySet());
                        if (nbCurInsertedExpr != globalExprMap.keySet().size()) {
                            throw log.throwing(new AssertionError(
                                    "Global expression calls incorrectly inserted: " + 
                                    nbCurInsertedExpr +" vs "+ globalExprMap.keySet().size()));
                        }
                        log.debug("Done inserting of {} global expression calls for species {} and gene ID {}.", 
                                nbCurInsertedExpr, speciesId, entry.getKey());
                        // Empty memory to free up some memory. We don't use clear() 
                        // because it empty ArgumentCaptor values in test in same time.
                        globalExprMap = null;
                        nbInsertedExpressions += nbCurInsertedExpr;
                        
                        log.debug("Start inserting relations between expression calls " +
                                "and global expression calls for species {} and gene ID {}...", 
                                speciesId, entry.getKey());
                        int nbCurInsertedGlobalExprToExpr = this.getExpressionCallDAO().
                                insertGlobalExpressionToExpression(globalExprToExprTOs);
                        log.debug("Done inserting {} relations between expression calls " +
                                "and global expression calls for species {} and gene ID {}.", 
                                nbCurInsertedGlobalExprToExpr, speciesId, entry.getKey());
                        nbInsertedGlobalExprToExpr += nbCurInsertedGlobalExprToExpr;
                        //free memory
                        exprTOIterator.remove();
                    }
                    
                    log.info("Done propagating expression calls for species {}: {} global expression calls inserted " +
                            "and {} correspondances inserted", speciesId, 
                            nbInsertedExpressions, nbInsertedGlobalExprToExpr);

                }
                this.commit();
            }            
        } finally {
            this.closeDAO();
        }

        log.exit();
    }

    /**
     * Retrieves all anatomical entity IDs allowed for no-expression call propagation. 
     * This method will retrieve the IDs of anatomical entities having at least 
     * an expression call, or a no-expression call, as well as the IDs of all 
     * anatomical entities with evolutionary relations to them (for instance, homology, 
     * or analogy), and the IDs of all the parents by is_a/part_of relation of 
     * all these anatomical entities.
     * <p>
     * These method is called by {@link #insert(List, boolean)} when the second argument 
     * is {@code true} (no-expression call propagation). The reason is that no-expression 
     * calls are propagated from parents to children, yet we do not want to propagate 
     * to all possible terms, as this would generate too many global no-expression calls. 
     * Instead, we restrain propagation to terms with data, or worthing a comparison 
     * to terms with data, or leading to these terms (for a consistent graph propagation).
     * 
     * @return              A {@code Set} of {@code String}s containing anatomical entity IDs
     *                      allowed to be used for no-expression call propagation.
     * @throws DAOException If an error occurred while getting the data from the Bgee data source.
     */
    private Set<String> loadAllowedAnatEntities() throws DAOException {
        log.entry();
        
        log.info("Start retrieving allowed anat entities for no-expression call propagation...");
        Set<String> allowedAnatEntities = new HashSet<String>();
        
        log.debug("Retrieving anat entities with expression calls...");
        ExpressionCallDAO exprDao = this.getExpressionCallDAO();
        exprDao.setAttributes(ExpressionCallDAO.Attribute.ANAT_ENTITY_ID);
        //we do not query global expression calls, because this way we can launch 
        //the propagation of global expression and global no-expression calls independently. 
        //We will propagate expression calls thanks to relations between anatomical terms.
        //params.setIncludeSubstructures(true);
        ExpressionCallTOResultSet rsExpressionCalls = 
                exprDao.getExpressionCalls(new ExpressionCallParams());
        while (rsExpressionCalls.next()) {
            String anatEntityId = rsExpressionCalls.getTO().getAnatEntityId();
            log.trace("Anat. entity with expression calls: {}", anatEntityId);
            allowedAnatEntities.add(anatEntityId);
        }
        //no need for a try with resource or a finally, the insert method will close everything 
        //at the end in any case.
        rsExpressionCalls.close();
        
        log.debug("Retrieving anat entities with no-expression calls...");
        NoExpressionCallDAO noExprDao = this.getNoExpressionCallDAO();
        noExprDao.setAttributes(NoExpressionCallDAO.Attribute.ANATENTITYID);
        NoExpressionCallTOResultSet rsNoExpressionCalls = noExprDao.getNoExpressionCalls(
                new NoExpressionCallParams());
        while (rsNoExpressionCalls.next()) {
            String anatEntityId = rsNoExpressionCalls.getTO().getAnatEntityId();
            log.trace("Anat. entity with no-expression calls: {}", anatEntityId);
            allowedAnatEntities.add(anatEntityId);
        }
        rsNoExpressionCalls.close();
        
        //TODO: retrieve anat. entities related by an evolutionary relation, 
        //once this information will be inserted into Bgee

        log.debug("Retrieving parents of anat entities allowed so far...");
        Map<String, Set<String>> parentsFromChildren = 
                BgeeDBUtils.getAnatEntityParentsFromChildren(null, this.getRelationDAO());
        Set<String> ancestorIds = new HashSet<String>();
        for (String anatEntityId: allowedAnatEntities) {
            ancestorIds.addAll(parentsFromChildren.get(anatEntityId));
        }
        allowedAnatEntities.addAll(ancestorIds);

        log.info("Done retrieving allowed anat entities for no-expression call propagation, {} entities allowed.", 
                allowedAnatEntities.size());
    
        return log.exit(allowedAnatEntities);        
    }

    /**
     * Generate the global expression calls from given expression calls filling the return 
     * {@code Map} associating each generated global expression calls to expression call IDs used 
     * to generate it.
     * <p>
     * First, the method fills the map with generic global expression calls as key (only gene ID, 
     * anatomical entity ID, and stage ID are defined) with all expression calls used to generate 
     * the global expression call. Second, it updates the global expression calls calling
     * {@link #updateGlobalExpressions()}.
     * 
     * @param expressionTOs         A {@code List} of {@code ExpressionCallTO}s containing 
     *                              all expression calls to propagate.
     * @param parentsFromChildren   A {@code Map} where keys are IDs of anatomical entities 
     *                              that are sources of a relation, the associated value 
     *                              being a {@code Set} of {@code String}s that are 
     *                              the IDs of their associated targets. 
     * @return                      A {@code Map} associating generated global expression calls to 
     *                              expression call IDs used to generate it.
     */
    private Map<ExpressionCallTO, Set<String>> generateGlobalExpressionTOs(
            List<ExpressionCallTO> expressionTOs, 
            Map<String, Set<String>> parentsFromChildren) {
        log.entry(expressionTOs, parentsFromChildren);
        log.debug("Propagating expression...");
        Map<ExpressionCallTO, Set<ExpressionCallTO>> mapGlobalExpr = 
                new HashMap<ExpressionCallTO, Set<ExpressionCallTO>>();
        int i = 0;
        int exprTOCount = expressionTOs.size();
        for (ExpressionCallTO exprCallTO : expressionTOs) {
            i++;
            if (log.isDebugEnabled() && i % 100000 == 0) {
                log.debug("{}/{} expression calls analyzed.", i, exprTOCount);
            }
            log.trace("Propagation for expression call: {}", exprCallTO);
            //the relations include a reflexive relation, where sourceId == targetId, 
            //this will allow to also include the actual not-propagated calls. 
            //we should always have at least a reflexive relation, so, if there is 
            //no "parents" for the anatomical entity, something is wrong 
            //in the database. 
            Set<String> parents = parentsFromChildren.get(exprCallTO.getAnatEntityId());
            if (parents == null) {
                throw log.throwing(new IllegalStateException("The anatomical entity " +
                        exprCallTO.getAnatEntityId() + " is not defined as existing " +
                        		"in the species of gene " + exprCallTO.getGeneId() + 
                        		", while it has expression data in it."));
            }
            for (String parentId : parents) {
                log.trace("Propagation of the current expression to parent: {}", parentId);
                // Set ID to null to be able to compare keys of the map on 
                // gene ID, anatomical entity ID, and stage ID.
                // Add propagated expression call (same gene ID and stage ID 
                // but with anatomical entity ID of the current relation target ID).
                ExpressionCallTO propagatedExpression = new ExpressionCallTO(
                        null, 
                        exprCallTO.getGeneId(),
                        parentId,
                        exprCallTO.getStageId(),
                        DataState.NODATA,      
                        DataState.NODATA,
                        DataState.NODATA,
                        DataState.NODATA,
                        false,
                        false,
                        ExpressionCallTO.OriginOfLine.SELF, 
                        ExpressionCallTO.OriginOfLine.SELF,
                        null);
                
                log.trace("Add the propagated expression: {}", propagatedExpression);
                Set<ExpressionCallTO> curExprAsSet = mapGlobalExpr.get(propagatedExpression);
                if (curExprAsSet == null) {
                    curExprAsSet = new HashSet<ExpressionCallTO>();
                    mapGlobalExpr.put(propagatedExpression, curExprAsSet);
                }
                curExprAsSet.add(exprCallTO);
            }
        }

        log.debug("Done propagating expression.");
        return log.exit(this.updateGlobalExpressions(mapGlobalExpr));        
    }

    /**
     * Generate the global no-expression calls from given no-expression calls filling the return
     * {@code Map} associating each generated global no-expression calls to no-expression call IDs 
     * used to generate it.
     * <p>
     * First, the method fills the map associating generic global no-expression calls as key 
     * (only gene ID, anatomical entity ID, and stage ID are defined) with all no-expression calls 
     * used to generate the global no-expression call. Second, it updates the global no-expression
     * calls calling {@link #updateGlobalNoExpressions()}.
     * 
     * @param noExpressionTOs           A {@code List} of {@code NoExpressionCallTO}s containing 
     *                                  all no-expression calls to be propagated.
     * @param childrenFromParents        A {@code Map} where keys are IDs of anatomical entities 
     *                                  that are targets of a relation, the associated value 
     *                                  being a {@code Set} of {@code String}s that are 
     *                                  the IDs of their associated sources. 
     * @param filteredAnatEntities      A {@code Set} of {@code String}s that are the IDs of 
     *                                  anatomical entities allowing filter calls to propagate.
     * @return                          A {@code Map} associating generated global no-expression 
     *                                  calls to no-expression call IDs used to generate it.
     */
    private Map<NoExpressionCallTO, Set<String>>
            generateGlobalNoExpressionTOs(List<NoExpressionCallTO> noExpressionTOs, 
                    Map<String, Set<String>> childrenFromParents, 
                    Set<String> filteredAnatEntities) {
        log.entry(noExpressionTOs, childrenFromParents, filteredAnatEntities);
        log.debug("Propagating no-expression...");

        Map<NoExpressionCallTO, Set<NoExpressionCallTO>> mapGlobalNoExpr = 
                new HashMap<NoExpressionCallTO, Set<NoExpressionCallTO>>();
        int i = 0;
        int noExprTOCount = noExpressionTOs.size();
        for (NoExpressionCallTO noExprCallTO : noExpressionTOs) {
            i++;
            if (log.isDebugEnabled() && i % 100000 == 0) {
                log.debug("{}/{} no-expression calls analyzed.", i, noExprTOCount);
            }
            log.trace("Propagation for no-expression call: {}", noExprCallTO);
            //the relations include a reflexive relation, where sourceId == targetId, 
            //this will allow to also include the actual not-propagated calls
            //we should always have at least a reflexive relation, so, if there is 
            //no "children" for the anatomical entity, something is wrong 
            //in the database. 
            Set<String> children = childrenFromParents.get(noExprCallTO.getAnatEntityId());
            if (children == null) {
                throw log.throwing(new IllegalStateException("The anatomical entity " +
                        noExprCallTO.getAnatEntityId() + " is not defined as existing " +
                                "in the species of gene " + noExprCallTO.getGeneId() + 
                                ", while it has expression data in it."));
            }
            for (String childId : children) {
                log.trace("Propagation of the current no-expression to child: {}", childId);

                // Add propagated no-expression call (same gene ID and stage ID 
                // but with anatomical entity ID of the current relation source ID) 
                // only in anatomical entities having at least a global expression call 
                //or no-expression call.
                if (childId.equals(noExprCallTO.getAnatEntityId()) || //reflexive relation
                            filteredAnatEntities.contains(childId)) {
                    // Set ID to null to be able to compare keys of the map on 
                    // gene ID, anatomical entity ID, and stage ID.
                    NoExpressionCallTO propagatedExpression = new NoExpressionCallTO(
                            null, 
                            noExprCallTO.getGeneId(),
                            childId,
                            noExprCallTO.getStageId(),
                            DataState.NODATA,      
                            DataState.NODATA,
                            DataState.NODATA,
                            DataState.NODATA,
                            false, 
                            NoExpressionCallTO.OriginOfLine.SELF);

                    log.trace("Add the propagated no-expression: {}", propagatedExpression);
                    Set<NoExpressionCallTO> curExprAsSet = mapGlobalNoExpr.get(propagatedExpression);
                    if (curExprAsSet == null) {
                       curExprAsSet = new HashSet<NoExpressionCallTO>();
                       mapGlobalNoExpr.put(propagatedExpression, curExprAsSet);
                    }
                    curExprAsSet.add(noExprCallTO);
                }
            }
        }

        log.debug("Done propagating no-expression.");
        return log.exit(this.updateGlobalNoExpressions(mapGlobalNoExpr));        
    }

    /**
     * Updates global expression calls of the given {@code Map} taking account {@code DataType}s and 
     * anatomical entity IDs of calls to generate {@code OrigineOfLine} of global calls, 
     * and returns them in a new {@code Map} associating generated global expression calls to 
     * expression call IDs.
     * <p>
     * The provided {@code Map} will be empty to free up some memory.
     * 
     * @param globalMap     A {@code Map} associating generated global expression calls to 
     *                      expression calls to be updated.
     * @return              A {@code Map} associating generated global expression calls to 
     *                      expression call IDs.
     */
    private Map<ExpressionCallTO, Set<String>> updateGlobalExpressions(
            Map<ExpressionCallTO, Set<ExpressionCallTO>> globalMap) {
        log.entry(globalMap);
        log.debug("Updating global expression calls...");
        // Create a Map associating generated global expression calls to expression call IDs.
        Map<ExpressionCallTO, Set<String>> globalExprWithExprIds =
                    new HashMap<ExpressionCallTO, Set<String>>();

        // Create a Set from keySet to be able to modify globalMap.
        Set<ExpressionCallTO> tmpGlobalCalls = new HashSet<ExpressionCallTO>(globalMap.keySet());
        int i = 0;
        int globalExprTOCount = tmpGlobalCalls.size();
        for (ExpressionCallTO globalCall: tmpGlobalCalls) {
            i++;
            if (log.isDebugEnabled() && i % 100000 == 0) {
                log.debug("{}/{} global expression calls analyzed.", i, globalExprTOCount);
            }
            // Remove generic global call which contains only 
            // gene ID, anatomical entity ID, and stage ID
            Set<ExpressionCallTO> calls = globalMap.remove(globalCall);

            log.trace("Update global expression calls: {}; with: {}", globalCall, calls);
            
            // Define the best DataType of the global call according to all calls,
            // get anatomical entity IDs to be able to define OriginOfLine later, 
            // and get expression IDs to build the new  
            DataState affymetrixData = DataState.NODATA, estData = DataState.NODATA, 
                    inSituData = DataState.NODATA, rnaSeqData = DataState.NODATA;
            Set<String> anatEntityIds = new HashSet<String>();
            Set<String> exprIds = new HashSet<String>();
            for (ExpressionCallTO call: calls) {
                affymetrixData = getBestDataState(affymetrixData, call.getAffymetrixData());
                estData = getBestDataState(estData, call.getESTData());
                inSituData = getBestDataState(inSituData, call.getInSituData());
                rnaSeqData = getBestDataState(rnaSeqData, call.getRNASeqData());
                anatEntityIds.add(call.getAnatEntityId());
                exprIds.add(call.getId());
            }

            // Define the OriginOfLine of the global expression call according to all calls
            ExpressionCallTO.OriginOfLine origin = ExpressionCallTO.OriginOfLine.DESCENT;
            if (anatEntityIds.contains(globalCall.getAnatEntityId())) {
                if (anatEntityIds.size() == 1) {
                    origin = ExpressionCallTO.OriginOfLine.SELF;
                } else {
                    origin = ExpressionCallTO.OriginOfLine.BOTH;
                }
            }
            ExpressionCallTO updatedGlobalCall =
                    new ExpressionCallTO(String.valueOf(this.globalId++), 
                            globalCall.getGeneId(), globalCall.getAnatEntityId(), 
                            globalCall.getStageId(), 
                            affymetrixData, estData, inSituData, rnaSeqData, true,
                            globalCall.isIncludeSubStages(), origin, 
                            globalCall.getStageOriginOfLine(), globalCall.isObservedData());

            log.trace("Updated global expression call: {}", updatedGlobalCall);

            // Add the updated global expression call
            globalExprWithExprIds.put(updatedGlobalCall, exprIds);
        } 

        log.debug("Done updating global expression calls.");
        return log.exit(globalExprWithExprIds);
    }

    /**
     * Updates global no-expression calls of the given {@code Map} taking account {@code DataType}s 
     * and anatomical entity IDs of calls to generate {@code OrigineOfLine} of global calls, 
     * and returns them in a new {@code Map} associating generated global no-expression calls to 
     * no-expression call IDs.
     * <p>
     * The provided {@code Map} will be empty to free up some memory.
     * 
     * @param globalMap     A {@code Map} associating generated global no-expression calls to 
     *                      no-expression calls to be updated.
     * @return              A {@code Map} associating generated global no-expression calls to 
     *                      no-expression call IDs.
     */
    private Map<NoExpressionCallTO, Set<String>> updateGlobalNoExpressions(
            Map<NoExpressionCallTO, Set<NoExpressionCallTO>> globalMap) {
        log.entry(globalMap);
        log.debug("Updating global no-expression calls...");
        // Create a Map associating generated global no-expression calls to no-expression call IDs.
        Map<NoExpressionCallTO, Set<String>> globalNoExprWithNoExprIds =
                new HashMap<NoExpressionCallTO, Set<String>>();

        // Create a Set from keySet to be able to modify globalMap.
        Set<NoExpressionCallTO> tmpGlobalCalls = new HashSet<NoExpressionCallTO>(globalMap.keySet());
        int i = 0;
        int globalNoExprTOCount = tmpGlobalCalls.size();
        for (NoExpressionCallTO globalCall: tmpGlobalCalls) {
            i++;
            if (log.isDebugEnabled() && i % 100000 == 0) {
                log.debug("{}/{} global no-expression calls analyzed.", i, globalNoExprTOCount);
            }
            // Remove generic global call which contains only 
            // gene ID, anatomical entity ID, and stage ID
            Set<NoExpressionCallTO> calls = globalMap.remove(globalCall);

            log.trace("Update global no-expression calls: {}; with: {}", globalCall, calls);
            
            // Define the best DataType of the global call according to all calls
            // and get anatomical entity IDs to be able to define OriginOfLine later.
            DataState affymetrixData = DataState.NODATA, relaxedinSituData = DataState.NODATA,
                    inSituData = DataState.NODATA, rnaSeqData = DataState.NODATA;
            Set<String> anatEntityIDs = new HashSet<String>();
            Set<String> noExprIDs = new HashSet<String>();
            for (NoExpressionCallTO call: calls) {
                affymetrixData = getBestDataState(affymetrixData, call.getAffymetrixData());
                inSituData = getBestDataState(inSituData, call.getInSituData());
                relaxedinSituData = getBestDataState(relaxedinSituData, call.getRelaxedInSituData());
                rnaSeqData = getBestDataState(rnaSeqData, call.getRNASeqData());
                anatEntityIDs.add(call.getAnatEntityId());
                noExprIDs.add(call.getId());
            }

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
                    new NoExpressionCallTO(String.valueOf(this.globalId++), 
                            globalCall.getGeneId(), globalCall.getAnatEntityId(), 
                            globalCall.getStageId(), 
                            affymetrixData, inSituData, relaxedinSituData, rnaSeqData,
                            true, origin);

            log.trace("Updated global no-expression call: {}", updatedGlobalCall);

            // Add the updated global no-expression call
            globalNoExprWithNoExprIds.put(updatedGlobalCall, noExprIDs);
        }

        log.debug("Done updating global no-expression calls.");
        return log.exit(globalNoExprWithNoExprIds);
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
            Map<T, Set<String>> globalExprMap, Class<V> type) {
        log.entry(globalExprMap, type);
        log.debug("Generating relations between global calls and basic calls...");
        
        Set<V> globalExprToExprTOs = new HashSet<V>();
        int i = 0;
        int globalTOCount = globalExprMap.entrySet().size();
        for (Entry<T, Set<String>> entry: globalExprMap.entrySet()) {
            i++;
            if (log.isDebugEnabled() && i % 100000 == 0) {
                log.debug("{}/{} global calls analyzed.", i, globalTOCount);
            }
            for (String expressionId : entry.getValue()) {
                if (type.equals(GlobalExpressionToExpressionTO.class)) {
                    globalExprToExprTOs.add(type.cast(new GlobalExpressionToExpressionTO(
                            expressionId, entry.getKey().getId())));
                } else if (type.equals(GlobalNoExpressionToNoExpressionTO.class)) {
                    globalExprToExprTOs.add(type.cast(new GlobalNoExpressionToNoExpressionTO(
                            expressionId, entry.getKey().getId())));
                } else {
                    throw log.throwing(new IllegalArgumentException("There is no propagation " +
                        "implemented for TransferObject " + entry.getKey().getClass() + "."));
                }
            }
        }

        log.debug("Done generating relations between global calls and basic calls.");
        return log.exit(globalExprToExprTOs);
    }
}
