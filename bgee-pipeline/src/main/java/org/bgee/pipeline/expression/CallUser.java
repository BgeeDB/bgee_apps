package org.bgee.pipeline.expression;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
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
//FIXME: to remove? Rescue some methods?
// XXX: enable constructor because we need it to generate files and we have no time to remove properly the class
public abstract class CallUser extends MySQLDAOUser {
//    /**
//     * {@code Logger} of the class.
//     */
//    private final static Logger log = LogManager.getLogger(CallUser.class.getName());
//
//    /**
//     * An {@code int} used to generate IDs of global expression or no-expression calls.
//     */        
//    protected int globalId;
//    
//    /**
//     * {@code Comparator} used to order {@code CallTO}s, according to the values returned by 
//     * the methods {@link CallTO#getBgeeGeneId()}, {@link CallTO#getAnatEntityId()}, and 
//     * {@link CallTO#getStageId()}.
//     */
//    //TODO: actually, for the differential expression file, it would be better to order 
//    //the stages by developmental time, not by name.
//    //We need to provide a List where stage IDs are ordered by their left bound. 
//    //Such a List could be returned by a new method in BgeeDBUtils.
//    public static final class CallTOComparator implements Comparator<CallTO>, Serializable {
//
//        private static final long serialVersionUID = 3537157597163398354L;
//        
//        /**
//         * See {@link #CallTOComparator(boolean)} constructor.
//         */
//        private final boolean byAnatomy;
//        
//        /**
//         * Constructor defining how to order {@code CallTO}s for equal gene IDs. 
//         * If {@code byAnatomy} is {@code true}, {@code CallTO}s with equal gene IDs 
//         * will be ordered first by anatEntityId, then by stageId; otherwise, first by 
//         * stageId, then by anatEntityId.
//         * 
//         * @param byAnatomy A {@code boolean} defining which attribute to use first 
//         *                  for {@code CallTO}s with equal gene IDs.
//         */
//        public CallTOComparator(boolean byAnatomy) {
//            this.byAnatomy = byAnatomy;
//        }
//
//        @Override
//        public int compare(CallTO callTO1, CallTO callTO2) {
//            log.entry(callTO1, callTO2);
//            int geneIdComp = callTO1.getBgeeGeneId().compareToIgnoreCase(
//                    callTO2.getBgeeGeneId());
//            if (geneIdComp != 0) {
//                return log.exit(geneIdComp);
//            }
//            int anatEntityIdComp = callTO1.getAnatEntityId().compareToIgnoreCase(
//                    callTO2.getAnatEntityId());
//            int stageIdComp = callTO1.getStageId().compareToIgnoreCase(
//                    callTO2.getStageId());
//            
//            if (this.byAnatomy) {
//                if (anatEntityIdComp != 0) {
//                    return log.exit(anatEntityIdComp);
//                }
//                if (stageIdComp != 0) {
//                    return log.exit(stageIdComp);
//                }
//            } else {
//                if (stageIdComp != 0) {
//                    return log.exit(stageIdComp);
//                }
//                if (anatEntityIdComp != 0) {
//                    return log.exit(anatEntityIdComp);
//                }
//            }
//            
//            return log.exit(0);
//        }
//    }
//
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
//        this.globalId = 1;
    }
