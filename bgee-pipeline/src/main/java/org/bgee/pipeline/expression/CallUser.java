package org.bgee.pipeline.expression;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.ExpressionCallParams;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallParams;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTOResultSet;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTOResultSet;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.pipeline.MySQLDAOUser;

/**
 * Class used by classes that interact with {@code CallTO}s. This class also extends 
 * {@code MySQLDAOUser}, as the {@code CallTO}s manipulated are always either 
 * retrieved from the database, or inserted/updated into the database.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public abstract class CallUser extends MySQLDAOUser {
    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(CallUser.class.getName());

    /**
     * An {@code int} used to generate IDs of global expression or no-expression calls.
     */        
    protected int globalId;
    
    /**
     * Default constructor using default {@code MySQLDAOManager}.
     */
    public CallUser() {
        this(null);
    }
    /**
     * Constructor providing the {@code MySQLDAOManager} that will be used by 
     * this object to perform queries to the database.
     * 
     * @param manager   the {@code MySQLDAOManager} to use.
     */
    public CallUser(MySQLDAOManager manager) {
        super(manager);
        this.globalId = 1;
    }
    


    /**
     * Retrieves all expression calls for given species in a {@code Map} associating gene IDs to 
     * {@code ExpressionCallTO}s, present into the Bgee database.
     * 
     * @param speciesIds        A {@code Set} of {@code String}s that are the IDs of species 
     *                          allowing to filter the expression calls to use.
     * @param exprCallDAO       A {@code ExpressionCallDAO} to use to retrieve information about 
     *                          expression calls from the Bgee data source.
     * @return                  A {@code LinkedHashMap} associating gene IDs to 
     *                          {@code ExpressionCallTO}s of the given species. Returns a 
     *                          {@code LinkedHashMap} to keep the order in which 
     *                          {@code ExpressionCallTO}s are retrieved.
     * @throws DAOException     If an error occurred while getting the data from the Bgee database.
     */
    protected LinkedHashMap<String, List<ExpressionCallTO>> getExpressionCallsByGeneId(
            Set<String> speciesIds) throws DAOException {
        log.entry(speciesIds);
        
        ExpressionCallParams params = new ExpressionCallParams();
        params.addAllSpeciesIds(speciesIds);

        log.debug("Generating Map from genes to expression calls for species {}...", 
                speciesIds);
        LinkedHashMap<String, List<ExpressionCallTO>> map = 
                new LinkedHashMap<String, List<ExpressionCallTO>>();
        try (ExpressionCallTOResultSet exprTORs = 
                this.getExpressionCallDAO().getExpressionCalls(params)) {
            while (exprTORs.next()) {
                ExpressionCallTO exprTO = exprTORs.getTO();
                log.trace("Expression call: {}", exprTO);
                List<ExpressionCallTO> curExprAsSet = map.get(exprTO.getGeneId());
                if (curExprAsSet == null) {
                    log.trace("Create new map key: {}", exprTO.getGeneId());
                    curExprAsSet = new ArrayList<ExpressionCallTO>();
                    map.put(exprTO.getGeneId(), curExprAsSet);
                }
                curExprAsSet.add(exprTO);
            }
        }
        log.debug("Done generating Map from genes to expression calls for species {}, {} entries.", 
                speciesIds, map.size());
        
        return log.exit(map);        
    }

    /**
     * Retrieves all no-expression calls for given species in a {@code Map} associating gene IDs to 
     * {@code NoExpressionCallTO}s, present into the Bgee database.
     * 
     * @param speciesIds        A {@code Set} of {@code String}s that are the IDs of species 
     *                          allowing to filter the no-expression calls to use.
     * @param noExprCallDAO     A {@code NoExpressionCallDAO} to use to retrieve information about 
     *                          no-expression calls from the Bgee data source.
     * @return                  A {@code LinkedHashMap} associating gene IDs to 
     *                          {@code NoExpressionCallTO}s of the given species. Returns a 
     *                          {@code LinkedHashMap} to keep the order in which 
     *                          {@code NoExpressionCallTO}s are retrieved.
     * @throws DAOException     If an error occurred while getting the data from the Bgee database.
     */
    protected LinkedHashMap<String, List<NoExpressionCallTO>> getNoExpressionCallsByGeneId(
            Set<String> speciesIds) throws DAOException {
        log.entry(speciesIds);

        NoExpressionCallParams params = new NoExpressionCallParams();
        params.addAllSpeciesIds(speciesIds);
        
        log.debug("Generating Map from genes to no-expression calls for species {}...", 
                speciesIds);
        LinkedHashMap<String, List<NoExpressionCallTO>> map = 
                new LinkedHashMap<String, List<NoExpressionCallTO>>();
        try (NoExpressionCallTOResultSet noExprTORs = 
                this.getNoExpressionCallDAO().getNoExpressionCalls(params)) {
            while (noExprTORs.next()) {
                NoExpressionCallTO noExprTO = noExprTORs.getTO();
                log.trace("No-expression call: {}", noExprTO);
                List<NoExpressionCallTO> curNoExprAsSet = map.get(noExprTO.getGeneId());
                if (curNoExprAsSet == null) {
                    log.trace("Create new map key: {}", noExprTO.getGeneId());
                    curNoExprAsSet = new ArrayList<NoExpressionCallTO>();
                    map.put(noExprTO.getGeneId(), curNoExprAsSet);
                }
                curNoExprAsSet.add(noExprTO);
            }
        }
        log.debug("Done generating Map from genes to no-expression calls for species {}, {} entries.", 
                speciesIds, map.size());
        
        return log.exit(map);        
    }
    
    /**
     * Group and order the provided {@code CallTO}s, according to the values returned by 
     * the methods {@code CallTO#getGeneId()}, {@code CallTO#getAnatEntityId()}, and 
     * {@code CallTO#getStageId()}. These {@code CallTO}s can be of mixed types,  
     * for instance mixing {@code ExpressionCallTO}s and {@code NoExpressionCallTO}s, 
     * and mixing global calls with basic calls. 
     * <p>
     * {@code CallTO}s with equal gene-anat.entity-stage are grouped 
     * in a same {@code Entry} of the returned {@code Map}, with their corresponding key 
     * being a {@code CallTO} storing this information of gene-anat.entity-stage. 
     * {@code Entry}s are ordered according to the natural ordering of the IDs 
     * of the gene, anat. entity, and stage, in that order, stored in the {@code CallTO}s 
     * used as keys. 
     * <p>
     * The {@code Collection}s stored as values in the returned {@code Map} are not {@code Set}s. 
     * This is because otherwise, a global call with data propagated could erase a basic call 
     * with no data propagated (or the opposite), as these calls would be seen as equal 
     * according to their gene-anat.entity-stage. 
     * <p>
     * The {@code CallTO}s should return a a not-null not-empty value when calling 
     * {@code CallTO#getGeneId()}, {@code CallTO#getAnatEntityId()}, and 
     * {@code CallTO#getStageId()}, otherwise, an {@code IllegalArgumentException} is thrown. 
     * 
     * @param callTOs   A {@code Collection} of {@code CallTO}s to be grouped and ordered. 
     * @return          A {@code SortedMap} where keys are {@code CallTO}s providing 
     *                  the information of gene-anat.entity-stage, the associated values 
     *                  being {@code Collection} of {@code CallTO}s with the corresponding 
     *                  gene-anat.entity-stage. {@code Entry}s are ordered according to 
     *                  the natural ordering of the IDs of the gene, anat. entity, and stage, 
     *                  in that order.
     * @throws IllegalArgumentException If any of the values returned by 
     *                                  {@code CallTO#getGeneId()}, {@code CallTO#getAnatEntityId()}, 
     *                                  or {@code CallTO#getStageId()} are {@code null} or empty, 
     *                                  for any of the {@code CallTO}s provided. 
     */
    protected SortedMap<CallTO, Collection<CallTO>> groupAndOrderByGeneAnatEntityStage(
            Collection<CallTO> callTOs) throws IllegalArgumentException {
        log.entry(callTOs);
        log.debug("Start sorting and grouping of {} calls...", callTOs.size());
        
        /**
         * {@code Comparator} used to order they keyset in the returned {@code Map}.
         */
        final class CallTOComparator implements Comparator<CallTO>, Serializable {
            private static final long serialVersionUID = 3537157597163398354L;

                    @Override
                    public int compare(CallTO callTO1, CallTO callTO2) {
                        log.entry(callTO1, callTO2);
                        int geneIdComp = callTO1.getGeneId().compareToIgnoreCase(
                                callTO2.getGeneId());
                        if (geneIdComp != 0) {
                            return log.exit(geneIdComp);
                        }
                        int anatEntityIdComp = callTO1.getAnatEntityId().compareToIgnoreCase(
                                callTO2.getAnatEntityId());
                        if (anatEntityIdComp != 0) {
                            return log.exit(anatEntityIdComp);
                        }
                        int stageIdComp = callTO1.getStageId().compareToIgnoreCase(
                                callTO2.getStageId());
                        if (stageIdComp != 0) {
                            return log.exit(stageIdComp);
                        }
                        return log.exit(0);
                    }
            
        };
        SortedMap<CallTO, Collection<CallTO>> aggregateMap = 
                new TreeMap<CallTO, Collection<CallTO>>(new CallTOComparator());
       
        for (CallTO callTO: callTOs) {
            //sanity checks
            if (StringUtils.isEmpty(callTO.getGeneId()) || 
                    StringUtils.isEmpty(callTO.getAnatEntityId()) || 
                    StringUtils.isEmpty(callTO.getStageId())) {
                throw log.throwing(new IllegalArgumentException("Invalid CallTO provided: " + 
                    callTO));
            }
            
            //create a fake CallTO to store the information of geneId-anatEntityId-stageId, 
            //that will be used as a key in the returned Map. As CallTO is an abstract class, 
            //we choose a concrete implementation, and we stick to it, to not mix 
            //different classes in the keyset. 
            CallTO fakeCallTO = new ExpressionCallTO(null, callTO.getGeneId(), 
                    callTO.getAnatEntityId(), callTO.getStageId(), 
                    null, null, null, null, null, null, null, null, null);
            Collection<CallTO> aggregatedCalls = aggregateMap.get(fakeCallTO);
            if (aggregatedCalls == null) {
                //note that this Collection must absolutely not be a Set, otherwise 
                //a global expression call could be seen as equal to a basic expression call. 
                //We declare it as a Collection and not as a List because we don't care about
                //the iteration order.
                aggregatedCalls = new ArrayList<CallTO>();
                aggregateMap.put(fakeCallTO, aggregatedCalls);
            }
            aggregatedCalls.add(callTO);
        }

        log.debug("Done sorting and grouping of {} calls.", callTOs.size());
        return log.exit(aggregateMap);
    }
    
    /**
     * Returns the {@code boolean} defining whether the provided {@code CallTO} was generated 
     * following developmental stage or anatomical entity propagation. 
     * {@code ExpressionCallTO}s must have a non-null value returned by the method 
     * {@code isObservedData}; {@code NoExpressionCallTO}s must have a non-null value 
     * returned by the method {@code getOriginOfLine}.
     *  
     * @param callTO    A {@code CallTO} to be checked. 
     * @return          If {@code true}, the provided {@code CallTO} was generated by propagation 
     *                  of substructures or sub-stages.
     * @throws IllegalArgumentException If {@code callTO} is null, or not supported. 
     */
    protected boolean isPropagatedOnly(CallTO callTO) throws IllegalArgumentException {
        log.entry(callTO);
        
        if (callTO == null) {
            throw log.throwing(new IllegalArgumentException("The provided CallTO is null"));
        }

        if (callTO instanceof  ExpressionCallTO) {
            if (!((ExpressionCallTO) callTO).isObservedData()) {
                return log.exit(true);
            }
        } else if (callTO instanceof NoExpressionCallTO){
            if (((NoExpressionCallTO) callTO).getOriginOfLine().
                        equals(NoExpressionCallTO.OriginOfLine.PARENT)) {
                return log.exit(true);
            }
        } else {
            throw log.throwing(new IllegalArgumentException("The CallTO provided (" +
                    callTO.getClass() + ") is not managed for expression/no-expression data: " + 
                    callTO.toString()));
        }
        return log.exit(false);
    }
    
    protected boolean isCallWithNoData(CallTO call) {
        log.entry(call);

        if (call == null) {
            throw log.throwing(new IllegalArgumentException("The provided CallTO is null"));
        }
        if ((call.getAffymetrixData() !=null && !call.getAffymetrixData().equals(DataState.NODATA)) || 
                (call.getInSituData() !=null && !call.getInSituData().equals(DataState.NODATA)) || 
                (call.getRNASeqData() !=null && !call.getRNASeqData().equals(DataState.NODATA))) {
            return log.exit(false);
        }
        if (call instanceof ExpressionCallTO) {
            if (((ExpressionCallTO) call).getESTData() != null && 
                    !((ExpressionCallTO) call).getESTData().equals(DataState.NODATA)) {
                return log.exit(false);
            }
        } else if (call instanceof NoExpressionCallTO) {
            if (((NoExpressionCallTO) call).getESTData() != null &&
                    !((NoExpressionCallTO) call).getRelaxedInSituData().equals(DataState.NODATA)) {
                return log.exit(false);
            }
        } else {
            throw log.throwing(new IllegalArgumentException("The CallTO provided (" +
                    call.getClass() + ") is not managed for expression/no-expression data: " + 
                    call.toString()));
        }
        return log.exit(true);
    }


    /**
     * Generates correct global propagated {@code ExpressionCallTO}s using {@code globalMap}, 
     * with IDs generated, correct values of {@code DataState} for each data type, 
     * correct values of {@code OriginOfLine} for both stage and anatomical entity 
     * origins, and correct value of {@code ObservedData}. This method is used when 
     * propagating {@code ExpressionCallTO}s to parent anatomical entities and/or parent 
     * developmental stages. The arguments {@code propagatingAnatomy} and 
     * {@code propagatingStage} allow to specify what is being propagated. 
     * <p> 
     * {@code globalMap} is a {@code Map} associating global {@code ExpressionCallTO}s, 
     * as keys, to a {@code Set} of {@code ExpressionCallTO}s they were generated from. 
     * The following computations will be performed for each global call: 
     * <ul>
     * <li>the best {@code DataState}s will be computed, for each data type, 
     * from all the associated {@code ExpressionCallTO}s.
     * <li>if {@code propagatingAnatomy} is true, the anatomical {@code OriginOfLine} will be 
     * computed from all the associated {@code ExpressionCallTO}s, otherwise the value 
     * returned by the method {@code getAnatOriginOfLine} of the global call will be used.
     * <li>if {@code propagatingAnatomy} is true, the value of {@code includeSubstructures} 
     * will be set to true, otherwise, the value returned by the method 
     * {@code isIncludeSubstructures} of the global call will be used.
     * <li>if {@code propagatingStage} is true, the stage {@code OriginOfLine} will be 
     * computed from all the associated {@code ExpressionCallTO}s, otherwise the value 
     * returned by the method {@code getStageOriginOfLine} of the global call will be used. 
     * <li>if {@code propagatingStage} is true, the value of {@code includeSubStages} 
     * will be set to true, otherwise, the value returned by the method 
     * {@code isIncludeSubStages} of the global call will be used.
     * <li>for the value defining whether the call was actually observed, if both 
     * {@code propagatingAnatomy} and {@code propagatingStage} are {@code false}, the value 
     * returned by the method {@code isObservedData} of the global call will be used; 
     * otherwise, it will be computed by this method. 
     * <li>The ID of the global call will be used, unless it is {@code null}; in that case, 
     * the IDs will be auto-generated by incrementing {@link #globalId}.
     * <p>
     * The returned {@code Map} stores the updated correct {@code ExpressionCallTO}s as keys, 
     * associated to a {@code Set} of {@code String}s that are the IDs of the calls 
     * they were generated from. 
     * <p>
     * The provided {@code Map} will be modified and emptied to free up some memory.
     * 
     * @param globalMap     A {@code Map} associating {@code ExpressionCallTO}s to be updated 
     *                      as keys, to {@code Set}s of {@code ExpressionCallTO}s they were 
     *                      generated from as values.
     * @param propagatingAnatomy    A {@code boolean} defining whether the global calls 
     *                              are being generating by propagating to parent anatomical 
     *                              entities.
     * @param propagatingStage      A {@code boolean} defining whether the global calls 
     *                              are being generating by propagating to parent developmental 
     *                              stages.
     * @return              A {@code Map} associating updated {@code ExpressionCallTO}s to 
     *                      {@code Set}s of {@code String}s that are the IDs of the 
     *                      {@code ExpressionCallTO}s they were generated from.
     */
    //TODO: unit test this method now that it is not private anymore
    protected Map<ExpressionCallTO, Set<String>> updateGlobalExpressions(
            Map<ExpressionCallTO, Set<ExpressionCallTO>> globalMap, 
            boolean propagatingAnatomy, boolean propagatingStage) {
        log.entry(globalMap, propagatingAnatomy, propagatingStage);
        
        log.debug("Updating expression calls...");
        // Create a Map associating generated expression calls to expression call IDs.
        Map<ExpressionCallTO, Set<String>> globalExprWithExprIds =
                    new HashMap<ExpressionCallTO, Set<String>>();

        // Create a Set from keySet to be able to modify globalMap.
        Set<ExpressionCallTO> tmpGlobalCalls = new HashSet<ExpressionCallTO>(globalMap.keySet());
        int i = 0;
        int globalExprTOCount = tmpGlobalCalls.size();
        for (ExpressionCallTO globalCall: tmpGlobalCalls) {
            i++;
            if (log.isDebugEnabled() && i % 100000 == 0) {
                log.debug("{}/{} expression calls analyzed.", i, globalExprTOCount);
            }
            // Remove generic global call, get associated calls
            Set<ExpressionCallTO> calls = globalMap.remove(globalCall);

            log.trace("Update expression calls: {}; with: {}", globalCall, calls);
            
            // Define the best DataType of the global call according to all calls,
            // get anatomical entity IDs and stage IDs to be able to define OriginOfLine later, 
            // and get expression IDs to build the new  
            DataState affymetrixData = DataState.NODATA, estData = DataState.NODATA, 
                    inSituData = DataState.NODATA, rnaSeqData = DataState.NODATA;
            Set<String> anatEntityIds = new HashSet<String>();
            Set<String> stageIds = new HashSet<String>();
            Set<String> exprIds = new HashSet<String>();
            //check whether the global call was actually observed with no propagation 
            //at least once
            boolean observedData = false;
            for (ExpressionCallTO call: calls) {
                //if we are not propagating to parent anat. entities, the global call 
                //and all the basic calls should occur in the same anat. entity.
                if (!propagatingAnatomy && 
                        (!globalCall.getAnatEntityId().equals(call.getAnatEntityId()) || 
                          globalCall.isIncludeSubstructures() != call.isIncludeSubstructures() || 
                          !globalCall.getAnatOriginOfLine().equals(call.getAnatOriginOfLine()))) {
                    throw log.throwing(new IllegalArgumentException("No propagation " +
                    		"on anatomy requested, but anatomical entity of global and basic " +
                    		"calls differ. Global call: " + globalCall + ", basic call: " + 
                    		call));
                }
                if (propagatingAnatomy && call.isIncludeSubstructures() != null && 
                        call.isIncludeSubstructures()) {
                    throw log.throwing(new IllegalArgumentException("Propagation using anatomy " +
                    		"is requested, but basic call is already propagated: " + call));
                }
                //if we are not propagating to parent stages, the global call 
                //and all the basic calls should occur in the same stage.
                if (!propagatingStage && 
                        (!globalCall.getStageId().equals(call.getStageId()) || 
                                globalCall.isIncludeSubStages() != call.isIncludeSubStages() || 
                                !globalCall.getStageOriginOfLine().equals(call.getStageOriginOfLine()))) {
                    throw log.throwing(new IllegalArgumentException("No propagation " +
                            "on stage requested, but stage of global and basic " +
                            "calls differ. Global call: " + globalCall + ", basic call: " + 
                            call));
                }
                if (propagatingStage && call.isIncludeSubStages() != null && 
                        call.isIncludeSubStages()) {
                    throw log.throwing(new IllegalArgumentException("Propagation using stages " +
                            "is requested, but basic call is already propagated: " + call));
                }
                if (!globalCall.getGeneId().equals(call.getGeneId())) {
                    throw log.throwing(new IllegalArgumentException("Incorrect grouping " +
                    		"according to gene ID. Global call: " + globalCall + 
                    		", basic call: " + call));
                }
                
                //********* OK, all sanity checks passed ************
                affymetrixData = getBestDataState(affymetrixData, call.getAffymetrixData());
                estData = getBestDataState(estData, call.getESTData());
                inSituData = getBestDataState(inSituData, call.getInSituData());
                rnaSeqData = getBestDataState(rnaSeqData, call.getRNASeqData());
                
                anatEntityIds.add(call.getAnatEntityId());
                stageIds.add(call.getStageId());
                if (call.getId() != null) {
                    exprIds.add(call.getId());
                }
                
                //check whether the global call has been observed with no propagation 
                //at least once.
                //for the anatomical entity, either the global call is being generated 
                //by propagating to parent entities, and we check whether the global call 
                //occurs in the same anatomical entity as the basic call; or it is not 
                //being generated by propagating to parent entities, and we just use the value 
                //provided by the basic call.
                boolean anatObserved = ((propagatingAnatomy && 
                        call.getAnatEntityId().equals(globalCall.getAnatEntityId())) || 
                        (!propagatingAnatomy && !call.getAnatOriginOfLine().equals(
                                ExpressionCallTO.OriginOfLine.DESCENT)));
                //same for the stages: either the global call is being generated 
                //by propagating to parent stages, and we check whether the global call 
                //occurs in the same stage as the basic call; or it is not 
                //being generated by propagating to parent stages, and we just use the value 
                //provided by the basic call.
                boolean stageObserved = ((propagatingStage && 
                        call.getStageId().equals(globalCall.getStageId())) || 
                        (!propagatingStage && !call.getStageOriginOfLine().equals(
                                ExpressionCallTO.OriginOfLine.DESCENT)));
                //if the call was actually observed both at the same stage and anatomical 
                //entity as the global call, the global call was actually observed, not only 
                //propagated
                if (anatObserved && stageObserved) {
                    observedData = true;
                }
            }

            // Define the anatomical OriginOfLine of the global expression call, according to 
            // all basic calls, only if we are propagating to parent structures (otherwise, 
            // we use the value of the global call)
            ExpressionCallTO.OriginOfLine anatOrigin = globalCall.getAnatOriginOfLine();
            if (propagatingAnatomy) {
                anatOrigin = ExpressionCallTO.OriginOfLine.DESCENT;
                if (anatEntityIds.contains(globalCall.getAnatEntityId())) {
                    if (anatEntityIds.size() == 1) {
                        anatOrigin = ExpressionCallTO.OriginOfLine.SELF;
                    } else {
                        anatOrigin = ExpressionCallTO.OriginOfLine.BOTH;
                    }
                }
            }
            //same for stage OriginOfLine
            ExpressionCallTO.OriginOfLine stageOrigin = globalCall.getStageOriginOfLine();
            if (propagatingStage) {
                stageOrigin = ExpressionCallTO.OriginOfLine.DESCENT;
                if (stageIds.contains(globalCall.getStageId())) {
                    if (stageIds.size() == 1) {
                        stageOrigin = ExpressionCallTO.OriginOfLine.SELF;
                    } else {
                        stageOrigin = ExpressionCallTO.OriginOfLine.BOTH;
                    }
                }
            }
            ExpressionCallTO updatedGlobalCall =
                    new ExpressionCallTO(
                            (globalCall.getId() != null? 
                                    globalCall.getId(): String.valueOf(this.globalId++)), 
                            globalCall.getGeneId(), globalCall.getAnatEntityId(), 
                            globalCall.getStageId(), 
                            affymetrixData, estData, inSituData, rnaSeqData, 
                            (propagatingAnatomy || globalCall.isIncludeSubstructures()), 
                            (propagatingStage || globalCall.isIncludeSubStages()), 
                            anatOrigin, stageOrigin, 
                            //if this method was not used to do any propagations (DataState 
                            //computations?), we cannot compute observedData, so we use 
                            //the value of the globalCall. Otherwise, we use the value 
                            //we have computed.
                            ((!propagatingAnatomy && !propagatingStage && 
                                    globalCall.isObservedData()) || observedData));

            log.trace("Updated global expression call: {}", updatedGlobalCall);

            // Add the updated global expression call
            globalExprWithExprIds.put(updatedGlobalCall, exprIds);
        } 

        log.debug("Done updating global expression calls.");
        return log.exit(globalExprWithExprIds);
    }

    /**
     * Get the best {@code DataState} between two {@code DataState}s.
     * 
     * @param dataState1    A {@code DataState} to be compare to {@code dataState2}.
     * @param dataState2    A {@code DataState} to be compare to {@code dataState1}.
     * @return              The best {@code DataState} between {@code dataState1} 
     *                      and {@code dataState2}.
     */
    //TODO: unit test this method now that it is not private anymore
    protected DataState getBestDataState(DataState dataState1, DataState dataState2) {
        log.entry(dataState1, dataState2);
        
        if (dataState1.ordinal() < dataState2.ordinal()) {
            return log.exit(dataState2);
        }
        
        return log.exit(dataState1);
    }
}