//    
//
//
//    /**
//     * Retrieves all expression calls for given species in a {@code Map} associating gene IDs to 
//     * {@code ExpressionCallTO}s, present into the Bgee database.
//     * 
//     * @param speciesIds        A {@code Set} of {@code Integer}s that are the IDs of species 
//     *                          allowing to filter the expression calls to use.
//     * @return                  A {@code LinkedHashMap} associating gene IDs to 
//     *                          {@code ExpressionCallTO}s of the given species. Returns a 
//     *                          {@code LinkedHashMap} to keep the order in which 
//     *                          {@code ExpressionCallTO}s are retrieved.
//     * @throws DAOException     If an error occurred while getting the data from the Bgee database.
//     */
//    protected LinkedHashMap<String, List<ExpressionCallTO>> getExpressionCallsByGeneId(
//            Set<Integer> speciesIds) throws DAOException {
//        log.entry(speciesIds);
//        
//        ExpressionCallParams params = new ExpressionCallParams();
//        params.addAllSpeciesIds(speciesIds);
//
//        log.debug("Generating Map from genes to expression calls for species {}...", 
//                speciesIds);
//        LinkedHashMap<String, List<ExpressionCallTO>> map = 
//                new LinkedHashMap<String, List<ExpressionCallTO>>();
//        try (ExpressionCallTOResultSet exprTORs = 
//                this.getExpressionCallDAO().getExpressionCalls(params)) {
//            while (exprTORs.next()) {
//                ExpressionCallTO exprTO = exprTORs.getTO();
//                log.trace("Expression call: {}", exprTO);
//                List<ExpressionCallTO> curExprAsSet = map.get(exprTO.getBgeeGeneId());
//                if (curExprAsSet == null) {
//                    log.trace("Create new map key: {}", exprTO.getBgeeGeneId());
//                    curExprAsSet = new ArrayList<ExpressionCallTO>();
//                    map.put(exprTO.getBgeeGeneId(), curExprAsSet);
//                }
//                curExprAsSet.add(exprTO);
//            }
//        }
//        log.debug("Done generating Map from genes to expression calls for species {}, {} entries.", 
//                speciesIds, map.size());
//        
//        return log.exit(map);        
//    }
//
//    /**
//     * Retrieves all no-expression calls for given species in a {@code Map} associating gene IDs to 
//     * {@code NoExpressionCallTO}s, present into the Bgee database.
//     * 
//     * @param speciesIds        A {@code Set} of {@code String}s that are the IDs of species 
//     *                          allowing to filter the no-expression calls to use.
//     * @return                  A {@code LinkedHashMap} associating gene IDs to 
//     *                          {@code NoExpressionCallTO}s of the given species. Returns a 
//     *                          {@code LinkedHashMap} to keep the order in which 
//     *                          {@code NoExpressionCallTO}s are retrieved.
//     * @throws DAOException     If an error occurred while getting the data from the Bgee database.
//     */
//    protected LinkedHashMap<String, List<NoExpressionCallTO>> getNoExpressionCallsByGeneId(
//            Set<String> speciesIds) throws DAOException {
//        log.entry(speciesIds);
//
//        NoExpressionCallParams params = new NoExpressionCallParams();
//        params.addAllSpeciesIds(speciesIds);
//        
//        log.debug("Generating Map from genes to no-expression calls for species {}...", 
//                speciesIds);
//        LinkedHashMap<String, List<NoExpressionCallTO>> map = 
//                new LinkedHashMap<String, List<NoExpressionCallTO>>();
//        try (NoExpressionCallTOResultSet noExprTORs = 
//                this.getNoExpressionCallDAO().getNoExpressionCalls(params)) {
//            while (noExprTORs.next()) {
//                NoExpressionCallTO noExprTO = noExprTORs.getTO();
//                log.trace("No-expression call: {}", noExprTO);
//                List<NoExpressionCallTO> curNoExprAsSet = map.get(noExprTO.getBgeeGeneId());
//                if (curNoExprAsSet == null) {
//                    log.trace("Create new map key: {}", noExprTO.getBgeeGeneId());
//                    curNoExprAsSet = new ArrayList<NoExpressionCallTO>();
//                    map.put(noExprTO.getBgeeGeneId(), curNoExprAsSet);
//                }
//                curNoExprAsSet.add(noExprTO);
//            }
//        }
//        log.debug("Done generating Map from genes to no-expression calls for species {}, {} entries.", 
//                speciesIds, map.size());
//        
//        return log.exit(map);        
//    }
//    
//    /**
//     * Group and order the provided {@code CallTO}s, according to the values returned by 
//     * the methods {@code CallTO#getBgeeGeneId()}, {@code CallTO#getAnatEntityId()}, and 
//     * {@code CallTO#getStageId()}. These {@code CallTO}s can be of mixed types,  
//     * for instance mixing {@code ExpressionCallTO}s and {@code NoExpressionCallTO}s, 
//     * and mixing global calls with basic calls. 
//     * <p>
//     * {@code CallTO}s with equal gene-anat.entity-stage are grouped 
//     * in a same {@code Entry} of the returned {@code Map}, with their corresponding key 
//     * being a {@code CallTO} storing this information of gene-anat.entity-stage. 
//     * {@code Entry}s are ordered according to the natural ordering of the IDs 
//     * of the gene, anat. entity, and stage, in that order, stored in the {@code CallTO}s 
//     * used as keys. 
//     * <p>
//     * The {@code Collection}s stored as values in the returned {@code Map} are not {@code Set}s. 
//     * This is because otherwise, a global call with data propagated could erase a basic call 
//     * with no data propagated (or the opposite), as these calls would be seen as equal 
//     * according to their gene-anat.entity-stage. 
//     * <p>
//     * The {@code CallTO}s should return a a not-null not-empty value when calling 
//     * {@code CallTO#getBgeeGeneId()}, {@code CallTO#getAnatEntityId()}, and 
//     * {@code CallTO#getStageId()}, otherwise, an {@code IllegalArgumentException} is thrown. 
//     * 
//     * @param callTOs   A {@code Collection} of {@code CallTO}s to be grouped and ordered. 
//     * @return          A {@code SortedMap} where keys are {@code CallTO}s providing 
//     *                  the information of gene-anat.entity-stage, the associated values 
//     *                  being {@code Collection} of {@code CallTO}s with the corresponding 
//     *                  gene-anat.entity-stage. {@code Entry}s are ordered according to 
//     *                  the natural ordering of the IDs of the gene, anat. entity, and stage, 
//     *                  in that order.
//     * @throws IllegalArgumentException If any of the values returned by 
//     *                                  {@code CallTO#getBgeeGeneId()}, {@code CallTO#getAnatEntityId()}, 
//     *                                  or {@code CallTO#getStageId()} are {@code null} or empty, 
//     *                                  for any of the {@code CallTO}s provided. 
//     */
//    protected SortedMap<CallTO, Collection<CallTO>> groupAndOrderByGeneAnatEntityStage(
//            Collection<CallTO> callTOs) throws IllegalArgumentException {
//        log.entry(callTOs);
//        log.trace("Start sorting and grouping of {} calls...", callTOs.size());
//        
//        SortedMap<CallTO, Collection<CallTO>> aggregateMap = 
//                new TreeMap<CallTO, Collection<CallTO>>(new CallTOComparator(true));
//       
//        for (CallTO callTO: callTOs) {
//            //sanity checks
//            if (StringUtils.isEmpty(callTO.getBgeeGeneId()) || 
//                    StringUtils.isEmpty(callTO.getAnatEntityId()) || 
//                    StringUtils.isEmpty(callTO.getStageId())) {
//                throw log.throwing(new IllegalArgumentException("Invalid CallTO provided: " + 
//                    callTO));
//            }
//            
//            //create a fake CallTO to store the information of geneId-anatEntityId-stageId, 
//            //that will be used as a key in the returned Map. As CallTO is an abstract class, 
//            //we choose a concrete implementation, and we stick to it, to not mix 
//            //different classes in the keyset. 
//            CallTO fakeCallTO = new ExpressionCallTO(null, callTO.getBgeeGeneId(), 
//                    callTO.getAnatEntityId(), callTO.getStageId(), 
//                    null, null, null, null, null, null, null, null, null);
//            Collection<CallTO> aggregatedCalls = aggregateMap.get(fakeCallTO);
//            if (aggregatedCalls == null) {
//                //note that this Collection must absolutely not be a Set, otherwise 
//                //a global expression call could be seen as equal to a basic expression call. 
//                //We declare it as a Collection and not as a List because we don't care about
//                //the iteration order.
//                aggregatedCalls = new ArrayList<CallTO>();
//                aggregateMap.put(fakeCallTO, aggregatedCalls);
//            }
//            aggregatedCalls.add(callTO);
//        }
//
//        log.trace("Done sorting and grouping of {} calls.", callTOs.size());
//        return log.exit(aggregateMap);
//    }
//    
//    /**
//     * Returns the {@code boolean} defining whether the provided {@code CallTO} was generated 
//     * following developmental stage or anatomical entity propagation. 
//     * {@code ExpressionCallTO}s must have a non-null value returned by the method 
//     * {@code isObservedData}; {@code NoExpressionCallTO}s must have a non-null value 
//     * returned by the method {@code getOriginOfLine}.
//     *  
//     * @param callTO    A {@code CallTO} to be checked. 
//     * @return          If {@code true}, the provided {@code CallTO} was generated by propagation 
//     *                  of substructures or sub-stages.
//     * @throws IllegalArgumentException If {@code callTO} is null or not supported, 
//     *                                  or if {@code callTO}s does not allow to determine  
//     *                                  origin of the data. 
//     */
//    protected boolean isPropagatedOnly(CallTO callTO) throws IllegalArgumentException {
//        log.entry(callTO);
//        
//        if (callTO == null) {
//            throw log.throwing(new IllegalArgumentException("The provided CallTO is null"));
//        }
//
//        if (callTO instanceof  ExpressionCallTO) {
//            if (((ExpressionCallTO) callTO).isObservedData() == null) {
//                throw log.throwing(new IllegalArgumentException("The CallTO provided (" +
//                        callTO.getClass() + ") does not allow to determine origin of the data: " + 
//                        callTO.toString()));
//            }
//            if (!((ExpressionCallTO) callTO).isObservedData()) {
//                return log.exit(true);
//            }
//        } else if (callTO instanceof NoExpressionCallTO) {
//            if (((NoExpressionCallTO) callTO).getOriginOfLine() == null) {
//                throw log.throwing(new IllegalArgumentException("The CallTO provided (" +
//                        callTO.getClass() + ") has a null value for origin of line: " + 
//                        callTO.toString()));
//            }
//            if (((NoExpressionCallTO) callTO).getOriginOfLine().
//                        equals(NoExpressionCallTO.OriginOfLine.PARENT)) {
//                return log.exit(true);
//            }
//        } else {
//            throw log.throwing(new IllegalArgumentException("The CallTO provided (" +
//                    callTO.getClass() + ") does not allow to determine origin of the data: " + 
//                    callTO.toString()));
//        }
//        return log.exit(false);
//    }
//    
//    /**
//     * Returns the {@code boolean} defining whether the provided {@code CallTO} is a global call. 
//     * {@code ExpressionCallTO}s must have a non-null value returned by the methods 
//     * {@code isIncludeSubstructures} and {@code isIncludeSubStages}; {@code NoExpressionCallTO}s 
//     * must have a non-null value returned by the method {@code isIncludeParentStructures}.
//     *  
//     * @param callTO    A {@code CallTO} to be checked. 
//     * @return          If {@code true}, the provided {@code CallTO} is a global call.
//     * @throws IllegalArgumentException If {@code callTO} is null or not supported, 
//     *                                  or if {@code callTO}s does not allow to determine  
//     *                                  origin of the data. 
//     */
//    protected boolean isGlobal(CallTO callTO) throws IllegalArgumentException {
//        log.entry(callTO);
//
//        if (callTO == null) {
//            throw log.throwing(new IllegalArgumentException("The provided CallTO is null"));
//        }
//
//        if (callTO instanceof  ExpressionCallTO) {
//            ExpressionCallTO exprTO = (ExpressionCallTO) callTO;
//            if (exprTO.isIncludeSubstructures() != null && exprTO.isIncludeSubstructures() || 
//                exprTO.isIncludeSubStages() != null && exprTO.isIncludeSubStages()) {
//                return log.exit(true);
//            }
//            if (exprTO.isIncludeSubstructures() == null || exprTO.isIncludeSubStages() == null) {
//                throw log.throwing(new IllegalArgumentException("The CallTO provided (" +
//                        callTO.getClass() + ") does not allow to determine whether "
//                      + "substructures/substages were considered: " + callTO.toString()));
//            }
//            return log.exit(false);
//            
//        } else if (callTO instanceof NoExpressionCallTO) {
//            NoExpressionCallTO noExprTO = (NoExpressionCallTO) callTO;
//            if (noExprTO.isIncludeParentStructures() == null) {
//                throw log.throwing(new IllegalArgumentException("The CallTO provided (" 
//                      + callTO.getClass() + ") does not allow to determine whether "
//                      + "parent structures were considered: " + callTO.toString()));                
//            }
//            return log.exit(noExprTO.isIncludeParentStructures());
//            
//        } else {
//            throw log.throwing(new IllegalArgumentException("The CallTO provided (" +
//                    callTO.getClass() + ") is not managed for expression/no-expression data: " + 
//                    callTO.toString()));
//        }
//    }
//        
//    protected boolean isCallWithNoData(CallTO call) {
//        log.entry(call);
//
//        if (call == null) {
//            throw log.throwing(new IllegalArgumentException("The provided CallTO is null"));
//        }
//        if ((call.getAffymetrixData() !=null && !call.getAffymetrixData().equals(DataState.NODATA)) || 
//                (call.getInSituData() !=null && !call.getInSituData().equals(DataState.NODATA)) || 
//                (call.getRNASeqData() !=null && !call.getRNASeqData().equals(DataState.NODATA))) {
//            return log.exit(false);
//        }
//        if (call instanceof ExpressionCallTO) {
//            if (((ExpressionCallTO) call).getESTData() != null && 
//                    !((ExpressionCallTO) call).getESTData().equals(DataState.NODATA)) {
//                return log.exit(false);
//            }
//        } else if (call instanceof NoExpressionCallTO) {
//            if (((NoExpressionCallTO) call).getESTData() != null &&
//                    !((NoExpressionCallTO) call).getRelaxedInSituData().equals(DataState.NODATA)) {
//                return log.exit(false);
//            }
//        } else {
//            throw log.throwing(new IllegalArgumentException("The CallTO provided (" +
//                    call.getClass() + ") is not managed for expression/no-expression data: " + 
//                    call.toString()));
//        }
//        return log.exit(true);
//    }
//
//
//    /**
//     * Propagate {@code ExpressionCallTO}s contained in {@code expressionTOs} 
//     * to parent anatomical entities or to parent developmental stages, depending on 
//     * {@code anatomyPropagation}, using the relations provided through 
//     * {@code parentsFromChildren}.
//     * <p>
//     * This method generates a {@code Map} where keys are the generated propagated 
//     * {@code ExpressionCallTO}s, the associated value containing all provided 
//     * {@code ExpressionCallTO}s it was generated from. {@link #updateGlobalExpressions(
//     * Map, boolean, boolean)} should then be called to compute correct parameters 
//     * for these propagated {@code ExpressionCallTO}s.
//     * <p>
//     * If {@code anatomyPropagation} is {@code true}, {@code parentsFromChildren} 
//     * should contain the relations {@code IS_A PART_OF} between anatomical entities, of all status 
//     * ({@code REFLEXIVE}, {@code INDIRECT}, {@code DIRECT}), with child IDs as keys, see 
//     * {@link org.bgee.pipeline.BgeeDBUtils#getAnatEntityParentsFromChildren(Set, org.bgee.model.dao.api.ontologycommon.RelationDAO)}.
//     * <p>
//     * If {@code anatomyPropagation} is {@code false}, {@code parentsFromChildren} 
//     * should contain the relations {@code IS_A PART_OF} between stages, of all status 
//     * ({@code REFLEXIVE}, {@code INDIRECT}, {@code DIRECT}), with child IDs as keys, see 
//     * {@link org.bgee.pipeline.BgeeDBUtils#getStageChildrenFromParents(Set, org.bgee.model.dao.api.ontologycommon.RelationDAO)}.
//     * 
//     * @param expressionTOs         A {@code Collection} of {@code ExpressionCallTO}s containing 
//     *                              all expression calls to propagate. It is a {@code Collection} 
//     *                              so that it can contain equal {@code ExpressionCallTO}s, 
//     *                              but with different propagation status.
//     * @param parentsFromChildren   A {@code Map} where keys are IDs of anatomical entities 
//     *                              or developmental stages that are sources of a relation, 
//     *                              the associated value being a {@code Set} of {@code String}s 
//     *                              that are the IDs of their associated targets.
//     * @param anatomyPropagation    A {@code boolean} defining whether {@code ExpressionCallTO}s 
//     *                              should be propagated to parent anatomical entities 
//     *                              (if {@code true}), or to parent stages (if {@code false}).
//     * @return                      A {@code Map} where keys are propagated global 
//     *                              {@code ExpressionCallTO}s, the associated value being 
//     *                              a {@code Set} containing the {@code ExpressionCallTO}s  
//     *                              it was generated from.
//     * @see #updateGlobalExpressions(Map, boolean, boolean)
//     */
//    //NOTE: this method does not call updateGlobalExpressions anymore, to provide better 
//    //unicity of the method, and allow better unit testing
//    protected Map<ExpressionCallTO, Set<ExpressionCallTO>> groupExpressionCallTOsByPropagatedCalls(
//            Collection<ExpressionCallTO> expressionTOs, Map<String, Set<String>> parentsFromChildren, 
//            boolean anatomyPropagation) {
//        log.entry(expressionTOs, parentsFromChildren, anatomyPropagation);
//        log.trace("Generating propagated calls (to anatomy? {} - to stages? {})...", 
//                anatomyPropagation, !anatomyPropagation);
//        
//        Map<ExpressionCallTO, Set<ExpressionCallTO>> mapGlobalExpr = 
//                new HashMap<ExpressionCallTO, Set<ExpressionCallTO>>();
//        int i = 0;
//        int exprTOCount = expressionTOs.size();
//        for (ExpressionCallTO exprCallTO : expressionTOs) {
//            i++;
//            if (log.isDebugEnabled() && i % 100000 == 0) {
//                log.debug("{}/{} expression calls analyzed.", i, exprTOCount);
//            }
//            log.trace("Propagation for expression call: {}", exprCallTO);
//            String childId = null;
//            if (anatomyPropagation) {
//                childId = exprCallTO.getAnatEntityId();
//            } else {
//                childId = exprCallTO.getStageId();
//            }
//            Set<String> parents = parentsFromChildren.get(childId);
//            //the relations include a reflexive relation, where sourceId == targetId, 
//            //this will allow to also include the actual not-propagated calls. 
//            //we should always have at least a reflexive relation, so, if there is 
//            //not a least one "parents" , something is wrong in the database. 
//            if (parents == null) {
//                IllegalStateException e = new IllegalStateException("The anatomical or stage entity " +
//                        childId + " is not defined as existing " +
//                        "in the species of gene " + exprCallTO.getBgeeGeneId() + 
//                        ", while it has expression data in it.");
//                log.debug("Throwing exception {}, offending call: {}, relations used: {}", 
//                        e, exprCallTO, parentsFromChildren);
//                throw log.throwing(e);
//            }
//            for (String parentId : parents) {
//                log.trace("Propagation of the current expression to parent: {}", parentId);
//                // Set ID to null to be able to compare keys of the map on 
//                // gene ID, anatomical entity ID, and stage ID.
//                // Add propagated expression call.
//                String newAnatEntityId = exprCallTO.getAnatEntityId();
//                String newStageId = exprCallTO.getStageId();
//                if (anatomyPropagation) {
//                    newAnatEntityId = parentId;
//                } else {
//                    newStageId = parentId;
//                }
//                ExpressionCallTO propagatedExpression = new ExpressionCallTO(
//                        null, 
//                        exprCallTO.getBgeeGeneId(),
//                        newAnatEntityId,
//                        newStageId,
//                        DataState.NODATA,      
//                        DataState.NODATA,
//                        DataState.NODATA,
//                        DataState.NODATA,
//                        false,
//                        false,
//                        ExpressionCallTO.OriginOfLine.SELF, 
//                        ExpressionCallTO.OriginOfLine.SELF,
//                        null);
//                
//                log.trace("Add the propagated expression: {}", propagatedExpression);
//                Set<ExpressionCallTO> curExprAsSet = mapGlobalExpr.get(propagatedExpression);
//                if (curExprAsSet == null) {
//                    curExprAsSet = new HashSet<ExpressionCallTO>();
//                    mapGlobalExpr.put(propagatedExpression, curExprAsSet);
//                }
//                curExprAsSet.add(exprCallTO);
//            }
//        }
//
//        log.trace("Done generating propagated calls.");
//        return log.exit(mapGlobalExpr);        
//    }
//
//    /**
//     * Generates correct global propagated {@code ExpressionCallTO}s using {@code globalMap}, 
//     * with IDs generated, correct values of {@code DataState} for each data type, 
//     * correct values of {@code OriginOfLine} for both stage and anatomical entity 
//     * origins, and correct value of {@code ObservedData}. This method is used when 
//     * propagating {@code ExpressionCallTO}s to parent anatomical entities and/or parent 
//     * developmental stages. The arguments {@code propagatingAnatomy} and 
//     * {@code propagatingStage} allow to specify what is being propagated. 
//     * <p> 
//     * {@code globalMap} is a {@code Map} associating global {@code ExpressionCallTO}s, 
//     * as keys, to a {@code Set} of {@code ExpressionCallTO}s they were generated from. 
//     * The following computations will be performed for each global call: 
//     * <ul>
//     * <li>the best {@code DataState}s will be computed, for each data type, 
//     * from all the associated {@code ExpressionCallTO}s.
//     * <li>the anatomical {@code OriginOfLine} will be 
//     * computed from all the associated {@code ExpressionCallTO}s.
//     * <li>if {@code propagatingAnatomy} is true, the value of {@code includeSubstructures} 
//     * will be set to true, otherwise, the value returned by the method 
//     * {@code isIncludeSubstructures} of the provided calls will be used.
//     * <li>the stage {@code OriginOfLine} will be 
//     * computed from all the associated {@code ExpressionCallTO}s. 
//     * <li>if {@code propagatingStage} is true, the value of {@code includeSubStages} 
//     * will be set to true, otherwise, the value returned by the method 
//     * {@code isIncludeSubStages} of the provided calls will be used.
//     * <li>for the value defining whether the call was actually observed,  
//     * will also be computed by this method. 
//     * <li>The ID of the global call will be used, unless it is {@code null}; in that case, 
//     * the IDs will be auto-generated by incrementing {@link #globalId}.
//     * <p>
//     * The returned {@code Map} stores the updated correct {@code ExpressionCallTO}s as keys, 
//     * associated to a {@code Set} of {@code String}s that are the IDs of the calls 
//     * they were generated from. 
//     * <p>
//     * The provided {@code Map} will be modified and emptied to free up some memory.
//     * 
//     * @param globalMap     A {@code Map} associating {@code ExpressionCallTO}s to be updated 
//     *                      as keys, to {@code Set}s of {@code ExpressionCallTO}s they were 
//     *                      generated from as values.
//     * @param propagatingAnatomy    A {@code boolean} defining whether the global calls 
//     *                              are being generating by propagating to parent anatomical 
//     *                              entities.
//     * @param propagatingStage      A {@code boolean} defining whether the global calls 
//     *                              are being generating by propagating to parent developmental 
//     *                              stages.
//     * @return              A {@code Map} associating updated {@code ExpressionCallTO}s to 
//     *                      {@code Set}s of {@code String}s that are the IDs of the 
//     *                      {@code ExpressionCallTO}s they were generated from.
//     */
//    protected Map<ExpressionCallTO, Set<String>> updateGlobalExpressions(
//            Map<ExpressionCallTO, Set<ExpressionCallTO>> globalMap, 
//            boolean propagatingAnatomy, boolean propagatingStage) {
//        log.entry(globalMap, propagatingAnatomy, propagatingStage);
//        if (!propagatingAnatomy && !propagatingStage) {
//            throw log.throwing(new IllegalArgumentException("If you have nothing " +
//            		"to propagate, why using this method?"));
//        }
//        
//        log.trace("Updating expression calls...");
//        // Create a Map associating generated expression calls to expression call IDs.
//        Map<ExpressionCallTO, Set<String>> globalExprWithExprIds =
//                    new HashMap<ExpressionCallTO, Set<String>>();
//
//        // Create a Set from keySet to be able to modify globalMap.
//        Set<ExpressionCallTO> tmpGlobalCalls = new HashSet<ExpressionCallTO>(globalMap.keySet());
//        int i = 0;
//        int globalExprTOCount = tmpGlobalCalls.size();
//        for (ExpressionCallTO globalCall: tmpGlobalCalls) {
//            i++;
//            if (log.isDebugEnabled() && i % 100000 == 0) {
//                log.debug("{}/{} expression calls analyzed.", i, globalExprTOCount);
//            }
//            // Remove generic global call, get associated calls
//            Set<ExpressionCallTO> calls = globalMap.remove(globalCall);
//
//            log.trace("Update expression calls: {}; with: {}", globalCall, calls);
//            
//            // Define the best DataType of the global call according to all calls
//            DataState affymetrixData = DataState.NODATA, estData = DataState.NODATA, 
//                    inSituData = DataState.NODATA, rnaSeqData = DataState.NODATA;
//            //to determine global anatomy origin of line
//            Set<ExpressionCallTO.OriginOfLine> anatOriginOfLines = 
//                    EnumSet.noneOf(ExpressionCallTO.OriginOfLine.class);
//            //to determine global stage origin of line
//            Set<ExpressionCallTO.OriginOfLine> stageOriginOfLines = 
//                    EnumSet.noneOf(ExpressionCallTO.OriginOfLine.class);
//            //check whether the global call was actually observed with no propagation 
//            //at least once
//            boolean observedData = false;
//            //to associate global calls to the provided expression IDs
//            Set<String> exprIds = new HashSet<String>();
//            //to check that all provided calls have consistent includeSubstructures and 
//            //includeSubStages states, and to store it.
//            Boolean includeSubstructures = null;
//            Boolean includeSubStages = null;
//            
//            for (ExpressionCallTO call: calls) {
//                //********* sanity checks ************
//                if (call.isIncludeSubstructures() == null || call.isIncludeSubStages() == null) {
//                    throw log.throwing(new IllegalArgumentException("Information " +
//                    		"includeSubstructures and includeSubStages must be provided."));
//                }
//                if (!globalCall.getBgeeGeneId().equals(call.getBgeeGeneId())) {
//                    throw log.throwing(new IllegalArgumentException("Incorrect grouping " +
//                            "according to gene ID. Global call: " + globalCall + 
//                            ", basic call: " + call));
//                }
//                //if we want to propagate to anatomy, cannot use calls already propagated to anatomy
//                if (propagatingAnatomy && call.isIncludeSubstructures()) {
//                    throw log.throwing(new IllegalArgumentException("Propagation using anatomy " +
//                            "is requested, but basic call is already propagated: " + call));
//                }
//                //if we are not propagating to parent anat. entities, the global call 
//                //and all the basic calls should occur in the same anat. entity.
//                if (!propagatingAnatomy && 
//                        !globalCall.getAnatEntityId().equals(call.getAnatEntityId())) {
//                    throw log.throwing(new IllegalArgumentException("No propagation " +
//                            "on anatomy requested, but anatomical entity of global and basic " +
//                            "calls differ. Global call: " + globalCall + ", basic call: " + 
//                            call));
//                }
//                //if we want to propagate to stages, cannot use calls already propagated to stages
//                if (propagatingStage && call.isIncludeSubStages()) {
//                    throw log.throwing(new IllegalArgumentException("Propagation using stages " +
//                            "is requested, but basic call is already propagated: " + call));
//                }
//                //if we are not propagating to parent stages, the global call 
//                //and all the basic calls should occur in the same stage.
//                if (!propagatingStage && 
//                        !globalCall.getStageId().equals(call.getStageId())) {
//                    throw log.throwing(new IllegalArgumentException("No propagation " +
//                            "on stage requested, but stage of global and basic " +
//                            "calls differ. Global call: " + globalCall + ", basic call: " + 
//                            call));
//                }
//                
//                //********* OK, all sanity checks passed ************
//                //determine global includeSubstructures state
//                if (includeSubstructures != null && 
//                        includeSubstructures != call.isIncludeSubstructures()) {
//                    throw log.throwing(new IllegalStateException("Provided calls have " +
//                            "inconsistent includeSubstructures values."));
//                }
//                includeSubstructures = call.isIncludeSubstructures();
//                //determine global includeSubStages state
//                if (includeSubStages != null && 
//                        includeSubStages != call.isIncludeSubStages()) {
//                    throw log.throwing(new IllegalStateException("Provided calls have " +
//                            "inconsistent includeSubStages values."));
//                }
//                includeSubStages = call.isIncludeSubStages();
//                
//                //get best data states
//                affymetrixData = getBestDataState(affymetrixData, call.getAffymetrixData());
//                estData = getBestDataState(estData, call.getESTData());
//                inSituData = getBestDataState(inSituData, call.getInSituData());
//                rnaSeqData = getBestDataState(rnaSeqData, call.getRNASeqData());
//                
//                //get expression IDs
//                if (call.getId() != null) {
//                    exprIds.add(call.getId());
//                }
//                
//                //anatomy origin of line.
//                //if we are not propagating to parent anat. entities, we use the value 
//                //of the iterated call.
//                ExpressionCallTO.OriginOfLine anatOriginOfLine = call.getAnatOriginOfLine();
//                //if we are propagating to parent anat. entities, we check whether 
//                //the global call occurs in the same anat. entity as the iterated call; 
//                //in that case, the origin of line will be SELF; otherwise, DESCENT.
//                if (propagatingAnatomy) { 
//                    if (call.getAnatEntityId().equals(globalCall.getAnatEntityId())) {
//                        anatOriginOfLine = ExpressionCallTO.OriginOfLine.SELF;
//                    } else {
//                        anatOriginOfLine = ExpressionCallTO.OriginOfLine.DESCENT;
//                    }
//                } else if (call.getAnatOriginOfLine() == null) {
//                    throw log.throwing(new IllegalArgumentException("The anatomy origin of line " +
//                    		"must be provided when not propagating to anatomy"));
//                }
//                //in order to infer the global origin of line
//                anatOriginOfLines.add(anatOriginOfLine);
//
//                //stage origin of line.
//                //if we are not propagating to parent stages, we use the value 
//                //of the iterated call.
//                ExpressionCallTO.OriginOfLine stageOriginOfLine = call.getStageOriginOfLine();
//                //if we are propagating to parent stages, we check whether 
//                //the global call occurs in the same stage as the iterated call; 
//                //in that case, the origin of line will be SELF; otherwise, DESCENT.
//                if (propagatingStage) { 
//                    if (call.getStageId().equals(globalCall.getStageId())) {
//                        stageOriginOfLine = ExpressionCallTO.OriginOfLine.SELF;
//                    } else {
//                        stageOriginOfLine = ExpressionCallTO.OriginOfLine.DESCENT;
//                    }
//                } else if (call.getStageOriginOfLine() == null) {
//                    throw log.throwing(new IllegalArgumentException("The stage origin of line " +
//                            "must be provided when not propagating to parent stages"));
//                }
//                //in order to infer the global origin of line
//                stageOriginOfLines.add(stageOriginOfLine);
//                
//                //whether the call was actually observed
//                if (!anatOriginOfLine.equals(ExpressionCallTO.OriginOfLine.DESCENT) && 
//                        !stageOriginOfLine.equals(ExpressionCallTO.OriginOfLine.DESCENT)) {
//                    observedData = true;
//                }
//            }
//            
//            //compute global anat origin of lines
//            ExpressionCallTO.OriginOfLine globalAnatOriginOfLine = null;
//            if (anatOriginOfLines.contains(ExpressionCallTO.OriginOfLine.BOTH) || 
//                    (anatOriginOfLines.contains(ExpressionCallTO.OriginOfLine.SELF) && 
//                     anatOriginOfLines.contains(ExpressionCallTO.OriginOfLine.DESCENT))) {
//                globalAnatOriginOfLine = ExpressionCallTO.OriginOfLine.BOTH;
//            } else if (anatOriginOfLines.contains(ExpressionCallTO.OriginOfLine.SELF)) {
//                globalAnatOriginOfLine = ExpressionCallTO.OriginOfLine.SELF;
//            } else if (anatOriginOfLines.contains(ExpressionCallTO.OriginOfLine.DESCENT)) {
//                globalAnatOriginOfLine = ExpressionCallTO.OriginOfLine.DESCENT;
//            } else {
//                throw log.throwing(new AssertionError("Supposed to be no other possibilities..."));
//            }
//            //compute global stage origin of lines
//            ExpressionCallTO.OriginOfLine globalStageOriginOfLine = null;
//            if (stageOriginOfLines.contains(ExpressionCallTO.OriginOfLine.BOTH) || 
//                    (stageOriginOfLines.contains(ExpressionCallTO.OriginOfLine.SELF) && 
//                     stageOriginOfLines.contains(ExpressionCallTO.OriginOfLine.DESCENT))) {
//                globalStageOriginOfLine = ExpressionCallTO.OriginOfLine.BOTH;
//            } else if (stageOriginOfLines.contains(ExpressionCallTO.OriginOfLine.SELF)) {
//                globalStageOriginOfLine = ExpressionCallTO.OriginOfLine.SELF;
//            } else if (stageOriginOfLines.contains(ExpressionCallTO.OriginOfLine.DESCENT)) {
//                globalStageOriginOfLine = ExpressionCallTO.OriginOfLine.DESCENT;
//            } else {
//                throw log.throwing(new AssertionError("Supposed to be no other possibilities..."));
//            }
//            
//            ExpressionCallTO updatedGlobalCall =
//                    new ExpressionCallTO(
//                            (globalCall.getId() != null? 
//                                    globalCall.getId(): String.valueOf(this.globalId++)), 
//                            globalCall.getBgeeGeneId(), globalCall.getAnatEntityId(), 
//                            globalCall.getStageId(), 
//                            affymetrixData, estData, inSituData, rnaSeqData, 
//                            (propagatingAnatomy || includeSubstructures), 
//                            (propagatingStage || includeSubStages), 
//                            globalAnatOriginOfLine, 
//                            globalStageOriginOfLine, 
//                            observedData);
//
//            log.trace("Updated global expression call: {}", updatedGlobalCall);
//
//            // Add the updated global expression call
//            globalExprWithExprIds.put(updatedGlobalCall, exprIds);
//        } 
//
//        log.trace("Done updating global expression calls.");
//        return log.exit(globalExprWithExprIds);
//    }
//
//    /**
//     * Get the best {@code DataState} between two {@code DataState}s.
//     * 
//     * @param dataState1    A {@code DataState} to be compare to {@code dataState2}.
//     * @param dataState2    A {@code DataState} to be compare to {@code dataState1}.
//     * @return              The best {@code DataState} between {@code dataState1} 
//     *                      and {@code dataState2}.
//     */
//    protected DataState getBestDataState(DataState dataState1, DataState dataState2) {
//        log.entry(dataState1, dataState2);
//        
//        if (dataState1.ordinal() < dataState2.ordinal()) {
//            return log.exit(dataState2);
//        }
//        
//        return log.exit(dataState1);
//    }
}
